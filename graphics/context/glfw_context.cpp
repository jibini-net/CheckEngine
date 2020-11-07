#include "glfw_context.h"

#include "util/intrinsics/singleton.h"

void destroy_glfw_window::operator() (GLFWwindow* ptr)
{
	// Relinquish the context from the current thread
	glfwMakeContextCurrent(NULL);

	// Schedule a main-thread task to destroy the context
	global_glfw_context::instance->thread_queue.push_back([ptr]
	{
		glfwDestroyWindow(ptr);
	});

	glfwPostEmptyEvent();
}


global_glfw_context::global_glfw_context()
{
	if (glfwInit() != GLFW_TRUE)
	{
		glfwTerminate();

		throw std::exception("GLFW failed to initialize; check system requirements and libraries");
	}
}

global_glfw_context::~global_glfw_context()
{
	glfwTerminate();
}

void global_glfw_context::execute_queue()
{
	while (this->thread_queue.size() > 0)
	{
		this->thread_queue.back()();
		this->thread_queue.pop_back();
	}
}

void global_glfw_context::park_thread()
{
	while (active_contexts > 0)
	{
		glfwWaitEventsTimeout(0.1);

		execute_queue();
	}

	execute_queue();
}

// A global singleton instance of this context for all threads
std::shared_ptr<global_glfw_context> global_glfw_context::instance = per_thread<global_glfw_context>::get_or_create();


glfw_context::glfw_context(int context_version)
{
	glfwDefaultWindowHints();

	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, context_version / 10);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, context_version % 10);

	glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

	this->pointer.reset(glfwCreateWindow(1366, 910, "", NULL, NULL));
	this->make_current();

	// Relinquish all contexts on the main thread
	glfwMakeContextCurrent(NULL);

	global_glfw_context::instance->active_contexts++;
}

glfw_context::~glfw_context()
{
	global_glfw_context::instance->active_contexts--;
}

void glfw_context::make_current()
{
	glfwMakeContextCurrent(this->pointer.get());
}

void bootable_game::park_thread()
{
	this->context->make_current();

	per_thread<glfw_context>::set(this->context);

	per_thread<glfw_window>::get_or_create();
	//per_thread<glfw_keyboard>::get_or_create();
	//per_thread<glfw_mouse>::get_or_create();

	per_thread<glfw_window>::get_or_create()->show();

	while (glfwWindowShouldClose(this->context->pointer.get()) != GLFW_TRUE)
	{
		temp_update();

		per_thread<glfw_window>::get_or_create()->swap_buffers();
	}
}
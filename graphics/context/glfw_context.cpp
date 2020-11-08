#include "glfw_context.h"

#include <thread>
#include <string>

#include "util/intrinsics/singleton.h"

// Local implementation logger instance
logger _log("GLFW Context");

void destroy_glfw_window::operator() (GLFWwindow* ptr)
{
	// Relinquish the context from the current thread
	glfwMakeContextCurrent(NULL);

	// Schedule a main-thread task to destroy the context
	(global_glfw_context::instance->thread_queue).add(
		new std::function<bool()>([ptr]() -> bool
		{
			glfwDestroyWindow(ptr);
		
			// Return true to indicate this lambda should be deleted once executed
			return true;
		}));

	glfwPostEmptyEvent();
}


global_glfw_context::global_glfw_context()
{
	if (glfwInit() != GLFW_TRUE)
	{
		_log.error("GLFW failed to initialize; check system requirements and libraries");

		glfwTerminate();
		throw std::exception();
	}
}

global_glfw_context::~global_glfw_context()
{
	glfwTerminate();
}

void global_glfw_context::execute_queue()
{
	while ((this->thread_queue).get_size() > 0)
	{
		auto *first = (this->thread_queue).remove();

		// If the lambda returns true, delete once executed
		if ((*first)())
			delete first;
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

	_log.debug("Successfully created context version "
		+ std::to_string(context_version / 10)
		+ "." + std::to_string(context_version % 10));

	if (glewInit() != GLEW_OK)
	{
		_log.error("GLEW failed to initialize; check system requirements and libraries");

		glfwTerminate();
		throw std::exception();
	}

	_log.debug("Successfully initialized GLEW bindings for new context");

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
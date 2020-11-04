#pragma once

#include "GLFW/glfw3.h"

#include <vector>
#include <functional>
#include <memory>

// Destruction class for GLFWwindow* smart-pointers
class destroy_glfw_window
{
public:
	// GLFWwindow* deletion implemenetation for smart-pointers
	void operator() (GLFWwindow* ptr);
};


// The overarching GLFW library context for all GLFW contexts
class global_glfw_context
{
protected:
	// Counter of how many contexts are currently alive
	int active_contexts = 0;

public:
	// A global singleton instance of this context for all threads
	static std::shared_ptr<global_glfw_context> instance;


	// Global GLFW library initialization and error check
	global_glfw_context();
	// Global GLFW library termination and destruction
	~global_glfw_context();


	// A queue of tasks to execute on the main application thread
	std::vector<std::function<void()>> thread_queue;


	// Iterates through and executes all tasks in the queue
	void execute_queue();

	// Sits on the main thread and infinitely polls the keyboard and executes tasks
	void park_thread();


	// Allow each GLFW context to access this global context fully
	friend class glfw_context;
};


class glfw_context
{
protected:
	// Internal unique context and window pointer
	std::unique_ptr<GLFWwindow, destroy_glfw_window> glfw_window;

public:
	// Initializes an invisible window with OpenGL context
	glfw_context(int context_version);
	// Decrements the number of active contexts when destructing
	~glfw_context();


	// Convenience method for making a GLFW context current
	void make_current();


	// Allow each bootable game to access its context fully
	friend class bootable_game;
};


//TODO MOVE OUT
class bootable_game
{
protected:
	std::unique_ptr<glfw_context> context;

public:
	bootable_game()
	{
		this->context.reset(new glfw_context(33));
	}

	void park_thread()
	{
		this->context->make_current();

		while (glfwWindowShouldClose(this->context->glfw_window.get()) != GLFW_TRUE)
		{


			glfwSwapBuffers(this->context->glfw_window.get());
		}
	}
};
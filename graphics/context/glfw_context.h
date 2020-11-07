#pragma once

#define GLEW_STATIC

#include "GL/glew.h"
#include "GLFW/glfw3.h"

#include <vector>
#include <functional>
#include <memory>

#include "glfw_window.h"

#include "util/collection/linked_list.h"

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
	linked_list<std::function<bool()>> thread_queue;


	// Iterates through and executes all tasks in the queue
	void execute_queue();

	// Sits on the main thread and infinitely polls the keyboard and executes tasks
	void park_thread();


	// Allow each GLFW context to access this global context fully
	friend class glfw_context;
};


class glfw_context
{
public:
	// Internal unique context and window pointer
	std::unique_ptr<GLFWwindow, destroy_glfw_window> pointer;

	// Empty constructor required by singleton implementation
	glfw_context()
	{ };

	// Initializes an invisible window with OpenGL context
	glfw_context(int context_version);
	// Decrements the number of active contexts when destructing
	~glfw_context();


	// Convenience method for making a GLFW context current
	void make_current();
};


//TODO MOVE OUT
class bootable_game
{
protected:
	std::shared_ptr<glfw_context> context;

	//TEMP
	std::function<void()> temp_start;
	std::function<void()> temp_update;

public:
	bootable_game(std::function<void()> temp_start, std::function<void()> temp_update);


	void park_thread();

	void boot_thread();
};
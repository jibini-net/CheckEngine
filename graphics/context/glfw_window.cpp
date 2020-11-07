#include "glfw_window.h"

glfw_window::glfw_window()
{
	this->window = per_thread<glfw_context>::get_or_create()->pointer.get();
}

glfw_window::~glfw_window()
{
	this->hide();
}

void glfw_window::show()
{
	glfwShowWindow(window);
}

void glfw_window::hide()
{
	glfwHideWindow(window);
}

void glfw_window::swap_buffers()
{
	glfwGetFramebufferSize(this->window, &this->width, &this->height);

	glfwSwapBuffers(this->window);
}

int glfw_window::get_width()
{
	return this->width;
}

int glfw_window::get_height()
{
	return this->height;
}
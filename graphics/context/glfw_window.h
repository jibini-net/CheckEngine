#pragma once

#include <memory>

#include "util/intrinsics/singleton.h"

#include "glfw_context.h"

class glfw_window
{
protected:
	GLFWwindow *window = nullptr;

	int width = 1366;
	int height = 910;

public:
	glfw_window();

	~glfw_window();


	void swap_buffers();


	void show();

	void hide();

	
	int get_width();

	int get_height();
};
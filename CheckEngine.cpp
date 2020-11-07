#include "graphics/context/glfw_context.h"

#include "util/intrinsics/singleton.h"

void start()
{
	
}

void update()
{
	auto window = per_thread<glfw_window>::get_or_create();
	auto window_ratio = (float)window->get_width() / window->get_height();

	glViewport(0, 0, window->get_width(), window->get_height());

	/*
	glClear(GL_COLOR_BUFFER_BIT);
	glLoadIdentity();

	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(-window_ratio, window_ratio, -1.0, 1.0, -1.0, 1.0);

	glMatrixMode(GL_MODELVIEW);

	glBegin(GL_QUADS);
	glVertex2f(-0.5f, -0.5f);
	glVertex2f(0.5f, -0.5f);
	glVertex2f(0.5f, 0.5f);
	glVertex2f(-0.5f, 0.5f);
	glEnd();
	*/
}

int main()
{
	bootable_game(start, update).boot_thread();

	per_thread<global_glfw_context>::get_or_create()->park_thread();
	per_thread<global_glfw_context>::remove_reference();


	return 0;
}
#include "bootable_game.h"

#include <thread>

bootable_game::bootable_game(std::function<void()> temp_start, std::function<void()> temp_update)
{
	this->context.reset(new glfw_context(43));

	this->temp_start = temp_start;
	this->temp_update = temp_update;
}

void bootable_game::park_thread()
{
	_log.info("Establishing current thread's OpenGL context . . .");
	per_thread<glfw_context>::set(this->context);
	per_thread<glfw_context>::get_or_create()->make_current();

	_log.info("Initializing additional window and user input devices . . .");
	auto window = per_thread<glfw_window>::get_or_create();
	_log.debug("Successfully initialized window utilities");
	//per_thread<glfw_keyboard>::get_or_create();
	_log.debug("Successfully initialized keyboard utilities");
	//per_thread<glfw_mouse>::get_or_create();
	_log.debug("Successfully initialized mouse utilities");

	_log.info("Invoking application initialization section . . .");
	temp_start();

	_log.debug("Showing application window and entering game loop . . .");
	per_thread<glfw_window>::get_or_create()->show();

	init_time.update();
	_log.info("Initialization successfully completed! \033[1;33m(" + std::to_string(init_time.delta_time()) + "s)");

	while (glfwWindowShouldClose(this->context->pointer.get()) != GLFW_TRUE)
	{
		temp_update();

		window->swap_buffers();
	}

	per_thread<glfw_window>::remove_reference();
	//per_thread<glfw_keyboard>::remove_reference();
	//per_thread<glfw_mouse>::remove_reference();

	per_thread<glfw_window>::remove_reference();
}

void bootable_game::boot_thread()
{
	_log.info("Spawning game application thread and context . . .");
	std::thread(&bootable_game::park_thread, *this).detach();
}
#pragma once

#include <functional>
#include <memory>
#include <string>

#include "graphics/context/glfw_context.h"

#include "util/diagnostic/timing/delta_timer.h"
#include "util/diagnostic/logging/logger.h"

class bootable_game
{
private:
	// Local implementation logger instance
	logger _log { "Game Lifecycle" };

protected:
	std::shared_ptr<glfw_context> context;

	delta_timer init_time;

	//TEMP
	std::function<void()> temp_start;
	std::function<void()> temp_update;

public:
	bootable_game(std::function<void()> temp_start, std::function<void()> temp_update);


	void park_thread();

	void boot_thread();
};
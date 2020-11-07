#include <thread>

#include "graphics/context/glfw_context.h"

#include "util/intrinsics/singleton.h"
#include "util/intrinsics/singleton.cpp"

void update()
{

}

int main()
{
	std::thread(&bootable_game::park_thread, bootable_game(update)).detach();

	per_thread<global_glfw_context>::get_or_create()->park_thread();
	per_thread<global_glfw_context>::remove_reference();

	return 0;
}
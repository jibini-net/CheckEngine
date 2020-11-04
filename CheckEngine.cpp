#include <iostream>
#include <thread>

#include "graphics/context/glfw_context.h"

#include "util/intrinsics/singleton.h"
#include "util/intrinsics/singleton.cpp"

#include "util/collection/tree_sort.h"
#include "util/collection/tree_sort.cpp"

int main()
{
	tree_sort<int> sort([](int *compare, int *to) -> int
	{
		return *compare - *to;
	});

	for (int i = 0; i < 20; i++)
	{
		int *value = new int;
		*value = rand() % 256 - 128;

		std::cout << *value << " ";

		sort.place(value);
	}

	std::cout << std::endl;

	sort.iterator()->for_each([](int *element) -> void
	{
		std::cout << *element << " ";
	});

	std::cout << std::endl;

	std::thread(&bootable_game::park_thread, bootable_game()).detach();
	
	per_thread<global_glfw_context>::get_or_create()->park_thread();
	per_thread<global_glfw_context>::remove_reference();

	return 0;
}
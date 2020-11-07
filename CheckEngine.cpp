#include <iostream>
#include <thread>

#include "graphics/context/glfw_context.h"

#include "util/intrinsics/singleton.h"
#include "util/collection/tree_sort.h"

void update()
{

}

int main()
{
	tree_sort<double> sort([](double *compare, double *to) -> int
	{
		if (*compare < *to)
			return -1;
		else if (*compare > *to)
			return 1;
		else
			return 0;
	});

	const int num_elements = 64;
	double elements[num_elements];

	for (int i = 0; i < num_elements; i++)
	{
		elements[i] = (float)(rand() % 2048 - 1024) / 512;

		sort.place(&elements[i]);
	}

	sort.iterator()->for_each([](double *element)
	{
		std::cout << *element << std::endl;
	});

	std::thread(&bootable_game::park_thread, bootable_game(update)).detach();

	per_thread<global_glfw_context>::get_or_create()->park_thread();
	per_thread<global_glfw_context>::remove_reference();


	return 0;
}
#include <iostream>
#include <thread>

#include "graphics/context/glfw_context.h"

#include "util/intrinsics/singleton.h"
#include "util/intrinsics/singleton.cpp"

#include "util/collection/tree_sort.h"
#include "util/collection/tree_sort.cpp"

#include "util/collection/linked_list.h"
#include "util/collection/linked_list.cpp"

int main()
{
	tree_sort<int> *sort = new tree_sort<int>([](int *compare, int *to) -> int
	{
		return *compare - *to;
	});

	for (int i = 0; i < 20; i++)
	{
		int *value = new int;
		*value = rand() % 256 - 128;

		std::cout << *value << " ";

		sort->place(value);
	}

	std::cout << std::endl;

	linked_list<int> *list = new linked_list<int>();

	sort->iterator()->for_each([&list](int *element) -> void
	{
		list->add(element);

		std::cout << *element << " ";
	});

	delete sort;
	std::cout << std::endl;

	list->iterator()->for_each([](int *element) -> void
	{
		std::cout << *element << " ";
	});

	delete list;
	std::cout << std::endl;

	std::thread(&bootable_game::park_thread, bootable_game()).detach();
	
	per_thread<global_glfw_context>::get_or_create()->park_thread();
	per_thread<global_glfw_context>::remove_reference();

	return 0;
}
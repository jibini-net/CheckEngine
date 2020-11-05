#pragma once

#include <memory>
#include <mutex>

#include "element_iterator.h"

template <typename E>
class linked_list_node
{
public:
	E *value = nullptr;

	linked_list_node<E> *next_node = nullptr;


	linked_list_node<E>(E *value);
};

template <typename E>
class linked_list : public iterable<E>
{
protected:
	linked_list_node<E> *first_node = new linked_list_node<E>(nullptr);
	linked_list_node<E> *last_node = first_node;

	int size = 0;

	std::mutex mtx;

public:
	~linked_list<E>();


	void add(E *value);

	int get_size();

	E *remove();


	std::unique_ptr<element_iterator<E>> iterator();
};


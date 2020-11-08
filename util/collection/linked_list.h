#pragma once

#include <memory>
#include <mutex>

#include "element_iterator.h"

template <typename E>
struct linked_list_node
{
	E *value = nullptr;

	linked_list_node<E> *next_node = nullptr;
};

template <typename E>
class linked_list : public iterable<E>
{
protected:
	linked_list_node<E> *first_node = new linked_list_node<E>;
	linked_list_node<E> *last_node = first_node;

	int size = 0;

	std::mutex mtx;

public:
	~linked_list<E>()
	{
		for (int i = 0; i <= size; i++)
		{
			auto delete_reference = this->first_node;
			this->first_node = delete_reference->next_node;

			delete delete_reference;
		}
	}


	void add(E *value)
	{
		this->mtx.lock();

		auto created_node = new linked_list_node<E> { value };

		this->last_node->next_node = created_node;
		this->last_node = created_node;

		size++;

		this->mtx.unlock();
	}

	int get_size()
	{
		return this->size;
	}

	E *remove()
	{
		this->mtx.lock();

		if (size == 0)
		{
			this->mtx.unlock();

			return nullptr;
		}

		auto delete_reference = this->first_node->next_node;
		auto returned_value = delete_reference->value;

		this->first_node->next_node = this->first_node->next_node->next_node;
		delete delete_reference;

		size--;

		this->mtx.unlock();

		return returned_value;
	}


	std::unique_ptr<element_iterator<E>> iterator()
	{
		return std::unique_ptr<element_iterator<E>>(new linked_list_iterator<E>(first_node));
	}
};

template <typename E>
class linked_list_iterator : public element_iterator<E>
{
protected:
	linked_list_node<E> *current_node;

public:
	linked_list_iterator(linked_list_node<E> *first_node)
	{
		this->current_node = first_node;
	}

	bool has_next()
	{
		return current_node->next_node != nullptr;
	}

	E *next()
	{
		current_node = current_node->next_node;

		return current_node->value;
	}
};
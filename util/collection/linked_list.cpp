#include "linked_list.h"

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

template<typename E>
linked_list<E>::~linked_list()
{
	for (int i = 0; i <= size; i++)
	{
		linked_list_node<E> *delete_reference = this->first_node;
		this->first_node = delete_reference->next_node;

		delete delete_reference;
	}
}

template<typename E>
linked_list_node<E>::linked_list_node(E *value)
{
	this->value = value;
}

template<typename E>
void linked_list<E>::add(E *value)
{
	this->mtx.lock();

	linked_list_node<E> *created_node = new linked_list_node<E>(value);

	this->last_node->next_node = created_node;
	this->last_node = created_node;

	size++;

	this->mtx.unlock();
}

template<typename E>
int linked_list<E>::get_size()
{
	return this->size;
}

template<typename E>
E *linked_list<E>::remove()
{
	this->mtx.lock();

	if (size == 0)
		return nullptr;

	linked_list_node<E> *delete_reference = this->first_node->next_node;
	E *returned_value = delete_reference->value;

	this->first_node->next_node = this->first_node->next_node->next_node;
	delete delete_reference;

	size--;

	this->mtx.unlock();

	return returned_value;
}

template<typename E>
std::unique_ptr<element_iterator<E>> linked_list<E>::iterator()
{
	return std::unique_ptr<element_iterator<E>>(new linked_list_iterator<E>(first_node));
}
#pragma once

#include <functional>
#include <memory>

#include "element_iterator.h"

/*
	EXAMPLE ITERATION
	=================

	template <typename E>
	void tree_sort<E>::print_ordered()
	{
		tree_sort_node<E> *climb = root_node->least_child;

		for (int i = 0; i < size; i ++)
		{
			std::cout << *climb->value << " ";

			if (climb->greater_than->value == nullptr)
				climb = climb->escape_node;
			else
				climb = climb->greater_than->least_child;
		}

		std::cout << std::endl;
	}

	EXAMPLE COMPARATOR LAMBDA
	=========================

	tree_sort<int> sort([](int *compare, int *to) -> int
	{
		return *compare - *to;
	});
*/

// Internal linked element of the tree-sort algorithm; each element has two
// links to child nodes and one link to escape to the most recent parent
// whose value is greater than this node's; also maintains each node's lowest-
// value child to make iteration more efficient
template <typename E>
struct tree_sort_node
{
	// The value held within this node; child nodes will be arranged
	// according to this value
	E *value = nullptr;

	// The most recent parent which this node is less than
	tree_sort_node<E> *escape_node = nullptr;
	// Quicker to store this pointer than to always iterate left; defaults to
	// itself in case there are no smaller children
	tree_sort_node<E> *least_child = this;

	// A child node where its value and all its children's values are less
	// than this node's value
	tree_sort_node<E> *less_than = nullptr;
	// A child node where its value and all its children's values are greater
	// than or equal to this node's value
	tree_sort_node<E> *greater_than = nullptr;
};

// Simple tree-sort sort algorithm implementation for any generic linkable
// type as expressed in a rearrangable linked-list
template <typename E>
class tree_sort : public iterable<E>
{
protected:
	// Root node of the sorting tree; defaults to a null-value node
	tree_sort_node<E> *root_node = new tree_sort_node<E>;
	// Stored comparator lambda to maintain consistent sorting throughout
	std::function<int(E *compare, E *to)> comparator;

	// Track the number of elements in the tree for iteration
	int size = 0;

public:
	// Stores the provided comparator for sorting
	tree_sort(std::function<int(E *compare, E *to)> comparator) : iterable<E>()
	{
		this->comparator = comparator;
	}

	// Deletes all dynamically allocated nodes from sorting operations
	~tree_sort()
	{
		// Start deleting at the first node
		tree_sort_node<E> *climb = root_node->least_child;
		// Delete the first node's left child
		delete climb->less_than;

		for (int i = 0; i < size; i++)
		{
			// Maintain a reference to delete later
			tree_sort_node<E> *delete_reference = climb;

			if (climb->greater_than->value == nullptr)
			{
				// Delete the right child before moving the node
				delete climb->greater_than;

				climb = climb->escape_node;
			} else
			{
				climb = climb->greater_than->least_child;

				// Delete the left child after moving the node
				delete climb->less_than;
			}

			// Delete each node reference saved from earlier
			delete delete_reference;
		}
	}

	// Non-recursive stable tree sorting algorithm based on similar concepts
	// to the quick-sort algorithm
	void place(E *value)
	{
		// Track the head node for each branch
		tree_sort_node<E> *last_branch_right = root_node;
		// Track the escape node for each value
		tree_sort_node<E> *escape_node = root_node;

		// Track if each node was branched off to the left
		bool is_lesser = false;

		// Track the node as the tree is traversed
		tree_sort_node<E> *climb = root_node;

		// Climb down the tree and jog left and right as appropriate until
		// a null-value node is reached
		while (climb->value != nullptr)
		{
			int comparison = comparator(value, climb->value);

			if (comparison < 0)
			{
				escape_node = climb;
				is_lesser = true;

				// Move the pointer after tracking escape node
				climb = climb->less_than;
			} else
			{
				// Move the pointer before tracking the rightwards branch
				climb = climb->greater_than;

				last_branch_right = climb;
				is_lesser = false;
			}
		}

		// Place the given value in the null-value node
		climb->value = value;

		// Branch out the node's child nodes to null-values
		climb->less_than = new tree_sort_node<E>;
		climb->greater_than = new tree_sort_node<E>;

		// Set the escape node of the current node
		climb->escape_node = escape_node;
		// Set the least child of the branch head if branching left
		if (is_lesser)
			last_branch_right->least_child = climb;

		// Track the number of elements in the tree for iteration
		size++;
	}

	// The collection's unique implementation of the iterator
	std::unique_ptr<element_iterator<E>> iterator()
	{
		int size = this->size;

		return std::unique_ptr<element_iterator<E>>(
			new tree_sort_iterator<E>(this->root_node->least_child, [size](tree_sort_node<E> *&current_node, int &i) -> bool
		{
			return i++ < size;
		}));
	}
};

template <typename E>
class tree_sort_iterator : public element_iterator<E>
{
protected:
	tree_sort_node<E> *current_node;
	std::function<bool(tree_sort_node<E> *&current_node, int &i)> predicate;

	int i = 0;

public:
	tree_sort_iterator<E>(tree_sort_node<E> *start_node, std::function<bool(tree_sort_node<E> *&current_node,
		int &i)> predicate) : element_iterator<E>()
	{
		this->current_node = start_node;
		this->predicate = predicate;
	}

	virtual bool has_next()
	{
		return this->predicate(this->current_node, this->i);
	}

	virtual E *next()
	{
		E *result = current_node->value;

		if (current_node->greater_than->value == nullptr)
			current_node = current_node->escape_node;
		else
			current_node = current_node->greater_than->least_child;

		return result;
	}
};
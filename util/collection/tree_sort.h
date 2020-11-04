#pragma once

#include <functional>
#include <memory>

#include "element_iterator.h"

// Internal linked element of the tree-sort algorithm; each element has two
// links to child nodes and one link to escape to the most recent parent
// whose value is greater than this node's; also maintains each node's lowest-
// value child to make iteration more efficient
template <typename E>
class tree_sort_node
{
public:
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
class tree_sort
{
protected:
	// Root node of the sorting tree; defaults to a null-value node
	tree_sort_node<E> *root_node = new tree_sort_node<E>();
	// Stored comparator lambda to maintain consistent sorting throughout
	std::function<int(E *compare, E *to)> comparator;

	// Track the number of elements in the tree for iteration
	int size = 0;

public:
	// Stores the provided comparator for sorting
	tree_sort(std::function<int(E *compare, E *to)> comparator);
	// Deletes all dynamically allocated nodes from sorting operations
	~tree_sort();

	// Non-recursive stable tree sorting algorithm based on similar concepts
	// to the quick-sort algorithm
	void place(E *value);

	std::unique_ptr<element_iterator<E>> iterator();
};
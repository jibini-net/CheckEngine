#include "tree_sort.h"

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

// Convenience method for the binary search functionality
template <typename E>
tree_sort_node<E> *find_node(E *value, std::function<int(E *compare, E *to)> comparator,
	tree_sort_node<E> *&start_node,  tree_sort_node<E> *&escape_node,
	tree_sort_node<E> *&last_branch_right, bool &is_lesser)
{
	// Track the node as the tree is traversed
	tree_sort_node<E> *climb = start_node;

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

	return climb;
}

template <typename E>
tree_sort<E>::tree_sort(std::function<int(E *compare, E *to)> comparator)
{
	this->comparator = comparator;
}

template <typename E>
tree_sort<E>::~tree_sort()
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

template <typename E>
void tree_sort<E>::place(E *value)
{
	// Track the head node for each branch
	tree_sort_node<E> *last_branch_right = root_node;
	// Track the escape node for each value
	tree_sort_node<E> *escape_node = root_node;

	// Track if each node was branched off to the left
	bool is_lesser = false;

	tree_sort_node<E> *null_value_node = find_node(value, this->comparator, root_node,
		escape_node, last_branch_right, is_lesser);

	// Place the given value in the null-value node
	null_value_node->value = value;

	// Branch out the node's child nodes to null-values
	null_value_node->less_than = new tree_sort_node<E>();
	null_value_node->greater_than = new tree_sort_node<E>();

	// Set the escape node of the current node
	null_value_node->escape_node = escape_node;
	// Set the least child of the branch head if branching left
	if (is_lesser)
		last_branch_right->least_child = null_value_node;

	// Track the number of elements in the tree for iteration
	size++;
}

template <typename E>
std::unique_ptr<element_iterator<E>> tree_sort<E>::iterator()
{
	int size = this->size;

	return std::unique_ptr<element_iterator<E>>(
		new tree_sort_iterator<E>(this->root_node->least_child, [size](tree_sort_node<E> *&current_node, int &i) -> bool
	{
		return i++ < size;
	}));
}

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
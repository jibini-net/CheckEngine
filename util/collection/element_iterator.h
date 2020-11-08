#pragma once

#include <functional>

// An object-oriented iterator system which relies on has-next
// and next clauses
template <typename E>
class element_iterator
{
public:
	// If this iterator can provide a next value; indicates that the
	// next call to next() should be successful
	virtual bool has_next() = 0;

	// Next value of the iterator; may throw an exception if
	// no element exists or is found
	virtual E *next() = 0;

	// Iterates through every element and invokes the given action
	inline void for_each(std::function<void(E *element)> action)
	{
		while (this->has_next())
		{
			auto next = this->next();

			action(next);
		}
	}
};

// Any collection which provides an iterator
template <typename E>
class iterable
{
public:
	// The collection's unique implementation of the iterator
	virtual std::unique_ptr<element_iterator<E>> iterator() = 0;
};
#pragma once

#include <memory>

// Supervises the creation of singleton objects on a per-thread basis
template <typename T>
class per_thread
{
protected:
	// Static thread-local instance of the object type
	static thread_local std::shared_ptr<T> instance;

public:
	// Gets the current thread's instance or creates one if it is missing
	static std::shared_ptr<T> get_or_create();

	// Removes the singleton reference (may result in deletion)
	static void remove_reference();
};
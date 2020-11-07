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
	static std::shared_ptr<T> get_or_create()
	{
		// Check if there is an instance yet; create if not
		if (per_thread<T>::instance == nullptr)
			instance = std::shared_ptr<T>(new T());

		return instance;
	}


	// Removes the singleton reference (may result in deletion)
	static void remove_reference()
	{
		instance.reset();

		instance = nullptr;
	}

	static void set(std::shared_ptr<T> pointer)
	{
		instance = pointer;
	}
};

template <typename T>
thread_local std::shared_ptr<T> per_thread<T>::instance = nullptr;
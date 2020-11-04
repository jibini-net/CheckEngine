#include "singleton.h"

// Static thread-local instance of the object type
template <typename T>
thread_local std::shared_ptr<T> per_thread<T>::instance = nullptr;

template <typename T>
std::shared_ptr<T> per_thread<T>::get_or_create()
{
	// Check if there is an instance yet; create if not
	if (per_thread<T>::instance == nullptr)
		instance = std::shared_ptr<T>(new T());

	return instance;
}

template <typename T>
void per_thread<T>::remove_reference()
{
	instance.reset();

	instance = nullptr;
}
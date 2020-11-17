#pragma once

#include <functional>

#include "util/diagnostic/logging/logger.h"

#include "graphics/context/glfw_context.h"

class buffer_object
{
private:
	// Local implementation logger instance
	logger _log { "Buffer Object" };

protected:
	GLuint pointer;

	GLenum buffer_type;

public:
	buffer_object(GLenum buffer_type);


	void bind();

	void unbind();


	void bind_base(int index);


	void *map(bool read, bool write);

	template <typename T>
	T *map_typed(bool read, bool write)
	{
		return static_cast<T *>(map(read, write));
	}

	void unmap();

	template <typename T>
	void map_scoped(bool read, bool write, std::function<void(T *mapped)> action)
	{
		T *mapped = this->map_typed<T>(read, write);

		action(mapped);

		this->unmap();
	}


	void put(void *data, GLsizeiptr size, GLenum usage);
};
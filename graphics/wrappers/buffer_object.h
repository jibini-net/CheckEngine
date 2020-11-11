#pragma once

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


	void put(void *data, GLsizeiptr size, GLenum usage);
};
#include "buffer_object.h"

buffer_object::buffer_object(GLenum buffer_type)
{
	glGenBuffers(1, &pointer);

	this->buffer_type = buffer_type;
};

void buffer_object::bind()
{
	glBindBuffer(buffer_type, pointer);
}

void buffer_object::unbind()
{
	glBindBuffer(buffer_type, 0);
}

void buffer_object::bind_base(int index)
{
	glBindBufferBase(buffer_type, index, pointer);
}

void *buffer_object::map(bool read, bool write)
{
	if (!read && !write)
	{
		_log.warn("Attempting to map a buffer with both read and write access disabled;"
			+ (std::string)"no bind was performed");

		return nullptr;
	}

	GLenum access_type;
	if (read && write)
		access_type = GL_READ_WRITE;
	else if (read)
		access_type = GL_READ_ONLY;
	else if (write)
		access_type = GL_WRITE_ONLY;

	this->bind();

	return glMapBuffer(buffer_type, access_type);
}

void buffer_object::unmap()
{
	this->bind();

	glUnmapBuffer(buffer_type);
}

void buffer_object::put(void *data, GLsizeiptr size, GLenum usage)
{
	this->bind();

	glBufferData(buffer_type, size, data, usage);
}
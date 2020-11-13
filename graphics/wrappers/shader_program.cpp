#include "shader_program.h"

shader_program::shader_program()
{
	pointer = glCreateProgram();
}

void shader_program::attach_shader(GLenum type, std::string source, std::string kernel_name)
{
	_log.info("Loading '" + kernel_name + "' shader . . .");

	GLuint shader = glCreateShader(type);

	const char *glsl = source.c_str();
	const int glsl_length = (int)source.length();

	glShaderSource(shader, 1, &glsl, &glsl_length);
	glCompileShader(shader);

	_log.debug("Checking shader for compilation or syntax errors . . .");

	GLint max_length;
	glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &max_length);

	if (max_length > 0)
	{
		std::vector<GLchar> error_log(max_length);
		glGetShaderInfoLog(shader, max_length, &max_length, &error_log[0]);

		_log.error("SHADER COMPILE ERROR:\n" + (std::string)error_log.data() + "\n");
	} else
		_log.debug("Shader compiled with no error messages");

	_log.debug("Shader is compiled; attaching to shader program and deleting . . .");

	glAttachShader(pointer, shader);
	glDeleteShader(shader);
}

void shader_program::link()
{
	glLinkProgram(pointer);
}

void shader_program::use()
{
	glUseProgram(pointer);
}
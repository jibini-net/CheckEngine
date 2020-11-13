#pragma once

#include <string>

#include "util/diagnostic/logging/logger.h"

#include "graphics/context/glfw_context.h"

class shader_program
{
private:
	// Local implementation logger instance
	logger _log { "Shader Program" };

protected:
	GLuint pointer;

public:
	shader_program();

	
	void attach_shader(GLenum type, std::string source, std::string kernel_name);


	void link();

	void use();
};
#include "graphics/context/glfw_context.h"

#include "util/intrinsics/singleton.h"
#include "util/intrinsics/bootable_game.h"

#include "util/diagnostic/logging/logger.h"

// Local implementation logger instance
logger _root_log("Check Engine");

struct body
{
	float position[2];
};

struct positional_data_t
{
	int num_local_bodies = 2;
	float __padding[1];

	body local_bodies[2]
	{
		body { 0.0f, 0.0f },
		body { 0.0f, 0.0f }
	};

} positional_data;

const std::string VERTEX_GLSL = 
"#version 430\n \
\
layout (location = 0) in vec3 vertex; \
\
struct body \
{ \
	vec2 position; \
}; \
\
layout (std430, binding = 0) buffer positional_data \
{ \
	int num_local_bodies; \
	body local_bodies[]; \
}; \
\
void main() \
{ \
	local_bodies[0].position.x += 00.01f; \
	local_bodies[0].position.y += 00.10f; \
	local_bodies[1].position.x += 01.00f; \
	local_bodies[1].position.y += 10.00f; \
\
	gl_PointSize = 80.0f; \
	gl_Position = vec4(vertex, 1.0f); \
}";

const std::string FRAGMENT_GLSL = 
"#version 430\n \
\
out vec4 color; \
\
struct body \
{ \
	vec2 position; \
}; \
\
layout (std430, binding = 0) buffer positional_data \
{ \
	int num_local_bodies; \
	body local_bodies[]; \
}; \
\
void main() \
{ \
	color = vec4(local_bodies[0].position, local_bodies[1].position); \
}";

GLuint vertex_shader;
GLuint fragment_shader;

GLuint shader_program;

GLuint shader_ssbo, vbo;

void start()
{
	_root_log.info("\033[1;33m===============================================================");
	_root_log.info("Initializing game assets prior to runtime . . .");
	_root_log.info("\033[1;33m===============================================================");

	vertex_shader = glCreateShader(GL_VERTEX_SHADER);
	const char *vertex_glsl = VERTEX_GLSL.c_str();
	const int vertex_glsl_length = VERTEX_GLSL.length();
	glShaderSource(vertex_shader, 1, &vertex_glsl, &vertex_glsl_length);
	glCompileShader(vertex_shader);
	
	fragment_shader = glCreateShader(GL_FRAGMENT_SHADER);
	const char *fragment_glsl = FRAGMENT_GLSL.c_str();
	const int fragment_glsl_length = FRAGMENT_GLSL.length();
	glShaderSource(fragment_shader, 1, &fragment_glsl, &fragment_glsl_length);
	glCompileShader(fragment_shader);

	GLint max_length;

	glGetShaderiv(vertex_shader, GL_INFO_LOG_LENGTH, &max_length);

	if (max_length > 0)
	{
		std::vector<GLchar> error_log(max_length);
		glGetShaderInfoLog(vertex_shader, max_length, &max_length, &error_log[0]);

		_root_log.error("VERTEX SHADER COMPILE ERROR:\n" + (std::string)error_log.data() + "\n");
	}

	glGetShaderiv(fragment_shader, GL_INFO_LOG_LENGTH, &max_length);

	if (max_length > 0)
	{
		std::vector<GLchar> error_log(max_length);
		glGetShaderInfoLog(fragment_shader, max_length, &max_length, &error_log[0]);

		_root_log.error("FRAGMENT SHADER COMPILE ERROR:\n" + (std::string)error_log.data() + "\n");
	}

	shader_program = glCreateProgram();
	glAttachShader(shader_program, vertex_shader);
	glAttachShader(shader_program, fragment_shader);
	glLinkProgram(shader_program);

	glDeleteShader(vertex_shader);
	glDeleteShader(fragment_shader);

	glUseProgram(shader_program);


	glGenBuffers(1, &shader_ssbo);
	glBindBuffer(GL_SHADER_STORAGE_BUFFER, shader_ssbo);
	glBufferData(GL_SHADER_STORAGE_BUFFER, sizeof(positional_data_t), &positional_data, GL_DYNAMIC_COPY);

	//glBindBuffer(GL_SHADER_STORAGE_BUFFER, shader_ssbo);
	GLvoid *p = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_WRITE_ONLY);
	memcpy(p, &positional_data, sizeof(positional_data_t));
	glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);

	GLuint block_index = glGetProgramResourceIndex(shader_program, GL_SHADER_STORAGE_BLOCK, "positional_data");
	glShaderStorageBlockBinding(shader_program, block_index, 0);
	glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, shader_ssbo);

	glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
	

	glGenBuffers(1, &vbo);
	glBindBuffer(GL_ARRAY_BUFFER, vbo);

	glEnable(GL_PROGRAM_POINT_SIZE);

	float data[] =  
	{
		0.0f, 0.0f, 0.0f
	};

	glBufferData(GL_ARRAY_BUFFER, sizeof(data), &data[0], GL_STATIC_DRAW);

	glBindBuffer(GL_ARRAY_BUFFER, 0);
	
	GLuint vao = 0;
	glGenVertexArrays(1, &vao);
	glBindVertexArray(vao);
}

void update()
{
	auto window = per_thread<glfw_window>::get_or_create();
	auto window_ratio = (float)window->get_width() / window->get_height();

	glViewport(0, 0, window->get_width(), window->get_height());

	glClear(GL_COLOR_BUFFER_BIT);


	glEnableVertexAttribArray(0);

	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, NULL);

	glDrawArrays(GL_POINTS, 0, 1);

	glDisableVertexAttribArray(0);


	glBindBuffer(GL_ARRAY_BUFFER, 0);

	int error = glGetError();
	if (error != GL_NO_ERROR)
		_root_log.error("An OpenGL error has occurred; context error flag is set to " + std::to_string(error));


	glBindBuffer(GL_SHADER_STORAGE_BUFFER, shader_ssbo);
	GLvoid *p = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_READ_ONLY);
	memcpy(&positional_data, p, sizeof(positional_data_t));
	glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);

	glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);


	std::cout << positional_data.local_bodies[0].position[0] << ", ";
	std::cout << positional_data.local_bodies[0].position[1] << " ";
	std::cout << positional_data.local_bodies[1].position[0] << ", ";
	std::cout << positional_data.local_bodies[1].position[1] << std::endl;
}

int main()
{
	_root_log.info("\033[1;33m===============================================================");
	_root_log.info("        Welcome to \033[1;31mCheck Engine\033[0m created by \033[1;31mZach Goethel");
	_root_log.info("\033[1;33m===============================================================");

	bootable_game(start, update).boot_thread();

	per_thread<global_glfw_context>::get_or_create()->park_thread();
	per_thread<global_glfw_context>::remove_reference();


	return 0;
}
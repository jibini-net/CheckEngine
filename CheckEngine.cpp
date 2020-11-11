#include <vector>

#include "util/diagnostic/logging/logger.h"

#include "graphics/context/glfw_context.h"

#include "graphics/compute/accel_compute.h"

#include "graphics/wrappers/shader_program.h"
#include "graphics/wrappers/buffer_object.h"

#include "util/intrinsics/singleton.h"
#include "util/intrinsics/bootable_game.h"

// Local implementation logger instance
logger _root_log("Check Engine");

struct body
{
	float position[2];
};

struct positional_data
{
	int num_local_bodies;
	float __padding[1];

	// Needs special memcpy ops
	//std::vector<body> local_bodies;
	body local_bodies[];
};

const std::string VERTEX_GLSL = 
"#version 430 core\n \
\
layout (location = 0) in int index; \
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
	local_bodies[0].position.x += 0.01f; \
\
	gl_PointSize = 16.0f; \
	gl_Position = vec4(index, 0.0f, 0.0f, 1.0f); \
}";

const std::string FRAGMENT_GLSL = 
"#version 430 core\n \
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

std::shared_ptr<buffer_object> shader_ssbo, vbo;

void start()
{
	_root_log.info("\033[1;33m===============================================================");
	_root_log.info("Initializing game assets prior to runtime . . .");

	_root_log.info("Loading point-based compute vertex shader (parallelized workhorse kernel) . . .");
	vertex_shader = glCreateShader(GL_VERTEX_SHADER);
	const char *vertex_glsl = VERTEX_GLSL.c_str();
	const int vertex_glsl_length = (int)VERTEX_GLSL.length();
	glShaderSource(vertex_shader, 1, &vertex_glsl, &vertex_glsl_length);
	glCompileShader(vertex_shader);

	_root_log.info("Loading point-based compute fragment shader (graphical debug/diagnostic output) . . .");
	fragment_shader = glCreateShader(GL_FRAGMENT_SHADER);
	const char *fragment_glsl = FRAGMENT_GLSL.c_str();
	const int fragment_glsl_length = (int)FRAGMENT_GLSL.length();
	glShaderSource(fragment_shader, 1, &fragment_glsl, &fragment_glsl_length);
	glCompileShader(fragment_shader);

	GLint max_length;
	_root_log.debug("Checking compiled shaders for compilation or syntax errors . . .");

	glGetShaderiv(vertex_shader, GL_INFO_LOG_LENGTH, &max_length);

	if (max_length > 0)
	{
		std::vector<GLchar> error_log(max_length);
		glGetShaderInfoLog(vertex_shader, max_length, &max_length, &error_log[0]);

		_root_log.error("VERTEX SHADER COMPILE ERROR:\n" + (std::string)error_log.data() + "\n");
	} else
		_root_log.debug("Vertex shader compiled with no error messages");

	glGetShaderiv(fragment_shader, GL_INFO_LOG_LENGTH, &max_length);

	if (max_length > 0)
	{
		std::vector<GLchar> error_log(max_length);
		glGetShaderInfoLog(fragment_shader, max_length, &max_length, &error_log[0]);

		_root_log.error("FRAGMENT SHADER COMPILE ERROR:\n" + (std::string)error_log.data() + "\n");
	} else
		_root_log.debug("Fragment shader compiled with no error messages");


	_root_log.debug("Shaders are compiled; attaching to shader program and deleting . . .");
	shader_program = glCreateProgram();

	glAttachShader(shader_program, vertex_shader);
	glAttachShader(shader_program, fragment_shader);

	glLinkProgram(shader_program);

	glDeleteShader(vertex_shader);
	glDeleteShader(fragment_shader);

	_root_log.info("Successfully linked point-based accelerated compute program");
	glUseProgram(shader_program);


	positional_data ssbo_data
	{
		2, { 0.0 },

		{
			{ 1.0f, 0.0f },
			{ 1.0f, 1.0f }
		}
	};

	int data_size = sizeof(positional_data) + ssbo_data.num_local_bodies * sizeof(body);
	shader_ssbo.reset(new buffer_object(GL_SHADER_STORAGE_BUFFER));
	shader_ssbo->put(&ssbo_data, data_size, GL_DYNAMIC_COPY);

	shader_ssbo->bind_base(0);

	shader_ssbo->unbind();
	

	glEnable(GL_PROGRAM_POINT_SIZE);
	glEnable(GL_DEPTH_TEST);

	int data[] =  
	{
		0
	};

	vbo.reset(new buffer_object(GL_ARRAY_BUFFER));
	vbo->put(&data[0], sizeof(data), GL_STATIC_DRAW);

	vbo->unbind();
	
	GLuint vertex_array;
	glGenVertexArrays(1, &vertex_array);
	glBindVertexArray(vertex_array);

	_root_log.info("\033[1;33m===============================================================");
}

void update()
{
	auto window = per_thread<glfw_window>::get_or_create();
	auto window_ratio = (float)window->get_width() / window->get_height();

	glViewport(0, 0, window->get_width(), window->get_height());

	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


	glEnableVertexAttribArray(0);

	vbo->bind();
	glVertexAttribPointer(0, 1, GL_INT, GL_FALSE, 0, NULL);

	glDrawArrays(GL_POINTS, 0, 1);

	glDisableVertexAttribArray(0);

	vbo->unbind();


	auto mapped = shader_ssbo->map_typed<positional_data>(true, false);

	// Mapped data is in scope; complete CPU-side operations

	shader_ssbo->unmap();
	shader_ssbo->unbind();
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
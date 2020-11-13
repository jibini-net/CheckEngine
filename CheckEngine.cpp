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

std::shared_ptr<shader_program> shader;
std::shared_ptr<buffer_object> shader_ssbo, vbo;

delta_timer frame_rate_time;
long frame_count = 0;

void start()
{
	_root_log.info("\033[1;33m===============================================================");
	_root_log.info("Initializing game assets prior to runtime . . .");

	shader.reset(new shader_program());

	shader->attach_shader(GL_VERTEX_SHADER, VERTEX_GLSL, "COMPUTE_VERT (workhorse)");
	shader->attach_shader(GL_FRAGMENT_SHADER, FRAGMENT_GLSL, "COMPUTE_FRAG (debug draw)");

	shader->link();
	shader->use();

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


	frame_rate_time.update();
	frame_count++;

	if (frame_rate_time.delta_time() >= 2.0)
	{
		double frame_rate = (double)frame_count / frame_rate_time.delta_time();

		_root_log.debug(std::to_string(frame_rate) + " frames per second");

		frame_rate_time.reset();
		frame_count = 0;
	}
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
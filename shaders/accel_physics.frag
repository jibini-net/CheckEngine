#version 430 core

out vec4 color;

struct vertex
{
	vec3 position;
	vec4 color;
	vec2 tex_coord;

	int body_index;
};

struct body
{
	vec2 position;
	vec2 velocity;

	float rot;
	float rot_velocity;
};

layout (std430, binding = 0) buffer phys_body_data
{
	body local_bodies[];
};

layout (std430, binding = 1) buffer vertex_data
{
	vertex vertices[];
};

void main()
{
	color = vec4(1.0f);
}
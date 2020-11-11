#version 430 core

layout (location = 0) in uint index;

struct body
{
	vec2 position;
	vec2 velocity;

	float rot;
	float rot_velocity;
};

layout (std430, binding = 0) buffer body_ssbo_data
{
	int num_local_bodies;
	body local_bodies[];
};

void main()
{
	gl_PointSize = 16.0f;
	gl_Position = vec4(index, 0.0f, 0.0f, 1.0f);
}
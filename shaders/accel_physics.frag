#version 430 core

out vec4 color;

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
	color = vec4(local_bodies[0].position, local_bodies[1].position);
}
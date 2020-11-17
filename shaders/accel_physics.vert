#version 430 core

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
	vertex current_vertex = vertices[gl_VertexID];
	body current_body = local_bodies[current_vertex.body_index];

	gl_PointSize = 16.0f;
	gl_Position = vec4(current_vertex.position, 0.0f, 1.0f);
}
#version 300 es

precision highp float;
precision highp int;
precision lowp sampler2D;
precision lowp samplerCube;

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec4 vertex_color;
layout(location = 2) in vec2 vertex_tex_coord;

out vec2 tex_coord;
out vec4 color;

uniform vec2 tex_offset;
uniform vec2 tex_delta;
uniform mat4 uniform_matrix;

void main()
{
    gl_Position = uniform_matrix * vec4(vertex, 1.0);

    tex_coord = vertex_tex_coord * tex_delta + tex_offset;
    color = vertex_color;
}
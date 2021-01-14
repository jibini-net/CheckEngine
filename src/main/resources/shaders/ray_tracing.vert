#version 300 es

precision highp float;
precision highp int;
precision lowp sampler2D;
precision lowp samplerCube;

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec4 vertex_color;
layout(location = 2) in vec2 vertex_tex_coord;

out float x_interp;
out float y_interp;

uniform mat4 uniform_matrix;

uniform int output_size;

void main()
{
    x_interp = float(int(
          gl_VertexID == 1
       || gl_VertexID == 4
       || gl_VertexID == 5
    ) * output_size) - 1.1;

    y_interp = float(int(
          gl_VertexID == 1
       || gl_VertexID == 2
       || gl_VertexID == 5
    ) * output_size) - 1.1;

    gl_Position = uniform_matrix * vec4(vertex, 1.0);
}
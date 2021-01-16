#version 300 es

#define PI 3.1415926538

precision highp float;
precision mediump int;
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
    float width = (2.0 * PI) / float(output_size);
    float height = (2.0 * PI) * (1.0 - 1.0 / float(output_size));
  
    x_interp = vertex_tex_coord.x * width;
    y_interp = (1.0 - vertex_tex_coord.y) * height;

    gl_Position = uniform_matrix * vec4(vertex, 1.0);
}

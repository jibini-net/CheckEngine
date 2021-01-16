#version 300 es

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

layout(location = 0) in vec3 vertex;
layout(location = 1) in vec4 vertex_color;
layout(location = 2) in vec2 vertex_tex_coord;

out vec2 frag_position;

uniform mat4 uniform_matrix;
uniform mat4 frag_matrix;

void main()
{
    gl_Position = uniform_matrix * vec4(vertex, 1.0);
    
    vec2 tex_coord = vec2(vertex_tex_coord.x, 1.0 - vertex_tex_coord.y);
    frag_position = (frag_matrix * vec4(tex_coord * 2.0 - vec2(1.0), 0.0, 1.0)).xy;
}

#version 300 es

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in vec2 tex_coord;
in vec4 color;

layout(location = 0) out vec4 frag_color;

uniform sampler2D tex;
uniform vec4 color_mult;

void main()
{
    frag_color = texture(tex, tex_coord) * color * color_mult;
}
#version 300 es

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in vec2 tex_coord;
in vec4 color;

layout(location = 0) out vec4 frag_color;
layout(location = 1) out vec4 light_mask;

uniform sampler2D tex;
uniform bool light_blocking;
uniform vec4 color_mult;

void main()
{
    frag_color = texture(tex, tex_coord) * color * color_mult;
    light_mask = vec4(int(light_blocking && frag_color.a > 0.2));
}
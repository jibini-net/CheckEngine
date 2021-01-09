#version 300 es

precision highp float;
precision highp int;
precision lowp sampler2D;
precision lowp samplerCube;

precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

uniform sampler2D tex;

in vec2 tex_coord;
in vec4 color;

layout(location = 0) out vec4 frag_color;

void main()
{
    frag_color = texture(tex, tex_coord) * color;
}
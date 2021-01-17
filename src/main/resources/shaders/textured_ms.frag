#version 310 es

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in vec2 tex_coord;
in vec4 color;

layout(location = 0) out vec4 frag_color;

uniform sampler2D tex;
uniform vec2 tex_size;

uniform vec4 color_mult;

void main()
{
    float offset_x = 0.5 / tex_size.x;
    float offset_y = 0.5 / tex_size.y;

    vec4 texel_0 = texture(tex, tex_coord + vec2(-offset_x, -offset_y));
    vec4 texel_1 = texture(tex, tex_coord + vec2(offset_x, -offset_y));
    vec4 texel_2 = texture(tex, tex_coord + vec2(offset_x, offset_y));
    vec4 texel_3 = texture(tex, tex_coord + vec2(-offset_x, offset_y));

    frag_color = (texel_0 + texel_1 + texel_2 + texel_3) / 4.0 * color * color_mult;
}
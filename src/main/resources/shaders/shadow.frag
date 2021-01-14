#version 300 es

#define PI 3.1415926538
#define MAX_RADIUS 1.4

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in vec2 tex_coord;
in vec4 color;

layout(location = 0) out vec4 frag_color;

uniform vec3 light_color;
uniform vec2 light_position;

uniform sampler2D tex;
uniform vec2 world_size;
uniform int input_size;

uniform mat4 frag_matrix;

void main()
{
    vec2 frag_position = (frag_matrix * vec4(tex_coord * 2.0 - vec2(1.0), 0.0, 1.0)).xy;
    vec2 to_fragment = frag_position - light_position * 0.2;
    float angle = atan(to_fragment.y, to_fragment.x);
    if (angle < 0.0)
        angle += 2.0 * PI;

    int index = int(ceil((angle / (2.0 * PI)) * float(input_size * input_size)));
    int x = index % input_size;
    int y = index / input_size;

    vec2 ref_coord = vec2(
        (float(x) + 0.5) / float(input_size),
        (float(y) + 0.5) / float(input_size)
    );

    vec2 ref_vector = texture(tex, ref_coord).rg;
    vec2 restored = (ref_vector - vec2(0.5)) * 2.0 * MAX_RADIUS * world_size.y;

    float mask = float(int(length(restored) > length(to_fragment) - 0.015));
    float len = length(to_fragment);

    frag_color = vec4(light_color * 0.5 / len * mask * (-1.0 * len + MAX_RADIUS) +
            light_color * (-1.2 * len + MAX_RADIUS * 0.6), 1.0);
}
#version 300 es

#define PI 3.1415926538
#define MAX_RADIUS 128
#define PIXELS_PER_TILE 16.0

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in vec2 frag_position;

layout(location = 0) out vec4 frag_color;

uniform vec3 light_color;
uniform vec2 light_position;

uniform sampler2D tex;
uniform int input_size;

uniform mat4 frag_matrix;
uniform float ray_scale;

void main()
{
    vec2 to_fragment = frag_position - light_position * 0.2;
    float len = length(to_fragment);
    if (len > 1.4)
        discard;
    float angle = atan(to_fragment.y, to_fragment.x);
    if (angle < 0.0)
        angle += 2.0 * PI;

    int index = int(floor((angle / (2.0 * PI)) * float(input_size * input_size)));
    int x = index % input_size;
    int y = index / input_size;

    vec2 ref_coord = vec2(
        float(x) / float(input_size),
        float(y) / float(input_size)
    );

    vec2 ref_vector = texture(tex, ref_coord).rg * ray_scale;
    vec2 restored = ref_vector * float(MAX_RADIUS) / PIXELS_PER_TILE * 0.2;

    float mask = float(int(length(restored) > length(to_fragment) - 0.022));

    frag_color = vec4(light_color * 0.5 / len * mask * (-1.0 * len + 1.4) +
            light_color * (-1.2 * len + 1.4 * 0.6), 1.0);
}

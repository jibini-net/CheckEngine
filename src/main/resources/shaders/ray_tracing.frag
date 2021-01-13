#version 300 es

#define PI 3.1415926538
#define MAX_RADIUS 0.5
#define PIXELS_PER_TILE 64.0

precision highp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in float x_interp;
in float y_interp;

layout(location = 0) out vec4 frag_color;

uniform int output_size;
uniform int input_width;
uniform int input_height;

uniform sampler2D light_mask;

uniform vec2 light_position;

void main()
{
    int x = int(round(x_interp));
    int y = int(round(y_interp));

    int id = y * output_size + x;

    float angle = 2.0 * PI * float(id) / float(output_size * output_size);
    float pixel_width = 1.0 / float(input_width);
    float pixel_height = 1.0 / float(input_height);
    vec2 direction = vec2(cos(angle), sin(angle));

    float step_size = min(pixel_width, pixel_height);
    vec2 step = direction * step_size;

    vec2 coord = light_position * vec2(PIXELS_PER_TILE / float(input_width), PIXELS_PER_TILE / float(input_height));
    float distance = 0.0;

    for (distance; distance <= MAX_RADIUS; distance += step_size)
    {
        coord += step;

        if (texture(light_mask, coord).b > 0.2)
            break;
    }

    frag_color = vec4((direction * distance) / MAX_RADIUS, 0.0, 1.0);
}
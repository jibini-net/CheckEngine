#version 300 es

#define PI 3.1415926538
#define MAX_RADIUS 128
#define PIXELS_PER_TILE 16.0

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
    //float angle = y_interp + x_interp;

    int x = int(floor(x_interp));
    int y = int(floor(y_interp));

    int id = y * output_size + x;

    float angle = 2.0 * PI * (float(id) / float(output_size * output_size));
    angle *= 1.003;

    vec2 direction = vec2(cos(angle), sin(angle));
    vec2 coord = light_position * PIXELS_PER_TILE;
    
    int distance = 0;

    for (distance; distance <= MAX_RADIUS; distance++)
    {
        coord += direction;
        
        ivec2 coord_cast = ivec2(int(coord.x), int(coord.y));
        vec4 texel_value = texelFetch(light_mask, coord_cast, 0);
        
        if (texel_value.r > 0.2)
            break;
    }

    frag_color = vec4(vec3(float(distance) / float(MAX_RADIUS)), 1.0);
}

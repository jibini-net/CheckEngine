#version 300 es

precision lowp float;
precision mediump int;
precision lowp sampler2D;
precision lowp samplerCube;

in vec2 tex_coord;
in vec4 color;

layout(location = 0) out vec4 frag_color;

uniform sampler2D tex;

uniform vec2 light;
uniform vec3 light_color;

void main()
{
    vec2 coord = tex_coord;
    vec2 towards_light = normalize(light - coord) * 0.01;

    int mask = 1;

    for (coord; abs(coord.x - light.x) > 0.01 || abs(coord.y - light.y) > 0.01; coord += towards_light)
    {
        mask -= int(texture(tex, coord).r);
    }

    frag_color = vec4(pow(0.1, length(tex_coord - light)) * light_color * vec3(float(mask)) * 1.7, 1.0);
}
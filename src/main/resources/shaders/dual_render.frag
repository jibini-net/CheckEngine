#version 120

uniform sampler2D tex;

varying vec2 tex_coord;
varying vec4 color;

uniform bool light_blocking;

void main()
{
    vec4 frag_color = texture2D(tex, tex_coord) * color;
    bool block = light_blocking && frag_color.a > 0.2;

    gl_FragColor = vec4(vec3(1.0) - vec3(int(block)), int(block));
}
#version 120

uniform sampler2D tex;

varying vec2 tex_coord;
varying vec4 color;

uniform bool light_blocking;

void main()
{
    vec4 frag_color = texture2D(tex, tex_coord) * color;

//    if (light_blocking && frag_color.a > 0.2)
//        gl_FragColor = vec4(1.0);
//    else
//        gl_FragColor = vec4(0.0);
    gl_FragColor = frag_color;
}
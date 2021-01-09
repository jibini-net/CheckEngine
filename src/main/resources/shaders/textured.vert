#version 120

uniform vec2 tex_offset;
uniform sampler2D tex;

varying vec2 tex_coord;
varying vec4 color;

void main()
{
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;

    tex_coord = gl_MultiTexCoord0.st + tex_offset;
    color = gl_Color;
}
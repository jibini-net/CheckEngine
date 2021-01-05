package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import org.lwjgl.opengl.GL20

@RegisterObject
class TempTexShaderImpl : Initializable
{
    private var texOffset = 0
    private var tex = 0

    private val vertSource = """
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
    """.trimIndent()

    val fragSource = """
        #version 120
        
        uniform sampler2D tex;
        
        varying vec2 tex_coord;
        varying vec4 color;
        
        void main()
        {
            gl_FragColor = texture2D(tex, tex_coord) * color;
        }
    """.trimIndent()

    override fun initialize()
    {
        val v = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(v, vertSource)
        GL20.glCompileShader(v)

        val f = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(f, fragSource)
        GL20.glCompileShader(f)

        val p = GL20.glCreateProgram()
        GL20.glAttachShader(p, v)
        GL20.glAttachShader(p, f)
        GL20.glLinkProgram(p)

        GL20.glDeleteShader(v)
        GL20.glDeleteShader(f)

        GL20.glUseProgram(p)

        texOffset = GL20.glGetUniformLocation(p, "tex_offset")
        tex = GL20.glGetUniformLocation(p, "tex")
    }

    fun updateUniform(s: Float, t: Float)
    {
        GL20.glUniform2f(texOffset, s, t)
        GL20.glUniform1i(tex, 0)
    }
}
package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer
import net.jibini.check.graphics.Shader
import org.lwjgl.opengl.GL20

class ShaderProgramImpl : AbstractAutoDestroyable(), Shader, Pointer<Int> by PointerImpl(GL20.glCreateProgram())
{
    override fun destroy()
    {
        GL20.glDeleteProgram(pointer)
    }

    override fun use()
    {
        GL20.glUseProgram(pointer)
    }

    override fun uniform(name: String, x: Int)
    {
        use()
        val location = GL20.glGetUniformLocation(pointer, name)

        GL20.glUniform1i(location, x)
    }

    override fun uniform(name: String, x: Float, y: Float)
    {
        use()
        val location = GL20.glGetUniformLocation(pointer, name)

        GL20.glUniform2f(location, x, y)
    }

    fun attach(shaderImpl: ShaderImpl)
    {
        GL20.glAttachShader(pointer, shaderImpl.pointer)
    }

    fun link()
    {
        GL20.glLinkProgram(pointer)
    }
}
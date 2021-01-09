package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer
import net.jibini.check.graphics.Shader
import org.lwjgl.opengles.GLES30

class ShaderProgramImpl : AbstractAutoDestroyable(), Shader, Pointer<Int> by PointerImpl(GLES30.glCreateProgram())
{
    override fun destroy()
    {
        GLES30.glDeleteProgram(pointer)
    }

    override fun use()
    {
        GLES30.glUseProgram(pointer)
    }

    override fun uniform(name: String, x: Int)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform1i(location, x)
    }

    override fun uniform(name: String, x: Float, y: Float)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform2f(location, x, y)
    }

    fun attach(shaderImpl: ShaderImpl)
    {
        GLES30.glAttachShader(pointer, shaderImpl.pointer)
    }

    fun link()
    {
        GLES30.glLinkProgram(pointer)
    }
}
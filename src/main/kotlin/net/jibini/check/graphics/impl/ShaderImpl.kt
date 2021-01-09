package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer
import org.lwjgl.opengl.GL20
import org.slf4j.LoggerFactory

class ShaderImpl(
    type: Int
) : AbstractAutoDestroyable(), Pointer<Int> by PointerImpl(GL20.glCreateShader(type))
{
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun destroy()
    {
        GL20.glDeleteShader(pointer)
    }

    fun source(source: String)
    {
        GL20.glShaderSource(pointer, source)
    }

    fun compile()
    {
        GL20.glCompileShader(pointer)
    }

    fun verify()
    {
        log.error("SHADER COMPILE LOG:\n" + GL20.glGetShaderInfoLog(pointer))
    }

    companion object
    {
        fun create(type: Int, source: String): ShaderImpl
        {
            val result = ShaderImpl(type)

            result.source(source)
            result.compile()

            result.verify()

            return result
        }
    }
}
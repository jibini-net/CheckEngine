package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer
import org.lwjgl.opengles.GLES30
import org.slf4j.LoggerFactory

class ShaderImpl(
    type: Int
) : AbstractAutoDestroyable(), Pointer<Int> by PointerImpl(GLES30.glCreateShader(type))
{
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun destroy()
    {
        GLES30.glDeleteShader(pointer)
    }

    fun source(source: String)
    {
        GLES30.glShaderSource(pointer, source)
    }

    fun compile()
    {
        GLES30.glCompileShader(pointer)
    }

    fun verify()
    {
        val errorLog = GLES30.glGetShaderInfoLog(pointer)
        if (errorLog.isNotEmpty())
            log.error("SHADER COMPILE LOG:\n$errorLog")
        else
            log.debug("Shader compiled with no errors logged")
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
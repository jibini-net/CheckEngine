package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer
import org.lwjgl.opengles.GLES30
import org.slf4j.LoggerFactory

/**
 * Implementation of a single GLSL shader which can be compiled and
 * verified. Can be attached to a [ShaderProgramImpl].
 *
 * @author Zach Goethel
 */
class ShaderImpl(
    /**
     * OpenGL constant for the type of shader.
     */
    type: Int
) : AbstractAutoDestroyable(), Pointer<Int> by PointerImpl(GLES30.glCreateShader(type))
{
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun destroy()
    {
        GLES30.glDeleteShader(pointer)
    }

    /**
     * Puts the give source into the shader object.
     *
     * @param source Shader GLSL source code.
     */
    fun source(source: String)
    {
        GLES30.glShaderSource(pointer, source)
    }

    /**
     * Compiles the shader. The shader should have [source].
     */
    fun compile()
    {
        GLES30.glCompileShader(pointer)
    }

    /**
     * Checks the shader compile log for errors and warnings.
     */
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
        /**
         * Creates, sources, and compiles a shader of the given type.
         *
         * @param type OpenGL constant for the type of shader.
         * @param source Shader GLSL source code.
         */
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
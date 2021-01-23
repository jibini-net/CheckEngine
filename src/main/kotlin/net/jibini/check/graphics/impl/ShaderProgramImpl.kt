package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Pointer
import net.jibini.check.graphics.Shader
import net.jibini.check.graphics.Uniforms

import org.joml.Matrix4f

import org.lwjgl.BufferUtils
import org.lwjgl.opengles.GLES30

import org.slf4j.LoggerFactory

/**
 * Implementation of an OpenGL shader program.
 *
 * @author Zach Goethel
 */
class ShaderProgramImpl : AbstractAutoDestroyable(), Shader, Pointer<Int> by PointerImpl(GLES30.glCreateProgram())
{
    private val log = LoggerFactory.getLogger(this::class.java)

    // Required to access uniforms which should be written
    @EngineObject
    private lateinit var uniforms: Uniforms

    // Required to read and write the currently bound shader
    @EngineObject
    private lateinit var statefulShaderImpl: StatefulShaderImpl

    override fun destroy()
    {
        GLES30.glDeleteProgram(pointer)
    }

    override fun use()
    {
        if (statefulShaderImpl.boundShader != this)
        {
            GLES30.glUseProgram(pointer)
            statefulShaderImpl.boundShader = this

            uniform("tex_offset", uniforms.textureOffset.x, uniforms.textureOffset.y)
            uniform("tex_delta", uniforms.textureDelta.x, uniforms.textureDelta.y)
            uniform("tex", uniforms.texture)

            uniform("color_mult",
                uniforms.colorMultiple.x,
                uniforms.colorMultiple.y,
                uniforms.colorMultiple.z,
                uniforms.colorMultiple.w
            )

            uniform("light_blocking", uniforms.blocking.compareTo(false))
        }
    }

    override fun uniform(name: String, x: Int)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform1i(location, x)
    }

    override fun uniform(name: String, x: Float)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform1f(location, x)
    }

    override fun uniform(name: String, x: Float, y: Float)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform2f(location, x, y)
    }

    override fun uniform(name: String, x: Float, y: Float, z: Float)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform3f(location, x, y, z)
    }

    override fun uniform(name: String, x: Float, y: Float, z: Float, w: Float)
    {
        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniform4f(location, x, y, z, w)
    }

    /**
     * A shared buffer for writing four-by-four matrices.
     */
    private val floatBuffer16 = BufferUtils.createFloatBuffer(16)

    override fun uniform(name: String, matrix: Matrix4f)
    {
        matrix.get(floatBuffer16)

        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniformMatrix4fv(location, false, floatBuffer16)
    }

    /**
     * Attaches the given shader to the shader program.
     *
     * @param shader Shader to attach.
     */
    fun attach(shader: ShaderImpl)
    {
        GLES30.glAttachShader(pointer, shader.pointer)
    }

    /**
     * Links the shader program to the OpenGL pipeline.
     */
    fun link()
    {
        GLES30.glLinkProgram(pointer)
    }

    /**
     * Checks the program link log for errors and warnings.
     */
    fun verify()
    {
        val errorLog = GLES30.glGetProgramInfoLog(pointer)

        if (errorLog.isNotEmpty())
            log.error("PROGRAM LINK LOG:\n$errorLog")
        else
            log.debug("Shader compiled with no errors logged")
    }
}

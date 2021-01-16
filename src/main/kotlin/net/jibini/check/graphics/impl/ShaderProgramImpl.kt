package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Pointer
import net.jibini.check.graphics.Shader
import net.jibini.check.graphics.Uniforms
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengles.GLES30
import org.slf4j.LoggerFactory

class ShaderProgramImpl : AbstractAutoDestroyable(), Shader, Pointer<Int> by PointerImpl(GLES30.glCreateProgram())
{
    private val log = LoggerFactory.getLogger(this::class.java)
    
    @EngineObject
    private lateinit var uniforms: Uniforms

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

    private val floatBuffer16 = BufferUtils.createFloatBuffer(16)

    override fun uniform(name: String, matrix: Matrix4f)
    {
        matrix.get(floatBuffer16)

        use()
        val location = GLES30.glGetUniformLocation(pointer, name)

        GLES30.glUniformMatrix4fv(location, false, floatBuffer16)
    }

    fun attach(shaderImpl: ShaderImpl)
    {
        GLES30.glAttachShader(pointer, shaderImpl.pointer)
    }

    fun link()
    {
        GLES30.glLinkProgram(pointer)
    }

    fun verify()
    {
        val errorLog = GLES30.glGetProgramInfoLog(pointer)
        if (errorLog.isNotEmpty())
            log.error("PROGRAM LINK LOG:\n$errorLog")
        else
            log.debug("Shader compiled with no errors logged")
    }
}

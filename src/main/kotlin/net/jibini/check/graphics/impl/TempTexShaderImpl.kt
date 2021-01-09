package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader
import net.jibini.check.resource.Resource

@RegisterObject
class TempTexShaderImpl : Initializable
{
    private lateinit var shader: Shader

    override fun initialize()
    {
        shader = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/textured.frag")
        )

        shader.use()
    }

    fun updateUniform(s: Float, t: Float)
    {
        if (this::shader.isInitialized)
        {
            shader.uniform("tex_offset", s, t)
            shader.uniform("tex", 0)
        }
    }

    fun updateBlocking(blocking: Boolean)
    {
        if (this::shader.isInitialized)
            shader.uniform("light_blocking", if (blocking) 1 else 0)
    }
}
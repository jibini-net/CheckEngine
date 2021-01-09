package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader
import net.jibini.check.resource.Resource

@RegisterObject
class DirectTexShaderImpl : Initializable
{
    lateinit var shader: Shader

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

    val init: Boolean
        get() = this::shader.isInitialized
}
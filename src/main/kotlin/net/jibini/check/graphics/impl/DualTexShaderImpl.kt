package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader
import net.jibini.check.resource.Resource

@RegisterObject
class DualTexShaderImpl : Initializable
{
    private lateinit var shader: Shader

    var claimRender = false

    override fun initialize()
    {
        shader = Shader.create(
            Resource.fromClasspath("shaders/dual_render.vert"),
            Resource.fromClasspath("shaders/dual_render.frag")
        )
    }

    fun performDual(renderTask: () -> Unit)
    {
        //TODO RENDER INTO FRAMEBUFFER
        if (this::shader.isInitialized)
            shader.use()

        claimRender = true
        renderTask()
        claimRender = false
    }
}
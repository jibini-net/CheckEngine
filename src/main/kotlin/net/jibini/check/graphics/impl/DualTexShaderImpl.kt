package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader
import net.jibini.check.input.Keyboard
import net.jibini.check.resource.Resource
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengles.GLES30

@RegisterObject
class DualTexShaderImpl : Initializable
{
    @EngineObject
    private lateinit var directTexShaderImpl: DirectTexShaderImpl

    @EngineObject
    private lateinit var keyboard: Keyboard

    private lateinit var shader: Shader

    var claimRender = false

    override fun initialize()
    {
        shader = Shader.create(
            Resource.fromClasspath("shaders/dual_render.vert"),
            Resource.fromClasspath("shaders/dual_render.frag")
        )
    }

    fun updateBlocking(blocking: Boolean)
    {
        if (this::shader.isInitialized)
            shader.uniform("light_blocking", if (blocking) 1 else 0)
    }

    fun performDual(renderTask: () -> Unit)
    {
        //TODO RENDER INTO FRAMEBUFFER
        claimRender = true
        if (this::shader.isInitialized)
            shader.use()
        if (keyboard.isPressed(GLFW.GLFW_KEY_0)) //TODO TEMP
        {
            GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

            renderTask()
        }

        claimRender = false
        if (directTexShaderImpl.init)
            directTexShaderImpl.shader.use()
        if (!keyboard.isPressed(GLFW.GLFW_KEY_0)) //TODO TEMP
        {
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

            renderTask()
        }
    }

    fun updateUniform(s: Float, t: Float)
    {
        if (this::shader.isInitialized)
        {
            shader.uniform("tex_offset", s, t)
            shader.uniform("tex", 0)
        }
    }
}
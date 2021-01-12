package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.FeatureSet
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.*
import net.jibini.check.input.Keyboard
import net.jibini.check.resource.Resource
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengles.GLES30
import java.awt.Frame

@RegisterObject
class LightingShaderImpl : Initializable
{
    @EngineObject
    private lateinit var window: Window

    @EngineObject
    private lateinit var keyboard: Keyboard

    @EngineObject
    private lateinit var directTex: DirectTexShaderImpl

    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var matrices: Matrices

    @EngineObject
    private lateinit var featureSet: FeatureSet

    private lateinit var shader: Shader
    private lateinit var framebuffer: Framebuffer
    private lateinit var shadow: Shader
    private lateinit var shadowDownscale: Framebuffer

    override fun initialize()
    {
        shader = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/light_mask.frag")
        )

        shadow = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/shadow.frag")
        )

        framebuffer = Framebuffer(window.width, window.height, 2)
        shadowDownscale = Framebuffer(window.width, window.height, 1)
    }

    private fun captureTextures(renderTask: () -> Unit)
    {
        framebuffer.bind()
        shader.use()
        GLES30.glClear(featureSet.clearFlags)

        renderTask()

        Framebuffer.release()
    }

    fun perform(renderTask: () -> Unit)
    {
//        if (framebuffer.width != window.width || framebuffer.height != window.height)
//        {
//            framebuffer.destroy()
//            framebuffer = Framebuffer(window.width / 10, window.height / 10, 2)
//
//            shadowDownscale.destroy()
//            shadowDownscale = Framebuffer(window.width / 4, window.height / 4)
//        }

        captureTextures(renderTask)

        matrices.projection.identity()
        matrices.model.identity()

        if (keyboard.isPressed(GLFW.GLFW_KEY_L))
        {
            shadowDownscale.bind()
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

            shadow.use()

            GLES30.glDisable(GLES30.GL_DEPTH_TEST)
            GLES30.glEnable(GLES30.GL_BLEND)
            GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE)

            framebuffer.renderAttachments[1]
                .flip(horizontal = false, vertical = true)
                .bind()

            shadow.uniform("light", 0.2f, 0.5f)
            shadow.uniform("light_color", 0.5f, 0.25f, 0.25f)

            renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

            shadow.uniform("light", 0.8f, 0.5f)
            shadow.uniform("light_color", 0.4f, 0.25f, 0.7f)

            renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

            shadow.uniform("light", 0.5f, 0.6f)
            shadow.uniform("light_color", 0.5f, 1.0f, 0.5f)

            renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

            directTex.shader.use()

            renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

            GLES30.glBlendFunc(GLES30.GL_DST_COLOR, GLES30.GL_ONE_MINUS_SRC_ALPHA)

            framebuffer.renderAttachments[0]
                .flip(horizontal = false, vertical = true)
                .bind()

            renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)

            Framebuffer.release()

            GLES30.glViewport(0, 0, window.width, window.height)

            directTex.shader.use()
            shadowDownscale.renderAttachments[0]
                .flip(horizontal = false, vertical = true)
                .bind()
        } else
        {
            GLES30.glViewport(0, 0, window.width, window.height)
            framebuffer.renderAttachments[0]
                .flip(horizontal = false, vertical = true)
                .bind()
        }

        renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)
    }
}
package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.FeatureSet
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.*
import net.jibini.check.resource.Resource
import org.lwjgl.opengles.GLES30

@RegisterObject
class LightingShaderImpl : Initializable
{
    @EngineObject
    private lateinit var window: Window

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

    override fun initialize()
    {
        shader = Shader.create(
            Resource.fromClasspath("shaders/light_mask.vert"),
            Resource.fromClasspath("shaders/light_mask.frag")
        )

        framebuffer = Framebuffer(window.width, window.height, 2)
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
        if (framebuffer.width != window.width || framebuffer.height != window.height)
        {
            framebuffer.destroy()
            framebuffer = Framebuffer(window.width, window.height, 2)
        }

        captureTextures(renderTask)

        directTex.shader.use()

        matrices.projection.identity()
        matrices.model.identity()

        framebuffer.renderAttachments[0]
            .flip(horizontal = false, vertical = true)
            .bind()

        renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)
    }
}
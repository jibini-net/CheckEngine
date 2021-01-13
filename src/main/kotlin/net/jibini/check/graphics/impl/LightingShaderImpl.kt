package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.*
import net.jibini.check.resource.Resource
import net.jibini.check.world.GameWorld
import org.lwjgl.opengles.GLES30
import java.awt.Frame

@RegisterObject
class LightingShaderImpl : Initializable
{
    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var window: Window

    @EngineObject
    private lateinit var gameWorld: GameWorld

    @EngineObject
    private lateinit var matrices: Matrices

    private lateinit var lightMask: Shader
    private lateinit var worldSpace: Framebuffer

    private lateinit var textured: Shader

    private lateinit var rayTracer: Shader
    private lateinit var rays: Framebuffer

    private val pixelsPerTile = 64
    private val raysSize = 32

    override fun initialize()
    {
        lightMask = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/light_mask.frag")
        )

        textured = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/textured.frag")
        )

        rayTracer = Shader.create(
            Resource.fromClasspath("shaders/ray_tracing.vert"),
            Resource.fromClasspath("shaders/ray_tracing.frag")
        )

        rays = Framebuffer(raysSize, raysSize, 1)
    }

    private fun validateFramebuffers()
    {
        val properWidth = gameWorld.room!!.width * pixelsPerTile
        val properHeight = gameWorld.room!!.height * pixelsPerTile

        if (!this::worldSpace.isInitialized
            || worldSpace.width != properWidth
            || worldSpace.height != properHeight)
        {
            worldSpace = Framebuffer(properWidth, properHeight, 2)
        }
    }

    private fun generateLightMask(renderTask: () -> Unit)
    {
        worldSpace.bind()
        lightMask.use()
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        matrices.projection
            .identity()
            .ortho(
                0.0f, 0.2f * gameWorld.room!!.width,
                0.0f, 0.2f * gameWorld.room!!.height,
                -100.0f, 100.0f
            )

        renderTask()

        Framebuffer.release()
    }

    private fun generatePresentedCopy(renderTask: () -> Unit)
    {
        textured.use()

        val windowRatio = window.width.toFloat() / window.height

        matrices.projection.identity()
        matrices.model.identity()

        GLES30.glViewport(0, 0, window.width, window.height)

        matrices.projection.ortho(
            -windowRatio, windowRatio,
            -1.0f, 1.0f,
            -100.0f, 100.0f
        )

        val offset = 0.3f
        val scale = 1.4f
        val playerX = gameWorld.player!!.x.toFloat()
        val playerY = gameWorld.player!!.y.toFloat()

        matrices.model.pushMatrix()
        matrices.model.translate(0.0f, 0.0f, 90.0f)
        rays.renderAttachments[0].bind()
        renderer.drawRectangle(-windowRatio, -1.0f, 0.5f, 0.5f)
        matrices.model.popMatrix()

        matrices.model.scale(scale)
        matrices.model.translate(-playerX, -playerY - offset, 0.0f)

        renderTask()
    }

    private fun generateRays(lightX: Float, lightY: Float)
    {
        rays.bind()

        rayTracer.use()
        rayTracer.uniform("output_size", raysSize)

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        matrices.projection.identity()
            .ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f)
        matrices.model.identity()

        renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

        Framebuffer.release()
    }

    fun perform(renderTask: () -> Unit)
    {
        validateFramebuffers()
        generateLightMask(renderTask)
        generateRays(0.0f, 0.0f)

        generatePresentedCopy(renderTask)

        rayTracer.use()
        worldSpace.renderAttachments[1].bind()

        rayTracer.uniform("output_size", 32)
        rayTracer.uniform("input_width", worldSpace.width)
        rayTracer.uniform("input_height", worldSpace.height)
        rayTracer.uniform("light_mask", 0)

        rayTracer.uniform("light_position", 4.0f, 2.0f)

        renderer.drawRectangle(-1.0f, -1.0f, 0.2f, 0.2f)
    }
}
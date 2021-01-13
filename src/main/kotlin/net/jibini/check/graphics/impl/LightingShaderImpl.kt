package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.*
import net.jibini.check.resource.Resource
import net.jibini.check.world.GameWorld
import org.joml.Matrix4f
import org.lwjgl.opengles.GLES30

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

    private lateinit var shadowAndLight: Shader

    private lateinit var textured: Shader

    private lateinit var rayTracer: Shader
    private lateinit var rays: Framebuffer

    private val pixelsPerTile = 32
    private val raysSize = 64

    private val offset = 0.3f
    private val scale = 1.4f

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

        shadowAndLight = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/shadow.frag")
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

    private fun drawPresentedCopy(renderTask: () -> Unit)
    {
        textured.use()
        GLES30.glViewport(0, 0, window.width, window.height)

        val windowRatio = window.width.toFloat() / window.height

        matrices.projection.identity()
        matrices.model.identity()

        matrices.projection.ortho(
            -windowRatio, windowRatio,
            -1.0f, 1.0f,
            -100.0f, 100.0f
        )

        val playerX = gameWorld.player!!.x.toFloat()
        val playerY = gameWorld.player!!.y.toFloat()

        //TODO REMOVE
        matrices.model.pushMatrix()
        matrices.model.translate(0.0f, 0.0f, 90.0f)
        rays.renderAttachments[0].bind()
        renderer.drawRectangle(-windowRatio, -1.0f, 0.5f, 0.5f)
        matrices.model.popMatrix()
        //TODO REMOVE

        matrices.model.scale(scale)
        matrices.model.translate(-playerX, -playerY - offset, 0.0f)

        renderTask()
    }

    private fun generateRays(lightX: Float, lightY: Float)
    {
        rays.bind()
        rayTracer.use()

        worldSpace.renderAttachments[1].bind()

        rayTracer.uniform("output_size", raysSize)
        rayTracer.uniform("input_width", worldSpace.width)
        rayTracer.uniform("input_height", worldSpace.height)
        rayTracer.uniform("light_mask", 0)

        rayTracer.uniform("light_position", lightX, lightY)

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        matrices.projection.identity()
            .ortho(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f)
        matrices.model.identity()

        renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

        Framebuffer.release()
    }

    private fun drawShadows(lightX: Float, lightY: Float, r: Float, g: Float, b: Float)
    {
        shadowAndLight.use()
        GLES30.glViewport(0, 0, window.width, window.height)

        val windowRatio = window.width.toFloat() / window.height

        matrices.projection.identity()
            .ortho(-windowRatio, windowRatio, -1.0f, 1.0f, -1.0f, 1.0f)
        matrices.model.identity()

        rays.renderAttachments[0]
            .flip(horizontal = false, vertical = true)
            .bind()

        val playerX = gameWorld.player!!.x.toFloat()
        val playerY = gameWorld.player!!.y.toFloat()

        val matrix = Matrix4f()
            .ortho(-windowRatio, windowRatio, -1.0f, 1.0f, -1.0f, 1.0f)
            .invertOrtho()
            .translate(playerX * scale / windowRatio, (playerY + offset) * scale, 0.0f)
            .scaleLocal(1.0f / scale, 1.0f / scale, 1.0f)

        shadowAndLight.uniform("input_size", raysSize)
        shadowAndLight.uniform("light_color", r, g, b)
        shadowAndLight.uniform("light_position", lightX, lightY)

        shadowAndLight.uniform("frag_matrix", matrix)

        renderer.drawRectangle(-windowRatio, -1.0f, windowRatio * 2.0f, 2.0f)
    }

    fun perform(renderTask: () -> Unit)
    {
        validateFramebuffers()
        generateLightMask(renderTask)

        drawPresentedCopy(renderTask)

        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        generateRays(3.0f, 2.0f)
        drawShadows(3.0f, 2.0f, 1.0f, 0.3f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
    }
}
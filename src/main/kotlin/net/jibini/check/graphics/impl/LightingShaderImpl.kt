package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.*
import net.jibini.check.resource.Resource
import net.jibini.check.world.GameWorld
import org.joml.Matrix4f
import org.joml.Vector2d
import org.lwjgl.opengles.GLES30

/**
 * The lighting engine and lighting algorithm pipeline. This is an
 * implementation class and is subject to change.
 *
 * @author Zach Goethel
 */
@RegisterObject
class LightingShaderImpl : Initializable
{
    // Required to draw quads to process render passes
    @EngineObject
    private lateinit var renderer: Renderer

    // Required to access the current window size
    @EngineObject
    private lateinit var window: Window

    // Required to access the world's tile array size
    @EngineObject
    private lateinit var gameWorld: GameWorld

    // Required to modify the transform matrices
    @EngineObject
    private lateinit var matrices: Matrices

    /**
     * A shader program which will produce two output textures: one
     * with color and textures, and one black and white mask of which
     * elements in the world are light-blocking.
     */
    private lateinit var lightMask: Shader

    /**
     * A framebuffer which spans over the entire level.
     */
    private lateinit var worldSpace: Framebuffer

    /**
     * A shader program which compares fragments on screen to the rays
     * recorded in the ray atlas. It draws shadows and assigns light
     * values to fragments.
     */
    private lateinit var shadowAndLight: Shader

    /**
     * Simple shader program for textured and colored drawing.
     */
    private lateinit var textured: Shader

    /**
     * Generates a ray atlas for a light into a small output texture.
     */
    private lateinit var rayTracer: Shader

    /**
     * Ray atlas which stores the traced rays for each light.
     */
    private lateinit var rays: Framebuffer

    /**
     * A framebuffer which spans over the screen, which may be
     * downscaled.
     */
    private lateinit var screenSpace: Framebuffer

    /**
     * A framebuffer which spans over the screen, which may be
     * downscaled.
     *
     * This framebuffer is used to combine multiple lights.
     */
    private lateinit var screen: Framebuffer

    /**
     * Number of pixels per tile in the world-space framebuffer. Decides
     * the resolution of the world-space framebuffer.
     */
    private val pixelsPerTile = 16

    /**
     * The ray atlas will have this many rays across one edge of its
     * texture. The number of rays will be this number squared.
     */
    private val raysSize = 16

    /**
     * The offset of the character compared to the center of the screen.
     */
    private val offset = 0.3f

    /**
     * World tiles and entities will be rendered with this size scale.
     */
    private val scale = 1.4f

    /**
     * When this flag is set to true, non-light-blocking entities which
     * should be bright despite any surrounding lighting should be
     * rendered as light-blocking.
     */
    var nlBlockingOverride = false

    /**
     * The global collection of lights in the level.
     */
    val lights = mutableListOf<Light>()

    var properWidth = 0
    var properHeight = 0

    /**
     * The ratio of the window width to the window height.
     */
    private val windowRatio: Float
        get() = properWidth.toFloat() / properHeight

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
            Resource.fromClasspath("shaders/shadow.vert"),
            Resource.fromClasspath("shaders/shadow.frag")
        )

        rays = Framebuffer(raysSize, raysSize, 1)
    }

    /**
     * Compares the resolution of the framebuffers to what they should
     * be and recreates them if necessary.
     */
    private fun validateFramebuffers()
    {
        properWidth = gameWorld.room!!.width * pixelsPerTile
        properHeight = gameWorld.room!!.height * pixelsPerTile

        if (!this::worldSpace.isInitialized
            || worldSpace.width != properWidth
            || worldSpace.height != properHeight)
        {
            worldSpace = Framebuffer(properWidth, properHeight, 2)
        }

        properHeight = (2.0f / scale / 0.2f * 16.0f).toInt();
        properWidth = (properHeight.toFloat() * (window.width.toFloat() / window.height)).toInt()

        properWidth -= properWidth % (16.0f * scale).toInt()

        if (!this::screenSpace.isInitialized
            || screenSpace.width != properWidth
            || screenSpace.height != properHeight)
        {
            screenSpace = Framebuffer(properWidth, properHeight, 2)
            screen = Framebuffer(properWidth, properHeight, 2)
        }
    }

    /**
     * Renders the entirety of the level into the world-space
     * framebuffer. This creates a world-wide mask of which elements
     * block light.
     */
    private fun generateLightMask(renderTask: () -> Unit)
    {
        worldSpace.bind()
        lightMask.use()

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // Set the projection matrix to cover the whole world
        matrices.projection
            .identity()
            .ortho(
                0.0f, 0.2f * gameWorld.room!!.width,
                0.0f, 0.2f * gameWorld.room!!.height,
                -100.0f, 100.0f
            )
        matrices.model.identity()

        renderTask()

        Framebuffer.release()
    }

    /**
     * Applies a scale and translation to correctly center the player on
     * the screen and the world around the player.
     */
    private fun worldTransform()
    {
        matrices.projection
            .identity()
            .ortho(
                -windowRatio, windowRatio,
                -1.0f, 1.0f,
                -100.0f, 100.0f
            )
        matrices.model.identity()

        val playerX = gameWorld.player!!.x.toFloat()
        val playerY = gameWorld.player!!.y.toFloat()

        // Snap to pixel
        var translateX = -playerX
        translateX -= translateX % (0.2f / 16)
        var translateY = -playerY - offset
        translateY -= translateY % (0.2f / 16)

        matrices.model.scale(scale)
        matrices.model.translate(translateX, translateY, 0.0f)
    }

    /**
     * Renders the portion of the level which is currently on screen.
     * During this render process, the [nlBlockingOverride] is set.
     */
    private fun generatePresentedCopy(renderTask: () -> Unit)
    {
        screenSpace.bind()
        lightMask.use()

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        worldTransform()

        nlBlockingOverride = true
        renderTask()
        nlBlockingOverride = false

        Framebuffer.release()
    }

    /**
     * Performs the ray-tracing for one light position.
     */
    private fun generateRays(lightX: Float, lightY: Float)
    {
        rays.bind()
        rayTracer.use()

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        matrices.projection.identity()
            .ortho(
                -1.0f, 1.0f,
                -1.0f, 1.0f,
                -1.0f, 1.0f
            )
        matrices.model.identity()

        // Bind the global light mask
        worldSpace
            .renderAttachments[1]
            .bind()

        // Set the ray-tracer uniforms
        rayTracer.uniform("output_size", raysSize)
        rayTracer.uniform("input_width", worldSpace.width)
        rayTracer.uniform("input_height", worldSpace.height)
        rayTracer.uniform("light_mask", 0)
        rayTracer.uniform("light_position", lightX, lightY)

        renderer.drawRectangle(-1.0f, -1.0f, 2.0f, 2.0f)

        Framebuffer.release()
    }

    /**
     * Draws the shadows in screen-space for one light position.
     */
    private fun drawShadows(lightX: Float, lightY: Float, r: Float, g: Float, b: Float)
    {
        screen.bind()
        shadowAndLight.use()

        matrices.projection.identity()
            .ortho(
                -windowRatio, windowRatio,
                -1.0f, 1.0f,
                -1.0f, 1.0f
            )
        matrices.model.identity()

        rays.renderAttachments[0]
            .flip(horizontal = false, vertical = true)
            .bind()

        val playerX = gameWorld.player!!.x.toFloat()
        val playerY = gameWorld.player!!.y.toFloat()

        val matrix = Matrix4f()
            .ortho(-windowRatio, windowRatio, -1.0f, 1.0f, -1.0f, 1.0f)
            .invertOrtho()
            .translate(playerX / windowRatio * scale, (playerY + offset) * scale, 0.0f)
            .scaleLocal(1.0f / scale, 1.0f / scale, 1.0f)

        shadowAndLight.uniform("input_size", raysSize)
        shadowAndLight.uniform("light_color", r, g, b)
        shadowAndLight.uniform("light_position", lightX, lightY)
        shadowAndLight.uniform("frag_matrix", matrix)
        shadowAndLight.uniform("ray_scale", 1.0f / scale)

        renderer.drawRectangle(-windowRatio, -1.0f, windowRatio * 2.0f, 2.0f)

        Framebuffer.release()
    }

    /**
     * Performs a blend of all of the lights, a [nlBlockingOverride]
     * mask, and the world colors.
     */
    private fun halfResolutionRender()
    {
        screen.bind()

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE)

        for (light in lights)
        {
            if (Vector2d(
                    gameWorld.player!!.x,
                    gameWorld.player!!.y
                ).distance(Vector2d(
                    light.x.toDouble() * 0.2,
                    light.y.toDouble() * 0.2)
                ) > windowRatio * 1.6f
            ) continue

            generateRays(light.x, light.y)
            drawShadows(light.x, light.y, light.r, light.g, light.b)
        }

        screen.bind()
        textured.use()

        matrices.projection.identity()
            .ortho(
                -windowRatio, windowRatio,
                -1.0f, 1.0f,
                -1.0f, 1.0f
            )
        matrices.model.identity()

        screenSpace.renderAttachments[1]
            .flip(horizontal = false, vertical = true)
            .bind()

        renderer.drawRectangle(-windowRatio, -1.0f, windowRatio * 2, 2.0f)

        if (lights.isEmpty())
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
        else
            GLES30.glBlendFunc(GLES30.GL_DST_COLOR, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        screenSpace.renderAttachments[0]
            .flip(horizontal = false, vertical = true)
            .bind()

        renderer.drawRectangle(-windowRatio, -1.0f, windowRatio * 2, 2.0f)

        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        Framebuffer.release()
    }

    /**
     * Performs a lit render of the given lambda collection of render
     * calls. This can include rendering of entities and world rooms,
     * but should not include updates to physics or AI.
     *
     * @param renderTask Lambda containing render calls for lighting.
     */
    fun perform(renderTask: () -> Unit)
    {
        validateFramebuffers()

        generateLightMask(renderTask)
        generatePresentedCopy(renderTask)

        halfResolutionRender()
        
        textured.use()

        GLES30.glViewport(0, 0, window.width, window.height)

        screen.renderAttachments[0]
            .flip(horizontal = false, vertical = true)
            .bind()

        renderer.drawRectangle(-windowRatio - 0.1f, -1.1f, windowRatio * 2.2f, 2.2f)

        rays.renderAttachments[0]
            .flip(horizontal = false, vertical = true)
            .bind()

        renderer.drawRectangle(-windowRatio + 0.075f, -0.925f, 0.5f, 0.5f)
    }
}

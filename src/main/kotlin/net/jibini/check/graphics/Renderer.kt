package net.jibini.check.graphics

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.world.Room

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * A per-context engine object which helps with rendering textured
 * rectangles. This renderer replaces the immediate mode functionality
 * of antiquated OpenGL, and also reimplements display-lists as [render
 * groups][RenderGroup].
 *
 * @author Zach Goethel
 */
class Renderer : EngineAware()
{
    // Required to access the current transformation matrices
    @EngineObject
    private lateinit var matrices: Matrices

    // Required to access the framebuffer pixel snap
    @EngineObject
    private lateinit var lightingShader: LightingShaderImpl

    /**
     * The current render group. If non-null, any rendering will be
     * appended to this group. If null, render calls will be immediately
     * drawn to the screen.
     */
    private var renderGroup: RenderGroup? = null

    /**
     * Begins a new render group. Any render calls made while this group
     * remains bound will be appended to the render group. A render
     * group can be re-bound via [continueGroup].
     *
     * _Do not forget to call [finalizeGroup] when this render group is
     * complete._
     *
     * @return The newly created [render group][RenderGroup].
     */
    fun beginGroup(): RenderGroup
    {
        renderGroup = RenderGroup()

        return renderGroup!!
    }

    /**
     * Re-binds the given render group to append additional geometry.
     * The render group must not be finalized. Use this method if
     * multiple render groups are being created simultaneously, but none
     * will be finalized until all are ready. See [Room] for an example
     * implementation.
     *
     * _Do not forget to call [finalizeGroup] when this render group is
     * complete._
     */
    fun continueGroup(group: RenderGroup)
    {
        renderGroup = group
    }

    /**
     * Finalizes the current render group and unbinds it.
     */
    fun finalizeGroup()
    {
        renderGroup?.finalize()
        renderGroup = null
    }

    /**
     * Implementation which caches a unit-square such that immediate
     * mode does not construct a new render group with every call.
     */
    private val cachedTexture = HashMap<Pair<Float, Float>, RenderGroup>()

    /**
     * Draws a rectangle with the given coordinates and size; uses the
     * currently bound texture and its texture coordinates to texture
     * the rectangle.
     *
     * If a [render group][RenderGroup] is [bound][beginGroup], this
     * rectangle will be appended to that render group. If none is
     * bound, the quad will be rendered directly to the screen.
     *
     * @param x Rectangle start x-coordinate.
     * @param y Rectangle start x-coordinate.
     * @param width Rectangle relative width (can be negative).
     * @param height Rectangle relative height (can be negative).
     */
    fun drawRectangle(x: Float, y: Float, width: Float, height: Float)
    {
        if (renderGroup == null)
        {
            // Retrieve or generate the unit-square
            val unitSquare = cachedTexture.getOrPut(1.0f to 1.0f)
            {
                val result = beginGroup()

                drawRectangle(0.0f, 0.0f, 1.0f, 1.0f)

                finalizeGroup()
                result
            }

            // Snap to pixel
            val adjustX = x - x % (0.2f / lightingShader.framebufferPixelsPerTile)
            val adjustY = y - y % (0.2f / lightingShader.framebufferPixelsPerTile)

            // Scale and transform to the requested size/position
            matrices.model.pushMatrix()
            matrices.model.translate(adjustX, adjustY, 0.0f)
            matrices.model.scale(width, height, 1.0f)

            // Immediately render the requested quad
            unitSquare.call()

            matrices.model.popMatrix()
        } else
        {
            // Append the vertices to the current render group
            renderGroup!!.consumeVertex(
                Vector3f(x, y, 0.0f),
                Vector4f(1.0f),
                Vector2f(0.0f, 1.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x + width, y + height, 0.0f),
                Vector4f(1.0f),
                Vector2f(1.0f, 0.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x, y + height, 0.0f),
                Vector4f(1.0f),
                Vector2f(0.0f, 0.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x, y, 0.0f),
                Vector4f(1.0f),
                Vector2f(0.0f, 1.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x + width, y, 0.0f),
                Vector4f(1.0f),
                Vector2f(1.0f, 1.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x + width, y + height, 0.0f),
                Vector4f(1.0f),
                Vector2f(1.0f, 0.0f)
            )
        }
    }
}
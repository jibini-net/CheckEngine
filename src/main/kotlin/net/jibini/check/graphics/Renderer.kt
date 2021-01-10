package net.jibini.check.graphics

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.texture.impl.TextureRegistry
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * A per-context engine object which helps with rendering textured rectangles
 *
 * @author Zach Goethel
 */
class Renderer : EngineAware()
{
    @EngineObject
    private lateinit var textureRegistry: TextureRegistry

    @EngineObject
    private lateinit var matrices: Matrices

    private var renderGroup: RenderGroup? = null

    fun beginGroup(): RenderGroup
    {
        renderGroup = RenderGroup()

        return renderGroup!!
    }

    fun continueGroup(group: RenderGroup)
    {
        renderGroup = group
    }

    fun finalizeGroup()
    {
        renderGroup?.finalize()
        renderGroup = null
    }

    private val cachedTexture = HashMap<Pair<Float, Float>, RenderGroup>()

    /**
     * Draws a rectangle with the given coordinates and size; uses the currently bound texture and its texture
     * coordinates to texture the rectangle
     *
     * @param x Rectangle start x-coordinate
     * @param y Rectangle start x-coordinate
     *
     * @param width Rectangle relative width (can be negative)
     * @param height Rectangle relative height (can be negative)
     */
    fun drawRectangle(x: Float, y: Float, width: Float, height: Float)
    {
        val coordinates = textureRegistry.bound?.textureCoordinates

        // Find texture width and height
        val dx = coordinates?.deltaX ?: 0.0f
        val dy = coordinates?.deltaY ?: 0.0f

        if (renderGroup == null)
        {
            val unitSquare = cachedTexture.getOrPut(dx to dy)
            {
                val result = beginGroup()

                drawRectangle(0.0f, 0.0f, 1.0f, 1.0f)

                finalizeGroup()
                result
            }

            matrices.model.pushMatrix()
            matrices.model.translate(x, y, 0.0f)
            matrices.model.scale(width, height, 1.0f)

            unitSquare.call()

            matrices.model.popMatrix()
        } else
        {
            renderGroup!!.consumeVertex(
                Vector3f(x, y, 0.0f),
                Vector4f(1.0f),
                Vector2f(0.0f, dy)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x + width, y + height, 0.0f),
                Vector4f(1.0f),
                Vector2f(dx, 0.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x, y + height, 0.0f),
                Vector4f(1.0f),
                Vector2f(0.0f, 0.0f)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x, y, 0.0f),
                Vector4f(1.0f),
                Vector2f(0.0f, dy)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x + width, y, 0.0f),
                Vector4f(1.0f),
                Vector2f(dx, dy)
            )

            renderGroup!!.consumeVertex(
                Vector3f(x + width, y + height, 0.0f),
                Vector4f(1.0f),
                Vector2f(dx, 0.0f)
            )
        }
    }
}
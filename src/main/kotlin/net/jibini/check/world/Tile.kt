package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer
import net.jibini.check.graphics.Window
import net.jibini.check.texture.Texture
import org.lwjgl.glfw.GLFW

/**
 * A small section of a game level which can be interacted with
 *
 * @author Zach Goethel
 */
class Tile(
    /**
     * Tile's texture for rendering
     */
    val texture: Texture,

    /**
     * Whether the tile blocks the player from moving
     */
    val blocking: Boolean,

    val lightBlocking: Boolean = blocking
) : EngineAware()
{
    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var window: Window

    @EngineObject
    private lateinit var world: GameWorld

    /**
     * Renders the tile at the given tile location
     *
     * @param x Tile position index x
     * @param y Tile position index y
     */
    @Deprecated("Rendering is performed by the optimized room renderer")
    fun render(x: Int, y: Int)
    {
        val widthRatio = window.width.toDouble() / window.height
        val tileSize = world.room?.tileSize?.toFloat() ?: 0.2f

        if (x * tileSize - (world.player?.x ?: 0.0) > widthRatio
            || x * tileSize + tileSize - (world.player?.x ?: 0.0) < -widthRatio
            || y * tileSize - (world.player?.y ?: 0.0) - 0.4 > 1.0
            || y * tileSize - (world.player?.y ?: 0.0) - 0.4 + tileSize < -1.0)
            return

        texture.bind()

        renderer.drawRectangle(tileSize * x, tileSize * y, tileSize, tileSize)
    }
}
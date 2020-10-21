package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer
import net.jibini.check.texture.Texture

/**
 * A small section of a game level which can be interacted with
 *
 * @author Zach Goethel
 */
class Tile(
    /**
     * Tile's texture for rendering
     */
    private val texture: Texture,

    /**
     * Whether the tile blocks the player from moving
     */
    val blocking: Boolean = true
) : EngineAware()
{
    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var world: GameWorld

    /**
     * Renders the tile at the given tile location
     *
     * @param x Tile position index x
     * @param y Tile position index y
     */
    fun render(x: Int, y: Int)
    {
        texture.bind()

        val tileSize = world.room?.tileSize?.toFloat() ?: 0.2f

        renderer.drawRectangle(tileSize * x, tileSize * y, tileSize, tileSize)
    }
}
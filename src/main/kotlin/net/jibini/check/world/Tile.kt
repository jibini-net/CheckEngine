package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer
import net.jibini.check.texture.Texture

class Tile(
    private val texture: Texture,

    val blocking: Boolean = true
) : EngineAware()
{
    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var world: GameWorld

    fun render(x: Int, y: Int)
    {
        texture.bind()

        val tileSize = world.room?.tileSize?.toFloat() ?: 0.2f

        renderer.drawRectangle(tileSize * x, tileSize * y, tileSize, tileSize)
    }
}
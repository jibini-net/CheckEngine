package net.jibini.check.world

import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.RenderGroup
import net.jibini.check.graphics.Renderer
import net.jibini.check.graphics.Uniforms
import net.jibini.check.graphics.impl.AbstractAutoDestroyable

/**
 * A collection of tiles in the current game level
 *
 * @author Zach Goethel
 */
class Room(
    /**
     * Two-dimensional tile array width
     */
    val width: Int,

    /**
     * Two-dimensional tile array height
     */
    val height: Int,

    /**
     * Each tile size (for rendering and physics)
     */
    val tileSize: Double = 0.2,

    val isSideScroller: Boolean
) : AbstractAutoDestroyable()
{
    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var uniforms: Uniforms

    /**
     * Two-dimensional tile array initialized to all null tiles
     */
    val tiles = Array(width) { Array<Tile?>(height) { null } }

    private val registeredMeshes = mutableMapOf<Tile, RenderGroup>()

    fun rebuildMeshes()
    {
        val attributed = mutableMapOf<Tile, MutableList<Pair<Int, Int>>>()

        for (x in 0 until width)
            for (y in 0 until height)
            {
                val tile = tiles[x][y] ?: continue
                val attribList = attributed.getOrPut(tile) { mutableListOf() }

                attribList += Pair(x, y)
            }

        for ((tile, tileCoordinates) in attributed)
        {
            val renderList = registeredMeshes.compute(tile)
            { _, group ->
                group?.destroy()
                renderer.beginGroup()
            }

            tile.texture.bind()

            renderer.continueGroup(renderList!!)

            for (coordinate in tileCoordinates)
            {
                renderer.drawRectangle(tileSize.toFloat() * coordinate.first,
                    tileSize.toFloat() * coordinate.second,
                    tileSize.toFloat(), tileSize.toFloat())
            }

            renderer.finalizeGroup()
        }
    }

    fun render()
    {
        for ((tile, list) in registeredMeshes)
        {
            tile.texture.bind()
            uniforms.blocking = tile.blocking

            list.call()
        }
    }

    override fun destroy()
    {
        for ((_, list) in registeredMeshes)
            list.destroy()
    }
}
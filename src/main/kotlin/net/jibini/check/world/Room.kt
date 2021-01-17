package net.jibini.check.world

import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.RenderGroup
import net.jibini.check.graphics.Renderer
import net.jibini.check.graphics.Uniforms
import net.jibini.check.graphics.impl.AbstractAutoDestroyable

/**
 * A collection of tiles in the current game level. Can be either a
 * side-scrolling platformer or a top-down dungeon crawler room.
 *
 * @author Zach Goethel
 */
class Room(
    /**
     * Two-dimensional tile array width.
     */
    val width: Int,

    /**
     * Two-dimensional tile array height.
     */
    val height: Int,

    /**
     * Each tile size (for rendering and physics).
     */
    val tileSize: Double = 0.2,

    /**
     * Set to true if platformer mode and gravity is enabled.
     */
    val isSideScroller: Boolean
) : AbstractAutoDestroyable()
{
    // Required to draw rectangles in render groups
    @EngineObject
    private lateinit var renderer: Renderer

    // Required to set light-blocking flag for blocking tiles
    @EngineObject
    private lateinit var uniforms: Uniforms

    /**
     * Two-dimensional tile array initialized to all null tiles.
     */
    val tiles = Array(width) { Array<Tile?>(height) { null } }

    /**
     * Cache of render groups for each type of tile.
     */
    private val registeredMeshes = mutableMapOf<Tile, RenderGroup>()

    /**
     * Creates new render groups for the tiles in this room. This
     * optimizes the rendering process.
     */
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
                    tileSize.toFloat() + 0.000006f, tileSize.toFloat() + 0.000006f)
            }

            renderer.finalizeGroup()
        }
    }

    /**
     * Renders all render groups of the current room.
     */
    fun render()
    {
        for ((tile, list) in registeredMeshes)
        {
            tile.texture.bind()
            uniforms.blocking = tile.lightBlocking

            list.call()
        }
    }

    override fun destroy()
    {
        for ((_, list) in registeredMeshes)
            list.destroy()
    }
}

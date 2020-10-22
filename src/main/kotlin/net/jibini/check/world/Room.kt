package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.Updatable

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
) : EngineAware(), Updatable
{
    /**
     * Two-dimensional tile array initialized to all null tiles
     */
    val tiles = Array(width) { Array<Tile?>(height) { null } }

    override fun update()
    {
        // Iterate through all tiles
        for (x in 0 until width)
            for (y in 0 until height)
            {
                // Render tile if not null
                tiles[x][y]?.render(x, y)
            }
    }
}
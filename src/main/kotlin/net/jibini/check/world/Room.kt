package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.Updatable

class Room(
    val width: Int,
    val height: Int,

    val tileSize: Double = 0.2
) : EngineAware(), Updatable
{
    val tiles = Array(width) { Array<Tile?>(height) { null } }

    override fun update()
    {
        for (x in 0 until width)
            for (y in 0 until height)
            {
                tiles[x][y]?.render(x, y)
            }
    }
}
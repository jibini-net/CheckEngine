package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Updatable
import net.jibini.check.graphics.Renderer
import net.jibini.check.graphics.impl.TempTexShaderImpl
import net.jibini.check.texture.Texture
import org.lwjgl.opengl.GL11
import kotlin.math.sin

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
    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var tempTexShaderImpl: TempTexShaderImpl

    /**
     * Two-dimensional tile array initialized to all null tiles
     */
    val tiles = Array(width) { Array<Tile?>(height) { null } }

    private val registeredMeshes = mutableMapOf<Tile, Int>()

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
            val renderList = registeredMeshes.getOrPut(tile) { GL11.glGenLists(1) }

            tile.texture.bind()

            GL11.glNewList(renderList, GL11.GL_COMPILE)

            for (coordinate in tileCoordinates)
            {
                renderer.drawRectangle(tileSize.toFloat() * coordinate.first,
                    tileSize.toFloat() * coordinate.second,
                    tileSize.toFloat(), tileSize.toFloat())
            }

            GL11.glEndList()
        }
    }

    override fun update()
    {
//        var i = 0;

        for ((tile, list) in registeredMeshes)
        {
            tile.texture.bind()

            tempTexShaderImpl.updateBlocking(tile.blocking)

//            GL11.glColor3f(sin(i.toFloat() * (2.0 * 3.14159 / 8)).toFloat(),
//                sin(i.toFloat() * (2.0 * 3.14159 / 8) + (2.0 * 3.14159 / 3)).toFloat(),
//                sin(i.toFloat() * (2.0 * 3.14159 / 8) + (4.0 * 3.14159 / 3)).toFloat())

            GL11.glCallList(list)

//            i++
        }
    }
}
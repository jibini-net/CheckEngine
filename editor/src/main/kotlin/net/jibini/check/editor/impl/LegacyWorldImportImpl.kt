package net.jibini.check.editor.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.texture.impl.TextureRegistry
import net.jibini.check.world.GameWorld
import net.jibini.check.world.impl.TileDescriptor
import net.jibini.check.world.impl.TileTexturing
import net.jibini.check.world.impl.WorldFile

import java.lang.IllegalStateException

@RegisterObject
class LegacyWorldImportImpl
{
    // Required to access the texture cache
    @EngineObject
    private lateinit var textureRegistry: TextureRegistry

    // Required to modify and open a new room
    @EngineObject
    private lateinit var gameWorld: GameWorld

    fun import(world: String): WorldFile
    {
        val tiles = gameWorld.loadRoom(world)
            .values
        val descriptors = tiles
            .map()
            {
                TileDescriptor()
                    .apply()
                    {
                        blocksLight = it.lightBlocking
                        blocksPlayer = it.blocking

                        texturing = TileTexturing().apply()
                        {
                            type = "resource"

                            path = try
                            {
                                textureRegistry
                                    .reverseLookup(it.texture)
                                    .split("; ", limit = 2)[1]
                            } catch (ex: IllegalStateException)
                            {
                                "tiles/black.png"
                            }
                        }
                    }
            }

        val result =  WorldFile()
            .apply()
            {
                width = gameWorld.room!!.width
                height = gameWorld.room!!.height

                sideScroller = gameWorld.room!!.isSideScroller

                tileDescriptors = descriptors.toMutableList()
            }

        val tileToDescriptor = tiles
            .zip(descriptors)
            .toMap()

        for (x in gameWorld.room!!.tiles.indices)
        {
            for (y in gameWorld.room!!.tiles[0].indices)
                if (gameWorld.room!!.tiles[x][y] == null)
                    continue
                else
                    tileToDescriptor[gameWorld.room!!.tiles[x][y]]!!
                        .usages
                        .add(intArrayOf(x, y))
        }

        //TODO DEBUG, REMOVE
        WorldFile.writeToFile(result, "example.json")

        gameWorld.reset()

        return result
    }
}
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

        val result =  WorldFile()
            .apply()
            {
                width = gameWorld.room!!.width
                height = gameWorld.room!!.height

                sideScroller = gameWorld.room!!.isSideScroller

                tileDescriptors = tiles
                    .map()
                    {
                        TileDescriptor()
                            .apply()
                            {
                                blocksLight = it.value.lightBlocking
                                blocksPlayer = it.value.blocking

                                texturing = TileTexturing().apply()
                                {
                                    type = "resource"

                                    path = try
                                    {
                                        textureRegistry
                                            .reverseLookup(it.value.texture)
                                            .split("; ", limit = 2)[1]
                                    } catch (ex: IllegalStateException)
                                    {
                                        "tiles/black.png"
                                    }
                                }
                            }
                    }
                    .toMutableList()
            }

        gameWorld.reset()

        return result
    }
}
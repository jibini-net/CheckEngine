package net.jibini.check.world.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import net.jibini.check.world.GameWorld
import net.jibini.check.world.Room
import net.jibini.check.world.Tile

import java.lang.IllegalStateException

@RegisterObject
class WorldFileLoadImpl
{
    @EngineObject
    private lateinit var gameWorld: GameWorld

    fun load(worldFile: WorldFile)
    {
        // Reset the world and create a correctly-sized empty room
        gameWorld.reset()
        gameWorld.room = Room(
            worldFile.width,
            worldFile.height,

            isSideScroller = worldFile.sideScroller
        )

        // Read and set up world tiles in the current room
        worldFile.tileDescriptors
            .forEach()
            { descriptor ->
                // Load the tile texture from the appropriate provider
                val texture = when(descriptor.texturing.type)
                {
                    "resource" -> Texture.load(Resource.fromClasspath(descriptor.texturing.path))
                    "file" -> Texture.load(Resource.fromFile(descriptor.texturing.path))
                    "custom" -> TODO("CUSTOM TEXTURE PROVIDER TYPE NOT YET SUPPORTED")

                    else -> throw IllegalStateException("Illegal texturing type '${descriptor.texturing.type}'")
                }

                // Create the tile instance
                val tile = Tile(
                    texture,

                    blocking = descriptor.blocksPlayer,
                    lightBlocking = descriptor.blocksLight
                )

                // Write the tile instance to all occurrences
                descriptor.usages
                    .forEach()
                    { usage ->
                        gameWorld.room!!.tiles[usage.x][usage.y] = tile
                    }
            }

        // Rebuild room meshes with loaded tiles
        gameWorld.room!!.rebuildMeshes()

        gameWorld.visible = true
    }
}
package net.jibini.check.world.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.entity.EntitySpawner
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.physics.QuadTree
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

    @EngineObject
    private lateinit var lightingShader: LightingShaderImpl

    fun load(worldFile: WorldFile)
    {
        // Reset the world and create a correctly-sized empty room
        gameWorld.reset()
        gameWorld.room = Room(
            worldFile.width,
            worldFile.height,

            isSideScroller = worldFile.sideScroller
        )

        lightingShader.lights.addAll(worldFile.lights)

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
                        gameWorld.room!!.tiles[usage[0]][usage[1]] = tile
                    }
            }

        worldFile.spawnList.forEach {
            EngineObjectsImpl.get<EntitySpawner>()
                .find { element -> element::class.simpleName == it.spawnerName }
                ?.spawn(gameWorld, *it.args.toTypedArray())
        }

        gameWorld.quadTree = QuadTree(
            0.0,
            0.0,
            maxOf(gameWorld.room!!.width, gameWorld.room!!.height) * 0.2,
            maxOf(gameWorld.room!!.width, gameWorld.room!!.height) * 0.2
        )
        for (entity in gameWorld.entities)
            gameWorld.quadTree.place(entity)

        // Rebuild room meshes with loaded tiles
        gameWorld.room!!.rebuildMeshes()
        gameWorld.visible = true
    }
}
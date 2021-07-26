package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Macro
import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.entity.Inventory
import net.jibini.check.entity.Platform
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.entity.character.NonPlayer
import net.jibini.check.graphics.Light
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.physics.BoundingBox
import net.jibini.check.physics.QuadTree
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import net.jibini.check.texture.impl.BitmapTextureImpl

import org.slf4j.LoggerFactory

import java.io.FileNotFoundException
import java.lang.IllegalStateException

import javax.imageio.ImageIO

class LegacyGameWorldPackage : EngineAware()
{
    @EngineObject
    lateinit var lightingShader: LightingShaderImpl
}

/**
 * Loads the given room from the program resources and spawns the
 * entities as described in the level metadata.
 *
 * @param name Level resource folder relative to file location
 *     'worlds/'.
 */
@Deprecated("This room file format has been replaced by the JSON world file")
fun GameWorld.loadRoom(name: String): Map<Int, Tile>
{
    val log = LoggerFactory.getLogger(javaClass)

    reset()

    val roomImageFile = Resource.fromFile("worlds/$name/$name.png").stream
    val roomImage = ImageIO.read(roomImageFile)

    val colors = IntArray(roomImage.width * roomImage.height)
    roomImage.getRGB(0, 0, roomImage.width, roomImage.height, colors, 0, roomImage.width)

    val colorIndices = mutableListOf<Int>()
    for (x in 0 until roomImage.width)
        colorIndices += colors[x]

    val roomMetaFile = Resource.fromFile("worlds/$name/$name.txt").stream
    val roomMetaReader = roomMetaFile.bufferedReader()

    val roomTiles = mutableMapOf<Int, Tile>()

    var isSideScroller = false

    val loadMacros = mutableListOf<Macro>()

    roomMetaReader.forEachLine {
        val split = it.split(" ")

        when (split[0])
        {
            "game_type" ->
            {
                isSideScroller = when (split[1])
                {
                    "top_down" -> false

                    "side_scroller" -> true

                    else -> throw IllegalStateException("Invalid game type entry in meta file '${split[1]}'")
                }
            }

            "run_macro" ->
            {

                val macro = EngineObjectsImpl.get<Macro>()
                    .find { element -> element::class.simpleName == split[1] }

                if (macro == null)
                    log.error("Could not find engine object '${split[1]}'")
                else
                    loadMacros += macro
            }

            "tile" ->
            {
                val index = split[1].toInt()

                val texture = when (split[2])
                {
                    "untextured" -> BitmapTextureImpl(2, 2)

                    else -> Texture.load(Resource.fromClasspath("tiles/${split[2]}"))
                }

                val blocking = when(split[3])
                {
                    "blocking" -> true

                    "nonblocking" -> false

                    "nlblocking" -> true

                    else -> throw IllegalStateException("Invalid blocking entry in meta file '${split[3]}'")
                }

                val nlBlocking = when(split[3])
                {
                    "blocking" -> true

                    "nonblocking" -> false

                    "nlblocking" -> false

                    else -> throw IllegalStateException("Invalid nlblocking entry in meta file '${split[3]}'")
                }

                roomTiles[colorIndices[index]] = Tile(texture, blocking, nlBlocking)
            }

            "spawn" ->
            {
                when (split[1])
                {
                    "player" ->
                    {
                        // TODO SUPPORT PLAYING AS MULTIPLE CHARACTERS
                        if (player == null)
                            player = NonPlayer(
                                Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_stand_right.gif")),
                                Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_stand_left.gif")),

                                Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_walk_right.gif")),
                                Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_walk_left.gif")),

                                split[2],

                                if (split.size >= 6)
                                    split[5]
                                else
                                    "NoBehavior",

                                Inventory(40)
                            )

                        player!!.x = split[3].toDouble() * 0.2
                        player!!.y = split[4].toDouble() * 0.2

                        if (!entities.contains(player!!))
                            entities += player!!
                    }

                    "character" ->
                    {
                        val standRight = Texture.load(
                            Resource.fromClasspath("characters/${split[2]}/${split[2]}_stand_right.gif")
                        )

                        val walkRight = try
                        {
                            Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_walk_right.gif"))
                        } catch (ex: FileNotFoundException)
                        {
                            standRight
                        }

                        val entity = NonPlayer(
                            standRight,

                            try
                            {
                                Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_stand_left.gif"))
                            } catch (ex: FileNotFoundException)
                            {
                                standRight.flip()
                            },

                            walkRight,

                            try
                            {
                                Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_walk_left.gif"))
                            } catch (ex: FileNotFoundException)
                            {
                                walkRight.flip()
                            },

                            split[2],
                            if (split.size >= 6) split[5] else "NoBehavior",

                            Inventory(2)
                        )

                        if (split.size > 5)
                        {
                            val behavior = EngineObjectsImpl.get<EntityBehavior>()
                                .find { element -> element::class.simpleName == split[5] }

                            if (behavior == null)
                                log.error("Could not find engine object '${split[5]}'")
                            else
                                entity.behavior = behavior
                        }

                        entity.x = split[3].toDouble() * 0.2
                        entity.y = split[4].toDouble() * 0.2

                        entities += entity
                    }

                    "entity" ->
                    {
                        when (split[2])
                        {
                            "platform" ->
                            {
                                val behavior =
                                    if (split.size > 6)
                                        EngineObjectsImpl.get<EntityBehavior>()
                                            .find { element -> element::class.simpleName == split[6] }!!
                                    else
                                        null

                                entities += Platform(
                                    split[3].toDouble() * 0.2f,
                                    split[4].toDouble() * 0.2f,

                                    split[5].toDouble() * 0.2f,

                                    behavior
                                )
                            }

                            else -> throw IllegalStateException("Invalid entity type ${split[2]} in world meta file")
                        }
                    }
                }
            }

            "portal" ->
            {
                portals[BoundingBox(
                    split[2].toDouble() * 0.2,
                    split[3].toDouble() * 0.2,

                    split[4].toDouble() * 0.2,
                    split[5].toDouble() * 0.2
                )] = split[1]
            }

            "light" ->
            {
                LegacyGameWorldPackage().lightingShader.lights += Light(
                    split[1].toFloat(),
                    split[2].toFloat(),

                    split[3].toFloat(),
                    split[4].toFloat(),
                    split[5].toFloat()
                )
            }
        }
    }

    roomMetaReader.close()

    room = Room(roomImage.width, roomImage.height - 1, 0.2, isSideScroller)
    quadTree = QuadTree(
        0.0,
        0.0,
        maxOf(room!!.width, room!!.height) * 0.2,
        maxOf(room!!.width, room!!.height) * 0.2
    )
    for (entity in entities)
        quadTree.place(entity)

    for (y in 1 until roomImage.height)
        for (x in 0 until roomImage.width)
        {
            val color = colors[y * roomImage.width + x]

            room!!.tiles[x][room!!.height - y] = roomTiles[color]
        }

    for (macro in loadMacros)
        try
        {
            macro.action()
        } catch (ex: Exception)
        {
            log.error("Failed to run world post-load macro '${macro::class.simpleName}'", ex)
        }

    room!!.rebuildMeshes()

    return roomTiles
}
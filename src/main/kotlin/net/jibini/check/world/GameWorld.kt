package net.jibini.check.world

import net.jibini.check.entity.Entity
import net.jibini.check.entity.character.NonPlayer
import net.jibini.check.entity.character.Player
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable
import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.entity.Platform
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.physics.Bounded
import net.jibini.check.physics.BoundingBox
import net.jibini.check.physics.QuadTree
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import net.jibini.check.texture.impl.BitmapTextureImpl
import org.lwjgl.opengl.GL11
import java.io.FileNotFoundException
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO

/**
 * An engine object which manages the game's current room and entities
 *
 * @author Zach Goethel
 */
@RegisterObject
class GameWorld : Initializable, Updatable
{
    var quadTree = QuadTree<Bounded>(0.0, 0.0, 1.0, 1.0)

    /**
     * Whether the world should be rendered and updated (set to false by default; should be changed to true once the
     * game is initialized and ready to start a level)
     */
    var visible = false
        set(value)
        {
            if (value)
                for (entity in entities)
                    entity.deltaTimer.reset()

            field = value
        }

    /**
     * Entities in the world; can be directly added to by the game
     */
    val entities: MutableList<Entity> = CopyOnWriteArrayList()

    /**
     * Current room to update and render; set to null if no room should be rendered
     */
    var room: Room? = null

    /**
     * A controllable character on which the renderer will center the screen
     */
    var player: Player? = null
        set(value)
        {
            if (value == null)
            {
                if (entities.contains(field))
                    entities.remove(field)
            } else if (!entities.contains(value))
                entities += value

            field = value
        }

    private val portals = ConcurrentHashMap<BoundingBox, String>()

    override fun initialize()
    {
//        thread(isDaemon = true, name = "Quad-tree") {
//            while (true)
//                try
//                {
//                    quadTree.reevaluate()
//                    for (entity in entities)
//                        quadTree.place(entity)
//
//                    Thread.sleep(10)
//                } catch (ex: ConcurrentModificationException)
//                {
//                    ex.printStackTrace()
//                }
//        }
    }

    override fun update()
    {
        if (!visible)
            return

        if (player != null)
            GL11.glTranslatef(-player!!.x.toFloat(), -player!!.y.toFloat() - 0.4f, 0.0f)

        room?.update()

        // Update entities last for transparency
        GL11.glPushMatrix()

        entities.sortByDescending { it.y }

        for (entity in entities)
        {
            // Translate forward to avoid transparency issues
            GL11.glTranslatef(0.0f, 0.0f, 0.02f)

            entity.update()
        }

        GL11.glTranslatef(0.0f, 0.0f, 0.02f)
        quadTree.reevaluate()
        quadTree.render()

        GL11.glPopMatrix()

        for ((box, world) in portals)
            if (box.overlaps(player!!.boundingBox))
            {
                loadRoom(world)

                visible = true
            }
    }

    /**
     * Loads the given room from the program resources and spawns the entities as described in the level metadata
     *
     * @param name Level resource folder relative to classpath location 'tile_sets/'
     */
    fun loadRoom(name: String)
    {
        reset()

        val roomImageFile = Resource.fromClasspath("tile_sets/$name/$name.png").stream
        val roomImage = ImageIO.read(roomImageFile)

        val colors = IntArray(roomImage.width * roomImage.height)
        roomImage.getRGB(0, 0, roomImage.width, roomImage.height, colors, 0, roomImage.width)

        val colorIndices = mutableListOf<Int>()
        for (x in 0 until roomImage.width)
            colorIndices += colors[x]

        val roomMetaFile = Resource.fromClasspath("tile_sets/$name/$name.txt").stream
        val roomMetaReader = roomMetaFile.bufferedReader()

        val roomTiles = mutableMapOf<Int, Tile>()

        var isSideScroller = false

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

                "tile" ->
                {
                    val index = split[1].toInt()

                    val texture = when (split[2])
                    {
                        "untextured" -> BitmapTextureImpl(2, 2)

                        else -> Texture.load(Resource.fromClasspath("tile_sets/$name/${split[2]}"))
                    }

                    val blocking = when(split[3])
                    {
                        "blocking" -> true

                        "nonblocking" -> false

                        else -> throw IllegalStateException("Invalid blocking entry in meta file '${split[3]}'")
                    }

                    roomTiles[colorIndices[index]] = Tile(texture, blocking)
                }

                "spawn" ->
                {
                    when (split[1])
                    {
                        "player" ->
                        {
                            // TODO SUPPORT PLAYING AS MULTIPLE CHARACTERS
                            if (player == null)
                                player = Player(
                                    Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_stand_right.gif")),
                                    Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_stand_left.gif")),

                                    Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_walk_right.gif")),
                                    Texture.load(Resource.fromClasspath("characters/${split[2]}/${split[2]}_walk_left.gif"))
                                )

                            player!!.x = split[3].toDouble() * 0.2
                            player!!.y = split[4].toDouble() * 0.2

                            if (!entities.contains(player))
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
                                }
                            )

                            if (split.size > 5)
                            {
                                val behavior = EngineObjectsImpl.get<EntityBehavior>()
                                    .find { element -> element::class.simpleName == split[5] }!!
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
            }
        }


        roomMetaReader.close()

        room = Room(roomImage.width, roomImage.height - 1, 0.2, isSideScroller)
        quadTree = QuadTree(0.0, 0.0, room!!.width * 0.2, room!!.width * 0.2)
        for (entity in entities)
            quadTree.place(entity)

        for (y in 1 until roomImage.height)
            for (x in 0 until roomImage.width)
            {
                val color = colors[y * roomImage.width + x]

                room!!.tiles[x][room!!.height - y] = roomTiles[color]
            }
    }

    fun reset()
    {
        entities.clear()
        portals.clear()

        visible = false
    }
}
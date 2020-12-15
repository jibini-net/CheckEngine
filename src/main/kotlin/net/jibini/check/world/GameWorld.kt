package net.jibini.check.world

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.jibini.check.engine.*
import net.jibini.check.entity.Entity
import net.jibini.check.entity.character.NonPlayer
import net.jibini.check.entity.character.Player
import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.entity.Platform
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.input.Keyboard
import net.jibini.check.physics.Bounded
import net.jibini.check.physics.BoundingBox
import net.jibini.check.physics.QuadTree
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import net.jibini.check.texture.impl.BitmapTextureImpl
import org.joml.Math
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO
import kotlin.math.abs

/**
 * An engine object which manages the game's current room and entities
 *
 * @author Zach Goethel
 */
@RegisterObject
class GameWorld : Initializable, Updatable
{
    var quadTree = QuadTree<Bounded>(0.0, 0.0, 1.0, 1.0)

    private val log = LoggerFactory.getLogger(javaClass)

    val physicsUpdateLock = Mutex()

    @EngineObject
    private lateinit var keyboard: Keyboard

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
            if (field != null)
            {
                if (value == null)
                {
                    if (entities.contains(field!!))
                        entities.remove(field!!)
                } else if (!entities.contains(value))
                    entities += value
            }

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
//
//                    Thread.sleep(10)
//                } catch (ex: ConcurrentModificationException)
//                {
//                    ex.printStackTrace()
//                }
//        }
    }

    private fun render()
    {
        if (!visible)
            return
        room ?: return

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

        if (keyboard.isPressed(GLFW.GLFW_KEY_F3))
            quadTree.render()

        GL11.glPopMatrix()
    }

    private fun quadTreeResolution()
    {
        quadTree.reevaluate()

        quadTree.iteratePairs {
                a, b ->

            if (a is Entity)
            {
                if ((b !is Entity || b.blocking) && !a.static)
                    a.boundingBox.resolve(b.boundingBox, a.deltaPosition, a)
            }

            if (b is Entity)
            {
                if ((a !is Entity || a.blocking) && !b.static)
                    b.boundingBox.resolve(a.boundingBox, b.deltaPosition, b)
            }
        }
    }

    private fun tileResolution(entity: Entity)
    {
        // Reset delta position aggregation
        entity.deltaPosition.set(0.0, 0.0)

        // Get the current game room; return if null
        val room = room ?: return

        val bB = entity.boundingBox

        // Iterate through each room tile
        for (y in maxOf(0, (bB.y / room.tileSize).toInt() - 1)
                until minOf(room.height, ((bB.y + bB.height) / room.tileSize).toInt() + 1))
            for (x in maxOf(0, (bB.x / room.tileSize).toInt() - 1)
                    until minOf(room.width, ((bB.x + bB.width) / room.tileSize).toInt() + 1))
            {
                // Check if the tile is blocking; default to false
                val blocking = room.tiles[x][y]?.blocking ?: false
                // Ignore tile if it is not blocking
                if (!blocking)
                    continue

                // Resolve the bounding box against each tile
                entity.boundingBox.resolve(
                    BoundingBox(x * room.tileSize + 0.01, y * room.tileSize, room.tileSize - 0.02, room.tileSize),
                    entity.deltaPosition,
                    entity
                )
            }
    }

    private fun portalResolution()
    {
        for ((box, world) in portals)
            if (box.overlaps(player!!.boundingBox))
            {
                loadRoom(world)

                visible = true
            }
    }

    private fun preResetUpdate(entity: Entity)
    {
        // Get delta time since last frame
        val delta = entity.deltaTimer.delta

        // Apply gravity to the velocity
        if (!entity.movementRestrictions.down && room!!.isSideScroller && !entity.static)
            entity.velocity.y -= 9.8 * delta

        // Apply the velocity to the delta position
        entity.deltaPosition.x += entity.velocity.x * delta
        entity.deltaPosition.y += entity.velocity.y * delta

        entity.deltaPosition.x = Math.clamp(-0.07, 0.07, entity.deltaPosition.x)
        entity.deltaPosition.y = Math.clamp(-0.07, 0.07, entity.deltaPosition.y)

        // Apply the delta position to the position
        entity.x += entity.deltaPosition.x
        entity.y += entity.deltaPosition.y

        if (entity.movementRestrictions.down)
        {
            if (entity.velocity.x != 0.0)
                entity.velocity.x -= (abs(entity.velocity.x) / entity.velocity.x) * 4.0 * delta
            if (abs(entity.velocity.x) < 0.05)
                entity.velocity.x = 0.0
        }
    }

    override fun update()
    {
        room ?: return
        render()

        runBlocking {
            physicsUpdateLock.withLock {

                for (entity in entities)
                {
                    preResetUpdate(entity)

                    entity.movementRestrictions.reset()
                    entity.deltaPosition.set(0.0, 0.0)

                    tileResolution(entity)
                }

                quadTreeResolution()
            }
        }

        portalResolution()
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
                                }
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
    }

    fun reset()
    {
        entities.clear()
        portals.clear()

        visible = false
    }
}
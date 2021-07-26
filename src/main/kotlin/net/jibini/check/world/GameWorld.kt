package net.jibini.check.world

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable
import net.jibini.check.entity.Entity
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.graphics.Matrices
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.physics.Bounded
import net.jibini.check.physics.BoundingBox
import net.jibini.check.physics.QuadTree

import org.joml.Math

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

import kotlin.math.abs

/**
 * An engine object which manages the game's current room and entities.
 *
 * @author Zach Goethel
 */
@RegisterObject
class GameWorld : Updatable
{
    /**
     * Quad-tree index of entities in the world.
     */
    var quadTree = QuadTree<Bounded>(0.0, 0.0, 1.0, 1.0)

    // Required to render the room with lighting
    @EngineObject
    private lateinit var lightingShader: LightingShaderImpl

    // Required to modify transformation matrices
    @EngineObject
    private lateinit var matrices: Matrices

    /**
     * Whether the world should be rendered and updated (set to false by
     * default; should be changed to true once the game is initialized
     * and ready to start a level).
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
     * Entities in the world; can be directly added to by the game.
     */
    val entities: MutableList<Entity> = CopyOnWriteArrayList()

    /**
     * Current room to update and render; set to null if no room should
     * be rendered.
     */
    var room: Room? = null

    /**
     * A controllable character on which the renderer will center the
     * screen.
     */
    var player: ActionableEntity? = null
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

    /**
     * A collection of world portals which trigger world loads.
     */
    val portals = ConcurrentHashMap<BoundingBox, String>()

    /**
     * Renders the game world and all of the entities within.
     */
    fun render()
    {
        if (!visible)
            return
        room ?: return

        room?.render()

        // Update entities last for transparency
        matrices.model.pushMatrix()

        entities.sortByDescending { it.y }
        entities.sortByDescending { it.renderBehind }

        for (entity in entities)
        {
            // Translate forward to avoid transparency issues
            matrices.model.translate(0.0f, 0.0f, 0.02f)
            entity.render()
        }

        matrices.model.popMatrix()
    }

    /**
     * Updates the quad-tree index and resolves collisions.
     */
    private fun quadTreeResolution()
    {
        quadTree.reevaluate()

        quadTree.iteratePairs {
                a, b ->

            if (a != b && a is Entity)
            {
                if (b is Entity && a.boundingBox.overlaps(b.boundingBox))
                {
                    a.behavior?.entityCollideFrame(a, b)
                    b.behavior?.entityCollideFrame(b, a)
                }

                if ((b !is Entity || b.blocking) && !a.static)
                    a.boundingBox.resolve(b.boundingBox, a.deltaPosition, a)
            }

            if (a != b && b is Entity)
            {
                if (a is Entity && a.boundingBox.overlaps(b.boundingBox))
                {
                    a.behavior?.entityCollideFrame(a, b)
                    b.behavior?.entityCollideFrame(b, a)
                }

                if ((a !is Entity || a.blocking) && !b.static)
                    b.boundingBox.resolve(a.boundingBox, b.deltaPosition, b)
            }
        }
    }

    /**
     * Resolves collisions with static world tiles.
     */
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

    /**
     * Detects collisions with portals in the world.
     */
    private fun portalResolution()
    {
        for ((box, world) in portals)
            if (box.overlaps(player!!.boundingBox))
            {
                loadRoom(world)

                visible = true
            }
    }

    /**
     * Updates physics, position, and gravity for entities.
     */
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

        // Friction.
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

        lightingShader.perform { render() }

        for (entity in entities)
            entity.update()

        for (entity in entities)
        {
            preResetUpdate(entity)

            entity.movementRestrictions.reset()
            entity.deltaPosition.set(0.0, 0.0)

            tileResolution(entity)
        }

        quadTreeResolution()

        portalResolution()
    }



    /**
     * Removes all entities, portals, lights, and world data.
     */
    fun reset()
    {
        entities.clear()
        portals.clear()

        visible = false

        lightingShader.lights.clear()

        player = null
    }
}
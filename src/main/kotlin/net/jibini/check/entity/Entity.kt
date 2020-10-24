package net.jibini.check.entity

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.graphics.Renderer
import net.jibini.check.physics.BoundingBox
import net.jibini.check.world.GameWorld
import org.joml.Math.clamp
import org.joml.Vector2d

/**
 * A dynamic being with position and velocity; affected by gravity
 *
 * @author Zach Goethel
 */
abstract class Entity(
    /**
     * Entity's x position; aligned with horizontal center of entity
     */
    var x: Double = 0.0,

    /**
     * Entity's y position; aligned with vertical bottom of entity
     */
    var y: Double = 0.0,

    /**
     * Entity's velocity vector; initialized to <0.0, 0.0> on instantiation
     */
    val velocity: Vector2d = Vector2d()
) : EngineAware()
{
    @EngineObject
    protected lateinit var renderer: Renderer

    @EngineObject
    protected lateinit var gameWorld: GameWorld

    val movementRestrictions = MovementRestrictions()

    /**
     * Delta timer to keep track of physics and movement timing
     */
    val deltaTimer = DeltaTimer()

    /**
     * Aggregate per-frame movement which can be added to by all sub-classes
     */
    protected val deltaPosition = Vector2d()

    open fun update()
    {
        // Get the current game room; return if null
        val room = gameWorld.room ?: return
        // Get delta time since last frame
        val delta = deltaTimer.delta

        // Apply gravity to the velocity
        if (!movementRestrictions.down && room.isSideScroller)
            velocity.y -= 9.8 * delta

        // Apply the velocity to the delta position
        deltaPosition.x += velocity.x * delta
        deltaPosition.y += velocity.y * delta

        deltaPosition.x = clamp(-0.07, 0.07, deltaPosition.x)
        deltaPosition.y = clamp(-0.07, 0.07, deltaPosition.y)

        // Apply the delta position to the position
        x += deltaPosition.x
        y += deltaPosition.y
        // Default to not-on-ground state
        movementRestrictions.reset()

        // Iterate through each room tile
        for (y in maxOf(0, (y / room.tileSize).toInt() - 2)
                until minOf(room.height, (y / room.tileSize).toInt() + 3))
            for (x in maxOf(0, (x / room.tileSize).toInt() - 2)
                    until minOf(room.width, (x / room.tileSize).toInt() + 3))
            {
                // Check if the tile is blocking; default to false
                val blocking = room.tiles[x][y]?.blocking ?: false
                // Ignore tile if it is not blocking
                if (!blocking)
                    continue

                // Resolve the bounding box against each tile
                boundingBox.resolve(
                    BoundingBox(x * room.tileSize + 0.01, y * room.tileSize, room.tileSize - 0.02, room.tileSize),
                    deltaPosition,
                    this
                )
            }

        // Reset delta position aggregation
        deltaPosition.x = 0.0
        deltaPosition.y = 0.0
    }

    /**
     * The bounding box of the entity, which should take into account the current coordinates of the entity
     */
    abstract val boundingBox: BoundingBox

    class MovementRestrictions
    {
        var left: Boolean = false

        var right: Boolean = false

        var up: Boolean = false

        var down: Boolean = false

        fun reset()
        {
            left = false
            right = false

            up = false;
            down = false;
        }
    }
}
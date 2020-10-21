package net.jibini.check.character

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.graphics.Renderer
import net.jibini.check.physics.BoundingBox
import net.jibini.check.world.GameWorld
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

    /**
     * Delta timer to keep track of physics and movement timing
     */
    private val deltaTimer = DeltaTimer()

    /**
     * Aggregate per-frame movement which can be added to by all sub-classes
     */
    protected val deltaPosition = Vector2d()

    /**
     * Updated per-frame for the next frame; whether the entity is grounded
     */
    protected var onGround: Boolean = false

    open fun update()
    {
        // Get the current game room; return if null
        val room = gameWorld.room ?: return
        // Get delta time since last frame
        val delta = deltaTimer.delta

        // Apply gravity to the velocity
        velocity.y -= 9.8 * delta

        // Apply the velocity to the delta position
        deltaPosition.x += velocity.x * delta
        deltaPosition.y += velocity.y * delta

        // Apply the delta position to the position
        x += deltaPosition.x
        y += deltaPosition.y
        // Default to not-on-ground state
        onGround = false

        // Iterate through each room tile
        for (y in 0 until room.height)
            for (x in 0 until room.width)
            {
                // Check if the tile is blocking; default to false
                val blocking = room.tiles[x][y]?.blocking ?: false
                // Ignore tile if it is not blocking
                if (!blocking)
                    continue

                if (
                    // Resolve the bounding box against each tile
                    boundingBox.resolve(
                        BoundingBox(x * room.tileSize, y * room.tileSize, room.tileSize, room.tileSize),
                        deltaPosition,
                        this
                    )
                )
                    // If at least one tile returns true, the entity is on the ground (see #resolve method info)
                    onGround = true
            }

        // Reset delta position aggregation
        deltaPosition.x = 0.0
        deltaPosition.y = 0.0
    }

    /**
     * The bounding box of the entity, which should take into account the current coordinates of the entity
     */
    abstract val boundingBox: BoundingBox
}
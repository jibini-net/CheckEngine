package net.jibini.check.entity

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.graphics.Renderer
import net.jibini.check.physics.Bounded
import net.jibini.check.world.GameWorld
import org.joml.Vector2d

/**
 * A dynamic being with position and velocity; may affected by gravity
 * unless the entity is static. Will serve as a platform or wall if
 * blocking.
 *
 * @author Zach Goethel
 */
abstract class Entity(
    /**
     * Entity's x-position; aligned with horizontal center of entity.
     */
    var x: Double = 0.0,

    /**
     * Entity's y-position; aligned with vertical bottom of entity.
     */
    var y: Double = 0.0,

    /**
     * Entity's velocity vector; initialized to zeros.
     */
    val velocity: Vector2d = Vector2d()
) : EngineAware(), Bounded
{
    open var behavior: EntityBehavior? = null

    val entityId = nextId++

    @EngineObject
    protected lateinit var renderer: Renderer

    @EngineObject
    protected lateinit var gameWorld: GameWorld

    val movementRestrictions = MovementRestrictions()

    /**
     * Delta timer to keep track of physics and movement timing
     */
    val deltaTimer = DeltaTimer()

    open val blocking = false
    open val static = false

    /**
     * Aggregate per-frame movement which can be added to by all sub-classes
     */
    val deltaPosition = Vector2d()

    open fun update()
    {
        behavior?.update(this)
    }

    abstract fun render()

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

            up = false
            down = false
        }
    }

    companion object
    {
        private var nextId = 0
    }
}
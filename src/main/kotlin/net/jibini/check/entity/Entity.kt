package net.jibini.check.entity

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.graphics.Renderer
import net.jibini.check.graphics.Uniforms
import net.jibini.check.graphics.Matrices
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
    /**
     * The current behavior of the entity. See the [EntityBehavior]
     * class for more information about how each entity's behavior is
     * updated each frame.
     */
    open var behavior: EntityBehavior? = null

    /**
     * A unique identifier is assigned to each entity. This identifier
     * currently has no known uses.
     */
    val entityId = nextId++

    // Required to render entities; shared with all subclasses
    @EngineObject
    protected lateinit var renderer: Renderer

    // Required to access the world tiles and entities; shared with
    // all subclasses
    @EngineObject
    protected lateinit var gameWorld: GameWorld

    /**
     * A collection of directional data stating which directions the
     * entity can or cannot physically move.
     */
    val movementRestrictions = MovementRestrictions()

    /**
     * Delta timer to keep track of physics and movement timing.
     */
    val deltaTimer = DeltaTimer()

    /**
     * Whether this entity will block other entities, such as a
     * [moving platform][Platform].
     *
     * _Blocking entities should also be [static]. Dynamic blocking
     * entities may cause unexpected behavior in the physics engine._
     */
    open val blocking = false

    /**
     * Set this flag to true to render before the player.
     */
    open var renderBehind = false;

    /**
     * When this flag is enabled, gravity and physics will not affect
     * the position and velocity of this object. The entity will sit
     * still.
     *
     * _If this entity is also [blocking], it will still affect other
     * entities._
     */
    open val static = false

    /**
     * Accumulated per-frame movement. This can be modified and added to
     * by multiple behaviors and physics updates.
     */
    val deltaPosition = Vector2d()

    open fun update()
    {
        behavior?.update(this)
    }

    /**
     * Uses the entity's rendering object to render the entity.
     *
     * This class should specify via the [Uniforms] and [Matrices] game
     * objects which transformations and rendering settings should be
     * used. Entities should use them to specify whether they block
     * light, and what uniform color and transparency they should be.
     *
     * _Remember to push and pop the transformation stack. Return
     * uniforms to their original states._
     */
    abstract fun render()

    /**
     * A collection of directional data stating which directions the
     * entity can or cannot physically move.
     */
    class MovementRestrictions
    {
        /**
         * This flag is set to true if the entity is blocked in the left
         * direction.
         */
        var left: Boolean = false

        /**
         * This flag is set to true if the entity is blocked in the
         * right direction.
         */
        var right: Boolean = false

        /**
         * This flag is set to true if the entity is blocked in the
         * upwards direction.
         */
        var up: Boolean = false

        /**
         * This flag is set to true if the entity is blocked in the
         * downwards direction.
         */
        var down: Boolean = false

        /**
         * Resets all of the movement flags to their original states.
         */
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
        /**
         * Keeps track of how many entity IDs have been assigned so far.
         */
        private var nextId = 0
    }
}
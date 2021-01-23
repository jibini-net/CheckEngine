package net.jibini.check.entity.behavior

import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Entity

import org.joml.Vector2d

import java.util.Random

import kotlin.math.cos
import kotlin.math.sin

/**
 * An example entity behavior which allows characters to wander
 * aimlessly around a top-down level.
 *
 * @author Zach Goethel
 */
@RegisterObject
class WanderingBehavior : EntityBehavior()
{
    /**
     * Track the time between wandering logic updates.
     */
    private val deltaTimer = DeltaTimer(false)

    /**
     * Used to generate random movement frequencies.
     */
    private val random = Random()

    /**
     * Keep a map of each entity's movement stateful data.
     */
    private val states = mutableMapOf<ActionableEntity, EntityMovementTrack>()

    override fun update(entity: Entity)
    {
        // Only applies to entities which can walk
        if (entity !is ActionableEntity)
            return

        // Get the stateful data of the entity
        val entityState = states.getOrPut(entity) { EntityMovementTrack() }
        // Update the walking if currently walking
        if (entityState.moving)
            entity.walk(entityState.movement.x / 3, entityState.movement.y / 3)

        // Check if the next movement should start
        if (deltaTimer.delta >= entityState.changeAt)
        {
            // Toggle the moving flag
            entityState.moving = !entityState.moving

            // Generate a random direction to walk
            val angle = Math.toRadians(random.nextInt(360).toDouble())
            // and derive x- and y-scalars
            entityState.movement.x = cos(angle)
            entityState.movement.y = sin(angle)

            // Cancel movement in directions which don't make sense
            if (entity.movementRestrictions.up)
                // Don't move up if character can't move up
                entityState.movement.y = minOf(0.0, entityState.movement.y)
            if (entity.movementRestrictions.down)
                // Don't move down if character can't move down
                entityState.movement.y = maxOf(0.0, entityState.movement.y)

            if (entity.movementRestrictions.left)
                // Don't move left if character can't move left
                entityState.movement.x = maxOf(0.0, entityState.movement.x)
            if (entity.movementRestrictions.right)
                // Don't move right if character can't move right
                entityState.movement.x = minOf(0.0, entityState.movement.x)

            // Schedule the next time to walk again
            entityState.changeAt = deltaTimer.delta + (random.nextInt(40) + 24).toDouble() / 40
        }
    }

    /**
     * Tracks a character's stateful movement information.
     */
    private class EntityMovementTrack
    {
        /**
         * Whether the character is currently walking.
         */
        var moving: Boolean = true

        /**
         * The direction the character is moving.
         */
        var movement = Vector2d()

        /**
         * The next time at which the character should walk.
         */
        var changeAt: Double = 0.0
    }
}
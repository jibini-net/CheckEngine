package net.jibini.check.entity.behavior

import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Entity
import org.joml.Vector2d
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@RegisterObject
class WanderingBehavior : EntityBehavior()
{
    private val deltaTimer = DeltaTimer(false)
    private val random = Random()

    private val states = mutableMapOf<ActionableEntity, EntityMovementTrack>()

    override fun update(entity: Entity)
    {
        if (entity !is ActionableEntity)
            return

        val entityState = states.getOrPut(entity) { EntityMovementTrack() }

        if (entityState.moving)
            entity.walk(entityState.movement.x / 3, entityState.movement.y / 3)

        if (deltaTimer.delta >= entityState.changeAt)
        {
            entityState.moving = !entityState.moving

            val angle = Math.toRadians(random.nextInt(360).toDouble())

            entityState.movement.x = cos(angle)
            entityState.movement.y = sin(angle)

            if (entity.movementRestrictions.up)
                entityState.movement.y = minOf(0.0, entityState.movement.y)
            if (entity.movementRestrictions.down)
                entityState.movement.y = maxOf(0.0, entityState.movement.y)

            if (entity.movementRestrictions.left)
                entityState.movement.x = maxOf(0.0, entityState.movement.x)
            if (entity.movementRestrictions.right)
                entityState.movement.x = minOf(0.0, entityState.movement.x)

            entityState.changeAt = deltaTimer.delta + (random.nextInt(40) + 24).toDouble() / 40
        }
    }

    private class EntityMovementTrack
    {
        var moving: Boolean = true

        var movement = Vector2d()

        var changeAt: Double = 0.0
    }
}
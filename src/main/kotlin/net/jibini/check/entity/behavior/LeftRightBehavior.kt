package net.jibini.check.entity.behavior

import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.Entity
import kotlin.math.abs

@RegisterObject
class LeftRightBehavior : EntityBehavior()
{
    override fun update(entity: Entity)
    {
        if (entity.movementRestrictions.left)
            entity.velocity.x = abs(entity.velocity.x)
        else if (entity.movementRestrictions.right)
            entity.velocity.x = -abs(entity.velocity.x)
    }
}
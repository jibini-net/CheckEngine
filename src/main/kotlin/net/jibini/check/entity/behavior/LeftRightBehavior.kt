package net.jibini.check.entity.behavior

import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.Entity
import kotlin.math.abs

/**
 * The entity behavior which causes platforms to bounce left and right.
 *
 * @author Zach Goethel
 */
@RegisterObject
class LeftRightBehavior : EntityBehavior()
{
    override fun update(entity: Entity)
    {
        // Set velocity to positive when left collision is detected
        if (entity.movementRestrictions.left)
            entity.velocity.x = abs(entity.velocity.x)
        // Set velocity to negative when right collision is detected
        else if (entity.movementRestrictions.right)
            entity.velocity.x = -abs(entity.velocity.x)
    }
}
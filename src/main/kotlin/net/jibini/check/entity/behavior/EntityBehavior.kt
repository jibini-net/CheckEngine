package net.jibini.check.entity.behavior

import net.jibini.check.entity.ActionableEntity

abstract class EntityBehavior
{
    abstract fun update(entity: ActionableEntity)
}
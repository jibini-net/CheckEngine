package net.jibini.check.entity.behavior

import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.Entity

/**
 * Classes implementing this interface are the global instances of an
 * entity's artificial intelligence. The behavior instance is shared by
 * all entities which have the behavior (there are not multiple behavior
 * instances). The behavior is updated each frame.
 *
 * Classes implementing this interface must be [registered as an engine
 * object][RegisterObject].
 *
 * @author Zach Goethel
 */
abstract class EntityBehavior
{
    /**
     * Updates the behavior and movement of the given entity according
     * to the instructions of this behavior.
     *
     * @param entity Entity to update with this behavior.
     */
    abstract fun update(entity: Entity)
}
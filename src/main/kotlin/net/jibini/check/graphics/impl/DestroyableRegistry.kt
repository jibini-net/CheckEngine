package net.jibini.check.graphics.impl

import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Destroyable

import org.slf4j.LoggerFactory

/**
 * Tracks all objects which should be destroyed upon exiting the game.
 * Objects will be disposed in the order they are created (first in,
 * first out).
 *
 * @author Zach Goethel
 */
//TODO RUNTIME DESTROYABLE ALLOCATION AND DESTRUCTION STACK
@RegisterObject
class DestroyableRegistry
{
    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * A collection of all destroyable objects which have been
     * registered. [Auto-destroyable][AbstractAutoDestroyable] objects
     * will automatically be added to this list.
     */
    val registered = mutableListOf<Destroyable>()

    /**
     * Deletes all registered objects for the current context.
     */
    fun flushRegistered()
    {
        log.info("Flushing cached destroyable objects for this thread")

        // Destroy all registered items safely
        for (item in registered)
            try
            {
                item.destroy()
            } catch (ex: Exception)
            {
                log.error("Cannot destroy registered destroyable", ex)
            }

        // Remove references to the objects
        registered.clear()
    }
}
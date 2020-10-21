package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Destroyable
import org.slf4j.LoggerFactory

/**
 * A destroyable object which will automatically be destroyed upon exiting the game
 *
 * @author Zach Goethel
 */
@Suppress("LeakingThis")
abstract class AbstractAutoDestroyable : Destroyable
{
    init
    {
        // Register self to be auto-destroyed
        forThread += this

        log.debug("Registered destroyable object $this")
    }

    companion object
    {
        private val log = LoggerFactory.getLogger(AbstractAutoDestroyable::class.java)

        /**
         * Per-thread collections of destroyable objects
         */
        private val destroyable = mutableMapOf<Thread, MutableList<Destroyable>>()

        /**
         * Retrieve the current thread's registered destroyable objects
         */
        val forThread: MutableList<Destroyable>
            // Check current thread on every reference
            get() = destroyable.getOrPut(Thread.currentThread(), { mutableListOf() })

        /**
         * Deletes all registered objects for the current context
         */
        fun flushRegistered()
        {
            log.info("Flushing cached destroyable objects for this thread")

            // Destroy all registered items safely
            for (item in forThread)
                try
                {
                    item.destroy()
                } catch (ex: Exception)
                {
                    log.error("Cannot destroy registered destroyable", ex)
                }

            destroyable.remove(Thread.currentThread())
        }
    }
}
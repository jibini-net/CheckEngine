package net.jibini.check.graphics.impl

import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Destroyable
import org.slf4j.LoggerFactory

@RegisterObject
class DestroyableRegistry
{
    private val log = LoggerFactory.getLogger(javaClass)

    val registered = mutableListOf<Destroyable>()

    /**
     * Deletes all registered objects for the current context
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

        registered.clear()
    }
}
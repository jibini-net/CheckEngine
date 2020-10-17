package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Destroyable
import org.slf4j.LoggerFactory

@Suppress("LeakingThis")
abstract class AbstractAutoDestroyable : Destroyable
{
    init
    {
        forThread += this

        log.debug("Registered destroyable object $this")
    }

    companion object
    {
        private val log = LoggerFactory.getLogger(AbstractAutoDestroyable::class.java)

        private val destroyable = mutableMapOf<Thread, MutableList<Destroyable>>()

        val forThread: MutableList<Destroyable>
            get() = destroyable.getOrPut(Thread.currentThread(), { mutableListOf() })

        fun flushRegistered()
        {
            log.info("Flushing cached destroyable objects for this thread")

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
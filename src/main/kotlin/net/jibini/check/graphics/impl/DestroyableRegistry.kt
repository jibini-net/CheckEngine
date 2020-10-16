package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Destroyable
import org.slf4j.LoggerFactory

object DestroyableRegistry
{
    private val log = LoggerFactory.getLogger(javaClass)

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
package net.jibini.check.graphics

import net.jibini.check.graphics.impl.DestroyableRegistry
import org.slf4j.LoggerFactory

@Suppress("LeakingThis")
abstract class Pointer<T : Number> : Destroyable
{
    private val log = LoggerFactory.getLogger(javaClass)

    init
    {
        log.debug("Registered destroyable object $this")

        DestroyableRegistry.forThread += this
    }

    abstract val pointer: T
}
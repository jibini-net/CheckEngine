package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Destroyable
import org.slf4j.LoggerFactory

/**
 * A destroyable object which will automatically be destroyed upon exiting the game
 *
 * @author Zach Goethel
 */
@Suppress("LeakingThis")
abstract class AbstractAutoDestroyable : EngineAware(), Destroyable
{
    private val log = LoggerFactory.getLogger(javaClass)

    @EngineObject
    private lateinit var destroyableRegistry: DestroyableRegistry

    init
    {
        // Register self to be auto-destroyed
        destroyableRegistry.registered += this

        log.debug("Registered destroyable object $this")
    }
}
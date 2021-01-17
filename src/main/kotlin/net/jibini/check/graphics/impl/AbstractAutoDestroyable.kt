package net.jibini.check.graphics.impl

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Destroyable
import org.slf4j.LoggerFactory

/**
 * A destroyable object which will automatically be destroyed upon
 * exiting the game. Objects will be disposed in the order they are
 * created (first in, first out).
 *
 * @author Zach Goethel
 */
//TODO RUNTIME DESTROYABLE ALLOCATION AND DESTRUCTION STACK
@Suppress("LeakingThis")
abstract class AbstractAutoDestroyable : EngineAware(), Destroyable
{
    private val log = LoggerFactory.getLogger(this::class.java)

    // Required in order for destroyable objects to register themselves upon construction
    @EngineObject
    private lateinit var destroyableRegistry: DestroyableRegistry

    init
    {
        // Register self to be auto-destroyed
        destroyableRegistry.registered += this

        log.debug("Registered destroyable object of type '${this::class.simpleName}'")
    }
}
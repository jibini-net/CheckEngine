package net.jibini.check.engine

import net.jibini.check.engine.impl.EngineObjectsImpl

/**
 * An object whose fields will be scanned for annotations to evaluate engine objects; has access to engine objects
 *
 * @author Zach Goethel
 */
@Suppress("LeakingThis")
abstract class EngineAware
{
    init
    {
        // Call engine object implementation upon construction
        EngineObjectsImpl.placeAll(this)
    }
}
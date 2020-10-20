package net.jibini.check.engine

import net.jibini.check.engine.impl.EngineObjectsImpl

@Suppress("LeakingThis")
abstract class EngineAware
{
    init
    {
        EngineObjectsImpl.placeAll(this)
    }
}
package net.jibini.check.engine.timing

import net.jibini.check.engine.RegisterObject

@RegisterObject
class GlobalDeltaSync
{
    private val deltaTimer = DeltaTimer(false)

    var delta: Double = 0.0

    fun globalAutoUpdate()
    {
        delta = deltaTimer.delta
        deltaTimer.reset()
    }
}
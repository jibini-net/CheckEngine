package net.jibini.check.engine.timing

import net.jibini.check.engine.RegisterObject

/**
 * Ensures that all auto-resetting [timers][DeltaTimer] reset in sync
 * with each other. The sync is [performed][globalAutoUpdate] once per
 * frame.
 *
 * @author Zach Goethel
 */
@RegisterObject
class GlobalDeltaSync
{
    /**
     * The global timer on which to base all child timers.
     */
    private val deltaTimer = DeltaTimer(false)

    /**
     * Delta time result of all synced auto-resetting timers.
     */
    var delta: Double = 0.0

    /**
     * Updates the internal timer, thus updating all auto-resetting
     * timers on a global scope.
     */
    fun globalAutoUpdate()
    {
        delta = deltaTimer.delta
        deltaTimer.reset()
    }
}
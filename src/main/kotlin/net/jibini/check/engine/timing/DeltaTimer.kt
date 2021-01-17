package net.jibini.check.engine.timing

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject

/**
 * Tracks fractional seconds between calls to calculate delta times.
 * These differential time values can be used to calculate physics,
 * animation frame timing, and game events.
 *
 * @author Zach Goethel
 */
class DeltaTimer(
    /**
     * If set to true, the timer will automatically be reset every time
     * delta is retrieved.
     *
     * More specifically, auto-resetting timers will be reset
     * [globally synced][GlobalDeltaSync] each frame. This is a non-
     * transparent workaround to avoid stuttering and inconsistent times
     * over a frame's render time.
     */
    private val autoReset: Boolean = true
) : EngineAware()
{
    companion object
    {
        /**
         * Upon setting to true, this global flag will cause all
         * auto-resetting timers to always return zero second deltas.
         *
         * Used to pause physics and animation while UI is open.
         */
        var globalFreeze = false
    }

    /**
     * Last time (in nanoseconds) at which the delta was updated. This
     * time is updated when the timer is [reset].
     */
    private var last: Long = System.nanoTime()

    // Required to synchronize all auto-resetting timers
    @EngineObject
    private lateinit var globalDeltaSync: GlobalDeltaSync

    /**
     * Gets the time in fractional seconds since the timer was last
     * reset; if auto-reset is enabled, this delta time will be in
     * relation to the the previous frame.
     *
     * Previously, accessing this field would reset the timer. For more
     * information about this change, see [autoReset].
     */
    val delta: Double
        get()
        {
            // Resort to the global delta sync if auto-resetting
            if (autoReset)
                return globalDeltaSync.delta

            // Get current time and compare to previous reset
            val current = System.nanoTime()
            val difference = current - last

            // Divide by one billion to get seconds (zero if frozen)
            return if (globalFreeze)
                0.0
            else
                difference.toDouble() / 1000000000.0
        }

    /**
     * Resets the timer such that all future delta times are in relation
     * to the current time.
     */
    fun reset()
    {
        // Store current time
        last = System.nanoTime()
    }
}
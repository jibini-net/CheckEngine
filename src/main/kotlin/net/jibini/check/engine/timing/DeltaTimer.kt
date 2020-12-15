package net.jibini.check.engine.timing

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject

/**
 * Tracks fractional seconds between calls to get delta times
 *
 * @author Zach Goethel
 */
class DeltaTimer(
    /**
     * If set to true, the timer will automatically be reset every time delta is retrieved
     */
    private val autoReset: Boolean = true
) : EngineAware()
{
    companion object
    {
        var globalFreeze = false
    }

    /**
     * Last time in nanoseconds when delta was calculated
     */
    private var last: Long = System.nanoTime()

    private var current: Long = System.nanoTime()

    @EngineObject
    private lateinit var globalDeltaSync: GlobalDeltaSync

    /**
     * Gets the time in seconds since the timer was last reset; if auto-reset is enabled, access to this property also
     * resets the timer
     */
    val delta: Double
        get()
        {
            if (autoReset)
                return globalDeltaSync.delta


            // Get current time and compare to previous reset
            val current = System.nanoTime()
            val difference = current - last

            // Reset if applicable

            // Divide by one billion to get seconds
            return if (globalFreeze)
                0.0
            else
                difference.toDouble() / 1000000000.0
        }

    /**
     * Resets the timer so that all future delta times are in relation to the current time
     */
    fun reset()
    {
        current = System.nanoTime()

        // Store current time
        last = current
    }
}
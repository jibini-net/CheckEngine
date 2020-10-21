package net.jibini.check.engine.timing

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
)
{
    /**
     * Last time in nanoseconds when delta was calculated
     */
    private var last: Long = System.nanoTime()

    /**
     * Gets the time in seconds since the timer was last reset; if auto-reset is enabled, access to this property also
     * resets the timer
     */
    val delta: Double
        get()
        {
            // Get current time and compare to previous reset
            val current = System.nanoTime()
            val difference = current - last

            // Reset if applicable
            if (autoReset)
                reset()

            // Divide by one billion to get seconds
            return difference.toDouble() / 1000000000.0
        }

    /**
     * Resets the timer so that all future delta times are in relation to the current time
     */
    fun reset()
    {
        // Store current time
        last = System.nanoTime()
    }
}
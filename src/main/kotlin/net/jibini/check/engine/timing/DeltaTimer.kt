package net.jibini.check.engine.timing

class DeltaTimer(
    private val autoReset: Boolean = true
)
{
    private var last: Long = System.nanoTime()

    val delta: Double
        get()
        {
            val current = System.nanoTime()
            val difference = current - last

            if (autoReset)
                reset()

            return difference.toDouble() / 1000000000.0
        }

    fun reset()
    {
        last = System.nanoTime()
    }
}
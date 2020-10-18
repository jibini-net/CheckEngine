package net.jibini.check.engine.timing

class DeltaTimer
{
    private var last: Long = System.nanoTime()

    val delta: Double
        get()
        {
            val current = System.nanoTime()
            val difference = current - last

            last = current

            return difference.toDouble() / 1000000000.0
        }
}
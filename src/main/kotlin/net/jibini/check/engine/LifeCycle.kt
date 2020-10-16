package net.jibini.check.engine

import org.slf4j.LoggerFactory

class LifeCycle
{
    private val log = LoggerFactory.getLogger(javaClass)

    private val tasks = mutableListOf<Runnable>()

    fun registerTask(task: Runnable)
    {
        tasks += task

        log.debug("Registered cycle task at end-of-progression index ${tasks.size}")
    }

    fun registerTask(task: Runnable, index: Int)
    {
        tasks.add(index, task)

        log.debug("Registered cycle task in progression order ${index + 1} of ${tasks.size}")
    }

    fun start(predicate: () -> Boolean)
    {
        while (predicate())
        {
            if (tasks.isEmpty())
                Thread.sleep(10)
            else
                for (task in tasks)
                    task.run()

            Thread.yield()
        }

        log.debug("Loop predicate failed; loop has exited")
    }
}
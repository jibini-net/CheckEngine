package net.jibini.check.engine

import org.slf4j.LoggerFactory

class LifeCycle
{
    private val log = LoggerFactory.getLogger(javaClass)

    private val tasks = mutableListOf<Runnable>()

    fun registerTask(task: Runnable)
    {
        // Add a new task to the end of the list
        tasks += task

        log.debug("Registered cycle task at end-of-progression index ${tasks.size}")
    }

    fun registerTask(task: Runnable, index: Int)
    {
        // Insert a task in the list at index
        tasks.add(index, task)

        log.debug("Registered cycle task in progression order ${index + 1} of ${tasks.size}")
    }

    fun start(predicate: () -> Boolean)
    {
        // Loop until the given predicate returns false
        while (predicate())
        {
            if (tasks.isEmpty())
                // Sleep to avoid 100% CPU usage
                Thread.sleep(10)
            else
                // Run all tasks
                for (task in tasks)
                    task.run()

            Thread.yield()
        }

        log.debug("Loop predicate failed; loop has exited")
    }
}
package net.jibini.check.engine

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock

import net.jibini.check.Check

import org.slf4j.LoggerFactory

/**
 * Collects and executes lifecycle tasks to be executed in a looped
 * fashion during the runtime of the application.
 *
 * @author Zach Goethel
 */
class LifeCycle
{
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Ordered collection of update tasks.
     */
    private val tasks = mutableListOf<Runnable>()

    /**
     * Adds the given task to the collection of update tasks.
     *
     * @param task Task to register at the end-index.
     */
    fun registerTask(task: Runnable)
    {
        // Add a new task to the end of the list
        tasks += task

        log.debug("Registered cycle task at end-of-progression index ${tasks.size}")
    }

    /**
     * Adds the given task to the collection of update tasks.
     *
     * @param task Task to register at the given index.
     * @param index Index at which to register the task.
     */
    fun registerTask(task: Runnable, index: Int)
    {
        // Insert a task in the list at index
        tasks.add(index, task)

        log.debug("Registered cycle task in progression order ${index + 1} of ${tasks.size}")
    }

    /**
     * Infinitely loop and call all update tasks in order until the
     * predicate returns false.
     *
     * @param predicate The life-cycle will execute until this predicate
     *     returns false.
     */
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
                runBlocking {
                    Check.pollMutex.withLock {
                        for (task in tasks)
                            task.run()
                    }
                }

            Thread.yield()
        }

        log.debug("Loop predicate failed; loop has exited")
    }
}
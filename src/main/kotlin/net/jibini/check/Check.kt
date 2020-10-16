package net.jibini.check

import net.jibini.check.engine.EngineObjects
import net.jibini.check.engine.FeatureSet
import net.jibini.check.engine.LifeCycle
import net.jibini.check.graphics.Window
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

/**
 * Game engine entry point factory
 */
object Check
{
    private val log = LoggerFactory.getLogger(javaClass)

    // Tracks if any contexts have already been created
    private var contextInit = false
    private var contextInitThread: Thread? = null

    // Tracks duplicate instances of one game type
    private val instanceCount = mutableMapOf<String, Int>()

    @JvmStatic
    fun boot(game: CheckGame)
    {
        log.info("Booting application '${game.profile.appName}' version ${game.profile.appVersion} . . .")

        if (contextInit)
        {
            if (Thread.currentThread() != contextInitThread)
                // Warn about GLFW's required thread safety protocol
                log.warn("Applications should all be booted from the same thread; on some systems, it may also be necessary for that" +
                            " thread to be the main thread  (https://www.glfw.org/docs/3.3.2/intro_guide.html#thread_safety)")
        } else
        {
            contextInitThread = Thread.currentThread()
            contextInit = true

            GLFW.glfwInit()
        }

        val postfix =
            if (instanceCount.containsKey(game.profile.appName))
            {
                instanceCount[game.profile.appName] = (instanceCount[game.profile.appName]!! + 1)

                " <${instanceCount[game.profile.appName]}>"
            } else
            {
                instanceCount[game.profile.appName] = 1

                ""
            }

        val window = Window(game.profile)
        EngineObjects.placeInstance(window, game)

        val featureSet = FeatureSet()
        EngineObjects.placeInstance(featureSet, game)

        GLFW.glfwMakeContextCurrent(0L)

        thread(name = "${game.profile.appName}$postfix") {
            log.debug("Branched application main engine thread")

            window.makeCurrent()
            GL.createCapabilities()

            val lifeCycle = LifeCycle()
            EngineObjects.placeInstance(lifeCycle, game)

            game.start()

            lifeCycle.registerTask({
                GL11.glClear(featureSet.clearFlags)
                GL11.glLoadIdentity()
            }, 0)

            lifeCycle.registerTask { window.swapBuffers() }

            lifeCycle.start { !window.shouldClose }

            log.debug("Breaking application engine thread")

            window.destroy()

            instanceCount[game.profile.appName] = instanceCount[game.profile.appName]!! - 1
            if (instanceCount[game.profile.appName] == 0)
                instanceCount.remove(game.profile.appName)

            log.debug("Removed application instance; orphaned and released")
        }
    }

    @JvmStatic
    fun infinitelyPoll()
    {
        log.debug("Entering infinite main thread polling . . .")
        while (instanceCount.isNotEmpty())
            GLFW.glfwWaitEventsTimeout(0.1)

        log.debug("Exited infinite polling; no instances remain")
    }
}
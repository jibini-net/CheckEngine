package net.jibini.check.input

import net.jibini.check.graphics.Window
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Wrapped GLFW keyboard functions linked to a window and context
 *
 * @author Zach Goethel
 */
class Keyboard(
    /**
     * Related window on the same context
     */
    window: Window
)
{
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Cached key states (GLFW does not provide this information)
     */
    private val keyStates = ConcurrentHashMap<Int, Boolean>()

    /**
     * Registered per-key listeners
     */
    private val keyListeners = ConcurrentHashMap<Int, MutableList<Runnable>>()

    init
    {
        // Set a key listener for all keys to keep track of states
        GLFW.glfwSetKeyCallback(window.pointer) {
                _: Long, key: Int, _: Int, action: Int, _: Int ->

            // Update key state in cache
            keyStates[key] = when(action)
            {
                GLFW.GLFW_PRESS   -> true
                GLFW.GLFW_RELEASE -> false

                else -> keyStates[key] ?: false
            }

            // Check for registered listeners for this key
            if (action == GLFW.GLFW_PRESS)
                if (keyListeners.containsKey(key))
                {
                    for (runnable in keyListeners[key]!!)
                        try
                        {
                            // Trigger listener
                            runnable.run()
                        } catch (ex: Exception)
                        {
                            log.error("An exception occurred in a key-press callback", ex)
                        }
                }
        }
    }

    /**
     * Checks if the given key index is currently pressed
     *
     * @param key GLFW key index (use GLFW constants)
     */
    fun isPressed(key: Int) = keyStates[key] ?: false

    /**
     * Adds a listener which will be invoked when the key is pressed
     *
     * @param key GLFW key index (use GLFW constants)
     * @param runnable Runnable which will be invoked upon key-press
     */
    fun addKeyListener(key: Int, runnable: Runnable)
    {
        val listeners = keyListeners.getOrPut(key) { Collections.synchronizedList(mutableListOf()) }

        listeners += runnable
    }

    /**
     * Deletes all key-listeners on the keyboard
     */
    fun clearKeyListeners()
    {
        keyListeners.clear()
    }

    /**
     * Deletes all key-listeners on the keyboard for the specified key
     *
     * @param key GLFW key index (use GLFW constants)
     */
    fun clearKeyListeners(key: Int)
    {
        keyListeners.remove(key)
    }
}
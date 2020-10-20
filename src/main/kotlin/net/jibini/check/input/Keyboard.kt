package net.jibini.check.input

import net.jibini.check.graphics.Window
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Keyboard(
    window: Window
)
{
    private val log = LoggerFactory.getLogger(javaClass)

    private val keyStates = ConcurrentHashMap<Int, Boolean>()
    private val keyListeners = ConcurrentHashMap<Int, MutableList<Runnable>>()

    init
    {
        GLFW.glfwSetKeyCallback(window.pointer) {
                _: Long, key: Int, _: Int, action: Int, _: Int ->

            keyStates[key] = when(action)
            {
                GLFW.GLFW_PRESS   -> true
                GLFW.GLFW_RELEASE -> false

                else -> keyStates[key] ?: false
            }

            if (action == GLFW.GLFW_PRESS)
                if (keyListeners.containsKey(key))
                {
                    for (runnable in keyListeners[key]!!)
                        try
                        {
                            runnable.run()
                        } catch (ex: Exception)
                        {
                            log.error("An exception occurred in a key-press callback", ex)
                        }
                }
        }
    }

    fun isPressed(key: Int) = keyStates[key] ?: false

    fun addKeyListener(key: Int, runnable: Runnable)
    {
        val listeners = keyListeners.getOrPut(key) { Collections.synchronizedList(mutableListOf()) }

        listeners += runnable
    }

    fun clearKeyListeners()
    {
        keyListeners.clear()
    }

    fun clearKeyListeners(key: Int)
    {
        keyListeners.remove(key)
    }
}
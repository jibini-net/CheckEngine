package net.jibini.check.graphics

import net.jibini.check.CheckGame
import org.lwjgl.glfw.GLFW

/**
 * Wrapped GLFW window instance which handles the OpenGL context
 *
 * @author Zach Goethel
 */
class Window(
    /**
     * Game creation data for title and context information
     */
    profile: CheckGame.Profile
) : Pointer<Long>, Destroyable
{
    override val pointer: Long

    /**
     * Whether the window has been manually closed
     */
    private var close = false

    private var internalWidth = 1366;
    private var internalHeight = 1000;

    var width: Int
        get() = internalWidth;
        set(value)
        {
            GLFW.glfwSetWindowSize(pointer,value, height)

            internalWidth = value
        }

    var height: Int
        get() = internalHeight;
        set(value)
        {
            GLFW.glfwSetWindowSize(pointer, width, value)

            internalHeight = value
        }

    /**
     * Converts booleans to ones and zeros
     */
    private fun Boolean.toGLFWValue(): Int
    {
        // Convert boolean to int (0/1)
        return if (this)
            GLFW.GLFW_TRUE
        else
            GLFW.GLFW_FALSE
    }

    init
    {
        GLFW.glfwDefaultWindowHints()

        // Set MSAA samples to 16x
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 16)
        // Set OpenGL context version
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, profile.contextVersion / 10)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, profile.contextVersion % 10)
        // Set profile settings
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, profile.contextForwardCompat.toGLFWValue())
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_CORE_PROFILE, profile.contextCore.toGLFWValue())

        // Create window with some defaults
        pointer = GLFW.glfwCreateWindow(
            width, height,
            "${profile.appName} ${profile.appVersion}",
            0L, 0L
        )

        // Make context current in thread
        this.makeCurrent()

        GLFW.glfwSetWindowSizeCallback(pointer) {
                _: Long, w: Int, h: Int ->

            internalWidth = w
            internalHeight = h
        }
    }

    /**
     * Is the window destroyed?
     */
    private var destroyed = false

    override fun destroy()
    {
        // Destroy if not already
        if (!destroyed)
            GLFW.glfwDestroyWindow(pointer)

        destroyed = true
    }

    /**
     * Makes the window's OpenGL context current in the current thread
     */
    fun makeCurrent()
    {
        GLFW.glfwMakeContextCurrent(pointer)
    }

    /**
     * Swaps the window's buffers; context must be current
     */
    fun swapBuffers()
    {
        GLFW.glfwSwapBuffers(pointer)
    }

    /**
     * Manually tells the window to close on the next frame
     */
    fun close()
    {
        // Set close flag to close on next loop
        this.close = true
    }

    /**
     * Checks whether the window should close or has been requested to close manually
     */
    val shouldClose: Boolean
        // Check on each reference
        get() = GLFW.glfwWindowShouldClose(pointer) || close
}
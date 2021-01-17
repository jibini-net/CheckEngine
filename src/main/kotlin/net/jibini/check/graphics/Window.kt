package net.jibini.check.graphics

import net.jibini.check.CheckGame
import org.lwjgl.glfw.GLFW
import java.io.File

/**
 * Wrapped GLFW window instance which handles the OpenGL context and
 * windowing information.
 *
 * @author Zach Goethel
 */
//TODO FULLSCREEN
class Window(
    /**
     * Game creation data for title and context information.
     */
    profile: CheckGame.Profile
) : Pointer<Long>, Destroyable
{
    override val pointer: Long

    /**
     * Whether the window has been manually closed.
     */
    private var close = false

    // Staring size of the window
    private var internalWidth = 1280;
    private var internalHeight = 800;

    /**
     * Width of the window in pixels. Setting this field will resize the
     * window.
     */
    var width: Int
        get() = internalWidth;
        set(value)
        {
            //TODO ASSESS THREAD-SAFETY
            GLFW.glfwSetWindowSize(pointer,value, height)

            internalWidth = value
        }

    /**
     * Height of the window in pixels. Setting this field will resize the
     * window.
     */
    var height: Int
        get() = internalHeight;
        set(value)
        {
            //TODO ASSESS THREAD-SAFETY
            GLFW.glfwSetWindowSize(pointer, width, value)

            internalHeight = value
        }

    init
    {
        GLFW.glfwDefaultWindowHints()

        if (File("opengl_es").exists())
        {
            // Configure OpenGL ES context
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API)

            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0)

            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_ANY_PROFILE)
        } else
        {
            // Configure OpenGL Core context
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4)
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)

            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        }

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
     * Makes the window's OpenGL context current in the current thread.
     */
    fun makeCurrent()
    {
        GLFW.glfwMakeContextCurrent(pointer)
    }

    /**
     * Swaps the window's buffers; context must be current.
     */
    fun swapBuffers()
    {
        GLFW.glfwSwapBuffers(pointer)
    }

    /**
     * Manually tells the window to close on the next frame.
     */
    fun close()
    {
        // Set close flag to close on next loop
        this.close = true
    }

    /**
     * Checks whether the window should close or has been requested to
     * close manually.
     */
    val shouldClose: Boolean
        // Check on each reference
        get() = GLFW.glfwWindowShouldClose(pointer) || close
}

package net.jibini.check.graphics

import net.jibini.check.CheckGame
import org.lwjgl.glfw.GLFW

class Window(
    profile: CheckGame.Profile
) : Pointer<Long>, Destroyable
{
    override val pointer: Long

    private var close = false

    private fun Boolean.toGLFWValue(): Int
    {
        return if (this)
            GLFW.GLFW_TRUE
        else
            GLFW.GLFW_FALSE
    }

    init
    {
        GLFW.glfwDefaultWindowHints()

        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 16)

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, profile.contextVersion / 10)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, profile.contextVersion % 10)

        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, profile.contextForwardCompat.toGLFWValue())
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_CORE_PROFILE, profile.contextCore.toGLFWValue())

        pointer = GLFW.glfwCreateWindow(
            768, 768,
            "${profile.appName} ${profile.appVersion}",
            0L, 0L
        )

        this.makeCurrent()
    }

    private var destroyed = false

    override fun destroy()
    {
        if (!destroyed)
            GLFW.glfwDestroyWindow(pointer)

        destroyed = true
    }

    fun makeCurrent()
    {
        GLFW.glfwMakeContextCurrent(pointer)
    }

    fun swapBuffers()
    {
        GLFW.glfwSwapBuffers(pointer)
    }

    fun close()
    {
        this.close = true
    }

    val shouldClose: Boolean
        get() = GLFW.glfwWindowShouldClose(pointer) || close
}
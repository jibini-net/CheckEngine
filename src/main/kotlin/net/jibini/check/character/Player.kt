package net.jibini.check.character

import net.jibini.check.engine.EngineObject
import net.jibini.check.input.Keyboard
import net.jibini.check.texture.Texture
import org.lwjgl.glfw.GLFW

class Player(
    idleRight: Texture,
    idleLeft: Texture = idleRight.flip(),

    walkRight: Texture,
    walkLeft: Texture = idleRight.flip()
) : Humanoid(idleRight, idleLeft, walkRight, walkLeft)
{
    @EngineObject
    private lateinit var keyboard: Keyboard

    override fun update()
    {
        super.update()

        val w = keyboard.isPressed(GLFW.GLFW_KEY_W)
        val a = keyboard.isPressed(GLFW.GLFW_KEY_A)
        val s = keyboard.isPressed(GLFW.GLFW_KEY_S)
        val d = keyboard.isPressed(GLFW.GLFW_KEY_D)

        val x = (if (a) -1 else 0) + (if (d) 1 else 0).toDouble()
        val y = (if (s) -1 else 0) + (if (w) 1 else 0).toDouble()

        this.walk(x, y)
    }
}
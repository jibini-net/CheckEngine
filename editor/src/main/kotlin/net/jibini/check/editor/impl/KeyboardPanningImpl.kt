package net.jibini.check.editor.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.input.Keyboard

import org.joml.Vector2f

import org.lwjgl.glfw.GLFW

/**
 * Allows panning around the current editing level using WASD keyboard
 * controls. Uses the [lighting][LightingShaderImpl] engine's fallback
 * [translation][LightingShaderImpl.translation] to translate the world
 * in screen-space.
 *
 * @author Zach Goethel
 */
@RegisterObject
class KeyboardPanningImpl : Updatable
{
    // Required to access the panning controls
    @EngineObject
    private lateinit var keyboard: Keyboard

    // Required to modify world panning translation
    @EngineObject
    private lateinit var lightingShader: LightingShaderImpl

    private val deltaTimer = DeltaTimer(false)

    override fun update()
    {
        // Poll the WASD keys
        val w = keyboard.isPressed(GLFW.GLFW_KEY_W)
        val a = keyboard.isPressed(GLFW.GLFW_KEY_A)
        val s = keyboard.isPressed(GLFW.GLFW_KEY_S)
        val d = keyboard.isPressed(GLFW.GLFW_KEY_D)

        // Translate the world accordingly
        lightingShader.translation.add(
            Vector2f(
                when { d && a -> 0.0f; d -> 1.0f; a -> -1.0f; else -> 0.0f },
                when { w && s -> 0.0f; w -> 1.0f; s -> -1.0f; else -> 0.0f }
            ).mul(deltaTimer.delta.toFloat())
        )

        deltaTimer.reset()
    }
}
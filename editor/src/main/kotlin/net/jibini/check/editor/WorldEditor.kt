package net.jibini.check.editor

import net.jibini.check.Check
import net.jibini.check.CheckGame
import net.jibini.check.engine.EngineObject
import net.jibini.check.entity.Entity
import net.jibini.check.entity.character.PlayerBehavior
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.input.Keyboard
import net.jibini.check.world.impl.WorldFile
import net.jibini.check.world.impl.WorldFileLoadImpl

import org.lwjgl.glfw.GLFW

import org.slf4j.LoggerFactory

fun main()
{
    Entity.frozen = true

    Check.boot(WorldEditor())
    Check.infinitelyPoll()
}

class WorldEditor : CheckGame
{
    private val log = LoggerFactory.getLogger(this::class.java)

    // Required to modify framebuffer pixels-per-tile
    @EngineObject
    private lateinit var lightingShader: LightingShaderImpl

    // Required to load world files into the game world
    @EngineObject
    private lateinit var worldFileLoad: WorldFileLoadImpl

    @EngineObject
    private lateinit var playerBehavior: PlayerBehavior

    @EngineObject
    private lateinit var keyboard: Keyboard

    var current: WorldFile? = null
        set(value)
        {
            field = value

            log.info("A new world file descriptor has been set; updating . . .")
            onCurrentChanged()
        }

    private var queueToggle = false

    override fun initialize()
    {
        lightingShader.framebufferPixelsPerTile = 16

        keyboard.addKeyListener(GLFW.GLFW_KEY_R) { queueToggle = true }
    }

    override fun update()
    {
        if (queueToggle && current != null)
        {
            queueToggle = false

            Entity.frozen = !Entity.frozen
            current = current
        }
    }

    private fun onCurrentChanged()
    {
        if (current != null)
        {
            worldFileLoad.load(current!!)
            lightingShader.translation.zero()
        }
    }

    override val profile = CheckGame.Profile(
        "Check World Editor",
        "1.0.0-SNAPSHOT"
    )
}
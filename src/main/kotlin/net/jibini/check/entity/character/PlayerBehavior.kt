package net.jibini.check.entity.character

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Entity
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.input.Keyboard
import net.jibini.check.resource.Resource
import net.jibini.check.world.GameWorld
import net.jibini.check.world.impl.WorldFile
import net.jibini.check.world.impl.WorldFileLoadImpl

import org.lwjgl.glfw.GLFW

import org.slf4j.LoggerFactory

@RegisterObject
class PlayerBehavior : EntityBehavior(), Initializable
{
    private val log = LoggerFactory.getLogger(javaClass)

    @EngineObject
    private lateinit var worldFileLoad: WorldFileLoadImpl

    @EngineObject
    private lateinit var keyboard: Keyboard

    @EngineObject
    private lateinit var gameWorld: GameWorld

    /**
     * Flag set to true if the jump key has been pressed since the last
     * frame (asynchronous callback on the main thread).
     */
    private var queueJump = false

    /**
     * Flag set to true if the attack key has been pressed.
     */
    private var queueAttack = false
    private var queueSecondary = false

    override fun initialize()
    {
        // Listen to space to trigger attack
        keyboard.addKeyListener(GLFW.GLFW_KEY_LEFT_CONTROL) { queueAttack = true }
        keyboard.addKeyListener(GLFW.GLFW_KEY_Z) { queueSecondary = true }
        // Listen to left shift to trigger jump
        keyboard.addKeyListener(GLFW.GLFW_KEY_SPACE) { queueJump = true }
    }

    override fun prepare(entity: Entity)
    {
        if (entity !is ActionableEntity) return
        gameWorld.player = entity
    }

    override fun update(entity: Entity)
    {
        if (entity !is ActionableEntity) return

        // Jump if the jump flag is set to true
        if (queueJump)
        {
            queueJump = false
            entity.jump(0.55)
        }

        // Attack if the attack flag is set
        if (queueAttack)
        {
            queueAttack = false
            entity.attack()
        }

        // Secondary attack if that flag is set
        if (queueSecondary)
        {
            queueSecondary = false
            entity.attack(primary = false)
        }

        // Check WASD keys
        val w = keyboard.isPressed(GLFW.GLFW_KEY_W) && !(gameWorld.room?.isSideScroller ?: false)
        val a = keyboard.isPressed(GLFW.GLFW_KEY_A)
        val s = keyboard.isPressed(GLFW.GLFW_KEY_S) && !(gameWorld.room?.isSideScroller ?: false)
        val d = keyboard.isPressed(GLFW.GLFW_KEY_D)

        // Calculate x/y movement based on key input
        val x = (if (a) -1 else 0) + (if (d) 1 else 0).toDouble()
        val y = (if (s) -1 else 0) + (if (w) 1 else 0).toDouble()

        // Walk based on previous movement
        entity.walk(x, y)
    }

    override fun onDeath(main: Entity, killer: Entity?)
    {
        //TODO FANCIER DEATHS
        worldFileLoad.load(WorldFile.read(Resource.fromFile("worlds/lobby/lobby.json")))
        gameWorld.visible = true

        main.y = 0.0

        log.info("The player was killed; killer is set to '$killer'")
    }
}
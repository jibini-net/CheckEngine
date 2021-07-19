package net.jibini.check.entity.character

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Entity
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.input.Keyboard
import net.jibini.check.world.GameWorld

import org.lwjgl.glfw.GLFW

@RegisterObject
class PlayerBehavior : EntityBehavior(), Initializable
{
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

    override fun initialize()
    {
        // Listen to space to trigger attack
        keyboard.addKeyListener(GLFW.GLFW_KEY_LEFT_CONTROL) { queueAttack = true }
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

        // Jump if the jump flag is set
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

        if (entity.y < -0.4)
        {
            //TODO FANCIER DEATH
            gameWorld.loadRoom("main_hub")
            gameWorld.visible = true

            entity.y = 0.0
        }
    }
}
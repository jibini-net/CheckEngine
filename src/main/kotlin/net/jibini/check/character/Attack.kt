package net.jibini.check.character

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.Updatable
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.texture.Texture
import net.jibini.check.texture.impl.AnimatedTextureImpl

class Attack(
    private val textureRight: Texture,
    private val textureLeft: Texture,

    private val animationTime: Double = 0.5,
    private val coolDown: Double = 0.35,
    private val restartAnimationOnReTrigger: Boolean = false,

    var damageAmount: Double = 1.0
) : EngineAware(), Updatable
{
    private val attackTimer = DeltaTimer(false)

    private var triggered: Humanoid? = null

    override fun update()
    {
        if (triggered == null)
            return

        triggered?.attackMovementModifier = 1.0

        if (attackTimer.delta < animationTime)
        {
            when (triggered?.characterState ?: Humanoid.RIGHT)
            {
                Humanoid.RIGHT -> textureRight.bind()

                Humanoid.LEFT -> textureLeft.bind()
            }

            triggered?.attackMovementModifier = 0.5
        }
    }

    fun trigger(triggered: Humanoid)
    {
        if (attackTimer.delta > coolDown)
        {
            if (attackTimer.delta > animationTime || restartAnimationOnReTrigger)
            {
                if (textureLeft is AnimatedTextureImpl)
                    textureLeft.currentFrameIndex = 0
                if (textureRight is AnimatedTextureImpl)
                    textureRight.currentFrameIndex = 0
            }

            attackTimer.reset();
        }

        this.triggered = triggered
    }
}
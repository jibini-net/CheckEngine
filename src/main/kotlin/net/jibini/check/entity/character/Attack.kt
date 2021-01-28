package net.jibini.check.entity.character

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.Updatable
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.texture.Texture
import net.jibini.check.texture.impl.AnimatedTextureImpl

/**
 * An action completed by a game character which has an associated
 * animation, cool-down time, and stats; can be performed by a player or
 * non-player character.
 *
 * @author Zach Goethel
 */
class Attack(
    /**
     * Animation to display during the attack when the character's state
     * is right-facing.
     */
    private val textureRight: Texture,

    /**
     * Animation to display during the attack when the character's state
     * is left-facing.
     */
    private val textureLeft: Texture,

    /**
     * How long to display the animation starting when the attack is
     * triggered.
     */
    private val animationTime: Double = 0.5,

    /**
     * The time after the animation is triggered until the attack can be
     * re-triggered.
     */
    private val coolDown: Double = 0.35,

    /**
     * If true, restart the animation every time it is triggered; if
     * false, only re-trigger if animation time is passed and the attack
     * is re-triggered.
     */
    private val restartAnimationOnReTrigger: Boolean = false,

    /**
     * Amount of damage dealt by this attack when it affects an entity.
     */
    //TODO IMPLEMENT DAMAGE AND HEALTH, OR MAKE EXTERNAL
    var damageAmount: Double = 1.0,

    /**
     * Movement speed modifier while the attack animation is in progress
     * (1.0 is full speed, 0.5 is half, and so on).
     */
    var movementModifier: Double = 0.5
) : EngineAware(), Updatable
{
    /**
     * Non-resetting timer reset every time the attack is triggered.
     */
    private val attackTimer = DeltaTimer(false)

    /**
     * Attacker which triggered the attack; null if no attack has been
     * triggered.
     */
    private var triggered: ActionableEntity? = null

    /**
     * Movement modifier of the attack; 1.0 if the attack is not
     * triggered, the value of the movement modifier if the attack
     * animation is in process.
     */
    var effectiveMovementModifier: Double = 1.0

    var onTrigger: Runnable? = null
    var onTriggeredUpdate: Runnable? = null

    override fun update()
    {
        // Only update if the attacker isn't null
        if (triggered == null)
            return
        // By default, do not affect movement speed
        effectiveMovementModifier = 1.0

        // If animation is not complete . . .
        if (attackTimer.delta < animationTime)
        {
            // Check which direction texture to bind; default to right-facing
            when (triggered?.characterState ?: ActionableEntity.RIGHT)
            {
                ActionableEntity.RIGHT -> textureRight.bind()

                ActionableEntity.LEFT -> textureLeft.bind()
            }

            // Change movement speed modifier
            effectiveMovementModifier = movementModifier

            onTriggeredUpdate?.run()
        }
    }

    /**
     * Triggers the attack animation and resets the cool-down timer;
     * only has an effect if the cool-down time is elapsed or there is
     * no in-progress attack.
     *
     * @param triggered The attacker (used to access directional state).
     */
    fun trigger(triggered: ActionableEntity)
    {
        // If cool-down time has elapsed, consider re-triggering
        if (attackTimer.delta > coolDown)
        {
            // Restart animation if animation time is elapsed OR the "always restart animation" is true
            if (attackTimer.delta > animationTime || restartAnimationOnReTrigger)
            {
                if (textureLeft is AnimatedTextureImpl)
                    textureLeft.currentFrameIndex = 0
                if (textureRight is AnimatedTextureImpl)
                    textureRight.currentFrameIndex = 0
            }

            // Re-trigger animation time
            attackTimer.reset();

            onTrigger?.run()
        }

        // Store the character so the correct textures can be bound
        this.triggered = triggered
    }
}
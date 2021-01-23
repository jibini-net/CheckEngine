package net.jibini.check.entity.behavior

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.ActionableEntity.Companion.LEFT
import net.jibini.check.entity.ActionableEntity.Companion.RIGHT
import net.jibini.check.entity.Entity
import net.jibini.check.world.GameWorld

import kotlin.random.Random

/**
 * An example entity behavior which causes the affected entity to jump
 * repeatedly at random heights.
 *
 * @author Zach Goethel
 */
@RegisterObject
class JumpingBehavior : EntityBehavior()
{
    // Required to know the player's position (facing direction)
    @EngineObject
    private lateinit var gameWorld: GameWorld

    override fun update(entity: Entity)
    {
        // Only applies to entities which can jump
        if (entity !is ActionableEntity)
            return

        // Face towards the player's location
        if (gameWorld.player?.x ?: 0.0 < entity.x)
            entity.characterState = LEFT
        else if (gameWorld.player?.x ?: 0.0 > entity.x)
            entity.characterState = RIGHT
        // Update the render texture accordingly
        entity.renderTexture = entity.textures[0][entity.characterState]

        // Jump!
        entity.jump((Random.nextInt(20) + 12).toDouble() / 64)
    }
}
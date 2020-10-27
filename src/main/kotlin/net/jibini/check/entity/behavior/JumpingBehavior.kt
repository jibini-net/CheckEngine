package net.jibini.check.entity.behavior

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.ActionableEntity.Companion.LEFT
import net.jibini.check.entity.ActionableEntity.Companion.RIGHT
import net.jibini.check.entity.Entity
import net.jibini.check.world.GameWorld
import kotlin.random.Random

@RegisterObject
class JumpingBehavior : EntityBehavior()
{
    @EngineObject
    private lateinit var gameWorld: GameWorld

    override fun update(entity: Entity)
    {
        if (entity !is ActionableEntity)
            return

        if (gameWorld.player?.x ?: 0.0 < entity.x)
            entity.characterState = LEFT
        else if (gameWorld.player?.x ?: 0.0 > entity.x)
            entity.characterState = RIGHT

        entity.renderTexture = entity.textures[0][entity.characterState]

        entity.jump((Random.nextInt(20) + 12).toDouble() / 64)
    }
}
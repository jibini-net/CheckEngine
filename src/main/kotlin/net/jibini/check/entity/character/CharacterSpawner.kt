package net.jibini.check.entity.character

import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.entity.EntitySpawner
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import net.jibini.check.world.GameWorld

import org.slf4j.LoggerFactory

import java.io.FileNotFoundException

/**
 * Entity spawner for creating behavior-based actionable entities within the
 * game world.
 *
 * @author Zach Goethel
 */
@RegisterObject
class CharacterSpawner : EntitySpawner
{
    private val log = LoggerFactory.getLogger(javaClass)

    override fun spawn(gameWorld: GameWorld, vararg args: Any?)
    {
        val x = args[0] as Double
        val y = args[1] as Double

        val characterName = args[2] as String
        val behavior = args[3] as String

        val standRight = Texture.load(
            Resource.fromClasspath("characters/$characterName/${characterName}_stand_right.gif")
        )

        val walkRight = try
        {
            Texture.load(Resource.fromClasspath("characters/$characterName/${characterName}_walk_right.gif"))
        } catch (ex: FileNotFoundException)
        {
            standRight
        }

        val entity = NonPlayer(
            standRight,

            try
            {
                Texture.load(Resource.fromClasspath("characters/$characterName/${characterName}_stand_left.gif"))
            } catch (ex: FileNotFoundException)
            {
                standRight.flip()
            },

            walkRight,

            try
            {
                Texture.load(Resource.fromClasspath("characters/$characterName/${characterName}_walk_left.gif"))
            } catch (ex: FileNotFoundException)
            {
                walkRight.flip()
            },

            characterName,
            behavior
        )

        val behaviorLoaded = EngineObjectsImpl.get<EntityBehavior>()
            .find { element -> element::class.simpleName == behavior }

        if (behaviorLoaded == null)
            log.error("Could not find engine object '$behavior'")
        else
        {
            entity.behavior = behaviorLoaded
            entity.behavior!!.prepare(entity)
        }

        entity.x = x
        entity.y = y

        if (!gameWorld.entities.contains(entity))
            gameWorld.entities += entity
    }
}
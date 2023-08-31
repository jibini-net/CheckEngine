package net.jibini.check

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Entity
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.entity.behavior.PlayerTargetBehavior
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.resource.Resource.Companion.fromClasspath
import net.jibini.check.texture.Texture
import net.jibini.check.texture.Texture.Companion.load
import org.joml.Vector3f
import java.util.*

@RegisterObject
class GendreauBehavior : EntityBehavior(), Initializable
{
    // Yelling textures.  A very important element of this behavior.
    private var leftYell: Texture? = null
    private var rightYell: Texture? = null

    @EngineObject
    private var playerTarget: PlayerTargetBehavior? = null

    @EngineObject
    private var lightingShader: LightingShaderImpl? = null

    /**
     * Local random for randomizing the jump height.
     */
    private val random = Random()
    override fun update(entity: Entity)
    {
        playerTarget!!.update(entity)
        (entity as ActionableEntity).jump(random.nextDouble() / 2 + 0.5)
        // Update the entity's yelling texture
        if ((playerTarget as PlayerTargetBehavior).target!!.x <= entity.x) entity.renderTexture = leftYell!! else entity.renderTexture = rightYell!!
        val redLight = lightingShader!!.lights[0]
        redLight.x = entity.x.toFloat() / 0.2f
        redLight.y = (entity.y + entity.falseYOffset + 0.2f).toFloat() / 0.2f
        val color = Vector3f(redLight.r, redLight.g, redLight.b)
        color.normalize().mul(entity.falseYOffset.toFloat() * 2.0f + 0.05f)
        redLight.r = color.x
        redLight.g = color.y
        redLight.b = color.z
    }

    override fun initialize()
    {
        // Load the yelling texture
        rightYell = load(fromClasspath("characters/gendreau/gendreau_yell_right.gif"))
        leftYell = rightYell!!.flip(true, false)
    }
}
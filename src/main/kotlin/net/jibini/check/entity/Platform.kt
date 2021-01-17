package net.jibini.check.entity

import net.jibini.check.engine.EngineObject
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.entity.behavior.LeftRightBehavior
import net.jibini.check.graphics.Uniforms
import net.jibini.check.physics.BoundingBox
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture

/**
 * A static and blocking entity which represents an in-game moving
 * platform. Use this entity with a [behavior][LeftRightBehavior] to
 * make it move around.
 *
 * @author Zach Goethel
 */
class Platform(
    /**
     * The starting x-position of the platform.
     */
    x: Double,

    /**
     * The starting y-position of the platform.
     */
    y: Double,

    /**
     * Width of the platform in screen units.
     */
    private val width: Double,

    /**
     * Starting behavior assigned to this platform.
     */
    override var behavior: EntityBehavior?
) : Entity(x = x, y = y)
{
    init
    {
        // Start moving to the right
        velocity.x = 0.5
    }

    // Required to set this entity as light-blocking
    @EngineObject
    private lateinit var uniforms: Uniforms

    // The central texture of the platform, which is tiled over the
    // width of the platform
    private val texture = Texture.load(Resource.fromClasspath("entities/platform.png"))
    // The left and right terminating caps of the platform
    private val textureLeft = Texture.load(Resource.fromClasspath("entities/platform_left.png"))
    private val textureRight = textureLeft.flip()

    // A platform should block and support the player
    override val blocking = true

    // A platform should be static and unaffected by physics; the
    // behavior will still affect the platform's position
    override val static = true

    // The platform is the specified width and half of a tile high
    override val boundingBox: BoundingBox
        get() = BoundingBox(x, y - 0.1, width, 0.1)

    override fun update()
    {
        super.update()

        //TODO REWRITE TO ALSO AFFECT OTHER ENTITIES

        // Sinful platform grabbing player.  This is bad.
        val bB = gameWorld.player?.boundingBox
        bB?.y = bB?.y?.minus(0.05) ?: 0.0
        bB?.x = bB?.x?.plus(0.025) ?: 0.0
        bB?.width = bB?.width?.minus(0.05) ?: 0.0

        // While the platform collides with the player, give the
        // player the platform's velocity
        if (bB?.overlaps(this.boundingBox) == true)
            gameWorld.player!!.velocity.x = velocity.x
    }

    override fun render()
    {
        // The platform should block light
        uniforms.blocking = true

        // Draw the left terminating cap
        textureLeft.bind()
        renderer.drawRectangle(x.toFloat(), y.toFloat() - 0.1f, 0.1f, 0.1f)

        // Draw the tiled center of the platform
        texture.bind()
        for (i in 1 until (width / 0.1).toInt() - 1)
            renderer.drawRectangle((x + i * 0.1).toFloat(), y.toFloat() - 0.1f, 0.1f, 0.1f)

        // Draw the right terminating cap
        textureRight.bind()
        renderer.drawRectangle((x + width).toFloat() - 0.1f, y.toFloat() - 0.1f, 0.1f, 0.1f)
    }
}
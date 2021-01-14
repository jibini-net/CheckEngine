package net.jibini.check.entity

import net.jibini.check.engine.EngineObject
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.graphics.Uniforms
import net.jibini.check.physics.BoundingBox
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture

class Platform(
    x: Double,
    y: Double,

    private val width: Double,

    override var behavior: EntityBehavior?
) : Entity(x = x, y = y)
{
//    private val deltaTimer3 = DeltaTimer()

    init
    {
        velocity.x = 0.5

//        deltaTimer3.delta
    }

    @EngineObject
    private lateinit var uniforms: Uniforms

    private val texture = Texture.load(Resource.fromClasspath("entities/platform.png"))
    private val textureLeft = Texture.load(Resource.fromClasspath("entities/platform_left.png"))
    private val textureRight = textureLeft.flip()

    override val blocking = true
    override val static = true

    override val boundingBox: BoundingBox
        get() = BoundingBox(x, y - 0.1, width, 0.1)

    override fun update()
    {
        super.update()

        // Sinful platform grabbing player.  This is bad.
        val bB = gameWorld.player?.boundingBox
        bB?.y = bB?.y?.minus(0.05) ?: 0.0
        bB?.x = bB?.x?.plus(0.025) ?: 0.0
        bB?.width = bB?.width?.minus(0.05) ?: 0.0
//        val delta = deltaTimer3.delta
        if (bB?.overlaps(this.boundingBox) == true)
            gameWorld.player!!.velocity.x = velocity.x
    }

    override fun render()
    {
        uniforms.blocking = true

        textureLeft.bind()
        renderer.drawRectangle(x.toFloat(), y.toFloat() - 0.1f, 0.1f, 0.1f)

        texture.bind()
        for (i in 1 until (width / 0.1).toInt() - 1)
            renderer.drawRectangle((x + i * 0.1).toFloat(), y.toFloat() - 0.1f, 0.1f, 0.1f)

        textureRight.bind()
        renderer.drawRectangle((x + width).toFloat() - 0.1f, y.toFloat() - 0.1f, 0.1f, 0.1f)
    }
}
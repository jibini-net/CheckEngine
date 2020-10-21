package net.jibini.check.character

import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.physics.BoundingBox
import net.jibini.check.texture.Texture
import kotlin.math.sqrt

abstract class Humanoid(
    idleRight: Texture,
    idleLeft: Texture = idleRight.flip(),

    walkRight: Texture,
    walkLeft: Texture = idleRight.flip()
) : Entity()
{
    private val stand = 0
    private val walk = 1

    var characterState = RIGHT

    var attack: Attack? = null

    private val textures: Array<Array<Texture>> = arrayOf(
        arrayOf(idleRight, idleLeft),
        arrayOf(walkRight, walkLeft)
    )

    var renderTexture = idleRight

    private val timer = DeltaTimer()

    fun jump(height: Double)
    {
        velocity.y = sqrt(2 * 9.8 * height)
    }

    override fun update()
    {
        renderTexture.bind()

        attack?.update()

        renderer.drawRectangle(
            x.toFloat() - 0.2f, y.toFloat() - (0.4f / 32),
            0.4f, 0.4f
        )

        super.update()
    }

    fun walk(x: Double, y: Double)
    {
        val movement = timer.delta / 2 * (attack?.effectiveMovementModifier ?: 1.0)

        var characterAnim: Int = stand
        if (x != 0.0 || y != 0.0) characterAnim = walk

        this.deltaPosition.y += movement * y
        this.deltaPosition.x += movement * x

        if (x < 0.0)
            characterState = LEFT
        else if (x > 0.0)
            characterState = RIGHT

        renderTexture = textures[characterAnim][characterState]
    }

    override val boundingBox: BoundingBox
        get() = BoundingBox(x - 0.1, y, 0.2, 0.3)

    fun attack()
    {
        attack?.trigger(this)
    }

    companion object
    {
        const val RIGHT = 0
        const val LEFT = 1
    }
}
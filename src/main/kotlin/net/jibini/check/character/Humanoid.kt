package net.jibini.check.character

import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.texture.Texture

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

    var attackMovementModifier = 1.0

    private val timer = DeltaTimer()

    override fun update()
    {
        renderTexture.bind()

        attack?.update()

        renderer.drawRectangle(
            x.toFloat() - 0.2f, y.toFloat() - (0.4f / 32),
            0.4f, 0.4f
        )
    }

    fun walk(x: Double, y: Double)
    {
        val delta = timer.delta / 2 * attackMovementModifier

        var characterAnim: Int = stand
        if (x != 0.0 || y != 0.0) characterAnim = walk

        this.y += delta * y
        this.x += delta * x

        if (x < 0.0)
            characterState = LEFT
        else if (x > 0.0)
            characterState = RIGHT

        renderTexture = textures[characterAnim][characterState]
    }

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
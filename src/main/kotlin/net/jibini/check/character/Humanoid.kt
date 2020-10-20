package net.jibini.check.character

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.texture.Texture
import net.jibini.check.world.GameWorld
import org.joml.Math.clamp
import org.lwjgl.opengl.GL11

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

    @EngineObject
    private lateinit var gameWorld: GameWorld

    private val textures: Array<Array<Texture>> = arrayOf(
        arrayOf(idleRight, idleLeft),
        arrayOf(walkRight, walkLeft)
    )

    var renderTexture = idleRight

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
        var blockXLeft = false
        var blockXRight = false
        var blockYUp = false
        var blockYDown = false

        val delta = timer.delta / 2 * (attack?.effectiveMovementModifier ?: 1.0)

        var characterAnim: Int = stand
//        if (x != 0.0 || y != 0.0) characterAnim = walk

        // === PHYSICS CHECK ===

        if (gameWorld.room != null)
        {
            val room = gameWorld.room!!

            val tileSize = room.tileSize

            val checkXLeft = clamp(0, room.width - 1, ((this.x - 0.1) / tileSize).toInt())
            val checkXRight = clamp(0, room.width - 1, ((this.x + 0.1) / tileSize).toInt())

            val checkYBottom = clamp(0, room.height - 1, ((this.y - 0.004) / tileSize).toInt())
            val checkYTop = clamp(0, room.height - 1, ((this.y + 0.305) / tileSize).toInt())

            val checkXLeft2 = clamp(0, room.width - 1, ((this.x - 0.11) / tileSize).toInt())
            val checkXRight2 = clamp(0, room.width - 1, ((this.x + 0.11) / tileSize).toInt())

            val checkYBottom2 = clamp(0, room.height - 1, ((this.y + 0.005) / tileSize).toInt())
            val checkYTop2 = clamp(0, room.height - 1, ((this.y + 0.285) / tileSize).toInt())

            val topLeft = room.tiles[checkXLeft][checkYTop]
            if (topLeft != null && topLeft.blocking)
                blockYUp = true
            val topRight = room.tiles[checkXRight][checkYTop]
            if (topRight != null && topRight.blocking)
                blockYUp = true

            val bottomLeft = room.tiles[checkXLeft][checkYBottom]
            if (bottomLeft != null && bottomLeft.blocking)
                blockYDown = true
            val bottomRight = room.tiles[checkXRight][checkYBottom]
            if (bottomRight != null && bottomRight.blocking)
                blockYDown = true

            val bottomLeft2 = room.tiles[checkXLeft2][checkYBottom2]
            if (bottomLeft2 != null && bottomLeft2.blocking)
                blockXLeft = true
            val topLeft2 = room.tiles[checkXLeft2][checkYTop2]
            if (topLeft2 != null && topLeft2.blocking)
                blockXLeft = true

            val bottomRight2 = room.tiles[checkXRight2][checkYBottom2]
            if (bottomRight2 != null && bottomRight2.blocking)
                blockXRight = true
            val topRight2 = room.tiles[checkXRight2][checkYTop2]
            if (topRight2 != null && topRight2.blocking)
                blockXRight = true
        }

        // =====================

        if ((!blockYUp && y > 0) || (!blockYDown && y < 0))
        {
            this.y += delta * y

            characterAnim = walk
        }

        if ((!blockXRight && x > 0) || (!blockXLeft && x < 0))
        {
            this.x += delta * x

            characterAnim = walk
        }

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
package net.jibini.check.physics

import net.jibini.check.character.Entity
import org.joml.Vector2d
import kotlin.math.abs

class BoundingBox(
    private val x: Double,
    private val y: Double,
    private val width: Double,
    private val height: Double
)
{
    fun resolve(static: BoundingBox, deltaPosition: Vector2d, entity: Entity): Boolean
    {
        val overlapX = maxOf(0.0, minOf(x + width , static.x + static.width ) - maxOf(x, static.x))
        val overlapY = maxOf(0.0, minOf(y + height, static.y + static.height) - maxOf(y, static.y))

        if (overlapX == 0.0 || overlapY == 0.0 || overlapX.isNaN() || overlapY.isNaN())
            return false

        if (overlapY > overlapX || overlapX == overlapY)
        {
            val resolution = when
                {
                    x + width - overlapX == static.x -> -1.0

                    x + overlapX == static.x + static.width -> 1.0

                    else -> -abs(deltaPosition.x) / deltaPosition.x
                }

            if (resolution.isNaN())
                return false

            entity.velocity.x = 0.0
            entity.x += resolution * overlapX
        }

        if (overlapX > overlapY || overlapX == overlapY)
        {
            val resolution = when
            {
                y + height - overlapY == static.y -> -1.0

                y + overlapY == static.y + static.height -> 1.0

                else -> -abs(deltaPosition.y) / deltaPosition.y
            }

            if (resolution.isNaN())
                return false

            entity.velocity.y = 0.0
            entity.y += resolution * overlapY
        }

        return true
    }
}
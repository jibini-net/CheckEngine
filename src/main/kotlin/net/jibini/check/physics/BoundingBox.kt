package net.jibini.check.physics

import net.jibini.check.character.Entity
import org.joml.Vector2d
import kotlin.math.abs

/**
 * An axis-aligned bounding box with x/y coordinates and a width and height; supports very basic physics calculations to
 * resolve object collisions
 *
 * @author Zach Goethel
 */
class BoundingBox(
    private val x: Double,
    private val y: Double,

    private val width: Double,
    private val height: Double
)
{
    /**
     * Detects and corrects bounding-box collisions on the horizontal and vertical axes
     *
     * @param static Bounding box of static object against which to check collisions
     * @param deltaPosition Vector representing the movement performed on this specific frame; delta x and y of entity
     *      position between the previous frame and this one
     * @param entity Entity whose position and velocity to correct depending on overlap
     *
     * @return Whether the static bounding box provides a traction-able ground point for walking (this box is supported
     *      in the vertical direction and is "on the ground")
     */
    fun resolve(static: BoundingBox, deltaPosition: Vector2d, entity: Entity): Boolean
    {
        // Detect if the two boxes overlap on either axis
        val overlapX = maxOf(0.0, minOf(x + width , static.x + static.width ) - maxOf(x, static.x))
        val overlapY = maxOf(0.0, minOf(y + height, static.y + static.height) - maxOf(y, static.y))
        // Return if there is no overlap
        if (overlapX == 0.0 || overlapY == 0.0 || overlapX.isNaN() || overlapY.isNaN())
            return false

        // If the overlap is primarily vertical, resolve in x-direction
        if (overlapY > overlapX || overlapX == overlapY)
        {
            val resolution = when
            {
                // If colliding from the right, correct leftwards
                x + width - overlapX == static.x -> -1.0
                // If colliding from the left, correct rightwards
                x + overlapX == static.x + static.width -> 1.0

                // If unsure, correct in opposite direction of movement
                else -> -abs(deltaPosition.x) / deltaPosition.x
            }

            // Check for divide-by-zero
            if (resolution.isNaN())
                return false

            // Nullify any horizontal velocity and correct box position
            entity.velocity.x = 0.0
            entity.x += resolution * overlapX
        }

        // If the overlap is primarily horizontal, resolve in y-direction
        if (overlapX > overlapY || overlapX == overlapY)
        {
            val resolution = when
            {
                // If colliding from the top, correct downwards
                y + height - overlapY == static.y -> -1.0
                // If colliding from the bottom, correct upwards
                y + overlapY == static.y + static.height -> 1.0

                // If unsure, correct in opposite direction of movement
                else -> -abs(deltaPosition.y) / deltaPosition.y
            }

            // Check for divide-by-zero
            if (resolution.isNaN())
                return false

            // Nullify any vertical velocity and correct box position
            entity.velocity.y = 0.0
            entity.y += resolution * overlapY

            // Return true if the static box supports in the positive y-axis
            if (resolution > 0.0)
                return true
        }

        // Return false if not supported positively in the y-axis
        return false
    }
}

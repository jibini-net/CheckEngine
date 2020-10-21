package net.jibini.check.character

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.graphics.Renderer
import net.jibini.check.physics.BoundingBox
import net.jibini.check.world.GameWorld
import org.joml.Vector2d

abstract class Entity(
    var x: Double = 0.0,
    var y: Double = 0.0,

    val velocity: Vector2d = Vector2d()
) : EngineAware()
{
    @EngineObject
    protected lateinit var renderer: Renderer

    @EngineObject
    protected lateinit var gameWorld: GameWorld

    private val deltaTimer = DeltaTimer()

    protected val deltaPosition = Vector2d()

    open fun update()
    {
        val room = gameWorld.room ?: return

        val delta = deltaTimer.delta

        velocity.y -= 9.8 * delta

        deltaPosition.x += velocity.x * delta
        deltaPosition.y += velocity.y * delta

        x += deltaPosition.x
        y += deltaPosition.y

        for (y in 0 until room.height)
            for (x in 0 until room.width)
            {
                val blocking = room.tiles[x][y]?.blocking ?: false
                if (!blocking)
                    continue

                boundingBox.resolve(
                    BoundingBox(x * room.tileSize, y * room.tileSize, room.tileSize, room.tileSize),
                    deltaPosition,
                    this
                )
            }

        deltaPosition.x = 0.0
        deltaPosition.y = 0.0
    }

    abstract val boundingBox: BoundingBox
}
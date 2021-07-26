package net.jibini.check.entity

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.entity.behavior.EntityBehavior
import net.jibini.check.graphics.Matrices
import net.jibini.check.graphics.Uniforms
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.physics.BoundingBox
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import net.jibini.check.world.GameWorld

import org.joml.Vector4f

@RegisterObject
class DroppedItemBehavior : EntityBehavior()
{
    @EngineObject
    private lateinit var gameWorld: GameWorld

    override fun update(entity: Entity)
    {
        //TODO REMOVE
        //println(gameWorld.player?.boundingBox?.overlaps(entity.boundingBox))
        if (entity.movementRestrictions.down)
            entity.velocity.y += 2
    }

    override fun entityCollideFrame(main: Entity, other: Entity)
    {
        print("Collect!")
        gameWorld.entities -= main

        main.behavior = null
    }
}

class Item(
    x: Double,
    y: Double
) : Entity(x, y)
{
    private val renderTexture = Texture.load(Resource.fromClasspath("little_cap/little_cap_sparking.gif"))
    private val shadowTexture = Texture.load(Resource.fromClasspath("characters/shadow.png"))

    // Required to set color and light-blocking uniforms
    @EngineObject
    private lateinit var uniforms: Uniforms

    // Required to perform transformations
    @EngineObject
    private lateinit var matrices: Matrices

    // Required to access lighting rendering flags
    @EngineObject
    private lateinit var lightingShader: LightingShaderImpl

    override fun render()
    {
        // Bind render texture
        renderTexture.bind()
        uniforms.blocking = (gameWorld.room?.isSideScroller == true) || lightingShader.nlBlockingOverride

        // Draw rectangle (centered on x-coordinate, 0.4 x 0.4)
        renderer.drawRectangle(
            x.toFloat() - 0.06f, y.toFloat() - 0.06f,
            0.12f, 0.12f
        )

        // The jump shadow blocks light in top-down mode
        uniforms.blocking = gameWorld.room?.isSideScroller == false && !lightingShader.nlBlockingOverride

        // Only render the shadow if in top-down mode
        if (gameWorld.room?.isSideScroller == false && !lightingShader.nlBlockingOverride)
        {
            shadowTexture.bind()
            // Reduce the transparency of the light-blocking texture
            // (so it's invisible, but still blocks light
            uniforms.colorMultiple = Vector4f(1.0f, 1.0f, 1.0f, 0.1f)

            renderer.drawRectangle(
                x.toFloat() - 0.03f, y.toFloat() - 0.03f,
                0.06f, 0.06f
            )

            // Always reset the color to white, 100% opacity
            uniforms.colorMultiple = Vector4f(1.0f)
        }

        // Reset the blocking flag to avoid interfering with others
        uniforms.blocking = false
    }

    override var behavior: EntityBehavior? = EngineObjectsImpl.get<DroppedItemBehavior>()[0]

    override val blocking = false
    override val static = false

    override val maxHealth = 5
    override var health = 5

    override val boundingBox: BoundingBox
        // Calculate bounding box, which is smaller than
        // rendered quad
        get() = BoundingBox(x - 0.06, y - 0.06, 0.12, 0.12)
}
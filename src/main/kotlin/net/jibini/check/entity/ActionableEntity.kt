package net.jibini.check.entity

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.character.Attack
import net.jibini.check.entity.character.Player
import net.jibini.check.graphics.Matrices
import net.jibini.check.graphics.Uniforms
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.physics.BoundingBox
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import org.joml.Math.clamp
import org.joml.Vector4f
import kotlin.math.sqrt

/**
 * A human, animal, monster, or other living/moving character; supports
 * default idle and walking animations and attacks.
 *
 * Non-player characters should extend this class, and player characters
 * should use [Player] which extends this class.
 *
 * @author Zach Goethel
 */
abstract class ActionableEntity(
    /**
     * Character's right-facing idle texture.
     */
    idleRight: Texture,

    /**
     * Character's left-facing idle texture.
     */
    idleLeft: Texture = idleRight.flip(),

    /**
     * Character's right-facing walking texture.
     */
    walkRight: Texture,

    /**
     * Character's left-facing walking texture.
     */
    walkLeft: Texture = walkRight.flip()
) : Entity()
{
    // Animation array indices
    private val stand = 0
    private val walk = 1

    /**
     * The shared jump light-blocking pattern.
     */
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

    /**
     * Character directional state (RIGHT and LEFT, or 0 and 1
     * respectively).
     */
    var characterState = RIGHT

    /**
     * Currently bound attack which will be triggered when the entity
     * attacks.
     */
    var attack: Attack? = null

    /**
     * Two-dimensional array of textures by animation and direction.
     */
    val textures: Array<Array<Texture>> = arrayOf(
        arrayOf(idleRight, idleLeft),
        arrayOf(walkRight, walkLeft)
    )

    /**
     * The texture which will be rendered when the entity is rendered.
     */
    var renderTexture = idleRight

    /**
     * The height of the false 3D effect applied to the top-down view of
     * a character.
     */
    var falseYOffset: Double = 0.0

    /**
     * The velocity of the false 3D effect applied to the top-down view
     * of a character.
     */
    var falseYVelocity: Double = 0.0

    /**
     * Entity-specific delta time to coordinate movement.
     */
    private var delta: Double = 0.0

    /**
     * Entity-specific timer to coordinate movement.
     */
    private val secondaryDeltaTimer = DeltaTimer()

    /**
     * Sets the vertical velocity in order to jump (only if currently on
     * ground).
     *
     * @param height Maximum peak height of the jump.
     */
    fun jump(height: Double)
    {
        // Only jump if character on ground
        if (movementRestrictions.down && gameWorld.room?.isSideScroller == true)
            velocity.y = sqrt(2 * 9.8 * height)

        if (gameWorld.room?.isSideScroller == false)
        {
            if (falseYOffset == 0.0)
                falseYVelocity = sqrt(2 * 9.8 * height / 2)
        }
    }

    override fun update()
    {
        walk(0.0, 0.0)

        super.update()

        // Update the movement delta time
        delta = secondaryDeltaTimer.delta

        if (gameWorld.room?.isSideScroller == true)
        {
            // Don't use false 3D on side-scrollers
            falseYOffset = 0.0
            falseYVelocity = 0.0
        } else
        {
            // Apply the velocity to the false 3D position
            falseYOffset = maxOf(0.0, falseYOffset + falseYVelocity * delta)
            // Apply gravity to the false 3D position
            falseYVelocity -= 9.8 * delta
        }
    }

    override fun render()
    {
        // Bind render texture
        renderTexture.bind()
        // Update attack; this may override previous texture
        attack?.update()

        // The whole body blocks light in side-scroller mode, or if the
        // non-light blocking override flag is set (to hide  shadows over
        // characters)
        uniforms.blocking = (gameWorld.room?.isSideScroller == true) || lightingShader.nlBlockingOverride

        // Draw rectangle (centered on x-coordinate, 0.4 x 0.4)
        renderer.drawRectangle(
            x.toFloat() - 0.2f, y.toFloat() - (0.4f / 32 * 0.99f) + falseYOffset.toFloat(),
            0.4f, 0.4f
        )

        // The jump shadow blocks light in top-down mode
        uniforms.blocking = gameWorld.room?.isSideScroller == false && !lightingShader.nlBlockingOverride
        // The shadow becomes smaller while jumping
        val shadowSize = clamp(0.1f, 0.3f, (0.3 - (falseYOffset / 3.2)).toFloat())

        // Only render the shadow if in top-down mode
        if (gameWorld.room?.isSideScroller == false && !lightingShader.nlBlockingOverride)
        {
            shadowTexture.bind()
            // Reduce the transparency of the light-blocking texture
            // (so it's invisible, but still blocks light
            uniforms.colorMultiple = Vector4f(1.0f, 1.0f, 1.0f, 0.1f)

            renderer.drawRectangle(
                x.toFloat() - shadowSize / 2, y.toFloat() - 0.012f,
                shadowSize, shadowSize
            )

            // Always reset the color to white, 100% opacity
            uniforms.colorMultiple = Vector4f(1.0f)
        }

        // Reset the blocking flag to avoid interfering with others
        uniforms.blocking = false
    }

    /**
     * The character will walk horizontally and vertically as
     * bounding-boxes will allow, animating the character as
     * appropriate; does not affect entity velocity.
     *
     * @param x Speed factor in the x-direction.
     * @param y Speed factor in the y-direction.
     */
    fun walk(x: Double, y: Double)
    {
        // Get movement speed based on delta time and attack
        // speed modifier
        val movement = delta / 1.5 * (attack?.effectiveMovementModifier ?: 1.0)

        // Default to idle animation
        var characterAnim: Int = stand

        // If on ground and moving, animate as walk
        if ((x != 0.0 || y != 0.0) && (movementRestrictions.down || (gameWorld.room?.isSideScroller != true)))
            characterAnim = walk
        // If not on ground and x-movement is against velocity,
        // zero the velocity (platform stuff)
        if (!movementRestrictions.down && x * velocity.x < 0 && gameWorld.room?.isSideScroller == true)
            velocity.x = 0.0

        // Update frame delta position with walking movement
        if ((!movementRestrictions.up && y > 0.0) || (!movementRestrictions.down && y < 0.0))
            this.deltaPosition.y += movement * y
        if ((!movementRestrictions.right && x > 0.0) || (!movementRestrictions.left && x < 0.0))
            this.deltaPosition.x += movement * x

        // Animate left if moving left; animate right if
        // moving right
        if (x < 0.0)
            characterState = LEFT
        else if (x > 0.0)
            characterState = RIGHT

        // Update the render texture depending on previous logic
        renderTexture = textures[characterAnim][characterState]
    }

    override val boundingBox: BoundingBox
        // Calculate bounding box, which is smaller than
        // rendered quad
        get() = BoundingBox(x - 0.1, y, 0.2, 0.3)

    /**
     * Trigger the bound attack; has no effect if no attack is bound.
     */
    fun attack()
    {
        attack?.trigger(this)
    }

    companion object
    {
        /**
         * Right-facing character direction state.
         */
        const val RIGHT = 0

        /**
         * Left-facing character direction state.
         */
        const val LEFT = 1
    }
}
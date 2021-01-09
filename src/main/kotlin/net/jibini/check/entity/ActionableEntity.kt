package net.jibini.check.entity

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.character.Attack
import net.jibini.check.graphics.impl.DualTexShaderImpl
import net.jibini.check.physics.BoundingBox
import net.jibini.check.resource.Resource
import net.jibini.check.texture.Texture
import org.joml.Math.clamp
import org.lwjgl.opengl.GL11
import kotlin.math.sqrt

/**
 * A human, animal, monster, or other living/moving character; supports default idle and walking animations and attacks;
 * non-player characters should extend this class, and player characters should use Player which extends this class
 *
 * @author Zach Goethel
 */
abstract class ActionableEntity(
    /**
     * Character's right-facing idle texture
     */
    idleRight: Texture,

    /**
     * Character's left-facing idle texture
     */
    idleLeft: Texture = idleRight.flip(),

    /**
     * Character's right-facing walking texture
     */
    walkRight: Texture,

    /**
     * Character's left-facing walking texture
     */
    walkLeft: Texture = idleRight.flip()
) : Entity()
{
    // Animation array indices
    private val stand = 0
    private val walk = 1

    private val shadowTexture = Texture.load(Resource.fromClasspath("characters/shadow.png"))

    @EngineObject
    private lateinit var dualTex: DualTexShaderImpl

    /**
     * Character directional state (RIGHT/LEFT, or 0/1 respectively)
     */
    var characterState = RIGHT

    /**
     * Currently bound attack which will be triggered when the entity attacks
     */
    var attack: Attack? = null

    /**
     * Two-dimensional array of textures by animation and direction
     */
    val textures: Array<Array<Texture>> = arrayOf(
        arrayOf(idleRight, idleLeft),
        arrayOf(walkRight, walkLeft)
    )

    /**
     * The texture which will be rendered when the entity is rendered
     */
    var renderTexture = idleRight

    /**
     * Delta timer to keep track of walking timing
     */
//    private val timer = DeltaTimer()

    private var falseYOffset: Double = 0.0
    private var falseYVelocity: Double = 0.0
    private var delta: Double = 0.0
    private val secondaryDeltaTimer = DeltaTimer()

    /**
     * Sets the vertical velocity in order to jump (only if currently on ground)
     *
     * @param height Maximum peak height of the jump
     */
    fun jump(height: Double)
    {
        // Only jump if character on ground
        if (movementRestrictions.down && gameWorld.room?.isSideScroller == true)
        // Velocity = sqrt(-2g * h)
            velocity.y = sqrt(2 * 9.8 * height)

        if (gameWorld.room?.isSideScroller == false)
            if (falseYOffset == 0.0)
                falseYVelocity = sqrt(2 * 9.8 * height / 2)
    }

    override fun update()
    {
        delta = secondaryDeltaTimer.delta

        if (gameWorld.room?.isSideScroller == true)
        {
            falseYOffset = 0.0
            falseYVelocity = 0.0
        } else
        {
            falseYOffset = maxOf(0.0, falseYOffset + falseYVelocity * delta)
            falseYVelocity -= 9.8 * delta
        }

        // Bind render texture
        renderTexture.bind()
        // Update attack; this may override previous texture
        attack?.update()

        if (dualTex.claimRender)
            dualTex.updateBlocking(true)

        // Draw rectangle (centered on x, 0.4 x 0.4)
        //TODO OpenGL ES
        GL11.glTranslatef(0.0f, 0.0f, 0.01f)
        renderer.drawRectangle(
            x.toFloat() - 0.2f, y.toFloat() - (0.4f / 32) + falseYOffset.toFloat(),
            0.4f, 0.4f
        )

        if (dualTex.claimRender)
            dualTex.updateBlocking(false)

        val shadowSize = clamp(0.1f, 0.2f, (0.2 - (falseYOffset / 3.2)).toFloat())
        //TODO OpenGL ES
        GL11.glColor4f(1.0f, 1.0f, 1.0f, shadowSize * 5 - 0.25f)

        if (gameWorld.room?.isSideScroller == false && falseYOffset > 0.0)
        {
            shadowTexture.bind()
            //TODO OpenGL ES
            GL11.glTranslatef(0.0f, 0.0f, -0.01f)
            renderer.drawRectangle(
                x.toFloat() - shadowSize / 2, y.toFloat() - 0.01f,
                shadowSize, shadowSize
            )
        }

        //TODO OpenGL ES
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

        // Update physics in entity last (after render to avoid shaking)
        super.update()
    }

    /**
     * The character will walk horizontally and vertically as bounding-boxes will allow, animating the character as
     * appropriate; does not affect entity velocity
     */
    fun walk(x: Double, y: Double)
    {
        // Get movement speed based on delta time and attack speed modifier
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

        // Animate left if moving left; animate right if moving right
        if (x < 0.0)
            characterState = LEFT
        else if (x > 0.0)
            characterState = RIGHT

        // Update the render texture depending on previous logic
        renderTexture = textures[characterAnim][characterState]
    }

    override val boundingBox: BoundingBox
        // Calculate bounding box on every reference
        get() = BoundingBox(x - 0.1, y, 0.2, 0.3)

    /**
     * Trigger the bound attack; has no effect if no attack is bound
     */
    fun attack()
    {
        attack?.trigger(this)
    }

    companion object
    {
        /**
         * Right-facing character direction state
         */
        const val RIGHT = 0

        /**
         * Left-facing character direction state
         */
        const val LEFT = 1
    }
}
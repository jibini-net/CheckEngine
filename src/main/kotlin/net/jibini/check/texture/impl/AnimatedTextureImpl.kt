package net.jibini.check.texture.impl

import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageInputStream
import kotlin.concurrent.thread

/**
 * A texture which iterates through several cropped sprite frames stored in centralized sprite-sheets
 *
 * @author Zach Goethel
 */
class AnimatedTextureImpl(
    /**
     * Image stream from which to load image frames
     */
    stream: ImageInputStream
) : Texture
{
    /**
     * Count of current frame being displayed
     */
    var currentFrameIndex = 0

    /**
     * List of all frames in the animation
     */
    private val animation = mutableListOf<AnimationFrame>()

    /**
     * Timer for tracking current frame's time displayed
     */
    private val timer = DeltaTimer(false)

    /**
     * A textured frame with a given frame time
     *
     * @author Zach Goethel
     */
    class AnimationFrame(
        /**
         * Frame texture object
         */
        val texture: Texture,

        /**
         * Frame display time in milliseconds
         */
        val time: Int
    )

    /**
     * Iterates to the next frame in the animation and resets the frame timer
     */
    private fun nextFrame()
    {
        currentFrameIndex++
        currentFrameIndex %= animation.size

        timer.reset()
    }

    init
    {
        // Open GIF animation reader
        val reader = ImageIO.getImageReadersByFormatName("gif").next()
        reader.input = stream

        // Track frame index number
        var frameIndex = 0

        while (true)
        {
            val image: BufferedImage = try
            {
                // Read next frame
                reader.read(frameIndex)
            } catch (io: IndexOutOfBoundsException)
            {
                // Break loop when out of frames
                break
            }

            // Check that the image is square
            if (image.width != image.height)
                throw IllegalStateException("Animated textures must have square sprite dimensions")

            // Get image metadata
            val root = reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0")
                    as IIOMetadataNode
            val gce = root.getElementsByTagName("GraphicControlExtension").item(0) as IIOMetadataNode

            // Get frame time in 1/100th seconds
            val delay = Integer.valueOf(gce.getAttribute("delayTime"))

            // Create new animation frame
            val frame = AnimationFrame(TextureSpriteMapImpl.claimSprite(image.width), delay * 10)
            // Append frame and put texture data
            animation += frame
            frame.texture.putData(0, 0, image)

            // Increment frame index last
            frameIndex++
        }

        // Close reader to avoid memory leak
        reader.dispose()
    }

    override val textureCoordinates: TextureCoordinates
        get()
        {
            if (timer.delta * 1000 >= animation[currentFrameIndex].time)
                nextFrame()

            return animation[currentFrameIndex].texture.textureCoordinates
        }

    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        animation[currentFrameIndex].texture.putData(offsetX, offsetY, width, height, data)
    }

    override val pointer: Int
        get()
        {
            if (timer.delta * 1000 >= animation[currentFrameIndex].time)
                nextFrame()
            
            return animation[currentFrameIndex].texture.pointer
        }
}
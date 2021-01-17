package net.jibini.check.texture

import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.graphics.Pointer
import net.jibini.check.resource.Resource
import net.jibini.check.texture.impl.AnimatedTextureImpl
import net.jibini.check.texture.impl.BitmapTextureImpl
import net.jibini.check.texture.impl.FlippedTextureImpl
import net.jibini.check.texture.impl.TextureRegistry
import org.lwjgl.BufferUtils
import org.lwjgl.opengles.GLES30
import java.awt.image.BufferedImage
import java.nio.Buffer
import java.nio.ByteBuffer
import javax.imageio.ImageIO

/**
 * An OpenGL texture which can be contextually bound and rendered onto geometry
 *
 * @author Zach Goethel
 */
interface Texture : Pointer<Int>
{
    /**
     * Changes the OpenGL texture bind state if it is not already correct
     */
    fun bind()
    {
        bind(GLES30.GL_TEXTURE_2D)
    }

    fun bind(target: Int)
    {
        EngineObjectsImpl.get<TextureRegistry>()[0].bind(this, target)
    }

    /**
     * Creates a texture object which has invertex texture coordinates
     *
     * @param horizontal Whether or not to flip horizontally
     * @param vertical Whether or not to flip vertically
     */
    fun flip(horizontal: Boolean = true, vertical: Boolean = false): Texture
    {
        return FlippedTextureImpl(this, horizontal, vertical)
    }

    /**
     * Returns the base texture coordinate and directional offsets
     */
    val textureCoordinates: TextureCoordinates

    /**
     * Puts texture data into the texture at the given locations
     *
     * @param offsetX Texture data start x-offset
     * @param offsetY Texture data start y-offset
     *
     * @param width Width of given texture data
     * @param height Height of given texture data
     *
     * @param data Byte-encoded RGBA texture data
     */
    fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)

    /**
     * Puts texture data into the texture at the given locations
     *
     * @param offsetX Texture data start x-offset
     * @param offsetY Texture data start y-offset
     *
     * @param data Image to collect data from and get width/height
     */
    fun putData(offsetX: Int, offsetY: Int, data: BufferedImage)
    {
        this.putData(offsetX, offsetY, data.width, data.height, data.toUnsignedBytes())
    }

    companion object
    {
        /**
         * Loads a texture from the given resource; animated textures will automatically be detected
         */
        @JvmStatic
        fun load(resource: Resource): Texture
        {
            val registry = EngineObjectsImpl.get<TextureRegistry>()[0]
            if (registry.cache.containsKey(resource.uniqueIdentifier))
                return registry.cache[resource.uniqueIdentifier]!!

            // Open texture resource and get readers
            val stream = ImageIO.createImageInputStream(resource.stream)
            val readers = ImageIO.getImageReaders(stream)

            var animate = false

            while (readers.hasNext())
            {
                val reader = readers.next()

                // If any reader is for GIFs, it should animate
                if (reader.formatName == "gif")
                {
                    animate = true

                    break
                }
            }

            // Use correct implementation for file format
            return if (animate)
            {
                val texture = AnimatedTextureImpl(stream)

                registry.cache[resource.uniqueIdentifier] = texture
                texture
            } else
            {
                // Read in buffered image and pass to texture
                val image = ImageIO.read(stream)
                val texture = BitmapTextureImpl(image.width, image.height)

                texture.putData(0, 0, image)

                registry.cache[resource.uniqueIdentifier] = texture

                texture
            }
        }

        /**
         * Converts an image to a byte-buffer of RGBA data
         */
        fun BufferedImage.toUnsignedBytes(): ByteBuffer
        {
            // Created because this method was refactored
            val image = this
            // Allocate array for all image pixels
            val pixels = IntArray(image.width * image.height)

            // Read pixel data into array as integers (4 bytes)
            image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)

            // Check alpha channel and allocate buffer
            val hasAlpha = image.colorModel.hasAlpha()
            val buffer = BufferUtils.createByteBuffer(image.width * image.height * 4)

            for (y in 0 until image.height)
                for (x in 0 until image.width)
                {
                    // Get current pixel
                    val pixel = pixels[y * image.width + x]

                    // Shift through bytes and push them on buffer
                    buffer.put(((pixel shr 16) and 0xFF).toByte())
                    buffer.put(((pixel shr  8) and 0xFF).toByte())
                    buffer.put(((pixel       ) and 0xFF).toByte())

                    // Default to opaque if alpha doesn't exist
                    if (hasAlpha)
                        buffer.put(((pixel shr 24) and 0xFF).toByte())
                    else
                        buffer.put(0xFF.toByte())
                }

            // Flip buffer; very important for OpenGL operations
            // Cast to a buffer as a JDK 9/later workaround
            (buffer as Buffer).flip()

            return buffer
        }
    }
}
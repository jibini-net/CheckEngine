package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.Texture.Companion.toUnsignedBytes
import net.jibini.check.texture.TextureCoordinates
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

/**
 * A decorated texture which crops a parent texture down to a single
 * sprite.
 *
 * @author Zach Goethel
 */
class CroppedSpriteTextureImpl(
    /**
     * Decorated parent texture instance.
     */
    private val internal: Texture,

    /**
     * Size of the sprite in pixels.
     */
    private val dimension: Int,

    /**
     * Size of the sprite's parent sheet in pixels.
     */
    private val sheetDimension: Int,

    /**
     * Index of the sprite as registered in the sprite sheet.
     */
    private val index: Int
) : Texture by internal
{
    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        // Calculate pixel offsets
        val cropX = (index % (sheetDimension / dimension)) * dimension
        val cropY = (index / (sheetDimension / dimension)) * dimension

        // Call decorated instance with offsets
        internal.putData(
            offsetX + cropX,
            offsetY + cropY,

            width, height,
            data
        )
    }

    override fun putData(offsetX: Int, offsetY: Int, data: BufferedImage)
    {
        // Use internal method (not decorated)
        this.putData(offsetX, offsetY, data.width, data.height, data.toUnsignedBytes())
    }

    /**
     * Texture-coordinate increment per sprite.
     */
    private val increment = dimension.toFloat() / sheetDimension

    override val textureCoordinates = TextureCoordinates(
        // Calculate texture coordinate offsets
        (index % (sheetDimension / dimension)).toFloat() * increment + 0.001f,
        (index / (sheetDimension / dimension)).toFloat() * increment + 0.001f,

        increment - 0.002f, increment - 0.002f
    )
}
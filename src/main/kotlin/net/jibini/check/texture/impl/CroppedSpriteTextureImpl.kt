package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.Texture.Companion.toUnsignedBytes
import net.jibini.check.texture.TextureCoordinates
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class CroppedSpriteTextureImpl(
    private val internal: Texture,

    private val dimension: Int,
    private val sheetDimension: Int,

    private val index: Int
) : Texture by DecoratedTextureImpl(internal)
{
    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        val cropX = (index % (sheetDimension / dimension)) * dimension
        val cropY = (index / (sheetDimension / dimension)) * dimension

        internal.putData(
            offsetX + cropX,
            offsetY + cropY,

            width, height,
            data
        )
    }

    override fun putData(offsetX: Int, offsetY: Int, data: BufferedImage)
    {
        this.putData(offsetX, offsetY, data.width, data.height, data.toUnsignedBytes())
    }

    private val increment = dimension.toFloat() / sheetDimension

    override val textureCoordinates = TextureCoordinates(
        (index % (sheetDimension / dimension)).toFloat() * increment,
        (index / (sheetDimension / dimension)).toFloat() * increment,

        increment, increment
    )
}
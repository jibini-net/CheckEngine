package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import org.lwjgl.BufferUtils
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class BufferedImageTexture(
    image: BufferedImage,

    private val texture: Texture = BitmapTexture(image.width, image.height)
) : Texture(texture.pointer)
{
    init
    {
        putData(0, 0, image.width, image.height, image.toUnsignedBytes())
    }

    override val textureCoordinates: TextureCoordinates
        get() = texture.textureCoordinates

    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        texture.putData(offsetX, offsetY, width, height, data)
    }

    companion object
    {
        fun BufferedImage.toUnsignedBytes(): ByteBuffer
        {
            val image = this

            val pixels = IntArray(image.width * image.height)

            image.getRGB(0, 0, image.width, image.height, pixels, 0, image.width)

            val hasAlpha = image.colorModel.hasAlpha()
            val buffer = BufferUtils.createByteBuffer(image.width * image.height * 4)

            for (y in 0 until image.height)
                for (x in 0 until image.width)
                {
                    val pixel = pixels[y * image.width + x]

                    buffer.put(((pixel shr 16) and 0xFF).toByte())
                    buffer.put(((pixel shr  8) and 0xFF).toByte())
                    buffer.put(((pixel       ) and 0xFF).toByte())

                    if (hasAlpha)
                        buffer.put(((pixel shr 24) and 0xFF).toByte())
                    else
                        buffer.put(0xFF.toByte())
                }

            buffer.flip()

            return buffer
        }
    }
}
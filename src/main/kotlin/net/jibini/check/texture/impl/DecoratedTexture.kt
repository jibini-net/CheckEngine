package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class DecoratedTexture(
    private val internal: Texture,

    override val textureCoordinates: TextureCoordinates,

    private val fullWidth: Int,
    private val fullHeight: Int
) : Texture(internal.pointer)
{
    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        internal.putData(
            offsetX + (textureCoordinates.baseX * fullWidth).roundToInt(),
            offsetY + (textureCoordinates.baseY * fullHeight).roundToInt(),

            width, height,
            data
        )
    }

    override val pointer: Int
        get() = internal.pointer
}
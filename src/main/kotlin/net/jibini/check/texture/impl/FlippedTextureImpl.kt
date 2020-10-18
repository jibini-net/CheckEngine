package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates

class FlippedTextureImpl(
    private val internal: Texture,

    private val horizontal: Boolean = true,
    private val vertical: Boolean = false
) : Texture by internal
{
    override val textureCoordinates: TextureCoordinates
        get()
        {
            val original = internal.textureCoordinates

            return TextureCoordinates(
                original.baseX + (if (horizontal) original.deltaX else 0.0f),
                original.baseY + (if (vertical  ) original.deltaY else 0.0f),

                (if (horizontal) -1.0f else 1.0f) * original.deltaX,
                (if (vertical  ) -1.0f else 1.0f) * original.deltaY
            )
        }

    override fun bind()
    {
        Texture.bind(this)
    }
}
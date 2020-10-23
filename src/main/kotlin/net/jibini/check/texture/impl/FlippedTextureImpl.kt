package net.jibini.check.texture.impl

import net.jibini.check.engine.impl.EngineObjectsImpl
import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates

/**
 * A decorated texture which inverts the parent texture's texture coordinates
 *
 * @author Zach Goethel
 */
class FlippedTextureImpl(
    /**
     * Decorated parent texture instance
     */
    private val internal: Texture,

    /**
     * Whether or not to flip horizontally
     */
    private val horizontal: Boolean = true,

    /**
     * Whether or not to flip vertically
     */
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
        EngineObjectsImpl.get<TextureRegistry>()[0].bind(this)
    }
}
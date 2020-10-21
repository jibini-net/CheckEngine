package net.jibini.check.texture.impl

import net.jibini.check.graphics.Pointer
import net.jibini.check.graphics.impl.AbstractAutoDestroyable
import net.jibini.check.graphics.impl.PointerImpl
import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

/**
 * A texture which is backed by a byte-buffer of RGBA data
 *
 * @author Zach Goethel
 */
class BitmapTextureImpl(
    /**
     * Width of the texture in texels
     */
    width: Int = TextureSpriteMapImpl.MAP_DIMENSION,

    /**
     * Height of the texture in texels
     */
    height: Int = TextureSpriteMapImpl.MAP_DIMENSION
) : AbstractAutoDestroyable(), Texture, Pointer<Int> by PointerImpl(GL11.glGenTextures())
{
    init
    {
        // Store currently bound texture
        val bound = Texture.bound
        bind()

        // Set coordinate clamp/wrap properties
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        // Set min/magnification filters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        // Allocate video memory to the texture
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0,
            GL11.GL_RGBA,
            width, height, 0,
            GL11.GL_RGBA, GL11.GL_FLOAT,
            0L
        )

        // Rebind previously bound texture
        bound?.bind()
    }

    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        // Store currently bound texture
        val bound = Texture.bound
        bind()

        // Set the video memory for the given texture segment
        GL11.glTexSubImage2D(
            GL11.GL_TEXTURE_2D, 0,
            offsetX, offsetY,
            width, height,
            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
            data
        )

        // Rebind previously bound texture
        bound?.bind()
    }

    override val textureCoordinates: TextureCoordinates = TextureCoordinates(
        0.0f, 0.0f,
        1.0f, 1.0f
    )

    override fun destroy()
    {
        GL11.glDeleteTextures(pointer)
    }
}
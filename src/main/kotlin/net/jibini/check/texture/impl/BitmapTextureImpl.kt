package net.jibini.check.texture.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Pointer
import net.jibini.check.graphics.impl.AbstractAutoDestroyable
import net.jibini.check.graphics.impl.PointerImpl
import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import org.lwjgl.opengles.GLES30
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
) : AbstractAutoDestroyable(), Texture, Pointer<Int> by PointerImpl(GLES30.glGenTextures())
{
    @EngineObject
    private lateinit var textureRegistry: TextureRegistry

    init
    {
        // Store currently bound texture
        val bound = textureRegistry.bound
        bind()

        // Set coordinate clamp/wrap properties
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        // Set min/magnification filters
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)

        // Allocate video memory to the texture
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0,
            GLES30.GL_RGBA,
            width, height, 0,
            GLES30.GL_RGBA, GLES30.GL_FLOAT,
            0L
        )

        // Rebind previously bound texture
        bound?.bind()
    }

    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        // Store currently bound texture
        val bound = textureRegistry.bound
        bind()

        // Set the video memory for the given texture segment
        GLES30.glTexSubImage2D(
            GLES30.GL_TEXTURE_2D, 0,
            offsetX, offsetY,
            width, height,
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,
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
        GLES30.glDeleteTextures(pointer)
    }
}
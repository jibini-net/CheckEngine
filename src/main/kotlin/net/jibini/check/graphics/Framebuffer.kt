package net.jibini.check.graphics

import net.jibini.check.graphics.impl.AbstractAutoDestroyable
import net.jibini.check.graphics.impl.PointerImpl
import net.jibini.check.texture.Texture
import net.jibini.check.texture.impl.BitmapTextureImpl

import org.lwjgl.opengles.GLES30

/**
 * An OpenGL framebuffer which has the specified number of render
 * attachments for color output.
 *
 * @author Zach Goethel
 */
class Framebuffer(
    /**
     * Width of the framebuffer and its attachments in pixels.
     */
    val width: Int,

    /**
     * Height of the framebuffer and its attachments in pixels.
     */
    val height: Int,

    /**
     * The number of render attachment texture to create.
     */
    numberRenderAttachments: Int = 1
): AbstractAutoDestroyable(), Pointer<Int> by PointerImpl(GLES30.glGenFramebuffers())
{
    /**
     * A collection of render attachment textures which are
     * automatically created and attached to the framebuffer. The index
     * of each texture corresponds to the index of the color render
     * attachment.
     */
    val renderAttachments = Array<Texture>(numberRenderAttachments) { BitmapTextureImpl(width, height) }

    init
    {
        bind()

        for (i in 0 until numberRenderAttachments)
                GLES30.glFramebufferTexture2D(
                    GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0 + i,
                    GLES30.GL_TEXTURE_2D, renderAttachments[i].pointer, 0
                )

        val drawBuffers = IntArray(numberRenderAttachments) { GLES30.GL_COLOR_ATTACHMENT0 + it }
        GLES30.glDrawBuffers(drawBuffers)

        release()
    }

    /**
     * Binds the framebuffer for rendering and sets the viewport state
     * to the framebuffer size.
     */
    fun bind()
    {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, pointer)
        GLES30.glViewport(0, 0, width, height)
    }

    override fun destroy()
    {
        GLES30.glDeleteFramebuffers(pointer)

        for (texture in renderAttachments)
        {
            if (texture is Destroyable)
                texture.destroy()
        }
    }

    companion object
    {
        /**
         * Releases any bound framebuffer to render directly to the
         * screen.
         */
        @JvmStatic
        fun release()
        {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        }
    }
}

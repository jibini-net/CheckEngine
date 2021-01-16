package net.jibini.check.graphics

import net.jibini.check.graphics.impl.AbstractAutoDestroyable
import net.jibini.check.graphics.impl.PointerImpl
import net.jibini.check.texture.impl.BitmapTextureImpl
import org.lwjgl.opengles.GLES30

class Framebuffer(
    val width: Int,
    val height: Int,

    numberRenderAttachments: Int = 1
): AbstractAutoDestroyable(), Pointer<Int> by PointerImpl(GLES30.glGenFramebuffers())
{
    val renderAttachments = Array(numberRenderAttachments) { BitmapTextureImpl(width, height) }

    init
    {
        bind()

        //val renderBuffer = GLES30.glGenRenderbuffers()
        //GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, renderBuffer)
        //GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT, width, height)

        //GLES30.glFramebufferRenderbuffer(
        //    GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
        //    GLES30.GL_RENDERBUFFER, renderBuffer
        //)

        for (i in 0 until numberRenderAttachments)
            GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0 + i,
                GLES30.GL_TEXTURE_2D, renderAttachments[i].pointer, 0
            )

        val drawBuffers = IntArray(numberRenderAttachments) { GLES30.GL_COLOR_ATTACHMENT0 + it }
        GLES30.glDrawBuffers(drawBuffers)

        release()
    }

    fun bind()
    {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, pointer)
        GLES30.glViewport(0, 0, width, height)
    }

    override fun destroy()
    {
        GLES30.glDeleteFramebuffers(pointer)
        for (texture in renderAttachments)
            texture.destroy()
    }

    companion object
    {
        @JvmStatic
        fun release()
        {
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        }
    }
}

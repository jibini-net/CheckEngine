package net.jibini.check.texture

import net.jibini.check.graphics.Pointer
import net.jibini.check.resource.Resource
import net.jibini.check.texture.impl.AnimatedTexture
import net.jibini.check.texture.impl.BufferedImageTexture
import org.lwjgl.opengl.GL11
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import javax.imageio.ImageIO

abstract class Texture(
    override val pointer: Int = GL11.glGenTextures()
) : Pointer<Int>()
{
    /**
     * Changes the OpenGL texture bind state if it is not already correct
     */
    fun bind()
    {
        bind(this)
    }

    /**
     * Returns the base texture coordinate and directional offsets
     */
    abstract val textureCoordinates: TextureCoordinates

    override fun destroy()
    {
        GL11.glDeleteTextures(pointer)
    }

    abstract fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)

    companion object
    {
        private val log = LoggerFactory.getLogger(Texture::class.java)

        private val boundPerThread = mutableMapOf<Thread, Texture>()

        val bound: Texture?
            get() = boundPerThread[Thread.currentThread()]

        private fun bind(texture: Texture)
        {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.pointer)

            boundPerThread[Thread.currentThread()] = texture
        }

        @JvmStatic
        fun from(resource: Resource): Texture
        {
            val stream = ImageIO.createImageInputStream(resource.stream)
            val readers = ImageIO.getImageReaders(stream)

            var animate = false

            while (readers.hasNext())
            {
                val reader = readers.next()

                if (reader.formatName == "gif")
                {
                    animate = true

                    log.debug("Animated image detected; handling individual frames")
                }
            }

            return if (animate)
            {
                AnimatedTexture(stream)
            } else
            {
                BufferedImageTexture(ImageIO.read(stream))
            }
        }
    }
}
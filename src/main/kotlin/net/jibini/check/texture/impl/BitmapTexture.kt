package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

class BitmapTexture(
    width: Int = TextureMap.MAP_DIMENSION,
    height: Int = TextureMap.MAP_DIMENSION
) : Texture()
{
    init
    {
        val bound = bound
        bind()

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0,
            GL11.GL_RGBA,
            width, height, 0,
            GL11.GL_RGBA, GL11.GL_FLOAT,
            0L
        )

        bound?.bind()
    }

    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        val bound = bound
        bind()

        GL11.glTexSubImage2D(
            GL11.GL_TEXTURE_2D, 0,
            offsetX, offsetY,
            width, height,
            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
            data
        )

        bound?.bind()
    }

    override val textureCoordinates: TextureCoordinates = TextureCoordinates(
        0.0f, 0.0f,
        1.0f, 1.0f
    )
}
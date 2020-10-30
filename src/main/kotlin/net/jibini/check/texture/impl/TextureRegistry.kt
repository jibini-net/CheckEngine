package net.jibini.check.texture.impl

import net.jibini.check.engine.RegisterObject
import net.jibini.check.texture.Texture
import org.lwjgl.opengl.GL11

@RegisterObject
class TextureRegistry
{
    /**
     * Currently bound texture in the current thread
     */
    var bound: Texture? = null

    /**
     * Currently bound pointer in the current thread
     */
    private var boundPointer: Int = 0

    /**
     * Textures which have already been loaded and allocated, matched by their resources' unique identifiers
     */
    val cache = mutableMapOf<String, Texture>()

    /**
     * Binds the given texture in the current thread
     *
     * @param texture Texture to bind
     */
    fun bind(texture: Texture)
    {
        // Only change bind if the currently bound state is different
        if (texture.pointer != boundPointer)
        {
            boundPointer = texture.pointer

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.pointer)
        }

        bound = texture
    }

    fun unbind()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        bound = null
        boundPointer = 0
    }
}
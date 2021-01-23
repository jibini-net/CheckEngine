package net.jibini.check.texture.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Uniforms
import net.jibini.check.texture.Texture

import org.joml.Vector2f

import org.lwjgl.opengles.GLES30

import java.lang.IllegalStateException

/**
 * Maintains cached textures and stateful data about bound textures.
 *
 * @author Zach Goethel
 */
@RegisterObject
class TextureRegistry
{
    // Required to access texture uniforms
    @EngineObject
    private lateinit var uniforms: Uniforms

    /**
     * Currently bound texture in the current thread.
     */
    var bound: Texture? = null

    /**
     * Currently bound pointer in the current thread.
     */
    private var boundPointer: Int = 0

    /**
     * Textures which have already been loaded and allocated, matched by
     * their resources' unique identifiers.
     */
    val cache = mutableMapOf<String, Texture>()

    fun reverseLookup(texture: Texture): String
    {
        for ((key, value) in cache)
        {
            if (value == texture)
                return key
        }

        throw IllegalStateException("Reverse lookup failed; texture is not cached")
    }

    /**
     * Binds the given texture in the current thread.
     *
     * @param texture Texture to bind.
     */
    fun bind(texture: Texture, target: Int = GLES30.GL_TEXTURE_2D)
    {
        // Only change bind if the currently bound state is different
        if (texture.pointer != boundPointer)
        {
            boundPointer = texture.pointer

            GLES30.glBindTexture(target, texture.pointer)
        }

        @Suppress("SENSELESS_COMPARISON")
        if (texture.textureCoordinates != null)
        {
            uniforms.textureOffset = Vector2f(texture.textureCoordinates.baseX, texture.textureCoordinates.baseY)
            uniforms.textureDelta = Vector2f(texture.textureCoordinates.deltaX, texture.textureCoordinates.deltaY)
        }

        bound = texture
    }
}
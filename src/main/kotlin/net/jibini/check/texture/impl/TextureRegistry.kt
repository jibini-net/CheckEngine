package net.jibini.check.texture.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.impl.DirectTexShaderImpl
import net.jibini.check.graphics.impl.DualTexShaderImpl
import net.jibini.check.texture.Texture
import org.lwjgl.opengles.GLES30

@RegisterObject
class TextureRegistry
{
    @EngineObject
    private lateinit var tempTexShaderImpl: DirectTexShaderImpl

    @EngineObject
    private lateinit var dualTexShaderImpl: DualTexShaderImpl

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

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.pointer)
        }

        @Suppress("SENSELESS_COMPARISON")
        if (texture.textureCoordinates != null)
        {
            if (dualTexShaderImpl.claimRender)
                dualTexShaderImpl.updateUniform(texture.textureCoordinates.baseX, texture.textureCoordinates.baseY)
            else
                tempTexShaderImpl.updateUniform(texture.textureCoordinates.baseX, texture.textureCoordinates.baseY)
        }

        bound = texture
    }

    fun unbind()
    {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        bound = null
        boundPointer = 0
    }
}
package net.jibini.check.texture

abstract class Texture
{
    /**
     * Changes the OpenGL texture bind state if it is not already correct
     */
    abstract fun bind()

    /**
     * Returns the base texture coordinate and directional offsets
     */
    abstract val textureCoordinates: TextureCoordinates
}
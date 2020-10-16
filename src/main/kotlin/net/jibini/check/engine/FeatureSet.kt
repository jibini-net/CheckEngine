package net.jibini.check.engine

import org.lwjgl.opengl.GL11

class FeatureSet
{
    var clearFlags: Int = GL11.GL_COLOR_BUFFER_BIT

    fun enableDepthTest(): FeatureSet
    {
        clearFlags = clearFlags or GL11.GL_DEPTH_BUFFER_BIT
        GL11.glEnable(GL11.GL_DEPTH_TEST)

        return this;
    }

    fun enable2DTextures(): FeatureSet
    {
        GL11.glEnable(GL11.GL_TEXTURE_2D)

        return this;
    }

    fun enableTransparency(): FeatureSet
    {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        return this
    }
}
package net.jibini.check.engine

import org.lwjgl.opengles.GLES30

/**
 * Object for configuring OpenGL features on application initialization.
 *
 * @author Zach Goethel
 */
@Deprecated("Usage has unclear effect (or lack thereof)")
class FeatureSet
{
    /**
     * OpenGL buffer flags for which buffers should be cleared each frame.
     */
    var clearFlags: Int = GLES30.GL_COLOR_BUFFER_BIT

    /**
     * Enables depth test and adds the depth buffer to the clear flags.
     */
    @Deprecated("Usage has unclear effect (or lack thereof)")
    fun enableDepthTest(): FeatureSet
    {
        // Add depth clear flag and enable depth test
        clearFlags = clearFlags or GLES30.GL_DEPTH_BUFFER_BIT
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        return this
    }

    /**
     * Enables 2D texturing globally on the current context.
     */
    @Deprecated("Usage has unclear effect (or lack thereof)")
    fun enable2DTextures(): FeatureSet
    {
        // Enable 2D textures globally
        GLES30.glEnable(GLES30.GL_TEXTURE_2D)

        return this
    }

    /**
     * Enables transparency and blending on the current context.
     */
    @Deprecated("Usage has unclear effect (or lack thereof)")
    fun enableTransparency(): FeatureSet
    {
        // Enable blending and set the blend function
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        return this
    }
}
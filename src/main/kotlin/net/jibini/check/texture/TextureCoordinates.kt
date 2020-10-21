package net.jibini.check.texture

/**
 * Base and offset texture coordinates
 *
 * @author Zach Goethel
 */
class TextureCoordinates(
    /**
     * Texture coordinate base x
     */
    val baseX: Float,

    /**
     * Texture coordinate base y
     */
    val baseY: Float,

    /**
     * Texture coordinate offset x
     */
    val deltaX: Float,

    /**
     * Texture coordinate offset y
     */
    val deltaY: Float
)

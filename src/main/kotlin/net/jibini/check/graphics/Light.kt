package net.jibini.check.graphics

/**
 * A single light with position and color.
 *
 * @author Zach Goethel
 */
class Light(
    /**
     * Tile x-position of the light.
     */
    val x: Float,

    /**
     * Tile y-position of the light.
     */
    val y: Float,

    /**
     * Red component of the light color.
     */
    val r: Float,

    /**
     * Green component of the light color.
     */
    val g: Float,

    /**
     * Blue component of the light color.
     */
    val b: Float
)
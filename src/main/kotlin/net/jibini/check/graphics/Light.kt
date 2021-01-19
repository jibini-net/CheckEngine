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
    var x: Float,

    /**
     * Tile y-position of the light.
     */
    var y: Float,

    /**
     * Red component of the light color.
     */
    var r: Float,

    /**
     * Green component of the light color.
     */
    var g: Float,

    /**
     * Blue component of the light color.
     */
    var b: Float
)
package net.jibini.check.physics

/**
 * An object which has a bounding box.
 *
 * @author Zach Goethel
 */
interface Bounded
{
    /**
     * The object's bounding box.
     */
    val boundingBox: BoundingBox
}
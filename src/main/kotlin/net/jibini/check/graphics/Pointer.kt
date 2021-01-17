package net.jibini.check.graphics

/**
 * Represents a graphical object's numerical pointer.
 *
 * @author Zach Goethel
 */
interface Pointer<T : Number>
{
    /**
     * Graphical object's numerical pointer.
     */
    val pointer: T
}
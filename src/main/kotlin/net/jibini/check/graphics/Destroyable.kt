package net.jibini.check.graphics

/**
 * Any object which can be destroyed to release memory and resources
 *
 * @author Zach Goethel
 */
interface Destroyable
{
    /**
     * Destroy the object and release any allocated resources
     */
    fun destroy()
}
package net.jibini.check.engine

/**
 * Any object which can be updated; if the class is also annotated with [RegisterObject], the object will automatically
 * be updated by the engine on every game frame
 *
 * @author Zach Goethel
 */
interface Updatable
{
    /**
     * Update function; automatically called every frame for registered engine objects
     */
    fun update()
}
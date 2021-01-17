package net.jibini.check.engine

/**
 * Any object which can be updated; if the class is also annotated with
 * [RegisterObject], the object will automatically be updated by the
 * engine every game frame.
 *
 * There is no guarantee of the order in which engine objects will be
 * updated. _It is not safe to assume that another engine object has
 * already been updated by the time this one is._
 *
 * @author Zach Goethel
 */
interface Updatable
{
    /**
     * Update function; automatically called every frame for registered
     * engine objects.
     *
     * There is no guarantee of the order in which engine objects will
     * be updated. _It is not safe to assume that another engine object
     * has already been updated by the time this one is._
     */
    fun update()
}
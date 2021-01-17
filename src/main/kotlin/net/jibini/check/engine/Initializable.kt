package net.jibini.check.engine

/**
 * Any object which can be initialized; if the class is also annotated
 * with [RegisterObject], the object will automatically be initialized
 * upon creation.
 *
 * There is no guarantee of the order in which engine objects will be
 * initialized. _It is not safe to assume that another engine object
 * has already been initialized by the time this one is._
 *
 * @author Zach Goethel
 */
interface Initializable
{
    /**
     * Initialization function; this is automatically called upon
     * creation for registered engine objects.
     *
     * There is no guarantee of the order in which engine objects will
     * be initialized. _It is not safe to assume that another engine
     * object has already been initialized by the time this one is._
     */
    fun initialize()
}
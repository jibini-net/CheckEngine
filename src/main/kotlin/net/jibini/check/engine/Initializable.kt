package net.jibini.check.engine

/**
 * Any object which can be initialized; if the class is also annotated with [RegisterObject], the object will
 * automatically be initialized upon creation
 *
 * @author Zach Goethel
 */
interface Initializable
{
    /**
     * Initialization function; automatically called upon creation for registered engine objects
     */
    fun initialize()
}
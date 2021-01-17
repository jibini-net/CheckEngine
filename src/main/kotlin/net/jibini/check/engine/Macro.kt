package net.jibini.check.engine

/**
 * Any action which can be called upon an event, such as a
 * world-initialization action triggered upon a world loading from an
 * asset file.
 *
 * Classes implementing this interface must be
 * [registered as an engine object][RegisterObject].
 *
 * @author Zach Goethel
 */
interface Macro
{
    /**
     * Execute actions here; this object will have access to engine
     * objects during execution.
     */
    fun action()
}
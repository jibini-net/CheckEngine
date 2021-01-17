package net.jibini.check

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.Updatable

/**
 * A primary game object which defines initialization and entry points
 * for the game's execution.
 *
 * @author Zach Goethel
 */
interface CheckGame : Initializable, Updatable
{
    /**
     * Basic information required to start up the game engine.
     */
    val profile: Profile

    /**
     * Basic information required to start up the game engine.
     */
    class Profile(
        /**
         * Name of the application; used for window titles and logging.
         */
        val appName: String,

        /**
         * Version of the application; used for window titles and
         * logging.
         */
        val appVersion: String
    )
}
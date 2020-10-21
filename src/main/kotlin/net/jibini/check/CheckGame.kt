package net.jibini.check

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.Updatable

/**
 * A primary game object which defines initialization and entry points for the game's execution
 *
 * @author Zach Goethel
 */
interface CheckGame : Initializable, Updatable
{
    /**
     * Basic information required to start up the game engine
     */
    val profile: Profile

    /**
     * Basic information required to start up the game engine
     */
    class Profile(
        /**
         * Name of the application; used for window titles and logging
         */
        val appName: String,

        /**
         * Version of the application; used for window titles and logging
         */
        val appVersion: String,

        /**
         * Version of OpenGL context to initialize (in form XY, where X is major version and Y is minor version);
         * determines the hardware support conformance level of the game engine's validation layer
         *
         * For core profiles and relatively modern features, consider a 3.X profile; for wide compatibility but limited
         * feature sets, use a 2.X profile; for bleeding edge and possibly unsupported features, use a 4.X profile
         *
         * @see contextCore
         * @see contextForwardCompat
         */
        val contextVersion: Int,

        /**
         * Whether the OpenGL context should be set to a core profile
         */
        val contextCore: Boolean,

        /**
         * Whether the OpenGL context should be set to a forward compatible profile
         */
        val contextForwardCompat: Boolean
    )
}
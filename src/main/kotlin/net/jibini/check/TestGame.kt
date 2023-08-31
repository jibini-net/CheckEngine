package net.jibini.check

import net.jibini.check.Check.boot
import net.jibini.check.Check.infinitelyPoll
import net.jibini.check.engine.EngineObject
import net.jibini.check.entity.character.Attack
import net.jibini.check.graphics.impl.LightingShaderImpl
import net.jibini.check.resource.Resource.Companion.fromClasspath
import net.jibini.check.texture.Texture.Companion.load
import net.jibini.check.world.GameWorld
import org.lwjgl.glfw.GLFW
import java.util.*

class TestGame : CheckGame
{
    @EngineObject
    private var gameWorld: GameWorld? = null

    @EngineObject
    private var lightingShader: LightingShaderImpl? = null

    /**
     * Game initialization section; the OpenGL context and GLFW window
     * have been created; this method is called from the game's personal
     * thread.
     * <br></br><br></br>
     *
     * Set up graphics and register update tasks here.
     */
    override fun initialize()
    {
        gameWorld!!.loadRoom("main_hub")
        Objects.requireNonNull((gameWorld as GameWorld).player)!!
            .attack = Attack(
            load(fromClasspath("characters/forbes/forbes_chop_right.gif")),
            load(fromClasspath("characters/forbes/forbes_chop_left.gif")),  /* Animation time (sec):    */
            0.5,  /* Cool-down time (sec):    */
            0.35,  /* Always reset animation?  */
            false,  /* Attack damage amount:    */
            1.0,  /* Movement scale effect:   */
            0.5
        )
        (gameWorld as GameWorld).visible = true
        lightingShader!!.framebufferPixelsPerTile = 16
        GLFW.glfwSwapInterval(0)
    }

    /**
     * Game update section; this is run every frame after the render
     * buffers are cleared and before the GLFW window buffers are
     * swapped; this method is called from the game's personal thread.
     * <br></br><br></br>
     *
     * Perform physics updates, game updates, and rendering here.
     */
    override fun update()
    {
    }/* App Name:    */  /* App Version: */

    /**
     * @return A game profile information object which specifies basic
     * game information.
     */
    override val profile: CheckGame.Profile
        get() = CheckGame.Profile( /* App Name:    */
            "Test Game",  /* App Version: */
            "0.0"
        )

    companion object
    {
        /**
         * Application entry point; calls the engine boot method and hangs
         * until the game is closed.
         * <br></br><br></br>
         *
         * !!! **DO NOT PLACE CODE IN THIS MAIN METHOD** !!!
         */
        @JvmStatic
        fun main(args: Array<String>)
        {
            boot(TestGame())
            infinitelyPoll()
        }
    }
}
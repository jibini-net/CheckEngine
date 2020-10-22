package net.jibini.check;

import net.jibini.check.character.Attack;
import net.jibini.check.character.Player;
import net.jibini.check.engine.*;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import net.jibini.check.world.GameWorld;
import net.jibini.check.world.Room;
import net.jibini.check.world.Tile;
import org.jetbrains.annotations.NotNull;

public class TestGame implements CheckGame
{
    @EngineObject
    private FeatureSet featureSet;

    @EngineObject
    private GameWorld gameWorld;

    /**
     * Application entry point; calls the engine boot method and hangs until the game is closed
     * <br /><br />
     *
     * !!! <strong>DO NOT PLACE CODE IN THIS MAIN METHOD</strong> !!!
     */
    public static void main(String[] args)
    {
        Check.boot(new TestGame());

        Check.infinitelyPoll();
    }

    /**
     * Game initialization section; the OpenGL context and GLFW window have been created; this method is called from the
     * game's personal thread
     * <br /><br />
     *
     * Enable features and register update tasks here
     */
    @Override
    public void initialize()
    {
        featureSet.enableDepthTest()
                .enable2DTextures()
                .enableTransparency();

        gameWorld.loadRoom("main_hub");

        gameWorld.getPlayer().setAttack(new Attack(
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_left.gif")),

                /* Animation time (sec):    */ 0.5,
                /* Cool-down time (sec):    */ 0.35,
                /* Always reset animation?  */ false,

                /* Attack damage amount:    */ 1.0,
                /* Movement scale effect:   */ 0.5
        ));

        gameWorld.setVisible(true);
    }

    /**
     * Game update section; this is run every frame after the render buffers are cleared and before the GLFW window
     * buffers are swapped; this method is called from the game's personal thread
     * <br /><br />
     *
     * Perform physics updates, game updates, and rendering here
     */
    public void update()
    {

    }

    /**
     * @return A game profile information object which specifies basic game information
     */
    @NotNull
    @Override
    public Profile getProfile()
    {
        return new CheckGame.Profile(
                /* App Name:    */ "Test Game",
                /* App Version: */ "0.0",
                /* GL Version:  */ 20,
                /* GL Core:     */ false,
                /* GL Forward:  */ false
        );
    }
}

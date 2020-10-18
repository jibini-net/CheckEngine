package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.FeatureSet;
import net.jibini.check.engine.LifeCycle;
import net.jibini.check.graphics.Renderer;
import net.jibini.check.graphics.Window;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import org.jetbrains.annotations.NotNull;

public class TestGame implements CheckGame
{
    @EngineObject
    public FeatureSet featureSet;

    @EngineObject
    public Window window;

    @EngineObject
    public LifeCycle lifeCycle;

    @EngineObject
    public Renderer renderer;

    private Texture[] texture;

    /**
     * Application entry point; calls the engine boot methods and polls for controller/keyboard/mouse inputs on the
     * main thread until the game is closed
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
    public void start()
    {
        featureSet.enableDepthTest()
                .enable2DTextures()
                .enableTransparency();
        lifeCycle.registerTask(this::update);

        texture = new Texture[] {
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_beard_right.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_left.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_right.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_back_left.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_back_right.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_left.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_right.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_back_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_back_left.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_left.gif"))
        };
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
        for (int i = 0; i < texture.length; i++)
        {
            texture[i].bind();
            //noinspection IntegerDivisionInFloatingPointContext
            renderer.drawRectangle(
                    -1.0f + 0.4f * (i % 5),
                    -1.0f + 0.4f * (i / 5),
                    0.4f, 0.4f
            );
        }
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

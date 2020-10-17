package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.FeatureSet;
import net.jibini.check.engine.LifeCycle;
import net.jibini.check.graphics.Renderer;
import net.jibini.check.graphics.Window;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

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
                Texture.from(Resource.fromClasspath("characters/allie.gif")),
                Texture.from(Resource.fromClasspath("characters/becky.gif")),
                Texture.from(Resource.fromClasspath("characters/foley.gif")),
                Texture.from(Resource.fromClasspath("characters/forbes.gif")),
                Texture.from(Resource.fromClasspath("characters/forbes_axe.gif")),
                Texture.from(Resource.fromClasspath("characters/forbes_beard.gif")),
                Texture.from(Resource.fromClasspath("characters/gendrau.gif")),
                Texture.from(Resource.fromClasspath("characters/hunt.gif")),
                Texture.from(Resource.fromClasspath("characters/hunt_sunglasses.gif")),
                Texture.from(Resource.fromClasspath("characters/jason.gif")),
                Texture.from(Resource.fromClasspath("characters/joe_gow.gif")),
                Texture.from(Resource.fromClasspath("characters/kasi.gif")),
                Texture.from(Resource.fromClasspath("characters/lei_wang.gif")),
                Texture.from(Resource.fromClasspath("characters/maraist.gif")),
                Texture.from(Resource.fromClasspath("characters/mathias.gif")),
                Texture.from(Resource.fromClasspath("characters/petullo.gif")),
                Texture.from(Resource.fromClasspath("characters/senger.gif")),
                Texture.from(Resource.fromClasspath("characters/zebrof.gif")),
                Texture.from(Resource.fromClasspath("characters/zheng.gif"))
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

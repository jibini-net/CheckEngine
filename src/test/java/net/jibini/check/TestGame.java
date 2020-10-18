package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.FeatureSet;
import net.jibini.check.engine.LifeCycle;
import net.jibini.check.engine.timing.DeltaTimer;
import net.jibini.check.graphics.Renderer;
import net.jibini.check.graphics.Window;
import net.jibini.check.input.Keyboard;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class TestGame implements CheckGame
{
    @EngineObject
    public FeatureSet featureSet;

    @EngineObject
    public Window window;

    @EngineObject
    public Keyboard keyboard;

    @EngineObject
    public LifeCycle lifeCycle;

    @EngineObject
    public Renderer renderer;

    private static final int STAND = 0;
    private static final int WALK = 1;
    private static final int ATTACK = 2;

    private static final int RIGHT = 0;
    private static final int LEFT = 1;

    private DeltaTimer timer = new DeltaTimer();

    private Texture[][] textures;
    private double x = 0, y = 0;
    private int characterState = RIGHT;
    private int characterAnim = STAND;

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

        textures = new Texture[][] {
                {
                        Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_right.gif")),
                        Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_left.gif"))
                },
                {
                        Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_right.gif")),
                        Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_left.gif"))
                },
                {
                        Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_right.gif")),
                        Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_left.gif"))
                }
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
        double delta = timer.getDelta() / 2;

        boolean space = keyboard.isPressed(GLFW.GLFW_KEY_SPACE);

        boolean w = keyboard.isPressed(GLFW.GLFW_KEY_W);
        boolean a = keyboard.isPressed(GLFW.GLFW_KEY_A);
        boolean s = keyboard.isPressed(GLFW.GLFW_KEY_S);
        boolean d = keyboard.isPressed(GLFW.GLFW_KEY_D);

        characterAnim = STAND;
        if ((w ^ s) || (a ^ d))
            characterAnim = WALK;

        if (space)
        {
            characterAnim = ATTACK;
            delta /= 2.4;
        }

        if (w)
            y += delta;
        if (s)
            y -= delta;

        if (a)
        {
            x -= delta;

            characterState = LEFT;
        }

        if (d)
        {
            x += delta;

            characterState = RIGHT;
        }

        textures[characterAnim][characterState].bind();

        renderer.drawRectangle(
                (float)x, (float)y,
                0.4f, 0.4f
        );
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

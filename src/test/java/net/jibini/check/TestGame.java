package net.jibini.check;

import net.jibini.check.character.Humanoid;
import net.jibini.check.character.Player;
import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.FeatureSet;
import net.jibini.check.engine.LifeCycle;
import net.jibini.check.graphics.Renderer;
import net.jibini.check.graphics.Window;
import net.jibini.check.input.Keyboard;
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
    public Keyboard keyboard;

    @EngineObject
    public LifeCycle lifeCycle;

    @EngineObject
    public Renderer renderer;

//    private final DeltaTimer attackTimer = new DeltaTimer(false);
//
//    private final double attackTime = 0.6;
//    private final double attackReTriggerTime = 0.5;

    private Humanoid forbes;

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

        forbes = new Player(
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_left.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_left.gif")),

                keyboard
        );

//        keyboard.addKeyListener(GLFW.GLFW_KEY_SPACE, () ->
//        {
//            if (attackTimer.getDelta() > attackReTriggerTime)
//            {
//                if (attackTimer.getDelta() > attackTime)
//                {
//                    if (textures[ATTACK][LEFT ] instanceof AnimatedTextureImpl)
//                        ((AnimatedTextureImpl) textures[ATTACK][LEFT ]).setCurrentFrameIndex(0);
//                    if (textures[ATTACK][RIGHT] instanceof AnimatedTextureImpl)
//                        ((AnimatedTextureImpl) textures[ATTACK][RIGHT]).setCurrentFrameIndex(0);
//                }
//
//                attackTimer.reset();
//            }
//        });
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
        forbes.update();

        forbes.render(renderer);
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

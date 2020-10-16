package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.FeatureSet;
import net.jibini.check.engine.LifeCycle;
import net.jibini.check.graphics.Window;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class TestGame implements CheckGame
{
    @EngineObject
    public Window window;

    @EngineObject
    public FeatureSet featureSet;

    @EngineObject
    public LifeCycle lifeCycle;

    private Texture gendrauTexture;

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
                .enable2DTextures();

        lifeCycle.registerTask(this::update);

        gendrauTexture = Texture.from(Resource.fromClasspath("fancy_square.gif"));
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
        gendrauTexture.bind();

        float baseX = gendrauTexture.getTextureCoordinates().getBaseX();
        float baseY = gendrauTexture.getTextureCoordinates().getBaseY();
        float dx = gendrauTexture.getTextureCoordinates().getDeltaX();
        float dy = gendrauTexture.getTextureCoordinates().getDeltaY();

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(baseX, baseY + dy);
        GL11.glVertex2f(-1.0f, -0.5f);
        GL11.glTexCoord2f(baseX + dx, baseY + dy);
        GL11.glVertex2f(0.0f, -0.5f);
        GL11.glTexCoord2f(baseX + dx, baseY);
        GL11.glVertex2f(0.0f, 0.5f);
        GL11.glTexCoord2f(baseX, baseY);
        GL11.glVertex2f(-1.0f, 0.5f);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(0.0f, -0.5f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(1.0f, -0.5f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(1.0f, 0.5f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(0.0f, 0.5f);
        GL11.glEnd();
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

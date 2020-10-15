package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.graphics.Window;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGame implements CheckGame
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @EngineObject
    public Window window;

    public static void main(String[] args)
    {
        Check.boot(new TestGame());

        Check.infinitelyPoll();
    }

    @Override
    public void start()
    {
        log.info("APPLICATION START");

        GL11.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void update()
    {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glLoadIdentity();

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(1, 0);
        GL11.glVertex2f(1, 1);
        GL11.glVertex2f(0, 1);
        GL11.glEnd();
    }

    @Override
    public void destroy()
    {
        log.info("APPLICATION DESTROY");
    }

    @NotNull
    @Override
    public Profile getProfile()
    {
        return new CheckGame.Profile()
        {
            @Override
            public boolean getContextForwardCompat()
            {
                return false;
            }

            @Override
            public boolean getContextCore()
            {
                return false;
            }

            @Override
            public int getContextVersion()
            {
                return 20;
            }

            @NotNull
            @Override
            public String getAppVersion()
            {
                return "0.0";
            }

            @NotNull
            @Override
            public String getAppName()
            {
                return "Test Game";
            }
        };
    }
}

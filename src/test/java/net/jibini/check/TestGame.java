package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.FeatureSet;
import net.jibini.check.engine.LifeCycle;
import net.jibini.check.graphics.Window;
import org.jetbrains.annotations.NotNull;

public class TestGame implements CheckGame
{
    @EngineObject
    public Window window;

    @EngineObject
    public FeatureSet featureSet;

    @EngineObject
    public LifeCycle lifeCycle;

    public static void main(String[] args)
    {
        Check.boot(new TestGame());

        Check.infinitelyPoll();
    }

    @Override
    public void start()
    {
        featureSet.enableDepthTest()
                .enable2DTextures();

        lifeCycle.registerTask(this::update);
    }

    public void update()
    {

    }

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

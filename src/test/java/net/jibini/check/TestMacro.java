package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.Macro;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.world.GameWorld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterObject
public class TestMacro implements Macro
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @EngineObject
    private GameWorld world;

    @Override
    public void action()
    {
        log.info("Test macro invoked! The world is set to '" + world + "'.");
    }
}

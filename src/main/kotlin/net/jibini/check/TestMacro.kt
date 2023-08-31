package net.jibini.check

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.Macro
import net.jibini.check.engine.RegisterObject
import net.jibini.check.world.GameWorld
import org.slf4j.LoggerFactory

@RegisterObject
class TestMacro : Macro
{
    private val log = LoggerFactory.getLogger(this.javaClass)

    @EngineObject
    private val world: GameWorld? = null
    override fun action()
    {
        log.info("Test macro invoked! The world is set to '$world'.")
    }
}
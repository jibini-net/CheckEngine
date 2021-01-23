package net.jibini.check.editor

import net.jibini.check.Check
import net.jibini.check.CheckGame
import net.jibini.check.world.impl.WorldFile

import org.slf4j.LoggerFactory

fun main()
{
    Check.boot(WorldEditor())
    Check.infinitelyPoll()
}

class WorldEditor : CheckGame
{
    private val log = LoggerFactory.getLogger(this::class.java)

    var current: WorldFile? = null
        set(value)
        {
            field = value

            log.info("A new world file descriptor has been set; updating . . .")
            onCurrentChanged()
        }

    override fun initialize()
    {

    }

    override fun update()
    {

    }

    private fun onCurrentChanged()
    {

    }

    override val profile = CheckGame.Profile(
        "Check World Editor",
        "1.0.0-SNAPSHOT"
    )
}
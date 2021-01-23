package net.jibini.check.world.impl

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.world.GameWorld

@RegisterObject
class WorldFileLoadImpl
{
    @EngineObject
    private lateinit var gameWorld: GameWorld

    fun load(worldFile: WorldFile)
    {

    }
}
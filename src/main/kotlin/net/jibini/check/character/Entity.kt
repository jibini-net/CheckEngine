package net.jibini.check.character

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer

abstract class Entity(
    var x: Double = 0.0,
    var y: Double = 0.0
) : EngineAware()
{
    @EngineObject
    protected lateinit var renderer: Renderer

    abstract fun update()
}
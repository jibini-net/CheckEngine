package net.jibini.check.character

import net.jibini.check.graphics.Renderer

abstract class Entity(
    var x: Double = 0.0,
    var y: Double = 0.0
)
{
    abstract fun render(renderer: Renderer)

    abstract fun update()
}
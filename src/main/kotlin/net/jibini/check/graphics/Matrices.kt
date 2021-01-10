package net.jibini.check.graphics

import net.jibini.check.engine.RegisterObject
import org.joml.Matrix4fStack

@RegisterObject
class Matrices
{
    val projection = Matrix4fStack(16)

    val model = Matrix4fStack(16)
}
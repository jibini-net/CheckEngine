package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import org.lwjgl.opengles.GLES30

@RegisterObject
class VAOBindOnBootImpl : Initializable
{
    override fun initialize()
    {
        val vertexArray = GLES30.glGenVertexArrays()
        GLES30.glBindVertexArray(vertexArray)
    }
}
package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject

import org.lwjgl.opengles.GLES30

/**
 * Binds the default VAO object on game boot. This may be required by
 * the OpenGL 4.3 Core profile.
 *
 * @author Zach Goethel
 */
@RegisterObject
class VAOBindOnBootImpl : Initializable
{
    override fun initialize()
    {
        val vertexArray = GLES30.glGenVertexArrays()
        GLES30.glBindVertexArray(vertexArray)
    }
}
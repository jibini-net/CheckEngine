package net.jibini.check.graphics

import net.jibini.check.engine.RegisterObject

import org.joml.Matrix4fStack

/**
 * A collection of render matrices which apply projection and
 * transformations to rendered elements.
 *
 * @author Zach Goethel
 */
@RegisterObject
class Matrices
{
    /**
     * The projection matrix stack. Modify this matrix to change the
     * orthographic or perspective projection of the engine.
     *
     * _If the stack is pushed, do not forget to pop the stack._
     */
    val projection = Matrix4fStack(4096)

    /**
     * The model transform matrix stack. Modify this matrix to change the
     * translation, rotation, and scale of the engine.
     *
     * _If the stack is pushed, do not forget to pop the stack._
     */
    val model = Matrix4fStack(4096)
}
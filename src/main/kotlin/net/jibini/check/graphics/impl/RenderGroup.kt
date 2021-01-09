package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer
import org.lwjgl.opengles.GLES30

class RenderGroup : Pointer<Int> by PointerImpl(GLES30.glGenVertexArrays())
{
    fun finalize()
    {

    }
}
package net.jibini.check.graphics

import net.jibini.check.graphics.impl.PointerImpl
import org.lwjgl.opengles.GLES30

class Framebuffer: Pointer<Int> by PointerImpl(GLES30.glGenFramebuffers())
{
}
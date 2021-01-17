package net.jibini.check.graphics.impl

import net.jibini.check.graphics.Pointer

/**
 * A simple generic numerical graphical object pointer.
 *
 * @author Zach Goethel
 */
class PointerImpl<T : Number>(override val pointer: T) : Pointer<T>
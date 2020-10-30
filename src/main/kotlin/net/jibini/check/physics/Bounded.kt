package net.jibini.check.physics

interface Bounded
{
    val boundingBox: BoundingBox
}

class BoundedImpl(
    override val boundingBox: BoundingBox
) : Bounded
package net.jibini.check.engine

import net.jibini.check.annotation.EngineObject
import net.jibini.check.engine.impl.EngineObjects
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestClassWithEngineObjects
{
    @EngineObject
    private lateinit var value: String

    fun doAssert()
    {
        Assertions.assertEquals("Hello, world!", value)
    }
}

class TestEngineObjects
{
    @Test
    fun engineObjectPlacementInKClass()
    {
        val placeIn = TestClassWithEngineObjects()
        EngineObjects.placeInstance("Hello, world!", placeIn)

        placeIn.doAssert()
    }
}
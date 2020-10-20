package net.jibini.check.engine

import net.jibini.check.engine.impl.EngineObjectsImpl
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@RegisterObject
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
        EngineObjectsImpl.placeInstance("Hello, world!", placeIn)

        placeIn.doAssert()
    }

    @Test
    fun engineObjectsAutoInit()
    {
        EngineObjectsImpl.objects += "Hello, world!"
        EngineObjectsImpl.initialize()

        EngineObjectsImpl.get<TestClassWithEngineObjects>()[0].doAssert()
    }
}
package net.jibini.check.engine;

import net.jibini.check.engine.impl.EngineObjectsImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestEngineObjectsJVM
{
    @RegisterObject
    public static class TestClassWithEngineObjects
    {
        @EngineObject
        private String value;

        public void doAssert()
        {
            Assertions.assertEquals("Hello, world!", value);
        }
    }

    @Test
    public void engineObjectPlacementInClass()
    {
        TestClassWithEngineObjects placeIn = new TestClassWithEngineObjects();
        EngineObjectsImpl.placeInstance("Hello, world!", placeIn);

        placeIn.doAssert();
    }
}

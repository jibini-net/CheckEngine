package net.jibini.check.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TestResources
{
    private void doAssert(Resource resource)
    {
        String contents = resource.getTextContents();

        Assertions.assertEquals("Hello, world!\n", contents);
    }

    @Test
    public void resourceFromStream() throws FileNotFoundException
    {
        doAssert(Resource.from(new FileInputStream("src/test/resources/test.txt")));
    }

    @Test
    public void resourceFromFile()
    {
        doAssert(Resource.fromFile("src/test/resources/test.txt"));
    }

    @Test
    public void resourceFromClasspath()
    {
        doAssert(Resource.fromClasspath("test.txt"));
    }
}

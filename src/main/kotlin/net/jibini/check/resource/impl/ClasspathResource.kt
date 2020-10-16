package net.jibini.check.resource.impl

import net.jibini.check.resource.Resource
import java.io.FileNotFoundException
import java.io.InputStream

class ClasspathResource(
    private val path: String
) : Resource()
{
    private val _stream = ClasspathResource::class.java.classLoader
        .getResourceAsStream(path)

    override val stream: InputStream
        get()
        {
            if (_stream == null)
                throw FileNotFoundException("Could not find classpath resource '$path'")

            return _stream
        }
}
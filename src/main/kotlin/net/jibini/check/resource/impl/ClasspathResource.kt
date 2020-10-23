package net.jibini.check.resource.impl

import net.jibini.check.resource.Resource
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * A resource stored on the classpath (e.g. within the JAR or a dependency's JAR)
 *
 * @author Zach Goethel
 */
class ClasspathResource(
    /**
     * Absolute resource path from the root of the classpath
     */
    private val path: String,

    override val uniqueIdentifier: String = "CLASSPATH; $path"
) : Resource()
{
    // Open classpath resource stream
    private val _stream = ClasspathResource::class.java.classLoader
        .getResourceAsStream(path)

    override val stream: InputStream
        get()
        {
            // Check if classpath stream is null
            if (_stream == null)
                throw FileNotFoundException("Could not find classpath resource '$path'")

            return _stream
        }
}
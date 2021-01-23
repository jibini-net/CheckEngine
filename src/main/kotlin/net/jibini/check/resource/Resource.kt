package net.jibini.check.resource

import net.jibini.check.resource.impl.ClasspathResource
import net.jibini.check.resource.impl.FileResource
import net.jibini.check.resource.impl.ResourceImpl

import java.io.File
import java.io.InputStream

/**
 * Any resource which can be accessed via a stream.
 *
 * @author Zach Goethel
 */
abstract class Resource
{
    /**
     * Stream providing access to the resource.
     */
    abstract val stream: InputStream

    /**
     * A unique identifier which can be used to cache resources.
     */
    abstract val uniqueIdentifier: String

    /**
     * Reads the entirety of the file as if it were text.
     */
    val textContents: String
        get() = stream
            .bufferedReader()
            .readText()

    companion object
    {
        /**
         * Creates a resource from the given file.
         */
        @JvmStatic
        fun fromFile(file: File): Resource = FileResource(file)

        /**
         * Creates a resource from the file at the given path.
         */
        @JvmStatic
        fun fromFile(path: String) = this.fromFile(File(path))

        /**
         * Creates a resource on the classpath at the given path.
         */
        @JvmStatic
        fun fromClasspath(path: String): Resource = ClasspathResource(path)

        /**
         * Creates a resource from the given stream.
         */
        @JvmStatic
        fun from(stream: InputStream): Resource = ResourceImpl(stream)
    }
}
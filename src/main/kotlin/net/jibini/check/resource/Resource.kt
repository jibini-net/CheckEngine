package net.jibini.check.resource

import net.jibini.check.resource.impl.ClasspathResource
import net.jibini.check.resource.impl.FileResource
import net.jibini.check.resource.impl.ResourceImpl
import java.io.File
import java.io.InputStream
import java.lang.StringBuilder

/**
 * Any resource which can be accessed via a stream
 *
 * @author Zach Goethel
 */
abstract class Resource
{
    abstract val stream: InputStream

    abstract val uniqueIdentifier: String

    /**
     * Reads the entirety of the file as if it were text
     */
    val textContents: String
        get()
        {
            // Prepare to read all lines
            val stringBuilder = StringBuilder()
            val reader = stream.bufferedReader()

            // Read first line
            var line = reader.readLine()

            // Loop until line is null
            while (line != null)
            {
                // Append line value
                stringBuilder.append(line)
                    .append('\n')

                // Read next line
                line = reader.readLine()
            }

            return stringBuilder.toString()
        }

    companion object
    {
        /**
         * Creates a resource from the given file
         */
        @JvmStatic
        fun fromFile(file: File): Resource = FileResource(file)

        /**
         * Creates a resource from the file at the given path
         */
        @JvmStatic
        fun fromFile(path: String) = this.fromFile(File(path))

        /**
         * Creates a resource on the classpath at the given path
         */
        @JvmStatic
        fun fromClasspath(path: String): Resource = ClasspathResource(path)

        /**
         * Creates a resource from the given stream
         */
        @JvmStatic
        fun from(stream: InputStream): Resource = ResourceImpl(stream)
    }
}
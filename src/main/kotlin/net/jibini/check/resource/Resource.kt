package net.jibini.check.resource

import net.jibini.check.resource.impl.ClasspathResource
import net.jibini.check.resource.impl.FileResource
import net.jibini.check.resource.impl.ResourceImpl
import java.io.File
import java.io.InputStream
import java.lang.StringBuilder

abstract class Resource
{
    abstract val stream: InputStream

    val textContents: String
        get()
        {
            val stringBuilder = StringBuilder()
            val reader = stream.bufferedReader()

            var line = reader.readLine()

            while (line != null)
            {
                stringBuilder.append(line)
                    .append('\n')

                line = reader.readLine()
            }

            return stringBuilder.toString()
        }

    companion object
    {
        @JvmStatic
        fun fromFile(file: File): Resource = FileResource(file)

        @JvmStatic
        fun fromFile(path: String) = this.fromFile(File(path))

        @JvmStatic
        fun fromClasspath(path: String): Resource = ClasspathResource(path)

        @JvmStatic
        fun from(stream: InputStream): Resource = ResourceImpl(stream)
    }
}
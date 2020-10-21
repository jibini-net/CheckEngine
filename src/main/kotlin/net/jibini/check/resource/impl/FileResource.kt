package net.jibini.check.resource.impl

import net.jibini.check.resource.Resource
import java.io.File
import java.io.FileInputStream

/**
 * A resource which is stored in a file
 *
 * @author Zach Goethel
 */
class FileResource(
    /**
     * File pointing to the resource
     */
    file: File
) : Resource()
{
    // Open file resource stream
    override val stream = FileInputStream(file)
}
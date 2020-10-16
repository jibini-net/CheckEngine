package net.jibini.check.resource.impl

import net.jibini.check.resource.Resource
import java.io.File
import java.io.FileInputStream

class FileResource(
    file: File
) : Resource()
{
    override val stream = FileInputStream(file)
}
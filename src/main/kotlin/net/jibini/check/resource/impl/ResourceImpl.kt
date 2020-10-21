package net.jibini.check.resource.impl

import net.jibini.check.resource.Resource
import java.io.InputStream

/**
 * A resource which is accessible through the given stream
 */
class ResourceImpl(override val stream: InputStream) : Resource()
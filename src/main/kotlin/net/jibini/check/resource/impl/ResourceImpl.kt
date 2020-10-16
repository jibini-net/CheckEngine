package net.jibini.check.resource.impl

import net.jibini.check.resource.Resource
import java.io.InputStream

class ResourceImpl(override val stream: InputStream) : Resource()
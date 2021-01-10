package net.jibini.check.graphics.impl

import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader
import net.jibini.check.resource.Resource

@RegisterObject
class DirectTexShaderImpl : Initializable
{
    lateinit var shader: Shader

    override fun initialize()
    {
        shader = Shader.create(
            Resource.fromClasspath("shaders/textured.vert"),
            Resource.fromClasspath("shaders/textured.frag")
        )
    }
}
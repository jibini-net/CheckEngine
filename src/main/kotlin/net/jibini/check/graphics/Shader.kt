package net.jibini.check.graphics

import net.jibini.check.graphics.impl.ShaderImpl
import net.jibini.check.graphics.impl.ShaderProgramImpl
import net.jibini.check.resource.Resource
import org.lwjgl.opengles.GLES30

interface Shader
{
    fun use()

    fun uniform(name: String, x: Int)

    fun uniform(name: String, x: Float, y: Float)

    companion object
    {
        @JvmStatic
        fun create(vertex: Resource, fragment: Resource): Shader
        {
            val vertShader = ShaderImpl.create(GLES30.GL_VERTEX_SHADER, vertex.textContents)
            val fragShader = ShaderImpl.create(GLES30.GL_FRAGMENT_SHADER, fragment.textContents)

            val result = ShaderProgramImpl()

            result.attach(vertShader)
            vertShader.destroy()
            result.attach(fragShader)
            fragShader.destroy()

            result.link()

            return result
        }
    }
}
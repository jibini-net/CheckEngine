package net.jibini.check.graphics

import net.jibini.check.graphics.impl.ShaderImpl
import net.jibini.check.graphics.impl.ShaderProgramImpl
import net.jibini.check.resource.Resource

import org.joml.Matrix4f

import org.lwjgl.opengles.GLES30

/**
 * A shader program which can shade render data or compute rays.
 *
 * @author Zach Goethel
 */
interface Shader
{
    /**
     * Binds the shader program. Render calls performed while this
     * shader is bound will be processed through this shader pipeline.
     */
    fun use()

    /**
     * Sets the named uniform with the given values.
     *
     * @param name Name of the uniform.
     */
    fun uniform(name: String, x: Int)

    /**
     * Sets the named uniform with the given values.
     *
     * @param name Name of the uniform.
     */
    fun uniform(name: String, x: Float)

    /**
     * Sets the named uniform with the given values.
     *
     * @param name Name of the uniform.
     */
    fun uniform(name: String, x: Float, y: Float)

    /**
     * Sets the named uniform with the given values.
     *
     * @param name Name of the uniform.
     */
    fun uniform(name: String, x: Float, y: Float, z: Float)

    /**
     * Sets the named uniform with the given values.
     *
     * @param name Name of the uniform.
     */
    fun uniform(name: String, x: Float, y: Float, z: Float, w: Float)

    /**
     * Sets the named uniform with the given values.
     *
     * @param name Name of the uniform.
     */
    fun uniform(name: String, matrix: Matrix4f)

    companion object
    {
        /**
         * Generates an OpenGL shader program with the given vertex and
         * fragment shaders. Each shader will be sourced, compiled,
         * and attached. Shaders are deleted after the program is
         * successfully linked.
         *
         * @param vertex Resource pointing to the vertex shader source.
         * @param fragment Resource pointing to the fragment shader
         *     source.
         * @return Generated shader program which is compiled and
         *     linked.
         */
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
            result.verify()

            return result
        }
    }
}

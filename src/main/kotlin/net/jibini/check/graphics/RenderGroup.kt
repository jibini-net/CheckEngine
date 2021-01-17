package net.jibini.check.graphics

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.impl.StatefulShaderImpl
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengles.GLES30
import java.nio.Buffer

/**
 * A collection of vertices which share a common texture and will be
 * rendered together. This is useful for mass rendering of identical
 * tiles.
 *
 * Each render group can be rendered with one OpenGL array draw.
 *
 * @author Zach Goethel
 */
class RenderGroup : EngineAware(), Destroyable
{
    // Required to set the matrix uniforms of the current shader
    @EngineObject
    private lateinit var statefulShader: StatefulShaderImpl

    // Required to access the current transformation matrices
    @EngineObject
    private lateinit var matrices: Matrices

    /**
     * Whether this render group has been finalized. A render group
     * should not be added to after it is finalized.
     */
    private var finalized = false

    /**
     * A mutable list of all vertices added to this render group prior
     * to finalizing.
     */
    private var vertexList: MutableList<Vector3f>? = mutableListOf()

    /**
     * A mutable list of all color data added to this render group prior
     * to finalizing.
     */
    private var colorList: MutableList<Vector4f>? = mutableListOf()

    /**
     * A mutable list of all tex coords added to this render group prior
     * to finalizing.
     */
    private var texCoordList: MutableList<Vector2f>? = mutableListOf()

    /**
     * The generated OpenGL buffer for vertex data.
     */
    private val vertexBuffer = GLES30.glGenBuffers()

    /**
     * The generated OpenGL buffer for color data.
     */
    private val colorBuffer = GLES30.glGenBuffers()

    /**
     * The generated OpenGL buffer for texture data.
     */
    private val texCoordBuffer = GLES30.glGenBuffers()

    /**
     * Number of vertices in this render group. It is also the number of
     * triangles times three.
     */
    private var size = 0

    companion object
    {
        /**
         * Stored matrix to check if the model matrix has changed.
         */
        private var lastPushedModelMatrix: Matrix4f? = null

        /**
         * Stored matrix to check if the projection matrix has changed.
         */
        private var lastPushedProjectionMatrix: Matrix4f? = null

        /**
         * Stored shader to check if the bound shader has changed.
         */
        private var lastPushedShader: Shader? = null
    }

    /**
     * Builds the OpenGL buffers for the render group. After finalized,
     * additional vertices cannot be added to this group.
     */
    fun finalize()
    {
        val vertexData = BufferUtils.createFloatBuffer(vertexList!!.size * 3)
        val colorData = BufferUtils.createFloatBuffer(colorList!!.size * 4)
        val texCoordData = BufferUtils.createFloatBuffer(texCoordList!!.size * 2)

        for (vertex in vertexList!!)
        {
            vertexData.put(vertex.x)
            vertexData.put(vertex.y)
            vertexData.put(vertex.z)
        }

        for (color in colorList!!)
        {
            colorData.put(color.x)
            colorData.put(color.y)
            colorData.put(color.z)
            colorData.put(color.w)
        }

        for (texCoord in texCoordList!!)
        {
            texCoordData.put(texCoord.x)
            texCoordData.put(texCoord.y)
        }

        // Cast to Buffer for JDK 9+ support
        (vertexData as Buffer).flip()
        (colorData as Buffer).flip()
        (texCoordData as Buffer).flip()

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, colorBuffer)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colorData, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, texCoordBuffer)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoordData, GLES30.GL_STATIC_DRAW)

        size = vertexList!!.size

        // Remove references to list so they can be GCed
        vertexList = null
        colorList = null
        texCoordList = null
        // Mask as finalized
        finalized = true
    }

    /**
     * Performs an OpenGL array render call for the generated buffers.
     * If the group is not yet [finalized][finalize], it will be
     * finalized.
     */
    fun call()
    {
        if (!finalized)
            finalize()

        if (
            lastPushedModelMatrix != matrices.model
                || lastPushedProjectionMatrix != matrices.projection
                || lastPushedShader != statefulShader.boundShader
        )
        {
            val combinationMatrix = Matrix4f()
            matrices.projection.mul(matrices.model, combinationMatrix)
            statefulShader.boundShader?.uniform("uniform_matrix", combinationMatrix)

            lastPushedModelMatrix = Matrix4f(matrices.model)
            lastPushedProjectionMatrix = Matrix4f(matrices.projection)
            lastPushedShader = statefulShader.boundShader
        }

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, colorBuffer)
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glEnableVertexAttribArray(2)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, texCoordBuffer)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 0, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, size)

        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDisableVertexAttribArray(2)
    }

    override fun destroy()
    {
        GLES30.glDeleteBuffers(vertexBuffer)
        GLES30.glDeleteBuffers(colorBuffer)
        GLES30.glDeleteBuffers(texCoordBuffer)
    }

    /**
     * Adds the given vertex data to the render group.
     *
     * @throws NullPointerException If the render group has already been
     *     finalized. No additional geometry
     *     can be added after finalization.
     */
    fun consumeVertex(vertex: Vector3f, color: Vector4f, texCoord: Vector2f)
    {
        vertexList!! += vertex
        colorList!! += color
        texCoordList!! += texCoord
    }
}
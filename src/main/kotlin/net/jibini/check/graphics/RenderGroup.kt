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

class RenderGroup : EngineAware(), Destroyable
{
    @EngineObject
    private lateinit var statefulShaderImpl: StatefulShaderImpl

    @EngineObject
    private lateinit var matrices: Matrices

    private var finalized = false

    private var vertexList: MutableList<Vector3f>? = mutableListOf()
    private var colorList: MutableList<Vector4f>? = mutableListOf()
    private var texCoordList: MutableList<Vector2f>? = mutableListOf()

    private val vertexBuffer = GLES30.glGenBuffers()
    private val colorBuffer = GLES30.glGenBuffers()
    private val texCoordBuffer = GLES30.glGenBuffers()

    private var size = 0

    private var lastPushedModelMatrix: Matrix4f? = null
    private var lastPushedProjectionMatrix: Matrix4f? = null

    private var lastPushedShader: Shader? = null

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

        vertexData.flip()
        colorData.flip()
        texCoordData.flip()

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffer)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexData, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, colorBuffer)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colorData, GLES30.GL_STATIC_DRAW)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, texCoordBuffer)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoordData, GLES30.GL_STATIC_DRAW)

        size = vertexList!!.size

        vertexList = null
        colorList = null
        texCoordList = null

        finalized = true
    }

    fun call()
    {
        if (!finalized)
            finalize()

        if (
            lastPushedModelMatrix != matrices.projection
                || lastPushedProjectionMatrix != matrices.projection
                || lastPushedShader != statefulShaderImpl.boundShader
        )
        {
            val combinationMatrix = Matrix4f()
            matrices.projection.mul(matrices.model, combinationMatrix)
            statefulShaderImpl.boundShader?.uniform("uniform_matrix", combinationMatrix)

            lastPushedModelMatrix = Matrix4f(matrices.model)
            lastPushedProjectionMatrix = Matrix4f(matrices.projection)
            lastPushedShader = statefulShaderImpl.boundShader
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

    fun consumeVertex(vertex: Vector3f, color: Vector4f, texCoord: Vector2f)
    {
        vertexList!! += vertex
        colorList!! += color
        texCoordList!! += texCoord
    }
}
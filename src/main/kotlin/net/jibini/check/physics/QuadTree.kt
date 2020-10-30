package net.jibini.check.physics

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer
import net.jibini.check.texture.impl.TextureRegistry
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class QuadTree<E : Bounded>(
    x: Double,
    y: Double,

    width: Double,
    height: Double
) : EngineAware()
{
    companion object
    {
        const val MAX_BUCKET_CAPACITY = 2

        const val MIN_BUCKET_SIZE = 0.1
    }

    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var textureRegistry: TextureRegistry

    private val rootNode = QuadTreeNode<E>(null, x, y, width, height)

    fun place(value: E): Boolean = rootNode.place(value)

    fun render()
    {
        textureRegistry.unbind()

        rootNode.render(renderer)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
    }

    fun reevaluate()
    {
        rootNode.reevaluate()
    }

    private class QuadTreeNode<E : Bounded>(
        var parent: QuadTreeNode<E>?,

        val x: Double,
        val y: Double,

        val width: Double,
        val height: Double
    ) : EngineAware()
    {
        val boundingBox = BoundingBox(x, y, width, height)

        val children = Array<QuadTreeNode<E>?>(4) { null }
        val bucket = CopyOnWriteArrayList<E>()

        var branched = false

        fun place(value: E): Boolean
        {
            // Return early if the value should not be in this node
            if (!boundingBox.overlaps(value.boundingBox))
                return false

            if (bucket.contains(value))
                return false

            if ((bucket.size < MAX_BUCKET_CAPACITY
                // Ignore max capacity if this node is the minimum size
                || width < MIN_BUCKET_SIZE
                || height < MIN_BUCKET_SIZE)
                // Don't add to this bucket if the node has been branched
                && !branched)
            {
                // Add the value to this node's bucket
                bucket += value
                // Return true as the object is contained in this node
                return true
            }

            // Make sure the children nodes are initialized
            if (!branched)
                branch()
            // Recursively place the object
            for (child in children)
                child?.place(value)

            // Return true as the object is contained somewhere in this node's children
            return true
        }

        fun render(renderer: Renderer)
        {
            GL11.glColor4f(
                if (bucket.size == 1) 1.0f else 0.0f,
                if (bucket.size == 2) 1.0f else 0.0f,
                if (bucket.size >= 3) 1.0f else 0.0f,

                0.5f
            )

            renderer.drawRectangle(
                x.toFloat(),
                y.toFloat(),
                width.toFloat(),
                0.005f * bucket.size
            )

            renderer.drawRectangle(
                x.toFloat(),
                y.toFloat() + height.toFloat() - 0.005f * bucket.size,
                width.toFloat(),
                0.005f * bucket.size
            )

            renderer.drawRectangle(
                x.toFloat(),
                y.toFloat(),
                0.005f * bucket.size,
                height.toFloat()
            )

            renderer.drawRectangle(
                x.toFloat() + width.toFloat() - 0.005f * bucket.size,
                y.toFloat(),
                0.005f * bucket.size,
                height.toFloat()
            )

            GL11.glTranslatef(0.0f, 0.0f, 0.2f)

            if (bucket.find { it.boundingBox.overlaps(boundingBox) } != null)
                renderer.drawRectangle(
                    x.toFloat(),
                    y.toFloat(),
                    width.toFloat(),
                    height.toFloat()
                )

            GL11.glTranslatef(0.0f, 0.0f, -0.2f)

            for (child in children)
                child?.render(renderer)
        }

        fun reevaluate()
        {
            if (branched)
            {
                var nodesWithChildren = 0
                var ignore = false

                for (child in children)
                {
                    child?.reevaluate()

                    if (child?.branched == true)
                        ignore = true
                    if (child?.bucket?.size ?: 0 > 0)
                        nodesWithChildren++
                }

                if (!ignore && nodesWithChildren <= 1)
                    consume()
            } else if (bucket.size > 0)
                for (i in bucket.size - 1 downTo 0)
                {
                    if (!boundingBox.overlaps(bucket[i].boundingBox))
                        bucket.removeAt(i)
                }
        }

        fun branch()
        {
            // Iterate through and create child nodes
            for (i in children.indices)
            {
                // Calculate x and y start coordinates
                val childX = x + (i % 2).toDouble() * (width  / 2)
                val childY = y + (i / 2).toDouble() * (height / 2)
                // Spawn the node in the array
                children[i] = QuadTreeNode(this, childX, childY, width / 2, height / 2)

                // Move the values in this bucket to children's buckets
                for (value in bucket)
                    children[i]?.place(value)
            }

            // Clear this node's bucket
            bucket.clear()

            branched = true
        }

        fun consume()
        {
            val toReplace = LinkedList<E>()

            for (i in children.indices)
            {
                toReplace.addAll(children[i]?.bucket ?: listOf())

                children[i] = null
            }

            bucket.addAll(toReplace)

            branched = false
        }
    }
}
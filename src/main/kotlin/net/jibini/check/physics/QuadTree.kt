package net.jibini.check.physics

import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer
import net.jibini.check.texture.impl.TextureRegistry
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 */
class QuadTree<E : Bounded>(
    x: Double,
    y: Double,

    width: Double,
    height: Double
) : EngineAware()
{
    companion object
    {
        const val MAX_BUCKET_CAPACITY = 1

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
            if (!boundingBox.contains(value.boundingBox))
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

            // Track if any child can hold the value
            var homeFound = false
            // Recursively place the object
            for (child in children)
                if (child?.place(value) == true)
                {
                    // A child can hold the value
                    homeFound = true
                    // Break out of the loop as only one child can hold
                    break
                }

            // No home was found, so keep it in this node's bucket
            if (!homeFound)
                bucket += value

            // Return true as the object is contained somewhere in this node's children
            return true
        }

        fun render(renderer: Renderer)
        {
            GL11.glColor3f(
                if (bucket.size == 1) 1.0f else 0.0f,
                if (bucket.size == 2) 1.0f else 0.0f,
                if (bucket.size >= 3) 1.0f else 0.0f
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

            for (child in children)
                child?.render(renderer)
        }

        fun reevaluate()
        {
            for (i in bucket.size - 1 downTo 0)
            {
                val value = bucket[i]

                if (!boundingBox.contains(value.boundingBox))
                {
                    if (parent?.bucket?.add(value) != null)
                        bucket.removeAt(i)

                    continue
                }

                for (child in children)
                    if (child?.place(value) == true)
                    {
                        bucket.removeAt(i)

                        break
                    }
            }

            if (bucket.size > MAX_BUCKET_CAPACITY && !branched)
                branch()

            val uniqueChildren = mutableListOf<E>()
            var ignore = false

            for (child in children)
            {
                child?.reevaluate()

                if (child?.branched == true)
                    ignore = true
                else if (child?.bucket?.size ?: 0 > 0)
                {
                    for (subChild in child?.bucket!!)
                        if (!uniqueChildren.contains(subChild))
                            uniqueChildren.add(subChild)
                }
            }

            if (!ignore && uniqueChildren.size + bucket.size <= MAX_BUCKET_CAPACITY)
                consume()
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
                for (j in bucket.size - 1 downTo 0)
                {
                    val value = bucket[j]

                    if (!boundingBox.contains(value.boundingBox))
                    {
                        if (parent?.bucket?.add(value) != null)
                            bucket.removeAt(j)

                        continue
                    }
                }
            }

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
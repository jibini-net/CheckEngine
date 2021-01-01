package net.jibini.check.physics

import kotlinx.coroutines.*
import net.jibini.check.engine.EngineAware
import net.jibini.check.engine.EngineObject
import net.jibini.check.graphics.Renderer
import net.jibini.check.texture.impl.TextureRegistry
import org.lwjgl.opengl.GL11
import java.util.*

/**
 * A branched tree structure where each node has four child nodes; nodes are given a two-dimensional bounding box for
 * within which they are responsible for any elements, and each node has a bucket of elements held within its bounds.
 *
 * The tree will attempt to allocate each element with the largest possible node where that element is the only element
 * contained within the node.  If such a node cannot be created, multiple elements will exist in one node, which
 * indicates a possible overlapping of elements.
 *
 * Upon querying the tree for possible collisions with a bounded area, collisions will be detected between the given
 * bounded area, any elements in the node where that area would be placed, any parent nodes to that node, and any
 * child nodes of that node.
 *
 * @author Zach Goethel
 */
class QuadTree<E : Bounded>(
    /**
     * The base x-coordinate of the quad-tree
     */
    x: Double,
    /**
     * The base y-coordinate of the quad-tree
     */
    y: Double,

    /**
     * The width of the quad-tree's head node
     */
    width: Double,
    /**
     * The height of the quad-tree's head node
     */
    height: Double
) : EngineAware()
{
    companion object
    {
        /**
         * When possible, limit the number of elements in each node to this amount; branch out nodes if this number is
         * exceeded
         */
        const val MAX_BUCKET_CAPACITY = 1

        /**
         * Do not branch out a node if it would create nodes smaller than this size
         */
        const val MIN_BUCKET_SIZE = 0.1

        /**
         * The width of the array of child nodes; the number of child nodes will be the perfect square created by
         * multiplying this number by itself
         */
        const val CHILD_ARRAY_WIDTH = 2
    }

    @EngineObject
    private lateinit var renderer: Renderer

    @EngineObject
    private lateinit var textureRegistry: TextureRegistry

    fun render()
    {
        textureRegistry.unbind()

        rootNode.render(renderer)
        GL11.glColor3f(1.0f, 1.0f, 1.0f)
    }

    /**
     * The head node covering the entire x-y-plane of the quad-tree
     */
    private val rootNode = QuadTreeNode<E>(null, x, y, width, height)

    /**
     * Recursively places the given element within the tree in the largest available node which will fit the element
     * completely; if that node is at its maximum capacity, it may be branched out
     *
     * @param value The [bounded][Bounded] element to place in the tree
     *
     * @return Whether the element was placed (and therefore belongs) within this node or any of its children
     */
    fun place(value: E): Boolean = rootNode.place(value)

    /**
     * Recursively checks the placement of all elements in all nodes to determine if any nodes should be reorganized.
     *
     * Each element will only be moved one node up or down per call.  If an element no longer belongs within a node, it
     * will be moved to that node's parent.  If an element can be moved into one of a node's child nodes, it will be
     * moved into that child node.
     */
    fun reevaluate()
    {
        rootNode.reevaluate()
    }

    /**
     * Iterates through unique combinations of two elements which may be colliding; no two pairs of bounded elements
     * will appear in two iterations.
     *
     * Thus `(u: E, v: E) == (v: E, u: E)` for the purposes of this iteration.
     */
    fun iteratePairs(action: (E, E) -> Unit)
    {
        rootNode.iteratePairs(action)
    }

    /**
     * Internal class representing a quadrant node
     */
    private class QuadTreeNode<E : Bounded>(
        var parent: QuadTreeNode<E>?,

        val x: Double,
        val y: Double,

        val width: Double,
        val height: Double
    )
    {
        /**
         * See [QuadTree.iteratePairs].
         */
        fun iteratePairs(action: (E, E) -> Unit)
        {
            if (branched)
            {
                for (child in children)
                    child?.iteratePairs(action)
            } else
            {
                var climb: QuadTreeNode<E>? = this

                while (climb != null)
                {
                    if (climb.bucket.size >= 2)
                        for ((i, element) in climb.bucket.subList(0, climb.bucket.size - 1).withIndex())
                        {
                            for (with in climb.bucket.subList(i + 1, climb.bucket.size))
//                            if (element != with)
                                    action(element, with)
                        }

                    climb.reactWithParents(action)
                    climb = climb.parent
                }
            }
        }

        /**
         * Calls the given lambda function with pairs of the current node crossed with the buckets of each parent of the
         * current node
         */
        fun reactWithParents(action: (E, E) -> Unit)
        {
            var climb: QuadTreeNode<E>? = this

            while (climb != null)
            {
                for (element in bucket)
                {
                    for (with in climb.bucket)
                        action(element, with)
                }

                climb = climb.parent
            }
        }

        /**
         * The bounded area within which any elements will be in this node's or one if its children's buckets
         */
        val boundingBox = BoundingBox(x, y, width, height)

        /**
         * All the child nodes of this node (initialized to null-pointers if not yet branched)
         */
        val children = Array<QuadTreeNode<E>?>(CHILD_ARRAY_WIDTH * CHILD_ARRAY_WIDTH) { null }

        /**
         * This node's contained values in a mutable list
         */
        val bucket: MutableList<E> = ArrayList(4)

        /**
         * Whether this node currently has any child nodes; initialized to false, set to true upon branching, and set to
         * false upon pruning
         */
        var branched = false

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

        /**
         * See [QuadTree.place].
         */
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

            // Make sure the child nodes are initialized
            if (!branched)
                branch()

            // Track if any child can hold the value
            var homeFound = false

            // Recursively place the object
            runBlocking {
                coroutineScope {

                    for (child in children)
                        launch {
                            if (child?.place(value) == true)
                            {
                                // A child can hold the value
                                homeFound = true
                            }
                        }

                }
            }

            // No home was found, so keep it in this node's bucket
            if (!homeFound)
                bucket += value

            // Return true as the object is contained somewhere in this node's children
            return true
        }

        /**
         * See [QuadTree.reevaluate].
         */
        fun reevaluate()
        {
            // Look through each item held within this node (in reverse order)
            for (i in bucket.size - 1 downTo 0)
            {
                val value = bucket[i]

                // If this element no longer belongs here, move it to the parent node
                if (!boundingBox.contains(value.boundingBox))
                {
                    // Move to the parent node if it is not null
                    if (parent?.bucket?.add(value) != null)
                        // If the parent node wasn't null, remove from this node
                        bucket.removeAt(i)

                    // Don't perform a child-placement test for this element
                    continue
                }

                // Attempt to place the value in one of this node's child nodes
                for (child in children)
                    if (child?.place(value) == true)
                    {
                        // If any placement succeeds, remove from this node
                        bucket.removeAt(i)

                        // And exit the loop
                        break
                    }
            }

            // Branch if the maximum capacity is reached
            if (bucket.size > MAX_BUCKET_CAPACITY && !branched)
                branch()

            for (child in children)
                    // Recursive call to reevaluate
                    child?.reevaluate()
        }

        /**
         * Branches out the quad-tree node into four child nodes; no changes are performed on this node's bucket
         */
        fun branch()
        {
            // Iterate through and create child nodes
            for (i in children.indices)
            {
                // Calculate x and y start coordinates
                val childX = x + (i % CHILD_ARRAY_WIDTH).toDouble() * (width  / CHILD_ARRAY_WIDTH)
                val childY = y + (i / CHILD_ARRAY_WIDTH).toDouble() * (height / CHILD_ARRAY_WIDTH)

                // Spawn the node in the array
                children[i] = QuadTreeNode(
                    this,
                    childX,
                    childY,
                    width / CHILD_ARRAY_WIDTH,
                    height / CHILD_ARRAY_WIDTH
                )
            }

            // Record that this node is now branched
            branched = true
        }

        /**
         * Deletes this node's child nodes and moves all elements held in their buckets to this node's bucket
         */
        fun prune()
        {
            runBlocking {
                coroutineScope {

                    for (i in children.indices)
                    {
                        launch {
                            // Recursively prune any branched child nodes
                            if (children[i]?.branched == true)
                                children[i]!!.prune()
                            // Add any elements in the child node to this node
                            bucket.addAll(children[i]?.bucket ?: listOf())

                            // Remove reference to child node
                            children[i] = null
                        }
                    }

                }
            }

            // Record that this node is no longer branched
            branched = false
        }
    }
}
package net.jibini.check.entity.behavior

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.timing.DeltaTimer
import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Entity
import net.jibini.check.entity.character.Player
import net.jibini.check.world.GameWorld
import net.jibini.check.world.Tile
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3i
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.LinkedBlockingDeque

/**
 * A path-finding behavior which targets the player as a destination.  This behavior maintains a vector field covering
 * the entire level, where each tile's vector points towards the most efficient path to the player.  In order to
 * navigate, the entity sums all of the vectors at all of its bounding-box corners as well as its center of mass.  The
 * vector is normalized and the entity walks in that direction.
 *
 * The implementation is a breadth-first search of all tiles to find the most efficient path to the player.  In order to
 * avoid stuttering, only 2048 tiles are processed per frame.  The search starts at the player's base coordinate.
 *
 * There is no major performance deficit with adding more entities with this behavior.  The vector field is calculated
 * once, and then all entities share the same vector field.  Additionally, to view the vectors in-game, hold the 'F4'
 * key for a rudimentary debug view of the vectors.
 *
 * The vectors will be of length one, facing upwards, leftwards, downwards, or rightwards (no diagonals).
 *
 * @author Zach Goethel
 */
@RegisterObject
class PlayerTargetBehavior : EntityBehavior()
{
    // Local class logger instance
    private val log = LoggerFactory.getLogger(this.javaClass)

    // Game world; required to access the tile array
    @EngineObject
    private var gameWorld: GameWorld? = null

    /**
     * A two-dimensional array of 2D vectors pointing to the most efficient path to the target (player); the third
     * element (z-coordinate) is the path length.
     */
    private var vectorField: Array<Array<Vector3i?>>? = null

    /**
     * A convenience reference to the world's tile array.  This reference is reset every update.
     */
    private var worldTiles: Array<Array<Tile?>>? = null

    /**
     * Non-resetting delta time which is used to update the vector field every refresh interval (0.3 seconds).
     */
    private val updateTimer = DeltaTimer(false)

    /**
     * Tracks the number of times each tile has been visited; this could be a boolean array, but that would require
     * an O(n^2) operation to reset the array.
     */
    private var worldVisitCount: Array<LongArray>? = null

    /**
     * The expected value in the visit-count array for an already-visited tile.
     */
    private var expected: Long = 0

    /**
     * Execution queue of the breadth-first search.  Tile coordinates are written to this queue, where the third element
     * (z-coordinate) is the length of the current path.
     *
     * Coordinates are written to this queue freely, but only a maximum of 2048 will be polled per update.
     */
    private val visitQueue: Deque<Vector3i> = LinkedBlockingDeque()

    /**
     * A convenience reference to the player instance.  This reference is reset every update.
     */
    var target: Player? = null

    /**
     * Performs a scheduled reset to the vector field.  This triggers the start of a new breadth-first search radiating
     * outwards from the player's base coordinates.
     *
     * This method validates that no other search is currently in progress.
     */
    private fun reset()
    {
        // Check if this was called too soon, or the last search hasn't yet finished
        if (updateTimer.delta < 0.3 || !visitQueue.isEmpty())
        {
            log.warn("Pathfinding update overran time limit or was re-triggered prematurely")
            return
        }

        // Find the base tile coordinate of the player's location
        val t = toTile(target!!.x, target!!.y)

        // Queue the base tile and mark it as visited
        visitQueue.add(Vector3i(t.x, t.y, 0))
        vectorField!![t.x][t.y] = Vector3i()
        worldVisitCount!![t.x][t.y] = ++expected

        // Reset the update timer; ensures the update will be called again
        updateTimer.reset()
    }

    /**
     * Visits the given tile coordinate, where the third element (z-coordinate) is the current path's accrued length.
     * By visiting a coordinate, this method sets its unvisited neighbors' vectors to point to this tile.
     *
     * The vector field will be updated if the neighbors are unvisited or this path is shorter than a previous visitor.
     *
     * @param location A 2D tile coordinate, with a third element (z-coordinate, path length).
     */
    private fun visit(location: Vector3i)
    {
        // Maintain potential neighbors which can access this tile
        val neighbors = arrayOf(
            Vector2i(location.x, location.y + 1),
            Vector2i(location.x, location.y - 1),
            Vector2i(location.x + 1, location.y),
            Vector2i(location.x - 1, location.y)
        )

        // Iterate through neighbors; check that the coordinates are valid and the neighbor should be updated (either
        // it is unvisited, or this path is shorter than a prior visitor)
        for (n in neighbors) if (validRange(n) && (worldVisitCount!![n.x][n.y] < expected || (vectorField!![n.x][n.y] != null
                    && location.z + 1 < vectorField!![n.x][n.y]!!.z))
        )
        {
            // Mark as visited if unvisited
            if (worldVisitCount!![n.x][n.y] < expected) worldVisitCount!![n.x][n.y] = expected

            // The vector pointing from the neighbor to the original tile
            val xy = Vector2i(location.x, location.y).sub(n)

            // Set the neighbor's vector field value and queue it
            vectorField!![n.x][n.y] = Vector3i(xy.x, xy.y, location.z + 1)
            visitQueue.add(Vector3i(n.x, n.y, location.z + 1))
        }
    }

    /**
     * Evaluates the tile coordinate queue and renders the vector field arrows (if applicable).
     */
    private fun evaluateField()
    {
        // Iterate through the queue until empty (max 2048 per frame)
        var i = 0
        while (i < 2048 && !visitQueue.isEmpty())
        {
            visit(visitQueue.poll())
            i++
        }
    }

    /**
     * @param n Neighbor coordinate to validate.  This can contain negative values, but negative values will be invalid.
     *
     * @return Whether the coordinate is valid, unblocking, and within the world.
     */
    private fun validRange(n: Vector2i): Boolean
    {
        return n.x >= 0 && n.y >= 0 && n.x < worldTiles!!.size && n.y < worldTiles!![0].size && !worldTiles!![n.x][n.y]!!.blocking
    }

    /**
     * @param x World x-coordinate.
     * @param y World y-coordinate.
     *
     * @return Tile coordinates.
     */
    private fun toTile(x: Double, y: Double): Vector2i
    {
        return Vector2i((x / 0.2).toInt(), (y / 0.2).toInt())
    }

    override fun update(entity: Entity)
    {
        // Only update actionable entities
        if (entity !is ActionableEntity) return

        // Update convenience references
        target = gameWorld!!.player
        worldTiles = Objects.requireNonNull((gameWorld as GameWorld).room)!!.tiles

        // Check if the world size has changed; that requires a more-involved reset
        if (vectorField == null || vectorField!!.size != worldTiles!!.size || vectorField!![0].size != worldTiles!![0].size)
        {
            vectorField = Array(worldTiles!!.size) { arrayOfNulls(worldTiles!![0].size) }
            worldVisitCount = Array(worldTiles!!.size) { LongArray(worldTiles!![0].size) }
            expected = 0

            // Reset prematurely (may generate warning)
            reset()
        }

        // Start a new breadth-first search every 0.3 seconds
        if (updateTimer.delta >= 0.3) reset()

        // Work through the tile queue
        evaluateField()

        // An aggregate vector of considered vector field values
        val direction = Vector2f()
        // A collection of tiles the character is covering
        val consider: MutableList<Vector2i> = ArrayList()
        val bb = entity.boundingBox

        // Consider left bottom corner of bounding box
        consider.add(toTile(bb.x, bb.y))
        // Consider right bottom corner of bounding box
        consider.add(toTile(bb.x + bb.width, bb.y))
        // Consider right top corner of bounding box
        consider.add(toTile(bb.x + bb.width, bb.y + bb.height))
        // Consider left top corner of bounding box
        consider.add(toTile(bb.x, bb.y + bb.height))

        // Consider the center point of the bounding box
        consider.add(toTile(bb.x + bb.width / 2, bb.y + bb.height / 2))

        // Sum all of the vectors the entity is touching
        for (c in consider)
        {
            if (validRange(c) && vectorField!![c.x][c.y] != null) direction.add(
                vectorField!![c.x][c.y]!!.x.toFloat(),
                vectorField!![c.x][c.y]!!.y.toFloat()
            )
        }

        // Make the sum vector be of length 1.0
        if (direction.length() > 0.0f) direction.normalize()
        direction.mul(0.8f)

        // Update the actionable entity movement
        if (!bb.overlaps(target!!.boundingBox)) entity.walk(direction.x.toDouble(), direction.y.toDouble())
    }
}
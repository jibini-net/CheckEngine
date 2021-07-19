package net.jibini.check.entity.behavior;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.engine.timing.DeltaTimer;
import net.jibini.check.entity.ActionableEntity;
import net.jibini.check.entity.Entity;
import net.jibini.check.physics.BoundingBox;
import net.jibini.check.world.GameWorld;
import net.jibini.check.world.Tile;

import org.jetbrains.annotations.NotNull;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3i;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

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
public class PlayerTargetBehavior extends EntityBehavior
{
    // Local class logger instance
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Game world; required to access the tile array
    @EngineObject
    private GameWorld gameWorld;

    /**
     * A two-dimensional array of 2D vectors pointing to the most efficient path to the target (player); the third
     * element (z-coordinate) is the path length.
     */
    private Vector3i[][] vectorField = null;

    /**
     * A convenience reference to the world's tile array.  This reference is reset every update.
     */
    private Tile[][] worldTiles = null;

    /**
     * Non-resetting delta time which is used to update the vector field every refresh interval (0.3 seconds).
     */
    private final DeltaTimer updateTimer = new DeltaTimer(false);

    /**
     * Tracks the number of times each tile has been visited; this could be a boolean array, but that would require
     * an O(n^2) operation to reset the array.
     */
    private long[][] worldVisitCount = null;

    /**
     * The expected value in the visit-count array for an already-visited tile.
     */
    private long expected = 0;

    /**
     * Execution queue of the breadth-first search.  Tile coordinates are written to this queue, where the third element
     * (z-coordinate) is the length of the current path.
     *
     * Coordinates are written to this queue freely, but only a maximum of 2048 will be polled per update.
     */
    private final Deque<Vector3i> visitQueue = new LinkedBlockingDeque<>();

    /**
     * A convenience reference to the player instance.  This reference is reset every update.
     */
    public ActionableEntity target = null;

    /**
     * Performs a scheduled reset to the vector field.  This triggers the start of a new breadth-first search radiating
     * outwards from the player's base coordinates.
     *
     * This method validates that no other search is currently in progress.
     */
    private void reset()
    {
        if (gameWorld.getPlayer() == null) return;

        // Check if this was called too soon, or the last search hasn't yet finished
        if (updateTimer.getDelta() < 0.3 || !visitQueue.isEmpty())
        {
            log.warn("Pathfinding update overran time limit or was re-triggered prematurely");

            return;
        }

        // Find the base tile coordinate of the player's location
        Vector2i t = toTile(target.getX(), target.getY());

        // Queue the base tile and mark it as visited
        visitQueue.add(new Vector3i(t.x, t.y, 0));
        vectorField[t.x][t.y] = new Vector3i();
        worldVisitCount[t.x][t.y] = ++expected;

        // Reset the update timer; ensures the update will be called again
        updateTimer.reset();
    }

    /**
     * Visits the given tile coordinate, where the third element (z-coordinate) is the current path's accrued length.
     * By visiting a coordinate, this method sets its unvisited neighbors' vectors to point to this tile.
     *
     * The vector field will be updated if the neighbors are unvisited or this path is shorter than a previous visitor.
     *
     * @param location A 2D tile coordinate, with a third element (z-coordinate, path length).
     */
    private void visit(Vector3i location)
    {
        if (gameWorld.getPlayer() == null) return;

        // Maintain potential neighbors which can access this tile
        Vector2i[] neighbors =
        {
            new Vector2i(location.x, location.y + 1),
            new Vector2i(location.x, location.y - 1),
            new Vector2i(location.x + 1, location.y),
            new Vector2i(location.x - 1, location.y),
        };

        // Iterate through neighbors; check that the coordinates are valid and the neighbor should be updated (either
        // it is unvisited, or this path is shorter than a prior visitor)
        for (Vector2i n : neighbors)
            if (validRange(n) && (worldVisitCount[n.x][n.y] < expected || (vectorField[n.x][n.y] != null
                    && location.z + 1 < vectorField[n.x][n.y].z)))
            {
                // Mark as visited if unvisited
                if (worldVisitCount[n.x][n.y] < expected)
                    worldVisitCount[n.x][n.y] = expected;

                // The vector pointing from the neighbor to the original tile
                Vector2i xy = new Vector2i(location.x, location.y).sub(n);

                // Set the neighbor's vector field value and queue it
                vectorField[n.x][n.y] = new Vector3i(xy.x, xy.y, location.z + 1);
                visitQueue.add(new Vector3i(n.x, n.y, location.z + 1));
            }
    }

    /**
     * Evaluates the tile coordinate queue and renders the vector field arrows (if applicable).
     */
    private void evaluateField()
    {
        if (gameWorld.getPlayer() == null) return;

        // Iterate through the queue until empty (max 2048 per frame)
        for (int i = 0; i < 2048 && !visitQueue.isEmpty(); i++)
            visit(visitQueue.poll());
    }

    /**
     * @param n Neighbor coordinate to validate.  This can contain negative values, but negative values will be invalid.
     *
     * @return Whether the coordinate is valid, unblocking, and within the world.
     */
    private boolean validRange(Vector2i n)
    {
        return n.x >= 0 && n.y >= 0 && n.x < worldTiles.length && n.y < worldTiles[0].length
                && !worldTiles[n.x][n.y].getBlocking();
    }

    /**
     * @param x World x-coordinate.
     * @param y World y-coordinate.
     *
     * @return Tile coordinates.
     */
    private Vector2i toTile(double x, double y)
    {
        return new Vector2i((int)(x / 0.2), (int)(y / 0.2));
    }

    @Override
    public void update(@NotNull Entity entity)
    {
        if (gameWorld.getPlayer() == null) return;

        // Only update actionable entities
        if (!(entity instanceof ActionableEntity))
            return;

        // Update convenience references
        target = gameWorld.getPlayer();
        worldTiles = Objects.requireNonNull(gameWorld.getRoom()).getTiles();

        // Check if the world size has changed; that requires a more-involved reset
        if (vectorField == null || vectorField.length != worldTiles.length
                || vectorField[0].length != worldTiles[0].length)
        {
            vectorField = new Vector3i[worldTiles.length][worldTiles[0].length];
            worldVisitCount = new long[worldTiles.length][worldTiles[0].length];

            expected = 0;

            // Reset prematurely (may generate warning)
            reset();
        }

        // Start a new breadth-first search every 0.3 seconds
        if (updateTimer.getDelta() >= 0.3)
            reset();

        // Work through the tile queue
        evaluateField();

        // An aggregate vector of considered vector field values
        Vector2f direction = new Vector2f();
        // A collection of tiles the character is covering
        List<Vector2i> consider = new ArrayList<>();

        BoundingBox bb = entity.getBoundingBox();

        // Consider left bottom corner of bounding box
        consider.add(toTile(bb.getX(), bb.getY()));
        // Consider right bottom corner of bounding box
        consider.add(toTile(bb.getX() + bb.getWidth(), bb.getY()));
        // Consider right top corner of bounding box
        consider.add(toTile(bb.getX() + bb.getWidth(), bb.getY() + bb.getHeight()));
        // Consider left top corner of bounding box
        consider.add(toTile(bb.getX(), bb.getY() + bb.getHeight()));

        // Consider the center point of the bounding box
        consider.add(toTile(bb.getX() + bb.getWidth() / 2, bb.getY() + bb.getHeight() / 2));

        // Sum all of the vectors the entity is touching
        for (Vector2i c : consider)
        {
            if (validRange(c) && vectorField[c.x][c.y] != null)
                direction.add(vectorField[c.x][c.y].x, vectorField[c.x][c.y].y);
        }

        // Make the sum vector be of length 1.0
        if (direction.length() > 0.0f)
            direction.normalize();
        direction.mul(0.8f);

        // Update the actionable entity movement
        if (!bb.overlaps(target.getBoundingBox()))
            ((ActionableEntity)entity).walk(direction.x, direction.y);
    }
}

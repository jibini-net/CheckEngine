package net.jibini.check.entity

import net.jibini.check.world.GameWorld

/**
 * An annotated game object which decides how an entity is spawned based on\
 * inputted arguments from the world file.
 *
 * @author Zach Goethel
 */
@FunctionalInterface
interface EntitySpawner
{
    fun spawn(gameWorld: GameWorld, vararg args: Any?)
}
package net.jibini.check.entity.character

import net.jibini.check.entity.ActionableEntity
import net.jibini.check.entity.Inventory
import net.jibini.check.texture.Texture

/**
 * A character which is rendered the same as a player, but isn't
 * controlled by user input.
 *
 * @author Zach Goethel
 */
class NonPlayer(
    /**
     * Character's right-facing idle texture.
     */
    idleRight: Texture,

    /**
     * Character's left-facing idle texture.
     */
    idleLeft: Texture = idleRight.flip(),

    /**
     * Character's right-facing walking texture.
     */
    walkRight: Texture,

    /**
     * Character's left-facing walking texture.
     */
    walkLeft: Texture = idleRight.flip(),

    /**
     * Entity's classifier name (such as an NPC's name).
     */
    val typeName: String,

    /**
     * Name of the behavior in case of null state of behavior.
     */
    val behaviorName: String,

    /**
     * The player's starting inventory; they will keep this inventory and any
     * references to it will reflect inventory changes.
     */
    override val inventory: Inventory
) : ActionableEntity(idleRight, idleLeft, walkRight, walkLeft)
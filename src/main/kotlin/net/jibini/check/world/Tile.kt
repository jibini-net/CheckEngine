package net.jibini.check.world

import net.jibini.check.engine.EngineAware
import net.jibini.check.texture.Texture

/**
 * A small section of a game level which can be interacted with.
 *
 * @author Zach Goethel
 */
class Tile(
    /**
     * Tile's texture for rendering.
     */
    val texture: Texture,

    /**
     * Whether the tile blocks the player from moving.
     */
    val blocking: Boolean,

    /**
     * Whether the tile blocks light.
     */
    val lightBlocking: Boolean = blocking
) : EngineAware()
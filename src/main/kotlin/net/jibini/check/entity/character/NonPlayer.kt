package net.jibini.check.entity.character

import net.jibini.check.entity.ActionableEntity
import net.jibini.check.texture.Texture

class NonPlayer(
    /**
     * Character's right-facing idle texture
     */
    idleRight: Texture,

    /**
     * Character's left-facing idle texture
     */
    idleLeft: Texture = idleRight.flip(),

    /**
     * Character's right-facing walking texture
     */
    walkRight: Texture,

    /**
     * Character's left-facing walking texture
     */
    walkLeft: Texture = idleRight.flip()
) : ActionableEntity(idleRight, idleLeft, walkRight, walkLeft)
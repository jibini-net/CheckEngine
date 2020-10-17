package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture

class DecoratedTextureImpl(
    internal: Texture
) : Texture by internal
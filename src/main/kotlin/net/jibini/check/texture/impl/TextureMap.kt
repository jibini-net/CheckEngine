package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import org.slf4j.LoggerFactory

class TextureMap(
    dimension: Int
)
{
    private val log = LoggerFactory.getLogger(javaClass)

    private val num = MAP_DIMENSION / dimension
    private val increment = dimension.toFloat() / MAP_DIMENSION.toFloat()
    private val maxCapacity = num * num;

    private var given = 0

    private val texture = BitmapTexture()

    init
    {
        if (dimension > MAP_DIMENSION)
            throw IllegalStateException("Sprite size $dimension is larger than the map maximum $MAP_DIMENSION")
        if (MAP_DIMENSION % dimension != 0)
            log.warn("Texture size $dimension does not fit evenly within the sprite sheet size $MAP_DIMENSION")
    }

    val next: Texture
        get() {
            val x = (given % num).toFloat() * increment
            val y = (given / num).toFloat() * increment

            given++

            return DecoratedTexture(texture, TextureCoordinates(x, y, increment, increment),
                MAP_DIMENSION, MAP_DIMENSION)
        }

    val full: Boolean
        get() = given == maxCapacity

    companion object
    {
        const val MAP_DIMENSION = 2048
    }
}
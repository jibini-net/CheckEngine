package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import org.slf4j.LoggerFactory

class TextureSpriteMapImpl(
    private val dimension: Int
)
{
    private val num = MAP_DIMENSION / dimension
    private val maxCapacity = num * num

    private var given = 0

    private val texture = BitmapTextureImpl()

    init
    {
        if (dimension > MAP_DIMENSION)
            throw IllegalStateException("Sprite size $dimension is larger than the map maximum $MAP_DIMENSION")
        if (MAP_DIMENSION % dimension != 0)
            log.warn("Texture size $dimension does not fit evenly within the sprite sheet size $MAP_DIMENSION")
    }

    val next: Texture
        get() = CroppedSpriteTextureImpl(texture, dimension, MAP_DIMENSION, given++)

    val full: Boolean
        get() = given == maxCapacity

    companion object
    {
        private val log = LoggerFactory.getLogger(TextureSpriteMapImpl::class.java)

        const val MAP_DIMENSION = 2048

        // Three-dimensional map of texture sprite sheets; the three dimensions are Thread (to separate game and context
        // instances), sprite dimension (such as 128 of 32 or 512), and the sheet index (in case there are full sheets)
        private val mappedSheets = mutableMapOf<Thread, MutableMap<Int, MutableList<TextureSpriteMapImpl>>>()

        @Suppress("UnnecessaryVariable")
        fun sheetsFor(dimension: Int): MutableList<TextureSpriteMapImpl>
        {
            // Get sprite sheets for the current thread
            val threadMap = mappedSheets.getOrPut(Thread.currentThread()) {
                log.info("Creating knit sprite sheets for current thread")
                // Create if missing
                mutableMapOf()
            }

            // Get sprite sheets for the given sprite size
            val sheetGroup = threadMap.getOrPut(dimension) {
                log.info("Creating first sprite sheet for sprite size $dimension")
                // Create if missing
                mutableListOf(TextureSpriteMapImpl(dimension))
            }

            return sheetGroup
        }

        fun claimSprite(dimension: Int): Texture
        {
            // Get latest sheet for sprite size
            val sheets = sheetsFor(dimension)
            // Allocate the next sprite
            val texture = sheets.last().next

            // Check if last sheet is now full
            if (sheets.last().full)
            {
                // If now full, allocate a new sprite sheet
                log.info("Creating next sprite sheet for sprite size $dimension")
                sheets += TextureSpriteMapImpl(dimension)
            }

            return texture
        }
    }
}
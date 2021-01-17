package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import org.slf4j.LoggerFactory

/**
 * A collection of small sprites stored in a grid on a large square
 * texture.
 *
 * @author Zach Goethel
 */
class TextureSpriteMapImpl(
    /**
     * Sprite size in pixels.  This should be fairly large.
     */
    private val dimension: Int
)
{
    /**
     * Max number of sprites which can be fit along one side of the
     * sprite-sheet.
     */
    private val num = MAP_DIMENSION / dimension

    /**
     * The max number of sprites which can be held in one sprite-sheet.
     */
    private val maxCapacity = num * num

    /**
     * The number of sprites which have been allocated from this
     * sprite-sheet.
     */
    private var given = 0

    /**
     * Sprite-sheet master texture with all sprites contained within.
     */
    private val texture = BitmapTextureImpl()

    init
    {
        if (dimension > MAP_DIMENSION)
            throw IllegalStateException("Sprite size $dimension is larger than the map maximum $MAP_DIMENSION")
        if (MAP_DIMENSION % dimension != 0)
            log.warn("Texture size $dimension does not fit evenly within the sprite sheet size $MAP_DIMENSION")
    }

    /**
     * Allocates the next sprite in the sheet; is not validated to
     * ensure the sprite-sheet is not empty.
     */
    val next: Texture
        get() = CroppedSpriteTextureImpl(texture, dimension, MAP_DIMENSION, given++)

    /**
     * Checks whether the sprite-sheet is full and another should be
     * created.
     */
    val full: Boolean
        get() = given == maxCapacity

    companion object
    {
        private val log = LoggerFactory.getLogger(TextureSpriteMapImpl::class.java)

        /**
         * The global default sprite-sheet size in pixels.
         */
        const val MAP_DIMENSION = 2048

        // Three-dimensional map of texture sprite sheets; the three
        // dimensions are Thread (to separate game and context
        // instances), sprite dimension (such as 128 of 32 or 512),
        // and the sheet index (in case there are full sheets)
        private val mappedSheets = mutableMapOf<Thread, MutableMap<Int, MutableList<TextureSpriteMapImpl>>>()

        /**
         * Gets all sprite-sheets of the given dimension created by the
         * current thread.
         */
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

        /**
         * Claims a sprite of the given size and allocates more
         * sprite-sheets if necessary.
         *
         * @param dimension Size of sprite in pixels.
         */
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
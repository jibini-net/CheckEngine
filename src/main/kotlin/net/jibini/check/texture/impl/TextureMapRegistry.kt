package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import org.slf4j.LoggerFactory

object TextureMapRegistry
{
    private val log = LoggerFactory.getLogger(javaClass)

    // Three-dimensional map of texture sprite sheets; the three dimensions are Thread (to separate game and context
    // instances), sprite dimension (such as 128 of 32 or 512), and the sheet index (in case there are full sheets)
    private val mappedSheets = mutableMapOf<Thread, MutableMap<Int, MutableList<TextureMap>>>()

    @Suppress("UnnecessaryVariable")
    fun sheetsFor(dimension: Int): MutableList<TextureMap>
    {
        val threadMap = mappedSheets.getOrPut(Thread.currentThread()) {
            log.info("Creating knit sprite sheets for current thread")

            mutableMapOf()
        }

        val sheetGroup = threadMap.getOrPut(dimension) {
            log.info("Creating first sprite sheet for sprite size $dimension")

            mutableListOf(TextureMap(dimension))
        }

        return sheetGroup
    }

    fun claimSprite(dimension: Int): Texture
    {
        val sheets = sheetsFor(dimension)
        val texture = sheets.last().next

        if (sheets.last().full)
        {
            log.info("Creating next sprite sheet for sprite size $dimension")
            sheets += TextureMap(dimension)
        }

        return texture
    }
}
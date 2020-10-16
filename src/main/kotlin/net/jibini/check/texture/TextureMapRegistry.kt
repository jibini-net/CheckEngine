package net.jibini.check.texture

object TextureMapRegistry
{
    private const val MAP_DIMENSION = 2048

    // Three-dimensional map of texture sprite sheets; the three dimensions are Thread (to separate game and context
    // instances), sprite dimension (such as 128 of 32 or 512), and the sheet index (in case there are full sheets)
    private val mappedSheets = mutableMapOf<Thread, MutableMap<Int, MutableList<Texture>>>()

    fun createNextSprite(dimension: Int, spriteIndex: Int): Texture //TODO COMPUTE SPRITE INDEX
    {
        if (dimension > MAP_DIMENSION)
            throw IllegalStateException("Sprite size $dimension is larger than the map maximum $MAP_DIMENSION")

        val threadMap = mappedSheets.computeIfAbsent(Thread.currentThread()) { mutableMapOf() }
        val sheetGroup = threadMap.computeIfAbsent(dimension) { mutableListOf() }

        val maxFit = MAP_DIMENSION / dimension

        val perSheet = maxFit * maxFit
        val sheetIndex = spriteIndex / perSheet
        val spriteInSheet = spriteIndex % perSheet

        //TODO CHANGE TO INCREMENT AND CREATE NEW
        return sheetGroup[sheetIndex] //TODO CREATE NEW MAPPED TEXTURE
    }
}
package net.jibini.check.world.impl

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import net.jibini.check.graphics.Light
import net.jibini.check.resource.Resource

import java.io.File

/**
 * File format definition for the root JSON contents of a world file.
 * This includes spawn descriptions, spawn locations, macro invocations,
 * tile-sets, and lighting.
 *
 * World files should be read from and written to file via GSON. World
 * instances can be edited in memory and written to a JSON file.
 *
 * This is a direct replacement for the image and meta file world
 * editing. Consolidation of tile data and meta allows easier
 * portability of world files. This format is more flexible for
 * custom entities, in-engine world editing, and for those with
 * color-blindness.
 *
 * @author Zach Goethel
 */
class WorldFile
{
    /**
     * Indexed list of all tiles used in this world. These descriptors
     * also contain the tile coordinates of all instances of each tile.
     *
     * This makes it easier to attribute a tile location to its tile
     * descriptor, as using numerical indices would be subject to
     * off-by-one errors and changes in indices. Unique names or
     * identifiers would use unnecessary amounts of space and lookups.
     */
    var tileDescriptors = mutableListOf<TileDescriptor>()

    /**
     * World width in tiles.
     */
    var width = 64

    /**
     * World height in tiles.
     */
    var height = 32

    /**
     * Whether this world is a side-scroller.
     */
    var sideScroller = false

    /**
     * Collection of lights in the world.
     */
    var lights = mutableListOf<Light>()

    /**
     * List of pairs of spawner object names and the arguments which will be passed
     * upon invocation.
     */
    var spawnList = mutableListOf<SpawnEntry>()

    companion object
    {
        /**
         * Reads the world file from the given resource location.
         *
         * @param resource Resource pointing to the world JSON file.
         * @return Parsed world file loaded from the given resource.
         */
        fun read(resource: Resource): WorldFile = Gson().fromJson(resource.textContents, WorldFile::class.java)

        /**
         * Writes the world file to the working directory at the given
         * path.
         *
         * @param worldFile World file to write to file as JSON.
         * @param path File path for exporting the world file.
         */
        fun writeToFile(worldFile: WorldFile, path: String)
        {
            val json = GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(worldFile)
                .replace(Regex("\n {8,}"), " ")

            File(path).writeText(json)
        }
    }
}

/**
 * Defines a tile's texturing and blocking properties. Maintains a local
 * list of all coordinates at which the tile occurs in the world. For
 * further explanation for this choice, see [WorldFile.tileDescriptors].
 *
 * @author Zach Goethel
 */
class TileDescriptor
{
    /**
     * Contains texturing data for this tile type. This allows custom
     * implementations of texturing to use classpath resources, files, a
     * custom generator, or some sort of randomization.
     */
    var texturing = TileTexturing()

    /**
     * Whether this tile will block light and create shadows.
     */
    var blocksLight = false

    /**
     * Whether this tile will physically block entity movement.
     */
    var blocksPlayer = false

    /**
     * A collection of all tile coordinates at which this tile occurs
     * in the world. For further explanation for this choice, see
     * [WorldFile.tileDescriptors].
     */
    var usages = mutableListOf<IntArray>()
}

/**
 * Denotes how to texture a tile. Textures can be sourced from
 * classpath resources or file, or from a custom implementation of a
 * texture providing engine object (for generation or randomization of
 * textures).
 *
 * @author Zach Goethel
 */
class TileTexturing
{
    /**
     * The type of texture provider for this texture. For classpath
     * resources, put "resource." For files, put "file." For a custom
     * provider, put "custom."
     *
     * Use the appropriate [path] for your selected provider type.
     */
    var type = "resource"

    /**
     * The relative path of the texture. Use the appropriate path for
     * the [type] selection (working directory or resource path for file
     * and classpath respectively).
     *
     * For custom texture providers, provide the name of the engine
     * object class which will provide a texture.
     */
    var path = "tiles/black.png"
}

data class SpawnEntry(
    var spawnerName: String,
    val args: MutableList<Any?>
)
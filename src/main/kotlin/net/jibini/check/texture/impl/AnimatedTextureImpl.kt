package net.jibini.check.texture.impl

import net.jibini.check.texture.Texture
import net.jibini.check.texture.TextureCoordinates
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.awt.Color
import java.awt.image.BufferedImage
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageInputStream
import kotlin.concurrent.thread

//TODO CLEANUP AND REWRITE
class AnimatedTextureImpl(
    stream: ImageInputStream
) : Texture
{
    private var currentFrameIndex = 0

    private val frames = mutableListOf<ImageFrame>()
    private val animation = mutableListOf<AnimationFrame>()

    class AnimationFrame(
        val texture: Texture,
        val time: Int
    )

    private fun nextFrame()
    {
        currentFrameIndex++
        currentFrameIndex %= animation.size
    }

    init
    {
        val reader = ImageIO.getImageReadersByFormatName("gif").next()

        reader.input = stream

        val metadata = reader.streamMetadata

        var backgroundColor: Color? = null

        var lastX = 0
        var lastY = 0

        var width = -1
        var height = -1

        if (metadata != null)
        {
            val globalRoot = metadata.getAsTree(metadata.nativeMetadataFormatName) as IIOMetadataNode

            val globalColorTable: NodeList? = globalRoot.getElementsByTagName("GlobalColorTable")
            val globalScreenDescriptor: NodeList? = globalRoot.getElementsByTagName("LogicalScreenDescriptor")

            if (globalScreenDescriptor != null && globalScreenDescriptor.length > 0)
            {
                val screenDescriptor = globalScreenDescriptor.item(0)

                if (screenDescriptor != null)
                {
                    width = screenDescriptor.attributes.getNamedItem("logicalScreenWidth").nodeValue.toInt()
                    height = screenDescriptor.attributes.getNamedItem("logicalScreenHeight").nodeValue.toInt()
                }
            }

            if (globalColorTable != null && globalColorTable.length > 0)
            {
                val colorTable = globalColorTable.item(0)

                if (colorTable != null)
                {
                    val bgIndex = colorTable.attributes.getNamedItem("backgroundColorIndex").nodeValue.toInt()

                    var colorEntry = colorTable.firstChild as IIOMetadataNode

                    while (true)
                    {
                        if (colorEntry.attributes.getNamedItem("index").nodeValue.toInt() == bgIndex)
                        {
                            val red = colorEntry.getAttribute("red").toInt()
                            val green = colorEntry.getAttribute("green").toInt()
                            val blue = colorEntry.getAttribute("blue").toInt()

                            backgroundColor = Color(red, green, blue, 0)

                            break
                        }

                        colorEntry = colorEntry.nextSibling as IIOMetadataNode
                    }
                }
            }
        }

        var master: BufferedImage? = null

//        var hasBackground = false
        var frameIndex = 0

        while (true)
        {
            val image: BufferedImage = try
            {
                reader.read(frameIndex)
            } catch (io: IndexOutOfBoundsException)
            {
                break
            }

            if (width == -1 || height == -1)
            {
                width = image.width
                height = image.height
            }

            val root = reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_1.0")
                    as IIOMetadataNode
            val gce = root.getElementsByTagName("GraphicControlExtension").item(0) as IIOMetadataNode

            val children = root.childNodes

            val delay = Integer.valueOf(gce.getAttribute("delayTime"))
            val disposal = gce.getAttribute("disposalMethod")

            if (master == null)
            {
                master = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

                val graphics = master.createGraphics()
                graphics.color = backgroundColor
                graphics.fillRect(0, 0, master.width, master.height)

//                hasBackground = image.width == width && image.height == height

                graphics.drawImage(image, 0, 0, null)
            } else
            {
                var x = 0
                var y = 0

                for (nodeIndex in 0 until children.length)
                {
                    val nodeItem: Node = children.item(nodeIndex)

                    if (nodeItem.nodeName == "ImageDescriptor")
                    {
                        val map: NamedNodeMap = nodeItem.attributes

                        x = Integer.valueOf(map.getNamedItem("imageLeftPosition").nodeValue)
                        y = Integer.valueOf(map.getNamedItem("imageTopPosition").nodeValue)
                    }
                }

                if (disposal == "restoreToPrevious")
                {
                    var from: BufferedImage? = null

                    for (i in frameIndex - 1 downTo 0)
                    {
                        if (frames[i].disposal != "restoreToPrevious" || frameIndex == 0)
                        {
                            from = frames[i].image

                            break
                        }
                    }

                    val model = from!!.colorModel
                    val alpha = from.isAlphaPremultiplied

                    val raster = from.copyData(null)

                    master = BufferedImage(model, raster, alpha, null)
                } else if (disposal == "restoreToBackgroundColor" && backgroundColor != null)
                {
                    val graphics = master.createGraphics()
                    graphics.color = backgroundColor

//                    if (!hasBackground || frameIndex > 1)
//                    {
                        graphics.clearRect(
                            lastX,
                            lastY,
                            frames[frameIndex - 1].width,
                            frames[frameIndex - 1].height
                        )
//                    }
                }

                master.createGraphics().drawImage(image, x, y, null)

                lastX = x
                lastY = y
            }

            var copy: BufferedImage?

            val model = master.colorModel
            val alpha = master.isAlphaPremultiplied
            val raster = master.copyData(null)

            copy = BufferedImage(model, raster, alpha, null)

            frames.add(ImageFrame(copy, delay, disposal, image.width, image.height))

            master.flush()

            frameIndex++
        }

        reader.dispose()

        if (width != height)
            throw IllegalStateException("Animations must have square dimensions due to sprite sheet storage")

        for ((i, frame) in frames.withIndex())
        {
            animation += AnimationFrame(TextureSpriteMapImpl.claimSprite(width), frame.delay * 10)
            animation[i].texture.putData(0, 0, frame.image)
        }

        thread(name = "Animation", isDaemon = true) {
//            val log = LoggerFactory.getLogger(javaClass)

//            log.debug("Started new animation choreography daemon")

            while (true)
            {
                for (anim in animation)
                {
                    Thread.sleep(anim.time.toLong())
                    nextFrame()

                    Thread.yield()
                }
            }
        }
    }

    override val textureCoordinates: TextureCoordinates
        get() = animation[minOf(currentFrameIndex, animation.size)].texture.textureCoordinates

    override fun putData(offsetX: Int, offsetY: Int, width: Int, height: Int, data: ByteBuffer)
    {
        animation[minOf(currentFrameIndex, animation.size)].texture.putData(offsetX, offsetY, width, height, data)
    }

    override val pointer: Int
        get()
        {
            return animation[minOf(currentFrameIndex, animation.size - 1)].texture.pointer
        }

    class ImageFrame(
        val image: BufferedImage,
        val delay: Int,
        val disposal: String,

        val width: Int,
        val height: Int
    )
}
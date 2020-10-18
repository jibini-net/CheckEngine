package net.jibini.check.graphics

import net.jibini.check.texture.Texture
import org.lwjgl.opengl.GL11

class Renderer
{
    fun drawRectangle(x: Float, y: Float, width: Float, height: Float)
    {
        val coordinates = Texture.bound?.textureCoordinates

        // Find coordinate start coordinates
        val baseX = coordinates?.baseX ?: 0.0f
        val baseY = coordinates?.baseY ?: 0.0f
        // Find texture width and height
        val dx = coordinates?.deltaX ?: 0.0f
        val dy = coordinates?.deltaY ?: 0.0f

        GL11.glBegin(GL11.GL_QUADS)

        // Coordinate zero
        GL11.glTexCoord2f(baseX, baseY + dy)
        GL11.glVertex2f(x, y)
        // Coordinate one
        GL11.glTexCoord2f(baseX + dx, baseY + dy)
        GL11.glVertex2f(x + width, y)
        // Coordinate two
        GL11.glTexCoord2f(baseX + dx, baseY)
        GL11.glVertex2f(x + width, y + height)
        // Coordinate three
        GL11.glTexCoord2f(baseX, baseY)
        GL11.glVertex2f(x, y + height)

        GL11.glEnd()
    }
}
package net.jibini.check.world

import net.jibini.check.character.Entity
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable
import org.lwjgl.opengl.GL11

@RegisterObject
class GameWorld : Initializable, Updatable
{
    var visible = false

    var entities = mutableListOf<Entity>()

    var room: Room? = null

    var centerOn: Entity? = null

    override fun initialize()
    {

    }

    override fun update()
    {
        if (!visible)
            return

        if (centerOn != null)
            GL11.glTranslatef(-centerOn!!.x.toFloat(), -centerOn!!.y.toFloat(), 0.0f)

        room?.update()

        // Update entities last for transparency
        GL11.glPushMatrix()

        for (entity in entities)
        {
            GL11.glTranslatef(0.0f, 0.0f, 0.01f)

            entity.update()
        }

        GL11.glPopMatrix()
    }

    fun reset()
    {
        entities.clear()

        visible = false
    }
}
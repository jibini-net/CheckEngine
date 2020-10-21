package net.jibini.check.world

import net.jibini.check.character.Entity
import net.jibini.check.engine.Initializable
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable
import org.lwjgl.opengl.GL11

/**
 * An engine object which manages the game's current room and entities
 *
 * @author Zach Goethel
 */
@RegisterObject
class GameWorld : Initializable, Updatable
{
    /**
     * Whether the world should be rendered and updated (set to false by default; should be changed to true once the
     * game is initialized and ready to start a level)
     */
    var visible = false

    /**
     * Entities in the world; can be directly added to by the game
     */
    var entities = mutableListOf<Entity>()

    /**
     * Current room to update and render; set to null if no room should be rendered
     */
    var room: Room? = null

    /**
     * An entity on which the renderer will center the screen
     */
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
            // Translate forward to avoid transparency issues
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
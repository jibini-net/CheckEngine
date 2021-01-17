package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.Initializable;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.entity.ActionableEntity;
import net.jibini.check.entity.Entity;
import net.jibini.check.entity.behavior.EntityBehavior;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import org.jetbrains.annotations.NotNull;
import java.util.Random;

@RegisterObject
public class GendreauBehavior extends EntityBehavior implements Initializable
{
    // Yelling textures.  A very important element of this behavior.
    private Texture leftYell;
    private Texture rightYell;

    @EngineObject
    private PlayerTargetBehavior playerTarget;

    /**
     * Local random for randomizing the jump height.
     */
    private final Random random = new Random();

    @Override
    public void update(@NotNull Entity entity)
    {
        playerTarget.update(entity);

        ((ActionableEntity)entity).jump(random.nextDouble() / 2 + 0.5);
        // Update the entity's yelling texture
        if (playerTarget.target.getX() <= entity.getX())
            ((ActionableEntity)entity).setRenderTexture(leftYell);
        else
            ((ActionableEntity)entity).setRenderTexture(rightYell);
    }

    @Override
    public void initialize()
    {
        // Load the yelling texture
        rightYell = Texture.load(Resource.fromClasspath("characters/gendrau/gendrau_yell_right.gif"));
        leftYell = rightYell.flip(true, false);
    }
}

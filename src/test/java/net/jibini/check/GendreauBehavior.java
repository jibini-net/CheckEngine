package net.jibini.check;

import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.Initializable;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.entity.ActionableEntity;
import net.jibini.check.entity.Entity;
import net.jibini.check.entity.behavior.EntityBehavior;
import net.jibini.check.graphics.Light;
import net.jibini.check.graphics.impl.LightingShaderImpl;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Random;

@RegisterObject
public class GendreauBehavior extends EntityBehavior implements Initializable
{
    // Yelling textures.  A very important element of this behavior.
    private Texture leftYell;
    private Texture rightYell;

    @EngineObject
    private PlayerTargetBehavior playerTarget;

    @EngineObject
    private LightingShaderImpl lightingShader;

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

        Light redLight = lightingShader.getLights().get(0);
        redLight.setX((float)entity.getX() / 0.2f);
        redLight.setY((float)(entity.getY() + ((ActionableEntity) entity).getFalseYOffset() + 0.2f) / 0.2f);

        Vector3f color = new Vector3f(redLight.getR(), redLight.getG(), redLight.getB());
        color.normalize().mul((float)((ActionableEntity) entity).getFalseYOffset() * 2.0f + 0.05f);
        redLight.setR(color.x);
        redLight.setG(color.y);
        redLight.setB(color.z);
    }

    @Override
    public void initialize()
    {
        // Load the yelling texture
        rightYell = Texture.load(Resource.fromClasspath("characters/gendrau/gendrau_yell_right.gif"));
        leftYell = rightYell.flip(true, false);
    }
}

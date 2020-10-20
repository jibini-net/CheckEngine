package net.jibini.check;

import net.jibini.check.character.Player;
import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.Initializable;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.engine.Updatable;
import net.jibini.check.graphics.Renderer;
import net.jibini.check.input.Keyboard;
import net.jibini.check.resource.Resource;
import net.jibini.check.texture.Texture;

@RegisterObject
public class TestGameWorld implements Initializable, Updatable
{
    @EngineObject
    private Keyboard keyboard;

    @EngineObject
    private Renderer renderer;

    private Player forbes;

    @Override
    public void initialize()
    {
        forbes = new Player(
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_stand_left.gif")),

                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_walk_left.gif"))
        );
    }

    @Override
    public void update()
    {
        forbes.update();
    }
}

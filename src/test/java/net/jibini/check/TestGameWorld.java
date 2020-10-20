package net.jibini.check;

import net.jibini.check.character.Attack;
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

        forbes.setAttack(new Attack(
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_right.gif")),
                Texture.load(Resource.fromClasspath("characters/forbes/forbes_chop_left.gif")),

                /* Animation time (sec):    */ 0.5,
                /* Cool-down time (sec):    */ 0.35,
                /* Always reset animation?  */ false,
                /* Attack damage amount:    */ 1.0
        ));
    }

    @Override
    public void update()
    {
        forbes.update();
    }
}

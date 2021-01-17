package net.jibini.check.graphics

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.impl.StatefulShaderImpl
import org.joml.Vector2f
import org.joml.Vector4f

/**
 * A collection of shader uniforms which are common in the shader
 * pipelines. Modifying these values will set the uniform for the shader
 * program.
 *
 * @author Zach Goethel
 */
@RegisterObject
class Uniforms
{
    // Required to access the current shader program
    @EngineObject
    private lateinit var statefulShader: StatefulShaderImpl

    /**
     * The currently bound texture index; usually just zero.
     */
    var texture = 0
        set(value)
        {
            if (field != value)
                statefulShader.boundShader?.uniform("tex", value)
            field = value
        }

    /**
     * Base texture coordinates of the bound texture.
     */
    var textureOffset = Vector2f()
        set(value)
        {
            if (field != value)
                statefulShader.boundShader?.uniform("tex_offset", value.x, value.y)
            field = value
        }

    /**
     * The x- and y-size of the texture coordinates of the bound
     * texture. Added to the [textureOffset] to correctly texture a
     * rectangle.
     */
    var textureDelta = Vector2f()
        set(value)
        {
            if (field != value)
                statefulShader.boundShader?.uniform("tex_delta", value.x, value.y)
            field = value
        }

    /**
     * Whether any objects rendered should block light.
     */
    var blocking = false
        set(value)
        {
            if (field != value)
                statefulShader.boundShader?.uniform("light_blocking", value.compareTo(false))
            field = value
        }

    /**
     * A uniform color by which all rendered geometry will be modulated.
     */
    var colorMultiple = Vector4f(1.0f)
        set(value)
        {
            if (field != value)
                statefulShader.boundShader?.uniform("color_mult", value.x, value.y, value.z, value.w)
            field = value
        }
}
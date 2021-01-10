package net.jibini.check.graphics

import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.impl.StatefulShaderImpl
import org.joml.Vector2f
import org.joml.Vector4f

@RegisterObject
class Uniforms
{
    @EngineObject
    private lateinit var statefulShaderImpl: StatefulShaderImpl

    var texture = 0
        set(value)
        {
            statefulShaderImpl.boundShader?.uniform("tex", value)
            field = value
        }

    var textureOffset = Vector2f()
        set(value)
        {
            statefulShaderImpl.boundShader?.uniform("tex_offset", value.x, value.y)
            field = value
        }

    var blocking = false
        set(value)
        {
            statefulShaderImpl.boundShader?.uniform("light_blocking", value.compareTo(false))
            field = value
        }

    var colorMultiple = Vector4f(1.0f)
        set(value)
        {
            statefulShaderImpl.boundShader?.uniform("color_mult", value.x, value.y, value.z, value.w)
            field = value
        }
}
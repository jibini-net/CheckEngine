package net.jibini.check.graphics.impl

import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader

@RegisterObject
class StatefulShaderImpl
{
    var boundShader: Shader? = null
}
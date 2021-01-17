package net.jibini.check.graphics.impl

import net.jibini.check.engine.RegisterObject
import net.jibini.check.graphics.Shader

/**
 * Stores the current shader.
 *
 * @author Zach Goethel
 */
//TODO REMOVE
@RegisterObject
class StatefulShaderImpl
{
    /**
     * The currently bound shader program.
     */
    var boundShader: Shader? = null
}
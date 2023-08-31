package net.jibini.check

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.type.ImBoolean
import imgui.type.ImString
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable

@RegisterObject
class TestGUI : Updatable
{
    private val epicToggle = ImBoolean(true)
    private val inputString = ImString()
    override fun update()
    {
        ImGui.setNextWindowSize(500f, 300f, ImGuiCond.Once)
        ImGui.setNextWindowPos(30f, 50f, ImGuiCond.Once)
        ImGui.setNextWindowCollapsed(true, ImGuiCond.Once)
        ImGui.begin("Test Window")
        ImGui.checkbox("This is epic?", epicToggle)
        ImGui.separator()
        ImGui.text("Try typing:")
        ImGui.inputTextMultiline("Input", inputString)
        ImGui.end()
    }
}
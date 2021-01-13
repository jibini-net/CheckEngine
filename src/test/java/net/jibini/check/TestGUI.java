package net.jibini.check;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;

import imgui.type.ImString;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.engine.Updatable;

@RegisterObject
public class TestGUI implements Updatable
{
    private final ImBoolean epicToggle = new ImBoolean(true);
    private final ImString inputString = new ImString();

    @Override
    public void update()
    {
        ImGui.setNextWindowSize(500, 300, ImGuiCond.Once);
        ImGui.setNextWindowPos(30, 50, ImGuiCond.Once);
        ImGui.setNextWindowCollapsed(true, ImGuiCond.Once);

        ImGui.begin("Test Window");

        ImGui.checkbox("This is epic?", epicToggle);

        ImGui.separator();

        ImGui.text("Try typing:");
        ImGui.inputTextMultiline("Input", inputString);

        ImGui.end();
    }
}

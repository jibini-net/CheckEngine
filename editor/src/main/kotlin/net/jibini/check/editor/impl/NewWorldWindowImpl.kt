package net.jibini.check.editor.impl

import imgui.ImGui
import imgui.flag.ImGuiCond
import imgui.type.ImBoolean
import imgui.type.ImInt

import net.jibini.check.editor.WorldEditor
import net.jibini.check.engine.EngineObject
import net.jibini.check.engine.RegisterObject
import net.jibini.check.engine.Updatable
import net.jibini.check.graphics.Window
import net.jibini.check.resource.Resource
import net.jibini.check.world.impl.WorldFile

import java.io.File

import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

@RegisterObject
class NewWorldWindowImpl : Updatable
{
    // Required to access the current editor room
    @EngineObject
    private lateinit var worldEditor: WorldEditor

    // Required to access screen size
    @EngineObject
    private lateinit var window: Window

    // Required to invoke legacy import
    @EngineObject
    private lateinit var legacyWorldImport: LegacyWorldImportImpl

    private val fileChooser = JFileChooser(System.getProperty("user.dir"))

    // ImGui field state objects
    private val worldWidth = ImInt(64)
    private val worldHeight = ImInt(32)

    private val worldSideScroller = ImBoolean(false)

    private var flag = true

    override fun update()
    {
        if (flag)
        {
            flag = false

            if (worldEditor.current == null)
            {
                ImGui.setNextWindowPos(
                    window.width.toFloat() / 2 - 150,
                    window.height.toFloat() / 2 - 125,

                    ImGuiCond.Always
                )

                ImGui.setNextWindowSize(
                    300.0f,
                    250.0f,

                    ImGuiCond.Always
                )
            } else
                ImGui.setNextWindowPos(25.0f, 20.0f, ImGuiCond.Always)

            ImGui.setNextWindowCollapsed(worldEditor.current != null, ImGuiCond.Always)
        }

        ImGui.begin("Open or create world")

        ImGui.text("New world")

        ImGui.inputInt("Width", worldWidth)
        ImGui.inputInt("Height", worldHeight)
        ImGui.spacing()

        ImGui.checkbox("Side-scroller", worldSideScroller)
        ImGui.spacing()

        if (ImGui.button("Create"))
            create()
        ImGui.spacing()

        ImGui.separator()
        ImGui.spacing()

        if (ImGui.button("Open world JSON", 280.0f, 30.0f))
            open()
        if (ImGui.button("Import legacy world", 280.0f, 30.0f))
            import()

        ImGui.end()
    }

    private fun create()
    {
        val result = if (worldEditor.current == null)
            true
        else
            JOptionPane.showConfirmDialog(null, "Are you sure?") == JOptionPane.OK_OPTION

        if (result)
        {
            flag = true

            worldEditor.current = WorldFile()
                .apply()
                {
                    width = worldWidth.get()
                    height = worldHeight.get()

                    sideScroller = worldSideScroller.get()
                }
        }
    }

    private fun open()
    {
        fileChooser.fileFilter = FileNameExtensionFilter("World JSON file", "json")
        fileChooser.grabFocus()

        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

        val result = fileChooser.showDialog(null, "Open")

        if (result == JFileChooser.APPROVE_OPTION)
        {
            flag = true

            val resource = Resource.fromFile(fileChooser.selectedFile)
            worldEditor.current = WorldFile.read(resource)
        }
    }

    private fun import()
    {
        fileChooser.resetChoosableFileFilters()
        fileChooser.grabFocus()
        fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

        val result = fileChooser.showDialog(null, "Import")

        if (result == JFileChooser.APPROVE_OPTION)
        {
            flag = true

            val basePath = File("${System.getProperty("user.dir")}/worlds")
            worldEditor.current = legacyWorldImport.import(fileChooser.selectedFile.relativeTo(basePath).path)
        }
    }
}
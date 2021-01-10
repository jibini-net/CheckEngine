package net.jibini.check;

import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import net.jibini.check.engine.EngineObject;
import net.jibini.check.engine.Initializable;
import net.jibini.check.engine.RegisterObject;
import net.jibini.check.engine.Updatable;
import net.jibini.check.graphics.Window;

import java.lang.reflect.Field;
import java.util.Arrays;

@RegisterObject
public class TestGUI implements Initializable, Updatable
{
    @EngineObject
    private Window window;

    private final ImGuiImplGlfw glfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 gles30 = new ImGuiImplGl3();

    private final ImBoolean epicToggle = new ImBoolean(true);

    private void addLibraryPath(String pathToAdd)
    {
        try
        {
            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            final String[] paths = (String[])usrPathsField.get(null);

            for (String path : paths)
            {
                if (path.equals(pathToAdd))
                    return;
            }

            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);

            newPaths[newPaths.length - 1] = pathToAdd;
            usrPathsField.set(null, newPaths);
        } catch (NoSuchFieldException | IllegalAccessException ignored)
        {  }
    }

    @Override
    public void initialize()
    {
        String wdPath = System.getProperty("user.dir");
        addLibraryPath(wdPath + "/bin");

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setConfigViewportsNoTaskBarIcon(true);

        ImFontAtlas fontAtlas = io.getFonts();
        fontAtlas.addFontDefault();

        glfw.init(window.getPointer(), true);
        gles30.init();
    }

    @Override
    public void update()
    {
        glfw.newFrame();
        ImGui.newFrame();

        // ===============

        ImGui.setNextWindowSize(500, 300);
        ImGui.setNextWindowPos(0, 0);
        ImGui.begin("Test Window");

        ImGui.checkbox("This is epic?", epicToggle);

        ImGui.end();

        // ===============

        ImGui.render();

        gles30.renderDrawData(ImGui.getDrawData());
    }
}

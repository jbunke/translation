package com.jordanbunke.translation.game_states;

import com.jordanbunke.jbjgl.contexts.ProgramContext;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.editor.Editor;
import com.jordanbunke.translation.editor.EditorHUD;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.menus.MenuHelper;
import com.jordanbunke.translation.menus.MenuIDs;
import com.jordanbunke.translation.menus.Menus;
import com.jordanbunke.translation.settings.debug.DebugRenderer;
import com.jordanbunke.translation.settings.debug.DebugSettings;

import java.awt.*;

public class EditorGameState extends ProgramContext {

    private EditorGameState() {
        Editor.initialize();
        EditorHUD.initialize();
    }

    public static EditorGameState create() {
        return new EditorGameState();
    }

    @Override
    public void update() {
        Editor.update();

        // DEBUG
        if (DebugSettings.isPrintDebug())
            DebugRenderer.update();
    }

    @Override
    public void render(Graphics g, JBJGLGameDebugger debugger) {
        Editor.render(g, debugger);

        // DEBUG
        if (DebugSettings.isShowingPixelGrid())
            g.drawImage(ImageAssets.PIXEL_GRID, 0, 0, null);

        if (DebugSettings.isPrintDebug())
            DebugRenderer.render(g);
    }

    @Override
    public void process(JBJGLListener listener) {
        Editor.process(listener);

        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.PAUSE), () -> {
                    Translation.manager.setActiveStateIndex(Translation.PAUSE_INDEX);
                    MenuHelper.linkMenu(MenuIDs.EDITOR_MENU, Menus.generateEditorMenu());
                }
        );
    }
}

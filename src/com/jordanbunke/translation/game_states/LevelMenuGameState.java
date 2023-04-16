package com.jordanbunke.translation.game_states;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.contexts.ProgramContext;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.menus.MenuIDs;
import com.jordanbunke.translation.menus.Menus;
import com.jordanbunke.translation.settings.debug.DebugRenderer;
import com.jordanbunke.translation.settings.debug.DebugSettings;

import java.awt.*;

public class LevelMenuGameState extends ProgramContext {
    private final Type type;

    private JBJGLMenuManager menuManager;

    public enum Type {
        PAUSE, LEVEL_COMPLETE
    }

    private LevelMenuGameState(final Level level, final Type type) {
        this.type = type;

        setLevel(level);
    }

    public static LevelMenuGameState create(final Level level, final Type type) {
        return new LevelMenuGameState(level, type);
    }

    public JBJGLMenuManager getMenuManager() {
        return menuManager;
    }

    public void setLevel(final Level level) {
        menuManager = switch (type) {
            case PAUSE -> Menus.generatePauseMenuManager(level);
            case LEVEL_COMPLETE -> Menus.generateLevelCompleteMenuManager(level);
        };
    }

    @Override
    public void update() {
        menuManager.update();

        // DEBUG
        if (DebugSettings.isPrintDebug())
            DebugRenderer.update();
    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        menuManager.render(g, debugger);

        if (DebugSettings.isPrintDebug())
            DebugRenderer.render(g);
    }

    @Override
    public void process(JBJGLListener listener) {
        menuManager.process(listener);

        if (type == Type.PAUSE)
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.PAUSE), () ->
                            Translation.manager.setActiveStateIndex(
                                    MenuIDs.isAnEditorMenu(menuManager.getActiveMenuID())
                                            ? Translation.EDITOR_INDEX
                                            : Translation.GAMEPLAY_INDEX)
            );
    }
}

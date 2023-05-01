package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.contexts.ProgramContext;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.translation.settings.debug.DebugRenderer;
import com.jordanbunke.translation.settings.debug.DebugSettings;

import java.awt.*;

public class MenuManagerWrapper extends ProgramContext {

    private final JBJGLMenuManager menuManager;

    private MenuManagerWrapper(final JBJGLMenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public static MenuManagerWrapper createOf(final JBJGLMenuManager menuManager) {
        return new MenuManagerWrapper(menuManager);
    }

    @Override
    public void update() {
        menuManager.update();

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
    public void process(final JBJGLListener listener) {
        menuManager.process(listener);
    }

    public JBJGLMenuManager getMenuManager() {
        return menuManager;
    }
}

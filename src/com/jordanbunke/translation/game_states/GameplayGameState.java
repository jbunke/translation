package com.jordanbunke.translation.game_states;

import com.jordanbunke.jbjgl.contexts.ProgramContext;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.menus.MenuHelper;
import com.jordanbunke.translation.menus.MenuIDs;
import com.jordanbunke.translation.settings.debug.DebugRenderer;
import com.jordanbunke.translation.settings.debug.DebugSettings;

import java.awt.*;

public class GameplayGameState extends ProgramContext {
    private Level level;

    private GameplayGameState(final Level level) {
        this.level = level;
    }

    public static GameplayGameState create(final Level level) {
        return new GameplayGameState(level);
    }

    public void setLevel(final Level level) {
        this.level = level;
    }

    @Override
    public void update() {
        level.update();

        // DEBUG
        if (DebugSettings.isPrintDebug())
            DebugRenderer.update();
    }

    @Override
    public void render(
            final Graphics g, final JBJGLGameDebugger debugger
    ) {
        level.render(g, debugger);

        // DEBUG
        if (DebugSettings.isShowingPixelGrid())
            g.drawImage(ImageAssets.PIXEL_GRID, 0, 0, null);

        if (DebugSettings.isPrintDebug())
            DebugRenderer.render(g);
    }

    @Override
    public void process(final JBJGLListener listener) {
        level.process(listener);

        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.PAUSE), () -> {
                    Translation.manager.setActiveStateIndex(Translation.PAUSE_INDEX);
                    MenuHelper.linkMenu(MenuIDs.PAUSE_MENU);
                }
        );
    }
}

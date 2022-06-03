package com.jordanbunke.translation;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.game.JBJGLGame;
import com.jordanbunke.jbjgl.game.JBJGLGameEngine;
import com.jordanbunke.jbjgl.game.JBJGLGameManager;
import com.jordanbunke.jbjgl.window.JBJGLWindow;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.game_states.LevelMenuGameState;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.game_states.GameplayGameState;
import com.jordanbunke.translation.menus.Menus;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugRenderer;
import com.jordanbunke.translation.settings.debug.DebugSettings;

public class Translation {
    public static final String TITLE = "Translation";
    public static final String VERSION = "0.1";

    public static final int GAMEPLAY_INDEX = 0, PAUSE_INDEX = 1,
            LEVEL_COMPLETE_INDEX = 2, MENU_INDEX = 3, SPLASH_SCREEN_INDEX = 4;

    public static Campaign campaign;

    public static GameplayGameState gameState;
    public static LevelMenuGameState pauseState;
    public static LevelMenuGameState levelCompleteState;
    public static JBJGLMenuManager menuManager;
    public static JBJGLMenuManager splashScreenManager;

    public static JBJGLGameManager manager;
    public static JBJGLGameDebugger debugger;
    public static JBJGLGameEngine gameEngine;
    public static JBJGLGame game;

    public static void main(String[] args) {
        Fonts.setGameFontToClassic();

        TechnicalSettings.setFullscreen(true);
        TechnicalSettings.setPixelLocked(true);

        GameplaySettings.setDefaultFollowMode(Camera.FollowMode.STEADY);
        GameplaySettings.setShowingNecromancerTethers(true);

        DebugSettings.setShowPixelGrid(false);
        DebugSettings.setPrintDebug(false);
        launch();
    }

    public static void resize(final boolean fullscreen) {
        TechnicalSettings.setFullscreen(fullscreen);
        ImageAssets.updateAfterResize();
        updateMenus();
        game.replaceWindow(generateWindow());
    }

    private static void updateMenus() {
        final String[] menuIDs = new String[] {
                "",
                pauseState.getMenuManager().getActiveMenuID(),
                levelCompleteState.getMenuManager().getActiveMenuID(),
                menuManager.getActiveMenuID(),
                splashScreenManager.getActiveMenuID()
        };

        final Level level = campaign.getLevel();

        pauseState = LevelMenuGameState.create(level, LevelMenuGameState.Type.PAUSE);
        levelCompleteState = LevelMenuGameState.create(level, LevelMenuGameState.Type.LEVEL_COMPLETE);
        menuManager = Menus.generateMenuManager();
        splashScreenManager = Menus.generateSplashScreenManager();

        pauseState.getMenuManager().setActiveMenuID(menuIDs[PAUSE_INDEX]);
        levelCompleteState.getMenuManager().setActiveMenuID(menuIDs[LEVEL_COMPLETE_INDEX]);
        menuManager.setActiveMenuID(menuIDs[MENU_INDEX]);
        splashScreenManager.setActiveMenuID(menuIDs[SPLASH_SCREEN_INDEX]);

        manager.setGameStateAtIndex(MENU_INDEX, menuManager);
        manager.setGameStateAtIndex(PAUSE_INDEX, pauseState);
        manager.setGameStateAtIndex(LEVEL_COMPLETE_INDEX, levelCompleteState);
        manager.setGameStateAtIndex(SPLASH_SCREEN_INDEX, splashScreenManager);
    }

    private static JBJGLWindow generateWindow() {
        return JBJGLWindow.create(
                TITLE, TechnicalSettings.getWidth(), TechnicalSettings.getHeight(),
                ImageAssets.ICON, true, false, TechnicalSettings.isFullscreen()
        );
    }

    private static void launch() {
        debugger = prepDebugger();

        campaign = LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER);
        final Level level = campaign.getLevel();

        gameState = GameplayGameState.create(level);
        pauseState = LevelMenuGameState.create(level, LevelMenuGameState.Type.PAUSE);
        levelCompleteState = LevelMenuGameState.create(level, LevelMenuGameState.Type.LEVEL_COMPLETE);
        menuManager = Menus.generateMenuManager();
        splashScreenManager = Menus.generateSplashScreenManager();

        manager = JBJGLGameManager.createOf(
                SPLASH_SCREEN_INDEX,
                gameState,
                pauseState,
                levelCompleteState,
                menuManager,
                splashScreenManager
        );

        game = JBJGLGame.create(TITLE, manager,
                TechnicalSettings.getWidth(), TechnicalSettings.getHeight(),
                ImageAssets.ICON,
                true, TechnicalSettings.isFullscreen(),
                GameplayConstants.UPDATE_HZ, GameplayConstants.TARGET_FPS);
        gameEngine = game.getGameEngine();
        gameEngine.overrideDebugger(debugger);
    }

    private static JBJGLGameDebugger prepDebugger() {
        JBJGLGameDebugger d = JBJGLGameDebugger.create();

        // debugger settings
        d.hideBoundingBoxes();

        d.muteChannel(JBJGLGameDebugger.PERFORMANCE_CHANNEL);

        // debugger channel output functions
        d.getChannel(JBJGLGameDebugger.PERFORMANCE_CHANNEL).setOutputFunction(DebugRenderer::debugOutputFunction);
        d.getChannel(JBJGLGameDebugger.LOGIC_CHANNEL).setOutputFunction(DebugRenderer::debugOutputFunction);
        d.getChannel(JBJGLGameDebugger.MEMORY_CHANNEL).setOutputFunction(DebugRenderer::debugOutputFunction);

        return d;
    }

    public static void setLevel(final Level level) {
        gameState.setLevel(level);
        pauseState.setLevel(level);
        levelCompleteState.setLevel(level);
    }

    public static void quitGame() {
        System.exit(0);
    }
}

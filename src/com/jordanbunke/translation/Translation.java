package com.jordanbunke.translation;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.game.JBJGLGame;
import com.jordanbunke.jbjgl.game.JBJGLGameEngine;
import com.jordanbunke.jbjgl.game.JBJGLGameManager;
import com.jordanbunke.jbjgl.window.JBJGLBoilerplate;
import com.jordanbunke.jbjgl.window.JBJGLWindow;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.game_states.EditorGameState;
import com.jordanbunke.translation.game_states.LevelMenuGameState;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.io.SettingsIO;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.game_states.GameplayGameState;
import com.jordanbunke.translation.menus.Menus;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugRenderer;

public class Translation {
    private static final int INDEX_SKIP_SPLASH_SCREEN = 0, INDEX_SHOW_BOUNDING_BOXES = 1, TOTAL_FLAGS = 2;

    public static final String TITLE = "Translation",
            VERSION = "0.2.0 (dev)",
            MY_GITHUB_LINK = "https://github.com/jbunke",
            MY_GAMES_LINK = "https://flinkerflitzer.itch.io/";

    public static final int GAMEPLAY_INDEX = 0, PAUSE_INDEX = 1,
            LEVEL_COMPLETE_INDEX = 2, MENU_INDEX = 3, SPLASH_SCREEN_INDEX = 4,
            EDITOR_INDEX = 5;

    public static Campaign campaign;
    private static Level currentLevel;

    public static GameplayGameState gameState;
    public static LevelMenuGameState pauseState;
    public static LevelMenuGameState levelCompleteState;
    public static JBJGLMenuManager menuManager;
    public static JBJGLMenuManager splashScreenManager;
    public static EditorGameState editorGameState;

    public static JBJGLGameManager manager;
    public static JBJGLGameDebugger debugger;
    public static JBJGLGameEngine gameEngine;
    public static JBJGLGame game;

    public static void main(String[] args) {
        JBJGLBoilerplate.run();

        Fonts.setGameFontToClassic();
        SettingsIO.read();
        launch(processArgs(args));
    }

    private static boolean[] processArgs(final String[] args) {
        final boolean[] flags = new boolean[TOTAL_FLAGS];

        final String skipSplashScreenFlag = "-sss";
        final String showBoundingBoxesFlag = "-sbb";

        for (String arg : args) {
            int index = switch (arg) {
                case skipSplashScreenFlag -> INDEX_SKIP_SPLASH_SCREEN;
                case showBoundingBoxesFlag -> INDEX_SHOW_BOUNDING_BOXES;
                default -> -1;
            };

            if (index >= 0)
                flags[index] = true;
        }

        return flags;
    }

    public static void resize(final boolean fullscreen) {
        TechnicalSettings.setFullscreen(fullscreen);
        ImageAssets.updateAfterResize();
        updateMenusAfterResize();
        game.replaceWindow(generateWindow());
    }

    private static void updateMenusAfterResize() {
        final Level level = currentLevel == null ? campaign.getLevel() : currentLevel;

        if (manager.getActiveStateIndex() == PAUSE_INDEX)
            Menus.generateAfterResize(pauseState.getMenuManager(), false, level);
        else
            Menus.generateAfterResize(menuManager, true, level);
    }

    private static JBJGLWindow generateWindow() {
        return JBJGLWindow.create(
                TITLE, TechnicalSettings.getWidth(), TechnicalSettings.getHeight(),
                ImageAssets.ICON, true, false, TechnicalSettings.isFullscreen()
        );
    }

    private static void launch(final boolean[] flags) {
        debugger = prepDebugger(flags);

        campaign = LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER);
        final Level level = campaign.getLevel();

        gameState = GameplayGameState.create(level);
        pauseState = LevelMenuGameState.create(level, LevelMenuGameState.Type.PAUSE);
        levelCompleteState = LevelMenuGameState.create(level, LevelMenuGameState.Type.LEVEL_COMPLETE);
        menuManager = Menus.generateMenuManager();
        splashScreenManager = Menus.generateSplashScreenManager();
        editorGameState = EditorGameState.create();

        manager = JBJGLGameManager.createOf(
                flags[INDEX_SKIP_SPLASH_SCREEN] ? MENU_INDEX : SPLASH_SCREEN_INDEX,
                gameState,
                pauseState,
                levelCompleteState,
                menuManager,
                splashScreenManager,
                editorGameState
        );

        game = JBJGLGame.create(TITLE, manager,
                TechnicalSettings.getWidth(), TechnicalSettings.getHeight(),
                ImageAssets.ICON,
                true, TechnicalSettings.isFullscreen(),
                GameplayConstants.UPDATE_HZ, GameplayConstants.TARGET_FPS);
        gameEngine = game.getGameEngine();
        gameEngine.overrideDebugger(debugger);
    }

    private static JBJGLGameDebugger prepDebugger(final boolean[] flags) {
        JBJGLGameDebugger d = JBJGLGameDebugger.create();

        // debugger settings
        if (!flags[INDEX_SHOW_BOUNDING_BOXES])
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

        currentLevel = level;
    }

    public static void quitGame() {
        SettingsIO.write();
        System.exit(0);
    }
}

package com.jordanbunke.translation.io;

import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugSettings;

import java.nio.file.Path;
import java.nio.file.Paths;

// TODO: extend for audio settings

public class SettingsIO {
    public static final Path SETTINGS_FILE = ParserWriter.RESOURCE_ROOT.resolve(
            Paths.get("settings", ".settings"));

    private static final String FULLSCREEN = "fullscreen",
            PIXEL_LOCKING = "pixel-locking",
            DEFAULT_FOLLOW_MODE = "default-follow-mode",
            SHOW_TETHERS = "show-tethers",
            SHOW_COMBO = "show-combo",
            SHOW_FOLLOW_MODE_UPDATES = "show-follow-mode-updates",
            SHOW_DEBUG = "show-debug",
            SHOW_PIXEL_GRID = "show-pixel-grid";

    public static void read() {
        final String toParse = JBJGLFileIO.readFile(SETTINGS_FILE);

        final boolean fullscreen =
                Boolean.parseBoolean(ParserWriter.extractFromTag(FULLSCREEN, toParse));
        final boolean pixelLocking =
                Boolean.parseBoolean(ParserWriter.extractFromTag(PIXEL_LOCKING, toParse));
        final Camera.FollowMode defaultFollowMode =
                Camera.FollowMode.valueOf(ParserWriter.extractFromTag(DEFAULT_FOLLOW_MODE, toParse));
        final boolean showTethers =
                Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_TETHERS, toParse));
        final boolean showCombo =
                Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_COMBO, toParse));
        final boolean showFollowModeUpdates =
                Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_FOLLOW_MODE_UPDATES, toParse));
        final boolean showDebug =
                Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_DEBUG, toParse));
        final boolean showPixelGrid =
                Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_PIXEL_GRID, toParse));

        TechnicalSettings.setFullscreen(fullscreen);
        TechnicalSettings.setPixelLocked(pixelLocking);
        GameplaySettings.setDefaultFollowMode(defaultFollowMode);
        GameplaySettings.setShowingNecromancerTethers(showTethers);
        GameplaySettings.setShowingCombo(showCombo);
        GameplaySettings.setShowingFollowModeUpdates(showFollowModeUpdates);
        DebugSettings.setPrintDebug(showDebug);
        DebugSettings.setShowPixelGrid(showPixelGrid);
    }

    public static void write() {
        final String[] lines = new String[] {
                ParserWriter.encloseInTag(FULLSCREEN,
                        String.valueOf(TechnicalSettings.isFullscreen())),
                ParserWriter.encloseInTag(PIXEL_LOCKING,
                        String.valueOf(TechnicalSettings.isPixelLocked())),
                "",
                ParserWriter.encloseInTag(DEFAULT_FOLLOW_MODE,
                        GameplaySettings.getDefaultFollowMode().name()),
                ParserWriter.encloseInTag(SHOW_TETHERS,
                        String.valueOf(GameplaySettings.isShowingNecromancerTethers())),
                ParserWriter.encloseInTag(SHOW_COMBO,
                        String.valueOf(GameplaySettings.isShowingCombo())),
                ParserWriter.encloseInTag(SHOW_FOLLOW_MODE_UPDATES,
                        String.valueOf(GameplaySettings.isShowingFollowModeUpdates())),
                "",
                ParserWriter.encloseInTag(SHOW_DEBUG,
                        String.valueOf(DebugSettings.isPrintDebug())),
                ParserWriter.encloseInTag(SHOW_PIXEL_GRID,
                        String.valueOf(DebugSettings.isShowingPixelGrid())),
                ""
        };
        JBJGLFileIO.writeFile(SETTINGS_FILE, lines);
    }
}

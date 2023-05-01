package com.jordanbunke.translation.io;

import com.jordanbunke.jbjgl.events.JBJGLKey;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugSettings;

import java.nio.file.Path;
import java.util.*;

public class SettingsIO {
    public static final Path SETTINGS_FILE = ParserWriter.GAME_DATA_ROOT.resolve(".settings");

    private static final String FULLSCREEN = "fullscreen",
            PIXEL_ALIGNMENT = "pixel-alignment",
            PLAY_UI_SOUNDS = "ui-sounds",
            PLAY_MILESTONE_SOUNDS = "milestone-sounds",
            PLAY_PLAYER_SOUNDS = "player-sounds",
            PLAY_SENTRY_SOUNDS = "sentry-sounds",
            PLAY_ENVIRONMENT_SOUNDS = "environment-sounds",
            DEFAULT_FOLLOW_MODE = "default-follow-mode",
            SHOW_TETHERS = "show-tethers",
            SHOW_COMBO = "show-combo",
            SHOW_FOLLOW_MODE_UPDATES = "show-follow-mode-updates",
            SHOW_DEBUG = "show-debug",
            SHOW_PIXEL_GRID = "show-pixel-grid",
            JUMP = "jump-c", DROP = "drop-c",
            MOVE_LEFT = "move-left-c", MOVE_RIGHT = "move-right-c",
            TELEPORT = "tp-c", SAVE_POS = "save-pos-c", LOAD_POS = "load-pos-c",
            PAUSE = "pause-c",
            TOGGLE_ZOOM = "zoom-c", TOGGLE_FOLLOW_MODE = "follow-c",
            CAM_UP = "cam-up-c", CAM_DOWN = "cam-down-c",
            CAM_LEFT = "cam-left-c", CAM_RIGHT = "cam-right-c",
            SNAP_TO_GRID = "snap-to-grid-c"
            ;

    public static void read() {
        final String toParse = JBJGLFileIO.readFile(SETTINGS_FILE);

        final boolean
                // VIDEO
                fullscreen = Boolean.parseBoolean(ParserWriter.extractFromTag(FULLSCREEN, toParse)),
                pixelAlignment = Boolean.parseBoolean(ParserWriter.extractFromTag(PIXEL_ALIGNMENT, toParse)),

                // AUDIO
                playUISounds = Boolean.parseBoolean(ParserWriter.extractFromTag(PLAY_UI_SOUNDS, toParse)),
                playMilestoneSounds = Boolean.parseBoolean(ParserWriter.extractFromTag(PLAY_MILESTONE_SOUNDS, toParse)),
                playPlayerSounds = Boolean.parseBoolean(ParserWriter.extractFromTag(PLAY_PLAYER_SOUNDS, toParse)),
                playSentrySounds = Boolean.parseBoolean(ParserWriter.extractFromTag(PLAY_SENTRY_SOUNDS, toParse)),
                playEnvironmentSounds = Boolean.parseBoolean(ParserWriter.extractFromTag(PLAY_ENVIRONMENT_SOUNDS, toParse)),

                // GAMEPLAY
                showTethers = Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_TETHERS, toParse)),
                showCombo = Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_COMBO, toParse)),
                showFollowModeUpdates = Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_FOLLOW_MODE_UPDATES, toParse)),

                // TECHNICAL
                showDebug = Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_DEBUG, toParse)),
                showPixelGrid = Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_PIXEL_GRID, toParse));

        final Camera.FollowMode defaultFollowMode = Camera.FollowMode.valueOf(
                ParserWriter.extractFromTag(DEFAULT_FOLLOW_MODE, toParse));

        TechnicalSettings.setFullscreen(fullscreen);
        TechnicalSettings.setPixelAlignment(pixelAlignment);

        TechnicalSettings.setPlayUISounds(playUISounds);
        TechnicalSettings.setPlayMilestoneSounds(playMilestoneSounds);
        TechnicalSettings.setPlayPlayerSounds(playPlayerSounds);
        TechnicalSettings.setPlaySentrySounds(playSentrySounds);
        TechnicalSettings.setPlayEnvironmentSounds(playEnvironmentSounds);

        GameplaySettings.setDefaultFollowMode(defaultFollowMode);
        GameplaySettings.setShowingNecromancerTethers(showTethers);
        GameplaySettings.setShowingCombo(showCombo);
        GameplaySettings.setShowingFollowModeUpdates(showFollowModeUpdates);

        DebugSettings.setPrintDebug(showDebug);
        DebugSettings.setShowPixelGrid(showPixelGrid);

        readControls(toParse);
    }

    private static Map<ControlScheme.Action, String> getActionLabels() {
        return Map.ofEntries(
                Map.entry(ControlScheme.Action.JUMP, JUMP),
                Map.entry(ControlScheme.Action.DROP, DROP),
                Map.entry(ControlScheme.Action.MOVE_LEFT, MOVE_LEFT),
                Map.entry(ControlScheme.Action.STOP_MOVING_LEFT, MOVE_LEFT),
                Map.entry(ControlScheme.Action.MOVE_RIGHT, MOVE_RIGHT),
                Map.entry(ControlScheme.Action.STOP_MOVING_RIGHT, MOVE_RIGHT),
                Map.entry(ControlScheme.Action.TELEPORT, TELEPORT),
                Map.entry(ControlScheme.Action.INIT_TELEPORT, TELEPORT),
                Map.entry(ControlScheme.Action.SAVE_POS, SAVE_POS),
                Map.entry(ControlScheme.Action.LOAD_POS, LOAD_POS),
                Map.entry(ControlScheme.Action.PAUSE, PAUSE),
                Map.entry(ControlScheme.Action.TOGGLE_ZOOM, TOGGLE_ZOOM),
                Map.entry(ControlScheme.Action.TOGGLE_FOLLOW_MODE, TOGGLE_FOLLOW_MODE),
                Map.entry(ControlScheme.Action.MOVE_CAM_LEFT, CAM_LEFT),
                Map.entry(ControlScheme.Action.STOP_MOVING_CAM_LEFT, CAM_LEFT),
                Map.entry(ControlScheme.Action.MOVE_CAM_RIGHT, CAM_RIGHT),
                Map.entry(ControlScheme.Action.STOP_MOVING_CAM_RIGHT, CAM_RIGHT),
                Map.entry(ControlScheme.Action.MOVE_CAM_UP, CAM_UP),
                Map.entry(ControlScheme.Action.STOP_MOVING_CAM_UP, CAM_UP),
                Map.entry(ControlScheme.Action.MOVE_CAM_DOWN, CAM_DOWN),
                Map.entry(ControlScheme.Action.STOP_MOVING_CAM_DOWN, CAM_DOWN),
                Map.entry(ControlScheme.Action.SNAP_TO_GRID, SNAP_TO_GRID)
        );
    }

    private static void readControls(final String toParse) {
        final Map<ControlScheme.Action, String> actionLabels = getActionLabels();

        for (ControlScheme.Action action : actionLabels.keySet()) {
            String label = actionLabels.get(action);
            ControlScheme.update(action, JBJGLKey.valueOf(
                    ParserWriter.extractFromTag(label, toParse)));
        }
    }

    public static void write() {
        final StringBuilder sb = new StringBuilder();

        sb.append(ParserWriter.encloseInTag(FULLSCREEN,
                String.valueOf(TechnicalSettings.isFullscreen())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(PIXEL_ALIGNMENT,
                String.valueOf(TechnicalSettings.isPixelAlignment())));
        ParserWriter.newLineSB(sb);

        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(PLAY_UI_SOUNDS,
                String.valueOf(TechnicalSettings.isPlayUISounds())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(PLAY_MILESTONE_SOUNDS,
                String.valueOf(TechnicalSettings.isPlayMilestoneSounds())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(PLAY_PLAYER_SOUNDS,
                String.valueOf(TechnicalSettings.isPlayPlayerSounds())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(PLAY_SENTRY_SOUNDS,
                String.valueOf(TechnicalSettings.isPlaySentrySounds())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(PLAY_ENVIRONMENT_SOUNDS,
                String.valueOf(TechnicalSettings.isPlayEnvironmentSounds())));
        ParserWriter.newLineSB(sb);

        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(DEFAULT_FOLLOW_MODE,
                GameplaySettings.getDefaultFollowMode().name()));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(SHOW_TETHERS,
                String.valueOf(GameplaySettings.isShowingNecromancerTethers())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(SHOW_COMBO,
                String.valueOf(GameplaySettings.isShowingCombo())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(SHOW_FOLLOW_MODE_UPDATES,
                String.valueOf(GameplaySettings.isShowingFollowModeUpdates())));
        ParserWriter.newLineSB(sb);

        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(SHOW_DEBUG,
                String.valueOf(DebugSettings.isPrintDebug())));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(SHOW_PIXEL_GRID,
                String.valueOf(DebugSettings.isShowingPixelGrid())));
        ParserWriter.newLineSB(sb);

        ParserWriter.newLineSB(sb);

        writeControls(sb);

        JBJGLFileIO.writeFile(SETTINGS_FILE, sb.toString());
    }

    private static void writeControls(final StringBuilder sb) {
        final Map<ControlScheme.Action, String> actionLabels = getActionLabels();

        final List<ControlScheme.Action> actions = new ArrayList<>(actionLabels.keySet());
        final Comparator<ControlScheme.Action> comparator = Comparator.comparing(actionLabels::get);
        actions.sort(comparator);

        final Set<String> processedLabels = new HashSet<>();

        for (ControlScheme.Action action : actions) {
            String label = actionLabels.get(action);

            if (processedLabels.contains(label))
                continue;

            sb.append(ParserWriter.encloseInTag(label, String.valueOf(
                            ControlScheme.getCorrespondingKey(action))));
            ParserWriter.newLineSB(sb);

            processedLabels.add(label);
        }
    }
}

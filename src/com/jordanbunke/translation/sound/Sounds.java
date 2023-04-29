package com.jordanbunke.translation.sound;

import com.jordanbunke.jbjgl.sound.JBJGLAudioPlayer;
import com.jordanbunke.jbjgl.sound.JBJGLSound;
import com.jordanbunke.translation.ResourceManager;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.nio.file.Path;

public class Sounds {
    private static final Path SOUNDS_FOLDER = ResourceManager.getSoundsFolder();

    private static final int TOTAL_CONTINUOUS_SENTRY_TYPES = 7;
    public static final int CST_PUSHER = 0, CST_PULLER = 1, CST_SHOVER = 2,
            CST_SLIDER = 3, CST_CRUMBLER = 4, CST_BUILDER = 5, CST_REPELLER = 6;
    private static final boolean[] seenByContinuousSentryTypes = new boolean[Sounds.TOTAL_CONTINUOUS_SENTRY_TYPES];

    // UI sounds
    public static final JBJGLSound
            BUTTON_CLICK = JBJGLSound.fromResource("BUTTON_CLICK", SOUNDS_FOLDER.resolve("button_click.wav"), ResourceManager.class),
            SET_CONTROL = JBJGLSound.fromResource("SET_CONTROL", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            CHAR_TYPED = JBJGLSound.fromResource("CHAR_TYPED", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SET_TYPED_BOX = JBJGLSound.fromResource("SET_TYPED_BOX", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PAUSED_GAME = JBJGLSound.fromResource("PAUSED_GAME", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            RESUMED_GAME = JBJGLSound.fromResource("RESUMED_GAME", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // MILESTONE
            LEVEL_COMPLETE = JBJGLSound.fromResource("LEVEL_COMPLETE", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            FIRST_TIME_MAIN_MENU = JBJGLSound.fromResource("FIRST_TIME_MAIN_MENU", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // DISCRETE SENTRY
            COWARD_SAW_PLAYER = JBJGLSound.fromResource("COWARD_SAW_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            DROPPER_SAW_PLAYER = JBJGLSound.fromResource("DROPPER_SAW_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            BOUNCER_SAW_PLAYER = JBJGLSound.fromResource("BOUNCER_SAW_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            BOOSTER_SAW_PLAYER = JBJGLSound.fromResource("BOOSTER_SAW_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            INVERTER_SAW_PLAYER = JBJGLSound.fromResource("INVERTER_SAW_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // CONTINUOUS SENTRY
            PUSHER_SEES_PLAYER = JBJGLSound.fromResource("PUSHER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PULLER_SEES_PLAYER = JBJGLSound.fromResource("PULLER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SHOVER_SEES_PLAYER = JBJGLSound.fromResource("SHOVER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SLIDER_SEES_PLAYER = JBJGLSound.fromResource("SLIDER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            CRUMBLER_SEES_PLAYER = JBJGLSound.fromResource("CRUMBLER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            BUILDER_SEES_PLAYER = JBJGLSound.fromResource("BUILDER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            REPELLER_SEES_PLAYER = JBJGLSound.fromResource("REPELLER_SEES_PLAYER", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // LOADED SENTRY
            NOMAD_TELEPORTED = JBJGLSound.fromResource("NOMAD_TELEPORTED", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SENTRY_REVIVED_SUCCESSFULLY = JBJGLSound.fromResource("SENTRY_REVIVED_SUCCESSFULLY", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            NECROMANCER_FAIL = JBJGLSound.fromResource("NECROMANCER_FAIL", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SENTRY_SPAWNED_SUCCESSFULLY = JBJGLSound.fromResource("SENTRY_SPAWNED_SUCCESSFULLY", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SPAWNER_FAIL = JBJGLSound.fromResource("SPAWNER_FAIL", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // SENTRY EVENTS
            SENTRY_CRUSHED = JBJGLSound.fromResource("SENTRY_CRUSHED", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            SENTRY_UNTETHERED = JBJGLSound.fromResource("SENTRY_UNTETHERED", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // ENVIRONMENTAL
            DRAGGED_BY_MAGNET = JBJGLSound.fromResource("DRAGGED_BY_MAGNET", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            HEAVIER_GRAVITY = JBJGLSound.fromResource("HEAVIER_GRAVITY", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            LIGHTER_GRAVITY = JBJGLSound.fromResource("LIGHTER_GRAVITY", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),

    // DISCRETE PLAYER
            PLAYER_JUMP = JBJGLSound.fromResource("PLAYER_JUMP", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_LAND = JBJGLSound.fromResource("PLAYER_LAND", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_DROP = JBJGLSound.fromResource("PLAYER_DROP", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_DIVE = JBJGLSound.fromResource("PLAYER_DIVE", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_INIT_TELEPORT = JBJGLSound.fromResource("PLAYER_INIT_TELEPORT", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_TELEPORTED = JBJGLSound.fromResource("PLAYER_TELEPORTED", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_SAVED_POSITION = JBJGLSound.fromResource("PLAYER_SAVED_POSITION", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_LOADED_POSITION = JBJGLSound.fromResource("PLAYER_LOADED_POSITION", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class),
            PLAYER_LOAD_POSITION_FAILED = JBJGLSound.fromResource("PLAYER_LOAD_POSITION_FAILED", SOUNDS_FOLDER.resolve("example.wav"), ResourceManager.class);

    public static void init() {
        // reads sounds in (static initializers) triggered on static class entry
    }

    // SOUND TRIGGERS

    // UI sounds

    public static void buttonClick() {
        playUISound(BUTTON_CLICK);
    }

    public static void controlInputSet() {
        playUISound(SET_CONTROL);
    }

    public static void typedChar() {
        playUISound(CHAR_TYPED);
    }

    public static void typedBoxSet() {
        playUISound(SET_TYPED_BOX);
    }

    public static void gamePaused() {
        pauseContinuousLevelSounds();

        playUISound(PAUSED_GAME);
    }

    public static void gameResumed() {
        resumeContinuousLevelSounds();

        playUISound(RESUMED_GAME);
    }

    public static void completedLevel() {
        playMilestoneSound(LEVEL_COMPLETE);
    }

    public static void bootedUpMainMenu() {
        playMilestoneSound(FIRST_TIME_MAIN_MENU);
    }

    // TODO - camera sounds (as part of UI setting)

    // player sounds

    public static void playerJumped() {
        playDiscretePlayerSound(PLAYER_JUMP);
    }

    public static void playerLanded() {
        playDiscretePlayerSound(PLAYER_LAND);
    }

    public static void playerDropped() {
        playDiscretePlayerSound(PLAYER_DROP);
    }

    public static void playerDove() {
        playDiscretePlayerSound(PLAYER_DIVE);
    }

    public static void playerInitiatedTeleport() {
        playDiscretePlayerSound(PLAYER_INIT_TELEPORT);
    }

    public static void playerTeleported() {
        playDiscretePlayerSound(PLAYER_TELEPORTED);
    }

    public static void playerSavedPosition() {
        playDiscretePlayerSound(PLAYER_SAVED_POSITION);
    }

    public static void playerLoadedPosition() {
        playDiscretePlayerSound(PLAYER_LOADED_POSITION);
    }

    public static void playerCouldNotLoadPosition() {
        playDiscretePlayerSound(PLAYER_LOAD_POSITION_FAILED);
    }

    // sentry events

    public static void sentryCrushed() {
        playDiscreteSentrySound(SENTRY_CRUSHED);
    }

    public static void reanimatedSentryUntethered() {
        playDiscreteSentrySound(SENTRY_UNTETHERED);
    }

    // continuous sentry sounds

    public static void pusherSeesPlayer() {
        playContinuousSentrySound(PUSHER_SEES_PLAYER);
        seenByCSTIndex(CST_PUSHER);
    }

    public static void pullerSeesPlayer() {
        playContinuousSentrySound(PULLER_SEES_PLAYER);
        seenByCSTIndex(CST_PULLER);
    }

    public static void shoverSeesPlayer() {
        playContinuousSentrySound(SHOVER_SEES_PLAYER);
        seenByCSTIndex(CST_SHOVER);
    }

    public static void sliderSeesPlayer() {
        playContinuousSentrySound(SLIDER_SEES_PLAYER);
        seenByCSTIndex(CST_SLIDER);
    }

    public static void crumblerSeesPlayer() {
        playContinuousSentrySound(CRUMBLER_SEES_PLAYER);
        seenByCSTIndex(CST_CRUMBLER);
    }

    public static void builderSeesPlayer() {
        playContinuousSentrySound(BUILDER_SEES_PLAYER);
        seenByCSTIndex(CST_BUILDER);
    }

    public static void repellerSeesPlayer() {
        playContinuousSentrySound(REPELLER_SEES_PLAYER);
        seenByCSTIndex(CST_REPELLER);
    }

    private static void pusherDoesNotSeePlayer() {
        stopContinuousSentrySound(PUSHER_SEES_PLAYER);
    }

    private static void pullerDoesNotSeePlayer() {
        stopContinuousSentrySound(PULLER_SEES_PLAYER);
    }

    private static void shoverDoesNotSeePlayer() {
        stopContinuousSentrySound(SHOVER_SEES_PLAYER);
    }

    private static void sliderDoesNotSeePlayer() {
        stopContinuousSentrySound(SLIDER_SEES_PLAYER);
    }

    private static void crumblerDoesNotSeePlayer() {
        stopContinuousSentrySound(CRUMBLER_SEES_PLAYER);
    }

    private static void builderDoesNotSeePlayer() {
        stopContinuousSentrySound(BUILDER_SEES_PLAYER);
    }

    private static void repellerDoesNotSeePlayer() {
        stopContinuousSentrySound(REPELLER_SEES_PLAYER);
    }

    // TODO - discrete sentry sounds
    // TODO - loaded sentry sounds
    // TODO -
    // TODO
    // TODO

    // MANAGERS

    private static void pauseContinuousLevelSounds() {
        pauseContinuousSound(PUSHER_SEES_PLAYER);
        pauseContinuousSound(PULLER_SEES_PLAYER);
        pauseContinuousSound(SHOVER_SEES_PLAYER);
        pauseContinuousSound(SLIDER_SEES_PLAYER);
        pauseContinuousSound(CRUMBLER_SEES_PLAYER);
        pauseContinuousSound(BUILDER_SEES_PLAYER);
        pauseContinuousSound(REPELLER_SEES_PLAYER);

        // TODO - environmental
    }

    private static void resumeContinuousLevelSounds() {
        resumeContinuousSound(PUSHER_SEES_PLAYER);
        resumeContinuousSound(PULLER_SEES_PLAYER);
        resumeContinuousSound(SHOVER_SEES_PLAYER);
        resumeContinuousSound(SLIDER_SEES_PLAYER);
        resumeContinuousSound(CRUMBLER_SEES_PLAYER);
        resumeContinuousSound(BUILDER_SEES_PLAYER);
        resumeContinuousSound(REPELLER_SEES_PLAYER);

        // TODO - environmental
    }

    // SOUND GATEKEEPERS

    public static void playUISound(final JBJGLSound sound) {
        if (TechnicalSettings.isPlayUISounds())
            simplePlaySound(sound);
    }

    private static void playMilestoneSound(final JBJGLSound sound) {
        if (TechnicalSettings.isPlayMilestoneSounds())
            simplePlaySound(sound);
    }

    /** includes loaded sentry sounds */
    private static void playDiscreteSentrySound(final JBJGLSound sound) {
        if (TechnicalSettings.isPlaySentrySounds())
            simplePlaySound(sound);
    }

    private static void playDiscretePlayerSound(final JBJGLSound sound) {
        if (TechnicalSettings.isPlayPlayerSounds())
            simplePlaySound(sound);
    }

    private static void playContinuousSentrySound(final JBJGLSound sound) {
        if (TechnicalSettings.isPlaySentrySounds())
            playContinuousSound(sound);
    }

    private static void stopContinuousSentrySound(final JBJGLSound sound) {
        stopContinuousSound(sound);
    }

    private static void playEnvironmentalSound(final JBJGLSound sound) {
        if (TechnicalSettings.isPlayEnvironmentSounds())
            playContinuousSound(sound);
    }

    private static void stopEnvironmentalSound(final JBJGLSound sound) {
        stopContinuousSound(sound);
    }

    private static void simplePlaySound(final JBJGLSound sound) {
        if (sound != null)
            JBJGLAudioPlayer.playSoundInNewThread(sound);
    }

    private static void playContinuousSound(final JBJGLSound sound) {
        sound.loop();
    }

    private static void stopContinuousSound(final JBJGLSound sound) {
        sound.stop();
    }

    private static void pauseContinuousSound(final JBJGLSound sound) {
        sound.pause();
    }

    private static void resumeContinuousSound(final JBJGLSound sound) {
        sound.resume();
    }

    public static void resetSeenByCSTArray() {
        for (int i = 0; i < TOTAL_CONTINUOUS_SENTRY_TYPES; i++)
            seenByContinuousSentryTypes[i] = false;
    }

    private static void seenByCSTIndex(final int index) {
        seenByContinuousSentryTypes[index] = true;
    }

    public static void processSeenByCSTArray() {
        if (!seenByContinuousSentryTypes[CST_PUSHER]) pusherDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_PULLER]) pullerDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_SHOVER]) shoverDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_SLIDER]) sliderDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_CRUMBLER]) crumblerDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_BUILDER]) builderDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_REPELLER]) repellerDoesNotSeePlayer();
    }
}

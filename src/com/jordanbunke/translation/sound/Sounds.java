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
    private static int netGravity = 0, netMagnetPull = 0;
    private static long lastBouncerCall = 0L, lastBoosterCall = 0L,
            lastInverterCall = 0L, lastTickCall = 0L;
    private static final long BOUNCER_BOOSTER_THRESHOLD_MILLIS = 250L,
            INVERTER_THRESHOLD_MILLIS = 500L, TICK_THRESHOLD_MILLIS = 125L;

    public static final JBJGLSound
            BUTTON_CLICK = JBJGLSound.fromResource("BUTTON_CLICK", SOUNDS_FOLDER.resolve("button_click.wav"), ResourceManager.class),
            SET_CONTROL = JBJGLSound.fromResource("SET_CONTROL", SOUNDS_FOLDER.resolve("ui_set.wav"), ResourceManager.class),
            SET_TYPED_BOX = JBJGLSound.fromResource("SET_TYPED_BOX", SOUNDS_FOLDER.resolve("ui_set.wav"), ResourceManager.class),
            ZOOM_IN = JBJGLSound.fromResource("ZOOM_IN", SOUNDS_FOLDER.resolve("zoom_in.wav"), ResourceManager.class),
            ZOOM_OUT = JBJGLSound.fromResource("ZOOM_OUT", SOUNDS_FOLDER.resolve("zoom_out.wav"), ResourceManager.class),
            TOGGLE = JBJGLSound.fromResource("TOGGLE", SOUNDS_FOLDER.resolve("ui_set.wav"), ResourceManager.class),
            TICK = JBJGLSound.fromResource("TICK", SOUNDS_FOLDER.resolve("tick.wav"), ResourceManager.class),
            UI_FAILED = JBJGLSound.fromResource("UI_FAILED", SOUNDS_FOLDER.resolve("ui_failed.wav"), ResourceManager.class),
            PAUSED_GAME = JBJGLSound.fromResource("PAUSED_GAME", SOUNDS_FOLDER.resolve("paused.wav"), ResourceManager.class),
            RESUMED_GAME = JBJGLSound.fromResource("RESUMED_GAME", SOUNDS_FOLDER.resolve("resumed.wav"), ResourceManager.class),

    // MILESTONE
            LEVEL_COMPLETE = JBJGLSound.fromResource("LEVEL_COMPLETE", SOUNDS_FOLDER.resolve("level_complete.wav"), ResourceManager.class),
            LEVEL_FAILED = JBJGLSound.fromResource("LEVEL_FAILED", SOUNDS_FOLDER.resolve("level_failed.wav"), ResourceManager.class),
            FIRST_TIME_MAIN_MENU = JBJGLSound.fromResource("FIRST_TIME_MAIN_MENU", SOUNDS_FOLDER.resolve("game_loaded.wav"), ResourceManager.class),

    // DISCRETE SENTRY
            COWARD_SAW_PLAYER = JBJGLSound.fromResource("COWARD_SAW_PLAYER", SOUNDS_FOLDER.resolve("coward.wav"), ResourceManager.class),
            DROPPER_SAW_PLAYER = JBJGLSound.fromResource("DROPPER_SAW_PLAYER", SOUNDS_FOLDER.resolve("dropper.wav"), ResourceManager.class),
            BOUNCER_SAW_PLAYER = JBJGLSound.fromResource("BOUNCER_SAW_PLAYER", SOUNDS_FOLDER.resolve("bouncer.wav"), ResourceManager.class),
            BOOSTER_SAW_PLAYER = JBJGLSound.fromResource("BOOSTER_SAW_PLAYER", SOUNDS_FOLDER.resolve("booster.wav"), ResourceManager.class),
            INVERTER_SAW_PLAYER = JBJGLSound.fromResource("INVERTER_SAW_PLAYER", SOUNDS_FOLDER.resolve("inverter.wav"), ResourceManager.class),

    // CONTINUOUS SENTRY
            PUSHER_SEES_PLAYER = JBJGLSound.fromResource("PUSHER_SEES_PLAYER", SOUNDS_FOLDER.resolve("pusher.wav"), ResourceManager.class),
            PULLER_SEES_PLAYER = JBJGLSound.fromResource("PULLER_SEES_PLAYER", SOUNDS_FOLDER.resolve("puller.wav"), ResourceManager.class),
            SHOVER_SEES_PLAYER = JBJGLSound.fromResource("SHOVER_SEES_PLAYER", SOUNDS_FOLDER.resolve("shover.wav"), ResourceManager.class),
            SLIDER_SEES_PLAYER = JBJGLSound.fromResource("SLIDER_SEES_PLAYER", SOUNDS_FOLDER.resolve("slider.wav"), ResourceManager.class),
            CRUMBLER_SEES_PLAYER = JBJGLSound.fromResource("CRUMBLER_SEES_PLAYER", SOUNDS_FOLDER.resolve("crumbler.wav"), ResourceManager.class),
            BUILDER_SEES_PLAYER = JBJGLSound.fromResource("BUILDER_SEES_PLAYER", SOUNDS_FOLDER.resolve("builder.wav"), ResourceManager.class),
            REPELLER_SEES_PLAYER = JBJGLSound.fromResource("REPELLER_SEES_PLAYER", SOUNDS_FOLDER.resolve("repeller.wav"), ResourceManager.class),

    // LOADED SENTRY
            NOMAD_WANDERED = JBJGLSound.fromResource("NOMAD_WANDERED", SOUNDS_FOLDER.resolve("nomad.wav"), ResourceManager.class),
            SENTRY_REANIMATED_SUCCESSFULLY = JBJGLSound.fromResource("SENTRY_REANIMATED_SUCCESSFULLY", SOUNDS_FOLDER.resolve("reanimated.wav"), ResourceManager.class),
            NECROMANCER_FAIL = JBJGLSound.fromResource("NECROMANCER_FAIL", SOUNDS_FOLDER.resolve("sentry_load_failed.wav"), ResourceManager.class),
            SENTRY_SPAWNED_SUCCESSFULLY = JBJGLSound.fromResource("SENTRY_SPAWNED_SUCCESSFULLY", SOUNDS_FOLDER.resolve("spawned.wav"), ResourceManager.class),
            SPAWNER_FAIL = JBJGLSound.fromResource("SPAWNER_FAIL", SOUNDS_FOLDER.resolve("sentry_load_failed.wav"), ResourceManager.class),

    // SENTRY EVENTS
            SENTRY_CRUSHED = JBJGLSound.fromResource("SENTRY_CRUSHED", SOUNDS_FOLDER.resolve("crushed.wav"), ResourceManager.class),
            SENTRY_UNTETHERED = JBJGLSound.fromResource("SENTRY_UNTETHERED", SOUNDS_FOLDER.resolve("untethered.wav"), ResourceManager.class),

    // ENVIRONMENTAL
            DRAGGED_BY_MAGNET = JBJGLSound.fromResource("DRAGGED_BY_MAGNET", SOUNDS_FOLDER.resolve("dragged_by_magnet.wav"), ResourceManager.class),
            HEAVIER_GRAVITY = JBJGLSound.fromResource("HEAVIER_GRAVITY", SOUNDS_FOLDER.resolve("heavier_gravity.wav"), ResourceManager.class),
            LIGHTER_GRAVITY = JBJGLSound.fromResource("LIGHTER_GRAVITY", SOUNDS_FOLDER.resolve("lighter_gravity.wav"), ResourceManager.class),

    // DISCRETE PLAYER
            PLAYER_JUMP = JBJGLSound.fromResource("PLAYER_JUMP", SOUNDS_FOLDER.resolve("jump.wav"), ResourceManager.class),
            PLAYER_LAND = JBJGLSound.fromResource("PLAYER_LAND", SOUNDS_FOLDER.resolve("land.wav"), ResourceManager.class),
            PLAYER_DROP = JBJGLSound.fromResource("PLAYER_DROP", SOUNDS_FOLDER.resolve("drop.wav"), ResourceManager.class),
            PLAYER_DIVE = JBJGLSound.fromResource("PLAYER_DIVE", SOUNDS_FOLDER.resolve("dive.wav"), ResourceManager.class),
            PLAYER_INIT_TELEPORT = JBJGLSound.fromResource("PLAYER_INIT_TELEPORT", SOUNDS_FOLDER.resolve("init_teleport.wav"), ResourceManager.class),
            PLAYER_TELEPORTED = JBJGLSound.fromResource("PLAYER_TELEPORTED", SOUNDS_FOLDER.resolve("teleported.wav"), ResourceManager.class),
            PLAYER_SAVED_POSITION = JBJGLSound.fromResource("PLAYER_SAVED_POSITION", SOUNDS_FOLDER.resolve("saved_position.wav"), ResourceManager.class),
            PLAYER_LOADED_POSITION = JBJGLSound.fromResource("PLAYER_LOADED_POSITION", SOUNDS_FOLDER.resolve("loaded_position.wav"), ResourceManager.class),
            PLAYER_LOAD_POSITION_FAILED = JBJGLSound.fromResource("PLAYER_LOAD_POSITION_FAILED", SOUNDS_FOLDER.resolve("load_position_failed.wav"), ResourceManager.class);

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

    public static void typedBoxSet() {
        playUISound(SET_TYPED_BOX);
    }

    public static void zoomIn() {
        playUISound(ZOOM_IN);
    }

    public static void zoomOut() {
        playUISound(ZOOM_OUT);
    }

    public static void actionSucceeded() {
        playUISound(TOGGLE);
    }

    public static void tick() {
        final long now = System.currentTimeMillis();
        if (now - lastTickCall >= TICK_THRESHOLD_MILLIS) {
            lastTickCall = now;
            playDiscreteSentrySound(TICK);
        }
    }

    public static void actionFailed() {
        playUISound(UI_FAILED);
    }

    public static void gamePaused() {
        pauseContinuousLevelSounds();

        playUISound(PAUSED_GAME);
    }

    public static void gameResumed() {
        resumeContinuousLevelSounds();

        playUISound(RESUMED_GAME);
    }

    public static void failedLevel() {
        stopContinuousLevelSounds();

        playMilestoneSound(LEVEL_FAILED);
    }

    public static void completedLevel() {
        stopContinuousLevelSounds();

        playMilestoneSound(LEVEL_COMPLETE);
    }

    public static void bootedUpMainMenu() {
        playMilestoneSound(FIRST_TIME_MAIN_MENU);
    }

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

    // discrete sentry sounds

    public static void cowardSawPlayer() {
        playDiscreteSentrySound(COWARD_SAW_PLAYER);
    }

    public static void dropperSawPlayer() {
        playDiscreteSentrySound(DROPPER_SAW_PLAYER);
    }

    public static void boosterSawPlayer() {
        final long now = System.currentTimeMillis();
        if (now - lastBoosterCall >= BOUNCER_BOOSTER_THRESHOLD_MILLIS) {
            lastBoosterCall = now;
            playDiscreteSentrySound(BOOSTER_SAW_PLAYER);
        }
    }

    public static void bouncerSawPlayer() {
        final long now = System.currentTimeMillis();
        if (now - lastBouncerCall >= BOUNCER_BOOSTER_THRESHOLD_MILLIS) {
            lastBouncerCall = now;
            playDiscreteSentrySound(BOUNCER_SAW_PLAYER);
        }
    }

    public static void inverterSawPlayer() {
        final long now = System.currentTimeMillis();
        if (now - lastInverterCall >= INVERTER_THRESHOLD_MILLIS) {
            lastInverterCall = now;
            playDiscreteSentrySound(INVERTER_SAW_PLAYER);
        }
    }

    // loaded sentry sounds

    public static void nomadWandered() {
        playDiscreteSentrySound(NOMAD_WANDERED);
    }

    public static void sentrySpawnedSuccessfully() {
        playDiscreteSentrySound(SENTRY_SPAWNED_SUCCESSFULLY);
    }

    public static void spawnAttemptFailed() {
        playDiscreteSentrySound(SPAWNER_FAIL);
    }

    public static void sentryReanimatedSuccessfully() {
        playDiscreteSentrySound(SENTRY_REANIMATED_SUCCESSFULLY);
    }

    public static void reanimationAttemptFailed() {
        playDiscreteSentrySound(NECROMANCER_FAIL);
    }

    // environmental sounds

    private static void beingDraggedByMagnet() {
        playEnvironmentalSound(DRAGGED_BY_MAGNET);
    }

    private static void gravityIsHeavierThanNormal() {
        playEnvironmentalSound(HEAVIER_GRAVITY);
        stopEnvironmentalSound(LIGHTER_GRAVITY);
    }

    private static void gravityIsLighterThanNormal() {
        playEnvironmentalSound(LIGHTER_GRAVITY);
        stopEnvironmentalSound(HEAVIER_GRAVITY);
    }

    // MANAGERS

    private static void pauseContinuousLevelSounds() {
        pauseContinuousSound(PUSHER_SEES_PLAYER);
        pauseContinuousSound(PULLER_SEES_PLAYER);
        pauseContinuousSound(SHOVER_SEES_PLAYER);
        pauseContinuousSound(SLIDER_SEES_PLAYER);
        pauseContinuousSound(CRUMBLER_SEES_PLAYER);
        pauseContinuousSound(BUILDER_SEES_PLAYER);
        pauseContinuousSound(REPELLER_SEES_PLAYER);

        pauseContinuousSound(DRAGGED_BY_MAGNET);
        pauseContinuousSound(HEAVIER_GRAVITY);
        pauseContinuousSound(LIGHTER_GRAVITY);
    }

    private static void resumeContinuousLevelSounds() {
        resumeContinuousSound(PUSHER_SEES_PLAYER);
        resumeContinuousSound(PULLER_SEES_PLAYER);
        resumeContinuousSound(SHOVER_SEES_PLAYER);
        resumeContinuousSound(SLIDER_SEES_PLAYER);
        resumeContinuousSound(CRUMBLER_SEES_PLAYER);
        resumeContinuousSound(BUILDER_SEES_PLAYER);
        resumeContinuousSound(REPELLER_SEES_PLAYER);

        resumeContinuousSound(DRAGGED_BY_MAGNET);
        resumeContinuousSound(HEAVIER_GRAVITY);
        resumeContinuousSound(LIGHTER_GRAVITY);
    }

    private static void stopContinuousLevelSounds() {
        stopContinuousSentrySound(PUSHER_SEES_PLAYER);
        stopContinuousSentrySound(PULLER_SEES_PLAYER);
        stopContinuousSentrySound(SHOVER_SEES_PLAYER);
        stopContinuousSentrySound(SLIDER_SEES_PLAYER);
        stopContinuousSentrySound(CRUMBLER_SEES_PLAYER);
        stopContinuousSentrySound(BUILDER_SEES_PLAYER);
        stopContinuousSentrySound(REPELLER_SEES_PLAYER);

        stopEnvironmentalSound(DRAGGED_BY_MAGNET);
        stopEnvironmentalSound(HEAVIER_GRAVITY);
        stopEnvironmentalSound(LIGHTER_GRAVITY);
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

    public static void resetContinuousSentryData() {
        for (int i = 0; i < TOTAL_CONTINUOUS_SENTRY_TYPES; i++)
            seenByContinuousSentryTypes[i] = false;

        netGravity = 0;
        netMagnetPull = 0;
    }

    private static void seenByCSTIndex(final int index) {
        seenByContinuousSentryTypes[index] = true;
    }

    public static void incrementGravityTracker() {
        netGravity++;
    }

    public static void decrementGravityTracker() {
        netGravity--;
    }

    public static void updateMagnetTracker(final int magnetPull) {
        netMagnetPull += magnetPull;
    }

    public static void processContinuousSentryData() {
        if (!seenByContinuousSentryTypes[CST_PUSHER]) pusherDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_PULLER]) pullerDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_SHOVER]) shoverDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_SLIDER]) sliderDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_CRUMBLER]) crumblerDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_BUILDER]) builderDoesNotSeePlayer();
        if (!seenByContinuousSentryTypes[CST_REPELLER]) repellerDoesNotSeePlayer();

        if (DRAGGED_BY_MAGNET.isPlaying()) {
            if (netMagnetPull == 0) stopEnvironmentalSound(DRAGGED_BY_MAGNET);
        } else if (netMagnetPull != 0)
            beingDraggedByMagnet();

        if (LIGHTER_GRAVITY.isPlaying()) {
            if (netGravity >= 0) stopEnvironmentalSound(LIGHTER_GRAVITY);
        } else if (netGravity < 0)
            gravityIsLighterThanNormal();

        if (HEAVIER_GRAVITY.isPlaying()) {
            if (netGravity <= 0) stopEnvironmentalSound(HEAVIER_GRAVITY);
        } else if (netGravity > 0)
            gravityIsHeavierThanNormal();
    }
}

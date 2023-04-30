package com.jordanbunke.translation.settings;

import com.jordanbunke.jbjgl.utility.RenderConstants;

import java.awt.*;

public class TechnicalSettings {
    private static final int[] SCREEN_SIZE = new int[] {
            Toolkit.getDefaultToolkit().getScreenSize().getSize().width,
            Toolkit.getDefaultToolkit().getScreenSize().getSize().height
    };
    private static final int[] WINDOWED_SIZE = new int[] {
            1280, 720
    };
    private static final int PIXEL_SIZE = 4;

    // Mutable
    private static boolean
            fullscreen = true, pixelLocked = true,
            playUISounds = true, playMilestoneSounds = true,
            playSentrySounds = true, playPlayerSounds = true,
            playEnvironmentSounds = true;

    // Helpers
    public static int pixelLockNumber(final int n) {
        if (pixelLocked) {
            final int mod = n % getPixelSize();
            final boolean roundDown = mod <= getPixelSize() / 2;

            return roundDown ? n - mod : n + (getPixelSize() - mod);
        }
        return n;
    }

    // Getters
    public static int getPixelSize() {
        return PIXEL_SIZE;
    }

    private static int getScreenWidth() {
        return SCREEN_SIZE[RenderConstants.WIDTH];
    }

    private static int getScreenHeight() {
        return SCREEN_SIZE[RenderConstants.HEIGHT];
    }

    private static int getWindowedWidth() {
        return WINDOWED_SIZE[RenderConstants.WIDTH];
    }

    private static int getWindowedHeight() {
        return WINDOWED_SIZE[RenderConstants.HEIGHT];
    }

    public static int getWidth() {
        return fullscreen ? getScreenWidth() : getWindowedWidth();
    }

    public static int getHeight() {
        return fullscreen ? getScreenHeight() : getWindowedHeight();
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static boolean isPixelLocked() {
        return pixelLocked;
    }

    public static boolean isPlayUISounds() {
        return playUISounds;
    }

    public static boolean isPlayMilestoneSounds() {
        return playMilestoneSounds;
    }

    public static boolean isPlayPlayerSounds() {
        return playPlayerSounds;
    }

    public static boolean isPlaySentrySounds() {
        return playSentrySounds;
    }

    public static boolean isPlayEnvironmentSounds() {
        return playEnvironmentSounds;
    }

    // Setters
    public static void setPixelLocked(final boolean pixelLocked) {
        TechnicalSettings.pixelLocked = pixelLocked;
    }

    public static void setFullscreen(final boolean fullscreen) {
        TechnicalSettings.fullscreen = fullscreen;
    }

    public static void setPlayUISounds(final boolean playUISounds) {
        TechnicalSettings.playUISounds = playUISounds;
    }

    public static void setPlayMilestoneSounds(boolean playMilestoneSounds) {
        TechnicalSettings.playMilestoneSounds = playMilestoneSounds;
    }

    public static void setPlayPlayerSounds(boolean playPlayerSounds) {
        TechnicalSettings.playPlayerSounds = playPlayerSounds;
    }

    public static void setPlaySentrySounds(boolean playSentrySounds) {
        TechnicalSettings.playSentrySounds = playSentrySounds;
    }

    public static void setPlayEnvironmentSounds(boolean playEnvironmentSounds) {
        TechnicalSettings.playEnvironmentSounds = playEnvironmentSounds;
    }
}

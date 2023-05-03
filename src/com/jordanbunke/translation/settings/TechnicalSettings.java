package com.jordanbunke.translation.settings;

import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.Translation;

import java.awt.*;

public class TechnicalSettings {

    public enum Theme {
        CLASSIC, NIGHT;

        public Theme next() {
            final Theme[] all = values();
            return all[(ordinal() + 1) % all.length];
        }
    }

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
            fullscreen = true, pixelAlignment = true,
            playUISounds = true, playMilestoneSounds = true,
            playSentrySounds = true, playPlayerSounds = true,
            playEnvironmentSounds = true, fancyReticle = true;

    private static Theme theme = Theme.CLASSIC;

    // Helpers
    public static int pixelLockNumber(final int n) {
        if (pixelAlignment) {
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

    public static boolean isPixelAlignment() {
        return pixelAlignment;
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

    public static boolean isFancyReticle() {
        return fancyReticle;
    }

    public static Theme getTheme() {
        return theme;
    }

    // Setters
    public static void setPixelAlignment(final boolean pixelAlignment) {
        TechnicalSettings.pixelAlignment = pixelAlignment;
    }

    public static void setFullscreen(final boolean fullscreen) {
        TechnicalSettings.fullscreen = fullscreen;
    }

    public static void setPlayUISounds(final boolean playUISounds) {
        TechnicalSettings.playUISounds = playUISounds;
    }

    public static void setPlayMilestoneSounds(final boolean playMilestoneSounds) {
        TechnicalSettings.playMilestoneSounds = playMilestoneSounds;
    }

    public static void setPlayPlayerSounds(final boolean playPlayerSounds) {
        TechnicalSettings.playPlayerSounds = playPlayerSounds;
    }

    public static void setPlaySentrySounds(final boolean playSentrySounds) {
        TechnicalSettings.playSentrySounds = playSentrySounds;
    }

    public static void setPlayEnvironmentSounds(final boolean playEnvironmentSounds) {
        TechnicalSettings.playEnvironmentSounds = playEnvironmentSounds;
    }

    public static void setFancyReticle(final boolean fancyReticle) {
        TechnicalSettings.fancyReticle = fancyReticle;
    }

    public static void setTheme(final Theme theme) {
        if (TechnicalSettings.theme == theme)
            return;

        TechnicalSettings.theme = theme;

        Translation.themeWasChanged();
    }
}

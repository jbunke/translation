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
    private static boolean fullscreen = true;
    private static boolean pixelLocked = true;

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

    public static int getScreenWidth() {
        return SCREEN_SIZE[RenderConstants.WIDTH];
    }

    public static int getScreenHeight() {
        return SCREEN_SIZE[RenderConstants.HEIGHT];
    }

    public static int getWindowedWidth() {
        return WINDOWED_SIZE[RenderConstants.WIDTH];
    }

    public static int getWindowedHeight() {
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

    // Setters
    public static void setPixelLocked(final boolean pixelLocked) {
        TechnicalSettings.pixelLocked = pixelLocked;
    }

    public static void setFullscreen(boolean fullscreen) {
        TechnicalSettings.fullscreen = fullscreen;
    }
}

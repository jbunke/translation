package com.jordanbunke.translation.colors;

import com.jordanbunke.translation.settings.TechnicalSettings;

import java.awt.*;

public class TLColors {
    private static final int OPAQUE = 255;
    private static final int SHADOW = 150;
    private static final int FAINT = 50;

    private static final int BACKGROUND_RGB = 20;

    private static final Color TITLE_RED = new Color(255, 0, 0, OPAQUE);
    private static final Color BACKGROUND = new Color(
            BACKGROUND_RGB, BACKGROUND_RGB, BACKGROUND_RGB, OPAQUE);

    private static Color background = BACKGROUND;

    // ACCESSORS
    public static Color getInvertedThemeColor() {
        return switch (TechnicalSettings.getTheme()) {
            case CLASSIC, FRACTURED -> TLColors.BLACK();
            case NIGHT -> TLColors.BACKGROUND();
        };
    }

    public static Color getComplementaryMenuTextThemeColor() {
        return switch (TechnicalSettings.getTheme()) {
            case CLASSIC, FRACTURED -> TLColors.BLACK();
            case NIGHT -> TLColors.WHITE();
        };
    }

    public static Color getTooltipThemeColor() {
        return switch (TechnicalSettings.getTheme()) {
            case CLASSIC, FRACTURED -> TLColors.PLAYER();
            case NIGHT -> TLColors.WHITE();
        };
    }

    // COLORS
    public static Color TITLE_RED() {
        return TITLE_RED;
    }

    public static Color BLACK() {
        return BLACK(OPAQUE);
    }

    public static Color WHITE() {
        return WHITE(OPAQUE);
    }

    public static Color PLATFORM() {
        return PLATFORM(OPAQUE);
    }

    public static Color BLACK(final int opacity) {
        return new Color(0, 0, 0, opacity);
    }

    public static Color WHITE(final int opacity) {
        return new Color(255, 255, 255, opacity);
    }

    public static Color PLATFORM(final int opacity) {
        return new Color(100, 100, 100, opacity);
    }

    public static Color MENU_TEXT() {
        return PLATFORM();
    }

    public static Color BACKGROUND() {
        return background;
    }

    public static Color PLAYER(final int opacity) {
        return new Color(255, 0, 0, opacity);
    }

    public static Color PLAYER() {
        return PLAYER(OPAQUE);
    }

    public static Color DEBUG(final int opacity) {
        return new Color(0, 255, 0, opacity);
    }

    public static Color DEBUG() {
        return DEBUG(OPAQUE);
    }

    public static Color NEW_PB() {
        return new Color(0, 150, 0, OPAQUE);
    }

    public static Color SAME_AS_PB() {
        return PLATFORM(); // new Color(150, 120, 0, OPAQUE);
    }

    public static Color WORSE_THAN_PB() {
        return new Color(150, 0, 0, OPAQUE);
    }

    // SETTERS
    public static void setBackgroundToBlack() {
        background = BLACK();
    }

    // HELPERS
    public static int OPAQUE() {
        return OPAQUE;
    }

    public static int SHADOW() {
        return SHADOW;
    }

    public static int FAINT() {
        return FAINT;
    }

    public static Color colorAtOpacity(final Color c, final int opacity) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), opacity);
    }
}

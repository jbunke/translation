package com.jordanbunke.translation.colors;

import java.awt.*;

public class TLColors {
    private static final int OPAQUE = 255;
    private static final int SHADOW = 150;
    private static final int FAINT = 50;

    private static final int BACKGROUND_RGB = 20;

    private static final Color TITLE_RED = new Color(255, 0, 0, OPAQUE);
    private static final Color LINK = new Color(0, 0, 255, OPAQUE);
    private static final Color BACKGROUND = new Color(
            BACKGROUND_RGB, BACKGROUND_RGB, BACKGROUND_RGB, OPAQUE);

    // COLORS
    public static Color TITLE_RED() {
        return TITLE_RED;
    }

    public static Color LINK() {
        return LINK;
    }

    public static Color BLACK() {
        return BLACK(OPAQUE);
    }

    public static Color WHITE() {
        return WHITE(OPAQUE);
    }

    public static Color BLACK(final int opacity) {
        return new Color(0, 0, 0, opacity);
    }

    public static Color WHITE(final int opacity) {
        return new Color(255, 255, 255, opacity);
    }

    public static Color BACKGROUND() {
        return BACKGROUND;
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

    public static Color NEW_PB(final int opacity) {
        return new Color(0, 150, 0, opacity);
    }

    public static Color WORSE_THAN_PB(final int opacity) {
        return new Color(150, 0, 0, opacity);
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

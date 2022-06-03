package com.jordanbunke.translation.settings.debug;

public class DebugSettings {
    public static final int DEBUG_MESSAGE_TIMEOUT = 500;

    private static boolean showPixelGrid = false;
    private static boolean printDebug = false;

    public static boolean isShowingPixelGrid() {
        return showPixelGrid;
    }

    public static boolean isPrintDebug() {
        return printDebug;
    }

    public static void setShowPixelGrid(final boolean showPixelGrid) {
        DebugSettings.showPixelGrid = showPixelGrid;
    }

    public static void setPrintDebug(final boolean printDebug) {
        DebugSettings.printDebug = printDebug;
    }
}

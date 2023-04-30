package com.jordanbunke.translation.settings;

public class GameplayConstants {
    public static final int SPAWN_HEIGHT = 64; // pixels above starting platform for the player to spawn at

    public static final double UPDATE_HZ = 60.0;
    public static final double TARGET_FPS = 60.0;

    private static final int SQUARE_AND_PLATFORM_HEIGHT = 24;

    public static int SQUARE_LENGTH() {
        return SQUARE_AND_PLATFORM_HEIGHT;
    }

    public static int PLATFORM_HEIGHT() {
        return SQUARE_AND_PLATFORM_HEIGHT;
    }
}

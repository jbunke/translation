package com.jordanbunke.translation.settings;

import com.jordanbunke.translation.gameplay.Camera;

public class GameplaySettings {
    private static Camera.FollowMode defaultFollowMode = Camera.FollowMode.STEADY;
    private static boolean showingNecromancerTethers = true;

    // HUD
    private static boolean showingCombo = true;
    private static boolean showingFollowModeUpdates = true;

    public static void setDefaultFollowMode(Camera.FollowMode defaultFollowMode) {
        GameplaySettings.defaultFollowMode = defaultFollowMode;
    }

    public static Camera.FollowMode getDefaultFollowMode() {
        return defaultFollowMode;
    }

    public static void setShowingNecromancerTethers(final boolean showingNecromancerTethers) {
        GameplaySettings.showingNecromancerTethers = showingNecromancerTethers;
    }

    public static boolean isShowingNecromancerTethers() {
        return showingNecromancerTethers;
    }

    public static void setShowingCombo(final boolean showingCombo) {
        GameplaySettings.showingCombo = showingCombo;
    }

    public static void setShowingFollowModeUpdates(final boolean showingFollowModeUpdates) {
        GameplaySettings.showingFollowModeUpdates = showingFollowModeUpdates;
    }

    public static boolean isShowingCombo() {
        return showingCombo;
    }

    public static boolean isShowingFollowModeUpdates() {
        return showingFollowModeUpdates;
    }
}

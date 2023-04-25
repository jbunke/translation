package com.jordanbunke.translation.menus;

public class MenuIDs {
    public static final String
            SPLASH_SCREEN = "splash-screen",

            TITLE_CARD = "title-card",

            MAIN_MENU = "main-menu",
                PLAY_MENU = "play-menu",
                    CAMPAIGN_FOLDER = "campaign-f",
                        CAMPAIGN_LEVELS = "campaign-l",
                            LEVEL_OVERVIEW = "level-overview",
                                ARE_YOU_SURE_DELETE_LEVEL = "ays-dl-lo",
                    MY_CONTENT_MENU = "my-content-p",
                        NEW_CAMPAIGN = "new-campaign-mcp",
                SETTINGS = "settings",
                    GAMEPLAY_SETTINGS = "gameplay-s",
                    CONTROLS_SETTINGS = "controls-s",
                    VIDEO_SETTINGS = "video-s",
                    AUDIO_SETTINGS = "audio-s",
                    TECHNICAL_SETTINGS = "technical-s",
                GAME_MECHANICS = "game-mechanics",
                    SENTRIES_GM = "sentries-gm",
                        SENTRY_ROLE_GM = "sentry-role-gm",
                    PLATFORMS_GM = "platforms-gm",
                    MOVEMENT_RULES_GM = "movement-rules-gm",
                        JUMP_DROP_MOVEMENT_GM = "jump-drop-mgm",
                        TELEPORTATION_MOVEMENT_GM = "teleportation-mgm",
                        SAVE_LOAD_MOVEMENT_GM = "save-load-mgm",
                ABOUT = "about",
                    PATCH_NOTES = "patch-notes-a",
                    BACKGROUND_ABOUT = "background-a",
                    DEVELOPER_ABOUT = "developer-a",
                    FEEDBACK = "feedback-a",
                ARE_YOU_SURE_QUIT_GAME = "ays-qg",

            PAUSE_MENU = "pause-menu",
                ARE_YOU_SURE_PAUSE_QUIT_TO_MENU = "ays-p-qtm",
                ARE_YOU_SURE_PAUSE_RETURN_TO_EDITOR = "ays-p-rte",

            LEVEL_COMPLETE = "level-complete",
                STATS_LEVEL_COMPLETE = "stats-lc",
                SAVE_EDITOR_LEVEL = "save-editor-level-lc",
                    SAVED_EDITOR_LEVEL_CONFIRMATION = "saved-confirmation-sel-lc",

            EDITOR_MENU = "editor-menu",
                ARE_YOU_SURE_EDITOR_RESET = "ays-e-r",
                ARE_YOU_SURE_EDITOR_QUIT_TO_MENU = "ays-e-qtm",

            // NON-GAME MENUS
            CUSTOM_TEXT = "custom-text"
    ;

    public static boolean isAnEditorMenu(final String menuID) {
            return switch (menuID) {
                    case EDITOR_MENU, ARE_YOU_SURE_EDITOR_QUIT_TO_MENU,
                            ARE_YOU_SURE_EDITOR_RESET -> true;
                    default -> false;
            };
    }
}

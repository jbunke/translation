package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.events.JBJGLKey;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLImageIO;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.*;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.jbjgl.text.JBJGLTextComponent;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.menus.custom_elements.SetInputMenuElement;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugSettings;
import com.jordanbunke.translation.swatches.Swatches;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class Menus {
    // Timings
    private static final int SPLASH_SCREEN_1_FRAME_COUNT = 45;
    private static final int SPLASH_SCREEN_2_FRAME_COUNT = 146;
    private static final int SPLASH_SCREEN_1_TICKS_PER_FRAME = 10;
    private static final int SPLASH_SCREEN_2_TICKS_PER_FRAME = 5;
    private static final int TITLE_CARD_TICKS = 200;

    // Positioning
    private static final int LIST_MENU_INITIAL_Y = 140;
    private static final int LIST_MENU_INCREMENT_Y = TechnicalSettings.getHeight() / 8;
    private static final int MENU_TITLE_Y = 0;
    private static final int MENU_SUBTITLE_Y = LIST_MENU_INITIAL_Y;
    private static final int MENU_TEXT_INCREMENT_Y = TechnicalSettings.getHeight() / 12;

    // MENU MANAGER GENERATORS

    public static JBJGLMenuManager generatePauseMenuManager(final Level level) {
        return JBJGLMenuManager.create(new HashMap<>(Map.ofEntries(
                Map.entry(MenuIDs.PAUSE_MENU, generatePauseMenu(level)),
                Map.entry(MenuIDs.ARE_YOU_SURE_PAUSE_QUIT_TO_MENU,
                        generateAreYouSureQuitToMainMenu(
                                () -> Translation.pauseState.getMenuManager()
                                                .setActiveMenuID(MenuIDs.PAUSE_MENU)
                        )),
                Map.entry(MenuIDs.SETTINGS_PAUSE, generateSettingsMenu(false)),
                Map.entry(MenuIDs.GAMEPLAY_SETTINGS, generateGameplaySettingsMenu(false)),
                Map.entry(MenuIDs.CONTROLS_SETTINGS, generateControlsSettingsMenu(false)),
                Map.entry(MenuIDs.VIDEO_SETTINGS, generateVideoSettingsMenu(false)),
                Map.entry(MenuIDs.AUDIO_SETTINGS, generateAudioSettingsMenu(false)),
                Map.entry(MenuIDs.FOR_NERDS_SETTINGS, generateSettingsForNerdsMenu(false))
                )), MenuIDs.PAUSE_MENU);
    }

    public static JBJGLMenuManager generateLevelCompleteMenuManager(final Level level) {
        return JBJGLMenuManager.create(Map.ofEntries(
                Map.entry(MenuIDs.LEVEL_COMPLETE, generateLevelCompleteMenu(level)),
                Map.entry(MenuIDs.STATS_LEVEL_COMPLETE, generateLevelCompleteStatsPage(level))
        ), MenuIDs.LEVEL_COMPLETE);
    }

    public static JBJGLMenuManager generateMenuManager() {
        return JBJGLMenuManager.create(new HashMap<>(Map.ofEntries(
                Map.entry(MenuIDs.TITLE_CARD, generateTitleCard()),
                Map.entry(MenuIDs.MAIN_MENU, generateMainMenu()),
                Map.entry(MenuIDs.CAMPAIGNS_MENU, generateCampaignsMenu()),
                Map.entry(MenuIDs.WIKI, generateWikiMenu()),
                Map.entry(MenuIDs.SENTRIES_WIKI, generateSentriesWikiPage()),
                Map.entry(MenuIDs.SENTRY_ROLE_WIKI, generatePageDoesNotExistYet()), // uninitialized
                Map.entry(MenuIDs.PLATFORMS_WIKI, generatePlatformWikiPage()),
                Map.entry(MenuIDs.MOVEMENT_RULES_WIKI, generateMovementRulesWikiPage()),
                Map.entry(MenuIDs.JUMP_DROP_MOVEMENT_WIKI, generateJumpDiveWikiPage()),
                Map.entry(MenuIDs.TELEPORTATION_MOVEMENT_WIKI, generateTeleportationWikiPage()),
                Map.entry(MenuIDs.SAVE_LOAD_MOVEMENT_WIKI, generateSaveLoadWikiPage()),
                Map.entry(MenuIDs.ABOUT, generateAboutMenu()),
                Map.entry(MenuIDs.BACKGROUND_ABOUT, generateBackgroundAboutPage()),
                Map.entry(MenuIDs.DEVELOPER_ABOUT, generateDeveloperAboutPage()),
                // TODO - feedback; currently omitted for performance
                Map.entry(MenuIDs.SETTINGS_MAIN_MENU, generateSettingsMenu(true)),
                Map.entry(MenuIDs.GAMEPLAY_SETTINGS, generateGameplaySettingsMenu(true)),
                Map.entry(MenuIDs.CONTROLS_SETTINGS, generateControlsSettingsMenu(true)),
                Map.entry(MenuIDs.VIDEO_SETTINGS, generateVideoSettingsMenu(true)),
                Map.entry(MenuIDs.AUDIO_SETTINGS, generateAudioSettingsMenu(true)),
                Map.entry(MenuIDs.FOR_NERDS_SETTINGS, generateSettingsForNerdsMenu(true)),
                Map.entry(MenuIDs.ARE_YOU_SURE_QUIT_GAME, generateAreYouSureQuitGame())
        )), MenuIDs.TITLE_CARD);
    }

    public static JBJGLMenuManager generateSplashScreenManager() {
        return JBJGLMenuManager.initialize(
                Utility.coinToss(0.8,
                        generateSplashScreen1(),
                        generateSplashScreen2()
                ), MenuIDs.SPLASH_SCREEN
        );
    }

    // MENU GENERATORS

    private static JBJGLMenu generateMainMenu() {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateElementTitle(coordinateFromFraction(width, 0.5),
                        MENU_TITLE_Y, JBJGLMenuElement.Anchor.CENTRAL_TOP),
                generateListMenuOptions(
                        new String[] {
                                "PLAY", "LEVEL EDITOR", "SETTINGS",
                                "WIKI", "ABOUT", "QUIT"
                        },
                        new Runnable[] {
                                // () -> Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.CAMPAIGNS_MENU),
                                null, // TODO - editor
                                () -> Translation.menuManager
                                        .setActiveMenuID(MenuIDs.SETTINGS_MAIN_MENU),
                                () -> Translation.menuManager
                                        .setActiveMenuID(MenuIDs.WIKI),
                                () -> Translation.menuManager
                                        .setActiveMenuID(MenuIDs.ABOUT),
                                () -> Translation.menuManager
                                        .setActiveMenuID(MenuIDs.ARE_YOU_SURE_QUIT_GAME)
                        }
                ),
                JBJGLTextMenuElement.generate(
                        new int[] { pixel, height }, JBJGLMenuElement.Anchor.LEFT_BOTTOM,
                        generateInitialMenuTextBuilder().setColor(Swatches.PLAYER(Swatches.OPAQUE()))
                                .addText("v. " + Translation.VERSION).addLineBreak()
                                .addText("Jordan Bunke, 2022").build()
                )
        );
    }

    private static JBJGLMenu generateCampaignsMenu() {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("CAMPAIGNS"),
                generateListMenuOptions(
                        new String[] { "BACK", "MAIN", "TUTORIAL",
                                "MY CAMPAIGNS", "IMPORTED CAMPAIGNS", "IMPORT NEW" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU),
                                () -> Translation.menuManager.addMenu(
                                        MenuIDs.CAMPAIGN_FOLDER,
                                        generateCampaignFolderMenu(
                                                "MAIN CAMPAIGN",
                                                LevelIO.readCampaignsInFolder(LevelIO.MAIN_CAMPAIGNS_FOLDER),
                                                MenuIDs.CAMPAIGNS_MENU, 0
                                        ), true
                                ),
                                () -> {
                                    Translation.campaign =
                                            LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER);
                                    Translation.menuManager.addMenu(
                                            MenuIDs.CAMPAIGN_LEVELS,
                                            generateMenuForCampaign(Translation.campaign, MenuIDs.CAMPAIGNS_MENU),
                                            true);
                                },
                                // TODO - link my campaigns, imported campaigns, and implement importing
                                null,
                                null,
                                null
                        }
                )
        );
    }

    private static JBJGLMenu generateCampaignFolderMenu(
            final String title, final Campaign[] campaigns,
            final String backMenuID, final int page
    ) {
        final JBJGLMenuElementGrouping campaignsOnPage =
                generateCampaignsOnPage(title, campaigns, backMenuID, page);

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle(title.toUpperCase()),
                generateTextMenuSubtitle("PAGE " + (page + 1)),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(backMenuID)
                        }, LIST_MENU_INCREMENT_Y
                ),
                campaignsOnPage
        );
    }

    private static JBJGLMenu generateMenuForCampaign(
            final Campaign campaign, final String backMenuID
    ) {
        return generateMenuForCampaign(campaign, backMenuID, 0);
    }

    private static JBJGLMenu generateMenuForCampaign(
            final Campaign campaign, final String backMenuID, final int page
    ) {
        final JBJGLMenuElementGrouping levelsOnPage =
                generateLevelsOnPage(campaign, backMenuID, page);

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle(campaign.getName().toUpperCase()),
                generateTextMenuSubtitle("CAMPAIGN, PAGE " + (page + 1)),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(backMenuID)
                        }, LIST_MENU_INCREMENT_Y
                ),
                levelsOnPage
        );
    }

    private static JBJGLMenu generateLevelOverview(
            final Level level, final int index
    ) {
        final int height = TechnicalSettings.getHeight();

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle(level.getName().toUpperCase()),
                generateTextMenuSubtitle(level.getHint().toUpperCase()),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.CAMPAIGN_LEVELS)
                        }, LIST_MENU_INCREMENT_Y
                ),
                generateListMenuOptions(
                        new String[] { "PLAY" },
                        new Runnable[] {
                                () -> {
                                    Translation.campaign.setLevel(index);
                                    Translation.campaign.getLevel().getStats().reset();
                                    Translation.campaign.getLevel().launchLevel();
                                    Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX);
                                }
                        },
                        height - (LIST_MENU_INITIAL_Y + (int)(1.2 * LIST_MENU_INCREMENT_Y))
                ),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                coordinateFromFraction(TechnicalSettings.getWidth(), 0.4),
                                LIST_MENU_INITIAL_Y + (int)(1.9 * LIST_MENU_INCREMENT_Y)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        generateInitialMenuTextBuilder().addText(
                                level.isBlind()
                                        ? "BLIND"
                                        : "SIGHTED"
                        ).build()
                ),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                coordinateFromFraction(TechnicalSettings.getWidth(), 0.6),
                                LIST_MENU_INITIAL_Y + (int)(1.9 * LIST_MENU_INCREMENT_Y)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        generateInitialMenuTextBuilder().addText(
                                level.isDeterministic()
                                        ? "DETERMINISTIC"
                                        : "NON-DETERMINISTIC"
                        ).build()
                ),
                generateLevelPBText(level.getStats())
        );
    }

    private static JBJGLMenu generateSettingsMenu(final boolean isMainMenu) {
        final String backPageLinksTo = isMainMenu
                ? MenuIDs.MAIN_MENU
                : MenuIDs.PAUSE_MENU;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("SETTINGS"),
                generateListMenuOptions(
                        new String[] { "BACK", "GAMEPLAY", "CONTROLS", "VIDEO", "AUDIO", "FOR NERDS" },
                        new Runnable[] {
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, backPageLinksTo),
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, MenuIDs.GAMEPLAY_SETTINGS),
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, MenuIDs.CONTROLS_SETTINGS),
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, MenuIDs.VIDEO_SETTINGS),
                                null, // TODO - runRegularOrPauseMenuManagerUpdate(isMainMenu, MenuIDs.AUDIO_SETTINGS),
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, MenuIDs.FOR_NERDS_SETTINGS)
                        }
                )
        );
    }

    private static JBJGLMenu generateGameplaySettingsMenu(final boolean isMainMenu) {
        final String settingsPage = isMainMenu
                ? MenuIDs.SETTINGS_MAIN_MENU
                : MenuIDs.SETTINGS_PAUSE;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("GAMEPLAY SETTINGS"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, settingsPage)
                        }
                ),
                generateListMenuToggleOptions(
                        new String[] {
                                "DEFAULT CAMERA MODE", "SHOW CAMERA UPDATES",
                                "SHOW COMBO", "SHOW TETHERS"
                        },
                        new String[][] {
                                new String[] {
                                        Camera.FollowMode.STEADY.toString(),
                                        Camera.FollowMode.GLUED.toString(),
                                        Camera.FollowMode.FIXED.toString()
                                },
                                new String[] { "ON", "OFF" },
                                new String[] { "ON", "OFF" },
                                new String[] { "ON", "OFF" }
                        },
                        new Runnable[][] {
                                new Runnable[] {
                                        () -> GameplaySettings.setDefaultFollowMode(
                                                GameplaySettings.getDefaultFollowMode().next()),
                                        () -> GameplaySettings.setDefaultFollowMode(
                                                GameplaySettings.getDefaultFollowMode().next()),
                                        () -> GameplaySettings.setDefaultFollowMode(
                                                GameplaySettings.getDefaultFollowMode().next())
                                },
                                new Runnable[] {
                                        () -> GameplaySettings.setShowingFollowModeUpdates(false),
                                        () -> GameplaySettings.setShowingFollowModeUpdates(true)
                                },
                                new Runnable[] {
                                        () -> GameplaySettings.setShowingCombo(false),
                                        () -> GameplaySettings.setShowingCombo(true)
                                },
                                new Runnable[] {
                                        () -> GameplaySettings.setShowingNecromancerTethers(false),
                                        () -> GameplaySettings.setShowingNecromancerTethers(true)
                                }
                        },
                        new Callable[] {
                                () -> GameplaySettings.getDefaultFollowMode().ordinal(),
                                () -> GameplaySettings.isShowingFollowModeUpdates() ? 0 : 1,
                                () -> GameplaySettings.isShowingCombo() ? 0 : 1,
                                () -> GameplaySettings.isShowingNecromancerTethers() ? 0 : 1
                        }, LIST_MENU_INCREMENT_Y
                )
        );
    }

    private static JBJGLMenu generateControlsSettingsMenu(final boolean isMainMenu) {
        final String settingsPage = isMainMenu
                ? MenuIDs.SETTINGS_MAIN_MENU
                : MenuIDs.SETTINGS_PAUSE;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("CONTROLS"),
                generateListMenuOptions(
                        new String[] { "BACK", "RESET" },
                        new Runnable[] {
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, settingsPage),
                                ControlScheme::reset
                        }
                ),
                generateControlsButtons(LIST_MENU_INCREMENT_Y * 2)
        );
    }

    private static JBJGLMenu generateVideoSettingsMenu(final boolean isMainMenu) {
        final String settingsPage = isMainMenu
                ? MenuIDs.SETTINGS_MAIN_MENU
                : MenuIDs.SETTINGS_PAUSE;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("VIDEO SETTINGS"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, settingsPage)
                        }
                ),
                generateListMenuToggleOptions(
                        new String[] { "FULLSCREEN", "PIXEL LOCKING" },
                        new String[][] {
                                new String[] { "ON", "OFF" },
                                new String[] { "ON", "OFF" }
                        },
                        new Runnable[][] {
                                new Runnable[] {
                                        () -> Translation.resize(false),
                                        () -> Translation.resize(true)
                                },
                                new Runnable[] {
                                        () -> TechnicalSettings.setPixelLocked(false),
                                        () -> TechnicalSettings.setPixelLocked(true)
                                }
                        },
                        new Callable[] {
                                () -> TechnicalSettings.isFullscreen() ? 0 : 1,
                                () -> TechnicalSettings.isPixelLocked() ? 0 : 1
                        }, LIST_MENU_INCREMENT_Y
                )
        );
    }

    private static JBJGLMenu generateAudioSettingsMenu(final boolean isMainMenu) {
        final String settingsPage = isMainMenu
                ? MenuIDs.SETTINGS_MAIN_MENU
                : MenuIDs.SETTINGS_PAUSE;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("AUDIO SETTINGS"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, settingsPage)
                        }
                )
                // TODO - audio settings toggle options and sliders
        );
    }

    private static JBJGLMenu generateSettingsForNerdsMenu(final boolean isMainMenu) {
        final String settingsPage = isMainMenu
                ? MenuIDs.SETTINGS_MAIN_MENU
                : MenuIDs.SETTINGS_PAUSE;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("SETTINGS FOR NERDS"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                runRegularOrPauseMenuManagerUpdate(isMainMenu, settingsPage)
                        }
                ),
                generateListMenuToggleOptions(
                        new String[] { "DEBUG LOG", "PIXEL GRID" },
                        new String[][] {
                                new String[] { "ON", "OFF" },
                                new String[] { "ON", "OFF" }
                        },
                        new Runnable[][] {
                                new Runnable[] {
                                        () -> DebugSettings.setPrintDebug(false),
                                        () -> DebugSettings.setPrintDebug(true)
                                },
                                new Runnable[] {
                                        () -> DebugSettings.setShowPixelGrid(false),
                                        () -> DebugSettings.setShowPixelGrid(true)
                                }
                        },
                        new Callable[] {
                                () -> DebugSettings.isPrintDebug() ? 0 : 1,
                                () -> DebugSettings.isShowingPixelGrid() ? 0 : 1
                        }, LIST_MENU_INCREMENT_Y
                )
        );
    }

    private static JBJGLMenu generateWikiMenu() {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("WIKI"),
                generateTextMenuSubtitle("BRIEF DESCRIPTIONS OF THE GAME'S SYSTEMS"),
                generateListMenuOptions(
                        new String[] { "BACK", "SENTRIES", "PLATFORMS", "MOVEMENT" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.SENTRIES_WIKI),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.PLATFORMS_WIKI),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.MOVEMENT_RULES_WIKI)
                        },
                        LIST_MENU_INCREMENT_Y
                )
        );
    }

    private static JBJGLMenu generateSentriesWikiPage() {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("SENTRIES"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.WIKI)
                        }
                ),
                generateMenuTextBlurb(
                        coordinateFromFraction(TechnicalSettings.getHeight(), 0.33),
                        new String[] {
                                ("Sentries patrol the platforms. Each type of " +
                                        "sentry has a unique ability, ranging from pushing").toUpperCase(),
                                ("the player to reviving crushed sentries. " +
                                        "A level is completed when all sentries are crushed.").toUpperCase()
                        }, 1
                ),
                generateSentryButtons()
        );
    }

    private static JBJGLMenu generateSentryRoleWikiPage(final Sentry.Role role) {
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle(role.name().toUpperCase()),
                generateTextMenuSubtitle("SENTRY TYPE"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.SENTRIES_WIKI)
                        }, LIST_MENU_INCREMENT_Y
                ),
                JBJGLStaticMenuElement.generate(
                        new int[] {
                                coordinateFromFraction(TechnicalSettings.getWidth(), 0.5),
                                coordinateFromFraction(TechnicalSettings.getHeight(), 0.5)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        ImageAssets.drawSentry(role)
                ),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                coordinateFromFraction(TechnicalSettings.getWidth(), 0.48),
                                coordinateFromFraction(TechnicalSettings.getHeight(), 0.5)
                        }, JBJGLMenuElement.Anchor.RIGHT_TOP,
                        generateInitialMenuTextBuilder().addText(
                                role.isSightDependent()
                                        ? "RELIES ON SIGHT"
                                        : "DOES NOT RELY ON SIGHT"
                        ).build()
                ),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                coordinateFromFraction(TechnicalSettings.getWidth(), 0.52) + pixel,
                                coordinateFromFraction(TechnicalSettings.getHeight(), 0.5)
                        }, JBJGLMenuElement.Anchor.LEFT_TOP,
                        generateInitialMenuTextBuilder().addText(
                                role.isDeterministic()
                                        ? "DETERMINISTIC BEHAVIOUR"
                                        : "NON-DETERMINISTIC BEHAVIOUR"
                        ).build()
                ),
                generateMenuTextBlurb(
                        coordinateFromFraction(TechnicalSettings.getHeight(), 0.57),
                        switch (role) {
                            case SHOVER -> new String[] {
                                    "A SUPER-POWERED VERSION OF THE " +
                                            Sentry.Role.PUSHER.name().toUpperCase() + ".",
                                    "PUSHES THE PLAYER " +
                                            Sentry.SHOVE_FACTOR + "x AS HARD ON SIGHT."
                            };
                            case PUSHER -> new String[] {
                                    "ON SIGHT, PUSHES THE PLAYER BY TELEKINESIS WITH",
                                    "THE SPEED THE PULLER IS MOVING."
                            };
                            case PULLER -> new String[] {
                                    "ON SIGHT, PULLS THE PLAYER BY TELEKINESIS WITH",
                                    "THE SPEED THE PULLER IS MOVING."
                            };
                            case DROPPER -> new String[] {
                                    "ON SIGHT, DROPS THE PLAYER'S VERTICAL POSITION",
                                    "TO DIRECTLY BELOW THE PLATFORM BEING PATROLLED."
                            };
                            case SLIDER -> new String[] {
                                    "ON SIGHT, PULLS THE PLATFORM AWAY FROM THE PLAYER",
                                    "AT THE SPEED THE SLIDER IS MOVING."
                            };
                            case BUILDER -> new String[] {
                                    "ON SIGHT, EXTENDS THE PLATFORM UNTIL IT REACHES",
                                    "A MAXIMUM WIDTH OF " + Sentry.MAX_PLATFORM_WIDTH + " PIXELS."
                            };
                            case CRUMBLER -> new String[] {
                                    "ON SIGHT, SHORTENS THE PLATFORM UNTIL IT REACHES",
                                    "A MINIMUM WIDTH JUST WIDE ENOUGH TO HOLD THE SENTRY."
                            };
                            case FEATHER -> new String[] {
                                    "DECREASES GRAVITY BY " + Sentry.GRAVITY_FACTOR + " PIX/TICK^2."
                            };
                            case ANCHOR -> new String[] {
                                    "INCREASES GRAVITY BY " + Sentry.GRAVITY_FACTOR + " PIX/TICK^2."
                            };
                            case BOOSTER -> new String[] {
                                    "DOUBLES VERTICAL VELOCITY ON SIGHT."
                            };
                            case BOUNCER -> new String[] {
                                    "NEGATES VERTICAL VELOCITY ON SIGHT (* -1)."
                            };
                            case REPELLER -> new String[] {
                                    "ON SIGHT, \"REPELS\" ALL PLATFORMS FROM ITS OWN.",
                                    "REPULSION SPEED IS PROPORTIONAL TO MOVEMENT",
                                    "SPEED."
                            };
                            case INVERTER -> new String[] {
                                    "ON SIGHT, THE INVERTER FLIPS THE STAGE AROUND",
                                    "THE X-AXIS TO MAKE IT APPEAR UPSIDE-DOWN."
                            };
                            case MAGNET -> new String[] {
                                    "DRAWS IN THE PLAYER ALONG THE X-AXIS.",
                                    "MAGNETISM IS PROPORTIONAL WITH MOVEMENT SPEED."
                            };
                            case COWARD -> new String[] {
                                    "ON SIGHT, THE COWARD FLEES TO A DIFFERENT,",
                                    "RANDOMLY-SELECTED PLATFORM."
                            };
                            case SPAWNER -> new String[] {
                                    "EVERY " + gameTicksToNearestSecond(Sentry.SPAWN_CYCLE) +
                                            " SECONDS, SPAWNS A NEW SENTRY UNTIL",
                                    "THE SPAWNER HAS SPAWNED A MAXIMUM OF " + Sentry.MAX_CHILDREN_SPAWNABLE,
                                    "CHILDREN. THE TYPE OF CHILDREN SPAWNED ARE PART",
                                    "OF THE SPAWNER'S SPECIFICATION."
                            };
                            case NOMAD -> new String[] {
                                    "EVERY " + gameTicksToNearestSecond(Sentry.NOMADIC_CYCLE) +
                                            " SECONDS, THE NOMAD MOVES TO A",
                                    "DIFFERENT, RANDOMLY-SELECTED PLATFORM."
                            };
                            case NECROMANCER -> new String[] {
                                    "EVERY " + gameTicksToNearestSecond(Sentry.REVIVAL_CYCLE) +
                                            " SECONDS, ATTEMPTS TO REVIVE A CRUSHED",
                                    "SENTRY. ANY REVIVED SENTRY IS \"TETHERED\"",
                                    "TO THE NECROMANCER, AND IS MAGICALLY CRUSHED",
                                    "AGAIN IF THE NECROMANCER IS CRUSHED."
                            };
                            case RANDOM -> new String[] {
                                    "RANDOMLY CHOOSES BETWEEN THE OTHER SENTRY",
                                    "TYPES AND BECOMES IT ON THE FIRST GAME",
                                    "TICK."
                            };
                        }, 2
                )
        );
    }

    private static JBJGLMenu generatePlatformWikiPage() {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("PLATFORMS"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.WIKI)
                        },
                        LIST_MENU_INCREMENT_Y
                ),
                generateMenuTextBlurb(
                        coordinateFromFraction(TechnicalSettings.getHeight(), 0.5),
                        new String[] {
                                "Platforms make up the \"stage\" of each level.".toUpperCase(),
                                "The player starts on the starting platform,".toUpperCase(),
                                "where no sentry can begin the level. Each".toUpperCase(),
                                "sentry has a platform that it patrols.".toUpperCase()
                        }, 2
                )
        );
    }

    private static JBJGLMenu generateMovementRulesWikiPage() {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("PLAYER MOVEMENT"),
                generateListMenuOptions(
                        new String[] { "BACK", "JUMP & DIVE", "TELEPORTATION", "SAVE & LOAD" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.WIKI),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.JUMP_DROP_MOVEMENT_WIKI),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.TELEPORTATION_MOVEMENT_WIKI),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.SAVE_LOAD_MOVEMENT_WIKI)
                        },
                        LIST_MENU_INCREMENT_Y
                )
        );
    }

    private static JBJGLMenu generateJumpDiveWikiPage() {
        return generatePlayerMovementTypeMenu(
                "JUMPING & DIVING",
                "JUMP: " + ControlScheme.getCorrespondingKey(ControlScheme.Action.JUMP).print() +
                ", DROP/DIVE: " + ControlScheme.getCorrespondingKey(ControlScheme.Action.DROP).print(),
                new String[] {
                        "When supported by a platform, the player can".toUpperCase(),
                        "jump up and drop down. In mid-air, the player".toUpperCase(),
                        "cannot jump, but can dive repeatedly to".toUpperCase(),
                        "increase downward velocity.".toUpperCase()
                }
        );
    }

    private static JBJGLMenu generateTeleportationWikiPage() {
        return generatePlayerMovementTypeMenu(
                "TELEPORTATION",
                ControlScheme.getCorrespondingKey(ControlScheme.Action.INIT_TELEPORT).print(),
                new String[] {
                        "At any point, the player can charge a telepor-".toUpperCase(),
                        "tation jump to the left or right and release".toUpperCase(),
                        "to teleport to their projection. The jump is of".toUpperCase(),
                        "variable length, depending on the charge time.".toUpperCase()
                }
        );
    }

    private static JBJGLMenu generateSaveLoadWikiPage() {
        return generatePlayerMovementTypeMenu(
                "SAVE & LOAD POSITION",
                "SAVE: " + ControlScheme.getCorrespondingKey(ControlScheme.Action.SAVE_POS).print() +
                        ", LOAD: " + ControlScheme.getCorrespondingKey(ControlScheme.Action.LOAD_POS).print(),
                new String[] {
                        "At any point, the player can save position.".toUpperCase(),
                        "Unless the player is falling without platform".toUpperCase(),
                        "cover below, the saved position can be loaded".toUpperCase(),
                        "and the player will teleport there. Saved".toUpperCase(),
                        "positions expire once they are loaded.".toUpperCase()
                }
        );
    }

    private static JBJGLMenu generateAboutMenu() {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("ABOUT THE GAME"),
                generateListMenuOptions(
                        new String[] { "BACK", "BACKGROUND", "THE DEVELOPER", "FEEDBACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.BACKGROUND_ABOUT),
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.DEVELOPER_ABOUT),
                                null // TODO - feedback
                        },
                        LIST_MENU_INCREMENT_Y
                )
        );
    }

    private static JBJGLMenu generateBackgroundAboutPage() {
        final int width = TechnicalSettings.getWidth(), height = TechnicalSettings.getHeight();

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("BACKGROUND"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.ABOUT)
                        }
                ),
                JBJGLTextMenuElement.generate(
                        new int[] { width / 2, coordinateFromFraction(height, 0.675) },
                        JBJGLMenuElement.Anchor.CENTRAL,
                        JBJGLTextBuilder.initialize(
                                TechnicalSettings.getPixelSize() / 4,
                                JBJGLText.Orientation.CENTER,
                                Swatches.BLACK(), Fonts.GAME_STANDARD()
                        ).addText(
                                "THE FIRST VERSION OF "
                        ).setColor(Swatches.TITLE_RED()).setFont(Fonts.GAME_ITALICS()).addText(
                                "TRANSLATION"
                        ).setColor(Swatches.BLACK()).setFont(Fonts.GAME_STANDARD()).addText(
                                " DATES BACK TO 2016. IT WAS AN UGLY,"
                        ).addLineBreak().addText(
                                "CLUNKY SIMULATOR THAT I MADE FOR MY HIGH SCHOOL PHYSICS CLASS TO"
                        ).addLineBreak().addText(
                                "DEMONSTRATE AN UNDERSTANDING OF THE PRINCIPLES OF KINEMATICS. SINCE THEN"
                        ).addLineBreak().addText(
                                "I HAVE MADE THREE MORE VERSIONS OF THE GAME, USUALLY AS TECH DEMOS OR"
                        ).addLineBreak().addText(
                                "WARM UPS: A SHORT BUT FULL DEVELOPMENT CYCLE OF A SIMPLE GAME CONCEPT WITH"
                        ).addLineBreak().addText(
                                "A TIGHT AND ADDICTIVE GAMEPLAY LOOP. THIS IS THE MOST COMPLETE (AND BY ALL"
                        ).addLineBreak().addText(
                                "INDICATIONS FINAL) VERSION OF THE GAME THAT I INTEND TO MAKE. IN THE SPIRIT"
                        ).addLineBreak().addText(
                                "OF ITS ROOTS AS A TOOL AND SIMULATOR RATHER THAN A TRADITIONAL GAME, I HAVE"
                        ).addLineBreak().addText(
                                "DESIGNED IT TO GIVE THE PLAYER AS MUCH CONTROL OVER THE EXPERIENCE AS POSSIBLE."
                        ).addLineBreak().addText(
                                "I HOPE YOU ENJOY PLAYING IT AS MUCH AS I ENJOYED MAKING IT!"
                        ).addLineBreak().setFont(Fonts.GAME_ITALICS()).addText(
                                " - Jordan"
                        ).build()
                )
        );
    }

    private static JBJGLMenu generateDeveloperAboutPage() {
        final int width = TechnicalSettings.getWidth(), height = TechnicalSettings.getHeight();

        final JBJGLImage developer = JBJGLImageIO.readImage(
                Paths.get("resources", "images", "developer.png")
        );

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("THE DEVELOPER"),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.ABOUT)
                        }
                ),
                JBJGLStaticMenuElement.generate(
                        new int[] { width / 2, coordinateFromFraction(height, 0.30) },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP, developer
                ),
                JBJGLTextMenuElement.generate(
                        new int[] { width / 2, coordinateFromFraction(height, 0.75) },
                        JBJGLMenuElement.Anchor.CENTRAL,
                        JBJGLTextBuilder.initialize(
                                TechnicalSettings.getPixelSize() / 4,
                                JBJGLText.Orientation.CENTER,
                                Swatches.BLACK(), Fonts.GAME_STANDARD()
                        ).addText(
                                "HI, I'M JORDAN! I DID ALL THE PROGRAMMING & DESIGN FOR "
                        ).setColor(Swatches.TITLE_RED()).setFont(Fonts.GAME_ITALICS()).addText(
                                "TRANSLATION"
                        ).setColor(Swatches.BLACK()).setFont(Fonts.GAME_STANDARD()).addText(
                                "."
                        ).addLineBreak().addText(
                                "I'VE BEEN MAKING GAMES FOR FUN SINCE I WAS A KID, AND USE GAME DEVELOPMENT"
                        ).addLineBreak().addText(
                                "AS A MEANS TO EXPRESS MY CREATIVITY. BESIDES PROGRAMMING AND MAKING GAMES,"
                        ).addLineBreak().addText(
                                "I DEDICATE MOST OF MY TIME TO WRITING BOOKS ABOUT LANGUAGE, CULTURE, HISTORY,"
                        ).addLineBreak().addText(
                                "IDENTITY, AND WHATEVER ELSE I MAY FIND INTERESTING AT THAT MOMENT IN TIME."
                        ).addLineBreak().addText(
                                "YOU CAN FIND OUT MORE ABOUT MY WORK AT "
                        ).setColor(Swatches.LINK()).addText(
                                "jordanbunke.com"
                        ).setColor(Swatches.BLACK()).addText(".").build()
                )
        );
    }

    private static JBJGLMenu generatePauseMenu(final Level level) {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle(level.getName().toUpperCase()),
                generateTextMenuSubtitle(level.getHint()),
                generateListMenuOptions(
                        new String[] { "RESUME", "SETTINGS", "QUIT TO MENU" },
                        new Runnable[] {
                                () -> Translation.manager
                                        .setActiveStateIndex(Translation.GAMEPLAY_INDEX),
                                () -> Translation.pauseState.getMenuManager()
                                        .setActiveMenuID(MenuIDs.SETTINGS_PAUSE),
                                () -> Translation.pauseState.getMenuManager()
                                        .setActiveMenuID(MenuIDs.ARE_YOU_SURE_PAUSE_QUIT_TO_MENU)
                        },
                        (int)(LIST_MENU_INCREMENT_Y * 1.5)
                )
        );
    }

    private static JBJGLMenu generateLevelCompleteMenu(final Level level) {
        final boolean hasNextLevel = Translation.campaign.hasNextLevel();
        final String[] buttonLabels = hasNextLevel 
                ? new String[] { "STATS", "NEXT LEVEL", "REPLAY", "BACK TO MENU" }
                : new String[] { "STATS", "REPLAY", "BACK TO MENU" };
        final Runnable[] buttonBehaviours = hasNextLevel
                ? new Runnable[] {
                () -> Translation.levelCompleteState.getMenuManager()
                        .setActiveMenuID(MenuIDs.STATS_LEVEL_COMPLETE),
                () -> {
                    Translation.campaign.setToNextLevel();
                    Translation.campaign.getLevel().getStats().reset();
                    Translation.campaign.getLevel().launchLevel();
                    Translation.manager
                            .setActiveStateIndex(Translation.GAMEPLAY_INDEX);
                },
                () -> {
                    level.getStats().reset();
                    level.launchLevel();
                    Translation.manager
                            .setActiveStateIndex(Translation.GAMEPLAY_INDEX);
                },
                () -> {
                    Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU);
                    Translation.manager
                            .setActiveStateIndex(Translation.MENU_INDEX);
                }
        } : new Runnable[] {
                () -> Translation.levelCompleteState.getMenuManager()
                        .setActiveMenuID(MenuIDs.STATS_LEVEL_COMPLETE),
                () -> {
                    level.getStats().reset();
                    level.launchLevel();
                    Translation.manager
                            .setActiveStateIndex(Translation.GAMEPLAY_INDEX);
                },
                () -> {
                    Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU);
                    Translation.manager
                            .setActiveStateIndex(Translation.MENU_INDEX);
                }
        };
        final int yOffset = hasNextLevel
                ? LIST_MENU_INCREMENT_Y
                : (int)(LIST_MENU_INCREMENT_Y * 1.5);

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("LEVEL COMPLETE!"),
                generateTextMenuSubtitle(level.getName().toUpperCase()),
                generateListMenuOptions(
                        buttonLabels, buttonBehaviours, yOffset)
        );
    }

    private static JBJGLMenu generateLevelCompleteStatsPage(final Level level) {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle("PERFORMANCE STATS"),
                generateTextMenuSubtitle(level.getName().toUpperCase()),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.levelCompleteState.getMenuManager()
                                        .setActiveMenuID(MenuIDs.LEVEL_COMPLETE)
                        },
                        LIST_MENU_INCREMENT_Y
                ),
                generateLevelStatsText(level.getStats())
        );
    }

    private static JBJGLMenu generateTitleCard() {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();

        return JBJGLMenu.of(
                // behaviour
                JBJGLTimedMenuElement.generate(TITLE_CARD_TICKS, () -> {
                    Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU);
                    Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                }),

                // visuals in render order
                generateElementBackground(),
                generateElementTitle(width / 2, height / 2, JBJGLMenuElement.Anchor.CENTRAL),
                JBJGLTextMenuElement.generate(
                        new int[] { width / 2, (height * 5) / 8 },
                        JBJGLMenuElement.Anchor.CENTRAL,
                        JBJGLTextBuilder.initialize(
                                        1, JBJGLText.Orientation.CENTER,
                                        Swatches.BLACK(), Fonts.GAME_STANDARD()
                                ).addText("A PURE PLATFORMING PLAYGROUND")
                                .addLineBreak().addText("BY JORDAN BUNKE").build()
                )
        );
    }

    private static JBJGLMenu generateSplashScreen1() {
        return generateSplashScreen(
                i -> 1 + ((i + 7) % 45),
                SPLASH_SCREEN_1_FRAME_COUNT,
                SPLASH_SCREEN_1_TICKS_PER_FRAME,
                5,
                Paths.get(
                        "resources", "images",
                        "splash_screen", "splash_screen_1"
                ),
                "ss1-frame- ("
        );
    }

    private static JBJGLMenu generateSplashScreen2() {
        return generateSplashScreen(
                i -> i + 1,
                SPLASH_SCREEN_2_FRAME_COUNT,
                SPLASH_SCREEN_2_TICKS_PER_FRAME,
                15,
                Paths.get(
                        "resources", "images",
                        "splash_screen", "splash_screen_2"
                ),
                "ss2-frame- ("
        );
    }

    private static JBJGLMenu generateAreYouSureQuitGame() {
        return generateAreYouSureMenu(
                "you want to quit the game?",
                () -> Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU),
                Translation::quitGame
        );
    }

    private static JBJGLMenu generateAreYouSureQuitToMainMenu(
            final Runnable noBehaviour
    ) {
        return generateAreYouSureMenu(
                "you want to quit to the main menu?",
                noBehaviour,
                () -> {
                    Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU);
                    Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                }
        );
    }

    private static JBJGLMenu generatePageDoesNotExistYet() {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();

        final int middle = width / 2;
        final int buttonWidth = TechnicalSettings.pixelLockNumber(width / 3);
        final int promptY = height / 3;
        final int buttonsY = (height * 3) / 5;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                JBJGLTextMenuElement.generate(
                        new int[] { middle, promptY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                        2, JBJGLText.Orientation.CENTER,
                                        Swatches.BLACK(), Fonts.GAME_ITALICS_SPACED()
                                ).addText("UH-OH!").addLineBreak()
                                .addText("THIS PAGE DOES NOT EXIST YET.").build()
                ),
                generateListMenuButton(
                        "MAIN MENU", new int[] { middle, buttonsY },
                        () -> {
                            Translation.menuManager.setActiveMenuID(MenuIDs.MAIN_MENU);
                            Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                        }, buttonWidth)
        );
    }

    private static JBJGLMenu generateAreYouSureMenu(
            final String decisionDescription,
            final Runnable noBehaviour,
            final Runnable yesBehaviour
    ) {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();

        final int buttonWidth = width / 6;
        final int leftX = (width / 2) - (width / 8);
        final int rightX = (width / 2) + (width / 8);
        final int promptY = height / 3;
        final int buttonsY = (height * 3) / 5;

        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                JBJGLTextMenuElement.generate(
                        new int[] { width / 2, promptY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                2, JBJGLText.Orientation.CENTER,
                                Swatches.BLACK(), Fonts.GAME_ITALICS_SPACED()
                        ).addText("ARE YOU SURE").addLineBreak()
                                .addText(decisionDescription.toUpperCase()).build()
                ),
                generateListMenuButton(
                        "NO", new int[] { leftX, buttonsY },
                        noBehaviour, buttonWidth),
                generateListMenuButton(
                        "YES", new int[] { rightX, buttonsY },
                        yesBehaviour, buttonWidth)
        );
    }

    // HELPERS

    private static JBJGLMenuElementGrouping generateElementTitle(
            final int x, final int y, final JBJGLMenuElement.Anchor anchor
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(
                        new int[] { x, y },
                        anchor,
                        JBJGLText.createOf(
                                pixel, JBJGLText.Orientation.CENTER,
                                JBJGLTextComponent.add(
                                        Translation.TITLE.toUpperCase(),
                                        Fonts.VIGILANT_ITALICS(),
                                        Swatches.BLACK()
                                )
                        )
                ),
                JBJGLTextMenuElement.generate(
                        new int[] { x - (pixel * 2), y },
                        anchor,
                        JBJGLText.createOf(
                                pixel, JBJGLText.Orientation.CENTER,
                                JBJGLTextComponent.add(
                                        Translation.TITLE.toUpperCase(),
                                        Fonts.VIGILANT_ITALICS(),
                                        Swatches.TITLE_RED()
                                )
                        )
                )
        );
    }

    private static JBJGLMenuElement generateElementBackground() {
        return JBJGLStaticMenuElement.generate(
                new int[] { 0, 0 },
                JBJGLMenuElement.Anchor.LEFT_TOP,
                ImageAssets.BACKGROUND()
        );
    }

    private static JBJGLMenuElement generateElementBlackBackground() {
        return JBJGLStaticMenuElement.generate(
                new int[] { 0, 0 },
                JBJGLMenuElement.Anchor.LEFT_TOP,
                ImageAssets.BLACK_BACKGROUND()
        );
    }

    private static JBJGLMenu generateSplashScreen(
            final Function<Integer, Integer> indexMapping,
            final int frameCount, final int ticksPerFrame,
            final int paddingFrameCount,
            final Path folder, final String baseFileName
    ) {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();

        final JBJGLImage[] frames = new JBJGLImage[paddingFrameCount + frameCount];

        for (int i = 0; i < frameCount; i++) {
            final int fileIndex = indexMapping.apply(i);
            final String filename = baseFileName + fileIndex + ").png";
            final Path path = folder.resolve(Paths.get(filename));

            frames[paddingFrameCount + i] = JBJGLImageIO.readImage(path);
        }

        final int frameWidth = frames[paddingFrameCount].getWidth(),
                frameHeight = frames[paddingFrameCount].getHeight();
        final int x = (width - frameWidth) / 2, y = (height - frameHeight) / 2;

        for (int i = 0; i < paddingFrameCount; i++) {
            frames[i] = JBJGLImage.create(frameWidth, frameHeight);
        }

        return JBJGLMenu.of(
                // behaviour
                JBJGLTimedMenuElement.generate(
                        (frameCount + paddingFrameCount) * ticksPerFrame,
                        () -> Translation.manager.setActiveStateIndex(Translation.MENU_INDEX)
                ),

                // visuals in render order
                generateElementBlackBackground(),
                JBJGLAnimationMenuElement.generate(
                        new int[] { x, y },
                        new int[] { frameWidth, frameHeight },
                        JBJGLMenuElement.Anchor.LEFT_TOP,
                        ticksPerFrame,
                        frames
                )
        );
    }

    private static JBJGLMenu generatePlayerMovementTypeMenu(
            final String heading, final String subtitle, final String[] lines
    ) {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                generateTextMenuTitle(heading),
                generateTextMenuSubtitle(subtitle.toUpperCase()),
                generateListMenuOptions(
                        new String[] { "BACK" },
                        new Runnable[] {
                                () -> Translation.menuManager.setActiveMenuID(MenuIDs.MOVEMENT_RULES_WIKI)
                        },
                        LIST_MENU_INCREMENT_Y
                ),
                generateMenuTextBlurb(
                        coordinateFromFraction(TechnicalSettings.getHeight(), 0.5),
                        lines, 2
                )
        );
    }

    private static Runnable runRegularOrPauseMenuManagerUpdate(
            final boolean isMainMenu, final String menuID
    ) {
        return isMainMenu
                ? () -> Translation.menuManager.setActiveMenuID(menuID)
                : () -> Translation.pauseState.getMenuManager().setActiveMenuID(menuID);
    }

    private static JBJGLTextMenuElement generateMenuTextBlurb(
            final int y, final String[] lines, final int textSize
    ) {
        final int width = TechnicalSettings.getWidth();

        final JBJGLTextBuilder tb = generateInitialMenuTextBuilder(textSize);

        for (int i = 0; i < lines.length; i++) {
            tb.addText(lines[i]);

            if (i + 1 < lines.length)
                tb.addLineBreak();
        }

        return JBJGLTextMenuElement.generate(
                new int[] { width / 2, y }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                tb.build()
        );
    }

    private static JBJGLMenuElementGrouping generateCampaignsOnPage(
            final String title, final Campaign[] campaigns,
            final String backMenuID, final int page
    ) {
        final int width = TechnicalSettings.getWidth();
        final int INITIAL_Y = LIST_MENU_INITIAL_Y + (int)(2.5 * LIST_MENU_INCREMENT_Y);
        final int BUTTON_WIDTH = coordinateFromFraction(width, 1/5.3);
        final int CAMPAIGNS_ON_PAGE = 3, campaignCount = campaigns.length;
        final int campaignsOnThisPage = Math.min(
                CAMPAIGNS_ON_PAGE, campaignCount - (CAMPAIGNS_ON_PAGE * page)
        );
        final int startingIndex = page * CAMPAIGNS_ON_PAGE;

        final boolean hasPreviousPage = page > 0;
        final boolean hasNextPage = campaignCount > ((page + 1) * CAMPAIGNS_ON_PAGE);
        final boolean hasPrevAndNext = hasPreviousPage && hasNextPage;

        final int menuElementsCount = campaignsOnThisPage +
                (hasPrevAndNext
                        ? 2
                        : ((hasPreviousPage || hasNextPage)
                        ? 1
                        : 0
                ));

        final JBJGLMenuElement[] campaignButtons = new JBJGLMenuElement[menuElementsCount];

        for (int i = 0; i < campaignsOnThisPage; i++) {
            final int x = coordinateFromFraction(width, 0.5),
                    y = INITIAL_Y + (i * LIST_MENU_INCREMENT_Y);
            final Campaign campaign = campaigns[startingIndex + i];
            campaignButtons[i] = generateCampaignButton(
                    x, y, coordinateFromFraction(width, 0.8), campaign);
        }

        populatePreviousAndNext(
                campaignButtons,
                hasPreviousPage, hasNextPage,
                BUTTON_WIDTH,
                () -> Translation.menuManager.addMenu(
                        MenuIDs.CAMPAIGN_FOLDER,
                        generateCampaignFolderMenu(
                                title, campaigns, backMenuID, page - 1
                        ), true),
                () -> Translation.menuManager.addMenu(
                        MenuIDs.CAMPAIGN_FOLDER,
                        generateCampaignFolderMenu(
                                title, campaigns, backMenuID, page + 1
                        ), true)
        );

        return JBJGLMenuElementGrouping.generate(campaignButtons);
    }

    private static JBJGLMenuElement generateCampaignButton(
            final int x, final int y, final int buttonWidth,
            final Campaign campaign
    ) {
        final Runnable behaviour = () -> {
            Translation.campaign = campaign;
            Translation.menuManager.addMenu(
                    MenuIDs.CAMPAIGN_LEVELS,
                    generateMenuForCampaign(
                            campaign, MenuIDs.CAMPAIGN_FOLDER), true
            );
        };

        return generateListMenuButton(
                Utility.cutOffIfLongerThan(campaign.getName().toUpperCase(), 40),
                new int[] { x, y }, behaviour, buttonWidth
        );
    }

    private static JBJGLMenuElementGrouping generateLevelsOnPage(
            final Campaign campaign, final String backMenuID, final int page
    ) {
        final int width = TechnicalSettings.getWidth();
        final int COLUMNS = 4, INITIAL_Y = coordinateFromFraction(
                TechnicalSettings.getHeight(), 0.45);
        final int BUTTON_WIDTH = coordinateFromFraction(width, 1/(COLUMNS + 1.3));
        final int LEVELS_ON_PAGE = 16, levelCount = campaign.getLevelCount();
        final int levelsOnThisPage = Math.min(
                LEVELS_ON_PAGE, levelCount - (LEVELS_ON_PAGE * page)
        );
        final int startingIndex = page * LEVELS_ON_PAGE;

        final boolean hasPreviousPage = page > 0;
        final boolean hasNextPage = levelCount > ((page + 1) * LEVELS_ON_PAGE);
        final boolean hasPrevAndNext = hasPreviousPage && hasNextPage;

        final int menuElementsCount = levelsOnThisPage +
                (hasPrevAndNext
                        ? 2
                        : ((hasPreviousPage || hasNextPage)
                            ? 1
                            : 0
                ));

        final JBJGLMenuElement[] levelButtons = new JBJGLMenuElement[menuElementsCount];

        for (int i = 0; i < levelsOnThisPage; i++) {
            final int column = i % COLUMNS;
            final int row = i / COLUMNS;
            final int x = coordinateFromFraction(
                    width, (column + 1) / (double)(COLUMNS + 1)
            ), y = INITIAL_Y + (row * LIST_MENU_INCREMENT_Y);
            final Level level = campaign.getLevelAt(startingIndex + i);
            levelButtons[i] = generateLevelButton(
                    x, y, BUTTON_WIDTH, campaign, level, startingIndex + i);
        }

        populatePreviousAndNext(
                levelButtons, hasPreviousPage, hasNextPage,
                BUTTON_WIDTH,
                () -> Translation.menuManager.addMenu(
                        MenuIDs.CAMPAIGN_LEVELS,
                        generateMenuForCampaign(
                                Translation.campaign, backMenuID, page - 1
                        ), true),
                () -> Translation.menuManager.addMenu(
                        MenuIDs.CAMPAIGN_LEVELS,
                        generateMenuForCampaign(
                                Translation.campaign, backMenuID, page + 1
                        ), true)
        );

        return JBJGLMenuElementGrouping.generate(levelButtons);
    }

    private static JBJGLMenuElement generateLevelButton(
            final int x, final int y, final int buttonWidth,
            final Campaign campaign, final Level level, final int index
    ) {
        final boolean isUnlocked = campaign.isUnlocked(index);
        final Runnable behaviour = isUnlocked
                ? () -> Translation.menuManager.addMenu(
                MenuIDs.LEVEL_OVERVIEW, generateLevelOverview(level, index), true
        ) : null;

        return generateListMenuButton(
                Utility.cutOffIfLongerThan(level.getName().toUpperCase(), 10),
                new int[] { x, y }, behaviour, buttonWidth
        );
    }

    private static void populatePreviousAndNext(
            final JBJGLMenuElement[] elements,
            final boolean hasPreviousPage, final boolean hasNextPage,
            final int BUTTON_WIDTH,
            final Runnable previous, final Runnable next
    ) {
        final int width = TechnicalSettings.getWidth();
        final int PREVIOUS_X = coordinateFromFraction(width, 0.2);
        final int NEXT_X = coordinateFromFraction(width, 0.8);
        final int NAVIGATION_Y = LIST_MENU_INITIAL_Y + LIST_MENU_INCREMENT_Y;

        final JBJGLMenuElement previousPageButton = generateListMenuButton(
                "< PREVIOUS", new int[] { PREVIOUS_X, NAVIGATION_Y },
                previous, BUTTON_WIDTH
        );
        final JBJGLMenuElement nextPageButton = generateListMenuButton(
                "NEXT >", new int[] { NEXT_X, NAVIGATION_Y },
                next, BUTTON_WIDTH
        );

        if (hasPreviousPage && hasNextPage) {
            elements[elements.length - 2] = previousPageButton;
            elements[elements.length - 1] = nextPageButton;
        } else if (hasPreviousPage)
            elements[elements.length - 1] = previousPageButton;
        else if (hasNextPage)
            elements[elements.length - 1] = nextPageButton;
    }

    private static JBJGLMenuElementGrouping generateSentryButtons() {
        final int width = TechnicalSettings.getWidth();
        final int COLUMNS = 4, INITIAL_Y = coordinateFromFraction(
                TechnicalSettings.getHeight(), 0.45);
        final Sentry.Role[] roles = Sentry.Role.values();
        final JBJGLClickableMenuElement[] menuElements = new JBJGLClickableMenuElement[roles.length];

        for (int i = 0; i < roles.length; i++) {
            final int column = i % COLUMNS;
            final int row = i / COLUMNS;
            final int x = coordinateFromFraction(
                    width, (column + 1) / (double)(COLUMNS + 1)
            ), y = INITIAL_Y + (row * MENU_TEXT_INCREMENT_Y);
            menuElements[i] = generateSentryButton(x, y, roles[i],
                    coordinateFromFraction(width, 1/(COLUMNS + 1.3)));
        }

        return JBJGLMenuElementGrouping.generate(
                menuElements
        );
    }

    private static JBJGLClickableMenuElement generateSentryButton(
            final int x, final int y, final Sentry.Role role, final int buttonWidth
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        final JBJGLImage nonHighlightedButton =
                drawNonHighlightedButton(buttonWidth, role.name(), Swatches.BLACK(), 1);
        final int width = nonHighlightedButton.getWidth(), height = nonHighlightedButton.getHeight();
        final JBJGLImage square = ImageAssets.drawSentry(role);
        final int squareX = pixel * 3, squareY = (height - square.getHeight()) / 2;

        final Graphics nhbg = nonHighlightedButton.getGraphics();
        nhbg.drawImage(square, squareX, squareY, null);

        final JBJGLImage highlightedButton = drawHighlightedButton(nonHighlightedButton);

        return JBJGLClickableMenuElement.generate(
                new int[] { x, y }, new int[] { width, height }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                nonHighlightedButton, highlightedButton,
                () -> Translation.menuManager.addMenu(MenuIDs.SENTRY_ROLE_WIKI,
                        generateSentryRoleWikiPage(role), true)
        );
    }

    private static JBJGLMenuElementGrouping generateControlsButtons(
            final int offsetY
    ) {
        final int width = TechnicalSettings.getWidth();
        final int COLUMNS = 4, CONTROL_AMOUNT = 14, textSize = TechnicalSettings.getPixelSize() / 4;
        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[CONTROL_AMOUNT * 2];

        final String[] associatedTexts = new String[] {
                "MOVE LEFT", "MOVE RIGHT", "JUMP", "DROP",
                "TELEPORT", "SAVE", "LOAD",
                "CAMERA LEFT", "CAMERA RIGHT", "CAMERA UP", "CAMERA DOWN",
                "TOGGLE ZOOM", "TOGGLE FOLLOW MODE", "PAUSE"
        };
        final List<Consumer<JBJGLKey>> setFunctions = List.of(
                key -> {
                    ControlScheme.update(ControlScheme.Action.MOVE_LEFT, key);
                    ControlScheme.update(ControlScheme.Action.STOP_MOVING_CAM_LEFT, key);
                },
                key -> {
                    ControlScheme.update(ControlScheme.Action.MOVE_RIGHT, key);
                    ControlScheme.update(ControlScheme.Action.STOP_MOVING_RIGHT, key);
                },
                key -> ControlScheme.update(ControlScheme.Action.JUMP, key),
                key -> ControlScheme.update(ControlScheme.Action.DROP, key),
                key -> {
                    ControlScheme.update(ControlScheme.Action.INIT_TELEPORT, key);
                    ControlScheme.update(ControlScheme.Action.TELEPORT, key);
                },
                key -> ControlScheme.update(ControlScheme.Action.SAVE_POS, key),
                key -> ControlScheme.update(ControlScheme.Action.LOAD_POS, key),
                key -> {
                    ControlScheme.update(ControlScheme.Action.STOP_MOVING_CAM_LEFT, key);
                    ControlScheme.update(ControlScheme.Action.MOVE_CAM_LEFT, key);
                },
                key -> {
                    ControlScheme.update(ControlScheme.Action.STOP_MOVING_CAM_RIGHT, key);
                    ControlScheme.update(ControlScheme.Action.MOVE_CAM_RIGHT, key);
                },
                key -> {
                    ControlScheme.update(ControlScheme.Action.STOP_MOVING_CAM_UP, key);
                    ControlScheme.update(ControlScheme.Action.MOVE_CAM_UP, key);
                },
                key -> {
                    ControlScheme.update(ControlScheme.Action.STOP_MOVING_CAM_DOWN, key);
                    ControlScheme.update(ControlScheme.Action.MOVE_CAM_DOWN, key);
                },
                key -> ControlScheme.update(ControlScheme.Action.TOGGLE_ZOOM, key),
                key -> ControlScheme.update(ControlScheme.Action.TOGGLE_FOLLOW_MODE, key),
                key -> ControlScheme.update(ControlScheme.Action.PAUSE, key)
        );
        final ControlScheme.Action[] actions = new ControlScheme.Action[] {
                ControlScheme.Action.MOVE_LEFT, ControlScheme.Action.MOVE_RIGHT,
                ControlScheme.Action.JUMP, ControlScheme.Action.DROP,
                ControlScheme.Action.TELEPORT,
                ControlScheme.Action.SAVE_POS, ControlScheme.Action.LOAD_POS,
                ControlScheme.Action.MOVE_CAM_LEFT,
                ControlScheme.Action.MOVE_CAM_RIGHT,
                ControlScheme.Action.MOVE_CAM_UP,
                ControlScheme.Action.MOVE_CAM_DOWN,
                ControlScheme.Action.TOGGLE_ZOOM,
                ControlScheme.Action.TOGGLE_FOLLOW_MODE,
                ControlScheme.Action.PAUSE
        };

        final int buttonWidth = coordinateFromFraction(
                width, 1 / (double)(3 * (COLUMNS + 1))
        );
        final JBJGLImage setButton = drawNonHighlightedButton(
                buttonWidth, "SET", Swatches.BLACK(), TechnicalSettings.getPixelSize() / 4
        );
        final JBJGLImage setHighlightedButton = drawHighlightedButton(setButton);

        for (int i = 0; i < CONTROL_AMOUNT; i++) {
            final int column = switch (i) {
                case 0, 1, 2, 3 -> 0;
                case 4, 5, 6 -> 1;
                case 7, 8, 9, 10 -> 2;
                default -> 3;
            };
            final int row = switch (i) {
                case 0, 4, 7, 11 -> 0;
                case 1, 5, 8, 12 -> 1;
                case 2, 6, 9, 13 -> 2;
                default -> 3;
            };
            final int x = coordinateFromFraction(
                    width, (column + 1) / (double)(COLUMNS + 1)
            );
            final int y = LIST_MENU_INITIAL_Y + offsetY + (row * LIST_MENU_INCREMENT_Y);

            menuElements[i * 2] = JBJGLTextMenuElement.generate(
                    new int[] { x, y + (textSize * 2) },
                    JBJGLMenuElement.Anchor.CENTRAL_TOP,
                    generateInitialMenuTextBuilder(textSize).addText(associatedTexts[i]).build()
            );
            menuElements[(i * 2) + 1] = generateControlButton(
                    new int[] { x, y + (int)(LIST_MENU_INCREMENT_Y * 0.3) },
                    buttonWidth, setFunctions.get(i), actions[i],
                    setButton, setHighlightedButton
            );
        }

        return JBJGLMenuElementGrouping.generate(
                menuElements
        );
    }

    private static SetInputMenuElement generateControlButton(
            final int[] position, final int width, final Consumer<JBJGLKey> setFunction,
            final ControlScheme.Action action,
            final JBJGLImage nonHighlightedSetImage, final JBJGLImage highlightedSetImage
    ) {
        final Callable<JBJGLImage> nhGeneratorFunction = () -> drawNonHighlightedButton(
                width,
                Utility.cutOffIfLongerThan(ControlScheme.getCorrespondingKey(action).print(), 8),
                Swatches.BLACK(),
                TechnicalSettings.getPixelSize() / 4);
        final Callable<JBJGLImage> hGeneratorFunction = () -> drawHighlightedButton(
                drawNonHighlightedButton(
                        width,
                        Utility.cutOffIfLongerThan(ControlScheme.getCorrespondingKey(action).print(), 8),
                        Swatches.BLACK(),
                        TechnicalSettings.getPixelSize() / 4)
        );

        return SetInputMenuElement.generate(position,
                new int[] { width, nonHighlightedSetImage.getHeight() },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                setFunction, nhGeneratorFunction, hGeneratorFunction,
                nonHighlightedSetImage, highlightedSetImage
        );
    }

    private static JBJGLMenuElementGrouping generateListMenuOptions(
            final String[] headings, final Runnable[] behaviours
    ) {
        return generateListMenuOptions(headings, behaviours, 0);
    }

    private static JBJGLMenuElementGrouping generateListMenuOptions(
            final String[] headings, final Runnable[] behaviours, final int offsetY
    ) {
        final int width = TechnicalSettings.getWidth();

        final int amount = headings.length;
        int drawY = LIST_MENU_INITIAL_Y + offsetY;

        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[amount];

        for (int i = 0; i < amount; i++) {
            final JBJGLMenuElement button = generateListMenuButton(
                    headings[i], new int[] { width / 2, drawY }, behaviours[i],
                    TechnicalSettings.pixelLockNumber(width / 3)
            );
            menuElements[i] = button;
            drawY += LIST_MENU_INCREMENT_Y;
        }

        return JBJGLMenuElementGrouping.generate(menuElements);
    }

    private static JBJGLMenuElementGrouping generateListMenuToggleOptions(
            final String[] associatedTexts, final String[][] buttonHeadings,
            final Runnable[][] behaviours, final Callable<Integer>[] updateIndexLogic,
            final int offsetY
    ) {
        final int width = TechnicalSettings.getWidth();
        final int pixel = TechnicalSettings.getPixelSize();
        final int amount = associatedTexts.length;
        
        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[amount * 2];
        final int associatedX = coordinateFromFraction(width, 0.3);
        final int buttonX = coordinateFromFraction(width, 0.7);
        int drawY = LIST_MENU_INITIAL_Y + offsetY;
        final int textSize = pixel / 2;

        for (int i = 0; i < amount; i++) {
            final JBJGLTextMenuElement associatedText = JBJGLTextMenuElement.generate(
                    new int[] { associatedX, drawY + (textSize * 2) },
                    JBJGLMenuElement.Anchor.CENTRAL_TOP,
                    generateInitialMenuTextBuilder(textSize).addText(associatedTexts[i]).build()
            );
            final JBJGLToggleClickableMenuElement button = generateListMenuToggleButton(
                    buttonHeadings[i], new int[] { buttonX, drawY }, behaviours[i],
                    updateIndexLogic[i], TechnicalSettings.pixelLockNumber(width / 4)
            );

            menuElements[i * 2] = associatedText;
            menuElements[(i * 2) + 1] = button;
            drawY += LIST_MENU_INCREMENT_Y;
        }

        return JBJGLMenuElementGrouping.generate(menuElements);
    }

    private static JBJGLToggleClickableMenuElement generateListMenuToggleButton(
            final String[] headings, final int[] position,
            final Runnable[] behaviours, final Callable<Integer> updateIndexLogic,
            final int width
    ) {
        final int amount = headings.length;
        final Color color = Swatches.BLACK();

        final JBJGLImage[] nonHighlightedButtons = new JBJGLImage[amount];
        final JBJGLImage[] highlightedButtons = new JBJGLImage[amount];

        for (int i = 0; i < amount; i++) {
            nonHighlightedButtons[i] = drawNonHighlightedButton(width, headings[i], color);
            highlightedButtons[i] = drawHighlightedButton(nonHighlightedButtons[i]);
        }

        return JBJGLToggleClickableMenuElement.generate(
                position, new int[] { width, nonHighlightedButtons[0].getHeight() },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                nonHighlightedButtons, highlightedButtons,
                behaviours, updateIndexLogic
        );
    }

    private static JBJGLMenuElement generateListMenuButton(
            final String heading, final int[] position,
            final Runnable behaviour, final int width
    ) {
        final boolean hasBehaviour = behaviour != null;
        final Color color = hasBehaviour ? Swatches.BLACK() : Swatches.WHITE();

        JBJGLImage nonHighlightedButton = drawNonHighlightedButton(width, heading, color);
        JBJGLImage highlightedButton = drawHighlightedButton(nonHighlightedButton);

        return hasBehaviour
                ? JBJGLClickableMenuElement.generate(
                position, new int[] { width, nonHighlightedButton.getHeight() },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                nonHighlightedButton, highlightedButton, behaviour
        )
                : JBJGLStaticMenuElement.generate(
                position, JBJGLMenuElement.Anchor.CENTRAL_TOP, nonHighlightedButton
        );
    }

    private static JBJGLMenuElementGrouping generateLevelPBText(final LevelStats levelStats) {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();
        final int fieldsX = coordinateFromFraction(width, 0.3);
        final int pbX = coordinateFromFraction(width, 0.6);
        final int y = coordinateFromFraction(height, 0.5);

        return JBJGLMenuElementGrouping.generateOf(
                generateLevelStatFields(fieldsX, y, false, 1),
                generatePBs(pbX, y, false, 1, levelStats)
        );
    }

    private static JBJGLMenuElementGrouping generateLevelStatsText(final LevelStats levelStats) {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();
        final int fieldsX = coordinateFromFraction(width, 0.1);
        final int thisRunX = coordinateFromFraction(width, 0.5);
        final int pbX = coordinateFromFraction(width, 0.8);
        final int y = coordinateFromFraction(height, 0.5);

        return JBJGLMenuElementGrouping.generateOf(
                generateLevelStatFields(fieldsX, y, true, 2),
                generateThisRunStats(thisRunX, y, true, 2, levelStats),
                generatePBs(pbX, y, true, 2, levelStats)
        );
    }

    private static JBJGLMenuElementGrouping generateLevelStatFields(
            final int x, final int y, final boolean withHeading, final int textSize
    ) {
        JBJGLText heading = generateInitialMenuTextBuilder(textSize)
                .addText(" ").build();
        JBJGLText[] body = new JBJGLText[] {
                generateInitialMenuTextBuilder(textSize)
                        .addText("TIME:").build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText("FAILURES:").build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText("SIGHTINGS:").build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText("MAX COMBO:").build()
        };

        JBJGLText[] lines = withHeading ? prependElementToArray(heading, body) : body;
        return generateMenuTextLines(x, y, JBJGLMenuElement.Anchor.LEFT_TOP, lines);
    }

    private static JBJGLMenuElementGrouping generateThisRunStats(
            final int x, final int y, final boolean withHeading,
            final int textSize, final LevelStats levelStats
    ) {
        JBJGLText heading = generateInitialMenuTextBuilder(textSize)
                .addText("THIS RUN").build();
        JBJGLText[] body = new JBJGLText[] {
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.TIME)
                                        ? Swatches.WORSE_THAN_PB(Swatches.OPAQUE())
                                        : Swatches.NEW_PB(Swatches.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.TIME)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.FAILURES)
                                        ? Swatches.WORSE_THAN_PB(Swatches.OPAQUE())
                                        : Swatches.NEW_PB(Swatches.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.FAILURES)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.SIGHTINGS)
                                        ? Swatches.WORSE_THAN_PB(Swatches.OPAQUE())
                                        : Swatches.NEW_PB(Swatches.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.SIGHTINGS)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.MAX_COMBO)
                                        ? Swatches.WORSE_THAN_PB(Swatches.OPAQUE())
                                        : Swatches.NEW_PB(Swatches.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.MAX_COMBO)).build()
        };

        JBJGLText[] lines = withHeading ? prependElementToArray(heading, body) : body;
        return generateMenuTextLines(x, y, JBJGLMenuElement.Anchor.CENTRAL_TOP, lines);
    }

    private static JBJGLMenuElementGrouping generatePBs(
            final int x, final int y, final boolean withHeading,
            final int textSize, final LevelStats levelStats
    ) {
        JBJGLText heading = generateInitialMenuTextBuilder(textSize)
                .addText("PERSONAL BESTS").build();
        JBJGLText[] body = new JBJGLText[] {
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPersonalBest(LevelStats.TIME, true)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPersonalBest(LevelStats.FAILURES, true)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPersonalBest(LevelStats.SIGHTINGS, true)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPersonalBest(LevelStats.MAX_COMBO, true)).build()
        };

        JBJGLText[] lines = withHeading ? prependElementToArray(heading, body) : body;
        return generateMenuTextLines(x, y, JBJGLMenuElement.Anchor.CENTRAL_TOP, lines);
    }

    private static JBJGLText[] prependElementToArray(
            final JBJGLText element, final JBJGLText[] array
    ) {
        JBJGLText[] replacement = new JBJGLText[array.length + 1];

        replacement[0] = element;
        System.arraycopy(array, 0, replacement, 1, replacement.length - 1);

        return replacement;
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder(final int textSize) {
        return JBJGLTextBuilder.initialize(
                textSize, JBJGLText.Orientation.LEFT,
                Swatches.BLACK(), Fonts.GAME_STANDARD()
        );
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder() {
        return generateInitialMenuTextBuilder(1);
    }

    private static JBJGLTextMenuElement generateTextMenuTitle(final String title) {
        final int width = TechnicalSettings.getWidth();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLTextMenuElement.generate(
                new int[] { width / 2, MENU_TITLE_Y },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                JBJGLTextBuilder.initialize(
                        pixel, JBJGLText.Orientation.CENTER,
                        Swatches.BLACK(), Fonts.VIGILANT_ITALICS_SPACED()
                ).addText(title).build()
        );
    }

    private static JBJGLTextMenuElement generateTextMenuSubtitle(final String subtitle) {
        final int width = TechnicalSettings.getWidth();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLTextMenuElement.generate(
                new int[] { width / 2, MENU_SUBTITLE_Y },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                JBJGLTextBuilder.initialize(
                        pixel / 2, JBJGLText.Orientation.CENTER,
                        Swatches.BLACK(), Fonts.GAME_ITALICS_SPACED()
                ).addText(subtitle).build()
        );
    }

    private static JBJGLMenuElementGrouping generateMenuTextLines(
            final int x, final int initialY, final JBJGLMenuElement.Anchor anchor,
            final JBJGLText[] texts
    ) {
        final JBJGLTextMenuElement[] textMenuElements = new JBJGLTextMenuElement[texts.length];
        int y = initialY;

        for (int i = 0; i < textMenuElements.length; i++) {
            textMenuElements[i] = JBJGLTextMenuElement.generate(
                    new int[] { x, y }, anchor, texts[i]
            );
            y += MENU_TEXT_INCREMENT_Y;
        }

        return JBJGLMenuElementGrouping.generate(textMenuElements);
    }

    private static JBJGLImage drawNonHighlightedButton(
            final int width, final String label, final Color color, final int textSize
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        JBJGLImage text = JBJGLTextBuilder.initialize(
                textSize, JBJGLText.Orientation.CENTER,
                color, Fonts.GAME_STANDARD()
        ).addText(label).build().draw();

        final int height = text.getHeight();
        final int trueWidth = Math.max(
                width,
                text.getWidth() + Math.max(pixel * 4, textSize * pixel * 2)
        );
        final int x = (trueWidth - text.getWidth()) / 2;

        JBJGLImage nonHighlightedButton =
                JBJGLImage.create(trueWidth, height);
        drawButtonPixelBorder(nonHighlightedButton, color);
        Graphics nhbg = nonHighlightedButton.getGraphics();
        nhbg.drawImage(text, x, textSize * 2, null);

        return nonHighlightedButton;
    }

    private static JBJGLImage drawNonHighlightedButton(
            final int width, final String label, final Color color
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        return drawNonHighlightedButton(width, label, color, pixel / 2);
    }

    private static JBJGLImage drawHighlightedButton(final JBJGLImage nonHighlightedButton) {
        final int width = nonHighlightedButton.getWidth(),
                height = nonHighlightedButton.getHeight();

        JBJGLImage highlightedButton = JBJGLImage.create(width, height);
        Graphics hbg = highlightedButton.getGraphics();
        hbg.setColor(Swatches.PLAYER(Swatches.OPAQUE()));
        hbg.fillRect(0, 0, width, height);
        hbg.drawImage(nonHighlightedButton, 0, 0, null);

        return highlightedButton;
    }

    private static void drawButtonPixelBorder(final JBJGLImage image, final Color c) {
        Graphics g = image.getGraphics();
        final int pixel = TechnicalSettings.getPixelSize(),
                width = image.getWidth(), height = image.getHeight();

        g.setColor(c);
        g.fillRect(0, 0, width, pixel);
        g.fillRect(0, 0, pixel, height);
        g.fillRect(0, height - pixel, width, pixel);
        g.fillRect(width - pixel, 0, pixel, height);
    }

    private static int coordinateFromFraction(final int dimension, final double fraction) {
        if (fraction < 0.)
            return 0;
        else if (fraction > 1.)
            return dimension;
        else
            return (int)(dimension * fraction);
    }

    private static int gameTicksToNearestSecond(final int ticks) {
        return (int)Math.round(ticks / GameplayConstants.UPDATE_HZ);
    }
}

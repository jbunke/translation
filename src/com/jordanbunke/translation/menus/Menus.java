package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.jbjgl.io.JBJGLImageIO;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.*;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.editor.Editor;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.io.ParserWriter;
import com.jordanbunke.translation.io.TextIO;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugSettings;
import com.jordanbunke.translation.utility.Utility;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public class Menus {

    // Timings
    private static final int FF_SPLASH_SCREEN_FRAME_COUNT = 6;
    private static final int SPLASH_SCREEN_1_FRAME_COUNT = 45;
    private static final int SPLASH_SCREEN_2_FRAME_COUNT = 146;
    private static final int FF_SPLASH_SCREEN_TICKS_PER_FRAME = 5;
    private static final int SPLASH_SCREEN_1_TICKS_PER_FRAME = 10;
    private static final int SPLASH_SCREEN_2_TICKS_PER_FRAME = 5;
    private static final int TITLE_CARD_TICKS = 200;

    // MENU MANAGER GENERATORS

    public static JBJGLMenuManager generatePauseMenuManager(final Level level) {
        return JBJGLMenuManager.initialize(
                generatePauseMenu(level), MenuIDs.PAUSE_MENU);
    }

    public static JBJGLMenuManager generateLevelCompleteMenuManager(final Level level) {
        return JBJGLMenuManager.initialize(
                generateLevelCompleteMenu(level), MenuIDs.LEVEL_COMPLETE);
    }

    public static JBJGLMenuManager generateMenuManager() {
        return JBJGLMenuManager.initialize(generateTitleCard(), MenuIDs.TITLE_CARD);
    }

    public static void generateAfterResize(
            final JBJGLMenuManager manager, final boolean isMainMenu, final Level level) {
        if (isMainMenu)
            manager.addMenu(MenuIDs.MAIN_MENU, generateMainMenu(), false);
        else
            manager.addMenu(MenuIDs.PAUSE_MENU, generatePauseMenu(level), false);

        manager.addMenu(MenuIDs.SETTINGS, generateSettingsMenu(isMainMenu), false);
        manager.addMenu(MenuIDs.VIDEO_SETTINGS, generateVideoSettingsMenu(), true);
    }

    public static JBJGLMenuManager generateSplashScreenManager() {
        return JBJGLMenuManager.initialize(
                Utility.coinToss(0.98,
                        generateFlinkerFlitzerSplashScreen(),
                        Utility.coinToss(
                                generateSplashScreen1(),
                                generateSplashScreen2())),
                MenuIDs.SPLASH_SCREEN);
    }

    // MENU GENERATORS

    private static JBJGLMenu generateMainMenu() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateGameTitleLogo(),
                MenuHelper.generateListMenuOptions(
                        new String[] {
                                "PLAY", "LEVEL EDITOR", "SETTINGS",
                                "WIKI", "INFORMATION", "QUIT"
                        },
                        new Runnable[] {
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGNS_MENU,
                                        generateCampaignsMenu()),
                                () -> Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX), // TODO - editor
                                () -> MenuHelper.linkMenu(MenuIDs.SETTINGS,
                                        generateSettingsMenu(true)),
                                () -> MenuHelper.linkMenu(MenuIDs.WIKI, generateWikiMenu()),
                                () -> MenuHelper.linkMenu(MenuIDs.ABOUT, generateAboutMenu()),
                                () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_QUIT_GAME,
                                        generateAreYouSureQuitGame())
                        }),
                MenuHelper.generateDevelopmentInformation()
        );

        return MenuHelper.generatePlainMenu(contents);
    }

    private static JBJGLMenu generateCampaignsMenu() {
        return MenuHelper.generateBasicMenu(
                "Campaigns", MenuHelper.DOES_NOT_EXIST,
                MenuHelper.generateListMenuOptions(
                        new String[] { "MAIN CAMPAIGNS", "TUTORIAL",
                                "MY CAMPAIGNS", "IMPORTED CAMPAIGNS" },
                        new Runnable[] {
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_FOLDER,
                                        MenuHelper.generateCampaignFolderMenu(
                                                "MAIN CAMPAIGNS",
                                                LevelIO.readCampaignsInFolder(LevelIO.MAIN_CAMPAIGNS_FOLDER),
                                                MenuIDs.CAMPAIGNS_MENU, 0)),
                                () -> {
                                    Translation.campaign =
                                            LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER);
                                    MenuHelper.linkMenu(
                                            MenuIDs.CAMPAIGN_LEVELS,
                                            MenuHelper.generateMenuForCampaign(
                                                    Translation.campaign, MenuIDs.CAMPAIGNS_MENU
                                            ));
                                },
                                // TODO - implement & link my campaigns, imported campaigns
                                null,
                                null
                        }), MenuIDs.MAIN_MENU);
    }

    private static JBJGLMenu generateSettingsMenu(final boolean isMainMenu) {
        final String backMenuID = isMainMenu
                ? MenuIDs.MAIN_MENU
                : MenuIDs.PAUSE_MENU;

        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "GAMEPLAY", "CONTROLS", "VIDEO", "AUDIO", "TECHNICAL" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.GAMEPLAY_SETTINGS, generateGameplaySettingsMenu()),
                        () -> MenuHelper.linkMenu(MenuIDs.CONTROLS_SETTINGS, generateControlsSettingsMenu()),
                        () -> MenuHelper.linkMenu(MenuIDs.VIDEO_SETTINGS, generateVideoSettingsMenu()),
                        null,
                        () -> MenuHelper.linkMenu(MenuIDs.TECHNICAL_SETTINGS, generateTechnicalSettingsMenu())
                });

        return MenuHelper.generateBasicMenu(
                "Settings", MenuHelper.DOES_NOT_EXIST,
                contents, backMenuID);
    }

    private static JBJGLMenu generateGameplaySettingsMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuToggleOptions(
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
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Gameplay Settings", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateControlsSettingsMenu() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateListMenuOptions(
                        new String[] { "RESET" },
                        new Runnable[] { ControlScheme::reset }),
                MenuHelper.generateControlsButtons(),
                MenuHelper.generateMenuTextBlurb(
                        "* - level editor",
                        JBJGLText.Orientation.CENTER,
                        JBJGLMenuElement.Anchor.CENTRAL,
                        MenuHelper.widthCoord(0.5),
                        MenuHelper.heightCoord(0.9), 1));

        return MenuHelper.generateBasicMenu(
                "Controls", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateVideoSettingsMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuToggleOptions(
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
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Video Settings", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateAudioSettingsMenu() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf();
        // TODO - audio settings toggle options and sliders

        return MenuHelper.generateBasicMenu(
                "Audio Settings", "Under construction...",
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateTechnicalSettingsMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuToggleOptions(
                new String[] { "SHOW DEBUG & FRAMERATE", "PIXEL GRID" },
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
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Technical Settings", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateWikiMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "SENTRIES", "PLATFORMS", "MOVEMENT" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.SENTRIES_WIKI,
                                generateSentriesWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.PLATFORMS_WIKI,
                                generatePlatformWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.MOVEMENT_RULES_WIKI,
                                generateMovementRulesWikiPage())
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Wiki", "BRIEF DESCRIPTIONS OF THE GAME'S SYSTEMS",
                contents, MenuIDs.MAIN_MENU);
    }

    private static JBJGLMenu generateSentriesWikiPage() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateSentryButtons();

        return MenuHelper.generateBasicMenu(
                "Sentries", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.WIKI);
    }

    private static JBJGLMenu generatePlatformWikiPage() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateMenuTextBlurb(
                        """
                                Platforms make up the "stage" of each level.
                                The player starts on the starting platform,
                                where no sentry can begin the level. Each
                                sentry has a platform that it patrols.""",
                        JBJGLText.Orientation.CENTER,
                        JBJGLMenuElement.Anchor.CENTRAL,
                        MenuHelper.widthCoord(0.5),
                        MenuHelper.heightCoord(0.6), 2));

        return MenuHelper.generateBasicMenu("Platforms", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.WIKI);
    }

    private static JBJGLMenu generateMovementRulesWikiPage() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "JUMP & DIVE", "TELEPORTATION", "SAVE & LOAD" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.JUMP_DROP_MOVEMENT_WIKI,
                                generateJumpDiveWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.TELEPORTATION_MOVEMENT_WIKI,
                                generateTeleportationWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.SAVE_LOAD_MOVEMENT_WIKI,
                                generateSaveLoadWikiPage())
                });

        return MenuHelper.generateBasicMenu("Player Movement", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.WIKI);
    }

    private static JBJGLMenu generateJumpDiveWikiPage() {
        final String subtitle = "JUMP: " +
                ControlScheme.getCorrespondingKey(ControlScheme.Action.JUMP).print() +
                ", DROP/DIVE: " +
                ControlScheme.getCorrespondingKey(ControlScheme.Action.DROP).print();
        final String text = """
                                When supported by a platform, the player can
                                jump up and drop down. In mid-air, the player
                                cannot jump, but can dive repeatedly to
                                increase downward velocity.""";

        return MenuHelper.generatePlayerMovementTypeMenu(
                "Jumping & Diving", subtitle, text);
    }

    private static JBJGLMenu generateTeleportationWikiPage() {
        final String subtitle =
                ControlScheme.getCorrespondingKey(ControlScheme.Action.INIT_TELEPORT).print();
        final String text = """
                                At any point, the player can charge a telepor-
                                tation jump to the left or right and release
                                to teleport to their projection. The jump is of
                                variable length, depending on the charge time.""";

        return MenuHelper.generatePlayerMovementTypeMenu(
                "Teleportation", subtitle, text);
    }

    private static JBJGLMenu generateSaveLoadWikiPage() {
        final String subtitle = "SAVE: " +
                ControlScheme.getCorrespondingKey(ControlScheme.Action.SAVE_POS).print() +
                ", LOAD: " +
                ControlScheme.getCorrespondingKey(ControlScheme.Action.LOAD_POS).print();
        final String text = """
                                At any point, the player can save position.
                                Unless the player is falling without platform
                                cover below, the saved position can be loaded
                                and the player will teleport there. Saved
                                positions expire once they are loaded.""";

        return MenuHelper.generatePlayerMovementTypeMenu(
                "Save & Load Position", subtitle, text);
    }

    private static JBJGLMenu generateAboutMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "PATCH NOTES", "BACKGROUND", "THE DEVELOPER", "FEEDBACK" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.PATCH_NOTES,
                                generatePatchNotesMenu()),
                        () -> MenuHelper.linkMenu(MenuIDs.BACKGROUND_ABOUT,
                                generateBackgroundAboutPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.DEVELOPER_ABOUT,
                                generateDeveloperAboutPage()),
                        null // TODO - feedback
                });

        return MenuHelper.generateBasicMenu("Information", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.MAIN_MENU);
    }

    private static JBJGLMenu generatePatchNotesMenu() {
        return MenuHelper.generatePatchNotesPage(
                TextIO.readUpdates(),
                MenuIDs.ABOUT,
                TextIO.DEFAULT_PATCH_NOTES_PAGE_INDEX);
    }

    private static JBJGLMenu generateBackgroundAboutPage() {
        final Path backgroundFilepath = ParserWriter.RESOURCE_ROOT.resolve(
                Paths.get("text", "background.txt"));

        final String backgroundText = JBJGLFileIO.readFile(backgroundFilepath);

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateMenuTextBlurb(
                        backgroundText,
                        JBJGLText.Orientation.CENTER,
                        JBJGLMenuElement.Anchor.CENTRAL,
                        MenuHelper.widthCoord(0.5), MenuHelper.heightCoord(0.6),
                        TechnicalSettings.getPixelSize() / 4));

        return MenuHelper.generateBasicMenu("Background", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.ABOUT);
    }

    private static JBJGLMenu generateDeveloperAboutPage() {
        final int DIM_X = 160, DIM_Y = 160;
        final int NUM_IMAGES = 6, FILENAME_INDEX_OFFSET = 1;
        final String baseFilename = "ff-large-running-cycle-";

        final Path devImageFolder = ParserWriter.RESOURCE_ROOT.resolve(
                Paths.get("images", "developer-running-cycle"));
        final Path devTextFilepath = TextIO.TEXT_FOLDER.resolve(
                Paths.get("developer.txt"));
        final String devText = JBJGLFileIO.readFile(devTextFilepath);

        final JBJGLImage[] devImages = new JBJGLImage[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
            final String filename = baseFilename + (i + FILENAME_INDEX_OFFSET) + ".png";
            devImages[i] =
                    JBJGLImageIO.readImage(devImageFolder.resolve(filename));

        }

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                JBJGLAnimationMenuElement.generate(
                        new int[] {
                                MenuHelper.widthCoord(0.5),
                                MenuHelper.heightCoord(0.30)
                        }, new int[] { DIM_X, DIM_Y }, JBJGLMenuElement.Anchor.CENTRAL,
                        5, devImages
                ),
                MenuHelper.generateMenuTextBlurb(devText,
                        JBJGLText.Orientation.CENTER,
                        MenuHelper.widthCoord(0.5),
                        MenuHelper.heightCoord(0.5),
                        TechnicalSettings.getPixelSize() / 4));

        return MenuHelper.generateBasicMenu("The Developer", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.ABOUT);
    }

    // EDITOR SECTION
    public static JBJGLMenu generateEditorMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "BACK TO EDITOR", "TEST LEVEL", "RESET", "QUIT TO MENU" },
                new Runnable[] {
                        () -> Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX),
                        null, // TODO
                        () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_EDITOR_RESET,
                                generateAreYouSureResetEditor()),
                        () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_EDITOR_QUIT_TO_MENU,
                                generateAreYouSureQuitToMainMenu(MenuIDs.EDITOR_MENU))
                }, 1.0);

        return MenuHelper.generateBasicMenu("Level Editor",
                MenuHelper.DOES_NOT_EXIST, contents);
    }

    // GAMEPLAY SECTION
    private static JBJGLMenu generatePauseMenu(final Level level) {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "RESUME", "SETTINGS", "QUIT TO MENU" },
                new Runnable[] {
                        () -> Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX),
                        () -> MenuHelper.linkMenu(MenuIDs.SETTINGS,
                                generateSettingsMenu(false)),
                        () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_PAUSE_QUIT_TO_MENU,
                                generateAreYouSureQuitToMainMenu(MenuIDs.PAUSE_MENU))
                }, 1.0);

        return MenuHelper.generateBasicMenu(level.getName(),
                level.getHint(), contents);
    }

    private static JBJGLMenu generateLevelCompleteMenu(final Level level) {
        final boolean hasNextLevel = Translation.campaign.hasNextLevel();
        final String[] buttonLabels = hasNextLevel 
                ? new String[] { "STATS", "NEXT LEVEL", "REPLAY", "BACK TO MENU" }
                : new String[] { "STATS", "REPLAY", "BACK TO MENU" };

        final Runnable statsBehaviour =
                () -> MenuHelper.linkMenu(MenuIDs.STATS_LEVEL_COMPLETE,
                        generateLevelCompleteStatsPage(level));
        final Runnable nextLevelBehaviour = () -> {
            Translation.campaign.setToNextLevel();
            Translation.campaign.getLevel().getStats().reset();
            Translation.campaign.getLevel().launchLevel();
            Translation.manager
                    .setActiveStateIndex(Translation.GAMEPLAY_INDEX);
        };
        final Runnable replayBehaviour = () -> {
            level.getStats().reset();
            level.launchLevel();
            Translation.manager
                    .setActiveStateIndex(Translation.GAMEPLAY_INDEX);
        };
        final Runnable backToMenuBehaviour = () -> {
            Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
            MenuHelper.linkMenu(MenuIDs.MAIN_MENU);
        };

        final Runnable[] buttonBehaviours = hasNextLevel
                        ? new Runnable[] {
                                statsBehaviour, nextLevelBehaviour,
                        replayBehaviour, backToMenuBehaviour}
                        : new Runnable[] {
                                statsBehaviour, replayBehaviour,
                        backToMenuBehaviour};

        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                buttonLabels, buttonBehaviours, 1.0);

        return MenuHelper.generateBasicMenu("Level Complete!",
                level.getName().toUpperCase(), contents);
    }

    private static JBJGLMenu generateLevelCompleteStatsPage(final Level level) {
        final JBJGLMenuElementGrouping contents =
                MenuHelper.generateLevelStatsText(level.getStats());

        return MenuHelper.generateBasicMenu(
                "Completion Statistics", level.getName().toUpperCase(),
                contents, MenuIDs.LEVEL_COMPLETE);
    }

    private static JBJGLMenu generateTitleCard() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                // behaviour
                JBJGLTimedMenuElement.generate(TITLE_CARD_TICKS, () -> {
                    Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                    MenuHelper.linkMenu(MenuIDs.MAIN_MENU, generateMainMenu());
                }),

                // visuals in render order
                MenuHelper.generateGameTitleLogo(
                        MenuHelper.widthCoord(0.5), MenuHelper.heightCoord(0.5),
                        JBJGLMenuElement.Anchor.CENTRAL),
                MenuHelper.generateMenuTextBlurb(
                        """
                                A PURE PLATFORMING PLAYGROUND
                                BY FLINKER FLITZER""",
                        JBJGLText.Orientation.CENTER,
                        MenuHelper.widthCoord(0.5),
                        MenuHelper.heightCoord(0.625), 1));

        return MenuHelper.generatePlainMenu(contents);
    }

    private static JBJGLMenu generateFlinkerFlitzerSplashScreen() {
        return MenuHelper.generateSplashScreen(
                i -> i + 1,
                FF_SPLASH_SCREEN_FRAME_COUNT,
                FF_SPLASH_SCREEN_TICKS_PER_FRAME, 0, 10,
                Paths.get("resources", "images",
                        "splash_screen", "splash_screen_3"),
                "ss3-frame (");
    }

    private static JBJGLMenu generateSplashScreen1() {
        return MenuHelper.generateSplashScreen(
                i -> 1 + ((i + 7) % 45),
                SPLASH_SCREEN_1_FRAME_COUNT,
                SPLASH_SCREEN_1_TICKS_PER_FRAME,
                5, 1,
                Paths.get("resources", "images",
                        "splash_screen", "splash_screen_1"),
                "ss1-frame- (");
    }

    private static JBJGLMenu generateSplashScreen2() {
        return MenuHelper.generateSplashScreen(
                i -> i + 1,
                SPLASH_SCREEN_2_FRAME_COUNT,
                SPLASH_SCREEN_2_TICKS_PER_FRAME,
                15, 1,
                Paths.get("resources", "images",
                        "splash_screen", "splash_screen_2"),
                "ss2-frame- (");
    }

    private static JBJGLMenu generateAreYouSureQuitGame() {
        return MenuHelper.generateAreYouSureMenu(
                "you want to quit the game?",
                () -> MenuHelper.linkMenu(MenuIDs.MAIN_MENU),
                Translation::quitGame);
    }

    private static JBJGLMenu generateAreYouSureQuitToMainMenu(final String noMenuID) {
        return MenuHelper.generateAreYouSureMenu(
                "you want to quit to the main menu?",
                () -> MenuHelper.linkMenu(noMenuID),
                () -> {
                    Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                    MenuHelper.linkMenu(MenuIDs.MAIN_MENU, generateMainMenu());
                });
    }

    private static JBJGLMenu generateAreYouSureResetEditor() {
        return MenuHelper.generateAreYouSureMenu(
                "you want to reset the level editor?",
                () -> MenuHelper.linkMenu(MenuIDs.EDITOR_MENU),
                () -> {
                    Editor.reset();
                    Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX);
                }
        );
    }
}

package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLAnimationMenuElement;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElementGrouping;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLTimedMenuElement;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.translation.Info;
import com.jordanbunke.translation.ResourceManager;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.editor.Editor;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.BrowserIO;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.io.TextIO;
import com.jordanbunke.translation.menus.custom_elements.TypedInputMenuElement;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.settings.debug.DebugSettings;
import com.jordanbunke.translation.sound.Sounds;
import com.jordanbunke.translation.utility.Utility;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
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

    public static void generateAfterVideoSettingsUpdate(
            final JBJGLMenuManager manager, final boolean isMainMenu, final Level level
    ) {
        if (isMainMenu)
            manager.addMenu(MenuIDs.MAIN_MENU, generateMainMenu(), false);
        else {
            manager.addMenu(MenuIDs.PAUSE_MENU, generatePauseMenu(level), false);

            Translation.levelCompleteState.getMenuManager().addMenu(
                    MenuIDs.LEVEL_COMPLETE, generateLevelCompleteMenu(level), true);
        }

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
                                "GAME MECHANICS", "INFORMATION", "QUIT GAME"
                        },
                        new Runnable[] {
                                () -> MenuHelper.linkMenu(MenuIDs.PLAY_MENU, generatePlayMenu()),
                                () -> Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX),
                                () -> MenuHelper.linkMenu(MenuIDs.SETTINGS, generateSettingsMenu(true)),
                                () -> MenuHelper.linkMenu(MenuIDs.GAME_MECHANICS, generateGameMechanicsMenu()),
                                () -> MenuHelper.linkMenu(MenuIDs.INFORMATION, generateInformationMenu()),
                                () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_QUIT_GAME, generateAreYouSureQuitGame())
                        }),
                MenuHelper.generateDevelopmentInformation(),
                MenuHelper.generateMainMenuSplashText()
        );

        return MenuHelper.generatePlainMenu(contents);
    }

    private static JBJGLMenu generatePlayMenu() {
        return MenuHelper.generateBasicMenu(
                "Play...", MenuHelper.DOES_NOT_EXIST,
                MenuHelper.generateListMenuOptions(
                        new String[] { "MAIN CAMPAIGNS", "TUTORIAL",
                                "MY CONTENT", "IMPORTED CAMPAIGNS" },
                        new Runnable[] {
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_FOLDER, MenuHelper.generateMainCampaignsFolderMenu()),
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_LEVELS, MenuHelper.generateMenuForTutorial()),
                                () -> MenuHelper.linkMenu(MenuIDs.MY_CONTENT_MENU, generateMyContentMenu()),
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_FOLDER, MenuHelper.generateImportedCampaignsFolderMenu())
                        }), MenuIDs.MAIN_MENU);
    }

    private static JBJGLMenu generateMyContentMenu() {
        return MenuHelper.generateBasicMenu(
                "My Content", MenuHelper.DOES_NOT_EXIST,
                MenuHelper.generateListMenuOptions(
                        new String[] { "MY LEVELS", "MY CAMPAIGNS" },
                        new Runnable[] {
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_LEVELS, MenuHelper.generateMenuForMyLevels()),
                                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_FOLDER, MenuHelper.generateMyCampaignsFolderMenu())
                        }
                ), MenuIDs.PLAY_MENU);
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
                        () -> MenuHelper.linkMenu(MenuIDs.AUDIO_SETTINGS, generateAudioSettingsMenu()),
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
                new String[] {
                        """
The camera can either steadily follow
you (the player), stay glued to you,
or be fixed in place and manually movable""",
                        """
UI element indicating when the camera's
follow mode has been updated""",
                        "",
                        """
Whether linking tethers between necromancers
and the sentries that they have reanimated are
visible or not"""
                },
                new String[][] {
                        Arrays.stream(Camera.FollowMode.values())
                                .map(Camera.FollowMode::toString).toArray(String[]::new),
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
        final JBJGLMenuElementGrouping controlsButtons =
                MenuHelper.generateControlsButtons();

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateListMenuOptions(
                        new String[] { "RESET" },
                        new Runnable[] {
                                () -> ControlScheme.reset(controlsButtons.getMenuElements())
                        }),
                controlsButtons,
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
                new String[] { "FULLSCREEN", "PIXEL ALIGNMENT", "TYPEFACE", "THEME" },
                new String[] {
                        """
The game is optimized to be run on 16:9 screens
between 720p and 1080p, so running the game on
fullscreen on a monitor outside of these bounds
may result in buttons and on-screen information
being rendered incorrectly.""",
                        """
Every "pixel" in from a game art perspective
is actually rendered onto a 4x4 pixel area.
Turning on pixel alignment will lock any entity
onto this 4x4 grid to provide the illusion that
rendering is actually constrained to those intervals.""",
                        "",
                        """
Different themes have different colour palettes,
UI element designs, etc."""
                },
                new String[][] {
                        new String[] { "ON", "OFF" },
                        new String[] { "ON", "OFF" },
                        Arrays.stream(Fonts.Typeface.values())
                                .map(Fonts.Typeface::toString).toArray(String[]::new),
                        Arrays.stream(TechnicalSettings.Theme.values())
                                .map(TechnicalSettings.Theme::toString).toArray(String[]::new)
                },
                new Runnable[][] {
                        new Runnable[] {
                                () -> Translation.resize(false),
                                () -> Translation.resize(true)
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setPixelAlignment(false),
                                () -> TechnicalSettings.setPixelAlignment(true)
                        },
                        new Runnable[] {
                                () -> Fonts.setTypeface(Fonts.getTypeface().next()),
                                () -> Fonts.setTypeface(Fonts.getTypeface().next()),
                                () -> Fonts.setTypeface(Fonts.getTypeface().next())
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setTheme(TechnicalSettings.getTheme().next()),
                                () -> TechnicalSettings.setTheme(TechnicalSettings.getTheme().next()),
                                () -> TechnicalSettings.setTheme(TechnicalSettings.getTheme().next())
                        }
                },
                new Callable[] {
                        () -> TechnicalSettings.isFullscreen() ? 0 : 1,
                        () -> TechnicalSettings.isPixelAlignment() ? 0 : 1,
                        () -> Fonts.getTypeface().ordinal(),
                        () -> TechnicalSettings.getTheme().ordinal()
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Video Settings", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateAudioSettingsMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuToggleOptions(
                new String[] {
                        "UI SOUNDS", "MILESTONE SOUNDS", "PLAYER SOUNDS",
                        "SENTRY SOUNDS", "ENVIRONMENT SOUNDS"
                },
                new String[] {
                        """
Button presses, editor changes, etc.""",
                        """
Level completion, failing a level, etc.""",
                        "",
                        "",
                        """
Changes to gravity or magnetic field"""
                },
                new String[][] {
                        new String[] { "ON", "OFF" },
                        new String[] { "ON", "OFF" },
                        new String[] { "ON", "OFF" },
                        new String[] { "ON", "OFF" },
                        new String[] { "ON", "OFF" }
                },
                new Runnable[][] {
                        new Runnable[] {
                                () -> TechnicalSettings.setPlayUISounds(false),
                                () -> TechnicalSettings.setPlayUISounds(true)
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setPlayMilestoneSounds(false),
                                () -> TechnicalSettings.setPlayMilestoneSounds(true)
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setPlayPlayerSounds(false),
                                () -> TechnicalSettings.setPlayPlayerSounds(true)
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setPlaySentrySounds(false),
                                () -> TechnicalSettings.setPlaySentrySounds(true)
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setPlayEnvironmentSounds(false),
                                () -> TechnicalSettings.setPlayEnvironmentSounds(true)
                        }
                },
                new Callable[] {
                        () -> TechnicalSettings.isPlayUISounds() ? 0 : 1,
                        () -> TechnicalSettings.isPlayMilestoneSounds() ? 0 : 1,
                        () -> TechnicalSettings.isPlayPlayerSounds() ? 0 : 1,
                        () -> TechnicalSettings.isPlaySentrySounds() ? 0 : 1,
                        () -> TechnicalSettings.isPlayEnvironmentSounds() ? 0 : 1
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Audio Settings", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateTechnicalSettingsMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuToggleOptions(
                new String[] { "DEBUG MODE", "EDITOR RETICLE" },
                new String[] {
                        """
Turning on debug mode will display the
debug log and the frame rate.""",
                        """
The indicator in the middle of the screen
in the level editor that shows what the
camera is pointing at"""
                },
                new String[][] {
                        new String[] { "ON", "OFF" },
                        new String[] { "FANCY", "SIMPLE CROSSHAIR" }
                },
                new Runnable[][] {
                        new Runnable[] {
                                () -> DebugSettings.setPrintDebug(false),
                                () -> DebugSettings.setPrintDebug(true)
                        },
                        new Runnable[] {
                                () -> TechnicalSettings.setFancyReticle(false),
                                () -> TechnicalSettings.setFancyReticle(true)
                        }
                },
                new Callable[] {
                        () -> DebugSettings.isPrintDebug() ? 0 : 1,
                        () -> TechnicalSettings.isFancyReticle() ? 0 : 1
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Technical Settings", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.SETTINGS);
    }

    private static JBJGLMenu generateGameMechanicsMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "SENTRIES", "PLATFORMS", "MOVEMENT" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.SENTRIES_GM,
                                generateSentriesWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.PLATFORMS_GM,
                                generatePlatformWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.MOVEMENT_RULES_GM,
                                generateMovementRulesWikiPage())
                }, 1.0);

        return MenuHelper.generateBasicMenu(
                "Game Mechanics", "BRIEF DESCRIPTIONS OF THE GAME'S SYSTEMS",
                contents, MenuIDs.MAIN_MENU);
    }

    private static JBJGLMenu generateSentriesWikiPage() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateSentryButtons();

        return MenuHelper.generateBasicMenu(
                "Sentries", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.GAME_MECHANICS);
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
                contents, MenuIDs.GAME_MECHANICS);
    }

    private static JBJGLMenu generateMovementRulesWikiPage() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "JUMP & DIVE", "TELEPORTATION", "SAVE & LOAD" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.JUMP_DROP_MOVEMENT_GM,
                                generateJumpDiveWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.TELEPORTATION_MOVEMENT_GM,
                                generateTeleportationWikiPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.SAVE_LOAD_MOVEMENT_GM,
                                generateSaveLoadWikiPage())
                });

        return MenuHelper.generateBasicMenu("Player Movement", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.GAME_MECHANICS);
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

    private static JBJGLMenu generateInformationMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "PATCH NOTES", "THE DEVELOPER", "FEEDBACK" },
                new Runnable[] {
                        () -> MenuHelper.linkMenu(MenuIDs.PATCH_NOTES,
                                generatePatchNotesMenu()),
                        () -> MenuHelper.linkMenu(MenuIDs.DEVELOPER_ABOUT,
                                generateDeveloperAboutPage()),
                        () -> MenuHelper.linkMenu(MenuIDs.FEEDBACK,
                                generateFeedbackPage()),
                });

        return MenuHelper.generateBasicMenu("Information", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.MAIN_MENU);
    }

    private static JBJGLMenu generatePatchNotesMenu() {
        return MenuHelper.generatePatchNotesPage(
                TextIO.readUpdates(),
                MenuIDs.INFORMATION,
                TextIO.DEFAULT_PATCH_NOTES_PAGE_INDEX);
    }

    private static JBJGLMenu generateFeedbackPage() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateListMenuOptions(
                        new String[] { "ITCH.IO COMMUNITY PAGE", "MY TWITTER" },
                        new Runnable[] {
                                () -> BrowserIO.openLink(URI.create(Info.THIS_GAME_ITCH_LINK)),
                                () -> BrowserIO.openLink(URI.create(Info.MY_TWITTER_LINK))
                        }, 1.0));

        return MenuHelper.generateBasicMenu("Feedback", "Please be nice!",
                contents, MenuIDs.INFORMATION);
    }

    private static JBJGLMenu generateDeveloperAboutPage() {
        final int DIM_X = 160, DIM_Y = 160;
        final int NUM_IMAGES = 6, FILENAME_INDEX_OFFSET = 1;
        final String baseFilename = "ff-large-running-cycle-";

        final Path devImageFolder = ResourceManager.getImagesFolder().resolve("developer-running-cycle");
        final Path devTextFilepath = ResourceManager.getTextFolder().resolve("developer.txt");
        final String devText = ResourceManager.getTextResource(devTextFilepath);

        final JBJGLImage[] devImages = new JBJGLImage[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
            final String filename = baseFilename + (i + FILENAME_INDEX_OFFSET) + ".png";
            devImages[i] = ResourceManager.getImageResource(devImageFolder.resolve(filename));
        }

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                JBJGLAnimationMenuElement.generate(
                        new int[] {
                                MenuHelper.widthCoord(0.2),
                                MenuHelper.heightCoord(0.5)
                        }, new int[] { DIM_X, DIM_Y }, JBJGLMenuElement.Anchor.CENTRAL,
                        5, devImages),
                MenuHelper.generateMenuTextBlurb(devText,
                        JBJGLText.Orientation.CENTER, JBJGLMenuElement.Anchor.CENTRAL,
                        MenuHelper.widthCoord(0.6),
                        MenuHelper.heightCoord(0.5),
                        TechnicalSettings.getPixelSize() / 2.),
                MenuHelper.generateHorizontalListMenuOptions(
                        new String[] { "PUBLISHED GAMES", "GITHUB", "HIRE ME" },
                        new Runnable[] {
                                () -> BrowserIO.openLink(URI.create(Info.MY_GAMES_LINK)),
                                () -> BrowserIO.openLink(URI.create(Info.MY_GITHUB_LINK)),
                                () -> BrowserIO.openLink(URI.create(Info.HIRE_ME_LINK))
                        }, MenuHelper.heightCoord(0.9), MenuHelper.widthCoord(0.3)
                ));

        return MenuHelper.generateBasicMenu("The Developer", MenuHelper.DOES_NOT_EXIST,
                contents, MenuIDs.INFORMATION);
    }

    // EDITOR SECTION
    public static JBJGLMenu generateEditorMenu() {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "BACK TO EDITOR", "TEST LEVEL", "RESET", "QUIT TO MENU" },
                new Runnable[] {
                        () -> {
                            Sounds.gameResumed();

                            Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX);
                        },
                        () -> {
                            final Level level = Level.fromEditor();
                            Translation.setLevel(level);
                            level.launchLevel();
                            Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX);
                        },
                        () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_EDITOR_RESET,
                                generateAreYouSureResetEditor()),
                        () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_EDITOR_QUIT_TO_MENU,
                                generateAreYouSureQuitToMainMenu(MenuIDs.EDITOR_MENU))
                });

        return MenuHelper.generateBasicMenu("Level Editor",
                MenuHelper.DOES_NOT_EXIST, contents);
    }

    // GAMEPLAY SECTION
    private static JBJGLMenu generatePauseMenu(final Level level) {
        final boolean isEditorLevel = level.isEditorLevel();
        final String aysButtonHeading = isEditorLevel
                ? "BACK TO EDITOR"
                : "QUIT TO MENU";
        final Runnable aysBehaviour = isEditorLevel
                ? () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_PAUSE_RETURN_TO_EDITOR,
                    generateAreYouSureBackToEditor(MenuIDs.PAUSE_MENU))
                : () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_PAUSE_QUIT_TO_MENU,
                    generateAreYouSureQuitToMainMenu(MenuIDs.PAUSE_MENU));

        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "RESUME", "SETTINGS", aysButtonHeading },
                new Runnable[] {
                        () -> {
                            Sounds.gameResumed();
                            Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX);
                        },
                        () -> MenuHelper.linkMenu(MenuIDs.SETTINGS,
                                generateSettingsMenu(false)),
                        aysBehaviour
                }, 1.0);

        return MenuHelper.generateBasicMenu(level.getName(),
                level.getParsedHint(), contents);
    }

    private static JBJGLMenu generateLevelCompleteMenu(final Level level) {
        final boolean isEditorLevel = level.isEditorLevel();
        final String aysButtonHeading = isEditorLevel
                ? "BACK TO EDITOR"
                : "BACK TO MENU";
        final Runnable aysBehaviour = isEditorLevel
                ? () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_PAUSE_RETURN_TO_EDITOR,
                generateAreYouSureBackToEditor(MenuIDs.LEVEL_COMPLETE))
                : () -> MenuHelper.linkMenu(MenuIDs.ARE_YOU_SURE_PAUSE_QUIT_TO_MENU,
                generateAreYouSureQuitToMainMenu(MenuIDs.LEVEL_COMPLETE));

        final boolean hasNextLevel = !isEditorLevel && Translation.campaign.hasNextLevel();
        final String[] buttonLabels = hasNextLevel 
                ? new String[] { "STATS", "NEXT LEVEL", "REPLAY", aysButtonHeading }
                : new String[] { isEditorLevel ? "SAVE LEVEL" : "STATS", "REPLAY", aysButtonHeading };

        final Runnable statsBehaviour = () -> MenuHelper.linkMenu(MenuIDs.STATS_LEVEL_COMPLETE,
                generateLevelCompleteStatsPage(level));
        final Runnable saveEditorLevelBehaviour = () -> MenuHelper.linkMenu(MenuIDs.SAVE_EDITOR_LEVEL,
                generateSaveEditorLevelMenu());
        final Runnable nextLevelBehaviour = () -> {
            Translation.campaign.setToNextLevel();
            Translation.campaign.getLevel().getStats().reset();
            Translation.campaign.getLevel().launchLevel();
            Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX);
        };
        final Runnable replayBehaviour = () -> {
            level.getStats().reset();
            level.launchLevel();
            Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX);
        };

        final Runnable[] buttonBehaviours = hasNextLevel
                ? new Runnable[] { statsBehaviour, nextLevelBehaviour, replayBehaviour, aysBehaviour }
                : new Runnable[] {
                isEditorLevel
                        ? saveEditorLevelBehaviour
                        : statsBehaviour,
                replayBehaviour, aysBehaviour
        };

        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                buttonLabels, buttonBehaviours, isEditorLevel ? 0. : 1.);

        return MenuHelper.generateBasicMenu(
                "Level " + (isEditorLevel ? "Verified" : "Complete") + "!",
                isEditorLevel ? " " : level.getName().toUpperCase(),
                contents);
    }

    private static JBJGLMenu generateSaveEditorLevelMenu() {
        final int x = MenuHelper.widthCoord(0.5), width = MenuHelper.widthCoord(0.8);

        final TypedInputMenuElement setLevelNameButton =
                MenuHelper.generateTypedInputButton(x,
                        MenuHelper.heightCoord(0.4), width, "SET LEVEL NAME",
                        "", Set.of("", Level.EDITOR_LEVEL_NAME), 60),
                setLevelHintButton = MenuHelper.generateTypedInputButton(x,
                        MenuHelper.heightCoord(0.6), width, "SET LEVEL HINT",
                        "", Set.of(), 120);

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                setLevelNameButton,
                setLevelHintButton,
                MenuHelper.generateConditionalButton("SAVE LEVEL",
                        MenuHelper.widthCoord(0.5), MenuHelper.heightCoord(0.8),
                        MenuHelper.widthCoord(0.3), JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        () -> {
                            final String name = setLevelNameButton.getInput();
                            LevelIO.saveValidatedEditorLevel(name, setLevelHintButton.getInput());
                            MenuHelper.linkMenu(MenuIDs.SAVED_EDITOR_LEVEL_CONFIRMATION,
                                    generateSavedConfirmationMenu(name));
                        },
                        () -> setLevelNameButton.inputIsValid() && setLevelHintButton.inputIsValid()
                )
        );

        return MenuHelper.generateBasicMenu("Save Editor Level",
                MenuHelper.DOES_NOT_EXIST, contents, MenuIDs.LEVEL_COMPLETE);
    }

    private static JBJGLMenu generateSavedConfirmationMenu(final String name) {
        final JBJGLMenuElementGrouping contents = MenuHelper.generateListMenuOptions(
                new String[] { "BACK TO EDITOR", "MAIN MENU" },
                new Runnable[] {
                        () -> Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX),
                        () -> MenuHelper.linkMenu(MenuIDs.MAIN_MENU, generateMainMenu())
                },
                1.);

        return MenuHelper.generateBasicMenu("Level Saved:", name, contents);
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
                        JBJGLText.Orientation.CENTER, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        MenuHelper.widthCoord(0.5),
                        MenuHelper.heightCoord(0.625), 1));

        return MenuHelper.generatePlainMenu(contents);
    }

    private static JBJGLMenu generateFlinkerFlitzerSplashScreen() {
        return MenuHelper.generateSplashScreen(
                i -> i + 1,
                FF_SPLASH_SCREEN_FRAME_COUNT,
                FF_SPLASH_SCREEN_TICKS_PER_FRAME, 0, 10,
                ResourceManager.getImagesFolder().resolve(Path.of("splash_screen", "splash_screen_3")),
                "ss3-frame (");
    }

    private static JBJGLMenu generateSplashScreen1() {
        return MenuHelper.generateSplashScreen(
                i -> 1 + ((i + 7) % 45),
                SPLASH_SCREEN_1_FRAME_COUNT,
                SPLASH_SCREEN_1_TICKS_PER_FRAME,
                5, 1,
                ResourceManager.getImagesFolder().resolve(Path.of("splash_screen", "splash_screen_1")),
                "ss1-frame- (");
    }

    private static JBJGLMenu generateSplashScreen2() {
        return MenuHelper.generateSplashScreen(
                i -> i + 1,
                SPLASH_SCREEN_2_FRAME_COUNT,
                SPLASH_SCREEN_2_TICKS_PER_FRAME,
                15, 1,
                ResourceManager.getImagesFolder().resolve(Path.of("splash_screen", "splash_screen_2")),
                "ss2-frame- (");
    }

    private static JBJGLMenu generateAreYouSureQuitGame() {
        return MenuHelper.generateAreYouSureMenu(false,
                "you want to quit the game?",
                () -> MenuHelper.linkMenu(MenuIDs.MAIN_MENU),
                Translation::quitGame);
    }

    private static JBJGLMenu generateAreYouSureQuitToMainMenu(final String noMenuID) {
        return MenuHelper.generateAreYouSureMenu(false,
                "you want to quit to the main menu?",
                () -> MenuHelper.linkMenu(noMenuID),
                () -> {
                    Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                    MenuHelper.linkMenu(MenuIDs.MAIN_MENU, generateMainMenu());
                });
    }

    private static JBJGLMenu generateAreYouSureBackToEditor(final String noMenuID) {
        final String consequence = noMenuID.equals(MenuIDs.PAUSE_MENU) ? "from scratch" : "again";

        return MenuHelper.generateAreYouSureMenu(true,
                "Level will have to be verified " + consequence + "...",
                () -> MenuHelper.linkMenu(noMenuID),
                () -> Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX));
    }

    private static JBJGLMenu generateAreYouSureResetEditor() {
        return MenuHelper.generateAreYouSureMenu(true,
                "All changes will be lost...",
                () -> MenuHelper.linkMenu(MenuIDs.EDITOR_MENU),
                () -> {
                    Editor.reset();
                    Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX);
                }
        );
    }
}

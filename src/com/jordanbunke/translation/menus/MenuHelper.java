package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.events.JBJGLKey;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.jbjgl.io.JBJGLImageIO;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.*;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.jbjgl.text.JBJGLTextComponent;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.io.ParserWriter;
import com.jordanbunke.translation.menus.custom_elements.SetInputMenuElement;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuHelper {

    // Positioning
    private static final int LIST_MENU_INITIAL_Y = 140;
    private static final int LIST_MENU_INCREMENT_Y = TechnicalSettings.getHeight() / 8;
    private static final int MENU_TITLE_Y = 0;
    private static final int MENU_SUBTITLE_Y = LIST_MENU_INITIAL_Y;
    private static final int MENU_TEXT_INCREMENT_Y = TechnicalSettings.getHeight() / 12;
    private static final int MARGIN = 20;

    public static final String DOES_NOT_EXIST = "!does-not-exist!";

    // MENU LINKING

    public static void linkMenu(final String menuID, final JBJGLMenu menu) {
        getMenuManager().addMenu(menuID, menu, true);
    }

    public static void linkMenu(final String menuID) {
        getMenuManager().setActiveMenuID(menuID);
    }

    private static JBJGLMenuManager getMenuManager() {
        final int index = Translation.manager.getActiveStateIndex();

        if (index == Translation.PAUSE_INDEX)
            return Translation.pauseState.getMenuManager();
        else if (index == Translation.LEVEL_COMPLETE_INDEX)
            return Translation.levelCompleteState.getMenuManager();
        else if (index == Translation.SPLASH_SCREEN_INDEX)
            return Translation.splashScreenManager;
        else
            return Translation.menuManager;
    }

    // PUBLICLY ACCESSIBLE

    // full menus

    public static JBJGLMenu generateBasicMenu(
            final String title, final String subtitle,
            final JBJGLMenuElementGrouping contents
    ) {
        return generateBasicMenu(title, subtitle, contents, DOES_NOT_EXIST);
    }

    public static JBJGLMenu generateBasicMenu(
            final String title, final String subtitle,
            final JBJGLMenuElementGrouping contents, final String backMenuID
    ) {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                title.equals(DOES_NOT_EXIST)
                        ? JBJGLPlaceholderMenuElement.generate()
                        : generateTextMenuTitle(title),
                subtitle.equals(DOES_NOT_EXIST)
                        ? JBJGLPlaceholderMenuElement.generate()
                        : generateTextMenuSubtitle(subtitle),
                backMenuID.equals(DOES_NOT_EXIST)
                        ? JBJGLPlaceholderMenuElement.generate()
                        : generateBackButton(backMenuID),
                contents);
    }

    public static JBJGLMenu generatePlainMenu(
            final JBJGLMenuElementGrouping contents
    ) {
        return JBJGLMenu.of(
                // visual render order
                generateElementBackground(),
                contents);
    }

    public static JBJGLMenu generatePageDoesNotExistYet() {
        final int middle = widthCoord(0.5);
        final int buttonWidth = widthCoord(1/3.);
        final int promptY = heightCoord(1/3.);
        final int buttonsY = heightCoord(3/5.);

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(
                        new int[] { middle, promptY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                        2, JBJGLText.Orientation.CENTER,
                                        TLColors.MENU_TEXT(), Fonts.GAME_ITALICS_SPACED()
                                ).addText("UH-OH!").addLineBreak()
                                .addText("THIS PAGE DOES NOT EXIST YET.").build()),
                determineTextButton(
                        "MAIN MENU", new int[] { middle, buttonsY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP, buttonWidth,
                        () -> {
                            Translation.manager.setActiveStateIndex(Translation.MENU_INDEX);
                            MenuHelper.linkMenu(MenuIDs.MAIN_MENU);
                        }));

        return generatePlainMenu(contents);
    }

    public static JBJGLMenu generateAreYouSureMenu(
            final String decisionDescription,
            final Runnable noBehaviour,
            final Runnable yesBehaviour
    ) {
        final int buttonWidth = widthCoord(1/6.);
        final int leftX = widthCoord(0.5) - widthCoord(0.125);
        final int rightX = widthCoord(0.5) + widthCoord(0.125);
        final int promptY = heightCoord(1/3.);
        final int buttonsY = heightCoord(3/5.);

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(
                        new int[] { widthCoord(0.5), promptY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                        2, JBJGLText.Orientation.CENTER,
                                        TLColors.MENU_TEXT(), Fonts.GAME_ITALICS_SPACED()
                                ).addText("ARE YOU SURE").addLineBreak()
                                .addText(decisionDescription.toUpperCase()).build()),
                determineTextButton(
                        "NO", new int[] { leftX, buttonsY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        buttonWidth, noBehaviour),
                determineTextButton(
                        "YES", new int[] { rightX, buttonsY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        buttonWidth, yesBehaviour));

        return generatePlainMenu(contents);
    }

    public static JBJGLMenu generateLevelOverview(
            final Level level, final int index, final Campaign campaign
    ) {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
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
                        widthCoord(0.5),
                        heightCoord(1.0) -
                                (LIST_MENU_INITIAL_Y + (int)(1.5 * LIST_MENU_INCREMENT_Y)),
                        JBJGLMenuElement.Anchor.CENTRAL_TOP),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                widthCoord(0.5),
                                LIST_MENU_INITIAL_Y + (int)(1.9 * LIST_MENU_INCREMENT_Y)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        generateInitialMenuTextBuilder().addText(
                                (level.isBlind() ? "BLIND" : "SIGHTED") + " & " +
                                        (level.isDeterministic() ? "DETERMINISTIC" : "NON-DETERMINISTIC")
                        ).build()),
                generateLevelKeyInfo(level),
                generatePrevNextLevelButtons(index, campaign));

        return generateBasicMenu(level.getName(), level.getHint(),
                contents, MenuIDs.CAMPAIGN_LEVELS);
    }

    public static JBJGLMenu generatePlayerMovementTypeMenu(
            final String heading, final String subtitle, final String text
    ) {
        return generateBasicMenu(heading, subtitle.toUpperCase(),
                JBJGLMenuElementGrouping.generateOf(
                        generateMenuTextBlurb(text, JBJGLText.Orientation.CENTER,
                                JBJGLMenuElement.Anchor.CENTRAL,
                                widthCoord(0.5), heightCoord(0.6), 2)),
                MenuIDs.MOVEMENT_RULES_WIKI);
    }

    private static JBJGLMenu generateSentryRoleWikiPage(final Sentry.Role role) {
        final int pixel = TechnicalSettings.getPixelSize();

        final Path sentryDescriptionFilepath = ParserWriter.RESOURCE_ROOT.resolve(
                Paths.get("sentries", "descriptions",
                        role.name().toLowerCase() + ".txt"));
        final String sentryDescription = JBJGLFileIO.readFile(sentryDescriptionFilepath);

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                // sentry
                JBJGLStaticMenuElement.generate(
                        new int[] {
                                widthCoord(0.5),
                                heightCoord(0.5)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        ImageAssets.drawSentry(role)),
                // metadata
                JBJGLTextMenuElement.generate(
                        new int[] {
                                widthCoord(0.48),
                                heightCoord(0.5)
                        }, JBJGLMenuElement.Anchor.RIGHT_TOP,
                        generateInitialMenuTextBuilder().addText(
                                role.isSightDependent()
                                        ? "RELIES ON SIGHT"
                                        : "DOES NOT RELY ON SIGHT").build()),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                widthCoord(0.52) + pixel,
                                heightCoord(0.5)
                        }, JBJGLMenuElement.Anchor.LEFT_TOP,
                        generateInitialMenuTextBuilder().addText(
                                role.isDeterministic()
                                        ? "DETERMINISTIC BEHAVIOUR"
                                        : "NON-DETERMINISTIC BEHAVIOUR"
                        ).build()),
                // description
                generateMenuTextBlurb(
                        sentryDescription, JBJGLText.Orientation.CENTER,
                        widthCoord(0.5), heightCoord(0.57),
                        2));

        return generateBasicMenu(role.name(),
                "SENTRY TYPE", contents, MenuIDs.SENTRIES_WIKI);
    }

    public static JBJGLMenu generateCampaignFolderMenu(
            final String title, final Campaign[] campaigns,
            final String backMenuID, final int page
    ) {
        final JBJGLMenuElementGrouping contents =
                MenuHelper.generateCampaignsOnPage(title, campaigns, backMenuID, page);

        return MenuHelper.generateBasicMenu(title,
                "PAGE " + (page + 1), contents, backMenuID);
    }

    public static JBJGLMenu generateMenuForCampaign(
            final Campaign campaign, final String backMenuID
    ) {
        return generateMenuForCampaign(campaign, backMenuID, 0);
    }

    public static JBJGLMenu generateMenuForCampaign(
            final Campaign campaign, final String backMenuID, final int page
    ) {
        final JBJGLMenuElementGrouping contents =
                MenuHelper.generateLevelsOnPage(campaign, backMenuID, page);

        return MenuHelper.generateBasicMenu(campaign.getName(),
                "PAGE " + (page + 1), contents, backMenuID);
    }

    public static JBJGLMenu generateSplashScreen(
            final Function<Integer, Integer> indexMapping,
            final int frameCount, final int ticksPerFrame,
            final int paddingFrameCount, final int reps,
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
                        (paddingFrameCount * ticksPerFrame) +
                                (frameCount * reps * ticksPerFrame),
                        () -> Translation.manager.setActiveStateIndex(Translation.MENU_INDEX)
                ),

                // visuals in render order
                generateElementBlackBackground(),
                JBJGLAnimationMenuElement.generate(
                        new int[] { x, y },
                        new int[] { frameWidth, frameHeight },
                        JBJGLMenuElement.Anchor.LEFT_TOP,
                        ticksPerFrame,
                        frames));
    }

    // element generators

    public static JBJGLMenuElementGrouping generateGameTitleLogo() {
        final int x = TechnicalSettings.getWidth() / 2;
        return generateGameTitleLogo(x, MENU_TITLE_Y, JBJGLMenuElement.Anchor.CENTRAL_TOP);
    }

    public static JBJGLMenuElementGrouping generateGameTitleLogo(
            final int x, final int y, final JBJGLMenuElement.Anchor anchor
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        // TODO - optimize
        return JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(
                        new int[] { x, y },
                        anchor,
                        JBJGLText.createOf(
                                pixel, JBJGLText.Orientation.CENTER,
                                JBJGLTextComponent.add(
                                        Translation.TITLE.toUpperCase(),
                                        Fonts.VIGILANT_ITALICS(),
                                        TLColors.BLACK()
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
                                        TLColors.TITLE_RED()
                                )
                        )
                )
        );
    }

    public static JBJGLTextMenuElement generateDevelopmentInformation() {
        final int height = TechnicalSettings.getHeight();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLTextMenuElement.generate(
                new int[] { pixel, height }, JBJGLMenuElement.Anchor.LEFT_BOTTOM,
                generateInitialMenuTextBuilder()
                        .addText("version " + Translation.VERSION).addLineBreak()
                        .addText("Jordan Bunke, 2022").build());
    }

    public static JBJGLTextMenuElement generateMenuTextBlurb(
            final String[] lines, JBJGLText.Orientation orientation,
            final JBJGLMenuElement.Anchor anchor,
            final int x, final int y, final int textSize
    ) {
        final JBJGLTextBuilder tb = generateInitialMenuTextBuilder(textSize, orientation);

        for (int i = 0; i < lines.length; i++) {
            tb.addText(lines[i].equals("") ? " " : lines[i]);

            if (i + 1 < lines.length)
                tb.addLineBreak();
        }

        return JBJGLTextMenuElement.generate(new int[] { x, y }, anchor, tb.build());
    }

    public static JBJGLTextMenuElement generateMenuTextBlurb(
            final String text, JBJGLText.Orientation orientation,
            final JBJGLMenuElement.Anchor anchor,
            final int x, final int y, final int textSize
    ) {
        final String NEW_LINE = "\n";
        return generateMenuTextBlurb(text.split(NEW_LINE),
                orientation, anchor, x, y, textSize);
    }

    public static JBJGLTextMenuElement generateMenuTextBlurb(
            final String text, JBJGLText.Orientation orientation,
            final int x, final int y, final int textSize
    ) {
        final String NEW_LINE = "\n";
        return generateMenuTextBlurb(text.split(NEW_LINE), orientation,
                JBJGLMenuElement.Anchor.CENTRAL_TOP, x, y, textSize);
    }

    public static JBJGLMenuElementGrouping generateListMenuToggleOptions(
            final String[] associatedTexts, final String[][] buttonHeadings,
            final Runnable[][] behaviours, final Callable<Integer>[] updateIndexLogic,
            final double offsetByNRows
    ) {
        final int offsetY = (int)(offsetByNRows * LIST_MENU_INCREMENT_Y);

        final int width = TechnicalSettings.getWidth();
        final int pixel = TechnicalSettings.getPixelSize();
        final int amount = associatedTexts.length;

        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[amount * 2];
        final int associatedX = widthCoord(0.3);
        final int buttonX = widthCoord(0.7);
        int drawY = LIST_MENU_INITIAL_Y + offsetY;
        final int textSize = pixel / 2;

        for (int i = 0; i < amount; i++) {
            final JBJGLTextMenuElement associatedText = JBJGLTextMenuElement.generate(
                    new int[] { associatedX, drawY + (textSize * 2) },
                    JBJGLMenuElement.Anchor.CENTRAL_TOP,
                    generateInitialMenuTextBuilder(textSize).addText(associatedTexts[i]).build()
            );
            final JBJGLToggleClickableMenuElement button = generateTextToggleButton(
                    buttonHeadings[i], new int[] { buttonX, drawY }, behaviours[i],
                    updateIndexLogic[i], TechnicalSettings.pixelLockNumber(width / 4));

            menuElements[i * 2] = associatedText;
            menuElements[(i * 2) + 1] = button;
            drawY += LIST_MENU_INCREMENT_Y;
        }

        return JBJGLMenuElementGrouping.generate(menuElements);
    }

    public static JBJGLMenuElementGrouping generateListMenuOptions(
            final String[] headings, final Runnable[] behaviours,
            final int x, final int offsetY, final JBJGLMenuElement.Anchor anchor
    ) {
        final int width = TechnicalSettings.getWidth();

        final int amount = headings.length;
        int drawY = LIST_MENU_INITIAL_Y + offsetY;

        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[amount];

        for (int i = 0; i < amount; i++) {
            final JBJGLMenuElement button = determineTextButton(
                    headings[i], new int[] { x, drawY }, anchor,
                    TechnicalSettings.pixelLockNumber(width / 3), behaviours[i]);
            menuElements[i] = button;
            drawY += LIST_MENU_INCREMENT_Y;
        }

        return JBJGLMenuElementGrouping.generate(menuElements);
    }

    public static JBJGLMenuElementGrouping generateListMenuOptions(
            final String[] headings, final Runnable[] behaviours
    ) {
        final int width = TechnicalSettings.getWidth();

        return generateListMenuOptions(
                headings, behaviours, width / 2, 0,
                JBJGLMenuElement.Anchor.CENTRAL_TOP);
    }

    public static JBJGLMenuElementGrouping generateListMenuOptions(
            final String[] headings, final Runnable[] behaviours,
            final double offsetByNButtons
    ) {
        final int width = TechnicalSettings.getWidth();

        return generateListMenuOptions(
                headings, behaviours,
                width / 2, (int)(offsetByNButtons * LIST_MENU_INCREMENT_Y),
                JBJGLMenuElement.Anchor.CENTRAL_TOP);
    }

    public static JBJGLMenuElementGrouping generateSentryButtons() {
        final int width = TechnicalSettings.getWidth(),
                height = TechnicalSettings.getHeight();

        final int COLUMNS = 4, INITIAL_Y = heightCoord(0.45);
        final Sentry.Role[] roles = Sentry.Role.values();
        final JBJGLClickableMenuElement[] menuElements = new JBJGLClickableMenuElement[roles.length];

        for (int i = 0; i < roles.length; i++) {
            final int column = i % COLUMNS;
            final int row = i / COLUMNS;
            final int x = widthCoord((column + 1) / (double)(COLUMNS + 1)),
                    y = INITIAL_Y + (row * MENU_TEXT_INCREMENT_Y);
            menuElements[i] = generateSentryButton(x, y, roles[i],
                    widthCoord(1/(COLUMNS + 1.3)));
        }

        return JBJGLMenuElementGrouping.generateOf(
                MenuHelper.generateMenuTextBlurb(
                        "", JBJGLText.Orientation.CENTER,
                        width / 2, height / 3, 1),
                JBJGLMenuElementGrouping.generate(menuElements));
    }

    public static JBJGLMenuElementGrouping generateControlsButtons() {
        final int offsetY = (int)(LIST_MENU_INCREMENT_Y * 1.5);
        final int COLUMNS = 4, CONTROL_AMOUNT = 14, textSize = TechnicalSettings.getPixelSize() / 4;
        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[CONTROL_AMOUNT * 2];

        final String[] associatedTexts = new String[] {
                "MOVE LEFT", "MOVE RIGHT", "JUMP", "DROP",
                "TELEPORT", "SAVE", "LOAD",
                "CAMERA LEFT", "CAMERA RIGHT", "CAMERA UP", "CAMERA DOWN",
                "TOGGLE ZOOM", "TOGGLE FOLLOW MODE", "PAUSE"
        };
        final java.util.List<Consumer<JBJGLKey>> setFunctions = List.of(
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

        final int buttonWidth = widthCoord(2 / (double)(3 * (COLUMNS + 1)));
        final JBJGLImage setButton =
                drawNonHighlightedTextButton(buttonWidth, "SET",
                        TechnicalSettings.getPixelSize() / 4);
        final JBJGLImage setHighlightedButton =
                drawHighlightedTextButton(buttonWidth, "SET",
                        TechnicalSettings.getPixelSize() / 4);

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
            final int x = widthCoord((column + 1) / (double)(COLUMNS + 1));
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

    public static JBJGLMenuElementGrouping generateLevelStatsText(final LevelStats levelStats) {
        final int fieldsX = widthCoord(0.1);
        final int thisRunX = widthCoord(0.5);
        final int pbX = widthCoord(0.8);
        final int y = heightCoord(0.38);

        return JBJGLMenuElementGrouping.generateOf(
                generateLevelStatFields(fieldsX, y, true, 2),
                generateThisRunStats(thisRunX, y, true, 2, levelStats),
                generatePBs(pbX, y, true, 2, levelStats));
    }

    public static JBJGLMenuElementGrouping generateCampaignsOnPage(
            final String title, final Campaign[] campaigns,
            final String backMenuID, final int page
    ) {
        final int INITIAL_Y = LIST_MENU_INITIAL_Y + (int)(2.5 * LIST_MENU_INCREMENT_Y);
        final int BUTTON_WIDTH = widthCoord(1/5.3);
        final int CAMPAIGNS_ON_PAGE = 3, campaignCount = campaigns.length;
        final int campaignsOnThisPage = Math.min(
                CAMPAIGNS_ON_PAGE, campaignCount - (CAMPAIGNS_ON_PAGE * page));
        final int startingIndex = page * CAMPAIGNS_ON_PAGE;

        final boolean hasPreviousPage = page > 0;
        final boolean hasNextPage = campaignCount > ((page + 1) * CAMPAIGNS_ON_PAGE);

        final int menuElementsCount = campaignsOnThisPage +
                calculatePreviousNextTotal(hasPreviousPage, hasNextPage);

        final JBJGLMenuElement[] campaignButtons = new JBJGLMenuElement[menuElementsCount];

        for (int i = 0; i < campaignsOnThisPage; i++) {
            final int x = widthCoord(0.5),
                    y = INITIAL_Y + (i * LIST_MENU_INCREMENT_Y);
            final Campaign campaign = campaigns[startingIndex + i];
            campaignButtons[i] = generateCampaignButton(
                    x, y, widthCoord(0.8), campaign);
        }

        populatePreviousAndNext(campaignButtons,
                hasPreviousPage, hasNextPage, BUTTON_WIDTH,
                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_FOLDER,
                        generateCampaignFolderMenu(
                                title, campaigns, backMenuID, page - 1)),
                () -> MenuHelper.linkMenu(
                        MenuIDs.CAMPAIGN_FOLDER,
                        generateCampaignFolderMenu(
                                title, campaigns, backMenuID, page + 1)));

        return JBJGLMenuElementGrouping.generate(campaignButtons);
    }

    public static JBJGLMenuElementGrouping generateLevelsOnPage(
            final Campaign campaign, final String backMenuID, final int page
    ) {
        final int COLUMNS = 4, INITIAL_Y = heightCoord(0.45);
        final int BUTTON_WIDTH = widthCoord(1/(COLUMNS + 1.3));
        final int LEVELS_ON_PAGE = 16, levelCount = campaign.getLevelCount();
        final int levelsOnThisPage = Math.min(
                LEVELS_ON_PAGE, levelCount - (LEVELS_ON_PAGE * page));
        final int startingIndex = page * LEVELS_ON_PAGE;

        final boolean hasPreviousPage = page > 0;
        final boolean hasNextPage = levelCount > ((page + 1) * LEVELS_ON_PAGE);

        final int menuElementsCount = levelsOnThisPage +
                calculatePreviousNextTotal(hasPreviousPage, hasNextPage);

        final JBJGLMenuElement[] levelButtons = new JBJGLMenuElement[menuElementsCount];

        for (int i = 0; i < levelsOnThisPage; i++) {
            final int column = i % COLUMNS;
            final int row = i / COLUMNS;
            final int x = widthCoord((column + 1) / (double)(COLUMNS + 1)),
                    y = INITIAL_Y + (row * LIST_MENU_INCREMENT_Y);
            final Level level = campaign.getLevelAt(startingIndex + i);
            levelButtons[i] = generateLevelButton(
                    x, y, BUTTON_WIDTH, campaign, level, startingIndex + i);
        }

        populatePreviousAndNext(
                levelButtons, hasPreviousPage, hasNextPage,
                BUTTON_WIDTH,
                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_LEVELS,
                        generateMenuForCampaign(
                                Translation.campaign, backMenuID, page - 1)),
                () -> MenuHelper.linkMenu(MenuIDs.CAMPAIGN_LEVELS,
                        generateMenuForCampaign(
                                Translation.campaign, backMenuID, page + 1)));

        return JBJGLMenuElementGrouping.generate(levelButtons);
    }

    // HIDDEN

    // MENUS



    // ELEMENT GENERATORS

    // level 0

    private static JBJGLMenuElement generateCampaignButton(
            final int x, final int y, final int buttonWidth,
            final Campaign campaign
    ) {
        final Runnable behaviour = () -> {
            Translation.campaign = campaign;
            MenuHelper.linkMenu(MenuIDs.CAMPAIGN_LEVELS,
                    generateMenuForCampaign(campaign, MenuIDs.CAMPAIGN_FOLDER));
        };

        return determineTextButton(
                Utility.cutOffIfLongerThan(campaign.getName().toUpperCase(), 40),
                new int[] { x, y }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                buttonWidth, behaviour);
    }

    private static JBJGLMenuElement generateLevelButton(
            final int x, final int y, final int buttonWidth,
            final Campaign campaign, final Level level, final int index
    ) {
        final boolean isUnlocked = campaign.isUnlocked(index);
        final Runnable behaviour = isUnlocked
                ? () -> MenuHelper.linkMenu(MenuIDs.LEVEL_OVERVIEW,
                generateLevelOverview(level, index, campaign))
                : null;

        return determineTextButton(
                Utility.cutOffIfLongerThan(level.getName().toUpperCase(), 10),
                new int[] { x, y }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                buttonWidth, behaviour);
    }

    // level 1

    private static JBJGLMenuElement generateBackButton(final String backMenuID) {
        return determineTextButton(
                "< BACK", new int[] { MARGIN, MARGIN },
                JBJGLMenuElement.Anchor.LEFT_TOP,
                widthCoord(0.125),
                () -> linkMenu(backMenuID));
    }

    private static JBJGLMenuElementGrouping generatePrevNextLevelButtons(
            final int index, final Campaign campaign
    ) {
        final int BUTTON_WIDTH = widthCoord(1/5.3);

        final boolean hasPreviousPage = index > 0;
        final boolean hasNextPage = index + 1 < campaign.getLevelCount() && campaign.getLevelsBeaten() > index;

        final int buttonsCount = calculatePreviousNextTotal(hasPreviousPage, hasNextPage);
        final JBJGLMenuElement[] prevNextButtons = new JBJGLMenuElement[buttonsCount];

        populatePreviousAndNext(prevNextButtons,
                hasPreviousPage, hasNextPage, BUTTON_WIDTH,
                () -> MenuHelper.linkMenu(MenuIDs.LEVEL_OVERVIEW,
                        generateLevelOverview(
                                campaign.getLevelAt(index - 1), index - 1, campaign)),
                () -> MenuHelper.linkMenu(MenuIDs.LEVEL_OVERVIEW,
                        generateLevelOverview(
                                campaign.getLevelAt(index + 1), index + 1, campaign)));

        return JBJGLMenuElementGrouping.generate(prevNextButtons);
    }

    private static void populatePreviousAndNext(
            final JBJGLMenuElement[] elements,
            final boolean hasPreviousPage, final boolean hasNextPage,
            final int BUTTON_WIDTH,
            final Runnable previous, final Runnable next
    ) {
        final int PREVIOUS_X = widthCoord(0.2);
        final int NEXT_X = widthCoord(0.8);
        final int NAVIGATION_Y = LIST_MENU_INITIAL_Y + LIST_MENU_INCREMENT_Y;

        final JBJGLMenuElement previousPageButton = determineTextButton(
                "< PREVIOUS", new int[] { PREVIOUS_X, NAVIGATION_Y },
                JBJGLMenuElement.Anchor.CENTRAL_TOP, BUTTON_WIDTH, previous);
        final JBJGLMenuElement nextPageButton = determineTextButton(
                "NEXT >", new int[] { NEXT_X, NAVIGATION_Y },
                JBJGLMenuElement.Anchor.CENTRAL_TOP, BUTTON_WIDTH, next);

        if (hasPreviousPage && hasNextPage) {
            elements[elements.length - 2] = previousPageButton;
            elements[elements.length - 1] = nextPageButton;
        } else if (hasPreviousPage)
            elements[elements.length - 1] = previousPageButton;
        else if (hasNextPage)
            elements[elements.length - 1] = nextPageButton;
    }

    private static JBJGLClickableMenuElement generateSentryButton(
            final int x, final int y, final Sentry.Role role, final int buttonWidth
    ) {
        final JBJGLImage nonHighlightedButton =
                drawNonHighlightedTextButton(buttonWidth, " ", 1);
        final int width = nonHighlightedButton.getWidth(), height = nonHighlightedButton.getHeight();
        final JBJGLImage square = ImageAssets.drawSentry(role);
        final int squareX = (buttonWidth / 2) - (square.getWidth() / 2),
                squareY = (height - square.getHeight()) / 2;

        final Graphics nhbg = nonHighlightedButton.getGraphics();
        nhbg.drawImage(square, squareX, squareY, null);

        final JBJGLImage highlightedButton = drawHighlightedTextButton(
                buttonWidth, role.name(),
                1, role.getColor(TLColors.OPAQUE()));

        return JBJGLClickableMenuElement.generate(
                new int[] { x, y }, new int[] { width, height },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                nonHighlightedButton, highlightedButton,
                () -> linkMenu(MenuIDs.SENTRY_ROLE_WIKI,
                        generateSentryRoleWikiPage(role)));
    }

    private static SetInputMenuElement generateControlButton(
            final int[] position, final int width, final Consumer<JBJGLKey> setFunction,
            final ControlScheme.Action action,
            final JBJGLImage nonHighlightedSetImage, final JBJGLImage highlightedSetImage
    ) {
        final Callable<JBJGLImage> nhGeneratorFunction =
                () -> drawNonHighlightedTextButton(width,
                        Utility.cutOffIfLongerThan(
                                ControlScheme.getCorrespondingKey(action).print(), 16),
                TechnicalSettings.getPixelSize() / 4);
        final Callable<JBJGLImage> hGeneratorFunction =
                () -> drawHighlightedTextButton(width,
                        Utility.cutOffIfLongerThan(
                                ControlScheme.getCorrespondingKey(action).print(), 16),
                        TechnicalSettings.getPixelSize() / 4);

        return SetInputMenuElement.generate(position,
                new int[] { width, nonHighlightedSetImage.getHeight() },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                setFunction, nhGeneratorFunction, hGeneratorFunction,
                nonHighlightedSetImage, highlightedSetImage);
    }

    private static JBJGLMenuElementGrouping generateLevelKeyInfo(final Level level) {
        final int platformsX = widthCoord(0.2);
        final int bestTimeX = widthCoord(0.5);
        final int sentriesX = widthCoord(0.8);
        final int topY = heightCoord(0.5);
        final int bottomYLow = heightCoord(0.625);
        final int bottomYHigh = heightCoord(0.55);

        final int platforms = level.getPlatformSpecs().length;
        final int sentries = level.getSentrySpecs().length;

        return JBJGLMenuElementGrouping.generateOf(
                generateNumberAndCaption(String.valueOf(platforms),
                        platforms == 1 ? "PLATFORM" : "PLATFORMS",
                        platformsX, topY, bottomYLow),
                generateNumberAndCaption(String.valueOf(sentries),
                        sentries == 1 ? "SENTRY" : "SENTRIES",
                        sentriesX, topY, bottomYLow),
                generateNumberAndCaption(
                        level.getStats().getPersonalBest(LevelStats.TIME, true),
                        "BEST TIME:", bestTimeX, bottomYHigh, topY));
    }

    private static JBJGLMenuElementGrouping generateNumberAndCaption(
            final String number, final String caption,
            final int x, final int numberY, final int captionY
    ) {
        return JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(
                        new int[] { x, numberY }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                4, JBJGLText.Orientation.CENTER,
                                TLColors.MENU_TEXT(), Fonts.GAME_ITALICS_SPACED()
                        ).addText(number).build()),
                JBJGLTextMenuElement.generate(
                        new int[] { x, captionY }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                2, JBJGLText.Orientation.CENTER,
                                TLColors.MENU_TEXT(), Fonts.GAME_STANDARD()
                        ).addText(caption).build())
        );
    }

    // level 2

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
                                        ? TLColors.WORSE_THAN_PB(TLColors.OPAQUE())
                                        : TLColors.NEW_PB(TLColors.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.TIME)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.FAILURES)
                                        ? TLColors.WORSE_THAN_PB(TLColors.OPAQUE())
                                        : TLColors.NEW_PB(TLColors.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.FAILURES)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.SIGHTINGS)
                                        ? TLColors.WORSE_THAN_PB(TLColors.OPAQUE())
                                        : TLColors.NEW_PB(TLColors.OPAQUE())
                        )
                        .addText(levelStats.getFinalStat(LevelStats.SIGHTINGS)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(
                                levelStats.isWorseThanPB(LevelStats.MAX_COMBO)
                                        ? TLColors.WORSE_THAN_PB(TLColors.OPAQUE())
                                        : TLColors.NEW_PB(TLColors.OPAQUE())
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

    private static JBJGLTextMenuElement generateTextMenuTitle(final String title) {
        final int INCREMENT = 23;
        final double THRESHOLD = 0.7;

        int offsetY = -INCREMENT;
        int textSize = TechnicalSettings.getPixelSize();
        JBJGLText titleText;

        do {
            titleText = JBJGLTextBuilder.initialize(textSize,
                    JBJGLText.Orientation.CENTER, TLColors.MENU_TEXT(),
                    Fonts.GAME_ITALICS_SPACED()).addText(title).build();

            textSize--;
            offsetY += INCREMENT;
        } while (titleText.draw().getWidth() > widthCoord(THRESHOLD));

        return JBJGLTextMenuElement.generate(
                new int[] { widthCoord(0.5), MENU_TITLE_Y + offsetY },
                JBJGLMenuElement.Anchor.CENTRAL_TOP, titleText);
    }

    private static JBJGLTextMenuElement generateTextMenuSubtitle(final String subtitle) {
        final int width = TechnicalSettings.getWidth();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLTextMenuElement.generate(
                new int[] { width / 2, MENU_SUBTITLE_Y },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                JBJGLTextBuilder.initialize(
                        pixel / 2, JBJGLText.Orientation.CENTER,
                        TLColors.MENU_TEXT(), Fonts.GAME_ITALICS_SPACED()
                ).addText(subtitle).build()
        );
    }

    private static JBJGLToggleClickableMenuElement generateTextToggleButton(
            final String[] headings, final int[] position,
            final Runnable[] behaviours, final Callable<Integer> updateIndexLogic,
            final int width
    ) {
        final int amount = headings.length;

        final JBJGLImage[] nonHighlightedButtons = new JBJGLImage[amount];
        final JBJGLImage[] highlightedButtons = new JBJGLImage[amount];

        for (int i = 0; i < amount; i++) {
            nonHighlightedButtons[i] =
                    drawNonHighlightedTextButton(width, headings[i]);
            highlightedButtons[i] =
                    drawHighlightedTextButton(width, headings[i]);
        }

        return JBJGLToggleClickableMenuElement.generate(
                position, new int[] { width, nonHighlightedButtons[0].getHeight() },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                nonHighlightedButtons, highlightedButtons,
                behaviours, updateIndexLogic);
    }

    private static JBJGLMenuElement determineTextButton(
            final String heading, final int[] position,
            final JBJGLMenuElement.Anchor anchor, final int buttonWidth,
            final Runnable behaviour
    ) {
        final boolean hasBehaviour = behaviour != null;

        if (hasBehaviour)
            return generateTextButton(heading, position, anchor, buttonWidth, behaviour);

        return generateTextButtonStub(heading, position, anchor, buttonWidth);
    }

    private static JBJGLClickableMenuElement generateTextButton(
            final String heading, final int[] position,
            final JBJGLMenuElement.Anchor anchor,
            final int buttonWidth, final Runnable behaviour
    ) {
        JBJGLImage nonHighlightedButton =
                drawNonHighlightedTextButton(buttonWidth, heading);
        JBJGLImage highlightedButton =
                drawHighlightedTextButton(buttonWidth, heading);

        return JBJGLClickableMenuElement.generate(
                position, new int[] { buttonWidth, nonHighlightedButton.getHeight() },
                anchor, nonHighlightedButton, highlightedButton, behaviour);
    }

    private static JBJGLStaticMenuElement generateTextButtonStub(
            final String heading, final int[] position,
            final JBJGLMenuElement.Anchor anchor, final int buttonWidth
    ) {
        JBJGLImage buttonStub =
                drawTextButton(buttonWidth, heading, TLColors.BLACK());

        return JBJGLStaticMenuElement.generate(position, anchor, buttonStub);
    }

    // level 3

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

    private static JBJGLTextBuilder generateInitialMenuTextBuilder(
            final int textSize, final JBJGLText.Orientation orientation
    ) {
        return JBJGLTextBuilder.initialize(
                textSize, orientation,
                TLColors.MENU_TEXT(), Fonts.GAME_STANDARD());
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder(final int textSize) {
        return generateInitialMenuTextBuilder(textSize, JBJGLText.Orientation.LEFT);
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder() {
        return generateInitialMenuTextBuilder(1, JBJGLText.Orientation.LEFT);
    }

    private static JBJGLText[] prependElementToArray(
            final JBJGLText element, final JBJGLText[] array
    ) {
        JBJGLText[] replacement = new JBJGLText[array.length + 1];

        replacement[0] = element;
        System.arraycopy(array, 0, replacement, 1, replacement.length - 1);

        return replacement;
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

    private static JBJGLImage drawNonHighlightedTextButton(
            final int width, final String label
    ) {
        return drawNonHighlightedTextButton(width,
                label, TechnicalSettings.getPixelSize() / 2);
    }

    private static JBJGLImage drawNonHighlightedTextButton(
            final int width, final String label, final int textSize
    ) {
        final Color nonHighlightedColor = TLColors.PLAYER();

        return drawTextButton(width, label, nonHighlightedColor, textSize);
    }

    private static JBJGLImage drawHighlightedTextButton(
            final int width, final String label
    ) {
        return drawHighlightedTextButton(width, label,
                TechnicalSettings.getPixelSize() / 2);
    }

    private static JBJGLImage drawHighlightedTextButton(
            final int width, final String label, final int textSize
    ) {
        return drawHighlightedTextButton(width, label, textSize, TLColors.PLAYER());
    }

    private static JBJGLImage drawHighlightedTextButton(
            final int width, final String label, final int textSize,
            final Color backgroundColor
    ) {
        final Color overlayColor = TLColors.BLACK();

        final int pixel = TechnicalSettings.getPixelSize();
        final int MARGIN_X = pixel * 2, MARGIN_Y = pixel * 2;

        final JBJGLImage overlay = drawTextButton(
                width, label, overlayColor, textSize);

        final int imageWidth = overlay.getWidth(),
                imageHeight = overlay.getHeight();

        JBJGLImage highlightedButton = JBJGLImage.create(imageWidth, imageHeight);
        Graphics hbg = highlightedButton.getGraphics();

        hbg.setColor(backgroundColor);
        hbg.fillRect(MARGIN_X, MARGIN_Y,
                imageWidth - (2 * MARGIN_X), imageHeight - (2 * MARGIN_Y));
        hbg.drawImage(overlay, 0, 0, null);

        return highlightedButton;
    }

    private static JBJGLImage drawTextButton(
            final int width, final String label,
            final Color color, final int textSize
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

    private static JBJGLImage drawTextButton(
            final int width, final String label, final Color color
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        return drawTextButton(width, label, color, pixel / 2);
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

    // MATHS HELPERS

    public static int widthCoord(final double fraction) {
        return coordinateFromFraction(TechnicalSettings.getWidth(), fraction);
    }

    public static int heightCoord(final double fraction) {
        return coordinateFromFraction(TechnicalSettings.getHeight(), fraction);
    }

    public static int coordinateFromFraction(final int dimension, final double fraction) {
        if (fraction < 0.)
            return 0;
        else if (fraction > 1.)
            return dimension;
        else
            return (int)(dimension * fraction);
    }

    private static int calculatePreviousNextTotal(
            final boolean hasPreviousPage, final boolean hasNextPage
    ) {
        return ((hasPreviousPage && hasNextPage)
                ? 2
                : ((hasPreviousPage || hasNextPage)
                ? 1 : 0));
    }
}

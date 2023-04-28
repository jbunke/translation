package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.events.JBJGLKey;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.*;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.jbjgl.text.JBJGLTextComponent;
import com.jordanbunke.translation.ResourceManager;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.editor.Editor;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.io.PatchNotes;
import com.jordanbunke.translation.menus.custom_elements.ConditionalMenuElement;
import com.jordanbunke.translation.menus.custom_elements.SetInputMenuElement;
import com.jordanbunke.translation.menus.custom_elements.TypedInputMenuElement;
import com.jordanbunke.translation.menus.custom_elements.VerticalScrollableMenuElement;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class MenuHelper {

    // Positioning
    private static final int
            LIST_MENU_INITIAL_Y = 140,
            MENU_TITLE_Y = 0,
            MENU_SUBTITLE_Y = MENU_TITLE_Y + (3 * 37),
            MARGIN = 20;

    public static final String DOES_NOT_EXIST = "!does-not-exist!";

    private enum Context {
        MY_LEVELS(true),
        MY_CAMPAIGNS(true),
        IMPORTED_CAMPAIGNS(false),
        STANDARD(false);

        final boolean levelsCanBeDeleted;

        Context(final boolean levelsCanBeDeleted) {
            this.levelsCanBeDeleted = levelsCanBeDeleted;
        }

        String getDeleteHeading() {
            return switch (this) {
                case MY_CAMPAIGNS -> "REMOVE";
                case MY_LEVELS -> "DELETE";
                default -> "";
            };
        }
    }

    // MENU LINKING

    public static void linkMenu(final String menuID, final JBJGLMenu menu, final boolean setActive) {
        getMenuManager().addMenu(menuID, menu, setActive);
    }

    public static void linkMenu(final String menuID, final JBJGLMenu menu) {
        linkMenu(menuID, menu, true);
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

    public static JBJGLMenu generateAreYouSureMenu(
            final boolean initialQuestionMark,
            final String decisionDescription,
            final Runnable noBehaviour,
            final Runnable yesBehaviour
    ) {
        final int buttonWidth = widthCoord(1/6.);
        final int leftX = widthCoord(0.5) - widthCoord(0.125);
        final int rightX = widthCoord(0.5) + widthCoord(0.125);
        final int promptY = heightCoord(1/3.);
        final int buttonsY = heightCoord(3/5.);

        final String firstLine = "ARE YOU SURE" + (initialQuestionMark ? "?" : "");

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(
                        new int[] { widthCoord(0.5), promptY },
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                        2., JBJGLText.Orientation.CENTER,
                                        TLColors.MENU_TEXT(), Fonts.GAME_ITALICS_SPACED()
                                ).addText(firstLine).addLineBreak()
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
            final Level level, final int index,
            final Campaign campaign, final Context context
    ) {
        final String PLAY_HEADING = "PLAY", TEMPLATE_HEADING = "USE AS TEMPLATE",
                DELETE_HEADING = context.getDeleteHeading();
        final Runnable playBehaviour = () -> {
            Translation.campaign.setLevel(index);
            Translation.campaign.getLevel().getStats().reset();
            Translation.campaign.getLevel().launchLevel();
            Translation.manager.setActiveStateIndex(Translation.GAMEPLAY_INDEX);
        }, templateBehaviour = () -> {
            Editor.setFromLevel(level);
            Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX);
        }, deleteBehaviour = () -> linkMenu(MenuIDs.ARE_YOU_SURE_DELETE_LEVEL,
                generateAreYouSureDeleteLevelMenu(campaign, level, context));

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                generateHorizontalListMenuOptions(
                        context.levelsCanBeDeleted
                                ? new String[] { PLAY_HEADING, TEMPLATE_HEADING, DELETE_HEADING }
                                : new String[] { PLAY_HEADING, TEMPLATE_HEADING },
                        context.levelsCanBeDeleted
                                ? new Runnable[] { playBehaviour, templateBehaviour, deleteBehaviour }
                                : new Runnable[] { playBehaviour, templateBehaviour },
                        heightCoord(0.85), widthCoord(context.levelsCanBeDeleted ? 0.25 : 0.4)),
                JBJGLTextMenuElement.generate(
                        new int[] { widthCoord(0.5), heightCoord(0.35) }, JBJGLMenuElement.Anchor.CENTRAL,
                        generateInitialMenuTextBuilder().addText((level.isBlind() ? "BLIND" : "SIGHTED") + " & " +
                                        (level.isDeterministic() ? "DETERMINISTIC" : "NON-DETERMINISTIC")).build()),
                generateLevelKeyInfo(level),
                generatePrevNextLevelButtons(index, campaign, context));

        final String title = (context == Context.MY_LEVELS ? "" : (index + 1) + ". ") + level.getName();

        return generateBasicMenu(title, level.getParsedHint(), contents, MenuIDs.CAMPAIGN_LEVELS);
    }

    private static JBJGLMenu generateAreYouSureDeleteCampaignMenu(
            final Campaign campaign, final Context context
    ) {
        return generateAreYouSureMenu(false,
                "you want to delete this campaign?",
                () -> linkMenu(MenuIDs.CAMPAIGN_LEVELS),
                () -> {
                    LevelIO.deleteCampaign(campaign,
                            context == Context.MY_CAMPAIGNS
                                    ? LevelIO.MY_CAMPAIGNS_FOLDER
                                    : LevelIO.IMPORTED_CAMPAIGNS_FOLDER);

                    linkMenu(MenuIDs.CAMPAIGN_FOLDER,
                            context == Context.MY_CAMPAIGNS
                                    ? generateMyCampaignsFolderMenu()
                                    : generateImportedCampaignsFolderMenu());
                });
    }

    private static JBJGLMenu generateAreYouSureDeleteLevelMenu(
            final Campaign campaign, final Level level, final Context context
    ) {
        final int LEVEL_NAME_TOO_LONG = 23;

        return generateAreYouSureMenu(false, "you want to " +
                        context.getDeleteHeading().toLowerCase() +  " \"" +
                        Utility.cutOffIfLongerThan(level.getName(), LEVEL_NAME_TOO_LONG) + "\"?",
                () -> linkMenu(MenuIDs.LEVEL_OVERVIEW),
                () -> {
                    campaign.removeLevel(level);
                    LevelIO.writeCampaign(campaign, false);

                    LevelIO.deleteLevelFile(level);

                    linkMenu(MenuIDs.CAMPAIGN_LEVELS,
                            generateMenuForCampaign(campaign, context == Context.MY_LEVELS
                                    ? MenuIDs.MY_CONTENT_MENU
                                    : MenuIDs.CAMPAIGN_FOLDER, context));
                });
    }

    public static JBJGLMenu generatePlayerMovementTypeMenu(
            final String heading, final String subtitle, final String text
    ) {
        return generateBasicMenu(heading, subtitle.toUpperCase(),
                JBJGLMenuElementGrouping.generateOf(
                        generateMenuTextBlurb(text, JBJGLText.Orientation.CENTER,
                                JBJGLMenuElement.Anchor.CENTRAL,
                                widthCoord(0.5), heightCoord(0.6), 2)),
                MenuIDs.MOVEMENT_RULES_GM);
    }

    private static JBJGLMenu generateSentryRoleWikiPage(final Sentry.Role role) {
        final double METADATA_HEIGHT_FRACTION = 0.35;
        final int pixel = TechnicalSettings.getPixelSize();

        final Path sentryDescriptionFilepath = Path.of("sentries", "descriptions", role.name().toLowerCase() + ".txt");
        final String sentryDescription = ResourceManager.getTextResource(sentryDescriptionFilepath);

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                // sentry
                JBJGLStaticMenuElement.generate(
                        new int[] {
                                widthCoord(0.5),
                                heightCoord(METADATA_HEIGHT_FRACTION)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        ImageAssets.drawSentry(role)),
                // metadata
                JBJGLTextMenuElement.generate(
                        new int[] {
                                widthCoord(0.48),
                                heightCoord(METADATA_HEIGHT_FRACTION) - pixel
                        }, JBJGLMenuElement.Anchor.RIGHT_TOP,
                        generateInitialMenuTextBuilder().addText(
                                role.isSightDependent()
                                        ? "RELIES ON SIGHT"
                                        : "DOES NOT RELY ON SIGHT").build()),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                widthCoord(0.52) + pixel,
                                heightCoord(METADATA_HEIGHT_FRACTION) - pixel
                        }, JBJGLMenuElement.Anchor.LEFT_TOP,
                        generateInitialMenuTextBuilder().addText(
                                role.isDeterministic()
                                        ? "DETERMINISTIC BEHAVIOUR"
                                        : "NON-DETERMINISTIC BEHAVIOUR"
                        ).build()),
                // description
                generateMenuTextBlurb(sentryDescription,
                        JBJGLText.Orientation.CENTER,
                        JBJGLMenuElement.Anchor.CENTRAL,
                        widthCoord(0.5), heightCoord(0.68), 2));

        return generateBasicMenu(role.name(),
                "SENTRY TYPE", contents, MenuIDs.SENTRIES_GM);
    }

    public static JBJGLMenu generatePatchNotesPage(
            final PatchNotes[] patchNotes, final String backMenuID, final int page
    ) {
        final JBJGLMenuElementGrouping contents =
                generatePatchNotesContent(patchNotes, backMenuID, page);

        return generateBasicMenu("Patch Notes",
                patchNotes[page].getVersion(), contents, backMenuID);
    }

    private static JBJGLMenu generateNewCampaignMenu() {
        final TypedInputMenuElement setCampaignNameButton =
                generateTypedInputButton(widthCoord(0.5), heightCoord(0.5),
                        widthCoord(0.8), "SET CAMPAIGN NAME", "",
                        Set.of("", "My Levels"), 80);

        final Runnable createCampaignButtonBehaviour = () -> {
            final Campaign campaign = LevelIO.createAndSaveNewCampaign(
                    setCampaignNameButton.getInput());

            linkMenu(MenuIDs.CAMPAIGN_FOLDER, generateMyCampaignsFolderMenu(), false);
            linkMenu(MenuIDs.CAMPAIGN_LEVELS, generateMenuForCampaign(campaign,
                            MenuIDs.CAMPAIGN_FOLDER, Context.MY_CAMPAIGNS));
        };

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                setCampaignNameButton, generateConditionalButton("CREATE CAMPAIGN",
                        widthCoord(0.5), heightCoord(0.8),
                        widthCoord(0.3), JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        createCampaignButtonBehaviour, setCampaignNameButton::inputIsValid));

        return generateBasicMenu("Create a New Campaign", DOES_NOT_EXIST,
                contents, MenuIDs.CAMPAIGN_FOLDER);
    }

    public static JBJGLMenu generateMyCampaignsFolderMenu() {
        return generateCampaignFolderMenu("My Campaigns", LevelIO.MY_CAMPAIGNS_FOLDER,
                MenuIDs.MY_CONTENT_MENU, Context.MY_CAMPAIGNS);
    }

    public static JBJGLMenu generateImportedCampaignsFolderMenu() {
        return generateCampaignFolderMenu("Imported Campaigns",
                LevelIO.IMPORTED_CAMPAIGNS_FOLDER, MenuIDs.PLAY_MENU, Context.IMPORTED_CAMPAIGNS);
    }

    public static JBJGLMenu generateMainCampaignsFolderMenu() {
        return generateCampaignFolderMenu("Main Campaigns",
                LevelIO.MAIN_CAMPAIGNS_FOLDER, MenuIDs.PLAY_MENU, Context.STANDARD);
    }

    private static JBJGLMenu generateCampaignFolderMenu(
            final String title, final Path folder,
            final String backMenuID, final Context context
    ) {
        final Campaign[] campaigns = LevelIO.readCampaignsInFolder(folder);

        final Runnable newButtonBehaviour = () -> linkMenu(MenuIDs.NEW_CAMPAIGN, generateNewCampaignMenu());
        final Runnable importButtonBehaviour = () -> {
            final Optional<File> toImport = JBJGLFileIO.openFileFromSystem(
                    new String[] { "[ Select any level file in the desired campaign folder ]" },
                    new String[][] { new String[] { LevelIO.LEVEL_FILE_SUFFIX } });

            if (toImport.isEmpty()) return;

            final Path importFromFolder = toImport.get().toPath().getParent();
            final Optional<Campaign> potentialImportedCampaign = LevelIO.tryReadImportedCampaign(importFromFolder);

            if (potentialImportedCampaign.isPresent()) {
                final Campaign importedCampaign = potentialImportedCampaign.get();

                LevelIO.saveImportedCampaign(importedCampaign);
                linkMenu(MenuIDs.CAMPAIGN_FOLDER, generateImportedCampaignsFolderMenu());
            } else {
                linkMenu(MenuIDs.INVALID_IMPORT, generateInvalidImportMenu());
            }
        };

        final JBJGLMenuElement additional = switch (context) {
            case IMPORTED_CAMPAIGNS -> generateTopRightButton("IMPORT +",
                    widthCoord(0.175), importButtonBehaviour);
            case MY_CAMPAIGNS -> generateTopRightButton("NEW +",
                    widthCoord(0.125), newButtonBehaviour);
            default -> JBJGLPlaceholderMenuElement.generate();
        };

        final JBJGLMenuElementGrouping contents =
                JBJGLMenuElementGrouping.generateOf(
                        generateCampaignsOnPage(campaigns, context), additional);

        return generateBasicMenu(title, DOES_NOT_EXIST, contents, backMenuID);
    }

    private static JBJGLMenu generateInvalidImportMenu() {
        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                generateMenuTextBlurb(
                        new String[] {
                                "The folder you attempted to import",
                                "is not a valid campaign folder."
                        },
                        JBJGLText.Orientation.CENTER, JBJGLMenuElement.Anchor.CENTRAL,
                        widthCoord(0.5), heightCoord(0.5),
                        TechnicalSettings.getPixelSize() / 2.),
                determineTextButton(
                        "UNDERSTOOD", new int[] { widthCoord(0.5), heightCoord(0.8) },
                        JBJGLMenuElement.Anchor.CENTRAL, widthCoord(0.3),
                        () -> linkMenu(MenuIDs.CAMPAIGN_FOLDER))
        );

        return generatePlainMenu(contents);
    }

    public static JBJGLMenu generateMenuForMyLevels() {
        return generateMenuForCampaign(LevelIO.readMyLevels(),
                MenuIDs.MY_CONTENT_MENU, Context.MY_LEVELS);
    }

    public static JBJGLMenu generateMenuForTutorial() {
        return generateMenuForCampaign(LevelIO.readCampaign(LevelIO.TUTORIAL_CAMPAIGN_FOLDER),
                MenuIDs.PLAY_MENU, Context.STANDARD);
    }

    private static JBJGLMenu generateMenuForCampaign(
            final Campaign campaign, final String backMenuID, final Context context
    ) {
        Translation.campaign = campaign;

        final int belowButtonY = heightCoord(0.85);

        final Runnable addLevelBehaviour = LevelIO.readMyLevels().getLevelCount() > 0 ?
                () -> linkMenu(MenuIDs.LEVEL_SELECT, generateLevelSelectMenu(campaign, context))
                : null;
        final Runnable deleteBehaviour = () ->
                linkMenu(MenuIDs.ARE_YOU_SURE_DELETE_CAMPAIGN,
                        generateAreYouSureDeleteCampaignMenu(campaign, context));
        final Runnable newLevelBehaviour = () -> Translation.manager.setActiveStateIndex(Translation.EDITOR_INDEX);

        final JBJGLMenuElement additional = switch (context) {
            case MY_CAMPAIGNS -> generateHorizontalListMenuOptions(
                    new String[] { "ADD LEVEL", "DELETE" },
                    new Runnable[] { addLevelBehaviour, deleteBehaviour },
                    belowButtonY, widthCoord(0.3));
            case IMPORTED_CAMPAIGNS -> generateHorizontalListMenuOptions(
                    new String[] { "DELETE" }, new Runnable[] { deleteBehaviour },
                    belowButtonY, widthCoord(0.5));
            case MY_LEVELS -> generateTopRightButton("NEW +",
                    widthCoord(0.125), newLevelBehaviour);
            default -> JBJGLPlaceholderMenuElement.generate();
        };

        final JBJGLMenuElementGrouping contents = JBJGLMenuElementGrouping.generateOf(
                generateLevelsOnPage(campaign, context), additional);

        final String
                subtitleA = (context == Context.MY_LEVELS ? "" : campaign.getLevelsBeaten() + " / ") + campaign.getLevelCount(),
                subtitleB = " level" + (campaign.getLevelCount() == 1 ? " " : "s "),
                subtitleC = context == Context.MY_LEVELS ? "created" : "beaten";

        return generateBasicMenu(campaign.getName(),
                subtitleA + subtitleB + subtitleC, contents, backMenuID);
    }

    private static JBJGLMenu generateLevelSelectMenu(
            final Campaign campaign, final Context context
    ) {
        final JBJGLMenuElementGrouping contents = generateMyLevelsToSelectFrom(campaign, context);

        return generateBasicMenu("Level Select", "Select a level to add to \"" +
                campaign.getName() + "\"...", contents, MenuIDs.CAMPAIGN_LEVELS);
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
            final Path path = folder.resolve(filename);

            frames[paddingFrameCount + i] = ResourceManager.getImageResource(path);
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

        return JBJGLMenuElementGrouping.generateOf(
                JBJGLTextMenuElement.generate(new int[] { x, y },
                        anchor, JBJGLText.createOf(
                                pixel, JBJGLText.Orientation.CENTER,
                                JBJGLTextComponent.add(
                                        Translation.TITLE.toUpperCase(),
                                        Fonts.VIGILANT_ITALICS(),
                                        TLColors.BLACK()))),
                JBJGLTextMenuElement.generate(new int[] { x - (pixel * 2), y },
                        anchor, JBJGLText.createOf(
                                pixel, JBJGLText.Orientation.CENTER,
                                JBJGLTextComponent.add(
                                        Translation.TITLE.toUpperCase(),
                                        Fonts.VIGILANT_ITALICS(),
                                        TLColors.TITLE_RED()))));
    }

    public static JBJGLTextMenuElement generateDevelopmentInformation() {
        final int height = TechnicalSettings.getHeight();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLTextMenuElement.generate(
                new int[] { pixel, height }, JBJGLMenuElement.Anchor.LEFT_BOTTOM,
                generateInitialMenuTextBuilder()
                        .addText("version " + Translation.VERSION).addLineBreak()
                        .addText("Jordan Bunke, 2022-2023").build());
    }

    public static JBJGLTextMenuElement generateMenuTextBlurb(
            final String[] lines, JBJGLText.Orientation orientation,
            final JBJGLMenuElement.Anchor anchor,
            final int x, final int y, final double textSize
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
            final int x, final int y, final double textSize
    ) {
        final String NEW_LINE = "\n";
        return generateMenuTextBlurb(text.split(NEW_LINE),
                orientation, anchor, x, y, textSize);
    }

    public static JBJGLMenuElementGrouping generateListMenuToggleOptions(
            final String[] associatedTexts, final String[][] buttonHeadings,
            final Runnable[][] behaviours, final Callable<Integer>[] updateIndexLogic,
            final double offsetByNRows
    ) {
        final int offsetY = (int)(offsetByNRows * listMenuIncrementY());

        final int width = TechnicalSettings.getWidth();
        final int pixel = TechnicalSettings.getPixelSize();
        final int amount = associatedTexts.length;

        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[amount * 2];
        final int associatedX = widthCoord(0.3);
        final int buttonX = widthCoord(0.7);
        int drawY = LIST_MENU_INITIAL_Y + offsetY;
        final double textSize = pixel / 2.;

        for (int i = 0; i < amount; i++) {
            final JBJGLTextMenuElement associatedText = JBJGLTextMenuElement.generate(
                    new int[] { associatedX, drawY + (int)(textSize * 2) },
                    JBJGLMenuElement.Anchor.CENTRAL_TOP,
                    generateInitialMenuTextBuilder(textSize).addText(associatedTexts[i]).build()
            );
            final JBJGLToggleClickableMenuElement button = generateTextToggleButton(
                    buttonHeadings[i], new int[] { buttonX, drawY }, behaviours[i],
                    updateIndexLogic[i], TechnicalSettings.pixelLockNumber(width / 4));

            menuElements[i * 2] = associatedText;
            menuElements[(i * 2) + 1] = button;
            drawY += listMenuIncrementY();
        }

        return JBJGLMenuElementGrouping.generate(menuElements);
    }

    public static JBJGLMenuElementGrouping generateHorizontalListMenuOptions(
            final String[] headings, final Runnable[] behaviours,
            final int y, final int widthPerButton
    ) {
        final int BUTTON_X_MARGIN = MARGIN;

        final int amount = headings.length;
        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[amount];

        final int centerElement = amount / 2,
                centerElementX = widthCoord(0.5) + (amount % 2 == 0
                // even
                ? (BUTTON_X_MARGIN + widthPerButton) / 2
                // odd
                : 0);

        for (int i = 0; i < amount; i++) {
            final int x = centerElementX - ((centerElement - i) * (widthPerButton + BUTTON_X_MARGIN));

            final JBJGLMenuElement button = determineTextButton(
                    headings[i], new int[] { x, y }, JBJGLMenuElement.Anchor.CENTRAL,
                    TechnicalSettings.pixelLockNumber(widthPerButton), behaviours[i]);
            menuElements[i] = button;
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
            drawY += listMenuIncrementY();
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
                width / 2, (int)(offsetByNButtons * listMenuIncrementY()),
                JBJGLMenuElement.Anchor.CENTRAL_TOP);
    }

    public static JBJGLMenuElementGrouping generateSentryButtons() {
        final int COLUMNS = 4, INITIAL_Y = LIST_MENU_INITIAL_Y + (listMenuIncrementY() / 2);
        final Sentry.Role[] roles = Sentry.Role.values();
        final JBJGLClickableMenuElement[] menuElements = new JBJGLClickableMenuElement[roles.length];

        for (int i = 0; i < roles.length; i++) {
            final int column = i % COLUMNS;
            final int row = i / COLUMNS;
            final int x = widthCoord((column + 1) / (double)(COLUMNS + 1)),
                    y = INITIAL_Y + (row * listMenuIncrementY());
            menuElements[i] = generateSentryButton(x, y, roles[i],
                    widthCoord(1 / (COLUMNS + 1.3)));
        }

        return JBJGLMenuElementGrouping.generateOf(
                JBJGLMenuElementGrouping.generate(menuElements));
    }

    public static JBJGLMenuElementGrouping generateControlsButtons() {
        final int offsetY = (int)(listMenuIncrementY() * 1.5);
        final int COLUMNS = 4, CONTROL_AMOUNT = 15, textSize = TechnicalSettings.getPixelSize() / 4;
        final JBJGLMenuElement[] menuElements = new JBJGLMenuElement[CONTROL_AMOUNT * 2];

        final String[] associatedTexts = new String[] {
                "MOVE LEFT", "MOVE RIGHT", "JUMP", "DROP",
                "TELEPORT", "SAVE / * CREATE PLATFORM", "LOAD / * DELETE PLATFORM",
                "CAMERA LEFT", "CAMERA RIGHT", "CAMERA UP", "CAMERA DOWN",
                "TOGGLE ZOOM", "TOGGLE FOLLOW (* EDIT) MODE", "PAUSE", "* SNAP TO GRID"
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
                key -> ControlScheme.update(ControlScheme.Action.PAUSE, key),
                key -> ControlScheme.update(ControlScheme.Action.SNAP_TO_GRID, key)
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
                ControlScheme.Action.PAUSE,
                ControlScheme.Action.SNAP_TO_GRID
        };

        final int buttonWidth = widthCoord(2 / (double)(3 * (COLUMNS + 1)));
        final JBJGLImage setButton =
                drawNonHighlightedTextButton(buttonWidth, "SET",
                        TechnicalSettings.getPixelSize() / 4.);
        final JBJGLImage setHighlightedButton =
                drawHighlightedTextButton(buttonWidth, "SET",
                        TechnicalSettings.getPixelSize() / 4.);

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
            final int y = LIST_MENU_INITIAL_Y + offsetY + (row * listMenuIncrementY());

            menuElements[i * 2] = JBJGLTextMenuElement.generate(
                    new int[] { x, y + (textSize * 2) },
                    JBJGLMenuElement.Anchor.CENTRAL_TOP,
                    generateInitialMenuTextBuilder(textSize).addText(associatedTexts[i]).build()
            );
            menuElements[(i * 2) + 1] = generateControlButton(
                    new int[] { x, y + (int)(listMenuIncrementY() * 0.3) },
                    buttonWidth, setFunctions.get(i), actions[i],
                    setButton, setHighlightedButton
            );
        }

        return JBJGLMenuElementGrouping.generate(menuElements);
    }

    public static JBJGLMenuElementGrouping generateLevelStatsText(final LevelStats levelStats) {
        final int fieldsX = widthCoord(0.1);
        final int thisRunX = widthCoord(0.5);
        final int pbX = widthCoord(0.8);
        final int y = heightCoord(0.38);

        return JBJGLMenuElementGrouping.generateOf(
                generateLevelStatFields(fieldsX, y, true, 2.),
                generateThisRunStats(thisRunX, y, true, 2., levelStats),
                generatePBs(pbX, y, true, 2, levelStats));
    }

    private static JBJGLMenuElementGrouping generatePatchNotesContent(
            final PatchNotes[] patchNotes, final String backMenuID, final int page
    ) {
        final int CONTENTS_BELOW_DATE = 50;
        final int DATE_INDEX = 0, CONTENT_INDEX = 1, CONTENTS_BESIDES_PREV_NEXT = 2;

        final boolean hasPreviousPage = page > 0;
        final boolean hasNextPage = page + 1 < patchNotes.length;

        final int menuElementsCount = CONTENTS_BESIDES_PREV_NEXT +
                calculatePreviousNextTotal(hasPreviousPage, hasNextPage);

        final JBJGLMenuElement[] content = new JBJGLMenuElement[menuElementsCount];

        content[DATE_INDEX] =
                generateMenuTextBlurb(
                        patchNotes[page].getDate(),
                        JBJGLText.Orientation.CENTER,
                        JBJGLMenuElement.Anchor.CENTRAL,
                        widthCoord(0.5), heightCoord(0.3), 2);
        content[CONTENT_INDEX] =
                generateMenuTextBlurb(
                        patchNotes[page].getContents(),
                        JBJGLText.Orientation.LEFT,
                        JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        widthCoord(0.5), heightCoord(0.3) + CONTENTS_BELOW_DATE, 2);

        // generate previous and next buttons, if they exist
        populatePreviousAndNext(content, hasPreviousPage, hasNextPage,
                () -> linkMenu(MenuIDs.PATCH_NOTES, generatePatchNotesPage(patchNotes, backMenuID, page - 1)),
                () -> linkMenu(MenuIDs.PATCH_NOTES, generatePatchNotesPage(patchNotes, backMenuID, page + 1)));

        return JBJGLMenuElementGrouping.generate(content);
    }

    private static JBJGLMenuElementGrouping generateCampaignsOnPage(
            final Campaign[] campaigns, final Context context
    ) {
        final int CAMPAIGN_NAME_TOO_LONG = 50;

        final int campaignCount = campaigns.length;

        if (campaignCount == 0) return JBJGLMenuElementGrouping.generateOf(
                generateMenuTextBlurb(
                        "This folder does not contain any campaigns.",
                        JBJGLText.Orientation.CENTER, JBJGLMenuElement.Anchor.CENTRAL,
                        widthCoord(0.5), heightCoord(0.5),
                        TechnicalSettings.getPixelSize() / 2.));

        final String[] headings = new String[campaignCount];
        final Runnable[] behaviours = new Runnable[campaignCount];

        for (int i = 0; i < campaignCount; i++) {
            final Campaign campaign = campaigns[i];

            final Runnable behaviour = () -> linkMenu(MenuIDs.CAMPAIGN_LEVELS,
                    generateMenuForCampaign(campaign, MenuIDs.CAMPAIGN_FOLDER, context));

            headings[i] = Utility.cutOffIfLongerThan(campaign.getName(), CAMPAIGN_NAME_TOO_LONG).toUpperCase();
            behaviours[i] = behaviour;
        }

        return JBJGLMenuElementGrouping.generateOf(
                VerticalScrollableMenuElement.generate(headings, behaviours,
                        widthCoord(0.5), heightCoord(0.25),
                        widthCoord(0.7), heightCoord(0.6)));
    }

    public static JBJGLMenuElementGrouping generateLevelsOnPage(
            final Campaign campaign, final Context context
    ) {
        final int LEVEL_NAME_TOO_LONG = 50;
        final int levelCount = campaign.getLevelCount();

        if (levelCount == 0) return JBJGLMenuElementGrouping.generateOf(
                generateMenuTextBlurb(
                        "This campaign does not contain any levels.",
                        JBJGLText.Orientation.CENTER, JBJGLMenuElement.Anchor.CENTRAL,
                        widthCoord(0.5), heightCoord(0.5),
                        TechnicalSettings.getPixelSize() / 2.));

        final String[] headings = new String[levelCount];
        final Runnable[] behaviours = new Runnable[levelCount];

        for (int i = 0; i < levelCount; i++) {
            final int index = i;
            final Level level = campaign.getLevelAt(i);
            final boolean isUnlocked = campaign.isUnlocked(i);
            final Runnable behaviour = isUnlocked
                    ? () -> linkMenu(MenuIDs.LEVEL_OVERVIEW,
                        generateLevelOverview(level, index, campaign, context))
                    : null;

            headings[i] = Utility.cutOffIfLongerThan(level.getName(),
                    LEVEL_NAME_TOO_LONG).toUpperCase();
            behaviours[i] = behaviour;
        }

        final boolean hasBelowButtons = context == Context.MY_CAMPAIGNS ||
                context == Context.IMPORTED_CAMPAIGNS;

        return JBJGLMenuElementGrouping.generateOf(
                VerticalScrollableMenuElement.generate(headings, behaviours,
                        widthCoord(0.5), heightCoord(0.3), widthCoord(0.7),
                        heightCoord(hasBelowButtons ? 0.4 : 0.5)));
    }

    private static JBJGLMenuElementGrouping generateMyLevelsToSelectFrom(
            final Campaign destinationCampaign, final Context context
    ) {
        final Campaign campaign = LevelIO.readMyLevels();

        final int LEVEL_NAME_TOO_LONG = 50;
        final int levelCount = campaign.getLevelCount();

        if (levelCount == 0) return JBJGLMenuElementGrouping.generateOf(
                generateMenuTextBlurb(
                        "This campaign does not contain any levels.",
                        JBJGLText.Orientation.CENTER, JBJGLMenuElement.Anchor.CENTRAL,
                        widthCoord(0.5), heightCoord(0.5),
                        TechnicalSettings.getPixelSize() / 2.));

        final String[] headings = new String[levelCount];
        final Runnable[] behaviours = new Runnable[levelCount];

        for (int i = 0; i < levelCount; i++) {
            final Level level = campaign.getLevelAt(i);
            final Runnable behaviour = () -> {
                destinationCampaign.addLevel(level,
                        LevelIO.generateLevelFilename(level.getName()), true);
                destinationCampaign.updateLevelFilenames();

                LevelIO.writeCampaign(destinationCampaign, false);
                LevelIO.writeLevel(level, true);

                linkMenu(MenuIDs.CAMPAIGN_LEVELS,
                        generateMenuForCampaign(destinationCampaign, MenuIDs.CAMPAIGN_FOLDER, context));
            };

            headings[i] = Utility.cutOffIfLongerThan(level.getName(),
                    LEVEL_NAME_TOO_LONG).toUpperCase();
            behaviours[i] = behaviour;
        }

        return JBJGLMenuElementGrouping.generateOf(
                VerticalScrollableMenuElement.generate(headings, behaviours,
                        widthCoord(0.5), heightCoord(0.3), widthCoord(0.7),
                        heightCoord(0.5)));
    }

    // ELEMENT GENERATORS

    // level 1

    private static JBJGLMenuElement generateBackButton(final String backMenuID) {
        return determineTextButton(
                "< BACK", new int[] { MARGIN, MARGIN },
                JBJGLMenuElement.Anchor.LEFT_TOP,
                widthCoord(0.125),
                () -> linkMenu(backMenuID));
    }

    private static JBJGLMenuElement generateTopRightButton(
            final String prompt, final int width, final Runnable behaviour
    ) {
        return determineTextButton(prompt, new int[] { widthCoord(1.0) - MARGIN, MARGIN },
                JBJGLMenuElement.Anchor.RIGHT_TOP, width, behaviour);
    }

    private static JBJGLMenuElementGrouping generatePrevNextLevelButtons(
            final int index, final Campaign campaign,
            final Context context
    ) {
        final boolean hasPreviousPage = index > 0;
        final boolean hasNextPage = index + 1 < campaign.getLevelCount() && campaign.getLevelsBeaten() > index;

        final int buttonsCount = calculatePreviousNextTotal(hasPreviousPage, hasNextPage);
        final JBJGLMenuElement[] prevNextButtons = new JBJGLMenuElement[buttonsCount];

        populatePreviousAndNext(prevNextButtons, hasPreviousPage, hasNextPage,
                () -> linkMenu(MenuIDs.LEVEL_OVERVIEW,
                        generateLevelOverview(campaign.getLevelAt(index - 1),
                                index - 1, campaign, context)),
                () -> linkMenu(MenuIDs.LEVEL_OVERVIEW,
                        generateLevelOverview(campaign.getLevelAt(index + 1),
                                index + 1, campaign, context)));

        return JBJGLMenuElementGrouping.generate(prevNextButtons);
    }

    private static void populatePreviousAndNext(
            final JBJGLMenuElement[] elements,
            final boolean hasPreviousPage, final boolean hasNextPage,
            final Runnable previous, final Runnable next
    ) {
        final int y = heightCoord(0.5), width = widthCoord(0.05);

        final JBJGLMenuElement previousPageButton = determineTextButton(
                "<", new int[] { MARGIN, y },
                JBJGLMenuElement.Anchor.LEFT_CENTRAL, width, previous);
        final JBJGLMenuElement nextPageButton = determineTextButton(
                ">", new int[] { widthCoord(1.0) - MARGIN, y },
                JBJGLMenuElement.Anchor.RIGHT_CENTRAL, width, next);

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
                drawNonHighlightedTextButton(buttonWidth, " ");
        final int width = nonHighlightedButton.getWidth(), height = nonHighlightedButton.getHeight();
        final JBJGLImage square = ImageAssets.drawSentry(role);
        final int squareX = (buttonWidth / 2) - (square.getWidth() / 2),
                squareY = (height - square.getHeight()) / 2;

        final Graphics nhbg = nonHighlightedButton.getGraphics();
        nhbg.drawImage(square, squareX, squareY, null);

        final JBJGLImage highlightedButton =
                drawHighlightedTextButton(buttonWidth, role.name(),
                TechnicalSettings.getPixelSize() / 2.,
                        role.getColor(TLColors.OPAQUE()));

        return JBJGLClickableMenuElement.generate(
                new int[] { x, y }, new int[] { width, height },
                JBJGLMenuElement.Anchor.CENTRAL_TOP,
                nonHighlightedButton, highlightedButton,
                () -> linkMenu(MenuIDs.SENTRY_ROLE_GM,
                        generateSentryRoleWikiPage(role)));
    }

    public static ConditionalMenuElement generateConditionalButton(
            final String label, final int x, final int y, final int width,
            final JBJGLMenuElement.Anchor anchor,
            final Runnable behaviour, final Callable<Boolean> condition
    ) {
        final JBJGLMenuElement trueElement = determineTextButton(label,
                new int[] { x, y }, anchor, width, behaviour);
        final JBJGLMenuElement falseElement = determineTextButton(label,
                new int[] { x, y }, anchor, width, null);

        return ConditionalMenuElement.generate(falseElement, trueElement, condition);
    }

    public static TypedInputMenuElement generateTypedInputButton(
            final int x, final int y, final int width,
            final String setPrompt, final String defaultInput,
            final Set<String> invalidInputs, final int maxLength
    ) {
        final JBJGLImage highlightedImage = drawHighlightedTextButton(width, setPrompt);
        final int height = highlightedImage.getHeight();

        final JBJGLImage highlightedBorder =
                drawHighlightedTextButton(width, "", TechnicalSettings.getPixelSize() / 2.);
        final JBJGLImage nonHighlightedBorder =
                drawNonHighlightedTextButton(width, "", TechnicalSettings.getPixelSize() / 2.);

        return TypedInputMenuElement.generate(
                new int[] { x, y }, new int[] { width, height },
                JBJGLMenuElement.Anchor.CENTRAL,
                highlightedBorder, nonHighlightedBorder,
                highlightedImage, defaultInput, invalidInputs, maxLength);
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
                TechnicalSettings.getPixelSize() / 4.);
        final Callable<JBJGLImage> hGeneratorFunction =
                () -> drawHighlightedTextButton(width,
                        Utility.cutOffIfLongerThan(
                                ControlScheme.getCorrespondingKey(action).print(), 16),
                        TechnicalSettings.getPixelSize() / 4.);

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
        final int topY = heightCoord(0.425);
        final int bottomYLow = heightCoord(0.55);
        final int bottomYHigh = heightCoord(0.475);

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
                                4., JBJGLText.Orientation.CENTER,
                                TLColors.MENU_TEXT(), Fonts.GAME_ITALICS_SPACED()
                        ).addText(number).build()),
                JBJGLTextMenuElement.generate(
                        new int[] { x, captionY }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                2., JBJGLText.Orientation.CENTER,
                                TLColors.MENU_TEXT(), Fonts.GAME_STANDARD()
                        ).addText(caption).build())
        );
    }

    // level 2

    private static JBJGLMenuElementGrouping generateLevelStatFields(
            final int x, final int y, final boolean withHeading, final double textSize
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
            final double textSize, final LevelStats levelStats
    ) {
        JBJGLText heading = generateInitialMenuTextBuilder(textSize)
                .addText("THIS RUN").build();
        JBJGLText[] body = new JBJGLText[] {
                generateInitialMenuTextBuilder(textSize)
                        .setColor(levelStats.getStatScreenColor(LevelStats.TIME))
                        .addText(levelStats.getFinalStat(LevelStats.TIME)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(levelStats.getStatScreenColor(LevelStats.FAILURES))
                        .addText(levelStats.getFinalStat(LevelStats.FAILURES)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(levelStats.getStatScreenColor(LevelStats.SIGHTINGS))
                        .addText(levelStats.getFinalStat(LevelStats.SIGHTINGS)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .setColor(levelStats.getStatScreenColor(LevelStats.MAX_COMBO))
                        .addText(levelStats.getFinalStat(LevelStats.MAX_COMBO)).build()
        };

        JBJGLText[] lines = withHeading ? prependElementToArray(heading, body) : body;
        return generateMenuTextLines(x, y, JBJGLMenuElement.Anchor.CENTRAL_TOP, lines);
    }

    private static JBJGLMenuElementGrouping generatePBs(
            final int x, final int y, final boolean withHeading,
            final double textSize, final LevelStats levelStats
    ) {
        JBJGLText heading = generateInitialMenuTextBuilder(textSize)
                .addText("PREVIOUS BESTS").build();
        JBJGLText[] body = new JBJGLText[] {
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPreviousPersonalBest(LevelStats.TIME)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPreviousPersonalBest(LevelStats.FAILURES)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPreviousPersonalBest(LevelStats.SIGHTINGS)).build(),
                generateInitialMenuTextBuilder(textSize)
                        .addText(levelStats.getPreviousPersonalBest(LevelStats.MAX_COMBO)).build()
        };

        JBJGLText[] lines = withHeading ? prependElementToArray(heading, body) : body;
        return generateMenuTextLines(x, y, JBJGLMenuElement.Anchor.CENTRAL_TOP, lines);
    }

    private static JBJGLTextMenuElement generateTextMenuTitle(final String title) {
        final int STARTING_Y = -23, INCREMENT = 5;
        final double THRESHOLD = 0.6, DECREMENT = 0.25;

        int offsetY = STARTING_Y;
        double textSize = TechnicalSettings.getPixelSize();
        JBJGLText titleText;

        do {
            titleText = JBJGLTextBuilder.initialize(textSize,
                    JBJGLText.Orientation.CENTER, TLColors.MENU_TEXT(),
                    Fonts.GAME_ITALICS_SPACED()).addText(title).build();

            textSize -= DECREMENT;
            offsetY += INCREMENT;
        } while (titleText.draw().getWidth() > widthCoord(THRESHOLD));

        return JBJGLTextMenuElement.generate(
                new int[] { widthCoord(0.5), MENU_TITLE_Y + offsetY },
                JBJGLMenuElement.Anchor.CENTRAL_TOP, titleText);
    }

    private static JBJGLTextMenuElement generateTextMenuSubtitle(final String subtitle) {
        final int INCREMENT = 2;
        final double THRESHOLD = 0.85, DECREMENT = 0.1;

        int offsetY = -INCREMENT;
        double textSize = TechnicalSettings.getPixelSize() / 2.;
        JBJGLText subtitleText;

        do {
            subtitleText = JBJGLTextBuilder.initialize(textSize,
                    JBJGLText.Orientation.CENTER, TLColors.MENU_TEXT(),
                    Fonts.GAME_ITALICS_SPACED()).addText(subtitle).build();

            textSize -= DECREMENT;
            offsetY += INCREMENT;
        } while (subtitleText.draw().getWidth() > widthCoord(THRESHOLD));

        return JBJGLTextMenuElement.generate(
                new int[] { widthCoord(0.5), MENU_SUBTITLE_Y + offsetY },
                JBJGLMenuElement.Anchor.CENTRAL_TOP, subtitleText);
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

    public static JBJGLMenuElement determineTextButton(
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
            final double textSize, final JBJGLText.Orientation orientation
    ) {
        return JBJGLTextBuilder.initialize(
                textSize, orientation,
                TLColors.MENU_TEXT(), Fonts.GAME_STANDARD());
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder(final double textSize) {
        return generateInitialMenuTextBuilder(textSize, JBJGLText.Orientation.LEFT);
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder() {
        return generateInitialMenuTextBuilder(1., JBJGLText.Orientation.LEFT);
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
            y += menuTextIncrementY();
        }

        return JBJGLMenuElementGrouping.generate(textMenuElements);
    }

    private static JBJGLImage drawNonHighlightedTextButton(
            final int width, final String label
    ) {
        return drawNonHighlightedTextButton(width,
                label, TechnicalSettings.getPixelSize() / 2.);
    }

    private static JBJGLImage drawNonHighlightedTextButton(
            final int width, final String label, final double textSize
    ) {
        final Color nonHighlightedColor = TLColors.PLAYER();

        return drawTextButton(width, label, nonHighlightedColor, textSize);
    }

    private static JBJGLImage drawHighlightedTextButton(
            final int width, final String label
    ) {
        return drawHighlightedTextButton(width, label,
                TechnicalSettings.getPixelSize() / 2.);
    }

    private static JBJGLImage drawHighlightedTextButton(
            final int width, final String label, final double textSize
    ) {
        return drawHighlightedTextButton(width, label, textSize, TLColors.PLAYER());
    }

    private static JBJGLImage drawHighlightedTextButton(
            final int width, final String label, final double textSize,
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
            final Color color, final double textSize
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        JBJGLImage text = JBJGLTextBuilder.initialize(
                textSize, JBJGLText.Orientation.CENTER,
                color, Fonts.GAME_STANDARD()
        ).addText(label).build().draw();

        final int height = text.getHeight();

        double adjustableTextSize = textSize;

        while (text.getWidth() + Math.max(pixel * 4, (int)(textSize * pixel * 2)) > width) {
            adjustableTextSize -= 0.1;
            text = JBJGLTextBuilder.initialize(
                    adjustableTextSize, JBJGLText.Orientation.CENTER,
                    color, Fonts.GAME_STANDARD()
            ).addText(label).build().draw();
        }

        final int x = (width - text.getWidth()) / 2, y = (int)(textSize * 2) +
                (int)(0.5 * (height - text.getHeight()));

        JBJGLImage nonHighlightedButton = JBJGLImage.create(width, height);
        drawButtonPixelBorder(nonHighlightedButton, color);
        Graphics nhbg = nonHighlightedButton.getGraphics();
        nhbg.drawImage(text, x, y, null);

        return nonHighlightedButton;
    }

    private static JBJGLImage drawTextButton(
            final int width, final String label, final Color color
    ) {
        final int pixel = TechnicalSettings.getPixelSize();

        return drawTextButton(width, label, color, pixel / 2.);
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

    public static int listMenuIncrementY() {
        return TechnicalSettings.getHeight() / 8;
    }

    private static int menuTextIncrementY() {
        return TechnicalSettings.getHeight() / 12;
    }

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

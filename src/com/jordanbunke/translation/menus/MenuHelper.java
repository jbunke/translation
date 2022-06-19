package com.jordanbunke.translation.menus;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLImageIO;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.*;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.jbjgl.text.JBJGLTextComponent;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.swatches.Swatches;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
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

    // PUBLICLY ACCESSIBLE

    // full menus

    public static void linkMenu(final String menuID, final JBJGLMenu menu) {
        // TODO - either true menu, pause menu, or level complete menu
        Translation.menuManager.addMenu(menuID, menu, true);
    }

    public static void linkMenu(final String menuID) {
        // TODO - either true menu, pause menu, or level complete menu
        Translation.menuManager.setActiveMenuID(menuID);
    }

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

    public static JBJGLMenu generateLevelOverview(
            final Level level, final int index, final Campaign campaign
    ) {
        final int width = TechnicalSettings.getWidth(), height = TechnicalSettings.getHeight();

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
                        width / 2,
                        height - (LIST_MENU_INITIAL_Y + (int)(1.5 * LIST_MENU_INCREMENT_Y)),
                        JBJGLMenuElement.Anchor.CENTRAL_TOP),
                JBJGLTextMenuElement.generate(
                        new int[] {
                                coordinateFromFraction(TechnicalSettings.getWidth(), 0.5),
                                LIST_MENU_INITIAL_Y + (int)(1.9 * LIST_MENU_INCREMENT_Y)
                        }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        generateInitialMenuTextBuilder().addText(
                                (level.isBlind() ? "BLIND" : "SIGHTED") + " & " +
                                        (level.isDeterministic() ? "DETERMINISTIC" : "NON-DETERMINISTIC")
                        ).build()),
                generateLevelKeyInfo(level),
                generatePrevNextLevelButtons(index, campaign));

        return generateBasicMenu(
                level.getName().toUpperCase(),
                level.getHint().toUpperCase(),
                contents, MenuIDs.CAMPAIGN_LEVELS);
    }

    public static JBJGLMenu generatePlayerMovementTypeMenu(
            final String heading, final String subtitle, final String[] lines
    ) {
        final int width = TechnicalSettings.getWidth(), height = TechnicalSettings.getHeight();

        return generateBasicMenu(heading, subtitle.toUpperCase(),
                JBJGLMenuElementGrouping.generateOf(
                        generateMenuTextBlurb(lines, JBJGLText.Orientation.CENTER,
                                width / 2, height / 2, 2)
                ),
                MenuIDs.MOVEMENT_RULES_WIKI);
    }

    public static JBJGLMenu generateSplashScreen(
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

    public static JBJGLTextMenuElement generateDevelopmentInformation() {
        final int height = TechnicalSettings.getHeight();
        final int pixel = TechnicalSettings.getPixelSize();

        return JBJGLTextMenuElement.generate(
                new int[] { pixel, height }, JBJGLMenuElement.Anchor.LEFT_BOTTOM,
                generateInitialMenuTextBuilder().setColor(Swatches.PLAYER(Swatches.OPAQUE()))
                        .addText("version " + Translation.VERSION).addLineBreak()
                        .addText("Jordan Bunke, 2022").build());
    }

    public static JBJGLTextMenuElement generateMenuTextBlurb(
            final String[] lines, JBJGLText.Orientation orientation,
            final int x, final int y, final int textSize
    ) {
        final JBJGLTextBuilder tb = generateInitialMenuTextBuilder(textSize, orientation);

        for (int i = 0; i < lines.length; i++) {
            tb.addText(lines[i]);

            if (i + 1 < lines.length)
                tb.addLineBreak();
        }

        return JBJGLTextMenuElement.generate(
                new int[] { x, y }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                tb.build());
    }

    public static JBJGLTextMenuElement generateMenuTextBlurb(
            final String text, JBJGLText.Orientation orientation,
            final int x, final int y, final int textSize
    ) {
        final String NEW_LINE = "\n";
        return generateMenuTextBlurb(text.split(NEW_LINE), orientation, x, y, textSize);
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

    // HIDDEN - ELEMENT GENERATORS

    // level 1

    private static JBJGLMenuElement generateBackButton(final String backMenuID) {
        // TODO - check
        return determineTextButton(
                "< BACK", new int[] { MARGIN, MARGIN },
                JBJGLMenuElement.Anchor.LEFT_TOP,
                coordinateFromFraction(TechnicalSettings.getWidth(), 0.1),
                () -> linkMenu(backMenuID));
    }

    private static JBJGLMenuElementGrouping generateLevelKeyInfo(final Level level) {
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();
        final int platformsX = coordinateFromFraction(width, 0.2);
        final int bestTimeX = coordinateFromFraction(width, 0.5);
        final int sentriesX = coordinateFromFraction(width, 0.8);
        final int topY = coordinateFromFraction(height, 0.5);
        final int bottomYLow = coordinateFromFraction(height, 0.625);
        final int bottomYHigh = coordinateFromFraction(height, 0.55);

        final int platforms = level.getPlatformSpecs().length;
        final int sentries = level.getSentrySpecs().length;

        return JBJGLMenuElementGrouping.generateOf(
                generateNumberAndCaption(
                        String.valueOf(platforms),
                        platforms == 1 ? "PLATFORM" : "PLATFORMS",
                        platformsX, topY, bottomYLow),
                generateNumberAndCaption(
                        String.valueOf(sentries),
                        sentries == 1 ? "SENTRY" : "SENTRIES",
                        sentriesX, topY, bottomYLow),
                generateNumberAndCaption(
                        level.getStats().getPersonalBest(LevelStats.TIME, true),
                        "BEST TIME:", bestTimeX, bottomYHigh, topY)
        );
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
                                Swatches.BLACK(), Fonts.GAME_ITALICS_SPACED()
                        ).addText(number).build()),
                JBJGLTextMenuElement.generate(
                        new int[] { x, captionY }, JBJGLMenuElement.Anchor.CENTRAL_TOP,
                        JBJGLTextBuilder.initialize(
                                2, JBJGLText.Orientation.CENTER,
                                Swatches.BLACK(), Fonts.GAME_STANDARD()
                        ).addText(caption).build())
        );
    }

    // level 2

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

    private static JBJGLToggleClickableMenuElement generateTextToggleButton(
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
                behaviours, updateIndexLogic);
    }

    @Deprecated
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
                drawNonHighlightedButton(buttonWidth, heading, Swatches.PLAYER(Swatches.OPAQUE()));
        JBJGLImage highlightedButton = drawHighlightedButton(nonHighlightedButton); // TODO - full refactor different params

        return JBJGLClickableMenuElement.generate(
                position, new int[] { buttonWidth, nonHighlightedButton.getHeight() },
                anchor, nonHighlightedButton, highlightedButton, behaviour);
    }

    private static JBJGLStaticMenuElement generateTextButtonStub(
            final String heading, final int[] position,
            final JBJGLMenuElement.Anchor anchor, final int buttonWidth
    ) {
        JBJGLImage buttonStub =
                drawNonHighlightedButton(buttonWidth, heading, Swatches.WHITE());

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
                Swatches.BLACK(), Fonts.GAME_STANDARD());
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder(final int textSize) {
        return generateInitialMenuTextBuilder(textSize, JBJGLText.Orientation.LEFT);
    }

    private static JBJGLTextBuilder generateInitialMenuTextBuilder() {
        return generateInitialMenuTextBuilder(1, JBJGLText.Orientation.LEFT);
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

    // MATHS HELPERS

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

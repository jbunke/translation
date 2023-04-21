package com.jordanbunke.translation.editor;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.entities.Platform;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class EditorHUD {
    private static final int INIT_MODE_UPDATE_COUNTER = 50;
    private static int modeUpdateCounter = 0;
    private static JBJGLImage modeHUD = JBJGLImage.create(1, 1);
    private static String savedSelectionText = "";
    private static JBJGLImage stImage = JBJGLImage.create(1, 1);

    public enum Anchor {
        TOP_LEFT,
        BOTTOM_LEFT,
        BOTTOM_MIDDLE,
        BOTTOM_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_RIGHT;

        void render(
                final JBJGLImage image, final Graphics g,
                final double previousAtAnchor
        ) {
            final int margin = TechnicalSettings.getPixelSize() * 8;

            final int width = TechnicalSettings.getWidth(),
                    height = TechnicalSettings.getHeight();
            int x, y;

            // x
            x = switch (this) {
                case TOP_LEFT, BOTTOM_LEFT, MIDDLE_LEFT ->
                        margin;
                case BOTTOM_RIGHT, MIDDLE_RIGHT ->
                        width - (image.getWidth() + margin);
                default ->
                        (width / 2) - (image.getWidth() / 2);
            };

            // y
            y = switch (this) {
                case TOP_LEFT ->
                        margin + (int)(image.getHeight() * previousAtAnchor);
                case MIDDLE_LEFT, MIDDLE_RIGHT ->
                        (height / 2) + margin +
                                (int)(image.getHeight() * previousAtAnchor);
                default ->
                        height - (margin + (image.getHeight() *
                                (int)(previousAtAnchor + 1)));
            };

            g.drawImage(image, x, y, null);
        }
    }

    public enum ControlPrompt {
        // GENERAL
        CAN_GO_TO_MENU(
                () -> true, " OPEN MENU",
                Anchor.TOP_LEFT, ControlScheme.Action.PAUSE
        ),
        CAN_ZOOM(
                () -> true, " TOGGLE ZOOM",
                Anchor.TOP_LEFT, ControlScheme.Action.TOGGLE_ZOOM
        ),
        CAN_SNAP_TO_GRID(
                Editor::canSnapToGrid, " SNAP TO GRID",
                Anchor.TOP_LEFT, ControlScheme.Action.SNAP_TO_GRID
        ),

        // PLATFORM
        CAN_CREATE_PLATFORM(
                Editor::canCreatePlatform, " CREATE PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.SAVE_POS
        ),
        CAN_DELETE_PLATFORM(
                Editor::canDeletePlatform, " DELETE PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.LOAD_POS
        ),
        CAN_TOGGLE_MODE(
                Editor::canToggleMode, " TOGGLE MODE",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.TOGGLE_FOLLOW_MODE
        ),
        CAN_EXPAND_PLATFORM(
                Editor::canExpandPlatform, " EXPAND PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.MOVE_RIGHT
        ),
        CAN_CONTRACT_PLATFORM(
                Editor::canContractPlatform, " CONTRACT PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.MOVE_LEFT
        ),
        CAN_MOVE_PLATFORM(
                Editor::canMovePlatform, " MOVE PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.JUMP
        ),

        // SENTRIES
        CAN_CREATE_SENTRY(
                Editor::canCreateSentry, " CREATE SENTRY",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.SAVE_POS
        ),
        CAN_DELETE_SENTRY(
                Editor::canDeleteSentry, " DELETE SENTRY",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.LOAD_POS
        ),
        CAN_TOGGLE_SENTRIES(
                Editor::canToggleSentries, " GO TO NEXT SENTRY",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.JUMP
        ),
        CAN_INCREASE_SENTRY_SPEED(
                Editor::canIncreaseSentrySpeed, " INCREASE SENTRY SPEED",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.MOVE_RIGHT
        ),
        CAN_DECREASE_SENTRY_SPEED(
                Editor::canDecreaseSentrySpeed, " DECREASE SENTRY SPEED",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.MOVE_LEFT
        ),
        CAN_TOGGLE_SENTRY_ROLE(
                Editor::canToggleSentryRole, " CHANGE ROLE",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.DROP
        ),
        CAN_TOGGLE_SENTRY_SPAWNER_SECONDARY_ROLE(
                Editor::canToggleSentrySpawnerSecondaryRole, " CHANGE SECONDARY ROLE",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.SNAP_TO_GRID
        );

        final Callable<Boolean> checkerFunction;
        final String caption;
        final ControlScheme.Action associatedAction;
        final Anchor anchor;

        private JBJGLImage controlPrompt;

        ControlPrompt(
                final Callable<Boolean> checkerFunction,
                final String caption, final Anchor anchor,
                final ControlScheme.Action associatedAction
        ) {
            this.checkerFunction = checkerFunction;
            this.caption = caption;
            this.associatedAction = associatedAction;
            this.anchor = anchor;

            update();
        }

        private String getPromptText() {
            if (caption.equals(" MOVE PLATFORM"))
                return
                        ControlScheme.getCorrespondingKey(ControlScheme.Action.JUMP)
                                .print().replace(" ]", "") + ", " +
                        ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_LEFT)
                                .print().replace(" ]", "")
                                .replace("[ ", "") + ", " +
                        ControlScheme.getCorrespondingKey(ControlScheme.Action.DROP)
                                .print().replace(" ]", "")
                                .replace("[ ", "") + ", " +
                        ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_RIGHT)
                                .print().replace("[ ", "") +
                        caption;

            return ControlScheme.getCorrespondingKey(associatedAction).print() + caption;
        }

        public void update() {
            controlPrompt = generateText(getPromptText(), 1.);
        }

        public JBJGLImage getControlPrompt() {
            return controlPrompt;
        }

        public boolean toBePrinted() {
            try {
                return checkerFunction.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void initialize() {
        reset();
    }

    public static void reset() {
        for (ControlPrompt cp : ControlPrompt.values())
            cp.update();
    }

    public static void initializeModeHUD() {
        modeUpdateCounter = INIT_MODE_UPDATE_COUNTER;
        modeHUD = generateModeHUD();
    }

    private static JBJGLImage generateModeHUD() {
        final Editor.Mode mode = Editor.getMode();

        final JBJGLImage[] modeTexts = new JBJGLImage[Editor.Mode.values().length];
        int widest = 0;

        for (Editor.Mode m : Editor.Mode.values()) {
            final int index = m.ordinal();
            final boolean isMode = m.equals(mode);

            final JBJGLImage modeText = generateText(m.print(), 1.,
                            isMode ? TLColors.BLACK() : TLColors.PLAYER());

            widest = Math.max(widest, modeText.getWidth());
            modeTexts[index] = modeText;
        }

        final int BUFFER = 16, SECTION_WIDTH = widest + (2 * BUFFER), HEIGHT = 48;

        final JBJGLImage modeHUD = JBJGLImage.create(
                SECTION_WIDTH * Editor.Mode.values().length, HEIGHT
        );
        final Graphics g = modeHUD.getGraphics();

        g.setColor(TLColors.BLACK());
        g.fillRect(0, BUFFER, modeHUD.getWidth(), HEIGHT - BUFFER);
        g.setColor(TLColors.PLAYER());
        g.fillRect(SECTION_WIDTH * mode.ordinal(), 0, SECTION_WIDTH, HEIGHT);

        for (int i = 0; i < modeTexts.length; i++) {
            final int offsetX = (SECTION_WIDTH - modeTexts[i].getWidth()) / 2;

            g.drawImage(
                    modeTexts[i], (SECTION_WIDTH * i) + offsetX,
                    mode.ordinal() == i ? 0 : BUFFER, null);
        }

        return modeHUD;
    }

    public static void update() {
        if (modeUpdateCounter > 0)
            modeUpdateCounter--;

        updateSelectedSentryHUD();
    }

    private static void updateSelectedSentryHUD() {
        if (!Editor.canDeleteSentry()) return;

        final EditorPlatformSentries.EditorSentrySpec s =
                Editor.getSelectedPlatformSentries().getSelectedSentry();

        s.updateCounter();
    }

    public static void render(
            final Graphics g
    ) {
        // cursor
        renderCursor(g);

        // cursor position
        renderCursorPosition(g);

        // selected text
        renderSelectionText(g);

        // control prompts
        renderControlPrompts(g);

        // selected entity information
        renderSelectedInformationForMode(g);

        // render selected sentry as HUD
        renderSelectedSentryAsHUD(g);

        // mode
        if (modeUpdateCounter > 0)
            renderModeHUD(g);
    }

    private static void renderCursor(
            final Graphics g
    ) {
        final int DISTANCE_MARKERS = 3, DISTANCE_INCREMENT = 100, SIZE_INCREMENT = 20;

        final int pixel = TechnicalSettings.getPixelSize(),
                width = TechnicalSettings.getWidth(),
                height = TechnicalSettings.getHeight();

        final int topDownX = (width / 2) - (pixel / 2),
                leftRightY = (height / 2) - (pixel / 2);

        g.setColor(Editor.getHighlightedPlatform() == null
                ? TLColors.PLATFORM() : TLColors.DEBUG());

        g.fillRect(topDownX, 0, pixel, height);
        g.fillRect(0, leftRightY, width, pixel);

        for (int i = DISTANCE_MARKERS; i > 0; i--) {
            int distance = i * DISTANCE_INCREMENT,
                    size = ((DISTANCE_MARKERS + 1) - i) * SIZE_INCREMENT;

            // top
            g.fillRect((width / 2) - (size / 2), leftRightY - distance, size, pixel);
            // bottom
            g.fillRect((width / 2) - (size / 2), leftRightY + distance, size, pixel);
            // left
            g.fillRect(topDownX - distance, (height / 2) - (size / 2), pixel, size);
            // right
            g.fillRect(topDownX + distance, (height / 2) - (size / 2), pixel, size);
        }
    }

    private static void renderCursorPosition(
            final Graphics g
    ) {
        final int[] cp = Editor.getCursorPosition();
        final JBJGLImage cpImage = generateText(
                "(" + cp[RenderConstants.X] + ", " +
                        cp[RenderConstants.Y] + ")", 1.);

        final int buffer = TechnicalSettings.getPixelSize() * 8;

        final int x = TechnicalSettings.pixelLockNumber(
                TechnicalSettings.getWidth() - (cpImage.getWidth() + buffer)
        ), y = TechnicalSettings.pixelLockNumber(buffer);

        g.drawImage(cpImage, x, y, null);
    }

    private static void renderSelectedInformationForMode(
            final Graphics g
    ) {
        final String NEW_LINE = "\n";

        final Platform p = Editor.getSelectedPlatform();
        final Editor.Mode mode = Editor.getMode();

        if (p == null) return;

        final String textToRender = switch (mode) {
            case MOVE_PLATFORM -> {
                int[] pos = p.getPosition();
                yield "POSITION: " + pos[RenderConstants.X] +
                        ", " + pos[RenderConstants.Y];
            }
            case RESIZE_PLATFORM -> "WIDTH: " + p.getWidth();
            case SENTRY -> {
                if (Editor.canDeleteSentry()) {
                    final EditorPlatformSentries.EditorSentrySpec s =
                            Editor.getSelectedPlatformSentries().getSelectedSentry();
                    final String direction = s.getDirection() == Sentry.LEFT ? "LEFT" : "RIGHT";

                    yield "SENTRY " + (Editor.getSelectedPlatformSentries().getSelectedIndex() + 1) +
                            " OF " + Editor.getSelectedPlatformSentries().getSize() +
                            NEW_LINE + " " + NEW_LINE + s.getRole() + // " " necessary to not trigger exception in JBJGLImage
                            (s.getRole() == Sentry.Role.SPAWNER
                                    ? " (" + s.getSecondaryRole() + ")"
                                    : "") + NEW_LINE +
                            "SPEED: " + s.getSpeed() + NEW_LINE +
                            "INITIAL DIRECTION: " + direction;
                }

                yield "NO SENTRIES ON PLATFORM";
            }
        };

        if (mode == Editor.Mode.SENTRY) {
            final String[] lines = textToRender.split(NEW_LINE);

            for (int i = 0; i < lines.length; i++)
                Anchor.MIDDLE_RIGHT.render(generateText(lines[i], 1.), g, i);
        } else
            Anchor.MIDDLE_RIGHT.render(generateText(textToRender, 1.), g, 0.);
    }

    private static void renderSelectedSentryAsHUD(
            final Graphics g
    ) {
        if (!Editor.canDeleteSentry()) return;

        final EditorPlatformSentries.EditorSentrySpec s =
                Editor.getSelectedPlatformSentries().getSelectedSentry();

        final JBJGLImage selectedSentryHUD = Sentry.drawForEditorHUD(true,
                s.getRole(), s.getSecondaryRole(), s.getCounter());

        final Sentry.Role
                previousRole = s.getRole().previous(),
                nextRole = s.getRole().next();
        final JBJGLImage previousSentryHUD = Sentry.drawForEditorHUD(false, previousRole, previousRole, 0);
        final JBJGLImage nextSentryHUD = Sentry.drawForEditorHUD(false, nextRole, nextRole, 0);

        final int dim = selectedSentryHUD.getWidth(), smallDim = previousSentryHUD.getWidth();
        final JBJGLImage lastThisAndNext = JBJGLImage.create(dim, dim);
        final Graphics ltnG = lastThisAndNext.getGraphics();

        ltnG.drawImage(selectedSentryHUD, 0, 0, null);
        ltnG.drawImage(previousSentryHUD, 0, (dim - smallDim) / 2, null);
        ltnG.drawImage(nextSentryHUD, dim - smallDim, (dim - smallDim) / 2, null);

        ltnG.dispose();

        Anchor.MIDDLE_RIGHT.render(lastThisAndNext, g, 0.32);
    }

    private static void renderModeHUD(
            final Graphics g
    ) {
        final int x = (TechnicalSettings.getWidth() / 2) - (modeHUD.getWidth() / 2),
                y = TechnicalSettings.getHeight() - modeHUD.getHeight();

        g.drawImage(modeHUD, x, y, null);
    }

    private static void renderSelectionText(
            final Graphics g
    ) {
        final String selectionText = Editor.getSelectionText();
        if (selectionText.equals(""))
            return;

        if (!selectionText.equals(savedSelectionText)) {
            savedSelectionText = selectionText;
            stImage = generateText(selectionText, 1.);
        }

        final int buffer = TechnicalSettings.getPixelSize() * 8;

        final int x = TechnicalSettings.pixelLockNumber(
                (TechnicalSettings.getWidth() / 2) - (stImage.getWidth() / 2)
        ), y = TechnicalSettings.pixelLockNumber(buffer);

        g.drawImage(stImage, x, y, null);
    }

    private static void renderControlPrompts(
            final Graphics g
    ) {
        // initialize map
        final Map<Anchor, Integer> promptsAtAnchor = new HashMap<>();

        for (Anchor a : Anchor.values())
            promptsAtAnchor.put(a, 0);

        // check prompts against conditions and print if passed
        for (ControlPrompt cp : ControlPrompt.values()) {
            if (cp.toBePrinted()) {
                cp.anchor.render(
                        cp.getControlPrompt(), g, promptsAtAnchor.get(cp.anchor)
                );
                promptsAtAnchor.put(cp.anchor, promptsAtAnchor.get(cp.anchor) + 1);
            }
        }
    }

    // HELPERS
    private static JBJGLImage generateText(
            final String text, final double textSize
    ) {
        return generateText(text, textSize, TLColors.PLAYER());
    }

    private static JBJGLImage generateText(
            final String text, final double textSize, final Color color
    ) {
        return JBJGLTextBuilder.initialize(
                textSize, JBJGLText.Orientation.CENTER, color,
                Fonts.GAME_STANDARD()).addText(text).build().draw();
    }
}

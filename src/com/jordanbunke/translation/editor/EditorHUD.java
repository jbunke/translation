package com.jordanbunke.translation.editor;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class EditorHUD {
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
                final int previousAtAnchor
        ) {
            final int buffer = TechnicalSettings.getPixelSize() * 8;

            final int width = TechnicalSettings.getWidth(),
                    height = TechnicalSettings.getHeight();
            int x, y;

            // x
            x = switch (this) {
                case TOP_LEFT, BOTTOM_LEFT, MIDDLE_LEFT ->
                        buffer;
                case BOTTOM_RIGHT, MIDDLE_RIGHT ->
                        width - (image.getWidth() + buffer);
                default ->
                        (width / 2) - (image.getWidth() / 2);
            };

            // y
            y = switch (this) {
                case TOP_LEFT ->
                        buffer + (image.getHeight() * previousAtAnchor);
                case MIDDLE_LEFT, MIDDLE_RIGHT ->
                        (height / 2) + buffer +
                                (image.getHeight() * previousAtAnchor);
                default ->
                        height - (buffer + (image.getHeight() *
                                (previousAtAnchor + 1)));
            };

            g.drawImage(image, x, y, null);
        }
    }

    public enum ControlPrompt {
        CAN_GO_TO_MENU(
                () -> true, "OPEN MENU",
                Anchor.TOP_LEFT, ControlScheme.Action.PAUSE
        ),
        CAN_SNAP_TO_GRID(
                () -> true, "SNAP TO GRID",
                Anchor.BOTTOM_LEFT, ControlScheme.Action.SNAP_TO_GRID
        ),
        CAN_ZOOM(
                () -> true, "TOGGLE ZOOM",
                Anchor.BOTTOM_LEFT, ControlScheme.Action.TOGGLE_ZOOM
        ),
        CAN_CREATE_PLATFORM(
                Editor::canCreatePlatform, "CREATE PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.SAVE_POS
        ),
        CAN_DELETE_PLATFORM(
                Editor::canDeletePlatform, "DELETE PLATFORM",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.LOAD_POS
        ),
        CAN_TOGGLE_MODE(
                Editor::canToggleMode, "TOGGLE MODE",
                Anchor.MIDDLE_LEFT, ControlScheme.Action.TOGGLE_FOLLOW_MODE
        )
        ;

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
            return ControlScheme.getCorrespondingKey(associatedAction).print() + " " + caption;
        }

        public void update() {
            controlPrompt = generateText(getPromptText(), 1);
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

        g.setColor(Editor.getHighlightedEntity() == null
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
                        cp[RenderConstants.Y] + ")", 1);

        final int buffer = TechnicalSettings.getPixelSize() * 8;

        final int x = TechnicalSettings.pixelLockNumber(
                TechnicalSettings.getWidth() - (cpImage.getWidth() + buffer)
        ), y = TechnicalSettings.pixelLockNumber(buffer);

        g.drawImage(cpImage, x, y, null);
    }

    private static void renderSelectionText(
            final Graphics g
    ) {
        final String selectionText = Editor.getSelectionText();
        if (selectionText.equals(""))
            return;

        if (!selectionText.equals(savedSelectionText)) {
            savedSelectionText = selectionText;
            stImage = generateText(selectionText, 1);
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
            final String text, final int textSize
    ) {
        return JBJGLTextBuilder.initialize(
                textSize, JBJGLText.Orientation.CENTER, TLColors.PLAYER(TLColors.OPAQUE()),
                Fonts.GAME_STANDARD()).addText(text).build().draw();
    }
}

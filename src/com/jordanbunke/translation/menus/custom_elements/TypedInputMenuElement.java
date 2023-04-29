package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.events.JBJGLEvent;
import com.jordanbunke.jbjgl.events.JBJGLKeyEvent;
import com.jordanbunke.jbjgl.events.JBJGLMouseEvent;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.menus.MenuHelper;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.sound.Sounds;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TypedInputMenuElement extends JBJGLMenuElement {

    private final Set<String> invalidInputs;

    private final int maxLength;
    private String input;
    private final List<JBJGLImage> nonHighlightedLetterGlyphs, highlightedLetterGlyphs;
    private int cursorIndex;

    private boolean setMode;
    private boolean highlighted;

    // private final Function<String, JBJGLImage> hGeneratorFunction, nhGeneratorFunction;

    private JBJGLImage nonHighlightedImage, nonHighlightedSetImage, highlightedSetImage;
    private final JBJGLImage highlightedImage, highlightedBorder, nonHighlightedBorder;

    private TypedInputMenuElement(
            final int[] position, final int[] dimensions,
            final Anchor anchor,
            final JBJGLImage highlightedBorder,
            final JBJGLImage nonHighlightedBorder,
            final JBJGLImage highlightedImage,
            final String defaultInput, final Set<String> invalidInputs, final int maxLength
    ) {
        super(position, dimensions, anchor, true);

        this.input = defaultInput;
        this.cursorIndex = input.length();

        this.invalidInputs = invalidInputs;
        this.maxLength = maxLength;

        setMode = false;
        highlighted = false;

        this.highlightedImage = highlightedImage;
        this.highlightedBorder = highlightedBorder;
        this.nonHighlightedBorder = nonHighlightedBorder;

        this.highlightedLetterGlyphs = new ArrayList<>();
        this.nonHighlightedLetterGlyphs = new ArrayList<>();

        generateInitialGlyphs();

        generateImages();
    }

    public static TypedInputMenuElement generate(
            final int[] position, final int[] dimensions,
            final Anchor anchor,
            final JBJGLImage highlightedBorder,
            final JBJGLImage nonHighlightedBorder,
            final JBJGLImage highlightedImage,
            final String defaultInput, final Set<String> invalidInputs, final int maxLength
    ) {
        return new TypedInputMenuElement(position, dimensions, anchor,
                highlightedBorder, nonHighlightedBorder, highlightedImage,
                defaultInput, invalidInputs, maxLength);
    }

    public boolean inputIsValid() {
        return !invalidInputs.contains(input);
    }

    private void generateImages() {
        nonHighlightedImage = generateImage(false, false);
        nonHighlightedSetImage = generateImage(false, true);
        highlightedSetImage = generateImage(true, true);
    }

    private JBJGLImage generateImage(final boolean highlighted, final boolean setMode) {
        final JBJGLImage image = JBJGLImage.create(getWidth(), getHeight());
        final Graphics g = image.getGraphics();

        final int pixel = TechnicalSettings.getPixelSize();
        int processed = pixel * 3;

        // cursor image
        final JBJGLImage cursor = JBJGLImage.create(pixel, getHeight());
        final Graphics cg = cursor.getGraphics();

        cg.setColor(getColor(highlighted));
        cg.fillRect(0, 0, cursor.getWidth(), cursor.getHeight());

        cg.dispose();

        // border
        g.drawImage(highlighted ? highlightedBorder : nonHighlightedBorder, 0, 0, null);

        final List<JBJGLImage> glyphs = highlighted
                ? highlightedLetterGlyphs
                : nonHighlightedLetterGlyphs;

        final int printableGlyphs = (int)(2.5 * MenuHelper.widthCoord(1.0)) / getHeight();

        final int startingIndex = cursorIndex > printableGlyphs ? cursorIndex - printableGlyphs : 0,
                endingIndex = glyphs.size();

        for (int i = startingIndex; i < endingIndex; i++) {
            if (processed > getWidth()) break;

            if (cursorIndex == i && setMode)
                g.drawImage(cursor, processed - pixel, 0, null);

            g.drawImage(glyphs.get(i), processed, pixel, null);
            processed += glyphs.get(i).getWidth() + pixel;
        }

        if (cursorIndex == endingIndex && setMode)
            g.drawImage(cursor, processed - pixel, 0, null);

        g.dispose();
        return image;
    }

    private void generateInitialGlyphs() {
        for (int i = 0; i < input.length(); i++) {
            addGlyph(nonHighlightedLetterGlyphs, i, input.charAt(i), false);
            addGlyph(highlightedLetterGlyphs, i, input.charAt(i), true);
        }
    }

    private void addGlyph(final List<JBJGLImage> list, final int index, final char c, final boolean highlighted) {
        final Color color = getColor(highlighted);

        list.add(index, drawText(String.valueOf(c), color));
    }

    private Color getColor(final boolean highlighted) {
        return highlighted ? TLColors.BLACK() : TLColors.PLAYER();
    }

    private JBJGLImage drawText(final String text, final Color color) {
        final double TEXT_SIZE = TechnicalSettings.getPixelSize() / 2.;

        return JBJGLTextBuilder.initialize(
                TEXT_SIZE, JBJGLText.Orientation.CENTER, color, Fonts.GAME_STANDARD()
        ).addText(text).build().draw();
    }

    @Override
    public void update() {

    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        final JBJGLImage renderImage = setMode
                ? (highlighted ? highlightedSetImage : nonHighlightedSetImage)
                : (highlighted ? highlightedImage : nonHighlightedImage);
        draw(renderImage, g);

        // Debug
        renderBoundingBox(g, debugger);
    }

    @Override
    public void process(final JBJGLListener listener, final JBJGLMenuManager menuManager) {
        processClick(listener);
        processTyping(listener);
    }

    private void processClick(final JBJGLListener listener) {
        highlighted = mouseIsWithinBounds(listener.getMousePosition());

        if (highlighted) {
            final java.util.List<JBJGLEvent> unprocessed = listener.getUnprocessedEvents();

            for (JBJGLEvent e : unprocessed)
                if (e instanceof JBJGLMouseEvent mouseEvent &&
                        mouseEvent.matchesAction(JBJGLMouseEvent.Action.CLICK)) {
                    mouseEvent.markAsProcessed();

                    Sounds.buttonClick();

                    setMode = !setMode;
                    highlighted = false;
                }
        } else {
            final List<JBJGLEvent> all = listener.getAllEvents();

            for (JBJGLEvent e : all)
                if (e instanceof JBJGLMouseEvent mouseEvent &&
                        mouseEvent.matchesAction(JBJGLMouseEvent.Action.CLICK))
                    setMode = false;
        }
    }

    private void processTyping(final JBJGLListener listener) {
        final int DELETE = 127, LOWEST_PRINTABLE = 32;

        if (setMode) {
            final List<JBJGLEvent> unprocessed = listener.getUnprocessedEvents();
            for (JBJGLEvent e : unprocessed)
                if (e instanceof JBJGLKeyEvent keyEvent) {
                    if (keyEvent.matchesAction(JBJGLKeyEvent.Action.PRESS)) {
                        switch (keyEvent.getKey()) {
                            // No longer setting button input
                            case ENTER, TAB, ESCAPE -> {
                                keyEvent.markAsProcessed();

                                Sounds.typedBoxSet();

                                setMode = false;
                                generateImages();
                            }
                            // Remove character before cursor from input string and regenerate images
                            case BACKSPACE -> {
                                keyEvent.markAsProcessed();

                                if (cursorIndex > 0) {
                                    input = input.substring(0, cursorIndex - 1) + input.substring(cursorIndex);
                                    nonHighlightedLetterGlyphs.remove(cursorIndex - 1);
                                    highlightedLetterGlyphs.remove(cursorIndex - 1);
                                    cursorIndex--;

                                    generateImages();
                                }
                            }
                            // Removes character after cursor from input string and regenerates images
                            case DELETE -> {
                                keyEvent.markAsProcessed();

                                if (cursorIndex < input.length()) {
                                    input = input.substring(0, cursorIndex) + input.substring(cursorIndex + 1);
                                    nonHighlightedLetterGlyphs.remove(cursorIndex);
                                    highlightedLetterGlyphs.remove(cursorIndex);

                                    generateImages();
                                }
                            }
                            // moves cursor index back if possible
                            case LEFT_ARROW ->  {
                                if (cursorIndex > 0) {
                                    cursorIndex--;

                                    generateImages();
                                }
                            }
                            // moves cursor index forwards if possible
                            case RIGHT_ARROW -> {
                                if (cursorIndex < input.length()) {
                                    cursorIndex++;

                                    generateImages();
                                }
                            }
                        }
                    } else if (keyEvent.matchesAction(JBJGLKeyEvent.Action.TYPE)) {
                        keyEvent.markAsProcessed();

                        final char c = keyEvent.getCharacter();

                        if (c == DELETE || c < LOWEST_PRINTABLE || input.length() >= maxLength) continue;

                        Sounds.typedChar();

                        input = input.substring(0, cursorIndex) + c + input.substring(cursorIndex);
                        addGlyph(nonHighlightedLetterGlyphs, cursorIndex, c, false);
                        addGlyph(highlightedLetterGlyphs, cursorIndex, c, true);
                        cursorIndex++;

                        generateImages();
                    }
                }
        }
    }

    // GETTERS

    public String getInput() {
        return input;
    }
}

package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.events.JBJGLEvent;
import com.jordanbunke.jbjgl.events.JBJGLKeyEvent;
import com.jordanbunke.jbjgl.events.JBJGLMouseEvent;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TypedInputMenuElement extends JBJGLMenuElement {

    private final Set<String> invalidInputs;

    private String input;

    private boolean setMode;
    private boolean highlighted;

    private final Function<String, JBJGLImage> hGeneratorFunction, nhGeneratorFunction;

    private JBJGLImage nonHighlightedImage, nonHighlightedSetImage, highlightedSetImage;
    private final JBJGLImage highlightedImage;

    private TypedInputMenuElement(
            final int[] position, final int[] dimensions,
            final Anchor anchor,
            final Function<String, JBJGLImage> hGeneratorFunction,
            final Function<String, JBJGLImage> nhGeneratorFunction,
            final JBJGLImage highlightedImage,
            final String defaultInput, final Set<String> invalidInputs
    ) {
        super(position, dimensions, anchor, true);

        this.input = defaultInput;
        this.invalidInputs = invalidInputs;

        setMode = false;
        highlighted = false;

        this.hGeneratorFunction = hGeneratorFunction;
        this.nhGeneratorFunction = nhGeneratorFunction;

        this.highlightedImage = highlightedImage;

        generateImages();
    }

    public static TypedInputMenuElement generate(
            final int[] position, final int[] dimensions,
            final Anchor anchor,
            final Function<String, JBJGLImage> hGeneratorFunction,
            final Function<String, JBJGLImage> nhGeneratorFunction,
            final JBJGLImage highlightedImage,
            final String defaultInput, final Set<String> invalidInputs
    ) {
        return new TypedInputMenuElement(position, dimensions, anchor,
                hGeneratorFunction, nhGeneratorFunction,
                highlightedImage, defaultInput, invalidInputs);
    }

    public boolean inputIsValid() {
        return !invalidInputs.contains(input);
    }

    public void generateImages() {
        final String prependToSetImages = "TYPING: ";

        nonHighlightedImage = nhGeneratorFunction.apply("\"" + input + "\"");
        nonHighlightedSetImage = nhGeneratorFunction.apply(prependToSetImages + "\"" + input + "\"");
        highlightedSetImage = hGeneratorFunction.apply(prependToSetImages + "\"" + input + "\"");
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
                    if (keyEvent.matchesAction(JBJGLKeyEvent.Action.RELEASE)) {
                        switch (keyEvent.getKey()) {
                            // No longer setting button input
                            case ENTER, TAB, ESCAPE -> {
                                keyEvent.markAsProcessed();

                                setMode = false;
                                generateImages();
                            }
                            // Remove last character from input string and regenerate images
                            case BACKSPACE -> {
                                keyEvent.markAsProcessed();

                                if (input.length() > 0)
                                    input = input.substring(0, input.length() - 1);

                                generateImages();
                            }
                        }
                    } else if (keyEvent.matchesAction(JBJGLKeyEvent.Action.TYPE)) {
                        keyEvent.markAsProcessed();

                        final char c = keyEvent.getCharacter();

                        if (c == DELETE || c < LOWEST_PRINTABLE) continue;

                        input += c;
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

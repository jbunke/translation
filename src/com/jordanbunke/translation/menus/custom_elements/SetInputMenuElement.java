package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.events.JBJGLEvent;
import com.jordanbunke.jbjgl.events.JBJGLKey;
import com.jordanbunke.jbjgl.events.JBJGLKeyEvent;
import com.jordanbunke.jbjgl.events.JBJGLMouseEvent;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class SetInputMenuElement extends JBJGLMenuElement {
    private static final int UPDATE_FREQUENCY = 100;
    private int updateCounter;

    private boolean setMode;
    private boolean isHighlighted;

    private final Callable<JBJGLImage> nhGeneratorFunction;
    private final Callable<JBJGLImage> hGeneratorFunction;

    private JBJGLImage nonHighlightedImage;
    private JBJGLImage highlightedImage;

    private final JBJGLImage nonHighlightedSetImage;
    private final JBJGLImage highlightedSetImage;

    private final Consumer<JBJGLKey> setFunction;

    private SetInputMenuElement(
            final int[] position, final int[] dimensions, final Anchor anchor,
            final Consumer<JBJGLKey> setFunction,
            final Callable<JBJGLImage> nhGeneratorFunction,
            final Callable<JBJGLImage> hGeneratorFunction,
            final JBJGLImage nonHighlightedSetImage, final JBJGLImage highlightedSetImage
    ) {
        super(position, dimensions, anchor, true);

        this.nhGeneratorFunction = nhGeneratorFunction;
        this.hGeneratorFunction = hGeneratorFunction;

        this.nonHighlightedSetImage = nonHighlightedSetImage;
        this.highlightedSetImage = highlightedSetImage;

        setMode = false;
        isHighlighted = false;

        this.setFunction = setFunction;
        updateCounter = Utility.boundedRandom(0, UPDATE_FREQUENCY); // randomize to reduce lag from synced updates

        generateImages();
    }

    public static SetInputMenuElement generate(
            final int[] position, final int[] dimensions, final Anchor anchor,
            final Consumer<JBJGLKey> setFunction,
            final Callable<JBJGLImage> nhGeneratorFunction,
            final Callable<JBJGLImage> hGeneratorFunction,
            final JBJGLImage nonHighlightedSetImage, final JBJGLImage highlightedSetImage
    ) {
        return new SetInputMenuElement(
                position, dimensions, anchor, setFunction,
                nhGeneratorFunction, hGeneratorFunction,
                nonHighlightedSetImage, highlightedSetImage
        );
    }

    private void generateImages() {
        try {
            nonHighlightedImage = nhGeneratorFunction.call();
            highlightedImage = hGeneratorFunction.call();
        } catch (Exception e) {
            Translation.debugger.getChannel(JBJGLGameDebugger.LOGIC_CHANNEL).printMessage(
                    "Callable function threw excecption. Did not generate images for " +
                            "SetInputMenuElement."
            );
        }
    }

    @Override
    public void update() {
        if (updateCounter == 0)
            generateImages();

        updateCounter++;
        updateCounter %= UPDATE_FREQUENCY;
    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        final JBJGLImage renderImage = setMode
                ? (isHighlighted
                        ? highlightedSetImage
                        : nonHighlightedSetImage)
                : (isHighlighted
                        ? highlightedImage
                        : nonHighlightedImage);
        draw(renderImage, g);

        // Debug
        renderBoundingBox(g, debugger);
    }

    @Override
    public void process(final JBJGLListener listener, final JBJGLMenuManager manager) {
        processClick(listener);
        processSet(listener);
    }

    private void processClick(final JBJGLListener listener) {
        isHighlighted = mouseIsWithinBounds(listener.getMousePosition());

        if (isHighlighted) {
            final List<JBJGLEvent> unprocessed = listener.getUnprocessedEvents();
            for (JBJGLEvent e : unprocessed)
                if (e instanceof JBJGLMouseEvent mouseEvent &&
                        mouseEvent.matchesAction(JBJGLMouseEvent.Action.CLICK)) {
                    mouseEvent.markAsProcessed();

                    setMode = !setMode;
                    isHighlighted = false;
                }
        }
    }

    private void processSet(final JBJGLListener listener) {
        if (setMode) {
            final List<JBJGLEvent> unprocessed = listener.getUnprocessedEvents();
            for (JBJGLEvent e : unprocessed)
                if (e instanceof JBJGLKeyEvent keyEvent &&
                        keyEvent.matchesAction(JBJGLKeyEvent.Action.RELEASE)) {
                    keyEvent.markAsProcessed();

                    setMode = false;
                    setFunction.accept(keyEvent.getKey());
                    generateImages();
                }
        }
    }
}

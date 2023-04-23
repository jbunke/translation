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

import java.awt.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class SetInputMenuElement extends JBJGLMenuElement {
    private boolean setMode;
    private boolean highlighted;

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
        highlighted = false;

        this.setFunction = setFunction;

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

    public void generateImages() {
        try {
            nonHighlightedImage = nhGeneratorFunction.call();
            highlightedImage = hGeneratorFunction.call();
        } catch (Exception e) {
            Translation.debugger.getChannel(JBJGLGameDebugger.LOGIC_CHANNEL).printMessage(
                    "Callable function threw exception. Did not generate images for SetInputMenuElement."
            );
        }
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
    public void process(final JBJGLListener listener, final JBJGLMenuManager manager) {
        processClick(listener);
        processSet(listener);
    }

    private void processClick(final JBJGLListener listener) {
        highlighted = mouseIsWithinBounds(listener.getMousePosition());

        if (highlighted) {
            final List<JBJGLEvent> unprocessed = listener.getUnprocessedEvents();

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

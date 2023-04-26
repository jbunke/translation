package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.error.JBJGLError;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;

import java.awt.*;
import java.util.concurrent.Callable;

public class ConditionalMenuElement extends JBJGLMenuElement {

    private final JBJGLMenuElement falseElement, trueElement;
    private final Callable<Boolean> condition;

    private boolean satisfied;

    private ConditionalMenuElement(
            final JBJGLMenuElement falseElement, final JBJGLMenuElement trueElement,
            final Callable<Boolean> condition
    ) {
        super(new int[] { 0, 0 }, new int[] { 1, 1 }, Anchor.LEFT_TOP, false);

        satisfied = false;

        this.falseElement = falseElement;
        this.trueElement = trueElement;

        this.condition = condition;
    }

    public static ConditionalMenuElement generate(
            final JBJGLMenuElement falseElement, final JBJGLMenuElement trueElement,
            final Callable<Boolean> condition
    ) {
        return new ConditionalMenuElement(falseElement, trueElement, condition);
    }

    @Override
    public void update() {
        try {
            satisfied = condition.call();
        } catch (Exception e) {
            JBJGLError.send("Couldn't validate condition in conditional menu element");
        }
    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        (satisfied ? trueElement : falseElement).render(g, debugger);
    }

    @Override
    public void process(final JBJGLListener listener, final JBJGLMenuManager menuManager) {
        (satisfied ? trueElement : falseElement).process(listener, menuManager);
    }
}

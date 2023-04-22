package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.menus.MenuHelper;

import java.awt.*;

public class ScrollingButtonMenuElement extends JBJGLMenuElement {
    private final int originalY;
    private final JBJGLMenuElement associatedElement;

    private ScrollingButtonMenuElement(
            final int originalY, final JBJGLMenuElement associatedElement
    ) {
        super(new int[] { 0, 0 }, new int[] { 1, 1 }, Anchor.CENTRAL_TOP, false);

        this.originalY = originalY;
        this.associatedElement = associatedElement;
    }

    public static ScrollingButtonMenuElement generate(
            final String heading, final Runnable behaviour,
            final int buttonWidth, final int[] position
    ) {
        final JBJGLMenuElement associatedElement =
                MenuHelper.determineTextButton(heading, position,
                        Anchor.CENTRAL_TOP, buttonWidth, behaviour);

        return new ScrollingButtonMenuElement(position[RenderConstants.Y], associatedElement);
    }

    public void offsetYBy(final int offsetY) {
        associatedElement.setY(originalY + offsetY);
    }

    @Override
    public void update() {
        associatedElement.update();
    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        associatedElement.render(g, debugger);
    }

    @Override
    public void process(final JBJGLListener listener, final JBJGLMenuManager menuManager) {
        associatedElement.process(listener, menuManager);
    }

    @Override
    public int getX() {
        return associatedElement.getX();
    }

    @Override
    public int getY() {
        return associatedElement.getY();
    }

    @Override
    public int getWidth() {
        return associatedElement.getWidth();
    }

    @Override
    public int getHeight() {
        return associatedElement.getHeight();
    }
}

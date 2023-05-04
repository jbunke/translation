package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.settings.GameplayConstants;

import java.awt.*;

public class TextWithTooltipMenuElement extends JBJGLMenuElement {

    private final JBJGLImage textImage;
    private final JBJGLImage tooltipImage;

    private boolean highlighted;
    private int tooltipX = 0, tooltipY = 0;

    private TextWithTooltipMenuElement(
            final int[] position, final int[] dimensions, final Anchor anchor,
            final JBJGLImage textImage, final JBJGLImage tooltipImage
    ) {
        super(position, dimensions, anchor, true);

        this.textImage = textImage;
        this.tooltipImage = tooltipImage;
    }

    public static TextWithTooltipMenuElement generate(
            final int[] position, final Anchor anchor,
            final JBJGLText text, final JBJGLText tooltip
    ) {
        final JBJGLImage textImage = text.draw();
        final JBJGLImage tooltipImage = tooltip.draw();
        final int[] dimensions = new int[] { textImage.getWidth(), textImage.getHeight() };
        return new TextWithTooltipMenuElement(position, dimensions, anchor, textImage, tooltipImage);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        draw(textImage, g);

        if (highlighted) {
            // render tooltip
            final int square = GameplayConstants.SQUARE_LENGTH();

            g.drawImage(tooltipImage, tooltipX + square, tooltipY + square, null);
        }

        // Debug
        renderBoundingBox(g, debugger);
    }

    @Override
    public void process(final JBJGLListener listener, final JBJGLMenuManager menuManager) {
        highlighted = mouseIsWithinBounds(listener.getMousePosition());

        if (highlighted) {
            final int[] mp = listener.getMousePosition();
            tooltipX = mp[RenderConstants.X];
            tooltipY = mp[RenderConstants.Y];
        }
    }
}

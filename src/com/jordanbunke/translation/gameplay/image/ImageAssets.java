package com.jordanbunke.translation.gameplay.image;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.swatches.Swatches;

import java.awt.*;

public class ImageAssets {
    // game
    private static JBJGLImage BACKGROUND = drawBackground(Swatches.BACKGROUND());
    private static JBJGLImage BLACK_BACKGROUND = drawBackground(Swatches.BLACK());
    public static final JBJGLImage ICON = drawIcon();

    // debug
    public static JBJGLImage PIXEL_GRID = drawPixelGrid();

    public static void updateAfterResize() {
        BACKGROUND = drawBackground(Swatches.BACKGROUND());
        BLACK_BACKGROUND = drawBackground(Swatches.BLACK());

        PIXEL_GRID = drawPixelGrid();
    }

    // ACCESSORS

    public static JBJGLImage BACKGROUND() {
        return BACKGROUND;
    }

    public static JBJGLImage BLACK_BACKGROUND() {
        return BLACK_BACKGROUND;
    }

    // GENERATORS

    public static JBJGLImage drawSentry(final Sentry.Role role) {
        return drawSentientSquare(role.getColor(Swatches.OPAQUE()));
    }

    private static JBJGLImage drawIcon() {
        return drawSentientSquare(Swatches.PLAYER(Swatches.OPAQUE()));
    }

    private static JBJGLImage drawSentientSquare(final Color color) {
        final int length = GameplayConstants.SQUARE_LENGTH();
        final int pixel = TechnicalSettings.getPixelSize();

        JBJGLImage square = JBJGLImage.create(length, length);
        Graphics g = square.getGraphics();

        g.setColor(Swatches.BLACK());
        g.fillRect(0, 0, length, length);

        g.setColor(color);
        g.fillRect(pixel, pixel, length - (2 * pixel), length - (2 * pixel));

        return square;
    }

    private static JBJGLImage drawBackground(final Color color) {
        JBJGLImage background = JBJGLImage.create(
                TechnicalSettings.getWidth(), TechnicalSettings.getHeight()
        );
        Graphics g = background.getGraphics();

        g.setColor(color);
        g.fillRect(0, 0, background.getWidth(), background.getHeight());

        return background;
    }

    private static JBJGLImage drawPixelGrid() {
        JBJGLImage pixelGrid = JBJGLImage.create(
                TechnicalSettings.getWidth(), TechnicalSettings.getHeight()
        );
        Graphics2D g = (Graphics2D) pixelGrid.getGraphics();

        final int pixelSize = TechnicalSettings.getPixelSize();

        g.setColor(Swatches.DEBUG(Swatches.FAINT()));
        g.setStroke(new BasicStroke(1));

        for (int x = 0; x < pixelGrid.getWidth(); x += pixelSize) {
            g.drawLine(x, 0, x, pixelGrid.getHeight());
        }

        for (int y = 0; y < pixelGrid.getHeight(); y += pixelSize) {
            g.drawLine(0, y, pixelGrid.getWidth(), y);
        }

        return pixelGrid;
    }
}

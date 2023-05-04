package com.jordanbunke.translation.gameplay.image;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLAnimationMenuElement;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLStaticMenuElement;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;

public class ImageAssets {
    // game
    private static final int FRACTURED_FRAME_SIZE = 20, TICKS_PER_FRAME = 5;
    private static int fracturedFrameIndex = 0;
    private static JBJGLImage[] FRACTURED_FRAMES = drawFracturedFrames();

    private static JBJGLImage BACKGROUND = drawBackground(TLColors.BACKGROUND());
    private static JBJGLImage BLACK_BACKGROUND = drawBackground(TLColors.BLACK());
    public static final JBJGLImage ICON = drawIcon();

    // debug
    public static JBJGLImage PIXEL_GRID = drawPixelGrid();

    public static void updateAfterResize() {
        BACKGROUND = drawBackground(TLColors.BACKGROUND());
        BLACK_BACKGROUND = drawBackground(TLColors.BLACK());
        FRACTURED_FRAMES = drawFracturedFrames();

        PIXEL_GRID = drawPixelGrid();
    }

    // ACCESSORS

    public static JBJGLImage getThemeBackground() {
        return switch (TechnicalSettings.getTheme()) {
            case CLASSIC -> ImageAssets.BACKGROUND();
            case NIGHT -> ImageAssets.BLACK_BACKGROUND();
            case FRACTURED -> {
                fracturedFrameIndex++;
                fracturedFrameIndex %= FRACTURED_FRAME_SIZE * TICKS_PER_FRAME;

                yield FRACTURED_FRAMES[fracturedFrameIndex / TICKS_PER_FRAME];
            }
        };
    }

    public static JBJGLMenuElement getThemeBackgroundMenuElement() {
        return switch (TechnicalSettings.getTheme()) {
            case CLASSIC, NIGHT -> JBJGLStaticMenuElement.generate(new int[] { 0, 0 },
                    JBJGLMenuElement.Anchor.LEFT_TOP, getThemeBackground());
            case FRACTURED -> JBJGLAnimationMenuElement.generate(new int[] { 0, 0 },
                    new int[] { TechnicalSettings.getWidth(), TechnicalSettings.getHeight() },
                    JBJGLMenuElement.Anchor.LEFT_TOP, TICKS_PER_FRAME, FRACTURED_FRAMES);
        };
    }

    public static JBJGLImage BACKGROUND() {
        return BACKGROUND;
    }

    public static JBJGLImage BLACK_BACKGROUND() {
        return BLACK_BACKGROUND;
    }

    // GENERATORS

    public static JBJGLImage drawSentry(final Sentry.Role role) {
        return drawSentientSquare(role.getColor(TLColors.OPAQUE()));
    }

    private static JBJGLImage drawIcon() {
        return drawSentientSquare(TLColors.PLAYER(TLColors.OPAQUE()));
    }

    private static JBJGLImage drawSentientSquare(final Color color) {
        final int length = GameplayConstants.SQUARE_LENGTH();
        final int pixel = TechnicalSettings.getPixelSize();

        JBJGLImage square = JBJGLImage.create(length, length);
        Graphics g = square.getGraphics();

        g.setColor(TLColors.BLACK());
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

    private static JBJGLImage[] drawFracturedFrames() {
        final int width = TechnicalSettings.getWidth(), height = TechnicalSettings.getHeight(),
                INCREMENT = 4, RANGE = INCREMENT * (FRACTURED_FRAME_SIZE / 2);

        final int paneSize = TechnicalSettings.getPixelSize() * 10,
                panesX = width / paneSize, panesY = height / paneSize;

        final Color[][] paneInitialization = new Color[panesX][panesY];
        final String[][] paneTendency = new String[panesX][panesY];

        for (int x = 0; x < panesX; x++) {
            for (int y = 0; y < panesY; y++) {
                paneInitialization[x][y] = new Color(
                        Utility.boundedRandom(0, RANGE),
                        Utility.boundedRandom(0, RANGE),
                        Utility.boundedRandom(0, RANGE),
                        TLColors.OPAQUE());
                paneTendency[x][y] = (x % 2 == 1 ? "+" : "-") +
                        (y % 2 == 1 ? "+" : "-") + ((x + y) % 2 == 1 ? "+" : "-");
            }
        }

        final JBJGLImage[] frames = new JBJGLImage[FRACTURED_FRAME_SIZE];

        for (int i = 0; i < FRACTURED_FRAME_SIZE; i++) {
            final JBJGLImage frame = JBJGLImage.create(width, height);
            final Graphics fg = frame.getGraphics();

            for (int x = 0; x < panesX; x++) {
                for (int y = 0; y < panesY; y++) {
                    final Color startingColor = paneInitialization[x][y];

                    final int delta = INCREMENT * i;

                    int r = startingColor.getRed() + (paneTendency[x][y].charAt(0) == '+' ? delta : -delta);
                    int g = startingColor.getGreen() + (paneTendency[x][y].charAt(1) == '+' ? delta : -delta);
                    int b = startingColor.getBlue() + (paneTendency[x][y].charAt(2) == '+' ? delta : -delta);

                    while (r < 0 || r > RANGE) {
                        if (r < 0)
                            r = -r;
                        else
                            r = RANGE - (r - RANGE);
                    }

                    while (g < 0 || g > RANGE) {
                        if (g < 0)
                            g = -g;
                        else
                            g = RANGE - (g - RANGE);
                    }

                    while (b < 0 || b > RANGE) {
                        if (b < 0)
                            b = -b;
                        else
                            b = RANGE - (b - RANGE);
                    }

                    final Color color = new Color(r, g, b, TLColors.OPAQUE());

                    fg.setColor(color);
                    fg.fillRect(x * paneSize, y * paneSize, paneSize, paneSize);
                }
            }

            fg.dispose();
            frames[i] = frame;
        }

        return frames;
    }

    private static JBJGLImage drawPixelGrid() {
        JBJGLImage pixelGrid = JBJGLImage.create(
                TechnicalSettings.getWidth(), TechnicalSettings.getHeight()
        );
        Graphics2D g = (Graphics2D) pixelGrid.getGraphics();

        final int pixelSize = TechnicalSettings.getPixelSize();

        g.setColor(TLColors.DEBUG(TLColors.FAINT()));
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

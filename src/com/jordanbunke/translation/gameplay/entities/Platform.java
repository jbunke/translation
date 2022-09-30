package com.jordanbunke.translation.gameplay.entities;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.editor.Editor;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.colors.TLColors;

import java.awt.*;

public class Platform extends Entity {
    private int width;

    private Platform(final int x, final int y, final int initialWidth) {
        super(x, y);

        width = initialWidth;
    }

    public static Platform create(
            final int x, final int y, final int initialWidth
    ) {
        return new Platform(x, y, initialWidth);
    }

    public boolean supports(final Player player) {
        final int[] pp = player.getPosition();
        final int[] plp = player.getLastPosition();

        final boolean wasAboveOrOn = plp[RenderConstants.Y] <= getPosition()[RenderConstants.Y];
        final boolean isNotAbove = pp[RenderConstants.Y] >
                getPosition()[RenderConstants.Y] - (GameplayConstants.PLATFORM_HEIGHT() + 1);

        return wasAboveOrOn && isNotAbove && xCoverage(player);
    }

    public boolean covers(final Player player) {
        final int[] pp = player.getPosition();

        final boolean isBelowPlayer = getPosition()[RenderConstants.Y] > pp[RenderConstants.Y];

        return isBelowPlayer && xCoverage(player);
    }

    private boolean xCoverage(final SentientSquare s) {
        final int[] sp = s.getPosition();

        final int diffX = Math.abs(sp[RenderConstants.X] - getPosition()[RenderConstants.X]);
        final int allowance = (getWidth() / 2) + (GameplayConstants.SQUARE_LENGTH() / 2);

        return diffX < allowance;
    }

    public boolean isHighlighted(final int[] cp) {
        final int diffX = Math.abs(cp[RenderConstants.X] - getPosition()[RenderConstants.X]);
        final int diffY = Math.abs(cp[RenderConstants.Y] - getPosition()[RenderConstants.Y]);

        final int allowanceX = getWidth() / 2;
        final int allowanceY = GameplayConstants.PLATFORM_HEIGHT() / 2;

        return diffX <= allowanceX && diffY <= allowanceY;
    }

    public void changeWidth(final int deltaWidth) {
        width += deltaWidth;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public void render(
            final Camera camera, final Graphics g,
            final JBJGLGameDebugger debugger
    ) {
        final int zoomFactor = camera.isZoomedIn() ? 1 : 2;
        final int width = TechnicalSettings.pixelLockNumber(
                getWidth() / zoomFactor
        );
        final int height = TechnicalSettings.pixelLockNumber(
                GameplayConstants.PLATFORM_HEIGHT() / zoomFactor
        );
        final int rawHalfWidth = getWidth() / 2;
        final int rawHalfHeight = GameplayConstants.PLATFORM_HEIGHT() / 2;
        final int pixel = TechnicalSettings.getPixelSize();

        final int[] renderPosition = camera.getRenderPosition(
                getPosition()[RenderConstants.X] - rawHalfWidth,
                getPosition()[RenderConstants.Y] - rawHalfHeight
        );

        JBJGLImage platform = JBJGLImage.create(width, height);
        Graphics pg = platform.getGraphics();

        pg.setColor(TLColors.BLACK());
        pg.fillRect(0, 0, width, height);

        Color platformColor = TLColors.PLATFORM();
        pg.setColor(platformColor);
        pg.fillRect(
                pixel, pixel,
                width - (2 * pixel), height - (2 * pixel)
        );

        g.drawImage(
                platform, renderPosition[RenderConstants.X],
                renderPosition[RenderConstants.Y], null
        );
    }

    public void renderForEditor(
            final Camera camera, final Graphics g,
            final JBJGLGameDebugger debugger
    ) {
        Entity selected = Editor.getSelectedEntity();

        if (selected instanceof Platform selectedPlatform && selectedPlatform.equals(this)) {
            final int zoomFactor = camera.isZoomedIn() ? 1 : 2;
            final int pixel = TechnicalSettings.getPixelSize();

            final int width = TechnicalSettings.pixelLockNumber(
                    getWidth() / zoomFactor
            ) + (pixel * 2);
            final int height = TechnicalSettings.pixelLockNumber(
                    GameplayConstants.PLATFORM_HEIGHT() / zoomFactor
            ) + (pixel * 2);

            final int rawHalfWidth = getWidth() / 2;
            final int rawHalfHeight = GameplayConstants.PLATFORM_HEIGHT() / 2;

            final int[] renderPosition = camera.getRenderPosition(
                    getPosition()[RenderConstants.X] - rawHalfWidth,
                    getPosition()[RenderConstants.Y] - rawHalfHeight
            );

            JBJGLImage selectionOutline = JBJGLImage.create(width, height);
            Graphics sog = selectionOutline.getGraphics();

            sog.setColor(TLColors.DEBUG());
            sog.fillRect(0, 0, width, height);

            g.drawImage(
                    selectionOutline, renderPosition[RenderConstants.X] - pixel,
                    renderPosition[RenderConstants.Y] - pixel, null
            );
        }

        render(camera, g, debugger);
    }
}

package com.jordanbunke.translation.gameplay.entities;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.swatches.Swatches;

import java.awt.*;

public class Animation extends Entity {
    private final Type type;
    private final int specialField;
    private final Color mainColor;

    private int tickAge;

    public enum Type {
        LEFT_TELEPORTATION, RIGHT_TELEPORTATION,
        LOAD,
        CRUSH,
        DISAPPEARANCE,
        JUMP;

        private static final int GENERAL_MAX_TICK_AGE = 20;

        private JBJGLImage getFrame(final Animation animation, final int zoomFactor) {
            final double fraction = animation.tickAge / (double) maxTickAge();
            final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
            final int pixel = TechnicalSettings.getPixelSize();

            JBJGLImage frame;

            switch (this) {
                case CRUSH -> {
                    final int maxDepth = 4 * sideLength;
                    final int animationDepth = 5 * sideLength;
                    final int drawAtY = TechnicalSettings.pixelLockNumber(
                            (int)Math.round(maxDepth * fraction)
                    );

                    int opacity = Swatches.SHADOW() - (int)(Swatches.SHADOW() * fraction);

                    frame = JBJGLImage.create(sideLength, animationDepth);
                    Graphics fg = frame.getGraphics();

                    fg.setColor(Swatches.colorAtOpacity(animation.mainColor, opacity));
                    fg.fillRect(0, drawAtY, sideLength, sideLength);

                    return frame;
                }
                case LOAD -> {
                    final Color c = Swatches.colorAtOpacity(
                            animation.mainColor,
                            Swatches.SHADOW() - (int)(Swatches.SHADOW() * fraction)
                    );
                    final int maxWidth = 3 * sideLength;
                    final int width = TechnicalSettings.pixelLockNumber((int)(maxWidth * fraction));
                    final int drawAtX = TechnicalSettings.pixelLockNumber((maxWidth - width) / 2);

                    frame = JBJGLImage.create(maxWidth, sideLength);
                    Graphics fg = frame.getGraphics();

                    fg.setColor(c);
                    fg.fillRect(drawAtX, pixel, width, sideLength - (2 * pixel));

                    return frame;
                }
                case LEFT_TELEPORTATION, RIGHT_TELEPORTATION -> {
                    final int length = Math.max(TechnicalSettings.pixelLockNumber(
                            (animation.specialField / zoomFactor) + sideLength
                    ), sideLength);
                    final int maxStrokeLength = length - (2 * pixel);
                    final Color streakColor = new Color(
                            255 - (int)(fraction * 255), 0, 0,
                            255 - (int)(fraction * 205)
                    );
                    final int strokeLength = TechnicalSettings.pixelLockNumber(
                            maxStrokeLength - (int)(maxStrokeLength * fraction)
                    );
                    final int strokeAtX = this == LEFT_TELEPORTATION
                            ? pixel
                            : pixel + (maxStrokeLength - strokeLength);

                    frame = JBJGLImage.create(length, sideLength);

                    if (strokeLength > 0) {
                        Graphics fg = frame.getGraphics();
                        fg.setColor(streakColor);
                        fg.fillRect(strokeAtX, pixel,
                                strokeLength, sideLength - (2 * pixel));
                    }

                    return frame;
                }
            }

            int opacity = Swatches.SHADOW() - (int)(Swatches.SHADOW() * fraction);
            frame = JBJGLImage.create(sideLength, sideLength);
            Graphics fg = frame.getGraphics();

            fg.setColor(Swatches.colorAtOpacity(animation.mainColor, opacity));
            fg.fillRect(0, 0, sideLength, sideLength);

            return frame;
        }

        // Helpers
        private int maxTickAge() {
            return GENERAL_MAX_TICK_AGE;
        }
    }

    private Animation(
            final int x, final int y,
            final Type type, final int specialField,
            final Color mainColor
    ) {
        super(x, y);

        this.type = type;
        this.specialField = specialField;
        this.mainColor = mainColor;

        tickAge = 0;
    }

    public static Animation createTeleportationAnimation(
            final int[] startingPosition, final int endingX
    ) {
        final int startingX = startingPosition[RenderConstants.X];
        final int y = startingPosition[RenderConstants.Y];
        final int width = endingX - startingX;

        final Type type = width > 0 ? Type.RIGHT_TELEPORTATION : Type.LEFT_TELEPORTATION;

        return new Animation(
                Math.min(startingX, endingX), y, type, Math.abs(width), Player.getColor(Swatches.OPAQUE())
        );
    }

    public static Animation createJump(
            final int[] position
    ) {
        return new Animation(
                position[RenderConstants.X], position[RenderConstants.Y],
                Type.JUMP, 0, Player.getColor(Swatches.OPAQUE())
        );
    }

    public static Animation createLoad(
           final int[] position
    ) {
        return new Animation(
                position[RenderConstants.X] - GameplayConstants.SQUARE_LENGTH(),
                position[RenderConstants.Y],
                Type.LOAD, 0, Player.getColor(Swatches.OPAQUE())
        );
    }

    public static Animation createDisappearance(
            final int[] position, final Sentry.Role role
    ) {
        return new Animation(
                position[RenderConstants.X], position[RenderConstants.Y],
                Type.DISAPPEARANCE, 0, role.getColor(Swatches.OPAQUE())
        );
    }

    public static Animation createCrush(
            final int[] position, final Sentry.Role role
    ) {
        // TODO - consider making force a special field that affects animation depth and/or duration
        return new Animation(
                position[RenderConstants.X],
                position[RenderConstants.Y] + GameplayConstants.PLATFORM_HEIGHT(),
                Type.CRUSH, 0, role.getColor(Swatches.OPAQUE())
        );
    }

    public void age() {
        tickAge++;
    }

    public boolean hasAgedOut() {
        return tickAge >= type.maxTickAge();
    }

    @Override
    public void render(
            final Camera camera, final Graphics g, final JBJGLGameDebugger debugger
    ) {
        final int zoomFactor = camera.isZoomedIn() ? 1 : 2;
        final int rawHalfLength = GameplayConstants.SQUARE_LENGTH() / 2;

        final int[] renderPosition = camera.getRenderPosition(
                getPosition()[RenderConstants.X] - rawHalfLength,
                getPosition()[RenderConstants.Y] - rawHalfLength
        );

        JBJGLImage frame = type.getFrame(this, zoomFactor);

        g.drawImage(
                frame, renderPosition[RenderConstants.X],
                renderPosition[RenderConstants.Y], null
        );
    }
}

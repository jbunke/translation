package com.jordanbunke.translation.gameplay;

import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.gameplay.entities.Entity;
import com.jordanbunke.translation.settings.TechnicalSettings;

public class Camera extends HasPosition {
    private static final int CAMERA_MOVEMENT_SPEED = TechnicalSettings.getPixelSize();

    private final Entity target;
    private FollowMode followMode;
    private boolean zoom; // true - zoomed in, false - zoomed out

    private boolean isMovingLeft;
    private boolean isMovingRight;
    private boolean isMovingUp;
    private boolean isMovingDown;

    public enum FollowMode {
        STEADY, GLUED, FIXED;

        public FollowMode next() {
            return switch (this) {
                case STEADY -> GLUED;
                case GLUED -> FIXED;
                case FIXED -> STEADY;
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case STEADY -> "STEADY FOLLOW";
                case GLUED -> "GLUED TO PLAYER";
                case FIXED -> "DOESN'T FOLLOW";
            };
        }
    }

    private Camera(
            final int initialX, final int initialY,
            final Entity target, final FollowMode followMode
    ) {
        super(initialX, initialY);
        this.target = target;
        this.followMode = followMode;
        this.zoom = true;

        this.isMovingLeft = false;
        this.isMovingRight = false;
        this.isMovingUp = false;
        this.isMovingDown = false;
    }

    public static Camera create(
            final Entity target, final FollowMode followMode
    ) {
        final int[] position = target.getPosition();

        return new Camera(
                position[RenderConstants.X], position[RenderConstants.Y], target, followMode
        );
    }

    public void update() {
        follow();
        manualMovement();
    }

    private void follow() {
        final int[] tp = target.getPosition();

        switch (followMode) {
            case GLUED -> setPosition(tp[RenderConstants.X], tp[RenderConstants.Y]);
            case STEADY -> {
                final int[] deltas = calculateDeltas();
                incrementX(deltas[RenderConstants.X]);
                incrementY(deltas[RenderConstants.Y]);
            }
        }
    }

    private void manualMovement() {
        if (followMode != FollowMode.FIXED)
            return;

        if (isMovingLeft)
            incrementX(-CAMERA_MOVEMENT_SPEED);
        if (isMovingRight)
            incrementX(CAMERA_MOVEMENT_SPEED);
        if (isMovingUp)
            incrementY(-CAMERA_MOVEMENT_SPEED);
        if (isMovingDown)
            incrementY(CAMERA_MOVEMENT_SPEED);
    }

    private int[] calculateDeltas() {
        final int[] deltas = new int[2];
        final int[] tp = target.getPosition();
        final int[] diff = new int[] {
                tp[RenderConstants.X] - getPosition()[RenderConstants.X],
                tp[RenderConstants.Y] - getPosition()[RenderConstants.Y]
        };

        deltas[RenderConstants.X] = (int)(Math.signum(diff[RenderConstants.X]) *
                Math.ceil(Math.sqrt(Math.abs(diff[RenderConstants.X]))));
        deltas[RenderConstants.Y] = (int)(Math.signum(diff[RenderConstants.Y]) *
                Math.ceil(Math.sqrt(Math.abs(diff[RenderConstants.Y]))));

        return deltas;
    }

    public int[] getRenderPosition(final int coreX, final int coreY) {
        final int[] screenMiddle = new int[] {
                TechnicalSettings.getWidth() / 2,
                TechnicalSettings.getHeight() / 2
        };
        final int[] camOffset = new int[] {
                screenMiddle[RenderConstants.X] - getPosition()[RenderConstants.X],
                screenMiddle[RenderConstants.Y] - getPosition()[RenderConstants.Y]
        };

        final int zoomFactor = isZoomedIn() ? 1 : 2;

        final int[] unadjustedRenderPosition = new int[] {
                screenMiddle[RenderConstants.X] +
                        (((camOffset[RenderConstants.X] + coreX) -
                                screenMiddle[RenderConstants.X]) / zoomFactor),
                screenMiddle[RenderConstants.Y] +
                        (((camOffset[RenderConstants.Y] + coreY) -
                                screenMiddle[RenderConstants.Y]) / zoomFactor)
        };

        return new int[] {
                TechnicalSettings.pixelLockNumber(
                        unadjustedRenderPosition[RenderConstants.X]),
                TechnicalSettings.pixelLockNumber(
                        unadjustedRenderPosition[RenderConstants.Y])
        };
    }

    private void updateMovementAfterFollowModeUpdate() {
        if (followMode != FollowMode.FIXED) {
            isMovingLeft = false;
            isMovingRight = false;
            isMovingUp = false;
            isMovingDown = false;
        }
    }

    // SETTERS
    public void toggleZoom() {
        zoom = !zoom;
    }

    public void toggleFollowMode() {
        followMode = followMode.next();

        updateMovementAfterFollowModeUpdate();
    }

    public void setIsMovingLeft(final boolean isMovingLeft) {
        this.isMovingLeft = isMovingLeft;
    }

    public void setIsMovingRight(final boolean isMovingRight) {
        this.isMovingRight = isMovingRight;
    }

    public void setIsMovingUp(final boolean isMovingUp) {
        this.isMovingUp = isMovingUp;
    }

    public void setIsMovingDown(final boolean isMovingDown) {
        this.isMovingDown = isMovingDown;
    }

    // GETTERS
    public boolean isZoomedIn() {
        return zoom;
    }

    public FollowMode getFollowMode() {
        return followMode;
    }
}

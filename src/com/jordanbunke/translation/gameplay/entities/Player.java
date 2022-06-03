package com.jordanbunke.translation.gameplay.entities;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.gameplay.level.LevelHUD;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.swatches.Swatches;

import java.awt.*;

public class Player extends SentientSquare {
    // CONSTANTS
    private static final int LEFT = -1, RIGHT = 1;
    private static final int DEFAULT_SPEED = 8;
    private static final int MAX_TELE_PHASE = 10;
    private static final int JUMP_ACCELERATION = 30;
    private static final int TELE_FACTOR = 4;
    private static final int GRAVITY_INC = 2;

    // Experimental
    public static final int TICKS_FOR_COMBO_TIMEOUT = 100;

    // FIELDS

    // positional
    private final int[] savedPosition;
    private final int[] lastPosition;

    // movement flags
    private boolean isLeft;
    private boolean isRight;
    private boolean isTele;
    private boolean positionIsSaved;

    // movement values
    private int lookingDirection;
    private int telePhase;
    private int gAcceleration;

    // experimental
    private int ticksSinceLastCrush;
    private int combo;

    private Player(final int x, final int y, final Level level) {
        super(x, y, level);

        lastPosition = new int[] { x, y };
        savedPosition = new int[] { x, y };

        isLeft = false;
        isRight = false;
        isTele = false;
        positionIsSaved = false;

        lookingDirection = RIGHT;
        setSpeed(DEFAULT_SPEED);
        telePhase = 0;
        gAcceleration = 0;

        ticksSinceLastCrush = -1;
        combo = 0;
    }

    public static Player create(final Level level) {
        final int[] platformPosition = level.getPlatforms().get(0).getPosition();
        final int x = platformPosition[RenderConstants.X];
        final int y = platformPosition[RenderConstants.Y] - GameplayConstants.SPAWN_HEIGHT;

        return new Player(x, y, level);
    }

    public static Color getColor(final int opacity) {
        return Swatches.PLAYER(opacity);
    }

    public void process(final JBJGLListener listener) {
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_LEFT),
                () -> {
                    isLeft = true;
                    lookingDirection = LEFT;
                }
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_RIGHT),
                () -> {
                    isRight = true;
                    lookingDirection = RIGHT;
                }
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_LEFT),
                () -> isLeft = false
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_RIGHT),
                () -> isRight = false
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.INIT_TELEPORT),
                () -> isTele = true
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.TELEPORT), this::teleport
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.JUMP), this::jump
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.DROP), this::drop
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.SAVE_POS), this::savePosition
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.LOAD_POS), this::loadPosition
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.TOGGLE_ZOOM),
                () -> getLevel().getCamera().toggleZoom()
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.TOGGLE_FOLLOW_MODE),
                () -> {
                    getLevel().getCamera().toggleFollowMode();
                    LevelHUD.initializeFollowModeUpdateCounter(getLevel());
                }
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_LEFT),
                () -> getLevel().getCamera().setIsMovingLeft(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_RIGHT),
                () -> getLevel().getCamera().setIsMovingRight(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_UP),
                () -> getLevel().getCamera().setIsMovingUp(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_DOWN),
                () -> getLevel().getCamera().setIsMovingDown(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_LEFT),
                () -> getLevel().getCamera().setIsMovingLeft(false)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_RIGHT),
                () -> getLevel().getCamera().setIsMovingRight(false)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_UP),
                () -> getLevel().getCamera().setIsMovingUp(false)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_DOWN),
                () -> getLevel().getCamera().setIsMovingDown(false)
        );
    }

    public void update() {
        comboUpdate();
        movementUpdate();
    }

    private void comboUpdate() {
        if (ticksSinceLastCrush >= 0)
            ticksSinceLastCrush++;

        if (ticksSinceLastCrush > TICKS_FOR_COMBO_TIMEOUT)
            combo = 0;
    }

    private void movementUpdate() {
        resetLastPosition();

        if (isLeft)
            incrementX(-getSpeed());

        if (isRight)
            incrementX(getSpeed());

        if (isTele && telePhase < MAX_TELE_PHASE)
            telePhase++;

        gravity();
        checkCrush();
    }

    private void gravity() {
        incrementY(-gAcceleration);

        if (gAcceleration > 0 || !isSupported())
            gAcceleration -= GRAVITY_INC;
    }

    private void checkCrush() {
        for (Sentry s : getLevel().getSentries()) {
            if (s.isAlive() && s.isCrushed(this)) {
                s.crush();

                // combo
                ticksSinceLastCrush = 0;
                combo++;
                getLevel().getStats().updateIfIsGreaterThanValue(LevelStats.MAX_COMBO, combo);
            }
        }
    }

    private boolean isSupported() {
        for (Platform p : getLevel().getPlatforms()) {
            if (p.supports(this)) {
                platformSupportLogic(p);
                return true;
            }
        }
        return false;
    }

    private boolean hasCover() {
        for (Platform p : getLevel().getPlatforms()) {
            if (p.covers(this))
                return true;
        }
        return false;
    }

    private boolean canLoadPosition() {
        final boolean isNotFallingWithoutCover =
                gAcceleration >= 0 || isSupported() || hasCover();
        return positionIsSaved && isNotFallingWithoutCover;
    }

    private void teleport() {
        if (telePhase == 0)
            return;

        resetLastPosition();
        isTele = false;
        incrementX(getTeleportationDistance());
        telePhase = 0;

        // trigger animation
        getLevel().getAnimations().add(
                Animation.createTeleportationAnimation(lastPosition, getPosition()[RenderConstants.X])
        );
    }

    private int getTeleportationDistance() {
        return TELE_FACTOR * lookingDirection * telePhase * getSpeed();
    }

    private void jump() {
        if (isSupported()) {
            gAcceleration = JUMP_ACCELERATION;
            getLevel().getAnimations().add(Animation.createJump(getPosition()));
        }
    }

    private void drop() {
        if (isSupported()) {
            // drop from platform
            incrementY(GameplayConstants.PLATFORM_HEIGHT() + 1);
        } else {
            // drop in air
            gAcceleration -= JUMP_ACCELERATION;
            getLevel().getAnimations().add(Animation.createJump(getPosition()));
        }
    }

    public void platformSupportLogic(final Platform platform) {
        gAcceleration = 0;
        setY(platform.getPosition()[RenderConstants.Y] - GameplayConstants.PLATFORM_HEIGHT());
    }

    private void resetLastPosition() {
        lastPosition[RenderConstants.X] = getPosition()[RenderConstants.X];
        lastPosition[RenderConstants.Y] = getPosition()[RenderConstants.Y];
    }

    private void savePosition() {
        savedPosition[RenderConstants.X] = getPosition()[RenderConstants.X];
        savedPosition[RenderConstants.Y] = getPosition()[RenderConstants.Y];

        positionIsSaved = true;
    }

    private void loadPosition() {
        if (canLoadPosition()) {
            setPosition(savedPosition[RenderConstants.X], savedPosition[RenderConstants.Y]);

            positionIsSaved = false;

            getLevel().getAnimations().add(Animation.createLoad(getPosition()));
        }
    }

    // Setters and modifiers
    public void invertSavedY() {
        savedPosition[RenderConstants.Y] *= -1;
    }

    public void incrementSavedY(final int deltaSavedY) {
        savedPosition[RenderConstants.Y] += deltaSavedY;
    }

    public void mulGAcceleration(final int multiplier) {
        gAcceleration *= multiplier;
    }

    public void incrementGAcceleration(final int increment) {
        gAcceleration += increment;
    }

    // Getters
    public int[] getLastPosition() {
        return lastPosition;
    }

    @Override
    public void render(Camera camera, Graphics g, JBJGLGameDebugger debugger) {
        final int zoomFactor = camera.isZoomedIn() ? 1 : 2;
        final int rawHalfLength = GameplayConstants.SQUARE_LENGTH() / 2;

        final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
        final int pixel = TechnicalSettings.getPixelSize();

        final int[] renderPosition = camera.getRenderPosition(
                getPosition()[RenderConstants.X] - rawHalfLength,
                getPosition()[RenderConstants.Y] - rawHalfLength
        );

        JBJGLImage square = JBJGLImage.create(sideLength, sideLength);
        Graphics sg = square.getGraphics();

        sg.setColor(isHighlighted() ? Swatches.WHITE() : Swatches.BLACK());
        sg.fillRect(0, 0, sideLength, sideLength);

        Color centerColor = Player.getColor(Swatches.OPAQUE());

        sg.setColor(centerColor);
        sg.fillRect(
                pixel, pixel,
                sideLength - (2 * pixel), sideLength - (2 * pixel)
        );

        // shadows
        final int shadowLength = sideLength - (2 * pixel);
        JBJGLImage shadow = JBJGLImage.create(sideLength, sideLength);
        Graphics shg = shadow.getGraphics();

        shg.setColor(Player.getColor(Swatches.SHADOW()));
        shg.fillRect(pixel, pixel, shadowLength, shadowLength);

        int[] shadowRenderPosition;

        // load position shadow
        if (canLoadPosition()) {
            shadowRenderPosition = camera.getRenderPosition(
                    savedPosition[RenderConstants.X] - rawHalfLength,
                    savedPosition[RenderConstants.Y] - rawHalfLength
            );

            g.drawImage(
                    shadow, shadowRenderPosition[RenderConstants.X],
                    shadowRenderPosition[RenderConstants.Y], null
            );
        }

        // teleportation shadow
        if (isTele) {
            shadowRenderPosition = camera.getRenderPosition(
                    (getPosition()[RenderConstants.X] +
                            getTeleportationDistance()) - rawHalfLength,
                    getPosition()[RenderConstants.Y] - rawHalfLength
            );

            g.drawImage(
                    shadow, shadowRenderPosition[RenderConstants.X],
                    shadowRenderPosition[RenderConstants.Y], null
            );
        }

        g.drawImage(
                square, renderPosition[RenderConstants.X],
                renderPosition[RenderConstants.Y], null
        );
    }

    public int getCombo() {
        return combo;
    }

    public int getTicksSinceLastCrush() {
        return ticksSinceLastCrush;
    }
}

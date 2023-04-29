package com.jordanbunke.translation.gameplay.entities;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.sound.Sounds;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Sentry extends SentientSquare {
    public static final int SHOVE_FACTOR = TechnicalSettings.getPixelSize();
    public static final int MAX_CHILDREN_SPAWNABLE = 5;
    public static final int MAX_PLATFORM_WIDTH = 480;
    public static final int GRAVITY_FACTOR = 3;

    public static final int MAX_SENTRY_SPEED = 14;
    public static final int LEFT = -1, RIGHT = 1;

    private static final int STANDARD_TICK_CYCLE = 200;
    public static final int ANIMATION_CYCLE = STANDARD_TICK_CYCLE / 4;

    public static final int SPAWN_CYCLE = STANDARD_TICK_CYCLE;
    public static final int REVIVAL_CYCLE = STANDARD_TICK_CYCLE * 2;
    public static final int NOMADIC_CYCLE = STANDARD_TICK_CYCLE;

    public enum Role {
        PUSHER,
        SHOVER,
        PULLER,
        DROPPER,
        BOOSTER,
        BOUNCER,
        FEATHER,
        ANCHOR,
        SLIDER,
        CRUMBLER,
        BUILDER,
        REPELLER,
        MAGNET,
        INVERTER,
        NECROMANCER,
        SPAWNER,
        COWARD,
        NOMAD,
        RANDOM
        ;

        public boolean isDeterministic() {
            return switch (this) {
                case PUSHER, PULLER, SLIDER, CRUMBLER, MAGNET,
                        FEATHER, ANCHOR, BUILDER, SHOVER,
                        BOOSTER, BOUNCER, DROPPER,
                        REPELLER, INVERTER, NECROMANCER -> true;
                default -> false;
            };
        }

        public boolean isSightDependent() {
            return switch (this) {
                case PUSHER, PULLER, SLIDER, COWARD, CRUMBLER, BUILDER, SHOVER,
                        BOOSTER, BOUNCER, DROPPER, REPELLER, INVERTER -> true;
                default -> false;
            };
        }

        public Color getColor(final int opacity) {
            return switch (this) {
                case PUSHER -> new Color(0, 0, 255, opacity);
                case SLIDER -> new Color(0, 255, 0, opacity);
                case PULLER -> new Color(0, 150, 255, opacity);
                case COWARD -> new Color(255, 255, 255, opacity);
                case CRUMBLER -> new Color(255, 255, 0, opacity);
                case MAGNET -> new Color(255, 100, 255, opacity);
                case FEATHER -> new Color(100, 100, 255, opacity);
                case ANCHOR -> new Color(255, 255, 150, opacity);
                case BUILDER -> new Color(0, 150, 0, opacity);
                case SHOVER -> new Color(0, 0, 150, opacity);
                case BOOSTER -> new Color(255, 100, 100, opacity);
                case BOUNCER -> new Color(100, 255, 100, opacity);
                case DROPPER -> new Color(100, 0, 0, opacity);
                case REPELLER -> new Color(255, 100, 0, opacity);
                case NECROMANCER -> new Color(100, 100, 100, opacity);
                case SPAWNER -> new Color(150, 100, 0, opacity);
                case INVERTER -> new Color(150, 0, 150, opacity);
                case NOMAD -> new Color(140, 50, 10, opacity);
                case RANDOM -> new Color(150, 150, 150, opacity);
            };
        }

        private void behave(final Sentry sentry) {
            final Player player = sentry.getLevel().getPlayer();
            final List<Platform> platforms = sentry.getLevel().getPlatforms();
            final List<Sentry> sentries = sentry.getLevel().getSentries();
            final List<Animation> animations = sentry.getLevel().getAnimations();

            switch (this) {
                case PUSHER -> {
                    Sounds.pusherSeesPlayer();
                    player.incrementX(sentry.getSpeed() * sentry.direction);
                }
                case SHOVER -> {
                    Sounds.shoverSeesPlayer();
                    player.incrementX(sentry.getSpeed() * sentry.direction * SHOVE_FACTOR);
                }
                case PULLER -> {
                    Sounds.pullerSeesPlayer();
                    player.incrementX(-sentry.getSpeed() * sentry.direction);
                }
                case DROPPER -> player.incrementY(GameplayConstants.SQUARE_LENGTH() + 1);
                case SLIDER -> {
                    Sounds.sliderSeesPlayer();
                    sentry.platform.incrementX(-(sentry.getSpeed() * sentry.direction));
                }
                case CRUMBLER -> {
                    if (sentry.platform.getWidth() > GameplayConstants.SQUARE_LENGTH()) {
                        Sounds.crumblerSeesPlayer();

                        sentry.platform.changeWidth(-sentry.getSpeed());
                        sentry.platform.incrementX(
                                sentry.direction * (-sentry.getSpeed() / 2)
                        );
                    }
                }
                case BUILDER -> {
                    if (sentry.platform.getWidth() < MAX_PLATFORM_WIDTH) {
                        Sounds.builderSeesPlayer();

                        sentry.platform.changeWidth(sentry.getSpeed());
                        sentry.platform.incrementX(
                                sentry.direction * (sentry.getSpeed() / 2)
                        );
                    }
                }
                case BOOSTER -> player.mulGAcceleration(2);
                case BOUNCER -> player.mulGAcceleration(-1);
                case REPELLER -> {
                    Sounds.repellerSeesPlayer();

                    for (Platform p : platforms) {
                        if (p.equals(sentry.platform))
                            continue;

                        final int[] diff = new int[] {
                                p.getPosition()[RenderConstants.X] - sentry.platform.getPosition()[RenderConstants.X],
                                p.getPosition()[RenderConstants.Y] - sentry.platform.getPosition()[RenderConstants.Y]
                        };
                        p.incrementX(sentry.getSpeed() * (int)Math.signum(
                                diff[RenderConstants.X]));
                        p.incrementY((sentry.getSpeed() / 2) * (int)Math.signum(
                                diff[RenderConstants.Y]));
                    }

                    for (Sentry s : sentries) {
                        if (s.equals(sentry))
                            continue;

                        s.fix();
                    }
                }
                case COWARD, NOMAD -> {
                    sentry.counter++;
                    sentry.counter %= NOMADIC_CYCLE;

                    if (this == COWARD || sentry.counter == 0) {
                        Platform candidate;

                        do {
                            candidate = Utility.randomElementFromList(platforms);
                        } while (candidate == null || candidate.equals(sentry.platform));

                        sentry.getLevel().getAnimations().add(Animation.createDisappearance(
                                sentry.getPosition(), sentry.role
                        ));
                        sentry.setPlatform(candidate);
                    }
                }
                case INVERTER -> {
                    final int playerY = player.getPosition()[RenderConstants.Y];

                    // player adjustments
                    player.setY(-1 * playerY);
                    player.invertSavedY();
                    player.mulGAcceleration(-1);
                    final int adjustment = playerY - player.getPosition()[RenderConstants.Y];
                    player.incrementY(adjustment);
                    player.incrementSavedY(adjustment);

                    // platform adjustments
                    for (Platform p : platforms) {
                        p.setY(-1 * p.getPosition()[RenderConstants.Y]);
                        p.incrementY(adjustment);
                    }

                    // sentry adjustments
                    for (Sentry s : sentries) {
                        s.setY(-1 * s.getPosition()[RenderConstants.Y]);
                        s.incrementY(
                                -1 * GameplayConstants.PLATFORM_HEIGHT() *
                                        GameplayConstants.SQUARE_LENGTH()
                        );
                        s.incrementY(adjustment);
                    }

                    // animation adjustments
                    for (Animation a : animations) {
                        a.setY(-1 * a.getPosition()[RenderConstants.Y]);
                        a.incrementY(adjustment);
                    }
                }
                case MAGNET -> {
                    sentry.counter++;
                    sentry.counter %= ANIMATION_CYCLE;

                    player.incrementX((int)(sentry.getSpeed() * Math.signum(
                            sentry.getPosition()[RenderConstants.X] -
                                    player.getPosition()[RenderConstants.X])));
                }
                case SPAWNER -> {
                    sentry.counter++;
                    sentry.counter %= SPAWN_CYCLE;

                    for (int i = 0; i < sentry.children.size(); i++) {
                        if (!sentry.children.get(i).isAlive()) {
                            sentry.children.remove(i);
                            i--;
                        }
                    }

                    if (sentry.counter == 0 && sentry.children.size() < MAX_CHILDREN_SPAWNABLE) {
                        final int speed = Utility.boundedRandom(1, (MAX_SENTRY_SPEED / 2) + 1) * 2;
                        Platform platform;

                        do {
                            platform = Utility.randomElementFromList(platforms);
                        } while (platform == null || platform.equals(sentry.platform));

                        Sentry child = Sentry.create(
                                sentry.secondary, sentry.secondary,
                                sentry.getLevel(), platform,
                                speed * Utility.coinToss(LEFT, RIGHT));

                        sentry.addChild(child);
                    }
                }
                case NECROMANCER -> {
                    sentry.counter++;
                    sentry.counter %= REVIVAL_CYCLE;

                    if (sentry.counter == 0) {
                        for (Sentry s : sentries) {
                            if (s.equals(sentry))
                                continue;

                            if (!s.isAlive()) {
                                s.alive = true;
                                sentry.addChild(s);
                                break;
                            }
                        }
                    }
                }
                case RANDOM -> {
                    Role role;

                    do {
                        role = Utility.randomElementFromArray(Role.values());
                    } while (role == RANDOM);

                    sentry.role = role;
                    if (role != SPAWNER)
                        sentry.secondary = role;
                }
                case ANCHOR, FEATHER -> {
                    sentry.counter++;
                    sentry.counter %= ANIMATION_CYCLE;

                    player.incrementGAcceleration(
                            GRAVITY_FACTOR * (this == ANCHOR ? -1 : 1)
                    );
                }
            }
        }

        public Role next() {
            return iterate(false);
        }

        public Role previous() {
            return iterate(true);
        }

        private Role iterate(final boolean backwards) {
            final int TOTAL = values().length;
            final int index = (ordinal() + (backwards ? -1 : 1)) % TOTAL;

            final int normalizedIndex = index < 0 ? index + TOTAL : index;

            return values()[normalizedIndex];
        }
    }

    private Role role;
    private Platform platform;

    private boolean alive;
    private int direction;

    private boolean seesPlayer;

    // special fields
    private Role secondary;
    private final List<Sentry> children;
    private int counter;

    private Sentry(
            final int x, final int y,
            final Role role, final Role secondary,
            final Level level, final Platform platform,
            final int direction, final int speed
    ) {
        super(x, y, level);

        this.role = role;
        this.secondary = secondary;
        this.platform = platform;

        this.counter = 0;
        this.children = new ArrayList<>();

        this.direction = direction;
        setSpeed(speed);
        alive = true;
        seesPlayer = false;
    }

    public static Sentry create(
            final Role role, final Role secondary,
            final Level level, final Platform platform,
            final int initialMovement
    ) {
        final int x = platform.getPosition()[RenderConstants.X];
        final int y = platform.getPosition()[RenderConstants.Y] - GameplayConstants.PLATFORM_HEIGHT();
        final int direction = initialMovement / Math.abs(initialMovement);
        final int speed = Math.abs(Math.min(initialMovement, MAX_SENTRY_SPEED));

        return new Sentry(x, y, role, secondary, level, platform, direction, speed);
    }

    void crush(final boolean untethered) {
        if (untethered)
            Sounds.reanimatedSentryUntethered();
        else
            Sounds.sentryCrushed();

        alive = false;

        // animation
        getLevel().getAnimations().add(Animation.createCrush(
                getPosition(), role));

        // transitive kills for necromancer revivals ONLY
        if (role != Role.NECROMANCER)
            return;

        for (Sentry child : children)
            if (child.isAlive())
                child.crush(true);
    }

    boolean isCrushed(final Player player) {
        final boolean playerWasAbove = getPosition()[RenderConstants.Y] >
                player.getLastPosition()[RenderConstants.Y];
        final boolean playerIsNotAbove = player.getPosition()[RenderConstants.Y] >
                getPosition()[RenderConstants.Y] - GameplayConstants.SQUARE_LENGTH();
        final boolean withinXAllowance =
                Math.abs(player.getPosition()[RenderConstants.X] -
                        getPosition()[RenderConstants.X]) <
                        GameplayConstants.SQUARE_LENGTH();
        final boolean playerIsMovingDownward = player.getPosition()[RenderConstants.Y] >
                player.getLastPosition()[RenderConstants.Y];
        return playerWasAbove && playerIsNotAbove && playerIsMovingDownward && withinXAllowance;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isHighlighted(final int[] cp) {
        final int diffX = Math.abs(cp[RenderConstants.X] - getPosition()[RenderConstants.X]);
        final int diffY = Math.abs(cp[RenderConstants.Y] - getPosition()[RenderConstants.Y]);

        final int allowance = GameplayConstants.SQUARE_LENGTH() / 2;

        return diffX <= allowance && diffY <= allowance;
    }

    public void update() {
        if (alive) {
            patrol();
            behave();
        }
    }

    private void patrol() {
        final int diffX = getPosition()[RenderConstants.X] -
                platform.getPosition()[RenderConstants.X];
        final boolean isWithinBounds = Math.abs(diffX) <
                (platform.getWidth() / 2) - (GameplayConstants.SQUARE_LENGTH() / 2);
        final boolean isHeadingTowardsMiddle = (diffX / direction) < 0;

        if (isWithinBounds || isHeadingTowardsMiddle) {
            // move along platform
            incrementX(getSpeed() * direction);
        } else {
            // turn around
            direction *= -1;
        }

        fix();
    }

    private void fix() {
        // fix sentry to platform in case it has moved
        setY(platform.getPosition()[RenderConstants.Y] - GameplayConstants.PLATFORM_HEIGHT());

        final int leftAllowanceLimit =
                platform.getPosition()[RenderConstants.X] - (platform.getWidth() / 2);
        final int rightAllowanceLimit =
                platform.getPosition()[RenderConstants.X] + (platform.getWidth() / 2);

        if (getPosition()[RenderConstants.X] < leftAllowanceLimit)
            setX(leftAllowanceLimit + (GameplayConstants.SQUARE_LENGTH() / 2));

        if (getPosition()[RenderConstants.X] > rightAllowanceLimit)
            setX(rightAllowanceLimit - (GameplayConstants.SQUARE_LENGTH() / 2));
    }

    private void behave() {
        if (!role.isSightDependent() || seesPlayer())
            role.behave(this);
    }

    public boolean seesPlayer() {
        final boolean atEyeHeightY = Math.abs(
                getLevel().getPlayer().getPosition()[RenderConstants.Y] -
                        getPosition()[RenderConstants.Y]) < GameplayConstants.SQUARE_LENGTH();
        final boolean lookingInRightDirection = (
                getLevel().getPlayer().getPosition()[RenderConstants.X] -
                        getPosition()[RenderConstants.X]) / direction > 0;
        final boolean seesPlayer = atEyeHeightY && lookingInRightDirection;

        // sightings should be incremented as this is the first tick of this sighting
        if ((this.seesPlayer != seesPlayer) && seesPlayer)
            getLevel().getStats().increment(LevelStats.SIGHTINGS);

        this.seesPlayer = seesPlayer;
        return seesPlayer;
    }

    private void setPlatform(final Platform platform) {
        this.platform = platform;
        fix();
    }

    private void addChild(final Sentry sentry) {
        children.add(sentry);
        getLevel().addSentry(sentry);
    }

    @Override
    public void render(Camera camera, Graphics g, JBJGLGameDebugger debugger) {
        if (!alive)
            return;

        final int zoomFactor = camera.isZoomedIn() ? 1 : 2;
        final int rawHalfLength = GameplayConstants.SQUARE_LENGTH() / 2;

        final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
        final int pixel = TechnicalSettings.getPixelSize();

        final int[] renderPosition = camera.getRenderPosition(
                getPosition()[RenderConstants.X] - rawHalfLength,
                getPosition()[RenderConstants.Y] - rawHalfLength
        );

        // line of sight
        if (role.isSightDependent()) {
            g.setColor(seesPlayer
                    ? role.getColor(TLColors.SHADOW())
                    : TLColors.WHITE(TLColors.FAINT())
            );

            final int sightLineX = direction == LEFT ? 0 : renderPosition[RenderConstants.X] + sideLength;
            final int sightLineWidth = direction == LEFT
                    ? renderPosition[RenderConstants.X]
                    : TechnicalSettings.getWidth() - sightLineX;

            g.fillRect(sightLineX, renderPosition[RenderConstants.Y], sightLineWidth, sideLength);
        } else {
            switch (role) {
                case SPAWNER, NECROMANCER, NOMAD -> {
                    final JBJGLImage halo = drawHaloEffect(camera.isZoomedIn(), counter, role, secondary);

                    g.drawImage(halo,
                            renderPosition[RenderConstants.X] - pixel,
                            renderPosition[RenderConstants.Y] - pixel, null);

                    if (role == Role.NECROMANCER && GameplaySettings.isShowingNecromancerTethers()
                            && children.size() > 0)
                        for (Sentry revived : children)
                            if (revived.isAlive()) {
                                // Necromancer tethers
                                final int distance = Utility.distance(this, revived);
                                for (int i = 0; i < distance; i += pixel) {
                                    final double f = i / (double)distance;
                                    final int[] point = Utility.pointAlongLine(this, revived, f);

                                    final int[] pointRenderPosition = camera.getRenderPosition(
                                            TechnicalSettings.pixelLockNumber(point[RenderConstants.X]),
                                            TechnicalSettings.pixelLockNumber(point[RenderConstants.Y])
                                    );

                                    g.setColor(Utility.colorAlongGradient(
                                            role.getColor(TLColors.FAINT()),
                                            revived.role.getColor(TLColors.OPAQUE()), f));
                                    g.fillRect(
                                            pointRenderPosition[RenderConstants.X],
                                            pointRenderPosition[RenderConstants.Y],
                                            pixel, pixel
                                    );
                                }
                            }
                }
                case MAGNET, ANCHOR, FEATHER -> {
                    final JBJGLImage effect = drawSpatialEffect(camera.isZoomedIn(), counter, role);

                    g.drawImage(effect,
                            renderPosition[RenderConstants.X] - (5 * pixel),
                            renderPosition[RenderConstants.Y] - (5 * pixel), null);
                }
            }
        }

        renderSquare(camera, g);
    }

    public void renderSquare(Camera camera, Graphics g) {
        final int rawHalfLength = GameplayConstants.SQUARE_LENGTH() / 2;

        final int[] renderPosition = camera.getRenderPosition(
                getPosition()[RenderConstants.X] - rawHalfLength,
                getPosition()[RenderConstants.Y] - rawHalfLength
        );

        g.drawImage(drawSquare(camera.isZoomedIn(), isHighlighted(), role),
                renderPosition[RenderConstants.X],
                renderPosition[RenderConstants.Y], null);
    }

    public static JBJGLImage drawForEditorHUD(
            final boolean selected, final Role role, final Role secondary,
            final int counter
    ) {
        final int zoomFactor = selected ? 1 : 2;

        final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
        final int pixel = TechnicalSettings.getPixelSize();

        final int dimension = sideLength + (selected ? 10 * pixel : 0);

        final JBJGLImage composition = JBJGLImage.create(dimension, dimension);
        final Graphics g = composition.getGraphics();

        // effects
        if (selected) {
            final JBJGLImage effect;
            final int[] effectDrawPosition;

            switch (role) {
                // halo
                case SPAWNER, NECROMANCER, NOMAD -> {
                    effect = drawHaloEffect(true, counter, role, secondary);
                    effectDrawPosition = new int[] {
                            (composition.getWidth() - effect.getWidth()) / 2,
                            (composition.getHeight() - effect.getHeight()) / 2
                    };

                    g.drawImage(effect,
                            effectDrawPosition[RenderConstants.X],
                            effectDrawPosition[RenderConstants.Y], null);
                }

                // spatial
                case MAGNET, ANCHOR, FEATHER -> {
                    effect = drawSpatialEffect(true, counter, role);
                    effectDrawPosition = new int[] { 0, 0 };

                    g.drawImage(effect,
                            effectDrawPosition[RenderConstants.X],
                            effectDrawPosition[RenderConstants.Y], null);
                }
            }
        }

        // square
        final JBJGLImage square = drawSquare(selected, false, role);
        final int[] drawPosition = new int[] {
                (composition.getWidth() - square.getWidth()) / 2,
                (composition.getHeight() - square.getHeight()) / 2
        };

        g.drawImage(square, drawPosition[RenderConstants.X],
                drawPosition[RenderConstants.Y], null);

        g.dispose();

        return composition;
    }

    private static JBJGLImage drawSquare(
            final boolean isZoomedIn, final boolean isHighlighted,
            final Role role
    ) {
        final int zoomFactor = isZoomedIn ? 1 : 2;
        final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
        final int pixel = TechnicalSettings.getPixelSize();

        final JBJGLImage square = JBJGLImage.create(sideLength, sideLength);
        final Graphics sg = square.getGraphics();

        sg.setColor(isHighlighted ? TLColors.WHITE() : TLColors.BLACK());
        sg.fillRect(0, 0, sideLength, sideLength);

        final Color centerColor = role.getColor(TLColors.OPAQUE());

        sg.setColor(centerColor);
        sg.fillRect(
                pixel, pixel,
                sideLength - (2 * pixel), sideLength - (2 * pixel)
        );

        sg.dispose();

        return square;
    }

    private static JBJGLImage drawSpatialEffect(
            final boolean isZoomedIn, final int counter,
            final Role role
    ) {
        final int zoomFactor = isZoomedIn ? 1 : 2;
        final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
        final int pixel = TechnicalSettings.getPixelSize();

        final double fraction = counter / (double)ANIMATION_CYCLE;
        final int maxSideLength = sideLength + (10 * pixel);
        final int frameSideLength = sideLength + (TechnicalSettings.pixelLockNumber(
                (int)Math.round((0.5 - Math.abs(0.5 - fraction)) *
                        (maxSideLength - sideLength))
        ) * 2);
        final int drawAtXY = TechnicalSettings.pixelLockNumber(
                (maxSideLength - frameSideLength) / 2
        );

        final JBJGLImage effect = JBJGLImage.create(maxSideLength, maxSideLength);
        final Graphics eg = effect.getGraphics();
        eg.setColor(role.getColor(TLColors.SHADOW()));

        eg.fillRect(drawAtXY, drawAtXY, frameSideLength, frameSideLength);

        eg.dispose();

        return effect;
    }

    private static JBJGLImage drawHaloEffect(
            final boolean isZoomedIn, final int counter,
            final Role role, final Role secondary
    ) {
        final double[] QUARTERS = new double[] { 0.0, 0.25, 0.5, 0.75, 1.0 };

        final int zoomFactor = isZoomedIn ? 1 : 2;
        final int sideLength = GameplayConstants.SQUARE_LENGTH() / zoomFactor;
        final int pixel = TechnicalSettings.getPixelSize();

        final int haloLength = sideLength + (2 * pixel);
        final JBJGLImage halo = JBJGLImage.create(haloLength, haloLength);
        final Graphics hg = halo.getGraphics();

        hg.setColor(secondary.getColor(TLColors.SHADOW()));

        final int counterMax = switch (role) {
            case NOMAD -> NOMADIC_CYCLE;
            case SPAWNER -> SPAWN_CYCLE;
            case NECROMANCER -> REVIVAL_CYCLE;
            default -> STANDARD_TICK_CYCLE;
        };
        final double fraction = counter / (double)counterMax;

        final int[] qLengths = new int[4];
        final int[][] dimensions = new int[4][];
        final int[][] coords = new int[4][];

        for (int i = 0; i < qLengths.length; i++) {
            qLengths[i] = fraction >= QUARTERS[i + 1]
                    ? haloLength
                    : TechnicalSettings.pixelLockNumber(
                    (int)(haloLength * ((fraction - QUARTERS[i]) / (QUARTERS[i + 1] - QUARTERS[i])))
            );
            dimensions[i] = i % 2 == 0 // odd-even check
                    ? new int[] { pixel, qLengths[i] }
                    : new int[] { qLengths[i], pixel };
            coords[i] = switch (i) {
                case 0 -> new int[] { haloLength - pixel, 0 };
                case 1 -> new int[] { haloLength - qLengths[i], haloLength - pixel };
                case 2 -> new int[] { 0, haloLength - qLengths[i] };
                default -> new int[] { 0, 0 };
            };

            hg.fillRect(
                    coords[i][RenderConstants.X],
                    coords[i][RenderConstants.Y],
                    dimensions[i][RenderConstants.X],
                    dimensions[i][RenderConstants.Y]
            );
        }

        hg.dispose();

        return halo;
    }
}

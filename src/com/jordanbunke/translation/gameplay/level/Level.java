package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.editor.Editor;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.entities.Animation;
import com.jordanbunke.translation.gameplay.entities.Platform;
import com.jordanbunke.translation.gameplay.entities.Player;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.io.LevelIO;
import com.jordanbunke.translation.menus.MenuHelper;
import com.jordanbunke.translation.menus.MenuIDs;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.debug.DebuggerHandler;
import com.jordanbunke.translation.sound.Sounds;
import com.jordanbunke.translation.utility.Utility;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Level {
    public static final String EDITOR_LEVEL_NAME = "TESTING EDITOR LEVEL";
    private static final String EDITOR_LEVEL_HINT = "Editor levels must be completed to pass verification";
    private static final int
            PLAYER_OUT_OF_BOUNDS_AT = 1000,
            SENTRY_TOO_FAR_FROM_PLAYER = 6000,
            TICKS_AFTER_COMPLETION = 50;

    private Path filepath;

    private final String name, hint, parsedHint;
    private final LevelStats stats;
    private final PlatformSpec[] platformSpecs;
    private final SentrySpec[] sentrySpecs;

    private Camera camera;
    private Player player;
    private List<Platform> platforms;
    private List<Sentry> sentries;
    private List<Animation> animations;

    // metadata
    private final boolean deterministic;
    private final boolean blind;

    // completion logic
    private boolean completed;
    private int countdown;

    private Level(
            final String name, final String hint, final LevelStats stats,
            final PlatformSpec[] platformSpecs, final SentrySpec[] sentrySpecs,
            final Path filepath
    ) {
        this.filepath = filepath;

        this.name = name;
        this.hint = hint;
        this.parsedHint = parseHint();
        this.stats = stats;

        this.platformSpecs = platformSpecs;
        this.sentrySpecs = sentrySpecs;
        deterministic = determine();
        blind = checkIfBlind();

        launchLevel();
    }

    public static Level create(
            final String name, final String hint,
            final PlatformSpec[] platformSpecs, final SentrySpec[] sentrySpecs
    ) {
        return new Level(name, hint, LevelStats.createNew(), platformSpecs, sentrySpecs, null);
    }

    public static Level fromEditor() {
        return new Level(EDITOR_LEVEL_NAME, EDITOR_LEVEL_HINT,
                LevelStats.createNew(),
                Editor.definePlatformSpecsForLevel(),
                Editor.defineSentrySpecsForLevel(), null);
    }

    public static Level fromEditorValidated(
            final String name, final String hint, final Path filepath
    ) {
        return new Level(name, hint, LevelStats.createNew(),
                Editor.definePlatformSpecsForLevel(),
                Editor.defineSentrySpecsForLevel(), filepath);
    }

    public static Level load(
            final String name, final String hint,
            final LevelStats levelStats,
            final PlatformSpec[] platformSpecs, final SentrySpec[] sentrySpecs,
            final Path filepath
    ) {
        return new Level(name, hint, levelStats, platformSpecs, sentrySpecs, filepath);
    }

    public void updateFilepathFromCampaign(final Path folder, final String levelFilename) {
        filepath = folder.resolve(levelFilename);
    }

    public void save(final boolean reset) {
        if (filepath != null)
            LevelIO.writeLevel(this, reset);
    }

    public void launchLevel() {
        LevelHUD.initializeHintUpdateCounter(this);

        this.platforms = generatePlatforms();
        this.sentries = generateSentries();
        this.animations = new ArrayList<>();

        player = Player.create(this);
        camera = Camera.create(player, GameplaySettings.getDefaultFollowMode());

        completed = false;
        countdown = TICKS_AFTER_COMPLETION;
    }

    private List<Platform> generatePlatforms() {
        List<Platform> platforms = new ArrayList<>();

        for (PlatformSpec p : platformSpecs)
            platforms.add(p.generate());

        return platforms;
    }

    private List<Sentry> generateSentries() {
        List<Sentry> sentries = new ArrayList<>();

        for (SentrySpec s : sentrySpecs)
            sentries.add(s.generate(this, platforms));

        return sentries;
    }

    public void update() {
        // Increment tick counter for stats
        if (!completed)
            stats.increment(LevelStats.TIME);

        // player
        player.update();

        // sentries
        updateSentries();

        // animation aging
        for (int i = 0; i < animations.size(); i++) {
            animations.get(i).age();

            if (animations.get(i).hasAgedOut()) {
                animations.remove(i);
                i--;
            }
        }

        // HUD elements
        LevelHUD.update();

        camera.update();

        if (outOfBounds()) {
            stats.increment(LevelStats.FAILURES);
            stats.resetStat(LevelStats.TIME);
            stats.resetStat(LevelStats.SIGHTINGS);

            Sounds.failedLevel();

            DebuggerHandler.printMessage("Out of bounds - level reset", JBJGLGameDebugger.LOGIC_CHANNEL);

            launchLevel();
        }

        // level complete logic
        checkIfCompleted();
    }

    public void render(
            final Graphics g, final JBJGLGameDebugger debugger
    ) {
        // background
        g.drawImage(ImageAssets.getThemeBackground(), 0, 0, null);

        // animations
        for (Animation a : animations)
            a.render(camera, g, debugger);

        // platforms
        for (Platform p : platforms)
            p.render(camera, g, debugger);

        // sentries
        for (Sentry s : sentries)
            s.render(camera, g, debugger);

        // player
        player.render(camera, g, debugger);

        // HUD
        LevelHUD.render(this, g);
    }

    public void process(final JBJGLListener listener) {
        player.process(listener);
    }

    private void updateSentries() {
        Sounds.resetContinuousSentryData();

        final List<Sentry> snapshotOfSentries = List.copyOf(sentries);
        for (Sentry sentry : snapshotOfSentries)
            sentry.update();

        Sounds.processContinuousSentryData();
    }

    private void checkIfCompleted() {
        if (completed) {
            countdown--;

            if (countdown <= 0)
                levelComplete();
        } else {
            completed = sentries.stream().map(
                    x -> !x.isAlive()
            ).reduce(
                    (a, b) -> a && b
            ).orElse(false);
        }
    }

    private void levelComplete() {
        stats.finalizeStats();

        // save level and campaign
        if (!isEditorLevel()) {
            Translation.campaign.updateBeaten();
            LevelIO.writeCampaign(Translation.campaign, false);
            save(false);
        }

        Sounds.completedLevel();

        Translation.manager.setActiveStateIndex(Translation.LEVEL_COMPLETE_INDEX);
        MenuHelper.linkMenu(MenuIDs.LEVEL_COMPLETE);
    }

    private boolean outOfBounds() {
        int highestPlatformY = Integer.MAX_VALUE;
        int lowestPlatformY = Integer.MIN_VALUE;
        int maximumDistance = 0;

        for (Platform p : platforms) {
            highestPlatformY = Math.min(highestPlatformY, p.getPosition()[RenderConstants.Y]);
            lowestPlatformY = Math.max(lowestPlatformY, p.getPosition()[RenderConstants.Y]);
        }

        for (Sentry s : sentries)
            maximumDistance = Math.max(maximumDistance, Utility.distance(player, s));

        final int playerY = player.getPosition()[RenderConstants.Y];

        final boolean playerIsTooHigh = playerY < highestPlatformY - PLAYER_OUT_OF_BOUNDS_AT;
        final boolean playerIsTooLow = playerY > lowestPlatformY + PLAYER_OUT_OF_BOUNDS_AT;
        final boolean furthestSentryIsTooFarAway = maximumDistance > SENTRY_TOO_FAR_FROM_PLAYER;

        return  playerIsTooHigh || playerIsTooLow || furthestSentryIsTooFarAway;
    }

    public void addSentry(final Sentry sentry) {
        if (!sentries.contains(sentry))
            sentries.add(sentry);
    }

    private boolean determine() {
        for (SentrySpec s : sentrySpecs)
            if (!s.getRole().isDeterministic())
                return false;

        return true;
    }

    private boolean checkIfBlind() {
        for (SentrySpec s : sentrySpecs)
            if (s.getRole().isSightDependent())
                return false;

        return true;
    }

    // HINT PARSING

    private String parseHint() {
        final Function<String, String> KEY_REPLACEMENT_F = x ->
                ControlScheme.getCorrespondingKey(ControlScheme.Action.valueOf(x)).print();

        final String KEY_TAG = "key", AFTER_TAG = ":", OPEN = "<", CLOSE = ">",
                TAG_START = KEY_TAG + AFTER_TAG + OPEN;

        String mutableHint = hint;

        while (mutableHint.contains(TAG_START) &&
                mutableHint.indexOf(CLOSE) > mutableHint.indexOf(TAG_START)) {
            final String before = mutableHint.substring(0, mutableHint.indexOf(TAG_START)),
                    replacement = KEY_REPLACEMENT_F.apply(
                            mutableHint.substring(mutableHint.indexOf(TAG_START) +
                                    TAG_START.length(), mutableHint.indexOf(CLOSE))),
                    after = mutableHint.substring(mutableHint.indexOf(CLOSE) + 1);

            mutableHint = before + replacement + after;
        }

        return mutableHint;
    }

    // GETTERS

    public boolean isEditorLevel() {
        return name.equals(EDITOR_LEVEL_NAME);
    }

    public Path getFilepath() {
        return filepath;
    }

    public PlatformSpec[] getPlatformSpecs() {
        return platformSpecs;
    }

    public SentrySpec[] getSentrySpecs() {
        return sentrySpecs;
    }

    public boolean isDeterministic() {
        return deterministic;
    }

    public boolean isBlind() {
        return blind;
    }

    public String getName() {
        return name;
    }

    public String getHint() {
        return hint;
    }

    public String getParsedHint() {
        return parsedHint;
    }

    public LevelStats getStats() {
        return stats;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Sentry> getSentries() {
        return sentries;
    }

    public List<Animation> getAnimations() {
        return animations;
    }

    public Camera getCamera() {
        return camera;
    }
}

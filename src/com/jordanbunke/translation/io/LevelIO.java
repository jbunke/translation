package com.jordanbunke.translation.io;

import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.jbjgl.utility.JBJGLGlobal;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.gameplay.level.PlatformSpec;
import com.jordanbunke.translation.gameplay.level.SentrySpec;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LevelIO {
    public static final Path CAMPAIGNS_FOLDER = Paths.get("resources", "campaigns");
    public static final Path MAIN_CAMPAIGNS_FOLDER = CAMPAIGNS_FOLDER.resolve(Paths.get("main"));
    public static final Path TUTORIAL_CAMPAIGN_FOLDER = CAMPAIGNS_FOLDER.resolve(Paths.get("tutorial"));
    public static final Path MY_CAMPAIGNS_FOLDER = CAMPAIGNS_FOLDER.resolve(Paths.get("my_campaigns"));
    public static final Path IMPORTED_CAMPAIGNS_FOLDER = CAMPAIGNS_FOLDER.resolve(Paths.get("imported"));

    private static final String CAMPAIGNS_IN_FOLDER = ".campaigns";
    private static final String CAMPAIGN_SPEC = ".spec";
    private static final String LEVEL_FILE_SUFFIX = ".lvl";

    private static final int NF = -1;
    private static final String NOT_FOUND = "NOT_FOUND", NAME = "name", HINT = "hint",
            STATS = "stats", PLATFORMS = "platforms", SENTRIES = "sentries",
            LEVEL_COUNT = "level-count", LEVELS_BEATEN = "levels-beaten",
            FILE_NAMING = "file-naming", FILES = "files",
            SHOW_HINT = "show-hint",
            DEFAULT = "default", // CUSTOM = "custom",
            CAMPAIGNS = "campaigns",
            CONTENT_FOLLOWING = ":",
            BIG_OPEN = "{", BIG_CLOSE = "}",
            SMALL_OPEN = "(", SMALL_CLOSE = ")",
            BIG_SEP = ";", SMALL_SEP = ",";

    public static Campaign[] readCampaignsInFolder(final Path folder) {
        String toParse = JBJGLFileIO.readFile(folder.resolve(Paths.get(CAMPAIGNS_IN_FOLDER)));
        final String[] campaignFolders = extractBigFrom(CAMPAIGNS, toParse).split(BIG_SEP);

        final Campaign[] campaigns = new Campaign[campaignFolders.length];

        for (int i = 0; i < campaigns.length; i++)
            campaigns[i] = readCampaign(folder.resolve(Paths.get(campaignFolders[i].trim())));

        return campaigns;
    }

    public static Campaign readCampaign(final Path campaignFolder) {
        String toParse = JBJGLFileIO.readFile(campaignFolder.resolve(Paths.get(CAMPAIGN_SPEC)));
        return parseCampaign(toParse, campaignFolder);
    }

    private static Campaign parseCampaign(final String toParse, final Path campaignFolder) {
        final String name = extractBigFrom(NAME, toParse);
        final int levelCount = Integer.parseInt(extractBigFrom(LEVEL_COUNT, toParse));
        final int levelsBeaten = Integer.parseInt(extractBigFrom(LEVELS_BEATEN, toParse));
        final boolean showHint = Boolean.parseBoolean(extractBigFrom(SHOW_HINT, toParse));
        final Level[] levels = extractLevels(toParse, levelCount, campaignFolder);

        return Campaign.load(name, levelsBeaten, levels, showHint, campaignFolder);
    }

    private static Level[] extractLevels(
            final String text, final int levelCount, final Path campaignFolder
    ) {
        final String fileNaming = extractBigFrom(FILE_NAMING, text);
        final boolean defaultNaming = fileNaming.equals(DEFAULT);
        final String[] levelFileNames = defaultNaming
                ? new String[levelCount]
                : extractBigFrom(FILES, text).split(BIG_SEP);

        if (levelFileNames.length != levelCount)
            JBJGLGlobal.printErrorToJBJGLChannel(
                    "Campaign file contains " + levelFileNames.length +
                            " levels despite declaring " + levelCount + "."
            );

        if (defaultNaming)
            for (int i = 0; i < levelFileNames.length; i++)
                levelFileNames[i] = i + LEVEL_FILE_SUFFIX;

        final Level[] levels = new Level[levelFileNames.length];

        for (int i = 0; i < levelFileNames.length; i++)
            levels[i] = readLevel(campaignFolder.resolve(Paths.get(levelFileNames[i].trim())));

        return levels;
    }

    public static Level readLevel(final Path filepath) {
        String toParse = JBJGLFileIO.readFile(filepath);
        return parseLevel(toParse);
    }

    private static Level parseLevel(final String toParse) {
        final String name = extractBigFrom(NAME, toParse);
        final String hint = extractBigFrom(HINT, toParse);
        final LevelStats levelStats = extractLevelStats(toParse);
        final PlatformSpec[] platformSpecs = extractPlatforms(toParse);
        final SentrySpec[] sentrySpecs = extractSentries(toParse);

        return Level.load(name, hint, levelStats, platformSpecs, sentrySpecs);
    }

    private static SentrySpec extractSentry(final String attributes) {
        final int REG_LENGTH = 3, SPAWNER_LENGTH = 4;

        final String[] unwrapped = unwrapAttributes(attributes);

        if (unwrapped == null ||
                (unwrapped.length != REG_LENGTH && unwrapped.length != SPAWNER_LENGTH))
            return null;

        final boolean isSpawner = unwrapped.length == SPAWNER_LENGTH &&
                unwrapped[0].trim().toUpperCase().equals(Sentry.Role.SPAWNER.name());
        final int length = isSpawner ? SPAWNER_LENGTH : REG_LENGTH;

        final Sentry.Role role = Sentry.Role.valueOf(unwrapped[length - 3].trim().toUpperCase());
        final int platformIndex = Integer.parseInt(unwrapped[length - 2].trim());
        final int initialMovement = Integer.parseInt(unwrapped[length - 1].trim());

        return isSpawner
                ? SentrySpec.defineSpawner(role, platformIndex, initialMovement)
                : SentrySpec.define(role, platformIndex, initialMovement);
    }

    private static SentrySpec[] extractSentries(final String text) {
        String[] sentryStrings = extractBigFrom(SENTRIES, text).split(BIG_SEP);

        SentrySpec[] sentrySpecs = new SentrySpec[sentryStrings.length];

        for (int i = 0; i < sentrySpecs.length; i++)
            sentrySpecs[i] = extractSentry(sentryStrings[i].trim());

        return sentrySpecs;
    }

    private static String[] unwrapAttributes(final String attributes) {
        if (attributes.indexOf(SMALL_OPEN) != 0 ||
                attributes.indexOf(SMALL_CLOSE) != attributes.length() - 1)
            return null;

        final String unwrapped = attributes.substring(1, attributes.length() - 1);
        return unwrapped.split(SMALL_SEP);
    }

    private static PlatformSpec extractPlatform(final String attributes) {
        final int X = 0, Y = 1, WIDTH = 2, LENGTH = 3;

        final String[] unwrapped = unwrapAttributes(attributes);

        if (unwrapped == null || unwrapped.length != LENGTH)
            return null;

        return PlatformSpec.define(
                Integer.parseInt(unwrapped[X].trim()),
                Integer.parseInt(unwrapped[Y].trim()),
                Integer.parseInt(unwrapped[WIDTH].trim())
        );
    }

    private static PlatformSpec[] extractPlatforms(final String text) {
        String[] platformStrings = extractBigFrom(PLATFORMS, text).split(BIG_SEP);

        PlatformSpec[] platformSpecs = new PlatformSpec[platformStrings.length];

        for (int i = 0; i < platformSpecs.length; i++)
            platformSpecs[i] = extractPlatform(platformStrings[i].trim());

        return platformSpecs;
    }

    private static LevelStats extractLevelStats(final String text) {
        String[] statStrings = extractBigFrom(STATS, text).split(BIG_SEP);

        if (statStrings.length != LevelStats.LENGTH)
            return LevelStats.createNew();
        
        final int[] stats = new int[LevelStats.LENGTH];

        for (int i = 0; i < stats.length; i++)
            stats[i] = Integer.parseInt(statStrings[i].trim());

        return LevelStats.load(
                stats[LevelStats.TIME], stats[LevelStats.FAILURES],
                stats[LevelStats.SIGHTINGS], stats[LevelStats.MAX_COMBO]
        );
    }

    private static String extractBigFrom(final String tag, final String text) {
        final String open = tag + CONTENT_FOLLOWING + BIG_OPEN;
        int openIndex = text.indexOf(open);
        
        if (openIndex == NF)
            return NOT_FOUND;

        openIndex += open.length();

        int relativeCloseIndex = text.substring(openIndex).indexOf(BIG_CLOSE);

        if (relativeCloseIndex == NF)
            return NOT_FOUND;

        return text.substring(openIndex, relativeCloseIndex + openIndex).trim();
    }
}

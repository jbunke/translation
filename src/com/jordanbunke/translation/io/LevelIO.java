package com.jordanbunke.translation.io;

import com.jordanbunke.jbjgl.error.JBJGLError;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.translation.gameplay.campaign.Campaign;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.level.Level;
import com.jordanbunke.translation.gameplay.level.LevelStats;
import com.jordanbunke.translation.gameplay.level.PlatformSpec;
import com.jordanbunke.translation.gameplay.level.SentrySpec;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LevelIO {
    public static final Path CAMPAIGNS_FOLDER = ParserWriter.RESOURCE_ROOT.resolve("campaigns");
    public static final Path MY_CONTENT_FOLDER = ParserWriter.RESOURCE_ROOT.resolve("my_content"); // TODO - potentially elsewhere

    public static final Path MAIN_CAMPAIGNS_FOLDER = CAMPAIGNS_FOLDER.resolve("main");
    public static final Path TUTORIAL_CAMPAIGN_FOLDER = CAMPAIGNS_FOLDER.resolve("tutorial");
    public static final Path IMPORTED_CAMPAIGNS_FOLDER = CAMPAIGNS_FOLDER.resolve("imported");

    public static final Path MY_CAMPAIGNS_FOLDER = MY_CONTENT_FOLDER.resolve("campaigns");
    private static final Path MY_LEVELS_FOLDER = MY_CONTENT_FOLDER.resolve("levels");

    private static final String CAMPAIGNS_IN_FOLDER = ".campaigns";
    private static final String CAMPAIGN_SPEC = ".spec";
    private static final String LEVEL_FILE_SUFFIX = ".lvl";

    private static final String NAME = "name", HINT = "hint",
            STATS = "stats", PLATFORMS = "platforms", SENTRIES = "sentries",
            LEVEL_COUNT = "level-count", LEVELS_BEATEN = "levels-beaten",
            FILE_NAMING = "file-naming", FILES = "files",
            SHOW_HINT = "show-hint",
            DEFAULT = "default", CUSTOM = "custom",
            CAMPAIGNS = "campaigns";

    // CAMPAIGN - READ

    public static Campaign[] readCampaignsInFolder(final Path folder) {
        String toParse = JBJGLFileIO.readFile(folder.resolve(CAMPAIGNS_IN_FOLDER));
        final String[] campaignFolders = ParserWriter.extractFromTagAndSplit(CAMPAIGNS, toParse);

        final Campaign[] campaigns = new Campaign[campaignFolders.length];

        for (int i = 0; i < campaigns.length; i++)
            campaigns[i] = readCampaign(folder.resolve(campaignFolders[i].trim()));

        return campaigns;
    }

    public static Campaign readMyLevels() {
        // TODO - unique implementation
        return readCampaign(MY_LEVELS_FOLDER);
    }

    public static Campaign readCampaign(final Path campaignFolder) {
        String toParse = JBJGLFileIO.readFile(campaignFolder.resolve(CAMPAIGN_SPEC));
        return parseCampaign(toParse, campaignFolder);
    }

    private static Campaign parseCampaign(final String toParse, final Path campaignFolder) {
        final String name = ParserWriter.extractFromTag(NAME, toParse);
        final int levelCount = Integer.parseInt(ParserWriter.extractFromTag(LEVEL_COUNT, toParse));
        final int levelsBeaten = Integer.parseInt(ParserWriter.extractFromTag(LEVELS_BEATEN, toParse));
        final boolean showHint = Boolean.parseBoolean(ParserWriter.extractFromTag(SHOW_HINT, toParse));
        final String fileNaming = ParserWriter.extractFromTag(FILE_NAMING, toParse);
        final boolean defaultNaming = fileNaming.equals(DEFAULT);
        final List<String> levelFiles = new ArrayList<>();
        final List<Level> levels = extractLevels(toParse, levelCount, defaultNaming, campaignFolder, levelFiles);

        return Campaign.load(name, levelsBeaten, levels, showHint, defaultNaming, campaignFolder, levelFiles);
    }

    private static List<Level> extractLevels(
            final String text, final int levelCount, final boolean defaultNaming,
            final Path campaignFolder, final List<String> levelFiles
    ) {

        final String[] levelFileNames = defaultNaming
                ? new String[levelCount]
                : ParserWriter.extractFromTagAndSplit(FILES, text);

        if (levelFileNames.length != levelCount)
            JBJGLError.send("Campaign file contains " + levelFileNames.length +
                    " levels despite declaring " + levelCount + ".");

        if (defaultNaming)
            for (int i = 0; i < levelFileNames.length; i++)
                levelFileNames[i] = i + LEVEL_FILE_SUFFIX;

        final List<Level> levels = new ArrayList<>();

        for (int i = 0; i < levelCount; i++) {
            levelFiles.add(levelFileNames[i].trim());
            levels.add(readLevel(campaignFolder.resolve(levelFiles.get(i))));
        }

        return levels;
    }

    // CAMPAIGN - WRITE

    public static void writeCampaign(
            final Campaign campaign, final boolean resetLevelsBeaten
    ) {
        final Path filepath = campaign.getFolder().resolve(CAMPAIGN_SPEC);
        final StringBuilder sb = new StringBuilder();

        sb.append(ParserWriter.encloseInTag(NAME, campaign.getName()));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(
                LEVEL_COUNT,
                String.valueOf(campaign.getLevelCount())
        ));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(
                LEVELS_BEATEN,
                String.valueOf(resetLevelsBeaten ? 0 : campaign.getLevelsBeaten())
        ));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(
                SHOW_HINT,
                String.valueOf(campaign.isShowingHint())
        ));
        ParserWriter.newLineSB(sb);

        final boolean isDefaultNaming = campaign.isDefaultNaming();
        sb.append(ParserWriter.encloseInTag(
                FILE_NAMING, isDefaultNaming ? DEFAULT : CUSTOM));
        ParserWriter.newLineSB(sb);

        if (!isDefaultNaming)
            writeCampaignFiles(campaign, sb);

        JBJGLFileIO.writeFile(filepath, sb.toString());
    }

    private static void writeCampaignFiles(final Campaign campaign, final StringBuilder sb) {
        final List<String> levelFiles = campaign.getLevelFilenames();
        final String[] levelFilenameArray = new String[levelFiles.size()];

        sb.append(ParserWriter.packAndEncloseInTag(FILES,
                levelFiles.toArray(levelFilenameArray), true));
    }

    // LEVEL - READ

    public static Level readLevel(final Path filepath) {
        String toParse = JBJGLFileIO.readFile(filepath);
        return parseLevel(toParse, filepath);
    }

    private static Level parseLevel(final String toParse, final Path filepath) {
        final String name = ParserWriter.extractFromTag(NAME, toParse);
        final String hint = ParserWriter.extractFromTag(HINT, toParse);
        final LevelStats levelStats = extractLevelStats(toParse);
        final PlatformSpec[] platformSpecs = extractPlatforms(toParse);
        final SentrySpec[] sentrySpecs = extractSentries(toParse);

        return Level.load(name, hint, levelStats, platformSpecs, sentrySpecs, filepath);
    }

    private static SentrySpec extractSentry(final String attributes) {
        final int REG_LENGTH = 3, SPAWNER_LENGTH = 4;

        final String[] unwrapped = ParserWriter.unwrapAttributes(attributes);

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
        String[] sentryStrings = ParserWriter.extractFromTagAndSplit(SENTRIES, text);

        SentrySpec[] sentrySpecs = new SentrySpec[sentryStrings.length];

        for (int i = 0; i < sentrySpecs.length; i++)
            sentrySpecs[i] = extractSentry(sentryStrings[i].trim());

        return sentrySpecs;
    }

    private static PlatformSpec extractPlatform(final String attributes) {
        final int X = 0, Y = 1, WIDTH = 2, LENGTH = 3;

        final String[] unwrapped = ParserWriter.unwrapAttributes(attributes);

        if (unwrapped == null || unwrapped.length != LENGTH)
            return null;

        return PlatformSpec.define(
                Integer.parseInt(unwrapped[X].trim()),
                Integer.parseInt(unwrapped[Y].trim()),
                Integer.parseInt(unwrapped[WIDTH].trim())
        );
    }

    private static PlatformSpec[] extractPlatforms(final String text) {
        String[] platformStrings = ParserWriter.extractFromTagAndSplit(PLATFORMS, text);

        PlatformSpec[] platformSpecs = new PlatformSpec[platformStrings.length];

        for (int i = 0; i < platformSpecs.length; i++)
            platformSpecs[i] = extractPlatform(platformStrings[i].trim());

        return platformSpecs;
    }

    private static LevelStats extractLevelStats(final String text) {
        String[] statStrings = ParserWriter.extractFromTagAndSplit(STATS, text);

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

    // LEVEL - SAVE

    public static void saveValidatedEditorLevel(
            final String name, final String hint
    ) {
        final String filename = generateLevelFilename(name);
        final Path filepath = MY_LEVELS_FOLDER.resolve(filename);
        final Level level = Level.fromEditorValidated(name, hint, filepath);

        writeLevel(level, true);

        Campaign myLevels = readMyLevels();
        myLevels.addLevel(level, filename, true);

        writeCampaign(myLevels, false);
    }

    private static String generateLevelFilename(final String name) {
        final String NAME_COMPONENT_SEPARATOR = "-";
        final StringBuilder sb = new StringBuilder();

        for (char c : name.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
                sb.append(c);
            else if (c >= 'A' && c <= 'Z')
                sb.append(String.valueOf(c).toLowerCase());
            else if (c == ' ' || c == '-')
                sb.append('-');
        }

        final LocalDateTime now = LocalDateTime.now();

        sb.append(" from ").append(now.getYear()).append(NAME_COMPONENT_SEPARATOR)
                .append(now.getMonthValue()).append(NAME_COMPONENT_SEPARATOR)
                .append(now.getDayOfMonth()).append(" at ").append(now.getHour())
                .append(NAME_COMPONENT_SEPARATOR).append(now.getMinute())
                .append(NAME_COMPONENT_SEPARATOR).append(now.getSecond());

        sb.append(LEVEL_FILE_SUFFIX);

        return sb.toString();
    }

    // LEVEL - WRITE

    public static void writeLevel(final Level level, final boolean reset) {
        final Path filepath = level.getFilepath();
        final StringBuilder sb = new StringBuilder();

        sb.append(ParserWriter.encloseInTag(NAME, level.getName()));
        ParserWriter.newLineSB(sb);

        sb.append(ParserWriter.encloseInTag(HINT, level.getHint()));
        ParserWriter.newLineSB(sb);

        writeLevelStats(level, sb, reset);

        writeLevelPlatforms(level, sb);

        writeLevelSentries(level, sb);

        JBJGLFileIO.writeFile(filepath, sb.toString());
    }

    private static void writeLevelStats(
            final Level level, final StringBuilder sb, final boolean reset
    ) {
        final LevelStats levelStats = level.getStats();
        if (!reset && levelStats.hasStats()) {
            final String[] statValues = new String[LevelStats.LENGTH];

            for (int i = 0; i < statValues.length; i++)
                statValues[i] = levelStats.getPersonalBest(i, false);

            sb.append(ParserWriter.packAndEncloseInTag(STATS, statValues, false));
        } else
            sb.append(ParserWriter.encloseInTag(STATS, ""));

        ParserWriter.newLineSB(sb);
    }

    private static void writeLevelPlatforms(final Level level, final StringBuilder sb) {
        final PlatformSpec[] platforms = level.getPlatformSpecs();
        final String[] platformStrings = new String[platforms.length];

        for (int i = 0; i < platforms.length; i++) {
            final String[] attributes = new String[] {
                    String.valueOf(platforms[i].getX()),
                    String.valueOf(platforms[i].getY()),
                    String.valueOf(platforms[i].getWidth())
            };
            platformStrings[i] = ParserWriter.wrapAttributes(attributes);
        }

        sb.append(ParserWriter.packAndEncloseInTag(PLATFORMS, platformStrings, true));
        ParserWriter.newLineSB(sb);
    }

    private static void writeLevelSentries(final Level level, final StringBuilder sb) {
        final SentrySpec[] sentries = level.getSentrySpecs();
        final String[] sentryStrings = new String[sentries.length];

        for (int i = 0; i < sentries.length; i++) {
            final String[] attributes = sentries[i].getRole() == Sentry.Role.SPAWNER
                    ? new String[] {
                    Sentry.Role.SPAWNER.name(),
                    sentries[i].getSecondary().name(),
                    String.valueOf(sentries[i].getPlatformIndex()),
                    String.valueOf(sentries[i].getInitialMovement())
            }
                    : new String[] {
                    sentries[i].getRole().name(),
                    String.valueOf(sentries[i].getPlatformIndex()),
                    String.valueOf(sentries[i].getInitialMovement())
            };
            sentryStrings[i] = ParserWriter.wrapAttributes(attributes);
        }

        sb.append(ParserWriter.packAndEncloseInTag(SENTRIES, sentryStrings, true));
        ParserWriter.newLineSB(sb);
    }

    // LEVEL - DELETE

    public static void deleteLevelFile(final Level level) {
        JBJGLFileIO.deleteFile(level.getFilepath());
    }
}

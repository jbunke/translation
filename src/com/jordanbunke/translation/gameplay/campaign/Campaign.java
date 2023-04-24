package com.jordanbunke.translation.gameplay.campaign;

import com.jordanbunke.jbjgl.error.JBJGLError;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.gameplay.level.Level;

import java.nio.file.Path;
import java.util.List;

public class Campaign {
    private final Path campaignFolder;
    private final List<String> levelFilenames;

    private final String name;
    private int levelIndex;
    private int levelsBeaten;
    private final List<Level> levels;

    // metadata
    private final boolean showHint;
    private final boolean defaultNaming;

    private Campaign(
            final String name, final int levelsBeaten, final List<Level> levels,
            final boolean showHint, final boolean defaultNaming,
            final Path campaignFolder, final List<String> levelFilenames
    ) {
        this.campaignFolder = campaignFolder;
        this.levelFilenames = levelFilenames;

        this.name = name;
        levelIndex = 0;
        this.levelsBeaten = levelsBeaten;
        this.levels = levels;

        this.showHint = showHint;
        this.defaultNaming = defaultNaming;
    }

    public static Campaign load(
            final String name, final int levelsBeaten, final List<Level> levels,
            final boolean showHint, final boolean defaultNaming,
            final Path campaignFolder, final List<String> levelFiles
    ) {
        return new Campaign(
                name, levelsBeaten, levels, showHint,
                defaultNaming, campaignFolder, levelFiles
        );
    }

    public void addLevel(
            final Level level, final String levelFilename,
            final boolean isMyLevels
    ) {
        levels.add(level);
        levelFilenames.add(levelFilename);

        if (isMyLevels) levelsBeaten = getLevelCount();
    }

    public void removeLevel(final Level level) {
        final int NOT_FOUND = -1;

        final int index = levels.indexOf(level);

        if (index == NOT_FOUND) {
            JBJGLError.send("Could not find level \"" + level.getName() +
                    "\" in campaign \"" + getName() + "\", so it was not removed.");
            return;
        }

        levels.remove(index);
        levelFilenames.remove(index);

        if (levelsBeaten > index)
            levelsBeaten--;
    }

    public void updateBeaten() {
        if (levelIndex == levelsBeaten)
            levelsBeaten++;
    }

    public boolean hasNextLevel() {
        return levelIndex + 1 < getLevelCount();
    }

    public void setToNextLevel() {
        levelIndex++;
        setLevel();
    }

    public void setLevel() {
        Translation.setLevel(getLevel());
    }

    public void setLevel(final int levelIndex) {
        this.levelIndex = levelIndex;
        setLevel();
    }

    public Level getLevel() {
        return getLevelAt(levelIndex);
    }

    public Level getLevelAt(final int index) {
        return levels.get(index);
    }

    public boolean isUnlocked(final int index) {
        return levelsBeaten >= index;
    }

    public int getLevelCount() {
        return levels.size();
    }

    // TRUE GETTERS

    public int getLevelsBeaten() {
        return levelsBeaten;
    }

    public Path getFolder() {
        return campaignFolder;
    }

    public List<String> getLevelFilenames() {
        return levelFilenames;
    }

    public String getName() {
        return name;
    }

    public boolean isShowingHint() {
        return showHint;
    }

    public boolean isDefaultNaming() {
        return defaultNaming;
    }

    // OVERRIDES

    @Override
    public String toString() {
        return name + " [" + levels.size() + " level campaign]";
    }
}

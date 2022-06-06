package com.jordanbunke.translation.gameplay.campaign;

import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.gameplay.level.Level;

import java.nio.file.Path;

public class Campaign {
    private final Path campaignFolder;
    private final String[] levelFiles;

    private final String name;
    private int levelIndex;
    private int levelsBeaten;
    private final Level[] levels;

    // metadata
    private final boolean showHint;
    private final boolean defaultNaming;

    private Campaign(
            final String name, final int levelsBeaten, final Level[] levels,
            final boolean showHint, final boolean defaultNaming,
            final Path campaignFolder, final String[] levelFiles
    ) {
        this.campaignFolder = campaignFolder;
        this.levelFiles = levelFiles;

        this.name = name;
        levelIndex = 0;
        this.levelsBeaten = levelsBeaten;
        this.levels = levels;

        this.showHint = showHint;
        this.defaultNaming = defaultNaming;
    }

    public static Campaign load(
            final String name, final int levelsBeaten, final Level[] levels,
            final boolean showHint, final boolean defaultNaming,
            final Path campaignFolder, final String[] levelFiles
    ) {
        return new Campaign(
                name, levelsBeaten, levels, showHint,
                defaultNaming, campaignFolder, levelFiles
        );
    }

    public void updateBeaten() {
        if (levelIndex == levelsBeaten)
            levelsBeaten++;
    }

    public boolean hasNextLevel() {
        return levelIndex + 1 < levels.length;
    }

    public void setToNextLevel() {
        levelIndex++;
        setLevel();
    }

    public void setLevel() {
        Translation.setLevel(levels[levelIndex]);
    }

    public void setLevel(final int levelIndex) {
        this.levelIndex = levelIndex;
        setLevel();
    }

    public Level getLevel() {
        return levels[levelIndex];
    }

    public Level getLevelAt(final int index) {
        return levels[index];
    }

    public boolean isUnlocked(final int index) {
        return levelsBeaten >= index;
    }

    public int getLevelCount() {
        return levels.length;
    }

    // TRUE GETTERS

    public int getLevelsBeaten() {
        return levelsBeaten;
    }

    public Path getFolder() {
        return campaignFolder;
    }

    public String[] getLevelFiles() {
        return levelFiles;
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
        return name + " [" + levels.length + " level campaign]";
    }
}

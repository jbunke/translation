package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.settings.GameplayConstants;

public class LevelStats {
    public static final int TIME = 0, FAILURES = 1, SIGHTINGS = 2, MAX_COMBO = 3;
    public static final int UNDEFINED = -1, INITIAL_VALUE = Integer.MAX_VALUE;
    public static final int LENGTH = 4;

    private final int[] stats;
    private final int[] finalStats;
    private final int[] personalBests;

    private LevelStats(
            final int[] stats, final int[] finalStats, final int[] personalBests,
            final boolean initializePB
    ) {
        this.stats = stats;
        this.finalStats = finalStats;
        this.personalBests = personalBests;

        initialize(initializePB);
    }

    public static LevelStats createNew() {
        return new LevelStats(new int[LENGTH], new int[LENGTH], new int[LENGTH], true);
    }

    public static LevelStats load(
            final int fastestTime, final int fewestFailures,
            final int fewestSightings, final int maxCombo
    ) {
        return new LevelStats(
                new int[LENGTH], new int[LENGTH],
                new int[] { fastestTime, fewestFailures, fewestSightings, maxCombo },
                false
        );
    }

    private void initialize(final boolean initializePB) {
        for (int i = 0; i < LENGTH; i++) {
            stats[i] = 0;
            finalStats[i] = 0;

            if (initializePB)
                personalBests[i] = getInitialValue(i);
        }
    }

    public void increment(final int index) {
        if (indexOutOfBounds(index))
            return;

        stats[index]++;
    }

    public void updateIfIsGreaterThanValue(final int index, final int candidate) {
        if (indexOutOfBounds(index))
            return;

        if (candidate > stats[index])
            stats[index] = candidate;
    }

    public void reset() {
        for (int i = 0; i < LENGTH; i++)
            resetStat(i);
    }

    public void resetStat(final int index) {
        if (indexOutOfBounds(index))
            return;

        stats[index] = 0;
    }

    public void finalizeStats() {
        System.arraycopy(stats, 0, finalStats, 0, LENGTH);

        for (int i = 0; i < LENGTH; i++)
            if (!isWorseThanPB(i))
                personalBests[i] = finalStats[i];
    }

    public boolean isWorseThanPB(final int index) {
        if (indexOutOfBounds(index))
            return true;

        if (index == MAX_COMBO)
            return finalStats[index] < personalBests[index];
        
        return finalStats[index] > personalBests[index];
    }

    public int getInitialValue(final int index) {
        if (indexOutOfBounds(index))
            return UNDEFINED;

        if (index == MAX_COMBO)
            return UNDEFINED;

        return INITIAL_VALUE;
    }

    public String getFinalStat(final int index) {
        if (indexOutOfBounds(index))
            return formatStat(UNDEFINED, index);

        return formatStat(finalStats[index], index);
    }

    public String getPersonalBest(final int index) {
        if (indexOutOfBounds(index))
            return formatStat(UNDEFINED, index);

        return formatStat(personalBests[index], index);
    }

    private String formatStat(final int stat, final int index) {
        final String base = statIsUndefined(stat) ? "???" : String.valueOf(stat);
        final String seconds = statIsUndefined(stat)
                ? "???"
                : String.valueOf(stat / GameplayConstants.UPDATE_HZ);

        return index == TIME
                ? seconds + " seconds"
                : base;
    }

    private boolean statIsUndefined(final int stat) {
        return stat == UNDEFINED || stat == INITIAL_VALUE;
    }

    private boolean indexOutOfBounds(final int index) {
        final boolean outOfBounds = index < 0 || index >= LENGTH;

        if (outOfBounds)
            Translation.debugger.getChannel(JBJGLGameDebugger.LOGIC_CHANNEL).printMessage(
                    "Passed an out of bounds parameter ( < 0 or >= 3 ) as index in LevelStats."
            );

        return outOfBounds;
    }
}

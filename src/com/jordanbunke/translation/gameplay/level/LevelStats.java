package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.settings.GameplayConstants;

import java.awt.*;
import java.util.function.BiFunction;

public class LevelStats {
    public static final int TIME = 0, FAILURES = 1, SIGHTINGS = 2, MAX_COMBO = 3;
    public static final int UNDEFINED = -1, INITIAL_VALUE = Integer.MAX_VALUE;
    public static final int LENGTH = 4;

    private final int[] stats;
    private final int[] finalStats;
    private final int[] personalBests;
    private final int[] previousPersonalBests;

    private LevelStats(
            final int[] stats, final int[] finalStats, final int[] personalBests,
            final boolean initializePB
    ) {
        this.stats = stats;
        this.finalStats = finalStats;
        this.personalBests = personalBests;
        this.previousPersonalBests = new int[LENGTH];

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

    public boolean hasStats() {
        return !(
                statIsUndefined(TIME) || statIsUndefined(FAILURES) ||
                        statIsUndefined(SIGHTINGS) || statIsUndefined(MAX_COMBO)
        );
    }

    private void initialize(final boolean initializePB) {
        for (int i = 0; i < LENGTH; i++) {
            stats[i] = 0;
            finalStats[i] = 0;

            if (initializePB) {
                final int value = getInitialValue(i);
                personalBests[i] = value;
                previousPersonalBests[i] = value;
            }
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
        System.arraycopy(personalBests, 0, previousPersonalBests, 0, LENGTH);

        for (int i = 0; i < LENGTH; i++)
            if (newPB(i))
                personalBests[i] = finalStats[i];
    }

    public Color getStatScreenColor(final int index) {
        if (indexOutOfBounds(index))
            return TLColors.BLACK();

        BiFunction<Integer, Integer, Boolean> comparison =
                index == MAX_COMBO
                        ? (a, b) -> (a > b)
                        : (a, b) -> (a < b);

        if (finalStats[index] == previousPersonalBests[index])
            return TLColors.SAME_AS_PB();
        else if (comparison.apply(finalStats[index], previousPersonalBests[index]))
            return TLColors.NEW_PB();
        else
            return TLColors.WORSE_THAN_PB();
    }

    public boolean newPB(final int index) {
        if (indexOutOfBounds(index))
            return false;

        if (index == MAX_COMBO)
            return finalStats[index] > previousPersonalBests[index];
        
        return finalStats[index] < previousPersonalBests[index];
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

    public String getPersonalBest(final int index, final boolean format) {
        if (indexOutOfBounds(index))
            return formatStat(UNDEFINED, index);

        return format
                ? formatStat(personalBests[index], index)
                : String.valueOf(personalBests[index]);
    }

    public String getPreviousPersonalBest(final int index) {
        if (indexOutOfBounds(index))
            return formatStat(UNDEFINED, index);

        return formatStat(previousPersonalBests[index], index);
    }

    private String formatStat(final int stat, final int index) {
        final String base = statIsUndefined(stat) ? "???" : String.valueOf(stat);
        final String seconds = statIsUndefined(stat)
                ? "???"
                : String.valueOf(stat / GameplayConstants.UPDATE_HZ);

        return index == TIME
                ? seconds + "s"
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

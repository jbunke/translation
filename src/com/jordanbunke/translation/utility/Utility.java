package com.jordanbunke.translation.utility;

import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.gameplay.HasPosition;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Utility {
    private static final Random r = new Random();

    public static <T> T randomElementFromList(final List<T> list) {
        if (list.size() == 0)
            return null;

        return list.get(boundedRandom(0, list.size()));
    }

    public static <T> T randomElementFromArray(final T[] array) {
        if (array.length == 0)
            return null;

        return array[boundedRandom(0, array.length)];
    }

    public static int boundedRandom(final int min, final int max) {
        return bounded(min, max, r.nextDouble());
    }

    public static int bounded(final int min, final int max, final double fromMinToMax) {
        return min + (int)(fromMinToMax * (max - min));
    }

    public static <T> T coinToss(final T heads, final T tails) {
        return coinToss(0.5, heads, tails);
    }

    public static <T> T coinToss(final double headProb, final T heads, final T tails) {
        return r.nextDouble() < headProb ? heads : tails;
    }

    public static int distance(final HasPosition a, final HasPosition b) {
        final int diffX = a.getPosition()[RenderConstants.X] - b.getPosition()[RenderConstants.X];
        final int diffY = a.getPosition()[RenderConstants.Y] - b.getPosition()[RenderConstants.Y];
        return (int)Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
    }

    public static int[] pointAlongLine(
            final HasPosition a, final HasPosition b, final double fraction
    ) {
        if (fraction < 0.)
            return a.getPosition();
        else if (fraction >= 1.)
            return b.getPosition();

        final int x = a.getPosition()[RenderConstants.X] +
                (int)(fraction * (b.getPosition()[RenderConstants.X] -
                        a.getPosition()[RenderConstants.X]));
        final int y = a.getPosition()[RenderConstants.Y] +
                (int)(fraction * (b.getPosition()[RenderConstants.Y] -
                        a.getPosition()[RenderConstants.Y]));

        return new int[] { x, y };
    }

    public static Color colorAlongGradient(
            final Color a, final Color b, final double fraction) {
        if (fraction < 0.)
            return a;
        else if (fraction >= 1.)
            return b;

        final int red = a.getRed() + (int)(fraction * (b.getRed() - a.getRed()));
        final int green = a.getGreen() + (int)(fraction * (b.getGreen() - a.getGreen()));
        final int blue = a.getBlue() + (int)(fraction * (b.getBlue() - a.getBlue()));
        final int opacity = a.getAlpha() + (int)(fraction * (b.getAlpha() - a.getAlpha()));

        return new Color(red, green, blue, opacity);
    }

    public static String cutOffIfLongerThan(final String string, final int length) {
        final int SHORTEN_BY = 2;
        return string.length() <= length ? string : string.substring(0, length - SHORTEN_BY) + "...";
    }
}

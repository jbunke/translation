package com.jordanbunke.translation.io;

import java.nio.file.Path;

public class ParserWriter {
    public static final Path GAME_DATA_ROOT = Path.of("game_data");

    private static final int NF = -1;
    private static final String
            NOT_FOUND = "NOT_FOUND", EMPTY = "",
            CONTENT_FOLLOWING = ":",
            BIG_OPEN = "{", BIG_CLOSE = "}",
            SMALL_OPEN = "(", SMALL_CLOSE = ")",
            BIG_SEP = ";", SMALL_SEP = ",",
            NEW_LINE = "\n", TAB = "\t";

    // WRITE HELPERS

    public static String wrapAttributes(final String[] attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append(SMALL_OPEN);
        pack(attributes, sb, SMALL_SEP, false);
        sb.append(SMALL_CLOSE);
        return sb.toString();
    }

    private static void pack(
            final String[] elements, final StringBuilder sb,
            final String separator, final boolean format
    ) {
        for (int i = 0; i < elements.length; i++) {
            if (format) {
                newLineSB(sb);
                tabSB(sb);
            }
            sb.append(elements[i]);
            if (i + 1 < elements.length)
                sb.append(separator);
        }

        if (format)
            newLineSB(sb);
    }

    public static String encloseInTag(final String tag, final String content) {
        return tag + CONTENT_FOLLOWING + BIG_OPEN + content + BIG_CLOSE;
    }

    public static String packAndEncloseInTag(
            final String tag, final String[] elements, final boolean format
    ) {
        StringBuilder sb = new StringBuilder();

        pack(elements, sb, BIG_SEP, format);
        return encloseInTag(tag, sb.toString());
    }

    public static void newLineSB(final StringBuilder sb) {
        sb.append(NEW_LINE);
    }

    public static void tabSB(final StringBuilder sb) {
        sb.append(TAB);
    }

    // READ HELPERS

    public static String[] unwrapAttributes(final String attributes) {
        if (attributes.indexOf(SMALL_OPEN) != 0 ||
                attributes.indexOf(SMALL_CLOSE) != attributes.length() - 1)
            return null;

        final String unwrapped = attributes.substring(1, attributes.length() - 1);
        return unwrapped.split(SMALL_SEP);
    }

    public static String[] extractFromTagAndSplit(final String tag, final String text) {
        final String[] contents = extractFromTag(tag, text).split(BIG_SEP);

        if (contents.length == 1 && contents[0].equals(EMPTY))
            return new String[] {};

        return contents;
    }

    public static String extractFromTag(final String tag, final String text) {
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

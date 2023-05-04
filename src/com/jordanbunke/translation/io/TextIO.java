package com.jordanbunke.translation.io;

import com.jordanbunke.translation.ResourceManager;
import com.jordanbunke.translation.utility.Utility;

import java.nio.file.Path;

public class TextIO {
    public static final Path
            PATCH_NOTES_FOLDER = ResourceManager.getTextFolder().resolve("patch notes"),
            UPDATES_FILE = PATCH_NOTES_FOLDER.resolve(".updates"),
            SPLASH_TEXT_FILE = ResourceManager.getTextFolder().resolve("splash_text.txt");
    public static final int DEFAULT_PATCH_NOTES_PAGE_INDEX = 1;

    private static final String TEXT_FILE_SUFFIX = ".txt";

    private static final String VERSION = "version", DATE = "date",
            CONTENTS = "contents", UPDATES = "updates", SPLASH = "splash";

    public static PatchNotes[] readUpdates() {
        final String toParse = ResourceManager.getTextResource(UPDATES_FILE);
        final String[] updateIDs = ParserWriter.extractFromTagAndSplit(UPDATES, toParse);

        final PatchNotes[] patchNotes = new PatchNotes[updateIDs.length];

        for (int i = 0; i < updateIDs.length; i++) {
            patchNotes[i] = readUpdate(updateIDs[i].trim());
        }

        return patchNotes;
    }

    private static PatchNotes readUpdate(final String updateID) {
        final Path filepath = generateFPFromUpdateID(updateID);
        final String toParse = ResourceManager.getTextResource(filepath);
        return parseUpdate(toParse);
    }

    private static PatchNotes parseUpdate(final String toParse) {
        final String version = ParserWriter.extractFromTag(VERSION, toParse).orElse("");
        final String date = ParserWriter.extractFromTag(DATE, toParse).orElse("");
        final String contents = ParserWriter.extractFromTag(CONTENTS, toParse).orElse("");

        return PatchNotes.generate(version, date, contents);
    }

    private static Path generateFPFromUpdateID(final String updateID) {
        final String filename = updateID.replaceAll("\\.", "_") +
                TEXT_FILE_SUFFIX;

        return PATCH_NOTES_FOLDER.resolve(filename);
    }

    public static String getRandomSplashText() {
        final String toParse = ResourceManager.getTextResource(SPLASH_TEXT_FILE);
        final String[] splashTexts = ParserWriter.extractFromTagAndSplit(SPLASH, toParse);

        final String splashText = Utility.randomElementFromArray(splashTexts);

        return splashText == null ? "" : splashText.trim();
    }
}

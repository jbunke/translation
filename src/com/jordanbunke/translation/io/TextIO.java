package com.jordanbunke.translation.io;

import com.jordanbunke.translation.ResourceManager;

import java.nio.file.Path;

public class TextIO {
    public static final Path
            PATCH_NOTES_FOLDER = ResourceManager.getTextFolder().resolve("patch notes"),
            UPDATES_FILE = PATCH_NOTES_FOLDER.resolve(".updates");
    public static final int DEFAULT_PATCH_NOTES_PAGE_INDEX = 1;

    private static final String TEXT_FILE_SUFFIX = ".txt";

    private static final String VERSION = "version", DATE = "date",
            CONTENTS = "contents", UPDATES = "updates";

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
        final String version = ParserWriter.extractFromTag(VERSION, toParse);
        final String date = ParserWriter.extractFromTag(DATE, toParse);
        final String contents = ParserWriter.extractFromTag(CONTENTS, toParse);

        return PatchNotes.generate(version, date, contents);
    }

    private static Path generateFPFromUpdateID(final String updateID) {
        final String filename = updateID.replaceAll("\\.", "_") +
                TEXT_FILE_SUFFIX;

        return PATCH_NOTES_FOLDER.resolve(filename);
    }
}

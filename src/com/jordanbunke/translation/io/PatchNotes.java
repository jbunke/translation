package com.jordanbunke.translation.io;

public class PatchNotes {
    private final String version;
    private final String date;
    private final String contents;

    private PatchNotes(
            final String version, final String date,
            final String contents
    ) {
        this.version = version;
        this.date = date;
        this.contents = contents;
    }

    public static PatchNotes generate(
            final String version, final String date,
            final String contents
    ) {
        return new PatchNotes(version, date, contents);
    }

    public String getDate() {
        return date;
    }

    public String getVersion() {
        return version;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return version +
                (date.length() > 0
                        ? (" - " + date + "\n")
                        : "\n") +
                contents;
    }
}

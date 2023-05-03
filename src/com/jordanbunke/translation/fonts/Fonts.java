package com.jordanbunke.translation.fonts;

import com.jordanbunke.jbjgl.fonts.Font;
import com.jordanbunke.jbjgl.fonts.FontFamily;
import com.jordanbunke.translation.ResourceManager;
import com.jordanbunke.translation.Translation;

import java.nio.file.Path;

public class Fonts {
    public enum Typeface {
        CLASSIC(Fonts.CLASSIC.getStandard(),
                Fonts.CLASSIC.getItalics(), CLASSIC_ITALICS_SPACED),
        VIGILANT(Fonts.VIGILANT.getStandard(),
                Fonts.VIGILANT.getItalics(), VIGILANT_ITALICS_SPACED),
        MY_HANDWRITING(Fonts.MY_HANDWRITING.getStandard(),
                Fonts.MY_HANDWRITING.getItalics(), Fonts.MY_HANDWRITING.getBold());

        private final Font standard, italics, italicsSpaced;

        Typeface(
                final Font standard, final Font italics, final Font italicsSpaced
        ) {
            this.standard = standard;
            this.italics = italics;
            this.italicsSpaced = italicsSpaced;
        }

        public Typeface next() {
            final Typeface[] all = values();
            return all[(ordinal() + 1) % all.length];
        }

        @Override
        public String toString() {
            return switch (this) {
                case CLASSIC -> "CLASSIC";
                case VIGILANT -> "VIGILANT";
                case MY_HANDWRITING -> "STYLUS";
            };
        }
    }

    private static Typeface typeface;

    private static final Path FONT_FOLDER = ResourceManager.getFontFilesFolder();
    private static final Class<ResourceManager> LOADER_CLASS = ResourceManager.class;

    private static final FontFamily CLASSIC = FontFamily.loadFromSources(
            "Classic", FONT_FOLDER, LOADER_CLASS, "font-classic",
            FontFamily.NOT_AVAILABLE, "font-classic-italics", 2, 2, 1, true);
    private static final Font CLASSIC_ITALICS_SPACED = Font.loadFromSource(
            FONT_FOLDER, LOADER_CLASS, "font-classic-italics", true, 2);
    private static final FontFamily VIGILANT = FontFamily.loadFromSources(
            "Vigilant", FONT_FOLDER, LOADER_CLASS, "font-vigilant",
            FontFamily.NOT_AVAILABLE, "font-vigilant-italics",
            2, 2, 0, true);
    private static final FontFamily MY_HANDWRITING = FontFamily.fromPreLoaded("My Handwriting",
            Font.loadFromSource(FONT_FOLDER, LOADER_CLASS, "font-hand-drawn",
                    false, 1.0, 1, true),
            Font.loadFromSource(FONT_FOLDER, LOADER_CLASS, "font-hand-drawn",
                    false, 1.0, 1, true),
            Font.loadFromSource(FONT_FOLDER, LOADER_CLASS, "font-hand-drawn",
                    false, 1.0, 3, true));
    private static final Font VIGILANT_ITALICS_SPACED = Font.loadFromSource(FONT_FOLDER, LOADER_CLASS,
            "font-vigilant-italics", true, 2);

    private static Font gameStandard;
    private static Font gameItalics;
    private static Font gameItalicsSpaced;

    public static Font gameStandard() {
        return gameStandard;
    }

    public static Font gameItalics() {
        return gameItalics;
    }

    public static Font gameItalicsSpaced() {
        return gameItalicsSpaced;
    }

    public static Font VIGILANT_ITALICS() {
        return VIGILANT.getItalics();
    }

    public static Font CLASSIC() {
        return CLASSIC.getStandard();
    }

    public static Typeface getTypeface() {
        return typeface;
    }

    public static void setTypeface(final Typeface typeface) {
        // this skips menu redrawing
        if (Fonts.typeface == typeface)
            return;

        Fonts.typeface = typeface;

        gameStandard = typeface.standard;
        gameItalics = typeface.italics;
        gameItalicsSpaced = typeface.italicsSpaced;

        Translation.typefaceWasChanged();
    }
}

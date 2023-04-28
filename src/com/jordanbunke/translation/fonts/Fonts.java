package com.jordanbunke.translation.fonts;

import com.jordanbunke.jbjgl.fonts.Font;
import com.jordanbunke.jbjgl.fonts.FontFamily;
import com.jordanbunke.translation.io.ParserWriter;

import java.nio.file.Path;

public class Fonts {
    private static final Path FONT_FOLDER = ParserWriter.RESOURCE_ROOT.resolve("font_files");
    private static final FontFamily CLASSIC = FontFamily.loadFromSources(
            "Classic", FONT_FOLDER,
            "font-classic", FontFamily.NOT_AVAILABLE, "font-classic-italics",
            2, 2, 1, true
    );
    private static final Font CLASSIC_ITALICS_SPACED = Font.loadFromSource(
            FONT_FOLDER, "font-classic-italics", true, 2
    );
    private static final FontFamily VIGILANT = FontFamily.loadFromSources(
            "Vigilant", FONT_FOLDER,
            "font-vigilant", FontFamily.NOT_AVAILABLE, "font-vigilant-italics",
            2, 2, 0, true
    );
    private static final FontFamily MY_HANDWRITING = FontFamily.fromPreLoaded(
            "My Handwriting", Font.loadFromSource(FONT_FOLDER,
                    "font-my-handwriting", false, 1.0, 0, true),
            Font.loadFromSource(FONT_FOLDER, "font-my-handwriting-italics",
                    false, 1.0, 0, true),
            Font.loadFromSource(FONT_FOLDER, "font-my-handwriting-italics",
                    false, 1.0, -8, true)
    );
    private static final Font VIGILANT_ITALICS_SPACED = Font.loadFromSource(
            FONT_FOLDER, "font-vigilant-italics", true, 2
    );

    private static Font GAME_STANDARD;
    private static Font GAME_ITALICS;
    private static Font GAME_ITALICS_SPACED;

    static {
        setGameFontToVigilant();
    }

    public static Font GAME_STANDARD() {
        return GAME_STANDARD;
    }

    public static Font GAME_ITALICS() {
        return GAME_ITALICS;
    }

    public static Font GAME_ITALICS_SPACED() {
        return GAME_ITALICS_SPACED;
    }

    public static Font MY_HANDWRITING() {
        return MY_HANDWRITING.getStandard();
    }

    public static Font MY_ITALICIZED_HANDWRITING() {
        return MY_HANDWRITING.getItalics();
    }

    public static Font MY_SPACED_ITALICIZED_HANDWRITING() {
        return MY_HANDWRITING.getBold();
    }

    public static Font VIGILANT() {
        return VIGILANT.getStandard();
    }

    public static Font VIGILANT_ITALICS() {
        return VIGILANT.getItalics();
    }

    public static Font VIGILANT_ITALICS_SPACED() {
        return VIGILANT_ITALICS_SPACED;
    }

    public static Font CLASSIC() {
        return CLASSIC.getStandard();
    }

    public static Font CLASSIC_ITALICS() {
        return CLASSIC.getItalics();
    }

    public static Font CLASSIC_ITALICS_SPACED() {
        return CLASSIC_ITALICS_SPACED;
    }

    public static void setGameFontToMyHandwriting() {
        GAME_STANDARD = MY_HANDWRITING();
        GAME_ITALICS = MY_ITALICIZED_HANDWRITING();
        GAME_ITALICS_SPACED = MY_SPACED_ITALICIZED_HANDWRITING();
    }

    public static void setGameFontToVigilant() {
        GAME_STANDARD = VIGILANT();
        GAME_ITALICS = VIGILANT_ITALICS();
        GAME_ITALICS_SPACED = VIGILANT_ITALICS_SPACED();
    }

    public static void setGameFontToClassic() {
        GAME_STANDARD = CLASSIC();
        GAME_ITALICS = CLASSIC_ITALICS();
        GAME_ITALICS_SPACED = CLASSIC_ITALICS_SPACED();
    }
}

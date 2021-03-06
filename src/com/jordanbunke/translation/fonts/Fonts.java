package com.jordanbunke.translation.fonts;

import com.jordanbunke.jbjgl.fonts.Font;
import com.jordanbunke.jbjgl.fonts.FontFamily;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Fonts {
    private static final Path FONT_FOLDER = Paths.get("resources", "font_files");
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

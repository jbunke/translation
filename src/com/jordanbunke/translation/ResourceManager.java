package com.jordanbunke.translation;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.jbjgl.io.JBJGLResourceLoader;

import java.io.InputStream;
import java.nio.file.Path;

public class ResourceManager {
    private static final Path
            FONT_FILES_FOLDER = Path.of("font_files"),
            IMAGES_FOLDER = Path.of("images"),
            SOUNDS_FOLDER = Path.of("sounds"),
            TEXT_FOLDER = Path.of("text");

    public static String getTextResource(final Path path) {
        return JBJGLFileIO.readResource(getResource(path), path.toString());
    }

    public static JBJGLImage getImageResource(final Path path) {
        return JBJGLResourceLoader.loadImageResource(ResourceManager.class, path);
    }

    public static InputStream getResource(final Path path) {
        return JBJGLResourceLoader.loadResource(ResourceManager.class, path);
    }

    // GETTERS

    public static Path getFontFilesFolder() {
        return FONT_FILES_FOLDER;
    }

    public static Path getImagesFolder() {
        return IMAGES_FOLDER;
    }

    public static Path getSoundsFolder() {
        return SOUNDS_FOLDER;
    }

    public static Path getTextFolder() {
        return TEXT_FOLDER;
    }
}

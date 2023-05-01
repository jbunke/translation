package com.jordanbunke.translation;

import com.jordanbunke.jbjgl.Constants;
import com.jordanbunke.jbjgl.io.JBJGLFileIO;
import com.jordanbunke.jbjgl.io.JBJGLResourceLoader;
import com.jordanbunke.jbjgl.utility.JBJGLVersion;
import com.jordanbunke.jbjgl.utility.StringProcessing;
import com.jordanbunke.translation.io.ParserWriter;

import java.nio.file.Path;

public class Info {

    private static final String
            INFO_FILENAME = "translation_info.txt", CODEBASE_RESOURCE_ROOT = "res",
            TITLE_TAG = "title", VERSION_TAG = "version",
            GAMES_TAG = "games_link", GITHUB_TAG = "github_link", HIRE_ME_TAG = "hire_me_link",
            ITCH_TAG = "this_game_itch_link", TWITTER_TAG = "twitter_link",
            FAILED = "failed", RELEASE = "1.0.0";

    public static final String TITLE, MY_GAMES_LINK, MY_GITHUB_LINK, HIRE_ME_LINK,
            THIS_GAME_ITCH_LINK, MY_TWITTER_LINK;
    public static final JBJGLVersion VERSION;

    static {
        final String SEPARATOR = ":", OPEN = "{", CLOSE = "}";
        final int HAS_BUILD_LENGTH = 4, MAJOR = 0, MINOR = 1, PATCH = 2, BUILD = 3;

        final Path INFO_FILE = Path.of(INFO_FILENAME);
        final String contents = JBJGLFileIO.readResource(
                JBJGLResourceLoader.loadResource(Constants.class, INFO_FILE), INFO_FILENAME);

        TITLE = StringProcessing.getContentsFromTag(contents,
                TITLE_TAG, SEPARATOR, OPEN, CLOSE, FAILED);

        MY_GAMES_LINK = StringProcessing.getContentsFromTag(contents,
                GAMES_TAG, SEPARATOR, OPEN, CLOSE, FAILED);

        MY_GITHUB_LINK = StringProcessing.getContentsFromTag(contents,
                GITHUB_TAG, SEPARATOR, OPEN, CLOSE, FAILED);

        HIRE_ME_LINK = StringProcessing.getContentsFromTag(contents,
                HIRE_ME_TAG, SEPARATOR, OPEN, CLOSE, FAILED);

        THIS_GAME_ITCH_LINK = StringProcessing.getContentsFromTag(contents,
                ITCH_TAG, SEPARATOR, OPEN, CLOSE, FAILED);

        MY_TWITTER_LINK = StringProcessing.getContentsFromTag(contents,
                TWITTER_TAG, SEPARATOR, OPEN, CLOSE, FAILED);

        final String[] versionInfo = StringProcessing.getContentsFromTag(contents,
                VERSION_TAG, SEPARATOR, OPEN, CLOSE, RELEASE).split("\\.");

        if (versionInfo.length == HAS_BUILD_LENGTH)
            VERSION = JBJGLVersion.generate(Integer.parseInt(versionInfo[MAJOR]),
                    Integer.parseInt(versionInfo[MINOR]), Integer.parseInt(versionInfo[PATCH]),
                    Integer.parseInt(versionInfo[BUILD]));
        else
            VERSION = JBJGLVersion.generate(Integer.parseInt(versionInfo[MAJOR]),
                    Integer.parseInt(versionInfo[MINOR]), Integer.parseInt(versionInfo[PATCH]));
    }

    public static void writeInfoFile() {
        final String[] infoFileContents = new String[] {
                ParserWriter.encloseInTag(TITLE_TAG, TITLE),
                ParserWriter.encloseInTag(VERSION_TAG, VERSION.toString()),
                ParserWriter.encloseInTag(GAMES_TAG, MY_GAMES_LINK),
                ParserWriter.encloseInTag(GITHUB_TAG, MY_GITHUB_LINK),
                ParserWriter.encloseInTag(HIRE_ME_TAG, HIRE_ME_LINK),
                ParserWriter.encloseInTag(ITCH_TAG, THIS_GAME_ITCH_LINK),
                ParserWriter.encloseInTag(TWITTER_TAG, MY_TWITTER_LINK),
                ""
        };

        JBJGLFileIO.writeFile(Path.of(CODEBASE_RESOURCE_ROOT, INFO_FILENAME), infoFileContents);
    }
}

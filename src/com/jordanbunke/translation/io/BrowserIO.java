package com.jordanbunke.translation.io;

import com.jordanbunke.jbjgl.error.JBJGLError;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class BrowserIO {
    public static void openLink(URI uri) {
        final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        if (desktop == null) return;

        try {
            desktop.browse(uri);
        } catch (IOException e) {
            JBJGLError.send("Couldn't open link in browser: " + uri);
        }
    }
}

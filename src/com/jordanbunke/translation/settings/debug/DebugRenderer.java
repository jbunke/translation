package com.jordanbunke.translation.settings.debug;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.colors.TLColors;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DebugRenderer {
    private static final int PRINT_AT_FROM_CORNER = 4;

    private static final List<DebugMessage> messages;
    private static int fpsOnFile;
    private static JBJGLImage fpsImage;

    static {
        messages = new ArrayList<>();
        fpsOnFile = -1;
        fpsImage = drawFps();
    }

    public static void debugOutputFunction(final String message) {
        messages.add(0, DebugMessage.create(message));
    }

    public static void update() {
        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).age();

            if (messages.get(i).agedOut()) {
                messages.remove(i);
                i--;
            }
        }

        if (Translation.debugger.getFPS() != fpsOnFile) {
            fpsOnFile = Translation.debugger.getFPS();
            fpsImage = drawFps();
        }
    }

    public static void render(final Graphics g) {
        int printAtY = PRINT_AT_FROM_CORNER;

        for (DebugMessage message : messages) {
            JBJGLImage image = message.getImage();
            g.drawImage(image, PRINT_AT_FROM_CORNER, printAtY, null);
            printAtY += TechnicalSettings.pixelLockNumber(image.getHeight());
        }

        final int printFPSAtX = TechnicalSettings.getWidth() -
                TechnicalSettings.pixelLockNumber(fpsImage.getWidth() + PRINT_AT_FROM_CORNER);
        g.drawImage(fpsImage, printFPSAtX, PRINT_AT_FROM_CORNER, null);
    }

    private static JBJGLImage drawFps() {
        return JBJGLTextBuilder.initialize(TechnicalSettings.getPixelSize(),
                JBJGLText.Orientation.RIGHT, TLColors.DEBUG(TLColors.OPAQUE()),
                Fonts.CLASSIC()).addText(String.valueOf(fpsOnFile)).build().draw();
    }
}

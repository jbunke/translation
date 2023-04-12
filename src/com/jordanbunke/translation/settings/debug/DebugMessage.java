package com.jordanbunke.translation.settings.debug;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.colors.TLColors;

public class DebugMessage {
    private final JBJGLImage image;
    private int age;

    private DebugMessage(final String message) {
        image = JBJGLTextBuilder.initialize(1.,
                JBJGLText.Orientation.LEFT, TLColors.DEBUG(TLColors.OPAQUE()),
                Fonts.CLASSIC()).addText(message).build().draw();
        age = 0;
    }

    public static DebugMessage create(final String message) {
        return new DebugMessage(message);
    }

    public void age() {
        age++;
    }

    public boolean agedOut() {
        return age >= DebugSettings.DEBUG_MESSAGE_TIMEOUT;
    }

    public JBJGLImage getImage() {
        return image;
    }
}

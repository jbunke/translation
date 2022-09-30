package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.entities.Player;
import com.jordanbunke.translation.settings.GameplaySettings;
import com.jordanbunke.translation.settings.TechnicalSettings;
import com.jordanbunke.translation.colors.TLColors;

import java.awt.*;

public class LevelHUD {
    private static final int INIT_FOLLOW_MODE_UPDATE_COUNTER = 50;
    private static final int INIT_HINT_UPDATE_COUNTER = 100;

    private static int followModeUpdateCounter = 0;
    private static JBJGLImage followModeHUD;

    private static int hintUpdateCounter;
    private static JBJGLImage hintHUD;

    public static void update(final Level level) {
        if (followModeUpdateCounter > 0)
            followModeUpdateCounter--;

        if (hintUpdateCounter > 0)
            hintUpdateCounter--;

        // TODO - other elements
    }

    public static void render(final Level level, final Graphics g) {
        final Player p = level.getPlayer();

        final int pixel = TechnicalSettings.getPixelSize();
        final int width = TechnicalSettings.getWidth();
        final int height = TechnicalSettings.getHeight();

        // combo
        if (GameplaySettings.isShowingCombo()) {
            final int combo = p.getCombo();
            if (combo >= 2) {
                final double fraction =
                        1. - (p.getTicksSinceLastCrush() / (double) Player.TICKS_FOR_COMBO_TIMEOUT);
                final int maxComboRenderWidth = pixel * 20;
                final int comboRenderWidth =
                        TechnicalSettings.pixelLockNumber((int)(fraction * maxComboRenderWidth));

                final int comboRenderX = pixel * 4;
                final int comboRenderY = TechnicalSettings.pixelLockNumber((int)(height * 0.45));

                JBJGLImage comboCounterBackground = JBJGLTextBuilder.initialize(
                        4, JBJGLText.Orientation.LEFT, TLColors.BLACK(), Fonts.GAME_ITALICS()
                ).addText("x" + combo).build().draw();
                JBJGLImage comboCounter = JBJGLTextBuilder.initialize(
                        4, JBJGLText.Orientation.LEFT, TLColors.PLAYER(TLColors.OPAQUE()), Fonts.GAME_ITALICS()
                ).addText("x" + combo).build().draw();

                JBJGLImage comboTimeoutBar =
                        JBJGLImage.create(maxComboRenderWidth + (2 * pixel), pixel * 4);
                Graphics ctbg = comboTimeoutBar.getGraphics();

                ctbg.setColor(TLColors.BLACK());
                ctbg.fillRect(0, 0, maxComboRenderWidth + (2 * pixel), pixel * 4);

                ctbg.setColor(TLColors.WHITE(TLColors.FAINT()));
                ctbg.fillRect(pixel, pixel, maxComboRenderWidth, pixel * 2);

                ctbg.setColor(TLColors.PLAYER(TLColors.OPAQUE()));
                ctbg.fillRect(pixel, pixel, comboRenderWidth, pixel * 2);

                g.drawImage(comboCounterBackground, comboRenderX + pixel, comboRenderY, null);
                g.drawImage(comboCounter, comboRenderX, comboRenderY, null);
                g.drawImage(
                        comboTimeoutBar, comboRenderX,
                        comboRenderY + TechnicalSettings.pixelLockNumber((int)(comboCounter.getHeight() * 0.67)),
                        null
                );
            }
        }

        // camera follow mode update counter
        if (followModeUpdateCounter > 0 && GameplaySettings.isShowingFollowModeUpdates()) {
            g.drawImage(followModeHUD,
                    TechnicalSettings.pixelLockNumber((width / 2) - (followModeHUD.getWidth() / 2)),
                    height - followModeHUD.getHeight(), null);
        }

        // hint
        if (hintUpdateCounter > 0 && Translation.campaign.isShowingHint()) {
            g.drawImage(hintHUD,
                    TechnicalSettings.pixelLockNumber((width / 2) - (hintHUD.getWidth() / 2)) + pixel,
                    TechnicalSettings.pixelLockNumber((int)(height * 0.7)), null);
        }
    }

    public static void initializeHintUpdateCounter(final Level level) {
        hintUpdateCounter = INIT_HINT_UPDATE_COUNTER;
        hintHUD = generateHintHUD(level);
    }

    private static JBJGLImage generateHintHUD(final Level level) {
        return JBJGLTextBuilder.initialize(
                2, JBJGLText.Orientation.CENTER, TLColors.PLAYER(),
                Fonts.GAME_STANDARD()).addText(level.getHint()).build().draw();
    }

    public static void initializeFollowModeUpdateCounter(final Level level) {
        followModeUpdateCounter = INIT_FOLLOW_MODE_UPDATE_COUNTER;
        followModeHUD = generateFollowModeHUD(level);
    }

    private static JBJGLImage generateFollowModeHUD(final Level level) {
        final int pixel = TechnicalSettings.getPixelSize();
        final int width = TechnicalSettings.getWidth();
        // final int height = TechnicalSettings.getHeight();

        final int index = level.getCamera().getFollowMode().ordinal();
        final Camera.FollowMode[] followModes = Camera.FollowMode.values();

        final int followModeRenderWidth = TechnicalSettings.pixelLockNumber((int)(width * 0.15));

        final JBJGLImage followModeHUD = JBJGLImage.create(
                        followModeRenderWidth * followModes.length, pixel * 12
                );
        final Graphics g = followModeHUD.getGraphics();

        for (int i = 0; i < followModes.length; i++) {
            final boolean isActive = index == i;

            final int followModeRenderX = followModeRenderWidth * i;
            final int followModeRenderHeight = isActive ? pixel * 12 : pixel * 10;

            final Color background = isActive ? TLColors.PLAYER(TLColors.OPAQUE()) : TLColors.BLACK();
            final Color textColor = isActive ? TLColors.BLACK() : TLColors.PLAYER(TLColors.OPAQUE());

            final JBJGLImage text = JBJGLTextBuilder.initialize(
                    1, JBJGLText.Orientation.CENTER, textColor, Fonts.GAME_STANDARD()
            ).addText(followModes[i].toString()).build().draw();

            final JBJGLImage followMode = JBJGLImage.create(followModeRenderWidth, followModeRenderHeight);
            final Graphics fmg = followMode.getGraphics();

            fmg.setColor(background);
            fmg.fillRect(0, 0, followModeRenderWidth, followModeRenderHeight);
            fmg.drawImage(text, (followModeRenderWidth / 2) - (text.getWidth() / 2), pixel, null);

            g.drawImage(followMode, followModeRenderX, isActive ? 0 : pixel * 2, null);
        }

        return followModeHUD;
    }
}

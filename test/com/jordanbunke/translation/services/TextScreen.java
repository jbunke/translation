package com.jordanbunke.translation.services;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.game.JBJGLGame;
import com.jordanbunke.jbjgl.game.JBJGLGameManager;
import com.jordanbunke.jbjgl.menus.JBJGLMenu;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElementGrouping;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLTextMenuElement;
import com.jordanbunke.jbjgl.text.JBJGLText;
import com.jordanbunke.jbjgl.text.JBJGLTextBuilder;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.fonts.Fonts;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.menus.MenuHelper;
import com.jordanbunke.translation.menus.MenuIDs;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.util.Scanner;

public class TextScreen {
    private static final String QUIT_STRING = "!f", NEW_LINE = "\n";

    public static void main(String[] args) {
        prep();
        prompt();
        displayScreen(generateMenu(getText()));
    }

    private static void prep() {
        Fonts.setTypeface(Fonts.Typeface.CLASSIC);
        TLColors.setBackgroundToBlack();
    }

    private static void prompt() {
        System.out.println("Type the message you would like to print below,");
        System.out.println("and type \"" + QUIT_STRING + "\" on its own line to finish");
        System.out.println("-----------------------------------------------------");
    }

    private static String getText() {
        Scanner in = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        String line = in.nextLine();

        while (!line.equals(QUIT_STRING)) {
            sb.append(line);
            sb.append(NEW_LINE);
            line = in.nextLine();
        }

        return sb.toString();
    }

    private static JBJGLMenu generateMenu(final String text) {
        JBJGLMenuElementGrouping textElement =
                JBJGLMenuElementGrouping.generateOf(
                        textFormatter(text)
                );

        return MenuHelper.generatePlainMenu(textElement);
    }

    private static JBJGLTextMenuElement textFormatter(final String text) {
        JBJGLTextBuilder tb = JBJGLTextBuilder.initialize(
                5., JBJGLText.Orientation.CENTER, TLColors.PLAYER(), Fonts.gameItalics()
        );
        for (String line : text.split(NEW_LINE)) {
            tb.addText(line.equals("") ? " " : line);
            tb.addLineBreak();
        }

        return JBJGLTextMenuElement.generate(
                new int[] {
                        MenuHelper.widthCoord(0.5),
                        MenuHelper.heightCoord(0.5)
                }, JBJGLMenuElement.Anchor.CENTRAL, tb.build()
        );
    }

    private static void displayScreen(final JBJGLMenu toDisplay) {
        final JBJGLMenuManager menuManager =
                JBJGLMenuManager.initialize(toDisplay, MenuIDs.CUSTOM_TEXT);
        final JBJGLGameManager manager =
                JBJGLGameManager.createOf(0, menuManager);
        final JBJGLGame screen = JBJGLGame.create("", manager,
                TechnicalSettings.getWidth(), TechnicalSettings.getHeight(),
                ImageAssets.ICON,
                true, TechnicalSettings.isFullscreen(),
                GameplayConstants.UPDATE_HZ, GameplayConstants.TARGET_FPS);
        screen.getGameEngine().getDebugger().hideBoundingBoxes();
    }
}

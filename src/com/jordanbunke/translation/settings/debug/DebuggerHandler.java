package com.jordanbunke.translation.settings.debug;

import com.jordanbunke.translation.Translation;

public class DebuggerHandler {
    public static final String NOTIFICATION_CENTER_CHANNEL_ID = "NOTIFICATION";

    public static void printNotification(final String message) {
        printMessage(message, NOTIFICATION_CENTER_CHANNEL_ID);
    }

    public static void printMessage(final String message, final String channelID) {
        Translation.debugger.getChannel(channelID).printMessage(message);
    }
}

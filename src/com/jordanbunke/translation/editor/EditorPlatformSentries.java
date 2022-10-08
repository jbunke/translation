package com.jordanbunke.translation.editor;

import com.jordanbunke.translation.gameplay.entities.Sentry;

public class EditorPlatformSentries {
    public static class EditorSentrySpec {
        private static final int SPEED_INCREMENT = 2, DEFAULT_SPEED = 6;
        private Sentry.Role role;
        private Sentry.Role secondary;
        private int speed;
        private int direction;

        private EditorSentrySpec() {
            role = Sentry.Role.PUSHER;
            secondary = Sentry.Role.RANDOM;

            speed = DEFAULT_SPEED;
            direction = Sentry.LEFT;
        }

        public static EditorSentrySpec create() {
            return new EditorSentrySpec();
        }

        public void speedDown() {
            speedChange(direction == Sentry.LEFT);
        }

        public void speedUp() {
            speedChange(direction == Sentry.RIGHT);
        }

        private void speedChange(final boolean condition) {
            if (speed == SPEED_INCREMENT && !condition) {
                direction *= -1;
                return;
            }

            speed = condition ?
                    Math.min(speed + SPEED_INCREMENT, Sentry.MAX_SENTRY_SPEED) :
                    Math.max(speed - SPEED_INCREMENT, SPEED_INCREMENT);
        }
    }

    private EditorPlatformSentries() {

    }


}

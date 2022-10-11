package com.jordanbunke.translation.editor;

import com.jordanbunke.translation.gameplay.entities.Sentry;

import java.util.ArrayList;
import java.util.List;

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

        public void nextRole(final boolean changeSecondary) {
            final int TOTAL = Sentry.Role.values().length;
            final int setToIndex = ((changeSecondary ? secondary : role).ordinal() + 1) % TOTAL;

            final Sentry.Role setTo = Sentry.Role.values()[setToIndex];

            if (changeSecondary)
                secondary = setTo;
            else
                role = setTo;
        }

        public int getSpeed() {
            return speed;
        }

        public int getDirection() {
            return direction;
        }

        public Sentry.Role getRole() {
            return role;
        }
    }

    private static final int NO_SENTRIES_INDEX = -1;

    private final List<EditorSentrySpec> sentrySpecs;
    private int sentryIndex;
    private int renderSentryIndex;
    private boolean selected;

    private EditorPlatformSentries() {
        sentrySpecs = new ArrayList<>();
        sentryIndex = NO_SENTRIES_INDEX;
        renderSentryIndex = NO_SENTRIES_INDEX;
        selected = false;
    }

    public static EditorPlatformSentries create() {
        return new EditorPlatformSentries();
    }

    public void createSentry() {
        sentrySpecs.add(EditorSentrySpec.create());
        sentryIndex = sentrySpecs.size() - 1;
    }

    public void deleteSentry() {
        if (!sentrySpecs.isEmpty()) {
            sentrySpecs.remove(sentryIndex);

            while (sentryIndex >= sentrySpecs.size())
                sentryIndex--;

            renderSentryIndex = 0;
        }
    }

    public void toggleSentryIndex() {
        if (!sentrySpecs.isEmpty())
            sentryIndex = (sentryIndex + 1) % sentrySpecs.size();
    }

    public void toggleRenderSentryIndex() {
        if (!sentrySpecs.isEmpty())
            renderSentryIndex = (renderSentryIndex + 1) % sentrySpecs.size();
    }

    public void select() {
        selected = true;
    }

    public void deselect() {
        selected = false;
        renderSentryIndex = 0;
    }

    public boolean isNotEmpty() {
        return !sentrySpecs.isEmpty();
    }

    public boolean hasMultiple() {
        return sentrySpecs.size() > 1;
    }

    public EditorSentrySpec getCurrentSentry() {
        return sentrySpecs.get(sentryIndex);
    }
}

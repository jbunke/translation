package com.jordanbunke.translation.editor;

import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.level.SentrySpec;
import com.jordanbunke.translation.sound.Sounds;

import java.util.ArrayList;
import java.util.List;

public class EditorPlatformSentries {
    public static class EditorSentrySpec {
        private static final int SPEED_INCREMENT = 2, DEFAULT_SPEED = 6;
        private Sentry.Role role;
        private Sentry.Role secondary;
        private int speed;
        private int direction;
        private int counter = 0, counterMax = 0;

        private EditorSentrySpec() {
            role = Sentry.Role.PUSHER;
            secondary = Sentry.Role.RANDOM;

            speed = DEFAULT_SPEED;
            direction = Sentry.LEFT;

            setCounter();
        }

        private EditorSentrySpec(final SentrySpec sentrySpec) {
            role = sentrySpec.getRole();
            secondary = sentrySpec.getSecondary();

            speed = Math.abs(sentrySpec.getInitialMovement());
            direction = (int)Math.signum(sentrySpec.getInitialMovement());

            setCounter();
        }

        public static EditorSentrySpec create() {
            return new EditorSentrySpec();
        }

        public static EditorSentrySpec fromSentrySpec(final SentrySpec sentrySpec) {
            return new EditorSentrySpec(sentrySpec);
        }

        public SentrySpec toSentrySpec(final int platformIndex) {
            final int initialMovement = speed * direction;
            return role == Sentry.Role.SPAWNER
                    ? SentrySpec.defineSpawner(secondary, platformIndex, initialMovement)
                    : SentrySpec.define(role, platformIndex, initialMovement);
        }

        public void speedDown() {
            speedChange(direction == Sentry.LEFT);
        }

        public void speedUp() {
            speedChange(direction == Sentry.RIGHT);
        }

        private void speedChange(final boolean condition) {
            Sounds.tick();

            if (speed == SPEED_INCREMENT && !condition) {
                direction *= -1;
                return;
            }

            speed = condition ?
                    Math.min(speed + SPEED_INCREMENT, Sentry.MAX_SENTRY_SPEED) :
                    Math.max(speed - SPEED_INCREMENT, SPEED_INCREMENT);
        }

        public void nextRole(final boolean changeSecondary) {
            final Sentry.Role setTo = (changeSecondary ? secondary : role).next();

            Sounds.tick();

            if (changeSecondary)
                secondary = setTo;
            else {
                role = setTo;

                if (setTo == Sentry.Role.SPAWNER)
                    secondary = Sentry.Role.RANDOM;
                else
                    secondary = setTo;

                setCounter();
            }
        }

        public void updateCounter() {
            counter++;
            counter %= counterMax;
        }

        private void setCounter() {
            counterMax = switch (role) {
                case SPAWNER -> Sentry.SPAWN_CYCLE;
                case NOMAD -> Sentry.NOMADIC_CYCLE;
                case NECROMANCER -> Sentry.REVIVAL_CYCLE;
                case ANCHOR, MAGNET, FEATHER -> Sentry.ANIMATION_CYCLE;
                default -> 1;
            };
            counter = 0;
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

        public Sentry.Role getSecondaryRole() {
            return secondary;
        }

        public int getCounter() {
            return counter;
        }
    }

    private static final int NO_SENTRIES_INDEX = -1;

    private final List<EditorSentrySpec> sentrySpecs;
    private int selectedIndex;
    private int renderSentryIndex;
    private boolean selected;

    private EditorPlatformSentries() {
        sentrySpecs = new ArrayList<>();
        selectedIndex = NO_SENTRIES_INDEX;
        renderSentryIndex = NO_SENTRIES_INDEX;
        selected = false;
    }

    public static EditorPlatformSentries create() {
        return new EditorPlatformSentries();
    }

    public void createSentry() {
        createSentry(EditorSentrySpec.create());
    }

    public void createSentry(final EditorSentrySpec sentrySpec) {
        Sounds.actionSucceeded();

        sentrySpecs.add(sentrySpec);
        selectedIndex = sentrySpecs.size() - 1;
        renderSentryIndex = selectedIndex;
    }

    public void deleteSentry() {
        if (!sentrySpecs.isEmpty()) {
            Sounds.actionFailed();

            sentrySpecs.remove(selectedIndex);

            while (selectedIndex >= sentrySpecs.size())
                selectedIndex--;

            renderSentryIndex = 0;
        }
    }

    public void toggleSentryIndex() {
        if (!sentrySpecs.isEmpty()) {
            Sounds.actionSucceeded();

            selectedIndex = (selectedIndex + 1) % sentrySpecs.size();
        }
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

    public EditorSentrySpec get(final int index) {
        return sentrySpecs.get(index);
    }

    public EditorSentrySpec getSelectedSentry() {
        return sentrySpecs.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public EditorSentrySpec getRenderSentry() {
        return sentrySpecs.get(renderSentryIndex);
    }

    public int getSize() {
        return sentrySpecs.size();
    }

    public boolean isSelected() {
        return selected;
    }
}

package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.gameplay.entities.Platform;
import com.jordanbunke.translation.gameplay.entities.Sentry;

import java.util.List;

public class SentrySpec {
    private final Sentry.Role role;
    private final Sentry.Role secondary;
    private final int platformIndex;
    private final int initialMovement;

    private SentrySpec(
            final Sentry.Role role, final Sentry.Role secondary,
            final int platformIndex, final int initialMovement
    ) {
        this.role = role;
        this.secondary = secondary;
        this.platformIndex = platformIndex;
        this.initialMovement = initialMovement;
    }

    public static SentrySpec define(
            final Sentry.Role role, final int platformIndex, final int initialMovement
    ) {
        return new SentrySpec(role, role, platformIndex, initialMovement);
    }

    public static SentrySpec defineSpawner(
            final Sentry.Role secondary, final int platformIndex, final int initialMovement
    ) {
        return new SentrySpec(Sentry.Role.SPAWNER, secondary, platformIndex, initialMovement);
    }

    public Sentry generate(
            final Level level, final List<Platform> platforms
            ) {
        if (platformIndex >= platforms.size())
            Translation.debugger.getChannel(JBJGLGameDebugger.LOGIC_CHANNEL).printMessage(
                    "Faulty level definition. Sentry specification platform index is too high."
            );
        return Sentry.create(role, secondary, level, platforms.get(platformIndex), initialMovement);
    }

    // TODO - validity of initial movement (w/ debugger message if initially invalid)

    // GETTERS

    public Sentry.Role getRole() {
        return role;
    }

    public Sentry.Role getSecondary() {
        return secondary;
    }

    public int getPlatformIndex() {
        return platformIndex;
    }

    public int getInitialMovement() {
        return initialMovement;
    }
}

package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.translation.gameplay.entities.Sentry;

public class LevelBuilder {
    public static Level dummyLevel() {
        PlatformSpec[] platformSpecs = new PlatformSpec[] {
                PlatformSpec.define(0, 0, 200),
                PlatformSpec.define(-500, 0, 200),
                PlatformSpec.define(500, 0, 200),
                PlatformSpec.define(0, -400, 200),
                PlatformSpec.define(-500, -400, 200),
                PlatformSpec.define(500, -400, 200),
                PlatformSpec.define(0, -200, 200),
                PlatformSpec.define(-500, -200, 200),
                PlatformSpec.define(500, -200, 200),
                PlatformSpec.define(0, 200, 200),
                PlatformSpec.define(-500, 200, 200),
                PlatformSpec.define(500, 200, 200),
                PlatformSpec.define(0, 400, 200),
                PlatformSpec.define(-500, 400, 200),
                PlatformSpec.define(500, 400, 200)
        };
        SentrySpec[] sentrySpecs = new SentrySpec[] {
                SentrySpec.define(Sentry.Role.NECROMANCER, 3, -10),
                SentrySpec.define(Sentry.Role.NOMAD, 4, -6),
                SentrySpec.define(Sentry.Role.NOMAD, 5, 6),
                SentrySpec.define(Sentry.Role.RANDOM, 6, 10),
                SentrySpec.define(Sentry.Role.NOMAD, 7, -6),
                SentrySpec.define(Sentry.Role.NOMAD, 8, 6),
                SentrySpec.define(Sentry.Role.RANDOM, 9, -10),
                SentrySpec.define(Sentry.Role.NOMAD, 10, -6),
                SentrySpec.define(Sentry.Role.NOMAD, 11, 6),
                SentrySpec.define(Sentry.Role.NECROMANCER, 12, 10),
                SentrySpec.define(Sentry.Role.NOMAD, 13, -6),
                SentrySpec.define(Sentry.Role.NOMAD, 14, 6)
        };

        return Level.create("Test Level", "* get good *", platformSpecs, sentrySpecs);
    }
}

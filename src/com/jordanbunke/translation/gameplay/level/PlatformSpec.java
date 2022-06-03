package com.jordanbunke.translation.gameplay.level;

import com.jordanbunke.translation.gameplay.entities.Platform;

public class PlatformSpec {
    private final int x;
    private final int y;
    private final int width;

    private PlatformSpec(final int x, final int y, final int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public static PlatformSpec define(
            final int x, final int y, final int width
    ) {
        return new PlatformSpec(x, y, width);
    }

    public Platform generate() {
        return Platform.create(x, y, width);
    }
}

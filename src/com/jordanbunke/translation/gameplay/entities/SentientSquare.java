package com.jordanbunke.translation.gameplay.entities;

import com.jordanbunke.translation.gameplay.level.Level;

public abstract class SentientSquare extends Entity {
    private final Level level;

    private int speed;
    private boolean highlighted; // whether square outline is black or white

    SentientSquare(final int x, final int y, final Level level) {
        super(x, y);

        this.level = level;
        highlighted = false;
    }

    public void setSpeed(final int speed) {
        this.speed = speed;
    }

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public int getSpeed() {
        return speed;
    }

    public Level getLevel() {
        return level;
    }
}

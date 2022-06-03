package com.jordanbunke.translation.gameplay;

import com.jordanbunke.jbjgl.utility.RenderConstants;

public abstract class HasPosition {
    private final int[] position;

    public HasPosition(final int x, final int y) {
        this.position = new int[] { x, y };
    }

    public void setPosition(final int x, final int y) {
        position[RenderConstants.X] = x;
        position[RenderConstants.Y] = y;
    }

    public void setX(final int x) {
        position[RenderConstants.X] = x;
    }

    public void setY(final int y) {
        position[RenderConstants.Y] = y;
    }

    public void incrementX(final int deltaX) {
        position[RenderConstants.X] += deltaX;
    }

    public void incrementY(final int deltaY) {
        position[RenderConstants.Y] += deltaY;
    }

    public int[] getPosition() {
        return position;
    }
}

package com.jordanbunke.translation.gameplay.entities;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.HasPosition;

import java.awt.*;

public abstract class Entity extends HasPosition {
    Entity(final int x, final int y) {
        super(x, y);
    }

    public abstract void render(
            final Camera camera, final Graphics g, final JBJGLGameDebugger debugger
    );
}

package com.jordanbunke.translation.io;

import com.jordanbunke.jbjgl.events.JBJGLKey;
import com.jordanbunke.jbjgl.events.JBJGLKeyEvent;

import java.util.HashMap;
import java.util.Map;

public class ControlScheme {
    private static final ControlScheme DEFAULT = new ControlScheme(
            Map.ofEntries(
                    Action.STOP_MOVING_CAM_DOWN.defaultPairing(),
                    Action.STOP_MOVING_CAM_UP.defaultPairing(),
                    Action.STOP_MOVING_CAM_RIGHT.defaultPairing(),
                    Action.STOP_MOVING_CAM_LEFT.defaultPairing(),
                    Action.STOP_MOVING_RIGHT.defaultPairing(),
                    Action.STOP_MOVING_LEFT.defaultPairing(),
                    Action.MOVE_CAM_DOWN.defaultPairing(),
                    Action.MOVE_CAM_UP.defaultPairing(),
                    Action.MOVE_CAM_RIGHT.defaultPairing(),
                    Action.MOVE_CAM_LEFT.defaultPairing(),
                    Action.MOVE_RIGHT.defaultPairing(),
                    Action.MOVE_LEFT.defaultPairing(),
                    Action.JUMP.defaultPairing(),
                    Action.DROP.defaultPairing(),
                    Action.SAVE_POS.defaultPairing(),
                    Action.LOAD_POS.defaultPairing(),
                    Action.INIT_TELEPORT.defaultPairing(),
                    Action.TELEPORT.defaultPairing(),
                    Action.TOGGLE_FOLLOW_MODE.defaultPairing(),
                    Action.TOGGLE_ZOOM.defaultPairing(),
                    Action.PAUSE.defaultPairing(),
                    Action.SNAP_TO_GRID.defaultPairing())
    );

    private final static ControlScheme controlScheme = initialize();

    private final Map<Action, JBJGLKeyEvent> inputMap;

    public enum Action {
        MOVE_LEFT, MOVE_RIGHT,
        STOP_MOVING_LEFT, STOP_MOVING_RIGHT,
        JUMP, DROP,
        INIT_TELEPORT, TELEPORT,
        SAVE_POS, LOAD_POS,
        TOGGLE_ZOOM, TOGGLE_FOLLOW_MODE,
        MOVE_CAM_LEFT, MOVE_CAM_RIGHT, MOVE_CAM_UP, MOVE_CAM_DOWN,
        STOP_MOVING_CAM_LEFT, STOP_MOVING_CAM_RIGHT, STOP_MOVING_CAM_UP, STOP_MOVING_CAM_DOWN,
        PAUSE,
        // EDITOR-SPECIFIC
        SNAP_TO_GRID
        ;

        private JBJGLKeyEvent.Action keyEventType() {
            return switch (this) {
                case STOP_MOVING_CAM_DOWN,
                        STOP_MOVING_CAM_LEFT,
                        STOP_MOVING_CAM_RIGHT,
                        STOP_MOVING_CAM_UP,
                        STOP_MOVING_LEFT,
                        STOP_MOVING_RIGHT,
                        SAVE_POS,
                        LOAD_POS,
                        TELEPORT,
                        TOGGLE_FOLLOW_MODE,
                        TOGGLE_ZOOM -> JBJGLKeyEvent.Action.RELEASE;
                default -> JBJGLKeyEvent.Action.PRESS;
            };
        }

        private JBJGLKey defaultKey() {
            return switch (this) {
                case PAUSE -> JBJGLKey.ESCAPE;
                case DROP -> JBJGLKey.S;
                case JUMP -> JBJGLKey.W;
                case LOAD_POS -> JBJGLKey.E;
                case SAVE_POS -> JBJGLKey.Q;
                case TELEPORT, INIT_TELEPORT -> JBJGLKey.SPACE;
                case MOVE_LEFT, STOP_MOVING_LEFT -> JBJGLKey.A;
                case MOVE_RIGHT, STOP_MOVING_RIGHT -> JBJGLKey.D;
                case MOVE_CAM_DOWN, STOP_MOVING_CAM_DOWN -> JBJGLKey.DOWN_ARROW;
                case MOVE_CAM_LEFT, STOP_MOVING_CAM_LEFT -> JBJGLKey.LEFT_ARROW;
                case MOVE_CAM_RIGHT, STOP_MOVING_CAM_RIGHT -> JBJGLKey.RIGHT_ARROW;
                case MOVE_CAM_UP, STOP_MOVING_CAM_UP -> JBJGLKey.UP_ARROW;
                case TOGGLE_ZOOM -> JBJGLKey.Z;
                case TOGGLE_FOLLOW_MODE -> JBJGLKey.X;
                case SNAP_TO_GRID -> JBJGLKey.C;
            };
        }

        private JBJGLKeyEvent defaultKeyEvent() {
            return JBJGLKeyEvent.generate(defaultKey(), keyEventType());
        }

        public Map.Entry<Action, JBJGLKeyEvent> defaultPairing() {
            return Map.entry(this, defaultKeyEvent());
        }
    }

    private ControlScheme(final Map<Action, JBJGLKeyEvent> inputMap) {
        this.inputMap = inputMap;
    }

    public static ControlScheme get() {
        return controlScheme;
    }

    public static boolean update(final Action action, final JBJGLKey key) {
        if (containsKey(key, action))
            return false;

        controlScheme.inputMap.put(
                action, JBJGLKeyEvent.generate(
                        key, action.keyEventType()
                )
        );
        return true;
    }

    public static void reset() {
        for (Action action : Action.values())
            controlScheme.inputMap.put(action, JBJGLKeyEvent.generate(
                    action.defaultKey(), action.keyEventType()
            ));
    }

    private static boolean containsKey(final JBJGLKey key, final Action reference) {
        for (Action action : controlScheme.inputMap.keySet())
            if (ControlScheme.getCorrespondingKey(action).equals(key) && !pairedActions(action, reference))
                return true;

        return false;
    }

    private static boolean pairedActions(final Action a, final Action b) {
        return a.defaultKey() == b.defaultKey();
    }

    private static ControlScheme initialize() {
        return new ControlScheme(
                new HashMap<>(DEFAULT.inputMap)
        );
    }

    public static JBJGLKeyEvent getKeyEvent(final Action action) {
        return controlScheme.inputMap.get(action);
    }

    public static JBJGLKey getCorrespondingKey(final Action action) {
        return controlScheme.inputMap.get(action).getKey();
    }
}

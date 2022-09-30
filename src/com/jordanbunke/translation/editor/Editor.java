package com.jordanbunke.translation.editor;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.entities.Entity;
import com.jordanbunke.translation.gameplay.entities.Platform;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Editor {
    private static final int DEFAULT_PLATFORM_WIDTH = 200;
    private static final int[] DEFAULT_SECOND_PLATFORM_POSITION = new int[] { 0, -200 };

    private static final int MESH_SIZE = 40;

    private static Platform startingPlatform = generateStartingPlatform();
    private static List<Platform> additionalPlatforms = defaultAdditionalPlatforms();
    private static List<Sentry> sentries = new ArrayList<>();
    private static Camera camera = Camera.createForEditor();
    private static Entity selectedEntity = null;
    private static Entity highlightedEntity = null;

    private static String selectionText = determineSelectionText();

    public static void initialize() {
        reset();
    }

    public static void update() {
        camera.update();

        highlightedEntity = determineHighlightedEntity();
        selectionText = determineSelectionText();
        // TODO

    }

    public static void render(
            final Graphics g, final JBJGLGameDebugger debugger
    ) {
        // background
        g.drawImage(ImageAssets.BACKGROUND(), 0, 0, null);

        // mesh
        renderMesh(g);

        // platforms
        startingPlatform.renderForEditor(camera, g, debugger);

        for (Platform p : additionalPlatforms)
            p.renderForEditor(camera, g, debugger);

        // sentries
        for (Sentry s : sentries)
            s.renderSquare(camera, g);

        // HUD
        EditorHUD.render(g);
    }

    private static void renderMesh(
            final Graphics g
    ) {
        int[] cp = getCursorPosition();

        // determine coordinates of camera bounds
        final int width = TechnicalSettings.getWidth(),
                height = TechnicalSettings.getHeight(),
                zoomMultiplier = camera.isZoomedIn() ? 1 : 2,
                adjustedSize = MESH_SIZE / zoomMultiplier;

        final int[] screenMiddle = new int[] { width / 2, height / 2 };

        final int modX = cp[RenderConstants.X] % MESH_SIZE,
                modY = cp[RenderConstants.Y] % MESH_SIZE;
        final int[] closestMesh = new int[] {
                cp[RenderConstants.X] - modX, cp[RenderConstants.Y] - modY
        };
        final int[] middleMesh = new int[] {
                screenMiddle[RenderConstants.X] -
                        ((cp[RenderConstants.X] - closestMesh[RenderConstants.X]) / zoomMultiplier),
                screenMiddle[RenderConstants.Y] -
                        ((cp[RenderConstants.Y] - closestMesh[RenderConstants.Y]) / zoomMultiplier)
        };
        final int[] startingMesh = new int[] {
                middleMesh[RenderConstants.X], middleMesh[RenderConstants.Y]
        };

        while (startingMesh[RenderConstants.X] > 0) {
            startingMesh[RenderConstants.X] -= adjustedSize;
        }

        while (startingMesh[RenderConstants.Y] > 0) {
            startingMesh[RenderConstants.Y] -= adjustedSize;
        }

        final int repsX = (width / adjustedSize) + 2;
        final int repsY = (height / adjustedSize) + 2;

        g.setColor(TLColors.BLACK());

        // vertical lines
        for (int x = 0; x < repsX; x++)
            g.fillRect(startingMesh[RenderConstants.X] + (x * adjustedSize), 0, 1, height);

        // horizontal lines
        for (int y = 0; y < repsY; y++)
            g.fillRect(0, startingMesh[RenderConstants.Y] + (y * adjustedSize), width, 1);
    }

    public static void process(
            final JBJGLListener listener
    ) {
        processCamera(listener);
        processSelection(listener);
        processPlatform(listener);

        // TODO - process platform manipulation/addition/deletion controls & sentry addition/deletion/customization

    }

    private static void processCamera(
            final JBJGLListener listener
    ) {
        // toggle camera zoom
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.TOGGLE_ZOOM),
                () -> camera.toggleZoom()
        );

        // snap cursor/camera to grid
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.SNAP_TO_GRID),
                Editor::snapToGrid
        );

        // move camera
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_LEFT),
                () -> camera.setIsMovingLeft(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_RIGHT),
                () -> camera.setIsMovingRight(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_UP),
                () -> camera.setIsMovingUp(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_CAM_DOWN),
                () -> camera.setIsMovingDown(true)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_LEFT),
                () -> camera.setIsMovingLeft(false)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_RIGHT),
                () -> camera.setIsMovingRight(false)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_UP),
                () -> camera.setIsMovingUp(false)
        );
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.STOP_MOVING_CAM_DOWN),
                () -> camera.setIsMovingDown(false)
        );
    }

    private static void processSelection(
            final JBJGLListener listener
    ) {
        // select / deselect
        listener.checkForMatchingKeyStroke(
                ControlScheme.getKeyEvent(ControlScheme.Action.INIT_TELEPORT),
                Editor::select
        );
    }

    private static void processPlatform(
            final JBJGLListener listener
    ) {
        // add platform
        if (canCreatePlatform())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.SAVE_POS),
                    Editor::addPlatform
            );

        // delete platform
        if (canDeletePlatform())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.LOAD_POS),
                    Editor::deletePlatform
            );

        // TODO - mode, move, expand, contract
    }

    private static Platform generateStartingPlatform() {
        return Platform.create(0, 0, DEFAULT_PLATFORM_WIDTH);
    }

    private static List<Platform> defaultAdditionalPlatforms() {
        return new ArrayList<>(
                List.of(
                        Platform.create(
                                DEFAULT_SECOND_PLATFORM_POSITION[RenderConstants.X],
                                DEFAULT_SECOND_PLATFORM_POSITION[RenderConstants.Y],
                                DEFAULT_PLATFORM_WIDTH)
                )
        );
    }

    public static void reset() {
        startingPlatform = generateStartingPlatform();
        additionalPlatforms = defaultAdditionalPlatforms();
        sentries = new ArrayList<>();
        camera = Camera.createForEditor();
        selectedEntity = null;
        highlightedEntity = null;
        selectionText = determineSelectionText();
    }

    // BEHAVIOURS
    private static Entity determineHighlightedEntity() {
        /* 1 - populate ALL platforms and sentries into an entity collection
         * 2 - check whether cursor position overlaps with the bounds of any entities
         * 3 - set selectedEntity to best match, or null if none found */

        List<Entity> allEntities = new ArrayList<>();
        allEntities.add(startingPlatform);
        allEntities.addAll(additionalPlatforms);
        allEntities.addAll(sentries);

        int[] cp = getCursorPosition();

        for (Entity e : allEntities)
            if (e instanceof Platform p && p.isHighlighted(cp))
                return e;
            else if (e instanceof Sentry s && s.isHighlighted(cp))
                return e;

        return null;
    }

    private static void select() {
        selectedEntity = highlightedEntity;
    }

    private static void addPlatform() {
        final int[] p = getCursorPosition();

        additionalPlatforms.add(Platform.create(
                p[RenderConstants.X], p[RenderConstants.Y], DEFAULT_PLATFORM_WIDTH
        ));
    }

    private static void deletePlatform() {
        additionalPlatforms.remove((Platform) selectedEntity);
        selectedEntity = null;
    }

    private static void snapToGrid() {
        int[] cp = getCursorPosition();

        final int[] mod = new int[] {
                cp[RenderConstants.X] % MESH_SIZE,
                cp[RenderConstants.Y] % MESH_SIZE
        };

        if (mod[RenderConstants.X] < 0)
            mod[RenderConstants.X] += MESH_SIZE;
        if (mod[RenderConstants.Y] < 0)
            mod[RenderConstants.Y] += MESH_SIZE;

        final int[] snapPosition = new int[] {
                mod[RenderConstants.X] > (MESH_SIZE / 2)
                        ? cp[RenderConstants.X] +
                            (MESH_SIZE - mod[RenderConstants.X])
                        : cp[RenderConstants.X] - mod[RenderConstants.X],
                mod[RenderConstants.Y] > (MESH_SIZE / 2)
                        ? cp[RenderConstants.Y] +
                            (MESH_SIZE - mod[RenderConstants.Y])
                        : cp[RenderConstants.Y] - mod[RenderConstants.Y]
        };

        camera.setPosition(
                snapPosition[RenderConstants.X],
                snapPosition[RenderConstants.Y]
        );
    }

    // HELPER
    private static String determineSelectionText() {
        String name = "";

        if (selectedEntity != null)
            name = determineNameOfEntity(selectedEntity).toUpperCase();
        else if (highlightedEntity != null)
            name = ControlScheme.getCorrespondingKey(
                    ControlScheme.Action.INIT_TELEPORT
            ).print() + " to select - " +
                    determineNameOfEntity(highlightedEntity).toUpperCase();

        return name;
    }

    private static String determineNameOfEntity(final Entity entity) {
        final String INVALID_SELECTION = "???";

        if (entity instanceof Platform p) {
            if (p.equals(startingPlatform))
                return "Starting Platform";
            else if (additionalPlatforms.contains(p)) {
                final int adjustedIndex = additionalPlatforms.indexOf(p) + 2;
                return "Platform " + adjustedIndex;
            }
        }

        Translation.debugger.getChannel(JBJGLGameDebugger.LOGIC_CHANNEL).printMessage(
                "Selected entity is not in the list of platforms."
        );

        return INVALID_SELECTION;
    }

    // GETTER / SETTER
    public static int[] getCursorPosition() {
        return camera.getPosition();
    }

    public static Platform getStartingPlatform() {
        return startingPlatform;
    }

    public static Entity getSelectedEntity() {
        return selectedEntity;
    }

    public static Entity getHighlightedEntity() {
        return highlightedEntity;
    }

    public static String getSelectionText() {
        return selectionText;
    }

    // CONTROL PROMPT CHECKERS
    public static boolean canCreatePlatform() {
        // TODO - more conditions
        final boolean noHighlightedEntity = highlightedEntity == null;
        return noHighlightedEntity;
    }

    public static boolean canDeletePlatform() {
        if (selectedEntity instanceof Platform selectedPlatform) {
            final boolean isNotStartingPlatform = !selectedPlatform.equals(startingPlatform),
                    hasMultipleAdditionalPlatforms = additionalPlatforms.size() > 1;

            return isNotStartingPlatform && hasMultipleAdditionalPlatforms;
        }

        return false;
    }
}

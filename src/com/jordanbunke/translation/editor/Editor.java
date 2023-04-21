package com.jordanbunke.translation.editor;

import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.events.JBJGLKeyEvent;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.Translation;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.gameplay.Camera;
import com.jordanbunke.translation.gameplay.entities.Entity;
import com.jordanbunke.translation.gameplay.entities.Platform;
import com.jordanbunke.translation.gameplay.entities.Sentry;
import com.jordanbunke.translation.gameplay.image.ImageAssets;
import com.jordanbunke.translation.gameplay.level.PlatformSpec;
import com.jordanbunke.translation.gameplay.level.SentrySpec;
import com.jordanbunke.translation.io.ControlScheme;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Editor {

    public enum Mode {
        MOVE_PLATFORM, RESIZE_PLATFORM, SENTRY
        ;

        Mode next() {
            resetPlatformMovementVariables();
            resetPlatformSizingVariables();

            return switch (this) {
                case MOVE_PLATFORM -> RESIZE_PLATFORM;
                case RESIZE_PLATFORM -> SENTRY;
                case SENTRY -> MOVE_PLATFORM;
            };
        }

        String print() {
            return switch (this) {
                case MOVE_PLATFORM -> "MOVE PLATFORM";
                case RESIZE_PLATFORM -> "RESIZE PLATFORM";
                case SENTRY -> "EDIT SENTRIES";
            };
        }
    }

    private static final int DEFAULT_PLATFORM_WIDTH = 200;
    private static final int SENTRY_RENDER_CYCLE = 200;
    private static final int[] DEFAULT_SECOND_PLATFORM_POSITION = new int[] { 0, -200 };

    private static final int MESH_SIZE = 40;

    private static Mode mode = Mode.MOVE_PLATFORM;

    private static boolean platformIsMovingLeft = false,
            platformIsMovingRight = false,
            platformIsMovingUp = false,
            platformIsMovingDown = false,
            platformIsExpanding = false,
            platformIsContracting = false;

    private static Platform startingPlatform = generateStartingPlatform();
    private static List<Platform> additionalPlatforms = defaultAdditionalPlatforms();
    private static Map<Platform, EditorPlatformSentries> sentriesMap = generateSentriesMap();
    private static Camera camera = Camera.createForEditor();
    private static Platform selectedPlatform = null;
    private static Platform highlightedPlatform = null;

    private static String selectionText = determineSelectionText();

    private static int sentryRenderCounter;

    public static void initialize() {
        reset();
    }

    public static void update() {
        camera.update();
        updatePlatformMovement();

        highlightedPlatform = determineHighlightedPlatform();
        selectionText = determineSelectionText();

        // Sentry rendering updates
        updateSentryRendering();

        // HUD updates
        EditorHUD.update();
    }

    private static void updatePlatformMovement() {
        final int DELTA = 1;

        if (platformIsSelected()) {
            Platform p = selectedPlatform;

            if (platformIsMovingLeft)
                p.incrementX(-DELTA);
            if (platformIsMovingRight)
                p.incrementX(DELTA);
            if (platformIsMovingUp)
                p.incrementY(-DELTA);
            if (platformIsMovingDown)
                p.incrementY(DELTA);

            if (platformIsExpanding && p.getWidth() < Sentry.MAX_PLATFORM_WIDTH)
                p.changeWidth(DELTA);
            if (platformIsContracting && p.getWidth() > GameplayConstants.SQUARE_LENGTH())
                p.changeWidth(-DELTA);
        }
    }

    private static void updateSentryRendering() {
        sentryRenderCounter++;

        if (sentryRenderCounter >= SENTRY_RENDER_CYCLE) {
            sentryRenderCounter = 0;

            Editor.sentriesMap.get(startingPlatform).toggleRenderSentryIndex();

            for (Platform p : additionalPlatforms)
                Editor.sentriesMap.get(p).toggleRenderSentryIndex();
        }
    }

    public static void render(
            final Graphics g, final JBJGLGameDebugger debugger
    ) {
        // background
        g.drawImage(ImageAssets.BACKGROUND(), 0, 0, null);

        // mesh
        renderMesh(g);

        // platforms
        renderPlatforms(g, debugger);

        // sentries
        renderSentries(g);

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

    private static void renderPlatforms(
            final Graphics g, final JBJGLGameDebugger debugger
    ) {
        startingPlatform.renderForEditor(camera, g, debugger);

        for (Platform p : additionalPlatforms)
            p.renderForEditor(camera, g, debugger);
    }

    private static void renderSentries(
            final Graphics g
    ) {
        final List<Platform> platforms = getAllPlatforms();

        for (Platform p : platforms) {
            final EditorPlatformSentries sentries = sentriesMap.get(p);

            if (!sentries.isNotEmpty()) continue;

            final boolean isSelected = sentries.isSelected();

            final EditorPlatformSentries.EditorSentrySpec sentrySpec = isSelected
                    ? sentries.getSelectedSentry()
                    : sentries.getRenderSentry();
            final Sentry sentry = Sentry.create(
                    sentrySpec.getRole(), sentrySpec.getSecondaryRole(),
                    null, p, sentrySpec.getDirection() * sentrySpec.getSpeed()
            );

            sentry.renderSquare(camera, g);
        }
    }

    public static void process(
            final JBJGLListener listener
    ) {
        processCamera(listener);
        processSelection(listener);
        processPlatform(listener);
        processSentry(listener);
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
        if (canSnapToGrid())
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

        // move platform
        if (canMovePlatform()) {
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.JUMP),
                    JBJGLKeyEvent.Action.PRESS,
                    () -> Editor.movePlatform(true, -1, true)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.JUMP),
                    JBJGLKeyEvent.Action.RELEASE,
                    () -> Editor.movePlatform(true, -1, false)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.DROP),
                    JBJGLKeyEvent.Action.PRESS,
                    () -> Editor.movePlatform(true, 1, true)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.DROP),
                    JBJGLKeyEvent.Action.RELEASE,
                    () -> Editor.movePlatform(true, 1, false)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_LEFT),
                    JBJGLKeyEvent.Action.PRESS,
                    () -> Editor.movePlatform(false, -1, true)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_LEFT),
                    JBJGLKeyEvent.Action.RELEASE,
                    () -> Editor.movePlatform(false, -1, false)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_RIGHT),
                    JBJGLKeyEvent.Action.PRESS,
                    () -> Editor.movePlatform(false, 1, true)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_RIGHT),
                    JBJGLKeyEvent.Action.RELEASE,
                    () -> Editor.movePlatform(false, 1, false)
            );
        }

        if (canExpandContractPlatform()) {
            // expand platform
            if (canExpandPlatform()) {
                listener.checkForMatchingKeyStroke(
                        ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_RIGHT),
                        JBJGLKeyEvent.Action.PRESS,
                        () -> Editor.expandContractPlatform(true, true)
                );
            }

            // contract platform
            if (canContractPlatform()) {
                listener.checkForMatchingKeyStroke(
                        ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_LEFT),
                        JBJGLKeyEvent.Action.PRESS,
                        () -> Editor.expandContractPlatform(false, true)
                );
            }

            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_RIGHT),
                    JBJGLKeyEvent.Action.RELEASE,
                    () -> Editor.expandContractPlatform(true, false)
            );
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getCorrespondingKey(ControlScheme.Action.MOVE_LEFT),
                    JBJGLKeyEvent.Action.RELEASE,
                    () -> Editor.expandContractPlatform(false, false)
            );
        }

        // toggle mode
        if (canToggleMode()) {
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.TOGGLE_FOLLOW_MODE),
                    Editor::toggleMode
            );
        }
    }

    private static void processSentry(
            final JBJGLListener listener
    ) {
        // create sentry
        if (canCreateSentry())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.SAVE_POS),
                    () -> getSelectedPlatformSentries().createSentry()
            );

        // delete sentry
        if (canDeleteSentry())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.LOAD_POS),
                    () -> getSelectedPlatformSentries().deleteSentry()
            );

        // toggle sentry
        if (canToggleSentries())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.JUMP),
                    () -> getSelectedPlatformSentries().toggleSentryIndex()
            );

        // decrease sentry speed
        if (canDecreaseSentrySpeed())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_LEFT),
                    () -> getSelectedPlatformSentries().
                            getSelectedSentry().speedDown()
            );

        // increase sentry speed
        if (canIncreaseSentrySpeed())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.MOVE_RIGHT),
                    () -> getSelectedPlatformSentries().
                            getSelectedSentry().speedUp()
            );

        // toggle sentry role
        if (canToggleSentryRole())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.DROP),
                    () -> getSelectedPlatformSentries().getSelectedSentry().
                            nextRole(false)
            );

        // toggle sentry (spawner) secondary role
        if (canToggleSentrySpawnerSecondaryRole())
            listener.checkForMatchingKeyStroke(
                    ControlScheme.getKeyEvent(ControlScheme.Action.SNAP_TO_GRID),
                    () -> getSelectedPlatformSentries().getSelectedSentry().nextRole(true)
            );
    }

    // LEVEL SPEC DEFINITIONS
    public static PlatformSpec[] definePlatformSpecsForLevel() {
        final List<Platform> allPlatforms = getAllPlatforms();

        final PlatformSpec[] platformSpecs = new PlatformSpec[allPlatforms.size()];

        for (int i = 0; i < platformSpecs.length; i++) {
            final Platform p = allPlatforms.get(i);
            platformSpecs[i] = PlatformSpec.define(
                    p.getPosition()[RenderConstants.X],
                    p.getPosition()[RenderConstants.Y], p.getWidth());
        }

        return platformSpecs;
    }

    public static SentrySpec[] defineSentrySpecsForLevel() {
        final List<Platform> allPlatforms = getAllPlatforms();

        final List<SentrySpec> sentrySpecList = new ArrayList<>();

        for (int i = 0; i < allPlatforms.size(); i++) {
            final Platform p = allPlatforms.get(i);
            final EditorPlatformSentries sentriesForPlatform = sentriesMap.get(p);

            for (int j = 0; j < sentriesForPlatform.getSize(); j++)
                sentrySpecList.add(sentriesForPlatform.get(j).toSentrySpec(i));
        }

        final SentrySpec[] sentrySpecs = new SentrySpec[sentrySpecList.size()];
        sentrySpecList.toArray(sentrySpecs);

        return sentrySpecs;
    }

    // DEFAULTS AND RESETS
    private static Platform generateStartingPlatform() {
        return Platform.create(0, 0, DEFAULT_PLATFORM_WIDTH);
    }

    private static List<Platform> defaultAdditionalPlatforms() {
        return new ArrayList<>(List.of(Platform.create(
                        DEFAULT_SECOND_PLATFORM_POSITION[RenderConstants.X],
                        DEFAULT_SECOND_PLATFORM_POSITION[RenderConstants.Y],
                        DEFAULT_PLATFORM_WIDTH)));
    }

    private static Map<Platform, EditorPlatformSentries> generateSentriesMap() {
        final Map<Platform, EditorPlatformSentries> map = new HashMap<>();

        map.put(startingPlatform, EditorPlatformSentries.create());

        for (Platform p : additionalPlatforms)
            map.put(p, EditorPlatformSentries.create());

        return map;
    }

    public static void reset() {
        mode = Mode.MOVE_PLATFORM;

        resetPlatformMovementVariables();
        resetPlatformSizingVariables();

        startingPlatform = generateStartingPlatform();
        additionalPlatforms = defaultAdditionalPlatforms();
        sentriesMap = generateSentriesMap();

        camera = Camera.createForEditor();

        selectedPlatform = null;
        highlightedPlatform = null;
        selectionText = determineSelectionText();

        sentryRenderCounter = 0;
    }

    private static void resetPlatformMovementVariables() {
        platformIsMovingLeft = false;
        platformIsMovingRight = false;
        platformIsMovingUp = false;
        platformIsMovingDown = false;
    }

    private static void resetPlatformSizingVariables() {
        platformIsExpanding = false;
        platformIsContracting = false;
    }

    // BEHAVIOURS
    private static void toggleMode() {
        mode = mode.next();
        EditorHUD.initializeModeHUD();

        if (platformIsSelected()) {
            if (modeIsSentry())
                getSelectedPlatformSentries().select();
            else
                getSelectedPlatformSentries().deselect();
        }

    }

    private static Platform determineHighlightedPlatform() {
        /* 1 - populate ALL platforms and sentries into an entity collection
         * 2 - check whether cursor position overlaps with the bounds of any entities
         * 3 - set selectedEntity to best match, or null if none found */

        final List<Platform> allPlatforms = getAllPlatforms();

        int[] cp = getCursorPosition();

        for (Platform p : allPlatforms)
            if (p.isHighlighted(cp))
                return p;

        return null;
    }

    private static void select() {
        resetPlatformMovementVariables();
        resetPlatformSizingVariables();

        if (platformIsSelected() && highlightedPlatform == null && modeIsSentry())
            getSelectedPlatformSentries().deselect();
        else if (!platformIsSelected() && highlightedPlatform != null && modeIsSentry())
            sentriesMap.get(highlightedPlatform).select();

        selectedPlatform = highlightedPlatform;
    }

    private static void addPlatform() {
        final int[] p = getCursorPosition();

        Platform newPlatform = Platform.create(
                p[RenderConstants.X], p[RenderConstants.Y],
                DEFAULT_PLATFORM_WIDTH);

        additionalPlatforms.add(newPlatform);
        sentriesMap.put(newPlatform, EditorPlatformSentries.create());
    }

    private static void deletePlatform() {
        additionalPlatforms.remove(selectedPlatform);
        sentriesMap.remove(selectedPlatform);
        selectedPlatform = null;
    }

    private static void movePlatform(
            final boolean vertical, final int direction,
            final boolean isPressed
    ) {
        if (vertical) {
            if (direction < 0)
                platformIsMovingUp = isPressed;
            else platformIsMovingDown = isPressed;
        } else {
            if (direction < 0)
                platformIsMovingLeft = isPressed;
            else platformIsMovingRight = isPressed;
        }
    }

    private static void expandContractPlatform(
            final boolean expand, final boolean isPressed
    ) {
        if (expand)
            platformIsExpanding = isPressed;
        else
            platformIsContracting = isPressed;
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

        if (selectedPlatform != null)
            name = determineNameOfEntity(selectedPlatform).toUpperCase();
        else if (highlightedPlatform != null)
            name = ControlScheme.getCorrespondingKey(
                    ControlScheme.Action.INIT_TELEPORT
            ).print() + " to select - " +
                    determineNameOfEntity(highlightedPlatform).toUpperCase();

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

    public static EditorPlatformSentries getSelectedPlatformSentries() {
        return sentriesMap.get(selectedPlatform);
    }

    private static List<Platform> getAllPlatforms() {
        List<Platform> allPlatforms = new ArrayList<>();
        allPlatforms.add(startingPlatform);
        allPlatforms.addAll(additionalPlatforms);

        return allPlatforms;
    }

    // GETTER / SETTER
    public static int[] getCursorPosition() {
        return camera.getPosition();
    }

    public static Platform getStartingPlatform() {
        return startingPlatform;
    }

    public static Mode getMode() {
        return mode;
    }

    public static Platform getSelectedPlatform() {
        return selectedPlatform;
    }

    public static Entity getHighlightedPlatform() {
        return highlightedPlatform;
    }

    public static String getSelectionText() {
        return selectionText;
    }

    // CONTROL PROMPT CHECKERS
    public static boolean canSnapToGrid() {
        return !canToggleSentrySpawnerSecondaryRole();
    }

    public static boolean canCreatePlatform() {
        final boolean isNotSelectingPlatformOnSentryMode =
                !(platformIsSelected() && modeIsSentry());

        return highlightedPlatform == null &&
                isNotSelectingPlatformOnSentryMode;
    }

    public static boolean canDeletePlatform() {
        if (platformIsSelected()) {
            final boolean isNotStartingPlatform = !selectedPlatform.equals(startingPlatform),
                    hasMultipleAdditionalPlatforms = additionalPlatforms.size() > 1;

            return isNotStartingPlatform && hasMultipleAdditionalPlatforms && !modeIsSentry();
        }

        return false;
    }

    public static boolean canMovePlatform() {
        final boolean modeIsMovePlatform = mode == Mode.MOVE_PLATFORM;

        return platformIsSelected() && modeIsMovePlatform;
    }

    public static boolean canExpandPlatform() {
        final boolean modeIsExpandContractPlatform = mode == Mode.RESIZE_PLATFORM;

        return platformIsSelected() &&
                selectedPlatform.getWidth() < Sentry.MAX_PLATFORM_WIDTH &&
                modeIsExpandContractPlatform;
    }

    public static boolean canContractPlatform() {
        final boolean modeIsExpandContractPlatform = mode == Mode.RESIZE_PLATFORM;

        return platformIsSelected() &&
                selectedPlatform.getWidth() > GameplayConstants.SQUARE_LENGTH() &&
                modeIsExpandContractPlatform;
    }

    private static boolean canExpandContractPlatform() {
        return canContractPlatform() || canExpandPlatform();
    }

    public static boolean canCreateSentry() {
        return platformIsSelected() && modeIsSentry();
    }

    public static boolean canDeleteSentry() {
        return platformIsSelected() && modeIsSentry() &&
                getSelectedPlatformSentries().isNotEmpty();
    }

    public static boolean canToggleSentries() {
        return platformIsSelected() && modeIsSentry() &&
                getSelectedPlatformSentries().hasMultiple();
    }

    public static boolean canIncreaseSentrySpeed() {
        if (platformIsSelected() && modeIsSentry() &&
                getSelectedPlatformSentries().isNotEmpty()) {
            EditorPlatformSentries.EditorSentrySpec sentry = getSelectedPlatformSentries().getSelectedSentry();

            final boolean sentryFacingLeft = sentry.getDirection() == Sentry.LEFT,
                    sentryBelowMaxSpeed = sentry.getSpeed() < Sentry.MAX_SENTRY_SPEED;

            return sentryFacingLeft || sentryBelowMaxSpeed;
        }

        return false;
    }

    public static boolean canDecreaseSentrySpeed() {
        if (platformIsSelected() && modeIsSentry() &&
                getSelectedPlatformSentries().isNotEmpty()) {
            EditorPlatformSentries.EditorSentrySpec sentry =
                    getSelectedPlatformSentries().getSelectedSentry();

            final boolean sentryFacingRight = sentry.getDirection() == Sentry.RIGHT,
                    sentryBelowMaxSpeed = sentry.getSpeed() < Sentry.MAX_SENTRY_SPEED;

            return sentryFacingRight || sentryBelowMaxSpeed;
        }

        return false;
    }

    public static boolean canToggleSentryRole() {
        return platformIsSelected() && modeIsSentry() &&
                getSelectedPlatformSentries().isNotEmpty();
    }

    public static boolean canToggleSentrySpawnerSecondaryRole() {
        if (platformIsSelected() && modeIsSentry() &&
                getSelectedPlatformSentries().isNotEmpty()) {
            EditorPlatformSentries.EditorSentrySpec sentry =
                    getSelectedPlatformSentries().getSelectedSentry();

            return sentry.getRole() == Sentry.Role.SPAWNER;
        }

        return false;
    }

    public static boolean canToggleMode() {
        return platformIsSelected();
    }

    public static boolean platformIsSelected() {
        return selectedPlatform != null;
    }

    private static boolean modeIsSentry() {
        return mode == Mode.SENTRY;
    }
}

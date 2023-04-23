package com.jordanbunke.translation.menus.custom_elements;

import com.jordanbunke.jbjgl.contexts.JBJGLMenuManager;
import com.jordanbunke.jbjgl.debug.JBJGLGameDebugger;
import com.jordanbunke.jbjgl.error.JBJGLError;
import com.jordanbunke.jbjgl.events.JBJGLEvent;
import com.jordanbunke.jbjgl.events.JBJGLMouseEvent;
import com.jordanbunke.jbjgl.image.JBJGLImage;
import com.jordanbunke.jbjgl.io.JBJGLListener;
import com.jordanbunke.jbjgl.menus.menu_elements.JBJGLMenuElement;
import com.jordanbunke.jbjgl.utility.RenderConstants;
import com.jordanbunke.translation.colors.TLColors;
import com.jordanbunke.translation.menus.MenuHelper;
import com.jordanbunke.translation.settings.GameplayConstants;
import com.jordanbunke.translation.settings.TechnicalSettings;

import java.awt.*;
import java.util.List;

public class VerticalScrollableMenuElement extends JBJGLMenuElement {
    private static final int SCROLL_BAR_WIDTH = GameplayConstants.SQUARE_LENGTH(),
            CONTENT_TO_BAR_GAP = TechnicalSettings.getPixelSize() * 2,
            MIN_SCROLL_BAR_HEIGHT = TechnicalSettings.getPixelSize() * 5,
            NOT_PINCHED = -1;

    private final ScrollingButtonMenuElement[] contents;

    private final int realBottomY, scrollBarHeight, scrollBarOffsetX;
    private int scrollBarOffsetY, pinchedScrollBarAtLocalY;

    private final boolean canScroll;
    private boolean highlighted, scrolling;

    private VerticalScrollableMenuElement(
            final int[] position, final int[] dimensions,
            final ScrollingButtonMenuElement[] contents,
            final int realBottomY
    ) {
        super(position, dimensions, Anchor.CENTRAL_TOP, true);

        this.contents = contents;
        this.realBottomY = realBottomY;

        this.canScroll = realBottomY > position[RenderConstants.Y] + dimensions[RenderConstants.HEIGHT];
        this.scrollBarHeight = calculateScrollBarHeight();
        this.scrollBarOffsetX = getWidth() - SCROLL_BAR_WIDTH;

        this.scrollBarOffsetY = 0;
        this.pinchedScrollBarAtLocalY = NOT_PINCHED;
        this.highlighted = false;
        this.scrolling = false;
    }

    public static VerticalScrollableMenuElement generate(
            final String[] headings, final Runnable[] behaviours,
            final int x, final int y, final int width, final int height
    ) {
        if (headings.length != behaviours.length)
            JBJGLError.send("Number of button headings for scroll element does not match number of button click behaviours",
                    () -> {}, true, true);

        final int length = headings.length;
        final ScrollingButtonMenuElement[] contents = new ScrollingButtonMenuElement[length];

        for (int i = 0; i < length; i++) {
            contents[i] = ScrollingButtonMenuElement.generate(headings[i],
                    behaviours[i], width - getRightMargin(),
                    new int[] { getButtonXPosition(x), getButtonYPosition(y, i) });
        }

        final int realBottomY = contents[length - 1].getY() + contents[length - 1].getHeight() + CONTENT_TO_BAR_GAP;

        return new VerticalScrollableMenuElement(new int[] { x, y }, new int[] { width, height }, contents, realBottomY);
    }

    @Override
    public void update() {
        // update contents
        for (ScrollingButtonMenuElement button : contents)
            button.update();
    }

    @Override
    public void render(final Graphics g, final JBJGLGameDebugger debugger) {
        // render scroll bar
        draw(drawScrollBar(), g);

        // render contents
        for (ScrollingButtonMenuElement button : contents)
            if (buttonInBounds(button))
                button.render(g, debugger);

        renderBoundingBox(g, debugger);
    }

    @Override
    public void process(final JBJGLListener listener, final JBJGLMenuManager menuManager) {
        // process scroll bar
        processScrollBar(listener);

        // process contents
        for (ScrollingButtonMenuElement button : contents)
            if (buttonInBounds(button))
                button.process(listener, menuManager);
    }

    private boolean mouseIsOverScrollBar(int[] mousePosition) {
        final int[] rp = getRenderPosition(new int[] { 0, 0 }),
                min = new int[] {
                        rp[RenderConstants.X] + scrollBarOffsetX,
                        rp[RenderConstants.Y] + scrollBarOffsetY
                }, max = new int[] {
                        min[RenderConstants.X] + SCROLL_BAR_WIDTH,
                        min[RenderConstants.Y] + scrollBarHeight
                };

        return mousePosition[RenderConstants.X] >= min[RenderConstants.X] &&
                mousePosition[RenderConstants.X] < max[RenderConstants.X] &&
                mousePosition[RenderConstants.Y] >= min[RenderConstants.Y] &&
                mousePosition[RenderConstants.Y] < max[RenderConstants.Y];
    }

    private void setPinchedScrollBarAtLocalY(final int mouseY) {
        final int scrollBarY = getRenderPosition(new int[] { 0, 0 })[RenderConstants.Y] + scrollBarOffsetY;

        pinchedScrollBarAtLocalY = mouseY - scrollBarY;
    }

    private void setScrollBarOffsetY(final int mouseY) {
        final int scrollBarY = mouseY - pinchedScrollBarAtLocalY;
        final int calculatedScrollBarOffsetY = scrollBarY -
                getRenderPosition(new int[] { 0, 0})[RenderConstants.Y];

        scrollBarOffsetY = Math.max(Math.min(calculatedScrollBarOffsetY,
                getHeight() - scrollBarHeight), 0);
    }

    private void processScrollBar(final JBJGLListener listener) {
        highlighted = scrolling || (canScroll && mouseIsOverScrollBar(listener.getMousePosition()));

        // mouse move for scrolling check
        if (scrolling) {
            // set scrollBarOffsetY
            setScrollBarOffsetY(listener.getMousePosition()[RenderConstants.Y]);

            // calculate contentOffsetY
            final int contentOffsetY = calculateContentOffsetY();

            // offset contents
            for (ScrollingButtonMenuElement button : contents)
                button.offsetYBy(contentOffsetY);
        }

        final List<JBJGLEvent> unprocessed = listener.getUnprocessedEvents();
        for (JBJGLEvent e : unprocessed) {
            if (e instanceof JBJGLMouseEvent mouseEvent) {
                // mouse down event
                if (mouseEvent.matchesAction(JBJGLMouseEvent.Action.DOWN) && highlighted && !scrolling) {
                    mouseEvent.markAsProcessed();

                    setPinchedScrollBarAtLocalY(listener.getMousePosition()[RenderConstants.Y]);
                    scrolling = true;
                }
                // mouse up event
                else if (mouseEvent.matchesAction(JBJGLMouseEvent.Action.UP)) {
                    mouseEvent.markAsProcessed();

                    pinchedScrollBarAtLocalY = NOT_PINCHED;
                    scrolling = false;
                }
            }
        }
    }

    private boolean buttonInBounds(final ScrollingButtonMenuElement button) {
        final boolean bottomInBounds = button.getY() + button.getHeight() <= getY() + getHeight();
        final boolean topInBounds = button.getY() >= getY();

        return bottomInBounds && topInBounds;
    }

    private int calculateScrollBarHeight() {
        if (!canScroll) return getHeight();

        final int calculatedHeight = (int)(getHeight() * (getHeight() / (double) (realBottomY - getY())));

        return Math.max(calculatedHeight, MIN_SCROLL_BAR_HEIGHT);
    }

    private int calculateContentOffsetY() {
        final double scrollPercentage = scrollBarOffsetY / (double) getHeight();

        return -1 * (int)(scrollPercentage * (realBottomY - getY()));
    }

    private JBJGLImage drawScrollBar() {
        final Color border = canScroll
                ? (highlighted
                    ? TLColors.BLACK()
                    : TLColors.PLAYER())
                : TLColors.PLATFORM();

        final Color filled = highlighted
                ? TLColors.PLAYER()
                : TLColors.PLAYER(0);

        final int margin = (2 * TechnicalSettings.getPixelSize());

        final JBJGLImage renderImage = JBJGLImage.create(getWidth(), getHeight());
        final Graphics g = renderImage.getGraphics();

        g.setColor(filled);

        if (scrolling)
            g.fillRect(scrollBarOffsetX, scrollBarOffsetY, SCROLL_BAR_WIDTH, scrollBarHeight);
        else
            g.fillRect(scrollBarOffsetX + margin, scrollBarOffsetY + margin,
                    SCROLL_BAR_WIDTH - (2 * margin), scrollBarHeight - (2 * margin));

        g.setColor(border);
        g.fillRect(scrollBarOffsetX, scrollBarOffsetY, SCROLL_BAR_WIDTH, TechnicalSettings.getPixelSize());
        g.fillRect(scrollBarOffsetX, (scrollBarOffsetY + scrollBarHeight) -
                TechnicalSettings.getPixelSize(), SCROLL_BAR_WIDTH, TechnicalSettings.getPixelSize());
        g.fillRect(scrollBarOffsetX, scrollBarOffsetY, TechnicalSettings.getPixelSize(), scrollBarHeight);
        g.fillRect((scrollBarOffsetX + SCROLL_BAR_WIDTH) - TechnicalSettings.getPixelSize(),
                scrollBarOffsetY, TechnicalSettings.getPixelSize(), scrollBarHeight);

        g.dispose();

        return renderImage;
    }

    // STATIC HELPER

    private static int getRightMargin() {
        return SCROLL_BAR_WIDTH + CONTENT_TO_BAR_GAP;
    }

    private static int getButtonXPosition(final int scrollingX) {
        final int rightMargin = VerticalScrollableMenuElement.getRightMargin();
        return scrollingX - ((rightMargin) / 2);
    }

    private static int getButtonYPosition(final int scrollingY, final int index) {
        return scrollingY + CONTENT_TO_BAR_GAP + (index * MenuHelper.listMenuIncrementY());
    }
}

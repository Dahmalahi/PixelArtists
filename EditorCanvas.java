import javax.microedition.lcdui.*;

public class EditorCanvas extends Canvas implements CommandListener {
    private PixelArtists midlet;
    private Sprite sprite;
    private Palette palette;

    private int zoomLevel = 4;
    private int maxZoom = 64;
    private int minZoom = 1;
    private int offsetX = 0;
    private int offsetY = 0;

    private int cursorX = 0;
    private int cursorY = 0;

    private int currentTool = TOOL_PENCIL;
    private int currentColorIndex = 2;
    private int brushSize = 1;
    private boolean isDrawing = false;
    private int lastDrawX = -1;
    private int lastDrawY = -1;
    private long lastDrawTime = 0;

    public static final int TOOL_PENCIL = 0;
    public static final int TOOL_ERASER = 1;
    public static final int TOOL_FILL = 2;
    public static final int TOOL_LINE = 3;
    public static final int TOOL_RECT = 4;
    public static final int TOOL_EYEDROP = 5;
    public static final int TOOL_SELECT = 6;
    public static final int TOOL_SPRAY = 7;
    public static final int TOOL_CIRCLE = 8;

    private static final String[] TOOL_NAMES = {
        "Pencil", "Eraser", "Fill", "Line", "Rect", "Pick", "Select", "Spray", "Circle"
    };

    private int screenW, screenH;
    private int toolbarH = 16;
    private int timelineH = 40;
    private int canvasAreaY;
    private int canvasAreaH;

    private Image offscreen;
    private Graphics offscreenG;

    private boolean showGrid = true;
    private boolean showOnionSkin = false;
    private boolean showTimeline = true;
    private boolean symmetryH = false;
    private boolean symmetryV = false;

    private int lineStartX = -1;
    private int lineStartY = -1;
    private boolean lineStarted = false;
    private int rectStartX = -1;
    private int rectStartY = -1;
    private boolean rectStarted = false;

    private UndoManager undoManager = new UndoManager(10);

    public EditorCanvas(PixelArtists midlet, Sprite sprite) {
        this.midlet = midlet;
        this.sprite = sprite;
        this.palette = sprite.getPalette();
        setFullScreenMode(true);
        screenW = getWidth();
        screenH = getHeight();
        canvasAreaY = toolbarH;
        canvasAreaH = showTimeline ? (screenH - toolbarH - timelineH) : (screenH - toolbarH);
        cursorX = sprite.getWidth() / 2;
        cursorY = sprite.getHeight() / 2;
        try {
            offscreen = Image.createImage(screenW, screenH);
            offscreenG = offscreen.getGraphics();
        } catch (Exception e) {}
    }

    protected void paint(Graphics g) {
        Graphics targetG = (offscreen != null) ? offscreenG : g;
        targetG.setColor(0x1A1A2E);
        targetG.fillRect(0, 0, screenW, screenH);
        drawToolbar(targetG);
        drawCanvas(targetG);
        if (showTimeline) {
            drawTimeline(targetG);
        }
        if (offscreen != null) {
            g.drawImage(offscreen, 0, 0, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void drawToolbar(Graphics g) {
        g.setColor(0x0F1419);
        g.fillRect(0, 0, screenW, toolbarH);
        g.setColor(0xFFFFFF);
        String toolText = TOOL_NAMES[currentTool];
        if (isDrawing) toolText = "[" + toolText + "]";
        g.drawString(toolText, 2, 1, Graphics.TOP | Graphics.LEFT);
        String zoomText = "x" + zoomLevel;
        g.drawString(zoomText, screenW / 2 - 8, 1, Graphics.TOP | Graphics.LEFT);
        int currentColor = palette.getColor(currentColorIndex);
        g.setColor(currentColor);
        g.fillRect(screenW - 18, 2, 12, 12);
        g.setColor(0xFFFFFF);
        g.drawRect(screenW - 18, 2, 12, 12);
        g.setColor(0xAAAAAA);
        String frameText = "F" + (sprite.getCurrentFrameIndex() + 1);
        g.drawString(frameText, screenW - 35, 1, Graphics.TOP | Graphics.LEFT);

        Frame frame = sprite.getCurrentFrame();
        if (frame != null) {
            String layerText = "L" + frame.getLayerCount();
            g.drawString(layerText, screenW - 50, 1, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void drawCanvas(Graphics g) {
        int canvasW = sprite.getWidth();
        int canvasH = sprite.getHeight();
        g.setColor(0x2B2B2B);
        g.fillRect(0, canvasAreaY, screenW, canvasAreaH);

        if (showOnionSkin) {
            drawOnionSkin(g, canvasW, canvasH);
        }

        int[] pixels = sprite.compositeCurrentFrame();
        if (pixels == null) return;

        int startX = Math.max(0, offsetX);
        int startY = Math.max(0, offsetY);
        int visibleW = Math.min(canvasW, offsetX + (screenW / zoomLevel) + 1);
        int visibleH = Math.min(canvasH, offsetY + (canvasAreaH / zoomLevel) + 1);

        for (int py = startY; py < visibleH; py++) {
            for (int px = startX; px < visibleW; px++) {
                int idx = py * canvasW + px;
                if (idx >= 0 && idx < pixels.length) {
                    int color = pixels[idx];
                    if (color != 0) {
                        g.setColor(color);
                        int sx = (px - offsetX) * zoomLevel;
                        int sy = canvasAreaY + (py - offsetY) * zoomLevel;
                        g.fillRect(sx, sy, zoomLevel, zoomLevel);
                    }
                }
            }
        }

        if (showGrid && zoomLevel >= 4) {
            g.setColor(0x555555);
            for (int py = startY; py <= visibleH; py++) {
                int sy = canvasAreaY + (py - offsetY) * zoomLevel;
                g.drawLine(0, sy, screenW, sy);
            }
            for (int px = startX; px <= visibleW; px++) {
                int sx = (px - offsetX) * zoomLevel;
                g.drawLine(sx, canvasAreaY, sx, canvasAreaY + canvasAreaH);
            }
        }

        int cursorSX = (cursorX - offsetX) * zoomLevel;
        int cursorSY = canvasAreaY + (cursorY - offsetY) * zoomLevel;
        g.setColor(0xFFFFFF);
        g.drawRect(cursorSX, cursorSY, zoomLevel - 1, zoomLevel - 1);
        g.setColor(0x000000);
        g.drawRect(cursorSX + 1, cursorSY + 1, zoomLevel - 3, zoomLevel - 3);

        if (lineStarted && currentTool == TOOL_LINE) {
            g.setColor(palette.getColor(currentColorIndex));
            drawLinePreview(g, lineStartX, lineStartY, cursorX, cursorY);
        }

        if (rectStarted && currentTool == TOOL_RECT) {
            g.setColor(palette.getColor(currentColorIndex));
            drawRectPreview(g, rectStartX, rectStartY, cursorX, cursorY);
        }
    }

    private void drawOnionSkin(Graphics g, int canvasW, int canvasH) {
        int prevFrameIdx = sprite.getCurrentFrameIndex() - 1;
        if (prevFrameIdx < 0) return;

        Frame prevFrame = sprite.getFrame(prevFrameIdx);
        if (prevFrame == null) return;

        int[] prevPixels = prevFrame.composite();
        if (prevPixels == null) return;

        g.setColor(0x40FF40);
        for (int py = 0; py < canvasH; py++) {
            for (int px = 0; px < canvasW; px++) {
                int idx = py * canvasW + px;
                if (idx < prevPixels.length && prevPixels[idx] != 0) {
                    int sx = (px - offsetX) * zoomLevel;
                    int sy = canvasAreaY + (py - offsetY) * zoomLevel;
                    if (sx >= 0 && sx < screenW && sy >= canvasAreaY && sy < canvasAreaY + canvasAreaH) {
                        g.fillRect(sx, sy, 1, 1);
                    }
                }
            }
        }
    }

    private void drawTimeline(Graphics g) {
        int timelineY = screenH - timelineH;
        g.setColor(0x0F1419);
        g.fillRect(0, timelineY, screenW, timelineH);
        int frameCount = sprite.getFrameCount();
        int currentFrameIdx = sprite.getCurrentFrameIndex();
        int thumbW = 32;
        int thumbH = 32;
        int thumbSpacing = 4;
        for (int i = 0; i < 10 && i < frameCount; i++) {
            int thumbX = i * (thumbW + thumbSpacing) + 4;
            int thumbY = timelineY + 4;
            if (i == currentFrameIdx) {
                g.setColor(0xE63946);
            } else {
                g.setColor(0x555555);
            }
            g.drawRect(thumbX, thumbY, thumbW, thumbH);
            g.setColor(0xFFFFFF);
            g.drawString("" + (i + 1), thumbX + 2, thumbY + 2, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void drawLinePreview(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            int sxScreen = (x0 - offsetX) * zoomLevel;
            int syScreen = canvasAreaY + (y0 - offsetY) * zoomLevel;
            g.fillRect(sxScreen, syScreen, zoomLevel, zoomLevel);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    private void drawRectPreview(Graphics g, int x0, int y0, int x1, int y1) {
        int minX = Math.min(x0, x1);
        int maxX = Math.max(x0, x1);
        int minY = Math.min(y0, y1);
        int maxY = Math.max(y0, y1);
        for (int x = minX; x <= maxX; x++) {
            int sx = (x - offsetX) * zoomLevel;
            int syTop = canvasAreaY + (minY - offsetY) * zoomLevel;
            int syBot = canvasAreaY + (maxY - offsetY) * zoomLevel;
            g.fillRect(sx, syTop, zoomLevel, zoomLevel);
            g.fillRect(sx, syBot, zoomLevel, zoomLevel);
        }
        for (int y = minY; y <= maxY; y++) {
            int sxLeft = (minX - offsetX) * zoomLevel;
            int sxRight = (maxX - offsetX) * zoomLevel;
            int sy = canvasAreaY + (y - offsetY) * zoomLevel;
            g.fillRect(sxLeft, sy, zoomLevel, zoomLevel);
            g.fillRect(sxRight, sy, zoomLevel, zoomLevel);
        }
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        switch (action) {
            case UP:    moveCursor(0, -1); return;
            case DOWN:  moveCursor(0,  1); return;
            case LEFT:  moveCursor(-1, 0); return;
            case RIGHT: moveCursor( 1, 0); return;
            case FIRE:
                toggleDrawing();
                return;
        }

        switch (keyCode) {
            case Canvas.KEY_NUM2: moveCursor(0, -1); return;
            case Canvas.KEY_NUM8: moveCursor(0,  1); return;
            case Canvas.KEY_NUM4: moveCursor(-1, 0); return;
            case Canvas.KEY_NUM6: moveCursor( 1, 0); return;
            case Canvas.KEY_NUM5: toggleDrawing(); return;
        }

        switch (keyCode) {
            case Canvas.KEY_NUM1: zoomIn(); return;
            case Canvas.KEY_NUM3: zoomOut(); return;
            case Canvas.KEY_NUM7: previousColor(); return;
            case Canvas.KEY_NUM9: nextColor(); return;
            case Canvas.KEY_STAR: previousTool(); return;
            case Canvas.KEY_POUND: nextTool(); return;
            case Canvas.KEY_NUM0: undo(); return;
        }

        if (keyCode == '[') { sprite.previousFrame(); repaint(); return; }
        if (keyCode == ']') { sprite.nextFrame(); repaint(); return; }

        char c = (char) keyCode;
        switch (c) {
            case 'g': case 'G': showGrid = !showGrid; repaint(); return;
            case 'o': case 'O': showOnionSkin = !showOnionSkin; repaint(); return;
            case 't': case 'T': showTimeline = !showTimeline; canvasAreaH = showTimeline ? (screenH - toolbarH - timelineH) : (screenH - toolbarH); repaint(); return;
            case 's': case 'S': symmetryH = !symmetryH; repaint(); return;
            case 'v': case 'V': symmetryV = !symmetryV; repaint(); return;
            case 'f': case 'F': sprite.flipHorizontal(); repaint(); return;
            case 'i': case 'I': sprite.invertColors(); repaint(); return;
            case 'h': case 'H': sprite.grayscale(); repaint(); return;
        }
    }

    private void moveCursor(int dx, int dy) {
        cursorX = clamp(cursorX + dx, 0, sprite.getWidth() - 1);
        cursorY = clamp(cursorY + dy, 0, sprite.getHeight() - 1);
        if (isDrawing && (currentTool == TOOL_PENCIL || currentTool == TOOL_ERASER)) {
            drawContinuous(cursorX, cursorY);
        }
        scrollToCursor();
        repaint();
    }

    private void scrollToCursor() {
        int visW = screenW / zoomLevel;
        int visH = canvasAreaH / zoomLevel;
        if (cursorX < offsetX) offsetX = cursorX;
        if (cursorX >= offsetX + visW) offsetX = cursorX - visW + 1;
        if (cursorY < offsetY) offsetY = cursorY;
        if (cursorY >= offsetY + visH) offsetY = cursorY - visH + 1;
        offsetX = clamp(offsetX, 0, Math.max(0, sprite.getWidth() - visW));
        offsetY = clamp(offsetY, 0, Math.max(0, sprite.getHeight() - visH));
    }

    private void toggleDrawing() {
        if (currentTool == TOOL_PENCIL || currentTool == TOOL_ERASER) {
            undoManager.saveState(sprite);
            isDrawing = !isDrawing;
            if (isDrawing) {
                lastDrawX = cursorX;
                lastDrawY = cursorY;
                int color = (currentTool == TOOL_ERASER) ? 0 : currentColorIndex;
                drawWithSymmetry(cursorX, cursorY, color);
            } else {
                lastDrawX = lastDrawY = -1;
            }
        } else if (currentTool == TOOL_LINE) {
            if (!lineStarted) {
                undoManager.saveState(sprite);
                lineStartX = cursorX;
                lineStartY = cursorY;
                lineStarted = true;
            } else {
                drawLineTool(lineStartX, lineStartY, cursorX, cursorY);
                lineStarted = false;
                lineStartX = lineStartY = -1;
            }
            repaint();
        } else if (currentTool == TOOL_RECT) {
            if (!rectStarted) {
                undoManager.saveState(sprite);
                rectStartX = cursorX;
                rectStartY = cursorY;
                rectStarted = true;
            } else {
                drawRectTool(rectStartX, rectStartY, cursorX, cursorY);
                rectStarted = false;
                rectStartX = rectStartY = -1;
            }
            repaint();
        } else {
            undoManager.saveState(sprite);
            applyTool();
            repaint();
        }
    }

    private void drawContinuous(int x, int y) {
        long now = System.currentTimeMillis();
        if (now - lastDrawTime < 30) return;
        lastDrawTime = now;
        if (lastDrawX == -1 || lastDrawY == -1) {
            lastDrawX = x;
            lastDrawY = y;
            return;
        }
        int color = (currentTool == TOOL_ERASER) ? 0 : currentColorIndex;
        drawLine(lastDrawX, lastDrawY, x, y, color);
        lastDrawX = x;
        lastDrawY = y;
    }

    private void drawWithSymmetry(int x, int y, int color) {
        sprite.setPixel(x, y, color);
        if (symmetryH) {
            sprite.setPixel(sprite.getWidth() - 1 - x, y, color);
        }
        if (symmetryV) {
            sprite.setPixel(x, sprite.getHeight() - 1 - y, color);
        }
        if (symmetryH && symmetryV) {
            sprite.setPixel(sprite.getWidth() - 1 - x, sprite.getHeight() - 1 - y, color);
        }
    }

    private void drawLine(int x0, int y0, int x1, int y1, int color) {
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).drawLine(x0, y0, x1, y1, color);
        }
    }

    private void drawLineTool(int x0, int y0, int x1, int y1) {
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).drawLine(x0, y0, x1, y1, currentColorIndex);
        }
    }

    private void drawRectTool(int x0, int y0, int x1, int y1) {
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).drawRect(x0, y0, Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1, currentColorIndex);
        }
    }

    private void applyTool() {
        switch (currentTool) {
            case TOOL_PENCIL: drawWithSymmetry(cursorX, cursorY, currentColorIndex); break;
            case TOOL_ERASER: drawWithSymmetry(cursorX, cursorY, 0); break;
            case TOOL_FILL: floodFill(cursorX, cursorY, currentColorIndex); break;
            case TOOL_EYEDROP: currentColorIndex = sprite.getPixel(cursorX, cursorY); break;
            case TOOL_SPRAY: spray(cursorX, cursorY, currentColorIndex); break;
            case TOOL_CIRCLE: drawCircleTool(cursorX, cursorY); break;
        }
    }

    private void drawCircleTool(int x, int y) {
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).drawCircle(x, y, brushSize, currentColorIndex);
        }
    }

    private void floodFill(int x, int y, int fillColor) {
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).floodFill(x, y, fillColor);
        }
    }

    private void spray(int x, int y, int color) {
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).spray(x, y, 5, color);
        }
    }

    private void zoomIn() {
        if (zoomLevel < maxZoom) {
            zoomLevel = Math.min(maxZoom, zoomLevel * 2);
            scrollToCursor();
            repaint();
        }
    }

    private void zoomOut() {
        if (zoomLevel > minZoom) {
            zoomLevel = Math.max(minZoom, zoomLevel / 2);
            scrollToCursor();
            repaint();
        }
    }

    private void previousColor() {
        currentColorIndex--;
        if (currentColorIndex < 0) {
            currentColorIndex = palette.getColorCount() - 1;
        }
        repaint();
    }

    private void nextColor() {
        currentColorIndex++;
        if (currentColorIndex >= palette.getColorCount()) {
            currentColorIndex = 0;
        }
        repaint();
    }

    private void previousTool() {
        currentTool--;
        if (currentTool < 0) {
            currentTool = TOOL_NAMES.length - 1;
        }
        isDrawing = false;
        lastDrawX = lastDrawY = -1;
        lineStarted = false;
        rectStarted = false;
        repaint();
    }

    private void nextTool() {
        currentTool++;
        if (currentTool >= TOOL_NAMES.length) {
            currentTool = 0;
        }
        isDrawing = false;
        lastDrawX = lastDrawY = -1;
        lineStarted = false;
        rectStarted = false;
        repaint();
    }

    private void undo() {
        undoManager.undo(sprite);
        repaint();
    }

    private int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    public void commandAction(Command c, Displayable d) {}
}
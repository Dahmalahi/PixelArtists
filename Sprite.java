public class Sprite {
    
    private int width;
    private int height;
    private Frame[] frames;
    private int currentFrameIndex;
    private int maxFrames;
    private Palette palette;
    private String name;
    private long lastModified;
    
    public static final int MAX_WIDTH = 256;
    public static final int MAX_HEIGHT = 256;
    public static final int MAX_FRAMES = 64;
    
    public Sprite(int width, int height) {
        if (width < 8 || width > MAX_WIDTH || height < 8 || height > MAX_HEIGHT) {
            throw new IllegalArgumentException("Invalid: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.maxFrames = MAX_FRAMES;
        this.frames = new Frame[maxFrames];
        this.frames[0] = new Frame(width, height);
        this.currentFrameIndex = 0;
        this.palette = Palette.createDefault32();
        this.name = "Sprite_" + System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }
    
    public int addFrame() {
        for (int i = 0; i < maxFrames; i++) {
            if (frames[i] == null) {
                frames[i] = new Frame(width, height);
                currentFrameIndex = i;
                lastModified = System.currentTimeMillis();
                return i;
            }
        }
        return -1;
    }
    
    public int duplicateCurrentFrame() {
        int newIndex = addFrame();
        if (newIndex >= 0) {
            Frame current = getCurrentFrame();
            Frame newFrame = frames[newIndex];
            for (int i = 0; i < current.getLayerCount(); i++) {
                Layer layer = current.getLayer(i);
                newFrame.addLayer(layer.duplicate());
            }
        }
        return newIndex;
    }
    
    public boolean deleteFrame(int index) {
        if (index < 0 || index >= maxFrames || frames[index] == null) return false;
        int count = getFrameCount();
        if (count <= 1) return false;
        frames[index] = null;
        if (currentFrameIndex == index) {
            for (int i = index - 1; i >= 0; i--) {
                if (frames[i] != null) {
                    currentFrameIndex = i;
                    break;
                }
            }
            if (currentFrameIndex == index) {
                for (int i = index + 1; i < maxFrames; i++) {
                    if (frames[i] != null) {
                        currentFrameIndex = i;
                        break;
                    }
                }
            }
        }
        lastModified = System.currentTimeMillis();
        return true;
    }
    
    public void previousFrame() {
        for (int i = currentFrameIndex - 1; i >= 0; i--) {
            if (frames[i] != null) {
                currentFrameIndex = i;
                return;
            }
        }
        for (int i = maxFrames - 1; i > currentFrameIndex; i--) {
            if (frames[i] != null) {
                currentFrameIndex = i;
                return;
            }
        }
    }
    
    public void nextFrame() {
        for (int i = currentFrameIndex + 1; i < maxFrames; i++) {
            if (frames[i] != null) {
                currentFrameIndex = i;
                return;
            }
        }
        for (int i = 0; i < currentFrameIndex; i++) {
            if (frames[i] != null) {
                currentFrameIndex = i;
                return;
            }
        }
    }
    
    public Frame getCurrentFrame() {
        return frames[currentFrameIndex];
    }
    
    public int getFrameCount() {
        int count = 0;
        for (int i = 0; i < maxFrames; i++) {
            if (frames[i] != null) count++;
        }
        return count;
    }
    
    public int[] compositeCurrentFrame() {
        Frame frame = getCurrentFrame();
        if (frame == null) return null;
        return frame.composite();
    }
    
    public int getPixel(int x, int y) {
        Frame frame = getCurrentFrame();
        if (frame == null) return 0;
        Layer activeLayer = frame.getActiveLayer();
        if (activeLayer == null) return 0;
        return activeLayer.getPixel(x, y);
    }
    
    public void setPixel(int x, int y, int color) {
        Frame frame = getCurrentFrame();
        if (frame == null) return;
        Layer activeLayer = frame.getActiveLayer();
        if (activeLayer == null) return;
        activeLayer.setPixel(x, y, color);
        lastModified = System.currentTimeMillis();
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Palette getPalette() { return palette; }
    public void setPalette(Palette p) { this.palette = p; }
    public int getCurrentFrameIndex() { return currentFrameIndex; }
    
    public void setCurrentFrameIndex(int index) {
        if (index >= 0 && index < maxFrames && frames[index] != null) {
            currentFrameIndex = index;
        }
    }
    
    public Frame getFrame(int index) {
        if (index >= 0 && index < maxFrames) {
            return frames[index];
        }
        return null;
    }
    
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public long getLastModified() { return lastModified; }
    
    public int getMemorySize() {
        int size = 0;
        for (int i = 0; i < maxFrames; i++) {
            if (frames[i] != null) {
                size += frames[i].getMemorySize();
            }
        }
        if (palette != null) {
            size += palette.getMemorySize();
        }
        size += 100;
        return size;
    }
    
    public void flipHorizontal() {
        Frame frame = getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).flipHorizontal();
        }
    }
    
    public void flipVertical() {
        Frame frame = getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).flipVertical();
        }
    }
    
    public void rotate90() {
        Frame frame = getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).rotate90();
        }
    }
    
    public void invertColors() {
        Frame frame = getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).invertColors(palette);
        }
    }
    
    public void grayscale() {
        Frame frame = getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            ((PixelLayer) layer).grayscale(palette);
        }
    }
}
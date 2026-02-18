public class UndoManager {
    
    private byte[][] undoStack;
    private int[] undoWidth;
    private int[] undoHeight;
    private int stackIndex;
    private int maxStates;
    
    public UndoManager(int maxStates) {
        this.maxStates = maxStates;
        this.undoStack = new byte[maxStates][];
        this.undoWidth = new int[maxStates];
        this.undoHeight = new int[maxStates];
        this.stackIndex = -1;
    }
    
    public void saveState(Sprite sprite) {
        stackIndex++;
        if (stackIndex >= maxStates) {
            for (int i = 0; i < maxStates - 1; i++) {
                undoStack[i] = undoStack[i + 1];
                undoWidth[i] = undoWidth[i + 1];
                undoHeight[i] = undoHeight[i + 1];
            }
            stackIndex = maxStates - 1;
        }
        
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer) {
            byte[] pixels = ((PixelLayer) layer).getRawPixels();
            undoStack[stackIndex] = new byte[pixels.length];
            System.arraycopy(pixels, 0, undoStack[stackIndex], 0, pixels.length);
            undoWidth[stackIndex] = sprite.getWidth();
            undoHeight[stackIndex] = sprite.getHeight();
        }
    }
    
    public void undo(Sprite sprite) {
        if (stackIndex < 0) return;
        
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        Layer layer = frame.getActiveLayer();
        if (layer instanceof PixelLayer && undoStack[stackIndex] != null) {
            byte[] pixels = ((PixelLayer) layer).getRawPixels();
            System.arraycopy(undoStack[stackIndex], 0, pixels, 0, pixels.length);
        }
        
        stackIndex--;
    }
    
    public void clear() {
        stackIndex = -1;
        for (int i = 0; i < maxStates; i++) {
            undoStack[i] = null;
        }
    }
}
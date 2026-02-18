import javax.microedition.lcdui.*;

/**
 * LayerFrameMenu - Manages layers and animation frames.
 * FIXED: Proper index handling and menu refresh
 */
public class LayerFrameMenu extends List implements CommandListener {
    
    private PixelArtists midlet;
    private EditorCanvas canvas;
    private Command backCmd = new Command("Back", Command.BACK, 2);
    private Command okCmd = new Command("OK", Command.OK, 1);
    
    private static final int MODE_LAYERS = 0;
    private static final int MODE_FRAMES = 1;
    
    private int currentMode = MODE_LAYERS;
    
    public LayerFrameMenu(PixelArtists midlet, EditorCanvas canvas) {
        super("Layers & Frames", List.IMPLICIT);
        this.midlet = midlet;
        this.canvas = canvas;
        
        rebuildMenu();
        addCommand(backCmd);
        addCommand(okCmd);
        setCommandListener(this);
    }
    
    private void rebuildMenu() {
        deleteAll();
        
        if (currentMode == MODE_LAYERS) {
            append("--- LAYERS ---", null);
            
            Sprite sprite = midlet.getCurrentSprite();
            if (sprite != null) {
                Frame frame = sprite.getCurrentFrame();
                if (frame != null) {
                    for (int i = 0; i < frame.getLayerCount(); i++) {
                        Layer layer = frame.getLayer(i);
                        String layerName = layer.getName();
                        if (!layer.isVisible()) {
                            layerName = "[H] " + layerName;
                        }
                        if (i == frame.getActiveLayerIndex()) {
                            layerName = "* " + layerName;
                        }
                        append(layerName, null);
                    }
                }
            }
            
            append("--- ADD ---", null);
            append("Add Layer", null);
            
            append("--- ACTIONS ---", null);
            append("Delete Layer", null);
            append("Move Up", null);
            append("Move Down", null);
            append("Toggle Visibility", null);
            append("Merge Down", null);
            
            append("--- SWITCH ---", null);
            append(">> Switch to Frames", null);
            
        } else {
            append("--- FRAMES ---", null);
            
            Sprite sprite = midlet.getCurrentSprite();
            if (sprite != null) {
                int frameCount = sprite.getFrameCount();
                int currentIdx = sprite.getCurrentFrameIndex();
                
                for (int i = 0; i < 12 && i < 64; i++) {
                    Frame frame = sprite.getFrame(i);
                    String frameText = "Frame " + (i + 1);
                    if (i == currentIdx) {
                        frameText = "* " + frameText;
                    }
                    if (frame == null) {
                        frameText = "(empty)";
                    }
                    append(frameText, null);
                }
            }
            
            append("--- ADD ---", null);
            append("Add Frame", null);
            append("Duplicate Frame", null);
            
            append("--- ACTIONS ---", null);
            append("Delete Frame", null);
            append("Previous Frame", null);
            append("Next Frame", null);
            
            append("--- SWITCH ---", null);
            append(">> Switch to Layers", null);
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            midlet.getDisplay().setCurrent(canvas);
            canvas.repaint();
            return;
        }
        
        if (c == okCmd || c == List.SELECT_COMMAND) {
            handleSelection(getSelectedIndex());
        }
    }
    
    private void handleSelection(int idx) {
        Sprite sprite = midlet.getCurrentSprite();
        if (sprite == null) return;
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;
        
        if (currentMode == MODE_LAYERS) {
            handleLayerAction(idx, sprite, frame);
        } else {
            handleFrameAction(idx, sprite);
        }
    }
    
    private void handleLayerAction(int idx, Sprite sprite, Frame frame) {
        int layerCount = frame.getLayerCount();
        int baseIdx = 0;
        
        // Section 1: Layer list (indexes 0 to layerCount-1)
        if (idx >= 0 && idx < layerCount) {
            frame.setActiveLayerIndex(idx);
            midlet.showAlert("Layer", "Selected: " + frame.getLayer(idx).getName(), AlertType.INFO);
            midlet.vibrate(50);
            rebuildMenu();
            return;
        }
        
        baseIdx = layerCount;
        
        // Skip "--- ADD ---" header (idx = baseIdx)
        if (idx == baseIdx) return;
        
        // Add Layer (idx = baseIdx + 1)
        if (idx == baseIdx + 1) {
            if (frame.addNewPixelLayer("Layer " + (layerCount + 1))) {
                midlet.showAlert("Layer", "Layer added!", AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Error", "Max 8 layers!", AlertType.ERROR);
            }
            rebuildMenu();
            return;
        }
        
        baseIdx = baseIdx + 2;
        
        // Skip "--- ACTIONS ---" header (idx = baseIdx)
        if (idx == baseIdx) return;
        
        // Delete Layer (idx = baseIdx + 1)
        if (idx == baseIdx + 1) {
            if (frame.deleteLayer(frame.getActiveLayerIndex())) {
                midlet.showAlert("Layer", "Layer deleted!", AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Error", "Cannot delete last layer!", AlertType.ERROR);
            }
            rebuildMenu();
            return;
        }
        
        // Move Up (idx = baseIdx + 2)
        if (idx == baseIdx + 2) {
            if (frame.moveLayerUp(frame.getActiveLayerIndex())) {
                midlet.showAlert("Layer", "Moved up!", AlertType.INFO);
                midlet.vibrate(50);
            } else {
                midlet.showAlert("Info", "Already at top!", AlertType.INFO);
            }
            rebuildMenu();
            return;
        }
        
        // Move Down (idx = baseIdx + 3)
        if (idx == baseIdx + 3) {
            if (frame.moveLayerDown(frame.getActiveLayerIndex())) {
                midlet.showAlert("Layer", "Moved down!", AlertType.INFO);
                midlet.vibrate(50);
            } else {
                midlet.showAlert("Info", "Already at bottom!", AlertType.INFO);
            }
            rebuildMenu();
            return;
        }
        
        // Toggle Visibility (idx = baseIdx + 4)
        if (idx == baseIdx + 4) {
            Layer layer = frame.getActiveLayer();
            if (layer != null) {
                layer.setVisible(!layer.isVisible());
                midlet.showAlert("Layer", "Visibility: " + (layer.isVisible() ? "ON" : "OFF"), AlertType.INFO);
                midlet.vibrate(50);
            }
            rebuildMenu();
            return;
        }
        
        // Merge Down (idx = baseIdx + 5)
        if (idx == baseIdx + 5) {
            if (frame.mergeLayerDown()) {
                midlet.showAlert("Layer", "Merged down!", AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Error", "Cannot merge!", AlertType.ERROR);
            }
            rebuildMenu();
            return;
        }
        
        baseIdx = baseIdx + 6;
        
        // Skip "--- SWITCH ---" header (idx = baseIdx)
        if (idx == baseIdx) return;
        
        // Switch to Frames (idx = baseIdx + 1)
        if (idx == baseIdx + 1) {
            currentMode = MODE_FRAMES;
            rebuildMenu();
            return;
        }
    }
    
    private void handleFrameAction(int idx, Sprite sprite) {
        int baseIdx = 0;
        
        // Section 1: Frame list (indexes 0 to 11)
        if (idx >= 0 && idx < 12) {
            Frame frame = sprite.getFrame(idx);
            if (frame != null) {
                sprite.setCurrentFrameIndex(idx);
                midlet.showAlert("Frame", "Selected Frame " + (idx + 1), AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Info", "Empty frame", AlertType.INFO);
            }
            rebuildMenu();
            return;
        }
        
        baseIdx = 12;
        
        // Skip "--- ADD ---" header (idx = baseIdx)
        if (idx == baseIdx) return;
        
        // Add Frame (idx = baseIdx + 1)
        if (idx == baseIdx + 1) {
            int newIdx = sprite.addFrame();
            if (newIdx >= 0) {
                midlet.showAlert("Frame", "Frame " + (newIdx + 1) + " added!", AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Error", "Max 64 frames!", AlertType.ERROR);
            }
            rebuildMenu();
            return;
        }
        
        // Duplicate Frame (idx = baseIdx + 2)
        if (idx == baseIdx + 2) {
            int newIdx = sprite.duplicateCurrentFrame();
            if (newIdx >= 0) {
                midlet.showAlert("Frame", "Frame duplicated!", AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Error", "Cannot duplicate!", AlertType.ERROR);
            }
            rebuildMenu();
            return;
        }
        
        baseIdx = baseIdx + 3;
        
        // Skip "--- ACTIONS ---" header (idx = baseIdx)
        if (idx == baseIdx) return;
        
        // Delete Frame (idx = baseIdx + 1)
        if (idx == baseIdx + 1) {
            if (sprite.deleteFrame(sprite.getCurrentFrameIndex())) {
                midlet.showAlert("Frame", "Frame deleted!", AlertType.INFO);
                midlet.vibrate(100);
            } else {
                midlet.showAlert("Error", "Cannot delete last frame!", AlertType.ERROR);
            }
            rebuildMenu();
            return;
        }
        
        // Previous Frame (idx = baseIdx + 2)
        if (idx == baseIdx + 2) {
            sprite.previousFrame();
            midlet.showAlert("Frame", "Previous frame", AlertType.INFO);
            midlet.vibrate(50);
            rebuildMenu();
            return;
        }
        
        // Next Frame (idx = baseIdx + 3)
        if (idx == baseIdx + 3) {
            sprite.nextFrame();
            midlet.showAlert("Frame", "Next frame", AlertType.INFO);
            midlet.vibrate(50);
            rebuildMenu();
            return;
        }
        
        baseIdx = baseIdx + 4;
        
        // Skip "--- SWITCH ---" header (idx = baseIdx)
        if (idx == baseIdx) return;
        
        // Switch to Layers (idx = baseIdx + 1)
        if (idx == baseIdx + 1) {
            currentMode = MODE_LAYERS;
            rebuildMenu();
            return;
        }
    }
    
    public static void show(PixelArtists midlet, EditorCanvas canvas) {
        LayerFrameMenu menu = new LayerFrameMenu(midlet, canvas);
        midlet.getDisplay().setCurrent(menu);
    }
}
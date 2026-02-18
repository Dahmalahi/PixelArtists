import javax.microedition.lcdui.*;

/**
 * ExportMenu - Multi-format export selection
 * 
 * Formats:
 * - BMP (uncompressed, large but compatible)
 * - PNG (compressed, small, best quality)
 * - JPG (lossy compression, smallest)
 * - GIF (animated, for animations)
 * - MP4 (image sequence for video editing)
 */
public class ExportMenu extends List implements CommandListener {
    
    private PixelArtists midlet;
    private Command backCmd = new Command("Back", Command.BACK, 1);
    private Command selectCmd = new Command("Export", Command.OK, 1);
    
    public ExportMenu(PixelArtists midlet) {
        super("Export Format", List.IMPLICIT);
        this.midlet = midlet;
        
        Sprite sprite = midlet.getCurrentSprite();
        int frameCount = sprite != null ? sprite.getFrameCount() : 1;
        
        // Single frame exports
        append("BMP - Uncompressed (large)", null);
        append("PNG - Compressed (best quality)", null);
        append("JPG - Lossy (smallest size)", null);
        
        // Animation exports (only if multiple frames)
        if (frameCount > 1) {
            append("--- ANIMATION ---", null);
            append("GIF - Animated GIF (" + frameCount + " frames)", null);
            append("MP4 - Image sequence (for video)", null);
            append("Frame sequence - All frames as PNG", null);
        }
        
        // Options
        append("--- OPTIONS ---", null);
        append("Current frame only", null);
        append("All layers flattened", null);
        if (frameCount > 1) {
            append("All frames individually", null);
        }
        
        addCommand(backCmd);
        addCommand(selectCmd);
        setCommandListener(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            midlet.getDisplay().setCurrent(midlet.getEditorCanvas());
            return;
        }
        
        if (c == selectCmd || c == List.SELECT_COMMAND) {
            int idx = getSelectedIndex();
            String selected = getString(idx);
            
            if (selected.startsWith("BMP")) {
                midlet.exportFormat("BMP");
            } else if (selected.startsWith("PNG")) {
                midlet.exportFormat("PNG");
            } else if (selected.startsWith("JPG")) {
                midlet.exportFormat("JPG");
            } else if (selected.startsWith("GIF")) {
                midlet.exportFormat("GIF");
            } else if (selected.startsWith("MP4")) {
                midlet.exportFormat("MP4");
            } else if (selected.startsWith("Frame sequence")) {
                exportAllFrames();
            } else if (selected.startsWith("Current frame")) {
                midlet.exportFormat("PNG");
            } else if (selected.startsWith("All layers")) {
                flattenAndExport();
            } else if (selected.startsWith("All frames individually")) {
                exportAllFrames();
            }
        }
    }
    
    private void exportAllFrames() {
        Sprite sprite = midlet.getCurrentSprite();
        if (sprite == null) return;
        
        int frameCount = sprite.getFrameCount();
        int savedFrame = sprite.getCurrentFrameIndex();
        
        midlet.showAlert("Exporting", "Exporting " + frameCount + " frames...", AlertType.INFO);
        
        try {
            for (int i = 0; i < frameCount; i++) {
                sprite.setCurrentFrameIndex(i);
                String filename = "frame_" + (i + 1) + "_" + System.currentTimeMillis();
                PNGExporter.export(sprite, filename, midlet);
                Thread.sleep(100);
            }
            
            sprite.setCurrentFrameIndex(savedFrame);
            midlet.showAlert("Success", frameCount + " frames exported as PNG!", AlertType.CONFIRMATION);
            
        } catch (Exception e) {
            midlet.showAlert("Error", "Frame export failed: " + e, AlertType.ERROR);
        }
    }
    
    private void flattenAndExport() {
        Sprite sprite = midlet.getCurrentSprite();
        if (sprite == null) return;
        
        Frame frame = sprite.getCurrentFrame();
        if (frame != null) {
            frame.flattenLayers();
            midlet.showAlert("Info", "Layers flattened. Exporting...", AlertType.INFO);
            midlet.exportFormat("PNG");
        }
    }
}

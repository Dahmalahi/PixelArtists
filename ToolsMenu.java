import javax.microedition.lcdui.*;

/**
 * ToolsMenu - Professional tools, effects and settings menu
 * 
 * Provides access to:
 * - All 8 drawing tools
 * - Image effects (invert, grayscale, flip, rotate)
 * - View settings (grid, onion skin, timeline)
 * - Drawing modes (symmetry)
 */
public class ToolsMenu extends List implements CommandListener {
    
    private PixelArtists midlet;
    private EditorCanvas canvas;
    private Command backCmd = new Command("Back", Command.BACK, 1);
    private Command selectCmd = new Command("Select", Command.OK, 1);
    
    public ToolsMenu(PixelArtists midlet, EditorCanvas canvas) {
        super("Tools & Effects", List.IMPLICIT);
        this.midlet = midlet;
        this.canvas = canvas;
        
        // Tools section
        append("=== TOOLS ===", null);
        append("1. Pencil (smooth)", null);
        append("2. Eraser", null);
        append("3. Fill (flood)", null);
        append("4. Line (two-point)", null);
        append("5. Rectangle", null);
        append("6. Eyedropper", null);
        append("7. Select area", null);
        append("8. Spray paint", null);
        
        // Effects section
        append("=== EFFECTS ===", null);
        append("Invert Colors", null);
        append("Grayscale", null);
        append("Flip Horizontal", null);
        append("Flip Vertical", null);
        append("Rotate 90° CW", null);
        
        // View settings
        append("=== VIEW ===", null);
        append("Toggle Grid", null);
        append("Toggle Onion Skin", null);
        append("Toggle Timeline", null);
        
        // Drawing modes
        append("=== DRAWING ===", null);
        append("Symmetry Horizontal", null);
        append("Symmetry Vertical", null);
        
        addCommand(backCmd);
        addCommand(selectCmd);
        setCommandListener(this);
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            midlet.getDisplay().setCurrent(canvas);
            canvas.repaint();
            return;
        }
        
        if (c == selectCmd || c == List.SELECT_COMMAND) {
            handleSelection(getSelectedIndex());
        }
    }
    
    private void handleSelection(int idx) {
        Sprite sprite = midlet.getCurrentSprite();
        if (sprite == null) return;
        
        // Skip section headers
        if (idx == 0 || idx == 9 || idx == 15 || idx == 19) return;
        
        // Tools (idx 1-8)
        if (idx >= 1 && idx <= 8) {
            // Tool selection would be handled by EditorCanvas
            // For now just show info
            midlet.showAlert("Tool", "Use * and # keys to cycle tools\nCurrent: " + getString(idx), AlertType.INFO);
            midlet.getDisplay().setCurrent(canvas);
            canvas.repaint();
            return;
        }
        
        // Effects (idx 10-14)
        if (idx == 10) { // Invert Colors
            sprite.invertColors();
            midlet.showAlert("Effect", "Colors inverted", AlertType.INFO);
            midlet.vibrate(100);
            canvas.repaint();
        } else if (idx == 11) { // Grayscale
            sprite.grayscale();
            midlet.showAlert("Effect", "Converted to grayscale", AlertType.INFO);
            midlet.vibrate(100);
            canvas.repaint();
        } else if (idx == 12) { // Flip H
            sprite.flipHorizontal();
            midlet.showAlert("Effect", "Flipped horizontally", AlertType.INFO);
            midlet.vibrate(100);
            canvas.repaint();
        } else if (idx == 13) { // Flip V
            sprite.flipVertical();
            midlet.showAlert("Effect", "Flipped vertically", AlertType.INFO);
            midlet.vibrate(100);
            canvas.repaint();
        } else if (idx == 14) { // Rotate
            sprite.rotate90();
            midlet.showAlert("Effect", "Rotated 90° clockwise", AlertType.INFO);
            midlet.vibrate(100);
            canvas.repaint();
        }
        
        // View settings (idx 16-18)
        if (idx == 16) { // Toggle Grid
            midlet.showAlert("View", "Press G to toggle grid\n(QWERTY keyboards)", AlertType.INFO);
        } else if (idx == 17) { // Toggle Onion Skin
            midlet.showAlert("View", "Press O to toggle onion skin\n(QWERTY keyboards)", AlertType.INFO);
        } else if (idx == 18) { // Toggle Timeline
            midlet.showAlert("View", "Press T to toggle timeline\n(QWERTY keyboards)", AlertType.INFO);
        }
        
        // Drawing modes (idx 20-21)
        if (idx == 20) { // Symmetry H
            midlet.showAlert("Drawing", "Press S to toggle horizontal symmetry\n(QWERTY keyboards)", AlertType.INFO);
        } else if (idx == 21) { // Symmetry V
            midlet.showAlert("Drawing", "Press V to toggle vertical symmetry\n(QWERTY keyboards)", AlertType.INFO);
        }
    }
    
    public static void show(PixelArtists midlet, EditorCanvas canvas) {
        ToolsMenu menu = new ToolsMenu(midlet, canvas);
        midlet.getDisplay().setCurrent(menu);
    }
}

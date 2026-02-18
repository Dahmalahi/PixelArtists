import javax.microedition.lcdui.*;

public class LayerMenu extends List implements CommandListener {
    private PixelArtists midlet;
    private EditorCanvas canvas;
    private Command backCmd = new Command("Back", Command.BACK, 1);
    private Command selectCmd = new Command("OK", Command.OK, 1);

    public LayerMenu(PixelArtists midlet, EditorCanvas canvas) {
        super("Layer Management", List.IMPLICIT);
        this.midlet = midlet;
        this.canvas = canvas;
        buildMenu();
        addCommand(backCmd);
        addCommand(selectCmd);
        setCommandListener(this);
    }

    private void buildMenu() {
        deleteAll();
        Sprite sprite = midlet.getCurrentSprite();
        if (sprite == null) return;

        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;

        int layerCount = frame.getLayerCount();
        int activeIdx = frame.getActiveLayerIndex();

        append("=== LAYERS (" + layerCount + "/8) ===", null);
        for (int i = layerCount - 1; i >= 0; i--) {
            Layer layer = frame.getLayer(i);
            if (layer == null) continue;

            String layerName = layer.getName();

            if (i == activeIdx) {
                layerName = "[*] " + layerName + " (editing)";
            } else {
                layerName = "    " + layerName;
            }

            if (!layer.isVisible()) {
                layerName += " [HIDDEN]";
            }

            if (layer.getOpacity() < 100) {
                layerName += " [" + layer.getOpacity() + "%]";
            }

            append(layerName, null);
        }

        append("=== ACTIONS ===", null);
        append("[+] Add New Layer", null);
        append("[X] Delete Active Layer", null);
        append("[V] Toggle Visibility", null);
        append("[O] Set Opacity", null);
        append("[UP] Move Layer Up", null);
        append("[DN] Move Layer Down", null);
        append("[M] Merge Down", null);
        append("[F] Flatten All Layers", null);

        append("=== INFO ===", null);
        append("[I] Layer Info", null);
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
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;

        String selected = getString(idx);

        if (selected.indexOf("Layer") >= 0 && selected.indexOf("Info") < 0 &&
            selected.indexOf("Add") < 0 && selected.indexOf("Delete") < 0 &&
            selected.indexOf("Move") < 0 && selected.indexOf("Merge") < 0 &&
            selected.indexOf("Flatten") < 0 && selected.indexOf("Visibility") < 0 &&
            selected.indexOf("Opacity") < 0) {
            int layerCount = frame.getLayerCount();
            int listIdx = idx - 1;
            if (listIdx >= 0 && listIdx < layerCount) {
                int layerIdx = layerCount - 1 - listIdx;
                frame.setActiveLayerIndex(layerIdx);
                midlet.showAlert("Layer", "Editing: " + frame.getLayer(layerIdx).getName(), AlertType.INFO);
                midlet.vibrate(50);
                buildMenu();
                canvas.repaint();
            }
            return;
        }

        if (selected.indexOf("Add New Layer") >= 0) {
            addLayer();
        } else if (selected.indexOf("Delete") >= 0) {
            deleteLayer();
        } else if (selected.indexOf("Visibility") >= 0) {
            toggleVisibility();
        } else if (selected.indexOf("Opacity") >= 0) {
            setOpacity();
        } else if (selected.indexOf("Move Layer Up") >= 0) {
            moveLayerUp();
        } else if (selected.indexOf("Move Layer Down") >= 0) {
            moveLayerDown();
        } else if (selected.indexOf("Merge Down") >= 0) {
            mergeDown();
        } else if (selected.indexOf("Flatten All") >= 0) {
            flattenAll();
        } else if (selected.indexOf("Layer Info") >= 0) {
            showLayerInfo();
        }
    }

    private void addLayer() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        int layerCount = frame.getLayerCount();
        String layerName = "Layer " + (layerCount + 1);

        if (frame.addNewPixelLayer(layerName)) {
            midlet.showAlert("Success",
                "Layer added!\n" +
                "Name: " + layerName + "\n" +
                "Total layers: " + frame.getLayerCount(),
                AlertType.CONFIRMATION);
            midlet.vibrate(100);
            buildMenu();
            canvas.repaint();
        } else {
            midlet.showAlert("Error", "Maximum 8 layers reached!", AlertType.ERROR);
        }
    }

    private void deleteLayer() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        if (frame.getLayerCount() <= 1) {
            midlet.showAlert("Error", "Cannot delete last layer!", AlertType.ERROR);
            return;
        }

        int activeIdx = frame.getActiveLayerIndex();
        String layerName = frame.getLayer(activeIdx).getName();

        if (frame.deleteLayer(activeIdx)) {
            midlet.showAlert("Success",
                "Layer deleted: " + layerName + "\n" +
                "Remaining layers: " + frame.getLayerCount(),
                AlertType.CONFIRMATION);
            midlet.vibrate(100);
            buildMenu();
            canvas.repaint();
        } else {
            midlet.showAlert("Error", "Cannot delete layer!", AlertType.ERROR);
        }
    }

    private void toggleVisibility() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        Layer layer = frame.getActiveLayer();
        if (layer != null) {
            layer.setVisible(!layer.isVisible());
            String status = layer.isVisible() ? "Visible" : "Hidden";
            midlet.showAlert("Layer", layer.getName() + ": " + status, AlertType.INFO);
            midlet.vibrate(50);
            buildMenu();
            canvas.repaint();
        }
    }

    private void setOpacity() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        Layer layer = frame.getActiveLayer();
        if (layer == null) return;

        final Layer finalLayer = layer;
        final List opacityList = new List("Set Opacity", List.EXCLUSIVE);
        opacityList.append("100% (Opaque)", null);
        opacityList.append("75%", null);
        opacityList.append("50%", null);
        opacityList.append("25%", null);

        int current = layer.getOpacity();
        if (current == 100) opacityList.setSelectedIndex(0, true);
        else if (current == 75) opacityList.setSelectedIndex(1, true);
        else if (current == 50) opacityList.setSelectedIndex(2, true);
        else if (current == 25) opacityList.setSelectedIndex(3, true);

        opacityList.addCommand(new Command("Set", Command.OK, 1));
        opacityList.addCommand(new Command("Cancel", Command.CANCEL, 2));

        opacityList.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    int idx = opacityList.getSelectedIndex();
                    int opacity = 100;
                    switch (idx) {
                        case 0: opacity = 100; break;
                        case 1: opacity = 75; break;
                        case 2: opacity = 50; break;
                        case 3: opacity = 25; break;
                    }
                    finalLayer.setOpacity(opacity);
                    midlet.showAlert("Opacity", "Set to " + opacity + "%", AlertType.INFO);
                    midlet.vibrate(50);
                    canvas.repaint();
                }
                midlet.getDisplay().setCurrent(LayerMenu.this);
                buildMenu();
            }
        });

        midlet.getDisplay().setCurrent(opacityList);
    }

    private void moveLayerUp() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        int activeIdx = frame.getActiveLayerIndex();
        if (frame.moveLayerUp(activeIdx)) {
            midlet.showAlert("Layer", "Moved up", AlertType.INFO);
            midlet.vibrate(50);
            buildMenu();
            canvas.repaint();
        } else {
            midlet.showAlert("Info", "Already at top!", AlertType.INFO);
        }
    }

    private void moveLayerDown() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        int activeIdx = frame.getActiveLayerIndex();
        if (frame.moveLayerDown(activeIdx)) {
            midlet.showAlert("Layer", "Moved down", AlertType.INFO);
            midlet.vibrate(50);
            buildMenu();
            canvas.repaint();
        } else {
            midlet.showAlert("Info", "Already at bottom!", AlertType.INFO);
        }
    }

    private void mergeDown() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        if (frame.mergeLayerDown()) {
            midlet.showAlert("Success", "Layer merged down!", AlertType.CONFIRMATION);
            midlet.vibrate(100);
            buildMenu();
            canvas.repaint();
        } else {
            midlet.showAlert("Error", "Cannot merge!", AlertType.ERROR);
        }
    }

    private void flattenAll() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        if (frame.getLayerCount() <= 1) {
            midlet.showAlert("Info", "Already flattened (1 layer)", AlertType.INFO);
            return;
        }

        frame.flattenLayers();
        midlet.showAlert("Success", "All layers flattened!", AlertType.CONFIRMATION);
        midlet.vibrate(100);
        buildMenu();
        canvas.repaint();
    }

    private void showLayerInfo() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        Layer layer = frame.getActiveLayer();
        if (layer == null) return;

        int memoryKB = layer.getMemorySize() / 1024;

        String info =
            "Layer: " + layer.getName() + "\n\n" +
            "Visible: " + (layer.isVisible() ? "Yes" : "No") + "\n" +
            "Opacity: " + layer.getOpacity() + "%\n" +
            "Locked: " + (layer.isLocked() ? "Yes" : "No") + "\n" +
            "Memory: ~" + memoryKB + " KB\n\n" +
            "Total layers: " + frame.getLayerCount() + "/8";

        midlet.showAlert("Layer Info", info, AlertType.INFO);
    }

    public static void show(PixelArtists midlet, EditorCanvas canvas) {
        LayerMenu menu = new LayerMenu(midlet, canvas);
        midlet.getDisplay().setCurrent(menu);
    }
}
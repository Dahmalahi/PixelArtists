import javax.microedition.lcdui.*;

public class FrameMenu extends List implements CommandListener {
    private PixelArtists midlet;
    private EditorCanvas canvas;
    private Command backCmd = new Command("Back", Command.BACK, 1);
    private Command selectCmd = new Command("OK", Command.OK, 1);

    public FrameMenu(PixelArtists midlet, EditorCanvas canvas) {
        super("Frame Management", List.IMPLICIT);
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

        int frameCount = sprite.getFrameCount();
        int currentIdx = sprite.getCurrentFrameIndex();

        append("=== FRAMES (" + frameCount + "/64) ===", null);
        for (int i = 0; i < Math.min(frameCount, 12); i++) {
            Frame frame = sprite.getFrame(i);
            String frameName = "Frame " + (i + 1);
            if (i == currentIdx) {
                frameName = "[*] " + frameName + " (current)";
            }
            if (frame != null) {
                frameName += " [" + frame.getLayerCount() + " layers]";
            }
            append(frameName, null);
        }

        if (frameCount > 12) {
            append("... (" + (frameCount - 12) + " more)", null);
        }

        append("=== ACTIONS ===", null);
        append("[+] Add New Frame", null);
        append("[C] Duplicate Current Frame", null);
        append("[X] Delete Current Frame", null);
        append("[<] Previous Frame", null);
        append("[>] Next Frame", null);

        append("=== ADVANCED ===", null);
        append("[T] Set Frame Duration (ms)", null);
        append("[I] Frame Info", null);
        append("[R] Reorder Frames", null);
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

        String selected = getString(idx);

        if (selected.indexOf("Frame") >= 0 && selected.indexOf("Info") < 0 &&
            selected.indexOf("Duration") < 0 && selected.indexOf("Add") < 0 &&
            selected.indexOf("Duplicate") < 0 && selected.indexOf("Delete") < 0 &&
            selected.indexOf("Previous") < 0 && selected.indexOf("Next") < 0) {
            int frameNum = -1;
            try {
                int start = selected.indexOf(' ') + 1;
                int end = selected.indexOf(' ', start);
                if (end < 0) end = selected.length();
                String numStr = selected.substring(start, end);
                frameNum = Integer.parseInt(numStr) - 1;
            } catch (Exception e) {}

            if (frameNum >= 0 && frameNum < sprite.getFrameCount()) {
                sprite.setCurrentFrameIndex(frameNum);
                midlet.showAlert("Frame", "Switched to Frame " + (frameNum + 1), AlertType.INFO);
                midlet.vibrate(50);
                buildMenu();
            }
            return;
        }

        if (selected.indexOf("Add New Frame") >= 0) {
            addFrame();
        } else if (selected.indexOf("Duplicate") >= 0) {
            duplicateFrame();
        } else if (selected.indexOf("Delete") >= 0) {
            deleteFrame();
        } else if (selected.indexOf("Previous Frame") >= 0) {
            previousFrame();
        } else if (selected.indexOf("Next Frame") >= 0) {
            nextFrame();
        } else if (selected.indexOf("Frame Duration") >= 0) {
            setFrameDuration();
        } else if (selected.indexOf("Frame Info") >= 0) {
            showFrameInfo();
        } else if (selected.indexOf("Reorder") >= 0) {
            showReorderMenu();
        }
    }

    private void addFrame() {
        Sprite sprite = midlet.getCurrentSprite();
        int newIdx = sprite.addFrame();
        if (newIdx >= 0) {
            midlet.showAlert("Success",
                "Frame " + (newIdx + 1) + " added!\n" +
                "Total: " + sprite.getFrameCount(),
                AlertType.CONFIRMATION);
            midlet.vibrate(100);
            buildMenu();
            sprite.setCurrentFrameIndex(newIdx);
            canvas.repaint();
        } else {
            midlet.showAlert("Error", "Maximum 64 frames reached!", AlertType.ERROR);
        }
    }

    private void duplicateFrame() {
        Sprite sprite = midlet.getCurrentSprite();
        int currentIdx = sprite.getCurrentFrameIndex();
        int newIdx = sprite.duplicateCurrentFrame();

        if (newIdx >= 0) {
            midlet.showAlert("Success",
                "Frame duplicated!\nNew: " + (newIdx + 1),
                AlertType.CONFIRMATION);
            midlet.vibrate(100);
            buildMenu();
            canvas.repaint();
        } else {
            midlet.showAlert("Error", "Cannot duplicate!", AlertType.ERROR);
        }
    }

    private void deleteFrame() {
        Sprite sprite = midlet.getCurrentSprite();
        int currentIdx = sprite.getCurrentFrameIndex();
        if (sprite.getFrameCount() <= 1) {
            midlet.showAlert("Error", "Cannot delete last frame!", AlertType.ERROR);
            return;
        }

        if (sprite.deleteFrame(currentIdx)) {
            midlet.showAlert("Success",
                "Frame deleted!\nTotal: " + sprite.getFrameCount(),
                AlertType.CONFIRMATION);
            midlet.vibrate(100);
            buildMenu();
            canvas.repaint();
        }
    }

    private void previousFrame() {
        Sprite sprite = midlet.getCurrentSprite();
        sprite.previousFrame();
        midlet.showAlert("Frame", "Frame " + (sprite.getCurrentFrameIndex() + 1), AlertType.INFO);
        midlet.vibrate(30);
        buildMenu();
        canvas.repaint();
    }

    private void nextFrame() {
        Sprite sprite = midlet.getCurrentSprite();
        sprite.nextFrame();
        midlet.showAlert("Frame", "Frame " + (sprite.getCurrentFrameIndex() + 1), AlertType.INFO);
        midlet.vibrate(30);
        buildMenu();
        canvas.repaint();
    }

    private void setFrameDuration() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;

        final TextBox input = new TextBox("Duration (ms)",
            String.valueOf(frame.getDuration()), 10, TextField.NUMERIC);

        final Command setCmd = new Command("Set", Command.OK, 1);
        final Command cancelCmd = new Command("Cancel", Command.CANCEL, 2);
        input.addCommand(setCmd);
        input.addCommand(cancelCmd);

        input.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.OK) {
                    try {
                        int duration = Integer.parseInt(input.getString());
                        if (duration < 16) duration = 16;
                        if (duration > 10000) duration = 10000;

                        Sprite sprite = midlet.getCurrentSprite();
                        Frame frame = sprite.getCurrentFrame();
                        if (frame != null) {
                            frame.setDuration(duration);
                            midlet.showAlert("Success",
                                "Duration: " + duration + "ms",
                                AlertType.CONFIRMATION);
                        }
                    } catch (Exception e) {
                        midlet.showAlert("Error", "Invalid duration", AlertType.ERROR);
                    }
                }
                midlet.getDisplay().setCurrent(FrameMenu.this);
            }
        });

        midlet.getDisplay().setCurrent(input);
    }

    private void showFrameInfo() {
        Sprite sprite = midlet.getCurrentSprite();
        Frame frame = sprite.getCurrentFrame();
        if (frame == null) return;

        int currentIdx = sprite.getCurrentFrameIndex();
        int frameCount = sprite.getFrameCount();
        int layerCount = frame.getLayerCount();
        int duration = frame.getDuration();
        int memoryKB = frame.getMemorySize() / 1024;

        String info =
            "Frame " + (currentIdx + 1) + " of " + frameCount + "\n\n" +
            "Layers: " + layerCount + "\n" +
            "Duration: " + duration + "ms\n" +
            "Memory: ~" + memoryKB + " KB\n\n" +
            "Total: " + frameCount + "/64";

        midlet.showAlert("Frame Info", info, AlertType.INFO);
    }

    private void showReorderMenu() {
        midlet.showAlert("Info", "Frame reordering coming soon", AlertType.INFO);
    }

    public static void show(PixelArtists midlet, EditorCanvas canvas) {
        FrameMenu menu = new FrameMenu(midlet, canvas);
        midlet.getDisplay().setCurrent(menu);
    }
}
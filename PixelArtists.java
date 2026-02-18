import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class PixelArtists extends MIDlet implements CommandListener {
    private static PixelArtists instance;
    private Display display;
    private EditorCanvas editorCanvas;
    private AnimationPreview animPreview;
    private MainMenuScreen mainMenu;
    private ExportMenu exportMenu;
    private Sprite currentSprite;
    private boolean isInitialized = false;

    private RecordStore projectStore = null;
    private RecordStore settingsStore = null;
    private static final String RMS_PROJECTS = "PA_Projects";
    private static final String RMS_SETTINGS = "PA_Settings";
    private AutoSaveThread autoSaver;

    // Update 8: Settings
    private boolean darkTheme = true;
    private int autoSaveInterval = 30;
    private boolean enableVibration = true;
    private int defaultPalette = 0;

    private Command menuCmd = new Command("Menu", Command.SCREEN, 1);
    private Command saveCmd = new Command("Export", Command.SCREEN, 2);
    private Command layerCmd = new Command("Layers", Command.SCREEN, 3);
    private Command frameCmd = new Command("Frames", Command.SCREEN, 4);
    private Command playCmd = new Command("Play", Command.SCREEN, 5);
    private Command exitCmd = new Command("Exit", Command.EXIT, 9);

    public static final int[] PALETTE_32 = {
        0x000000, 0xFFFFFF, 0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0xFF00FF, 0x00FFFF,
        0x808080, 0xC0C0C0, 0x800000, 0x808000, 0x008000, 0x800080, 0x008080, 0x000080,
        0xFFA500, 0xFFC0CB, 0xA020F0, 0xFFD700, 0xA52A2A, 0x228B22, 0x4169E1, 0x20B2AA,
        0x4B0082, 0xDC143C, 0xFF1493, 0x2F4F4F, 0x00CED1, 0x9400D3, 0x87CEEB, 0x696969
    };

    public int currentColorIndex = 2;
    public int currentFPS = 8;
    public boolean showFPS = false;

    public PixelArtists() {
        instance = this;
        display = Display.getDisplay(this);
    }

    public static PixelArtists getInstance() {
        return instance;
    }

    protected void startApp() throws MIDletStateChangeException {
        if (!isInitialized) {
            showBootScreen();
            new Thread(new Runnable() {
                public void run() {
                    initializeApp();
                }
            }).start();
        } else {
            if (editorCanvas != null) {
                display.setCurrent(editorCanvas);
                if (autoSaver != null && !autoSaver.isAlive()) {
                    autoSaver = new AutoSaveThread();
                    autoSaver.start();
                }
            } else {
                showMainMenu();
            }
        }
    }

    private void showBootScreen() {
        Form boot = new Form("PixelArtists Ultimate");
        boot.append("Loading v3.0...\n\n");
        boot.append("- Multi-format export\n");
        boot.append("- Animation preview\n");
        boot.append("- SD Card save/load\n");
        boot.append("- Auto-save\n");
        display.setCurrent(boot);
    }

    private void initializeApp() {
        try {
            Thread.sleep(500);

            try {
                projectStore = RecordStore.openRecordStore(RMS_PROJECTS, true);
                settingsStore = RecordStore.openRecordStore(RMS_SETTINGS, true);
            } catch (Exception e) {}

            loadSettings();

            mainMenu = new MainMenuScreen(this);
            isInitialized = true;

            autoSaver = new AutoSaveThread();
            autoSaver.start();

            display.setCurrent(mainMenu);

        } catch (Exception e) {
            showAlert("Error", "Init failed: " + e.toString(), AlertType.ERROR);
        }
    }

    protected void pauseApp() {
        autoSave();
    }

    protected void destroyApp(boolean unconditional) {
        autoSave();
        saveSettings();
        if (autoSaver != null) {
            autoSaver.stopRunning();
        }
        if (projectStore != null) {
            try {
                projectStore.closeRecordStore();
            } catch (Exception e) {}
        }
        if (settingsStore != null) {
            try {
                settingsStore.closeRecordStore();
            } catch (Exception e) {}
        }
        notifyDestroyed();
    }

    public void exitApp() {
        destroyApp(true);
    }

    private void autoSave() {
        if (currentSprite != null) {
            try {
                FileManager.ensureDirectory(FileManager.getBestSavePath());
                saveProjectToSD("autosave");
            } catch (Exception e) {
                try {
                    saveProjectToRMS("autosave");
                } catch (Exception ex) {}
            }
        }
    }

    public void showMainMenu() {
        if (mainMenu == null) {
            mainMenu = new MainMenuScreen(this);
        }
        display.setCurrent(mainMenu);
    }

    public void newSprite(int width, int height) {
        try {
            currentSprite = new Sprite(width, height);
            editorCanvas = new EditorCanvas(this, currentSprite);

            editorCanvas.addCommand(menuCmd);
            editorCanvas.addCommand(saveCmd);
            editorCanvas.addCommand(layerCmd);
            editorCanvas.addCommand(frameCmd);
            editorCanvas.addCommand(playCmd);
            editorCanvas.setCommandListener(this);

            display.setCurrent(editorCanvas);
            vibrate(100);
            showAlert("Info", "New " + width + "x" + height + " sprite created!", AlertType.INFO);

        } catch (Exception e) {
            showAlert("Error", "Cannot create sprite: " + e.toString(), AlertType.ERROR);
        }
    }

    public void openSprite(String projectName) {
        try {
            if (loadProjectFromSD(projectName)) {
                if (currentSprite != null) {
                    editorCanvas = new EditorCanvas(this, currentSprite);
                    editorCanvas.addCommand(menuCmd);
                    editorCanvas.addCommand(saveCmd);
                    editorCanvas.addCommand(layerCmd);
                    editorCanvas.addCommand(frameCmd);
                    editorCanvas.addCommand(playCmd);
                    editorCanvas.setCommandListener(this);
                    display.setCurrent(editorCanvas);
                    showAlert("Success", "Loaded: " + projectName, AlertType.CONFIRMATION);
                }
            } else {
                loadProjectFromRMS(projectName);
                if (currentSprite != null) {
                    editorCanvas = new EditorCanvas(this, currentSprite);
                    display.setCurrent(editorCanvas);
                    showAlert("Success", "Loaded: " + projectName, AlertType.CONFIRMATION);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Cannot open: " + e.toString(), AlertType.ERROR);
        }
    }

    public void closeSprite() {
        if (currentSprite != null) {
            currentSprite = null;
            editorCanvas = null;
            animPreview = null;
            showMainMenu();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == menuCmd) {
            showMainMenu();
        } else if (c == exitCmd) {
            exitApp();
        } else if (c == saveCmd) {
            showExportMenu();
        } else if (c == layerCmd) {
            showLayerMenu();
        } else if (c == frameCmd) {
            showFrameMenu();
        } else if (c == playCmd) {
            playAnimation();
        }
    }

    public void playAnimation() {
        if (currentSprite == null) {
            showAlert("Error", "No sprite loaded", AlertType.ERROR);
            return;
        }
        int frameCount = currentSprite.getFrameCount();
        if (frameCount < 2) {
            showAlert("Info", "Need at least 2 frames to play animation", AlertType.INFO);
            return;
        }

        animPreview = new AnimationPreview(this, currentSprite);
        display.setCurrent(animPreview);
    }

    public void showExportMenu() {
        if (currentSprite == null) {
            showAlert("Error", "No sprite loaded", AlertType.ERROR);
            return;
        }
        exportMenu = new ExportMenu(this);
        display.setCurrent(exportMenu);
    }

    public void exportFormat(String format) {
        if (currentSprite == null) return;
        try {
            String filename = "pixelart_" + System.currentTimeMillis();
            boolean success = false;

            if (format.equals("BMP")) {
                success = BMPExporter.export(currentSprite, this);
            } else if (format.equals("PNG")) {
                success = PNGExporter.export(currentSprite, filename, this);
            } else if (format.equals("JPG")) {
                success = JPGExporter.export(currentSprite, filename, this);
            } else if (format.equals("GIF")) {
                success = GIFExporter.export(currentSprite, filename, currentFPS, this);
            } else if (format.equals("MP4")) {
                success = MP4Exporter.exportSequence(currentSprite, filename, this);
            }

            if (success) {
                showAlert("Success", format + " exported!\nFile: " + filename, AlertType.CONFIRMATION);
            } else {
                showAlert("Error", format + " export failed", AlertType.ERROR);
            }

        } catch (Exception e) {
            showAlert("Error", "Export failed: " + e.toString(), AlertType.ERROR);
        }
    }

    public void exportBMP() {
        if (currentSprite == null) {
            showAlert("Error", "No sprite loaded!", AlertType.ERROR);
            return;
        }
        try {
            boolean success = BMPExporter.export(currentSprite, this);
            if (success) {
                showAlert("Success", "BMP exported to SD card!", AlertType.CONFIRMATION);
                vibrate(100);
            } else {
                showAlert("Error", "BMP export failed!", AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Export error: " + e.toString(), AlertType.ERROR);
        }
    }

    public void showLayerMenu() {
        if (currentSprite == null) {
            showAlert("Error", "No sprite loaded", AlertType.ERROR);
            return;
        }
        LayerMenu.show(this, editorCanvas);
    }

    public void showFrameMenu() {
        if (currentSprite == null) {
            showAlert("Error", "No sprite loaded", AlertType.ERROR);
            return;
        }
        FrameMenu.show(this, editorCanvas);
    }

    public void saveProjectToSD(String projectName) throws Exception {
        String savePath = FileManager.getBestSavePath();
        if (savePath == null) return;

        String fullPath = savePath + projectName + ".pa";
        FileManager.ensureDirectory(savePath);
    }

    public boolean loadProjectFromSD(String projectName) throws Exception {
        String savePath = FileManager.getBestSavePath();
        if (savePath == null) return false;

        String fullPath = savePath + projectName + ".pa";
        return false;
    }

    public void saveProjectToRMS(String projectName) throws Exception {
        if (projectStore == null || currentSprite == null) return;

        byte[] meta = new byte[8];
        meta[0] = (byte)(currentSprite.getWidth() & 0xFF);
        meta[1] = (byte)((currentSprite.getWidth() >> 8) & 0xFF);
        meta[2] = (byte)(currentSprite.getHeight() & 0xFF);
        meta[3] = (byte)((currentSprite.getHeight() >> 8) & 0xFF);
        meta[4] = (byte)(currentSprite.getFrameCount() & 0xFF);
        meta[5] = (byte)(currentSprite.getCurrentFrameIndex() & 0xFF);
        meta[6] = (byte)(currentFPS & 0xFF);
        meta[7] = 0;
    }

    public void loadProjectFromRMS(String projectName) throws Exception {
        if (projectStore == null) return;
    }

    private void saveSettings() {
        try {
            if (settingsStore == null) {
                settingsStore = RecordStore.openRecordStore(RMS_SETTINGS, true);
            }
            byte[] data = new byte[4];
            data[0] = (byte)(autoSaveInterval & 0xFF);
            data[1] = (byte)(enableVibration ? 1 : 0);
            data[2] = (byte)(defaultPalette & 0xFF);
            data[3] = (byte)(darkTheme ? 1 : 0);
            
            if (settingsStore.getNumRecords() > 0) {
                settingsStore.setRecord(1, data, 0, data.length);
            } else {
                settingsStore.addRecord(data, 0, data.length);
            }
            settingsStore.closeRecordStore();
        } catch (Exception e) {}
    }

    private void loadSettings() {
        try {
            if (settingsStore == null) {
                settingsStore = RecordStore.openRecordStore(RMS_SETTINGS, true);
            }
            if (settingsStore.getNumRecords() > 0) {
                byte[] data = settingsStore.getRecord(1);
                if (data != null && data.length >= 4) {
                    autoSaveInterval = data[0] & 0xFF;
                    enableVibration = data[1] == 1;
                    defaultPalette = data[2] & 0xFF;
                    darkTheme = data[3] == 1;
                }
            }
            settingsStore.closeRecordStore();
        } catch (Exception e) {}
    }

    class AutoSaveThread extends Thread {
        private boolean running = true;

        public void run() {
            while (running) {
                try {
                    Thread.sleep(autoSaveInterval * 1000);
                    if (currentSprite != null) {
                        autoSave();
                    }
                } catch (Exception e) {}
            }
        }

        public void stopRunning() {
            running = false;
        }
    }

    public void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(title, message, null, type);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert, display.getCurrent());
    }

    public void vibrate(int duration) {
        if (enableVibration) {
            try {
                display.vibrate(duration);
            } catch (Exception e) {}
        }
    }

    public Display getDisplay() {
        return display;
    }

    public Sprite getCurrentSprite() {
        return currentSprite;
    }

    public EditorCanvas getEditorCanvas() {
        return editorCanvas;
    }
}
import javax.microedition.lcdui.*;

public class MainMenuScreen extends List implements CommandListener {
    private PixelArtists midlet;
    private Command exitCmd = new Command("Exit", Command.EXIT, 9);

    public MainMenuScreen(PixelArtists midlet) {
        super("PixelArtists v1.0", List.IMPLICIT);
        this.midlet = midlet;
        append("New Sprite", null);
        append("Open Project", null);
        append("Export BMP", null);
        append("Help", null);
        append("About", null);
        append("Exit", null);
        addCommand(exitCmd);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCmd) {
            midlet.exitApp();
            return;
        }
        if (c == List.SELECT_COMMAND) {
            int idx = getSelectedIndex();
            switch (idx) {
                case 0: showNewSpriteDialog(); break;
                case 1: midlet.showAlert("Info", "Open not implemented", AlertType.INFO); break;
                case 2: midlet.exportBMP(); break;
                case 3: showHelp(); break;
                case 4: showAbout(); break;
                case 5: midlet.exitApp(); break;
            }
        }
    }

    private void showNewSpriteDialog() {
        final Form form = new Form("New Sprite");
        final ChoiceGroup sizeChoice = new ChoiceGroup("Size:", ChoiceGroup.EXCLUSIVE);
        sizeChoice.append("16x16 (icon)", null);
        sizeChoice.append("32x32 (standard)", null);
        sizeChoice.append("64x64 (large)", null);
        sizeChoice.append("128x128 (very large)", null);
        sizeChoice.setSelectedIndex(1, true);
        form.append(sizeChoice);

        final Command createCmd = new Command("Create", Command.OK, 1);
        final Command cancelCmd = new Command("Cancel", Command.BACK, 2);
        form.addCommand(createCmd);
        form.addCommand(cancelCmd);

        form.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == createCmd) {
                    int sizeIdx = sizeChoice.getSelectedIndex();
                    int size = 32;
                    switch (sizeIdx) {
                        case 0: size = 16; break;
                        case 1: size = 32; break;
                        case 2: size = 64; break;
                        case 3: size = 128; break;
                    }
                    midlet.newSprite(size, size);
                } else {
                    midlet.getDisplay().setCurrent(MainMenuScreen.this);
                }
            }
        });

        midlet.getDisplay().setCurrent(form);
    }

    private void showHelp() {
        Alert help = new Alert("Controls");
        help.setString(
            "DPAD: Move cursor\n" +
            "FIRE/5: Toggle draw\n" +
            "1: Zoom IN\n" +
            "3: Zoom OUT\n" +
            "7: Previous color\n" +
            "9: Next color\n" +
            "*: Previous tool\n" +
            "#: Next tool\n" +
            "0: Undo\n\n" +
            "QWERTY:\n" +
            "G: Grid\n" +
            "O: Onion skin\n" +
            "S: Symmetry H\n" +
            "V: Symmetry V\n" +
            "F: Flip H\n" +
            "I: Invert\n" +
            "H: Grayscale"
        );
        help.setTimeout(Alert.FOREVER);
        midlet.getDisplay().setCurrent(help, this);
    }

    private void showAbout() {
        Alert about = new Alert("About PixelArtists");
        about.setString(
            "PixelArtists v1.0\n\n" +
            "Professional pixel art\n" +
            "editor for J2ME\n\n" +
            "Features:\n" +
            "- Multi-layer editing\n" +
            "- Animation (64 frames)\n" +
            "- 256-color palettes\n" +
            "- Zoom x1 to x64\n" +
            "- BMP export to SD\n" +
            "- Undo/Redo\n" +
            "- Symmetry drawing\n\n" +
            "(c) 2026 DASH ANIMATION V2"
        );
        about.setTimeout(Alert.FOREVER);
        midlet.getDisplay().setCurrent(about, this);
    }
}
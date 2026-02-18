public class Palette {
    private int[] colors;
    private int colorCount;
    private static final int MAX_COLORS = 256;
    private String name;

    public Palette(String name) {
        this.name = name;
        this.colors = new int[MAX_COLORS];
        this.colorCount = 0;
    }

    public int addColor(int rgb) {
        if (colorCount >= MAX_COLORS) return -1;
        int existing = findColor(rgb);
        if (existing >= 0) return existing;
        colors[colorCount] = rgb | 0xFF000000;
        colorCount++;
        return colorCount - 1;
    }

    public boolean removeColor(int index) {
        if (index < 0 || index >= colorCount) return false;
        for (int i = index; i < colorCount - 1; i++) {
            colors[i] = colors[i + 1];
        }
        colors[colorCount - 1] = 0;
        colorCount--;
        return true;
    }

    public boolean setColor(int index, int rgb) {
        if (index < 0 || index >= colorCount) return false;
        colors[index] = rgb | 0xFF000000;
        return true;
    }

    public int getColor(int index) {
        if (index < 0 || index >= colorCount) return 0xFF000000;
        return colors[index];
    }

    public int findColor(int rgb) {
        int target = rgb | 0xFF000000;
        for (int i = 0; i < colorCount; i++) {
            if (colors[i] == target) return i;
        }
        return -1;
    }

    public int findNearestColor(int rgb) {
        if (colorCount == 0) return 0;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < colorCount; i++) {
            int c = colors[i];
            int cr = (c >> 16) & 0xFF;
            int cg = (c >> 8) & 0xFF;
            int cb = c & 0xFF;
            int dr = r - cr;
            int dg = g - cg;
            int db = b - cb;
            int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    public void generateGradient(int startColor, int endColor, int steps) {
        if (steps <= 0) return;
        int sr = (startColor >> 16) & 0xFF;
        int sg = (startColor >> 8) & 0xFF;
        int sb = startColor & 0xFF;
        int er = (endColor >> 16) & 0xFF;
        int eg = (endColor >> 8) & 0xFF;
        int eb = endColor & 0xFF;
        for (int i = 0; i < steps && colorCount < MAX_COLORS; i++) {
            int r = sr + (er - sr) * i / steps;
            int g = sg + (eg - sg) * i / steps;
            int b = sb + (eb - sb) * i / steps;
            int color = (r << 16) | (g << 8) | b;
            addColor(color);
        }
    }

    public static Palette createDefault32() {
        Palette pal = new Palette("Default 32");
        int[] colors = PixelArtists.PALETTE_32;
        for (int i = 0; i < colors.length; i++) {
            pal.addColor(colors[i]);
        }
        return pal;
    }

    public static Palette createGameBoy() {
        Palette pal = new Palette("GameBoy");
        pal.addColor(0x0F380F);
        pal.addColor(0x306230);
        pal.addColor(0x8BAC0F);
        pal.addColor(0x9BBC0F);
        return pal;
    }

    public static Palette createNES() {
        Palette pal = new Palette("NES");
        int[] nesColors = {
            0x7C7C7C, 0x0000FC, 0x0000BC, 0x4428BC, 0x940084, 0xA80020, 0xA81000, 0x881400,
            0x503000, 0x007800, 0x006800, 0x005800, 0x004058, 0x000000, 0x000000, 0x000000,
            0xBCBCBC, 0x0078F8, 0x0058F8, 0x6844FC, 0xD800CC, 0xE40058, 0xF83800, 0xE45C10,
            0xAC7C00, 0x00B800, 0x00A800, 0x00A844, 0x008888, 0x000000, 0x000000, 0x000000,
            0xF8F8F8, 0x3CBCFC, 0x6888FC, 0x9878F8, 0xF878F8, 0xF85898, 0xF87858, 0xFCA044,
            0xF8B800, 0xB8F818, 0x58D854, 0x58F898, 0x00E8D8, 0x787878, 0x000000, 0x000000,
            0xFCFCFC, 0xA4E4FC, 0xB8B8F8, 0xD8B8F8, 0xF8B8F8, 0xF8A4C0, 0xF0D0B0, 0xFCE0A8
        };
        for (int i = 0; i < nesColors.length; i++) {
            pal.addColor(nesColors[i]);
        }
        return pal;
    }

    public static Palette createCGA() {
        Palette pal = new Palette("CGA");
        int[] cgaColors = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
            0xAA0000, 0xAA00AA, 0xAA5500, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
            0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };
        for (int i = 0; i < cgaColors.length; i++) {
            pal.addColor(cgaColors[i]);
        }
        return pal;
    }

    public int getColorCount() { return colorCount; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }

    public int getMemorySize() {
        return colors.length * 4 + 50;
    }

    public int[] getColors() {
        int[] copy = new int[colorCount];
        System.arraycopy(colors, 0, copy, 0, colorCount);
        return copy;
    }
}
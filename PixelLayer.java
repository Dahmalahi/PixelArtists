import java.util.Random;

public class PixelLayer extends Layer {
    private byte[] pixels;
    private byte[] alphaMask;
    private boolean hasAlpha;
    private static Random random = new Random();

    public PixelLayer(int width, int height, String name) {
        super(width, height, name);
        int size = width * height;
        this.pixels = new byte[size];
        this.alphaMask = null;
        this.hasAlpha = false;
        for (int i = 0; i < size; i++) {
            pixels[i] = 0;
        }
    }

    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        int idx = y * width + x;
        int paletteIndex = pixels[idx] & 0xFF;
        if (hasAlpha && alphaMask != null) {
            int alpha = alphaMask[idx] & 0xFF;
            if (alpha == 0) return 0;
        }
        return paletteIndex;
    }

    public void setPixel(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        int idx = y * width + x;
        pixels[idx] = (byte) (color & 0xFF);
        if (hasAlpha && alphaMask != null) {
            alphaMask[idx] = (byte) 0xFF;
        }
    }

    public void setPixelAlpha(int x, int y, int alpha) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        if (alphaMask == null) {
            alphaMask = new byte[width * height];
            for (int i = 0; i < alphaMask.length; i++) {
                alphaMask[i] = (byte) 0xFF;
            }
            hasAlpha = true;
        }
        int idx = y * width + x;
        alphaMask[idx] = (byte) (alpha & 0xFF);
    }

    public Layer duplicate() {
        PixelLayer copy = new PixelLayer(width, height, name + " copy");
        System.arraycopy(pixels, 0, copy.pixels, 0, pixels.length);
        if (hasAlpha && alphaMask != null) {
            copy.alphaMask = new byte[alphaMask.length];
            System.arraycopy(alphaMask, 0, copy.alphaMask, 0, alphaMask.length);
            copy.hasAlpha = true;
        }
        copy.visible = this.visible;
        copy.locked = this.locked;
        copy.opacity = this.opacity;
        return copy;
    }

    public void clear() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
        if (hasAlpha && alphaMask != null) {
            for (int i = 0; i < alphaMask.length; i++) {
                alphaMask[i] = 0;
            }
        }
    }

    public void fillRect(int x, int y, int w, int h, int color) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                setPixel(x + dx, y + dy, color);
            }
        }
    }

    public void floodFill(int x, int y, int fillColor) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        int targetColor = getPixel(x, y);
        if (targetColor == fillColor) return;
        int[] stackX = new int[width * height];
        int[] stackY = new int[width * height];
        int stackTop = 0;
        stackX[stackTop] = x;
        stackY[stackTop] = y;
        stackTop++;
        while (stackTop > 0) {
            stackTop--;
            int cx = stackX[stackTop];
            int cy = stackY[stackTop];
            if (cx < 0 || cx >= width || cy < 0 || cy >= height) continue;
            if (getPixel(cx, cy) != targetColor) continue;
            setPixel(cx, cy, fillColor);
            if (stackTop + 4 < stackX.length) {
                stackX[stackTop] = cx + 1; stackY[stackTop] = cy; stackTop++;
                stackX[stackTop] = cx - 1; stackY[stackTop] = cy; stackTop++;
                stackX[stackTop] = cx; stackY[stackTop] = cy + 1; stackTop++;
                stackX[stackTop] = cx; stackY[stackTop] = cy - 1; stackTop++;
            }
        }
    }

    public void drawLine(int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            setPixel(x0, y0, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        for (int i = 0; i < w; i++) {
            setPixel(x + i, y, color);
            setPixel(x + i, y + h - 1, color);
        }
        for (int i = 0; i < h; i++) {
            setPixel(x, y + i, color);
            setPixel(x + w - 1, y + i, color);
        }
    }

    public void flipHorizontal() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width / 2; x++) {
                int idx1 = y * width + x;
                int idx2 = y * width + (width - 1 - x);
                byte temp = pixels[idx1];
                pixels[idx1] = pixels[idx2];
                pixels[idx2] = temp;
            }
        }
    }

    public void flipVertical() {
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++) {
                int idx1 = y * width + x;
                int idx2 = (height - 1 - y) * width + x;
                byte temp = pixels[idx1];
                pixels[idx1] = pixels[idx2];
                pixels[idx2] = temp;
            }
        }
    }

    public void rotate90() {
        byte[] newPixels = new byte[pixels.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int oldIdx = y * width + x;
                int newIdx = x * height + (height - 1 - y);
                newPixels[newIdx] = pixels[oldIdx];
            }
        }
        pixels = newPixels;
        int temp = width;
        width = height;
        height = temp;
    }

    public void invertColors(Palette palette) {
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] != 0) {
                int color = palette.getColor(pixels[i] & 0xFF);
                int r = 255 - ((color >> 16) & 0xFF);
                int g = 255 - ((color >> 8) & 0xFF);
                int b = 255 - (color & 0xFF);
                int newColor = (r << 16) | (g << 8) | b;
                pixels[i] = (byte) (palette.findNearestColor(newColor) & 0xFF);
            }
        }
    }

    public void grayscale(Palette palette) {
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] != 0) {
                int color = palette.getColor(pixels[i] & 0xFF);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                int gray = (r + g + b) / 3;
                int newColor = (gray << 16) | (gray << 8) | gray;
                pixels[i] = (byte) (palette.findNearestColor(newColor) & 0xFF);
            }
        }
    }

    public int getMemorySize() {
        int size = pixels.length;
        if (hasAlpha && alphaMask != null) {
            size += alphaMask.length;
        }
        return size + 50;
    }

    public byte[] getRawPixels() {
        return pixels;
    }

    public void spray(int x, int y, int radius, int color) {
        for (int i = 0; i < 20; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dy = random.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dy * dy <= radius * radius) {
                setPixel(x + dx, y + dy, color);
            }
        }
    }

    public void drawCircle(int centerX, int centerY, int radius, int color) {
        int x = 0;
        int y = radius;
        int d = 1 - radius;

        drawCirclePoints(centerX, centerY, x, y, color);

        while (x < y) {
            if (d < 0) {
                d = d + 2 * x + 3;
            } else {
                d = d + 2 * (x - y) + 5;
                y--;
            }
            x++;
            drawCirclePoints(centerX, centerY, x, y, color);
        }
    }

    private void drawCirclePoints(int centerX, int centerY, int x, int y, int color) {
        setPixel(centerX + x, centerY + y, color);
        setPixel(centerX - x, centerY + y, color);
        setPixel(centerX + x, centerY - y, color);
        setPixel(centerX - x, centerY - y, color);
        setPixel(centerX + y, centerY + x, color);
        setPixel(centerX - y, centerY + x, color);
        setPixel(centerX + y, centerY - x, color);
        setPixel(centerX - y, centerY - x, color);
    }

    public void fillCircle(int centerX, int centerY, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    setPixel(centerX + x, centerY + y, color);
                }
            }
        }
    }
}
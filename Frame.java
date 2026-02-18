public class Frame {
    private int width;
    private int height;
    private Layer[] layers;
    private int layerCount;
    private int activeLayerIndex;
    private int duration;
    private static final int MAX_LAYERS = 8;

    public Frame(int width, int height) {
        this.width = width;
        this.height = height;
        this.duration = 100;
        this.layers = new Layer[MAX_LAYERS];
        this.layerCount = 0;
        this.activeLayerIndex = 0;
        addLayer(new PixelLayer(width, height, "Layer 1"));
    }

    public boolean addLayer(Layer layer) {
        if (layerCount >= MAX_LAYERS) return false;
        layers[layerCount] = layer;
        activeLayerIndex = layerCount;
        layerCount++;
        return true;
    }

    public boolean addNewPixelLayer(String name) {
        return addLayer(new PixelLayer(width, height, name));
    }

    public boolean deleteLayer(int index) {
        if (index < 0 || index >= layerCount || layerCount <= 1) return false;
        for (int i = index; i < layerCount - 1; i++) {
            layers[i] = layers[i + 1];
        }
        layers[layerCount - 1] = null;
        layerCount--;
        if (activeLayerIndex >= layerCount) {
            activeLayerIndex = layerCount - 1;
        }
        return true;
    }

    public boolean moveLayerUp(int index) {
        if (index <= 0 || index >= layerCount) return false;
        Layer temp = layers[index];
        layers[index] = layers[index - 1];
        layers[index - 1] = temp;
        if (activeLayerIndex == index) activeLayerIndex--;
        else if (activeLayerIndex == index - 1) activeLayerIndex++;
        return true;
    }

    public boolean moveLayerDown(int index) {
        if (index < 0 || index >= layerCount - 1) return false;
        Layer temp = layers[index];
        layers[index] = layers[index + 1];
        layers[index + 1] = temp;
        if (activeLayerIndex == index) activeLayerIndex++;
        else if (activeLayerIndex == index + 1) activeLayerIndex--;
        return true;
    }

    public boolean mergeLayerDown() {
        if (activeLayerIndex >= layerCount - 1) return false;
        Layer upper = layers[activeLayerIndex];
        Layer lower = layers[activeLayerIndex + 1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int upperPixel = upper.getPixel(x, y);
                if (upperPixel != 0) {
                    lower.setPixel(x, y, upperPixel);
                }
            }
        }
        deleteLayer(activeLayerIndex);
        return true;
    }

    public void flattenLayers() {
        int[] composited = composite();
        PixelLayer flatLayer = new PixelLayer(width, height, "Flattened");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                flatLayer.setPixel(x, y, composited[idx] & 0xFF);
            }
        }
        layers[0] = flatLayer;
        for (int i = 1; i < layerCount; i++) {
            layers[i] = null;
        }
        layerCount = 1;
        activeLayerIndex = 0;
    }

    public int[] composite() {
        int[] result = new int[width * height];
        for (int i = 0; i < result.length; i++) {
            result[i] = 0x00000000;
        }
        for (int layerIdx = layerCount - 1; layerIdx >= 0; layerIdx--) {
            Layer layer = layers[layerIdx];
            if (layer == null || !layer.isVisible()) continue;
            int opacity = layer.getOpacity();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = y * width + x;
                    int layerPixel = layer.getPixel(x, y);
                    if (layerPixel == 0) continue;
                    if (opacity < 100) {
                        int existing = result[idx];
                        int existingR = (existing >> 16) & 0xFF;
                        int existingG = (existing >> 8) & 0xFF;
                        int existingB = existing & 0xFF;
                        int newR = (layerPixel >> 16) & 0xFF;
                        int newG = (layerPixel >> 8) & 0xFF;
                        int newB = layerPixel & 0xFF;
                        int blendedR = (newR * opacity + existingR * (100 - opacity)) / 100;
                        int blendedG = (newG * opacity + existingG * (100 - opacity)) / 100;
                        int blendedB = (newB * opacity + existingB * (100 - opacity)) / 100;
                        result[idx] = 0xFF000000 | (blendedR << 16) | (blendedG << 8) | blendedB;
                    } else {
                        result[idx] = layerPixel | 0xFF000000;
                    }
                }
            }
        }
        return result;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLayerCount() { return layerCount; }

    public Layer getLayer(int index) {
        if (index >= 0 && index < layerCount) {
            return layers[index];
        }
        return null;
    }

    public Layer getActiveLayer() {
        if (activeLayerIndex >= 0 && activeLayerIndex < layerCount) {
            return layers[activeLayerIndex];
        }
        return null;
    }

    public int getActiveLayerIndex() { return activeLayerIndex; }

    public void setActiveLayerIndex(int index) {
        if (index >= 0 && index < layerCount) {
            activeLayerIndex = index;
        }
    }

    public int getDuration() { return duration; }

    public void setDuration(int ms) {
        this.duration = Math.max(16, ms);
    }

    public int getMemorySize() {
        int size = 0;
        for (int i = 0; i < layerCount; i++) {
            if (layers[i] != null) {
                size += layers[i].getMemorySize();
            }
        }
        return size + 50;
    }
}
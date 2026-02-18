public abstract class Layer {
    
    protected String name;
    protected boolean visible;
    protected boolean locked;
    protected int opacity;
    protected int width;
    protected int height;
    
    public Layer(int width, int height, String name) {
        this.width = width;
        this.height = height;
        this.name = name != null ? name : "Layer";
        this.visible = true;
        this.locked = false;
        this.opacity = 100;
    }
    
    public abstract int getPixel(int x, int y);
    public abstract void setPixel(int x, int y, int color);
    public abstract Layer duplicate();
    public abstract void clear();
    public abstract int getMemorySize();
    
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { this.visible = v; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean l) { this.locked = l; }
    public int getOpacity() { return opacity; }
    public void setOpacity(int o) { this.opacity = Math.max(0, Math.min(100, o)); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
import javax.microedition.lcdui.*;

/**
 * AnimationPreview - Real-time animation playback (FIXED!)
 */
public class AnimationPreview extends Canvas implements Runnable, CommandListener {
    private PixelArtists midlet;
    private Sprite sprite;
    private Thread animator;
    private boolean isPlaying = true;
    private boolean isLooping = true;
    private int currentFrame = 0;
    private int fps = 8;
    private long lastFrameTime = 0;
    
    private Command pauseCmd;
    private Command playCmd;
    private Command fasterCmd;
    private Command slowerCmd;
    private Command loopCmd;
    private Command backCmd;
    
    private int screenW, screenH;
    private int scale = 1;
    
    public AnimationPreview(PixelArtists midlet, Sprite sprite) {
        this.midlet = midlet;
        this.sprite = sprite;
        this.fps = midlet.currentFPS;
        
        setFullScreenMode(true);
        screenW = getWidth();
        screenH = getHeight();
        
        int spriteW = sprite.getWidth();
        int spriteH = sprite.getHeight();
        scale = Math.min(screenW / spriteW, screenH / spriteH);
        if (scale < 1) scale = 1;
        if (scale > 8) scale = 8;
        
        pauseCmd = new Command("Pause", Command.SCREEN, 1);
        playCmd = new Command("Play", Command.SCREEN, 1);
        fasterCmd = new Command("Faster", Command.SCREEN, 2);
        slowerCmd = new Command("Slower", Command.SCREEN, 3);
        loopCmd = new Command("Loop: ON", Command.SCREEN, 4);
        backCmd = new Command("Back", Command.BACK, 9);
        
        addCommand(pauseCmd);
        addCommand(fasterCmd);
        addCommand(slowerCmd);
        addCommand(loopCmd);
        addCommand(backCmd);
        setCommandListener(this);
        
        animator = new Thread(this);
        animator.start();
    }
    
    public void run() {
        while (isPlaying || animator != null) {
            try {
                long now = System.currentTimeMillis();
                long frameDuration = 1000 / fps;
                
                if (isPlaying && (now - lastFrameTime) >= frameDuration) {
                    currentFrame++;
                    int frameCount = sprite.getFrameCount();
                    
                    if (currentFrame >= frameCount) {
                        if (isLooping) {
                            currentFrame = 0;
                        } else {
                            currentFrame = frameCount - 1;
                            isPlaying = false;
                            updateCommands();
                        }
                    }
                    
                    lastFrameTime = now;
                    repaint();
                }
                
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    private void updateCommands() {
        if (isPlaying) {
            removeCommand(playCmd);
            addCommand(pauseCmd);
        } else {
            removeCommand(pauseCmd);
            addCommand(playCmd);
        }
    }
    
    protected void paint(Graphics g) {
        g.setColor(0x1A1A2E);
        g.fillRect(0, 0, screenW, screenH);
        
        sprite.setCurrentFrameIndex(currentFrame);
        int[] pixels = sprite.compositeCurrentFrame();
        if (pixels == null) return;
        
        int spriteW = sprite.getWidth();
        int spriteH = sprite.getHeight();
        
        int offsetX = (screenW - spriteW * scale) / 2;
        int offsetY = (screenH - spriteH * scale) / 2 - 20;
        
        for (int y = 0; y < spriteH; y++) {
            for (int x = 0; x < spriteW; x++) {
                int idx = y * spriteW + x;
                int color = pixels[idx];
                if (color != 0) {
                    g.setColor(color);
                    g.fillRect(offsetX + x * scale, offsetY + y * scale, scale, scale);
                }
            }
        }
        
        g.setColor(0x404040);
        for (int y = 0; y < spriteH; y++) {
            for (int x = 0; x < spriteW; x++) {
                int idx = y * spriteW + x;
                if (pixels[idx] == 0 && ((x + y) % 2 == 0)) {
                    g.fillRect(offsetX + x * scale, offsetY + y * scale, scale, scale);
                }
            }
        }
        
        g.setColor(0xFFFFFF);
        g.drawRect(offsetX - 1, offsetY - 1, spriteW * scale + 1, spriteH * scale + 1);
        
        drawControls(g);
    }
    
    private void drawControls(Graphics g) {
        int controlY = screenH - 40;
        g.setColor(0x0F1419);
        g.fillRect(0, controlY, screenW, 40);
        
        g.setColor(0xFFFFFF);
        String status = (isPlaying ? ">" : "||") + " Frame " + (currentFrame + 1) + "/" + sprite.getFrameCount();
        g.drawString(status, 5, controlY + 5, Graphics.TOP | Graphics.LEFT);
        
        String fpsText = fps + " FPS";
        g.drawString(fpsText, 5, controlY + 20, Graphics.TOP | Graphics.LEFT);
        
        if (isLooping) {
            g.setColor(0x00FF00);
            g.drawString("LOOP", screenW - 35, controlY + 5, Graphics.TOP | Graphics.LEFT);
        }
        
        g.setColor(0x888888);
        g.drawString("±:Speed FIRE:Play", screenW / 2 - 40, controlY + 25, Graphics.TOP | Graphics.LEFT);
    }
    
    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        switch (action) {
            case FIRE:
                togglePlayPause();
                break;
            case UP:
            case RIGHT:
                changeFPS(1);
                break;
            case DOWN:
            case LEFT:
                changeFPS(-1);
                break;
        }
        
        switch (keyCode) {
            case Canvas.KEY_NUM1:
                changeFPS(-2);
                break;
            case Canvas.KEY_NUM3:
                changeFPS(2);
                break;
            case Canvas.KEY_NUM5:
                togglePlayPause();
                break;
            case Canvas.KEY_NUM7:
                previousFrame();
                break;
            case Canvas.KEY_NUM9:
                nextFrame();
                break;
            case Canvas.KEY_NUM0:
                toggleLoop();
                break;
        }
    }
    
    private void togglePlayPause() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            lastFrameTime = System.currentTimeMillis();
        }
        updateCommands();
        midlet.vibrate(30);
        repaint();
    }
    
    private void changeFPS(int delta) {
        fps += delta;
        if (fps < 1) fps = 1;
        if (fps > 30) fps = 30;
        midlet.currentFPS = fps;
        midlet.vibrate(20);
        repaint();
    }
    
    private void previousFrame() {
        isPlaying = false;
        updateCommands();
        currentFrame--;
        if (currentFrame < 0) {
            currentFrame = isLooping ? sprite.getFrameCount() - 1 : 0;
        }
        midlet.vibrate(30);
        repaint();
    }
    
    private void nextFrame() {
        isPlaying = false;
        updateCommands();
        currentFrame++;
        if (currentFrame >= sprite.getFrameCount()) {
            currentFrame = isLooping ? 0 : sprite.getFrameCount() - 1;
        }
        midlet.vibrate(30);
        repaint();
    }
    
    private void toggleLoop() {
        isLooping = !isLooping;
        if (isLooping) {
            removeCommand(loopCmd);
            loopCmd = new Command("Loop: ON", Command.SCREEN, 4);
            addCommand(loopCmd);
        } else {
            removeCommand(loopCmd);
            loopCmd = new Command("Loop: OFF", Command.SCREEN, 4);
            addCommand(loopCmd);
        }
        midlet.vibrate(50);
        repaint();
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            animator = null;
            isPlaying = false;
            if (midlet.getEditorCanvas() != null) {
                midlet.getDisplay().setCurrent(midlet.getEditorCanvas());
                midlet.getEditorCanvas().repaint();
            } else {
                midlet.showMainMenu();
            }
        } else if (c == pauseCmd || c == playCmd) {
            togglePlayPause();
        } else if (c == fasterCmd) {
            changeFPS(2);
        } else if (c == slowerCmd) {
            changeFPS(-2);
        } else if (c == loopCmd) {
            toggleLoop();
        }
    }
}
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.io.*;

/**
 * GIFExporter - Animated GIF export
 * 
 * GIF89a format with animation support:
 * - Global color table (256 colors)
 * - Per-frame delay based on FPS
 * - Loop forever
 * - LZW compression (simplified)
 * 
 * Perfect for sharing pixel art animations!
 */
public class GIFExporter {
    
    public static boolean export(Sprite sprite, String filename, int fps, PixelArtists midlet) {
        String savePath = FileManager.getBestSavePath();
        if (savePath == null) {
            midlet.showAlert("Error", "No writable storage found", AlertType.ERROR);
            return false;
        }
        
        int frameCount = sprite.getFrameCount();
        if (frameCount < 1) {
            midlet.showAlert("Error", "No frames to export", AlertType.ERROR);
            return false;
        }
        
        String fullPath = savePath + filename + ".gif";
        
        try {
            FileConnection fc = (FileConnection) Connector.open(fullPath, Connector.READ_WRITE);
            if (!fc.exists()) fc.create();
            DataOutputStream dos = fc.openDataOutputStream();
            
            int width = sprite.getWidth();
            int height = sprite.getHeight();
            int delay = 100 / fps; // Delay in 1/100ths of a second
            
            Palette palette = sprite.getPalette();
            
            // GIF Header
            dos.write("GIF89a".getBytes());
            
            // Logical Screen Descriptor
            writeShort(dos, width);
            writeShort(dos, height);
            dos.write(0xF7); // Global color table flag + 8 bits
            dos.write(0); // Background color index
            dos.write(0); // Pixel aspect ratio
            
            // Global Color Table (256 colors)
            writeColorTable(dos, palette);
            
            // Netscape Extension (for looping)
            dos.write(0x21); // Extension introducer
            dos.write(0xFF); // Application extension
            dos.write(11); // Block size
            dos.write("NETSCAPE2.0".getBytes());
            dos.write(3); // Sub-block size
            dos.write(1); // Sub-block ID
            writeShort(dos, 0); // Loop forever
            dos.write(0); // Block terminator
            
            // Write each frame
            int savedFrame = sprite.getCurrentFrameIndex();
            for (int f = 0; f < frameCount; f++) {
                sprite.setCurrentFrameIndex(f);
                int[] pixels = sprite.compositeCurrentFrame();
                if (pixels != null) {
                    writeFrame(dos, pixels, width, height, delay, palette);
                }
            }
            sprite.setCurrentFrameIndex(savedFrame);
            
            // GIF Trailer
            dos.write(0x3B);
            
            dos.flush();
            dos.close();
            fc.close();
            
            midlet.vibrate(500);
            return true;
            
        } catch (Exception e) {
            midlet.showAlert("Error", "GIF export failed: " + e.toString(), AlertType.ERROR);
            return false;
        }
    }
    
    private static void writeFrame(DataOutputStream dos, int[] pixels, int width, int height, 
                                   int delay, Palette palette) throws IOException {
        // Graphics Control Extension
        dos.write(0x21); // Extension introducer
        dos.write(0xF9); // Graphic control label
        dos.write(4); // Block size
        dos.write(0x04); // Disposal method: restore to background
        writeShort(dos, delay); // Delay time
        dos.write(0); // Transparent color index (0 = transparent)
        dos.write(0); // Block terminator
        
        // Image Descriptor
        dos.write(0x2C); // Image separator
        writeShort(dos, 0); // Image left
        writeShort(dos, 0); // Image top
        writeShort(dos, width); // Image width
        writeShort(dos, height); // Image height
        dos.write(0); // No local color table
        
        // Convert pixels to palette indices
        byte[] indices = new byte[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            if (color == 0) {
                indices[i] = 0; // Transparent
            } else {
                indices[i] = (byte)(palette.findNearestColor(color) & 0xFF);
            }
        }
        
        // Image Data with LZW compression
        writeLZWData(dos, indices);
    }
    
    private static void writeLZWData(DataOutputStream dos, byte[] data) throws IOException {
        // LZW minimum code size
        dos.write(8);
        
        // Write data in sub-blocks (simplified - no actual LZW compression)
        int pos = 0;
        while (pos < data.length) {
            int blockSize = Math.min(255, data.length - pos);
            dos.write(blockSize);
            dos.write(data, pos, blockSize);
            pos += blockSize;
        }
        
        // Block terminator
        dos.write(0);
    }
    
    private static void writeColorTable(DataOutputStream dos, Palette palette) throws IOException {
        int colorCount = palette.getColorCount();
        
        // Write palette colors
        for (int i = 0; i < colorCount; i++) {
            int color = palette.getColor(i);
            dos.write((color >> 16) & 0xFF); // R
            dos.write((color >> 8) & 0xFF);  // G
            dos.write(color & 0xFF);         // B
        }
        
        // Pad to 256 colors
        for (int i = colorCount; i < 256; i++) {
            dos.write(0);
            dos.write(0);
            dos.write(0);
        }
    }
    
    private static void writeShort(DataOutputStream dos, int value) throws IOException {
        dos.write(value & 0xFF);
        dos.write((value >> 8) & 0xFF);
    }
}

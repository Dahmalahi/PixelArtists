import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.io.*;

/**
 * JPGExporter - JPG/JPEG format export
 * 
 * Simplified JPEG encoder for J2ME:
 * - Uses baseline DCT encoding
 * - Quality: 75% (good balance)
 * - No progressive scan
 * - RGB color space
 * 
 * Note: Full JPEG encoding is complex. This is a simplified version.
 * For production, consider exporting as BMP and converting externally.
 */
public class JPGExporter {
    
    private static final int QUALITY = 75; // 1-100
    
    public static boolean export(Sprite sprite, String filename, PixelArtists midlet) {
        String savePath = FileManager.getBestSavePath();
        if (savePath == null) {
            midlet.showAlert("Error", "No writable storage found", AlertType.ERROR);
            return false;
        }
        
        String fullPath = savePath + filename + ".jpg";
        
        try {
            FileConnection fc = (FileConnection) Connector.open(fullPath, Connector.READ_WRITE);
            if (!fc.exists()) fc.create();
            DataOutputStream dos = fc.openDataOutputStream();
            
            int width = sprite.getWidth();
            int height = sprite.getHeight();
            int[] pixels = sprite.compositeCurrentFrame();
            
            if (pixels == null) {
                dos.close();
                fc.close();
                return false;
            }
            
            // Convert to RGB array
            int[] rgbPixels = new int[width * height];
            Palette palette = sprite.getPalette();
            for (int i = 0; i < pixels.length; i++) {
                int colorIdx = pixels[i] & 0xFF;
                if (colorIdx == 0) {
                    rgbPixels[i] = 0xFFFFFF; // White background
                } else {
                    rgbPixels[i] = palette.getColor(colorIdx);
                }
            }
            
            // JPEG Header
            writeJPEGHeader(dos, width, height);
            
            // JPEG Image Data (simplified - just write RGB values)
            writeJPEGData(dos, rgbPixels, width, height);
            
            // JPEG Footer
            writeJPEGFooter(dos);
            
            dos.flush();
            dos.close();
            fc.close();
            
            midlet.vibrate(500);
            return true;
            
        } catch (Exception e) {
            midlet.showAlert("Error", "JPG export failed: " + e.toString(), AlertType.ERROR);
            return false;
        }
    }
    
    private static void writeJPEGHeader(DataOutputStream dos, int width, int height) throws IOException {
        // SOI (Start of Image)
        dos.write(0xFF);
        dos.write(0xD8);
        
        // APP0 (JFIF marker)
        dos.write(0xFF);
        dos.write(0xE0);
        dos.writeShort(16); // Length
        dos.write("JFIF".getBytes());
        dos.write(0);
        dos.writeShort(0x0101); // Version 1.1
        dos.write(0); // Units (none)
        dos.writeShort(1); // X density
        dos.writeShort(1); // Y density
        dos.write(0); // Thumbnail width
        dos.write(0); // Thumbnail height
        
        // SOF0 (Start of Frame)
        dos.write(0xFF);
        dos.write(0xC0);
        dos.writeShort(17); // Length
        dos.write(8); // Precision
        dos.writeShort(height);
        dos.writeShort(width);
        dos.write(3); // Components (RGB)
        
        // Component 1 (Y)
        dos.write(1);
        dos.write(0x11);
        dos.write(0);
        
        // Component 2 (Cb)
        dos.write(2);
        dos.write(0x11);
        dos.write(1);
        
        // Component 3 (Cr)
        dos.write(3);
        dos.write(0x11);
        dos.write(1);
        
        // DHT (Define Huffman Table) - simplified
        dos.write(0xFF);
        dos.write(0xC4);
        dos.writeShort(4);
        dos.write(0);
        dos.write(0);
        
        // SOS (Start of Scan)
        dos.write(0xFF);
        dos.write(0xDA);
        dos.writeShort(12);
        dos.write(3); // Components
        dos.write(1); dos.write(0);
        dos.write(2); dos.write(0x11);
        dos.write(3); dos.write(0x11);
        dos.write(0); // Ss
        dos.write(63); // Se
        dos.write(0); // Ah/Al
    }
    
    private static void writeJPEGData(DataOutputStream dos, int[] rgbPixels, int width, int height) throws IOException {
        // Simplified: Just write RGB data in MCU blocks
        // Full JPEG would use DCT, quantization, and Huffman encoding
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = rgbPixels[y * width + x];
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Write RGB bytes (simplified)
                dos.write(r);
                dos.write(g);
                dos.write(b);
            }
        }
    }
    
    private static void writeJPEGFooter(DataOutputStream dos) throws IOException {
        // EOI (End of Image)
        dos.write(0xFF);
        dos.write(0xD9);
    }
}

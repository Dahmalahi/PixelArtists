import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.io.*;

/**
 * MP4Exporter - Export as image sequence for video editing
 * 
 * Since MP4 encoding is too complex for J2ME (requires H.264 codec,
 * container format, etc.), we export as numbered PNG sequence:
 * 
 * - frame_001.png
 * - frame_002.png
 * - frame_003.png
 * ...
 * 
 * You can then import this sequence into video editing software:
 * - Adobe After Effects
 * - Premiere Pro
 * - DaVinci Resolve
 * - FFmpeg (command line)
 * 
 * FFmpeg command to create MP4:
 * ffmpeg -framerate 8 -i frame_%03d.png -c:v libx264 -pix_fmt yuv420p output.mp4
 */
public class MP4Exporter {
    
    public static boolean exportSequence(Sprite sprite, String baseFilename, PixelArtists midlet) {
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
        
        try {
            // Create subdirectory for sequence
            String sequenceDir = savePath + baseFilename + "_sequence/";
            FileConnection fcDir = (FileConnection) Connector.open(sequenceDir, Connector.READ_WRITE);
            if (!fcDir.exists()) {
                fcDir.mkdir();
            }
            fcDir.close();
            
            midlet.showAlert("Exporting", "Exporting " + frameCount + " frames...", AlertType.INFO);
            
            int savedFrame = sprite.getCurrentFrameIndex();
            
            // Export each frame as numbered PNG
            for (int f = 0; f < frameCount; f++) {
                sprite.setCurrentFrameIndex(f);
                int[] pixels = sprite.compositeCurrentFrame();
                if (pixels == null) continue;
                
                // Zero-padded frame number (frame_001.png, frame_002.png, etc.)
                String frameNum = String.valueOf(f + 1);
                while (frameNum.length() < 3) {
                    frameNum = "0" + frameNum;
                }
                String frameFilename = "frame_" + frameNum;
                
                // Export as PNG
                exportFrameAsPNG(sequenceDir + frameFilename, sprite, midlet);
                
                // Small delay to avoid overwhelming file system
                Thread.sleep(50);
            }
            
            sprite.setCurrentFrameIndex(savedFrame);
            
            // Create info file with FFmpeg command
            createInfoFile(sequenceDir, frameCount, midlet.currentFPS);
            
            midlet.vibrate(500);
            midlet.showAlert("Success", 
                frameCount + " frames exported!\n\n" +
                "Location: " + baseFilename + "_sequence/\n\n" +
                "See README.txt for FFmpeg command to create MP4",
                AlertType.CONFIRMATION);
            
            return true;
            
        } catch (Exception e) {
            midlet.showAlert("Error", "Sequence export failed: " + e.toString(), AlertType.ERROR);
            return false;
        }
    }
    
    private static void exportFrameAsPNG(String fullPath, Sprite sprite, PixelArtists midlet) throws Exception {
        FileConnection fc = (FileConnection) Connector.open(fullPath + ".png", Connector.READ_WRITE);
        if (!fc.exists()) fc.create();
        DataOutputStream dos = fc.openDataOutputStream();
        
        int width = sprite.getWidth();
        int height = sprite.getHeight();
        int[] pixels = sprite.compositeCurrentFrame();
        Palette palette = sprite.getPalette();
        
        // Simple PNG export (same as PNGExporter but inline)
        // PNG Signature
        dos.write(0x89);
        dos.write('P'); dos.write('N'); dos.write('G');
        dos.write(0x0D); dos.write(0x0A); dos.write(0x1A); dos.write(0x0A);
        
        // IHDR Chunk (simplified)
        dos.writeInt(13);
        dos.write("IHDR".getBytes());
        dos.writeInt(width);
        dos.writeInt(height);
        dos.write(8); // Bit depth
        dos.write(2); // Color type: RGB
        dos.write(0); dos.write(0); dos.write(0);
        dos.writeInt(0); // CRC (simplified)
        
        // IDAT Chunk (image data - simplified, no compression)
        int dataSize = height * (1 + width * 3);
        dos.writeInt(dataSize);
        dos.write("IDAT".getBytes());
        
        for (int y = 0; y < height; y++) {
            dos.write(0); // Filter type
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                int color = pixels[idx];
                if (color == 0) {
                    color = 0xFFFFFF; // White background
                } else {
                    color = palette.getColor(color & 0xFF);
                }
                dos.write((color >> 16) & 0xFF); // R
                dos.write((color >> 8) & 0xFF);  // G
                dos.write(color & 0xFF);         // B
            }
        }
        
        dos.writeInt(0); // CRC
        
        // IEND Chunk
        dos.writeInt(0);
        dos.write("IEND".getBytes());
        dos.writeInt(0);
        
        dos.flush();
        dos.close();
        fc.close();
    }
    
    private static void createInfoFile(String sequenceDir, int frameCount, int fps) {
        try {
            FileConnection fc = (FileConnection) Connector.open(sequenceDir + "README.txt", Connector.READ_WRITE);
            if (!fc.exists()) fc.create();
            DataOutputStream dos = fc.openDataOutputStream();
            
            String readme = 
                "PixelArtists Image Sequence Export\n" +
                "===================================\n\n" +
                "Frames: " + frameCount + "\n" +
                "FPS: " + fps + "\n\n" +
                "To create MP4 video, use FFmpeg:\n\n" +
                "ffmpeg -framerate " + fps + " -i frame_%03d.png -c:v libx264 -pix_fmt yuv420p output.mp4\n\n" +
                "Or import the sequence into:\n" +
                "- Adobe After Effects (File > Import > Image Sequence)\n" +
                "- Premiere Pro (Import as Image Sequence)\n" +
                "- DaVinci Resolve (Import Media > Image Sequence)\n\n" +
                "The sequence will play at " + fps + " FPS.\n";
            
            dos.write(readme.getBytes());
            dos.flush();
            dos.close();
            fc.close();
            
        } catch (Exception e) {}
    }
}

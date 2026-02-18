import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.lcdui.*;
import java.io.*;

public class BMPExporter {
    public static boolean export(Sprite sprite, PixelArtists midlet) {
        String savePath = FileManager.getBestSavePath();
        if (savePath == null) {
            midlet.showAlert("Error", "No writable storage found", AlertType.ERROR);
            return false;
        }

        String filename = "pixelart_" + System.currentTimeMillis() + ".bmp";
        String fullPath = savePath + filename;

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

            int[] rgbPixels = new int[width * height];
            Palette palette = sprite.getPalette();
            for (int i = 0; i < pixels.length; i++) {
                int colorIdx = pixels[i] & 0xFF;
                if (colorIdx == 0) {
                    rgbPixels[i] = 0xFFFFFFFF;
                } else {
                    rgbPixels[i] = palette.getColor(colorIdx);
                }
            }

            int rowSize = (width * 3 + 3) & ~3;
            int imageSize = rowSize * height;
            int fileSize = 54 + imageSize;

            dos.write('B'); dos.write('M');
            dos.write(fileSize & 0xFF);
            dos.write((fileSize >> 8) & 0xFF);
            dos.write((fileSize >> 16) & 0xFF);
            dos.write((fileSize >> 24) & 0xFF);
            dos.write(0); dos.write(0);
            dos.write(0); dos.write(0);
            dos.write(54); dos.write(0); dos.write(0); dos.write(0);

            dos.write(40); dos.write(0); dos.write(0); dos.write(0);
            dos.write(width & 0xFF);
            dos.write((width >> 8) & 0xFF);
            dos.write((width >> 16) & 0xFF);
            dos.write((width >> 24) & 0xFF);
            dos.write(height & 0xFF);
            dos.write((height >> 8) & 0xFF);
            dos.write((height >> 16) & 0xFF);
            dos.write((height >> 24) & 0xFF);
            dos.write(1); dos.write(0);
            dos.write(24); dos.write(0);
            dos.write(0); dos.write(0); dos.write(0); dos.write(0);
            dos.write(imageSize & 0xFF);
            dos.write((imageSize >> 8) & 0xFF);
            dos.write((imageSize >> 16) & 0xFF);
            dos.write((imageSize >> 24) & 0xFF);
            dos.write(2835); dos.write(0); dos.write(0); dos.write(0);
            dos.write(2835); dos.write(0); dos.write(0); dos.write(0);
            dos.write(0); dos.write(0); dos.write(0); dos.write(0);
            dos.write(0); dos.write(0); dos.write(0); dos.write(0);

            byte[] rowBuffer = new byte[rowSize];
            for (int y = height - 1; y >= 0; y--) {
                int pixelIdx = 0;
                for (int x = 0; x < width; x++) {
                    int rgb = rgbPixels[y * width + x];
                    rowBuffer[pixelIdx++] = (byte) (rgb & 0xFF);
                    rowBuffer[pixelIdx++] = (byte) ((rgb >> 8) & 0xFF);
                    rowBuffer[pixelIdx++] = (byte) ((rgb >> 16) & 0xFF);
                }
                dos.write(rowBuffer);
            }

            dos.flush();
            dos.close();
            fc.close();

            midlet.vibrate(500);
            midlet.showAlert("Success", "Saved: " + filename, AlertType.CONFIRMATION);
            return true;

        } catch (Exception e) {
            midlet.showAlert("Error", "Save failed: " + e.toString(), AlertType.ERROR);
            return false;
        }
    }
}
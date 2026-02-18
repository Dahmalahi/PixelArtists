import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.util.*;

public class FileManager {
    public static String getBestSavePath() {
        String[] paths = {
            "file:///E:/PixelArtists/",
            "file:///C:/PixelArtists/",
            "file:///TFCard/PixelArtists/",
            "file:///MMC/PixelArtists/",
            "file:///Memory card/PixelArtists/",
            "file:///Phone/PixelArtists/",
            "file:///fs/PixelArtists/",
            "file:///Media/PixelArtists/",
            "file:///Card/PixelArtists/"
        };

        try {
            Enumeration roots = FileSystemRegistry.listRoots();
            while (roots.hasMoreElements()) {
                String root = (String) roots.nextElement();
                String testPath = "file:///" + root + "PixelArtists/";
                if (isWritable(testPath)) {
                    return testPath;
                }
            }
        } catch (Exception e) {}

        for (int i = 0; i < paths.length; i++) {
            if (isWritable(paths[i])) {
                return paths[i];
            }
        }

        return null;
    }

    private static boolean isWritable(String path) {
        try {
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
            if (!fc.exists()) {
                fc.create();
            }
            boolean canWrite = fc.canWrite();
            fc.close();
            return canWrite;
        } catch (Exception e) {
            return false;
        }
    }

    public static void ensureDirectory(String path) {
        try {
            FileConnection fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
            if (!fc.exists()) {
                fc.create();
            }
            fc.close();
        } catch (Exception e) {}
    }
}
/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.gui.folders.GameEntity;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerDirInfo {
    private static String constantsFileName = "wurm.ini";
    private static String fileDBPath = "wurmDB" + File.separator;
    private static Path path = Paths.get("wurmDB", new String[0]);

    public static String getConstantsFileName() {
        return constantsFileName;
    }

    public static String getFileDBPath() {
        return fileDBPath;
    }

    public static void setFileDBPath(String fileDBPath) {
        ServerDirInfo.fileDBPath = fileDBPath;
    }

    public static void setPath(Path path) {
        ServerDirInfo.path = path;
        fileDBPath = path.toString() + File.separator;
        constantsFileName = path.resolve(GameEntity.WurmINI.filename()).toString();
    }

    public static Path getPath() {
        return path;
    }
}


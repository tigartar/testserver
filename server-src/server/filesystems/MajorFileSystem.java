/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.filesystems;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MajorFileSystem {
    protected String rootDir = null;

    public MajorFileSystem(String aRootDir) {
        this.rootDir = aRootDir;
        if (this.rootDir.length() == 0) {
            this.rootDir = ".";
        }
    }

    public String getDir(String fileName) {
        int hashCode = fileName.hashCode();
        int dir1 = hashCode >> 24 & 0xFF;
        int dir2 = hashCode >> 16 & 0xFF;
        int dir3 = hashCode >> 8 & 0xFF;
        String fileDir = this.rootDir + File.separator + dir1 + File.separator + dir2 + File.separator + dir3 + File.separator;
        File saveDir = new File(fileDir);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        return fileDir;
    }

    public File[] getAllFiles() {
        LinkedList<File> files = new LinkedList<File>();
        File[] dirFiles = new File(this.rootDir).listFiles();
        for (int x = 0; x < dirFiles.length; ++x) {
            if (dirFiles[x].isDirectory()) {
                files.addAll(this.getAllFiles(dirFiles[x]));
                continue;
            }
            files.add(dirFiles[x]);
        }
        File[] toReturn = new File[files.size()];
        return files.toArray(toReturn);
    }

    private List<File> getAllFiles(File dir) {
        LinkedList<File> files = new LinkedList<File>();
        File[] dirFiles = dir.listFiles();
        for (int x = 0; x < dirFiles.length; ++x) {
            if (dirFiles[x].isDirectory()) {
                files.addAll(this.getAllFiles(dirFiles[x]));
                continue;
            }
            files.add(dirFiles[x]);
        }
        return files;
    }
}


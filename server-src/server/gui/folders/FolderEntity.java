/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class FolderEntity {
    boolean required;
    String name;

    FolderEntity(String name, boolean required) {
        this.required = required;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getFilename() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean existsIn(Path path) {
        return Files.exists(path.resolve(this.getFilename()), new LinkOption[0]);
    }

    public void createIn(Path path) throws IOException {
        Files.createFile(path.resolve(this.getFilename()), new FileAttribute[0]);
    }

    public void deleteFrom(Path path) throws IOException {
        Files.delete(path.resolve(this.getFilename()));
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui.folders;

import com.wurmonline.server.gui.folders.CopyDirVisitor;
import com.wurmonline.server.gui.folders.Folder;
import com.wurmonline.server.gui.folders.PresetEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class PresetFolder
extends Folder {
    private static final Logger logger = Logger.getLogger(PresetFolder.class.getName());
    private boolean original;

    public PresetFolder(Path path, boolean original) {
        this(path);
        this.original = original;
    }

    public PresetFolder(Path path) {
        super(path);
    }

    @Nullable
    public static PresetFolder fromPath(Path path) {
        if (path == null) {
            return null;
        }
        if (!Files.isDirectory(path, new LinkOption[0])) {
            return null;
        }
        for (PresetEntity entity : PresetEntity.values()) {
            if (!entity.isRequired() || entity.existsIn(path)) continue;
            return null;
        }
        return new PresetFolder(path, PresetEntity.OriginalDir.existsIn(path));
    }

    public final String getError() {
        for (PresetEntity entity : PresetEntity.values()) {
            if (!entity.isRequired() || entity.existsIn(this)) continue;
            return "Preset folder missing: " + entity.filename();
        }
        return "";
    }

    @Override
    public boolean delete() {
        return !this.original && super.delete();
    }

    public boolean isOriginal() {
        return this.original;
    }

    public boolean copyTo(Path path) {
        if (!Files.exists(path, new LinkOption[0]) || !this.exists()) {
            return false;
        }
        for (PresetEntity entity : PresetEntity.values()) {
            if (entity == PresetEntity.OriginalDir || !entity.existsIn(this.path)) continue;
            try {
                if (Files.isDirectory(this.path.resolve(entity.filename()), new LinkOption[0])) {
                    Files.walkFileTree(this.path.resolve(entity.filename()), new CopyDirVisitor(this.path.resolve(entity.filename()), path.resolve(entity.filename()), StandardCopyOption.REPLACE_EXISTING));
                    continue;
                }
                Files.copy(this.path.resolve(entity.filename()), path.resolve(entity.filename()), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                logger.warning("Unable to copy " + entity.filename() + " from " + this.path.toString() + " to " + path.toString());
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}


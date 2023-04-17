/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui.folders;

import com.wurmonline.server.gui.folders.DistEntity;
import com.wurmonline.server.gui.folders.Folder;
import com.wurmonline.server.gui.folders.PresetFolder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class DistFolder
extends Folder {
    private static final Logger logger = Logger.getLogger(PresetFolder.class.getName());

    public DistFolder(Path path) {
        super(path);
    }

    @Nullable
    public static DistFolder fromPath(Path path) {
        if (path == null) {
            return null;
        }
        if (!Files.isDirectory(path, new LinkOption[0])) {
            return null;
        }
        for (DistEntity entity : DistEntity.values()) {
            if (!entity.isRequired() || entity.existsIn(path)) continue;
            logger.warning("Dist folder missing " + entity.filename());
            return null;
        }
        return new DistFolder(path);
    }

    public final Path getPathFor(DistEntity entity) {
        return this.getPath().resolve(entity.filename());
    }
}


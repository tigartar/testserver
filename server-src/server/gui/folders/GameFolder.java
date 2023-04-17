/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui.folders;

import com.wurmonline.server.gui.folders.CopyDirVisitor;
import com.wurmonline.server.gui.folders.Folder;
import com.wurmonline.server.gui.folders.GameEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class GameFolder
extends Folder {
    private static final Logger logger = Logger.getLogger(GameFolder.class.getName());
    boolean current;

    public GameFolder(Path path, boolean current) {
        this(path);
        this.current = current;
    }

    public GameFolder(Path path) {
        super(path);
    }

    @Nullable
    public static GameFolder fromPath(Path path) {
        if (path == null) {
            return null;
        }
        if (!Files.isDirectory(path, new LinkOption[0])) {
            return null;
        }
        if (!GameEntity.GameDir.existsIn(path)) {
            return null;
        }
        return new GameFolder(path, GameEntity.CurrentDir.existsIn(path));
    }

    @Override
    public boolean create() {
        if (!super.create()) {
            return false;
        }
        if (GameEntity.GameDir.existsIn(this.path)) {
            return true;
        }
        try {
            GameEntity.GameDir.createIn(this.path);
        }
        catch (IOException ex) {
            logger.warning("Could not create gamedir in " + this.path.toString());
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public final String getError() {
        for (GameEntity entity : GameEntity.values()) {
            if (!entity.isRequired() || entity.existsIn(this)) continue;
            return "Game folder missing: " + entity.filename();
        }
        return "";
    }

    public final boolean isCurrent() {
        return this.current;
    }

    public final boolean setCurrent(boolean isCurrent) {
        try {
            this.current = isCurrent;
            if (isCurrent && !GameEntity.CurrentDir.existsIn(this)) {
                GameEntity.CurrentDir.createIn(this);
            } else if (!isCurrent && GameEntity.CurrentDir.existsIn(this)) {
                GameEntity.CurrentDir.deleteFrom(this);
            }
        }
        catch (IOException ex) {
            logger.warning("Unable to set current game folder");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean copyTo(Path path) {
        if (!Files.exists(path, new LinkOption[0]) || !this.exists()) {
            return false;
        }
        for (GameEntity entity : GameEntity.values()) {
            if (entity == GameEntity.CurrentDir || entity == GameEntity.GameDir || !entity.existsIn(this.path)) continue;
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

    public Path getPathFor(GameEntity entity) {
        return this.getPath().resolve(entity.filename());
    }
}


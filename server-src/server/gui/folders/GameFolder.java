package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class GameFolder extends Folder {
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
      } else if (!Files.isDirectory(path)) {
         return null;
      } else {
         return !GameEntity.GameDir.existsIn(path) ? null : new GameFolder(path, GameEntity.CurrentDir.existsIn(path));
      }
   }

   @Override
   public boolean create() {
      if (!super.create()) {
         return false;
      } else if (GameEntity.GameDir.existsIn(this.path)) {
         return true;
      } else {
         try {
            GameEntity.GameDir.createIn(this.path);
            return true;
         } catch (IOException var2) {
            logger.warning("Could not create gamedir in " + this.path.toString());
            var2.printStackTrace();
            return false;
         }
      }
   }

   public final String getError() {
      for(GameEntity entity : GameEntity.values()) {
         if (entity.isRequired() && !entity.existsIn(this)) {
            return "Game folder missing: " + entity.filename();
         }
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

         return true;
      } catch (IOException var3) {
         logger.warning("Unable to set current game folder");
         var3.printStackTrace();
         return false;
      }
   }

   public boolean copyTo(Path path) {
      if (Files.exists(path) && this.exists()) {
         for(GameEntity entity : GameEntity.values()) {
            if (entity != GameEntity.CurrentDir && entity != GameEntity.GameDir && entity.existsIn(this.path)) {
               try {
                  if (Files.isDirectory(this.path.resolve(entity.filename()))) {
                     Files.walkFileTree(
                        this.path.resolve(entity.filename()),
                        new CopyDirVisitor(this.path.resolve(entity.filename()), path.resolve(entity.filename()), StandardCopyOption.REPLACE_EXISTING)
                     );
                  } else {
                     Files.copy(this.path.resolve(entity.filename()), path.resolve(entity.filename()), StandardCopyOption.REPLACE_EXISTING);
                  }
               } catch (IOException var7) {
                  logger.warning("Unable to copy " + entity.filename() + " from " + this.path.toString() + " to " + path.toString());
                  var7.printStackTrace();
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public Path getPathFor(GameEntity entity) {
      return this.getPath().resolve(entity.filename());
   }
}

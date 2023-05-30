package com.wurmonline.server.gui.folders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class DistFolder extends Folder {
   private static final Logger logger = Logger.getLogger(PresetFolder.class.getName());

   public DistFolder(Path path) {
      super(path);
   }

   @Nullable
   public static DistFolder fromPath(Path path) {
      if (path == null) {
         return null;
      } else if (!Files.isDirectory(path)) {
         return null;
      } else {
         for(DistEntity entity : DistEntity.values()) {
            if (entity.isRequired() && !entity.existsIn(path)) {
               logger.warning("Dist folder missing " + entity.filename());
               return null;
            }
         }

         return new DistFolder(path);
      }
   }

   public final Path getPathFor(DistEntity entity) {
      return this.getPath().resolve(entity.filename());
   }
}

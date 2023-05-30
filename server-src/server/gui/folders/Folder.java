package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;

public class Folder {
   private static final Logger logger = Logger.getLogger(Folder.class.getName());
   Path path;
   String name;

   public Folder(Path path) {
      this.path = path;
      this.name = this.path.getName(path.getNameCount() - 1).toString();
   }

   public final Path getPath() {
      return this.path;
   }

   public final String getName() {
      return this.name;
   }

   public boolean isEmpty() throws IOException {
      boolean var3;
      try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.path)) {
         var3 = !dirStream.iterator().hasNext();
      }

      return var3;
   }

   public boolean create() {
      if (this.exists()) {
         return true;
      } else {
         try {
            Files.createDirectory(this.path);
            return true;
         } catch (IOException var2) {
            logger.warning("Exception creating " + this.path.toString());
            var2.printStackTrace();
            return false;
         }
      }
   }

   public boolean exists() {
      return Files.exists(this.path);
   }

   public boolean delete() {
      if (!this.exists()) {
         return true;
      } else {
         try {
            Files.walk(this.path).sorted(Comparator.reverseOrder()).forEach(path -> {
               try {
                  Files.delete(path);
               } catch (IOException var3) {
                  logger.warning("Exception deleting " + this.path.toString());
                  var3.printStackTrace();
               }
            });
            return true;
         } catch (IOException var2) {
            logger.warning("Exception deleting " + this.path.toString());
            var2.printStackTrace();
            return false;
         }
      }
   }

   public void move(Path path) throws IOException {
      Files.move(this.path, path);
   }
}

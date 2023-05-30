package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FolderEntity {
   boolean required;
   String name;

   FolderEntity(String name, boolean required) {
      this.required = required;
      this.name = name;
   }

   @Override
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
      return Files.exists(path.resolve(this.getFilename()));
   }

   public void createIn(Path path) throws IOException {
      Files.createFile(path.resolve(this.getFilename()));
   }

   public void deleteFrom(Path path) throws IOException {
      Files.delete(path.resolve(this.getFilename()));
   }
}

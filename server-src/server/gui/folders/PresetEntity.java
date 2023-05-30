package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.Path;

public enum PresetEntity {
   WurmINI("wurm.ini", true),
   Sqlite("sqlite", true),
   OriginalDir("originaldir", false),
   TopLayer("top_layer.map", true),
   RockLayer("rock_layer.map", true),
   Flags("flags.map", false),
   Cave("map_cave.map", false),
   Resources("resources.map", false),
   ProtectedTiles("protectedTiles.bmap", false);

   private FolderEntity entity;

   private PresetEntity(String name, boolean required) {
      this.entity = new FolderEntity(name, required);
   }

   @Override
   public String toString() {
      return this.entity.toString();
   }

   public String filename() {
      return this.entity.getFilename();
   }

   public boolean isRequired() {
      return this.entity.isRequired();
   }

   public boolean existsIn(PresetFolder folder) {
      return this.entity.existsIn(folder.getPath());
   }

   public boolean existsIn(Path path) {
      return this.entity.existsIn(path);
   }

   public void createIn(PresetFolder folder) throws IOException {
      this.entity.createIn(folder.getPath());
   }

   public void createIn(Path path) throws IOException {
      this.entity.createIn(path);
   }

   public void deleteFrom(PresetFolder folder) throws IOException {
      this.entity.deleteFrom(folder.getPath());
   }

   public void deleteFrom(Path path) throws IOException {
      this.entity.deleteFrom(path);
   }
}

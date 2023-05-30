package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.Path;

public enum GameEntity {
   CurrentDir("currentdir", false),
   GameDir("gamedir", false),
   WurmINI("wurm.ini", true),
   Sqlite("sqlite", true),
   TopLayer("top_layer.map", true),
   RockLayer("rock_layer.map", true),
   Flags("flags.map", false),
   Cave("map_cave.map", false),
   Resources("resources.map", false),
   Recipes("recipes", false),
   ProtectedTiles("protectedTiles.bmap", false);

   private FolderEntity entity;

   private GameEntity(String name, boolean required) {
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

   public boolean existsIn(GameFolder gameFolder) {
      return this.entity.existsIn(gameFolder.getPath());
   }

   public boolean existsIn(Path path) {
      return this.entity.existsIn(path);
   }

   public void createIn(GameFolder gameFolder) throws IOException {
      this.entity.createIn(gameFolder.getPath());
   }

   public void createIn(Path path) throws IOException {
      this.entity.createIn(path);
   }

   public void deleteFrom(GameFolder gameFolder) throws IOException {
      this.entity.deleteFrom(gameFolder.getPath());
   }
}

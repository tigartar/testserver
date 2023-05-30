package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public enum Folders {
   INSTANCE;

   private static final Logger logger = Logger.getLogger(Folders.class.getName());
   private HashMap<String, GameFolder> gameFolders = new HashMap<>();
   private HashMap<String, PresetFolder> presets = new HashMap<>();
   private GameFolder current;
   private DistFolder dist;
   private Path distPath = Paths.get(System.getProperty("wurm.distRoot", "./dist"));
   private Path gamesPath = Paths.get(System.getProperty("wurm.gameFolderRoot", "."));
   private Path presetsPath = Paths.get(System.getProperty("wurm.presetsRoot", "./presets"));

   public static Folders getInstance() {
      return INSTANCE;
   }

   public static ArrayList<GameFolder> getGameFolders() {
      ArrayList<GameFolder> gameFolders = new ArrayList<>();
      gameFolders.addAll(getInstance().gameFolders.values());
      return gameFolders;
   }

   @Nullable
   public static GameFolder getGameFolder(String folderName) {
      return getInstance().gameFolders.get(folderName);
   }

   public static boolean setCurrent(GameFolder gameFolder) {
      if (getInstance().current != null && !getInstance().current.setCurrent(false)) {
         return false;
      } else {
         getInstance().current = gameFolder;
         if (!gameFolder.setCurrent(true)) {
            return false;
         } else {
            logger.info("Current game folder: " + gameFolder.getName());
            return true;
         }
      }
   }

   public static void clear() {
      getInstance().gameFolders.clear();
      getInstance().current = null;
      logger.info("Game folders cleared.");
   }

   public static GameFolder getCurrent() {
      return getInstance().current;
   }

   public static boolean loadGames() {
      return loadGamesFrom(getInstance().gamesPath);
   }

   public static boolean loadGamesFrom(Path parent) {
      if (!getInstance().gameFolders.isEmpty()) {
         getInstance().gameFolders = new HashMap<>();
      }

      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent)) {
         for(Path path : directoryStream) {
            GameFolder gameFolder = GameFolder.fromPath(path);
            if (gameFolder != null) {
               getInstance().gameFolders.put(gameFolder.getName(), gameFolder);
               if (gameFolder.isCurrent()) {
                  if (getInstance().current == null) {
                     getInstance().current = gameFolder;
                  } else if (!gameFolder.setCurrent(false)) {
                     return false;
                  }
               }
            }
         }

         return true;
      } catch (IOException var19) {
         logger.warning("IOException while reading game folders");
         var19.printStackTrace();
         return false;
      }
   }

   public static boolean loadDist() {
      getInstance().dist = DistFolder.fromPath(getInstance().distPath);
      return getInstance().dist != null;
   }

   public static DistFolder getDist() {
      if (getInstance().dist == null && !loadDist()) {
         logger.warning("Unable to load 'dist' folder, please run Steam validation");
         return new DistFolder(getInstance().distPath);
      } else {
         return getInstance().dist;
      }
   }

   public static boolean loadPresets() {
      if (!getInstance().presets.isEmpty()) {
         getInstance().presets = new HashMap<>();
      }

      if (getInstance().dist == null && !loadDist()) {
         logger.warning("Unable to load 'dist' folder, please run Steam validation");
         return false;
      } else if (!loadPresetsFrom(getInstance().dist.getPath())) {
         logger.warning("Unable to load presets from 'dist', please run Steam validation");
         return false;
      } else {
         if (!Files.exists(getInstance().presetsPath)) {
            try {
               Files.createDirectory(getInstance().presetsPath);
            } catch (IOException var1) {
               logger.warning("Could not create presets folder");
               return false;
            }
         }

         return loadPresetsFrom(getInstance().presetsPath);
      }
   }

   private static boolean loadPresetsFrom(Path parent) {
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent)) {
         for(Path path : directoryStream) {
            PresetFolder folder = PresetFolder.fromPath(path);
            if (folder != null) {
               getInstance().presets.put(folder.getName(), folder);
            }
         }

         return true;
      } catch (IOException var16) {
         logger.warning("IOException while reading game folders");
         var16.printStackTrace();
         return false;
      }
   }

   public static Path getGamesPath() {
      return getInstance().gamesPath;
   }

   public static void addGame(GameFolder folder) {
      getInstance().gameFolders.put(folder.getName(), folder);
   }

   public static void removeGame(GameFolder folder) {
      getInstance().gameFolders.remove(folder.getName());
   }
}

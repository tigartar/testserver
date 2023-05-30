package com.wurmonline.server.filesystems;

import com.wurmonline.server.Constants;
import com.wurmonline.server.ServerDirInfo;

public final class FileSystems {
   public static final AlphabeticalFileSystem creatureTemplates = new AlphabeticalFileSystem(ServerDirInfo.getFileDBPath() + Constants.creatureTemplatesDBPath);
   public static final AlphabeticalFileSystem skillTemplates = new AlphabeticalFileSystem(ServerDirInfo.getFileDBPath() + Constants.skillTemplatesDBPath);
   public static final AlphabeticalFileSystem itemTemplates = new AlphabeticalFileSystem(ServerDirInfo.getFileDBPath() + Constants.itemTemplatesDBPath);
   public static final AlphaCreationalFileSystem playerStats = new AlphaCreationalFileSystem(ServerDirInfo.getFileDBPath() + Constants.playerStatsDBPath);
   public static final MajorFileSystem creatureStats = new MajorFileSystem(ServerDirInfo.getFileDBPath() + Constants.creatureStatsDBPath);
   public static final MajorFileSystem itemStats = new MajorFileSystem(ServerDirInfo.getFileDBPath() + Constants.itemStatsDBPath);
   public static final MajorFileSystem zoneStats = new MajorFileSystem(ServerDirInfo.getFileDBPath() + Constants.zonesDBPath);
   public static final MajorFileSystem itemOldStats = new MajorFileSystem(ServerDirInfo.getFileDBPath() + Constants.itemOldStatsDBPath);
   public static final MajorFileSystem creatureOldStats = new MajorFileSystem(ServerDirInfo.getFileDBPath() + Constants.creatureOldStatsDBPath);
   public static final MajorFileSystem tileStats = new MajorFileSystem(ServerDirInfo.getFileDBPath() + Constants.tileStatsDBPath);

   private FileSystems() {
   }

   public static String getCreatureTemplateDirFor(String fileName) {
      return creatureTemplates.getDir(fileName);
   }

   public static String getSkillTemplateDirFor(String fileName) {
      return skillTemplates.getDir(fileName);
   }

   public static String getItemTemplateDirFor(String fileName) {
      return itemTemplates.getDir(fileName);
   }

   public static String getCreatureStateDirFor(String fileName) {
      return creatureStats.getDir(fileName);
   }

   public static String getPlayerStateDirFor(String fileName) {
      return playerStats.getDir(fileName);
   }

   public static String getItemStateDirFor(String fileName) {
      return itemStats.getDir(fileName);
   }

   public static String getItemOldStateDirFor(String fileName) {
      return itemOldStats.getDir(fileName);
   }

   public static String getTileStateDirFor(String fileName) {
      return tileStats.getDir(fileName);
   }

   public static String getZoneStateDirFor(String fileName) {
      return zoneStats.getDir(fileName);
   }
}

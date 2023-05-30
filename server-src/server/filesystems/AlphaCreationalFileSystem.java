package com.wurmonline.server.filesystems;

import java.io.File;

public class AlphaCreationalFileSystem extends AlphabeticalFileSystem {
   public AlphaCreationalFileSystem(String aRootDir) {
      super(aRootDir);
   }

   @Override
   public String getDir(String fileName) {
      String dirName = fileName;
      if (fileName.indexOf(".") > 0) {
         dirName = fileName.substring(0, fileName.indexOf("."));
      }

      String firstLetter = fileName.substring(0, 1);
      String secondLetters = fileName.substring(0, 2);
      String dir1 = firstLetter.toLowerCase();
      String dir2 = secondLetters.toLowerCase();
      String fileDir = this.rootDir + File.separator + dir1 + File.separator + dir2 + File.separator + dirName + File.separator;
      File saveDir = new File(fileDir);
      if (!saveDir.exists()) {
         saveDir.mkdirs();
      }

      return fileDir;
   }
}

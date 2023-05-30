package com.wurmonline.server.gui.folders;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyDirVisitor extends SimpleFileVisitor<Path> {
   private final Path fromPath;
   private final Path toPath;
   private final CopyOption copyOption;

   public CopyDirVisitor(Path fromPath, Path toPath, CopyOption copyOption) {
      this.fromPath = fromPath;
      this.toPath = toPath;
      this.copyOption = copyOption;
   }

   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      Path target = this.toPath.resolve(this.fromPath.relativize(dir));
      if (!Files.exists(target)) {
         Files.createDirectory(target);
      }

      return FileVisitResult.CONTINUE;
   }

   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Files.copy(file, this.toPath.resolve(this.fromPath.relativize(file)), this.copyOption);
      return FileVisitResult.CONTINUE;
   }
}

package com.wurmonline.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties {
   private final Properties properties = new Properties();

   private BuildProperties() {
   }

   public static BuildProperties getPropertiesFor(String path) throws IOException {
      BuildProperties bp = new BuildProperties();

      try (InputStream inputStream = BuildProperties.class.getResourceAsStream(path)) {
         bp.properties.load(inputStream);
      }

      return bp;
   }

   public String getGitSha1Short() {
      String sha = this.getGitSha1();
      return sha.length() < 7 ? sha : sha.substring(0, 7);
   }

   public String getGitBranch() {
      return this.properties.getProperty("git-branch");
   }

   public String getGitSha1() {
      return this.properties.getProperty("git-sha-1");
   }

   public String getVersion() {
      return this.properties.getProperty("version");
   }

   public String getBuildTimeString() {
      return this.properties.getProperty("build-time");
   }
}

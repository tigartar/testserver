package com.wurmonline.website;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

public abstract class Section {
   public abstract List<Block> getBlocks(HttpServletRequest var1, LoginInfo var2);

   public abstract String getId();

   public abstract String getName();

   public void handlePost(HttpServletRequest req, LoginInfo loginInfo) {
   }
}

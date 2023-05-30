package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;

public final class WurmPermissions implements MiscConstants {
   private WurmPermissions() {
   }

   public static boolean mayCreateItems(Creature performer) {
      return Servers.isThisATestServer() || performer.getPower() >= 2 || Players.isArtist(performer.getWurmId(), false, false);
   }

   public static boolean mayChangeTile(Creature performer) {
      return performer.getPower() >= 3 || Players.isArtist(performer.getWurmId(), false, false);
   }

   public static boolean mayUseDeityWand(Creature performer) {
      return performer.getPower() >= 2 || Players.isArtist(performer.getWurmId(), false, false);
   }

   public static boolean mayUseGMWand(Creature performer) {
      return performer.getPower() >= 2 || Players.isArtist(performer.getWurmId(), false, false);
   }

   public static boolean maySetFaith(Creature performer) {
      return performer.getPower() >= 3 || Servers.isThisATestServer() && Players.isArtist(performer.getWurmId(), false, false);
   }
}

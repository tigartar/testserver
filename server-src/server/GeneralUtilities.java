package com.wurmonline.server;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;
import java.util.SimpleTimeZone;

public final class GeneralUtilities implements MiscConstants {
   private GeneralUtilities() {
   }

   public static boolean isValidTileLocation(int tilex, int tiley) {
      return tilex >= 0 && tilex < 1 << Constants.meshSize && tiley >= 0 && tiley < 1 << Constants.meshSize;
   }

   public static float calcOreRareQuality(double power, int actionBonus, int toolBonus) {
      return calcRareQuality(power, actionBonus, toolBonus, 0, 2, 108.428F);
   }

   public static float calcRareQuality(double power, int actionBonus, int toolBonus) {
      return calcRareQuality(power, actionBonus, toolBonus, 0, 2, 100.0F);
   }

   public static float calcRareQuality(double power, int actionBonus, int toolBonus, int targetBonus) {
      return calcRareQuality(power, actionBonus, toolBonus, targetBonus, 3, 100.0F);
   }

   public static float calcRareQuality(double power, int actionBonus, int toolBonus, int targetBonus, int numbBonus, float fiddleFactor) {
      float rPower = (float)power;
      int totalBonus = toolBonus + targetBonus + actionBonus;
      float bonus = 0.0F;
      if (totalBonus > 0) {
         float val = fiddleFactor - rPower;
         float square = val * val;
         float n = square / 1000.0F;
         float mod = Math.min(n * 1.25F, 1.0F);
         bonus = (float)totalBonus * 3.0F / (float)numbBonus * mod;
      }

      return Math.max(Math.min(99.999F, rPower + bonus), 1.0F);
   }

   public static final Map<String, Map<CreationEntry, Integer>> getCreationList(Item source, Item target, Player player) {
      CreationEntry[] entries = CreationMatrix.getInstance().getCreationOptionsFor(source, target);
      return ItemBehaviour.generateMapfromOptions(player, source, target, entries);
   }

   public static String toGMTString(long aDate) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
      sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
      return sdf.format(new Date(aDate));
   }

   public static void setSettingsBits(BitSet bits, int value) {
      for(int x = 0; x < 32; ++x) {
         bits.set(x, (value & 1) == 1);
      }
   }

   public static int getIntSettingsFrom(BitSet bits) {
      int ret = 0;

      for(int x = 0; x < 32; ++x) {
         if (bits.get(x)) {
            ret = (int)((long)ret + (1L << x));
         }
      }

      return ret;
   }

   public static boolean isOnSameLevel(Creature creature1, Creature creature2) {
      float difference = Math.abs(creature1.getStatus().getPositionZ() - creature2.getStatus().getPositionZ()) * 10.0F;
      return difference < 30.0F;
   }

   public static boolean mayAttackSameLevel(Creature creature1, Creature creature2) {
      float difference = Math.abs(creature1.getStatus().getPositionZ() - creature2.getStatus().getPositionZ()) * 10.0F;
      return difference < 29.7F;
   }

   public static boolean isOnSameLevel(Creature creature, Item item) {
      float pz = creature.getStatus().getPositionZ();
      if (creature.getVehicle() != -10L) {
         Vehicle vehicle = Vehicles.getVehicleForId(creature.getVehicle());
         if (vehicle != null) {
            pz = vehicle.getPosZ();
         }
      }

      float difference = Math.abs(Math.max(0.0F, pz) - Math.max(0.0F, item.getPosZ())) * 10.0F;
      return difference < 30.0F;
   }

   public static short getHeight(int tilex, int tiley, boolean onSurface) {
      return onSurface ? Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)) : Tiles.decodeHeight(Server.caveMesh.getTile(tilex, tiley));
   }
}

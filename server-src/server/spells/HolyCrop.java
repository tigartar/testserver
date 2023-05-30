package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class HolyCrop extends ReligiousSpell {
   public static final int RANGE = 4;

   HolyCrop() {
      super("Holy Crop", 400, 100, 300, 60, 50, 7200000L);
      this.isRitual = true;
      this.targetItem = true;
      this.description = "crop and animal blessings";
      this.type = 0;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      if (performer.getDeity() != null) {
         Deity deity = performer.getDeity();
         Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
         if (templateDeity.getFavor() < 100000 && !Servers.isThisATestServer()) {
            performer.getCommunicator().sendNormalServerMessage(deity.getName() + " can not grant that power right now.", (byte)3);
            return false;
         }

         if (target.getBless() == deity && target.isDomainItem()) {
            return true;
         }

         performer.getCommunicator().sendNormalServerMessage(String.format("You need to cast this spell at an altar of %s.", deity.getName()), (byte)3);
      }

      return false;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      Deity deity = performer.getDeity();
      Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
      performer.getCommunicator()
         .sendNormalServerMessage(performer.getDeity().getName() + " graces the lands with abundant crop yield and happy animals!", (byte)2);
      Server.getInstance().broadCastSafe("As the Holy Crop ritual is completed, followers of " + deity.getName() + " may now receive a blessing!");
      HistoryManager.addHistory(
         performer.getName(), "casts " + this.name + ". " + performer.getDeity().getName() + " graces the lands with abundant crop yield and happy animals."
      );
      templateDeity.setFavor(templateDeity.getFavor() - 100000);
      performer.achievement(635);

      for(Creature c : performer.getLinks()) {
         c.achievement(635);
      }

      new RiteEvent.RiteOfCropEvent(-10, performer.getWurmId(), this.getNumber(), deity.getNumber(), System.currentTimeMillis(), 86400000L);
      int pow = 100 + Math.max(20, (int)power * 3);
      if (Features.Feature.NEWDOMAINS.isEnabled()) {
         for(FaithZone f : Zones.getFaithZones()) {
            if (f != null && f.getCurrentRuler().getTemplateDeity() == deity.getTemplateDeity()) {
               try {
                  if (Zones.getFaithZone(f.getCenterX(), f.getCenterY(), true) != f) {
                     continue;
                  }
               } catch (NoSuchZoneException var15) {
                  continue;
               }

               for(int tx = f.getStartX(); tx < f.getEndX(); ++tx) {
                  for(int ty = f.getStartY(); ty < f.getEndY(); ++ty) {
                     this.effectTile(tx, ty, pow);
                  }
               }
            }
         }
      } else {
         FaithZone[][] surfaceZones = Zones.getFaithZones(true);

         for(int x = 0; x < Zones.faithSizeX; ++x) {
            for(int y = 0; y < Zones.faithSizeY; ++y) {
               if (surfaceZones[x][y].getCurrentRuler().getTemplateDeity() == deity.getTemplateDeity()) {
                  for(int tx = surfaceZones[x][y].getStartX(); tx < surfaceZones[x][y].getEndX(); ++tx) {
                     for(int ty = surfaceZones[x][y].getStartY(); ty < surfaceZones[x][y].getEndY(); ++ty) {
                        this.effectTile(tx, ty, pow);
                     }
                  }
               }
            }
         }
      }
   }

   private void effectTile(int tx, int ty, int pow) {
      int tile = Server.surfaceMesh.getTile(tx, ty);
      if (Tiles.decodeType(tile) == Tiles.Tile.TILE_FIELD.id) {
         int worldResource = Server.getWorldResource(tx, ty);
         int farmedCount = worldResource >>> 11;
         int farmedChance = worldResource & 2047;
         if (farmedCount < 5) {
            ++farmedCount;
         }

         farmedChance = Math.min(farmedChance + pow, 2047);
         Server.setWorldResource(tx, ty, (farmedCount << 11) + farmedChance);
      }

      VolaTile t = Zones.getTileOrNull(tx, ty, true);
      if (t != null) {
         Creature[] crets = t.getCreatures();

         for(Creature lCret : crets) {
            if (lCret.getLoyalty() > 0.0F) {
               lCret.setLoyalty(99.0F);
               lCret.getStatus().modifyHunger(0, 80.0F);
            } else if (lCret.isDomestic()) {
               lCret.getStatus().modifyHunger(0, 80.0F);
            }

            lCret.removeRandomNegativeTrait();
         }
      }
   }
}

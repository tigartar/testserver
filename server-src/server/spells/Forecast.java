package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;

public class Forecast extends KarmaSpell {
   public static final int RANGE = 24;

   public Forecast() {
      super("Forecast", 560, 60, 300, 30, 1, 180000L);
      this.offensive = true;
      this.targetCreature = true;
      this.targetTile = true;
      this.description = "predicts the future for an area by placing deadly traps around it";
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      int villid = 0;
      if (performer.getCitizenVillage() != null) {
         villid = performer.getCitizenVillage().id;
      }

      int layer = target.getLayer();

      for(int x = Zones.safeTileX(target.getTileX() - 5); x < Zones.safeTileX(target.getTileX() + 5); ++x) {
         int y = Zones.safeTileY(target.getTileY() - 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      for(int x = Zones.safeTileX(target.getTileX() - 5); x < Zones.safeTileX(target.getTileX() + 5); ++x) {
         int y = Zones.safeTileY(target.getTileY() + 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      for(int y = Zones.safeTileY(target.getTileY() - 5); y < Zones.safeTileY(target.getTileY() + 5); ++y) {
         int x = Zones.safeTileX(target.getTileX() - 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      for(int y = Zones.safeTileY(target.getTileY() - 5); y < Zones.safeTileY(target.getTileY() + 5); ++y) {
         int x = Zones.safeTileX(target.getTileX() + 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      performer.getCommunicator()
         .sendNormalServerMessage(
            "You predict a grim future for " + target.getNameWithGenus() + " by placing deadly traps around " + target.getHimHerItString() + "."
         );
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      int villid = 0;
      if (performer.getCitizenVillage() != null) {
         villid = performer.getCitizenVillage().id;
      }

      for(int x = Zones.safeTileX(tilex - 5); x < Zones.safeTileX(tilex + 5); ++x) {
         int y = Zones.safeTileY(tiley - 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      for(int x = Zones.safeTileX(tilex - 5); x < Zones.safeTileX(tilex + 5); ++x) {
         int y = Zones.safeTileY(tiley + 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      for(int y = Zones.safeTileY(tiley - 4); y < Zones.safeTileY(tiley + 4); ++y) {
         int x = Zones.safeTileX(tilex - 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      for(int y = Zones.safeTileY(tiley - 4); y < Zones.safeTileY(tiley + 4); ++y) {
         int x = Zones.safeTileX(tilex + 5);
         this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
      }

      performer.getCommunicator().sendNormalServerMessage("You predict a grim future for enemies near the area by placing deadly traps around it.");
   }

   private final void createTrapAt(int x, int y, int layer, int villid, byte creatorKingdom) {
      boolean ok = true;
      if (layer < 0) {
         int t = Server.caveMesh.getTile(x, y);
         if (Tiles.isSolidCave(Tiles.decodeType(t))) {
            ok = false;
         }

         if (Tiles.decodeHeight(t) < 0) {
            ok = false;
         }
      } else {
         int t = Server.surfaceMesh.getTile(x, y);
         if (Tiles.decodeHeight(t) < 0) {
            ok = false;
         }
      }

      if (ok) {
         try {
            VolaTile ttile = Zones.getOrCreateTile(x, y, layer >= 0);
            if (ttile != null) {
               int fl = ttile.getDropFloorLevel(layer * 30);
               layer = fl / 30;
               ttile.sendAddQuickTileEffect((byte)71, layer * 30);
            }

            Trap t = new Trap((byte)10, (byte)99, creatorKingdom, villid, Trap.createId(x, y, layer), (byte)100, (byte)100, (byte)100);
            t.create();
         } catch (IOException var9) {
            return;
         }
      }
   }
}

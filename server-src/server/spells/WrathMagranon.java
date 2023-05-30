package com.wurmonline.server.spells;

import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.util.ArrayList;

public class WrathMagranon extends DamageSpell implements AttitudeConstants {
   public static final int RANGE = 4;
   public static final double BASE_DAMAGE = 3000.0;
   public static final double DAMAGE_PER_POWER = 60.0;
   public static final float BASE_STRUCTURE_DAMAGE = 7.5F;
   public static final float STRUCTURE_DAMAGE_PER_POWER = 0.15F;
   public static final int RADIUS = 1;

   public WrathMagranon() {
      super("Wrath of Magranon", 441, 10, 50, 50, 50, 300000L);
      this.targetTile = true;
      this.offensive = true;
      this.description = "covers an area with exploding power, damaging enemies and walls";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      performer.getCommunicator().sendNormalServerMessage("You slam down the fist of Magranon, which crushes enemy structures in the area!");
      int radiusBonus = (int)(power / 80.0);
      int sx = Zones.safeTileX(tilex - 1 - radiusBonus - performer.getNumLinks());
      int sy = Zones.safeTileY(tiley - 1 - radiusBonus - performer.getNumLinks());
      int ex = Zones.safeTileX(tilex + 1 + radiusBonus + performer.getNumLinks());
      int ey = Zones.safeTileY(tiley + 1 + radiusBonus + performer.getNumLinks());
      float structureDamage = 7.5F + (float)power * 0.15F;
      ArrayList<Fence> damagedFences = new ArrayList<>();

      for(int x = sx; x <= ex; ++x) {
         for(int y = sy; y <= ey; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, layer == 0);
            if (t != null) {
               Item ring = Zones.isWithinDuelRing(x, y, layer >= 0);
               if (ring == null) {
                  for(Creature lCret : t.getCreatures()) {
                     if (!lCret.isInvulnerable() && lCret.getAttitude(performer) == 2) {
                        lCret.addAttacker(performer);
                        double damage = this.calculateDamage(lCret, power, 3000.0, 60.0);
                        lCret.addWoundOfType(performer, (byte)0, 1, true, 1.0F, false, damage, (float)power / 5.0F, 0.0F, false, true);
                     }
                  }

                  if (Servers.isThisAPvpServer()) {
                     for(Wall wall : t.getWalls()) {
                        if (wall.getType() != StructureTypeEnum.PLAN) {
                           boolean dealDam = true;

                           Structure structure;
                           try {
                              structure = Structures.getStructure(wall.getStructureId());
                           } catch (NoSuchStructureException var31) {
                              continue;
                           }

                           int tx = wall.getTileX();
                           int ty = wall.getTileY();
                           Village v = Zones.getVillage(tx, ty, performer.isOnSurface());
                           if (v != null && !v.isEnemy(performer) && !MethodsStructure.mayModifyStructure(performer, structure, wall.getTile(), (short)82)) {
                              dealDam = false;
                           }

                           if (dealDam) {
                              float wallql = wall.getCurrentQualityLevel();
                              float damageToDeal = structureDamage * ((150.0F - wallql) / 100.0F);
                              wall.setDamage(wall.getDamage() + damageToDeal);
                           }
                        }
                     }
                  }

                  for(Fence fence : t.getAllFences()) {
                     if (fence.isFinished() && !damagedFences.contains(fence)) {
                        boolean dealDam = true;
                        Village vill = MethodsStructure.getVillageForFence(fence);
                        if (vill != null && !vill.isEnemy(performer)) {
                           dealDam = false;
                        }

                        float mult = 1.0F;
                        if (performer.getCultist() != null && performer.getCultist().doubleStructDamage()) {
                           mult *= 2.0F;
                        }

                        if (dealDam) {
                           float fenceql = fence.getCurrentQualityLevel();
                           float damageToDeal = structureDamage * ((150.0F - fenceql) / 100.0F);
                           fence.setDamage(fence.getDamage() + damageToDeal * mult);
                           damagedFences.add(fence);
                        }
                     }
                  }
               }
            }
         }
      }

      VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
      if (t != null && layer == 0) {
         Zones.flash(tilex, tiley, false);
      }
   }
}

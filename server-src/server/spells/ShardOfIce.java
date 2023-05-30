package com.wurmonline.server.spells;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShardOfIce extends DamageSpell implements AttitudeConstants {
   private static Logger logger = Logger.getLogger(FireHeart.class.getName());
   public static final int RANGE = 50;
   public static final double BASE_DAMAGE = 5000.0;
   public static final double DAMAGE_PER_POWER = 120.0;

   public ShardOfIce() {
      super("Shard of Ice", 485, 7, 20, 30, 35, 30000L);
      this.targetCreature = true;
      this.offensive = true;
      this.description = "damages the targets body with a spear of ice causing frost damage";
      this.type = 2;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && performer.faithful && !performer.isDuelOrSpar(target)) {
         performer.getCommunicator()
            .sendNormalServerMessage(performer.getDeity().getName() + " would never accept your attack on " + target.getName() + ".", (byte)3);
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.isDuelOrSpar(target)) {
         performer.modifyFaith(-(100.0F - performer.getFaith()) / 50.0F);
      }

      try {
         VolaTile t = performer.getCurrentTile();
         long shardId = WurmId.getNextTempItemId();
         if (t != null) {
            t.sendProjectile(
               shardId,
               (byte)4,
               "model.spell.ShardOfIce",
               "Shard Of Ice",
               (byte)0,
               performer.getPosX(),
               performer.getPosY(),
               performer.getPositionZ() + performer.getAltOffZ(),
               performer.getStatus().getRotation(),
               (byte)performer.getLayer(),
               (float)((int)target.getPosX()),
               (float)((int)target.getPosY()),
               target.getPositionZ() + target.getAltOffZ(),
               performer.getWurmId(),
               target.getWurmId(),
               0.0F,
               0.0F
            );
         }

         t = target.getCurrentTile();
         if (t != null) {
            t.sendProjectile(
               shardId,
               (byte)4,
               "model.spell.ShardOfIce",
               "Shard Of Ice",
               (byte)0,
               performer.getPosX(),
               performer.getPosY(),
               performer.getPositionZ() + performer.getAltOffZ(),
               performer.getStatus().getRotation(),
               (byte)performer.getLayer(),
               (float)((int)target.getPosX()),
               (float)((int)target.getPosY()),
               target.getPositionZ() + target.getAltOffZ(),
               performer.getWurmId(),
               target.getWurmId(),
               0.0F,
               0.0F
            );
         }

         byte pos = target.getBody().getCenterWoundPos();
         double damage = this.calculateDamage(target, power, 5000.0, 120.0);
         target.addWoundOfType(performer, (byte)8, pos, false, 1.0F, false, damage, 0.0F, 0.0F, false, true);
      } catch (Exception var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
      }
   }
}

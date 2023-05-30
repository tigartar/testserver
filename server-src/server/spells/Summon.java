package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Summon extends KarmaSpell implements CreatureTemplateIds {
   private static final Logger logger = Logger.getLogger(Summon.class.getName());
   public static final int RANGE = 24;

   public Summon() {
      super("Summon", 559, 60, 500, 30, 1, 180000L);
      this.targetTile = true;
      this.description = "summons a creature to your aid";
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
      if (performer.knowsKarmaSpell(630)
         && (layer >= 0 || performer.getLayer() >= 0)
         && !WurmCalendar.isNight()
         && !performer.knowsKarmaSpell(629)
         && !performer.knowsKarmaSpell(631)) {
         performer.getCommunicator().sendNormalServerMessage("You cannot summon this above ground during daytime.", (byte)3);
         return false;
      } else if (performer.getFollowers().length > 0) {
         performer.getCommunicator().sendNormalServerMessage("You are too busy leading other creatures and can not focus on summoning.", (byte)3);
         return false;
      } else if (layer < 0 && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)))) {
         performer.getCommunicator().sendNormalServerMessage("You can not summon there.", (byte)3);
         return false;
      } else {
         try {
            if (Zones.calculateHeight((float)((tilex << 2) + 2), (float)((tiley << 2) + 2), performer.isOnSurface()) < 0.0F) {
               performer.getCommunicator().sendNormalServerMessage("You can not summon there.", (byte)3);
               return false;
            } else {
               return true;
            }
         } catch (Exception var7) {
            performer.getCommunicator().sendNormalServerMessage("You can not summon there.", (byte)3);
            return false;
         }
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      try {
         if (performer.knowsKarmaSpell(629)) {
            this.spawnCreature(86, performer);
         }

         if (performer.knowsKarmaSpell(631)) {
            for(int nums = 0; (double)nums < Math.max(2.0, power / 10.0); ++nums) {
               this.spawnCreature(87, performer);
            }
         }

         if (performer.knowsKarmaSpell(630) && (WurmCalendar.isNight() || layer < 0 || performer.getLayer() < 0)) {
            this.spawnCreature(88, performer);
         }
      } catch (Exception var10) {
         logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
      }
   }

   private final void spawnCreature(int templateId, Creature performer) {
      try {
         CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(templateId);
         byte sex = 0;
         if (Server.rand.nextInt(2) == 0) {
            sex = 1;
         }

         byte ctype = (byte)Math.max(0, Server.rand.nextInt(22) - 10);
         if (Server.rand.nextInt(20) == 0) {
            ctype = 99;
         }

         Creature var6 = Creature.doNew(
            templateId,
            true,
            performer.getPosX() - 4.0F + Server.rand.nextFloat() * 9.0F,
            performer.getPosY() - 4.0F + Server.rand.nextFloat() * 9.0F,
            Server.rand.nextFloat() * 360.0F,
            performer.getLayer(),
            ct.getName(),
            sex,
            performer.getKingdomId(),
            ctype,
            true
         );
      } catch (NoSuchCreatureTemplateException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } catch (Exception var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      }
   }
}

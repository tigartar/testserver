package com.wurmonline.server.skills;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TempSkill extends Skill {
   private static Logger logger = Logger.getLogger(TempSkill.class.getName());

   public TempSkill(int aNumber, double aStartValue, Skills aParent) {
      super(aNumber, aStartValue, aParent);
   }

   public TempSkill(long aId, Skills aParent, int aNumber, double aKnowledge, double aMinimum, long aLastused) {
      super(aId, aParent, aNumber, aKnowledge, aMinimum, aLastused);
   }

   public TempSkill(long aId, Skills aParent) throws IOException {
      super(aId, aParent);
   }

   @Override
   void save() throws IOException {
   }

   @Override
   void load() throws IOException {
   }

   @Override
   void saveValue(boolean aPlayer) throws IOException {
   }

   @Override
   public void setJoat(boolean aJoat) throws IOException {
   }

   @Override
   public void setNumber(int newNumber) throws IOException {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0) {
         try {
            Player player = Players.getInstance().getPlayer(pid);
            Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.setNumber(newNumber);
         } catch (NoSuchPlayerException var7) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var7);
         }
      } else {
         try {
            Creature creature = Creatures.getInstance().getCreature(pid);
            Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.setNumber(newNumber);
         } catch (NoSuchCreatureException var6) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var6);
         }
      }
   }

   @Override
   protected void alterSkill(double advanceMultiplicator, boolean decay, float times) {
      this.alterSkill(advanceMultiplicator, decay, times, false, 1.0);
   }

   @Override
   protected void alterSkill(double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider) {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0) {
         try {
            Player player = Players.getInstance().getPlayer(pid);
            Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.alterSkill(advanceMultiplicator, decay, times, useNewSystem, skillDivider);
         } catch (NoSuchPlayerException var13) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var13);
         }
      } else {
         try {
            Creature creature = Creatures.getInstance().getCreature(pid);
            Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.alterSkill(advanceMultiplicator, decay, times, useNewSystem, skillDivider);
         } catch (NoSuchCreatureException var12) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var12);
         }
      }
   }

   @Override
   public void setKnowledge(double aKnowledge, boolean load) {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0) {
         try {
            Player player = Players.getInstance().getPlayer(pid);
            Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.setKnowledge(aKnowledge, load);
         } catch (NoSuchPlayerException var9) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var9);
         }
      } else {
         try {
            Creature creature = Creatures.getInstance().getCreature(pid);
            Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.setKnowledge(aKnowledge, load);
         } catch (NoSuchCreatureException var8) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var8);
         }
      }
   }

   @Override
   public void setKnowledge(double aKnowledge, boolean load, boolean setMinimum) {
      long pid = this.parent.getId();
      if (WurmId.getType(pid) == 0) {
         try {
            Player player = Players.getInstance().getPlayer(pid);
            Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.setKnowledge(aKnowledge, load, setMinimum);
         } catch (NoSuchPlayerException var10) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var10);
         }
      } else {
         try {
            Creature creature = Creatures.getInstance().getCreature(pid);
            Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
            realSkill.setKnowledge(aKnowledge, load, setMinimum);
         } catch (NoSuchCreatureException var9) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var9);
         }
      }
   }

   @Override
   public double skillCheck(double check, double bonus, boolean test, float times) {
      return this.skillCheck(check, bonus, test, 10.0F, true, 1.1F, null, null);
   }

   @Override
   public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider) {
      return this.skillCheck(check, bonus, test, 10.0F, true, 1.1F, null, null);
   }

   @Override
   public double skillCheck(double check, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent) {
      return this.skillCheck(check, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
   }

   @Override
   public double skillCheck(
      double check,
      double bonus,
      boolean test,
      float times,
      boolean useNewSystem,
      double skillDivider,
      @Nullable Creature skillowner,
      @Nullable Creature opponent
   ) {
      if (skillowner != null) {
         Skill realSkill = skillowner.getSkills().learn(this.number, (float)this.knowledge, false);
         return realSkill.skillCheck(check, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
      } else {
         long pid = this.parent.getId();
         if (WurmId.getType(pid) == 0) {
            try {
               Player player = Players.getInstance().getPlayer(pid);
               Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
               return realSkill.skillCheck(check, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
            } catch (NoSuchPlayerException var16) {
               logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var16);
               return 0.0;
            }
         } else {
            try {
               Creature creature = Creatures.getInstance().getCreature(pid);
               Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
               return realSkill.skillCheck(check, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
            } catch (NoSuchCreatureException var17) {
               logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var17);
               return 0.0;
            }
         }
      }
   }

   @Override
   public double skillCheck(double check, Item item, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent) {
      return this.skillCheck(check, item, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
   }

   @Override
   public double skillCheck(
      double check,
      Item item,
      double bonus,
      boolean test,
      float times,
      boolean useNewSystem,
      double skillDivider,
      @Nullable Creature skillowner,
      @Nullable Creature opponent
   ) {
      if (skillowner != null) {
         Skill realSkill = skillowner.getSkills().learn(this.number, (float)this.knowledge, false);
         return realSkill.skillCheck(check, item, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
      } else {
         long pid = this.parent.getId();
         if (WurmId.getType(pid) == 0) {
            try {
               Player player = Players.getInstance().getPlayer(pid);
               Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
               return realSkill.skillCheck(check, item, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
            } catch (NoSuchPlayerException var17) {
               logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var17);
               return 0.0;
            }
         } else {
            try {
               Creature creature = Creatures.getInstance().getCreature(pid);
               Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
               return realSkill.skillCheck(check, item, bonus, test, 10.0F, true, 1.1F, skillowner, opponent);
            } catch (NoSuchCreatureException var18) {
               logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, (Throwable)var18);
               return 0.0;
            }
         }
      }
   }

   @Override
   public double skillCheck(double check, Item item, double bonus, boolean test, float times) {
      return this.skillCheck(check, item, bonus, test, 10.0F, true, 1.1F, null, null);
   }

   @Override
   public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider) {
      return this.skillCheck(check, item, bonus, test, 10.0F, true, 1.1F, null, null);
   }

   @Override
   public final boolean isTemporary() {
      return true;
   }
}

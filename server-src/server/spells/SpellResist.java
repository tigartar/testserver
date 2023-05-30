package com.wurmonline.server.spells;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

public class SpellResist implements TimeConstants {
   protected static Logger logger = Logger.getLogger(SpellResist.class.getName());
   protected static ArrayList<Creature> resistingCreatures = new ArrayList<>();
   public Creature creature;
   public long lastUpdated;
   public double currentResistance;
   public long fullyExpires;
   public SpellEffectsEnum spellEffect = SpellEffectsEnum.RES_HEAL;
   public double recoveryPerSecond = 0.016666666666666666;
   protected static final int GROUP_HEALING = 0;
   protected static final int GROUP_DRAIN_HEALTH = 1;
   protected static final int GROUP_FUNGUS_TRAP = 2;
   protected static final int GROUP_ICE_PILLAR = 3;
   protected static final int GROUP_FIRE_PILLAR = 4;
   protected static final int GROUP_TENTACLES = 5;
   protected static final int GROUP_PAIN_RAIN = 6;
   protected static final int GROUP_ROTTING_GUT = 7;
   protected static final int GROUP_TORNADO = 8;
   protected static final int GROUP_HEAVY_NUKE = 9;
   protected static final int GROUP_FIREHEART = 10;
   protected static final int GROUP_SHARDOFICE = 11;
   protected static final int GROUP_SCORN_OF_LIBILA = 12;
   protected static final int GROUP_HUMID_DRIZZLE = 13;
   protected static final int GROUP_SMITE = 14;
   protected static final int GROUP_LOCATE = 15;
   protected static final int GROUP_WRATH_MAGRANON = 16;
   protected static final int GROUP_DISPEL = 17;
   protected static final double HEALING_RECOVERY_SECOND = 8.0E-4;
   public static long lastPolledSpellResist = 0L;
   public static final long pollSpellResistTime = 1000L;

   public SpellResist(Creature creature) {
      this.creature = creature;
      this.lastUpdated = System.currentTimeMillis();
      this.currentResistance = 1.0;
      this.fullyExpires = this.lastUpdated;
   }

   public double scalePower(double power) {
      return power;
   }

   public static SpellResist getNewResistanceForGroup(Creature creature, int group) {
      switch(group) {
         case 0:
            return new SpellResist.HealingResist(creature);
         case 1:
            return new SpellResist.DrainHealthResist(creature);
         case 2:
            return new SpellResist.FungusTrapResist(creature);
         case 3:
            return new SpellResist.IcePillarResist(creature);
         case 4:
            return new SpellResist.FirePillarResist(creature);
         case 5:
            return new SpellResist.TentaclesResist(creature);
         case 6:
            return new SpellResist.PainRainResist(creature);
         case 7:
            return new SpellResist.RottingGutResist(creature);
         case 8:
            return new SpellResist.TornadoResist(creature);
         case 9:
            return new SpellResist.HeavyNukeResist(creature);
         case 10:
            return new SpellResist.FireHeartResist(creature);
         case 11:
            return new SpellResist.ShardOfIceResist(creature);
         case 12:
            return new SpellResist.ScornOfLibilaResist(creature);
         case 13:
            return new SpellResist.HumidDrizzleResist(creature);
         case 14:
            return new SpellResist.SmiteResist(creature);
         case 15:
            return new SpellResist.LocateResist(creature);
         case 16:
            return new SpellResist.WrathMagranonResist(creature);
         case 17:
            return new SpellResist.DispelResist(creature);
         default:
            logger.warning(String.format("Could not find a proper SpellResist instance for resist group %s.", group));
            return new SpellResist.HealingResist(creature);
      }
   }

   public static int getSpellGroup(int spellNumber) {
      switch(spellNumber) {
         case 246:
         case 247:
         case 248:
         case 249:
         case 408:
         case 409:
         case 438:
            return 0;
         case 252:
            return 14;
         case 255:
            return 1;
         case 407:
            return 13;
         case 413:
            return 8;
         case 414:
            return 3;
         case 418:
            return 5;
         case 419:
         case 451:
            return 15;
         case 420:
            return 4;
         case 424:
            return 10;
         case 428:
            return 7;
         case 430:
         case 931:
         case 932:
            return 9;
         case 432:
            return 6;
         case 433:
            return 2;
         case 441:
            return 16;
         case 448:
            return 12;
         case 450:
            return 17;
         case 485:
            return 11;
         default:
            logger.warning(String.format("Could not find a proper SpellResist group for spell number %s.", spellNumber));
            return 0;
      }
   }

   protected static double updateSpellResistance(Creature creature, SpellResist res, double additionalResistance) {
      long timeDelta = System.currentTimeMillis() - res.lastUpdated;
      double secondsPassed = (double)timeDelta / 1000.0;
      res.currentResistance = Math.min(1.0, res.currentResistance + secondsPassed * res.recoveryPerSecond);
      res.currentResistance = Math.max(0.0, res.currentResistance - additionalResistance);
      res.lastUpdated = System.currentTimeMillis();
      double secondsUntilFullyHealed = (1.0 - res.currentResistance) / res.recoveryPerSecond;
      res.fullyExpires = (long)((double)System.currentTimeMillis() + secondsUntilFullyHealed * 1000.0);
      if (res.spellEffect != null) {
         creature.getCommunicator().sendAddStatusEffect(res.spellEffect, (int)secondsUntilFullyHealed);
         if (!resistingCreatures.contains(creature)) {
            resistingCreatures.add(creature);
         }
      }

      return res.currentResistance;
   }

   public static double getSpellResistance(Creature creature, int spellNumber) {
      HashMap<Integer, SpellResist> resistances = creature.getSpellResistances();
      int group = getSpellGroup(spellNumber);
      if (resistances.containsKey(group)) {
         SpellResist res = resistances.get(group);
         return updateSpellResistance(creature, res, 0.0);
      } else {
         return 1.0;
      }
   }

   public static void addSpellResistance(Creature creature, int spellNumber, double power) {
      HashMap<Integer, SpellResist> resistances = creature.getSpellResistances();
      int group = getSpellGroup(spellNumber);
      if (resistances.containsKey(group)) {
         SpellResist res = resistances.get(group);
         double reduction = res.scalePower(power);
         double castPower = 0.0;
         if (group == 9) {
            switch(spellNumber) {
               case 430:
                  castPower = power * 100.0 / 17500.0 + 12000.0;
                  reduction = castPower * res.recoveryPerSecond * 3.0;
                  break;
               case 931:
                  castPower = power * 100.0 / 25000.0 + 7500.0;
                  reduction = castPower * res.recoveryPerSecond * 3.0;
                  break;
               case 932:
                  castPower = power * 100.0 / 10000.0 + 25000.0;
                  reduction = castPower * res.recoveryPerSecond * 3.5;
            }
         }

         updateSpellResistance(creature, res, reduction);
      } else {
         SpellResist res = getNewResistanceForGroup(creature, group);
         double reduction = res.scalePower(power);
         updateSpellResistance(creature, res, reduction);
         resistances.put(group, res);
      }
   }

   public static void onServerPoll() {
      long now = System.currentTimeMillis();
      if (lastPolledSpellResist + 1000L <= now) {
         for(Creature cret : new ArrayList<>(resistingCreatures)) {
            HashMap<Integer, SpellResist> resistances = cret.getSpellResistances();
            if (!resistances.isEmpty()) {
               for(int num : new HashSet<>(resistances.keySet())) {
                  SpellResist res = resistances.get(num);
                  if (res.fullyExpires <= System.currentTimeMillis()) {
                     if (res.creature != null) {
                        res.creature.getCommunicator().sendRemoveSpellEffect(res.spellEffect);
                     }

                     resistances.remove(num);
                  }
               }
            } else {
               resistingCreatures.remove(cret);
            }
         }

         lastPolledSpellResist = System.currentTimeMillis();
      }
   }

   public static class DispelResist extends SpellResist {
      public DispelResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_DISPEL;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         return power * this.recoveryPerSecond;
      }
   }

   public static class DrainHealthResist extends SpellResist {
      public DrainHealthResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_DRAINHEALTH;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 4000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond;
      }
   }

   public static class FireHeartResist extends SpellResist {
      public FireHeartResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_FIREHEART;
         this.recoveryPerSecond = 0.006666666666666667;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 17000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 1.5;
      }
   }

   public static class FirePillarResist extends SpellResist {
      public FirePillarResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_FIREPILLAR;
         this.recoveryPerSecond = 0.005555555555555556;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 575.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 0.05;
      }
   }

   public static class FungusTrapResist extends SpellResist {
      public FungusTrapResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_FUNGUSTRAP;
         this.recoveryPerSecond = 0.005555555555555556;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 550.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 0.05;
      }
   }

   public static class HealingResist extends SpellResist {
      public HealingResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_HEAL;
         this.recoveryPerSecond = 8.0E-4;
      }

      @Override
      public double scalePower(double power) {
         return power / 131070.0;
      }
   }

   public static class HeavyNukeResist extends SpellResist {
      public HeavyNukeResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_SHARED;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 29500.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 3.0;
      }
   }

   public static class HumidDrizzleResist extends SpellResist {
      public HumidDrizzleResist(Creature creature) {
         super(creature);
         this.spellEffect = null;
         this.recoveryPerSecond = 0.0011111111111111111;
      }

      @Override
      public double scalePower(double power) {
         return 1.0;
      }
   }

   public static class IcePillarResist extends SpellResist {
      public IcePillarResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_ICEPILLAR;
         this.recoveryPerSecond = 0.005555555555555556;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 550.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 0.05;
      }
   }

   public static class LocateResist extends SpellResist {
      public LocateResist(Creature creature) {
         super(creature);
         this.spellEffect = null;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         return power / 100.0;
      }
   }

   public static class PainRainResist extends SpellResist {
      public PainRainResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_PAINRAIN;
         this.recoveryPerSecond = 0.004166666666666667;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 10000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 2.0;
      }
   }

   public static class RottingGutResist extends SpellResist {
      public RottingGutResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_ROTTINGGUT;
         this.recoveryPerSecond = 0.006666666666666667;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 17000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 1.5;
      }
   }

   public static class ScornOfLibilaResist extends SpellResist {
      public ScornOfLibilaResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_SCORNLIBILA;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 8000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 1.5;
      }
   }

   public static class ShardOfIceResist extends SpellResist {
      public ShardOfIceResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_SHARDOFICE;
         this.recoveryPerSecond = 0.006666666666666667;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 17000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 1.5;
      }
   }

   public static class SmiteResist extends SpellResist {
      public SmiteResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_SMITE;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         return power / 65535.0;
      }
   }

   public static class TentaclesResist extends SpellResist {
      public TentaclesResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_TENTACLES;
         this.recoveryPerSecond = 0.005555555555555556;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 500.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond * 0.05;
      }
   }

   public static class TornadoResist extends SpellResist {
      public TornadoResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_TORNADO;
         this.recoveryPerSecond = 0.005555555555555556;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 12000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond;
      }
   }

   public static class WrathMagranonResist extends SpellResist {
      public WrathMagranonResist(Creature creature) {
         super(creature);
         this.spellEffect = SpellEffectsEnum.RES_WRATH_OF_MAGRANON;
         this.recoveryPerSecond = 0.0033333333333333335;
      }

      @Override
      public double scalePower(double power) {
         double maxDamage = 9000.0;
         double castPower = power * 100.0 / maxDamage;
         return castPower * this.recoveryPerSecond;
      }
   }
}

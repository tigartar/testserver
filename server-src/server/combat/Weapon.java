package com.wurmonline.server.combat;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import java.util.HashMap;
import java.util.Map;

public final class Weapon implements MiscConstants {
   private final int itemid;
   private final float damage;
   private final float speed;
   private final float critchance;
   private final int reach;
   private final int weightGroup;
   private final float parryPercent;
   private final double skillPenalty;
   private static float randomizer = 0.0F;
   private static final Map<Integer, Weapon> weapons = new HashMap<>();
   private static Weapon toCheck = null;
   private boolean damagedByMetal = false;
   private static final float critChanceMod = 5.0F;
   private static final float strengthModifier = Servers.localServer.isChallengeOrEpicServer() ? 1000.0F : 300.0F;

   public Weapon(int _itemid, float _damage, float _speed, float _critchance, int _reach, int _weightGroup, float _parryPercent, double _skillPenalty) {
      this.itemid = _itemid;
      this.damage = _damage;
      this.speed = _speed;
      this.critchance = _critchance / 5.0F;
      this.reach = _reach;
      this.weightGroup = _weightGroup;
      this.parryPercent = _parryPercent;
      this.skillPenalty = _skillPenalty;
      weapons.put(this.itemid, this);
   }

   public static final float getBaseDamageForWeapon(Item weapon) {
      if (weapon == null) {
         return 0.0F;
      } else {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.damage : 0.0F;
      }
   }

   public static final double getModifiedDamageForWeapon(Item weapon, Skill strength) {
      return getModifiedDamageForWeapon(weapon, strength, false);
   }

   public static final double getModifiedDamageForWeapon(Item weapon, Skill strength, boolean fullDam) {
      if (fullDam) {
         randomizer = 1.0F;
      } else {
         randomizer = (50.0F + Server.rand.nextFloat() * 50.0F) / 100.0F;
      }

      double damreturn = 1.0;
      if (weapon.isBodyPartAttached()) {
         damreturn = (double)getBaseDamageForWeapon(weapon);
      } else {
         damreturn = (double)(getBaseDamageForWeapon(weapon) * weapon.getCurrentQualityLevel() / 100.0F);
      }

      damreturn *= 1.0 + strength.getKnowledge(0.0) / (double)strengthModifier;
      return damreturn * (double)randomizer;
   }

   public static final float getBaseSpeedForWeapon(Item weapon) {
      if (weapon != null && !weapon.isBodyPartAttached()) {
         float materialMod = 1.0F;
         if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch(weapon.getMaterial()) {
               case 7:
                  materialMod = 1.05F;
                  break;
               case 13:
                  materialMod = 0.95F;
                  break;
               case 34:
                  materialMod = 0.96F;
                  break;
               case 57:
                  materialMod = 0.9F;
                  break;
               case 67:
                  materialMod = 0.95F;
                  break;
               case 96:
                  materialMod = 1.025F;
            }
         }

         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.speed * materialMod : 20.0F * materialMod;
      } else {
         return 1.0F;
      }
   }

   public static final float getRarityCritMod(byte rarity) {
      switch(rarity) {
         case 0:
            return 1.0F;
         case 1:
            return 1.1F;
         case 2:
            return 1.3F;
         case 3:
            return 1.5F;
         default:
            return 1.0F;
      }
   }

   public static final float getCritChanceForWeapon(Item weapon) {
      if (weapon != null && !weapon.isBodyPartAttached()) {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.critchance * getRarityCritMod(weapon.getRarity()) : 0.0F;
      } else {
         return 0.01F;
      }
   }

   public static final int getReachForWeapon(Item weapon) {
      if (weapon != null && !weapon.isBodyPartAttached()) {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.reach : 1;
      } else {
         return 1;
      }
   }

   public static final int getWeightGroupForWeapon(Item weapon) {
      if (weapon != null && !weapon.isBodyPartAttached()) {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.weightGroup : 10;
      } else {
         return 1;
      }
   }

   public static final double getSkillPenaltyForWeapon(Item weapon) {
      if (weapon != null && !weapon.isBodyPartAttached()) {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.skillPenalty : 7.0;
      } else {
         return 0.0;
      }
   }

   public static final float getWeaponParryPercent(Item weapon) {
      if (weapon == null) {
         return 0.0F;
      } else if (weapon.isBodyPart()) {
         return 0.0F;
      } else {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.parryPercent : 0.0F;
      }
   }

   void setDamagedByMetal(boolean aDamagedByMetal) {
      this.damagedByMetal = aDamagedByMetal;
   }

   public static final boolean isWeaponDamByMetal(Item weapon) {
      if (weapon == null) {
         return false;
      } else if (weapon.isBodyPart() && weapon.isBodyPartRemoved()) {
         return true;
      } else {
         toCheck = weapons.get(weapon.getTemplateId());
         return toCheck != null ? toCheck.damagedByMetal : false;
      }
   }

   public static double getMaterialDamageBonus(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 7:
               return 0.975F;
            case 10:
               return 0.65F;
            case 12:
               return 0.5;
            case 13:
               return 0.9F;
            case 30:
               return 0.99F;
            case 31:
               return 0.985F;
            case 34:
               return 0.925F;
            case 56:
               return 1.1F;
            case 67:
               return 1.05F;
         }
      } else if (material == 56) {
         return 1.1F;
      }

      return 1.0;
   }

   public static double getMaterialHunterDamageBonus(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 8:
               return 1.1F;
            case 96:
               return 1.05F;
         }
      }

      return 1.0;
   }

   public static double getMaterialArmourDamageBonus(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 7:
               return 1.05F;
            case 9:
               return 1.025F;
            case 30:
               return 1.05F;
            case 31:
               return 1.075F;
         }
      }

      return 1.0;
   }

   public static float getMaterialParryBonus(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 8:
               return 1.025F;
            case 34:
               return 1.05F;
         }
      }

      return 1.0F;
   }

   public static float getMaterialExtraWoundMod(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 10:
               return 0.3F;
            case 12:
               return 0.75F;
         }
      }

      return 0.0F;
   }

   public static byte getMaterialExtraWoundType(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 10:
               return 5;
            case 12:
               return 5;
         }
      }

      return 5;
   }

   public static double getMaterialBashModifier(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 7:
               return 1.1F;
            case 8:
               return 1.1F;
            case 9:
               return 1.05F;
            case 10:
               return 0.9F;
            case 12:
               return 1.2F;
            case 13:
               return 0.85F;
            case 30:
               return 1.05F;
            case 31:
               return 1.025F;
            case 34:
               return 0.9F;
            case 56:
               return 1.075F;
            case 57:
               return 1.1F;
            case 67:
               return 1.075F;
            case 96:
               return 1.1F;
         }
      }

      return 1.0;
   }
}

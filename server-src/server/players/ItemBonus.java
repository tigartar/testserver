package com.wurmonline.server.players;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.SpellEffect;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemBonus implements TimeConstants {
   private static final Logger logger = Logger.getLogger(ItemBonus.class.getName());
   private static final ConcurrentHashMap<Long, Map<Integer, ItemBonus>> playerBonuses = new ConcurrentHashMap<>();
   private final Item itemOne;
   private Item itemTwo;
   private final long playerId;
   private final int bonusTypeId;
   private float bonusValue;
   private float bonus2Value;
   private boolean stacks = false;
   private static final long decayTime = 28800L;

   private ItemBonus(Item item, long playerid, int bonusType, float value) {
      this(item, playerid, bonusType, value, false);
   }

   private ItemBonus(Item item, long playerid, int bonusType, float value, boolean isStacking) {
      this.itemOne = item;
      this.playerId = playerid;
      this.bonusTypeId = bonusType;
      this.bonusValue = value;
      this.setStacking(isStacking);
   }

   public static final void calcAndAddBonus(Item item, Creature creature) {
      SpellEffectsEnum bonusType = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
      if (bonusType != null) {
         float value = getBonusValueForItem(item);
         if (value > 0.0F) {
            addBonus(item, creature, bonusType.getTypeId(), value, getStacking(item));
         }
      }
   }

   public static final void checkDepleteAndRename(Item usedItem, Creature owner) {
      if (usedItem.isRiftLoot() && isTimed(usedItem) && WurmCalendar.currentTime - usedItem.getLastMaintained() > 28800L) {
         SpellEffectsEnum bonusType = SpellEffectsEnum.getEnumForItemTemplateId(usedItem.getTemplateId(), usedItem.getData1());
         if (bonusType != null) {
            ItemBonus cbonus = getItemBonusObject(owner.getWurmId(), bonusType.getTypeId());
            if (cbonus != null) {
               if (usedItem.getAuxData() <= 0) {
                  removeBonus(usedItem, owner);
                  return;
               }

               usedItem.setAuxData((byte)(usedItem.getAuxData() - 1));
               usedItem.setLastMaintained(WurmCalendar.currentTime);
               rename(usedItem);
            }
         }
      }
   }

   private static final void rename(Item usedItem) {
      if (usedItem.getAuxData() > 0 && !usedItem.getActualName().toLowerCase().contains("used")) {
         usedItem.setName("used " + usedItem.getActualName());
      } else if (usedItem.getAuxData() <= 0) {
         if (usedItem.getActualName().toLowerCase().contains("used")) {
            usedItem.setName(usedItem.getActualName().replace("used", "depleted"));
         } else if (!usedItem.getActualName().toLowerCase().contains("depleted")) {
            usedItem.setName("depleted " + usedItem.getActualName());
         }
      }
   }

   private static final void addBonus(Item item, Creature creature, int bonusType, float value, boolean isStacking) {
      if (!isTimed(item) || item.getAuxData() > 0) {
         Map<Integer, ItemBonus> curr = playerBonuses.get(creature.getWurmId());
         if (curr == null) {
            curr = new ConcurrentHashMap<>();
            playerBonuses.put(creature.getWurmId(), curr);
         }

         ItemBonus cbonus = curr.get(bonusType);
         if (cbonus == null) {
            cbonus = new ItemBonus(item, creature.getWurmId(), bonusType, value, isStacking);
            curr.put(bonusType, cbonus);
         } else {
            cbonus.setItemTwo(item);
            cbonus.setBonus2Value(value);
         }

         cbonus.sendNewBonusToClient(item, creature);
         checkDepleteAndRename(item, creature);
      }
   }

   public final void sendNewBonusToClient(Item item, Creature creature) {
      if (item != null) {
         if (!isTimed(item) || this.getSecondsLeft() > 0) {
            SpellEffectsEnum senum = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
            creature.getCommunicator().sendAddSpellEffect(senum, this.getSecondsLeft(), this.getBonusVal(0.0F));
         }
      } else {
         logger.log(Level.INFO, "Item was null for " + creature.getName(), (Throwable)(new Exception()));
      }
   }

   public static final void sendAllItemBonusToPlayer(Player player) {
      Map<Integer, ItemBonus> curr = playerBonuses.get(player.getWurmId());
      if (curr != null) {
         for(ItemBonus bonus : curr.values()) {
            bonus.sendNewBonusToClient(bonus.getItemOne(), player);
         }
      }
   }

   public final void sendRemoveBonusToClient(Item item, Creature creature) {
      SpellEffectsEnum senum = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
      creature.getCommunicator().sendRemoveSpellEffect(senum.getId(), senum);
      byte debuff = SpellEffectsEnum.getDebuffForEnum(senum);
      if (debuff != 0) {
         SpellEffects effs = creature.getSpellEffects();
         SpellEffect edebuff = effs.getSpellEffect(debuff);
         if (edebuff == null) {
            edebuff = new SpellEffect(creature.getWurmId(), debuff, 100.0F, 300, (byte)10, (byte)1, true);
            effs.addSpellEffect(edebuff);
         } else {
            edebuff.setTimeleft(300);
         }
      }
   }

   public static final void removeBonus(Item item, Creature creature) {
      Map<Integer, ItemBonus> curr = playerBonuses.get(creature.getWurmId());
      if (curr != null) {
         SpellEffectsEnum senum = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
         ItemBonus cbonus = curr.get(senum.getTypeId());
         if (cbonus != null) {
            if (cbonus.getItemTwo() == item) {
               cbonus.setItemTwo(null);
               cbonus.setBonus2Value(0.0F);
               cbonus.sendNewBonusToClient(item, creature);
               return;
            }

            if (cbonus.getItemOne() == item) {
               if (cbonus.getItemTwo() != null) {
                  ItemBonus newBonus = new ItemBonus(
                     cbonus.getItemTwo(), creature.getWurmId(), cbonus.getBonusType(), cbonus.getItemTwoBonusValue(0.0F), cbonus.isStacking()
                  );
                  curr.put(cbonus.getBonusType(), newBonus);
                  newBonus.sendNewBonusToClient(item, creature);
               } else {
                  curr.remove(cbonus.getBonusType());
                  cbonus.sendRemoveBonusToClient(item, creature);
               }
            }
         } else {
            logger.log(Level.INFO, "Failed to remove bonus for " + item.getName() + " for " + creature.getName() + " although it should be registered.");
         }

         if (curr.isEmpty()) {
            playerBonuses.remove(creature.getWurmId());
         }
      }
   }

   public static final void clearBonuses(long playerid) {
      playerBonuses.remove(playerid);
   }

   private static final float getBonus(long playerid, int bonusType) {
      return getBonus(playerid, bonusType, 0.0F);
   }

   private static final float getBonus(long playerid, int bonusType, float damageDealt) {
      ItemBonus bonus = getItemBonusObject(playerid, bonusType);
      return bonus == null ? 0.0F : bonus.getBonusVal(damageDealt);
   }

   private static final ItemBonus getItemBonusObject(long playerId, int bonusType) {
      Map<Integer, ItemBonus> curr = playerBonuses.get(playerId);
      return curr == null ? null : curr.get(bonusType);
   }

   public final float getBonusVal(float damageDealt) {
      if (this.isStacking()) {
         return this.getItemOneBonusValue(damageDealt) + this.getItemTwoBonusValue(damageDealt);
      } else {
         return this.getItemOneBonusValue(0.0F) > this.getItemTwoBonusValue(0.0F)
            ? this.getItemOneBonusValue(damageDealt)
            : this.getItemTwoBonusValue(damageDealt);
      }
   }

   public final int getSecondsLeft() {
      if (this.isStacking()) {
         return Math.min(this.getSeconds1Left(), this.getSeconds2Left());
      } else {
         return this.getItemOneBonusValue(0.0F) > this.getItemTwoBonusValue(0.0F) ? this.getSeconds1Left() : this.getSeconds2Left();
      }
   }

   public Item getItemOne() {
      return this.itemOne;
   }

   public long getPlayerId() {
      return this.playerId;
   }

   public int getBonusType() {
      return this.bonusTypeId;
   }

   private float getItemOneBonusValue(float damageDealt) {
      if (damageDealt > 0.0F) {
         this.itemOne.setDamage(this.itemOne.getDamage() + damageDealt);
      }

      return this.bonusValue;
   }

   private float getItemTwoBonusValue(float damageDealt) {
      if (this.itemTwo != null && damageDealt > 0.0F) {
         this.itemTwo.setDamage(this.itemTwo.getDamage() + damageDealt);
      }

      return this.bonus2Value;
   }

   public void setBonus2Value(float bonus2Value) {
      this.bonus2Value = bonus2Value;
   }

   public Item getItemTwo() {
      return this.itemTwo;
   }

   public void setItemTwo(Item item2) {
      this.itemTwo = item2;
   }

   public boolean isStacking() {
      return this.stacks;
   }

   public void setStacking(boolean stacks) {
      this.stacks = stacks;
   }

   public static final float getBonusValueForItem(Item item) {
      return 0.0F;
   }

   public static final boolean isTimed(Item item) {
      return false;
   }

   public static final boolean getStacking(Item item) {
      return false;
   }

   private int getSeconds1Left() {
      return this.itemOne != null && isTimed(this.itemOne) ? this.itemOne.getAuxData() * 3600 : -1;
   }

   private int getSeconds2Left() {
      return this.itemTwo != null && isTimed(this.itemTwo) ? this.itemTwo.getAuxData() * 3600 : -1;
   }

   public static final float getGlanceBonusFor(ArmourTemplate.ArmourType armourType, byte woundType, Item weapon, Creature creature) {
      float bonus = 0.0F;
      if (armourType == ArmourTemplate.ARMOUR_TYPE_CLOTH) {
         if (woundType == 0) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_COTTON_CRUSHING.getTypeId());
         }

         if (woundType == 1) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_COTTON_SLASHING.getTypeId());
         }
      } else if (armourType == ArmourTemplate.ARMOUR_TYPE_LEATHER) {
         if (weapon.isTwoHanded()) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_LEATHER_TWOHANDED.getTypeId());
         }
      } else if (armourType == ArmourTemplate.ARMOUR_TYPE_STUDDED && weapon.isTwoHanded()) {
         bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_STUDDED_TWOHANDED.getTypeId());
      }

      return bonus;
   }

   public static final float getFaceDamReductionBonus(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_FACEDAM.getTypeId());
   }

   public static final float getAreaSpellReductionBonus(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_AREASPELL_DAMREDUCT.getTypeId());
   }

   public static final float getAreaSpellDamageIncreaseBonus(Creature creature) {
      return 1.0F + getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_AREA_SPELL.getTypeId());
   }

   public static final float getWeaponDamageIncreaseBonus(Creature creature, Item weapon) {
      float bonus = 0.0F;
      return 1.0F + bonus;
   }

   public static final float getArcheryPenaltyReduction(Creature creature) {
      return creature.getArmourLimitingFactor() <= -0.15F ? 1.0F + getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_HEAVY_ARCHERY.getTypeId()) : 1.0F;
   }

   public static final float getStaminaReductionBonus(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_STAMINA.getTypeId());
   }

   public static final float getDodgeBonus(Creature creature) {
      return 1.0F + getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_DODGE.getTypeId());
   }

   public static final float getCRBonus(Creature creature) {
      return getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_CR.getTypeId());
   }

   public static final float getSpellResistBonus(Creature creature) {
      return getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_SPELLRESIST.getTypeId());
   }

   public static final float getHealingBonus(Creature creature) {
      return getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_HEALING.getTypeId());
   }

   public static final float getSkillGainBonus(Creature creature, int skillId) {
      return 0.0F;
   }

   public static final float getKillEfficiencyBonus(Creature creature) {
      return 1.0F + getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_SKILLEFF.getTypeId(), 1.0F);
   }

   public static final float getImproveSkillMaxBonus(Creature creature) {
      return 1.0F + getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_SKILLMAX.getTypeId());
   }

   public static final float getDrownDamReduction(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_SWIMMING.getTypeId(), 1.0F);
   }

   public static final float getStealthBonus(Creature creature) {
      return 1.0F + getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_STEALTH.getTypeId(), 0.5F);
   }

   public static final float getDetectionBonus(Creature creature) {
      return 50.0F * getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_DETECTION.getTypeId());
   }

   public static final float getParryBonus(Creature creature, Item weapon) {
      float bonus = 0.0F;
      return 1.0F - bonus;
   }

   public static final float getWeaponSpellDamageIncreaseBonus(long ownerid) {
      return ownerid > 0L ? 1.0F + getBonus(ownerid, SpellEffectsEnum.ITEM_BRACELET_ENCHANTDAM.getTypeId()) : 1.0F;
   }

   public static final float getHurtingReductionBonus(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_HURTING.getTypeId(), 1.0F);
   }

   public static final float getFocusBonus(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_FOCUS.getTypeId());
   }

   public static final float getReplenishBonus(Creature creature) {
      return 1.0F - getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_REPLENISH.getTypeId());
   }

   public static final float getDamReductionBonusFor(ArmourTemplate.ArmourType armourType, byte woundType, Item weapon, Creature creature) {
      float bonus = 0.0F;
      if (armourType == ArmourTemplate.ARMOUR_TYPE_CLOTH) {
         if (woundType == 1) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_COTTON_SLASHDAM.getTypeId(), 0.1F);
         }
      } else if (armourType == ArmourTemplate.ARMOUR_TYPE_LEATHER) {
         if (woundType == 0) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_LEATHER_CRUSHDAM.getTypeId(), 0.1F);
         }
      } else if (armourType == ArmourTemplate.ARMOUR_TYPE_CHAIN) {
         if (woundType == 1) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_CHAIN_SLASHDAM.getTypeId(), 0.1F);
         }

         if (woundType == 2) {
            bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_CHAIN_PIERCEDAM.getTypeId(), 0.1F);
         }
      }

      if (weapon.getEnchantmentDamageType() > 0) {
         bonus += getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_ENCHANT_DAMREDUCT.getTypeId(), 0.1F);
      }

      return 1.0F - bonus;
   }

   public static final float getBashDodgeBonusFor(Creature creature) {
      return 1.0F;
   }
}

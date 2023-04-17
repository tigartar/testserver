/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.spells.SpellEffect;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemBonus
implements TimeConstants {
    private static final Logger logger = Logger.getLogger(ItemBonus.class.getName());
    private static final ConcurrentHashMap<Long, Map<Integer, ItemBonus>> playerBonuses = new ConcurrentHashMap();
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
        float value;
        SpellEffectsEnum bonusType = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
        if (bonusType != null && (value = ItemBonus.getBonusValueForItem(item)) > 0.0f) {
            ItemBonus.addBonus(item, creature, bonusType.getTypeId(), value, ItemBonus.getStacking(item));
        }
    }

    public static final void checkDepleteAndRename(Item usedItem, Creature owner) {
        ItemBonus cbonus;
        SpellEffectsEnum bonusType;
        if (usedItem.isRiftLoot() && ItemBonus.isTimed(usedItem) && WurmCalendar.currentTime - usedItem.getLastMaintained() > 28800L && (bonusType = SpellEffectsEnum.getEnumForItemTemplateId(usedItem.getTemplateId(), usedItem.getData1())) != null && (cbonus = ItemBonus.getItemBonusObject(owner.getWurmId(), bonusType.getTypeId())) != null) {
            if (usedItem.getAuxData() <= 0) {
                ItemBonus.removeBonus(usedItem, owner);
                return;
            }
            usedItem.setAuxData((byte)(usedItem.getAuxData() - 1));
            usedItem.setLastMaintained(WurmCalendar.currentTime);
            ItemBonus.rename(usedItem);
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
        ItemBonus cbonus;
        if (ItemBonus.isTimed(item) && item.getAuxData() <= 0) {
            return;
        }
        Map<Integer, ItemBonus> curr = playerBonuses.get(creature.getWurmId());
        if (curr == null) {
            curr = new ConcurrentHashMap<Integer, ItemBonus>();
            playerBonuses.put(creature.getWurmId(), curr);
        }
        if ((cbonus = curr.get(bonusType)) == null) {
            cbonus = new ItemBonus(item, creature.getWurmId(), bonusType, value, isStacking);
            curr.put(bonusType, cbonus);
        } else {
            cbonus.setItemTwo(item);
            cbonus.setBonus2Value(value);
        }
        cbonus.sendNewBonusToClient(item, creature);
        ItemBonus.checkDepleteAndRename(item, creature);
    }

    public final void sendNewBonusToClient(Item item, Creature creature) {
        if (item != null) {
            if (!ItemBonus.isTimed(item) || this.getSecondsLeft() > 0) {
                SpellEffectsEnum senum = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
                creature.getCommunicator().sendAddSpellEffect(senum, this.getSecondsLeft(), this.getBonusVal(0.0f));
            }
        } else {
            logger.log(Level.INFO, "Item was null for " + creature.getName(), new Exception());
        }
    }

    public static final void sendAllItemBonusToPlayer(Player player) {
        Map<Integer, ItemBonus> curr = playerBonuses.get(player.getWurmId());
        if (curr != null) {
            for (ItemBonus bonus : curr.values()) {
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
                edebuff = new SpellEffect(creature.getWurmId(), debuff, 100.0f, 300, 10, 1, true);
                effs.addSpellEffect(edebuff);
            } else {
                edebuff.setTimeleft(300);
            }
        }
    }

    public static final void removeBonus(Item item, Creature creature) {
        Map<Integer, ItemBonus> curr = playerBonuses.get(creature.getWurmId());
        if (curr == null) {
            return;
        }
        SpellEffectsEnum senum = SpellEffectsEnum.getEnumForItemTemplateId(item.getTemplateId(), item.getData1());
        ItemBonus cbonus = curr.get(senum.getTypeId());
        if (cbonus != null) {
            if (cbonus.getItemTwo() == item) {
                cbonus.setItemTwo(null);
                cbonus.setBonus2Value(0.0f);
                cbonus.sendNewBonusToClient(item, creature);
                return;
            }
            if (cbonus.getItemOne() == item) {
                if (cbonus.getItemTwo() != null) {
                    ItemBonus newBonus = new ItemBonus(cbonus.getItemTwo(), creature.getWurmId(), cbonus.getBonusType(), cbonus.getItemTwoBonusValue(0.0f), cbonus.isStacking());
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

    public static final void clearBonuses(long playerid) {
        playerBonuses.remove(playerid);
    }

    private static final float getBonus(long playerid, int bonusType) {
        return ItemBonus.getBonus(playerid, bonusType, 0.0f);
    }

    private static final float getBonus(long playerid, int bonusType, float damageDealt) {
        ItemBonus bonus = ItemBonus.getItemBonusObject(playerid, bonusType);
        if (bonus == null) {
            return 0.0f;
        }
        return bonus.getBonusVal(damageDealt);
    }

    private static final ItemBonus getItemBonusObject(long playerId, int bonusType) {
        Map<Integer, ItemBonus> curr = playerBonuses.get(playerId);
        if (curr == null) {
            return null;
        }
        return curr.get(bonusType);
    }

    public final float getBonusVal(float damageDealt) {
        if (this.isStacking()) {
            return this.getItemOneBonusValue(damageDealt) + this.getItemTwoBonusValue(damageDealt);
        }
        if (this.getItemOneBonusValue(0.0f) > this.getItemTwoBonusValue(0.0f)) {
            return this.getItemOneBonusValue(damageDealt);
        }
        return this.getItemTwoBonusValue(damageDealt);
    }

    public final int getSecondsLeft() {
        if (this.isStacking()) {
            return Math.min(this.getSeconds1Left(), this.getSeconds2Left());
        }
        if (this.getItemOneBonusValue(0.0f) > this.getItemTwoBonusValue(0.0f)) {
            return this.getSeconds1Left();
        }
        return this.getSeconds2Left();
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
        if (damageDealt > 0.0f) {
            this.itemOne.setDamage(this.itemOne.getDamage() + damageDealt);
        }
        return this.bonusValue;
    }

    private float getItemTwoBonusValue(float damageDealt) {
        if (this.itemTwo != null && damageDealt > 0.0f) {
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
        return 0.0f;
    }

    public static final boolean isTimed(Item item) {
        return false;
    }

    public static final boolean getStacking(Item item) {
        return false;
    }

    private int getSeconds1Left() {
        if (this.itemOne != null && ItemBonus.isTimed(this.itemOne)) {
            return this.itemOne.getAuxData() * 3600;
        }
        return -1;
    }

    private int getSeconds2Left() {
        if (this.itemTwo != null && ItemBonus.isTimed(this.itemTwo)) {
            return this.itemTwo.getAuxData() * 3600;
        }
        return -1;
    }

    public static final float getGlanceBonusFor(ArmourTemplate.ArmourType armourType, byte woundType, Item weapon, Creature creature) {
        float bonus = 0.0f;
        if (armourType == ArmourTemplate.ARMOUR_TYPE_CLOTH) {
            if (woundType == 0) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_COTTON_CRUSHING.getTypeId());
            }
            if (woundType == 1) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_COTTON_SLASHING.getTypeId());
            }
        } else if (armourType == ArmourTemplate.ARMOUR_TYPE_LEATHER) {
            if (weapon.isTwoHanded()) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_LEATHER_TWOHANDED.getTypeId());
            }
        } else if (armourType == ArmourTemplate.ARMOUR_TYPE_STUDDED && weapon.isTwoHanded()) {
            bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_STUDDED_TWOHANDED.getTypeId());
        }
        return bonus;
    }

    public static final float getFaceDamReductionBonus(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_FACEDAM.getTypeId());
    }

    public static final float getAreaSpellReductionBonus(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_AREASPELL_DAMREDUCT.getTypeId());
    }

    public static final float getAreaSpellDamageIncreaseBonus(Creature creature) {
        return 1.0f + ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_AREA_SPELL.getTypeId());
    }

    public static final float getWeaponDamageIncreaseBonus(Creature creature, Item weapon) {
        float bonus = 0.0f;
        return 1.0f + bonus;
    }

    public static final float getArcheryPenaltyReduction(Creature creature) {
        if (creature.getArmourLimitingFactor() <= -0.15f) {
            return 1.0f + ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_HEAVY_ARCHERY.getTypeId());
        }
        return 1.0f;
    }

    public static final float getStaminaReductionBonus(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_STAMINA.getTypeId());
    }

    public static final float getDodgeBonus(Creature creature) {
        return 1.0f + ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_DODGE.getTypeId());
    }

    public static final float getCRBonus(Creature creature) {
        return ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_CR.getTypeId());
    }

    public static final float getSpellResistBonus(Creature creature) {
        return ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_SPELLRESIST.getTypeId());
    }

    public static final float getHealingBonus(Creature creature) {
        return ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_HEALING.getTypeId());
    }

    public static final float getSkillGainBonus(Creature creature, int skillId) {
        return 0.0f;
    }

    public static final float getKillEfficiencyBonus(Creature creature) {
        return 1.0f + ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_SKILLEFF.getTypeId(), 1.0f);
    }

    public static final float getImproveSkillMaxBonus(Creature creature) {
        return 1.0f + ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_SKILLMAX.getTypeId());
    }

    public static final float getDrownDamReduction(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_SWIMMING.getTypeId(), 1.0f);
    }

    public static final float getStealthBonus(Creature creature) {
        return 1.0f + ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_STEALTH.getTypeId(), 0.5f);
    }

    public static final float getDetectionBonus(Creature creature) {
        return 50.0f * ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_RING_DETECTION.getTypeId());
    }

    public static final float getParryBonus(Creature creature, Item weapon) {
        float bonus = 0.0f;
        return 1.0f - bonus;
    }

    public static final float getWeaponSpellDamageIncreaseBonus(long ownerid) {
        if (ownerid > 0L) {
            return 1.0f + ItemBonus.getBonus(ownerid, SpellEffectsEnum.ITEM_BRACELET_ENCHANTDAM.getTypeId());
        }
        return 1.0f;
    }

    public static final float getHurtingReductionBonus(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_HURTING.getTypeId(), 1.0f);
    }

    public static final float getFocusBonus(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_FOCUS.getTypeId());
    }

    public static final float getReplenishBonus(Creature creature) {
        return 1.0f - ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_NECKLACE_REPLENISH.getTypeId());
    }

    public static final float getDamReductionBonusFor(ArmourTemplate.ArmourType armourType, byte woundType, Item weapon, Creature creature) {
        float bonus = 0.0f;
        if (armourType == ArmourTemplate.ARMOUR_TYPE_CLOTH) {
            if (woundType == 1) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_COTTON_SLASHDAM.getTypeId(), 0.1f);
            }
        } else if (armourType == ArmourTemplate.ARMOUR_TYPE_LEATHER) {
            if (woundType == 0) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_LEATHER_CRUSHDAM.getTypeId(), 0.1f);
            }
        } else if (armourType == ArmourTemplate.ARMOUR_TYPE_CHAIN) {
            if (woundType == 1) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_CHAIN_SLASHDAM.getTypeId(), 0.1f);
            }
            if (woundType == 2) {
                bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_CHAIN_PIERCEDAM.getTypeId(), 0.1f);
            }
        }
        if (weapon.getEnchantmentDamageType() > 0) {
            bonus += ItemBonus.getBonus(creature.getWurmId(), SpellEffectsEnum.ITEM_ENCHANT_DAMREDUCT.getTypeId(), 0.1f);
        }
        return 1.0f - bonus;
    }

    public static final float getBashDodgeBonusFor(Creature creature) {
        return 1.0f;
    }
}


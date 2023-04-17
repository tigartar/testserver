/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MessageServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.combat.BattleEvent;
import com.wurmonline.server.combat.Battles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.SpellTypes;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.Enchants;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class Spell
implements SpellTypes,
MiscConstants,
SoundNames,
Enchants,
TimeConstants,
Comparable<Spell> {
    protected static final Logger logger = Logger.getLogger(Spell.class.getName());
    public static final byte TYPE_CHAPLAIN = 1;
    public static final byte TYPE_MISSIONARY = 2;
    public static final byte TYPE_BOTH = 0;
    boolean[][] area;
    int[][] offsets;
    public final int number;
    public final String name;
    private final int castingTime;
    public boolean religious = false;
    public boolean offensive = false;
    public boolean healing = false;
    public boolean singleItemEnchant = false;
    static final int enchantDifficulty = 60;
    final int cost;
    protected boolean hasDynamicCost = false;
    protected final int difficulty;
    public final int level;
    protected boolean targetCreature = false;
    protected boolean targetItem = false;
    protected boolean targetWeapon = false;
    protected boolean targetArmour = false;
    protected boolean targetJewelry = false;
    protected boolean targetPendulum = false;
    protected boolean targetWound = false;
    protected boolean targetTile = false;
    protected boolean targetTileBorder = false;
    protected boolean karmaSpell = false;
    private long cooldown = 60000L;
    boolean dominate = false;
    public boolean isRitual = false;
    byte enchantment = 0;
    public String effectdesc = "";
    String description = "N/A";
    protected byte type = 0;
    public static final int TIME_ENCHANT_CAST = 30;
    public static final long TIME_ENCHANT = 300000L;
    public static final long TIME_CONTINUUM = 240000L;
    public static final long TIME_CREATUREBUFF = 180000L;
    public static final long TIME_AOE = 120000L;
    public static final long TIME_UTILITY = 1800000L;
    public static final long TIME_UTILITY_HALF = 900000L;
    public static final long TIME_UTILITY_DOUBLE = 3600000L;
    public static final long TIME_COMBAT = 0L;
    public static final long TIME_COMBAT_SMALLDELAY = 10000L;
    public static final long TIME_COMBAT_NORMALDELAY = 30000L;
    public static final long TIME_COMBAT_LONGDELAY = 60000L;
    public static final int Spirit_Fire = 1;
    public static final int Spirit_Water = 2;
    public static final int Spirit_Earth = 3;
    public static final int Spirit_Air = 4;
    public static final double minOffensivePower = 50.0;

    Spell(String _name, int num, int _castingTime, int _cost, int _difficulty, int _level, long _cooldown) {
        this.name = _name;
        this.number = num;
        this.castingTime = _castingTime;
        this.cost = _cost;
        this.difficulty = _difficulty;
        this.level = _level;
        this.cooldown = _cooldown;
    }

    Spell(String _name, int num, int _castingTime, int _cost, int _difficulty, int _level, String aEffectDescription, byte aEnchantment, boolean aDominate, boolean aReligious, boolean aOffensive, boolean aTargetCreature, boolean aTargetItem, boolean aTargetWound, boolean aTargetTile) {
        this.name = _name;
        this.number = num;
        this.castingTime = _castingTime;
        this.cost = _cost;
        this.difficulty = _difficulty;
        this.level = _level;
        this.effectdesc = aEffectDescription;
        this.enchantment = aEnchantment;
        this.dominate = aDominate;
        this.religious = aReligious;
        this.offensive = aOffensive;
        this.targetCreature = aTargetCreature;
        this.targetItem = aTargetItem;
        this.targetTile = aTargetTile;
        this.targetWound = aTargetWound;
    }

    public Spell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown, boolean aReligious) {
        this.name = aName;
        this.number = aNum;
        this.castingTime = aCastingTime;
        this.cost = aCost;
        this.difficulty = aDifficulty;
        this.level = aLevel;
        this.cooldown = aCooldown;
        this.religious = aReligious;
    }

    @Override
    public int compareTo(Spell otherSpell) {
        return this.getName().compareTo(otherSpell.getName());
    }

    public static final boolean mayBeEnchanted(Item target) {
        if (target.isOverrideNonEnchantable()) {
            return true;
        }
        return !target.isBodyPart() && !target.isNewbieItem() && !target.isNoTake() && target.getTemplateId() != 179 && target.getTemplateId() != 386 && !target.isTemporary() && target.getTemplateId() != 272 && (!target.isLockable() || target.getLockId() == -10L) && !target.isIndestructible() && !target.isHugeAltar() && !target.isDomainItem() && !target.isKingdomMarker() && !target.isTraded() && !target.isBanked() && !target.isArtifact() && !target.isEgg() && !target.isChallengeNewbieItem() && !target.isRiftLoot() && !target.isRiftAltar() && target.getTemplateId() != 1307;
    }

    public final long getCooldown() {
        return this.cooldown;
    }

    public final void setType(byte newType) {
        this.type = newType;
    }

    protected static double trimPower(Creature performer, double power) {
        if (Servers.localServer.HOMESERVER && performer.isChampion()) {
            power = Math.min(power, 50.0);
        }
        if (performer.hasFlag(82)) {
            power += 5.0;
        }
        return power;
    }

    public final boolean stillCooldown(Creature performer) {
        if (this.cooldown > 0L) {
            long avail;
            Cooldowns cd = Cooldowns.getCooldownsFor(performer.getWurmId(), false);
            if (cd != null && (avail = cd.isAvaibleAt(this.number)) > System.currentTimeMillis() && performer.getPower() < 3) {
                performer.getCommunicator().sendNormalServerMessage("You need to wait " + Server.getTimeFor(avail - System.currentTimeMillis()) + " until you can cast " + this.name + " again.");
                return true;
            }
            for (Creature c : performer.getLinks()) {
                long avail2;
                if (!this.stillCooldown(c)) continue;
                Cooldowns cd2 = Cooldowns.getCooldownsFor(performer.getWurmId(), false);
                if (cd2 != null && (avail2 = cd2.isAvaibleAt(this.number)) > System.currentTimeMillis() && c.getPower() < 3) {
                    performer.getCommunicator().sendNormalServerMessage(c.getName() + " needs to wait " + Server.getTimeFor(avail2 - System.currentTimeMillis()) + " until " + c.getHeSheItString() + " can cast " + this.name + " again.");
                }
                return true;
            }
        }
        return false;
    }

    public final void touchCooldown(Creature performer) {
        if (this.cooldown > 0L) {
            Cooldowns cd = Cooldowns.getCooldownsFor(performer.getWurmId(), true);
            cd.addCooldown(this.number, System.currentTimeMillis() + this.cooldown, false);
        }
    }

    public static double modifyDamage(Creature target, double damage) {
        if (!target.isPlayer()) {
            double armourMult = target.getArmourMod() * 2.0f;
            double bodyStrengthWeight = target.getStrengthSkill() * 0.25;
            double soulStrengthWeight = target.getSoulStrengthVal() * 0.75;
            double strengthMult = (100.0 - (bodyStrengthWeight + soulStrengthWeight)) * 0.02;
            double damageMult = armourMult * strengthMult;
            double clampedMult = damageMult / (1.0 + damageMult / 3.0);
            damage *= clampedMult;
        }
        return damage;
    }

    protected boolean checkFavorRequirements(Creature performer, float baseCost) {
        if (this.isReligious()) {
            if (baseCost < 15.0f && performer.getFavor() < baseCost) {
                return true;
            }
            if (baseCost < 200.0f && performer.getFavor() < baseCost * 0.33f && performer.getFavor() < 15.0f) {
                return true;
            }
        }
        return false;
    }

    protected Skill getCastingSkill(Creature performer) {
        if (this.religious) {
            return performer.getChannelingSkill();
        }
        return performer.getMindLogical();
    }

    protected boolean canCastSpell(Creature performer) {
        if (this.isReligious()) {
            return performer.isPriest() || performer.getPower() > 0;
        }
        return performer.knowsKarmaSpell(this.getNumber());
    }

    private boolean isCastValid(Creature performer, boolean validTarget, String targetName, int tileX, int tileY, boolean onSurface) {
        if (!this.canCastSpell(performer)) {
            performer.getCommunicator().sendNormalServerMessage("You cannot cast that spell.");
            return false;
        }
        if (this.isReligiousSpell() && !validTarget) {
            performer.getCommunicator().sendNormalServerMessage("You cannot cast " + this.getName() + " on " + targetName + ".");
            return false;
        }
        if (this.stillCooldown(performer)) {
            return false;
        }
        if (performer.attackingIntoIllegalDuellingRing(tileX, tileY, onSurface)) {
            performer.getCommunicator().sendNormalServerMessage("The duelling ring is holy ground and casting is restricted across the border.");
            return true;
        }
        return true;
    }

    public final boolean isValidItemType(Creature performer, Item target) {
        if (this.isTargetItem()) {
            return true;
        }
        if (this.isTargetArmour() && target.isArmour()) {
            return true;
        }
        if (this.isTargetWeapon() && (target.isWeapon() || target.isWeaponBow() || target.isBowUnstringed() || target.isArrow())) {
            return true;
        }
        if (this.isTargetJewelry() && target.isEnchantableJewelry()) {
            return true;
        }
        if (this.isTargetPendulum() && target.getTemplateId() == 233) {
            return true;
        }
        EnchantUtil.sendInvalidTargetMessage(performer, this);
        return false;
    }

    public final boolean run(Creature performer, Item target, float counter) {
        Skill sp;
        boolean done = false;
        if (!this.isCastValid(performer, target.isBodyPart() ? this.isTargetCreature() : this.isTargetAnyItem(), target.getNameWithGenus(), target.getTileX(), target.getTileY(), target.isOnSurface())) {
            return true;
        }
        if (!this.isValidItemType(performer, target)) {
            return true;
        }
        if (target.getTemplateId() == 669) {
            performer.getCommunicator().sendNormalServerMessage("You cannot cast " + this.getName() + " on the bulk item.");
            return true;
        }
        if (target.isMagicContainer()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot cast " + this.getName() + " on the " + target.getName() + ".");
            return true;
        }
        Skill castSkill = this.getCastingSkill(performer);
        if (!this.precondition(castSkill, performer, target)) {
            return true;
        }
        float baseCost = this.getCost(target);
        if (performer.getPower() >= 5 && Servers.isThisATestServer()) {
            baseCost = 1.0f;
        }
        float needed = baseCost;
        if (this.religious) {
            if (performer.isRoyalPriest()) {
                needed *= 0.5f;
            }
            if (performer.getFavorLinked() < needed) {
                performer.getCommunicator().sendNormalServerMessage("You need more favor with your god to cast that spell.");
                return true;
            }
        } else if ((float)performer.getKarma() < needed) {
            performer.getCommunicator().sendNormalServerMessage("You need more karma to use that ability.");
            return true;
        }
        if (counter == 1.0f && this.checkFavorRequirements(performer, baseCost)) {
            performer.getCommunicator().sendNormalServerMessage("You need more favor from your god to cast that spell.");
            return true;
        }
        double power = 0.0;
        if (counter == 1.0f && this.getCastingTime(performer) > 1) {
            if (this.isItemEnchantment() && performer.isChampion()) {
                if (((Player)performer).getChampionPoints() <= 0) {
                    performer.getCommunicator().sendNormalServerMessage("You will need to spend one Champion point in order to enchant items.");
                    return true;
                }
                performer.getCommunicator().sendAlertServerMessage("You will spend one champion point if you successfully enchant the item!");
            }
            performer.setStealth(false);
            performer.getCommunicator().sendNormalServerMessage("You start to cast '" + this.name + "' on " + target.getNameWithGenus() + ".");
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " starts to cast '" + this.name + "' on " + target.getNameWithGenus() + ".", performer, 5, this.shouldMessageCombat());
            performer.sendActionControl(Actions.actionEntrys[122].getVerbString(), true, this.getCastingTime(performer) * 10);
        }
        int speedMod = 0;
        if (!this.religious && (sp = performer.getMindSpeed()) != null) {
            speedMod = (int)(sp.getKnowledge(0.0) / 25.0);
        }
        if (counter >= (float)(this.getCastingTime(performer) - speedMod) || counter > 2.0f && performer.getPower() == 5) {
            done = true;
            boolean limitFail = false;
            if (this.isOffensive() && performer.getArmourLimitingFactor() < 0.0f && Server.rand.nextFloat() < Math.abs(performer.getArmourLimitingFactor())) {
                limitFail = true;
            }
            float bonus = 0.0f;
            if (!this.religious) {
                Skill sp2 = performer.getMindSpeed();
                if (sp2 != null) {
                    sp2.skillCheck(this.getDifficulty(true), performer.zoneBonus, false, counter);
                }
            } else {
                bonus = Math.abs(performer.getAlignment()) - 49.0f;
            }
            int rdDiff = 0;
            if (performer.mustChangeTerritory()) {
                bonus -= 50.0f;
                rdDiff = 20;
            } else if (target.isCrystal() && !target.isGem()) {
                bonus += 100.0f;
            }
            if (performer.getCitizenVillage() != null) {
                bonus += performer.getCitizenVillage().getFaithCreateBonus();
            }
            boolean dryRun = false;
            float modifier = 1.0f;
            if (target.getSpellEffects() != null) {
                modifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_ENCHANTABILITY);
            }
            if (bonus > 0.0f) {
                bonus *= 1.0f + performer.getArmourLimitingFactor();
            }
            power = Spell.trimPower(performer, castSkill.skillCheck(this.getDifficulty(true) + rdDiff + performer.getNumLinks() * 3, (performer.zoneBonus + bonus) * modifier, false, counter));
            if (limitFail) {
                power = -30.0f + Server.rand.nextFloat() * 29.0f;
            }
            if (power >= 0.0) {
                this.touchCooldown(performer);
                if (power >= 95.0) {
                    performer.achievement(629);
                }
                performer.getCommunicator().sendNormalServerMessage("You cast '" + this.name + "' on " + target.getNameWithGenus() + ".");
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " casts '" + this.name + "' on " + target.getNameWithGenus() + ".", performer, 5, this.shouldMessageCombat());
                if (this.religious) {
                    if (!this.postcondition(castSkill, performer, target, power)) {
                        try {
                            performer.depleteFavor(baseCost / 20.0f, this.isOffensive());
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, performer.getName(), iox);
                            performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                            return true;
                        }
                    } else {
                        try {
                            performer.depleteFavor(needed, this.isOffensive());
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, performer.getName(), iox);
                            performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                            return true;
                        }
                    }
                } else {
                    performer.modifyKarma((int)(-needed));
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Success Cost:" + needed + ", Power:" + power + ", SpeedMod:" + speedMod + ", Bonus:" + bonus);
                }
                this.doEffect(castSkill, power, performer, target);
                if (this.isItemEnchantment()) {
                    performer.achievement(606);
                    if (performer.isChampion()) {
                        performer.modifyChampionPoints(-1);
                    }
                }
            } else {
                if (this.religious) {
                    if (performer.mustChangeTerritory() && performer.isPlayer() && Server.rand.nextInt(3) == 0) {
                        performer.getCommunicator().sendAlertServerMessage("You sense a lack of energy. Rumours have it that " + performer.getDeity().getName() + " wants " + performer.getDeity().getHisHerItsString() + " champions to move between kingdoms and seek out the enemy.");
                    }
                    performer.getCommunicator().sendNormalServerMessage("You fail to channel the '" + this.name + "'.");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails to channel the '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
                    try {
                        performer.depleteFavor(baseCost / 5.0f, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The '" + this.name + "' fizzles!");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fizzles " + performer.getHisHerItsString() + " '" + this.name + "'!", performer, 5, this.shouldMessageCombat());
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Fail Cost:" + needed + ", Power:" + power);
                }
                this.doNegativeEffect(castSkill, power, performer, target);
            }
        }
        return done;
    }

    public final boolean run(Creature performer, Creature target, float counter) {
        Skill sp;
        boolean done = false;
        if (target.isDead()) {
            return true;
        }
        if (!this.isCastValid(performer, this.targetCreature, target.getNameWithGenus(), target.getTileX(), target.getTileY(), target.isOnSurface())) {
            return true;
        }
        Skill castSkill = this.getCastingSkill(performer);
        if (!this.precondition(castSkill, performer, target)) {
            return true;
        }
        float baseCost = this.getCost(target);
        if (performer.getPower() >= 5 && Servers.isThisATestServer()) {
            baseCost = 1.0f;
        }
        float needed = baseCost;
        if (this.religious) {
            if (performer.isRoyalPriest()) {
                needed *= 0.5f;
            }
            if (performer.getFavorLinked() < needed) {
                performer.getCommunicator().sendNormalServerMessage("You need more favor with your god to cast that spell.");
                return true;
            }
        } else if ((float)performer.getKarma() < needed) {
            performer.getCommunicator().sendNormalServerMessage("You need more karma to use that ability.");
            return true;
        }
        if (counter == 1.0f) {
            if (this.checkFavorRequirements(performer, baseCost)) {
                performer.getCommunicator().sendNormalServerMessage("You need more favor from your god to cast that spell.");
                return true;
            }
            if (this.offensive) {
                if ((performer.opponent != null || target.isAggHuman()) && performer.opponent == null) {
                    performer.setOpponent(target);
                }
                if (target.opponent == null) {
                    target.setOpponent(performer);
                    target.setTarget(performer.getWurmId(), false);
                    target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " is attacking you with a spell!");
                }
                target.addAttacker(performer);
            }
        }
        double power = 0.0;
        int speedMod = 0;
        if (counter == 1.0f && this.getCastingTime(performer) > 1) {
            performer.setStealth(false);
            if (performer == target) {
                performer.getCommunicator().sendNormalServerMessage("You start to cast '" + this.name + "' on yourself.");
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " starts to cast '" + this.name + "' on " + target.getHimHerItString() + "self.", performer, 5, this.shouldMessageCombat());
            } else {
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new CreatureLineSegment(performer));
                segments.add(new MulticolorLineSegment(" starts to cast " + this.name + " on ", 0));
                segments.add(new CreatureLineSegment(target));
                segments.add(new MulticolorLineSegment(".", 0));
                MessageServer.broadcastColoredAction(segments, performer, 5, this.shouldMessageCombat());
                if (this.offensive || this.number == 450) {
                    target.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
                }
                segments.get(1).setText(" start to cast " + this.name + " on ");
                if (this.shouldMessageCombat()) {
                    performer.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
                } else {
                    performer.getCommunicator().sendColoredMessageEvent(segments);
                }
            }
            performer.sendActionControl(Actions.actionEntrys[122].getVerbString(), true, this.getCastingTime(performer) * 10);
        }
        if (!this.isReligious() && (sp = performer.getMindSpeed()) != null) {
            speedMod = (int)(sp.getKnowledge(0.0) / 25.0);
        }
        if (counter >= (float)(this.getCastingTime(performer) - speedMod) || counter > 2.0f && performer.getPower() == 5) {
            done = true;
            double resist = 0.0;
            double attbonus = 0.0;
            if (!Zones.interruptedRange(performer, target)) {
                Skill sp2;
                boolean limitFail = false;
                if (this.isOffensive() && performer.getArmourLimitingFactor() < 0.0f && Server.rand.nextFloat() < Math.abs(performer.getArmourLimitingFactor())) {
                    limitFail = true;
                }
                target.setStealth(false);
                if (!this.isReligious() && (sp2 = performer.getMindSpeed()) != null) {
                    sp2.skillCheck(this.difficulty, performer.zoneBonus, false, counter);
                }
                if (this.isOffensive()) {
                    Battle battle;
                    target.addAttacker(performer);
                    if (performer.isPlayer() && target.isPlayer() && (battle = Battles.getBattleFor(performer, target)) != null) {
                        battle.addEvent(new BattleEvent(114, performer.getName(), target.getName(), performer.getName() + " casts " + this.getName() + " at " + target.getName() + "."));
                    }
                    int defSkill = 105;
                    int attSkill = 105;
                    if (this.dominate) {
                        try {
                            float extraDiff = 0.0f;
                            if (target.isUnique()) {
                                extraDiff = target.getBaseCombatRating();
                            }
                            if ((attbonus = performer.getSkills().getSkill(attSkill).skillCheck(this.difficulty, performer.zoneBonus, false, counter)) > 0.0) {
                                attbonus *= (double)(1.0f + performer.getArmourLimitingFactor());
                            }
                            power = Spell.trimPower(performer, castSkill.skillCheck((double)(1.0f + ItemBonus.getSpellResistBonus(target)) * (target.getSkills().getSkill(defSkill).getKnowledge(0.0) + (double)target.getStatus().getBattleRatingTypeModifier() + (double)(performer.getNumLinks() * 3) + (double)extraDiff), (double)performer.zoneBonus + attbonus, false, counter));
                        }
                        catch (NoSuchSkillException nss) {
                            performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " seems impossible to dominate.");
                            logger.log(Level.WARNING, nss.getMessage(), nss);
                        }
                    } else {
                        float abon = 0.0f;
                        float defbon = 0.0f;
                        if (performer.getEnemyPresense() > 1200 && target.isPlayer()) {
                            abon = 20.0f;
                        }
                        if (target.getEnemyPresense() > 1200 && performer.isPlayer()) {
                            defbon = 20.0f;
                        }
                        if (!this.religious) {
                            attSkill = 101;
                            defSkill = 101;
                        }
                        try {
                            resist = target.getSkills().getSkill(defSkill).skillCheck(this.difficulty, defbon, false, counter);
                        }
                        catch (NoSuchSkillException nss) {
                            logger.log(Level.WARNING, target.getName() + " learning defskill " + defSkill, nss);
                            if (target.isPlayer()) {
                                target.getSkills().learn(defSkill, 20.0f);
                            }
                            target.getSkills().learn(defSkill, 99.99f);
                        }
                        try {
                            attbonus = resist > 0.0 ? performer.getSkills().getSkill(attSkill).skillCheck(resist, abon, false, counter) : (double)(10.0f + abon);
                        }
                        catch (NoSuchSkillException nss) {
                            logger.log(Level.WARNING, performer.getName() + " learning attskill " + attSkill, nss);
                            performer.getSkills().learn(attSkill, 1.0f);
                        }
                    }
                    if (!target.isPlayer()) {
                        if (!performer.isInvulnerable()) {
                            target.setTarget(performer.getWurmId(), false);
                        }
                        target.setFleeCounter(20);
                    }
                }
                float bonus = 0.0f;
                if (this.religious) {
                    bonus = Math.abs(performer.getAlignment()) - 49.0f;
                }
                if (bonus > 0.0f) {
                    bonus *= 1.0f + performer.getArmourLimitingFactor();
                }
                double distDiff = 0.0;
                if (this.isOffensive() || this.getNumber() == 450) {
                    double dist = Creature.getRange(performer, target.getPosX(), target.getPosY());
                    try {
                        distDiff = dist - (double)((float)Actions.actionEntrys[this.number].getRange() / 2.0f);
                        if (distDiff > 0.0) {
                            distDiff *= 2.0;
                        }
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, this.getName() + " error: " + ex.getMessage());
                    }
                }
                if (!this.dominate) {
                    power = Spell.trimPower(performer, Math.max((double)(Server.rand.nextFloat() * 10.0f), castSkill.skillCheck((double)(1.0f + ItemBonus.getSpellResistBonus(target)) * (distDiff + (double)this.difficulty + (double)(performer.getNumLinks() * 3)), (double)performer.zoneBonus + attbonus + (double)bonus, false, counter)));
                }
                if (limitFail) {
                    power = -30.0f + Server.rand.nextFloat() * 29.0f;
                }
            }
            if (power > 0.0) {
                this.touchCooldown(performer);
                Methods.sendSound(performer, "sound.religion.channel");
                if (power >= 95.0) {
                    performer.achievement(629);
                }
                if (performer == target) {
                    performer.getCommunicator().sendNormalServerMessage("You cast '" + this.name + "' on yourself.");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " casts '" + this.name + "' on " + target.getHimHerItString() + "self.", performer, 5, this.shouldMessageCombat());
                } else {
                    ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                    segments.add(new CreatureLineSegment(performer));
                    segments.add(new MulticolorLineSegment(" casts " + this.name + " on ", 0));
                    segments.add(new CreatureLineSegment(target));
                    segments.add(new MulticolorLineSegment(".", 0));
                    MessageServer.broadcastColoredAction(segments, performer, 5, this.shouldMessageCombat());
                    if (this.offensive || this.number == 450) {
                        target.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
                    }
                    segments.get(1).setText(" cast " + this.name + " on ");
                    if (this.shouldMessageCombat()) {
                        performer.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
                    } else {
                        performer.getCommunicator().sendColoredMessageEvent(segments);
                    }
                }
                if (Constants.devmode) {
                    performer.getCommunicator().sendNormalServerMessage("Power=" + power);
                }
                if (this.religious) {
                    try {
                        performer.depleteFavor(needed, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                }
                performer.modifyKarma((int)(-needed));
                if (!performer.isPlayer()) {
                    try {
                        performer.depleteFavor(100.0f, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                    }
                }
                boolean eff = true;
                if (this.isOffensive() && target.getCultist() != null && target.getCultist().ignoresSpells()) {
                    eff = false;
                    ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                    segments.add(new CreatureLineSegment(target));
                    segments.add(new MulticolorLineSegment(" ignores the effects!", 0));
                    MessageServer.broadcastColoredAction(segments, performer, target, 5, true);
                    if (this.shouldMessageCombat()) {
                        performer.getCommunicator().sendColoredMessageCombat(segments);
                    } else {
                        performer.getCommunicator().sendColoredMessageEvent(segments);
                    }
                    segments.get(1).setText(" ignore the effects!");
                    if (this.shouldMessageCombat()) {
                        target.getCommunicator().sendColoredMessageCombat(segments);
                    } else {
                        target.getCommunicator().sendColoredMessageEvent(segments);
                    }
                }
                if (eff) {
                    if (Servers.isThisATestServer()) {
                        performer.getCommunicator().sendNormalServerMessage("Success Cost:" + needed + ", Power:" + power + ", SpeedMod:" + speedMod + ", Bonus:" + attbonus);
                    }
                    this.doEffect(castSkill, power, performer, target);
                }
            } else {
                if (this.isReligious()) {
                    performer.getCommunicator().sendNormalServerMessage("You fail to channel the '" + this.name + "'.");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails to channel the '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
                    try {
                        performer.depleteFavor(baseCost / 20.0f, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The '" + this.name + "' fails!");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails " + performer.getHisHerItsString() + " '" + this.name + "'!", performer, 5, this.shouldMessageCombat());
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Fail Cost:" + needed + ", Power:" + power);
                }
                this.doNegativeEffect(castSkill, power, performer, target);
            }
        }
        return done;
    }

    public final boolean run(Creature performer, Wound target, float counter) {
        Skill sp;
        boolean done = false;
        if (!this.isCastValid(performer, this.targetWound, target.getName(), target.getCreature().getTileX(), target.getCreature().getTileY(), target.getCreature().isOnSurface())) {
            return true;
        }
        Skill castSkill = this.getCastingSkill(performer);
        if (!this.precondition(castSkill, performer, target)) {
            return true;
        }
        float baseCost = this.getCost(target);
        if (performer.getPower() >= 5 && Servers.isThisATestServer()) {
            baseCost = 1.0f;
        }
        float needed = baseCost;
        if (this.religious) {
            if (performer.isRoyalPriest()) {
                needed *= 0.5f;
            }
            if (performer.getFavorLinked() < needed) {
                performer.getCommunicator().sendNormalServerMessage("You need more favor with your god to cast that spell.");
                return true;
            }
        } else if ((float)performer.getKarma() < needed) {
            performer.getCommunicator().sendNormalServerMessage("You need more karma to use that ability.");
            return true;
        }
        if (target.getCreature() == null) {
            performer.getCommunicator().sendNormalServerMessage("You fail to get a clear line of sight.");
            return true;
        }
        if (counter == 1.0f && this.checkFavorRequirements(performer, baseCost)) {
            performer.getCommunicator().sendNormalServerMessage("You need more favor from your god to cast that spell.");
            return true;
        }
        double power = 0.0;
        if (counter == 1.0f && this.getCastingTime(performer) > 1) {
            performer.setStealth(false);
            performer.getCommunicator().sendNormalServerMessage("You start to cast '" + this.name + "' on the wound.");
            if (target.getCreature() != null) {
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " starts to cast '" + this.name + "' on " + target.getCreature().getName() + ".", performer, 5, this.shouldMessageCombat());
            }
            performer.sendActionControl(Actions.actionEntrys[122].getVerbString(), true, this.getCastingTime(performer) * 10);
        }
        int speedMod = 0;
        if (!this.isReligious() && (sp = performer.getMindSpeed()) != null) {
            speedMod = (int)(sp.getKnowledge(0.0) / 25.0);
        }
        if (counter >= (float)(this.getCastingTime(performer) - speedMod) || counter > 2.0f && performer.getPower() == 5) {
            done = true;
            boolean limitFail = false;
            if (this.isOffensive() && performer.getArmourLimitingFactor() < 0.0f && Server.rand.nextFloat() < Math.abs(performer.getArmourLimitingFactor())) {
                limitFail = true;
            }
            float bonus = 0.0f;
            if (!this.religious) {
                Skill sp2 = performer.getMindSpeed();
                if (sp2 != null) {
                    sp2.skillCheck(this.difficulty, performer.zoneBonus, false, counter);
                }
            } else {
                bonus = Math.abs(performer.getAlignment()) - 49.0f;
            }
            if (bonus > 0.0f) {
                bonus *= 1.0f + performer.getArmourLimitingFactor();
            }
            power = Spell.trimPower(performer, Math.max((double)(Server.rand.nextFloat() * 10.0f), castSkill.skillCheck(this.difficulty + performer.getNumLinks() * 3, performer.zoneBonus + bonus, false, counter)));
            if (limitFail) {
                power = -30.0f + Server.rand.nextFloat() * 29.0f;
            }
            if (power >= 0.0) {
                Battle battle;
                this.touchCooldown(performer);
                if (power >= 95.0) {
                    performer.achievement(629);
                }
                if (target.getCreature() != null) {
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " casts '" + this.name + "' on " + target.getCreature().getName() + ".", performer, 5, this.shouldMessageCombat());
                }
                if ((battle = performer.getBattle()) != null) {
                    battle.addEvent(new BattleEvent(114, performer.getName(), target.getName(), performer.getName() + " casts '" + this.name + "' on " + target.getCreature().getName() + "."));
                }
                if (this.religious) {
                    try {
                        performer.depleteFavor(needed, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else {
                    performer.modifyKarma((int)(-needed));
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Success Cost:" + needed + ", Power:" + power + ", SpeedMod:" + speedMod + ", Bonus:" + bonus);
                }
                this.doEffect(castSkill, power, performer, target);
            } else {
                if (this.isReligious()) {
                    performer.getCommunicator().sendNormalServerMessage("You fail to channel the '" + this.name + "'.");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails to channel the '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
                    try {
                        performer.depleteFavor(baseCost / 20.0f, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The '" + this.name + "' fails!");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails " + performer.getHisHerItsString() + " '" + this.name + "'!", performer, 5, this.shouldMessageCombat());
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Fail Cost:" + needed + ", Power:" + power);
                }
            }
        }
        return done;
    }

    public final boolean run(Creature performer, int tilexborder, int tileyborder, int layer, int heightOffset, Tiles.TileBorderDirection dir, float counter) {
        Skill sp;
        boolean done = false;
        if (!this.isCastValid(performer, this.targetTileBorder, "that border", tilexborder, tileyborder, layer >= 0)) {
            return true;
        }
        Skill castSkill = this.getCastingSkill(performer);
        if (!this.precondition(castSkill, performer, tilexborder, tileyborder, layer, heightOffset, dir)) {
            return true;
        }
        float baseCost = this.getCost(tilexborder, tileyborder, layer, heightOffset, dir);
        if (performer.getPower() >= 5 && Servers.isThisATestServer()) {
            baseCost = 1.0f;
        }
        float needed = baseCost;
        if (this.isReligious()) {
            if (performer.isRoyalPriest()) {
                needed *= 0.5f;
            }
            if (performer.getFavorLinked() < needed) {
                performer.getCommunicator().sendNormalServerMessage("You need more favor with your god to cast that spell.");
                return true;
            }
        } else if (performer.getPower() <= 1 && (float)performer.getKarma() < needed) {
            performer.getCommunicator().sendNormalServerMessage("You need more karma to use that ability.");
            return true;
        }
        if (counter == 1.0f && this.checkFavorRequirements(performer, baseCost)) {
            performer.getCommunicator().sendNormalServerMessage("You need more favor from your god to cast that spell.");
            return true;
        }
        double power = 0.0;
        if (counter == 1.0f && this.getCastingTime(performer) > 1) {
            performer.setStealth(false);
            performer.getCommunicator().sendNormalServerMessage("You start to cast '" + this.name + "'.");
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " starts to cast '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
            performer.sendActionControl(Actions.actionEntrys[122].getVerbString(), true, this.getCastingTime(performer) * 10);
        }
        int speedMod = 0;
        if (!this.isReligious() && (sp = performer.getMindSpeed()) != null) {
            speedMod = (int)(sp.getKnowledge(0.0) / 25.0);
        }
        if (counter >= (float)(this.getCastingTime(performer) - speedMod) || counter > 2.0f && performer.getPower() == 5) {
            done = true;
            boolean limitFail = false;
            if (this.isOffensive() && performer.getArmourLimitingFactor() < 0.0f && Server.rand.nextFloat() < Math.abs(performer.getArmourLimitingFactor())) {
                limitFail = true;
            }
            float bonus = 0.0f;
            if (!this.isReligious()) {
                Skill sp2 = performer.getMindSpeed();
                if (sp2 != null) {
                    sp2.skillCheck(this.difficulty, performer.zoneBonus, false, counter);
                }
            } else {
                bonus = Math.abs(performer.getAlignment()) - 49.0f;
            }
            if (bonus > 0.0f) {
                bonus *= 1.0f + performer.getArmourLimitingFactor();
            }
            double distDiff = 0.0;
            if (this.isOffensive() || this.getNumber() == 450) {
                double dist = 4.0 * Creature.getTileRange(performer, tilexborder, tileyborder);
                try {
                    distDiff = dist - (double)((float)Actions.actionEntrys[this.number].getRange() / 2.0f);
                    if (distDiff > 0.0) {
                        distDiff *= 2.0;
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, this.getName() + " error: " + ex.getMessage());
                }
            }
            power = Spell.trimPower(performer, Math.max((double)(Server.rand.nextFloat() * 10.0f), castSkill.skillCheck(distDiff + (double)this.difficulty + (double)(performer.getNumLinks() * 3), performer.zoneBonus + bonus, false, counter)));
            if (limitFail) {
                power = -30.0f + Server.rand.nextFloat() * 29.0f;
            }
            if (power >= 0.0) {
                if (performer.getPower() <= 1) {
                    this.touchCooldown(performer);
                }
                if (power >= 95.0) {
                    performer.achievement(629);
                }
                Server.getInstance().broadCastAction(performer.getNameWithGenus() + " casts '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
                performer.getCommunicator().sendNormalServerMessage("You succeed.");
                if (this.isReligious()) {
                    try {
                        performer.depleteFavor(needed, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else if (performer.getPower() <= 1) {
                    performer.modifyKarma((int)(-needed));
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Success Cost:" + needed + ", Power:" + power + ", SpeedMod:" + speedMod + ", Bonus:" + bonus);
                }
                this.doEffect(castSkill, power, performer, tilexborder, tileyborder, layer, heightOffset, dir);
            } else {
                if (this.religious) {
                    performer.getCommunicator().sendNormalServerMessage("You fail to channel the '" + this.name + "'.");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails to channel the '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
                    try {
                        performer.depleteFavor(baseCost / 20.0f, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The '" + this.name + "' fails!");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails " + performer.getHisHerItsString() + " '" + this.name + "'!", performer, 5, this.shouldMessageCombat());
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Fail Cost:" + needed + ", Power:" + power);
                }
            }
        }
        return done;
    }

    public final boolean run(Creature performer, int tilex, int tiley, int layer, int heightOffset, float counter) {
        Skill sp;
        boolean done = false;
        if (!this.isCastValid(performer, this.targetTile, "that tile", tilex, tiley, layer >= 0)) {
            return true;
        }
        Skill castSkill = this.getCastingSkill(performer);
        if (!this.precondition(castSkill, performer, tilex, tiley, layer)) {
            return true;
        }
        float baseCost = this.getCost(tilex, tiley, layer, heightOffset);
        if (performer.getPower() >= 5 && Servers.isThisATestServer()) {
            baseCost = 1.0f;
        }
        float needed = baseCost;
        if (this.isReligious()) {
            if (performer.isRoyalPriest()) {
                needed *= 0.5f;
            }
            if (performer.getFavorLinked() < needed) {
                performer.getCommunicator().sendNormalServerMessage("You need more favor with your god to cast that spell.");
                return true;
            }
        } else if (performer.getPower() <= 1 && (float)performer.getKarma() < needed) {
            performer.getCommunicator().sendNormalServerMessage("You need more karma to use that ability.");
            return true;
        }
        if (counter == 1.0f && this.checkFavorRequirements(performer, baseCost)) {
            performer.getCommunicator().sendNormalServerMessage("You need more favor from your god to cast that spell.");
            return true;
        }
        double power = 0.0;
        if (counter == 1.0f && this.getCastingTime(performer) > 1) {
            performer.setStealth(false);
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(performer));
            segments.add(new MulticolorLineSegment(" starts to cast " + this.getName() + ".", 0));
            MessageServer.broadcastColoredAction(segments, performer, null, 5, this.shouldMessageCombat(), (byte)2);
            segments.get(1).setText(" start to cast " + this.getName() + ".");
            if (this.shouldMessageCombat()) {
                performer.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
            } else {
                performer.getCommunicator().sendColoredMessageEvent(segments);
            }
            performer.sendActionControl(Actions.actionEntrys[122].getVerbString(), true, this.getCastingTime(performer) * 10);
        }
        int speedMod = 0;
        if (!this.isReligious() && (sp = performer.getMindSpeed()) != null) {
            speedMod = (int)(sp.getKnowledge(0.0) / 25.0);
        }
        if (counter >= (float)(this.getCastingTime(performer) - speedMod) || counter > 2.0f && performer.getPower() == 5) {
            done = true;
            boolean limitFail = false;
            if (this.isOffensive() && performer.getArmourLimitingFactor() < 0.0f && Server.rand.nextFloat() < Math.abs(performer.getArmourLimitingFactor())) {
                limitFail = true;
            }
            float bonus = 0.0f;
            if (!this.isReligious()) {
                Skill sp2 = performer.getMindSpeed();
                if (sp2 != null) {
                    sp2.skillCheck(this.difficulty, performer.zoneBonus, false, counter);
                }
            } else {
                bonus = Math.abs(performer.getAlignment()) - 49.0f;
            }
            if (bonus > 0.0f) {
                bonus *= 1.0f + performer.getArmourLimitingFactor();
            }
            double distDiff = 0.0;
            if (this.isOffensive() || this.getNumber() == 450) {
                double dist = 4.0 * Creature.getTileRange(performer, tilex, tiley);
                try {
                    distDiff = dist - (double)((float)Actions.actionEntrys[this.number].getRange() / 2.0f);
                    if (distDiff > 0.0) {
                        distDiff *= 2.0;
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, this.getName() + " error: " + ex.getMessage());
                }
            }
            power = Spell.trimPower(performer, Math.max((double)(Server.rand.nextFloat() * 10.0f), castSkill.skillCheck(distDiff + (double)this.difficulty + (double)(performer.getNumLinks() * 3), performer.zoneBonus + bonus, false, counter)));
            if (limitFail) {
                power = -30.0f + Server.rand.nextFloat() * 29.0f;
            }
            if (power >= 0.0) {
                if (performer.getPower() <= 1) {
                    this.touchCooldown(performer);
                }
                if (power >= 95.0) {
                    performer.achievement(629);
                }
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new CreatureLineSegment(performer));
                segments.add(new MulticolorLineSegment(" casts " + this.getName() + ".", 0));
                MessageServer.broadcastColoredAction(segments, performer, null, 5, this.shouldMessageCombat(), (byte)2);
                segments.get(1).setText(" cast " + this.getName() + ".");
                if (this.shouldMessageCombat()) {
                    performer.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
                } else {
                    performer.getCommunicator().sendColoredMessageEvent(segments);
                }
                if (this.isReligious()) {
                    try {
                        performer.depleteFavor(needed, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else if (performer.getPower() <= 1) {
                    performer.modifyKarma((int)(-needed));
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Success Cost:" + needed + ", Power:" + power + ", SpeedMod:" + speedMod + ", Bonus:" + bonus);
                }
                this.doEffect(castSkill, power, performer, tilex, tiley, layer, heightOffset);
            } else {
                if (this.isReligious()) {
                    performer.getCommunicator().sendNormalServerMessage("You fail to channel the '" + this.name + "'.");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails to channel the '" + this.name + "'.", performer, 5, this.shouldMessageCombat());
                    try {
                        performer.depleteFavor(baseCost / 20.0f, this.isOffensive());
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName(), iox);
                        performer.getCommunicator().sendNormalServerMessage("The spell fizzles!");
                        return true;
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The '" + this.name + "' fails!");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " fails " + performer.getHisHerItsString() + " '" + this.name + "'!", performer, 5, this.shouldMessageCombat());
                }
                if (Servers.isThisATestServer()) {
                    performer.getCommunicator().sendNormalServerMessage("Fail Cost:" + needed + ", Power:" + power);
                }
            }
        }
        return done;
    }

    private boolean shouldMessageCombat() {
        return this.offensive || this.karmaSpell || this.healing;
    }

    public void enchantItem(Creature performer, Item target, byte enchantment, float power) {
        SpellEffect eff;
        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null) {
            effs = new ItemSpellEffects(target.getWurmId());
        }
        if ((eff = effs.getSpellEffect(enchantment)) == null) {
            eff = new SpellEffect(target.getWurmId(), enchantment, power, 20000000);
            effs.addSpellEffect(eff);
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " " + this.getEffectdesc(), (byte)2);
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased.", performer, 5);
        } else if (eff.getPower() > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to improve the power.", (byte)3);
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " frowns.", performer, 5);
        } else {
            eff.improvePower(performer, power);
            performer.getCommunicator().sendNormalServerMessage("You succeed in improving the power of the " + this.getName() + ".", (byte)2);
            Server.getInstance().broadCastAction(performer.getNameWithGenus() + " looks pleased.", performer, 5);
        }
    }

    public static final boolean mayArmourBeEnchanted(Item target, @Nullable Creature performer, byte enchantment) {
        if (!Spell.mayBeEnchanted(target)) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.");
            }
            return false;
        }
        if (enchantment != 17 && target.getSpellPainShare() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 46 && target.getSpellSlowdown() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        return true;
    }

    public static final boolean mayReceiveSkillgainBuff(Item target, @Nullable Creature performer, byte enchantment) {
        if (!Spell.mayBeEnchanted(target)) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.");
            }
            return false;
        }
        if (enchantment != 47) {
            if (target.getBonusForSpellEffect((byte)47) > 0.0f) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
                }
                return false;
            }
        } else if (target.getBonusForSpellEffect((byte)13) > 0.0f || target.getBonusForSpellEffect((byte)16) > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        return true;
    }

    public static final boolean mayWeaponBeEnchanted(Item target, @Nullable Creature performer, byte enchantment) {
        if (!Spell.mayBeEnchanted(target)) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.");
            }
            return false;
        }
        if (enchantment != 18 && target.getSpellRotModifier() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 26 && target.getSpellLifeTransferModifier() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 27 && target.getSpellVenomBonus() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 33 && target.getSpellFrostDamageBonus() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 45 && target.getSpellExtraDamageBonus() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 14 && target.getSpellDamageBonus() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        if (enchantment != 63 && target.getSpellEssenceDrainModifier() > 0.0f) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted with something that would negate the effect.");
            }
            return false;
        }
        return true;
    }

    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
    }

    void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target) {
    }

    public void castSpell(double power, Creature performer, Item target) {
        if (this.precondition(performer.getMindLogical(), performer, target)) {
            this.doEffect(performer.getMindLogical(), power, performer, target);
        }
    }

    public void castSpell(double power, Creature performer, Creature target) {
        if (this.precondition(performer.getMindLogical(), performer, target)) {
            this.doEffect(performer.getMindLogical(), power, performer, target);
        }
    }

    public void castSpell(double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        if (this.precondition(performer.getMindLogical(), performer, tilex, tiley, layer)) {
            this.doEffect(performer.getMindLogical(), power, performer, tilex, tiley, layer, heightOffset);
        }
    }

    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
    }

    void doNegativeEffect(Skill castSkill, double power, Creature performer, Item target) {
    }

    void doEffect(Skill castSkill, double power, Creature performer, Wound target) {
    }

    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
    }

    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
    }

    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        return true;
    }

    boolean precondition(Skill castSkill, Creature performer, Item target) {
        return true;
    }

    boolean postcondition(Skill castSkill, Creature performer, Item target, double effect) {
        return true;
    }

    boolean precondition(Skill castSkill, Creature performer, Wound target) {
        return true;
    }

    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        return true;
    }

    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
        return true;
    }

    final int getNumber() {
        return this.number;
    }

    public final String getName() {
        return this.name;
    }

    final int getCastingTime(Creature performer) {
        SpellEffect eff;
        SpellEffects effs = performer.getSpellEffects();
        if (effs != null && (eff = effs.getSpellEffect((byte)93)) != null) {
            return (int)((float)this.castingTime * (1.0f + Math.max(30.0f, eff.getPower()) / 100.0f));
        }
        return this.castingTime;
    }

    final boolean isReligious() {
        return this.religious;
    }

    final boolean isKarmaSpell() {
        return this.karmaSpell;
    }

    final boolean isOffensive() {
        return this.offensive;
    }

    public boolean isCreatureItemEnchantment() {
        return this.isTargetCreature() && this.isTargetAnyItem() && this.getEnchantment() != 0;
    }

    public boolean isItemEnchantment() {
        return this.isTargetAnyItem() && this.getEnchantment() != 0;
    }

    public final int getCost() {
        return this.cost;
    }

    public int getCost(Creature creature) {
        return this.cost;
    }

    public int getCost(Item item) {
        return this.cost;
    }

    public int getCost(Wound wound) {
        return this.getCost();
    }

    public int getCost(int tilexborder, int tileyborder, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
        return this.getCost();
    }

    public int getCost(int tilex, int tiley, int layer, int heightOffset) {
        return this.getCost();
    }

    public boolean isDynamicCost() {
        return this.hasDynamicCost;
    }

    public final int getDifficulty(boolean forItem) {
        if (forItem && this.isCreatureItemEnchantment()) {
            return this.difficulty * 2;
        }
        return this.difficulty;
    }

    public String getDescription() {
        return this.description;
    }

    final int getLevel() {
        return this.level;
    }

    static final Logger getLogger() {
        return logger;
    }

    public final boolean isTargetCreature() {
        return this.targetCreature;
    }

    public final boolean isTargetItem() {
        return this.targetItem;
    }

    public final boolean isTargetAnyItem() {
        return this.targetItem || this.targetWeapon || this.targetArmour || this.targetJewelry || this.targetPendulum;
    }

    public final boolean isTargetWound() {
        return this.targetWound;
    }

    public final boolean isTargetTile() {
        return this.targetTile;
    }

    public final boolean isTargetTileBorder() {
        return this.targetTileBorder;
    }

    public final boolean isTargetWeapon() {
        return this.targetWeapon;
    }

    public final boolean isTargetArmour() {
        return this.targetArmour;
    }

    public final boolean isTargetJewelry() {
        return this.targetJewelry;
    }

    public final boolean isTargetPendulum() {
        return this.targetPendulum;
    }

    final boolean isDominate() {
        return this.dominate;
    }

    public final byte getEnchantment() {
        return this.enchantment;
    }

    final String getEffectdesc() {
        return this.effectdesc;
    }

    public final boolean isChaplain() {
        return this.type == 1 || this.type == 0;
    }

    public final boolean isReligiousSpell() {
        return this.religious;
    }

    public final boolean isSorcerySpell() {
        return this.karmaSpell;
    }

    private float getMaterialShatterMod(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 56: {
                    return 0.15f;
                }
                case 57: {
                    return 0.25f;
                }
                case 7: {
                    return 0.2f;
                }
                case 67: {
                    return 1.0f;
                }
                case 8: {
                    return 0.1f;
                }
                case 96: {
                    return 0.15f;
                }
            }
        } else if (material == 67) {
            return 1.0f;
        }
        return 0.0f;
    }

    void checkDestroyItem(double power, Creature performer, Item target) {
        if (Server.rand.nextFloat() < this.getMaterialShatterMod(target.getMaterial())) {
            return;
        }
        ItemSpellEffects spellEffects = target.getSpellEffects();
        float chanceModifier = 1.0f;
        if (spellEffects != null) {
            chanceModifier = spellEffects.getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SHATTERRES);
        }
        if (power < (double)(-(target.getQualityLevel() * chanceModifier)) || power < 0.0 && (double)Server.rand.nextFloat() <= 0.01 / (double)chanceModifier) {
            SpellEffect eff;
            if (spellEffects != null && (eff = spellEffects.getSpellEffect((byte)98)) != null) {
                spellEffects.removeSpellEffect((byte)98);
                performer.getCommunicator().sendAlertServerMessage("The " + target.getName() + " emits a strong deep sound of resonance and starts to shatter, but the Metallic Liquid protects the " + target.getName() + "! The Metallic Liquid has dissipated.", (byte)3);
                Server.getInstance().broadCastAction("The " + target.getName() + " starts to shatter, but gets protected by a mystic substance!", performer, 5);
                return;
            }
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits a strong deep sound of resonance, then shatters!", (byte)3);
            Server.getInstance().broadCastAction("The " + target.getName() + " shatters!", performer, 5);
            Items.destroyItem(target.getWurmId());
            performer.achievement(627);
        } else if (power < (double)(-(target.getQualityLevel() * chanceModifier) / 3.0f)) {
            SpellEffect eff;
            if (spellEffects != null && (eff = spellEffects.getSpellEffect((byte)98)) != null) {
                eff.setPower(eff.getPower() - 20.0f);
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits a deep worrying sound of resonance, and a small crack wants to start forming, but the Metallic Liquid steps in and takes the damage instead!");
                if (eff.getPower() <= 0.0f) {
                    performer.getCommunicator().sendAlertServerMessage("The Metallic Liquid's strength has been depleted, and its protection has been removed from the " + target.getName());
                    spellEffects.removeSpellEffect((byte)98);
                }
                Server.getInstance().broadCastAction("The " + target.getName() + " starts to form cracks, but a mystic liquid protects it!", performer, 5);
                return;
            }
            target.setDamage(target.getDamage() + (float)Math.abs(power / 20.0));
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits a deep worrying sound of resonance, and a small crack starts to form on the surface.");
        } else {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits a deep worrying sound of resonance, but stays intact.");
        }
    }

    final List<Long> findBridgesInTheArea(int sx, int sy, int ex, int ey, int layer, int heightOffset, int groundHeight) {
        int actualHeight = groundHeight + heightOffset;
        ArrayList<Long> arr = new ArrayList<Long>();
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                Long id;
                Structure structure;
                VolaTile tile;
                if (x == 266 && y == 303) {
                    boolean f;
                    boolean bl = f = false;
                }
                if ((tile = Zones.getOrCreateTile(x, y, layer >= 0)) == null || (structure = tile.getStructure()) == null || !structure.isTypeBridge()) continue;
                float[] hts = Zones.getNodeHeights(x, y, layer, structure.getWurmId());
                float h = hts[0] * 0.5f * 0.5f + hts[1] * 0.5f * 0.5f + hts[2] * 0.5f * 0.5f + hts[3] * 0.5f * 0.5f;
                int closestHeight = -1000;
                int smallestDiff = 110;
                for (int i = 0; i < hts.length; ++i) {
                    int dec = (int)(hts[i] * 10.0f);
                    int diff = Math.abs(actualHeight - dec);
                    if (diff >= smallestDiff) continue;
                    smallestDiff = diff;
                    closestHeight = dec;
                }
                if (closestHeight <= -1000 || smallestDiff > 5 || arr.contains(id = Long.valueOf(structure.getWurmId()))) continue;
                arr.add(id);
            }
        }
        return arr;
    }

    final void calculateAOE(int sx, int sy, int ex, int ey, int tilex, int tiley, int layer, Structure playerStructure, Structure targetStructure, int heightOffset) {
        this.area = new boolean[1 + ex - sx][1 + ey - sy];
        this.offsets = new int[1 + ex - sx][1 + ey - sy];
        int groundHeight = 0;
        if (targetStructure == null || targetStructure.isTypeHouse()) {
            float[] hts = Zones.getNodeHeights(tilex, tiley, layer, -10L);
            float h = hts[0] * 0.5f * 0.5f + hts[1] * 0.5f * 0.5f + hts[2] * 0.5f * 0.5f + hts[3] * 0.5f * 0.5f;
            groundHeight = (int)(h * 10.0f);
        }
        List<Long> bridges = this.findBridgesInTheArea(sx, sy, ex, ey, layer, heightOffset, groundHeight);
        for (int x = sx; x <= ex; ++x) {
            block1: for (int y = sy; y <= ey; ++y) {
                VolaTile tile;
                Item ring = Zones.isWithinDuelRing(x, y, layer >= 0);
                if (ring != null || (tile = Zones.getOrCreateTile(x, y, layer >= 0)) == null) continue;
                Structure tileStructure = tile.getStructure();
                int currAreaX = x - sx;
                int currAreaY = y - sy;
                if (layer < 0) {
                    int ttile = Server.caveMesh.getTile(x, y);
                    byte ttype = Tiles.decodeType(ttile);
                    if (Tiles.decodeHeight(ttile) < 0) {
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (Tiles.isSolidCave(ttype)) {
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (x > tilex + 1) {
                        if (this.area[currAreaX - 1][currAreaY]) {
                            this.area[currAreaX][currAreaY] = true;
                        }
                    } else if (x < tilex - 1 && this.area[currAreaX + 1][currAreaY]) {
                        this.area[currAreaX][currAreaY] = true;
                    }
                    if (y < tiley - 1) {
                        if (!this.area[currAreaX][currAreaY + 1]) continue;
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (y <= tiley + 1 || !this.area[currAreaX][currAreaY - 1]) continue;
                    this.area[currAreaX][currAreaY] = true;
                    continue;
                }
                if (targetStructure != null && targetStructure.isTypeHouse()) {
                    if (tileStructure != null && tileStructure.getWurmId() == targetStructure.getWurmId()) {
                        boolean foundFloor = false;
                        for (Floor floor : tile.getFloors()) {
                            if (floor.getHeightOffset() != heightOffset) continue;
                            foundFloor = true;
                            break;
                        }
                        if (!foundFloor) {
                            this.area[currAreaX][currAreaY] = true;
                            continue;
                        }
                        this.offsets[currAreaX][currAreaY] = heightOffset + groundHeight;
                        continue;
                    }
                    if (tileStructure != null && tileStructure.isTypeBridge()) {
                        Long bridgeId = tileStructure.getWurmId();
                        if (!bridges.contains(bridgeId)) {
                            this.area[currAreaX][currAreaY] = true;
                            continue;
                        }
                        for (BridgePart bp : tileStructure.getBridgeParts()) {
                            if (bp.getTileX() != x || bp.getTileY() != y) continue;
                            this.offsets[currAreaX][currAreaY] = bp.getHeightOffset();
                            continue block1;
                        }
                        continue;
                    }
                    this.area[currAreaX][currAreaY] = true;
                    continue;
                }
                if (targetStructure != null && targetStructure.isTypeBridge()) {
                    if (tileStructure != null && tileStructure.isTypeHouse()) {
                        boolean foundConnection = false;
                        for (int xx = x - 1; xx <= x + 1; ++xx) {
                            for (int yy = y - 1; yy <= y + 1; ++yy) {
                                Structure s;
                                VolaTile t;
                                if (yy == y && xx == x || (t = Zones.getOrCreateTile(xx, yy, layer >= 0)) == null || (s = t.getStructure()) == null || s.getWurmId() != targetStructure.getWurmId()) continue;
                                foundConnection = true;
                                int bridgeH = 0;
                                for (BridgePart part : targetStructure.getBridgeParts()) {
                                    if (part.getTileX() != xx || part.getTileY() != yy) continue;
                                    bridgeH = part.getHeightOffset();
                                    break;
                                }
                                float[] hts = Zones.getNodeHeights(x, y, layer, -10L);
                                float h = hts[0] * 0.5f * 0.5f + hts[1] * 0.5f * 0.5f + hts[2] * 0.5f * 0.5f + hts[3] * 0.5f * 0.5f;
                                int gh = (int)(h * 10.0f);
                                int closestHeight = -1000;
                                int smallestDiff = 110;
                                for (Floor floor : tile.getFloors()) {
                                    int fh = gh + floor.getFloorLevel() * 30;
                                    if (Math.abs(fh - bridgeH) >= smallestDiff) continue;
                                    smallestDiff = Math.abs(fh - bridgeH);
                                    closestHeight = fh;
                                }
                                this.offsets[currAreaX][currAreaY] = closestHeight;
                                break;
                            }
                            if (foundConnection) break;
                        }
                        if (foundConnection) continue;
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (tileStructure == null || !tileStructure.isTypeBridge()) continue;
                    if (tileStructure.getWurmId() != targetStructure.getWurmId()) {
                        Long id = tileStructure.getWurmId();
                        if (bridges.contains(id)) continue;
                        BridgePart part = null;
                        for (BridgePart bp : tileStructure.getBridgeParts()) {
                            if (bp.getTileX() != x || bp.getTileY() != y) continue;
                            part = bp;
                            break;
                        }
                        this.area[currAreaX][currAreaY] = true;
                        float[] hts = Zones.getNodeHeights(x, y, layer, -10L);
                        float h = hts[0] * 0.5f * 0.5f + hts[1] * 0.5f * 0.5f + hts[2] * 0.5f * 0.5f + hts[3] * 0.5f * 0.5f;
                        groundHeight = (int)(h * 10.0f);
                        if (Math.abs(groundHeight - heightOffset) >= 25) continue;
                        this.offsets[currAreaX][currAreaY] = heightOffset;
                        this.area[currAreaX][currAreaY] = false;
                        continue;
                    }
                    BridgePart part = null;
                    for (BridgePart bp : tileStructure.getBridgeParts()) {
                        if (bp.getTileX() != x || bp.getTileY() != y) continue;
                        part = bp;
                        break;
                    }
                    if (part == null) continue;
                    this.offsets[currAreaX][currAreaY] = part.getHeightOffset();
                    continue;
                }
                if (tileStructure == null) continue;
                if (tileStructure.isTypeBridge()) {
                    BridgePart part = null;
                    for (BridgePart p : tileStructure.getBridgeParts()) {
                        if (p.getTileX() != x || p.getTileY() != y) continue;
                        part = p;
                        break;
                    }
                    if (part == null || Math.abs(part.getHeightOffset() - groundHeight) <= 25) continue;
                    this.area[currAreaX][currAreaY] = true;
                    continue;
                }
                this.area[currAreaX][currAreaY] = true;
            }
        }
    }

    final void calculateArea(int sx, int sy, int ex, int ey, int tilex, int tiley, int layer, Structure currstr) {
        int y;
        int x;
        this.area = new boolean[1 + ex - sx][1 + ey - sy];
        for (x = sx; x <= ex; ++x) {
            for (y = sy; y <= ey; ++y) {
                Structure toCheck;
                Item ring = Zones.isWithinDuelRing(x, y, layer > 0);
                if (ring != null) continue;
                VolaTile t = Zones.getTileOrNull(x, y, layer >= 0);
                if (t != null) {
                    toCheck = t.getStructure();
                    if (!(toCheck == null || toCheck.isFinalFinished() && toCheck.isFinished())) {
                        toCheck = null;
                    }
                } else {
                    toCheck = null;
                }
                int currAreaX = x - sx;
                int currAreaY = y - sy;
                if (currstr == toCheck) {
                    if (layer >= 0) continue;
                    int ttile = Server.caveMesh.getTile(x, y);
                    byte ttype = Tiles.decodeType(ttile);
                    if (Tiles.decodeHeight(ttile) < 0) {
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (Tiles.isSolidCave(ttype)) {
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (x > tilex + 1) {
                        if (this.area[currAreaX - 1][currAreaY]) {
                            this.area[currAreaX][currAreaY] = true;
                        }
                    } else if (x < tilex - 1 && this.area[currAreaX + 1][currAreaY]) {
                        this.area[currAreaX][currAreaY] = true;
                    }
                    if (y < tiley - 1) {
                        if (!this.area[currAreaX][currAreaY + 1]) continue;
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (y <= tiley + 1 || !this.area[currAreaX][currAreaY - 1]) continue;
                    this.area[currAreaX][currAreaY] = true;
                    continue;
                }
                this.area[currAreaX][currAreaY] = true;
            }
        }
        if (layer < 0) {
            for (x = sx; x <= ex; ++x) {
                for (y = sy; y <= ey; ++y) {
                    int currAreaX = x - sx;
                    int currAreaY = y - sy;
                    int ttile = Server.caveMesh.getTile(x, y);
                    byte ttype = Tiles.decodeType(ttile);
                    if (Tiles.decodeHeight(ttile) < 0) {
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (Tiles.isSolidCave(ttype)) {
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (x > tilex + 1) {
                        if (this.area[currAreaX - 1][currAreaY]) {
                            this.area[currAreaX][currAreaY] = true;
                        }
                    } else if (x < tilex - 1 && this.area[currAreaX + 1][currAreaY]) {
                        this.area[currAreaX][currAreaY] = true;
                    }
                    if (y < tiley - 1) {
                        if (!this.area[currAreaX][currAreaY + 1]) continue;
                        this.area[currAreaX][currAreaY] = true;
                        continue;
                    }
                    if (y <= tiley + 1 || !this.area[currAreaX][currAreaY - 1]) continue;
                    this.area[currAreaX][currAreaY] = true;
                }
            }
        }
    }

    final boolean isSpellBlocked(int deityId, int blockingSpellNum) {
        Random rand;
        Random rand2 = new Random(deityId + this.number * 1071);
        return rand2.nextInt(3) == 0 && (rand = new Random(deityId + blockingSpellNum * 1071)).nextInt(3) == 0;
    }

    final boolean deityCanHaveSpell(int deityId) {
        Random rand = new Random(deityId + this.number * 1071);
        return rand.nextInt(3) == 0;
    }

    final boolean hateEnchantPrecondition(Item target, Creature performer) {
        if (!Spell.mayBeEnchanted(target)) {
            performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
            return false;
        }
        if (target.enchantment != 0) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted.", (byte)3);
            return false;
        }
        if (target.getCurrentQualityLevel() < 70.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is of too low quality for this enchantment.", (byte)3);
            return false;
        }
        return true;
    }
}


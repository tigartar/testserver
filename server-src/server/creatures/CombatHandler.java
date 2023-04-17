/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MessageServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.CombatMove;
import com.wurmonline.server.combat.SpecialMove;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.AttackAction;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.creatures.UsedAttackData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class CombatHandler
implements MiscConstants,
TimeConstants,
CombatConstants,
SoundNames,
CreatureTemplateIds {
    private static final Logger logger = Logger.getLogger(CombatHandler.class.getName());
    private final Creature creature;
    private boolean addToSkills = false;
    public static final byte[] NO_COMBAT_OPTIONS = new byte[0];
    private List<ActionEntry> moveStack = null;
    private boolean turned = false;
    private byte currentStance = (byte)15;
    private static SpecialMove[] specialmoves = null;
    private byte currentStrength = 1;
    private static final List<ActionEntry> standardDefences = new LinkedList<ActionEntry>();
    private static boolean hit = false;
    private static boolean miss = true;
    private static boolean crit = false;
    private int usedShieldThisRound = 0;
    private boolean receivedShieldSkill = false;
    private static boolean dead = false;
    private static boolean aiming = false;
    private static float chanceToHit = 0.0f;
    private static double attCheck = 0.0;
    private static double attBonus = 0.0;
    private static double defCheck = 0.0;
    private static double defBonus = 0.0;
    private static double damage = 0.0;
    private static byte pos = 0;
    private static byte type = 0;
    private static Item defShield = null;
    private static Item defParryWeapon = null;
    private static Item defLeftWeapon = null;
    private static Skill defPrimWeaponSkill = null;
    private static Skills defenderSkills = null;
    private static String attString = "";
    private static String othersString = "";
    private static final List<ActionEntry> selectStanceList = new LinkedList<ActionEntry>();
    private static final String prones = "stancerebound";
    private static final String opens = "stanceopen";
    private static final String dodge = "dodge";
    private static final String fight = "fight";
    private static final String strike = "_strike";
    public static final float minShieldDam = 0.01f;
    private static boolean justOpen = false;
    private static double manouvreMod = 0.0;
    private byte opportunityAttacks = 0;
    public static final float enemyTerritoryMod = 0.7f;
    private static float parryBonus = 1.0f;
    private byte battleratingPenalty = 0;
    private Set<DoubleValueModifier> parryModifiers;
    private Set<DoubleValueModifier> dodgeModifiers;
    private boolean sentAttacks = false;
    private static final float DODGE_MODIFIER = 3.0f;
    private boolean receivedFStyleSkill = false;
    private boolean receivedWeaponSkill = false;
    private boolean receivedSecWeaponSkill = false;
    private Set<Item> secattacks = null;
    private boolean hasSpiritFervor = false;
    private int lastShieldBashed = 0;
    private boolean hasRodEffect = false;
    private static final float poleArmDamageBonus = 1.7f;
    private float lastTimeStamp = 1.0f;
    private float lastAttackPollDelta = 0.0f;
    private float waitTime = 0.0f;

    public CombatHandler(Creature _creature) {
        this.creature = _creature;
    }

    public static void resolveRound() {
        Players.getInstance().combatRound();
        Creatures.getInstance().combatRound();
    }

    public void shieldBash() {
        this.lastShieldBashed = 2;
        this.creature.getCommunicator().sendToggleShield(false);
    }

    public boolean mayShieldBash() {
        return this.lastShieldBashed <= 0;
    }

    public void calcAttacks(boolean newround) {
        if (!(this.creature.isDead() || this.moveStack != null && !newround && this.sentAttacks)) {
            if (this.moveStack == null) {
                this.moveStack = new LinkedList<ActionEntry>();
            } else {
                this.moveStack.clear();
            }
            manouvreMod = this.creature.getMovementScheme().armourMod.getModifier();
            if (this.creature.opponent != null && this.creature.getPrimWeapon() != null) {
                float knowl = this.getCombatKnowledgeSkill();
                if (!this.creature.isPlayer()) {
                    knowl += 20.0f;
                }
                if (knowl > 50.0f) {
                    this.moveStack.addAll(standardDefences);
                }
                float mycr = this.creature.getCombatHandler().getCombatRating(this.creature.opponent, this.creature.getPrimWeapon(), false);
                float oppcr = this.creature.opponent.getCombatHandler().getCombatRating(this.creature, this.creature.opponent.getPrimWeapon(), false);
                this.moveStack.addAll(this.getHighAttacks(this.creature.getPrimWeapon(), this.creature.isAutofight(), this.creature.opponent, mycr, oppcr, knowl));
                this.moveStack.addAll(this.getMidAttacks(this.creature.getPrimWeapon(), this.creature.isAutofight(), this.creature.opponent, mycr, oppcr, knowl));
                this.moveStack.addAll(this.getLowAttacks(this.creature.getPrimWeapon(), this.creature.isAutofight(), this.creature.opponent, mycr, oppcr, knowl));
            }
            if (!this.sentAttacks || newround) {
                this.sentAttacks = true;
                if (!this.creature.isAutofight()) {
                    this.creature.getCommunicator().sendCombatOptions(CombatHandler.getOptions(this.moveStack, this.currentStance), (short)0);
                }
                this.sendSpecialMoves();
                if (this.creature.getShield() != null) {
                    if (this.mayShieldBash()) {
                        this.creature.getCommunicator().sendToggleShield(true);
                    } else {
                        this.creature.getCommunicator().sendToggleShield(false);
                    }
                } else {
                    this.creature.getCommunicator().sendToggleShield(false);
                }
            }
        }
    }

    public float getCombatKnowledgeSkill() {
        Seat s;
        float knowl = 0.0f;
        int primarySkill = 10052;
        try {
            if (!this.creature.getPrimWeapon().isBodyPartAttached()) {
                primarySkill = this.creature.getPrimWeapon().getPrimarySkill();
            }
            Skill fightingSkill = this.creature.getSkills().getSkill(primarySkill);
            knowl = (float)fightingSkill.getKnowledge(this.creature.getPrimWeapon(), 0.0);
        }
        catch (NoSuchSkillException fightingSkill) {
            // empty catch block
        }
        if (knowl == 0.0f && !this.creature.isPlayer()) {
            Skill unarmed = this.creature.getFightingSkill();
            knowl = (float)unarmed.getKnowledge(0.0);
        }
        if (this.creature.getPrimWeapon().isBodyPartAttached()) {
            knowl += this.creature.getBonusForSpellEffect((byte)24) / 5.0f;
        }
        if ((s = this.creature.getSeat()) != null) {
            knowl *= s.manouvre;
        }
        if (this.creature.isOnHostileHomeServer()) {
            knowl *= 0.525f;
        }
        return knowl;
    }

    private void sendSpecialMoves() {
        block9: {
            if (this.creature.combatRound > 3 && !this.creature.getPrimWeapon().isBodyPart()) {
                double fightskill = 0.0;
                try {
                    fightskill = this.creature.getSkills().getSkill(this.creature.getPrimWeapon().getPrimarySkill()).getKnowledge(0.0);
                    if (fightskill > 19.0) {
                        specialmoves = SpecialMove.getMovesForWeaponSkillAndStance(this.creature, this.creature.getPrimWeapon(), (int)fightskill);
                        if (specialmoves.length > 0) {
                            this.creature.getCommunicator().sendSpecialMove((short)-1, "");
                            if (!this.creature.isAutofight()) {
                                for (int sx = 0; sx < specialmoves.length; ++sx) {
                                    this.creature.getCommunicator().sendSpecialMove((short)(197 + sx), specialmoves[sx].getName());
                                }
                            }
                            this.selectSpecialMove();
                        } else {
                            this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
                        }
                        break block9;
                    }
                    this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
                }
                catch (NoSuchSkillException nss) {
                    this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
                }
            } else {
                this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
            }
        }
    }

    private void selectSpecialMove() {
        if (this.creature.isAutofight() && Server.rand.nextInt(3) == 0) {
            int sm = Server.rand.nextInt(specialmoves.length);
            try {
                float chance = this.getChanceToHit(this.creature.opponent, this.creature.getPrimWeapon());
                if (chance > 50.0f && this.creature.getStatus().getStamina() > specialmoves[sm].getStaminaCost()) {
                    this.creature.setAction(new Action(this.creature, -1L, this.creature.getWurmId(), (short)(197 + sm), this.creature.getPosX(), this.creature.getPosY(), this.creature.getPositionZ() + this.creature.getAltOffZ(), this.creature.getStatus().getRotation()));
                }
            }
            catch (Exception fe) {
                logger.log(Level.WARNING, this.creature.getName() + " failed:" + fe.getMessage(), fe);
            }
        }
    }

    public void addBattleRatingPenalty(byte penalty) {
        if (this.battleratingPenalty == 0) {
            penalty = (byte)Math.max(penalty, 2);
        }
        this.battleratingPenalty = (byte)Math.min(5, this.battleratingPenalty + penalty);
    }

    byte getBattleratingPenalty() {
        return this.battleratingPenalty;
    }

    public void setCurrentStance(int actNum, byte aStance) {
        this.currentStance = aStance;
        if (actNum > 0) {
            this.creature.sendStance(this.currentStance);
        } else if (aStance == 15) {
            this.creature.sendStance(this.currentStance);
        } else if (aStance == 8) {
            this.creature.playAnimation(prones, true);
        } else if (aStance == 9) {
            this.creature.getStatus().setStunned(3.0f, false);
            this.creature.playAnimation(opens, false);
        } else if (aStance == 0) {
            this.creature.sendStance(this.currentStance);
        }
    }

    public void setCurrentStance(byte aCurrentStance) {
        this.currentStance = aCurrentStance;
    }

    public byte getCurrentStance() {
        return this.currentStance;
    }

    public void sendStanceAnimation(byte aStance, boolean attack) {
        if (aStance == 8) {
            this.creature.sendToLoggers(this.creature.getName() + ": " + prones, (byte)2);
            this.creature.playAnimation(prones, false);
        } else if (aStance == 9) {
            this.creature.getStatus().setStunned(3.0f, false);
            this.creature.playAnimation(opens, false);
            this.creature.sendToLoggers(this.creature.getName() + ": " + opens, (byte)2);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(fight);
            if (attack) {
                if (attString.equals("hit")) {
                    sb.append(strike);
                } else {
                    sb.append("_" + attString);
                }
            }
            if (!this.creature.isUnique() || this.creature.getHugeMoveCounter() == 2) {
                this.creature.playAnimation(sb.toString(), !attack);
            }
            this.creature.sendToLoggers(this.creature.getName() + ": " + sb.toString(), (byte)2);
        }
    }

    public static final String getStanceDescription(byte currentStance) {
        StringBuilder sb = new StringBuilder();
        if (CombatHandler.isHigh(currentStance)) {
            sb.append("higher ");
        } else if (CombatHandler.isLow(currentStance)) {
            sb.append("lower ");
        } else {
            sb.append("mid ");
        }
        if (CombatHandler.isLeft(currentStance)) {
            sb.append("left ");
        } else if (CombatHandler.isRight(currentStance)) {
            sb.append("right ");
        } else {
            sb.append("center ");
        }
        return sb.toString();
    }

    private void addToList(List<ActionEntry> list, @Nullable Item weapon, short number, Creature opponent, float mycr, float oppcr, float primweaponskill) {
        float movechance = this.creature.isPlayer() ? CombatHandler.getMoveChance(this.creature, weapon, this.currentStance, Actions.actionEntrys[number], mycr, oppcr, primweaponskill) : CombatHandler.getMoveChance(this.creature, weapon, this.currentStance, Actions.actionEntrys[number], mycr, oppcr, primweaponskill);
        if (movechance > 0.0f) {
            list.add(new ActionEntry(number, (int)movechance + "%, " + Actions.actionEntrys[number].getActionString(), "attack"));
        }
    }

    public static final int getAttackSkillCap(short action) {
        switch (action) {
            case 303: {
                return 0;
            }
            case 291: {
                return 3;
            }
            case 309: {
                return 2;
            }
            case 300: {
                return 15;
            }
            case 288: {
                return 13;
            }
            case 306: {
                return 12;
            }
            case 297: {
                return 9;
            }
            case 294: {
                return 7;
            }
            case 312: {
                return 5;
            }
        }
        return 0;
    }

    private List<ActionEntry> getHighAttacks(@Nullable Item weapon, boolean auto, Creature opponent, float mycr, float oppcr, float primweaponskill) {
        LinkedList<ActionEntry> tempList = new LinkedList<ActionEntry>();
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)300)) {
            this.addToList(tempList, weapon, (short)300, opponent, mycr, oppcr, primweaponskill);
        }
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)288)) {
            this.addToList(tempList, weapon, (short)288, opponent, mycr, oppcr, primweaponskill);
        }
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)306)) {
            this.addToList(tempList, weapon, (short)306, opponent, mycr, oppcr, primweaponskill);
        }
        if (!auto && tempList.size() > 0) {
            tempList.addFirst(new ActionEntry((short)(-tempList.size()), "High", "high"));
        }
        return tempList;
    }

    private List<ActionEntry> getMidAttacks(@Nullable Item weapon, boolean auto, Creature opponent, float mycr, float oppcr, float primweaponskill) {
        LinkedList<ActionEntry> tempList = new LinkedList<ActionEntry>();
        this.addToList(tempList, weapon, (short)303, opponent, mycr, oppcr, primweaponskill);
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)291)) {
            this.addToList(tempList, weapon, (short)291, opponent, mycr, oppcr, primweaponskill);
        }
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)309)) {
            this.addToList(tempList, weapon, (short)309, opponent, mycr, oppcr, primweaponskill);
        }
        if (!auto && tempList.size() > 0) {
            tempList.addFirst(new ActionEntry((short)(-tempList.size()), "Mid", "Mid"));
        }
        return tempList;
    }

    private List<ActionEntry> getLowAttacks(@Nullable Item weapon, boolean auto, Creature opponent, float mycr, float oppcr, float primweaponskill) {
        LinkedList<ActionEntry> tempList = new LinkedList<ActionEntry>();
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)297)) {
            this.addToList(tempList, weapon, (short)297, opponent, mycr, oppcr, primweaponskill);
        }
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)294)) {
            this.addToList(tempList, weapon, (short)294, opponent, mycr, oppcr, primweaponskill);
        }
        if (primweaponskill > (float)CombatHandler.getAttackSkillCap((short)312)) {
            this.addToList(tempList, weapon, (short)312, opponent, mycr, oppcr, primweaponskill);
        }
        if (!auto && tempList.size() > 0) {
            tempList.addFirst(new ActionEntry((short)(-tempList.size()), "Low", "Low"));
        }
        return tempList;
    }

    public static final float getMoveChance(Creature performer, @Nullable Item weapon, int stance, ActionEntry entry, float mycr, float oppcr, float primweaponskill) {
        float basechance = 100.0f - oppcr * 2.0f + mycr + primweaponskill;
        float cost = 0.0f;
        if (CombatHandler.isHigh(stance)) {
            cost = entry.isAttackHigh() ? (cost += 5.0f) : (entry.isAttackLow() ? (cost += 10.0f) : (cost += 3.0f));
        } else if (CombatHandler.isLow(stance)) {
            cost = entry.isAttackHigh() ? (cost += 10.0f) : (entry.isAttackLow() ? (cost += 5.0f) : (cost += 3.0f));
        } else if (entry.isAttackHigh()) {
            cost += 5.0f;
        } else if (entry.isAttackLow()) {
            cost += 5.0f;
        }
        cost = CombatHandler.isRight(stance) ? (entry.isAttackRight() ? (cost += 3.0f) : (entry.isAttackLeft() ? (cost += 10.0f) : (cost += 3.0f))) : (CombatHandler.isLeft(stance) ? (entry.isAttackRight() ? (cost += 10.0f) : (entry.isAttackLeft() ? (cost += 3.0f) : (cost += 3.0f))) : (entry.isAttackLeft() ? (cost += 5.0f) : (entry.isAttackRight() ? (cost += 5.0f) : (cost += 10.0f))));
        if (entry.isAttackHigh() && !entry.isAttackLeft() && !entry.isAttackRight()) {
            cost += 3.0f;
        } else if (entry.isAttackLow() && !entry.isAttackLeft() && !entry.isAttackRight()) {
            cost += 3.0f;
        }
        cost = (float)((double)cost * (1.0 - manouvreMod));
        if (weapon != null) {
            cost += Weapon.getBaseSpeedForWeapon(weapon);
        }
        if (performer.fightlevel >= 2) {
            cost -= 10.0f;
        }
        return Math.min(100.0f, Math.max(0.0f, basechance - cost));
    }

    public static final boolean isHigh(int stance) {
        return stance == 6 || stance == 1 || stance == 7;
    }

    public static final boolean isLow(int stance) {
        return stance == 4 || stance == 3 || stance == 10 || stance == 8;
    }

    public static final boolean isLeft(int stance) {
        return stance == 4 || stance == 5 || stance == 6;
    }

    public static final boolean isRight(int stance) {
        return stance == 3 || stance == 2 || stance == 1 || stance == 11;
    }

    public static final boolean isCenter(int stance) {
        return stance == 0 || stance == 9 || stance == 13 || stance == 14 || stance == 12;
    }

    public static final boolean isDefend(int stance) {
        return stance == 13 || stance == 14 || stance == 12 || stance == 11;
    }

    public static boolean prerequisitesFail(Creature creature, Creature opponent, boolean opportunity, Item weapon) {
        return CombatHandler.prerequisitesFail(creature, opponent, opportunity, weapon, false);
    }

    public static boolean prerequisitesFail(Creature creature, Creature opponent, boolean opportunity, Item weapon, boolean ignoreWeapon) {
        BlockingResult result;
        if (opponent.isDead()) {
            creature.setTarget(-10L, true);
            return true;
        }
        if (opponent.equals(creature)) {
            if (!opportunity) {
                creature.getCommunicator().sendCombatAlertMessage("You cannot attack yourself.");
                creature.setOpponent(null);
            }
            return true;
        }
        if (!creature.isPlayer() && opponent.isPlayer() && creature.getHitched() != null && creature.getHitched().wurmid == opponent.getVehicle()) {
            creature.setOpponent(null);
            creature.setTarget(-10L, true);
            return true;
        }
        if (!opponent.isPlayer() && creature.isPlayer() && opponent.getHitched() != null && opponent.getHitched().wurmid == creature.getVehicle()) {
            opponent.setOpponent(null);
            opponent.setTarget(-10L, true);
            return true;
        }
        if (!ignoreWeapon && weapon == null) {
            if (!opportunity) {
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new MulticolorLineSegment("You have no weapon to attack ", 0));
                segments.add(new CreatureLineSegment(opponent));
                segments.add(new MulticolorLineSegment(" with.", 0));
                creature.getCommunicator().sendColoredMessageCombat(segments);
                creature.setOpponent(null);
            }
            return true;
        }
        if (opponent.isBridgeBlockingAttack(creature, false)) {
            return true;
        }
        if (!GeneralUtilities.mayAttackSameLevel(creature, opponent)) {
            VolaTile t;
            if (creature.isOnSurface() && (t = Zones.getTileOrNull(creature.getTileX(), creature.getTileY(), creature.isOnSurface())) != null) {
                creature.sendToLoggers("Fighting " + opponent.getName() + " my z=" + creature.getPositionZ() + " opponent z=" + opponent.getPositionZ() + " structure=" + t.getStructure() + " diff=" + Math.abs(creature.getStatus().getPositionZ() - opponent.getStatus().getPositionZ()) * 10.0f);
                if (t.getStructure() != null) {
                    return true;
                }
            }
            if (opponent.isOnSurface() && (t = Zones.getTileOrNull(opponent.getTileX(), opponent.getTileY(), opponent.isOnSurface())) != null && t.getStructure() != null) {
                return true;
            }
        }
        if ((result = Blocking.getBlockerBetween(creature, opponent, 4)) != null) {
            boolean blocked = false;
            for (Blocker b : result.getBlockerArray()) {
                if (!b.isDoor()) {
                    blocked = true;
                }
                if (!b.canBeOpenedBy(creature, false)) {
                    blocked = true;
                }
                if (!b.canBeOpenedBy(opponent, false)) {
                    blocked = true;
                }
                if (blocked) break;
            }
            if (blocked) {
                creature.breakout();
                if (!opportunity) {
                    creature.getCommunicator().sendNormalServerMessage("The " + result.getFirstBlocker().getName() + " blocks your attempt.");
                    if (result.getFirstBlocker().isTile()) {
                        if (opponent.opponent == creature || opponent.getTarget() == creature) {
                            opponent.setTarget(-10L, true);
                        }
                        if (creature.getTarget() == opponent) {
                            creature.setTarget(-10L, true);
                        }
                    }
                }
                creature.setOpponent(null);
                creature.sendToLoggers("Blocker result when attacking " + opponent.getName() + " " + result.getFirstBlocker().getName(), (byte)2);
                return true;
            }
        }
        if (creature.isOnSurface() != opponent.isOnSurface()) {
            boolean fail = false;
            boolean transition = false;
            if (opponent.getCurrentTile().isTransition) {
                transition = true;
                if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(opponent.getTileX(), opponent.getTileY())))) {
                    fail = true;
                }
            }
            if (!fail && creature.getCurrentTile().isTransition) {
                transition = true;
                if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(creature.getTileX(), creature.getTileY())))) {
                    fail = true;
                }
            }
            if (!transition) {
                fail = true;
            }
            return fail;
        }
        return false;
    }

    private final AttackAction getAttackAction(boolean isSecondary) {
        AttackAction attack = null;
        List<AttackAction> list = !isSecondary ? this.creature.getTemplate().getPrimaryAttacks() : this.creature.getTemplate().getSecondaryAttacks();
        ArrayList<AttackAction> valid = new ArrayList<AttackAction>();
        for (AttackAction act : list) {
            UsedAttackData data = this.creature.getUsedAttackData(act);
            if (data == null) {
                valid.add(act);
                continue;
            }
            if (!(data.getTime() <= 0.0f) || data.getRounds() > 0) continue;
            valid.add(act);
        }
        if (valid.size() > 0) {
            int index = Server.rand.nextInt(valid.size());
            attack = (AttackAction)valid.get(index);
        }
        return attack;
    }

    private boolean attack2(Creature opponent, int combatCounter, boolean opportunity, float actionCounter, Action act) {
        boolean lDead;
        block19: {
            int[] cmoves;
            boolean doSpecialAttack;
            block22: {
                boolean shouldDoSecondary;
                AttackAction secondaryAttack;
                block21: {
                    AttackAction primaryAttack;
                    block20: {
                        ArrayList<MulticolorLineSegment> segments;
                        block18: {
                            float updateTime;
                            float delta = Math.max(0.0f, actionCounter - this.lastTimeStamp);
                            this.lastAttackPollDelta = updateTime = Math.abs(delta - this.lastAttackPollDelta);
                            this.creature.updateAttacksUsed(updateTime);
                            if (delta <= this.waitTime) {
                                return false;
                            }
                            if (opportunity) {
                                this.creature.opportunityAttackCounter = (byte)2;
                            }
                            this.lastAttackPollDelta = 0.0f;
                            this.waitTime = 0.5f;
                            primaryAttack = this.getAttackAction(false);
                            secondaryAttack = this.getAttackAction(true);
                            int[] tcmoves = this.creature.getCombatMoves();
                            boolean canDoSpecials = tcmoves != null && tcmoves.length > 0;
                            this.creature.setSecondsToLogout(300);
                            if (!opponent.isPlayer()) {
                                this.creature.setSecondsToLogout(180);
                            }
                            shouldDoSecondary = false;
                            if (primaryAttack != null && secondaryAttack != null && this.creature.combatRound > 1) {
                                shouldDoSecondary = Server.rand.nextInt(5) == 0;
                            } else if (primaryAttack == null && secondaryAttack != null) {
                                shouldDoSecondary = true;
                            }
                            doSpecialAttack = false;
                            if (!this.creature.isPlayer()) {
                                boolean changedStance = false;
                                if (Server.rand.nextInt(10) == 0) {
                                    changedStance = this.checkStanceChange(this.creature, opponent);
                                }
                                if (canDoSpecials && !changedStance) {
                                    doSpecialAttack = primaryAttack == null && secondaryAttack == null ? true : Server.rand.nextInt(80) < 20;
                                }
                            }
                            Item weapon = null;
                            if (shouldDoSecondary && secondaryAttack != null && secondaryAttack.isUsingWeapon() || !shouldDoSecondary && primaryAttack != null && primaryAttack.isUsingWeapon()) {
                                weapon = this.creature.getPrimWeapon();
                            }
                            if (opportunity) {
                                this.creature.opportunityAttackCounter = (byte)2;
                            }
                            this.lastTimeStamp = actionCounter;
                            if (CombatHandler.prerequisitesFail(this.creature, opponent, opportunity, weapon, weapon == null)) {
                                return true;
                            }
                            if (act != null && act.justTickedSecond()) {
                                this.creature.getCommunicator().sendCombatStatus(CombatHandler.getDistdiff(this.creature, opponent, shouldDoSecondary ? secondaryAttack : primaryAttack), this.getFootingModifier(weapon, opponent), this.currentStance);
                            }
                            if (this.isProne() || this.isOpen()) {
                                return false;
                            }
                            lDead = false;
                            this.creature.opponentCounter = 30;
                            if (actionCounter != 1.0f || opportunity || !this.creature.isMoving() || opponent.isMoving() || opponent.target != this.creature.getWurmId()) break block18;
                            opponent.attackTarget();
                            if (opponent.opponent != this.creature) break block19;
                            this.creature.sendToLoggers("Opponent strikes first", (byte)2);
                            ArrayList<MulticolorLineSegment> segments2 = new ArrayList<MulticolorLineSegment>();
                            segments2.add(new CreatureLineSegment(opponent));
                            segments2.add(new MulticolorLineSegment(" strike ", 0));
                            segments2.add(new CreatureLineSegment(this.creature));
                            segments2.add(new MulticolorLineSegment(" as " + this.creature.getHeSheItString() + " approaches!", 0));
                            opponent.getCommunicator().sendColoredMessageCombat(segments2);
                            segments2.get(1).setText(" strikes ");
                            segments2.get(1).setText(" as you approach. ");
                            this.creature.getCommunicator().sendColoredMessageCombat(segments2);
                            lDead = opponent.getCombatHandler().attack(this.creature, combatCounter, true, 2.0f, null);
                            break block19;
                        }
                        if (!opportunity || primaryAttack == null) break block20;
                        this.opportunityAttacks = (byte)(this.opportunityAttacks + 1);
                        this.creature.sendToLoggers("YOU OPPORTUNITY", (byte)2);
                        opponent.sendToLoggers(this.creature.getName() + " OPPORTUNITY", (byte)2);
                        if (opponent.spamMode()) {
                            segments = new ArrayList<MulticolorLineSegment>();
                            segments.add(new MulticolorLineSegment("You open yourself to an attack from ", 7));
                            segments.add(new CreatureLineSegment(this.creature));
                            segments.add(new MulticolorLineSegment(".", 7));
                            opponent.getCommunicator().sendColoredMessageCombat(segments);
                        }
                        if (this.creature.spamMode()) {
                            segments = new ArrayList();
                            segments.add(new CreatureLineSegment(opponent));
                            segments.add(new MulticolorLineSegment(" opens " + opponent.getHimHerItString() + "self up to an easy attack.", 3));
                            opponent.getCommunicator().sendColoredMessageCombat(segments);
                        }
                        lDead = this.attack(opponent, primaryAttack);
                        break block19;
                    }
                    if (lDead || primaryAttack == null || shouldDoSecondary || doSpecialAttack) break block21;
                    float time = this.getSpeed(primaryAttack, primaryAttack.isUsingWeapon() ? this.creature.getPrimWeapon() : null);
                    this.creature.addToAttackUsed(primaryAttack, time, primaryAttack.getAttackValues().getRounds());
                    lDead = this.attack(opponent, primaryAttack);
                    this.waitTime = primaryAttack.getAttackValues().getWaitTime();
                    if (!this.creature.isPlayer() || act == null || !act.justTickedSecond()) break block19;
                    this.checkStanceChange(this.creature, opponent);
                    break block19;
                }
                if (lDead || secondaryAttack == null || !shouldDoSecondary || doSpecialAttack) break block22;
                float time = this.getSpeed(secondaryAttack, secondaryAttack.isUsingWeapon() ? this.creature.getPrimWeapon(false) : null);
                this.creature.addToAttackUsed(secondaryAttack, time, secondaryAttack.getAttackValues().getRounds());
                lDead = this.attack(opponent, secondaryAttack);
                this.waitTime = secondaryAttack.getAttackValues().getWaitTime();
                if (!this.creature.isPlayer() || act == null || !act.justTickedSecond()) break block19;
                this.checkStanceChange(this.creature, opponent);
                break block19;
            }
            if (!lDead && !this.creature.isPlayer() && this.creature.getTarget() != null && doSpecialAttack && (cmoves = this.creature.getCombatMoves()).length > 0) {
                for (int lCmove : cmoves) {
                    CombatMove c = CombatMove.getCombatMove(lCmove);
                    if (!(Server.rand.nextFloat() < c.getRarity()) || this.creature.getHugeMoveCounter() != 0) continue;
                    this.creature.sendToLoggers("YOU COMBAT MOVE", (byte)2);
                    opponent.sendToLoggers(this.creature.getName() + " COMBAT MOVE", (byte)2);
                    this.creature.setHugeMoveCounter(2 + Server.rand.nextInt(4));
                    c.perform(this.creature);
                    this.waitTime = 2.0f;
                    break;
                }
            }
        }
        return lDead;
    }

    public void resetSecAttacks() {
        if (this.secattacks != null) {
            this.secattacks.clear();
        }
    }

    public boolean attack(Creature opponent, int combatCounter, boolean opportunity, float actionCounter, Action act) {
        boolean lDead;
        block17: {
            int[] cmoves;
            boolean performedAttack;
            block19: {
                boolean shouldAttack;
                Item weapon;
                float delta;
                block18: {
                    Item[] secweapons;
                    ArrayList<MulticolorLineSegment> segments;
                    block16: {
                        opponent.addAttacker(this.creature);
                        if (actionCounter == 1.0f) {
                            this.lastTimeStamp = actionCounter;
                        }
                        if (Features.Feature.CREATURE_COMBAT_CHANGES.isEnabled() && this.creature.getTemplate().isUsingNewAttacks()) {
                            return this.attack2(opponent, combatCounter, opportunity, actionCounter, act);
                        }
                        delta = Math.max(0.0f, actionCounter - this.lastTimeStamp);
                        if ((double)delta < 0.1) {
                            return false;
                        }
                        if (opportunity) {
                            this.creature.opportunityAttackCounter = (byte)2;
                        }
                        this.lastTimeStamp = actionCounter;
                        weapon = this.creature.getPrimWeapon();
                        this.creature.setSecondsToLogout(300);
                        if (!opponent.isPlayer()) {
                            this.creature.setSecondsToLogout(180);
                        }
                        if (CombatHandler.prerequisitesFail(this.creature, opponent, opportunity, weapon)) {
                            return true;
                        }
                        if (act != null && act.justTickedSecond()) {
                            this.creature.getCommunicator().sendCombatStatus(CombatHandler.getDistdiff(weapon, this.creature, opponent), this.getFootingModifier(weapon, opponent), this.currentStance);
                        }
                        lDead = false;
                        if (this.isProne() || this.isOpen()) {
                            return false;
                        }
                        this.creature.opponentCounter = 30;
                        if (actionCounter != 1.0f || opportunity || !this.creature.isMoving() || opponent.isMoving() || opponent.target != this.creature.getWurmId()) break block16;
                        opponent.attackTarget();
                        if (opponent.opponent != this.creature) break block17;
                        this.creature.sendToLoggers("Opponent strikes first", (byte)2);
                        ArrayList<MulticolorLineSegment> segments2 = new ArrayList<MulticolorLineSegment>();
                        segments2.add(new CreatureLineSegment(opponent));
                        segments2.add(new MulticolorLineSegment(" strike ", 0));
                        segments2.add(new CreatureLineSegment(this.creature));
                        segments2.add(new MulticolorLineSegment(" as " + this.creature.getHeSheItString() + " approaches!", 0));
                        opponent.getCommunicator().sendColoredMessageCombat(segments2);
                        segments2.get(1).setText(" strikes ");
                        segments2.get(1).setText(" as you approach. ");
                        this.creature.getCommunicator().sendColoredMessageCombat(segments2);
                        lDead = opponent.getCombatHandler().attack(this.creature, combatCounter, true, 2.0f, null);
                        break block17;
                    }
                    if (!opportunity) break block18;
                    this.opportunityAttacks = (byte)(this.opportunityAttacks + 1);
                    this.creature.sendToLoggers("YOU OPPORTUNITY", (byte)2);
                    opponent.sendToLoggers(this.creature.getName() + " OPPORTUNITY", (byte)2);
                    if (opponent.spamMode()) {
                        segments = new ArrayList<MulticolorLineSegment>();
                        segments.add(new MulticolorLineSegment("You open yourself to an attack from ", 7));
                        segments.add(new CreatureLineSegment(this.creature));
                        segments.add(new MulticolorLineSegment(".", 7));
                        opponent.getCommunicator().sendColoredMessageCombat(segments);
                    }
                    if (this.creature.spamMode()) {
                        segments = new ArrayList();
                        segments.add(new CreatureLineSegment(opponent));
                        segments.add(new MulticolorLineSegment(" opens " + opponent.getHimHerItString() + "self up to an easy attack.", 3));
                        this.creature.getCommunicator().sendColoredMessageCombat(segments);
                    }
                    if (Server.rand.nextInt(3) == 0 && (secweapons = this.creature.getSecondaryWeapons()).length > 0) {
                        weapon = secweapons[Server.rand.nextInt(secweapons.length)];
                    }
                    lDead = this.attack(opponent, weapon, false);
                    break block17;
                }
                performedAttack = false;
                if (!lDead && this.creature.combatRound > 1) {
                    Item[] secweapons;
                    for (Item lSecweapon : secweapons = this.creature.getSecondaryWeapons()) {
                        boolean shouldAttack2;
                        if (this.creature.opponent == null) continue;
                        if (this.secattacks == null) {
                            this.secattacks = new HashSet<Item>();
                        }
                        if (this.secattacks.contains(lSecweapon) || (lSecweapon.getTemplateId() == 12 || lSecweapon.getTemplateId() == 17) && (this.creature.getHugeMoveCounter() != 0 || !Server.rand.nextBoolean())) continue;
                        float time = this.getSpeed(lSecweapon);
                        float timer = this.creature.addToWeaponUsed(lSecweapon, delta);
                        boolean bl = shouldAttack2 = timer > time;
                        if (lDead || this.creature.combatRound % 2 != 1 || !shouldAttack2) continue;
                        this.creature.deductFromWeaponUsed(lSecweapon, time);
                        this.creature.sendToLoggers("YOU SECONDARY " + lSecweapon.getName(), (byte)2);
                        opponent.sendToLoggers(this.creature.getName() + " SECONDARY " + lSecweapon.getName() + "(" + lSecweapon.getWurmId() + ")", (byte)2);
                        this.creature.setHugeMoveCounter(2 + Server.rand.nextInt(4));
                        lDead = this.attack(opponent, lSecweapon, true);
                        performedAttack = true;
                        this.secattacks.add(lSecweapon);
                    }
                }
                float time = this.getSpeed(weapon);
                float timer = this.creature.addToWeaponUsed(weapon, delta);
                boolean bl = shouldAttack = timer > time;
                if (lDead || !shouldAttack) break block19;
                this.creature.deductFromWeaponUsed(weapon, time);
                this.creature.sendToLoggers("YOU PRIMARY " + weapon.getName(), (byte)2);
                opponent.sendToLoggers(this.creature.getName() + " PRIMARY " + weapon.getName(), (byte)2);
                lDead = this.attack(opponent, weapon, false);
                performedAttack = true;
                if (!this.creature.isPlayer() || act == null || !act.justTickedSecond()) break block17;
                this.checkStanceChange(this.creature, opponent);
                break block17;
            }
            if (!(performedAttack || lDead || this.creature.isPlayer() || this.creature.getTarget() == null || this.creature.getLayer() != opponent.getLayer() || this.checkStanceChange(this.creature, opponent) || (cmoves = this.creature.getCombatMoves()).length <= 0)) {
                for (int lCmove : cmoves) {
                    CombatMove c = CombatMove.getCombatMove(lCmove);
                    if (!(Server.rand.nextFloat() < c.getRarity()) || this.creature.getHugeMoveCounter() != 0) continue;
                    this.creature.sendToLoggers("YOU COMBAT MOVE", (byte)2);
                    opponent.sendToLoggers(this.creature.getName() + " COMBAT MOVE", (byte)2);
                    this.creature.setHugeMoveCounter(2 + Server.rand.nextInt(4));
                    c.perform(this.creature);
                    break;
                }
            }
        }
        return lDead;
    }

    public void clearRound() {
        this.opportunityAttacks = 0;
        this.receivedWeaponSkill = false;
        this.receivedSecWeaponSkill = false;
        this.receivedFStyleSkill = false;
        this.receivedShieldSkill = false;
        this.usedShieldThisRound = 0;
        if (this.lastShieldBashed > 0) {
            --this.lastShieldBashed;
        }
        if (this.secattacks != null) {
            this.secattacks.clear();
        }
        this.turned = false;
        if (this.battleratingPenalty > 0) {
            this.battleratingPenalty = (byte)Math.max(0, this.battleratingPenalty - 2);
            if (this.battleratingPenalty == 0 && this.creature.isPlayer()) {
                this.creature.getCommunicator().sendCombatNormalMessage("You concentrate better again.");
            }
        }
        if (this.creature.isFighting()) {
            ++this.creature.combatRound;
            if (!this.creature.opponent.isDead()) {
                this.calcAttacks(true);
            } else {
                this.moveStack = null;
            }
            this.creature.setStealth(false);
        }
    }

    public static final boolean isStanceParrying(byte defenderStance, byte attackerStance) {
        if (attackerStance == 8 || attackerStance == 9) {
            return true;
        }
        if (defenderStance == 8 || defenderStance == 9) {
            return false;
        }
        if (defenderStance == 11) {
            return attackerStance == 3 || attackerStance == 4 || attackerStance == 10;
        }
        if (defenderStance == 12) {
            return attackerStance == 1 || attackerStance == 6 || attackerStance == 7;
        }
        if (defenderStance == 14) {
            return attackerStance == 5 || attackerStance == 6 || attackerStance == 4;
        }
        if (defenderStance == 13) {
            return attackerStance == 2 || attackerStance == 1 || attackerStance == 3;
        }
        return false;
    }

    public static final boolean isStanceOpposing(byte defenderStance, byte attackerStance) {
        if (attackerStance == 8 || attackerStance == 9) {
            return true;
        }
        if (defenderStance == 8 || defenderStance == 9) {
            return false;
        }
        if (defenderStance == 1) {
            return attackerStance == 6;
        }
        if (defenderStance == 6) {
            return attackerStance == 1;
        }
        if (defenderStance == 4) {
            return attackerStance == 3;
        }
        if (defenderStance == 3) {
            return attackerStance == 4;
        }
        if (defenderStance == 5) {
            return attackerStance == 2;
        }
        if (defenderStance == 2) {
            return attackerStance == 5;
        }
        if (defenderStance == 7) {
            return attackerStance == 7;
        }
        if (defenderStance == 0) {
            return attackerStance == 0;
        }
        if (defenderStance == 10) {
            return attackerStance == 10;
        }
        return false;
    }

    private byte getWoundPos(byte aStance, Creature aCreature) throws Exception {
        return aCreature.getBody().getRandomWoundPos(aStance);
    }

    private static void resetFlags(Creature opponent) {
        hit = false;
        miss = false;
        crit = false;
        aiming = false;
        dead = false;
        chanceToHit = 0.0f;
        pos = 0;
        attCheck = 0.0;
        attBonus = 0.0;
        defCheck = 0.0;
        defBonus = 0.0;
        defShield = null;
        defenderSkills = opponent.getSkills();
        defParryWeapon = null;
        defLeftWeapon = null;
        defPrimWeaponSkill = null;
        type = 0;
        attString = "";
        damage = 0.0;
        justOpen = false;
    }

    public float getSpeed(AttackAction act, Item weapon) {
        float timeMod = 0.5f;
        if (this.currentStrength == 0) {
            timeMod = 1.5f;
        }
        if (act.isUsingWeapon() && weapon != null) {
            float calcspeed = this.getWeaponSpeed(act, weapon);
            calcspeed += timeMod;
            if (weapon.getSpellSpeedBonus() != 0.0f) {
                calcspeed = (float)((double)calcspeed - 0.5 * (double)(weapon.getSpellSpeedBonus() / 100.0f));
            } else if (!weapon.isArtifact() && this.creature.getBonusForSpellEffect((byte)39) > 0.0f) {
                calcspeed -= 0.5f;
            }
            if (weapon.isTwoHanded() && this.currentStrength == 3) {
                calcspeed *= 0.9f;
            }
            if (!Features.Feature.METALLIC_ITEMS.isEnabled() && weapon.getMaterial() == 57) {
                calcspeed *= 0.9f;
            }
            if (this.creature.getStatus().getStamina() < 2000) {
                calcspeed += 1.0f;
            }
            calcspeed = (float)((double)calcspeed * (this.creature.getMovementScheme().getWebArmourMod() * -4.0));
            if (this.creature.hasSpellEffect((byte)66)) {
                calcspeed *= 2.0f;
            }
            return Math.max(3.0f, calcspeed);
        }
        float calcspeed = this.getWeaponSpeed(act, null);
        calcspeed += timeMod;
        if (this.creature.getStatus().getStamina() < 2000) {
            calcspeed += 1.0f;
        }
        calcspeed = (float)((double)calcspeed * (this.creature.getMovementScheme().getWebArmourMod() * -4.0));
        if (this.creature.hasSpellEffect((byte)66)) {
            calcspeed *= 2.0f;
        }
        return Math.max(3.0f, calcspeed);
    }

    public float getSpeed(Item weapon) {
        float timeMod = 0.5f;
        if (this.currentStrength == 0) {
            timeMod = 1.5f;
        }
        float calcspeed = this.getWeaponSpeed(weapon);
        calcspeed += timeMod;
        if (weapon.getSpellSpeedBonus() != 0.0f) {
            calcspeed = (float)((double)calcspeed - 0.5 * (double)(weapon.getSpellSpeedBonus() / 100.0f));
        } else if (!weapon.isArtifact() && this.creature.getBonusForSpellEffect((byte)39) > 0.0f) {
            float maxBonus = calcspeed * 0.1f;
            float percentBonus = this.creature.getBonusForSpellEffect((byte)39) / 100.0f;
            calcspeed -= maxBonus * percentBonus;
        }
        if (weapon.isTwoHanded() && this.currentStrength == 3) {
            calcspeed *= 0.9f;
        }
        if (!Features.Feature.METALLIC_ITEMS.isEnabled() && weapon.getMaterial() == 57) {
            calcspeed *= 0.9f;
        }
        if (this.creature.getStatus().getStamina() < 2000) {
            calcspeed += 1.0f;
        }
        float waMult = (float)(this.creature.getMovementScheme().getWebArmourMod() * -2.0);
        calcspeed *= 1.0f + waMult;
        if (this.creature.hasSpellEffect((byte)66)) {
            calcspeed *= 2.0f;
        }
        return Math.max(3.0f, calcspeed);
    }

    public boolean isOpen() {
        return this.currentStance == 9;
    }

    public boolean isProne() {
        return this.currentStance == 8;
    }

    private boolean attack(Creature opponent, AttackAction attackAction) {
        CombatHandler.resetFlags(opponent);
        if (!(opponent instanceof Player) || !opponent.hasLink()) {
            if (!this.turned) {
                if (opponent.getTarget() == null || opponent.getTarget() == this.creature) {
                    opponent.turnTowardsCreature(this.creature);
                }
                this.turned = true;
            }
            boolean switchOpp = false;
            if (!opponent.isFighting() && (this.creature.isPlayer() || this.creature.isDominated())) {
                switchOpp = true;
            }
            opponent.setTarget(this.creature.getWurmId(), switchOpp);
        }
        this.creature.getStatus().modifyStamina((int)(-80.0f * (1.0f + (float)this.currentStrength * 0.5f)));
        this.addToSkills = true;
        Item weapon = null;
        weapon = this.creature.getPrimWeapon(!attackAction.isUsingWeapon());
        chanceToHit = this.getChanceToHit(opponent, weapon);
        type = attackAction.getAttackValues().getDamageType();
        float percent = this.checkShield(opponent, weapon);
        if (percent > 50.0f) {
            chanceToHit = 0.0f;
        } else if (percent > 0.0f) {
            chanceToHit *= 1.0f - percent / 100.0f;
        }
        float parrPercent = -1.0f;
        if ((opponent.getFightStyle() != 1 || Server.rand.nextInt(3) == 0) && chanceToHit > 0.0f) {
            parrPercent = this.checkDefenderParry(opponent, weapon);
            if (parrPercent > 60.0f) {
                chanceToHit = 0.0f;
            } else if (parrPercent > 0.0f) {
                chanceToHit *= 1.0f - parrPercent / 200.0f;
            }
        }
        pos = (byte)2;
        try {
            pos = this.getWoundPos(this.currentStance, opponent);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, this.creature.getName() + " " + ex.getMessage(), ex);
        }
        attCheck = (double)(Server.rand.nextFloat() * 100.0f) * (1.0 + this.creature.getVisionMod());
        String combatDetails = " CHANCE:" + chanceToHit + ", roll=" + attCheck;
        if (this.creature.spamMode() && Servers.isThisATestServer()) {
            this.creature.getCommunicator().sendCombatSafeMessage(combatDetails);
        }
        this.creature.sendToLoggers("YOU" + combatDetails, (byte)2);
        opponent.sendToLoggers(this.creature.getName() + combatDetails, (byte)2);
        if (attCheck < (double)chanceToHit) {
            if (opponent.isPlayer() && !weapon.isArtifact()) {
                float critChance = attackAction.getAttackValues().getCriticalChance();
                if (CombatHandler.isAtSoftSpot(opponent.getCombatHandler().getCurrentStance(), this.getCurrentStance())) {
                    critChance += 0.05f;
                }
                if (Server.rand.nextFloat() < critChance) {
                    crit = true;
                }
            }
        } else {
            miss = true;
        }
        if (!miss && !crit) {
            boolean keepGoing = true;
            defCheck = (double)(Server.rand.nextFloat() * 100.0f) * opponent.getCombatHandler().getDodgeMod();
            defCheck *= (double)opponent.getStatus().getDodgeTypeModifier();
            if (opponent.getMovePenalty() != 0) {
                defCheck *= (double)(1.0f + (float)opponent.getMovePenalty() / 10.0f);
            }
            if ((defCheck *= 1.0 - opponent.getMovementScheme().armourMod.getModifier()) < opponent.getBodyControl() * (double)ItemBonus.getDodgeBonus(opponent) / 3.0) {
                if ((double)(opponent.getStatus().getDodgeTypeModifier() * 100.0f) < opponent.getBodyControl() / 3.0) {
                    logger.log(Level.WARNING, opponent.getName() + " is impossible to hit except for crits: " + opponent.getCombatHandler().getDodgeMod() * 100.0 + " is always less than " + opponent.getBodyControl());
                }
                this.sendDodgeMessage(opponent);
                keepGoing = false;
                String dodgeDetails = "Dodge=" + defCheck + "<" + opponent.getBodyControl() / 3.0 + " dodgemod=" + opponent.getCombatHandler().getDodgeMod() + " dodgeType=" + opponent.getStatus().getDodgeTypeModifier() + " dodgeMovePenalty=" + opponent.getMovePenalty() + " armour=" + opponent.getMovementScheme().armourMod.getModifier();
                if (this.creature.spamMode() && Servers.isThisATestServer()) {
                    this.creature.getCommunicator().sendCombatSafeMessage(dodgeDetails);
                }
                this.creature.sendToLoggers(dodgeDetails, (byte)4);
                CombatHandler.checkIfHitVehicle(this.creature, opponent);
            }
            if (keepGoing) {
                hit = true;
            }
        }
        if (hit || crit) {
            this.creature.sendToLoggers("YOU DAMAGE " + weapon.getName(), (byte)2);
            opponent.sendToLoggers(this.creature.getName() + " DAMAGE " + weapon.getName(), (byte)2);
            dead = this.setDamage(opponent, weapon, damage, pos, type);
        }
        if (dead) {
            this.setKillEffects(this.creature, opponent);
        }
        if (miss) {
            if (this.creature.spamMode() && (chanceToHit > 0.0f || percent > 0.0f && parrPercent > 0.0f)) {
                this.creature.getCommunicator().sendCombatNormalMessage("You miss with the " + weapon.getName() + ".");
                this.creature.sendToLoggers("YOU MISS " + weapon.getName(), (byte)2);
                opponent.sendToLoggers(this.creature.getName() + " MISS " + weapon.getName(), (byte)2);
            }
            if (!this.creature.isUnique() && attCheck - (double)chanceToHit > 50.0 && Server.rand.nextInt(10) == 0) {
                justOpen = true;
                this.setCurrentStance(-1, (byte)9);
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new CreatureLineSegment(this.creature));
                segments.add(new MulticolorLineSegment(" makes a bad move and is an easy target!.", 0));
                opponent.getCommunicator().sendColoredMessageCombat(segments);
                segments.get(1).setText(" make a bad move, making you an easy target.");
                this.creature.getCommunicator().sendColoredMessageCombat(segments);
                this.creature.getCurrentTile().checkOpportunityAttacks(this.creature);
                opponent.getCurrentTile().checkOpportunityAttacks(this.creature);
            } else if (Server.rand.nextInt(10) == 0) {
                CombatHandler.checkIfHitVehicle(this.creature, opponent);
            }
        }
        this.addToSkills = false;
        this.getDamage(this.creature, attackAction, opponent);
        attString = attackAction.getAttackIdentifier().getAnimationString();
        this.sendStanceAnimation(this.currentStance, true);
        return dead;
    }

    private boolean attack(Creature opponent, Item weapon, boolean secondaryWeapon) {
        if (weapon.isWeaponBow()) {
            return false;
        }
        CombatHandler.resetFlags(opponent);
        if (!(opponent instanceof Player) || !opponent.hasLink()) {
            if (!this.turned) {
                if (opponent.getTarget() == null || opponent.getTarget() == this.creature) {
                    opponent.turnTowardsCreature(this.creature);
                }
                this.turned = true;
            }
            boolean switchOpp = false;
            if (!opponent.isFighting() && (this.creature.isPlayer() || this.creature.isDominated())) {
                switchOpp = true;
            }
            opponent.setTarget(this.creature.getWurmId(), switchOpp);
        }
        this.creature.getStatus().modifyStamina((int)((float)(-weapon.getWeightGrams()) / 10.0f * (1.0f + (float)this.currentStrength * 0.5f)));
        this.addToSkills = true;
        chanceToHit = this.getChanceToHit(opponent, weapon);
        this.getType(weapon, false);
        this.getDamage(this.creature, weapon, opponent);
        CombatHandler.setAttString(this.creature, weapon, type);
        this.sendStanceAnimation(this.currentStance, true);
        float percent = this.checkShield(opponent, weapon);
        if (percent > 50.0f) {
            chanceToHit = 0.0f;
        } else if (percent > 0.0f) {
            chanceToHit *= 1.0f - percent / 100.0f;
        }
        float parrPercent = -1.0f;
        if ((opponent.getFightStyle() != 1 || Server.rand.nextInt(3) == 0) && chanceToHit > 0.0f) {
            parrPercent = this.checkDefenderParry(opponent, weapon);
            if (parrPercent > 60.0f) {
                chanceToHit = 0.0f;
            } else if (parrPercent > 0.0f) {
                chanceToHit *= 1.0f - parrPercent / 200.0f;
            }
        }
        pos = (byte)2;
        try {
            pos = this.getWoundPos(this.currentStance, opponent);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, this.creature.getName() + " " + ex.getMessage(), ex);
        }
        attCheck = (double)(Server.rand.nextFloat() * 100.0f) * (1.0 + this.creature.getVisionMod());
        String combatDetails = " CHANCE:" + chanceToHit + ", roll=" + attCheck;
        if (this.creature.spamMode() && Servers.isThisATestServer()) {
            this.creature.getCommunicator().sendCombatSafeMessage(combatDetails);
        }
        this.creature.sendToLoggers("YOU" + combatDetails, (byte)2);
        opponent.sendToLoggers(this.creature.getName() + combatDetails, (byte)2);
        if (attCheck < (double)chanceToHit) {
            if (opponent.isPlayer()) {
                float critChance = Weapon.getCritChanceForWeapon(weapon);
                if (CombatHandler.isAtSoftSpot(opponent.getCombatHandler().getCurrentStance(), this.getCurrentStance())) {
                    critChance += 0.05f;
                }
                if (!weapon.isArtifact() && Server.rand.nextFloat() < critChance) {
                    crit = true;
                }
            }
        } else {
            miss = true;
        }
        if (!miss && !crit) {
            boolean keepGoing = true;
            defCheck = (double)(Server.rand.nextFloat() * 100.0f) * opponent.getCombatHandler().getDodgeMod();
            defCheck *= (double)opponent.getStatus().getDodgeTypeModifier();
            if (opponent.getMovePenalty() != 0) {
                defCheck *= (double)(1.0f + (float)opponent.getMovePenalty() / 10.0f);
            }
            if ((defCheck *= 1.0 - opponent.getMovementScheme().armourMod.getModifier()) < opponent.getBodyControl() / 3.0) {
                if ((double)(opponent.getStatus().getDodgeTypeModifier() * 100.0f) < opponent.getBodyControl() / 3.0) {
                    logger.log(Level.WARNING, opponent.getName() + " is impossible to hit except for crits: " + opponent.getCombatHandler().getDodgeMod() * 100.0 + " is always less than " + opponent.getBodyControl());
                }
                this.sendDodgeMessage(opponent);
                keepGoing = false;
                String dodgeDetails = "Dodge=" + defCheck + "<" + opponent.getBodyControl() / 3.0 + " dodgemod=" + opponent.getCombatHandler().getDodgeMod() + " dodgeType=" + opponent.getStatus().getDodgeTypeModifier() + " dodgeMovePenalty=" + opponent.getMovePenalty() + " armour=" + opponent.getMovementScheme().armourMod.getModifier();
                if (this.creature.spamMode() && Servers.isThisATestServer()) {
                    this.creature.getCommunicator().sendCombatSafeMessage(dodgeDetails);
                }
                this.creature.sendToLoggers(dodgeDetails, (byte)4);
                CombatHandler.checkIfHitVehicle(this.creature, opponent);
            }
            if (keepGoing) {
                hit = true;
            }
        }
        if (hit || crit) {
            this.creature.sendToLoggers("YOU DAMAGE " + weapon.getName(), (byte)2);
            opponent.sendToLoggers(this.creature.getName() + " DAMAGE " + weapon.getName(), (byte)2);
            dead = this.setDamage(opponent, weapon, damage, pos, type);
        }
        if (dead) {
            this.setKillEffects(this.creature, opponent);
        }
        if (miss) {
            if (this.creature.spamMode() && (chanceToHit > 0.0f || percent > 0.0f && parrPercent > 0.0f)) {
                this.creature.getCommunicator().sendCombatNormalMessage("You miss with the " + weapon.getName() + ".");
                this.creature.sendToLoggers("YOU MISS " + weapon.getName(), (byte)2);
                opponent.sendToLoggers(this.creature.getName() + " MISS " + weapon.getName(), (byte)2);
            }
            if (!this.creature.isUnique() && attCheck - (double)chanceToHit > 50.0 && Server.rand.nextInt(10) == 0) {
                justOpen = true;
                this.setCurrentStance(-1, (byte)9);
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new CreatureLineSegment(this.creature));
                segments.add(new MulticolorLineSegment(" makes a bad move and is an easy target!.", 0));
                opponent.getCommunicator().sendColoredMessageCombat(segments);
                segments.get(1).setText(" make a bad move, making you an easy target.");
                this.creature.getCommunicator().sendColoredMessageCombat(segments);
                this.creature.getCurrentTile().checkOpportunityAttacks(this.creature);
                opponent.getCurrentTile().checkOpportunityAttacks(this.creature);
            } else if (Server.rand.nextInt(10) == 0) {
                CombatHandler.checkIfHitVehicle(this.creature, opponent);
            }
        }
        this.addToSkills = false;
        return dead;
    }

    private static final void checkIfHitVehicle(Creature creature, Creature opponent) {
        Vehicle vehic;
        if (creature.isBreakFence() && opponent.getVehicle() > -10L && (vehic = Vehicles.getVehicleForId(opponent.getVehicle())) != null && !vehic.creature) {
            try {
                Item i = Items.getItem(opponent.getVehicle());
                Server.getInstance().broadCastAction(creature.getNameWithGenus() + " hits the " + i.getName() + " with huge force!", creature, 10, true);
                i.setDamage(i.getDamage() + (float)(damage / 300000.0));
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
    }

    public static void setAttString(Creature _creature, Item _weapon, byte _type) {
        attString = CombatEngine.getAttackString(_creature, _weapon, _type);
    }

    public static void setAttString(String string) {
        attString = string;
    }

    public boolean setDamage(Creature defender, Item attWeapon, double ddamage, byte position, byte _type) {
        float demiseBonus;
        float armourMod = defender.getArmourMod();
        float poisdam = 0.0f;
        if (attWeapon.getSpellVenomBonus() > 0.0f) {
            _type = (byte)5;
        }
        if (attWeapon.enchantment == 90) {
            _type = (byte)10;
        } else if (attWeapon.enchantment == 92) {
            _type = (byte)8;
        } else if (attWeapon.enchantment == 91) {
            _type = (byte)4;
        }
        float infection = 0.0f;
        if (attWeapon.getSpellExtraDamageBonus() > 0.0f) {
            float bloodthirstPower = attWeapon.getSpellExtraDamageBonus();
            if (Server.rand.nextFloat() * 100000.0f <= bloodthirstPower) {
                _type = (byte)6;
                infection = bloodthirstPower / 1000.0f;
            }
        }
        boolean metalArmour = false;
        Item armour = null;
        float bounceWoundPower = 0.0f;
        float evasionChance = ArmourTemplate.calculateGlanceRate(defender.getArmourType(), armour, _type, armourMod);
        if (armourMod == 1.0f || defender.isVehicle() || defender.isKingdomGuard()) {
            try {
                byte bodyPosition = ArmourTemplate.getArmourPosition(position);
                armour = defender.getArmour(bodyPosition);
                armourMod = !defender.isKingdomGuard() ? ArmourTemplate.calculateDR(armour, _type) : (armourMod *= ArmourTemplate.calculateDR(armour, _type));
                defender.sendToLoggers("YOU ARMORMOD " + armourMod, (byte)2);
                this.creature.sendToLoggers(defender.getName() + " ARMORMOD " + armourMod, (byte)2);
                if (defender.isPlayer() || defender.isHorse()) {
                    armour.setDamage(armour.getDamage() + Math.max(0.01f, Math.min(1.0f, (float)(ddamage * Weapon.getMaterialArmourDamageBonus(attWeapon.getMaterial()) * (double)ArmourTemplate.getArmourDamageModFor(armour, _type) / 1200000.0) * armour.getDamageModifier())));
                }
                CombatEngine.checkEnchantDestruction(attWeapon, armour, defender);
                if (armour.isMetal()) {
                    metalArmour = true;
                }
                evasionChance = !defender.isPlayer() ? ArmourTemplate.calculateCreatureGlanceRate(_type, armour) : ArmourTemplate.calculateGlanceRate(null, armour, _type, armourMod);
                evasionChance *= 1.0f + ItemBonus.getGlanceBonusFor(armour.getArmourType(), _type, attWeapon, defender);
            }
            catch (NoArmourException bodyPosition) {
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + position);
            }
            if ((armour == null || armour.getArmourType() != null && armour.getArmourType().getLimitFactor() >= 0.0f) && defender.getBonusForSpellEffect((byte)22) > 0.0f) {
                if (!CombatEngine.isEye(position) || defender.isUnique()) {
                    float omod = 100.0f;
                    float minmod = 0.6f;
                    if (!defender.isPlayer()) {
                        omod = 300.0f;
                        minmod = 0.7f;
                    } else if (defender.getBonusForSpellEffect((byte)22) > 70.0f) {
                        bounceWoundPower = defender.getBonusForSpellEffect((byte)22);
                    }
                    if (armourMod >= 1.0f) {
                        armourMod = 0.3f + (float)(1.0 - Server.getBuffedQualityEffect(defender.getBonusForSpellEffect((byte)22) / omod)) * minmod;
                        evasionChance = (float)Server.getBuffedQualityEffect(defender.getBonusForSpellEffect((byte)22) / 100.0f) / 3.0f;
                    } else {
                        armourMod = Math.min(armourMod, 0.3f + (float)(1.0 - Server.getBuffedQualityEffect(defender.getBonusForSpellEffect((byte)22) / omod)) * minmod);
                    }
                }
            } else if (defender.isReborn()) {
                armourMod = (float)(1.0 - Server.getBuffedQualityEffect(defender.getStrengthSkill() / 100.0));
            }
        }
        if (defender.isUnique()) {
            evasionChance = 0.5f;
        }
        if (!attWeapon.isBodyPartAttached() && this.creature.isPlayer()) {
            boolean rust = defender.hasSpellEffect((byte)70);
            if (rust) {
                this.creature.getCommunicator().sendAlertServerMessage("Your " + attWeapon.getName() + " takes excessive damage from " + defender.getNameWithGenus() + ".");
            }
            float mod = rust ? 5.0f : 1.0f;
            attWeapon.setDamage(attWeapon.getDamage() + Math.min(1.0f, (float)(ddamage * (double)armourMod / 1000000.0)) * attWeapon.getDamageModifier() * mod);
        }
        double defdamage = ddamage * (double)ItemBonus.getDamReductionBonusFor(armour != null ? armour.getArmourType() : defender.getArmourType(), _type, attWeapon, defender);
        if (attWeapon.getSpellVenomBonus() > 0.0f) {
            defdamage *= (double)(0.8f + 0.2f * (attWeapon.getSpellVenomBonus() / 100.0f));
        }
        if (defender.isPlayer()) {
            if (((Player)defender).getAlcohol() > 50.0f) {
                defdamage *= 0.5;
            }
            if (defender.fightlevel >= 5) {
                defdamage *= 0.5;
            }
        }
        if (defender.hasTrait(2)) {
            defdamage *= (double)0.9f;
        }
        if ((demiseBonus = EnchantUtil.getDemiseBonus(attWeapon, defender)) > 0.0f) {
            defdamage *= (double)(1.0f + demiseBonus);
        }
        if (this.creature.hasSpellEffect((byte)67) && !attWeapon.isArtifact()) {
            crit = true;
        }
        if (crit && !defender.isUnique()) {
            armourMod *= 1.5f;
        }
        if (defender.getTemplate().isTowerBasher() && (this.creature.isSpiritGuard() || this.creature.isKingdomGuard())) {
            float mod = 1.0f / defender.getArmourMod();
            defdamage = Math.max((double)((float)(500 + Server.rand.nextInt(1000)) * mod), defdamage);
        }
        if (Server.rand.nextFloat() < evasionChance) {
            if (this.creature.spamMode()) {
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new MulticolorLineSegment("Your attack glances off ", 0));
                segments.add(new CreatureLineSegment(defender));
                segments.add(new MulticolorLineSegment("'s armour.", 0));
                this.creature.getCommunicator().sendColoredMessageCombat(segments);
            }
            if (defender.spamMode()) {
                defender.getCommunicator().sendCombatNormalMessage("The attack to the " + defender.getBody().getWoundLocationString(pos) + " glances off your armour.");
            }
            this.creature.sendToLoggers(defender.getName() + " GLANCE", (byte)2);
            defender.sendToLoggers("YOU GLANCE", (byte)2);
        } else if (defdamage * (double)armourMod >= 500.0) {
            float extraDmg;
            ItemSpellEffects speffs;
            SpellEffect speff;
            float champMod;
            if (this.creature.hasSpellEffect((byte)67) && !attWeapon.isArtifact()) {
                this.creature.removeTrueStrike();
            }
            if (attWeapon != null && !attWeapon.isBodyPartRemoved() && !attWeapon.isWeaponBow()) {
                try {
                    int primweaponskill = 10052;
                    if (!attWeapon.isBodyPartAttached()) {
                        primweaponskill = attWeapon.getPrimarySkill();
                    }
                    try {
                        Skill pwsk = this.creature.getSkills().getSkill(primweaponskill);
                        double d = pwsk.skillCheck(pwsk.getKnowledge(), attWeapon, 0.0, defender.isNoSkillFor(this.creature), (float)defdamage * armourMod / 1000.0f);
                    }
                    catch (NoSuchSkillException nss1) {
                        this.creature.getSkills().learn(primweaponskill, 1.0f);
                    }
                }
                catch (NoSuchSkillException primweaponskill) {
                    // empty catch block
                }
            }
            if (Servers.isThisATestServer()) {
                String message = String.format("Base Damage: %.1f, Armour DR: %.2f%%, Final Damage: %.1f. Critical: %s", defdamage, Float.valueOf((1.0f - armourMod) * 100.0f), defdamage * (double)armourMod, crit);
                if (this.creature.spamMode()) {
                    this.creature.getCommunicator().sendCombatSafeMessage(message);
                }
                if (defender.spamMode()) {
                    defender.getCommunicator().sendCombatAlertMessage(message);
                }
            }
            this.creature.sendToLoggers(defender.getName() + " DAMAGED " + defdamage * (double)armourMod + " crit=" + crit, (byte)2);
            defender.sendToLoggers("YOU DAMAGED " + defdamage * (double)armourMod + " crit=" + crit, (byte)2);
            Battle battle = defender.getBattle();
            dead = false;
            float f = champMod = defender.isChampion() ? 0.4f : 1.0f;
            if (armour != null && armour.getSpellPainShare() > 0.0f) {
                bounceWoundPower = armour.getSpellPainShare();
                int rarityModifier = Math.max(1, armour.getRarity() * 5);
                speff = armour.getSpellEffect((byte)17);
                if (speff != null && Server.rand.nextInt(Math.max(2, (int)((float)rarityModifier * speff.power * 80.0f))) == 0) {
                    speff.setPower(speff.getPower() - 1.0f);
                    if (speff.getPower() <= 0.0f && (speffs = armour.getSpellEffects()) != null) {
                        speffs.removeSpellEffect(speff.type);
                    }
                }
            }
            if (defender.isUnique() && this.creature.isUnique() && defender.getStatus().damage > 10000) {
                defender.setTarget(-10L, true);
                this.creature.setTarget(-10L, true);
                defender.setOpponent(null);
                this.creature.setOpponent(null);
                try {
                    defender.checkMove();
                }
                catch (Exception rarityModifier) {
                    // empty catch block
                }
                try {
                    this.creature.checkMove();
                }
                catch (Exception rarityModifier) {
                    // empty catch block
                }
            }
            if (defender.isSparring(this.creature)) {
                if ((double)defender.getStatus().damage + defdamage * (double)armourMod * 2.0 > 65535.0) {
                    Item weapon;
                    defender.setTarget(-10L, true);
                    this.creature.setTarget(-10L, true);
                    defender.setOpponent(null);
                    this.creature.setOpponent(null);
                    this.creature.getCommunicator().sendCombatSafeMessage("You win against " + defender.getName() + "! Congratulations!");
                    defender.getCommunicator().sendCombatNormalMessage("You lose against " + this.creature.getName() + " who stops just before finishing you off!");
                    Server.getInstance().broadCastAction(this.creature.getName() + " defeats " + defender.getName() + " while sparring!", this.creature, defender, 10);
                    this.creature.getCommunicator().sendCombatOptions(NO_COMBAT_OPTIONS, (short)0);
                    this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
                    this.creature.achievement(39);
                    if (!Servers.localServer.PVPSERVER) {
                        this.creature.achievement(8);
                    }
                    if ((weapon = this.creature.getPrimWeapon()) != null) {
                        if (weapon.isWeaponBow()) {
                            this.creature.achievement(11);
                        } else if (weapon.isWeaponSword()) {
                            this.creature.achievement(14);
                        } else if (weapon.isWeaponCrush()) {
                            this.creature.achievement(17);
                        } else if (weapon.isWeaponAxe()) {
                            this.creature.achievement(20);
                        } else if (weapon.isWeaponKnife()) {
                            this.creature.achievement(25);
                        }
                        if (weapon.getTemplateId() == 314) {
                            this.creature.achievement(27);
                        } else if (weapon.getTemplateId() == 567) {
                            this.creature.achievement(29);
                        } else if (weapon.getTemplateId() == 20) {
                            this.creature.achievement(30);
                        }
                    }
                    return true;
                }
                if (bounceWoundPower > 0.0f && defdamage * (double)bounceWoundPower * (double)champMod / 300.0 > 500.0 && (double)this.creature.getStatus().damage + defdamage * (double)bounceWoundPower * (double)champMod / 300.0 > 65535.0) {
                    Item weapon;
                    defender.setTarget(-10L, true);
                    this.creature.setTarget(-10L, true);
                    defender.setOpponent(null);
                    this.creature.setOpponent(null);
                    defender.getCommunicator().sendCombatSafeMessage("You win against " + this.creature.getName() + "! Congratulations!");
                    this.creature.getCommunicator().sendCombatNormalMessage("You lose against " + defender.getName() + " whose armour enchantment almost finished you off!");
                    Server.getInstance().broadCastAction(defender.getName() + " defeats " + this.creature.getName() + " while sparring!", defender, this.creature, 10);
                    this.creature.getCommunicator().sendCombatOptions(NO_COMBAT_OPTIONS, (short)0);
                    this.creature.getCommunicator().sendSpecialMove((short)-1, "N/A");
                    this.creature.achievement(39);
                    if (!Servers.localServer.PVPSERVER) {
                        this.creature.achievement(8);
                    }
                    if ((weapon = this.creature.getPrimWeapon()) != null) {
                        if (weapon.isWeaponBow()) {
                            this.creature.achievement(11);
                        } else if (weapon.isWeaponSword()) {
                            this.creature.achievement(14);
                        } else if (weapon.isWeaponCrush()) {
                            this.creature.achievement(17);
                        } else if (weapon.isWeaponAxe()) {
                            this.creature.achievement(20);
                        } else if (weapon.isWeaponKnife()) {
                            this.creature.achievement(25);
                        }
                        if (weapon.getTemplateId() == 314) {
                            this.creature.achievement(27);
                        } else if (weapon.getTemplateId() == 567) {
                            this.creature.achievement(29);
                        } else if (weapon.getTemplateId() == 20) {
                            this.creature.achievement(30);
                        }
                    }
                    return true;
                }
            }
            if (defender.getStaminaSkill().getKnowledge() < 2.0) {
                defender.die(false, "Combat Stam Check Fail");
                this.creature.achievement(223);
                dead = true;
            } else if (attWeapon.getWeaponSpellDamageBonus() > 0.0f) {
                defdamage += defdamage * (double)attWeapon.getWeaponSpellDamageBonus() / 500.0;
                dead = CombatEngine.addWound(this.creature, defender, _type, position, defdamage, armourMod, attString, battle, Server.rand.nextInt((int)Math.max(1.0f, attWeapon.getWeaponSpellDamageBonus())), poisdam, false, false, false, false);
                if (attWeapon.isWeaponCrush() && attWeapon.getWeightGrams() > 4000 && armour != null && armour.getTemplateId() == 286) {
                    defender.achievement(49);
                }
            } else {
                Wound[] w;
                int dmgBefore = defender.getStatus().damage;
                dead = CombatEngine.addWound(this.creature, defender, _type, position, defdamage, armourMod, attString, battle, infection, poisdam, false, false, false, false);
                float lifeTransferPower = Math.max(attWeapon.getSpellLifeTransferModifier(), attWeapon.getSpellEssenceDrainModifier() / 3.0f);
                if (lifeTransferPower > 0.0f && dmgBefore != defender.getStatus().damage && this.creature.getBody() != null && this.creature.getBody().getWounds() != null && (w = this.creature.getBody().getWounds().getWounds()).length > 0) {
                    float mod = 500.0f;
                    if (this.creature.isChampion()) {
                        mod = 1000.0f;
                    } else if (this.creature.getCultist() != null && this.creature.getCultist().healsFaster()) {
                        mod = 250.0f;
                    }
                    double toHeal = defdamage * (double)lifeTransferPower / (double)mod;
                    double resistance = SpellResist.getSpellResistance(this.creature, 409);
                    toHeal *= resistance;
                    Wound targetWound = w[0];
                    for (Wound wound : w) {
                        if (!(wound.getSeverity() > targetWound.getSeverity())) continue;
                        targetWound = wound;
                    }
                    SpellResist.addSpellResistance(this.creature, 409, Math.min((double)targetWound.getSeverity(), toHeal));
                    targetWound.modifySeverity(-((int)toHeal));
                }
            }
            if (this.creature.isPlayer() != defender.isPlayer() && defdamage > 10000.0 && defender.fightlevel > 0) {
                defender.fightlevel = (byte)(defender.fightlevel - 1);
                defender.getCommunicator().sendCombatNormalMessage("You lose some focus.");
                if (defender.isPlayer()) {
                    defender.getCommunicator().sendFocusLevel(defender.getWurmId());
                }
            }
            if (!dead && attWeapon.getSpellDamageBonus() > 0.0f && ((double)(attWeapon.getSpellDamageBonus() / 300.0f) * defdamage > 500.0 || crit)) {
                dead = defender.addWoundOfType(this.creature, (byte)4, position, false, armourMod, false, (double)(attWeapon.getSpellDamageBonus() / 300.0f) * defdamage, 0.0f, 0.0f, true, true);
            }
            if (!dead && attWeapon.getSpellFrostDamageBonus() > 0.0f && ((double)(attWeapon.getSpellFrostDamageBonus() / 300.0f) * defdamage > 500.0 || crit)) {
                dead = defender.addWoundOfType(this.creature, (byte)8, position, false, armourMod, false, (double)(attWeapon.getSpellFrostDamageBonus() / 300.0f) * defdamage, 0.0f, 0.0f, true, true);
            }
            if (!dead && attWeapon.getSpellEssenceDrainModifier() > 0.0f && ((double)(attWeapon.getSpellEssenceDrainModifier() / 1000.0f) * defdamage > 500.0 || crit)) {
                dead = defender.addWoundOfType(this.creature, (byte)9, position, false, armourMod, false, (double)(attWeapon.getSpellEssenceDrainModifier() / 1000.0f) * defdamage, 0.0f, 0.0f, true, true);
            }
            if (!dead && Weapon.getMaterialExtraWoundMod(attWeapon.getMaterial()) > 0.0f && ((double)(extraDmg = Weapon.getMaterialExtraWoundMod(attWeapon.getMaterial())) * defdamage > 500.0 || crit)) {
                dead = defender.addWoundOfType(this.creature, Weapon.getMaterialExtraWoundType(attWeapon.getMaterial()), position, false, armourMod, false, (double)extraDmg * defdamage, 0.0f, 0.0f, false, true);
            }
            if (armour != null || bounceWoundPower > 0.0f) {
                if (bounceWoundPower > 0.0f) {
                    if (this.creature.isUnique()) {
                        if (armour != null) {
                            defender.getCommunicator().sendCombatNormalMessage(this.creature.getNameWithGenus() + " ignores the effects of the " + armour.getName() + ".");
                        }
                    } else if (defdamage * (double)bounceWoundPower * (double)champMod / 300.0 > 500.0) {
                        CombatEngine.addBounceWound(defender, this.creature, _type, position, defdamage * (double)bounceWoundPower * (double)champMod / 300.0, armourMod, 0.0f, 0.0f, false, true);
                    }
                } else if (armour != null && armour.getSpellSlowdown() > 0.0f) {
                    if (this.creature.getMovementScheme().setWebArmourMod(true, armour.getSpellSlowdown())) {
                        this.creature.setWebArmourModTime(armour.getSpellSlowdown() / 10.0f);
                        this.creature.getCommunicator().sendCombatAlertMessage("Dark stripes spread along your " + attWeapon.getName() + " from " + defender.getNamePossessive() + " armour. You feel drained.");
                    }
                    int rm = Math.max(1, armour.getRarity() * 5);
                    speff = armour.getSpellEffect((byte)46);
                    if (speff != null && Server.rand.nextInt(Math.max(2, (int)((float)rm * speff.power * 80.0f))) == 0) {
                        speff.setPower(speff.getPower() - 1.0f);
                        if (speff.getPower() <= 0.0f && (speffs = armour.getSpellEffects()) != null) {
                            speffs.removeSpellEffect(speff.type);
                        }
                    }
                }
            }
            if (!Players.getInstance().isOverKilling(this.creature.getWurmId(), defender.getWurmId()) && attWeapon.getSpellExtraDamageBonus() > 0.0f) {
                if (defender.isPlayer() && !defender.isNewbie()) {
                    SpellEffect speff2 = attWeapon.getSpellEffect((byte)45);
                    float mod = 1.0f;
                    if (defdamage * (double)armourMod * (double)champMod < 5000.0) {
                        mod = (float)(defdamage * (double)armourMod * (double)champMod / 5000.0);
                    }
                    if (speff2 != null) {
                        speff2.setPower(Math.min(10000.0f, speff2.power + (dead ? 20.0f : 2.0f * mod)));
                    }
                } else if (!defender.isPlayer() && !defender.isGuard() && dead) {
                    SpellEffect speff3 = attWeapon.getSpellEffect((byte)45);
                    float mod = 1.0f;
                    if (speff3.getPower() > 5000.0f && !Servers.isThisAnEpicOrChallengeServer()) {
                        mod = Math.max(0.5f, 1.0f - (speff3.getPower() - 5000.0f) / 5000.0f);
                    }
                    if (speff3 != null) {
                        speff3.setPower(Math.min(10000.0f, speff3.power + defender.getBaseCombatRating() * mod));
                    }
                }
            }
            if (dead) {
                if (battle != null) {
                    battle.addCasualty(this.creature, defender);
                }
                if (defender.isSparring(this.creature) && (double)defender.getStatus().damage + defdamage * (double)armourMod * 2.0 > 65535.0) {
                    Item weapon;
                    this.creature.achievement(39);
                    if (!Servers.localServer.PVPSERVER) {
                        this.creature.achievement(8);
                    }
                    if ((weapon = this.creature.getPrimWeapon()) != null) {
                        if (weapon.isWeaponBow()) {
                            this.creature.achievement(11);
                        } else if (weapon.isWeaponSword()) {
                            this.creature.achievement(14);
                        } else if (weapon.isWeaponCrush()) {
                            this.creature.achievement(17);
                        } else if (weapon.isWeaponAxe()) {
                            this.creature.achievement(20);
                        } else if (weapon.isWeaponKnife()) {
                            this.creature.achievement(25);
                        }
                        if (weapon.getTemplateId() == 314) {
                            this.creature.achievement(27);
                        } else if (weapon.getTemplateId() == 567) {
                            this.creature.achievement(29);
                        } else if (weapon.getTemplateId() == 20) {
                            this.creature.achievement(30);
                        }
                    }
                    this.creature.getCommunicator().sendCombatSafeMessage("You accidentally slay " + defender.getName() + "! Congratulations!");
                    defender.getCommunicator().sendCombatNormalMessage("You lose against " + this.creature.getName() + " who unfortunately fails to stop just before finishing you off!");
                    Server.getInstance().broadCastAction(this.creature.getName() + " defeats and accidentally slays " + defender.getName() + " while sparring!", this.creature, defender, 10);
                }
                if (this.creature.isDuelling(defender)) {
                    this.creature.achievement(37);
                }
            } else if (defdamage > 30000.0 && (double)Server.rand.nextInt(100000) < defdamage) {
                Skill defBodyControl = null;
                try {
                    defBodyControl = defender.getSkills().getSkill(104);
                }
                catch (NoSuchSkillException nss) {
                    defBodyControl = defender.getSkills().learn(104, 1.0f);
                }
                if (defBodyControl.skillCheck(defdamage / 10000.0, (double)(defender.getCombatHandler().getFootingModifier(attWeapon, this.creature) * 10.0f), false, 10.0f, defender, this.creature) < 0.0) {
                    defender.getCombatHandler().setCurrentStance(-1, (byte)8);
                    defender.getStatus().setStunned((byte)Math.max(3.0, defdamage / 10000.0), false);
                    ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                    segments.add(new CreatureLineSegment(defender));
                    segments.add(new MulticolorLineSegment(" is knocked senseless from the hit.", 0));
                    this.creature.getCommunicator().sendColoredMessageCombat(segments);
                    defender.getCommunicator().sendCombatNormalMessage("You are knocked senseless from the hit.");
                    segments.clear();
                    segments.add(new CreatureLineSegment(this.creature));
                    segments.add(new MulticolorLineSegment(" knocks ", 0));
                    segments.add(new CreatureLineSegment(defender));
                    segments.add(new MulticolorLineSegment(" senseless with " + this.creature.getHisHerItsString() + " hit!", 0));
                    MessageServer.broadcastColoredAction(segments, this.creature, defender, 5, true);
                }
            }
            int numsound = Server.rand.nextInt(3);
            if (defdamage > 10000.0) {
                if (numsound == 0) {
                    SoundPlayer.playSound("sound.combat.fleshbone1", defender, 1.6f);
                } else if (numsound == 1) {
                    SoundPlayer.playSound("sound.combat.fleshbone2", defender, 1.6f);
                } else if (numsound == 2) {
                    SoundPlayer.playSound("sound.combat.fleshbone3", defender, 1.6f);
                }
            } else if (metalArmour) {
                if (numsound == 0) {
                    SoundPlayer.playSound("sound.combat.fleshmetal1", defender, 1.6f);
                } else if (numsound == 1) {
                    SoundPlayer.playSound("sound.combat.fleshmetal2", defender, 1.6f);
                } else if (numsound == 2) {
                    SoundPlayer.playSound("sound.combat.fleshmetal3", defender, 1.6f);
                }
            } else if (numsound == 0) {
                SoundPlayer.playSound("sound.combat.fleshhit1", defender, 1.6f);
            } else if (numsound == 1) {
                SoundPlayer.playSound("sound.combat.fleshhit2", defender, 1.6f);
            } else if (numsound == 2) {
                SoundPlayer.playSound("sound.combat.fleshhit3", defender, 1.6f);
            }
            SoundPlayer.playSound(defender.getHitSound(), defender, 1.6f);
        } else {
            if (aiming || this.creature.spamMode()) {
                ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                segments.add(new CreatureLineSegment(defender));
                segments.add(new MulticolorLineSegment(" takes no real damage from the hit to the " + defender.getBody().getWoundLocationString(position) + ".", 0));
                this.creature.getCommunicator().sendColoredMessageCombat(segments);
            }
            if (defender.spamMode()) {
                defender.getCommunicator().sendCombatNormalMessage("You take no real damage from the blow to the " + defender.getBody().getWoundLocationString(position) + ".");
            }
            this.creature.sendToLoggers(defender.getName() + " NO DAMAGE", (byte)2);
            defender.sendToLoggers("YOU TAKE NO DAMAGE", (byte)2);
        }
        return dead;
    }

    private static void setDefenderWeaponSkill(Item defPrimWeapon) {
        block7: {
            int skillnum = -10;
            if (defPrimWeapon != null) {
                if (defPrimWeapon.isBodyPart()) {
                    try {
                        skillnum = 10052;
                        defPrimWeaponSkill = defenderSkills.getSkill(skillnum);
                    }
                    catch (NoSuchSkillException nss) {
                        if (skillnum != -10) {
                            defPrimWeaponSkill = defenderSkills.learn(skillnum, 1.0f);
                        }
                        break block7;
                    }
                }
                try {
                    skillnum = defPrimWeapon.getPrimarySkill();
                    defPrimWeaponSkill = defenderSkills.getSkill(skillnum);
                }
                catch (NoSuchSkillException nss) {
                    if (skillnum == -10) break block7;
                    defPrimWeaponSkill = defenderSkills.learn(skillnum, 1.0f);
                }
            }
        }
    }

    private static float getWeaponParryBonus(Item weapon) {
        if (weapon.isWeaponSword()) {
            return 2.0f;
        }
        return 1.0f;
    }

    private float checkDefenderParry(Creature defender, Item attWeapon) {
        defCheck = 0.0;
        boolean parried = false;
        int parryTime = 200;
        if (defender.getFightStyle() == 2) {
            parryTime = 120;
        } else if (defender.getFightStyle() == 1) {
            parryTime = 360;
        }
        parryBonus = CombatHandler.getParryBonus(defender.getCombatHandler().currentStance, this.currentStance);
        if (defender.fightlevel > 0) {
            parryBonus -= (float)(defender.fightlevel * 4) / 100.0f;
        }
        if (defender.getPrimWeapon() != null) {
            parryBonus *= Weapon.getMaterialParryBonus(defender.getPrimWeapon().getMaterial());
        }
        if (WurmCalendar.currentTime > defender.lastParry + (long)Server.rand.nextInt(parryTime = (int)((float)parryTime * parryBonus))) {
            defParryWeapon = defender.getPrimWeapon();
            if (Weapon.getWeaponParryPercent(defParryWeapon) > 0.0f) {
                if (defParryWeapon.isTwoHanded() && defShield != null) {
                    defParryWeapon = null;
                    parried = false;
                } else {
                    parried = true;
                }
            } else {
                defParryWeapon = null;
            }
            if (!(parried && Server.rand.nextInt(3) != 0 || defShield != null || (defLeftWeapon = defender.getLefthandWeapon()) == defParryWeapon)) {
                if (defLeftWeapon != null && (defLeftWeapon.getSizeZ() > defender.getSize() * 10 || Weapon.getWeaponParryPercent(defLeftWeapon) <= 0.0f)) {
                    defLeftWeapon = null;
                }
                if (defLeftWeapon != null) {
                    if (defParryWeapon != null && parried) {
                        if (defLeftWeapon.getSizeZ() > defParryWeapon.getSizeZ()) {
                            defParryWeapon = defLeftWeapon;
                        }
                    } else {
                        defParryWeapon = defLeftWeapon;
                    }
                }
            }
            if (defParryWeapon != null && Weapon.getWeaponParryPercent(defParryWeapon) > Server.rand.nextFloat()) {
                defCheck = -1.0;
                if (defender.getStatus().getStamina() >= 300) {
                    double pdiff;
                    CombatHandler.setDefenderWeaponSkill(defParryWeapon);
                    if (defPrimWeaponSkill != null && (!defender.isMoving() || defPrimWeaponSkill.getRealKnowledge() > 40.0)) {
                        pdiff = Math.max(1.0, (attCheck - defBonus + (double)((float)defParryWeapon.getWeightGrams() / 100.0f)) / (double)CombatHandler.getWeaponParryBonus(defParryWeapon) * (1.0 - this.getParryMod()));
                        if (!defender.isPlayer()) {
                            pdiff *= (double)defender.getStatus().getParryTypeModifier();
                        }
                        defCheck = defPrimWeaponSkill.skillCheck(pdiff * (double)ItemBonus.getParryBonus(defender, defParryWeapon), defParryWeapon, 0.0, this.creature.isNoSkillFor(defender) || defParryWeapon.isWeaponBow(), 1.0f, defender, this.creature);
                        defender.lastParry = WurmCalendar.currentTime;
                        defender.getStatus().modifyStamina(-300.0f);
                    }
                    if (defCheck < 0.0 && Server.rand.nextInt(20) == 0 && defLeftWeapon != null && !defLeftWeapon.equals(defParryWeapon)) {
                        CombatHandler.setDefenderWeaponSkill(defLeftWeapon);
                        if (!defender.isMoving() || defPrimWeaponSkill.getRealKnowledge() > 40.0) {
                            pdiff = Math.max(1.0, (attCheck - defBonus + (double)((float)defLeftWeapon.getWeightGrams() / 100.0f)) / (double)CombatHandler.getWeaponParryBonus(defLeftWeapon) * this.getParryMod());
                            defCheck = defPrimWeaponSkill.skillCheck((pdiff *= (double)defender.getStatus().getParryTypeModifier()) * (double)ItemBonus.getParryBonus(defender, defParryWeapon), defLeftWeapon, 0.0, this.creature.isNoSkillFor(defender) || defParryWeapon.isWeaponBow(), 1.0f, defender, this.creature);
                            defender.lastParry = WurmCalendar.currentTime;
                            defender.getStatus().modifyStamina(-300.0f);
                        }
                    }
                    if (defCheck > 0.0) {
                        this.setParryEffects(defender, attWeapon, defCheck);
                    }
                }
            }
        }
        return (float)defCheck;
    }

    private void setParryEffects(Creature defender, Item attWeapon, double parryEff) {
        defender.lastParry = WurmCalendar.currentTime;
        if (aiming || this.creature.spamMode()) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" " + CombatEngine.getParryString(parryEff) + " parries with " + defParryWeapon.getNameWithGenus() + ".", 0));
            this.creature.getCommunicator().sendColoredMessageCombat(segments);
        }
        if (defender.spamMode()) {
            defender.getCommunicator().sendCombatNormalMessage("You " + CombatEngine.getParryString(parryEff) + " parry with your " + defParryWeapon.getName() + ".");
        }
        if (!defParryWeapon.isBodyPart() || defParryWeapon.getAuxData() == 100) {
            float vulnerabilityModifier = 1.0f;
            if (defender.isPlayer()) {
                if (attWeapon.isMetal() && Weapon.isWeaponDamByMetal(defParryWeapon)) {
                    vulnerabilityModifier = 4.0f;
                }
                if (defParryWeapon.isWeaponSword()) {
                    defParryWeapon.setDamage(defParryWeapon.getDamage() + 1.0E-7f * (float)damage * defParryWeapon.getDamageModifier() * vulnerabilityModifier);
                } else {
                    defParryWeapon.setDamage(defParryWeapon.getDamage() + 2.0E-7f * (float)damage * defParryWeapon.getDamageModifier() * vulnerabilityModifier);
                }
            }
            if (this.creature.isPlayer()) {
                vulnerabilityModifier = 1.0f;
                if (defParryWeapon.isMetal() && Weapon.isWeaponDamByMetal(attWeapon)) {
                    vulnerabilityModifier = 4.0f;
                }
                if (!(!attWeapon.isBodyPartAttached())) {
                    attWeapon.setDamage(attWeapon.getDamage() + 1.0E-7f * (float)damage * attWeapon.getDamageModifier() * vulnerabilityModifier);
                }
            }
        }
        this.creature.sendToLoggers(defender.getName() + " PARRY " + parryEff, (byte)2);
        defender.sendToLoggers("YOU PARRY " + parryEff, (byte)2);
        String lSstring = CombatHandler.getParrySound(Server.rand);
        SoundPlayer.playSound(lSstring, defender, 1.6f);
        CombatEngine.checkEnchantDestruction(attWeapon, defParryWeapon, defender);
        defender.playAnimation("parry.weapon", false);
    }

    static String getParrySound(Random aRandom) {
        int x = aRandom.nextInt(3);
        String lSstring = x == 0 ? "sound.combat.parry2" : (x == 1 ? "sound.combat.parry3" : "sound.combat.parry1");
        return lSstring;
    }

    public void increaseUseShieldCounter() {
        ++this.usedShieldThisRound;
    }

    private float checkShield(Creature defender, Item weapon) {
        if (defender.getCombatHandler().usedShieldThisRound > 1) {
            return 0.0f;
        }
        defShield = defender.getShield();
        defCheck = 0.0;
        float blockPercent = 0.0f;
        if (defShield != null) {
            Item defweapon = defender.getPrimWeapon();
            if (defweapon != null && defweapon.isTwoHanded()) {
                return 0.0f;
            }
            Item defSecondWeapon = defender.getLefthandWeapon();
            if (defSecondWeapon != null && defSecondWeapon.isTwoHanded()) {
                return 0.0f;
            }
            if (!defShield.isArtifact()) {
                ++defender.getCombatHandler().usedShieldThisRound;
            }
            if (VirtualZone.isCreatureShieldedVersusTarget(this.creature, defender)) {
                Skill defShieldSkill;
                block16: {
                    int skillnum = -10;
                    defShieldSkill = null;
                    try {
                        skillnum = defShield.getPrimarySkill();
                        defShieldSkill = defenderSkills.getSkill(skillnum);
                    }
                    catch (NoSuchSkillException nss) {
                        if (skillnum == -10) break block16;
                        defShieldSkill = defenderSkills.learn(skillnum, 1.0f);
                    }
                }
                if (defShieldSkill != null) {
                    if (pos == 9) {
                        blockPercent = 100.0f;
                        if (defender.spamMode() && Servers.isThisATestServer()) {
                            defender.getCommunicator().sendCombatNormalMessage("Blocking left underarm.");
                        }
                    } else if (!(defender.getStatus().getStamina() < 300 && Server.rand.nextInt(10) != 0 || defender.isMoving() && !(defShieldSkill.getRealKnowledge() > 40.0))) {
                        double shieldModifier = (float)(defShield.getSizeY() + defShield.getSizeZ()) / 2.0f * (defShield.getCurrentQualityLevel() / 100.0f);
                        double diff = Math.max(1.0, (double)chanceToHit - shieldModifier) - defBonus;
                        blockPercent = (float)defShieldSkill.skillCheck(diff, defShield, defShield.isArtifact() ? 50.0 : 0.0, this.creature.isNoSkillFor(defender) || defender.getCombatHandler().receivedShieldSkill, (float)(damage / 1000.0), defender, this.creature);
                        defender.getCombatHandler().receivedShieldSkill = true;
                        if (defender.spamMode() && Servers.isThisATestServer()) {
                            defender.getCommunicator().sendCombatNormalMessage("Shield parrying difficulty=" + diff + " including defensive bonus " + defBonus + " vs " + defShieldSkill.getKnowledge(defShield, 0.0) + " " + defender.zoneBonus + ":" + defender.getMovePenalty() + " gave " + blockPercent + ">0");
                        }
                        defender.getStatus().modifyStamina((int)(-300.0f - (float)defShield.getWeightGrams() / 20.0f));
                    }
                    if (blockPercent > 0.0f) {
                        float damageMod = !weapon.isBodyPart() && weapon.isWeaponCrush() ? 1.5E-5f : (type == 0 ? 1.0E-6f : 5.0E-6f);
                        if (defender.isPlayer()) {
                            defShield.setDamage(defShield.getDamage() + Math.max(0.01f, damageMod * (float)damage * defShield.getDamageModifier()));
                        }
                        this.sendShieldMessage(defender, weapon, blockPercent);
                    }
                }
            }
        }
        return blockPercent;
    }

    private void sendShieldMessage(Creature defender, Item weapon, float blockPercent) {
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new CreatureLineSegment(defender));
        segments.add(new MulticolorLineSegment(" raises " + defender.getHisHerItsString() + " shield and parries your " + attString + ".", 0));
        if (aiming || this.creature.spamMode()) {
            this.creature.getCommunicator().sendColoredMessageCombat(segments);
        }
        if (defender.spamMode()) {
            segments.get(1).setText(" raise your shield and parry against ");
            segments.add(new CreatureLineSegment(this.creature));
            segments.add(new MulticolorLineSegment("'s " + attString + ".", 0));
            defender.getCommunicator().sendColoredMessageCombat(segments);
        }
        if (defShield.isWood()) {
            Methods.sendSound(defender, "sound.combat.shield.wood");
        } else {
            Methods.sendSound(defender, "sound.combat.shield.metal");
        }
        CombatEngine.checkEnchantDestruction(weapon, defShield, defender);
        this.creature.sendToLoggers(defender.getName() + " SHIELD " + blockPercent, (byte)2);
        defender.sendToLoggers("You SHIELD " + blockPercent, (byte)2);
        defender.playAnimation("parry.shield", false);
    }

    private void sendDodgeMessage(Creature defender) {
        double power = (float)(defender.getBodyControl() / 3.0 - defCheck);
        String sstring = power > 20.0 ? "sound.combat.miss.heavy" : (power > 10.0 ? "sound.combat.miss.med" : "sound.combat.miss.light");
        SoundPlayer.playSound(sstring, this.creature, 1.6f);
        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
        segments.add(new CreatureLineSegment(defender));
        segments.add(new MulticolorLineSegment(" " + CombatEngine.getParryString(power) + " evades the blow to the " + defender.getBody().getWoundLocationString(pos) + ".", 0));
        if (aiming || this.creature.spamMode()) {
            this.creature.getCommunicator().sendColoredMessageCombat(segments);
        }
        if (defender.spamMode()) {
            segments.get(1).setText(" " + CombatEngine.getParryString(power) + " evade the blow to the " + defender.getBody().getWoundLocationString(pos) + ".");
            defender.getCommunicator().sendColoredMessageCombat(segments);
        }
        this.creature.sendToLoggers(defender.getName() + " EVADE", (byte)2);
        defender.sendToLoggers("You EVADE", (byte)2);
        defender.playAnimation(dodge, false);
    }

    private final double getDamage(Creature _creature, AttackAction attk, Creature opponent) {
        Skill attStrengthSkill = null;
        try {
            attStrengthSkill = _creature.getSkills().getSkill(102);
        }
        catch (NoSuchSkillException nss) {
            attStrengthSkill = _creature.getSkills().learn(102, 1.0f);
            logger.log(Level.WARNING, _creature.getName() + " had no strength. Weird.");
        }
        Item weapon = _creature.getPrimWeapon(!attk.isUsingWeapon());
        if (!attk.isUsingWeapon()) {
            damage = attk.getAttackValues().getBaseDamage() * 1000.0f * _creature.getStatus().getDamageTypeModifier();
            if (_creature.isPlayer()) {
                Skill weaponLess = _creature.getWeaponLessFightingSkill();
                double modifier = 1.0 + 2.0 * weaponLess.getKnowledge() / 100.0;
                damage *= modifier;
            }
            if (damage < 10000.0 && _creature.getBonusForSpellEffect((byte)24) > 0.0f) {
                damage = _creature.isPlayer() ? (damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0f) * 15000.0) : (damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0f) * 5000.0);
            }
            float randomizer = (50.0f + Server.rand.nextFloat() * 50.0f) / 100.0f;
            damage *= (double)randomizer;
        } else {
            damage = Weapon.getModifiedDamageForWeapon(weapon, attStrengthSkill, opponent.getTemplate().getTemplateId() == 116) * 1000.0;
            damage += Server.getBuffedQualityEffect(weapon.getCurrentQualityLevel() / 100.0f) * (double)Weapon.getBaseDamageForWeapon(weapon) * 2400.0;
            damage *= Weapon.getMaterialDamageBonus(weapon.getMaterial());
            if (!opponent.isPlayer() && opponent.isHunter()) {
                damage *= Weapon.getMaterialHunterDamageBonus(weapon.getMaterial());
            }
            damage *= (double)ItemBonus.getWeaponDamageIncreaseBonus(_creature, weapon);
            damage *= (double)(1.0f + weapon.getCurrentQualityLevel() / 100.0f * (weapon.getSpellExtraDamageBonus() / 30000.0f));
        }
        if (_creature.getEnemyPresense() > 1200 && opponent.isPlayer() && !weapon.isArtifact()) {
            damage *= (double)1.15f;
        }
        if (!weapon.isArtifact() && this.hasRodEffect && opponent.isPlayer()) {
            damage *= (double)1.2f;
        }
        Vehicle vehicle = Vehicles.getVehicleForId(opponent.getVehicle());
        boolean mildStack = false;
        if (weapon.isWeaponPolearm() && (vehicle != null && vehicle.isCreature() || opponent.isRidden() && weapon.isWeaponPierce())) {
            damage *= (double)1.7f;
        } else if (weapon.isArtifact()) {
            mildStack = true;
        } else if (_creature.getCultist() != null && _creature.getCultist().doubleWarDamage()) {
            damage *= 1.5;
            mildStack = true;
        } else if (_creature.getDeity() != null && _creature.getDeity().isWarrior() && _creature.getFaith() >= 40.0f && _creature.getFavor() >= 20.0f) {
            damage *= (double)1.15f;
            mildStack = true;
        }
        if (_creature.isPlayer()) {
            if ((_creature.getFightStyle() != 2 || attStrengthSkill.getRealKnowledge() < 20.0) && attStrengthSkill.getRealKnowledge() != 20.0) {
                damage *= 1.0 + (attStrengthSkill.getRealKnowledge() - 20.0) / 200.0;
            }
            if (this.currentStrength == 0) {
                Skill fstyle = null;
                try {
                    fstyle = _creature.getSkills().getSkill(10054);
                }
                catch (NoSuchSkillException nss) {
                    fstyle = _creature.getSkills().learn(10054, 1.0f);
                }
                double d = opponent.getBaseCombatRating() * 3.0f;
                boolean bl = this.receivedFStyleSkill || opponent.isNoSkillFor(_creature);
                if (fstyle.skillCheck(d, 0.0, bl, 10.0f, _creature, opponent) > 0.0) {
                    this.receivedFStyleSkill = true;
                    damage *= (double)0.8f;
                } else {
                    damage *= 0.5;
                }
            }
            if (_creature.getStatus().getStamina() > 2000 && this.currentStrength >= 1 && !this.receivedFStyleSkill) {
                int num = 10053;
                if (this.currentStrength == 1) {
                    num = 10055;
                }
                Skill fstyle = null;
                try {
                    fstyle = _creature.getSkills().getSkill(num);
                }
                catch (NoSuchSkillException nss) {
                    fstyle = _creature.getSkills().learn(num, 1.0f);
                }
                double d = opponent.getBaseCombatRating() * 3.0f;
                boolean bl = this.receivedFStyleSkill || opponent.isNoSkillFor(_creature);
                if (fstyle.skillCheck(d, 0.0, bl, 10.0f, _creature, opponent) > 0.0) {
                    this.receivedFStyleSkill = true;
                    if (this.currentStrength > 1) {
                        damage *= 1.0 + Server.getModifiedFloatEffect(fstyle.getRealKnowledge() / 100.0) / (double)(mildStack ? 8.0f : 4.0f);
                    }
                }
            }
            float knowl = 1.0f;
            try {
                Skill wSkill = _creature.getSkills().getSkill(weapon.getPrimarySkill());
                knowl = (float)wSkill.getRealKnowledge();
            }
            catch (NoSuchSkillException noSuchSkillException) {
                // empty catch block
            }
            if (knowl < 50.0f) {
                damage = (double)0.8f * damage + 0.2 * ((double)(knowl / 50.0f) * damage);
            }
        } else {
            damage *= (double)(0.85f + (float)this.currentStrength * 0.15f);
        }
        if (_creature.isStealth() && _creature.opponent != null && !_creature.isVisibleTo(opponent)) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(_creature));
            segments.add(new MulticolorLineSegment(" backstab ", 0));
            segments.add(new CreatureLineSegment(opponent));
            _creature.getCommunicator().sendColoredMessageCombat(segments);
            damage = Math.min(50000.0, damage * 4.0);
        }
        if (_creature.getCitizenVillage() != null && _creature.getCitizenVillage().getFaithWarBonus() > 0.0f) {
            damage *= (double)(1.0f + _creature.getCitizenVillage().getFaithWarBonus() / 100.0f);
        }
        if (_creature.fightlevel >= 4) {
            damage *= (double)1.1f;
        }
        return damage;
    }

    private final double getDamage(Creature _creature, Item weapon, Creature opponent) {
        Skill attStrengthSkill = null;
        try {
            attStrengthSkill = _creature.getSkills().getSkill(102);
        }
        catch (NoSuchSkillException nss) {
            attStrengthSkill = _creature.getSkills().learn(102, 1.0f);
            logger.log(Level.WARNING, _creature.getName() + " had no strength. Weird.");
        }
        if (weapon.isBodyPartAttached()) {
            damage = _creature.getCombatDamage(weapon) * 1000.0f * _creature.getStatus().getDamageTypeModifier();
            if (_creature.isPlayer()) {
                Skill weaponLess = _creature.getWeaponLessFightingSkill();
                double modifier = 1.0 + 2.0 * weaponLess.getKnowledge() / 100.0;
                damage *= modifier;
            }
            if (damage < 10000.0 && _creature.getBonusForSpellEffect((byte)24) > 0.0f) {
                damage = _creature.isPlayer() ? (damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0f) * 15000.0) : (damage += Server.getBuffedQualityEffect(_creature.getBonusForSpellEffect((byte)24) / 100.0f) * 5000.0);
            }
            float randomizer = (50.0f + Server.rand.nextFloat() * 50.0f) / 100.0f;
            damage *= (double)randomizer;
        } else {
            damage = Weapon.getModifiedDamageForWeapon(weapon, attStrengthSkill, opponent.getTemplate().getTemplateId() == 116) * 1000.0;
            if (!Servers.isThisAnEpicOrChallengeServer()) {
                damage += (double)(weapon.getCurrentQualityLevel() / 100.0f * weapon.getSpellExtraDamageBonus());
            }
            damage += Server.getBuffedQualityEffect(weapon.getCurrentQualityLevel() / 100.0f) * (double)Weapon.getBaseDamageForWeapon(weapon) * 2400.0;
            damage *= Weapon.getMaterialDamageBonus(weapon.getMaterial());
            if (!opponent.isPlayer() && opponent.isHunter()) {
                damage *= Weapon.getMaterialHunterDamageBonus(weapon.getMaterial());
            }
            damage *= (double)ItemBonus.getWeaponDamageIncreaseBonus(_creature, weapon);
            if (Servers.isThisAnEpicOrChallengeServer()) {
                damage *= (double)(1.0f + weapon.getCurrentQualityLevel() / 100.0f * weapon.getSpellExtraDamageBonus() / 30000.0f);
            }
        }
        if (_creature.getEnemyPresense() > 1200 && opponent.isPlayer() && !weapon.isArtifact()) {
            damage *= (double)1.15f;
        }
        if (!weapon.isArtifact() && this.hasRodEffect && opponent.isPlayer()) {
            damage *= (double)1.2f;
        }
        Vehicle vehicle = Vehicles.getVehicleForId(opponent.getVehicle());
        boolean mildStack = false;
        if (weapon.isWeaponPolearm() && (vehicle != null && vehicle.isCreature() || opponent.isRidden() && weapon.isWeaponPierce())) {
            damage *= (double)1.7f;
        } else if (weapon.isArtifact()) {
            mildStack = true;
        } else if (_creature.getCultist() != null && _creature.getCultist().doubleWarDamage()) {
            damage *= 1.5;
            mildStack = true;
        } else if (_creature.getDeity() != null && _creature.getDeity().isWarrior() && _creature.getFaith() >= 40.0f && _creature.getFavor() >= 20.0f) {
            damage *= (double)1.15f;
            mildStack = true;
        }
        if (_creature.isPlayer()) {
            if ((_creature.getFightStyle() != 2 || attStrengthSkill.getRealKnowledge() < 20.0) && attStrengthSkill.getRealKnowledge() != 20.0) {
                damage *= 1.0 + (attStrengthSkill.getRealKnowledge() - 20.0) / 200.0;
            }
            if (this.currentStrength == 0) {
                Skill fstyle = null;
                try {
                    fstyle = _creature.getSkills().getSkill(10054);
                }
                catch (NoSuchSkillException nss) {
                    fstyle = _creature.getSkills().learn(10054, 1.0f);
                }
                double d = opponent.getBaseCombatRating() * 3.0f;
                boolean bl = this.receivedFStyleSkill || opponent.isNoSkillFor(_creature);
                if (fstyle.skillCheck(d, 0.0, bl, 10.0f, _creature, opponent) > 0.0) {
                    this.receivedFStyleSkill = true;
                    damage *= (double)0.8f;
                } else {
                    damage *= 0.5;
                }
            }
            if (_creature.getStatus().getStamina() > 2000 && this.currentStrength >= 1 && !this.receivedFStyleSkill) {
                int num = 10053;
                if (this.currentStrength == 1) {
                    num = 10055;
                }
                Skill fstyle = null;
                try {
                    fstyle = _creature.getSkills().getSkill(num);
                }
                catch (NoSuchSkillException nss) {
                    fstyle = _creature.getSkills().learn(num, 1.0f);
                }
                double d = opponent.getBaseCombatRating() * 3.0f;
                boolean bl = this.receivedFStyleSkill || opponent.isNoSkillFor(_creature);
                if (fstyle.skillCheck(d, 0.0, bl, 10.0f, _creature, opponent) > 0.0) {
                    this.receivedFStyleSkill = true;
                    if (this.currentStrength > 1) {
                        damage *= 1.0 + Server.getModifiedFloatEffect(fstyle.getRealKnowledge() / 100.0) / (double)(mildStack ? 8.0f : 4.0f);
                    }
                }
            }
            float knowl = 1.0f;
            try {
                Skill wSkill = _creature.getSkills().getSkill(weapon.getPrimarySkill());
                knowl = (float)wSkill.getRealKnowledge();
            }
            catch (NoSuchSkillException noSuchSkillException) {
                // empty catch block
            }
            if (knowl < 50.0f) {
                damage = (double)0.8f * damage + 0.2 * ((double)(knowl / 50.0f) * damage);
            }
        } else {
            damage *= (double)(0.85f + (float)this.currentStrength * 0.15f);
        }
        if (_creature.isStealth() && _creature.opponent != null && !_creature.isVisibleTo(opponent)) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(_creature));
            segments.add(new MulticolorLineSegment(" backstab ", 0));
            segments.add(new CreatureLineSegment(opponent));
            _creature.getCommunicator().sendColoredMessageCombat(segments);
            damage = Math.min(50000.0, damage * 4.0);
        }
        if (_creature.getCitizenVillage() != null && _creature.getCitizenVillage().getFaithWarBonus() > 0.0f) {
            damage *= (double)(1.0f + _creature.getCitizenVillage().getFaithWarBonus() / 100.0f);
        }
        if (_creature.fightlevel >= 4) {
            damage *= (double)1.1f;
        }
        return damage;
    }

    public byte getType(Item weapon, boolean rawType) {
        int woundType = this.creature.getCombatDamageType();
        if (weapon.isWeaponSword() || weapon.getTemplateId() == 706) {
            woundType = rawType || Server.rand.nextInt(2) == 0 ? 1 : 2;
        } else if (weapon.getTemplateId() == 1115) {
            woundType = rawType || Server.rand.nextInt(3) == 0 ? 2 : 0;
        } else if (weapon.isWeaponSlash()) {
            woundType = 1;
        } else if (weapon.isWeaponPierce()) {
            woundType = 2;
        } else if (weapon.isWeaponCrush()) {
            woundType = 0;
        } else if (weapon.isBodyPart()) {
            if (weapon.getTemplateId() == 17) {
                woundType = 3;
            } else if (weapon.getTemplateId() == 12) {
                woundType = 0;
            }
        }
        type = woundType;
        return (byte)woundType;
    }

    private float getWeaponSpeed(Item _weapon) {
        float flspeed = 20.0f;
        float knowl = 0.0f;
        int spskillnum = 10052;
        if (_weapon.isBodyPartAttached()) {
            flspeed = this.creature.getBodyWeaponSpeed(_weapon);
        } else {
            flspeed = Weapon.getBaseSpeedForWeapon(_weapon);
            try {
                spskillnum = _weapon.getPrimarySkill();
            }
            catch (NoSuchSkillException noSuchSkillException) {
                // empty catch block
            }
        }
        try {
            Skill wSkill = this.creature.getSkills().getSkill(spskillnum);
            knowl = (float)wSkill.getRealKnowledge();
        }
        catch (NoSuchSkillException noSuchSkillException) {
            // empty catch block
        }
        if (!this.creature.isGhost()) {
            flspeed -= flspeed * 0.1f * knowl / 100.0f;
        }
        return flspeed;
    }

    private float getWeaponSpeed(AttackAction act, Item _weapon) {
        float flspeed = 20.0f;
        float knowl = 0.0f;
        int spskillnum = 10052;
        if (!act.isUsingWeapon()) {
            flspeed = act.getAttackValues().getBaseSpeed();
        } else {
            flspeed = act.getAttackValues().getBaseSpeed();
            try {
                spskillnum = _weapon.getPrimarySkill();
            }
            catch (NoSuchSkillException noSuchSkillException) {
                // empty catch block
            }
        }
        try {
            Skill wSkill = this.creature.getSkills().getSkill(spskillnum);
            knowl = (float)wSkill.getRealKnowledge();
        }
        catch (NoSuchSkillException noSuchSkillException) {
            // empty catch block
        }
        if (!this.creature.isGhost()) {
            flspeed -= flspeed * 0.1f * knowl / 100.0f;
        }
        return flspeed;
    }

    public final void setHasSpiritFervor(boolean hasFervor) {
        this.hasSpiritFervor = hasFervor;
    }

    public float getCombatRating(Creature opponent, Item weapon, boolean attacking) {
        float bon;
        float combatRating = this.creature.getBaseCombatRating();
        if (this.hasSpiritFervor) {
            combatRating += 1.0f;
        }
        if (this.creature.isKing() && this.creature.isEligibleForKingdomBonus()) {
            combatRating += 3.0f;
        }
        if (this.creature.hasTrait(0)) {
            combatRating += 1.0f;
        }
        combatRating = attacking ? (combatRating += 1.0f + this.creature.getBonusForSpellEffect((byte)30) / 30.0f) : (combatRating += 1.0f + this.creature.getBonusForSpellEffect((byte)28) / 30.0f);
        if (this.creature.getDeity() != null && this.creature.getFaith() > 70.0f) {
            Tiles.Tile theTile;
            MeshIO mesh = Server.surfaceMesh;
            if (this.creature.getLayer() < 0) {
                mesh = Server.caveMesh;
            }
            int tile = mesh.getTile(this.creature.getCurrentTile().getTileX(), this.creature.getCurrentTile().getTileY());
            byte type = Tiles.decodeType(tile);
            if (this.creature.getDeity().isFo() && ((theTile = Tiles.getTile(type)).isNormalTree() || theTile.isMyceliumTree() || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id || type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_TUNDRA.id)) {
                combatRating += 1.0f;
            }
            if ((this.creature.getDeity().isMagranon() || this.creature.getDeity().isLibila()) && attacking) {
                combatRating += 2.0f;
            }
            if (this.creature.getDeity().isVynora() && !attacking) {
                short height = Tiles.decodeHeight(tile);
                if (height < 0) {
                    combatRating += 2.0f;
                } else if (Terraforming.isRoad(type)) {
                    combatRating += 2.0f;
                }
            }
        }
        if (this.creature.getCultist() != null && this.creature.getCultist().hasFearEffect()) {
            combatRating += 2.0f;
        }
        if (this.creature.isPlayer()) {
            int antiGankBonus = Math.max(0, this.creature.getLastAttackers() - 1);
            combatRating += (float)antiGankBonus;
            this.creature.sendToLoggers("Adding " + antiGankBonus + " to combat rating due to attackers.");
        }
        if (this.creature.isHorse() && this.creature.getLeader() != null && this.creature.getLeader().isPlayer()) {
            combatRating -= 5.0f;
        }
        if (this.creature.hasSpellEffect((byte)97)) {
            combatRating -= 4.0f;
        }
        if (this.creature.isSpiritGuard()) {
            if (Servers.localServer.isChallengeServer()) {
                if (opponent.isPlayer() && opponent.getKingdomId() != this.creature.getKingdomId()) {
                    combatRating = 10.0f;
                }
            } else if (this.creature.getCitizenVillage() != null && this.creature.getCitizenVillage().plan.isUnderSiege()) {
                combatRating += (float)(this.creature.getCitizenVillage().plan.getSiegeCount() / 3);
            }
        }
        if ((bon = weapon.getSpellNimbleness()) > 0.0f) {
            combatRating += bon / 30.0f;
        }
        if (this.creature.isPlayer() && opponent.isPlayer()) {
            if (this.creature.isRoyalExecutioner() && this.creature.isEligibleForKingdomBonus()) {
                combatRating += 2.0f;
            } else if (this.creature.hasCrownInfluence()) {
                combatRating += 1.0f;
            }
            combatRating += Players.getInstance().getCRBonus(this.creature.getKingdomId());
            if (this.creature.isInOwnDuelRing()) {
                if (opponent.getKingdomId() != this.creature.getKingdomId()) {
                    combatRating += 4.0f;
                }
            } else if (opponent.isInOwnDuelRing() && opponent.getKingdomId() != this.creature.getKingdomId()) {
                combatRating -= 4.0f;
            }
            if (Servers.localServer.PVPSERVER && this.creature.getNumberOfFollowers() > 1) {
                combatRating -= 10.0f;
            }
        }
        if (this.creature.isPlayer() && this.creature.hasBattleCampBonus()) {
            combatRating += 3.0f;
        }
        combatRating += ItemBonus.getCRBonus(this.creature);
        float crmod = 1.0f;
        if (attacking) {
            if (this.creature.isPlayer() && this.currentStrength >= 1 && this.creature.getStatus().getStamina() > 2000) {
                int num = 10053;
                if (this.currentStrength == 1) {
                    num = 10055;
                }
                Skill def = null;
                try {
                    def = this.creature.getSkills().getSkill(num);
                }
                catch (NoSuchSkillException nss) {
                    def = this.creature.getSkills().learn(num, 1.0f);
                }
                if (def.skillCheck((double)(this.creature.getBaseCombatRating() * 2.0f), 0.0, true, 10.0f, this.creature, opponent) > 0.0) {
                    combatRating = (float)((double)combatRating + (double)((float)this.currentStrength / 2.0f) * Server.getModifiedFloatEffect(def.getRealKnowledge() / 100.0));
                }
            }
        } else if (this.creature.isPlayer() && this.currentStrength > 1) {
            Skill def = null;
            try {
                def = this.creature.getSkills().getSkill(10053);
            }
            catch (NoSuchSkillException nss) {
                def = this.creature.getSkills().learn(10053, 1.0f);
            }
            if (def.skillCheck(Server.getModifiedFloatEffect(70.0), 0.0, true, 10.0f, this.creature, opponent) < 0.0) {
                combatRating = (float)((double)combatRating - (double)this.currentStrength * Server.getModifiedFloatEffect((100.0 - def.getRealKnowledge()) / 100.0));
            }
        }
        if (this.creature.isPlayer()) {
            combatRating = (float)((double)combatRating - Weapon.getSkillPenaltyForWeapon(weapon));
            combatRating += (float)this.creature.getCRCounterBonus();
        }
        if (this.creature.isPlayer()) {
            combatRating = opponent.isPlayer() ? (float)((double)combatRating + this.creature.getFightingSkill().getKnowledge(0.0) / 5.0) : (float)((double)combatRating + this.creature.getFightingSkill().getRealKnowledge() / 10.0);
        }
        if (this.battleratingPenalty > 0) {
            combatRating -= (float)this.battleratingPenalty;
        }
        crmod *= this.getFlankingModifier(opponent);
        crmod *= this.getHeightModifier(opponent);
        crmod *= this.getAlcMod();
        if (this.creature.getCitizenVillage() != null) {
            crmod *= 1.0f + this.creature.getCitizenVillage().getFaithWarBonus() / 100.0f;
        }
        combatRating *= crmod;
        if (this.creature.fightlevel >= 3) {
            combatRating += (float)(this.creature.fightlevel * 2);
        }
        if (this.creature.isPlayer()) {
            combatRating *= Servers.localServer.getCombatRatingModifier();
        }
        combatRating *= this.getFootingModifier(weapon, opponent);
        if (this.creature.isOnHostileHomeServer()) {
            combatRating *= 0.7f;
        }
        if (this.isOpen()) {
            combatRating *= 0.7f;
        } else if (this.isProne()) {
            combatRating *= 0.5f;
        } else {
            try {
                Creature linkedTo;
                Action act = this.creature.getCurrentAction();
                if (act.isVulnerable()) {
                    combatRating *= 0.5f;
                } else if (this.creature.isLinked() && (linkedTo = this.creature.getCreatureLinkedTo()) != null) {
                    try {
                        linkedTo.getCurrentAction().isSpell();
                        combatRating *= 0.7f;
                    }
                    catch (NoSuchActionException noSuchActionException) {}
                }
            }
            catch (NoSuchActionException noSuchActionException) {
                // empty catch block
            }
        }
        if (this.creature.hasAttackedUnmotivated()) {
            combatRating = Math.min(4.0f, combatRating);
        }
        return this.normcr(combatRating);
    }

    private float getAlcMod() {
        if (this.creature.isPlayer()) {
            float alc = 0.0f;
            alc = ((Player)this.creature).getAlcohol();
            if (alc < 20.0f) {
                return (100.0f + alc) / 100.0f;
            }
            return Math.max(40.0f, 100.0f - alc) / 80.0f;
        }
        return 1.0f;
    }

    private float normcr(float combatRating) {
        return Math.min(100.0f, Math.max(1.0f, combatRating));
    }

    static float getParryBonus(byte defenderStance, byte attackerStance) {
        if (CombatHandler.isStanceParrying(defenderStance, attackerStance)) {
            return 0.8f;
        }
        if (CombatHandler.isStanceOpposing(defenderStance, attackerStance)) {
            return 0.9f;
        }
        return 1.0f;
    }

    public float getChanceToHit(Creature opponent, Item weapon) {
        Seat s;
        this.setBonuses(weapon, opponent);
        float myCR = this.getCombatRating(opponent, weapon, true);
        float oppCR = opponent.getCombatHandler().getCombatRating(this.creature, opponent.getPrimWeapon(), false);
        if (this.creature.isPlayer()) {
            float distdiff = Math.abs(CombatHandler.getDistdiff(weapon, this.creature, opponent));
            if (distdiff > 10.0f) {
                myCR -= 1.0f;
            }
            if (distdiff > 20.0f) {
                myCR -= 1.0f;
            }
        }
        parryBonus = CombatHandler.getParryBonus(opponent.getCombatHandler().currentStance, this.currentStance);
        if (opponent.fightlevel > 0) {
            parryBonus -= (float)(opponent.fightlevel * 1) / 100.0f;
        }
        double m = 1.0;
        if (attBonus != 0.0) {
            m = 1.0 + attBonus / 100.0;
        }
        if ((s = opponent.getSeat()) != null) {
            m *= (double)s.cover;
        }
        float chance = (float)((double)(this.normcr(myCR) / (this.normcr(oppCR) + this.normcr(myCR))) * m * (double)parryBonus);
        float rest = Math.max(0.01f, 1.0f - chance);
        return 100.0f * Math.max(0.01f, (float)Server.getBuffedQualityEffect(1.0f - rest));
    }

    public void setBonuses(Item weapon, Creature defender) {
        attBonus = (double)this.creature.zoneBonus - (double)this.creature.getMovePenalty() * 0.5;
        if (this.currentStrength == 0) {
            attBonus -= 20.0;
        }
        defBonus = defender.zoneBonus - (float)defender.getMovePenalty();
        if (this.addToSkills && defender.isPlayer() && defender.getCombatHandler().currentStrength == 0) {
            Skill def = null;
            try {
                def = defender.getSkills().getSkill(10054);
            }
            catch (NoSuchSkillException nss) {
                def = defender.getSkills().learn(10054, 1.0f);
            }
            if (defender.getStatus().getStamina() > 2000) {
                double d = this.creature.getBaseCombatRating() * 2.0f;
                boolean bl = this.creature.isNoSkillFor(defender) || defender.getCombatHandler().receivedFStyleSkill;
                if (def.skillCheck(d, 0.0, bl, 10.0f, defender, this.creature) > 0.0) {
                    defender.getCombatHandler().receivedFStyleSkill = true;
                    defBonus += def.getKnowledge(0.0) / 4.0;
                }
            }
        }
        if (defender.getCombatHandler().currentStrength > 0 && defender instanceof Player) {
            if (defender.isMoving()) {
                defBonus -= (double)(defender.getCombatHandler().currentStrength * 15);
            } else if (defender.getCombatHandler().currentStrength > 1) {
                defBonus -= (double)(defender.getCombatHandler().currentStrength * 7);
            }
        }
        if (defender.isOnHostileHomeServer()) {
            defBonus -= 20.0;
        } else if (this.creature.isMoving() && this.creature instanceof Player) {
            attBonus -= 15.0;
        }
    }

    private static final float getDistdiff(Creature creature, Creature opponent, AttackAction atk) {
        if (atk != null && !atk.isUsingWeapon()) {
            float idealDist = 10 + atk.getAttackValues().getAttackReach() * 3;
            float dist = Creature.rangeToInDec(creature, opponent);
            return idealDist - dist;
        }
        Item wpn = creature.getPrimWeapon();
        return CombatHandler.getDistdiff(wpn, creature, opponent);
    }

    private static final float getDistdiff(Item weapon, Creature creature, Creature opponent) {
        float idealDist = 10 + Weapon.getReachForWeapon(weapon) * 3;
        float dist = Creature.rangeToInDec(creature, opponent);
        return idealDist - dist;
    }

    private float getFootingModifier(Item weapon, Creature opponent) {
        short[] steepness = Creature.getTileSteepness(this.creature.getCurrentTile().tilex, this.creature.getCurrentTile().tiley, this.creature.isOnSurface());
        float footingMod = 0.0f;
        float heightDiff = 0.0f;
        heightDiff = Math.max(-1.45f, this.creature.getStatus().getPositionZ() + this.creature.getAltOffZ()) - Math.max(-1.45f, opponent.getStatus().getPositionZ() + opponent.getAltOffZ());
        if ((double)heightDiff > 0.5) {
            footingMod = (float)((double)footingMod + 0.1);
        } else if ((double)heightDiff < -0.5) {
            footingMod -= 0.1f;
        }
        if (this.creature.isSubmerged()) {
            return 1.0f;
        }
        if (this.creature.getVehicle() == -10L) {
            if (weapon != null && opponent.getVehicle() != -10L && weapon.isTwoHanded() && !weapon.isWeaponBow()) {
                footingMod += 0.3f;
            }
            if (this.creature.getStatus().getPositionZ() <= -1.45f) {
                return 0.2f + footingMod;
            }
            if (this.creature.isPlayer() && (steepness[1] > 20 || steepness[1] < -20)) {
                Skill bcskill = null;
                try {
                    bcskill = this.creature.getSkills().getSkill(104);
                }
                catch (NoSuchSkillException nss) {
                    bcskill = this.creature.getSkills().learn(104, 1.0f);
                }
                if (bcskill != null && bcskill.skillCheck(Math.abs(Math.max(Math.min(steepness[1], 99), -99)), this.creature.fightlevel * 10, true, 1.0f) > 0.0) {
                    return 1.0f + footingMod;
                }
                if (steepness[1] > 40 || steepness[1] < -40) {
                    if (steepness[1] > 60 || steepness[1] < -60) {
                        if (steepness[1] > 80 || steepness[1] < -80) {
                            if (steepness[1] > 100 || steepness[1] < -100) {
                                return 0.2f + footingMod;
                            }
                            return 0.4f + footingMod;
                        }
                        return 0.6f + footingMod;
                    }
                    return 0.8f + footingMod;
                }
                return 0.9f + footingMod;
            }
        } else if (opponent.isSubmerged()) {
            footingMod = 0.0f;
        }
        return 1.0f + footingMod;
    }

    private float getDirectionTo(Creature opponent) {
        float defAngle = Creature.normalizeAngle(opponent.getStatus().getRotation());
        double newrot = Math.atan2(this.creature.getStatus().getPositionY() - opponent.getStatus().getPositionY(), this.creature.getStatus().getPositionX() - opponent.getStatus().getPositionX());
        float attAngle = (float)(newrot * 57.29577951308232) + 90.0f;
        return Creature.normalizeAngle(attAngle - defAngle);
    }

    private float getFlankingModifier(Creature opponent) {
        Vehicle vehic;
        if (opponent == null) {
            return 1.0f;
        }
        float attAngle = this.getDirectionTo(opponent);
        if (opponent.getVehicle() > -10L && (vehic = Vehicles.getVehicleForId(opponent.getVehicle())) != null && vehic.isCreature()) {
            try {
                Creature ridden = Server.getInstance().getCreature(opponent.getVehicle());
                attAngle = this.getDirectionTo(ridden);
            }
            catch (Exception ex) {
                logger.log(Level.INFO, "No creature for id " + opponent.getVehicle());
            }
        }
        if (attAngle > 140.0f && attAngle < 220.0f) {
            if (attAngle > 160.0f && attAngle < 200.0f) {
                return 1.25f;
            }
            return 1.1f;
        }
        return 1.0f;
    }

    private float getHeightModifier(Creature opponent) {
        if (opponent == null) {
            return 1.0f;
        }
        float diff = this.creature.getPositionZ() + this.creature.getAltOffZ() - (opponent.getPositionZ() + opponent.getAltOffZ());
        if (diff > 1.0f) {
            if (diff > 2.0f) {
                return 1.1f;
            }
            return 1.05f;
        }
        if (diff < -1.0f) {
            if (diff < -2.0f) {
                return 0.9f;
            }
            return 0.95f;
        }
        return 1.0f;
    }

    public static final byte getStanceForAction(ActionEntry entry) {
        if (entry.isAttackHigh()) {
            if (entry.isAttackLeft()) {
                return 6;
            }
            if (entry.isAttackRight()) {
                return 1;
            }
            return 7;
        }
        if (entry.isAttackLow()) {
            if (entry.isAttackLeft()) {
                return 4;
            }
            if (entry.isAttackRight()) {
                return 3;
            }
            return 10;
        }
        if (entry.isAttackLeft()) {
            return 5;
        }
        if (entry.isAttackRight()) {
            return 2;
        }
        if (entry.isDefend()) {
            switch (entry.getNumber()) {
                case 314: {
                    return 12;
                }
                case 315: {
                    return 14;
                }
                case 316: {
                    return 11;
                }
                case 317: {
                    return 13;
                }
            }
            return 0;
        }
        return 0;
    }

    public void setKillEffects(Creature performer, Creature defender) {
        float ms;
        defender.setOpponent(null);
        defender.setTarget(-10L, true);
        if (defender.getWurmId() == performer.target) {
            performer.setTarget(-10L, true);
        }
        defender.getCombatHandler().setCurrentStance(-1, (byte)15);
        performer.getCombatHandler().setCurrentStance(-1, (byte)15);
        if (performer.isUndead()) {
            performer.healRandomWound(100);
            float nut = (float)(50 + Server.rand.nextInt(49)) / 100.0f;
            performer.getStatus().refresh(nut, true);
        }
        if (performer.getCitizenVillage() != null) {
            performer.getCitizenVillage().removeTarget(defender);
        }
        if (defender.isPlayer() && performer.isPlayer()) {
            if (!defender.isOkToKillBy(performer)) {
                if (performer.hasAttackedUnmotivated() && performer.getReputation() < 0) {
                    performer.setReputation(performer.getReputation() - 10);
                } else {
                    performer.setReputation(performer.getReputation() - 20);
                }
            }
            if (!defender.isFriendlyKingdom(performer.getKingdomId()) && !Players.getInstance().isOverKilling(performer.getWurmId(), defender.getWurmId())) {
                if (performer.getKingdomTemplateId() == 3 || performer.getDeity() != null && performer.getDeity().isHateGod()) {
                    performer.maybeModifyAlignment(-5.0f);
                } else {
                    performer.maybeModifyAlignment(5.0f);
                }
                if (performer.getCombatHandler().currentStrength == 0) {
                    performer.achievement(43);
                }
            }
        } else if (!defender.isPlayer() && !performer.isPlayer() && defender.isPrey() && performer.isCarnivore()) {
            performer.getStatus().modifyHunger(-65000, 99.0f);
        }
        if (!defender.isPlayer() && !defender.isReborn() && performer.isPlayer()) {
            if (defender.isKingdomGuard() && defender.getKingdomId() == performer.getKingdomId()) {
                performer.achievement(44);
            }
            try {
                int tid = defender.getTemplate().getTemplateId();
                if (CreatureTemplate.isDragon(tid)) {
                    ((Player)performer).addTitle(Titles.Title.DragonSlayer);
                } else if (tid == 11 || tid == 27) {
                    ((Player)performer).addTitle(Titles.Title.TrollSlayer);
                } else if (tid == 20 || tid == 22) {
                    ((Player)performer).addTitle(Titles.Title.GiantSlayer);
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, defender.getName() + " and " + performer.getName() + ":" + ex.getMessage(), ex);
            }
            if (performer.getDeity() != null && performer.getDeity().number == 2) {
                performer.maybeModifyAlignment(0.5f);
            }
            if (performer.getDeity() != null && performer.getDeity().number == 4) {
                performer.maybeModifyAlignment(-0.5f);
            }
        }
        if (performer.getPrimWeapon() != null && (ms = performer.getPrimWeapon().getSpellMindStealModifier()) > 0.0f && !defender.isPlayer() && defender.getKingdomId() != performer.getKingdomId()) {
            Skills s = defender.getSkills();
            int r = Server.rand.nextInt(s.getSkills().length);
            Skill toSteal = s.getSkills()[r];
            float skillStolen = ms / 100.0f * 0.1f;
            try {
                Skill owned = this.creature.getSkills().getSkill(toSteal.getNumber());
                if (owned.getKnowledge() < toSteal.getKnowledge()) {
                    double smod = (toSteal.getKnowledge() - owned.getKnowledge()) / 100.0;
                    owned.setKnowledge(owned.getKnowledge() + (double)skillStolen * smod, false);
                    this.creature.getCommunicator().sendSafeServerMessage("The " + performer.getPrimWeapon().getName() + " steals some " + toSteal.getName() + ".");
                }
            }
            catch (NoSuchSkillException noSuchSkillException) {
                // empty catch block
            }
        }
    }

    private boolean checkStanceChange(Creature defender, Creature opponent) {
        if (defender.isFighting()) {
            if (defender.isPlayer()) {
                if (defender.isAutofight() && Server.rand.nextInt(10) == 0) {
                    this.selectStance(defender, opponent);
                    return true;
                }
            } else if (Server.rand.nextInt(5) == 0) {
                this.selectStance(defender, opponent);
                return true;
            }
        }
        return false;
    }

    private void selectStance(Creature defender, Creature player) {
        boolean selectNewStance = false;
        try {
            if (!defender.getCurrentAction().isDefend() && !defender.getCurrentAction().isStanceChange()) {
                selectNewStance = true;
            }
        }
        catch (NoSuchActionException nsa) {
            selectNewStance = true;
        }
        if (!defender.isPlayer() && selectNewStance && Server.rand.nextInt((int)(11.0f - Math.min(10.0f, (float)defender.getAggressivity() * defender.getStatus().getAggTypeModifier() / 10.0f))) != 0) {
            selectNewStance = false;
        }
        if (selectNewStance) {
            selectStanceList.clear();
            float mycr = -1.0f;
            float oppcr = -1.0f;
            float knowl = -1.0f;
            if (defender.isFighting()) {
                if (defender.mayRaiseFightLevel() && defender.getMindLogical().getKnowledge(0.0) > 7.0) {
                    if (defender.isPlayer() || Server.rand.nextInt(100) < 30) {
                        selectNewStance = false;
                        selectStanceList.add(Actions.actionEntrys[340]);
                    } else {
                        selectStanceList.add(Actions.actionEntrys[340]);
                    }
                }
                if (defender.isPlayer() && this.getSpeed(defender.getPrimWeapon()) > (float)Server.rand.nextInt(10)) {
                    selectNewStance = false;
                }
                if (selectNewStance) {
                    mycr = defender.getCombatHandler().getCombatRating(player, defender.getPrimWeapon(), false);
                    oppcr = player.getCombatHandler().getCombatRating(defender, player.getPrimWeapon(), false);
                    knowl = this.getCombatKnowledgeSkill();
                    if (knowl > 50.0f) {
                        selectStanceList.addAll(standardDefences);
                    }
                    if (!defender.isPlayer()) {
                        knowl += 20.0f;
                    }
                    selectStanceList.addAll(defender.getCombatHandler().getHighAttacks(null, true, player, mycr, oppcr, knowl));
                    selectStanceList.addAll(defender.getCombatHandler().getMidAttacks(null, true, player, mycr, oppcr, knowl));
                    selectStanceList.addAll(defender.getCombatHandler().getLowAttacks(null, true, player, mycr, oppcr, knowl));
                }
            }
            if (selectStanceList.size() > 0) {
                this.selectStanceFromList(defender, player, mycr, oppcr, knowl);
            }
            if (!defender.isPlayer() && Server.rand.nextInt(10) == 0) {
                int randInt = Server.rand.nextInt(100);
                if ((float)randInt <= Math.max(10.0f, (float)(defender.getAggressivity() - 20) * defender.getStatus().getAggTypeModifier())) {
                    if (defender.getFightStyle() != 1) {
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" suddenly goes into a frenzy.", 0));
                        player.getCommunicator().sendColoredMessageCombat(segments);
                        defender.setFightingStyle((byte)1);
                    }
                } else if ((float)randInt > Math.min(90.0f, ((float)defender.getAggressivity() * defender.getStatus().getAggTypeModifier() + 20.0f) * defender.getStatus().getAggTypeModifier())) {
                    if (defender.getFightStyle() != 2) {
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" cowers.", 0));
                        player.getCommunicator().sendColoredMessageCombat(segments);
                        defender.setFightingStyle((byte)2);
                    }
                } else {
                    if (defender.getFightStyle() == 1) {
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" calms down a bit.", 0));
                        player.getCommunicator().sendColoredMessageCombat(segments);
                    } else if (defender.getFightStyle() == 2) {
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" seems a little more brave now.", 0));
                        player.getCommunicator().sendColoredMessageCombat(segments);
                    }
                    if (defender.getFightStyle() != 0) {
                        defender.setFightingStyle((byte)0);
                    }
                }
            }
        }
    }

    private static final ActionEntry getDefensiveActionEntry(byte opponentStance) {
        ListIterator<ActionEntry> it = selectStanceList.listIterator();
        while (it.hasNext()) {
            ActionEntry e = (ActionEntry)it.next();
            if (!CombatHandler.isStanceParrying(CombatHandler.getStanceForAction(e = Actions.actionEntrys[e.getNumber()]), opponentStance) || CombatHandler.isAtSoftSpot(CombatHandler.getStanceForAction(e), opponentStance)) continue;
            return e;
        }
        return null;
    }

    private static final ActionEntry getOpposingActionEntry(byte opponentStance) {
        ListIterator<ActionEntry> it = selectStanceList.listIterator();
        while (it.hasNext()) {
            ActionEntry e = (ActionEntry)it.next();
            if (!CombatHandler.isStanceOpposing(CombatHandler.getStanceForAction(e = Actions.actionEntrys[e.getNumber()]), opponentStance) || CombatHandler.isAtSoftSpot(CombatHandler.getStanceForAction(e), opponentStance)) continue;
            return e;
        }
        return null;
    }

    private static final ActionEntry getNonDefensiveActionEntry(byte opponentStance) {
        for (int x = 0; x < selectStanceList.size(); ++x) {
            int num = Server.rand.nextInt(selectStanceList.size());
            ActionEntry e = selectStanceList.get(num);
            if (CombatHandler.isStanceParrying(CombatHandler.getStanceForAction(e = Actions.actionEntrys[e.getNumber()]), opponentStance) || CombatHandler.isStanceOpposing(CombatHandler.getStanceForAction(e), opponentStance) || CombatHandler.isAtSoftSpot(CombatHandler.getStanceForAction(e), opponentStance)) continue;
            return e;
        }
        return null;
    }

    private static final boolean isNextGoodStance(byte currentStance, byte nextStance, byte opponentStance) {
        if (CombatHandler.isAtSoftSpot(nextStance, opponentStance)) {
            return false;
        }
        if (CombatHandler.isAtSoftSpot(opponentStance, currentStance)) {
            return false;
        }
        if (CombatHandler.isAtSoftSpot(opponentStance, nextStance)) {
            return true;
        }
        if (currentStance == 0) {
            return nextStance == 5 || nextStance == 2;
        }
        if (currentStance == 5) {
            return nextStance == 6 || nextStance == 4;
        }
        if (currentStance == 2) {
            return nextStance == 1 || nextStance == 3;
        }
        if (currentStance == 1 || currentStance == 6) {
            return nextStance == 7;
        }
        if (currentStance == 3 || currentStance == 4) {
            return nextStance == 10;
        }
        return false;
    }

    static final byte[] getSoftSpots(byte currentStance) {
        if (currentStance == 0) {
            return standardSoftSpots;
        }
        if (currentStance == 5) {
            return midLeftSoftSpots;
        }
        if (currentStance == 2) {
            return midRightSoftSpots;
        }
        if (currentStance == 1) {
            return upperRightSoftSpots;
        }
        if (currentStance == 6) {
            return upperLeftSoftSpots;
        }
        if (currentStance == 3) {
            return lowerRightSoftSpots;
        }
        if (currentStance == 4) {
            return lowerLeftSoftSpots;
        }
        return emptyByteArray;
    }

    private static final boolean isAtSoftSpot(byte stanceChecked, byte stanceUnderAttack) {
        byte[] opponentSoftSpots;
        for (byte spot : opponentSoftSpots = CombatHandler.getSoftSpots(stanceChecked)) {
            if (spot != stanceUnderAttack) continue;
            return true;
        }
        return false;
    }

    private static final boolean existsBetterOffensiveStance(byte _currentStance, byte opponentStance) {
        if (CombatHandler.isAtSoftSpot(opponentStance, _currentStance)) {
            return false;
        }
        boolean isOpponentAtSoftSpot = CombatHandler.isAtSoftSpot(_currentStance, opponentStance);
        if (isOpponentAtSoftSpot || !CombatHandler.isStanceParrying(_currentStance, opponentStance) && !CombatHandler.isStanceOpposing(_currentStance, opponentStance)) {
            for (int x = 0; x < selectStanceList.size(); ++x) {
                int num = Server.rand.nextInt(selectStanceList.size());
                ActionEntry e = selectStanceList.get(num);
                byte nextStance = CombatHandler.getStanceForAction(e = Actions.actionEntrys[e.getNumber()]);
                if (!CombatHandler.isNextGoodStance(_currentStance, nextStance, opponentStance)) continue;
                return true;
            }
            return false;
        }
        for (int x = 0; x < selectStanceList.size(); ++x) {
            int num = Server.rand.nextInt(selectStanceList.size());
            ActionEntry e = selectStanceList.get(num);
            byte nextStance = CombatHandler.getStanceForAction(e = Actions.actionEntrys[e.getNumber()]);
            if (CombatHandler.isStanceParrying(_currentStance, nextStance) || CombatHandler.isStanceOpposing(_currentStance, nextStance)) continue;
            return true;
        }
        return false;
    }

    private static final ActionEntry changeToBestOffensiveStance(byte _currentStance, byte opponentStance) {
        for (int x = 0; x < selectStanceList.size(); ++x) {
            int num = Server.rand.nextInt(selectStanceList.size());
            ActionEntry e = selectStanceList.get(num);
            byte nextStance = CombatHandler.getStanceForAction(e = Actions.actionEntrys[e.getNumber()]);
            if (!CombatHandler.isNextGoodStance(_currentStance, nextStance, opponentStance)) continue;
            return e;
        }
        return null;
    }

    private final void selectStanceFromList(Creature defender, Creature opponent, float mycr, float oppcr, float knowl) {
        ActionEntry e = null;
        if (defender.isPlayer() || defender.getMindLogical().getKnowledge(0.0) > 17.0) {
            if (oppcr - mycr > 3.0f) {
                if (Server.rand.nextInt(2) == 0) {
                    if (defender.mayRaiseFightLevel()) {
                        e = Actions.actionEntrys[340];
                    }
                } else if (defender.opponent == opponent && (e = CombatHandler.getDefensiveActionEntry(opponent.getCombatHandler().currentStance)) == null) {
                    e = CombatHandler.getOpposingActionEntry(opponent.getCombatHandler().currentStance);
                }
            }
            if (e == null) {
                if (defender.combatRound > 2 && Server.rand.nextInt(2) == 0) {
                    if (defender.mayRaiseFightLevel()) {
                        e = Actions.actionEntrys[340];
                    }
                } else if (mycr - oppcr > 2.0f || defender.getCombatHandler().getSpeed(defender.getPrimWeapon()) < 3.0f) {
                    if (CombatHandler.existsBetterOffensiveStance(defender.getCombatHandler().currentStance, opponent.getCombatHandler().currentStance) && (e = CombatHandler.changeToBestOffensiveStance(defender.getCombatHandler().currentStance, opponent.getCombatHandler().currentStance)) == null) {
                        e = CombatHandler.getNonDefensiveActionEntry(opponent.getCombatHandler().currentStance);
                    }
                } else if (mycr >= oppcr) {
                    if (defender.getStatus().damage < opponent.getStatus().damage) {
                        if (CombatHandler.existsBetterOffensiveStance(defender.getCombatHandler().currentStance, opponent.getCombatHandler().currentStance) && (e = CombatHandler.changeToBestOffensiveStance(defender.getCombatHandler().currentStance, opponent.getCombatHandler().currentStance)) == null) {
                            e = CombatHandler.getNonDefensiveActionEntry(opponent.getCombatHandler().currentStance);
                        }
                    } else {
                        e = CombatHandler.getDefensiveActionEntry(opponent.getCombatHandler().currentStance);
                        if (e == null) {
                            e = CombatHandler.getOpposingActionEntry(opponent.getCombatHandler().currentStance);
                        }
                    }
                }
            }
        } else if (e == null) {
            if (!Server.rand.nextBoolean() || defender.getShield() == null) {
                int num = Server.rand.nextInt(selectStanceList.size());
                e = selectStanceList.get(num);
                e = Actions.actionEntrys[e.getNumber()];
            } else {
                e = Actions.actionEntrys[105];
            }
        }
        if (e != null && e.getNumber() > 0) {
            try {
                if (Creature.rangeTo(defender, opponent) <= e.getRange()) {
                    if (e.getNumber() == 105) {
                        defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender.getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender.getStatus().getRotation()));
                    } else if (e.isStanceChange() && e.getNumber() != 340) {
                        if (CombatHandler.getStanceForAction(e) != this.currentStance) {
                            defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender.getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender.getStatus().getRotation()));
                        }
                    } else if (defender.mayRaiseFightLevel() && e.getNumber() == 340) {
                        defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender.getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender.getStatus().getRotation()));
                    } else {
                        defender.setAction(new Action(defender, -1L, opponent.getWurmId(), e.getNumber(), defender.getPosX(), defender.getPosY(), defender.getPositionZ() + defender.getAltOffZ(), defender.getStatus().getRotation()));
                    }
                } else {
                    logger.log(Level.INFO, defender.getName() + " too far away for stance " + e.getActionString() + " attacking " + opponent.getName() + " with range " + Creature.rangeTo(defender, opponent));
                }
            }
            catch (Exception fe) {
                logger.log(Level.WARNING, defender.getName() + " failed:" + fe.getMessage(), fe);
            }
        }
    }

    public static final byte[] getOptions(List<ActionEntry> list, byte currentStance) {
        if (list == null || list.isEmpty()) {
            return NO_COMBAT_OPTIONS;
        }
        byte[] toReturn = new byte[31];
        ListIterator<ActionEntry> it = list.listIterator();
        while (it.hasNext()) {
            ActionEntry act = it.next();
            int x = act.getNumber() - 287;
            if (act.isDefend()) {
                toReturn[x] = 50;
                continue;
            }
            if (x < 0 || x > 30 || CombatHandler.getStanceForAction(Actions.actionEntrys[act.getNumber()]) == currentStance) continue;
            toReturn[x] = Byte.parseByte(act.getActionString().substring(0, act.getActionString().indexOf("%")));
        }
        return toReturn;
    }

    public void addParryModifier(DoubleValueModifier modifier) {
        if (this.parryModifiers == null) {
            this.parryModifiers = new HashSet<DoubleValueModifier>();
        }
        this.parryModifiers.add(modifier);
    }

    public void removeParryModifier(DoubleValueModifier modifier) {
        if (this.parryModifiers != null) {
            this.parryModifiers.remove(modifier);
        }
    }

    private double getParryMod() {
        if (this.parryModifiers == null) {
            return 1.0;
        }
        double doubleModifier = 1.0;
        for (DoubleValueModifier lDoubleValueModifier : this.parryModifiers) {
            doubleModifier += lDoubleValueModifier.getModifier();
        }
        return doubleModifier;
    }

    public void addDodgeModifier(DoubleValueModifier modifier) {
        if (this.dodgeModifiers == null) {
            this.dodgeModifiers = new HashSet<DoubleValueModifier>();
        }
        this.dodgeModifiers.add(modifier);
    }

    public void removeDodgeModifier(DoubleValueModifier modifier) {
        if (this.dodgeModifiers != null) {
            this.dodgeModifiers.remove(modifier);
        }
    }

    private double getDodgeMod() {
        float diff = this.creature.getTemplate().getWeight() / this.creature.getWeight();
        if (this.creature.isPlayer()) {
            diff = this.creature.getTemplate().getWeight() / (this.creature.getWeight() + (float)this.creature.getBody().getBodyItem().getFullWeight() + (float)this.creature.getInventory().getFullWeight());
        }
        diff = 0.8f + diff * 0.2f;
        if (this.dodgeModifiers == null) {
            return 1.0f * diff;
        }
        double doubleModifier = 1.0;
        for (DoubleValueModifier lDoubleValueModifier : this.dodgeModifiers) {
            doubleModifier += lDoubleValueModifier.getModifier();
        }
        return doubleModifier * (double)diff;
    }

    void setFightingStyle(byte style) {
        this.currentStrength = style == 2 ? (byte)0 : (style == 1 ? (byte)3 : (byte)1);
    }

    public List<ActionEntry> getMoveStack() {
        return this.moveStack;
    }

    void clearMoveStack() {
        if (this.moveStack != null) {
            this.moveStack.clear();
            this.moveStack = null;
        }
    }

    byte getOpportunityAttacks() {
        return this.opportunityAttacks;
    }

    public boolean isSentAttacks() {
        return this.sentAttacks;
    }

    public void setSentAttacks(boolean aSentAttacks) {
        this.sentAttacks = aSentAttacks;
    }

    public void setRodEffect(boolean effect) {
        this.hasRodEffect = effect;
        this.sendRodEffect();
    }

    public void sendRodEffect() {
        if (this.hasRodEffect) {
            this.creature.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ROD_BEGUILING_EFFECT, 100000, 100.0f);
        } else {
            this.creature.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.ROD_BEGUILING_EFFECT);
        }
    }

    public static String getOthersString() {
        return othersString;
    }

    public static void setOthersString(String othersString) {
        CombatHandler.othersString = othersString;
    }

    static {
        standardDefences.add(Actions.actionEntrys[314]);
        standardDefences.add(Actions.actionEntrys[315]);
        standardDefences.add(Actions.actionEntrys[316]);
        standardDefences.add(Actions.actionEntrys[317]);
    }
}


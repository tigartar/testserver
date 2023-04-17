/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.combat;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class SpecialMove
implements CombatConstants,
MiscConstants {
    private static final Logger logger = Logger.getLogger(SpecialMove.class.getName());
    private final int speed;
    private final String name;
    private String actorMessage = "decapitate";
    private String othersMessage = "decapitates";
    private byte[] triggeredStances = new byte[]{7};
    private final int fightingSkillLevelNeeded;
    private final double difficulty;
    private byte weaponType = 1;
    private int staminaCost = 5000;
    private byte kingdom = 1;
    private int battleRatingPenalty = 0;
    private int stunseconds = 0;
    private int staminaStolen = 0;
    private boolean pukeMove = false;
    private byte[] woundLocationDmg = new byte[]{1};
    private int[] woundLocation = new int[]{2};
    private static final Map<Integer, SpecialMove> specialMoves = new HashMap<Integer, SpecialMove>();
    private static final Map<Byte, Map<Integer, Set<SpecialMove>>> movesByWeapon = new HashMap<Byte, Map<Integer, Set<SpecialMove>>>();
    private static final SpecialMove[] emptyMoves = new SpecialMove[0];

    public SpecialMove(int id, String _name, int _speed, int fightskillNeeded, double _difficulty) {
        this.name = _name;
        this.speed = _speed;
        this.fightingSkillLevelNeeded = fightskillNeeded;
        this.difficulty = _difficulty;
        specialMoves.put(id, this);
    }

    public boolean mayPerform(Creature creature, Item weapon, int skill) {
        return (this.weaponType == -1 || creature.getCombatHandler().getType(weapon, true) == this.weaponType) && skill >= this.fightingSkillLevelNeeded;
    }

    public void doEffect(Creature creature, Item weapon, Creature defender, double eff) {
        this.doWoundEffect(creature, weapon, defender, eff);
        this.doBREffect(creature, weapon, defender, eff);
        this.doStaminaEffect(creature, weapon, defender, eff);
        this.doPukeEffect(creature, weapon, defender, eff);
        this.doStunEffect(creature, weapon, defender, eff);
        creature.getStatus().modifyStamina(-this.staminaCost);
    }

    private void doBREffect(Creature creature, Item weapon, Creature defender, double eff) {
        if (this.battleRatingPenalty != 0) {
            defender.getCombatHandler().addBattleRatingPenalty((byte)this.battleRatingPenalty);
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" loses concentration.", 0));
            creature.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(creature));
            segments.get(1).setText(" hits you with a " + this.name + " that lowers your concentration.");
            defender.getCommunicator().sendColoredMessageCombat(segments);
        }
    }

    private void doWoundEffect(Creature creature, Item weapon, Creature defender, double eff) {
        if (this.woundLocationDmg != null) {
            if (this.woundLocation != null) {
                if (this.woundLocationDmg.length == this.woundLocation.length) {
                    for (int w = 0; w < this.woundLocationDmg.length; ++w) {
                        if (this.woundLocationDmg[w] <= 0 || defender.isDead()) continue;
                        byte t = creature.getCombatHandler().getType(weapon, true);
                        CombatHandler.setAttString(this.getActorMessage());
                        CombatHandler.setOthersString(this.getOthersMessage());
                        creature.getCombatHandler().setDamage(defender, weapon, eff / 100.0 * (double)this.woundLocationDmg[w] * 2500.0, (byte)this.woundLocation[w], t);
                        CombatHandler.setOthersString("");
                    }
                } else {
                    logger.log(Level.WARNING, "Combat move wrong damages:" + this.name);
                }
            } else {
                logger.log(Level.WARNING, "Combat move lacking damage:" + this.name);
            }
        }
    }

    private void doStaminaEffect(Creature creature, Item weapon, Creature defender, double eff) {
        if (this.staminaStolen > 0) {
            defender.getStatus().modifyStamina(-((int)((double)this.staminaStolen * Math.max(50.0, eff) / 100.0)));
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" is drained of stamina and gasps for air.", 0));
            creature.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(creature));
            segments.get(1).setText(" hits you with a " + this.name + " in a sensitive spot. You lose stamina!");
            defender.getCommunicator().sendColoredMessageCombat(segments);
        }
    }

    private void doPukeEffect(Creature creature, Item weapon, Creature defender, double eff) {
        if (this.pukeMove) {
            defender.getStatus().modifyHunger((int)(65535.0 * eff / 100.0), 0.01f);
            defender.getStatus().modifyThirst((int)(65535.0 * eff / 100.0));
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" throws up from the impact.", 0));
            creature.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(creature));
            segments.get(1).setText(" hits you with a " + this.name + " and the impact makes you throw up.");
            defender.getCommunicator().sendColoredMessageCombat(segments);
        }
    }

    private void doStunEffect(Creature creature, Item weapon, Creature defender, double eff) {
        if (this.stunseconds > 0) {
            defender.getStatus().setStunned((byte)((double)this.stunseconds * eff / 100.0));
            ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" is stunned.", 0));
            creature.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(creature));
            segments.get(1).setText(" hits you with a " + this.name + " and stuns you.");
            defender.getCommunicator().sendColoredMessageCombat(segments);
        }
    }

    public static final void createMoves() {
        SpecialMove sludge = new SpecialMove(1, "Sludge", 5, 19, 25.0);
        sludge.triggeredStances = new byte[]{10};
        sludge.woundLocation = new int[]{25};
        sludge.woundLocationDmg = new byte[]{2};
        sludge.staminaCost = 11000;
        sludge.stunseconds = 2;
        sludge.staminaStolen = 2000;
        sludge.weaponType = (byte)-1;
        sludge.kingdom = 1;
        sludge.setActorMessage("duck low and hit");
        sludge.setOthersMessage("ducks low and hits");
        SpecialMove.addMovesByWeapon(sludge.weaponType, sludge);
        SpecialMove falcon = new SpecialMove(2, "Falcon", 7, 25, 30.0);
        falcon.triggeredStances = new byte[]{6, 1, 7};
        falcon.woundLocationDmg = new byte[]{3};
        falcon.staminaCost = 13000;
        falcon.staminaStolen = 8000;
        falcon.weaponType = (byte)-1;
        falcon.kingdom = 1;
        falcon.setActorMessage("knock");
        falcon.setOthersMessage("knocks");
        SpecialMove.addMovesByWeapon(falcon.weaponType, falcon);
        SpecialMove narr = new SpecialMove(3, "Narrower", 8, 30, 35.0);
        narr.triggeredStances = new byte[]{5};
        narr.woundLocation = new int[]{23};
        narr.woundLocationDmg = new byte[]{6};
        narr.staminaCost = 12000;
        narr.stunseconds = 3;
        narr.weaponType = (byte)2;
        narr.kingdom = 1;
        narr.setActorMessage("strongly pierce");
        narr.setOthersMessage("strongly pierces");
        SpecialMove.addMovesByWeapon(narr.weaponType, narr);
        SpecialMove cray = new SpecialMove(4, "Crayfish", 7, 40, 45.0);
        cray.triggeredStances = new byte[]{10, 4, 3};
        cray.woundLocation = new int[]{34};
        cray.woundLocationDmg = new byte[]{10};
        cray.staminaCost = 14000;
        cray.stunseconds = 2;
        cray.weaponType = (byte)2;
        cray.kingdom = 1;
        cray.setActorMessage("stab");
        cray.setOthersMessage("stabs");
        SpecialMove.addMovesByWeapon(cray.weaponType, cray);
        SpecialMove pearl = new SpecialMove(5, "Mommys pearl", 4, 50, 55.0);
        pearl.triggeredStances = new byte[]{7};
        pearl.woundLocation = new int[]{1};
        pearl.woundLocationDmg = new byte[]{11};
        pearl.staminaCost = 17000;
        pearl.weaponType = (byte)2;
        pearl.stunseconds = 3;
        pearl.kingdom = 1;
        pearl.setActorMessage("stab");
        pearl.setOthersMessage("stabs");
        SpecialMove.addMovesByWeapon(pearl.weaponType, pearl);
        SpecialMove motley = new SpecialMove(6, "Motley visions", 8, 60, 50.0);
        motley.triggeredStances = new byte[]{10};
        motley.woundLocation = new int[]{25};
        motley.woundLocationDmg = new byte[]{10};
        motley.staminaCost = 16000;
        motley.weaponType = (byte)2;
        motley.stunseconds = 3;
        motley.kingdom = 1;
        motley.setActorMessage("punch holes in");
        motley.setOthersMessage("punches holes in");
        SpecialMove.addMovesByWeapon(motley.weaponType, motley);
        SpecialMove nick = new SpecialMove(7, "Carver", 5, 30, 35.0);
        nick.triggeredStances = new byte[]{5};
        nick.woundLocation = new int[]{23, 24};
        nick.woundLocationDmg = new byte[]{3, 3};
        nick.battleRatingPenalty = 1;
        nick.staminaCost = 15000;
        nick.weaponType = 1;
        nick.kingdom = 1;
        nick.setActorMessage("engrave");
        nick.setOthersMessage("engraves");
        SpecialMove.addMovesByWeapon(nick.weaponType, nick);
        SpecialMove props = new SpecialMove(8, "False props", 7, 40, 35.0);
        props.triggeredStances = new byte[]{10, 4, 3};
        props.woundLocation = new int[]{34, 15, 16};
        props.woundLocationDmg = new byte[]{4, 2, 2};
        props.staminaCost = 15000;
        props.battleRatingPenalty = 1;
        props.weaponType = 1;
        props.kingdom = 1;
        props.setActorMessage("fool and cut");
        props.setOthersMessage("fools and cuts");
        SpecialMove.addMovesByWeapon(props.weaponType, props);
        SpecialMove flurry = new SpecialMove(9, "Flurry of pain", 4, 50, 55.0);
        flurry.triggeredStances = new byte[]{2, 5};
        flurry.woundLocation = new int[]{3, 21, 4};
        flurry.woundLocationDmg = new byte[]{3, 4, 3};
        flurry.staminaCost = 17000;
        flurry.battleRatingPenalty = 2;
        flurry.weaponType = 1;
        flurry.kingdom = 1;
        flurry.setActorMessage("assault");
        flurry.setOthersMessage("assaults");
        SpecialMove.addMovesByWeapon(flurry.weaponType, flurry);
        SpecialMove twilfit = new SpecialMove(10, "Twilfit twin", 8, 60, 50.0);
        twilfit.triggeredStances = new byte[]{6, 1, 7};
        twilfit.woundLocation = new int[]{1, 22};
        twilfit.woundLocationDmg = new byte[]{4, 5};
        twilfit.staminaCost = 15000;
        twilfit.weaponType = 1;
        twilfit.battleRatingPenalty = 3;
        twilfit.kingdom = 1;
        twilfit.setActorMessage("doubly slash");
        twilfit.setOthersMessage("doubly slashes");
        SpecialMove.addMovesByWeapon(twilfit.weaponType, twilfit);
        SpecialMove toe = new SpecialMove(11, "Union of the snake", 5, 30, 35.0);
        toe.triggeredStances = new byte[]{4, 3, 10};
        toe.woundLocation = new int[]{16};
        toe.woundLocationDmg = new byte[]{5};
        toe.staminaCost = 11000;
        toe.staminaStolen = 5000;
        toe.weaponType = 0;
        toe.kingdom = 1;
        toe.setActorMessage("pound");
        toe.setOthersMessage("pounds");
        SpecialMove.addMovesByWeapon(toe.weaponType, toe);
        SpecialMove marmalade = new SpecialMove(12, "Sour marmalade", 6, 40, 30.0);
        marmalade.triggeredStances = new byte[]{2};
        marmalade.woundLocationDmg = new byte[]{2};
        marmalade.staminaCost = 14000;
        marmalade.staminaStolen = 8000;
        marmalade.weaponType = 0;
        marmalade.kingdom = 1;
        marmalade.pukeMove = true;
        marmalade.setActorMessage("squish");
        marmalade.setOthersMessage("squishes");
        SpecialMove.addMovesByWeapon(marmalade.weaponType, marmalade);
        SpecialMove minikill = new SpecialMove(13, "Minikill", 7, 50, 55.0);
        minikill.triggeredStances = new byte[]{6, 1};
        minikill.woundLocation = new int[]{23};
        minikill.woundLocationDmg = new byte[]{6};
        minikill.staminaCost = 16000;
        minikill.weaponType = 0;
        minikill.staminaStolen = 15000;
        minikill.kingdom = 1;
        minikill.setActorMessage("bonk");
        minikill.setOthersMessage("bonks");
        SpecialMove.addMovesByWeapon(minikill.weaponType, minikill);
        SpecialMove golem = new SpecialMove(14, "Golem roar", 9, 60, 65.0);
        golem.triggeredStances = new byte[]{0};
        golem.woundLocationDmg = new byte[]{4};
        golem.staminaCost = 17000;
        golem.weaponType = 0;
        golem.pukeMove = true;
        golem.staminaStolen = 25000;
        golem.kingdom = 1;
        golem.setActorMessage("devastate");
        golem.setOthersMessage("devastates");
        SpecialMove.addMovesByWeapon(golem.weaponType, golem);
        SpecialMove tail = new SpecialMove(15, "Dragontail", 7, 19, 25.0);
        tail.triggeredStances = new byte[]{10};
        tail.woundLocationDmg = new byte[]{2};
        tail.staminaCost = 12000;
        tail.stunseconds = 2;
        tail.staminaStolen = 3000;
        tail.weaponType = (byte)-1;
        tail.kingdom = (byte)3;
        tail.setActorMessage("whip");
        tail.setOthersMessage("whips");
        SpecialMove.addMovesByWeapon(tail.weaponType, tail);
        SpecialMove wanderer = new SpecialMove(16, "Wanderer", 8, 25, 30.0);
        wanderer.triggeredStances = new byte[]{3, 4};
        wanderer.woundLocationDmg = new byte[]{2};
        wanderer.staminaCost = 14000;
        wanderer.staminaStolen = 10000;
        wanderer.weaponType = (byte)-1;
        wanderer.kingdom = (byte)3;
        wanderer.setActorMessage("stomp");
        wanderer.setOthersMessage("stomps");
        SpecialMove.addMovesByWeapon(wanderer.weaponType, wanderer);
        SpecialMove horses = new SpecialMove(17, "Horses ass", 7, 30, 35.0);
        horses.triggeredStances = new byte[]{5, 2};
        horses.woundLocation = new int[]{24};
        horses.woundLocationDmg = new byte[]{6};
        horses.stunseconds = 3;
        horses.staminaCost = 11000;
        horses.weaponType = (byte)2;
        horses.kingdom = (byte)3;
        horses.setActorMessage("restructure");
        horses.setOthersMessage("restructures");
        SpecialMove.addMovesByWeapon(horses.weaponType, horses);
        SpecialMove scorpio = new SpecialMove(18, "Slow scorpio", 9, 40, 45.0);
        scorpio.triggeredStances = new byte[]{10, 4, 3};
        scorpio.woundLocation = new int[]{34};
        scorpio.woundLocationDmg = new byte[]{10};
        scorpio.stunseconds = 3;
        scorpio.staminaCost = 12000;
        scorpio.weaponType = (byte)2;
        scorpio.kingdom = (byte)3;
        scorpio.setActorMessage("sting");
        scorpio.setOthersMessage("stings");
        SpecialMove.addMovesByWeapon(scorpio.weaponType, scorpio);
        SpecialMove moist = new SpecialMove(19, "Moist hind", 6, 50, 55.0);
        moist.triggeredStances = new byte[]{7};
        moist.woundLocation = new int[]{1};
        moist.woundLocationDmg = new byte[]{9};
        moist.staminaCost = 15000;
        moist.weaponType = (byte)2;
        moist.stunseconds = 4;
        moist.kingdom = (byte)3;
        moist.setActorMessage("stab");
        moist.setOthersMessage("stabs");
        SpecialMove.addMovesByWeapon(moist.weaponType, moist);
        SpecialMove mask = new SpecialMove(20, "Mask of defiance", 8, 60, 65.0);
        mask.triggeredStances = new byte[]{7};
        mask.woundLocation = new int[]{1};
        mask.woundLocationDmg = new byte[]{14};
        mask.staminaCost = 18000;
        mask.weaponType = (byte)2;
        mask.stunseconds = 3;
        mask.kingdom = (byte)3;
        mask.setActorMessage("punch holes in");
        mask.setOthersMessage("punches holes in");
        SpecialMove.addMovesByWeapon(mask.weaponType, mask);
        SpecialMove red = new SpecialMove(21, "Red wind", 7, 30, 35.0);
        red.triggeredStances = new byte[]{2};
        red.woundLocation = new int[]{23};
        red.woundLocationDmg = new byte[]{6};
        red.staminaCost = 15000;
        red.battleRatingPenalty = 2;
        red.weaponType = 1;
        red.kingdom = (byte)3;
        red.setActorMessage("engrave");
        red.setOthersMessage("engraves");
        SpecialMove.addMovesByWeapon(red.weaponType, red);
        SpecialMove slow = new SpecialMove(22, "Dark grace", 9, 40, 45.0);
        slow.triggeredStances = new byte[]{10, 4, 3};
        slow.woundLocation = new int[]{34, 15, 16};
        slow.woundLocationDmg = new byte[]{5, 3, 3};
        slow.staminaCost = 14000;
        slow.battleRatingPenalty = 1;
        slow.weaponType = 1;
        slow.kingdom = (byte)3;
        slow.setActorMessage("grace");
        slow.setOthersMessage("graces");
        SpecialMove.addMovesByWeapon(slow.weaponType, slow);
        SpecialMove hurting = new SpecialMove(23, "Hurting scion", 4, 50, 55.0);
        hurting.triggeredStances = new byte[]{5, 2};
        hurting.woundLocation = new int[]{21, 4};
        hurting.woundLocationDmg = new byte[]{6, 4};
        hurting.staminaCost = 17000;
        hurting.weaponType = 1;
        hurting.battleRatingPenalty = 2;
        hurting.kingdom = (byte)3;
        hurting.setActorMessage("cut open");
        hurting.setOthersMessage("cuts open");
        SpecialMove.addMovesByWeapon(hurting.weaponType, hurting);
        SpecialMove trueb = new SpecialMove(24, "True blood", 8, 55, 60.0);
        trueb.triggeredStances = new byte[]{1, 6};
        trueb.woundLocation = new int[]{1, 21};
        trueb.woundLocationDmg = new byte[]{4, 8};
        trueb.staminaCost = 15000;
        trueb.weaponType = 1;
        trueb.battleRatingPenalty = 2;
        trueb.kingdom = (byte)3;
        trueb.setActorMessage("whip");
        trueb.setOthersMessage("whips");
        SpecialMove.addMovesByWeapon(trueb.weaponType, trueb);
        SpecialMove kissg = new SpecialMove(25, "Kiss goodnight", 4, 25, 30.0);
        kissg.triggeredStances = new byte[]{6, 1, 7};
        kissg.woundLocationDmg = new byte[]{3};
        kissg.pukeMove = true;
        kissg.staminaCost = 15000;
        kissg.staminaStolen = 5000;
        kissg.weaponType = 0;
        kissg.kingdom = (byte)3;
        kissg.setActorMessage("pound");
        kissg.setOthersMessage("pounds");
        SpecialMove.addMovesByWeapon(kissg.weaponType, kissg);
        SpecialMove squarep = new SpecialMove(26, "Squarepusher", 7, 40, 45.0);
        squarep.triggeredStances = new byte[]{2};
        squarep.woundLocation = new int[]{14};
        squarep.woundLocationDmg = new byte[]{6};
        squarep.staminaStolen = 10000;
        squarep.staminaCost = 12000;
        squarep.weaponType = 0;
        squarep.kingdom = (byte)3;
        squarep.setActorMessage("maul");
        squarep.setOthersMessage("mauls");
        SpecialMove.addMovesByWeapon(squarep.weaponType, squarep);
        SpecialMove doubleimp = new SpecialMove(27, "Double impact", 4, 50, 55.0);
        doubleimp.triggeredStances = new byte[]{6, 1};
        doubleimp.woundLocation = new int[]{1};
        doubleimp.woundLocationDmg = new byte[]{5};
        doubleimp.staminaCost = 16000;
        doubleimp.staminaStolen = 14000;
        doubleimp.weaponType = 0;
        doubleimp.pukeMove = true;
        doubleimp.kingdom = (byte)3;
        doubleimp.setActorMessage("gong-gong");
        doubleimp.setOthersMessage("gong-gongs");
        SpecialMove.addMovesByWeapon(doubleimp.weaponType, doubleimp);
        SpecialMove dissolver = new SpecialMove(28, "Dissolver", 9, 60, 60.0);
        dissolver.triggeredStances = new byte[]{0};
        dissolver.woundLocationDmg = new byte[]{3};
        dissolver.staminaCost = 18000;
        dissolver.weaponType = 0;
        dissolver.staminaStolen = 25000;
        dissolver.kingdom = (byte)3;
        dissolver.setActorMessage("dissolve");
        dissolver.setOthersMessage("dissolves");
        SpecialMove.addMovesByWeapon(dissolver.weaponType, dissolver);
        SpecialMove cricket = new SpecialMove(29, "Cricket", 5, 19, 25.0);
        cricket.triggeredStances = new byte[]{6, 1, 7};
        cricket.woundLocationDmg = new byte[]{2};
        cricket.staminaCost = 9000;
        cricket.stunseconds = 2;
        cricket.staminaStolen = 2000;
        cricket.weaponType = (byte)-1;
        cricket.kingdom = (byte)2;
        cricket.setActorMessage("string");
        cricket.setOthersMessage("strings");
        SpecialMove.addMovesByWeapon(cricket.weaponType, cricket);
        SpecialMove faithpush = new SpecialMove(30, "Faithpush", 5, 25, 30.0);
        faithpush.triggeredStances = new byte[]{10, 4, 3};
        faithpush.woundLocationDmg = new byte[]{3};
        faithpush.staminaCost = 12000;
        faithpush.staminaStolen = 6000;
        faithpush.weaponType = (byte)-1;
        faithpush.kingdom = (byte)2;
        faithpush.setActorMessage("stomp");
        faithpush.setOthersMessage("stomps");
        SpecialMove.addMovesByWeapon(faithpush.weaponType, faithpush);
        SpecialMove delusion = new SpecialMove(31, "Rampant delusion", 4, 30, 35.0);
        delusion.triggeredStances = new byte[]{5, 2};
        delusion.woundLocation = new int[]{21};
        delusion.woundLocationDmg = new byte[]{4};
        delusion.staminaCost = 10000;
        delusion.stunseconds = 3;
        delusion.weaponType = (byte)2;
        delusion.kingdom = (byte)2;
        delusion.setActorMessage("puncture");
        delusion.setOthersMessage("punctures");
        SpecialMove.addMovesByWeapon(delusion.weaponType, delusion);
        SpecialMove swamp = new SpecialMove(32, "Burning swamp", 7, 40, 45.0);
        swamp.triggeredStances = new byte[]{10, 4, 3};
        swamp.woundLocation = new int[]{34};
        swamp.woundLocationDmg = new byte[]{10};
        swamp.staminaCost = 12000;
        swamp.stunseconds = 2;
        swamp.weaponType = (byte)2;
        swamp.kingdom = (byte)2;
        swamp.setActorMessage("stab");
        swamp.setOthersMessage("stabs");
        SpecialMove.addMovesByWeapon(swamp.weaponType, swamp);
        SpecialMove sensgard = new SpecialMove(33, "Sensitive warden", 4, 45, 50.0);
        sensgard.triggeredStances = new byte[]{7};
        sensgard.woundLocation = new int[]{1};
        sensgard.woundLocationDmg = new byte[]{10};
        sensgard.staminaCost = 15000;
        sensgard.weaponType = (byte)2;
        sensgard.stunseconds = 2;
        sensgard.kingdom = (byte)2;
        sensgard.setActorMessage("penetrate");
        sensgard.setOthersMessage("penetrates");
        SpecialMove.addMovesByWeapon(sensgard.weaponType, sensgard);
        SpecialMove prod = new SpecialMove(34, "Prod", 3, 60, 55.0);
        prod.triggeredStances = new byte[]{10};
        prod.woundLocation = new int[]{25};
        prod.woundLocationDmg = new byte[]{10};
        prod.staminaCost = 15000;
        prod.weaponType = (byte)2;
        prod.stunseconds = 3;
        prod.kingdom = (byte)2;
        prod.setActorMessage("punch holes in");
        prod.setOthersMessage("punches holes in");
        SpecialMove.addMovesByWeapon(prod.weaponType, prod);
        SpecialMove boneb = new SpecialMove(35, "Bonebringer", 5, 30, 35.0);
        boneb.triggeredStances = new byte[]{5, 6};
        boneb.woundLocation = new int[]{23};
        boneb.woundLocationDmg = new byte[]{6};
        boneb.staminaCost = 12000;
        boneb.battleRatingPenalty = 1;
        boneb.weaponType = 1;
        boneb.kingdom = (byte)2;
        boneb.setActorMessage("dissect");
        boneb.setOthersMessage("dissects");
        SpecialMove.addMovesByWeapon(boneb.weaponType, boneb);
        SpecialMove winged = new SpecialMove(36, "Winged fang", 6, 35, 40.0);
        winged.triggeredStances = new byte[]{7, 6, 1};
        winged.woundLocation = new int[]{27, 26, 21};
        winged.woundLocationDmg = new byte[]{2, 2, 4};
        winged.staminaCost = 15000;
        winged.battleRatingPenalty = 1;
        winged.weaponType = 1;
        winged.kingdom = (byte)2;
        winged.setActorMessage("paint");
        winged.setOthersMessage("paints");
        SpecialMove.addMovesByWeapon(winged.weaponType, winged);
        SpecialMove fast = new SpecialMove(37, "Firefangs", 4, 45, 55.0);
        fast.triggeredStances = new byte[]{6, 7};
        fast.woundLocation = new int[]{21, 5};
        fast.woundLocationDmg = new byte[]{6, 6};
        fast.staminaCost = 16000;
        fast.weaponType = 1;
        fast.battleRatingPenalty = 1;
        fast.kingdom = (byte)2;
        fast.setActorMessage("cut");
        fast.setOthersMessage("cuts");
        SpecialMove.addMovesByWeapon(fast.weaponType, fast);
        SpecialMove wildgard = new SpecialMove(38, "Wild garden", 5, 50, 55.0);
        wildgard.triggeredStances = new byte[]{2, 5};
        wildgard.woundLocation = new int[]{17, 29};
        wildgard.woundLocationDmg = new byte[]{7, 6};
        wildgard.staminaCost = 14000;
        wildgard.weaponType = 1;
        wildgard.battleRatingPenalty = 1;
        wildgard.kingdom = (byte)2;
        wildgard.setActorMessage("redecorate");
        wildgard.setOthersMessage("redecorates");
        SpecialMove.addMovesByWeapon(wildgard.weaponType, wildgard);
        SpecialMove bonker = new SpecialMove(39, "Bonker", 5, 25, 30.0);
        bonker.triggeredStances = new byte[]{4, 3, 10};
        bonker.woundLocationDmg = new byte[]{3};
        bonker.staminaCost = 15000;
        bonker.staminaStolen = 5000;
        bonker.pukeMove = true;
        bonker.weaponType = 0;
        bonker.kingdom = (byte)2;
        bonker.setActorMessage("pound");
        bonker.setOthersMessage("pounds");
        SpecialMove.addMovesByWeapon(bonker.weaponType, bonker);
        SpecialMove rotten = new SpecialMove(40, "Rotten stomach", 4, 30, 35.0);
        rotten.triggeredStances = new byte[]{1, 2};
        rotten.woundLocation = new int[]{23};
        rotten.woundLocationDmg = new byte[]{4};
        rotten.staminaCost = 9000;
        rotten.staminaStolen = 5000;
        rotten.weaponType = 0;
        rotten.kingdom = (byte)2;
        rotten.setActorMessage("jam");
        rotten.setOthersMessage("jams");
        SpecialMove.addMovesByWeapon(rotten.weaponType, rotten);
        SpecialMove thing = new SpecialMove(41, "Pain thing", 9, 40, 45.0);
        thing.triggeredStances = new byte[]{5, 4};
        thing.woundLocationDmg = new byte[]{5};
        thing.staminaCost = 16000;
        thing.weaponType = 0;
        thing.pukeMove = true;
        thing.staminaStolen = 15000;
        thing.kingdom = (byte)2;
        thing.setActorMessage("hurt");
        thing.setOthersMessage("hurts");
        SpecialMove.addMovesByWeapon(thing.weaponType, thing);
        SpecialMove tripleimp = new SpecialMove(42, "Triple impact", 6, 50, 55.0);
        tripleimp.triggeredStances = new byte[]{6, 1};
        tripleimp.woundLocation = new int[]{1};
        tripleimp.woundLocationDmg = new byte[]{2};
        tripleimp.staminaCost = 15000;
        tripleimp.staminaStolen = 20000;
        tripleimp.weaponType = 0;
        tripleimp.kingdom = (byte)2;
        tripleimp.setActorMessage("bowl");
        tripleimp.setOthersMessage("bowls");
        SpecialMove.addMovesByWeapon(tripleimp.weaponType, tripleimp);
        SpecialMove lrider = new SpecialMove(1, "Low rider", 5, 19, 25.0);
        lrider.triggeredStances = new byte[]{10};
        lrider.woundLocation = new int[]{25};
        lrider.woundLocationDmg = new byte[]{2};
        lrider.staminaCost = 11000;
        lrider.stunseconds = 2;
        lrider.staminaStolen = 2000;
        lrider.weaponType = (byte)-1;
        lrider.kingdom = (byte)4;
        lrider.setActorMessage("duck low and hit");
        lrider.setOthersMessage("ducks low and hits");
        SpecialMove.addMovesByWeapon(lrider.weaponType, lrider);
        SpecialMove clouds = new SpecialMove(2, "Cold clouds", 7, 25, 30.0);
        clouds.triggeredStances = new byte[]{6, 1, 7};
        clouds.woundLocationDmg = new byte[]{3};
        clouds.staminaCost = 13000;
        clouds.staminaStolen = 8000;
        clouds.weaponType = (byte)-1;
        clouds.kingdom = (byte)4;
        clouds.setActorMessage("knock");
        clouds.setOthersMessage("knocks");
        SpecialMove.addMovesByWeapon(clouds.weaponType, clouds);
        SpecialMove backbr = new SpecialMove(7, "Back breaker", 5, 30, 35.0);
        backbr.triggeredStances = new byte[]{5};
        backbr.woundLocation = new int[]{23, 24};
        backbr.woundLocationDmg = new byte[]{3, 3};
        backbr.battleRatingPenalty = 1;
        backbr.staminaCost = 15000;
        backbr.weaponType = 1;
        backbr.kingdom = (byte)4;
        backbr.setActorMessage("engrave");
        backbr.setOthersMessage("engraves");
        SpecialMove.addMovesByWeapon(backbr.weaponType, backbr);
        SpecialMove bloodsc = new SpecialMove(8, "Bloodscion", 7, 40, 35.0);
        bloodsc.triggeredStances = new byte[]{10, 4, 3};
        bloodsc.woundLocation = new int[]{34, 15, 16};
        bloodsc.woundLocationDmg = new byte[]{4, 2, 2};
        bloodsc.staminaCost = 15000;
        bloodsc.battleRatingPenalty = 1;
        bloodsc.weaponType = 1;
        bloodsc.kingdom = (byte)4;
        bloodsc.setActorMessage("lunge and cut");
        bloodsc.setOthersMessage("lunges and cuts");
        SpecialMove.addMovesByWeapon(bloodsc.weaponType, bloodsc);
        SpecialMove raktak = new SpecialMove(9, "Raktaktak", 4, 50, 55.0);
        raktak.triggeredStances = new byte[]{2, 5};
        raktak.woundLocation = new int[]{3, 21, 4};
        raktak.woundLocationDmg = new byte[]{3, 4, 3};
        raktak.staminaCost = 17000;
        raktak.battleRatingPenalty = 2;
        raktak.weaponType = 1;
        raktak.kingdom = (byte)4;
        raktak.setActorMessage("assault");
        raktak.setOthersMessage("assaults");
        SpecialMove.addMovesByWeapon(raktak.weaponType, raktak);
        SpecialMove tattoo = new SpecialMove(10, "Tattoo twice", 8, 60, 50.0);
        tattoo.triggeredStances = new byte[]{6, 1, 7};
        tattoo.woundLocation = new int[]{1, 22};
        tattoo.woundLocationDmg = new byte[]{4, 5};
        tattoo.staminaCost = 15000;
        tattoo.weaponType = 1;
        tattoo.battleRatingPenalty = 3;
        tattoo.kingdom = (byte)4;
        tattoo.setActorMessage("doubly grind");
        tattoo.setOthersMessage("doubly grinds");
        SpecialMove.addMovesByWeapon(tattoo.weaponType, tattoo);
        SpecialMove sleepwalk = new SpecialMove(25, "Sleepwalker", 4, 25, 30.0);
        sleepwalk.triggeredStances = new byte[]{6, 1, 7};
        sleepwalk.woundLocationDmg = new byte[]{3};
        sleepwalk.pukeMove = true;
        sleepwalk.staminaCost = 15000;
        sleepwalk.staminaStolen = 5000;
        sleepwalk.weaponType = 0;
        sleepwalk.kingdom = (byte)4;
        sleepwalk.setActorMessage("pound");
        sleepwalk.setOthersMessage("pounds");
        SpecialMove.addMovesByWeapon(sleepwalk.weaponType, sleepwalk);
        SpecialMove hammerha = new SpecialMove(26, "Hammerhand", 7, 40, 45.0);
        hammerha.triggeredStances = new byte[]{2};
        hammerha.woundLocation = new int[]{14};
        hammerha.woundLocationDmg = new byte[]{6};
        hammerha.staminaStolen = 10000;
        hammerha.staminaCost = 12000;
        hammerha.weaponType = 0;
        hammerha.kingdom = (byte)4;
        hammerha.setActorMessage("spruce");
        hammerha.setOthersMessage("spruces");
        SpecialMove.addMovesByWeapon(hammerha.weaponType, hammerha);
        SpecialMove echop = new SpecialMove(27, "Echo pain", 4, 50, 55.0);
        echop.triggeredStances = new byte[]{6, 1};
        echop.woundLocation = new int[]{1};
        echop.woundLocationDmg = new byte[]{5};
        echop.staminaCost = 16000;
        echop.staminaStolen = 14000;
        echop.weaponType = 0;
        echop.pukeMove = true;
        echop.kingdom = (byte)4;
        echop.setActorMessage("impact");
        echop.setOthersMessage("impacts");
        SpecialMove.addMovesByWeapon(echop.weaponType, echop);
        SpecialMove highnum = new SpecialMove(28, "High number", 9, 60, 60.0);
        highnum.triggeredStances = new byte[]{0};
        highnum.woundLocationDmg = new byte[]{3};
        highnum.staminaCost = 18000;
        highnum.weaponType = 0;
        highnum.staminaStolen = 25000;
        highnum.kingdom = (byte)4;
        highnum.setActorMessage("numb");
        highnum.setOthersMessage("numbs");
        SpecialMove.addMovesByWeapon(highnum.weaponType, highnum);
        SpecialMove sharptw = new SpecialMove(31, "Sharp twig", 4, 30, 35.0);
        sharptw.triggeredStances = new byte[]{5, 2};
        sharptw.woundLocation = new int[]{21};
        sharptw.woundLocationDmg = new byte[]{4};
        sharptw.staminaCost = 10000;
        sharptw.stunseconds = 3;
        sharptw.weaponType = (byte)2;
        sharptw.kingdom = (byte)4;
        sharptw.setActorMessage("puncture");
        sharptw.setOthersMessage("punctures");
        SpecialMove.addMovesByWeapon(sharptw.weaponType, sharptw);
        SpecialMove snakeb = new SpecialMove(32, "Snakebite", 7, 40, 45.0);
        snakeb.triggeredStances = new byte[]{10, 4, 3};
        snakeb.woundLocation = new int[]{34};
        snakeb.woundLocationDmg = new byte[]{10};
        snakeb.staminaCost = 12000;
        snakeb.stunseconds = 2;
        snakeb.weaponType = (byte)2;
        snakeb.kingdom = (byte)4;
        snakeb.setActorMessage("stab");
        snakeb.setOthersMessage("stabs");
        SpecialMove.addMovesByWeapon(snakeb.weaponType, snakeb);
        SpecialMove eyesock = new SpecialMove(33, "Eyesocket", 4, 45, 50.0);
        eyesock.triggeredStances = new byte[]{7};
        eyesock.woundLocation = new int[]{1};
        eyesock.woundLocationDmg = new byte[]{10};
        eyesock.staminaCost = 15000;
        eyesock.weaponType = (byte)2;
        eyesock.stunseconds = 2;
        eyesock.kingdom = (byte)4;
        eyesock.setActorMessage("penetrate");
        eyesock.setOthersMessage("penetrates");
        SpecialMove.addMovesByWeapon(eyesock.weaponType, eyesock);
        SpecialMove rflood = new SpecialMove(34, "Red flood", 3, 60, 55.0);
        rflood.triggeredStances = new byte[]{10};
        rflood.woundLocation = new int[]{25};
        rflood.woundLocationDmg = new byte[]{10};
        rflood.staminaCost = 15000;
        rflood.weaponType = (byte)2;
        rflood.stunseconds = 3;
        rflood.kingdom = (byte)4;
        rflood.setActorMessage("open up");
        rflood.setOthersMessage("opens up");
        SpecialMove.addMovesByWeapon(rflood.weaponType, rflood);
    }

    public static SpecialMove getById(int id) {
        return specialMoves.get(id);
    }

    public static void addMovesByWeapon(byte weaponType, SpecialMove move) {
        Set<SpecialMove> tempset;
        Map<Integer, Set<SpecialMove>> movesForWeaponType = movesByWeapon.get(weaponType);
        if (movesForWeaponType == null) {
            movesForWeaponType = new HashMap<Integer, Set<SpecialMove>>();
        }
        if ((tempset = movesForWeaponType.get(move.fightingSkillLevelNeeded)) == null) {
            tempset = new HashSet<SpecialMove>();
        }
        tempset.add(move);
        movesForWeaponType.put(move.fightingSkillLevelNeeded, tempset);
        movesByWeapon.put(weaponType, movesForWeaponType);
    }

    public static final SpecialMove[] getMovesForWeaponSkillAndStance(Creature cret, Item weapon, int skill) {
        Set<SpecialMove> tset = null;
        byte creatureKingdom = cret.getKingdomTemplateId();
        Map<Integer, Set<SpecialMove>> tempmap = movesByWeapon.get(cret.getCombatHandler().getType(weapon, true));
        byte creatureStance = cret.getCombatHandler().getCurrentStance();
        tset = SpecialMove.fillTSet(skill, tempmap, creatureKingdom, creatureStance, tset);
        tempmap = movesByWeapon.get((byte)-1);
        tset = SpecialMove.fillTSet(skill, tempmap, creatureKingdom, creatureStance, tset);
        if (tset != null) {
            return tset.toArray(new SpecialMove[tset.size()]);
        }
        return emptyMoves;
    }

    private static final Set<SpecialMove> fillTSet(int skill, Map<Integer, Set<SpecialMove>> aMovesForWeaponType, byte aCreatureKingdom, byte aCreatureStance, @Nullable Set<SpecialMove> tset) {
        Set<SpecialMove> movesToReturn = tset;
        if (aMovesForWeaponType != null) {
            Set<SpecialMove> tempset = null;
            for (Map.Entry<Integer, Set<SpecialMove>> entry : aMovesForWeaponType.entrySet()) {
                if (entry.getKey() > skill || (tempset = entry.getValue()) == null) continue;
                for (SpecialMove tmove : tempset) {
                    if (tmove.kingdom != aCreatureKingdom) continue;
                    for (byte lTriggeredStance : tmove.triggeredStances) {
                        if (aCreatureStance != lTriggeredStance) continue;
                        if (movesToReturn == null) {
                            movesToReturn = new HashSet<SpecialMove>();
                        }
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("Adding " + tmove.name + ", for type " + tmove.weaponType);
                        }
                        movesToReturn.add(tmove);
                    }
                }
            }
        }
        return movesToReturn;
    }

    public double getDifficulty() {
        return this.difficulty;
    }

    public String getName() {
        return this.name;
    }

    public int getStaminaCost() {
        return this.staminaCost;
    }

    public int getSpeed() {
        return this.speed;
    }

    public byte getWeaponType() {
        return this.weaponType;
    }

    public String getActorMessage() {
        return this.actorMessage;
    }

    public void setActorMessage(String aActorMessage) {
        this.actorMessage = aActorMessage;
    }

    public String getOthersMessage() {
        return this.othersMessage;
    }

    public void setOthersMessage(String othersMessage) {
        this.othersMessage = othersMessage;
    }

    static {
        SpecialMove.createMoves();
    }
}


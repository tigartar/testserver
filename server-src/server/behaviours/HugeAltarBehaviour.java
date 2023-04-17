/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.DomainItemBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.zones.NoSuchZoneException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HugeAltarBehaviour
extends DomainItemBehaviour {
    private static final Logger logger = Logger.getLogger(HugeAltarBehaviour.class.getName());
    private static final float MAX_DAM = 99.9f;
    public static final int maxCharges = 30;

    HugeAltarBehaviour() {
        super((short)34);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, target));
        toReturn.addAll(this.getCommonBehaviours(performer, target));
        return toReturn;
    }

    private List<ActionEntry> getCommonBehaviours(Creature performer, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        if (EndGameItems.getEndGameItem(target) != null) {
            if (EndGameItems.getEndGameItem(target).isHoly()) {
                int nums = -3;
                Deity rand = Deities.getRandomNonHateDeity();
                if (rand != null) {
                    --nums;
                }
                toReturn.add(new ActionEntry((short)nums, "Inscriptions", "inscriptions"));
                toReturn.add(Actions.actionEntrys[217]);
                toReturn.add(Actions.actionEntrys[218]);
                toReturn.add(Actions.actionEntrys[219]);
                if (rand != null) {
                    toReturn.add(Actions.actionEntrys[482]);
                }
                if (performer.getDeity() != null && !performer.getDeity().isHateGod() && performer.getFaith() == 30.0f && !performer.isPriest()) {
                    toReturn.add(Actions.actionEntrys[286]);
                }
                if (!performer.isChampion() && !Servers.localServer.isChallengeServer() && performer.getDeity() != null && !performer.getDeity().isHateGod() && performer.getFaith() >= 50.0f) {
                    if (performer.getDeity().isWarrior()) {
                        try {
                            if (performer.getSkills().getSkill(105).getKnowledge(0.0) > 25.0) {
                                toReturn.add(Actions.actionEntrys[220]);
                            }
                        }
                        catch (NoSuchSkillException nss) {
                            logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                            performer.getSkills().learn(105, 1.0f);
                        }
                    } else {
                        try {
                            if (performer.getSkills().getSkill(106).getKnowledge(0.0) > 25.0) {
                                toReturn.add(Actions.actionEntrys[220]);
                            }
                        }
                        catch (NoSuchSkillException nss) {
                            logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul depth.");
                            performer.getSkills().learn(106, 1.0f);
                        }
                    }
                }
            } else {
                int nums = -1;
                Deity rand = Deities.getRandomHateDeity();
                if (rand != null) {
                    --nums;
                }
                toReturn.add(new ActionEntry((short)nums, "Inscriptions", "inscriptions"));
                toReturn.add(Actions.actionEntrys[217]);
                if (rand != null) {
                    toReturn.add(Actions.actionEntrys[482]);
                }
                if (performer.getDeity() != null && performer.getDeity().isHateGod() && performer.getFaith() == 30.0f && !performer.isPriest()) {
                    toReturn.add(Actions.actionEntrys[286]);
                }
                if (!performer.isChampion() && !Servers.localServer.isChallengeServer() && performer.getDeity() != null && performer.getDeity().isHateGod() && performer.getFaith() >= 50.0f) {
                    try {
                        if (performer.getSkills().getSkill(105).getKnowledge(0.0) > 25.0) {
                            toReturn.add(Actions.actionEntrys[220]);
                        }
                    }
                    catch (NoSuchSkillException nss) {
                        logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                        performer.getSkills().learn(105, 1.0f);
                    }
                }
            }
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        toReturn.addAll(this.getCommonBehaviours(performer, target));
        if (EndGameItems.getEndGameItem(target) != null) {
            toReturn.add(Actions.actionEntrys[221]);
        }
        if (source.isArtifact()) {
            toReturn.add(Actions.actionEntrys[370]);
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean done = true;
        boolean reachable = true;
        if (target.getOwnerId() == -10L) {
            reachable = false;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f)) {
                reachable = true;
            }
        }
        if (reachable) {
            if (EndGameItems.getEndGameItem(target) != null) {
                Deity rand;
                if (EndGameItems.getEndGameItem(target).isHoly()) {
                    if (action == 217) {
                        Methods.sendAltarConversion(performer, target, Deities.getDeity(1));
                    } else if (action == 218) {
                        Methods.sendAltarConversion(performer, target, Deities.getDeity(2));
                    } else if (action == 219) {
                        Methods.sendAltarConversion(performer, target, Deities.getDeity(3));
                    } else if (action == 482 && (rand = Deities.getRandomNonHateDeity()) != null) {
                        Methods.sendAltarConversion(performer, target, rand);
                    }
                } else if (action == 217 || action == 482) {
                    if (action == 217) {
                        Methods.sendAltarConversion(performer, target, Deities.getDeity(4));
                    } else {
                        rand = Deities.getRandomHateDeity();
                        if (rand != null) {
                            Methods.sendAltarConversion(performer, target, rand);
                        }
                    }
                }
                if (action == 220) {
                    if (Servers.localServer.isChallengeServer()) {
                        return true;
                    }
                    if (performer.isChampion()) {
                        performer.getCommunicator().sendNormalServerMessage("You are already the Champion of a deity.");
                        return true;
                    }
                    if (System.currentTimeMillis() - performer.getChampTimeStamp() < 14515200000L) {
                        String timefor = Server.getTimeFor(14515200000L + performer.getChampTimeStamp() - System.currentTimeMillis());
                        performer.getCommunicator().sendNormalServerMessage("You may become champion again in " + timefor + ".");
                        return true;
                    }
                    if (EndGameItems.getEndGameItem(target) != null) {
                        if (EndGameItems.getEndGameItem(target).isHoly()) {
                            if (performer.getDeity() != null && !performer.getDeity().isHateGod() && performer.getFaith() >= 50.0f) {
                                if (performer.isKing()) {
                                    performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " will not let a ruler such as yourself risk " + performer.getHisHerItsString() + " life as a champion.");
                                    return true;
                                }
                                if (performer.getDeity().isWarrior()) {
                                    try {
                                        if (performer.getSkills().getSkill(105).getKnowledge(0.0) > 25.0) {
                                            Methods.sendRealDeathQuestion(performer, target, performer.getDeity());
                                        }
                                    }
                                    catch (NoSuchSkillException nss) {
                                        logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                                        performer.getSkills().learn(105, 1.0f);
                                    }
                                } else {
                                    try {
                                        if (performer.getSkills().getSkill(106).getKnowledge(0.0) > 25.0) {
                                            Methods.sendRealDeathQuestion(performer, target, performer.getDeity());
                                        }
                                    }
                                    catch (NoSuchSkillException nss) {
                                        logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul depth.");
                                        performer.getSkills().learn(106, 1.0f);
                                    }
                                }
                            }
                        } else if (performer.getDeity() != null && performer.getDeity().isHateGod() && performer.getFaith() >= 50.0f) {
                            if (performer.isKing()) {
                                performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " will not let a ruler such as yourself risk " + performer.getHisHerItsString() + " life as a champion.");
                                return true;
                            }
                            try {
                                if (performer.getSkills().getSkill(105).getKnowledge(0.0) > 25.0) {
                                    Methods.sendRealDeathQuestion(performer, target, performer.getDeity());
                                }
                            }
                            catch (NoSuchSkillException nss) {
                                logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                                performer.getSkills().learn(106, 1.0f);
                            }
                        }
                    }
                } else if (action == 286) {
                    if (EndGameItems.getEndGameItem(target) != null) {
                        if (EndGameItems.getEndGameItem(target).isHoly()) {
                            if (performer.getDeity() != null && !performer.getDeity().isHateGod() && performer.getFaith() == 30.0f) {
                                MethodsCreatures.sendAskPriestQuestion(performer, target, null);
                            }
                        } else if (performer.getDeity() != null && performer.getDeity().isHateGod() && performer.getFaith() == 30.0f) {
                            MethodsCreatures.sendAskPriestQuestion(performer, target, null);
                        }
                    }
                } else {
                    done = super.action(act, performer, target, action, counter);
                }
            } else if (action == 7 && Constants.loadEndGameItems && performer.getPower() >= 5) {
                boolean holy;
                boolean bl = holy = target.getRealTemplateId() == 327;
                if (holy && EndGameItems.getGoodAltar() != null || !holy && EndGameItems.getEvilAltar() != null) {
                    performer.getCommunicator().sendNormalServerMessage("You must remove the current " + target.getActualName() + " before placing a new one.");
                    return done;
                }
                try {
                    target.putItemInfrontof(performer);
                    EndGameItem eg = new EndGameItem(target, holy, 68, true);
                    EndGameItems.altars.put(new Long(eg.getWurmid()), eg);
                    target.bless(holy ? 1 : 4);
                    target.enchant(holy ? (byte)5 : 8);
                    short eff = holy ? (short)2 : 3;
                    for (Player lPlayer : Players.getInstance().getPlayers()) {
                        lPlayer.getCommunicator().sendAddEffect(eg.getWurmid(), eff, eg.getItem().getPosX(), eg.getItem().getPosY(), eg.getItem().getPosZ(), (byte)0);
                    }
                    logger.log(Level.INFO, "Created " + (holy ? "holy" : "unholy") + " altar at " + target.getPosX() + ", " + target.getPosY() + ".");
                    performer.getCommunicator().sendNormalServerMessage("You successfully place " + target.getNameWithGenus() + " into the world.");
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, nsi.getMessage(), nsi);
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, "Failed to locate creature " + performer.getWurmId(), nsc);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, "Failed to locate player " + performer.getWurmId(), nsp);
                }
                catch (NoSuchZoneException nsz) {
                    Items.destroyItem(target.getWurmId());
                }
            } else {
                done = super.action(act, performer, target, action, counter);
            }
        } else {
            done = super.action(act, performer, target, action, counter);
        }
        return done;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        boolean done = false;
        boolean reachable = true;
        if (target.getOwnerId() == -10L) {
            reachable = false;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f)) {
                reachable = true;
            }
        }
        if (reachable) {
            if (action == 217 || action == 218 || action == 219 || action == 482 || action == 220) {
                done = this.action(act, performer, target, action, counter);
            } else if (action == 221) {
                if (source.getOwnerId() != performer.getWurmId()) {
                    return true;
                }
                done = EndGameItems.getEndGameItem(target) != null ? HugeAltarBehaviour.destroy(act, performer, source, target, counter) : true;
            } else if (action == 370) {
                if (source.getOwnerId() != performer.getWurmId()) {
                    return true;
                }
                if (source.isArtifact()) {
                    if (!Deities.mayDestroyAltars()) {
                        performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is vigilantly protected from Valrei. The position of the moons make it possible to recharge the " + source.getName() + " on Wrath day and the day of Awakening in the first and third week of a Starfall.");
                        return true;
                    }
                    if (EndGameItems.getEndGameItem(target) != null) {
                        if (EndGameItems.getEndGameItem(target).isHoly()) {
                            if (EndGameItems.getEndGameItem(source) == null || !EndGameItems.getEndGameItem(source).isHoly()) {
                                performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you need to try at the other altar?");
                                return true;
                            }
                            if (counter == 1.0f) {
                                if (!EndGameItems.mayRechargeItem()) {
                                    performer.getCommunicator().sendNormalServerMessage("Nothing happens. You have to wait a minute between recharge attempts.");
                                    return true;
                                }
                                Server.getInstance().broadCastNormal("The sky darkens as someone starts to recharge the " + source.getName() + " at the " + target.getName() + ".");
                                Players.getInstance().sendGlobalNonPersistantEffect(-10L, (short)16, target.getTileX(), target.getTileY(), Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(target.getTileX(), target.getTileY())));
                                EndGameItems.touchRecharge();
                            }
                        }
                    } else {
                        return true;
                    }
                    if (EndGameItems.getEndGameItem(target) != null) {
                        if (!EndGameItems.getEndGameItem(target).isHoly()) {
                            if (EndGameItems.getEndGameItem(source) == null || EndGameItems.getEndGameItem(source).isHoly()) {
                                performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you need to try at the other altar?");
                                return true;
                            }
                            if (counter == 1.0f) {
                                if (!EndGameItems.mayRechargeItem()) {
                                    performer.getCommunicator().sendNormalServerMessage("Nothing happens. You have to wait a minute between recharge attempts.");
                                    return true;
                                }
                                Players.getInstance().sendGlobalNonPersistantEffect(-10L, (short)17, target.getTileX(), target.getTileY(), Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(target.getTileX(), target.getTileY())));
                                Server.getInstance().broadCastNormal("The sky gets brighter as someone starts to recharge the " + source.getName() + " at the " + target.getName() + ".");
                                EndGameItems.touchRecharge();
                            }
                        }
                    } else {
                        return true;
                    }
                    done = false;
                    if (counter == 1.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You start to recharge the " + source.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to recharge the " + source.getName() + ".", performer, 5);
                        performer.sendActionControl(Actions.actionEntrys[370].getVerbString(), true, 2400);
                        performer.getStatus().modifyStamina(-500.0f);
                    } else if (act.currentSecond() % 10 == 0) {
                        performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates faintly.");
                        source.setAuxData((byte)Math.min(30, source.getAuxData() + 1));
                        EndGameItems.touchRecharge();
                    } else if (counter >= 300.0f || source.getAuxData() == 30) {
                        performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " is now fully charged.");
                        source.setAuxData((byte)30);
                        done = true;
                    }
                }
            } else {
                done = super.action(act, performer, source, target, action, counter);
            }
        } else {
            done = super.action(act, performer, source, target, action, counter);
        }
        return done;
    }

    private static boolean destroy(Action act, Creature performer, Item source, Item altar, float counter) {
        boolean done = false;
        if (Deities.mayDestroyAltars()) {
            float dam = Weapon.getBaseDamageForWeapon(source);
            if (dam <= 0.0f) {
                performer.getCommunicator().sendNormalServerMessage("That wouldn't leave a dent in the " + altar.getName() + ".");
                return true;
            }
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage("You start to destroy the " + altar.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to destroy the " + altar.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[221].getVerbString(), true, 1000);
                performer.getStatus().modifyStamina(-500.0f);
            } else if (act.currentSecond() % 10 == 0) {
                Skills skills = performer.getSkills();
                Skill weapon = null;
                Skill soul = null;
                try {
                    weapon = skills.getSkill(source.getPrimarySkill());
                }
                catch (NoSuchSkillException nss) {
                    try {
                        weapon = skills.learn(source.getPrimarySkill(), 1.0f);
                    }
                    catch (NoSuchSkillException ns2) {
                        logger.log(Level.WARNING, performer.getName() + " using " + source.getName() + " which has no primary skill!");
                    }
                }
                try {
                    soul = skills.getSkill(105);
                }
                catch (NoSuchSkillException nss) {
                    soul = skills.learn(105, 1.0f);
                }
                if (weapon != null && weapon.skillCheck(80.0, soul.getKnowledge(0.0), true, act.currentSecond()) > 0.0) {
                    if (altar.getDamage() >= 99.9f) {
                        performer.getCommunicator().sendSafeServerMessage("You destroy the " + altar.getName() + "!");
                        EndGameItems.destroyHugeAltar(altar, performer);
                        done = true;
                    } else {
                        if (performer.getPower() > 0 && Servers.isThisATestServer()) {
                            altar.setDamage(Math.min(99.9f, altar.getDamage() + 5.0f));
                        } else if (source.isDestroyHugeAltar()) {
                            altar.setDamage(Math.min(99.9f, altar.getDamage() + 0.1f));
                        } else {
                            altar.setDamage(Math.min(99.9f, altar.getDamage() + (float)Weapon.getModifiedDamageForWeapon(source, performer.getBodyStrength()) / 200000.0f));
                        }
                        performer.getCommunicator().sendNormalServerMessage("Using your skills and drawing from the strength of your soul, you manage to dent the huge altar a little!");
                    }
                }
                performer.getStatus().modifyStamina(-500.0f);
            } else if (act.currentSecond() > 100) {
                done = true;
                if (altar.getDamage() >= 99.9f) {
                    performer.getCommunicator().sendSafeServerMessage("You destroy the " + altar.getName() + "!");
                    EndGameItems.destroyHugeAltar(altar, performer);
                } else {
                    if (performer.getPower() > 0 && Servers.isThisATestServer()) {
                        altar.setDamage(Math.min(99.9f, altar.getDamage() + 10.0f));
                    } else if (source.isDestroyHugeAltar()) {
                        altar.setDamage(Math.min(99.9f, altar.getDamage() + 0.3f));
                    } else {
                        altar.setDamage(Math.min(99.9f, altar.getDamage() + (float)Weapon.getModifiedDamageForWeapon(source, performer.getBodyStrength()) / 100000.0f));
                    }
                    performer.getCommunicator().sendNormalServerMessage("You manage to damage the altar a bit.");
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("The " + altar.getName() + " is vigilantly protected from Valrei. The position of the moons make the " + altar.getName() + " vulnerable on Wrath day and the day of Awakening in the first and third week of a Starfall.");
            done = true;
        }
        return done;
    }
}


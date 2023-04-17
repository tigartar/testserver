/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.Rebirth;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CreatureTypes;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CorpseBehaviour
extends ItemBehaviour
implements CreatureTypes {
    private static final Logger logger = Logger.getLogger(CorpseBehaviour.class.getName());

    CorpseBehaviour() {
        super((short)28);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, target));
        if (performer.getDeity() != null && performer.getDeity().isHateGod()) {
            try {
                CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                if (template.isHuman()) {
                    toReturn.add(Actions.actionEntrys[141]);
                }
            }
            catch (NoSuchCreatureTemplateException nst) {
                logger.log(Level.WARNING, "No creatureTemplate for corpse " + target.getName() + " with id " + target.getWurmId());
            }
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        if ((source.isWeaponKnife() || source.isWeaponSlash() || source.isWeaponPierce()) && !target.isButchered() && (target.getWasBrandedTo() == -10L || target.mayCommand(performer))) {
            toReturn.add(Actions.actionEntrys[120]);
        }
        if (source.getTemplateId() == 25 || source.getTemplateId() == 821 || source.getTemplateId() == 20) {
            if (target.getTemplateId() == 272 && (target.getWasBrandedTo() == -10L || target.mayCommand(performer))) {
                toReturn.add(Actions.actionEntrys[119]);
                toReturn.add(Actions.actionEntrys[707]);
            }
        } else if (source.getTemplateId() == 338) {
            toReturn.add(Actions.actionEntrys[118]);
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean done = true;
        if (action == 1) {
            if (target.isButchered()) {
                performer.getCommunicator().sendNormalServerMessage("You see the butchered " + target.getName() + ".");
            } else {
                performer.getCommunicator().sendNormalServerMessage("You see the " + target.getName() + ".");
            }
            if (target.getWasBrandedTo() != -10L) {
                try {
                    Village wasBrandedTo = Villages.getVillage((int)target.getWasBrandedTo());
                    performer.getCommunicator().sendNormalServerMessage("It still shows a brand from village " + wasBrandedTo.getName() + ".");
                }
                catch (NoSuchVillageException e) {
                    target.setWasBrandedTo(-10L);
                }
            }
        } else if (action == 141) {
            if (performer.getDeity() != null && performer.getDeity().isHateGod()) {
                try {
                    CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                    if (template.isHuman()) {
                        done = MethodsReligion.pray(act, performer, counter);
                    }
                }
                catch (NoSuchCreatureTemplateException nst) {
                    logger.log(Level.WARNING, "No creatureTemplate for corpse " + target.getName() + " with id " + target.getWurmId());
                }
            }
        } else {
            done = super.action(act, performer, target, action, counter);
        }
        return done;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        boolean done;
        block29: {
            done = true;
            if (action == 120) {
                if (target.getWasBrandedTo() != -10L && !target.mayCommand(performer)) {
                    performer.getCommunicator().sendNormalServerMessage("You don't have permission to do that!");
                } else if (!(source.isWeaponKnife() || source.isWeaponSlash() || source.isWeaponPierce())) {
                    performer.getCommunicator().sendNormalServerMessage("You can't butcher with that!");
                } else {
                    done = CorpseBehaviour.butcher(performer, source, target, counter);
                }
            } else if (action == 119 || action == 707) {
                if (MethodsItems.isLootableBy(performer, target)) {
                    done = source.getTemplateId() == 25 || source.getTemplateId() == 20 ? CorpseBehaviour.bury(act, performer, source, target, counter, action) : (source.getTemplateId() == 821 ? CorpseBehaviour.createGrave(act, performer, source, target, counter, action) : true);
                } else {
                    done = true;
                    performer.getCommunicator().sendNormalServerMessage("You may not bury that corpse.");
                }
            } else if (action == 1) {
                if (target.isButchered()) {
                    performer.getCommunicator().sendNormalServerMessage("You see the butchered " + target.getName());
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You see the " + target.getName());
                }
                if (target.getWasBrandedTo() != -10L) {
                    try {
                        Village wasBrandedTo = Villages.getVillage((int)target.getWasBrandedTo());
                        performer.getCommunicator().sendNormalServerMessage("It still shows a brand from village " + wasBrandedTo.getName() + ".");
                    }
                    catch (NoSuchVillageException e) {
                        target.setWasBrandedTo(-10L);
                    }
                }
            } else if (action == 141) {
                if (performer.getDeity() != null && performer.getDeity().isHateGod()) {
                    try {
                        CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                        if (template.isHuman()) {
                            done = MethodsReligion.pray(act, performer, counter);
                            break block29;
                        }
                        done = true;
                    }
                    catch (NoSuchCreatureTemplateException nst) {
                        done = true;
                        logger.log(Level.WARNING, "No creatureTemplate for corpse " + target.getName() + " with id " + target.getWurmId());
                    }
                }
            } else if (action == 118) {
                if (source.getTemplateId() == 338) {
                    if (source.getAuxData() > 0) {
                        if (Rebirth.mayRaise(performer, target, true)) {
                            Rebirth.raise(50.0, performer, target, false);
                            source.setAuxData((byte)(source.getAuxData() - 1));
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " emits no sense of power right now.");
                    }
                }
            } else {
                done = super.action(act, performer, source, target, action, counter);
            }
        }
        return done;
    }

    private static boolean createGrave(Action act, Creature performer, Item gravestone, Item corpse, float counter, short action) {
        block33: {
            int time = 0;
            Skill dig = null;
            try {
                Item shovel;
                CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(corpse.getData1());
                if (corpse.getParentId() != -10L && corpse.getNumItemsNotCoins() > 0) {
                    try {
                        Item parent = Items.getItem(corpse.getParentId());
                        if (parent.getNumItemsNotCoins() >= 100) {
                            performer.getCommunicator().sendNormalServerMessage("The " + parent.getName() + " is full so you need to bury the corpse from the ground.");
                            return true;
                        }
                    }
                    catch (NoSuchItemException parent) {
                        // empty catch block
                    }
                }
                if ((shovel = CorpseBehaviour.getItemOfType(performer.getInventory(), 25)) == null) {
                    performer.getCommunicator().sendNormalServerMessage("You need a shovel in your inventory to do this action.");
                    return true;
                }
                if (counter == 1.0f) {
                    if (!performer.isOnSurface()) {
                        performer.getCommunicator().sendNormalServerMessage("The ground is too hard to bury anything in.");
                        return true;
                    }
                    VolaTile tile = performer.getCurrentTile();
                    int t = Server.surfaceMesh.getTile(tile.tilex, tile.tiley);
                    float h = Tiles.decodeHeight(t);
                    if (h < 0.0f) {
                        performer.getCommunicator().sendNormalServerMessage("The water is too deep.");
                        return true;
                    }
                    if (template.isHuman() && performer.getDeity() != null && performer.getDeity().isAllowsButchering()) {
                        if (performer.faithful) {
                            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " wants corpses to rot in the open.");
                            return true;
                        }
                        performer.maybeModifyAlignment(1.0f);
                    }
                    Skills skills = performer.getSkills();
                    try {
                        dig = skills.getSkill(1009);
                    }
                    catch (NoSuchSkillException nss) {
                        dig = skills.learn(1009, 1.0f);
                    }
                    time = Actions.getStandardActionTime(performer, dig, shovel, 0.0);
                    act.setTimeLeft(time);
                    performer.sendActionControl(Actions.actionEntrys[119].getVerbString(), true, time);
                    performer.getCommunicator().sendNormalServerMessage("You start to bury the " + corpse.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to bury the " + corpse.getName() + ".", performer, 5);
                    return false;
                }
                time = act.getTimeLeft();
                if (act.currentSecond() % 5 == 0) {
                    shovel.setDamage(shovel.getDamage() + 5.0E-4f * shovel.getDamageModifier());
                    VolaTile vtile = performer.getCurrentTile();
                    String sstring = "sound.work.digging1";
                    int x = Server.rand.nextInt(3);
                    if (x == 0) {
                        sstring = "sound.work.digging2";
                    } else if (x == 1) {
                        sstring = "sound.work.digging3";
                    }
                    SoundPlayer.playSound(sstring, vtile.tilex, vtile.tiley, performer.isOnSurface(), 1.0f);
                    performer.getStatus().modifyStamina(-500.0f);
                }
                if (!(counter * 10.0f > (float)time)) break block33;
                VolaTile tile = performer.getCurrentTile();
                int t = Server.surfaceMesh.getTile(tile.tilex, tile.tiley);
                short h = Tiles.decodeHeight(t);
                int tg = Server.rockMesh.getTile(tile.tilex, tile.tiley);
                if (Tiles.decodeHeight(tg) > h - 3) {
                    performer.getCommunicator().sendNormalServerMessage("The rock is too shallow to bury anything in.");
                    return true;
                }
                Skills skills = performer.getSkills();
                try {
                    dig = skills.getSkill(1009);
                }
                catch (NoSuchSkillException nss) {
                    dig = skills.learn(1009, 1.0f);
                }
                dig.skillCheck(corpse.getCurrentQualityLevel(), 0.0, false, counter);
                if (template.isHuman()) {
                    if (performer.getDeity() != null && performer.getDeity().isAllowsButchering() && (float)Server.rand.nextInt(100) > performer.getFaith() - 10.0f) {
                        performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " noticed you and is outraged at your behaviour!");
                        performer.modifyFaith(-0.25f);
                        try {
                            performer.setFavor(performer.getFavor() - 10.0f);
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, "Problem setting the Favor of " + performer.getName() + " after burying a human corpse " + iox.getMessage(), iox);
                        }
                    }
                    if (Servers.localServer.PVPSERVER) {
                        performer.maybeModifyAlignment(1.0f);
                    } else {
                        performer.maybeModifyAlignment(2.0f);
                    }
                }
                try {
                    Item newGravestone = ItemFactory.createItem(822, gravestone.getQualityLevel(), gravestone.getRarity(), gravestone.getCreatorName());
                    newGravestone.setPos(corpse.getPosX(), corpse.getPosY(), corpse.getPosZ(), corpse.getRotation(), corpse.getBridgeId());
                    Zone z = Zones.getZone((int)corpse.getPosX() >> 2, (int)corpse.getPosY() >> 2, corpse.isOnSurface());
                    z.addItem(newGravestone);
                    String name = corpse.getName().replace("corpse of ", "");
                    newGravestone.setDescription(name);
                    newGravestone.setLastOwnerId(performer.getWurmId());
                    if (!template.isHuman() && !template.isUnique() && action == 707) {
                        for (Item i : corpse.getAllItems(false)) {
                            Items.destroyItem(i.getWurmId());
                        }
                    }
                    Items.destroyItem(corpse.getWurmId());
                    Items.destroyItem(gravestone.getWurmId());
                    performer.getCommunicator().sendNormalServerMessage("You bury the " + corpse.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " buries the " + corpse.getName() + ".", performer, 5);
                    performer.achievement(101);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, nsz.getMessage(), nsz);
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
                return true;
            }
            catch (NoSuchCreatureTemplateException nst) {
                logger.log(Level.WARNING, performer.getName() + " had a problem burying " + corpse + ", source item: " + gravestone + ": " + nst.getMessage(), nst);
                return true;
            }
        }
        return false;
    }

    private static boolean bury(Action act, Creature performer, Item source, Item corpse, float counter, short action) {
        boolean done;
        block44: {
            done = false;
            Skill dig = null;
            int buryskill = 1009;
            int time = 1000;
            try {
                byte type;
                int encodedtype;
                VolaTile tile;
                CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(corpse.getData1());
                if (template.isUnique()) {
                    performer.getCommunicator().sendNormalServerMessage("The " + corpse.getName() + " is too large to be buried.");
                    return true;
                }
                if (corpse.getParentId() != -10L && corpse.getNumItemsNotCoins() > 0) {
                    try {
                        Item parent = Items.getItem(corpse.getParentId());
                        if (parent.getNumItemsNotCoins() >= 100) {
                            performer.getCommunicator().sendNormalServerMessage("The " + parent.getName() + " is full so you need to bury the corpse from the ground.");
                            return true;
                        }
                    }
                    catch (NoSuchItemException parent) {
                        // empty catch block
                    }
                }
                if (corpse.getParentOrNull() != null && corpse.getParentOrNull().getTemplate().hasViewableSubItems() && (!corpse.getParentOrNull().getTemplate().isContainerWithSubItems() || corpse.isPlacedOnParent())) {
                    performer.getCommunicator().sendNormalServerMessage("The " + corpse.getName() + " cannot be buried from there.");
                    return true;
                }
                if (counter == 1.0f) {
                    if (!performer.isOnSurface()) {
                        if (source.getTemplateId() != 20) {
                            performer.getCommunicator().sendNormalServerMessage("The ground is too hard to bury anything in using a shovel, try using a pickaxe.");
                            return true;
                        }
                        buryskill = 1008;
                    } else {
                        tile = performer.getCurrentTile();
                        int t = Server.surfaceMesh.getTile(tile.tilex, tile.tiley);
                        byte type2 = Tiles.decodeType(t);
                        if (type2 == Tiles.Tile.TILE_ROCK.id || type2 == Tiles.Tile.TILE_CLIFF.id) {
                            if (source.getTemplateId() != 20) {
                                performer.getCommunicator().sendNormalServerMessage("The ground is too hard to bury anything in using a shovel, try using a pickaxe.");
                                return true;
                            }
                            buryskill = 1008;
                        } else if (source.getTemplateId() != 25) {
                            performer.getCommunicator().sendNormalServerMessage("Try using a shovel to bury this corpse.");
                            return true;
                        }
                    }
                    if (template.isHuman() && performer.getDeity() != null && performer.getDeity().isAllowsButchering()) {
                        if (performer.faithful) {
                            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " wants corpses to rot in the open.");
                            return true;
                        }
                        performer.maybeModifyAlignment(1.0f);
                    }
                    Skills skills = performer.getSkills();
                    dig = skills.getSkillOrLearn(buryskill);
                    time = Actions.getStandardActionTime(performer, dig, source, 0.0);
                    act.setTimeLeft(time);
                    performer.sendActionControl(Actions.actionEntrys[119].getVerbString(), true, time);
                    performer.getCommunicator().sendNormalServerMessage("You start to bury the " + corpse.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to bury the " + corpse.getName() + ".", performer, 5);
                } else {
                    time = act.getTimeLeft();
                }
                if (act.currentSecond() % 5 == 0) {
                    source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
                    VolaTile vtile = performer.getCurrentTile();
                    String sstring = "sound.work.digging1";
                    int x = Server.rand.nextInt(3);
                    if (x == 0) {
                        sstring = "sound.work.digging2";
                    } else if (x == 1) {
                        sstring = "sound.work.digging3";
                    }
                    SoundPlayer.playSound(sstring, vtile.tilex, vtile.tiley, performer.isOnSurface(), 1.0f);
                    performer.getStatus().modifyStamina(-500.0f);
                }
                if (!(counter * 10.0f > (float)time)) break block44;
                tile = performer.getCurrentTile();
                if (!performer.isOnSurface()) {
                    encodedtype = Server.caveMesh.getTile(tile.tilex, tile.tiley);
                    type = Tiles.decodeType(encodedtype);
                    buryskill = 1008;
                } else {
                    encodedtype = Server.surfaceMesh.getTile(tile.tilex, tile.tiley);
                    type = Tiles.decodeType(encodedtype);
                    if (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CLIFF.id) {
                        buryskill = 1008;
                    }
                }
                Skills skills = performer.getSkills();
                dig = skills.getSkillOrLearn(buryskill);
                dig.skillCheck(corpse.getCurrentQualityLevel(), 0.0, false, counter);
                done = true;
                if (template.isHuman()) {
                    if (performer.getDeity() != null && performer.getDeity().isAllowsButchering() && (float)Server.rand.nextInt(100) > performer.getFaith() - 10.0f) {
                        performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " noticed you and is outraged at your behaviour!");
                        performer.modifyFaith(-0.25f);
                        try {
                            performer.setFavor(performer.getFavor() - 10.0f);
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, "Problem setting the Favor of " + performer.getName() + " after burying a human corpse " + iox.getMessage(), iox);
                        }
                    }
                    if (Servers.localServer.PVPSERVER) {
                        performer.maybeModifyAlignment(1.0f);
                    } else {
                        performer.maybeModifyAlignment(2.0f);
                    }
                }
                if (!template.isHuman() && !template.isUnique() && action == 707) {
                    for (Item i : corpse.getAllItems(false)) {
                        Items.destroyItem(i.getWurmId());
                    }
                }
                Items.destroyItem(corpse.getWurmId());
                if (!performer.isOnSurface()) {
                    float h = Tiles.decodeHeight(encodedtype);
                    if (h < 0.0f) {
                        performer.getCommunicator().sendNormalServerMessage("You mine some rocks, attach them to the corpse and watch as the " + corpse.getName() + " sinks into the depths.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You mine some rocks and use them to bury the " + corpse.getName() + ".");
                    }
                } else {
                    short h = Tiles.decodeHeight(encodedtype);
                    if (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CLIFF.id) {
                        if (h < 0) {
                            performer.getCommunicator().sendNormalServerMessage("You mine some rocks, attach them to the corpse and watch as the " + corpse.getName() + " slowly sinks into the depths.");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You mine some rocks and use them to bury the " + corpse.getName() + ".");
                        }
                    } else if (h < 0) {
                        performer.getCommunicator().sendNormalServerMessage("You find some rocks, attach them to the corpse and watch as the " + corpse.getName() + " slowly sinks into the depths.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You bury the " + corpse.getName() + ".");
                    }
                }
                Server.getInstance().broadCastAction(performer.getName() + " buries the " + corpse.getName() + ".", performer, 5);
                performer.achievement(101);
                performer.checkCoinAward(100);
            }
            catch (NoSuchCreatureTemplateException nst) {
                logger.log(Level.WARNING, performer.getName() + " had a problem burying " + corpse + ", source item: " + source + ": " + nst.getMessage(), nst);
                done = true;
            }
        }
        return done;
    }

    private static boolean butcher(Creature performer, Item source, Item corpse, float counter) {
        boolean done;
        block24: {
            done = false;
            Skill butcher = null;
            if (corpse.getOwnerId() != -10L && corpse.getOwnerId() != performer.getWurmId()) {
                done = true;
                performer.getCommunicator().sendNormalServerMessage("You can't reach the " + corpse.getName() + ".");
            }
            if (corpse.isButchered()) {
                done = true;
                performer.getCommunicator().sendNormalServerMessage("The corpse is already butchered.");
            }
            if (corpse.getTopParentOrNull() != performer.getInventory() && !Methods.isActionAllowed(performer, (short)120, corpse)) {
                return true;
            }
            if (!done) {
                try {
                    Skills skills;
                    int time = 1000;
                    Action act = performer.getCurrentAction();
                    double power = 0.0;
                    CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(corpse.getData1());
                    if (counter == 1.0f) {
                        if (template.isHuman() && performer.getDeity() != null && !performer.getDeity().isAllowsButchering() && performer.faithful) {
                            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " does not accept that.");
                            return true;
                        }
                        skills = performer.getSkills();
                        try {
                            butcher = skills.getSkill(10059);
                        }
                        catch (NoSuchSkillException nss) {
                            butcher = skills.learn(10059, 1.0f);
                        }
                        time = Actions.getStandardActionTime(performer, butcher, source, 0.0);
                        act.setTimeLeft(time);
                        performer.sendActionControl(Actions.actionEntrys[120].getVerbString(), true, time);
                        performer.getCommunicator().sendNormalServerMessage("You start to butcher the " + corpse.getName() + ".");
                        Server.getInstance().broadCastAction(performer.getNameWithGenus() + " starts to butcher the " + corpse.getName() + ".", performer, 5);
                        SoundPlayer.playSound("sound.butcherKnife", performer, 1.0f);
                    } else {
                        time = act.getTimeLeft();
                    }
                    if (act.currentSecond() % 5 == 0) {
                        source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
                        SoundPlayer.playSound("sound.butcherKnife", performer, 1.0f);
                    }
                    if (!(counter * 10.0f > (float)time)) break block24;
                    done = true;
                    skills = performer.getSkills();
                    try {
                        butcher = skills.getSkill(10059);
                    }
                    catch (NoSuchSkillException nss) {
                        butcher = skills.learn(10059, 1.0f);
                    }
                    double bonus = 0.0;
                    boolean dryRun = template.isHuman();
                    try {
                        Skill primskill = null;
                        int primarySkill = source.getPrimarySkill();
                        try {
                            primskill = performer.getSkills().getSkill(primarySkill);
                        }
                        catch (Exception ex) {
                            primskill = performer.getSkills().learn(primarySkill, 1.0f);
                        }
                        bonus = primskill.skillCheck(10.0, 0.0, dryRun, counter);
                    }
                    catch (NoSuchSkillException primskill) {
                        // empty catch block
                    }
                    if (source.getTemplateId() != 93) {
                        bonus = 0.0;
                    }
                    int fat = corpse.getFat();
                    if (template.isHuman()) {
                        if (performer.getDeity() != null && !performer.getDeity().isAllowsButchering() && (float)Server.rand.nextInt(100) > performer.getFaith() - 10.0f) {
                            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " noticed you and is outraged at your behaviour!");
                            performer.modifyFaith(-0.25f);
                            try {
                                performer.setFavor(performer.getFavor() - 10.0f);
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, "Problem setting the Favor of " + performer.getName() + " after burying a human corpse  " + iox.getMessage(), iox);
                            }
                        }
                        performer.maybeModifyAlignment(-1.0f);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You butcher the " + corpse.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getNameWithGenus() + " butchers the " + corpse.getName() + ".", performer, 5);
                    CorpseBehaviour.createResult(performer, corpse, butcher, source, Math.min(butcher.getKnowledge(0.0), 0.0), bonus, template, fat);
                    corpse.setButchered();
                }
                catch (NoSuchActionException nsa) {
                    done = true;
                    logger.log(Level.WARNING, performer.getName() + " this action doesn't exist?");
                }
                catch (NoSuchCreatureTemplateException ex) {
                    logger.log(Level.WARNING, "Data1 (templateid) was " + corpse.getData1() + " for corpse with id " + corpse.getWurmId() + ". This is not a valid template.");
                    done = true;
                }
            }
        }
        return done;
    }

    private static void createResult(Creature performer, Item corpse, Skill butcher, Item tool, double power, double bonus, CreatureTemplate creaturetemplate, int fat) {
        block32: {
            try {
                int[] itemnums = creaturetemplate.getItemsButchered();
                String creatureName = "";
                boolean dryRun = true;
                creatureName = creaturetemplate.getName().toLowerCase();
                dryRun = creaturetemplate.isHuman();
                ItemTemplate meattemplate = null;
                int meatType = creaturetemplate.getTemplateId() == 95 ? 900 : 92;
                try {
                    meattemplate = ItemTemplateFactory.getInstance().getTemplate(meatType);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template for meat!");
                }
                boolean createMeat = false;
                if (!dryRun && creaturetemplate.isNeedFood() || creaturetemplate.isAnimal()) {
                    createMeat = true;
                } else if (performer.getKingdomTemplateId() == 3 && !creaturetemplate.isNoSkillgain()) {
                    createMeat = true;
                }
                if (meattemplate != null && corpse.getWeightGrams() < meattemplate.getWeightGrams()) {
                    createMeat = false;
                }
                if (creaturetemplate.isKingdomGuard()) {
                    createMeat = false;
                }
                int diffAdded = 0;
                if (tool.getTemplateId() != 93) {
                    diffAdded = 1;
                }
                if (createMeat && meattemplate != null) {
                    int max = tool.getRarity() + fat / 10;
                    for (int x = 0; x < max; ++x) {
                        power = butcher.skillCheck(Server.rand.nextInt((x + 1 + diffAdded) * 3), tool, bonus, dryRun, 1.0f);
                        if (tool.getSpellEffects() != null) {
                            float imbueEnhancement = 1.0f + tool.getSkillSpellImprovement(10059) / 100.0f;
                            power *= (double)(tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED) * imbueEnhancement);
                        }
                        if (!(power > 0.0 && tool.getTemplateId() == 93) && x != 0) continue;
                        try {
                            Item toCreate = ItemFactory.createItem(meatType, Math.min((float)Math.max((double)(1.0f + Server.rand.nextFloat() * (float)(10 + tool.getRarity() * 5)), Math.min(100.0, power)), corpse.getCurrentQualityLevel()), null);
                            toCreate.setData2(corpse.getData1());
                            toCreate.setMaterial(creaturetemplate.getMeatMaterial());
                            toCreate.setWeight((int)Math.min((float)corpse.getWeightGrams() * 0.5f, (float)(meattemplate.getWeightGrams() * creaturetemplate.getSize())), true);
                            if (toCreate.getWeightGrams() == 0) continue;
                            corpse.insertItem(toCreate, true);
                            performer.getCommunicator().sendNormalServerMessage("You produce " + toCreate.getNameWithGenus() + ".");
                            continue;
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, "No template for meat!");
                        }
                    }
                }
                for (int x = 0; x < itemnums.length; ++x) {
                    if (createMeat && (itemnums[x] == 92 || itemnums[x] == 900)) continue;
                    try {
                        meattemplate = ItemTemplateFactory.getInstance().getTemplate(itemnums[x]);
                        power = butcher.skillCheck(Server.rand.nextInt((x + 1 + diffAdded) * 10), tool, 0.0, dryRun, 1.0f);
                        if (tool.getSpellEffects() != null) {
                            float imbueEnhancement = 1.0f + tool.getSkillSpellImprovement(10059) / 100.0f;
                            power *= (double)(tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED) * imbueEnhancement);
                        }
                        if (power > 0.0) {
                            Item toCreate = ItemFactory.createItem(itemnums[x], Math.min((float)power + Server.rand.nextFloat() * (float)tool.getRarity() * 5.0f, corpse.getCurrentQualityLevel()), null);
                            toCreate.setData2(corpse.getData1());
                            if (toCreate.getTemplateId() != 683) {
                                if (!toCreate.getName().contains(creatureName)) {
                                    toCreate.setName(creatureName.toLowerCase() + " " + meattemplate.getName());
                                }
                                int modWeight = meattemplate.getWeightGrams() * creaturetemplate.getSize();
                                toCreate.setWeight((int)Math.min((float)corpse.getWeightGrams() * 0.5f, (float)modWeight), true);
                                if (toCreate.getTemplateId() == 867) {
                                    if (Server.rand.nextInt(250) == 0) {
                                        toCreate.setRarity((byte)3);
                                    } else if (Server.rand.nextInt(50) == 0) {
                                        toCreate.setRarity((byte)2);
                                    } else {
                                        toCreate.setRarity((byte)1);
                                    }
                                }
                            }
                            if (toCreate.getWeightGrams() == 0) continue;
                            toCreate.setLastOwnerId(performer.getWurmId());
                            corpse.insertItem(toCreate, true);
                            performer.getCommunicator().sendNormalServerMessage("You produce " + toCreate.getNameWithGenus() + ".");
                            continue;
                        }
                        performer.getCommunicator().sendNormalServerMessage("You fail to produce " + meattemplate.getNameWithGenus() + ".");
                        continue;
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, "No template for item id " + itemnums[x]);
                    }
                }
                if (!creaturetemplate.isFromValrei) break block32;
                try {
                    if (power > 0.0) {
                        int chanceModifier = 1;
                        if (Server.rand.nextInt(30 * chanceModifier) == 0) {
                            Item seryll = ItemFactory.createItem(837, Math.min((float)power + Server.rand.nextFloat() * (float)tool.getRarity() * 5.0f, 70.0f + Server.rand.nextFloat() * 5.0f), null);
                            seryll.setLastOwnerId(performer.getWurmId());
                            corpse.insertItem(seryll, true);
                            performer.getCommunicator().sendNormalServerMessage("You manage to extract some seryll from the cranium.");
                        }
                        if (Server.rand.nextInt(60 * chanceModifier) == 0) {
                            int num = 871 + Server.rand.nextInt(14);
                            Item potion = ItemFactory.createItem(num, Math.min((float)power + Server.rand.nextFloat() * (float)tool.getRarity() * 5.0f, 70.0f + Server.rand.nextFloat() * 5.0f), null);
                            potion.setLastOwnerId(performer.getWurmId());
                            corpse.insertItem(potion, true);
                            performer.getCommunicator().sendNormalServerMessage("You manage to extract some weird concoction from the liver.");
                        }
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, performer.getName() + " had a problem with corpse: " + corpse + ", butcher skill: " + butcher + ", tool: " + tool + ", template: " + creaturetemplate + ", fatigue: " + fat + " due to " + fe.getMessage(), fe);
            }
        }
    }

    private static Item getItemOfType(Item container, int templateId) {
        Item[] items = container.getItemsAsArray();
        Item found = null;
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() == templateId) {
                found = items[i];
                break;
            }
            if (items[i].isHollow() && (found = CorpseBehaviour.getItemOfType(items[i], templateId)) != null) break;
        }
        return found;
    }
}


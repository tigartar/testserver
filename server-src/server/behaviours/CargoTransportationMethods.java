/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.VehicleBehaviour;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.behaviours.WarmachineBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstants;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CargoTransportationMethods
implements MiscConstants,
CreatureTemplateIds {
    private static final Logger logger = Logger.getLogger(CargoTransportationMethods.class.getName());
    private static final double LOAD_STRENGTH_NEEDED = 23.0;

    private CargoTransportationMethods() {
    }

    public static final boolean loadCargo(Creature performer, Item target, float counter) {
        Action act = null;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "Unable to get current action in loadCargo().", nsa);
            return true;
        }
        for (Item item : target.getAllItems(true)) {
            if (item.getTemplateId() != 1436 || item.isEmpty(true)) continue;
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has chickens in it, remove them first.");
            return true;
        }
        if (target.getTemplateId() == 1311) {
            try {
                Item vehicle = Items.getItem(performer.getVehicle());
                if (vehicle.getTemplateId() == 491) {
                    performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will not fit in the " + vehicle.getName() + ".");
                    return true;
                }
                if (vehicle.getTemplateId() == 490) {
                    performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will not fit in the " + vehicle.getName() + ".");
                    return true;
                }
            }
            catch (NoSuchItemException ex) {
                logger.log(Level.WARNING, "NoSuchItemException: " + String.valueOf(ex));
                ex.printStackTrace();
            }
            if (target.getTemplateId() == 1311 && !target.isEmpty(true)) {
                for (Item item : target.getAllItems(true)) {
                    try {
                        Creature getCreature = Creatures.getInstance().getCreature(item.getData());
                        if (getCreature.getDominator() != null && getCreature.getDominator() != performer) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot load this cage, the creature inside is not tamed by you.");
                            return true;
                        }
                    }
                    catch (NoSuchCreatureException ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            }
            try {
                int max;
                Item vehicle = Items.getItem(performer.getVehicle());
                int theVessel = vehicle.getTemplateId();
                switch (theVessel) {
                    case 541: {
                        max = 5;
                        break;
                    }
                    case 540: {
                        max = 6;
                        break;
                    }
                    case 542: {
                        max = 4;
                        break;
                    }
                    case 543: {
                        max = 8;
                        break;
                    }
                    case 850: {
                        max = 2;
                        break;
                    }
                    case 1410: {
                        max = 4;
                        break;
                    }
                    default: {
                        max = 0;
                    }
                }
                if (vehicle.getInsertItem() != null && vehicle.getInsertItem().getNumberCages() >= max) {
                    performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will not fit in the " + vehicle.getName() + ".");
                    return true;
                }
            }
            catch (NoSuchItemException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        if (CargoTransportationMethods.isLargeMagicChest(target) && !CargoTransportationMethods.performerIsLastOwner(performer, target)) {
            return true;
        }
        if (CargoTransportationMethods.isAutoRefillWell(target)) {
            performer.getCommunicator().sendNormalServerMessage("This is a special version of the item that is designed to exist only on starter deeds, and is therefor not transportable.");
            return true;
        }
        if (CargoTransportationMethods.targetIsNotTransportable(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsNotOnTheGround(target, performer, false)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsDraggedCheck(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsPlantedCheck(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsOccupiedBed(target, performer, act.getNumber())) {
            return true;
        }
        if (CargoTransportationMethods.performerIsNotOnAVehicle(performer)) {
            return true;
        }
        Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
        if (CargoTransportationMethods.performerIsNotOnATransportVehicle(performer, vehicle)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsSameAsCarrier(performer, vehicle, target)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsHitchedOrCommanded(target, performer)) {
            return true;
        }
        Seat seat = vehicle.getSeatFor(performer.getWurmId());
        if (CargoTransportationMethods.performerIsNotSeatedOnAVehicle(performer, seat)) {
            return true;
        }
        if (CargoTransportationMethods.perfomerActionTargetIsBlocked(performer, target)) {
            return true;
        }
        Item carrier = CargoTransportationMethods.getCarrierItem(vehicle, performer);
        if (carrier == null) {
            return true;
        }
        int distance = CargoTransportationMethods.getLoadActionDistance(carrier);
        if (!CargoTransportationMethods.performerIsWithinDistanceToTarget(performer, target, distance)) {
            return true;
        }
        if (!target.isVehicle()) {
            if (CargoTransportationMethods.targetIsLockedCheck(target, performer, vehicle)) {
                return true;
            }
        } else {
            if (target.isOwnedByWagoner()) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is owned by a wagoner, and will not allow that to be loaded.");
                return true;
            }
            if (CargoTransportationMethods.targetVehicleIsLockedCheck(target, performer, vehicle)) {
                return true;
            }
        }
        if (CargoTransportationMethods.performerMayNotUseInventory(performer, carrier)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsLoadedWarmachine(target, performer, carrier)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsNotEmptyContainerCheck(target, performer, carrier, true)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsOnFireCheck(target, performer, carrier)) {
            return true;
        }
        if (CargoTransportationMethods.targetHasActiveQueen(target, performer, carrier, true)) {
            return true;
        }
        if (target.getTemplateId() != 1311 && CargoTransportationMethods.targetCanNotBeInsertedCheck(target, carrier, vehicle, performer)) {
            return true;
        }
        if (!CargoTransportationMethods.isOnSameLevelLoad(target, performer)) {
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)605)) {
            return true;
        }
        if (target.isPlanted() && !Methods.isActionAllowed(performer, (short)685)) {
            return true;
        }
        if (target.isCrate() && target.isSealedByPlayer() && target.getLastOwnerId() != performer.getWurmId()) {
            String pname = PlayerInfoFactory.getPlayerName(target.getLastOwnerId());
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has a security seal on it, and may only be loaded by " + pname + ".");
            return true;
        }
        if (target.isMarketStall()) {
            int tilex = target.getTileX();
            int tiley = target.getTileY();
            try {
                Zone zone = Zones.getZone(tilex, tiley, target.isOnSurface());
                VolaTile t = zone.getOrCreateTile(tilex, tiley);
                if (t != null && t.getCreatures().length > 0) {
                    for (Creature cret : t.getCreatures()) {
                        if (!cret.isNpcTrader()) continue;
                        performer.getCommunicator().sendSafeServerMessage("This stall is currently in use.");
                        return true;
                    }
                }
            }
            catch (NoSuchZoneException nsz) {
                logger.warning(String.format("Could not find zone at tile [%s, %s] for item %s.", tilex, tiley, target.getName()));
            }
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            if (!CargoTransportationMethods.strengthCheck(performer, 23.0)) {
                String message = StringUtil.format("You are not strong enough to do this, you need at least %.1f body strength.", 23.0);
                performer.getCommunicator().sendNormalServerMessage(message);
                return true;
            }
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to load the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to load the " + target.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[605].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-10000.0f);
            return false;
        }
        time = act.getTimeLeft();
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-10000.0f);
        }
        if (act.currentSecond() == 3 && target.isLocked() && (target.isGuest(-30L) || target.isGuest(-20L) || target.isGuest(-40L))) {
            performer.getCommunicator().sendServerMessage("WARNING - " + target.getName() + " has Group permissions, this WILL cause problems when crossing servers!", 255, 127, 63);
        }
        if (counter * 10.0f > (float)time) {
            if (target.getTemplateId() != 1311 && CargoTransportationMethods.targetCanNotBeInsertedCheck(target, carrier, vehicle, performer)) {
                return true;
            }
            boolean isStealing = MethodsItems.checkIfStealing(target, performer, act);
            if (CargoTransportationMethods.performerIsTryingToStealInLawfulMode(performer, isStealing)) {
                return true;
            }
            Creature[] watchers = null;
            try {
                watchers = target.getWatchers();
            }
            catch (Exception t) {
                // empty catch block
            }
            if (isStealing) {
                Reputation rep;
                if (CargoTransportationMethods.performerMayNotStealCheck(performer)) {
                    return true;
                }
                Skill stealing = null;
                boolean dryRun = false;
                Village v = Zones.getVillage(target.getTileX(), target.getTileY(), true);
                if (v != null && (rep = v.getReputationObject(performer.getWurmId())) != null && rep.getValue() >= 0 && rep.isPermanent()) {
                    dryRun = true;
                }
                if (MethodsItems.setTheftEffects(performer, act, target)) {
                    stealing = performer.getStealSkill();
                    stealing.skillCheck(target.getQualityLevel(), 0.0, dryRun, 10.0f);
                    return true;
                }
                stealing = performer.getStealSkill();
                stealing.skillCheck(target.getQualityLevel(), 0.0, dryRun, 10.0f);
            }
            try {
                if (CargoTransportationMethods.targetIsNotOnTheGround(target, performer, true)) {
                    return true;
                }
                Zone zone = Zones.getZone(target.getTileX(), target.getTileY(), target.isOnSurface());
                zone.removeItem(target);
                if (CargoTransportationMethods.shouldRemovePlantedFlag(target)) {
                    target.setIsPlanted(false);
                }
                carrier.insertItem(target, true);
                if (carrier.getTemplateId() == 1410) {
                    CargoTransportationMethods.updateItemModel(carrier);
                }
                if (watchers != null) {
                    for (Creature c : watchers) {
                        c.getCommunicator().sendCloseInventoryWindow(target.getWurmId());
                    }
                }
                if (target.isCrate() && target.isSealedByPlayer() && target.getLastOwnerId() == performer.getWurmId()) {
                    performer.getCommunicator().sendNormalServerMessage("You accidently knock off the security seal.");
                    target.setIsSealedByPlayer(false);
                }
                performer.getCommunicator().sendNormalServerMessage("You finish loading the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " finishes loading the " + target.getName() + ".", performer, 5);
                target.setLastMaintained(WurmCalendar.currentTime);
            }
            catch (NoSuchZoneException nsz) {
                String message = StringUtil.format("Unable to find zone for x: %d y: %d surface: %s in loadCargo().", target.getTileX(), target.getTileY(), Boolean.toString(target.isOnSurface()));
                logger.log(Level.WARNING, message, nsz);
            }
            return true;
        }
        return false;
    }

    static boolean loadCreature(Creature performer, Item target, float counter) {
        Action act;
        Creature[] folls = performer.getFollowers();
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "Unable to get current action in loadCreature().", nsa);
            return true;
        }
        if (!target.isEmpty(true)) {
            performer.getCommunicator().sendNormalServerMessage("There is already a creature in this " + target.getName() + ".", (byte)3);
            return true;
        }
        for (Creature foll : folls) {
            if (foll.getStatus().getBody().isWounded()) {
                performer.getCommunicator().sendNormalServerMessage("The creature whimpers in pain, and refuses to enter the cage.");
                return true;
            }
            if (foll.getBody().getAllItems().length <= 0) continue;
            performer.getCommunicator().sendNormalServerMessage("You cannot load the creature with items on it, remove them first.");
            return true;
        }
        if (folls.length > 1) {
            performer.getCommunicator().sendNormalServerMessage("You are currently leading too many creatures.");
            return true;
        }
        if (target.getParentId() != -10L) {
            performer.getCommunicator().sendNormalServerMessage("You must unload the cage to load creatures into it.");
            return true;
        }
        if (target.isLocked() && !target.mayAccessHold(performer)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to load creatures into this cage. The target is locked.");
            return true;
        }
        if (target.getCurrentQualityLevel() < 10.0f) {
            performer.getCommunicator().sendNormalServerMessage("The cage is in too poor of shape to be used.");
            return true;
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            for (Creature foll : folls) {
                if (foll.isPregnant()) {
                    performer.getCommunicator().sendNormalServerMessage("You feel terrible caging the unborn offspring of " + foll.getNameWithGenus() + ", and decide not to.");
                    return true;
                }
                if (!performer.isWithinDistanceTo(foll, 5.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("You are too far away from the creature.");
                    return true;
                }
                if (!foll.isWithinDistanceTo(target, 5.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("The creature is too far away from the cage.");
                    return true;
                }
                if (!performer.isWithinDistanceTo(target, 5.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("You are too far away from the cage.");
                    return true;
                }
                act.setTimeLeft(time);
                performer.getCommunicator().sendNormalServerMessage("You start to load the " + foll.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to load the " + foll.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[907].getVerbString(), true, time);
                performer.getStatus().modifyStamina(-10000.0f);
            }
            return false;
        }
        if (counter * 10.0f > (float)time) {
            try {
                for (Creature foll : folls) {
                    if (!foll.getInventory().isEmpty(true)) {
                        for (Item item : foll.getInventory().getAllItems(true)) {
                            Items.destroyItem(item.getWurmId());
                        }
                    }
                    Item insertTarget = target.getInsertItem();
                    Item i = ItemFactory.createItem(1310, foll.getStatus().age, (byte)0, null);
                    if (insertTarget != null) {
                        insertTarget.insertItem(i, true);
                        i.setData(foll.getWurmId());
                        i.setName(foll.getName());
                        i.setWeight((int)foll.getWeight(), false);
                    } else {
                        logger.log(Level.WARNING, "" + performer.getName() + " caused Null pointer.");
                    }
                    DbCreatureStatus.setLoaded(1, foll.getWurmId());
                    target.setAuxData((byte)foll.getTemplate().getTemplateId());
                    foll.setLeader(null);
                    foll.getCurrentTile().deleteCreature(foll);
                    foll.savePosition(-10);
                    foll.destroyVisionArea();
                    foll.getStatus().setDead(true);
                    target.setName("creature cage [" + foll.getName() + "]");
                    CargoTransportationMethods.updateItemModel(target);
                    target.setData(System.currentTimeMillis());
                }
            }
            catch (FailedException | NoSuchTemplateException | IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            for (Creature foll : folls) {
                if (foll.getStatus().getAgeString().equals("venerable")) {
                    performer.getCommunicator().sendNormalServerMessage("The " + foll.getName() + " is " + "venerable" + ", and may die if it crosses to another server.", (byte)3);
                }
                performer.getCommunicator().sendNormalServerMessage("You finish loading the " + foll.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " finishes loading the " + foll.getName() + ".", performer, 5);
            }
            return true;
        }
        return false;
    }

    static boolean unloadCreature(Creature performer, Item target, float counter) {
        Action act;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "Unable to get current action in unloadCreature().", nsa);
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)234)) {
            return true;
        }
        try {
            Creature getCreature = Creatures.getInstance().getCreature(target.getData());
            if (getCreature.getDominator() != null && getCreature.getDominator() != performer) {
                performer.getCommunicator().sendNormalServerMessage("You cannot unload this creature, it is not your pet.");
                return true;
            }
        }
        catch (NoSuchCreatureException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        try {
            if (target.getParent().getParentId() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("You must unload the cage to unload the creature within.");
                return true;
            }
        }
        catch (NoSuchItemException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        if (target.getData() == -10L) {
            performer.getCommunicator().sendNormalServerMessage("The loaded creature has no data, return to the origin server and re-load it.");
            return true;
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to unload the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to unload the " + target.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[908].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-10000.0f);
            return false;
        }
        if (counter * 10.0f > (float)time) {
            try {
                target.getParent().setName("creature cage [Empty]");
                Creature getCreature = Creatures.getInstance().getCreature(target.getData());
                Creatures cstat = Creatures.getInstance();
                getCreature.getStatus().setDead(false);
                cstat.removeCreature(getCreature);
                cstat.addCreature(getCreature, false);
                getCreature.putInWorld();
                CreatureBehaviour.blinkTo(getCreature, performer.getPosX(), performer.getPosY(), performer.getLayer(), performer.getPositionZ(), performer.getBridgeId(), performer.getFloorLevel());
                target.getParent().setDamage((float)((double)target.getParent().getDamage() + getCreature.getSkills().getSkill(102).getKnowledge(0.0) / (double)target.getParent().getQualityLevel()));
                target.getParent().setAuxData((byte)0);
                CargoTransportationMethods.updateItemModel(target.getParent());
                performer.getCommunicator().sendNormalServerMessage("Creature unloaded.");
                performer.getCommunicator().sendNormalServerMessage("The creature damages the cage from anger.");
                getCreature.save();
                getCreature.savePosition(target.getZoneId());
                Items.destroyItem(target.getWurmId());
                target.setLastOwnerId(performer.getWurmId());
                DbCreatureStatus.setLoaded(0, getCreature.getWurmId());
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            performer.getCommunicator().sendNormalServerMessage("You finish unloading the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " finishes unloading the " + target.getName() + ".", performer, 5);
            return true;
        }
        return false;
    }

    private static final boolean isOnSameLevelLoad(Item target, Creature player) {
        if (!GeneralUtilities.isOnSameLevel(player, target)) {
            String m = StringUtil.format("You must be on the same floor level as the %s in order to load it.", target.getName());
            player.getCommunicator().sendNormalServerMessage(m);
            return false;
        }
        return true;
    }

    private static final boolean isOnSameLevelUnload(Item target, Creature player) {
        if (!GeneralUtilities.isOnSameLevel(player, target)) {
            String m = StringUtil.format("You must be on the same floor level as the %s in order to unload items from it.", target.getName());
            player.getCommunicator().sendNormalServerMessage(m);
            return false;
        }
        return true;
    }

    public static final boolean shouldRemovePlantedFlag(Item target) {
        return target.isPlanted();
    }

    public static final boolean loadShip(Creature performer, Item target, float counter) {
        Action act = null;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "Unable to get current action in loadShip().", nsa);
            return true;
        }
        if (CargoTransportationMethods.targetIsMoored(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsDraggedCheck(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsNotOnTheGround(target, performer, false)) {
            return true;
        }
        Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
        if (CargoTransportationMethods.performerIsNotOnATransportVehicle(performer, vehicle)) {
            return true;
        }
        Seat seat = vehicle.getSeatFor(performer.getWurmId());
        if (CargoTransportationMethods.performerIsNotSeatedOnAVehicle(performer, seat)) {
            return true;
        }
        if (CargoTransportationMethods.perfomerActionTargetIsBlocked(performer, target)) {
            return true;
        }
        if (CargoTransportationMethods.targetVehicleIsLockedCheck(target, performer, vehicle)) {
            return true;
        }
        Item carrier = CargoTransportationMethods.getCarrierItem(vehicle, performer);
        if (carrier == null) {
            return true;
        }
        if (CargoTransportationMethods.performerMayNotUseInventory(performer, carrier)) {
            return true;
        }
        if (carrier.getItems().size() > 0) {
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format("The %s is full.", carrier.getName()));
            return true;
        }
        if (CargoTransportationMethods.targetIsHitchedOrCommanded(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsNotEmptyContainerCheck(target, performer, carrier, true)) {
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)605)) {
            return true;
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            float modifier = (float)target.getFullWeight(true) / (float)target.getTemplate().getWeightGrams();
            if (target.isUnfinished()) {
                modifier = 1.0f;
            }
            if (!CargoTransportationMethods.strengthCheck(performer, 23.0 + (double)(3.0f * modifier - 3.0f))) {
                String message = StringUtil.format("You are not strong enough to do this, you need at least %.1f body strength.", 23.0 + (double)(3.0f * modifier - 3.0f));
                performer.getCommunicator().sendNormalServerMessage(message);
                return true;
            }
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to load the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to load the " + target.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[605].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-10000.0f);
            return false;
        }
        time = act.getTimeLeft();
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-10000.0f);
        }
        if (counter * 10.0f > (float)time) {
            boolean isStealing = MethodsItems.checkIfStealing(target, performer, act);
            if (CargoTransportationMethods.performerIsTryingToStealInLawfulMode(performer, isStealing)) {
                return true;
            }
            if (isStealing) {
                Reputation rep;
                if (CargoTransportationMethods.performerMayNotStealCheck(performer)) {
                    return true;
                }
                Skill stealing = null;
                boolean dryRun = false;
                Creature[] v = Zones.getVillage(target.getTileX(), target.getTileY(), true);
                if (v != null && (rep = v.getReputationObject(performer.getWurmId())) != null && rep.getValue() >= 0 && rep.isPermanent()) {
                    dryRun = true;
                }
                if (MethodsItems.setTheftEffects(performer, act, target)) {
                    stealing = performer.getStealSkill();
                    stealing.skillCheck(target.getQualityLevel(), 0.0, dryRun, 10.0f);
                    return true;
                }
                stealing = performer.getStealSkill();
                stealing.skillCheck(target.getQualityLevel(), 0.0, dryRun, 10.0f);
            }
            Creature[] watchers = null;
            try {
                watchers = target.getWatchers();
            }
            catch (Exception dryRun) {
                // empty catch block
            }
            try {
                if (CargoTransportationMethods.targetIsNotOnTheGround(target, performer, true)) {
                    return true;
                }
                Zone zone = Zones.getZone(target.getTileX(), target.getTileY(), target.isOnSurface());
                zone.removeItem(target);
                carrier.insertItem(target, true);
                CargoTransportationMethods.updateItemModel(carrier);
                performer.getCommunicator().sendNormalServerMessage("You finish loading the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " finishes loading the " + target.getName() + ".", performer, 5);
                if (watchers != null) {
                    for (Creature c : watchers) {
                        c.getCommunicator().sendCloseInventoryWindow(target.getWurmId());
                    }
                }
            }
            catch (NoSuchZoneException nsz) {
                String message = StringUtil.format("Unable to find zone for x: %d y: %d surface: %s in loadShip().", target.getTileX(), target.getTileY(), Boolean.toString(target.isOnSurface()));
                logger.log(Level.WARNING, message, nsz);
            }
            return true;
        }
        return false;
    }

    public static final void updateItemModel(Item toUpdate) {
        toUpdate.updateModelNameOnGroundItem();
        toUpdate.updateName();
    }

    private static final boolean targetIsDraggedCheck(Item target, Creature performer) {
        if (target.isDraggable() && Items.isItemDragged(target)) {
            String message = StringUtil.format("You can not load the %s while it's being dragged.", target.getName());
            performer.getCommunicator().sendNormalServerMessage(message);
            return true;
        }
        return false;
    }

    private static final boolean targetIsSameAsCarrier(Creature performer, Vehicle vehicle, Item target) {
        if (vehicle.getWurmid() == target.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You are unable to bend reality enough to accomplish that!");
            return true;
        }
        return false;
    }

    private static final boolean targetIsNotOnTheGround(Item target, Creature performer, boolean finalCheck) {
        if (target.getTopParent() != target.getWurmId()) {
            String message = finalCheck ? "The %s is no longer on the ground, so you can't load it." : "The %s needs to be on the ground.";
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format(message, target.getName()));
            return true;
        }
        return false;
    }

    private static final boolean targetIsMoored(Item target, Creature player) {
        if (target.getData() != -1L) {
            try {
                Item anchor = Items.getItem(target.getData());
                if (anchor.isAnchor()) {
                    player.getCommunicator().sendNormalServerMessage("You are not allowed to load a moored ship.");
                    return true;
                }
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.FINE, "Unable to find item with id: " + target.getData(), nsi);
            }
        }
        return false;
    }

    private static final boolean targetIsHitchedOrCommanded(Item target, Creature performer) {
        if (target.isVehicle()) {
            int i;
            Vehicle targetVehicle = Vehicles.getVehicle(target);
            if (targetVehicle == null) {
                return false;
            }
            for (i = 0; i < targetVehicle.getHitched().length; ++i) {
                if (!targetVehicle.getHitched()[i].isOccupied()) continue;
                String m = StringUtil.format("You can not load the %s while it's being dragged or has creatures hitched.", targetVehicle.getName());
                performer.getCommunicator().sendNormalServerMessage(m);
                return true;
            }
            for (i = 0; i < targetVehicle.getSeats().length; ++i) {
                if (!targetVehicle.getSeats()[i].isOccupied()) continue;
                String m = StringUtil.format("You can not load the %s while someone is using it.", targetVehicle.getName());
                performer.getCommunicator().sendNormalServerMessage(m);
                return true;
            }
        }
        return false;
    }

    private static final boolean targetIsPlantedCheck(Item target, Creature performer) {
        if (target.isEnchantedTurret()) {
            if (target.isPlanted() && (target.getKingdom() != performer.getKingdomId() || target.getLastOwnerId() != performer.getWurmId())) {
                VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
                if (t != null && t.getVillage() != null && t.getVillage().isCitizen(performer) && target.getKingdom() == performer.getKingdomId()) {
                    return false;
                }
                String message = StringUtil.format("The %s is firmly planted in the ground.", target.getName());
                performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
                return true;
            }
        } else if (target.isPlanted() && !ItemBehaviour.isSignManipulationOk(target, performer, (short)6)) {
            String message = StringUtil.format("The %s is firmly planted in the ground.", target.getName());
            performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean targetIsNotTransportable(Item target, Creature performer) {
        if (!target.getTemplate().isTransportable()) {
            String message = StringUtil.format("%s is not transportable in this way.", target.getName());
            performer.getCommunicator().sendNormalServerMessage(message);
            return true;
        }
        return false;
    }

    private static final boolean targetCanNotBeInsertedCheck(Item target, Item carrier, Vehicle vehicle, Creature performer) {
        boolean result = true;
        int freevol = carrier.getFreeVolume();
        int targetvol = target.getVolume();
        if (!carrier.isBoat()) {
            if (carrier.getContainerSizeX() >= target.getSizeX() && carrier.getContainerSizeY() >= target.getSizeY() && carrier.getContainerSizeZ() > target.getSizeZ() && freevol >= targetvol) {
                result = !carrier.mayCreatureInsertItem();
            }
        } else if (freevol >= targetvol) {
            boolean bl = result = !carrier.mayCreatureInsertItem();
        }
        if (result) {
            String message = StringUtil.format("There is not enough room in the %s for the %s.", vehicle.getName(), target.getName());
            performer.getCommunicator().sendNormalServerMessage(message);
            return result;
        }
        return result;
    }

    private static final boolean targetIsOnFireCheck(Item target, Creature performer, Item carrier) {
        if (target.isOnFire() && !target.isAlwaysLit()) {
            String message = StringUtil.format("The %s is still burning and can not be loaded on to the %s.", target.getName(), carrier.getName());
            performer.getCommunicator().sendNormalServerMessage(message);
            return true;
        }
        return false;
    }

    private static final boolean targetHasActiveQueen(Item target, Creature performer, Item carrier, boolean load) {
        if (target.getTemplateId() == 1175 && target.hasQueen() && performer.getBestBeeSmoker() == null && performer.getPower() <= 1) {
            performer.getCommunicator().sendSafeServerMessage("The bees get angry and defend the " + target.getName() + " by stinging you.");
            performer.addWoundOfType(null, (byte)5, 2, true, 1.0f, false, 4000.0f + Server.rand.nextFloat() * 8000.0f, 0.0f, 25.0f, false, false);
            return true;
        }
        return false;
    }

    private static final boolean targetIsNotEmptyContainerCheck(Item target, Creature performer, Item carrier, boolean load) {
        return false;
    }

    private static final boolean targetIsLoadedWarmachine(Item target, Creature performer, Item carrier) {
        if (WarmachineBehaviour.isLoaded(target)) {
            String message = StringUtil.format("The %s must be unloaded before being loaded on to the %s.", target.getName(), carrier.getName());
            performer.getCommunicator().sendNormalServerMessage(message);
            return true;
        }
        return false;
    }

    private static final Item getCarrierItem(Vehicle vehicle, Creature performer) {
        try {
            return Items.getItem(vehicle.getWurmid());
        }
        catch (NoSuchItemException nsi) {
            String message = StringUtil.format("Unable to get vehicle item for vehicle with id: %d.", vehicle.getWurmid());
            logger.log(Level.WARNING, message, nsi);
            return null;
        }
    }

    private static final Item getCarrierItemForTarget(Item target, Creature performer) {
        try {
            return Items.getItem(target.getTopParent());
        }
        catch (NoSuchItemException nsi) {
            String message = StringUtil.format("Unable to get parent vehicle for: %s with top parent: %d", target.getName(), target.getTopParent());
            logger.log(Level.WARNING, message, nsi);
            return null;
        }
    }

    private static final int getLoadActionDistance(Item carrier) {
        int DEFAULT_MAX_RANGE = 4;
        if (!carrier.isVehicle()) {
            return 4;
        }
        Vehicle vehicle = Vehicles.getVehicle(carrier);
        if (vehicle != null) {
            return vehicle.getMaxAllowedLoadDistance();
        }
        return 4;
    }

    private static final boolean targetIsLockedCheck(Item target, Creature performer, Vehicle vehicle) {
        return CargoTransportationMethods.targetIsLockedCheck(target, performer, vehicle, true);
    }

    private static final boolean targetIsLockedCheck(Item target, Creature performer, Vehicle vehicle, boolean sendMessage) {
        if (target.getLockId() != -10L) {
            boolean hasKey;
            boolean locked;
            block7: {
                locked = false;
                hasKey = false;
                try {
                    Item lock = Items.getItem(target.getLockId());
                    locked = lock.isLocked();
                    hasKey = performer.hasKeyForLock(lock);
                    if (target.getTemplateId() != 1311 || !target.isLocked()) break block7;
                    if (target.mayAccessHold(performer)) {
                        hasKey = true;
                        break block7;
                    }
                    if (sendMessage) {
                        performer.getCommunicator().sendNormalServerMessage("You cannot open the " + target.getName() + " so therefore cannot load it.");
                    }
                    return locked;
                }
                catch (NoSuchItemException nsi) {
                    locked = true;
                }
            }
            if (locked && !hasKey) {
                if (sendMessage) {
                    String message = "The %s is locked. It needs to be unlocked before being loaded on to the %s.";
                    performer.getCommunicator().sendSafeServerMessage(StringUtil.format("The %s is locked. It needs to be unlocked before being loaded on to the %s.", target.getName(), vehicle.getName()));
                }
                return locked;
            }
        }
        return false;
    }

    private static final boolean targetVehicleIsLockedCheck(Item target, Creature performer, Vehicle vehicle) {
        if (CargoTransportationMethods.targetIsLockedCheck(target, performer, vehicle, false) && !VehicleBehaviour.mayDriveVehicle(performer, target, null)) {
            String message = "The %s is locked. It needs to be unlocked before being loaded on to the %s, or you must be allowed to embark as a driver.";
            performer.getCommunicator().sendSafeServerMessage(StringUtil.format("The %s is locked. It needs to be unlocked before being loaded on to the %s, or you must be allowed to embark as a driver.", target.getName(), vehicle.getName()));
            return true;
        }
        return false;
    }

    private static final boolean performerIsNotOnAVehicle(Creature performer) {
        if (performer.getVehicle() == -10L) {
            performer.getCommunicator().sendNormalServerMessage("You are not on a vehicle.");
            return true;
        }
        return false;
    }

    private static final boolean performerIsNotOnATransportVehicle(Creature performer, Vehicle vehicle) {
        if (vehicle == null || vehicle.creature) {
            performer.getCommunicator().sendNormalServerMessage("You are not the commander or passenger of a vehicle.");
            return true;
        }
        return false;
    }

    private static final boolean performerIsNotSeatedOnAVehicle(Creature performer, Seat seat) {
        if (seat == null) {
            performer.getCommunicator().sendNormalServerMessage("You need to be the commander or a passenger to do this.");
            return true;
        }
        return false;
    }

    private static final boolean performerIsWithinDistanceToTarget(Creature performer, Item target, int maxDistance) {
        if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), maxDistance)) {
            performer.getCommunicator().sendNormalServerMessage("You need to be closer to do this.");
            return false;
        }
        return true;
    }

    private static final boolean carrierIsNotVehicle(Item carrier, Item target, Creature performer) {
        if (!carrier.isVehicle()) {
            String message = StringUtil.format("%s must be loaded on a vehicle for this to work.", target.getName());
            performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean performerMayNotStealCheck(Creature performer) {
        if (!performer.maySteal()) {
            performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.", (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean performerIsTryingToStealInLawfulMode(Creature performer, boolean isStealing) {
        if (isStealing && Action.checkLegalMode(performer)) {
            performer.getCommunicator().sendNormalServerMessage("This would be stealing. You need to deactivate lafwful mode in order to steal.", (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean perfomerActionTargetIsBlocked(Creature performer, Item target) {
        BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
        if (result != null) {
            String message = StringUtil.format("You can't reach the %s through the %s.", target.getName(), result.getFirstBlocker().getName());
            performer.getCommunicator().sendSafeServerMessage(message, (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean performerIsLastOwner(Creature performer, Item target) {
        if (target.getLastOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You are not the owner of the %s.", target.getName()), (byte)3);
            return false;
        }
        return true;
    }

    private static final boolean isLargeMagicChest(Item target) {
        return target.getTemplateId() == 664;
    }

    private static final boolean isAutoRefillWell(Item target) {
        byte aux;
        return target.getTemplateId() == 608 && ((aux = target.getAuxData()) == 4 || aux == 5 || aux == 6);
    }

    private static final boolean performerMayNotUseInventory(Creature performer, Item carrier) {
        if (carrier.isLocked() && !MethodsItems.mayUseInventoryOfVehicle(performer, carrier)) {
            String message = "You are not allowed to access the inventory of the %s.";
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You are not allowed to access the inventory of the %s.", carrier.getName()), (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean targetIsOccupiedBed(Item target, Creature performer, short actionId) {
        PlayerInfo info;
        if (target.isBed() && (info = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId())) != null) {
            if (actionId == 672 || actionId == 671) {
                String message = StringUtil.format("You can not haul the %s because it's occupied by %s.", target.getName(), info.getName());
                performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
            } else {
                String message = StringUtil.format("You can not load the %s because it's occupied by %s.", target.getName(), info.getName());
                performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
            }
            return true;
        }
        return false;
    }

    public static final boolean unloadCargo(Creature performer, Item target, float counter) {
        VolaTile t;
        if (target.getTopParent() == target.getWurmId()) {
            return true;
        }
        Action act = null;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            String message = StringUtil.format("Unable to find current action for %s in unloadCargo.", performer.getName());
            logger.log(Level.WARNING, message, nsa);
            return true;
        }
        if (target.getTemplateId() == 1311 && (t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), performer.isOnSurface())).getFourPerTileCount(performer.getFloorLevel()) >= 4) {
            performer.getCommunicator().sendNormalServerMessage("You cannot unload this here, there isn't enough room.");
            return true;
        }
        Item carrier = CargoTransportationMethods.getCarrierItemForTarget(target, performer);
        if (carrier == null) {
            return true;
        }
        if (CargoTransportationMethods.carrierIsNotVehicle(carrier, target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.cantBeUnloaded(target, carrier, performer)) {
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)234)) {
            return true;
        }
        int distance = CargoTransportationMethods.getLoadActionDistance(carrier);
        if (!CargoTransportationMethods.performerIsWithinDistanceToTarget(performer, target, distance)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsNotEmptyContainerCheck(target, performer, carrier, false)) {
            return true;
        }
        if (CargoTransportationMethods.targetHasActiveQueen(target, performer, carrier, false)) {
            return true;
        }
        if (!CargoTransportationMethods.mayUnloadHereCheck(target, performer)) {
            return true;
        }
        if (!CargoTransportationMethods.isOnSameLevelUnload(carrier, performer)) {
            return true;
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            if (!CargoTransportationMethods.strengthCheck(performer, 23.0)) {
                performer.getCommunicator().sendNormalServerMessage("You are not strong enough to do this, you need at least 23 body strength.", (byte)3);
                return true;
            }
            act.setTimeLeft(time);
            String youMessage = StringUtil.format("You start to unload the %s from the %s.", target.getName(), carrier.getName());
            String broadcastMessage = StringUtil.format("%s starts to unload the %s from the %s.", performer.getName(), target.getName(), carrier.getName());
            performer.getCommunicator().sendNormalServerMessage(youMessage);
            Server.getInstance().broadCastAction(broadcastMessage, performer, 5);
            performer.sendActionControl(Actions.actionEntrys[606].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-10000.0f);
            return false;
        }
        time = act.getTimeLeft();
        if (act.currentSecond() == 3) {
            Structure structure;
            Village village = Zones.getVillage(target.getTileX(), target.getTileY(), performer.isOnSurface());
            VolaTile vt = Zones.getTileOrNull(target.getTileX(), target.getTileY(), performer.isOnSurface());
            Structure structure2 = structure = vt != null ? vt.getStructure() : null;
            if (!(performer.isOnSurface() && structure != null && structure.isTypeHouse() && structure.isFinished() && structure.isActionAllowed(performer, (short)605) || village == null || village.isActionAllowed((short)605, performer))) {
                performer.getCommunicator().sendServerMessage("WARNING: You currently do not have permissions to re-load this item.", 255, 127, 63);
            }
        }
        if (counter * 10.0f > (float)time) {
            String message;
            try {
                carrier.dropItem(target.getWurmId(), false);
            }
            catch (NoSuchItemException nsi) {
                message = StringUtil.format("Unable to find and drop item: %s with id:%d", target.getName(), target.getWurmId());
                logger.log(Level.WARNING, message, nsi);
                return true;
            }
            try {
                Zone zone = Zones.getZone(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
                target.setPos(performer.getPosX(), performer.getPosY(), performer.getPositionZ(), target.getRotation(), performer.getBridgeId());
                zone.addItem(target);
                if (target.getLockId() == -10L && !target.isVehicle() && !CargoTransportationMethods.isLargeMagicChest(target)) {
                    target.setLastOwnerId(performer.getWurmId());
                }
                if (target.getTemplateId() == 891) {
                    target.setLastOwnerId(performer.getWurmId());
                }
                if (carrier.getTemplateId() == 853 || carrier.getTemplateId() == 1410) {
                    CargoTransportationMethods.updateItemModel(carrier);
                }
                String youMessage = StringUtil.format("You finish unloading the %s from the %s.", target.getName(), carrier.getName());
                String broadcastMessage = StringUtil.format("%s finishes unloading the %s from the %s.", performer.getName(), target.getName(), carrier.getName());
                performer.getCommunicator().sendNormalServerMessage(youMessage);
                Server.getInstance().broadCastAction(broadcastMessage, performer, 5);
                return true;
            }
            catch (NoSuchZoneException nsz) {
                message = StringUtil.format("Unable to find zone for x:%d y:%d surface:%s.", performer.getTileX(), performer.getTileY(), Boolean.toString(performer.isOnSurface()));
                logger.log(Level.WARNING, message, nsz);
                return true;
            }
        }
        return false;
    }

    private static final boolean mayUnloadHereCheck(Item target, Creature player) {
        int encodedTile;
        byte tileType;
        VolaTile tile = Zones.getTileOrNull(player.getTileX(), player.getTileY(), player.isOnSurface());
        if (tile == null) {
            return false;
        }
        int level = tile.getDropFloorLevel(player.getFloorLevel(true));
        if (tile.getNumberOfItems(level) >= 100) {
            String message = "You cannot unload the %s here, since there are too many items here already.";
            player.getCommunicator().sendNormalServerMessage(StringUtil.format("You cannot unload the %s here, since there are too many items here already.", StringUtil.toLowerCase(target.getName())), (byte)3);
            return false;
        }
        if (tile.getNumberOfDecorations(level) >= 15 && !target.isCrate()) {
            String message = "You cannot unload the %s here, since there are too many decorations here already.";
            player.getCommunicator().sendNormalServerMessage(StringUtil.format("You cannot unload the %s here, since there are too many decorations here already.", StringUtil.toLowerCase(target.getName())), (byte)3);
            return false;
        }
        if (target.isOnePerTile() && tile.hasOnePerTileItem(level) || target.isFourPerTile() && tile.getFourPerTileCount(level) == 4) {
            String message = "You cannot unload the %s here, since there is not enough space in front of you.";
            player.getCommunicator().sendNormalServerMessage(StringUtil.format("You cannot unload the %s here, since there is not enough space in front of you.", StringUtil.toLowerCase(target.getName())), (byte)3);
            return false;
        }
        if (target.isOutsideOnly() && tile.getStructure() != null) {
            String message = "You cannot unload the %s here, it must be unloaded outside.";
            player.getCommunicator().sendNormalServerMessage(StringUtil.format("You cannot unload the %s here, it must be unloaded outside.", StringUtil.toLowerCase(target.getName())), (byte)3);
            return false;
        }
        if (target.isSurfaceOnly() && !player.isOnSurface()) {
            String message = "You cannot unload the %s here, it must be unloaded on the surface.";
            player.getCommunicator().sendNormalServerMessage(StringUtil.format("You cannot unload the %s here, it must be unloaded on the surface.", StringUtil.toLowerCase(target.getName())), (byte)3);
            return false;
        }
        if (player.isOnSurface() && target.isSurfaceOnly() && ((tileType = Tiles.decodeType(encodedTile = Server.surfaceMesh.getTile(player.getTileX(), player.getTileY()))) == 0 || Tiles.isMineDoor(tileType))) {
            String message = "You cannot unload the %s here, it cannot be unloaded on a mine door.";
            player.getCommunicator().sendNormalServerMessage(StringUtil.format("You cannot unload the %s here, it cannot be unloaded on a mine door.", StringUtil.toLowerCase(target.getName())), (byte)3);
            return false;
        }
        return true;
    }

    public static final boolean strengthCheck(Creature player, double neededStrength) {
        try {
            Skill strength = player.getSkills().getSkill(102);
            return !(strength.getRealKnowledge() < neededStrength);
        }
        catch (NoSuchSkillException nss) {
            logger.log(Level.WARNING, "Unable to find body strength of player: " + player.getName(), nss);
            return false;
        }
    }

    public static final List<ActionEntry> getHaulActions(Creature player, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        if (target.getTemplate().isTransportable() && !target.isBoat()) {
            int targetFloorLevel;
            int playerFloorLevel = player.getFloorLevel(true);
            if (playerFloorLevel > (targetFloorLevel = target.getFloorLevel())) {
                int floorDiff = playerFloorLevel - targetFloorLevel;
                if (playerFloorLevel > 0 && floorDiff == 1) {
                    VolaTile tile = player.getCurrentTile();
                    if (tile == null) {
                        return toReturn;
                    }
                    Structure structure = tile.getStructure();
                    if (structure == null) {
                        return toReturn;
                    }
                    Floor[] floors = structure.getFloorsAtTile(tile.tilex, tile.tiley, playerFloorLevel * 30, playerFloorLevel * 30);
                    if (floors == null || floors.length == 0) {
                        return toReturn;
                    }
                    if (floors[0].getType() != StructureConstants.FloorType.OPENING) {
                        return toReturn;
                    }
                    toReturn.add(Actions.actionEntrys[671]);
                }
            } else if (playerFloorLevel == targetFloorLevel) {
                VolaTile tile = player.getCurrentTile();
                if (tile == null) {
                    return toReturn;
                }
                Structure structure = tile.getStructure();
                if (structure == null) {
                    return toReturn;
                }
                Floor[] floors = structure.getFloorsAtTile(tile.tilex, tile.tiley, playerFloorLevel * 30, playerFloorLevel * 30);
                if (floors == null || floors.length == 0) {
                    return toReturn;
                }
                if (floors[0].getType() != StructureConstants.FloorType.OPENING) {
                    return toReturn;
                }
                toReturn.add(Actions.actionEntrys[672]);
            }
        }
        return toReturn;
    }

    public static final boolean haul(Creature performer, Item target, float counter, short actionId, Action act) {
        int time;
        double strNeeded = 21.0;
        if (!CargoTransportationMethods.strengthCheck(performer, 21.0)) {
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You need at least %.2f Body Strength to haul.", 21.0), (byte)3);
            return true;
        }
        if (CargoTransportationMethods.targetIsNotOnTheGround(target, performer, false)) {
            return true;
        }
        if (CargoTransportationMethods.isAutoRefillWell(target)) {
            performer.getCommunicator().sendNormalServerMessage("This is a special version of the item that is designed to exist only on starter deeds, and is therefor not transportable.");
            return true;
        }
        if (!target.getTemplate().isTransportable()) {
            performer.getCommunicator().sendNormalServerMessage("You can not haul this item.", (byte)3);
            return true;
        }
        if (target.getTemplate().isContainerWithSubItems()) {
            for (Item i : target.getItems()) {
                if (!i.isPlacedOnParent()) continue;
                performer.getCommunicator().sendNormalServerMessage("You can not haul this item while it has items placed on top of it.", (byte)3);
                return true;
            }
        }
        if (target.isBoat()) {
            performer.getCommunicator().sendNormalServerMessage("You may not haul boats up or down in houses.", (byte)3);
            return true;
        }
        if (CargoTransportationMethods.targetIsPlantedCheck(target, performer)) {
            return true;
        }
        if (CargoTransportationMethods.targetIsOccupiedBed(target, performer, actionId)) {
            return true;
        }
        if (target.isVehicle()) {
            if ((target.getTemplateId() == 853 || target.getTemplateId() == 1410) && target.getItemCount() != 0) {
                performer.getCommunicator().sendNormalServerMessage(StringUtil.format("The %s needs to be empty before you can haul it.", target.getName()), (byte)3);
                return true;
            }
            Vehicle vehicle = Vehicles.getVehicle(target);
            if (vehicle.isAnySeatOccupied()) {
                performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You may not haul the %s when it's in use by another player.", target.getName()), (byte)3);
                return true;
            }
            if (vehicle.isAnythingHitched()) {
                performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You may not haul the %s when there are creatures hitched to it.", target.getName()), (byte)3);
                return true;
            }
        }
        if (target.isDraggable() && Items.isItemDragged(target)) {
            performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You can not haul the %s while it's being dragged.", target.getName()), (byte)3);
            return true;
        }
        int playerFloorLevel = performer.getFloorLevel(true);
        int targetFloorLevel = target.getFloorLevel();
        int diff = Math.abs(playerFloorLevel - targetFloorLevel);
        if (actionId == 671) {
            if (diff > 1) {
                performer.getCommunicator().sendNormalServerMessage("The difference in floor levels is to great, you need to be closer.", (byte)3);
                return true;
            }
        } else if (diff != 0) {
            performer.getCommunicator().sendNormalServerMessage("You must be on the same floor level.", (byte)3);
            return true;
        }
        if (!CargoTransportationMethods.performerIsWithinDistanceToTarget(performer, target, 4)) {
            return true;
        }
        boolean onDeed = Actions.canReceiveDeedSpeedBonus(performer);
        int n = time = onDeed ? 10 : 50;
        if (counter == 1.0f) {
            if (!CargoTransportationMethods.strengthCheck(performer, 21.0)) {
                performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You need at least %.2f Body Strength to haul.", 21.0), (byte)3);
                return true;
            }
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to haul the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to haul the " + target.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[actionId].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-10000.0f);
            return false;
        }
        time = act.getTimeLeft();
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-10000.0f);
        }
        if (counter * 10.0f > (float)time) {
            int tileX = target.getTileX();
            int tileY = target.getTileY();
            try {
                Zone zone = Zones.getZone(tileX, tileY, target.isOnSurface());
                float z = 0.0f;
                z = actionId == 671 ? Zones.calculatePosZ(performer.getPosX(), performer.getPosY(), performer.getCurrentTile(), target.isOnSurface(), performer.isOnSurface(), target.getPosZ(), performer, -10L) : Zones.calculateHeight(target.getPosX(), target.getPosY(), target.isOnSurface()) + (float)(performer.getFloorLevel() - 1) * 3.0f;
                zone.removeItem(target);
                target.setPosXYZ(performer.getPosX(), performer.getPosY(), z);
                zone.addItem(target);
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, "Unable to find zone for item.", nsz);
            }
            return true;
        }
        return false;
    }

    public static final List<ActionEntry> getLoadUnloadActions(Creature player, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        if (!(target.getTemplate().isTransportable() || target.isBoat() || target.isUnfinished())) {
            return toReturn;
        }
        if (target.getTopParent() == target.getWurmId()) {
            Seat seat;
            Vehicle vehicle;
            if (player.getVehicle() != -10L && (vehicle = Vehicles.getVehicleForId(player.getVehicle())) != null && !vehicle.creature && !vehicle.isChair() && (seat = vehicle.getSeatFor(player.getWurmId())) != null) {
                try {
                    Item vehicleItem = Items.getItem(vehicle.getWurmid());
                    if (target.isBoat() && vehicleItem.getTemplateId() != 853) {
                        return toReturn;
                    }
                    if (target.isUnfinished() && vehicleItem.getTemplateId() == 853) {
                        ItemTemplate template = target.getRealTemplate();
                        if (template == null) {
                            return toReturn;
                        }
                        if (!template.isBoat()) {
                            return toReturn;
                        }
                    }
                    if (MethodsItems.mayUseInventoryOfVehicle(player, vehicleItem) || vehicleItem.getLockId() == -10L || Items.isItemDragged(vehicleItem)) {
                        toReturn.add(Actions.actionEntrys[605]);
                    }
                }
                catch (NoSuchItemException nsi) {
                    String message = StringUtil.format("Unable to find vehicle item with id: %s.", vehicle.getWurmid());
                    logger.log(Level.WARNING, message, nsi);
                }
            }
        } else {
            Vehicle vehicle = Vehicles.getVehicleForId(target.getTopParent());
            try {
                if (vehicle != null) {
                    Item vehicleItem = Items.getItem(vehicle.getWurmid());
                    if (vehicle != null && !vehicle.creature && (MethodsItems.mayUseInventoryOfVehicle(player, vehicleItem) || vehicleItem.getLockId() == -10L || Items.isItemDragged(vehicleItem))) {
                        toReturn.add(Actions.actionEntrys[606]);
                    }
                }
            }
            catch (NoSuchItemException nsi) {
                String message = StringUtil.format("Unable to find vehicle item with id: %d.", vehicle.getWurmid());
                logger.log(Level.WARNING, message, nsi);
            }
        }
        return toReturn;
    }

    private static boolean cantBeUnloaded(Item target, Item carrier, Creature performer) {
        if (target.getTopParentOrNull() != null && target.getParentOrNull() != null && target.getParentOrNull().isVehicle() && target.getTopParentOrNull() != target.getParentOrNull()) {
            performer.getCommunicator().sendNormalServerMessage("You can't unload the " + target.getName() + " from there. Unload the " + target.getParentOrNull().getName() + " first.");
            return true;
        }
        if ((target.isBoat() || CargoTransportationMethods.isUnfinishedBoat(target)) && carrier.getTemplateId() == 853) {
            return false;
        }
        if (target.getTemplateId() == 1311 && carrier.getTemplateId() == 1410) {
            return false;
        }
        return CargoTransportationMethods.targetIsNotTransportable(target, performer);
    }

    private static boolean isUnfinishedBoat(Item item) {
        return item.isUnfinished() && item.getRealTemplate() != null && item.getRealTemplate().isBoat();
    }

    static boolean loadChicken(Creature performer, Item target, float counter) {
        Action act;
        Creature[] folls = performer.getFollowers();
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "Unable to get current action in loadChicken().", nsa);
            return true;
        }
        if (folls.length > 1) {
            performer.getCommunicator().sendNormalServerMessage("You are currently leading too many creatures.");
            return true;
        }
        if (target.isLocked() && !target.mayAccessHold(performer)) {
            performer.getCommunicator().sendNormalServerMessage("You cant put a chicken in this coop, its locked.");
            return true;
        }
        if (target.getCurrentQualityLevel() < 10.0f) {
            performer.getCommunicator().sendNormalServerMessage("The coop is in too poor of shape to be used.");
            return true;
        }
        for (Item item : target.getAllItems(true)) {
            if (item.getTemplateId() == 1434 && item.isEmpty(true)) {
                performer.getCommunicator().sendNormalServerMessage("You need to put food in the " + item.getName() + " first.");
                return true;
            }
            if (item.getTemplateId() != 1435 || !item.isEmpty(true)) continue;
            performer.getCommunicator().sendNormalServerMessage("You need to put water in the " + item.getName() + " first.");
            return true;
        }
        if (target.getParentId() != -10L) {
            performer.getCommunicator().sendNormalServerMessage("You must unload the coop to load creatures into it.");
            return true;
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            for (Creature foll : folls) {
                for (Item item : target.getAllItems(true)) {
                    if (item.getTemplateId() != 1436) continue;
                    try {
                        if (item.getAllItems(true).length < (int)item.getParent().getQualityLevel() / 10 + item.getParent().getRarity()) continue;
                        performer.getCommunicator().sendNormalServerMessage("The " + foll.getName() + " refuses to enter the coop. There is no space.");
                        return true;
                    }
                    catch (NoSuchItemException ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
                if (foll.getTemplate().getTemplateId() != 45) {
                    performer.getCommunicator().sendNormalServerMessage("The " + foll.getName() + " refuses to enter the coop.");
                    return true;
                }
                if (!performer.isWithinDistanceTo(foll, 5.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("You are too far away from the creature.");
                    return true;
                }
                if (!foll.isWithinDistanceTo(target, 5.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("The creature is too far away from the coop.");
                    return true;
                }
                if (!performer.isWithinDistanceTo(target, 5.0f)) {
                    performer.getCommunicator().sendNormalServerMessage("You are too far away from the coop.");
                    return true;
                }
                act.setTimeLeft(time);
                performer.getCommunicator().sendNormalServerMessage("You start to load the " + foll.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to load the " + foll.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[907].getVerbString(), true, time);
                performer.getStatus().modifyStamina(-10000.0f);
            }
            return false;
        }
        if (counter * 10.0f > (float)time) {
            try {
                for (Creature foll : folls) {
                    Item i = ItemFactory.createItem(1310, foll.getStatus().age, (byte)0, null);
                    for (Item item : target.getAllItems(true)) {
                        if (item.getTemplateId() != 1436) continue;
                        i.setData(foll.getWurmId());
                        i.setName(foll.getName());
                        i.setWeight((int)foll.getWeight(), false);
                        item.insertItem(i, true);
                    }
                    DbCreatureStatus.setLoaded(1, foll.getWurmId());
                    target.setAuxData((byte)foll.getTemplate().getTemplateId());
                    foll.setLeader(null);
                    foll.getCurrentTile().deleteCreature(foll);
                    try {
                        foll.savePosition(-10);
                        foll.getStatus().setDead(true);
                    }
                    catch (IOException ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                    foll.destroyVisionArea();
                    CargoTransportationMethods.updateItemModel(target);
                    target.setData(System.currentTimeMillis());
                    performer.getCommunicator().sendNormalServerMessage("You finish loading the " + foll.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " finishes loading the " + foll.getName() + ".", performer, 5);
                }
            }
            catch (FailedException | NoSuchTemplateException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            return true;
        }
        return false;
    }

    static boolean unloadChicken(Creature performer, Item target, float counter) {
        Action act;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "Unable to get current action in loadChicken().", nsa);
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)234)) {
            return true;
        }
        try {
            Creature getCreature = Creatures.getInstance().getCreature(target.getData());
            if (getCreature.getDominator() != null && getCreature.getDominator() != performer) {
                performer.getCommunicator().sendNormalServerMessage("You cannot unload this creature, it is not your pet.");
                return true;
            }
        }
        catch (NoSuchCreatureException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        if (target.getData() == -10L) {
            logger.log(Level.WARNING, target.getWurmId() + " has no data, this should not happen, destroying.");
            Items.destroyItem(target.getWurmId());
            return true;
        }
        int time = Actions.getLoadUnloadActionTime(performer);
        if (counter == 1.0f) {
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to unload the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to unload the " + target.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[908].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-10000.0f);
            return false;
        }
        if (counter * 10.0f > (float)time) {
            try {
                Creature getCreature = Creatures.getInstance().getCreature(target.getData());
                DbCreatureStatus.setLoaded(0, getCreature.getWurmId());
                Creatures cstat = Creatures.getInstance();
                getCreature.getStatus().setDead(false);
                cstat.removeCreature(getCreature);
                cstat.addCreature(getCreature, false);
                getCreature.putInWorld();
                CreatureBehaviour.blinkTo(getCreature, performer.getPosX(), performer.getPosY(), performer.getLayer(), performer.getPositionZ(), performer.getBridgeId(), performer.getFloorLevel());
                performer.getCommunicator().sendNormalServerMessage("You finish unloading the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " finishes unloading the " + target.getName() + ".", performer, 5);
                getCreature.save();
                getCreature.savePosition(target.getZoneId());
                if (performer.getFollowers().length < 4) {
                    getCreature.setLeader(performer);
                    performer.addFollower(getCreature, performer.getLeadingItem(getCreature));
                }
                Item nestingBox = target.getParent();
                Item coop = nestingBox.getParent();
                Items.destroyItem(target.getWurmId());
                CargoTransportationMethods.updateItemModel(coop);
            }
            catch (NoSuchItemException | NoSuchCreatureException | IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            return true;
        }
        return false;
    }
}


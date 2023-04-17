/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.shared.constants.ProtoConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class Vehicles
implements ProtoConstants,
CreatureTemplateIds {
    private static final Logger logger = Logger.getLogger(Vehicles.class.getName());
    private static Map<Long, Vehicle> vehicles = new ConcurrentHashMap<Long, Vehicle>();

    private Vehicles() {
    }

    public static Vehicle getVehicle(Item item) {
        Vehicle toReturn = null;
        if ((item.isVehicle() || item.isHitchTarget()) && (toReturn = vehicles.get(item.getWurmId())) == null) {
            toReturn = Vehicles.createVehicle(item);
        }
        return toReturn;
    }

    public static Vehicle getVehicle(Creature creature) {
        Vehicle toReturn = null;
        if (creature.isVehicle() && (toReturn = vehicles.get(creature.getWurmId())) == null) {
            toReturn = Vehicles.createVehicle(creature);
        }
        return toReturn;
    }

    public static Vehicle getVehicleForId(long id) {
        return vehicles.get(id);
    }

    public static Vehicle createVehicle(Item item) {
        Vehicle vehic = new Vehicle(item.getWurmId());
        Vehicles.setSettingsForVehicle(item, vehic);
        vehicles.put(item.getWurmId(), vehic);
        return vehic;
    }

    public static Vehicle createVehicle(Creature creature) {
        Vehicle vehic = new Vehicle(creature.getWurmId());
        Vehicles.setSettingsForVehicle(creature, vehic);
        vehicles.put(creature.getWurmId(), vehic);
        return vehic;
    }

    static void setSettingsForVehicle(Creature creature, Vehicle vehicle) {
        int cid = creature.getTemplate().getTemplateId();
        vehicle.embarkString = "mount";
        vehicle.embarksString = "mounts";
        if (cid == 49 || cid == 3) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.7f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 19.0f;
            if (cid == 49) {
                vehicle.skillNeeded = 23.0f;
            }
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(17.0f);
            vehicle.commandType = (byte)3;
        } else if (cid == 64) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.7f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 21.0f;
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(30.0f);
            vehicle.commandType = (byte)3;
            vehicle.canHaveEquipment = true;
        } else if (cid == 83) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.7f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 31.0f;
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(32.0f);
            vehicle.commandType = (byte)3;
            vehicle.canHaveEquipment = true;
        } else if (CreatureTemplate.isFullyGrownDragon(cid)) {
            vehicle.createPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.3f, 1.5f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 3.0f);
            vehicle.setSeatFightMod(1, 0.3f, 1.3f);
            vehicle.setSeatOffset(1, 1.0f, 0.0f, 3.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 30.0f;
            vehicle.name = creature.getName();
            vehicle.setMaxSpeed(35.0f);
            vehicle.maxDepth = -0.7f;
            vehicle.commandType = (byte)3;
            vehicle.canHaveEquipment = true;
        } else if (CreatureTemplate.isDragonHatchling(cid)) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.6f, 1.3f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.5f);
            vehicle.creature = true;
            vehicle.skillNeeded = 29.0f;
            vehicle.name = creature.getName();
            vehicle.setMaxSpeed(33.0f);
            vehicle.maxDepth = -0.7f;
            vehicle.commandType = (byte)3;
            vehicle.canHaveEquipment = true;
        } else if (cid == 12) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.8f, 1.1f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 23.0f;
            vehicle.name = creature.getName();
            vehicle.maxHeightDiff = 0.04f;
            vehicle.maxDepth = -0.7f;
            vehicle.setMaxSpeed(20.0f);
            vehicle.commandType = (byte)3;
        } else if (cid == 40 || cid == 37 || cid == 86) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.8f, 1.1f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.5f);
            vehicle.creature = true;
            vehicle.skillNeeded = 24.0f;
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(20.0f);
            vehicle.commandType = (byte)3;
        } else if (cid == 59) {
            vehicle.createPassengerSeats(0);
            vehicle.setSeatFightMod(0, 0.5f, 1.1f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 25.0f;
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(17.0f);
            vehicle.commandType = (byte)3;
        } else if (cid == 21) {
            vehicle.createPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.6f, 1.0f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.setSeatFightMod(1, 0.9f, 0.5f);
            vehicle.setSeatOffset(1, 0.5f, 0.0f, 1.54f);
            vehicle.creature = true;
            vehicle.skillNeeded = 26.0f;
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(33.0f);
            vehicle.commandType = (byte)3;
            vehicle.canHaveEquipment = true;
        } else if (cid == 24) {
            vehicle.createPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.6f, 1.0f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.5f);
            vehicle.setSeatFightMod(1, 0.3f, 1.3f);
            vehicle.setSeatOffset(1, 0.5f, -0.3f, 0.5f);
            vehicle.setSeatFightMod(2, 0.3f, 1.3f);
            vehicle.setSeatOffset(2, 0.5f, 0.3f, 0.5f);
            vehicle.creature = true;
            vehicle.skillNeeded = 26.0f;
            vehicle.name = creature.getName();
            vehicle.maxDepth = -0.3f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(20.0f);
            vehicle.commandType = (byte)3;
        } else if (cid == 58) {
            vehicle.createPassengerSeats(1);
            vehicle.setSeatFightMod(0, 1.3f, 1.0f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f);
            vehicle.setSeatOffset(1, 0.5f, 0.0f, 0.0f);
            vehicle.creature = true;
            vehicle.skillNeeded = 25.0f;
            vehicle.maxHeight = 2499.0f;
            vehicle.name = creature.getName();
            vehicle.maxHeightDiff = 0.04f;
            vehicle.setMaxSpeed(11.0f);
            vehicle.commandType = (byte)3;
        }
    }

    static void setSettingsForVehicle(Item item, Vehicle vehicle) {
        Seat[] hitches;
        if (item.getTemplateId() == 861) {
            vehicle.setUnmountable(true);
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.7f, 0.4f);
            vehicle.creature = false;
            vehicle.embarkString = "enter";
            vehicle.embarksString = "enters";
            vehicle.name = item.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2)};
            hitches[0].offx = -3.0f;
            hitches[0].offy = -1.0f;
            vehicle.addHitchSeats(hitches);
        }
        if (item.getTemplateId() == 863) {
            vehicle.setUnmountable(true);
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.7f, 0.4f);
            vehicle.creature = false;
            vehicle.embarkString = "enter";
            vehicle.embarksString = "enters";
            vehicle.name = item.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2)};
            hitches[0].offx = -3.0f;
            hitches[0].offy = -1.0f;
            vehicle.addHitchSeats(hitches);
        }
        if (item.getTemplateId() == 864) {
            vehicle.setUnmountable(true);
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.7f, 0.4f);
            vehicle.creature = false;
            vehicle.embarkString = "enter";
            vehicle.embarksString = "enters";
            vehicle.name = item.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2)};
            hitches[0].offx = -3.0f;
            hitches[0].offy = -1.0f;
            vehicle.addHitchSeats(hitches);
        }
        if (item.getTemplateId() == 186) {
            vehicle.setUnmountable(true);
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setSeatFightMod(0, 0.7f, 0.4f);
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
        } else if (item.getTemplateId() == 490) {
            vehicle.createPassengerSeats(2);
            vehicle.pilotName = "captain";
            vehicle.creature = false;
            vehicle.name = item.getName();
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 0.246f);
            vehicle.setSeatOffset(1, -0.997f, 0.0f, 0.246f);
            vehicle.setSeatOffset(2, -2.018f, 0.0f, 0.246f);
            vehicle.setSeatFightMod(0, 0.7f, 0.4f);
            vehicle.setSeatFightMod(1, 1.5f, 0.4f);
            vehicle.setSeatFightMod(2, 1.5f, 0.4f);
            vehicle.setWindImpact((byte)10);
            vehicle.maxHeight = -0.5f;
            vehicle.skillNeeded = 19.0f;
            vehicle.setMaxSpeed(5.0f);
            vehicle.commandType = 1;
            vehicle.setMaxAllowedLoadDistance(6);
        } else if (item.getTemplateId() == 491) {
            vehicle.createPassengerSeats(4);
            vehicle.pilotName = "captain";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.4f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 0.351f);
            vehicle.setSeatOffset(1, -1.392f, 0.378f, 0.351f);
            vehicle.setSeatOffset(2, -2.15f, -0.349f, 0.341f);
            vehicle.setSeatOffset(3, -3.7f, -0.281f, 0.34f);
            vehicle.setSeatOffset(4, -4.39f, 0.14f, 0.352f);
            vehicle.setWindImpact((byte)30);
            vehicle.maxHeight = -0.5f;
            vehicle.skillNeeded = 20.1f;
            vehicle.setMaxSpeed(5.0f);
            vehicle.commandType = 1;
            vehicle.setMaxAllowedLoadDistance(6);
        } else if (item.getTemplateId() == 539) {
            vehicle.createPassengerSeats(3);
            vehicle.pilotName = "driver";
            vehicle.creature = false;
            vehicle.embarkString = "ride";
            vehicle.embarksString = "rides";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.3f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 1.458f);
            vehicle.setSeatFightMod(1, 1.0f, 0.4f);
            vehicle.setSeatOffset(1, 0.448f, 0.729f, 1.529f);
            vehicle.setSeatFightMod(2, 1.0f, 0.4f);
            vehicle.setSeatOffset(2, 0.65f, -0.697f, 1.568f);
            vehicle.setSeatFightMod(3, 1.0f, 0.0f);
            vehicle.setSeatOffset(3, 1.122f, 0.738f, 1.621f);
            vehicle.maxHeightDiff = 0.04f;
            vehicle.maxDepth = -0.7f;
            vehicle.skillNeeded = 20.1f;
            vehicle.setMaxSpeed(0.75f);
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2), new Seat(2)};
            hitches[0].offx = -2.0f;
            hitches[0].offy = -1.0f;
            hitches[1].offx = -2.0f;
            hitches[1].offy = 1.0f;
            vehicle.addHitchSeats(hitches);
            vehicle.setMaxAllowedLoadDistance(4);
        } else if (item.getTemplateId() == 853) {
            vehicle.createPassengerSeats(0);
            vehicle.pilotName = "driver";
            vehicle.creature = false;
            vehicle.embarkString = "ride";
            vehicle.embarksString = "rides";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.3f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 1.563f);
            vehicle.maxHeightDiff = 0.04f;
            vehicle.maxDepth = -1.5f;
            vehicle.skillNeeded = 20.1f;
            vehicle.setMaxSpeed(0.5f);
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2), new Seat(2)};
            hitches[0].offx = -2.0f;
            hitches[0].offy = -1.0f;
            hitches[1].offx = -2.0f;
            hitches[1].offy = 1.0f;
            vehicle.addHitchSeats(hitches);
        } else if (item.getTemplateId() == 1410) {
            vehicle.createPassengerSeats(1);
            vehicle.pilotName = "driver";
            vehicle.creature = false;
            vehicle.embarkString = "ride";
            vehicle.embarksString = "rides";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.3f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 1.563f);
            vehicle.setSeatFightMod(1, 1.0f, 0.4f);
            vehicle.setSeatOffset(1, 4.05f, 0.0f, 0.84f);
            vehicle.maxHeightDiff = 0.04f;
            vehicle.maxDepth = -1.5f;
            vehicle.skillNeeded = 20.1f;
            vehicle.setMaxSpeed(0.5f);
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2), new Seat(2)};
            hitches[0].offx = -2.0f;
            hitches[0].offy = -1.0f;
            hitches[1].offx = -2.0f;
            hitches[1].offy = 1.0f;
            vehicle.addHitchSeats(hitches);
        } else if (item.getTemplateId() == 850) {
            if (Features.Feature.WAGON_PASSENGER.isEnabled()) {
                vehicle.createPassengerSeats(1);
            } else {
                vehicle.createPassengerSeats(0);
            }
            vehicle.pilotName = "driver";
            vehicle.creature = false;
            vehicle.embarkString = "ride";
            vehicle.embarksString = "rides";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.3f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 1.453f);
            if (Features.Feature.WAGON_PASSENGER.isEnabled()) {
                vehicle.setSeatFightMod(1, 1.0f, 0.4f);
                vehicle.setSeatOffset(1, 4.05f, 0.0f, 0.84f);
            }
            vehicle.maxHeightDiff = 0.04f;
            vehicle.maxDepth = -0.7f;
            vehicle.skillNeeded = 21.0f;
            vehicle.setMaxSpeed(0.7f);
            vehicle.commandType = (byte)2;
            hitches = new Seat[]{new Seat(2), new Seat(2), new Seat(2), new Seat(2)};
            hitches[0].offx = -2.0f;
            hitches[0].offy = -1.0f;
            hitches[1].offx = -2.0f;
            hitches[1].offy = 1.0f;
            hitches[2].offx = -5.0f;
            hitches[2].offy = -1.0f;
            hitches[3].offx = -5.0f;
            hitches[3].offy = 1.0f;
            vehicle.addHitchSeats(hitches);
            vehicle.setMaxAllowedLoadDistance(4);
        } else if (item.getTemplateId() == 541) {
            vehicle.createPassengerSeats(6);
            vehicle.pilotName = "captain";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 3.02f);
            vehicle.setSeatOffset(1, -7.192f, -1.036f, 2.16f);
            vehicle.setSeatOffset(2, 3.0f, 1.287f, 2.47f);
            vehicle.setSeatOffset(3, -3.657f, 1.397f, 1.93f);
            vehicle.setSeatOffset(4, 2.858f, -1.076f, 2.473f);
            vehicle.setSeatOffset(5, -5.625f, 0.679f, 1.926f);
            vehicle.setSeatOffset(6, -2.3f, -1.838f, 1.93f);
            vehicle.setWindImpact((byte)60);
            vehicle.maxHeight = -2.0f;
            vehicle.skillNeeded = 21.0f;
            vehicle.setMaxSpeed(3.8f);
            vehicle.commandType = 1;
            vehicle.setMaxAllowedLoadDistance(12);
        } else if (item.getTemplateId() == 540) {
            vehicle.createPassengerSeats(8);
            vehicle.pilotName = "captain";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 4.011f);
            vehicle.setSeatOffset(1, -16.042f, -0.901f, 3.96f);
            vehicle.setSeatOffset(2, -7.629f, 0.0f, 14.591f);
            vehicle.setSeatOffset(3, -4.411f, -2.097f, 3.51f);
            vehicle.setSeatOffset(4, -16.01f, 0.838f, 3.96f);
            vehicle.setSeatOffset(5, -9.588f, -1.855f, 1.802f);
            vehicle.setSeatOffset(6, -11.08f, 2.451f, 1.805f);
            vehicle.setSeatOffset(7, -4.411f, 1.774f, 3.52f);
            vehicle.setSeatOffset(8, -1.813f, -1.872f, 3.789f);
            vehicle.setWindImpact((byte)80);
            vehicle.maxHeight = -2.0f;
            vehicle.skillNeeded = 22.0f;
            vehicle.setMaxSpeed(3.5f);
            vehicle.commandType = 1;
            vehicle.setMaxAllowedLoadDistance(12);
        } else if (item.getTemplateId() == 542) {
            vehicle.createPassengerSeats(12);
            vehicle.pilotName = "captain";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 0.787f);
            vehicle.setSeatOffset(1, -7.713f, -0.41f, 0.485f);
            vehicle.setSeatOffset(2, -9.722f, 0.455f, 0.417f);
            vehicle.setSeatOffset(3, -3.85f, -0.412f, 0.598f);
            vehicle.setSeatOffset(4, -11.647f, 0.0f, 0.351f);
            vehicle.setSeatOffset(5, -1.916f, -0.211f, 0.651f);
            vehicle.setSeatOffset(6, -12.627f, 0.018f, 0.469f);
            vehicle.setSeatOffset(7, -5.773f, 0.429f, 0.547f);
            vehicle.setSeatOffset(8, -2.882f, 0.388f, 0.626f);
            vehicle.setSeatOffset(9, -8.726f, 0.013f, 0.445f);
            vehicle.setSeatOffset(10, -10.66f, -0.162f, 0.387f);
            vehicle.setSeatOffset(11, -7.708f, 0.454f, 0.479f);
            vehicle.setSeatOffset(12, -5.773f, -0.429f, 0.547f);
            vehicle.setWindImpact((byte)50);
            vehicle.maxHeight = -0.5f;
            vehicle.skillNeeded = 23.0f;
            vehicle.setMaxSpeed(4.1f);
            vehicle.commandType = 1;
            vehicle.setMaxAllowedLoadDistance(8);
        } else if (item.getTemplateId() == 543) {
            vehicle.createPassengerSeats(13);
            vehicle.pilotName = "captain";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 0.9f, 0.9f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.0f, 3.866f);
            vehicle.setSeatOffset(1, -6.98f, 0.0f, 12.189f);
            vehicle.setSeatOffset(2, -14.716f, -0.202f, 3.402f);
            vehicle.setSeatOffset(3, -4.417f, 1.024f, 2.013f);
            vehicle.setSeatOffset(4, 1.206f, -0.657f, 4.099f);
            vehicle.setSeatOffset(5, -7.953f, 0.028f, 0.731f);
            vehicle.setSeatOffset(6, -5.317f, -1.134f, 1.941f);
            vehicle.setSeatOffset(7, -7.518f, 1.455f, 0.766f);
            vehicle.setSeatOffset(8, -2.598f, -0.104f, 2.22f);
            vehicle.setSeatOffset(9, -12.46f, 0.796f, 2.861f);
            vehicle.setSeatOffset(10, -12.417f, -0.82f, 2.852f);
            vehicle.setSeatOffset(11, -4.046f, -0.536f, 2.056f);
            vehicle.setSeatOffset(12, -1.089f, 1.004f, 3.65f);
            vehicle.setSeatOffset(13, -0.942f, -0.845f, 3.678f);
            vehicle.setWindImpact((byte)70);
            vehicle.maxHeight = -2.0f;
            vehicle.skillNeeded = 24.0f;
            vehicle.setMaxSpeed(4.0f);
            vehicle.commandType = 1;
            vehicle.setMaxAllowedLoadDistance(12);
        } else if (item.getTemplateId() == 931) {
            vehicle.createPassengerSeats(0);
            vehicle.pilotName = "pusher";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setWindImpact((byte)0);
            vehicle.maxHeight = 6000.0f;
            vehicle.maxDepth = -1.0f;
            vehicle.skillNeeded = 200.0f;
            vehicle.setMaxSpeed(3.0f);
        } else if (item.getTemplateId() == 936 || item.getTemplateId() == 937) {
            vehicle.createPassengerSeats(0);
            vehicle.pilotName = "mover";
            vehicle.creature = false;
            vehicle.embarkString = "board";
            vehicle.embarksString = "boards";
            vehicle.name = item.getName();
            vehicle.setWindImpact((byte)0);
            vehicle.maxHeight = 6000.0f;
            vehicle.maxDepth = -1.0f;
            vehicle.skillNeeded = 200.0f;
            vehicle.setMaxSpeed(0.0f);
        } else if (item.getTemplateId() == 263 || item.getTemplateId() == 265) {
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setChair(true);
            vehicle.creature = false;
            vehicle.embarkString = "sit";
            vehicle.embarksString = "sits";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 1.0f, 0.4f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.59f);
            vehicle.setWindImpact((byte)0);
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
        } else if (item.getTemplateId() == 404 || item.getTemplateId() == 891 || item.getTemplateId() == 924) {
            vehicle.createOnlyPassengerSeats(2);
            vehicle.setChair(true);
            vehicle.creature = false;
            vehicle.embarkString = "sit";
            vehicle.embarksString = "sits";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 1.0f, 0.4f);
            vehicle.setSeatOffset(0, 0.0f, -0.4f, 0.59f);
            vehicle.setSeatFightMod(1, 1.0f, 0.4f);
            vehicle.setSeatOffset(1, 0.0f, 0.4f, 0.59f);
            vehicle.setWindImpact((byte)0);
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
        } else if (item.getTemplateId() == 261 || item.getTemplateId() == 913 || item.getTemplateId() == 914 || item.getTemplateId() == 915) {
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setChair(true);
            vehicle.creature = false;
            vehicle.embarkString = "sit";
            vehicle.embarksString = "sits";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 1.0f, 0.4f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.57f);
            vehicle.setWindImpact((byte)0);
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
        } else if (item.getTemplateId() == 894) {
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setChair(true);
            vehicle.creature = false;
            vehicle.embarkString = "sit";
            vehicle.embarksString = "sits";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 1.0f, 0.4f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.63f);
            vehicle.setWindImpact((byte)0);
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
        } else if (item.getTemplateId() == 1313 || item.getTemplateId() == 484 || item.getTemplateId() == 890) {
            vehicle.createOnlyPassengerSeats(1);
            vehicle.setBed(true);
            vehicle.creature = false;
            vehicle.embarkString = "lie down";
            vehicle.embarksString = "lies down";
            vehicle.name = item.getName();
            vehicle.setSeatFightMod(0, 1.0f, 0.4f);
            vehicle.setSeatOffset(0, 0.0f, 0.0f, 0.57f);
            vehicle.setWindImpact((byte)0);
            vehicle.maxDepth = -0.7f;
            vehicle.maxHeightDiff = 0.04f;
            vehicle.commandType = (byte)2;
        }
    }

    public static void destroyVehicle(long id) {
        Vehicle vehicle = vehicles.get(id);
        if (vehicle != null) {
            vehicle.kickAll();
            vehicle.seats = Vehicle.EMPTYSEATS;
            if (vehicle.getDraggers() != null) {
                vehicle.purgeDraggers();
            }
        }
        vehicles.remove(id);
        ItemSettings.remove(id);
    }

    static int getNumberOfVehicles() {
        if (vehicles != null) {
            return vehicles.size();
        }
        logger.warning("vehicles Map is null");
        return 0;
    }

    public static final void removeDragger(Creature dragger) {
        for (Vehicle draggedVehicle : vehicles.values().toArray(new Vehicle[vehicles.size()])) {
            if (!draggedVehicle.removeDragger(dragger)) continue;
            dragger.setHitched(null, false);
            return;
        }
    }
}


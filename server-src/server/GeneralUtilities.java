/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;
import java.util.SimpleTimeZone;

public final class GeneralUtilities
implements MiscConstants {
    private GeneralUtilities() {
    }

    public static boolean isValidTileLocation(int tilex, int tiley) {
        return tilex >= 0 && tilex < 1 << Constants.meshSize && tiley >= 0 && tiley < 1 << Constants.meshSize;
    }

    public static float calcOreRareQuality(double power, int actionBonus, int toolBonus) {
        return GeneralUtilities.calcRareQuality(power, actionBonus, toolBonus, 0, 2, 108.428f);
    }

    public static float calcRareQuality(double power, int actionBonus, int toolBonus) {
        return GeneralUtilities.calcRareQuality(power, actionBonus, toolBonus, 0, 2, 100.0f);
    }

    public static float calcRareQuality(double power, int actionBonus, int toolBonus, int targetBonus) {
        return GeneralUtilities.calcRareQuality(power, actionBonus, toolBonus, targetBonus, 3, 100.0f);
    }

    public static float calcRareQuality(double power, int actionBonus, int toolBonus, int targetBonus, int numbBonus, float fiddleFactor) {
        float rPower = (float)power;
        int totalBonus = toolBonus + targetBonus + actionBonus;
        float bonus = 0.0f;
        if (totalBonus > 0) {
            float val = fiddleFactor - rPower;
            float square = val * val;
            float n = square / 1000.0f;
            float mod = Math.min(n * 1.25f, 1.0f);
            bonus = (float)totalBonus * 3.0f / (float)numbBonus * mod;
        }
        return Math.max(Math.min(99.999f, rPower + bonus), 1.0f);
    }

    public static final Map<String, Map<CreationEntry, Integer>> getCreationList(Item source, Item target, Player player) {
        CreationEntry[] entries = CreationMatrix.getInstance().getCreationOptionsFor(source, target);
        Map<String, Map<CreationEntry, Integer>> map = ItemBehaviour.generateMapfromOptions(player, source, target, entries);
        return map;
    }

    public static String toGMTString(long aDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return sdf.format(new Date(aDate));
    }

    public static void setSettingsBits(BitSet bits, int value) {
        for (int x = 0; x < 32; ++x) {
            bits.set(x, (value & 1) == 1);
        }
    }

    public static int getIntSettingsFrom(BitSet bits) {
        int ret = 0;
        for (int x = 0; x < 32; ++x) {
            if (!bits.get(x)) continue;
            ret = (int)((long)ret + (1L << x));
        }
        return ret;
    }

    public static boolean isOnSameLevel(Creature creature1, Creature creature2) {
        float difference = Math.abs(creature1.getStatus().getPositionZ() - creature2.getStatus().getPositionZ()) * 10.0f;
        return difference < 30.0f;
    }

    public static boolean mayAttackSameLevel(Creature creature1, Creature creature2) {
        float difference = Math.abs(creature1.getStatus().getPositionZ() - creature2.getStatus().getPositionZ()) * 10.0f;
        return difference < 29.7f;
    }

    public static boolean isOnSameLevel(Creature creature, Item item) {
        float difference;
        Vehicle vehicle;
        float pz = creature.getStatus().getPositionZ();
        if (creature.getVehicle() != -10L && (vehicle = Vehicles.getVehicleForId(creature.getVehicle())) != null) {
            pz = vehicle.getPosZ();
        }
        return (difference = Math.abs(Math.max(0.0f, pz) - Math.max(0.0f, item.getPosZ())) * 10.0f) < 30.0f;
    }

    public static short getHeight(int tilex, int tiley, boolean onSurface) {
        if (onSurface) {
            return Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley));
        }
        return Tiles.decodeHeight(Server.caveMesh.getTile(tilex, tiley));
    }
}


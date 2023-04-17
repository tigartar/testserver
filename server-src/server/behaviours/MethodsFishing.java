/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Point;
import com.wurmonline.server.Point4f;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.FishEnums;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.scripts.FishAI;
import com.wurmonline.server.items.ContainerRestriction;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.FishingEnums;
import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class MethodsFishing
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(MethodsFishing.class.getName());

    private MethodsFishing() {
    }

    static boolean fish(Creature performer, Item source, int tilex, int tiley, int tile, float counter, Action act) {
        if (!Terraforming.isTileUnderWater(tile, tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("The water is too shallow to fish.");
            return true;
        }
        Item[] fishingItems = source.getFishingItems();
        Item fishingReel = fishingItems[0];
        Item fishingLine = fishingItems[1];
        Item fishingFloat = fishingItems[2];
        Item fishingHook = fishingItems[3];
        Item fishingBait = fishingItems[4];
        Skill fishing = performer.getSkills().getSkillOrLearn(10033);
        int timeleft = 1800;
        int defaultTimer = 1800;
        int startCommand = 0;
        if (source.getTemplateId() == 1343) {
            startCommand = 40;
            if (performer.getVehicle() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("You cannot use a fishing net whilst on a vehicle.");
                return true;
            }
        } else if (source.getTemplateId() == 705 || source.getTemplateId() == 707) {
            startCommand = 20;
            if (performer.getVehicle() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("You cannot use a spear to fish whilst on a vehicle.");
                if (counter != 1.0f) {
                    MethodsFishing.sendSpearStop(performer, act);
                }
                return true;
            }
        } else {
            startCommand = 0;
            boolean failed = false;
            if (source.getTemplateId() == 1344) {
                if (act.getCounterAsFloat() < act.getFailSecond()) {
                    if (fishingLine == null || fishingHook == null) {
                        performer.getCommunicator().sendNormalServerMessage("Fishing pole needs a line with a fishing hook to be able to catch fish.");
                        failed = true;
                    }
                    if (fishingFloat == null) {
                        performer.getCommunicator().sendNormalServerMessage("Fishing pole needs a float for you to be able to see when a fish can be hooked.");
                        failed = true;
                    }
                }
                if (failed) {
                    act.setFailSecond(act.getCounterAsFloat());
                }
            } else {
                if (act.getCounterAsFloat() < act.getFailSecond()) {
                    if (fishingReel == null || fishingLine == null || fishingHook == null) {
                        performer.getCommunicator().sendNormalServerMessage("Fishing rod needs a reel, line and a fishing hook to be able to catch fish.");
                        failed = true;
                    }
                    if (fishingFloat == null) {
                        performer.getCommunicator().sendNormalServerMessage("Fishing rod needs a float for you to be able to see when a fish can be hooked.");
                        failed = true;
                    }
                }
                if (failed) {
                    act.setFailSecond(act.getCounterAsFloat());
                }
            }
            if (failed) {
                if (counter != 1.0f && act.getCreature() != null) {
                    byte currentPhase = (byte)act.getTickCount();
                    if (currentPhase != 15) {
                        MethodsFishing.processFishMovedOn(performer, act, 2.2f);
                        act.setTickCount(15);
                    }
                } else {
                    MethodsFishing.sendFishStop(performer, act);
                    return true;
                }
            }
        }
        if (counter == 1.0f) {
            if (MethodsFishing.fishingTestsFailed(performer, act.getPosX(), act.getPosY())) {
                return true;
            }
            switch (startCommand) {
                case 40: {
                    if (!source.isEmpty(false)) {
                        performer.getCommunicator().sendNormalServerMessage("The net is too unwieldly for you to cast, please empty it first!");
                        return true;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You throw out the net and start slowly pulling it in, hoping to catch some small fish.");
                    Server.getInstance().broadCastAction(performer.getName() + " throws out a net and starts fishing.", performer, 5);
                    timeleft = 50 + Server.rand.nextInt(250);
                    defaultTimer = 600;
                    act.setTickCount(40);
                    break;
                }
                case 20: {
                    performer.getCommunicator().sendNormalServerMessage("You stand still to wait for a passing fish, are they like buses?");
                    Server.getInstance().broadCastAction(performer.getName() + " stands still looking at the water.", performer, 5);
                    performer.getCommunicator().sendSpearStart();
                    timeleft = 100 + Server.rand.nextInt(250) - source.getRarity() * 10;
                    act.setTickCount(21);
                    break;
                }
                case 0: {
                    String ss = performer.getVehicle() == -10L ? "stand" : "sit";
                    performer.getCommunicator().sendNormalServerMessage("You " + ss + " still and get ready to cast.");
                    Server.getInstance().broadCastAction(performer.getName() + " gets ready to cast.", performer, 5);
                    float[] radi = MethodsFishing.getMinMaxRadius(source, fishingLine);
                    float minRadius = radi[0];
                    float maxRadius = radi[1];
                    byte rodType = MethodsFishing.getRodType(source, fishingReel, fishingLine);
                    byte rodMaterial = source.getMaterial();
                    FishEnums.ReelType reelType = FishEnums.ReelType.fromItem(fishingReel);
                    byte reelMaterial = fishingReel == null ? (byte)0 : fishingReel.getMaterial();
                    FishEnums.FloatType floatType = FishEnums.FloatType.fromItem(fishingFloat);
                    FishEnums.HookType hookType = FishEnums.HookType.fromItem(fishingHook);
                    byte hookMaterial = fishingHook.getMaterial();
                    FishEnums.BaitType baitType = FishEnums.BaitType.fromItem(fishingBait);
                    performer.getCommunicator().sendFishStart(minRadius, maxRadius, rodType, rodMaterial, reelType.getTypeId(), reelMaterial, floatType.getTypeId(), baitType.getTypeId());
                    MethodsFishing.remember(source, reelType, reelMaterial, floatType, hookType, hookMaterial, baitType);
                    defaultTimer = timeleft = 300;
                    act.setTickCount(0);
                    break;
                }
            }
            act.setTimeLeft(defaultTimer);
            act.setData(timeleft);
            performer.sendActionControl(Actions.actionEntrys[160].getVerbString(), true, defaultTimer);
            return false;
        }
        byte currentPhase = (byte)act.getTickCount();
        if (MethodsFishing.canTimeOut(act) && act.getSecond() * 10 > act.getTimeLeft()) {
            switch (startCommand) {
                case 40: {
                    performer.getCommunicator().sendNormalServerMessage("You finish pulling in the net.");
                    break;
                }
                case 20: {
                    performer.getCommunicator().sendNormalServerMessage("You speared nothing!");
                    MethodsFishing.sendSpearStop(performer, act);
                    break;
                }
                case 0: {
                    switch (currentPhase) {
                        case 0: {
                            performer.getCommunicator().sendNormalServerMessage("You never cast your line, so caught nothing.");
                            return MethodsFishing.sendFishStop(performer, act);
                        }
                    }
                    performer.getCommunicator().sendNormalServerMessage("You did not catch anything!");
                    MethodsFishing.sendFishStop(performer, act);
                }
            }
            return true;
        }
        if (currentPhase == 17) {
            Creature fish = act.getCreature();
            if (performer.isWithinDistanceTo(fish, 1.0f)) {
                act.setTickCount(12);
            } else if (!performer.isWithinDistanceTo(fish, 10.0f)) {
                act.setTickCount(13);
            }
        }
        timeleft = (int)act.getData();
        if (act.getSecond() * 10 > timeleft || MethodsFishing.isInstant(currentPhase)) {
            switch (startCommand) {
                case 40: {
                    if (!MethodsFishing.processNetPhase(performer, currentPhase, source, act, tilex, tiley, fishing)) break;
                    return true;
                }
                case 20: {
                    if (!MethodsFishing.processSpearPhase(performer, currentPhase, source, act, fishing)) break;
                    return true;
                }
                case 0: {
                    if (!MethodsFishing.processFishPhase(performer, currentPhase, source, act, fishing)) break;
                    return true;
                }
            }
        }
        if (act.justTickedSecond()) {
            if (act.getSecond() % 2 == 0 && MethodsFishing.fishingTestsFailed(performer, act.getPosX(), act.getPosY())) {
                switch (startCommand) {
                    case 40: {
                        return true;
                    }
                    case 20: {
                        MethodsFishing.sendSpearStop(performer, act);
                        return true;
                    }
                    case 0: {
                        MethodsFishing.sendFishStop(performer, act);
                        return true;
                    }
                }
                return true;
            }
            Creature fish = act.getCreature();
            if (fish != null && startCommand == 20) {
                FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
                if (fish.getPosX() == faid.getTargetPosX() && fish.getPosY() == faid.getTargetPosY()) {
                    MethodsFishing.testMessage(performer, "Fish out of range? Swam away!", "");
                    act.setTickCount(28);
                }
            }
        }
        return false;
    }

    private static boolean isInstant(byte currentPhase) {
        switch (currentPhase) {
            case 2: 
            case 3: 
            case 5: 
            case 6: 
            case 7: 
            case 9: 
            case 10: 
            case 13: 
            case 22: 
            case 23: 
            case 24: 
            case 27: 
            case 28: {
                return true;
            }
        }
        return false;
    }

    private static boolean canTimeOut(Action act) {
        Creature fish = act.getCreature();
        return fish == null;
    }

    private static void remember(Item rod, FishEnums.ReelType reelType, byte reelMaterial, FishEnums.FloatType floatType, FishEnums.HookType hookType, byte hookMaterial, FishEnums.BaitType baitType) {
        int types = (reelType.getTypeId() << 12) + (floatType.getTypeId() << 8) + (hookType.getTypeId() << 4) + baitType.getTypeId();
        int materials = (reelMaterial << 8) + hookMaterial;
        rod.setData(types, materials);
    }

    public static void playerOutOfRange(Creature performer, Action act) {
        Item source = act.getSubject();
        int stid = source == null ? 0 : source.getTemplateId();
        switch (stid) {
            case 705: 
            case 707: {
                MethodsFishing.sendSpearStop(performer, act);
                break;
            }
            case 1344: 
            case 1346: {
                MethodsFishing.sendFishStop(performer, act);
            }
        }
    }

    private static boolean processNetPhase(Creature performer, byte currentPhase, Item net, Action act, int tilex, int tiley, Skill fishing) {
        float posx = (tilex << 2) + 2;
        float posy = (tiley << 2) + 2;
        FishRow fr = MethodsFishing.caughtFish(performer, posx, posy, net);
        if (fr != null) {
            int weight = MethodsFishing.makeDeadFish(performer, act, fishing, fr.getFishTypeId(), net, net);
            FishEnums.FishData fd = FishEnums.FishData.fromInt(fr.getFishTypeId());
            float damMod = (float)fd.getDamageMod() * Math.max(0.1f, (float)weight / 3000.0f);
            float additionalDamage = MethodsFishing.additionalDamage(net, damMod * 10.0f, false);
            if (additionalDamage > 0.0f) {
                float newDam = net.getDamage() + additionalDamage;
                if (newDam >= 100.0f && !net.isEmpty(false)) {
                    performer.getCommunicator().sendNormalServerMessage("As the " + net.getName() + " disintegrates, the fish inside all swim away");
                    MethodsFishing.destroyContents(net);
                }
                net.setDamage(newDam);
            }
        }
        int moreTime = 50 + Server.rand.nextInt(250);
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        return false;
    }

    private static boolean processSpearPhase(Creature performer, byte currentPhase, Item spear, Action act, Skill fishing) {
        switch (currentPhase) {
            case 27: {
                return MethodsFishing.processSpearCancel(performer, act);
            }
            case 21: {
                return MethodsFishing.processSpearMove(performer, act, spear, fishing);
            }
            case 28: {
                return MethodsFishing.processSpearSwamAway(performer, act, spear, 50);
            }
            case 24: {
                MethodsFishing.testMessage(performer, "You launch the spear at nothing!", "");
                act.setTickCount(21);
                return false;
            }
            case 23: {
                return MethodsFishing.processSpearMissed(performer, act, spear);
            }
            case 22: {
                return MethodsFishing.processSpearHit(performer, act, fishing, spear);
            }
            case 26: {
                Creature fish = act.getCreature();
                MethodsFishing.testMessage(performer, "You launch the spear at " + fish.getName() + ".", " @fx:" + (int)fish.getPosX() + " fy:" + (int)fish.getPosY());
                performer.getCommunicator().sendSpearStrike(fish.getPosX(), fish.getPosY());
                return MethodsFishing.processSpearStrike(performer, act, fishing, spear, fish.getPosX(), fish.getPosY());
            }
        }
        return false;
    }

    private static boolean processFishPhase(Creature performer, byte currentPhase, Item rod, Action act, Skill fishing) {
        switch (currentPhase) {
            case 10: {
                return MethodsFishing.processFishCancel(performer, act);
            }
            case 1: {
                return MethodsFishing.processFishBite(performer, act, rod, fishing);
            }
            case 18: {
                return MethodsFishing.processFishPause(performer, act);
            }
            case 16: {
                if (MethodsFishing.processFishMove(performer, act, fishing, rod)) {
                    MethodsFishing.sendFishStop(performer, act);
                    return true;
                }
                return false;
            }
            case 2: {
                return MethodsFishing.processFishMovedOn(performer, act);
            }
            case 19: {
                return MethodsFishing.processFishMovingOn(performer, act);
            }
            case 14: {
                return MethodsFishing.processFishSwamAway(performer, act, rod, 50);
            }
            case 3: {
                return MethodsFishing.processFishHooked(performer, act, fishing, rod);
            }
            case 11: {
                return MethodsFishing.processFishStrike(performer, act, fishing, rod);
            }
            case 17: {
                if (MethodsFishing.processFishPull(performer, act, fishing, rod, false)) {
                    Creature fish = act.getCreature();
                    String fname = fish == null ? "fish" : fish.getName();
                    performer.getCommunicator().sendNormalServerMessage("The " + fname + " swims off.");
                    MethodsFishing.sendFishStop(performer, act);
                    return true;
                }
                return false;
            }
            case 12: {
                return MethodsFishing.processFishCaught(performer, act, fishing, rod);
            }
            case 4: {
                return MethodsFishing.sendFishStop(performer, act);
            }
            case 5: {
                performer.getCommunicator().sendNormalServerMessage("You hooked nothing while reeling in your line.");
                return MethodsFishing.sendFishStop(performer, act);
            }
            case 13: {
                Creature fish = act.getCreature();
                String fname = fish == null ? "fish" : fish.getName();
                performer.getCommunicator().sendNormalServerMessage("The " + fname + " managed to jump the fishing hook!");
                return MethodsFishing.sendFishStop(performer, act);
            }
            case 6: 
            case 7: {
                return MethodsFishing.processFishLineSnapped(performer, act, rod);
            }
            case 15: {
                return MethodsFishing.sendFishStop(performer, act);
            }
        }
        return false;
    }

    private static boolean fishingTestsFailed(Creature performer, float posx, float posy) {
        int tilex = (int)posx >> 2;
        int tiley = (int)posy >> 2;
        if (!GeneralUtilities.isValidTileLocation(tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("A huge shadow moves beneath the waves, and you reel in the line in panic.");
            return true;
        }
        if (performer.getInventory().getNumItemsNotCoins() >= 100) {
            performer.getCommunicator().sendNormalServerMessage("You wouldn't be able to carry the fish. Drop something first.");
            return true;
        }
        if (!performer.canCarry(1000)) {
            performer.getCommunicator().sendNormalServerMessage("You are too heavily loaded. Drop something first.");
            return true;
        }
        int depth = FishEnums.getWaterDepth(performer.getPosX(), performer.getPosY(), performer.isOnSurface());
        if (performer.getBridgeId() == -10L && depth > 10 && performer.getVehicle() == -10L) {
            performer.getCommunicator().sendNormalServerMessage("You can't swim and fish at the same time.");
            return true;
        }
        return false;
    }

    static boolean destroyFishCreature(Action act) {
        Creature fish = act.getCreature();
        act.setCreature(null);
        if (fish != null) {
            fish.destroy();
        }
        return true;
    }

    private static boolean isFishSpotValid(Point point) {
        if (WaterType.getWaterType(Zones.safeTileX(point.getX()), Zones.safeTileY(point.getY()), true) == 4) {
            float ht = 0.0f;
            try {
                ht = Zones.calculateHeight(point.getX(), point.getY(), true) * 10.0f;
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            if (ht < -100.0f) {
                return true;
            }
        }
        return false;
    }

    public static Point[] getSpecialSpots(int tilex, int tiley, int season) {
        ArrayList<Point> specialSpots = new ArrayList<Point>();
        int zoneX = tilex / 128;
        int zoneY = tiley / 128;
        for (FishEnums.FishData fd : FishEnums.FishData.values()) {
            if (!fd.isSpecialFish()) continue;
            Point tp = fd.getSpecialSpot(zoneX, zoneY, season);
            if ((tilex != 0 || tiley != 0) && !MethodsFishing.isFishSpotValid(tp)) continue;
            specialSpots.add(tp);
        }
        return specialSpots.toArray(new Point[specialSpots.size()]);
    }

    public static FishRow[] getFishTable(Creature performer, float posX, float posY, Item rod) {
        Skill fishing = performer.getSkills().getSkillOrLearn(10033);
        float knowledge = (float)fishing.getKnowledge();
        Item[] fishingItems = rod.getFishingItems();
        Item fishingReel = fishingItems[0];
        Item fishingLine = fishingItems[1];
        Item fishingFloat = fishingItems[2];
        Item fishingHook = fishingItems[3];
        Item fishingBait = fishingItems[4];
        float totalChances = 0.0f;
        FishRow[] fishTable = new FishRow[FishEnums.FishData.getLength()];
        for (FishEnums.FishData fd : FishEnums.FishData.values()) {
            fishTable[fd.getTypeId()] = new FishRow(fd.getTypeId(), fd.getName());
            if (fd.getTypeId() == FishEnums.FishData.NONE.getTypeId() || fd.getTypeId() == FishEnums.FishData.CLAM.getTypeId()) continue;
            float fishChance = fd.getChance(knowledge, rod, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait, posX, posY, performer.isOnSurface());
            fishTable[fd.getTypeId()].setChance(fishChance);
            totalChances += fishChance;
        }
        if (totalChances > 99.0f) {
            float percentageFactor = totalChances / 99.0f;
            totalChances = 0.0f;
            for (FishEnums.FishData fd : FishEnums.FishData.values()) {
                float chance = fishTable[fd.getTypeId()].getChance();
                if (chance > 0.0f) {
                    fishTable[fd.getTypeId()].setChance(chance / percentageFactor);
                }
                totalChances += fishTable[fd.getTypeId()].getChance();
            }
        }
        if (totalChances < 100.0f) {
            fishTable[FishEnums.FishData.CLAM.getTypeId()].setChance(Math.min(2.0f, 100.0f - totalChances));
        }
        return fishTable;
    }

    public static boolean showFishTable(Creature performer, Item source, int tileX, int tileY, float counter, Action act) {
        int time = act.getTimeLeft();
        if (counter == 1.0f) {
            time = 250;
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start looking around for what fish might be in this area.");
            performer.sendActionControl(act.getActionString(), true, time);
        } else if (act.justTickedSecond() && act.getSecond() == 5) {
            Item[] fishingItems;
            if (source.getTemplateId() == 1344) {
                fishingItems = source.getFishingItems();
                if (fishingItems[1] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your pole is missing a line, float and fishing hook!");
                    return true;
                }
                if (fishingItems[2] == null) {
                    if (fishingItems[3] == null) {
                        performer.getCommunicator().sendNormalServerMessage("Your pole is missing a float and fishing hook!");
                        return true;
                    }
                    performer.getCommunicator().sendNormalServerMessage("Your pole is missing a float!");
                    return true;
                }
                if (fishingItems[3] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your pole is missing a fishing hook!");
                    return true;
                }
                if (fishingItems[4] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your pole looks all set, but you think catching something could be easier using bait.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Your pole looks all set.");
                }
            } else if (source.getTemplateId() == 1346) {
                fishingItems = source.getFishingItems();
                if (fishingItems[0] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your rod is missing a reel, line, float and fishing hook!");
                    return true;
                }
                if (fishingItems[1] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your rod is missing a line, float and fishing hook!");
                    return true;
                }
                if (fishingItems[2] == null) {
                    if (fishingItems[3] == null) {
                        performer.getCommunicator().sendNormalServerMessage("Your rod is missing a float and fishing hook!");
                        return true;
                    }
                    performer.getCommunicator().sendNormalServerMessage("Your rod is missing a float!");
                    return true;
                }
                if (fishingItems[3] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your rod is missing a fishing hook!");
                    return true;
                }
                if (fishingItems[4] == null) {
                    performer.getCommunicator().sendNormalServerMessage("Your rod looks all set, but you think catching something could be easier using bait.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Your rod looks all set.");
                }
            }
        }
        double fishingSkill = performer.getSkills().getSkillOrLearn(10033).getKnowledge(0.0);
        float posx = (tileX << 2) + 2;
        float posy = (tileY << 2) + 2;
        String waterType = WaterType.getWaterTypeString(tileX, tileY, performer.isOnSurface()).toLowerCase();
        int waterDepth = FishEnums.getWaterDepth(posx, posy, performer.isOnSurface());
        if (act.justTickedSecond() && act.getSecond() == 10) {
            if (fishingSkill < 10.0) {
                performer.getCommunicator().sendNormalServerMessage("You're not too sure what type of water you're standing in, perhaps with a bit more fishing knowledge you'll understand better.");
            } else if (fishingSkill < 40.0) {
                performer.getCommunicator().sendNormalServerMessage("You believe this area of water to be " + waterType + ".");
            } else {
                performer.getCommunicator().sendNormalServerMessage("You believe this area of water to be " + waterType + " around a depth of " + waterDepth / 10 + "m.");
            }
        } else if (act.justTickedSecond() && act.getSecond() == 15) {
            FishRow[] fishChances = MethodsFishing.getFishTable(performer, posx, posy, source);
            FishRow topChance = null;
            for (FishRow fishChance : fishChances) {
                float chance;
                if (FishEnums.FishData.fromName(fishChance.getName()).isSpecialFish() || !((chance = fishChance.getChance()) > 0.0f) || topChance != null && !(topChance.getChance() < chance)) continue;
                topChance = fishChance;
            }
            if (topChance == null) {
                performer.getCommunicator().sendNormalServerMessage("You can't find anything that you'll catch in this area with the " + source.getName() + ".");
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage("The most common fish around here that you think you might catch with the " + source.getName() + " is a " + topChance.getName() + ".");
        } else if (act.justTickedSecond() && act.getSecond() == 25) {
            FishRow[] fishChances = MethodsFishing.getFishTable(performer, posx, posy, source);
            FishRow topChance = null;
            ArrayList<FishRow> otherChances = new ArrayList<FishRow>();
            for (FishRow fishChance : fishChances) {
                float chance;
                if (FishEnums.FishData.fromName(fishChance.getName()).isSpecialFish() || !((chance = fishChance.getChance()) > 0.0f)) continue;
                if (topChance == null || topChance.getChance() < chance) {
                    topChance = fishChance;
                }
                if (!((double)chance > (100.0 - fishingSkill) / 5.0 + 5.0)) continue;
                otherChances.add(fishChance);
            }
            otherChances.remove(topChance);
            if (otherChances.isEmpty() || fishingSkill < 20.0) {
                performer.getCommunicator().sendNormalServerMessage("You're not sure of what other fish you might find here.");
            } else {
                String allOthers = "";
                for (int i = 0; i < otherChances.size(); ++i) {
                    if (i > 0 && i == otherChances.size() - 1) {
                        allOthers = allOthers + " and ";
                    } else if (i > 0) {
                        allOthers = allOthers + ", ";
                    }
                    allOthers = allOthers + ((FishRow)otherChances.get(i)).getName();
                }
                performer.getCommunicator().sendNormalServerMessage("You also think the " + source.getName() + " will be useful to catch " + allOthers + " in this area.");
            }
            return true;
        }
        return false;
    }

    @Nullable
    private static FishRow caughtFish(Creature performer, float posX, float posY, Item rod) {
        FishRow[] fishChances = MethodsFishing.getFishTable(performer, posX, posY, rod);
        float rno = Server.rand.nextFloat() * 100.0f;
        float runningTotal = 0.0f;
        for (FishRow fishChance : fishChances) {
            if (!(fishChance.getChance() > 0.0f) || !((runningTotal += fishChance.getChance()) > rno)) continue;
            return fishChance;
        }
        return null;
    }

    public static boolean processSpearStrike(Creature performer, Action act, Skill fishing, Item spear, float posX, float posY) {
        Creature fish = act.getCreature();
        if (fish == null) {
            act.setTickCount(24);
            return false;
        }
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        float dx = Math.abs(fish.getStatus().getPositionX() - posX);
        float dy = Math.abs(fish.getStatus().getPositionY() - posY);
        float dd = (float)Math.sqrt(dx * dx + dy * dy);
        MethodsFishing.testMessage(performer, "Distance away was " + dd + " fish:" + fish.getStatus().getPositionX() + "," + fish.getStatus().getPositionY() + " strike:" + posX + "," + posY, "");
        performer.sendSpearStrike(posX, posY);
        if (dd > 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You missed the " + faid.getNameWithSize() + "!");
            Server.getInstance().broadCastAction(performer.getName() + " attempted to spear a fish and failed!", performer, 5);
            act.setTickCount(23);
            return false;
        }
        float result = MethodsFishing.getDifficulty(performer, act, performer.getPosX(), performer.getPosY(), fishing, spear, null, null, null, null, null, 0.0f, 100.0f);
        if (result <= 0.0f) {
            if (result < -80.0f && performer.isWithinDistanceTo(posX, posY, 0.0f, 2.0f)) {
                Skill bodyStr = performer.getSkills().getSkillOrLearn(102);
                int foot = Server.rand.nextBoolean() ? 15 : 16;
                performer.getCommunicator().sendNormalServerMessage("You miss the " + fish.getName() + " and hit your own foot!");
                Server.getInstance().broadCastAction(performer.getName() + " spears their own foot.. how silly!", performer, 5);
                performer.addWoundOfType(null, (byte)2, foot, false, 1.0f, true, 400.0 * bodyStr.getKnowledge(0.0), 0.0f, 0.0f, false, false);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You missed the " + fish.getName() + "!");
                Server.getInstance().broadCastAction(performer.getName() + " attempted to spear a fish and failed!", performer, 5);
            }
            act.setTickCount(23);
        } else {
            act.setTickCount(22);
            performer.getCommunicator().sendNormalServerMessage("You managed to spear the " + fish.getName() + "!");
            Server.getInstance().broadCastAction(performer.getName() + " managed to spear a fish!", performer, 5);
            performer.achievement(559);
        }
        return false;
    }

    public static boolean processFishStrike(Creature performer, Action act, Skill fishing, Item rod) {
        Creature fish = act.getCreature();
        if (fish == null) {
            act.setTickCount(5);
            return false;
        }
        if (rod == null) {
            act.setTickCount(7);
            return false;
        }
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        String extra = "";
        if (act.getTickCount() != 18) {
            if (act.getTickCount() != 4 && act.getTickCount() != 2 && act.getTickCount() != 19) {
                if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
                    extra = " (Test only) Cmd:" + MethodsFishing.fromCommand((byte)act.getTickCount());
                }
                performer.getCommunicator().sendNormalServerMessage("You scare off the " + faid.getNameWithSize() + " before it starts feeding." + extra);
                MethodsFishing.processFishMovedOn(performer, act, 2.0f);
                act.setTickCount(4);
                performer.getCommunicator().sendFishSubCommand((byte)4, -1L);
            }
            return false;
        }
        Item[] fishingItems = rod.getFishingItems();
        Item fishingReel = fishingItems[0];
        Item fishingLine = fishingItems[1];
        Item fishingFloat = fishingItems[2];
        Item fishingHook = fishingItems[3];
        Item fishingBait = fishingItems[4];
        float result = MethodsFishing.getDifficulty(performer, act, performer.getPosX(), performer.getPosY(), fishing, rod, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait, 0.0f, 40.0f);
        if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
            extra = " (Test only) Res:" + result;
        }
        act.setTickCount(3);
        return false;
    }

    private static float getDifficulty(Creature performer, Action act, float posx, float posy, Skill fishing, Item source, Item reel, Item line, Item fishingFloat, Item hook, Item bait, float bonus, float times) {
        Creature fish = act.getCreature();
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        float difficulty = faid.getDifficulty();
        if (difficulty == -10.0f) {
            FishEnums.FishData fd = FishEnums.FishData.fromInt(faid.getFishTypeId());
            float knowledge = (float)fishing.getKnowledge();
            difficulty = fd.getDifficulty(knowledge, posx, posy, performer.isOnSurface(), source, reel, line, fishingFloat, hook, bait);
            faid.setDifficulty(difficulty);
            MethodsFishing.testMessage(performer, "", fd.getName() + " Dif:" + difficulty);
        }
        double result = fishing.skillCheck(difficulty, source, bonus, false, times);
        return (float)result;
    }

    private static boolean processSpearCancel(Creature performer, Action act) {
        performer.getCommunicator().sendNormalServerMessage("You have cancelled your spearing!");
        return MethodsFishing.sendSpearStop(performer, act);
    }

    private static boolean processSpearMove(Creature performer, Action act, Item spear, Skill fishing) {
        if (act.getCreature() == null) {
            if (MethodsFishing.makeFish(performer, act, spear, fishing, (byte)20)) {
                return true;
            }
            act.setTickCount(21);
            return false;
        }
        act.setTickCount(28);
        return false;
    }

    private static boolean processSpearSwamAway(Creature performer, Action act, Item spear, int delay) {
        performer.getCommunicator().sendNormalServerMessage("The " + act.getCreature().getName() + " swims off.");
        MethodsFishing.destroyFishCreature(act);
        int moreTime = delay + Server.rand.nextInt(250) - spear.getRarity() * 10;
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        act.setTickCount(21);
        return false;
    }

    private static boolean processSpearMissed(Creature performer, Action act, Item spear) {
        Creature fish = act.getCreature();
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        faid.setRaceAway(true);
        int moreTime = (int)(faid.getTimeToTarget() / 2.0f);
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        act.setTickCount(21);
        FishEnums.FishData fd = faid.getFishData();
        float fdam = fd.getDamageMod();
        float damMod = fdam * Math.max(0.1f, (float)(faid.getWeight() / 3000));
        float additionalDamage = MethodsFishing.additionalDamage(spear, damMod * 10.0f, true);
        if (additionalDamage > 0.0f) {
            return spear.setDamage(spear.getDamage() + additionalDamage);
        }
        return false;
    }

    private static boolean processSpearHit(Creature performer, Action act, Skill fishing, Item spear) {
        Creature fish = act.getCreature();
        if (fish == null) {
            return true;
        }
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        byte fishTypeId = faid.getFishTypeId();
        performer.getCommunicator().sendSpearHit(fishTypeId, fish.getWurmId());
        MethodsFishing.makeDeadFish(performer, act, fishing, fishTypeId, spear, performer.getInventory());
        MethodsFishing.destroyFishCreature(act);
        FishEnums.FishData fd = faid.getFishData();
        float fdam = fd.getDamageMod();
        float damMod = fdam * Math.max(0.1f, (float)(faid.getWeight() / 3000));
        float additionalDamage = MethodsFishing.additionalDamage(spear, damMod * 8.0f, true);
        if (additionalDamage > 0.0f) {
            return spear.setDamage(spear.getDamage() + additionalDamage);
        }
        return false;
    }

    private static boolean sendSpearStop(Creature performer, Action act) {
        MethodsFishing.destroyFishCreature(act);
        performer.getCommunicator().sendFishSubCommand((byte)29, -1L);
        return true;
    }

    private static boolean makeFish(Creature performer, Action act, Item source, Skill fishing, byte startCmd) {
        Point4f end;
        Point4f start;
        Point4f mid;
        float angle;
        FishRow fr = MethodsFishing.caughtFish(performer, act.getPosX(), act.getPosY(), source);
        if (fr == null) {
            int moreTime = 100 + Server.rand.nextInt(250);
            int timeleft = act.getSecond() * 10 + moreTime;
            act.setData(timeleft);
            MethodsFishing.testMessage(performer, "No Fish!", " Next attempt in approx:" + moreTime / 10 + "s");
            return false;
        }
        float speedMod = 1.0f;
        float rot = performer.getStatus().getRotation();
        if (performer.getVehicle() == -10L) {
            int ang = Server.rand.nextInt(40);
            angle = Server.rand.nextBoolean() ? Creature.normalizeAngle(rot - 130.0f + (float)ang) : Creature.normalizeAngle(rot + 130.0f - (float)ang);
        } else {
            angle = Server.rand.nextInt(360);
        }
        if (startCmd == 20) {
            float dist = 0.05f + Server.rand.nextFloat();
            mid = MethodsFishing.calcSpot(act.getPosX(), act.getPosY(), performer.getStatus().getRotation(), dist);
            start = MethodsFishing.calcSpot(mid.getPosX(), mid.getPosY(), angle, 10.0f);
            end = MethodsFishing.calcSpot(start.getPosX(), start.getPosY(), start.getRot(), 20.0f);
            try {
                if (Zones.calculateHeight(start.getPosX(), start.getPosY(), performer.isOnSurface()) > 0.0f) {
                    Point4f temp = start;
                    start = end;
                    end = temp;
                }
            }
            catch (NoSuchZoneException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            speedMod = 0.5f;
        } else {
            mid = new Point4f(act.getPosX(), act.getPosY(), 0.0f, Creature.normalizeAngle(rot + 180.0f));
            start = MethodsFishing.calcSpot(mid.getPosX(), mid.getPosY(), angle, 10.0f);
            end = new Point4f(mid.getPosX(), mid.getPosY(), 0.0f, start.getRot());
            try {
                if (Zones.calculateHeight(start.getPosX(), start.getPosY(), performer.isOnSurface()) > 0.0f) {
                    Point4f temp = start;
                    start = end;
                    end = temp;
                }
            }
            catch (NoSuchZoneException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            speedMod = 1.2f;
        }
        double ql = MethodsFishing.getQL(performer, source, fishing, fr.getFishTypeId(), act.getPosX(), act.getPosY(), true);
        Creature fish = MethodsFishing.makeFishCreature(performer, start, fr.getName(), ql, fr.getFishTypeId(), speedMod, end);
        if (fish == null) {
            performer.getCommunicator().sendNormalServerMessage("You jump as you see a ghost of a fish, and stop fishing.");
            return true;
        }
        act.setCreature(fish);
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        int moreTime = (int)faid.getTimeToTarget();
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
            if (performer.getPower() >= 2) {
                MethodsFishing.testMessage(performer, "", performer.getName() + " @px:" + (int)performer.getPosX() + ",py:" + (int)performer.getPosY() + ",pr:" + (int)performer.getStatus().getRotation());
            }
            if (startCmd == 20) {
                MethodsFishing.addMarker(performer, fish.getName() + " mid", mid);
            }
            MethodsFishing.addMarker(performer, fish.getName() + " start", start);
            MethodsFishing.addMarker(performer, fish.getName() + " end", end);
        }
        return false;
    }

    @Nullable
    public static Creature makeFishCreature(Creature performer, Point4f start, String fishName, double ql, byte fishTypeId, float speedMod, Point4f end) {
        try {
            Creature fish = Creature.doNew(119, start.getPosX(), start.getPosY(), start.getRot(), performer.getLayer(), fishName, Server.rand.nextBoolean() ? (byte)0 : 1, (byte)0);
            fish.setVisible(false);
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            faid.setFishTypeId(fishTypeId);
            faid.setQL(ql);
            faid.setMovementSpeedModifier(speedMod);
            faid.setTargetPos(end.getPosX(), end.getPosY());
            fish.setVisible(true);
            return fish;
        }
        catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    public static void addMarker(Creature performer, String string, Point4f loc) {
        if (!Servers.isThisATestServer()) {
            return;
        }
        String sloc = "";
        if (performer.getPower() >= 2) {
            int depth = FishEnums.getWaterDepth(loc.getPosX(), loc.getPosY(), performer.isOnSurface());
            sloc = " @x:" + (int)loc.getPosX() + ", y:" + (int)loc.getPosY() + ", r:" + (int)loc.getRot() + ", d:" + depth;
            MethodsFishing.testMessage(performer, string, sloc);
        }
        if (performer.getPower() >= 5 && performer.hasFlag(51)) {
            VolaTile vtile = Zones.getOrCreateTile(loc.getTileX(), loc.getTileY(), performer.isOnSurface());
            try {
                Item marker = ItemFactory.createItem(344, 1.0f, null);
                marker.setSizes(10, 10, 100);
                marker.setPosXY(loc.getPosX(), loc.getPosY());
                marker.setRotation(loc.getRot());
                marker.setDescription(string + sloc);
                vtile.addItem(marker, false, false);
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public static Point4f calcSpot(float posx, float posy, float rot, float dist) {
        float r = rot * (float)Math.PI / 180.0f;
        float s = (float)Math.sin(r);
        float c = (float)Math.cos(r);
        float xo = s * dist;
        float yo = c * -dist;
        float newx = posx + xo;
        float newy = posy + yo;
        float angle = rot + 180.0f;
        return new Point4f(newx, newy, 0.0f, Creature.normalizeAngle(angle));
    }

    private static int makeDeadFish(Creature performer, Action act, Skill fishing, byte fishTypeId, Item source, Item container) {
        int weight;
        double ql;
        FishEnums.FishData fd;
        Creature fish = act.getCreature();
        if (fish == null) {
            if (source.getTemplateId() != 1343) {
                return 0;
            }
            fd = FishEnums.FishData.fromInt(fishTypeId);
            ql = MethodsFishing.getQL(performer, source, fishing, fishTypeId, act.getPosX(), act.getPosY(), false);
            ItemTemplate it = fd.getTemplate();
            if (it == null) {
                MethodsFishing.testMessage(performer, "", fd.getName() + " no template!");
                return 0;
            }
            weight = (int)((double)it.getWeightGrams() * (ql / 100.0));
        } else {
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            fd = faid.getFishData();
            ql = faid.getQL();
            weight = faid.getWeight();
        }
        if (weight == 0) {
            performer.getCommunicator().sendNormalServerMessage("You fumbled when trying to move the fish to " + container.getName() + ", and it got away.");
            return 0;
        }
        MethodsFishing.testMessage(performer, "", fd.getTemplate().getName() + ":" + weight + "g");
        if (weight >= fd.getMinWeight()) {
            try {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                Item deadFish = ItemFactory.createItem(fd.getTemplateId(), (float)ql, (byte)2, act.getRarity(), performer.getName());
                deadFish.setSizes(weight);
                deadFish.setWeight(weight, false);
                boolean inserted = container.insertItem(deadFish);
                if (inserted) {
                    if (source.getTemplateId() == 1343) {
                        performer.getCommunicator().sendNormalServerMessage("You catch " + deadFish.getNameWithGenus() + " in the " + source.getName() + ".");
                    }
                    if (fd.getTypeId() == FishEnums.FishData.CLAM.getTypeId()) {
                        performer.getCommunicator().sendNormalServerMessage("You will need to pry open the clam with a knife.");
                    }
                    performer.achievement(126);
                    if (weight > 3000) {
                        performer.achievement(542);
                    }
                    if (weight > 10000) {
                        performer.achievement(585);
                    }
                    if (weight > 175000) {
                        performer.achievement(297);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You fumbled when trying to move the " + deadFish.getName() + " to " + container.getName() + ", and it got away.");
                    Items.destroyItem(deadFish.getWurmId());
                }
                act.setRarity(performer.getRarity());
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        } else {
            if (source.getTemplateId() == 1343) {
                performer.getCommunicator().sendNormalServerMessage("The " + fd.getTemplate().getName() + "  was so small, it swam through the net.");
            } else {
                performer.getCommunicator().sendNormalServerMessage("The " + fd.getTemplate().getName() + "  was so small, you threw it back in.");
                Server.getInstance().broadCastAction(performer.getName() + " throws the tiddler " + fd.getTemplate().getName() + " back in.", performer, 5);
            }
            return 0;
        }
        return weight;
    }

    private static float additionalDamage(Item item, float damMod, boolean flexible) {
        if (item == null) {
            return 0.0f;
        }
        float typeMod = item.isWood() ? 2.0f : (item.isMetal() ? 1.0f : 0.5f);
        float newDam = typeMod * item.getDamageModifier(false, flexible) / (10.0f * Math.max(10.0f, item.getQualityLevel()));
        float qlMod = (100.0f - item.getCurrentQualityLevel()) / 50.0f;
        float extraDam = Math.min(qlMod, Math.max(0.1f, newDam) * damMod);
        return extraDam;
    }

    private static double getQL(Creature performer, Item source, Skill fishing, byte fishTypeId, float posx, float posy, boolean noSkill) {
        Item[] fishingItems = source.getFishingItems();
        Item fishingReel = fishingItems[0];
        Item fishingLine = fishingItems[1];
        Item fishingFloat = fishingItems[2];
        Item fishingHook = fishingItems[3];
        Item fishingBait = fishingItems[4];
        FishEnums.FishData fd = FishEnums.FishData.fromInt(fishTypeId);
        float knowledge = (float)fishing.getKnowledge();
        float difficulty = fd.getDifficulty(knowledge, posx, posy, performer.isOnSurface(), source, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait);
        double power = fishing.skillCheck(difficulty, null, 0.0, noSkill, 10.0f);
        double ql = 10.0 + 0.9 * Math.max((double)(Server.rand.nextFloat() * 10.0f), power);
        return ql;
    }

    private static boolean autoCast(Creature performer, Action act, Item rod) {
        Item[] fishingItems = rod.getFishingItems();
        Item line = fishingItems[1];
        float[] radi = MethodsFishing.getMinMaxRadius(rod, line);
        float minRadius = radi[0];
        float maxRadius = radi[1];
        float half = (maxRadius + minRadius) / 2.0f;
        float rot = performer.getStatus().getRotation();
        float r = rot * (float)Math.PI / 180.0f;
        float s = (float)Math.sin(r);
        float c = (float)Math.cos(r);
        float xo = s * half;
        float yo = c * -half;
        float castX = performer.getPosX() + xo;
        float castY = performer.getPosY() + yo;
        int tilex = (int)castX >> 2;
        int tiley = (int)castY >> 2;
        int tile = performer.isOnSurface() ? Server.surfaceMesh.getTile(tilex, tiley) : Server.caveMesh.getTile(tilex, tiley);
        if (!Terraforming.isTileUnderWater(tile, tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("The water is too shallow to fish.");
            MethodsFishing.testMessage(performer, "", performer.getTileX() + "=" + tilex + "," + performer.getTileY() + "=" + tiley);
            return true;
        }
        MethodsFishing.testMessage(performer, "Auto cast as you were too lazy to cast!", "");
        Server.getInstance().broadCastAction(performer.getName() + " casts and starts fishing.", performer, 5);
        act.setTickCount(9);
        return MethodsFishing.processFishCasted(performer, act, castX, castY, rod, false);
    }

    private static boolean processFishCasted(Creature performer, Action act, float castX, float castY, Item rod, boolean manualMode) {
        act.setPosX(castX);
        act.setPosY(castY);
        int defaultTimer = 1800;
        act.setTimeLeft(1800);
        performer.sendActionControl(Actions.actionEntrys[160].getVerbString(), true, 1800);
        Item[] fishingItems = rod.getFishingItems();
        Item floatItem = fishingItems[2];
        performer.sendFishingLine(castX, castY, FishEnums.FloatType.fromItem(floatItem).getTypeId());
        act.setTickCount(16);
        int moreTime = MethodsFishing.getNextFishDelay(rod, 100, 600);
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
            Point4f cast = new Point4f(castX, castY, 0.0f, 0.0f);
            MethodsFishing.addMarker(performer, (manualMode ? "Manual" : "Auto") + " cast (" + moreTime / 10 + "s)", cast);
        }
        return false;
    }

    private static boolean processFishMove(Creature performer, Action act, Skill fishing, Item rod) {
        if (act.getCreature() == null) {
            if (MethodsFishing.makeFish(performer, act, rod, fishing, (byte)0)) {
                return true;
            }
            if (act.getCreature() == null) {
                return false;
            }
            Creature fish = act.getCreature();
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            int moreTime = (int)faid.getTimeToTarget();
            int timeleft = act.getSecond() * 10 + moreTime;
            act.setData(timeleft);
            act.setTickCount(1);
            return false;
        }
        act.setTickCount(14);
        return false;
    }

    private static boolean processFishMovedOn(Creature performer, Action act) {
        performer.getCommunicator().sendFishSubCommand((byte)2, -1L);
        MethodsFishing.processFishMovedOn(performer, act, 1.2f);
        act.setTickCount(19);
        return false;
    }

    private static boolean processFishMovingOn(Creature performer, Action act) {
        act.setTickCount(16);
        return false;
    }

    private static boolean processFishMovedOn(Creature performer, Action act, float speed) {
        Creature fish = act.getCreature();
        MethodsFishing.testMessage(performer, fish.getName() + " Swims off.", "");
        performer.getCommunicator().sendNormalServerMessage("The " + fish.getName() + " swims off into the distance.");
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        Point4f end = MethodsFishing.calcSpot(fish.getPosX(), fish.getPosY(), performer.getStatus().getRotation(), 10.0f);
        faid.setMovementSpeedModifier(faid.getMovementSpeedModifier() * speed);
        faid.setTargetPos(end.getPosX(), end.getPosY());
        int moreTime = (int)faid.getTimeToTarget();
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        return false;
    }

    private static boolean processFishSwamAway(Creature performer, Action act, Item rod, int delay) {
        MethodsFishing.destroyFishCreature(act);
        int moreTime = MethodsFishing.getNextFishDelay(rod, delay, 200);
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        act.setTickCount(16);
        return false;
    }

    private static int getNextFishDelay(Item rod, int delay, int rnd) {
        Item[] fishingItems = rod.getFishingItems();
        Item bait = fishingItems[4];
        int bonus = bait == null ? 0 : bait.getRarity() * 10;
        Item hook = fishingItems[3];
        float fmod = hook == null ? 1.0f : hook.getMaterialFragrantModifier();
        return (int)((float)(delay + Server.rand.nextInt(rnd) - bonus) * fmod);
    }

    private static boolean processFishCancel(Creature performer, Action act) {
        performer.getCommunicator().sendNormalServerMessage("You have cancelled your fishing!");
        return MethodsFishing.sendFishStop(performer, act);
    }

    private static boolean processFishBite(Creature performer, Action act, Item rod, Skill fishing) {
        Item[] newFishingItems;
        Item newFishingBait;
        Creature fish = act.getCreature();
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
            String fpos = performer.getPower() >= 2 ? " fx:" + (int)fish.getPosX() + ",fy:" + (int)fish.getPosY() + " cx:" + (int)act.getPosX() + ",cy:" + (int)act.getPosY() : "";
            MethodsFishing.testMessage(performer, fish.getName() + " takes a bite!", fpos);
        }
        Item[] fishingItems = rod.getFishingItems();
        Item fishingLine = fishingItems[1];
        Item fishingFloat = fishingItems[2];
        Item fishingHook = fishingItems[3];
        Item fishingBait = fishingItems[4];
        FishEnums.FishData fd = faid.getFishData();
        float damMod = (float)fd.getDamageMod() * Math.max(0.1f, (float)(faid.getWeight() / 3000));
        float additionalDamage = MethodsFishing.additionalDamage(fishingFloat, damMod * 5.0f, false);
        float newDamage = fishingFloat.getDamage() + additionalDamage;
        if (newDamage > 100.0f) {
            // empty if block
        }
        if (additionalDamage > 0.0f) {
            fishingFloat.setDamage(newDamage);
        }
        additionalDamage = MethodsFishing.additionalDamage(fishingHook, damMod * 10.0f, false);
        newDamage = fishingHook.getDamage() + additionalDamage;
        if (newDamage > 100.0f) {
            MethodsFishing.destroyContents(fishingHook);
        }
        if (additionalDamage > 0.0f) {
            fishingHook.setDamage(newDamage);
        }
        if ((fishingHook = fishingLine.getFishingHook()) != null && (fishingBait = fishingHook.getFishingBait()) != null) {
            float dam = MethodsFishing.getBaitDamage(fd, fishingBait);
            if (fishingBait.setDamage(fishingBait.getDamage() + dam)) {
                // empty if block
            }
        }
        int bonus = (newFishingBait = (newFishingItems = rod.getFishingItems())[4]) == null ? 0 : newFishingBait.getRarity() * 10;
        int skillBonus = (int)(fishing.getKnowledge(0.0) / 3.0);
        int moreTime = 35 + skillBonus + Server.rand.nextInt(20) + bonus;
        int timeleft = act.getSecond() * 10 + moreTime;
        act.setData(timeleft);
        act.setTickCount(18);
        performer.getCommunicator().sendFishBite(faid.getFishTypeId(), fish.getWurmId(), -1L);
        performer.getCommunicator().sendNormalServerMessage("You feel something nibble on the line.");
        return false;
    }

    private static float getBaitDamage(FishEnums.FishData fd, Item bait) {
        float crumbles = FishEnums.BaitType.fromItem(bait).getCrumbleFactor();
        float dif = fd.getTemplateDifficulty();
        float base = dif + Server.rand.nextFloat() * 20.0f;
        float newDam = (base + 10.0f) / crumbles;
        return Math.max(20.0f, newDam);
    }

    private static float getRodDamageModifier(Item rod) {
        Item reel = rod.getFishingReel();
        if (reel == null) {
            return 5.0f;
        }
        switch (reel.getTemplateId()) {
            case 1372: {
                return 4.5f;
            }
            case 1373: {
                return 4.0f;
            }
            case 1374: {
                return 3.5f;
            }
            case 1375: {
                return 2.5f;
            }
        }
        return 5.0f;
    }

    private static float getReelDamageModifier(Item reel) {
        if (reel == null) {
            return 0.0f;
        }
        switch (reel.getTemplateId()) {
            case 1372: {
                return 0.1f;
            }
            case 1373: {
                return 0.07f;
            }
            case 1374: {
                return 0.05f;
            }
            case 1375: {
                return 0.02f;
            }
        }
        return 0.1f;
    }

    private static boolean autoReplace(Creature performer, int templateId, byte material, Item targetContainer) {
        Item[] contents;
        Item compartment;
        Item tacklebox = performer.getBestTackleBox();
        if (tacklebox != null && (compartment = MethodsFishing.getBoxCompartment(tacklebox, templateId)) != null && (contents = compartment.getItemsAsArray()).length > 0) {
            for (Item item : contents) {
                if (item.getTemplateId() != templateId || material != 0 && item.getMaterial() != material) continue;
                targetContainer.insertItem(item);
                item.sendUpdate();
                return true;
            }
        }
        return false;
    }

    private static boolean doAutoReplace(Creature performer, Action act) {
        Item hook;
        Item afloat;
        Item rod = act.getSubject();
        if (rod == null) {
            return true;
        }
        Skill fishing = performer.getSkills().getSkillOrLearn(10033);
        boolean hasTacklebox = performer.getBestTackleBox() != null;
        float knowledge = (float)fishing.getKnowledge(0.0);
        boolean replaced = false;
        Item reel = rod.getFishingReel();
        if (rod.getTemplateId() == 1346 && reel == null && hasTacklebox) {
            if (knowledge >= 90.0f) {
                FishEnums.ReelType reelType = FishEnums.ReelType.fromInt(rod.getData1() >> 12 & 0xF);
                byte reelMaterial = (byte)(rod.getData2() >> 8 & 0xFF);
                replaced = MethodsFishing.autoReplace(performer, reelType.getTemplateId(), reelMaterial, rod);
                if (replaced) {
                    reel = rod.getFishingReel();
                    performer.getCommunicator().sendNormalServerMessage("You managed to put another " + reel.getName() + " in the " + rod.getName() + "!");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("No replacement reel found!");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot remember what the reel was!");
            }
            if (!replaced) {
                performer.getCommunicator().sendNormalServerMessage("You are missing reel, line, float, fishing hook and bait!");
                return true;
            }
        }
        Item lineParent = rod.getTemplateId() == 1346 ? reel : rod;
        replaced = false;
        Item line = lineParent.getFishingLine();
        if (line == null && hasTacklebox) {
            if (knowledge > 70.0f || reel != null) {
                int lineTemplateId = FishEnums.ReelType.fromItem(reel).getAssociatedLineTemplateId();
                replaced = MethodsFishing.autoReplace(performer, lineTemplateId, (byte)0, lineParent);
                if (replaced) {
                    line = lineParent.getFishingLine();
                    performer.getCommunicator().sendNormalServerMessage("You managed to put another " + line.getName() + " in the " + lineParent.getName() + "!");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("No replacement line found!");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot remember what the line was!");
            }
            if (!replaced) {
                performer.getCommunicator().sendNormalServerMessage("You are missing line, float, fishing hook and bait!");
                return true;
            }
        }
        if ((afloat = line.getFishingFloat()) == null && hasTacklebox) {
            if (knowledge > 50.0f) {
                FishEnums.FloatType floatType = FishEnums.FloatType.fromInt(rod.getData1() >> 8 & 0xF);
                replaced = MethodsFishing.autoReplace(performer, floatType.getTemplateId(), (byte)0, line);
                if (replaced) {
                    afloat = line.getFishingFloat();
                    String floatName = afloat.getName();
                    performer.getCommunicator().sendNormalServerMessage("You managed to put another " + floatName + " on the " + line.getName() + "!");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("No replacement float found!");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot remember what the float was!");
            }
        }
        if ((hook = line.getFishingHook()) == null && hasTacklebox) {
            if (knowledge > 30.0f) {
                FishEnums.HookType hookType = FishEnums.HookType.fromInt(rod.getData1() >> 4 & 0xF);
                byte hookMaterial = (byte)(rod.getData2() & 0xFF);
                replaced = MethodsFishing.autoReplace(performer, hookType.getTemplateId(), hookMaterial, line);
                if (replaced) {
                    hook = line.getFishingHook();
                    performer.getCommunicator().sendNormalServerMessage("You managed to put another " + hook.getName() + " on the " + line.getName() + "!");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("No replacement fishing hook found!");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot remember what the fishing hook was!");
            }
        }
        Item bait = null;
        if (hook != null && (bait = hook.getFishingBait()) == null && hasTacklebox) {
            if (knowledge > 10.0f) {
                FishEnums.BaitType baitType = FishEnums.BaitType.fromInt(rod.getData1() & 0xF);
                replaced = MethodsFishing.autoReplace(performer, baitType.getTemplateId(), (byte)0, hook);
                if (replaced) {
                    bait = hook.getFishingBait();
                    performer.getCommunicator().sendNormalServerMessage("You managed to put another " + bait.getName() + " on the " + hook.getName() + "!");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("No replacement bait found!");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot remember what the bait was!");
            }
        }
        if (afloat == null && hook == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a float, fishing hook and bait!");
            return true;
        }
        if (afloat == null && bait == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a float and bait!");
            return true;
        }
        if (afloat == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a float!");
            return true;
        }
        if (hook == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a fishing hook and bait!");
            return true;
        }
        if (bait == null) {
            performer.getCommunicator().sendNormalServerMessage("You are missing a bait!");
            return true;
        }
        return true;
    }

    @Nullable
    private static Item getBoxCompartment(Item tacklebox, int templateId) {
        for (Item compartment : tacklebox.getItems()) {
            for (ContainerRestriction cRest : compartment.getTemplate().getContainerRestrictions()) {
                if (!cRest.contains(templateId)) continue;
                return compartment;
            }
        }
        return null;
    }

    private static boolean processFishPause(Creature performer, Action act) {
        act.setTickCount(2);
        return false;
    }

    private static boolean processFishHooked(Creature performer, Action act, Skill fishing, Item rod) {
        if (act.getCreature() == null) {
            act.setTickCount(5);
        } else {
            Creature fish = act.getCreature();
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            performer.getCommunicator().sendFishSubCommand((byte)3, -1L);
            performer.sendFishHooked(faid.getFishTypeId(), fish.getWurmId());
            if (MethodsFishing.processFishPull(performer, act, fishing, rod, true)) {
                MethodsFishing.sendFishStop(performer, act);
                return true;
            }
        }
        return false;
    }

    private static boolean processFishPull(Creature performer, Action act, Skill fishing, Item rod, boolean initial) {
        float result;
        boolean isClam;
        Item[] fishingItems = rod.getFishingItems();
        Item fishingReel = fishingItems[0];
        Item fishingLine = fishingItems[1];
        Item fishingFloat = fishingItems[2];
        Item fishingHook = fishingItems[3];
        Item fishingBait = fishingItems[4];
        Creature fish = act.getCreature();
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        boolean bl = isClam = faid.getFishTypeId() == FishEnums.FishData.CLAM.getTypeId();
        if (initial) {
            performer.getCommunicator().sendNormalServerMessage("You hooked " + faid.getNameWithGenusAndSize() + "!");
            Server.getInstance().broadCastAction(performer.getName() + " hooks " + faid.getNameWithGenusAndSize() + ".", performer, 5);
        }
        float fstr = faid.getBodyStrength();
        float pstr = (float)performer.getSkills().getSkillOrLearn(102).getKnowledge(0.0);
        float strBonus = Math.max(1.0f, pstr - fstr);
        float stamBonus = faid.getBodyStamina() - 20.0f;
        float bonus = (strBonus + stamBonus) / 2.0f;
        float adjusted = result = MethodsFishing.getDifficulty(performer, act, act.getPosX(), act.getPosY(), fishing, rod, fishingReel, fishingLine, fishingFloat, fishingHook, fishingBait, bonus, 10.0f);
        if (fishingReel != null) {
            adjusted += (float)(fishingReel.getRarity() * fishingReel.getRarity());
        }
        if (adjusted < 0.0f && !isClam) {
            if (result <= (float)(-(90 + rod.getRarity() * 3))) {
                act.setTickCount(7);
            } else if (result <= (float)(-(70 + fishingLine.getRarity() * 3))) {
                act.setTickCount(6);
            } else if (result <= (float)(-(50 + fishingHook.getRarity() * 3))) {
                act.setTickCount(13);
            } else {
                if (!initial) {
                    if (result <= -30.0f) {
                        performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " pulls hard on the line!");
                    } else if (result <= -15.0f) {
                        performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " pulls somewhat on the line!");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " pulls a bit on the line!");
                    }
                }
                MethodsFishing.testMessage(performer, faid.getNameWithSize() + " moving away", " Result:" + result);
                faid.decBodyStamina(strBonus / 2.0f + 2.0f);
                double lNewrot = Math.atan2(performer.getPosY() - fish.getPosY(), performer.getPosX() - fish.getPosX());
                float rot = (float)(lNewrot * 57.29577951308232) - 90.0f;
                float angle = rot + result * 2.0f + 90.0f;
                float dist = Math.min(2.0f, strBonus / 10.0f);
                Point4f end = MethodsFishing.calcSpot(fish.getPosX(), fish.getPosY(), Creature.normalizeAngle(angle), dist);
                float speedMod = dist / 2.0f;
                faid.setMovementSpeedModifier(speedMod);
                faid.setTargetPos(end.getPosX(), end.getPosY());
                int moreTime = (int)faid.getTimeToTarget();
                int timeleft = act.getSecond() * 10 + moreTime;
                act.setData(timeleft);
                act.setTickCount(17);
            }
        } else {
            Point4f end;
            float speedMod;
            if (!initial && !isClam) {
                if (result > 80.0f) {
                    performer.getCommunicator().sendNormalServerMessage("You manage to easily reel in the " + faid.getNameWithSize() + "!");
                } else if (result > 50.0f) {
                    performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " stands no chance!");
                } else if (result > 25.0f) {
                    performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " takes a rest, so you reel it in a bit!");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The " + faid.getNameWithSize() + " starts to get tired and you manage to reel it in a bit!");
                }
            }
            double lNewrot = Math.atan2(performer.getPosY() - fish.getPosY(), performer.getPosX() - fish.getPosX());
            float rot = (float)(lNewrot * 57.29577951308232);
            if (isClam) {
                performer.getCommunicator().sendNormalServerMessage("You quickly reel in the " + faid.getNameWithSize() + "!");
                speedMod = 2.0f;
                end = new Point4f(performer.getPosX(), performer.getPosY(), 0.0f, Creature.normalizeAngle(rot + 180.0f));
            } else {
                MethodsFishing.testMessage(performer, faid.getNameWithSize() + " moving closer", " Result:" + result);
                faid.decBodyStamina(1.0f);
                float angle = rot + result + 40.0f;
                float dist = Math.min(2.5f, strBonus / 15.0f + Math.max(0.0f, result - 50.0f) / 15.0f);
                speedMod = 1.0f;
                end = MethodsFishing.calcSpot(fish.getPosX(), fish.getPosY(), Creature.normalizeAngle(angle), dist);
            }
            faid.setMovementSpeedModifier(speedMod);
            faid.setTargetPos(end.getPosX(), end.getPosY());
            int moreTime = (int)faid.getTimeToTarget();
            int timeleft = act.getSecond() * 10 + moreTime;
            act.setData(timeleft);
            act.setTickCount(17);
        }
        FishEnums.FishData fd = faid.getFishData();
        float fdam = fd.getDamageMod();
        float damMod = (float)((double)fdam * Math.max((double)0.1f, faid.getWeight() > 6000 ? Math.pow(faid.getWeight() / 1000, 0.6) : (double)(faid.getWeight() / 3000)));
        float additionalDamage = MethodsFishing.additionalDamage(rod, damMod * MethodsFishing.getRodDamageModifier(rod), true);
        float newDamage = rod.getDamage() + additionalDamage;
        if (newDamage > 100.0f) {
            MethodsFishing.destroyContents(rod);
            return rod.setDamage(newDamage);
        }
        if (additionalDamage > 0.0f) {
            rod.setDamage(newDamage);
        }
        if (rod.getTemplateId() == 1344) {
            fishingLine = rod.getFishingLine();
        } else {
            fishingReel = rod.getFishingReel();
            additionalDamage = MethodsFishing.additionalDamage(fishingReel, damMod * MethodsFishing.getReelDamageModifier(fishingReel), true);
            newDamage = fishingReel.getDamage() + additionalDamage;
            if (newDamage > 100.0f) {
                MethodsFishing.destroyContents(fishingReel);
                return fishingReel.setDamage(newDamage);
            }
            if (additionalDamage > 0.0f) {
                fishingReel.setDamage(newDamage);
            }
            fishingLine = fishingReel.getFishingLine();
        }
        additionalDamage = MethodsFishing.additionalDamage(fishingLine, damMod * 0.3f, true);
        newDamage = fishingLine.getDamage() + additionalDamage;
        if (newDamage > 100.0f) {
            MethodsFishing.destroyContents(fishingLine);
            return fishingLine.setDamage(newDamage);
        }
        if (additionalDamage > 0.0f) {
            fishingLine.setDamage(newDamage);
        }
        return false;
    }

    private static boolean processFishCaught(Creature performer, Action act, Skill fishing, Item rod) {
        Creature fish = act.getCreature();
        performer.getCommunicator().sendFishSubCommand((byte)12, -1L);
        performer.sendFishingStopped();
        FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
        performer.getCommunicator().sendNormalServerMessage("You catch " + faid.getNameWithGenusAndSize());
        Server.getInstance().broadCastAction(performer.getName() + " lands " + faid.getNameWithGenusAndSize() + ".", performer, 5);
        MethodsFishing.makeDeadFish(performer, act, fishing, faid.getFishTypeId(), rod, performer.getInventory());
        MethodsFishing.destroyFishCreature(act);
        return MethodsFishing.doAutoReplace(performer, act);
    }

    private static boolean processFishLineSnapped(Creature performer, Action act, Item rod) {
        performer.getCommunicator().sendNormalServerMessage("Your line snapped!!");
        Item[] fishingItems = rod.getFishingItems();
        Item fishingReel = fishingItems[0];
        Item fishingLine = fishingItems[1];
        MethodsFishing.destroyContents(fishingLine);
        byte currentPhase = (byte)act.getTickCount();
        boolean brokeRod = false;
        if (currentPhase == 7) {
            Creature fish = act.getCreature();
            FishAI.FishAIData faid = (FishAI.FishAIData)fish.getCreatureAIData();
            FishEnums.FishData fd = faid.getFishData();
            int weight = faid.getWeight();
            float fdam = (float)fd.getDamageMod() * 5.0f;
            float damMod = fdam * Math.max(0.1f, (float)(weight / 3000));
            float additionalDamage = MethodsFishing.additionalDamage(rod, damMod * MethodsFishing.getRodDamageModifier(rod) * 2.0f, true);
            float newDamage = rod.getDamage() + additionalDamage;
            if (newDamage > 100.0f) {
                MethodsFishing.destroyContents(rod);
            }
            if (additionalDamage > 0.0f) {
                brokeRod = rod.setDamage(newDamage);
            }
            if (!brokeRod && fishingReel != null) {
                additionalDamage = MethodsFishing.additionalDamage(fishingReel, damMod * MethodsFishing.getReelDamageModifier(fishingReel) * 2.0f, true);
                newDamage = fishingReel.getDamage() + additionalDamage;
                if (newDamage > 100.0f) {
                    MethodsFishing.destroyContents(fishingReel);
                }
                if (additionalDamage > 0.0f) {
                    fishingReel.setDamage(newDamage);
                }
            }
        }
        performer.getCommunicator().sendFishSubCommand((byte)6, -1L);
        MethodsFishing.processFishMovedOn(performer, act, 2.2f);
        act.setTickCount(15);
        return false;
    }

    private static void destroyContents(Item container) {
        for (Item item : container.getItemsAsArray()) {
            if (!item.isEmpty(false)) {
                MethodsFishing.destroyContents(item);
            }
            item.setDamage(100.0f);
        }
    }

    private static boolean sendFishStop(Creature performer, Action act) {
        MethodsFishing.destroyFishCreature(act);
        performer.getCommunicator().sendFishSubCommand((byte)15, -1L);
        performer.sendFishingStopped();
        return MethodsFishing.doAutoReplace(performer, act);
    }

    public static void fromClient(Creature performer, byte subCommand, float posX, float posY) {
        try {
            Action act = performer.getCurrentAction();
            if (act.getNumber() != 160) {
                MethodsFishing.testMessage(performer, "not fishing? ", "Action:" + act.getNumber());
                logger.log(Level.WARNING, "not fishing! " + act.getNumber());
                return;
            }
            byte phase = (byte)act.getTickCount();
            switch (subCommand) {
                case 9: {
                    if (phase != 0) {
                        MethodsFishing.testMessage(performer, "Incorrect fishing subcommand", " (" + MethodsFishing.fromCommand(subCommand) + ") for phase (" + MethodsFishing.fromCommand(phase) + ")");
                        logger.log(Level.WARNING, "Incorrect fishing subcommand (" + MethodsFishing.fromCommand(subCommand) + ") for phase (" + MethodsFishing.fromCommand(phase) + ")");
                        return;
                    }
                    Item rod = act.getSubject();
                    if (rod == null) {
                        MethodsFishing.testMessage(performer, "", "Subject missing in action");
                        logger.log(Level.WARNING, "Subject missing in action");
                        break;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You cast the line and start fishing.");
                    Server.getInstance().broadCastAction(performer.getName() + " casts and starts fishing.", performer, 5);
                    MethodsFishing.processFishCasted(performer, act, posX, posY, rod, true);
                    break;
                }
                case 10: {
                    act.setTickCount(subCommand);
                    break;
                }
                case 11: {
                    Skill fishing = performer.getSkills().getSkillOrLearn(10033);
                    Item rod = act.getSubject();
                    if (rod == null) {
                        MethodsFishing.testMessage(performer, "", "Subject missing in action");
                        logger.log(Level.WARNING, "Subject missing in action");
                        break;
                    }
                    if (MethodsFishing.canStrike(phase)) {
                        MethodsFishing.processFishStrike(performer, act, fishing, rod);
                    }
                    break;
                }
                case 27: {
                    act.setTickCount(subCommand);
                    break;
                }
                case 26: {
                    if (phase != 21) {
                        MethodsFishing.testMessage(performer, "Incorrect fishing subcommand", " (" + MethodsFishing.fromCommand(subCommand) + ") for phase (" + MethodsFishing.fromCommand(phase) + ")");
                        logger.log(Level.WARNING, "Incorrect fishing subcommand (" + MethodsFishing.fromCommand(subCommand) + ") for phase (" + MethodsFishing.fromCommand(phase) + ")");
                        return;
                    }
                    act.setTickCount(subCommand);
                    Skill fishing = performer.getSkills().getSkillOrLearn(10033);
                    Item spear = act.getSubject();
                    if (spear == null) {
                        MethodsFishing.testMessage(performer, "", "Subject missing in action");
                        logger.log(Level.WARNING, "Subject missing in action");
                        break;
                    }
                    MethodsFishing.processSpearStrike(performer, act, fishing, spear, posX, posY);
                    break;
                }
                default: {
                    MethodsFishing.testMessage(performer, "Bad fishing subcommand!", " (" + MethodsFishing.fromCommand(subCommand) + ")");
                    logger.log(Level.WARNING, "Bad fishing subcommand! " + MethodsFishing.fromCommand(subCommand) + " (" + subCommand + ")");
                    return;
                }
            }
        }
        catch (NoSuchActionException e) {
            MethodsFishing.testMessage(performer, "", "No current action, should be FISH.");
            logger.log(Level.WARNING, "No current action, should be FISH:" + e.getMessage(), e);
        }
    }

    private static boolean canStrike(byte phase) {
        switch (phase) {
            case 3: 
            case 17: {
                return false;
            }
        }
        return true;
    }

    private static void testMessage(Creature performer, String message, String powerMessage) {
        if (Servers.isThisATestServer() && (performer.getPower() > 1 || performer.hasFlag(51))) {
            if (performer.getPower() >= 2) {
                performer.getCommunicator().sendNormalServerMessage("(test only) " + message + powerMessage);
            } else if (message.length() > 0) {
                performer.getCommunicator().sendNormalServerMessage("(test only) " + message);
            }
        }
    }

    public static String fromCommand(byte command) {
        switch (command) {
            case 0: {
                return "FISH_START";
            }
            case 1: {
                return "FISH_BITE";
            }
            case 2: {
                return "FISH_MOVED_ON";
            }
            case 19: {
                return "FISH_MOVING_ON";
            }
            case 3: {
                return "FISH_HOOKED";
            }
            case 4: {
                return "FISH_MISSED";
            }
            case 5: {
                return "FISH_NO_FISH";
            }
            case 6: {
                return "FISH_LINE_SNAPPED";
            }
            case 7: {
                return "FISH_ROD_BROKE";
            }
            case 8: {
                return "FISH_TIME_OUT";
            }
            case 9: {
                return "FISH_CASTED";
            }
            case 10: {
                return "FISH_CANCEL";
            }
            case 11: {
                return "FISH_STRIKE";
            }
            case 12: {
                return "FISH_CAUGHT";
            }
            case 13: {
                return "FISH_GOT_AWAY";
            }
            case 14: {
                return "FISH_SWAM_AWAY";
            }
            case 15: {
                return "FISH_STOP";
            }
            case 16: {
                return "FISH_MOVE";
            }
            case 17: {
                return "FISH_PULL";
            }
            case 18: {
                return "FISH_PAUSE";
            }
            case 20: {
                return "SPEAR_START";
            }
            case 21: {
                return "SPEAR_MOVE";
            }
            case 22: {
                return "SPEAR_HIT";
            }
            case 23: {
                return "SPEAR_MISSED";
            }
            case 24: {
                return "SPEAR_NO_FISH";
            }
            case 25: {
                return "SPEAR_TIME_OUT";
            }
            case 26: {
                return "SPEAR_STRIKE";
            }
            case 27: {
                return "SPEAR_CANCEL";
            }
            case 28: {
                return "SPEAR_SWAM_AWAY";
            }
            case 29: {
                return "SPEAR_STOP";
            }
            case 40: {
                return "NET_START";
            }
            case 45: {
                return "SHOW_FISH_SPOTS";
            }
        }
        return "Unknown (" + command + ")";
    }

    private static byte getRodType(Item rod, @Nullable Item reel, @Nullable Item line) {
        if (rod.getTemplateId() == 1344) {
            return FishingEnums.RodType.FISHING_POLE.getTypeId();
        }
        if (rod.getTemplateId() != 1346 || reel == null) {
            return -1;
        }
        if (line == null) {
            switch (reel.getTemplateId()) {
                case 1372: {
                    return FishingEnums.RodType.FISHING_ROD_BASIC.getTypeId();
                }
                case 1373: {
                    return FishingEnums.RodType.FISHING_ROD_FINE.getTypeId();
                }
                case 1374: {
                    return FishingEnums.RodType.FISHING_ROD_DEEP_WATER.getTypeId();
                }
                case 1375: {
                    return FishingEnums.RodType.FISHING_ROD_DEEP_SEA.getTypeId();
                }
            }
            return -1;
        }
        switch (reel.getTemplateId()) {
            case 1372: {
                return FishingEnums.RodType.FISHING_ROD_BASIC_WITH_LINE.getTypeId();
            }
            case 1373: {
                return FishingEnums.RodType.FISHING_ROD_FINE_WITH_LINE.getTypeId();
            }
            case 1374: {
                return FishingEnums.RodType.FISHING_ROD_DEEP_WATER_WITH_LINE.getTypeId();
            }
            case 1375: {
                return FishingEnums.RodType.FISHING_ROD_DEEP_SEA_WITH_LINE.getTypeId();
            }
        }
        return -1;
    }

    private static float[] getMinMaxRadius(Item rod, Item line) {
        float min = rod.getTemplateId() == 1344 ? 2.0f : 4.0f;
        float linelength = MethodsFishing.getSingleLineLength(line);
        float max = Math.min((linelength - min) / 2.0f, 8.0f) + min;
        return new float[]{min, max};
    }

    public static int getLineLength(Item line) {
        int lineTemplateWeight = line.getTemplate().getWeightGrams();
        int lineWeight = line.getWeightGrams();
        float comb = lineWeight / lineTemplateWeight;
        int slen = MethodsFishing.getSingleLineLength(line);
        int tlen = (int)(comb * (float)slen);
        return tlen;
    }

    public static int getSingleLineLength(Item line) {
        switch (line.getTemplateId()) {
            case 1347: {
                return 10;
            }
            case 1348: {
                return 12;
            }
            case 1349: {
                return 14;
            }
            case 1350: {
                return 16;
            }
            case 1351: {
                return 18;
            }
        }
        return 10;
    }

    public static Color getBgColour(int season) {
        int[] bgRed = new int[]{181, 34, 183, 192};
        int[] bgGreen = new int[]{230, 177, 141, 192};
        int[] bgBlue = new int[]{29, 0, 76, 192};
        return new Color(bgRed[season], bgGreen[season], bgBlue[season]);
    }

    public static Point getSeasonOffset(int season) {
        int[] offsetXs = new int[]{0, 128, 0, 128};
        int[] offsetYs = new int[]{0, 0, 128, 128};
        return new Point(offsetXs[season], offsetYs[season]);
    }

    public static Color getFishColour(int templateId) {
        int red = 0;
        int green = 0;
        int blue = 0;
        switch (templateId) {
            case 1336: {
                red = 255;
                break;
            }
            case 572: {
                break;
            }
            case 569: {
                red = 255;
                green = 255;
                break;
            }
            case 570: {
                red = 255;
                green = 127;
                break;
            }
            case 574: {
                green = 255;
                break;
            }
            case 573: {
                green = 255;
                blue = 255;
                break;
            }
            case 571: {
                red = 127;
                blue = 255;
                break;
            }
            case 575: {
                blue = 255;
            }
        }
        return new Color(red, green, blue);
    }

    public static class FishRow {
        private final byte fishTypeId;
        private final String name;
        private float chance = 0.0f;

        public FishRow(int fishTypeId, String name) {
            this.fishTypeId = (byte)fishTypeId;
            this.name = name;
        }

        public byte getFishTypeId() {
            return this.fishTypeId;
        }

        public String getName() {
            return this.name;
        }

        public float getChance() {
            return this.chance;
        }

        public void setChance(float chance) {
            this.chance = chance;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("FishData [");
            buf.append("Name: ").append(this.name);
            buf.append(", Id: ").append(this.fishTypeId);
            buf.append(", Chance: ").append(this.chance);
            buf.append("]");
            return buf.toString();
        }
    }
}


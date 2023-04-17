/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.questions.CultQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Cults
implements TimeConstants,
MiscConstants {
    public static final byte PATH_NONE = 0;
    public static final byte PATH_LOVE = 1;
    public static final byte PATH_HATE = 2;
    public static final byte PATH_KNOWLEDGE = 3;
    public static final byte PATH_INSANITY = 4;
    public static final byte PATH_POWER = 5;
    private static final String[] YES_NO_STRINGS = new String[]{"Yes", "No"};
    private static final Logger logger = Logger.getLogger(Cults.class.getName());

    private Cults() {
    }

    public static final byte getPathFor(int tilex, int tiley, int layer, int performerHeight) {
        boolean bl;
        short[] steepness;
        byte path = 0;
        if (layer >= 0 && (steepness = Creature.getTileSteepness(tilex, tiley, layer >= 0))[1] > 10) {
            return 0;
        }
        int[][] closeTiles = Cults.getCloseTiles(tilex, tiley, layer);
        HashMap<Byte, Integer> dominatingType = new HashMap<Byte, Integer>();
        boolean nearWater = false;
        boolean quickDropoff = false;
        boolean belowRockfall = false;
        boolean insanityBlocker = false;
        boolean hateEnabler = false;
        int flowerTiles = 0;
        int thornTiles = 0;
        for (int[] nArray : closeTiles) {
            for (int y = 0; y < closeTiles.length; ++y) {
                boolean detectedWater = false;
                byte type = Tiles.decodeType(nArray[y]);
                Tiles.Tile theTile = Tiles.getTile(type);
                Integer nums = (Integer)dominatingType.get(type);
                if (nums == null) {
                    nums = 0;
                }
                if (layer < 0) {
                    if (Tiles.isOreCave(type)) {
                        insanityBlocker = true;
                    } else if (type == Tiles.Tile.TILE_CAVE_EXIT.id) {
                        insanityBlocker = true;
                    }
                } else if (type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_TUNDRA.id || type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_TAR.id) {
                    hateEnabler = true;
                }
                nums = nums + 1;
                dominatingType.put(type, nums);
                short height = Tiles.decodeHeight(nArray[y]);
                int diff = performerHeight - height;
                if ((float)height < -5.0f) {
                    detectedWater = true;
                } else if (diff < -100) {
                    belowRockfall = true;
                } else if (diff > 100 && !nearWater) {
                    quickDropoff = true;
                }
                if (detectedWater) {
                    nearWater = true;
                }
                if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_REED.id) {
                    GrassData.FlowerType f = GrassData.FlowerType.decodeTileData(Tiles.decodeData(nArray[y]));
                    if (f == GrassData.FlowerType.NONE) continue;
                    ++flowerTiles;
                    continue;
                }
                if (!theTile.isMyceliumBush() || !theTile.isThorn(Tiles.decodeData(nArray[y]))) continue;
                ++thornTiles;
            }
        }
        if (layer < 0) {
            if (!insanityBlocker) {
                return 4;
            }
            return 0;
        }
        byte domType = Tiles.Tile.TILE_GRASS.id;
        byte secondType = Tiles.Tile.TILE_GRASS.id;
        int maxCount = 0;
        for (Map.Entry e : dominatingType.entrySet()) {
            int count = (Integer)e.getValue();
            if (count <= maxCount) continue;
            secondType = domType;
            domType = (Byte)e.getKey();
            maxCount = count;
        }
        boolean bl2 = false;
        int westTile = Server.surfaceMesh.getTile(Zones.safeTileX(tilex + 20), Zones.safeTileY(tiley));
        short height = Tiles.decodeHeight(westTile);
        int diff = performerHeight - height;
        if ((float)height < -5.0f) {
            boolean bl3 = true;
        } else if (diff < -400) {
            belowRockfall = true;
        } else if (diff > 400) {
            quickDropoff = true;
        }
        int northTile = Server.surfaceMesh.getTile(Zones.safeTileX(tilex), Zones.safeTileY(tiley - 20));
        height = Tiles.decodeHeight(northTile);
        diff = performerHeight - height;
        if ((float)height < -5.0f) {
            boolean bl4 = true;
        } else if (diff < -400) {
            belowRockfall = true;
        } else if (diff > 400) {
            quickDropoff = true;
        }
        int southTile = Server.surfaceMesh.getTile(Zones.safeTileX(tilex), Zones.safeTileY(tiley + 20));
        height = Tiles.decodeHeight(southTile);
        diff = performerHeight - height;
        if ((float)height < -5.0f) {
            boolean bl5 = true;
        } else if (diff < -400) {
            belowRockfall = true;
        } else if (diff > 400) {
            quickDropoff = true;
        }
        int eastTile = Server.surfaceMesh.getTile(Zones.safeTileX(tilex - 20), Zones.safeTileY(tiley));
        height = Tiles.decodeHeight(eastTile);
        diff = performerHeight - height;
        if ((float)height < -5.0f) {
            bl = true;
        } else if (diff < -400) {
            belowRockfall = true;
        } else if (diff > 400) {
            quickDropoff = true;
        }
        if (domType == Tiles.Tile.TILE_MOSS.id || domType == Tiles.Tile.TILE_CLAY.id || domType == Tiles.Tile.TILE_TUNDRA.id) {
            return 0;
        }
        if (secondType == Tiles.Tile.TILE_MOSS.id || secondType == Tiles.Tile.TILE_CLAY.id || secondType == Tiles.Tile.TILE_TUNDRA.id) {
            return 0;
        }
        if (performerHeight > 2000) {
            if (!belowRockfall && (domType == Tiles.Tile.TILE_ROCK.id || domType == Tiles.Tile.TILE_CLIFF.id || quickDropoff)) {
                return 5;
            }
            return 0;
        }
        if (Tiles.getTile(domType).isMycelium() && !nearWater && (hateEnabler || thornTiles > 3)) {
            return 2;
        }
        if (bl && nearWater) {
            if (belowRockfall || quickDropoff) {
                return 3;
            }
            if (domType == Tiles.Tile.TILE_SAND.id) {
                return 3;
            }
        }
        if (!nearWater && bl && secondType == Tiles.Tile.TILE_GRASS.id && flowerTiles > 4) {
            path = 1;
        }
        return path;
    }

    static final int[][] getCloseTiles(int tilex, int tiley, int layer) {
        int[][] toReturn = new int[5][5];
        for (int x = -2; x < 3; ++x) {
            for (int y = -2; y < 3; ++y) {
                try {
                    if (layer >= 0) {
                        toReturn[x + 2][y + 2] = Server.surfaceMesh.getTile(tilex + x, tiley + y);
                        continue;
                    }
                    toReturn[x + 2][y + 2] = Server.caveMesh.getTile(tilex + x, tiley + y);
                    continue;
                }
                catch (Exception ex) {
                    toReturn[x + 2][y + 2] = -100;
                }
            }
        }
        return toReturn;
    }

    public static final boolean meditate(Creature performer, int layer, Action aAct, float counter, Item rug) {
        Cultist cultist;
        int tilex = performer.getTileX();
        int tiley = performer.getTileY();
        boolean done = false;
        if (performer.getPositionZ() + performer.getAltOffZ() < 0.1f) {
            performer.getCommunicator().sendNormalServerMessage("You can not meditate here. It is too wet.");
            return true;
        }
        if (!rug.isMeditation()) {
            performer.getCommunicator().sendNormalServerMessage("You can't meditate with that.");
            return true;
        }
        byte path = Cults.getPathFor(performer.getTileX(), performer.getTileY(), layer, (int)(performer.getPositionZ() * 10.0f));
        Skill meditation = null;
        try {
            meditation = performer.getSkills().getSkill(10086);
        }
        catch (NoSuchSkillException nss) {
            meditation = performer.getSkills().learn(10086, 1.0f);
        }
        boolean increaseSkill = true;
        if (performer.getPower() == 0 && Math.abs(performer.getMeditateX() - performer.getTileX()) < 10 && Math.abs(performer.getMeditateY() - performer.getTileY()) < 10) {
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage("You recently meditated here and need to find new insights somewhere else.");
            }
            increaseSkill = false;
        }
        if ((cultist = Cultist.getCultist(performer.getWurmId())) == null && meditation.getKnowledge(0.0) >= 20.0) {
            increaseSkill = false;
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage("You need to find deeper mysteries in order to advance your skill in meditation now.");
            }
        }
        int time = aAct.getTimeLeft();
        if (counter == 1.0f) {
            time = 110 + Server.rand.nextInt(20);
            if (performer.getPower() < 5) {
                time *= 10;
            }
            aAct.setTimeLeft(time);
            String tosend = "You start meditating.";
            if (performer.getPower() >= 5) {
                tosend = tosend + " Path is " + Cults.getPathNameFor(path);
            }
            performer.getCommunicator().sendNormalServerMessage(tosend);
            Server.getInstance().broadCastAction(performer.getName() + " starts meditating.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[384].getVerbString(), true, time);
            performer.playAnimation("meditate", false);
        } else if (aAct.currentSecond() == 5) {
            float difficulty = 5.0f;
            if (path != 0 && increaseSkill) {
                difficulty = 10.0f;
            }
            if (!increaseSkill) {
                difficulty = 20.0f;
            }
            if (meditation.skillCheck(difficulty, rug, 0.0, true, 1.0f + (float)rug.getRarity() * 0.1f) < 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You fail to relax.");
                if (performer.getPower() == 5) {
                    performer.getCommunicator().sendNormalServerMessage("Difficulty=" + difficulty + " increaseSkill=" + increaseSkill + " cultist is null?" + (cultist == null));
                }
                return true;
            }
            byte level = 0;
            if (cultist != null) {
                level = cultist.getLevel();
            }
            performer.getCommunicator().sendNormalServerMessage("You fall into a trance. " + Cults.getMeditationString(path, level, tilex, tiley));
        }
        if (counter * 10.0f > (float)time) {
            if (performer.getPower() == 5) {
                if (performer.getLayer() < 0) {
                    path = 4;
                } else {
                    switch (performer.getKingdomTemplateId()) {
                        case 3: {
                            path = 2;
                            break;
                        }
                        case 1: {
                            path = 3;
                            break;
                        }
                        case 4: {
                            path = 1;
                            break;
                        }
                        case 2: {
                            path = 5;
                            break;
                        }
                    }
                }
            }
            done = true;
            double power = 0.0;
            float difficulty = 5.0f;
            performer.getCommunicator().sendNormalServerMessage("You finish your meditation.");
            Server.getInstance().broadCastAction(performer.getName() + " finishes " + performer.getHisHerItsString() + " meditation.", performer, 5);
            if (cultist != null && meditation.getKnowledge(0.0) >= 20.0) {
                long timeSinceLast = System.currentTimeMillis() - cultist.getLastMeditated();
                if (increaseSkill && cultist.getSkillgainCount() > 0 && timeSinceLast > (Servers.localServer.isChallengeServer() ? 1800000L : 10800000L)) {
                    cultist.decreaseSkillGain();
                }
                if (timeSinceLast < 1800000L || cultist.getSkillgainCount() >= 5) {
                    increaseSkill = false;
                } else if (increaseSkill) {
                    cultist.increaseSkillgain();
                    if (cultist.getSkillgainCount() >= 5) {
                        performer.getCommunicator().sendNormalServerMessage("You feel that it will take you a while before you are ready to meditate again.");
                    }
                    cultist.setLastMeditated();
                }
                try {
                    cultist.saveCultist(false);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, performer.getName() + iox.getMessage(), iox);
                }
            }
            boolean bl = !increaseSkill;
            if (meditation.skillCheck(95.0, rug, 0.0, bl, 1.0f + (float)rug.getRarity() * 0.1f) > 0.0) {
                float nut = (float)(50 + Server.rand.nextInt(49)) / 100.0f;
                if (performer.getStatus().refresh(nut, false)) {
                    performer.getCommunicator().sendNormalServerMessage("You feel invigorated.");
                }
            }
            performer.achievement(103);
            boolean mayIncreaseLevel = false;
            long timeToNextLevel = -1L;
            if (path != 0) {
                difficulty = 10.0f;
                if (cultist != null) {
                    timeToNextLevel = cultist.getTimeLeftToIncreasePath(System.currentTimeMillis(), meditation.getKnowledge(0.0));
                    if (timeToNextLevel <= 0L) {
                        if ((double)(cultist.getLevel() * 10) - meditation.getKnowledge(0.0) < 30.0 || meditation.getKnowledge(0.0) > 90.0) {
                            mayIncreaseLevel = true;
                            difficulty = 1 + cultist.getLevel() * 10;
                        } else {
                            difficulty = 1 + cultist.getLevel() * 3;
                            performer.getCommunicator().sendNormalServerMessage("You have a hard time to focus and no question comes to you. You probably need higher meditation skill.");
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage(Cults.getWaitForProgressMessage(timeToNextLevel));
                        if (performer.getPower() == 5) {
                            mayIncreaseLevel = true;
                            difficulty = 1 + cultist.getLevel() * 10;
                            timeToNextLevel = 0L;
                            performer.getCommunicator().sendNormalServerMessage(".. but what the heck. Go on, man. Go on.");
                        }
                    }
                } else if (meditation.getKnowledge(0.0) >= 15.0) {
                    mayIncreaseLevel = true;
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You should return here when you have become better at meditating.");
                }
            }
            power = meditation.skillCheck(difficulty, rug, 0.0, !increaseSkill, counter / 3.0f * (1.0f + (float)rug.getRarity() * 0.1f));
            performer.setMeditateX(performer.getTileX());
            performer.setMeditateY(performer.getTileY());
            rug.setDamage(rug.getDamage() + 0.1f * rug.getDamageModifier());
            if (path != 0) {
                if (path != 4) {
                    performer.getCommunicator().sendNormalServerMessage("This is indeed a special place.");
                }
                if (mayIncreaseLevel) {
                    if (power > 0.0) {
                        if (cultist == null) {
                            CultQuestion cq = new CultQuestion(performer, "Following a path", "Do you wish to embark on a journey?", timeToNextLevel, null, path, false, false);
                            cq.sendQuestion();
                        } else if (path == cultist.getPath()) {
                            if (cultist.getLevel() < 6 || performer.isPaying()) {
                                CultQuestion cq = new CultQuestion(performer, "Following a path", "Are you ready to proceed?", timeToNextLevel, cultist, path, false, false);
                                cq.sendQuestion();
                            }
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You gain no inspiration here for your own path.");
                        }
                    } else if (performer.getPower() == 5) {
                        performer.getCommunicator().sendNormalServerMessage("Power of skillroll is " + power + " for difficulty " + difficulty + ".");
                    }
                }
            }
        }
        return done;
    }

    static String getWaitForProgressMessage(long timeToNextLevel) {
        String message = timeToNextLevel > 1814400000L ? "You still need to let your recent progress sink in for weeks to come." : (timeToNextLevel > 1209600000L ? "You still need to let your recent progress sink in for a few weeks." : (timeToNextLevel > 604800000L ? "You still need to let your recent progress sink in for more than a week." : (timeToNextLevel > 259200000L ? "You still need to let your recent progress sink in for about a week." : (timeToNextLevel > 86400000L ? "You still need to let your recent progress sink in for a few days." : (timeToNextLevel > 43200000L ? "You still need to let your recent progress sink in for many hours." : (timeToNextLevel > 10800000L ? "You still need to let your recent progress sink in for several hours." : (timeToNextLevel > 0L ? "You still need to let your recent progress sink in for a few hours." : "")))))));
        return message;
    }

    public static final String getMeditationString(byte path, byte level, int tilex, int tiley) {
        String medString;
        int max;
        int baseRand;
        byte pathForMessage;
        Random random = new Random();
        random.setSeed((tilex << 16) + tiley);
        if (Server.rand.nextInt(4) != 0) {
            pathForMessage = 0;
            baseRand = random.nextInt(60);
            max = 60;
        } else {
            pathForMessage = path;
            max = 20;
            baseRand = random.nextInt(21);
        }
        int rand = baseRand - 2 + Server.rand.nextInt(5);
        if (rand < 0) {
            rand += max;
        }
        if (rand > max) {
            rand -= max;
        }
        switch (pathForMessage) {
            case 0: {
                medString = Cults.getMeditationStringForNoPath(rand);
                break;
            }
            case 1: {
                medString = Cults.getMeditationStringForLovePath(rand);
                break;
            }
            case 5: {
                medString = Cults.getMeditationStringForPowerPath(rand);
                break;
            }
            case 2: {
                medString = Cults.getMeditationStringForHatePath(rand);
                break;
            }
            case 4: {
                medString = Cults.getMeditationStringForInsanityPath(rand);
                break;
            }
            case 3: {
                medString = Cults.getMeditationStringForKnowledgePath(rand);
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    static String getMeditationStringForKnowledgePath(int id) {
        String medString;
        switch (id) {
            case 0: {
                medString = "You think about reflections and shadows.";
                break;
            }
            case 1: {
                medString = "You wonder if the future exists but we only see parts of it like a mountainrange.";
                break;
            }
            case 2: {
                medString = "You try to recall if someone said that the gods don't play dice.";
                break;
            }
            case 3: {
                medString = "You wonder if you always try to decide on the best option.";
                break;
            }
            case 4: {
                medString = "You think about the next thing you want to learn.";
                break;
            }
            case 5: {
                medString = "You think of the things that interest you most.";
                break;
            }
            case 6: {
                medString = "You wonder if there is something really interesting out there that you haven't heard of.";
                break;
            }
            case 7: {
                medString = "You envision yourself teaching to the masses.";
                break;
            }
            case 8: {
                medString = "You wonder what you would say if you got to tell the whole world one sentence of truth.";
                break;
            }
            case 9: {
                medString = "You wonder how you would study and learn a book of lies.";
                break;
            }
            case 10: {
                medString = "What lies have you been told this week?";
                break;
            }
            case 11: {
                medString = "You think about your barriers for learning and how to remove them.";
                break;
            }
            case 12: {
                medString = "You wonder if it is possible to find knowledge that in no possible situation would save lives.";
                break;
            }
            case 13: {
                medString = "What would you do if you possessed most knowledge in the world?";
                break;
            }
            case 14: {
                medString = "If you could become best in the world at anything, what would that be?";
                break;
            }
            case 15: {
                medString = "You think of the various ways people become the best at something and whether you should adopt some style.";
                break;
            }
            case 16: {
                medString = "You wonder if time can move backwards somehow.";
                break;
            }
            case 17: {
                medString = "How come people who ought to know so much often are proven wrong?";
                break;
            }
            case 18: {
                medString = "Should you trust someone more because the person sounds more convincing?";
                break;
            }
            case 19: {
                medString = "Do you often lie or take chances when you are asked to speak?";
                break;
            }
            case 20: {
                medString = "Does history always repeat itself?";
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    static String getMeditationStringForInsanityPath(int id) {
        String medString;
        switch (id) {
            case 0: {
                medString = "Are they after you?";
                break;
            }
            case 1: {
                medString = "Who was that?";
                break;
            }
            case 2: {
                medString = "When? When when when?";
                break;
            }
            case 3: {
                medString = "It sure is dark.";
                break;
            }
            case 4: {
                medString = "Where does eternity begin?";
                break;
            }
            case 5: {
                medString = "Am I here, really?";
                break;
            }
            case 6: {
                medString = "What is this smell?";
                break;
            }
            case 7: {
                medString = "Isn't life really pretty disgusting?";
                break;
            }
            case 8: {
                medString = "Who built the mountains?";
                break;
            }
            case 9: {
                medString = "You consider whether you are supposed to be here.";
                break;
            }
            case 10: {
                medString = "Is all this a coincidence?";
                break;
            }
            case 11: {
                medString = "You try to fathom the number of grains of sand in the world.";
                break;
            }
            case 12: {
                medString = "Is this all part of a plan?";
                break;
            }
            case 13: {
                medString = "You try to figure out what your moral obligations are.";
                break;
            }
            case 14: {
                medString = "You think about how people can torture each other and who decides who will live and who will die.";
                break;
            }
            case 15: {
                medString = "The thought that someone is watching strikes you. ";
                break;
            }
            case 16: {
                medString = "You think of all the people that decide your fate.";
                break;
            }
            case 17: {
                medString = "A sense of hopelessness suddenly strikes.";
                break;
            }
            case 18: {
                medString = "You feel like laughing like a donkey.";
                break;
            }
            case 19: {
                medString = "You decide not to breathe at all. Does the future already exist?";
                break;
            }
            case 20: {
                medString = "You are beset by thoughts of demons.";
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    static String getMeditationStringForHatePath(int id) {
        String medString;
        switch (id) {
            case 0: {
                medString = "You wonder why you never got that gift.";
                break;
            }
            case 1: {
                medString = "Are they after you?";
                break;
            }
            case 2: {
                medString = "You think about how you can do most damage in the least time.";
                break;
            }
            case 3: {
                medString = "You think about how destruction gives control.";
                break;
            }
            case 4: {
                medString = "What is the effect of fear?";
                break;
            }
            case 5: {
                medString = "You wonder if you are afraid of something.";
                break;
            }
            case 6: {
                medString = "You think of armies gathering to crush the foe.";
                break;
            }
            case 7: {
                medString = "Where does your hate come from?";
                break;
            }
            case 8: {
                medString = "What is the purpose of your hate?";
                break;
            }
            case 9: {
                medString = "How can you make your hate grow?";
                break;
            }
            case 10: {
                medString = "What triggers your aggression?";
                break;
            }
            case 11: {
                medString = "How is aggression good for you?";
                break;
            }
            case 12: {
                medString = "What happens if you use violence?";
                break;
            }
            case 13: {
                medString = "Am I prepared to kill someone?";
                break;
            }
            case 14: {
                medString = "Do I want to replace my hate?";
                break;
            }
            case 15: {
                medString = "You wonder how you can use aggression.";
                break;
            }
            case 16: {
                medString = "You consider if hate really something is else.";
                break;
            }
            case 17: {
                medString = "Who controls my hate?";
                break;
            }
            case 18: {
                medString = "Who is in control of me?";
                break;
            }
            case 19: {
                medString = "You think of what your options today are.";
                break;
            }
            case 20: {
                medString = "You think of the faces of the people you hate or have hated.";
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    static String getMeditationStringForPowerPath(int id) {
        String medString;
        switch (id) {
            case 0: {
                medString = "You think of aggression and violence.";
                break;
            }
            case 1: {
                medString = "You envision fire along the horizon. Dark clouds above.";
                break;
            }
            case 2: {
                medString = "Who is more worthy of power? You or your neighbour?";
                break;
            }
            case 3: {
                medString = "What happens if all the bad people get all the gold?";
                break;
            }
            case 4: {
                medString = "Do I have responsibility to gain power?";
                break;
            }
            case 5: {
                medString = "What will happen to me if I never take control of my life?";
                break;
            }
            case 6: {
                medString = "You think 'Do plans ever work? Do I need a plan?'";
                break;
            }
            case 7: {
                medString = "How long plans can I make and do I wish to?";
                break;
            }
            case 8: {
                medString = "Why do some people become rich?";
                break;
            }
            case 9: {
                medString = "Do I want to be famous?";
                break;
            }
            case 10: {
                medString = "You think of heroes and whether you want to be one.";
                break;
            }
            case 11: {
                medString = "You think of ways to make your neighbour do as you wish.";
                break;
            }
            case 12: {
                medString = "You envision a straight path through a forest and a tunnel through a distant mountain.";
                break;
            }
            case 13: {
                medString = "You try to hear the thundering waves of a stormy sea.";
                break;
            }
            case 14: {
                medString = "You consider the speed and effects of lightning.";
                break;
            }
            case 15: {
                medString = "You meditate upon the power of the bear.";
                break;
            }
            case 16: {
                medString = "You think of the things you can not buy for money.";
                break;
            }
            case 17: {
                medString = "You think about why we use money.";
                break;
            }
            case 18: {
                medString = "You think about hurricanes and volcanoes and how it would feel to be one.";
                break;
            }
            case 19: {
                medString = "The image of a small baby surrounded by snakes comes to mind.";
                break;
            }
            case 20: {
                medString = "You feel full of energy, like the sun.";
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    static String getMeditationStringForLovePath(int id) {
        String medString;
        switch (id) {
            case 0: {
                medString = "You think about colorful flowers.";
                break;
            }
            case 1: {
                medString = "You recall the smile of a baby.";
                break;
            }
            case 2: {
                medString = "You wonder if you can punish with love.";
                break;
            }
            case 3: {
                medString = "Is it possible to kill for love?";
                break;
            }
            case 4: {
                medString = "Who loves the most?";
                break;
            }
            case 5: {
                medString = "What is love anyway?";
                break;
            }
            case 6: {
                medString = "Is love a feeling or a need?";
                break;
            }
            case 7: {
                medString = "If I give, do I do it for feeling good myself or for others?";
                break;
            }
            case 8: {
                medString = "The image of a sprout growing through hard dirt comes to mind.";
                break;
            }
            case 9: {
                medString = "The bliss of rain on a hot summer day.";
                break;
            }
            case 10: {
                medString = "You think about the worst person you have met.";
                break;
            }
            case 11: {
                medString = "You think about the most perfect person you have met.";
                break;
            }
            case 12: {
                medString = "When I forgive, do I really do it for myself?";
                break;
            }
            case 13: {
                medString = "Is letting go good or bad? Can I overcome my fears?";
                break;
            }
            case 14: {
                medString = "If I let my loved one be ruined, how could I live with myself?";
                break;
            }
            case 15: {
                medString = "Is love possible if I constantly fear rejection?";
                break;
            }
            case 16: {
                medString = "You think about soft words and whispers.";
                break;
            }
            case 17: {
                medString = "Is it right to try to control the one you love?";
                break;
            }
            case 18: {
                medString = "You think about how domination can destroy love and how some people want to be dominated.";
                break;
            }
            case 19: {
                medString = "Do people grow taller from love?";
                break;
            }
            case 20: {
                medString = "You wonder if the people in your hometown are like people in other towns.";
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    static String getMeditationStringForNoPath(int id) {
        String medString;
        switch (id) {
            case 0: {
                medString = "You consider the wind and the places it goes.";
                break;
            }
            case 1: {
                medString = "You feel the earth and what walks on it.";
                break;
            }
            case 2: {
                medString = "You think about the sea and the creatures that swim in it.";
                break;
            }
            case 3: {
                medString = "The light in a drop of water.";
                break;
            }
            case 4: {
                medString = "You think about the craftsmen, tinkering and toiling.";
                break;
            }
            case 5: {
                medString = "You ponder the miners, digging deep in the darkness of the mountains.";
                break;
            }
            case 6: {
                medString = "The sky lifts your heart.";
                break;
            }
            case 7: {
                medString = "You try to think of nothing here.";
                break;
            }
            case 8: {
                medString = "This place reminds you of home for some reason.";
                break;
            }
            case 9: {
                medString = "You try to recall all the faces you have met.";
                break;
            }
            case 10: {
                medString = "You think about rain.";
                break;
            }
            case 11: {
                medString = "You listen to the wind.";
                break;
            }
            case 12: {
                medString = "You think about sunshine.";
                break;
            }
            case 13: {
                medString = "You wonder how far you can go.";
                break;
            }
            case 14: {
                medString = "The thought of the highest mountain makes your mind wander.";
                break;
            }
            case 15: {
                medString = "The vision of slow flowing rivers calms your mind.";
                break;
            }
            case 16: {
                medString = "Is it really the same river the next time you step into it?";
                break;
            }
            case 17: {
                medString = "What makes trees grow?";
                break;
            }
            case 18: {
                medString = "Which path should you choose today?";
                break;
            }
            case 19: {
                medString = "You focus on breathing.";
                break;
            }
            case 20: {
                medString = "You try to clear your mind.";
                break;
            }
            case 21: {
                medString = "Is there a smallest particle?";
                break;
            }
            case 22: {
                medString = "What do the gods want?";
                break;
            }
            case 23: {
                medString = "What is your spirit made of?";
                break;
            }
            case 24: {
                medString = "Where does my soul live as I leave my body?";
                break;
            }
            case 25: {
                medString = "Is it possible to move along the chain of time?";
                break;
            }
            case 26: {
                medString = "Can you freeze water with thoughts?";
                break;
            }
            case 27: {
                medString = "Aren't most things very predictive?";
                break;
            }
            case 28: {
                medString = "Is there a physical representation of chance?";
                break;
            }
            case 29: {
                medString = "Does everything have a shadow somewhere?";
                break;
            }
            case 30: {
                medString = "If something sees light as something else, how does it look?";
                break;
            }
            case 31: {
                medString = "What if light and dark is the same thing, just in different shapes.";
                break;
            }
            case 32: {
                medString = "It sure is a nice day.";
                break;
            }
            case 33: {
                medString = "Is fear and hate the same thing?";
                break;
            }
            case 34: {
                medString = "Is love a large hole that needs to be filled or is it something else?";
                break;
            }
            case 35: {
                medString = "Who has the right to lock someone up?";
                break;
            }
            case 36: {
                medString = "Can you picture a situation where you could kill someone?";
                break;
            }
            case 37: {
                medString = "Are you all that honest?";
                break;
            }
            case 38: {
                medString = "When you judge and limit someone, does that limit you as well?";
                break;
            }
            case 39: {
                medString = "Do you believe in what people say or what people do?";
                break;
            }
            case 40: {
                medString = "Will you care about what happens after you are dead?";
                break;
            }
            case 41: {
                medString = "Does hurting someone really hurt you more?";
                break;
            }
            case 42: {
                medString = "Is it better to protect a child or let it risk a severe bruise now and then?";
                break;
            }
            case 43: {
                medString = "Can something good come from small children fighting with each other?";
                break;
            }
            case 44: {
                medString = "Should children really be allowed to sort out their indifferences themselves?";
                break;
            }
            case 45: {
                medString = "Are there people who are soulless?";
                break;
            }
            case 46: {
                medString = "Do you know anyone who has had a perfect childhood?";
                break;
            }
            case 47: {
                medString = "Maybe we all have our problems.";
                break;
            }
            case 48: {
                medString = "Who decides who is allowed to complain about something?";
                break;
            }
            case 49: {
                medString = "Do you taint people by complaining about things to them?";
                break;
            }
            case 50: {
                medString = "Does what you do have any effect at all?";
                break;
            }
            case 51: {
                medString = "What is the difference between words spoken and words thought?";
                break;
            }
            case 52: {
                medString = "What is the difference between an arrow fired and an arrow sheathed?";
                break;
            }
            case 53: {
                medString = "What is the smallest thing you can do that has effect on another person?";
                break;
            }
            case 54: {
                medString = "May your mere appearance change the course of events?";
                break;
            }
            case 55: {
                medString = "Should you accept when someone destroys what you love?";
                break;
            }
            case 56: {
                medString = "Do you let too many things slip?";
                break;
            }
            case 57: {
                medString = "Do you try to control things you should not or can not?";
                break;
            }
            case 58: {
                medString = "Can you change anything in your life for the better?";
                break;
            }
            case 59: {
                medString = "Does meditating really help anything?";
                break;
            }
            case 60: {
                medString = "Should you be doing something completely different right now?";
                break;
            }
            default: {
                medString = "You start meditating over recent events.";
            }
        }
        return medString;
    }

    public static final String getNameForLevel(byte path, byte level) {
        String name;
        switch (path) {
            case 2: {
                name = Cults.getNameForLevelForHatePath(level);
                break;
            }
            case 1: {
                name = Cults.getNameForLevelForLovePath(level);
                break;
            }
            case 3: {
                name = Cults.getNameForLevelForKnowledgePath(level);
                break;
            }
            case 5: {
                name = Cults.getNameForLevelForPowerPath(level);
                break;
            }
            case 4: {
                name = Cults.getNameForLevelForInsanityPath(level);
                break;
            }
            default: {
                name = "uninitiated";
            }
        }
        return name;
    }

    static String getNameForLevelForInsanityPath(byte level) {
        String name;
        if (level > 11) {
            name = level + "th Eidolon";
        } else {
            switch (level) {
                case 1: {
                    name = "Initiate";
                    break;
                }
                case 2: {
                    name = "Disturbed";
                    break;
                }
                case 3: {
                    name = "Crazed";
                    break;
                }
                case 4: {
                    name = "Deranged";
                    break;
                }
                case 5: {
                    name = "Sicko";
                    break;
                }
                case 6: {
                    name = "Mental";
                    break;
                }
                case 7: {
                    name = "Psycho";
                    break;
                }
                case 8: {
                    name = "Beast";
                    break;
                }
                case 9: {
                    name = "Maniac";
                    break;
                }
                case 10: {
                    name = "Drooling";
                    break;
                }
                case 11: {
                    name = "Gone";
                    break;
                }
                default: {
                    name = "uninitiated";
                }
            }
        }
        return name;
    }

    static String getNameForLevelForPowerPath(byte level) {
        String name;
        if (level > 11) {
            name = level + "th Sovereign";
        } else {
            switch (level) {
                case 1: {
                    name = "Initiate";
                    break;
                }
                case 2: {
                    name = "Gatherer";
                    break;
                }
                case 3: {
                    name = "Greedy";
                    break;
                }
                case 4: {
                    name = "Strong";
                    break;
                }
                case 5: {
                    name = "Released";
                    break;
                }
                case 6: {
                    name = "Unafraid";
                    break;
                }
                case 7: {
                    name = "Brave";
                    break;
                }
                case 8: {
                    name = "Performer";
                    break;
                }
                case 9: {
                    name = "Liberator";
                    break;
                }
                case 10: {
                    name = "Force";
                    break;
                }
                case 11: {
                    name = "Vibrant Light";
                    break;
                }
                default: {
                    name = "uninitiated";
                }
            }
        }
        return name;
    }

    static String getNameForLevelForKnowledgePath(byte level) {
        String name;
        if (level > 11) {
            name = level + "th Hierophant";
        } else {
            switch (level) {
                case 1: {
                    name = "Initiate";
                    break;
                }
                case 2: {
                    name = "Eager";
                    break;
                }
                case 3: {
                    name = "Explorer";
                    break;
                }
                case 4: {
                    name = "Sheetfolder";
                    break;
                }
                case 5: {
                    name = "Desertmind";
                    break;
                }
                case 6: {
                    name = "Observer";
                    break;
                }
                case 7: {
                    name = "Bookkeeper";
                    break;
                }
                case 8: {
                    name = "Mud-dweller";
                    break;
                }
                case 9: {
                    name = "Thought Eater";
                    break;
                }
                case 10: {
                    name = "Crooked";
                    break;
                }
                case 11: {
                    name = "Enlightened";
                    break;
                }
                default: {
                    name = "uninitiated";
                }
            }
        }
        return name;
    }

    static String getNameForLevelForLovePath(byte level) {
        String name;
        if (level > 11) {
            name = level + "th Deva";
        } else {
            switch (level) {
                case 1: {
                    name = "Initiate";
                    break;
                }
                case 2: {
                    name = "Nice";
                    break;
                }
                case 3: {
                    name = "Gentle";
                    break;
                }
                case 4: {
                    name = "Warm";
                    break;
                }
                case 5: {
                    name = "Goodhearted";
                    break;
                }
                case 6: {
                    name = "Giving";
                    break;
                }
                case 7: {
                    name = "Rock";
                    break;
                }
                case 8: {
                    name = "Splendid";
                    break;
                }
                case 9: {
                    name = "Protector";
                    break;
                }
                case 10: {
                    name = "Respectful";
                    break;
                }
                case 11: {
                    name = "Saint";
                    break;
                }
                default: {
                    name = "uninitiated";
                }
            }
        }
        return name;
    }

    static String getNameForLevelForHatePath(byte level) {
        String name;
        if (level > 11) {
            name = level + "th Harbinger";
        } else {
            switch (level) {
                case 1: {
                    name = "Initiate";
                    break;
                }
                case 2: {
                    name = "Ridiculous";
                    break;
                }
                case 3: {
                    name = "Envious";
                    break;
                }
                case 4: {
                    name = "Hateful";
                    break;
                }
                case 5: {
                    name = "Finger";
                    break;
                }
                case 6: {
                    name = "Sheep";
                    break;
                }
                case 7: {
                    name = "Snake";
                    break;
                }
                case 8: {
                    name = "Shark";
                    break;
                }
                case 9: {
                    name = "Infection";
                    break;
                }
                case 10: {
                    name = "Swarm";
                    break;
                }
                case 11: {
                    name = "Free";
                    break;
                }
                default: {
                    name = "uninitiated";
                }
            }
        }
        return name;
    }

    public static final String getQuestionForLevel(byte path, byte level) {
        String question = "Is this the question?";
        switch (path) {
            case 2: {
                question = Cults.getQuestionForLevelForHatePath(level);
                break;
            }
            case 3: {
                question = Cults.getQuestionForLevelForKnowledgePath(level);
                break;
            }
            case 5: {
                question = Cults.getQuestionForLevelForPowerPath(level);
                break;
            }
            case 4: {
                question = Cults.getQuestionForLevelForInsanityPath(level);
                break;
            }
            case 1: {
                question = Cults.getQuestionForLevelForLovePath(level);
                break;
            }
            default: {
                question = "Do you wish to follow a path?";
            }
        }
        return question;
    }

    static String getQuestionForLevelForHatePath(byte level) {
        String question;
        switch (level) {
            case 0: {
                question = "What is hate?";
                break;
            }
            case 1: {
                question = "What is the best thing that comes from hate?";
                break;
            }
            case 2: {
                question = "What is the worst thing that hate can bring?";
                break;
            }
            case 3: {
                question = "What type of hate is the weakest?";
                break;
            }
            case 4: {
                question = "How can I best strengthen my hate?";
                break;
            }
            case 5: {
                question = "What is the best use of my hate?";
                break;
            }
            case 6: {
                question = "Who is in control of my aggression?";
                break;
            }
            case 7: {
                question = "How do I best control my hate?";
                break;
            }
            case 8: {
                question = "What is the best effect of displaying aggression?";
                break;
            }
            case 9: {
                question = "When should I get rid of my hate?";
                break;
            }
            case 10: {
                question = "When have I mastered my hate?";
                break;
            }
            case 11: {
                question = "What is the most beneficial aspect of blind hate?";
                break;
            }
            case 12: {
                question = "Which is the most important enemy to strike down?";
                break;
            }
            case 13: {
                question = "Which of these is most successful in controlling any number of people?";
                break;
            }
            case 14: {
                question = "Which of these diminishes the power of hate most?";
                break;
            }
            case 15: {
                question = "What will increase the effect of your hate most?";
                break;
            }
            case 16: {
                question = "What is the difference between hate and anger?";
                break;
            }
            case 17: {
                question = "Which hate is the strongest?";
                break;
            }
            default: {
                question = "Is this the question?";
            }
        }
        return question;
    }

    static String getQuestionForLevelForKnowledgePath(byte level) {
        String question;
        switch (level) {
            case 0: {
                question = "What is knowledge?";
                break;
            }
            case 1: {
                question = "What best constitutes a fact?";
                break;
            }
            case 2: {
                question = "Is there any absolute truth?";
                break;
            }
            case 3: {
                question = "How do I best prepare myself to learn things?";
                break;
            }
            case 4: {
                question = "What is the most important product of knowledge?";
                break;
            }
            case 5: {
                question = "What constitutes a professional?";
                break;
            }
            case 6: {
                question = "Which of these is the most certain way to rise above the crowd?";
                break;
            }
            case 7: {
                question = "What do I do when I have too little knowledge to make the best choice?";
                break;
            }
            case 8: {
                question = "How important is knowledge of history?";
                break;
            }
            case 9: {
                question = "What is required to make good decisions?";
                break;
            }
            case 10: {
                question = "What will make my knowledge useless?";
                break;
            }
            case 11: {
                question = "Which of these is a requirement for truly working knowledge?";
                break;
            }
            case 12: {
                question = "What knowledge is most valuable?";
                break;
            }
            case 13: {
                question = "What is the best path to understanding a particular human behaviour?";
                break;
            }
            case 14: {
                question = "What is the best path to understanding a particular function in society?";
                break;
            }
            case 15: {
                question = "What is the ultimate purpose of gaining knowledge?";
                break;
            }
            case 16: {
                question = "Who of these has the most valuable knowledge?";
                break;
            }
            case 17: {
                question = "Why does society care about knowledge?";
                break;
            }
            default: {
                question = "Is this the question?";
            }
        }
        return question;
    }

    static String getQuestionForLevelForPowerPath(byte level) {
        String question;
        switch (level) {
            case 0: {
                question = "What is the ultimate purpose of power?";
                break;
            }
            case 1: {
                question = "What is money?";
                break;
            }
            case 2: {
                question = "In large amounts, what of these things is the most certain route to power for an unknown person?";
                break;
            }
            case 3: {
                question = "What is the main reason that people strive for power and money?";
                break;
            }
            case 4: {
                question = "What is the safest way of getting rid of your enemy?";
                break;
            }
            case 5: {
                question = "If power is a sword, what do I need to swing it?";
                break;
            }
            case 6: {
                question = "What is the biggest risk of my gaining power?";
                break;
            }
            case 7: {
                question = "How do I best control other people while releasing their energy?";
                break;
            }
            case 8: {
                question = "Will I loose my power if I am dishonest?";
                break;
            }
            case 9: {
                question = "Why am I in power?";
                break;
            }
            case 10: {
                question = "Which of these are blocked from achieving ultimate power?";
                break;
            }
            case 11: {
                question = "Which of these is the least working tactic when bringing someone down socially?";
                break;
            }
            case 12: {
                question = "Which is most powerful?";
                break;
            }
            case 13: {
                question = "How do you best assume control of a democracy?";
                break;
            }
            case 14: {
                question = "How do you best assume control of the whole world?";
                break;
            }
            case 15: {
                question = "How do you best control your partner?";
                break;
            }
            case 16: {
                question = "What is the main reason society works?";
                break;
            }
            case 17: {
                question = "What is the purpose of violence?";
                break;
            }
            default: {
                question = "Is this the question?";
            }
        }
        return question;
    }

    static String getQuestionForLevelForInsanityPath(byte level) {
        String question;
        switch (level) {
            case 0: {
                question = "How come some people are rich and some are poor?";
                break;
            }
            case 1: {
                question = "Who has most control of my fate?";
                break;
            }
            case 2: {
                question = "If I win the lottery, what determined it?";
                break;
            }
            case 3: {
                question = "A horse slips and kills the rider. What happened?";
                break;
            }
            case 4: {
                question = "Who of these are usually hiding something";
                break;
            }
            case 5: {
                question = "Do I have a free will?";
                break;
            }
            case 6: {
                question = "Must I treat others as I want to be treated myself?";
                break;
            }
            case 7: {
                question = "These roads, mines and houses are";
                break;
            }
            case 8: {
                question = "Who usually has most incentive to attack a normal person like me?";
                break;
            }
            case 9: {
                question = "What best describes humankind?";
                break;
            }
            case 10: {
                question = "What best describes my life?";
                break;
            }
            case 11: {
                question = "Who committed the crime?";
                break;
            }
            case 12: {
                question = "He worries about someone therefor he ___ the person";
                break;
            }
            case 13: {
                question = "The poor";
                break;
            }
            case 14: {
                question = "Good people will often";
                break;
            }
            case 15: {
                question = "You have no friends because you are";
                break;
            }
            case 16: {
                question = "You should always vote for";
                break;
            }
            case 17: {
                question = "It is better to";
                break;
            }
            default: {
                question = "Is this the question?";
            }
        }
        return question;
    }

    static String getQuestionForLevelForLovePath(byte level) {
        String question;
        switch (level) {
            case 0: {
                question = "What is love?";
                break;
            }
            case 1: {
                question = "Without which can love not survive?";
                break;
            }
            case 2: {
                question = "Which of these negates love?";
                break;
            }
            case 3: {
                question = "How do I love someone more?";
                break;
            }
            case 4: {
                question = "What makes love grow most?";
                break;
            }
            case 5: {
                question = "What should I say to best strengthen existing love?";
                break;
            }
            case 6: {
                question = "What is the best reason to ask someone I love to change?";
                break;
            }
            case 7: {
                question = "What do I need to do to love everyone?";
                break;
            }
            case 8: {
                question = "Which of these is the biggest failure in love?";
                break;
            }
            case 9: {
                question = "What of these will ultimately make love fail?";
                break;
            }
            case 10: {
                question = "What is the biggest act of love?";
                break;
            }
            case 11: {
                question = "Are there people who can love you no matter what?";
                break;
            }
            case 12: {
                question = "What do I really have to fight in myself in order to be a loving person?";
                break;
            }
            case 13: {
                question = "What best describes a business?";
                break;
            }
            case 14: {
                question = "What best describes society?";
                break;
            }
            case 15: {
                question = "Being which of these gives you least friends?";
                break;
            }
            case 16: {
                question = "Which of these will always block love?";
                break;
            }
            case 17: {
                question = "Which of these is a side effect of love?";
                break;
            }
            default: {
                question = "Is this the question?";
            }
        }
        return question;
    }

    public static final String[] getAnswerAlternativesForLevel(byte path, byte level) {
        String[] alts;
        switch (path) {
            case 4: {
                alts = Cults.getAnswerAlternativesForLevelForInsanityPath(level);
                break;
            }
            case 5: {
                alts = Cults.getAnswerAlternativesForLevelForPowerPath(level);
                break;
            }
            case 1: {
                alts = Cults.getAnswerAlternativesForLevelForLovePath(level);
                break;
            }
            case 2: {
                alts = Cults.getAnswerAlternativesForLevelForHatePath(level);
                break;
            }
            case 3: {
                alts = Cults.getAnswerAlternativesForLevelForKnowledgePath(level);
                break;
            }
            default: {
                alts = YES_NO_STRINGS;
            }
        }
        return alts;
    }

    static String[] getAnswerAlternativesForLevelForInsanityPath(byte level) {
        String[] alts;
        switch (level) {
            case 0: {
                alts = new String[]{"It is natural", "Life is unfair", "Some people are unlucky", "There are not enough resources", "Who knows?"};
                break;
            }
            case 1: {
                alts = new String[]{"I have", "People around me", "The stars", "The natural laws", "The government"};
                break;
            }
            case 2: {
                alts = new String[]{"The number on the ticket", "The person who pulled the ticket", "Simple luck", "The events leading up to the draw", "World history"};
                break;
            }
            case 3: {
                alts = new String[]{"World history lead to this", "Chance struck", "The situation called for it", "Nothing unusual", "Natural laws"};
                break;
            }
            case 4: {
                alts = new String[]{"A loud person", "A hostile person", "A silent person", "A happy person", "A sad person"};
                break;
            }
            case 5: {
                alts = new String[]{"Yes", "No", "I will answer some other day", "Maybe", "I refuse to answer"};
                break;
            }
            case 6: {
                alts = new String[]{"No", "Sometimes I should", "Usually I should", "It works best for me", "Always"};
                break;
            }
            case 7: {
                alts = new String[]{"Beautiful", "Natural", "Detestable", "An abomination", "Basically unnatural"};
                break;
            }
            case 8: {
                alts = new String[]{"The mother", "A sister", "A neighbour", "The government", "A mob"};
                break;
            }
            case 9: {
                alts = new String[]{"Animals", "Evil", "Good", "Natural", "Destructive"};
                break;
            }
            case 10: {
                alts = new String[]{"A bit of a success", "Totally meaningless", "At least I am alive", "Fun", "Tragic"};
                break;
            }
            case 11: {
                alts = new String[]{"The ex convict", "The whore", "The foreigner", "The poor guy", "The rich guy"};
                break;
            }
            case 12: {
                alts = new String[]{"cares about", "loathes", "diminishes", "upsets", "lies about"};
                break;
            }
            case 13: {
                alts = new String[]{"are more envious and dangerous", "are more common and popular", "don't have to be protected", "are more trustworthy", "have more to lose"};
                break;
            }
            case 14: {
                alts = new String[]{"help you", "hurt you", "love you", "ignore you", "pay you"};
                break;
            }
            case 15: {
                alts = new String[]{"ill", "shy", "superior", "dangerous", "annoying"};
                break;
            }
            case 16: {
                alts = new String[]{"what benefits everyone else most", "your own idea", "someone elses idea", "what benefits you most", "nothing"};
                break;
            }
            case 17: {
                alts = new String[]{"stick to your principles", "sugarcoat", "pay your friends off", "annoy your friends", "party"};
                break;
            }
            default: {
                alts = YES_NO_STRINGS;
            }
        }
        return alts;
    }

    static String[] getAnswerAlternativesForLevelForPowerPath(byte level) {
        String[] alts;
        switch (level) {
            case 0: {
                alts = new String[]{"To become insanely rich", "To dominate other creatures", "To get away with everything", "To enjoy the small things in life", "To achieve personal freedom"};
                break;
            }
            case 1: {
                alts = new String[]{"Money is mainly control", "You can buy everything with money", "Money is mainly oppression", "Money is mainly energy", "Money is only numbers"};
                break;
            }
            case 2: {
                alts = new String[]{"Fear", "Money", "Knowledge", "Love", "Cooperation"};
                break;
            }
            case 3: {
                alts = new String[]{"To push down their competitors", "To gain control over their lives", "To control others", "To become rich", "They act like animals"};
                break;
            }
            case 4: {
                alts = new String[]{"Infiltrating them", "Harassing them", "Buying them", "Conquer them", "Letting them destroy themselves"};
                break;
            }
            case 5: {
                alts = new String[]{"Intelligence", "Faith", "Courage", "Hate", "Money"};
                break;
            }
            case 6: {
                alts = new String[]{"That I become a coward", "That I hurt someone", "That I go silent", "That I waste resources", "That I stop listening"};
                break;
            }
            case 7: {
                alts = new String[]{"Remove their feeling of self sufficiency", "Terrorize them", "Give them what they want", "Give them limited power", "Ignore them"};
                break;
            }
            case 8: {
                alts = new String[]{"Usually not", "Never", "Most often", "Always", "There is no such thing as dishonesty"};
                break;
            }
            case 9: {
                alts = new String[]{"I was appointed", "I am lucky", "I am better", "I paid for it", "I took it"};
                break;
            }
            case 10: {
                alts = new String[]{"Someone stupid", "A coward", "Someone nice", "A murderer", "None"};
                break;
            }
            case 11: {
                alts = new String[]{"Telling poisonous jokes about the person", "Spreading big lies about the person", "Spreading small lies about the person", "Showing how you worry about the person", "Ridiculing the person"};
                break;
            }
            case 12: {
                alts = new String[]{"Being able to kill", "Controlling people", "Being very rich", "Being free of riches and dependancies", "Being impervious to personal attacks"};
                break;
            }
            case 13: {
                alts = new String[]{"Build a system of coherent ideology that can't be refuted", "Prey on peoples inner fears", "Use weapons and money", "Run for office using a reasonable philosophic base with solutions to contemporary problems", "Run for office vocalizing single popular issues"};
                break;
            }
            case 14: {
                alts = new String[]{"Use your own country to conquer it by force", "Buy country after country", "Assume control of some super-state organization and increase its influence", "Join beings from outside of the world", "Use religion"};
                break;
            }
            case 15: {
                alts = new String[]{"More Money", "Igniting Love", "Overwhelming Power", "Secret Knowledge", "Superior Sex"};
                break;
            }
            case 16: {
                alts = new String[]{"Monopoly of Violence", "Secure Trade", "Coordination of Efforts", "Accumulation of Knowledge", "Taxes"};
                break;
            }
            case 17: {
                alts = new String[]{"To impress", "There is no reason", "To extract information", "To achieve a purpose", "To diminish"};
                break;
            }
            default: {
                alts = YES_NO_STRINGS;
            }
        }
        return alts;
    }

    static String[] getAnswerAlternativesForLevelForLovePath(byte level) {
        String[] alts;
        switch (level) {
            case 0: {
                alts = new String[]{"It is a spring", "It is a hole", "It is a shackle", "It is fragile glass", "It is a sword"};
                break;
            }
            case 1: {
                alts = new String[]{"Trust", "Respect", "Hate", "Death", "Beauty"};
                break;
            }
            case 2: {
                alts = new String[]{"Power", "Desire", "Hate", "Contempt", "Distrust"};
                break;
            }
            case 3: {
                alts = new String[]{"I hate", "I demand", "I accept", "I desire", "I pursue"};
                break;
            }
            case 4: {
                alts = new String[]{"Power", "Gifts", "Fear", "Time", "Kisses"};
                break;
            }
            case 5: {
                alts = new String[]{"I respect you", "I hate you", "I need you", "I want you", "I love you"};
                break;
            }
            case 6: {
                alts = new String[]{"To destroy the relationship", "To preserve balance", "To make the person grow", "To gain independence", "To assert control"};
                break;
            }
            case 7: {
                alts = new String[]{"Test everyone", "Know everyone", "Distrust everyone", "Hate everyone", "Accept humankind"};
                break;
            }
            case 8: {
                alts = new String[]{"Not protecting your love", "Stealing from your love", "Accusing your love", "Hiding from your love", "Testing your love"};
                break;
            }
            case 9: {
                alts = new String[]{"Lack of money", "Lack of desire", "Lack of patience", "Lack of hate", "Lack of time"};
                break;
            }
            case 10: {
                alts = new String[]{"Incarcerating", "Setting free", "Giving everything", "Killing to protect", "Forgiving"};
                break;
            }
            case 11: {
                alts = new String[]{"Not if you hurt other people", "Not if you are really disgusting", "Only if you give them respect", "Yes. There are such people", "No that is impossible"};
                break;
            }
            case 12: {
                alts = new String[]{"Fear", "Megalomania", "Envy", "Pride", "Control"};
                break;
            }
            case 13: {
                alts = new String[]{"It represents a way of taking money from people", "It provides a service in much need", "It exists in order to distribute wealth", "It is an organisation driven by well-meaning people", "It is beneficial for everyone"};
                break;
            }
            case 14: {
                alts = new String[]{"Organisation forced upon the individual", "A way for people to cooperate in order to reduce overall pain", "A way for people to cooperate in order to promote leaders", "A way for people to cooperate in order to make everyone rich", "A way for people to cooperate in order to promote individuals"};
                break;
            }
            case 15: {
                alts = new String[]{"poor", "sick", "superior", "loving", "annoying"};
                break;
            }
            case 16: {
                alts = new String[]{"pride", "jealousy", "fear", "hate", "principles"};
                break;
            }
            case 17: {
                alts = new String[]{"vulnerability", "strength", "joy", "anger", "jealousy"};
                break;
            }
            default: {
                alts = YES_NO_STRINGS;
            }
        }
        return alts;
    }

    static String[] getAnswerAlternativesForLevelForHatePath(byte level) {
        String[] alts;
        switch (level) {
            case 0: {
                alts = new String[]{"Something ethereal", "A fist", "An emotion", "A need", "A dog"};
                break;
            }
            case 1: {
                alts = new String[]{"Death", "Energy", "Hate", "Beauty", "Action"};
                break;
            }
            case 2: {
                alts = new String[]{"It can kill me", "It can hurt someone I care about", "It takes a lot of time", "It drains energy", "It is destructive"};
                break;
            }
            case 3: {
                alts = new String[]{"Hate based on fear", "Hate based on facts", "Hate based on pain", "Hate based on envy", "Hate based on insults"};
                break;
            }
            case 4: {
                alts = new String[]{"By pretending", "By hating", "By killing", "By becoming stupid", "By finding proof"};
                break;
            }
            case 5: {
                alts = new String[]{"To kill my enemies", "To turn it into love", "To remove inner pressure", "To have fun", "To resolve my problems"};
                break;
            }
            case 6: {
                alts = new String[]{"I am", "The people who watch me", "Anyone who knows about it", "The rulers", "My enemies"};
                break;
            }
            case 7: {
                alts = new String[]{"With the truth", "With emotions", "With hate", "With force", "By letting it out"};
                break;
            }
            case 8: {
                alts = new String[]{"Violence", "Fear", "Confusion", "Disruption", "Love"};
                break;
            }
            case 9: {
                alts = new String[]{"No", "Yes", "If I hurt my friends", "If I loose out", "If I fail to hurt my enemy"};
                break;
            }
            case 10: {
                alts = new String[]{"When my enemies are gone", "When I have power and knowledge to use it", "When I am calm inside", "When I can loose it at will", "Nobody can"};
                break;
            }
            case 11: {
                alts = new String[]{"It lets you survive by striking first", "You scare people away and gets left alone", "Others will give you power and coins", "Nobody will stop you and you can take what you want", "You will have nothing left to tie your soul down"};
                break;
            }
            case 12: {
                alts = new String[]{"The enemy which doesn't pay me", "The enemy which tries to like me", "The enemy in my head", "The enemy on the way to the real enemy", "The enemy who confronts me with words"};
                break;
            }
            case 13: {
                alts = new String[]{"Abnormal beauty", "Physical strength", "Extreme intelligence", "Masterful psychology", "Random violence"};
                break;
            }
            case 14: {
                alts = new String[]{"Believing in the concept of an afterlife", "Repeated compassion shown by another human being", "Believing in the concept of a saint", "Being saved to life by someone", "Being left alone for a long time"};
                break;
            }
            case 15: {
                alts = new String[]{"Repetition", "Evilness", "Exact and overwhelming force", "Ruthlessness", "People who believe in you"};
                break;
            }
            case 16: {
                alts = new String[]{"Fear", "Loathing", "Action", "Love", "The person"};
                break;
            }
            case 17: {
                alts = new String[]{"Envy", "Feeling of injustice", "Fear", "Wrath", "Bad upbringing"};
                break;
            }
            default: {
                alts = YES_NO_STRINGS;
            }
        }
        return alts;
    }

    static String[] getAnswerAlternativesForLevelForKnowledgePath(byte level) {
        String[] alts;
        switch (level) {
            case 0: {
                alts = new String[]{"Everything I have seen", "Lies are the truth", "Commonly accepted facts", "Information that I have accepted as a fact", "Applied experience"};
                break;
            }
            case 1: {
                alts = new String[]{"Experience", "Anything I accept as the truth", "Logically verified information", "I trust nothing", "What a lot of people say"};
                break;
            }
            case 2: {
                alts = new String[]{"Everything we experience is true", "Only what I decide to see within me", "No", "It is a joke", "What people decide"};
                break;
            }
            case 3: {
                alts = new String[]{"I accept nothing as fact", "I test facts the first time I encounter them", "I only accept facts from my own experiences", "I do not trust my senses but other sources of information", "I initially accept everything as fact and try to constantly verify it"};
                break;
            }
            case 4: {
                alts = new String[]{"Fearlessness", "Doubt", "Psychological strength", "Wealth", "Humility"};
                break;
            }
            case 5: {
                alts = new String[]{"Someone who knows a lot", "Someone who is paid", "Someone who is extremely skillful", "Someone who knows a bit more than average", "Someone worthy of respect"};
                break;
            }
            case 6: {
                alts = new String[]{"To really learn the details", "To buy new clothes", "To relax and take it easy", "To know a little about everything", "To talk to friends"};
                break;
            }
            case 7: {
                alts = new String[]{"Always gather more facts", "Usually you could toss a coin", "Wait and see", "Let someone else decide", "Look for a sign"};
                break;
            }
            case 8: {
                alts = new String[]{"It can help", "It is very important", "You should not be allowed to make decisions without it", "The history is useless", "I don't know"};
                break;
            }
            case 9: {
                alts = new String[]{"A brave heart", "Fear of making an error", "Luck", "Fairly logical thinking", "More knowledge"};
                break;
            }
            case 10: {
                alts = new String[]{"Being wrong", "Being right", "Inactivity", "Making too many decisions", "Killing someone innocent"};
                break;
            }
            case 11: {
                alts = new String[]{"That there is nothingness", "That there are no people around to interfere with the results", "That there is no randomness in the world", "That I like the results", "That I have the acceptance of my peers"};
                break;
            }
            case 12: {
                alts = new String[]{"To see what it is that you actually do to the world and the people around you", "To understand how the fabric of the world is constructed", "To understand how man-made things are constructed", "To accept what others are trying to teach you", "To see things from other peoples perspective"};
                break;
            }
            case 13: {
                alts = new String[]{"Thinking in new paths", "Asking the person to explain his or her intentions", "By understanding how the behaviour is supposed to benefit the individual in question", "Meeting other types of people", "Trying to relax and focus"};
                break;
            }
            case 14: {
                alts = new String[]{"Thinking outside the box", "Asking the government to improve in explaining the benefits", "Learning other related things", "Trying to relax and focus", "By investigating how the function is supposed to benefit everyone"};
                break;
            }
            case 15: {
                alts = new String[]{"To gain status", "To achieve happiness", "To gain control", "To further humanity", "To learn from history"};
                break;
            }
            case 16: {
                alts = new String[]{"The Priest", "The Expert", "The Professor", "The Fool", "The Lover"};
                break;
            }
            case 17: {
                alts = new String[]{"To gain status", "To achieve happiness", "To gain control", "To survive", "To become rich"};
                break;
            }
            default: {
                alts = YES_NO_STRINGS;
            }
        }
        return alts;
    }

    public static final String getWrongAnswerStringForLevel(byte path, byte level) {
        String wrongAnswer;
        switch (path) {
            case 5: {
                wrongAnswer = Cults.getWrongAnswerStringForLevelForPowerPath(level);
                break;
            }
            case 4: {
                wrongAnswer = Cults.getWrongAnswerStringForLevelForInsanityPath(level);
                break;
            }
            case 1: {
                wrongAnswer = Cults.getWrongAnswerStringForLevelForLovePath(level);
                break;
            }
            case 2: {
                wrongAnswer = Cults.getWrongAnswerStringForLevelForHatePath(level);
                break;
            }
            case 3: {
                wrongAnswer = Cults.getWrongAnswerStringForLevelForKnowledgePath(level);
                break;
            }
            default: {
                wrongAnswer = "You feel no revelation. You have to rethink.";
            }
        }
        return wrongAnswer;
    }

    static String getWrongAnswerStringForLevelForPowerPath(byte level) {
        String wrongAnswer;
        switch (level) {
            case 0: {
                wrongAnswer = "You need something more clear to light the path.";
                break;
            }
            case 1: {
                wrongAnswer = "Hmm. You feel no revelation.";
                break;
            }
            case 2: {
                wrongAnswer = "There must be a safer route.";
                break;
            }
            case 3: {
                wrongAnswer = "Hmm, is that really so?";
                break;
            }
            case 4: {
                wrongAnswer = "That does not feel like a clean solution.";
                break;
            }
            case 5: {
                wrongAnswer = "Hmm. That's not required.";
                break;
            }
            case 6: {
                wrongAnswer = "That answer doesn't quite cut it.";
                break;
            }
            case 7: {
                wrongAnswer = "There must be a better way.";
                break;
            }
            case 8: {
                wrongAnswer = "You consider the situations you have witnessed and see flaws in your reasoning.";
                break;
            }
            case 9: {
                wrongAnswer = "Somehow you doubt that it is really the reason.";
                break;
            }
            case 10: {
                wrongAnswer = "Hasn't that happened?";
                break;
            }
            case 11: {
                wrongAnswer = "Not effective enough.";
                break;
            }
            case 12: {
                wrongAnswer = "Too blunt a weapon.";
                break;
            }
            case 13: {
                wrongAnswer = "That can't be the best way.";
                break;
            }
            case 14: {
                wrongAnswer = "That plan has many flaws.";
                break;
            }
            case 15: {
                wrongAnswer = "Maybe there is something more powerful.";
                break;
            }
            case 16: {
                wrongAnswer = "Even without that, society can exist.";
                break;
            }
            case 17: {
                wrongAnswer = "Not quite.";
                break;
            }
            default: {
                wrongAnswer = "You felt no revelation. You will have to reconsider.";
            }
        }
        return wrongAnswer;
    }

    static String getWrongAnswerStringForLevelForInsanityPath(byte level) {
        String wrongAnswer;
        switch (level) {
            case 0: {
                wrongAnswer = "Too sane.";
                break;
            }
            case 1: {
                wrongAnswer = "Hmm. That's not insane.";
                break;
            }
            case 2: {
                wrongAnswer = "That makes too much sense.";
                break;
            }
            case 3: {
                wrongAnswer = "A normal answer.";
                break;
            }
            case 4: {
                wrongAnswer = "Too sane.";
                break;
            }
            case 5: {
                wrongAnswer = "Hmm. That's not insane.";
                break;
            }
            case 6: {
                wrongAnswer = "A normal answer.";
                break;
            }
            case 7: {
                wrongAnswer = "Too sane.";
                break;
            }
            case 8: {
                wrongAnswer = "Nothing unusual about that answer.";
                break;
            }
            case 9: {
                wrongAnswer = "You feel a weird sensation. Sanity at last?";
                break;
            }
            case 10: {
                wrongAnswer = "You think too clearly. Maybe you are not worthy?";
                break;
            }
            case 11: {
                wrongAnswer = "Where's your problem now?";
                break;
            }
            case 12: {
                wrongAnswer = "Too sane.";
                break;
            }
            case 13: {
                wrongAnswer = "You don't feel that it is a fair definition.";
                break;
            }
            case 14: {
                wrongAnswer = "Hmm. That definition feels too blunt.";
                break;
            }
            case 15: {
                wrongAnswer = "Maybe you are that actually...";
                break;
            }
            case 16: {
                wrongAnswer = "Isn't it obvious that..";
                break;
            }
            case 17: {
                wrongAnswer = "You are better than that!";
                break;
            }
            default: {
                wrongAnswer = "You felt no revelation. You will have to reconsider.";
            }
        }
        return wrongAnswer;
    }

    static String getWrongAnswerStringForLevelForLovePath(byte level) {
        String wrongAnswer;
        switch (level) {
            case 0: {
                wrongAnswer = "You don't feel any revelation. Not quite the answer.";
                break;
            }
            case 1: {
                wrongAnswer = "Hmm. Maybe love does not require that?";
                break;
            }
            case 2: {
                wrongAnswer = "Isn't that a common problem when it comes to love?";
                break;
            }
            case 3: {
                wrongAnswer = "No that can't be it. The path forward is easier.";
                break;
            }
            case 4: {
                wrongAnswer = "That answer maybe was too obvious. You figure you did not challenge yourself enough.";
                break;
            }
            case 5: {
                wrongAnswer = "There must be something better.";
                break;
            }
            case 6: {
                wrongAnswer = "Does that benefit both of you most?";
                break;
            }
            case 7: {
                wrongAnswer = "You ask yourself 'Is that even possible?'";
                break;
            }
            case 8: {
                wrongAnswer = "You suspect that there is something worse.";
                break;
            }
            case 9: {
                wrongAnswer = "You can probably love something without that.";
                break;
            }
            case 10: {
                wrongAnswer = "Wouldn't you usually do that for your own benefit?";
                break;
            }
            case 11: {
                wrongAnswer = "Something feels wrong with that answer.";
                break;
            }
            case 12: {
                wrongAnswer = "Something feels wrong with that answer.";
                break;
            }
            case 13: {
                wrongAnswer = "Are you really worthy?";
                break;
            }
            case 14: {
                wrongAnswer = "Did you answer with love?";
                break;
            }
            case 15: {
                wrongAnswer = "What is the ne?";
                break;
            }
            case 16: {
                wrongAnswer = "One thing will block more";
                break;
            }
            case 17: {
                wrongAnswer = "Something else happens as well";
                break;
            }
            default: {
                wrongAnswer = "You felt no revelation. You will have to reconsider.";
            }
        }
        return wrongAnswer;
    }

    static String getWrongAnswerStringForLevelForHatePath(byte level) {
        String wrongAnswer;
        switch (level) {
            case 0: {
                wrongAnswer = "It would seem so but you feel that it is more powerful.";
                break;
            }
            case 1: {
                wrongAnswer = "That is good but you suspect that there is something better.";
                break;
            }
            case 2: {
                wrongAnswer = "Something must be worse than that.";
                break;
            }
            case 3: {
                wrongAnswer = "'But does that go away so easily?', you think.";
                break;
            }
            case 4: {
                wrongAnswer = "'Does that really help?', you wonder.";
                break;
            }
            case 5: {
                wrongAnswer = "That doesn't seem powerful enough.";
                break;
            }
            case 6: {
                wrongAnswer = "After all, are they that bad of a threat?";
                break;
            }
            case 7: {
                wrongAnswer = "But does that really constitute control?";
                break;
            }
            case 8: {
                wrongAnswer = "You feel that there is something more powerful.";
                break;
            }
            case 9: {
                wrongAnswer = "Maybe not.";
                break;
            }
            case 10: {
                wrongAnswer = "Is that absolute power?";
                break;
            }
            case 11: {
                wrongAnswer = "It must be more profound than that.";
                break;
            }
            case 12: {
                wrongAnswer = "That suddenly sounds like semantics to you.";
                break;
            }
            case 13: {
                wrongAnswer = "There is something even better.";
                break;
            }
            case 14: {
                wrongAnswer = "Something that removes the core.";
                break;
            }
            case 15: {
                wrongAnswer = "Not powerful enough.";
                break;
            }
            case 16: {
                wrongAnswer = "There is a definite borderline.";
                break;
            }
            case 17: {
                wrongAnswer = "Everyone can hate.";
                break;
            }
            default: {
                wrongAnswer = "You felt no revelation. You will have to reconsider.";
            }
        }
        return wrongAnswer;
    }

    static String getWrongAnswerStringForLevelForKnowledgePath(byte level) {
        String wrongAnswer;
        switch (level) {
            case 0: {
                wrongAnswer = "You don't feel that sense of revelation. The answer is probably too obvious.";
                break;
            }
            case 1: {
                wrongAnswer = "You feel that the answer contains holes.";
                break;
            }
            case 2: {
                wrongAnswer = "You understand that you need to challenge your beliefs more in order to evolve.";
                break;
            }
            case 3: {
                wrongAnswer = "That does not help you.";
                break;
            }
            case 4: {
                wrongAnswer = "You suspect that there is something greater.";
                break;
            }
            case 5: {
                wrongAnswer = "Somehow, that doesn't seem powerful enough.";
                break;
            }
            case 6: {
                wrongAnswer = "You wonder if that would really help.";
                break;
            }
            case 7: {
                wrongAnswer = "Come to think about it, is that the best way forward?";
                break;
            }
            case 8: {
                wrongAnswer = "Is that so?";
                break;
            }
            case 9: {
                wrongAnswer = "Then again, is that fruitful?";
                break;
            }
            case 10: {
                wrongAnswer = "Something feels wrong with that answer.";
                break;
            }
            case 11: {
                wrongAnswer = "It must be something absolute.";
                break;
            }
            case 12: {
                wrongAnswer = "Something feels wrong with that answer.";
                break;
            }
            case 13: {
                wrongAnswer = "People may tell you that.";
                break;
            }
            case 14: {
                wrongAnswer = "You are not getting to the core.";
                break;
            }
            case 15: {
                wrongAnswer = "Maybe there is something even more important.";
                break;
            }
            case 16: {
                wrongAnswer = "Without them there is nothing.";
                break;
            }
            case 17: {
                wrongAnswer = "The only reason, really.";
                break;
            }
            default: {
                wrongAnswer = "You felt no revelation. You will have to reconsider.";
            }
        }
        return wrongAnswer;
    }

    public static final int getCorrectAnswerForNextLevel(byte path, byte level) {
        int answerForNextLevel;
        switch (path) {
            case 5: {
                answerForNextLevel = Cults.getCorrectAnswerForNextLevelForPowerPath(level);
                break;
            }
            case 4: {
                answerForNextLevel = Cults.getCorrectAnswerForNextLevelForInsanityPath(level);
                break;
            }
            case 1: {
                answerForNextLevel = Cults.getCorrectAnswerForNextLevelForLovePath(level);
                break;
            }
            case 2: {
                answerForNextLevel = Cults.getCorrectAnswerForNextLevelForHatePath(level);
                break;
            }
            case 3: {
                answerForNextLevel = Cults.getCorrectAnswerForNextLevelForKnowledgePath(level);
                break;
            }
            default: {
                answerForNextLevel = 0;
            }
        }
        return answerForNextLevel;
    }

    static int getCorrectAnswerForNextLevelForPowerPath(byte level) {
        int answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = 4;
                break;
            }
            case 1: {
                answerForNextLevel = 3;
                break;
            }
            case 2: {
                answerForNextLevel = 1;
                break;
            }
            case 3: {
                answerForNextLevel = 1;
                break;
            }
            case 4: {
                answerForNextLevel = 3;
                break;
            }
            case 5: {
                answerForNextLevel = 2;
                break;
            }
            case 6: {
                answerForNextLevel = 0;
                break;
            }
            case 7: {
                answerForNextLevel = 3;
                break;
            }
            case 8: {
                answerForNextLevel = 0;
                break;
            }
            case 9: {
                answerForNextLevel = 2;
                break;
            }
            case 10: {
                answerForNextLevel = 4;
                break;
            }
            case 11: {
                answerForNextLevel = 4;
                break;
            }
            case 12: {
                answerForNextLevel = 2;
                break;
            }
            case 13: {
                answerForNextLevel = 3;
                break;
            }
            case 14: {
                answerForNextLevel = 2;
                break;
            }
            case 15: {
                answerForNextLevel = 1;
                break;
            }
            case 16: {
                answerForNextLevel = 0;
                break;
            }
            case 17: {
                answerForNextLevel = 3;
                break;
            }
            default: {
                answerForNextLevel = 0;
            }
        }
        return answerForNextLevel;
    }

    static int getCorrectAnswerForNextLevelForInsanityPath(byte level) {
        int answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = 1;
                break;
            }
            case 1: {
                answerForNextLevel = 2;
                break;
            }
            case 2: {
                answerForNextLevel = 2;
                break;
            }
            case 3: {
                answerForNextLevel = 1;
                break;
            }
            case 4: {
                answerForNextLevel = 2;
                break;
            }
            case 5: {
                answerForNextLevel = 0;
                break;
            }
            case 6: {
                answerForNextLevel = 4;
                break;
            }
            case 7: {
                answerForNextLevel = 4;
                break;
            }
            case 8: {
                answerForNextLevel = 3;
                break;
            }
            case 9: {
                answerForNextLevel = 1;
                break;
            }
            case 10: {
                answerForNextLevel = 1;
                break;
            }
            case 11: {
                answerForNextLevel = 4;
                break;
            }
            case 12: {
                answerForNextLevel = 0;
                break;
            }
            case 13: {
                answerForNextLevel = 3;
                break;
            }
            case 14: {
                answerForNextLevel = 2;
                break;
            }
            case 15: {
                answerForNextLevel = 2;
                break;
            }
            case 16: {
                answerForNextLevel = 3;
                break;
            }
            case 17: {
                answerForNextLevel = 0;
                break;
            }
            default: {
                answerForNextLevel = 0;
            }
        }
        return answerForNextLevel;
    }

    static int getCorrectAnswerForNextLevelForLovePath(byte level) {
        int answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = 0;
                break;
            }
            case 1: {
                answerForNextLevel = 1;
                break;
            }
            case 2: {
                answerForNextLevel = 3;
                break;
            }
            case 3: {
                answerForNextLevel = 2;
                break;
            }
            case 4: {
                answerForNextLevel = 3;
                break;
            }
            case 5: {
                answerForNextLevel = 4;
                break;
            }
            case 6: {
                answerForNextLevel = 1;
                break;
            }
            case 7: {
                answerForNextLevel = 4;
                break;
            }
            case 8: {
                answerForNextLevel = 0;
                break;
            }
            case 9: {
                answerForNextLevel = 2;
                break;
            }
            case 10: {
                answerForNextLevel = 1;
                break;
            }
            case 11: {
                answerForNextLevel = 3;
                break;
            }
            case 12: {
                answerForNextLevel = 2;
                break;
            }
            case 13: {
                answerForNextLevel = 1;
                break;
            }
            case 14: {
                answerForNextLevel = 1;
                break;
            }
            case 15: {
                answerForNextLevel = 4;
                break;
            }
            case 16: {
                answerForNextLevel = 4;
                break;
            }
            case 17: {
                answerForNextLevel = 0;
                break;
            }
            default: {
                answerForNextLevel = 0;
            }
        }
        return answerForNextLevel;
    }

    static int getCorrectAnswerForNextLevelForHatePath(byte level) {
        int answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = 3;
                break;
            }
            case 1: {
                answerForNextLevel = 1;
                break;
            }
            case 2: {
                answerForNextLevel = 0;
                break;
            }
            case 3: {
                answerForNextLevel = 0;
                break;
            }
            case 4: {
                answerForNextLevel = 4;
                break;
            }
            case 5: {
                answerForNextLevel = 4;
                break;
            }
            case 6: {
                answerForNextLevel = 2;
                break;
            }
            case 7: {
                answerForNextLevel = 0;
                break;
            }
            case 8: {
                answerForNextLevel = 1;
                break;
            }
            case 9: {
                answerForNextLevel = 3;
                break;
            }
            case 10: {
                answerForNextLevel = 3;
                break;
            }
            case 11: {
                answerForNextLevel = 0;
                break;
            }
            case 12: {
                answerForNextLevel = 1;
                break;
            }
            case 13: {
                answerForNextLevel = 3;
                break;
            }
            case 14: {
                answerForNextLevel = 0;
                break;
            }
            case 15: {
                answerForNextLevel = 4;
                break;
            }
            case 16: {
                answerForNextLevel = 2;
                break;
            }
            case 17: {
                answerForNextLevel = 1;
                break;
            }
            default: {
                answerForNextLevel = 0;
            }
        }
        return answerForNextLevel;
    }

    static int getCorrectAnswerForNextLevelForKnowledgePath(byte level) {
        int answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = 3;
                break;
            }
            case 1: {
                answerForNextLevel = 1;
                break;
            }
            case 2: {
                answerForNextLevel = 1;
                break;
            }
            case 3: {
                answerForNextLevel = 4;
                break;
            }
            case 4: {
                answerForNextLevel = 0;
                break;
            }
            case 5: {
                answerForNextLevel = 1;
                break;
            }
            case 6: {
                answerForNextLevel = 0;
                break;
            }
            case 7: {
                answerForNextLevel = 1;
                break;
            }
            case 8: {
                answerForNextLevel = 0;
                break;
            }
            case 9: {
                answerForNextLevel = 3;
                break;
            }
            case 10: {
                answerForNextLevel = 2;
                break;
            }
            case 11: {
                answerForNextLevel = 2;
                break;
            }
            case 12: {
                answerForNextLevel = 0;
                break;
            }
            case 13: {
                answerForNextLevel = 2;
                break;
            }
            case 14: {
                answerForNextLevel = 4;
                break;
            }
            case 15: {
                answerForNextLevel = 1;
                break;
            }
            case 16: {
                answerForNextLevel = 4;
                break;
            }
            case 17: {
                answerForNextLevel = 3;
                break;
            }
            default: {
                answerForNextLevel = 0;
            }
        }
        return answerForNextLevel;
    }

    public static final String getCorrectAnswerStringForNextLevel(byte path, byte level) {
        String answerForNextLevel;
        switch (path) {
            case 5: {
                answerForNextLevel = Cults.getCorrectAnswerStringForNextLevelForPowerPath(level);
                break;
            }
            case 4: {
                answerForNextLevel = Cults.getCorrectAnswerStringForNextLevelForInsanityPath(level);
                break;
            }
            case 1: {
                answerForNextLevel = Cults.getCorrectAnswerStringForNextLevelForLovePath(level);
                break;
            }
            case 2: {
                answerForNextLevel = Cults.getCorrectAnswerStringForNextLevelForHatePath(level);
                break;
            }
            case 3: {
                answerForNextLevel = Cults.getCorrectAnswerStringForNextLevelForKnowledgePath(level);
                break;
            }
            default: {
                answerForNextLevel = "You think 'Yes, that felt right'";
            }
        }
        return answerForNextLevel;
    }

    static String getCorrectAnswerStringForNextLevelForKnowledgePath(byte level) {
        String answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = "You think 'I am on to something'";
                break;
            }
            case 1: {
                answerForNextLevel = "You think 'That must be it'";
                break;
            }
            case 2: {
                answerForNextLevel = "You think 'That makes sense'";
                break;
            }
            case 3: {
                answerForNextLevel = "You think 'Probably yes'";
                break;
            }
            case 4: {
                answerForNextLevel = "You think 'Sounds about right'";
                break;
            }
            case 5: {
                answerForNextLevel = "You think 'Yes, that probably is it'";
                break;
            }
            case 6: {
                answerForNextLevel = "You think 'That feels right'";
                break;
            }
            case 7: {
                answerForNextLevel = "You think 'Seems I got that right'";
                break;
            }
            case 8: {
                answerForNextLevel = "You think 'Correct'";
                break;
            }
            case 9: {
                answerForNextLevel = "You think 'Yes'";
                break;
            }
            case 10: {
                answerForNextLevel = "You think 'Indeed so'";
                break;
            }
            case 11: {
                answerForNextLevel = "There is undoubted certainty about this.";
                break;
            }
            case 12: {
                answerForNextLevel = "Absolutely. Of course!";
                break;
            }
            case 13: {
                answerForNextLevel = "Insight in mind of others is the way to wisdom.";
                break;
            }
            case 14: {
                answerForNextLevel = "Insight in common goals is essential for omniscience.";
                break;
            }
            case 15: {
                answerForNextLevel = "The purpose in life.";
                break;
            }
            case 16: {
                answerForNextLevel = "Without love nothing has value.";
                break;
            }
            case 17: {
                answerForNextLevel = "Otherwise there is nothing.";
                break;
            }
            default: {
                answerForNextLevel = "You think 'Yes, that felt right'";
            }
        }
        return answerForNextLevel;
    }

    static String getCorrectAnswerStringForNextLevelForHatePath(byte level) {
        String answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = "You think 'I am on to something'";
                break;
            }
            case 1: {
                answerForNextLevel = "You think 'That must be it'";
                break;
            }
            case 2: {
                answerForNextLevel = "You think 'That makes sense'";
                break;
            }
            case 3: {
                answerForNextLevel = "You think 'Yes. Fear is removed by certainty.'";
                break;
            }
            case 4: {
                answerForNextLevel = "You think 'Sounds about right'";
                break;
            }
            case 5: {
                answerForNextLevel = "You think 'Yes, that probably is it'";
                break;
            }
            case 6: {
                answerForNextLevel = "You think 'That feels right'";
                break;
            }
            case 7: {
                answerForNextLevel = "You think 'Seems I got that right'";
                break;
            }
            case 8: {
                answerForNextLevel = "You think 'Correct'";
                break;
            }
            case 9: {
                answerForNextLevel = "You think 'Yes'";
                break;
            }
            case 10: {
                answerForNextLevel = "You think 'Indeed so'";
                break;
            }
            case 11: {
                answerForNextLevel = "You feel certain about this.";
                break;
            }
            case 12: {
                answerForNextLevel = "You feel the strength of truth lift you.";
                break;
            }
            case 13: {
                answerForNextLevel = "This may be useful.";
                break;
            }
            case 14: {
                answerForNextLevel = "You realize that afterlife is a powerful enemy.";
                break;
            }
            case 15: {
                answerForNextLevel = "One person will double your efforts.";
                break;
            }
            case 16: {
                answerForNextLevel = "Once you truly hate, you will take action.";
                break;
            }
            case 17: {
                answerForNextLevel = "Injustice lives on for generations.";
                break;
            }
            default: {
                answerForNextLevel = "You think 'Yes, that felt right'";
            }
        }
        return answerForNextLevel;
    }

    static String getCorrectAnswerStringForNextLevelForLovePath(byte level) {
        String answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = "You think 'Yes, that probably is it'";
                break;
            }
            case 1: {
                answerForNextLevel = "You think 'That must be it'";
                break;
            }
            case 2: {
                answerForNextLevel = "You think 'That makes sense'";
                break;
            }
            case 3: {
                answerForNextLevel = "You think 'Probably yes'";
                break;
            }
            case 4: {
                answerForNextLevel = "You think 'Sounds about right'";
                break;
            }
            case 5: {
                answerForNextLevel = "You think 'Yes, that probably is it'";
                break;
            }
            case 6: {
                answerForNextLevel = "You think 'That feels right'";
                break;
            }
            case 7: {
                answerForNextLevel = "You think 'Seems I got that right'";
                break;
            }
            case 8: {
                answerForNextLevel = "You think 'Correct'";
                break;
            }
            case 9: {
                answerForNextLevel = "You think 'Yes'";
                break;
            }
            case 10: {
                answerForNextLevel = "You think 'Indeed so'";
                break;
            }
            case 11: {
                answerForNextLevel = "You experience certainty and understanding.";
                break;
            }
            case 12: {
                answerForNextLevel = "You feel the strength of truth lift you.";
                break;
            }
            case 13: {
                answerForNextLevel = "More understanding and acceptance is balm for your soul.";
                break;
            }
            case 14: {
                answerForNextLevel = "Together we can do it.";
                break;
            }
            case 15: {
                answerForNextLevel = "Try not to.";
                break;
            }
            case 16: {
                answerForNextLevel = "Yes, that will make it hard to love.";
                break;
            }
            case 17: {
                answerForNextLevel = "New ties that can cause sorrow.";
                break;
            }
            default: {
                answerForNextLevel = "You think 'Yes, that felt right'";
            }
        }
        return answerForNextLevel;
    }

    static String getCorrectAnswerStringForNextLevelForInsanityPath(byte level) {
        String answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = "You feel certain that this is right.";
                break;
            }
            case 1: {
                answerForNextLevel = "You nod to yourself. Of course.";
                break;
            }
            case 2: {
                answerForNextLevel = "You try to pat yourself on the back.";
                break;
            }
            case 3: {
                answerForNextLevel = "You silently nod at your findings.";
                break;
            }
            case 4: {
                answerForNextLevel = "Yes, yes. The sly bastards!";
                break;
            }
            case 5: {
                answerForNextLevel = "The insight is appalling and fascinating.";
                break;
            }
            case 6: {
                answerForNextLevel = "Yes. It will be a bit of a trouble but it is the only way. The only way!";
                break;
            }
            case 7: {
                answerForNextLevel = "Your mind grows dark and heavy as you realize the sincerity of this. Where did we go wrong? Who is to blame?";
                break;
            }
            case 8: {
                answerForNextLevel = "You can't quite remove the grin on your face that this revelation brings.";
                break;
            }
            case 9: {
                answerForNextLevel = "You find yourself cackling as you are on to something here.";
                break;
            }
            case 10: {
                answerForNextLevel = "Of course. You spin around on the spot, afraid that someone has noticed you. You feel light-headed.";
                break;
            }
            case 11: {
                answerForNextLevel = "Something feels terribly wrong. Or was it right?";
                break;
            }
            case 12: {
                answerForNextLevel = "Who cares. Who cares. Who cares? You stare into nothingness wherever you look.";
                break;
            }
            case 13: {
                answerForNextLevel = "You cackle, bubble and froth happily at your findings.";
                break;
            }
            case 14: {
                answerForNextLevel = "You feel safe in the love of everyone who smiles at you.";
                break;
            }
            case 15: {
                answerForNextLevel = "People just don't see it yet.";
                break;
            }
            case 16: {
                answerForNextLevel = "That will surely make a better world.";
                break;
            }
            case 17: {
                answerForNextLevel = "There is never reason to change the foundations of a person.";
                break;
            }
            default: {
                answerForNextLevel = "As you wake up with this insight, you have to wipe off some drool on your chin.";
            }
        }
        return answerForNextLevel;
    }

    static String getCorrectAnswerStringForNextLevelForPowerPath(byte level) {
        String answerForNextLevel;
        switch (level) {
            case 0: {
                answerForNextLevel = "You think 'I am on to something'";
                break;
            }
            case 1: {
                answerForNextLevel = "This must be the path.";
                break;
            }
            case 2: {
                answerForNextLevel = "You think 'That makes sense'";
                break;
            }
            case 3: {
                answerForNextLevel = "You think 'Probably yes'";
                break;
            }
            case 4: {
                answerForNextLevel = "You think 'Sounds about right'";
                break;
            }
            case 5: {
                answerForNextLevel = "You think 'Yes, that probably is it'";
                break;
            }
            case 6: {
                answerForNextLevel = "You think 'That feels right'";
                break;
            }
            case 7: {
                answerForNextLevel = "You think 'Seems I got that right'";
                break;
            }
            case 8: {
                answerForNextLevel = "You think 'Correct'";
                break;
            }
            case 9: {
                answerForNextLevel = "You think 'Yes'";
                break;
            }
            case 10: {
                answerForNextLevel = "You think 'Indeed so'";
                break;
            }
            case 11: {
                answerForNextLevel = "The strength of truth inside!";
                break;
            }
            case 12: {
                answerForNextLevel = "Change taking places. Feelings of insight.";
                break;
            }
            case 13: {
                answerForNextLevel = "You now feel ready to take charge.";
                break;
            }
            case 14: {
                answerForNextLevel = "There is nothing stopping you now!";
                break;
            }
            case 15: {
                answerForNextLevel = "Nobody escapes his heart.";
                break;
            }
            case 16: {
                answerForNextLevel = "Without it, there is anarchy.";
                break;
            }
            case 17: {
                answerForNextLevel = "Even if it is to diminish, there is purpose for the executor.";
                break;
            }
            default: {
                answerForNextLevel = "You think 'Yes, that felt right'";
            }
        }
        return answerForNextLevel;
    }

    public static final String getPathNameFor(byte path) {
        switch (path) {
            case 2: {
                return "the path of hate";
            }
            case 1: {
                return "the path of love";
            }
            case 4: {
                return "the path of insanity";
            }
            case 3: {
                return "the path of knowledge";
            }
            case 5: {
                return "the path of power";
            }
        }
        return "no path";
    }
}


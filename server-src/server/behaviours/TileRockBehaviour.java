/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.CaveTileBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.TileBehaviour;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TileRockBehaviour
extends TileBehaviour {
    private static final Logger logger = Logger.getLogger(TileRockBehaviour.class.getName());
    static final Random rockRandom = new Random();
    private static final int worldSizeX = 1 << Constants.meshSize;
    private static final int mineZoneSize = 32;
    private static final int mineZoneDiv = worldSizeX / 32;
    private static final int minPrayingHeightDec = 400;
    private static final byte[][] minezones = new byte[mineZoneDiv + 1][mineZoneDiv + 1];
    static final long HUGE_PRIME = 789221L;
    static final long PROSPECT_PRIME = 181081L;
    public static final long SALT_PRIME = 102533L;
    public static final long SANDSTONE_PRIME = 123307L;
    static long SOURCE_PRIME = 786431L + (long)Server.rand.nextInt(10000);
    public static final int saltFactor = 100;
    public static final int sandstoneFactor = 64;
    static final int flintFactor = 200;
    static int sourceFactor = 1000;
    static final long FLINT_PRIME = 6883L;
    static final int MIN_QL = 20;
    static int MAX_QL = 100;
    static final int MAX_ROCK_QL = 100;
    static final long EMERALD_PRIME = 66083L;
    static final long OPAL_PRIME = 101333L;
    static final long RUBY_PRIME = 812341L;
    static final long DIAMOND_PRIME = 104711L;
    static final long SAPPHIRE_PRIME = 781661L;
    private static final short CAVE_DESCENT_RATE = 20;
    static final int MAX_CEIL = 255;
    static final int DIG_CEIL = 30;
    public static final int MIN_CEIL = 5;
    static final int DIG_CEIL_REACH = 60;
    static final short MIN_CAVE_FLOOR = -25;
    static final short MAX_SLOPE_DOWN = -40;
    static final short MIN_ROCK_UNDERWATER = -25;
    public static final short CAVE_INIT_HEIGHT = -100;
    private static final int ORE_ZONE_FACTOR = 4;
    private static int oreRand = 0;

    TileRockBehaviour() {
        super((short)9);
        sourceFactor = Servers.isThisAHomeServer() ? 100 : 50;
    }

    TileRockBehaviour(short type) {
        super(type);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
        HighwayPos highwaypos;
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
        if (Tiles.decodeHeight(tile) > 400 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
            Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
        }
        if (performer.getCultist() != null && performer.getCultist().maySpawnVolcano() && ((highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface)) == null || !MethodsHighways.onHighway(highwaypos))) {
            toReturn.add(new ActionEntry(78, "Erupt", "erupting"));
        }
        toReturn.add(Actions.actionEntrys[642]);
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile) {
        HighwayPos highwaypos;
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
        if (subject.isMiningtool()) {
            toReturn.add(new ActionEntry(-3, "Mining", "Mining options"));
            toReturn.add(Actions.actionEntrys[145]);
            toReturn.add(Actions.actionEntrys[156]);
            toReturn.add(Actions.actionEntrys[227]);
            if (performer.getPower() >= 4 && subject.getTemplateId() == 176) {
                toReturn.add(Actions.actionEntrys[518]);
            }
        } else if (subject.getTemplateId() == 782) {
            toReturn.add(Actions.actionEntrys[518]);
        }
        if (Tiles.decodeHeight(tile) > 400 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
            Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
        }
        if ((performer.getCultist() != null && performer.getCultist().maySpawnVolcano() || subject.getTemplateId() == 176 && performer.getPower() >= 5) && ((highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface)) == null || !MethodsHighways.onHighway(highwaypos))) {
            toReturn.add(new ActionEntry(78, "Erupt", "erupting"));
        }
        toReturn.add(Actions.actionEntrys[642]);
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter) {
        boolean done = true;
        if (action == 1) {
            Communicator comm = performer.getCommunicator();
            comm.sendNormalServerMessage("You see hard rock.");
            TileRockBehaviour.sendVillageString(performer, tilex, tiley, true);
            Trap t = Trap.getTrap(tilex, tiley, performer.getLayer());
            if (performer.getPower() > 3) {
                comm.sendNormalServerMessage("Your rot: " + Creature.normalizeAngle(performer.getStatus().getRotation()) + ", Wind rot=" + Server.getWeather().getWindRotation() + ", pow=" + Server.getWeather().getWindPower() + " x=" + Server.getWeather().getXWind() + ", y=" + Server.getWeather().getYWind());
                comm.sendNormalServerMessage("Tile is spring=" + Zone.hasSpring(tilex, tiley));
                if (performer.getPower() >= 5) {
                    comm.sendNormalServerMessage("tilex: " + tilex + ", tiley=" + tiley);
                }
                if (t != null) {
                    String villageName = "none";
                    if (t.getVillage() > 0) {
                        try {
                            villageName = Villages.getVillage(t.getVillage()).getName();
                        }
                        catch (NoSuchVillageException noSuchVillageException) {
                            // empty catch block
                        }
                    }
                    comm.sendNormalServerMessage("A " + t.getName() + ", ql=" + t.getQualityLevel() + " kingdom=" + Kingdoms.getNameFor(t.getKingdom()) + ", vill=" + villageName + ", rotdam=" + t.getRotDamage() + " firedam=" + t.getFireDamage() + " speed=" + t.getSpeedBon());
                }
            } else if (t != null && (t.getKingdom() == performer.getKingdomId() || performer.getDetectDangerBonus() > 0.0f)) {
                String qlString = "average";
                if (t.getQualityLevel() < 20) {
                    qlString = "low";
                } else if (t.getQualityLevel() > 80) {
                    qlString = "deadly";
                } else if (t.getQualityLevel() > 50) {
                    qlString = "high";
                }
                String villageName = ".";
                if (t.getVillage() > 0) {
                    try {
                        villageName = " of " + Villages.getVillage(t.getVillage()).getName() + ".";
                    }
                    catch (NoSuchVillageException noSuchVillageException) {
                        // empty catch block
                    }
                }
                String rotDam = "";
                if (t.getRotDamage() > 0) {
                    rotDam = " It has ugly black-green speckles.";
                }
                String fireDam = "";
                if (t.getFireDamage() > 0) {
                    fireDam = " It has the rune of fire.";
                }
                StringBuilder buf = new StringBuilder();
                buf.append("You detect a ");
                buf.append(t.getName());
                buf.append(" here, of ");
                buf.append(qlString);
                buf.append(" quality.");
                buf.append(" It has been set by people from ");
                buf.append(Kingdoms.getNameFor(t.getKingdom()));
                buf.append(villageName);
                buf.append(rotDam);
                buf.append(fireDam);
                comm.sendNormalServerMessage(buf.toString());
            }
        } else if (action == 141) {
            if (Tiles.decodeHeight(tile) > 400 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
                done = MethodsReligion.pray(act, performer, counter);
            }
        } else if (action == 78) {
            boolean cultistSpawn;
            HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
            if (highwaypos != null && MethodsHighways.onHighway(highwaypos)) {
                return true;
            }
            boolean bl = cultistSpawn = Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().maySpawnVolcano();
            if (cultistSpawn || performer.getPower() >= 5) {
                if (cultistSpawn) {
                    if (TileRockBehaviour.isHoleNear(tilex, tiley)) {
                        performer.getCommunicator().sendNormalServerMessage("A cave entrance is too close.");
                        return true;
                    }
                    if (Zones.getKingdom(tilex, tiley) != performer.getKingdomId()) {
                        performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from your own kingdom?");
                        return true;
                    }
                    try {
                        FaithZone fz = Zones.getFaithZone(tilex, tiley, performer.isOnSurface());
                        if (fz != null && fz.getCurrentRuler() != null && fz.getCurrentRuler().number != 2) {
                            performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from Magranon's domain?");
                            return true;
                        }
                    }
                    catch (NoSuchZoneException nsz) {
                        performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you can not spawn lava too far from Magranon's domain?");
                        return true;
                    }
                    if (!Methods.isActionAllowed(performer, (short)547, tilex, tiley)) {
                        return true;
                    }
                    done = false;
                    if (counter == 1.0f) {
                        int sx = Zones.safeTileX(tilex - 1);
                        int sy = Zones.safeTileX(tiley - 1);
                        int ey = Zones.safeTileX(tiley + 1);
                        int ex = Zones.safeTileX(tilex + 1);
                        for (int x = sx; x <= ex; ++x) {
                            for (int y = sy; y <= ey; ++y) {
                                Item[] its;
                                VolaTile tt = Zones.getTileOrNull(x, y, onSurface);
                                if (tt == null) continue;
                                for (Item i : its = tt.getItems()) {
                                    if (!i.isNoTake()) continue;
                                    performer.getCommunicator().sendNormalServerMessage("The " + i.getName() + " blocks your efforts.");
                                    return true;
                                }
                            }
                        }
                        performer.getCommunicator().sendNormalServerMessage("You start concentrating on the rock.");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to look intensely on the rock.", performer, 5);
                        if (cultistSpawn) {
                            performer.sendActionControl("Erupting", true, 400);
                        }
                    }
                }
                if (!cultistSpawn || counter > 40.0f) {
                    done = true;
                    int caveTile = Server.caveMesh.getTile(tilex, tiley);
                    byte type = Tiles.decodeType(caveTile);
                    if (Tiles.isSolidCave(type) && !Tiles.getTile(type).isReinforcedCave()) {
                        performer.getCommunicator().sendNormalServerMessage("The rock starts to bubble with lava.");
                        Server.getInstance().broadCastAction(performer.getName() + " makes the rock boil with red hot lava.", performer, 5);
                        short height = Tiles.decodeHeight(tile);
                        TileEvent.log(tilex, tiley, 0, performer.getWurmId(), action);
                        int nh = height + 4;
                        if (cultistSpawn) {
                            performer.getCultist().touchCooldown2();
                        }
                        Server.setSurfaceTile(tilex, tiley, (short)nh, Tiles.Tile.TILE_LAVA.id, (byte)0);
                        for (int xx = 0; xx <= 1; ++xx) {
                            for (int yy = 0; yy <= 1; ++yy) {
                                try {
                                    short tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx, tiley + yy));
                                    Server.rockMesh.setTile(tilex + xx, tiley + yy, Tiles.encode(tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                                    continue;
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }
                        Terraforming.setAsRock(tilex, tiley, false, true);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
                    }
                }
            }
        } else {
            done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
        }
        return done;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter) {
        boolean flintExists;
        boolean saltExists;
        String findString;
        boolean done;
        block94: {
            Iterator it;
            int time;
            Skill prospecting;
            block93: {
                boolean makingWideTunnel;
                block96: {
                    block97: {
                        block95: {
                            Point lowestCorner;
                            done = true;
                            if (action == 518 && (source.getTemplateId() == 782 || performer.getPower() >= 4 && source.getTemplateId() == 176)) {
                                int digTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
                                int digTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
                                return CaveTileBehaviour.raiseRockLevel(performer, source, digTileX, digTileY, counter, act);
                            }
                            if (!source.isMiningtool() || action != 227) break block95;
                            if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
                                performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
                                return true;
                            }
                            if (Zones.isTileProtected(tilex, tiley)) {
                                performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
                                return true;
                            }
                            short h = Tiles.decodeHeight(tile);
                            if (h <= -24) {
                                performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
                                return done;
                            }
                            makingWideTunnel = false;
                            if (TileRockBehaviour.isHoleNear(tilex, tiley)) {
                                if (!TileRockBehaviour.canHaveWideEntrance(performer, tilex, tiley)) {
                                    performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.");
                                    return true;
                                }
                                makingWideTunnel = true;
                            }
                            if ((lowestCorner = TileRockBehaviour.findLowestCorner(performer, tilex, tiley)) == null) {
                                return true;
                            }
                            Point nextLowestCorner = TileRockBehaviour.findNextLowestCorner(performer, tilex, tiley, lowestCorner);
                            if (nextLowestCorner == null) {
                                return true;
                            }
                            Point highestCorner = TileRockBehaviour.findHighestCorner(tilex, tiley);
                            if (highestCorner == null) {
                                return false;
                            }
                            Point nextHighestCorner = TileRockBehaviour.findNextHighestCorner(tilex, tiley, highestCorner);
                            if (nextHighestCorner == null) {
                                return false;
                            }
                            if (nextLowestCorner.getH() != lowestCorner.getH() && TileRockBehaviour.isStructureNear(nextLowestCorner.getX(), nextLowestCorner.getY()) || nextHighestCorner.getH() != highestCorner.getH() && TileRockBehaviour.isStructureNear(highestCorner.getX(), highestCorner.getY())) {
                                performer.getCommunicator().sendNormalServerMessage("Cannot create a tunnel here as there is a structure too close.", (byte)3);
                                return true;
                            }
                            break block96;
                        }
                        if (source.isMiningtool() && action == 145) {
                            int digTilex = (int)performer.getStatus().getPositionX() + 2 >> 2;
                            int digTiley = (int)performer.getStatus().getPositionY() + 2 >> 2;
                            return TileRockBehaviour.mine(act, performer, source, tilex, tiley, action, counter, digTilex, digTiley);
                        }
                        if (!source.isMiningtool() || action != 156) break block97;
                        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
                            performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.", (byte)3);
                            return true;
                        }
                        float h = Tiles.decodeHeight(tile);
                        if (!(h > -25.0f)) {
                            performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.");
                            return done;
                        }
                        Skills skills = performer.getSkills();
                        prospecting = null;
                        done = false;
                        try {
                            prospecting = skills.getSkill(10032);
                        }
                        catch (Exception ex) {
                            prospecting = skills.learn(10032, 1.0f);
                        }
                        time = 0;
                        if (counter == 1.0f) {
                            String sstring = "sound.work.prospecting1";
                            int x = Server.rand.nextInt(3);
                            if (x == 0) {
                                sstring = "sound.work.prospecting2";
                            } else if (x == 1) {
                                sstring = "sound.work.prospecting3";
                            }
                            SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 1.0f);
                            time = (int)Math.max(30.0, 100.0 - prospecting.getKnowledge(source, 0.0));
                            try {
                                performer.getCurrentAction().setTimeLeft(time);
                            }
                            catch (NoSuchActionException nsa) {
                                logger.log(Level.INFO, "This action does not exist?", nsa);
                            }
                            performer.getCommunicator().sendNormalServerMessage("You start to gather fragments of the rock.");
                            Server.getInstance().broadCastAction(performer.getName() + " starts gathering fragments of the rock.", performer, 5);
                            performer.sendActionControl(Actions.actionEntrys[156].getVerbString(), true, time);
                            break block93;
                        } else {
                            try {
                                time = performer.getCurrentAction().getTimeLeft();
                                break block93;
                            }
                            catch (NoSuchActionException nsa) {
                                logger.log(Level.INFO, "This action does not exist?", nsa);
                            }
                        }
                        break block93;
                    }
                    if (action == 141) return this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                    if (action == 78) return this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                    return super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
                }
                int x = -1;
                while (true) {
                    if (x <= 1) {
                    } else {
                        int t;
                        done = false;
                        Skills skills = performer.getSkills();
                        Skill mining = null;
                        Skill tool = null;
                        boolean insta = performer.getPower() >= 2 && source.isWand();
                        try {
                            mining = skills.getSkill(1008);
                        }
                        catch (Exception ex) {
                            mining = skills.learn(1008, 1.0f);
                        }
                        try {
                            tool = skills.getSkill(source.getPrimarySkill());
                        }
                        catch (Exception ex) {
                            try {
                                tool = skills.learn(source.getPrimarySkill(), 1.0f);
                            }
                            catch (NoSuchSkillException nse) {
                                logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source.getName());
                            }
                        }
                        int time2 = 0;
                        if (counter == 1.0f) {
                            time2 = Actions.getStandardActionTime(performer, mining, source, 0.0);
                            try {
                                performer.getCurrentAction().setTimeLeft(time2);
                            }
                            catch (NoSuchActionException nsa) {
                                logger.log(Level.INFO, "This action does not exist?", nsa);
                            }
                            if (TileRockBehaviour.affectsHighway(tilex, tiley)) {
                                performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
                                return true;
                            }
                            if (!this.isOutInTunnelOkay(performer, tilex, tiley, makingWideTunnel)) {
                                return true;
                            }
                            Server.getInstance().broadCastAction(performer.getName() + " starts tunneling.", performer, 5);
                            performer.getCommunicator().sendNormalServerMessage("You start to tunnel.");
                            performer.sendActionControl(Actions.actionEntrys[227].getVerbString(), true, time2);
                            source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                            performer.getStatus().modifyStamina(-1000.0f);
                            return done;
                        }
                        try {
                            time2 = performer.getCurrentAction().getTimeLeft();
                        }
                        catch (NoSuchActionException nsa) {
                            logger.log(Level.INFO, "This action does not exist?", nsa);
                        }
                        if (counter * 10.0f <= (float)time2 && !insta) {
                            if (act.currentSecond() % 5 != 0) {
                                if (act.currentSecond() != 3) return done;
                                if (time2 >= 50) return done;
                            }
                            String sstring = "sound.work.mining1";
                            int x2 = Server.rand.nextInt(3);
                            if (x2 == 0) {
                                sstring = "sound.work.mining2";
                            } else if (x2 == 1) {
                                sstring = "sound.work.mining3";
                            }
                            SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 0.0f);
                            source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                            performer.getStatus().modifyStamina(-7000.0f);
                            return done;
                        }
                        if (act.getRarity() != 0) {
                            performer.playPersonalSound("sound.fx.drumroll");
                        }
                        double bonus = 0.0;
                        double power = 0.0;
                        done = true;
                        int itemTemplateCreated = 146;
                        float diff = 1.0f;
                        int mineDir = TileRockBehaviour.getTunnelExit(tilex, tiley);
                        if (mineDir == -1) {
                            performer.getCommunicator().sendNormalServerMessage("The topology here makes it impossible to mine in a good way.", (byte)3);
                            return true;
                        }
                        byte state = Zones.getMiningState(tilex, tiley);
                        if (state == -1) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                            return true;
                        }
                        if (TileRockBehaviour.affectsHighway(tilex, tiley)) {
                            performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
                            return true;
                        }
                        if (state >= Math.max(1, Servers.localServer.getTunnelingHits()) + Server.rand.nextInt(10) || insta) {
                            t = Server.caveMesh.getTile(tilex, tiley);
                            if (Tiles.isReinforcedCave(Tiles.decodeType(t))) {
                                performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                                return true;
                            }
                            Zones.deleteMiningTile(tilex, tiley);
                            if (!TileRockBehaviour.areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel)) {
                                performer.getCommunicator().sendNormalServerMessage("The ground sounds strangely hollow and brittle. You have to abandon the mining operation.", (byte)3);
                                return true;
                            }
                            int drop = -20;
                            if (makingWideTunnel) {
                                MeshTile mTileEast;
                                MeshTile mTileSouth;
                                MeshTile mTileWest;
                                MeshTile mTileCurrent = new MeshTile(Server.surfaceMesh, tilex, tiley);
                                MeshTile mCaveCurrent = new MeshTile(Server.caveMesh, tilex, tiley);
                                MeshTile mTileNorth = mTileCurrent.getNorthMeshTile();
                                if (mTileNorth.isHole()) {
                                    MeshTile mCaveNorth = mCaveCurrent.getNorthMeshTile();
                                    drop = -Math.abs(mCaveNorth.getSouthSlope());
                                }
                                if ((mTileWest = mTileCurrent.getWestMeshTile()).isHole()) {
                                    MeshTile mCaveWest = mCaveCurrent.getWestMeshTile();
                                    drop = -Math.abs(mCaveWest.getEastSlope());
                                }
                                if ((mTileSouth = mTileCurrent.getSouthMeshTile()).isHole()) {
                                    MeshTile mCaveSouth = mCaveCurrent.getSouthMeshTile();
                                    drop = -Math.abs(mCaveSouth.getNorthSlope());
                                }
                                if ((mTileEast = mTileCurrent.getEastMeshTile()).isHole()) {
                                    MeshTile mCaveEast = mCaveCurrent.getEastMeshTile();
                                    drop = -Math.abs(mCaveEast.getWestSlope());
                                }
                            }
                            if (!TileRockBehaviour.createOutInTunnel(tilex, tiley, tile, performer, drop)) {
                                return true;
                            }
                        } else {
                            if (!TileRockBehaviour.areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel)) {
                                performer.getCommunicator().sendNormalServerMessage("The ground sounds strangely hollow and brittle. You have to abandon the mining operation.", (byte)3);
                                return true;
                            }
                            if (!this.isOutInTunnelOkay(performer, tilex, tiley, makingWideTunnel)) {
                                return true;
                            }
                        }
                        if (state > 10 && Tiles.isReinforcedCave(Tiles.decodeType(t = Server.caveMesh.getTile(tilex, tiley)))) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot keep mining here. The rock is unusually hard.", (byte)3);
                            return true;
                        }
                        if (state < 76) {
                            state = (byte)(state + 1);
                            Zones.setMiningState(tilex, tiley, state, false);
                            if (state > Servers.localServer.getTunnelingHits()) {
                                performer.getCommunicator().sendNormalServerMessage("You will soon create an entrance.");
                            }
                        }
                        if (tool != null) {
                            bonus = tool.skillCheck(1.0, source, 0.0, false, counter) / 5.0;
                        }
                        power = Math.max(1.0, mining.skillCheck(1.0, source, bonus, false, counter));
                        if (performer.getTutorialLevel() == 10 && !performer.skippedTutorial()) {
                            performer.missionFinished(true, true);
                        }
                        if (Server.rand.nextInt(5) != 0) {
                            performer.getCommunicator().sendNormalServerMessage("You chip away at the rock.");
                            return done;
                        }
                        try {
                            if (mining.getKnowledge(0.0) < power) {
                                power = mining.getKnowledge(0.0);
                            }
                            rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
                            int m = 100;
                            double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(1008) / 100.0;
                            float modifier = 1.0f;
                            if (source.getSpellEffects() != null) {
                                modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                            }
                            int max = (int)Math.min(100.0, 20.0 + (double)rockRandom.nextInt(80) * imbueEnhancement * (double)modifier + (double)source.getRarity());
                            power = Math.min(power, (double)max);
                            if (source.isCrude()) {
                                power = 1.0;
                            }
                            Item newItem = ItemFactory.createItem(146, (float)power, performer.getPosX(), performer.getPosY(), Server.rand.nextFloat() * 360.0f, performer.isOnSurface(), act.getRarity(), -10L, null);
                            newItem.setLastOwnerId(performer.getWurmId());
                            newItem.setDataXY(tilex, tiley);
                            performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                            Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
                            TileRockBehaviour.createGem(tilex, tiley, performer, power, true, act);
                            return done;
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Factory failed to produce item", ex);
                            return done;
                        }
                    }
                    for (int y = -1; y <= 1; ++y) {
                        Structure cs;
                        Structure ss;
                        VolaTile svt = Zones.getTileOrNull(tilex + x, tiley + y, true);
                        Structure structure = ss = svt == null ? null : svt.getStructure();
                        if (ss != null && ss.isTypeBridge()) {
                            performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                            return true;
                        }
                        VolaTile cvt = Zones.getTileOrNull(tilex + x, tiley + y, false);
                        Structure structure2 = cs = cvt == null ? null : cvt.getStructure();
                        if (cs == null || !cs.isTypeBridge()) continue;
                        performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                        return true;
                    }
                    ++x;
                }
            }
            if (!(counter * 10.0f > (float)time)) return done;
            performer.getStatus().modifyStamina(-3000.0f);
            prospecting.skillCheck(1.0, source, 0.0, false, counter);
            source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
            done = true;
            findString = "only rock";
            LinkedList<String> list = new LinkedList<String>();
            int m = 100;
            saltExists = false;
            flintExists = false;
            int x = -3;
            while (true) {
                if (x <= 3) {
                } else if (list.isEmpty()) {
                    findString = "only rock";
                    break block94;
                } else {
                    x = 0;
                    it = list.iterator();
                    break;
                }
                for (int y = -3; y <= 3; ++y) {
                    int itemTemplate;
                    int resource = Server.getCaveResource(tilex + x, tiley + y);
                    findString = "";
                    if (resource == 65535) {
                        resource = Server.rand.nextInt(10000);
                        Server.setCaveResource(tilex + x, tiley + y, resource);
                    }
                    if ((itemTemplate = TileRockBehaviour.getItemTemplateForTile(Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y)))) != 146) {
                        try {
                            ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(itemTemplate);
                            String qlstring = "";
                            if (prospecting.getKnowledge(0.0) > 20.0) {
                                rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 789221L);
                                int max = Math.min(100, 20 + rockRandom.nextInt(80));
                                qlstring = " (" + TileRockBehaviour.getShardQlDescription(max) + ")";
                            }
                            findString = t.getProspectName() + qlstring;
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, performer.getName() + " - " + nst.getMessage() + ": " + itemTemplate + " at " + tilex + ", " + tiley, nst);
                        }
                    }
                    if (prospecting.getKnowledge(0.0) > 40.0) {
                        rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 102533L);
                        if (rockRandom.nextInt(100) == 0) {
                            saltExists = true;
                        }
                    }
                    if (prospecting.getKnowledge(0.0) > 20.0) {
                        rockRandom.setSeed((long)(tilex + x + (tiley + y) * Zones.worldTileSizeY) * 6883L);
                        if (rockRandom.nextInt(200) == 0) {
                            flintExists = true;
                        }
                    }
                    if (findString.length() <= 0 || list.contains(findString)) continue;
                    if (Server.rand.nextBoolean()) {
                        list.addFirst(findString);
                        continue;
                    }
                    list.addLast(findString);
                }
                ++x;
            }
            while (it.hasNext()) {
                findString = x == 0 ? (String)it.next() : (x == list.size() - 1 ? findString + " and " + (String)it.next() : findString + ", " + (String)it.next());
                ++x;
            }
        }
        performer.getCommunicator().sendNormalServerMessage("There is " + findString + " nearby.");
        if (saltExists) {
            performer.getCommunicator().sendNormalServerMessage("You will find salt here!");
        }
        if (!flintExists) return done;
        performer.getCommunicator().sendNormalServerMessage("You will find flint here!");
        return done;
    }

    private static int getTunnelExit(int tilex, int tiley) {
        float lowestHeight;
        int lowestX = 100000;
        int lowestY = 100000;
        int nextLowestX = lowestX;
        int nextLowestY = lowestY;
        float nextLowestHeight = lowestHeight = 100000.0f;
        int sameX = lowestX;
        int sameY = lowestY;
        int lowerCount = 0;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                short rockHeight = Tiles.decodeHeight(rockTile);
                if (lowestHeight == 32767.0f) {
                    lowestHeight = rockHeight;
                    lowestX = tilex + x;
                    lowestY = tiley + y;
                    lowerCount = 1;
                    continue;
                }
                if ((float)rockHeight < lowestHeight) {
                    lowestHeight = rockHeight;
                    lowestX = tilex + x;
                    lowestY = tiley + y;
                    lowerCount = 1;
                    continue;
                }
                if ((float)rockHeight != lowestHeight) continue;
                sameX = tilex + x;
                sameY = tiley + y;
                ++lowerCount;
            }
        }
        if (lowerCount > 2) {
            logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
            return -1;
        }
        if (lowerCount == 2 && sameX - lowestX != 0 && sameY - lowestY != 0) {
            logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
            return -1;
        }
        int nsY = tiley + (1 - (lowestY - tiley));
        int nsRockTile = Server.rockMesh.getTile(lowestX, nsY);
        short nsRockHeight = Tiles.decodeHeight(nsRockTile);
        nextLowestHeight = nsRockHeight;
        nextLowestX = lowestX;
        nextLowestY = nsY;
        int weX = tilex + (1 - (lowestX - tilex));
        int weRockTile = Server.rockMesh.getTile(weX, lowestY);
        short weRockHeight = Tiles.decodeHeight(weRockTile);
        if ((float)weRockHeight < nextLowestHeight) {
            nextLowestHeight = weRockHeight;
            nextLowestX = weX;
            nextLowestY = lowestY;
        } else if ((float)weRockHeight == nextLowestHeight) {
            logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
            return -1;
        }
        if (lowestX == tilex + 0) {
            if (lowestY == tiley + 0) {
                if (nextLowestX == tilex + 1) {
                    if (nextLowestY == tiley + 0) {
                        return 3;
                    }
                } else if (nextLowestY == tiley + 1) {
                    return 2;
                }
            } else if (lowestY == tiley + 1) {
                if (nextLowestX == tilex + 1) {
                    if (nextLowestY == tiley + 1) {
                        return 5;
                    }
                } else if (nextLowestY == tiley + 0) {
                    return 2;
                }
            }
        } else if (lowestY == tiley + 0) {
            if (nextLowestX == tilex + 1) {
                if (nextLowestY == tiley + 1) {
                    return 4;
                }
            } else if (nextLowestY == tiley + 0) {
                return 3;
            }
        } else if (lowestY == tiley + 1) {
            if (nextLowestX == tilex + 1) {
                if (nextLowestY == tiley + 0) {
                    return 4;
                }
            } else if (nextLowestY == tiley + 1) {
                return 5;
            }
        }
        logger.log(Level.WARNING, "Bad tile at " + tilex + ", " + tiley);
        return -1;
    }

    private static void setTileToTransition(int tilex, int tiley) {
        VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
        if (t != null) {
            t.isTransition = true;
        }
        if ((t = Zones.getTileOrNull(tilex, tiley, false)) != null) {
            t.isTransition = true;
        }
    }

    private boolean isOutInTunnelOkay(Creature performer, int tilex, int tiley, boolean makingWideTunnel) {
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                int tileNew = Server.surfaceMesh.getTile(tilex + x, tiley + y);
                if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_HOLE.id && !makingWideTunnel) {
                    performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.", (byte)3);
                    return false;
                }
                if (Tiles.isMineDoor(Tiles.decodeType(tileNew))) {
                    performer.getCommunicator().sendNormalServerMessage("Cannot make a tunnel next to a mine door.");
                    return false;
                }
                if (x < 0 || y < 0) continue;
                int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                short rockHeight = Tiles.decodeHeight(rockTile);
                int caveTile = Server.caveMesh.getTile(tilex + x, tiley + y);
                short cheight = Tiles.decodeHeight(caveTile);
                if (TileRockBehaviour.isNullWall(caveTile) || rockHeight - cheight < 255) continue;
                performer.getCommunicator().sendNormalServerMessage("Not enough rock height to make a tunnel there.");
                return false;
            }
        }
        return true;
    }

    static boolean isHoleNear(int tilex, int tiley) {
        MeshIO surfMesh = Server.surfaceMesh;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                int tileNew = surfMesh.getTile(tilex + x, tiley + y);
                if (Tiles.decodeType(tileNew) != Tiles.Tile.TILE_HOLE.id) continue;
                return true;
            }
        }
        return false;
    }

    static boolean canHaveWideEntrance(@Nullable Creature performer, int tilex, int tiley) {
        MeshTile mTileEast;
        MeshTile mTileSouth;
        MeshTile mTileWest;
        MeshIO surfMesh = Server.surfaceMesh;
        if (!TileRockBehaviour.hasValidNearbyEntrance(performer, surfMesh, tilex, tiley)) {
            return false;
        }
        MeshTile currentMT = new MeshTile(surfMesh, tilex, tiley);
        MeshTile mTileNorth = currentMT.getNorthMeshTile();
        if (mTileNorth.isHole()) {
            int dir = mTileNorth.getLowerLip();
            if (dir == 6) {
                if (currentMT.getWestSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getSouthSlope() <= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
            if (dir == 2) {
                if (currentMT.getEastSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getSouthSlope() >= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
        }
        if ((mTileWest = currentMT.getWestMeshTile()).isHole()) {
            int dir = mTileWest.getLowerLip();
            if (dir == 0) {
                if (currentMT.getNorthSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getEastSlope() <= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
            if (dir == 4) {
                if (currentMT.getSouthSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getEastSlope() >= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
        }
        if ((mTileSouth = currentMT.getSouthMeshTile()).isHole()) {
            int dir = mTileSouth.getLowerLip();
            if (dir == 6) {
                if (currentMT.getWestSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getNorthSlope() <= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
            if (dir == 2) {
                if (currentMT.getEastSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getNorthSlope() >= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
        }
        if ((mTileEast = currentMT.getEastMeshTile()).isHole()) {
            int dir = mTileEast.getLowerLip();
            if (dir == 0) {
                if (currentMT.getNorthSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getWestSlope() <= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
            if (dir == 4) {
                if (currentMT.getSouthSlope() != 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs a flat border to correspond to lower part of adjacent cave entrance.");
                    }
                    return false;
                }
                if (currentMT.getWestSlope() >= 0) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Current tile needs to be same orientation as adjacent cave entrance.");
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private static boolean hasValidNearbyEntrance(@Nullable Creature performer, MeshIO surfMesh, int tilex, int tiley) {
        int tileNew;
        byte type;
        int y;
        int x;
        int holeX = -1;
        int holeY = -1;
        int holeXX = -1;
        int holeYY = -1;
        for (x = -1; x <= 1; ++x) {
            for (y = -1; y <= 1; ++y) {
                if (x == 0 || y == 0 || (type = Tiles.decodeType(tileNew = surfMesh.getTile(tilex + x, tiley + y))) != Tiles.Tile.TILE_HOLE.id) continue;
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Cannot have cave entrances meeting diagonally.");
                }
                return false;
            }
        }
        for (x = -1; x <= 1; ++x) {
            for (y = -1; y <= 1; ++y) {
                if (x == 0 && y == 0 || !Tiles.isMineDoor(type = Tiles.decodeType(tileNew = surfMesh.getTile(tilex + x, tiley + y)))) continue;
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Cannot make a tunnel next to a mine door.");
                }
                return false;
            }
        }
        for (x = -1; x <= 1; ++x) {
            for (y = -1; y <= 1; ++y) {
                if (x == 0 && y == 0 || x != 0 && y != 0 || Tiles.decodeType(tileNew = surfMesh.getTile(tilex + x, tiley + y)) != Tiles.Tile.TILE_HOLE.id) continue;
                if (holeX != -1) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                    }
                    return false;
                }
                holeX = tilex + x;
                holeY = tiley + y;
            }
        }
        if (holeX == -1) {
            return true;
        }
        for (int xx = -1; xx <= 1; ++xx) {
            for (int yy = -1; yy <= 1; ++yy) {
                int tileTwo;
                if (xx == 0 && yy == 0 || Tiles.decodeType(tileTwo = surfMesh.getTile(holeX + xx, holeY + yy)) != Tiles.Tile.TILE_HOLE.id) continue;
                if (holeXX != -1) {
                    if (performer != null) {
                        performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                    }
                    return false;
                }
                holeXX = holeX + xx;
                holeYY = holeY + yy;
                if (tilex + xx + xx == holeXX && tiley + yy + yy == holeYY) continue;
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                }
                return false;
            }
        }
        if (holeXX == -1) {
            return true;
        }
        for (int xxx = -1; xxx <= 1; ++xxx) {
            for (int yyy = -1; yyy <= 1; ++yyy) {
                int tileThree;
                if (xxx == 0 && yyy == 0 || Tiles.decodeType(tileThree = surfMesh.getTile(holeXX + xxx, holeYY + yyy)) != Tiles.Tile.TILE_HOLE.id || holeXX + xxx == holeX && holeYY + yyy == holeY) continue;
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Can only make two or three tile wide cave entrances .");
                }
                return false;
            }
        }
        return true;
    }

    static boolean isStructureNear(int tilex, int tiley) {
        for (int x = -1; x <= 0; ++x) {
            for (int y = -1; y <= 0; ++y) {
                VolaTile vt = Zones.getTileOrNull(tilex + x, tiley + y, true);
                if (vt != null && vt.getStructure() != null) {
                    return true;
                }
                VolaTile vtc = Zones.getTileOrNull(tilex + x, tiley + y, false);
                if (vtc == null || vtc.getStructure() == null) continue;
                return true;
            }
        }
        return false;
    }

    static boolean createOutInTunnel(int tilex, int tiley, int tile, Creature performer, int mod) {
        MeshIO surfmesh = Server.surfaceMesh;
        MeshIO cavemesh = Server.caveMesh;
        VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
        if (t != null) {
            Item[] items;
            for (Item lItem : items = t.getItems()) {
                if (!lItem.isDecoration()) continue;
                performer.getCommunicator().sendNormalServerMessage(LoginHandler.raiseFirstLetter(lItem.getNameWithGenus()) + " on the surface disturbs your operation.");
                return false;
            }
            if (t.getStructure() != null) {
                performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a structure in the way.");
                return false;
            }
        }
        boolean makingWideTunnel = false;
        if (TileRockBehaviour.isHoleNear(tilex, tiley)) {
            if (TileRockBehaviour.canHaveWideEntrance(performer, tilex, tiley)) {
                makingWideTunnel = true;
            } else {
                performer.getCommunicator().sendNormalServerMessage("Another tunnel is too close. It would collapse.");
                return false;
            }
        }
        if (TileRockBehaviour.affectsHighway(tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("A surface highway interferes with your tunneling operation.", (byte)3);
            return false;
        }
        Point lowestCorner = TileRockBehaviour.findLowestCorner(performer, tilex, tiley);
        if (lowestCorner == null) {
            return false;
        }
        Point nextLowestCorner = TileRockBehaviour.findNextLowestCorner(performer, tilex, tiley, lowestCorner);
        if (nextLowestCorner == null) {
            return false;
        }
        Point highestCorner = TileRockBehaviour.findHighestCorner(tilex, tiley);
        if (highestCorner == null) {
            return false;
        }
        Point nextHighestCorner = TileRockBehaviour.findNextHighestCorner(tilex, tiley, highestCorner);
        if (nextHighestCorner == null) {
            return false;
        }
        if (nextLowestCorner.getH() != lowestCorner.getH() && TileRockBehaviour.isStructureNear(nextLowestCorner.getX(), nextLowestCorner.getY()) || nextHighestCorner.getH() != highestCorner.getH() && TileRockBehaviour.isStructureNear(highestCorner.getX(), highestCorner.getY())) {
            performer.getCommunicator().sendNormalServerMessage("Cannot create a tunnel here as there is a structure too close.", (byte)3);
            return false;
        }
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                Structure cs;
                Structure ss;
                VolaTile svt = Zones.getTileOrNull(tilex + x, tiley + y, true);
                Structure structure = ss = svt == null ? null : svt.getStructure();
                if (ss != null && ss.isTypeBridge()) {
                    performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                    return false;
                }
                VolaTile cvt = Zones.getTileOrNull(tilex + x, tiley + y, false);
                Structure structure2 = cs = cvt == null ? null : cvt.getStructure();
                if (cs == null || !cs.isTypeBridge()) continue;
                performer.getCommunicator().sendNormalServerMessage("You can't tunnel here, there is a bridge in the way.");
                return false;
            }
        }
        int nsY = tiley + (1 - (nextLowestCorner.getY() - tiley));
        int weX = tilex + (1 - (nextLowestCorner.getX() - tilex));
        int nsCorner = surfmesh.getTile(nextLowestCorner.getX(), nsY);
        if (!TileRockBehaviour.mayLowerCornerOnSlope(lowestCorner.getH(), performer, nsCorner)) {
            return false;
        }
        int weCorner = surfmesh.getTile(weX, nextLowestCorner.getY());
        if (!TileRockBehaviour.mayLowerCornerOnSlope(lowestCorner.getH(), performer, weCorner)) {
            return false;
        }
        if (Tiles.isReinforcedCave(Tiles.decodeType(cavemesh.getTile(tilex, tiley)))) {
            return false;
        }
        if (makingWideTunnel) {
            performer.getCommunicator().sendNormalServerMessage("You expand a tunnel entrance!");
        } else {
            performer.getCommunicator().sendNormalServerMessage("You create a tunnel entrance!");
        }
        short targetHeight = (short)lowestCorner.getH();
        for (int x = tilex; x <= tilex + 1; ++x) {
            for (int y = tiley; y <= tiley + 1; ++y) {
                int newFloorHeight;
                int[] newfloorceil;
                int tileNew = cavemesh.getTile(x, y);
                int rockTile = Server.rockMesh.getTile(x, y);
                short rockHeight = Tiles.decodeHeight(rockTile);
                int surfTile = Server.surfaceMesh.getTile(x, y);
                short surfHeight = Tiles.decodeHeight(surfTile);
                if (x == tilex && y == tiley) {
                    if (x == lowestCorner.getX() && y == lowestCorner.getY() || x == nextLowestCorner.getX() && y == nextLowestCorner.getY()) {
                        VolaTile cavet;
                        newfloorceil = TileRockBehaviour.getFloorAndCeiling(x, y, targetHeight, 0, true, false, performer);
                        newFloorHeight = newfloorceil[0];
                        cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE_EXIT.id, (byte)0));
                        VolaTile surft = Zones.getTileOrNull(x, y, true);
                        if (surft != null) {
                            surft.isTransition = true;
                        }
                        if ((cavet = Zones.getTileOrNull(x, y, false)) != null) {
                            cavet.isTransition = true;
                        }
                        if (rockHeight != newFloorHeight || surfHeight != newFloorHeight) {
                            Server.rockMesh.setTile(x, y, Tiles.encode((short)newFloorHeight, (short)0));
                            surfmesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeTileData(surfTile)));
                            Players.getInstance().sendChangedTile(x, y, true, true);
                        }
                    } else {
                        newfloorceil = TileRockBehaviour.getFloorAndCeiling(x, y, targetHeight, mod, false, true, performer);
                        newFloorHeight = newfloorceil[0];
                        int newCeil = newfloorceil[1];
                        if (Tiles.decodeType(tileNew) == Tiles.Tile.TILE_CAVE_WALL.id || Tiles.decodeType(tileNew) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
                            VolaTile cavet;
                            cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE_EXIT.id, (byte)(newCeil - newFloorHeight)));
                            VolaTile surft = Zones.getTileOrNull(x, y, true);
                            if (surft != null) {
                                surft.isTransition = true;
                            }
                            if ((cavet = Zones.getTileOrNull(x, y, false)) != null) {
                                cavet.isTransition = true;
                            }
                        } else {
                            cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)(newCeil - newFloorHeight)));
                        }
                    }
                } else if (x == lowestCorner.getX() && y == lowestCorner.getY() || x == nextLowestCorner.getX() && y == nextLowestCorner.getY()) {
                    newfloorceil = TileRockBehaviour.getFloorAndCeiling(x, y, targetHeight, 0, true, false, performer);
                    newFloorHeight = newfloorceil[0];
                    cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)0));
                    if (rockHeight != newFloorHeight || surfHeight != newFloorHeight) {
                        Server.rockMesh.setTile(x, y, Tiles.encode((short)newFloorHeight, (short)0));
                        surfmesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeTileData(surfTile)));
                        Players.getInstance().sendChangedTile(x, y, true, true);
                    }
                } else {
                    newfloorceil = TileRockBehaviour.getFloorAndCeiling(x, y, targetHeight, mod, false, true, performer);
                    newFloorHeight = newfloorceil[0];
                    int newCeil = newfloorceil[1];
                    cavemesh.setTile(x, y, Tiles.encode((short)newFloorHeight, Tiles.decodeType(tileNew), (byte)(newCeil - newFloorHeight)));
                }
                Players.getInstance().sendChangedTile(x, y, false, true);
                for (int xx = -1; xx <= 0; ++xx) {
                    for (int yy = -1; yy <= 0; ++yy) {
                        try {
                            Zone toCheckForChange = Zones.getZone(x + xx, y + yy, false);
                            toCheckForChange.changeTile(x + xx, y + yy);
                            continue;
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, "no such zone?: " + (x + xx) + ", " + (y + yy), nsz);
                        }
                    }
                }
            }
        }
        TileRockBehaviour.setTileToTransition(tilex, tiley);
        tile = Server.surfaceMesh.getTile(tilex, tiley);
        surfmesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_HOLE.id, Tiles.decodeData(tile)));
        short targetUpperHeight = (short)nextHighestCorner.getH();
        short tileData = Tiles.decodeTileData(Server.surfaceMesh.getTile(highestCorner.getX(), highestCorner.getY()));
        Server.surfaceMesh.setTile(highestCorner.getX(), highestCorner.getY(), Tiles.encode(targetUpperHeight, tileData));
        tileData = Tiles.decodeTileData(Server.rockMesh.getTile(highestCorner.getX(), highestCorner.getY()));
        Server.rockMesh.setTile(highestCorner.getX(), highestCorner.getY(), Tiles.encode(targetUpperHeight, tileData));
        tileData = Tiles.decodeTileData(Server.caveMesh.getTile(highestCorner.getX(), highestCorner.getY()));
        Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), true, true);
        Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), false, true);
        Players.getInstance().sendChangedTile(tilex, tiley, true, true);
        VolaTile to = Zones.getOrCreateTile(tilex, tiley, true);
        to.checkCaveOpening();
        return true;
    }

    @Nullable
    private static Point findLowestCorner(Creature performer, int tilex, int tiley) {
        int lowestX = 100000;
        int lowestY = 100000;
        short lowestHeight = Short.MAX_VALUE;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                short rockHeight = Tiles.decodeHeight(rockTile);
                int caveTile = Server.caveMesh.getTile(tilex + x, tiley + y);
                short cheight = Tiles.decodeHeight(caveTile);
                if (!TileRockBehaviour.isNullWall(caveTile) && rockHeight - cheight >= 255) {
                    performer.getCommunicator().sendNormalServerMessage("The mountainside would risk crumbling. You cannot tunnel here.");
                    return null;
                }
                if (lowestHeight == Short.MAX_VALUE) {
                    lowestHeight = rockHeight;
                    lowestX = tilex + x;
                    lowestY = tiley + y;
                    continue;
                }
                if (rockHeight >= lowestHeight) continue;
                lowestHeight = rockHeight;
                lowestX = tilex + x;
                lowestY = tiley + y;
            }
        }
        return new Point(lowestX, lowestY, lowestHeight);
    }

    private static Point findNextLowestCorner(Creature performer, int tilex, int tiley, Point lowestCorner) {
        int nextLowestX = lowestCorner.getX();
        int nextLowestY = tiley + (1 - (lowestCorner.getY() - tiley));
        int nsRockTile = Server.rockMesh.getTile(nextLowestX, nextLowestY);
        short nextLowestHeight = Tiles.decodeHeight(nsRockTile);
        int weX = tilex + (1 - (lowestCorner.getX() - tilex));
        int weRockTile = Server.rockMesh.getTile(weX, lowestCorner.getY());
        short weRockHeight = Tiles.decodeHeight(weRockTile);
        if (weRockHeight < nextLowestHeight) {
            nextLowestHeight = weRockHeight;
            nextLowestX = weX;
            nextLowestY = lowestCorner.getY();
        }
        return new Point(nextLowestX, nextLowestY, nextLowestHeight);
    }

    public static Point findHighestCorner(int tilex, int tiley) {
        int highestX = 100000;
        int highestY = 100000;
        short highestHeight = Short.MAX_VALUE;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                short rockHeight = Tiles.decodeHeight(rockTile);
                if (highestHeight == Short.MAX_VALUE) {
                    highestHeight = rockHeight;
                    highestX = tilex + x;
                    highestY = tiley + y;
                    continue;
                }
                if (rockHeight <= highestHeight) continue;
                highestHeight = rockHeight;
                highestX = tilex + x;
                highestY = tiley + y;
            }
        }
        return new Point(highestX, highestY, highestHeight);
    }

    public static Point findNextHighestCorner(int tilex, int tiley, Point highestCorner) {
        int nextHighestX = highestCorner.getX();
        int nextHighestY = tiley + (1 - (highestCorner.getY() - tiley));
        int nsRockTile = Server.rockMesh.getTile(nextHighestX, nextHighestY);
        short nextHighestHeight = Tiles.decodeHeight(nsRockTile);
        int weX = tilex + (1 - (highestCorner.getX() - tilex));
        int weRockTile = Server.rockMesh.getTile(weX, highestCorner.getY());
        short weRockHeight = Tiles.decodeHeight(weRockTile);
        if (weRockHeight > nextHighestHeight) {
            nextHighestHeight = weRockHeight;
            nextHighestX = weX;
            nextHighestY = highestCorner.getY();
        }
        return new Point(nextHighestX, nextHighestY, nextHighestHeight);
    }

    private static boolean mayLowerCornerOnSlope(int targetHeight, Creature performer, int checkedTile) {
        short nCHeight = Tiles.decodeHeight(checkedTile);
        if (nCHeight - targetHeight > 270) {
            performer.getCommunicator().sendNormalServerMessage("The mountainside would risk crumbling. You can't open a hole here.");
            return false;
        }
        return true;
    }

    private static boolean areAllTilesRockOrReinforcedRock(int tilex, int tiley, int tile, int direction, boolean creatingExit, boolean makingWideTunnel) {
        boolean checkTile = false;
        int t = 0;
        byte type = 0;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                t = Server.caveMesh.getTile(tilex + x, tiley + y);
                type = Tiles.decodeType(t);
                if (direction == 3) {
                    if (y <= 0) {
                        checkTile = true;
                    }
                } else if (direction == 4) {
                    if (x >= 0) {
                        checkTile = true;
                    }
                } else if (direction == 5) {
                    if (y >= 0) {
                        checkTile = true;
                    }
                } else if (x <= 0) {
                    checkTile = true;
                }
                if (checkTile && creatingExit && type != Tiles.Tile.TILE_CAVE_WALL.id && type != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id && !Tiles.isReinforcedCave(type) && (type != Tiles.Tile.TILE_CAVE_EXIT.id || !makingWideTunnel)) {
                    return false;
                }
                checkTile = false;
            }
        }
        return true;
    }

    static boolean isInsideTunnelOk(int tilex, int tiley, int tile, int action, int direction, Creature performer, boolean disintegrate) {
        if (!(Tiles.decodeType(tile) != Tiles.Tile.TILE_CAVE_WALL.id && Tiles.decodeType(tile) != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id && Server.getCaveResource(tilex, tiley) > 0 && !disintegrate || Tiles.decodeHeight(tile) < -25 && Tiles.decodeHeight(tile) != -100)) {
            int dir = 6;
            if (direction == 3) {
                dir = 0;
            } else if (direction == 5) {
                dir = 4;
            } else if (direction == 4) {
                dir = 2;
            }
            boolean[][] solids = new boolean[3][3];
            float minHeight = 1000000.0f;
            float maxHeight = 0.0f;
            float currHeight = 100000.0f;
            float currCeil = 0.0f;
            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    int t = Server.caveMesh.getTile(tilex + x, tiley + y);
                    solids[x + 1][y + 1] = Tiles.isSolidCave(Tiles.decodeType(t));
                    short height = Tiles.decodeHeight(t);
                    int ceil = Tiles.decodeData(t) & 0xFF;
                    boolean setCurrHeight = false;
                    boolean setExitheight = false;
                    if (dir == 0) {
                        if (x == 0 && y == 1 || x == 1 && y == 1) {
                            setCurrHeight = true;
                        } else if (y == 0 && x >= 0) {
                            setExitheight = true;
                        }
                    } else if (dir == 2) {
                        if (x == 0 && y == 0 || x == 0 && y == 1) {
                            setCurrHeight = true;
                        } else if (x == 1 && y >= 0) {
                            setExitheight = true;
                        }
                    } else if (dir == 6) {
                        if (x == 1 && y == 1 || x == 1 && y == 0) {
                            setCurrHeight = true;
                        } else if (x == 0 && y >= 0) {
                            setExitheight = true;
                        }
                    } else if (dir == 4) {
                        if (x == 0 && y == 0 || x == 1 && y == 0) {
                            setCurrHeight = true;
                        } else if (y == 1 && x >= 0) {
                            setExitheight = true;
                        }
                    }
                    if (setCurrHeight) {
                        if ((float)height < currHeight) {
                            currHeight = height;
                        }
                        if ((float)(height + ceil) > currCeil) {
                            currCeil = height + ceil;
                        }
                    }
                    if (!setExitheight || TileRockBehaviour.isNullWall(t)) continue;
                    if ((float)height < minHeight) {
                        minHeight = height;
                    }
                    if (!((float)(height + ceil) > maxHeight)) continue;
                    maxHeight = height + ceil;
                }
            }
            if (!solids[0][0] && solids[1][0] && solids[0][1]) {
                performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
                return false;
            }
            if (!solids[2][0] && solids[2][1] && solids[1][0]) {
                performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
                return false;
            }
            if (!solids[0][2] && solids[1][2] && solids[0][1]) {
                performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
                return false;
            }
            if (!solids[2][2] && solids[1][2] && solids[2][1]) {
                performer.getCommunicator().sendNormalServerMessage("The cave walls sound hollow. A dangerous side shaft could emerge.");
                return false;
            }
            if (action == 147) {
                if (currHeight - 20.0f < minHeight) {
                    minHeight = currHeight - 20.0f;
                }
            } else if (action == 146 && currCeil + 20.0f > maxHeight) {
                maxHeight = currCeil + 20.0f;
            }
            if (maxHeight - minHeight > 254.0f) {
                performer.getCommunicator().sendNormalServerMessage("A dangerous crack is starting to form on the floor. You will have to find another way.");
                return false;
            }
            if (maxHeight - minHeight > 100.0f) {
                performer.getCommunicator().sendNormalServerMessage("You hear falling rocks from the other side of the wall. A deep shaft will probably emerge.");
            }
            return true;
        }
        return false;
    }

    private static boolean wouldPassThroughRockLayer(int tilex, int tiley, int tile, int action) {
        short s;
        short ht;
        int y;
        int x;
        int n = -100000;
        short minRockHeight = 100000;
        for (x = 0; x <= 1; ++x) {
            for (y = 0; y <= 1; ++y) {
                tile = Server.caveMesh.getTile(tilex + x, tiley + y);
                ht = Tiles.decodeHeight(tile);
                boolean allSolid = true;
                if (ht != -100) {
                    for (int xx = -1; xx <= 0 && allSolid; ++xx) {
                        for (int yy = -1; yy <= 0 && allSolid; ++yy) {
                            int encodedTile = Server.caveMesh.getTile(tilex + x + xx, tiley + y + yy);
                            byte type = Tiles.decodeType(encodedTile);
                            if (Tiles.isSolidCave(type)) continue;
                            allSolid = false;
                        }
                    }
                    if (allSolid) {
                        ht = -100;
                        Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode(ht, Tiles.decodeType(tile), (byte)0));
                    }
                }
                if (ht <= s) continue;
                s = ht;
            }
        }
        for (x = 0; x <= 1; ++x) {
            for (y = 0; y <= 1; ++y) {
                tile = Server.rockMesh.getTile(tilex + x, tiley + y);
                ht = Tiles.decodeHeight(tile);
                if (ht >= minRockHeight) continue;
                minRockHeight = Tiles.decodeHeight(tile);
            }
        }
        int mod = 0;
        if (action == 147) {
            mod = -20;
        } else if (action == 146) {
            mod = 20;
        }
        return s + mod + 30 > minRockHeight;
    }

    /*
     * Enabled aggressive block sorting
     */
    public static boolean createInsideTunnel(int tilex, int tiley, int tile, Creature performer, int action, int direction, boolean disintegrate, @Nullable Action act) {
        if (!TileRockBehaviour.isInsideTunnelOk(tilex, tiley, tile, action, direction, performer, disintegrate)) {
            return false;
        }
        if (TileRockBehaviour.wouldPassThroughRockLayer(tilex, tiley, tile, action)) {
            int mineDir = TileRockBehaviour.getTunnelExit(tilex, tiley);
            if (mineDir == -1) {
                performer.getCommunicator().sendNormalServerMessage("The topology here makes it impossible to mine in a good way.");
                return false;
            }
            boolean makingWideTunnel = false;
            if (TileRockBehaviour.canHaveWideEntrance(performer, tilex, tiley)) {
                makingWideTunnel = true;
            }
            if (!TileRockBehaviour.areAllTilesRockOrReinforcedRock(tilex, tiley, tile, mineDir, true, makingWideTunnel)) {
                performer.getCommunicator().sendNormalServerMessage("The cave walls look very unstable. You cannot keep mining here.");
                return false;
            }
            int t = Server.surfaceMesh.getTile(tilex, tiley);
            if (Tiles.decodeType(t) != Tiles.Tile.TILE_ROCK.id && Tiles.decodeType(t) != Tiles.Tile.TILE_CLIFF.id) {
                performer.getCommunicator().sendNormalServerMessage("The cave walls look very unstable and dirt flows in. You would be buried alive.");
                return false;
            }
            if (!TileRockBehaviour.createOutInTunnel(tilex, tiley, tile, performer, 0)) {
                return false;
            }
        } else if (!TileRockBehaviour.createStandardTunnel(tilex, tiley, tile, performer, action, direction, disintegrate, act)) {
            return false;
        }
        TileEvent.log(tilex, tiley, -1, performer.getWurmId(), 227);
        return true;
    }

    static final boolean allCornersAtRockHeight(int tilex, int tiley) {
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                short rockHeight;
                int ceil;
                int cavet = Server.caveMesh.getTile(tilex + x, tiley + y);
                short caveheight = Tiles.decodeHeight(cavet);
                if (caveheight + (ceil = Tiles.decodeData(cavet) & 0xFF) == (rockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(tilex + x, tiley + y)))) continue;
                return false;
            }
        }
        return true;
    }

    public static final int getCurrentCeilingHeight(int tilex, int tiley) {
        int cavet = Server.caveMesh.getTile(tilex, tiley);
        return Tiles.decodeHeight(cavet) + (Tiles.decodeData(cavet) & 0xFF);
    }

    private static final int getRockHeight(int tilex, int tiley) {
        int rockTile = Server.rockMesh.getTile(tilex, tiley);
        return Tiles.decodeHeight(rockTile);
    }

    private static final boolean isNullWall(int tile) {
        byte cavetype = Tiles.decodeType(tile);
        if (!Tiles.isSolidCave(cavetype)) {
            return false;
        }
        return Tiles.decodeHeight(tile) == -100 && (Tiles.decodeData(tile) & 0xFF) == 0;
    }

    private static final int[] getFloorAndCeiling(int tilex, int tiley, int fromHeight, int mod, boolean tryZeroCeiling, boolean tryCeilingAtRockHeight, Creature performer) {
        int targetFloor = fromHeight + mod;
        boolean fixedHeight = false;
        for (int x = -1; x <= 0; ++x) {
            for (int y = -1; y <= 0; ++y) {
                VolaTile vt = Zones.getTileOrNull(tilex + x, tiley + y, false);
                if (vt == null || vt.getStructure() == null) continue;
                fixedHeight = true;
                int tile = Server.caveMesh.getTile(tilex + x, tiley + y);
                targetFloor = Tiles.decodeHeight(tile);
            }
        }
        int targetCeiling = targetFloor + 30;
        if (!(tryZeroCeiling || tryCeilingAtRockHeight || fixedHeight)) {
            if (Server.rand.nextInt(5) == 0) {
                targetCeiling = TileRockBehaviour.maybeAddExtraSlopes(performer, targetCeiling);
            }
            if (Server.rand.nextInt(5) == 0) {
                targetFloor = TileRockBehaviour.maybeAddExtraSlopes(performer, targetFloor);
            }
        } else if (tryZeroCeiling) {
            targetCeiling = targetFloor;
        }
        int rockHeight = TileRockBehaviour.getRockHeight(tilex, tiley);
        int tile = Server.caveMesh.getTile(tilex, tiley);
        short currentFloor = Tiles.decodeHeight(tile);
        int currentCeiling = currentFloor + (Tiles.decodeData(tile) & 0xFF);
        if (targetFloor >= currentFloor && !TileRockBehaviour.isNullWall(tile)) {
            targetFloor = currentFloor;
        }
        if (targetCeiling <= currentCeiling) {
            targetCeiling = currentCeiling;
            if (mod > 0 && targetFloor < currentFloor && !TileRockBehaviour.isNullWall(tile)) {
                targetFloor = currentFloor;
            }
        }
        if (targetCeiling >= rockHeight || tryCeilingAtRockHeight) {
            targetCeiling = rockHeight;
        }
        if (targetFloor >= rockHeight) {
            targetFloor = rockHeight;
        }
        if (targetCeiling - targetFloor >= 255) {
            if (targetFloor < currentFloor) {
                targetFloor = currentCeiling - 255;
                targetCeiling = currentCeiling;
            } else {
                targetCeiling = Math.min(currentCeiling, targetFloor + 255);
            }
        }
        if (targetCeiling < 5 && !tryZeroCeiling) {
            targetCeiling = 5;
        }
        return new int[]{targetFloor, targetCeiling};
    }

    private static final int maybeAddExtraSlopes(Creature performer, int _previousValue) {
        int miningSkillMod;
        if (performer.getPower() > 0) {
            return _previousValue;
        }
        if (performer instanceof Player) {
            Player p = (Player)performer;
            Skill mine = null;
            try {
                Skills skills = p.getSkills();
                mine = skills.getSkill(1008);
            }
            catch (NoSuchSkillException nss) {
                logger.info(performer.getName() + ": No such skill for mining? " + nss);
            }
            double realKnowledge = mine == null ? 1.0 : mine.getKnowledge(0.0);
            if (realKnowledge > 90.0) {
                return _previousValue;
            }
            miningSkillMod = realKnowledge > 70.0 ? 1 : (realKnowledge > 50.0 ? 2 : 3);
        } else {
            miningSkillMod = 3;
        }
        int randVal = Server.rand.nextInt(miningSkillMod * 2 + 1);
        return _previousValue - miningSkillMod + randVal;
    }

    private static final void maybeCreateSource(int tilex, int tiley, Creature performer) {
        if (!(Server.rand.nextInt(10000) != 0 && (!Servers.localServer.testServer || performer.getPower() < 5 || Server.rand.nextInt(10) != 0) || Servers.localServer.EPIC && Servers.localServer.HOMESERVER || Items.getSourceSprings().length <= 0 || Items.getSourceSprings().length >= Zones.worldTileSizeX / 20)) {
            try {
                Item target1 = ItemFactory.createItem(767, 100.0f, tilex * 4 + 2, tiley * 4 + 2, Server.rand.nextInt(360), false, (byte)0, -10L, "");
                target1.setSizes(target1.getSizeX() + Server.rand.nextInt(1), target1.getSizeY() + Server.rand.nextInt(2), target1.getSizeZ() + Server.rand.nextInt(3));
                logger.log(Level.INFO, "Created " + target1.getName() + " at " + target1.getTileX() + " " + target1.getTileY() + " sizes " + target1.getSizeX() + "," + target1.getSizeY() + "," + target1.getSizeZ() + ")");
                Items.addSourceSpring(target1);
                performer.getCommunicator().sendSafeServerMessage("You find a source spring!");
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage(), nst);
            }
        }
    }

    private static final boolean createStandardTunnel(int tilex, int tiley, int tile, Creature performer, int action, int direction, boolean disintegrate, @Nullable Action act) {
        if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL.id || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id || Server.getCaveResource(tilex, tiley) <= 0 || disintegrate) {
            if (TileRockBehaviour.areAllTilesRockOrReinforcedRock(tilex, tiley, tile, direction, false, false)) {
                if (Tiles.decodeHeight(tile) >= -25 || Tiles.decodeHeight(tile) == -100) {
                    Item gem;
                    int fromx;
                    int dir = 6;
                    if (direction == 3) {
                        dir = 0;
                    } else if (direction == 5) {
                        dir = 4;
                    } else if (direction == 4) {
                        dir = 2;
                    }
                    int mod = 0;
                    if (action == 147) {
                        mod = -20;
                    } else if (action == 146) {
                        mod = 20;
                    }
                    if (disintegrate) {
                        Server.setCaveResource(tilex, tiley, 0);
                    }
                    if (dir == 0) {
                        int fromy2;
                        int fromx2;
                        int t2;
                        short height2;
                        fromx = tilex;
                        int fromy = tiley + 1;
                        int t = Server.caveMesh.getTile(fromx, fromy);
                        short height = Tiles.decodeHeight(t);
                        short avheight = (short)((height + (height2 = Tiles.decodeHeight(t2 = Server.caveMesh.getTile(fromx2 = tilex + 1, fromy2 = tiley + 1)))) / 2);
                        int[] newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex, tiley, avheight, mod, false, false, performer);
                        int newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        int newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex, tiley, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE.id, (byte)(newCeil - newFloorHeight)));
                        TileRockBehaviour.maybeCreateSource(tilex, tiley, performer);
                        t2 = Server.caveMesh.getTile(tilex + 1, tiley);
                        newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex + 1, tiley, avheight, mod, false, false, performer);
                        newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex + 1, tiley, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
                        TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
                    } else if (dir == 4) {
                        fromx = tilex;
                        int fromy = tiley;
                        int t = Server.caveMesh.getTile(fromx, fromy);
                        short height = Tiles.decodeHeight(t);
                        Server.caveMesh.setTile(tilex, tiley, Tiles.encode(height, Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(t)));
                        TileRockBehaviour.maybeCreateSource(tilex, tiley, performer);
                        int fromx2 = tilex + 1;
                        int fromy2 = tiley;
                        int t2 = Server.caveMesh.getTile(fromx2, fromy2);
                        short height2 = Tiles.decodeHeight(t2);
                        short avheight = (short)((height + height2) / 2);
                        t2 = Server.caveMesh.getTile(tilex, tiley + 1);
                        int[] newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex, tiley + 1, avheight, mod, false, false, performer);
                        int newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        int newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
                        t2 = Server.caveMesh.getTile(tilex + 1, tiley + 1);
                        newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex + 1, tiley + 1, avheight, mod, false, false, performer);
                        newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex + 1, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
                        TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
                    } else if (dir == 2) {
                        fromx = tilex;
                        int fromy = tiley;
                        int t = Server.caveMesh.getTile(fromx, fromy);
                        short height = Tiles.decodeHeight(t);
                        Server.caveMesh.setTile(tilex, tiley, Tiles.encode(height, Tiles.Tile.TILE_CAVE.id, Tiles.decodeData(t)));
                        TileRockBehaviour.maybeCreateSource(tilex, tiley, performer);
                        int fromx2 = tilex;
                        int fromy2 = tiley + 1;
                        int t2 = Server.caveMesh.getTile(fromx2, fromy2);
                        short height2 = Tiles.decodeHeight(t2);
                        short avheight = (short)((height + height2) / 2);
                        t2 = Server.caveMesh.getTile(tilex + 1, tiley);
                        int[] newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex + 1, tiley, avheight, mod, false, false, performer);
                        int newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        int newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex + 1, tiley, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
                        t2 = Server.caveMesh.getTile(tilex + 1, tiley + 1);
                        newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex + 1, tiley + 1, avheight, mod, false, false, performer);
                        newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex + 1, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
                        TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
                    } else if (dir == 6) {
                        int fromy2;
                        int fromx2;
                        int t2;
                        short height2;
                        fromx = tilex + 1;
                        int fromy = tiley;
                        int t = Server.caveMesh.getTile(fromx, fromy);
                        short height = Tiles.decodeHeight(t);
                        short avheight = (short)((height + (height2 = Tiles.decodeHeight(t2 = Server.caveMesh.getTile(fromx2 = tilex + 1, fromy2 = tiley + 1)))) / 2);
                        int[] newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex, tiley, avheight, mod, false, false, performer);
                        int newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        int newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex, tiley, Tiles.encode((short)newFloorHeight, Tiles.Tile.TILE_CAVE.id, (byte)(newCeil - newFloorHeight)));
                        TileRockBehaviour.maybeCreateSource(tilex, tiley, performer);
                        t2 = Server.caveMesh.getTile(tilex, tiley + 1);
                        newfloorceil = TileRockBehaviour.getFloorAndCeiling(tilex, tiley + 1, avheight, mod, false, false, performer);
                        newFloorHeight = newfloorceil[0];
                        if (newFloorHeight < -25) {
                            newFloorHeight = -25;
                        }
                        newCeil = newfloorceil[1];
                        Server.caveMesh.setTile(tilex, tiley + 1, Tiles.encode((short)newFloorHeight, Tiles.decodeType(t2), (byte)(newCeil - newFloorHeight)));
                        TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
                    }
                    if (!performer.isPlayer() && (gem = TileRockBehaviour.createGem(-1, -1, performer, Server.rand.nextFloat() * 100.0f, false, act)) != null) {
                        performer.getInventory().insertItem(gem);
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static final void sendCaveTile(int tilex, int tiley, int diffX, int diffY) {
        Players.getInstance().sendChangedTile(tilex + diffX, tiley + diffY, false, true);
        for (int x = -1; x <= 0; ++x) {
            for (int y = -1; y <= 0; ++y) {
                try {
                    Zone toCheckForChange = Zones.getZone(tilex + diffX + x, tiley + diffY + y, false);
                    toCheckForChange.changeTile(tilex + diffX + x, tiley + diffY + y);
                    continue;
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.INFO, "no such zone?: " + (tilex + diffX + x) + ", " + (tiley + diffY + y), nsz);
                }
            }
        }
    }

    public static final boolean surroundedByWalls(int x, int y) {
        if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x - 1, y)))) {
            return false;
        }
        if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x + 1, y)))) {
            return false;
        }
        if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y - 1)))) {
            return false;
        }
        return Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y + 1)));
    }

    public static final void reProspect() {
        int numsChanged = 0;
        int numsUntouched = 0;
        for (int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); ++x) {
            int xx = x & (1 << Constants.meshSize) - 1;
            int yy = x >> Constants.meshSize;
            int old = Server.caveMesh.getTile(xx, yy);
            if (!Tiles.isOreCave(Tiles.decodeType(old)) || xx <= 5 || yy <= 5 || xx >= worldSizeX - 3 || yy >= worldSizeX - 3) continue;
            if (TileRockBehaviour.surroundedByWalls(xx, yy)) {
                byte newType = TileRockBehaviour.prospect(xx, yy, true);
                Server.caveMesh.setTile(xx, yy, Tiles.encode(Tiles.decodeHeight(old), newType, Tiles.decodeData(old)));
                ++numsChanged;
                continue;
            }
            ++numsUntouched;
        }
        logger.log(Level.INFO, "Reprospect finished. Changed=" + numsChanged + ", untouched=" + numsUntouched);
        try {
            Server.caveMesh.saveAllDirtyRows();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
    }

    public static final Item createRandomGem() {
        return TileRockBehaviour.createRandomGem(100.0f);
    }

    public static final Item createRandomGem(float maxql) {
        try {
            int rand = Server.rand.nextInt(300);
            int templateId = 349;
            float ql = Server.rand.nextFloat() * maxql;
            if (rand < 50) {
                templateId = 349;
            } else if (rand < 100) {
                templateId = 446;
            } else if (rand < 140) {
                templateId = 376;
                if (ql >= 99.0f) {
                    templateId = 377;
                }
            } else if (rand < 180) {
                templateId = 374;
                if (ql >= 99.0f) {
                    templateId = 375;
                }
            } else if (rand < 220) {
                templateId = 382;
                if (ql >= 99.0f) {
                    templateId = 383;
                }
            } else if (rand < 260) {
                templateId = 378;
                if (ql >= 99.0f) {
                    templateId = 379;
                }
            } else if (rand < 300) {
                templateId = 380;
                if (ql >= 99.0f) {
                    templateId = 381;
                }
            }
            return ItemFactory.createItem(templateId, Server.rand.nextFloat() * ql, null);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        return null;
    }

    static final Item createGem(int minedTilex, int minedTiley, Creature performer, double power, boolean surfaced, @Nullable Action act) {
        return TileRockBehaviour.createGem(minedTilex, minedTiley, minedTilex, minedTiley, performer, power, surfaced, act);
    }

    static final Item createGem(int tilex, int tiley, int createtilex, int createtiley, Creature performer, double power, boolean surfaced, @Nullable Action act) {
        byte rarity = act != null ? act.getRarity() : (byte)0;
        try {
            rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 102533L);
            if (rockRandom.nextInt(100) == 0 && Server.rand.nextInt(10) == 0) {
                if (tilex < 0 && tiley < 0) {
                    Item gem = ItemFactory.createItem(349, (float)power, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    return gem;
                }
                Item salt = ItemFactory.createItem(349, (float)power, rarity, null);
                salt.setLastOwnerId(performer.getWurmId());
                salt.putItemInfrontof(performer, 0.0f);
                performer.getCommunicator().sendNormalServerMessage("You mine some salt.");
            }
            rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * SOURCE_PRIME);
            if (rockRandom.nextInt(sourceFactor) == 0) {
                boolean isVein = Tiles.isOreCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)));
                if (Server.rand.nextInt(10) == 0 && !isVein) {
                    if (tilex < 0 && tiley < 0) {
                        Item gem = ItemFactory.createItem(765, (float)power, null);
                        gem.setLastOwnerId(performer.getWurmId());
                        return gem;
                    }
                    Item crystal = ItemFactory.createItem(765, (float)power, rarity, null);
                    crystal.setLastOwnerId(performer.getWurmId());
                    crystal.putItemInfrontof(performer, 0.0f);
                    performer.getCommunicator().sendNormalServerMessage("You mine some pink crystals.");
                }
            }
            rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 6883L);
            if (rockRandom.nextInt(200) == 0 && Server.rand.nextInt(40) == 0) {
                if (tilex < 0 && tiley < 0) {
                    Item gem = ItemFactory.createItem(446, (float)power, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    return gem;
                }
                Item flint = ItemFactory.createItem(446, (float)power, rarity, null);
                flint.setLastOwnerId(performer.getWurmId());
                flint.putItemInfrontof(performer, 0.0f);
                performer.getCommunicator().sendNormalServerMessage("You find flint!");
            }
            if (Server.rand.nextInt(1000) == 0) {
                int rand = Server.rand.nextInt(5);
                if (rand == 0) {
                    int templateId = 376;
                    float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0f);
                    if (ql >= 99.0f) {
                        templateId = 377;
                    }
                    if (tilex < 0 && tiley < 0) {
                        Item gem = ItemFactory.createItem(templateId, (float)power, null);
                        gem.setLastOwnerId(performer.getWurmId());
                        return gem;
                    }
                    Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    gem.putItemInfrontof(performer, 0.0f);
                    if (ql >= 99.0f) {
                        performer.achievement(298);
                    }
                    if (gem.getQualityLevel() > 90.0f) {
                        performer.achievement(299);
                    }
                    if (rarity > 2) {
                        performer.achievement(334);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
                } else if (rand == 1) {
                    int templateId = 374;
                    float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0f);
                    if (ql >= 99.0f) {
                        templateId = 375;
                    }
                    if (tilex < 0 && tiley < 0) {
                        Item gem = ItemFactory.createItem(templateId, (float)power, null);
                        gem.setLastOwnerId(performer.getWurmId());
                        return gem;
                    }
                    Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    gem.putItemInfrontof(performer, 0.0f);
                    if (ql >= 99.0f) {
                        performer.achievement(298);
                    }
                    if (gem.getQualityLevel() > 90.0f) {
                        performer.achievement(299);
                    }
                    if (rarity > 2) {
                        performer.achievement(334);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
                } else if (rand == 2) {
                    int templateId = 382;
                    float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0f);
                    if (ql >= 99.0f) {
                        templateId = 383;
                    }
                    if (tilex < 0 && tiley < 0) {
                        Item gem = ItemFactory.createItem(templateId, (float)power, null);
                        gem.setLastOwnerId(performer.getWurmId());
                        return gem;
                    }
                    Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    gem.putItemInfrontof(performer, 0.0f);
                    if (ql >= 99.0f) {
                        performer.achievement(298);
                    }
                    if (gem.getQualityLevel() > 90.0f) {
                        performer.achievement(299);
                    }
                    if (rarity > 2) {
                        performer.achievement(334);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
                } else if (rand == 3) {
                    int templateId = 378;
                    float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0f);
                    if (ql >= 99.0f) {
                        templateId = 379;
                    }
                    if (tilex < 0 && tiley < 0) {
                        Item gem = ItemFactory.createItem(templateId, (float)power, null);
                        gem.setLastOwnerId(performer.getWurmId());
                        return gem;
                    }
                    Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    gem.putItemInfrontof(performer, 0.0f);
                    if (ql >= 99.0f) {
                        performer.achievement(298);
                    }
                    if (gem.getQualityLevel() > 90.0f) {
                        performer.achievement(299);
                    }
                    if (rarity > 2) {
                        performer.achievement(334);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
                } else {
                    int templateId = 380;
                    float ql = Math.min((float)MAX_QL, Server.rand.nextFloat() * 100.0f);
                    if (ql >= 99.0f) {
                        templateId = 381;
                    }
                    if (tilex < 0 && tiley < 0) {
                        Item gem = ItemFactory.createItem(templateId, (float)power, null);
                        gem.setLastOwnerId(performer.getWurmId());
                        return gem;
                    }
                    Item gem = ItemFactory.createItem(templateId, (float)power, rarity, null);
                    gem.setLastOwnerId(performer.getWurmId());
                    gem.putItemInfrontof(performer, 0.0f);
                    if (ql >= 99.0f) {
                        performer.achievement(298);
                    }
                    if (gem.getQualityLevel() > 90.0f) {
                        performer.achievement(299);
                    }
                    if (rarity > 2) {
                        performer.achievement(334);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!");
                }
            }
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, performer.getName() + ": " + fe.getMessage(), fe);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, performer.getName() + ": no template", nst);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Factory failed to produce item", ex);
        }
        return null;
    }

    public static boolean cannotMineSlope(Creature performer, Skill mining, int digTilex, int digTiley) {
        int diff = Terraforming.getMaxSurfaceDifference(Server.surfaceMesh.getTile(digTilex, digTiley), digTilex, digTiley);
        int maxSlope = (int)(mining.getKnowledge(0.0) * (double)(Servers.localServer.PVPSERVER ? 1 : 3));
        if (Math.signum(diff) == 1.0f && diff > maxSlope) {
            performer.getCommunicator().sendNormalServerMessage("You are too unskilled to mine here.", (byte)3);
            return true;
        }
        if (Math.signum(diff) == -1.0f && -1 - diff > maxSlope) {
            performer.getCommunicator().sendNormalServerMessage("You are too unskilled to mine here.", (byte)3);
            return true;
        }
        return false;
    }

    public static final boolean mine(Action act, Creature performer, Item source, int tilex, int tiley, short action, float counter, int digTilex, int digTiley) {
        boolean done = true;
        int tile = Server.surfaceMesh.getTile(digTilex, digTiley);
        if (digTilex < 1 || digTilex > (1 << Constants.meshSize) - 1 || digTiley < 1 || digTiley > (1 << Constants.meshSize) - 1) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
            return true;
        }
        if (Zones.isTileProtected(digTilex, digTiley)) {
            performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
            return true;
        }
        short h = Tiles.decodeHeight(tile);
        if (h > -25) {
            int x;
            done = false;
            Skills skills = performer.getSkills();
            Skill mining = null;
            Skill tool = null;
            boolean insta = performer.getPower() > 3 && source.isWand();
            try {
                mining = skills.getSkill(1008);
            }
            catch (Exception ex) {
                mining = skills.learn(1008, 1.0f);
            }
            try {
                tool = skills.getSkill(source.getPrimarySkill());
            }
            catch (Exception ex) {
                try {
                    tool = skills.learn(source.getPrimarySkill(), 1.0f);
                }
                catch (NoSuchSkillException nse) {
                    logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source.getName());
                }
            }
            for (x = -1; x <= 0; ++x) {
                for (int y = -1; y <= 0; ++y) {
                    byte decType = Tiles.decodeType(Server.surfaceMesh.getTile(digTilex + x, digTiley + y));
                    if (decType == Tiles.Tile.TILE_ROCK.id || decType == Tiles.Tile.TILE_CLIFF.id) continue;
                    performer.getCommunicator().sendNormalServerMessage("The surrounding area needs to be rock before you mine.", (byte)3);
                    return true;
                }
            }
            for (x = 0; x >= -1; --x) {
                for (int y = 0; y >= -1; --y) {
                    VolaTile vt = Zones.getTileOrNull(digTilex + x, digTiley + y, true);
                    if (vt == null || vt.getStructure() == null) continue;
                    if (vt.getStructure().isTypeHouse()) {
                        if (x == 0 && y == 0) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot mine in a building.", (byte)3);
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a building.", (byte)3);
                        }
                        return true;
                    }
                    for (BridgePart bp : vt.getBridgeParts()) {
                        if (bp.getType().isSupportType()) {
                            performer.getCommunicator().sendNormalServerMessage("The bridge support nearby prevents mining.");
                            return true;
                        }
                        if (!(x == -1 && bp.hasEastExit() || x == 0 && bp.hasWestExit() || y == -1 && bp.hasSouthExit()) && (y != 0 || !bp.hasNorthExit())) continue;
                        performer.getCommunicator().sendNormalServerMessage("The end of the bridge nearby prevents mining.");
                        return true;
                    }
                }
            }
            VolaTile vt = Zones.getTileOrNull(digTilex, digTiley, true);
            if (vt != null && vt.getFencesForLevel(0).length > 0) {
                performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
                return true;
            }
            vt = Zones.getTileOrNull(digTilex, digTiley - 1, true);
            if (vt != null && vt.getFencesForLevel(0).length > 0) {
                for (Fence f : vt.getFencesForLevel(0)) {
                    if (f.isHorizontal()) continue;
                    performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
                    return true;
                }
            }
            if ((vt = Zones.getTileOrNull(digTilex - 1, digTiley, true)) != null && vt.getFencesForLevel(0).length > 0) {
                for (Fence f : vt.getFencesForLevel(0)) {
                    if (!f.isHorizontal()) continue;
                    performer.getCommunicator().sendNormalServerMessage("You cannot mine next to a fence.", (byte)3);
                    return true;
                }
            }
            int time = 0;
            VolaTile dropTile = Zones.getTileOrNull((int)performer.getPosX() >> 2, (int)performer.getPosY() >> 2, true);
            if (dropTile != null && dropTile.getNumberOfItems(performer.getFloorLevel()) > 99) {
                performer.getCommunicator().sendNormalServerMessage("There is no space to mine here. Clear the area first.", (byte)3);
                return true;
            }
            if (counter == 1.0f) {
                if (TileRockBehaviour.cannotMineSlope(performer, mining, digTilex, digTiley)) {
                    return true;
                }
                time = Actions.getStandardActionTime(performer, mining, source, 0.0);
                try {
                    performer.getCurrentAction().setTimeLeft(time);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                Server.getInstance().broadCastAction(performer.getName() + " starts mining.", performer, 5);
                performer.getCommunicator().sendNormalServerMessage("You start to mine.");
                performer.sendActionControl(Actions.actionEntrys[145].getVerbString(), true, time);
                source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                performer.getStatus().modifyStamina(-1000.0f);
            } else {
                try {
                    time = performer.getCurrentAction().getTimeLeft();
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                if (counter * 10.0f <= (float)time && !insta) {
                    if (act.currentSecond() % 5 == 0 || act.currentSecond() == 3 && time < 50) {
                        String sstring = "sound.work.mining1";
                        int x2 = Server.rand.nextInt(3);
                        if (x2 == 0) {
                            sstring = "sound.work.mining2";
                        } else if (x2 == 1) {
                            sstring = "sound.work.mining3";
                        }
                        SoundPlayer.playSound(sstring, digTilex, digTiley, performer.isOnSurface(), 0.0f);
                        source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                        performer.getStatus().modifyStamina(-7000.0f);
                    }
                } else {
                    short maxDiff;
                    if (act.getRarity() != 0) {
                        performer.playPersonalSound("sound.fx.drumroll");
                    }
                    if (TileRockBehaviour.cannotMineSlope(performer, mining, digTilex, digTiley)) {
                        return true;
                    }
                    double bonus = 0.0;
                    double power = 0.0;
                    done = true;
                    int itemTemplateCreated = 146;
                    float diff = 1.0f;
                    int caveTile = Server.caveMesh.getTile(digTilex, digTiley);
                    short caveFloor = Tiles.decodeHeight(caveTile);
                    int caveCeilingHeight = caveFloor + (short)(Tiles.decodeData(caveTile) & 0xFF);
                    MeshIO mesh = Server.surfaceMesh;
                    if (h - 1 <= caveCeilingHeight) {
                        performer.getCommunicator().sendNormalServerMessage("The rock sounds hollow. You need to tunnel to proceed.", (byte)3);
                        return true;
                    }
                    double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(1008) / 100.0;
                    int lNewTile = mesh.getTile(digTilex - 1, digTiley);
                    if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff = (short)Math.max(10.0, mining.getKnowledge(0.0) * 3.0 * imbueEnhancement))) {
                        return true;
                    }
                    lNewTile = mesh.getTile(digTilex + 1, digTiley);
                    if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                        return true;
                    }
                    lNewTile = mesh.getTile(digTilex, digTiley - 1);
                    if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                        return true;
                    }
                    lNewTile = mesh.getTile(digTilex, digTiley + 1);
                    if (Terraforming.checkMineSurfaceTile(lNewTile, performer, h, maxDiff)) {
                        return true;
                    }
                    if (Terraforming.isAltarBlocking(performer, tilex, tiley)) {
                        performer.getCommunicator().sendSafeServerMessage("You cannot build here, since this is holy ground.", (byte)2);
                        return true;
                    }
                    if (performer.getTutorialLevel() == 10 && !performer.skippedTutorial()) {
                        performer.missionFinished(true, true);
                    }
                    float tickCounter = counter;
                    if (tool != null) {
                        bonus = tool.skillCheck(1.0, source, 0.0, false, tickCounter) / 5.0;
                    }
                    power = Math.max(1.0, mining.skillCheck(1.0, source, bonus, false, tickCounter));
                    float chance = Math.max(0.2f, (float)mining.getKnowledge(0.0) / 200.0f);
                    if (Server.rand.nextFloat() < chance) {
                        try {
                            if (mining.getKnowledge(0.0) * imbueEnhancement < power) {
                                power = mining.getKnowledge(0.0) * imbueEnhancement;
                            }
                            rockRandom.setSeed((long)(digTilex + digTiley * Zones.worldTileSizeY) * 789221L);
                            int m = 100;
                            int max = Math.min(100, (int)(20.0 + (double)rockRandom.nextInt(80) * imbueEnhancement));
                            power = Math.min(power, (double)max);
                            if (source.isCrude()) {
                                power = 1.0;
                            }
                            float modifier = 1.0f;
                            if (source.getSpellEffects() != null) {
                                modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                            }
                            float orePower = GeneralUtilities.calcOreRareQuality(power * (double)modifier, act.getRarity(), source.getRarity());
                            Item newItem = ItemFactory.createItem(146, orePower, performer.getPosX(), performer.getPosY(), Server.rand.nextFloat() * 360.0f, performer.isOnSurface(), act.getRarity(), -10L, null);
                            newItem.setLastOwnerId(performer.getWurmId());
                            newItem.setDataXY(tilex, tiley);
                            performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                            Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
                            TileEvent.log(digTilex, digTiley, 0, performer.getWurmId(), action);
                            short newHeight = (short)(h - 1);
                            mesh.setTile(digTilex, digTiley, Tiles.encode(newHeight, Tiles.Tile.TILE_ROCK.id, Tiles.decodeData(tile)));
                            Server.rockMesh.setTile(digTilex, digTiley, Tiles.encode(newHeight, (short)0));
                            for (int xx = 0; xx >= -1; --xx) {
                                for (int yy = 0; yy >= -1; --yy) {
                                    performer.getMovementScheme().touchFreeMoveCounter();
                                    Players.getInstance().sendChangedTile(digTilex + xx, digTiley + yy, performer.isOnSurface(), true);
                                    try {
                                        Zone toCheckForChange = Zones.getZone(digTilex + xx, digTiley + yy, performer.isOnSurface());
                                        toCheckForChange.changeTile(digTilex + xx, digTiley + yy);
                                        continue;
                                    }
                                    catch (NoSuchZoneException nsz) {
                                        logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                                    }
                                }
                            }
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Factory failed to produce item", ex);
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You chip away at the rock.");
                    }
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
        }
        return done;
    }

    public static final byte prospect(int x, int y, boolean reprospecting) {
        oreRand = Server.rand.nextInt(reprospecting ? 75 : 1000);
        if (oreRand < 74) {
            if (reprospecting) {
                if (minezones[x / 32][y / 32] != Tiles.Tile.TILE_CAVE_WALL.id) {
                    if (Server.rand.nextInt(5) == 0) {
                        return TileRockBehaviour.getOreId(oreRand);
                    }
                    return minezones[x / 32][y / 32];
                }
                return TileRockBehaviour.getOreId(oreRand);
            }
            if (Server.rand.nextInt(5) == 0) {
                return TileRockBehaviour.getOreId(oreRand);
            }
            byte type = minezones[x / 32][y / 32];
            return type;
        }
        return Tiles.Tile.TILE_CAVE_WALL.id;
    }

    static boolean affectsHighway(int tilex, int tiley) {
        if (MethodsHighways.onHighway(tilex, tiley - 1, true)) {
            return true;
        }
        if (MethodsHighways.onHighway(tilex + 1, tiley - 1, true)) {
            return true;
        }
        if (MethodsHighways.onHighway(tilex + 1, tiley, true)) {
            return true;
        }
        if (MethodsHighways.onHighway(tilex + 1, tiley + 1, true)) {
            return true;
        }
        if (MethodsHighways.onHighway(tilex, tiley + 1, true)) {
            return true;
        }
        if (MethodsHighways.onHighway(tilex - 1, tiley + 1, true)) {
            return true;
        }
        if (MethodsHighways.onHighway(tilex - 1, tiley, true)) {
            return true;
        }
        return MethodsHighways.onHighway(tilex - 1, tiley - 1, true);
    }

    private static byte getOreId(int num) {
        if (num < 2) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id;
        }
        if (num < 6) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id;
        }
        if (num < 10) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id;
        }
        if (num < 14) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id;
        }
        if (num < 18) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id;
        }
        if (num < 22) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id;
        }
        if (num < 72) {
            return Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id;
        }
        if (num < 73) {
            return Tiles.Tile.TILE_CAVE_WALL_MARBLE.id;
        }
        if (num < 74) {
            return Tiles.Tile.TILE_CAVE_WALL_SLATE.id;
        }
        return Tiles.Tile.TILE_CAVE_WALL.id;
    }

    static final int getItemTemplateForTile(byte type) {
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id) {
            return 43;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id) {
            return 39;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
            return 38;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
            return 41;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
            return 40;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
            return 207;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id) {
            return 42;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
            return 693;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
            return 697;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id) {
            return 785;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id) {
            return 770;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
            return 1238;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id) {
            return 1116;
        }
        return 146;
    }

    static final int getDifficultyForTile(byte type) {
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id || type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id) {
            return 20;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id || type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id) {
            return 40;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
            return 3;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
            return 20;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
            return 35;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
            return 10;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
            return 60;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
            return 55;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
            return 30;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id) {
            return 45;
        }
        return 2;
    }

    static {
        Random prand = new Random();
        prand.setSeed(181081L + (long)Servers.getLocalServerId());
        Server.rand.setSeed(789221L);
        for (int x = 0; x <= mineZoneDiv; ++x) {
            for (int y = 0; y <= mineZoneDiv; ++y) {
                int num = Server.rand.nextInt(75);
                int prandnum = prand.nextInt(4);
                TileRockBehaviour.minezones[x][y] = prandnum == 0 ? TileRockBehaviour.getOreId(num) : Tiles.Tile.TILE_CAVE_WALL.id;
            }
        }
    }
}


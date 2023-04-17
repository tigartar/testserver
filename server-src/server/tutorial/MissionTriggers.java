/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTarget;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.tutorial.TriggerRun;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MissionTriggers
implements CounterTypes,
MiscConstants {
    private static Logger logger = Logger.getLogger(MissionTriggers.class.getName());
    private static final String LOADALLTRIGGERS = "SELECT * FROM MISSIONTRIGGERS";
    private static final Map<Integer, MissionTrigger> triggers = new ConcurrentHashMap<Integer, MissionTrigger>();
    public static final int SHOW_ALL = 0;
    public static final int SHOW_LINKED = 1;
    public static final int SHOW_UNLINKED = 2;

    private MissionTriggers() {
    }

    public static void addMissionTrigger(MissionTrigger trigger) {
        triggers.put(trigger.getId(), trigger);
        MissionTargets.addMissionTrigger(trigger);
    }

    public static MissionTrigger[] getAllTriggers() {
        return triggers.values().toArray(new MissionTrigger[triggers.size()]);
    }

    public static MissionTrigger getRespawnTriggerForMission(int missionId, float state) {
        MissionTrigger[] trigs = MissionTriggers.getAllTriggers();
        float foundState = -100.0f;
        MissionTrigger toret = null;
        for (int x = 0; x < trigs.length; ++x) {
            if (trigs[x].getMissionRequired() != missionId || !(trigs[x].getStateRequired() < state) || !trigs[x].isSpawnPoint() || !(foundState < trigs[x].getStateRequired())) continue;
            foundState = trigs[x].getStateRequired();
            toret = trigs[x];
        }
        return toret;
    }

    public static int getNumTriggers() {
        return triggers.size();
    }

    public static MissionTrigger[] getFilteredTriggers(Creature creature, int triggerId, byte creatorType, long listForUser, boolean dontListMine, boolean listMineOnly) {
        HashSet<MissionTrigger> trigs = new HashSet<MissionTrigger>();
        for (MissionTrigger trig : triggers.values()) {
            boolean userMatch;
            boolean own = trig.getOwnerId() == creature.getWurmId();
            boolean show = creature.getPower() > 0 || own;
            boolean bl = userMatch = trig.getOwnerId() == listForUser;
            if (triggerId > 0 && trig.getId() == triggerId) {
                show = true;
            } else if (own) {
                if (dontListMine) {
                    show = false;
                }
            } else if (listMineOnly) {
                show = false;
                if (listForUser != -10L && userMatch) {
                    show = true;
                }
            } else if (listForUser != -10L) {
                show = false;
                if (userMatch) {
                    show = true;
                }
            }
            if (creatorType == 2 && creature.getPower() < MissionManager.CAN_SEE_EPIC_MISSIONS) {
                show = false;
            }
            if (!show) continue;
            trigs.add(trig);
        }
        return trigs.toArray(new MissionTrigger[trigs.size()]);
    }

    public static MissionTrigger[] getFilteredTriggers(Creature creature, int linked, boolean includeInactive, int missionId, int triggerId) {
        HashSet<MissionTrigger> trigs = new HashSet<MissionTrigger>();
        if (triggerId != 0) {
            MissionTrigger trig = triggers.get(triggerId);
            if (trig != null && MissionTriggers.showTrigger(trig, creature, linked, includeInactive, missionId)) {
                trigs.add(trig);
            }
        } else {
            for (MissionTrigger trig : triggers.values()) {
                if (!MissionTriggers.showTrigger(trig, creature, linked, includeInactive, missionId)) continue;
                trigs.add(trig);
            }
        }
        return trigs.toArray(new MissionTrigger[trigs.size()]);
    }

    private static boolean showTrigger(MissionTrigger trig, Creature creature, int linked, boolean includeInactive, int missionId) {
        boolean show = false;
        if (missionId == 0 || trig.getMissionRequired() == missionId) {
            if (missionId == 0) {
                switch (linked) {
                    case 0: {
                        show = true;
                        break;
                    }
                    case 1: {
                        show = trig.getMissionRequired() != 0;
                        break;
                    }
                    case 2: {
                        show = trig.getMissionRequired() == 0;
                    }
                }
            } else {
                show = true;
            }
            if (!includeInactive && !trig.isInactive()) {
                show = false;
            }
            if (show && creature.getPower() == 0 && trig.getOwnerId() != creature.getWurmId()) {
                show = false;
            }
        }
        return show;
    }

    public static MissionTrigger[] getMissionTriggers(int missionId) {
        HashSet<MissionTrigger> trigs = new HashSet<MissionTrigger>();
        for (MissionTrigger m : triggers.values()) {
            if (m.getMissionRequired() != missionId) continue;
            trigs.add(m);
        }
        return trigs.toArray(new MissionTrigger[trigs.size()]);
    }

    public static boolean hasMissionTriggers(int missionId) {
        for (MissionTrigger m : triggers.values()) {
            if (m.getMissionRequired() != missionId) continue;
            return true;
        }
        return false;
    }

    public static MissionTrigger[] getMissionTriggersWith(int itemUsed, int action, long target) {
        HashSet<MissionTrigger> trigs = new HashSet<MissionTrigger>();
        for (MissionTrigger m : triggers.values()) {
            if (m.getOnActionPerformed() > 0 && m.getOnActionPerformed() != action || m.getItemUsedId() > 0 && m.getItemUsedId() != itemUsed || m.getItemUsedId() <= 0 && action == 142 && m.getItemUsedId() != itemUsed) continue;
            if (WurmId.getType(target) == 3 && WurmId.getType(m.getTarget()) == 3 || WurmId.getType(target) == 17 && WurmId.getType(m.getTarget()) == 17) {
                short x = Tiles.decodeTileX(target);
                int y = Tiles.decodeTileY(target);
                short x2 = Tiles.decodeTileX(m.getTarget());
                int y2 = Tiles.decodeTileY(m.getTarget());
                if (x != x2 || y != y2) continue;
                trigs.add(m);
                continue;
            }
            if (m.getTarget() > 0L && m.getTarget() != target) continue;
            EpicMission mis = EpicServerStatus.getEpicMissionForMission(m.getMissionRequired());
            if (mis != null && mis.getMissionType() == 116 && !Servers.localServer.PVPSERVER) {
                try {
                    Creature killed = Creatures.getInstance().getCreature(target);
                    if (killed.getStatus().getModType() != 99) {
                        continue;
                    }
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                    // empty catch block
                }
            }
            trigs.add(m);
        }
        return trigs.toArray(new MissionTrigger[trigs.size()]);
    }

    public static MissionTrigger[] getMissionTriggerPlate(int tilex, int tiley, int layer) {
        HashSet<MissionTrigger> trigs = new HashSet<MissionTrigger>();
        for (MissionTrigger m : triggers.values()) {
            int y2;
            short x2;
            if (m.getOnActionPerformed() != 475) continue;
            if (layer == 0 && WurmId.getType(m.getTarget()) == 3) {
                x2 = Tiles.decodeTileX(m.getTarget());
                y2 = Tiles.decodeTileY(m.getTarget());
                if (tilex != x2 || tiley != y2) continue;
                trigs.add(m);
                continue;
            }
            if (layer >= 0 || WurmId.getType(m.getTarget()) != 17) continue;
            x2 = Tiles.decodeTileX(m.getTarget());
            y2 = Tiles.decodeTileY(m.getTarget());
            if (tilex != x2 || tiley != y2) continue;
            trigs.add(m);
        }
        return trigs.toArray(new MissionTrigger[trigs.size()]);
    }

    public static boolean activateTriggerPlate(Creature performer, int tilex, int tiley, int layer) {
        MissionTrigger[] trigs;
        if (performer.isPlayer() && (trigs = MissionTriggers.getMissionTriggerPlate(tilex, tiley, layer)).length > 0) {
            TriggerRun tr = new TriggerRun();
            tr.run(performer, trigs, 1);
            return tr.isTriggered();
        }
        return false;
    }

    public static boolean activateTriggers(Creature performer, int creatureTemplateId, int actionPerformed, long targetId, int counter) {
        boolean done = true;
        MissionTrigger[] trigs = MissionTriggers.getMissionTriggersWith(creatureTemplateId, actionPerformed, targetId);
        if (trigs.length > 0) {
            TriggerRun tr = new TriggerRun();
            tr.run(performer, trigs, counter);
            done = tr.isDone();
        }
        return done;
    }

    public static boolean activateTriggers(Creature performer, Item item, int actionPerformed, long targetId, int counter) {
        boolean done = true;
        if (performer.isPlayer()) {
            MissionTrigger[] trigs = MissionTriggers.getMissionTriggersWith(item != null ? item.getTemplateId() : 0, actionPerformed, targetId);
            performer.sendToLoggers("Found " + trigs.length + " triggers.", (byte)2);
            if (trigs.length > 0) {
                TriggerRun tr = new TriggerRun();
                tr.run(performer, trigs, counter);
                done = tr.isDone();
                if (tr.isTriggered()) {
                    if (actionPerformed == 492) {
                        EffectFactory.getInstance().deleteEffByOwner(targetId);
                    }
                    if (actionPerformed == 47 && item != null) {
                        if (tr.getLastTrigger() != null && tr.getLastTrigger().getCreatorType() == 3) {
                            if (WurmId.getType(targetId) == 1 || WurmId.getType(targetId) == 0) {
                                item.putInVoid();
                                try {
                                    Creature targetC = Server.getInstance().getCreature(targetId);
                                    targetC.getInventory().insertItem(item);
                                }
                                catch (NoSuchCreatureException nsc) {
                                    Items.destroyItem(item.getWurmId());
                                }
                                catch (NoSuchPlayerException nsp) {
                                    Items.destroyItem(item.getWurmId());
                                }
                            }
                        } else {
                            Items.destroyItem(item.getWurmId());
                        }
                    }
                }
            }
        }
        return done;
    }

    public static boolean isDoorOpen(Creature performer, long doorid, int counter) {
        MissionTarget targ = MissionTargets.getMissionTargetFor(doorid);
        if (targ != null) {
            MissionTrigger[] trigs = targ.getMissionTriggers();
            TriggerRun tr = new TriggerRun();
            tr.run(performer, trigs, counter);
            return tr.isOpenedDoor();
        }
        return false;
    }

    public static MissionTrigger getTriggerWithId(int id) {
        return triggers.get(id);
    }

    public static String getTargetAsString(Creature creature, long target) {
        StringBuilder buf;
        block49: {
            buf = new StringBuilder();
            if (target <= 0L) {
                buf.append("None");
            } else if (WurmId.getType(target) == 1) {
                try {
                    Creature c = Creatures.getInstance().getCreature(target);
                    if (creature.getPower() > 0) {
                        buf.append(c.getName() + " at " + c.getTileX() + "," + c.getTileY());
                        break block49;
                    }
                    buf.append(c.getName());
                }
                catch (NoSuchCreatureException nsc) {
                    buf.append("Nonexistant creature.");
                }
            } else if (WurmId.getType(target) == 0) {
                try {
                    Player p = Players.getInstance().getPlayer(target);
                    if (creature.getPower() > 0) {
                        buf.append(p.getName() + " at " + p.getTileX() + "," + p.getTileY());
                        break block49;
                    }
                    buf.append(p.getName());
                }
                catch (NoSuchPlayerException nsc) {
                    buf.append("Nonexistant creature.");
                }
            } else if (WurmId.getType(target) == 5) {
                int x = (int)(target >> 32) & 0xFFFF;
                int y = (int)(target >> 16) & 0xFFFF;
                boolean onSurface = Tiles.decodeLayer(target) == 0;
                Wall wall = Wall.getWall(target);
                String loc = "";
                if (creature.getPower() > 0) {
                    loc = " at " + x + "," + y + ", " + onSurface;
                }
                if (wall == null) {
                    buf.append("Unknown wall" + loc);
                } else {
                    buf.append(wall.getName());
                    buf.append(" (level:" + wall.getFloorLevel() + ")");
                    buf.append(loc);
                }
            } else if (WurmId.getType(target) == 2 || WurmId.getType(target) == 6 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20) {
                try {
                    Item targetItem = Items.getItem(target);
                    String tgtName = targetItem.getName().replace('\"', '\'');
                    if (creature.getPower() > 0) {
                        buf.append(tgtName + " at " + targetItem.getTileX() + "," + targetItem.getTileY());
                    } else {
                        buf.append(tgtName);
                    }
                    if (targetItem.getOwnerId() != -10L && creature.getPower() > 0) {
                        buf.append(" owned by " + targetItem.getOwnerId());
                    }
                }
                catch (NoSuchItemException nsi) {
                    buf.append("Unknown item");
                }
            } else if (WurmId.getType(target) == 7) {
                int x = (int)(target >> 32) & 0xFFFF;
                int y = (int)(target >> 16) & 0xFFFF;
                boolean onSurface = Tiles.decodeLayer(target) == 0;
                Fence fence = Fence.getFence(target);
                String loc = "";
                if (creature.getPower() > 0) {
                    loc = " at " + x + ", " + y + ", " + onSurface;
                }
                if (fence == null) {
                    buf.append("Unknown fence" + loc);
                } else {
                    buf.append(fence.getName());
                    if (fence.getFloorLevel() > 0) {
                        buf.append(" (level:" + fence.getFloorLevel() + ")");
                    }
                    buf.append(loc);
                }
            } else if (WurmId.getType(target) == 28) {
                BridgePart[] bridgeParts;
                short x = Tiles.decodeTileX(target);
                int y = Tiles.decodeTileY(target);
                byte layer = Tiles.decodeLayer(target);
                String loc = "";
                if (creature.getPower() > 0) {
                    loc = " at " + x + ", " + y + ", " + (layer == 0);
                }
                if ((bridgeParts = Zones.getBridgePartsAtTile(x, y, layer == 0)).length == 0) {
                    buf.append("Unknown bridge part" + loc);
                } else if (bridgeParts.length > 1) {
                    buf.append("Too many bridge parts found" + loc);
                } else {
                    buf.append(bridgeParts[0].getName() + loc);
                }
            } else if (WurmId.getType(target) == 23) {
                Floor[] floors;
                short x = Tiles.decodeTileX(target);
                int y = Tiles.decodeTileY(target);
                byte layer = Tiles.decodeLayer(target);
                int htOffset = Floor.getHeightOffsetFromWurmId(target);
                String loc = "";
                if (creature.getPower() > 0) {
                    loc = " at " + x + ", " + y + ", " + (layer == 0);
                }
                if ((floors = Zones.getFloorsAtTile((int)x, y, htOffset, htOffset, layer)) == null || floors.length == 0) {
                    buf.append("Unknown floor" + loc);
                } else {
                    buf.append(floors[0].getName());
                    buf.append(" (level:" + floors[0].getFloorLevel() + ")");
                    buf.append(loc);
                }
            } else if (WurmId.getType(target) == 3) {
                boolean broken = false;
                int x = (int)(target >> 32) & 0xFFFF;
                int y = (int)(target >> 16) & 0xFFFF;
                if (x > Zones.worldTileSizeX) {
                    x = (int)(target >> 40) & 0xFFFFFF;
                    broken = true;
                }
                int heightOffset = (int)(target >> 48) & 0xFFFF;
                int tile = Server.surfaceMesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                Tiles.Tile t = Tiles.getTile(type);
                buf.append(t.tiledesc);
                if (creature.getPower() > 0) {
                    if (broken) {
                        buf.append(" * ");
                    }
                    buf.append(" at ");
                    buf.append(x);
                    buf.append(", ");
                    buf.append(y);
                    buf.append(", true");
                }
            } else if (WurmId.getType(target) == 17) {
                int x = (int)(target >> 32) & 0xFFFF;
                int y = (int)(target >> 16) & 0xFFFF;
                int tile = Server.caveMesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                Tiles.Tile t = Tiles.getTile(type);
                buf.append(t.tiledesc);
                if (creature.getPower() > 0) {
                    buf.append(" at ");
                    buf.append(x);
                    buf.append(", ");
                    buf.append(y);
                    buf.append(", false");
                }
            }
        }
        return buf.toString();
    }

    static boolean removeTrigger(int id) {
        boolean existed;
        MissionTrigger trigger = triggers.get(id);
        boolean bl = existed = triggers.remove(id) != null;
        if (trigger != null) {
            MissionTargets.removeMissionTrigger(trigger, true);
        }
        return existed;
    }

    static void destroyTriggersForTarget(long target) {
        MissionTrigger[] mtarr = MissionTriggers.getAllTriggers();
        for (int t = 0; t < mtarr.length; ++t) {
            if (mtarr[t] == null || mtarr[t].getTarget() != target) continue;
            TriggerEffects.destroyEffectsForTrigger(mtarr[t].getId());
            MissionTriggers.removeTrigger(mtarr[t].getId());
            mtarr[t].destroy();
        }
        MissionTargets.destroyMissionTarget(target, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadAllTriggers() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOADALLTRIGGERS);
            rs = ps.executeQuery();
            int mid = -10;
            while (rs.next()) {
                mid = rs.getInt("ID");
                MissionTrigger m = new MissionTrigger();
                m.setId(mid);
                m.setName(rs.getString("NAME"));
                m.setDescription(rs.getString("DESCRIPTION"));
                m.setOnItemUsedId(rs.getInt("ONITEMCREATED"));
                m.setOnActionPerformed(rs.getInt("ONACTIONPERFORMED"));
                m.setOnTargetId(rs.getLong("ONTARGET"));
                m.setMissionRequirement(rs.getInt("MISSION_REQ"));
                m.setStateRequirement(rs.getFloat("MISSION_STATE_REQ"));
                m.setStateEnd(rs.getFloat("MISSION_STATE_END"));
                m.setInactive(rs.getBoolean("INACTIVE"));
                m.setLastModifierName(rs.getString("LASTMODIFIER"));
                m.setCreatorName(rs.getString("CREATOR"));
                m.setCreatedDate(rs.getString("CREATEDDATE"));
                m.setLastModifierName(rs.getString("LASTMODIFIER"));
                Timestamp st = new Timestamp(System.currentTimeMillis());
                try {
                    String lastModified = rs.getString("LASTMODIFIEDDATE");
                    if (lastModified != null) {
                        st = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastModified).getTime());
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
                m.setLastModifiedDate(st);
                m.setSeconds(rs.getInt("SECONDS"));
                m.setCreatorType(rs.getByte("CREATORTYPE"));
                m.setOwnerId(rs.getLong("CREATORID"));
                m.setIsSpawnpoint(rs.getBoolean("SPAWNPOINT"));
                MissionTriggers.addMissionTrigger(m);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage());
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    static {
        try {
            MissionTriggers.loadAllTriggers();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problems loading all Mission Triggers", ex);
        }
    }
}


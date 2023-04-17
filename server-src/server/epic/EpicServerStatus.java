/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicMissionEnum;
import com.wurmonline.server.epic.EpicScenario;
import com.wurmonline.server.epic.EpicTargetItems;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.Valrei;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.tutorial.Triggers2Effects;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcEpicStatusReport;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;
import com.wurmonline.shared.util.TerrainUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class EpicServerStatus
implements MiscConstants,
TimeConstants {
    private static final Logger logger = Logger.getLogger(EpicServerStatus.class.getName());
    private static final Set<EpicMission> epicMissions = new HashSet<EpicMission>();
    private static final EpicMission[] emptyEpicMArr = new EpicMission[0];
    private static HashMap<Integer, Integer> backupDifficultyMap;
    private final int serverId;
    private final PathTile notFoundTile = new PathTile(-1, -1, -1, true, 0);
    static final String LOAD_LOCAL_EPIC_ENTITY_MISSIONS = "SELECT * FROM EPICMISSIONS";
    private static final int BUILD_TARGET = 0;
    private static final int USE_TARGET = 1;
    private static final int USE_TILE = 2;
    private static final int USE_MANY_TILES = 3;
    private static final int USE_GUARDTOWER = 4;
    private static final int DRAIN_SETTLEMENT = 5;
    private static final int KILL_CREATURES = 6;
    private static final int SACRIFICE_ITEMS = 7;
    private static final int BUILD_COMPLEX_ITEM = 8;
    private static final int BRING_ITEM_TO_CREATURE = 9;
    private static final int SACRIFICE_CREATURES = 10;
    public static final byte TYPE_BUILDSTRUCTURE_SP = 101;
    public static final byte TYPE_BUILDSTRUCTURE_TO = 102;
    public static final byte TYPE_BUILDSTRUCTURE_SG = 103;
    public static final byte TYPE_RITUALMS_F = 104;
    public static final byte TYPE_RITUALMS_E = 105;
    public static final byte TYPE_CUTTREE_F = 106;
    public static final byte TYPE_CUTTREE_E = 107;
    public static final byte TYPE_RITUALGT = 108;
    public static final byte TYPE_SACMISSION = 109;
    public static final byte TYPE_SACITEM = 110;
    public static final byte TYPE_CREATEITEM = 111;
    public static final byte TYPE_GIVEITEM_F = 112;
    public static final byte TYPE_GIVEITEM_E = 113;
    public static final byte TYPE_SLAYCREATURE_P = 114;
    public static final byte TYPE_SLAYCREATURE_L = 115;
    public static final byte TYPE_SLAYCREATURE_H = 116;
    public static final byte TYPE_SLAYTRAITOR_P = 117;
    public static final byte TYPE_SLAYTRAITOR_L = 118;
    public static final byte TYPE_SLAYTRAITOR_H = 119;
    public static final byte TYPE_DESTROYGT = 120;
    public static final byte TYPE_SACCREATURE_P = 121;
    public static final byte TYPE_SACCREATURE_L = 122;
    public static final byte TYPE_SACCREATURE_H = 123;
    public static final byte TYPE_SLAYTOWERGUARD = 124;
    private int maxTimeSecs = 1000;
    private static EpicScenario currentScenario;
    private static final List<ItemTemplate> itemplates;
    private static HexMap valrei;
    private static final String[] missionAdjectives;
    private static final String[] missionNames;
    private static final String[] missionFor;
    private static ArrayList<EpicMission> matchingMissions;

    public EpicServerStatus() {
        this.serverId = Servers.localServer.id;
    }

    EpicServerStatus(int server) {
        this.serverId = server;
    }

    public static final HexMap getValrei() {
        if (valrei == null) {
            valrei = new Valrei();
        }
        return valrei;
    }

    static void setupMissionItemTemplates() {
        ItemTemplate[] templates;
        for (ItemTemplate lTemplate : templates = ItemTemplateFactory.getInstance().getTemplates()) {
            if (!lTemplate.isMissionItem() || lTemplate.isUseOnGroundOnly() || lTemplate.isNoTake() || lTemplate.unique || lTemplate.artifact || lTemplate.isRiftLoot() || lTemplate.getTemplateId() == 737 || lTemplate.getTemplateId() == 683 || lTemplate.getTemplateId() == 1414) continue;
            itemplates.add(lTemplate);
        }
    }

    public static final void addMission(EpicMission mission) {
        epicMissions.add(mission);
    }

    public static final EpicMission getEpicMissionForMission(int missionId) {
        for (EpicMission em : epicMissions) {
            if (em.getMissionId() != missionId) continue;
            return em;
        }
        return null;
    }

    public static final EpicMission getEpicMissionForEntity(int entityId) {
        for (EpicMission em : epicMissions) {
            if (em.getEpicEntityId() != entityId || !em.isCurrent()) continue;
            return em;
        }
        return null;
    }

    public static final EpicMission[] getEpicMissionsForKingdomTemplate(byte kingdomTemplateId) {
        ArrayList<EpicMission> toRet = new ArrayList<EpicMission>();
        for (EpicMission em : epicMissions) {
            if (!em.isCurrent()) continue;
            Deity d = Deities.translateDeityForEntity(em.getEpicEntityId());
            int deityNum = -1;
            if (d != null) {
                deityNum = d.getNumber();
            }
            if (Deities.getFavoredKingdom(deityNum) != kingdomTemplateId) continue;
            toRet.add(em);
        }
        if (toRet.size() > 0) {
            return toRet.toArray(new EpicMission[toRet.size()]);
        }
        return emptyEpicMArr;
    }

    public static final EpicMission[] getCurrentEpicMissions() {
        ArrayList<EpicMission> toRet = new ArrayList<EpicMission>();
        for (EpicMission em : epicMissions) {
            if (!em.isCurrent()) continue;
            toRet.add(em);
        }
        if (toRet.size() > 0) {
            return toRet.toArray(new EpicMission[toRet.size()]);
        }
        return emptyEpicMArr;
    }

    public static final EpicMission[] getEpicMissionsForDeity(int deityNum) {
        ArrayList<EpicMission> toRet = new ArrayList<EpicMission>();
        for (EpicMission em : epicMissions) {
            if (!em.isCurrent() || em.getEpicEntityId() != deityNum) continue;
            toRet.add(em);
        }
        if (toRet.size() > 0) {
            return toRet.toArray(new EpicMission[toRet.size()]);
        }
        return emptyEpicMArr;
    }

    public int getServerId() {
        return this.serverId;
    }

    public static final EpicScenario getCurrentScenario() {
        return currentScenario;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadLocalEntries() {
        logger.log(Level.INFO, "LOADING LOCAL EPIC MISSIONS");
        currentScenario = new EpicScenario();
        currentScenario.loadCurrentScenario();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(LOAD_LOCAL_EPIC_ENTITY_MISSIONS);
            rs = ps.executeQuery();
            while (rs.next()) {
                EpicMission mp = new EpicMission(rs.getInt("ENTITY"), rs.getInt("SCENARIO"), rs.getString("NAME"), rs.getString("SCENARIONAME"), rs.getInt("MISSION"), rs.getByte("MISSIONTYPE"), rs.getInt("DIFFICULTY"), rs.getFloat("PROGRESS"), rs.getInt("SERVERID"), rs.getLong("TSTAMP"), true, rs.getBoolean("CURRENT"));
                EpicServerStatus.addMission(mp);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load epic mission.", sqx);
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

    private static final int getRitualAction(byte targetKingdom) {
        if (targetKingdom != 3) {
            return 496 + Server.rand.nextInt(5);
        }
        return 496 + Server.rand.nextInt(7);
    }

    public PathTile getTargetTileInProximityTo(PathTile p, int direction, int sizeX, int sizeY, boolean isTree) {
        Tiles.Tile theTile;
        byte data;
        byte type;
        int tileToCheck;
        int y;
        int x;
        if (direction == 0) {
            for (x = p.getTileX(); x < p.getTileX() + sizeX; ++x) {
                for (y = p.getTileY() - sizeY; y < p.getTileY(); ++y) {
                    if (Server.rand.nextInt(Math.max(1, sizeX / 2)) != 0) continue;
                    tileToCheck = Zones.getTileIntForTile(x, y, 0);
                    type = Tiles.decodeType(tileToCheck);
                    data = Tiles.decodeData(tileToCheck);
                    theTile = Tiles.getTile(type);
                    if (isTree) {
                        if (!theTile.isMyceliumTree() && !theTile.isNormalTree() || FoliageAge.getAgeAsByte(data) <= FoliageAge.MATURE_ONE.getAgeId()) continue;
                        return new PathTile(x, y, tileToCheck, true, 0);
                    }
                    return new PathTile(x, y, tileToCheck, true, 0);
                }
            }
        }
        if (direction == 6) {
            for (x = p.getTileX() - sizeX; x < p.getTileX(); ++x) {
                for (y = p.getTileY(); y < p.getTileY() + sizeY; ++y) {
                    if (Server.rand.nextInt(Math.max(1, sizeX / 2)) != 0) continue;
                    tileToCheck = Zones.getTileIntForTile(x, y, 0);
                    type = Tiles.decodeType(tileToCheck);
                    data = Tiles.decodeData(tileToCheck);
                    theTile = Tiles.getTile(type);
                    if (isTree) {
                        if (!theTile.isMyceliumTree() && !theTile.isNormalTree() || FoliageAge.getAgeAsByte(data) <= FoliageAge.MATURE_ONE.getAgeId()) continue;
                        return new PathTile(x, y, tileToCheck, true, 0);
                    }
                    return new PathTile(x, y, tileToCheck, true, 0);
                }
            }
        }
        if (direction == 2) {
            for (x = p.getTileX(); x < p.getTileX() + sizeX; ++x) {
                for (y = p.getTileY(); y < p.getTileY() + sizeY; ++y) {
                    if (Server.rand.nextInt(Math.max(1, sizeX / 2)) != 0) continue;
                    tileToCheck = Zones.getTileIntForTile(x, y, 0);
                    type = Tiles.decodeType(tileToCheck);
                    data = Tiles.decodeData(tileToCheck);
                    theTile = Tiles.getTile(type);
                    if (isTree) {
                        if (!theTile.isMyceliumTree() && !theTile.isNormalTree() || FoliageAge.getAgeAsByte(data) <= FoliageAge.MATURE_ONE.getAgeId()) continue;
                        return new PathTile(x, y, tileToCheck, true, 0);
                    }
                    return new PathTile(x, y, tileToCheck, true, 0);
                }
            }
        }
        if (direction == 4) {
            for (x = p.getTileX(); x < p.getTileX() + sizeX; ++x) {
                for (y = p.getTileY(); y < p.getTileY() + sizeY; ++y) {
                    if (Server.rand.nextInt(Math.max(1, sizeX / 2)) != 0) continue;
                    tileToCheck = Zones.getTileIntForTile(x, y, 0);
                    type = Tiles.decodeType(tileToCheck);
                    data = Tiles.decodeData(tileToCheck);
                    theTile = Tiles.getTile(type);
                    if (isTree) {
                        if (!theTile.isMyceliumTree() && !theTile.isNormalTree() || FoliageAge.getAgeAsByte(data) <= FoliageAge.MATURE_ONE.getAgeId()) continue;
                        return new PathTile(x, y, tileToCheck, true, 0);
                    }
                    return new PathTile(x, y, tileToCheck, true, 0);
                }
            }
        }
        return this.notFoundTile;
    }

    public static long getTileId(int x, int y) {
        return Tiles.getTileId(x, y, 0);
    }

    private final String generateNeedString(int difficulty) {
        String needString;
        switch (difficulty) {
            case 1: {
                needString = " asks ";
                break;
            }
            case 2: {
                needString = " wants ";
                break;
            }
            case 3: {
                needString = " requires ";
                break;
            }
            case 4: {
                needString = " needs ";
                break;
            }
            case 5: {
                needString = " urges ";
                break;
            }
            case 6: {
                needString = " demands ";
                break;
            }
            case 7: {
                needString = " commands ";
                break;
            }
            default: {
                needString = " needs ";
            }
        }
        return needString;
    }

    private final void linkMission(Mission m, MissionTrigger trig, TriggerEffect effect, byte missionType, int difficulty) {
        m.update();
        Missions.addMission(m);
        trig.setMissionRequirement(m.getId());
        trig.create();
        MissionTriggers.addMissionTrigger(trig);
        effect.setTrigger(trig.getId());
        effect.setMission(m.getId());
        effect.setStopSkillgain(false);
        effect.setStartSkillgain(false);
        effect.create();
        Triggers2Effects.addLink(trig.getId(), effect.getId(), false);
        TriggerEffects.addTriggerEffect(effect);
        for (EpicMission em : epicMissions) {
            if (!em.isCurrent() || em.getEpicEntityId() != (int)m.getOwnerId()) continue;
            em.setCurrent(false);
            em.update();
        }
        EpicMission mp = new EpicMission((int)m.getOwnerId(), currentScenario.getScenarioNumber(), m.getName(), currentScenario.getScenarioName(), m.getId(), missionType, difficulty, 1.0f, Servers.localServer.id, System.currentTimeMillis() + (long)this.maxTimeSecs * 1000L, false, true);
        EpicServerStatus.addMission(mp);
        Players.getInstance().sendUpdateEpicMission(mp);
    }

    public static final String getAreaString(int tilex, int tiley) {
        StringBuilder sbuild = new StringBuilder();
        if (tiley < Zones.worldTileSizeY / 3) {
            sbuild.append("north");
        } else if (tiley > Zones.worldTileSizeY - Zones.worldTileSizeY / 3) {
            sbuild.append("south");
        } else {
            sbuild.append("center");
        }
        if (tilex < Zones.worldTileSizeX / 3) {
            sbuild.append("west");
        } else if (tilex > Zones.worldTileSizeX - Zones.worldTileSizeX / 3) {
            sbuild.append("east");
        }
        sbuild.append(" regions");
        return sbuild.toString();
    }

    private final ItemTemplate getRandomItemTemplateUsed() {
        return itemplates.get(Server.rand.nextInt(itemplates.size()));
    }

    private final MissionTrigger initializeMissionTrigger(int epicEntityId, String epicEntityName) {
        MissionTrigger trig = new MissionTrigger();
        trig.setStateRequirement(0.0f);
        trig.setName(epicEntityName + "Auto" + Server.rand.nextInt());
        trig.setCreatorType((byte)2);
        trig.setCreatorName("System");
        trig.setLastModifierName(epicEntityName);
        trig.setOwnerId(epicEntityId);
        return trig;
    }

    private final TriggerEffect initializeTriggerEffect(int epicEntityId, String epicEntityName) {
        TriggerEffect effect = new TriggerEffect();
        effect.setName(epicEntityName + "Auto" + Server.rand.nextInt());
        effect.setCreatorType((byte)2);
        effect.setCreatorName("System");
        effect.setLastModifierName(epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased. You do your part well.");
        effect.setSoundName("sound.music.song.spawn1");
        effect.setOwnerId(epicEntityId);
        return effect;
    }

    public static final void deleteMission(EpicMission mission) {
        epicMissions.remove(mission);
        mission.delete();
    }

    private static final void destroyLastMissionForEntity(int epicEntityId) {
        for (EpicMission em : epicMissions) {
            if (!em.isCurrent() || em.getEpicEntityId() != epicEntityId) continue;
            EpicServerStatus.destroySpecificMission(em);
        }
    }

    /*
     * WARNING - void declaration
     */
    private static final void destroySpecificMission(EpicMission em) {
        boolean failed = false;
        if (!em.isCompleted()) {
            failed = true;
            em.updateProgress(-1.0f);
        }
        em.setCurrent(false);
        em.update();
        if (Servers.localServer.EPIC) {
            Players.getInstance().sendUpdateEpicMission(em);
        }
        Mission[] missions = Missions.getAllMissions();
        MissionTrigger[] triggers = MissionTriggers.getAllTriggers();
        TriggerEffect[] effects = TriggerEffects.getAllEffects();
        for (Mission m : missions) {
            if (m.getCreatorType() != 2 || m.getOwnerId() != (long)em.getEpicEntityId() || m.isInactive()) continue;
            if (failed) {
                void var12_14;
                MissionPerformer[] allperfs;
                MissionPerformer[] missionPerformerArray = allperfs = MissionPerformed.getAllPerformers();
                int n = missionPerformerArray.length;
                boolean i = false;
                while (var12_14 < n) {
                    MissionPerformer mp = missionPerformerArray[var12_14];
                    MissionPerformed thisMission = mp.getMission(m.getId());
                    if (thisMission != null) {
                        thisMission.setState(-1.0f, mp.getWurmId());
                    }
                    ++var12_14;
                }
            }
            m.setInactive(true);
            for (MissionTrigger missionTrigger : triggers) {
                if (missionTrigger.getMissionRequired() != m.getId()) continue;
                TriggerEffects.destroyEffectsForTrigger(missionTrigger.getId());
                missionTrigger.destroy();
            }
            for (Comparable<MissionTrigger> comparable : effects) {
                if (((TriggerEffect)comparable).getMissionId() != m.getId()) continue;
                ((TriggerEffect)comparable).destroy();
            }
        }
    }

    public static final void pollExpiredMissions() {
        HashSet<Integer> deities = new HashSet<Integer>();
        for (EpicMission m : EpicServerStatus.getCurrentEpicMissions()) {
            EpicMissionEnum missionEnum;
            Mission mis = Missions.getMissionWithId(m.getMissionId());
            if (m.isCurrent()) {
                deities.add(m.getEpicEntityId());
            }
            if (m.isCurrent() && System.currentTimeMillis() > m.getExpireTime()) {
                int dietyNum = m.getEpicEntityId();
                EpicServerStatus.destroyLastMissionForEntity(dietyNum);
                WcEpicStatusReport wce = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, dietyNum, m.getMissionType(), m.getDifficulty());
                wce.sendToLoginServer();
                EpicServerStatus.storeLastMissionForEntity(m.getEpicEntityId(), m);
                if (Servers.localServer.EPIC || dietyNum <= 0 || dietyNum > 4) continue;
                EpicServerStatus es = new EpicServerStatus();
                if (EpicServerStatus.getCurrentScenario() == null) {
                    EpicServerStatus.loadLocalEntries();
                }
                if (EpicServerStatus.getCurrentScenario() == null) continue;
                es.generateNewMissionForEpicEntity(dietyNum, Deities.getDeityName(dietyNum), Math.max(1, m.getDifficulty() - 2), 604800, EpicServerStatus.getCurrentScenario().getScenarioName(), EpicServerStatus.getCurrentScenario().getScenarioNumber(), EpicServerStatus.getCurrentScenario().getScenarioQuest(), true);
                continue;
            }
            if (mis == null || Servers.localServer.EPIC || !m.isCurrent() || System.currentTimeMillis() <= mis.getLastModifiedAsLong() + 302400000L || !(m.getMissionProgress() < 33.0f) || !(missionEnum = EpicMissionEnum.getMissionForType(m.getMissionType())).isKarmaMultProgress() && !EpicMissionEnum.isRitualMission(missionEnum) || m.getExpireTime() <= System.currentTimeMillis() + 43200000L) continue;
            m.setExpireTime(System.currentTimeMillis() + 43200000L);
        }
        Map<Integer, String> entityMap = Deities.getEntities();
        for (Map.Entry<Integer, String> entry : entityMap.entrySet()) {
            EpicMission m;
            boolean found = false;
            for (Integer i : deities) {
                if (entry.getKey().intValue() != i.intValue()) continue;
                found = true;
            }
            if (found || (m = EpicServerStatus.getEpicMissionForEntity(entry.getKey())) == null) continue;
            EpicServerStatus.destroyLastMissionForEntity(entry.getKey());
            WcEpicStatusReport wce = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, entry.getKey(), m.getMissionType(), m.getDifficulty());
            wce.sendToLoginServer();
            EpicServerStatus.storeLastMissionForEntity(m.getEpicEntityId(), m);
        }
    }

    public void generateNewMissionForEpicEntity(int epicEntityId, String epicEntityName, int baseDifficulty, int maxTimeSeconds, String scenarioNameId, int scenarioIdentity, String questReasonString, boolean destroyPreviousMission) {
        int attempt = 0;
        boolean error = true;
        String creationResponse = "";
        while (error && attempt++ < 10) {
            if (baseDifficulty == -1) {
                EpicMission em = EpicServerStatus.getEpicMissionForEntity(epicEntityId);
                baseDifficulty = em != null ? em.getDifficulty() : 1;
            } else if (baseDifficulty == -2 || baseDifficulty == -3) {
                EpicMission[] e = EpicServerStatus.getValrei().getEntity(epicEntityId);
                if (e != null && (long)e.getLatestMissionDifficulty() != -10L) {
                    if (baseDifficulty == -2) {
                        baseDifficulty = e.getLatestMissionDifficulty();
                        if (Server.rand.nextInt(Math.max(1, baseDifficulty)) == 0) {
                            ++baseDifficulty;
                        }
                    } else {
                        baseDifficulty = e.getLatestMissionDifficulty() - 2;
                    }
                } else if (backupDifficultyMap == null || !backupDifficultyMap.containsKey(epicEntityId)) {
                    baseDifficulty = 1;
                    logger.log(Level.WARNING, "Error getting proper difficulty of new mission for " + epicEntityName + " sent from login server. Empty backup map.");
                } else {
                    if (baseDifficulty == -2) {
                        baseDifficulty = backupDifficultyMap.get(epicEntityId);
                        if (Server.rand.nextInt(Math.max(1, baseDifficulty)) == 0) {
                            ++baseDifficulty;
                        }
                    } else {
                        baseDifficulty = backupDifficultyMap.get(epicEntityId) - 1;
                    }
                    logger.log(Level.WARNING, "Error getting proper difficulty of new mission for " + epicEntityName + " sent from login server. Used backup value.");
                }
            }
            baseDifficulty = Math.max(1, Math.min(7, baseDifficulty));
            if (!destroyPreviousMission) {
                for (EpicMission m : EpicServerStatus.getCurrentEpicMissions()) {
                    if (m.getEpicEntityId() != epicEntityId || System.currentTimeMillis() >= m.getExpireTime() || !(m.getMissionProgress() < 100.0f)) continue;
                    return;
                }
            }
            EpicServerStatus.destroyLastMissionForEntity(epicEntityId);
            Deity d = Deities.translateDeityForEntity(epicEntityId);
            int deityNumber = d == null ? -1 : d.getNumber();
            byte favoredKingdomId = Deities.getFavoredKingdom(deityNumber);
            boolean battlegroundServer = Servers.localServer.PVPSERVER && !Servers.localServer.HOMESERVER;
            boolean enemyHomeServer = Servers.localServer.EPIC && Servers.localServer.HOMESERVER && Servers.localServer.KINGDOM != favoredKingdomId;
            boolean friendlyHomeServer = Servers.localServer.HOMESERVER && !enemyHomeServer;
            EpicMissionEnum newMission = EpicMissionEnum.getRandomMission(baseDifficulty, battlegroundServer, friendlyHomeServer, enemyHomeServer);
            if (newMission == null) {
                return;
            }
            if (!(newMission.getMissionType() != 121 && newMission.getMissionType() != 122 && newMission.getMissionType() != 123 || deityNumber != 4 || Servers.localServer.EPIC || Servers.localServer.isChaosServer())) {
                newMission = null;
            }
            if (newMission == null) {
                for (int tries = 0; newMission == null && tries < 30; ++tries) {
                    newMission = EpicMissionEnum.getRandomMission(baseDifficulty, battlegroundServer, friendlyHomeServer, enemyHomeServer);
                    if (newMission.getMissionType() != 121 && newMission.getMissionType() != 122 && newMission.getMissionType() != 123 || deityNumber != 4 || Servers.localServer.EPIC || Servers.localServer.isChaosServer()) continue;
                    newMission = null;
                }
                if (newMission == null) {
                    logger.warning("Failed to create new mission for " + epicEntityName + " (difficulty=" + baseDifficulty + ")");
                    return;
                }
            }
            byte targetKingdom = favoredKingdomId;
            if (friendlyHomeServer || enemyHomeServer) {
                targetKingdom = Servers.localServer.KINGDOM;
            }
            if (friendlyHomeServer) {
                favoredKingdomId = Servers.localServer.KINGDOM;
            }
            if (battlegroundServer && newMission.isEnemyTerritory()) {
                byte[] enemyKingdoms = EpicServerStatus.getEnemyKingdoms(epicEntityId, favoredKingdomId);
                if (enemyKingdoms.length <= 0) continue;
                targetKingdom = enemyKingdoms[Server.rand.nextInt(enemyKingdoms.length)];
            }
            Mission m = new Mission("System", epicEntityName);
            m.setCreatorType((byte)2);
            m.setMaxTimeSeconds(maxTimeSeconds);
            m.setOwnerId(epicEntityId);
            m.setLastModifierName(epicEntityName);
            this.maxTimeSecs = maxTimeSeconds;
            switch (newMission.getMissionType()) {
                case 101: 
                case 102: 
                case 103: {
                    creationResponse = this.createBuildStructureMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 104: 
                case 105: {
                    creationResponse = this.createMSRitualMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 106: 
                case 107: {
                    creationResponse = this.createCutTreeMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 108: {
                    creationResponse = this.createGTRitualMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 109: {
                    creationResponse = this.createMISacrificeMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 110: {
                    creationResponse = this.createGenericSacrificeMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 111: {
                    creationResponse = this.createCreateItemMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 112: 
                case 113: {
                    creationResponse = this.createGiveItemMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 114: 
                case 115: 
                case 116: {
                    creationResponse = this.createSlayCreatureMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 117: 
                case 118: 
                case 119: {
                    creationResponse = this.createSlayTraitorMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 120: {
                    creationResponse = this.createDestroyGTMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 121: 
                case 122: 
                case 123: {
                    creationResponse = this.createSacrificeCreatureMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                    break;
                }
                case 124: {
                    creationResponse = this.createSlayTowerGuardsMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, targetKingdom);
                }
            }
            error = creationResponse.contains("Error") || creationResponse.contains("error");
        }
        logger.log(error ? Level.WARNING : Level.INFO, creationResponse);
    }

    private String createBuildStructureMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        int targetItem = 712;
        switch (newMission.getMissionType()) {
            case 101: {
                if (!Server.rand.nextBoolean()) break;
                targetItem = 717;
                break;
            }
            case 102: {
                if (Server.rand.nextBoolean()) {
                    targetItem = 715;
                    break;
                }
                targetItem = 714;
                break;
            }
            case 103: {
                targetItem = Server.rand.nextBoolean() ? 713 : 716;
            }
        }
        try {
            m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
            m.create();
            ItemTemplate targetTemplate = ItemTemplateFactory.getInstance().getTemplate(targetItem);
            String actionString = Item.getMaterialString(targetTemplate.getMaterial()) + " " + targetTemplate.getName();
            int placementLocation = EpicTargetItems.getTargetItemPlacement(m.getId());
            String location = EpicTargetItems.getTargetItemPlacementString(placementLocation);
            String requirement = EpicTargetItems.getInstructionStringForKingdom(targetItem, favoredKingdomId);
            TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
            effect.setDescription("Create " + actionString);
            effect.setMissionStateChange(100.0f);
            effect.setTopText("Mission complete!");
            effect.setTextDisplayed("The " + targetTemplate.getName() + " is complete! " + epicEntityName + " is pleased.");
            String missionInstruction = epicEntityName + this.generateNeedString(baseDifficulty) + "you to construct " + targetTemplate.getNameWithGenus() + '.' + ' ' + requirement + ' ' + location;
            MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
            trig.setDescription("Create " + actionString);
            trig.setOnActionPerformed(148);
            trig.setOnItemUsedId(targetItem);
            m.setInstruction(missionInstruction.toString());
            this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
            return "Build structure mission successfully created for " + epicEntityName + ".";
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage());
            return "Error when creating build structure mission for " + epicEntityName + ".";
        }
    }

    private String createGenericRitualMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, Item ritualTargetItem, String targetName) {
        if (ritualTargetItem == null) {
            return "Error creating generic ritual mission for " + epicEntityName + ". Null target item.";
        }
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        String location = EpicServerStatus.getAreaString(ritualTargetItem.getTileX(), ritualTargetItem.getTileY());
        int action = EpicServerStatus.getRitualAction(favoredKingdomId);
        ActionEntry e = Actions.actionEntrys[action];
        String actionString = "perform the " + e.getActionString();
        int numbers = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setDescription(actionString);
        effect.setMissionStateChange(100.0f / (float)numbers);
        effect.setTopText("Mission complete!");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        StringBuilder sbuild = new StringBuilder(epicEntityName + this.generateNeedString(baseDifficulty) + numbers + " of you to " + actionString + " at the " + targetName);
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        if (baseDifficulty > 1) {
            ItemTemplate it = this.getRandomItemTemplateUsed();
            trig.setOnItemUsedId(it.getTemplateId());
            sbuild.append(" using " + it.getNameWithGenus());
        }
        sbuild.append(". It is located in the " + location + ".");
        trig.setDescription(actionString);
        trig.setOnActionPerformed(action);
        trig.setOnTargetId(ritualTargetItem.getWurmId());
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Ritual mission successfully created for " + epicEntityName + ".";
    }

    private String createMSRitualMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        Item ritualTargetItem = null;
        int itemFindAttempts = 0;
        while (ritualTargetItem == null) {
            VolaTile t;
            ritualTargetItem = EpicTargetItems.getRandomRitualTarget();
            if (ritualTargetItem != null && (t = Zones.getTileOrNull(ritualTargetItem.getTilePos(), ritualTargetItem.isOnSurface())) != null) {
                if (t.getVillage() != null || t.getStructure() != null) {
                    ritualTargetItem = null;
                }
                if (t.getKingdom() != targetKingdom) {
                    ritualTargetItem = null;
                }
            }
            if (ritualTargetItem != null || ++itemFindAttempts <= 50) continue;
            return "Error finding correct target item for ritual mission for " + epicEntityName + ".";
        }
        return this.createGenericRitualMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, ritualTargetItem, ritualTargetItem.getName());
    }

    private String createGTRitualMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        Item ritualTargetItem = null;
        int itemFindAttempts = 0;
        String targetName = "";
        while (ritualTargetItem == null && itemFindAttempts++ < 50) {
            GuardTower tower = Kingdoms.getRandomTowerForKingdom(targetKingdom);
            if (tower == null || (ritualTargetItem = tower.getTower()) == null) continue;
            if (ritualTargetItem.getKingdom() != targetKingdom) {
                ritualTargetItem = null;
                continue;
            }
            VolaTile t = Zones.getTileOrNull(ritualTargetItem.getTilePos(), ritualTargetItem.isOnSurface());
            if (t != null && (t.getVillage() != null || t.getStructure() != null)) {
                ritualTargetItem = null;
            }
            targetName = Kingdoms.getNameFor(targetKingdom) + " guard tower (" + tower.getName() + ")";
        }
        if (ritualTargetItem == null) {
            return "Error finding correct target item for guard tower ritual mission for " + epicEntityName + ".";
        }
        return this.createGenericRitualMission(m, newMission, epicEntityId, epicEntityName, baseDifficulty, favoredKingdomId, ritualTargetItem, targetName);
    }

    private String createMISacrificeMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        try {
            int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
            ItemTemplate usedTemplate = ItemTemplateFactory.getInstance().getTemplate(737);
            m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
            m.create();
            String itemName = HexMap.generateFirstName(m.getId()) + ' ' + HexMap.generateSecondName(m.getId());
            itemName = itemName + (required > 1 && !itemName.endsWith("s") ? "s" : "");
            int action = 142;
            String actionString = "Sacrifice";
            TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
            effect.setTopText("Mission progress");
            effect.setTextDisplayed(epicEntityName + " is pleased.");
            effect.setDescription("Sacrifice");
            effect.setMissionStateChange(100.0f / (float)required);
            StringBuilder sbuild = new StringBuilder(epicEntityName + this.generateNeedString(baseDifficulty) + "you to sacrifice " + required + " of the hidden " + itemName + " which can be found by investigating and digging in the wilderness.");
            MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
            trig.setDescription("Sacrifice");
            trig.setOnActionPerformed(142);
            trig.setOnTargetId(-10L);
            trig.setOnItemUsedId(usedTemplate.getTemplateId());
            m.setInstruction(sbuild.toString());
            this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
            return "Created sacrifice mission item mission for " + epicEntityName + ".";
        }
        catch (NoSuchTemplateException nst) {
            return "Error creating sacrifice mission item mission for " + epicEntityName + ". Unable to find template.";
        }
    }

    private String createGenericSacrificeMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        ItemTemplate usedTemplate = null;
        ItemTemplate[] templates = ItemTemplateFactory.getInstance().getEpicMissionTemplates();
        if (templates.length == 0) {
            return "Error creating generic sacrifice mission for " + epicEntityName + ". Failed to load templates.";
        }
        boolean found = false;
        try {
            ItemTemplate altar = ItemTemplateFactory.getInstance().getTemplate(322);
            while (!found) {
                usedTemplate = templates[Server.rand.nextInt(templates.length)];
                if (usedTemplate.getSizeZ() > altar.getSizeZ() || usedTemplate.getSizeY() > altar.getSizeY() || usedTemplate.getSizeX() > altar.getSizeX()) continue;
                CreationEntry ce = CreationMatrix.getInstance().getCreationEntry(usedTemplate.getTemplateId());
                if (ce != null) {
                    CreationEntry ceTarg;
                    CreationEntry ceSource;
                    required /= Math.min(100, ce.getTotalNumberOfItems());
                    if ((ce.depleteSource || ce.depleteEqually) && (ceSource = CreationMatrix.getInstance().getCreationEntry(ce.getObjectSource())) != null && ceSource instanceof AdvancedCreationEntry) {
                        required /= Math.min(100, ceSource.getTotalNumberOfItems());
                    }
                    if ((ce.depleteTarget || ce.depleteEqually) && (ceTarg = CreationMatrix.getInstance().getCreationEntry(ce.getObjectTarget())) != null && ceTarg instanceof AdvancedCreationEntry) {
                        required /= Math.min(100, ceTarg.getTotalNumberOfItems());
                    }
                }
                found = true;
            }
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        required = Math.max(required, 1);
        if (usedTemplate == null) {
            return "Error creating generic sacrifice mission for " + epicEntityName + ". Null template.";
        }
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        String itemName = " decent " + usedTemplate.sizeString;
        itemName = itemName + (required > 1 ? usedTemplate.getPlural() : usedTemplate.getName());
        int action = 142;
        String actionString = "Sacrifice";
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        effect.setDescription("Sacrifice");
        effect.setMissionStateChange(100.0f / (float)required);
        StringBuilder sbuild = new StringBuilder(epicEntityName + this.generateNeedString(baseDifficulty) + "you to sacrifice " + required + itemName + ".");
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setDescription("Sacrifice");
        trig.setOnActionPerformed(142);
        trig.setOnTargetId(-10L);
        trig.setOnItemUsedId(usedTemplate.getTemplateId());
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Created sacrifice mission item mission for " + epicEntityName + ".";
    }

    private String createCutTreeMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        int action = 492;
        if (Villages.getVillages().length == 0) {
            return "Error creating cut down tree mission for " + epicEntityName + ". No villages.";
        }
        Village randVillage = null;
        int villageAttempts = 0;
        while (randVillage == null) {
            randVillage = Villages.getVillages()[Server.rand.nextInt(Villages.getVillages().length)];
            if (randVillage.kingdom != targetKingdom) {
                randVillage = null;
            } else {
                byte direction = (byte)Server.rand.nextInt(8);
                int[] position = EpicServerStatus.getTileOutsideVillage(direction, randVillage.getPerimeterSize() + baseDifficulty * baseDifficulty, randVillage.getPerimeterSize() + baseDifficulty * baseDifficulty, randVillage);
                if (position[0] < 0 || position[1] < 0) {
                    randVillage = null;
                } else {
                    m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
                    m.create();
                    String dirProximityString = MiscConstants.getDirectionString(direction) + " of " + randVillage.getName();
                    long nextTarget = EpicServerStatus.getTileId(position[0], position[1]);
                    String location = EpicServerStatus.getAreaString(position[0], position[1]);
                    int tileToCheck = Zones.getTileIntForTile(position[0], position[1], 0);
                    Tiles.Tile theTile = Tiles.getTile(Tiles.decodeType(tileToCheck));
                    String treeName = theTile.getTileName(Tiles.decodeData(tileToCheck));
                    String actionString = "cut down ";
                    byte kingdom = Zones.getKingdom(position[0], position[1]);
                    TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
                    effect.setMissionStateChange(100.0f);
                    effect.setDescription("cut down  " + treeName + " near " + randVillage.getName());
                    StringBuilder sbuild = new StringBuilder(epicEntityName + this.generateNeedString(baseDifficulty) + "you to " + "cut down " + "the " + treeName + ' ' + dirProximityString);
                    sbuild.append(". It is located in the " + location);
                    Kingdom k2 = Kingdoms.getKingdom(kingdom);
                    if (k2 != null) {
                        sbuild.append(" in " + k2.getName() + '.');
                    } else {
                        sbuild.append('.');
                    }
                    MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
                    trig.setDescription("cut down ");
                    trig.setOnActionPerformed(492);
                    trig.setOnTargetId(nextTarget);
                    m.setInstruction(sbuild.toString());
                    try {
                        float xTree = (float)(position[0] << 2) + 4.0f * TerrainUtilities.getTreePosX(position[0], position[1]);
                        float yTree = (float)(position[1] << 2) + 4.0f * TerrainUtilities.getTreePosY(position[0], position[1]);
                        float zTree = Zones.calculateHeight(xTree, yTree, true) + 4.0f;
                        EffectFactory.getInstance().createGenericEffect(nextTarget, "tree", xTree, yTree, zTree, true, -1.0f, Server.rand.nextInt(360));
                    }
                    catch (NoSuchZoneException e) {
                        logger.log(Level.WARNING, "Unable to add tree effect when creating mission", e);
                    }
                    this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
                    return "Cut tree mission successfully created for " + epicEntityName + ".";
                }
            }
            if (++villageAttempts <= 30) continue;
        }
        return "Error creating cut down tree mission for " + epicEntityName + ".";
    }

    private String createCreateItemMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        CreationEntry[] entries = CreationMatrix.getInstance().getAdvancedEntriesNotEpicMission();
        if (entries.length == 0) {
            return "Error creating create item mission for " + epicEntityName + ". Found no creation entries.";
        }
        int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        ItemTemplate usedTemplate = null;
        try {
            usedTemplate = ItemTemplateFactory.getInstance().getTemplate(entries[Server.rand.nextInt(entries.length)].getObjectCreated());
            CreationEntry ce = CreationMatrix.getInstance().getCreationEntry(usedTemplate.getTemplateId());
            if (ce != null) {
                CreationEntry ceTarg;
                CreationEntry ceSource;
                required /= Math.min(100, ce.getTotalNumberOfItems());
                if ((ce.depleteSource || ce.depleteEqually) && (ceSource = CreationMatrix.getInstance().getCreationEntry(ce.getObjectSource())) != null && ceSource instanceof AdvancedCreationEntry) {
                    required /= Math.min(100, ceSource.getTotalNumberOfItems());
                }
                if ((ce.depleteTarget || ce.depleteEqually) && (ceTarg = CreationMatrix.getInstance().getCreationEntry(ce.getObjectTarget())) != null && ceTarg instanceof AdvancedCreationEntry) {
                    required /= Math.min(100, ceTarg.getTotalNumberOfItems());
                }
            }
        }
        catch (NoSuchTemplateException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        required = Math.max(required, 1);
        if (usedTemplate == null) {
            return "Error creating create item mission for " + epicEntityName + ". Null item template.";
        }
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        int action = 148;
        String actionString = "Create";
        String itemName = usedTemplate.sizeString;
        itemName = itemName + (required > 1 ? usedTemplate.getPlural() : usedTemplate.getName());
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setDescription("Create");
        effect.setMissionStateChange(100.0f / (float)required);
        StringBuilder sbuild = new StringBuilder(epicEntityName + this.generateNeedString(baseDifficulty) + "you to create " + required + ' ' + itemName + '.');
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setDescription("Create");
        trig.setOnActionPerformed(148);
        trig.setOnItemUsedId(usedTemplate.getTemplateId());
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Create item mission created successfully for " + epicEntityName + ".";
    }

    private String createGiveItemMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        String prefix;
        ItemTemplate[] templates = ItemTemplateFactory.getInstance().getEpicMissionTemplates();
        if (templates.length == 0) {
            return "Error creating give item mission for " + epicEntityName + ". Unable to load templates.";
        }
        ItemTemplate usedTemplate = templates[Server.rand.nextInt(templates.length)];
        int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        String string = required == 1 && !usedTemplate.getName().endsWith("s") ? (usedTemplate.sizeString.equals("") ? (StringUtilities.isVowel(usedTemplate.getName().charAt(0)) ? "an " : "a ") : (StringUtilities.isVowel(usedTemplate.sizeString.charAt(0)) ? "an " : "a ")) : (prefix = "");
        if (Villages.getVillages().length == 0) {
            return "Error creating give item mission for " + epicEntityName + ". No villages.";
        }
        Village randVillage = null;
        int villageAttempts = 0;
        while (randVillage == null) {
            randVillage = Villages.getVillages()[Server.rand.nextInt(Villages.getVillages().length)];
            if (randVillage.kingdom != targetKingdom) {
                randVillage = null;
            } else {
                byte direction = (byte)Server.rand.nextInt(8);
                int[] position = EpicServerStatus.getTileOutsideVillage(direction, randVillage.getDiameterX() * baseDifficulty, randVillage.getDiameterY() * baseDifficulty, randVillage);
                if (position[0] < 0 || position[1] < 0) {
                    randVillage = null;
                } else {
                    int cid = 68;
                    if (epicEntityId == 4) {
                        cid = 81;
                    } else if (epicEntityId == 3) {
                        cid = 78;
                    } else if (epicEntityId == 1) {
                        cid = 80;
                    } else if (epicEntityId == 2) {
                        cid = 79;
                    }
                    try {
                        CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(cid);
                        byte sex = ct.getSex();
                        Creature target = Creature.doNew(cid, false, (position[0] << 2) + 2, (position[1] << 2) + 2, Server.rand.nextFloat() * 360.0f, 0, "Avatar of " + epicEntityName, sex, favoredKingdomId, (byte)0, false);
                        String proximityString = "near " + randVillage.getName();
                        int action = 47;
                        String actionString = "Give";
                        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
                        m.create();
                        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
                        effect.setTopText("Mission progress");
                        effect.setTextDisplayed(epicEntityName + " is pleased.");
                        effect.setDescription("Give");
                        effect.setMissionStateChange(100.0f / (float)required);
                        effect.setDestroysTarget(true);
                        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
                        trig.setDescription("Give");
                        trig.setOnActionPerformed(47);
                        trig.setOnTargetId(target.getWurmId());
                        trig.setOnItemUsedId(usedTemplate.getTemplateId());
                        String itemName = usedTemplate.sizeString;
                        itemName = itemName + (required > 1 ? usedTemplate.getPlural() : usedTemplate.getName());
                        StringBuilder sbuild = new StringBuilder();
                        sbuild.append(epicEntityName + this.generateNeedString(baseDifficulty) + ' ' + required + " of you to bring and give " + prefix + itemName + " to " + target.getName() + ". ");
                        sbuild.append(target.getName().substring(0, 1).toUpperCase() + target.getName().substring(1));
                        sbuild.append(" was last seen " + proximityString + " in the " + EpicServerStatus.getAreaString(position[0], position[1]) + '.');
                        m.setInstruction(sbuild.toString());
                        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
                        return "Give item mission created successfully for " + epicEntityName + ".";
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, ex.getMessage());
                    }
                }
            }
            if (++villageAttempts <= 30) continue;
        }
        return "Error creating give item mission for " + epicEntityName + ". Unable to find suitable village.";
    }

    private String createSlayCreatureMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        CreatureTemplate[] templates = CreatureTemplateFactory.getInstance().getTemplates();
        if (templates.length == 0) {
            return "Error creating slay creature mission for " + epicEntityName + ". Unable to load templates.";
        }
        ArrayList<CreatureTemplate> possibleTemplates = new ArrayList<CreatureTemplate>();
        CreatureTemplate usedTemplate = null;
        boolean forceChamps = false;
        switch (newMission.getMissionType()) {
            case 114: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || !t.isHerbivore() || t.isBabyCreature()) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
            case 115: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || !t.isEpicMissionSlayable() || t.isBabyCreature()) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
            case 116: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || (!Servers.localServer.PVPSERVER || !t.isFromValrei || t.getTemplateId() == 68) && (!t.isEpicMissionSlayable() || !(t.getBaseCombatRating() > 7.0f))) continue;
                    possibleTemplates.add(t);
                    if (Servers.localServer.PVPSERVER) continue;
                    forceChamps = true;
                }
                break;
            }
        }
        if (!possibleTemplates.isEmpty()) {
            usedTemplate = (CreatureTemplate)possibleTemplates.get(Server.rand.nextInt(possibleTemplates.size()));
        }
        if (usedTemplate == null) {
            return "Error creating slay creature mission for " + epicEntityName + ". Null creature template.";
        }
        int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        int action = 491;
        String actionString = "Slay";
        int requiredSpawn = Zones.worldTileSizeX / 2048 * required;
        if (newMission.getMissionType() == 116) {
            requiredSpawn /= 2;
        }
        requiredSpawn = (int)Math.max((float)required * 1.5f, (float)requiredSpawn);
        for (int i = 0; i < requiredSpawn; ++i) {
            EpicServerStatus.spawnSingleCreature(usedTemplate, forceChamps);
        }
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        effect.setDescription("Slay");
        effect.setMissionStateChange(100.0f / (float)required);
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setOnItemUsedId(usedTemplate.getTemplateId());
        trig.setDescription("Slay");
        trig.setOnActionPerformed(491);
        String creatureName = required > 1 ? usedTemplate.getPlural() : usedTemplate.getName();
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(epicEntityName + this.generateNeedString(baseDifficulty) + "you to slay " + required + (forceChamps ? " champion " : " ") + creatureName + " that have appeared across the land.");
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Slay creature mission successfully created for " + epicEntityName + ".";
    }

    private String createSlayTraitorMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        CreatureTemplate[] templates = CreatureTemplateFactory.getInstance().getTemplates();
        if (templates.length == 0) {
            return "Error creating slay creature mission for " + epicEntityName + ". Unable to load templates.";
        }
        ArrayList<CreatureTemplate> possibleTemplates = new ArrayList<CreatureTemplate>();
        CreatureTemplate usedTemplate = null;
        boolean forceChamps = false;
        switch (newMission.getMissionType()) {
            case 117: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || !t.isHerbivore() || t.isBabyCreature()) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
            case 118: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || !t.isEpicMissionTraitor() || t.isBabyCreature()) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
            case 119: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || (!Servers.localServer.PVPSERVER || !t.isFromValrei) && (!t.isEpicMissionTraitor() || !(t.getBaseCombatRating() > 7.0f))) continue;
                    possibleTemplates.add(t);
                    if (Servers.localServer.PVPSERVER) continue;
                    forceChamps = true;
                }
                break;
            }
        }
        if (!possibleTemplates.isEmpty()) {
            usedTemplate = (CreatureTemplate)possibleTemplates.get(Server.rand.nextInt(possibleTemplates.size()));
        }
        if (usedTemplate == null) {
            return "Error creating slay traitor mission for " + epicEntityName + ". Null creature template.";
        }
        int action = 491;
        String actionString = "Slay";
        Creature spawnedTraitor = EpicServerStatus.spawnSingleCreature(usedTemplate, forceChamps);
        if (spawnedTraitor == null) {
            return "Error creating slay traitor mission for " + epicEntityName + ". Failed to create creature.";
        }
        spawnedTraitor.setName(usedTemplate.getName() + " (traitor)");
        spawnedTraitor.getStatus().setTraitBit(28, true);
        SpellEffect eff = new SpellEffect(spawnedTraitor.getWurmId(), 22, 80.0f, 20000000, 9, 0, true);
        if (spawnedTraitor.getSpellEffects() == null) {
            spawnedTraitor.createSpellEffects();
        }
        spawnedTraitor.getSpellEffects().addSpellEffect(eff);
        spawnedTraitor.setVisible(false);
        Zones.flash(spawnedTraitor.getTileX(), spawnedTraitor.getTileY(), false);
        spawnedTraitor.setVisible(true);
        Effect traitorEffect = EffectFactory.getInstance().createGenericEffect(spawnedTraitor.getWurmId(), "traitor", spawnedTraitor.getPosX(), spawnedTraitor.getPosY(), spawnedTraitor.getPositionZ() + (float)spawnedTraitor.getHalfHeightDecimeters() / 10.0f, spawnedTraitor.isOnSurface(), -1.0f, spawnedTraitor.getStatus().getRotation());
        spawnedTraitor.addEffect(traitorEffect);
        try {
            spawnedTraitor.getStatus().setChanged(true);
            spawnedTraitor.save();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Unable to save new traitor creature.", e);
        }
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        effect.setDescription("Slay");
        effect.setMissionStateChange(100.0f);
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setOnTargetId(spawnedTraitor.getWurmId());
        trig.setDescription("Slay");
        trig.setOnActionPerformed(491);
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(epicEntityName + this.generateNeedString(baseDifficulty) + "you to slay the traitor" + (forceChamps ? " champion " : " ") + usedTemplate.getName() + " seen fleeing from Valrei to these lands. It was last spotted in the " + EpicServerStatus.getAreaString(spawnedTraitor.getTileX(), spawnedTraitor.getTileY()) + ".");
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Slay traitor mission successfully created for " + epicEntityName + ".";
    }

    private String createSacrificeCreatureMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        CreatureTemplate[] templates = CreatureTemplateFactory.getInstance().getTemplates();
        if (templates.length == 0) {
            return "Error creating sacrifice creature mission for " + epicEntityName + ". Unable to load templates.";
        }
        ArrayList<CreatureTemplate> possibleTemplates = new ArrayList<CreatureTemplate>();
        CreatureTemplate usedTemplate = null;
        switch (newMission.getMissionType()) {
            case 121: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || t.isSubmerged() || !t.isHerbivore() || t.isBabyCreature()) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
            case 122: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || t.isSubmerged() || !t.isEpicMissionSlayable() || !(t.getBaseCombatRating() <= 7.0f) || t.isBabyCreature()) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
            case 123: {
                for (CreatureTemplate t : templates) {
                    if (t.isMissionDisabled() || t.isSubmerged() || (!Servers.localServer.PVPSERVER || !t.isFromValrei) && (!t.isEpicMissionSlayable() || !(t.getBaseCombatRating() > 7.0f))) continue;
                    possibleTemplates.add(t);
                }
                break;
            }
        }
        if (!possibleTemplates.isEmpty()) {
            usedTemplate = (CreatureTemplate)possibleTemplates.get(Server.rand.nextInt(possibleTemplates.size()));
        }
        if (usedTemplate == null) {
            return "Error creating sacrifice creature mission for " + epicEntityName + ". Null creature template.";
        }
        int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        int action = 142;
        String actionString = "Sacrifice";
        int requiredSpawn = Math.max(required, Zones.worldTileSizeX / 2048 * required);
        for (int i = 0; i < requiredSpawn; ++i) {
            EpicServerStatus.spawnSingleCreature(usedTemplate, false);
        }
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        effect.setDescription("Sacrifice");
        effect.setMissionStateChange(100.0f / (float)required);
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setOnItemUsedId(-usedTemplate.getTemplateId());
        trig.setOnTargetId(-10L);
        trig.setDescription("Sacrifice");
        trig.setOnActionPerformed(142);
        String creatureName = required > 1 ? usedTemplate.getPlural() : usedTemplate.getName();
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(epicEntityName + this.generateNeedString(baseDifficulty) + "you to sacrifice " + required + " " + creatureName + " that " + (required > 1 ? "have appeared across the land" : "has appeared in the world") + ". Use a sacrificial knife on them inside the domain of " + epicEntityName + " after weakening them to at least half of their health.");
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Sacrifice creature mission successfully created for " + epicEntityName + ".";
    }

    private String createSlayTowerGuardsMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        CreatureTemplate usedTemplate;
        int templateId = 67;
        Kingdom k = Kingdoms.getKingdom(targetKingdom);
        if (k == null) {
            return "Error creating slay tower guards mission for " + epicEntityName + ". Invalid kingdom.";
        }
        switch (k.getTemplate()) {
            case 3: {
                templateId = 35;
                break;
            }
            case 1: {
                templateId = 34;
                break;
            }
            case 2: {
                templateId = 36;
                break;
            }
            default: {
                return "Error creating slay tower guards mission for " + epicEntityName + ". Invalid kingdom template.";
            }
        }
        try {
            usedTemplate = CreatureTemplateFactory.getInstance().getTemplate(templateId);
        }
        catch (NoSuchCreatureTemplateException e) {
            return "Error creating slay tower guards mission for " + epicEntityName + ". Invalid creature template.";
        }
        int required = EpicServerStatus.getNumberRequired(baseDifficulty, newMission);
        int action = 491;
        String actionString = "Slay";
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        effect.setDescription("Slay");
        effect.setMissionStateChange(100.0f / (float)required);
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setOnItemUsedId(usedTemplate.getTemplateId());
        trig.setDescription("Slay");
        trig.setOnActionPerformed(491);
        String creatureName = required > 1 ? usedTemplate.getPlural() : usedTemplate.getName();
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(epicEntityName + this.generateNeedString(baseDifficulty) + "you to slay " + required + " " + creatureName + " to help cleanse the lands.");
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Slay tower guard mission successfully created for " + epicEntityName + ".";
    }

    private String createDestroyGTMission(Mission m, EpicMissionEnum newMission, int epicEntityId, String epicEntityName, int baseDifficulty, byte favoredKingdomId, byte targetKingdom) {
        Item targetTower = null;
        int itemFindAttempts = 0;
        while (targetTower == null && itemFindAttempts++ < 50 && Zones.getGuardTowers().size() > 0) {
            targetTower = Zones.getGuardTowers().get(Server.rand.nextInt(Zones.getGuardTowers().size()));
            if (targetTower == null) continue;
            if (targetTower.getKingdom() != targetKingdom) {
                targetTower = null;
                continue;
            }
            VolaTile t = Zones.getTileOrNull(targetTower.getTilePos(), targetTower.isOnSurface());
            if (t == null || t.getVillage() == null && t.getStructure() == null) continue;
            targetTower = null;
        }
        if (targetTower == null) {
            return "Error finding correct target item for destroy guard tower mission for " + epicEntityName + ".";
        }
        int action = 913;
        String actionString = "Destroy";
        m.setName(EpicServerStatus.getMissionName(epicEntityName, newMission));
        m.create();
        TriggerEffect effect = this.initializeTriggerEffect(epicEntityId, epicEntityName);
        effect.setTopText("Mission progress");
        effect.setTextDisplayed(epicEntityName + " is pleased.");
        effect.setDescription("Destroy");
        effect.setMissionStateChange(100.0f);
        MissionTrigger trig = this.initializeMissionTrigger(epicEntityId, epicEntityName);
        trig.setOnTargetId(targetTower.getWurmId());
        trig.setDescription("Destroy");
        trig.setOnActionPerformed(913);
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(epicEntityName + this.generateNeedString(baseDifficulty) + "you to destroy " + targetTower.getName() + " which can be found in the " + EpicServerStatus.getAreaString(targetTower.getTileX(), targetTower.getTileY()) + ".");
        m.setInstruction(sbuild.toString());
        this.linkMission(m, trig, effect, newMission.getMissionType(), baseDifficulty);
        return "Destroy guard tower mission successfully created for " + epicEntityName + ".";
    }

    private static int[] getTileOutsideVillage(byte direction, int distanceX, int distanceY, Village v) {
        int startX = v.getTokenX();
        int startY = v.getTokenY();
        if (direction == 7 || direction == 0 || direction == 1) {
            startY = v.getStartY() - distanceY;
        } else if (direction == 6 || direction == 2) {
            startY = v.getStartY();
            distanceY = v.getDiameterY();
        } else {
            startY = v.getEndY();
        }
        if (direction == 7 || direction == 6 || direction == 5) {
            startX = v.getStartX() - distanceX;
        } else if (direction == 0 || direction == 4) {
            startX = v.getStartX();
            distanceX = v.getDiameterX();
        } else {
            startX = v.getEndX();
        }
        int tileAttempts = 0;
        int tileX = -1;
        int tileY = -1;
        while ((tileX < 0 || tileY < 0) && tileAttempts++ < 50) {
            int tempY;
            int tempX = startX + Server.rand.nextInt(Math.max(1, distanceX));
            Village vt = Villages.getVillageWithPerimeterAt(tempX, tempY = startY + Server.rand.nextInt(Math.max(1, distanceY)), true);
            if (vt != null) continue;
            int tileToCheck = Zones.getTileIntForTile(tempX, tempY, 0);
            byte type = Tiles.decodeType(tileToCheck);
            byte data = Tiles.decodeData(tileToCheck);
            Tiles.Tile theTile = Tiles.getTile(type);
            if (!theTile.isTree() || theTile.isEnchanted() || FoliageAge.getAgeAsByte(data) <= FoliageAge.MATURE_ONE.getAgeId() || FoliageAge.getAgeAsByte(data) >= FoliageAge.OVERAGED.getAgeId()) continue;
            tileX = tempX;
            tileY = tempY;
        }
        return new int[]{tileX, tileY};
    }

    public static final int getNumberRequired(int difficulty, EpicMissionEnum newMission) {
        int playerMax = (int)(Servers.localServer.PVPSERVER ? (double)difficulty * 1.5 : (double)difficulty * 2.5);
        switch (newMission.getMissionType()) {
            case 101: 
            case 102: 
            case 103: {
                return 1;
            }
            case 104: 
            case 105: {
                return Math.max(1, Math.min(difficulty * difficulty, playerMax));
            }
            case 106: 
            case 107: {
                return 1;
            }
            case 108: {
                return Math.max(1, playerMax);
            }
            case 109: {
                return Math.max(1, difficulty * difficulty);
            }
            case 110: 
            case 111: {
                return Math.max(1, difficulty * 75);
            }
            case 112: 
            case 113: {
                return Math.max(1, Math.min(difficulty * difficulty, playerMax));
            }
            case 114: 
            case 115: {
                return Math.max(1, difficulty * difficulty * (difficulty / 2) + 5);
            }
            case 116: {
                return Math.max(1, (difficulty * difficulty * difficulty + 5) / 15);
            }
            case 117: 
            case 118: 
            case 119: {
                return 1;
            }
            case 120: {
                return 1;
            }
            case 121: 
            case 122: 
            case 123: {
                return Math.max(1, (int)Math.min((double)(difficulty * difficulty), (double)difficulty * 2.5));
            }
            case 124: {
                return Math.max(1, difficulty * difficulty * (difficulty / 2));
            }
        }
        return 1;
    }

    private final PathTile getTargetVillage(byte requiredKingdom) {
        PathTile tile = null;
        Village[] varr = Villages.getVillages();
        ArrayList<Village> prospects = new ArrayList<Village>();
        for (Village lElement : varr) {
            if (lElement.kingdom != requiredKingdom) continue;
            prospects.add(lElement);
        }
        if (prospects.size() > 0) {
            Village prosp = (Village)prospects.get(Server.rand.nextInt(prospects.size()));
            tile = new PathTile(prosp.getTokenX(), prosp.getTokenY(), 0, true, 0);
        }
        return tile;
    }

    private static final byte[] getEnemyKingdoms(int epicEntityId, byte favoredKingdom) {
        HashSet<Byte> toReturn = new HashSet<Byte>();
        if (favoredKingdom != 0) {
            Kingdom[] karr;
            for (Kingdom lElement : karr = Kingdoms.getAllKingdoms()) {
                if (lElement.getTemplate() == favoredKingdom || lElement.isAllied(favoredKingdom)) continue;
                toReturn.add(lElement.getId());
            }
        }
        byte[] toRet = new byte[toReturn.size()];
        int x = 0;
        for (Byte b : toReturn) {
            toRet[x] = b;
            ++x;
        }
        return toRet;
    }

    private static final String getMissionName(String epicEntityName, EpicMissionEnum newMission) {
        StringBuilder builder = new StringBuilder();
        boolean startWithName = Server.rand.nextBoolean();
        if (startWithName) {
            builder.append(epicEntityName);
            builder.append("'s ");
        }
        if (Server.rand.nextBoolean() && missionAdjectives.length > 0) {
            builder.append(missionAdjectives[Server.rand.nextInt(missionAdjectives.length)]);
        }
        builder.append(missionNames[Server.rand.nextInt(missionNames.length)]);
        if (!startWithName) {
            builder.append(missionFor[Server.rand.nextInt(missionFor.length)]);
            builder.append(epicEntityName);
        } else if (Server.rand.nextInt(3) == 0) {
            if (Server.rand.nextBoolean()) {
                builder.append(" of ");
            } else {
                builder.append(" for ");
            }
            builder.append(newMission.getRandomMissionName());
        }
        String toret = LoginHandler.raiseFirstLetter(builder.toString());
        if (!startWithName) {
            toret = toret.replace(epicEntityName.toLowerCase(), epicEntityName);
        }
        return toret;
    }

    public static EpicMission getMISacrificeMission() {
        matchingMissions.clear();
        for (EpicMission m : EpicServerStatus.getCurrentEpicMissions()) {
            if (m.getMissionType() != 109) continue;
            matchingMissions.add(m);
        }
        if (matchingMissions.isEmpty()) {
            return null;
        }
        return matchingMissions.get(Server.rand.nextInt(matchingMissions.size()));
    }

    public static EpicMission[] getTraitorMissions() {
        matchingMissions.clear();
        for (EpicMission m : EpicServerStatus.getCurrentEpicMissions()) {
            if (m.getMissionType() != 117 && m.getMissionType() != 118 && m.getMissionType() != 119) continue;
            matchingMissions.add(m);
        }
        if (matchingMissions.isEmpty()) {
            return null;
        }
        return matchingMissions.toArray(new EpicMission[matchingMissions.size()]);
    }

    public static Creature[] getCurrentTraitors() {
        EpicMission[] missions = EpicServerStatus.getTraitorMissions();
        if (missions != null) {
            ArrayList<Creature> creatureList = new ArrayList<Creature>();
            for (EpicMission m : missions) {
                if (m.getMissionProgress() < 1.0f || m.getMissionProgress() >= 100.0f) continue;
                for (MissionTrigger mt : MissionTriggers.getAllTriggers()) {
                    if (mt.getMissionRequired() != m.getMissionId()) continue;
                    long traitorId = mt.getTarget();
                    Creature c = Creatures.getInstance().getCreatureOrNull(traitorId);
                    if (c == null) continue;
                    creatureList.add(c);
                }
            }
            if (creatureList.isEmpty()) {
                return null;
            }
            return creatureList.toArray(new Creature[creatureList.size()]);
        }
        return null;
    }

    public static boolean doesGiveItemMissionExist(long creatureId) {
        for (MissionTrigger mt : MissionTriggers.getAllTriggers()) {
            EpicMission mis;
            if (mt.getOnActionPerformed() != 47 || mt.getTarget() != creatureId || (mis = EpicServerStatus.getEpicMissionForMission(mt.getMissionRequired())) == null) continue;
            return true;
        }
        return false;
    }

    public static boolean doesTraitorMissionExist(long wurmId) {
        Creature[] cList = EpicServerStatus.getCurrentTraitors();
        if (cList == null) {
            return false;
        }
        for (Creature c : cList) {
            if (c.getWurmId() != wurmId) continue;
            return true;
        }
        return false;
    }

    public static void avatarCreatureKilled(long creatureId) {
        for (MissionTrigger mt : MissionTriggers.getAllTriggers()) {
            EpicMission mis;
            if (mt.getOnActionPerformed() != 47 || mt.getTarget() != creatureId || (mis = EpicServerStatus.getEpicMissionForMission(mt.getMissionRequired())) == null) continue;
            EpicServerStatus.destroySpecificMission(mis);
            WcEpicStatusReport wce = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, mis.getEpicEntityId(), mis.getMissionType(), mis.getDifficulty());
            wce.sendToLoginServer();
            EpicServerStatus.storeLastMissionForEntity(mis.getEpicEntityId(), mis);
        }
    }

    public static void traitorCreatureKilled(long creatureId) {
        for (MissionTrigger mt : MissionTriggers.getAllTriggers()) {
            EpicMission mis;
            if (mt.getOnActionPerformed() != 491 || mt.getTarget() != creatureId || (mis = EpicServerStatus.getEpicMissionForMission(mt.getMissionRequired())) == null) continue;
            EpicServerStatus.destroySpecificMission(mis);
            WcEpicStatusReport wce = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, mis.getEpicEntityId(), mis.getMissionType(), mis.getDifficulty());
            wce.sendToLoginServer();
            EpicServerStatus.storeLastMissionForEntity(mis.getEpicEntityId(), mis);
        }
    }

    @Nullable
    private static Creature spawnSingleCreature(CreatureTemplate template, boolean champion) {
        int attempts = 0;
        while (attempts < 5000) {
            ++attempts;
            int centerx = Server.rand.nextInt(Zones.worldTileSizeX);
            int centery = Server.rand.nextInt(Zones.worldTileSizeY);
            for (int x = 0; x < 10; ++x) {
                int tx = Zones.safeTileX(centerx - 5 + Server.rand.nextInt(10));
                int ty = Zones.safeTileY(centery - 5 + Server.rand.nextInt(10));
                try {
                    VolaTile t;
                    float height = Zones.calculateHeight(tx * 4 + 2, ty * 4 + 2, true);
                    if (!(height >= 0.0f && !template.isSubmerged() || template.isSubmerged() && height < -30.0f) && (!template.isSwimming() || !(height < -2.0f) || !(height > -30.0f)) || (t = Zones.getOrCreateTile(tx, ty, true)).getStructure() != null || t.getVillage() != null) continue;
                    byte sex = template.getSex();
                    if (sex == 0 && !template.keepSex && Server.rand.nextBoolean()) {
                        sex = 1;
                    }
                    byte ctype = champion ? (byte)99 : 0;
                    Creature toReturn = Creature.doNew(template.getTemplateId(), false, tx * 4 + 2, ty * 4 + 2, Server.rand.nextFloat() * 360.0f, 0, template.getName(), sex, (byte)0, ctype, false, (byte)(Server.rand.nextInt(8) + 4));
                    toReturn.getStatus().setTraitBit(29, true);
                    return toReturn;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    public static void storeLastMissionForEntity(int epicEntityId, EpicMission em) {
        EpicEntity entity = EpicServerStatus.getValrei().getEntity(epicEntityId);
        if (entity != null) {
            entity.setLatestMissionDifficulty(em.getDifficulty());
        } else {
            if (backupDifficultyMap == null) {
                backupDifficultyMap = new HashMap();
            }
            backupDifficultyMap.put(epicEntityId, em.getDifficulty());
        }
    }

    public static EpicMission getRitualMissionForTarget(long targetId) {
        ArrayList<MissionTrigger> allTriggers = new ArrayList<MissionTrigger>();
        for (int actionId = 496; actionId <= 502; ++actionId) {
            MissionTrigger[] triggers;
            for (MissionTrigger t : triggers = MissionTriggers.getMissionTriggersWith(-1, actionId, targetId)) {
                allTriggers.add(t);
            }
        }
        if (allTriggers.isEmpty()) {
            return null;
        }
        return EpicServerStatus.getEpicMissionForMission(((MissionTrigger)allTriggers.get(0)).getMissionRequired());
    }

    public static EpicMission getBuildMissionForTemplate(int templateId) {
        MissionTrigger[] triggers;
        ArrayList<MissionTrigger> allTriggers = new ArrayList<MissionTrigger>();
        for (MissionTrigger t : triggers = MissionTriggers.getMissionTriggersWith(templateId, 148, -1L)) {
            allTriggers.add(t);
        }
        if (allTriggers.isEmpty()) {
            return null;
        }
        return EpicServerStatus.getEpicMissionForMission(((MissionTrigger)allTriggers.get(0)).getMissionRequired());
    }

    static {
        itemplates = new ArrayList<ItemTemplate>();
        EpicServerStatus.setupMissionItemTemplates();
        valrei = null;
        missionAdjectives = new String[]{"last ", "horrendous ", "shining ", "first ", "scary ", "mysterious ", "enigmatic ", "important ", "strong ", "massive ", "gigantic ", "heavy ", "light ", "bright ", "deadly ", "dangerous ", "marked ", "fantastic ", "imposing ", "paradoxical ", "final "};
        missionNames = new String[]{"whisper", "gesture", "tears", "laughter", "horror", "mystery", "enigma", "celebration", "word", "run", "challenge", "test", "jest", "joke", "need", "quest", "trip", "folly", "lesson", "journey", "adventure"};
        missionFor = new String[]{" to ", " for ", " from ", " in honour of ", " of ", " to help ", " in aid of ", " in service of "};
        matchingMissions = new ArrayList();
    }
}


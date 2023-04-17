/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.Triggers2Effects;
import com.wurmonline.server.utils.DbUtilities;
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

public final class TriggerEffects
implements CounterTypes {
    private static Logger logger = Logger.getLogger(TriggerEffects.class.getName());
    public static final String LOADALLEFFECTS = "SELECT * FROM TRIGGEREFFECTS";
    private static final Map<Integer, TriggerEffect> effects = new ConcurrentHashMap<Integer, TriggerEffect>();
    public static final int SHOW_ALL = 0;
    public static final int SHOW_LINKED = 1;
    public static final int SHOW_UNLINKED = 2;

    private TriggerEffects() {
    }

    public static void addTriggerEffect(TriggerEffect effect) {
        effects.put(effect.getId(), effect);
    }

    public static int getNumEffects() {
        return effects.size();
    }

    public static TriggerEffect[] getAllEffects() {
        return effects.values().toArray(new TriggerEffect[effects.size()]);
    }

    public static TriggerEffect[] getEffectsForTrigger(int triggerId, boolean incInactive) {
        return Triggers2Effects.getEffectsForTrigger(triggerId, incInactive);
    }

    public static TriggerEffect[] getEffectsForMission(int missionId) {
        HashSet<TriggerEffect> effs = new HashSet<TriggerEffect>();
        for (TriggerEffect effect : effects.values()) {
            if (effect.getMissionId() != missionId) continue;
            effs.add(effect);
        }
        return effs.toArray(new TriggerEffect[effs.size()]);
    }

    public static TriggerEffect[] getFilteredEffects(MissionTrigger[] trigs, Creature creature, int linked, boolean incInactive, boolean dontListMine, boolean listMineOnly, long listForUser, boolean showAll) {
        HashSet<TriggerEffect> effs = new HashSet<TriggerEffect>();
        for (TriggerEffect effect : effects.values()) {
            boolean found = showAll;
            if (!found) {
                for (MissionTrigger trig : trigs) {
                    if (!Triggers2Effects.hasLink(trig.getId(), effect.getId())) continue;
                    found = true;
                    break;
                }
            }
            if (!found || !TriggerEffects.canShow(effect, creature, linked, incInactive, dontListMine, listMineOnly, listForUser)) continue;
            effs.add(effect);
        }
        return effs.toArray(new TriggerEffect[effs.size()]);
    }

    public static TriggerEffect[] getFilteredEffects(MissionTrigger[] trigs, Creature creature, int linked, boolean incInactive, boolean dontListMine, boolean listMineOnly, long listForUser, int missionId) {
        HashSet<TriggerEffect> effs = new HashSet<TriggerEffect>();
        for (TriggerEffect effect : effects.values()) {
            if (effect.getMissionId() != missionId) continue;
            boolean found = false;
            for (MissionTrigger trig : trigs) {
                if (!Triggers2Effects.hasLink(trig.getId(), effect.getId())) continue;
                found = true;
                break;
            }
            if (found || !TriggerEffects.canShow(effect, creature, linked, incInactive, dontListMine, listMineOnly, listForUser)) continue;
            effs.add(effect);
        }
        return effs.toArray(new TriggerEffect[effs.size()]);
    }

    public static TriggerEffect[] getFilteredEffects(Creature creature, int linked, boolean incInactive, boolean dontListMine, boolean listMineOnly, long listForUser) {
        HashSet<TriggerEffect> effs = new HashSet<TriggerEffect>();
        for (TriggerEffect effect : effects.values()) {
            if (!TriggerEffects.canShow(effect, creature, linked, incInactive, dontListMine, listMineOnly, listForUser)) continue;
            effs.add(effect);
        }
        return effs.toArray(new TriggerEffect[effs.size()]);
    }

    private static boolean canShow(TriggerEffect effect, Creature creature, int linked, boolean incInactive, boolean dontListMine, boolean listMineOnly, long listForUser) {
        boolean userMatch;
        boolean own = effect.getOwnerId() == creature.getWurmId();
        boolean show = creature.getPower() > 0 || own;
        boolean bl = userMatch = effect.getOwnerId() == listForUser;
        if (own) {
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
        if (effect.getCreatorType() == 2 && creature.getPower() < MissionManager.CAN_SEE_EPIC_MISSIONS) {
            show = false;
        }
        if (show) {
            switch (linked) {
                case 0: {
                    break;
                }
                case 1: {
                    show = effect.getMissionId() != 0;
                    break;
                }
                case 2: {
                    show = effect.getMissionId() == 0;
                }
            }
        }
        return show;
    }

    protected static boolean removeEffect(int id) {
        return effects.remove(id) != null;
    }

    public static TriggerEffect getTriggerEffect(int id) {
        return effects.get(id);
    }

    public static void destroyEffectsForTrigger(int triggerId) {
        TriggerEffect[] tes;
        for (TriggerEffect mt : tes = Triggers2Effects.getEffectsForTrigger(triggerId, true)) {
            MissionTrigger trig = MissionTriggers.getTriggerWithId(triggerId);
            if (trig != null && (mt.destroysTarget() || WurmId.getType(trig.getTarget()) == 1) && mt.getCreatorType() != 3) {
                mt.destroyTarget(trig.getTarget());
            }
            if (tes.length != 1) continue;
            TriggerEffects.removeEffect(mt.getId());
            mt.destroy();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void loadAllTriggerEffects() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOADALLEFFECTS);
            rs = ps.executeQuery();
            int mid = -10;
            while (rs.next()) {
                mid = rs.getInt("ID");
                TriggerEffect m = new TriggerEffect();
                m.setId(mid);
                m.setName(rs.getString("NAME"));
                m.setDescription(rs.getString("DESCRIPTION"));
                m.setRewardItem(rs.getInt("REWARDITEM"));
                m.setRewardNumbers(rs.getInt("REWARDITEMNUMBERS"));
                m.setRewardQl(rs.getInt("REWARDQUALITY"));
                m.setRewardByteValue(rs.getByte("REWARDBYTE"));
                m.setExistingItemReward(rs.getLong("EXISTINGREWARDITEMID"));
                m.setRewardTargetContainerId(rs.getLong("REWARDTARGETCONTAINERID"));
                m.setRewardSkillNum(rs.getInt("REWARDSKILLNUM"));
                m.setRewardSkillVal(rs.getFloat("REWARDSKILLVAL"));
                m.setSpecialEffect(rs.getInt("SPECIALEFFECTID"));
                int triggerId = rs.getInt("TRIGGERID");
                if (triggerId > 0) {
                    Triggers2Effects.addLink(triggerId, mid, false);
                }
                m.setSoundName(rs.getString("SOUND"));
                m.setTextDisplayed(rs.getString("TEXT"));
                m.setTopText(rs.getString("TOP"));
                m.setMission(rs.getInt("MISSION"));
                m.setMissionStateChange(rs.getFloat("MISSIONSTATECHANGE"));
                m.setInactive(rs.getBoolean("INACTIVE"));
                m.setLastModifierName(rs.getString("LASTMODIFIER"));
                m.setDestroysTarget(rs.getBoolean("DESTROYTARGET"));
                m.setCreatorName(rs.getString("CREATOR"));
                m.setCreatedDate(rs.getString("CREATEDDATE"));
                m.setLastModifierName(rs.getString("LASTMODIFIER"));
                Timestamp st = new Timestamp(System.currentTimeMillis());
                try {
                    st = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("LASTMODIFIEDDATE")).getTime());
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
                m.setLastModifiedDate(st);
                m.setCreatorType(rs.getByte("CREATORTYPE"));
                m.setOwnerId(rs.getLong("CREATORID"));
                m.setItemMaterial(rs.getByte("ITEMMATERIAL"));
                m.setNewbieItem(rs.getBoolean("NEWBIE"));
                m.setModifyTileX(rs.getInt("MODIFYTILEX"));
                m.setModifyTileY(rs.getInt("MODIFYTILEY"));
                m.setNewTileType(rs.getInt("NEWTILETYPE"));
                m.setNewTileData(rs.getByte("NEWTILEDATA"));
                m.setSpawnTileX(rs.getInt("SPAWNTILEX"));
                m.setSpawnTileY(rs.getInt("SPAWNTILEY"));
                m.setCreatureSpawn(rs.getInt("CREATURESPAWN"));
                m.setCreatureAge(rs.getInt("CREATUREAGE"));
                m.setCreatureType(rs.getByte("CREATURE_TYPE"));
                m.setCreatureName(rs.getString("CREATURE_NAME"));
                m.setTeleportX(rs.getInt("TELEPORTX"));
                m.setTeleportY(rs.getInt("TELEPORTY"));
                m.setTeleportLayer(rs.getInt("TELEPORTLAYER"));
                m.setMissionToActivate(rs.getInt("MISSIONACTIVATED"));
                m.setMissionToDeActivate(rs.getInt("MISSIONDEACTIVATED"));
                m.setWindowSizeX(rs.getInt("WSZX"));
                m.setWindowSizeY(rs.getInt("WSZY"));
                m.setStartSkillgain(rs.getBoolean("STARTSKILLGAIN"));
                m.setStopSkillgain(rs.getBoolean("STOPSKILLGAIN"));
                m.setDestroyInventory(rs.getBoolean("DESTROYITEMS"));
                TriggerEffects.addTriggerEffect(m);
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
            TriggerEffects.loadAllTriggerEffects();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problems loading all Trigger Effects", ex);
        }
    }
}


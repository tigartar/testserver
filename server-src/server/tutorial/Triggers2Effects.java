/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Triggers2Effects {
    private static Logger logger = Logger.getLogger(Triggers2Effects.class.getName());
    private static final String LOAD_ALL_LINKS = "SELECT * FROM TRIGGERS2EFFECTS";
    private static final String CREATE_LINK = "INSERT INTO TRIGGERS2EFFECTS (TRIGGERID, EFFECTID) VALUES(?,?)";
    private static final String DELETE_LINK = "DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=? AND EFFECTID=?";
    private static final String DELETE_TRIGGER = "DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=?";
    private static final String DELETE_EFFECT = "DELETE FROM TRIGGERS2EFFECTS WHERE EFFECTID=?";
    private static final Map<Integer, HashSet<Integer>> triggers2Effects = new ConcurrentHashMap<Integer, HashSet<Integer>>();
    private static final Map<Integer, HashSet<Integer>> effects2Triggers = new ConcurrentHashMap<Integer, HashSet<Integer>>();

    private Triggers2Effects() {
    }

    public static TriggerEffect[] getEffectsForTrigger(int triggerId, boolean incInactive) {
        HashSet<TriggerEffect> effs = new HashSet<TriggerEffect>();
        HashSet<Integer> effects = triggers2Effects.get(triggerId);
        if (effects != null) {
            for (Integer effectId : effects) {
                TriggerEffect eff = TriggerEffects.getTriggerEffect(effectId);
                if (eff == null || !incInactive && (incInactive || eff.isInactive())) continue;
                effs.add(eff);
            }
        }
        return effs.toArray(new TriggerEffect[effs.size()]);
    }

    public static MissionTrigger[] getTriggersForEffect(int effectId, boolean incInactive) {
        HashSet<MissionTrigger> trgs = new HashSet<MissionTrigger>();
        HashSet<Integer> triggers = effects2Triggers.get(effectId);
        if (triggers != null) {
            for (Integer triggerId : triggers) {
                MissionTrigger trg = MissionTriggers.getTriggerWithId(triggerId);
                if (trg == null || !incInactive && (incInactive || trg.isInactive())) continue;
                trgs.add(trg);
            }
        }
        return trgs.toArray(new MissionTrigger[trgs.size()]);
    }

    public static boolean hasLink(int triggerId, int effectId) {
        HashSet<Integer> effects = triggers2Effects.get(triggerId);
        if (effects != null) {
            return effects.contains(effectId);
        }
        return false;
    }

    public static boolean hasEffect(int triggerId) {
        HashSet<Integer> effects = triggers2Effects.get(triggerId);
        if (effects != null) {
            return !effects.isEmpty();
        }
        return false;
    }

    public static boolean hasTrigger(int effectId) {
        HashSet<Integer> triggers = effects2Triggers.get(effectId);
        if (triggers != null) {
            return !triggers.isEmpty();
        }
        return false;
    }

    public static void addLink(int triggerId, int effectId, boolean loading) {
        HashSet<Integer> triggers;
        if (triggerId <= 0 || effectId <= 0) {
            return;
        }
        HashSet<Integer> effects = triggers2Effects.get(triggerId);
        if (effects == null) {
            effects = new HashSet();
        }
        boolean effAdded = effects.add(effectId);
        if (!effects.isEmpty()) {
            triggers2Effects.put(triggerId, effects);
        }
        if ((triggers = effects2Triggers.get(effectId)) == null) {
            triggers = new HashSet();
        }
        boolean trgAdded = triggers.add(triggerId);
        if (!triggers.isEmpty()) {
            effects2Triggers.put(effectId, triggers);
        }
        if (!loading && (effAdded || trgAdded)) {
            Triggers2Effects.dbCreateLink(triggerId, effectId);
        }
    }

    public static void deleteLink(int triggerId, int effectId) {
        HashSet<Integer> triggers;
        HashSet<Integer> effects = triggers2Effects.remove(triggerId);
        if (effects != null) {
            effects.remove(effectId);
            if (!effects.isEmpty()) {
                triggers2Effects.put(triggerId, effects);
            }
        }
        if ((triggers = effects2Triggers.remove(effectId)) != null) {
            triggers.remove(triggerId);
            if (!triggers.isEmpty()) {
                effects2Triggers.put(effectId, triggers);
            }
        }
        Triggers2Effects.dbDeleteLink(triggerId, effectId);
    }

    public static void deleteTrigger(int triggerId) {
        HashSet<Integer> effects = triggers2Effects.remove(triggerId);
        if (effects != null) {
            for (Integer effectId : effects) {
                HashSet<Integer> triggers = effects2Triggers.remove(effectId);
                if (triggers == null) continue;
                triggers.remove(triggerId);
                if (triggers.isEmpty()) continue;
                effects2Triggers.put((int)effectId, triggers);
            }
        }
        Triggers2Effects.dbDeleteTrigger(triggerId);
    }

    public static void deleteEffect(int effectId) {
        HashSet<Integer> triggers = effects2Triggers.remove(effectId);
        if (triggers != null) {
            for (Integer triggerId : triggers) {
                HashSet<Integer> effects = effects2Triggers.remove(triggerId);
                if (effects == null) continue;
                effects.remove(effectId);
                if (effects.isEmpty()) continue;
                effects2Triggers.put(effectId, triggers);
            }
        }
        Triggers2Effects.dbDeleteEffect(effectId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbCreateLink(int triggerId, int effectId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(CREATE_LINK);
            ps.setInt(1, triggerId);
            ps.setInt(2, effectId);
            ps.executeUpdate();
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbDeleteLink(int triggerId, int effectId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_LINK);
            ps.setInt(1, triggerId);
            ps.setInt(2, effectId);
            ps.executeUpdate();
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbDeleteTrigger(int triggerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_TRIGGER);
            ps.setInt(1, triggerId);
            ps.executeUpdate();
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbDeleteEffect(int effectId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_EFFECT);
            ps.setInt(1, effectId);
            ps.executeUpdate();
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbLoadAllTriggers2Effects() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOAD_ALL_LINKS);
            rs = ps.executeQuery();
            while (rs.next()) {
                int triggerId = rs.getInt("TRIGGERID");
                int effectId = rs.getInt("EFFECTID");
                Triggers2Effects.addLink(triggerId, effectId, true);
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
            Triggers2Effects.dbLoadAllTriggers2Effects();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problems loading all Triggers 2 Effects", ex);
        }
    }
}


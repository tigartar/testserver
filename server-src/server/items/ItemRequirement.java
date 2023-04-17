/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemRequirement {
    private static final Logger logger = Logger.getLogger(ItemRequirement.class.getName());
    private static final String loadItemRequirements = "SELECT * FROM ITEMREQUIREMENTS";
    private static final String deleteItemRequirements = "DELETE FROM ITEMREQUIREMENTS WHERE WURMID=?";
    private static final String updateItemRequirements = "UPDATE ITEMREQUIREMENTS SET ITEMSDONE=? WHERE WURMID=? AND TEMPLATEID=?";
    private static final String createItemRequirements = "INSERT INTO ITEMREQUIREMENTS (ITEMSDONE, WURMID, TEMPLATEID) VALUES(?,?,?)";
    private final int templateId;
    private int numsDone;
    private static final Map<Long, Set<ItemRequirement>> requirements = new HashMap<Long, Set<ItemRequirement>>();
    private static boolean found = false;

    private ItemRequirement(int aItemTemplateId, int aNumbersDone) {
        this.templateId = aItemTemplateId;
        this.numsDone = aNumbersDone;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAllItemRequirements() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(loadItemRequirements);
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemRequirement.setRequirements(rs.getLong("WURMID"), rs.getInt("TEMPLATEID"), rs.getInt("ITEMSDONE"), false, false);
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed loading item reqs " + ex.getMessage(), ex);
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

    public static void setRequirements(long _wurmid, int _templateId, int _numsDone, boolean save, boolean create) {
        found = false;
        Set<ItemRequirement> doneset = requirements.get(_wurmid);
        if (doneset == null) {
            doneset = new HashSet<ItemRequirement>();
            requirements.put(_wurmid, doneset);
        }
        for (ItemRequirement next : doneset) {
            if (next.templateId != _templateId) continue;
            next.numsDone = _numsDone;
            found = true;
        }
        if (!found) {
            ItemRequirement newreq = new ItemRequirement(_templateId, _numsDone);
            doneset.add(newreq);
        }
        if (save) {
            ItemRequirement.updateDatabaseRequirements(_wurmid, _templateId, _numsDone, create);
        }
    }

    public final int getTemplateId() {
        return this.templateId;
    }

    public final int getNumsDone() {
        return this.numsDone;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void deleteRequirements(long _wurmid) {
        requirements.remove(_wurmid);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(deleteItemRequirements);
            ps.setLong(1, _wurmid);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to delete reqs " + _wurmid, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public static final Set<ItemRequirement> getRequirements(long wurmid) {
        return requirements.get(wurmid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void updateDatabaseRequirements(long _wurmid, int _templateId, int numsDone, boolean create) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = numsDone == 1 || create ? dbcon.prepareStatement(createItemRequirements) : dbcon.prepareStatement(updateItemRequirements);
            ps.setInt(1, numsDone);
            ps.setLong(2, _wurmid);
            ps.setInt(3, _templateId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update reqs " + _wurmid + ",tid=" + _templateId + ", nums=" + numsDone, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    static int getStateForRequirement(int _templateId, long _wurmId) {
        Set<ItemRequirement> doneSet = requirements.get(_wurmId);
        if (doneSet != null) {
            for (ItemRequirement next : doneSet) {
                if (next.templateId != _templateId) continue;
                return next.numsDone;
            }
        }
        return 0;
    }
}


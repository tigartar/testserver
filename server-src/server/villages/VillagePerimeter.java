/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class VillagePerimeter {
    private static final Logger logger = Logger.getLogger(VillagePerimeter.class.getName());
    private final int villageId;
    private static final String INSERT_PERIMETERVALUES = "INSERT INTO VILLAGEPERIMETERS(SETTINGS,ID) VALUES (?,?)";
    private static final String UPDATE_PERIMETERVALUES = "UPDATE VILLAGEPERIMETERS SET SETTINGS=? WHERE ID=?";
    private static final String DELETE_PERIMETERVALUES = "DELETE FROM VILLAGEPERIMETERS WHERE ID=?";
    private static final String INSERT_PERIMETERFRIEND = "INSERT INTO PERIMETERFRIENDS(ID,NAME) VALUES (?,?)";
    private static final String DELETE_PERIMETERFRIEND = "DELETE FROM PERIMETERFRIENDS WHERE NAME=? AND ID=?";
    private static final String DELETE_PERIMETERFRIENDVILLAGE = "DELETE FROM PERIMETERFRIENDS WHERE ID=?";
    private final Set<String> perimeterFriends = new HashSet<String>();
    private static final Map<Integer, VillagePerimeter> parmap = new HashMap<Integer, VillagePerimeter>();
    private long settings;
    private static final String[] emptyFriends = new String[0];

    VillagePerimeter(int aVillageId) {
        this.villageId = aVillageId;
    }

    static VillagePerimeter getPerimeter(int villageId) {
        return parmap.get(villageId);
    }

    static void removePerimeter(int villageId) {
        parmap.remove(villageId);
    }

    void create() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(INSERT_PERIMETERVALUES);
            ps.setLong(1, this.settings);
            ps.setInt(2, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    void update() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_PERIMETERVALUES);
            ps.setLong(1, this.settings);
            ps.setInt(2, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    void delete() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_PERIMETERVALUES);
            ps.setInt(1, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        this.deleteAllFriend();
    }

    void setAllowsFenceDestruction(boolean allow) throws IOException {
        boolean allowFenceDestruction;
        boolean bl = allowFenceDestruction = (this.settings & 1L) == 1L;
        if (allow != allowFenceDestruction) {
            this.settings = allow ? ++this.settings : --this.settings;
            this.update();
        }
    }

    void setAllowsRoadDestruction(boolean allow) throws IOException {
        boolean allowRoadDestruction;
        boolean bl = allowRoadDestruction = (this.settings >> 1 & 1L) == 1L;
        if (allow != allowRoadDestruction) {
            this.settings = allow ? (this.settings += 2L) : (this.settings -= 2L);
            this.update();
        }
    }

    void setAllowsFenceBuilding(boolean allow) throws IOException {
        boolean allowFenceBuilding;
        boolean bl = allowFenceBuilding = (this.settings >> 2 & 1L) == 1L;
        if (allow != allowFenceBuilding) {
            this.settings = allow ? (this.settings += 4L) : (this.settings -= 4L);
            this.update();
        }
    }

    void setAllowsRoadBuilding(boolean allow) throws IOException {
        boolean allowRoadBuilding;
        boolean bl = allowRoadBuilding = (this.settings >> 3 & 1L) == 1L;
        if (allow != allowRoadBuilding) {
            this.settings = allow ? (this.settings += 8L) : (this.settings -= 8L);
            this.update();
        }
    }

    void setAllowsBuildings(boolean allow) throws IOException {
        boolean allowBuildings;
        boolean bl = allowBuildings = (this.settings >> 4 & 1L) == 1L;
        if (allow != allowBuildings) {
            this.settings = allow ? (this.settings += 16L) : (this.settings -= 16L);
            this.update();
        }
    }

    void setAllowsPerimeterActionsForAllies(boolean allow) throws IOException {
        boolean allowPerimeterActionsForAllies;
        boolean bl = allowPerimeterActionsForAllies = (this.settings >> 5 & 1L) == 1L;
        if (allow != allowPerimeterActionsForAllies) {
            this.settings = allow ? (this.settings += 32L) : (this.settings -= 32L);
            this.update();
        }
    }

    boolean allowsFenceDestruction() {
        return (this.settings & 1L) == 1L;
    }

    boolean allowsRoadDestruction() {
        return (this.settings >> 1 & 1L) == 1L;
    }

    boolean allowsFenceBuilding() {
        return (this.settings >> 2 & 1L) == 1L;
    }

    boolean allowsRoadBuilding() {
        return (this.settings >> 3 & 1L) == 1L;
    }

    boolean allowsBuildings() {
        return (this.settings >> 4 & 1L) == 1L;
    }

    boolean allowsPerimeterActionsForAllies() {
        return (this.settings >> 5 & 1L) == 1L;
    }

    void deleteAllFriend() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_PERIMETERFRIENDVILLAGE);
            ps.setInt(1, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    boolean addFriend(String friendName) throws IOException {
        if (!this.perimeterFriends.contains(friendName)) {
            boolean bl;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(INSERT_PERIMETERFRIEND);
                ps.setInt(1, this.villageId);
                ps.setString(2, friendName);
                ps.executeUpdate();
                this.perimeterFriends.add(friendName);
                bl = true;
            }
            catch (SQLException sqx) {
                try {
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return bl;
        }
        return false;
    }

    boolean deleteFriend(String friendName) throws IOException {
        if (this.perimeterFriends.contains(friendName)) {
            boolean bl;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(DELETE_PERIMETERFRIEND);
                ps.setString(1, friendName);
                ps.setInt(2, this.villageId);
                ps.executeUpdate();
                this.perimeterFriends.remove(friendName);
                bl = true;
            }
            catch (SQLException sqx) {
                try {
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return bl;
        }
        return false;
    }

    boolean isFriend(String name) {
        return this.perimeterFriends.contains(name);
    }

    public String[] getFriends() {
        if (this.perimeterFriends.isEmpty()) {
            return emptyFriends;
        }
        return this.perimeterFriends.toArray(new String[this.perimeterFriends.size()]);
    }
}


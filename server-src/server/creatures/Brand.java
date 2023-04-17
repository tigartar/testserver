/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Brand
implements MiscConstants {
    private static Logger logger = Logger.getLogger(Brand.class.getName());
    private static final String INSERT_CREATURE_BRAND = "INSERT INTO BRANDS (OWNERID,LASTBRANDED,WURMID) VALUES (?,?,?)";
    private static final String DELETE_CREATURE_BRAND = "DELETE FROM BRANDS WHERE WURMID=?";
    private static final String UPDATE_CREATURE_BRAND = "UPDATE BRANDS SET OWNERID=?,LASTBRANDED=? WHERE WURMID=?";
    private static final String GET_CREATURE_BRANDS = "SELECT * FROM BRANDS";
    private final long creatureId;
    private final long timeBranded;
    private long brand;

    public Brand(long _creatureId, long _timeBranded, long _brand, boolean load) {
        this.creatureId = _creatureId;
        this.timeBranded = _timeBranded;
        this.brand = _brand;
        if (!load) {
            this.save(true);
            this.addInitialPermissions();
        } else {
            Creatures.getInstance().addBrand(this);
        }
    }

    protected final void setBrandId(long newId) {
        this.brand = newId;
        this.save(false);
        this.addInitialPermissions();
    }

    private final void addInitialPermissions() {
        if (Servers.isThisAPvpServer()) {
            return;
        }
        AnimalSettings.remove(this.creatureId);
        try {
            Creature creature = Creatures.getInstance().getCreature(this.creatureId);
            int value = AnimalSettings.Animal2Permissions.MANAGE.getValue() + AnimalSettings.Animal2Permissions.COMMANDER.getValue() + AnimalSettings.Animal2Permissions.ACCESS_HOLD.getValue();
            Vehicle vehicle = Vehicles.getVehicle(creature);
            if (vehicle != null && !vehicle.isUnmountable() && vehicle.getMaxPassengers() != 0) {
                value += AnimalSettings.Animal2Permissions.PASSENGER.getValue();
            }
            Village village = Villages.getVillage((int)this.brand);
            AnimalSettings.addPlayer(this.creatureId, -60L, value);
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
        }
        catch (NoSuchVillageException noSuchVillageException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final void save(boolean newBrand) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = newBrand ? dbcon.prepareStatement(INSERT_CREATURE_BRAND) : dbcon.prepareStatement(UPDATE_CREATURE_BRAND);
            ps.setLong(1, this.brand);
            ps.setLong(2, this.timeBranded);
            ps.setLong(3, this.creatureId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save brand " + this.creatureId, sqex);
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

    public static final void loadAllBrands() throws NoSuchCreatureException {
        logger.info("Loading Brands");
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(GET_CREATURE_BRANDS);
            rs = ps.executeQuery();
            while (rs.next()) {
                new Brand(rs.getLong("WURMID"), rs.getLong("LASTBRANDED"), rs.getLong("OWNERID"), true);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load brands:" + sqx.getMessage(), sqx);
                throw new NoSuchCreatureException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    public final long getBrandId() {
        return this.brand;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void deleteBrand() {
        block5: {
            if (Creatures.isLoading()) {
                return;
            }
            if (Creatures.getInstance().getBrand(this.creatureId) == null) break block5;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(DELETE_CREATURE_BRAND);
                ps.setLong(1, this.creatureId);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to delete brand " + this.creatureId, sqex);
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
            Creatures.getInstance().setBrand(this.creatureId, -1L);
            AnimalSettings.remove(this.creatureId);
            PermissionsHistories.remove(this.creatureId);
        }
    }

    public long getCreatureId() {
        return this.creatureId;
    }

    long getBrand() {
        return this.brand;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.BodyFactory;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbCreatureStatus
extends CreatureStatus
implements CounterTypes {
    private static final Logger logger = Logger.getLogger(DbCreatureStatus.class.getName());
    private static final String getPlayerStatus = "select * from PLAYERS where WURMID=?";
    private static final String getCreatureStatus = "select * from CREATURES where WURMID=?";
    private static final String savePlayerStatus = "update PLAYERS set TEMPLATENAME=?, SEX=?, CENTIMETERSHIGH=?, CENTIMETERSLONG=?, CENTIMETERSWIDE=?, INVENTORYID=?, BODYID=?, BUILDINGID=?, HUNGER=?, THIRST=?, STAMINA=?,KINGDOM=?,FAT=?,STEALTH=?,DETECTIONSECS=?,TRAITS=?,NUTRITION=?,CALORIES=?,CARBS=?,FATS=?,PROTEINS=? where WURMID=?";
    private static final String saveCreatureStatus = "update CREATURES set NAME=?, TEMPLATENAME=?,SEX=?, CENTIMETERSHIGH=?, CENTIMETERSLONG=?, CENTIMETERSWIDE=?, INVENTORYID=?, BODYID=?, BUILDINGID=?, HUNGER=?, THIRST=?, STAMINA=?,KINGDOM=?,FAT=?,STEALTH=?,DETECTIONSECS=?,TRAITS=?,NUTRITION=?,PETNAME=?,AGE=? where WURMID=?";
    private static final String createPlayerStatus = "insert into PLAYERS (TEMPLATENAME, SEX,CENTIMETERSHIGH, CENTIMETERSLONG, CENTIMETERSWIDE, INVENTORYID,BODYID,BUILDINGID,HUNGER, THIRST, STAMINA,KINGDOM,FAT,STEALTH,DETECTIONSECS,TRAITS,NUTRITION,CALORIES,CARBS,FATS,PROTEINS,WURMID ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String createCreatureStatus = "insert into CREATURES (NAME, TEMPLATENAME, SEX,CENTIMETERSHIGH, CENTIMETERSLONG, CENTIMETERSWIDE, INVENTORYID,BODYID, BUILDINGID,HUNGER,THIRST, STAMINA,KINGDOM,FAT,STEALTH,DETECTIONSECS,TRAITS,NUTRITION,PETNAME, AGE, WURMID) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String SET_PLAYER_KINGDOM = "update PLAYERS set KINGDOM=? WHERE WURMID=?";
    private static final String SET_PLAYER_INVENTORYID = "update PLAYERS set INVENTORYID=? WHERE WURMID=?";
    private static final String SET_CREATURE_INVENTORYID = "update CREATURES set INVENTORYID=? WHERE WURMID=?";
    private static final String SET_CREATURE_KINGDOM = "update CREATURES set KINGDOM=? WHERE WURMID=?";
    private static final String SET_DEAD_CREATURE = "update CREATURES set DEAD=? WHERE WURMID=?";
    private static final String SET_DEAD_PLAYER = "update PLAYERS set DEAD=? WHERE WURMID=?";
    private static final String SET_AGE_CREATURE = "update CREATURES set AGE=?,LASTPOLLEDAGE=? WHERE WURMID=?";
    private static final String SET_AGE_PLAYER = "update PLAYERS set AGE=?,LASTPOLLEDAGE=? WHERE WURMID=?";
    private static final String SET_FAT_CREATURE = "update CREATURES set FAT=? WHERE WURMID=?";
    private static final String SET_FAT_PLAYER = "update PLAYERS set FAT=? WHERE WURMID=?";
    private static final String SET_DOMINATOR = "update CREATURES set DOMINATOR=? WHERE WURMID=?";
    private static final String SET_REBORN = "update CREATURES set REBORN=? WHERE WURMID=?";
    private static final String SET_LOYALTY = "update CREATURES set LOYALTY=? WHERE WURMID=?";
    private static final String SET_OFFLINE = "update CREATURES set OFFLINE=? WHERE WURMID=?";
    private static final String SET_STAYONLINE = "update CREATURES set STAYONLINE=? WHERE WURMID=?";
    private static final String SET_CREATURE_TYPE = "update CREATURES set TYPE=? WHERE WURMID=?";
    private static final String SET_PLAYER_TYPE = "update PLAYERS set TYPE=? WHERE WURMID=?";
    private static final String SET_CREATURE_NAME = "update CREATURES set NAME=? WHERE WURMID=?";
    private static final String SET_CREATURE_INHERITANCE = "update CREATURES set TRAITS=?,MOTHER=?,FATHER=? WHERE WURMID=?";
    private static final String SET_PLAYER_INHERITANCE = "update PLAYERS set TRAITS=?,MOTHER=?,FATHER=? WHERE WURMID=?";
    private static final String SET_LASTPOLLEDLOYALTY = "update CREATURES set LASTPOLLEDLOYALTY=? WHERE WURMID=?";
    private static final String SET_PLDETECTIONSECS = "update PLAYERS set DETECTIONSECS=? WHERE WURMID=?";
    private static final String SET_LASTGROOMED = "update CREATURES set LASTGROOMED=? WHERE WURMID=?";
    private static final String SET_CDISEASE = "update CREATURES set DISEASE=? WHERE WURMID=?";
    private static final String SET_VEHICLE = "update CREATURES set VEHICLE=?,SEAT_TYPE=? WHERE WURMID=?";
    private static final String SET_PDISEASE = "update PLAYERS set DISEASE=? WHERE WURMID=?";
    private static final String ISLOADED = "update CREATURES set ISLOADED=? WHERE WURMID=?";
    private static final String SET_NEWAGE = "update CREATURES set AGE=? WHERE WURMID=?";

    public DbCreatureStatus(Creature aCreature, float posx, float posy, float aRot, int aLayer) throws Exception {
        super(aCreature, posx, posy, aRot, aLayer);
    }

    @Override
    public boolean save() throws IOException {
        float lElapsedTime;
        long now = System.nanoTime();
        long id = this.statusHolder.getWurmId();
        this.inventoryId = this.statusHolder.getInventory().getWurmId();
        if (this.bodyId <= 0L) {
            this.bodyId = this.body.getId();
        }
        Connection dbcon = null;
        boolean toReturn = true;
        try {
            if (WurmId.getType(this.statusHolder.getWurmId()) == 0) {
                dbcon = DbConnector.getPlayerDbCon();
                toReturn = this.savePlayerStatus(id, dbcon);
            } else {
                dbcon = DbConnector.getCreatureDbCon();
                toReturn = this.saveCreatureStatus(id, dbcon);
            }
            dbcon = null;
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save status for creature with id " + id, sqex);
                throw new IOException("Failed to save status for creature with id " + id);
            }
            catch (Throwable throwable) {
                float lElapsedTime2;
                dbcon = null;
                if (logger.isLoggable(Level.FINER) && ((lElapsedTime2 = (float)(System.nanoTime() - now) / 1000000.0f) > 10.0f || logger.isLoggable(Level.FINEST) && lElapsedTime2 > 1.0f)) {
                    logger.finer("Saving Status for " + (WurmId.getType(this.statusHolder.getWurmId()) == 0 ? " player id, " : " Creature id, ") + this.statusHolder.getWurmId() + "," + this.statusHolder.getName() + ", which took " + lElapsedTime2 + " millis.");
                }
                throw throwable;
            }
        }
        if (logger.isLoggable(Level.FINER) && ((lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f) > 10.0f || logger.isLoggable(Level.FINEST) && lElapsedTime > 1.0f)) {
            logger.finer("Saving Status for " + (WurmId.getType(this.statusHolder.getWurmId()) == 0 ? " player id, " : " Creature id, ") + this.statusHolder.getWurmId() + "," + this.statusHolder.getName() + ", which took " + lElapsedTime + " millis.");
        }
        return toReturn;
    }

    private boolean saveCreatureStatus(long id, Connection dbcon) throws SQLException {
        if (this.isChanged()) {
            PreparedStatement ps;
            if (this.isStatusExists()) {
                ps = dbcon.prepareStatement(saveCreatureStatus);
            } else {
                ps = dbcon.prepareStatement(createCreatureStatus);
                this.setStatusExists(true);
            }
            ps.setString(1, this.statusHolder.name);
            ps.setString(2, this.template.getName());
            ps.setByte(3, this.sex);
            if (this.body.getCentimetersHigh() == 0) {
                this.body.setCentimetersHigh(this.template.getCentimetersHigh());
            }
            ps.setShort(4, this.body.getCentimetersHigh());
            if (this.body.getCentimetersLong() == 0) {
                this.body.setCentimetersLong(this.template.getCentimetersLong());
            }
            ps.setShort(5, this.body.getCentimetersLong());
            if (this.body.getCentimetersWide() == 0) {
                this.body.setCentimetersWide(this.template.getCentimetersWide());
            }
            ps.setShort(6, this.body.getCentimetersWide());
            ps.setLong(7, this.inventoryId);
            ps.setLong(8, this.bodyId);
            ps.setLong(9, this.buildingId);
            ps.setShort(10, (short)this.hunger);
            ps.setShort(11, (short)this.thirst);
            ps.setShort(12, (short)this.stamina);
            ps.setByte(13, this.kingdom);
            ps.setInt(14, this.fat);
            ps.setBoolean(15, this.stealth);
            ps.setShort(16, (short)this.detectInvisCounter);
            ps.setLong(17, this.getTraitBits());
            ps.setFloat(18, this.nutrition);
            ps.setString(19, this.statusHolder.petName);
            ps.setShort(20, (short)this.age);
            ps.setLong(21, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            this.setChanged(false);
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean savePlayerStatus(long id, Connection dbcon) throws SQLException {
        PreparedStatement ps = null;
        try {
            if (this.isStatusExists()) {
                ps = dbcon.prepareStatement(savePlayerStatus);
            } else {
                logger.log(Level.INFO, "Creating new status");
                ps = dbcon.prepareStatement(createPlayerStatus);
                this.stamina = 65535;
                this.setStatusExists(true);
            }
            ps.setString(1, this.template.getName());
            ps.setByte(2, this.sex);
            ps.setShort(3, this.body.getCentimetersHigh());
            ps.setShort(4, this.body.getCentimetersLong());
            ps.setShort(5, this.body.getCentimetersWide());
            ps.setLong(6, this.inventoryId);
            ps.setLong(7, this.bodyId);
            ps.setLong(8, this.buildingId);
            ps.setShort(9, (short)this.hunger);
            ps.setShort(10, (short)this.thirst);
            ps.setShort(11, (short)this.stamina);
            ps.setByte(12, this.kingdom);
            ps.setByte(13, this.fat);
            ps.setBoolean(14, this.stealth);
            ps.setShort(15, (short)this.detectInvisCounter);
            ps.setLong(16, this.getTraitBits());
            ps.setFloat(17, this.nutrition);
            ps.setFloat(18, this.calories);
            ps.setFloat(19, this.carbs);
            ps.setFloat(20, this.fats);
            ps.setFloat(21, this.proteins);
            ps.setLong(22, id);
            ps.executeUpdate();
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, null);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        return true;
    }

    @Override
    public void load() throws Exception {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block20: {
            long id = this.statusHolder.getWurmId();
            dbcon = null;
            String loadString = getCreatureStatus;
            this.setPosition(CreaturePos.getPosition(id));
            ps = null;
            rs = null;
            try {
                if (WurmId.getType(id) == 0) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Loading a player - id:" + id);
                    }
                    loadString = getPlayerStatus;
                    dbcon = DbConnector.getPlayerDbCon();
                } else {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Loading a creature - id:" + id);
                    }
                    dbcon = DbConnector.getCreatureDbCon();
                }
                ps = dbcon.prepareStatement(loadString);
                ps.setLong(1, id);
                rs = ps.executeQuery();
                if (!rs.next()) break block20;
                String templateName = rs.getString("TEMPLATENAME");
                this.statusHolder.template = this.template = CreatureTemplateFactory.getInstance().getTemplate(templateName);
                this.bodyId = rs.getLong("BODYID");
                this.body = BodyFactory.getBody(this.statusHolder, this.template.getBodyType(), this.template.getCentimetersHigh(), this.template.getCentimetersLong(), this.template.getCentimetersWide());
                this.body.setCentimetersLong(rs.getShort("CENTIMETERSLONG"));
                this.body.setCentimetersHigh(rs.getShort("CENTIMETERSHIGH"));
                this.body.setCentimetersWide(rs.getShort("CENTIMETERSWIDE"));
                this.sex = rs.getByte("SEX");
                this.modtype = rs.getByte("TYPE");
                String name = rs.getString("NAME");
                this.statusHolder.setName(name);
                this.inventoryId = rs.getLong("INVENTORYID");
                this.stamina = rs.getShort("STAMINA") & 0xFFFF;
                this.hunger = rs.getShort("HUNGER") & 0xFFFF;
                this.thirst = rs.getShort("THIRST") & 0xFFFF;
                this.buildingId = rs.getLong("BUILDINGID");
                this.kingdom = rs.getByte("KINGDOM");
                this.dead = rs.getBoolean("DEAD");
                this.stealth = rs.getBoolean("STEALTH");
                this.age = rs.getInt("AGE");
                this.fat = rs.getByte("FAT");
                this.lastPolledAge = rs.getLong("LASTPOLLEDAGE");
                this.statusHolder.dominator = rs.getLong("DOMINATOR");
                this.reborn = rs.getBoolean("REBORN");
                this.loyalty = rs.getFloat("LOYALTY");
                this.lastPolledLoyalty = rs.getLong("LASTPOLLEDLOYALTY");
                this.detectInvisCounter = rs.getShort("DETECTIONSECS");
                this.traits = rs.getLong("TRAITS");
                if (this.traits != 0L) {
                    this.setTraitBits(this.traits);
                }
                this.mother = rs.getLong("MOTHER");
                this.father = rs.getLong("FATHER");
                this.nutrition = rs.getFloat("NUTRITION");
                this.disease = rs.getByte("DISEASE");
                if (WurmId.getType(id) == 0) {
                    this.calories = rs.getFloat("CALORIES");
                    this.carbs = rs.getFloat("CARBS");
                    this.fats = rs.getFloat("FATS");
                    this.proteins = rs.getFloat("PROTEINS");
                }
                if (this.buildingId != -10L) {
                    try {
                        Structure struct = Structures.getStructure(this.buildingId);
                        if (!struct.isFinalFinished()) {
                            this.statusHolder.setStructure(struct);
                        } else {
                            this.buildingId = -10L;
                        }
                    }
                    catch (NoSuchStructureException nss) {
                        this.buildingId = -10L;
                        logger.log(Level.INFO, "Could not find structure for " + this.statusHolder.getName());
                        this.statusHolder.setStructure(null);
                    }
                }
                if (WurmId.getType(id) == 1) {
                    this.lastGroomed = rs.getLong("LASTGROOMED");
                    this.offline = rs.getBoolean("OFFLINE");
                    this.stayOnline = rs.getBoolean("STAYONLINE");
                }
                this.statusHolder.calculateSize();
                long hitchedTo = rs.getLong("VEHICLE");
                if (hitchedTo > 0L) {
                    try {
                        Item vehicle = Items.getItem(hitchedTo);
                        Vehicle vehic = Vehicles.getVehicle(vehicle);
                        if (vehic.addDragger(this.statusHolder)) {
                            this.statusHolder.setHitched(vehic, true);
                            Seat driverseat = vehic.getPilotSeat();
                            float _r = (-vehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                            float _s = (float)Math.sin(_r);
                            float _c = (float)Math.cos(_r);
                            float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                            float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                            float nPosX = this.getPositionX() - xo;
                            float nPosY = this.getPositionY() - yo;
                            float nPosZ = this.getPositionZ() - driverseat.offz;
                            this.setPositionX(nPosX);
                            this.setPositionY(nPosY);
                            this.setRotation(-vehicle.getRotation() + 180.0f);
                            this.statusHolder.getMovementScheme().setPosition(this.getPositionX(), this.getPositionY(), nPosZ, this.getRotation(), this.statusHolder.getLayer());
                        }
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.INFO, "Item " + hitchedTo + " missing for hitched " + id + " " + name);
                    }
                }
                this.setStatusExists(true);
            }
            catch (Exception sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to load status for creature with id " + id, sqex);
                    throw new IOException("Failed to load status for creature with id " + id);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long getInventoryIdFor(long creatureId) {
        long toReturn;
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block6: {
            dbcon = null;
            ps = null;
            rs = null;
            toReturn = -10L;
            try {
                String selectString = getPlayerStatus;
                if (WurmId.getType(creatureId) == 1) {
                    selectString = getCreatureStatus;
                    dbcon = DbConnector.getCreatureDbCon();
                } else {
                    dbcon = DbConnector.getPlayerDbCon();
                }
                ps = dbcon.prepareStatement(selectString);
                ps.setLong(1, creatureId);
                rs = ps.executeQuery();
                if (!rs.next()) break block6;
                toReturn = rs.getLong("INVENTORYID");
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Creature has no inventoryitem?" + creatureId, sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return toReturn;
    }

    @Override
    public void savePosition(long id, boolean player, int zoneid, boolean immediately) throws IOException {
        float lElapsedTime;
        long now;
        block9: {
            if (this.getPosition() == null) {
                logger.log(Level.WARNING, "Position is null for " + id + " at ", new Exception());
                return;
            }
            now = System.nanoTime();
            try {
                if (player) {
                    this.getPosition().savePlayerPosition(zoneid, immediately);
                    break block9;
                }
                this.getPosition().saveCreaturePosition(zoneid, immediately);
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to save status for creature/player with id " + id, sqex);
                    if (Server.getMillisToShutDown() == Long.MIN_VALUE) {
                        Server.getInstance().startShutdown(5, "The server lost connection to the database. Shutting down ");
                    }
                    throw new IOException("Failed to save status for creature/player with id " + id, sqex);
                }
                catch (Throwable throwable) {
                    float lElapsedTime2;
                    if (logger.isLoggable(Level.FINER) && ((lElapsedTime2 = (float)(System.nanoTime() - now) / 1000000.0f) > 10.0f || logger.isLoggable(Level.FINEST) && lElapsedTime2 > 1.0f)) {
                        logger.finer("Saving Position for " + (player ? " player id, " : " Creature id, ") + this.statusHolder.getWurmId() + "," + this.statusHolder.getName() + ", which took " + lElapsedTime2 + " millis.");
                    }
                    throw throwable;
                }
            }
        }
        if (logger.isLoggable(Level.FINER) && ((lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f) > 10.0f || logger.isLoggable(Level.FINEST) && lElapsedTime > 1.0f)) {
            logger.finer("Saving Position for " + (player ? " player id, " : " Creature id, ") + this.statusHolder.getWurmId() + "," + this.statusHolder.getName() + ", which took " + lElapsedTime + " millis.");
        }
    }

    @Override
    public void setKingdom(byte kingd) throws IOException {
        boolean send;
        boolean bl = send = this.kingdom != 0;
        if (this.kingdom != kingd && this.statusHolder.isPlayer()) {
            Kingdoms.getKingdom(this.kingdom).removeMember(this.statusHolder.getWurmId());
        }
        this.kingdom = kingd;
        if (this.statusHolder.isPlayer() && this.statusHolder.getPower() == 0) {
            Kingdoms.getKingdom(this.kingdom).addMember(this.statusHolder.getWurmId());
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            if (WurmId.getType(this.statusHolder.getWurmId()) == 0) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PLAYER_KINGDOM);
            } else {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_CREATURE_KINGDOM);
            }
            ps.setByte(1, this.kingdom);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
            if (send) {
                this.statusHolder.sendAttitudeChange();
                this.statusHolder.refreshVisible();
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
                throw new IOException("Failed to set kingdom for " + this.statusHolder.getWurmId() + " to " + Kingdoms.getNameFor(kingd) + ".");
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        if (this.statusHolder.isPlayer()) {
            Players.getInstance().registerNewKingdom(this.statusHolder.getKingdomId(), this.kingdom);
        }
    }

    @Override
    public void setInventoryId(long newInventoryId) throws IOException {
        this.inventoryId = newInventoryId;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (WurmId.getType(this.statusHolder.getWurmId()) == 0) {
                ps = dbcon.prepareStatement(SET_PLAYER_INVENTORYID);
            } else {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_CREATURE_INVENTORYID);
            }
            ps.setLong(1, this.inventoryId);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
                throw new IOException("Failed to set inventory id for " + this.statusHolder.getWurmId() + " to " + this.inventoryId + ".");
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

    @Override
    public void setDead(boolean isdead) throws IOException {
        this.dead = isdead;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            if (this.statusHolder.isPlayer()) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_DEAD_PLAYER);
            } else {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_DEAD_CREATURE);
            }
            ps.setBoolean(1, this.dead);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
                throw new IOException("Failed to set dead to " + isdead + " for " + this.statusHolder.getWurmId() + '.');
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

    @Override
    public void updateAge(int newAge) throws IOException {
        this.age = newAge;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.lastPolledAge = WurmCalendar.currentTime;
            if (this.statusHolder.isPlayer()) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_AGE_PLAYER);
            } else {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_AGE_CREATURE);
            }
            ps.setShort(1, (short)this.age);
            ps.setLong(2, this.lastPolledAge);
            ps.setLong(3, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Problem setting age of creature ID " + this.statusHolder.getWurmId() + " to " + this.age + ", lastPolledAge: " + this.lastPolledAge + " " + sqex.getMessage(), sqex);
                throw new IOException("Failed to set age to " + this.age + " for " + this.statusHolder.getWurmId() + '.');
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

    @Override
    public void updateFat() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            if (this.statusHolder.isPlayer()) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_FAT_PLAYER);
            } else {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_FAT_CREATURE);
            }
            ps.setByte(1, this.fat);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set fat to " + this.fat + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
                throw new IOException("Failed to set fat to " + this.fat + " for " + this.statusHolder.getWurmId() + '.');
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDominator(long dominator) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_DOMINATOR);
            ps.setLong(1, dominator);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set dominator to " + dominator + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setReborn(boolean reb) {
        this.reborn = reb;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_REBORN);
            ps.setBoolean(1, this.reborn);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set reborn to " + this.reborn + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLoyalty(float _loyalty) {
        _loyalty = Math.min(100.0f, _loyalty);
        if ((_loyalty = Math.max(0.0f, _loyalty)) != this.loyalty) {
            this.loyalty = _loyalty;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_LOYALTY);
                ps.setFloat(1, this.loyalty);
                ps.setLong(2, this.statusHolder.getWurmId());
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to set loyalty to " + this.loyalty + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastPolledLoyalty() {
        this.lastPolledLoyalty = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_LASTPOLLEDLOYALTY);
            ps.setLong(1, this.lastPolledLoyalty);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set lastPolledLoyalty to " + this.lastPolledLoyalty + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDetectionSecs() {
        if (this.statusHolder.isPlayer()) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PLDETECTIONSECS);
                ps.setShort(1, (short)this.detectInvisCounter);
                ps.setLong(2, this.statusHolder.getWurmId());
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to set detectInvisCounter to " + this.detectInvisCounter + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOffline(boolean _offline) {
        this.offline = _offline;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_OFFLINE);
            ps.setBoolean(1, this.offline);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set offline to " + this.offline + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setStayOnline(boolean _stayOnline) {
        this.stayOnline = _stayOnline;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_STAYONLINE);
            ps.setBoolean(1, this.stayOnline);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set stayOnline to " + this.stayOnline + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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
        return this.stayOnline;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setType(byte newtype) {
        this.modtype = newtype;
        if (this.modtype == 11) {
            this.disease = 1;
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            if (this.statusHolder.isPlayer()) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PLAYER_TYPE);
            } else {
                ps = dbcon.prepareStatement(SET_CREATURE_TYPE);
            }
            ps.setByte(1, this.modtype);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set type to " + this.modtype + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void setInheritance(long _traits, long _mother, long _father) throws IOException {
        this.traits = _traits;
        this.mother = _mother;
        this.father = _father;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            if (this.statusHolder.isPlayer()) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PLAYER_INHERITANCE);
            } else {
                ps = dbcon.prepareStatement(SET_CREATURE_INHERITANCE);
            }
            ps.setLong(1, this.traits);
            ps.setLong(2, this.mother);
            ps.setLong(3, this.father);
            ps.setLong(4, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set type to " + this.modtype + " for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void saveCreatureName(String name) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_CREATURE_NAME);
            ps.setString(1, name);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save name for " + this.statusHolder.getName() + " to " + name + " ," + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastGroomed(long _lastGroomed) {
        this.lastGroomed = _lastGroomed;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(SET_LASTGROOMED);
            ps.setLong(1, this.lastGroomed);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set lastgroomed for " + this.statusHolder.getName() + " ," + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void setDisease(byte _disease) {
        this.disease = _disease;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            if (this.statusHolder.isPlayer()) {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PDISEASE);
            } else {
                ps = dbcon.prepareStatement(SET_CDISEASE);
            }
            ps.setByte(1, this.disease);
            ps.setLong(2, this.statusHolder.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set disease for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setVehicle(long vehicleId, byte seatType) {
        if (!this.statusHolder.isPlayer()) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(SET_VEHICLE);
                ps.setLong(1, vehicleId);
                ps.setByte(2, seatType);
                ps.setLong(3, this.statusHolder.getWurmId());
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to set hitched to for " + this.statusHolder.getWurmId() + " " + sqex.getMessage(), sqex);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setLoaded(int loadstate, long cretID) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(ISLOADED);
            ps.setInt(1, loadstate);
            ps.setLong(2, cretID);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to set loadstate to for " + cretID + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getIsLoaded(long cretID) {
        Statement stmt = null;
        ResultSet rs = null;
        int isLoaded = 0;
        try {
            Connection dbcon = DbConnector.getCreatureDbCon();
            stmt = dbcon.createStatement();
            rs = stmt.executeQuery("select * from CREATURES where WURMID=" + cretID + "");
            if (rs.next()) {
                isLoaded = rs.getInt("ISLOADED");
            }
            DbUtilities.closeDatabaseObjects(stmt, rs);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        finally {
            DbUtilities.closeDatabaseObjects(stmt, rs);
        }
        return isLoaded;
    }
}


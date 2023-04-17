/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MapAnnotation {
    private static final Logger logger = Logger.getLogger(MapAnnotation.class.getName());
    public static final byte PRIVATE_POI = 0;
    public static final byte VILLAGE_POI = 1;
    public static final byte ALLIANCE_POI = 2;
    public static final short MAX_PRIVATE_ANNOTATIONS = 500;
    public static final short MAX_VILLAGE_ANNOTATIONS = 500;
    public static final short MAX_ALLIANCE_ANNOTATIONS = 500;
    private long id;
    private String name;
    private byte type;
    private long position;
    private long ownerId;
    private String server;
    private byte icon;
    private static final String CREATE_NEW_POI = "INSERT INTO MAP_ANNOTATIONS (ID, NAME, POSITION, POITYPE, OWNERID, SERVER, ICON) VALUES ( ?, ?, ?, ?, ?, ?, ? );";
    private static final String DELETE_POI = "DELETE FROM MAP_ANNOTATIONS WHERE ID=?;";
    private static final String DELETE_ALL_PRIVATE_ANNOTATIONS_BY_OWNER = "DELETE FROM MAP_ANNOTATIONS WHERE OWNERID=? AND POITYPE=0;";

    public static final MapAnnotation createNew(long id, String _name, byte _type, long _position, long _ownerId, String _server, byte _icon) throws IOException {
        MapAnnotation mapAnnotation;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            MapAnnotation poi;
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(CREATE_NEW_POI);
            ps.setLong(1, id);
            ps.setString(2, _name);
            ps.setLong(3, _position);
            ps.setByte(4, _type);
            ps.setLong(5, _ownerId);
            ps.setString(6, _server);
            ps.setByte(7, _icon);
            ps.executeUpdate();
            mapAnnotation = poi = new MapAnnotation(id, _name, _type, _position, _ownerId, _server, _icon);
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create POI: " + _name + ": " + sqx.getMessage(), sqx);
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
        return mapAnnotation;
    }

    public static final MapAnnotation createNew(String _name, byte _type, long _position, long _ownerId, String _server, byte _icon) throws IOException {
        MapAnnotation mapAnnotation;
        long id = WurmId.getNextPoiId();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            MapAnnotation poi;
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(CREATE_NEW_POI);
            ps.setLong(1, id);
            ps.setString(2, _name);
            ps.setLong(3, _position);
            ps.setByte(4, _type);
            ps.setLong(5, _ownerId);
            ps.setString(6, _server);
            ps.setByte(7, _icon);
            ps.executeUpdate();
            mapAnnotation = poi = new MapAnnotation(id, _name, _type, _position, _ownerId, _server, _icon);
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create POI: " + _name + ": " + sqx.getMessage(), sqx);
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
        return mapAnnotation;
    }

    public static void deleteAnnotation(long id) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_POI);
            ps.setLong(1, id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete POI: " + id + " :" + sqx.getMessage(), sqx);
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

    public static void deletePrivateAnnotationsForOwner(long ownerId) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_ALL_PRIVATE_ANNOTATIONS_BY_OWNER);
            ps.setLong(1, ownerId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete POI's for owner: " + ownerId + " :" + sqx.getMessage(), sqx);
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

    public MapAnnotation(long _id) {
        this.id = _id;
    }

    public MapAnnotation(long _id, String _name, byte _type, long _position, long _ownerId, String _server, byte _icon) {
        this.id = _id;
        this.name = _name;
        this.type = _type;
        this.position = _position;
        this.ownerId = _ownerId;
        this.server = _server;
        this.icon = _icon;
    }

    public final byte getIcon() {
        return this.icon;
    }

    public final long getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public final long getOwnerId() {
        return this.ownerId;
    }

    public final long getPosition() {
        return this.position;
    }

    public final String getServer() {
        return this.server;
    }

    public final byte getType() {
        return this.type;
    }

    public final int getXPos() {
        return BigInteger.valueOf(this.position).shiftRight(32).intValue();
    }

    public final int getYPos() {
        return BigInteger.valueOf(this.position).intValue();
    }

    public void setIcon(byte _icon) {
        this.icon = _icon;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public void setOwnerId(long _ownerId) {
        this.ownerId = _ownerId;
    }

    public void setPosition(int x, int y) {
        long pos = x;
        this.position = BigInteger.valueOf(pos).shiftLeft(32).longValue() + (long)y;
    }

    public void setPosition(long _position) {
        this.position = _position;
    }

    public void setServer(String _server) {
        this.server = _server;
    }

    public void setType(byte _type) {
        this.type = _type;
    }
}


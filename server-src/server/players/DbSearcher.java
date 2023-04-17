/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DbSearcher {
    private static final String getPlayerName = "select * from PLAYERS where WURMID=?";
    private static final String getPlayerId = "select * from PLAYERS where NAME=?";

    private DbSearcher() {
    }

    public static String getNameForPlayer(long wurmId) throws IOException, NoSuchPlayerException {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            String name;
            dbcon = null;
            ps = null;
            rs = null;
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getPlayerName);
            ps.setLong(1, wurmId);
            rs = ps.executeQuery();
            if (!rs.next()) break block5;
            String string = name = rs.getString("NAME");
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return string;
        }
        try {
            try {
                throw new NoSuchPlayerException("No player with id " + wurmId);
            }
            catch (SQLException sqx) {
                throw new IOException("Problem finding Player ID " + wurmId, sqx);
            }
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            throw throwable;
        }
    }

    public static long getWurmIdForPlayer(String name) throws IOException, NoSuchPlayerException {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            long id;
            dbcon = null;
            ps = null;
            rs = null;
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getPlayerId);
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (!rs.next()) break block5;
            long l = id = rs.getLong("WURMID");
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return l;
        }
        try {
            try {
                throw new NoSuchPlayerException("No player with name " + name);
            }
            catch (SQLException sqx) {
                throw new IOException("Problem finding Player name " + name, sqx);
            }
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            throw throwable;
        }
    }
}


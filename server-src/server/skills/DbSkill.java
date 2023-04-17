/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

class DbSkill
extends Skill {
    private static Logger logger = Logger.getLogger(DbSkill.class.getName());
    private static final String createCreatureSkill = "insert into SKILLS (VALUE, LASTUSED, MINVALUE, NUMBER, OWNER,ID) values(?,?,?,?,?,?)";
    private static final String updateCreatureSkill = "update SKILLS set VALUE=?, LASTUSED=?, MINVALUE=? where ID=?";
    private static final String updateNumber = "update SKILLS set NUMBER=? where ID=?";
    private static final String setJoat = "update SKILLS set JOAT=? where ID=?";
    private static final String getCreatureSkill = "select * from SKILLS where ID=?";
    private static final String createSkillChance = "insert into SKILLCHANCES (SKILL,DIFFICULTY,CHANCE) values(?,?,?)";
    private static final String loadSkillChance = "select * from SKILLCHANCES";

    DbSkill(int aNumber, double aStartValue, Skills aParent) {
        super(aNumber, aStartValue, aParent);
    }

    DbSkill(long aId, Skills aParent) throws IOException {
        super(aId, aParent);
    }

    DbSkill(long aId, Skills aParent, int aNumber, double aKnowledge, double aMinimum, long aLastused) {
        super(aId, aParent, aNumber, aKnowledge, aMinimum, aLastused);
    }

    DbSkill(long aId, int aNumber, double aKnowledge, double aMinimum, long aLastused) {
        super(aId, aNumber, aKnowledge, aMinimum, aLastused);
    }

    @Override
    void save() throws IOException {
        if (this.parent.isPersonal()) {
            PreparedStatement ps;
            Connection dbcon;
            block6: {
                dbcon = null;
                ps = null;
                long wurmId = this.parent.getId();
                try {
                    dbcon = WurmId.getType(wurmId) == 1 ? DbConnector.getCreatureDbCon() : DbConnector.getPlayerDbCon();
                    if (this.exists(dbcon, this.id)) {
                        ps = dbcon.prepareStatement(updateCreatureSkill);
                        ps.setDouble(1, this.knowledge);
                        ps.setLong(2, this.lastUsed);
                        ps.setDouble(3, this.minimum);
                        ps.setLong(4, this.id);
                        ps.executeUpdate();
                        break block6;
                    }
                    ps = dbcon.prepareStatement(createCreatureSkill);
                    ps.setDouble(1, this.knowledge);
                    ps.setLong(2, this.lastUsed);
                    ps.setDouble(3, this.minimum);
                    ps.setInt(4, this.number);
                    ps.setLong(5, wurmId);
                    ps.setLong(6, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException e) {
                    try {
                        throw new IOException("Problem updating or creating Creature skills, ID: " + this.id + ", wurmID: " + wurmId, e);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    void saveValue(boolean player) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = player ? DbConnector.getPlayerDbCon() : DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(updateCreatureSkill);
            ps.setDouble(1, this.knowledge);
            ps.setLong(2, this.lastUsed);
            ps.setDouble(3, this.minimum);
            ps.setLong(4, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sql) {
            try {
                throw new IOException("Problem updating or creating Creature skills, ID: " + this.id, sql);
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
    public void setJoat(boolean _joat) throws IOException {
        if (_joat != this.joat) {
            this.joat = _joat;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(setJoat);
                ps.setBoolean(1, this.joat);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sql) {
                try {
                    throw new IOException("Problem setting JOAT, ID: " + this.id, sql);
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
    }

    @Override
    public void setNumber(int newNumber) throws IOException {
        this.number = newNumber;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(updateNumber);
            ps.setInt(1, this.number);
            ps.setLong(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sql) {
            try {
                throw new IOException("Problem setting Number, ID: " + this.id, sql);
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
    void load() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (this.parent.isPersonal()) {
                long wurmId = this.parent.getId();
                dbcon = WurmId.getType(wurmId) == 1 ? DbConnector.getCreatureDbCon() : DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(getCreatureSkill);
                ps.setLong(1, this.id);
                rs = ps.executeQuery();
                if (rs.next()) {
                    this.number = rs.getInt("NUMBER");
                    this.knowledge = rs.getDouble("VALUE");
                    this.minimum = rs.getDouble("MINVALUE");
                    this.lastUsed = rs.getLong("LASTUSED");
                }
            }
        }
        catch (SQLException sqx) {
            throw new IOException("Problem updating or creating Creature/Player skills, ID: " + this.id, sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }

    private boolean exists(Connection aDbcon, long aId) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = aDbcon.prepareStatement(getCreatureSkill);
            ps.setLong(1, aId);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (SQLException sqx) {
            try {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Problem checking if creature skill ID exists, ID: " + aId, sqx);
                }
                throw sqx;
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    static byte[][] loadSkillChances() throws Exception {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        byte[][] toReturn = null;
        try {
            dbcon = DbConnector.getTemplateDbCon();
            ps = dbcon.prepareStatement(loadSkillChance);
            rs = ps.executeQuery();
            while (rs.next()) {
                byte chance;
                if (toReturn == null) {
                    toReturn = new byte[101][101];
                }
                byte sk = rs.getByte("SKILL");
                byte diff = rs.getByte("DIFFICULTY");
                toReturn[sk][diff] = chance = rs.getByte("CHANCE");
            }
        }
        catch (SQLException sqx) {
            try {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Problem loading skill chances", sqx);
                }
                throw sqx;
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return toReturn;
    }

    static void saveSkillChances(byte[][] chances) throws Exception {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getTemplateDbCon();
            for (int x = 0; x < 101; ++x) {
                for (int y = 0; y < 101; ++y) {
                    ps = dbcon.prepareStatement(createSkillChance);
                    ps.setByte(1, (byte)x);
                    ps.setByte(2, (byte)y);
                    ps.setByte(3, chances[x][y]);
                    ps.executeUpdate();
                }
            }
        }
        catch (SQLException sqx) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Problem saving skill chances", sqx);
            }
            throw sqx;
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }
}


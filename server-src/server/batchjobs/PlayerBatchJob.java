/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.batchjobs;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.items.BodyDbStrings;
import com.wurmonline.server.items.CoinDbStrings;
import com.wurmonline.server.items.ItemDbStrings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerBatchJob
implements TimeConstants,
MiscConstants {
    private static final String deleteSkills = "DELETE FROM SKILLS WHERE OWNER=?";
    private static final String deletePlayer = "DELETE FROM PLAYERS WHERE WURMID=?";
    private static final String getPlayers = "SELECT WURMID FROM PLAYERS WHERE LASTLOGOUT<" + (System.currentTimeMillis() - 2419200000L);
    private static final String getAllPlayers = "SELECT WURMID,PASSWORD FROM WURMPLAYERS2.PLAYERS";
    private static final String getAllReimbs = "SELECT * FROM REIMB WHERE CREATED=0";
    private static final String insertPassword = "UPDATE PLAYERS SET PASSWORD=? WHERE WURMID=?";
    private static final String getChampions = "SELECT WURMID FROM PLAYERS WHERE REALDEATH>0 AND REALDEATH<5";
    private static final String revertChampFaithFavor = "UPDATE PLAYERS SET FAITH=50, FAVOR=50, REALDEATH=0,PRIEST=1 WHERE WURMID=?";
    private static final String updateChampSkillStepOne = "UPDATE SKILLS SET VALUE=VALUE-50, MINVALUE=VALUE WHERE OWNER=? AND NUMBER=?";
    private static final String updateChampSkillStepTwo = "UPDATE SKILLS SET VALUE=10, MINVALUE=VALUE WHERE OWNER=? AND NUMBER=? AND VALUE<10";
    private static final String updateChampSkillStatOne = "UPDATE SKILLS SET VALUE=VALUE-5, MINVALUE=VALUE WHERE OWNER=? AND NUMBER=?";
    private static final String selectChanneling = "SELECT * FROM SKILLS WHERE OWNER=? AND NUMBER=10067";
    private static final String updateChanneling = "UPDATE SKILLS SET VALUE=?, MINVALUE=VALUE WHERE OWNER=? AND NUMBER=10067";
    private static final String reimburseFatigue = "UPDATE PLAYERS SET FATIGUE=?,LASTFATIGUE=?,SLEEP=LEAST(36000,SLEEP+?)";
    private static Logger logger = Logger.getLogger(PlayerBatchJob.class.getName());
    private static final String updateReimb = "UPDATE REIMB SET CREATED=1 WHERE NAME=?";

    private PlayerBatchJob() {
    }

    public static final void monthlyPrune() {
        try {
            Connection pdbcon = DbConnector.getPlayerDbCon();
            Connection idbcon = DbConnector.getItemDbCon();
            PreparedStatement ps = pdbcon.prepareStatement(getPlayers);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long owner = rs.getLong("WURMID");
                PreparedStatement ps2 = idbcon.prepareStatement(ItemDbStrings.getInstance().deleteByOwnerId());
                ps2.setLong(1, owner);
                ps2.executeUpdate();
                ps2.close();
                PreparedStatement ps3 = pdbcon.prepareStatement(deleteSkills);
                ps3.setLong(1, owner);
                ps3.executeUpdate();
                ps3.close();
                PreparedStatement ps4 = pdbcon.prepareStatement(deletePlayer);
                ps4.setLong(1, owner);
                ps4.executeUpdate();
                ps4.close();
                PreparedStatement ps5 = idbcon.prepareStatement(BodyDbStrings.getInstance().deleteByOwnerId());
                ps5.setLong(1, owner);
                ps5.executeUpdate();
                ps5.close();
                PreparedStatement ps6 = idbcon.prepareStatement(CoinDbStrings.getInstance().deleteByOwnerId());
                ps6.setLong(1, owner);
                ps6.executeUpdate();
                ps6.close();
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    public static final void reimburseFatigue() {
        logger.log(Level.INFO, "Wurm crashed. Reimbursing fatigue for all players.");
        try {
            Connection pdbcon = DbConnector.getPlayerDbCon();
            PreparedStatement ps = pdbcon.prepareStatement(reimburseFatigue);
            ps.setLong(1, 43200L);
            ps.setLong(2, System.currentTimeMillis());
            ps.setLong(3, 18000L);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    public static final void fixPasswords() {
        logger.log(Level.INFO, "Fixing passwords.");
        try {
            long wurmid = -1L;
            String password = "";
            Connection pdbcon = DbConnector.getPlayerDbCon();
            PreparedStatement ps = pdbcon.prepareStatement(getAllPlayers);
            ResultSet rs = ps.executeQuery();
            int nums = 0;
            while (rs.next()) {
                wurmid = rs.getLong("WURMID");
                password = rs.getString("PASSWORD");
                PreparedStatement ps2 = pdbcon.prepareStatement(insertPassword);
                ps2.setString(1, password);
                ps2.setLong(2, wurmid);
                ps2.executeUpdate();
                ps2.close();
                ++nums;
            }
            rs.close();
            ps.close();
            logger.log(Level.INFO, "Fixed " + nums + " passwords.");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    public static final void createReimbursedAccs() {
        try {
            Connection pdbcon = DbConnector.getPlayerDbCon();
            PreparedStatement ps = pdbcon.prepareStatement(getAllReimbs);
            ResultSet rs = ps.executeQuery();
            int nums = 0;
            boolean wild = false;
            int serverId = 2;
            byte kingdom = 1;
            String name = "";
            String password = "";
            String email = "";
            String challengePhrase = "";
            String answer = "";
            byte power = 0;
            byte gender = 0;
            while (rs.next()) {
                ServerEntry toCreateOn;
                serverId = 2;
                name = rs.getString("NAME");
                email = rs.getString("EMAIL");
                password = rs.getString("PASSWORD");
                if (password == null || password.length() == 0) {
                    password = name + "kjhoiu1131";
                }
                if ((challengePhrase = rs.getString("PWQUESTION")) == null || challengePhrase.length() == 0) {
                    challengePhrase = "";
                }
                if ((answer = rs.getString("PWANSWER")) == null || answer.length() == 0) {
                    answer = "";
                }
                wild = rs.getBoolean("WILD");
                power = rs.getByte("POWER");
                kingdom = rs.getByte("KINGDOM");
                gender = rs.getByte("GENDER");
                if (wild) {
                    serverId = 3;
                }
                if ((toCreateOn = Servers.getServerWithId(serverId)) != null) {
                    int tilex = toCreateOn.SPAWNPOINTJENNX;
                    int tiley = toCreateOn.SPAWNPOINTJENNY;
                    if (kingdom == 3) {
                        tilex = toCreateOn.SPAWNPOINTLIBX;
                        tiley = toCreateOn.SPAWNPOINTLIBY;
                    }
                    LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
                    try {
                        logger.log(Level.INFO, "Creating " + name + " on server " + serverId);
                        byte[] playerData = lsw.createAndReturnPlayer(name, password, challengePhrase, answer, email, kingdom, power, Server.rand.nextLong(), gender, true, true, true);
                        long wurmId = IntraServerConnection.savePlayerToDisk(playerData, tilex, tiley, true, true);
                        ++nums;
                        PreparedStatement ps2 = pdbcon.prepareStatement(updateReimb);
                        ps2.setString(1, name);
                        ps2.executeUpdate();
                        ps2.close();
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                    continue;
                }
                logger.log(Level.WARNING, "Failed to create player " + name + ": The desired server " + serverId + " does not exist.");
            }
            rs.close();
            ps.close();
            logger.log(Level.INFO, "Created " + nums + " players.");
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    public static final void removeChampions() {
        try {
            Connection pdbcon = DbConnector.getPlayerDbCon();
            PreparedStatement ps = pdbcon.prepareStatement(getChampions);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long owner = rs.getLong("WURMID");
                PreparedStatement ps2 = pdbcon.prepareStatement(revertChampFaithFavor);
                ps2.setLong(1, owner);
                ps2.executeUpdate();
                ps2.close();
                PlayerBatchJob.updateChampSkill(10066, owner, pdbcon);
                PlayerBatchJob.updateChampSkill(10068, owner, pdbcon);
                PlayerBatchJob.updateChampStat(104, owner, pdbcon);
                PlayerBatchJob.updateChampStat(102, owner, pdbcon);
                PlayerBatchJob.updateChampStat(103, owner, pdbcon);
                PlayerBatchJob.updateChampStat(100, owner, pdbcon);
                PlayerBatchJob.updateChampStat(101, owner, pdbcon);
                PlayerBatchJob.updateChampStat(106, owner, pdbcon);
                PlayerBatchJob.updateChampStat(105, owner, pdbcon);
                PlayerBatchJob.fixChanneling(owner, pdbcon);
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    private static final void updateChampSkill(int number, long wurmid, Connection dbcon) {
        try {
            PreparedStatement ps = dbcon.prepareStatement(updateChampSkillStepOne);
            ps.setLong(1, wurmid);
            ps.setInt(2, number);
            ps.executeUpdate();
            ps.close();
            PreparedStatement ps2 = dbcon.prepareStatement(updateChampSkillStepTwo);
            ps2.setLong(1, wurmid);
            ps2.setInt(2, number);
            ps2.executeUpdate();
            ps2.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    private static final void updateChampStat(int number, long wurmid, Connection dbcon) {
        try {
            PreparedStatement ps = dbcon.prepareStatement(updateChampSkillStatOne);
            ps.setLong(1, wurmid);
            ps.setInt(2, number);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }

    private static final void fixChanneling(long wurmid, Connection dbcon) {
        try {
            PreparedStatement ps = dbcon.prepareStatement(selectChanneling);
            ps.setLong(1, wurmid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                float value = rs.getFloat("VALUE");
                ps.close();
                rs.close();
                float newValue = value - 50.0f;
                if (value > 80.0f) {
                    newValue += (value - 80.0f) * 2.0f;
                    if (value > 85.0f) {
                        newValue += (value - 85.0f) * 2.0f;
                    }
                    if (value > 90.0f) {
                        newValue += (value - 90.0f) * 2.0f;
                    }
                }
                PreparedStatement ps2 = dbcon.prepareStatement(updateChanneling);
                ps2.setFloat(1, newValue);
                ps2.setLong(2, wurmid);
                ps2.executeUpdate();
                ps2.close();
            }
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, sqx.getMessage(), sqx);
        }
    }
}


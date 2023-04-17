/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Reimbursement
implements MonetaryConstants {
    private String name = "";
    private String email = "";
    private String paypalEmail = "";
    private int daysLeft = 0;
    private int months = 0;
    private int silver = 0;
    private boolean titleAndBok = false;
    private boolean mBok = false;
    private static Logger reimblogger = Logger.getLogger("Reimbursements");
    private static final Logger logger = Logger.getLogger(Reimbursement.class.getName());
    private static Map<String, Reimbursement> reimbursements = new HashMap<String, Reimbursement>();
    private boolean deleted = false;
    private static final String LOAD_REIMB = "SELECT * FROM REIMB WHERE REIMBURSED=0";
    private static final String LOAD_SPECREIMB = "SELECT * FROM REIMB WHERE NAME=?";
    private static final String SET_REIMB = "UPDATE REIMB SET MONTHS=?, SILVER=?,TITLEBOK=?,DAYSLEFT=? WHERE NAME=?";
    private static final String UPDATE_REIMB = "UPDATE REIMB SET MONTHS=?, SILVER=?,TITLEBOK=?,DAYSLEFT=?, REIMBURSED=0 WHERE NAME=?";
    private static final String DELETE_REIMB = "UPDATE REIMB SET REIMBURSED=1 WHERE NAME=?";
    public static final String NOREIMBS = "text{text='You have no reimbursements pending.'}";
    public static final String nameString = "Name=";
    public static final String nameEndString = " - '}";
    public static final String keySilver = "silver";
    public static final String keyDays = "days";
    public static final String keyBok = "bok";
    public static final String keyMBok = "mbok";
    public static final String keyTrinket = "trinket";

    private Reimbursement() {
    }

    public static void loadAll() throws IOException {
        block6: {
            block5: {
                long start = System.nanoTime();
                Connection dbcon = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                if (Servers.localServer.id != Servers.loginServer.id) break block5;
                try {
                    reimbursements = new HashMap<String, Reimbursement>();
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(LOAD_REIMB);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        Reimbursement r = new Reimbursement();
                        r.name = rs.getString("NAME");
                        r.email = rs.getString("EMAIL");
                        r.paypalEmail = rs.getString("PAYPALEMAIL");
                        r.months = rs.getInt("MONTHS");
                        r.daysLeft = rs.getInt("DAYSLEFT");
                        r.silver = rs.getInt("SILVER");
                        r.titleAndBok = rs.getBoolean("TITLEBOK");
                        r.mBok = rs.getBoolean("MBOK");
                        reimbursements.put(r.name, r);
                    }
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
                        throw new IOException(sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, rs);
                        DbConnector.returnConnection(dbcon);
                        long end = System.nanoTime();
                        logger.info("Loaded " + reimbursements.size() + " reimbursements from the database took " + (float)(end - start) / 1000000.0f + " ms");
                        throw throwable;
                    }
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + reimbursements.size() + " reimbursements from the database took " + (float)(end - start) / 1000000.0f + " ms");
                break block6;
            }
            logger.info("Did not load reimbursements from the database as this is not the login server, which has id: " + Servers.loginServer.id);
        }
    }

    private static void loadReimb(String name) throws IOException {
        block6: {
            block5: {
                ResultSet rs;
                PreparedStatement ps;
                Connection dbcon;
                block4: {
                    if (Servers.localServer.id != Servers.loginServer.id) break block5;
                    dbcon = null;
                    ps = null;
                    rs = null;
                    try {
                        dbcon = DbConnector.getPlayerDbCon();
                        ps = dbcon.prepareStatement(LOAD_SPECREIMB);
                        ps.setString(1, name);
                        rs = ps.executeQuery();
                        if (!rs.next()) break block4;
                        Reimbursement r = new Reimbursement();
                        r.name = rs.getString("NAME");
                        r.email = rs.getString("EMAIL");
                        r.paypalEmail = rs.getString("PAYPALEMAIL");
                        r.months = rs.getInt("MONTHS");
                        r.daysLeft = rs.getInt("DAYSLEFT");
                        r.silver = rs.getInt("SILVER");
                        r.titleAndBok = rs.getBoolean("TITLEBOK");
                        r.mBok = rs.getBoolean("MBOK");
                        reimbursements.put(r.name, r);
                        logger.log(Level.INFO, "Found " + r.name + ": " + r.silver + "s, " + r.months + "m, " + r.daysLeft + "d, bok=" + r.titleAndBok + ", mbok=" + r.mBok + ".");
                    }
                    catch (SQLException sqex) {
                        try {
                            logger.log(Level.WARNING, sqex.getMessage(), sqex);
                            throw new IOException(sqex);
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
                break block6;
            }
            logger.info("Did not load reimbursement " + name + " from the database as this is not the login server, which has id: " + Servers.loginServer.id);
        }
    }

    public static String addReimb(String changerName, String name, int numMonths, int _silver, int _daysLeft, boolean setbok) {
        Reimbursement r = reimbursements.get(name);
        if (r == null) {
            try {
                Reimbursement.loadReimb(name);
                r = reimbursements.get(name);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, Servers.localServer.name + " - error " + iox.getMessage(), iox);
                return Servers.localServer.name + " - error " + iox.getMessage();
            }
        }
        if (r != null) {
            if (r.deleted) {
                r.months = 0;
                r.silver = 0;
                r.titleAndBok = false;
                r.daysLeft = 0;
            }
            r.months += numMonths;
            r.silver += _silver;
            r.daysLeft += _daysLeft;
            if (!r.titleAndBok) {
                r.titleAndBok = setbok;
            }
            reimblogger.log(Level.INFO, changerName + " added to " + name + ": " + numMonths + " m, " + _silver + " s, " + _daysLeft + " days, bok=" + setbok + ".");
            try {
                r.update();
            }
            catch (IOException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
                return Servers.localServer.name + " - Error - problem saving to database. Player was awarded though and may withdraw anyway. " + ex.getMessage();
            }
            return Servers.localServer.name + " - ok. " + name + " now has " + r.months + "m, " + r.silver + "s, " + r.daysLeft + "d, bok=" + r.titleAndBok + "\n";
        }
        return "";
    }

    public static boolean withDraw(String retriever, String name, String _email, int _months, int _silvers, boolean titlebok, int _daysLeft) {
        Reimbursement r = reimbursements.get(name);
        if (r != null) {
            if (r.email.toLowerCase().equals(_email.toLowerCase())) {
                if (r.withDraw(retriever, _months, _silvers, titlebok, _daysLeft)) {
                    reimblogger.log(Level.INFO, retriever + " withdrew from " + name + " " + _months + " months, " + _silvers + " silver, " + _daysLeft + " days" + (titlebok ? " and the title and bok" : "."));
                    return true;
                }
            } else {
                logger.log(Level.WARNING, name + " does not match email: " + r.email.toLowerCase() + " with submitted email " + _email.toLowerCase());
            }
        }
        return false;
    }

    private static boolean awardPlayerSilver(String name, int silver) {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        try {
            pinf.load();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, name + ", " + iox.getMessage(), iox);
            return false;
        }
        if (pinf != null && silver > 0) {
            try {
                PlayerInfoFactory.addMoneyToBank(pinf.wurmId, 10000 * silver, "Reimbursed " + pinf.getName());
                return true;
            }
            catch (Exception iox) {
                logger.log(Level.WARNING, name + ", silver=" + silver + "," + iox.getMessage(), iox);
            }
        }
        return false;
    }

    private static boolean awardPlayerDays(String name, int days) {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        try {
            pinf.load();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, name + ", " + iox.getMessage(), iox);
            return false;
        }
        if (pinf != null && days > 0) {
            try {
                PlayerInfoFactory.addPlayingTime(pinf.wurmId, 0, days, "Reimbursed " + pinf.getName());
                return true;
            }
            catch (Exception iox) {
                logger.log(Level.WARNING, name + ", days=" + days + "," + iox.getMessage(), iox);
            }
        }
        return false;
    }

    private boolean withDraw(String aName, int _months, int _silvers, boolean titlebok, int _daysLeft) {
        logger.log(Level.INFO, aName + " Withdrawing " + _months + "m, " + _silvers + "s, bok=" + titlebok + ", " + _daysLeft + "d.");
        boolean awardedSilver = false;
        boolean awardedDays = false;
        if (!this.deleted && (_months > 0 || _silvers > 0 || titlebok || _daysLeft > 0)) {
            if (_months < 0 || _silvers < 0 || _daysLeft < 0) {
                return false;
            }
            if (_months > this.months || _silvers > this.silver || titlebok && !this.titleAndBok || _daysLeft > this.daysLeft) {
                return false;
            }
            _months = Math.min(5, _months);
            this.months -= _months;
            if (_silvers > 0) {
                if (Reimbursement.awardPlayerSilver(aName, _silvers)) {
                    awardedSilver = true;
                    this.silver -= _silvers;
                } else {
                    return false;
                }
            }
            if (_daysLeft > 0) {
                _daysLeft = this.daysLeft < 30 ? this.daysLeft : Math.max(_daysLeft, 30);
                if (Reimbursement.awardPlayerDays(aName, _daysLeft)) {
                    awardedDays = true;
                    this.daysLeft -= _daysLeft;
                } else {
                    return false;
                }
            }
            if (titlebok) {
                this.titleAndBok = false;
            }
            if (!this.titleAndBok && this.months == 0 && this.silver == 0 && this.daysLeft == 0) {
                try {
                    this.delete();
                }
                catch (IOException iox) {
                    if (titlebok) {
                        this.titleAndBok = true;
                    }
                    this.months += _months;
                    if (!awardedSilver) {
                        this.silver += _silvers;
                    }
                    if (!awardedDays) {
                        this.daysLeft += _daysLeft;
                    }
                    return false;
                }
            }
            try {
                this.save();
            }
            catch (IOException iox) {
                if (titlebok) {
                    this.titleAndBok = true;
                }
                this.months += _months;
                if (!awardedSilver) {
                    this.silver += _silvers;
                }
                if (!awardedDays) {
                    this.daysLeft += _daysLeft;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_REIMB);
            ps.setInt(1, this.months);
            ps.setInt(2, this.silver);
            ps.setBoolean(3, this.titleAndBok);
            ps.setInt(4, this.daysLeft);
            ps.setString(5, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set reimbursed for " + this.name, ex);
                throw new IOException(ex);
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

    private void update() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(UPDATE_REIMB);
            ps.setInt(1, this.months);
            ps.setInt(2, this.silver);
            ps.setBoolean(3, this.titleAndBok);
            ps.setInt(4, this.daysLeft);
            ps.setString(5, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set reimbursed for " + this.name, ex);
                throw new IOException(ex);
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

    private void delete() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_REIMB);
            ps.setString(1, this.name);
            ps.executeUpdate();
            this.deleted = true;
            reimblogger.log(Level.INFO, this.name + " Reimbursements unavailable.");
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to delete reimbursed for " + this.name, ex);
                throw new IOException(ex);
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

    private static Reimbursement[] getReimbsForEmail(String _email) {
        HashSet<Reimbursement> set = new HashSet<Reimbursement>();
        for (Reimbursement r : reimbursements.values()) {
            if (r.deleted || !r.email.toLowerCase().equals(_email) || r.months <= 0 && r.silver <= 0 && !r.titleAndBok && r.daysLeft <= 0) continue;
            set.add(r);
        }
        return set.toArray(new Reimbursement[set.size()]);
    }

    public static String getReimbursementInfo(String _email) {
        if (_email.length() == 0) {
            logger.log(Level.WARNING, "Cannot get reimbs for a player with an empty email");
            return NOREIMBS;
        }
        Reimbursement[] reimbs = Reimbursement.getReimbsForEmail(_email.toLowerCase());
        logger.log(Level.INFO, "Trying to get reimbs for " + _email);
        if (reimbs.length > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("text{text='Read this carefully:'}text{text=''}");
            buf.append("text{text='If you have trinkets to withdraw, what you get will _depend on how many trinkets you withdraw_.'}text{text=''}");
            buf.append("text{text='If you withdraw 1 trinket many times you will end up with a lot of spyglasses.'}");
            buf.append("text{text='If you withdraw 2 trinkets many times you will end up with a lot of spyglasses and a lot of basic tools.'}");
            buf.append("text{text='This is the trinket ladder:'}");
            buf.append("text{text='1 trinket: Spyglass.'}");
            buf.append("text{text='2 trinkets: 1 Resurrection stone, Basic tools QL 30 and the above.'}");
            buf.append("text{text='3 trinkets: QL 30 full leather armour and ql 30 medium metal shield and the above.'}");
            buf.append("text{text='4 trinkets: QL 50 lantern and ql 70 compass and the above.'}");
            buf.append("text{text='5 trinkets: 3 resurrection stones and the above.'}");
            buf.append("text{text='You will not withdraw more than 5 trinkets at a time.'}");
            buf.append("text{type='italic';text='Note that within a few months the kingdom of Mol-Rehan may emerge. You may want to save some if you want to try a MR character.'}");
            buf.append("text{type='italic';text='If you have premium days left, you will withdraw at a minimum 30 days no matter what you type in the box.'}");
            buf.append("text{text='You have the following reimbursements available (you will withdraw the amount in the textbox):'}text{text=''}");
            for (int x = 0; x < reimbs.length; ++x) {
                buf.append("text{type='bold';text='Name=" + reimbs[x].name + nameEndString);
                if (reimbs[x].months > 0) {
                    buf.append("harray{label{text='Trinkets:'};input{id='trinket" + reimbs[x].name + "'; text='" + reimbs[x].months + "'; maxchars='2'}}");
                }
                if (reimbs[x].silver > 0) {
                    buf.append("harray{label{text='Silver:'};input{id='silver" + reimbs[x].name + "'; text='" + reimbs[x].silver + "'; maxchars='3'}}");
                }
                if (reimbs[x].daysLeft > 0) {
                    buf.append("harray{label{text='Premium days:'};input{id='days" + reimbs[x].name + "'; text='" + reimbs[x].daysLeft + "'; maxchars='3'}}");
                    if (reimbs[x].daysLeft > 30) {
                        buf.append("harray{label{text='(Withdraw 0 or minimum 30!)'}}");
                    } else {
                        buf.append("harray{label{text='(Withdraw 0 or minimum " + reimbs[x].daysLeft + ")!'}}");
                    }
                }
                if (reimbs[x].titleAndBok && reimbs[x].mBok) {
                    buf.append("harray{label{text='Titles+MBoK:'};checkbox{id='mbok" + reimbs[x].name + "';text='Mark this if you want to retrieve the Ageless and Keeper titles and the Master BoK'}}");
                } else if (reimbs[x].titleAndBok) {
                    buf.append("harray{label{text='Title+BoK:'};checkbox{id='bok" + reimbs[x].name + "';text='Mark this if you want to retrieve the Ageless title and the BoK'}}");
                }
                buf.append("label{text='__________________________'};");
            }
            return buf.toString();
        }
        return NOREIMBS;
    }
}


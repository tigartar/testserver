/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.intra;

import com.wurmonline.communication.SimpleConnectionListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerMonitoring;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.WoundMetaData;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureDataStream;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.EffectMetaData;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.DbStrings;
import com.wurmonline.server.items.FrozenItemDbStrings;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemData;
import com.wurmonline.server.items.ItemDbStrings;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.ItemRequirement;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.Itempool;
import com.wurmonline.server.items.Puppet;
import com.wurmonline.server.items.RecipesByPlayer;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Awards;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.EpicPlayerTransferMetaData;
import com.wurmonline.server.players.MapAnnotation;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerMetaData;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.SkillMetaData;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.SpellEffectMetaData;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IntraServerConnection
implements SimpleConnectionListener,
MiscConstants,
TimeConstants {
    private final SocketConnection conn;
    private static final Logger logger = Logger.getLogger(IntraServerConnection.class.getName());
    private ByteArrayOutputStream dataStream;
    private final ServerMonitoring wurmserver;
    private static final String DELETE_FRIENDS = "DELETE FROM FRIENDS WHERE WURMID=?";
    private static final String DELETE_ENEMIES = "DELETE FROM ENEMIES WHERE WURMID=?";
    private static final String DELETE_IGNORED = "DELETE FROM IGNORED WHERE WURMID=?";
    private static final String DELETE_TITLES = "DELETE FROM TITLES WHERE WURMID=?";
    private static final String DELETE_HISTORY_IP = "DELETE FROM PLAYERHISTORYIPS WHERE PLAYERID=?";
    private static final String DELETE_HISTORY_EMAIL = "DELETE FROM PLAYEREHISTORYEMAIL WHERE PLAYERID=?";
    private static final int DISCONNECT_TICKS = 200;
    private static long draggedItem = -10L;
    private static final Set<String> moneyDetails = new HashSet<String>();
    private static final Set<String> timeDetails = new HashSet<String>();
    public static String lastItemName = "unknown";
    public static long lastItemId = -10L;
    private static boolean saving = false;

    IntraServerConnection(SocketConnection aConn, ServerMonitoring aServer) {
        this.conn = aConn;
        this.wurmserver = aServer;
    }

    @Override
    public void reallyHandle(int num, ByteBuffer byteBuffer) {
        long check = System.currentTimeMillis();
        short cmd = byteBuffer.get();
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Received cmd " + cmd);
        }
        if (cmd == 1) {
            this.validate(byteBuffer);
        } else if (cmd == 13) {
            try {
                this.sendPingAnswer();
            }
            catch (IOException iOException) {}
        } else if (cmd == 9) {
            long wurmid = byteBuffer.getLong();
            try {
                String name = Players.getInstance().getNameFor(wurmid);
                PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
                pinf.load();
                this.sendPlayerVersion(pinf.version);
            }
            catch (NoSuchPlayerException nsp) {
                try {
                    this.sendPlayerVersion(0L);
                }
                catch (IOException iox) {
                    try {
                        this.sendCommandFailed();
                    }
                    catch (IOException iox2) {
                        logger.log(Level.WARNING, "Failed to send command failed.");
                    }
                }
            }
            catch (IOException iox3) {
                try {
                    this.sendCommandFailed();
                }
                catch (IOException iox2) {
                    logger.log(Level.WARNING, "Failed to send command failed.");
                }
            }
        } else if (cmd == 11) {
            long wid = byteBuffer.getLong();
            try {
                String name = Players.getInstance().getNameFor(wid);
                PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
                pinf.load();
                this.sendPlayerPaymentExpire(pinf.getPaymentExpire());
            }
            catch (NoSuchPlayerException nsp) {
                try {
                    this.sendPlayerPaymentExpire(0L);
                }
                catch (IOException iox) {
                    try {
                        this.sendCommandFailed();
                    }
                    catch (IOException iox2) {
                        logger.log(Level.WARNING, "Failed to send command failed.");
                    }
                }
            }
            catch (IOException iox3) {
                try {
                    this.sendCommandFailed();
                }
                catch (IOException iox2) {
                    logger.log(Level.WARNING, "Failed to send command failed.");
                }
            }
        } else if (cmd == 6) {
            this.validateTransferRequest(byteBuffer);
        } else if (cmd == 3) {
            boolean surfaced;
            int posx = byteBuffer.getInt();
            int posy = byteBuffer.getInt();
            boolean bl = surfaced = byteBuffer.get() != 0;
            if (this.unpackPlayerData(posx, posy, surfaced)) {
                try {
                    this.sendCommandDone();
                }
                catch (IOException ex) {
                    try {
                        logger.log(Level.WARNING, "Failed to receive user: " + ex.getMessage(), ex);
                        this.sendCommandFailed();
                    }
                    catch (IOException ex2) {
                        logger.log(Level.WARNING, "Failed to send command failed.");
                    }
                    this.conn.ticksToDisconnect = 200;
                }
            } else {
                try {
                    this.sendCommandFailed();
                    logger.log(Level.WARNING, "Failed to unpack data.");
                    this.conn.ticksToDisconnect = 200;
                }
                catch (IOException ex2) {
                    logger.log(Level.WARNING, "Failed to send command failed.");
                }
            }
        } else if (cmd == 7) {
            if (this.readNextDataBlock(byteBuffer)) {
                try {
                    this.sendDataReceived();
                }
                catch (IOException iox) {
                    try {
                        this.sendCommandFailed();
                    }
                    catch (IOException ex2) {
                        logger.log(Level.WARNING, "Failed to send command failed.");
                    }
                }
            }
        } else if (cmd == 16) {
            this.conn.ticksToDisconnect = 200;
            long wurmid = byteBuffer.getLong();
            long currentMoney = byteBuffer.getLong();
            long moneyAdded = byteBuffer.getLong();
            int length = byteBuffer.getInt();
            byte[] det = new byte[length];
            byteBuffer.get(det);
            String detail = "unknown";
            try {
                detail = new String(det, "UTF-8");
                if (moneyDetails.contains(detail)) {
                    try {
                        this.sendCommandDone();
                        return;
                    }
                    catch (IOException iOException) {}
                }
            }
            catch (UnsupportedEncodingException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
            if (info != null) {
                try {
                    info.load();
                }
                catch (IOException iox) {
                    try {
                        logger.log(Level.WARNING, "Failed to load player info for " + wurmid + ": " + iox.getMessage(), iox);
                        this.sendCommandFailed();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    this.conn.ticksToDisconnect = 200;
                    return;
                }
            }
            logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + "!");
            try {
                this.sendCommandFailed();
            }
            catch (IOException iox) {
                // empty catch block
            }
            if (info != null && info.wurmId > 0L) {
                if (info.currentServer != Servers.localServer.id) {
                    logger.warning("Received a CMD_SET_PLAYER_MONEY for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + ")");
                }
                try {
                    Shop kingsShop;
                    info.setMoney(currentMoney);
                    if (detail.contains("Premium") && (kingsShop = Economy.getEconomy().getKingsShop()) != null) {
                        kingsShop.setMoney(kingsShop.getMoney() - moneyAdded);
                    }
                    new MoneyTransfer(info.getName(), wurmid, currentMoney, moneyAdded, detail, 0, "");
                    this.sendCommandDone();
                    boolean referred = false;
                    if (detail.startsWith("Referred by ")) {
                        referred = true;
                        info.addToSleep(3600);
                    }
                    moneyDetails.add(detail);
                    try {
                        Player p = Players.getInstance().getPlayer(wurmid);
                        Change c = new Change(currentMoney);
                        p.getCommunicator().sendNormalServerMessage("Your available money in the bank is now " + c.getChangeString() + ".");
                        if (referred) {
                            String sleepString = "You also received an hour of sleep bonus which will increase your skill gain speed.";
                            p.getCommunicator().sendSafeServerMessage("You also received an hour of sleep bonus which will increase your skill gain speed.");
                        }
                    }
                    catch (NoSuchPlayerException p) {
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, wurmid + ", failed to set money to " + currentMoney + ".", iox);
                    try {
                        this.sendCommandFailed();
                    }
                    catch (IOException iox2) {
                        this.conn.disconnect();
                    }
                }
            } else {
                logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + "!");
                try {
                    this.sendCommandFailed();
                }
                catch (IOException iox2) {
                    this.conn.disconnect();
                }
            }
            if (System.currentTimeMillis() - check > 1000L) {
                logger.log(Level.INFO, "Lag detected at CMD_SET_PLAYER_MONEY: " + (int)((System.currentTimeMillis() - check) / 1000L));
            }
        } else if (cmd == 17) {
            block135: {
                this.conn.ticksToDisconnect = 200;
                long wurmid = byteBuffer.getLong();
                long currentExpire = byteBuffer.getLong();
                int days = byteBuffer.getInt();
                int months = byteBuffer.getInt();
                boolean dealItems = byteBuffer.get() > 0;
                int length = byteBuffer.getInt();
                byte[] det = new byte[length];
                byteBuffer.get(det);
                String detail = "unknown";
                try {
                    detail = new String(det, "UTF-8");
                    if (timeDetails.contains(detail)) {
                        try {
                            this.sendCommandDone();
                            return;
                        }
                        catch (IOException iox2) {}
                    }
                }
                catch (UnsupportedEncodingException ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
                PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
                if (info != null) {
                    try {
                        info.load();
                    }
                    catch (IOException iox) {
                        try {
                            this.sendCommandFailed();
                        }
                        catch (IOException iox2) {
                            this.conn.disconnect();
                        }
                        this.conn.ticksToDisconnect = 200;
                        if (System.currentTimeMillis() - check > 1000L) {
                            logger.log(Level.INFO, "Lag detected at CMD_SET_PLAYER_PAYMENTEXPIRE IOEXCEPTION: " + (int)((System.currentTimeMillis() - check) / 1000L));
                        }
                        return;
                    }
                    catch (NullPointerException np) {
                        logger.log(Level.WARNING, "No player with id=" + wurmid + " on this server.");
                        try {
                            this.sendCommandFailed();
                        }
                        catch (IOException iox2) {
                            this.conn.disconnect();
                        }
                        this.conn.ticksToDisconnect = 200;
                        if (System.currentTimeMillis() - check > 1000L) {
                            logger.log(Level.INFO, "Lag detected at CMD_SET_PLAYER_PAYMENTEXPIRE IOEXCEPTION: " + (int)((System.currentTimeMillis() - check) / 1000L));
                        }
                        return;
                    }
                }
                if (info.wurmId > 0L) {
                    if (info.currentServer != Servers.localServer.id) {
                        logger.warning("Received a CMD_SET_PLAYER_PAYMENTEXPIRE for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + ")");
                    }
                    try {
                        if (currentExpire > System.currentTimeMillis()) {
                            if (info.getPaymentExpire() <= 0L) {
                                Server.addNewPlayer(info.getName());
                            } else {
                                Server.incrementOldPremiums(info.getName());
                            }
                        }
                        info.setPaymentExpire(currentExpire);
                        boolean referred = false;
                        if (detail.startsWith("Referred by ")) {
                            referred = true;
                            info.addToSleep(3600);
                        }
                        new TimeTransfer(info.getName(), wurmid, months, dealItems, days, detail);
                        this.sendCommandDone();
                        timeDetails.add(detail);
                        try {
                            Player p = Players.getInstance().getPlayer(wurmid);
                            String expireString = "You now have premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
                            p.getCommunicator().sendSafeServerMessage(expireString);
                            if (referred) {
                                String sleepString = "You also received an hour of sleep bonus which will increase your skill gain speed.";
                                p.getCommunicator().sendSafeServerMessage("You also received an hour of sleep bonus which will increase your skill gain speed.");
                            }
                            if (dealItems) {
                                try {
                                    Item inventory = p.getInventory();
                                    for (int x = 0; x < months; ++x) {
                                        Item i = ItemFactory.createItem(666, 99.0f, "");
                                        inventory.insertItem(i, true);
                                    }
                                    logger.log(Level.INFO, "Inserted " + months + " sleep powder in " + p.getName() + " inventory " + inventory.getWurmId());
                                    Message rmess = new Message(null, 3, ":Event", "You have received " + months + " sleeping powders in your inventory.");
                                    rmess.setReceiver(p.getWurmId());
                                    Server.getInstance().addMessage(rmess);
                                }
                                catch (Exception ex) {
                                    logger.log(Level.INFO, ex.getMessage(), ex);
                                }
                            }
                        }
                        catch (NoSuchPlayerException exp) {
                            if (!dealItems) break block135;
                            try {
                                long inventoryId = DbCreatureStatus.getInventoryIdFor(info.wurmId);
                                for (int x = 0; x < months; ++x) {
                                    Item i = ItemFactory.createItem(666, 99.0f, "");
                                    i.setParentId(inventoryId, true);
                                    i.setOwnerId(info.wurmId);
                                }
                                logger.log(Level.INFO, "Inserted " + months + " sleep powder in offline " + info.getName() + " inventory " + inventoryId);
                            }
                            catch (Exception ex) {
                                logger.log(Level.INFO, ex.getMessage(), ex);
                            }
                        }
                    }
                    catch (IOException iox) {
                        try {
                            this.sendCommandFailed();
                        }
                        catch (IOException iox2) {
                            this.conn.disconnect();
                        }
                    }
                } else {
                    logger.log(Level.WARNING, wurmid + ", failed to locate player info and set expire time to " + currentExpire + "!");
                    try {
                        this.sendCommandFailed();
                    }
                    catch (IOException iox2) {
                        this.conn.disconnect();
                    }
                }
            }
            if (System.currentTimeMillis() - check > 1000L) {
                logger.log(Level.INFO, "Lag detected at CMD_SET_PLAYER_PAYMENTEXPIRE: " + (int)((System.currentTimeMillis() - check) / 1000L));
            }
        } else if (cmd == 10) {
            try {
                this.sendTimeSync();
            }
            catch (IOException ex2) {
                this.conn.ticksToDisconnect = 200;
            }
            if (System.currentTimeMillis() - check > 1000L) {
                logger.log(Level.INFO, "Lag detected at CMD_GET_TIME: " + (int)((System.currentTimeMillis() - check) / 1000L));
            }
        } else if (cmd == 18) {
            if (this.changePassword(byteBuffer)) {
                try {
                    this.sendCommandDone();
                }
                catch (IOException iox) {
                    this.conn.ticksToDisconnect = 200;
                }
            } else {
                try {
                    this.sendCommandFailed();
                }
                catch (IOException iox) {
                    this.conn.ticksToDisconnect = 200;
                }
            }
            if (System.currentTimeMillis() - check > 1000L) {
                logger.log(Level.INFO, "Lag detected at CMD_SET_PLAYER_PASSWORD: " + (int)((System.currentTimeMillis() - check) / 1000L));
            }
        } else if (cmd == 15) {
            logger.log(Level.INFO, "Received disconnect.");
            this.conn.disconnect();
        }
    }

    private boolean changePassword(ByteBuffer byteBuffer) {
        long playerId = byteBuffer.getLong();
        int length = byteBuffer.getInt();
        byte[] pw = new byte[length];
        byteBuffer.get(pw);
        try {
            String hashedPassword = new String(pw, "UTF-8");
            return IntraServerConnection.setNewPassword(playerId, hashedPassword);
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static final boolean setNewPassword(long playerId, String newHashedPassword) {
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
        if (info != null) {
            try {
                info.load();
            }
            catch (Exception eex) {
                logger.log(Level.WARNING, "Failed to load info for wurmid " + playerId + ". Password unchanged." + eex.getMessage(), eex);
            }
            if (info.wurmId <= 0L) {
                logger.log(Level.WARNING, "Failed to load info for wurmid " + playerId + ". No info available. Password unchanged.");
            } else {
                info.setPassword(newHashedPassword);
                try {
                    Player p = Players.getInstance().getPlayer(playerId);
                    p.getCommunicator().sendAlertServerMessage("Your password has been updated. Use the new one to connect next time.");
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
        return true;
    }

    private boolean readNextDataBlock(ByteBuffer byteBuffer) {
        if (this.dataStream == null) {
            this.dataStream = new ByteArrayOutputStream();
        }
        int length = byteBuffer.getInt();
        byte[] toput = new byte[length];
        byteBuffer.get(toput);
        this.dataStream.write(toput, 0, length);
        return byteBuffer.get() == 1;
    }

    public static final void deletePlayer(long id) throws IOException {
        CreaturePos.delete(id);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Player ID: " + id);
            }
            ps = dbcon.prepareStatement("DELETE FROM PLAYERS WHERE WURMID=?");
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Skills for Player ID: " + id);
            }
            ps = dbcon.prepareStatement("DELETE FROM SKILLS WHERE OWNER=?");
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Wounds for Player ID: " + id);
            }
            ps = dbcon.prepareStatement("DELETE FROM WOUNDS WHERE OWNER=?");
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Friends for Player ID: " + id);
            }
            ps = dbcon.prepareStatement(DELETE_FRIENDS);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Enemies for Player ID: " + id);
            }
            ps = dbcon.prepareStatement(DELETE_ENEMIES);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Ignored for Player ID: " + id);
            }
            ps = dbcon.prepareStatement(DELETE_IGNORED);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Titles for Player ID: " + id);
            }
            ps = dbcon.prepareStatement(DELETE_TITLES);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting IP History for Player ID: " + id);
            }
            ps = dbcon.prepareStatement(DELETE_HISTORY_IP);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Email History for Player ID: " + id);
            }
            ps = dbcon.prepareStatement(DELETE_HISTORY_EMAIL);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            SpellEffect.deleteEffectsForPlayer(id);
            RecipesByPlayer.deleteRecipesForPlayer(id);
            AffinitiesTimed.deleteTimedAffinitiesForPlayer(id);
        }
        catch (SQLException sqex) {
            try {
                throw new IOException("Problem deleting playerid: " + id + " due to " + sqex.getMessage(), sqex);
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

    private static final void deletePlayer(String name, long id) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (id > -10L) {
                ps = dbcon.prepareStatement("DELETE FROM PLAYERS WHERE NAME=?");
                ps.setString(1, name);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement("DELETE FROM SKILLS WHERE OWNER=?");
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement("DELETE FROM WOUNDS WHERE OWNER=?");
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_FRIENDS);
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_ENEMIES);
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_IGNORED);
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_TITLES);
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_HISTORY_IP);
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_HISTORY_EMAIL);
                ps.setLong(1, id);
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                SpellEffect.deleteEffectsForPlayer(id);
            }
        }
        catch (SQLException sqex) {
            throw new IOException(name + " " + sqex.getMessage(), sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void deleteItem(long id, boolean frozen) throws IOException {
        if (WurmId.getType(id) != 19) {
            PreparedStatement ps;
            Connection dbcon;
            block15: {
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    DbStrings dbstrings = Item.getDbStringsByWurmId(id);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Deleting item: " + id);
                    }
                    ps = dbcon.prepareStatement(dbstrings.deleteTransferedItem());
                    ps.setLong(1, id);
                    int rows = ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (dbstrings == ItemDbStrings.getInstance()) {
                        dbstrings = FrozenItemDbStrings.getInstance();
                        ps = dbcon.prepareStatement(dbstrings.deleteTransferedItem());
                        ps.setLong(1, id);
                        if (rows == 0) {
                            rows = ps.executeUpdate();
                        } else {
                            ps.executeUpdate();
                        }
                        DbUtilities.closeDatabaseObjects(ps, null);
                    }
                    if (rows <= 0) break block15;
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Deleting effects for item: " + id);
                    }
                    ps = dbcon.prepareStatement("DELETE FROM EFFECTS WHERE OWNER=?");
                    ps.setLong(1, id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Deleting itemdata for item: " + id);
                    }
                    ps = dbcon.prepareStatement("DELETE FROM ITEMDATA WHERE WURMID=?");
                    ps.setLong(1, id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Deleting inscription data for item: " + id);
                    }
                    ps = dbcon.prepareStatement("DELETE FROM INSCRIPTIONS WHERE WURMID=?");
                    ps.setLong(1, id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Deleting locks for item: " + id);
                    }
                    ps = dbcon.prepareStatement("DELETE FROM LOCKS WHERE WURMID=?");
                    ps.setLong(1, id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    ItemSpellEffects spefs = ItemSpellEffects.getSpellEffects(id);
                    if (spefs != null) {
                        spefs.clear();
                    }
                    SpellEffect.deleteEffectsForItem(id);
                }
                catch (SQLException sqex) {
                    try {
                        if (!Servers.localServer.LOGINSERVER) {
                            throw new IOException(id + " " + sqex.getMessage(), sqex);
                        }
                        logger.log(Level.WARNING, "ITEMDELETE Failed to delete item " + id + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    private boolean unpackPlayerData(int posx, int posy, boolean surfaced) {
        try {
            this.dataStream.flush();
            this.dataStream.close();
            byte[] bytes = this.dataStream.toByteArray();
            return IntraServerConnection.savePlayerToDisk(bytes, posx, posy, surfaced, false) > 0L;
        }
        catch (IOException ex) {
            logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
            return false;
        }
    }

    public static final void readNullCultist(DataInputStream dis, String name, long wurmId) {
        try {
            Cultist cultist = Cultist.getCultist(wurmId);
            if (cultist != null) {
                cultist.deleteCultist();
            }
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to read cultist for " + name + " " + wurmId);
        }
    }

    public static final void readCultist(DataInputStream dis, String name, long wurmId) {
        block7: {
            try {
                byte clevel = dis.readByte();
                byte cpath = dis.readByte();
                long lastMeditated = dis.readLong();
                long lastReceivedLevel = dis.readLong();
                long lastAppointedLevel = dis.readLong();
                long cd1 = dis.readLong();
                long cd2 = dis.readLong();
                long cd3 = dis.readLong();
                long cd4 = dis.readLong();
                long cd5 = dis.readLong();
                long cd6 = dis.readLong();
                long cd7 = dis.readLong();
                byte skillgainCount = dis.readByte();
                Cultist cultist = Cultist.getCultist(wurmId);
                if (cultist == null) {
                    Cultist c = new Cultist(wurmId, lastMeditated, lastReceivedLevel, lastAppointedLevel, clevel, cpath, cd1, cd2, cd3, cd4, cd5, cd6, cd7);
                    try {
                        c.saveCultist(true);
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, "Failed to save cultist " + name + " level=" + clevel + " path=" + cpath + " " + iox.getMessage(), iox);
                    }
                    c.setSkillgainCount(skillgainCount);
                    break block7;
                }
                cultist.deleteCultist();
                Cultist c = new Cultist(wurmId, lastMeditated, lastReceivedLevel, lastAppointedLevel, clevel, cpath, cd1, cd2, cd3, cd4, cd5, cd6, cd7);
                try {
                    c.saveCultist(true);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save cultist " + name + " level=" + clevel + " path=" + cpath + " " + iox.getMessage(), iox);
                }
                c.setSkillgainCount(skillgainCount);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to read cultist for " + name + " " + wurmId);
            }
        }
    }

    public static final byte calculateBloodFromKingdom(byte kingdom) {
        if (kingdom == 3) {
            return 1;
        }
        if (kingdom == 2) {
            return 8;
        }
        if (kingdom == 1) {
            return 4;
        }
        if (kingdom == 4) {
            return 2;
        }
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long savePlayerEpicTransfer(DataInputStream dis) {
        try {
            logger.log(Level.INFO, "Epic transfer");
            long wurmId = dis.readLong();
            String name = dis.readUTF();
            String password = dis.readUTF();
            String session = dis.readUTF();
            String emailAddress = dis.readUTF();
            long sessionExpiration = dis.readLong();
            byte power = dis.readByte();
            long money = dis.readLong();
            long paymentExpire = dis.readLong();
            int numignored = dis.readInt();
            long[] ignored = new long[numignored];
            for (int ni = 0; ni < numignored; ++ni) {
                ignored[ni] = dis.readLong();
            }
            if (numignored == 0) {
                ignored = EMPTY_LONG_PRIMITIVE_ARRAY;
            }
            int numfriends = dis.readInt();
            long[] friends = new long[numfriends];
            byte[] friendCats = new byte[numfriends];
            for (int nf = 0; nf < numfriends; ++nf) {
                friends[nf] = dis.readLong();
                friendCats[nf] = dis.readByte();
            }
            if (numfriends == 0) {
                friends = EMPTY_LONG_PRIMITIVE_ARRAY;
                friendCats = EMPTY_BYTE_PRIMITIVE_ARRAY;
            }
            long playingTime = dis.readLong();
            long creationDate = dis.readLong();
            long lastwarned = dis.readLong();
            byte kingdom = dis.readByte();
            boolean banned = dis.readBoolean();
            long banexpiry = dis.readLong();
            String banreason = dis.readUTF();
            boolean mute = dis.readBoolean();
            short muteTimes = dis.readShort();
            long muteexpiry = dis.readLong();
            String mutereason = dis.readUTF();
            boolean maymute = dis.readBoolean();
            boolean overRideShop = dis.readBoolean();
            boolean reimbursed = dis.readBoolean();
            int warnings = dis.readInt();
            boolean mayHearDevtalk = dis.readBoolean();
            String ipaddress = dis.readUTF();
            long version = dis.readLong();
            long referrer = dis.readLong();
            String pwQuestion = dis.readUTF();
            String pwAnswer = dis.readUTF();
            boolean logging = dis.readBoolean();
            boolean seesCAWin = dis.readBoolean();
            boolean isCA = dis.readBoolean();
            boolean mayAppointCA = dis.readBoolean();
            long face = dis.readLong();
            byte blood = dis.readByte();
            long flags = dis.readLong();
            long flags2 = dis.readLong();
            byte chaosKingdom = dis.readByte();
            byte undeadType = dis.readByte();
            int undeadKills = dis.readInt();
            int undeadPKills = dis.readInt();
            int undeadPSecs = dis.readInt();
            long lastResetEarningsCounter = dis.readLong();
            long moneyEarnedBySellingLastHour = dis.readLong();
            long moneyEarnedBySellingEver = dis.readLong();
            int daysPrem = 0;
            long lastTicked = 0L;
            int monthsPaidEver = 0;
            int monthsPaidInARow = 0;
            int monthsPaidSinceReset = 0;
            int silverPaidEver = 0;
            int currentLoyalty = 0;
            int totalLoyalty = 0;
            boolean awards = false;
            if (dis.readBoolean()) {
                awards = true;
                daysPrem = dis.readInt();
                lastTicked = dis.readLong();
                monthsPaidEver = dis.readInt();
                monthsPaidInARow = dis.readInt();
                monthsPaidSinceReset = dis.readInt();
                silverPaidEver = dis.readInt();
                currentLoyalty = dis.readInt();
                totalLoyalty = dis.readInt();
            }
            byte sex = dis.readByte();
            int epicServerId = dis.readInt();
            byte epicServerKingdom = dis.readByte();
            int numskills = dis.readInt();
            for (int s = 0; s < numskills; ++s) {
                long skillId = dis.readLong();
                int skillNumber = dis.readInt();
                double skillValue = dis.readDouble();
                double skillMinimum = dis.readDouble();
                long skillLastUsed = dis.readLong();
                if (!Servers.isThisAnEpicServer()) continue;
                SkillMetaData sk = SkillMetaData.copyToEpicSkill(skillId, wurmId, skillNumber, skillValue, skillMinimum, skillLastUsed);
                SkillMetaData.deleteSkill(wurmId, skillNumber);
                sk.save();
            }
            IntraServerConnection.unpackAchievements(wurmId, dis);
            RecipesByPlayer.unPackRecipes(dis, wurmId);
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
            if (pinf == null) {
                try {
                    LoginHandler.createPlayer(name, password, pwQuestion, pwAnswer, emailAddress, kingdom, power, face, sex, false, false, wurmId);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Creation exception " + ex.getMessage(), ex);
                    long l = -1L;
                    if (dis != null) {
                        try {
                            dis.close();
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                    return l;
                }
                pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
                if (!Servers.localServer.EPIC) {
                    pinf.lastChangedKindom = 0L;
                }
            } else if (Servers.isThisAChaosServer()) {
                chaosKingdom = pinf.getChaosKingdom();
                Village cv = Villages.getVillageForCreature(wurmId);
                if (cv != null) {
                    kingdom = cv.kingdom;
                }
                if (blood == 0) {
                    blood = IntraServerConnection.calculateBloodFromKingdom(chaosKingdom);
                }
            }
            if (pinf != null) {
                if (awards) {
                    pinf.awards = Awards.getAwards(pinf.wurmId);
                    if (pinf.awards != null) {
                        pinf.awards = new Awards(wurmId, daysPrem, monthsPaidEver, monthsPaidInARow, monthsPaidSinceReset, silverPaidEver, lastTicked, currentLoyalty, totalLoyalty, false);
                        pinf.awards.update();
                    } else {
                        pinf.awards = new Awards(wurmId, daysPrem, monthsPaidEver, monthsPaidInARow, monthsPaidSinceReset, silverPaidEver, lastTicked, currentLoyalty, totalLoyalty, true);
                    }
                }
                IntraServerConnection.unpackPMList(pinf, dis);
            }
            if (Servers.isThisLoginServer() && pinf != null) {
                paymentExpire = pinf.getPaymentExpire();
                overRideShop = pinf.overRideShop;
                if (pinf.emailAddress.length() > 0) {
                    emailAddress = pinf.emailAddress;
                }
                password = pinf.getPassword();
                if (money != pinf.money) {
                    logger.log(Level.INFO, "Setting money for " + pinf.getName() + " to " + pinf.money + " instead of " + money);
                }
                money = pinf.money;
            }
            if (blood == 0 || Servers.localServer.EPIC && blood == 2) {
                blood = IntraServerConnection.calculateBloodFromKingdom(kingdom);
            }
            EpicPlayerTransferMetaData pmd = new EpicPlayerTransferMetaData(wurmId, name, password, session, sessionExpiration, power, lastwarned, playingTime, kingdom, banned, banexpiry, banreason, reimbursed, warnings, mayHearDevtalk, paymentExpire, ignored, friends, friendCats, ipaddress, mute, sex, version, money, face, seesCAWin, logging, isCA, mayAppointCA, referrer, pwQuestion, pwAnswer, overRideShop, muteTimes, muteexpiry, mutereason, maymute, emailAddress, creationDate, epicServerId, epicServerKingdom, chaosKingdom, blood, flags, flags2, undeadType, undeadKills, undeadPKills, undeadPSecs, moneyEarnedBySellingEver, daysPrem, lastTicked, currentLoyalty, totalLoyalty, monthsPaidEver, monthsPaidInARow, monthsPaidSinceReset, silverPaidEver, awards);
            pmd.save();
            if (pinf != null) {
                boolean setPremFlag = pinf.isFlagSet(8);
                pinf.setMoneyEarnedBySellingLastHour(moneyEarnedBySellingLastHour);
                pinf.setLastResetEarningsCounter(lastResetEarningsCounter);
                if (!password.equals(pinf.getPassword())) {
                    logger.log(Level.WARNING, name + " after transfer but before loading: password now is " + pinf.getPassword() + ". Sent " + password);
                }
                pinf.loaded = false;
                try {
                    pinf.load();
                    boolean updateFlags = false;
                    if (pinf.flags != flags) {
                        pinf.flags = flags;
                        pinf.setFlagBits(pinf.flags);
                        if (setPremFlag) {
                            pinf.setFlag(8, true);
                        }
                        updateFlags = true;
                    }
                    if (pinf.flags2 != flags2) {
                        pinf.flags2 = flags2;
                        pinf.setFlag2Bits(pinf.flags2);
                        updateFlags = true;
                    }
                    if (updateFlags) {
                        pinf.forceFlagsUpdate();
                    }
                    if (!password.equals(pinf.getPassword())) {
                        logger.log(Level.WARNING, name + " after transfer: password now is " + pinf.getPassword() + "  Sent " + password);
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage());
                }
            }
            pinf.loaded = false;
            pinf.load();
            pinf.lastUsedEpicPortal = System.currentTimeMillis();
            long l = wurmId;
            return l;
        }
        catch (IOException ex) {
            logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
            long l = -1L;
            return l;
        }
        finally {
            if (dis != null) {
                try {
                    dis.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    private static final void unpackAchievements(long wurmId, DataInputStream dis) throws IOException {
        int templateNums = dis.readInt();
        for (int x = 0; x < templateNums; ++x) {
            int number = dis.readInt();
            String tname = dis.readUTF();
            String desc = dis.readUTF();
            String creator = dis.readUTF();
            AchievementTemplate t = Achievement.getTemplate(number);
            if (t != null) continue;
            new AchievementTemplate(number, tname, false, 1, desc, creator, false, false);
        }
        int nums = dis.readInt();
        Achievements.deleteAllAchievements(wurmId);
        for (int x = 0; x < nums; ++x) {
            int achievement = dis.readInt();
            int counter = dis.readInt();
            long date = dis.readLong();
            Timestamp ts = new Timestamp(date);
            new Achievement(achievement, ts, wurmId, counter, -1).create(true);
        }
    }

    private static final void unpackPMList(PlayerInfo pinf, DataInputStream dis) throws IOException {
        int theCount = dis.readInt();
        for (int x = 0; x < theCount; ++x) {
            String targetName = dis.readUTF();
            long targetId = dis.readLong();
            pinf.addPMTarget(targetName, targetId);
        }
        long sessionFlags = dis.readLong();
        pinf.setSessionFlags(sessionFlags);
    }

    private static final void unpackPrivateMapAnnotations(long playerID, DataInputStream dis) throws IOException {
        MapAnnotation.deletePrivateAnnotationsForOwner(playerID);
        boolean containsAnnotations = dis.readBoolean();
        if (containsAnnotations) {
            int count = dis.readInt();
            for (int i = 0; i < count; ++i) {
                long id = dis.readLong();
                byte type = dis.readByte();
                String name = dis.readUTF();
                String server = dis.readUTF();
                long position = dis.readLong();
                long ownerId = dis.readLong();
                byte icon = dis.readByte();
                MapAnnotation.createNew(id, name, type, position, ownerId, server, icon);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long savePlayerToDisk(byte[] bytes, int posx, int posy, boolean surfaced, boolean newPlayer) {
        long l;
        if (saving) {
            return -10L;
        }
        saving = true;
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new ByteArrayInputStream(bytes));
            if (dis.readBoolean()) {
                long l2 = IntraServerConnection.savePlayerEpicTransfer(dis);
                return l2;
            }
            long wurmId = dis.readLong();
            try {
                Player p = Players.getInstance().getPlayer(wurmId);
                Players.getInstance().logoutPlayer(p);
            }
            catch (NoSuchPlayerException p) {
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
            if (info != null) {
                info.lastLogout = System.currentTimeMillis();
            }
            byte oldChaosKingdom = 0;
            boolean setPremFlag = false;
            if (info != null) {
                oldChaosKingdom = info.getChaosKingdom();
                if (info.isFlagSet(8)) {
                    setPremFlag = true;
                }
            }
            IntraServerConnection.deletePlayer(wurmId);
            Cooldowns.deleteCooldownsFor(wurmId);
            Set<Long> itemIds = Items.loadAllNonTransferredItemsIdsForCreature(wurmId, info);
            Iterator<Long> it = itemIds.iterator();
            while (it.hasNext()) {
                IntraServerConnection.deleteItem(it.next(), info != null && info.hasMovedInventory());
            }
            int numwounds = dis.readInt();
            for (int w = 0; w < numwounds; ++w) {
                WoundMetaData wm = new WoundMetaData(dis.readLong(), dis.readByte(), dis.readByte(), dis.readFloat(), wurmId, dis.readFloat(), dis.readFloat(), dis.readBoolean(), dis.readLong(), dis.readByte());
                wm.save();
            }
            String name = dis.readUTF();
            if (info == null) {
                info = PlayerInfoFactory.createPlayerInfo(name);
                info.loaded = false;
                try {
                    info.load();
                    logger.log(Level.INFO, "Found old player info for the name " + name + ". Deleting old information with wurmid " + info.wurmId + ". New wurmid=" + wurmId);
                    if (info.wurmId > 0L) {
                        IntraServerConnection.deletePlayer(name, info.wurmId);
                        info.wurmId = wurmId;
                        info.loaded = false;
                        info = null;
                        logger.log(Level.INFO, "Player " + name + " deleted. PlayerInfo is null");
                    } else {
                        logger.log(Level.INFO, "Since the player information for " + name + " had wurmid " + info.wurmId + " it was not deleted.");
                    }
                }
                catch (IOException iox) {
                    info = null;
                }
            }
            String password = dis.readUTF();
            String session = dis.readUTF();
            String email = dis.readUTF();
            long sessionExpiration = dis.readLong();
            byte power = dis.readByte();
            byte deity = dis.readByte();
            float align = dis.readFloat();
            float faith = dis.readFloat();
            float favor = dis.readFloat();
            byte god = dis.readByte();
            byte realdeath = dis.readByte();
            long lastChangedDeity = dis.readLong();
            int fatiguesecsleft = dis.readInt();
            int fatigueSecsToday = dis.readInt();
            int fatigueSecsYesterday = dis.readInt();
            long lastfatigue = dis.readLong();
            long lastwarned = dis.readLong();
            long lastcheated = dis.readLong();
            long plantedSign = dis.readLong();
            long playingTime = dis.readLong();
            long creationDate = dis.readLong();
            byte kingdom = dis.readByte();
            boolean votedKing = dis.readBoolean();
            int rank = dis.readInt();
            int maxRank = dis.readInt();
            long lastModifiedRank = dis.readLong();
            boolean banned = dis.readBoolean();
            long banexpiry = dis.readLong();
            String banreason = dis.readUTF();
            short muteTimes = dis.readShort();
            boolean reimbursed = dis.readBoolean();
            int warnings = dis.readInt();
            boolean mayHearDevtalk = dis.readBoolean();
            long paymentExpire = dis.readLong();
            int numignored = dis.readInt();
            long[] ignored = new long[numignored];
            for (int ni = 0; ni < numignored; ++ni) {
                ignored[ni] = dis.readLong();
            }
            if (numignored == 0) {
                ignored = EMPTY_LONG_PRIMITIVE_ARRAY;
            }
            int numfriends = dis.readInt();
            long[] friends = new long[numfriends];
            byte[] friendCats = new byte[numfriends];
            for (int nf = 0; nf < numfriends; ++nf) {
                friends[nf] = dis.readLong();
                friendCats[nf] = dis.readByte();
            }
            if (numfriends == 0) {
                friends = EMPTY_LONG_PRIMITIVE_ARRAY;
                friendCats = EMPTY_BYTE_PRIMITIVE_ARRAY;
            }
            String ipaddress = dis.readUTF();
            long version = dis.readLong();
            boolean dead = dis.readBoolean();
            boolean mute = dis.readBoolean();
            long lastFaith = dis.readLong();
            byte numFaith = dis.readByte();
            long money = dis.readLong();
            boolean climbing = dis.readBoolean();
            byte changedKingdom = dis.readByte();
            long face = dis.readLong();
            byte blood = dis.readByte();
            long flags = dis.readLong();
            long flags2 = dis.readLong();
            long abilities = dis.readLong();
            int scenarioKarma = dis.readInt();
            int abilityTitle = dis.readInt();
            byte chaosKingdom = dis.readByte();
            byte undeadType = dis.readByte();
            int undeadKills = dis.readInt();
            int undeadPKills = dis.readInt();
            int undeadPSecs = dis.readInt();
            long lastResetEarningsCounter = dis.readLong();
            long moneyEarnedBySellingLastHour = dis.readLong();
            long moneyEarnedBySellingEver = dis.readLong();
            int daysPrem = 0;
            long lastTicked = 0L;
            int monthsPaidEver = 0;
            int monthsPaidInARow = 0;
            int monthsPaidSinceReset = 0;
            int silverPaidEver = 0;
            int currentLoyalty = 0;
            int totalLoyalty = 0;
            boolean awards = false;
            if (dis.readBoolean()) {
                awards = true;
                daysPrem = dis.readInt();
                lastTicked = dis.readLong();
                monthsPaidEver = dis.readInt();
                monthsPaidInARow = dis.readInt();
                monthsPaidSinceReset = dis.readInt();
                silverPaidEver = dis.readInt();
                currentLoyalty = dis.readInt();
                totalLoyalty = dis.readInt();
                if (info != null) {
                    info.awards = Awards.getAwards(info.wurmId);
                    if (info.awards != null) {
                        info.awards = new Awards(wurmId, daysPrem, monthsPaidEver, monthsPaidInARow, monthsPaidSinceReset, silverPaidEver, lastTicked, currentLoyalty, totalLoyalty, false);
                        info.awards.update();
                    } else {
                        info.awards = new Awards(wurmId, daysPrem, monthsPaidEver, monthsPaidInARow, monthsPaidSinceReset, silverPaidEver, lastTicked, currentLoyalty, totalLoyalty, true);
                    }
                }
            }
            short hotaWins = dis.readShort();
            boolean hasFreeTransfer = dis.readBoolean();
            int reputation = dis.readInt();
            long lastPolledRep = dis.readLong();
            long pet = dis.readLong();
            if (pet != -10L) {
                if (!Creatures.getInstance().isCreatureOffline(pet)) {
                    try {
                        Creature petcret = Creatures.getInstance().getCreature(pet);
                        if (petcret.dominator != wurmId) {
                            pet = -10L;
                        }
                    }
                    catch (NoSuchCreatureException nsc) {
                        pet = Creatures.getInstance().getPetId(wurmId);
                    }
                }
            } else {
                pet = Creatures.getInstance().getPetId(wurmId);
            }
            long nicotime = dis.readLong();
            long alcotime = dis.readLong();
            float nicotine = dis.readFloat();
            float alcohol = dis.readFloat();
            boolean logging = dis.readBoolean();
            int title = dis.readInt();
            int secondTitle = dis.readInt();
            int numTitles = dis.readInt();
            int[] titleArr = EMPTY_INT_ARRAY;
            if (numTitles > 0) {
                titleArr = new int[numTitles];
                for (int x = 0; x < numTitles; ++x) {
                    titleArr[x] = dis.readInt();
                }
            }
            long muteexpiry = dis.readLong();
            String mutereason = dis.readUTF();
            boolean maymute = dis.readBoolean();
            boolean overRideShop = dis.readBoolean();
            int currentServer = dis.readInt();
            int lastServer = dis.readInt();
            long referrer = dis.readLong();
            String pwQuestion = dis.readUTF();
            String pwAnswer = dis.readUTF();
            boolean isPriest = dis.readBoolean();
            byte priestType = 0;
            long lastChangedPriest = 0L;
            if (isPriest) {
                priestType = dis.readByte();
                lastChangedPriest = dis.readLong();
            }
            if (Servers.localServer.PVPSERVER) {
                BitSet flag2Bits;
                Village cv;
                if (oldChaosKingdom != 0) {
                    chaosKingdom = oldChaosKingdom;
                }
                if (chaosKingdom != 0) {
                    kingdom = chaosKingdom;
                }
                if ((cv = Villages.getVillageForCreature(wurmId)) != null) {
                    kingdom = cv.kingdom;
                }
                if (blood == 0) {
                    blood = IntraServerConnection.calculateBloodFromKingdom(chaosKingdom);
                }
                if (info != null && info.getDeity() != null && info.getDeity().getNumber() == 4 && !(flag2Bits = MiscConstants.createBitSetLong(flags2)).get(11)) {
                    if (deity == 0) {
                        deity = 4;
                    }
                    if (info.getFaith() > faith) {
                        faith = info.getFaith();
                    }
                    if (info.isPriest && !isPriest) {
                        isPriest = info.isPriest;
                    }
                    flag2Bits.set(11);
                    flags2 = MiscConstants.bitSetToLong(flag2Bits);
                }
                if (Deities.getDeity(deity) != null && !QuestionParser.doesKingdomTemplateAcceptDeity(Kingdoms.getKingdomTemplateFor(kingdom), Deities.getDeity(deity))) {
                    if (kingdom == 4) {
                        kingdom = 3;
                    } else {
                        faith = 0.0f;
                        favor = 0.0f;
                        align = 0.0f;
                        deity = 0;
                        isPriest = false;
                    }
                }
            } else if (power <= 0 && !Servers.localServer.PVPSERVER && Servers.localServer.HOMESERVER) {
                BitSet flag2Bits;
                byte by = kingdom = Servers.localServer.getKingdom() != 0 ? (byte)Servers.localServer.getKingdom() : (byte)4;
                if (deity == 4 && info != null && info.getDeity() != null && info.getDeity().getNumber() != 4 && !(flag2Bits = MiscConstants.createBitSetLong(flags2)).get(11)) {
                    if (info.getFaith() > faith) {
                        faith = info.getFaith();
                    }
                    if (info.isPriest && !isPriest) {
                        isPriest = info.isPriest;
                    }
                    flag2Bits.set(11);
                    flags2 = MiscConstants.bitSetToLong(flag2Bits);
                }
            }
            if (blood == 0) {
                blood = IntraServerConnection.calculateBloodFromKingdom(chaosKingdom);
            }
            long bed = dis.readLong();
            int sleep = dis.readInt();
            boolean theftWarned = dis.readBoolean();
            boolean noReimbursmentLeft = dis.readBoolean();
            boolean deathProt = dis.readBoolean();
            byte fightmode = dis.readByte();
            long naffinity = dis.readLong();
            int tutLevel = dis.readInt();
            boolean autof = dis.readBoolean();
            long appoints = dis.readLong();
            boolean seesPAWin = dis.readBoolean();
            boolean isPA = dis.readBoolean();
            boolean mayAppointPA = dis.readBoolean();
            long lastChangedKingdom = dis.readLong();
            float px = (posx << 2) + 2;
            float py = (posy << 2) + 2;
            float posz = 0.0f;
            int zoneId = 0;
            if (!Servers.localServer.LOGINSERVER) {
                try {
                    if (posx > Zones.worldTileSizeX || posx < 0) {
                        posx = Zones.worldTileSizeX / 2;
                    }
                    if (posy > Zones.worldTileSizeY || posy < 0) {
                        posy = Zones.worldTileSizeY / 2;
                    }
                    px = (posx << 2) + 2;
                    py = (posy << 2) + 2;
                    Zone zone = Zones.getZone(posx, posy, surfaced);
                    zoneId = zone.getId();
                    posz = 0.0f;
                    int tile = Server.surfaceMesh.getTile(posx, posy);
                    if (!surfaced) {
                        tile = Server.caveMesh.getTile(posx, posy);
                        posz = Math.max(-1.45f, Tiles.decodeHeightAsFloat(tile));
                    } else {
                        posz = Math.max(-1.45f, Tiles.decodeHeightAsFloat(tile));
                    }
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, "No end zone for " + wurmId + " at " + posx + ", " + posy);
                    long l3 = -1L;
                    saving = false;
                    if (dis != null) {
                        try {
                            dis.close();
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                    return l3;
                }
            }
            long lastLostChampion = dis.readLong();
            short championPoints = dis.readShort();
            float champChanneling = dis.readFloat();
            byte epicKingdom = dis.readByte();
            int epicServerId = dis.readInt();
            int karma = dis.readInt();
            int maxKarma = dis.readInt();
            int totalKarma = dis.readInt();
            String templateName = dis.readUTF();
            short chigh = dis.readShort();
            short clong = dis.readShort();
            short cwide = dis.readShort();
            float rotation = dis.readFloat();
            long bodyId = dis.readLong();
            long buildingId = dis.readLong();
            int damage = dis.readInt();
            int hunger = dis.readInt();
            int stunned = dis.readInt();
            int thirst = dis.readInt();
            int stamina = dis.readInt();
            float nutritionLevel = dis.readFloat();
            byte sex = dis.readByte();
            long inventoryId = dis.readLong();
            boolean onSurface = dis.readBoolean();
            boolean unconscious = dis.readBoolean();
            int age = dis.readInt();
            long lastPolledAge = dis.readLong();
            byte fat = dis.readByte();
            short detectionSecs = dis.readShort();
            byte disease = dis.readByte();
            float calories = dis.readFloat();
            float carbs = dis.readFloat();
            float fats = dis.readFloat();
            float proteins = dis.readFloat();
            if (dis.readBoolean()) {
                IntraServerConnection.readCultist(dis, name, wurmId);
            } else {
                IntraServerConnection.readNullCultist(dis, name, wurmId);
            }
            long lastChangedPath = dis.readLong();
            long lastPuppeteered = dis.readLong();
            if (lastPuppeteered > 0L) {
                Puppet.addPuppetTime(wurmId, lastPuppeteered);
            }
            int numcooldowns = dis.readInt();
            HashMap<Integer, Long> cooldowns = new HashMap<Integer, Long>();
            if (numcooldowns > 0) {
                for (int x = 0; x < numcooldowns; ++x) {
                    cooldowns.put(dis.readInt(), dis.readLong());
                }
            }
            int numItems = dis.readInt();
            HashSet<ItemMetaData> idset = new HashSet<ItemMetaData>();
            for (int x = 0; x < numItems; ++x) {
                IntraServerConnection.createItem(dis, px, py, posz, idset, info != null && info.hasMovedInventory());
            }
            Affinities.deleteAllPlayerAffinity(wurmId);
            int numskills = dis.readInt();
            for (int s = 0; s < numskills; ++s) {
                SkillMetaData sk = new SkillMetaData(dis.readLong(), wurmId, dis.readInt(), dis.readDouble(), dis.readDouble(), dis.readLong());
                if (Servers.localServer.isChallengeServer()) {
                    sk.setChallenge();
                }
                sk.save();
            }
            int numAffinities = dis.readInt();
            for (int xa = 0; xa < numAffinities; ++xa) {
                int skillNumber = dis.readInt();
                int affinity = dis.readByte() & 0xFF;
                if (affinity <= 0) continue;
                Affinities.setAffinity(wurmId, skillNumber, affinity, false);
            }
            int numspeffects = dis.readInt();
            for (int seff = 0; seff < numspeffects; ++seff) {
                new SpellEffectMetaData(dis.readLong(), wurmId, dis.readByte(), dis.readFloat(), dis.readInt(), false).save();
            }
            IntraServerConnection.unpackAchievements(wurmId, dis);
            try {
                RecipesByPlayer.unPackRecipes(dis, wurmId);
            }
            catch (Exception e) {
                logger.warning("Exception unpacking recipes: " + e.getMessage());
                e.printStackTrace();
                logger.warning("Deleting recipes for player to prevent corruption.");
                RecipesByPlayer.deleteRecipesForPlayer(wurmId);
            }
            PlayerMetaData pmd = new PlayerMetaData(wurmId, name, password, session, chigh, clong, cwide, sessionExpiration, power, deity, align, faith, favor, god, realdeath, lastChangedDeity, fatiguesecsleft, lastfatigue, lastwarned, lastcheated, plantedSign, playingTime, kingdom, rank, banned, banexpiry, banreason, reimbursed, warnings, mayHearDevtalk, paymentExpire, ignored, friends, friendCats, templateName, ipaddress, dead, mute, bodyId, buildingId, damage, hunger, stunned, thirst, stamina, sex, inventoryId, surfaced, unconscious, px, py, posz, rotation, zoneId, version, lastFaith, numFaith, money, climbing, changedKingdom, age, lastPolledAge, fat, face, reputation, lastPolledRep, title, secondTitle, titleArr);
            pmd.pet = pet;
            pmd.alcohol = alcohol;
            pmd.alcoholTime = alcotime;
            pmd.nicotine = nicotine;
            pmd.nicotineTime = nicotime;
            pmd.priestType = priestType;
            pmd.lastChangedPriestType = lastChangedPriest;
            pmd.logging = logging;
            pmd.mayMute = maymute;
            pmd.overrideshop = overRideShop;
            pmd.maxRank = maxRank;
            pmd.lastModifiedRank = lastModifiedRank;
            pmd.muteexpiry = muteexpiry;
            pmd.mutereason = mutereason;
            pmd.lastServer = lastServer;
            pmd.currentServer = currentServer;
            pmd.referrer = referrer;
            pmd.pwQuestion = pwQuestion;
            pmd.pwAnswer = pwAnswer;
            pmd.isPriest = isPriest;
            pmd.bed = bed;
            pmd.sleep = sleep;
            pmd.creationDate = creationDate;
            pmd.istheftwarned = theftWarned;
            pmd.noReimbLeft = noReimbursmentLeft;
            pmd.deathProt = deathProt;
            pmd.fatigueSecsToday = fatigueSecsToday;
            pmd.fatigueSecsYday = fatigueSecsYesterday;
            pmd.fightmode = fightmode;
            pmd.nextAffinity = naffinity;
            pmd.detectionSecs = detectionSecs;
            pmd.tutLevel = tutLevel;
            pmd.autofight = autof;
            pmd.appointments = appoints;
            pmd.seesPAWin = seesPAWin;
            pmd.isPA = isPA;
            pmd.mayAppointPA = mayAppointPA;
            pmd.nutrition = nutritionLevel;
            pmd.disease = disease;
            pmd.calories = calories;
            pmd.carbs = carbs;
            pmd.fats = fats;
            pmd.proteins = proteins;
            pmd.cooldowns = cooldowns;
            pmd.lastChangedKingdom = lastChangedKingdom;
            pmd.lastLostChampion = lastLostChampion;
            pmd.championPoints = championPoints;
            pmd.champChanneling = champChanneling;
            pmd.muteTimes = muteTimes;
            pmd.voteKing = votedKing;
            pmd.epicKingdom = epicKingdom;
            pmd.epicServerId = epicServerId;
            pmd.chaosKingdom = chaosKingdom;
            pmd.hotaWins = hotaWins;
            pmd.hasFreeTransfer = hasFreeTransfer;
            pmd.karma = karma;
            pmd.maxKarma = maxKarma;
            pmd.totalKarma = totalKarma;
            if (blood == 0) {
                blood = IntraServerConnection.calculateBloodFromKingdom(kingdom);
            }
            pmd.blood = blood;
            pmd.flags = flags;
            pmd.flags2 = flags2;
            pmd.scenarioKarma = scenarioKarma;
            pmd.abilities = abilities;
            pmd.abilityTitle = abilityTitle;
            pmd.undeadType = undeadType;
            pmd.undeadKills = undeadKills;
            pmd.undeadPKills = undeadPKills;
            pmd.undeadPSecs = undeadPSecs;
            pmd.moneySalesEver = moneyEarnedBySellingEver;
            pmd.daysPrem = daysPrem;
            pmd.lastTicked = lastTicked;
            pmd.currentLoyaltyPoints = currentLoyalty;
            pmd.totalLoyaltyPoints = totalLoyalty;
            pmd.monthsPaidEver = monthsPaidEver;
            pmd.monthsPaidInARow = monthsPaidInARow;
            pmd.monthsPaidSinceReset = monthsPaidSinceReset;
            pmd.silverPaidEver = silverPaidEver;
            pmd.hasAwards = awards;
            if (Servers.isThisLoginServer()) {
                if (info != null) {
                    pmd.paymentExpire = info.getPaymentExpire();
                    pmd.emailAdress = info.emailAddress.length() == 0 ? email : info.emailAddress;
                    pmd.password = info.getPassword();
                    if (pmd.money != info.money) {
                        logger.log(Level.INFO, "Setting money for " + info.getName() + " to " + info.money + " instead of " + pmd.money);
                    }
                    pmd.money = info.money;
                } else {
                    pmd.emailAdress = email;
                }
                pmd.save();
            } else {
                pmd.emailAdress = email;
                pmd.save();
            }
            logger.log(Level.INFO, "has info:" + (info != null));
            if (info != null) {
                IntraServerConnection.unpackPMList(info, dis);
                IntraServerConnection.unpackPrivateMapAnnotations(info.getPlayerId(), dis);
                if (!password.equals(info.getPassword())) {
                    logger.log(Level.WARNING, name + " after transfer but before loading: password now is " + info.getPassword() + ". Sent " + password);
                }
                info.loaded = false;
                try {
                    info.load();
                    boolean updateFlags = false;
                    if (info.flags != flags) {
                        info.flags = flags;
                        info.setFlagBits(info.flags);
                        if (setPremFlag) {
                            info.setFlag(8, true);
                        }
                        updateFlags = true;
                    }
                    if (info.flags2 != flags2) {
                        info.flags2 = flags2;
                        info.setFlag2Bits(info.flags2);
                        updateFlags = true;
                    }
                    if (updateFlags) {
                        info.forceFlagsUpdate();
                    }
                    if (!password.equals(info.getPassword())) {
                        logger.log(Level.WARNING, name + " after transfer: password now is " + info.getPassword() + "  Sent " + password);
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage());
                }
                info.setMoneyEarnedBySellingLastHour(moneyEarnedBySellingLastHour);
                info.setLastResetEarningsCounter(lastResetEarningsCounter);
                if (lastChangedPath > info.getLastChangedPath()) {
                    info.setLastChangedPath(lastChangedPath);
                }
            }
            if (draggedItem >= 0L) {
                try {
                    Item d = Items.getItem(draggedItem);
                    try {
                        Zone z = Zones.getZone((int)d.getPosX() >> 2, (int)d.getPosY() >> 2, true);
                        z.addItem(d);
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, nsz.getMessage(), nsz);
                    }
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Weird. No dragged item " + draggedItem + " after it was saved.");
                }
                draggedItem = -10L;
            }
            if (newPlayer) {
                logger.log(Level.FINE, name + " created successfully.");
            } else {
                logger.log(Level.FINE, name + " unpacked successfully.");
            }
            Village v = Villages.getVillageForCreature(wurmId);
            if (v != null && v.kingdom != kingdom) {
                if (v.getMayor().getId() != wurmId) {
                    Citizen c = v.getCitizen(wurmId);
                    v.removeCitizen(c);
                } else if (Servers.localServer.HOMESERVER) {
                    v.startDisbanding(null, name, wurmId);
                }
            }
            saving = false;
            l = wurmId;
        }
        catch (IOException ex) {
            saving = false;
            logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
            long l4 = -1L;
            return l4;
        }
        finally {
            saving = false;
            if (dis != null) {
                try {
                    dis.close();
                }
                catch (IOException iOException) {}
            }
        }
        return l;
    }

    public static final void resetTransferVariables(String playerName) {
        logger.log(Level.INFO, playerName + " resetting transfer data");
        lastItemName = "unknown";
        lastItemId = -10L;
    }

    public static void createItem(DataInputStream dis, float posx, float posy, float posz, Set<ItemMetaData> metadataset, boolean frozen) throws IOException {
        boolean hasInscription;
        boolean ok;
        try {
            boolean isStoredAnimalItem = dis.readBoolean();
            if (isStoredAnimalItem) {
                CreatureDataStream.fromStream(dis);
            }
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Exception", e);
        }
        boolean locked = dis.readBoolean();
        long lockid = dis.readLong();
        if (lockid != -10L && (ok = dis.readBoolean())) {
            IntraServerConnection.createItem(dis, posx, posy, posz, metadataset, frozen);
        }
        long itemId = dis.readLong();
        IntraServerConnection.deleteItem(itemId, frozen);
        boolean dragged = dis.readBoolean();
        if (dragged) {
            draggedItem = itemId;
        }
        int numEffects = dis.readInt();
        for (int e = 0; e < numEffects; ++e) {
            new EffectMetaData(itemId, dis.readShort(), 0.0f, 0.0f, 0.0f, dis.readLong()).save();
        }
        int numspeffects = dis.readInt();
        for (int seff = 0; seff < numspeffects; ++seff) {
            new SpellEffectMetaData(dis.readLong(), itemId, dis.readByte(), dis.readFloat(), dis.readInt(), true).save();
        }
        int numKeys = dis.readInt();
        long[] keyids = EMPTY_LONG_PRIMITIVE_ARRAY;
        if (numKeys > 0) {
            keyids = new long[numKeys];
            for (int k = 0; k < numKeys; ++k) {
                keyids[k] = dis.readLong();
            }
        }
        long lastowner = dis.readLong();
        int data1 = dis.readInt();
        int data2 = dis.readInt();
        int extra1 = dis.readInt();
        int extra2 = dis.readInt();
        if (data1 != -1 || data2 != -1 || extra1 != -1 || extra2 != -1) {
            ItemData d = new ItemData(itemId, data1, data2, extra1, extra2);
            try {
                d.createDataEntry(DbConnector.getItemDbCon());
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
        }
        String itname = dis.readUTF();
        if (Servers.isThisATestServer()) {
            logger.log(Level.INFO, "Creating " + itname + ", " + itemId);
        }
        String desc = dis.readUTF();
        long ownerId = dis.readLong();
        long parentId = dis.readLong();
        long lastmaintained = dis.readLong();
        float ql = dis.readFloat();
        float itemdam = dis.readFloat();
        float origQl = dis.readFloat();
        int itemtemplateId = dis.readInt();
        int weight = dis.readInt();
        short place = dis.readShort();
        int sizex = dis.readInt();
        int sizey = dis.readInt();
        int sizez = dis.readInt();
        int bless = dis.readInt();
        byte enchantment = dis.readByte();
        byte material = dis.readByte();
        int price = dis.readInt();
        short temp = dis.readShort();
        boolean banked = dis.readBoolean();
        byte auxdata = dis.readByte();
        long creationDate = dis.readLong();
        byte creationState = dis.readByte();
        int realTemplate = dis.readInt();
        boolean hasMoreItems = dis.readBoolean();
        if (hasMoreItems) {
            ItemRequirement.deleteRequirements(itemId);
            int nums = dis.readInt();
            for (int xa = 0; xa < nums; ++xa) {
                int templateId = dis.readInt();
                int numsDone = dis.readInt();
                ItemRequirement.setRequirements(itemId, templateId, numsDone, true, true);
            }
        }
        boolean wornAsArmour = dis.readBoolean();
        boolean female = dis.readBoolean();
        boolean mailed = dis.readBoolean();
        byte mailTimes = dis.readByte();
        byte rarity = dis.readByte();
        long onBridge = dis.readLong();
        int settings = dis.readInt();
        int numPermissions = dis.readInt();
        ItemSettings.remove(itemId);
        LinkedList<String> added = new LinkedList<String>();
        for (int p = 0; p < numPermissions; ++p) {
            long pId = dis.readLong();
            int pSettings = dis.readInt();
            ItemSettings.addPlayer(itemId, pId, pSettings);
            String pName = PermissionsByPlayer.getPlayerOrGroupName(pId);
            BitSet permissionBits = new BitSet(32);
            for (int x = 0; x < 32; ++x) {
                if ((pSettings >>> x & 1) != 1) continue;
                permissionBits.set(x);
            }
            LinkedList<String> perms = new LinkedList<String>();
            if (permissionBits.get(ItemSettings.ItemPermissions.MANAGE.getBit())) {
                perms.add("+Manage");
            }
            if (permissionBits.get(ItemSettings.VehiclePermissions.COMMANDER.getBit())) {
                perms.add("+Commander");
            }
            if (permissionBits.get(ItemSettings.VehiclePermissions.PASSENGER.getBit())) {
                perms.add("+Passenger");
            }
            if (permissionBits.get(ItemSettings.VehiclePermissions.ACCESS_HOLD.getBit())) {
                perms.add("+Access Hold");
            }
            if (permissionBits.get(ItemSettings.BedPermissions.MAY_USE_BED.getBit())) {
                perms.add("+Sleep");
            }
            if (permissionBits.get(ItemSettings.BedPermissions.FREE_SLEEP.getBit())) {
                perms.add("+Free Sleep");
            }
            if (permissionBits.get(ItemSettings.MessageBoardPermissions.MAY_POST_NOTICES.getBit())) {
                perms.add("+Add Notices");
            }
            if (permissionBits.get(ItemSettings.MessageBoardPermissions.MAY_ADD_PMS.getBit())) {
                perms.add("+Add PMs");
            }
            if (permissionBits.get(ItemSettings.VehiclePermissions.DRAG.getBit())) {
                perms.add("+Drag");
            }
            if (permissionBits.get(ItemSettings.VehiclePermissions.EXCLUDE.getBit())) {
                perms.add("+Deny All");
            }
            added.add(pName + "(" + String.join((CharSequence)", ", perms) + ")");
        }
        if (!added.isEmpty()) {
            String stuffAdded = "Imported " + String.join((CharSequence)", ", added);
            PermissionsHistories.addHistoryEntry(itemId, System.currentTimeMillis(), -10L, "Transfered", stuffAdded);
        }
        if (hasInscription = dis.readBoolean()) {
            InscriptionData insdata = new InscriptionData(itemId, dis.readUTF(), dis.readUTF(), 0);
            try {
                insdata.createInscriptionEntry(DbConnector.getItemDbCon());
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
        }
        int color = dis.readInt();
        int color2 = dis.readInt();
        String creator = dis.readUTF();
        Itempool.deleteItem(itemtemplateId, itemId);
        if (dis.readBoolean()) {
            short calories = dis.readShort();
            short carbs = dis.readShort();
            short fats = dis.readShort();
            short proteins = dis.readShort();
            byte bonus = dis.readByte();
            byte stages = dis.readByte();
            byte ingredients = dis.readByte();
            short recipeId = dis.readShort();
            ItemMealData.save(itemId, recipeId, calories, carbs, fats, proteins, bonus, stages, ingredients);
        }
        ItemMetaData imd = new ItemMetaData(locked, lockid, itemId, keyids, lastowner, data1, data2, extra1, extra2, itname, desc, ownerId, parentId, lastmaintained, ql, itemdam, origQl, itemtemplateId, weight, sizex, sizey, sizez, bless, enchantment, material, price, temp, banked, auxdata, creationDate, creationState, realTemplate, wornAsArmour, color, color2, place, posx, posy, posz, creator, female, mailed, mailTimes, rarity, onBridge, hasInscription, settings, frozen);
        imd.save();
        metadataset.add(imd);
        lastItemName = itname;
        lastItemId = itemId;
    }

    private void validateTransferRequest(ByteBuffer byteBuffer) {
        boolean isDev;
        if (this.wurmserver.isLagging()) {
            try {
                this.sendTransferUserRequestAnswer(false, "The server is lagging. Try later.", 10);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
        if (Constants.maintaining) {
            try {
                this.sendTransferUserRequestAnswer(false, "The server is in maintenance mode.", 0);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
        boolean bl = isDev = byteBuffer.get() != 0;
        if (!isDev && Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit) {
            try {
                this.sendTransferUserRequestAnswer(false, "The server is full. Try later", 30);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
    }

    private void validate(ByteBuffer byteBuffer) {
        int protocolVersion = byteBuffer.getInt();
        if (protocolVersion != 1) {
            try {
                this.sendLoginAnswer(false, "You are using an old protocol.\nPlease update the server.", 0);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
        byte[] bytes = new byte[byteBuffer.get() & 0xFF];
        byteBuffer.get(bytes);
        boolean dev = byteBuffer.get() == 1;
        String password = "Unknown";
        try {
            password = new String(bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException nse) {
            password = new String(bytes);
            logger.log(Level.WARNING, "Unsupported encoding for password.", nse);
        }
        if (!password.equals(Servers.localServer.INTRASERVERPASSWORD)) {
            try {
                this.sendLoginAnswer(false, "Wrong password: " + password, 0);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
        try {
            this.sendLoginAnswer(true, "ok" + Server.rand.nextInt(1000000), 0);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private void sendLoginAnswer(boolean ok, String message, int retrySeconds) throws IOException {
        try {
            byte[] messageb = message.getBytes("UTF-8");
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)2);
            if (ok) {
                bb.put((byte)1);
            } else {
                bb.put((byte)0);
            }
            bb.putShort((short)messageb.length);
            bb.put(messageb);
            bb.putShort((short)retrySeconds);
            bb.putLong(System.currentTimeMillis());
            this.conn.flush();
            if (!ok) {
                this.conn.ticksToDisconnect = 200;
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send login answer.", ex);
        }
    }

    private void sendTransferUserRequestAnswer(boolean ok, String sessionKey, int retrySeconds) throws IOException {
        try {
            byte[] messageb = sessionKey.getBytes("UTF-8");
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)6);
            if (ok) {
                bb.put((byte)1);
            } else {
                bb.put((byte)0);
            }
            bb.putShort((short)messageb.length);
            bb.put(messageb);
            bb.putShort((short)retrySeconds);
            bb.putLong(System.currentTimeMillis());
            this.conn.flush();
            if (!ok) {
                this.conn.ticksToDisconnect = 200;
            }
            logger.log(Level.INFO, "Intraserver sent transferrequestanswer. " + ok);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send TransferUserRequest answer.", ex);
        }
    }

    private void sendCommandDone() throws IOException {
        try {
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)4);
            this.conn.flush();
            this.conn.ticksToDisconnect = 200;
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send command done.", ex);
        }
    }

    private void sendCommandFailed() throws IOException {
        logger.log(Level.WARNING, "Command failed : ", new Exception());
        try {
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)5);
            this.conn.flush();
            this.conn.ticksToDisconnect = 200;
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send command failed.", ex);
        }
    }

    private void sendDataReceived() throws IOException {
        try {
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)8);
            this.conn.flush();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send DataReceived.", ex);
        }
    }

    private void sendTimeSync() throws IOException {
        try {
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)10);
            bb.putLong(WurmCalendar.currentTime);
            this.conn.flush();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send timesync.", ex);
        }
        this.conn.ticksToDisconnect = 200;
    }

    private void sendPlayerVersion(long playerversion) throws IOException {
        try {
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)9);
            bb.putLong(playerversion);
            this.conn.flush();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send player version.", ex);
        }
        this.conn.ticksToDisconnect = 200;
    }

    private void sendPlayerPaymentExpire(long paymentExpire) throws IOException {
        try {
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)11);
            bb.putLong(paymentExpire);
            this.conn.flush();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send expiretime.", ex);
            this.sendCommandFailed();
        }
        this.conn.ticksToDisconnect = 200;
    }

    private void sendPingAnswer() throws IOException {
        try {
            ByteBuffer bb = this.conn.getBuffer();
            if (Server.getMillisToShutDown() > -1000L && Server.getMillisToShutDown() < 120000L) {
                bb.put((byte)14);
            } else {
                bb.put((byte)13);
                if (Constants.maintaining) {
                    bb.put((byte)1);
                } else {
                    bb.put((byte)0);
                }
                bb.putInt(Players.getInstance().getNumberOfPlayers());
                bb.putInt(Servers.localServer.pLimit);
                if (Server.getMillisToShutDown() > 0L) {
                    bb.putInt(Math.max(1, (int)(Server.getMillisToShutDown() / 1000L)));
                } else {
                    bb.putInt(0);
                }
                bb.putInt(Constants.meshSize);
            }
            this.conn.flush();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send ping answer.", ex);
            this.sendCommandFailed();
        }
    }
}


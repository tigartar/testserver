/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  sun.misc.BASE64Encoder
 */
package com.wurmonline.server;

import com.wurmonline.communication.SimpleConnectionListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Groups;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.Team;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.VehicleBehaviour;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.HackerIp;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.KingdomIp;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerCommunicator;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.SelectSpawnQuestion;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.ProtocolUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WCGmMessage;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import sun.misc.BASE64Encoder;

public final class LoginHandler
implements SimpleConnectionListener,
TimeConstants,
MiscConstants,
CreatureTemplateIds,
ProtoConstants,
CounterTypes {
    private final SocketConnection conn;
    private static Logger logger = Logger.getLogger(LoginHandler.class.getName());
    public static final String legalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int DISCONNECT_TICKS = 400;
    private int loadedItems = 0;
    private boolean redirected = false;
    private static final byte clientDevLevel = 2;
    static int redirects = 0;
    static int logins = 0;
    private static final String BROKEN_PLAYER_MODEL = "model.player.broken";
    private static final String MESSAGE_FORMAT_UTF_8 = "UTF-8";
    private static final String PROBLEM_SENDING_LOGIN_DENIED_MESSAGE = ", problem sending login denied message: ";
    public static final Map<String, HackerIp> failedIps = new HashMap<String, HackerIp>();
    private static final int MAX_REAL_DEATHS = 4;
    private static final int MAX_NAME_LENGTH = 40;
    private static final int MIN_NAME_LENGTH = 3;
    private static final int ITERATIONS = 1000;
    private static final int KEY_LENGTH = 192;

    public LoginHandler(SocketConnection aConn) {
        this.conn = aConn;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Creating LoginHandler for SocketConnection " + aConn);
        }
    }

    public static final boolean containsIllegalCharacters(String name) {
        char[] chars;
        for (char lC : chars = name.toCharArray()) {
            if (legalChars.indexOf(lC) >= 0) continue;
            return true;
        }
        return false;
    }

    public static final String raiseFirstLetter(String oldString) {
        if (oldString.length() == 0) {
            return oldString;
        }
        String lOldString = oldString.toLowerCase();
        String firstLetter = lOldString.substring(0, 1).toUpperCase();
        String newString = firstLetter + lOldString.substring(1, lOldString.length());
        return newString;
    }

    @Override
    public void reallyHandle(int num, ByteBuffer byteBuffer) {
        short cmd = byteBuffer.get();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Handling block with Command: " + cmd + ", " + ProtocolUtilities.getDescriptionForCommand((byte)cmd));
        }
        if (cmd == -15 || cmd == 23) {
            int protocolVersion = byteBuffer.getInt();
            if (protocolVersion != 250990585) {
                block19: {
                    String message = "Incompatible communication protocol.\nPlease update the client at http://www.wurmonline.com or wait for the server to be updated.";
                    logger.log(Level.INFO, "Rejected protocol " + protocolVersion + ". Mine=" + 250990585 + ", (" + "0xEF5CFF9s" + ") " + this.conn);
                    try {
                        this.sendLoginAnswer(false, "Incompatible communication protocol.\nPlease update the client at http://www.wurmonline.com or wait for the server to be updated.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                    }
                    catch (IOException ioe) {
                        if (!logger.isLoggable(Level.FINE)) break block19;
                        logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "Incompatible communication protocol.\nPlease update the client at http://www.wurmonline.com or wait for the server to be updated.", ioe);
                    }
                }
                return;
            }
            String name = this.getNextString(byteBuffer, "name", true);
            name = LoginHandler.raiseFirstLetter(name);
            String password = this.getNextString(byteBuffer, "password for " + name, false);
            String serverPassword = this.getNextString(byteBuffer, "server password for " + name, false);
            String steamIDAsString = this.getNextString(byteBuffer, "steamid for " + name, false);
            boolean sendExtraBytes = false;
            if (byteBuffer.hasRemaining()) {
                boolean bl = sendExtraBytes = byteBuffer.get() != 0;
            }
            if (cmd == 23) {
                this.reconnect(name, password, false, serverPassword, steamIDAsString);
            } else {
                this.login(name, password, sendExtraBytes, false, serverPassword, steamIDAsString);
            }
        } else if (cmd == -52) {
            try {
                String steamIDAsString = this.getNextString(byteBuffer, "steamid", false);
                long authTicket = byteBuffer.getLong();
                int arrayLenght = byteBuffer.getInt();
                byte[] ticketArray = new byte[arrayLenght];
                for (int i = 0; i < ticketArray.length; ++i) {
                    ticketArray[i] = byteBuffer.get();
                }
                long tokenLen = byteBuffer.getLong();
                if (Server.getInstance().steamHandler.getIsOfflineServer()) {
                    Server.getInstance().steamHandler.setIsPlayerAuthenticated(steamIDAsString);
                    this.sendAuthenticationAnswer(true, "");
                    return;
                }
                boolean wasAddedBeforeWithSameIp = Server.getInstance().steamHandler.addLoginHandler(steamIDAsString, this);
                int authenticationResult = Server.getInstance().steamHandler.BeginAuthSession(steamIDAsString, ticketArray, tokenLen);
                if (authenticationResult != 0) {
                    if (authenticationResult == 2) {
                        if (!wasAddedBeforeWithSameIp) {
                            logger.log(Level.INFO, "Duplicate authentication");
                            this.sendAuthenticationAnswer(false, "Duplicate authentication");
                        } else {
                            Server.getInstance().steamHandler.setIsPlayerAuthenticated(steamIDAsString);
                            this.sendAuthenticationAnswer(true, "");
                        }
                    } else {
                        logger.log(Level.INFO, "Steam could not authenticate the user");
                        this.sendAuthenticationAnswer(false, "Steam could not authenticate the user");
                    }
                }
            }
            catch (Throwable t) {
                logger.log(Level.SEVERE, "Error while authenticating the user with steam.");
                this.sendAuthenticationAnswer(false, "Error while authenticating the user with steam.");
            }
        }
    }

    private String getNextString(ByteBuffer byteBuffer, String name, boolean logValue) {
        String decoded;
        byte[] bytes = new byte[byteBuffer.get() & 0xFF];
        byteBuffer.get(bytes);
        try {
            decoded = new String(bytes, MESSAGE_FORMAT_UTF_8);
        }
        catch (UnsupportedEncodingException nse) {
            decoded = new String(bytes);
            String logMessage = "Unsupported encoding for " + (logValue ? name + ": " + decoded : name);
            logger.log(Level.WARNING, logMessage, nse);
        }
        return decoded;
    }

    private void login(String name, String password, boolean sendExtraBytes, boolean isUndead, String serverPassword, String steamIDAsString) {
        try {
            password = LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(name)));
        }
        catch (Exception ex) {
            block11: {
                logger.log(Level.SEVERE, name + " Failed to encrypt password", ex);
                String message = "We failed to encrypt your password. Please try another.";
                try {
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    this.sendLoginAnswer(false, "We failed to encrypt your password. Please try another.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                }
                catch (IOException ioe) {
                    if (!logger.isLoggable(Level.FINE)) break block11;
                    logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "We failed to encrypt your password. Please try another.", ioe);
                }
            }
            return;
        }
        try {
            if (!(Servers.localServer.LOGINSERVER || Constants.maintaining || isUndead || Servers.localServer.testServer)) {
                block12: {
                    logger.log(Level.WARNING, name + " logging in directly! Rejected.");
                    String message = "You need to connect to the login server.";
                    try {
                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                        this.sendLoginAnswer(false, "You need to connect to the login server.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                    }
                    catch (IOException ioe) {
                        if (!logger.isLoggable(Level.FINE)) break block12;
                        logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "You need to connect to the login server.", ioe);
                    }
                }
                return;
            }
            this.handleLogin(name, password, sendExtraBytes, false, false, isUndead, serverPassword, steamIDAsString);
        }
        catch (Exception ex) {
            block13: {
                logger.log(Level.SEVERE, "Failed to log " + name + " due to an Exception: " + ex.getMessage(), ex);
                String message = "We failed to log you in. " + ex.getMessage();
                try {
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                }
                catch (IOException ioe) {
                    if (!logger.isLoggable(Level.FINE)) break block13;
                    logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + message, ioe);
                }
            }
            return;
        }
    }

    private void reconnect(String name, String sessionkey, boolean isUndead, String serverPassword, String steamIDAsString) {
        this.redirected = true;
        try {
            this.handleLogin(name, sessionkey, false, false, true, isUndead, serverPassword, steamIDAsString);
        }
        catch (Exception ex) {
            block4: {
                logger.log(Level.SEVERE, name + " " + ex.getMessage(), ex);
                String message = "We failed to log you in. " + ex.getMessage();
                try {
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                }
                catch (IOException ioe) {
                    if (!logger.isLoggable(Level.FINE)) break block4;
                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + message, ioe);
                }
            }
            return;
        }
    }

    private boolean preValidateLogin(String name, String steamIDAsString) {
        if (Server.getInstance().isLagging()) {
            block3: {
                logger.log(Level.INFO, "Refusing connection due to lagging server for " + name);
                String message = "The server is lagging. Retrying in 20 seconds.";
                try {
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    this.sendLoginAnswer(false, "The server is lagging. Retrying in 20 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 20);
                }
                catch (IOException ioe) {
                    if (!logger.isLoggable(Level.FINE)) break block3;
                    logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "The server is lagging. Retrying in 20 seconds.", ioe);
                }
            }
            return false;
        }
        return true;
    }

    /*
     * WARNING - void declaration
     */
    private void handleLogin(String name, String password, boolean sendExtraBytes, boolean usingWeb, boolean reconnecting, boolean isUndead, String serverPassword, String steamIDAsString) {
        if (!this.preValidateLogin(name, steamIDAsString)) {
            return;
        }
        String hashedSteamId = steamIDAsString;
        try {
            hashedSteamId = LoginHandler.hashPassword(hashedSteamId, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(name)));
        }
        catch (Exception ex) {
            block216: {
                logger.log(Level.SEVERE, name + " Failed to encrypt password", ex);
                String message = "We failed to encrypt your password. Please try another.";
                try {
                    Server.getInstance().steamHandler.removeIsPlayerAuthenticated(steamIDAsString);
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    this.sendLoginAnswer(false, "We failed to encrypt your password. Please try another.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                }
                catch (IOException ioe) {
                    if (!logger.isLoggable(Level.FINE)) break block216;
                    logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "We failed to encrypt your password. Please try another.", ioe);
                }
            }
            return;
        }
        if (!Server.getInstance().steamHandler.isPlayerAuthenticated(steamIDAsString)) {
            Server.getInstance().steamHandler.removeIsPlayerAuthenticated(steamIDAsString);
            try {
                String message = "You need to be authenticated";
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -2);
            }
            catch (IOException message) {
                // empty catch block
            }
            if (!password.equals(hashedSteamId)) {
                logger.log(Level.INFO, "Unauthenticated user trying to login with incorrect credentials, with ip: " + this.conn.getIp());
            } else {
                logger.log(Level.INFO, "Unauthenticated user trying to login, with ip: " + this.conn.getIp());
            }
            return;
        }
        Server.getInstance().steamHandler.removeIsPlayerAuthenticated(steamIDAsString);
        if (!password.equals(hashedSteamId)) {
            try {
                String message = "You need to be authenticated";
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -2);
            }
            catch (IOException message) {
                // empty catch block
            }
            logger.log(Level.INFO, "Authenticated user trying to login with incorrect credentials, with ip: " + this.conn.getIp());
            return;
        }
        String steamServerPassword = Servers.localServer.getSteamServerPassword();
        if (!steamServerPassword.equals(serverPassword)) {
            try {
                String message = "Incorrect server password!";
                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -2);
            }
            catch (IOException message) {
                // empty catch block
            }
            logger.log(Level.INFO, "Incorrect server password: " + this.conn.getIp());
            return;
        }
        if (this.checkName(name)) {
            if (isUndead && !Servers.localServer.LOGINSERVER) {
                name = "Undead " + name;
            }
            boolean wasinvuln = false;
            try {
                Player p = Players.getInstance().getPlayer(name);
                p.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString)));
                String dbpassw = "";
                dbpassw = p.getSaveFile().getPassword();
                if (!reconnecting) {
                    p.setSendExtraBytes(sendExtraBytes);
                }
                if (dbpassw.equals(password) || sendExtraBytes) {
                    Item item;
                    Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), p, steamIDAsString);
                    if (ban != null) {
                        String string = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                        String message = ban.getIdentifier() + " is banned for " + string + " more. Reason: " + ban.getReason();
                        if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                            message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                        }
                        try {
                            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                            this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                        logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
                        return;
                    }
                    if (!p.isFullyLoaded() || p.isLoggedOut()) {
                        try {
                            Zone zone = Zones.getZone(p.getTileX(), p.getTileY(), p.isOnSurface());
                            zone.deleteCreature(p, true);
                        }
                        catch (NoSuchPlayerException | NoSuchCreatureException | NoSuchZoneException wurmServerException) {
                            // empty catch block
                        }
                        Players.getInstance().logoutPlayer(p);
                        logger.log(Level.INFO, this.conn.getIp() + "," + name + " logged on too early after reconnecting.");
                        return;
                    }
                    if (p.getCommunicator().getCurrentmove() != null && p.getCommunicator().getCurrentmove().getNext() != null) {
                        block217: {
                            logger.log(Level.INFO, this.conn.getIp() + "," + name + " was still moving at reconnect - " + p.getCommunicator().getMoves());
                            String string = "You are still moving on the server. Retry in 10 seconds.";
                            try {
                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                this.sendLoginAnswer(false, "You are still moving on the server. Retry in 10 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 10);
                            }
                            catch (IOException ioe) {
                                if (!logger.isLoggable(Level.FINE)) break block217;
                                logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "You are still moving on the server. Retry in 10 seconds.", ioe);
                            }
                        }
                        return;
                    }
                    if (p.getSaveFile().realdeath > 4) {
                        block218: {
                            String string = "Your account has suffered real death. You can not log on.";
                            try {
                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                this.sendLoginAnswer(false, "Your account has suffered real death. You can not log on.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                            }
                            catch (IOException ioe) {
                                if (!logger.isLoggable(Level.FINE)) break block218;
                                logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "Your account has suffered real death. You can not log on.", ioe);
                            }
                        }
                        return;
                    }
                    logger.log(Level.INFO, this.conn.getIp() + "," + name + " successfully reconnected.");
                    wasinvuln = p.getCommunicator().isInvulnerable();
                    p.getCommunicator().sendShutDown("Reconnected", true);
                    p.stopTeleporting();
                    p.getMovementScheme().clearIntraports();
                    p.getCommunicator().player = null;
                    p.setCommunicator(new PlayerCommunicator(p, this.conn));
                    this.conn.setLogin(true);
                    if (p.getSaveFile().currentServer != Servers.localServer.id) {
                        p.getSaveFile().setLogin();
                        p.getSaveFile().logout();
                        String string = "Failed to redirect to another server.";
                        logger.log(Level.INFO, this.conn.getIp() + "," + name + " redirected from " + Servers.localServer.id + " to " + p.getSaveFile().currentServer);
                        try {
                            ServerEntry entry = Servers.getServerWithId(p.getSaveFile().currentServer);
                            if (entry != null) {
                                if (entry.isAvailable(p.getPower(), p.isReallyPaying())) {
                                    p.getCommunicator().sendReconnect(entry.EXTERNALIP, Integer.parseInt(entry.EXTERNALPORT), password);
                                } else {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    String string2 = "The server is currently not available. Please try later.";
                                    this.sendLoginAnswer(false, string2, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 300);
                                }
                            } else {
                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                this.sendLoginAnswer(false, string, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                            }
                        }
                        catch (IOException entry) {
                            // empty catch block
                        }
                        Players.getInstance().logoutPlayer(p);
                        ++redirects;
                        this.conn.ticksToDisconnect = 400;
                        return;
                    }
                    ++logins;
                    p.setLoginHandler(this);
                    this.conn.setConnectionListener(p.getCommunicator());
                    Server.getInstance().addIp(this.conn.getIp());
                    p.setIpaddress(this.conn.getIp());
                    if (p.isTransferring()) {
                        block219: {
                            String string = "You are being transferred to another server.";
                            try {
                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                this.sendLoginAnswer(false, "You are being transferred to another server.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                            }
                            catch (IOException ioe) {
                                if (!logger.isLoggable(Level.FINE)) break block219;
                                logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "You are being transferred to another server.", ioe);
                            }
                        }
                        return;
                    }
                    if (p.getPower() < 1 && !p.isPaying() && Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit) {
                        block220: {
                            String string = "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.";
                            try {
                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                this.sendLoginAnswer(false, "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                            }
                            catch (IOException ioe) {
                                if (!logger.isLoggable(Level.FINE)) break block220;
                                logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", ioe);
                            }
                        }
                        return;
                    }
                    if (Constants.maintaining && p.getPower() <= 1) {
                        block221: {
                            String string = "The server is in maintenance mode. Retrying in 60 seconds.";
                            try {
                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                this.sendLoginAnswer(false, "The server is in maintenance mode. Retrying in 60 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                            }
                            catch (IOException ioe) {
                                if (!logger.isLoggable(Level.FINE)) break block221;
                                logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "The server is in maintenance mode. Retrying in 60 seconds.", ioe);
                            }
                        }
                        return;
                    }
                    p.setLink(true);
                    if (!p.isDead()) {
                        p.setNewTile(null, 0.0f, true);
                    }
                    if (p.isTeleporting()) {
                        p.cancelTeleport();
                    }
                    LoginHandler.putOutsideWall(p);
                    if (p.isOnSurface()) {
                        LoginHandler.putOutsideFence(p);
                        if (!p.isDead() && LoginHandler.creatureIsInsideWrongHouse(p, false)) {
                            LoginHandler.putOutsideHouse(p, false);
                        }
                    }
                    p.sendAddChampionPoints();
                    p.getCommunicator().sendWeather();
                    p.getCommunicator().checkSendWeather();
                    p.getCommunicator().sendSleepInfo();
                    if (!p.isDead()) {
                        LoginHandler.putInBoatAndAssignSeat(p, true);
                        try {
                            Zone zone = Zones.getZone(p.getTileX(), p.getTileY(), p.isOnSurface());
                            zone.addCreature(p.getWurmId());
                        }
                        catch (NoSuchZoneException noSuchZoneException) {
                            logger.log(Level.WARNING, noSuchZoneException.getMessage(), noSuchZoneException);
                            p.logoutIn(2, "You were out of bounds.");
                            return;
                        }
                        catch (NoSuchPlayerException | NoSuchCreatureException wurmServerException) {
                            logger.log(Level.WARNING, wurmServerException.getMessage(), wurmServerException);
                            p.logoutIn(2, "A server error occurred.");
                            return;
                        }
                    }
                    try {
                        byte by;
                        Seat s;
                        Vehicle vehic;
                        String string = "Reconnecting " + name + "! " + (Servers.localServer.hasMotd() ? Servers.localServer.getMotd() : Constants.motd);
                        float posx = p.getStatus().getPositionX();
                        float posy = p.getStatus().getPositionY();
                        boolean bl = false;
                        if (p.isVehicleCommander() && (vehic = Vehicles.getVehicleForId(p.getVehicle())) != null && (s = vehic.getPilotSeat()) != null && s.occupant == p.getWurmId()) {
                            float posz = p.getStatus().getPositionZ();
                            try {
                                Structure structure;
                                VolaTile tile = Zones.getOrCreateTile((int)(p.getPosX() / 4.0f), (int)(p.getPosY() / 4.0f), p.getLayer() >= 0);
                                boolean skipSetZ = false;
                                if (tile != null && (structure = tile.getStructure()) != null) {
                                    boolean bl2 = skipSetZ = structure.isTypeHouse() || structure.getWurmId() == p.getBridgeId();
                                }
                                if (!skipSetZ) {
                                    posz = Zones.calculateHeight(p.getStatus().getPositionX(), p.getStatus().getPositionY(), p.isOnSurface());
                                }
                                if (posz < 0.0f) {
                                    posz = Math.max(-1.45f, posz);
                                }
                                p.getStatus().setPositionZ(posz);
                            }
                            catch (NoSuchZoneException tile) {
                                // empty catch block
                            }
                            by = vehic.commandType;
                            posz = Math.max(posz + s.offz, s.offz);
                            posy += s.offy;
                            posx += s.offx;
                            p.getStatus().setPositionZ(posz);
                        }
                        p.getMovementScheme().setPosition(posx, posy, p.getStatus().getPositionZ(), p.getStatus().getRotation(), p.isOnSurface() ? 0 : -1);
                        VolaTile targetTile = Zones.getTileOrNull((int)(posx / 4.0f), (int)(posy / 4.0f), p.isOnSurface());
                        if (targetTile != null) {
                            float height;
                            float f = height = p.getFloorLevel() > 0 ? (float)(p.getFloorLevel() * 3) : 0.0f;
                            if (p.getBridgeId() > 0L) {
                                height = 0.0f;
                            }
                            p.getMovementScheme().setGroundOffset((int)(height * 10.0f), true);
                            p.calculateFloorLevel(targetTile, true);
                        }
                        p.getMovementScheme().haltSpeedModifier();
                        p.setTeleporting(true);
                        p.setTeleportCounter(p.getTeleportCounter() + 1);
                        byte power = Players.isArtist(p.getWurmId(), false, false) ? (byte)2 : (byte)p.getPower();
                        this.sendLoginAnswer(true, string, p.getStatus().getPositionX(), p.getStatus().getPositionY(), p.getStatus().getPositionZ(), p.getStatus().getRotation(), p.isOnSurface() ? 0 : -1, p.getModelName(), power, 0, by, p.getKingdomTemplateId(), p.getFace(), p.getTeleportCounter(), p.getBlood(), p.getBridgeId(), p.getMovementScheme().getGroundOffset());
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "Sent " + p.getStatus().getPositionX() + "," + p.getStatus().getPositionY() + "," + p.getStatus().getPositionZ() + "," + p.getStatus().getRotation());
                        }
                    }
                    catch (IOException iOException) {
                        logger.log(Level.INFO, "Player " + name + " dropped during login.", iOException);
                        p.logoutIn(2, "You seemed to have lost your connection to Wurm.");
                        return;
                    }
                    Server.getInstance().addToPlayersAtLogin(p);
                    try {
                        p.loadSkills();
                        p.sendSkills();
                        LoginHandler.sendAllItemModelNames(p);
                        LoginHandler.sendAllEquippedArmor(p);
                        if (p.getStatus().getBody().getBodyItem() != null) {
                            p.getStatus().getBody().getBodyItem().addWatcher(-1L, p);
                        }
                        p.getInventory().addWatcher(-1L, p);
                        p.getBody().sendWounds();
                        Players.loadAllPrivatePOIForPlayer(p);
                        p.sendAllMapAnnotations();
                        p.resetLastSentToolbelt();
                        ValreiMapData.sendAllMapData(p);
                    }
                    catch (Exception exception) {
                        logger.log(Level.SEVERE, "Failed to load status for player " + name + ".", exception);
                        p.getCommunicator().sendAlertServerMessage("Failed to load your status! Please contact server administrators.");
                        p.logoutIn(2, "The game failed to load your status. Please contact server administrators.");
                        return;
                    }
                    p.sendReligion();
                    p.sendKarma();
                    p.sendScenarioKarma();
                    if (p.getTeam() != null) {
                        p.getTeam().creatureReconnectedTeam(p);
                    }
                    p.lastSentHasCompass = false;
                    Players.getInstance().sendReconnect(p);
                    try {
                        p.sendActionControl(p.getCurrentAction().getActionString(), true, p.getCurrentAction().getTimeLeft());
                    }
                    catch (NoSuchActionException noSuchActionException) {
                        // empty catch block
                    }
                    if (p.getSpellEffects() != null) {
                        p.getSpellEffects().sendAllSpellEffects();
                    }
                    ItemBonus.sendAllItemBonusToPlayer(p);
                    p.sendSpellResistances();
                    p.getCommunicator().sendClimb(p.isClimbing());
                    p.getCommunicator().sendToggle(0, p.isClimbing());
                    p.getCommunicator().sendToggle(2, p.isLegal());
                    p.getCommunicator().sendToggle(1, p.faithful);
                    p.getCommunicator().sendToggle(3, p.isStealth());
                    p.getCommunicator().sendToggle(4, p.isAutofight());
                    p.getCommunicator().sendToggle(100, p.isArcheryMode());
                    if (p.getShield() != null) {
                        p.getCommunicator().sendToggleShield(true);
                    }
                    if ((item = p.getMovementScheme().getDraggedItem()) != null) {
                        Items.stopDragging(item);
                        Items.startDragging(p, item);
                    }
                } else {
                    block222: {
                        String message = "Password incorrect. Please try again or create a new player with a different name than " + name + ".";
                        HackerIp hackerIp = failedIps.get(this.conn.getIp());
                        if (hackerIp != null) {
                            hackerIp.name = name;
                            ++hackerIp.timesFailed;
                            long atime = 0L;
                            if (hackerIp.timesFailed == 10) {
                                atime = 180000L;
                            }
                            if (hackerIp.timesFailed == 20) {
                                atime = 600000L;
                            } else if (hackerIp.timesFailed % 20 == 0) {
                                atime = 10800000L;
                            }
                            if (hackerIp.timesFailed == 100) {
                                Players.addGmMessage("System", "The ip " + this.conn.getIp() + " has failed the password for " + name + " 100 times. It is now banned one hour every failed attempt.");
                            }
                            if (hackerIp.timesFailed > 100) {
                                atime = 3600000L;
                            }
                            hackerIp.mayTryAgain = System.currentTimeMillis() + atime;
                            if (atime > 0L) {
                                message = message + " Because of the repeated failures you may try again in " + Server.getTimeFor(atime) + ".";
                            }
                        } else {
                            failedIps.put(this.conn.getIp(), new HackerIp(1, System.currentTimeMillis(), name));
                        }
                        try {
                            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                            this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -2);
                        }
                        catch (IOException ioe) {
                            if (!logger.isLoggable(Level.FINE)) break block222;
                            logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + message, ioe);
                        }
                    }
                    return;
                }
                Players.getInstance().addToGroups(p);
                p.destroyVisionArea();
                try {
                    p.createVisionArea();
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to create visionarea for player " + p.getName(), ex);
                    p.logoutIn(2, "The game failed to create your vision area. Please contact the administrators.");
                    return;
                }
                if (!p.hasLink()) {
                    p.destroyVisionArea();
                    return;
                }
                if (!p.isDead()) {
                    VolaTile tile = p.getCurrentTile();
                    if (tile == null) {
                        logger.log(Level.WARNING, p.getName() + " isn't in the world. Adding and retrying.");
                        p.sendToWorld();
                    }
                    p.getCommunicator().sendSelfToLocal();
                    Achievements.sendAchievementList(p);
                    p.getCommunicator().sendAllKingdoms();
                    tile = p.getCurrentTile();
                    Door[] doorArray = tile.getDoors();
                    if (doorArray != null) {
                        void var18_121;
                        Door[] ioe = doorArray;
                        int posy = ioe.length;
                        boolean bl = false;
                        while (var18_121 < posy) {
                            Door lDoor = ioe[var18_121];
                            if (lDoor.canBeOpenedBy(p, false)) {
                                if (lDoor instanceof FenceGate) {
                                    p.getCommunicator().sendOpenFence(((FenceGate)lDoor).getFence(), true, true);
                                } else {
                                    p.getCommunicator().sendOpenDoor(lDoor);
                                }
                            }
                            ++var18_121;
                        }
                    }
                    if (!wasinvuln) {
                        p.getCommunicator().sendAlertServerMessage("You are not invulnerable now.");
                        p.getCommunicator().setInvulnerable(false);
                    }
                } else {
                    p.getCommunicator().sendDead();
                    p.sendSpawnQuestion();
                }
                p.setFullyLoaded();
                p.getCommunicator().setReady(true);
                LoginHandler.sendLoggedInPeople(p);
                LoginHandler.sendStatus(p);
                p.getCombatHandler().sendRodEffect();
                p.sendHasFingerEffect();
                p.getStatus().sendStateString();
                p.getCommunicator().sendMapInfo();
                Server.getInstance().addToPlayersAtLogin(p);
                if (p.getVisionArea() != null && p.getVisionArea().getSurface() != null) {
                    p.getVisionArea().getSurface().sendCreatureItems(p);
                }
                p.setBestLightsource(null, true);
                boolean isEducated = false;
                for (Titles.Title title : p.getTitles()) {
                    if (title != Titles.Title.Educated) continue;
                    isEducated = true;
                }
                if (!isEducated) {
                    PlayerTutorial.getTutorialForPlayer(p.getWurmId(), true).sendCurrentStageBML();
                }
                p.isLit = false;
                p.recalcLimitingFactor(null);
                if (p.getCultist() != null) {
                    p.getCultist().sendBuffs();
                }
                AffinitiesTimed.sendTimedAffinitiesFor(p);
                if (p.isDeathProtected()) {
                    p.getCommunicator().sendAddStatusEffect(SpellEffectsEnum.DEATH_PROTECTION, Integer.MAX_VALUE);
                }
                MissionPerformer.sendEpicMissionsPerformed(p, p.getCommunicator());
                MissionPerformer missionPerformer = MissionPerformed.getMissionPerformer(p.getWurmId());
                if (missionPerformer != null) {
                    missionPerformer.sendAllMissionPerformed(p.getCommunicator());
                }
                LoginHandler.checkPutOnBoat(p);
            }
            catch (NoSuchPlayerException nsp) {
                PlayerInfo file = PlayerInfoFactory.createPlayerInfo(name);
                Player player = null;
                try {
                    void var15_61;
                    if (isUndead) {
                        file.undeadType = (byte)(1 + Server.rand.nextInt(3));
                        if (Servers.localServer.LOGINSERVER) {
                            try {
                                if (file.currentServer <= 1) {
                                    for (ServerEntry serverEntry : Servers.getAllServers()) {
                                        if (!serverEntry.EPIC || !serverEntry.isAvailable(file.getPower(), true)) continue;
                                        file.currentServer = serverEntry.getId();
                                    }
                                }
                                player = new Player(file, this.conn);
                                player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString)));
                                Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), player, steamIDAsString);
                                if (ban != null) {
                                    block223: {
                                        try {
                                            String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                                            String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
                                            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                            if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                                                message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                                            }
                                            this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                                        }
                                        catch (IOException ioe) {
                                            if (!logger.isLoggable(Level.FINE)) break block223;
                                            logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending banned IP login denied message", ioe);
                                        }
                                    }
                                    logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
                                    return;
                                }
                                String message = "The server is currently not available. Please try later.";
                                ServerEntry entry = Servers.getServerWithId(file.currentServer);
                                if (entry != null) {
                                    if (entry.isAvailable(file.getPower(), file.isPaying())) {
                                        player.getCommunicator().sendReconnect(entry.EXTERNALIP, Integer.parseInt(entry.EXTERNALPORT), password);
                                        logger.log(Level.INFO, this.conn.getIp() + ", " + name + " redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer);
                                    } else {
                                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                        message = "The server is currently not available. Please try later.";
                                        this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 300);
                                        logger.log(Level.INFO, this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer + " not avail.");
                                    }
                                } else {
                                    logger.warning(this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to non-existant server ID: " + file.currentServer + ", the database entry is wrong");
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -1);
                                }
                                return;
                            }
                            catch (IOException iOException) {
                                return;
                            }
                        }
                    }
                    file.load();
                    if (password.equals(file.getPassword())) {
                        player = new Player(file, this.conn);
                        player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString)));
                        Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), player, steamIDAsString);
                        if (ban != null) {
                            block224: {
                                try {
                                    String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                                    String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                                        message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                                    }
                                    this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block224;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending banned IP login denied message", ioe);
                                }
                            }
                            logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
                            return;
                        }
                        if (file.currentServer != Servers.localServer.id) {
                            block225: {
                                String message = "The server is currently not available. Please try later.";
                                try {
                                    ServerEntry entry = Servers.getServerWithId(file.currentServer);
                                    if (entry != null) {
                                        if (entry.isAvailable(file.getPower(), file.isPaying())) {
                                            player.getCommunicator().sendReconnect(entry.EXTERNALIP, Integer.parseInt(entry.EXTERNALPORT), password);
                                            logger.log(Level.INFO, this.conn.getIp() + ", " + name + " redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer);
                                        } else {
                                            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                            message = "The server is currently not available. Please try later.";
                                            this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 300);
                                            logger.log(Level.INFO, this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to server ID: " + file.currentServer + " not avail.");
                                        }
                                    } else {
                                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                        logger.warning(this.conn.getIp() + ", " + name + " could not be redirected from " + Servers.localServer.id + " to non-existant server ID: " + file.currentServer + ", the database entry is wrong");
                                        this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -1);
                                    }
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block225;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem redirecting from " + Servers.localServer.id + " to server ID: " + file.currentServer, ioe);
                                }
                            }
                            file.lastLogin = System.currentTimeMillis() - 10000L;
                            file.logout();
                            file.save();
                            ++redirects;
                            this.conn.ticksToDisconnect = 400;
                            return;
                        }
                        if (!Constants.isGameServer && file.currentServer == Servers.localServer.id) {
                            block226: {
                                logger.log(Level.WARNING, name + " tried to logon locally.");
                                String message = "You can not log on to this type of server. Contact a GM or Dev";
                                try {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, "You can not log on to this type of server. Contact a GM or Dev", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -1);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block226;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "You can not log on to this type of server. Contact a GM or Dev", ioe);
                                }
                            }
                            file.lastLogin = System.currentTimeMillis() - 10000L;
                            file.logout();
                            file.save();
                            this.conn.ticksToDisconnect = 400;
                            return;
                        }
                        if (player.getSaveFile().realdeath > 4) {
                            block227: {
                                String message = "Your account has suffered real death. You can not log on.";
                                try {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, "Your account has suffered real death. You can not log on.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -1);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block227;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "Your account has suffered real death. You can not log on.", ioe);
                                }
                            }
                            return;
                        }
                        player.setLoginHandler(this);
                        this.conn.setConnectionListener(player.getCommunicator());
                        ++logins;
                        if (player.getPower() < 1 && !player.isPaying() && Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit) {
                            block228: {
                                String message = "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.";
                                try {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block228;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", ioe);
                                }
                            }
                            return;
                        }
                        if (player.getPower() < 1 && !player.isPaying() && Servers.localServer.ISPAYMENT) {
                            block229: {
                                String message = "This server is a premium only server. You can not log on until you have purchased premium time in the webshop.";
                                try {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, "This server is a premium only server. You can not log on until you have purchased premium time in the webshop.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block229;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "This server is a premium only server. You can not log on until you have purchased premium time in the webshop.", ioe);
                                }
                            }
                            return;
                        }
                        if (Constants.maintaining && player.getPower() <= 1) {
                            block230: {
                                try {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, "The server is in maintenance mode. Retrying in 60 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block230;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending maintenance mode login denied", ioe);
                                }
                            }
                            return;
                        }
                        if (Constants.enableSpyPrevention && Servers.localServer.PVPSERVER && !Servers.localServer.testServer && player.getPower() < 1) {
                            byte kingdom = Players.getInstance().getKingdomForPlayer(player.getWurmId());
                            KingdomIp kip = KingdomIp.getKIP(this.conn.getIp(), kingdom);
                            if (kip != null) {
                                long l = kip.mayLogonKingdom(kingdom);
                                if (l < 0L) {
                                    block231: {
                                        try {
                                            Kingdom k = Kingdoms.getKingdom(kip.getKingdom());
                                            Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                            if (k != null) {
                                                this.sendLoginAnswer(false, "Spy prevention: Someone is playing on kingdom " + k.getName() + " from this ip address.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                            } else {
                                                this.sendLoginAnswer(false, "Spy prevention: Someone is playing on another kingdom from this ip address.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                            }
                                        }
                                        catch (IOException ioe) {
                                            if (!logger.isLoggable(Level.FINE)) break block231;
                                            logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending spy prevention login denied", ioe);
                                        }
                                    }
                                    return;
                                }
                                if (l > 1L) {
                                    BridgePart[] timeLeft = Server.getTimeFor(l);
                                    if (l < 0L) {
                                        block232: {
                                            try {
                                                Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                                Kingdom k = Kingdoms.getKingdom(kingdom);
                                                if (k != null) {
                                                    this.sendLoginAnswer(false, "Spy prevention: You have to wait " + (String)timeLeft + " because someone was recently playing " + k.getName() + " from this ip address.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                                } else {
                                                    this.sendLoginAnswer(false, "Spy prevention: You have to wait " + (String)timeLeft + " because someone was recently playing in another kingdom from this ip address.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                                }
                                            }
                                            catch (IOException ioe) {
                                                if (!logger.isLoggable(Level.FINE)) break block232;
                                                logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending spy prevention login denied", ioe);
                                            }
                                        }
                                        return;
                                    }
                                }
                                kip.logon(kingdom);
                            }
                        }
                        logger.log(Level.INFO, this.conn.getIp() + "," + name + " successfully logged on, id: " + player.getWurmId() + '.');
                        Server.getInstance().addPlayer(player);
                        player.initialisePlayer(file);
                        if (!reconnecting) {
                            player.setSendExtraBytes(sendExtraBytes);
                        }
                        player.checkBodyInventoryConsistency();
                        player.getBody().createBodyParts();
                        Server.getInstance().startSendingFinals(player);
                        long start = System.nanoTime();
                        player.loadSkills();
                        Items.loadAllItemsForCreature(player, player.getStatus().getInventoryId());
                        player.getCommunicator().sendMapInfo();
                        Players.loadAllPrivatePOIForPlayer(player);
                        player.resetLastSentToolbelt();
                        player.sendAllMapAnnotations();
                        ValreiMapData.sendAllMapData(player);
                        player.getCommunicator().sendClearTickets();
                        player.getCommunicator().sendClearFriendsList();
                        if (player.getCultist() != null) {
                            player.getCultist().sendBuffs();
                        }
                        AffinitiesTimed.sendTimedAffinitiesFor(player);
                        Players.getInstance().sendConnectInfo(player, " has logged in.", player.getLastLogin(), PlayerOnlineStatus.ONLINE, true);
                        Players.getInstance().addToGroups(player);
                        if (player.getBridgeId() != -10L) {
                            BridgePart[] bridgePartArray = player.getCurrentTile().getBridgeParts();
                            boolean foundBridge = false;
                            for (BridgePart bp : bridgePartArray) {
                                foundBridge = true;
                                if (bp.isFinished()) continue;
                                foundBridge = false;
                                break;
                            }
                            if (foundBridge) {
                                for (BridgePart bp : bridgePartArray) {
                                    if (!bp.isFinished() || !bp.hasAnExit() || bp.getStructureId() == player.getBridgeId()) continue;
                                    logger.info(String.format("Player %s logged in at [%s, %s] where bridge ID %s used to be built, but has since been replaced by the bridge ID %s.", player.getName(), player.getTileX(), player.getTileY(), player.getBridgeId(), bp.getStructureId()));
                                    player.setBridgeId(bp.getStructureId());
                                    break;
                                }
                            } else {
                                logger.info(String.format("Player %s logged in at [%s, %s] where a bridge used to be, but no longer exists.", player.getName(), player.getTileX(), player.getTileY()));
                                player.setBridgeId(-10L);
                            }
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.info("Loading all skills and items took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis for " + name);
                        }
                    }
                    logger.log(Level.INFO, this.conn.getIp() + "," + name + ", tried to log in with wrong password.");
                    if (file.getPower() > 0) {
                        Players.getInstance().sendConnectAlert(this.conn.getIp() + "," + name + ", tried to log in with wrong password.");
                        Players.addGmMessage(name, this.conn.getIp() + "," + name + ", tried to log in with wrong password.");
                        WCGmMessage wCGmMessage = new WCGmMessage(WurmId.getNextWCCommandId(), name, "(" + Servers.localServer.id + ") " + this.conn.getIp() + "," + name + ", tried to log in with wrong password.", false);
                        wCGmMessage.sendToLoginServer();
                    }
                    String string = "Password incorrect. Please try again or create a new player with a different name than " + name + ".";
                    HackerIp ip = failedIps.get(this.conn.getIp());
                    if (ip != null) {
                        ip.name = name;
                        ++ip.timesFailed;
                        long atime = 0L;
                        if (ip.timesFailed == 10) {
                            atime = 180000L;
                        }
                        if (ip.timesFailed == 20) {
                            atime = 600000L;
                        } else if (ip.timesFailed % 20 == 0) {
                            atime = 10800000L;
                        }
                        if (ip.timesFailed == 100) {
                            Players.addGmMessage("System", "The ip " + this.conn.getIp() + " has failed the password for " + name + " 100 times. It is now banned one hour every failed attempt.");
                        }
                        if (ip.timesFailed > 100) {
                            atime = 3600000L;
                        }
                        ip.mayTryAgain = System.currentTimeMillis() + atime;
                        if (atime > 0L) {
                            String string3 = string + " Because of the repeated failures you may try again in " + Server.getTimeFor(atime) + ".";
                        }
                    } else {
                        failedIps.put(this.conn.getIp(), new HackerIp(1, System.currentTimeMillis(), name));
                    }
                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                    this.sendLoginAnswer(false, (String)var15_61, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                    return;
                }
                catch (Exception exception) {
                    if (!isUndead && !Servers.localServer.testServer && logger.isLoggable(Level.INFO) && !Server.getInstance().isPS()) {
                        logger.log(Level.INFO, "Caught Exception while trying to log player in:" + exception.getMessage() + " for " + name, exception);
                    }
                    try {
                        if (sendExtraBytes || Servers.localServer.testServer || isUndead || Server.getInstance().isPS()) {
                            float[] txty;
                            if (Constants.maintaining) {
                                block234: {
                                    try {
                                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                        this.sendLoginAnswer(false, "The server is in maintenance mode. Retrying in 60 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                    }
                                    catch (IOException ioe) {
                                        if (!logger.isLoggable(Level.FINE)) break block234;
                                        logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending maintenance mode login denied", ioe);
                                    }
                                }
                                return;
                            }
                            if (Players.getInstance().numberOfPlayers() > Servers.localServer.pLimit) {
                                block235: {
                                    String message = "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.";
                                    try {
                                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                        this.sendLoginAnswer(false, "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 60);
                                    }
                                    catch (IOException ioe) {
                                        if (!logger.isLoggable(Level.FINE)) break block235;
                                        logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "The server is full. If you pay for a premium account you will be able to enter anyway. Retrying in 60 seconds.", ioe);
                                    }
                                }
                                return;
                            }
                            if (Servers.localServer.id != Servers.loginServer.id && !isUndead && !Servers.localServer.testServer) {
                                block236: {
                                    String message = "There are multiple login servers in the cluster, please remove so it is only one";
                                    try {
                                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                        this.sendLoginAnswer(false, "There are multiple login servers in the cluster, please remove so it is only one", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, -1);
                                    }
                                    catch (IOException ioe) {
                                        if (!logger.isLoggable(Level.FINE)) break block236;
                                        logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "There are multiple login servers in the cluster, please remove so it is only one", ioe);
                                    }
                                }
                                return;
                            }
                            logger.log(Level.INFO, this.conn.getIp() + "," + name + " was created successfully.");
                            player = Player.doNewPlayer(1, this.conn);
                            player.setName(name);
                            float posX = Servers.localServer.SPAWNPOINTJENNX * 4 + Server.rand.nextInt(10);
                            float posY = Servers.localServer.SPAWNPOINTJENNY * 4 + Server.rand.nextInt(10);
                            int n = Server.rand.nextInt(3);
                            float rot = Server.rand.nextInt(360);
                            byte kingdom = 1;
                            if (isUndead) {
                                kingdom = 0;
                                txty = Player.findRandomSpawnX(false, false);
                                posX = txty[0];
                                posY = txty[1];
                            } else {
                                if (Servers.localServer.KINGDOM != 0) {
                                    kingdom = Servers.localServer.KINGDOM;
                                } else if (n == 1) {
                                    kingdom = 2;
                                    posX = Servers.localServer.SPAWNPOINTMOLX * 4 + Server.rand.nextInt(10);
                                    posY = Servers.localServer.SPAWNPOINTMOLY * 4 + Server.rand.nextInt(10);
                                } else if (n == 2) {
                                    kingdom = 3;
                                    posX = Servers.localServer.SPAWNPOINTLIBX * 4 + Server.rand.nextInt(10);
                                    posY = Servers.localServer.SPAWNPOINTLIBY * 4 + Server.rand.nextInt(10);
                                }
                                if (Servers.localServer.randomSpawns) {
                                    txty = Player.findRandomSpawnX(true, true);
                                    posX = txty[0];
                                    posY = txty[1];
                                }
                            }
                            Spawnpoint sp = LoginHandler.getInitialSpawnPoint(kingdom);
                            if (sp != null) {
                                posX = sp.tilex * 4 + Server.rand.nextInt(10);
                                posY = sp.tiley * 4 + Server.rand.nextInt(10);
                            }
                            long wurmId = WurmId.getNextPlayerId();
                            player = (Player)player.setWurmId(wurmId, posX, posY, rot, 0);
                            Ban ban = Players.getInstance().getAnyBan(this.conn.getIp(), player, steamIDAsString);
                            if (ban != null) {
                                block237: {
                                    try {
                                        String time = Server.getTimeFor(ban.getExpiry() - System.currentTimeMillis());
                                        String message = ban.getIdentifier() + " is banned for " + time + " more. Reason: " + ban.getReason();
                                        Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                        if (ban.getExpiry() - System.currentTimeMillis() > 29030400000L) {
                                            message = ban.getIdentifier() + " is permanently banned. Reason: " + ban.getReason();
                                        }
                                        this.sendLoginAnswer(false, message, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                                    }
                                    catch (IOException ioe) {
                                        if (!logger.isLoggable(Level.FINE)) break block237;
                                        logger.log(Level.FINE, this.conn.getIp() + ", " + name + " problem sending IP banned login denied message", ioe);
                                    }
                                }
                                logger.log(Level.INFO, name + " is banned, trying to log on from " + this.conn.getIp());
                                return;
                            }
                            LoginHandler.putOutsideWall(player);
                            if (player.isOnSurface()) {
                                LoginHandler.putOutsideHouse(player, false);
                                LoginHandler.putOutsideFence(player);
                            }
                            String message = "Welcome to Wurm, " + name + "! " + (Servers.localServer.hasMotd() ? Servers.localServer.getMotd() : Constants.motd);
                            player.getMovementScheme().setPosition(player.getStatus().getPositionX(), player.getStatus().getPositionY(), player.getStatus().getPositionZ(), player.getStatus().getRotation(), player.isOnSurface() ? 0 : -1);
                            VolaTile targetTile = Zones.getTileOrNull((int)(player.getStatus().getPositionX() / 4.0f), (int)(player.getStatus().getPositionY() / 4.0f), player.isOnSurface());
                            if (targetTile != null) {
                                float height = player.getFloorLevel() > 0 ? (float)(player.getFloorLevel() * 3) : 0.0f;
                                player.getMovementScheme().setGroundOffset((int)(height * 10.0f), true);
                                player.calculateFloorLevel(targetTile, true);
                            }
                            player.getStatus().checkStaminaEffects(65535);
                            player.getMovementScheme().haltSpeedModifier();
                            player.setTeleporting(true);
                            player.setTeleportCounter(player.getTeleportCounter() + 1);
                            player.setNewPlayer(true);
                            file.initialize(name, player.getWurmId(), password, "What is your mother's maiden name?", "Sawyer", Server.rand.nextLong(), sendExtraBytes);
                            file.setEmailAddress(name + "@test.com");
                            player.setSaveFile(file);
                            player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString)));
                            if (player.isUndead()) {
                                file.setUndeadData();
                            }
                            ++logins;
                            player.setLoginHandler(this);
                            this.conn.setConnectionListener(player.getCommunicator());
                            Server.getInstance().addIp(this.conn.getIp());
                            player.setIpaddress(this.conn.getIp());
                            player.setSteamID(SteamId.fromSteamID64(Long.valueOf(steamIDAsString)));
                            player.setFlag(3, true);
                            player.setFlag(53, true);
                            Server.getInstance().addPlayer(player);
                            player.getBody().createBodyParts();
                            player.getLoginhandler().sendLoginAnswer(true, message, player.getStatus().getPositionX(), player.getStatus().getPositionY(), player.getStatus().getPositionZ(), player.getStatus().getRotation(), player.isOnSurface() ? 0 : -1, player.getModelName(), (byte)0, 0, (byte)0, kingdom, 0L, player.getTeleportCounter(), player.getBlood(), player.getBridgeId(), player.getMovementScheme().getGroundOffset());
                            SelectSpawnQuestion question = new SelectSpawnQuestion(player, "Define your character", "Please select gender:", player.getWurmId(), message, isUndead);
                            question.sendQuestion();
                            if (player.getStatus().getBody().getBodyItem() != null) {
                                player.getStatus().getBody().getBodyItem().addWatcher(-1L, player);
                            }
                        } else {
                            block238: {
                                String message = "You need to register an account on www.wurmonline.com.";
                                try {
                                    Server.getInstance().steamHandler.EndAuthSession(steamIDAsString);
                                    this.sendLoginAnswer(false, "You need to register an account on www.wurmonline.com.", 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                                }
                                catch (IOException ioe) {
                                    if (!logger.isLoggable(Level.FINE)) break block238;
                                    logger.log(Level.FINE, this.conn.getIp() + ", " + name + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + "You need to register an account on www.wurmonline.com.", ioe);
                                }
                            }
                            return;
                        }
                        player.setFlag(76, true);
                    }
                    catch (Exception ex2) {
                        logger.log(Level.WARNING, "Failed to create player with name " + name, ex2);
                    }
                }
            }
        }
    }

    private void setHasLoadedItems(int step) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Setting loadedItems to " + step);
        }
        this.loadedItems = step;
    }

    int loadPlayer(Player player, int step) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Loading " + player + ", step: " + step);
        }
        if (step == 0) {
            player.sendAddChampionPoints();
            player.getSaveFile().frozenSleep = true;
            return step;
        }
        if (step == 1) {
            Player p = player;
            LoginHandler l = this;
            if (this.loadedItems == 0) {
                Thread t = new Thread("PlayerLoader-Thread-" + p.getWurmId()){

                    @Override
                    public void run() {
                        try {
                            p.getBody().load();
                            player.getSaveFile().loadIgnored(player.getWurmId());
                            player.getSaveFile().loadFriends(player.getWurmId());
                            player.getSaveFile().loadTitles(player.getWurmId());
                            player.getSaveFile().loadHistoryIPs(player.getWurmId());
                            player.getSaveFile().loadHistorySteamIds(player.getWurmId());
                            player.getSaveFile().loadHistoryEmails(player.getWurmId());
                            l.setHasLoadedItems(1);
                        }
                        catch (Exception sex2) {
                            try {
                                logger.log(Level.WARNING, p.getName() + " has no body. Creating!", sex2);
                                p.getStatus().createNewBody();
                                l.setHasLoadedItems(1);
                            }
                            catch (Exception sex) {
                                logger.log(Level.WARNING, p.getName() + " has no body.", sex);
                                l.setHasLoadedItems(-1);
                            }
                        }
                    }
                };
                this.loadedItems = Integer.MAX_VALUE;
                t.setPriority(4);
                t.start();
            }
            if (logger.isLoggable(Level.FINER)) {
                logger.finer("Body step=" + step + ", loadedItems=" + this.loadedItems);
            }
            if (this.loadedItems == 1 || this.loadedItems == -1) {
                return this.loadedItems;
            }
            return step - 1;
        }
        if (step == 2) {
            if (!player.isReallyPaying() && player.hasFlag(8)) {
                if (player.getPaymentExpire() == 0L) {
                    logger.log(Level.INFO, player.getName() + " logged on to prevent expiry.");
                }
                player.setFlag(8, false);
            }
            this.loadedItems = 2;
            return this.loadedItems;
        }
        if (step == 3) {
            LoginHandler.putOutsideWall(player);
            if (player.isOnSurface()) {
                LoginHandler.putOutsideHouse(player, true);
                LoginHandler.putOutsideFence(player);
            }
            player.getCommunicator().sendWeather();
            player.getCommunicator().checkSendWeather();
            if (!player.isDead()) {
                LoginHandler.putInBoatAndAssignSeat(player, false);
            }
            return step;
        }
        if (step == 4) {
            player.getMovementScheme().setPosition(player.getStatus().getPositionX(), player.getStatus().getPositionY(), player.getStatus().getPositionZ(), player.getStatus().getRotation(), player.isOnSurface() ? 0 : -1);
            VolaTile targetTile = Zones.getTileOrNull((int)(player.getStatus().getPositionX() / 4.0f), (int)(player.getStatus().getPositionY() / 4.0f), player.isOnSurface());
            if (targetTile != null) {
                float height;
                float f = height = player.getFloorLevel() > 0 ? (float)(player.getFloorLevel() * 3) : 0.0f;
                if (player.getBridgeId() > 0L) {
                    height = 0.0f;
                }
                player.getMovementScheme().setGroundOffset((int)(height * 10.0f), true);
                player.calculateFloorLevel(targetTile, true);
            }
            player.getMovementScheme().haltSpeedModifier();
            try {
                String message = "Welcome back, " + player.getName() + "! " + (Servers.localServer.hasMotd() ? Servers.localServer.getMotd() : Constants.motd);
                player.setTeleporting(true);
                player.setTeleportCounter(player.getTeleportCounter() + 1);
                byte power = Players.isArtist(player.getWurmId(), false, false) ? (byte)2 : (byte)player.getPower();
                this.sendLoginAnswer(true, message, player.getStatus().getPositionX(), player.getStatus().getPositionY(), player.getStatus().getPositionZ(), player.getStatus().getRotation(), player.isOnSurface() ? 0 : -1, player.getModelName(), power, 0, (byte)0, player.getKingdomId(), player.getFace(), player.getTeleportCounter(), player.getBlood(), player.getBridgeId(), player.getMovementScheme().getGroundOffset());
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, player.getName() + ": sent Position X,Y,Z,Rotation: " + player.getStatus().getPositionX() + "," + player.getStatus().getPositionY() + "," + player.getStatus().getPositionZ() + "," + player.getStatus().getRotation());
                }
            }
            catch (IOException ioe) {
                logger.log(Level.FINE, "Player " + player.getName() + " dropped during login.", ioe);
                return -1;
            }
            LoginHandler.sendAllItemModelNames(player);
            LoginHandler.sendAllEquippedArmor(player);
            return step;
        }
        if (step == 5) {
            if (!player.isDead() && !LoginHandler.willGoOnBoat(player)) {
                try {
                    player.createVisionArea();
                }
                catch (Exception ve) {
                    logger.log(Level.WARNING, "Failed to create visionarea for player " + player.getName(), ve);
                    return -1;
                }
            }
            if (!player.hasLink()) {
                player.destroyVisionArea();
                return -1;
            }
            return step;
        }
        if (step == 6) {
            player.getCommunicator().sendToggle(0, player.isClimbing());
            player.getCommunicator().sendToggle(2, player.isLegal());
            player.getCommunicator().sendToggle(1, player.faithful);
            player.getCommunicator().sendToggle(3, player.isStealth());
            player.getCommunicator().sendToggle(4, player.isAutofight());
            if (player.isStealth()) {
                player.getMovementScheme().setStealthMod(true);
            }
            if (player.getShield() != null) {
                player.getCommunicator().sendToggleShield(true);
            }
            if (player.getPower() > 0) {
                player.getStatus().visible = false;
                player.getCommunicator().sendNormalServerMessage("You should not be visible now.");
            }
            player.sendActionControl("", false, 0);
            if (!player.isDead()) {
                VolaTile tile;
                player.getCommunicator().sendClimb(player.isClimbing());
                player.sendToWorld();
                player.getCommunicator().sendSelfToLocal();
                player.getCommunicator().sendAllKingdoms();
                Achievements.sendAchievementList(player);
                if (player.getVisionArea() != null) {
                    if (this.redirected) {
                        player.getCommunicator().sendAlertServerMessage("You may not move right now.");
                        player.transferCounter = 10;
                    } else {
                        player.getCommunicator().setReady(true);
                    }
                }
                if ((tile = player.getCurrentTile()) != null) {
                    Door[] doors = tile.getDoors();
                    if (doors != null) {
                        for (Door lDoor : doors) {
                            if (!lDoor.covers(player.getPosX(), player.getPosY(), player.getPositionZ(), player.getFloorLevel(), player.followsGround()) || !lDoor.canBeOpenedBy(player, false)) continue;
                            if (lDoor instanceof FenceGate) {
                                player.getCommunicator().sendOpenFence(((FenceGate)lDoor).getFence(), true, true);
                                continue;
                            }
                            player.getCommunicator().sendOpenDoor(lDoor);
                        }
                    }
                } else {
                    logger.log(Level.WARNING, player.getName() + "- tile is null!", new Exception());
                }
            } else {
                player.getCommunicator().sendDead();
            }
            return step;
        }
        if (step == 7) {
            player.getBody().getBodyItem().addWatcher(-1L, player);
            return step;
        }
        if (step == 8) {
            player.getInventory().addWatcher(-1L, player);
            return step;
        }
        if (step == 9) {
            LoginHandler.setStamina(player);
            if (player.isDead()) {
                player.sendSpawnQuestion();
            } else {
                player.checkChallengeWarnQuestion();
            }
            player.recalcLimitingFactor(null);
            return step;
        }
        if (step == 10) {
            player.getBody().loadWounds();
            return step;
        }
        if (step == 11) {
            if (player.mayHearDevTalk()) {
                Players.sendGmMessages(player);
            }
            if (player.mayHearMgmtTalk()) {
                Players.sendMgmtMessages(player);
            }
            return step;
        }
        if (step == 12) {
            player.createSpellEffects();
            player.addNewbieBuffs();
            player.getSaveFile().setLogin();
            player.sendSpellResistances();
            return step;
        }
        if (step == 13) {
            LoginHandler.sendStatus(player);
            Team t = Groups.getTeamForOfflineMember(player.getWurmId());
            if (t != null) {
                player.setTeam(t, false);
            }
            return step;
        }
        if (step == 14) {
            player.getStatus().sendStateString();
            player.setBestLightsource(null, true);
            return step;
        }
        if (step == 15) {
            if (player.hasLink()) {
                player.setIpaddress(this.conn.getIp());
                Server.getInstance().addIp(this.conn.getIp());
                MissionPerformer.sendEpicMissionsPerformed(player, player.getCommunicator());
                MissionPerformer mp = MissionPerformed.getMissionPerformer(player.getWurmId());
                if (mp != null) {
                    mp.sendAllMissionPerformed(player.getCommunicator());
                }
                return step;
            }
            return Integer.MAX_VALUE;
        }
        if (step == 16) {
            LoginHandler.sendLoggedInPeople(player);
            player.sendReligion();
            player.sendKarma();
            player.sendScenarioKarma();
            player.setFullyLoaded();
            if (player.getCultist() != null) {
                player.getCultist().sendBuffs();
            }
            AffinitiesTimed.sendTimedAffinitiesFor(player);
            if (player.isDeathProtected()) {
                player.getCommunicator().sendAddStatusEffect(SpellEffectsEnum.DEATH_PROTECTION, Integer.MAX_VALUE);
            }
            player.recalcLimitingFactor(null);
            player.getCommunicator().sendSafeServerMessage("Type /help for available commands.");
            if (player.isOnHostileHomeServer()) {
                player.getCommunicator().sendAlertServerMessage("These enemy lands drain you of your confidence. You fight less effectively.");
            }
            boolean isEducated = false;
            for (Titles.Title t : player.getTitles()) {
                if (t != Titles.Title.Educated) continue;
                isEducated = true;
            }
            if (!isEducated) {
                PlayerTutorial.getTutorialForPlayer(player.getWurmId(), true).sendCurrentStageBML();
            }
            return step;
        }
        if (step == 17) {
            LoginHandler.checkReimbursement(player);
            if (player.getSaveFile().pet != -10L) {
                try {
                    Creature c = Creatures.getInstance().getCreature(player.getSaveFile().pet);
                    if (c.dominator != player.getWurmId()) {
                        player.getCommunicator().sendNormalServerMessage(c.getNameWithGenus() + " is no longer your pet.");
                        player.setPet(-10L);
                    }
                }
                catch (NoSuchCreatureException nsc) {
                    try {
                        Creature c = Creatures.getInstance().loadOfflineCreature(player.getSaveFile().pet);
                        if (c.dominator != player.getWurmId()) {
                            if (logger.isLoggable(Level.FINER)) {
                                logger.finer(c.getName() + "," + c.getWurmId() + " back from offline - no longer dominated by " + player.getWurmId());
                            }
                            player.getCommunicator().sendNormalServerMessage(c.getNameWithGenus() + " is no longer your pet.");
                            player.setPet(-10L);
                        }
                    }
                    catch (NoSuchCreatureException nsc2) {
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("Failed to load from offline to " + player.getSaveFile().pet);
                        }
                        player.getCommunicator().sendNormalServerMessage("Your pet is nowhere to be found. It may have died of old age.");
                        player.setPet(-10L);
                    }
                }
            }
            Creatures.getInstance().returnCreaturesForPlayer(player.getWurmId());
            if (player.getVisionArea() != null && player.getVisionArea().getSurface() != null) {
                player.getVisionArea().getSurface().sendCreatureItems(player);
            }
            LoginHandler.checkPutOnBoat(player);
            if (!player.checkTileInvulnerability()) {
                player.getCommunicator().sendAlertServerMessage("You are not invulnerable here.");
                player.getCommunicator().setInvulnerable(false);
            }
            if (player.isStealth()) {
                player.setStealth(false);
            }
            return step;
        }
        return Integer.MAX_VALUE;
    }

    static int createPlayer(Player player, int step) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Creating player " + player + ", step: " + step);
        }
        if (step == 0) {
            try {
                player.loadSkills();
                player.sendSkills();
            }
            catch (Exception ex) {
                logger.log(Level.INFO, "Failed to create skills: " + ex.getMessage(), ex);
                return -1;
            }
            return step;
        }
        if (step == 1) {
            try {
                player.getBody().createBodyParts();
            }
            catch (Exception ex) {
                logger.log(Level.INFO, "Failed to create bodyparts: " + ex.getMessage(), ex);
                return -1;
            }
            return step;
        }
        if (step == 2) {
            try {
                player.createPossessions();
            }
            catch (Exception ex) {
                logger.log(Level.INFO, "Failed to create possessions: " + ex.getMessage(), ex);
                return -1;
            }
            return step;
        }
        if (step == 3) {
            return step;
        }
        if (step == 4) {
            try {
                player.createVisionArea();
            }
            catch (Exception ve) {
                logger.log(Level.WARNING, "Failed to create visionarea for player " + player.getName(), ve);
                return -1;
            }
            if (!player.hasLink()) {
                player.destroyVisionArea();
                return -1;
            }
            player.getCommunicator().setReady(true);
            return step;
        }
        if (step == 5) {
            player.createSomeItems(1.0f, false);
            return step;
        }
        if (step == 6) {
            player.setFullyLoaded();
            player.getCommunicator().sendToggle(0, player.isClimbing());
            player.getCommunicator().sendToggle(2, player.isLegal());
            player.getCommunicator().sendToggle(1, player.faithful);
            player.getCommunicator().sendToggle(3, player.isStealth());
            player.getCommunicator().sendToggle(4, player.isAutofight());
            return step;
        }
        if (step == 7) {
            player.sendReligion();
            player.sendKarma();
            player.sendScenarioKarma();
            player.sendToWorld();
            player.getCommunicator().sendWeather();
            player.getCommunicator().checkSendWeather();
            player.getCommunicator().sendSelfToLocal();
            return step;
        }
        if (step == 8) {
            if (!player.isGuest()) {
                player.getStatus().setStatusExists(true);
                try {
                    player.save();
                }
                catch (Exception ex) {
                    logger.log(Level.INFO, "Failed to save player: " + ex.getMessage(), ex);
                    return -1;
                }
            }
            return step;
        }
        if (step == 9) {
            return step;
        }
        if (step == 10) {
            player.createSpellEffects();
            player.getSaveFile().setLogin();
            return step;
        }
        if (step == 11) {
            LoginHandler.sendStatus(player);
            return step;
        }
        if (step == 12) {
            player.getStatus().sendStateString();
            return step;
        }
        if (step == 13) {
            LoginHandler.sendLoggedInPeople(player);
            MissionPerformer.sendEpicMissionsPerformed(player, player.getCommunicator());
            MissionPerformer mp = MissionPerformed.getMissionPerformer(player.getWurmId());
            if (mp != null) {
                mp.sendAllMissionPerformed(player.getCommunicator());
            }
            return step;
        }
        if (step == 14) {
            Item mirroritem;
            LoginHandler.checkReimbursement(player);
            if (player.isNew() && (mirroritem = player.getCarriedItem(781)) != null) {
                player.getCommunicator().sendCustomizeFace(player.getFace(), mirroritem.getWurmId());
            }
            player.setNewPlayer(false);
            if (player.getVisionArea() != null && player.getVisionArea().getSurface() != null) {
                player.getVisionArea().getSurface().sendCreatureItems(player);
            }
            LoginHandler.sendAllEquippedArmor(player);
            LoginHandler.sendAllItemModelNames(player);
            boolean isEducated = false;
            for (Titles.Title t : player.getTitles()) {
                if (t != Titles.Title.Educated) continue;
                isEducated = true;
            }
            if (!isEducated) {
                PlayerTutorial.getTutorialForPlayer(player.getWurmId(), true).sendCurrentStageBML();
            }
            return Integer.MAX_VALUE;
        }
        return Integer.MAX_VALUE;
    }

    private static void checkReimbursement(Player player) {
        player.reimburse();
    }

    private static void setStamina(Player player) {
        if (System.currentTimeMillis() - player.getSaveFile().getLastLogin() < 21600000L) {
            player.getStatus().modifyStamina2((float)(System.currentTimeMillis() - player.getSaveFile().lastLogout) / 2.16E7f);
        } else {
            player.getStatus().modifyStamina2(1.0f);
            player.getStatus().modifyHunger(-10000, 0.5f);
            player.getStatus().modifyThirst(-10000.0f);
        }
    }

    private static boolean creatureIsInsideWrongHouse(Player player, boolean load) {
        Structure struct;
        int tiley;
        if (player.getPower() > 1) {
            return false;
        }
        VolaTile startTile = null;
        int tilex = player.getTileX();
        startTile = Zones.getTileOrNull(tilex, tiley = player.getTileY(), player.isOnSurface());
        if (startTile != null && (struct = startTile.getStructure()) != null && struct.isFinished()) {
            return !struct.mayPass(player);
        }
        return false;
    }

    public static boolean putOutsideHouse(Player player, boolean load) {
        Structure struct;
        int tiley;
        if (player.getPower() > 1) {
            return false;
        }
        VolaTile startTile = null;
        int tilex = player.getTileX();
        startTile = Zones.getTileOrNull(tilex, tiley = player.getTileY(), player.isOnSurface());
        if (startTile != null && (struct = startTile.getStructure()) != null && struct.isFinished() && struct.isTypeHouse()) {
            block15: {
                MountTransfer mt;
                Door[] doors;
                Item[] keys;
                for (Item lKey : keys = player.getKeys()) {
                    if (lKey.getWurmId() != struct.getWritId()) continue;
                    return false;
                }
                if (struct.mayPass(player)) {
                    return false;
                }
                for (Door door : doors = struct.getAllDoors()) {
                    if (door.isLocked() || door.getOuterTile().getStructure() == struct) continue;
                    return false;
                }
                for (Door door : doors) {
                    if (door.getOuterTile().getStructure() == struct) continue;
                    startTile = door.getOuterTile();
                    break;
                }
                if (startTile == null) {
                    startTile = Zones.getOrCreateTile(Server.rand.nextBoolean() ? struct.getMaxX() + 1 : struct.getMinX() - 1, Server.rand.nextBoolean() ? struct.getMaxY() + 1 : struct.getMinY() - 1, true);
                }
                float posX = (startTile.getTileX() << 2) + 2;
                float posY = (startTile.getTileY() << 2) + 2;
                if (Servers.localServer.entryServer) {
                    posX = (float)(startTile.getTileX() << 2) + 0.5f + Server.rand.nextFloat() * 3.0f;
                    posY = (float)(startTile.getTileY() << 2) + 0.5f + Server.rand.nextFloat() * 3.0f;
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Setting " + player.getName() + " outside structure " + struct.getName() + " on " + startTile.getTileX() + ", " + startTile.getTileY() + ". New or reconnect=" + !load);
                }
                if ((mt = MountTransfer.getTransferFor(player.getWurmId())) != null) {
                    mt.remove(player.getWurmId());
                }
                player.setPositionX(posX);
                player.setPositionY(posY);
                if (player.getVehicle() != -10L) {
                    player.disembark(false);
                }
                try {
                    player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                }
                catch (NoSuchZoneException nsz) {
                    Iterator<Spawnpoint> iterator;
                    logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player.getStatus().getPositionY());
                    player.calculateSpawnPoints();
                    if (player.spawnpoints == null || !(iterator = player.spawnpoints.iterator()).hasNext()) break block15;
                    Spawnpoint p = iterator.next();
                    tilex = p.tilex;
                    tiley = p.tiley;
                    posX = tilex * 4;
                    posY = tiley * 4;
                    player.setPositionX(posX + 2.0f);
                    player.setPositionY(posY + 2.0f);
                    try {
                        player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                    }
                    catch (NoSuchZoneException nsz2) {
                        logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
                    }
                    player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
                    return true;
                }
            }
            LoginHandler.putOutsideEnemyDeed(player, load);
            return true;
        }
        return LoginHandler.putOutsideEnemyDeed(player, load);
    }

    private static final VolaTile getStartTileForDeed(Player player) {
        int tiley;
        int tilex = player.getTileX();
        Village v = Zones.getVillage(tilex, tiley = player.getTileY(), player.isOnSurface());
        if (v != null && v.isEnemy(player, true)) {
            Structure struct;
            VolaTile startTile;
            player.getCommunicator().sendSafeServerMessage("You find yourself outside the " + v.getName() + " settlement.");
            int ntx = v.getEndX() + Server.rand.nextInt(10);
            if (Server.rand.nextBoolean()) {
                ntx = v.getStartX() - Server.rand.nextInt(10);
            }
            int nty = v.getEndY() + Server.rand.nextInt(10);
            if (Server.rand.nextBoolean()) {
                nty = v.getStartY() - Server.rand.nextInt(10);
            }
            if ((startTile = Zones.getTileOrNull(ntx, nty, player.isOnSurface())) != null) {
                struct = startTile.getStructure();
                if (struct == null || !struct.isFinished()) {
                    return startTile;
                }
            } else {
                return Zones.getOrCreateTile(ntx, nty, true);
            }
            ntx = v.getEndX() + Server.rand.nextInt(10);
            if (Server.rand.nextBoolean()) {
                ntx = v.getStartX() - Server.rand.nextInt(10);
            }
            if ((startTile = Zones.getTileOrNull(ntx, nty = v.getStartY() - 10 + Server.rand.nextInt(v.getEndY() + 20 - v.getStartY()), player.isOnSurface())) != null) {
                struct = startTile.getStructure();
                if (struct == null || !struct.isFinished()) {
                    return startTile;
                }
            } else {
                return Zones.getOrCreateTile(ntx, nty, true);
            }
            for (int x = 0; x < 20; ++x) {
                nty = v.getEndY() + Server.rand.nextInt(10);
                if (Server.rand.nextBoolean()) {
                    nty = v.getStartY() - Server.rand.nextInt(10);
                }
                if ((startTile = Zones.getTileOrNull(ntx = v.getStartX() - 10 + Server.rand.nextInt(v.getEndX() + 20 - v.getStartX()), nty, player.isOnSurface())) != null) {
                    Structure struct2 = startTile.getStructure();
                    if (struct2 != null && struct2.isFinished()) continue;
                    return startTile;
                }
                return Zones.getOrCreateTile(ntx, nty, true);
            }
        }
        return null;
    }

    public static boolean putOutsideEnemyDeed(Player player, boolean load) {
        VolaTile startTile;
        if (load && player.getPower() == 0 && (startTile = LoginHandler.getStartTileForDeed(player)) != null) {
            block7: {
                float posX = (startTile.getTileX() << 2) + 2;
                float posY = (startTile.getTileY() << 2) + 2;
                MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
                if (mt != null) {
                    mt.remove(player.getWurmId());
                }
                player.setPositionX(posX);
                player.setPositionY(posY);
                if (player.getVehicle() != -10L) {
                    player.disembark(false);
                }
                try {
                    player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                }
                catch (NoSuchZoneException nsz) {
                    Iterator<Spawnpoint> iterator;
                    logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player.getStatus().getPositionY());
                    player.calculateSpawnPoints();
                    if (player.spawnpoints == null || !(iterator = player.spawnpoints.iterator()).hasNext()) break block7;
                    Spawnpoint p = iterator.next();
                    short tilex = p.tilex;
                    short tiley = p.tiley;
                    posX = tilex * 4;
                    posY = tiley * 4;
                    player.setPositionX(posX + 2.0f);
                    player.setPositionY(posY + 2.0f);
                    try {
                        player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                    }
                    catch (NoSuchZoneException nsz2) {
                        logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
                    }
                    player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    public static void putOutsideWall(Player player) {
        int tiley;
        int tilex;
        int tile;
        if (player.getStatus().getLayer() < 0 && Tiles.isSolidCave(Tiles.decodeType(tile = Server.caveMesh.getTile(tilex = player.getTileX(), tiley = player.getTileY())))) {
            block8: {
                boolean saved = false;
                block4: for (int x = -1; x <= 1; ++x) {
                    for (int y = -1; y <= 1; ++y) {
                        tile = Server.caveMesh.getTile(tilex + x, tiley + y);
                        if (Tiles.isSolidCave(Tiles.decodeType(tile))) continue;
                        float posX = (tilex + x) * 4;
                        float posY = (tiley + y) * 4;
                        player.setPositionX(posX + 2.0f);
                        player.setPositionY(posY + 2.0f);
                        saved = true;
                        continue block4;
                    }
                }
                if (!saved) {
                    player.setLayer(0, false);
                }
                try {
                    player.setPositionZ(Zones.calculateHeight(player.getStatus().getPositionX(), player.getStatus().getPositionY(), player.isOnSurface()));
                }
                catch (NoSuchZoneException nsz) {
                    Iterator<Spawnpoint> iterator;
                    logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player.getStatus().getPositionY() + ". Respawning.");
                    player.calculateSpawnPoints();
                    if (player.spawnpoints == null || !(iterator = player.spawnpoints.iterator()).hasNext()) break block8;
                    Spawnpoint p = iterator.next();
                    tilex = p.tilex;
                    tiley = p.tiley;
                    float posX = tilex * 4;
                    float posY = tiley * 4;
                    player.setPositionX(posX + 2.0f);
                    player.setPositionY(posY + 2.0f);
                    try {
                        player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                    }
                    catch (NoSuchZoneException nsz2) {
                        logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
                    }
                    player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
                    return;
                }
            }
            return;
        }
    }

    public static boolean putOutsideFence(Player player) {
        block7: {
            boolean moved = true;
            int tilex = player.getTileX();
            int tiley = player.getTileY();
            float posX = tilex * 4;
            float posY = tiley * 4;
            if (player.getBridgeId() <= 0L) {
                posX = posX + 0.5f + Server.rand.nextFloat() * 3.0f;
                posY = posY + 0.5f + Server.rand.nextFloat() * 3.0f;
            } else {
                posX += 2.0f;
                posY += 2.0f;
            }
            player.setPositionX(posX);
            player.setPositionY(posY);
            if (player.getFloorLevel() <= 0) {
                try {
                    player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                }
                catch (NoSuchZoneException nsz) {
                    Iterator<Spawnpoint> iterator;
                    logger.log(Level.WARNING, player.getName() + " ending up outside map: " + player.getStatus().getPositionX() + ", " + player.getStatus().getPositionY() + ". Respawning.");
                    player.calculateSpawnPoints();
                    if (player.spawnpoints == null || !(iterator = player.spawnpoints.iterator()).hasNext()) break block7;
                    Spawnpoint p = iterator.next();
                    tilex = p.tilex;
                    tiley = p.tiley;
                    posX = tilex * 4;
                    posY = tiley * 4;
                    player.setPositionX(posX + 2.0f);
                    player.setPositionY(posY + 2.0f);
                    try {
                        player.setPositionZ(Zones.calculateHeight(posX, posY, true));
                    }
                    catch (NoSuchZoneException nsz2) {
                        logger.log(Level.WARNING, player.getName() + " Respawn failed at spawnpoint " + tilex + "," + tiley);
                    }
                    player.getCommunicator().sendNormalServerMessage("You have been respawned since your position was out of bounds.");
                    return true;
                }
            }
        }
        return true;
    }

    public static final boolean willGoOnBoat(Player player) {
        long vehicleId;
        Vehicle vehic;
        MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
        if (mt != null && (vehic = Vehicles.getVehicleForId(vehicleId = mt.getVehicleId())) != null) {
            if (!vehic.creature) {
                try {
                    Item i = Items.getItem(vehicleId);
                    if (i.isBoat()) {
                        return true;
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to locate boat with id " + vehicleId + " for player " + player.getName(), ex);
                }
            } else {
                try {
                    Creatures.getInstance().getCreature(vehicleId);
                    return true;
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to locate creature with id " + vehicleId + " for player " + player.getName(), ex);
                }
            }
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static final boolean putInBoatAndAssignSeat(Player player, boolean reconnect) {
        Vehicle vehic;
        MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
        if (mt != null || player.getVehicle() != -10L && !reconnect) return false;
        long vehicleId = player.getSaveFile().lastvehicle;
        if (reconnect) {
            vehicleId = player.getVehicle();
        }
        if ((vehic = Vehicles.getVehicleForId(vehicleId)) == null) return false;
        try {
            Structure structure;
            int x;
            Item i = null;
            Creature creature = null;
            int freeseatnum = -1;
            float offz = 0.0f;
            float offx = 0.0f;
            float offy = 0.0f;
            int start = 9999;
            float posx = 50.0f;
            float posy = 50.0f;
            int layer = 0;
            if (WurmId.getType(vehicleId) == 2) {
                i = Items.getItem(vehicleId);
                if (!reconnect && !i.isBoat()) return false;
                posx = i.getPosX();
                posy = i.getPosY();
                int n = layer = i.isOnSurface() ? 0 : -1;
                if ((VehicleBehaviour.hasKeyForVehicle(player, i) || VehicleBehaviour.mayDriveVehicle(player, i, null)) && VehicleBehaviour.canBeDriverOfVehicle(player, vehic)) {
                    start = 1;
                } else if (VehicleBehaviour.hasKeyForVehicle(player, i) || VehicleBehaviour.mayEmbarkVehicle((Creature)player, i)) {
                    start = 1;
                } else {
                    logger.log(Level.INFO, player.getName() + " may no longer embark the vehicle " + i.getName());
                }
            } else if (WurmId.getType(vehicleId) == 1) {
                creature = Creatures.getInstance().getCreature(vehicleId);
                posx = creature.getPosX();
                posy = creature.getPosY();
                layer = creature.getLayer();
                if (VehicleBehaviour.mayDriveVehicle(player, creature) && VehicleBehaviour.canBeDriverOfVehicle(player, vehic)) {
                    start = 1;
                } else if (VehicleBehaviour.mayEmbarkVehicle((Creature)player, creature)) {
                    start = 1;
                } else {
                    logger.log(Level.INFO, player.getName() + " may no longer mount the " + creature.getName());
                }
            }
            for (x = 0; x < vehic.seats.length; ++x) {
                if (vehic.seats[x].occupant != player.getWurmId()) continue;
                freeseatnum = x;
                offz = vehic.seats[x].offz;
                offy += vehic.seats[x].offy;
                offx += vehic.seats[x].offx;
            }
            if (freeseatnum < 0) {
                for (x = start; x < vehic.seats.length; ++x) {
                    if (vehic.seats[x].occupant != -10L || freeseatnum >= 0) continue;
                    freeseatnum = x;
                    offz = vehic.seats[x].offz;
                    offy += vehic.seats[x].offy;
                    offx += vehic.seats[x].offx;
                }
            }
            player.setPositionX(posx + offx);
            player.setPositionY(posy + offy);
            VolaTile tile = Zones.getOrCreateTile((int)(player.getPosX() / 4.0f), (int)(player.getPosY() / 4.0f), player.getLayer() >= 0);
            boolean skipSetZ = false;
            if (tile != null && (structure = tile.getStructure()) != null) {
                boolean bl = skipSetZ = structure.isTypeHouse() || structure.getWurmId() == player.getBridgeId();
            }
            if (!skipSetZ) {
                player.setPositionZ(Math.max(Zones.calculateHeight(posx + offx, posy + offy, layer >= 0) + offz, freeseatnum >= 0 ? offz : -1.45f));
            }
            if (freeseatnum < 0) return true;
            mt = new MountTransfer(vehicleId, freeseatnum == 0 ? player.getWurmId() : -10L);
            mt.addToSeat(player.getWurmId(), freeseatnum);
            return true;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "No item to board for " + player.getName() + ":" + vehicleId, nsi);
            return false;
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        return false;
    }

    public static final boolean checkPutOnBoat(Player player) {
        long vehicleId;
        Vehicle vehic;
        MountTransfer mt = MountTransfer.getTransferFor(player.getWurmId());
        if (mt != null && (vehic = Vehicles.getVehicleForId(vehicleId = mt.getVehicleId())) != null) {
            if (vehic.isChair()) {
                return false;
            }
            try {
                Item i = null;
                Creature creature = null;
                if (WurmId.getType(vehicleId) == 2) {
                    i = Items.getItem(vehicleId);
                } else if (WurmId.getType(vehicleId) == 1) {
                    creature = Creatures.getInstance().getCreature(vehicleId);
                }
                int seatnum = mt.getSeatFor(player.getWurmId());
                if (seatnum >= 0) {
                    vehic.seats[seatnum].occupant = player.getWurmId();
                    if (mt.getPilotId() == player.getWurmId()) {
                        vehic.pilotId = player.getWurmId();
                        player.setVehicleCommander(true);
                    }
                    MountAction m = new MountAction(creature, i, vehic, seatnum, mt.getPilotId() == player.getWurmId(), vehic.seats[seatnum].offz);
                    player.setMountAction(m);
                    player.setVehicle(vehicleId, false, vehic.seats[seatnum].getType());
                    return true;
                }
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "No item to board for " + player.getName() + ":" + vehicleId, nsi);
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, "No creature to mount for " + player.getName() + ":" + vehicleId, nsc);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return false;
    }

    public static void sendWho(Player player, boolean loggingin) {
        if (player.isUndead()) {
            return;
        }
        String[] names = Players.getInstance().getPlayerNames();
        String playerList = "none!";
        Communicator comm = player.getCommunicator();
        int otherServers = 0;
        String localServerName = Servers.localServer.name;
        if (localServerName.length() > 1) {
            localServerName = localServerName.toLowerCase();
            localServerName = Character.toUpperCase(localServerName.charAt(0)) + localServerName.substring(1);
        }
        int epic = 0;
        for (ServerEntry entry : Servers.getAllServers()) {
            if (!entry.EPIC) {
                if (entry.isLocal) continue;
                otherServers += entry.currentPlayers;
                continue;
            }
            epic += entry.currentPlayers;
        }
        if (player.getPower() > 0) {
            comm.sendSafeServerMessage("These other players are online on " + localServerName + ":");
            int nums = names.length;
            if (names.length > 1) {
                playerList = "";
                for (int x = 0; x < names.length; ++x) {
                    if (!names[x].equals(player.getName())) {
                        PlayerInfo p = PlayerInfoFactory.createPlayerInfo(names[x]);
                        if (player.getPower() >= p.getPower()) {
                            playerList = playerList + names[x] + " ";
                        } else {
                            --nums;
                        }
                    }
                    if (x == 0 || x % 10 != 0) continue;
                    comm.sendSafeServerMessage(playerList);
                    playerList = "";
                }
                if (playerList.length() > 0) {
                    comm.sendSafeServerMessage(playerList);
                }
                String ss = "";
                if (names.length > 1) {
                    ss = "s";
                }
                comm.sendSafeServerMessage(nums + " player" + ss + " on this server. (" + (nums + otherServers + epic) + " totally in Wurm)");
            } else {
                comm.sendSafeServerMessage("none! (" + (nums + otherServers + epic) + " totally in Wurm)");
            }
        } else if (names.length > 1) {
            comm.sendSafeServerMessage(names.length - 1 + " other players are online. You are on " + localServerName + " (" + (names.length + otherServers + epic) + " totally in Wurm).");
        } else {
            comm.sendSafeServerMessage("No other players are online on " + localServerName + " (" + (1 + otherServers) + " totally in Wurm).");
        }
    }

    private static void sendLoggedInPeople(Player player) {
        if (player.isUndead()) {
            return;
        }
        if (player.isSignedIn()) {
            player.getCommunicator().signIn("Just transferred.");
        } else if (player.canSignIn() && player.getPower() <= 1) {
            player.getCommunicator().remindToSignIn();
        }
        LoginHandler.sendWho(player, true);
        Village vill = player.getCitizenVillage();
        if (vill != null) {
            vill.sendCitizensToPlayer(player);
        }
        if (player.mayHearMgmtTalk() || player.mayHearDevTalk()) {
            Players.getInstance().sendGmsToPlayer(player);
        }
        if (player.seesPlayerAssistantWindow()) {
            Players.getInstance().sendPAWindow(player);
        }
        if (player.seesGVHelpWindow() && !Servers.isThisLoginServer()) {
            Players.getInstance().sendGVHelpWindow(player);
        }
        Players.getInstance().sendAltarsToPlayer(player);
        Players.getInstance().sendTicketsToPlayer(player);
        player.checkKingdom();
        if (player.isGlobalChat()) {
            Players.getInstance().sendStartGlobalKingdomChat(player);
        }
        if (player.isKingdomChat()) {
            Players.getInstance().sendStartKingdomChat(player);
        }
        if (player.isTradeChannel()) {
            Players.getInstance().sendStartGlobalTradeChannel(player);
        }
    }

    private static void sendStatus(Player player) {
        player.getStatus().sendHunger();
        player.getStatus().sendThirst();
        player.getStatus().lastSentStamina = -200;
        player.getStatus().sendStamina();
        player.sendDeityEffectBonuses();
        player.getCommunicator().sendOwnTitles();
        player.getCommunicator().sendSleepInfo();
        player.sendAllPoisonEffect();
        Abilities.sendEffectsToCreature(player);
    }

    private void sendLoginAnswer(boolean ok, String message, float x, float y, float z, float rot, int layer, String bodyName, byte power, int retrySeconds) throws IOException {
        this.sendLoginAnswer(ok, message, x, y, z, rot, layer, bodyName, power, retrySeconds, (byte)0, (byte)0, 0L, 0, (byte)0, -10L, 0.0f);
    }

    public void sendLoginAnswer(boolean ok, String message, float x, float y, float z, float rot, int layer, String bodyName, byte power, int retrySeconds, byte commandType, byte templateKingdomId, long face, int teleportCounter, byte blood, long bridgeId, float groundOffset) throws IOException {
        try {
            if (Constants.useQueueToSendDataToPlayers) {
                // empty if block
            }
            byte[] messageb = message.getBytes(MESSAGE_FORMAT_UTF_8);
            ByteBuffer bb = this.conn.getBuffer();
            bb.put((byte)-15);
            if (ok) {
                bb.put((byte)1);
            } else {
                bb.put((byte)0);
            }
            bb.putShort((short)messageb.length);
            bb.put(messageb);
            bb.put((byte)layer);
            bb.putLong(WurmCalendar.currentTime);
            bb.putLong(System.currentTimeMillis());
            bb.putFloat(rot);
            bb.putFloat(x);
            bb.putFloat(y);
            bb.putFloat(z);
            byte[] bodyb = bodyName.getBytes(MESSAGE_FORMAT_UTF_8);
            bb.putShort((short)bodyb.length);
            bb.put(bodyb);
            if (power == 0) {
                bb.put((byte)0);
            } else if (power == 1) {
                bb.put((byte)2);
            } else {
                bb.put((byte)1);
            }
            bb.put(commandType);
            bb.putShort((short)retrySeconds);
            bb.putLong(face);
            bb.put(templateKingdomId);
            bb.putInt(teleportCounter);
            bb.put(blood);
            bb.putLong(bridgeId);
            bb.putFloat(groundOffset);
            bb.putInt(Zones.worldTileSizeX);
            this.conn.flush();
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send login answer.", ex);
        }
    }

    public void sendAuthenticationAnswer(boolean wasSucces, String failedMessage) {
        ByteBuffer bb = this.conn.getBuffer();
        bb.put((byte)-52);
        if (wasSucces) {
            bb.put((byte)1);
        } else {
            bb.put((byte)0);
        }
        try {
            byte[] failedMessageb = failedMessage.getBytes(MESSAGE_FORMAT_UTF_8);
            bb.putShort((short)failedMessageb.length);
            bb.put(failedMessageb);
        }
        catch (Exception e) {
            bb.putShort((short)0);
        }
        try {
            this.conn.flush();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to send Auth answer.", ex);
        }
    }

    public static byte[] createAndReturnPlayer(String name, String password, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed) throws Exception {
        return LoginHandler.createAndReturnPlayer(name, password, pwQuestion, pwAnswer, email, kingdom, power, appearance, gender, titleKeeper, addPremium, passwordIsHashed, -10L);
    }

    public static byte[] createAndReturnPlayer(String name, String password, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed, long wurmId) throws Exception {
        if (Servers.localServer.HOMESERVER && Servers.localServer.KINGDOM != kingdom) {
            throw new WurmServerException("Illegal kingdom");
        }
        name = LoginHandler.raiseFirstLetter(name);
        if (!passwordIsHashed) {
            try {
                password = LoginHandler.hashPassword(password, LoginHandler.encrypt(name));
            }
            catch (Exception ex) {
                throw new WurmServerException("We failed to encrypt your password. Please try another.");
            }
        }
        if (wurmId < 0L) {
            String result = LoginHandler.checkName2(name);
            if (result.length() > 0) {
                throw new WurmServerException(result);
            }
            if (Players.getInstance().getWurmIdByPlayerName(name) != -1L) {
                throw new WurmServerException("That name is taken.");
            }
        }
        Player player = Player.doNewPlayer(1);
        player.setName(name);
        int startx = Servers.localServer.SPAWNPOINTJENNX;
        int starty = Servers.localServer.SPAWNPOINTJENNY;
        Spawnpoint spawn = LoginHandler.getInitialSpawnPoint(kingdom);
        if (spawn != null) {
            startx = spawn.tilex;
            starty = spawn.tiley;
        } else if (kingdom == 3) {
            if (Servers.localServer.SPAWNPOINTLIBX > 0) {
                startx = Servers.localServer.SPAWNPOINTLIBX;
                starty = Servers.localServer.SPAWNPOINTLIBY;
            }
        } else if (kingdom == 2 && Servers.localServer.SPAWNPOINTMOLX > 0) {
            startx = Servers.localServer.SPAWNPOINTMOLX;
            starty = Servers.localServer.SPAWNPOINTMOLY;
        }
        if (Servers.localServer.id == 5) {
            startx = 2884;
            starty = 3004;
        }
        float posX = (float)(startx * 4) + Server.rand.nextFloat() * 2.0f * 4.0f - 4.0f;
        float posY = (float)(starty * 4) + Server.rand.nextFloat() * 2.0f * 4.0f - 4.0f;
        float rot = (float)Server.rand.nextInt(45) - 22.5f;
        if (wurmId < 0L) {
            player.setWurmId(WurmId.getNextPlayerId(), posX, posY, rot, 0);
        } else {
            player.setWurmId(wurmId, posX, posY, rot, 0);
        }
        LoginHandler.putOutsideWall(player);
        if (player.isOnSurface()) {
            LoginHandler.putOutsideHouse(player, false);
            LoginHandler.putOutsideFence(player);
        }
        player.setNewPlayer(true);
        PlayerInfo file = PlayerInfoFactory.createPlayerInfo(name);
        file.initialize(name, player.getWurmId(), password, pwQuestion, pwAnswer, appearance, false);
        player.getStatus().setStatusExists(true);
        file.setEmailAddress(email);
        file.loaded = true;
        player.setSaveFile(file);
        file.togglePlayerAssistantWindow(true);
        player.loadSkills();
        player.getBody().createBodyParts();
        player.createPossessions();
        player.createSomeItems(1.0f, false);
        player.setPower(power);
        LoginHandler.checkReimbursement(player);
        player.setSex(gender, true);
        player.getStatus().setKingdom(kingdom);
        player.setFlag(53, true);
        player.setFlag(76, true);
        Players.loadAllPrivatePOIForPlayer(player);
        player.sendAllMapAnnotations();
        ValreiMapData.sendAllMapData(player);
        player.setFullyLoaded();
        if (power > 0) {
            file.setReimbursed(false);
        }
        if (titleKeeper) {
            if (kingdom == 3) {
                player.addTitle(Titles.Title.Destroyer_Faith);
            } else {
                player.addTitle(Titles.Title.Keeper_Faith);
            }
        }
        if (addPremium) {
            file.setPaymentExpire(System.currentTimeMillis());
        }
        player.sleep();
        PlayerInfoFactory.addPlayerInfo(file);
        Server.addNewbie();
        return PlayerTransfer.createPlayerData(Wounds.emptyWounds, player.getSaveFile(), player.getStatus(), player.getAllItems(), player.getSkills().getSkillsNoTemp(), null, Servers.localServer.id, 0L, kingdom);
    }

    public static final Spawnpoint getInitialSpawnPoint(byte kingdom) {
        Village[] villages;
        if ((!Servers.localServer.entryServer || Server.getInstance().isPS()) && (villages = Villages.getPermanentVillages(kingdom)).length > 0) {
            Village chosen = villages[Server.rand.nextInt(villages.length)];
            return new Spawnpoint(chosen.getName(), 1, chosen.getMotto(), (short)chosen.getTokenX(), (short)chosen.getTokenY(), true, chosen.kingdom);
        }
        return null;
    }

    public static long createPlayer(String name, String password, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender) throws Exception {
        try {
            password = LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(name)));
        }
        catch (Exception ex) {
            throw new WurmServerException("We failed to encrypt your password. Please try another.");
        }
        return LoginHandler.createPlayer(name, password, pwQuestion, pwAnswer, email, kingdom, power, appearance, gender, false, false, -10L);
    }

    public static long createPlayer(String name, String hashedPassword, String pwQuestion, String pwAnswer, String email, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, long wurmId) throws Exception {
        if (Servers.localServer.HOMESERVER && Servers.localServer.KINGDOM != kingdom) {
            kingdom = Servers.localServer.KINGDOM;
        }
        name = LoginHandler.raiseFirstLetter(name);
        if (wurmId < 0L) {
            String result = LoginHandler.checkName2(name);
            if (result.length() > 0) {
                throw new WurmServerException(result);
            }
            if (Players.getInstance().getWurmIdByPlayerName(name) != -1L) {
                throw new WurmServerException("That name is taken.");
            }
        }
        Player player = Player.doNewPlayer(1);
        player.setName(name);
        int startx = Servers.localServer.SPAWNPOINTJENNX;
        int starty = Servers.localServer.SPAWNPOINTJENNY;
        Spawnpoint spawn = LoginHandler.getInitialSpawnPoint(kingdom);
        if (spawn != null) {
            startx = spawn.tilex;
            starty = spawn.tiley;
        } else if (kingdom == 3) {
            if (Servers.localServer.SPAWNPOINTLIBX > 0) {
                startx = Servers.localServer.SPAWNPOINTLIBX;
                starty = Servers.localServer.SPAWNPOINTLIBY;
            }
        } else if (kingdom == 2 && Servers.localServer.SPAWNPOINTMOLX > 0) {
            startx = Servers.localServer.SPAWNPOINTMOLX;
            starty = Servers.localServer.SPAWNPOINTMOLY;
        }
        float posX = (float)(startx * 4) + Server.rand.nextFloat() * 2.0f * 4.0f - 4.0f;
        float posY = (float)(starty * 4) + Server.rand.nextFloat() * 2.0f * 4.0f - 4.0f;
        float rot = 4.0f;
        if (wurmId < 0L) {
            player.setWurmId(WurmId.getNextPlayerId(), posX, posY, 4.0f, 0);
        } else {
            player.setWurmId(wurmId, posX, posY, 4.0f, 0);
        }
        Players.getInstance().addPlayer(player);
        player.setNewPlayer(true);
        PlayerInfo file = PlayerInfoFactory.createPlayerInfo(name);
        file.initialize(name, player.getWurmId(), hashedPassword, pwQuestion, pwAnswer, appearance, false);
        player.getStatus().setStatusExists(true);
        file.loaded = true;
        player.setSaveFile(file);
        file.togglePlayerAssistantWindow(true);
        file.setEmailAddress(email);
        LoginHandler.putOutsideWall(player);
        if (player.isOnSurface()) {
            LoginHandler.putOutsideHouse(player, false);
            LoginHandler.putOutsideFence(player);
        }
        player.loadSkills();
        player.createPossessions();
        player.getBody().createBodyParts();
        player.createSomeItems(1.0f, false);
        player.setPower(power);
        LoginHandler.checkReimbursement(player);
        player.setSex(gender, true);
        player.getStatus().setKingdom(kingdom);
        player.setFlag(53, true);
        player.setFlag(76, true);
        Players.loadAllPrivatePOIForPlayer(player);
        player.sendAllMapAnnotations();
        ValreiMapData.sendAllMapData(player);
        Kingdom k = Kingdoms.getKingdom(kingdom);
        if (k != null) {
            if (k.isCustomKingdom()) {
                player.calculateSpawnPoints();
                Set<Spawnpoint> spawns = player.spawnpoints;
                if (spawns != null) {
                    for (Spawnpoint sp : spawns) {
                        if (sp.tilex <= 20 || sp.tilex >= Zones.worldTileSizeX - 20 || sp.tiley <= 20 || sp.tiley >= Zones.worldTileSizeY - 20) continue;
                        float nposX = (float)(sp.tilex * 4 + 1) + Server.rand.nextFloat() * 2.0f;
                        float nposY = (float)(sp.tiley * 4 + 1) + Server.rand.nextFloat() * 2.0f;
                        player.getStatus().setPositionXYZ(nposX, nposY, Zones.calculateHeight(nposX, nposY, true));
                        break;
                    }
                }
            } else {
                if (kingdom == 3) {
                    player.setDeity(Deities.getDeity(4));
                    player.setFaith(1.0f);
                }
                if (Servers.localServer.entryServer && Players.getInstance().getNumberOfPlayers() > 100) {
                    player.calculateSpawnPoints();
                    Set<Spawnpoint> spawns = player.spawnpoints;
                    if (spawns != null && spawns.size() > 0) {
                        int rand = Server.rand.nextInt(spawns.size());
                        int current = 0;
                        for (Spawnpoint sp : spawns) {
                            if (rand == current) {
                                startx = sp.tilex;
                                starty = sp.tiley;
                                float posNX = (float)(startx * 4) + Server.rand.nextFloat() * 2.0f * 4.0f - 4.0f;
                                float posNY = (float)(starty * 4) + Server.rand.nextFloat() * 2.0f * 4.0f - 4.0f;
                                player.getStatus().getPosition().setPosX(posNX);
                                player.getStatus().getPosition().setPosY(posNY);
                                player.updateEffects();
                                break;
                            }
                            ++current;
                        }
                    }
                }
            }
        }
        player.setFullyLoaded();
        if (power > 0) {
            file.setReimbursed(false);
        }
        if (titleKeeper) {
            if (kingdom == 3) {
                player.addTitle(Titles.Title.Destroyer_Faith);
            } else {
                player.addTitle(Titles.Title.Keeper_Faith);
            }
        }
        if (addPremium) {
            file.setPaymentExpire(System.currentTimeMillis());
        }
        player.sleep();
        PlayerInfoFactory.addPlayerInfo(file);
        Server.addNewbie();
        Players.getInstance().removePlayer(player);
        return player.getWurmId();
    }

    private boolean checkName(String name) {
        String result = LoginHandler.checkName2(name);
        if (result.length() > 0) {
            block3: {
                try {
                    this.sendLoginAnswer(false, result, 0.0f, 0.0f, 0.0f, 0.0f, 0, BROKEN_PLAYER_MODEL, (byte)0, 0);
                }
                catch (IOException ioe) {
                    if (!logger.isLoggable(Level.FINE)) break block3;
                    logger.log(Level.FINE, this.conn.getIp() + PROBLEM_SENDING_LOGIN_DENIED_MESSAGE + result, ioe);
                }
            }
            return false;
        }
        return true;
    }

    private static void sendAllItemModelNames(Player player) {
        for (ItemTemplate item : ItemTemplateFactory.getInstance().getTemplates()) {
            if (item.isNoTake()) continue;
            player.getCommunicator().sendItemTemplateList(item.getTemplateId(), item.getModelName());
        }
    }

    private static void sendAllEquippedArmor(Player player) {
        for (Item item : player.getBody().getContainersAndWornItems()) {
            if (item == null) continue;
            try {
                byte armorSlot = item.isArmour() ? BodyTemplate.convertToArmorEquipementSlot((byte)item.getParent().getPlace()) : BodyTemplate.convertToItemEquipementSlot((byte)item.getParent().getPlace());
                player.getCommunicator().sendWearItem(-1L, item.getTemplateId(), armorSlot, WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), WurmColor.getColorBlue(item.getColor2()), item.getMaterial(), item.getRarity());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static final String checkName2(String name) {
        boolean notok = LoginHandler.containsIllegalCharacters(name);
        if (notok) {
            return "Please use only letters from a to z in your name.";
        }
        if (name.length() < 3) {
            return "Please use a name at least 3 letters long.";
        }
        if (name.length() > 40) {
            return "Please use a name no longer than 40 letters.";
        }
        if (!Deities.isNameOkay(name) || !CreatureTemplateFactory.isNameOkay(name)) {
            return "Illegal name.";
        }
        return "";
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = salt.getBytes();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, 1000, 192);
        SecretKeyFactory key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hashedPassword = key.generateSecret(spec).getEncoded();
        return String.format("%x", new BigInteger(hashedPassword));
    }

    public static String encrypt(String plaintext) throws Exception {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WurmServerException("No such algorithm 'SHA'", e);
        }
        try {
            md.update(plaintext.getBytes(MESSAGE_FORMAT_UTF_8));
        }
        catch (UnsupportedEncodingException e) {
            throw new WurmServerException("No such encoding: UTF-8", e);
        }
        byte[] raw = md.digest();
        String hash = new BASE64Encoder().encode(raw);
        return hash;
    }

    public String getConnectionIp() {
        if (this.conn != null) {
            return this.conn.getIp();
        }
        return "";
    }

    static /* synthetic */ void access$000(LoginHandler x0, int x1) {
        x0.setHasLoadedItems(x1);
    }

    static /* synthetic */ Logger access$100() {
        return logger;
    }
}


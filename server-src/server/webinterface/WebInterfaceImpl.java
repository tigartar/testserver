/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PasswordTransfer;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Reimbursement;
import com.wurmonline.server.questions.AscensionQuestion;
import com.wurmonline.server.questions.NewsInfo;
import com.wurmonline.server.questions.WurmInfo;
import com.wurmonline.server.questions.WurmInfo2;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillStat;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcKingdomInfo;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.server.webinterface.WebInterfaceTest;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebInterfaceImpl
extends UnicastRemoteObject
implements WebInterface,
Serializable,
MiscConstants,
TimeConstants,
CounterTypes,
MonetaryConstants {
    public static final String VERSION = "$Revision: 1.54 $";
    public static String mailAccount = "mail@mydomain.com";
    public static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[\\w\\.\\+-=]+@[\\w\\.-]+\\.[\\w-]+$");
    private static final String PASSWORD_CHARS = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789";
    private static final long serialVersionUID = -2682536434841429586L;
    private final boolean isRunning = true;
    private final Random faceRandom = new Random();
    private static final long faceRandomSeed = 8263186381637L;
    private static final DecimalFormat twoDecimals = new DecimalFormat("##0.00");
    private static final Set<String> moneyDetails = new HashSet<String>();
    private static final Set<String> timeDetails = new HashSet<String>();
    private static final Logger logger = Logger.getLogger(WebInterfaceImpl.class.getName());
    private static final long[] noInfoLong = new long[]{-1L, -1L};
    private static final String BAD_PASSWORD = "Access denied.";
    private final SimpleDateFormat alloformatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
    private String hostname = "localhost";
    private static final Map<String, Long> ipAttempts = new HashMap<String, Long>();
    private String[] bannedMailHosts = new String[]{"sharklasers", "spam4", "grr.la", "guerrillamail"};
    static final int[] emptyIntZero = new int[]{0, 0};

    public WebInterfaceImpl(int port) throws RemoteException {
        super(port);
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            this.hostname = localMachine.getHostName();
            logger.info("Hostname of local machine used to send registration emails: " + this.hostname);
        }
        catch (UnknownHostException uhe) {
            throw new RemoteException("Could not find localhost for WebInterface", uhe);
        }
    }

    public WebInterfaceImpl() throws RemoteException {
    }

    private String getRemoteClientDetails() {
        try {
            return WebInterfaceImpl.getClientHost();
        }
        catch (ServerNotActiveException e) {
            logger.log(Level.WARNING, "Could not get ClientHost details due to " + e.getMessage(), e);
            return "Unknown Remote Client";
        }
    }

    @Override
    public int getPower(String intraServerPassword, long aPlayerID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPower for playerID: " + aPlayerID);
        }
        try {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
            p.load();
            return p.getPower();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, aPlayerID + ": " + iox.getMessage(), iox);
            return 0;
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
            return 0;
        }
    }

    @Override
    public boolean isRunning(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " isRunning");
        }
        return true;
    }

    @Override
    public int getPlayerCount(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayerCount");
        }
        return Players.getInstance().numberOfPlayers();
    }

    @Override
    public int getPremiumPlayerCount(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPremiumPlayerCount");
        }
        return Players.getInstance().numberOfPremiumPlayers();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getTestMessage(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getTestMessage");
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            return "HEj! " + System.currentTimeMillis();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void broadcastMessage(String intraServerPassword, String message) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " broadcastMessage: " + message);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            Server.getInstance().broadCastAlert(message);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getAccountStatusForPlayer(String intraServerPassword, String playerName) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getAccountStatusForPlayer for player: " + playerName);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            if (Servers.localServer.id != Servers.loginServer.id) {
                throw new RemoteException("Not a valid request for this server. Ask the login server instead.");
            }
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                return p.money;
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, playerName + ": " + iox.getMessage(), iox);
                return 0L;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, Integer> getBattleRanks(String intraServerPassword, int numberOfRanksToGet) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getBattleRanks number of Ranks: " + numberOfRanksToGet);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            return Players.getBattleRanks(numberOfRanksToGet);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String getServerStatus(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getServerStatus");
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            String toReturn = "Up and running.";
            if (Server.getMillisToShutDown() > -1000L) {
                toReturn = "Shutting down in " + Server.getMillisToShutDown() / 1000L + " seconds: " + Server.getShutdownReason();
            } else if (Constants.maintaining) {
                toReturn = "The server is in maintenance mode and not open for connections.";
            }
            return toReturn;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, Long> getFriends(String intraServerPassword, long aPlayerID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getFriends for playerid: " + aPlayerID);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            return Players.getFriends(aPlayerID);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, String> getInventory(String intraServerPassword, long aPlayerID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getInventory for playerid: " + aPlayerID);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<String, String> toReturn = new HashMap<String, String>();
            try {
                Player p = Players.getInstance().getPlayer(aPlayerID);
                Item inventory = p.getInventory();
                Item[] items = inventory.getAllItems(false);
                for (int x = 0; x < items.length; ++x) {
                    toReturn.put(String.valueOf(items[x].getWurmId()), items[x].getName() + ", QL: " + items[x].getQualityLevel() + ", DAM: " + items[x].getDamage());
                }
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
            return toReturn;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<Long, Long> getBodyItems(String intraServerPassword, long aPlayerID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getBodyItems for playerid: " + aPlayerID);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<Long, Long> toReturn = new HashMap<Long, Long>();
            try {
                Player p = Players.getInstance().getPlayer(aPlayerID);
                Body lBody = p.getBody();
                if (lBody != null) {
                    Item[] items = lBody.getAllItems();
                    for (int x = 0; x < items.length; ++x) {
                        toReturn.put(items[x].getWurmId(), items[x].getParentId());
                    }
                }
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
            return toReturn;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, Float> getSkills(String intraServerPassword, long aPlayerID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getSkills for playerid: " + aPlayerID);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<String, Float> toReturn = new HashMap<String, Float>();
            Skills skills = SkillsFactory.createSkills(aPlayerID);
            try {
                skills.load();
                Skill[] skillarr = skills.getSkills();
                for (int x = 0; x < skillarr.length; ++x) {
                    toReturn.put(skillarr[x].getName(), new Float(skillarr[x].getKnowledge(0.0)));
                }
            }
            catch (Exception iox) {
                logger.log(Level.WARNING, aPlayerID + ": " + iox.getMessage(), iox);
            }
            return toReturn;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, ?> getPlayerSummary(String intraServerPassword, long aPlayerID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayerSummary for playerid: " + aPlayerID);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<String, Object> toReturn = new HashMap<String, Object>();
            if (WurmId.getType(aPlayerID) == 0) {
                try {
                    Player p = Players.getInstance().getPlayer(aPlayerID);
                    toReturn.put("Name", p.getName());
                    if (p.citizenVillage != null) {
                        Citizen citiz = p.citizenVillage.getCitizen(aPlayerID);
                        toReturn.put("CitizenVillage", p.citizenVillage.getName());
                        toReturn.put("CitizenRole", citiz.getRole().getName());
                    }
                    String location = "unknown";
                    if (p.currentVillage != null) {
                        location = p.currentVillage.getName() + ", in " + Kingdoms.getNameFor(p.currentVillage.kingdom);
                    } else if (p.currentKingdom != 0) {
                        location = Kingdoms.getNameFor(p.currentKingdom);
                    }
                    toReturn.put("Location", location);
                    if (p.getDeity() != null) {
                        toReturn.put("Deity", p.getDeity().name);
                    }
                    toReturn.put("Faith", new Float(p.getFaith()));
                    toReturn.put("Favor", new Float(p.getFavor()));
                    toReturn.put("Gender", p.getSex());
                    toReturn.put("Alignment", new Float(p.getAlignment()));
                    toReturn.put("Kingdom", p.getKingdomId());
                    toReturn.put("Battle rank", p.getRank());
                    toReturn.put("WurmId", new Long(aPlayerID));
                    toReturn.put("Banned", p.getSaveFile().isBanned());
                    toReturn.put("Money in bank", p.getMoney());
                    toReturn.put("Payment", new Date(p.getPaymentExpire()));
                    toReturn.put("Email", p.getSaveFile().emailAddress);
                    toReturn.put("Current server", Servers.localServer.id);
                    toReturn.put("Last login", new Date(p.getLastLogin()));
                    toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
                    if (p.getSaveFile().isBanned()) {
                        toReturn.put("IPBan reason", p.getSaveFile().banreason);
                        toReturn.put("IPBan expires in", Server.getTimeFor(p.getSaveFile().banexpiry - System.currentTimeMillis()));
                    }
                    toReturn.put("Warnings", String.valueOf(p.getSaveFile().getWarnings()));
                    if (p.isMute()) {
                        toReturn.put("Muted", Boolean.TRUE);
                        toReturn.put("Mute reason", p.getSaveFile().mutereason);
                        toReturn.put("Mute expires in", Server.getTimeFor(p.getSaveFile().muteexpiry - System.currentTimeMillis()));
                    }
                    toReturn.put("PlayingTime", Server.getTimeFor(p.getSaveFile().playingTime));
                    toReturn.put("Reputation", p.getReputation());
                    if (p.getTitle() != null || Features.Feature.COMPOUND_TITLES.isEnabled() && p.getSecondTitle() != null) {
                        toReturn.put("Title", p.getTitleString());
                    }
                    toReturn.put("Coord x", (int)p.getStatus().getPositionX() >> 2);
                    toReturn.put("Coord y", (int)p.getStatus().getPositionY() >> 2);
                    if (p.isPriest()) {
                        toReturn.put("Priest", Boolean.TRUE);
                    }
                    toReturn.put("LoggedOut", p.loggedout);
                }
                catch (NoSuchPlayerException nsp) {
                    try {
                        PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
                        p.load();
                        toReturn.put("Name", p.getName());
                        if (p.getDeity() != null) {
                            toReturn.put("Deity", p.getDeity().name);
                        }
                        toReturn.put("Faith", new Float(p.getFaith()));
                        toReturn.put("Favor", new Float(p.getFavor()));
                        toReturn.put("Current server", p.currentServer);
                        toReturn.put("Alignment", new Float(p.getAlignment()));
                        toReturn.put("Battle rank", p.getRank());
                        toReturn.put("WurmId", new Long(aPlayerID));
                        toReturn.put("Banned", p.isBanned());
                        toReturn.put("Money in bank", new Long(p.money));
                        toReturn.put("Payment", new Date(p.getPaymentExpire()));
                        toReturn.put("Email", p.emailAddress);
                        toReturn.put("Last login", new Date(p.getLastLogin()));
                        toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
                        if (p.isBanned()) {
                            toReturn.put("IPBan reason", p.banreason);
                            toReturn.put("IPBan expires in", Server.getTimeFor(p.banexpiry - System.currentTimeMillis()));
                        }
                        toReturn.put("Warnings", String.valueOf(p.getWarnings()));
                        if (p.isMute()) {
                            toReturn.put("Muted", Boolean.TRUE);
                            toReturn.put("Mute reason", p.mutereason);
                            toReturn.put("Mute expires in", Server.getTimeFor(p.muteexpiry - System.currentTimeMillis()));
                        }
                        toReturn.put("PlayingTime", Server.getTimeFor(p.playingTime));
                        toReturn.put("Reputation", p.getReputation());
                        if (p.title != null && p.title.getName(true) != null) {
                            toReturn.put("Title", p.title.getName(true));
                        }
                        if (p.isPriest) {
                            toReturn.put("Priest", Boolean.TRUE);
                        }
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, aPlayerID + ":" + iox.getMessage(), iox);
                    }
                    catch (NoSuchPlayerException nsp2) {
                        logger.log(Level.WARNING, aPlayerID + ":" + nsp2.getMessage(), nsp2);
                    }
                }
            } else {
                toReturn.put("Not a player", String.valueOf(aPlayerID));
            }
            return toReturn;
        }
    }

    @Override
    public long getLocalCreationTime(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getLocalCreationTime");
        }
        return Server.getStartTime();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<Integer, String> getKingdoms(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getKingdoms");
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<Integer, String> toReturn = new HashMap<Integer, String>();
            if (Servers.localServer.HOMESERVER) {
                toReturn.put(Integer.valueOf(Servers.localServer.KINGDOM), Kingdoms.getNameFor(Servers.localServer.KINGDOM));
            } else {
                toReturn.put(1, Kingdoms.getNameFor((byte)1));
                toReturn.put(3, Kingdoms.getNameFor((byte)3));
                toReturn.put(2, Kingdoms.getNameFor((byte)2));
            }
            return toReturn;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<Long, String> getPlayersForKingdom(String intraServerPassword, int aKingdom) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayersForKingdom: " + aKingdom);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<Long, String> toReturn = new HashMap<Long, String>();
            Player[] players = Players.getInstance().getPlayers();
            for (int x = 0; x < players.length; ++x) {
                if (players[x].getKingdomId() != aKingdom) continue;
                toReturn.put(new Long(players[x].getWurmId()), players[x].getName());
            }
            return toReturn;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public long getPlayerId(String intraServerPassword, String name) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayerId for player name: " + name);
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            return Players.getInstance().getWurmIdByPlayerName(LoginHandler.raiseFirstLetter(name));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, ?> createPlayer(String intraServerPassword, String name, String password, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " createPlayer for player name: " + name);
        }
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        HashMap<String, Object> toReturn = new HashMap<String, Object>();
        logger.log(Level.INFO, "Trying to create player " + name);
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            if (WebInterfaceImpl.isEmailValid(emailAddress)) {
                try {
                    toReturn.put("PlayerId", new Long(LoginHandler.createPlayer(name, password, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
                }
                catch (Exception ex) {
                    toReturn.put("PlayerId", -1L);
                    toReturn.put("error", ex.getMessage());
                    logger.log(Level.WARNING, name + ":" + ex.getMessage(), ex);
                }
            } else {
                toReturn.put("error", "The email address " + emailAddress + " is not valid.");
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getPendingAccounts(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPendingAccounts");
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        for (Map.Entry<String, PendingAccount> entry : PendingAccount.accounts.entrySet()) {
            toReturn.put(entry.getKey(), entry.getValue().emailAddress + ", " + GeneralUtilities.toGMTString(entry.getValue().expiration));
        }
        return toReturn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    @Override
    public Map<String, String> createPlayerPhaseOne(String intraServerPassword, String aPlayerName, String aEmailAddress) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        HashMap<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("error", "The server is currently in maintenance mode.");
            return toReturn;
        }
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Trying to create player phase one " + aPlayerName);
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            aPlayerName = LoginHandler.raiseFirstLetter(aPlayerName);
            String errstat = LoginHandler.checkName2(aPlayerName);
            if (errstat.length() == 0) {
                if (PlayerInfoFactory.doesPlayerExist(aPlayerName)) {
                    toReturn.put("error", "The name " + aPlayerName + " is taken.");
                    return toReturn;
                }
                if (PendingAccount.doesPlayerExist(aPlayerName)) {
                    toReturn.put("error", "The name " + aPlayerName + " is reserved for up to two days.");
                    return toReturn;
                }
                if (!WebInterfaceImpl.isEmailValid(aEmailAddress)) {
                    toReturn.put("error", "The email " + aEmailAddress + " is invalid.");
                    return toReturn;
                }
                String[] numAccounts = PlayerInfoFactory.getAccountsForEmail(aEmailAddress);
                if (numAccounts.length >= 5) {
                    void var9_11;
                    String accnames = "";
                    boolean bl = false;
                    while (var9_11 < numAccounts.length) {
                        accnames = accnames + " " + numAccounts[var9_11];
                        ++var9_11;
                    }
                    toReturn.put("error", "You may only have 5 accounts. Please play Wurm with any of the following:" + accnames + ".");
                    return toReturn;
                }
                String[] numAccounts2 = PendingAccount.getAccountsForEmail(aEmailAddress);
                if (numAccounts2.length >= 5) {
                    void var9_13;
                    String string = "";
                    for (int x = 0; x < numAccounts2.length; ++x) {
                        String string2 = (String)var9_13 + " " + numAccounts2[x];
                    }
                    toReturn.put("error", "You may only have 5 accounts. The following accounts are awaiting confirmation by following the link in the verification email:" + (String)var9_13 + ".");
                    return toReturn;
                }
                String[] stringArray = this.bannedMailHosts;
                int x = stringArray.length;
                for (int i = 0; i < x; ++i) {
                    String blocked = stringArray[i];
                    if (!aEmailAddress.toLowerCase().contains(blocked)) continue;
                    String domain = aEmailAddress.substring(aEmailAddress.indexOf("@"), aEmailAddress.length());
                    toReturn.put("error", "We do not accept email addresses from :" + domain + ".");
                    return toReturn;
                }
                if (numAccounts.length + numAccounts2.length >= 5) {
                    void var9_19;
                    String string = "";
                    for (x = 0; x < numAccounts.length; ++x) {
                        void var9_17;
                        String string3 = (String)var9_17 + " " + numAccounts[x];
                    }
                    for (x = 0; x < numAccounts2.length; ++x) {
                        String string4 = (String)var9_19 + " " + numAccounts2[x];
                    }
                    toReturn.put("error", "You may only have 5 accounts. The following accounts are already registered or awaiting confirmation by following the link in the verification email:" + (String)var9_19 + ".");
                    return toReturn;
                }
                String string = WebInterfaceImpl.generateRandomPassword();
                long expireTime = System.currentTimeMillis() + 172800000L;
                PendingAccount pedd = new PendingAccount();
                pedd.accountName = aPlayerName;
                pedd.emailAddress = aEmailAddress;
                pedd.expiration = expireTime;
                pedd.password = string;
                if (pedd.create()) {
                    try {
                        if (!Constants.devmode) {
                            String email = Mailer.getPhaseOneMail();
                            email = email.replace("@pname", aPlayerName);
                            email = email.replace("@email", URLEncoder.encode(aEmailAddress, "UTF-8"));
                            email = email.replace("@expiration", GeneralUtilities.toGMTString(expireTime));
                            email = email.replace("@password", string);
                            Mailer.sendMail(mailAccount, aEmailAddress, "Wurm Online character creation request", email);
                        } else {
                            toReturn.put("Hash", string);
                            logger.log(Level.WARNING, "NO MAIL SENT: DEVMODE ACTIVE");
                        }
                        toReturn.put("ok", "An email has been sent to " + aEmailAddress + ". You will have to click a link in order to proceed with the registration.");
                    }
                    catch (Exception ex) {
                        toReturn.put("error", "An error occured when sending the mail: " + ex.getMessage() + ". No account was reserved.");
                        pedd.delete();
                        logger.log(Level.WARNING, aEmailAddress + ":" + ex.getMessage(), ex);
                    }
                } else {
                    toReturn.put("error", "The account could not be created. Please try later.");
                    logger.warning(aEmailAddress + " The account could not be created. Please try later.");
                }
            } else {
                toReturn.put("error", errstat);
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, ?> createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, String phaseOneHash) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " createPlayerPhaseTwo for player name: " + playerName);
        }
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        return this.createPlayerPhaseTwo(intraServerPassword, playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, 1);
    }

    @Override
    public Map<String, ?> createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, String phaseOneHash, int serverId) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        return this.createPlayerPhaseTwo(intraServerPassword, playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, phaseOneHash, serverId, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, ?> createPlayerPhaseTwo(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, String phaseOneHash, int serverId, boolean optInEmail) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        serverId = 1;
        appearance = Server.rand.nextInt(5);
        this.faceRandom.setSeed(8263186381637L + appearance);
        appearance = this.faceRandom.nextLong();
        kingdom = (byte)4;
        if (kingdom == 3) {
            serverId = 3;
        }
        HashMap<String, Object> toReturn = new HashMap<String, Object>();
        if (Constants.maintaining) {
            toReturn.put("error", "The server is currently in maintenance mode.");
            return toReturn;
        }
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Trying to create player phase two " + playerName);
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            block38: {
                if (playerName == null || hashedIngamePassword == null || challengePhrase == null || challengeAnswer == null || emailAddress == null || phaseOneHash == null) {
                    if (playerName == null) {
                        toReturn.put("error", "PlayerName is null.");
                    }
                    if (hashedIngamePassword == null) {
                        toReturn.put("error", "hashedIngamePassword is null.");
                    }
                    if (challengePhrase == null) {
                        toReturn.put("error", "ChallengePhrase is null.");
                    }
                    if (challengeAnswer == null) {
                        toReturn.put("error", "ChallengeAnswer is null.");
                    }
                    if (emailAddress == null) {
                        toReturn.put("error", "EmailAddress is null.");
                    }
                    if (phaseOneHash == null) {
                        toReturn.put("error", "phaseOneHash is null.");
                    }
                    return toReturn;
                }
                if (challengePhrase.equals(challengeAnswer)) {
                    toReturn.put("error", "We don't allow the password retrieval question and answer to be the same.");
                    return toReturn;
                }
                String errstat = LoginHandler.checkName2(playerName = LoginHandler.raiseFirstLetter(playerName));
                if (errstat.length() > 0) {
                    toReturn.put("error", errstat);
                    return toReturn;
                }
                if (PlayerInfoFactory.doesPlayerExist(playerName)) {
                    toReturn.put("error", "The name " + playerName + " is taken. Your reservation must have expired.");
                    return toReturn;
                }
                if (hashedIngamePassword.length() < 6 || hashedIngamePassword.length() > 40) {
                    toReturn.put("error", "The hashed password must contain at least 6 characters and maximum 40 characters.");
                    return toReturn;
                }
                if (challengePhrase.length() < 4 || challengePhrase.length() > 120) {
                    toReturn.put("error", "The challenge phrase must contain at least 4 characters and max 120 characters.");
                    return toReturn;
                }
                if (challengeAnswer.length() < 1 || challengeAnswer.length() > 20) {
                    toReturn.put("error", "The challenge answer must contain at least 1 character and max 20 characters.");
                    return toReturn;
                }
                if (emailAddress.length() > 125) {
                    toReturn.put("error", "The email address consists of too many characters.");
                    return toReturn;
                }
                if (WebInterfaceImpl.isEmailValid(emailAddress)) {
                    try {
                        PendingAccount pacc = PendingAccount.getAccount(playerName);
                        if (pacc == null) {
                            toReturn.put("PlayerId", -1L);
                            toReturn.put("error", "The verification is done too late or the name was never reserved. The name reservation expires after two days. Please try to create the player again.");
                            return toReturn;
                        }
                        if (pacc.password.equals(phaseOneHash)) {
                            if (pacc.emailAddress.toLowerCase().equals(emailAddress.toLowerCase())) {
                                try {
                                    if (serverId == Servers.localServer.id) {
                                        toReturn.put("PlayerId", new Long(LoginHandler.createPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender)));
                                    }
                                    if (Servers.localServer.LOGINSERVER) {
                                        ServerEntry toCreateOn = Servers.getServerWithId(serverId);
                                        if (toCreateOn != null) {
                                            int tilex = toCreateOn.SPAWNPOINTJENNX;
                                            int tiley = toCreateOn.SPAWNPOINTJENNY;
                                            if (kingdom == 2) {
                                                tilex = toCreateOn.SPAWNPOINTMOLX;
                                                tiley = toCreateOn.SPAWNPOINTMOLY;
                                            }
                                            if (kingdom == 3) {
                                                tilex = toCreateOn.SPAWNPOINTLIBX;
                                                tiley = toCreateOn.SPAWNPOINTLIBY;
                                            }
                                            if (serverId == 5) {
                                                tilex = 2884;
                                                tiley = 3004;
                                            }
                                            LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
                                            byte[] playerData = lsw.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, false, false, false);
                                            long wurmId = IntraServerConnection.savePlayerToDisk(playerData, tilex, tiley, true, true);
                                            toReturn.put("PlayerId", wurmId);
                                        }
                                        toReturn.put("PlayerId", -1L);
                                        toReturn.put("error", "Failed to create player " + playerName + ": The desired server does not exist.");
                                    }
                                    toReturn.put("PlayerId", -1L);
                                    toReturn.put("error", "Failed to create player " + playerName + ": This is not a login server.");
                                }
                                catch (Exception cex) {
                                    logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + cex.getMessage(), cex);
                                    toReturn.put("PlayerId", -1L);
                                    toReturn.put("error", "Failed to create player " + playerName + ":" + cex.getMessage());
                                    return toReturn;
                                }
                            } else {
                                toReturn.put("PlayerId", -1L);
                                toReturn.put("error", "The email supplied does not match with the one that was registered with the name.");
                                return toReturn;
                            }
                            pacc.delete();
                            try {
                                if (!Constants.devmode) {
                                    String mail = Mailer.getPhaseTwoMail();
                                    mail = mail.replace("@pname", playerName);
                                    Mailer.sendMail(mailAccount, emailAddress, "Wurm Online character creation success", mail);
                                }
                                break block38;
                            }
                            catch (Exception cex2) {
                                logger.log(Level.WARNING, "Failed to send email to " + emailAddress + " for player " + playerName + ":" + cex2.getMessage(), cex2);
                                toReturn.put("error", "Failed to send email to " + emailAddress + " for player " + playerName + ":" + cex2.getMessage());
                            }
                            break block38;
                        }
                        toReturn.put("PlayerId", -1L);
                        toReturn.put("error", "The verification hash does not match.");
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + ex.getMessage(), ex);
                        toReturn.put("PlayerId", -1L);
                        toReturn.put("error", ex.getMessage());
                    }
                } else {
                    toReturn.put("error", "The email address " + emailAddress + " is not valid.");
                }
            }
        }
        return toReturn;
    }

    @Override
    public byte[] createAndReturnPlayer(String intraServerPassword, String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (Constants.maintaining) {
            throw new RemoteException("The server is currently in maintenance mode.");
        }
        try {
            appearance = Server.rand.nextInt(5);
            this.faceRandom.setSeed(8263186381637L + appearance);
            appearance = this.faceRandom.nextLong();
            logger.log(Level.INFO, WebInterfaceImpl.getClientHost() + " Received create attempt for " + playerName);
            return LoginHandler.createAndReturnPlayer(playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, titleKeeper, addPremium, passwordIsHashed);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        byte executor = 6;
        boolean ok = true;
        String campaignId = "";
        name = LoginHandler.raiseFirstLetter(name);
        HashMap<String, String> toReturn = new HashMap<String, String>();
        if (name == null || name.length() == 0) {
            toReturn.put("error", "Illegal name.");
            return toReturn;
        }
        if (moneyToAdd <= 0L) {
            toReturn.put("error", "Invalid amount; must be greater than zero");
            return toReturn;
        }
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            try {
                Player p = Players.getInstance().getPlayer(name);
                p.addMoney(moneyToAdd);
                long money = p.getMoney();
                new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, executor, campaignId);
                Change change = new Change(moneyToAdd);
                Change current = new Change(money);
                p.save();
                toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + ".");
            }
            catch (NoSuchPlayerException nsp) {
                try {
                    PlayerInfo p = PlayerInfoFactory.createPlayerInfo(name);
                    p.load();
                    if (p.wurmId > 0L) {
                        p.setMoney(p.money + moneyToAdd);
                        Change change = new Change(moneyToAdd);
                        Change current = new Change(p.money);
                        p.save();
                        toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + ". It may take a while to reach your server.");
                        if (Servers.localServer.id != p.currentServer) {
                            new MoneyTransfer(name, p.wurmId, p.money, moneyToAdd, transactionDetail, executor, campaignId, false);
                        } else {
                            new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, executor, campaignId);
                        }
                    } else {
                        toReturn.put("error", "No player found with the name " + name + ".");
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
                throw new RemoteException("An error occured. Please contact customer support.");
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, name + ":" + ex.getMessage(), ex);
                throw new RemoteException("An error occured. Please contact customer support.");
            }
        }
        return toReturn;
    }

    @Override
    public long getMoney(String intraServerPassword, long playerId, String playerName) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
        if (p == null) {
            p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to load pinfo for " + playerName);
            }
            if (p.wurmId <= 0L) {
                return 0L;
            }
        }
        if (p != null) {
            return p.money;
        }
        return 0L;
    }

    @Override
    public Map<String, String> reversePayment(String intraServerPassword, long moneyToRemove, int monthsToRemove, int daysToRemove, String reversalTransactionID, String originalTransactionID, String playerName) throws RemoteException {
        HashMap<String, String> toReturn;
        block13: {
            this.validateIntraServerPassword(intraServerPassword);
            toReturn = new HashMap<String, String>();
            logger.log(Level.INFO, this.getRemoteClientDetails() + " Reverse payment for player name: " + playerName + ", reversalTransactionID: " + reversalTransactionID + ", originalTransactionID: " + originalTransactionID);
            try {
                PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
                p.load();
                if (p.wurmId > 0L) {
                    if (moneyToRemove > 0L) {
                        if (p.money < moneyToRemove) {
                            Change lack = new Change(moneyToRemove - p.money);
                            toReturn.put("moneylack", "An amount of " + lack.getChangeString() + " was lacking from the account. Removing what we can.");
                        }
                        p.setMoney(Math.max(0L, p.money - moneyToRemove));
                        Change change = new Change(moneyToRemove);
                        Change current = new Change(p.money);
                        p.save();
                        toReturn.put("moneyok", "An amount of " + change.getChangeString() + " has been removed from the account. Current balance is " + current.getChangeString() + ".");
                        if (Servers.localServer.id != p.currentServer) {
                            new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, 4, "", false);
                        } else {
                            new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, 4, "");
                        }
                    }
                    if (daysToRemove <= 0 && monthsToRemove <= 0) break block13;
                    long timeToRemove = 0L;
                    if (daysToRemove > 0) {
                        timeToRemove = (long)daysToRemove * 86400000L;
                    }
                    if (monthsToRemove > 0) {
                        timeToRemove += (long)monthsToRemove * 86400000L * 30L;
                    }
                    long currTime = p.getPaymentExpire();
                    currTime = Math.max(currTime, System.currentTimeMillis());
                    currTime = Math.max(currTime - timeToRemove, System.currentTimeMillis());
                    try {
                        p.setPaymentExpire(currTime);
                        String expireString = "The premier playing time has expired now.";
                        if (System.currentTimeMillis() < currTime) {
                            expireString = "The player now has premier playing time until " + GeneralUtilities.toGMTString(currTime) + ". Your in game player account will be updated shortly.";
                        }
                        p.save();
                        toReturn.put("timeok", expireString);
                        if (p.currentServer != Servers.localServer.id) {
                            new TimeTransfer(playerName, p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID, false);
                            break block13;
                        }
                        new TimeTransfer(p.getName(), p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID);
                    }
                    catch (IOException iox) {
                        toReturn.put("timeerror", p.getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage());
                        logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
                    }
                    break block13;
                }
                toReturn.put("error", "No player found with the name " + playerName + ".");
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, playerName + ":" + iox.getMessage(), iox);
                throw new RemoteException("An error occured. Please contact customer support.");
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail, boolean ingame) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " addMoneyToBank for player name: " + name);
        }
        return this.addMoneyToBank(intraServerPassword, name, -1L, moneyToAdd, transactionDetail, ingame);
    }

    public static String encryptMD5(String plaintext) throws Exception {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WurmServerException("No such algorithm 'MD5'", e);
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new WurmServerException("No such encoding: UTF-8", e);
        }
        byte[] raw = md.digest();
        BigInteger bi = new BigInteger(1, raw);
        String hash = bi.toString(16);
        return hash;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long wurmId, long moneyToAdd, String transactionDetail, boolean ingame) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<String, String> toReturn = new HashMap<String, String>();
            if ((name == null || name.length() == 0) && wurmId <= 0L) {
                toReturn.put("error", "Illegal name.");
                return toReturn;
            }
            if (moneyToAdd <= 0L) {
                toReturn.put("error", "Invalid amount; must be greater than zero");
                return toReturn;
            }
            if (name != null) {
                name = LoginHandler.raiseFirstLetter(name);
            }
            int executor = 6;
            String campaignId = "";
            logger.log(Level.INFO, this.getRemoteClientDetails() + " Add money to bank 2 , " + moneyToAdd + " for player name: " + name + ", wid " + wurmId);
            if (name != null && name.length() > 0 || wurmId > 0L) {
                try {
                    Player p = null;
                    p = wurmId <= 0L ? Players.getInstance().getPlayer(name) : Players.getInstance().getPlayer(wurmId);
                    p.addMoney(moneyToAdd);
                    long money = p.getMoney();
                    if (!ingame) {
                        new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, 6, "");
                    }
                    Change change = new Change(moneyToAdd);
                    Change current = new Change(money);
                    p.save();
                    toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + ".");
                }
                catch (NoSuchPlayerException nsp) {
                    try {
                        PlayerInfo p = null;
                        p = name != null && name.length() > 0 ? PlayerInfoFactory.createPlayerInfo(name) : PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
                        if (p != null) {
                            p.load();
                            if (p.wurmId > 0L) {
                                p.setMoney(p.money + moneyToAdd);
                                Change change = new Change(moneyToAdd);
                                Change current = new Change(p.money);
                                p.save();
                                toReturn.put("ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + ". It may take a while to reach your server.");
                                if (!ingame) {
                                    if (Servers.localServer.id != p.currentServer) {
                                        new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, 6, "", false);
                                    } else {
                                        new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, 6, "");
                                    }
                                }
                            } else {
                                toReturn.put("error", "No player found with the wurmid " + p.wurmId + ".");
                            }
                        } else {
                            toReturn.put("error", "No player found with the name " + name + ".");
                        }
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, name + ": " + wurmId + "," + iox.getMessage(), iox);
                        throw new RemoteException("An error occured. Please contact customer support.");
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, name + ":" + wurmId + "," + iox.getMessage(), iox);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, name + ":" + wurmId + "," + ex.getMessage(), ex);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
            }
            return toReturn;
        }
    }

    @Override
    public long chargeMoney(String intraServerPassword, String playerName, long moneyToCharge) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        logger.log(Level.INFO, this.getRemoteClientDetails() + " ChargeMoney for player name: " + playerName + ", money: " + moneyToCharge);
        if (Servers.localServer.id == Servers.loginServer.id) {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                if (p.money > 0L) {
                    if (p.money - moneyToCharge < 0L) {
                        return -10L;
                    }
                    p.setMoney(p.money - moneyToCharge);
                    logger.info(playerName + " was charged " + moneyToCharge + " and now has " + p.money);
                    return p.money;
                }
                return -10L;
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, playerName + ": " + iox.getMessage(), iox);
                return -10L;
            }
        }
        logger.warning(playerName + " cannot charge " + moneyToCharge + " as this server is not the login server");
        return -10L;
    }

    @Override
    public Map<String, String> addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        return this.addPlayingTime(intraServerPassword, name, months, days, transactionDetail, true);
    }

    @Override
    public Map<String, String> addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail, boolean addSleepPowder) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            HashMap<String, String> toReturn = new HashMap<String, String>();
            if (name == null || name.length() == 0 || transactionDetail == null || transactionDetail.length() == 0) {
                toReturn.put("error", "Illegal arguments. Check if name or transaction detail is null or empty strings.");
                return toReturn;
            }
            if (months < 0 || days < 0) {
                toReturn.put("error", "Illegal arguments. Make sure that the values for days and months are not negative.");
                return toReturn;
            }
            boolean ok = true;
            logger.log(Level.INFO, this.getRemoteClientDetails() + " Addplayingtime for player name: " + name + ", months: " + months + ", days: " + days + ", transactionDetail: " + transactionDetail);
            SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
            Object object2 = Server.SYNC_LOCK;
            synchronized (object2) {
                long timeToAdd = 0L;
                if (days != 0) {
                    timeToAdd = (long)days * 86400000L;
                }
                if (months != 0) {
                    timeToAdd += (long)months * 86400000L * 30L;
                }
                try {
                    Player p = Players.getInstance().getPlayer(name);
                    long currTime = p.getPaymentExpire();
                    if (timeToAdd > 0L) {
                        if (currTime <= 0L) {
                            Server.addNewPlayer(p.getName());
                        } else {
                            Server.incrementOldPremiums(p.getName());
                        }
                    }
                    currTime = Math.max(currTime, System.currentTimeMillis());
                    currTime += timeToAdd;
                    try {
                        p.getSaveFile().setPaymentExpire(currTime, !transactionDetail.startsWith("firstBuy"));
                        new TimeTransfer(p.getName(), p.getWurmId(), months, addSleepPowder, days, transactionDetail);
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
                    }
                    String expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ".";
                    p.save();
                    toReturn.put("ok", expireString);
                    Message mess = new Message(null, 3, ":Event", expireString);
                    mess.setReceiver(p.getWurmId());
                    Server.getInstance().addMessage(mess);
                    logger.info(p.getName() + ' ' + expireString);
                    if (addSleepPowder) {
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
                    return toReturn;
                }
                catch (NoSuchPlayerException nsp) {
                    try {
                        PlayerInfo p = PlayerInfoFactory.createPlayerInfo(name);
                        p.load();
                        if (p.wurmId > 0L) {
                            long currTime = p.getPaymentExpire();
                            if (timeToAdd > 0L) {
                                if (currTime <= 0L) {
                                    Server.addNewPlayer(p.getName());
                                } else {
                                    Server.incrementOldPremiums(p.getName());
                                }
                            }
                            currTime = Math.max(currTime, System.currentTimeMillis());
                            currTime += timeToAdd;
                            try {
                                p.setPaymentExpire(currTime, !transactionDetail.startsWith("firstBuy"));
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + iox.getMessage(), iox);
                            }
                            ServerEntry entry = Servers.getServerWithId(p.currentServer);
                            String expireString = "Your premier playing time has expired now.";
                            if (System.currentTimeMillis() < currTime) {
                                expireString = entry.entryServer ? "You now have premier playing time until " + formatter.format(new Date(currTime)) + ". Your in game player account will be updated shortly. NOTE that you will have to use a portal to get to the premium servers in order to benefit from it." : "You now have premier playing time until " + formatter.format(new Date(currTime)) + ". Your in game player account will be updated shortly.";
                            }
                            p.save();
                            toReturn.put("ok", expireString);
                            logger.info(p.getName() + ' ' + expireString);
                            if (p.currentServer != Servers.localServer.id) {
                                new TimeTransfer(name, p.wurmId, months, addSleepPowder, days, transactionDetail, false);
                            } else {
                                new TimeTransfer(p.getName(), p.wurmId, months, addSleepPowder, days, transactionDetail);
                                if (addSleepPowder) {
                                    try {
                                        long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);
                                        for (int x = 0; x < months; ++x) {
                                            Item i = ItemFactory.createItem(666, 99.0f, "");
                                            i.setParentId(inventoryId, true);
                                            i.setOwnerId(p.wurmId);
                                        }
                                        logger.log(Level.INFO, "Inserted " + months + " sleep powder in offline " + p.getName() + " inventory " + inventoryId);
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.INFO, ex.getMessage(), ex);
                                    }
                                }
                            }
                            return toReturn;
                        }
                        toReturn.put("error", "No player found with the name " + name + ".");
                        return toReturn;
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
                        throw new RemoteException("An error occured. Please contact customer support.");
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, name + ":" + iox.getMessage(), iox);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, name + ":" + ex.getMessage(), ex);
                    throw new RemoteException("An error occured. Please contact customer support.");
                }
            }
        }
    }

    @Override
    public Map<Integer, String> getDeeds(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getDeeds");
        }
        HashMap<Integer, String> toReturn = new HashMap<Integer, String>();
        Village[] vills = Villages.getVillages();
        for (int x = 0; x < vills.length; ++x) {
            toReturn.put(vills[x].id, vills[x].getName());
        }
        return toReturn;
    }

    @Override
    public Map<String, ?> getDeedSummary(String intraServerPassword, int aVillageID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getDeedSummary for villageID: " + aVillageID);
        }
        try {
            Village village = Villages.getVillage(aVillageID);
            HashMap<String, Object> toReturn = new HashMap<String, Object>();
            toReturn.put("Villageid", village.getId());
            toReturn.put("Deedid", village.getDeedId());
            toReturn.put("Name", village.getName());
            toReturn.put("Motto", village.getMotto());
            toReturn.put("Location", Kingdoms.getNameFor(village.kingdom));
            toReturn.put("Size", (village.getEndX() - village.getStartX()) / 2);
            toReturn.put("Founder", village.getFounderName());
            toReturn.put("Mayor", village.mayorName);
            if (village.disband > 0L) {
                toReturn.put("Disbanding in", Server.getTimeFor(village.disband - System.currentTimeMillis()));
                toReturn.put("Disbander", Players.getInstance().getNameFor(village.disbander));
            }
            toReturn.put("Citizens", village.citizens.size());
            toReturn.put("Allies", village.getAllies().length);
            if (village.guards != null) {
                toReturn.put("guards", village.guards.size());
            }
            try {
                short[] sp = village.getTokenCoords();
                toReturn.put("Token Coord x", Integer.valueOf(sp[0]));
                toReturn.put("Token Coord y", Integer.valueOf(sp[1]));
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
            return toReturn;
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public Map<String, Long> getPlayersForDeed(String intraServerPassword, int aVillageID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayersForDeed for villageID: " + aVillageID);
        }
        HashMap<String, Long> toReturn = new HashMap<String, Long>();
        try {
            Village village = Villages.getVillage(aVillageID);
            Citizen[] citizens = village.getCitizens();
            for (int x = 0; x < citizens.length; ++x) {
                if (WurmId.getType(citizens[x].getId()) != 0) continue;
                try {
                    toReturn.put(Players.getInstance().getNameFor(citizens[x].getId()), new Long(citizens[x].getId()));
                    continue;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
        return toReturn;
    }

    @Override
    public Map<String, Integer> getAlliesForDeed(String intraServerPassword, int aVillageID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getAlliesForDeed for villageID: " + aVillageID);
        }
        HashMap<String, Integer> toReturn = new HashMap<String, Integer>();
        try {
            Village village = Villages.getVillage(aVillageID);
            Village[] allies = village.getAllies();
            for (int x = 0; x < allies.length; ++x) {
                toReturn.put(allies[x].getName(), allies[x].getId());
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
        return toReturn;
    }

    @Override
    public String[] getHistoryForDeed(String intraServerPassword, int villageID, int maxLength) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getHistoryForDeed for villageID: " + villageID + ", maxLength: " + maxLength);
        }
        try {
            Village village = Villages.getVillage(villageID);
            return village.getHistoryAsStrings(maxLength);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public String[] getAreaHistory(String intraServerPassword, int maxLength) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getAreaHistory maxLength: " + maxLength);
        }
        return HistoryManager.getHistory(maxLength);
    }

    @Override
    public Map<String, ?> getItemSummary(String intraServerPassword, long aWurmID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getItemSummary for WurmId: " + aWurmID);
        }
        HashMap<String, Object> toReturn = new HashMap<String, Object>();
        try {
            Item item = Items.getItem(aWurmID);
            toReturn.put("WurmId", new Long(aWurmID));
            toReturn.put("Name", item.getName());
            toReturn.put("QL", String.valueOf(item.getQualityLevel()));
            toReturn.put("DMG", String.valueOf(item.getDamage()));
            toReturn.put("SizeX", String.valueOf(item.getSizeX()));
            toReturn.put("SizeY", String.valueOf(item.getSizeY()));
            toReturn.put("SizeZ", String.valueOf(item.getSizeZ()));
            if (item.getOwnerId() != -10L) {
                toReturn.put("Owner", new Long(item.getOwnerId()));
            } else {
                toReturn.put("Last owner", new Long(item.lastOwner));
            }
            toReturn.put("Coord x", (int)item.getPosX() >> 2);
            toReturn.put("Coord y", (int)item.getPosY() >> 2);
            toReturn.put("Creator", item.creator);
            toReturn.put("Creationdate", WurmCalendar.getTimeFor(item.creationDate));
            toReturn.put("Description", item.getDescription());
            toReturn.put("Material", Item.getMaterialString(item.getMaterial()));
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            throw new RemoteException(ex.getMessage());
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getPlayerIPAddresses(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayerIPAddresses");
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        Player[] playerArr = Players.getInstance().getPlayersByIp();
        for (int x = 0; x < playerArr.length; ++x) {
            if (playerArr[x].getSaveFile().getPower() != 0) continue;
            toReturn.put(playerArr[x].getName(), playerArr[x].getSaveFile().getIpaddress());
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getNameBans(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getNameBans");
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        Ban[] bips = Players.getInstance().getPlayersBanned();
        if (bips.length > 0) {
            for (int x = 0; x < bips.length; ++x) {
                long daytime = bips[x].getExpiry() - System.currentTimeMillis();
                toReturn.put(bips[x].getIdentifier(), Server.getTimeFor(daytime) + ", " + bips[x].getReason());
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getIPBans(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getIPBans");
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        Ban[] bips = Players.getInstance().getBans();
        if (bips.length > 0) {
            for (int x = 0; x < bips.length; ++x) {
                long daytime = bips[x].getExpiry() - System.currentTimeMillis();
                toReturn.put(bips[x].getIdentifier(), Server.getTimeFor(daytime) + ", " + bips[x].getReason());
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, String> getWarnings(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getWarnings");
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Not implemented", "Need a name to check.");
        return toReturn;
    }

    @Override
    public String getWurmTime(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getWurmTime");
        }
        return WurmCalendar.getTime();
    }

    @Override
    public String getUptime(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getUptime");
        }
        return Server.getTimeFor(System.currentTimeMillis() - Server.getStartTime());
    }

    @Override
    public String getNews(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getNews");
        }
        return NewsInfo.getInfo();
    }

    @Override
    public String getGameInfo(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getGameInfo");
        }
        return WurmInfo.getInfo() + WurmInfo2.getInfo();
    }

    @Override
    public Map<String, String> getKingdomInfluence(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getKingdomInfluence");
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        Zones.calculateZones(false);
        Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        for (int x = 0; x < kingdoms.length; ++x) {
            toReturn.put("Percent controlled by " + kingdoms[x].getName(), twoDecimals.format(Zones.getPercentLandForKingdom(kingdoms[x].getId())));
        }
        return toReturn;
    }

    @Override
    public Map<String, ?> getMerchantSummary(String intraServerPassword, long aWurmID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getMerchantSummary for WurmID: " + aWurmID);
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Not implemented", "not yet");
        return toReturn;
    }

    @Override
    public Map<String, ?> getBankAccount(String intraServerPassword, long aPlayerID) throws RemoteException {
        HashMap<String, Object> toReturn;
        block12: {
            this.validateIntraServerPassword(intraServerPassword);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(this.getRemoteClientDetails() + " getBankAccount for playerid: " + aPlayerID);
            }
            toReturn = new HashMap<String, Object>();
            logger.log(Level.INFO, "GetBankAccount " + aPlayerID);
            try {
                Bank lBank = Banks.getBank(aPlayerID);
                if (lBank != null) {
                    BankSlot[] lSlots;
                    toReturn.put("BankID", lBank.id);
                    toReturn.put("Owner", lBank.owner);
                    toReturn.put("StartedMoving", lBank.startedMoving);
                    toReturn.put("Open", lBank.open);
                    toReturn.put("Size", lBank.size);
                    try {
                        Village lCurrentVillage = lBank.getCurrentVillage();
                        if (lCurrentVillage != null) {
                            toReturn.put("CurrentVillageID", lCurrentVillage.getId());
                            toReturn.put("CurrentVillageName", lCurrentVillage.getName());
                        }
                    }
                    catch (BankUnavailableException lCurrentVillage) {
                        // empty catch block
                    }
                    int lTargetVillageID = lBank.targetVillage;
                    if (lTargetVillageID > 0) {
                        toReturn.put("TargetVillageID", lTargetVillageID);
                    }
                    if ((lSlots = lBank.slots) != null && lSlots.length > 0) {
                        HashMap<Long, String> lItemsMap = new HashMap<Long, String>(lSlots.length + 1);
                        for (int i = 0; i < lSlots.length; ++i) {
                            if (lSlots[i] == null) {
                                logger.log(Level.INFO, "Weird. Bank Slot " + i + " is null for " + aPlayerID);
                                continue;
                            }
                            Item lItem = lSlots[i].item;
                            if (lItem == null) continue;
                            lItemsMap.put(lItem.getWurmId(), lItem.getName() + ", Inserted: " + lSlots[i].inserted + ", Stasis: " + lSlots[i].stasis);
                        }
                        if (lItemsMap != null && lItemsMap.size() > 0) {
                            toReturn.put("Items", lItemsMap);
                        }
                    }
                    break block12;
                }
                toReturn.put("Error", "Cannot find bank for player ID " + aPlayerID);
            }
            catch (RuntimeException e) {
                logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
                toReturn.put("Error", "Problem getting bank account for player ID " + aPlayerID + ", " + e);
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, ?> authenticateUser(String intraServerPassword, String playerName, String emailAddress, String hashedIngamePassword, Map params) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
        }
        Map<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "The server is currently unavailable.");
            toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
            return toReturn;
        }
        try {
            String ipAddress;
            boolean ver = false;
            Object answer = params.get("VerifiedPayPalAccount");
            if (answer != null && answer instanceof Boolean) {
                ver = (Boolean)answer;
            }
            boolean rev = false;
            answer = params.get("ChargebackOrReversal");
            if (answer != null && answer instanceof Boolean) {
                rev = (Boolean)answer;
            }
            Date lastReversal = (Date)params.get("LastChargebackOrReversal");
            Date first = (Date)params.get("FirstTransactionDate");
            Date last = (Date)params.get("LastTransactionDate");
            int total = 0;
            answer = params.get("TotalEurosSuccessful");
            if (answer != null && answer instanceof Integer && (total = ((Integer)answer).intValue()) < 0) {
                total = 0;
            }
            int lastMonthEuros = 0;
            answer = params.get("LastMonthEurosSuccessful");
            if (answer != null && answer instanceof Integer && (lastMonthEuros = ((Integer)answer).intValue()) < 0) {
                lastMonthEuros = 0;
            }
            if ((ipAddress = (String)params.get("IP")) != null) {
                logger.log(Level.INFO, "IP:" + ipAddress);
                Long lastAttempt = ipAttempts.get(ipAddress);
                if (lastAttempt != null && System.currentTimeMillis() - lastAttempt < 5000L) {
                    toReturn.put((String)((Object)"ResponseCode0"), (String)((Object)"NOTOK"));
                    toReturn.put((String)((Object)"ErrorMessage0"), (String)((Object)"Too many logon attempts. Please try again in a few seconds."));
                    toReturn.put((String)((Object)"display_text0"), (String)((Object)"Too many logon attempts. Please try again in a few seconds."));
                    return toReturn;
                }
                ipAttempts.put(ipAddress, System.currentTimeMillis());
            }
            PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            if (file.undeadType != 0) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                toReturn.put("display_text0", "Undeads not allowed in here!");
                return toReturn;
            }
            try {
                file.load();
                if (file.undeadType != 0) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                    toReturn.put("display_text0", "Undeads not allowed in here!");
                    return toReturn;
                }
            }
            catch (IOException iox) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "An error occurred when loading your account.");
                toReturn.put("display_text0", "An error occurred when loading your account.");
                logger.log(Level.WARNING, iox.getMessage(), iox);
                return toReturn;
            }
            if (!file.overRideShop && rev && (lastReversal == null || last == null || lastReversal.after(last))) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put((String)((Object)"ErrorMessage0"), (String)((Object)"This paypal account has reversed transactions registered."));
                toReturn.put((String)((Object)"display_text0"), (String)((Object)"This paypal account has reversed transactions registered."));
                return toReturn;
            }
            toReturn = this.authenticateUser(intraServerPassword, playerName, emailAddress, hashedIngamePassword);
            Integer max = (Integer)toReturn.get("maximum_silver0");
            if (max != null) {
                int maxval = max;
                if (file.overRideShop) {
                    maxval = 50 + Math.min(50, (int)(file.playingTime / 3600000L * 3L));
                    toReturn.put((String)((Object)"maximum_silver0"), (String)((Object)Integer.valueOf(maxval)));
                } else if (lastMonthEuros >= 400) {
                    maxval = 0;
                    toReturn.put((String)((Object)"maximum_silver0"), (String)((Object)Integer.valueOf(maxval)));
                    toReturn.put((String)((Object)"display_text0"), (String)((Object)"You may only purchase 400 silver via PayPal per month"));
                }
            }
            return toReturn;
        }
        catch (Exception ew) {
            logger.log(Level.WARNING, "Error: " + ew.getMessage(), ew);
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "An error occured.");
            return toReturn;
        }
    }

    @Override
    public Map<String, String> doesPlayerExist(String intraServerPassword, String playerName) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " doesPlayerExist for player name: " + playerName);
        }
        HashMap<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("ResponseCode", "NOTOK");
            toReturn.put("ErrorMessage", "The server is currently unavailable.");
            toReturn.put("display_text", "The server is currently unavailable.");
            return toReturn;
        }
        toReturn.put("ResponseCode", "OK");
        if (playerName != null) {
            PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                file.load();
                if (file.wurmId <= 0L) {
                    toReturn.clear();
                    toReturn.put("ResponseCode", "NOTOK");
                    toReturn.put("ErrorMessage", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity.");
                    toReturn.put("display_text", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity.");
                }
            }
            catch (Exception ex) {
                toReturn.clear();
                toReturn.put("ResponseCode", "NOTOK");
                toReturn.put("ErrorMessage", ex.getMessage());
                toReturn.put("display_text", "An error occurred on the " + Servers.localServer.name + " game server: " + ex.getMessage());
            }
        }
        return toReturn;
    }

    @Override
    public Map<String, ?> authenticateUser(String intraServerPassword, String playerName, String emailAddress, String hashedIngamePassword) throws RemoteException {
        HashMap<String, Object> toReturn;
        block31: {
            this.validateIntraServerPassword(intraServerPassword);
            if (logger.isLoggable(Level.FINER)) {
                logger.finer(this.getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
            }
            toReturn = new HashMap<String, Object>();
            if (Constants.maintaining) {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "The server is currently unavailable.");
                toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
                return toReturn;
            }
            if (playerName != null) {
                PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
                if (file.undeadType != 0) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                    toReturn.put("display_text0", "Undeads not allowed in here!");
                    return toReturn;
                }
                try {
                    file.load();
                    if (file.undeadType != 0) {
                        toReturn.put("ResponseCode0", "NOTOK");
                        toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                        toReturn.put("display_text0", "Undeads not allowed in here!");
                        return toReturn;
                    }
                    if (file.wurmId <= 0L) {
                        toReturn.put("ResponseCode0", "NOTOK");
                        toReturn.put("ErrorMessage0", "No such player.");
                        break block31;
                    }
                    if (hashedIngamePassword.equals(file.getPassword())) {
                        LoginServerWebConnection lsw;
                        Map<String, String> m;
                        String resp;
                        if (Servers.isThisLoginServer() && (resp = (m = (lsw = new LoginServerWebConnection(file.currentServer)).doesPlayerExist(playerName)).get("ResponseCode")) != null && resp.equals("NOTOK")) {
                            toReturn.put("ResponseCode0", "NOTOK");
                            toReturn.put("ErrorMessage0", m.get("ErrorMessage"));
                            toReturn.put("display_text0", m.get("display_text"));
                            return toReturn;
                        }
                        toReturn.put("ErrorMessage0", "");
                        if (file.getPaymentExpire() < 0L) {
                            toReturn.put("display_text0", "You are new to the game and may give away an in-game referral to the person who introduced you to Wurm Online using the chat command '/refer' if you purchase premium game time.");
                        } else {
                            toReturn.put("display_text0", "Don't forget to use the in-game '/refer' chat command to refer the one who introduced you to Wurm Online.");
                        }
                        if (file.getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
                            toReturn.put("display_text0", "You have less than a week left of premium game time so the amount of coins you can purchase is somewhat limited.");
                            toReturn.put("maximum_silver0", 10);
                        } else {
                            toReturn.put("maximum_silver0", 20 + Math.min(100, (int)(file.playingTime / 3600000L * 3L)));
                        }
                        if (!file.overRideShop && file.isBanned()) {
                            toReturn.put("PurchaseOk0", "NOTOK");
                            toReturn.put("maximum_silver0", 0);
                            toReturn.put("display_text0", "You have been banned. Reason: " + file.banreason);
                            toReturn.put("ErrorMessage0", "The player has been banned. Reason: " + file.banreason);
                        } else {
                            toReturn.put("PurchaseOk0", "OK");
                        }
                        int maxMonths = 0;
                        if (file.getPaymentExpire() > System.currentTimeMillis()) {
                            long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - file.getPaymentExpire();
                            maxMonths = (int)(maxMonthsMillis / 2419200000L);
                            if (maxMonths < 0) {
                                maxMonths = 0;
                            }
                        } else {
                            maxMonths = 12;
                        }
                        toReturn.put("maximum_months0", maxMonths);
                        toReturn.put("new_customer0", file.getPaymentExpire() <= 0L);
                        toReturn.put("ResponseCode0", "OK");
                        toReturn.put("PlayerID0", new Long(file.wurmId));
                        toReturn.put("ingameBankBalance0", new Long(file.money));
                        toReturn.put("PlayingTimeExpire0", new Long(file.getPaymentExpire()));
                        break block31;
                    }
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", "Password does not match.");
                }
                catch (Exception ex) {
                    toReturn.put("ResponseCode0", "NOTOK");
                    toReturn.put("ErrorMessage0", ex.getMessage());
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
            } else if (WebInterfaceImpl.isEmailValid(emailAddress)) {
                PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                for (int x = 0; x < infos.length; ++x) {
                    if (infos[x].getPassword().equals(hashedIngamePassword)) {
                        toReturn.put("ErrorMessage" + x, "");
                        if (infos[x].getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
                            toReturn.put("maximum_silver" + x, 10);
                        } else {
                            toReturn.put("maximum_silver" + x, 10 + Math.min(100, (int)(infos[x].playingTime / 86400000L)));
                        }
                        if (!infos[x].overRideShop && infos[x].isBanned()) {
                            toReturn.put("PurchaseOk" + x, "NOTOK");
                            toReturn.put("maximum_silver" + x, 0);
                            toReturn.put("display_text" + x, "You have been banned. Reason: " + infos[x].banreason);
                            toReturn.put("ErrorMessage" + x, "The player has been banned. Reason: " + infos[x].banreason);
                        } else {
                            toReturn.put("PurchaseOk" + x, "OK");
                        }
                        int maxMonths = 0;
                        if (infos[x].getPaymentExpire() > System.currentTimeMillis()) {
                            long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - infos[x].getPaymentExpire();
                            maxMonths = (int)(maxMonthsMillis / 2419200000L);
                            if (maxMonths < 0) {
                                maxMonths = 0;
                            }
                        } else {
                            maxMonths = 12;
                        }
                        toReturn.put("maximum_months" + x, maxMonths);
                        toReturn.put("new_customer" + x, infos[x].getPaymentExpire() <= 0L);
                        toReturn.put("ResponseCode" + x, "OK");
                        toReturn.put("PlayerID" + x, new Long(infos[x].wurmId));
                        toReturn.put("ingameBankBalance" + x, new Long(infos[x].money));
                        toReturn.put("PlayingTimeExpire" + x, new Long(infos[x].getPaymentExpire()));
                        continue;
                    }
                    toReturn.put("ResponseCode" + x, "NOTOK");
                    toReturn.put("ErrorMessage" + x, "Password does not match.");
                }
            } else {
                toReturn.put("ResponseCode0", "NOTOK");
                toReturn.put("ErrorMessage0", "Invalid email: " + emailAddress);
            }
        }
        return toReturn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Map<String, String> changePassword(String intraServerPassword, String playerName, String emailAddress, String newPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        HashMap<String, String> toReturn = new HashMap<String, String>();
        try {
            block28: {
                toReturn.put("Result", "Unknown email.");
                logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword Name: " + playerName + ", email: " + emailAddress);
                if (emailAddress != null) {
                    if (!WebInterfaceImpl.isEmailValid(emailAddress)) {
                        toReturn.put("Error", emailAddress + " is an invalid email.");
                        break block28;
                    } else {
                        PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                        int nums = 0;
                        for (int x = 0; x < infos.length; ++x) {
                            if (infos[x].getPower() == 0) {
                                try {
                                    infos[x].updatePassword(newPassword);
                                    if (infos[x].currentServer != Servers.localServer.id) {
                                        new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                                    }
                                    toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                                }
                                catch (IOException iox) {
                                    logger.log(Level.WARNING, "Failed to update password for " + infos[x].getName(), iox);
                                    toReturn.put("Error" + nums, infos[x].getName() + " password was _not_ updated.");
                                }
                                continue;
                            }
                            toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
                            logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x].getPower());
                        }
                        if (nums > 0) {
                            toReturn.put("Result", nums + " player accounts were affected.");
                        } else {
                            toReturn.put("Error", nums + " player accounts were affected.");
                        }
                        HashMap<String, String> x = toReturn;
                        return x;
                    }
                }
                if (playerName != null) {
                    PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
                    try {
                        p.load();
                        if (WebInterfaceImpl.isEmailValid(p.emailAddress)) {
                            emailAddress = p.emailAddress;
                            PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                            int nums = 0;
                            boolean failed = false;
                            for (int x = 0; x < infos.length; ++x) {
                                if (infos[x].getPower() == 0) {
                                    try {
                                        infos[x].updatePassword(newPassword);
                                        if (infos[x].currentServer != Servers.localServer.id) {
                                            new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                                        }
                                        toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                                    }
                                    catch (IOException iox) {
                                        failed = true;
                                        toReturn.put("Error" + nums, "Failed to update password for a player.");
                                    }
                                    continue;
                                }
                                failed = true;
                                logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x].getPower());
                            }
                            if (nums > 0) {
                                toReturn.put("Result", nums + " player accounts were affected.");
                            } else {
                                toReturn.put("Error", nums + " player accounts were affected.");
                            }
                            if (failed) {
                                logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                            }
                            HashMap<String, String> hashMap = toReturn;
                            return hashMap;
                        }
                    }
                    catch (IOException iox) {
                        toReturn.put("Error", "Failed to load player data. Password not changed.");
                        logger.log(Level.WARNING, iox.getMessage(), iox);
                        break block28;
                    }
                    {
                        toReturn.put("Error", emailAddress + " is an invalid email.");
                    }
                }
            }
            HashMap<String, String> hashMap = toReturn;
            return hashMap;
        }
        finally {
            logger.info("Changepassword Name: " + playerName + ", email: " + emailAddress + ", exit: " + toReturn);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Map<String, String> changePassword(String intraServerPassword, String playerName, String emailAddress, String hashedOldPassword, String newPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Result", "Unknown email.");
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword 2 for player name: " + playerName);
        if (emailAddress != null) {
            if (!WebInterfaceImpl.isEmailValid(emailAddress)) {
                toReturn.put("Result", emailAddress + " is an invalid email.");
                return toReturn;
            }
            PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
            boolean ok = false;
            int nums = 0;
            for (int x = 0; x < infos.length; ++x) {
                if (!infos[x].getPassword().equals(hashedOldPassword)) continue;
                ok = true;
            }
            if (ok) {
                boolean failed = false;
                for (int x = 0; x < infos.length; ++x) {
                    if (infos[x].getPower() == 0) {
                        try {
                            infos[x].updatePassword(newPassword);
                            if (infos[x].currentServer != Servers.localServer.id) {
                                new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                            }
                            toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                        }
                        catch (IOException iox) {
                            failed = true;
                            toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
                        }
                        continue;
                    }
                    failed = true;
                    toReturn.put("Error" + nums, infos[x].getName() + " password was _not_ updated.");
                }
                if (failed) {
                    logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                }
            }
            if (nums > 0) {
                toReturn.put("Result", nums + " player accounts were affected.");
                return toReturn;
            } else {
                toReturn.put("Error", nums + " player accounts were affected.");
            }
            return toReturn;
        }
        if (playerName == null) return toReturn;
        PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
        try {
            p.load();
            boolean ok = false;
            if (WebInterfaceImpl.isEmailValid(p.emailAddress)) {
                emailAddress = p.emailAddress;
                PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                for (int x = 0; x < infos.length; ++x) {
                    if (!infos[x].getPassword().equals(hashedOldPassword)) continue;
                    ok = true;
                }
                int nums = 0;
                if (ok) {
                    boolean failed = false;
                    for (int x = 0; x < infos.length; ++x) {
                        if (infos[x].getPower() == 0) {
                            try {
                                infos[x].updatePassword(newPassword);
                                if (infos[x].currentServer != Servers.localServer.id) {
                                    new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                                }
                                toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                            }
                            catch (IOException iox) {
                                failed = true;
                                toReturn.put("Error" + x, "Failed to update password for " + infos[x].getName());
                            }
                            continue;
                        }
                        failed = true;
                    }
                    if (failed) {
                        logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                    }
                }
                if (nums > 0) {
                    toReturn.put("Result", nums + " player accounts were affected.");
                    return toReturn;
                } else {
                    toReturn.put("Error", nums + " player accounts were affected.");
                }
                return toReturn;
            }
            toReturn.put("Error", emailAddress + " is an invalid email.");
            return toReturn;
        }
        catch (IOException iox) {
            toReturn.put("Error", "Failed to load player data. Password not changed.");
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        return toReturn;
    }

    @Override
    public Map<String, String> changeEmail(String intraServerPassword, String playerName, String oldEmailAddress, String newEmailAddress) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        HashMap<String, String> toReturn = new HashMap<String, String>();
        toReturn.put("Result", "Unknown email.");
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Change Email for player name: " + playerName);
        if (Constants.maintaining) {
            toReturn.put("Error", "The server is currently unavailable.");
            toReturn.put("Result", "The server is in maintenance mode. Please try later.");
            return toReturn;
        }
        if (oldEmailAddress != null) {
            if (!WebInterfaceImpl.isEmailValid(oldEmailAddress)) {
                toReturn.put("Error", "The old email address, " + oldEmailAddress + " is an invalid email.");
            } else if (!WebInterfaceImpl.isEmailValid(newEmailAddress)) {
                toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
            } else {
                PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
                int nums = 0;
                for (int x = 0; x < infos.length; ++x) {
                    if (infos[x].getPower() == 0) {
                        infos[x].setEmailAddress(newEmailAddress);
                        toReturn.put("Account" + ++nums, infos[x].getName() + " account was affected.");
                        continue;
                    }
                    toReturn.put("Account" + nums, infos[x].getName() + " account was _not_ affected.");
                }
                if (nums > 0) {
                    toReturn.put("Result", nums + " player accounts were affected.");
                } else {
                    toReturn.put("Error", nums + " player accounts were affected.");
                }
            }
            return toReturn;
        }
        if (playerName != null) {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
            try {
                p.load();
                if (WebInterfaceImpl.isEmailValid(newEmailAddress)) {
                    oldEmailAddress = p.emailAddress;
                    PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
                    int nums = 0;
                    for (int x = 0; x < infos.length; ++x) {
                        if (infos[x].getPower() == 0) {
                            infos[x].setEmailAddress(newEmailAddress);
                            toReturn.put("Account" + ++nums, infos[x].getName() + " account was affected.");
                            continue;
                        }
                        toReturn.put("Account" + nums, infos[x].getName() + " account was _not_ affected.");
                    }
                    if (nums > 0) {
                        toReturn.put("Result", nums + " player accounts were affected.");
                    } else {
                        toReturn.put("Error", nums + " player accounts were affected.");
                    }
                    return toReturn;
                }
                toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
            }
            catch (IOException iox) {
                toReturn.put("Error", "Failed to load player data. Email not changed.");
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
        return toReturn;
    }

    @Override
    public String getChallengePhrase(String intraServerPassword, String playerName) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (playerName.contains("@")) {
            PlayerInfo[] pinfos = PlayerInfoFactory.getPlayerInfosForEmail(playerName);
            if (pinfos.length > 0) {
                return pinfos[0].pwQuestion;
            }
            return "Incorrect email.";
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getChallengePhrase for player name: " + playerName);
        }
        PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
        try {
            p.load();
            return p.pwQuestion;
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
            return "Error";
        }
    }

    @Override
    public String[] getPlayerNamesForEmail(String intraServerPassword, String emailAddress) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getPlayerNamesForEmail: " + emailAddress);
        }
        String[] nameArray = PlayerInfoFactory.getAccountsForEmail(emailAddress);
        return nameArray;
    }

    @Override
    public String getEmailAddress(String intraServerPassword, String playerName) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getEmailAddress for player name: " + playerName);
        }
        PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
        try {
            p.load();
            return p.emailAddress;
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
            return "Error";
        }
    }

    public static String generateRandomPassword() {
        Random rand = new Random();
        int length = rand.nextInt(3) + 6;
        char[] password = new char[length];
        for (int x = 0; x < length; ++x) {
            int randDecimalAsciiVal = rand.nextInt(PASSWORD_CHARS.length());
            password[x] = PASSWORD_CHARS.charAt(randDecimalAsciiVal);
        }
        return String.valueOf(password);
    }

    public static final boolean isEmailValid(String emailAddress) {
        if (emailAddress == null) {
            return false;
        }
        Matcher m = VALID_EMAIL_PATTERN.matcher(emailAddress);
        return m.matches();
    }

    @Override
    public Map<String, String> requestPasswordReset(String intraServerPassword, String email, String challengePhraseAnswer) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        HashMap<String, String> toReturn = new HashMap<String, String>();
        if (Constants.maintaining) {
            toReturn.put("Error0", "The server is currently in maintenance mode.");
            return toReturn;
        }
        boolean ok = false;
        String password = WebInterfaceImpl.generateRandomPassword();
        String playernames = "";
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Password reset for email/name: " + email);
        if (challengePhraseAnswer == null || challengePhraseAnswer.length() < 1) {
            toReturn.put("Error0", "The answer is too short.");
            return toReturn;
        }
        if (!email.contains("@")) {
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(email);
            if (!pinf.loaded) {
                try {
                    pinf.load();
                    logger.log(Level.INFO, email + " " + challengePhraseAnswer + " compares to " + pinf.pwAnswer);
                    if (System.currentTimeMillis() - pinf.lastRequestedPassword > 60000L) {
                        logger.log(Level.INFO, email + " time ok. comparing.");
                        if (pinf.pwAnswer.equalsIgnoreCase(challengePhraseAnswer)) {
                            logger.log(Level.INFO, email + " challenge answer correct.");
                            ok = true;
                            playernames = pinf.getName();
                            pinf.updatePassword(password);
                            if (pinf.currentServer != Servers.localServer.id) {
                                new PasswordTransfer(pinf.getName(), pinf.wurmId, pinf.getPassword(), System.currentTimeMillis(), false);
                            }
                        }
                    } else {
                        toReturn.put("Error", "Please try again in a minute.");
                        return toReturn;
                    }
                    pinf.lastRequestedPassword = System.currentTimeMillis();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, email + ":" + iox.getMessage(), iox);
                    toReturn.put("Error", "An error occured. Please try later.");
                    return toReturn;
                }
            }
        } else {
            PlayerInfo[] p = PlayerInfoFactory.getPlayerInfosWithEmail(email);
            for (int x = 0; x < p.length; ++x) {
                try {
                    p[x].load();
                    if (p[x].pwAnswer.toLowerCase().equals(challengePhraseAnswer.toLowerCase()) || p[x].pwAnswer.length() == 0 && p[x].pwQuestion.length() == 0) {
                        if (System.currentTimeMillis() - p[x].lastRequestedPassword > 60000L) {
                            ok = true;
                            playernames = playernames.length() > 0 ? playernames + ", " + p[x].getName() : p[x].getName();
                            p[x].updatePassword(password);
                            if (p[x].currentServer != Servers.localServer.id) {
                                new PasswordTransfer(p[x].getName(), p[x].wurmId, p[x].getPassword(), System.currentTimeMillis(), false);
                            }
                        } else if (!ok) {
                            toReturn.put("Error", "Please try again in a minute.");
                            return toReturn;
                        }
                    }
                    p[x].lastRequestedPassword = System.currentTimeMillis();
                    continue;
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, email + ":" + iox.getMessage(), iox);
                    toReturn.put("Error", "An error occured. Please try later.");
                    return toReturn;
                }
            }
        }
        if (ok) {
            toReturn.put("Result", "Password was changed.");
        } else {
            toReturn.put("Error", "Password was not changed.");
        }
        if (playernames.length() > 0) {
            try {
                String mail = Mailer.getPasswordMail();
                mail = mail.replace("@pname", playernames);
                mail = mail.replace("@password", password);
                Mailer.sendMail(mailAccount, email, "Wurm Online password request", mail);
                toReturn.put("MailResult", "A mail was sent to the mail adress: " + email + " for " + playernames + ".");
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, email + ":" + ex.getMessage(), ex);
                toReturn.put("MailError", "An error occured - " + ex.getMessage() + ". Please try later.");
            }
        } else {
            toReturn.put("Error", "Wrong answer.");
            return toReturn;
        }
        return toReturn;
    }

    @Override
    public Map<Integer, String> getAllServers(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        return this.getAllServerInternalAddresses(intraServerPassword);
    }

    @Override
    public Map<Integer, String> getAllServerInternalAddresses(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        HashMap<Integer, String> toReturn = new HashMap<Integer, String>();
        ServerEntry[] entries = Servers.getAllServers();
        for (int x = 0; x < entries.length; ++x) {
            toReturn.put(entries[x].id, entries[x].INTRASERVERADDRESS);
        }
        return toReturn;
    }

    @Override
    public boolean sendMail(String intraServerPassword, String sender, String receiver, String subject, String text) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (!WebInterfaceImpl.isEmailValid(sender)) {
            return false;
        }
        if (!WebInterfaceImpl.isEmailValid(receiver)) {
            return false;
        }
        try {
            Mailer.sendMail(sender, receiver, subject, text);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    @Override
    public void shutDown(String intraServerPassword, String playerName, String password, String reason, int seconds) throws RemoteException {
        block9: {
            this.validateIntraServerPassword(intraServerPassword);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(this.getRemoteClientDetails() + " shutDown by player name: " + playerName);
            }
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(playerName));
            try {
                pinf.load();
                if (pinf.getPower() >= 4) {
                    try {
                        String pw = LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(pinf.getName())));
                        if (pw.equals(pinf.getPassword())) {
                            logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + " initiated shutdown in " + seconds + " seconds: " + reason);
                            if (seconds <= 0) {
                                Server.getInstance().shutDown();
                            } else {
                                Server.getInstance().startShutdown(seconds, reason);
                            }
                            break block9;
                        }
                        logger.log(Level.WARNING, this.getRemoteClientDetails() + " player: " + playerName + " denied shutdown due to wrong password.");
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, "Failed to encrypt password for player " + playerName, ex);
                    }
                    break block9;
                }
                logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + " DENIED shutdown in " + seconds + " seconds: " + reason);
            }
            catch (IOException iox) {
                logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + ": " + iox.getMessage(), iox);
            }
        }
    }

    @Override
    public Map<String, Byte> getReferrers(String intraServerPassword, long wurmid) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getReferrers for WurmID: " + wurmid);
        }
        return PlayerInfoFactory.getReferrers(wurmid);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String addReferrer(String intraServerPassword, String receiver, long referrer) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        logger.info(this.getRemoteClientDetails() + " addReferrer for Receiver player name: " + receiver + ", referrerID: " + referrer);
        Object object = Server.SYNC_LOCK;
        synchronized (object) {
            try {
                PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(receiver);
                try {
                    pinf.load();
                }
                catch (IOException iox) {
                    return receiver + " - no such player exists. Please check the spelling.";
                }
                if (pinf.wurmId == referrer) {
                    return "You may not refer yourself.";
                }
                if (pinf.getPaymentExpire() <= 0L) {
                    return pinf.getName() + " has never had a premium account and may not receive referrals.";
                }
                if (PlayerInfoFactory.addReferrer(pinf.wurmId, referrer)) {
                    return String.valueOf(pinf.wurmId);
                }
                return "You have already awarded referral to that player.";
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage() + " " + receiver + " from " + referrer, e);
                return "An error occurred. Please write a bug report about this.";
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String acceptReferrer(String intraServerPassword, long wurmid, String awarderName, boolean money) throws RemoteException {
        block16: {
            this.validateIntraServerPassword(intraServerPassword);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(this.getRemoteClientDetails() + " acceptReferrer for player wurmid: " + wurmid + ", awarderName: " + awarderName + ", money: " + money);
            }
            String name = awarderName;
            PlayerInfo pinf = null;
            try {
                long l = Long.parseLong(awarderName);
                pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(l);
            }
            catch (NumberFormatException nfe) {
                pinf = PlayerInfoFactory.createPlayerInfo(name);
                try {
                    pinf.load();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                    return "Failed to locate the player " + awarderName + " in the database.";
                }
            }
            if (pinf != null) {
                try {
                    Object nfe = Server.SYNC_LOCK;
                    synchronized (nfe) {
                        if (PlayerInfoFactory.acceptReferer(wurmid, pinf.wurmId, money)) {
                            try {
                                if (money) {
                                    PlayerInfoFactory.addMoneyToBank(wurmid, 30000L, "Referred by " + pinf.getName());
                                }
                                PlayerInfoFactory.addPlayingTime(wurmid, 0, 20, "Referred by " + pinf.getName());
                            }
                            catch (Exception ex) {
                                logger.log(Level.WARNING, ex.getMessage(), ex);
                                PlayerInfoFactory.revertReferer(wurmid, pinf.wurmId);
                                return "An error occured. Please try later or post a bug report.";
                            }
                        } else {
                            return "Failed to match " + awarderName + " to any existing referral.";
                        }
                        break block16;
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                    return "An error occured. Please try later or post a bug report.";
                }
            }
            return "Failed to locate " + awarderName + " in the database.";
        }
        return "Okay, accepted the referral from " + awarderName + ". The reward will arrive soon if it has not already.";
    }

    @Override
    public Map<String, Double> getSkillStats(String intraServerPassword, int skillid) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getSkillStats for skillid: " + skillid);
        }
        HashMap<String, Double> toReturn = new HashMap<String, Double>();
        try {
            SkillStat sk = SkillStat.getSkillStatForSkill(skillid);
            for (Map.Entry<Long, Double> entry : sk.stats.entrySet()) {
                Long lid = entry.getKey();
                long pid = lid;
                PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(pid);
                if (p == null || !(entry.getValue() > 1.0)) continue;
                toReturn.put(p.getName(), entry.getValue());
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            toReturn.put("ERROR: " + ex.getMessage(), 0.0);
        }
        return toReturn;
    }

    @Override
    public Map<Integer, String> getSkills(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        return SkillSystem.skillNames;
    }

    @Override
    public Map<String, ?> getStructureSummary(String intraServerPassword, long aStructureID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getStructureSummary for StructureID: " + aStructureID);
        }
        HashMap<String, Object> lToReturn = new HashMap<String, Object>(10);
        try {
            Structure lStructure = Structures.getStructure(aStructureID);
            if (lStructure != null) {
                lToReturn.put("CenterX", lStructure.getCenterX());
                lToReturn.put("CenterY", lStructure.getCenterY());
                lToReturn.put("CreationDate", lStructure.getCreationDate());
                lToReturn.put("Door Count", lStructure.getDoors());
                lToReturn.put("FinalFinished", lStructure.isFinalFinished());
                lToReturn.put("Finalized", lStructure.isFinalized());
                lToReturn.put("Finished", lStructure.isFinished());
                lToReturn.put("Guest Count", lStructure.getPermissionsPlayerList().size());
                lToReturn.put("Limit", lStructure.getLimit());
                lToReturn.put("Lockable", lStructure.isLockable());
                lToReturn.put("Locked", lStructure.isLocked());
                lToReturn.put("MaxX", lStructure.getMaxX());
                lToReturn.put("MaxY", lStructure.getMaxY());
                lToReturn.put("MinX", lStructure.getMinX());
                lToReturn.put("MinY", lStructure.getMinY());
                lToReturn.put("Name", lStructure.getName());
                lToReturn.put("OwnerID", lStructure.getOwnerId());
                lToReturn.put("Roof", lStructure.getRoof());
                lToReturn.put("Size", lStructure.getSize());
                lToReturn.put("HasWalls", lStructure.hasWalls());
                Wall[] lWalls = lStructure.getWalls();
                if (lWalls != null) {
                    lToReturn.put("Wall Count", lWalls.length);
                } else {
                    lToReturn.put("Wall Count", 0);
                }
                lToReturn.put("WritID", lStructure.getWritId());
                lToReturn.put("WurmID", lStructure.getWurmId());
            } else {
                lToReturn.put("Error", "No such Structure");
            }
        }
        catch (NoSuchStructureException nss) {
            logger.log(Level.WARNING, "Structure with id " + aStructureID + " not found.", nss);
            lToReturn.put("Error", "No such Structure");
            lToReturn.put("Exception", nss.getMessage());
        }
        catch (RuntimeException e) {
            logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            lToReturn.put("Exception", e);
        }
        return lToReturn;
    }

    @Override
    public long getStructureIdFromWrit(String intraServerPassword, long aWritID) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getStructureIdFromWrit for WritID: " + aWritID);
        }
        try {
            Structure struct = Structures.getStructureForWrit(aWritID);
            if (struct != null) {
                return struct.getWurmId();
            }
        }
        catch (NoSuchStructureException noSuchStructureException) {
            // empty catch block
        }
        return -1L;
    }

    @Override
    public Map<String, ?> getTileSummary(String intraServerPassword, int tilex, int tiley, boolean surfaced) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getTileSummary for tile (x,y): " + tilex + ", " + tiley);
        }
        HashMap<String, Object> lToReturn = new HashMap<String, Object>(10);
        try {
            Zone zone = Zones.getZone(tilex, tiley, surfaced);
            VolaTile tile = zone.getTileOrNull(tilex, tiley);
            if (tile != null) {
                Structure lStructure = tile.getStructure();
                if (lStructure != null) {
                    lToReturn.put("StructureID", lStructure.getWurmId());
                    lToReturn.put("StructureName", lStructure.getName());
                }
                lToReturn.put("Kingdom", tile.getKingdom());
                Village lVillage = tile.getVillage();
                if (lVillage != null) {
                    lToReturn.put("VillageID", lVillage.getId());
                    lToReturn.put("VillageName", lVillage.getName());
                }
                lToReturn.put("Coord x", tile.getTileX());
                lToReturn.put("Coord y", tile.getTileY());
            } else {
                lToReturn.put("Error", "No such tile");
            }
        }
        catch (NoSuchZoneException e) {
            lToReturn.put("Error", "No such zone");
            lToReturn.put("Exception", e.getMessage());
        }
        catch (RuntimeException e) {
            logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            lToReturn.put("Exception", e);
        }
        return lToReturn;
    }

    @Override
    public String getReimbursementInfo(String intraServerPassword, String email) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getReimbursementInfo for email: " + email);
        }
        return Reimbursement.getReimbursementInfo(email);
    }

    @Override
    public boolean withDraw(String intraServerPassword, String retriever, String name, String _email, int _months, int _silvers, boolean titlebok, int _daysLeft) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        logger.info(this.getRemoteClientDetails() + " withDraw for retriever: " + retriever + ", name: " + name + ", email: " + _email + ", months: " + _months + ", silvers: " + _silvers);
        return Reimbursement.withDraw(retriever, name, _email, _months, _silvers, titlebok, _daysLeft);
    }

    @Override
    public boolean transferPlayer(String intraServerPassword, String playerName, int posx, int posy, boolean surfaced, int power, byte[] data) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (Constants.maintaining && power <= 0) {
            return false;
        }
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Transferplayer name: " + playerName + ", position (x,y): " + posx + ", " + posy + ", surfaced: " + surfaced);
        if (IntraServerConnection.savePlayerToDisk(data, posx, posy, surfaced, false) > 0L) {
            if (!Servers.isThisLoginServer()) {
                return new LoginServerWebConnection().setCurrentServer(playerName, Servers.localServer.id);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean changePassword(String intraServerPassword, long wurmId, String newPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword name: " + wurmId);
        return IntraServerConnection.setNewPassword(wurmId, newPassword);
    }

    @Override
    public boolean setCurrentServer(String intraServerPassword, String name, int currentServer) throws RemoteException {
        PlayerInfo pinf;
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " setCurrentServer to " + currentServer + " for player name: " + name);
        }
        if ((pinf = PlayerInfoFactory.createPlayerInfo(name)) == null) {
            return false;
        }
        pinf.setCurrentServer(currentServer);
        return true;
    }

    @Override
    public boolean addDraggedItem(String intraServerPassword, long itemId, byte[] itemdata, long draggerId, int posx, int posy) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
        logger.log(Level.INFO, this.getRemoteClientDetails() + " Adddraggeditem itemID: " + itemId + ", draggerId: " + draggerId);
        try {
            HashSet<ItemMetaData> idset = new HashSet<ItemMetaData>();
            int nums = iis.readInt();
            for (int x = 0; x < nums; ++x) {
                IntraServerConnection.createItem(iis, 0.0f, 0.0f, 0.0f, idset, false);
            }
            Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
            return false;
        }
        try {
            Item i = Items.getItem(itemId);
            Zone z = Zones.getZone(posx, posy, true);
            z.addItem(i);
            return true;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, nsi.getMessage(), nsi);
            return false;
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, nsz.getMessage(), nsz);
            return false;
        }
    }

    @Override
    public String rename(String intraServerPassword, String oldName, String newName, String newPass, int power) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " rename oldName: " + oldName + ", newName: " + newName + ", power: " + power);
        }
        String toReturn = "";
        newName = LoginHandler.raiseFirstLetter(newName);
        if (Servers.localServer.LOGINSERVER && Players.getInstance().doesPlayerNameExist(newName)) {
            return "The name " + newName + " already exists. This is an Error.";
        }
        if (Servers.localServer.LOGINSERVER) {
            toReturn = toReturn + Servers.rename(oldName, newName, newPass, power);
        }
        if (!toReturn.contains("Error.")) {
            try {
                toReturn = PlayerInfoFactory.rename(oldName, newName, newPass, power);
            }
            catch (IOException iox) {
                toReturn = toReturn + Servers.localServer.name + " " + iox.getMessage() + ". This is an Error.\n";
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
        return toReturn;
    }

    @Override
    public String changePassword(String intraServerPassword, String changerName, String name, String newPass, int power) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " changePassword, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
        }
        String toReturn = "";
        changerName = LoginHandler.raiseFirstLetter(changerName);
        name = LoginHandler.raiseFirstLetter(name);
        try {
            toReturn = PlayerInfoFactory.changePassword(changerName, name, newPass, power);
        }
        catch (IOException iox) {
            toReturn = toReturn + Servers.localServer.name + " " + iox.getMessage() + "\n";
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        logger.log(Level.INFO, this.getRemoteClientDetails() + " changePassword, changerName: " + changerName + ", for player name: " + name);
        if (Servers.localServer.LOGINSERVER) {
            if (changerName.equals(name)) {
                PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
                if (pinf != null && Servers.localServer.id != pinf.currentServer) {
                    LoginServerWebConnection lsw = new LoginServerWebConnection(pinf.currentServer);
                    toReturn = toReturn + lsw.changePassword(changerName, name, newPass, power);
                }
            } else {
                toReturn = toReturn + Servers.sendChangePass(changerName, name, newPass, power);
            }
        }
        return toReturn;
    }

    @Override
    public String changeEmail(String intraServerPassword, String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " changeEmail, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
        }
        changerName = LoginHandler.raiseFirstLetter(changerName);
        name = LoginHandler.raiseFirstLetter(name);
        String toReturn = "";
        logger.log(Level.INFO, this.getRemoteClientDetails() + " changeEmail, changerName: " + changerName + ", for player name: " + name);
        try {
            toReturn = PlayerInfoFactory.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
            if (toReturn.equals("NO") || toReturn.equals("NO Retrieval info updated.")) {
                return "You may only have 5 accounts with the same email. Also you need to provide the correct password for a character with that email address in order to change to it.";
            }
        }
        catch (IOException iox) {
            toReturn = toReturn + Servers.localServer.name + " " + iox.getMessage() + "\n";
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        if (Servers.localServer.LOGINSERVER) {
            toReturn = toReturn + Servers.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
        }
        return toReturn;
    }

    @Override
    public String addReimb(String intraServerPassword, String changerName, String name, int numMonths, int _silver, int _daysLeft, boolean setbok) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(this.getRemoteClientDetails() + " addReimb, changerName: " + changerName + ", for player name: " + name + ", numMonths: " + numMonths + ", silver: " + _silver + ", daysLeft: " + _daysLeft + ", setbok: " + setbok);
        }
        changerName = LoginHandler.raiseFirstLetter(changerName);
        name = LoginHandler.raiseFirstLetter(name);
        if (Servers.localServer.LOGINSERVER) {
            return Reimbursement.addReimb(changerName, name, numMonths, _silver, _daysLeft, setbok);
        }
        return Servers.localServer.name + " - failed to add reimbursement. This is not the login server.";
    }

    @Override
    public long[] getCurrentServerAndWurmid(String intraServerPassword, String name, long wurmid) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getCurrentServerAndWurmid for player name: " + name + ", wurmid: " + wurmid);
        }
        PlayerInfo pinf = null;
        if (name != null && name.length() > 2) {
            name = LoginHandler.raiseFirstLetter(name);
            pinf = PlayerInfoFactory.createPlayerInfo(name);
        } else if (wurmid > 0L) {
            pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        }
        if (pinf != null) {
            try {
                pinf.load();
                long[] toReturn = new long[]{pinf.currentServer, pinf.wurmId};
                return toReturn;
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return noInfoLong;
    }

    @Override
    public Map<Long, byte[]> getPlayerStates(String intraServerPassword, long[] wurmids) throws RemoteException, WurmServerException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            if (wurmids.length == 0) {
                logger.finer(this.getRemoteClientDetails() + " getPlayersSubInfo for ALL players.");
            } else {
                StringBuilder buf = new StringBuilder();
                for (int x = 0; x < wurmids.length; ++x) {
                    if (x > 0) {
                        buf.append(",");
                    }
                    buf.append(wurmids[x]);
                }
                logger.finer(this.getRemoteClientDetails() + " getPlayersSubInfo for player wurmids: " + buf.toString());
            }
        }
        return PlayerInfoFactory.getPlayerStates(wurmids);
    }

    @Override
    public void manageFeature(String intraServerPassword, int serverId, int featureId, boolean aOverridden, boolean aEnabled, boolean global) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " manageFeature " + featureId);
        }
        Thread t = new Thread("manageFeature-Thread-" + featureId){

            @Override
            public void run() {
                Features.Feature.setOverridden(Servers.getLocalServerId(), featureId, aOverridden, aEnabled, global);
            }
        };
        t.setPriority(4);
        t.start();
    }

    @Override
    public void startShutdown(String intraServerPassword, String instigator, int seconds, String reason) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (Servers.isThisLoginServer()) {
            Servers.startShutdown(instigator, seconds, reason);
        }
        logger.log(Level.INFO, instigator + " shutting down server in " + seconds + " seconds, reason: " + reason);
        Server.getInstance().startShutdown(seconds, reason);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public String sendMail(String intraServerPassword, byte[] maildata, byte[] itemdata, long sender, long wurmid, int targetServer) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        logger.log(Level.INFO, this.getRemoteClientDetails() + " sendMail " + sender + " to server " + targetServer + ", receiver ID: " + wurmid);
        if (targetServer == Servers.localServer.id) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(maildata));
            try {
                int nums = dis.readInt();
                for (int x = 0; x < nums; ++x) {
                    WurmMail m = new WurmMail(dis.readByte(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readLong(), dis.readInt(), dis.readBoolean(), false);
                    WurmMail.addWurmMail(m);
                    m.createInDatabase();
                }
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
                return "A database error occurred. Please report this to a GM.";
            }
            DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
            try {
                HashSet<ItemMetaData> idset = new HashSet<ItemMetaData>();
                int nums = iis.readInt();
                for (int x = 0; x < nums; ++x) {
                    IntraServerConnection.createItem(iis, 0.0f, 0.0f, 0.0f, idset, false);
                }
                Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
                return "";
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
                return "A database error occurred when inserting an item. Please report this to a GM.";
            }
        }
        ServerEntry entry = Servers.getServerWithId(targetServer);
        if (entry == null) return "Failed to locate target server.";
        if (!entry.isAvailable(5, true)) return "The target server is not available right now.";
        LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
        return lsw.sendMail(maildata, itemdata, sender, wurmid, targetServer);
    }

    @Override
    public String pardonban(String intraServerPassword, String name) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " pardonban for player name: " + name);
        }
        if (Servers.localServer.LOGINSERVER) {
            PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
            if (info != null) {
                try {
                    info.load();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, this.getRemoteClientDetails() + " Failed to load the player information. Not pardoned - " + iox.getMessage(), iox);
                    return "Failed to load the player information. Not pardoned.";
                }
                try {
                    info.setBanned(false, "", 0L);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, this.getRemoteClientDetails() + " Failed to save the player information. Not pardoned - " + iox.getMessage(), iox);
                    return "Failed to save the player information. Not pardoned.";
                }
                logger.info(this.getRemoteClientDetails() + " Login server pardoned " + name);
                return "Login server pardoned " + name + ".";
            }
            logger.warning("Failed to locate the player " + name + ".");
            return "Failed to locate the player " + name + ".";
        }
        logger.warning(Servers.localServer.name + " not login server. Pardon failed.");
        return Servers.localServer.name + " not login server. Pardon failed.";
    }

    @Override
    public String ban(String intraServerPassword, String name, String reason, int days) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " ban for player name: " + name + ", reason: " + reason + ", for " + days + " days");
        }
        if (Servers.localServer.LOGINSERVER) {
            PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
            if (info != null) {
                long expiry = System.currentTimeMillis() + (long)days * 86400000L;
                try {
                    info.load();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to load the player information. Not banned - " + iox.getMessage(), iox);
                    return "Failed to load the player information. Not banned.";
                }
                try {
                    info.setBanned(true, reason, expiry);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save the player information. Not banned - " + iox.getMessage(), iox);
                    return "Failed to save the player information. Not banned.";
                }
                logger.info(this.getRemoteClientDetails() + " Login server banned " + name + ": " + reason + " for " + days + " days.");
                return "Login server banned " + name + ": " + reason + " for " + days + " days.";
            }
            logger.warning("Failed to locate the player " + name + ".");
            return "Failed to locate the player " + name + ".";
        }
        logger.warning(Servers.localServer.name + " not login server. IPBan failed.");
        return Servers.localServer.name + " not login server. IPBan failed.";
    }

    @Override
    public String addBannedIp(String intraServerPassword, String ip, String reason, int days) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        long expiry = System.currentTimeMillis() + (long)days * 86400000L;
        Players.getInstance().addBannedIp(ip, reason, expiry);
        logger.info(this.getRemoteClientDetails() + " RMI client requested " + ip + " banned for " + days + " days - " + reason);
        return ip + " banned for " + days + " days - " + reason;
    }

    @Override
    public Ban[] getPlayersBanned(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        return Players.getInstance().getPlayersBanned();
    }

    @Override
    public Ban[] getIpsBanned(String intraServerPassword) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        return Players.getInstance().getBans();
    }

    @Override
    public String removeBannedIp(String intraServerPassword, String ip) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (Players.getInstance().removeBan(ip)) {
            logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client requested " + ip + " was pardoned.");
            return "Okay, " + ip + " was pardoned.";
        }
        logger.info(this.getRemoteClientDetails() + " RMI client requested pardon but the ip " + ip + " was not previously banned.");
        return "The ip " + ip + " was not previously banned.";
    }

    @Override
    public String setPlayerMoney(String intraServerPassword, long wurmid, long currentMoney, long moneyAdded, String detail) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (moneyDetails.contains(detail)) {
            logger.warning(this.getRemoteClientDetails() + " RMI client The money transaction has already been performed, wurmid: " + wurmid + ", currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
            return "The money transaction has already been performed";
        }
        logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client set player money for " + wurmid);
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        if (info != null) {
            try {
                info.load();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to load player info for " + wurmid + ", detail: " + detail + ": " + iox.getMessage(), iox);
                return "Failed to load the player from database. Transaction failed.";
            }
        } else {
            logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
            return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
        }
        if (info.wurmId > 0L) {
            if (info.currentServer != Servers.localServer.id) {
                logger.warning("Received a CMD_SET_PLAYER_MONEY for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
                return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
            }
            try {
                Change c;
                block11: {
                    info.setMoney(currentMoney);
                    new MoneyTransfer(info.getName(), wurmid, currentMoney, moneyAdded, detail, 6, "");
                    c = new Change(currentMoney);
                    moneyDetails.add(detail);
                    try {
                        logger.info(this.getRemoteClientDetails() + " RMI client Added " + moneyAdded + " to player ID: " + wurmid + ", currentMoney: " + currentMoney + ", detail: " + detail);
                        Player p = Players.getInstance().getPlayer(wurmid);
                        Message mess = new Message(null, 3, ":Event", "Your available money in the bank is now " + c.getChangeString() + ".");
                        mess.setReceiver(p.getWurmId());
                        Server.getInstance().addMessage(mess);
                    }
                    catch (NoSuchPlayerException exp) {
                        if (!logger.isLoggable(Level.FINER)) break block11;
                        logger.finer("player ID: " + wurmid + " is not online, currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail);
                    }
                }
                return "Okay. The player now has " + c.getChangeString() + " in the bank.";
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, wurmid + ", failed to set money to " + currentMoney + ", detail: " + detail + ".", iox);
                return "Money transaction failed. Error reported was " + iox.getMessage() + ".";
            }
        }
        logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
        return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
    }

    @Override
    public String setPlayerPremiumTime(String intraServerPassword, long wurmid, long currentExpire, int days, int months, String detail) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (timeDetails.contains(detail)) {
            logger.warning(this.getRemoteClientDetails() + " RMI client The time transaction has already been performed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail);
            return "The time transaction has already been performed";
        }
        logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client set premium time for " + wurmid);
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        if (info != null) {
            try {
                info.load();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to load the player from database. Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail, iox);
                return "Failed to load the player from database. Transaction failed.";
            }
            if (info.currentServer != Servers.localServer.id) {
                logger.warning("Received a CMD_SET_PLAYER_PAYMENTEXPIRE for player " + info.getName() + " (id: " + wurmid + ") but their currentserver (id: " + info.getCurrentServer() + ") is not this server (id: " + Servers.localServer.id + "), detail: " + detail);
                return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
            }
            try {
                info.setPaymentExpire(currentExpire);
                new TimeTransfer(info.getName(), wurmid, months, false, days, detail);
                timeDetails.add(detail);
                try {
                    Player p = Players.getInstance().getPlayer(wurmid);
                    String expireString = "You now have premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
                    Message mess = new Message(null, 3, ":Event", expireString);
                    mess.setReceiver(p.getWurmId());
                    Server.getInstance().addMessage(mess);
                }
                catch (NoSuchPlayerException p) {
                    // empty catch block
                }
                logger.info(this.getRemoteClientDetails() + " RMI client " + info.getName() + " now has premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ", wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail + '.');
                return "Okay. " + info.getName() + " now has premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Transaction failed, wurmid: " + wurmid + ", currentExpire: " + currentExpire + ", days: " + days + ", months: " + months + ", detail: " + detail + ", " + iox.getMessage(), iox);
                return "Time transaction failed. Error reported was " + iox.getMessage() + ".";
            }
        }
        logger.log(Level.WARNING, wurmid + ", failed to locate player info and set expire time to " + currentExpire + "!, detail: " + detail);
        return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
    }

    @Override
    public void setWeather(String intraServerPassword, float windRotation, float windpower, float windDir) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        Server.getWeather().setWindOnly(windRotation, windpower, windDir);
        logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client. Received weather data from login server. Propagating windrot=" + windRotation);
        Players.getInstance().setShouldSendWeather(true);
    }

    @Override
    public String sendVehicle(String intraServerPassword, byte[] passengerdata, byte[] itemdata, long pilotId, long vehicleId, int targetServer, int tilex, int tiley, int layer, float rot) throws RemoteException {
        long start;
        this.validateIntraServerPassword(intraServerPassword);
        logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client send vehicle for pilot " + pilotId + " vehicle " + vehicleId + " itemdata bytes=" + itemdata.length + " passenger data bytes=" + passengerdata.length);
        if (targetServer == Servers.localServer.id) {
            start = System.nanoTime();
            DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
            HashSet<ItemMetaData> idset = new HashSet<ItemMetaData>();
            try {
                int nums = iis.readInt();
                logger.log(Level.INFO, "Trying to create " + nums + " items for vehicle: " + vehicleId);
                float posx = tilex * 4 + 2;
                float posy = tiley * 4 + 2;
                IntraServerConnection.resetTransferVariables(String.valueOf(pilotId));
                for (int x = 0; x < nums; ++x) {
                    IntraServerConnection.createItem(iis, posx, posy, 0.0f, idset, false);
                }
                Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId, iox);
                for (ItemMetaData md : idset) {
                    logger.log(Level.INFO, md.itname + ", " + md.itemId);
                }
                return "A database error occurred when inserting an item. Please report this to a GM.";
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId, ex);
                return "A database error occurred when inserting an item. Please report this to a GM.";
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(passengerdata));
            try {
                Item i = Items.getItem(vehicleId);
                i.setPosXYZ(tilex * 4 + 2, tiley * 4 + 2, 0.0f);
                i.setRotation(rot);
                logger.log(Level.INFO, "Trying to put " + i.getName() + ", " + i.getDescription() + " at " + i.getTileX() + "," + i.getTileY());
                Zones.getZone(i.getTileX(), i.getTileY(), layer == 0).addItem(i);
                Vehicles.createVehicle(i);
                MountTransfer mt = new MountTransfer(vehicleId, pilotId);
                int nums = dis.readInt();
                for (int x = 0; x < nums; ++x) {
                    mt.addToSeat(dis.readLong(), dis.readInt());
                }
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + nsi.getMessage(), nsi);
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + nsz.getMessage(), nsz);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + iox.getMessage(), iox);
                return "A database error occurred. Please report this to a GM.";
            }
        } else {
            ServerEntry entry = Servers.getServerWithId(targetServer);
            if (entry != null) {
                if (entry.isAvailable(5, true)) {
                    LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
                    return lsw.sendVehicle(passengerdata, itemdata, pilotId, vehicleId, targetServer, tilex, tiley, layer, rot);
                }
                return "The target server is not available right now.";
            }
            return "Failed to locate target server.";
        }
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.log(Level.INFO, "Transferring vehicle " + vehicleId + " took " + lElapsedTime + " ms.");
        return "";
    }

    @Override
    public void genericWebCommand(String intraServerPassword, short wctype, long id, byte[] data) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        WebCommand wc = WebCommand.createWebCommand(wctype, id, data);
        if (wc != null) {
            if (Servers.localServer.LOGINSERVER && wc.autoForward()) {
                Servers.sendWebCommandToAllServers(wctype, wc, wc.isEpicOnly());
            }
            if (!(WurmId.getOrigin(id) == Servers.localServer.id)) {
                Server.getInstance().addWebCommand(wc);
            }
        }
    }

    @Override
    public void setKingdomInfo(String intraServerPassword, int serverId, byte kingdomId, byte templateKingdom, String _name, String _password, String _chatName, String _suffix, String mottoOne, String mottoTwo, boolean acceptsPortals) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        Kingdom newInfo = new Kingdom(kingdomId, templateKingdom, _name, _password, _chatName, _suffix, mottoOne, mottoTwo, acceptsPortals);
        if (serverId != Servers.localServer.id) {
            Kingdoms.addKingdom(newInfo);
        }
        WcKingdomInfo wck = new WcKingdomInfo(WurmId.getNextWCCommandId(), true, kingdomId);
        wck.encode();
        Servers.sendWebCommandToAllServers((short)7, wck, wck.isEpicOnly());
    }

    @Override
    public boolean kingdomExists(String intraServerPassword, int serverId, byte kingdomId, boolean exists) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        logger.log(Level.INFO, "serverId:" + serverId + " kingdom id " + kingdomId + " exists=" + exists);
        boolean result = Servers.kingdomExists(serverId, kingdomId, exists);
        if (Servers.getServerWithId(serverId) != null && Servers.getServerWithId((int)serverId).name != null) {
            logger.log(Level.INFO, Servers.getServerWithId((int)serverId).name + " kingdom id " + kingdomId + " exists=" + exists);
        } else if (Servers.getServerWithId(serverId) == null) {
            logger.log(Level.INFO, serverId + " server is null " + kingdomId + " exists=" + exists);
        } else {
            logger.log(Level.INFO, "Name for " + Servers.getServerWithId(serverId) + " server is null " + kingdomId + " exists=" + exists);
        }
        if (Servers.localServer.LOGINSERVER) {
            if (!exists) {
                if (!result) {
                    Kingdom k = Kingdoms.getKingdomOrNull(kingdomId);
                    boolean sendDelete = false;
                    if (k != null && k.isCustomKingdom()) {
                        k.delete();
                        Kingdoms.removeKingdom(kingdomId);
                        sendDelete = true;
                    }
                } else {
                    Servers.sendKingdomExistsToAllServers(serverId, kingdomId, false);
                }
            } else {
                Servers.sendKingdomExistsToAllServers(serverId, kingdomId, true);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        if (args.length == 2 && args[0].compareTo("ShutdownLive") == 0) {
            try {
                WebInterfaceTest wit = new WebInterfaceTest();
                System.out.println("Shutting down ALL live servers!");
                wit.shutdownAll("Maintenance restart. Up to thirty minutes downtime.", Integer.parseInt(args[1]));
                System.out.println("I do hope this is what you wanted. All servers will be down in approximately " + args[1] + " seconds.");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (args.length == 3) {
            try {
                WebInterfaceTest wit = new WebInterfaceTest();
                System.out.println("Attempting to shutdown server at " + args[0] + ", port " + args[1]);
                String[] userInfo = args[2].split(":");
                wit.shutDown(args[0], args[1], userInfo[0], userInfo[1]);
            }
            catch (Exception ex) {
                logger.log(Level.INFO, "failed to shut down localhost", ex);
                ex.printStackTrace();
            }
        } else {
            System.out.println("Usage:\nNo arguments - This message.\nShutdownLive <delay> - Shutsdown ALL LIVE SERVERS using the seconds provided as a delay\n<host> <port> <user>:<password> - Shutdown the specified server using your GM credentials.");
        }
    }

    private boolean validateAccount(String user, String password, byte power) throws IOException, Exception {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(user));
        if (pinf == null) {
            return false;
        }
        pinf.load();
        if (pinf.getPower() <= power) {
            return false;
        }
        String pw = LoginHandler.encrypt(pinf.getName() + password);
        return pw.equals(pinf.getPassword());
    }

    private void interactiveShutdown() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int state = 0;
        boolean interactive = true;
        String user = "";
        String message = "Maintenance shutdown. Up to thirty minutes downtime. See the forums for more information: http://forum.wurmonline.com/";
        int delay = 1800;
        System.out.println("[Shutdown Servers]\n(Type 'quit' at any time to abort)");
        while (interactive) {
            try {
                switch (state) {
                    case 0: {
                        System.out.print("GM Name: ");
                        user = br.readLine().trim();
                        state = 1;
                        break;
                    }
                    case 1: {
                        System.out.print("GM password: ");
                        String password = br.readLine().trim();
                        if (!this.validateAccount(user, password, (byte)4)) {
                            interactive = false;
                            System.out.println("Invalid password or power level insufficient.");
                            return;
                        }
                        state = 2;
                        break;
                    }
                    case 2: {
                        System.out.print("Message: [default '" + message + "'] ");
                        String in = br.readLine().trim();
                        if (!in.isEmpty()) {
                            message = in;
                        }
                        state = 3;
                        in = "";
                        break;
                    }
                    case 3: {
                        System.out.print("Delay: [default '" + delay + "']");
                        String in = br.readLine().trim();
                        if (!in.isEmpty()) {
                            delay = Integer.valueOf(in);
                        }
                        state = 4;
                    }
                }
                String s = br.readLine();
                System.out.print("Enter Integer:");
                int n = Integer.parseInt(br.readLine());
            }
            catch (NumberFormatException nfe) {
                System.err.println("Invalid Format!");
            }
            catch (Exception exception) {}
        }
    }

    @Override
    public void requestDemigod(String intraServerPassword, byte existingDeity, String existingDeityName) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        Player[] players = Players.getInstance().getPlayers();
        for (int x = 0; x < players.length; ++x) {
            MissionPerformer mp;
            if (players[x].getKingdomTemplateId() != Deities.getFavoredKingdom(existingDeity) || players[x].getPower() != 0 && !Servers.localServer.testServer || (mp = MissionPerformed.getMissionPerformer(players[x].getWurmId())) == null) continue;
            MissionPerformed[] perfs = mp.getAllMissionsPerformed();
            int numsForDeity = 0;
            logger.log(Level.INFO, "Checking if " + players[x].getName() + " can be elevated.");
            for (MissionPerformed mpf : perfs) {
                Mission m = mpf.getMission();
                if (m == null) continue;
                logger.log(Level.INFO, "Found a mission for " + existingDeityName);
                if (m.getCreatorType() != 2 || m.getOwnerId() != (long)existingDeity) continue;
                ++numsForDeity;
            }
            logger.log(Level.INFO, "Found " + numsForDeity + " missions for " + players[x].getName());
            if (Server.rand.nextInt(numsForDeity) <= 2) continue;
            logger.log(Level.INFO, "Sending ascension to " + players[x].getName());
            AscensionQuestion asc = new AscensionQuestion(players[x], existingDeity, existingDeityName);
            asc.sendQuestion();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public String ascend(String intraServerPassword, int newId, String deityName, long wurmid, byte existingDeity, byte gender, byte newPower, float initialBStr, float initialBSta, float initialBCon, float initialML, float initialMS, float initialSS, float initialSD) {
        try {
            this.validateIntraServerPassword(intraServerPassword);
        }
        catch (AccessException e) {
            e.printStackTrace();
        }
        String toReturn = "";
        if (Servers.localServer.LOGINSERVER) {
            Deity deity = null;
            if (newPower == 2) {
                deity = Deities.ascend(newId, deityName, wurmid, gender, newPower, -1.0f, -1.0f);
                if (deity == null) return "Ouch, failed to save your demigod on the login server. Please contact administration";
                StringBuilder builder = new StringBuilder("You have now ascended! ");
                if (initialBStr < 30.0f) {
                    builder.append("The other immortals will not fear your strength initially. ");
                } else if (initialBStr < 45.0f) {
                    builder.append("You have acceptable strength as a demigod. ");
                } else if (initialBStr < 60.0f) {
                    builder.append("Your strength and skills will impress other immortals. ");
                } else {
                    builder.append("Your enormous strength will strike fear in other immortals. ");
                }
                if (initialBSta < 30.0f) {
                    builder.append("You are not the most vital demigod around so you will have to watch your back in the beginning. ");
                } else if (initialBSta < 45.0f) {
                    builder.append("Your vitality is acceptable and will earn respect. ");
                } else if (initialBSta < 60.0f) {
                    builder.append("You have good vitality and can expect a bright future as immortal. ");
                } else {
                    builder.append("Other immortals will envy your fantastic vitality and avoid confrontations with you. ");
                }
                if (deity.isHealer()) {
                    builder.append("Your love and kindness will be a beacon for everyone to follow. ");
                } else if (deity.isHateGod()) {
                    builder.append("Your true nature turns out to be based on rage and hate. ");
                }
                if (deity.isForestGod()) {
                    builder.append("Love for trees and living things will bind your followers together. ");
                }
                if (deity.isMountainGod()) {
                    builder.append("Your followers will look for you in high places and fear and adore you as they do the dragon. ");
                }
                if (deity.isWaterGod()) {
                    builder.append("You will be considered the pathfinder and explorer of your kin. ");
                }
                HexMap.VALREI.addDemigod(deityName, deity.number, existingDeity, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD);
                return builder.toString();
            }
            if (newPower <= 2) return toReturn;
            String sgender = "He";
            String sposs = "his";
            if (gender == 1) {
                sgender = "She";
                sposs = "her";
            }
            Servers.ascend(newId, deityName, wurmid, existingDeity, gender, newPower, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD);
            HistoryManager.addHistory(deityName, "has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender.toLowerCase() + " will now forever partake in the hunts on Valrei!");
            Server.getInstance().broadCastSafe(deityName + " has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender.toLowerCase() + " will now forever partake in the hunts on Valrei!");
            return toReturn;
        } else {
            if (newPower <= 2) return toReturn;
            Deities.ascend(newId, deityName, wurmid, gender, newPower, -1.0f, -1.0f);
            String sgender = "He";
            String sposs = "his";
            if (gender == 1) {
                sgender = "She";
                sposs = "her";
            }
            HistoryManager.addHistory(deityName, "has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender.toLowerCase() + " will now forever partake in the hunts on Valrei!");
            Server.getInstance().broadCastSafe(deityName + " has joined the ranks of true deities. " + sgender + " invites you to join " + sposs + " religion, as " + sgender.toLowerCase() + " will now forever partake in the hunts on Valrei!");
        }
        return toReturn;
    }

    @Override
    public final int[] getPremTimeSilvers(String intraServerPassword, long wurmId) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
        if (info != null) {
            try {
                if (!info.loaded) {
                    info.load();
                }
                if (info.getPaymentExpire() > 0L && info.awards != null) {
                    int[] toReturn = new int[]{info.awards.getMonthsPaidEver(), info.awards.getSilversPaidEver()};
                    return toReturn;
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return emptyIntZero;
    }

    @Override
    public void awardPlayer(String intraServerPassword, long wurmid, String name, int days, int months) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        Server.addPendingAward(new PendingAward(wurmid, name, days, months));
    }

    @Override
    public boolean requestDeityMove(String intraServerPassword, int deityNum, int desiredHex, String guide) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (Servers.localServer.LOGINSERVER) {
            EpicEntity entity = HexMap.VALREI.getEntity(deityNum);
            if (entity != null) {
                logger.log(Level.INFO, "Requesting move for " + entity);
                MapHex mh = HexMap.VALREI.getMapHex(desiredHex);
                if (mh != null) {
                    entity.setNextTargetHex(desiredHex);
                    entity.broadCastWithName(" was guided by " + guide + " towards " + mh.getName() + ".");
                    entity.sendEntityData();
                    return true;
                }
                logger.log(Level.INFO, "No hex for " + desiredHex);
            } else {
                logger.log(Level.INFO, "Requesting move for nonexistant " + deityNum);
            }
        }
        return false;
    }

    private void validateIntraServerPassword(String intraServerPassword) throws AccessException {
        if (!Servers.localServer.INTRASERVERPASSWORD.equals(intraServerPassword)) {
            throw new AccessException(BAD_PASSWORD);
        }
    }

    @Override
    public boolean isFeatureEnabled(String intraServerPassword, int aFeatureId) throws RemoteException {
        this.validateIntraServerPassword(intraServerPassword);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " isFeatureEnabled " + aFeatureId);
        }
        return Features.Feature.isFeatureEnabled(aFeatureId);
    }

    @Override
    public boolean setPlayerFlag(String intraServerPassword, long wurmid, int flag, boolean set) throws RemoteException {
        return false;
    }

    public boolean setPlayerFlag(long wurmid, int flag, boolean set) {
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
        if (pinf != null) {
            pinf.setFlag(flag, set);
            return true;
        }
        return false;
    }
}


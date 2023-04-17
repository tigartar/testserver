/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.webinterface.WcCreateEpicMission;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoginServerWebConnection {
    private WebInterface wurm = null;
    private static Logger logger = Logger.getLogger(LoginServerWebConnection.class.getName());
    private int serverId;
    private static final char EXCLAMATION_MARK = '!';
    private static final String FAILED_TO_CREATE_TRINKET = ", failed to create trinket! ";
    private static final String YOU_RECEIVED = "You received ";
    private static final String AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER = "An error occurred when contacting the login server. Please try later.";
    private static final String FAILED_TO_CONTACT_THE_LOGIN_SERVER = "Failed to contact the login server ";
    private static final String FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER = "Failed to contact the login server. Please try later.";
    private static final String FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER = "Failed to contact the bank. Please try later.";
    private static final String GAME_SERVER_IS_CURRENTLY_UNAVAILABLE = "The game server is currently unavailable.";
    private static final char COLON_CHAR = ':';
    private String intraServerPassword;
    static final int[] failedIntZero = new int[]{-1, -1};

    public LoginServerWebConnection() {
        this.serverId = Servers.loginServer.id;
        this.intraServerPassword = Servers.localServer.INTRASERVERPASSWORD;
    }

    public LoginServerWebConnection(int aServerId) {
        this.serverId = Servers.loginServer.id;
        this.intraServerPassword = Servers.localServer.INTRASERVERPASSWORD;
        this.serverId = aServerId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void connect() throws MalformedURLException, RemoteException, NotBoundException {
        if (this.wurm == null) {
            if (Servers.localServer.id == this.serverId) {
                this.wurm = new WebInterfaceImpl();
            } else {
                long lStart = System.nanoTime();
                String name = null;
                try {
                    ServerEntry server = Servers.getServerWithId(this.serverId);
                    if (server == null) {
                        throw new RemoteException("Server " + this.serverId + " not found");
                    }
                    if (!server.isAvailable(5, true)) {
                        throw new RemoteException("Server unavailable");
                    }
                    this.intraServerPassword = server.INTRASERVERPASSWORD;
                    name = "//" + server.INTRASERVERADDRESS + ':' + server.RMI_PORT + "/" + "wuinterface";
                    this.wurm = (WebInterface)Naming.lookup(name);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Looking up WebInterface RMI: " + name + " took " + (float)(System.nanoTime() - lStart) / 1000000.0f + "ms.");
                    }
                }
                catch (Throwable throwable) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Looking up WebInterface RMI: " + name + " took " + (float)(System.nanoTime() - lStart) / 1000000.0f + "ms.");
                    }
                    throw throwable;
                }
            }
        }
    }

    public int getServerId() {
        return this.serverId;
    }

    public byte[] createAndReturnPlayer(String playerName, String hashedIngamePassword, String challengePhrase, String challengeAnswer, String emailAddress, byte kingdom, byte power, long appearance, byte gender, boolean titleKeeper, boolean addPremium, boolean passwordIsHashed) throws Exception {
        if (this.wurm == null) {
            this.connect();
        }
        if (this.wurm != null) {
            return this.wurm.createAndReturnPlayer(this.intraServerPassword, playerName, hashedIngamePassword, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender, titleKeeper, addPremium, passwordIsHashed);
        }
        throw new RemoteException("Failed to create web connection.");
    }

    public long chargeMoney(String playerName, long moneyToCharge) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, playerName + " + Failed to contact the login server " + ex.getMessage());
                return -10L;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.chargeMoney(this.intraServerPassword, playerName, moneyToCharge);
            }
            catch (RemoteException rx) {
                return -10L;
            }
        }
        return -10L;
    }

    public boolean addPlayingTime(Creature player, String name, int months, int days, String detail) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER);
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                Map<String, String> result = this.wurm.addPlayingTime(this.intraServerPassword, name, months, days, detail, Servers.localServer.testServer || player.getPower() > 0);
                for (Map.Entry<String, String> e : result.entrySet()) {
                    if (e.getKey().equals("error")) {
                        player.getCommunicator().sendAlertServerMessage(e.getValue());
                        return false;
                    }
                    if (!e.getKey().equals("ok")) continue;
                    return true;
                }
            }
            catch (RemoteException rx) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER);
                return false;
            }
        }
        return false;
    }

    public boolean addMoney(Creature player, String name, long money, String detail) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER);
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                Map<String, String> result = this.wurm.addMoneyToBank(this.intraServerPassword, name, money, detail, false);
                for (Map.Entry<String, String> e : result.entrySet()) {
                    if (e.getKey().equals("error")) {
                        player.getCommunicator().sendAlertServerMessage(e.getValue());
                        return false;
                    }
                    if (!e.getKey().equals("ok")) continue;
                    return true;
                }
            }
            catch (RemoteException rx) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER);
                return false;
            }
        }
        return false;
    }

    public long getMoney(Creature player) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER);
                logger.log(Level.WARNING, player.getName() + " " + FAILED_TO_CONTACT_THE_LOGIN_SERVER + " " + this.serverId + " " + ex.getMessage());
                return 0L;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getMoney(this.intraServerPassword, player.getWurmId(), player.getName());
            }
            catch (RemoteException rx) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER);
                return 0L;
            }
        }
        return 0L;
    }

    public boolean addMoney(long wurmid, String name, long money, String detail) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, wurmid + ": failed to receive " + money + ", " + detail + ", " + ex.getMessage());
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                Map<String, String> result = this.wurm.addMoneyToBank(this.intraServerPassword, name, wurmid, money, detail, false);
                for (Map.Entry<String, String> e : result.entrySet()) {
                    if (e.getKey().equals("error")) {
                        logger.log(Level.WARNING, wurmid + ": failed to receive " + money + ", " + detail + ", " + e.getValue());
                        return false;
                    }
                    if (!e.getKey().equals("ok")) continue;
                    return true;
                }
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, wurmid + ": failed to receive " + money + ", " + detail + ", " + rx, rx);
                return false;
            }
        }
        return false;
    }

    public void testAdding(String playerName) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, playerName + ": " + ex.getMessage(), ex);
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return;
            }
        }
        try {
            Map<String, String> result = this.wurm.addPlayingTime(this.intraServerPassword, playerName, 1, 4, "test" + System.currentTimeMillis());
            for (Map.Entry<String, String> e : result.entrySet()) {
                logger.log(Level.INFO, e.getKey() + ':' + e.getValue());
            }
            Map<String, String> result2 = this.wurm.addMoneyToBank(this.intraServerPassword, playerName, 10000L, "test" + System.currentTimeMillis());
            for (Map.Entry<String, String> e : result2.entrySet()) {
                logger.log(Level.INFO, e.getKey() + ':' + e.getValue());
            }
        }
        catch (RemoteException rx) {
            logger.log(Level.WARNING, rx.getMessage(), rx);
            return;
        }
    }

    public void setWeather(float windRotation, float windpower, float windDir) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                return;
            }
        }
        if (this.wurm != null) {
            try {
                this.wurm.setWeather(this.intraServerPassword, windRotation, windpower, windDir);
            }
            catch (RemoteException rx) {
                return;
            }
        }
    }

    public Map<String, Byte> getReferrers(Creature player, long wurmid) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return null;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getReferrers(this.intraServerPassword, wurmid);
            }
            catch (RemoteException rx) {
                player.getCommunicator().sendAlertServerMessage(AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER);
            }
        }
        return null;
    }

    public void addReferrer(Player player, String receiver) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return;
            }
        }
        if (this.wurm != null) {
            try {
                String mess = this.wurm.addReferrer(this.intraServerPassword, receiver, player.getWurmId());
                try {
                    long referrer = Long.parseLong(mess);
                    player.getSaveFile().setReferedby(referrer);
                    player.getCommunicator().sendNormalServerMessage("Okay, you have set " + receiver + " as your referrer.");
                }
                catch (NumberFormatException nfe) {
                    player.getCommunicator().sendNormalServerMessage(mess);
                }
            }
            catch (RemoteException rx) {
                player.getCommunicator().sendAlertServerMessage(AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER);
            }
        }
    }

    public void acceptReferrer(Creature player, String referrerName, boolean money) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return;
            }
        }
        if (this.wurm != null) {
            try {
                player.getCommunicator().sendNormalServerMessage(this.wurm.acceptReferrer(this.intraServerPassword, player.getWurmId(), referrerName, money));
            }
            catch (RemoteException rx) {
                player.getCommunicator().sendAlertServerMessage(AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER);
            }
        }
    }

    public String getReimburseInfo(Player player) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getReimbursementInfo(this.intraServerPassword, player.getSaveFile().emailAddress);
            }
            catch (RemoteException rx) {
                return AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER;
            }
        }
        return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
    }

    public long[] getCurrentServer(String name, long wurmid) throws Exception {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                throw new WurmServerException(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getCurrentServerAndWurmid(this.intraServerPassword, name, wurmid);
            }
            catch (RemoteException rx) {
                throw new WurmServerException(AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER, rx);
            }
        }
        throw new WurmServerException(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
    }

    public Map<Long, byte[]> getPlayerStates(long[] wurmids) throws WurmServerException {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                throw new WurmServerException(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getPlayerStates(this.intraServerPassword, wurmids);
            }
            catch (RemoteException rx) {
                throw new WurmServerException(AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER, rx);
            }
        }
        throw new WurmServerException(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
    }

    public void manageFeature(int aServerId, int featureId, boolean aOverridden, boolean aEnabled, boolean global) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
            }
        }
        if (this.wurm != null) {
            try {
                this.wurm.manageFeature(this.intraServerPassword, aServerId, featureId, aOverridden, aEnabled, global);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + rx.getMessage());
            }
        }
    }

    public void startShutdown(String instigator, int seconds, String reason) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
            }
        }
        if (this.wurm != null) {
            try {
                this.wurm.startShutdown(this.intraServerPassword, instigator, seconds, reason);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + rx.getMessage());
            }
        }
    }

    public String withDraw(Player player, String name, String _email, int _months, int _silvers, boolean titlebok, boolean mbok, int _daysLeft) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
            }
        }
        if (this.wurm != null) {
            try {
                if (this.wurm.withDraw(this.intraServerPassword, player.getName(), name, _email, _months, _silvers, titlebok, _daysLeft)) {
                    if (titlebok) {
                        try {
                            Item bok = ItemFactory.createItem(443, 99.0f, player.getName());
                            if (mbok) {
                                bok.setName("Master bag of keeping");
                                bok.setSizes(3, 10, 20);
                            }
                            player.getInventory().insertItem(bok, true);
                            player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + bok.getNameWithGenus() + '!');
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, player.getName() + ", failed to create bok! " + fe.getMessage(), fe);
                        }
                        catch (NoSuchTemplateException nsi) {
                            logger.log(Level.WARNING, player.getName() + ", failed to create bok! " + nsi.getMessage(), nsi);
                        }
                        player.addTitle(Titles.Title.Ageless);
                        if (mbok) {
                            player.addTitle(Titles.Title.KeeperTruth);
                        }
                    }
                    if (_months > 0) {
                        try {
                            Item spyglass = ItemFactory.createItem(489, 80.0f + (float)Server.rand.nextInt(20), player.getName());
                            player.getInventory().insertItem(spyglass, true);
                            player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + spyglass.getNameWithGenus() + '!');
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + fe.getMessage(), fe);
                        }
                        catch (NoSuchTemplateException nsi) {
                            logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + nsi.getMessage(), nsi);
                        }
                        Item trinket = null;
                        if (_months > 1) {
                            try {
                                trinket = ItemFactory.createItem(509, 80.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                            }
                            catch (FailedException fe) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + fe.getMessage(), fe);
                            }
                            catch (NoSuchTemplateException nsi) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + nsi.getMessage(), nsi);
                            }
                            try {
                                trinket = ItemFactory.createItem(93, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(79, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(20, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(313, 40.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(8, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(90, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                            }
                            catch (FailedException fe) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + fe.getMessage(), fe);
                            }
                            catch (NoSuchTemplateException nsi) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + nsi.getMessage(), nsi);
                            }
                        }
                        if (_months > 2) {
                            try {
                                trinket = ItemFactory.createItem(105, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(105, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(107, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(103, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(103, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(108, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(104, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(106, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(106, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(4, 30.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                            }
                            catch (FailedException fe) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + fe.getMessage(), fe);
                            }
                            catch (NoSuchTemplateException nsi) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + nsi.getMessage(), nsi);
                            }
                        }
                        if (_months > 3) {
                            try {
                                trinket = ItemFactory.createItem(135, 50.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                trinket = ItemFactory.createItem(480, 70.0f, player.getName());
                                player.getInventory().insertItem(trinket, true);
                                player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                            }
                            catch (FailedException fe) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + fe.getMessage(), fe);
                            }
                            catch (NoSuchTemplateException nsi) {
                                logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + nsi.getMessage(), nsi);
                            }
                        }
                        if (_months > 4) {
                            for (int x = 0; x < 3; ++x) {
                                try {
                                    trinket = ItemFactory.createItem(509, 80.0f, player.getName());
                                    player.getInventory().insertItem(trinket, true);
                                    player.getCommunicator().sendSafeServerMessage(YOU_RECEIVED + trinket.getNameWithGenus() + '!');
                                    continue;
                                }
                                catch (FailedException fe) {
                                    logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + fe.getMessage(), fe);
                                    continue;
                                }
                                catch (NoSuchTemplateException nsi) {
                                    logger.log(Level.WARNING, player.getName() + FAILED_TO_CREATE_TRINKET + nsi.getMessage(), nsi);
                                }
                            }
                        }
                    }
                    return "You have been reimbursed.";
                }
                return "There was an error with your request. The server may be unavailable. You may also want to verify the amounts entered.";
            }
            catch (RemoteException rx) {
                return AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER;
            }
        }
        return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
    }

    public boolean transferPlayer(Player player, String playerName, int posx, int posy, boolean surfaced, byte[] data) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + "," + ex.getMessage());
                if (player != null) {
                    player.getCommunicator().sendAlertServerMessage(FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER);
                }
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                if (!this.wurm.transferPlayer(this.intraServerPassword, playerName, posx, posy, surfaced, player.getPower(), data)) {
                    if (player != null) {
                        player.getCommunicator().sendAlertServerMessage("An error was reported from the login server. Please try later or report this using /support if the problem persists.");
                    }
                    return false;
                }
                return true;
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to transfer " + playerName + " to the login server " + rx.getMessage());
                if (player != null) {
                    player.getCommunicator().sendAlertServerMessage(AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER);
                }
                return false;
            }
        }
        return false;
    }

    public boolean changePassword(long wurmId, String newPassword) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage() + " server=" + this.serverId);
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.changePassword(this.intraServerPassword, wurmId, newPassword);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to change password for  " + wurmId + "." + rx.getMessage());
                return false;
            }
        }
        return false;
    }

    public int[] getPremTimeSilvers(long wurmId) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                return failedIntZero;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getPremTimeSilvers(this.intraServerPassword, wurmId);
            }
            catch (RemoteException remoteException) {
                // empty catch block
            }
        }
        return failedIntZero;
    }

    public boolean setCurrentServer(String name, int currentServer) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                if (this.wurm.setCurrentServer(this.intraServerPassword, name, currentServer)) {
                    return true;
                }
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "failed to set current server of " + name + " to " + currentServer + ", " + rx.getMessage());
                return false;
            }
        }
        return false;
    }

    public String renamePlayer(String oldName, String newName, String newPass, int power) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, FAILED_TO_CONTACT_THE_LOGIN_SERVER + ex.getMessage() + "" + this.serverId);
                return "Failed to contact server. Try later. This is an Error.";
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.rename(this.intraServerPassword, oldName, newName, newPass, power);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to change name of " + oldName + ", " + rx.getMessage());
                return "Failed to contact server. Try later. This is an Error.";
            }
        }
        return "";
    }

    public String changePassword(String changerName, String name, String newPass, int power) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.changePassword(this.intraServerPassword, changerName, name, newPass, power);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, changerName + " failed to change password of " + name + ", " + rx.getMessage());
                return rx.getMessage();
            }
        }
        return "";
    }

    public String ascend(int newDeityId, String deityName, long wurmid, byte existingDeity, byte gender, byte newPower, float initialBStr, float initialBSta, float initialBCon, float initialML, float initialMS, float initialSS, float initialSD) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, FAILED_TO_CONTACT_THE_LOGIN_SERVER + ex.getMessage() + " " + this.serverId);
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.ascend(this.intraServerPassword, newDeityId, deityName, wurmid, existingDeity, gender, newPower, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, wurmid + " failed to create deity " + deityName + ", " + rx.getMessage());
                return rx.getMessage();
            }
        }
        return "";
    }

    public String changeEmail(String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.changeEmail(this.intraServerPassword, changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, changerName + " failed to change email of " + name + ", " + rx.getMessage());
                return rx.getMessage();
            }
        }
        return "";
    }

    public String addReimb(String changerName, String name, int numMonths, int _silver, int _daysLeft, boolean setbok) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.addReimb(this.intraServerPassword, changerName, name, numMonths, _silver, _daysLeft, setbok);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, changerName + " failed to add reimb of " + name + ", " + rx.getMessage());
                return rx.getMessage();
            }
        }
        return "";
    }

    public String sendMail(byte[] maildata, byte[] items, long sender, long wurmid, int targetServer) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.sendMail(this.intraServerPassword, maildata, items, sender, wurmid, targetServer);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to send mail " + rx.getMessage());
                return rx.getMessage();
            }
        }
        return "";
    }

    public String ban(String name, String reason, int days) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.ban(this.intraServerPassword, name, reason, days);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to ban " + name + ':' + rx.getMessage());
                return "Failed to ban " + name + ':' + rx.getMessage();
            }
        }
        return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
    }

    public String addBannedIp(String ip, String reason, int days) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.addBannedIp(this.intraServerPassword, ip, reason, days);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to ban " + ip + ':' + rx.getMessage());
                return "Failed to ban " + ip + ':' + rx.getMessage();
            }
        }
        return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
    }

    public Ban[] getPlayersBanned() throws Exception {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                throw new WurmServerException("Failed to contact the login server:" + ex.getMessage());
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getPlayersBanned(this.intraServerPassword);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to retrieve banned players :" + rx.getMessage());
                throw new WurmServerException("Failed to retrieve banned players :" + rx.getMessage());
            }
        }
        return null;
    }

    public Ban[] getIpsBanned() throws Exception {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                throw new WurmServerException("Failed to contact the login server:" + ex.getMessage());
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.getIpsBanned(this.intraServerPassword);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to retrieve banned ips :" + rx.getMessage());
                throw new WurmServerException("Failed to retrieve banned ips :" + rx.getMessage());
            }
        }
        return null;
    }

    public String pardonban(String name) throws RemoteException {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.pardonban(this.intraServerPassword, name);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to pardon " + name + ':' + rx.getMessage());
                return "Failed to pardon " + name + ':' + rx.getMessage();
            }
        }
        return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
    }

    public String removeBannedIp(String ip) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.removeBannedIp(this.intraServerPassword, ip);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to ban " + ip + ':' + rx.getMessage());
                return "Failed to ban " + ip + ':' + rx.getMessage();
            }
        }
        return FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER;
    }

    public Map<String, String> doesPlayerExist(String playerName) {
        HashMap<String, String> toReturn = new HashMap<String, String>();
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                toReturn.put("ResponseCode", "NOTOK");
                toReturn.put("ErrorMessage", GAME_SERVER_IS_CURRENTLY_UNAVAILABLE);
                toReturn.put("display_text", GAME_SERVER_IS_CURRENTLY_UNAVAILABLE);
                return toReturn;
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.doesPlayerExist(this.intraServerPassword, playerName);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to contact server.");
                toReturn.put("ResponseCode", "NOTOK");
                toReturn.put("ErrorMessage", GAME_SERVER_IS_CURRENTLY_UNAVAILABLE);
                toReturn.put("display_text", GAME_SERVER_IS_CURRENTLY_UNAVAILABLE);
            }
        } else {
            toReturn.put("ResponseCode", "NOTOK");
            toReturn.put("ErrorMessage", GAME_SERVER_IS_CURRENTLY_UNAVAILABLE);
            toReturn.put("display_text", GAME_SERVER_IS_CURRENTLY_UNAVAILABLE);
        }
        return toReturn;
    }

    public String sendVehicle(byte[] passengerdata, byte[] itemdata, long pilot, long vehicleId, int targetServer, int tilex, int tiley, int layer, float rotation) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return ex.getMessage();
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.sendVehicle(this.intraServerPassword, passengerdata, itemdata, pilot, vehicleId, targetServer, tilex, tiley, layer, rotation);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to send vehicle " + rx.getMessage());
                return rx.getMessage();
            }
            catch (Exception ex) {
                return ex.getMessage();
            }
        }
        return "";
    }

    public void sendWebCommand(short type, WebCommand command) {
        new Thread(){

            @Override
            public void run() {
                boolean ok = false;
                if (LoginServerWebConnection.this.wurm == null) {
                    try {
                        LoginServerWebConnection.this.connect();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (LoginServerWebConnection.this.wurm != null) {
                    try {
                        LoginServerWebConnection.this.wurm.genericWebCommand(LoginServerWebConnection.this.intraServerPassword, type, command.getWurmId(), command.getData());
                        ok = true;
                    }
                    catch (RemoteException rx) {
                        logger.log(Level.WARNING, "Failed to send command " + rx.getMessage());
                    }
                }
                if (!ok && command.getType() == 11 && Servers.localServer.LOGINSERVER) {
                    try {
                        EpicEntity entity = Server.getEpicMap().getEntity(((WcCreateEpicMission)command).entityNumber);
                        if (entity != null) {
                            entity.addFailedServer(LoginServerWebConnection.this.serverId);
                        }
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            }
        }.start();
    }

    public void setKingdomInfo(byte kingdomId, byte templateKingdom, String _name, String _password, String _chatName, String _suffix, String mottoOne, String mottoTwo, boolean acceptsPortals) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.wurm != null) {
            try {
                this.wurm.setKingdomInfo(this.intraServerPassword, Servers.localServer.id, kingdomId, templateKingdom, _name, _password, _chatName, _suffix, mottoOne, mottoTwo, acceptsPortals);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to send command " + rx.getMessage());
            }
        }
    }

    public boolean kingdomExists(int thisServerId, byte kingdomId, boolean exists) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.kingdomExists(this.intraServerPassword, thisServerId, kingdomId, exists);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to send command " + rx.getMessage());
            }
        }
        return true;
    }

    public void requestDemigod(byte existingDeity, String deityName) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.wurm != null) {
            try {
                this.wurm.requestDemigod(this.intraServerPassword, existingDeity, deityName);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to send command " + rx.getMessage());
            }
        }
    }

    public boolean requestDeityMove(int deityNum, int desiredHex, String guide) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.requestDeityMove(this.intraServerPassword, deityNum, desiredHex, guide);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "Failed to send command " + rx.getMessage());
            }
        }
        return false;
    }

    public boolean awardPlayer(long wurmid, String name, int days, int months) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
                return false;
            }
        }
        if (this.wurm != null) {
            try {
                this.wurm.awardPlayer(this.intraServerPassword, wurmid, name, days, months);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "failed to set award " + wurmid + " (" + name + ") " + months + " months, " + days + " days, " + rx.getMessage());
                return false;
            }
        }
        return false;
    }

    public boolean isFeatureEnabled(int featureId) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.isFeatureEnabled(this.intraServerPassword, featureId);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + rx.getMessage());
            }
        }
        return false;
    }

    public boolean setPlayerFlag(long wurmid, int flag, boolean set) {
        if (this.wurm == null) {
            try {
                this.connect();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + ex.getMessage());
            }
        }
        if (this.wurm != null) {
            try {
                return this.wurm.setPlayerFlag(Servers.localServer.INTRASERVERPASSWORD, wurmid, flag, set);
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + rx.getMessage());
            }
        }
        return false;
    }
}


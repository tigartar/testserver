/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public final class MessageServer
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(MessageServer.class.getName());
    private static final Logger chatlogger = Logger.getLogger("Chat");
    private static final Queue<Message> MESSAGES = new LinkedBlockingDeque<Message>();

    private MessageServer() {
    }

    static synchronized void initialise() {
        logger.info("Initialising MessageServer");
    }

    public static void addMessage(Message message) {
        MESSAGES.add(message);
    }

    static void sendMessages() {
        for (Message message : MESSAGES) {
            Player p2;
            int n;
            int x;
            Player[] playarr;
            long senderid = message.getSenderId();
            if (senderid < 0L && message.getSender() != null) {
                senderid = message.getSender().getWurmId();
            }
            if (message.getType() == 9) {
                chatlogger.log(Level.INFO, "PR-" + message.getMessage());
                playarr = Players.getInstance().getPlayers();
                for (int x2 = 0; x2 < playarr.length; ++x2) {
                    if (!playarr[x2].mayHearMgmtTalk() || playarr[x2].isIgnored(senderid)) continue;
                    playarr[x2].getCommunicator().sendMessage(message);
                }
                continue;
            }
            if (message.getType() == 10) {
                chatlogger.log(Level.INFO, "SH-" + message.getMessage());
                playarr = Players.getInstance().getPlayers();
                byte kingdom = message.getSender().getKingdomId();
                for (x = 0; x < playarr.length; ++x) {
                    if (!playarr[x].isKingdomChat() || playarr[x].isIgnored(senderid) || playarr[x].getPower() <= 0 && kingdom != playarr[x].getKingdomId() || playarr[x].getCommunicator().isInvulnerable()) continue;
                    playarr[x].getCommunicator().sendMessage(message);
                }
                continue;
            }
            if (message.getType() == 16) {
                chatlogger.log(Level.INFO, "KSH-" + message.getMessage());
                playarr = Players.getInstance().getPlayers();
                byte kingdom = message.getSenderKingdom();
                for (x = 0; x < playarr.length; ++x) {
                    if (!playarr[x].isGlobalChat() || (playarr[x].isIgnored(senderid) || kingdom != playarr[x].getKingdomId()) && playarr[x].getPower() <= 0 || playarr[x].getCommunicator().isInvulnerable()) continue;
                    playarr[x].getCommunicator().sendMessage(message);
                }
                continue;
            }
            if (message.getType() == 5) {
                chatlogger.log(Level.INFO, "BR-" + message.getMessage());
                Server.getInstance().twitLocalServer(message.getMessage());
                Player[] kingdom = playarr = Players.getInstance().getPlayers();
                x = kingdom.length;
                for (n = 0; n < x; ++n) {
                    Player lElement = kingdom[n];
                    lElement.getCommunicator().sendMessage(message);
                }
                continue;
            }
            if (message.getType() == 1) {
                chatlogger.log(Level.INFO, "ANN-" + message.getMessage());
                Player[] kingdom = playarr = Players.getInstance().getPlayers();
                x = kingdom.length;
                for (n = 0; n < x; ++n) {
                    Player lElement = kingdom[n];
                    lElement.getCommunicator().sendMessage(message);
                }
                continue;
            }
            if (message.getType() == 11) {
                chatlogger.log(Level.INFO, "GM-" + message.getMessage());
                Player[] kingdom = playarr = Players.getInstance().getPlayers();
                x = kingdom.length;
                for (n = 0; n < x; ++n) {
                    Player lElement = kingdom[n];
                    if (!lElement.mayHearDevTalk()) continue;
                    lElement.getCommunicator().sendMessage(message);
                }
                continue;
            }
            if (message.getType() == 3) {
                try {
                    p2 = Players.getInstance().getPlayer(message.getReceiver());
                    p2.getCommunicator().sendMessage(message);
                }
                catch (NoSuchPlayerException p2) {}
                continue;
            }
            if (message.getType() == 17) {
                try {
                    p2 = Players.getInstance().getPlayer(message.getReceiver());
                    p2.getCommunicator().sendNormalServerMessage(message.getMessage());
                }
                catch (NoSuchPlayerException p3) {}
                continue;
            }
            if (message.getType() != 18) continue;
            chatlogger.log(Level.INFO, "TD-" + message.getMessage());
            playarr = Players.getInstance().getPlayers();
            byte kingdom = message.getSenderKingdom();
            int red = message.getRed();
            int blue = message.getBlue();
            int green = message.getGreen();
            for (int x3 = 0; x3 < playarr.length; ++x3) {
                Matcher matcher;
                Pattern pattern;
                String patternString;
                if (!playarr[x3].isTradeChannel() || (playarr[x3].isIgnored(senderid) || kingdom != playarr[x3].getKingdomId()) && playarr[x3].getPower() <= 0) continue;
                boolean tell = true;
                if (message.getMessage().contains(") @")) {
                    patternString = "@" + playarr[x3].getName().toLowerCase() + "\\b";
                    pattern = Pattern.compile(patternString);
                    matcher = pattern.matcher(message.getMessage().toLowerCase());
                    tell = matcher.find();
                }
                if ((matcher = (pattern = Pattern.compile(patternString = "\\b" + playarr[x3].getName().toLowerCase() + "\\b")).matcher(message.getMessage().toLowerCase())).find()) {
                    message.setColorR(100);
                    message.setColorG(170);
                    message.setColorB(255);
                } else {
                    message.setColorR(red);
                    message.setColorG(green);
                    message.setColorB(blue);
                }
                if (playarr[x3].getCommunicator().isInvulnerable() || !tell) continue;
                playarr[x3].getCommunicator().sendMessage(message);
            }
        }
        MESSAGES.clear();
    }

    public static void broadCastNormal(String message) {
        Player[] playarr;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Broadcasting Serverwide Normal message: " + message);
        }
        for (Player lElement : playarr = Players.getInstance().getPlayers()) {
            lElement.getCommunicator().sendNormalServerMessage(message);
        }
    }

    public static void broadCastSafe(String message, byte messageType) {
        Player[] playarr;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Broadcasting Serverwide Safe message: " + message);
        }
        for (Player lElement : playarr = Players.getInstance().getPlayers()) {
            lElement.getCommunicator().sendSafeServerMessage(message, messageType);
        }
    }

    public static void broadCastAlert(String message, byte messageType) {
        Player[] playarr;
        logger.info("Broadcasting Serverwide Alert: " + message);
        for (Player lPlayer : playarr = Players.getInstance().getPlayers()) {
            lPlayer.getCommunicator().sendAlertServerMessage(message, messageType);
            if (!lPlayer.isFighting()) continue;
            lPlayer.getCommunicator().sendCombatAlertMessage(message);
        }
    }

    public static void broadCastAction(String message, Creature performer, int tileDist) {
        MessageServer.broadCastAction(message, performer, null, tileDist, false);
    }

    public static void broadCastAction(String message, Creature performer, Creature receiver, int tileDist) {
        MessageServer.broadCastAction(message, performer, receiver, tileDist, false);
    }

    public static void broadCastAction(String message, Creature performer, @Nullable Creature receiver, int tileDist, boolean combat) {
        if (message.length() > 0) {
            int lTileDist = Math.abs(tileDist);
            int tilex = performer.getTileX();
            int tiley = performer.getTileY();
            for (int x = tilex - lTileDist; x <= tilex + lTileDist; ++x) {
                for (int y = tiley - lTileDist; y <= tiley + lTileDist; ++y) {
                    try {
                        Zone zone = Zones.getZone(x, y, performer.isOnSurface());
                        VolaTile tile = zone.getTileOrNull(x, y);
                        if (tile == null) continue;
                        tile.broadCastAction(message, performer, receiver, combat);
                        continue;
                    }
                    catch (NoSuchZoneException noSuchZoneException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public static void broadcastColoredAction(List<MulticolorLineSegment> segments, Creature performer, int tileDist, boolean combat) {
        MessageServer.broadcastColoredAction(segments, performer, null, tileDist, combat, (byte)0);
    }

    public static void broadcastColoredAction(List<MulticolorLineSegment> segments, Creature performer, @Nullable Creature receiver, int tileDist, boolean combat) {
        MessageServer.broadcastColoredAction(segments, performer, receiver, tileDist, combat, (byte)0);
    }

    public static void broadcastColoredAction(List<MulticolorLineSegment> segments, Creature performer, @Nullable Creature receiver, int tileDist, boolean combat, byte onScreenMessage) {
        if (segments == null || segments.isEmpty()) {
            return;
        }
        int lTileDist = Math.abs(tileDist);
        int tilex = performer.getTileX();
        int tiley = performer.getTileY();
        for (int x = tilex - lTileDist; x <= tilex + lTileDist; ++x) {
            for (int y = tiley - lTileDist; y <= tiley + lTileDist; ++y) {
                try {
                    Zone zone = Zones.getZone(x, y, performer.isOnSurface());
                    VolaTile tile = zone.getTileOrNull(x, y);
                    if (tile == null) continue;
                    tile.broadCastMulticolored(segments, performer, receiver, combat, onScreenMessage);
                    continue;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
        }
    }

    public static void broadCastMessage(String message, int tilex, int tiley, boolean surfaced, int tiledistance) {
        if (message.length() > 0) {
            for (int x = tilex - tiledistance; x <= tilex + tiledistance; ++x) {
                for (int y = tiley - tiledistance; y <= tiley + tiledistance; ++y) {
                    try {
                        Zone zone = Zones.getZone(x, y, surfaced);
                        VolaTile tile = zone.getTileOrNull(x, y);
                        if (tile == null) continue;
                        tile.broadCast(message);
                        continue;
                    }
                    catch (NoSuchZoneException noSuchZoneException) {
                        // empty catch block
                    }
                }
            }
        }
    }
}


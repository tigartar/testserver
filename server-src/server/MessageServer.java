package com.wurmonline.server;

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

public final class MessageServer implements MiscConstants {
   private static final Logger logger = Logger.getLogger(MessageServer.class.getName());
   private static final Logger chatlogger = Logger.getLogger("Chat");
   private static final Queue<Message> MESSAGES = new LinkedBlockingDeque<>();

   private MessageServer() {
   }

   static synchronized void initialise() {
      logger.info("Initialising MessageServer");
   }

   public static void addMessage(Message message) {
      MESSAGES.add(message);
   }

   static void sendMessages() {
      for(Message message : MESSAGES) {
         long senderid = message.getSenderId();
         if (senderid < 0L && message.getSender() != null) {
            senderid = message.getSender().getWurmId();
         }

         if (message.getType() == 9) {
            chatlogger.log(Level.INFO, "PR-" + message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();

            for(int x = 0; x < playarr.length; ++x) {
               if (playarr[x].mayHearMgmtTalk() && !playarr[x].isIgnored(senderid)) {
                  playarr[x].getCommunicator().sendMessage(message);
               }
            }
         } else if (message.getType() == 10) {
            chatlogger.log(Level.INFO, "SH-" + message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();
            byte kingdom = message.getSender().getKingdomId();

            for(int x = 0; x < playarr.length; ++x) {
               if (playarr[x].isKingdomChat()
                  && !playarr[x].isIgnored(senderid)
                  && (playarr[x].getPower() > 0 || kingdom == playarr[x].getKingdomId())
                  && !playarr[x].getCommunicator().isInvulnerable()) {
                  playarr[x].getCommunicator().sendMessage(message);
               }
            }
         } else if (message.getType() == 16) {
            chatlogger.log(Level.INFO, "KSH-" + message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();
            byte kingdom = message.getSenderKingdom();

            for(int x = 0; x < playarr.length; ++x) {
               if (playarr[x].isGlobalChat()
                  && (!playarr[x].isIgnored(senderid) && kingdom == playarr[x].getKingdomId() || playarr[x].getPower() > 0)
                  && !playarr[x].getCommunicator().isInvulnerable()) {
                  playarr[x].getCommunicator().sendMessage(message);
               }
            }
         } else if (message.getType() == 5) {
            chatlogger.log(Level.INFO, "BR-" + message.getMessage());
            Server.getInstance().twitLocalServer(message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();

            for(Player lElement : playarr) {
               lElement.getCommunicator().sendMessage(message);
            }
         } else if (message.getType() == 1) {
            chatlogger.log(Level.INFO, "ANN-" + message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();

            for(Player lElement : playarr) {
               lElement.getCommunicator().sendMessage(message);
            }
         } else if (message.getType() == 11) {
            chatlogger.log(Level.INFO, "GM-" + message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();

            for(Player lElement : playarr) {
               if (lElement.mayHearDevTalk()) {
                  lElement.getCommunicator().sendMessage(message);
               }
            }
         } else if (message.getType() == 3) {
            try {
               Player p = Players.getInstance().getPlayer(message.getReceiver());
               p.getCommunicator().sendMessage(message);
            } catch (NoSuchPlayerException var15) {
            }
         } else if (message.getType() == 17) {
            try {
               Player p = Players.getInstance().getPlayer(message.getReceiver());
               p.getCommunicator().sendNormalServerMessage(message.getMessage());
            } catch (NoSuchPlayerException var14) {
            }
         } else if (message.getType() == 18) {
            chatlogger.log(Level.INFO, "TD-" + message.getMessage());
            Player[] playarr = Players.getInstance().getPlayers();
            byte kingdom = message.getSenderKingdom();
            int red = message.getRed();
            int blue = message.getBlue();
            int green = message.getGreen();

            for(int x = 0; x < playarr.length; ++x) {
               if (playarr[x].isTradeChannel() && (!playarr[x].isIgnored(senderid) && kingdom == playarr[x].getKingdomId() || playarr[x].getPower() > 0)) {
                  boolean tell = true;
                  if (message.getMessage().contains(") @")) {
                     String patternString = "@" + playarr[x].getName().toLowerCase() + "\\b";
                     Pattern pattern = Pattern.compile(patternString);
                     Matcher matcher = pattern.matcher(message.getMessage().toLowerCase());
                     tell = matcher.find();
                  }

                  String patternString = "\\b" + playarr[x].getName().toLowerCase() + "\\b";
                  Pattern pattern = Pattern.compile(patternString);
                  Matcher matcher = pattern.matcher(message.getMessage().toLowerCase());
                  if (matcher.find()) {
                     message.setColorR(100);
                     message.setColorG(170);
                     message.setColorB(255);
                  } else {
                     message.setColorR(red);
                     message.setColorG(green);
                     message.setColorB(blue);
                  }

                  if (!playarr[x].getCommunicator().isInvulnerable() && tell) {
                     playarr[x].getCommunicator().sendMessage(message);
                  }
               }
            }
         }
      }

      MESSAGES.clear();
   }

   public static void broadCastNormal(String message) {
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("Broadcasting Serverwide Normal message: " + message);
      }

      Player[] playarr = Players.getInstance().getPlayers();

      for(Player lElement : playarr) {
         lElement.getCommunicator().sendNormalServerMessage(message);
      }
   }

   public static void broadCastSafe(String message, byte messageType) {
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("Broadcasting Serverwide Safe message: " + message);
      }

      Player[] playarr = Players.getInstance().getPlayers();

      for(Player lElement : playarr) {
         lElement.getCommunicator().sendSafeServerMessage(message, messageType);
      }
   }

   public static void broadCastAlert(String message, byte messageType) {
      logger.info("Broadcasting Serverwide Alert: " + message);
      Player[] playarr = Players.getInstance().getPlayers();

      for(Player lPlayer : playarr) {
         lPlayer.getCommunicator().sendAlertServerMessage(message, messageType);
         if (lPlayer.isFighting()) {
            lPlayer.getCommunicator().sendCombatAlertMessage(message);
         }
      }
   }

   public static void broadCastAction(String message, Creature performer, int tileDist) {
      broadCastAction(message, performer, null, tileDist, false);
   }

   public static void broadCastAction(String message, Creature performer, Creature receiver, int tileDist) {
      broadCastAction(message, performer, receiver, tileDist, false);
   }

   public static void broadCastAction(String message, Creature performer, @Nullable Creature receiver, int tileDist, boolean combat) {
      if (message.length() > 0) {
         int lTileDist = Math.abs(tileDist);
         int tilex = performer.getTileX();
         int tiley = performer.getTileY();

         for(int x = tilex - lTileDist; x <= tilex + lTileDist; ++x) {
            for(int y = tiley - lTileDist; y <= tiley + lTileDist; ++y) {
               try {
                  Zone zone = Zones.getZone(x, y, performer.isOnSurface());
                  VolaTile tile = zone.getTileOrNull(x, y);
                  if (tile != null) {
                     tile.broadCastAction(message, performer, receiver, combat);
                  }
               } catch (NoSuchZoneException var12) {
               }
            }
         }
      }
   }

   public static void broadcastColoredAction(List<MulticolorLineSegment> segments, Creature performer, int tileDist, boolean combat) {
      broadcastColoredAction(segments, performer, null, tileDist, combat, (byte)0);
   }

   public static void broadcastColoredAction(
      List<MulticolorLineSegment> segments, Creature performer, @Nullable Creature receiver, int tileDist, boolean combat
   ) {
      broadcastColoredAction(segments, performer, receiver, tileDist, combat, (byte)0);
   }

   public static void broadcastColoredAction(
      List<MulticolorLineSegment> segments, Creature performer, @Nullable Creature receiver, int tileDist, boolean combat, byte onScreenMessage
   ) {
      if (segments != null && !segments.isEmpty()) {
         int lTileDist = Math.abs(tileDist);
         int tilex = performer.getTileX();
         int tiley = performer.getTileY();

         for(int x = tilex - lTileDist; x <= tilex + lTileDist; ++x) {
            for(int y = tiley - lTileDist; y <= tiley + lTileDist; ++y) {
               try {
                  Zone zone = Zones.getZone(x, y, performer.isOnSurface());
                  VolaTile tile = zone.getTileOrNull(x, y);
                  if (tile != null) {
                     tile.broadCastMulticolored(segments, performer, receiver, combat, onScreenMessage);
                  }
               } catch (NoSuchZoneException var13) {
               }
            }
         }
      }
   }

   public static void broadCastMessage(String message, int tilex, int tiley, boolean surfaced, int tiledistance) {
      if (message.length() > 0) {
         for(int x = tilex - tiledistance; x <= tilex + tiledistance; ++x) {
            for(int y = tiley - tiledistance; y <= tiley + tiledistance; ++y) {
               try {
                  Zone zone = Zones.getZone(x, y, surfaced);
                  VolaTile tile = zone.getTileOrNull(x, y);
                  if (tile != null) {
                     tile.broadCast(message);
                  }
               } catch (NoSuchZoneException var9) {
               }
            }
         }
      }
   }
}

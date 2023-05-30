package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectSpawnQuestion extends Question {
   private static final Logger logger = Logger.getLogger(SelectSpawnQuestion.class.getName());
   final String welcomeMess;
   boolean unDead = false;
   private final LinkedList<Kingdom> availKingdoms = new LinkedList<>();

   public SelectSpawnQuestion(Player aResponder, String aTitle, String aQuestion, long aTarget, String message, boolean Undead) {
      super(aResponder, aTitle, aQuestion, 134, aTarget);
      this.welcomeMess = message;
      this.unDead = Undead;
   }

   @Override
   public void answer(Properties answers) {
      try {
         Player player = (Player)this.getResponder();
         byte kingdom = 4;
         if (Servers.localServer.HOMESERVER) {
            kingdom = Servers.localServer.KINGDOM;
         } else {
            try {
               String did = answers.getProperty("kingdomid");
               int index = Integer.parseInt(did);
               Kingdom k = this.getAvailKingdoms().get(index);
               kingdom = k == null ? 0 : k.getId();
            } catch (Exception var11) {
               logger.log(Level.INFO, var11.getMessage(), (Throwable)var11);
            }
         }

         boolean male = true;

         try {
            male = Boolean.parseBoolean(answers.getProperty("male"));
         } catch (Exception var10) {
            logger.log(Level.INFO, var10.getMessage(), (Throwable)var10);
         }

         if (!male) {
            player.setSex((byte)1, false);
         }

         player.setKingdomId(kingdom, true);
         player.setBlood(IntraServerConnection.calculateBloodFromKingdom(kingdom));
         float posX = (float)(Servers.localServer.SPAWNPOINTJENNX * 4 + Server.rand.nextInt(10));
         float posY = (float)(Servers.localServer.SPAWNPOINTJENNY * 4 + Server.rand.nextInt(10));
         int r = Server.rand.nextInt(3);
         float rot = (float)Server.rand.nextInt(360);
         if (this.unDead) {
            kingdom = 0;
            float[] txty = Player.findRandomSpawnX(false, false);
            posX = txty[0];
            posY = txty[1];
         } else {
            if (Servers.localServer.KINGDOM != 0) {
               kingdom = Servers.localServer.KINGDOM;
            } else if (r == 1) {
               posX = (float)(Servers.localServer.SPAWNPOINTMOLX * 4 + Server.rand.nextInt(10));
               posY = (float)(Servers.localServer.SPAWNPOINTMOLY * 4 + Server.rand.nextInt(10));
            } else if (r == 2) {
               posX = (float)(Servers.localServer.SPAWNPOINTLIBX * 4 + Server.rand.nextInt(10));
               posY = (float)(Servers.localServer.SPAWNPOINTLIBY * 4 + Server.rand.nextInt(10));
            }

            if (Servers.localServer.randomSpawns) {
               float[] txty = Player.findRandomSpawnX(true, true);
               posX = txty[0];
               posY = txty[1];
            }
         }

         Spawnpoint sp = LoginHandler.getInitialSpawnPoint(kingdom);
         if (sp != null) {
            posX = (float)(sp.tilex * 4 + Server.rand.nextInt(10));
            posY = (float)(sp.tiley * 4 + Server.rand.nextInt(10));
         }

         player.setPositionX(posX);
         player.setPositionY(posY);
         player.setRotation(rot);
         LoginHandler.putOutsideWall(player);
         if (player.isOnSurface()) {
            LoginHandler.putOutsideHouse(player, false);
            LoginHandler.putOutsideFence(player);
         }

         player.setTeleportPoints(posX, posY, 0, 0);
         player.startTeleporting();
         Players.getInstance().sendConnectInfo(player, " has logged in.", player.getLastLogin(), PlayerOnlineStatus.ONLINE, true);
         Players.getInstance().addToGroups(player);
         Server.getInstance().startSendingFinals(player);
         player.getCommunicator().sendMapInfo();
         Achievements.sendAchievementList(player);
         Players.loadAllPrivatePOIForPlayer(player);
         player.sendAllMapAnnotations();
         ValreiMapData.sendAllMapData(player);
         player.resetLastSentToolbelt();
      } catch (IOException var12) {
         var12.printStackTrace();
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("text{text=\"\"}text{text=\"\"}");
      boolean selected = Server.rand.nextBoolean();
      buf.append("radio{ group='male'; id='false';text='Female';selected='" + !selected + "'}");
      buf.append("radio{ group='male'; id='true';text='Male';selected='" + selected + "'}");
      if (!Servers.localServer.HOMESERVER) {
         buf.append("text{text=\"\"}text{text=\"\"}");
         buf.append("text{text=\"Please select kingdom.\"}text{text=\"\"}");
         buf.append("harray{label{text='Kingdom: '};dropdown{id='kingdomid';options=\"");
         Kingdom[] kingdoms = Kingdoms.getAllKingdoms();

         for(int x = 0; x < kingdoms.length; ++x) {
            if (kingdoms[x].getId() != 0 && kingdoms[x].existsHere() && (!kingdoms[x].isCustomKingdom() || kingdoms[x].acceptsTransfers())) {
               this.availKingdoms.add(kingdoms[x]);
               buf.append(kingdoms[x].getName());
               buf.append(",");
            }
         }

         buf.append(",None\"}}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   List<Kingdom> getAvailKingdoms() {
      return this.availKingdoms;
   }
}

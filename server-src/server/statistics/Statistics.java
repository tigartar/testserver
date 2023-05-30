package com.wurmonline.server.statistics;

import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Questions;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public final class Statistics extends TimerTask {
   private static Logger log = null;
   private long totalBytesIn = 0L;
   private long totalBytesOut = 0L;
   private long currentBytesIn = 0L;
   private long currentBytesOut = 0L;
   private long playerCount = 0L;
   private static final long fivemin = 300000L;
   private static final long Onemeg = 1024000L;
   private static Statistics instance = null;
   private long creationTime = System.currentTimeMillis();

   private Statistics() {
   }

   public static Statistics getInstance() {
      if (instance == null) {
         instance = new Statistics();
      }

      return instance;
   }

   public void startup(Logger logger) {
      log = logger;
      Timer timer = new Timer();
      timer.scheduleAtFixedRate(this, 300000L, 300000L);
   }

   @Override
   public void run() {
      Runtime rt = Runtime.getRuntime();
      this.countBytes();
      log.info(
         "current mem in use: "
            + rt.totalMemory() / 1024000L
            + "M free mem: "
            + rt.freeMemory() / 1024000L
            + "M Max mem: "
            + rt.maxMemory() / 1024000L
            + "M\nplayer count: "
            + this.playerCount
            + "\nbytes in: "
            + this.currentBytesIn
            + " bytes out: "
            + this.currentBytesOut
            + " total in: "
            + this.totalBytesIn
            + " total out: "
            + this.totalBytesOut
            + '\n'
            + "Server uptime: "
            + (System.currentTimeMillis() - this.creationTime) / 1000L
            + " seconds. Unanswered questions:"
            + Questions.getNumUnanswered()
      );
   }

   private void countBytes() {
      long bytesIn = 0L;
      long bytesOut = 0L;
      Player[] players = Players.getInstance().getPlayers();
      this.playerCount = (long)players.length;

      for(int x = 0; x != players.length; ++x) {
         if (players[x].hasLink()) {
            bytesIn += (long)players[x].getCommunicator().getConnection().getReadBytes();
            bytesOut += (long)players[x].getCommunicator().getConnection().getSentBytes();
         }
      }

      this.currentBytesIn = bytesIn - this.totalBytesIn;
      this.currentBytesOut = bytesOut - this.totalBytesOut;
      if (this.currentBytesIn < 0L) {
         this.currentBytesIn = 0L;
      }

      if (this.currentBytesOut < 0L) {
         this.currentBytesOut = 0L;
      }

      this.totalBytesIn += this.currentBytesIn;
      this.totalBytesOut += this.currentBytesOut;
   }
}

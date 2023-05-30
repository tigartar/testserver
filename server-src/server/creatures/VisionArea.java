package com.wurmonline.server.creatures;

import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VisionArea {
   private static Logger logger = Logger.getLogger(VisionArea.class.getName());
   private Creature owner;
   private final int startPosX;
   private final int startPosY;
   private int xPos = 0;
   private int yPos = 0;
   private static final int playerViewSize = 150;
   private boolean initialized = false;
   private boolean sentCloseStrips = false;
   private boolean sentFarStrips = false;
   private boolean resumed = false;
   private int currentStrip = 0;
   private static final long divModifier = 16L;
   private VirtualZone surfaceViewField;
   private VirtualZone underGroundViewField;
   private boolean seesCaves = false;
   private int caveMoveCounter = 0;

   VisionArea(Creature aOwner, int size) {
      this.owner = aOwner;
      this.xPos = aOwner.getTileX();
      this.yPos = aOwner.getTileY();
      this.startPosX = this.xPos;
      this.startPosY = this.yPos;
      this.surfaceViewField = Zones.createZone(aOwner, this.xPos - size, this.yPos - size, this.xPos, this.yPos, size, true);
      if (aOwner.isPlayer()) {
         this.underGroundViewField = Zones.createZone(aOwner, this.xPos - 24, this.yPos - 24, this.xPos, this.yPos, 24, false);
      } else {
         this.underGroundViewField = Zones.createZone(aOwner, this.xPos - size, this.yPos - size, this.xPos, this.yPos, size, false);
      }

      if (this.owner.isPlayer()) {
         this.seesCaves = !this.owner.isOnSurface();
         if (!this.seesCaves) {
            this.seesCaves = this.underGroundViewField.shouldSeeCaves();
         }
      } else {
         this.surfaceViewField.initialize();
         this.underGroundViewField.initialize();
      }
   }

   public final boolean isNearCave() {
      return !this.owner.isOnSurface() ? true : this.seesCaves;
   }

   public VirtualZone getSurface() {
      return this.surfaceViewField;
   }

   public VirtualZone getUnderGround() {
      return this.underGroundViewField;
   }

   public void broadCastUpdateSelectBar(long toUpdate) {
      this.broadCastUpdateSelectBar(toUpdate, false);
   }

   public void broadCastUpdateSelectBar(long toUpdate, boolean keepSelection) {
      Zone[] zones = Zones.getZonesCoveredBy(this.getSurface());
      List<Creature> broadcastTargets = new ArrayList<>();
      if (zones != null) {
         for(int i = 0; i < zones.length; ++i) {
            List<Creature> pWatchers = zones[i].getPlayerWatchers();

            for(int j = 0; j < pWatchers.size(); ++j) {
               if (!broadcastTargets.contains(pWatchers.get(j))) {
                  broadcastTargets.add(pWatchers.get(j));
               }
            }
         }

         zones = null;
      }

      zones = Zones.getZonesCoveredBy(this.getUnderGround());
      if (zones != null) {
         for(int i = 0; i < zones.length; ++i) {
            List<Creature> pWatchers = zones[i].getPlayerWatchers();

            for(int j = 0; j < pWatchers.size(); ++j) {
               if (!broadcastTargets.contains(pWatchers.get(j))) {
                  broadcastTargets.add(pWatchers.get(j));
               }
            }
         }

         zones = null;
      }

      for(int i = 0; i < broadcastTargets.size(); ++i) {
         Creature target = broadcastTargets.get(i);
         target.getCommunicator().sendUpdateSelectBar(toUpdate, keepSelection);
      }
   }

   public int getDistance(int xFrom, int yFrom, int xTo, int yTo) {
      return Math.max(Math.abs(xTo - xFrom), Math.abs(yTo - yFrom));
   }

   public boolean contains(int tilex, int tiley) {
      return tilex > this.xPos - 152 && tilex < this.xPos + 153 && tiley > this.yPos - 152 && tiley < this.yPos + 153;
   }

   public boolean containsCave(int tilex, int tiley) {
      return tilex >= this.xPos - 24 && tilex < this.xPos + 23 && tiley >= this.yPos - 24 && tiley < this.yPos + 23;
   }

   public void linkZones(int xChange, int yChange) {
      while(xChange != 0) {
         this.surfaceViewField.linkVisionArea();
         this.underGroundViewField.linkVisionArea();
         if (xChange > 0) {
            --xChange;
         } else {
            ++xChange;
         }
      }

      while(yChange != 0) {
         this.surfaceViewField.linkVisionArea();
         this.underGroundViewField.linkVisionArea();
         if (yChange > 0) {
            --yChange;
         } else {
            ++yChange;
         }
      }
   }

   private void increaseCaveMoveCounter() {
      ++this.caveMoveCounter;
      if (this.caveMoveCounter > 5) {
         this.checkCaves(false);
         this.caveMoveCounter = 0;
      }
   }

   public void checkCaves(boolean initialize) {
      if (this.owner.isPlayer()) {
         if (this.seesCaves) {
            if (this.owner.isOnSurface()) {
               this.seesCaves = this.underGroundViewField.shouldSeeCaves();
            }

            if (initialize) {
               this.initializeCaves();
            }
         } else if (this.underGroundViewField.shouldSeeCaves() || initialize) {
            this.seesCaves = true;
            this.initializeCaves();
         }
      }
   }

   public void move(int xChange, int yChange) throws IOException {
      while(xChange != 0) {
         boolean positive;
         if (xChange > 0) {
            ++this.xPos;
            this.surfaceViewField.move(1, 0);
            this.underGroundViewField.move(1, 0);
            --xChange;
            positive = true;
         } else {
            --this.xPos;
            this.surfaceViewField.move(-1, 0);
            this.underGroundViewField.move(-1, 0);
            ++xChange;
            positive = false;
         }

         if (this.owner.isPlayer()) {
            this.increaseCaveMoveCounter();
            this.sendVerticalStrip(positive);
            this.sendVerticalCaveStrip(positive);
            if ((long)(this.xPos - this.startPosX) % 16L == 0L) {
               this.sendVerticalStripFar(positive);
            }
         }
      }

      while(yChange != 0) {
         boolean positive;
         if (yChange > 0) {
            ++this.yPos;
            this.surfaceViewField.move(0, 1);
            this.underGroundViewField.move(0, 1);
            --yChange;
            positive = true;
         } else {
            --this.yPos;
            this.surfaceViewField.move(0, -1);
            this.underGroundViewField.move(0, -1);
            ++yChange;
            positive = false;
         }

         if (this.owner.isPlayer()) {
            this.increaseCaveMoveCounter();
            this.sendHorisontalStrip(positive);
            this.sendHorisontalCaveStrip(positive);
            if ((long)(this.yPos - this.startPosY) % 16L == 0L) {
               this.sendHorisontalStripFar(positive);
            }
         }
      }
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   void destroy() {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Destroying varea " + (this.surfaceViewField != null) + "=sview, uview=" + (this.underGroundViewField != null));
      }

      if (this.surfaceViewField != null) {
         this.surfaceViewField.stopWatching();
         Zones.removeZone(this.surfaceViewField.getId());
      }

      if (this.underGroundViewField != null) {
         this.underGroundViewField.stopWatching();
         Zones.removeZone(this.underGroundViewField.getId());
      }

      this.underGroundViewField = null;
      this.surfaceViewField = null;
      this.owner = null;
   }

   void refreshAttitudes() {
      if (this.surfaceViewField != null) {
         this.surfaceViewField.refreshAttitudes();
      }

      if (this.underGroundViewField != null) {
         this.underGroundViewField.refreshAttitudes();
      }
   }

   public void sendNextStrip() throws Exception {
      if (this.owner != null) {
         Communicator comm = this.owner.getCommunicator();

         while(!this.sentCloseStrips && comm.getConnection().getUnflushed() < 4096) {
            int y = this.startPosY + this.currentStrip;
            comm.sendTileStrip((short)(this.startPosX - 151), (short)y, 302, 1);
            if (this.seesCaves && this.currentStrip < 24) {
               comm.sendCaveStrip((short)(this.startPosX - 24), (short)(this.startPosY + this.currentStrip), 48, 1);
            }

            ++this.currentStrip;
            int y2 = this.startPosY - this.currentStrip;
            comm.sendTileStrip((short)(this.startPosX - 151), (short)y2, 302, 1);
            if (this.seesCaves && this.currentStrip < 25) {
               comm.sendCaveStrip((short)(this.startPosX - 24), (short)(this.startPosY - this.currentStrip), 48, 1);
            }

            if (this.currentStrip > 3) {
               this.owner.checkOpenMineDoor();
            }

            if (this.currentStrip > 152) {
               this.sentCloseStrips = true;
               this.currentStrip = 0;
            }
         }

         if (this.sentCloseStrips) {
            this.sendFarStrips();
         }

         if (this.currentStrip > 10 && !this.resumed) {
            this.surfaceViewField.initialize();
            this.underGroundViewField.initialize();
            this.owner.spawnFreeItems();
            if (this.owner.getVehicle() > 0L) {
               this.owner.getMovementScheme().resendMountSpeed();
            }

            this.owner.getMovementScheme().resumeSpeedModifier();
            this.owner.getCommunicator().sendStartMoving();
            this.resumed = true;
         }

         if (this.initialized && !this.resumed) {
            logger.log(Level.WARNING, this.owner.getName() + ": VisionArea was never resumed.");
         }
      }
   }

   private void sendFarStrips() {
      if (this.owner != null) {
         Communicator comm = this.owner.getCommunicator();

         while(!this.sentFarStrips && comm.getConnection().getUnflushed() < 4096) {
            comm.sendTileStripFar(
               (short)((int)((long)this.startPosX / 16L - 151L)), (short)((int)((long)this.startPosY / 16L + (long)this.currentStrip)), 302, 1
            );
            ++this.currentStrip;
            comm.sendTileStripFar(
               (short)((int)((long)this.startPosX / 16L - 151L)), (short)((int)((long)this.startPosY / 16L - (long)this.currentStrip)), 302, 1
            );
            if (this.currentStrip > 152) {
               this.sentFarStrips = true;
               this.initialized = true;
               this.currentStrip = 0;
            }
         }
      }
   }

   private void sendHorisontalStrip(boolean positive) throws IOException {
      Communicator comm = this.owner.getCommunicator();
      if (positive) {
         int y = this.yPos + 150;
         comm.sendTileStrip((short)(this.xPos - 151), (short)y, 302, 1);
      } else {
         int y = this.yPos - 151;
         comm.sendTileStrip((short)(this.xPos - 151), (short)y, 302, 1);
      }
   }

   private void sendVerticalStrip(boolean positive) throws IOException {
      Communicator comm = this.owner.getCommunicator();
      if (positive) {
         int x = this.xPos + 150;
         comm.sendTileStrip((short)x, (short)(this.yPos - 151), 1, 302);
      } else {
         int x = this.xPos - 151;
         comm.sendTileStrip((short)x, (short)(this.yPos - 151), 1, 302);
      }
   }

   private void sendHorisontalCaveStrip(boolean positive) {
      if (this.isNearCave()) {
         Communicator comm = this.owner.getCommunicator();
         if (positive) {
            comm.sendCaveStrip((short)(this.xPos - 24), (short)(this.yPos + 23), 48, 1);
         } else {
            comm.sendCaveStrip((short)(this.xPos - 24), (short)(this.yPos - 24), 48, 1);
         }
      }
   }

   private void sendVerticalCaveStrip(boolean positive) {
      if (this.isNearCave()) {
         Communicator comm = this.owner.getCommunicator();
         if (positive) {
            comm.sendCaveStrip((short)(this.xPos + 23), (short)(this.yPos - 24), 1, 48);
         } else {
            comm.sendCaveStrip((short)(this.xPos - 24), (short)(this.yPos - 24), 1, 48);
         }
      }
   }

   void initializeCaves() {
      Communicator comm = this.owner.getCommunicator();
      comm.sendCaveStrip((short)(this.xPos - 24), (short)(this.yPos - 24), 48, 48);
   }

   private void sendHorisontalStripFar(boolean positive) {
      Communicator comm = this.owner.getCommunicator();
      if (positive) {
         comm.sendTileStripFar((short)((int)((long)this.xPos / 16L - 151L)), (short)((int)((long)this.yPos / 16L + 150L)), 302, 1);
      } else {
         comm.sendTileStripFar((short)((int)((long)this.xPos / 16L - 151L)), (short)((int)((long)this.yPos / 16L - 151L)), 302, 1);
      }
   }

   private void sendVerticalStripFar(boolean positive) {
      Communicator comm = this.owner.getCommunicator();
      if (positive) {
         comm.sendTileStripFar((short)((int)((long)this.xPos / 16L + 150L)), (short)((int)((long)this.yPos / 16L - 151L)), 1, 302);
      } else {
         comm.sendTileStripFar((short)((int)((long)this.xPos / 16L - 151L)), (short)((int)((long)this.yPos / 16L - 151L)), 1, 302);
      }
   }

   @Override
   public String toString() {
      return "VisionArea [initialised: "
         + this.isInitialized()
         + ", resumed: "
         + this.resumed
         + ", sentCloseStrips: "
         + this.sentCloseStrips
         + ", sentFarStrips: "
         + this.sentFarStrips
         + ']';
   }
}

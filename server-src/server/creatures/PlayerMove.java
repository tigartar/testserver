package com.wurmonline.server.creatures;

import com.wurmonline.server.players.Player;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerMove {
   private static final Logger logger = Logger.getLogger(PlayerMove.class.getName());
   public static final int NOHEIGHTCHANGE = -10000;
   static final float NOSPEEDCHANGE = -100.0F;
   static final byte NOWINDCHANGE = -100;
   static final short NOMOVECHANGE = -100;
   static final byte NOBRIDGECHANGE = 0;
   private float newPosX;
   private float newPosY;
   private float newPosZ;
   private float newRot;
   private byte bm;
   private byte layer;
   private PlayerMove next = null;
   private int sameMoves = 0;
   private boolean handled = false;
   private boolean toggleClimb = false;
   private boolean climbing = false;
   private boolean weatherChange = false;
   private float newSpeedMod = -100.0F;
   private byte newWindMod = -100;
   private short newMountSpeed = -100;
   private long newBridgeId = 0L;
   private int newHeightOffset = -10000;
   private boolean changeHeightImmediately = false;
   private boolean isOnFloor = false;
   private boolean isFalling = false;
   int number = -1;
   boolean cleared = false;

   public void clear(boolean clearMoveChanges, MovementScheme ticker, Player player, Logger cheatlogger) {
      PlayerMove nnext;
      for(PlayerMove cnext = this.next; cnext != null; cnext = nnext) {
         if (cnext.cleared) {
            logger.log(Level.INFO, "This (" + cnext + ") was already cleared. Returning. Next=" + this.next, (Throwable)(new Exception()));
            return;
         }

         if (clearMoveChanges) {
            CommuincatorMoveChangeChecker.checkMoveChanges(cnext, ticker, player, cheatlogger);
         }

         cnext.cleared = true;
         if (cnext.next == cnext) {
            logger.log(Level.INFO, "This (" + cnext + ") was same as this. Returning. Next=" + cnext.next, (Throwable)(new Exception()));
            this.next = null;
            return;
         }

         nnext = cnext.next;
         cnext.next = null;
      }
   }

   float getNewPosX() {
      return this.newPosX;
   }

   void setNewPosX(float aNewPosX) {
      this.newPosX = aNewPosX;
   }

   float getNewPosY() {
      return this.newPosY;
   }

   void setNewPosY(float aNewPosY) {
      this.newPosY = aNewPosY;
   }

   float getNewPosZ() {
      return this.newPosZ;
   }

   void setNewPosZ(float aNewPosZ) {
      this.newPosZ = aNewPosZ;
   }

   float getNewRot() {
      return this.newRot;
   }

   void setNewRot(float aNewRot) {
      this.newRot = aNewRot;
   }

   byte getBm() {
      return this.bm;
   }

   void setBm(byte aBm) {
      this.bm = aBm;
   }

   byte getLayer() {
      return this.layer;
   }

   void setLayer(byte aLayer) {
      this.layer = aLayer;
   }

   public PlayerMove getNext() {
      return this.next;
   }

   public int getNumber() {
      return this.number;
   }

   public void setNext(PlayerMove aNext) {
      this.next = aNext;
      if (this.next != null) {
         this.next.number = this.number + 1;
      }
   }

   int getSameMoves() {
      return this.sameMoves;
   }

   void incrementSameMoves() {
      ++this.sameMoves;
   }

   void sameNoMoves() {
      this.sameMoves = 1;
   }

   void resetSameMoves() {
      this.sameMoves = 0;
   }

   boolean isHandled() {
      return this.handled;
   }

   void setHandled(boolean aHandled) {
      this.handled = aHandled;
      if (this.handled) {
         this.number = 0;
      }
   }

   boolean isToggleClimb() {
      return this.toggleClimb;
   }

   void setToggleClimb(boolean aToggleClimb) {
      this.toggleClimb = aToggleClimb;
   }

   boolean isClimbing() {
      return this.climbing;
   }

   boolean isFalling() {
      return this.isFalling;
   }

   public void setIsFalling(boolean falling) {
      this.isFalling = falling;
   }

   void setClimbing(boolean aClimbing) {
      this.climbing = aClimbing;
   }

   boolean isWeatherChange() {
      return this.weatherChange;
   }

   void setWeatherChange(boolean aWeatherChange) {
      this.weatherChange = aWeatherChange;
   }

   float getNewSpeedMod() {
      return this.newSpeedMod;
   }

   void setNewSpeedMod(float aNewSpeedMod) {
      this.newSpeedMod = aNewSpeedMod;
   }

   byte getNewWindMod() {
      return this.newWindMod;
   }

   void setNewWindMod(byte aNewWindMod) {
      this.newWindMod = aNewWindMod;
   }

   short getNewMountSpeed() {
      return this.newMountSpeed;
   }

   void setNewMountSpeed(short aNewMountSpeed) {
      this.newMountSpeed = aNewMountSpeed;
   }

   public PlayerMove getLast() {
      return this.next != null ? this.next.getLast() : this;
   }

   public int getNewHeightOffset() {
      return this.newHeightOffset;
   }

   public void setNewHeightOffset(int aNewHeightOffset) {
      this.newHeightOffset = aNewHeightOffset;
   }

   public boolean isChangeHeightImmediately() {
      return this.changeHeightImmediately;
   }

   public void setChangeHeightImmediately(boolean aChangeHeightImmediately) {
      this.changeHeightImmediately = aChangeHeightImmediately;
   }

   public long getNewBridgeId() {
      return this.newBridgeId;
   }

   public void setNewBridgeId(long newBridgeId) {
      this.newBridgeId = newBridgeId;
   }

   public boolean isOnFloor() {
      return this.isOnFloor;
   }

   public void setOnFloor(boolean aIsOnFloor) {
      this.isOnFloor = aIsOnFloor;
   }
}

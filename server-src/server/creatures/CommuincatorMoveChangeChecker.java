package com.wurmonline.server.creatures;

import com.wurmonline.server.Server;
import com.wurmonline.server.players.Player;
import java.util.logging.Logger;

final class CommuincatorMoveChangeChecker {
   private static final Logger logger = Logger.getLogger(CommuincatorMoveChangeChecker.class.getName());

   private CommuincatorMoveChangeChecker() {
   }

   static boolean checkMoveChanges(PlayerMove currentmove, MovementScheme ticker, Player player, Logger cheatlogger) {
      checkIsFalling(currentmove, ticker);
      checkFloorOverride(currentmove, ticker);
      return checkBridgeChange(currentmove, player, cheatlogger)
         | checkClimb(currentmove, ticker)
         | checkWeather(currentmove, ticker)
         | checkWindMod(currentmove, player, cheatlogger)
         | checkMountSpeed(currentmove, player, cheatlogger)
         | checkSpeedMod(currentmove, player, cheatlogger)
         | checkHeightOffsetChanged(currentmove, player);
   }

   static void checkFloorOverride(PlayerMove currentmove, MovementScheme ticker) {
      if (currentmove != null) {
         ticker.setOnFloorOverride(currentmove.isOnFloor());
      }
   }

   static boolean checkClimb(PlayerMove currentmove, MovementScheme ticker) {
      if (currentmove != null && currentmove.isToggleClimb()) {
         ticker.setServerClimbing(currentmove.isClimbing());
         currentmove.setToggleClimb(false);
         return true;
      } else {
         return false;
      }
   }

   static void checkIsFalling(PlayerMove currentmove, MovementScheme ticker) {
      if (currentmove != null) {
         ticker.setIsFalling(currentmove.isFalling());
      }
   }

   static boolean checkWeather(PlayerMove currentmove, MovementScheme ticker) {
      if (currentmove != null && currentmove.isWeatherChange()) {
         ticker.diffWindX = Server.getWeather().getXWind();
         ticker.diffWindY = Server.getWeather().getYWind();
         ticker.setWindRotation(Server.getWeather().getWindRotation());
         ticker.setWindStrength(Server.getWeather().getWindPower());
         currentmove.setWeatherChange(false);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkSpeedMod(PlayerMove currentmove, Player player, Logger cheatlogger) {
      if (currentmove != null && currentmove.getNewSpeedMod() != -100.0F) {
         player.getMovementScheme().setSpeedModifier(currentmove.getNewSpeedMod());
         currentmove.setNewSpeedMod(-100.0F);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkBridgeChange(PlayerMove currentmove, Player player, Logger cheatlogger) {
      if (currentmove != null && currentmove.getNewBridgeId() != 0L) {
         if (player.getBridgeId() > 0L && currentmove.getNewBridgeId() < 0L && currentmove.getNewHeightOffset() > 0) {
            player.getMovementScheme().setGroundOffset(currentmove.getNewHeightOffset(), currentmove.isChangeHeightImmediately());
         }

         player.setBridgeId(currentmove.getNewBridgeId(), false);
         player.getMovementScheme().setBridgeId(currentmove.getNewBridgeId());
         currentmove.setNewBridgeId(0L);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkWindMod(PlayerMove currentmove, Player player, Logger cheatlogger) {
      if (currentmove != null && currentmove.getNewWindMod() != -100) {
         player.getMovementScheme().setWindMod(currentmove.getNewWindMod());
         currentmove.setNewWindMod((byte)-100);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkMountSpeed(PlayerMove currentmove, Player player, Logger cheatlogger) {
      if (currentmove != null && currentmove.getNewMountSpeed() != -100) {
         player.getMovementScheme().setMountSpeed(currentmove.getNewMountSpeed());
         currentmove.setNewMountSpeed((short)-100);
         return true;
      } else {
         return false;
      }
   }

   static boolean checkHeightOffsetChanged(PlayerMove currentMove, Player player) {
      if (currentMove != null && currentMove.getNewHeightOffset() != -10000) {
         player.getMovementScheme().setGroundOffset(currentMove.getNewHeightOffset(), currentMove.isChangeHeightImmediately());
         currentMove.setNewHeightOffset(-10000);
         return true;
      } else {
         return false;
      }
   }
}

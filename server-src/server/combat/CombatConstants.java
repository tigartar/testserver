package com.wurmonline.server.combat;

public interface CombatConstants {
   int ROUND_TIME = 10;
   byte FIGHTNORMAL = 0;
   byte FIGHTAGG = 1;
   byte FIGHTDEFEND = 2;
   byte STANCE_STANDARD = 0;
   byte STANCE_UPPER_RIGHT = 1;
   byte STANCE_MID_RIGHT = 2;
   byte STANCE_LOWER_RIGHT = 3;
   byte STANCE_LOWER_LEFT = 4;
   byte STANCE_MID_LEFT = 5;
   byte STANCE_UPPER_LEFT = 6;
   byte STANCE_HIGH = 7;
   byte STANCE_PRONE = 8;
   byte STANCE_OPEN = 9;
   byte STANCE_LOW = 10;
   byte STANCE_DEFEND_LOW = 11;
   byte STANCE_DEFEND_HIGH = 12;
   byte STANCE_DEFEND_RIGHT = 13;
   byte STANCE_DEFEND_LEFT = 14;
   byte STANCE_IDLE = 15;
   byte[] standardSoftSpots = new byte[]{6, 1};
   byte[] lowCenterSoftSpots = new byte[]{5, 2};
   byte[] midLeftSoftSpots = new byte[]{1, 4};
   byte[] midRightSoftSpots = new byte[]{6, 3};
   byte[] upperCenterSoftSpots = new byte[]{4, 5};
   byte[] upperLeftSoftSpots = new byte[]{3, 2};
   byte[] upperRightSoftSpots = new byte[]{4};
   byte[] lowerRightSoftSpots = new byte[]{7};
   byte[] lowerLeftSoftSpots = new byte[]{10};
   byte[] emptyByteArray = new byte[0];
}

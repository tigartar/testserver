package com.wurmonline.server.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ProtocolUtilities {
   private static final String UTF_8_ENCODING = "UTF-8";
   private static Logger logger = Logger.getLogger(ProtocolUtilities.class.getName());
   public static final String CMD_LOGIN_DESCRIPTION = "Login-Command";
   public static final String CMD_LOGOUT_DESCRIPTION = "Logout-Command";
   public static final String CMD_ERROR_DESCRIPTION = "Error-Command";
   public static final String CMD_FATAL_ERROR_DESCRIPTION = "Fatal-Error-Command";
   public static final String CMD_MESSAGE_DESCRIPTION = "Message-Command";
   public static final String CMD_ACTION_DESCRIPTION = "Action-Command";
   public static final String CMD_MOVE_DESCRIPTION = "Move-Command";
   public static final String CMD_SET_SKILL_DESCRIPTION = "Set-Skill-Command";
   public static final String CMD_ADD_ITEM_DESCRIPTION = "Add-Item-Command";
   public static final String CMD_REMOVE_ITEM_DESCRIPTION = "Remove-Item-Command";
   public static final String CMD_REMOVE_FROM_INVENTORY_DESCRIPTION = "Remove-From-Inventory-Command";
   public static final String CMD_TILESTRIP_FAR_DESCRIPTION = "TileStrip-Far-Command";
   public static final String CMD_TILESTRIP_DESCRIPTION = "TileStrip-Command";
   public static final String CMD_UPDATE_INVENTORY_DESCRIPTION = "Update-Inventory-Command";
   public static final String CMD_ADD_TO_INVENTORY_DESCRIPTION = "Add-To-Inventory-Command";
   public static final String CMD_DELETE_CREATURE_DESCRIPTION = "Delete-Creature-Command";
   public static final String CMD_MOVE_CREATURE_DESCRIPTION = "Move-Creature-Command";
   public static final String CMD_UPDATE_SKILL_DESCRIPTION = "Update-Skill-Command";
   public static final String CMD_ADD_CREATURE_DESCRIPTION = "Add-Creature-Command";
   public static final String CMD_NOT_MOVE_CREATURE_DESCRIPTION = "Not-Move-Command";
   public static final String CMD_STATUS_WEIGHT_DESCRIPTION = "Status-Weight-Command";
   public static final String CMD_AVAILABLE_ACTIONS_DESCRIPTION = "Available-Actions-Command";
   public static final String CMD_TILESTRIP_CAVE_DESCRIPTION = "TileStrip-Cave-Command";
   public static final String CMD_STATUS_STAMINA_DESCRIPTION = "Status-Stamina-Command";
   public static final String CMD_ACTION_STRING_DESCRIPTION = "Action-String-Command";
   public static final String CMD_ADD_EFFECT_DESCRIPTION = "Add-Effect-Command";
   public static final String CMD_REMOVE_EFFECT_DESCRIPTION = "Remove-Effect-Command";
   public static final String CMD_MOVE_INVENTORY_DESCRIPTION = "Move-Inventory-Command";
   public static final String CMD_STATUS_HUNGER_DESCRIPTION = "Status-Hunger-Command";
   public static final String CMD_STATUS_THIRST_DESCRIPTION = "Status-Thirst-Command";
   public static final String CMD_OPEN_WALL_DESCRIPTION = "Open-Wall-Command";
   public static final String CMD_SPEEDMODIFIER_DESCRIPTION = "SpeedModifier-Command";
   public static final String CMD_TIMELEFT_DESCRIPTION = "TimeLeft-Command";
   public static final String CMD_BUILD_MARK_DESCRIPTION = "Build-Mark-Command";
   public static final String CMD_ADD_STRUCTURE_DESCRIPTION = "Add-Structure-Command";
   public static final String CMD_REMOVE_STRUCTURE_DESCRIPTION = "Remove-Structure-Command";
   public static final String CMD_ADD_WALL_DESCRIPTION = "Add-Wall-Command";
   public static final String CMD_REQUEST_ACTIONS_DESCRIPTION = "Request-Actions-Command";
   public static final String CMD_CLOSE_WALL_DESCRIPTION = "Close-Wall-Command";
   public static final String CMD_SET_PASSABLE_DESCRIPTION = "Set-Passable-Command";
   public static final String CMD_SHOW_HTML_DESCRIPTION = "Show-HTML-Command";
   public static final String CMD_FORM_RESPONSE_DESCRIPTION = "Form-Response-Command";
   public static final String CMD_RECEIVED_DESCRIPTION = "Received-Command";
   public static final String CMD_RENAME_DESCRIPTION = "Rename-Command";
   public static final String CMD_TELEPORT_DESCRIPTION = "Teleport-Command";
   public static final String CMD_OPEN_INVENTORY_WINDOW_DESCRIPTION = "Open-Inventory-Window-Command";
   public static final String CMD_CLOSE_INVENTORY_WINDOW_DESCRIPTION = "Close-Inventory-Window-Command";
   public static final String CMD_OPEN_TRADE_WINDOW_DESCRIPTION = "Open-Trade-Window-Command";
   public static final String CMD_CLOSE_TRADE_WINDOW_DESCRIPTION = "Close-Trade-Window-Command";
   public static final String CMD_SET_TRADE_AGREE_DESCRIPTION = "Set-Trade-Agree-Command";
   public static final String CMD_TRADE_CHANGED_DESCRIPTION = "Trade-Changed-Command";
   public static final String CMD_RENAME_ITEM_DESCRIPTION = "Rename-Item-Command";
   public static final String CMD_ADD_FENCE_DESCRIPTION = "Add-Fence-Command";
   public static final String CMD_REMOVE_FENCE_DESCRIPTION = "Remove-Fence-Command";
   public static final String CMD_OPEN_FENCE_DESCRIPTION = "Open-Fence-Command";
   public static final String CMD_PLAYSOUND_DESCRIPTION = "Play-Sound-Command";
   public static final String CMD_STATUS_STRING_DESCRIPTION = "Status-String-Command";
   public static final String CMD_JOIN_GROUP_DESCRIPTION = "Join-Group-Command";
   public static final String CMD_PART_GROUP_DESCRIPTION = "Part-Group-Command";
   public static final String CMD_SET_CREATURE_ATTITUDE_DESCRIPTION = "Set-Creature-Attitude-Command";
   public static final String CMD_WEATHER_UPDATE_DESCRIPTION = "Weather-Update-Command";
   public static final String CMD_DEAD_DESCRIPTION = "Dead-Command";
   public static final String CMD_RECONNECT_DESCRIPTION = "Reconnect-Command";
   public static final String CMD_CLIMB_DESCRIPTION = "Climb-Command";
   public static final String CMD_TOGGLE_SWITCH_DESCRIPTION = "Toggle-Switch-Command";
   public static final String CMD_MORE_ITEMS_DESCRIPTION = "More-Items-Command";
   public static final String CMD_EMPTY_DESCRIPTION = "Empty-Command";
   public static final String CMD_CREATURE_LAYER_DESCRIPTION = "Creature-Layer-Command";
   public static final String CMD_TOGGLE_CLIENT_FEATURE_DESCRIPTION = "Toggle-Client-Feature-Command";
   public static final String CMD_BML_FORM_DESCRIPTION = "BML-Form-Command";
   public static final String CMD_SERVER_TIME_DESCRIPTION = "Server-Time-Command";
   public static final String CMD_ATTACH_EFFECT_DESCRIPTION = "Attach-Effect-Command";
   public static final String CMD_UNATTACH_EFFECT_DESCRIPTION = "Unattach-Effect-Command";
   public static final String CMD_SET_EQUIPMENT_DESCRIPTION = "Set-Equipment-Command";
   public static final String CMD_USE_ITEM_DESCRIPTION = "Use-Item-Command";
   public static final String CMD_STOP_USE_ITEM_DESCRIPTION = "Stop-Use-Item-Command";
   public static final String CMD_MOVE_CREATURE_AND_SET_Z_DESCRIPTION = "Move-Creature-And-Set-Z-Command";
   public static final String CMD_REPAINT_DESCRIPTION = "Repaint-Command";
   public static final String CMD_RESIZE_DESCRIPTION = "Resize-Command";
   public static final String CMD_CLIENT_QUIT_DESCRIPTION = "Client-Quit-Command";
   public static final String CMD_ATTACH_CREATURE_DESCRIPTION = "Attach-Creature-Command";
   public static final String CMD_SET_VEHICLE_CONTROLLER_DESCRIPTION = "Set-Vehicle-Controller-Command";
   public static final String CMD_PLAY_ANIMATION_DESCRIPTION = "Play-Animation-Command";
   public static final String CMD_MESSAGE_TYPED_DESCRIPTION = "Message-Typed-Command";
   public static final String CMD_FIGHT_MOVE_OPTIONS_DESCRIPTION = "Fight-Move-Options-Command";
   public static final String CMD_FIGHT_STATUS_DESCRIPTION = "Fight-Status-Command";
   public static final String CMD_STUNNED_DESCRIPTION = "Stunned-Command";
   public static final String CMD_SPECIALMOVE_DESCRIPTION = "Special-Move-Command";
   public static final String CMD_SET_TARGET_DESCRIPTION = "Set-Target-Command";
   public static final String CMD_SET_FIGHTSTYLE_DESCRIPTION = "Set-Fight-Style-Command";
   public static final String CMD_SET_CREATUREDAMAGE_DESCRIPTION = "Set-Creature-Damage-Command";
   public static final String CMD_PLAYMUSIC_DESCRIPTION = "Play-Music-Command";
   public static final String CMD_WINDIMPACT_DESCRIPTION = "Wind-Impact-Command";
   public static final String CMD_ROTATE_DESCRIPTION = "Rotate-Command";
   public static final String CMD_MOUNTSPEED_DESCRIPTION = "Mount-Speed-Command";
   public static final String TOGGLE_CLIMBING_DESCRIPTION = "Climbing-Toggle";
   public static final String TOGGLE_FAITHFUL_DESCRIPTION = "Faithful-Toggle";
   public static final String TOGGLE_LAWFUL_DESCRIPTION = "Lawful-Toggle";
   public static final String TOGGLE_STEALTH_DESCRIPTION = "Stealth-Toggle";
   public static final String TOGGLE_AUTOFIGHT_DESCRIPTION = "Autofight-Toggle";
   public static final String TOGGLE_ARCHERY_DESCRIPTION = "Archery-Toggle";

   private ProtocolUtilities() {
   }

   public static String getDescriptionForCommand(byte aCommandByte) {
      String lDescription;
      switch(aCommandByte) {
         case -38:
            lDescription = "Move-Command";
            break;
         case -37:
         case -36:
         case -35:
         case -34:
         case -33:
         case -32:
         case -31:
         case -29:
         case -28:
         case -27:
         case -26:
         case -25:
         case -24:
         case -23:
         case -22:
         case -21:
         case -20:
         case -19:
         case -8:
         case -7:
         case -6:
         case -5:
         case -4:
         case -3:
         case -2:
         case -1:
         case 0:
         case 1:
         case 2:
         case 3:
         case 7:
         case 17:
         case 19:
         case 21:
         case 22:
         case 27:
         case 31:
         case 33:
         case 34:
         case 35:
         case 38:
         case 39:
         case 40:
         case 45:
         case 50:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 70:
         case 75:
         case 77:
         case 78:
         case 80:
         case 81:
         case 82:
         case 84:
         case 85:
         case 88:
         case 89:
         case 94:
         case 95:
         case 100:
         case 104:
         case 113:
         case 118:
         case 123:
         default:
            logger.warning("Unknown command byte: '" + Byte.toString(aCommandByte) + '\'');
            lDescription = "Unknown-Command";
            break;
         case -30:
            lDescription = "Toggle-Client-Feature-Command";
            break;
         case -18:
            lDescription = "Status-String-Command";
            break;
         case -17:
            lDescription = "Special-Move-Command";
            break;
         case -16:
            lDescription = "Empty-Command";
            break;
         case -15:
            lDescription = "Login-Command";
            break;
         case -14:
            lDescription = "Fight-Status-Command";
            break;
         case -13:
            lDescription = "Join-Group-Command";
            break;
         case -12:
            lDescription = "Action-String-Command";
            break;
         case -11:
            lDescription = "Show-HTML-Command";
            break;
         case -10:
            lDescription = "Remove-From-Inventory-Command";
            break;
         case -9:
            lDescription = "Add-Item-Command";
            break;
         case 4:
            lDescription = "Client-Quit-Command";
            break;
         case 5:
            lDescription = "Status-Weight-Command";
            break;
         case 6:
            lDescription = "Set-Creature-Attitude-Command";
            break;
         case 8:
            lDescription = "Logout-Command";
            break;
         case 9:
            lDescription = "Not-Move-Command";
            break;
         case 10:
            lDescription = "Remove-Item-Command";
            break;
         case 11:
            lDescription = "Set-Creature-Damage-Command";
            break;
         case 12:
            lDescription = "Add-Fence-Command";
            break;
         case 13:
            lDescription = "Remove-Fence-Command";
            break;
         case 14:
            lDescription = "Delete-Creature-Command";
            break;
         case 15:
            lDescription = "Form-Response-Command";
            break;
         case 16:
            lDescription = "Fatal-Error-Command";
            break;
         case 18:
            lDescription = "Unattach-Effect-Command";
            break;
         case 20:
            lDescription = "Available-Actions-Command";
            break;
         case 23:
            lDescription = "Reconnect-Command";
            break;
         case 24:
            lDescription = "Play-Animation-Command";
            break;
         case 25:
            lDescription = "Set-Target-Command";
            break;
         case 26:
            lDescription = "Set-Fight-Style-Command";
            break;
         case 28:
            lDescription = "Stunned-Command";
            break;
         case 29:
            lDescription = "More-Items-Command";
            break;
         case 30:
            lDescription = "Creature-Layer-Command";
            break;
         case 32:
            lDescription = "SpeedModifier-Command";
            break;
         case 36:
            lDescription = "Move-Creature-Command";
            break;
         case 37:
            lDescription = "Remove-Effect-Command";
            break;
         case 41:
            lDescription = "Error-Command";
            break;
         case 42:
            lDescription = "Set-Trade-Agree-Command";
            break;
         case 43:
            lDescription = "Move-Inventory-Command";
            break;
         case 44:
            lDescription = "Rename-Item-Command";
            break;
         case 46:
            lDescription = "Weather-Update-Command";
            break;
         case 47:
            lDescription = "Rename-Command";
            break;
         case 48:
            lDescription = "Remove-Structure-Command";
            break;
         case 49:
            lDescription = "Add-Wall-Command";
            break;
         case 51:
            lDescription = "Teleport-Command";
            break;
         case 60:
            lDescription = "Mount-Speed-Command";
            break;
         case 61:
            lDescription = "Status-Hunger-Command";
            break;
         case 62:
            lDescription = "Toggle-Switch-Command";
            break;
         case 63:
            lDescription = "Set-Vehicle-Controller-Command";
            break;
         case 64:
            lDescription = "Add-Effect-Command";
            break;
         case 65:
            lDescription = "Dead-Command";
            break;
         case 66:
            lDescription = "Update-Skill-Command";
            break;
         case 67:
            lDescription = "Rotate-Command";
            break;
         case 68:
            lDescription = "Update-Inventory-Command";
            break;
         case 69:
            lDescription = "Received-Command";
            break;
         case 71:
            lDescription = "Stop-Use-Item-Command";
            break;
         case 72:
            lDescription = "Move-Creature-And-Set-Z-Command";
            break;
         case 73:
            lDescription = "TileStrip-Command";
            break;
         case 74:
            lDescription = "Resize-Command";
            break;
         case 76:
            lDescription = "Add-To-Inventory-Command";
            break;
         case 79:
            lDescription = "Climb-Command";
            break;
         case 83:
            lDescription = "Open-Fence-Command";
            break;
         case 86:
            lDescription = "Play-Sound-Command";
            break;
         case 87:
            lDescription = "TimeLeft-Command";
            break;
         case 90:
            lDescription = "Status-Stamina-Command";
            break;
         case 91:
            lDescription = "Trade-Changed-Command";
            break;
         case 92:
            lDescription = "Repaint-Command";
            break;
         case 93:
            lDescription = "Message-Typed-Command";
            break;
         case 96:
            lDescription = "Build-Mark-Command";
            break;
         case 97:
            lDescription = "Action-Command";
            break;
         case 98:
            lDescription = "Fight-Move-Options-Command";
            break;
         case 99:
            lDescription = "Message-Command";
            break;
         case 101:
            lDescription = "Set-Equipment-Command";
            break;
         case 102:
            lDescription = "TileStrip-Cave-Command";
            break;
         case 103:
            lDescription = "TileStrip-Far-Command";
            break;
         case 105:
            lDescription = "Status-Thirst-Command";
            break;
         case 106:
            lDescription = "BML-Form-Command";
            break;
         case 107:
            lDescription = "Server-Time-Command";
            break;
         case 108:
            lDescription = "Add-Creature-Command";
            break;
         case 109:
            lDescription = "Attach-Effect-Command";
            break;
         case 110:
            lDescription = "Use-Item-Command";
            break;
         case 111:
            lDescription = "Attach-Creature-Command";
            break;
         case 112:
            lDescription = "Add-Structure-Command";
            break;
         case 114:
            lDescription = "Part-Group-Command";
            break;
         case 115:
            lDescription = "Play-Music-Command";
            break;
         case 116:
            lDescription = "Open-Inventory-Window-Command";
            break;
         case 117:
            lDescription = "Wind-Impact-Command";
            break;
         case 119:
            lDescription = "Open-Trade-Window-Command";
            break;
         case 120:
            lDescription = "Close-Inventory-Window-Command";
            break;
         case 121:
            lDescription = "Close-Trade-Window-Command";
            break;
         case 122:
            lDescription = "Open-Wall-Command";
            break;
         case 124:
            lDescription = "Set-Skill-Command";
            break;
         case 125:
            lDescription = "Set-Passable-Command";
            break;
         case 126:
            lDescription = "Request-Actions-Command";
            break;
         case 127:
            lDescription = "Close-Wall-Command";
      }

      return lDescription;
   }

   public static String getDescriptionForToggle(int aToggleCode) {
      String lDescription;
      switch(aToggleCode) {
         case 0:
            lDescription = "Climbing-Toggle";
            break;
         case 1:
            lDescription = "Faithful-Toggle";
            break;
         case 2:
            lDescription = "Lawful-Toggle";
            break;
         case 3:
            lDescription = "Stealth-Toggle";
            break;
         case 4:
            lDescription = "Autofight-Toggle";
            break;
         case 100:
            lDescription = "Archery-Toggle";
            break;
         default:
            logger.warning("Unknown Toggle code: '" + aToggleCode + '\'');
            lDescription = "Unknown-Toggle";
      }

      return lDescription;
   }

   public static boolean isValidCommandType(byte aCommandByte) {
      boolean isValid;
      switch(aCommandByte) {
         case -38:
         case -30:
         case -18:
         case -17:
         case -16:
         case -15:
         case -14:
         case -13:
         case -12:
         case -11:
         case -10:
         case -9:
         case 4:
         case 5:
         case 6:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 18:
         case 20:
         case 23:
         case 24:
         case 25:
         case 26:
         case 28:
         case 29:
         case 30:
         case 32:
         case 36:
         case 37:
         case 41:
         case 42:
         case 43:
         case 44:
         case 46:
         case 47:
         case 48:
         case 49:
         case 51:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
         case 69:
         case 71:
         case 72:
         case 73:
         case 74:
         case 76:
         case 79:
         case 83:
         case 86:
         case 87:
         case 90:
         case 91:
         case 92:
         case 93:
         case 96:
         case 97:
         case 98:
         case 99:
         case 101:
         case 102:
         case 103:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 114:
         case 115:
         case 116:
         case 117:
         case 119:
         case 120:
         case 121:
         case 122:
         case 124:
         case 125:
         case 126:
         case 127:
            isValid = true;
            break;
         case -37:
         case -36:
         case -35:
         case -34:
         case -33:
         case -32:
         case -31:
         case -29:
         case -28:
         case -27:
         case -26:
         case -25:
         case -24:
         case -23:
         case -22:
         case -21:
         case -20:
         case -19:
         case -8:
         case -7:
         case -6:
         case -5:
         case -4:
         case -3:
         case -2:
         case -1:
         case 0:
         case 1:
         case 2:
         case 3:
         case 7:
         case 17:
         case 19:
         case 21:
         case 22:
         case 27:
         case 31:
         case 33:
         case 34:
         case 35:
         case 38:
         case 39:
         case 40:
         case 45:
         case 50:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 70:
         case 75:
         case 77:
         case 78:
         case 80:
         case 81:
         case 82:
         case 84:
         case 85:
         case 88:
         case 89:
         case 94:
         case 95:
         case 100:
         case 104:
         case 113:
         case 118:
         case 123:
         default:
            logger.warning("Unknown command byte: '" + Byte.toString(aCommandByte) + '\'');
            isValid = false;
      }

      return isValid;
   }

   public static boolean getBooleanFromSingleByte(ByteBuffer aByteBuffer) {
      return aByteBuffer.get() != 0;
   }

   public static void putBooleanIntoSingleByte(ByteBuffer aByteBuffer, boolean aValue) {
      aByteBuffer.put((byte)(aValue ? 1 : 0));
   }

   public static int getIntFromSingleByte(ByteBuffer aByteBuffer) {
      return aByteBuffer.get() & 0xFF;
   }

   public static void putIntIntoSingleByte(ByteBuffer aByteBuffer, int aValue) {
      aByteBuffer.put((byte)aValue);
   }

   public static float getFloat64FromSingleByte(ByteBuffer aByteBuffer) {
      return (float)(aByteBuffer.get() & 255) / 64.0F;
   }

   public static float getFloat255FromSingleByte(ByteBuffer aByteBuffer) {
      return (float)(aByteBuffer.get() & 255) / 255.0F;
   }

   public static String getString(ByteBuffer aByteBuffer) {
      byte[] lStringBytes = new byte[aByteBuffer.get() & 255];
      aByteBuffer.get(lStringBytes);

      String lString;
      try {
         lString = new String(lStringBytes, "UTF-8");
      } catch (UnsupportedEncodingException var4) {
         logger.log(Level.WARNING, "Could not create a UTF-8 String", (Throwable)var4);
         lString = "";
      }

      return lString;
   }

   public static void putString(ByteBuffer aByteBuffer, String aValue) {
      if (aValue.length() > 255) {
         logger.warning("Only the first 255 characters will be put in the ByteBuffer, String: " + aValue);
      }

      try {
         byte[] lMessageBytes = aValue.getBytes("UTF-8");
         aByteBuffer.put((byte)lMessageBytes.length);
         aByteBuffer.put(lMessageBytes);
      } catch (UnsupportedEncodingException var3) {
         logger.log(Level.WARNING, "Could not create a UTF-8 String so putting length 0 in the Buffer, String: " + aValue, (Throwable)var3);
         aByteBuffer.put((byte)0);
      }
   }
}

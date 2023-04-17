/*
 * Decompiled with CFR 0.152.
 */
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
        switch (aCommandByte) {
            case -15: {
                lDescription = CMD_LOGIN_DESCRIPTION;
                break;
            }
            case 8: {
                lDescription = CMD_LOGOUT_DESCRIPTION;
                break;
            }
            case 41: {
                lDescription = CMD_ERROR_DESCRIPTION;
                break;
            }
            case 16: {
                lDescription = CMD_FATAL_ERROR_DESCRIPTION;
                break;
            }
            case 99: {
                lDescription = CMD_MESSAGE_DESCRIPTION;
                break;
            }
            case 97: {
                lDescription = CMD_ACTION_DESCRIPTION;
                break;
            }
            case -38: {
                lDescription = CMD_MOVE_DESCRIPTION;
                break;
            }
            case 124: {
                lDescription = CMD_SET_SKILL_DESCRIPTION;
                break;
            }
            case -9: {
                lDescription = CMD_ADD_ITEM_DESCRIPTION;
                break;
            }
            case 10: {
                lDescription = CMD_REMOVE_ITEM_DESCRIPTION;
                break;
            }
            case -10: {
                lDescription = CMD_REMOVE_FROM_INVENTORY_DESCRIPTION;
                break;
            }
            case 76: {
                lDescription = CMD_ADD_TO_INVENTORY_DESCRIPTION;
                break;
            }
            case 103: {
                lDescription = CMD_TILESTRIP_FAR_DESCRIPTION;
                break;
            }
            case 73: {
                lDescription = CMD_TILESTRIP_DESCRIPTION;
                break;
            }
            case 68: {
                lDescription = CMD_UPDATE_INVENTORY_DESCRIPTION;
                break;
            }
            case 14: {
                lDescription = CMD_DELETE_CREATURE_DESCRIPTION;
                break;
            }
            case 36: {
                lDescription = CMD_MOVE_CREATURE_DESCRIPTION;
                break;
            }
            case 66: {
                lDescription = CMD_UPDATE_SKILL_DESCRIPTION;
                break;
            }
            case 108: {
                lDescription = CMD_ADD_CREATURE_DESCRIPTION;
                break;
            }
            case 9: {
                lDescription = CMD_NOT_MOVE_CREATURE_DESCRIPTION;
                break;
            }
            case 5: {
                lDescription = CMD_STATUS_WEIGHT_DESCRIPTION;
                break;
            }
            case 20: {
                lDescription = CMD_AVAILABLE_ACTIONS_DESCRIPTION;
                break;
            }
            case 102: {
                lDescription = CMD_TILESTRIP_CAVE_DESCRIPTION;
                break;
            }
            case 90: {
                lDescription = CMD_STATUS_STAMINA_DESCRIPTION;
                break;
            }
            case -12: {
                lDescription = CMD_ACTION_STRING_DESCRIPTION;
                break;
            }
            case 64: {
                lDescription = CMD_ADD_EFFECT_DESCRIPTION;
                break;
            }
            case 37: {
                lDescription = CMD_REMOVE_EFFECT_DESCRIPTION;
                break;
            }
            case 43: {
                lDescription = CMD_MOVE_INVENTORY_DESCRIPTION;
                break;
            }
            case 61: {
                lDescription = CMD_STATUS_HUNGER_DESCRIPTION;
                break;
            }
            case 105: {
                lDescription = CMD_STATUS_THIRST_DESCRIPTION;
                break;
            }
            case 122: {
                lDescription = CMD_OPEN_WALL_DESCRIPTION;
                break;
            }
            case 32: {
                lDescription = CMD_SPEEDMODIFIER_DESCRIPTION;
                break;
            }
            case 87: {
                lDescription = CMD_TIMELEFT_DESCRIPTION;
                break;
            }
            case 96: {
                lDescription = CMD_BUILD_MARK_DESCRIPTION;
                break;
            }
            case 112: {
                lDescription = CMD_ADD_STRUCTURE_DESCRIPTION;
                break;
            }
            case 48: {
                lDescription = CMD_REMOVE_STRUCTURE_DESCRIPTION;
                break;
            }
            case 49: {
                lDescription = CMD_ADD_WALL_DESCRIPTION;
                break;
            }
            case 126: {
                lDescription = CMD_REQUEST_ACTIONS_DESCRIPTION;
                break;
            }
            case 127: {
                lDescription = CMD_CLOSE_WALL_DESCRIPTION;
                break;
            }
            case 125: {
                lDescription = CMD_SET_PASSABLE_DESCRIPTION;
                break;
            }
            case -11: {
                lDescription = CMD_SHOW_HTML_DESCRIPTION;
                break;
            }
            case 15: {
                lDescription = CMD_FORM_RESPONSE_DESCRIPTION;
                break;
            }
            case 69: {
                lDescription = CMD_RECEIVED_DESCRIPTION;
                break;
            }
            case 47: {
                lDescription = CMD_RENAME_DESCRIPTION;
                break;
            }
            case 51: {
                lDescription = CMD_TELEPORT_DESCRIPTION;
                break;
            }
            case 116: {
                lDescription = CMD_OPEN_INVENTORY_WINDOW_DESCRIPTION;
                break;
            }
            case 120: {
                lDescription = CMD_CLOSE_INVENTORY_WINDOW_DESCRIPTION;
                break;
            }
            case 119: {
                lDescription = CMD_OPEN_TRADE_WINDOW_DESCRIPTION;
                break;
            }
            case 121: {
                lDescription = CMD_CLOSE_TRADE_WINDOW_DESCRIPTION;
                break;
            }
            case 42: {
                lDescription = CMD_SET_TRADE_AGREE_DESCRIPTION;
                break;
            }
            case 91: {
                lDescription = CMD_TRADE_CHANGED_DESCRIPTION;
                break;
            }
            case 44: {
                lDescription = CMD_RENAME_ITEM_DESCRIPTION;
                break;
            }
            case 12: {
                lDescription = CMD_ADD_FENCE_DESCRIPTION;
                break;
            }
            case 13: {
                lDescription = CMD_REMOVE_FENCE_DESCRIPTION;
                break;
            }
            case 83: {
                lDescription = CMD_OPEN_FENCE_DESCRIPTION;
                break;
            }
            case 86: {
                lDescription = CMD_PLAYSOUND_DESCRIPTION;
                break;
            }
            case -18: {
                lDescription = CMD_STATUS_STRING_DESCRIPTION;
                break;
            }
            case -13: {
                lDescription = CMD_JOIN_GROUP_DESCRIPTION;
                break;
            }
            case 114: {
                lDescription = CMD_PART_GROUP_DESCRIPTION;
                break;
            }
            case 6: {
                lDescription = CMD_SET_CREATURE_ATTITUDE_DESCRIPTION;
                break;
            }
            case 46: {
                lDescription = CMD_WEATHER_UPDATE_DESCRIPTION;
                break;
            }
            case 65: {
                lDescription = CMD_DEAD_DESCRIPTION;
                break;
            }
            case 23: {
                lDescription = CMD_RECONNECT_DESCRIPTION;
                break;
            }
            case 79: {
                lDescription = CMD_CLIMB_DESCRIPTION;
                break;
            }
            case 62: {
                lDescription = CMD_TOGGLE_SWITCH_DESCRIPTION;
                break;
            }
            case 29: {
                lDescription = CMD_MORE_ITEMS_DESCRIPTION;
                break;
            }
            case -16: {
                lDescription = CMD_EMPTY_DESCRIPTION;
                break;
            }
            case 30: {
                lDescription = CMD_CREATURE_LAYER_DESCRIPTION;
                break;
            }
            case -30: {
                lDescription = CMD_TOGGLE_CLIENT_FEATURE_DESCRIPTION;
                break;
            }
            case 106: {
                lDescription = CMD_BML_FORM_DESCRIPTION;
                break;
            }
            case 107: {
                lDescription = CMD_SERVER_TIME_DESCRIPTION;
                break;
            }
            case 109: {
                lDescription = CMD_ATTACH_EFFECT_DESCRIPTION;
                break;
            }
            case 18: {
                lDescription = CMD_UNATTACH_EFFECT_DESCRIPTION;
                break;
            }
            case 101: {
                lDescription = CMD_SET_EQUIPMENT_DESCRIPTION;
                break;
            }
            case 110: {
                lDescription = CMD_USE_ITEM_DESCRIPTION;
                break;
            }
            case 71: {
                lDescription = CMD_STOP_USE_ITEM_DESCRIPTION;
                break;
            }
            case 72: {
                lDescription = CMD_MOVE_CREATURE_AND_SET_Z_DESCRIPTION;
                break;
            }
            case 92: {
                lDescription = CMD_REPAINT_DESCRIPTION;
                break;
            }
            case 74: {
                lDescription = CMD_RESIZE_DESCRIPTION;
                break;
            }
            case 4: {
                lDescription = CMD_CLIENT_QUIT_DESCRIPTION;
                break;
            }
            case 111: {
                lDescription = CMD_ATTACH_CREATURE_DESCRIPTION;
                break;
            }
            case 63: {
                lDescription = CMD_SET_VEHICLE_CONTROLLER_DESCRIPTION;
                break;
            }
            case 24: {
                lDescription = CMD_PLAY_ANIMATION_DESCRIPTION;
                break;
            }
            case 93: {
                lDescription = CMD_MESSAGE_TYPED_DESCRIPTION;
                break;
            }
            case 98: {
                lDescription = CMD_FIGHT_MOVE_OPTIONS_DESCRIPTION;
                break;
            }
            case -14: {
                lDescription = CMD_FIGHT_STATUS_DESCRIPTION;
                break;
            }
            case 28: {
                lDescription = CMD_STUNNED_DESCRIPTION;
                break;
            }
            case -17: {
                lDescription = CMD_SPECIALMOVE_DESCRIPTION;
                break;
            }
            case 25: {
                lDescription = CMD_SET_TARGET_DESCRIPTION;
                break;
            }
            case 26: {
                lDescription = CMD_SET_FIGHTSTYLE_DESCRIPTION;
                break;
            }
            case 11: {
                lDescription = CMD_SET_CREATUREDAMAGE_DESCRIPTION;
                break;
            }
            case 115: {
                lDescription = CMD_PLAYMUSIC_DESCRIPTION;
                break;
            }
            case 117: {
                lDescription = CMD_WINDIMPACT_DESCRIPTION;
                break;
            }
            case 67: {
                lDescription = CMD_ROTATE_DESCRIPTION;
                break;
            }
            case 60: {
                lDescription = CMD_MOUNTSPEED_DESCRIPTION;
                break;
            }
            default: {
                logger.warning("Unknown command byte: '" + Byte.toString(aCommandByte) + '\'');
                lDescription = "Unknown-Command";
            }
        }
        return lDescription;
    }

    public static String getDescriptionForToggle(int aToggleCode) {
        String lDescription;
        switch (aToggleCode) {
            case 0: {
                lDescription = TOGGLE_CLIMBING_DESCRIPTION;
                break;
            }
            case 1: {
                lDescription = TOGGLE_FAITHFUL_DESCRIPTION;
                break;
            }
            case 2: {
                lDescription = TOGGLE_LAWFUL_DESCRIPTION;
                break;
            }
            case 3: {
                lDescription = TOGGLE_STEALTH_DESCRIPTION;
                break;
            }
            case 4: {
                lDescription = TOGGLE_AUTOFIGHT_DESCRIPTION;
                break;
            }
            case 100: {
                lDescription = TOGGLE_ARCHERY_DESCRIPTION;
                break;
            }
            default: {
                logger.warning("Unknown Toggle code: '" + aToggleCode + '\'');
                lDescription = "Unknown-Toggle";
            }
        }
        return lDescription;
    }

    public static boolean isValidCommandType(byte aCommandByte) {
        boolean isValid;
        switch (aCommandByte) {
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
            case 127: {
                isValid = true;
                break;
            }
            default: {
                logger.warning("Unknown command byte: '" + Byte.toString(aCommandByte) + '\'');
                isValid = false;
            }
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
        return (float)(aByteBuffer.get() & 0xFF) / 64.0f;
    }

    public static float getFloat255FromSingleByte(ByteBuffer aByteBuffer) {
        return (float)(aByteBuffer.get() & 0xFF) / 255.0f;
    }

    public static String getString(ByteBuffer aByteBuffer) {
        String lString;
        byte[] lStringBytes = new byte[aByteBuffer.get() & 0xFF];
        aByteBuffer.get(lStringBytes);
        try {
            lString = new String(lStringBytes, UTF_8_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Could not create a UTF-8 String", e);
            lString = "";
        }
        return lString;
    }

    public static void putString(ByteBuffer aByteBuffer, String aValue) {
        if (aValue.length() > 255) {
            logger.warning("Only the first 255 characters will be put in the ByteBuffer, String: " + aValue);
        }
        try {
            byte[] lMessageBytes = aValue.getBytes(UTF_8_ENCODING);
            aByteBuffer.put((byte)lMessageBytes.length);
            aByteBuffer.put(lMessageBytes);
        }
        catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Could not create a UTF-8 String so putting length 0 in the Buffer, String: " + aValue, e);
            aByteBuffer.put((byte)0);
        }
    }
}


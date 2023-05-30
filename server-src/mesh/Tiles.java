package com.wurmonline.mesh;

import com.wurmonline.shared.constants.CounterTypes;
import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class Tiles implements CounterTypes {
   public static final int TILE_COUNT = 256;
   private static final Tiles.Tile[] tiles = new Tiles.Tile[256];
   public static final byte TILE_TYPE_NONE_ID = -1;
   public static final int TILE_TYPE_HOLE = 0;
   public static final int TILE_TYPE_SAND = 1;
   public static final int TILE_TYPE_GRASS = 2;
   public static final int TILE_TYPE_TREE = 3;
   public static final int TILE_TYPE_ROCK = 4;
   public static final int TILE_TYPE_DIRT = 5;
   public static final int TILE_TYPE_CLAY = 6;
   public static final int TILE_TYPE_FIELD = 7;
   public static final int TILE_TYPE_DIRT_PACKED = 8;
   public static final int TILE_TYPE_COBBLESTONE = 9;
   public static final int TILE_TYPE_MYCELIUM = 10;
   public static final int TILE_TYPE_MYCELIUM_TREE = 11;
   public static final int TILE_TYPE_LAVA = 12;
   public static final int TILE_TYPE_ENCHANTED_GRASS = 13;
   public static final int TILE_TYPE_ENCHANTED_TREE = 14;
   public static final int TILE_TYPE_PLANKS = 15;
   public static final int TILE_TYPE_STONE_SLABS = 16;
   public static final int TILE_TYPE_GRAVEL = 17;
   public static final int TILE_TYPE_PEAT = 18;
   public static final int TILE_TYPE_TUNDRA = 19;
   public static final int TILE_TYPE_MOSS = 20;
   public static final int TILE_TYPE_CLIFF = 21;
   public static final int TILE_TYPE_STEPPE = 22;
   public static final int TILE_TYPE_MARSH = 23;
   public static final int TILE_TYPE_TAR = 24;
   public static final int TILE_TYPE_MINE_DOOR_WOOD = 25;
   public static final int TILE_TYPE_MINE_DOOR_STONE = 26;
   public static final int TILE_TYPE_MINE_DOOR_GOLD = 27;
   public static final int TILE_TYPE_MINE_DOOR_SILVER = 28;
   public static final int TILE_TYPE_MINE_DOOR_STEEL = 29;
   public static final int TILE_TYPE_SNOW = 30;
   public static final int TILE_TYPE_BUSH = 31;
   public static final int TILE_TYPE_KELP = 32;
   public static final int TILE_TYPE_REED = 33;
   public static final int TILE_TYPE_ENCHANTED_BUSH = 34;
   public static final int TILE_TYPE_MYCELIUM_BUSH = 35;
   public static final int TILE_TYPE_SLATE_BRICKS = 36;
   public static final int TILE_TYPE_MARBLE_SLABS = 37;
   public static final int TILE_TYPE_LAWN = 38;
   public static final int TILE_TYPE_PLANKS_TARRED = 39;
   public static final int TILE_TYPE_MYCELIUM_LAWN = 40;
   public static final int TILE_TYPE_COBBLESTONE_ROUGH = 41;
   public static final int TILE_TYPE_COBBLESTONE_ROUND = 42;
   public static final int TILE_TYPE_FIELD2 = 43;
   public static final int TILE_TYPE_SANDSTONE_BRICKS = 44;
   public static final int TILE_TYPE_SANDSTONE_SLABS = 45;
   public static final int TILE_TYPE_SLATE_SLABS = 46;
   public static final int TILE_TYPE_MARBLE_BRICKS = 47;
   public static final int TILE_TYPE_POTTERY_BRICKS = 48;
   public static final int TILE_TYPE_PREPARED_BRIDGE = 49;
   public static final int TILE_TYPE_TREE_BIRCH = 100;
   public static final int TILE_TYPE_TREE_PINE = 101;
   public static final int TILE_TYPE_TREE_OAK = 102;
   public static final int TILE_TYPE_TREE_CEDAR = 103;
   public static final int TILE_TYPE_TREE_WILLOW = 104;
   public static final int TILE_TYPE_TREE_MAPLE = 105;
   public static final int TILE_TYPE_TREE_APPLE = 106;
   public static final int TILE_TYPE_TREE_LEMON = 107;
   public static final int TILE_TYPE_TREE_OLIVE = 108;
   public static final int TILE_TYPE_TREE_CHERRY = 109;
   public static final int TILE_TYPE_TREE_CHESTNUT = 110;
   public static final int TILE_TYPE_TREE_WALNUT = 111;
   public static final int TILE_TYPE_TREE_FIR = 112;
   public static final int TILE_TYPE_TREE_LINDEN = 113;
   public static final int TILE_TYPE_TREE_MYCELIUM_BIRCH = 114;
   public static final int TILE_TYPE_TREE_MYCELIUM_PINE = 115;
   public static final int TILE_TYPE_TREE_MYCELIUM_OAK = 116;
   public static final int TILE_TYPE_TREE_MYCELIUM_CEDAR = 117;
   public static final int TILE_TYPE_TREE_MYCELIUM_WILLOW = 118;
   public static final int TILE_TYPE_TREE_MYCELIUM_MAPLE = 119;
   public static final int TILE_TYPE_TREE_MYCELIUM_APPLE = 120;
   public static final int TILE_TYPE_TREE_MYCELIUM_LEMON = 121;
   public static final int TILE_TYPE_TREE_MYCELIUM_OLIVE = 122;
   public static final int TILE_TYPE_TREE_MYCELIUM_CHERRY = 123;
   public static final int TILE_TYPE_TREE_MYCELIUM_CHESTNUT = 124;
   public static final int TILE_TYPE_TREE_MYCELIUM_WALNUT = 125;
   public static final int TILE_TYPE_TREE_MYCELIUM_FIR = 126;
   public static final int TILE_TYPE_TREE_MYCELIUM_LINDEN = 127;
   public static final int TILE_TYPE_TREE_ENCHANTED_BIRCH = 128;
   public static final int TILE_TYPE_TREE_ENCHANTED_PINE = 129;
   public static final int TILE_TYPE_TREE_ENCHANTED_OAK = 130;
   public static final int TILE_TYPE_TREE_ENCHANTED_CEDAR = 131;
   public static final int TILE_TYPE_TREE_ENCHANTED_WILLOW = 132;
   public static final int TILE_TYPE_TREE_ENCHANTED_MAPLE = 133;
   public static final int TILE_TYPE_TREE_ENCHANTED_APPLE = 134;
   public static final int TILE_TYPE_TREE_ENCHANTED_LEMON = 135;
   public static final int TILE_TYPE_TREE_ENCHANTED_OLIVE = 136;
   public static final int TILE_TYPE_TREE_ENCHANTED_CHERRY = 137;
   public static final int TILE_TYPE_TREE_ENCHANTED_CHESTNUT = 138;
   public static final int TILE_TYPE_TREE_ENCHANTED_WALNUT = 139;
   public static final int TILE_TYPE_TREE_ENCHANTED_FIR = 140;
   public static final int TILE_TYPE_TREE_ENCHANTED_LINDEN = 141;
   public static final int TILE_TYPE_BUSH_LAVENDER = 142;
   public static final int TILE_TYPE_BUSH_ROSE = 143;
   public static final int TILE_TYPE_BUSH_THORN = 144;
   public static final int TILE_TYPE_BUSH_GRAPE = 145;
   public static final int TILE_TYPE_BUSH_CAMELLIA = 146;
   public static final int TILE_TYPE_BUSH_OLEANDER = 147;
   public static final int TILE_TYPE_BUSH_MYCELIUM_LAVENDER = 148;
   public static final int TILE_TYPE_BUSH_MYCELIUM_ROSE = 149;
   public static final int TILE_TYPE_BUSH_MYCELIUM_THORN = 150;
   public static final int TILE_TYPE_BUSH_MYCELIUM_GRAPE = 151;
   public static final int TILE_TYPE_BUSH_MYCELIUM_CAMELLIA = 152;
   public static final int TILE_TYPE_BUSH_MYCELIUM_OLEANDER = 153;
   public static final int TILE_TYPE_BUSH_ENCHANTED_LAVENDER = 154;
   public static final int TILE_TYPE_BUSH_ENCHANTED_ROSE = 155;
   public static final int TILE_TYPE_BUSH_ENCHANTED_THORN = 156;
   public static final int TILE_TYPE_BUSH_ENCHANTED_GRAPE = 157;
   public static final int TILE_TYPE_BUSH_ENCHANTED_CAMELLIA = 158;
   public static final int TILE_TYPE_BUSH_ENCHANTED_OLEANDER = 159;
   public static final int TILE_TYPE_BUSH_HAZELNUT = 160;
   public static final int TILE_TYPE_BUSH_MYCELIUM_HAZELNUT = 161;
   public static final int TILE_TYPE_BUSH_ENCHANTED_HAZELNUT = 162;
   public static final int TILE_TYPE_TREE_ORANGE = 163;
   public static final int TILE_TYPE_TREE_MYCELIUM_ORANGE = 164;
   public static final int TILE_TYPE_TREE_ENCHANTED_ORANGE = 165;
   public static final int TILE_TYPE_BUSH_RASPBERRY = 166;
   public static final int TILE_TYPE_BUSH_MYCELIUM_RASPBERRY = 167;
   public static final int TILE_TYPE_BUSH_ENCHANTED_RASPBERRY = 168;
   public static final int TILE_TYPE_BUSH_BLUEBERRY = 169;
   public static final int TILE_TYPE_BUSH_MYCELIUM_BLUEBERRY = 170;
   public static final int TILE_TYPE_BUSH_ENCHANTED_BLUEBERRY = 171;
   public static final int TILE_TYPE_BUSH_LINGONBERRY = 172;
   public static final int TILE_TYPE_CAVE = 200;
   public static final int TILE_TYPE_CAVE_EXIT = 201;
   public static final int TILE_TYPE_CAVE_WALL = 202;
   public static final int TILE_TYPE_CAVE_WALL_REINFORCED = 203;
   public static final int TILE_TYPE_CAVE_WALL_LAVA = 204;
   public static final int TILE_TYPE_CAVE_WALL_SLATE = 205;
   public static final int TILE_TYPE_CAVE_WALL_MARBLE = 206;
   public static final int TILE_TYPE_CAVE_FLOOR_REINFORCED = 207;
   public static final int TILE_TYPE_CAVE_WALL_ORE_GOLD = 220;
   public static final int TILE_TYPE_CAVE_WALL_ORE_SILVER = 221;
   public static final int TILE_TYPE_CAVE_WALL_ORE_IRON = 222;
   public static final int TILE_TYPE_CAVE_WALL_ORE_COPPER = 223;
   public static final int TILE_TYPE_CAVE_WALL_ORE_LEAD = 224;
   public static final int TILE_TYPE_CAVE_WALL_ORE_ZINC = 225;
   public static final int TILE_TYPE_CAVE_WALL_ORE_TIN = 226;
   public static final int TILE_TYPE_CAVE_WALL_ORE_ADAMANTINE = 227;
   public static final int TILE_TYPE_CAVE_WALL_ORE_GLIMMERSTEEL = 228;
   public static final int TILE_TYPE_CAVE_WALL_ROCKSALT = 229;
   public static final int TILE_TYPE_CAVE_WALL_SANDSTONE = 230;
   public static final int TILE_TYPE_CAVE_WALL_STONE_REINFORCED = 231;
   public static final int TILE_TYPE_CAVE_WALL_SLATE_REINFORCED = 232;
   public static final int TILE_TYPE_CAVE_WALL_POTTERY_REINFORCED = 233;
   public static final int TILE_TYPE_CAVE_WALL_ROUNDED_STONE_REINFORCED = 234;
   public static final int TILE_TYPE_CAVE_WALL_SANDSTONE_REINFORCED = 235;
   public static final int TILE_TYPE_CAVE_WALL_RENDERED_REINFORCED = 236;
   public static final int TILE_TYPE_CAVE_WALL_MARBLE_REINFORCED = 237;
   public static final int TILE_TYPE_CAVE_WALL_WOOD_REINFORCED = 238;
   public static final int TILE_TYPE_CAVE_WALL_PART_STONE_REINFORCED = 239;
   public static final int TILE_TYPE_CAVE_WALL_PART_SLATE_REINFORCED = 240;
   public static final int TILE_TYPE_CAVE_WALL_PART_POTTERY_REINFORCED = 241;
   public static final int TILE_TYPE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED = 242;
   public static final int TILE_TYPE_CAVE_WALL_PART_SANDSTONE_REINFORCED = 243;
   public static final int TILE_TYPE_CAVE_WALL_PART_MARBLE_REINFORCED = 244;
   public static final int TILE_TYPE_CAVE_WALL_PART_WOOD_REINFORCED = 245;
   public static final int TILE_TYPE_CAVE_PREPARED_FLOOR_REINFORCED = 246;
   public static final int CAVE_SIDE_FLOOR = 0;
   public static final int CAVE_SIDE_ROOF = 1;
   public static final int CAVE_SIDE_EAST = 2;
   public static final int CAVE_SIDE_SOUTH = 3;
   public static final int CAVE_SIDE_WEST = 4;
   public static final int CAVE_SIDE_NORTH = 5;
   public static final int CAVE_SIDE_MINE_DOOR = 6;
   public static final int CAVE_SIDE_CORNER = 7;
   public static final int CAVE_SIDE_BORDER_NORTH = 8;
   public static final int CAVE_SIDE_BORDER_WEST = 9;
   public static final int CAVE_SIDE_BORDER_SOUTH = 10;
   public static final int CAVE_SIDE_BORDER_EAST = 11;
   public static final int CAT_ALL = 0;
   public static final int CAT_BUSHES = 1;
   public static final int CAT_CAVE = 2;
   public static final int CAT_MINEDOORS = 3;
   public static final int CAT_NORMAL = 4;
   public static final int CAT_PAVING = 5;
   public static final int CAT_SURFACE = 6;
   public static final int CAT_TREES = 7;
   public static final int BAD_TILE = -1;
   public static final int BUFFER_SIDE_SIZE = 512;
   public static final int BUFFER_SIDE_MASK = 511;
   public static final int WORLD_SIDE_SIZE = 4096;
   public static final int TILE_WIDTH = 4;
   public static final float DIRT_FACTOR = 10.0F;
   public static final int FLOOR_FACTOR = 30;
   public static final int FLOOR_FACTOR_METERS = 3;
   public static final float FLOOR_THICKNESS = 0.25F;
   public static final byte INFILTRATION_NONE = 0;
   public static final byte INFILTRATION_SLOW = 1;
   public static final byte INFILTRATION_MODERATE = 2;
   public static final byte INFILTRATION_RAPID = 3;
   public static final byte RESERVOIR_SMALL = 0;
   public static final byte RESERVOIR_MEDIUM = 1;
   public static final byte RESERVOIR_LARGE = 2;
   public static final byte LEAKAGE_SLOW = 0;
   public static final byte LEAKAGE_MODERATE = 1;
   public static final byte LEAKAGE_RAPID = 2;

   private Tiles() {
   }

   public static Tiles.Tile getTile(int id) {
      if (tiles[id & 0xFF] == null) {
         int var1 = Tiles.Tile.values().length;
      }

      return tiles[id & 0xFF];
   }

   public static Tiles.Tile getTile(byte id) {
      return getTile(id & 255);
   }

   public static int encode(float height, byte type, byte data) {
      return encode((short)((int)(height * 10.0F)), type, data);
   }

   public static int encode(short height, byte type, byte data) {
      return ((type & 0xFF) << 24) + ((data & 0xFF) << 16) + (height & 65535);
   }

   public static int encode(short height, short tileData) {
      return ((tileData & 65535) << 16) + (height & 65535);
   }

   public static short decodeHeight(int encodedTile) {
      return (short)(encodedTile & 65535);
   }

   public static short decodeTileX(long clientWurmId) {
      return (short)((int)(clientWurmId >> 32 & 65535L));
   }

   public static int decodeTileY(long clientWurmId) {
      return (short)((int)(clientWurmId >> 16 & 65535L));
   }

   public static int decodeHeightOffset(long clientWurmId) {
      return (short)((int)(clientWurmId >> 48 & 65535L));
   }

   public static int decodeFloorLevel(long clientWurmId) {
      return decodeHeightOffset(clientWurmId) / 30;
   }

   public static Tiles.TileBorderDirection decodeDirection(long clientWurmId) {
      byte code = (byte)((int)(clientWurmId >> 8 & 15L));
      if (code == 0) {
         return Tiles.TileBorderDirection.DIR_HORIZ;
      } else if (code == 2) {
         return Tiles.TileBorderDirection.DIR_DOWN;
      } else {
         return code == 4 ? Tiles.TileBorderDirection.CORNER : Tiles.TileBorderDirection.DIR_HORIZ;
      }
   }

   public static byte decodeLayer(long clientWurmId) {
      byte code = (byte)((int)(clientWurmId >> 12 & 15L));
      return (byte)(code == 0 ? 0 : -1);
   }

   public static short decodeTileData(int encodedTile) {
      return (short)(encodedTile >> 16 & 65535);
   }

   public static float decodeHeightAsFloat(int encodedTile) {
      return (float)((short)(encodedTile & 65535)) / 10.0F;
   }

   public static byte decodeType(int encodedTile) {
      int type = encodedTile >> 24 & 0xFF;
      return (byte)type;
   }

   public static byte decodeData(int encodedTile) {
      return (byte)(encodedTile >> 16 & 0xFF);
   }

   public static boolean isRoadType(int encodedTile) {
      return isRoadType((byte)(encodedTile >> 24 & 0xFF));
   }

   public static boolean isRoadType(byte type) {
      return type == Tiles.Tile.TILE_COBBLESTONE.id
         || type == Tiles.Tile.TILE_COBBLESTONE_ROUGH.id
         || type == Tiles.Tile.TILE_COBBLESTONE_ROUND.id
         || type == Tiles.Tile.TILE_STONE_SLABS.id
         || type == Tiles.Tile.TILE_GRAVEL.id
         || type == Tiles.Tile.TILE_POTTERY_BRICKS.id
         || type == Tiles.Tile.TILE_SLATE_SLABS.id
         || type == Tiles.Tile.TILE_SLATE_BRICKS.id
         || type == Tiles.Tile.TILE_SANDSTONE_SLABS.id
         || type == Tiles.Tile.TILE_SANDSTONE_BRICKS.id
         || type == Tiles.Tile.TILE_MARBLE_SLABS.id
         || type == Tiles.Tile.TILE_MARBLE_BRICKS.id
         || type == Tiles.Tile.TILE_PLANKS.id
         || type == Tiles.Tile.TILE_PLANKS_TARRED.id;
   }

   public static byte getSecondTypeforRoad(int _type) {
      int toReturn;
      if (isTundra(_type)) {
         toReturn = 19;
      } else if (isEnchanted(_type)) {
         toReturn = 13;
      } else if (isMycelium(_type)) {
         toReturn = 10;
      } else if (isNormal(_type)) {
         toReturn = 2;
      } else {
         switch(_type & 0xFF) {
            case 0:
               toReturn = 9;
               break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 11:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 22:
            case 23:
            case 24:
            default:
               toReturn = _type;
               break;
            case 7:
            case 12:
               toReturn = 5;
               break;
            case 21:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
               toReturn = 4;
         }
      }

      return (byte)toReturn;
   }

   public static float shortHeightToFloat(short s) {
      return (float)s / 10.0F;
   }

   public static boolean isSolidCave(byte _type) {
      boolean toReturn;
      switch(_type & 0xFF) {
         case 202:
         case 203:
         case 204:
         case 205:
         case 206:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 229:
         case 230:
         case 231:
         case 232:
         case 233:
         case 234:
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
            toReturn = true;
            break;
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isReinforcedCaveWall(byte _id) {
      boolean toReturn;
      switch(_id & 0xFF) {
         case 203:
         case 231:
         case 232:
         case 233:
         case 234:
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
            toReturn = true;
            break;
         case 204:
         case 205:
         case 206:
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 229:
         case 230:
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isSolidCave(Tiles.Tile theTile) {
      return theTile.isSolidCave();
   }

   public static boolean isOreCave(byte _id) {
      boolean toReturn;
      switch(_id & 0xFF) {
         case 205:
         case 206:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 230:
            toReturn = true;
            break;
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         case 229:
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isReinforcedCave(byte _id) {
      boolean toReturn;
      switch(_id & 0xFF) {
         case 203:
         case 205:
         case 206:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 230:
         case 231:
         case 232:
         case 233:
         case 234:
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
            toReturn = true;
            break;
         case 204:
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         case 217:
         case 218:
         case 219:
         case 229:
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isReinforcedFloor(byte _id) {
      switch(_id & 0xFF) {
         case 207:
         case 246:
            return true;
         default:
            return isRoadType(_id);
      }
   }

   public static boolean isMineDoor(int _id) {
      return _id == 26 || isVisibleMineDoor(_id);
   }

   public static boolean isMineDoor(byte _id) {
      return isMineDoor(_id & 255);
   }

   public static boolean isMineDoor(Tiles.Tile tile) {
      return tile != null && isMineDoor(tile.intId);
   }

   public static boolean isEnchanted(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 13:
         case 14:
         case 34:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 159:
         case 165:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isEnchantedBush(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 34:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 159:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isEnchantedTree(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 14:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 165:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isMycelium(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 10:
         case 11:
         case 35:
         case 40:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 161:
         case 164:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isMyceliumBush(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 35:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 161:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isMyceliumTree(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 11:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 164:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isTundra(int tileId) {
      switch(tileId & 0xFF) {
         case 19:
         case 172:
            return true;
         default:
            return false;
      }
   }

   public static boolean isNormal(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 2:
         case 3:
         case 31:
         case 38:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 160:
         case 163:
         case 166:
         case 169:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isNormalBush(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 31:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 160:
         case 166:
         case 169:
         case 172:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isNormalTree(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 3:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 163:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static int toNormal(int tileId) {
      switch(tileId & 0xFF) {
         case 10:
         case 13:
            return 2;
         case 11:
         case 14:
            return 3;
         case 12:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 36:
         case 37:
         case 38:
         case 39:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
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
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 160:
         case 163:
         case 166:
         case 169:
         default:
            return tileId;
         case 34:
         case 35:
            return 31;
         case 40:
            return 38;
         case 114:
         case 128:
            return 100;
         case 115:
         case 129:
            return 101;
         case 116:
         case 130:
            return 102;
         case 117:
         case 131:
            return 103;
         case 118:
         case 132:
            return 104;
         case 119:
         case 133:
            return 105;
         case 120:
         case 134:
            return 106;
         case 121:
         case 135:
            return 107;
         case 122:
         case 136:
            return 108;
         case 123:
         case 137:
            return 109;
         case 124:
         case 138:
            return 110;
         case 125:
         case 139:
            return 111;
         case 126:
         case 140:
            return 112;
         case 127:
         case 141:
            return 113;
         case 148:
         case 154:
            return 142;
         case 149:
         case 155:
            return 143;
         case 150:
         case 156:
            return 144;
         case 151:
         case 157:
            return 145;
         case 152:
         case 158:
            return 146;
         case 153:
         case 159:
            return 147;
         case 161:
         case 162:
            return 160;
         case 164:
         case 165:
            return 163;
         case 167:
         case 168:
            return 166;
         case 170:
         case 171:
            return 169;
      }
   }

   public static int toEnchanted(int tileId) {
      switch(tileId & 0xFF) {
         case 2:
            return 13;
         case 3:
            return 14;
         case 31:
            return 34;
         case 100:
            return 128;
         case 101:
            return 129;
         case 102:
            return 130;
         case 103:
            return 131;
         case 104:
            return 132;
         case 105:
            return 133;
         case 106:
            return 134;
         case 107:
            return 135;
         case 108:
            return 136;
         case 109:
            return 137;
         case 110:
            return 138;
         case 111:
            return 139;
         case 112:
            return 140;
         case 113:
            return 141;
         case 142:
            return 154;
         case 143:
            return 155;
         case 144:
            return 156;
         case 145:
            return 157;
         case 146:
            return 158;
         case 147:
            return 159;
         case 160:
            return 162;
         case 163:
            return 165;
         case 166:
            return 168;
         case 169:
            return 171;
         case 172:
            return 172;
         default:
            return tileId;
      }
   }

   public static int toMycelium(int tileId) {
      switch(tileId & 0xFF) {
         case 2:
            return 10;
         case 3:
            return 11;
         case 31:
            return 35;
         case 38:
            return 40;
         case 100:
            return 114;
         case 101:
            return 115;
         case 102:
            return 116;
         case 103:
            return 117;
         case 104:
            return 118;
         case 105:
            return 119;
         case 106:
            return 120;
         case 107:
            return 121;
         case 108:
            return 122;
         case 109:
            return 123;
         case 110:
            return 124;
         case 111:
            return 125;
         case 112:
            return 126;
         case 113:
            return 127;
         case 142:
            return 148;
         case 143:
            return 149;
         case 144:
            return 150;
         case 145:
            return 151;
         case 146:
            return 152;
         case 147:
            return 153;
         case 160:
            return 161;
         case 163:
            return 164;
         case 166:
            return 167;
         case 169:
            return 170;
         case 172:
            return 172;
         default:
            return tileId;
      }
   }

   public static boolean isBush(byte tileId) {
      switch(tileId & 0xFF) {
         case 31:
         case 34:
         case 35:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 159:
         case 160:
         case 161:
         case 162:
         case 166:
         case 167:
         case 168:
         case 169:
         case 170:
         case 171:
         case 172:
            return true;
         case 32:
         case 33:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
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
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 163:
         case 164:
         case 165:
         default:
            return false;
      }
   }

   public static boolean isGrassType(byte tileId) {
      switch(tileId & 0xFF) {
         case 2:
         case 32:
         case 33:
         case 38:
            return true;
         default:
            return false;
      }
   }

   public static boolean isTree(byte tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 3:
         case 11:
         case 14:
         case 100:
         case 101:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         case 115:
         case 116:
         case 117:
         case 118:
         case 119:
         case 120:
         case 121:
         case 122:
         case 123:
         case 124:
         case 125:
         case 126:
         case 127:
         case 128:
         case 129:
         case 130:
         case 131:
         case 132:
         case 133:
         case 134:
         case 135:
         case 136:
         case 137:
         case 138:
         case 139:
         case 140:
         case 141:
         case 163:
         case 164:
         case 165:
            toReturn = true;
            break;
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 12:
         case 13:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
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
         case 70:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 99:
         case 142:
         case 143:
         case 144:
         case 145:
         case 146:
         case 147:
         case 148:
         case 149:
         case 150:
         case 151:
         case 152:
         case 153:
         case 154:
         case 155:
         case 156:
         case 157:
         case 158:
         case 159:
         case 160:
         case 161:
         case 162:
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean canSpawnTree(byte tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 2:
         case 5:
         case 10:
         case 19:
         case 22:
            toReturn = true;
            break;
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isVisibleMineDoor(int tileId) {
      boolean toReturn;
      switch(tileId & 0xFF) {
         case 25:
         case 27:
         case 28:
         case 29:
            toReturn = true;
            break;
         case 26:
         default:
            toReturn = false;
      }

      return toReturn;
   }

   public static boolean isVisibleMineDoor(byte tileId) {
      return isVisibleMineDoor(tileId & 255);
   }

   public static boolean isVisibleMineDoor(Tiles.Tile tile) {
      return isVisibleMineDoor(tile.intId);
   }

   public static long getBorderObjectId(int x, int y, int heightOffset, byte layer, int dir, byte type) {
      int layerBit = layer == 0 ? 0 : 128;
      return ((long)heightOffset << 48) + ((long)x << 32) + ((long)y << 16) + ((long)(layerBit + dir) << 8) + (long)(type & 255);
   }

   public static long getBorderId(int x, int y, int heightOffset, byte layer, int dir) {
      return getBorderObjectId(x, y, heightOffset, layer, dir, (byte)12);
   }

   public static long getHouseWallId(int x, int y, int heightOffset, byte layer, byte dir) {
      return getBorderObjectId(x, y, heightOffset, layer, dir, (byte)5);
   }

   public static long getFenceId(int x, int y, int heightOffset, byte layer, byte dir) {
      return getBorderObjectId(x, y, heightOffset, layer, dir, (byte)7);
   }

   public static long getFloorId(int x, int y, int heightOffset, byte layer) {
      return getBorderObjectId(x, y, heightOffset, layer, 0, (byte)23);
   }

   public static long getTileCornerId(int x, int y, int heightOffset, byte layer) {
      return getBorderObjectId(x, y, heightOffset, layer, Tiles.TileBorderDirection.CORNER.getCode(), (byte)27);
   }

   public static long getBridgePartId(int x, int y, int realHeight, byte layer, byte dir) {
      return getBorderObjectId(x, y, realHeight, layer, dir, (byte)28);
   }

   public static byte encodeTreeData(FoliageAge foliageAge, boolean hasFruit, boolean centre, GrassData.GrowthTreeStage growthTreeStage) {
      return encodeTreeData(foliageAge.getAgeId(), hasFruit, centre, growthTreeStage.getEncodedData());
   }

   public static byte encodeTreeData(byte foliageAge, boolean hasFruit, boolean centre, GrassData.GrowthTreeStage growthTreeStage) {
      return encodeTreeData(foliageAge, hasFruit, centre, growthTreeStage.getEncodedData());
   }

   public static byte encodeTreeData(byte treeAge, boolean hasFruit, boolean centre, byte growthTreeStage) {
      byte tileData = (byte)(treeAge << 4 | growthTreeStage);
      if (hasFruit) {
         tileData = (byte)(tileData | 8);
      }

      if (centre) {
         tileData = (byte)(tileData | 4);
      }

      return tileData;
   }

   private static long getTileId(int x, int y, int heightOffset, byte type) {
      return ((long)heightOffset << 48) + ((long)x << 32) + ((long)y << 16) + (long)type;
   }

   public static long getTileId(int x, int y, int heightOffset) {
      return getTileId(x, y, heightOffset, (byte)3);
   }

   public static long getTileId(int x, int y, int heightOffset, boolean onSurface) {
      return onSurface ? getTileId(x, y, heightOffset, (byte)3) : getTileId(x, y, heightOffset, (byte)17);
   }

   private static enum Flag {
      USESNEWDATA,
      ALIGNED,
      TREE,
      BUSH,
      NORMAL,
      MYCELIUM,
      ENCHANTED,
      GRASS,
      ROAD,
      FLATROAD,
      TUNDRA,
      CAVE,
      CAVEDOOR,
      VISIBLE_CAVEDOOR,
      SOLIDCAVE,
      REINFORCEDCAVE,
      ORECAVE,
      REINFORCEDFLOOR,
      BOTANIZE,
      FORAGE,
      BIRCH,
      PINE,
      OAK,
      CEDAR,
      WILLOW,
      MAPLE,
      APPLE,
      LEMON,
      OLIVE,
      CHERRY,
      CHESTNUT,
      WALNUT,
      FIR,
      LINDEN,
      LAVENDER,
      ROSE,
      THORN,
      GRAPE,
      CAMELLIA,
      OLEANDER,
      HAZELNUT,
      ORANGE,
      RASPBERRY,
      LINGONBERRY,
      BLUEBERRY;
   }

   public static enum Tile {
      TILE_HOLE(0, "Hole", "#000000", 1.0F, "img.texture.terrain.hole", new Tiles.Flag[]{Tiles.Flag.ALIGNED}, 60, (byte)3, (byte)2, (byte)2),
      TILE_SAND(1, "Sand", "#A0936D", 0.8F, "img.texture.terrain.sand", new Tiles.Flag[0], 60, (byte)3, (byte)2, (byte)2),
      TILE_GRASS(
         2,
         "Grass",
         "#366503",
         0.75F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.GRASS, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE},
         60,
         (byte)2,
         (byte)1,
         (byte)0
      ),
      TILE_TREE(
         3,
         "TreePosition",
         "TreePosition (Superseded)",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.TREE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ROCK(4, "Rock", "#726E6B", 1.0F, "img.texture.terrain.rock", new Tiles.Flag[0], 60, (byte)0, (byte)0, (byte)0),
      TILE_DIRT(5, "Dirt", "#4B3F2F", 0.8F, "img.texture.terrain.dirt", new Tiles.Flag[0], 60, (byte)2, (byte)0, (byte)0),
      TILE_CLAY(6, "Clay", "#717C76", 0.6F, "img.texture.terrain.clay", new Tiles.Flag[0], 60, (byte)0, (byte)0, (byte)0),
      TILE_FIELD(7, "Field", "#473C2F", 0.8F, "img.texture.terrain.farm", new Tiles.Flag[]{Tiles.Flag.ALIGNED}, 60, (byte)1, (byte)1, (byte)0),
      TILE_DIRT_PACKED(8, "Packed dirt", "#4B3F2F", 0.9F, "img.texture.terrain.dirt.packed", new Tiles.Flag[0], 60, (byte)1, (byte)1, (byte)0),
      TILE_COBBLESTONE(9, "Cobblestone", "#5C5349", 1.0F, "img.texture.terrain.cobblestone", new Tiles.Flag[]{Tiles.Flag.ROAD}, 60, (byte)0, (byte)1, (byte)0),
      TILE_MYCELIUM(
         10,
         "Mycelium",
         "#470233",
         0.75F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.MYCELIUM, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)0
      ),
      TILE_MYCELIUM_TREE(
         11,
         "Infected tree",
         "Infected tree (Superseded)",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.TREE, Tiles.Flag.MYCELIUM, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_LAVA(12, "Lava", "#d7331e", 0.5F, "img.texture.terrain.lava", new Tiles.Flag[0], 60, (byte)3, (byte)2, (byte)2),
      TILE_ENCHANTED_GRASS(
         13, "Enchanted grass", "#2d5d2b", 0.8F, "img.texture.terrain.grass.enchanted", new Tiles.Flag[]{Tiles.Flag.ENCHANTED}, 60, (byte)2, (byte)1, (byte)0
      ),
      TILE_ENCHANTED_TREE(
         14,
         "Enchanted tree",
         "Enchanted tree (Superseded)",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.TREE, Tiles.Flag.ENCHANTED},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_PLANKS(
         15,
         "Wooden planks",
         "#726650",
         1.0F,
         "img.texture.terrain.planks",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)0,
         (byte)0
      ),
      TILE_STONE_SLABS(
         16,
         "Stone slabs",
         "#636363",
         1.0F,
         "img.texture.terrain.stoneslabs",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)0,
         (byte)0
      ),
      TILE_GRAVEL(17, "Gravel", "#4f4a40", 0.9F, "img.texture.terrain.gravel", new Tiles.Flag[]{Tiles.Flag.ROAD}, 60, (byte)1, (byte)1, (byte)0),
      TILE_PEAT(18, "Peat", "#362720", 0.7F, "img.texture.terrain.peat", new Tiles.Flag[]{Tiles.Flag.BOTANIZE}, 60, (byte)1, (byte)2, (byte)0),
      TILE_TUNDRA(
         19, "Tundra", "#76876d", 0.7F, "img.texture.terrain.tundra", new Tiles.Flag[]{Tiles.Flag.FORAGE, Tiles.Flag.TUNDRA}, 60, (byte)1, (byte)1, (byte)0
      ),
      TILE_MOSS(20, "Moss", "#6a8e38", 0.7F, "img.texture.terrain.moss", new Tiles.Flag[]{Tiles.Flag.BOTANIZE}, 60, (byte)1, (byte)1, (byte)0),
      TILE_CLIFF(21, "Cliff", "#9b9794", 0.6F, "img.texture.terrain.rock.cliff", new Tiles.Flag[0], 60, (byte)0, (byte)1, (byte)0),
      TILE_STEPPE(
         22, "Steppe", "#727543", 0.8F, "img.texture.terrain.steppe", new Tiles.Flag[]{Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE}, 60, (byte)1, (byte)1, (byte)0
      ),
      TILE_MARSH(
         23, "Marsh", "#2b6548", 0.6F, "img.texture.terrain.marsh", new Tiles.Flag[]{Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE}, 60, (byte)3, (byte)2, (byte)0
      ),
      TILE_TAR(24, "Tar", "#121528", 0.4F, "img.texture.terrain.tar", new Tiles.Flag[0], 60, (byte)0, (byte)1, (byte)0),
      TILE_MINE_DOOR_WOOD(
         25,
         "Mine door",
         "Wood mine door",
         "#293A02",
         0.8F,
         "img.texture.terrain.minedoor.wood",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.VISIBLE_CAVEDOOR},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_MINE_DOOR_STONE(
         26, "Rock", "Stone mine door", "#726E6B", 1.0F, "img.texture.terrain.rock", new Tiles.Flag[]{Tiles.Flag.CAVEDOOR}, 60, (byte)0, (byte)1, (byte)0
      ),
      TILE_MINE_DOOR_GOLD(
         27,
         "Mine door",
         "Gold mine door",
         "#1a3418",
         0.8F,
         "img.texture.terrain.minedoor.gold",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.VISIBLE_CAVEDOOR},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_MINE_DOOR_SILVER(
         28,
         "Mine door",
         "Silver mine door",
         "#362720",
         0.8F,
         "img.texture.terrain.minedoor.silver",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.VISIBLE_CAVEDOOR},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_MINE_DOOR_STEEL(
         29,
         "Mine door",
         "Steel mine door",
         "#2b6548",
         0.8F,
         "img.texture.terrain.minedoor.steel",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.VISIBLE_CAVEDOOR},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_SNOW(30, "Snow", "Snow", "#FFFFFF", 0.5F, "img.texture.terrain.grass.winter", new Tiles.Flag[0], 60, (byte)2, (byte)1, (byte)0),
      TILE_BUSH(
         31,
         "BushPosition",
         "BushPosition (Superseded)",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.BUSH, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE},
         60,
         (byte)2,
         (byte)1,
         (byte)0
      ),
      TILE_KELP(32, "Kelp", "#366503", 0.75F, "img.texture.terrain.grass.kelp", new Tiles.Flag[]{Tiles.Flag.GRASS}, 60, (byte)2, (byte)1, (byte)0),
      TILE_REED(33, "Reed", "#366503", 0.75F, "img.texture.terrain.grass.reed", new Tiles.Flag[]{Tiles.Flag.GRASS}, 60, (byte)2, (byte)1, (byte)0),
      TILE_ENCHANTED_BUSH(
         34,
         "Enchanted bush",
         "Enchanted bush (Superseded)",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.BUSH, Tiles.Flag.ENCHANTED},
         60,
         (byte)2,
         (byte)1,
         (byte)0
      ),
      TILE_MYCELIUM_BUSH(
         35,
         "Infected bush",
         "Infected bush (Superseded)",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.BUSH, Tiles.Flag.MYCELIUM, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)0
      ),
      TILE_SLATE_BRICKS(
         36,
         "Slate bricks",
         "#5C5349",
         1.0F,
         "img.texture.terrain.slatebricks",
         new Tiles.Flag[]{Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_MARBLE_SLABS(
         37,
         "Marble slabs",
         "#636363",
         1.0F,
         "img.texture.terrain.marbleslabs",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_LAWN(38, "Lawn", "#366503", 0.8F, "img.texture.terrain.grass.lawn", new Tiles.Flag[]{Tiles.Flag.NORMAL}, 60, (byte)1, (byte)1, (byte)0),
      TILE_PLANKS_TARRED(
         39,
         "Wooden planks",
         "Tarred wooden planks",
         "#726650",
         1.0F,
         "img.texture.terrain.planks.tarred",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_MYCELIUM_LAWN(
         40, "Mycelium Lawn", "#470233", 0.75F, "img.texture.terrain.mycelium.lawn", new Tiles.Flag[]{Tiles.Flag.MYCELIUM}, 60, (byte)1, (byte)1, (byte)0
      ),
      TILE_COBBLESTONE_ROUGH(
         41, "Rough cobblestone", "#5C5349", 1.0F, "img.texture.terrain.cobble2", new Tiles.Flag[]{Tiles.Flag.ROAD}, 60, (byte)0, (byte)1, (byte)0
      ),
      TILE_COBBLESTONE_ROUND(
         42, "Round cobblestone", "#5C5349", 1.0F, "img.texture.terrain.cobble3", new Tiles.Flag[]{Tiles.Flag.ROAD}, 60, (byte)0, (byte)1, (byte)0
      ),
      TILE_FIELD2(43, "Field", "#473C2F", 0.8F, "img.texture.terrain.farm", new Tiles.Flag[]{Tiles.Flag.ALIGNED}, 60, (byte)1, (byte)1, (byte)0),
      TILE_SANDSTONE_BRICKS(
         44,
         "Sandstone bricks",
         "#5C5349",
         1.0F,
         "img.texture.terrain.sandstonebricks",
         new Tiles.Flag[]{Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_SANDSTONE_SLABS(
         45,
         "Sandstone slabs",
         "#5C5349",
         1.0F,
         "img.texture.terrain.sandstoneslabs",
         new Tiles.Flag[]{Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_SLATE_SLABS(
         46,
         "Slate slabs",
         "#636363",
         1.0F,
         "img.texture.terrain.slateslabs",
         new Tiles.Flag[]{Tiles.Flag.ALIGNED, Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_MARBLE_BRICKS(
         47,
         "Marble bricks",
         "#5C5349",
         1.0F,
         "img.texture.terrain.marblebricks",
         new Tiles.Flag[]{Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_POTTERY_BRICKS(
         48,
         "Pottery bricks",
         "#5C5349",
         1.0F,
         "img.texture.terrain.potterybricks",
         new Tiles.Flag[]{Tiles.Flag.ROAD, Tiles.Flag.FLATROAD},
         60,
         (byte)0,
         (byte)1,
         (byte)0
      ),
      TILE_PREPARED_BRIDGE(49, "Prepared for paving", "#636363", 1.0F, "img.texture.terrain.prepared", new Tiles.Flag[0], 60, (byte)0, (byte)0, (byte)0),
      TILE_TREE_BIRCH(
         100,
         "Birch tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.BIRCH, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_PINE(
         101,
         "Pine tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.PINE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_OAK(
         102,
         "Oak tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.OAK, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_CEDAR(
         103,
         "Cedar tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.CEDAR, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_WILLOW(
         104,
         "Willow tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.WILLOW, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_MAPLE(
         105,
         "Maple tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.MAPLE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_APPLE(
         106,
         "Apple tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.APPLE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_LEMON(
         107,
         "Lemon tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.LEMON, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_OLIVE(
         108,
         "Olive tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.OLIVE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_CHERRY(
         109,
         "Cherry tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.CHERRY, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_CHESTNUT(
         110,
         "Chestnut tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.CHESTNUT, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_WALNUT(
         111,
         "Walnut tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.WALNUT, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_FIR(
         112,
         "Fir tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.FIR, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_LINDEN(
         113,
         "Linden tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.LINDEN, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_BIRCH(
         114,
         "Infected birch tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.BIRCH, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_PINE(
         115,
         "Infected pine tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.PINE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_OAK(
         116,
         "Infected oak tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.OAK, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_CEDAR(
         117,
         "Infected cedar tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.CEDAR, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_WILLOW(
         118,
         "Infected willow tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.WILLOW, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_MAPLE(
         119,
         "Infected maple tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.MAPLE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_APPLE(
         120,
         "Infected apple tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.APPLE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_LEMON(
         121,
         "Infected lemon tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.LEMON, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_OLIVE(
         122,
         "Infected olive tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.OLIVE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_CHERRY(
         123,
         "Infected cherry tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.CHERRY, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_CHESTNUT(
         124,
         "Infected chestnut tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.CHESTNUT, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_WALNUT(
         125,
         "Infected walnut tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.WALNUT, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_FIR(
         126,
         "Infected fir tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.FIR, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_LINDEN(
         127,
         "Infected linden tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.LINDEN, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_BIRCH(
         128,
         "Enchanted birch tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.BIRCH, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_PINE(
         129,
         "Enchanted pine tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.PINE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_OAK(
         130,
         "Enchanted oak tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.OAK, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_CEDAR(
         131,
         "Enchanted cedar tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.CEDAR, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_WILLOW(
         132,
         "Enchanted willow tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.WILLOW, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_MAPLE(
         133,
         "Enchanted maple tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.MAPLE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_APPLE(
         134,
         "Enchanted apple tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.APPLE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_LEMON(
         135,
         "Enchanted lemon tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.LEMON, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_OLIVE(
         136,
         "Enchanted olive tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.OLIVE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_CHERRY(
         137,
         "Enchanted cherry tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.CHERRY, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_CHESTNUT(
         138,
         "Enchanted chestnut tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.CHESTNUT, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_WALNUT(
         139,
         "Enchanted walnut tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.WALNUT, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_FIR(
         140,
         "Enchanted fir tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.FIR, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_LINDEN(
         141,
         "Enchanted linden tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.LINDEN, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_LAVENDER(
         142,
         "Lavender bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.LAVENDER, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_ROSE(
         143,
         "Rose bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.ROSE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_THORN(
         144,
         "Thorn bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.THORN, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_GRAPE(
         145,
         "Grape bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.GRAPE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_CAMELLIA(
         146,
         "Camellia bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.CAMELLIA, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_OLEANDER(
         147,
         "Oleander bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.OLEANDER, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_LAVENDER(
         148,
         "Infected lavender bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.LAVENDER, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_ROSE(
         149,
         "Infected rose bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.ROSE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_THORN(
         150,
         "Infected thorn bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.THORN, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_GRAPE(
         151,
         "Infected grape bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.GRAPE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_CAMELLIA(
         152,
         "Infected camellia bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.CAMELLIA, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_OLEANDER(
         153,
         "Infected oleander bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.OLEANDER, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_LAVENDER(
         154,
         "Enchanted lavender bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.LAVENDER, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_ROSE(
         155,
         "Enchanted rose bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.ROSE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_THORN(
         156,
         "Enchanted thorn bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.THORN, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_GRAPE(
         157,
         "Enchanted grape bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.GRAPE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_CAMELLIA(
         158,
         "Enchanted camellia bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.CAMELLIA, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_OLEANDER(
         159,
         "Enchanted oleander bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.OLEANDER, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_HAZELNUT(
         160,
         "Hazelnut bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.HAZELNUT, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_HAZELNUT(
         161,
         "Infected hazelnut bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.HAZELNUT, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_HAZELNUT(
         162,
         "Enchanted hazelnut bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.HAZELNUT, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_TREE_ORANGE(
         163,
         "Orange tree",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.ORANGE, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_TREE_ORANGE(
         164,
         "Infected orange tree",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.ORANGE, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_TREE_ORANGE(
         165,
         "Enchanted orange tree",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.ORANGE, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_RASPBERRYE(
         166,
         "Raspberry bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.RASPBERRY, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_RASPBERRY(
         167,
         "Infected raspberry bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.RASPBERRY, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_RASPBERRY(
         168,
         "Enchanted raspberry bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.RASPBERRY, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_BLUEBERRY(
         169,
         "Blueberry bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.grass",
         new Tiles.Flag[]{Tiles.Flag.BLUEBERRY, Tiles.Flag.NORMAL, Tiles.Flag.BOTANIZE, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_MYCELIUM_BUSH_BLUEBERRY(
         170,
         "Infected blueberry bush",
         "#DD0229",
         0.7F,
         "img.texture.terrain.mycelium",
         new Tiles.Flag[]{Tiles.Flag.BLUEBERRY, Tiles.Flag.MYCELIUM, Tiles.Flag.USESNEWDATA, Tiles.Flag.FORAGE, Tiles.Flag.BOTANIZE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_ENCHANTED_BUSH_BLUEBERRY(
         171,
         "Enchanted blueberry bush",
         "#1a3418",
         0.75F,
         "img.texture.terrain.tree.enchanted",
         new Tiles.Flag[]{Tiles.Flag.BLUEBERRY, Tiles.Flag.ENCHANTED, Tiles.Flag.USESNEWDATA},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_BUSH_LINGONBERRY(
         172,
         "Lingonberry bush",
         "#293A02",
         0.7F,
         "img.texture.terrain.tundra",
         new Tiles.Flag[]{Tiles.Flag.LINGONBERRY, Tiles.Flag.NORMAL, Tiles.Flag.FORAGE, Tiles.Flag.USESNEWDATA, Tiles.Flag.TUNDRA},
         60,
         (byte)1,
         (byte)1,
         (byte)0
      ),
      TILE_CAVE(200, "Cave", "#B9B9B9", 0.8F, "img.texture.cave.rock", new Tiles.Flag[]{Tiles.Flag.CAVE}, 60, (byte)0, (byte)1, (byte)1),
      TILE_CAVE_EXIT(
         201,
         "Cave",
         "Cave exit",
         "#000000",
         0.8F,
         "img.texture.cave.rock",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ALIGNED},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL(
         202, "Cave wall", "#7f7f7f", 0.001F, "img.texture.cave.rock", new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.SOLIDCAVE}, 60, (byte)0, (byte)1, (byte)1
      ),
      TILE_CAVE_WALL_REINFORCED(
         203,
         "Reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.rock.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_LAVA(
         204, "Lava wall", "#7f7f7f", 0.0F, "img.texture.terrain.lava", new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.SOLIDCAVE}, 60, (byte)0, (byte)1, (byte)1
      ),
      TILE_CAVE_WALL_SLATE(
         205, "Slate wall", "#ffffff", 0.0F, "img.texture.cave.slate", new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE}, 60, (byte)0, (byte)1, (byte)1
      ),
      TILE_CAVE_WALL_MARBLE(
         206, "Marble wall", "#ffffff", 0.0F, "img.texture.cave.marble", new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE}, 60, (byte)0, (byte)1, (byte)1
      ),
      TILE_CAVE_FLOOR_REINFORCED(
         207,
         "Reinforced cave",
         "Reinforced cave floor",
         "#7f7f7f",
         0.8F,
         "img.texture.cave.rock.floor.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDFLOOR},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_GOLD(
         220,
         "Gold vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.gold",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_SILVER(
         221,
         "Silver vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.silver",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_IRON(
         222,
         "Iron vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.iron",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         1234,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_COPPER(
         223,
         "Copper vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.copper",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_LEAD(
         224,
         "Lead vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.lead",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_ZINC(
         225,
         "Zinc vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.zinc",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)2,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_TIN(
         226, "Tin vein", "#ffffff", 0.001F, "img.texture.cave.ore.tin", new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE}, 60, (byte)0, (byte)1, (byte)1
      ),
      TILE_CAVE_WALL_ORE_ADAMANTINE(
         227,
         "Adamantine vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.adamantine",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ORE_GLIMMERSTEEL(
         228,
         "Glimmersteel vein",
         "#ffffff",
         0.001F,
         "img.texture.cave.ore.glimmersteel",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ROCKSALT(
         229,
         "Rocksalt",
         "#ffffff",
         0.001F,
         "img.texture.cave.rocksalt",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.SOLIDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_SANDSTONE(
         230,
         "Sandstone",
         "#ffffff",
         0.001F,
         "img.texture.cave.sandstone",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.ORECAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_STONE_REINFORCED(
         231,
         "Stone brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.stone.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_SLATE_REINFORCED(
         232,
         "Slate brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.slate.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_POTTERY_REINFORCED(
         233,
         "Pottery brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.pottery.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED(
         234,
         "Rounded stone brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.roundedstone.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_SANDSTONE_REINFORCED(
         235,
         "Sandstone brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.sandstone.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_RENDERED_REINFORCED(
         236,
         "Rendered brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.rendered.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_MARBLE_REINFORCED(
         237,
         "Marble brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.marble.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_WOOD_REINFORCED(
         238,
         "Wood clad reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.wood.wall.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_STONE_REINFORCED(
         239,
         "Incomplete stone brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.stone.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_SLATE_REINFORCED(
         240,
         "Incomplete slate brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.slate.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_POTTERY_REINFORCED(
         241,
         "Incomplete pottery brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.pottery.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED(
         242,
         "Incomplete rounded stone brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.roundedstone.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED(
         243,
         "Incomplete sandstone brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.sandstone.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_MARBLE_REINFORCED(
         244,
         "Incomplete marble brick reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.marble.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_WALL_PART_WOOD_REINFORCED(
         245,
         "Incomplete wood clad reinforced cave wall",
         "#7f7f7f",
         0.001F,
         "img.texture.cave.wood.wall.reinforced.unfinished",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDCAVE},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      ),
      TILE_CAVE_PREPATED_FLOOR_REINFORCED(
         246,
         "Prepared reinforced cave",
         "Prepared reinforced cave floor",
         "#7f7f7f",
         0.8F,
         "img.texture.cave.prepared.floor.reinforced",
         new Tiles.Flag[]{Tiles.Flag.CAVE, Tiles.Flag.REINFORCEDFLOOR},
         60,
         (byte)0,
         (byte)1,
         (byte)1
      );

      public final byte id;
      private final int intId;
      private final Color color;
      public final float unused;
      public final float speed;
      public final String textureResource;
      public final String tilename;
      public final String tiledesc;
      public byte materialId = -1;
      private boolean aligned = false;
      private Tiles.TileRoadDirection direction = Tiles.TileRoadDirection.DIR_STRAIGHT;
      private int woodDifficulity = 2;
      private String modelName = "model.tree.birch";
      private BushData.BushType bushType = BushData.BushType.LAVENDER;
      private TreeData.TreeType treeType = TreeData.TreeType.BIRCH;
      private boolean isCave = false;
      private boolean isCaveDoor = false;
      private boolean isVisibleCaveDoor = false;
      private boolean isTree = false;
      private boolean canBearFruit = false;
      private boolean isBush = false;
      private boolean isNormal = false;
      private boolean isMycelium = false;
      private boolean isEnchanted = false;
      private boolean isGrass = false;
      private boolean isTundra = false;
      private boolean isSolidCave = false;
      private boolean isReinforcedWall = false;
      private boolean isReinforcedFloor = false;
      private boolean isOreCave = false;
      private boolean isRoad = false;
      private boolean isFlatRoad = false;
      private boolean canBotanize = false;
      private boolean canForage = false;
      private boolean usesNewData = false;
      private final int iconId;
      private final byte waterInfiltrationCode;
      private final byte waterReservoirCode;
      private final byte waterLeakageCode;

      private Tile(
         int id,
         String name,
         String color,
         float speed,
         String textureResource,
         Tiles.Flag[] flags,
         int iconId,
         byte waterInfiltration,
         byte waterReservoir,
         byte waterLeakage
      ) {
         this(id, name, name, color, speed, textureResource, flags, iconId, waterInfiltration, waterReservoir, waterLeakage);
      }

      private Tile(
         int id,
         String name,
         String uniqueName,
         String color,
         float speed,
         String textureResource,
         Tiles.Flag[] flags,
         int iconId,
         byte waterInfiltration,
         byte waterReservoir,
         byte waterLeakage
      ) {
         this.id = (byte)id;
         if (this.id == -1) {
            throw new RuntimeException("Illegal id: " + this.id + ". It is reserved for NO_TILE");
         } else {
            this.intId = id;
            this.tilename = name;
            this.tiledesc = uniqueName;
            this.color = Color.decode(color);
            this.unused = (float)new Random().nextInt();
            this.speed = speed;
            this.textureResource = textureResource;
            this.iconId = iconId;
            this.waterInfiltrationCode = waterInfiltration;
            this.waterReservoirCode = waterReservoir;
            this.waterLeakageCode = waterLeakage;
            this.processFlags(flags);
            Tiles.tiles[id] = this;
         }
      }

      private void processFlags(Tiles.Flag[] flags) {
         for(Tiles.Flag flag : flags) {
            switch(flag) {
               case USESNEWDATA:
                  this.usesNewData = true;
                  break;
               case ALIGNED:
                  this.aligned = true;
                  break;
               case TREE:
                  this.isTree = true;
                  break;
               case BUSH:
                  this.isBush = true;
                  break;
               case NORMAL:
                  this.isNormal = true;
                  break;
               case MYCELIUM:
                  this.isMycelium = true;
                  break;
               case ENCHANTED:
                  this.isEnchanted = true;
                  break;
               case GRASS:
                  this.isGrass = true;
                  break;
               case TUNDRA:
                  this.isTundra = true;
                  break;
               case ROAD:
                  this.isRoad = true;
                  break;
               case FLATROAD:
                  this.isFlatRoad = true;
                  break;
               case CAVE:
                  this.isCave = true;
                  break;
               case CAVEDOOR:
                  this.isCaveDoor = true;
                  break;
               case VISIBLE_CAVEDOOR:
                  this.isVisibleCaveDoor = true;
                  this.isCaveDoor = true;
                  break;
               case SOLIDCAVE:
                  this.isSolidCave = true;
                  break;
               case REINFORCEDCAVE:
                  this.isSolidCave = true;
                  this.isReinforcedWall = true;
                  break;
               case REINFORCEDFLOOR:
                  this.isReinforcedFloor = true;
                  break;
               case ORECAVE:
                  this.isSolidCave = true;
                  this.isOreCave = true;
                  break;
               case BIRCH:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.BIRCH;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case PINE:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.PINE;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case OAK:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.OAK;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case CEDAR:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.CEDAR;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case WILLOW:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.WILLOW;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case MAPLE:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.MAPLE;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case APPLE:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.APPLE;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case LEMON:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.LEMON;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case OLIVE:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.OLIVE;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case CHERRY:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.CHERRY;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case CHESTNUT:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.CHESTNUT;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case WALNUT:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.WALNUT;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case FIR:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.FIR;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case LINDEN:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.LINDEN;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case ORANGE:
                  this.isTree = true;
                  this.treeType = TreeData.TreeType.ORANGE;
                  this.modelName = this.treeType.getModelName();
                  this.materialId = this.treeType.getMaterial();
                  this.woodDifficulity = this.treeType.getDifficulty();
                  this.canBearFruit = this.treeType.canBearFruit();
                  break;
               case LAVENDER:
                  this.isBush = true;
                  this.bushType = BushData.BushType.LAVENDER;
                  this.materialId = this.bushType.getMaterial();
                  this.modelName = this.bushType.getModelName();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case ROSE:
                  this.isBush = true;
                  this.bushType = BushData.BushType.ROSE;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case THORN:
                  this.isBush = true;
                  this.bushType = BushData.BushType.THORN;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case GRAPE:
                  this.isBush = true;
                  this.bushType = BushData.BushType.GRAPE;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case CAMELLIA:
                  this.isBush = true;
                  this.bushType = BushData.BushType.CAMELLIA;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case OLEANDER:
                  this.isBush = true;
                  this.bushType = BushData.BushType.OLEANDER;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case HAZELNUT:
                  this.isBush = true;
                  this.bushType = BushData.BushType.HAZELNUT;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case RASPBERRY:
                  this.isBush = true;
                  this.bushType = BushData.BushType.RASPBERRY;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case BLUEBERRY:
                  this.isBush = true;
                  this.bushType = BushData.BushType.BLUEBERRY;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case LINGONBERRY:
                  this.isBush = true;
                  this.bushType = BushData.BushType.LINGONBERRY;
                  this.modelName = this.bushType.getModelName();
                  this.materialId = this.bushType.getMaterial();
                  this.woodDifficulity = this.bushType.getDifficulty();
                  this.canBearFruit = this.bushType.canBearFruit();
                  break;
               case BOTANIZE:
                  this.canBotanize = true;
                  break;
               case FORAGE:
                  this.canForage = true;
            }
         }
      }

      public String getTileName(byte data) {
         if (this == TILE_TREE) {
            return TreeData.getTypeName(data);
         } else if (this == TILE_BUSH) {
            return BushData.getTypeName(data);
         } else if (this == TILE_MYCELIUM_TREE) {
            return "Infected " + TreeData.getTypeName(data);
         } else if (this == TILE_MYCELIUM_BUSH) {
            return "Infected " + BushData.getTypeName(data);
         } else if (this == TILE_ENCHANTED_TREE) {
            return "Enchanted " + TreeData.getTypeName(data);
         } else if (this == TILE_ENCHANTED_BUSH) {
            return "Enchanted " + BushData.getTypeName(data);
         } else if (this == TILE_GRASS) {
            return GrassData.GrassType.decodeTileData(data).getName();
         } else if (this == TILE_KELP) {
            return "Kelp";
         } else if (this == TILE_REED) {
            return "Reed";
         } else if (this == TILE_LAWN) {
            return "Lawn";
         } else if (this.isCave) {
            if (data == 0) {
               return this.tilename + " floor";
            } else {
               return data == 1 ? "Cave ceiling" : this.tilename;
            }
         } else {
            return this.tilename;
         }
      }

      public String getHelpSubject(byte data) {
         if (this == TILE_TREE) {
            return TreeData.getHelpSubject(data, false);
         } else if (this == TILE_BUSH) {
            return BushData.getHelpSubject(data, false);
         } else if (this == TILE_MYCELIUM_TREE) {
            return TreeData.getHelpSubject(data, true);
         } else if (this == TILE_MYCELIUM_BUSH) {
            return BushData.getHelpSubject(data, true);
         } else if (this.intId >= TILE_CAVE.intId) {
            if (data == 0) {
               return "Terrain:" + this.tilename + "_floor";
            } else {
               return data == 1 ? "Terrain:" + this.tilename + "_ceiling" : "Terrain:" + this.tilename;
            }
         } else {
            return "Terrain:" + this.tilename;
         }
      }

      public static Tiles.Tile[] getTiles() {
         return Tiles.tiles;
      }

      public static Tiles.Tile[] getTiles(int category, String filter) {
         Set<Tiles.Tile> catTiles = new HashSet<>();

         for(Tiles.Tile tile : Tiles.tiles) {
            if (tile != null && tile.direction == Tiles.TileRoadDirection.DIR_STRAIGHT) {
               switch(category) {
                  case 1:
                     if (tile.isBush() && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  case 2:
                     if (tile.isCave() && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  case 3:
                     if (tile.isCaveDoor() && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  case 4:
                     if (!tile.isCave()
                        && !tile.isTree()
                        && !tile.isBush()
                        && !tile.isRoad()
                        && !tile.isCaveDoor()
                        && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  case 5:
                     if (tile.isRoad() && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  case 6:
                     if (!tile.isCave() && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  case 7:
                     if (tile.isTree() && wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
                     break;
                  default:
                     if (wildCardMatch(tile.getName().toLowerCase(), filter.toLowerCase())) {
                        catTiles.add(tile);
                     }
               }
            }
         }

         return catTiles.toArray(new Tiles.Tile[catTiles.size()]);
      }

      public static boolean wildCardMatch(String text, String pattern) {
         String[] cards = pattern.split("\\*");
         int offset = 0;
         boolean first = true;

         for(String card : cards) {
            if (card.length() > 0) {
               int idx = text.indexOf(card, offset);
               if (idx == -1 || first && idx != 0) {
                  return false;
               }

               offset = idx + card.length();
            }

            first = false;
         }

         return offset >= text.length() || pattern.endsWith("*");
      }

      public String getName() {
         return this.tilename;
      }

      public String getDesc() {
         return this.tiledesc;
      }

      public byte getId() {
         return this.id;
      }

      public int getIntId() {
         return this.intId;
      }

      public Color getColor() {
         return this.color;
      }

      public float getSpeed() {
         return this.speed;
      }

      public String getTextureResource() {
         return this.textureResource;
      }

      public byte getMaterialId() {
         return this.materialId;
      }

      public int getTexturePosX(byte data) {
         if (this.isTree()) {
            return this.getTreeType(data).getTexturPosX();
         } else {
            return this.isBush() ? this.getBushType(data).getTexturPosX() : 0;
         }
      }

      public int getTexturePosY(byte data) {
         if (this.isTree()) {
            return this.getTreeType(data).getTexturPosY();
         } else {
            return this.isBush() ? this.getBushType(data).getTexturPosY() : 0;
         }
      }

      public float getTreeImageWidth(byte data) {
         if (this.isTree()) {
            return this.getTreeType(data).getWidth();
         } else {
            return this.isBush() ? this.getBushType(data).getWidth() : 0.0F;
         }
      }

      public float getTreeImageHeight(byte data) {
         if (this.isTree()) {
            return this.getTreeType(data).getHeight();
         } else {
            return this.isBush() ? this.getBushType(data).getHeight() : 0.0F;
         }
      }

      public float getTreeBaseRadius(byte data) {
         if (this.isTree()) {
            return this.getTreeType(data).getRadius();
         } else {
            return this.isBush() ? this.getBushType(data).getRadius() : 0.0F;
         }
      }

      public String getModelResourceName(byte data) {
         int treeAge = FoliageAge.getAgeAsByte(data);
         if (this.isTree()) {
            return this.getTreeType(data).getModelResourceName(treeAge);
         } else {
            return this.isBush() ? this.getBushType(data).getModelResourceName(treeAge) : this.modelName;
         }
      }

      public int getWoodDificulity() {
         return this.woodDifficulity;
      }

      public boolean canBotanize() {
         return this.canBotanize;
      }

      public boolean canForage() {
         return this.canForage;
      }

      public boolean usesNewData() {
         return this.usesNewData;
      }

      public boolean isAligned() {
         return this.aligned;
      }

      public Tiles.TileRoadDirection getDirection() {
         return this.direction;
      }

      public boolean isCave() {
         return this.isCave;
      }

      public boolean isCaveDoor() {
         return this.isCaveDoor;
      }

      public boolean isVisibleCaveDoor() {
         return this.isVisibleCaveDoor;
      }

      public boolean isTree() {
         return this.isTree;
      }

      public boolean canBearFruit() {
         return this.canBearFruit;
      }

      public boolean isBush() {
         return this.isBush;
      }

      public final byte getWaterInfiltrationCode() {
         return this.waterInfiltrationCode;
      }

      public final byte getWaterReservoirCode() {
         return this.waterReservoirCode;
      }

      public final byte getWaterLeakageCode() {
         return this.waterLeakageCode;
      }

      public boolean isOak(byte data) {
         if (!this.isTree) {
            return false;
         } else {
            return this.getTreeType(data) == TreeData.TreeType.OAK;
         }
      }

      public boolean isWillow(byte data) {
         if (!this.isTree) {
            return false;
         } else {
            return this.getTreeType(data) == TreeData.TreeType.WILLOW;
         }
      }

      public boolean isThorn(byte data) {
         if (!this.isBush) {
            return false;
         } else {
            return this.getBushType(data) == BushData.BushType.THORN;
         }
      }

      public boolean isMaple(byte data) {
         if (!this.isTree) {
            return false;
         } else {
            return this.getTreeType(data) == TreeData.TreeType.MAPLE;
         }
      }

      public TreeData.TreeType getTreeType(byte data) {
         return this.usesNewData ? this.treeType : TreeData.TreeType.fromInt(data & 15);
      }

      public BushData.BushType getBushType(byte data) {
         return this.usesNewData ? this.bushType : BushData.BushType.fromInt(data & 15);
      }

      public boolean isNormal() {
         return this.isNormal;
      }

      public boolean isMycelium() {
         return this.isMycelium;
      }

      public boolean isEnchanted() {
         return this.isEnchanted;
      }

      public boolean isNormalBush() {
         return this.isNormal && this.isBush;
      }

      public boolean isMyceliumBush() {
         return this.isMycelium && this.isBush;
      }

      public boolean isEnchantedBush() {
         return this.isEnchanted && this.isBush;
      }

      public boolean isNormalTree() {
         return this.isNormal && this.isTree;
      }

      public boolean isMyceliumTree() {
         return this.isMycelium && this.isTree;
      }

      public boolean isEnchantedTree() {
         return this.isEnchanted && this.isTree;
      }

      public boolean isGrass() {
         return this.isGrass;
      }

      public boolean isTundra() {
         return this.isTundra;
      }

      public boolean isSolidCave() {
         return this.isSolidCave;
      }

      public boolean isReinforcedCave() {
         return this.isReinforcedWall;
      }

      public boolean isReinforcedFloor() {
         return this.isReinforcedFloor;
      }

      public boolean isOreCave() {
         return this.isOreCave;
      }

      public boolean isRoad() {
         return this.isRoad;
      }

      public boolean isFlatRoad() {
         return this.isFlatRoad;
      }

      public int getIconId() {
         return this.iconId;
      }

      @Override
      public String toString() {
         return super.toString();
      }
   }

   public static enum TileBorderDirection {
      DIR_HORIZ((byte)0),
      DIR_DOWN((byte)2),
      CORNER((byte)4);

      private byte id;

      private TileBorderDirection(byte newId) {
         this.id = newId;
      }

      public byte getCode() {
         return this.id;
      }
   }

   public static enum TileRoadDirection {
      DIR_STRAIGHT((byte)0),
      DIR_NW((byte)1),
      DIR_NE((byte)2),
      DIR_SE((byte)3),
      DIR_SW((byte)4);

      private byte id;

      private TileRoadDirection(byte newId) {
         this.id = newId;
      }

      public byte getCode() {
         return this.id;
      }
   }
}

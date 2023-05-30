package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchTileException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Wall implements MiscConstants, TimeConstants, Blocker, StructureSupport, Permissions.IAllow {
   public int x1;
   public int x2;
   public int y1;
   public int y2;
   private static final Vector3f normalHoriz = new Vector3f(0.0F, 1.0F, 0.0F);
   private static final Vector3f normalVertical = new Vector3f(1.0F, 0.0F, 0.0F);
   private Vector3f centerPoint;
   private static final Map<Long, Set<Wall>> walls = new HashMap<>();
   private static final String GETALLWALLS = "SELECT * FROM WALLS WHERE STARTX<ENDX OR STARTY<ENDY";
   Permissions permissions = new Permissions();
   private static final Set<Wall> rubbleWalls = new HashSet<>();
   public long structureId = -10L;
   int number = -10;
   private static final Logger logger = Logger.getLogger(Wall.class.getName());
   public float originalQL;
   public float currentQL;
   public float damage;
   public StructureTypeEnum type = StructureTypeEnum.SOLID;
   public int tilex;
   public int tiley;
   private int floorLevel = 0;
   public int heightOffset = 0;
   byte layer = 0;
   public long lastUsed;
   public StructureStateEnum state = StructureStateEnum.INITIALIZED;
   private StructureMaterialEnum material = StructureMaterialEnum.WOOD;
   int color = -1;
   boolean wallOrientationFlag = false;
   private static final String WOOD = "wood";
   private static final String STONE = "stone";
   private static final String TIMBER_FRAMED = "timber framed";
   private static final String PLAIN_STONE = "plain stone";
   private static final String SLATE = "slate";
   private static final String ROUNDED_STONE = "rounded stone";
   private static final String POTTERY = "pottery";
   private static final String SANDSTONE = "sandstone";
   private static final String RENDERED = "rendered";
   private static final String MARBLE = "marble";
   private static final int[] emptyArr = new int[0];
   protected boolean isIndoor = false;

   public Wall(
      StructureTypeEnum aType,
      int aTileX,
      int aTileY,
      int aStartX,
      int aStartY,
      int aEndX,
      int aEndY,
      float aQualityLevel,
      long aStructure,
      StructureMaterialEnum _material,
      boolean _isIndoor,
      int _heightOffset,
      int _layer
   ) {
      this.structureId = aStructure;
      this.tilex = aTileX;
      this.tiley = aTileY;
      this.x1 = aStartX;
      this.y1 = aStartY;
      this.x2 = aEndX;
      this.y2 = aEndY;
      this.currentQL = aQualityLevel;
      this.originalQL = aQualityLevel;
      this.lastUsed = System.currentTimeMillis();
      this.type = aType;
      this.material = _material;
      this.isIndoor = _isIndoor;
      this.heightOffset = _heightOffset;
      this.setFloorLevel();
      this.layer = (byte)(_layer & 0xFF);
   }

   public Wall(
      int wallid,
      StructureTypeEnum typ,
      int tx,
      int ty,
      int xs,
      int ys,
      int xe,
      int ye,
      float qualityLevel,
      float origQl,
      float dam,
      long structure,
      long last,
      StructureStateEnum stat,
      int col,
      StructureMaterialEnum _material,
      boolean _isIndoor,
      int _heightOffset,
      int _layer,
      boolean _wallOrientation,
      int aSettings
   ) {
      this.number = wallid;
      this.type = typ;
      this.tilex = tx;
      this.tiley = ty;
      this.x1 = xs;
      this.y1 = ys;
      this.x2 = xe;
      this.y2 = ye;
      this.currentQL = qualityLevel;
      this.originalQL = origQl;
      this.damage = dam;
      this.structureId = structure;
      this.lastUsed = last;
      this.state = stat;
      this.color = col;
      this.material = _material;
      this.isIndoor = _isIndoor;
      this.heightOffset = _heightOffset;
      this.setFloorLevel();
      this.layer = (byte)(_layer & 0xFF);
      this.wallOrientationFlag = _wallOrientation;
      this.setSettings(aSettings);
   }

   public Wall(int wallid, boolean load) throws IOException {
      this.number = wallid;
      if (load) {
         this.load();
      }
   }

   public final String getIdName() {
      if (this.material == StructureMaterialEnum.WOOD) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "wooden_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "wooden_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "wooden_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "wooden_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "wooden_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "wooden_portcullis";
         }

         if (this.type == StructureTypeEnum.CANOPY_DOOR) {
            return "wooden_canopy_door";
         }

         if (this.type == StructureTypeEnum.WIDE_WINDOW) {
            return "wooden_wide_window";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "wooden_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "wooden_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "wooden_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.STONE) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "stone_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "stone_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "sturdy_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "sturdy_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "study_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "sturdy_portcullis";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "stone_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "sturdy_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "sturdy_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "sturdy_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.PLAIN_STONE) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "plain_stone_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "plain_stone_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "plain_narrow_stone_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "plain_stone_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "plain_stone_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "plain_stone_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "plain_stone_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "plain_stone_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "plain_stone_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "plain_stone_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "plain_stone_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "plain_stone_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.SLATE) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "slate_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "slate_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "narrow_slate_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "slate_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "slate_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "slate_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "slate_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "slate_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "slate_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "slate_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "slate_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "slate_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.ROUNDED_STONE) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "rounded_stone_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "rounded_stone_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "narrow_rounded_stone_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "rounded_stone_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "rounded_stone_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "rounded_stone_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "rounded_stone_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "rounded_stone_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "rounded_stone_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "rounded_stone_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "rounded_stone_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "rounded_stone_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.POTTERY) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "pottery_brick_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "pottery_brick_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "narrow_pottery_brick_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "pottery_brick_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "pottery_brick_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "pottery_brick_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "pottery_brick_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "pottery_brick_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "pottery_brick_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "pottery_brick_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "pottery_brick_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "pottery_brick_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.SANDSTONE) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "sandstone_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "sandstone_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "narrow_sandstone_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "sandstone_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "sandstone_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "sandstone_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "sandstone_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "sandstone_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "sandstone_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "sandstone_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "sandstone_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "sandstone_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.RENDERED) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "rendered_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "rendered_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "narrow_rendered_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "rendered_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "rendered_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "rendered_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "rendered_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "rendered_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "rendered_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "rendered_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "rendered_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "rendered_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.MARBLE) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "marble_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "marble_window";
         }

         if (this.type == StructureTypeEnum.NARROW_WINDOW) {
            return "narrow_marble_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "marble_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "marble_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "marble_arched";
         }

         if (this.type == StructureTypeEnum.PORTCULLIS) {
            return "marble_portcullis";
         }

         if (this.type == StructureTypeEnum.BARRED) {
            return "marble_barred_wall";
         }

         if (this.type == StructureTypeEnum.ORIEL) {
            return "marble_oriel";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "marble_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "marble_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "marble_arched_t";
         }
      } else if (this.material == StructureMaterialEnum.TIMBER_FRAMED) {
         if (this.type == StructureTypeEnum.SOLID) {
            return "timber_framed_wall";
         }

         if (this.type == StructureTypeEnum.WINDOW) {
            return "timber_framed_window";
         }

         if (this.type == StructureTypeEnum.DOOR) {
            return "timber_framed_door";
         }

         if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
            return "timber_framed_double_door";
         }

         if (this.type == StructureTypeEnum.ARCHED) {
            return "timber_framed_arched";
         }

         if (this.type == StructureTypeEnum.BALCONY) {
            return "timber_framed_balcony";
         }

         if (this.type == StructureTypeEnum.JETTY) {
            return "timber_framed_jetty";
         }

         if (this.type == StructureTypeEnum.ARCHED_LEFT) {
            return "timber_framed_arched_left";
         }

         if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
            return "timber_framed_arched_right";
         }

         if (this.type == StructureTypeEnum.ARCHED_T) {
            return "timber_framed_arched_t";
         }
      }

      return this.type == StructureTypeEnum.PLAN ? "wall_plan" : "unknown_wall";
   }

   @Override
   public final String getName() {
      return getName(this.type, this.material);
   }

   public static final String getName(StructureTypeEnum type, StructureMaterialEnum material) {
      if (type == StructureTypeEnum.RUBBLE) {
         return "pile of debris";
      } else {
         if (material == StructureMaterialEnum.WOOD) {
            if (type == StructureTypeEnum.SOLID) {
               return "wooden wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "wooden window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "wooden door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "wooden double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "wooden arched wall";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "wooden portcullis";
            }

            if (type == StructureTypeEnum.CANOPY_DOOR) {
               return "wooden canopy door";
            }

            if (type == StructureTypeEnum.WIDE_WINDOW) {
               return "wooden wide window";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "wooden left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "wooden right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "wooden T arch";
            }

            if (type == StructureTypeEnum.SCAFFOLDING) {
               return "wooden scaffolding";
            }
         } else if (material == StructureMaterialEnum.STONE) {
            if (type == StructureTypeEnum.SOLID) {
               return "stone wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "stone window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "sturdy door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "sturdy double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "sturdy arched wall";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "sturdy portcullis";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "stone oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "sturdy left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "sturdy right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "sturdy T arch";
            }
         } else if (material == StructureMaterialEnum.PLAIN_STONE) {
            if (type == StructureTypeEnum.SOLID) {
               return "plain stone wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "plain stone window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "plain narrow stone window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "plain stone door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "plain stone double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "plain stone arched wall";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "plain stone portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "plain stone barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "plain stone oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "plain stone left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "plain stone right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "plain stone T arch";
            }
         } else if (material == StructureMaterialEnum.SLATE) {
            if (type == StructureTypeEnum.SOLID) {
               return "slate wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "slate window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "narrow slate window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "slate door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "slate double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "slate arched";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "slate portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "slate barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "slate oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "slate left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "slate right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "slate T arch";
            }
         } else if (material == StructureMaterialEnum.ROUNDED_STONE) {
            if (type == StructureTypeEnum.SOLID) {
               return "rounded stone wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "rounded stone window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "narrow rounded stone window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "rounded stone door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "rounded stone double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "rounded stone arched";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "rounded stone portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "rounded stone barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "rounded stone oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "rounded stone left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "rounded stone right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "rounded stone T arch";
            }
         } else if (material == StructureMaterialEnum.POTTERY) {
            if (type == StructureTypeEnum.SOLID) {
               return "pottery brick wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "pottery brick window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "narrow pottery brick window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "pottery brick door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "pottery brick double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "pottery brick arched";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "pottery brick portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "pottery brick barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "pottery brick oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "pottery brick left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "pottery brick right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "pottery brick T arch";
            }
         } else if (material == StructureMaterialEnum.SANDSTONE) {
            if (type == StructureTypeEnum.SOLID) {
               return "sandstone wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "sandstone window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "narrow sandstone window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "sandstone door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "sandstone double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "sandstone arched";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "sandstone portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "sandstone barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "sandstone oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "sandstone left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "sandstone right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "sandstone T arch";
            }
         } else if (material == StructureMaterialEnum.RENDERED) {
            if (type == StructureTypeEnum.SOLID) {
               return "rendered wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "rendered window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "narrow rendered window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "rendered door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "rendered double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "rendered arched";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "rendered portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "rendered barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "rendered oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "rendered left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "rendered right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "rendered T arch";
            }
         } else if (material == StructureMaterialEnum.MARBLE) {
            if (type == StructureTypeEnum.SOLID) {
               return "marble wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "marble window";
            }

            if (type == StructureTypeEnum.NARROW_WINDOW) {
               return "narrow marble window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "marble door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "marble double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "marble arched";
            }

            if (type == StructureTypeEnum.PORTCULLIS) {
               return "marble portcullis";
            }

            if (type == StructureTypeEnum.BARRED) {
               return "marble barred wall";
            }

            if (type == StructureTypeEnum.ORIEL) {
               return "marble oriel";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "marble left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "marble right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "marble T arch";
            }
         } else if (material == StructureMaterialEnum.TIMBER_FRAMED) {
            if (type == StructureTypeEnum.SOLID) {
               return "timber framed wall";
            }

            if (type == StructureTypeEnum.WINDOW) {
               return "timber framed window";
            }

            if (type == StructureTypeEnum.DOOR) {
               return "timber framed door";
            }

            if (type == StructureTypeEnum.DOUBLE_DOOR) {
               return "timber framed double door";
            }

            if (type == StructureTypeEnum.ARCHED) {
               return "timber framed arched wall";
            }

            if (type == StructureTypeEnum.JETTY) {
               return "timber framed jetty";
            }

            if (type == StructureTypeEnum.BALCONY) {
               return "timber framed balcony";
            }

            if (type == StructureTypeEnum.ARCHED_LEFT) {
               return "timber framed left arch";
            }

            if (type == StructureTypeEnum.ARCHED_RIGHT) {
               return "timber framed right arch";
            }

            if (type == StructureTypeEnum.ARCHED_T) {
               return "timber framed T arch";
            }
         }

         return type == StructureTypeEnum.PLAN ? "wall plan" : "unknown wall";
      }
   }

   public static final String getMaterialName(StructureMaterialEnum material) {
      if (material == StructureMaterialEnum.WOOD) {
         return "Wooden";
      } else if (material == StructureMaterialEnum.STONE) {
         return "Stone brick";
      } else if (material == StructureMaterialEnum.PLAIN_STONE) {
         return "Plain stone";
      } else if (material == StructureMaterialEnum.SLATE) {
         return "Slate";
      } else if (material == StructureMaterialEnum.ROUNDED_STONE) {
         return "Rounded stone";
      } else if (material == StructureMaterialEnum.POTTERY) {
         return "Pottery";
      } else if (material == StructureMaterialEnum.SANDSTONE) {
         return "Sandstone";
      } else if (material == StructureMaterialEnum.MARBLE) {
         return "Marble";
      } else {
         return material == StructureMaterialEnum.TIMBER_FRAMED ? "Timber framed" : "unknown";
      }
   }

   public static final Wall[] getRubbleWalls() {
      return rubbleWalls.toArray(new Wall[rubbleWalls.size()]);
   }

   protected static final void addRubble(Wall wall) {
      rubbleWalls.add(wall);
   }

   protected static final void removeRubble(Wall wall) {
      rubbleWalls.remove(wall);
   }

   @Override
   public final boolean isFence() {
      return false;
   }

   @Override
   public final boolean isWall() {
      return true;
   }

   @Override
   public final boolean isFloor() {
      return false;
   }

   @Override
   public final boolean isRoof() {
      return false;
   }

   @Override
   public final boolean isStair() {
      return false;
   }

   @Override
   public final boolean isTile() {
      return false;
   }

   @Override
   public final boolean isDoor() {
      return this.type == StructureTypeEnum.DOOR
         || this.type == StructureTypeEnum.DOUBLE_DOOR
         || this.type == StructureTypeEnum.PORTCULLIS
         || this.type == StructureTypeEnum.CANOPY_DOOR
         || isArched(this.type);
   }

   @Override
   public final Vector3f getNormal() {
      return this.isHorizontal() ? normalHoriz : normalVertical;
   }

   private final Vector3f calculateCenterPoint() {
      int sx = Math.min(this.x1, this.x2);
      int sy = Math.min(this.y1, this.y2);
      return new Vector3f(
         this.isHorizontal() ? (float)(sx * 4 + 2) : (float)(sx * 4), this.isHorizontal() ? (float)(sy * 4) : (float)(sy * 4 + 2), this.getMinZ() + 1.5F
      );
   }

   @Override
   public final Vector3f getCenterPoint() {
      if (this.centerPoint == null) {
         this.centerPoint = this.calculateCenterPoint();
      }

      return this.centerPoint;
   }

   @Override
   public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f normal, int blockType, long target, boolean followGround) {
      if (target == this.getId()) {
         return null;
      } else if (this.type != StructureTypeEnum.PLAN && this.type != StructureTypeEnum.RUBBLE && !isArched(this.type)) {
         if (blockType != 5 || !this.isWindow() && !this.isBalcony() && !this.isJetty() && !this.isOriel()) {
            if (!this.isFinished()) {
               return null;
            } else if ((blockType == 6 || blockType == 8) && this.isDoor() && this.getDoor() != null) {
               return this.getDoor().canBeOpenedBy(creature, true)
                  ? null
                  : this.getIntersectionPoint(startPos, endPos, normal, creature, blockType, followGround);
            } else {
               return this.getIntersectionPoint(startPos, endPos, normal, creature, blockType, followGround);
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor) {
      if (this.type == StructureTypeEnum.PLAN || this.isArched()) {
         return true;
      } else if (!this.isFinished()) {
         return true;
      } else {
         return this.isDoor() && this.getDoor() != null && this.getDoor().canBeOpenedBy(creature, true);
      }
   }

   @Override
   public final float getBlockPercent(Creature creature) {
      if (this.type == StructureTypeEnum.RUBBLE) {
         return 0.0F;
      } else if (this.isFinished()) {
         if (this.isWindow() || this.isJetty()) {
            return 70.0F;
         } else if (this.isOriel()) {
            return 80.0F;
         } else {
            return this.isBalcony() ? 10.0F : 100.0F;
         }
      } else {
         return (float)Math.max(0, this.getState().state);
      }
   }

   public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f normal, Creature creature, int blockType, boolean followGround) {
      Vector3f spcopy = startPos.clone();
      Vector3f epcopy = endPos.clone();
      if (this.getFloorLevel() == 0 && (followGround || spcopy.z <= this.getMinZ())) {
         spcopy.z = this.getMinZ() + 1.75F;
         if (followGround) {
            epcopy.z = this.getMinZ() + 0.5F;
         }
      }

      Vector3f diff = this.getCenterPoint().subtract(spcopy);
      Vector3f diffend = epcopy.subtract(spcopy);
      if (this.isHorizontal()) {
         float steps = diff.y / normal.y;
         Vector3f intersection = spcopy.add(normal.mult(steps));
         Vector3f interDiff = intersection.subtract(spcopy);
         if (diffend.length() + 0.01F < interDiff.length()) {
            return null;
         }

         if (this.isWithinBounds(intersection, followGround)) {
            float u = this.getNormal().dot(this.getCenterPoint().subtract(startPos)) / this.getNormal().dot(epcopy.subtract(spcopy));
            if (u >= 0.0F && u <= 1.0F) {
               return intersection;
            }

            return null;
         }
      } else {
         float steps = diff.x / normal.x;
         Vector3f intersection = spcopy.add(normal.mult(steps));
         Vector3f interDiff = intersection.subtract(spcopy);
         if (diffend.length() < interDiff.length()) {
            return null;
         }

         if (this.isWithinBounds(intersection, followGround)) {
            float u = this.getNormal().dot(this.getCenterPoint().subtract(spcopy)) / this.getNormal().dot(epcopy.subtract(spcopy));
            if (u >= 0.0F && u <= 1.0F) {
               return intersection;
            }

            return null;
         }
      }

      return null;
   }

   private final boolean isWithinBounds(Vector3f pointToCheck, boolean followGround) {
      if (this.isHorizontal()) {
         if (pointToCheck.getY() >= (float)(this.y1 * 4) - 0.1F
            && pointToCheck.getY() <= (float)(this.y2 * 4) + 0.1F
            && pointToCheck.getX() >= (float)(Math.min(this.x1, this.x2) * 4)
            && pointToCheck.getX() <= (float)(Math.max(this.x2, this.x1) * 4)
            && (followGround && this.getFloorLevel() == 0 || pointToCheck.getZ() >= this.getMinZ() && pointToCheck.getZ() <= this.getMaxZ())) {
            return true;
         }
      } else if (pointToCheck.getX() >= (float)(this.x1 * 4) - 0.1F
         && pointToCheck.getX() <= (float)(this.x2 * 4) + 0.1F
         && pointToCheck.getY() >= (float)(Math.min(this.y1, this.y2) * 4)
         && pointToCheck.getY() <= (float)(Math.max(this.y2, this.y1) * 4)
         && (followGround && this.getFloorLevel() == 0 || pointToCheck.getZ() >= this.getMinZ() && pointToCheck.getZ() <= this.getMaxZ())) {
         return true;
      }

      return false;
   }

   @Override
   public final int getTileX() {
      return this.tilex;
   }

   public final int getColor() {
      return this.color;
   }

   public final boolean getWallOrientationFlag() {
      return this.wallOrientationFlag;
   }

   public final int getNumber() {
      return this.number;
   }

   @Override
   public final int getTileY() {
      return this.tiley;
   }

   public final Tiles.TileBorderDirection getDir() {
      return this.isHorizontal() ? Tiles.TileBorderDirection.DIR_HORIZ : Tiles.TileBorderDirection.DIR_DOWN;
   }

   @Override
   public final boolean isHorizontal() {
      return this.y1 == this.y2;
   }

   public final float getCurrentQualityLevel() {
      return this.currentQL * Math.max(1.0F, 100.0F - this.damage) / 100.0F;
   }

   public final boolean isOnPvPServer() {
      if (this.isHorizontal()) {
         if (Zones.isOnPvPServer(this.x1, this.y1)) {
            return true;
         }

         if (Zones.isOnPvPServer(this.x1, this.y1 - 1)) {
            return true;
         }
      } else {
         if (Zones.isOnPvPServer(this.x1, this.y1)) {
            return true;
         }

         if (Zones.isOnPvPServer(this.x1 - 1, this.y1)) {
            return true;
         }
      }

      return false;
   }

   public final float getOriginalQualityLevel() {
      return this.originalQL;
   }

   @Override
   public final int getStartX() {
      return this.x1;
   }

   @Override
   public final int getStartY() {
      return this.y1;
   }

   @Override
   public final int getMinX() {
      return Math.min(this.x1, this.x2);
   }

   @Override
   public final int getMinY() {
      return Math.min(this.y1, this.y2);
   }

   @Override
   public final int getEndX() {
      return this.x2;
   }

   @Override
   public final int getEndY() {
      return this.y2;
   }

   @Override
   public final float getPositionX() {
      return (float)(this.x1 * 4 + this.x2 * 4) / 2.0F;
   }

   @Override
   public final float getPositionY() {
      return (float)(this.y1 * 4 + this.y2 * 4) / 2.0F;
   }

   public final void setStructureId(long structure) {
      this.structureId = structure;
   }

   public final long getStructureId() {
      return this.structureId;
   }

   public final long getOLDId() {
      if (this.y1 == this.y2) {
         if (this.x1 < this.x2) {
            return 0L + ((long)this.x1 << 32) + (long)(this.y1 << 16) + 5L;
         } else if (this.x1 > this.x2) {
            return 72057594037927936L + ((long)this.x2 << 32) + (long)(this.y1 << 16) + 5L;
         } else {
            throw new IllegalStateException("Found a broken wall.");
         }
      } else if (this.x1 == this.x2) {
         if (this.y1 < this.y2) {
            return 72339069014638592L + ((long)this.x1 << 32) + (long)(this.y1 << 16) + 5L;
         } else if (this.y1 > this.y2) {
            return 281474976710656L + ((long)this.x2 << 32) + (long)(this.y2 << 16) + 5L;
         } else {
            throw new IllegalStateException("Found a broken wall.");
         }
      } else {
         throw new IllegalStateException("Found a broken wall.");
      }
   }

   @Override
   public final boolean equals(StructureSupport support) {
      return support.getId() == this.getId();
   }

   @Override
   public final long getId() {
      if (this.y1 == this.y2) {
         if (this.x1 < this.x2) {
            return Tiles.getHouseWallId(this.x1, this.y1, this.heightOffset, this.getLayer(), (byte)0);
         } else if (this.x1 > this.x2) {
            return Tiles.getHouseWallId(this.x2, this.y1, this.heightOffset, this.getLayer(), (byte)0);
         } else {
            throw new IllegalStateException("Found a broken wall.");
         }
      } else if (this.x1 == this.x2) {
         if (this.y1 < this.y2) {
            return Tiles.getHouseWallId(this.x1, this.y1, this.heightOffset, this.getLayer(), (byte)1);
         } else if (this.y1 > this.y2) {
            return Tiles.getHouseWallId(this.x2, this.y2, this.heightOffset, this.getLayer(), (byte)1);
         } else {
            throw new IllegalStateException("Found a broken wall.");
         }
      } else {
         throw new IllegalStateException("Found a broken wall.");
      }
   }

   public final void setType(StructureTypeEnum aType) {
      this.type = aType;
      this.lastUsed = System.currentTimeMillis();
   }

   public final StructureTypeEnum getType() {
      return this.type;
   }

   public final boolean isArched() {
      return isArched(this.type);
   }

   public final boolean isLRArch() {
      switch(this.type) {
         case ARCHED_LEFT:
         case ARCHED_RIGHT:
            return true;
         default:
            return false;
      }
   }

   public final boolean isHalfArch() {
      return isHalfArch(this.type);
   }

   public final StructureStateEnum getState() {
      return this.state;
   }

   public final StructureStateEnum getNeeded() {
      int needed = this.getFinalState().state - this.getState().state;
      if (this.isHalfArch()) {
         --needed;
      }

      return StructureStateEnum.getStateByValue((byte)needed);
   }

   public final StructureStateEnum getFinalState() {
      int extra = this.isHalfArch() && !this.isWood() ? 1 : 0;
      if (this.isTimberFramed()) {
         return StructureStateEnum.getStateByValue((byte)(26 + extra));
      } else {
         return this.type == StructureTypeEnum.SCAFFOLDING
            ? StructureStateEnum.getStateByValue((byte)5)
            : StructureStateEnum.getStateByValue((byte)(21 + extra));
      }
   }

   public final boolean isFinished() {
      return this.state == StructureStateEnum.FINISHED;
   }

   public abstract void setState(StructureStateEnum var1);

   public final VolaTile getOrCreateOuterTile(boolean surfaced) throws NoSuchZoneException, NoSuchTileException {
      if (this.isHorizontal()) {
         VolaTile t = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
         if (t.getStructure() == null) {
            return t;
         } else {
            VolaTile t2 = Zones.getZone(this.x1, this.y1 - 1, surfaced).getOrCreateTile(this.x1, this.y1 - 1);
            return t2;
         }
      } else {
         VolaTile t = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
         return t.getStructure() == null ? t : Zones.getZone(this.x1 - 1, this.y1, surfaced).getOrCreateTile(this.x1 - 1, this.y1);
      }
   }

   public final VolaTile getOrCreateInnerTile(boolean surfaced) throws NoSuchZoneException, NoSuchTileException {
      if (this.isHorizontal()) {
         VolaTile toReturn = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
         if (toReturn.getStructure() != null) {
            return toReturn.isTransition() ? Zones.getZone(this.x1, this.y1, false).getOrCreateTile(this.x1, this.y1) : toReturn;
         } else {
            VolaTile t2 = Zones.getZone(this.x1, this.y1 - 1, surfaced).getOrCreateTile(this.x1, this.y1 - 1);
            if (t2.getStructure() == null) {
               logger.log(Level.INFO, t2 + " has no structure, so no inner wall exists.", (Throwable)(new Exception()));
            }

            return t2.isTransition() ? Zones.getZone(this.x1, this.y1 - 1, false).getOrCreateTile(this.x1, this.y1 - 1) : t2;
         }
      } else {
         VolaTile toReturn = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
         if (toReturn.getStructure() != null) {
            return toReturn.isTransition() ? Zones.getZone(this.x1, this.y1, false).getOrCreateTile(this.x1, this.y1) : toReturn;
         } else {
            VolaTile t2 = Zones.getZone(this.x1 - 1, this.y1, surfaced).getOrCreateTile(this.x1 - 1, this.y1);
            if (t2.getStructure() == null) {
               logger.log(Level.INFO, t2 + " has no structure, so no inner wall exists.", (Throwable)(new Exception()));
            }

            return t2.isTransition() ? Zones.getZone(this.x1 - 1, this.y1, false).getOrCreateTile(this.x1 - 1, this.y1) : t2;
         }
      }
   }

   public final void poll(long currTime, VolaTile t, Structure struct) {
      if (this.type != StructureTypeEnum.PLAN) {
         if (this.type == StructureTypeEnum.RUBBLE) {
            this.setDamage(this.getDamage() + 4.0F);
         } else if (struct == null) {
            logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + " no structure attached.");
         } else if (currTime - struct.getCreationDate() > 172800000L) {
            float mod = 1.0F;
            Village village = null;
            if (t != null) {
               village = t.getVillage();
               if (village == null) {
                  if (!this.isHorizontal()) {
                     Village westTile = Zones.getVillage(this.tilex - 1, this.tiley, true);
                     if (westTile != null && this.getStartX() == this.tilex) {
                        village = westTile;
                     }

                     Village eastTile = Zones.getVillage(this.tilex + 1, this.tiley, true);
                     if (eastTile != null && this.getStartX() == this.tilex + 1) {
                        village = eastTile;
                     }
                  } else {
                     Village northTile = Zones.getVillage(this.tilex, this.tiley - 1, true);
                     if (northTile != null && this.getStartY() == this.tiley) {
                        village = northTile;
                     }

                     Village southTile = Zones.getVillage(this.tilex, this.tiley + 1, true);
                     if (southTile != null && this.getStartY() == this.tiley + 1) {
                        village = southTile;
                     }
                  }
               }

               if (village != null && !village.lessThanWeekLeft()) {
                  if (village.moreThanMonthLeft()) {
                     return;
                  }

                  mod *= 10.0F;
               } else if (t.getKingdom() == 0 || Servers.localServer.HOMESERVER) {
                  mod *= 0.5F;
               }

               if (!t.isOnSurface()) {
                  mod *= 0.75F;
               }
            }

            if ((float)(currTime - this.lastUsed) > (Servers.localServer.testServer ? 60000.0F * mod : 8.64E7F * mod) && !this.hasNoDecay()) {
               long ownerId = struct.getOwnerId();
               if (ownerId == -10L) {
                  this.damage += 20.0F + Server.rand.nextFloat() * 10.0F;
               } else {
                  boolean ownerIsInactive = false;
                  long aMonth = Servers.isThisATestServer() ? 86400000L : 2419200000L;
                  PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(ownerId);
                  if (pInfo == null) {
                     ownerIsInactive = true;
                  } else if (pInfo.lastLogin == 0L && pInfo.lastLogout < System.currentTimeMillis() - 3L * aMonth) {
                     ownerIsInactive = true;
                  }

                  if (ownerIsInactive) {
                     this.damage += 3.0F;
                  }

                  if (t != null && village == null) {
                     Village v = Villages.getVillageWithPerimeterAt(t.tilex, t.tiley, t.isOnSurface());
                     if (v != null && !v.isCitizen(ownerId) && ownerIsInactive) {
                        this.damage += 3.0F;
                     }
                  }
               }

               this.setLastUsed(currTime);
               this.setDamage(this.damage + 0.1F * this.getDamageModifier());
            }
         }
      }
   }

   public static final boolean isArched(StructureTypeEnum type) {
      switch(type) {
         case ARCHED_LEFT:
         case ARCHED_RIGHT:
         case ARCHED:
         case ARCHED_T:
         case SCAFFOLDING:
            return true;
         default:
            return false;
      }
   }

   public static final boolean isHalfArch(StructureTypeEnum type) {
      switch(type) {
         case ARCHED_LEFT:
         case ARCHED_RIGHT:
         case ARCHED_T:
            return true;
         case ARCHED:
         default:
            return false;
      }
   }

   public static final List<Wall> getWallsAsArrayListFor(long structureId) {
      List<Wall> toReturn = new ArrayList<>();
      Set<Wall> flset = walls.get(structureId);
      if (flset != null) {
         toReturn.addAll(flset);
      }

      return toReturn;
   }

   public static final void loadAllWalls() throws IOException {
      logger.log(Level.INFO, "Loading all walls.");
      long s = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM WALLS WHERE STARTX<ENDX OR STARTY<ENDY");

         long sid;
         Set<Wall> flset;
         for(rs = ps.executeQuery();
            rs.next();
            flset.add(
               new DbWall(
                  rs.getInt("ID"),
                  StructureTypeEnum.getTypeByINDEX(rs.getByte("TYPE")),
                  rs.getInt("TILEX"),
                  rs.getInt("TILEY"),
                  rs.getInt("STARTX"),
                  rs.getInt("STARTY"),
                  rs.getInt("ENDX"),
                  rs.getInt("ENDY"),
                  rs.getFloat("CURRENTQL"),
                  rs.getFloat("ORIGINALQL"),
                  rs.getFloat("DAMAGE"),
                  sid,
                  rs.getLong("LASTMAINTAINED"),
                  StructureStateEnum.getStateByValue(rs.getByte("STATE")),
                  rs.getInt("COLOR"),
                  StructureMaterialEnum.getEnumByMaterial(rs.getByte("MATERIAL")),
                  rs.getBoolean("ISINDOOR"),
                  rs.getInt("HEIGHTOFFSET"),
                  rs.getInt("LAYER"),
                  rs.getBoolean("WALLORIENTATION"),
                  rs.getInt("SETTINGS")
               )
            )
         ) {
            sid = rs.getLong("STRUCTURE");
            flset = walls.get(sid);
            if (flset == null) {
               flset = new HashSet<>();
               walls.put(sid, flset);
            }
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, "Failed to load walls! " + var13.getMessage(), (Throwable)var13);
         throw new IOException(var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long e = System.nanoTime();
         logger.log(Level.INFO, "Loaded " + walls.size() + " wall. That took " + (float)(e - s) / 1000000.0F + " ms.");
      }
   }

   @Override
   public final float getDamageModifier() {
      if (this.type == StructureTypeEnum.RUBBLE) {
         return 0.001F;
      } else {
         return !this.isStone()
               && !this.isPlainStone()
               && !this.isSlate()
               && !this.isRoundedStone()
               && !this.isMarble()
               && !this.isRendered()
               && !this.isPottery()
               && !this.isSandstone()
            ? 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F)
            : 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F) * 0.3F;
      }
   }

   public final Door getDoor() {
      if (this.isDoor()) {
         try {
            for(Door door : this.getOrCreateInnerTile(this.getLayer() == 0).getDoors()) {
               try {
                  if (door.getWall() == this) {
                     return door;
                  }
               } catch (NoSuchWallException var7) {
               }
            }
         } catch (NoSuchTileException var10) {
            logger.log(
               Level.WARNING,
               "Why: " + var10.getMessage() + " " + this.getTileX() + "," + this.getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this
            );
         } catch (NoSuchZoneException var11) {
            logger.log(
               Level.WARNING,
               "Why: " + var11.getMessage() + " " + this.getTileX() + "," + this.getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this
            );
         }

         try {
            for(Door door : this.getOrCreateOuterTile(true).getDoors()) {
               try {
                  if (door.getWall() == this) {
                     return door;
                  }
               } catch (NoSuchWallException var6) {
               }
            }
         } catch (NoSuchTileException var8) {
            logger.log(
               Level.WARNING,
               "Why: " + var8.getMessage() + " " + this.getTileX() + "," + this.getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this
            );
         } catch (NoSuchZoneException var9) {
            logger.log(
               Level.WARNING,
               "Why: " + var9.getMessage() + " " + this.getTileX() + "," + this.getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this
            );
         }
      }

      return null;
   }

   public final VolaTile getTile() {
      try {
         Structure struct = Structures.getStructure(this.structureId);
         return struct.getTileFor(this);
      } catch (NoSuchStructureException var2) {
         logger.log(
            Level.WARNING,
            "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + var2.getMessage(),
            (Throwable)var2
         );
         return null;
      }
   }

   private final void removeDoors() throws NoSuchStructureException {
      Structure struct = Structures.getStructure(this.structureId);
      if (this.isDoor()) {
         Door[] doors = struct.getAllDoors();

         for(int x = 0; x < doors.length; ++x) {
            try {
               if (doors[x].getWall() == this) {
                  struct.removeDoor(doors[x]);
                  doors[x].removeFromTiles();
               }
            } catch (NoSuchWallException var5) {
               logger.log(Level.WARNING, "Problem removing doors from wall in StructureId: " + this.structureId + " - " + var5.getMessage(), (Throwable)var5);
            }
         }
      }
   }

   private final void setPlanData() {
      this.type = StructureTypeEnum.PLAN;
      this.state = StructureStateEnum.INITIALIZED;
      this.currentQL = 1.0F;
      this.originalQL = 1.0F;
      this.damage = 0.0F;
      this.material = StructureMaterialEnum.WOOD;
   }

   private final void setRubbleData() {
      this.type = StructureTypeEnum.RUBBLE;
      this.state = StructureStateEnum.FINISHED;
      this.currentQL = 100.0F;
      this.originalQL = 1.0F;
      this.damage = 0.0F;
   }

   public final void setAsRubble() {
      try {
         Structure struct = Structures.getStructure(this.structureId);
         struct.setFinished(false);
         VolaTile tile = struct.getTileFor(this);
         if (tile != null) {
            this.removeDoors();
            this.setRubbleData();
            this.setColor(-1);
            tile.updateWall(this);
            addRubble(this);
         } else {
            logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ": no tile!?  StructureId: " + this.structureId);
         }
      } catch (NoSuchStructureException var3) {
         logger.log(
            Level.WARNING,
            "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + var3.getMessage(),
            (Throwable)var3
         );
      }
   }

   public final void setAsPlan() {
      try {
         Structure struct = Structures.getStructure(this.structureId);
         struct.setFinished(false);
         VolaTile tile = struct.getTileFor(this);
         if (tile != null) {
            this.removeDoors();
            this.setPlanData();
            this.setColor(-1);
            tile.updateWall(this);
            removeRubble(this);
         } else {
            logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ": no tile!?  StructureId: " + this.structureId);
         }
      } catch (NoSuchStructureException var3) {
         logger.log(
            Level.WARNING,
            "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + var3.getMessage(),
            (Throwable)var3
         );
      }
   }

   public final boolean isWindow() {
      return this.type == StructureTypeEnum.WINDOW || this.type == StructureTypeEnum.WIDE_WINDOW;
   }

   public final boolean isJetty() {
      return this.type == StructureTypeEnum.JETTY;
   }

   public final boolean isBalcony() {
      return this.type == StructureTypeEnum.BALCONY;
   }

   public final boolean isOriel() {
      return this.type == StructureTypeEnum.ORIEL;
   }

   public final float getDamageModifierForItem(Item item) {
      float mod = 0.0F;
      if (this.type == StructureTypeEnum.RUBBLE) {
         return 0.01F;
      } else {
         if (!this.isWood() && !this.isTimberFramed()) {
            if (this.isStone()
               || this.isPlainStone()
               || this.isSlate()
               || this.isRoundedStone()
               || this.isMarble()
               || this.isRendered()
               || this.isPottery()
               || this.isSandstone()) {
               if (item.getTemplateId() == 20) {
                  mod = 0.02F;
               } else if (item.isWeaponCrush()) {
                  mod = 0.015F;
               } else if (item.isWeaponAxe()) {
                  mod = 0.0075F;
               } else if (item.isWeaponSlash()) {
                  mod = 0.005F;
               } else if (item.isWeaponPierce()) {
                  mod = 0.005F;
               } else if (item.isWeaponMisc()) {
                  mod = 0.002F;
               }
            }
         } else if (item.isWeaponAxe()) {
            mod = 0.03F;
         } else if (item.isWeaponCrush()) {
            mod = 0.02F;
         } else if (item.isWeaponSlash()) {
            mod = 0.015F;
         } else if (item.isWeaponPierce()) {
            mod = 0.01F;
         } else if (item.isWeaponMisc()) {
            mod = 0.007F;
         }

         return mod;
      }
   }

   public final StructureMaterialEnum getMaterial() {
      return this.material;
   }

   public final void setMaterial(StructureMaterialEnum aMaterial) {
      this.material = aMaterial;
   }

   @Override
   public final boolean isStone() {
      return this.material == StructureMaterialEnum.STONE;
   }

   public final boolean isPlainStone() {
      return this.material == StructureMaterialEnum.PLAIN_STONE;
   }

   public final boolean isSlate() {
      return this.material == StructureMaterialEnum.SLATE;
   }

   public final boolean isRendered() {
      return this.material == StructureMaterialEnum.RENDERED;
   }

   public final boolean isRoundedStone() {
      return this.material == StructureMaterialEnum.ROUNDED_STONE;
   }

   public final boolean isPottery() {
      return this.material == StructureMaterialEnum.POTTERY;
   }

   public final boolean isSandstone() {
      return this.material == StructureMaterialEnum.SANDSTONE;
   }

   public final boolean isPlastered() {
      return this.material == StructureMaterialEnum.RENDERED;
   }

   public final boolean isMarble() {
      return this.material == StructureMaterialEnum.MARBLE;
   }

   public final boolean canSupportStoneBridges() {
      return this.isStone()
         || this.isPlainStone()
         || this.isMarble()
         || this.isSandstone()
         || this.isRoundedStone()
         || this.isSlate()
         || this.isRendered()
         || this.isPottery();
   }

   @Override
   public final boolean isWood() {
      return this.material == StructureMaterialEnum.WOOD;
   }

   @Override
   public final boolean isMetal() {
      return this.material == StructureMaterialEnum.METAL;
   }

   public final boolean isTimberFramed() {
      return this.material == StructureMaterialEnum.TIMBER_FRAMED;
   }

   public final int getCover() {
      if (this.isFinished()) {
         if (this.isWindow() || this.isJetty()) {
            return 70;
         } else if (this.isOriel()) {
            return 80;
         } else {
            return this.isBalcony() ? 10 : 100;
         }
      } else {
         return Math.max(0, this.getState().state);
      }
   }

   public static final int[] getItemTemplatesDealtForWall(StructureTypeEnum type, StructureStateEnum state, boolean finished) {
      if (finished) {
         int[] toReturn = new int[20];

         for(int x = 0; x < toReturn.length; ++x) {
            toReturn[x] = 22;
         }

         return toReturn;
      } else if (state.state > 0 && type != StructureTypeEnum.PLAN) {
         int[] toReturn = new int[state.state];

         for(int x = 0; x < state.state; ++x) {
            toReturn[x] = 22;
         }

         return toReturn;
      } else {
         return EMPTY_INT_ARRAY;
      }
   }

   public final void setColor(int newcolor) {
      this.changeColor(newcolor);
   }

   public final String getMaterialString() {
      if (this.isStone()) {
         return "stone";
      } else if (this.isWood()) {
         return "wood";
      } else if (this.isTimberFramed()) {
         return "timber framed";
      } else if (this.isPlainStone()) {
         return "plain stone";
      } else if (this.isSlate()) {
         return "slate";
      } else if (this.isRoundedStone()) {
         return "rounded stone";
      } else if (this.isPottery()) {
         return "pottery";
      } else if (this.isSandstone()) {
         return "sandstone";
      } else if (this.isPlastered()) {
         return "rendered";
      } else {
         return this.isMarble() ? "marble" : "wood";
      }
   }

   public final int[] getTemplateIdsNeededForNextState(StructureTypeEnum type) {
      int[] templatesNeeded;
      if (isHalfArch(type)) {
         if (this.isWood()) {
            if (this.state == StructureStateEnum.INITIALIZED) {
               templatesNeeded = new int[]{860, 217};
            } else if (this.state == StructureStateEnum.STATE_2_NEEDED) {
               templatesNeeded = new int[1];
               templatesNeeded[0] = 22;
            } else if (!this.isFinished()) {
               templatesNeeded = new int[1];
               templatesNeeded[0] = 22;
            } else {
               templatesNeeded = emptyArr;
            }
         } else if (this.isTimberFramed()) {
            if (this.state == StructureStateEnum.INITIALIZED || this.state.state < 7) {
               templatesNeeded = new int[1];
               templatesNeeded[0] = 860;
            } else if (this.state.state < 17) {
               templatesNeeded = new int[2];
               templatesNeeded[0] = 620;
               templatesNeeded[1] = 130;
            } else if (this.state.state < this.getFinalState().state) {
               templatesNeeded = new int[1];
               templatesNeeded[0] = 130;
            } else {
               templatesNeeded = emptyArr;
            }
         } else if (this.state == StructureStateEnum.INITIALIZED) {
            templatesNeeded = new int[1];
            templatesNeeded[0] = 681;
         } else if (!this.isFinished()) {
            templatesNeeded = new int[2];
            templatesNeeded[0] = this.getBrickFromType();
            templatesNeeded[1] = 492;
         } else {
            templatesNeeded = emptyArr;
         }
      } else if (this.isWood()) {
         if (this.state == StructureStateEnum.INITIALIZED) {
            templatesNeeded = new int[2];
            templatesNeeded[0] = 22;
            templatesNeeded[1] = 217;
         } else if (!this.isFinished()) {
            templatesNeeded = new int[1];
            templatesNeeded[0] = 22;
         } else {
            templatesNeeded = emptyArr;
         }
      } else if (this.isTimberFramed()) {
         if (this.state == StructureStateEnum.INITIALIZED || this.state.state < 6) {
            templatesNeeded = new int[1];
            templatesNeeded[0] = 860;
         } else if (this.state.state < 16) {
            templatesNeeded = new int[2];
            templatesNeeded[0] = 620;
            templatesNeeded[1] = 130;
         } else if (this.state.state < this.getFinalState().state) {
            templatesNeeded = new int[1];
            templatesNeeded[0] = 130;
         } else {
            templatesNeeded = emptyArr;
         }
      } else if (!this.isFinished()) {
         templatesNeeded = new int[2];
         templatesNeeded[0] = this.getBrickFromType();
         templatesNeeded[1] = 492;
      } else {
         templatesNeeded = emptyArr;
      }

      return templatesNeeded;
   }

   public final String getBrickName() {
      String brickType;
      if (this.isSlate()) {
         brickType = "slate brick";
      } else if (this.isRoundedStone()) {
         brickType = "rounded stone";
      } else if (this.isPottery()) {
         brickType = "pottery brick";
      } else if (this.isSandstone()) {
         brickType = "sandstone brick";
      } else if (this.isMarble()) {
         brickType = "marble brick";
      } else {
         brickType = "stone brick";
      }

      return brickType;
   }

   public final int getBrickFromType() {
      return getBrickFromType(this.material);
   }

   public static final int getBrickFromType(StructureMaterialEnum material) {
      if (material == StructureMaterialEnum.SLATE) {
         return 1123;
      } else if (material == StructureMaterialEnum.ROUNDED_STONE) {
         return 1122;
      } else if (material == StructureMaterialEnum.POTTERY) {
         return 776;
      } else if (material == StructureMaterialEnum.SANDSTONE) {
         return 1121;
      } else {
         return material == StructureMaterialEnum.MARBLE ? 786 : 132;
      }
   }

   public final int getRepairItemTemplate() {
      if (this.isWood()) {
         return 22;
      } else if (this.isStone() || this.isPlainStone() || this.isRendered()) {
         return 132;
      } else if (this.isRoundedStone()) {
         return 1122;
      } else if (this.isPottery()) {
         return 776;
      } else if (this.isSandstone()) {
         return 1121;
      } else if (this.isMarble()) {
         return 786;
      } else {
         return this.isSlate() ? 1123 : 22;
      }
   }

   public static final Wall getWall(long wid) {
      int x = Tiles.decodeTileX(wid);
      int y = Tiles.decodeTileY(wid);
      boolean onSurface = Tiles.decodeLayer(wid) == 0;

      for(int xx = 1; xx >= -1; --xx) {
         for(int yy = 1; yy >= -1; --yy) {
            try {
               Zone zone = Zones.getZone(x + xx, y + yy, onSurface);
               VolaTile tile = zone.getTileOrNull(x + xx, y + yy);
               if (tile != null) {
                  Wall[] wallarr = tile.getWalls();

                  for(int s = 0; s < wallarr.length; ++s) {
                     if (wallarr[s].getId() == wid) {
                        return wallarr[s];
                     }
                  }
               }
            } catch (NoSuchZoneException var11) {
            }
         }
      }

      return null;
   }

   public long getLastUsed() {
      return this.lastUsed;
   }

   public abstract void save() throws IOException;

   abstract void load() throws IOException;

   public abstract void setLastUsed(long var1);

   public abstract void improveOrigQualityLevel(float var1);

   abstract boolean changeColor(int var1);

   public abstract void setWallOrientation(boolean var1);

   public abstract void delete();

   public final int getHeight() {
      return this.heightOffset;
   }

   public void setHeightOffset(int newHeightOffset) {
      this.heightOffset = newHeightOffset;
      this.setFloorLevel();
   }

   public final boolean isOnFloorLevel(int level) {
      return level == this.floorLevel;
   }

   @Override
   public final int getFloorLevel() {
      return this.floorLevel;
   }

   private final void setFloorLevel() {
      this.floorLevel = this.heightOffset / 30;
   }

   @Override
   public float getFloorZ() {
      return (float)(this.heightOffset / 10);
   }

   @Override
   public float getMinZ() {
      return Zones.getHeightForNode(this.getTileX(), this.getTileY(), this.getLayer()) + this.getFloorZ();
   }

   @Override
   public float getMaxZ() {
      return this.getMinZ() + 3.0F;
   }

   @Override
   public boolean isWithinZ(float maxZ, float minZ, boolean followGround) {
      return this.getFloorLevel() == 0 && followGround || minZ <= this.getMaxZ() && maxZ >= this.getMinZ();
   }

   public byte getLayer() {
      return this.layer;
   }

   @Override
   public boolean isOnSurface() {
      return this.layer == 0;
   }

   public abstract void setIndoor(boolean var1);

   public boolean isIndoor() {
      return this.isIndoor;
   }

   public void destroy() {
      if (!this.isIndoor()) {
         this.setAsPlan();
      } else if (!MethodsStructure.isWallInsideStructure(this, this.isOnSurface())) {
         this.setAsPlan();
      } else {
         removeRubble(this);
         this.removeIndoorWall();
      }
   }

   private final void removeIndoorWall() {
      if (!this.isIndoor()) {
         logger.log(Level.WARNING, "Tried to wall.remove() completely for an outdoor wall!");
      } else if (!MethodsStructure.isWallInsideStructure(this, this.isOnSurface())) {
         logger.log(Level.WARNING, "Tried to wall.remove() completely next to a wall without structure tiles on both sides!");
      } else {
         try {
            this.removeDoors();
         } catch (NoSuchStructureException var3) {
            logger.log(Level.WARNING, "Structure not found when trying to remove doors from wall " + this.getStructureId());
            return;
         }

         this.delete();
         VolaTile myTile = this.getTile();
         if (myTile != null) {
            myTile.removeWall(this, false);
         } else {
            logger.log(Level.INFO, this.getName() + " at " + this.getTileX() + "," + this.getTileY() + " not removed from tile since we couldn't locate it.");
         }

         Set<Wall> flset = walls.get(this.getStructureId());
         if (flset != null) {
            flset.remove(this);
         }
      }
   }

   public boolean isWallPlan() {
      return this.getState() == StructureStateEnum.INITIALIZED;
   }

   public final boolean isRubble() {
      return this.getType() == StructureTypeEnum.RUBBLE;
   }

   public boolean isAlwaysOpen() {
      return this.isArched();
   }

   @Override
   public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel) {
      return this.floorLevel <= maxFloorLevel && this.floorLevel >= minFloorLevel;
   }

   @Override
   public boolean supports(StructureSupport support) {
      if (!this.supports()) {
         return false;
      } else {
         if (support.isFloor()) {
            if (this.getFloorLevel() == support.getFloorLevel() || this.getFloorLevel() == support.getFloorLevel() - 1) {
               if (this.isHorizontal()) {
                  if (this.getMinX() == support.getMinX() && (this.getMinY() == support.getStartY() || this.getStartY() == support.getEndY())) {
                     return true;
                  }
               } else if (this.getMinY() == support.getMinY() && (this.getMinX() == support.getStartX() || this.getMinX() == support.getEndX())) {
                  return true;
               }
            }
         } else {
            int levelMod = support.supports() ? -1 : 0;
            if (support.getFloorLevel() >= this.getFloorLevel() + levelMod
               && support.getFloorLevel() <= this.getFloorLevel() + 1
               && support.getMinX() == this.getMinX()
               && support.getMinY() == this.getMinY()
               && this.isHorizontal() == support.isHorizontal()) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public final boolean supports() {
      return true;
   }

   @Override
   public boolean isSupportedByGround() {
      return this.getFloorLevel() == 0;
   }

   @Override
   public String toString() {
      return "Wall [number="
         + this.number
         + ", structureId="
         + this.structureId
         + ", type="
         + this.type
         + ", material="
         + this.getMaterial()
         + ", QL="
         + this.getQualityLevel()
         + ", DMG="
         + this.getDamage()
         + "]";
   }

   @Override
   public long getTempId() {
      return -10L;
   }

   public String getTypeName() {
      return WallEnum.getWall(this.getType(), this.getMaterial()).getName();
   }

   public boolean setTile(int newTileX, int newTileY) {
      this.tilex = newTileX;
      this.tiley = newTileY;

      try {
         this.save();
      } catch (IOException var4) {
         logger.log(Level.WARNING, StringUtil.format("Failed to move wall to %d,%d: %s", newTileX, newTileY, var4.getMessage()), (Throwable)var4);
      }

      return true;
   }

   void setSettings(int aSettings) {
      this.permissions.setPermissionBits(aSettings);
   }

   Permissions getSettings() {
      return this.permissions;
   }

   @Override
   public boolean canBeAlwaysLit() {
      return false;
   }

   @Override
   public boolean canBeAutoFilled() {
      return false;
   }

   @Override
   public boolean canBeAutoLit() {
      return false;
   }

   @Override
   public final boolean canBePeggedByPlayer() {
      return false;
   }

   @Override
   public boolean canBePlanted() {
      return false;
   }

   @Override
   public final boolean canBeSealedByPlayer() {
      return false;
   }

   @Override
   public boolean canChangeCreator() {
      return false;
   }

   @Override
   public boolean canDisableDecay() {
      return true;
   }

   @Override
   public boolean canDisableDestroy() {
      return true;
   }

   @Override
   public boolean canDisableDrag() {
      return false;
   }

   @Override
   public boolean canDisableDrop() {
      return false;
   }

   @Override
   public boolean canDisableEatAndDrink() {
      return false;
   }

   @Override
   public boolean canDisableImprove() {
      return true;
   }

   @Override
   public boolean canDisableLocking() {
      return this.isDoor();
   }

   @Override
   public boolean canDisableLockpicking() {
      return this.isDoor();
   }

   @Override
   public boolean canDisableMoveable() {
      return false;
   }

   @Override
   public final boolean canDisableOwnerMoveing() {
      return false;
   }

   @Override
   public final boolean canDisableOwnerTurning() {
      return false;
   }

   @Override
   public boolean canDisablePainting() {
      return true;
   }

   @Override
   public boolean canDisablePut() {
      return false;
   }

   @Override
   public boolean canDisableRepair() {
      return true;
   }

   @Override
   public boolean canDisableRuneing() {
      return false;
   }

   @Override
   public boolean canDisableSpellTarget() {
      return false;
   }

   @Override
   public boolean canDisableTake() {
      return false;
   }

   @Override
   public boolean canDisableTurning() {
      return true;
   }

   @Override
   public boolean canHaveCourier() {
      return false;
   }

   @Override
   public boolean canHaveDakrMessenger() {
      return false;
   }

   @Override
   public String getCreatorName() {
      return null;
   }

   @Override
   public float getDamage() {
      return this.damage;
   }

   @Override
   public float getQualityLevel() {
      return this.currentQL;
   }

   @Override
   public boolean hasCourier() {
      return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
   }

   @Override
   public boolean hasDarkMessenger() {
      return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
   }

   @Override
   public boolean hasNoDecay() {
      return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
   }

   @Override
   public boolean isAlwaysLit() {
      return this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit());
   }

   @Override
   public boolean isAutoFilled() {
      return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
   }

   @Override
   public boolean isAutoLit() {
      return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
   }

   @Override
   public boolean isIndestructible() {
      return this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit());
   }

   @Override
   public boolean isNoDrag() {
      return this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit());
   }

   @Override
   public boolean isNoDrop() {
      return this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit());
   }

   @Override
   public boolean isNoEatOrDrink() {
      return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
   }

   @Override
   public boolean isNoImprove() {
      return this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit());
   }

   @Override
   public boolean isNoMove() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit());
   }

   @Override
   public boolean isNoPut() {
      return this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit());
   }

   @Override
   public boolean isNoRepair() {
      return this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit());
   }

   @Override
   public boolean isNoTake() {
      return this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit());
   }

   @Override
   public boolean isNotLockable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit());
   }

   @Override
   public boolean isNotLockpickable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit());
   }

   @Override
   public boolean isNotPaintable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit());
   }

   @Override
   public boolean isNotRuneable() {
      return true;
   }

   @Override
   public boolean isNotSpellTarget() {
      return this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit());
   }

   @Override
   public boolean isNotTurnable() {
      return this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit());
   }

   @Override
   public boolean isOwnerMoveable() {
      return this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit());
   }

   @Override
   public boolean isOwnerTurnable() {
      return this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit());
   }

   @Override
   public boolean isPlanted() {
      return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
   }

   @Override
   public final boolean isSealedByPlayer() {
      return this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit());
   }

   @Override
   public void setCreator(String aNewCreator) {
   }

   @Override
   public abstract boolean setDamage(float var1);

   @Override
   public void setHasCourier(boolean aCourier) {
      this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
   }

   @Override
   public void setHasDarkMessenger(boolean aDarkmessenger) {
      this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
   }

   @Override
   public void setHasNoDecay(boolean aNoDecay) {
      this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
   }

   @Override
   public void setIsAlwaysLit(boolean aAlwaysLit) {
      this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
   }

   @Override
   public void setIsAutoFilled(boolean aAutoFill) {
      this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
   }

   @Override
   public void setIsAutoLit(boolean aAutoLight) {
      this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
   }

   @Override
   public void setIsIndestructible(boolean aNoDestroy) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
   }

   @Override
   public void setIsNoDrag(boolean aNoDrag) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
   }

   @Override
   public void setIsNoDrop(boolean aNoDrop) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
   }

   @Override
   public void setIsNoEatOrDrink(boolean aNoEatOrDrink) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
   }

   @Override
   public void setIsNoImprove(boolean aNoImprove) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
   }

   @Override
   public void setIsNoMove(boolean aNoMove) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
   }

   @Override
   public void setIsNoPut(boolean aNoPut) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
   }

   @Override
   public void setIsNoRepair(boolean aNoRepair) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
   }

   @Override
   public void setIsNoTake(boolean aNoTake) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
   }

   @Override
   public void setIsNotLockable(boolean aNoLock) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
   }

   @Override
   public void setIsNotLockpickable(boolean aNoLockpick) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
   }

   @Override
   public void setIsNotPaintable(boolean aNoPaint) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
   }

   @Override
   public void setIsNotRuneable(boolean aNoRune) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
   }

   @Override
   public void setIsNotSpellTarget(boolean aNoSpells) {
      this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
   }

   @Override
   public void setIsNotTurnable(boolean aNoTurn) {
      this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
   }

   @Override
   public void setIsOwnerMoveable(boolean aOwnerMove) {
      this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
   }

   @Override
   public void setIsOwnerTurnable(boolean aOwnerTurn) {
      this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
   }

   @Override
   public void setIsPlanted(boolean aPlant) {
      this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
   }

   @Override
   public void setIsSealedByPlayer(boolean aSealed) {
      this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
   }

   @Override
   public abstract boolean setQualityLevel(float var1);

   @Override
   public void setOriginalQualityLevel(float newQL) {
   }

   @Override
   public abstract void savePermissions();

   @Override
   public final boolean isOnSouthBorder(TilePos pos) {
      return (this.getStartX() == pos.x || this.getEndX() == pos.x) && this.getEndY() == pos.y + 1 && this.getStartY() == pos.y + 1;
   }

   @Override
   public final boolean isOnNorthBorder(TilePos pos) {
      return (this.getStartX() == pos.x || this.getEndX() == pos.x) && this.getEndY() == pos.y && this.getStartY() == pos.y;
   }

   @Override
   public final boolean isOnWestBorder(TilePos pos) {
      return this.getStartX() == pos.x && this.getEndX() == pos.x && (this.getEndY() == pos.y || this.getStartY() == pos.y);
   }

   @Override
   public final boolean isOnEastBorder(TilePos pos) {
      return this.getStartX() == pos.x + 1 && this.getEndX() == pos.x + 1 && (this.getEndY() == pos.y || this.getStartY() == pos.y);
   }
}

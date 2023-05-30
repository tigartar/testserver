package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Structures implements MiscConstants, CounterTypes {
   private static final String GET_STRUCTURES = "SELECT * FROM STRUCTURES";
   private static Map<Long, Structure> structures;
   private static Map<Long, Structure> bridges;
   private static final Structure[] emptyStructures = new Structure[0];
   private static final Logger logger = Logger.getLogger(Structures.class.getName());

   private Structures() {
   }

   public static int getNumberOfStructures() {
      return structures != null ? structures.size() : 0;
   }

   public static final Structure[] getAllStructures() {
      return structures == null ? emptyStructures : structures.values().toArray(new Structure[structures.size()]);
   }

   public static final Structure[] getManagedBuildingsFor(Player player, int villageId, boolean includeAll) {
      if (structures == null) {
         return emptyStructures;
      } else {
         Set<Structure> buildings = new HashSet<>();

         for(Structure structure : structures.values()) {
            if (structure.isTypeHouse()) {
               if (structure.canManage(player)) {
                  buildings.add(structure);
               }

               if (includeAll && (villageId >= 0 && structure.getVillageId() == villageId || structure.isActualOwner(player.getWurmId()))) {
                  buildings.add(structure);
               }

               if (structure.getWritid() != -10L && structure.isActualOwner(player.getWurmId())) {
                  Items.destroyItem(structure.getWritId());
                  structure.setWritid(-10L, true);
               }
            }
         }

         return buildings.toArray(new Structure[buildings.size()]);
      }
   }

   public static final Structure[] getOwnedBuildingFor(Player player) {
      if (structures == null) {
         return emptyStructures;
      } else {
         Set<Structure> buildings = new HashSet<>();

         for(Structure structure : structures.values()) {
            if (structure.isTypeHouse() && (structure.isOwner(player) || structure.isActualOwner(player.getWurmId()))) {
               buildings.add(structure);
            }
         }

         return buildings.toArray(new Structure[buildings.size()]);
      }
   }

   public static final Structure getStructureOrNull(long id) {
      Structure structure = null;
      if (structures == null) {
         structures = new ConcurrentHashMap<>();
      } else {
         structure = structures.get(new Long(id));
      }

      if (structure == null && WurmId.getType(id) == 4) {
         try {
            structure = loadStructure(id);
            addStructure(structure);
         } catch (IOException var4) {
         } catch (NoSuchStructureException var5) {
         }
      }

      return structure;
   }

   public static final Structure getStructure(long id) throws NoSuchStructureException {
      Structure structure = getStructureOrNull(id);
      if (structure == null) {
         throw new NoSuchStructureException("No such structure.");
      } else {
         return structure;
      }
   }

   public static void addStructure(Structure structure) {
      if (structures == null) {
         structures = new ConcurrentHashMap<>();
      }

      structures.put(new Long(structure.getWurmId()), structure);
      if (structure.isTypeBridge()) {
         addBridge(structure);
      }
   }

   public static final void addBridge(Structure bridge) {
      if (bridges == null) {
         bridges = new ConcurrentHashMap<>();
      }

      bridges.put(new Long(bridge.getWurmId()), bridge);
   }

   public static void removeBridge(long id) {
      if (bridges != null) {
         bridges.remove(new Long(id));
      }
   }

   public static final Structure getBridge(long id) {
      Structure bridge = null;
      if (bridges != null) {
         bridge = bridges.get(new Long(id));
      }

      return bridge;
   }

   public static void removeStructure(long id) {
      if (structures != null) {
         structures.remove(new Long(id));
      }
   }

   public static final Structure createStructure(byte theStructureType, String name, long id, int startx, int starty, boolean surfaced) {
      Structure toReturn = null;
      Structure var8 = new DbStructure(theStructureType, name, id, startx, starty, surfaced);
      addStructure(var8);
      return var8;
   }

   private static final Structure loadStructure(long id) throws IOException, NoSuchStructureException {
      Structure toReturn = null;
      Structure var3 = new DbStructure(id);
      addStructure(var3);
      return var3;
   }

   public static Structure getStructureForWrit(long writId) throws NoSuchStructureException {
      if (writId == -10L) {
         throw new NoSuchStructureException("No structure for writid " + writId);
      } else {
         for(Structure s : structures.values()) {
            if (s.getWritId() == writId) {
               return s;
            }
         }

         throw new NoSuchStructureException("No structure for writid " + writId);
      }
   }

   public static void endLoadAll() {
      if (structures != null) {
         for(Structure struct : structures.values()) {
            try {
               struct.endLoading();
            } catch (IOException var3) {
               logger.log(Level.WARNING, var3.getMessage() + ": " + struct.getWurmId() + " writ " + struct.getWritid());
            }
         }
      }
   }

   public static void loadAllStructures() throws IOException {
      logger.info("Loading all Structures");
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM STRUCTURES");

         long wurmid;
         byte structureType;
         boolean surfaced;
         byte roof;
         String name;
         boolean finished;
         boolean finalfinished;
         boolean allowsCitizens;
         boolean allowsAllies;
         boolean allowsKingdom;
         String planner;
         long ownerId;
         int settings;
         int villageId;
         long writid;
         for(rs = ps.executeQuery();
            rs.next();
            addStructure(
               new DbStructure(
                  structureType,
                  name,
                  wurmid,
                  surfaced,
                  roof,
                  finished,
                  finalfinished,
                  writid,
                  planner,
                  ownerId,
                  settings,
                  villageId,
                  allowsCitizens,
                  allowsAllies,
                  allowsKingdom
               )
            )
         ) {
            wurmid = rs.getLong("WURMID");
            structureType = rs.getByte("STRUCTURETYPE");
            surfaced = rs.getBoolean("SURFACED");
            roof = rs.getByte("ROOF");
            name = rs.getString("NAME");
            if (name == null) {
               name = "Unknown structure";
            }

            if (name.length() >= 50) {
               name = name.substring(0, 49);
            }

            finished = rs.getBoolean("FINISHED");
            finalfinished = rs.getBoolean("FINFINISHED");
            allowsCitizens = rs.getBoolean("ALLOWSVILLAGERS");
            allowsAllies = rs.getBoolean("ALLOWSALLIES");
            allowsKingdom = rs.getBoolean("ALLOWSKINGDOM");
            planner = rs.getString("PLANNER");
            ownerId = rs.getLong("OWNERID");
            settings = rs.getInt("SETTINGS");
            villageId = rs.getInt("VILLAGE");
            writid = -10L;

            try {
               writid = rs.getLong("WRITID");
            } catch (Exception var29) {
               if (structureType == 0) {
                  logger.log(Level.INFO, "No writ for house with id:" + wurmid + " creating new after loading.", (Throwable)var29);
               }
            }
         }
      } catch (SQLException var30) {
         throw new IOException(var30);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         int numberOfStructures = structures != null ? structures.size() : 0;
         logger.log(
            Level.INFO,
            "Structures loaded. Number of structures=" + numberOfStructures + ". That took " + (float)(System.nanoTime() - start) / 1000000.0F + " ms."
         );
      }
   }

   public static Structure getStructureForTile(int tilex, int tiley, boolean onSurface) {
      if (structures != null) {
         for(Structure s : structures.values()) {
            if (s.isOnSurface() == onSurface && s.contains(tilex, tiley)) {
               return s;
            }
         }
      }

      return null;
   }

   public static Structure getBuildingForTile(int tilex, int tiley) {
      if (structures != null) {
         for(Structure s : structures.values()) {
            if (s.contains(tilex, tiley)) {
               return s;
            }
         }
      }

      return null;
   }

   public static final void createRandomStructure(
      Creature creator, int stx, int endtx, int sty, int endty, int centerx, int centery, byte material, String sname
   ) {
      if (creator.getCurrentTile() == null || creator.getCurrentTile().getStructure() == null) {
         try {
            Structure struct = createStructure((byte)0, sname, WurmId.getNextPlanId(), centerx, centery, true);

            for(int currx = stx; currx <= endtx; ++currx) {
               for(int curry = sty; curry <= endty; ++curry) {
                  if (currx != stx || curry != sty && Server.rand.nextInt(3) < 2) {
                     VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
                     struct.addBuildTile(vtile, false);
                     struct.clearAllWallsAndMakeWallsForStructureBorder(vtile);
                  }
               }
            }

            float rot = Creature.normalizeAngle(creator.getStatus().getRotation());
            struct.makeFinal(creator, sname);

            for(VolaTile bt : struct.getStructureTiles()) {
               StructureTypeEnum wtype = StructureTypeEnum.SOLID;
               if (Server.rand.nextInt(2) == 0) {
                  wtype = StructureTypeEnum.WINDOW;
               }

               for(Wall plan : bt.getWalls()) {
                  if (!plan.isHorizontal() && plan.getStartY() == creator.getTileY() && rot <= 315.0F && rot >= 235.0F) {
                     wtype = StructureTypeEnum.DOOR;
                  }

                  if (plan.isHorizontal() && plan.getStartX() == creator.getTileX() && (rot >= 315.0F && rot <= 360.0F || rot >= 0.0F && rot <= 45.0F)) {
                     wtype = StructureTypeEnum.DOOR;
                  }

                  if (plan.isHorizontal() && plan.getStartX() == creator.getTileX() && rot >= 135.0F && rot <= 215.0F) {
                     wtype = StructureTypeEnum.DOOR;
                  }

                  if (!plan.isHorizontal() && plan.getStartY() == creator.getTileY() && rot <= 135.0F && rot >= 45.0F) {
                     wtype = StructureTypeEnum.DOOR;
                  }

                  if (material == 15) {
                     plan.setMaterial(StructureMaterialEnum.STONE);
                  } else {
                     plan.setMaterial(StructureMaterialEnum.WOOD);
                  }

                  plan.setType(wtype);
                  plan.setQualityLevel(80.0F);
                  plan.setState(StructureStateEnum.FINISHED);
                  bt.updateWall(plan);
                  if (plan.isDoor()) {
                     Door door = new DbDoor(plan);
                     door.setStructureId(struct.getWurmId());
                     struct.addDoor(door);
                     door.save();
                     door.addToTiles();
                  }
               }
            }

            struct.setFinished(true);
            struct.setFinalFinished(true);

            for(VolaTile bt : struct.getStructureTiles()) {
               Floor floor = new DbFloor(
                  StructureConstants.FloorType.FLOOR, bt.getTileX(), bt.getTileY(), 0, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.WOOD, 0
               );
               floor.setFloorState(StructureConstants.FloorState.COMPLETED);
               bt.addFloor(floor);
               floor.save();
               Floor roof = new DbFloor(
                  StructureConstants.FloorType.ROOF, bt.getTileX(), bt.getTileY(), 30, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.THATCH, 0
               );
               roof.setFloorState(StructureConstants.FloorState.COMPLETED);
               bt.addFloor(roof);
               roof.save();
            }
         } catch (Exception var21) {
            logger.log(Level.WARNING, "exception " + var21, (Throwable)var21);
            creator.getCommunicator().sendAlertServerMessage(var21.getMessage());
         }
      }
   }

   public static final void createSquareStructure(
      Creature creator, int stx, int endtx, int sty, int endty, int centerx, int centery, byte material, String sname
   ) {
      if (creator.getCurrentTile() == null || creator.getCurrentTile().getStructure() == null) {
         try {
            Structure struct = createStructure((byte)0, sname, WurmId.getNextPlanId(), centerx, centery, true);

            for(int currx = stx; currx <= endtx; ++currx) {
               for(int curry = sty; curry <= endty; ++curry) {
                  VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
                  struct.addBuildTile(vtile, false);
                  struct.clearAllWallsAndMakeWallsForStructureBorder(vtile);
               }
            }

            float rot = Creature.normalizeAngle(creator.getStatus().getRotation());
            struct.makeFinal(creator, sname);

            for(int currx = stx; currx <= endtx; ++currx) {
               for(int curry = sty; curry <= endty; ++curry) {
                  VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
                  StructureTypeEnum wtype = StructureTypeEnum.SOLID;
                  if (Server.rand.nextInt(2) == 0) {
                     wtype = StructureTypeEnum.WINDOW;
                  }

                  if (currx == stx) {
                     for(Wall plan : vtile.getWalls()) {
                        if (!plan.isHorizontal() && plan.getStartX() == currx) {
                           if (curry == creator.getTileY() && rot <= 315.0F && rot >= 235.0F) {
                              wtype = StructureTypeEnum.DOOR;
                           }

                           if (material == 15) {
                              plan.setMaterial(StructureMaterialEnum.STONE);
                           } else {
                              plan.setMaterial(StructureMaterialEnum.WOOD);
                           }

                           plan.setType(wtype);
                           plan.setQualityLevel(80.0F);
                           plan.setState(StructureStateEnum.FINISHED);
                           vtile.updateWall(plan);
                           if (plan.isDoor()) {
                              Door door = new DbDoor(plan);
                              door.setStructureId(struct.getWurmId());
                              struct.addDoor(door);
                              door.save();
                              door.addToTiles();
                           }
                        }
                     }
                  }

                  if (curry == sty) {
                     for(Wall plan : vtile.getWalls()) {
                        if (plan.isHorizontal() && plan.getStartY() == curry) {
                           if (currx == creator.getTileX() && (rot >= 315.0F && rot <= 360.0F || rot >= 0.0F && rot <= 45.0F)) {
                              wtype = StructureTypeEnum.DOOR;
                           }

                           if (material == 15) {
                              plan.setMaterial(StructureMaterialEnum.STONE);
                           } else {
                              plan.setMaterial(StructureMaterialEnum.WOOD);
                           }

                           plan.setType(wtype);
                           plan.setQualityLevel(80.0F);
                           plan.setState(StructureStateEnum.FINISHED);
                           vtile.updateWall(plan);
                           if (plan.isDoor()) {
                              Door door = new DbDoor(plan);
                              door.setStructureId(struct.getWurmId());
                              struct.addDoor(door);
                              door.save();
                              door.addToTiles();
                           }
                        }
                     }
                  }

                  if (curry == endty) {
                     for(Wall plan : vtile.getWalls()) {
                        if (plan.isHorizontal() && plan.getStartY() == curry + 1) {
                           if (currx == creator.getTileX() && rot >= 135.0F && rot <= 215.0F) {
                              wtype = StructureTypeEnum.DOOR;
                           }

                           if (material == 15) {
                              plan.setMaterial(StructureMaterialEnum.STONE);
                           } else {
                              plan.setMaterial(StructureMaterialEnum.WOOD);
                           }

                           plan.setType(wtype);
                           plan.setQualityLevel(80.0F);
                           plan.setState(StructureStateEnum.FINISHED);
                           vtile.updateWall(plan);
                           if (plan.isDoor()) {
                              Door door = new DbDoor(plan);
                              door.setStructureId(struct.getWurmId());
                              struct.addDoor(door);
                              door.save();
                              door.addToTiles();
                           }
                        }
                     }
                  }

                  if (currx == endtx) {
                     for(Wall plan : vtile.getWalls()) {
                        if (!plan.isHorizontal() && plan.getStartX() == currx + 1) {
                           if (curry == creator.getTileY() && rot <= 135.0F && rot >= 45.0F) {
                              wtype = StructureTypeEnum.DOOR;
                           }

                           if (material == 15) {
                              plan.setMaterial(StructureMaterialEnum.STONE);
                           } else {
                              plan.setMaterial(StructureMaterialEnum.WOOD);
                           }

                           plan.setType(wtype);
                           plan.setQualityLevel(80.0F);
                           plan.setState(StructureStateEnum.FINISHED);
                           vtile.updateWall(plan);
                           if (plan.isDoor()) {
                              Door door = new DbDoor(plan);
                              door.setStructureId(struct.getWurmId());
                              struct.addDoor(door);
                              door.save();
                              door.addToTiles();
                           }
                        }
                     }
                  }
               }
            }

            struct.setFinished(true);
            struct.setFinalFinished(true);

            for(int currx = stx; currx <= endtx; ++currx) {
               for(int curry = sty; curry <= endty; ++curry) {
                  VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
                  Floor floor = new DbFloor(
                     StructureConstants.FloorType.FLOOR, currx, curry, 0, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.WOOD, 0
                  );
                  floor.setFloorState(StructureConstants.FloorState.COMPLETED);
                  vtile.addFloor(floor);
                  floor.save();
                  Floor roof = new DbFloor(
                     StructureConstants.FloorType.ROOF, currx, curry, 30, 80.0F, struct.getWurmId(), StructureConstants.FloorMaterial.THATCH, 0
                  );
                  roof.setFloorState(StructureConstants.FloorState.COMPLETED);
                  vtile.addFloor(roof);
                  roof.save();
               }
            }
         } catch (Exception var20) {
            logger.log(Level.WARNING, "exception " + var20, (Throwable)var20);
            creator.getCommunicator().sendAlertServerMessage(var20.getMessage());
         }
      }
   }
}

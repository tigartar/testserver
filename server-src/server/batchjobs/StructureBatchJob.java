package com.wurmonline.server.batchjobs;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.structures.DbDoor;
import com.wurmonline.server.structures.DbWall;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StructureBatchJob {
   private static final String DELETE_DOORS = "DELETE FROM DOORS WHERE LOCKID<=0";
   private static final String DELETE_GATES = "DELETE FROM GATES WHERE LOCKID<=0";
   private static final String LOAD_WALLS = "SELECT * FROM WALLS";
   private static final String LOAD_FENCES = "SELECT * FROM FENCES";
   private static final String updateGate = "UPDATE GATES SET ID=? WHERE ID=?";
   private static Logger logger = Logger.getLogger(StructureBatchJob.class.getName());

   private StructureBatchJob() {
   }

   public static final void runBatch1() {
      logger.log(Level.INFO, "Running batch 1.");

      try {
         Map<Long, LinkedList<Door>> doors = new HashMap<>();
         Map<Long, LinkedList<Wall>> walls = new HashMap<>();
         Connection dbcon = DbConnector.getZonesDbCon();
         PreparedStatement psA = dbcon.prepareStatement("DELETE FROM DOORS WHERE LOCKID<=0");
         psA.executeUpdate();
         psA.close();
         PreparedStatement ps = dbcon.prepareStatement("SELECT * FROM WALLS");
         ResultSet rs = ps.executeQuery();

         while(rs.next()) {
            try {
               Wall wall = new DbWall(rs.getInt("ID"));
               wall.x1 = rs.getInt("STARTX");
               wall.x2 = rs.getInt("ENDX");
               wall.y1 = rs.getInt("STARTY");
               wall.y2 = rs.getInt("ENDY");
               wall.tilex = rs.getInt("TILEX");
               wall.tiley = rs.getInt("TILEY");
               wall.currentQL = rs.getFloat("ORIGINALQL");
               wall.originalQL = rs.getFloat("CURRENTQL");
               wall.lastUsed = rs.getLong("LASTMAINTAINED");
               wall.structureId = rs.getLong("STRUCTURE");
               wall.type = StructureTypeEnum.getTypeByINDEX(rs.getByte("TYPE"));
               wall.state = StructureStateEnum.getStateByValue(rs.getByte("STATE"));
               wall.damage = rs.getFloat("DAMAGE");
               wall.setColor(rs.getInt("COLOR"));
               wall.setIndoor(rs.getBoolean("ISINDOOR"));
               wall.heightOffset = rs.getInt("HEIGHTOFFSET");
               if (wall.getType() == StructureTypeEnum.DOOR || wall.getType() == StructureTypeEnum.DOUBLE_DOOR || wall.isArched()) {
                  LinkedList<Wall> wallist = walls.get(wall.structureId);
                  if (wallist == null) {
                     wallist = new LinkedList<>();
                     walls.put(wall.structureId, wallist);
                  }

                  wallist.add(wall);
                  LinkedList<Door> doorlist = doors.get(wall.structureId);
                  if (doorlist == null) {
                     doorlist = new LinkedList<>();
                     doors.put(wall.structureId, doorlist);
                  }

                  boolean updated = false;
                  Door door = new DbDoor(wall);
                  doorlist.add(door);
                  doors.put(wall.structureId, doorlist);
               }
            } catch (IOException var11) {
               logger.log(Level.INFO, "IOException");
            }
         }

         ps.close();
         rs.close();
      } catch (SQLException var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
      }

      logger.log(Level.INFO, "Done running batch 1.");
   }

   public static final void convertToNewPermissions() {
      logger.log(Level.INFO, "Converting Structures to New Permission System.");
      int structuresDone = 0;

      for(Structure structure : Structures.getAllStructures()) {
         if (structure.convertToNewPermissions()) {
            ++structuresDone;
         }
      }

      logger.log(Level.INFO, "Converted " + structuresDone + " structures to New Permissions System.");
   }

   public static final void convertGatesToNewPermissions() {
      logger.log(Level.INFO, "Converting Gates to New Permission System.");
      int gatesDone = 0;

      for(FenceGate gate : FenceGate.getAllGates()) {
         if (gate.convertToNewPermissions()) {
            ++gatesDone;
         }
      }

      logger.log(Level.INFO, "Converted " + gatesDone + " gates to New Permissions System.");
   }

   public static final void fixGatesForNewPermissions() {
      logger.log(Level.INFO, "fixing Gates for New Permission System.");
      int gatesDone = 0;

      for(FenceGate gate : FenceGate.getAllGates()) {
         if (gate.fixForNewPermissions()) {
            ++gatesDone;
         }
      }

      logger.log(Level.INFO, "Fixed " + gatesDone + " gates to New Permissions System.");
   }
}

package com.wurmonline.server.creatures;

import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.CreaturePositionDatabaseUpdater;
import com.wurmonline.server.utils.CreaturePositionDbUpdatable;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.PlayerPositionDatabaseUpdater;
import com.wurmonline.server.utils.PlayerPositionDbUpdatable;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaturePos implements CounterTypes, TimeConstants, MiscConstants {
   private static final Logger logger = Logger.getLogger(CreaturePos.class.getName());
   private static final String createPos = "insert into POSITION (POSX, POSY, POSZ, ROTATION,ZONEID,LAYER,ONBRIDGE, WURMID) values (?,?,?,?,?,?,?,?)";
   private static final String updatePosOld = "update POSITION set POSX=?, POSY=?, POSZ=?, ROTATION=?,ZONEID=?,LAYER=?,ONBRIDGE=? where WURMID=?";
   private static final String updatePos = "INSERT INTO POSITION (POSX, POSY, POSZ, ROTATION, ZONEID, LAYER, ONBRIDGE, WURMID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE POSX=VALUES(POSX), POSY=VALUES(POSY), POSZ=VALUES(POSZ), ROTATION=VALUES(ROTATION), ZONEID=VALUES(ZONEID), LAYER=VALUES(LAYER), ONBRIDGE=VALUES(ONBRIDGE)";
   private static final String loadAllPos = "SELECT * FROM POSITION";
   private static final String deletePosition = "DELETE FROM POSITION WHERE WURMID=?";
   private boolean changed = false;
   private float posX;
   private float posY;
   private float posZ;
   private float rotation;
   private int zoneId;
   private int layer;
   private long bridgeId = -10L;
   private final long wurmid;
   private final boolean isPlayer;
   protected long lastSavedPos = System.currentTimeMillis() - (long)Server.rand.nextInt(2000000);
   public static boolean logCreaturePos = false;
   protected static final long saveIntervalPlayer = 60000L;
   protected static final long saveIntervalCreature = 600000L;
   private static final ConcurrentHashMap<Long, CreaturePos> allPositions = new ConcurrentHashMap<>();
   private static PreparedStatement cretPosPS = null;
   private static int cretPosPSCount = 0;
   public static int totalCretPosPSCount = 0;
   private static PreparedStatement playPosPS = null;
   private static int playPosPSCount = 0;
   public static int totalPlayPosPSCount = 0;
   private static final CreaturePositionDatabaseUpdater creatureDbPosUpdater = new CreaturePositionDatabaseUpdater(
      "Creature Database Position Updater", Constants.numberOfDbCreaturePositionsToUpdateEachTime
   );
   private static final PlayerPositionDatabaseUpdater playerDbPosUpdater = new PlayerPositionDatabaseUpdater(
      "Player Database Position Updater", Constants.numberOfDbPlayerPositionsToUpdateEachTime
   );

   public CreaturePos(long wurmId, float posx, float posy, float posz, float rot, int zone, int layerId, long bridge, boolean createInDatabase) {
      this.wurmid = wurmId;
      this.isPlayer = WurmId.getType(this.wurmid) == 0;
      this.setPosX(posx);
      this.setPosY(posy);
      this.setPosZ(posz, false);
      this.rotation = rot;
      this.setZoneId(zone);
      this.setLayer(layerId);
      this.setBridgeId(bridge);
      allPositions.put(this.wurmid, this);
      if (createInDatabase) {
         this.changed = true;
         this.save(true);
      }
   }

   public boolean isChanged() {
      return this.changed;
   }

   public void setChanged(boolean hasChanged) {
      this.changed = hasChanged;
   }

   public final Vector2f getPos2f() {
      return new Vector2f(this.posX, this.posY);
   }

   public final Vector3f getPos3f() {
      return new Vector3f(this.posX, this.posY, this.posZ);
   }

   public float getPosX() {
      return this.posX;
   }

   public void setPosX(float posx) {
      if (this.posX != posx) {
         if ((int)this.posX >> 2 != (int)posx >> 2) {
            this.changed = true;
         }

         this.posX = posx;
      }
   }

   public float getPosY() {
      return this.posY;
   }

   public void setPosY(float posy) {
      if (this.posY != posy) {
         if ((int)this.posY >> 2 != (int)posy >> 2) {
            this.changed = true;
         }

         this.posY = posy;
      }
   }

   public float getPosZ() {
      return this.posZ;
   }

   public void setPosZ(float posz, boolean forceSave) {
      if (this.posZ != posz) {
         this.posZ = posz;
         if (forceSave) {
            this.changed = true;
         }
      }
   }

   public float getRotation() {
      return this.rotation;
   }

   public void setRotation(float rot) {
      if (this.rotation != rot) {
         this.rotation = rot;
         this.changed = true;
         PlayerTutorial.firePlayerTrigger(this.wurmid, PlayerTutorial.PlayerTrigger.MOVED_PLAYER_VIEW);
      }
   }

   public int getZoneId() {
      return this.zoneId;
   }

   public void setZoneId(int zoneid) {
      if (zoneid != this.zoneId) {
         this.zoneId = zoneid;
         this.changed = true;
      }
   }

   public int getLayer() {
      return this.layer;
   }

   public void setLayer(int layerId) {
      if (this.layer != layerId) {
         this.layer = layerId;
         this.changed = true;
      }
   }

   public long getBridgeId() {
      return this.bridgeId;
   }

   public void setBridgeId(long bridgeid) {
      if (this.bridgeId != bridgeid) {
         this.bridgeId = bridgeid;
         this.changed = true;
         this.save(false);
      }
   }

   public long getWurmid() {
      return this.wurmid;
   }

   public final void save(boolean create) {
      if (this.changed) {
         this.changed = false;
         PreparedStatement ps = null;
         Connection dbcon = null;

         try {
            if (this.isPlayer()) {
               dbcon = DbConnector.getPlayerDbCon();
            } else {
               dbcon = DbConnector.getCreatureDbCon();
            }

            if (create) {
               ps = dbcon.prepareStatement("insert into POSITION (POSX, POSY, POSZ, ROTATION,ZONEID,LAYER,ONBRIDGE, WURMID) values (?,?,?,?,?,?,?,?)");
            } else {
               ps = dbcon.prepareStatement("update POSITION set POSX=?, POSY=?, POSZ=?, ROTATION=?,ZONEID=?,LAYER=?,ONBRIDGE=? where WURMID=?");
            }

            ps.setFloat(1, this.getPosX());
            ps.setFloat(2, this.getPosY());
            ps.setFloat(3, this.getPosZ());
            ps.setFloat(4, this.getRotation());
            ps.setInt(5, this.getZoneId());
            ps.setInt(6, this.getLayer());
            ps.setLong(7, this.getBridgeId());
            ps.setLong(8, this.getWurmid());
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to update creaturePos for " + this.getWurmid() + " " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public static void clearBatches() {
      try {
         if (cretPosPS != null) {
            int[] x = cretPosPS.executeBatch();
            logger.log(Level.INFO, "Creatures Position saved batch size " + x.length);
            DbUtilities.closeDatabaseObjects(cretPosPS, null);
            cretPosPS = null;
            cretPosPSCount = 0;
         }

         if (playPosPS != null) {
            int[] x = playPosPS.executeBatch();
            logger.log(Level.INFO, "Players Position saved batch size " + x.length);
            DbUtilities.closeDatabaseObjects(playPosPS, null);
            playPosPS = null;
            playPosPSCount = 0;
         }
      } catch (Exception var1) {
         logger.log(Level.WARNING, var1.getMessage(), (Throwable)var1);
      }
   }

   protected final void savePlayerPosition(int zoneid, boolean immediately) throws SQLException {
      this.setZoneId(zoneid);
      if ((System.currentTimeMillis() - this.lastSavedPos > 60000L || immediately) && this.changed) {
         if (Constants.useScheduledExecutorToUpdatePlayerPositionInDatabase) {
            PlayerPositionDbUpdatable lUpdatable = new PlayerPositionDbUpdatable(
               this.getWurmid(), this.getPosX(), this.getPosY(), this.getPosZ(), this.getRotation(), this.getZoneId(), this.getLayer(), this.getBridgeId()
            );
            playerDbPosUpdater.addToQueue(lUpdatable);
            ++totalPlayPosPSCount;
            if (immediately) {
               playerDbPosUpdater.saveImmediately();
            }
         } else {
            if (playPosPS == null) {
               Connection dbcon = DbConnector.getPlayerDbCon();
               if (Server.getInstance().isPS()) {
                  playPosPS = dbcon.prepareStatement("update POSITION set POSX=?, POSY=?, POSZ=?, ROTATION=?,ZONEID=?,LAYER=?,ONBRIDGE=? where WURMID=?");
               } else {
                  playPosPS = dbcon.prepareStatement(
                     "INSERT INTO POSITION (POSX, POSY, POSZ, ROTATION, ZONEID, LAYER, ONBRIDGE, WURMID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE POSX=VALUES(POSX), POSY=VALUES(POSY), POSZ=VALUES(POSZ), ROTATION=VALUES(ROTATION), ZONEID=VALUES(ZONEID), LAYER=VALUES(LAYER), ONBRIDGE=VALUES(ONBRIDGE)"
                  );
               }
            }

            playPosPS.setFloat(1, this.getPosX());
            playPosPS.setFloat(2, this.getPosY());
            playPosPS.setFloat(3, this.getPosZ());
            this.setRotation(Creature.normalizeAngle(this.getRotation()));
            playPosPS.setFloat(4, this.getRotation());
            playPosPS.setInt(5, this.getZoneId());
            playPosPS.setInt(6, this.getLayer());
            playPosPS.setLong(7, this.getBridgeId());
            playPosPS.setLong(8, this.getWurmid());
            playPosPS.addBatch();
            ++playPosPSCount;
            ++totalPlayPosPSCount;
            if (playPosPSCount > Constants.numberOfDbPlayerPositionsToUpdateEachTime || immediately) {
               long checkms = System.nanoTime();
               playPosPS.executeBatch();
               DbUtilities.closeDatabaseObjects(playPosPS, null);
               playPosPS = null;
               float elapsedMilliseconds = (float)(System.nanoTime() - checkms) / 1000000.0F;
               if (elapsedMilliseconds > 300.0F || logger.isLoggable(Level.FINER)) {
                  logger.log(Level.WARNING, "SavePlayerPos batch took " + elapsedMilliseconds + " ms for " + playPosPSCount + " updates.");
               }

               playPosPSCount = 0;
            }
         }

         this.changed = false;
         this.lastSavedPos = System.currentTimeMillis();
      }
   }

   protected void saveCreaturePosition(int zoneid, boolean immediately) throws SQLException {
      this.setZoneId(zoneid);
      if ((System.currentTimeMillis() - this.lastSavedPos > 600000L || immediately) && this.changed) {
         if (Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase && !immediately) {
            CreaturePositionDbUpdatable lUpdatable = new CreaturePositionDbUpdatable(
               this.getWurmid(), this.getPosX(), this.getPosY(), this.getPosZ(), this.getRotation(), this.getZoneId(), this.getLayer(), this.getBridgeId()
            );
            creatureDbPosUpdater.addToQueue(lUpdatable);
            ++totalCretPosPSCount;
            this.lastSavedPos = System.currentTimeMillis();
         } else {
            if (cretPosPS == null) {
               Connection dbcon = DbConnector.getCreatureDbCon();
               if (Server.getInstance().isPS()) {
                  cretPosPS = dbcon.prepareStatement("update POSITION set POSX=?, POSY=?, POSZ=?, ROTATION=?,ZONEID=?,LAYER=?,ONBRIDGE=? where WURMID=?");
               } else {
                  cretPosPS = dbcon.prepareStatement(
                     "INSERT INTO POSITION (POSX, POSY, POSZ, ROTATION, ZONEID, LAYER, ONBRIDGE, WURMID) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE POSX=VALUES(POSX), POSY=VALUES(POSY), POSZ=VALUES(POSZ), ROTATION=VALUES(ROTATION), ZONEID=VALUES(ZONEID), LAYER=VALUES(LAYER), ONBRIDGE=VALUES(ONBRIDGE)"
                  );
               }
            }

            cretPosPS.setFloat(1, this.getPosX());
            cretPosPS.setFloat(2, this.getPosY());
            cretPosPS.setFloat(3, this.getPosZ());
            this.setRotation(Creature.normalizeAngle(this.getRotation()));
            cretPosPS.setFloat(4, this.getRotation());
            cretPosPS.setInt(5, this.getZoneId());
            cretPosPS.setInt(6, this.getLayer());
            cretPosPS.setLong(7, this.getBridgeId());
            cretPosPS.setLong(8, this.getWurmid());
            cretPosPS.addBatch();
            ++cretPosPSCount;
            ++totalCretPosPSCount;
            if (cretPosPSCount > Constants.numberOfDbCreaturePositionsToUpdateEachTime || immediately) {
               cretPosPS.executeBatch();
               DbUtilities.closeDatabaseObjects(cretPosPS, null);
               cretPosPS = null;
               cretPosPSCount = 0;
            }
         }

         this.changed = false;
      }
   }

   public boolean isPlayer() {
      return this.isPlayer;
   }

   public static final void loadAllPositions() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM POSITION");
         rs = ps.executeQuery();

         while(rs.next()) {
            new CreaturePos(
               rs.getLong("WURMID"),
               rs.getFloat("POSX"),
               rs.getFloat("POSY"),
               rs.getFloat("POSZ"),
               rs.getFloat("ROTATION"),
               rs.getInt("ZONEID"),
               rs.getInt("LAYER"),
               rs.getLong("ONBRIDGE"),
               false
            );
         }

         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM POSITION");
         rs = ps.executeQuery();

         while(rs.next()) {
            new CreaturePos(
               rs.getLong("WURMID"),
               rs.getFloat("POSX"),
               rs.getFloat("POSY"),
               rs.getFloat("POSZ"),
               rs.getFloat("ROTATION"),
               rs.getInt("ZONEID"),
               rs.getInt("LAYER"),
               rs.getLong("ONBRIDGE"),
               false
            );
         }
      } catch (Exception var7) {
         logger.log(Level.WARNING, "Failed to load all positions", (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final CreaturePos getPosition(long wurmId) {
      return allPositions.get(wurmId);
   }

   public static final void delete(long wurmId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         boolean player = false;
         CreaturePos pos = getPosition(wurmId);
         if (pos != null) {
            player = pos.isPlayer();
            allPositions.remove(wurmId);
         }

         if (player) {
            dbcon = DbConnector.getPlayerDbCon();
         } else {
            dbcon = DbConnector.getCreatureDbCon();
         }

         ps = dbcon.prepareStatement("DELETE FROM POSITION WHERE WURMID=?");
         ps.setLong(1, wurmId);
         ps.executeUpdate();
      } catch (Exception var9) {
         logger.log(Level.WARNING, "Failed to load all positions", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static CreaturePositionDatabaseUpdater getCreatureDbPosUpdater() {
      return creatureDbPosUpdater;
   }

   public static PlayerPositionDatabaseUpdater getPlayerDbPosUpdater() {
      return playerDbPosUpdater;
   }
}

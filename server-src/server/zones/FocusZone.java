package com.wurmonline.server.zones;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FocusZone extends Zone implements TimeConstants {
   private static final String loadAll = "SELECT * FROM FOCUSZONES";
   private static final String addZone = "INSERT INTO FOCUSZONES (STARTX,STARTY,ENDX,ENDY,TYPE,NAME,DESCRIPTION) VALUES (?,?,?,?,?,?,?)";
   private static final String deleteZone = "DELETE FROM FOCUSZONES WHERE STARTX=? AND STARTY=? AND ENDX=? AND ENDY=? AND TYPE=? AND NAME=?";
   private static final Set<FocusZone> focusZones = new HashSet<>();
   private static final Logger logger = Logger.getLogger(FocusZone.class.getName());
   private final byte type;
   public static final byte TYPE_NONE = 0;
   public static final byte TYPE_VOLCANO = 1;
   public static final byte TYPE_PVP = 2;
   public static final byte TYPE_NAME = 3;
   public static final byte TYPE_NAME_POPUP = 4;
   public static final byte TYPE_NON_PVP = 5;
   public static final byte TYPE_PVP_HOTA = 6;
   public static final byte TYPE_PVP_BATTLECAMP = 7;
   public static final byte TYPE_FLATTEN_DIRT = 8;
   public static final byte TYPE_HOUSE_WOOD = 9;
   public static final byte TYPE_HOUSE_STONE = 10;
   public static final byte TYPE_PREM_SPAWN = 11;
   public static final byte TYPE_NO_BUILD = 12;
   public static final byte TYPE_TALLWALLS = 13;
   public static final byte TYPE_FOG = 14;
   public static final byte TYPE_FLATTEN_ROCK = 15;
   public static final byte TYPE_REPLENISH_DIRT = 16;
   public static final byte TYPE_REPLENISH_TREES = 17;
   public static final byte TYPE_REPLENISH_ORES = 18;
   private int polls = 0;
   private Item projectile = null;
   private int pollSecondLanded = 0;
   private final String name;
   private final String description;

   public FocusZone(int aStartX, int aEndX, int aStartY, int aEndY, byte zoneType, String aName, String aDescription, boolean save) {
      super(aStartX, aEndX, aStartY, aEndY, true);
      this.name = aName;
      this.description = aDescription;
      this.type = zoneType;
      if (save) {
         try {
            this.save();
            focusZones.add(this);
         } catch (IOException var10) {
            logger.log(Level.INFO, var10.getMessage(), (Throwable)var10);
         }
      }
   }

   public final String getName() {
      return this.name;
   }

   public final String getDescription() {
      return this.description;
   }

   public final boolean isPvP() {
      return this.type == 2 || this.type == 6;
   }

   public final boolean isNonPvP() {
      return this.type == 5;
   }

   public final boolean isNamePopup() {
      return this.type == 4;
   }

   public final boolean isName() {
      return this.type == 3 || this.type == 7;
   }

   public final boolean isBattleCamp() {
      return this.type == 7;
   }

   public final boolean isPvPHota() {
      return this.type == 6;
   }

   public final boolean isPremSpawnOnly() {
      return this.type == 11;
   }

   public final boolean isNoBuild() {
      return this.type == 12;
   }

   public final boolean isFog() {
      return this.type == 14;
   }

   public final boolean isType(byte wantedType) {
      return this.type == wantedType;
   }

   @Override
   void load() throws IOException {
   }

   public static void pollAll() {
      for(FocusZone fz : focusZones) {
         fz.poll();
      }
   }

   public static final Set<FocusZone> getZonesAt(int tilex, int tiley) {
      if (focusZones.size() > 0) {
         Set<FocusZone> toReturn = new HashSet<>();

         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley)) {
               toReturn.add(fz);
            }
         }

         return toReturn;
      } else {
         return focusZones;
      }
   }

   public static final boolean isPvPZoneAt(int tilex, int tiley) {
      if (focusZones.size() > 0) {
         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley) && fz.isPvP()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final boolean isNonPvPZoneAt(int tilex, int tiley) {
      if (focusZones.size() > 0) {
         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley) && fz.isNonPvP()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final boolean isPremSpawnOnlyZoneAt(int tilex, int tiley) {
      if (focusZones.size() > 0) {
         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley) && fz.isPremSpawnOnly()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final boolean isNoBuildZoneAt(int tilex, int tiley) {
      if (focusZones.size() > 0) {
         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley) && fz.isNoBuild()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final boolean isFogZoneAt(int tilex, int tiley) {
      if (focusZones.size() > 0) {
         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley) && fz.isFog()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final boolean isZoneAt(int tilex, int tiley, byte wantedType) {
      if (focusZones.size() > 0) {
         for(FocusZone fz : focusZones) {
            if (fz.covers(tilex, tiley) && fz.isType(wantedType)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final FocusZone[] getAllZones() {
      return focusZones.toArray(new FocusZone[focusZones.size()]);
   }

   public static final FocusZone getHotaZone() {
      for(FocusZone fz : getAllZones()) {
         if (fz.isPvPHota()) {
            return fz;
         }
      }

      return null;
   }

   public static void loadAll() {
      long now = System.nanoTime();
      int numberOfZonesLoaded = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM FOCUSZONES");

         for(rs = ps.executeQuery(); rs.next(); ++numberOfZonesLoaded) {
            FocusZone fz = new FocusZone(
               rs.getInt("STARTX"),
               rs.getInt("ENDX"),
               rs.getInt("STARTY"),
               rs.getInt("ENDY"),
               rs.getByte("TYPE"),
               rs.getString("NAME"),
               rs.getString("DESCRIPTION"),
               false
            );
            focusZones.add(fz);
         }
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Problem loading focus zone, count is " + numberOfZonesLoaded + " due to " + var11.getMessage(), (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
         logger.log(Level.INFO, "Loaded " + numberOfZonesLoaded + " focus zones. It took " + lElapsedTime + " millis.");
      }
   }

   @Override
   void loadFences() throws IOException {
   }

   public void delete() throws IOException {
      if (getHotaZone() == this) {
         Hota.destroyHota();
      }

      focusZones.remove(this);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM FOCUSZONES WHERE STARTX=? AND STARTY=? AND ENDX=? AND ENDY=? AND TYPE=? AND NAME=?");
         ps.setInt(1, this.startX);
         ps.setInt(2, this.startY);
         ps.setInt(3, this.endX);
         ps.setInt(4, this.endY);
         ps.setByte(5, this.type);
         ps.setString(6, this.name);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   void save() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("INSERT INTO FOCUSZONES (STARTX,STARTY,ENDX,ENDY,TYPE,NAME,DESCRIPTION) VALUES (?,?,?,?,?,?,?)");
         ps.setInt(1, this.startX);
         ps.setInt(2, this.startY);
         ps.setInt(3, this.endX);
         ps.setInt(4, this.endY);
         ps.setByte(5, this.type);
         ps.setString(6, this.name);
         ps.setString(7, this.description);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void poll() {
      ++this.polls;
      if (this.type == 1 && this.polls % 5 == 0) {
         boolean foundLava = false;

         for(int x = this.startX; x < this.endX; ++x) {
            for(int y = this.startY; y < this.endY; ++y) {
               if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) == Tiles.Tile.TILE_CAVE_WALL_LAVA.id) {
                  for(int xx = -1; xx <= 1; ++xx) {
                     for(int yy = -1; yy <= 1; ++yy) {
                        if ((xx != 0 || yy != 0) && (xx == 0 || yy == 0) && !Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x + xx, y + yy)))) {
                           logger.log(Level.INFO, "Lava flow at " + (x + xx) + "," + (y + yy));
                           Terraforming.setAsRock(x + xx, y + yy, true, true);
                           foundLava = true;
                           break;
                        }
                     }
                  }
               }

               if (foundLava) {
                  break;
               }
            }
         }

         if (this.pollSecondLanded > 0 && this.polls >= this.pollSecondLanded) {
            if (this.projectile != null) {
               try {
                  Zone z = Zones.getZone(this.projectile.getTileX(), this.projectile.getTileY(), true);
                  z.addItem(this.projectile);
                  logger.log(Level.INFO, "Added projectile to " + this.projectile.getTileX() + "," + this.projectile.getTileY());
               } catch (NoSuchZoneException var19) {
                  logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
               }

               this.projectile = null;
            }

            this.pollSecondLanded = 0;
            this.polls = 0;
         } else if ((long)this.polls == 42600L) {
            Server.getInstance().broadCastNormal(this.name + " rumbles.");
         } else if ((long)this.polls == 43200L) {
            Server.getInstance().broadCastNormal(this.name + " rumbles intensely.");
         } else if ((long)this.polls >= 43200L && Server.rand.nextInt(3600) == 0) {
            try {
               this.projectile = ItemFactory.createItem(692, 80.0F + Server.rand.nextFloat() * 20.0F, null);
               int centerX = this.getStartX() + this.getSize() / 2;
               int centerY = this.getStartY() + this.getSize() / 2;
               int randX = Zones.safeTileX(centerX - 10 + Server.rand.nextInt(21));
               int randY = Zones.safeTileY(centerY - 10 + Server.rand.nextInt(21));
               int landX = Zones.safeTileX(randX - 100 + Server.rand.nextInt(200));
               int landY = Zones.safeTileY(randY - 100 + Server.rand.nextInt(200));
               int secondsInAir = Math.max(5, Math.max(Math.abs(randX - landX), Math.abs(randY - landY)) / 10);
               this.pollSecondLanded = this.polls + secondsInAir;
               float sx = (float)(randX * 4 + 2);
               float sy = (float)(randY * 4 + 2);
               float ex = (float)(landX * 4 + 2);
               float ey = (float)(landY * 4 + 2);
               float rot = Server.rand.nextFloat() * 360.0F;
               logger.log(Level.INFO, "Creating projectile from " + randX + "," + randY + " to " + landX + "," + landY);

               try {
                  float sh = Zones.calculateHeight(sx, sy, true) - 10.0F;
                  float eh = Zones.calculateHeight(ex, ey, true);
                  this.projectile.setPosXYZRotation(ex, ey, eh, rot);
                  Player[] players = Players.getInstance().getPlayers();

                  for(int x = 0; x < players.length; ++x) {
                     if (players[x].isWithinDistanceTo(sx, sy, sh, 500.0F) || players[x].isWithinDistanceTo(ex, ey, eh, 500.0F)) {
                        players[x]
                           .getCommunicator()
                           .sendProjectile(
                              this.projectile.getWurmId(),
                              (byte)3,
                              this.projectile.getModelName(),
                              this.projectile.getName(),
                              this.projectile.getMaterial(),
                              sx,
                              sy,
                              sh,
                              rot,
                              (byte)0,
                              (float)landX,
                              (float)landY,
                              eh,
                              -10L,
                              -10L,
                              (float)secondsInAir,
                              (float)secondsInAir
                           );
                     }
                  }
               } catch (NoSuchZoneException var20) {
                  logger.log(Level.WARNING, var20.getMessage(), (Throwable)var20);
                  this.projectile = null;
                  this.pollSecondLanded = 0;
                  this.polls = 0;
               }
            } catch (FailedException var21) {
               logger.log(Level.WARNING, var21.getMessage(), (Throwable)var21);
               this.projectile = null;
               this.pollSecondLanded = 0;
               this.polls = 0;
            } catch (NoSuchTemplateException var22) {
               logger.log(Level.WARNING, var22.getMessage(), (Throwable)var22);
               this.projectile = null;
               this.pollSecondLanded = 0;
               this.polls = 0;
            }
         }
      } else if (this.type == 16) {
         if ((long)this.polls % 900L == 0L) {
            float avgHeight = (
                  Zones.getHeightForNode(this.getStartX(), this.getStartY(), 1)
                     + Zones.getHeightForNode(this.getStartX(), this.getEndY() + 1, 1)
                     + Zones.getHeightForNode(this.getEndX() + 1, this.getStartY(), 1)
                     + Zones.getHeightForNode(this.getEndX() + 1, this.getEndY() + 1, 1)
               )
               / 4.0F;

            for(int tileX = this.getStartX() + 1; tileX < this.getEndX(); ++tileX) {
               for(int tileY = this.getStartY() + 1; tileY < this.getEndY(); ++tileY) {
                  int tile = Server.surfaceMesh.getTile(tileX, tileY);
                  byte type = Tiles.decodeType(tile);
                  if (type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_DIRT_PACKED.id || type == Tiles.Tile.TILE_SAND.id || Tiles.isGrassType(type)) {
                     short actualHeight = Tiles.decodeHeight(tile);
                     if ((float)actualHeight > avgHeight * 10.0F + 5.0F) {
                        Server.surfaceMesh.setTile(tileX, tileY, Tiles.encode((short)(actualHeight - 1), type, Tiles.decodeData(tile)));
                     } else {
                        if (!((float)actualHeight < avgHeight * 10.0F - 5.0F)) {
                           continue;
                        }

                        Server.surfaceMesh.setTile(tileX, tileY, Tiles.encode((short)(actualHeight + 1), type, Tiles.decodeData(tile)));
                     }

                     Players.getInstance().sendChangedTile(tileX, tileY, true, true);

                     try {
                        Zone toCheckForChange = Zones.getZone(tileX, tileY, true);
                        toCheckForChange.changeTile(tileX, tileY);
                     } catch (NoSuchZoneException var18) {
                        logger.log(Level.INFO, "no such zone?: " + tileX + ", " + tileY, (Throwable)var18);
                     }
                  }
               }
            }
         }
      } else if (this.type == 17) {
         if ((long)this.polls % 300L == 0L) {
            for(int tileX = this.getStartX() + 1; tileX < this.getEndX(); ++tileX) {
               for(int tileY = this.getStartY() + 1; tileY < this.getEndY(); ++tileY) {
                  int tile = Server.surfaceMesh.getTile(tileX, tileY);
                  byte type = Tiles.decodeType(tile);
                  if (Tiles.isTree(type)) {
                     byte age = FoliageAge.getAgeAsByte(Tiles.decodeData(tile));
                     if (age <= FoliageAge.MATURE_THREE.getAgeId()) {
                        int newData = Tiles.encodeTreeData((byte)(age + 1), false, false, GrassData.GrowthTreeStage.decodeTileData(Tiles.decodeData(tile)));
                        Server.surfaceMesh.setTile(tileX, tileY, Tiles.encode(Tiles.decodeHeight(tile), Tiles.decodeType(tile), (byte)newData));
                        Players.getInstance().sendChangedTile(tileX, tileY, true, false);
                     }
                  } else {
                     boolean skip = false;

                     for(int x = tileX - 1; x < tileX + 1; ++x) {
                        for(int y = tileY - 1; y < tileY + 1; ++y) {
                           if (Tiles.isTree(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))) && (tileX == 0 || tileY == 0)) {
                              skip = true;
                           }
                        }
                     }

                     if (!skip) {
                        TreeData.TreeType treeType = TreeData.TreeType.BIRCH;
                        switch(Server.rand.nextInt(5)) {
                           case 0:
                              treeType = TreeData.TreeType.LINDEN;
                              break;
                           case 1:
                              treeType = TreeData.TreeType.PINE;
                              break;
                           case 2:
                              treeType = TreeData.TreeType.WALNUT;
                              break;
                           case 3:
                              treeType = TreeData.TreeType.CEDAR;
                        }

                        int newData = Tiles.encodeTreeData(FoliageAge.YOUNG_ONE, false, false, GrassData.GrowthTreeStage.SHORT);
                        Server.setSurfaceTile(tileX, tileY, Tiles.decodeHeight(tile), treeType.asNormalTree(), (byte)newData);
                        Server.setWorldResource(tileX, tileY, 0);
                        Players.getInstance().sendChangedTile(tileX, tileY, true, true);
                     }
                  }
               }
            }
         }
      } else if (this.type == 18 && (long)this.polls % 900L == 0L) {
         for(int tileX = this.getStartX() + 1; tileX < this.getEndX(); ++tileX) {
            for(int tileY = this.getStartY() + 1; tileY < this.getEndY(); ++tileY) {
               int tile = Server.caveMesh.getTile(tileX, tileY);
               byte type = Tiles.decodeType(tile);
               if (Tiles.isOreCave(type)) {
                  int resource = Server.getCaveResource(tileX, tileY);
                  if (resource < 1000) {
                     resource = Server.rand.nextInt(10000) + 10000;
                     Server.setCaveResource(tileX, tileY, resource);
                  }
               }
            }
         }
      }
   }
}

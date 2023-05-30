package com.wurmonline.server.endgames;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.Enchants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class EndGameItems implements MiscConstants, ItemTypes, Enchants, TimeConstants {
   public static final Map<Long, EndGameItem> altars = new HashMap<>();
   private static final Map<Long, EndGameItem> artifacts = new HashMap<>();
   private static final Logger logger = Logger.getLogger(EndGameItems.class.getName());
   private static final String LOAD_ENDGAMEITEMS = "SELECT * FROM ENDGAMEITEMS";
   private static float posx = 0.0F;
   private static float posy = 0.0F;
   private static int tileX = 0;
   private static int tileY = 0;
   private static final LinkedList<Kingdom> missingCrowns = new LinkedList<>();
   public static final byte chargeDecay = 10;
   private static long lastRechargedItem = 0L;

   private EndGameItems() {
   }

   public static final void createAltars() {
      logger.log(Level.INFO, "Creating altars.");
      boolean found = false;
      int startX = (Zones.worldTileSizeX - 10) / 2;
      int startY = Math.min(Zones.worldTileSizeY / 20, 300);
      int tries = 0;

      while(!found && tries < 1000) {
         ++tries;
         float posz = findPlacementTile(startX, startY);
         if (posz <= 0.0F) {
            startX += Math.min(Zones.worldTileSizeX / 20, 300);
            if (startX >= Zones.worldTileSizeX - Math.min(Zones.worldTileSizeX / 20, 100)) {
               startX = (Zones.worldTileSizeX - 10) / 2;
               startY += Math.min(Zones.worldTileSizeY / 20, 100);
            }

            if (startY >= Zones.worldTileSizeY - Math.min(Zones.worldTileSizeY / 20, 100)) {
               break;
            }
         } else {
            found = true;
         }
      }

      if (!found) {
         logger.log(Level.WARNING, "Failed to locate a good spot to create holy altar. Exiting.");
      } else {
         posx = (float)(tileX << 2);
         posy = (float)(tileY << 2);

         try {
            Item holy = ItemFactory.createItem(327, 90.0F, posx, posy, 180.0F, true, (byte)0, -10L, null);
            holy.bless(1);
            holy.enchant((byte)5);
            EndGameItem eg = new EndGameItem(holy, true, (short)68, true);
            altars.put(new Long(eg.getWurmid()), eg);
            logger.log(Level.INFO, "Created holy altar at " + posx + ", " + posy + ".");
         } catch (NoSuchTemplateException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         } catch (FailedException var9) {
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }

         tileX = 0;
         tileY = 0;
         found = false;
         startX = (Zones.worldTileSizeX - 10) / 2;
         startY = Math.max(Zones.worldTileSizeY - 300, Zones.worldTileSizeY - Zones.worldTileSizeY / 20);
         tries = 0;

         while(!found && tries < 1000) {
            ++tries;
            float posz = findPlacementTile(startX, startY);
            if (posz <= 0.0F) {
               startX += Math.min(Zones.worldTileSizeX / 20, 300);
               if (startX >= Zones.worldTileSizeX - Math.min(Zones.worldTileSizeX / 20, 100)) {
                  startX = (Zones.worldTileSizeX - 10) / 2;
                  startY -= Math.min(Zones.worldTileSizeY / 20, 100);
               }

               if (startY <= 0) {
                  break;
               }
            } else {
               found = true;
            }
         }

         if (!found) {
            logger.log(Level.WARNING, "Failed to locate a good spot to create unholy altar. Exiting.");
         } else {
            posx = (float)(tileX << 2);
            posy = (float)(tileY << 2);

            try {
               Item unholy = ItemFactory.createItem(328, 90.0F, posx, posy, 180.0F, true, (byte)0, -10L, null);
               unholy.bless(4);
               unholy.enchant((byte)8);
               EndGameItem eg = new EndGameItem(unholy, false, (short)68, true);
               altars.put(new Long(eg.getWurmid()), eg);
               logger.log(Level.INFO, "Created unholy altar at " + posx + ", " + posy + ".");
            } catch (NoSuchTemplateException var6) {
               logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            } catch (FailedException var7) {
               logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            }
         }
      }
   }

   @Nullable
   public static final EndGameItem getEvilAltar() {
      if (altars != null) {
         for(EndGameItem eg : altars.values()) {
            if (eg.getItem().getTemplateId() == 328) {
               return eg;
            }
         }
      }

      return null;
   }

   @Nullable
   public static final EndGameItem getGoodAltar() {
      if (altars != null) {
         for(EndGameItem eg : altars.values()) {
            if (eg.getItem().getTemplateId() == 327) {
               return eg;
            }
         }
      }

      return null;
   }

   public static final float findPlacementTile(int tx, int ty) {
      float maxZ = 0.0F;
      if (Zones.isWithinDuelRing(tx, ty, tx + 20, ty + 20)) {
         return maxZ;
      } else {
         for(int x = 0; x < 20; ++x) {
            for(int y = 0; y < 20; ++y) {
               int tile = Server.surfaceMesh.getTile(tx + x, ty + y);
               float z = (float)Tiles.decodeHeight(tile);
               byte ttype = Tiles.decodeType(tile);
               if (ttype != Tiles.Tile.TILE_ROCK.id
                  && ttype != Tiles.Tile.TILE_CLIFF.id
                  && ttype != Tiles.Tile.TILE_HOLE.id
                  && z > 0.0F
                  && z > maxZ
                  && z < 700.0F) {
                  tileX = tx + x;
                  tileY = ty + y;
                  maxZ = z;
               }
            }
         }

         return maxZ;
      }
   }

   public static final void createArtifacts() {
      try {
         Item rod = ItemFactory.createItem(329, 90.0F, (byte)3, null);
         rod.bless(1);
         rod.enchant((byte)5);
         placeArtifact(rod);
         EndGameItem eg = new EndGameItem(rod, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item crownmight = ItemFactory.createItem(330, 90.0F, (byte)3, null);
         crownmight.bless(2);
         crownmight.enchant((byte)6);
         placeArtifact(crownmight);
         eg = new EndGameItem(crownmight, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item charmOfFo = ItemFactory.createItem(331, 90.0F, (byte)3, null);
         charmOfFo.bless(1);
         charmOfFo.enchant((byte)5);
         placeArtifact(charmOfFo);
         eg = new EndGameItem(charmOfFo, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item vynorasEye = ItemFactory.createItem(332, 90.0F, (byte)3, null);
         vynorasEye.bless(3);
         vynorasEye.enchant((byte)7);
         placeArtifact(vynorasEye);
         eg = new EndGameItem(vynorasEye, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item vynorasEar = ItemFactory.createItem(333, 90.0F, (byte)3, null);
         vynorasEar.bless(3);
         vynorasEar.enchant((byte)7);
         placeArtifact(vynorasEar);
         eg = new EndGameItem(vynorasEar, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item vynorasMouth = ItemFactory.createItem(334, 90.0F, (byte)3, null);
         vynorasMouth.bless(3);
         vynorasEar.enchant((byte)7);
         placeArtifact(vynorasMouth);
         eg = new EndGameItem(vynorasMouth, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item fingerOfFo = ItemFactory.createItem(335, 90.0F, (byte)3, null);
         fingerOfFo.bless(1);
         fingerOfFo.enchant((byte)5);
         placeArtifact(fingerOfFo);
         eg = new EndGameItem(fingerOfFo, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item swordOfMagranon = ItemFactory.createItem(336, 90.0F, (byte)3, null);
         swordOfMagranon.bless(2);
         swordOfMagranon.enchant((byte)4);
         placeArtifact(swordOfMagranon);
         eg = new EndGameItem(swordOfMagranon, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item hammerOfMagranon = ItemFactory.createItem(337, 90.0F, (byte)3, null);
         hammerOfMagranon.bless(2);
         hammerOfMagranon.enchant((byte)4);
         placeArtifact(hammerOfMagranon);
         eg = new EndGameItem(hammerOfMagranon, true, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item libilasScale = ItemFactory.createItem(338, 90.0F, (byte)3, null);
         libilasScale.bless(4);
         libilasScale.enchant((byte)8);
         placeArtifact(libilasScale);
         eg = new EndGameItem(libilasScale, false, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item orbOfDoom = ItemFactory.createItem(339, 90.0F, (byte)3, null);
         orbOfDoom.bless(4);
         orbOfDoom.enchant((byte)8);
         placeArtifact(orbOfDoom);
         eg = new EndGameItem(orbOfDoom, false, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
         Item sceptreOfAscension = ItemFactory.createItem(340, 90.0F, (byte)3, null);
         sceptreOfAscension.bless(4);
         sceptreOfAscension.enchant((byte)1);
         placeArtifact(sceptreOfAscension);
         eg = new EndGameItem(sceptreOfAscension, false, (short)69, true);
         artifacts.put(new Long(eg.getWurmid()), eg);
      } catch (NoSuchTemplateException var13) {
         logger.log(Level.WARNING, "Failed to create item: " + var13.getMessage(), (Throwable)var13);
      } catch (FailedException var14) {
         logger.log(Level.WARNING, "Failed to create item: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public static final void placeArtifact(Item artifact) {
      boolean found = false;

      while(!found) {
         int x = Server.rand.nextInt(Zones.worldTileSizeX);
         int y = Server.rand.nextInt(Zones.worldTileSizeX);
         int tile = Server.surfaceMesh.getTile(x, y);
         int rocktile = Server.rockMesh.getTile(x, y);
         float th = Tiles.decodeHeightAsFloat(tile);
         float rh = Tiles.decodeHeightAsFloat(rocktile);
         FocusZone hoderZone = FocusZone.getHotaZone();

         assert hoderZone != null;

         float seth = 0.0F;
         if (th > 4.0F && rh > 4.0F) {
            if (th - rh >= 1.0F) {
               seth = (float)Math.max(1, Server.rand.nextInt((int)(th * 10.0F - 5.0F - rh * 10.0F)));
            }

            if (seth > 0.0F) {
               VolaTile t = Zones.getTileOrNull(x, y, true);
               if (t == null || t.getStructure() == null && t.getVillage() == null && t.getZone() != hoderZone) {
                  seth /= 10.0F;
                  found = true;
                  artifact.setPosXYZ((float)((x << 2) + 2), (float)((y << 2) + 2), rh + seth);
                  artifact.setAuxData((byte)30);
                  logger.log(
                     Level.INFO,
                     "Placed " + artifact.getName() + " at " + x + "," + y + " at height " + (rh + seth) + " rockheight=" + rh + " tileheight=" + th
                  );
               }
            }
         }
      }
   }

   public static final Item[] getArtifactDugUp(int x, int y, float height, boolean allCornersRock) {
      Set<Item> found = new HashSet<>();

      for(EndGameItem artifact : artifacts.values()) {
         if ((long)artifact.getItem().getZoneId() == -10L
            && artifact.getItem().getOwnerId() == -10L
            && (int)artifact.getItem().getPosX() >> 2 == x
            && (int)artifact.getItem().getPosY() >> 2 == y
            && (height <= artifact.getItem().getPosZ() || allCornersRock)) {
            found.add(artifact.getItem());
            artifact.setLastMoved(System.currentTimeMillis());
         }
      }

      return found.toArray(new Item[found.size()]);
   }

   public static final EndGameItem getArtifactAtTile(int x, int y) {
      for(EndGameItem artifact : artifacts.values()) {
         if ((long)artifact.getItem().getZoneId() == -10L
            && artifact.getItem().getOwnerId() == -10L
            && (int)artifact.getItem().getPosX() >> 2 == x
            && (int)artifact.getItem().getPosY() >> 2 == y) {
            return artifact;
         }
      }

      return null;
   }

   public static final void deleteEndGameItem(EndGameItem eg) {
      if (eg != null) {
         if (eg.getItem().isHugeAltar()) {
            altars.remove(new Long(eg.getWurmid()));
         } else if (eg.getItem().isArtifact()) {
            artifacts.remove(new Long(eg.getWurmid()));
         }

         eg.delete();
      }
   }

   public static final void loadEndGameItems() {
      logger.info("Loading End Game Items.");
      long now = System.nanoTime();
      if (Servers.localServer.id == 3
         || Servers.localServer.id == 12
         || Servers.localServer.isChallengeServer()
         || Server.getInstance().isPS() && Constants.loadEndGameItems) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM ENDGAMEITEMS");
            rs = ps.executeQuery();
            long iid = -10L;
            boolean holy = true;
            short type = 0;
            boolean found = false;
            boolean foundAltar = false;
            long lastMoved = 0L;

            while(rs.next()) {
               iid = rs.getLong("WURMID");
               holy = rs.getBoolean("HOLY");
               type = rs.getShort("TYPE");
               lastMoved = rs.getLong("LASTMOVED");

               try {
                  Item item = Items.getItem(iid);
                  EndGameItem eg = new EndGameItem(item, holy, type, false);
                  eg.lastMoved = lastMoved;
                  if (type == 68) {
                     eg.setLastMoved(System.currentTimeMillis());
                     foundAltar = true;
                     altars.put(new Long(iid), eg);
                  } else if (type == 69) {
                     found = true;
                     artifacts.put(new Long(iid), eg);
                     if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Loaded Artifact, ID: " + iid + ", " + eg);
                     }
                  } else {
                     logger.warning("End Game Items should only be Huge Altars or Artifiacts not type " + type + ", ID: " + iid + ", " + eg);
                  }
               } catch (NoSuchItemException var19) {
                  if (Server.getInstance().isPS()) {
                     logger.log(Level.INFO, "Endgame item missing: " + iid + ". Deleting entry.");
                     EndGameItem.delete(iid);
                     if (type == 68) {
                        logger.log(
                           Level.INFO,
                           (holy ? "White Light" : "Black Light")
                              + " altar is missing. Destroy the "
                              + (!holy ? "White Light" : "Black Light")
                              + " altar to respawn both."
                        );
                     }
                  } else {
                     logger.log(Level.WARNING, "Endgame item missing: " + iid, (Throwable)var19);
                  }
               }
            }

            DbUtilities.closeDatabaseObjects(ps, rs);
            if (!found) {
               createArtifacts();
            } else {
               setArtifactsInWorld();
            }

            if (!foundAltar) {
               createAltars();
            }
         } catch (SQLException var20) {
            logger.log(Level.WARNING, "Failed to load item datas: " + var20.getMessage(), (Throwable)var20);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      }

      int numberOfAltars = altars != null ? altars.size() : 0;
      int numberOfArtifacts = artifacts != null ? artifacts.size() : 0;
      logger.log(
         Level.INFO,
         "Loaded " + numberOfAltars + " altars and " + numberOfArtifacts + " artifacts. That took " + (float)(System.nanoTime() - now) / 1000000.0F + " ms."
      );
   }

   public static EndGameItem getEndGameItem(Item item) {
      if (item.isHugeAltar()) {
         return altars.get(new Long(item.getWurmId()));
      } else {
         return item.isArtifact() ? artifacts.get(new Long(item.getWurmId())) : null;
      }
   }

   public static final boolean mayRechargeItem() {
      return System.currentTimeMillis() - lastRechargedItem > 60000L;
   }

   public static final void touchRecharge() {
      lastRechargedItem = System.currentTimeMillis();
   }

   public static final void destroyHugeAltar(Item altar, Creature destroyer) {
      EndGameItem eg = altars.get(new Long(altar.getWurmId()));
      if (eg != null) {
         Server.getInstance().broadCastAlert("The " + altar.getName() + " has fallen to the hands of " + destroyer.getName() + "!", true, (byte)4);
         HistoryManager.addHistory(destroyer.getName(), "Destroyed the " + altar.getName() + ".");
         if (destroyer.isPlayer()) {
            float sx = altar.getPosX() - 100.0F;
            float ex = altar.getPosX() + 100.0F;
            float sy = altar.getPosY() - 100.0F;
            float ey = altar.getPosY() + 100.0F;

            for(Player p : Players.getInstance().getPlayers()) {
               if (p.getPosX() > sx && p.getPosX() < ex && p.getPosY() > sy && p.getPosY() < ey && p.getKingdomId() == destroyer.getKingdomId()) {
                  p.addTitle(Titles.Title.Altar_Destroyer);
                  if (eg.isHoly()) {
                     p.achievement(356);
                  } else {
                     p.achievement(357);
                  }
               }
            }
         }

         Player[] players = Players.getInstance().getPlayers();

         for(Player lPlayer : players) {
            if (eg.isHoly()) {
               if (lPlayer.getDeity() != null && lPlayer.getDeity().isHateGod()) {
                  lPlayer.setFarwalkerSeconds((byte)100);
                  lPlayer.healRandomWound(100);
               } else if (lPlayer.getDeity() != null && !lPlayer.getDeity().isHateGod()) {
                  lPlayer.getCommunicator().sendCombatAlertMessage("Your life force is drained, as it is used to heal the " + altar.getName() + "!");
                  lPlayer.addWoundOfType(null, (byte)9, 1, false, 1.0F, false, 5000.0, 0.0F, 0.0F, false, false);
               }
            } else if (lPlayer.getDeity() != null && !lPlayer.getDeity().isHateGod()) {
               lPlayer.setFarwalkerSeconds((byte)100);
               lPlayer.healRandomWound(100);
            } else if (lPlayer.getDeity() != null && lPlayer.getDeity().isHateGod()) {
               lPlayer.getCommunicator().sendCombatAlertMessage("Your life force is drained, as it is used to heal the " + altar.getName() + "!");
               lPlayer.addWoundOfType(null, (byte)9, 1, false, 1.0F, false, 5000.0, 0.0F, 0.0F, false, false);
            }
         }

         healAndTeleportAltar(eg);
         hideRandomArtifact(eg.isHoly());
      }
   }

   private static final void healAndTeleportAltar(EndGameItem altar) {
      Item altarItem = altar.getItem();
      altarItem.putInVoid();
      altarItem.setDamage(0.0F);
      Player[] p = Players.getInstance().getPlayers();

      for(Player lPlayer : p) {
         lPlayer.getCommunicator().sendRemoveEffect(altar.getWurmid());
      }

      boolean found = false;
      int randX = Zones.worldTileSizeX - 200;
      int randY = Zones.worldTileSizeY / 2;
      int startX = Zones.safeTileX(100 + Server.rand.nextInt(randX));
      int startY = Zones.safeTileY(100 + Server.rand.nextInt(randY));
      if (!altar.isHoly()) {
         startY = Zones.safeTileY(Zones.worldTileSizeY / 2 + startY);
      }

      int tries = 0;
      float posz = 0.0F;

      while(!found && tries < 1000) {
         ++tries;
         posz = findPlacementTile(startX, startY);
         if (Villages.getVillageWithPerimeterAt(tileX, tileY, true) != null) {
            posz = -1.0F;
         }

         if (posz <= 0.0F) {
            startX = Zones.safeTileX(100 + Server.rand.nextInt(randX));
            startY = Zones.safeTileY(100 + Server.rand.nextInt(randY));
            if (!altar.isHoly()) {
               startY = Zones.safeTileY(Zones.worldTileSizeY / 2 + startY);
            }
         } else {
            found = true;
         }
      }

      if (!found) {
         logger.log(Level.WARNING, "Failed to locate a good spot to create holy altar. Exiting.");
      } else {
         posx = (float)((tileX << 2) + 2);
         posy = (float)((tileY << 2) + 2);
         altarItem.setPosXYZ(posx, posy, posz);

         try {
            Zone z = Zones.getZone(tileX, tileY, true);
            z.addItem(altarItem);
            if (altar.isHoly()) {
               for(Player lPlayer : p) {
                  lPlayer.getCommunicator()
                     .sendAddEffect(altar.getWurmid(), (short)2, altar.getItem().getPosX(), altar.getItem().getPosY(), altar.getItem().getPosZ(), (byte)0);
               }
            } else {
               for(Player lPlayer : p) {
                  lPlayer.getCommunicator()
                     .sendAddEffect(altar.getWurmid(), (short)3, altar.getItem().getPosX(), altar.getItem().getPosY(), altar.getItem().getPosZ(), (byte)0);
               }
            }
         } catch (NoSuchZoneException var15) {
            logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
         }
      }
   }

   private static final void hideRandomArtifact(boolean holy) {
      EndGameItem[] arts = artifacts.values().toArray(new EndGameItem[artifacts.size()]);
      Item artifactToPlace = null;
      List<Item> candidates = new ArrayList<>();

      for(EndGameItem lArt : arts) {
         Item artifact = lArt.getItem();
         if (lArt.isInWorld() && lArt.isHoly() == holy) {
            candidates.add(artifact);
         }
      }

      if (candidates.size() > 0) {
         artifactToPlace = candidates.get(Server.rand.nextInt(candidates.size()));

         try {
            Item parent = artifactToPlace.getParent();
            parent.dropItem(artifactToPlace.getWurmId(), false);
            placeArtifact(artifactToPlace);
         } catch (NoSuchItemException var9) {
         }
      }
   }

   public static final void setArtifactsInWorld() {
      EndGameItem[] arts = artifacts.values().toArray(new EndGameItem[artifacts.size()]);

      for(EndGameItem lArt : arts) {
         Item artifact = lArt.getItem();
         if (artifact.getOwnerId() != -10L) {
            CreaturePos stat = CreaturePos.getPosition(artifact.getOwnerId());
            if (stat != null && stat.getPosX() > 0.0F) {
               try {
                  Item parent = artifact.getParent();
                  parent.dropItem(artifact.getWurmId(), false);
                  Zone z = Zones.getZone((int)stat.getPosX() >> 2, (int)stat.getPosY() >> 2, stat.getLayer() >= 0);
                  artifact.setPosXY(stat.getPosX(), stat.getPosY());
                  z.addItem(artifact);
                  logger.log(
                     Level.INFO, "Zone " + z.getId() + " added " + artifact.getName() + " at " + ((int)stat.getPosX() >> 2) + "," + ((int)stat.getPosY() >> 2)
                  );
               } catch (NoSuchItemException var9) {
                  logger.log(Level.WARNING, artifact.getName() + ": " + var9.getMessage(), (Throwable)var9);
               } catch (NoSuchZoneException var10) {
                  logger.log(Level.WARNING, artifact.getName() + ": " + var10.getMessage(), (Throwable)var10);
               }
            }
         }
      }
   }

   public static final void pollAll() {
      EndGameItem[] arts = artifacts.values().toArray(new EndGameItem[artifacts.size()]);

      for(EndGameItem lArt : arts) {
         if (lArt.isInWorld() && System.currentTimeMillis() - lArt.getLastMoved() > (Servers.isThisATestServer() ? 60000L : 604800000L)) {
            lArt.setLastMoved(System.currentTimeMillis());
            Item artifact = lArt.getItem();
            if (artifact.getAuxData() <= 0) {
               moveArtifact(artifact);
            } else {
               artifact.setAuxData((byte)Math.max(0, artifact.getAuxData() - 10));

               try {
                  if (artifact.getOwner() != -10L) {
                     Creature owner = Server.getInstance().getCreature(artifact.getOwner());
                     owner.getCommunicator().sendNormalServerMessage(artifact.getName() + " vibrates faintly.");
                  }
               } catch (NoSuchCreatureException var7) {
               } catch (NoSuchPlayerException var8) {
               } catch (NotOwnedException var9) {
               }
            }
         }
      }
   }

   private static final void moveArtifact(Item artifact) {
      try {
         if (artifact.getOwner() != -10L) {
            Creature owner = Server.getInstance().getCreature(artifact.getOwner());
            owner.getCommunicator().sendNormalServerMessage(artifact.getName() + " disappears. It has fulfilled its mission.");
         }
      } catch (NoSuchCreatureException var2) {
      } catch (NoSuchPlayerException var3) {
      } catch (NotOwnedException var4) {
      }

      String act;
      switch(Server.rand.nextInt(6)) {
         case 0:
            act = "is reported to have disappeared.";
            break;
         case 1:
            act = "is gone missing.";
            break;
         case 2:
            act = "returned to the depths.";
            break;
         case 3:
            act = "seems to have decided to leave.";
            break;
         case 4:
            act = "has found a new location.";
            break;
         default:
            act = "has vanished.";
      }

      HistoryManager.addHistory("The " + artifact.getName(), act);
      artifact.putInVoid();
      placeArtifact(artifact);
   }

   public static final void destroyArtifacts() {
      EndGameItem[] arts = artifacts.values().toArray(new EndGameItem[artifacts.size()]);

      for(EndGameItem lArt : arts) {
         Item artifact = lArt.getItem();

         try {
            if (artifact.getOwner() != -10L) {
               Creature owner = Server.getInstance().getCreature(artifact.getOwner());
               owner.getCommunicator().sendNormalServerMessage(artifact.getName() + " disappears. It has fulfilled its mission.");
            }
         } catch (NoSuchCreatureException var7) {
         } catch (NoSuchPlayerException var8) {
         } catch (NotOwnedException var9) {
         }

         Items.destroyItem(artifact.getWurmId());
         lArt.destroy();
      }
   }

   public static final String locateEndGameItem(int templateId, Creature performer) {
      String toReturn = "The artifact was hidden from view by the gods.";
      if (Servers.localServer.HOMESERVER) {
         if (Servers.localServer.serverEast != null && !Servers.localServer.serverEast.HOMESERVER) {
            return "You feel a faint indication far to the east.";
         } else if (Servers.localServer.serverSouth != null && !Servers.localServer.serverSouth.HOMESERVER) {
            return "You feel a faint indication far to the south.";
         } else if (Servers.localServer.serverWest != null && !Servers.localServer.serverWest.HOMESERVER) {
            return "You feel a faint indication far to the west.";
         } else {
            return Servers.localServer.serverNorth != null && !Servers.localServer.serverNorth.HOMESERVER
               ? "You feel a faint indication far to the north."
               : toReturn;
         }
      } else {
         EndGameItem itemsearched = null;
         if (templateId == -1) {
            if (Server.rand.nextBoolean()) {
               missingCrowns.clear();
               Kingdom[] kingdoms = Kingdoms.getAllKingdoms();

               for(int x = 0; x < kingdoms.length; ++x) {
                  if (kingdoms[x].isCustomKingdom() && kingdoms[x].existsHere()) {
                     King k = King.getKing(kingdoms[x].getId());
                     if (k == null) {
                        missingCrowns.add(kingdoms[x]);
                     }
                  }
               }

               if (missingCrowns.size() > 0) {
                  int crownToLookFor = Server.rand.nextInt(missingCrowns.size());
                  Kingdom toLookFor = missingCrowns.get(crownToLookFor);
                  Item[] _items = Items.getAllItems();

                  for(Item lItem : _items) {
                     if (lItem.isRoyal() && lItem.getKingdom() == toLookFor.getId()) {
                        itemsearched = new EndGameItem(lItem, false, (short)122, false);
                     }
                  }
               }
            }

            if (itemsearched == null) {
               int s = artifacts.size();
               if (s > 0) {
                  int num = Server.rand.nextInt(s);
                  int x = 0;

                  for(Iterator<EndGameItem> it = artifacts.values().iterator(); it.hasNext(); ++x) {
                     itemsearched = it.next();
                     if (x == num) {
                        break;
                     }
                  }
               }
            }
         } else {
            Iterator<EndGameItem> it = artifacts.values().iterator();

            while(it.hasNext()) {
               itemsearched = it.next();
               if (itemsearched.getItem().getTemplateId() == templateId) {
                  break;
               }
            }
         }

         String name = "artifact";
         if (itemsearched != null && itemsearched.getItem() != null) {
            toReturn = "";
            name = itemsearched.getItem().getName();
            if (itemsearched.getType() == 122) {
               Kingdom k = Kingdoms.getKingdom(itemsearched.getItem().getKingdom());
               if (k != null) {
                  name = itemsearched.getItem().getName() + " of " + k.getName();
               }
            }

            int tilex = (int)itemsearched.getItem().getPosX() >> 2;
            int tiley = (int)itemsearched.getItem().getPosY() >> 2;
            if (itemsearched.getItem().getOwnerId() != -10L) {
               try {
                  Creature c = Server.getInstance().getCreature(itemsearched.getItem().getOwnerId());
                  toReturn = toReturn + "The " + name + " is carried by " + c.getName() + ". ";
                  VolaTile t = c.getCurrentTile();
                  if (t != null) {
                     if (t.getVillage() != null) {
                        toReturn = toReturn + c.getName() + " is in the settlement of " + t.getVillage().getName() + ". ";
                     }

                     if (t.getStructure() != null) {
                        toReturn = toReturn + c.getName() + " is in the house of " + t.getStructure().getName() + ". ";
                     }
                  }
               } catch (NoSuchCreatureException var18) {
                  toReturn = toReturn + "In your vision, you can only discern a shadow that carries the " + name + ". ";
               } catch (NoSuchPlayerException var19) {
                  toReturn = toReturn + "In your vision, you can only discern a shadow that carries the " + name + ". ";
               }
            } else if (itemsearched.isInWorld()) {
               VolaTile t = Zones.getTileOrNull(tilex, tiley, itemsearched.getItem().isOnSurface());
               if (t != null) {
                  if (t.getVillage() != null) {
                     toReturn = toReturn + "The " + name + " is in the settlement of " + t.getVillage().getName() + ". ";
                  }

                  if (t.getStructure() != null) {
                     toReturn = toReturn + "The " + name + " is in the house of " + t.getStructure().getName() + ". ";
                  }

                  try {
                     if (itemsearched.getItem() != null) {
                        long parentId = itemsearched.getItem().getTopParent();
                        Item parent = Items.getItem(parentId);
                        if (parent != itemsearched.getItem()) {
                           toReturn = toReturn + "It is within a " + parent.getName() + ".";
                        }
                     }
                  } catch (NoSuchItemException var17) {
                  }

                  toReturn = toReturn + "The " + name + " is in the wild. ";
                  VolaTile ct = performer.getCurrentTile();
                  if (ct != null) {
                     int ctx = ct.tilex;
                     int cty = ct.tiley;
                     int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                     int dir = MethodsCreatures.getDir(performer, tilex, tiley);
                     String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                     toReturn = toReturn + getDistanceString(mindist, name, direction, true);
                  }
               } else {
                  try {
                     Zone z = Zones.getZone(tilex, tiley, true);
                     Village[] villages = z.getVillages();
                     if (villages.length > 0) {
                        for(Village lVillage : villages) {
                           toReturn = toReturn + "The " + name + " is near the settlement of " + lVillage.getName() + ". ";
                        }
                     } else {
                        Structure[] structs = z.getStructures();
                        if (structs.length > 0) {
                           for(Structure lStruct : structs) {
                              toReturn = toReturn + "The " + name + " is near " + lStruct.getName() + ". ";
                           }
                        } else {
                           VolaTile ct = performer.getCurrentTile();
                           if (ct != null) {
                              int ctx = ct.tilex;
                              int cty = ct.tiley;
                              int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                              int dir = MethodsCreatures.getDir(performer, tilex, tiley);
                              String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                              toReturn = toReturn + getDistanceString(mindist, name, direction, true);
                           }
                        }
                     }
                  } catch (NoSuchZoneException var20) {
                     logger.log(
                        Level.WARNING,
                        "No Zone At " + tilex + ", " + tiley + " surf=true for item " + itemsearched.getItem().getName() + ".",
                        (Throwable)var20
                     );
                  }
               }
            } else {
               toReturn = toReturn + "The " + name + " has not yet been revealed. ";
               VolaTile ct = performer.getCurrentTile();
               if (ct != null) {
                  int ctx = ct.tilex;
                  int cty = ct.tiley;
                  int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                  int dir = MethodsCreatures.getDir(performer, tilex, tiley);
                  String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                  toReturn = toReturn + getDistanceString(mindist, name, direction, true);
               }
            }
         }

         return toReturn;
      }
   }

   public static final EndGameItem[] getArtifacts() {
      return artifacts.values().toArray(new EndGameItem[artifacts.values().size()]);
   }

   public static String getEpicPlayerLocateString(int mindist, String name, String direction) {
      String toReturn = "";
      if (mindist == 0) {
         toReturn = toReturn + "You are practically standing on the " + name + "! ";
      } else if (mindist < 1) {
         toReturn = toReturn + "The " + name + " is " + direction + " a few steps away! ";
      } else if (mindist < 4) {
         toReturn = toReturn + "The " + name + " is " + direction + " a stone's throw away! ";
      } else if (mindist < 6) {
         toReturn = toReturn + "The " + name + " is " + direction + " very close. ";
      } else if (mindist < 10) {
         toReturn = toReturn + "The " + name + " is " + direction + " pretty close by. ";
      } else if (mindist < 20) {
         toReturn = toReturn + "The " + name + " is " + direction + " fairly close by. ";
      } else if (mindist < 50) {
         toReturn = toReturn + "The " + name + " is some distance away " + direction + ". ";
      } else if (mindist < 200) {
         toReturn = toReturn + "The " + name + " is quite some distance away " + direction + ". ";
      } else {
         toReturn = toReturn + "No such soul found.";
      }

      return toReturn;
   }

   public static final String getDistanceString(int mindist, String name, String direction, boolean includeThe) {
      String toReturn = "";
      if (mindist == 0) {
         toReturn = toReturn + "You are practically standing on the " + name + "! ";
      } else if (mindist < 1) {
         toReturn = toReturn + "The " + name + " is " + direction + " a few steps away! ";
      } else if (mindist < 4) {
         toReturn = toReturn + "The " + name + " is " + direction + " a stone's throw away! ";
      } else if (mindist < 6) {
         toReturn = toReturn + "The " + name + " is " + direction + " very close. ";
      } else if (mindist < 10) {
         toReturn = toReturn + "The " + name + " is " + direction + " pretty close by. ";
      } else if (mindist < 20) {
         toReturn = toReturn + "The " + name + " is " + direction + " fairly close by. ";
      } else if (mindist < 50) {
         toReturn = toReturn + "The " + name + " is some distance away " + direction + ". ";
      } else if (mindist < 200) {
         toReturn = toReturn + "The " + name + " is quite some distance away " + direction + ". ";
      } else if (mindist < 500) {
         toReturn = toReturn + "The " + name + " is rather a long distance away " + direction + ". ";
      } else if (mindist < 1000) {
         toReturn = toReturn + "The " + name + " is pretty far away " + direction + ". ";
      } else if (mindist < 2000) {
         toReturn = toReturn + "The " + name + " is far away " + direction + ". ";
      } else {
         toReturn = toReturn + "The " + name + " is very far away " + direction + ". ";
      }

      return toReturn;
   }

   public static final String locateRandomEndGameItem(Creature performer) {
      return locateEndGameItem(-1, performer);
   }

   public static final void relocateAllEndGameItems() {
      for(EndGameItem eg : artifacts.values()) {
         eg.setLastMoved(System.currentTimeMillis());
         moveArtifact(eg.getItem());
      }

      for(EndGameItem altar : altars.values()) {
         Items.destroyItem(altar.getItem().getWurmId());
         altar.delete();
      }

      altars.clear();
   }
}

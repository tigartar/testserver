package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Hota implements TimeConstants, MiscConstants {
   private static final Logger logger = Logger.getLogger(Hota.class.getName());
   private static final String LOAD_ALL_HOTA_ITEMS = "SELECT * FROM HOTA_ITEMS";
   private static final String CREATE_HOTA_ITEM = "INSERT INTO HOTA_ITEMS (ITEMID,ITEMTYPE) VALUES (?,?)";
   private static final String DELETE_HOTA_ITEMS = "DELETE FROM HOTA_ITEMS";
   private static final String INSERT_HOTA_HELPER = "INSERT INTO HOTA_HELPERS (CONQUERS,WURMID) VALUES (?,?)";
   private static final String UPDATE_HOTA_HELPER = "UPDATE HOTA_HELPERS SET CONQUERS=? WHERE WURMID=?";
   private static final String LOAD_ALL_HOTA_HELPER = "SELECT * FROM HOTA_HELPERS";
   private static final String DELETE_HOTA_HELPERS = "DELETE FROM HOTA_HELPERS";
   private static final ConcurrentHashMap<Item, Byte> hotaItems = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Long, Integer> hotaHelpers = new ConcurrentHashMap<>();
   public static final byte TYPE_NONE = 0;
   public static final byte TYPE_PILLAR = 1;
   public static final byte TYPE_SPEEDSHRINE = 2;
   private static long nextRoundMessage = Long.MAX_VALUE;
   public static final int VILLAGE_ID_MOD = 2000000;
   public static final LinkedList<Item> pillarsLeft = new LinkedList<>();
   public static final LinkedList<Item> pillarsTouched = new LinkedList<>();
   private static final Set<Long> hotaConquerers = new HashSet<>();

   private Hota() {
   }

   public static void loadAllHotaItems() {
      long now = System.nanoTime();
      int numberOfItemsLoaded = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM HOTA_ITEMS");
         rs = ps.executeQuery();

         while(rs.next()) {
            byte hotatype = rs.getByte("ITEMTYPE");
            long itemId = rs.getLong("ITEMID");

            try {
               Item item = Items.getItem(itemId);
               hotaItems.put(item, hotatype);
               ++numberOfItemsLoaded;
            } catch (NoSuchItemException var15) {
               logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
            }
         }
      } catch (SQLException var16) {
         logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
         logger.log(Level.INFO, "Loaded " + numberOfItemsLoaded + " HOTA items. It took " + lElapsedTime + " millis.");
      }
   }

   public static void poll() {
      if (Servers.localServer.getNextHota() > 0L) {
         if (System.currentTimeMillis() > Servers.localServer.getNextHota()) {
            FocusZone hotaZone = FocusZone.getHotaZone();
            if (hotaZone != null) {
               if (hotaItems.isEmpty()) {
                  createHotaItems();
               }

               putHotaItemsInWorld();
               Servers.localServer.setNextHota(Long.MAX_VALUE);
               String toBroadCast = "The Hunt of the Ancients has begun!";
               switch(Server.rand.nextInt(4)) {
                  case 0:
                     toBroadCast = "The Hunt of the Ancients has begun!";
                     break;
                  case 1:
                     toBroadCast = "Let The Hunt Begin!";
                     break;
                  case 2:
                     toBroadCast = "Hunt! Hunt! Hunt!";
                     break;
                  case 3:
                     toBroadCast = "The Hunt of the Ancients is on!";
                     break;
                  case 4:
                     if (WurmCalendar.isNight()) {
                        toBroadCast = "It's the night of the Hunter!";
                     } else {
                        toBroadCast = "It's a glorious day for the Hunt!";
                     }
                     break;
                  case 5:
                     if (Server.rand.nextInt(100) == 0) {
                        toBroadCast = "Run, Forrest! Run!";
                     } else {
                        toBroadCast = "Go conquer those pillars!";
                     }
                     break;
                  default:
                     toBroadCast = "The Hunt of the Ancients has begun!";
               }

               Server.getInstance().broadCastSafe(toBroadCast);
            } else {
               putHotaItemsInVoid();
               Servers.localServer.setNextHota(0L);
               nextRoundMessage = Long.MAX_VALUE;
            }
         } else if (Servers.localServer.getNextHota() < Long.MAX_VALUE) {
            if (nextRoundMessage == Long.MAX_VALUE) {
               nextRoundMessage = System.currentTimeMillis();
            }

            long timeLeft = Servers.localServer.getNextHota() - System.currentTimeMillis();
            if (System.currentTimeMillis() >= nextRoundMessage) {
               String one = "The ";
               String two = "Hunt of the Ancients ";
               String three = "begins ";
               if (Server.rand.nextBoolean()) {
                  one = "The next ";
                  if (Server.rand.nextBoolean()) {
                     one = "A new ";
                  }
               }

               if (Server.rand.nextBoolean()) {
                  two = "Hunt ";
                  if (Server.rand.nextBoolean()) {
                     two = "HotA ";
                  }
               }

               if (Server.rand.nextBoolean()) {
                  three = "starts ";
                  if (Server.rand.nextBoolean()) {
                     three = "will begin ";
                  }
               }

               Server.getInstance().broadCastSafe(one + two + three + "in " + Server.getTimeFor(timeLeft) + ".");
               nextRoundMessage = System.currentTimeMillis() + (Servers.localServer.getNextHota() - System.currentTimeMillis()) / 2L;
            }
         }
      }
   }

   public static void addPillarConquered(Creature creature, Item pillar) {
      int numsToAdd = 5;
      Integer points = hotaHelpers.get(creature.getWurmId());
      if (points != null) {
         numsToAdd = points + 5;
      }

      addHotaHelper(creature, numsToAdd);
      if (!pillarsTouched.contains(pillar)) {
         if (creature.isPlayer() && !hotaConquerers.contains(creature.getWurmId())) {
            hotaConquerers.add(creature.getWurmId());
         }

         if (Servers.localServer.isChallengeServer() && creature.isPlayer()) {
            ChallengeSummary.addToScore(((Player)creature).getSaveFile(), ChallengePointEnum.ChallengePoint.HOTAPILLARS.getEnumtype(), 1.0F);
            ChallengeSummary.addToScore(((Player)creature).getSaveFile(), ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 5.0F);
         }

         pillarsTouched.add(pillar);
         if (!pillarsLeft.isEmpty()) {
            Item next = pillarsLeft.remove(Server.rand.nextInt(pillarsLeft.size()));
            putPillarInWorld(next);
         }
      }

      creature.achievement(1);
      Map<Integer, Integer> alliances = new HashMap<>();

      for(Item item : hotaItems.keySet()) {
         if (item.getData1() > 0) {
            Integer nums = alliances.get(item.getData1());
            if (nums != null) {
               nums = nums + 1;
            } else {
               nums = 1;
            }

            alliances.put(item.getData1(), nums);
            if (nums >= 4) {
               win(item.getData1(), creature);
            }
         }
      }
   }

   public final LinkedList<Item> getPillarsInWorld() {
      return pillarsTouched;
   }

   static void win(int allianceNumber, Creature winner) {
      PvPAlliance winAlliance = PvPAlliance.getPvPAlliance(allianceNumber);
      if (winAlliance != null) {
         Server.getInstance().broadCastSafe(winner.getName() + " has secured victory for " + winAlliance.getName() + "!");
         winAlliance.addHotaWin();
      } else {
         try {
            Village v = Villages.getVillage(allianceNumber - 2000000);
            Server.getInstance().broadCastSafe(winner.getName() + " has secured victory for " + v.getName() + "!");
            v.addHotaWin();
            v.createHotaPrize(v.getHotaWins());
         } catch (NoSuchVillageException var5) {
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            Server.getInstance().broadCastSafe(winner.getName() + " has secured victory for " + winner.getHimHerItString() + "self!");
            winner.setHotaWins((short)(winner.getHotaWins() + 1));
         }
      }

      clearHotaHelpers();
      putHotaItemsInVoid();
      Servers.localServer.setNextHota(System.currentTimeMillis() + 129600000L);
      nextRoundMessage = Servers.isThisATestServer() ? System.currentTimeMillis() + 60000L : System.currentTimeMillis() + 3600000L;
   }

   public static void addPillarTouched(Creature creature, Item pillar) {
      int sx = Zones.safeTileX(pillar.getTileX() - 30);
      int sy = Zones.safeTileY(pillar.getTileY() - 30);
      int ex = Zones.safeTileX(pillar.getTileX() + 30);
      int ey = Zones.safeTileY(pillar.getTileY() + 30);

      for(int x = sx; x < ex; ++x) {
         for(int y = sy; y < ey; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, creature.isOnSurface());
            if (t != null) {
               for(Creature c : t.getCreatures()) {
                  Integer points = hotaHelpers.get(c.getWurmId());
                  if (points == null) {
                     addHotaHelper(c, 1);
                  }
               }
            }
         }
      }
   }

   private static void putHotaItemsInVoid() {
      pillarsTouched.clear();
      pillarsLeft.clear();

      for(Item item : hotaItems.keySet()) {
         item.deleteAllEffects();
         item.setData1(0);
         if (item.getZoneId() > 0) {
            item.putInVoid();
         }
      }
   }

   public static final Set<Item> getHotaItems() {
      return hotaItems.keySet();
   }

   private static void putHotaItemsInWorld() {
      boolean SaromansEdition = Server.rand.nextInt(10) > 0;
      FocusZone hotaZone = FocusZone.getHotaZone();
      if (hotaZone != null) {
         int num = 0;
         int numShrines = 0;

         for(Item item : hotaItems.keySet()) {
            item.setData1(0);
            if (item.getZoneId() > 0) {
               item.deleteAllEffects();
               item.putInVoid();
            }

            int sizeX = hotaZone.getEndX() - hotaZone.getStartX();
            int xborder = sizeX / 10;
            int sizeXSlot = (sizeX - xborder - xborder) / 3;
            logger.log(Level.INFO, "Hota size x " + sizeX + " border=" + xborder + " sizeXSlot=" + sizeXSlot);
            int sizeY = hotaZone.getEndY() - hotaZone.getStartY();
            int yborder = sizeY / 10;
            int sizeYSlot = (sizeY - yborder - yborder) / 3;
            logger.log(Level.INFO, "Hota size y  " + sizeY + " border=" + yborder + " sizeYSlot=" + sizeYSlot);
            int tx = Zones.safeTileX(hotaZone.getStartX() + Server.rand.nextInt(Math.max(10, sizeX)));
            int ty = Zones.safeTileY(hotaZone.getStartY() + Server.rand.nextInt(Math.max(10, sizeY)));
            boolean pillar = false;
            if (item.getTemplateId() == 739) {
               pillar = true;
               tx = Zones.safeTileX(hotaZone.getStartX() + xborder + num % 3 * sizeXSlot + Server.rand.nextInt(sizeXSlot));
               if (num < 3) {
                  ty = Zones.safeTileY(hotaZone.getStartY() + yborder + Server.rand.nextInt(sizeYSlot));
               } else if (num < 6) {
                  ty = Zones.safeTileY(hotaZone.getStartY() + yborder + sizeYSlot + Server.rand.nextInt(sizeYSlot));
               } else if (num < 9) {
                  ty = Zones.safeTileY(hotaZone.getStartY() + yborder + 2 * sizeYSlot + Server.rand.nextInt(sizeYSlot));
               } else if (num >= 9) {
                  tx = Zones.safeTileX(hotaZone.getStartX() + xborder + Server.rand.nextInt(sizeXSlot * 3));
                  ty = Zones.safeTileY(hotaZone.getStartY() + yborder + Server.rand.nextInt(sizeYSlot * 3));
               }

               ++num;
            } else if (item.getTemplateId() == 741) {
               tx = Math.max(0, Zones.safeTileX(hotaZone.getStartX() + xborder + sizeXSlot / 2 + numShrines % 2 * sizeXSlot + Server.rand.nextInt(sizeXSlot)));
               if (numShrines < 2) {
                  ty = Zones.safeTileY(hotaZone.getStartY() + yborder + sizeYSlot / 2 + Server.rand.nextInt(sizeXSlot));
               } else {
                  ty = Zones.safeTileY(hotaZone.getStartY() + yborder + sizeYSlot / 2 + sizeYSlot + Server.rand.nextInt(sizeXSlot));
               }

               ++numShrines;
            }

            float posx = (float)((tx << 2) + 2);
            float posy = (float)((ty << 2) + 2);

            try {
               item.setPosXYZ(posx, posy, Zones.calculateHeight(posx, posy, true));
               if (pillar && SaromansEdition) {
                  pillarsLeft.add(item);
               } else {
                  putPillarInWorld(item);
               }

               logger.log(Level.INFO, item.getName() + " " + num + "(" + numShrines + ") put at " + tx + "," + ty + " num % 3 =" + num % 3);
            } catch (NoSuchZoneException var18) {
               logger.log(Level.INFO, "Item " + item.getWurmId() + " outside range " + item.getPosX() + " " + item.getPosY());
            }
         }

         if (!pillarsLeft.isEmpty()) {
            Item next = pillarsLeft.remove(Server.rand.nextInt(pillarsLeft.size()));
            putPillarInWorld(next);
            Item next2 = pillarsLeft.remove(Server.rand.nextInt(pillarsLeft.size()));
            putPillarInWorld(next2);
            Item next3 = pillarsLeft.remove(Server.rand.nextInt(pillarsLeft.size()));
            putPillarInWorld(next3);
         }
      }
   }

   public static final void putPillarInWorld(Item pillar) {
      try {
         Zone z = Zones.getZone((int)pillar.getPosX() >> 2, (int)pillar.getPosY() >> 2, true);
         z.addItem(pillar);
         logger.log(Level.INFO, pillar.getName() + " spawned at " + pillar.getTileX() + "," + pillar.getTileY());
      } catch (NoSuchZoneException var2) {
         logger.log(Level.INFO, "Pillar " + pillar.getWurmId() + " outside range " + pillar.getPosX() + " " + pillar.getPosY());
      }
   }

   public static final void forcePillarsToWorld() {
      if (Servers.localServer.isShuttingDownIn < 30 && Servers.localServer.getNextHota() == Long.MAX_VALUE) {
         logger.warning("Forcing all remaining pillars to spawn into the world");

         while(!pillarsLeft.isEmpty()) {
            Item next = pillarsLeft.removeLast();
            logger.fine("Putting " + next.getName() + " in the world!");
            putPillarInWorld(next);
         }
      } else if (!Servers.localServer.maintaining) {
         logger.warning("Something just tried to force all HoTA pillars to spawn when not appropriate");
      }
   }

   static void createHotaItems() {
      logger.info("Creating Hunt of the Ancients items.");

      try {
         Item pillarOne = ItemFactory.createItem(739, 90.0F, null);
         pillarOne.setName("Green pillar of the hunt");
         pillarOne.setColor(WurmColor.createColor(0, 255, 0));
         hotaItems.put(pillarOne, (byte)1);
         insertHotaItem(pillarOne, (byte)1);
         Item pillarTwo = ItemFactory.createItem(739, 90.0F, null);
         pillarTwo.setName("Blue pillar of the hunt");
         pillarTwo.setColor(WurmColor.createColor(0, 0, 255));
         hotaItems.put(pillarTwo, (byte)1);
         insertHotaItem(pillarTwo, (byte)1);
         Item pillarThree = ItemFactory.createItem(739, 90.0F, null);
         pillarThree.setName("Red pillar of the hunt");
         pillarThree.setColor(WurmColor.createColor(255, 0, 0));
         hotaItems.put(pillarThree, (byte)1);
         insertHotaItem(pillarThree, (byte)1);
         Item pillarFour = ItemFactory.createItem(739, 90.0F, null);
         pillarFour.setName("Yellow pillar of the hunt");
         pillarFour.setColor(WurmColor.createColor(238, 244, 6));
         hotaItems.put(pillarFour, (byte)1);
         insertHotaItem(pillarFour, (byte)1);
         Item pillarFive = ItemFactory.createItem(739, 90.0F, null);
         pillarFive.setName("Sky pillar of the hunt");
         pillarFive.setColor(WurmColor.createColor(33, 208, 218));
         hotaItems.put(pillarFive, (byte)1);
         insertHotaItem(pillarFive, (byte)1);
         Item pillarSix = ItemFactory.createItem(739, 90.0F, null);
         pillarSix.setName("Black pillar of the hunt");
         pillarSix.setColor(WurmColor.createColor(0, 0, 0));
         hotaItems.put(pillarSix, (byte)1);
         insertHotaItem(pillarSix, (byte)1);
         Item pillarSeven = ItemFactory.createItem(739, 90.0F, null);
         pillarSeven.setName("Clear pillar of the hunt");
         pillarSix.setColor(WurmColor.createColor(255, 255, 255));
         hotaItems.put(pillarSeven, (byte)1);
         insertHotaItem(pillarSeven, (byte)1);
         Item pillarEight = ItemFactory.createItem(739, 90.0F, null);
         pillarEight.setName("Brown pillar of the hunt");
         pillarEight.setColor(WurmColor.createColor(154, 88, 22));
         hotaItems.put(pillarEight, (byte)1);
         insertHotaItem(pillarEight, (byte)1);
         Item pillarNine = ItemFactory.createItem(739, 90.0F, null);
         pillarNine.setName("Lilac pillar of the hunt");
         pillarNine.setColor(WurmColor.createColor(134, 69, 186));
         hotaItems.put(pillarNine, (byte)1);
         insertHotaItem(pillarNine, (byte)1);
         Item pillarTen = ItemFactory.createItem(739, 90.0F, null);
         pillarTen.setName("Orange pillar of the hunt");
         pillarTen.setColor(WurmColor.createColor(255, 128, 186));
         hotaItems.put(pillarTen, (byte)1);
         insertHotaItem(pillarTen, (byte)1);
         Item shrineOne = ItemFactory.createItem(741, 90.0F, null);
         hotaItems.put(shrineOne, (byte)2);
         insertHotaItem(shrineOne, (byte)2);
         Item shrineTwo = ItemFactory.createItem(741, 90.0F, null);
         hotaItems.put(shrineTwo, (byte)2);
         insertHotaItem(shrineTwo, (byte)2);
         Item shrineThree = ItemFactory.createItem(741, 90.0F, null);
         hotaItems.put(shrineThree, (byte)2);
         insertHotaItem(shrineThree, (byte)2);
         Item shrineFour = ItemFactory.createItem(741, 90.0F, null);
         hotaItems.put(shrineFour, (byte)2);
         insertHotaItem(shrineFour, (byte)2);
      } catch (NoSuchTemplateException var18) {
         logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
      } catch (FailedException var19) {
         logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
      } finally {
         logger.info("Finished creating Hunt of the Ancients items.");
      }
   }

   public static void loadAllHelpers() {
      long now = System.nanoTime();
      int numberOfHelpersLoaded = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM HOTA_HELPERS");

         for(rs = ps.executeQuery(); rs.next(); ++numberOfHelpersLoaded) {
            int conquerValue = rs.getInt("CONQUERS");
            long wid = rs.getLong("WURMID");
            hotaHelpers.put(wid, conquerValue);
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
         logger.log(Level.INFO, "Loaded " + numberOfHelpersLoaded + " HOTA helpers. It took " + lElapsedTime + " millis.");
      }
   }

   public static int getHelpValue(long creatureId) {
      Integer helped = hotaHelpers.get(creatureId);
      return helped != null ? helped : 0;
   }

   static void addHotaHelper(Creature creature, int helpValue) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         String updateOrInsert = "INSERT INTO HOTA_HELPERS (CONQUERS,WURMID) VALUES (?,?)";
         Integer helped = hotaHelpers.get(creature.getWurmId());
         if (helped != null) {
            updateOrInsert = "UPDATE HOTA_HELPERS SET CONQUERS=? WHERE WURMID=?";
         }

         hotaHelpers.put(creature.getWurmId(), helpValue);
         ps = dbcon.prepareStatement(updateOrInsert);
         ps.setInt(1, helpValue);
         ps.setLong(2, creature.getWurmId());
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update Hota helper: " + creature.getName() + " with value " + helpValue, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void destroyHota() {
      for(Item i : hotaItems.keySet()) {
         Items.destroyItem(i.getWurmId());
      }

      hotaItems.clear();
      clearHotaHelpers();
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM HOTA_ITEMS");
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to delete hota items", (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      Servers.localServer.setNextHota(0L);
      nextRoundMessage = Long.MAX_VALUE;
   }

   static void clearHotaHelpers() {
      hotaHelpers.clear();
      hotaConquerers.clear();
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM HOTA_HELPERS");
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to delete all Hota helpers: ", (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void insertHotaItem(Item item, byte type) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("INSERT INTO HOTA_ITEMS (ITEMID,ITEMTYPE) VALUES (?,?)");
         ps.setLong(1, item.getWurmId());
         ps.setByte(2, type);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to insert hota item id " + item.getWurmId() + " - " + item.getName() + " and type " + type, (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}

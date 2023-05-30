package com.wurmonline.server.epic;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class EpicTargetItems implements MiscConstants {
   private final long[] epicTargetItems = new long[18];
   private static final String LOAD_ALL_TARGET_ITEMS = "SELECT * FROM EPICTARGETITEMS WHERE KINGDOM=?";
   private static final String UPDATE_TARGET_ITEMS = "UPDATE EPICTARGETITEMS SET PILLARONE=?,PILLARTWO=?,PILLARTHREE=?,OBELISQUEONE=?,OBELISQUETWO=?,OBELISQUETHREE=?,PYLONONE=?,PYLONTWO=?,PYLONTHREE=?,TEMPLEONE=?,TEMPLETWO=?,TEMPLETHREE=?,SHRINEONE=?,SHRINETWO=?,SHRINETHREE=?,SPIRITGATEONE=?,SPIRITGATETWO=?,SPIRITGATETHREE=? WHERE KINGDOM=?";
   private static final String INSERT_TARGET_ITEMS = "INSERT INTO EPICTARGETITEMS (KINGDOM) VALUES(?)";
   private final byte kingdomId;
   static final int PILLAR_ONE = 0;
   static final int PILLAR_TWO = 1;
   static final int PILLAR_THREE = 2;
   static final int OBELISK_ONE = 3;
   static final int OBELISK_TWO = 4;
   static final int OBELISK_THREE = 5;
   static final int PYLON_ONE = 6;
   static final int PYLON_TWO = 7;
   static final int PYLON_THREE = 8;
   static final int TEMPLE_ONE = 9;
   static final int TEMPLE_TWO = 10;
   static final int TEMPLE_THREE = 11;
   static final int SHRINE_ONE = 12;
   static final int SHRINE_TWO = 13;
   static final int SHRINE_THREE = 14;
   static final int SPIRIT_GATE_ONE = 15;
   static final int SPIRIT_GATE_TWO = 16;
   static final int SPIRIT_GATE_THREE = 17;
   private static final Logger logger = Logger.getLogger(EpicTargetItems.class.getName());
   private static final Map<Byte, EpicTargetItems> KINGDOM_ITEMS = new ConcurrentHashMap<>();
   private static final ArrayList<Item> ritualTargetItems = new ArrayList<>();

   public EpicTargetItems(byte kingdomTemplateId) {
      this.kingdomId = kingdomTemplateId;
      this.loadAll();
      MissionHelper.loadAll();
   }

   public static void removeRitualTargetItem(Item ritualItem) {
      if (ritualTargetItems.contains(ritualItem)) {
         ritualTargetItems.remove(ritualItem);
      }
   }

   public static void addRitualTargetItem(Item ritualItem) {
      if (ritualItem != null) {
         if (ritualItem.isEpicTargetItem()) {
            if (!ritualItem.isUnfinished()) {
               if (!ritualTargetItems.contains(ritualItem)) {
                  ritualTargetItems.add(ritualItem);
               }
            }
         }
      }
   }

   public static Item getRandomRitualTarget() {
      return ritualTargetItems.isEmpty() ? null : ritualTargetItems.get(Server.rand.nextInt(ritualTargetItems.size()));
   }

   public static final EpicTargetItems getEpicTargets(byte kingdomTemplateId) {
      EpicTargetItems toReturn = KINGDOM_ITEMS.get(kingdomTemplateId);
      if (toReturn == null) {
         toReturn = new EpicTargetItems(kingdomTemplateId);
         KINGDOM_ITEMS.put(kingdomTemplateId, toReturn);
      }

      return toReturn;
   }

   public static final boolean isItemAlreadyEpic(Item itemChecked) {
      for(EpicTargetItems etis : KINGDOM_ITEMS.values()) {
         for(long item : etis.epicTargetItems) {
            if (item == itemChecked.getWurmId()) {
               return true;
            }
         }
      }

      return false;
   }

   public final byte getKingdomTemplateId() {
      return this.kingdomId;
   }

   public final void loadAll() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean found = false;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM EPICTARGETITEMS WHERE KINGDOM=?");
         ps.setByte(1, this.kingdomId);

         for(rs = ps.executeQuery(); rs.next(); found = true) {
            rs.getByte(1);

            for(int x = 0; x <= 17; ++x) {
               this.epicTargetItems[x] = rs.getLong(x + 2);
            }
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to load epic target items.", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      if (!found) {
         this.initialize();
      }
   }

   private final void initialize() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("INSERT INTO EPICTARGETITEMS (KINGDOM) VALUES(?)");
         ps.setByte(1, this.kingdomId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save epic target status for kingdom " + this.kingdomId, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void testSetCounter(int toSet, long wid) {
      this.epicTargetItems[toSet] = wid;
      this.update();
   }

   public static final boolean isEpicItemWithMission(Item epicItem) {
      for(EpicMission m : EpicServerStatus.getCurrentEpicMissions()) {
         boolean correctItem = false;
         switch(m.getMissionType()) {
            case 101:
               if (epicItem.getTemplateId() == 717 || epicItem.getTemplateId() == 712) {
                  correctItem = true;
               }
               break;
            case 102:
               if (epicItem.getTemplateId() == 715 || epicItem.getTemplateId() == 714) {
                  correctItem = true;
               }
               break;
            case 103:
               if (epicItem.getTemplateId() == 713 || epicItem.getTemplateId() == 716) {
                  correctItem = true;
               }
         }

         if (correctItem) {
            int placementLocation = getTargetItemPlacement(m.getMissionId());
            int itemLocation = epicItem.getGlobalMapPlacement();
            if (itemLocation == placementLocation) {
               return true;
            }

            if (placementLocation == 0 && epicItem.isInTheNorth()) {
               return true;
            }

            if (placementLocation == 2 && epicItem.isInTheEast()) {
               return true;
            }

            if (placementLocation == 4 && epicItem.isInTheSouth()) {
               return true;
            }

            if (placementLocation == 6 && epicItem.isInTheWest()) {
               return true;
            }
         }
      }

      return false;
   }

   public final boolean addEpicItem(Item epicItem, Creature performer) {
      if (epicItem.isEpicTargetItem()) {
         if (mayBuildEpicItem(
            epicItem.getTemplateId(), epicItem.getTileX(), epicItem.getTileY(), epicItem.isOnSurface(), performer, performer.getKingdomTemplateId()
         )) {
            if (epicItem.getGlobalMapPlacement() == this.getGlobalMapPlacementRequirement(epicItem.getTemplateId())) {
               logger.log(Level.INFO, performer.getName() + " Correct placement for " + epicItem);
               performer.sendToLoggers("Correct placement for " + epicItem, (byte)2);
               int toSet = this.getCurrentCounter(epicItem.getTemplateId());
               this.epicTargetItems[toSet] = epicItem.getWurmId();
               this.update();
               return true;
            }

            logger.log(
               Level.INFO,
               performer.getName()
                  + " Not proper map placement "
                  + epicItem.getGlobalMapPlacement()
                  + " for "
                  + epicItem.getName()
                  + " here at "
                  + epicItem.getTileX()
                  + ","
                  + epicItem.getTileY()
                  + ": Required "
                  + this.getGlobalMapPlacementRequirement(epicItem.getTemplateId())
            );
            performer.sendToLoggers(
               "Not proper map placement "
                  + epicItem.getGlobalMapPlacement()
                  + " for "
                  + epicItem.getName()
                  + " here at "
                  + epicItem.getTileX()
                  + ","
                  + epicItem.getTileY()
                  + ": Required "
                  + this.getGlobalMapPlacementRequirement(epicItem.getTemplateId()),
               (byte)2
            );
         } else {
            performer.sendToLoggers("May not build " + epicItem.getName() + " here at " + epicItem.getTileX() + "," + epicItem.getTileY(), (byte)2);
         }
      }

      return false;
   }

   public final int testGetCurrentCounter(int templateId) {
      return this.getCurrentCounter(templateId);
   }

   public final int getCurrentCounter(int itemTemplateId) {
      int toReturn = -1;
      switch(itemTemplateId) {
         case 712:
            if (this.epicTargetItems[12] == 0L) {
               return 12;
            }

            if (this.epicTargetItems[13] == 0L) {
               return 13;
            }

            if (this.epicTargetItems[14] == 0L) {
               return 14;
            }
            break;
         case 713:
            if (this.epicTargetItems[6] == 0L) {
               return 6;
            }

            if (this.epicTargetItems[7] == 0L) {
               return 7;
            }

            if (this.epicTargetItems[8] == 0L) {
               return 8;
            }
            break;
         case 714:
            if (this.epicTargetItems[3] == 0L) {
               return 3;
            }

            if (this.epicTargetItems[4] == 0L) {
               return 4;
            }

            if (this.epicTargetItems[5] == 0L) {
               return 5;
            }
            break;
         case 715:
            if (this.epicTargetItems[9] == 0L) {
               return 9;
            }

            if (this.epicTargetItems[10] == 0L) {
               return 10;
            }

            if (this.epicTargetItems[11] == 0L) {
               return 11;
            }
            break;
         case 716:
            if (this.epicTargetItems[15] == 0L) {
               return 15;
            }

            if (this.epicTargetItems[16] == 0L) {
               return 16;
            }

            if (this.epicTargetItems[17] == 0L) {
               return 17;
            }
            break;
         case 717:
            if (this.epicTargetItems[0] == 0L) {
               return 0;
            }

            if (this.epicTargetItems[1] == 0L) {
               return 1;
            }

            if (this.epicTargetItems[2] == 0L) {
               return 2;
            }
            break;
         default:
            toReturn = -1;
      }

      return toReturn;
   }

   public static final String getSymbolNamePartString(Creature performer) {
      String toReturn = "Faith";
      int rand = Server.rand.nextInt(50);
      byte kingdomId = performer.getKingdomTemplateId();
      switch(rand) {
         case 0:
            toReturn = "Secrets";
            break;
         case 1:
            if (kingdomId == 3) {
               toReturn = "Libila";
            } else if (kingdomId == 2) {
               toReturn = "Magranon";
            } else if (performer.getDeity() != null && performer.getDeity().number == 1) {
               toReturn = "Fo";
            } else {
               toReturn = "Vynora";
            }
            break;
         case 2:
            if (kingdomId == 3) {
               toReturn = "Hate";
            } else if (kingdomId == 2) {
               toReturn = "Fire";
            } else if (performer.getDeity() != null && performer.getDeity().number == 1) {
               toReturn = "Love";
            } else {
               toReturn = "Mysteries";
            }
            break;
         case 3:
            if (kingdomId == 3) {
               toReturn = "Revenge";
            } else if (kingdomId == 2) {
               toReturn = "Power";
            } else if (performer.getDeity() != null && performer.getDeity().number == 1) {
               toReturn = "Compassion";
            } else {
               toReturn = "Wisdom";
            }
            break;
         case 4:
            if (kingdomId == 3) {
               toReturn = "Death";
            } else if (kingdomId == 2) {
               toReturn = "Sand";
            } else if (performer.getDeity() != null && performer.getDeity().number == 1) {
               toReturn = "Tree";
            } else {
               toReturn = "Water";
            }
            break;
         case 5:
            toReturn = "Spirit";
            break;
         case 6:
            toReturn = "Soul";
            break;
         case 7:
            toReturn = "Hope";
            break;
         case 8:
            toReturn = "Despair";
            break;
         case 9:
            toReturn = "Luck";
            break;
         case 10:
            toReturn = "Heaven";
            break;
         case 11:
            toReturn = "Valrei";
            break;
         case 12:
            toReturn = "Strength";
            break;
         case 13:
            toReturn = "Sleep";
            break;
         case 14:
            toReturn = "Tongue";
            break;
         case 15:
            toReturn = "Dreams";
            break;
         case 16:
            toReturn = "Enlightened";
            break;
         case 17:
            toReturn = "Fool";
            break;
         case 18:
            toReturn = "Cat";
            break;
         case 19:
            toReturn = "Troll";
            break;
         case 20:
            toReturn = "Dragon";
            break;
         case 21:
            toReturn = "Deep";
            break;
         case 22:
            toReturn = "Square";
            break;
         case 23:
            toReturn = "Song";
            break;
         case 24:
            toReturn = "Jump";
            break;
         case 25:
            toReturn = "High";
            break;
         case 26:
            toReturn = "Low";
            break;
         case 27:
            toReturn = "Inbetween";
            break;
         case 28:
            toReturn = "One";
            break;
         case 29:
            toReturn = "Many";
            break;
         case 30:
            toReturn = "Sorrow";
            break;
         case 31:
            toReturn = "Pain";
            break;
         case 32:
            toReturn = "Oracle";
            break;
         case 33:
            toReturn = "Slithering";
            break;
         case 34:
            toReturn = "Roundabout";
            break;
         case 35:
            toReturn = "Winter";
            break;
         case 36:
            toReturn = "Summer";
            break;
         case 37:
            toReturn = "Fallen";
            break;
         case 38:
            toReturn = "Cherry";
            break;
         case 39:
            toReturn = "Innocent";
            break;
         case 40:
            toReturn = "Demon";
            break;
         case 41:
            toReturn = "Left";
            break;
         case 42:
            toReturn = "Shard";
            break;
         case 43:
            toReturn = "Mantra";
            break;
         case 44:
            toReturn = "Island";
            break;
         case 45:
            toReturn = "Seafarer";
            break;
         case 46:
            toReturn = "Ascendant";
            break;
         case 47:
            toReturn = "Shame";
            break;
         case 48:
            toReturn = "Running";
            break;
         case 49:
            toReturn = "Lamentation";
            break;
         default:
            toReturn = "Figure";
      }

      return toReturn;
   }

   public static final String getTypeNamePartString(int itemTemplateId) {
      String toReturn = "Focus";
      int rand = Server.rand.nextInt(10);
      return getTypeNamePartStringWithPart(itemTemplateId, rand);
   }

   static final String getTypeNamePartStringWithPart(int itemTemplateId, int partId) {
      switch(itemTemplateId) {
         case 712:
            switch(partId) {
               case 0:
                  return "Shrine";
               case 1:
                  return "Barrow";
               case 2:
                  return "Vault";
               case 3:
                  return "Long Home";
               case 4:
                  return "Mausoleum";
               case 5:
                  return "Chamber";
               case 6:
                  return "Reliquary";
               case 7:
                  return "Remembrance";
               case 8:
                  return "Sacrarium";
               case 9:
                  return "Sanctum";
               default:
                  return "Shrine";
            }
         case 713:
            switch(partId) {
               case 0:
                  return "Memento";
               case 1:
                  return "Monument";
               case 2:
                  return "Path";
               case 3:
                  return "Way";
               case 4:
                  return "Door";
               case 5:
                  return "Victorial";
               case 6:
                  return "Shield";
               case 7:
                  return "Passage";
               case 8:
                  return "Rest";
               case 9:
                  return "Gate";
               default:
                  return "Pylon";
            }
         case 714:
            switch(partId) {
               case 0:
                  return "Needle";
               case 1:
                  return "Fist";
               case 2:
                  return "Obelisk";
               case 3:
                  return "Charge";
               case 4:
                  return "Mantra";
               case 5:
                  return "Testimonial";
               case 6:
                  return "Trophy";
               case 7:
                  return "Stand";
               case 8:
                  return "Spear";
               case 9:
                  return "Challenge";
               default:
                  return "Obelisk";
            }
         case 715:
            switch(partId) {
               case 0:
                  return "Church";
               case 1:
                  return "Temple";
               case 2:
                  return "Hand";
               case 3:
                  return "House";
               case 4:
                  return "Sanctuary";
               case 5:
                  return "Chapel";
               case 6:
                  return "Abode";
               case 7:
                  return "Walls";
               case 8:
                  return "Sign";
               case 9:
                  return "Fist";
               default:
                  return "Temple";
            }
         case 716:
            switch(partId) {
               case 0:
                  return "Pathway";
               case 1:
                  return "Mirror";
               case 2:
                  return "Mystery";
               case 3:
                  return "Gate";
               case 4:
                  return "Shimmer";
               case 5:
                  return "Route";
               case 6:
                  return "Run";
               case 7:
                  return "Trail";
               case 8:
                  return "Wake";
               case 9:
                  return "Secret";
               default:
                  return "Gate";
            }
         case 717:
            switch(partId) {
               case 0:
                  return "Pillar";
               case 1:
                  return "Foundation";
               case 2:
                  return "Ram";
               case 3:
                  return "Symbol";
               case 4:
                  return "Tower";
               case 5:
                  return "Post";
               case 6:
                  return "Column";
               case 7:
                  return "Backbone";
               case 8:
                  return "Menhir";
               case 9:
                  return "Last Stand";
               default:
                  return "Pillar";
            }
         default:
            return "Monument";
      }
   }

   public final String getInstructionString(int itemTemplateId) {
      return getInstructionStringForKingdom(itemTemplateId, this.kingdomId);
   }

   public static final String getInstructionStringForKingdom(int itemTemplateId, byte aKingdomId) {
      String toReturn;
      switch(itemTemplateId) {
         case 712:
            toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. A couple of fruit trees or bushes must be within 5 tiles.";
            break;
         case 713:
            toReturn = "This must be constructed on a 7x7 slabbed area close to water, not inside a settlement, and on a flat surface.";
            break;
         case 714:
            toReturn = "This must be constructed on a 3x3 slabbed area, not inside a settlement, and on a flat surface.";
            break;
         case 715:
            if (aKingdomId == 3) {
               toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be within 5 tiles of marsh or mycelium.";
            } else {
               toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be built higher up than 100 steps.";
            }
            break;
         case 716:
            if (aKingdomId == 3) {
               if (Servers.localServer.PVPSERVER) {
                  toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be within 5 tiles of marsh as well as mycelium.";
               } else {
                  toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be within 5 tiles of marsh as well as moss.";
               }
            } else {
               toReturn = "This must be constructed on a 5x5 slabbed area, not inside a settlement, and on a flat surface. It must be built higher up than 100 steps.";
            }
            break;
         case 717:
            toReturn = "This should be built in the darkness of a cave with sufficient ceiling height, not inside a settlement, and on a flat surface.";
            break;
         default:
            toReturn = "It is not the right time to build this now.";
      }

      return toReturn;
   }

   public final String getGlobalMapPlacementRequirementString(int itemTemplateId) {
      int placement = this.getGlobalMapPlacementRequirement(itemTemplateId);
      String toReturn;
      switch(placement) {
         case 1:
            toReturn = "This must be built in the north east.";
            break;
         case 2:
         case 4:
         case 6:
         default:
            toReturn = "It is not the right time to build this now.";
            break;
         case 3:
            toReturn = "This must be built in the south east.";
            break;
         case 5:
            toReturn = "This must be built in the south west.";
            break;
         case 7:
            toReturn = "This must be built in the north west.";
      }

      return toReturn;
   }

   public final int getGlobalMapPlacementRequirement(int itemTemplateId) {
      int counter = this.getCurrentCounter(itemTemplateId);
      int toReturn = 0;
      return counter <= -1 ? toReturn : getGlobalMapPlacementRequirementWithCounter(itemTemplateId, counter, this.kingdomId);
   }

   public static final String getTargetItemPlacementString(int placementLocation) {
      switch(placementLocation) {
         case 0:
            return "This must be built in the north.";
         case 1:
            return "This must be built in the northeast.";
         case 2:
            return "This must be built in the east.";
         case 3:
            return "This must be built in the southeast.";
         case 4:
            return "This must be built in the south.";
         case 5:
            return "This must be built in the southwest.";
         case 6:
            return "This must be built in the west.";
         case 7:
            return "This must be built in the northwest.";
         default:
            return "It is not the right time to build this now.";
      }
   }

   public static final int getTargetItemPlacement(int missionId) {
      Random r = new Random((long)missionId);
      return r.nextInt(8);
   }

   static final int getGlobalMapPlacementRequirementWithCounter(int aItemTemplateId, int aCounter, byte aKingdomId) {
      int toReturn = 0;
      switch(aItemTemplateId) {
         case 712:
            switch(aKingdomId) {
               case 1:
               case 4:
                  switch(aCounter) {
                     case 12:
                        return 3;
                     case 13:
                        return 7;
                     case 14:
                        return 5;
                     default:
                        return 0;
                  }
               case 2:
                  switch(aCounter) {
                     case 12:
                        return 1;
                     case 13:
                        return 7;
                     case 14:
                        return 3;
                     default:
                        return 0;
                  }
               case 3:
                  switch(aCounter) {
                     case 12:
                        return 1;
                     case 13:
                        return 3;
                     case 14:
                        return 7;
                     default:
                        return 0;
                  }
               default:
                  return 0;
            }
         case 713:
            switch(aKingdomId) {
               case 1:
               case 4:
                  switch(aCounter) {
                     case 6:
                        return 3;
                     case 7:
                        return 7;
                     case 8:
                        return 5;
                     default:
                        return 0;
                  }
               case 2:
                  switch(aCounter) {
                     case 6:
                        return 3;
                     case 7:
                        return 1;
                     case 8:
                        return 5;
                     default:
                        return 0;
                  }
               case 3:
                  switch(aCounter) {
                     case 6:
                        return 5;
                     case 7:
                        return 7;
                     case 8:
                        return 1;
                     default:
                        return 0;
                  }
               default:
                  return 0;
            }
         case 714:
            switch(aKingdomId) {
               case 1:
               case 4:
                  switch(aCounter) {
                     case 3:
                        return 7;
                     case 4:
                        return 3;
                     case 5:
                        return 5;
                     default:
                        return 0;
                  }
               case 2:
                  switch(aCounter) {
                     case 3:
                        return 7;
                     case 4:
                        return 3;
                     case 5:
                        return 1;
                     default:
                        return 0;
                  }
               case 3:
                  switch(aCounter) {
                     case 3:
                        return 7;
                     case 4:
                        return 3;
                     case 5:
                        return 1;
                     default:
                        return 0;
                  }
               default:
                  return 0;
            }
         case 715:
            switch(aKingdomId) {
               case 1:
               case 4:
                  switch(aCounter) {
                     case 9:
                        return 3;
                     case 10:
                        return 7;
                     case 11:
                        return 5;
                     default:
                        return 0;
                  }
               case 2:
                  switch(aCounter) {
                     case 9:
                        return 3;
                     case 10:
                        return 1;
                     case 11:
                        return 5;
                     default:
                        return 0;
                  }
               case 3:
                  switch(aCounter) {
                     case 9:
                        return 7;
                     case 10:
                        return 5;
                     case 11:
                        return 1;
                     default:
                        return 0;
                  }
               default:
                  return 0;
            }
         case 716:
            switch(aKingdomId) {
               case 1:
               case 4:
                  switch(aCounter) {
                     case 15:
                        return 5;
                     case 16:
                        return 3;
                     case 17:
                        return 7;
                     default:
                        return 0;
                  }
               case 2:
                  switch(aCounter) {
                     case 15:
                        return 7;
                     case 16:
                        return 1;
                     case 17:
                        return 3;
                     default:
                        return 0;
                  }
               case 3:
                  switch(aCounter) {
                     case 15:
                        return 1;
                     case 16:
                        return 7;
                     case 17:
                        return 3;
                     default:
                        return 0;
                  }
               default:
                  return 0;
            }
         case 717:
            switch(aKingdomId) {
               case 1:
               case 4:
                  switch(aCounter) {
                     case 0:
                        return 3;
                     case 1:
                        return 7;
                     case 2:
                        return 5;
                     default:
                        return 0;
                  }
               case 2:
                  switch(aCounter) {
                     case 0:
                        return 5;
                     case 1:
                        return 1;
                     case 2:
                        return 3;
                     default:
                        return 0;
                  }
               case 3:
                  switch(aCounter) {
                     case 0:
                        return 7;
                     case 1:
                        return 1;
                     case 2:
                        return 5;
                     default:
                        return 0;
                  }
               default:
                  return 0;
            }
         default:
            return 0;
      }
   }

   public static final boolean mayBuildEpicItem(int itemTemplateId, int tilex, int tiley, boolean surfaced, Creature performer, byte kingdomTemplateId) {
      if (!Terraforming.isFlat(tilex, tiley, surfaced, 4)) {
         performer.sendToLoggers("The tile is not flat", (byte)2);
         return false;
      } else if (Villages.getVillage(tilex, tiley, surfaced) != null) {
         return false;
      } else {
         boolean toReturn = true;
         switch(itemTemplateId) {
            case 712:
               for(int x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); ++x) {
                  for(int y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); ++y) {
                     if (!Terraforming.isFlat(x, y, true, 4)) {
                        toReturn = false;
                        break;
                     }

                     if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
                        toReturn = false;
                        break;
                     }
                  }
               }

               if (toReturn) {
                  toReturn = false;
                  int numSmalltrees = 0;

                  for(int x = Zones.safeTileX(tilex - 5); x <= Zones.safeTileX(tilex + 5); ++x) {
                     for(int y = Zones.safeTileY(tiley - 5); y <= Zones.safeTileY(tiley + 5); ++y) {
                        int t = Server.surfaceMesh.getTile(x, y);
                        Tiles.Tile theTile = Tiles.getTile(Tiles.decodeType(t));
                        byte data = Tiles.decodeData(t);
                        if (theTile.isNormalTree()) {
                           if (theTile.getTreeType(data).isFruitTree()) {
                              ++numSmalltrees;
                           }
                        } else if (theTile.isMyceliumTree() && kingdomTemplateId == 3 && theTile.getTreeType(data).isFruitTree()) {
                           ++numSmalltrees;
                        }
                     }
                  }

                  if (numSmalltrees > 3) {
                     toReturn = true;
                  }
               }
               break;
            case 713:
               toReturn = true;

               for(int x = Zones.safeTileX(tilex - 3); x <= Zones.safeTileX(tilex + 3); ++x) {
                  for(int y = Zones.safeTileY(tiley - 3); y <= Zones.safeTileY(tiley + 3); ++y) {
                     if (!Terraforming.isFlat(x, y, true, 4)) {
                        toReturn = false;
                        break;
                     }

                     if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
                        toReturn = false;
                        break;
                     }
                  }
               }

               if (toReturn) {
                  toReturn = false;

                  for(int x = Zones.safeTileX(tilex - 10); x <= Zones.safeTileX(tilex + 10); x += 5) {
                     for(int y = Zones.safeTileY(tiley - 10); y <= Zones.safeTileY(tiley + 10); y += 5) {
                        if (Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) < 0) {
                           toReturn = true;
                           break;
                        }
                     }
                  }
               }
               break;
            case 714:
               toReturn = true;

               for(int x = Zones.safeTileX(tilex - 1); x <= Zones.safeTileX(tilex + 1); ++x) {
                  for(int y = Zones.safeTileY(tiley - 1); y <= Zones.safeTileY(tiley + 1); ++y) {
                     if (!Terraforming.isFlat(x, y, true, 4)) {
                        toReturn = false;
                     }

                     if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
                        toReturn = false;
                     }
                  }
               }
               break;
            case 715:
               for(int x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); ++x) {
                  for(int y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); ++y) {
                     if (!Terraforming.isFlat(x, y, true, 4)) {
                        toReturn = false;
                        break;
                     }

                     if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
                        toReturn = false;
                        break;
                     }
                  }
               }

               if (toReturn) {
                  toReturn = false;
                  if (kingdomTemplateId == 3) {
                     for(int x = Zones.safeTileX(tilex - 5); x <= Zones.safeTileX(tilex + 5); ++x) {
                        for(int y = Zones.safeTileY(tiley - 5); y <= Zones.safeTileY(tiley + 5); ++y) {
                           int t = Server.surfaceMesh.getTile(x, y);
                           byte type = Tiles.decodeType(t);
                           if (Tiles.decodeType(t) == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_MYCELIUM.id || Tiles.getTile(type).isMyceliumTree()) {
                              toReturn = true;
                              break;
                           }
                        }
                     }
                  } else {
                     int t = Server.surfaceMesh.getTile(tilex, tiley);
                     if (Tiles.decodeHeight(t) > 1000) {
                        toReturn = true;
                     }
                  }
               }
               break;
            case 716:
               for(int x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); ++x) {
                  for(int y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); ++y) {
                     if (!Terraforming.isFlat(x, y, true, 4)) {
                        toReturn = false;
                        break;
                     }

                     if (!Tiles.isRoadType(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
                        toReturn = false;
                        break;
                     }
                  }
               }

               if (toReturn) {
                  toReturn = false;
                  if (kingdomTemplateId == 3) {
                     boolean foundMycel = false;
                     boolean foundMarsh = false;

                     for(int x = Zones.safeTileX(tilex - 5); x <= Zones.safeTileX(tilex + 5); ++x) {
                        for(int y = Zones.safeTileY(tiley - 5); y <= Zones.safeTileY(tiley + 5); ++y) {
                           int t = Server.surfaceMesh.getTile(x, y);
                           if (Servers.localServer.PVPSERVER) {
                              byte type = Tiles.decodeType(t);
                              if (type == Tiles.Tile.TILE_MYCELIUM.id || Tiles.getTile(type).isMyceliumTree()) {
                                 foundMycel = true;
                                 continue;
                              }
                           } else if (Tiles.decodeType(t) == Tiles.Tile.TILE_MOSS.id) {
                              foundMycel = true;
                              continue;
                           }

                           if (Tiles.decodeType(t) == Tiles.Tile.TILE_MARSH.id) {
                              foundMarsh = true;
                           }
                        }
                     }

                     if (foundMycel && foundMarsh) {
                        toReturn = true;
                     }
                  } else {
                     int t = Server.surfaceMesh.getTile(tilex, tiley);
                     if (Tiles.decodeHeight(t) > 1000) {
                        toReturn = true;
                     }
                  }
               }
               break;
            case 717:
               toReturn = false;
               if (!surfaced) {
                  toReturn = true;
                  int cornerNorthW = Server.caveMesh.getTile(tilex, tiley);
                  short ceilHeight = (short)(Tiles.decodeData(cornerNorthW) & 255);
                  if (ceilHeight < 50) {
                     performer.sendToLoggers("The NW corner is too low " + ceilHeight, (byte)2);
                     toReturn = false;
                  }

                  int cornerNorthE = Server.caveMesh.getTile(tilex + 1, tiley);
                  short ceilHeightNE = (short)(Tiles.decodeData(Tiles.decodeTileData(cornerNorthE)) & 255);
                  if (ceilHeightNE < 50) {
                     performer.sendToLoggers("The NE corner is too low " + ceilHeightNE, (byte)2);
                     toReturn = false;
                  }

                  int cornerSE = Server.caveMesh.getTile(tilex + 1, tiley + 1);
                  short ceilHeightSE = (short)(Tiles.decodeData(Tiles.decodeTileData(cornerSE)) & 255);
                  if (ceilHeightSE < 50) {
                     performer.sendToLoggers("The SE corner is too low " + ceilHeightSE, (byte)2);
                     toReturn = false;
                  }

                  int cornerSW = Server.caveMesh.getTile(tilex, tiley + 1);
                  short ceilHeightSW = (short)(Tiles.decodeData(Tiles.decodeTileData(cornerSW)) & 255);
                  if (ceilHeightSW < 50) {
                     performer.sendToLoggers("The SW corner is too low " + ceilHeightSW, (byte)2);
                     toReturn = false;
                  }
               } else {
                  performer.sendToLoggers("The pillar is on the surface!", (byte)2);
               }
               break;
            default:
               toReturn = false;
         }

         return toReturn;
      }
   }

   private final void update() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE EPICTARGETITEMS SET PILLARONE=?,PILLARTWO=?,PILLARTHREE=?,OBELISQUEONE=?,OBELISQUETWO=?,OBELISQUETHREE=?,PYLONONE=?,PYLONTWO=?,PYLONTHREE=?,TEMPLEONE=?,TEMPLETWO=?,TEMPLETHREE=?,SHRINEONE=?,SHRINETWO=?,SHRINETHREE=?,SPIRITGATEONE=?,SPIRITGATETWO=?,SPIRITGATETHREE=? WHERE KINGDOM=?"
         );

         for(int x = 0; x <= 17; ++x) {
            ps.setLong(x + 1, this.epicTargetItems[x]);
         }

         ps.setByte(19, this.kingdomId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save epic target status for kingdom " + this.kingdomId, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   final long getRandomTarget() {
      return this.getRandomTarget(0, 0, null);
   }

   private final long getRandomTarget(int attempts, int targetTemplate, @Nullable ArrayList<Long> itemList) {
      long itemFound = -1L;
      if (Servers.localServer.PVPSERVER) {
         int numsExisting = 0;

         for(int x = 0; x < 17; ++x) {
            if (this.epicTargetItems[x] > 0L) {
               ++numsExisting;
            }
         }

         if (numsExisting > 0) {
            for(int x = 0; x < 17; ++x) {
               if (this.epicTargetItems[x] > 0L) {
                  try {
                     Item eti = Items.getItem(this.epicTargetItems[x]);
                     Village v = Villages.getVillage(eti.getTilePos(), eti.isOnSurface());
                     if (v == null) {
                        if (itemFound == -1L) {
                           itemFound = this.epicTargetItems[x];
                        } else if (Server.rand.nextInt(numsExisting) == 0) {
                           itemFound = this.epicTargetItems[x];
                        }
                     } else {
                        logger.info(
                           "Disqualified Epic Mission Target item due to being in village "
                              + v.getName()
                              + ": Name: "
                              + eti.getName()
                              + " | WurmID: "
                              + eti.getWurmId()
                              + " | TileX: "
                              + eti.getTileX()
                              + " | TileY: "
                              + eti.getTileY()
                        );
                     }
                  } catch (NoSuchItemException var21) {
                     logger.warning(
                        "Epic mission item could not be found when loaded, maybe it was wrongfully deleted? WurmID:" + this.epicTargetItems[x] + ". " + var21
                     );
                  }
               }
            }
         }
      } else {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("Entering Freedom Version of Valrei Mission Target Structure selection.");
         }

         Connection dbcon = null;
         PreparedStatement ps1 = null;
         ResultSet rs = null;
         int structureType = Server.rand.nextInt(6);
         int templateId;
         if (targetTemplate > 0) {
            templateId = targetTemplate;
         } else {
            switch(structureType) {
               case 0:
                  templateId = 717;
                  break;
               case 1:
                  templateId = 714;
                  break;
               case 2:
                  templateId = 713;
                  break;
               case 3:
                  templateId = 715;
                  break;
               case 4:
                  templateId = 712;
                  break;
               case 5:
                  templateId = 716;
                  break;
               default:
                  templateId = 713;
            }
         }

         if (logger.isLoggable(Level.FINE)) {
            logger.fine("Selected template with id=" + templateId);
         }

         if (itemList == null) {
            itemList = new ArrayList<>();

            try {
               String dbQueryString = "SELECT WURMID FROM ITEMS WHERE TEMPLATEID=?";
               if (logger.isLoggable(Level.FINER)) {
                  logger.finer("Query String [ SELECT WURMID FROM ITEMS WHERE TEMPLATEID=? ]");
               }

               dbcon = DbConnector.getItemDbCon();
               ps1 = dbcon.prepareStatement("SELECT WURMID FROM ITEMS WHERE TEMPLATEID=?");
               ps1.setInt(1, templateId);
               rs = ps1.executeQuery();

               while(rs.next()) {
                  long currentLong = rs.getLong("WURMID");
                  if (currentLong > 0L) {
                     itemList.add(currentLong);
                  }

                  if (logger.isLoggable(Level.FINEST)) {
                     logger.finest(rs.toString());
                  }
               }
            } catch (SQLException var22) {
               logger.log(Level.WARNING, "Failed to locate mission items with templateid=" + templateId, (Throwable)var22);
            } finally {
               DbUtilities.closeDatabaseObjects(ps1, null);
               DbConnector.returnConnection(dbcon);
            }
         }

         if (itemList.size() <= 0) {
            logger.info("Couldn't find any items with itemtemplate=" + templateId + " failing, the roll.");
            return -1L;
         }

         int randomIndex = Server.rand.nextInt(itemList.size());
         if (itemList.get(randomIndex) == null) {
            logger.warning("WURMID was null for item with templateId=" + templateId);
            return -1L;
         }

         long selectedTarget = itemList.get(randomIndex);

         try {
            Item eti = Items.getItem(selectedTarget);
            Village v = Villages.getVillage(eti.getTilePos(), eti.isOnSurface());
            if (v == null) {
               logger.info("Selected mission target with wurmid=" + selectedTarget);
               return selectedTarget;
            }

            logger.info(
               "Disqualified Epic Mission Target item due to being in village "
                  + v.getName()
                  + ": Name: "
                  + eti.getName()
                  + " | WurmID: "
                  + eti.getWurmId()
                  + " | TileX: "
                  + eti.getTileX()
                  + " | TileY: "
                  + eti.getTileY()
            );
            int ATTEMPT_NUMBER_OF_TIMES = 25;
            if (attempts < 25) {
               logger.fine("Failing roll number " + attempts + "/" + 25 + " and trying again.");
               return this.getRandomTarget(attempts + 1, templateId, itemList);
            }

            logger.info(
               "Failing roll of finding structure with templateID="
                  + templateId
                  + " completely,  could not find any mission structure not in a village in "
                  + 25
                  + " tries."
            );
            return -1L;
         } catch (NoSuchItemException var24) {
         }
      }

      return itemFound;
   }

   final int getNextBuildTarget(int difficulty) {
      difficulty = Math.min(5, difficulty);
      int start = difficulty * 3;
      int templateFound = -1;

      for(int x = start; x < 17; ++x) {
         if (this.epicTargetItems[x] <= 0L) {
            templateFound = x;
            break;
         }
      }

      if (templateFound == -1) {
         for(int x = start; x > 0; --x) {
            if (this.epicTargetItems[x] <= 0L) {
               templateFound = x;
               break;
            }
         }
      }

      if (templateFound > -1) {
         if (templateFound < 3) {
            return 717;
         } else if (templateFound < 6) {
            return 714;
         } else if (templateFound < 9) {
            return 713;
         } else if (templateFound < 12) {
            return 715;
         } else {
            return templateFound < 15 ? 712 : 716;
         }
      } else {
         return -1;
      }
   }

   static {
      getEpicTargets((byte)4);
      getEpicTargets((byte)1);
      getEpicTargets((byte)2);
      getEpicTargets((byte)3);
      getEpicTargets((byte)0);
   }
}

package com.wurmonline.server.highways;

import com.wurmonline.math.Vector2f;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.HighwayConstants;
import com.wurmonline.shared.constants.StructureConstants;
import java.util.HashSet;
import javax.annotation.Nullable;

public class MethodsHighways implements MiscConstants, HighwayConstants {
   private MethodsHighways() {
   }

   public static final boolean middleOfHighway(HighwayPos highwayPos) {
      int tilex = highwayPos.getTilex();
      int tiley = highwayPos.getTiley();
      boolean onSurface = highwayPos.isOnSurface();
      BridgePart currentBridgePart = highwayPos.getBridgePart();
      Floor currentFloor = highwayPos.getFloor();
      if (currentBridgePart != null) {
         return bridgeChecks(tilex, tiley, onSurface, currentBridgePart);
      } else if (currentFloor != null) {
         return floorChecks(tilex, tiley, onSurface, currentFloor);
      } else {
         return !onSurface ? caveChecks(tilex, tiley) : surfaceChecks(tilex, tiley);
      }
   }

   public static final boolean onHighway(Item item) {
      if (!Features.Feature.HIGHWAYS.isEnabled()) {
         return false;
      } else {
         HighwayPos highwayPos = getHighwayPos(item);
         return onHighway(highwayPos);
      }
   }

   public static final boolean onHighway(int cornerx, int cornery, boolean onSurface) {
      if (!Features.Feature.HIGHWAYS.isEnabled()) {
         return false;
      } else {
         HighwayPos highwayPos = getHighwayPos(cornerx, cornery, onSurface);
         return onHighway(highwayPos);
      }
   }

   public static final boolean onWagonerCamp(int cornerx, int cornery, boolean onSurface) {
      if (!Features.Feature.HIGHWAYS.isEnabled()) {
         return false;
      } else {
         HighwayPos highwayPos = getHighwayPos(cornerx, cornery, onSurface);
         return onWagonerCamp(highwayPos);
      }
   }

   public static final boolean onHighway(BridgePart bridgePart) {
      if (!Features.Feature.HIGHWAYS.isEnabled()) {
         return false;
      } else {
         HighwayPos highwayPos = getHighwayPos(bridgePart);
         return onHighway(highwayPos);
      }
   }

   public static final boolean onHighway(Floor floor) {
      if (!Features.Feature.HIGHWAYS.isEnabled()) {
         return false;
      } else {
         HighwayPos highwayPos = getHighwayPos(floor);
         return onHighway(highwayPos);
      }
   }

   public static final boolean onHighway(@Nullable HighwayPos highwaypos) {
      if (highwaypos == null) {
         return false;
      } else if (containsMarker(highwaypos, (byte)0)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)1)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)2)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)4)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)8)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)16)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)32)) {
         return true;
      } else if (containsMarker(highwaypos, (byte)64)) {
         return true;
      } else {
         return containsMarker(highwaypos, (byte)-128);
      }
   }

   public static final boolean onWagonerCamp(@Nullable HighwayPos highwaypos) {
      if (highwaypos == null) {
         return false;
      } else if (containsWagonerWaystone(highwaypos, (byte)0)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)1)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)2)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)4)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)8)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)16)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)32)) {
         return true;
      } else if (containsWagonerWaystone(highwaypos, (byte)64)) {
         return true;
      } else {
         return containsWagonerWaystone(highwaypos, (byte)-128);
      }
   }

   private static final boolean caveChecks(int tilex, int tiley) {
      MeshIO caveMesh = Server.caveMesh;
      int currentEncodedTile = caveMesh.getTile(tilex, tiley);
      byte currentType = Tiles.decodeType(currentEncodedTile);
      boolean onSurface = false;
      if (currentType != Tiles.Tile.TILE_CAVE_EXIT.id) {
         boolean foundBridge = false;
         if (!Tiles.isReinforcedFloor(currentType) && currentType != Tiles.Tile.TILE_CAVE_EXIT.id) {
            return false;
         } else {
            int northEncodedTile = caveMesh.getTile(tilex, tiley - 1);
            byte northType = Tiles.decodeType(northEncodedTile);
            BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, false);
            if (bridgePartNorth != null) {
               if (bridgePartNorth.getSouthExit() == 0) {
                  foundBridge = true;
               }
            } else if (!Tiles.isReinforcedFloor(northType) && northType != Tiles.Tile.TILE_CAVE_EXIT.id) {
               return false;
            }

            int westEncodedTile = caveMesh.getTile(tilex - 1, tiley);
            byte westType = Tiles.decodeType(westEncodedTile);
            BridgePart bridgePartWest = Zones.getBridgePartFor(tilex, tiley - 1, false);
            if (bridgePartWest != null) {
               if (bridgePartWest.getEastExit() == 0) {
                  foundBridge = true;
               }
            } else if (!Tiles.isReinforcedFloor(westType) && westType != Tiles.Tile.TILE_CAVE_EXIT.id) {
               return false;
            }

            if (foundBridge) {
               BridgePart bridgePart = Zones.getBridgePartFor(tilex - 1, tiley - 1, false);
               if (bridgePart == null) {
                  return false;
               }
            } else {
               int northWestEncodedTile = caveMesh.getTile(tilex - 1, tiley - 1);
               byte northWestType = Tiles.decodeType(northWestEncodedTile);
               if (!Tiles.isReinforcedFloor(northWestType) && northWestType != Tiles.Tile.TILE_CAVE_EXIT.id) {
                  return false;
               }
            }

            return true;
         }
      } else {
         for(int x = -1; x <= 0; ++x) {
            for(int y = -1; y <= 0; ++y) {
               int encodedTile = caveMesh.getTile(tilex + x, tiley + y);
               byte type = Tiles.decodeType(encodedTile);
               if (!Tiles.isReinforcedFloor(type) && !Tiles.isRoadType(type) && type != Tiles.Tile.TILE_CAVE_EXIT.id) {
                  if (!Tiles.isSolidCave(type)) {
                     return false;
                  }

                  int surfaceTile = Server.surfaceMesh.getTile(tilex + x, tiley + y);
                  byte surfaceType = Tiles.decodeType(surfaceTile);
                  if (!Tiles.isRoadType(surfaceType)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   private static final boolean surfaceChecks(int tilex, int tiley) {
      boolean foundBridge = false;
      boolean onSurface = true;
      int currentEncodedTile = Server.surfaceMesh.getTile(tilex, tiley);
      byte currentType = Tiles.decodeType(currentEncodedTile);
      if (!Tiles.isRoadType(currentType) && currentType != Tiles.Tile.TILE_HOLE.id) {
         return false;
      } else {
         int northEncodedTile = Server.surfaceMesh.getTile(tilex, tiley - 1);
         byte northType = Tiles.decodeType(northEncodedTile);
         if (!Tiles.isRoadType(northType) && northType != Tiles.Tile.TILE_HOLE.id) {
            BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley - 1, true);
            if (bridgePart == null) {
               return false;
            }

            if (bridgePart.getSouthExit() == 0) {
               foundBridge = true;
            }
         }

         int westEncodedTile = Server.surfaceMesh.getTile(tilex - 1, tiley);
         byte westType = Tiles.decodeType(westEncodedTile);
         if (!Tiles.isRoadType(westType) && westType != Tiles.Tile.TILE_HOLE.id) {
            BridgePart bridgePart = Zones.getBridgePartFor(tilex - 1, tiley, true);
            if (bridgePart == null) {
               return false;
            }

            if (bridgePart.getEastExit() == 0) {
               foundBridge = true;
            }
         }

         if (foundBridge) {
            BridgePart bridgePart = Zones.getBridgePartFor(tilex - 1, tiley - 1, true);
            if (bridgePart == null) {
               return false;
            }
         } else {
            int northWestEncodedTile = Server.surfaceMesh.getTile(tilex - 1, tiley - 1);
            byte northWestType = Tiles.decodeType(northWestEncodedTile);
            if (!Tiles.isRoadType(northWestType) && northWestType != Tiles.Tile.TILE_HOLE.id) {
               return false;
            }
         }

         return true;
      }
   }

   private static final boolean bridgeChecks(int tilex, int tiley, boolean onSurface, BridgePart currentBridgePart) {
      if (currentBridgePart.hasNorthExit()) {
         if (currentBridgePart.getNorthExit() == 0) {
            MeshIO mesh = onSurface ? Server.surfaceMesh : Server.caveMesh;
            if (!Tiles.isRoadType(mesh.getTile(tilex, tiley - 1))) {
               return false;
            } else if (!Tiles.isRoadType(mesh.getTile(tilex - 1, tiley - 1))) {
               return false;
            } else {
               BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
               return bridgePartWest != null && bridgePartWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED;
            }
         } else {
            Floor floorNorth = Zones.getFloor(tilex, tiley - 1, onSurface, currentBridgePart.getNorthExitFloorLevel());
            if (floorNorth != null && floorNorth.getFloorState() == StructureConstants.FloorState.COMPLETED) {
               Floor floorNorthWest = Zones.getFloor(tilex - 1, tiley - 1, onSurface, currentBridgePart.getNorthExitFloorLevel());
               if (floorNorthWest != null && floorNorthWest.getFloorState() == StructureConstants.FloorState.COMPLETED) {
                  BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
                  return bridgePartWest != null && bridgePartWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      } else if (currentBridgePart.hasWestExit()) {
         if (currentBridgePart.getWestExit() == 0) {
            MeshIO mesh = onSurface ? Server.surfaceMesh : Server.caveMesh;
            BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
            if (bridgePartNorth == null || bridgePartNorth.getBridgePartState() != BridgeConstants.BridgeState.COMPLETED) {
               return false;
            } else if (!Tiles.isRoadType(mesh.getTile(tilex - 1, tiley - 1))) {
               return false;
            } else {
               return Tiles.isRoadType(mesh.getTile(tilex - 1, tiley));
            }
         } else {
            BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
            if (bridgePartNorth != null && bridgePartNorth.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED) {
               Floor floorNorthWest = Zones.getFloor(tilex - 1, tiley - 1, onSurface, currentBridgePart.getWestExitFloorLevel());
               if (floorNorthWest != null && floorNorthWest.getFloorState() == StructureConstants.FloorState.COMPLETED) {
                  Floor floorWest = Zones.getFloor(tilex - 1, tiley, onSurface, currentBridgePart.getWestExitFloorLevel());
                  return floorWest != null && floorWest.getFloorState() == StructureConstants.FloorState.COMPLETED;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      } else {
         BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
         if (bridgePartNorth != null && bridgePartNorth.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED) {
            BridgePart bridgePartNorthWest = Zones.getBridgePartFor(tilex - 1, tiley - 1, onSurface);
            if (bridgePartNorthWest != null && bridgePartNorthWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED) {
               BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
               return bridgePartWest != null && bridgePartWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private static final boolean floorChecks(int tilex, int tiley, boolean onSurface, Floor currentFloor) {
      Floor floorNorth = Zones.getFloor(tilex, tiley - 1, onSurface, currentFloor.getFloorLevel());
      if (floorNorth == null) {
         BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
         if (bridgePartNorth == null || bridgePartNorth.getSouthExitFloorLevel() != currentFloor.getFloorLevel()) {
            return false;
         }

         BridgePart bridgePartNorthWest = Zones.getBridgePartFor(tilex - 1, tiley - 1, onSurface);
         if (bridgePartNorthWest == null || bridgePartNorthWest.getSouthExitFloorLevel() != currentFloor.getFloorLevel()) {
            return false;
         }
      }

      Floor floorWest = Zones.getFloor(tilex - 1, tiley, onSurface, currentFloor.getFloorLevel());
      if (floorWest == null) {
         BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
         if (bridgePartWest == null || bridgePartWest.getEastExitFloorLevel() != currentFloor.getFloorLevel()) {
            return false;
         }

         BridgePart bridgePartNorthWest = Zones.getBridgePartFor(tilex - 1, tiley - 1, onSurface);
         if (bridgePartNorthWest == null || bridgePartNorthWest.getEastExitFloorLevel() != currentFloor.getFloorLevel()) {
            return false;
         }
      }

      if (floorNorth != null && floorWest != null) {
         Floor floorNorthWest = Zones.getFloor(tilex - 1, tiley - 1, onSurface, currentFloor.getFloorLevel());
         if (floorNorthWest == null) {
            return false;
         }
      }

      return true;
   }

   public static final boolean hasLink(byte dirs, byte linkdir) {
      return (dirs & linkdir) != 0;
   }

   public static final byte getPossibleLinksFrom(Item marker) {
      HighwayPos highwayPos = getHighwayPosFromMarker(marker);
      return getPossibleLinksFrom(highwayPos, marker, marker.getAuxData());
   }

   public static final byte getPossibleLinksFrom(HighwayPos highwayPos, Item marker) {
      return getPossibleLinksFrom(highwayPos, marker, (byte)0);
   }

   private static final byte getPossibleLinksFrom(HighwayPos highwayPos, Item marker, byte currentLinks) {
      byte possibles = (byte)(~currentLinks & 0xFF);
      possibles = checkLink(possibles, highwayPos, (byte)1);
      possibles = checkLink(possibles, highwayPos, (byte)2);
      possibles = checkLink(possibles, highwayPos, (byte)4);
      possibles = checkLink(possibles, highwayPos, (byte)8);
      possibles = checkLink(possibles, highwayPos, (byte)16);
      possibles = checkLink(possibles, highwayPos, (byte)32);
      possibles = checkLink(possibles, highwayPos, (byte)64);
      possibles = checkLink(possibles, highwayPos, (byte)-128);
      if (marker.getTemplateId() == 1114 && numberOfSetBits(possibles) > 2) {
         int lower = possibles & 15;
         int upper = possibles & 240;
         int loup = lower << 4;
         int uplo = upper >>> 4;
         int upnew = upper & loup;
         int lonew = lower & uplo;
         byte poss = (byte)(upnew | lonew);
         if (numberOfSetBits(poss) == 2) {
            possibles = poss;
         }
      }

      return possibles;
   }

   private static final byte checkLink(byte possibles, HighwayPos currentHighwayPos, byte checkdir) {
      if (hasLink(possibles, checkdir)) {
         HighwayPos highwayPos = getNewHighwayPosLinked(currentHighwayPos, checkdir);
         if (highwayPos == null) {
            return (byte)(possibles & ~checkdir);
         }

         Item marker = getMarker(highwayPos);
         if (marker == null) {
            return (byte)(possibles & ~checkdir);
         }

         if (hasLink(getOppositedir(checkdir), marker.getAuxData())) {
            return (byte)(possibles & ~checkdir);
         }

         if (marker.getTemplateId() == 1114 && numberOfSetBits(marker.getAuxData()) > 1) {
            return (byte)(possibles & ~checkdir);
         }
      }

      return possibles;
   }

   public static final void autoLink(Item newMarker, byte possibleLinks) {
      HighwayPos currentHighwayPos = getHighwayPosFromMarker(newMarker);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)1, (byte)16);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)2, (byte)32);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)4, (byte)64);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)8, (byte)-128);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)16, (byte)1);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)32, (byte)2);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)64, (byte)4);
      addLink(newMarker, currentHighwayPos, possibleLinks, (byte)-128, (byte)8);
      Routes.checkForNewRoutes(newMarker);
   }

   private static final void addLink(Item newMarker, HighwayPos currentHighwayPos, byte possibles, byte linkdir, byte reversedir) {
      if (hasLink(possibles, linkdir)) {
         Item linkMarker = getMarker(currentHighwayPos, linkdir);
         if (linkMarker != null) {
            newMarker.setAuxData((byte)(newMarker.getAuxData() | linkdir));
            linkMarker.setAuxData((byte)(linkMarker.getAuxData() | reversedir));
            newMarker.updateModelNameOnGroundItem();
            linkMarker.updateModelNameOnGroundItem();
         }
      }
   }

   public static final void removeLinksTo(Item fromMarker) {
      Item[] markers = Routes.getRouteMarkers(fromMarker);
      HighwayPos currentHighwayPos = getHighwayPosFromMarker(fromMarker);
      removeLink(currentHighwayPos, (byte)1, (byte)16);
      removeLink(currentHighwayPos, (byte)2, (byte)32);
      removeLink(currentHighwayPos, (byte)4, (byte)64);
      removeLink(currentHighwayPos, (byte)8, (byte)-128);
      removeLink(currentHighwayPos, (byte)16, (byte)1);
      removeLink(currentHighwayPos, (byte)32, (byte)2);
      removeLink(currentHighwayPos, (byte)64, (byte)4);
      removeLink(currentHighwayPos, (byte)-128, (byte)8);
      fromMarker.setAuxData((byte)0);
      Items.removeMarker(fromMarker);
      fromMarker.updateModelNameOnGroundItem();

      for(Item marker : markers) {
         marker.updateModelNameOnGroundItem();
      }
   }

   private static final void removeLink(HighwayPos currentHighwayPos, byte fromdir, byte linkdir) {
      Item marker = getMarker(currentHighwayPos, fromdir);
      if (marker != null && hasLink(marker.getAuxData(), linkdir)) {
         marker.setAuxData((byte)(marker.getAuxData() & ~linkdir));
         marker.updateModelNameOnGroundItem();
      }
   }

   @Nullable
   public static final Item getMarker(Item marker, byte dir) {
      HighwayPos currentHighwayPos = getHighwayPosFromMarker(marker);
      switch(dir) {
         case -128:
            return getMarker(currentHighwayPos, (byte)-128);
         case 1:
            return getMarker(currentHighwayPos, (byte)1);
         case 2:
            return getMarker(currentHighwayPos, (byte)2);
         case 4:
            return getMarker(currentHighwayPos, (byte)4);
         case 8:
            return getMarker(currentHighwayPos, (byte)8);
         case 16:
            return getMarker(currentHighwayPos, (byte)16);
         case 32:
            return getMarker(currentHighwayPos, (byte)32);
         case 64:
            return getMarker(currentHighwayPos, (byte)64);
         default:
            return null;
      }
   }

   public static final boolean viewProtection(Creature performer, Item marker) {
      HighwayPos highwayPos = getHighwayPosFromMarker(marker);
      return sendShowProtection(performer, marker, highwayPos);
   }

   public static final boolean viewProtection(Creature performer, HighwayPos highwayPos, Item marker) {
      return sendShowProtection(performer, marker, highwayPos);
   }

   public static final boolean viewLinks(Creature performer, Item marker) {
      HighwayPos highwayPos = getHighwayPosFromMarker(marker);
      return viewLinks(performer, highwayPos, marker, (byte)1, marker.getAuxData());
   }

   public static final boolean viewLinks(Creature performer, HighwayPos highwayPos, Item marker) {
      byte links = getPossibleLinksFrom(highwayPos, marker);
      return viewLinks(performer, highwayPos, marker, (byte)0, links);
   }

   public static final boolean viewLinks(Creature performer, HighwayPos currentHighwayPos, Item marker, byte linktype, byte links) {
      String linktypeString = linktype == 1 ? "Links" : "Possible links";
      boolean showing = false;
      if (links == 0) {
         performer.getCommunicator().sendNormalServerMessage("There are no " + linktypeString.toLowerCase() + " from there!");
      } else {
         showing = sendShowLinks(performer, currentHighwayPos, marker, linktype, links);
         if (Servers.isThisATestServer()) {
            int count = 0;
            int todo = numberOfSetBits(links);
            StringBuilder buf = new StringBuilder();
            buf.append(linktypeString + " are: ");
            if (hasLink(links, (byte)1) && containsMarker(currentHighwayPos, (byte)1)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)1));
            }

            if (hasLink(links, (byte)2) && containsMarker(currentHighwayPos, (byte)2)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)2));
            }

            if (hasLink(links, (byte)4) && containsMarker(currentHighwayPos, (byte)4)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)4));
            }

            if (hasLink(links, (byte)8) && containsMarker(currentHighwayPos, (byte)8)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)8));
            }

            if (hasLink(links, (byte)16) && containsMarker(currentHighwayPos, (byte)16)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)16));
            }

            if (hasLink(links, (byte)32) && containsMarker(currentHighwayPos, (byte)32)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)32));
            }

            if (hasLink(links, (byte)64) && containsMarker(currentHighwayPos, (byte)64)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)64));
            }

            if (hasLink(links, (byte)-128) && containsMarker(currentHighwayPos, (byte)-128)) {
               if (count++ > 0) {
                  if (count == todo) {
                     buf.append(" and ");
                  } else {
                     buf.append(", ");
                  }
               }

               buf.append(getLinkDirString((byte)-128));
            }

            performer.getCommunicator().sendNormalServerMessage("test only:" + buf.toString());
         }
      }

      return showing;
   }

   private static final boolean sendShowLinks(Creature performer, HighwayPos currentHighwayPos, Item marker, byte linktype, byte links) {
      boolean markerType = marker.getTemplateId() == 1112;
      byte[] glows = new byte[]{
         getLinkGlow(linktype, marker, links, (byte)1),
         getLinkGlow(linktype, marker, links, (byte)2),
         getLinkGlow(linktype, marker, links, (byte)4),
         getLinkGlow(linktype, marker, links, (byte)8),
         getLinkGlow(linktype, marker, links, (byte)16),
         getLinkGlow(linktype, marker, links, (byte)32),
         getLinkGlow(linktype, marker, links, (byte)64),
         getLinkGlow(linktype, marker, links, (byte)-128)
      };
      return performer.getCommunicator().sendShowLinks(markerType, currentHighwayPos, glows);
   }

   private static final byte getLinkGlow(byte linktype, Item marker, byte links, byte link) {
      if (hasLink(links, link)) {
         if (linktype == 1) {
            if (marker.getTemplateId() == 1112) {
               Node node = Routes.getNode(marker.getWurmId());
               if (node != null) {
                  Route route = node.getRoute(link);
                  if (route != null) {
                     return 3;
                  }
               }

               return 1;
            } else {
               int count = numberOfSetBits(marker.getAuxData());
               if (count == 2) {
                  return 3;
               } else {
                  return (byte)(count == 1 ? 2 : 1);
               }
            }
         } else {
            if (marker.getTemplateId() == 1112) {
            }

            return 2;
         }
      } else {
         return -1;
      }
   }

   public static final boolean sendShowProtection(Creature performer, Item marker, HighwayPos currentHighwayPos) {
      StringBuilder buf = new StringBuilder();
      buf.append("Protected: center");
      boolean markerType = marker.getTemplateId() == 1112;
      HashSet<HighwayPos> protectedTiles = new HashSet<>();
      HighwayPos highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)1);
      if (highwayPos != null) {
         protectedTiles.add(highwayPos);
         buf.append(", north");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)2);
      if (highwayPos != null && isPaved(highwayPos)) {
         protectedTiles.add(highwayPos);
         buf.append(", northeast");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)4);
      if (highwayPos != null && isPaved(highwayPos)) {
         protectedTiles.add(highwayPos);
         buf.append(", east");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)8);
      if (highwayPos != null && isPaved(highwayPos)) {
         protectedTiles.add(highwayPos);
         buf.append(", southeast");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)16);
      if (highwayPos != null && isPaved(highwayPos)) {
         protectedTiles.add(highwayPos);
         buf.append(", south");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)32);
      if (highwayPos != null && isPaved(highwayPos)) {
         protectedTiles.add(highwayPos);
         buf.append(", southwest");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)64);
      if (highwayPos != null) {
         protectedTiles.add(highwayPos);
         buf.append(", west");
      }

      highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)-128);
      if (highwayPos != null) {
         protectedTiles.add(highwayPos);
         buf.append(", northwest");
      }

      HighwayPos[] protectedHPs = protectedTiles.toArray(new HighwayPos[protectedTiles.size()]);
      if (Servers.isThisATestServer()) {
         int pos = buf.lastIndexOf(",");
         if (pos > 0) {
            buf.replace(pos, pos + 1, " and");
         }

         performer.getCommunicator().sendNormalServerMessage("test only:" + buf.toString());
      }

      return performer.getCommunicator().sendShowProtection(markerType, currentHighwayPos, protectedHPs);
   }

   private static boolean isPaved(HighwayPos highwayPos) {
      if (highwayPos.getBridgeId() != -10L) {
         return true;
      } else if (highwayPos.getFloorLevel() > 0) {
         return true;
      } else {
         if (highwayPos.isOnSurface()) {
            int surfaceTile = Server.surfaceMesh.getTile(highwayPos.getTilex(), highwayPos.getTiley());
            byte surfaceType = Tiles.decodeType(surfaceTile);
            if (!Tiles.isRoadType(surfaceType)) {
               return false;
            }
         } else {
            int caveTile = Server.caveMesh.getTile(highwayPos.getTilex(), highwayPos.getTiley());
            byte caveType = Tiles.decodeType(caveTile);
            if (!Tiles.isReinforcedFloor(caveType) && !Tiles.isRoadType(caveType) && caveType != Tiles.Tile.TILE_CAVE_EXIT.id) {
               return false;
            }
         }

         return true;
      }
   }

   public static final String getLinkAsString(byte links) {
      int count = 0;
      int todo = numberOfSetBits(links);
      StringBuilder buf = new StringBuilder();
      if (hasLink(links, (byte)1)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)1));
      }

      if (hasLink(links, (byte)2)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)2));
      }

      if (hasLink(links, (byte)4)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)4));
      }

      if (hasLink(links, (byte)8)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)8));
      }

      if (hasLink(links, (byte)16)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)16));
      }

      if (hasLink(links, (byte)32)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)32));
      }

      if (hasLink(links, (byte)64)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)64));
      }

      if (hasLink(links, (byte)-128)) {
         if (count++ > 0) {
            if (count == todo) {
               buf.append(" and ");
            } else {
               buf.append(", ");
            }
         }

         buf.append(getLinkDirString((byte)-128));
      }

      if (count == 0) {
         buf.append("none");
      }

      return buf.toString();
   }

   public static final boolean containsWagonerWaystone(HighwayPos highwayPos, byte fromdir) {
      Item marker = getMarker(highwayPos, fromdir);
      if (marker != null && marker.getTemplateId() != 1114) {
         return marker.getData() != -1L;
      } else {
         return false;
      }
   }

   public static final boolean containsMarker(HighwayPos highwayPos, byte fromdir) {
      return getMarker(highwayPos, fromdir) != null;
   }

   @Nullable
   public static final Item getMarker(@Nullable HighwayPos currentHighwayPos, byte fromdir) {
      if (currentHighwayPos == null) {
         return null;
      } else if (fromdir == 0) {
         return getMarker(currentHighwayPos);
      } else {
         HighwayPos highwayPos = getNewHighwayPosLinked(currentHighwayPos, fromdir);
         return highwayPos != null ? getMarker(highwayPos) : null;
      }
   }

   @Nullable
   public static final Item getMarker(HighwayPos highwaypos) {
      return highwaypos == null
         ? null
         : Items.getMarker(highwaypos.getTilex(), highwaypos.getTiley(), highwaypos.isOnSurface(), highwaypos.getFloorLevel(), highwaypos.getBridgeId());
   }

   @Nullable
   public static final Item getMarker(Creature creature) {
      return Items.getMarker(creature.getTileX(), creature.getTileY(), creature.isOnSurface(), creature.getFloorLevel(), creature.getBridgeId());
   }

   @Nullable
   public static final HighwayPos getHighwayPos(Item marker) {
      int tilex = marker.getTileX();
      int tiley = marker.getTileY();
      boolean onSurface = marker.isOnSurface();
      if (marker.getBridgeId() != -10L) {
         return new HighwayPos(tilex, tiley, onSurface, Zones.getBridgePartFor(tilex, tiley, onSurface), null);
      } else {
         return marker.getFloorLevel() > 0
            ? new HighwayPos(tilex, tiley, onSurface, null, Zones.getFloor(tilex, tiley, onSurface, marker.getFloorLevel()))
            : new HighwayPos(tilex, tiley, onSurface, null, null);
      }
   }

   @Nullable
   public static final HighwayPos getHighwayPos(BridgePart bridgePart) {
      int tilex = bridgePart.getTileX();
      int tiley = bridgePart.getTileY();
      boolean onSurface = bridgePart.isOnSurface();
      return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
   }

   @Nullable
   public static final HighwayPos getHighwayPos(Floor floor) {
      int tilex = floor.getTileX();
      int tiley = floor.getTileY();
      boolean onSurface = floor.isOnSurface();
      return new HighwayPos(tilex, tiley, onSurface, null, floor);
   }

   @Nullable
   public static final HighwayPos getNewHighwayPosLinked(@Nullable HighwayPos currentHighwayPos, byte todir) {
      if (currentHighwayPos == null) {
         return null;
      } else {
         int tilex = currentHighwayPos.getTilex();
         int tiley = currentHighwayPos.getTiley();
         switch(todir) {
            case -128:
               --tiley;
               --tilex;
               break;
            case 1:
               --tiley;
               break;
            case 2:
               --tiley;
               ++tilex;
               break;
            case 4:
               ++tilex;
               break;
            case 8:
               ++tiley;
               ++tilex;
               break;
            case 16:
               ++tiley;
               break;
            case 32:
               ++tiley;
               --tilex;
               break;
            case 64:
               --tilex;
         }

         boolean onSurface = currentHighwayPos.isOnSurface();
         if (currentHighwayPos.getBridgePart() != null) {
            return getNewHighwayPosFromBridge(tilex, tiley, onSurface, currentHighwayPos.getBridgePart(), todir);
         } else if (currentHighwayPos.getFloor() != null) {
            return getNewHighwayPosFromFloor(tilex, tiley, onSurface, currentHighwayPos.getFloor(), todir);
         } else {
            if (onSurface) {
               int encodedtile = Server.surfaceMesh.getTile(tilex, tiley);
               byte type = Tiles.decodeType(encodedtile);
               if (type == Tiles.Tile.TILE_HOLE.id) {
                  return new HighwayPos(tilex, tiley, false, null, null);
               }

               BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
               if (bridgePart != null) {
                  if (bridgePart.getSouthExit() == 0 && (todir == -128 || todir == 1 || todir == 2)) {
                     return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                  }

                  if (bridgePart.getWestExit() == 0 && (todir == 2 || todir == 4 || todir == 8)) {
                     return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                  }

                  if (bridgePart.getNorthExit() == 0 && (todir == 8 || todir == 16 || todir == 32)) {
                     return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                  }

                  if (bridgePart.getEastExit() == 0 && (todir == 32 || todir == 64 || todir == -128)) {
                     return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                  }
               }
            } else {
               int encodedCurrentTile = Server.caveMesh.getTile(currentHighwayPos.getTilex(), currentHighwayPos.getTiley());
               byte currentType = Tiles.decodeType(encodedCurrentTile);
               int encodedtile = Server.caveMesh.getTile(tilex, tiley);
               byte type = Tiles.decodeType(encodedtile);
               if (currentType == Tiles.Tile.TILE_CAVE_EXIT.id) {
                  if (Tiles.isSolidCave(type)) {
                     return new HighwayPos(tilex, tiley, true, null, null);
                  }
               } else {
                  if (Tiles.isSolidCave(type)) {
                     return null;
                  }

                  BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
                  if (bridgePart != null) {
                     if (bridgePart.getSouthExit() == 0 && (todir == -128 || todir == 1 || todir == 2)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                     }

                     if (bridgePart.getWestExit() == 0 && (todir == 2 || todir == 4 || todir == 8)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                     }

                     if (bridgePart.getNorthExit() == 0 && (todir == 8 || todir == 16 || todir == 32)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                     }

                     if (bridgePart.getEastExit() == 0 && (todir == 32 || todir == 64 || todir == -128)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                     }
                  }
               }
            }

            return new HighwayPos(tilex, tiley, onSurface, null, null);
         }
      }
   }

   @Nullable
   public static final HighwayPos getHighwayPos(int cornerx, int cornery, boolean onSurface) {
      if (onSurface) {
         int encodedTile = Server.surfaceMesh.getTile(cornerx, cornery);
         byte type = Tiles.decodeType(encodedTile);
         if (type == Tiles.Tile.TILE_HOLE.id) {
            return new HighwayPos(cornerx, cornery, false, null, null);
         }
      }

      BridgePart bridgePart = Zones.getBridgePartFor(cornerx, cornery, onSurface);
      return bridgePart == null
            || bridgePart.getNorthExit() != 0 && bridgePart.getEastExit() != 0 && bridgePart.getSouthExit() != 0 && bridgePart.getWestExit() != 0
         ? new HighwayPos(cornerx, cornery, onSurface, null, null)
         : new HighwayPos(cornerx, cornery, onSurface, bridgePart, null);
   }

   @Nullable
   public static final HighwayPos getHighwayPos(int cornerx, int cornery, boolean onSurface, int heightOffset) {
      if (heightOffset == 0) {
         return getHighwayPos(cornerx, cornery, onSurface);
      } else {
         Floor[] floors = Zones.getFloorsAtTile(cornerx, cornery, heightOffset, heightOffset, onSurface);
         if (floors != null && floors.length == 1) {
            return getHighwayPos(floors[0]);
         } else {
            if (heightOffset > 0) {
               BridgePart bridgePart = Zones.getBridgePartFor(cornerx, cornery, onSurface);
               if (bridgePart != null) {
                  return getHighwayPos(bridgePart);
               }
            }

            return null;
         }
      }
   }

   @Nullable
   public static final HighwayPos getHighwayPos(Creature creature) {
      int tilex = creature.getTileX();
      int tiley = creature.getTileY();
      boolean onSurface = creature.isOnSurface();
      if (creature.getBridgeId() != -10L) {
         BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
         if (bridgePart != null) {
            return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
         }
      }

      if (creature.getFloorLevel() > 0) {
         Floor floor = Zones.getFloor(tilex, tiley, onSurface, creature.getFloorLevel());
         if (floor != null) {
            return new HighwayPos(tilex, tiley, onSurface, null, floor);
         }
      }

      return new HighwayPos(tilex, tiley, onSurface, null, null);
   }

   @Nullable
   public static final HighwayPos getNewHighwayPosCorner(
      Creature performer, int currentTilex, int currentTiley, boolean onSurface, @Nullable BridgePart currentBridgePart, @Nullable Floor currentFloor
   ) {
      Vector2f pos = performer.getPos2f();
      int posTilex = CoordUtils.WorldToTile(pos.x + 2.0F);
      int posTiley = CoordUtils.WorldToTile(pos.y + 2.0F);
      if (posTilex == currentTilex && posTiley == currentTiley) {
         return new HighwayPos(currentTilex, currentTiley, onSurface, currentBridgePart, currentFloor);
      } else {
         byte fromdir = 0;
         if (posTilex == currentTilex && posTiley < currentTiley) {
            fromdir = 1;
         } else if (posTilex > currentTilex && posTiley < currentTiley) {
            fromdir = 2;
         } else if (posTilex > currentTilex && posTiley == currentTiley) {
            fromdir = 4;
         } else if (posTilex > currentTilex && posTiley > currentTiley) {
            fromdir = 8;
         } else if (posTilex == currentTilex && posTiley > currentTiley) {
            fromdir = 16;
         } else if (posTilex < currentTilex && posTiley > currentTiley) {
            fromdir = 32;
         } else if (posTilex < currentTilex && posTiley == currentTiley) {
            fromdir = 64;
         } else if (posTilex < currentTilex && posTiley < currentTiley) {
            fromdir = -128;
         }

         if (currentBridgePart != null) {
            return getNewHighwayPosFromBridge(posTilex, posTiley, onSurface, currentBridgePart, fromdir);
         } else if (currentFloor != null) {
            return getNewHighwayPosFromFloor(posTilex, posTiley, onSurface, currentFloor, fromdir);
         } else {
            if (onSurface) {
               int encodedtile = Server.surfaceMesh.getTile(posTilex, posTiley);
               byte type = Tiles.decodeType(encodedtile);
               if (type == Tiles.Tile.TILE_HOLE.id) {
                  return new HighwayPos(posTilex, posTiley, false, null, null);
               }

               BridgePart bridgePart = Zones.getBridgePartFor(posTilex, posTiley, onSurface);
               if (bridgePart != null) {
                  if (bridgePart.getSouthExit() == 0 && (fromdir == -128 || fromdir == 1 || fromdir == 2)) {
                     return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                  }

                  if (bridgePart.getWestExit() == 0 && (fromdir == 2 || fromdir == 4 || fromdir == 8)) {
                     return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                  }

                  if (bridgePart.getNorthExit() == 0 && (fromdir == 8 || fromdir == 16 || fromdir == 32)) {
                     return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                  }

                  if (bridgePart.getEastExit() == 0 && (fromdir == 32 || fromdir == 64 || fromdir == -128)) {
                     return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                  }
               }
            } else {
               int encodedtile = Server.caveMesh.getTile(posTilex, posTiley);
               byte type = Tiles.decodeType(encodedtile);
               if (Tiles.isSolidCave(type)) {
                  return new HighwayPos(posTilex, posTiley, true, null, null);
               }
            }

            return new HighwayPos(posTilex, posTiley, onSurface, null, null);
         }
      }
   }

   @Nullable
   private static final HighwayPos getNewHighwayPosFromBridge(int tilex, int tiley, boolean onSurface, BridgePart currentBridgePart, byte fromdir) {
      if (!currentBridgePart.hasNorthExit() || fromdir != -128 && fromdir != 1 && fromdir != 2) {
         if (!currentBridgePart.hasEastExit() || fromdir != 2 && fromdir != 4 && fromdir != 32 && fromdir != 2) {
            if (!currentBridgePart.hasSouthExit() || fromdir != 8 && fromdir != 16 && fromdir != 32) {
               if (!currentBridgePart.hasWestExit() || fromdir != 32 && fromdir != 64 && fromdir != -128 && fromdir != 2) {
                  BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
                  return bridgePart != null ? new HighwayPos(tilex, tiley, onSurface, bridgePart, null) : null;
               } else if (currentBridgePart.hasHouseWestExit()) {
                  Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getWestExitFloorLevel());
                  return floor == null ? null : new HighwayPos(tilex, tiley, onSurface, null, floor);
               } else {
                  return new HighwayPos(tilex, tiley, onSurface, null, null);
               }
            } else if (currentBridgePart.hasHouseSouthExit()) {
               Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getSouthExitFloorLevel());
               return floor == null ? null : new HighwayPos(tilex, tiley, onSurface, null, floor);
            } else {
               return new HighwayPos(tilex, tiley, onSurface, null, null);
            }
         } else if (currentBridgePart.hasHouseEastExit()) {
            Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getEastExitFloorLevel());
            return floor == null ? null : new HighwayPos(tilex, tiley, onSurface, null, floor);
         } else {
            return new HighwayPos(tilex, tiley, onSurface, null, null);
         }
      } else if (currentBridgePart.hasHouseNorthExit()) {
         Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getNorthExitFloorLevel());
         return floor == null ? null : new HighwayPos(tilex, tiley, onSurface, null, floor);
      } else {
         return new HighwayPos(tilex, tiley, onSurface, null, null);
      }
   }

   @Nullable
   private static final HighwayPos getNewHighwayPosFromFloor(int tilex, int tiley, boolean onSurface, Floor currentFloor, byte fromdir) {
      Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentFloor.getFloorLevel());
      if (floor != null) {
         return new HighwayPos(tilex, tiley, onSurface, null, floor);
      } else {
         BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
         if (bridgePart != null) {
            if (bridgePart.getSouthExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == -128 || fromdir == 1 || fromdir == 2)) {
               return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }

            if (bridgePart.getWestExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == 2 || fromdir == 4 || fromdir == 8)) {
               return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }

            if (bridgePart.getNorthExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == 8 || fromdir == 16 || fromdir == 32)) {
               return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }

            if (bridgePart.getEastExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == 32 || fromdir == 64 || fromdir == -128)) {
               return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }
         }

         return null;
      }
   }

   @Nullable
   private static final HighwayPos getHighwayPosFromMarker(Item marker) {
      int tilex = marker.getTileX();
      int tiley = marker.getTileY();
      boolean onSurface = marker.isOnSurface();
      if (marker.getBridgeId() != -10L) {
         BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
         return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
      } else if (marker.getFloorLevel() > 0) {
         Floor floor = Zones.getFloor(tilex, tiley, marker.isOnSurface(), marker.getFloorLevel());
         return new HighwayPos(marker.getTileX(), marker.getTileY(), marker.isOnSurface(), null, floor);
      } else {
         return new HighwayPos(marker.getTileX(), marker.getTileY(), marker.isOnSurface(), null, null);
      }
   }

   public static final String getLinkDirString(byte linkdir) {
      switch(linkdir) {
         case -128:
            return "northwest";
         case 1:
            return "north";
         case 2:
            return "northeast";
         case 4:
            return "east";
         case 8:
            return "southeast";
         case 16:
            return "south";
         case 32:
            return "southwest";
         case 64:
            return "west";
         default:
            return "unknown(" + linkdir + ")";
      }
   }

   public static final boolean canPlantMarker(@Nullable Creature performer, HighwayPos currentHighwayPos, Item marker, byte possibleLinks) {
      int cornerX = currentHighwayPos.getTilex();
      int cornerY = currentHighwayPos.getTiley();
      Village village = Villages.getVillagePlus(cornerX, cornerY, true, 2);
      int pcount = numberOfSetBits(possibleLinks);
      if (marker.getTemplateId() == 1112) {
         if (pcount == 0 && village == null) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("Can only plant if there is an adjacent marker.");
            }

            return false;
         }
      } else {
         if (pcount == 0) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("Can only plant if there is an adjacent marker.");
            }

            return false;
         }

         if (pcount > 2) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("Catseyes can only be planted if there is a maximum of two possible links.");
            }

            return false;
         }
      }

      if (performer != null) {
         if (village != null) {
            if (!village.isActionAllowed((short)176, performer)) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You do not have permission to plant a " + marker.getName() + " on (or next to) \"" + village.getName() + "\".");
               return false;
            }

            if (!village.isHighwayAllowed()) {
               performer.getCommunicator().sendNormalServerMessage("\"" + village.getName() + "\" does not allow highways.");
               return false;
            }

            if (village.getReputations().length > 0) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You cannot plant a " + marker.getName() + " on (or next to) \"" + village.getName() + "\" as it has an active kos list."
                  );
               return false;
            }
         }

         Skill skill = performer.getSkills().getSkillOrLearn(10031);
         if (skill.getRealKnowledge() < 20.1) {
            performer.getCommunicator().sendNormalServerMessage("You do not have enough skill to plant that.");
            return false;
         }

         if (!performer.isPaying()) {
            performer.getCommunicator().sendNormalServerMessage("You need to be premium to plant that.");
            return false;
         }

         if (checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("This area is too sloped to allow highway markers.");
            }

            return false;
         }

         HighwayPos highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)1);
         if (highwayPos != null && checkSlopes(highwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("North tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)2);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("North East tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)4);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("East tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)8);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("South East tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)16);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("South tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)32);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("South West tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)64);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("West tile is too sloped to allow highway markers.");
            }

            return false;
         }

         highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)-128);
         if (highwayPos != null && checkSlopes(currentHighwayPos)) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("North West tile is too sloped to allow highway markers.");
            }

            return false;
         }
      }

      return true;
   }

   static boolean checkSlopes(HighwayPos highwayPos) {
      if (highwayPos.isSurfaceTile()) {
         MeshTile meshTile = new MeshTile(Server.surfaceMesh, highwayPos.getTilex(), highwayPos.getTiley());
         if (Tiles.isRoadType(meshTile.getTileType()) && meshTile.checkSlopes(20, 28)) {
            return true;
         }
      }

      if (highwayPos.isCaveTile()) {
         MeshTile meshTile = new MeshTile(Server.caveMesh, highwayPos.getTilex(), highwayPos.getTiley());
         if (Tiles.isRoadType(meshTile.getTileType()) && meshTile.checkSlopes(20, 28)) {
            return true;
         }
      }

      return false;
   }

   public static final void removeNearbyMarkers(Floor floor) {
      HighwayPos highwayPos = new HighwayPos(floor.getTileX(), floor.getTileY(), floor.isOnSurface(), null, floor);
      removeNearbyMarkers(highwayPos);
   }

   public static final void removeNearbyMarkers(BridgePart bridgePart) {
      HighwayPos highwayPos = new HighwayPos(bridgePart.getTileX(), bridgePart.getTileY(), bridgePart.isOnSurface(), bridgePart, null);
      removeNearbyMarkers(highwayPos);
   }

   public static final void removeNearbyMarkers(int tilex, int tiley, boolean onSurface) {
      HighwayPos highwayPos = new HighwayPos(tilex, tiley, onSurface, null, null);
      removeNearbyMarkers(highwayPos);
   }

   private static final void removeNearbyMarkers(HighwayPos highwayPos) {
      Item marker = getMarker(highwayPos);
      if (marker != null) {
         marker.setDamage(100.0F);
      }

      removeNearbyMarker(highwayPos, (byte)1);
      removeNearbyMarker(highwayPos, (byte)2);
      removeNearbyMarker(highwayPos, (byte)4);
      removeNearbyMarker(highwayPos, (byte)8);
      removeNearbyMarker(highwayPos, (byte)16);
      removeNearbyMarker(highwayPos, (byte)32);
      removeNearbyMarker(highwayPos, (byte)64);
      removeNearbyMarker(highwayPos, (byte)-128);
   }

   private static final void removeNearbyMarker(HighwayPos currentHighwayPos, byte linkdir) {
      HighwayPos highwayPos = getNewHighwayPosLinked(currentHighwayPos, (byte)1);
      if (highwayPos != null) {
         Item marker = getMarker(highwayPos);
         if (marker != null) {
            if (currentHighwayPos.getBridgeId() == -10L && currentHighwayPos.getFloorLevel() == 0) {
               if (marker.getBridgeId() == -10L || marker.getFloorLevel() == 0) {
                  marker.setDamage(100.0F);
               }
            } else if (marker.getBridgeId() != -10L || marker.getFloorLevel() != 0) {
               marker.setDamage(100.0F);
            }
         }
      }
   }

   public static final byte convertLink(byte link) {
      switch(link) {
         case -128:
            return 7;
         case 1:
            return 0;
         case 2:
            return 1;
         case 4:
            return 2;
         case 8:
            return 3;
         case 16:
            return 4;
         case 32:
            return 5;
         case 64:
            return 6;
         default:
            return -1;
      }
   }

   public static final byte getOppositedir(byte fromdir) {
      int lr4 = (fromdir & 255) >>> 4;
      int ll4 = (fromdir & 255) << 4;
      int lc4 = lr4 | ll4;
      return (byte)(lc4 & 0xFF);
   }

   public static final byte getOtherdir(byte dirs, byte fromdir) {
      return (byte)(dirs & ~fromdir);
   }

   public static final boolean isNextToACamp(HighwayPos currentHighwayPos) {
      Item marker = getMarker(currentHighwayPos, (byte)1);
      if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
         return true;
      } else {
         marker = getMarker(currentHighwayPos, (byte)2);
         if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
         } else {
            marker = getMarker(currentHighwayPos, (byte)4);
            if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
               return true;
            } else {
               marker = getMarker(currentHighwayPos, (byte)8);
               if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
                  return true;
               } else {
                  marker = getMarker(currentHighwayPos, (byte)16);
                  if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
                     return true;
                  } else {
                     marker = getMarker(currentHighwayPos, (byte)32);
                     if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
                        return true;
                     } else {
                        marker = getMarker(currentHighwayPos, (byte)64);
                        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
                           return true;
                        } else {
                           marker = getMarker(currentHighwayPos, (byte)-128);
                           return marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static final int numberOfSetBits(byte b) {
      return Integer.bitCount(b & 0xFF);
   }
}

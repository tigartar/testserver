package com.wurmonline.server.highways;

import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.support.Trello;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.webinterface.WcTrelloHighway;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.HighwayConstants;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Routes implements HighwayConstants {
   private static Logger logger = Logger.getLogger(Routes.class.getName());
   private static int nextId = 1;
   private static ConcurrentHashMap<Integer, Route> allRoutes = new ConcurrentHashMap<>();
   private static ConcurrentHashMap<Long, Node> allNodes = new ConcurrentHashMap<>();
   private static final ConcurrentLinkedDeque<PlayerMessageToSend> playerMessagesToSend = new ConcurrentLinkedDeque<>();

   private Routes() {
   }

   public static final void generateAllRoutes() {
      logger.info("Calculating All routes.");
      long start = System.nanoTime();

      for(Item waystone : Items.getWaystones()) {
         makeNodeFrom(waystone);
      }

      for(Item waystone : Items.getWaystones()) {
         checkForRoutes(waystone, false, null);
      }

      for(Item waystone : Items.getWaystones()) {
         Node startNode = getNode(waystone);
         HighwayFinder.queueHighwayFinding(null, startNode, null, (byte)0);
      }

      logger.log(
         Level.INFO,
         "Calculated " + allRoutes.size() + " routes and " + allNodes.size() + " nodes.That took " + (float)(System.nanoTime() - start) / 1000000.0F + " ms."
      );
      Players.getInstance()
         .sendGmMessage(
            null,
            "Roads",
            "Calculated "
               + allRoutes.size()
               + " routes and "
               + allNodes.size()
               + " nodes. That took "
               + (float)(System.nanoTime() - start) / 1000000.0F
               + " ms.",
            false
         );
   }

   private static final boolean checkForRoutes(Item waystone, boolean tellGms, Item marker) {
      boolean foundRoute = false;
      foundRoute |= checkForRoute(waystone, (byte)1, tellGms, marker);
      foundRoute |= checkForRoute(waystone, (byte)2, tellGms, marker);
      foundRoute |= checkForRoute(waystone, (byte)4, tellGms, marker);
      foundRoute |= checkForRoute(waystone, (byte)8, tellGms, marker);
      foundRoute |= checkForRoute(waystone, (byte)16, tellGms, marker);
      foundRoute |= checkForRoute(waystone, (byte)32, tellGms, marker);
      foundRoute |= checkForRoute(waystone, (byte)64, tellGms, marker);
      return foundRoute | checkForRoute(waystone, (byte)-128, tellGms, marker);
   }

   @Nullable
   private static final boolean checkForRoute(Item waystone, byte checkdir, boolean tellGms, Item planted) {
      if (!MethodsHighways.hasLink(waystone.getAuxData(), checkdir)) {
         return false;
      } else {
         Node startNode = getNode(waystone);
         if (startNode.getRoute(checkdir) != null) {
            return false;
         } else {
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(waystone);
            Route newRoute = new Route(startNode, checkdir, nextId);
            boolean checking = true;

            byte todir;
            for(byte linkdir = checkdir; checking; linkdir = todir) {
               int lastx = highwayPos.getTilex();
               int lasty = highwayPos.getTiley();
               boolean lastSurf = highwayPos.isOnSurface();
               long lastbp = highwayPos.getBridgeId();
               int lastfl = highwayPos.getFloorLevel();
               highwayPos = MethodsHighways.getNewHighwayPosLinked(highwayPos, linkdir);
               Item marker = MethodsHighways.getMarker(highwayPos);
               if (marker == null) {
                  logger.warning(
                     "Lost! "
                        + MethodsHighways.getLinkAsString(linkdir)
                        + " from:x:"
                        + lastx
                        + ",y:"
                        + lasty
                        + ",Surface:"
                        + lastSurf
                        + ",bp:"
                        + lastbp
                        + ",fl:"
                        + lastfl
                  );
                  return false;
               }

               byte fromdir = MethodsHighways.getOppositedir(linkdir);
               if (!MethodsHighways.hasLink(marker.getAuxData(), fromdir)) {
                  logger.info(
                     "Missing Link! "
                        + MethodsHighways.getLinkAsString(linkdir)
                        + " from:x:"
                        + lastx
                        + ",y:"
                        + lasty
                        + ",bp:"
                        + lastbp
                        + ",fl:"
                        + lastfl
                        + "  to:x"
                        + highwayPos.getTilex()
                        + ",y:"
                        + highwayPos.getTiley()
                        + "Surf:"
                        + highwayPos.isOnSurface()
                        + ",bp:"
                        + highwayPos.getBridgeId()
                        + ",fl:"
                        + highwayPos.getFloorLevel()
                  );
                  return false;
               }

               if (marker.getTemplateId() != 1114) {
                  if (marker.getTemplateId() != 1112) {
                     return false;
                  }

                  Node endNode = getNode(marker);
                  newRoute.AddEndNode(endNode);
                  startNode.AddRoute(checkdir, newRoute);
                  allRoutes.put(newRoute.getId(), newRoute);
                  LinkedList<Item> catseyes = new LinkedList<>();

                  for(Item catseye : newRoute.getCatseyesList()) {
                     catseyes.addFirst(catseye);
                  }

                  byte backdir = fromdir;
                  Route backRoute = new Route(endNode, fromdir, ++nextId);

                  for(Item catseye : catseyes) {
                     byte oppdir = MethodsHighways.getOppositedir(backdir);
                     backRoute.AddCatseye(catseye, false, oppdir);
                     backdir = MethodsHighways.getOtherdir(catseye.getAuxData(), oppdir);
                  }

                  backRoute.AddEndNode(startNode);
                  endNode.AddRoute(fromdir, backRoute);
                  allRoutes.put(backRoute.getId(), backRoute);
                  newRoute.SetOppositeRoute(backRoute);
                  backRoute.SetOppositeRoute(newRoute);
                  if (tellGms) {
                     waystone.updateModelNameOnGroundItem();

                     for(Item catseye : newRoute.getCatseyes()) {
                        catseye.updateModelNameOnGroundItem();
                     }

                     marker.updateModelNameOnGroundItem();
                     HighwayFinder.queueHighwayFinding(null, startNode, null, checkdir);
                     HighwayFinder.queueHighwayFinding(null, endNode, null, fromdir);
                  }

                  ++nextId;
                  checking = false;
                  return true;
               }

               todir = MethodsHighways.getOtherdir(marker.getAuxData(), fromdir);
               newRoute.AddCatseye(marker, false, todir);
               if (MethodsHighways.numberOfSetBits(todir) != 1) {
                  if (Servers.isThisATestServer()) {
                     logger.info(
                        "End of road! @"
                           + marker.getTileX()
                           + ","
                           + marker.getTileY()
                           + " (from:"
                           + MethodsHighways.getLinkAsString(fromdir)
                           + ",to:"
                           + MethodsHighways.getLinkAsString(todir)
                           + ")"
                     );
                  }

                  return false;
               }
            }

            return false;
         }
      }
   }

   public static final Node getNode(Item waystone) {
      Node node = allNodes.get(waystone.getWurmId());
      return node != null ? node : makeNodeFrom(waystone);
   }

   private static final Node makeNodeFrom(Item waystone) {
      Node node = new Node(waystone);
      VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
      if (vt != null && vt.getVillage() != null) {
         node.setVillage(vt.getVillage());
      }

      allNodes.put(waystone.getWurmId(), node);
      return node;
   }

   public static final void remove(Item marker) {
      if (marker.getTemplateId() == 1114) {
         for(Entry<Integer, Route> entry : allRoutes.entrySet()) {
            if (entry.getValue().containsCatseye(marker)) {
               removeRoute(entry.getValue(), marker);
               break;
            }
         }
      } else {
         Node node = allNodes.remove(marker.getWurmId());
         if (node != null) {
            removeRoute(node, (byte)1, marker);
            removeRoute(node, (byte)2, marker);
            removeRoute(node, (byte)4, marker);
            removeRoute(node, (byte)8, marker);
            removeRoute(node, (byte)16, marker);
            removeRoute(node, (byte)32, marker);
            removeRoute(node, (byte)64, marker);
            removeRoute(node, (byte)-128, marker);
         }
      }
   }

   private static final void removeRoute(Node node, byte checkdir, Item marker) {
      Route route = node.getRoute(checkdir);
      if (route != null) {
         removeRoute(route, marker);
      }
   }

   private static final void removeRoute(Route route, Item marker) {
      Node nodeStart = route.getStartNode();
      Node nodeEnd = route.getEndNode();
      boolean doCatseyes = nodeStart.removeRoute(route);
      allRoutes.remove(route.getId());
      Route oppRoute = route.getOppositeRoute();
      if (oppRoute != null) {
         Node oppStart = oppRoute.getStartNode();
         doCatseyes |= oppStart.removeRoute(oppRoute);
         allRoutes.remove(oppRoute.getId());
      }

      if (doCatseyes) {
         nodeStart.getWaystone().updateModelNameOnGroundItem();

         for(Item catseye : route.getCatseyes()) {
            catseye.updateModelNameOnGroundItem();
         }

         if (nodeEnd != null) {
            nodeEnd.getWaystone().updateModelNameOnGroundItem();
         }
      }

      if (!marker.isReplacing()) {
         String whatHappened = marker.getWhatHappened();
         if (whatHappened.length() == 0) {
            whatHappened = "unknown";
         }

         StringBuffer ttl = new StringBuffer();
         ttl.append(marker.getName());
         ttl.append(" @");
         ttl.append(marker.getTileX());
         ttl.append(",");
         ttl.append(marker.getTileY());
         ttl.append(",");
         ttl.append(marker.isOnSurface());
         ttl.append(" ");
         ttl.append(whatHappened);
         String title = ttl.toString();
         StringBuffer dsc = new StringBuffer();
         dsc.append("Routes removed between ");
         dsc.append(nodeStart.getWaystone().getTileX());
         dsc.append(",");
         dsc.append(nodeStart.getWaystone().getTileY());
         dsc.append(",");
         dsc.append(nodeStart.getWaystone().isOnSurface());
         dsc.append(" and ");
         if (nodeEnd != null) {
            dsc.append(nodeEnd.getWaystone().getTileX());
            dsc.append(",");
            dsc.append(nodeEnd.getWaystone().getTileY());
            dsc.append(",");
            dsc.append(nodeEnd.getWaystone().isOnSurface());
         } else {
            dsc.append(" end node missing!");
         }

         String description = dsc.toString();
         sendToTrello(title, description);
      }
   }

   public static final void sendToTrello(String title, String description) {
      Players.getInstance().sendGmMessage(null, "Roads", title, false);
      if (Servers.isThisLoginServer()) {
         Trello.addHighwayMessage(Servers.localServer.getAbbreviation(), title, description);
      } else {
         WcTrelloHighway wtc = new WcTrelloHighway(title, description);
         wtc.sendToLoginServer();
      }
   }

   public static final boolean checkForNewRoutes(Item marker) {
      if (marker.getTemplateId() == 1112) {
         getNode(marker);
         return checkForRoutes(marker, true, marker);
      } else {
         if (MethodsHighways.numberOfSetBits(marker.getAuxData()) == 2) {
            byte startdir = getStartdir(marker);
            if (startdir != 0) {
               Set<Item> markersDone = new HashSet<>();
               HighwayPos highwayPos = MethodsHighways.getHighwayPos(marker);
               boolean checking = true;

               byte todir;
               for(byte linkdir = startdir; checking; linkdir = todir) {
                  int lastx = highwayPos.getTilex();
                  int lasty = highwayPos.getTiley();
                  boolean lastSurf = highwayPos.isOnSurface();
                  long lastbp = highwayPos.getBridgeId();
                  int lastfl = highwayPos.getFloorLevel();
                  highwayPos = MethodsHighways.getNewHighwayPosLinked(highwayPos, linkdir);
                  Item nextMarker = MethodsHighways.getMarker(highwayPos);
                  if (nextMarker == null) {
                     logger.warning(
                        "Dead End! "
                           + MethodsHighways.getLinkAsString(linkdir)
                           + " from:x:"
                           + lastx
                           + ",y:"
                           + lasty
                           + ",Surface:"
                           + lastSurf
                           + ",bp:"
                           + lastbp
                           + ",fl:"
                           + lastfl
                     );
                     return false;
                  }

                  if (markersDone.contains(nextMarker)) {
                     logger.warning(
                        "Circular! "
                           + MethodsHighways.getLinkAsString(linkdir)
                           + " from:x:"
                           + lastx
                           + ",y:"
                           + lasty
                           + ",Surface:"
                           + lastSurf
                           + ",bp:"
                           + lastbp
                           + ",fl:"
                           + lastfl
                     );
                     return false;
                  }

                  markersDone.add(nextMarker);
                  byte fromdir = MethodsHighways.getOppositedir(linkdir);
                  if (MethodsHighways.numberOfSetBits(fromdir) != 1) {
                     logger.warning(
                        "Lost! "
                           + MethodsHighways.getLinkAsString(linkdir)
                           + " from:x:"
                           + lastx
                           + ",y:"
                           + lasty
                           + ",Surface:"
                           + lastSurf
                           + ",bp:"
                           + lastbp
                           + ",fl:"
                           + lastfl
                     );
                     return false;
                  }

                  if (!MethodsHighways.hasLink(nextMarker.getAuxData(), fromdir)) {
                     logger.info(
                        "Missing Link! "
                           + MethodsHighways.getLinkAsString(linkdir)
                           + " from:x:"
                           + lastx
                           + ",y:"
                           + lasty
                           + ",bp:"
                           + lastbp
                           + ",fl:"
                           + lastfl
                           + "  to:x"
                           + highwayPos.getTilex()
                           + ",y:"
                           + highwayPos.getTiley()
                           + "Surf:"
                           + highwayPos.isOnSurface()
                           + ",bp:"
                           + highwayPos.getBridgeId()
                           + ",fl:"
                           + highwayPos.getFloorLevel()
                     );
                     return false;
                  }

                  if (nextMarker.getTemplateId() != 1114) {
                     if (nextMarker.getTemplateId() == 1112) {
                        checking = false;
                        return checkForRoute(nextMarker, fromdir, true, marker);
                     }

                     return false;
                  }

                  todir = MethodsHighways.getOtherdir(nextMarker.getAuxData(), fromdir);
                  if (MethodsHighways.numberOfSetBits(todir) != 1) {
                     if (Servers.isThisATestServer()) {
                        logger.info(
                           "End of road! @"
                              + nextMarker.getTileX()
                              + ","
                              + nextMarker.getTileY()
                              + " (from:"
                              + MethodsHighways.getLinkAsString(fromdir)
                              + ",to:"
                              + MethodsHighways.getLinkAsString(todir)
                              + ")"
                        );
                     }

                     return false;
                  }
               }
            }
         }

         return false;
      }
   }

   private static final byte getStartdir(Item marker) {
      byte startdir = 0;
      byte dirs = marker.getAuxData();
      if (MethodsHighways.hasLink(dirs, (byte)1)) {
         startdir = 1;
      } else if (MethodsHighways.hasLink(dirs, (byte)2)) {
         startdir = 2;
      } else if (MethodsHighways.hasLink(dirs, (byte)4)) {
         startdir = 4;
      } else if (MethodsHighways.hasLink(dirs, (byte)8)) {
         startdir = 8;
      } else if (MethodsHighways.hasLink(dirs, (byte)16)) {
         startdir = 16;
      } else if (MethodsHighways.hasLink(dirs, (byte)32)) {
         startdir = 32;
      } else if (MethodsHighways.hasLink(dirs, (byte)64)) {
         startdir = 64;
      } else if (MethodsHighways.hasLink(dirs, (byte)-128)) {
         startdir = -128;
      }

      return startdir;
   }

   public static final Item[] getMarkers() {
      ConcurrentHashMap<Long, Item> markers = new ConcurrentHashMap<>();

      for(Route route : allRoutes.values()) {
         Item waystone = route.getStartNode().getWaystone();
         markers.put(waystone.getWurmId(), waystone);

         for(Item catseye : route.getCatseyes()) {
            markers.put(catseye.getWurmId(), catseye);
         }

         Node node = route.getEndNode();
         if (node != null) {
            markers.put(node.getWaystone().getWurmId(), node.getWaystone());
         }
      }

      return markers.values().toArray(new Item[markers.size()]);
   }

   public static final Item[] getRouteMarkers(Item marker) {
      ConcurrentHashMap<Long, Item> markers = new ConcurrentHashMap<>();
      if (marker.getTemplateId() == 1114) {
         for(Route route : allRoutes.values()) {
            if (route.containsCatseye(marker)) {
               Item startWaystone = route.getStartNode().getWaystone();
               markers.put(startWaystone.getWurmId(), startWaystone);

               for(Item catseye : route.getCatseyes()) {
                  markers.put(catseye.getWurmId(), catseye);
               }

               Item endWaystone = route.getEndNode().getWaystone();
               markers.put(endWaystone.getWurmId(), endWaystone);
               break;
            }
         }
      } else {
         for(Route route : allRoutes.values()) {
            if (route.getStartNode().getWurmId() == marker.getWurmId()) {
               Item startWaystone = route.getStartNode().getWaystone();
               markers.put(startWaystone.getWurmId(), startWaystone);

               for(Item catseye : route.getCatseyes()) {
                  markers.put(catseye.getWurmId(), catseye);
               }

               Item endWaystone = route.getEndNode().getWaystone();
               markers.put(endWaystone.getWurmId(), endWaystone);
            }

            if (route.getEndNode().getWurmId() == marker.getWurmId()) {
               Item startWaystone = route.getStartNode().getWaystone();
               markers.put(startWaystone.getWurmId(), startWaystone);

               for(Item catseye : route.getCatseyes()) {
                  markers.put(catseye.getWurmId(), catseye);
               }

               Item endWaystone = route.getEndNode().getWaystone();
               markers.put(endWaystone.getWurmId(), endWaystone);
            }
         }
      }

      return markers.values().toArray(new Item[markers.size()]);
   }

   public static final boolean isCatseyeUsed(Item catseye) {
      for(Route route : allRoutes.values()) {
         if (route.containsCatseye(catseye)) {
            return true;
         }
      }

      return false;
   }

   public static final boolean isMarkerUsed(Item marker) {
      if (marker.getTemplateId() == 1114) {
         return isCatseyeUsed(marker);
      } else {
         for(Route route : allRoutes.values()) {
            if (route.getStartNode().getWaystone().getWurmId() == marker.getWurmId()) {
               return true;
            }

            if (route.getEndNode() != null && route.getEndNode().getWaystone().getWurmId() == marker.getWurmId()) {
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   public static final Route getRoute(int id) {
      return allRoutes.get(id);
   }

   @Nullable
   public static final Node getNode(long wurmId) {
      return allNodes.get(wurmId);
   }

   public static final Route[] getAllRoutes() {
      return allRoutes.values().toArray(new Route[allRoutes.size()]);
   }

   public static final Node[] getAllNodes() {
      return allNodes.values().toArray(new Node[allNodes.size()]);
   }

   public static final Village[] getVillages() {
      ConcurrentHashMap<Integer, Village> villages = new ConcurrentHashMap<>();

      for(Node node : allNodes.values()) {
         Village vill = node.getVillage();
         if (vill != null && vill.isHighwayFound()) {
            villages.put(vill.getId(), vill);
         }
      }

      return villages.values().toArray(new Village[villages.size()]);
   }

   public static final Village[] getVillages(long waystoneId) {
      ConcurrentHashMap<Integer, Village> villages = new ConcurrentHashMap<>();

      for(Node node : allNodes.values()) {
         Village vill = node.getVillage();
         if (vill != null && vill.isHighwayFound() && PathToCalculate.isVillageConnected(waystoneId, vill)) {
            villages.put(vill.getId(), vill);
         }
      }

      return villages.values().toArray(new Village[villages.size()]);
   }

   public static final Node[] getNodesFor(Village village) {
      ConcurrentHashMap<Long, Node> nodes = new ConcurrentHashMap<>();

      for(Node node : allNodes.values()) {
         Village vill = node.getVillage();
         if (vill != null && vill.equals(village)) {
            nodes.put(node.getWaystone().getWurmId(), node);
         }
      }

      return nodes.values().toArray(new Node[nodes.size()]);
   }

   public static final void handlePathsToSend() {
      for(PlayerMessageToSend playerMessageToSend = playerMessagesToSend.pollFirst();
         playerMessageToSend != null;
         playerMessageToSend = playerMessagesToSend.pollFirst()
      ) {
         playerMessageToSend.send();
      }
   }

   public static final void queuePlayerMessage(Player player, String text) {
      playerMessagesToSend.add(new PlayerMessageToSend(player, text));
   }
}

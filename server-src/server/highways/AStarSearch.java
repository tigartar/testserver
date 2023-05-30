package com.wurmonline.server.highways;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AStarSearch {
   protected static List<Route> constructPath(AStarNode startNode, AStarNode node) {
      LinkedList<Route> path = new LinkedList<>();

      for(LinkedList<AStarNode> nodes = new LinkedList<>(); node.pathParent != null; node = node.pathParent) {
         if (nodes.contains(node)) {
            return null;
         }

         nodes.addFirst(node);
         path.addFirst(node.pathRoute);
      }

      return path;
   }

   public static List<Route> findPath(AStarNode startNode, AStarNode goalNode, byte initialDir) {
      AStarSearch.PriorityList openList = new AStarSearch.PriorityList();
      LinkedList<AStarNode> closedList = new LinkedList<>();
      startNode.costFromStart = 0.0F;
      startNode.estimatedCostToGoal = startNode.getEstimatedCost(goalNode);
      startNode.pathParent = null;
      startNode.pathRoute = null;
      openList.add(startNode);

      for(byte checkDir = initialDir; !openList.isEmpty(); checkDir = 0) {
         AStarNode node = openList.removeFirst();
         if (node == goalNode) {
            return constructPath(startNode, goalNode);
         }

         ConcurrentHashMap<Byte, Route> routesMap = node.getRoutes(checkDir);

         for(Entry<Byte, Route> entry : routesMap.entrySet()) {
            Route route = entry.getValue();
            AStarNode neighbourNode = route.getEndNode();
            boolean isOpen = openList.contains(neighbourNode);
            boolean isClosed = closedList.contains(neighbourNode);
            float costFromStart = node.costFromStart + route.getCost();
            if (!isOpen && !isClosed || costFromStart < neighbourNode.costFromStart) {
               neighbourNode.pathParent = node;
               neighbourNode.costFromStart = costFromStart;
               neighbourNode.estimatedCostToGoal = neighbourNode.getEstimatedCost(goalNode);
               neighbourNode.pathRoute = route;
               if (isClosed) {
                  closedList.remove(neighbourNode);
               }

               if (!isOpen) {
                  openList.add(neighbourNode);
               }
            }
         }

         closedList.add(node);
      }

      return null;
   }

   public static class PriorityList extends LinkedList<AStarNode> {
      private static final long serialVersionUID = 1L;

      public void add(Comparable<AStarNode> object) {
         for(int i = 0; i < this.size(); ++i) {
            if (object.compareTo(this.get(i)) <= 0) {
               this.add(i, (AStarNode)object);
               return;
            }
         }

         this.addLast((AStarNode)object);
      }
   }
}

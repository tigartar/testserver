/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.highways;

import com.wurmonline.server.highways.AStarNode;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.Route;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AStarSearch {
    protected static List<Route> constructPath(AStarNode startNode, AStarNode node) {
        LinkedList<Route> path = new LinkedList<Route>();
        LinkedList<AStarNode> nodes = new LinkedList<AStarNode>();
        while (node.pathParent != null) {
            if (nodes.contains(node)) {
                return null;
            }
            nodes.addFirst(node);
            path.addFirst(node.pathRoute);
            node = node.pathParent;
        }
        return path;
    }

    public static List<Route> findPath(AStarNode startNode, AStarNode goalNode, byte initialDir) {
        PriorityList openList = new PriorityList();
        LinkedList<AStarNode> closedList = new LinkedList<AStarNode>();
        startNode.costFromStart = 0.0f;
        startNode.estimatedCostToGoal = startNode.getEstimatedCost(goalNode);
        startNode.pathParent = null;
        startNode.pathRoute = null;
        openList.add(startNode);
        byte checkDir = initialDir;
        while (!openList.isEmpty()) {
            AStarNode node = (AStarNode)openList.removeFirst();
            if (node == goalNode) {
                return AStarSearch.constructPath(startNode, goalNode);
            }
            ConcurrentHashMap<Byte, Route> routesMap = node.getRoutes(checkDir);
            for (Map.Entry<Byte, Route> entry : routesMap.entrySet()) {
                Route route = entry.getValue();
                Node neighbourNode = route.getEndNode();
                boolean isOpen = openList.contains(neighbourNode);
                boolean isClosed = closedList.contains(neighbourNode);
                float costFromStart = node.costFromStart + route.getCost();
                if ((isOpen || isClosed) && !(costFromStart < neighbourNode.costFromStart)) continue;
                neighbourNode.pathParent = node;
                neighbourNode.costFromStart = costFromStart;
                neighbourNode.estimatedCostToGoal = ((AStarNode)neighbourNode).getEstimatedCost(goalNode);
                neighbourNode.pathRoute = route;
                if (isClosed) {
                    closedList.remove(neighbourNode);
                }
                if (isOpen) continue;
                openList.add(neighbourNode);
            }
            closedList.add(node);
            checkDir = 0;
        }
        return null;
    }

    public static class PriorityList
    extends LinkedList<AStarNode> {
        private static final long serialVersionUID = 1L;

        public void add(Comparable<AStarNode> object) {
            for (int i = 0; i < this.size(); ++i) {
                if (object.compareTo((AStarNode)this.get(i)) > 0) continue;
                this.add(i, (AStarNode)object);
                return;
            }
            this.addLast((AStarNode)object);
        }
    }
}


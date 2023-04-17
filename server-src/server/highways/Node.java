/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.highways;

import com.wurmonline.server.highways.AStarNode;
import com.wurmonline.server.highways.ClosestVillage;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public class Node
extends AStarNode {
    private final Item waystone;
    private Village village = null;
    final ConcurrentHashMap<Byte, Route> routes = new ConcurrentHashMap();
    final ConcurrentHashMap<Byte, AStarNode> neighbours = new ConcurrentHashMap();
    final ConcurrentHashMap<Byte, ClosestVillage> pointers = new ConcurrentHashMap();

    public Node(Item waystone) {
        this.waystone = waystone;
    }

    public long getWurmId() {
        return this.waystone.getWurmId();
    }

    public Item getWaystone() {
        return this.waystone;
    }

    public void setVillage(Village village) {
        this.village = village;
    }

    public int getRouteCount() {
        return this.routes.size();
    }

    public Village getVillage() {
        return this.village;
    }

    public void AddRoute(byte direction, Route route) {
        this.routes.put(direction, route);
        this.neighbours.put(direction, route.getEndNode());
    }

    public void addClosestVillage(byte direction, String name, short distance) {
        this.pointers.put(direction, new ClosestVillage(name, distance));
    }

    @Nullable
    public ClosestVillage getClosestVillage(byte direction) {
        return this.pointers.get(direction);
    }

    @Nullable
    public Route getRoute(byte direction) {
        return this.routes.get(direction);
    }

    public byte getNodeDir(Node node) {
        byte bestdir = 0;
        float bestCost = 99999.0f;
        for (Map.Entry<Byte, Route> entry : this.routes.entrySet()) {
            Route route = entry.getValue();
            byte dir = entry.getKey();
            if (route.getEndNode() != node && route.getStartNode() != node || !(route.getCost() < bestCost)) continue;
            bestCost = route.getCost();
            bestdir = dir;
        }
        return bestdir;
    }

    public boolean removeRoute(Route oldRoute) {
        for (Map.Entry<Byte, Route> entry : this.routes.entrySet()) {
            if (entry.getValue() != oldRoute) continue;
            this.routes.remove(entry.getKey());
        }
        for (Map.Entry<Byte, Object> entry : this.neighbours.entrySet()) {
            if (entry.getValue() != oldRoute.getEndNode()) continue;
            this.neighbours.remove(entry.getKey());
        }
        return this.village == null && this.routes.isEmpty();
    }

    @Override
    public float getCost(AStarNode node) {
        Route route = this.findRoute(node);
        if (route != null) {
            return route.getCost();
        }
        return 99999.0f;
    }

    public float getDistance(AStarNode node) {
        Route route = this.findRoute(node);
        if (route != null) {
            return route.getDistance();
        }
        return 99999.0f;
    }

    @Override
    public float getEstimatedCost(AStarNode node) {
        Route route = this.findRoute(node);
        if (route != null) {
            int diffx = Math.abs(this.waystone.getTileX() - ((Node)node).waystone.getTileX());
            int diffy = Math.abs(this.waystone.getTileY() - ((Node)node).waystone.getTileY());
            return diffx + diffy;
        }
        return 99999.0f;
    }

    @Nullable
    private Route findRoute(AStarNode node) {
        for (Map.Entry<Byte, AStarNode> entry : this.neighbours.entrySet()) {
            if (entry.getValue() != node) continue;
            return this.routes.get(entry.getKey());
        }
        return null;
    }

    @Override
    public List<AStarNode> getNeighbours(byte dir) {
        ArrayList<AStarNode> alist = new ArrayList<AStarNode>();
        if (dir != 0) {
            Route route = this.getRoute(dir);
            if (route != null && route.getEndNode() != null) {
                alist.add(this.neighbours.get(dir));
            }
        } else {
            for (Map.Entry<Byte, AStarNode> entry : this.neighbours.entrySet()) {
                if (alist.contains(entry.getValue())) continue;
                alist.add(entry.getValue());
            }
        }
        return alist;
    }

    @Override
    public ConcurrentHashMap<Byte, Route> getRoutes(byte dir) {
        if (dir != 0) {
            ConcurrentHashMap<Byte, Route> lroutes = new ConcurrentHashMap<Byte, Route>();
            Route route = this.routes.get(dir);
            if (route != null) {
                lroutes.put(dir, route);
            }
            return lroutes;
        }
        return this.routes;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{Node:" + this.waystone.getWurmId());
        boolean first = true;
        for (Map.Entry<Byte, Route> entry : this.routes.entrySet()) {
            if (first) {
                first = false;
                buf.append("{");
            } else {
                buf.append(",");
            }
            buf.append(" {Dir:");
            buf.append(MethodsHighways.getLinkDirString(entry.getKey()));
            buf.append(",Cost:");
            buf.append(entry.getValue().getCost());
            buf.append(",Route:");
            buf.append(entry.getValue().getId());
            buf.append("}");
        }
        if (!first) {
            buf.append("}");
        }
        buf.append("}");
        return buf.toString();
    }
}


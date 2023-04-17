/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.highways;

import com.wurmonline.server.highways.Node;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

public class Route {
    private final Node startNode;
    private final byte direction;
    private final int id;
    private final LinkedList<Item> catseyes = new LinkedList();
    private Node endNode = null;
    private float cost = 0.0f;
    private float distance = 0.0f;
    private Route oppositeRoute = null;

    public Route(Node startNode, byte direction, int id) {
        this.startNode = startNode;
        this.direction = direction;
        this.id = id;
        this.addCost(startNode.getWaystone(), direction);
    }

    public Node getStartNode() {
        return this.startNode;
    }

    public byte getDirection() {
        return this.direction;
    }

    public int getId() {
        return this.id;
    }

    @Nullable
    public Route getOppositeRoute() {
        return this.oppositeRoute;
    }

    void SetOppositeRoute(Route oppositeRoute) {
        this.oppositeRoute = oppositeRoute;
    }

    public void AddCatseye(Item catseye, boolean atFront, byte direction) {
        if (atFront) {
            this.catseyes.addFirst(catseye);
        } else {
            this.catseyes.add(catseye);
        }
        this.addCost(catseye, direction);
    }

    private void addCost(Item marker, byte direction) {
        float thiscost = 1.0f;
        if (direction == 2 || direction == 8 || direction == 32 || direction == -128) {
            thiscost = 1.414f;
        }
        this.distance += thiscost;
        if (!marker.isOnSurface()) {
            thiscost *= 1.1f;
        }
        if (marker.getBridgeId() != -10L) {
            thiscost *= 1.05f;
        }
        this.cost += thiscost;
    }

    public Item[] getCatseyes() {
        return this.catseyes.toArray(new Item[this.catseyes.size()]);
    }

    public LinkedList<Item> getCatseyesList() {
        return this.catseyes;
    }

    public LinkedList<Item> getCatseyesListCopy() {
        return new LinkedList<Item>(this.catseyes);
    }

    public boolean containsCatseye(Item catseye) {
        return this.catseyes.contains(catseye);
    }

    public float getCost() {
        return this.cost;
    }

    public float getDistance() {
        return this.distance;
    }

    public void AddEndNode(Node node) {
        this.endNode = node;
    }

    @Nullable
    public Node getEndNode() {
        return this.endNode;
    }

    public final short isOnHighwayPath(Player player) {
        List<Route> highwayPath = player.getHighwayPath();
        if (highwayPath != null) {
            for (int x = 0; x < highwayPath.size(); ++x) {
                if (highwayPath.get(x) != this) continue;
                float distance = 0.0f;
                for (int y = x; y < highwayPath.size(); ++y) {
                    distance += highwayPath.get(y).getDistance();
                }
                return (short)distance;
            }
        }
        return -1;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{route:" + this.id);
        buf.append(" Start Waystone:" + this.startNode.toString());
        buf.append(" End waystone:");
        if (this.endNode != null) {
            buf.append(this.endNode.getWaystone().toString());
        } else {
            buf.append("missing");
        }
        boolean first = true;
        for (Item catseye : this.catseyes) {
            if (first) {
                first = false;
                buf.append(" Catseyes:{");
            } else {
                buf.append(",");
            }
            buf.append(catseye.toString());
        }
        if (!first) {
            buf.append("}");
        }
        buf.append("}");
        return buf.toString();
    }
}


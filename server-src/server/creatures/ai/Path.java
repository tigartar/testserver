/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.creatures.ai.PathTile;
import java.util.LinkedList;

public final class Path {
    private LinkedList<PathTile> path;

    Path() {
        this.path = new LinkedList();
    }

    public Path(LinkedList<PathTile> pathlist) {
        this.path = pathlist;
    }

    public PathTile getFirst() {
        return this.path.getFirst();
    }

    public PathTile getTargetTile() {
        return this.path.getLast();
    }

    public int getSize() {
        return this.path.size();
    }

    public void removeFirst() {
        this.path.removeFirst();
    }

    public boolean isEmpty() {
        return this.path == null || this.path.isEmpty();
    }

    public LinkedList<PathTile> getPathTiles() {
        return this.path;
    }

    public void clear() {
        if (this.path != null) {
            this.path.clear();
        }
        this.path = null;
    }
}


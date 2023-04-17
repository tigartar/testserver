/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

public enum ModifiedBy {
    NOTHING(0),
    NO_TREES(1),
    NEAR_TREE(2),
    NEAR_BUSH(3),
    NEAR_OAK(4),
    EASTER(5),
    HUNGER(6),
    WOUNDED(7),
    NEAR_WATER(8);

    private final int code;

    private ModifiedBy(int aCode) {
        this.code = aCode;
    }

    public float chanceModifier(Creature performer, int modifier, int tilex, int tiley) {
        int y;
        int x;
        if (this == NOTHING) {
            return 0.0f;
        }
        if (this == EASTER) {
            if (!performer.isPlayer() || ((Player)performer).isReallyPaying() && WurmCalendar.isEaster() && !((Player)performer).isReimbursed()) {
                return modifier;
            }
            return 0.0f;
        }
        if (this == HUNGER) {
            if (performer.getStatus().getHunger() < 20) {
                return modifier;
            }
            return 0.0f;
        }
        if (this == WOUNDED) {
            if (performer.getStatus().damage > 15) {
                return modifier;
            }
            return 0.0f;
        }
        MeshIO mesh = Server.surfaceMesh;
        if (this.isAModifier(mesh.getTile(tilex, tiley))) {
            if (this == NO_TREES) {
                return 0.0f;
            }
            return modifier;
        }
        for (x = -1; x <= 1; ++x) {
            for (y = -1; y <= 1; ++y) {
                if (x != -1 && x != 1 && y != -1 && y != 1 || !this.isAModifier(mesh.getTile(tilex + x, tiley + y))) continue;
                if (this == NO_TREES) {
                    return 0.0f;
                }
                return modifier / 2;
            }
        }
        for (x = -2; x <= 2; ++x) {
            for (y = -2; y <= 2; ++y) {
                if (x != -2 && x != 2 && y != -2 && y != 2 || !this.isAModifier(mesh.getTile(tilex + x, tiley + y))) continue;
                if (this == NO_TREES) {
                    return 0.0f;
                }
                return modifier / 3;
            }
        }
        for (x = -5; x <= 5; ++x) {
            for (y = -5; y <= 5; ++y) {
                if (x > -3 && x < 3 && y > -3 && y < 3 || !this.isAModifier(mesh.getTile(tilex + x, tiley + y))) continue;
                if (this == NO_TREES) {
                    return 0.0f;
                }
                return modifier / 4;
            }
        }
        if (this == NO_TREES) {
            return modifier;
        }
        return 0.0f;
    }

    private boolean isAModifier(int tile) {
        if (this == NEAR_WATER) {
            return Tiles.decodeHeight(tile) < 5;
        }
        byte decodedType = Tiles.decodeType(tile);
        byte decodedData = Tiles.decodeData(tile);
        Tiles.Tile theTile = Tiles.getTile(decodedType);
        if (this == NEAR_OAK) {
            if (theTile.isNormalTree()) {
                return theTile.isOak(decodedData);
            }
        } else {
            if (this == NEAR_TREE || this == NO_TREES) {
                return theTile.isNormalTree();
            }
            if (this == NEAR_BUSH) {
                return theTile.isNormalBush();
            }
        }
        return false;
    }
}


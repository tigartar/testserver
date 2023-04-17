/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.math.Vector2f;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

public class UtilitiesAOE {
    public static HashSet<Creature> getLineAreaCreatures(Creature c, float distance, float width) {
        Rectangle2D.Float r = new Rectangle2D.Float(c.getPosX() - width / 2.0f, c.getPosY(), width, distance);
        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(Creature.normalizeAngle(c.getStatus().getRotation() - 180.0f)), c.getPosX(), c.getPosY());
        Shape rotatedArea = at.createTransformedShape(r);
        HashSet<Creature> creatureList = new HashSet<Creature>();
        for (int i = c.getTileX() - ((int)(distance / 4.0f) + 1); i < c.getTileX() + ((int)(distance / 4.0f) + 1); ++i) {
            for (int j = c.getTileY() - ((int)(distance / 4.0f) + 1); j < c.getTileY() + ((int)(distance / 4.0f) + 1); ++j) {
                VolaTile v = Zones.getTileOrNull(i, j, c.isOnSurface());
                if (v == null) continue;
                for (Creature target : v.getCreatures()) {
                    if (!rotatedArea.contains(target.getPosX(), target.getPosY())) continue;
                    creatureList.add(target);
                }
            }
        }
        return creatureList;
    }

    public static HashSet<Point> getLineArea(Creature c, float distance, float width) {
        Rectangle2D.Float r = new Rectangle2D.Float(c.getPosX() - width / 2.0f, c.getPosY(), width, distance);
        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(Creature.normalizeAngle(c.getStatus().getRotation() - 180.0f)), c.getPosX(), c.getPosY());
        Shape rotatedArea = at.createTransformedShape(r);
        HashSet<Point> tileList = new HashSet<Point>();
        for (int i = c.getTileX() - ((int)(distance / 4.0f) + 1); i < c.getTileX() + ((int)(distance / 4.0f) + 1); ++i) {
            for (int j = c.getTileY() - ((int)(distance / 4.0f) + 1); j < c.getTileY() + ((int)(distance / 4.0f) + 1); ++j) {
                if (!rotatedArea.contains(i * 4 + 2, j * 4 + 2)) continue;
                tileList.add(new Point(i, j));
            }
        }
        return tileList;
    }

    public static HashSet<Creature> getRadialAreaCreatures(Creature c, float radius) {
        HashSet<Creature> creatureList = new HashSet<Creature>();
        int tileRadius = (int)(radius / 4.0f);
        for (int i = c.getTileX() - (tileRadius + 1); i < c.getTileX() + (tileRadius + 1); ++i) {
            for (int j = c.getTileY() - (tileRadius + 1); j < c.getTileY() + (tileRadius + 1); ++j) {
                VolaTile v = Zones.getTileOrNull(i, j, c.isOnSurface());
                if (v == null) continue;
                for (Creature target : v.getCreatures()) {
                    if (!((target.getPosX() - c.getPosX()) * (target.getPosX() - c.getPosX()) + (target.getPosY() - c.getPosY()) * (target.getPosY() - c.getPosY()) < radius * radius)) continue;
                    creatureList.add(target);
                }
            }
        }
        return creatureList;
    }

    public static HashSet<Point> getRadialArea(Creature c, int radius) {
        HashSet<Point> tileList = new HashSet<Point>();
        for (int i = c.getTileX() - (radius + 1); i < c.getTileX() + (radius + 1); ++i) {
            for (int j = c.getTileY() - (radius + 1); j < c.getTileY() + (radius + 1); ++j) {
                if ((i - c.getTileX()) * (i - c.getTileX()) + (j - c.getTileY()) * (j - c.getTileY()) >= radius * radius) continue;
                tileList.add(new Point(i, j));
            }
        }
        return tileList;
    }

    public static HashSet<Creature> getConeAreaCreatures(Creature c, float coneDistance, int coneAngle) {
        float attAngle = Creature.normalizeAngle(c.getStatus().getRotation() - 90.0f);
        Vector2f creaturePoint = new Vector2f(c.getPosX(), c.getPosY());
        Vector2f testPoint = new Vector2f();
        HashSet<Creature> creatureList = new HashSet<Creature>();
        int coneDistTiles = (int)(coneDistance / 4.0f);
        for (int i = c.getTileX() - (coneDistTiles + 1); i < c.getTileX() + (coneDistTiles + 1); ++i) {
            for (int j = c.getTileY() - (coneDistTiles + 1); j < c.getTileY() + (coneDistTiles + 1); ++j) {
                VolaTile v = Zones.getTileOrNull(i, j, c.isOnSurface());
                if (v == null) continue;
                for (Creature target : v.getCreatures()) {
                    if (!((target.getPosX() - c.getPosX()) * (target.getPosX() - c.getPosX()) + (target.getPosY() - c.getPosY()) * (target.getPosY() - c.getPosY()) < coneDistance * coneDistance)) continue;
                    testPoint.set(target.getPosX(), target.getPosY());
                    if (!(Math.abs(UtilitiesAOE.getAngleDiff(creaturePoint, testPoint) - attAngle) < (float)(coneAngle / 2))) continue;
                    creatureList.add(target);
                }
            }
        }
        return creatureList;
    }

    public static HashSet<Point> getConeArea(Creature c, int coneDistance, int coneAngle) {
        float attAngle = Creature.normalizeAngle(c.getStatus().getRotation() - 90.0f);
        Vector2f creaturePoint = new Vector2f(c.getPosX(), c.getPosY());
        Vector2f testPoint = new Vector2f();
        HashSet<Point> tileList = new HashSet<Point>();
        for (int i = c.getTileX() - (coneDistance + 1); i < c.getTileX() + (coneDistance + 1); ++i) {
            for (int j = c.getTileY() - (coneDistance + 1); j < c.getTileY() + (coneDistance + 1); ++j) {
                if ((i - c.getTileX()) * (i - c.getTileX()) + (j - c.getTileY()) * (j - c.getTileY()) >= coneDistance * coneDistance) continue;
                testPoint.set(i * 4 + 2, j * 4 + 2);
                if (!(Math.abs(UtilitiesAOE.getAngleDiff(creaturePoint, testPoint) - attAngle) < (float)(coneAngle / 2))) continue;
                tileList.add(new Point(i, j));
            }
        }
        return tileList;
    }

    public static Vector2f getPointInFrontOf(Creature c, float distance) {
        float attAngle = (float)Math.toRadians(Creature.normalizeAngle(c.getStatus().getRotation() - 90.0f));
        Vector2f toReturn = new Vector2f((float)Math.cos(attAngle) * distance, (float)Math.sin(attAngle) * distance);
        toReturn = toReturn.add(new Vector2f(c.getPosX(), c.getPosY()));
        return toReturn;
    }

    private static float getAngleDiff(Vector2f from, Vector2f to) {
        float angle = (float)Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        return angle;
    }
}


package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.math.Vector2f;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D.Float;
import java.util.HashSet;

public class UtilitiesAOE {
   public static HashSet<Creature> getLineAreaCreatures(Creature c, float distance, float width) {
      Float r = new Float(c.getPosX() - width / 2.0F, c.getPosY(), width, distance);
      AffineTransform at = AffineTransform.getRotateInstance(
         Math.toRadians((double)Creature.normalizeAngle(c.getStatus().getRotation() - 180.0F)), (double)c.getPosX(), (double)c.getPosY()
      );
      Shape rotatedArea = at.createTransformedShape(r);
      HashSet<Creature> creatureList = new HashSet<>();

      for(int i = c.getTileX() - ((int)(distance / 4.0F) + 1); i < c.getTileX() + (int)(distance / 4.0F) + 1; ++i) {
         for(int j = c.getTileY() - ((int)(distance / 4.0F) + 1); j < c.getTileY() + (int)(distance / 4.0F) + 1; ++j) {
            VolaTile v = Zones.getTileOrNull(i, j, c.isOnSurface());
            if (v != null) {
               for(Creature target : v.getCreatures()) {
                  if (rotatedArea.contains((double)target.getPosX(), (double)target.getPosY())) {
                     creatureList.add(target);
                  }
               }
            }
         }
      }

      return creatureList;
   }

   public static HashSet<Point> getLineArea(Creature c, float distance, float width) {
      Float r = new Float(c.getPosX() - width / 2.0F, c.getPosY(), width, distance);
      AffineTransform at = AffineTransform.getRotateInstance(
         Math.toRadians((double)Creature.normalizeAngle(c.getStatus().getRotation() - 180.0F)), (double)c.getPosX(), (double)c.getPosY()
      );
      Shape rotatedArea = at.createTransformedShape(r);
      HashSet<Point> tileList = new HashSet<>();

      for(int i = c.getTileX() - ((int)(distance / 4.0F) + 1); i < c.getTileX() + (int)(distance / 4.0F) + 1; ++i) {
         for(int j = c.getTileY() - ((int)(distance / 4.0F) + 1); j < c.getTileY() + (int)(distance / 4.0F) + 1; ++j) {
            if (rotatedArea.contains((double)(i * 4 + 2), (double)(j * 4 + 2))) {
               tileList.add(new Point(i, j));
            }
         }
      }

      return tileList;
   }

   public static HashSet<Creature> getRadialAreaCreatures(Creature c, float radius) {
      HashSet<Creature> creatureList = new HashSet<>();
      int tileRadius = (int)(radius / 4.0F);

      for(int i = c.getTileX() - (tileRadius + 1); i < c.getTileX() + tileRadius + 1; ++i) {
         for(int j = c.getTileY() - (tileRadius + 1); j < c.getTileY() + tileRadius + 1; ++j) {
            VolaTile v = Zones.getTileOrNull(i, j, c.isOnSurface());
            if (v != null) {
               for(Creature target : v.getCreatures()) {
                  if ((target.getPosX() - c.getPosX()) * (target.getPosX() - c.getPosX())
                        + (target.getPosY() - c.getPosY()) * (target.getPosY() - c.getPosY())
                     < radius * radius) {
                     creatureList.add(target);
                  }
               }
            }
         }
      }

      return creatureList;
   }

   public static HashSet<Point> getRadialArea(Creature c, int radius) {
      HashSet<Point> tileList = new HashSet<>();

      for(int i = c.getTileX() - (radius + 1); i < c.getTileX() + radius + 1; ++i) {
         for(int j = c.getTileY() - (radius + 1); j < c.getTileY() + radius + 1; ++j) {
            if ((i - c.getTileX()) * (i - c.getTileX()) + (j - c.getTileY()) * (j - c.getTileY()) < radius * radius) {
               tileList.add(new Point(i, j));
            }
         }
      }

      return tileList;
   }

   public static HashSet<Creature> getConeAreaCreatures(Creature c, float coneDistance, int coneAngle) {
      float attAngle = Creature.normalizeAngle(c.getStatus().getRotation() - 90.0F);
      Vector2f creaturePoint = new Vector2f(c.getPosX(), c.getPosY());
      Vector2f testPoint = new Vector2f();
      HashSet<Creature> creatureList = new HashSet<>();
      int coneDistTiles = (int)(coneDistance / 4.0F);

      for(int i = c.getTileX() - (coneDistTiles + 1); i < c.getTileX() + coneDistTiles + 1; ++i) {
         for(int j = c.getTileY() - (coneDistTiles + 1); j < c.getTileY() + coneDistTiles + 1; ++j) {
            VolaTile v = Zones.getTileOrNull(i, j, c.isOnSurface());
            if (v != null) {
               for(Creature target : v.getCreatures()) {
                  if ((target.getPosX() - c.getPosX()) * (target.getPosX() - c.getPosX())
                        + (target.getPosY() - c.getPosY()) * (target.getPosY() - c.getPosY())
                     < coneDistance * coneDistance) {
                     testPoint.set(target.getPosX(), target.getPosY());
                     if (Math.abs(getAngleDiff(creaturePoint, testPoint) - attAngle) < (float)(coneAngle / 2)) {
                        creatureList.add(target);
                     }
                  }
               }
            }
         }
      }

      return creatureList;
   }

   public static HashSet<Point> getConeArea(Creature c, int coneDistance, int coneAngle) {
      float attAngle = Creature.normalizeAngle(c.getStatus().getRotation() - 90.0F);
      Vector2f creaturePoint = new Vector2f(c.getPosX(), c.getPosY());
      Vector2f testPoint = new Vector2f();
      HashSet<Point> tileList = new HashSet<>();

      for(int i = c.getTileX() - (coneDistance + 1); i < c.getTileX() + coneDistance + 1; ++i) {
         for(int j = c.getTileY() - (coneDistance + 1); j < c.getTileY() + coneDistance + 1; ++j) {
            if ((i - c.getTileX()) * (i - c.getTileX()) + (j - c.getTileY()) * (j - c.getTileY()) < coneDistance * coneDistance) {
               testPoint.set((float)(i * 4 + 2), (float)(j * 4 + 2));
               if (Math.abs(getAngleDiff(creaturePoint, testPoint) - attAngle) < (float)(coneAngle / 2)) {
                  tileList.add(new Point(i, j));
               }
            }
         }
      }

      return tileList;
   }

   public static Vector2f getPointInFrontOf(Creature c, float distance) {
      float attAngle = (float)Math.toRadians((double)Creature.normalizeAngle(c.getStatus().getRotation() - 90.0F));
      Vector2f toReturn = new Vector2f((float)Math.cos((double)attAngle) * distance, (float)Math.sin((double)attAngle) * distance);
      return toReturn.add(new Vector2f(c.getPosX(), c.getPosY()));
   }

   private static float getAngleDiff(Vector2f from, Vector2f to) {
      float angle = (float)Math.toDegrees(Math.atan2((double)(to.y - from.y), (double)(to.x - from.x)));
      if (angle < 0.0F) {
         angle += 360.0F;
      }

      return angle;
   }
}

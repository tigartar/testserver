package com.wurmonline.server.structures;

import com.wurmonline.math.Vector3f;
import java.util.LinkedList;

public final class BlockingResult {
   private LinkedList<Blocker> blockers;
   private LinkedList<Vector3f> intersections;
   private float totalCover;
   private static final Blocker[] emptyBlockers = new Blocker[0];
   private float estimatedBlockingTime;
   private float actualBlockingTime;

   public final float addBlocker(Blocker blockerToAdd, Vector3f intersection, float factorToAdd) {
      if (this.blockers == null) {
         this.blockers = new LinkedList<>();
         this.intersections = new LinkedList<>();
      }

      this.blockers.add(blockerToAdd);
      this.intersections.add(intersection);
      this.addBlockingFactor(factorToAdd);
      return this.totalCover;
   }

   public final void addBlockingFactor(float factorToAdd) {
      this.totalCover += factorToAdd;
   }

   public final Blocker getFirstBlocker() {
      return this.blockers != null && !this.blockers.isEmpty() ? this.blockers.getFirst() : null;
   }

   public final Blocker getLastBlocker() {
      return this.blockers != null && !this.blockers.isEmpty() ? this.blockers.getLast() : null;
   }

   public final Vector3f getFirstIntersection() {
      return this.intersections != null && !this.intersections.isEmpty() ? this.intersections.getFirst() : null;
   }

   public final Vector3f getLastIntersection() {
      return this.intersections != null && !this.intersections.isEmpty() ? this.intersections.getLast() : null;
   }

   public final float getTotalCover() {
      return this.totalCover;
   }

   public final void removeBlocker(Blocker blocker) {
      if (this.blockers != null) {
         this.blockers.remove(blocker);
      }
   }

   public final Blocker[] getBlockerArray() {
      return this.blockers != null && !this.blockers.isEmpty() ? this.blockers.toArray(new Blocker[this.blockers.size()]) : emptyBlockers;
   }

   public float getActualBlockingTime() {
      return this.actualBlockingTime;
   }

   public void setActualBlockingTime(float aActualBlockingTime) {
      this.actualBlockingTime = aActualBlockingTime;
   }

   public float getEstimatedBlockingTime() {
      return this.estimatedBlockingTime;
   }

   public void setEstimatedBlockingTime(float aEstimatedBlockingTime) {
      this.estimatedBlockingTime = aEstimatedBlockingTime;
   }
}

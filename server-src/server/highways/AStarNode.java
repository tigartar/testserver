package com.wurmonline.server.highways;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AStarNode implements Comparable<AStarNode> {
   AStarNode pathParent;
   float costFromStart;
   float estimatedCostToGoal;
   Route pathRoute;

   public float getCost() {
      return this.costFromStart + this.estimatedCostToGoal;
   }

   public int compareTo(AStarNode other) {
      float v = this.getCost() - other.getCost();
      return v > 0.0F ? 1 : (v < 0.0F ? -1 : 0);
   }

   public abstract float getCost(AStarNode var1);

   public abstract float getEstimatedCost(AStarNode var1);

   public abstract List<AStarNode> getNeighbours(byte var1);

   public abstract ConcurrentHashMap<Byte, Route> getRoutes(byte var1);
}

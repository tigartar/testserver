package com.wurmonline.server.highways;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

public class HighwayFinder extends Thread implements MiscConstants {
   private static Logger logger = Logger.getLogger(HighwayFinder.class.getName());
   private static final ConcurrentLinkedDeque<PathToCalculate> pathingQueue = new ConcurrentLinkedDeque<>();
   private boolean shouldStop = false;
   private boolean sleeping = false;
   private int waystoneno = 0;

   public HighwayFinder() {
      super("HighwayFinder-Thread");
   }

   @Override
   public void run() {
      while(!this.shouldStop) {
         try {
            PathToCalculate nextPath = getNextPathToCalculate();
            if (nextPath != null) {
               nextPath.calculate();
               this.sleeping = true;
               sleep(100L);
               this.sleeping = false;
            } else {
               this.sleeping = true;
               sleep(15000L);
               this.sleeping = false;
               int nextwaystone = this.waystoneno++;
               Item[] waystones = Items.getWaystones();
               if (nextwaystone >= waystones.length) {
                  this.waystoneno = 0;
               } else {
                  Item waystone = waystones[nextwaystone];
                  Node startNode = Routes.getNode(waystone);
                  pathingQueue.add(new PathToCalculate(null, startNode, null, (byte)0));
               }
            }
         } catch (InterruptedException var6) {
            this.sleeping = false;
         }
      }
   }

   public void shouldStop() {
      this.shouldStop = true;
      this.interrupt();
   }

   boolean isSleeping() {
      return this.sleeping;
   }

   public static final void queueHighwayFinding(Creature creature, Node startNode, Village village, byte checkDir) {
      HighwayFinder highwayThread = Server.getInstance().getHighwayFinderThread();
      if (highwayThread != null) {
         pathingQueue.add(new PathToCalculate(creature, startNode, village, checkDir));
         if (highwayThread.isSleeping()) {
            highwayThread.interrupt();
         }
      }
   }

   private static final PathToCalculate getNextPathToCalculate() {
      return pathingQueue.pollFirst();
   }
}

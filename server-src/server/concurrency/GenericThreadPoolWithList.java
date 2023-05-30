package com.wurmonline.server.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericThreadPoolWithList {
   private static Logger logger = Logger.getLogger(GenericThreadPoolWithList.class.getName());
   public static final String VERSION = "$Revision: 1.0 $";

   public static void multiThreadedPoll(List<? extends Pollable> lInputList, int aNumberOfTasks) {
      System.out.println("Polling banks");
      ExecutorService execSvc = Executors.newCachedThreadPool();
      int lLastID = lInputList.size();
      int lFirstID = 0;
      List toRun = new ArrayList();
      int lNumberOfTasks = Math.min(aNumberOfTasks, lInputList.size());

      for(int i = 1; i <= aNumberOfTasks && lNumberOfTasks <= i; ++i) {
         int m = lLastID * i / aNumberOfTasks;
         if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, i + " - First: " + lFirstID + ", last: " + m);
         }

         toRun.add(new GenericPollerWithList(lFirstID, m, lInputList));
         System.out.println("ADDED A TASK");
         lFirstID = m + 1;
      }

      long start = System.nanoTime();

      try {
         execSvc.invokeAll(toRun);
         if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "invokeAll took " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
         }
      } catch (InterruptedException var11) {
         logger.log(Level.WARNING, "task invocation interrupted", (Throwable)var11);
      } catch (RejectedExecutionException var12) {
         if (!execSvc.isShutdown()) {
            logger.log(Level.WARNING, "task submission rejected", (Throwable)var12);
         }
      }

      execSvc.shutdown();
      if (execSvc instanceof ThreadPoolExecutor) {
         ThreadPoolExecutor tpe = (ThreadPoolExecutor)execSvc;
         if (logger.isLoggable(Level.FINE)) {
            logger.log(
               Level.FINE,
               "ThreadPoolExecutor CorePoolSize: "
                  + tpe.getCorePoolSize()
                  + ", LargestPoolSize: "
                  + tpe.getLargestPoolSize()
                  + ", TaskCount: "
                  + tpe.getTaskCount()
            );
         }
      }

      if (logger.isLoggable(Level.FINEST)) {
         logger.log(Level.FINEST, "execSvc.isTerminated(): " + execSvc.isTerminated() + " took: " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
      }

      try {
         if (!execSvc.awaitTermination(30L, TimeUnit.SECONDS)) {
            logger.log(Level.WARNING, "ThreadPoolExceutor timed out instead of terminating");
         }
      } catch (InterruptedException var10) {
         logger.log(Level.WARNING, "task awaitTermination interrupted", (Throwable)var10);
      }

      if (logger.isLoggable(Level.FINEST)) {
         logger.log(Level.FINEST, "execSvc.isTerminated(): " + execSvc.isTerminated() + " took: " + (float)(System.nanoTime() - start) / 1000000.0F + "ms");
      }
   }
}

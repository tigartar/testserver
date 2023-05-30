package com.wurmonline.server.concurrency;

import java.util.ArrayList;
import java.util.List;

public class GenericThreadPoolWithListTester {
   public static void main(String[] args) {
      try {
         Thread.sleep(1000L);
      } catch (InterruptedException var6) {
         var6.printStackTrace();
      }

      long lLastID = 10000L;
      List<Pollable> lInputList = new ArrayList<>(10010);

      for(long j = 0L; j < 10000L; ++j) {
         lInputList.add(new Pollable() {
            @Override
            public void poll(long now) {
               Thread.yield();
            }
         });
      }

      for(int lNumberOfTasks = 1; lNumberOfTasks < 50; ++lNumberOfTasks) {
         GenericThreadPoolWithList.multiThreadedPoll(lInputList, lNumberOfTasks);
      }
   }
}

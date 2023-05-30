package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.concurrent.GuardedBy;

public final class Factions {
   private static Factions instance = null;
   @GuardedBy("FACTIONS_RW_LOCK")
   private static Map<String, Faction> factions;
   private static final ReentrantReadWriteLock FACTIONS_RW_LOCK = new ReentrantReadWriteLock();

   public static Factions getInstance() {
      if (instance == null) {
         instance = new Factions();
      }

      return instance;
   }

   private Factions() {
      FACTIONS_RW_LOCK.writeLock().lock();

      try {
         factions = new HashMap<>();
      } finally {
         FACTIONS_RW_LOCK.writeLock().unlock();
      }
   }

   public static void addFaction(Faction faction) {
      FACTIONS_RW_LOCK.writeLock().lock();

      try {
         factions.put(faction.getName(), faction);
      } finally {
         FACTIONS_RW_LOCK.writeLock().unlock();
      }
   }

   public static Faction getFaction(String name) throws Exception {
      FACTIONS_RW_LOCK.readLock().lock();

      Faction var2;
      try {
         Faction toReturn = factions.get(name);
         if (toReturn == null) {
            throw new WurmServerException("No faction with name " + name);
         }

         var2 = toReturn;
      } finally {
         FACTIONS_RW_LOCK.readLock().unlock();
      }

      return var2;
   }
}

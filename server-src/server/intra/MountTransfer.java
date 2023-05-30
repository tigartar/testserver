package com.wurmonline.server.intra;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.TimeConstants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MountTransfer implements MiscConstants, TimeConstants {
   private static final Map<Long, MountTransfer> transfers = new HashMap<>();
   private static final Map<Long, MountTransfer> transfersPerCreature = new HashMap<>();
   private final Map<Long, Integer> seats = new HashMap<>();
   private static final Logger logger = Logger.getLogger(MountTransfer.class.getName());
   private final long vehicleid;
   private final long pilotid;
   private final long creationTime;

   public MountTransfer(long vehicleId, long pilotId) {
      this.vehicleid = vehicleId;
      this.pilotid = pilotId;
      this.creationTime = System.currentTimeMillis();
      transfers.put(vehicleId, this);
   }

   public void addToSeat(long wid, int seatid) {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Adding " + wid + ", seat=" + seatid);
      }

      this.seats.put(wid, seatid);
      transfersPerCreature.put(wid, this);
   }

   public int getSeatFor(long wurmid) {
      return this.seats.keySet().contains(wurmid) ? this.seats.get(wurmid) : -1;
   }

   public void remove(long wurmid) {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Removing " + wurmid);
      }

      this.seats.remove(wurmid);
      transfersPerCreature.remove(wurmid);
      if (this.seats.isEmpty()) {
         this.clearAndRemove();
      }
   }

   long getCreationTime() {
      return this.creationTime;
   }

   private void clearAndRemove() {
      Iterator<Long> seatIt = this.seats.keySet().iterator();

      while(seatIt.hasNext()) {
         transfersPerCreature.remove(seatIt.next());
      }

      transfers.remove(this.vehicleid);
      this.seats.clear();
   }

   public long getVehicleId() {
      return this.vehicleid;
   }

   public long getPilotId() {
      return this.pilotid;
   }

   public static final MountTransfer getTransferFor(long wurmid) {
      return transfersPerCreature.get(wurmid);
   }

   public static final void pruneTransfers() {
      Set<MountTransfer> toRemove = new HashSet<>();

      for(MountTransfer mt : transfers.values()) {
         if (System.currentTimeMillis() - mt.getCreationTime() > 1800000L) {
            toRemove.add(mt);
         }
      }

      Iterator<MountTransfer> it2 = toRemove.iterator();

      while(it2.hasNext()) {
         it2.next().clearAndRemove();
      }
   }
}

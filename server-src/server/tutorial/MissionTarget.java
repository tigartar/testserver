package com.wurmonline.server.tutorial;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class MissionTarget {
   private final Set<MissionTrigger> missionTriggers = new HashSet<>();
   private static final Logger logger = Logger.getLogger(MissionTarget.class.getName());
   private final long id;

   MissionTarget(long targetId) {
      this.id = targetId;
   }

   long getId() {
      return this.id;
   }

   void addMissionTrigger(MissionTrigger missionReqs) {
      if (missionReqs != null) {
         this.missionTriggers.add(missionReqs);
      }
   }

   void removeMissionTrigger(MissionTrigger missionReqs) {
      this.missionTriggers.remove(missionReqs);
   }

   int getNumTriggers() {
      return this.missionTriggers.size();
   }

   private MissionTrigger getMissionTrigger(int mission, int state, boolean checkActive) {
      for(MissionTrigger mr : this.missionTriggers) {
         if (mr.getMissionRequired() == mission && mr.isTriggered((float)state, checkActive)) {
            return mr;
         }
      }

      return null;
   }

   public MissionTrigger[] getMissionTriggers() {
      return this.missionTriggers.toArray(new MissionTrigger[this.missionTriggers.size()]);
   }

   boolean isMissionFulfilled(int mission, byte state) {
      for(MissionTrigger mr : this.missionTriggers) {
         if (mr.getMissionRequired() == mission && mr.getStateRequired() == (float)state) {
            return true;
         }
      }

      return false;
   }

   void destroy() {
      MissionTriggers.destroyTriggersForTarget(this.id);
   }

   @Override
   public String toString() {
      return "MissionTarget [id=" + this.id + ", missionTriggers=" + this.missionTriggers + ", numTriggers=" + this.getNumTriggers() + "]";
   }
}

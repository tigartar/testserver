package com.wurmonline.server.tutorial;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MissionTargets implements CounterTypes {
   private static final Map<Long, MissionTarget> missionTargets = new HashMap<>();
   private static final Logger logger = Logger.getLogger(MissionTargets.class.getName());

   private MissionTargets() {
   }

   public static void destroyMissionTarget(long missionTarget, boolean destroyTriggers) {
      MissionTarget m = missionTargets.remove(missionTarget);
      if (m != null && destroyTriggers) {
         m.destroy();
      }
   }

   public static boolean isMissionTarget(long potentialTarget) {
      return missionTargets.containsKey(potentialTarget);
   }

   public static MissionTarget getMissionTargetFor(long potentialTarget) {
      return missionTargets.get(potentialTarget);
   }

   public static Long[] getTargetIds() {
      return missionTargets.keySet().toArray(new Long[missionTargets.size()]);
   }

   public static boolean destroyStructureTargets(long structureId, @Nullable String possibleCreatorName) {
      boolean found = false;
      Long[] targs = getTargetIds();

      for(Long tid : targs) {
         if (tid != null) {
            long targetId = tid;
            if (WurmId.getType(targetId) == 5) {
               Wall w = Wall.getWall(targetId);
               if (w != null && w.getStructureId() == structureId) {
                  MissionTarget mt = getMissionTargetFor(targetId);
                  if (mt != null) {
                     MissionTrigger[] mits = mt.getMissionTriggers();

                     for(MissionTrigger missionT : mits) {
                        if (possibleCreatorName == null || missionT.getCreatorName().toLowerCase().equals(possibleCreatorName)) {
                           found = true;
                           missionT.destroy();
                        }
                     }
                  }
               }
            }
         }
      }

      return found;
   }

   public static void addMissionTrigger(MissionTrigger trigger) {
      MissionTarget mt = getMissionTargetFor(trigger.getTarget());
      if (mt == null && trigger.getTarget() > 0L) {
         mt = new MissionTarget(trigger.getTarget());
         missionTargets.put(trigger.getTarget(), mt);
      }

      if (mt != null) {
         mt.addMissionTrigger(trigger);
      }
   }

   public static void removeMissionTrigger(MissionTrigger trigger, boolean destroyAllTriggers) {
      if (trigger != null) {
         MissionTarget mt = getMissionTargetFor(trigger.getTarget());
         if (mt != null) {
            mt.removeMissionTrigger(trigger);
            if (mt.getNumTriggers() == 0) {
               destroyMissionTarget(mt.getId(), destroyAllTriggers);
            }
         }
      }
   }
}

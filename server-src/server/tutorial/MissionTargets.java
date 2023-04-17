/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTarget;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MissionTargets
implements CounterTypes {
    private static final Map<Long, MissionTarget> missionTargets = new HashMap<Long, MissionTarget>();
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
        Long[] targs;
        boolean found = false;
        for (Long tid : targs = MissionTargets.getTargetIds()) {
            MissionTrigger[] mits;
            MissionTarget mt;
            Wall w;
            long targetId;
            if (tid == null || WurmId.getType(targetId = tid.longValue()) != 5 || (w = Wall.getWall(targetId)) == null || w.getStructureId() != structureId || (mt = MissionTargets.getMissionTargetFor(targetId)) == null) continue;
            for (MissionTrigger missionT : mits = mt.getMissionTriggers()) {
                if (possibleCreatorName != null && !missionT.getCreatorName().toLowerCase().equals(possibleCreatorName)) continue;
                found = true;
                missionT.destroy();
            }
        }
        return found;
    }

    public static void addMissionTrigger(MissionTrigger trigger) {
        MissionTarget mt = MissionTargets.getMissionTargetFor(trigger.getTarget());
        if (mt == null && trigger.getTarget() > 0L) {
            mt = new MissionTarget(trigger.getTarget());
            missionTargets.put(trigger.getTarget(), mt);
        }
        if (mt != null) {
            mt.addMissionTrigger(trigger);
        }
    }

    public static void removeMissionTrigger(MissionTrigger trigger, boolean destroyAllTriggers) {
        MissionTarget mt;
        if (trigger != null && (mt = MissionTargets.getMissionTargetFor(trigger.getTarget())) != null) {
            mt.removeMissionTrigger(trigger);
            if (mt.getNumTriggers() == 0) {
                MissionTargets.destroyMissionTarget(mt.getId(), destroyAllTriggers);
            }
        }
    }
}


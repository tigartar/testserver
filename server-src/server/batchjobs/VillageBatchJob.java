/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.batchjobs;

import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageBatchJob {
    private static Logger logger = Logger.getLogger(VillageBatchJob.class.getName());

    private VillageBatchJob() {
    }

    public static void convertToNewRolePermissionSystem() {
        logger.log(Level.INFO, "Converting to New Village Permission System.");
        int villagesDone = 0;
        int rolesDone = 0;
        int failed = 0;
        for (Village v : Villages.getVillages()) {
            ++villagesDone;
            for (VillageRole vr : v.getRoles()) {
                vr.convertSettings();
                try {
                    ++rolesDone;
                    vr.save();
                }
                catch (IOException ioe) {
                    ++failed;
                    logger.log(Level.INFO, "Failed to save role " + vr.getName() + " for village " + v.getName() + ".", ioe);
                }
            }
        }
        logger.log(Level.INFO, "Converted " + rolesDone + " roles in " + villagesDone + " villages to New Permissions System." + (failed > 0 ? " Failed " + failed + " saves" : ""));
    }

    public static void fixNewRolePermissionSystem() {
        logger.log(Level.INFO, "fix for New Village Permission System.");
        int villagesDone = 0;
        int rolesDone = 0;
        int failed = 0;
        for (Village v : Villages.getVillages()) {
            ++villagesDone;
            for (VillageRole vr : v.getRoles()) {
                boolean fixed = false;
                if (vr.mayBreed()) {
                    vr.setCanBrand(true);
                    fixed = true;
                }
                if (vr.mayManageSettings()) {
                    vr.setCanManageAllowedObjects(true);
                    fixed = true;
                }
                if (!fixed) continue;
                try {
                    ++rolesDone;
                    vr.save();
                }
                catch (IOException ioe) {
                    ++failed;
                    logger.log(Level.INFO, "Failed to save role " + vr.getName() + " for village " + v.getName() + ".", ioe);
                }
            }
        }
        logger.log(Level.INFO, "Fixed " + rolesDone + " roles in " + villagesDone + " villages to New Permissions System." + (failed > 0 ? " Failed " + failed + " saves" : ""));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.util.Pair
 */
package com.wurmonline.server.items;

import com.wurmonline.server.Server;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.util.Pair;

public abstract class LootTableUtilities {
    private static final Logger logger = Logger.getLogger(LootTableUtilities.class.getName());

    private LootTableUtilities() {
    }

    public static long getRandomLoot(@NonNull ArrayList<Pair<Short, Long>> lootTable, long maxValue, long minValueCap, long maxValueCap, int minRerolls, int maxRerolls, float chanceToReroll, float rrChanceDecreasePerRoll, long aimRollsTowardsValue) {
        long rolledValue;
        long value;
        if (lootTable == null) {
            logger.severe("Loot table was null");
            return -1L;
        }
        if (maxValue <= 0L) {
            logger.severe("maxValue was less than or equal to 0, maxValue=" + maxValue);
            return -1L;
        }
        if (minValueCap > maxValue || minValueCap > maxValueCap) {
            logger.severe("Min value cap is an unreasonable number. minValueCap=" + minValueCap + ", maxValue=" + maxValue + ", maxValueCap=" + maxValueCap);
            return -1L;
        }
        if (maxValueCap > maxValue) {
            logger.severe("Max value cap is larger than the max value. maxValueCap=" + maxValueCap + ", maxValue=" + maxValue);
            return -1L;
        }
        if (minRerolls > maxRerolls) {
            logger.severe("minRerolls larger than maxRerolls. minRerolls=" + minRerolls + ", maxRerolls=" + maxRerolls);
            return -1L;
        }
        if ((double)chanceToReroll > 1.0 || (double)chanceToReroll < 0.0) {
            logger.severe("chance to reroll is not a reasonable value. chanceToReroll=" + chanceToReroll);
            return -1L;
        }
        long[] candidateValues = new long[maxRerolls];
        int timesRerolled = 0;
        long actualMinValueCap = minValueCap < 0L ? 0L : minValueCap;
        float currentChanceToReroll = chanceToReroll + rrChanceDecreasePerRoll;
        boolean hasRerolled = false;
        do {
            long probationalValue;
            if (!hasRerolled) {
                hasRerolled = true;
            } else {
                ++timesRerolled;
            }
            while ((probationalValue = Server.rand.nextLong()) <= maxValue && probationalValue > actualMinValueCap && probationalValue <= maxValueCap) {
            }
            candidateValues[timesRerolled] = probationalValue;
        } while (timesRerolled < minRerolls || Server.rand.nextFloat() < currentChanceToReroll - rrChanceDecreasePerRoll && timesRerolled < maxRerolls);
        if (aimRollsTowardsValue < 0L) {
            value = 0L;
            for (int i = 0; i <= timesRerolled; ++i) {
                if (candidateValues[i] <= value) continue;
                value = candidateValues[i];
            }
            rolledValue = value;
        } else {
            value = 0L;
            long distance = Long.MAX_VALUE;
            for (int i = 0; i <= timesRerolled; ++i) {
                if (candidateValues[i] == aimRollsTowardsValue) {
                    return aimRollsTowardsValue;
                }
                long temp = Math.abs(candidateValues[i] - aimRollsTowardsValue);
                if (temp >= distance) continue;
                distance = temp;
                value = candidateValues[i];
            }
            rolledValue = value;
        }
        return rolledValue;
    }
}


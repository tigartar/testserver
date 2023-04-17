/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;

public enum EpicMissionEnum {
    BUILDSTRUCTURE_SP(101, 1, 3, 4, 4, true, false, true, true, false, 250, 250, 0, 60, false, true, "creation", "building"),
    BUILDSTRUCTURE_TO(102, 1, 4, 5, 5, true, false, true, true, false, 250, 500, 0, 60, false, true, "creation", "building"),
    BUILDSTRUCTURE_SG(103, 1, 5, 6, 6, true, false, true, true, false, 250, 750, 0, 60, false, true, "creation", "building"),
    RITUALMS_FRIENDLY(104, 4, 1, 7, 4, true, false, true, true, false, 0, 500, 30, 45, false, true, "proximity", "humility"),
    RITUALMS_ENEMY(105, 4, 1, 7, 5, false, true, true, false, true, 0, 500, 30, 45, false, true, "proximity", "humility"),
    CUTTREE_FRIENDLY(106, 4, 1, 5, 5, true, false, true, true, false, 500, 0, 30, 30, false, true, "revenge", "entrapment"),
    CUTTREE_ENEMY(107, 4, 1, 6, 6, false, true, true, false, true, 750, 0, 30, 30, false, true, "revenge", "entrapment"),
    RITUALGT(108, 4, 1, 5, 5, false, true, true, false, true, 0, 300, 30, 30, false, true, "submission", "danger"),
    SACMISSIONITEMS(109, 5, 1, 7, 7, true, true, true, true, false, 100, 500, 20, 30, true, false, "wealth", "sacrifice"),
    SACITEMS(110, 5, 1, 7, 7, true, false, true, true, false, 0, 500, 20, 30, true, false, "wealth", "sacrifice"),
    CREATEITEMS(111, 6, 1, 7, 7, true, false, true, true, false, 0, 500, 20, 30, true, false, "construction", "thought"),
    GIVEITEMS_FRIENDLY(112, 4, 1, 6, 4, true, false, true, true, false, 100, 500, 20, 20, true, false, "gifts", "concession"),
    GIVEITEMS_ENEMY(113, 3, 3, 7, 5, true, true, true, true, false, 150, 600, 30, 40, true, false, "gifts", "concession"),
    SLAYCREATURE_PASSIVE(114, 4, 1, 3, 3, true, false, true, true, false, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SLAYCREATURE_HOSTILELOW(115, 5, 2, 6, 6, true, true, true, true, true, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SLAYCREATURE_HOSTILEHIGH(116, 4, 5, 7, 7, true, true, true, true, true, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SLAYTRAITOR_PASSIVE(117, 4, 1, 3, 3, true, false, true, true, false, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SLAYTRAITOR_HOSTILELOW(118, 5, 2, 5, 5, true, true, true, true, true, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SLAYTRAITOR_HOSTILEHIGH(119, 4, 5, 7, 7, true, true, true, true, true, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    DESTROYGT(120, 3, 3, 6, 6, false, true, true, false, true, 750, 0, 30, 30, false, true, "destruction", "devastation"),
    SACCREATURE_PASSIVE(121, 3, 1, 3, 3, true, false, true, true, false, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SACCREATURE_HOSTILELOW(122, 4, 4, 5, 5, true, true, true, true, true, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SACCREATURE_HOSTILEHIGH(123, 3, 6, 7, 7, true, true, true, true, true, 0, 500, 15, 30, true, false, "annihilation", "treason"),
    SLAYTOWERGUARDS(124, 3, 1, 7, 7, false, true, true, false, true, 0, 500, 15, 30, true, false, "cleansing", "attack");

    private byte missionType;
    private int missionChance;
    private int minDifficulty;
    private int maxDifficulty;
    private boolean friendlyTerritory;
    private boolean enemyTerritory;
    private boolean battlegroundServer;
    private boolean homeServer;
    private boolean enemyHomeServer;
    private int baseKarma;
    private int karmaBonusDiffMult;
    private int baseSleep;
    private int sleepBonusDiffMult;
    private boolean isKarmaMultProgress;
    private boolean isSleepMultNearby;
    private String[] missionNames;

    public static EpicMissionEnum getRandomMission(int difficulty, boolean battlegroundServer, boolean homeServer, boolean enemyHomeServer) {
        int totalChance = 0;
        for (EpicMissionEnum f : EpicMissionEnum.values()) {
            if (f.minDifficulty > difficulty || f.maxDifficulty < difficulty || !(battlegroundServer && f.battlegroundServer || homeServer && f.homeServer) && (!enemyHomeServer || !f.enemyHomeServer)) continue;
            totalChance += f.getMissionChance();
        }
        if (totalChance == 0) {
            return null;
        }
        int winningVal = Server.rand.nextInt(totalChance);
        int thisVal = 0;
        for (EpicMissionEnum f : EpicMissionEnum.values()) {
            if (f.minDifficulty > difficulty || f.maxDifficulty < difficulty || !(battlegroundServer && f.battlegroundServer || homeServer && f.homeServer) && (!enemyHomeServer || !f.enemyHomeServer)) continue;
            if (thisVal + f.getMissionChance() > winningVal) {
                return f;
            }
            thisVal += f.getMissionChance();
        }
        return null;
    }

    public static EpicMissionEnum getMissionForType(byte missionType) {
        for (EpicMissionEnum f : EpicMissionEnum.values()) {
            if (f.getMissionType() != missionType) continue;
            return f;
        }
        return null;
    }

    public static boolean isMissionItem(EpicMissionEnum mission) {
        switch (mission.getMissionType()) {
            case 109: 
            case 110: 
            case 111: 
            case 112: 
            case 113: {
                return true;
            }
        }
        return false;
    }

    public static boolean isNumReqItemEffected(EpicMissionEnum mission) {
        switch (mission.getMissionType()) {
            case 110: 
            case 111: {
                return true;
            }
        }
        return false;
    }

    public static boolean isMissionCreature(EpicMissionEnum mission) {
        switch (mission.getMissionType()) {
            case 114: 
            case 115: 
            case 116: 
            case 117: 
            case 118: 
            case 119: 
            case 121: 
            case 122: 
            case 123: 
            case 124: {
                return true;
            }
        }
        return false;
    }

    public static boolean isMissionKarmaGivenOnKill(EpicMissionEnum mission) {
        switch (mission.getMissionType()) {
            case 114: 
            case 115: 
            case 116: 
            case 117: 
            case 118: 
            case 119: 
            case 124: {
                return true;
            }
        }
        return false;
    }

    public static boolean isKarmaSplitNearby(EpicMissionEnum mission) {
        switch (mission.getMissionType()) {
            case 101: 
            case 102: 
            case 103: {
                return true;
            }
        }
        return false;
    }

    public static boolean isRitualMission(EpicMissionEnum mission) {
        switch (mission.getMissionType()) {
            case 104: 
            case 105: 
            case 108: {
                return true;
            }
        }
        return false;
    }

    private EpicMissionEnum(byte missionType, int missionChance, int minDifficulty, int maxDifficulty, int maxDifficultyPvp, boolean friendlyTerritory, boolean enemyTerritory, boolean battlegroundServer, boolean homeServer, boolean enemyHomeServer, int baseKarma, int karmaBonusDiffMult, int baseSleep, int sleepBonusDiffMult, boolean isKarmaMultProgress, boolean isSleepMultNearby, String ... missionNames) {
        this.missionType = missionType;
        this.missionChance = missionChance;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
        if (Servers.localServer.PVPSERVER) {
            this.maxDifficulty = maxDifficultyPvp;
        }
        this.friendlyTerritory = friendlyTerritory;
        this.enemyTerritory = enemyTerritory;
        this.battlegroundServer = battlegroundServer;
        this.homeServer = homeServer;
        this.enemyHomeServer = enemyHomeServer;
        this.baseKarma = baseKarma;
        this.karmaBonusDiffMult = karmaBonusDiffMult;
        this.baseSleep = baseSleep;
        this.sleepBonusDiffMult = sleepBonusDiffMult;
        this.isKarmaMultProgress = isKarmaMultProgress;
        this.isSleepMultNearby = isSleepMultNearby;
        this.missionNames = missionNames;
    }

    public byte getMissionType() {
        return this.missionType;
    }

    public int getMissionChance() {
        return this.missionChance;
    }

    public int getMinDifficulty() {
        return this.minDifficulty;
    }

    public int getMaxDifficulty() {
        return this.maxDifficulty;
    }

    public boolean isFriendlyTerritory() {
        return this.friendlyTerritory;
    }

    public boolean isEnemyTerritory() {
        return this.enemyTerritory;
    }

    public boolean isBattlegroundServer() {
        return this.battlegroundServer;
    }

    public boolean isHomeServer() {
        return this.homeServer;
    }

    public boolean isEnemyHomeServer() {
        return this.enemyHomeServer;
    }

    public int getBaseKarma() {
        return this.baseKarma;
    }

    public int getKarmaBonusDiffMult() {
        return this.karmaBonusDiffMult;
    }

    public int getBaseSleep() {
        return this.baseSleep;
    }

    public int getSleepBonusDiffMult() {
        return this.sleepBonusDiffMult;
    }

    public boolean isKarmaMultProgress() {
        return this.isKarmaMultProgress;
    }

    public boolean isSleepMultNearby() {
        return this.isSleepMultNearby;
    }

    public String[] getMissionNames() {
        return this.missionNames;
    }

    public String getRandomMissionName() {
        return this.missionNames[Server.rand.nextInt(this.missionNames.length)];
    }

    public static final long getTimeReductionForMission(byte missionType, int missionDifficulty) {
        long toReturn = 14400000L;
        return toReturn += 0x6DDD00L * (long)missionDifficulty;
    }
}


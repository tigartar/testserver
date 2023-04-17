/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.ValreiFight;
import com.wurmonline.server.epic.ValreiFightHistory;
import com.wurmonline.server.epic.ValreiFightHistoryManager;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WCValreiMapUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapHex
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(MapHex.class.getName());
    private final int id;
    private final int type;
    private final String name;
    private final float moveCost;
    private String presenceStringOne = " is in ";
    private String prepositionString = " in ";
    private String leavesStringOne = " leaves ";
    private static final Random rand = new Random();
    private final LinkedList<Integer> nearHexes = new LinkedList();
    private final LinkedList<EpicEntity> entities = new LinkedList();
    private final Set<EpicEntity> visitedBy = new HashSet<EpicEntity>();
    private long spawnEntityId = 0L;
    private long homeEntityId = 0L;
    public static final int TYPE_STANDARD = 0;
    public static final int TYPE_TRAP = 1;
    public static final int TYPE_SLOW = 2;
    public static final int TYPE_ENHANCE_STRENGTH = 3;
    public static final int TYPE_ENHANCE_VITALITY = 4;
    public static final int TYPE_TELEPORT = 5;
    private final HexMap myMap;
    private static final String addVisitedBy = "INSERT INTO VISITED(ENTITYID,HEXID) VALUES (?,?)";
    private static final String clearVisitedHex = "DELETE FROM VISITED WHERE HEXID=?";

    MapHex(HexMap map, int hexNumber, String hexName, float hexMoveCost, int hexType) {
        this.id = hexNumber;
        this.name = hexName;
        this.moveCost = Math.max(0.5f, hexMoveCost);
        this.type = hexType;
        this.myMap = map;
        map.addMapHex(this);
    }

    public final int getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    final String getEnemyStatus(EpicEntity entity) {
        StringBuilder build = new StringBuilder();
        if (entity.isCollectable() || entity.isSource()) {
            return "";
        }
        for (EpicEntity e : this.entities) {
            if (e == entity || e.isCollectable() || e.isSource()) continue;
            if (e.isWurm()) {
                if (build.length() > 0) {
                    build.append(' ');
                }
                build.append(entity.getName() + " is battling the Wurm.");
            } else if (e.isSentinelMonster()) {
                if (build.length() > 0) {
                    build.append(' ');
                }
                build.append(entity.getName() + " is trying to defeat the " + e.getName() + ".");
            } else if (e.isEnemy(entity)) {
                if (build.length() > 0) {
                    build.append(' ');
                }
                build.append(entity.getName() + " is fighting " + e.getName() + ".");
            } else if (entity.getCompanion() == e) {
                if (build.length() > 0) {
                    build.append(' ');
                }
                build.append(entity.getName() + " is meeting with " + e.getName() + ".");
            }
            if (!e.isAlly()) continue;
            if (build.length() > 0) {
                build.append(' ');
            }
            build.append(entity.getName() + " visits the " + e.getName() + ".");
        }
        return build.toString();
    }

    long getSpawnEntityId() {
        return this.spawnEntityId;
    }

    long getHomeEntityId() {
        return this.homeEntityId;
    }

    final String getOwnPresenceString() {
        return " is home" + this.getFullPrepositionString();
    }

    final String getFullPresenceString() {
        return this.getPresenceStringOne() + this.name + ".";
    }

    final String getFullPrepositionString() {
        return this.getPrepositionString() + this.name + ".";
    }

    final float getMoveCost() {
        return this.moveCost;
    }

    HexMap getMyMap() {
        return this.myMap;
    }

    final void setPresenceStringOne(String ps) {
        this.presenceStringOne = ps;
    }

    final String getPresenceStringOne() {
        return this.presenceStringOne;
    }

    final void setPrepositionString(String ps) {
        this.prepositionString = ps;
    }

    final String getPrepositionString() {
        return this.prepositionString;
    }

    final void setLeavesStringOne(String ps) {
        this.leavesStringOne = ps;
    }

    final String getLeavesStringOne() {
        return this.leavesStringOne;
    }

    final int getType() {
        return this.type;
    }

    final void addEntity(EpicEntity entity) {
        if (!this.entities.contains(entity)) {
            this.entities.add(entity);
            entity.setMapHex(this);
            if (entity.isWurm() || entity.isDeity()) {
                if (entity.getAttack() > entity.getInitialAttack()) {
                    entity.setAttack(entity.getAttack() - 0.1f);
                }
                if (entity.getVitality() > entity.getInitialVitality()) {
                    entity.setVitality(entity.getVitality() - 0.1f);
                } else if (entity.getVitality() < entity.getInitialVitality()) {
                    entity.setVitality(entity.getVitality() + 0.1f);
                }
            } else if (entity.isCollectable() || entity.isSource()) {
                this.clearVisitedBy();
            }
        }
    }

    final void removeEntity(EpicEntity entity, boolean load) {
        if (this.entities.contains(entity)) {
            this.entities.remove(entity);
            entity.setMapHex(null);
        }
    }

    boolean checkLeaveStatus(EpicEntity entity) {
        return this.setEntityEffects(entity);
    }

    public final Integer[] getNearMapHexes() {
        return this.nearHexes.toArray(new Integer[this.nearHexes.size()]);
    }

    final void addNearHex(int hexId) {
        this.nearHexes.add(hexId);
    }

    final void addNearHexes(int hexId1, int hexId2, int hexId3, int hexId4, int hexId5, int hexId6) {
        this.nearHexes.add(hexId1);
        this.nearHexes.add(hexId2);
        this.nearHexes.add(hexId3);
        this.nearHexes.add(hexId4);
        this.nearHexes.add(hexId5);
        this.nearHexes.add(hexId6);
    }

    final boolean isVisitedBy(EpicEntity entity) {
        for (EpicEntity ent : this.entities) {
            if (!ent.isCollectable() && !ent.isSource()) continue;
            return false;
        }
        return this.visitedBy.contains(entity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void addVisitedBy(EpicEntity entity, boolean load) {
        if (this.visitedBy != null && !this.visitedBy.contains(entity)) {
            this.visitedBy.add(entity);
            if (!load) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getDeityDbCon();
                    ps = dbcon.prepareStatement(addVisitedBy);
                    ps.setLong(1, entity.getId());
                    ps.setInt(2, this.getId());
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, sqx.getMessage(), sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final void clearVisitedBy() {
        this.visitedBy.clear();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(clearVisitedHex);
            ps.setInt(1, this.getId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    LinkedList<Integer> cloneNearHexes() {
        LinkedList<Integer> clone = new LinkedList<Integer>();
        for (Integer i : this.nearHexes) {
            clone.add(i);
        }
        return clone;
    }

    final boolean containsWurm() {
        for (EpicEntity e : this.entities) {
            if (!e.isWurm()) continue;
            return true;
        }
        return false;
    }

    final boolean containsEnemy(EpicEntity toCheck) {
        for (EpicEntity e : this.entities) {
            if (!e.isEnemy(toCheck)) continue;
            return true;
        }
        return false;
    }

    final boolean containsMonsterOrHelper() {
        for (EpicEntity e : this.entities) {
            if (!e.isSentinelMonster() && !e.isAlly()) continue;
            return true;
        }
        return false;
    }

    final boolean containsDeity() {
        for (EpicEntity e : this.entities) {
            if (!e.isDeity()) continue;
            return true;
        }
        return false;
    }

    boolean mayEnter(EpicEntity entity) {
        if (entity.isWurm() && this.containsMonsterOrHelper()) {
            return this.containsDeity();
        }
        return true;
    }

    int getNextHexToWinPoint(EpicEntity entity) {
        if (entity.mustReturnHomeToWin()) {
            MapHex home = this.myMap.getSpawnHex(entity);
            if (home != null && home != this) {
                return this.findClosestHexTo(home.getId(), entity, true);
            }
            return this.getId();
        }
        return this.findClosestHexTo(this.myMap.getHexNumRequiredToWin(), entity, true);
    }

    int findClosestHexTo(int target, EpicEntity entity, boolean avoidEnemies) {
        logger.log(Level.INFO, entity.getName() + " at " + this.getId() + " pathing to " + target);
        HashMap<Integer, Integer> steps = new HashMap<Integer, Integer>();
        LinkedList<Integer> copy = this.cloneNearHexes();
        while (copy.size() > 0) {
            Integer i = copy.remove(rand.nextInt(copy.size()));
            if (i == target) {
                return target;
            }
            MapHex hex = this.myMap.getMapHex(i);
            if (!hex.mayEnter(entity) || avoidEnemies && hex.containsEnemy(entity)) continue;
            HashSet<Integer> checked = new HashSet<Integer>();
            checked.add(i);
            int numSteps = this.findNextHex(checked, hex, target, entity, avoidEnemies, 0);
            steps.put(hex.getId(), numSteps);
        }
        int minSteps = 100;
        int hexNum = 0;
        for (Map.Entry entry : steps.entrySet()) {
            int csteps = (Integer)entry.getValue();
            if (csteps >= minSteps) continue;
            minSteps = csteps;
            hexNum = (Integer)entry.getKey();
        }
        return hexNum;
    }

    int findNextHex(Set<Integer> checked, MapHex startHex, int targetHexId, EpicEntity entity, boolean avoidEnemies, int counter) {
        LinkedList<Integer> nearClone = startHex.cloneNearHexes();
        int minNum = 100;
        while (nearClone.size() > 0) {
            int steps;
            MapHex nearhex;
            Integer ni = nearClone.remove(rand.nextInt(nearClone.size()));
            if (ni == targetHexId) {
                return counter;
            }
            if (checked.contains(ni)) continue;
            checked.add(ni);
            if (counter >= 6 || !(nearhex = this.myMap.getMapHex(ni)).mayEnter(entity) || avoidEnemies && nearhex.containsEnemy(entity) || (steps = this.findNextHex(checked, nearhex, targetHexId, entity, avoidEnemies, ++counter)) >= minNum) continue;
            minNum = steps;
        }
        return minNum;
    }

    int findNextHex(EpicEntity entity) {
        MapHex hex;
        Integer i;
        if (this.nearHexes.isEmpty()) {
            logger.log(Level.WARNING, "Near hexes is empty for map " + this.getId());
            return 0;
        }
        if (entity.hasEnoughCollectablesToWin()) {
            if (this.getId() == this.myMap.getHexNumRequiredToWin()) {
                return this.getId();
            }
            return this.getNextHexToWinPoint(entity);
        }
        LinkedList<Integer> copy = this.cloneNearHexes();
        while (copy.size() > 0) {
            i = copy.remove(rand.nextInt(copy.size()));
            hex = this.myMap.getMapHex(i);
            if (!hex.mayEnter(entity)) continue;
            if (entity.isWurm()) {
                return hex.getId();
            }
            if (hex.isVisitedBy(entity)) continue;
            return hex.getId();
        }
        copy = this.cloneNearHexes();
        while (copy.size() > 0) {
            i = copy.remove(rand.nextInt(copy.size()));
            hex = this.myMap.getMapHex(i);
            if (!hex.mayEnter(entity)) continue;
            LinkedList<Integer> nearClone = hex.cloneNearHexes();
            while (nearClone.size() > 0) {
                Integer ni = nearClone.remove(rand.nextInt(nearClone.size()));
                MapHex nearhex = this.myMap.getMapHex(ni);
                if (nearhex.isVisitedBy(entity)) continue;
                return hex.getId();
            }
        }
        copy = this.cloneNearHexes();
        while (copy.size() > 0) {
            i = copy.remove(rand.nextInt(copy.size()));
            hex = this.myMap.getMapHex(i);
            if (!hex.mayEnter(entity)) continue;
            return i;
        }
        logger.log(Level.INFO, entity.getName() + " Failed to take random step to neighbour.");
        return 0;
    }

    public boolean isTrap() {
        return this.type == 1;
    }

    public boolean isTeleport() {
        return this.type == 5;
    }

    public boolean isSlow() {
        return this.type == 2;
    }

    int getSlowModifier() {
        return this.isSlow() ? 2 : 1;
    }

    private final boolean resolveDispute(EpicEntity entity) {
        EpicEntity enemy = null;
        for (EpicEntity e : this.entities) {
            if (e == entity || !e.isEnemy(entity)) continue;
            if (enemy == null) {
                enemy = e;
                continue;
            }
            if (!Server.rand.nextBoolean()) continue;
            enemy = e;
        }
        if (enemy == null) {
            return true;
        }
        ValreiFight vFight = new ValreiFight(this, entity, enemy);
        ValreiFightHistory fightHistory = vFight.completeFight(false);
        ValreiFightHistoryManager.getInstance().addFight(fightHistory.getFightId(), fightHistory);
        if (Servers.localServer.LOGINSERVER) {
            WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), 5);
            updater.sendFromLoginServer();
        }
        if (fightHistory.getFightWinner() == entity.getId()) {
            this.fightEndEffects(entity, enemy);
            return true;
        }
        this.fightEndEffects(enemy, entity);
        return false;
    }

    private final void fightEndEffects(EpicEntity winner, EpicEntity loser) {
        if (loser.isWurm()) {
            winner.broadCastWithName(" wards off " + loser.getName() + this.getFullPrepositionString());
        } else if (winner.isWurm()) {
            loser.broadCastWithName(" is defeated by " + winner.getName() + this.getFullPrepositionString());
        } else if (loser.isSentinelMonster()) {
            winner.broadCastWithName(" prevails against " + loser.getName() + this.getFullPrepositionString());
        } else {
            loser.broadCastWithName(" is vanquished by " + winner.getName() + this.getFullPrepositionString());
        }
        loser.dropAll(winner.isDemigod());
        this.removeEntity(loser, false);
        this.addVisitedBy(loser, false);
        if (loser.isDemigod()) {
            this.myMap.destroyEntity(loser);
        }
    }

    private final boolean resolveDisputeDeprecated(EpicEntity entity) {
        EpicEntity enemy = null;
        EpicEntity enemy2 = null;
        EpicEntity helper = null;
        EpicEntity friend = null;
        for (EpicEntity e : this.entities) {
            if (e == entity) continue;
            if (e.isEnemy(entity)) {
                if (enemy == null) {
                    enemy = e;
                } else {
                    enemy2 = e;
                }
            } else if (e.isAlly() && e.isFriend(entity)) {
                helper = e;
            }
            if (!e.isDeity() && !e.isDemigod() && !entity.isFriend(e)) continue;
            friend = e;
        }
        if (friend != null && friend.countCollectables() > 0 && entity.countCollectables() > 0 && entity.isDeity()) {
            friend.giveCollectables(entity);
        }
        if (enemy != null) {
            while (true) {
                if (enemy != null) {
                    if (this.attack(enemy, entity)) {
                        return false;
                    }
                    if (this.attack(entity, enemy)) {
                        enemy = null;
                        if (enemy2 == null) {
                            return true;
                        }
                    }
                    if (helper != null && this.attack(helper, enemy)) {
                        enemy = null;
                        if (enemy2 == null) {
                            return true;
                        }
                    }
                }
                if (enemy2 == null) continue;
                if (this.attack(entity, enemy2)) {
                    enemy2 = null;
                    if (enemy != null) continue;
                    return true;
                }
                if (this.attack(enemy2, entity)) break;
            }
            return false;
        }
        return true;
    }

    private final boolean attack(EpicEntity entity, EpicEntity enemy) {
        if (entity.rollAttack() && enemy.setVitality(enemy.getVitality() - 1.0f)) {
            if (enemy.isWurm()) {
                entity.broadCastWithName(" wards off " + enemy.getName() + this.getFullPrepositionString());
            } else if (entity.isWurm()) {
                enemy.broadCastWithName(" is defeated by " + entity.getName() + this.getFullPrepositionString());
            } else if (enemy.isSentinelMonster()) {
                entity.broadCastWithName(" prevails against " + enemy.getName() + this.getFullPrepositionString());
            } else {
                enemy.broadCastWithName(" is vanquished by " + entity.getName() + this.getFullPrepositionString());
            }
            enemy.dropAll(entity.isDemigod());
            this.removeEntity(enemy, false);
            this.addVisitedBy(enemy, false);
            if (enemy.isDemigod()) {
                this.myMap.destroyEntity(enemy);
            }
            return true;
        }
        return false;
    }

    protected final String getCollectibleName() {
        ListIterator lit = this.entities.listIterator();
        while (lit.hasNext()) {
            EpicEntity next = (EpicEntity)lit.next();
            if (!next.isCollectable()) continue;
            return next.getName();
        }
        return "";
    }

    protected final int countCollectibles() {
        int toret = 0;
        ListIterator lit = this.entities.listIterator();
        while (lit.hasNext()) {
            EpicEntity next = (EpicEntity)lit.next();
            if (!next.isCollectable()) continue;
            ++toret;
        }
        return toret;
    }

    private final void pickupStuff(EpicEntity entity) {
        ListIterator lit = this.entities.listIterator();
        while (lit.hasNext()) {
            EpicEntity next = (EpicEntity)lit.next();
            if (!next.isCollectable() && !next.isSource()) continue;
            entity.logWithName(" found " + next.getName() + ".");
            lit.remove();
            next.setMapHex(null);
            next.setCarrier(entity, true, false, false);
        }
    }

    public boolean isStrength() {
        return this.type == 3;
    }

    public boolean isVitality() {
        return this.type == 4;
    }

    final boolean setEntityEffects(EpicEntity entity) {
        if (this.resolveDispute(entity)) {
            switch (this.type) {
                case 1: {
                    break;
                }
                case 2: {
                    break;
                }
                case 3: {
                    if (!entity.isDeity() && !entity.isWurm()) break;
                    float current = entity.getCurrentSkill(102);
                    entity.setSkill(102, current + (100.0f - current) / 1250.0f);
                    current = entity.getCurrentSkill(104);
                    entity.setSkill(104, current + (100.0f - current) / 1250.0f);
                    current = entity.getCurrentSkill(105);
                    entity.setSkill(105, current + (100.0f - current) / 1250.0f);
                    entity.broadCastWithName(" is strengthened by the influence of " + this.getName() + ".");
                    break;
                }
                case 4: {
                    if (!entity.isDeity() && !entity.isWurm()) break;
                    float current = entity.getCurrentSkill(100);
                    entity.setSkill(100, current + (100.0f - current) / 1250.0f);
                    current = entity.getCurrentSkill(103);
                    entity.setSkill(103, current + (100.0f - current) / 1250.0f);
                    current = entity.getCurrentSkill(101);
                    entity.setSkill(101, current + (100.0f - current) / 1250.0f);
                    entity.broadCastWithName(" is vitalized by the influence of " + this.getName() + ".");
                    break;
                }
                case 5: {
                    break;
                }
            }
            entity.setVitality(Math.max(entity.getInitialVitality() / 2.0f, entity.getVitality()), false);
            this.pickupStuff(entity);
            this.addVisitedBy(entity, false);
            return true;
        }
        return false;
    }

    long getEntitySpawn() {
        return this.spawnEntityId;
    }

    boolean isSpawnFor(long entityId) {
        return this.spawnEntityId == entityId;
    }

    void setSpawnEntityId(long entityId) {
        this.spawnEntityId = entityId;
    }

    boolean isSpawn() {
        return this.spawnEntityId != 0L;
    }

    boolean isHomeFor(long entityId) {
        return this.homeEntityId == entityId;
    }

    void setHomeEntityId(long entityId) {
        this.homeEntityId = entityId;
    }
}


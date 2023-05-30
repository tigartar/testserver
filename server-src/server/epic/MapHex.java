package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapHex implements MiscConstants {
   private static final Logger logger = Logger.getLogger(MapHex.class.getName());
   private final int id;
   private final int type;
   private final String name;
   private final float moveCost;
   private String presenceStringOne = " is in ";
   private String prepositionString = " in ";
   private String leavesStringOne = " leaves ";
   private static final Random rand = new Random();
   private final LinkedList<Integer> nearHexes = new LinkedList<>();
   private final LinkedList<EpicEntity> entities = new LinkedList<>();
   private final Set<EpicEntity> visitedBy = new HashSet<>();
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
      this.moveCost = Math.max(0.5F, hexMoveCost);
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
      if (!entity.isCollectable() && !entity.isSource()) {
         for(EpicEntity e : this.entities) {
            if (e != entity && !e.isCollectable() && !e.isSource()) {
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

               if (e.isAlly()) {
                  if (build.length() > 0) {
                     build.append(' ');
                  }

                  build.append(entity.getName() + " visits the " + e.getName() + ".");
               }
            }
         }

         return build.toString();
      } else {
         return "";
      }
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
               entity.setAttack(entity.getAttack() - 0.1F);
            }

            if (entity.getVitality() > entity.getInitialVitality()) {
               entity.setVitality(entity.getVitality() - 0.1F);
            } else if (entity.getVitality() < entity.getInitialVitality()) {
               entity.setVitality(entity.getVitality() + 0.1F);
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
      for(EpicEntity ent : this.entities) {
         if (ent.isCollectable() || ent.isSource()) {
            return false;
         }
      }

      return this.visitedBy.contains(entity);
   }

   final void addVisitedBy(EpicEntity entity, boolean load) {
      if (this.visitedBy != null && !this.visitedBy.contains(entity)) {
         this.visitedBy.add(entity);
         if (!load) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               dbcon = DbConnector.getDeityDbCon();
               ps = dbcon.prepareStatement("INSERT INTO VISITED(ENTITYID,HEXID) VALUES (?,?)");
               ps.setLong(1, entity.getId());
               ps.setInt(2, this.getId());
               ps.executeUpdate();
            } catch (SQLException var9) {
               logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      }
   }

   final void clearVisitedBy() {
      this.visitedBy.clear();
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VISITED WHERE HEXID=?");
         ps.setInt(1, this.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   LinkedList<Integer> cloneNearHexes() {
      LinkedList<Integer> clone = new LinkedList<>();

      for(Integer i : this.nearHexes) {
         clone.add(i);
      }

      return clone;
   }

   final boolean containsWurm() {
      for(EpicEntity e : this.entities) {
         if (e.isWurm()) {
            return true;
         }
      }

      return false;
   }

   final boolean containsEnemy(EpicEntity toCheck) {
      for(EpicEntity e : this.entities) {
         if (e.isEnemy(toCheck)) {
            return true;
         }
      }

      return false;
   }

   final boolean containsMonsterOrHelper() {
      for(EpicEntity e : this.entities) {
         if (e.isSentinelMonster() || e.isAlly()) {
            return true;
         }
      }

      return false;
   }

   final boolean containsDeity() {
      for(EpicEntity e : this.entities) {
         if (e.isDeity()) {
            return true;
         }
      }

      return false;
   }

   boolean mayEnter(EpicEntity entity) {
      return entity.isWurm() && this.containsMonsterOrHelper() ? this.containsDeity() : true;
   }

   int getNextHexToWinPoint(EpicEntity entity) {
      if (entity.mustReturnHomeToWin()) {
         MapHex home = this.myMap.getSpawnHex(entity);
         return home != null && home != this ? this.findClosestHexTo(home.getId(), entity, true) : this.getId();
      } else {
         return this.findClosestHexTo(this.myMap.getHexNumRequiredToWin(), entity, true);
      }
   }

   int findClosestHexTo(int target, EpicEntity entity, boolean avoidEnemies) {
      logger.log(Level.INFO, entity.getName() + " at " + this.getId() + " pathing to " + target);
      Map<Integer, Integer> steps = new HashMap<>();
      LinkedList<Integer> copy = this.cloneNearHexes();

      while(copy.size() > 0) {
         Integer i = copy.remove(rand.nextInt(copy.size()));
         if (i == target) {
            return target;
         }

         MapHex hex = this.myMap.getMapHex(i);
         if (hex.mayEnter(entity) && (!avoidEnemies || !hex.containsEnemy(entity))) {
            Set<Integer> checked = new HashSet<>();
            checked.add(i);
            int numSteps = this.findNextHex(checked, hex, target, entity, avoidEnemies, 0);
            steps.put(hex.getId(), numSteps);
         }
      }

      int minSteps = 100;
      int hexNum = 0;

      for(Entry<Integer, Integer> entry : steps.entrySet()) {
         int csteps = entry.getValue();
         if (csteps < minSteps) {
            minSteps = csteps;
            hexNum = entry.getKey();
         }
      }

      return hexNum;
   }

   int findNextHex(Set<Integer> checked, MapHex startHex, int targetHexId, EpicEntity entity, boolean avoidEnemies, int counter) {
      LinkedList<Integer> nearClone = startHex.cloneNearHexes();
      int minNum = 100;

      while(nearClone.size() > 0) {
         Integer ni = nearClone.remove(rand.nextInt(nearClone.size()));
         if (ni == targetHexId) {
            return counter;
         }

         if (!checked.contains(ni)) {
            checked.add(ni);
            if (counter < 6) {
               MapHex nearhex = this.myMap.getMapHex(ni);
               if (nearhex.mayEnter(entity) && (!avoidEnemies || !nearhex.containsEnemy(entity))) {
                  int steps = this.findNextHex(checked, nearhex, targetHexId, entity, avoidEnemies, ++counter);
                  if (steps < minNum) {
                     minNum = steps;
                  }
               }
            }
         }
      }

      return minNum;
   }

   int findNextHex(EpicEntity entity) {
      if (this.nearHexes.isEmpty()) {
         logger.log(Level.WARNING, "Near hexes is empty for map " + this.getId());
         return 0;
      } else if (entity.hasEnoughCollectablesToWin()) {
         return this.getId() == this.myMap.getHexNumRequiredToWin() ? this.getId() : this.getNextHexToWinPoint(entity);
      } else {
         LinkedList<Integer> copy = this.cloneNearHexes();

         while(copy.size() > 0) {
            Integer i = copy.remove(rand.nextInt(copy.size()));
            MapHex hex = this.myMap.getMapHex(i);
            if (hex.mayEnter(entity)) {
               if (entity.isWurm()) {
                  return hex.getId();
               }

               if (!hex.isVisitedBy(entity)) {
                  return hex.getId();
               }
            }
         }

         copy = this.cloneNearHexes();

         while(copy.size() > 0) {
            Integer i = copy.remove(rand.nextInt(copy.size()));
            MapHex hex = this.myMap.getMapHex(i);
            if (hex.mayEnter(entity)) {
               LinkedList<Integer> nearClone = hex.cloneNearHexes();

               while(nearClone.size() > 0) {
                  Integer ni = nearClone.remove(rand.nextInt(nearClone.size()));
                  MapHex nearhex = this.myMap.getMapHex(ni);
                  if (!nearhex.isVisitedBy(entity)) {
                     return hex.getId();
                  }
               }
            }
         }

         copy = this.cloneNearHexes();

         while(copy.size() > 0) {
            Integer i = copy.remove(rand.nextInt(copy.size()));
            MapHex hex = this.myMap.getMapHex(i);
            if (hex.mayEnter(entity)) {
               return i;
            }
         }

         logger.log(Level.INFO, entity.getName() + " Failed to take random step to neighbour.");
         return 0;
      }
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

      for(EpicEntity e : this.entities) {
         if (e != entity && e.isEnemy(entity)) {
            if (enemy == null) {
               enemy = e;
            } else if (Server.rand.nextBoolean()) {
               enemy = e;
            }
         }
      }

      if (enemy == null) {
         return true;
      } else {
         ValreiFight vFight = new ValreiFight(this, entity, enemy);
         ValreiFightHistory fightHistory = vFight.completeFight(false);
         ValreiFightHistoryManager.getInstance().addFight(fightHistory.getFightId(), fightHistory);
         if (Servers.localServer.LOGINSERVER) {
            WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)5);
            updater.sendFromLoginServer();
         }

         if (fightHistory.getFightWinner() == entity.getId()) {
            this.fightEndEffects(entity, enemy);
            return true;
         } else {
            this.fightEndEffects(enemy, entity);
            return false;
         }
      }
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

      for(EpicEntity e : this.entities) {
         if (e != entity) {
            if (e.isEnemy(entity)) {
               if (enemy == null) {
                  enemy = e;
               } else {
                  enemy2 = e;
               }
            } else if (e.isAlly() && e.isFriend(entity)) {
               helper = e;
            }

            if (e.isDeity() || e.isDemigod() || entity.isFriend(e)) {
               friend = e;
            }
         }
      }

      if (friend != null && friend.countCollectables() > 0 && entity.countCollectables() > 0 && entity.isDeity()) {
         friend.giveCollectables(entity);
      }

      if (enemy != null) {
         while(true) {
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

            if (enemy2 != null) {
               if (this.attack(entity, enemy2)) {
                  enemy2 = null;
                  if (enemy == null) {
                     return true;
                  }
               } else if (this.attack(enemy2, entity)) {
                  return false;
               }
            }
         }
      } else {
         return true;
      }
   }

   private final boolean attack(EpicEntity entity, EpicEntity enemy) {
      if (entity.rollAttack() && enemy.setVitality(enemy.getVitality() - 1.0F)) {
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
      } else {
         return false;
      }
   }

   protected final String getCollectibleName() {
      for(EpicEntity next : this.entities) {
         if (next.isCollectable()) {
            return next.getName();
         }
      }

      return "";
   }

   protected final int countCollectibles() {
      int toret = 0;

      for(EpicEntity next : this.entities) {
         if (next.isCollectable()) {
            ++toret;
         }
      }

      return toret;
   }

   private final void pickupStuff(EpicEntity entity) {
      ListIterator<EpicEntity> lit = this.entities.listIterator();

      while(lit.hasNext()) {
         EpicEntity next = lit.next();
         if (next.isCollectable() || next.isSource()) {
            entity.logWithName(" found " + next.getName() + ".");
            lit.remove();
            next.setMapHex(null);
            next.setCarrier(entity, true, false, false);
         }
      }
   }

   public boolean isStrength() {
      return this.type == 3;
   }

   public boolean isVitality() {
      return this.type == 4;
   }

   final boolean setEntityEffects(EpicEntity entity) {
      if (!this.resolveDispute(entity)) {
         return false;
      } else {
         switch(this.type) {
            case 1:
            case 2:
            case 5:
            default:
               break;
            case 3:
               if (entity.isDeity() || entity.isWurm()) {
                  float current = entity.getCurrentSkill(102);
                  entity.setSkill(102, current + (100.0F - current) / 1250.0F);
                  current = entity.getCurrentSkill(104);
                  entity.setSkill(104, current + (100.0F - current) / 1250.0F);
                  current = entity.getCurrentSkill(105);
                  entity.setSkill(105, current + (100.0F - current) / 1250.0F);
                  entity.broadCastWithName(" is strengthened by the influence of " + this.getName() + ".");
               }
               break;
            case 4:
               if (entity.isDeity() || entity.isWurm()) {
                  float current = entity.getCurrentSkill(100);
                  entity.setSkill(100, current + (100.0F - current) / 1250.0F);
                  current = entity.getCurrentSkill(103);
                  entity.setSkill(103, current + (100.0F - current) / 1250.0F);
                  current = entity.getCurrentSkill(101);
                  entity.setSkill(101, current + (100.0F - current) / 1250.0F);
                  entity.broadCastWithName(" is vitalized by the influence of " + this.getName() + ".");
               }
         }

         entity.setVitality(Math.max(entity.getInitialVitality() / 2.0F, entity.getVitality()), false);
         this.pickupStuff(entity);
         this.addVisitedBy(entity, false);
         return true;
      }
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

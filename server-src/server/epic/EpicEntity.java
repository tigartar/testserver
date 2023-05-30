package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcCreateEpicMission;
import com.wurmonline.server.webinterface.WcEpicStatusReport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EpicEntity implements MiscConstants, TimeConstants {
   private static final String CREATE_ENTITY = "INSERT INTO ENTITIES (ID,NAME,SPAWNPOINT,ENTITYTYPE,ATTACK,VITALITY,INATTACK,INVITALITY,CARRIER) VALUES (?,?,?,?,?,?,?,?,?)";
   private static final String CREATE_ENTITY_SKILLS = "INSERT INTO ENTITYSKILLS (ENTITYID,SKILLID,DEFAULTVAL,CURRENTVAL) VALUES (?,?,?,?)";
   private static final String UPDATE_ENTITY_SKILLS = "UPDATE ENTITYSKILLS SET DEFAULTVAL=?,CURRENTVAL=? WHERE ENTITYID=? AND SKILLID=?";
   private static final String UPDATE_ENTITY_COMPANION = "UPDATE ENTITIES SET COMPANION=? WHERE ID=?";
   private static final String UPDATE_ENTITY_DEMIGODPLUS = "UPDATE ENTITIES SET DEMIGODPLUS=? WHERE ID=?";
   private static final String UPDATE_ENTITY_CARRIER = "UPDATE ENTITIES SET CARRIER=? WHERE ID=?";
   private static final String UPDATE_ENTITY_POWERVIT = "UPDATE ENTITIES SET ATTACK=?,VITALITY=?,INATTACK=?,INVITALITY=? WHERE ID=?";
   private static final String UPDATE_ENTITY_HEX = "UPDATE ENTITIES SET CURRENTHEX=?,HELPED=?,ENTERED=?,LEAVING=?,TARGETHEX=? WHERE ID=?";
   private static final String UPDATE_ENTITY_TYPE = "UPDATE ENTITIES SET ENTITYTYPE=? WHERE ID=?";
   private static final String DELETE_ENTITY = "DELETE FROM ENTITIES WHERE ID=?";
   private static final Logger logger = Logger.getLogger(EpicEntity.class.getName());
   static final int TYPE_DEITY = 0;
   public static final int TYPE_SOURCE = 1;
   public static final int TYPE_COLLECT = 2;
   static final int TYPE_WURM = 4;
   public static final int TYPE_MONSTER_SENTINEL = 5;
   public static final int TYPE_ALLY = 6;
   public static final int TYPE_DEMIGOD = 7;
   static final long MIN_TIME_PER_HEX = 7200000L;
   static final long MOVE_TIME_PER_HEX = 60000L;
   static final long MIN_TIME_TRAPPED = 86400000L;
   static final long MAX_TIME_TRAPPED = 518400000L;
   private static final int HELPED_TIME_MODIFIER = 1;
   static final long MISSION_TIME_EFFECT = 43200000L;
   private static final int NOT_HELPED_TIME_MODIFIER = 12;
   private static final Random RAND = new Random();
   private boolean headingHome = false;
   private static final int DIEROLL = 20;
   private final String name;
   private final long identifier;
   private int type = 0;
   private boolean helped = false;
   private String collName = "";
   private long enteredCurrentHex = 0L;
   private long timeUntilLeave = 0L;
   private boolean shouldCreateMission = false;
   private boolean succeedLastMission = false;
   private int targetHex = 0;
   private float attack = 0.0F;
   private float vitality = 0.0F;
   private float initialAttack = 0.0F;
   private float initialVitality = 0.0F;
   private MapHex hex = null;
   private HexMap myMap = null;
   private EpicEntity carrier = null;
   private EpicEntity companion = null;
   private int steps = 0;
   private byte demigodsToAppoint = 0;
   private static final int TWELVE_HOURS = 43200000;
   private static final int TWENTY_HOURS = 72000000;
   private static final int LEAVE_TIME = 259200000;
   private long nextSpawnedCreatures = System.currentTimeMillis() + 43200000L + (long)new Random().nextInt(43200000);
   private boolean dirtyVitality = false;
   private final List<EpicEntity> entities = new ArrayList<>();
   private static boolean dumpToXML = true;
   private long nextHeal = System.currentTimeMillis() + 3600000L;
   private WcCreateEpicMission lastSentWCC;
   private final Set<Integer> serversFailed = new HashSet<>();
   private int latestMissionDifficulty = -10;
   private HashMap<Integer, EpicEntity.SkillVal> skills = new HashMap<>();

   EpicEntity(HexMap map, long id, String entityName, int entityType) {
      this.identifier = id;
      this.name = entityName;
      this.type = entityType;
      this.setHexMap(map);
   }

   EpicEntity(HexMap map, long id, String entityName, int entityType, float entityInitialAttack, float entityInitialVitality) {
      this(map, id, entityName, entityType, entityInitialAttack, entityInitialVitality, false, 0L, System.currentTimeMillis() + 259200000L, -1);
   }

   public static final void toggleXmlDump(boolean dump) {
      dumpToXML = dump;
   }

   EpicEntity(
      HexMap map,
      long id,
      String entityName,
      int entityType,
      float entityInitialAttack,
      float entityInitialVitality,
      boolean isHelped,
      long enterTime,
      long leaveTime,
      int targetH
   ) {
      this.identifier = id;
      this.name = entityName;
      this.type = entityType;
      this.initialAttack = entityInitialAttack;
      this.attack = this.initialAttack;
      this.initialVitality = entityInitialVitality;
      this.vitality = this.initialVitality;
      this.helped = isHelped;
      this.enteredCurrentHex = enterTime;
      this.timeUntilLeave = leaveTime;
      this.targetHex = targetH;
      this.setHexMap(map);
   }

   public void setLatestMissionDifficulty(int em) {
      this.latestMissionDifficulty = em;
   }

   public int getLatestMissionDifficulty() {
      return this.latestMissionDifficulty;
   }

   void setHexMap(HexMap newMap) {
      if (this.myMap != null) {
         this.myMap.removeEntity(this);
      }

      this.myMap = newMap;
      if (this.myMap != null) {
         this.myMap.addEntity(this);
      }
   }

   public final long getId() {
      return this.identifier;
   }

   public String getName() {
      return this.name;
   }

   void setType(int newType) {
      this.type = newType;
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("UPDATE ENTITIES SET ENTITYTYPE=? WHERE ID=?");
         ps.setInt(1, this.type);
         ps.setLong(2, this.getId());
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   EpicEntity getDemiGod() {
      return this.myMap.getDemiGodFor(this);
   }

   public final int getType() {
      return this.type;
   }

   public final boolean isDeity() {
      return this.type == 0;
   }

   public final String getCollectibleName() {
      return this.collName;
   }

   public final boolean isDemigod() {
      return this.type == 7;
   }

   final boolean isSentinelMonster() {
      return this.type == 5;
   }

   final boolean isWurm() {
      return this.type == 4;
   }

   public final boolean isCollectable() {
      return this.type == 2;
   }

   final boolean isAlly() {
      return this.type == 6;
   }

   final void setCompanion(EpicEntity entity) {
      this.setCompanion(entity, false);
   }

   final void setCompanion(EpicEntity entity, boolean load) {
      if (this.companion != null) {
         logger.log(Level.WARNING, this.getName() + " replacing " + this.companion.getName() + " with " + entity.getName());
      }

      this.companion = entity;
      if (!load) {
         this.setCompanionForEntity(this.companion == null ? 0L : this.companion.getId());
      }
   }

   public final void addFailedServer(int serverId) {
      this.serversFailed.add(serverId);
      logger.log(Level.INFO, this.getName() + " adding failed server for epic mission creation command.");
   }

   public final void checkifServerFailed(int serverId) {
      if (this.lastSentWCC != null) {
         boolean remove = false;

         for(Integer id : this.serversFailed) {
            if (id == serverId && this.lastSentWCC != null) {
               LoginServerWebConnection lsw = new LoginServerWebConnection(serverId);
               lsw.sendWebCommand(this.lastSentWCC.getType(), this.lastSentWCC);
               logger.log(Level.INFO, this.getName() + " ... Server " + serverId + " has reconnected. Resent WCC!");
               remove = true;
            }
         }

         if (remove) {
            this.serversFailed.remove(serverId);
         }

         if (this.serversFailed.isEmpty()) {
            this.lastSentWCC = null;
         }
      }
   }

   protected void sendNewScenarioWebCommand(int difficulty) {
      if (this.myMap != null && this.isDeity()) {
         EpicMission oldmission = EpicServerStatus.getEpicMissionForEntity((int)this.getId());
         if (oldmission != null) {
            EpicServerStatus.deleteMission(oldmission);
         }

         int numberOfLoyalServers = Servers.getNumberOfLoyalServers((int)this.getId());
         if (!Servers.localServer.EPIC) {
            EpicMission mission = new EpicMission(
               (int)this.getId(),
               this.myMap.getScenarioNumber(),
               this.getName() + " waiting for help",
               this.myMap.getScenarioName(),
               (int)this.getId(),
               (byte)-10,
               difficulty,
               0.0F,
               numberOfLoyalServers,
               System.currentTimeMillis(),
               false,
               true
            );
            EpicServerStatus.addMission(mission);
            mission.setCurrent(true);
         }

         WcCreateEpicMission wce = new WcCreateEpicMission(
            WurmId.getNextWCCommandId(),
            this.myMap.getScenarioName(),
            this.myMap.getScenarioNumber(),
            this.myMap.getReasonAndEffectInt(),
            this.myMap.getCollictblesRequiredToWin(),
            this.myMap.getCollictblesRequiredForWurmToWin(),
            this.myMap.isSpawnPointRequiredToWin(),
            this.myMap.getHexNumRequiredToWin(),
            this.myMap.getScenarioQuestString() + ' ' + this.getLocationStatus() + ' ' + this.getEnemyStatus(),
            this.getId(),
            difficulty,
            this.getName(),
            (this.getTimeUntilLeave() - System.currentTimeMillis()) / 1000L,
            false
         );
         this.lastSentWCC = wce;
         wce.sendFromLoginServer();
      }
   }

   final void setDemigodsToAppoint(byte aDemigodsToAppoint) {
      this.demigodsToAppoint = aDemigodsToAppoint;
   }

   void setMapHex(MapHex mapHex) {
      if (mapHex != null) {
         this.broadCastWithName(" enters " + mapHex.getName());
      }

      this.setMapHex(mapHex, false);
   }

   int resetSteps() {
      int toReturn = this.steps;
      this.steps = 0;
      return toReturn;
   }

   protected void setMapHex(MapHex mapHex, boolean load) {
      if (mapHex != null && !mapHex.equals(this.hex)) {
         if (this.hex != null) {
            this.hex.removeEntity(this, load);
         }

         this.hex = mapHex;
         ++this.steps;
         this.setHelped(false, load);
         toggleXmlDump(false);
         this.hex.addEntity(this);
         if (!load) {
            this.setEnteredCurrentHex();
         }

         toggleXmlDump(true);
      } else if (mapHex == null) {
         if (this.hex != null) {
            toggleXmlDump(false);
            this.hex.removeEntity(this, load);
            toggleXmlDump(true);
         }

         this.hex = null;
         this.saveHexPos();
      }
   }

   public final void setHelped(boolean isHelped, boolean load) {
      this.helped = isHelped;
      if (!load) {
         this.saveHexPos();
      }
   }

   final float getHelpModifier() {
      if (this.isDeity()) {
         return this.helped ? 1.0F : 12.0F;
      } else {
         return 1.0F;
      }
   }

   public final long getTimeUntilLeave() {
      return this.hex != null ? this.timeUntilLeave : getMinTimePerHex();
   }

   public final long getTimeToNextHex() {
      if (this.targetHex > 0) {
         MapHex next = this.myMap.getMapHex(this.targetHex);
         if (next != null) {
            return (long)((float)this.getTimeUntilLeave() + 60000.0F * next.getMoveCost());
         }
      }

      return this.getTimeUntilLeave();
   }

   final void poll() {
      if (this.hex != null) {
         if (this.targetHex > 0 && System.currentTimeMillis() > this.getTimeUntilLeave()) {
            MapHex next = this.myMap.getMapHex(this.targetHex);
            if (next != null && System.currentTimeMillis() > this.getTimeToNextHex() && this.hex.checkLeaveStatus(this)) {
               if (this.hex.isTeleport()) {
                  next = this.myMap.getRandomHex();

                  while(!next.mayEnter(this)) {
                     next = this.myMap.getRandomHex();
                  }

                  this.targetHex = 0;
                  this.broadCastWithName(" shifts to " + next.getName() + ".");
               }

               this.setMapHex(next);
            }
         }
      } else if (!this.isCollectable() && !this.isSource()) {
         this.spawn();
      }

      if (!this.isCollectable() && !this.isSource() && System.currentTimeMillis() > this.nextHeal && this.getVitality() < this.getInitialVitality()) {
         this.setVitality(Math.min(this.getInitialVitality(), this.getVitality() + 1.0F));
         this.nextHeal = System.currentTimeMillis() + 72000000L;
      }

      if (this.isDeity() || this.isWurm()) {
         this.findNextTargetHex();
      }

      if ((this.isDeity() || this.isWurm()) && System.currentTimeMillis() > this.nextSpawnedCreatures) {
         int next = 72000000;
         if (this.myMap.spawnCreatures(this)) {
            next = 144000000;
         }

         this.nextSpawnedCreatures = System.currentTimeMillis() + 72000000L + (long)new Random().nextInt(next);
         logger.log(Level.INFO, this.getName() + " spawns creatures. Next in " + Server.getTimeFor(this.nextSpawnedCreatures - System.currentTimeMillis()));
      }

      if (this.dirtyVitality) {
         this.updateEntityVitality();
         this.dirtyVitality = false;
      }
   }

   final boolean setVitality(float newVitality) {
      return this.setVitality(newVitality, false);
   }

   public static final long getMinTimePerHex() {
      return 7200000L;
   }

   final boolean setVitality(float newVitality, boolean load) {
      if (this.initialVitality == 0.0F) {
         this.initialVitality = newVitality;
      }

      this.vitality = newVitality;
      if (!load && this.vitality > 0.0F) {
         this.dirtyVitality = true;
      }

      return this.vitality <= 0.0F;
   }

   final void permanentlyModifyVitality(float modifierVal) {
      this.vitality += modifierVal;
      this.updateEntityVitality();
   }

   final void permanentlyModifyAttack(float modifierVal) {
      this.attack += modifierVal;
      this.updateEntityVitality();
   }

   public final boolean isSource() {
      return this.type == 1;
   }

   public final float getVitality() {
      return this.vitality;
   }

   final float getInitialVitality() {
      return this.initialVitality;
   }

   final float getInitialAttack() {
      return this.initialAttack;
   }

   final boolean isFriend(EpicEntity other) {
      if (other != null) {
         if (other.equals(this.companion)) {
            return true;
         } else {
            return other.getCompanion() != null && other.getCompanion() != this && other.getCompanion().isCompanion(this) ? true : other.isCompanion(this);
         }
      } else {
         return false;
      }
   }

   final boolean isEnemy(EpicEntity other) {
      if (other == this) {
         return false;
      } else if (this.isFriend(other)) {
         return false;
      } else if (other.isFriend(this)) {
         return false;
      } else if (other.isSentinelMonster()) {
         return !other.isWurm();
      } else if (other.isWurm()) {
         return this.isDeity() || this.isAlly() || this.isDemigod();
      } else {
         return !other.isDeity() && !other.isDemigod() || !this.isDeity() && !this.isWurm() && !this.isSentinelMonster() && !this.isDemigod()
            ? other.isCompanion(this)
            : true;
      }
   }

   final boolean rollAttack() {
      int bonus = 0;

      for(EpicEntity e : this.entities) {
         if (e.isSource()) {
            ++bonus;
         }
      }

      if (this.hex != null) {
         if (this.hex.isHomeFor(this.identifier)) {
            ++bonus;
         }

         if (this.hex.isSpawnFor(this.getId())) {
            ++bonus;
         }
      }

      if (this.helped) {
         ++bonus;
      }

      return (float)RAND.nextInt(20) < Math.min(18.0F, this.attack + (float)bonus);
   }

   final EpicEntity getCompanion() {
      return this.companion;
   }

   final boolean isCompanion(EpicEntity entity) {
      return entity != null && entity.equals(this.companion);
   }

   final void setAttack(float newAttack) {
      this.setAttack(newAttack, false);
   }

   final void setAttack(float newAttack, boolean load) {
      if (this.initialAttack == 0.0F) {
         this.initialAttack = newAttack;
      }

      this.attack = Math.min(18.0F, Math.max(this.initialAttack, newAttack));
      if (!load) {
         this.updateEntityVitality();
      }
   }

   public final float getAttack() {
      return this.attack;
   }

   final void spawn() {
      this.headingHome = false;
      this.carrier = null;
      this.vitality = this.initialVitality;
      this.attack = this.initialAttack;
      this.updateEntityVitality();
      this.targetHex = 0;
      this.helped = false;
      this.resetSteps();
      if (this.myMap != null) {
         MapHex mh = this.myMap.getSpawnHex(this);
         if (mh != null) {
            if (!mh.containsEnemy(this)) {
               mh.addEntity(this);
            }
         } else {
            this.saveHexPos();
         }
      }
   }

   final EpicEntity getCarrier() {
      return this.carrier;
   }

   int getHexNumRequiredToWin() {
      return this.myMap.getHexNumRequiredToWin();
   }

   boolean mustReturnHomeToWin() {
      return this.myMap.isSpawnPointRequiredToWin();
   }

   boolean hasEnoughCollectablesToWin() {
      if (this.isWurm()) {
         return this.countCollectables() >= this.myMap.getCollictblesRequiredForWurmToWin();
      } else if (this.isDeity()) {
         return this.countCollectables() >= this.myMap.getCollictblesRequiredToWin();
      } else {
         return false;
      }
   }

   private final void findNextTargetHex() {
      if (this.hex != null && (this.targetHex == this.hex.getId() || this.targetHex <= 0)) {
         this.setNextTargetHex(this.hex.findNextHex(this));
      }
   }

   public final void setNextTargetHex(int target) {
      if (target > 0) {
         logger.log(Level.INFO, this.getName() + " set target hex to " + this.myMap.getMapHex(target).getName());
      } else {
         logger.log(Level.INFO, this.getName() + " set target hex to 0.");
      }

      this.targetHex = target;
      this.saveHexPos();
      this.sendEntityData();
   }

   public final int getTargetHex() {
      return this.targetHex;
   }

   public final long getEnteredCurrentHexTime() {
      return this.enteredCurrentHex;
   }

   private final void setEnteredCurrentHex() {
      this.enteredCurrentHex = System.currentTimeMillis();
      if (this.isWurm()) {
         this.timeUntilLeave = System.currentTimeMillis() + 86400000L;
      } else {
         this.timeUntilLeave = System.currentTimeMillis() + 259200000L;
      }

      if (this.hex != null) {
         if (this.hex.isTrap()) {
            if (this.isWurm()) {
               this.timeUntilLeave += 86400000L;
            } else {
               this.timeUntilLeave += 259200000L;
            }
         }

         if (this.hex.isSlow()) {
            if (this.isWurm()) {
               this.timeUntilLeave += 43200000L;
            } else {
               this.timeUntilLeave += 86400000L;
            }
         }
      }

      this.setShouldCreateMission(true, true);
      this.saveHexPos();
   }

   public long modifyTimeToLeave(long timeChanged) {
      this.timeUntilLeave += timeChanged;
      return this.timeUntilLeave;
   }

   public MapHex getMapHex() {
      return this.hex;
   }

   void setCarrier(EpicEntity entity, boolean setReverse, boolean load, boolean log) {
      if (setReverse) {
         if (entity != null) {
            entity.addEntity(this, log, true);
         }

         if (this.carrier != null) {
            this.carrier.removeEntity(this, log);
         }
      }

      this.carrier = entity;
      if (!load) {
         this.saveCarrierForEntity();
      }
   }

   private final void addEntity(EpicEntity entity, boolean log, boolean receives) {
      if (!this.entities.contains(entity)) {
         this.entities.add(entity);
         if (log) {
            if (receives) {
               this.logWithName(" receives " + entity.getName());
            } else {
               this.logWithName(" finds " + entity.getName());
            }
         }
      }
   }

   private final void removeEntity(EpicEntity entity, boolean log) {
      if (this.entities.contains(entity)) {
         this.entities.remove(entity);
         if (log) {
            this.logWithName(" drops " + entity.getName());
         }
      }
   }

   final void dropAll(boolean killedByDemigod) {
      toggleXmlDump(false);
      if (!this.entities.isEmpty()) {
         ListIterator<EpicEntity> lit = this.entities.listIterator();

         while(lit.hasNext()) {
            EpicEntity next = lit.next();
            lit.remove();
            next.setCarrier(null, false, false, true);
            if (killedByDemigod) {
               next.setMapHex(this.myMap.getRandomHex());
            } else {
               next.setMapHex(this.hex);
            }
         }
      }

      toggleXmlDump(true);
   }

   void setHeadingHome(boolean headingHomeToSet) {
      this.headingHome = headingHomeToSet;
   }

   boolean isHeadingHome() {
      return this.headingHome;
   }

   public void broadCastWithName(String toBroadCast) {
      if (this.myMap != null) {
         this.myMap.broadCast(this.name + toBroadCast);
      }
   }

   void broadCast(String toBroadCast) {
      if (this.myMap != null) {
         this.myMap.broadCast(toBroadCast);
      }
   }

   void log(String toLog) {
      logger.log(Level.INFO, toLog);
   }

   void logWithName(String toLog) {
      logger.log(Level.INFO, this.name + toLog);
   }

   public final String getLocationStatus() {
      if (this.hex != null) {
         if (!this.isCollectable() && !this.isSource()) {
            String prep = this.name + this.hex.getFullPresenceString();
            if (this.hex.getSpawnEntityId() == this.getId()) {
               prep = this.name + this.hex.getOwnPresenceString();
            }

            if (this.myMap != null && this.targetHex > 0) {
               prep = prep
                  + " Heading to "
                  + this.myMap.getMapHex(this.targetHex).getName()
                  + " leaving in "
                  + Server.getTimeFor(this.getTimeUntilLeave() - System.currentTimeMillis())
                  + " time to next="
                  + Server.getTimeFor(this.getTimeToNextHex() - System.currentTimeMillis());
            }

            return prep;
         } else {
            return this.name + " is" + this.hex.getPrepositionString() + this.hex.getName() + ".";
         }
      } else {
         return this.name + " is in an unknown location.";
      }
   }

   public final String getEnemyStatus() {
      if (this.hex != null) {
         String prep = this.hex.getEnemyStatus(this);
         if (prep != null && prep.length() > 0) {
            logger.log(Level.INFO, prep);
         }

         return prep;
      } else {
         return this.name + " is in an unknown location.";
      }
   }

   public final int countCollectables() {
      int numColl = 0;

      for(EpicEntity e : this.entities) {
         if (e.isCollectable()) {
            this.collName = e.getName();
            ++numColl;
         }
      }

      return numColl;
   }

   public final List<EpicEntity> getAllCollectedItems() {
      return this.entities;
   }

   public final void giveCollectables(EpicEntity receiver) {
      Set<EpicEntity> collsToGive = new HashSet<>();

      for(EpicEntity e : this.entities) {
         if (e.isCollectable()) {
            collsToGive.add(e);
         }
      }

      for(EpicEntity e : collsToGive) {
         if (e.isCollectable()) {
            e.setCarrier(receiver, true, false, true);
         }
      }
   }

   boolean checkWinCondition() {
      if ((this.isDeity() || this.isWurm()) && this.hex != null) {
         int numColl = this.countCollectables();
         numColl += this.hex.countCollectibles();
         if (this.steps > 0) {
            if (this.hex.containsEnemy(this)) {
               if (this.isShouldCreateMission()) {
                  this.sendNewScenarioWebCommand(this.succeededLastMission() ? -2 : -3);
               }

               this.setShouldCreateMission(false, false);
               return false;
            }

            boolean win = this.myMap.winCondition(this.isWurm(), numColl, this.hex.isSpawnFor(this.getId()), this.hex.getId());
            if (win) {
               this.myMap.win(this, this.collName, numColl);
               this.sendNewScenarioWebCommand(1);
            } else if (this.isShouldCreateMission()) {
               this.sendNewScenarioWebCommand(this.succeededLastMission() ? -2 : -3);
            }

            this.setShouldCreateMission(false, false);
            return win;
         }

         if (this.isShouldCreateMission()) {
            this.sendNewScenarioWebCommand(this.succeededLastMission() ? -2 : -3);
         }

         this.setShouldCreateMission(false, false);
      }

      return false;
   }

   final void createEntity(int spawn) {
      if (spawn > 0 && this.type != 2 && this.type != 1) {
         MapHex mh = this.myMap.getMapHex(spawn);
         if (!mh.isSpawnFor(this.identifier) && !mh.isSpawn()) {
            mh.setSpawnEntityId(this.identifier);
         }
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement(
            "INSERT INTO ENTITIES (ID,NAME,SPAWNPOINT,ENTITYTYPE,ATTACK,VITALITY,INATTACK,INVITALITY,CARRIER) VALUES (?,?,?,?,?,?,?,?,?)"
         );
         ps.setLong(1, this.identifier);
         ps.setString(2, this.name);
         ps.setInt(3, spawn);
         ps.setInt(4, this.type);
         ps.setFloat(5, this.attack);
         ps.setFloat(6, this.vitality);
         ps.setFloat(7, this.attack);
         ps.setFloat(8, this.vitality);
         if (this.carrier != null) {
            ps.setLong(9, this.carrier.getId());
         } else {
            ps.setLong(9, 0L);
         }

         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem creating an Epic Entity for spawn: " + spawn + " due to " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void createAndSaveSkills() {
      if (this.skills.isEmpty()) {
         logger.log(Level.WARNING, "Error creating skills for epic entity " + this.getName() + ". No default skills exist for this entity.");
      } else {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getDeityDbCon();

            for(int skillId : this.skills.keySet()) {
               ps = dbcon.prepareStatement("INSERT INTO ENTITYSKILLS (ENTITYID,SKILLID,DEFAULTVAL,CURRENTVAL) VALUES (?,?,?,?)");
               ps.setLong(1, this.identifier);
               ps.setInt(2, skillId);
               ps.setFloat(3, this.skills.get(skillId).getDefaultVal());
               ps.setFloat(4, this.skills.get(skillId).getCurrentVal());
               ps.executeUpdate();
            }
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Problem creating an epic entity skill due to " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public final void updateSkills() {
      if (this.skills.isEmpty()) {
         logger.log(Level.WARNING, "Error updating skills for epic entity " + this.getName() + ". No skills exist for this entity.");
      } else {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getDeityDbCon();

            for(int skillId : this.skills.keySet()) {
               ps = dbcon.prepareStatement("UPDATE ENTITYSKILLS SET DEFAULTVAL=?,CURRENTVAL=? WHERE ENTITYID=? AND SKILLID=?");
               ps.setFloat(1, this.skills.get(skillId).getDefaultVal());
               ps.setFloat(2, this.skills.get(skillId).getCurrentVal());
               ps.setLong(3, this.identifier);
               ps.setInt(4, skillId);
               ps.executeUpdate();
            }
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Problem updating an epic entity skill due to " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   private final void updateEntityVitality() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("UPDATE ENTITIES SET ATTACK=?,VITALITY=?,INATTACK=?,INVITALITY=? WHERE ID=?");
         ps.setFloat(1, this.attack);
         ps.setFloat(2, this.vitality);
         ps.setFloat(3, this.initialAttack);
         ps.setFloat(4, this.initialVitality);
         ps.setLong(5, this.identifier);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void sendEntityData() {
      if (this.myMap != null && dumpToXML) {
         EpicXmlWriter.dumpEntities(this.myMap);
         WcEpicStatusReport report = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, 0, (byte)-1, -1);
         report.fillStatusReport(this.myMap);
         report.sendFromLoginServer();
         if (Features.Feature.VALREI_MAP.isEnabled()) {
            ValreiMapData.updateFromEpicEntity(this);
         }
      }
   }

   final void saveHexPos() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("UPDATE ENTITIES SET CURRENTHEX=?,HELPED=?,ENTERED=?,LEAVING=?,TARGETHEX=? WHERE ID=?");
         if (this.hex != null) {
            ps.setInt(1, this.hex.getId());
         } else {
            ps.setInt(1, -1);
         }

         ps.setBoolean(2, this.helped);
         ps.setLong(3, this.enteredCurrentHex);
         ps.setLong(4, this.timeUntilLeave);
         ps.setInt(5, this.targetHex);
         ps.setLong(6, this.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      this.sendEntityData();
   }

   final void deleteEntity() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("DELETE FROM ENTITIES WHERE ID=?");
         ps.setLong(1, this.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   final void setCompanionForEntity(long companionId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("UPDATE ENTITIES SET COMPANION=? WHERE ID=?");
         ps.setLong(1, companionId);
         ps.setLong(2, this.identifier);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   final byte getDemigodsToAppoint() {
      return this.demigodsToAppoint;
   }

   final void setDemigodPlusForEntity(byte numsToAppoint) {
      this.demigodsToAppoint = numsToAppoint;
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("UPDATE ENTITIES SET DEMIGODPLUS=? WHERE ID=?");
         ps.setByte(1, numsToAppoint);
         ps.setLong(2, this.identifier);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private final void saveCarrierForEntity() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("UPDATE ENTITIES SET CARRIER=? WHERE ID=?");
         ps.setLong(1, this.carrier == null ? 0L : this.carrier.getId());
         ps.setLong(2, this.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public boolean isShouldCreateMission() {
      return this.shouldCreateMission;
   }

   public void setShouldCreateMission(boolean aShouldCreateMission, boolean lastMissionSuccess) {
      this.shouldCreateMission = aShouldCreateMission;
      this.succeedLastMission = lastMissionSuccess;
   }

   public boolean succeededLastMission() {
      return this.succeedLastMission;
   }

   public boolean isPlayerGod() {
      return this.isDeity() && this.identifier > 100L;
   }

   public boolean setSkill(int skillId, float newCurrentVal) {
      if (this.skills.containsKey(skillId)) {
         this.skills.get(skillId).setCurrentVal(newCurrentVal);
         this.updateSkills();
         return true;
      } else {
         return false;
      }
   }

   public void addSkill(int skillId, float skillVal) {
      this.setSkill(skillId, skillVal, skillVal);
   }

   public void setSkill(int skillId, float defaultVal, float currentVal) {
      if (!this.skills.containsKey(skillId)) {
         this.skills.put(skillId, new EpicEntity.SkillVal(defaultVal, currentVal));
      } else {
         EpicEntity.SkillVal existing = this.skills.get(skillId);
         existing.setDefaultVal(defaultVal);
         existing.setCurrentVal(currentVal);
      }
   }

   public void increaseRandomSkill(float skillDivider) {
      int randomSkill = 100 + Server.rand.nextInt(7);
      float currentSkill = this.getCurrentSkill(randomSkill);
      this.setSkill(randomSkill, currentSkill + (100.0F - currentSkill) / skillDivider);
   }

   public EpicEntity.SkillVal getSkill(int skillId) {
      return this.skills.get(skillId);
   }

   public HashMap<Integer, EpicEntity.SkillVal> getAllSkills() {
      return this.skills;
   }

   public float getCurrentSkill(int skillId) {
      if (!this.isCollectable() && !this.isSource()) {
         if (this.skills.get(skillId) != null) {
            return this.skills.get(skillId).getCurrentVal();
         } else {
            if (skillId == 102 || skillId == 103 || skillId == 104 || skillId == 100 || skillId == 101 || skillId == 105 || skillId == 106) {
               HexMap.VALREI.setEntityDefaultSkills(this);
               if (this.skills.get(skillId) != null) {
                  this.createAndSaveSkills();
                  return this.skills.get(skillId).getCurrentVal();
               }
            }

            logger.log(Level.WARNING, "Unable to find skill value for epic entity: " + this.getName() + " skill: " + skillId);
            return -1.0F;
         }
      } else {
         return -1.0F;
      }
   }

   class SkillVal {
      private float defaultVal;
      private float currentVal;

      SkillVal() {
         this(-1.0F, -1.0F);
      }

      SkillVal(float defaultVal, float currentVal) {
         this.defaultVal = defaultVal;
         this.currentVal = currentVal;
      }

      public void setCurrentVal(float newCurrentVal) {
         this.currentVal = newCurrentVal;
      }

      public float getCurrentVal() {
         return this.currentVal;
      }

      public void setDefaultVal(float newDefaultVal) {
         this.defaultVal = newDefaultVal;
      }

      public float getDefaultVal() {
         return this.defaultVal;
      }
   }
}

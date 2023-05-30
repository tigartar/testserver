package com.wurmonline.server.tutorial;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicMissionEnum;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.MissionPopup;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcEpicKarmaCommand;
import com.wurmonline.server.webinterface.WcEpicStatusReport;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TriggerEffect implements CounterTypes, MiscConstants, Comparable<TriggerEffect>, TimeConstants {
   private static Logger logger = Logger.getLogger(TriggerEffect.class.getName());
   private static final String UPDATE_EFFECT = "UPDATE TRIGGEREFFECTS SET NAME=?,DESCRIPTION=?,REWARDITEM=?,REWARDITEMNUMBERS=?,REWARDQUALITY=?,REWARDBYTE=?,EXISTINGREWARDITEMID=?,REWARDTARGETCONTAINERID=?,REWARDSKILLNUM=?,REWARDSKILLVAL=?,SPECIALEFFECTID=?,SOUND=?,TEXT=?,MISSION=?,MISSIONSTATECHANGE=?,INACTIVE=?,CREATOR=?,CREATEDDATE=?,LASTMODIFIER=?,TRIGGERID=?,DESTROYTARGET=?,ITEMMATERIAL=?,NEWBIE=?,MODIFYTILEX=?,MODIFYTILEY=?,NEWTILETYPE=?,NEWTILEDATA=?,SPAWNTILEX=?,SPAWNTILEY=?,CREATURESPAWN=?,CREATUREAGE=?,CREATURE_TYPE=?,CREATURE_NAME=?,TELEPORTX=?,TELEPORTY=?,MISSIONACTIVATED=?,MISSIONDEACTIVATED=?,TRIGGER_ACTIVATED=?,TRIGGER_DEACTIVATED=?,EFFECT_ACTIVATED=?,EFFECT_DEACTIVATED=?,WSZX=?,WSZY=?,STARTSKILLGAIN=?,STOPSKILLGAIN=?,DESTROYITEMS=?,TOP=?,TELEPORTLAYER=?,ACHIEVEMENTID=? WHERE ID=?";
   private static final String CREATE_EFFECT = "INSERT INTO TRIGGEREFFECTS (NAME, DESCRIPTION,REWARDITEM,REWARDITEMNUMBERS,REWARDQUALITY,REWARDBYTE,EXISTINGREWARDITEMID,REWARDTARGETCONTAINERID,REWARDSKILLNUM,REWARDSKILLVAL,SPECIALEFFECTID,SOUND,TEXT,MISSION,MISSIONSTATECHANGE,INACTIVE,CREATOR,CREATEDDATE,LASTMODIFIER,TRIGGERID,DESTROYTARGET,CREATORID,CREATORTYPE,ITEMMATERIAL,NEWBIE,MODIFYTILEX,MODIFYTILEY,NEWTILETYPE,NEWTILEDATA,SPAWNTILEX,SPAWNTILEY,CREATURESPAWN,CREATUREAGE,CREATURE_TYPE,CREATURE_NAME,TELEPORTX,TELEPORTY,MISSIONACTIVATED,MISSIONDEACTIVATED,TRIGGER_ACTIVATED,TRIGGER_DEACTIVATED,EFFECT_ACTIVATED,EFFECT_DEACTIVATED,WSZX,WSZY,STARTSKILLGAIN,STOPSKILLGAIN,DESTROYITEMS,TOP,TELEPORTLAYER,ACHIEVEMENTID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String DELETE_EFFECT = "DELETE FROM TRIGGEREFFECTS WHERE ID=?";
   private int id;
   private String name;
   private String description;
   private int rewardItem;
   private int rewardNumbers;
   private int rewardQl;
   private byte rewardByteValue;
   private long existingItemReward;
   private long rewardTargetContainerId;
   private int rewardSkillNum;
   private float rewardSkillVal;
   private int triggerId;
   private int specialEffectId;
   private int achievementId;
   private String soundName;
   private String topText;
   private String textDisplayed;
   private int missionId;
   private float missionStateChange;
   private boolean destroysTarget = false;
   private boolean inActive = false;
   private boolean startSkillGain = true;
   private boolean stopSkillGain = false;
   private boolean destroyItems = false;
   private String creatorName;
   private String createdDate;
   private String lastModifierName;
   private Timestamp lastModifiedDate;
   private long ownerId = 0L;
   private byte creatorType = 0;
   private byte itemMaterial = 0;
   private boolean newbieItem = false;
   private int modifyTileX = 0;
   private int modifyTileY = 0;
   private int newTileType = 0;
   private byte newTileData = 0;
   private int spawnTileX = 0;
   private int spawnTileY = 0;
   private int creatureSpawn = 0;
   private int creatureAge = 0;
   private byte creatureType = 0;
   private String creatureName = "";
   private int teleportX = 0;
   private int teleportY = 0;
   private int teleportLayer = 0;
   private int missionToActivate = 0;
   private int missionToDeActivate = 0;
   private int triggerToActivate = 0;
   private int triggerToDeActivate = 0;
   private int effectToActivate = 0;
   private int effectToDeActivate = 0;
   private boolean destroyed = false;
   private int windowSizeX = 0;
   private int windowSizeY = 0;

   public void setCreatedDate(String aCreatedDate) {
      this.createdDate = aCreatedDate;
   }

   public String getCreatedDate() {
      return this.createdDate;
   }

   public void setMissionToActivate(int val) {
      this.missionToActivate = val;
   }

   public int getMissionToActivate() {
      return this.missionToActivate;
   }

   public void setMissionToDeActivate(int val) {
      this.missionToDeActivate = val;
   }

   public int getMissionToDeActivate() {
      return this.missionToDeActivate;
   }

   public void setTriggerToActivate(int val) {
      this.triggerToActivate = val;
   }

   public int getTriggerToActivate() {
      return this.triggerToActivate;
   }

   public void setTriggerToDeActivate(int val) {
      this.triggerToDeActivate = val;
   }

   public int getTriggerToDeActivate() {
      return this.triggerToDeActivate;
   }

   public void setEffectToActivate(int val) {
      this.effectToActivate = val;
   }

   public int getEffectToActivate() {
      return this.effectToActivate;
   }

   public void setEffectToDeActivate(int val) {
      this.effectToDeActivate = val;
   }

   public int getEffectToDeActivate() {
      return this.effectToDeActivate;
   }

   public void setTeleportY(int val) {
      this.teleportY = val;
   }

   public int getTeleportY() {
      return this.teleportY;
   }

   public void setTeleportX(int val) {
      this.teleportX = val;
   }

   public int getTeleportX() {
      return this.teleportX;
   }

   public void setTeleportLayer(int val) {
      this.teleportLayer = val;
   }

   public int getTeleportLayer() {
      return this.teleportLayer;
   }

   public void setWindowSizeX(int val) {
      this.windowSizeX = Math.max(0, Math.min(999, val));
   }

   public int getWindowSizeX() {
      return this.windowSizeX;
   }

   public void setWindowSizeY(int val) {
      this.windowSizeY = Math.max(0, Math.min(999, val));
   }

   public int getWindowSizeY() {
      return this.windowSizeY;
   }

   public void setCreatureAge(int val) {
      this.creatureAge = val;
   }

   public int getCreatureAge() {
      return this.creatureAge;
   }

   public void setCreatureSpawn(int val) {
      this.creatureSpawn = val;
   }

   public int getCreatureSpawn() {
      return this.creatureSpawn;
   }

   public void setCreatureType(byte val) {
      this.creatureType = val;
   }

   public byte getCreatureType() {
      return this.creatureType;
   }

   @Nullable
   public void setCreatureName(String val) {
      if (val == null) {
         this.creatureName = "";
      } else {
         this.creatureName = val;
      }
   }

   public String getCreatureName() {
      return this.creatureName;
   }

   public void setSpawnTileY(int val) {
      this.spawnTileY = val;
   }

   public int getSpawnTileY() {
      return this.spawnTileY;
   }

   public void setSpawnTileX(int val) {
      this.spawnTileX = val;
   }

   public int getSpawnTileX() {
      return this.spawnTileX;
   }

   public void setModifyTileY(int val) {
      this.modifyTileY = val;
   }

   public int getModifyTileY() {
      return this.modifyTileY;
   }

   public int getNewTileType() {
      return this.newTileType;
   }

   public void setNewTileType(int val) {
      this.newTileType = val;
   }

   public void setModifyTileX(int val) {
      this.modifyTileX = val;
   }

   public int getModifyTileX() {
      return this.modifyTileX;
   }

   public void setNewTileData(byte tiledata) {
      this.newTileData = tiledata;
   }

   public final byte getNewTileData() {
      return this.newTileData;
   }

   public void setItemMaterial(byte material) {
      this.itemMaterial = material;
   }

   public final byte getItemMaterial() {
      return this.itemMaterial;
   }

   public void setNewbieItem(boolean newbie) {
      this.newbieItem = newbie;
   }

   public final boolean isNewbieItem() {
      return this.newbieItem;
   }

   public void setStopSkillgain(boolean stop) {
      this.stopSkillGain = stop;
   }

   public final boolean isStopSkillgain() {
      return this.stopSkillGain;
   }

   public void setStartSkillgain(boolean start) {
      this.startSkillGain = start;
   }

   public final boolean isStartSkillgain() {
      return this.startSkillGain;
   }

   public void setDestroyInventory(boolean destroys) {
      this.destroyItems = destroys;
   }

   public final boolean destroysInventory() {
      return this.destroyItems;
   }

   public void setCreatorType(byte aCreatorType) {
      this.creatorType = aCreatorType;
   }

   public byte getCreatorType() {
      return this.creatorType;
   }

   public void setOwnerId(long aWurmId) {
      this.ownerId = aWurmId;
   }

   public long getOwnerId() {
      return this.ownerId;
   }

   void setId(int aId) {
      this.id = aId;
   }

   public int getId() {
      return this.id;
   }

   void setLastModifiedDate(Timestamp aLastModifiedDate) {
      this.lastModifiedDate = aLastModifiedDate;
   }

   public String getLastModifiedDate() {
      return DateFormat.getDateInstance(2).format(this.lastModifiedDate);
   }

   public String getDescription() {
      return this.description;
   }

   public String getName() {
      return this.name;
   }

   public String getType() {
      StringBuilder buf = new StringBuilder();
      if (!this.getTopText().isEmpty()) {
         buf.append(",popup");
      }

      if (this.getTeleportX() != 0) {
         buf.append(",tp");
      }

      if (this.getSpawnTileX() != 0) {
         buf.append(",spawn");
      }

      if (this.getModifyTileX() != 0) {
         buf.append(",tile");
      }

      if (this.getRewardItem() > 0) {
         buf.append(",item");
      }

      if (this.getSpecialEffectId() > 0) {
         buf.append(",special");
      }

      if (this.getRewardSkillNum() > 0) {
         buf.append(",skill");
      }

      if (!this.getSoundName().isEmpty()) {
         buf.append(",sound");
      }

      if (buf.length() == 0) {
         buf.append(",other");
      }

      return buf.substring(1);
   }

   public void setDescription(String n) {
      this.description = n;
      if (this.description != null) {
         this.description = this.description.substring(0, Math.min(this.description.length(), 400));
      }
   }

   public void setName(String n) {
      this.name = n;
      if (this.name != null) {
         this.name = this.name.substring(0, Math.min(this.name.length(), 40));
      }
   }

   public boolean isInactive() {
      return this.inActive;
   }

   public void setInactive(boolean inactive) {
      this.inActive = inactive;
   }

   public boolean destroysTarget() {
      return this.destroysTarget;
   }

   public void setDestroysTarget(boolean destroys) {
      this.destroysTarget = destroys;
   }

   public String getSoundName() {
      return this.soundName;
   }

   public int getRewardItem() {
      return this.rewardItem;
   }

   public int getRewardNumbers() {
      return this.rewardNumbers;
   }

   public long getExistingItemReward() {
      return this.existingItemReward;
   }

   public int getRewardQl() {
      return this.rewardQl;
   }

   public byte getRewardByteValue() {
      return this.rewardByteValue;
   }

   public void setCreatorName(String n) {
      this.creatorName = n;
      if (this.creatorName != null) {
         this.creatorName = this.creatorName.substring(0, Math.min(this.creatorName.length(), 40));
      }
   }

   public String getCreatorName() {
      return this.creatorName;
   }

   public void setLastModifierName(String aName) {
      this.lastModifierName = aName;
      if (this.lastModifierName != null) {
         this.lastModifierName = this.lastModifierName.substring(0, Math.min(this.lastModifierName.length(), 40));
      }
   }

   public String getLastModifierName() {
      return this.lastModifierName;
   }

   public String getTopText() {
      return this.topText;
   }

   public void setTopText(String tdi) {
      this.topText = tdi;
      if (this.topText != null) {
         this.topText = this.topText.substring(0, Math.min(this.topText.length(), 1000));
      }
   }

   public String getTextDisplayed() {
      return this.textDisplayed;
   }

   public void setTextDisplayed(String tdi) {
      this.textDisplayed = tdi;
      if (this.textDisplayed != null) {
         this.textDisplayed = this.textDisplayed.substring(0, Math.min(this.textDisplayed.length(), 1000));
      }
   }

   public void setRewardItem(int ri) {
      this.rewardItem = ri;
   }

   public void setRewardNumbers(int nums) {
      this.rewardNumbers = nums;
   }

   public void setExistingItemReward(long rew) {
      this.existingItemReward = rew;
   }

   public long getRewardTargetContainerId() {
      return this.rewardTargetContainerId;
   }

   public void setRewardSkillVal(float snum) {
      this.rewardSkillVal = snum;
   }

   public float getRewardSkillModifier() {
      return this.rewardSkillVal;
   }

   public void setSpecialEffect(int effectid) {
      this.specialEffectId = effectid;
   }

   public int getSpecialEffectId() {
      return this.specialEffectId;
   }

   public void setTrigger(int tid) {
      if (tid > 0) {
         Triggers2Effects.addLink(tid, this.id, false);
      }

      this.triggerId = tid;
   }

   public int getTriggerId() {
      MissionTrigger[] trigs = Triggers2Effects.getTriggersForEffect(this.id, true);
      return trigs.length >= 1 ? trigs[0].getId() : this.triggerId;
   }

   public void setMissionStateChange(float stateChange) {
      this.missionStateChange = stateChange;
   }

   public float getMissionStateChange() {
      return this.missionStateChange;
   }

   public void setMission(int mid) {
      this.missionId = mid;
   }

   public int getMissionId() {
      return this.missionId;
   }

   public void setRewardSkillNum(int snum) {
      this.rewardSkillNum = snum;
   }

   public int getRewardSkillNum() {
      return this.rewardSkillNum;
   }

   public void setRewardTargetContainerId(long rew) {
      this.rewardTargetContainerId = rew;
   }

   public void setRewardQl(int ql) {
      this.rewardQl = ql;
   }

   public void setRewardByteValue(byte bv) {
      this.rewardByteValue = bv;
   }

   public void setSoundName(String sn) {
      this.soundName = sn;
      if (this.soundName != null) {
         this.soundName = this.soundName.substring(0, Math.min(this.soundName.length(), 50));
      }
   }

   public final boolean setMissionState(Creature performer, MissionPerformed mperf, boolean setFinishedEpic) {
      boolean toReturn = false;
      Mission mission = Missions.getMissionWithId(this.missionId);
      if (mission != null && !mission.isInactive()) {
         float newstate = Math.min(100.0F, mperf.getState() + this.getMissionStateChange());
         boolean sendStartPopup = false;
         if (this.creatorType == 2) {
            if (setFinishedEpic) {
               newstate = 100.0F;
            }
         } else if (mperf.getMissionId() != this.missionId) {
            MissionPerformer missionPerfor = MissionPerformed.getMissionPerformer(performer.getWurmId());
            MissionPerformed mpf = missionPerfor.getMission(this.missionId);
            if (mpf != null) {
               mperf = mpf;
            } else {
               missionPerfor = MissionPerformed.startNewMission(this.missionId, performer.getWurmId(), 1.0F);
               sendStartPopup = true;
               mperf = missionPerfor.getMission(this.missionId);
            }
         }

         if (this.getMissionStateChange() == -1.0F) {
            newstate = -1.0F;
         }

         if (mperf != null && (mperf.isStarted() || mperf.isFailed())) {
            boolean secondChance = mperf.isFailed() && mission.hasSecondChance() || mperf.isCompleted() && mission.mayBeRestarted();
            if (secondChance) {
               toReturn = mperf.setState(1.0F, performer.getWurmId());
               sendStartPopup = true;
            }
         }

         if (!mperf.isFailed()) {
            if (sendStartPopup && mission.getInstruction() != null && mission.getInstruction().length() > 0) {
               SimplePopup pop = new SimplePopup(performer, "Mission start", mission.getInstruction());
               pop.sendQuestion();
            }

            if (mission.getMaxTimeSeconds() > 0 && System.currentTimeMillis() > mperf.getFinishTimeAsLong(mission.getMaxTimeSeconds())) {
               mperf.setState(-1.0F, performer.getWurmId());
               String miss = Server.getTimeFor(System.currentTimeMillis() - mperf.getFinishTimeAsLong(mission.getMaxTimeSeconds()));
               SimplePopup pop = new SimplePopup(performer, "Mission failed", "You failed " + mission.getName() + ". You are " + miss + " late.");
               pop.sendQuestion();
               toReturn = true;
            } else {
               performer.sendToLoggers("Proper state achieved for mission " + this.missionId, (byte)2);
               performer.sendToLoggers("Setting state of mission to " + newstate, (byte)2);
               toReturn = mperf.setState(newstate, performer.getWurmId());
            }
         }
      }

      return toReturn;
   }

   public boolean sendTriggerDescription(Creature performer) {
      if (this.getTextDisplayed() != null && this.getTextDisplayed().length() > 0 || this.getTopText() != null && this.getTopText().length() > 0) {
         MissionPopup pop = new MissionPopup(performer, "Mission progress", "");
         if (this.windowSizeX > 0) {
            pop.windowSizeX = this.windowSizeX;
            pop.windowSizeY = this.windowSizeY;
         }

         pop.setToSend(this.getTextDisplayed());
         pop.setTop(this.getTopText());
         pop.sendQuestion();
         return true;
      } else {
         return false;
      }
   }

   private static void destroyInventoryItems(Item inventory, Creature performer) {
      Item[] inventoryItems = inventory.getItemsAsArray();

      for(int i = 0; i < inventoryItems.length; ++i) {
         if (inventoryItems[i].isTraded() && performer.getTrade() != null) {
            inventoryItems[i].getTradeWindow().removeItem(inventoryItems[i]);
         }

         if (!inventoryItems[i].isNoDrop() && !inventoryItems[i].isCoin()) {
            Items.destroyItem(inventoryItems[i].getWurmId());
         }

         if (inventoryItems[i].isInventoryGroup()) {
            destroyInventoryItems(inventoryItems[i], performer);
         }
      }
   }

   public void effect(Creature performer, MissionPerformed perf, long target, boolean setFinished, boolean triggerEpicHelper) {
      performer.sendToLoggers("Running effect " + this.name + " state effect " + this.getMissionStateChange() + " on mission " + this.missionId, (byte)2);
      if (!this.inActive) {
         boolean sendSound = true;
         if (!performer.isPlayer() || this.creatorType == 2) {
            sendSound = setFinished;
         } else if (this.sendTriggerDescription(performer)) {
            ((Player)performer).setLastTrigger(this.id);
         }

         if (sendSound && this.getSoundName() != null && this.getSoundName().length() > 0) {
            SoundPlayer.playSound(this.getSoundName(), performer, 1.5F);
         }

         if (this.getMissionStateChange() != 0.0F) {
            if (this.setMissionState(performer, perf, setFinished) && this.destroysTarget && this.creatorType != 3) {
               this.destroyTarget(target);
            }

            if (this.creatorType == 2) {
               performer.sendToLoggers("Trying to get epic mission for " + perf.getMissionId(), (byte)2);
               EpicMission em = EpicServerStatus.getEpicMissionForMission(perf.getMissionId());
               if (em != null) {
                  if (em.isCurrent()) {
                     performer.sendToLoggers(
                        "Effect " + this.name + " state effect " + this.getMissionStateChange() + " on Epic mission " + em.getEntityName(), (byte)2
                     );
                     em.updateProgress(em.getMissionProgress() + this.getMissionStateChange());
                     Deity d = Deities.translateDeityForEntity(em.getEpicEntityId());
                     int deityNum = -1;
                     if (d != null) {
                        deityNum = d.getNumber();
                     }

                     EpicMissionEnum missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType());
                     if ((Deities.getFavoredKingdom(deityNum) == performer.getKingdomTemplateId() || !Servers.localServer.EPIC)
                        && missionEnum != null
                        && !EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)) {
                        int karmaSplit = missionEnum.getKarmaBonusDiffMult() * em.getDifficulty();
                        if (missionEnum.isKarmaMultProgress()) {
                           int karmaGained = (int)Math.ceil((double)((float)karmaSplit / 100.0F * this.getMissionStateChange()));
                           if (karmaGained > 0) {
                              performer.modifyKarma(karmaGained);
                              if (performer.isPaying()) {
                                 performer.setScenarioKarma(performer.getScenarioKarma() + karmaGained);
                                 if (Servers.localServer.EPIC) {
                                    WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(
                                       WurmId.getNextWCCommandId(),
                                       new long[]{performer.getWurmId()},
                                       new int[]{performer.getScenarioKarma()},
                                       em.getEpicEntityId()
                                    );
                                    wcek.sendToLoginServer();
                                 }
                              }

                              if (this.getMissionStateChange() * 10.0F > 10.0F) {
                                 logger.log(
                                    Level.INFO,
                                    "Added karma "
                                       + karmaGained
                                       + " to "
                                       + performer.getName()
                                       + " for mission "
                                       + em.getMissionId()
                                       + " for "
                                       + em.getEntityName()
                                       + " state change="
                                       + this.getMissionStateChange()
                                       + " to "
                                       + em.getMissionProgress()
                                 );
                              }
                           }
                        }
                     }

                     if (triggerEpicHelper || setFinished) {
                        performer.achievement(52);
                     }

                     if (em.isCompleted()) {
                        logger.log(
                           Level.INFO,
                           "Mission is complete! "
                              + performer.getName()
                              + " mission "
                              + em.getMissionId()
                              + " for "
                              + em.getEntityName()
                              + ", performer mission is "
                              + perf.getMissionId()
                        );
                        if (performer.isPlayer()) {
                           ((Player)performer).setLastTrigger(this.id);
                        }

                        this.sendTriggerDescription(performer);
                        Mission m = Missions.getMissionWithId(perf.getMissionId());
                        if (m != null) {
                           if (Deities.getFavoredKingdom(deityNum) == performer.getKingdomTemplateId() || !Servers.localServer.EPIC) {
                              LinkedList<Long> ids = new LinkedList<>();
                              LinkedList<Integer> values = new LinkedList<>();
                              int baseKarma = missionEnum.getBaseKarma();
                              int baseSleep = missionEnum.getBaseSleep();
                              int karmaSplit = missionEnum.getKarmaBonusDiffMult() * em.getDifficulty();
                              int sleepSplit = missionEnum.getSleepBonusDiffMult() * em.getDifficulty();
                              ArrayList<Player> totalNearby = new ArrayList<>();

                              for(Player p : Players.getInstance().getPlayers()) {
                                 if ((Deities.getFavoredKingdom(deityNum) == p.getKingdomTemplateId() || !Servers.localServer.EPIC)
                                    && p.isWithinDistanceTo(performer, 300.0F)) {
                                    totalNearby.add(p);
                                 }
                              }

                              ArrayList<PlayerInfo> allParticipants = new ArrayList<>();
                              MissionPerformer[] allperfs = MissionPerformed.getAllPerformers();

                              for(MissionPerformer mp : allperfs) {
                                 MissionPerformed thisMission = mp.getMission(m.getId());
                                 if (thisMission != null) {
                                    thisMission.setState(100.0F, mp.getWurmId());
                                    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(mp.getWurmId());
                                    if (pinf != null) {
                                       allParticipants.add(pinf);
                                    }
                                 }
                              }

                              int karmaGainedNearby = 0;
                              int sleepGainedNearby = 0;
                              if (karmaSplit > 0 && EpicMissionEnum.isKarmaSplitNearby(missionEnum)) {
                                 karmaGainedNearby = (int)Math.ceil((double)((float)karmaSplit / (float)totalNearby.size()));
                                 karmaSplit = 0;
                              }

                              if (sleepSplit > 0 && missionEnum.isSleepMultNearby()) {
                                 sleepGainedNearby = (int)Math.ceil((double)((float)sleepSplit / (float)totalNearby.size()));
                                 sleepSplit = 0;
                              }

                              for(Player p : totalNearby) {
                                 if (sleepGainedNearby > 0) {
                                    p.getSaveFile().addToSleep((int)((long)Math.min(30, sleepGainedNearby) * 60L));
                                 }

                                 if (karmaGainedNearby > 0) {
                                    p.modifyKarma(Math.max(1, karmaGainedNearby));
                                    if (p.isPaying()) {
                                       p.setScenarioKarma(p.getScenarioKarma() + Math.max(1, karmaGainedNearby));
                                       ids.add(p.getWurmId());
                                       values.add(p.getScenarioKarma());
                                    }
                                 }
                              }

                              int karmaGained = baseKarma;
                              if (karmaSplit > 0) {
                                 karmaGained = baseKarma + (int)Math.ceil((double)((float)karmaSplit / (float)allParticipants.size()));
                              }

                              int sleepBonusGain = 0;
                              if (sleepSplit > 0) {
                                 sleepBonusGain += (int)Math.ceil((double)((float)sleepSplit / (float)allParticipants.size()));
                              }

                              for(PlayerInfo pinf : allParticipants) {
                                 if (baseSleep > 0 || sleepBonusGain > 0) {
                                    pinf.addToSleep((int)((long)(baseSleep + Math.min(30, sleepBonusGain)) * 60L));
                                 }

                                 if (karmaGained > 0) {
                                    pinf.setKarma(pinf.getKarma() + Math.max(1, karmaGained));
                                    if (pinf.isPaying()) {
                                       pinf.setScenarioKarma(pinf.getScenarioKarma() + Math.max(1, karmaGained));
                                       ids.add(pinf.wurmId);
                                       values.add(pinf.getScenarioKarma());
                                    }
                                 }

                                 if (Servers.localServer.PVPSERVER) {
                                    try {
                                       pinf.setRank(pinf.getRank());
                                    } catch (IOException var34) {
                                       logger.log(Level.WARNING, this.getName() + ": failed to reset rank decay timer " + pinf.getRank(), (Throwable)var34);
                                    }
                                 }

                                 Achievements.triggerAchievement(pinf.wurmId, 601);
                              }

                              if (Servers.localServer.EPIC) {
                                 long[] idsToSend = new long[ids.size()];
                                 int[] valuesToSend = new int[values.size()];

                                 for(int x = 0; x < idsToSend.length; ++x) {
                                    idsToSend[x] = ids.get(x);
                                    valuesToSend[x] = values.get(x);
                                 }

                                 WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(WurmId.getNextWCCommandId(), idsToSend, valuesToSend, em.getEpicEntityId());
                                 wcek.sendToLoginServer();
                              }

                              performer.achievement(53);
                           }

                           Players.printRanks();
                           MissionTrigger[] triggers = MissionTriggers.getAllTriggers();

                           for(MissionTrigger t : triggers) {
                              if (t.getMissionRequired() == m.getId()) {
                                 logger.log(
                                    Level.INFO,
                                    performer.getName()
                                       + "Destroying triggers for  mission "
                                       + em.getMissionId()
                                       + " for "
                                       + em.getEntityName()
                                       + ", performer mission is "
                                       + perf.getMissionId()
                                 );
                                 TriggerEffects.destroyEffectsForTrigger(t.getId());
                                 t.destroy();
                              }
                           }
                        }

                        logger.log(
                           Level.INFO,
                           performer.getName()
                              + "Destroying effect for  mission "
                              + em.getMissionId()
                              + " for "
                              + em.getEntityName()
                              + ", performer mission is "
                              + perf.getMissionId()
                        );
                        this.destroy();
                        logger.log(
                           Level.INFO,
                           performer.getName()
                              + " Sending report for "
                              + em.getMissionId()
                              + " for "
                              + em.getEntityName()
                              + ", performer mission is "
                              + perf.getMissionId()
                        );
                        WcEpicStatusReport wce = new WcEpicStatusReport(
                           WurmId.getNextWCCommandId(), true, em.getEpicEntityId(), em.getMissionType(), em.getDifficulty()
                        );
                        wce.sendToLoginServer();
                        EpicServerStatus.storeLastMissionForEntity(em.getEpicEntityId(), em);
                        if (!Servers.localServer.EPIC) {
                           int deityNumber = em.getEpicEntityId();
                           if (deityNumber > 0 && deityNumber <= 4) {
                              String entityName = Deities.getEntityName(deityNumber);
                              EpicServerStatus es = new EpicServerStatus();
                              if (EpicServerStatus.getCurrentScenario() == null) {
                                 EpicServerStatus.loadLocalEntries();
                              }

                              es.generateNewMissionForEpicEntity(
                                 deityNumber,
                                 entityName,
                                 em.getDifficulty() + 1,
                                 604800,
                                 em.getScenarioName(),
                                 EpicServerStatus.getCurrentScenario().getScenarioNumber(),
                                 "You must really do this for " + entityName + " because yeah you know.",
                                 true
                              );
                           }
                        }
                     }
                  } else {
                     performer.sendToLoggers("Not current mission: " + em.getEntityName(), (byte)2);
                  }
               }
            }
         }

         if (this.destroysInventory()) {
            destroyInventoryItems(performer.getInventory(), performer);
            Item[] boditems = performer.getBody().getContainersAndWornItems();

            for(int i = 0; i < boditems.length; ++i) {
               if (boditems[i].isTraded() && performer.getTrade() != null) {
                  boditems[i].getTradeWindow().removeItem(boditems[i]);
               }

               if (!boditems[i].isNoDrop() && !boditems[i].isCoin()) {
                  Items.destroyItem(boditems[i].getWurmId());
               }
            }
         }

         if (this.rewardItem > 0 && this.creatorType != 3) {
            performer.sendToLoggers("Creating " + this.rewardNumbers + " of template " + this.rewardItem, (byte)2);

            for(int x = 0; x < this.rewardNumbers; ++x) {
               try {
                  Item toCreate = ItemFactory.createItem(this.rewardItem, (float)this.rewardQl, this.itemMaterial, (byte)0, performer.getName());
                  if (this.newbieItem) {
                     toCreate.setAuxData((byte)1);
                  } else if (this.rewardByteValue != 0) {
                     toCreate.setAuxData(this.rewardByteValue);
                  }

                  this.insertItem(performer, toCreate, x);
               } catch (NoSuchTemplateException var35) {
                  logger.log(Level.WARNING, var35.getMessage(), (Throwable)var35);
                  break;
               } catch (FailedException var36) {
                  logger.log(Level.WARNING, var36.getMessage(), (Throwable)var36);
                  break;
               }
            }
         }

         if (this.existingItemReward > 0L) {
            try {
               Item toCreate = Items.getItem(this.existingItemReward);
               this.insertItem(performer, toCreate, 0);
               performer.sendToLoggers("Inserting existing reward " + this.existingItemReward, (byte)2);
               this.destroy();
            } catch (NoSuchItemException var33) {
               performer.sendToLoggers("Inserting existing reward " + this.existingItemReward + " failed. Does not exist.", (byte)2);
               this.existingItemReward = 0L;
               this.update();
            }
         }

         if (this.rewardSkillNum > 0 && this.creatorType != 3) {
            performer.sendToLoggers("Adding skill " + this.rewardSkillNum + " : " + this.rewardSkillVal, (byte)2);
            Skills skills = performer.getSkills();
            if (skills != null) {
               try {
                  Skill skill = skills.getSkill(this.rewardSkillNum);
                  double existing = skill.getKnowledge();
                  double reward = (double)this.rewardSkillVal;
                  if (this.rewardSkillVal < 1.0F) {
                     double diff = 100.0 - existing;
                     reward = diff * (double)this.rewardSkillVal;
                  }

                  skill.setKnowledge(existing + reward, false);
               } catch (NoSuchSkillException var32) {
                  skills.learn(this.rewardSkillNum, this.rewardSkillVal);
               }
            }
         }

         if (this.getSpecialEffectId() > 0) {
            if (this.teleportX <= 0 && this.teleportY <= 0) {
               try {
                  SpecialEffects.getEffects()[this.getSpecialEffectId()].run(performer, target, this.rewardNumbers);
               } catch (Exception var31) {
                  logger.log(Level.WARNING, var31.getMessage(), (Throwable)var31);
               }
            } else {
               SpecialEffects.getEffects()[this.getSpecialEffectId()].run(performer, this.teleportX, this.teleportY, this.teleportLayer);
            }
         }

         if (this.getAchievementId() > 0) {
            performer.achievement(this.getAchievementId());
         }

         if ((this.getModifyTileX() > 0 || this.getModifyTileY() > 0) && this.getNewTileType() != 0) {
            if (this.getNewTileType() != Tiles.Tile.TILE_CAVE_WALL.id && this.getNewTileType() != Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
               int otile = Server.surfaceMesh.getTile(this.getModifyTileX(), this.getModifyTileY());
               if (Tiles.decodeType(otile) != Tiles.Tile.TILE_HOLE.id) {
                  VolaTile t = Zones.getOrCreateTile(this.getModifyTileX(), this.getModifyTileY(), true);
                  if (t != null) {
                     if (t.getStructure() == null) {
                        Server.setSurfaceTile(
                           this.getModifyTileX(), this.getModifyTileY(), Tiles.decodeHeight(otile), (byte)this.getNewTileType(), this.getNewTileData()
                        );
                        Server.setWorldResource(this.getModifyTileX(), this.getModifyTileY(), 0);
                        Players.getInstance().sendChangedTile(this.getModifyTileX(), this.getModifyTileY(), true, true);
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("A structure bars an effect of your action.");
                     }
                  }
               }
            } else {
               Terraforming.setAsRock(this.getModifyTileX(), this.getModifyTileY(), false);
               if (this.getNewTileType() == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
                  int ntile = Server.caveMesh.getTile(this.getModifyTileX(), this.getModifyTileY());
                  if (Tiles.decodeType(ntile) == Tiles.Tile.TILE_CAVE_WALL.id) {
                     Server.caveMesh
                        .setTile(
                           this.getModifyTileX(),
                           this.getModifyTileY(),
                           Tiles.encode(Tiles.decodeHeight(ntile), Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id, Tiles.decodeData(ntile))
                        );
                     Server.setCaveResource(this.getModifyTileX(), this.getModifyTileY(), 10000 + Server.rand.nextInt(20000));
                     Players.getInstance().sendChangedTile(this.getModifyTileX(), this.getModifyTileY(), false, true);
                  }
               }
            }
         }

         if (this.spawnTileX > 0 || this.spawnTileY > 0) {
            try {
               if (this.getCreatureSpawn() > 0) {
                  CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(this.getCreatureSpawn());
                  byte sex = ct.getSex();
                  if (!ct.keepSex) {
                     sex = (byte)(Server.rand.nextBoolean() ? 1 : 0);
                  }

                  byte ttype = this.getCreatureType();
                  byte age = (byte)this.getCreatureAge();

                  try {
                     Creature var60 = Creature.doNew(
                        ct.getTemplateId(),
                        true,
                        (float)(this.spawnTileX * 4 + 2),
                        (float)(this.spawnTileY * 4 + 2),
                        performer.getStatus().getRotation() - 180.0F,
                        performer.getLayer(),
                        ct.getName(),
                        sex,
                        (byte)0,
                        ttype,
                        false,
                        age
                     );
                  } catch (Exception var29) {
                     logger.log(Level.WARNING, performer.getName() + " " + var29.getMessage(), (Throwable)var29);
                  }
               }
            } catch (NoSuchCreatureTemplateException var30) {
               logger.log(Level.WARNING, performer.getName() + " " + var30.getMessage(), (Throwable)var30);
            }
         }

         if (this.getMissionToActivate() > 0) {
            Mission mis = Missions.getMissionWithId(this.getMissionToActivate());
            if (mis != null) {
               mis.setInactive(false);
            }
         }

         if (this.getMissionToDeActivate() > 0) {
            Mission mis = Missions.getMissionWithId(this.getMissionToDeActivate());
            if (mis != null) {
               mis.setInactive(true);
            }
         }

         if (this.getTriggerToActivate() > 0) {
            MissionTrigger trg = MissionTriggers.getTriggerWithId(this.getTriggerToActivate());
            if (trg != null) {
               trg.setInactive(false);
            }
         }

         if (this.getTriggerToDeActivate() > 0) {
            MissionTrigger trg = MissionTriggers.getTriggerWithId(this.getTriggerToDeActivate());
            if (trg != null) {
               trg.setInactive(true);
            }
         }

         if (this.getEffectToActivate() > 0) {
            TriggerEffect eff = TriggerEffects.getTriggerEffect(this.getEffectToActivate());
            if (eff != null) {
               eff.setInactive(false);
            }
         }

         if (this.getEffectToDeActivate() > 0) {
            TriggerEffect eff = TriggerEffects.getTriggerEffect(this.getEffectToDeActivate());
            if (eff != null) {
               eff.setInactive(true);
            }
         }

         if (this.isStopSkillgain()) {
            performer.setHasSkillGain(false);
            performer.getCommunicator().sendAlertServerMessage("Your skill gain has been temporarily paused.");
         }

         if (this.isStartSkillgain()) {
            if (!performer.hasSkillGain()) {
               performer.getCommunicator().sendSafeServerMessage("Your skill gain has been unpaused.");
            }

            performer.setHasSkillGain(true);
         }

         if ((this.teleportX > 0 || this.teleportY > 0) && this.getSpecialEffectId() <= 0 && performer.getCurrentTile() != null && !performer.isTeleporting()) {
            performer.setTeleportPoints((short)this.teleportX, (short)this.teleportY, this.teleportLayer, 0);
            performer.startTeleporting();
            performer.getCommunicator().sendTeleport(false);
         }
      } else {
         performer.sendToLoggers("The effect " + this.id + " is inactive", (byte)2);
         logger.log(Level.WARNING, "The effect " + this.id + " is inactive but called anyways.");
      }
   }

   void destroyTarget(long targetId) {
      if (WurmId.getType(targetId) == 1) {
         try {
            Creature c = Creatures.getInstance().getCreature(targetId);
            c.destroy();
         } catch (NoSuchCreatureException var5) {
         }
      }

      if (WurmId.getType(targetId) == 0) {
         try {
            Player p = Players.getInstance().getPlayer(targetId);
            p.die(true, "Destruction");
         } catch (NoSuchPlayerException var4) {
         }
      }

      if (WurmId.getType(targetId) == 5) {
         Wall wall = Wall.getWall(targetId);
         if (wall != null) {
            wall.setAsPlan();
         }
      }

      if (WurmId.getType(targetId) == 2 || WurmId.getType(targetId) == 6 || WurmId.getType(targetId) == 19 || WurmId.getType(targetId) == 20) {
         Items.destroyItem(targetId);
      } else if (WurmId.getType(targetId) == 7) {
         Fence fence = Fence.getFence(targetId);
         if (fence != null) {
            fence.destroy();
         }
      }
   }

   private void insertItem(Creature performer, Item toCreate, int x) {
      if (this.rewardTargetContainerId == 0L) {
         performer.sendToLoggers("Inserting into inventory " + toCreate.getName(), (byte)2);
         performer.getInventory().insertItem(toCreate, true);
         if (x < 1) {
            performer.getCommunicator().sendSafeServerMessage("You are rewarded a " + toCreate.getName() + ".");
         }
      } else {
         try {
            Item container = Items.getItem(this.rewardTargetContainerId);
            performer.sendToLoggers("Inserting into " + container.getName() + " " + toCreate.getName(), (byte)2);
            container.insertItem(toCreate, true);
            if (x < 1) {
               performer.getCommunicator().sendSafeServerMessage("A " + container.getName() + " now contains " + toCreate.getName() + ".");
            }
         } catch (NoSuchItemException var5) {
            performer.sendToLoggers("Inserting " + toCreate.getName() + " into inventory because reward container no longer exists ", (byte)2);
            performer.getInventory().insertItem(toCreate, true);
            if (x < 1) {
               performer.getCommunicator().sendSafeServerMessage("You are rewarded a " + toCreate.getName() + ".");
            }
         }
      }
   }

   public void update() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE TRIGGEREFFECTS SET NAME=?,DESCRIPTION=?,REWARDITEM=?,REWARDITEMNUMBERS=?,REWARDQUALITY=?,REWARDBYTE=?,EXISTINGREWARDITEMID=?,REWARDTARGETCONTAINERID=?,REWARDSKILLNUM=?,REWARDSKILLVAL=?,SPECIALEFFECTID=?,SOUND=?,TEXT=?,MISSION=?,MISSIONSTATECHANGE=?,INACTIVE=?,CREATOR=?,CREATEDDATE=?,LASTMODIFIER=?,TRIGGERID=?,DESTROYTARGET=?,ITEMMATERIAL=?,NEWBIE=?,MODIFYTILEX=?,MODIFYTILEY=?,NEWTILETYPE=?,NEWTILEDATA=?,SPAWNTILEX=?,SPAWNTILEY=?,CREATURESPAWN=?,CREATUREAGE=?,CREATURE_TYPE=?,CREATURE_NAME=?,TELEPORTX=?,TELEPORTY=?,MISSIONACTIVATED=?,MISSIONDEACTIVATED=?,TRIGGER_ACTIVATED=?,TRIGGER_DEACTIVATED=?,EFFECT_ACTIVATED=?,EFFECT_DEACTIVATED=?,WSZX=?,WSZY=?,STARTSKILLGAIN=?,STOPSKILLGAIN=?,DESTROYITEMS=?,TOP=?,TELEPORTLAYER=?,ACHIEVEMENTID=? WHERE ID=?"
         );
         ps.setString(1, this.name);
         ps.setString(2, this.description);
         ps.setInt(3, this.rewardItem);
         ps.setInt(4, this.rewardNumbers);
         ps.setInt(5, this.rewardQl);
         ps.setByte(6, this.rewardByteValue);
         ps.setLong(7, this.existingItemReward);
         ps.setLong(8, this.rewardTargetContainerId);
         ps.setInt(9, this.rewardSkillNum);
         ps.setFloat(10, this.rewardSkillVal);
         ps.setInt(11, this.specialEffectId);
         ps.setString(12, this.soundName);
         ps.setString(13, this.textDisplayed);
         ps.setInt(14, this.missionId);
         ps.setFloat(15, this.missionStateChange);
         ps.setBoolean(16, this.inActive);
         ps.setString(17, this.creatorName);
         this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
         ps.setString(18, this.createdDate);
         ps.setString(19, this.lastModifierName);
         ps.setInt(20, this.triggerId);
         ps.setBoolean(21, this.destroysTarget);
         ps.setByte(22, this.itemMaterial);
         ps.setBoolean(23, this.newbieItem);
         ps.setInt(24, this.modifyTileX);
         ps.setInt(25, this.modifyTileY);
         ps.setInt(26, this.newTileType);
         ps.setByte(27, this.newTileData);
         ps.setInt(28, this.spawnTileX);
         ps.setInt(29, this.spawnTileY);
         ps.setInt(30, this.creatureSpawn);
         ps.setInt(31, this.creatureAge);
         ps.setByte(32, this.creatureType);
         ps.setString(33, this.creatureName);
         ps.setInt(34, this.teleportX);
         ps.setInt(35, this.teleportY);
         ps.setInt(36, this.missionToActivate);
         ps.setInt(37, this.missionToDeActivate);
         ps.setInt(38, this.triggerToActivate);
         ps.setInt(39, this.triggerToDeActivate);
         ps.setInt(40, this.effectToActivate);
         ps.setInt(41, this.effectToDeActivate);
         ps.setInt(42, this.windowSizeX);
         ps.setInt(43, this.windowSizeY);
         ps.setBoolean(44, this.startSkillGain);
         ps.setBoolean(45, this.stopSkillGain);
         ps.setBoolean(46, this.destroyItems);
         ps.setString(47, this.topText);
         ps.setInt(48, this.teleportLayer);
         ps.setInt(49, this.achievementId);
         ps.setInt(50, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void create() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(
            "INSERT INTO TRIGGEREFFECTS (NAME, DESCRIPTION,REWARDITEM,REWARDITEMNUMBERS,REWARDQUALITY,REWARDBYTE,EXISTINGREWARDITEMID,REWARDTARGETCONTAINERID,REWARDSKILLNUM,REWARDSKILLVAL,SPECIALEFFECTID,SOUND,TEXT,MISSION,MISSIONSTATECHANGE,INACTIVE,CREATOR,CREATEDDATE,LASTMODIFIER,TRIGGERID,DESTROYTARGET,CREATORID,CREATORTYPE,ITEMMATERIAL,NEWBIE,MODIFYTILEX,MODIFYTILEY,NEWTILETYPE,NEWTILEDATA,SPAWNTILEX,SPAWNTILEY,CREATURESPAWN,CREATUREAGE,CREATURE_TYPE,CREATURE_NAME,TELEPORTX,TELEPORTY,MISSIONACTIVATED,MISSIONDEACTIVATED,TRIGGER_ACTIVATED,TRIGGER_DEACTIVATED,EFFECT_ACTIVATED,EFFECT_DEACTIVATED,WSZX,WSZY,STARTSKILLGAIN,STOPSKILLGAIN,DESTROYITEMS,TOP,TELEPORTLAYER,ACHIEVEMENTID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            1
         );
         ps.setString(1, this.name);
         ps.setString(2, this.description);
         ps.setInt(3, this.rewardItem);
         ps.setInt(4, this.rewardNumbers);
         ps.setInt(5, this.rewardQl);
         ps.setByte(6, this.rewardByteValue);
         ps.setLong(7, this.existingItemReward);
         ps.setLong(8, this.rewardTargetContainerId);
         ps.setInt(9, this.rewardSkillNum);
         ps.setFloat(10, this.rewardSkillVal);
         ps.setInt(11, this.specialEffectId);
         ps.setString(12, this.soundName);
         ps.setString(13, this.textDisplayed);
         ps.setInt(14, this.missionId);
         ps.setFloat(15, this.missionStateChange);
         ps.setBoolean(16, this.inActive);
         ps.setString(17, this.creatorName);
         this.createdDate = DateFormat.getDateInstance(2).format(new Timestamp(System.currentTimeMillis()));
         this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
         ps.setString(18, this.createdDate);
         ps.setString(19, this.lastModifierName);
         ps.setInt(20, this.triggerId);
         ps.setBoolean(21, this.destroysTarget);
         ps.setLong(22, this.ownerId);
         ps.setByte(23, this.creatorType);
         ps.setByte(24, this.itemMaterial);
         ps.setBoolean(25, this.newbieItem);
         ps.setInt(26, this.modifyTileX);
         ps.setInt(27, this.modifyTileY);
         ps.setInt(28, this.newTileType);
         ps.setByte(29, this.newTileData);
         ps.setInt(30, this.spawnTileX);
         ps.setInt(31, this.spawnTileY);
         ps.setInt(32, this.creatureSpawn);
         ps.setInt(33, this.creatureAge);
         ps.setByte(34, this.creatureType);
         ps.setString(35, this.creatureName);
         ps.setInt(36, this.teleportX);
         ps.setInt(37, this.teleportY);
         ps.setInt(38, this.missionToActivate);
         ps.setInt(39, this.missionToDeActivate);
         ps.setInt(40, this.triggerToActivate);
         ps.setInt(41, this.triggerToDeActivate);
         ps.setInt(42, this.effectToActivate);
         ps.setInt(43, this.effectToDeActivate);
         ps.setInt(44, this.windowSizeX);
         ps.setInt(45, this.windowSizeY);
         ps.setBoolean(46, this.startSkillGain);
         ps.setBoolean(47, this.stopSkillGain);
         ps.setBoolean(48, this.destroyItems);
         ps.setString(49, this.topText);
         ps.setInt(50, this.teleportLayer);
         ps.setInt(51, this.achievementId);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (rs.next()) {
            this.id = rs.getInt(1);
         }

         logger.log(Level.INFO, "Trigger effect " + this.name + " (" + this.id + ") created at " + this.createdDate);
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void destroy() {
      TriggerEffects.removeEffect(this.id);
      Triggers2Effects.deleteEffect(this.id);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM TRIGGEREFFECTS WHERE ID=?");
         ps.setInt(1, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      this.destroyed = true;
   }

   public boolean isDestroyed() {
      return this.destroyed;
   }

   public int compareTo(TriggerEffect aTriggerEffect) {
      return this.getName().compareTo(aTriggerEffect.getName());
   }

   public int getAchievementId() {
      return this.achievementId;
   }

   public void setAchievementId(int aAchievementId) {
      this.achievementId = aAchievementId;
   }

   public boolean hasTargetOf(long currentTargetId, Creature performer) {
      MissionTrigger t = MissionTriggers.getTriggerWithId(this.triggerId);
      if (t != null) {
         return t.getTarget() == currentTargetId;
      } else {
         return false;
      }
   }
}

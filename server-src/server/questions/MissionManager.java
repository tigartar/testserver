package com.wurmonline.server.questions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.tutorial.SpecialEffects;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.tutorial.Triggers2Effects;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.CreatureTypes;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class MissionManager extends Question implements CounterTypes, TimeConstants, CreatureTypes {
   private int level = 0;
   private int missionId = 0;
   private int triggerId = 0;
   private int effectId = 0;
   private static final float REWSK1 = 0.001F;
   private static final float REWSK2 = 0.002F;
   private static final float REWSK3 = 0.01F;
   private static final float REWSK4 = 0.05F;
   private static final float REWSK5 = 0.1F;
   private static final float REWSK6 = 1.0F;
   private static final float REWSK7 = 10.0F;
   private static final float REWSK8 = 20.0F;
   private static final String percent = "%";
   private static final String percentComma = "%,";
   private static final int INTRO = 0;
   private static final int CREATE_MISSION = 1;
   private static final int EDIT_MISSION = 2;
   private static final int EDIT_TRIGGER = 3;
   private static final int CREATE_TRIGGER = 4;
   private static final int LIST_MISSIONS = 5;
   private static final int LIST_TRIGGERS = 6;
   private static final int EDIT_EFFECT = 7;
   private static final int CREATE_EFFECT = 8;
   private static final int LIST_EFFECTS = 9;
   private static Logger logger = Logger.getLogger(MissionManager.class.getName());
   private LinkedList<ItemTemplate> itemplates = new LinkedList<>();
   private LinkedList<Item> ritems = new LinkedList<>();
   private LinkedList<MissionTrigger> mtriggers = new LinkedList<>();
   private LinkedList<MissionTrigger> utriggers = new LinkedList<>();
   private LinkedList<SkillTemplate> stemplates = new LinkedList<>();
   private LinkedList<ActionEntry> actionEntries = new LinkedList<>();
   private LinkedList<Mission> missionsAvail = new LinkedList<>();
   private LinkedList<SpecialEffects> effectsAvail = new LinkedList<>();
   private LinkedList<CreatureTemplate> creaturesAvail = new LinkedList<>();
   private LinkedList<TriggerEffect> teffects = new LinkedList<>();
   private LinkedList<TriggerEffect> ueffects = new LinkedList<>();
   private LinkedList<AchievementTemplate> myAchievements = null;
   private LinkedList<Byte> creaturesTypes = new LinkedList<>();
   private final String targName;
   private final long missionRulerId;
   private boolean listMineOnly = false;
   private boolean dontListMine = false;
   private boolean onlyCurrent = false;
   private int includeM = 0;
   private boolean incMInactive = true;
   private boolean typeSystem = true;
   private boolean typeGM = true;
   private boolean typePlayer = true;
   private long listForUser = -10L;
   private String userName = "";
   private String groupName = "";
   private boolean incTInactive = true;
   private int showT = 0;
   private boolean incEInactive = true;
   private int showE = 0;
   private long currentTargetId = -10L;
   private int sortBy = 2;
   private String origQuestion = "";
   private String origTitle = "";
   private String lastQuestion = "";
   private String lastTitle = "";
   private String errorText = "";
   private byte creatorType = 0;
   private String sbacks = "";
   public static byte CAN_SEE_EPIC_MISSIONS = 2;
   private static final String red = "color=\"255,127,127\"";
   private static final String green = "color=\"127,255,127\"";
   private static final String orange = "color=\"255,177,40\"";
   private static final String blue = "color=\"140,140,255\";";
   private static final String hoverActive = ";hover=\"Active\"";
   private static final String hoverInactive = ";hover=\"Inactive\"";
   private static final String hoverNoTriggers = ";hover=\"No Triggers\"";
   private static final String hoverCurrentTarget = ";hover=\"Current Target\"";
   private static final String hoverNotImplemented = ";hover=\"Not implemented (yet)\"";
   private static final String line = "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}";

   public MissionManager(Creature aResponder, String aTitle, String aQuestion, long aTarget, String targetName, long _missionRulerId) {
      super(aResponder, aTitle, aQuestion, 86, aTarget);
      this.targName = targetName;
      this.missionRulerId = _missionRulerId;
      this.currentTargetId = aTarget;
      this.origQuestion = aQuestion;
      this.origTitle = aTitle;
      this.lastQuestion = aQuestion;
      this.lastTitle = aTitle;
      if (aResponder.getPower() <= 1) {
         this.listMineOnly = true;
         this.typeSystem = false;
         this.typeGM = false;
      }
   }

   @Override
   public void answer(Properties aAnswers) {
      Item ruler = null;

      try {
         ruler = Items.getItem(this.missionRulerId);
         if (ruler.getOwnerId() != this.getResponder().getWurmId()) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You are not the ruler of this mission ruler.");
            return;
         }
      } catch (NoSuchItemException var4) {
         this.getResponder().getCommunicator().sendNormalServerMessage("The mission ruler is gone!");
         return;
      }

      this.setAnswer(aAnswers);
      this.parseAnswer(ruler);
   }

   private boolean parseAnswer(Item ruler) {
      boolean back = this.getBooleanProp("back");
      if (back) {
         return this.parseBack();
      } else {
         switch(this.level) {
            case 0:
               return this.parseIntro();
            case 1:
            case 2:
               return this.parseMission(ruler);
            case 3:
            case 4:
               return this.parseTrigger(ruler);
            case 5:
               return this.parseMissionList();
            case 6:
               return this.parseTriggerList();
            case 7:
            case 8:
               return this.parseEffect(ruler);
            case 9:
               return this.parseEffectsList();
            default:
               return false;
         }
      }
   }

   private boolean parseBack() {
      String[] backs = this.sbacks.split(Pattern.quote("|"));
      if (backs.length <= 0) {
         return this.showIntro();
      } else {
         this.errorText = "";
         StringBuilder buf = new StringBuilder();
         if (backs.length > 1) {
            buf.append(backs[0]);

            for(int s = 1; s < backs.length - 1; ++s) {
               buf.append("|" + backs[s]);
            }
         }

         this.sbacks = buf.toString();
         String[] lparts = backs[backs.length - 1].split(",");
         int newLevel = Integer.parseInt(lparts[0]);
         this.missionId = Integer.parseInt(lparts[1]);
         this.triggerId = Integer.parseInt(lparts[2]);
         this.effectId = Integer.parseInt(lparts[3]);
         switch(newLevel) {
            case 0:
               return this.showIntro();
            case 1:
            case 2:
               return this.editMission(this.missionId, null);
            case 3:
            case 4:
               return this.editTrigger(this.triggerId, null);
            case 5:
               this.sortBy = 2;
               return this.showMissionList();
            case 6:
               this.sortBy = 2;
               return this.showTriggerList(this.missionId);
            case 7:
            case 8:
               return this.editEffect(this.effectId, null);
            case 9:
               this.sortBy = 2;
               return this.showEffectsList(this.missionId, this.triggerId);
            default:
               return false;
         }
      }
   }

   private boolean parseIntro() {
      this.sbacks = "0,0,0,0";
      this.missionId = 0;
      this.triggerId = 0;
      this.effectId = 0;
      if (this.getResponder().getPower() > 0) {
         this.listMineOnly = this.getBooleanProp("listmine");
         this.dontListMine = this.getBooleanProp("nolistmine");
         this.onlyCurrent = this.getBooleanProp("onlyCurrent");
         this.includeM = this.getIntProp("includeM");
         this.incMInactive = this.getBooleanProp("incMInactive");
         this.typeSystem = this.getBooleanProp("typeSystem");
         this.typeGM = this.getBooleanProp("typeGM");
         this.typePlayer = this.getBooleanProp("typePlayer");
      }

      this.groupName = this.getStringProp("groupName");
      if (this.groupName == null) {
         this.groupName = "";
      }

      String specialName = this.getStringProp("specialName");
      this.parsePlayerName(specialName);
      this.incTInactive = this.getBooleanProp("incTInactive");
      this.incEInactive = this.getBooleanProp("incEInactive");
      this.showE = this.getIntProp("showE");
      boolean listMissions = this.getBooleanProp("listMissions");
      boolean listTriggers = this.getBooleanProp("listTriggers");
      boolean listEffects = this.getBooleanProp("listEffects");
      boolean createMission = this.getBooleanProp("createMission");
      boolean createTrigger = this.getBooleanProp("createTrigger");
      boolean createEffect = this.getBooleanProp("createEffect");
      this.sortBy = 2;
      if (listMissions) {
         return this.showMissionList();
      } else if (listTriggers) {
         return this.showTriggerList(0);
      } else if (listEffects) {
         return this.showEffectsList(0, 0);
      } else if (createMission) {
         return this.createNewMission(null);
      } else if (createTrigger) {
         return this.createNewTrigger(null);
      } else {
         return createEffect ? this.createNewEffect(null) : false;
      }
   }

   private boolean parseMissionList() {
      this.missionId = 0;
      this.triggerId = 0;
      boolean filter = this.getBooleanProp("filter");
      boolean editMission = this.getBooleanProp("editMission");
      boolean showStats = this.getBooleanProp("showStats");
      boolean listTriggers = this.getBooleanProp("listTriggers");
      boolean listEffects = this.getBooleanProp("listEffects");
      boolean createMission = this.getBooleanProp("createMission");
      if (filter) {
         if (this.getResponder().getPower() > 0) {
            this.includeM = Integer.parseInt(this.getAnswer().getProperty("includeM"));
            this.incMInactive = this.getBooleanProp("incMInactive");
         }

         this.groupName = this.getAnswer().getProperty("groupName");
         if (this.groupName == null) {
            this.groupName = "";
         }

         String specialName = this.getAnswer().getProperty("specialName");
         this.parsePlayerName(specialName);
         return this.showMissionList();
      } else if (createMission) {
         this.sbacks = this.sbacks + "|" + 5 + ",0,0,0";
         return this.createNewMission(null);
      } else {
         String sel = this.getAnswer().getProperty("sel");
         int mid = Integer.parseInt(sel);
         if (editMission) {
            this.sbacks = this.sbacks + "|" + 5 + ",0,0,0";
            return mid == 0 ? this.createNewMission(null) : this.editMission(mid, null);
         } else if (showStats) {
            return this.showStats(mid);
         } else if (listTriggers) {
            this.sortBy = 2;
            this.sbacks = this.sbacks + "|" + 5 + ",0,0,0";
            return this.showTriggerList(mid);
         } else if (listEffects) {
            this.sortBy = 2;
            this.sbacks = this.sbacks + "|" + 5 + ",0,0,0";
            return this.showEffectsList(mid, 0);
         } else {
            for(String key : this.getAnswer().stringPropertyNames()) {
               if (key.startsWith("sort")) {
                  String sid = key.substring(4);
                  this.sortBy = Integer.parseInt(sid);
                  break;
               }
            }

            return this.showMissionList();
         }
      }
   }

   private boolean parseTriggerList() {
      boolean filter = this.getBooleanProp("filter");
      boolean editTrigger = this.getBooleanProp("editTrigger");
      boolean listEffects = this.getBooleanProp("listEffects");
      boolean createTrigger = this.getBooleanProp("createTrigger");
      if (filter) {
         this.incTInactive = this.getBooleanProp("incTInactive");
         this.showT = Integer.parseInt(this.getAnswer().getProperty("showT"));
         return this.showTriggerList(this.missionId);
      } else if (createTrigger) {
         this.sbacks = this.sbacks + "|" + 6 + "," + this.missionId + ",0,0";
         return this.createNewTrigger(null);
      } else {
         String sel = this.getAnswer().getProperty("sel");
         int tid = Integer.parseInt(sel);
         if (editTrigger) {
            this.sbacks = this.sbacks + "|" + 6 + "," + this.missionId + ",0,0";
            return tid == 0 ? this.createNewTrigger(null) : this.editTrigger(tid, null);
         } else if (listEffects) {
            this.sortBy = 2;
            this.sbacks = this.sbacks + "|" + 6 + "," + this.missionId + ",0,0";
            return this.showEffectsList(this.missionId, this.triggerId);
         } else {
            for(String key : this.getAnswer().stringPropertyNames()) {
               if (key.startsWith("sort")) {
                  String sid = key.substring(4);
                  this.sortBy = Integer.parseInt(sid);
                  break;
               }

               if (key.startsWith("delT")) {
                  String sid = key.substring(4);
                  int trigId = Integer.parseInt(sid);
                  MissionTrigger trg = MissionTriggers.getTriggerWithId(trigId);
                  if (trg == null) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find trigger!");
                  } else {
                     trg.destroy();
                     this.getResponder().getCommunicator().sendNormalServerMessage("You delete the trigger " + trg.getName() + ".");
                  }
                  break;
               }
            }

            return this.showTriggerList(this.missionId);
         }
      }
   }

   private boolean parseEffectsList() {
      boolean filter = this.getBooleanProp("filter");
      boolean editEffect = this.getBooleanProp("editEffect");
      boolean showTriggers = this.getBooleanProp("showTriggers");
      boolean createEffect = this.getBooleanProp("createEffect");
      if (filter) {
         this.incEInactive = this.getBooleanProp("incEInactive");
         this.showE = Integer.parseInt(this.getAnswer().getProperty("showE"));
         return this.showEffectsList(this.missionId, this.triggerId);
      } else if (createEffect) {
         this.sbacks = this.sbacks + "|" + 9 + "," + this.missionId + "," + this.triggerId + ",0";
         return this.createNewEffect(null);
      } else {
         String sel = this.getAnswer().getProperty("sel");
         int eid = Integer.parseInt(sel);
         if (editEffect) {
            this.sbacks = this.sbacks + "|" + 9 + "," + this.missionId + "," + this.triggerId + ",0";
            return eid == 0 ? this.createNewEffect(null) : this.editEffect(eid, null);
         } else {
            if (showTriggers) {
               this.getResponder().getCommunicator().sendNormalServerMessage("Not Implemented (yet)");
            } else {
               for(String key : this.getAnswer().stringPropertyNames()) {
                  if (key.startsWith("sort")) {
                     String sid = key.substring(4);
                     this.sortBy = Integer.parseInt(sid);
                     break;
                  }
               }
            }

            return this.showEffectsList(this.missionId, this.triggerId);
         }
      }
   }

   private void parsePlayerName(String playerName) {
      if (playerName != null && playerName.length() > 0) {
         PlayerInfo pf = PlayerInfoFactory.createPlayerInfo(playerName);

         try {
            pf.load();
            this.listForUser = pf.wurmId;
            this.userName = pf.getName();
         } catch (IOException var4) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such player: " + playerName + ".");
            this.listForUser = -10L;
            this.userName = "";
         }
      } else {
         this.listForUser = -10L;
         this.userName = "";
      }
   }

   private boolean showIntro() {
      MissionManager mm = new MissionManager(this.getResponder(), this.origTitle, this.origQuestion, this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = 0;
      mm.sendQuestion();
      return true;
   }

   private boolean showMissionList() {
      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": Listing MISSIONS");
      }

      MissionManager mm = new MissionManager(this.getResponder(), "Mission list", "Mission list", this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = 5;
      mm.sendMissionList();
      return true;
   }

   private boolean editMission(int mid, @Nullable Properties ans) {
      String name = "";
      if (ans == null) {
         if (mid == 0) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No mission selected!");
            return false;
         }

         Mission msn = Missions.getMissionWithId(mid);
         if (msn == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
            return false;
         }

         name = msn.getName();
      } else {
         name = ans.getProperty("name", "");
      }

      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": Edit MISSION with name " + name + " and id " + mid);
      }

      MissionManager mm = new MissionManager(this.getResponder(), "Edit mission", "Edit mission:", this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      if (ans == null) {
         mm.level = 2;
         mm.missionId = mid;
      } else {
         this.getResponder().getCommunicator().sendAlertServerMessage(this.errorText);
         mm.level = this.level;
         mm.setAnswer(this.getAnswer());
      }

      mm.sendManageMission();
      return true;
   }

   private boolean editTrigger(int tid, @Nullable Properties ans) {
      String name = "";
      if (ans == null) {
         if (tid == 0) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No trigger selected!");
            return false;
         }

         MissionTrigger trg = MissionTriggers.getTriggerWithId(tid);
         if (trg == null) {
            this.errorText = "Cannot find trigger!";
            this.getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
            return false;
         }

         name = trg.getName();
      } else {
         name = ans.getProperty("name", "");
      }

      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": Edit TRIGGER with name " + name + " and id " + tid);
      }

      MissionManager mm = new MissionManager(this.getResponder(), "Mission triggers", "Edit mission trigger", this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      if (ans == null) {
         mm.triggerId = tid;
         mm.level = 3;
      } else {
         this.getResponder().getCommunicator().sendAlertServerMessage(this.errorText);
         mm.level = this.level;
         mm.setAnswer(ans);
      }

      mm.sendManageTrigger();
      return true;
   }

   private boolean editEffect(int eid, @Nullable Properties ans) {
      String name = "";
      if (ans == null) {
         if (eid == 0) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No mission selected!");
            return false;
         }

         TriggerEffect eff = TriggerEffects.getTriggerEffect(eid);
         if (eff == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find effect!");
            return false;
         }

         name = eff.getName();
      } else {
         name = ans.getProperty("name", "");
      }

      MissionManager mm = new MissionManager(
         this.getResponder(), "Trigger effect", "Edit trigger effect " + name, this.target, this.targName, this.missionRulerId
      );
      this.cloneValues(mm);
      mm.triggerId = 0;
      if (ans == null) {
         mm.effectId = eid;
         mm.level = 7;
      } else {
         this.getResponder().getCommunicator().sendAlertServerMessage(this.errorText);
         mm.level = this.level;
         mm.setAnswer(ans);
      }

      mm.sendManageEffect();
      return true;
   }

   private boolean showStats(int mid) {
      if (mid == 0) {
         this.getResponder().getCommunicator().sendNormalServerMessage("No mission selected!");
         return false;
      } else {
         Mission msn = Missions.getMissionWithId(mid);
         if (msn == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
            return false;
         } else {
            MissionStats ms = new MissionStats(this.getResponder(), "Mission statistics", "Statistics for " + msn.getName(), (long)msn.getId());
            ms.setRoot(this);
            ms.sendQuestion();
            return true;
         }
      }
   }

   private boolean showTriggerList(int mid) {
      String mTitle;
      String mQuestion;
      if (mid == 0) {
         mTitle = "Trigger List";
         mQuestion = "Listing all mission triggers";
      } else {
         Mission msn = Missions.getMissionWithId(mid);
         if (msn == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
            return false;
         }

         mTitle = msn.getName() + " Trigger List";
         mQuestion = "Listing mission " + msn.getName() + " triggers";
      }

      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": " + mTitle);
      }

      MissionManager mm = new MissionManager(this.getResponder(), mTitle, mQuestion, this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = 6;
      mm.missionId = mid;
      mm.sendTriggerList();
      return true;
   }

   private boolean showEffectsList(int mid, int tid) {
      String mTitle;
      String mQuestion;
      if (mid == 0) {
         if (tid == 0) {
            mTitle = "Mission Effects List";
            mQuestion = "Listing all mission trigger effects";
         } else {
            MissionTrigger mt = MissionTriggers.getTriggerWithId(tid);
            if (mt == null) {
               this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find trigger!");
               return false;
            }

            mTitle = "Trigger " + mt.getName() + " Effects List";
            mQuestion = "Listing trigger " + mt.getName() + "  effects";
         }
      } else {
         Mission msn = Missions.getMissionWithId(mid);
         if (msn == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find mission!");
            return false;
         }

         if (tid == 0) {
            mTitle = msn.getName() + " Trigger Effects List";
            mQuestion = "Listing mission " + msn.getName() + " trigger effects";
         } else {
            MissionTrigger mt = MissionTriggers.getTriggerWithId(tid);
            if (mt == null) {
               this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find trigger!");
               return false;
            }

            mTitle = msn.getName() + " Trigger " + mt.getName() + " Effects List";
            mQuestion = "Listing trigger " + mt.getName() + "  effects";
         }
      }

      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": " + mTitle);
      }

      MissionManager mm = new MissionManager(this.getResponder(), mTitle, mQuestion, this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = 9;
      mm.missionId = mid;
      mm.triggerId = tid;
      mm.sendEffectList();
      return true;
   }

   private boolean createNewMission(@Nullable Properties ans) {
      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": Create new MISSION");
      }

      MissionManager mm = new MissionManager(this.getResponder(), "Create New Mission", "Create New Mission:", this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = 1;
      mm.missionId = 0;
      mm.setAnswer(ans);
      mm.sendManageMission();
      return true;
   }

   private boolean createNewTrigger(@Nullable Properties ans) {
      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": Create TRIGGER");
      }

      MissionManager mm = new MissionManager(
         this.getResponder(), "New mission trigger", "Create a new mission trigger", this.target, this.targName, this.missionRulerId
      );
      this.cloneValues(mm);
      mm.level = 4;
      mm.missionId = 0;
      mm.triggerId = 0;
      mm.setAnswer(ans);
      mm.sendManageTrigger();
      return true;
   }

   private boolean createNewEffect(@Nullable Properties ans) {
      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0) {
         this.getResponder().getLogger().info(this.getResponder() + ": Create EFFECT");
      }

      MissionManager mm = new MissionManager(
         this.getResponder(), "New mission effect", "Create a new mission trigger effect", this.target, this.targName, this.missionRulerId
      );
      this.cloneValues(mm);
      mm.level = 8;
      mm.missionId = 0;
      mm.triggerId = 0;
      mm.effectId = 0;
      mm.setAnswer(ans);
      mm.sendManageEffect();
      return true;
   }

   private void cloneValues(MissionManager mm) {
      mm.missionId = this.missionId;
      mm.triggerId = this.triggerId;
      mm.effectId = this.effectId;
      mm.listMineOnly = this.listMineOnly;
      mm.dontListMine = this.dontListMine;
      mm.onlyCurrent = this.onlyCurrent;
      mm.includeM = this.includeM;
      mm.incMInactive = this.incMInactive;
      mm.typeSystem = this.typeSystem;
      mm.typeGM = this.typeGM;
      mm.typePlayer = this.typePlayer;
      mm.listForUser = this.listForUser;
      mm.userName = this.userName;
      mm.groupName = this.groupName;
      mm.currentTargetId = this.currentTargetId;
      mm.origQuestion = this.origQuestion;
      mm.origTitle = this.origTitle;
      mm.lastQuestion = this.lastQuestion;
      mm.lastTitle = this.lastTitle;
      mm.incTInactive = this.incTInactive;
      mm.showT = this.showT;
      mm.incEInactive = this.incEInactive;
      mm.showE = this.showE;
      mm.sbacks = this.sbacks;
      mm.errorText = this.errorText;
      mm.sortBy = this.sortBy;
      mm.creatorType = this.creatorType;
      mm.itemplates = this.itemplates;
      mm.ritems = this.ritems;
      mm.mtriggers = this.mtriggers;
      mm.stemplates = this.stemplates;
      mm.actionEntries = this.actionEntries;
      mm.missionsAvail = this.missionsAvail;
      mm.effectsAvail = this.effectsAvail;
      mm.creaturesAvail = this.creaturesAvail;
      mm.myAchievements = this.myAchievements;
      mm.creaturesTypes = this.creaturesTypes;
   }

   protected void cloneAndSendManageEffect(@Nullable String sound) {
      MissionManager mm = new MissionManager(this.getResponder(), this.lastTitle, this.lastQuestion, this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = this.level;
      mm.setAnswer(this.getAnswer());
      if (sound != null) {
         mm.getAnswer().setProperty("sound", sound);
      }

      mm.sendManageEffect();
   }

   protected boolean reshow() {
      MissionManager mm = new MissionManager(this.getResponder(), this.lastTitle, this.lastQuestion, this.target, this.targName, this.missionRulerId);
      this.cloneValues(mm);
      mm.level = this.level;
      mm.lastQuestion = this.lastQuestion;
      mm.lastTitle = this.lastTitle;
      mm.sbacks = this.sbacks;
      switch(this.level) {
         case 0:
            mm.sendQuestion();
            return true;
         case 1:
         case 2:
            mm.setAnswer(this.getAnswer());
            mm.sendManageMission();
            return true;
         case 3:
         case 4:
            mm.setAnswer(this.getAnswer());
            mm.sendManageTrigger();
            return true;
         case 5:
            mm.sendMissionList();
            return true;
         case 6:
            mm.sendTriggerList();
            return true;
         case 7:
         case 8:
            mm.setAnswer(this.getAnswer());
            mm.sendManageEffect();
            return true;
         case 9:
            mm.sendEffectList();
            return true;
         default:
            return false;
      }
   }

   private void sendManageTrigger() {
      MissionTrigger trg = null;
      String name = "";
      boolean inactive = false;
      String desc = "";
      int itemUsedId = -10;
      int actionId = 0;
      long trgTarget = 0L;
      boolean useCurrentTarget = true;
      String tgtString = "";
      boolean spawnpoint = false;
      String trigsecs = "0";
      int missionRequired = 0;
      String stateFromString = "0.0";
      String stateToString = "0.0";
      if (this.getAnswer() != null) {
         name = this.getStringProp("name");
         inactive = this.getBooleanProp("inactive");
         desc = this.getStringProp("desc");
         itemUsedId = this.indexItemTemplate("onItemCreatedId", "Item Created");
         actionId = this.indexActionId("actionId", "action");
         trgTarget = this.getLongProp("targetid");
         useCurrentTarget = this.getBooleanProp("useCurrentTarget");
         tgtString = MissionTriggers.getTargetAsString(this.getResponder(), trgTarget);
         spawnpoint = this.getBooleanProp("spawnpoint");
         trigsecs = this.getStringProp("seconds");
         missionRequired = this.indexMission("missionRequired", "available missions");
         stateFromString = this.getStringProp("stateFrom");
         stateToString = this.getStringProp("stateTo");
      } else if (this.triggerId > 0) {
         trg = MissionTriggers.getTriggerWithId(this.triggerId);
         if (trg != null) {
            name = trg.getName();
            inactive = trg.isInactive();
            desc = trg.getDescription();
            itemUsedId = trg.getItemUsedId();
            actionId = trg.getOnActionPerformed();
            trgTarget = trg.getTarget();
            useCurrentTarget = trgTarget == this.currentTargetId;
            tgtString = MissionTriggers.getTargetAsString(this.getResponder(), trg.getTarget());
            if (this.getResponder().getPower() > 0) {
               spawnpoint = trg.isSpawnPoint();
            }

            trigsecs = Integer.toString(trg.getSeconds());
            missionRequired = trg.getMissionRequired();
            stateFromString = Float.toString(trg.getStateRequired());
            stateToString = Float.toString(trg.getStateEnd());
         }
      } else {
         tgtString = "None";
      }

      String currentString = MissionTriggers.getTargetAsString(this.getResponder(), this.currentTargetId);
      StringBuilder buf = new StringBuilder();
      buf.append(
         "border{border{size=\"20,20\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\""
            + this.question
            + "    \"};label{type=\"bolditalic\";"
            + "color=\"255,127,127\""
            + ";text=\""
            + this.errorText
            + "\"}}}harray{button{id=\"back\";text=\"Back\"};label{text=\"  \"}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      buf.append(
         "harray{label{text=\"Name (max 40 chars)\"};input{id=\"name\";maxchars=\"40\";text=\""
            + name
            + "\"};label{text=\" \"};checkbox{id=\"inactive\";selected=\""
            + inactive
            + "\"};label{text=\"Inactive \"};}"
      );
      buf.append("label{text=\"Description (max 100 chars)\"};");
      buf.append("input{id=\"intro\";maxchars=\"400\"text=\"" + desc + "\";maxlines=\"1\"};");
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"How triggered\"}");
      buf.append("harray{label{text=\"On item used\"};" + this.dropdownItemTemplates("onItemCreatedId", itemUsedId, false) + "}");
      String curColour = "";
      if (trgTarget == this.currentTargetId && trgTarget > 0L) {
         curColour = "color=\"140,140,255\";";
      }

      if (this.getResponder().getPower() > 0) {
         buf.append(
            "harray{label{text=\"Action performed\"};"
               + this.dropdownActions("actionId", (short)actionId)
               + "label{text=\" on target \"};label{"
               + curColour
               + "text=\""
               + tgtString
               + "\"};}"
         );
         buf.append(
            "harray{checkbox{text=\"\";id=\"useCurrentTarget\";selected=\""
               + useCurrentTarget
               + "\"}label{text=\"Use current: \"};label{"
               + "color=\"140,140,255\";"
               + "text=\""
               + currentString
               + "\"};label{text=\" Current Id: \"};input{id=\"targetid\";text=\""
               + this.currentTargetId
               + "\";}}"
         );
      } else {
         buf.append(
            "harray{label{text=\"Action performed\"};"
               + this.dropdownActions("actionId", (short)actionId)
               + "label{text=\" on target \"};label{"
               + curColour
               + "text=\""
               + tgtString
               + "\"};}"
         );
         buf.append(
            "harray{checkbox{text=\"\";id=\"useCurrentTarget\";selected=\""
               + useCurrentTarget
               + "\"}label{text=\"Use current: \"};label{"
               + "color=\"140,140,255\";"
               + "text=\""
               + currentString
               + "\"};passthrough{id=\"targetid\";text=\""
               + trgTarget
               + "\"}}"
         );
      }

      buf.append("text{text=\"\"}");
      buf.append("header{text=\"General\"}");
      buf.append(
         "harray{"
            + (this.getResponder().getPower() > 0 ? "label{text=\"Target is Spawn Point \"};checkbox{id=\"spawnpoint\";selected=\"" + spawnpoint + "\"};" : "")
            + "label{text=\"Seconds to trigger \"};input{id=\"seconds\";text=\""
            + trigsecs
            + "\";maxchars=\"2\"};}"
      );
      buf.append("harray{label{text=\"Trigger Mission \"};" + this.dropdownMissions("missionRequired", missionRequired, true) + "}");
      buf.append(
         "harray{label{text=\"Triggered state from \"};input{id=\"stateFrom\";maxchars=\"5\";text=\""
            + stateFromString
            + "\"};label{text=\" to \"};input{id=\"stateTo\";maxchars=\"5\";text=\""
            + stateToString
            + "\"};label{"
            + "color=\"140,140,255\";"
            + "text=\"  See Note 4.\";hover=\"The state 'to' is only valid if greater than the 'from'. Leave 'to' as '0.0' if not required.\"}label{text=\"  % Use '.' for decimals.\"}};"
      );
      buf.append("text{text=\"\"}");
      if (this.level == 4) {
         buf.append(this.appendChargeInfo());
         buf.append("harray{button{text=\"Create Trigger\";id=\"createTrigger\"};}");
      } else {
         buf.append(
            "harray{button{text=\"Update Trigger\";id=\"updateTrigger\"};label{text=\"  \"};button{text=\"Delete Trigger\";id=\"deleteTrigger\"hover=\"This will delete "
               + name
               + "\";confirm=\"You are about to delete "
               + name
               + ".\";question=\"Do you really want to do that?\"};label{text=\"  \"};button{id=\"cloneTrigger\";text=\"Clone Trigger\"hover=\"This will show a copy of this trigger for creation\";};}"
         );
      }

      buf.append("text{type=\"bold\";text=\"Notes:\"}");
      buf.append("text{type=\"italic\";text=\"1. Creating a trigger on a tile with the action 'Step On' will disregard the item used.\"}");
      buf.append(
         "text{type=\"italic\";text=\"2. Using the 'Create' action will only trigger when the selected item is manufactured or finished by polishing.\"}"
      );
      buf.append("text{type=\"italic\";text=\"3. Be careful with restartable and second chance missions. They can really mess things up.\"}");
      buf.append("text{type=\"italic\";text=\"4. The state 'to' is only valid if greater than the 'from'. Leave 'to' as '0.0' if not required.\"}");
      if (trg != null) {
         buf.append("text{type=\"italic\";text=\"5. A trigger effect that triggers on Not Started (0) will start a new mission and set its state to 1.\"}");
         buf.append(
            "text{type=\"italic\";text=\"6. A trigger effect may increase the state of a mission (does not have to be the mission that this trigger is attached to).\"}"
         );
         buf.append("text{type=\"italic\";text=\"7. If the state of a mission is higher than 0 it is considered started.\"}");
         buf.append("text{text=\"    If it is 100.0% the mission is finished. If the state is set to -1.0 it has failed.\"}");
         buf.append(
            "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
         );
         buf.append(
            "harray{label{type=\"bold\";text=\"Effects:     \"}button{id=\"createEffect\";text=\"Create New Effect\"};label{text=\"  All Effects\"}"
               + this.dropdownUnlinkedEffects("linkEffects", 0)
               + "label{text=\" \"}button{id=\"linkEffect\";text=\"Link Effect\"};}"
         );
         MissionTrigger[] trigs = new MissionTrigger[]{trg};
         TriggerEffect[] teffs = TriggerEffects.getFilteredEffects(
            trigs, this.getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, false
         );
         if (teffs.length > 0) {
            Arrays.sort((Object[])teffs);
            buf.append("table{rows=\"1\";cols=\"7\";");
            buf.append(
               "label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"+State\"};label{text=\"Type\"};label{text=\"\"};label{text=\"\"};"
            );

            for(TriggerEffect lEff : teffs) {
               String colour = "color=\"127,255,127\"";
               String hover = ";hover=\"Active\"";
               if (lEff.isInactive()) {
                  colour = "color=\"255,127,127\"";
                  hover = ";hover=\"Inactive\"";
               } else if (lEff.hasTargetOf(this.currentTargetId, this.getResponder())) {
                  colour = "color=\"140,140,255\";";
                  hover = ";hover=\"Current Target\"";
               }

               String clrtxt = "label{text=\"         \";hover=\"no text to clear\"}";
               if (!lEff.getTextDisplayed().isEmpty()) {
                  clrtxt = "harray{label{text=\" \"};button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
               }

               buf.append(
                  "harray{button{id=\"edtE"
                     + lEff.getId()
                     + "\";text=\"Edit\"};label{text=\" \"};};label{"
                     + colour
                     + "text=\""
                     + lEff.getId()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getName()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getMissionStateChange()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getType()
                     + " \""
                     + hover
                     + "};"
                     + clrtxt
                     + "harray{label{text=\" \"};button{id=\"unlE"
                     + lEff.getId()
                     + "\";text=\"Unlink\"};};"
               );
            }

            buf.append("}");
         } else {
            buf.append("label{text=\"No trigger effects found\"}");
         }
      }

      buf.append("}};null;null;}");
      if (trg == null) {
         this.getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
      } else {
         this.getResponder().getCommunicator().sendBml(630, 600, true, true, buf.toString(), 200, 200, 200, this.title);
      }
   }

   private long indexExistingItem(String key, long existingItem) {
      long ans = 0L;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         if (this.getResponder().getPower() > 0) {
            try {
               long newexistingItem = Long.parseLong(sIndex);
               if (newexistingItem != existingItem) {
                  try {
                     Item old = Items.getItem(existingItem);
                     this.getResponder().getInventory().insertItem(old);
                     this.getResponder().getCommunicator().sendNormalServerMessage("Your old item was returned.");
                  } catch (NoSuchItemException var10) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("The previous reward item could not be located.");
                  }

                  ans = newexistingItem;
               }
            } catch (NumberFormatException var12) {
               if (this.errorText.isEmpty()) {
                  this.errorText = "Failed to parse value for existingItem.";
               }
            }
         } else {
            try {
               int index = Integer.parseInt(sIndex);
               if (index > 0) {
                  ans = this.ritems.get(index - 1).getWurmId();
               } else if (existingItem > 0L) {
                  Items.destroyItem(existingItem);
                  this.getResponder().getCommunicator().sendNormalServerMessage("The old reward item was lost.");
                  ans = 0L;
               }
            } catch (NumberFormatException var11) {
               if (this.errorText.isEmpty()) {
                  this.errorText = "Failed to parse value for existingItem.";
               }
            }
         }
      }

      return ans;
   }

   private int indexItemTemplate(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.itemplates.get(index - 1).getTemplateId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexLayer(String key, String type) {
      return this.indexBoolean(key, type) ? 0 : -1;
   }

   private boolean indexBoolean(String key, String type) {
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            return index == 0;
         } catch (NumberFormatException var5) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return false;
   }

   private int indexSpecialEffect(String key, String type, boolean doChecks) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);

            try {
               ans = this.effectsAvail.get(index).getId();
            } catch (Exception var8) {
               if (this.errorText.isEmpty()) {
                  this.errorText = "Unknown " + type + "?";
               }
            }
         } catch (NumberFormatException var9) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }

         if (doChecks && ans != 0) {
            if (ans == 1 && !maySetToOpen(this.getResponder(), this.currentTargetId)) {
               ans = 0;
               if (this.errorText.isEmpty()) {
                  this.errorText = "You may not set that to open. Try a fence gate you can open.";
               }
            }

            if (ans != 0) {
               SpecialEffects sp = SpecialEffects.getEffect(ans);
               if (sp.getPowerRequired() > this.getResponder().getPower()) {
                  ans = 0;
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Invalid effect. Set to no effect.";
                  }
               }
            }
         }
      }

      return ans;
   }

   private int indexSkillNum(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.stemplates.get(index - 1).getNumber();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private float indexSkillVal(String key, String type) {
      float ans = 0.0F;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               if (index == 1) {
                  return 0.001F;
               }

               if (this.getResponder().getPower() > 0) {
                  switch(index) {
                     case 2:
                        return 0.002F;
                     case 3:
                        return 0.01F;
                     case 4:
                        return 0.05F;
                     case 5:
                        return 0.1F;
                     case 6:
                        return 1.0F;
                     case 7:
                        return 10.0F;
                     case 8:
                        return 20.0F;
                     default:
                        return 0.001F;
                  }
               }

               return 0.001F;
            }
         } catch (NumberFormatException var6) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private float indexStateChange(String key, int specialEffect) {
      float val = this.getFloatProp(key, -101.0F, 100.0F, true);
      if (val != 0.0F && specialEffect == 1) {
         val = 0.0F;
         if (this.errorText.isEmpty()) {
            this.errorText = "State change can only be 0 for effect OPEN DOOR because it triggers several times and blocks half through passing the door.";
         }
      }

      return val;
   }

   private int indexTileType(String key) {
      int index = this.getIntProp("newTileType", 0, 8, false);
      switch(index) {
         case 0:
            return 0;
         case 1:
            return Tiles.Tile.TILE_TREE.id;
         case 2:
            return Tiles.Tile.TILE_DIRT.id;
         case 3:
            return Tiles.Tile.TILE_GRASS.id;
         case 4:
            return Tiles.Tile.TILE_FIELD.id;
         case 5:
            return Tiles.Tile.TILE_SAND.id;
         case 6:
            return Tiles.Tile.TILE_MYCELIUM.id;
         case 7:
            return Tiles.Tile.TILE_CAVE_WALL.id;
         case 8:
            return Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id;
         default:
            return 0;
      }
   }

   private int indexTrigger(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.mtriggers.get(index - 1).getId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexUnlinkedTrigger(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.utriggers.get(index - 1).getId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexEffect(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.teffects.get(index - 1).getId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexUnlinkedEffect(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.ueffects.get(index - 1).getId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexAchievementId(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.myAchievements.get(index - 1).getNumber();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexActionId(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.actionEntries.get(index - 1).getNumber();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown " + type + "?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexMission(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.missionsAvail.get(index - 1).getId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown mission?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private int indexCreatureTemplate(String key, String type) {
      int ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Integer.parseInt(sIndex);
            if (index != 0) {
               try {
                  ans = this.creaturesAvail.get(index - 1).getTemplateId();
               } catch (Exception var7) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "Unknown creature?";
                  }
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private byte indexCreatureType(String key, String type) {
      byte ans = 0;
      String sIndex = this.getStringProp(key);
      if (sIndex != null && !sIndex.isEmpty()) {
         try {
            int index = Byte.parseByte(sIndex);

            try {
               ans = this.creaturesTypes.get(index);
            } catch (Exception var7) {
               if (this.errorText.isEmpty()) {
                  this.errorText = "Unknown " + type + "?";
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + type + ".";
            }
         }
      }

      return ans;
   }

   private void sendManageEffect() {
      String name = "";
      boolean inactive = false;
      String desc = "";
      boolean startSkill = false;
      boolean stopSkill = false;
      boolean destroysInventory = false;
      int itemTemplate = 0;
      byte itemMaterial = 0;
      boolean newbieItem = false;
      String sql = "0";
      String snumbers = "0";
      String sbytevalue = "0";
      String sexistingItem = "0";
      String sexistingContainer = "0";
      boolean destroysTarget = false;
      int rewardSkillNum = 0;
      String srewardSkillVal = "0";
      String sModifyTileX = "0";
      String sModifyTileY = "0";
      int newTileType = 0;
      String snewTileData = "0";
      String sSpawnTileX = "0";
      String sSpawnTileY = "0";
      int creatureSpawn = 0;
      String sCreatureAge = "0";
      String creatureName = "";
      byte creatureType = 0;
      String sTeleportX = "0";
      String sTeleportY = "0";
      int teleportLayer = 0;
      int specialEffect = 0;
      int achievement = 0;
      int missionAffected = 0;
      String sStateChange = "0.0";
      String soundName = "";
      int missionToActivate = 0;
      int missionToDeactivate = 0;
      int triggerToActivate = 0;
      int triggerToDeactivate = 0;
      int effectToActivate = 0;
      int effectToDeactivate = 0;
      String sWinSizeX = "0";
      String sWinSizeY = "0";
      String topText = "";
      String textDisplayed = "";
      if (this.getAnswer() != null) {
         name = this.getStringProp("name");
         inactive = this.getBooleanProp("inactive");
         desc = this.getStringProp("desc");
         startSkill = this.getBooleanProp("startSkill");
         stopSkill = this.getBooleanProp("stopSkill");
         destroysInventory = this.getBooleanProp("destroysInventory");
         itemTemplate = this.indexItemTemplate("itemReward", "Reward Item");
         itemMaterial = this.getByteProp("itemMaterial");
         newbieItem = this.getBooleanProp("newbie");
         sql = this.getStringProp("ql");
         snumbers = this.getStringProp("numbers");
         sbytevalue = this.getStringProp("bytevalue");
         sexistingItem = this.getStringProp("existingItem");
         sexistingContainer = this.getStringProp("rewardTargetContainerId");
         destroysTarget = this.getBooleanProp("destroysTarget");
         rewardSkillNum = this.indexSkillNum("rewardSkillNum", "Reward Skill Number");
         srewardSkillVal = this.getStringProp("rewardSkillVal");
         sModifyTileX = this.getStringProp("modifyTileX");
         sModifyTileY = this.getStringProp("modifyTileY");
         newTileType = this.getIntProp("newTileType");
         snewTileData = this.getStringProp("newTileData");
         sSpawnTileX = this.getStringProp("spawnTileX");
         sSpawnTileY = this.getStringProp("spawnTileY");
         creatureSpawn = this.indexCreatureTemplate("creatureSpawn", "Creature Spawn");
         sCreatureAge = this.getStringProp("creatureAge");
         creatureName = this.getStringProp("creatureName");
         creatureType = this.indexCreatureType("creatureType", "Creature Type");
         sTeleportX = this.getStringProp("teleportTileX");
         sTeleportY = this.getStringProp("teleportTileY");
         teleportLayer = this.indexLayer("teleportLayer", "Teleport layer");
         specialEffect = this.indexSpecialEffect("specialEffect", "Special Effect", false);
         achievement = this.getIntProp("achievement");
         missionAffected = this.indexMission("missionId", "mission id");
         sStateChange = this.getStringProp("missionStateChange");
         soundName = this.getStringProp("sound");
         missionToActivate = this.indexMission("missionToActivate", "mission to activate");
         missionToDeactivate = this.indexMission("missionToDeactivate", "mission to deactivate");
         triggerToActivate = this.indexTrigger("triggerToActivate", "trigger to activate");
         triggerToDeactivate = this.indexTrigger("triggerToDeactivate", "trigger to deactivate");
         effectToActivate = this.indexEffect("effectToActivate", "effect to activate");
         effectToDeactivate = this.indexEffect("effectToDeactivate", "effect to deactivate");
         sWinSizeX = this.getStringProp("winsizeX");
         sWinSizeY = this.getStringProp("winsizeY");
         topText = this.getStringProp("toptext");
         textDisplayed = this.getStringProp("textdisplayed");
      } else if (this.effectId > 0) {
         TriggerEffect eff = TriggerEffects.getTriggerEffect(this.effectId);
         if (eff != null) {
            if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0 && eff != null && eff.getName() != null) {
               String buildLogString = this.getResponder() + ": Viewing trigger EFFECT settings for effect with name: " + eff.getName();
               buildLogString = buildLogString + (eff.getDescription() != null ? " and description = " + eff.getDescription() : "");
               this.getResponder().getLogger().info(buildLogString);
            }

            this.creatorType = eff.getCreatorType();
            name = eff.getName();
            inactive = eff.isInactive();
            desc = eff.getDescription();
            startSkill = eff.isStartSkillgain();
            stopSkill = eff.isStopSkillgain();
            destroysInventory = eff.destroysInventory();
            itemTemplate = eff.getRewardItem();
            itemMaterial = eff.getItemMaterial();
            newbieItem = eff.isNewbieItem();
            sql = Integer.toString(eff.getRewardQl());
            snumbers = Integer.toString(eff.getRewardNumbers());
            sbytevalue = Byte.toString(eff.getRewardByteValue());
            sexistingItem = Long.toString(eff.getExistingItemReward());
            sexistingContainer = Long.toString(eff.getRewardTargetContainerId());
            destroysTarget = eff.destroysTarget();
            rewardSkillNum = eff.getRewardSkillNum();
            srewardSkillVal = Float.toString(eff.getRewardSkillModifier());
            sModifyTileX = Integer.toString(eff.getModifyTileX());
            sModifyTileY = Integer.toString(eff.getModifyTileY());
            newTileType = eff.getNewTileType();
            snewTileData = Byte.toString(eff.getNewTileData());
            sSpawnTileX = Integer.toString(eff.getSpawnTileX());
            sSpawnTileY = Integer.toString(eff.getSpawnTileY());
            creatureSpawn = eff.getCreatureSpawn();
            sCreatureAge = Integer.toString(eff.getCreatureAge());
            creatureName = eff.getCreatureName();
            creatureType = eff.getCreatureType();
            sTeleportX = Integer.toString(eff.getTeleportX());
            sTeleportY = Integer.toString(eff.getTeleportY());
            teleportLayer = eff.getTeleportLayer();
            specialEffect = eff.getSpecialEffectId();
            achievement = eff.getAchievementId();
            missionAffected = eff.getMissionId();
            sStateChange = Float.toString(eff.getMissionStateChange());
            soundName = eff.getSoundName();
            missionToActivate = eff.getMissionToActivate();
            missionToDeactivate = eff.getMissionToDeActivate();
            triggerToActivate = eff.getTriggerToActivate();
            triggerToDeactivate = eff.getTriggerToDeActivate();
            effectToActivate = eff.getEffectToActivate();
            effectToDeactivate = eff.getEffectToDeActivate();
            sWinSizeX = Integer.toString(eff.getWindowSizeX());
            sWinSizeY = Integer.toString(eff.getWindowSizeY());
            topText = eff.getTopText();
            textDisplayed = eff.getTextDisplayed();
         } else {
            this.creatorType = 0;
         }
      } else {
         this.creatorType = 0;
      }

      StringBuilder buf = new StringBuilder();
      buf.append(
         "border{border{size=\"20,20\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\""
            + this.question
            + "    \"};label{type=\"bolditalic\";"
            + "color=\"255,127,127\""
            + ";text=\""
            + this.errorText
            + "\"}}}harray{button{id=\"back\";text=\"Back\"};label{text=\"  \"}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      buf.append(
         "harray{label{text=\"Name (max 40 chars)\"};input{id=\"name\";maxchars=\"40\";text=\""
            + name
            + "\"};label{text=\" \"};checkbox{id=\"inactive\";selected=\""
            + inactive
            + "\";hover=\"Not sure this is implemented currently\"};label{text=\"Inactive \";hover=\"Not sure this is implemented currently\"};}"
      );
      buf.append("label{text=\"Description (max 400 chars)\"};");
      buf.append("input{id=\"desc\";maxchars=\"400\";text=\"" + desc + "\";maxlines=\"4\"};");
      if (this.getResponder().getPower() > 0) {
         buf.append("text{text=\"\"}");
         buf.append("header{text=\"Dangerous\"};");
         buf.append(
            "harray{checkbox{id=\"startSkill\";text=\"Resumes skill gain.  \";selected=\""
               + startSkill
               + "\"};checkbox{id=\"stopSkill\";text=\"Stops skill gain.  \";selected=\""
               + stopSkill
               + "\"};checkbox{id=\"destroysInventory\";text=\"Destroys all (!) carried items.\";selected=\""
               + destroysInventory
               + "\"};}"
         );
         buf.append("text{text=\"\"}");
         buf.append("header{text=\"Reward\"};");
         buf.append("label{type=\"bolditalic\";text=\"Item\"}");
         buf.append(
            "harray{label{text=\"New reward item\"};"
               + this.dropdownItemTemplates("itemReward", itemTemplate, true)
               + "label{text=\" Material \"}"
               + this.dropdownItemMaterials("itemMaterial", itemMaterial)
               + "label{text=\" \"}checkbox{id=\"newbieItem\";text=\"Tutorial/newbie item.\";selected=\""
               + newbieItem
               + "\"};}"
         );
         buf.append(
            "harray{label{text=\"Quality \"};input{id=\"ql\";text=\""
               + sql
               + "\";maxchars=\"2\"};label{text=\" Numbers \"};input{id=\"numbers\";text=\""
               + snumbers
               + "\";maxchars=\"2\"};label{text=\" Byte value \"};input{id=\"bytevalue\";text=\""
               + sbytevalue
               + "\";maxchars=\"3\"};}"
         );
         buf.append(
            "harray{label{text=\"Existing item reward id \"};input{id=\"existingItem\";text=\""
               + sexistingItem
               + "\"};label{text=\"Existing container id to insert into \"};input{id=\"rewardTargetContainerId\";text=\""
               + sexistingContainer
               + "\"};}"
         );
         String hoverDel = ";hover=\"If you select Destroy target, the target that triggers this effect will be destroyed:\"";
         buf.append(
            "harray{checkbox{id=\"destroysTarget\";selected=\""
               + destroysTarget
               + "\"};label{text=\"Destroy target \""
               + ";hover=\"If you select Destroy target, the target that triggers this effect will be destroyed:\""
               + "};}"
         );
         buf.append("label{type=\"bolditalic\";text=\"Skill\"}");
         buf.append(
            "harray{label{text=\"New skill Reward\"};"
               + this.dropdownSkillTemplates("rewardSkillNum", rewardSkillNum)
               + "label{text=\"Skill Reward value\"};"
               + this.dropdownSkillValues("rewardSkillVal", srewardSkillVal)
               + "}"
         );
         buf.append("text{text=\"\"}");
         buf.append("header{text=\"Tiles\"};");
         buf.append("label{type=\"bolditalic\";text=\"Modify\"}");
         buf.append(
            "harray{label{text=\"Modify Tile X \"};input{id=\"modifyTileX\";text=\""
               + sModifyTileX
               + "\";maxchars=\"4\"};label{text=\" Y \"};input{id=\"modifyTileY\";text=\""
               + sModifyTileY
               + "\";maxchars=\"4\"};label{text=\" Type \"};"
               + this.dropdownTileTypes("newTileType", newTileType)
               + "label{text=\" Data \"};input{id=\"newTileData\";text=\""
               + snewTileData
               + "\";maxchars=\"3\"};}"
         );
         buf.append("label{type=\"bolditalic\";text=\"Spawn\"}");
         buf.append(
            "harray{label{text=\"Spawn Tile X \"};input{id=\"spawnTileX\";text=\""
               + sSpawnTileX
               + "\";maxchars=\"4\"};label{text=\" Y \"};input{id=\"spawnTileY\";text=\""
               + sSpawnTileY
               + "\";maxchars=\"4\"};label{text=\" Template \"};"
               + this.dropdownCreatureTemplates("creatureSpawn", creatureSpawn)
               + "}"
         );
         buf.append(
            "harray{label{text=\" Age \"};input{id=\"creatureAge\";text=\""
               + sCreatureAge
               + "\";maxchars=\"2\"};label{text=\" Name (instead of template name) (max 20 chars) \"};input{id=\"creatureName\";text=\""
               + creatureName
               + "\";maxchars=\"40\"};label{text=\" Type \"};"
               + this.dropdownCreatureTypes("creatureType", creatureType)
               + "}"
         );
         buf.append("label{type=\"bolditalic\";text=\"Teleport / Special\"}");
         buf.append(
            "harray{label{text=\"Teleport/Special effect Tile X \"};input{id=\"teleportTileX\";text=\""
               + sTeleportX
               + "\";maxchars=\"4\"};label{text=\" Y \"};input{id=\"teleportTileY\";text=\""
               + sTeleportY
               + "\";maxchars=\"4\"};label{text=\"Surface\"};"
               + this.dropdownBoolean("teleportLayer", teleportLayer >= 0)
               + "}"
         );
      } else {
         buf.append("text{text=\"\"}");
         buf.append("header{text=\"Reward\"};");
         buf.append("label{type=\"bolditalic\";text=\"Item\"}");

         try {
            long itemReward = Long.parseLong(sexistingItem);
            if (itemReward > 0L) {
               Item i = Items.getItem(itemReward);
               buf.append("label{text=\"Existing item reward: " + i.getName() + "\"};");
               buf.append("passthrough{id=\"existingItem\";text=\"" + sexistingItem + "\"}");
            } else {
               buf.append("harray{label{text=\"Your Item Reward (ql)\"};" + this.dropdownInventory("existingItem", itemReward) + "}");
            }
         } catch (NoSuchItemException var55) {
            buf.append("harray{label{text=\"Existing item reward: \"};label{color=\"255,127,127\"text=\"Not Found!\"}}");
         } catch (NumberFormatException var56) {
         }

         buf.append("text{text=\"\"}");
         buf.append("header{text=\"Tiles\"};");
      }

      buf.append("harray{label{text=\"Special effect \"};" + this.dropdownSpecialEffects("specialEffect", specialEffect) + "}");
      buf.append("label{type=\"bolditalic\";text=\"Achievement\"}");
      buf.append("harray{label{text=\"Trigger Achievement \"};" + this.dropdownAchievements("achievement", achievement) + "}");
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"General\"};");
      buf.append("label{type=\"bolditalic\";text=\"Mission\"}");
      String hoverState = ";hover=\"An effect may increase the state of any mission.\"";
      buf.append(
         "harray{label{text=\"Mission state affected \";hover=\"An effect may increase the state of any mission.\"};"
            + this.dropdownMissions("missionId", missionAffected, true)
            + "label{text=\"State change \""
            + ";hover=\"An effect may increase the state of any mission.\""
            + "};input{id=\"missionStateChange\";maxchars=\"5\";text=\""
            + sStateChange
            + "\"};label{text=\"%. Use '.' for decimals.\""
            + ";hover=\"An effect may increase the state of any mission.\""
            + "}}"
      );
      buf.append("label{type=\"bolditalic\";text=\"Sound\"}");
      buf.append(
         "harray{label{text=\"Sound mapping (max 50 chars)\"};input{id=\"sound\";text=\""
            + soundName
            + "\";maxchars=\"50\"};label{text=\" \"};button{id=\"playSound\";text=\"Play Sound\"}label{text=\" \"};button{id=\"listSounds\";text=\"Show Sound List\"}}"
      );
      buf.append("label{type=\"bolditalic\";text=\"Activate / Deactivate\"}");
      buf.append(
         "harray{label{text=\"Mission to activate \"};"
            + this.dropdownMissions("missionToActivate", missionToActivate, false)
            + "label{text=\"Mission to deactivate \"};"
            + this.dropdownMissions("missionToDeactivate", missionToDeactivate, false)
            + "}"
      );
      buf.append(
         "harray{label{text=\"Trigger to activate \"};"
            + this.dropdownTriggers("triggerToActivate", triggerToActivate, true)
            + "label{text=\"Trigger to deactivate \"};"
            + this.dropdownTriggers("triggerToDeactivate", triggerToDeactivate, false)
            + "}"
      );
      buf.append(
         "harray{label{text=\"Effect to activate \"};"
            + this.dropdownEffects("effectToActivate", effectToActivate, true)
            + "label{text=\"Effect to deactivate \"};"
            + this.dropdownEffects("effectToDeactivate", effectToDeactivate, false)
            + "}"
      );
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Popup\"};");
      buf.append(
         "harray{label{text=\"Window Size Width \"};input{id=\"winsizeX\";text=\""
            + sWinSizeX
            + "\";maxchars=\"3\"};label{text=\" Height \"};input{id=\"winsizeY\";text=\""
            + sWinSizeY
            + "\";maxchars=\"3\"};label{text=\"  \"};button{id=\"testText\";text=\"Verify Popup\";hover=\"Effect will be redisplayed after verification.\"}}"
      );
      buf.append("label{text=\"Top Text displayed (max 1000 chars)\"}");
      buf.append("input{id=\"toptext\";maxchars=\"1000\";maxlines=\"3\";text=\"" + topText + "\"}");
      buf.append("label{text=\"Text displayed (Can be normal text or BML) (max 1000 chars)\"}");
      buf.append("input{id=\"textdisplayed\";maxchars=\"1000\";maxlines=\"6\";text=\"" + textDisplayed + "\"}");
      buf.append("text{text=\"\"}");
      if (this.level == 8) {
         buf.append(this.appendChargeInfo());
         buf.append("harray{button{id=\"createEffect\";text=\"Create Effect\";hover=\"After creation, this will redisplay this effect\"}}");
      } else {
         buf.append(
            "harray{button{id=\"updateEffect\";text=\"Update Effect\"}label{text=\"  \"}button{id=\"deleteEffect\";text=\"Delete Effect\"hover=\"This will delete effect "
               + name
               + "\";confirm=\"You are about to delete effect "
               + name
               + ".\";question=\"Do you really want to do that?\"}label{text=\"  \"}button{id=\"cloneEffect\";text=\"Clone Effect\"hover=\"This will show a copy of this effect for creation\";}}"
         );
      }

      if (this.level == 7) {
         buf.append(
            "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
         );
         buf.append(
            "harray{label{type=\"bold\";text=\"Triggers:     \"}button{id=\"createTrigger\";text=\"Create New Trigger\"};label{text=\"  Triggers\"}"
               + this.dropdownUnlinkedTriggers("trigs", 0)
               + "label{text=\" \"}button{id=\"linkTrigger\";text=\"Link Trigger\"};}"
         );
         MissionTrigger[] trigs = Triggers2Effects.getTriggersForEffect(this.effectId, this.incTInactive);
         if (trigs.length > 0) {
            Arrays.sort((Object[])trigs);
            buf.append("table{rows=\"1\";cols=\"7\";");
            buf.append(
               "label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"State\"};label{text=\"Action\"};label{text=\"Target\"};label{text=\"\"};"
            );

            for(MissionTrigger trigger : trigs) {
               String colour = "color=\"127,255,127\"";
               String hover = ";hover=\"Active\"";
               if (trigger.isInactive()) {
                  colour = "color=\"255,127,127\"";
                  hover = ";hover=\"Inactive\"";
               } else if (trigger.hasTargetOf(this.currentTargetId, this.getResponder())) {
                  colour = "color=\"140,140,255\";";
                  hover = ";hover=\"Current Target\"";
               }

               buf.append(
                  "harray{button{id=\"edtT"
                     + trigger.getId()
                     + "\";text=\"Edit\"};label{text=\"  \"}};label{"
                     + colour
                     + "text=\""
                     + trigger.getId()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getName()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getStateRange()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getActionString()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getTargetAsString(this.getResponder())
                     + "\""
                     + hover
                     + "};harray{label{text=\"  \"}button{id=\"unlT"
                     + trigger.getId()
                     + "\";text=\"Unlink\"hover=\"This will unlink the trigger "
                     + trigger.getName()
                     + " from this effect\";};};"
               );
            }

            buf.append("}");
         } else {
            buf.append("label{text=\"No triggers found\"}");
         }
      }

      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(600, 600, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String dropdownTriggers(String id, int current, boolean reload) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      int counter = 0;
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");
      if (reload) {
         MissionTrigger[] triggers = MissionTriggers.getFilteredTriggers(
            this.getResponder(), current, this.creatorType, this.listForUser, this.dontListMine, this.listMineOnly
         );
         Arrays.sort((Object[])triggers);
         this.mtriggers.clear();

         for(MissionTrigger trig : triggers) {
            this.mtriggers.add(trig);
            ++counter;
            buf.append(",");
            buf.append(trig.getName());
            if (current > 0 && current == trig.getId()) {
               def = counter;
            }
         }
      } else {
         for(MissionTrigger trig : this.mtriggers) {
            ++counter;
            buf.append(",");
            buf.append(trig.getName());
            if (current > 0 && current == trig.getId()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownUnlinkedTriggers(String id, int show) {
      StringBuilder buf = new StringBuilder();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");
      MissionTrigger[] utrigs = MissionTriggers.getFilteredTriggers(this.getResponder(), show, this.incTInactive, 0, 0);
      Arrays.sort((Object[])utrigs);
      this.utriggers.clear();

      for(MissionTrigger trig : utrigs) {
         this.utriggers.add(trig);
         buf.append(",");
         buf.append(trig.getName());
      }

      buf.append("\";default=\"0\"}");
      return buf.toString();
   }

   private String dropdownEffects(String id, int current, boolean reload) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      int counter = 0;
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");
      if (reload) {
         TriggerEffect[] effects = TriggerEffects.getFilteredEffects(
            this.getResponder(), 0, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser
         );
         Arrays.sort((Object[])effects);
         this.teffects.clear();

         for(TriggerEffect eff : effects) {
            this.teffects.add(eff);
            ++counter;
            buf.append(",");
            buf.append(eff.getName());
            if (current > 0 && current == eff.getId()) {
               def = counter;
            }
         }
      } else {
         for(TriggerEffect eff : this.teffects) {
            ++counter;
            buf.append(",");
            buf.append(eff.getName());
            if (current > 0 && current == eff.getId()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownUnlinkedEffects(String id, int show) {
      StringBuilder buf = new StringBuilder();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");
      TriggerEffect[] effects = TriggerEffects.getFilteredEffects(
         this.getResponder(), show, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser
      );
      Arrays.sort((Object[])effects);
      this.ueffects.clear();

      for(TriggerEffect eff : effects) {
         this.ueffects.add(eff);
         buf.append(",");
         buf.append(eff.getName());
      }

      buf.append("\";default=\"0\"}");
      return buf.toString();
   }

   private String dropdownMissions(String id, int current, boolean reload) {
      StringBuilder buf = new StringBuilder();
      int counter = 0;
      int def = 0;
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");
      if (reload) {
         Mission[] missions = Missions.getFilteredMissions(
            this.getResponder(),
            this.includeM,
            this.incMInactive,
            this.dontListMine,
            this.listMineOnly,
            this.listForUser,
            this.groupName,
            this.onlyCurrent,
            this.currentTargetId
         );
         this.missionsAvail.clear();

         for(Mission lMission : missions) {
            this.missionsAvail.add(lMission);
            ++counter;
            buf.append(",");
            buf.append(lMission.getName());
            if (current > 0 && current == lMission.getId()) {
               def = counter;
            }
         }
      } else {
         for(Mission lMission : this.missionsAvail) {
            ++counter;
            buf.append(",");
            buf.append(lMission.getName());
            if (current > 0 && current == lMission.getId()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownSkillTemplates(String id, int current) {
      StringBuilder buf = new StringBuilder();
      SkillTemplate[] stemps = SkillSystem.getAllSkillTemplates();
      Arrays.sort((Object[])stemps);
      int def = 0;
      int counter = 0;
      this.stemplates.clear();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");

      for(SkillTemplate lStemp : stemps) {
         if (lStemp.isMission()) {
            ++counter;
            buf.append(",");
            this.stemplates.add(lStemp);
            buf.append(lStemp.getName());
            if (current > 0 && current == lStemp.getNumber()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownSpecialEffects(String id, int current) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      int counter = 0;
      buf.append("dropdown{id=\"specialEffect\";options=\"");
      this.effectsAvail.clear();
      SpecialEffects[] effects = SpecialEffects.getEffects();

      for(SpecialEffects lEffect : effects) {
         if (this.getResponder().getPower() >= lEffect.getPowerRequired()) {
            this.effectsAvail.add(lEffect);
            if (counter > 0) {
               buf.append(",");
            }

            buf.append(lEffect.getName());
            if (current > 0 && current == lEffect.getId()) {
               def = counter;
            } else if (current == 0 && lEffect.getId() == 0) {
               def = counter;
            }

            ++counter;
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownSkillValues(String id, String current) {
      StringBuilder buf = new StringBuilder();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("0,");
      buf.append(0.001F);
      buf.append("%");
      buf.append(", ");
      buf.append(0.002F);
      buf.append("%,");
      buf.append(0.01F);
      buf.append("%,");
      buf.append(0.05F);
      buf.append("%,");
      buf.append(0.1F);
      buf.append("%,");
      buf.append(1.0F);
      buf.append(", ");
      buf.append(10.0F);
      buf.append(", ");
      buf.append(20.0F);
      int def = 0;

      try {
         float cv = Float.parseFloat(current);
         if (cv == 0.001F) {
            def = 1;
         } else if (cv == 0.002F) {
            def = 2;
         } else if (cv == 0.01F) {
            def = 3;
         } else if (cv == 0.05F) {
            def = 4;
         } else if (cv == 0.1F) {
            def = 5;
         } else if (cv == 1.0F) {
            def = 6;
         } else if (cv == 10.0F) {
            def = 7;
         } else if (cv == 20.0F) {
            def = 8;
         }
      } catch (NumberFormatException var6) {
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownItemMaterials(String id, byte current) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      int counter = 0;
      buf.append("dropdown{id=\"" + id + "\";options=\"");

      for(int x = 0; x < 96; ++x) {
         if (x == 0) {
            buf.append("standard");
         } else {
            buf.append(",");
            buf.append(Item.getMaterialString((byte)x));
         }

         if (current > 0 && current == x) {
            def = counter;
         }

         ++counter;
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownCreatureTemplates(String id, int current) {
      StringBuilder buf = new StringBuilder();
      CreatureTemplate[] crets = CreatureTemplateFactory.getInstance().getTemplates();
      int def = 0;
      int counter = 0;
      buf.append("dropdown{id='creatureSpawn';options=\"");

      for(int x = 0; x < crets.length; ++x) {
         if (x == 0) {
            buf.append("none");
         } else if (crets[x].baseCombatRating < 15.0F) {
            buf.append(",");
            this.creaturesAvail.add(crets[x]);
            buf.append(crets[x].getName());
            ++counter;
         }

         if (current > 0 && current == crets[x].getTemplateId()) {
            def = counter;
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownBoolean(String id, boolean current) {
      StringBuilder buf = new StringBuilder();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("True,");
      buf.append("False");
      buf.append("\";default=\"" + (current ? 0 : 1) + "\"}");
      return buf.toString();
   }

   private String dropdownCreatureTypes(String id, byte current) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      this.creaturesTypes.clear();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None,");
      this.creaturesTypes.add((byte)0);
      buf.append("fierce ");
      this.creaturesTypes.add((byte)1);
      buf.append(", ");
      buf.append("angry ");
      this.creaturesTypes.add((byte)2);
      buf.append(", ");
      buf.append("raging ");
      this.creaturesTypes.add((byte)3);
      buf.append(", ");
      buf.append("slow ");
      this.creaturesTypes.add((byte)4);
      buf.append(", ");
      buf.append("alert ");
      this.creaturesTypes.add((byte)5);
      buf.append(", ");
      buf.append("greenish ");
      this.creaturesTypes.add((byte)6);
      buf.append(", ");
      buf.append("lurking ");
      this.creaturesTypes.add((byte)7);
      buf.append(", ");
      buf.append("sly ");
      this.creaturesTypes.add((byte)8);
      buf.append(", ");
      buf.append("hardened ");
      this.creaturesTypes.add((byte)9);
      buf.append(", ");
      buf.append("scared ");
      this.creaturesTypes.add((byte)10);
      buf.append(", ");
      buf.append("diseased ");
      this.creaturesTypes.add((byte)11);
      buf.append(", ");
      buf.append("champion ");
      this.creaturesTypes.add((byte)99);
      def = current & 255;
      if (current == 99) {
         def = 12;
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownItemTemplates(String id, int current, boolean incSpecial) {
      StringBuilder buf = new StringBuilder();
      ItemTemplate[] templates = ItemTemplateFactory.getInstance().getTemplates();
      Arrays.sort((Object[])templates);
      int def = 0;
      int counter = 0;
      this.itemplates.clear();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");

      for(ItemTemplate lTemplate : templates) {
         if (lTemplate.isMissionItem() || incSpecial && lTemplate.getTemplateId() == 791) {
            buf.append(",");
            this.itemplates.add(lTemplate);
            if (lTemplate.isMetal()) {
               buf.append(lTemplate.sizeString + Item.getMaterialString(lTemplate.getMaterial()) + " " + lTemplate.getName());
            } else if (lTemplate.bowUnstringed) {
               buf.append(lTemplate.sizeString + lTemplate.getName() + " [unstringed]");
            } else {
               buf.append(lTemplate.sizeString + lTemplate.getName());
            }

            ++counter;
            if (current > 0 && current == lTemplate.getTemplateId()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownInventory(String id, long itemReward) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      int counter = 0;
      Item[] itemarr = this.getResponder().getInventory().getAllItems(false);
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");

      for(Item lElement : itemarr) {
         if (!lElement.isNoDrop() && !lElement.isArtifact()) {
            buf.append(",");
            this.ritems.add(lElement);
            buf.append(lElement.getName() + " - " + (int)lElement.getQualityLevel());
            ++counter;
            if (itemReward > 0L && itemReward == lElement.getWurmId()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownAchievements(String id, int current) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      int counter = 0;
      this.myAchievements = Achievement.getSteelAchievements(this.getResponder());
      if (this.getResponder().getPower() > 0) {
         this.myAchievements.add(Achievement.getTemplate(141));
      }

      buf.append("dropdown{id='achievement';options=\"");
      buf.append("None");

      for(AchievementTemplate template : this.myAchievements) {
         ++counter;
         buf.append(",");
         buf.append(template.getName() + " (" + template.getCreator() + ")");
         if (current > 0 && template.getNumber() == current) {
            def = counter;
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownActions(String id, short current) {
      StringBuilder buf = new StringBuilder();
      ActionEntry[] acts = (ActionEntry[])Actions.actionEntrys.clone();
      Arrays.sort((Object[])acts);
      int def = 0;
      int counter = 0;
      this.actionEntries.clear();
      buf.append("dropdown{id=\"" + id + "\";options=\"");
      buf.append("None");

      for(ActionEntry lAct : acts) {
         if (lAct.isMission()) {
            ++counter;
            buf.append(",");
            buf.append(lAct.getActionString());
            this.actionEntries.add(lAct);
            if (current > 0 && current == lAct.getNumber()) {
               def = counter;
            }
         }
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private String dropdownTileTypes(String id, int current) {
      StringBuilder buf = new StringBuilder();
      int def = 0;
      buf.append("dropdown{id=\"newTileType\";options=\"");
      buf.append("None,");
      buf.append("Tree,");
      buf.append("Dirt,");
      buf.append("Grass,");
      buf.append("Field,");
      buf.append("Sand,");
      buf.append("Mycelium,");
      buf.append("Cave wall,");
      buf.append("Iron ore");
      if (current == Tiles.Tile.TILE_TREE.id) {
         def = 1;
      } else if (current == Tiles.Tile.TILE_DIRT.id) {
         def = 2;
      } else if (current == Tiles.Tile.TILE_GRASS.id) {
         def = 3;
      } else if (current == Tiles.Tile.TILE_FIELD.id) {
         def = 4;
      } else if (current == Tiles.Tile.TILE_SAND.id) {
         def = 5;
      } else if (current == Tiles.Tile.TILE_MYCELIUM.id) {
         def = 6;
      } else if (current == Tiles.Tile.TILE_CAVE_WALL.id) {
         def = 7;
      } else if (current == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
         def = 8;
      }

      buf.append("\";default=\"" + def + "\"}");
      return buf.toString();
   }

   private void sendManageMission() {
      String name = "";
      String group = this.groupName;
      boolean inactive = false;
      boolean hidden = false;
      String intro = "";
      boolean faildeath = false;
      boolean secondChance = false;
      boolean mayBeRestarted = false;
      String sdays = "0";
      String shours = "0";
      String sminutes = "0";
      String sseconds = "0";
      int height = 400;
      if (this.getAnswer() != null) {
         name = this.getStringProp("name");
         group = this.getStringProp("groupName");
         intro = this.getStringProp("intro");
         inactive = this.getBooleanProp("inactive");
         hidden = this.getBooleanProp("hidden");
         faildeath = this.getBooleanProp("faildeath");
         mayBeRestarted = this.getBooleanProp("mayBeRestarted");
         secondChance = this.getBooleanProp("secondChance");
         sdays = this.getStringProp("days");
         shours = this.getStringProp("hours");
         sminutes = this.getStringProp("minutes");
         sseconds = this.getStringProp("seconds");
      } else if (this.missionId > 0) {
         Mission m = Missions.getMissionWithId(this.missionId);
         if (m != null) {
            if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0 && m.getName() != null) {
               this.getResponder().getLogger().info(this.getResponder() + ": Viewing mission settings for mission with name: " + m.getName());
            }

            name = m.getName();
            group = m.getGroupName();
            inactive = m.isInactive();
            hidden = m.isHidden();
            intro = m.getInstruction();
            faildeath = m.isFailOnDeath();
            secondChance = m.hasSecondChance();
            mayBeRestarted = m.mayBeRestarted();
            if (m.getMaxTimeSeconds() > 0) {
               int left = m.getMaxTimeSeconds();
               int days = (int)((long)left / 86400L);
               left = (int)((long)left - 86400L * (long)days);
               int hours = (int)((long)left / 3600L);
               left = (int)((long)left - 3600L * (long)hours);
               int minutes = (int)((long)left / 60L);
               left = (int)((long)left - 60L * (long)minutes);
               sdays = "" + days;
               shours = "" + hours;
               sminutes = "" + minutes;
               sseconds = "" + left;
            }
         }
      }

      StringBuilder buf = new StringBuilder();
      buf.append(
         "border{border{size=\"20,20\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\""
            + this.question
            + "    \"};label{type=\"bolditalic\";"
            + "color=\"255,127,127\""
            + ";text=\""
            + this.errorText
            + "\"}}}harray{button{id=\"back\";text=\"Back\"};label{text=\"  \"}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      buf.append(
         "harray{label{text=\"Name (max 100 chars)\"};input{id=\"name\";maxchars=\"100\";text=\""
            + name
            + "\"};label{text=\"  Group (20 chars)\"};input{id=\"groupName\";maxchars=\"20\";text=\""
            + group
            + "\"};}"
      );
      buf.append(
         "harray{checkbox{id=\"inactive\";selected=\""
            + inactive
            + "\"};label{text=\"Inactive \"};checkbox{id=\"hidden\";selected=\""
            + hidden
            + "\"};label{text=\"Hidden from players \"};}"
      );
      buf.append("label{text=\"Introduction (max 400 chars) - Note: This text will appear as a popup when mission starts.\"};");
      buf.append("input{id=\"intro\";maxchars=\"400\"text=\"" + intro + "\";maxlines=\"4\"};");
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Fail\"};");
      buf.append("harray{checkbox{id=\"faildeath\";selected=\"" + faildeath + "\"};label{text=\"Fail on death \"};}");
      buf.append("text{text=\"A mission may fail after a certain time period. Set this limit here.\"}");
      buf.append(
         "harray{label{text=\"Max time: days\"};input{id=\"days\";text=\""
            + sdays
            + "\";maxchars=\"3\"};label{text=\" hours\"};input{id=\"hours\";text=\""
            + shours
            + "\";maxchars=\"2\"};label{text=\" minutes\"};input{id=\"minutes\";text=\""
            + sminutes
            + "\";maxchars=\"2\"};label{text=\" seconds\"};input{id=\"seconds\";text=\""
            + sseconds
            + "\";maxchars=\"2\"};}"
      );
      buf.append("text{text=\"\"}");
      buf.append("header{text=\"Restart\"};");
      buf.append(
         "harray{checkbox{id=\"secondChance\";selected=\""
            + secondChance
            + "\";text=\"New chance if fail \"};label{text=\"  \"};checkbox{id=\"mayBeRestarted\";selected=\""
            + mayBeRestarted
            + "\";text=\"May restart when finished \"};}"
      );
      buf.append("text{text=\"\"}");
      if (this.missionId == 0) {
         buf.append(this.appendChargeInfo());
         buf.append("harray{button{id=\"createMission\";text=\"Create New Mission\"};}");
      } else {
         height = 500;
         buf.append(
            "harray{button{id=\"updateMission\";text=\"Update Mission\"};label{text=\"  \"};button{id=\"deleteMission\";text=\"Delete Mission \"hover=\"This will delete "
               + name
               + "\";confirm=\"You are about to delete "
               + name
               + ".\";question=\"Do you really want to do that?\"};label{text=\"  \"};button{id=\"cloneMission\";text=\"Clone Mission\"hover=\"This will show a copy of this mission for creation\";};}"
         );
         buf.append(
            "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
         );
         buf.append(
            "harray{label{type=\"bold\";text=\"Triggers:     \"}button{id=\"createTrigger\";text=\"Create New Trigger\"};label{text=\"  Unlinked Triggers\"}"
               + this.dropdownUnlinkedTriggers("unTrigs", 2)
               + "label{text=\" \"}button{id=\"linkTrigger\";text=\"Link Trigger\"};}"
         );
         MissionTrigger[] trigs = MissionTriggers.getFilteredTriggers(this.getResponder(), 0, this.incTInactive, this.missionId, 0);
         if (trigs.length <= 0) {
            buf.append("label{text=\"No triggers found\"}");
         } else {
            Arrays.sort((Object[])trigs);
            buf.append("table{rows=\"1\";cols=\"7\";");
            buf.append(
               "label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"State\"};label{text=\"Action\"};label{text=\"Target\"};label{text=\"\"};"
            );

            for(MissionTrigger trigger : trigs) {
               String colour = "color=\"127,255,127\"";
               String hover = ";hover=\"Active\"";
               if (trigger.isInactive()) {
                  colour = "color=\"255,127,127\"";
                  hover = ";hover=\"Inactive\"";
               } else if (trigger.hasTargetOf(this.currentTargetId, this.getResponder())) {
                  colour = "color=\"140,140,255\";";
                  hover = ";hover=\"Current Target\"";
               }

               buf.append(
                  "button{id=\"edtT"
                     + trigger.getId()
                     + "\";text=\"Edit\"};label{"
                     + colour
                     + "text=\""
                     + trigger.getId()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getName()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getStateRange()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getActionString()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + trigger.getTargetAsString(this.getResponder())
                     + "\""
                     + hover
                     + "};harray{label{text=\"  \"}button{id=\"unlT"
                     + trigger.getId()
                     + "\";text=\"Unlink\"hover=\"This will unlink the trigger "
                     + trigger.getName()
                     + " from this mission\";};};"
               );
            }

            buf.append("}");
         }

         buf.append(
            "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
         );
         buf.append("harray{label{type=\"bold\";text=\"Trigger effects:     \"}button{id=\"createEffect\";text=\"Create New Effect\"};}");
         TriggerEffect[] teffs = TriggerEffects.getFilteredEffects(
            trigs, this.getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, false
         );
         if (teffs.length <= 0) {
            buf.append("label{text=\"No trigger effects found\"}");
         } else {
            Arrays.sort((Object[])teffs);
            buf.append("table{rows=\"1\";cols=\"7\";");
            buf.append(
               "label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"+State\"};label{text=\"Type\"};label{text=\"\"};label{text=\"\"};"
            );

            for(TriggerEffect lEff : teffs) {
               String colour = "color=\"127,255,127\"";
               String hover = ";hover=\"Active\"";
               if (lEff.isInactive()) {
                  colour = "color=\"255,127,127\"";
                  hover = ";hover=\"Inactive\"";
               } else if (lEff.hasTargetOf(this.currentTargetId, this.getResponder())) {
                  colour = "color=\"140,140,255\";";
                  hover = ";hover=\"Current Target\"";
               }

               String clrtxt = "label{type=\"italic\";text=\"   no text\";hover=\"no text to clear\"}";
               if (!lEff.getTextDisplayed().isEmpty()) {
                  clrtxt = "harray{label{text=\" \"};button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
               }

               buf.append(
                  "button{id=\"edtE"
                     + lEff.getId()
                     + "\";text=\"Edit\"};label{"
                     + colour
                     + "text=\""
                     + lEff.getId()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getName()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getMissionStateChange()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getType()
                     + " \""
                     + hover
                     + "};"
                     + clrtxt
                     + "harray{label{text=\" \"}};"
               );
            }

            buf.append("}");
         }

         buf.append(
            "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
         );
         buf.append("label{type=\"bold\";text=\"Other Mission effects that change this missions state:\"}");
         buf.append(
            "harray{label{text=\"  Unlinked Effects\"}"
               + this.dropdownUnlinkedEffects("unEffs", 2)
               + "label{text=\" \"}button{id=\"linkEffect\";text=\"Link Effect\"};}"
         );
         TriggerEffect[] meffs = TriggerEffects.getFilteredEffects(
            trigs, this.getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, this.missionId
         );
         if (meffs.length > 0) {
            Arrays.sort((Object[])meffs);
            buf.append("table{rows=\"1\";cols=\"7\";");
            buf.append(
               "label{text=\"\"};label{text=\"Id\"};label{text=\"Name\"};label{text=\"+State\"};label{text=\"Type\"};label{text=\"\"};label{text=\"\"};"
            );

            for(TriggerEffect lEff : meffs) {
               String colour = "color=\"127,255,127\"";
               String hover = ";hover=\"Active\"";
               if (lEff.isInactive()) {
                  colour = "color=\"255,127,127\"";
                  hover = ";hover=\"Inactive\"";
               } else if (lEff.hasTargetOf(this.currentTargetId, this.getResponder())) {
                  colour = "color=\"140,140,255\";";
                  hover = ";hover=\"Current Target\"";
               }

               String clrtxt = "label{type=\"italic\";text=\"   no text\";hover=\"no text to clear\"}";
               if (!lEff.getTextDisplayed().isEmpty()) {
                  clrtxt = "harray{label{text=\" \"};button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
               }

               buf.append(
                  "button{id=\"edtE"
                     + lEff.getId()
                     + "\";text=\"Edit\"};label{"
                     + colour
                     + "text=\""
                     + lEff.getId()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getName()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getMissionStateChange()
                     + " \""
                     + hover
                     + "};label{"
                     + colour
                     + "text=\""
                     + lEff.getType()
                     + " \""
                     + hover
                     + "};"
                     + clrtxt
                     + "harray{label{text=\" \"}button{id=\"unlE"
                     + lEff.getId()
                     + "\";text=\"Unlink\"hover=\"This will unlink effect "
                     + lEff.getName()
                     + " from this mission\";};};"
               );
            }

            buf.append("}");
         } else {
            buf.append("label{text=\"No mission effects found\"}");
         }
      }

      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(500, height, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String appendChargeInfo() {
      StringBuilder buf = new StringBuilder();
      if (this.getResponder().getPower() == 0) {
         Item ruler = null;

         try {
            ruler = Items.getItem(this.missionRulerId);
            if (ruler.getOwnerId() != this.getResponder().getWurmId()) {
               buf.append("text{text=\"You are not the ruler of this mission ruler.\"}");
            } else if (ruler.getAuxData() <= 0) {
               buf.append("text{text=\"Your " + ruler.getName() + " contains no charges. You can not create new mission functionality.\"}");
            } else {
               buf.append(
                  "text{text=\"Your "
                     + ruler.getName()
                     + " contains "
                     + ruler.getAuxData()
                     + " charges. Creating a new mission functionality uses up 1 charge.\"}"
               );
            }
         } catch (NoSuchItemException var4) {
            buf.append("text{text=\"The mission ruler is gone!\"}");
         }

         buf.append("text{text=\"\"}");
      }

      return buf.toString();
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeaderNoQuestion());
      buf.append("header{text=\"Missions\"}");
      if (this.getResponder().getPower() > 0) {
         buf.append(
            "harray{label{text=\"Filter:\"};checkbox{id=\"listmine\";text=\"Only my missions  \""
               + this.addSel(this.listMineOnly)
               + "};checkbox{id=\"nolistmine\";text=\"Not my missions  \""
               + this.addSel(this.dontListMine)
               + "};checkbox{id=\"onlyCurrent\";text=\"Only current target missions  \""
               + this.addSel(this.onlyCurrent)
               + "}}"
         );
         buf.append(
            "harray{label{text=\"Show:\"};radio{group=\"includeM\";id=\"0\";text=\"All  \""
               + this.addSel(this.includeM == 0)
               + "};radio{group=\"includeM\";id=\""
               + 1
               + "\";text=\"With triggers  \""
               + this.addSel(this.includeM == 1)
               + "};radio{group=\"includeM\";id=\""
               + 2
               + "\";text=\"Without triggers  \""
               + this.addSel(this.includeM == 2)
               + "};label{text=\"     \"};checkbox{id=\"incMInactive\";text=\"Include inactive\""
               + this.addSel(this.incMInactive)
               + "};}"
         );
         buf.append(
            "harray{label{text=\"Type: \"};checkbox{id=\"typeSystem\";text=\"System  \""
               + this.addSel(this.typeSystem)
               + "};checkbox{id=\"typeGM\";text=\"GM  \""
               + this.addSel(this.typeGM)
               + "};checkbox{id=\"typePlayer\";text=\"Player \""
               + this.addSel(this.typePlayer)
               + "};};"
         );
         buf.append(
            "harray{label{text=\"Group: \"};input{id=\"groupName\";text=\""
               + this.groupName
               + "\";maxchars=\"20\"}label{text=\" Player: \"};input{id=\"specialName\";text=\""
               + this.userName
               + "\";maxchars=\"30\"}};"
         );
         buf.append("text{text=\"\"}");
      } else {
         buf.append("harray{label{text=\"Group: \"};input{id=\"groupName\";text=\"" + this.groupName + "\";maxchars=\"20\"}};");
         buf.append(this.appendChargeInfo());
      }

      buf.append("harray{button{id=\"listMissions\";text=\"List Missions\"}label{text=\"  \"};button{id=\"createMission\";text=\"Create New Mission \"};}");
      buf.append(
         "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
      );
      buf.append("header{text=\"Triggers\"}");
      buf.append(
         "harray{label{text=\"Show:\"};radio{group=\"showT\";id=\"0\";text=\"All  \""
            + this.addSel(this.showT == 0)
            + "};radio{group=\"showT\";id=\""
            + 1
            + "\";text=\"Linked to missions\""
            + this.addSel(this.showT == 1)
            + "}radio{group=\"showT\";id=\""
            + 2
            + "\";text=\"Unlinked\""
            + this.addSel(this.showT == 2)
            + "}label{text=\"     \"};checkbox{id=\"incTInactive\";text=\"Include inactive  \""
            + this.addSel(this.incTInactive)
            + "};}"
      );
      buf.append("harray{button{id=\"listTriggers\";text=\"List Triggers \"};label{text=\"  \"};button{id=\"createTrigger\";text=\"Create New Trigger  \"};}");
      buf.append(
         "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
      );
      buf.append("header{text=\"Effects\"}");
      buf.append(
         "harray{label{text=\"Show:\"};radio{group=\"showE\";id=\"0\";text=\"All  \""
            + this.addSel(this.showE == 0)
            + "};radio{group=\"showE\";id=\""
            + 1
            + "\";text=\"Linked to trigger\""
            + this.addSel(this.showE == 1)
            + "}radio{group=\"showE\";id=\""
            + 2
            + "\";text=\"Unlinked\""
            + this.addSel(this.showE == 2)
            + "}label{text=\"     \"};checkbox{id=\"incEInactive\";text=\"Include inactive  \""
            + this.addSel(this.incEInactive)
            + "};}"
      );
      buf.append("harray{button{id=\"listEffects\";text=\"List Effects\"}label{text=\"  \"};button{id=\"createEffect\";text=\"Create New Effect \"}}");
      buf.append(
         "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}"
      );
      buf.append("header{text=\"Current Target\"}");
      String tgt = MissionTriggers.getTargetAsString(this.getResponder(), this.currentTargetId);
      buf.append(
         "harray{label{color=\"140,140,255\";text=\""
            + tgt
            + "\"}"
            + (this.getResponder().getPower() > 0 ? "label{color=\"140,140,255\";text=\"(Id:" + this.currentTargetId + ")\"}" : "")
            + "}"
      );
      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(450, 450, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public static final boolean maySetToOpen(Creature resp, long targetId) {
      if (resp.getPower() == 0) {
         if (WurmId.getType(targetId) != 5) {
            if (WurmId.getType(targetId) == 7) {
               Fence f = Fence.getFence(targetId);
               if (f.isDoor()) {
                  FenceGate fg = FenceGate.getFenceGate(targetId);
                  if (fg == null) {
                     return false;
                  }

                  if (fg.canBeOpenedBy(resp, false)) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            Wall w = Wall.getWall(targetId);
            if (w != null) {
               try {
                  Structure s = Structures.getStructure(w.getStructureId());

                  for(Item key : resp.getKeys()) {
                     if (key.getWurmId() == s.getWritId()) {
                        return true;
                     }
                  }
               } catch (NoSuchStructureException var9) {
               }
            }

            return false;
         }
      } else {
         return true;
      }
   }

   private boolean parseEffect(Item ruler) {
      boolean delete = this.getBooleanProp("deleteEffect");
      TriggerEffect eff = TriggerEffects.getTriggerEffect(this.effectId);
      if (eff == null) {
         if (delete) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You tried to delete a non-existing trigger effect.");
            return this.reshow();
         }

         eff = new TriggerEffect();
         eff.setCreatorName(this.getResponder().getName());
         eff.setLastModifierName(this.getResponder().getName());
      } else if (delete) {
         eff.destroy();
         this.getResponder().getCommunicator().sendNormalServerMessage("You delete the trigger effect.");
         return this.parseBack();
      }

      this.errorText = "";
      String name = eff.getName();
      String description = eff.getDescription();
      boolean inActive = eff.isInactive();
      boolean startSkill = eff.isStartSkillgain();
      boolean stopSkill = eff.isStopSkillgain();
      boolean destroysInventory = eff.destroysInventory();
      int itemReward = eff.getRewardItem();
      byte itemMaterial = eff.getItemMaterial();
      boolean newbie = eff.isNewbieItem();
      int ql = eff.getRewardQl();
      int numbers = eff.getRewardNumbers();
      byte bytevalue = eff.getRewardByteValue();
      long existingItem = eff.getExistingItemReward();
      long rewardTargetContainerId = eff.getRewardTargetContainerId();
      boolean destroysTarget = eff.destroysTarget();
      int rewardSkillNum = eff.getRewardSkillNum();
      float rewardSkillVal = eff.getRewardSkillModifier();
      int modifyTileX = eff.getModifyTileX();
      int modifyTileY = eff.getModifyTileY();
      int newTileType = eff.getNewTileType();
      byte newTileData = eff.getNewTileData();
      int spawnTileX = eff.getSpawnTileX();
      int spawnTileY = eff.getSpawnTileY();
      int creatureSpawn = eff.getCreatureSpawn();
      int creatureAge = eff.getCreatureAge();
      String creatureName = eff.getCreatureName();
      byte creatureType = eff.getCreatorType();
      int teleportX = eff.getTeleportX();
      int teleportY = eff.getTeleportY();
      int teleportLayer = eff.getTeleportLayer();
      int specialEffect = eff.getSpecialEffectId();
      int achievementId = eff.getAchievementId();
      int missionAffectedId = eff.getMissionId();
      float missionStateChange = eff.getMissionStateChange();
      int missionActivated = eff.getMissionToActivate();
      int missionDeActivated = eff.getMissionToDeActivate();
      int triggerActivated = eff.getTriggerToActivate();
      int triggerDeActivated = eff.getTriggerToDeActivate();
      int effectActivated = eff.getEffectToActivate();
      int effectDeActivated = eff.getEffectToDeActivate();
      String sound = eff.getSoundName();
      int setWindowSizeX = eff.getWindowSizeX();
      int setWindowSizeY = eff.getWindowSizeY();
      String textdisplayed = eff.getTextDisplayed();
      String top = eff.getTopText();
      boolean cloneEffect = this.getBooleanProp("cloneEffect");
      if (cloneEffect) {
         this.sbacks = this.sbacks + "|" + 7 + "," + this.missionId + "," + this.triggerId + "," + this.effectId;
         return this.createNewEffect(this.getAnswer());
      } else {
         sound = this.getStringProp("sound");
         boolean playSound = this.getBooleanProp("playSound");
         if (playSound) {
            if (!sound.isEmpty()) {
               SoundPlayer.playSound(sound, this.getResponder(), 1.5F);
            } else {
               this.getResponder().getCommunicator().sendNormalServerMessage("No sound mapped", (byte)1);
            }

            return this.reshow();
         } else {
            boolean listSounds = this.getBooleanProp("listSounds");
            if (listSounds) {
               SoundList sl = new SoundList(this.getResponder(), "SoundList", "Select the Sound to use");
               sl.setSelected(sound);
               sl.setRoot(this);
               sl.sendQuestion();
               return false;
            } else {
               setWindowSizeX = this.getIntProp("winsizeX");
               setWindowSizeY = this.getIntProp("winsizeY");
               top = this.getStringProp("toptext");
               textdisplayed = this.getStringProp("textdisplayed");
               boolean testText = this.getBooleanProp("testText");
               if (testText) {
                  if (!top.isEmpty() && !textdisplayed.isEmpty()) {
                     MissionPopup pop = new MissionPopup(this.getResponder(), "Mission progress", "");
                     if (setWindowSizeX > 0 && setWindowSizeY > 0) {
                        pop.windowSizeX = setWindowSizeX;
                        pop.windowSizeY = setWindowSizeY;
                     }

                     pop.setToSend(textdisplayed);
                     pop.setTop(top);
                     pop.setRoot(this);
                     pop.sendQuestion();
                     return false;
                  } else {
                     return this.reshow();
                  }
               } else {
                  boolean updateEffect = this.getBooleanProp("updateEffect");
                  boolean createEffect = this.getBooleanProp("createEffect");
                  if (!updateEffect && !createEffect) {
                     boolean createTrigger = this.getBooleanProp("createTrigger");
                     if (createTrigger) {
                        this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId;
                        return this.createNewTrigger(null);
                     } else {
                        boolean linkTrigger = this.getBooleanProp("linkTrigger");
                        if (linkTrigger) {
                           int trig = this.indexUnlinkedTrigger("trigs", "Unlinked trigger");
                           if (trig > 0) {
                              MissionTrigger mtrig = MissionTriggers.getTriggerWithId(trig);
                              if (mtrig != null) {
                                 Triggers2Effects.addLink(mtrig.getId(), this.effectId, false);
                                 this.getResponder()
                                    .getCommunicator()
                                    .sendNormalServerMessage("You link the trigger '" + mtrig.getName() + "' to the effect '" + name + "'.");
                              }
                           }

                           return this.reshow();
                        } else {
                           for(String key : this.getAnswer().stringPropertyNames()) {
                              boolean edtT = key.startsWith("edtT");
                              boolean delT = key.startsWith("delT");
                              boolean unlT = key.startsWith("unlT");
                              if (edtT || delT || unlT) {
                                 String sid = key.substring(4);
                                 int tid = Integer.parseInt(sid);
                                 MissionTrigger trg = MissionTriggers.getTriggerWithId(tid);
                                 if (trg == null) {
                                    this.errorText = "Cannot find trigger!";
                                    this.getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
                                    return this.reshow();
                                 }

                                 if (edtT) {
                                    this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0";
                                    return this.editTrigger(tid, null);
                                 }

                                 if (delT) {
                                    return this.reshow();
                                 }

                                 if (unlT) {
                                    Triggers2Effects.deleteLink(tid, this.effectId);
                                    this.getResponder()
                                       .getCommunicator()
                                       .sendNormalServerMessage("You unlink the trigger '" + trg.getName() + "' from the effect '" + name + "'.");
                                    return this.editEffect(this.effectId, null);
                                 }
                              }
                           }

                           return false;
                        }
                     }
                  } else {
                     name = this.getStringProp("name", 3);
                     description = this.getStringProp("desc");
                     inActive = this.getBooleanProp("inactive");
                     if (this.getResponder().getPower() > 0) {
                        startSkill = this.getBooleanProp("startSkill");
                        stopSkill = this.getBooleanProp("stopSkill");
                        destroysInventory = this.getBooleanProp("destroysInventory");
                        itemReward = this.indexItemTemplate("itemReward", "Item Reward");
                        itemMaterial = this.getByteProp("itemMaterial");
                        newbie = this.getBooleanProp("newbieItem");
                        ql = this.getIntProp("ql", 0, 100, true);
                        numbers = this.getIntProp("numbers");
                        bytevalue = this.getByteProp("bytevalue");
                        rewardTargetContainerId = this.readContainerId();
                        destroysTarget = this.getBooleanProp("destroysTarget");
                        rewardSkillNum = this.indexSkillNum("rewardSkillNum", "Reward Skill Number");
                        rewardSkillVal = this.indexSkillVal("rewardSkillVal", "Reward Skill Value");
                        modifyTileX = this.getIntProp("modifyTileX");
                        modifyTileY = this.getIntProp("modifyTileY");
                        newTileData = this.getByteProp("newTileData");
                        newTileType = this.indexTileType("newTileType");
                        spawnTileX = this.getIntProp("spawnTileX");
                        spawnTileY = this.getIntProp("spawnTileY");
                        creatureSpawn = this.indexCreatureTemplate("creatureSpawn", "Creature Spawn");
                        creatureAge = this.getIntProp("creatureAge");
                        creatureName = this.getStringProp("creatureName");
                        creatureType = this.indexCreatureType("creatureType", "Creature Type");
                        teleportX = this.getIntProp("teleportTileX");
                        teleportY = this.getIntProp("teleportTileY");
                        teleportLayer = this.indexLayer("teleportLayer", "Teleport Layer");
                     }

                     specialEffect = this.indexSpecialEffect("specialEffect", "Special Effect", true);
                     achievementId = this.indexAchievementId("achievement", "achievement");
                     missionAffectedId = this.indexMission("missionId", "mission Id");
                     missionStateChange = this.indexStateChange("missionStateChange", specialEffect);
                     missionActivated = this.indexMission("missionToActivate", "mission To Activate");
                     missionDeActivated = this.indexMission("missionToDeactivate", "mission To Deactivate");
                     triggerActivated = this.indexTrigger("triggerToActivate", "trigger To Activate");
                     triggerDeActivated = this.indexTrigger("triggerToDeactivate", "trigger To Deactivate");
                     effectActivated = this.indexTrigger("effectToActivate", "effect To Activate");
                     effectDeActivated = this.indexTrigger("effectToDeactivate", "effect To Deactivate");
                     if (this.getResponder().getPower() > 0 || this.level == 8) {
                        existingItem = this.indexExistingItem("existingItem", existingItem);
                     }

                     if (this.errorText.length() > 0) {
                        return this.editEffect(this.effectId, this.getAnswer());
                     } else {
                        boolean changed = false;
                        if (!name.equals(eff.getName())) {
                           eff.setName(name);
                           changed = true;
                        }

                        if (!description.equals(eff.getDescription())) {
                           eff.setDescription(description);
                           changed = true;
                        }

                        if (inActive != eff.isInactive()) {
                           eff.setInactive(inActive);
                           changed = true;
                        }

                        if (itemReward != eff.getRewardItem()) {
                           eff.setRewardItem(itemReward);
                           changed = true;
                        }

                        if (modifyTileX != eff.getModifyTileX()) {
                           eff.setModifyTileX(modifyTileX);
                           changed = true;
                        }

                        if (modifyTileY != eff.getModifyTileY()) {
                           eff.setModifyTileY(modifyTileY);
                           changed = true;
                        }

                        if (newTileType != eff.getNewTileType()) {
                           eff.setNewTileType(newTileType);
                           changed = true;
                        }

                        if (newTileData != eff.getNewTileData()) {
                           eff.setNewTileData(newTileData);
                           changed = true;
                        }

                        if (spawnTileX != eff.getSpawnTileX()) {
                           eff.setSpawnTileX(spawnTileX);
                           changed = true;
                        }

                        if (spawnTileY != eff.getSpawnTileY()) {
                           eff.setSpawnTileY(spawnTileY);
                           changed = true;
                        }

                        if (creatureSpawn != eff.getCreatureSpawn()) {
                           eff.setCreatureSpawn(creatureSpawn);
                           changed = true;
                        }

                        if (creatureAge != eff.getCreatureAge()) {
                           eff.setCreatureAge(creatureAge);
                           changed = true;
                        }

                        if (!creatureName.equals(eff.getCreatureName())) {
                           eff.setCreatureName(creatureName);
                           changed = true;
                        }

                        if (creatureType != eff.getCreatureType()) {
                           eff.setCreatureType(creatureType);
                           changed = true;
                        }

                        if (teleportX != eff.getTeleportX()) {
                           eff.setTeleportX(teleportX);
                           changed = true;
                        }

                        if (teleportY != eff.getTeleportY()) {
                           eff.setTeleportY(teleportY);
                           changed = true;
                        }

                        if (teleportLayer != eff.getTeleportLayer()) {
                           eff.setTeleportLayer(teleportLayer);
                           changed = true;
                        }

                        if (missionActivated != eff.getMissionToActivate()) {
                           eff.setMissionToActivate(missionActivated);
                           changed = true;
                        }

                        if (missionDeActivated != eff.getMissionToDeActivate()) {
                           eff.setMissionToDeActivate(missionDeActivated);
                           changed = true;
                        }

                        if (triggerActivated != eff.getTriggerToActivate()) {
                           eff.setTriggerToActivate(triggerActivated);
                           changed = true;
                        }

                        if (triggerDeActivated != eff.getTriggerToDeActivate()) {
                           eff.setTriggerToDeActivate(triggerDeActivated);
                           changed = true;
                        }

                        if (effectActivated != eff.getEffectToActivate()) {
                           eff.setEffectToActivate(effectActivated);
                           changed = true;
                        }

                        if (effectDeActivated != eff.getEffectToDeActivate()) {
                           eff.setEffectToDeActivate(effectDeActivated);
                           changed = true;
                        }

                        if (itemMaterial != eff.getItemMaterial()) {
                           eff.setItemMaterial(itemMaterial);
                           changed = true;
                        }

                        if (newbie != eff.isNewbieItem()) {
                           eff.setNewbieItem(newbie);
                           changed = true;
                        }

                        if (startSkill != eff.isStartSkillgain()) {
                           eff.setStartSkillgain(startSkill);
                           changed = true;
                        }

                        if (stopSkill != eff.isStopSkillgain()) {
                           eff.setStopSkillgain(stopSkill);
                           changed = true;
                        }

                        if (destroysInventory != eff.destroysInventory()) {
                           eff.setDestroyInventory(destroysInventory);
                           changed = true;
                        }

                        if (ql != eff.getRewardQl()) {
                           eff.setRewardQl(ql);
                           changed = true;
                        }

                        if (numbers != eff.getRewardNumbers()) {
                           eff.setRewardNumbers(numbers);
                           changed = true;
                        }

                        if (bytevalue != eff.getRewardByteValue()) {
                           eff.setRewardByteValue(bytevalue);
                           changed = true;
                        }

                        if (existingItem != eff.getExistingItemReward()) {
                           if (existingItem > 0L) {
                              try {
                                 Item i = Items.getItem(existingItem);
                                 if (i.getOwnerId() != this.getResponder().getWurmId() || i.isTraded() || i.isTransferred() || i.mailed) {
                                    this.getResponder()
                                       .getCommunicator()
                                       .sendAlertServerMessage("The " + i.getName() + " may not be selected as reward right now.");
                                    existingItem = 0L;
                                 }

                                 if (existingItem > 0L) {
                                    i.putInVoid();
                                 }
                              } catch (NoSuchItemException var67) {
                                 existingItem = 0L;
                              }
                           }

                           eff.setExistingItemReward(existingItem);
                           changed = true;
                        }

                        if (rewardTargetContainerId != eff.getRewardTargetContainerId()) {
                           eff.setRewardTargetContainerId(rewardTargetContainerId);
                           changed = true;
                        }

                        if (specialEffect != eff.getSpecialEffectId()) {
                           eff.setSpecialEffect(specialEffect);
                           changed = true;
                           if (eff.getTeleportX() > 0 || eff.getTeleportY() > 0) {
                              this.getResponder()
                                 .getCommunicator()
                                 .sendNormalServerMessage("The special effect will affect tile " + eff.getTeleportX() + "," + eff.getTeleportY() + ".");
                           }
                        }

                        if (achievementId != eff.getAchievementId()) {
                           eff.setAchievementId(achievementId);
                           changed = true;
                        }

                        if (rewardSkillNum != eff.getRewardSkillNum()) {
                           eff.setRewardSkillNum(rewardSkillNum);
                           changed = true;
                        }

                        if (rewardSkillVal != eff.getRewardSkillModifier()) {
                           eff.setRewardSkillVal(rewardSkillVal);
                           changed = true;
                        }

                        if (missionAffectedId != eff.getMissionId()) {
                           eff.setMission(missionAffectedId);
                           changed = true;
                        }

                        if (missionStateChange != eff.getMissionStateChange()) {
                           eff.setMissionStateChange(missionStateChange);
                           changed = true;
                        }

                        if (destroysTarget != eff.destroysTarget()) {
                           eff.setDestroysTarget(destroysTarget);
                           changed = true;
                        }

                        if (!sound.equals(eff.getSoundName())) {
                           eff.setSoundName(sound);
                           changed = true;
                        }

                        if (setWindowSizeY != eff.getWindowSizeY()) {
                           eff.setWindowSizeY(setWindowSizeY);
                           changed = true;
                        }

                        if (setWindowSizeX != eff.getWindowSizeX()) {
                           eff.setWindowSizeX(setWindowSizeX);
                           changed = true;
                        }

                        if (!top.equals(eff.getTopText())) {
                           eff.setTopText(top);
                           changed = true;
                        }

                        if (!textdisplayed.equals(eff.getTextDisplayed())) {
                           eff.setTextDisplayed(textdisplayed);
                           changed = true;
                        }

                        if (this.level == 7) {
                           if (changed) {
                              eff.setLastModifierName(this.getResponder().getName());
                              eff.update();
                              this.getResponder().getCommunicator().sendNormalServerMessage("You update the effect " + eff.getName() + ".");
                           }

                           return this.parseBack();
                        } else if (changed) {
                           if (this.getResponder().getPower() == 0) {
                              if (ruler.getAuxData() <= 0) {
                                 this.getResponder()
                                    .getCommunicator()
                                    .sendAlertServerMessage("Your " + ruler.getName() + " contains no charges. You can not create a new trigger effect.");
                                 return this.parseBack();
                              }

                              ruler.setAuxData((byte)(ruler.getAuxData() - 1));
                              this.getResponder()
                                 .getCommunicator()
                                 .sendAlertServerMessage(
                                    "You spend a charge from your " + ruler.getName() + ". It now has " + ruler.getAuxData() + " charges left."
                                 );
                              eff.setCreatorType((byte)3);
                           } else {
                              eff.setCreatorType((byte)1);
                           }

                           eff.setOwnerId(this.getResponder().getWurmId());
                           eff.create();
                           TriggerEffects.addTriggerEffect(eff);
                           this.effectId = eff.getId();
                           this.getResponder().getCommunicator().sendNormalServerMessage("You create the effect " + eff.getName() + ".");
                           return this.editEffect(this.effectId, null);
                        } else {
                           if (!changed) {
                              this.getResponder().getCommunicator().sendNormalServerMessage("You change nothing.");
                           }

                           return this.parseBack();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private long readContainerId() {
      String svalue = this.getStringProp("rewardTargetContainerId");
      long id = 0L;

      try {
         id = Long.parseLong(svalue);
      } catch (NumberFormatException var6) {
         if (this.errorText.isEmpty()) {
            this.errorText = "Failed to parse value for rewardTargetContainerId.";
         }
      }

      if (id > 0L) {
         try {
            Item i = Items.getItem(id);
            if (i.isTraded() || i.isTransferred() || i.mailed) {
               id = 0L;
               if (this.errorText.isEmpty()) {
                  this.errorText = "The " + i.getName() + " may not be used as container right now.";
               }
            }
         } catch (NoSuchItemException var5) {
            id = 0L;
            if (this.errorText.isEmpty()) {
               this.errorText = "Container not found!";
            }
         }
      }

      return id;
   }

   private boolean parseMission(Item ruler) {
      boolean delete = this.getBooleanProp("deleteMission");
      Mission m = Missions.getMissionWithId(this.missionId);
      if (m == null) {
         if (delete) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You tried to delete a non-existing mission.");
            return this.showMissionList();
         }

         m = new Mission(this.getResponder().getName(), this.getResponder().getName());
      } else if (delete) {
         m.destroy();
         this.getResponder().getCommunicator().sendNormalServerMessage("You delete the mission.");
         return this.showMissionList();
      }

      this.errorText = "";
      String name = m.getName();
      String group = m.getGroupName();
      String intro = m.getInstruction();
      boolean inactive = m.isInactive();
      boolean hidden = m.isHidden();
      boolean faildeath = m.isFailOnDeath();
      boolean mayBeRestarted = m.mayBeRestarted();
      boolean hasSecondChance = m.hasSecondChance();
      int maxTimeSeconds = m.getMaxTimeSeconds();
      boolean cloneMission = this.getBooleanProp("cloneMission");
      if (cloneMission) {
         this.sbacks = this.sbacks + "|" + 2 + "," + this.missionId + ",0,0";
         return this.createNewMission(this.getAnswer());
      } else {
         boolean updateMission = this.getBooleanProp("updateMission");
         boolean createMission = this.getBooleanProp("createMission");
         if (!updateMission && !createMission) {
            boolean createTrigger = this.getBooleanProp("createTrigger");
            if (createTrigger) {
               this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId;
               return this.createNewTrigger(null);
            } else {
               boolean linkTrigger = this.getBooleanProp("linkTrigger");
               if (linkTrigger) {
                  int utrig = this.indexUnlinkedTrigger("unTrigs", "Unlinked trigger");
                  if (utrig > 0) {
                     MissionTrigger mtrig = MissionTriggers.getTriggerWithId(utrig);
                     if (mtrig != null) {
                        mtrig.setMissionRequirement(this.missionId);
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You link the trigger '" + mtrig.getName() + "' to the mission '" + name + "'.");
                     }
                  }

                  return this.reshow();
               } else {
                  boolean linkEffect = this.getBooleanProp("linkEffect");
                  if (linkEffect) {
                     this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId;
                     int uEff = this.indexUnlinkedEffect("unEffs", "Unlinked effects");
                     if (uEff > 0) {
                        TriggerEffect mEff = TriggerEffects.getTriggerEffect(uEff);
                        if (mEff != null) {
                           mEff.setMission(this.missionId);
                           this.getResponder()
                              .getCommunicator()
                              .sendNormalServerMessage("You link the effect '" + mEff.getName() + "' to the mision '" + name + "'.");
                        }
                     }

                     return this.reshow();
                  } else {
                     boolean createEffect = this.getBooleanProp("createEffect");
                     if (createEffect) {
                        this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + "," + this.effectId;
                        return this.createNewEffect(null);
                     } else {
                        for(String key : this.getAnswer().stringPropertyNames()) {
                           boolean edtT = key.startsWith("edtT");
                           boolean delT = key.startsWith("delT");
                           boolean unlT = key.startsWith("unlT");
                           if (edtT || delT || unlT) {
                              String sid = key.substring(4);
                              int tid = Integer.parseInt(sid);
                              MissionTrigger trg = MissionTriggers.getTriggerWithId(tid);
                              if (trg == null) {
                                 this.errorText = "Cannot find trigger!";
                                 this.getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
                                 return this.reshow();
                              }

                              if (edtT) {
                                 this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0";
                                 return this.editTrigger(tid, null);
                              }

                              if (delT) {
                                 return this.reshow();
                              }

                              if (unlT) {
                                 trg.setMissionRequirement(0);
                                 this.getResponder()
                                    .getCommunicator()
                                    .sendNormalServerMessage("You unlink the trigger '" + trg.getName() + "' from the mission.");
                                 return this.reshow();
                              }
                           }

                           boolean edtE = key.startsWith("edtE");
                           boolean delE = key.startsWith("delE");
                           boolean unlE = key.startsWith("unlE");
                           boolean clrE = key.startsWith("clrE");
                           if (edtE || delE || unlE || clrE) {
                              String sid = key.substring(4);
                              int eid = Integer.parseInt(sid);
                              TriggerEffect te = TriggerEffects.getTriggerEffect(eid);
                              if (te == null) {
                                 this.errorText = "Cannot find effect!";
                                 this.getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
                                 return this.reshow();
                              }

                              if (edtE) {
                                 this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0";
                                 return this.editEffect(eid, null);
                              }

                              if (delE) {
                                 return this.reshow();
                              }

                              if (unlE) {
                                 te.setMission(0);
                                 this.getResponder()
                                    .getCommunicator()
                                    .sendNormalServerMessage("You unlink the effect '" + te.getName() + "' from the mission.");
                                 return this.reshow();
                              }

                              if (clrE) {
                                 te.setTextDisplayed("");
                                 te.setTopText("");
                                 this.getResponder().getCommunicator().sendNormalServerMessage("You clear the text for trigger effect " + te.getName() + ".");
                                 logger.log(Level.INFO, this.getResponder().getName() + " cleared text of effect " + te.getName());
                                 return this.reshow();
                              }
                           }
                        }

                        return false;
                     }
                  }
               }
            }
         } else {
            name = this.getStringProp("name", 3);
            group = this.getStringProp("groupName");
            intro = this.getStringProp("intro");
            inactive = this.getBooleanProp("inactive");
            hidden = this.getBooleanProp("hidden");
            faildeath = this.getBooleanProp("faildeath");
            mayBeRestarted = this.getBooleanProp("mayBeRestarted");
            hasSecondChance = this.getBooleanProp("secondChance");
            int days = this.getIntProp("days");
            maxTimeSeconds = (int)((long)days * 86400L);
            int hours = this.getIntProp("hours", 0, 23, false);
            maxTimeSeconds = (int)((long)maxTimeSeconds + (long)hours * 3600L);
            int minutes = this.getIntProp("minutes", 0, 59, false);
            maxTimeSeconds = (int)((long)maxTimeSeconds + (long)minutes * 60L);
            int secs = this.getIntProp("seconds", 0, 59, false);
            maxTimeSeconds += secs;
            if (this.errorText.length() > 0) {
               return this.editMission(this.missionId, this.getAnswer());
            } else {
               boolean changed = false;
               if (!name.equals(m.getName())) {
                  m.setName(name);
                  changed = true;
               }

               if (!group.equals(m.getGroupName())) {
                  m.setGroupName(group);
                  changed = true;
               }

               if (!intro.equals(m.getInstruction())) {
                  m.setInstruction(intro);
                  changed = true;
               }

               if (inactive != m.isInactive()) {
                  m.setInactive(inactive);
                  changed = true;
               }

               if (hidden != m.isHidden()) {
                  m.setIsHidden(hidden);
                  changed = true;
               }

               if (faildeath != m.isFailOnDeath()) {
                  m.setFailOnDeath(faildeath);
                  changed = true;
               }

               if (mayBeRestarted != m.mayBeRestarted()) {
                  m.setMayBeRestarted(mayBeRestarted);
                  changed = true;
               }

               if (hasSecondChance != m.hasSecondChance()) {
                  m.setSecondChance(hasSecondChance);
                  changed = true;
               }

               if (maxTimeSeconds != m.getMaxTimeSeconds()) {
                  m.setMaxTimeSeconds(maxTimeSeconds);
                  changed = true;
               }

               if (this.level == 1) {
                  if (changed) {
                     if (this.getResponder().getPower() == 0) {
                        if (ruler.getAuxData() <= 0) {
                           this.getResponder()
                              .getCommunicator()
                              .sendAlertServerMessage("Your " + ruler.getName() + " contains no charges. You can not create a new mission.");
                           return this.showMissionList();
                        }

                        ruler.setAuxData((byte)(ruler.getAuxData() - 1));
                        this.getResponder()
                           .getCommunicator()
                           .sendAlertServerMessage("You spend a charge from your " + ruler.getName() + ". It now has " + ruler.getAuxData() + " charges left.");
                        m.setCreatorType((byte)3);
                     } else {
                        m.setCreatorType((byte)1);
                     }

                     m.setOwnerId(this.getResponder().getWurmId());
                     m.create();
                     Missions.addMission(m);
                     this.getResponder().getCommunicator().sendNormalServerMessage("You create the mission " + m.getName() + ".");
                     this.missionId = m.getId();
                     return this.editMission(this.missionId, null);
                  } else {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to create a new mission.");
                     return this.parseBack();
                  }
               } else {
                  if (changed) {
                     m.setLastModifierName(this.getResponder().getName());
                     m.update();
                     this.getResponder().getCommunicator().sendNormalServerMessage("You update the mission " + m.getName() + ".");
                  }

                  if (!changed) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You change nothing.");
                  }

                  return this.parseBack();
               }
            }
         }
      }
   }

   private boolean parseTrigger(Item ruler) {
      boolean delete = this.getBooleanProp("deleteTrigger");
      MissionTrigger trg = MissionTriggers.getTriggerWithId(this.triggerId);
      if (trg == null) {
         if (delete) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You tried to delete a non-existing mission trigger.");
            return this.showTriggerList(this.missionId);
         }

         trg = new MissionTrigger();
         trg.setCreatorName(this.getResponder().getName());
         trg.setLastModifierName(this.getResponder().getName());
      } else if (delete) {
         trg.destroy();
         this.getResponder().getCommunicator().sendNormalServerMessage("You delete the mission trigger.");
         return this.showTriggerList(this.missionId);
      }

      if (this.getResponder().getLogger() != null && this.getResponder().getPower() > 0 && trg.getName() != null) {
         this.getResponder()
            .getLogger()
            .info(this.getResponder() + ": Editing mission trigger with trigger name: " + trg.getName() + " and description " + trg.getDescription());
      }

      this.errorText = "";
      String name = trg.getName();
      boolean inActive = trg.isInactive();
      String description = trg.getDescription();
      int onItemCreatedId = trg.getItemUsedId();
      int onActionPerformed = trg.getOnActionPerformed();
      long onActionTargetId = trg.getTarget();
      boolean useCurrentTarget = onActionTargetId == this.currentTargetId;
      boolean spawnpoint = false;
      spawnpoint = trg.isSpawnPoint();
      int seconds = trg.getSeconds();
      int missionRequired = trg.getMissionRequired();
      float stateFrom = trg.getStateRequired();
      float stateTo = trg.getStateEnd();
      boolean cloneTrigger = this.getBooleanProp("cloneTrigger");
      if (cloneTrigger) {
         this.sbacks = this.sbacks + "|" + 3 + "," + this.missionId + "," + this.triggerId + ",0";
         return this.createNewTrigger(this.getAnswer());
      } else {
         boolean updateTrigger = this.getBooleanProp("updateTrigger");
         boolean createTrigger = this.getBooleanProp("createTrigger");
         if (!updateTrigger && !createTrigger) {
            boolean createEffect = this.getBooleanProp("createEffect");
            if (createEffect) {
               this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0";
               return this.createNewEffect(null);
            } else {
               boolean linkEffect = this.getBooleanProp("linkEffect");
               if (linkEffect) {
                  int effe = this.indexUnlinkedEffect("linkEffects", "all effects");
                  if (effe > 0) {
                     TriggerEffect meff = TriggerEffects.getTriggerEffect(effe);
                     if (meff != null) {
                        Triggers2Effects.addLink(this.triggerId, meff.getId(), false);
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You link the effect '" + meff.getName() + "' to the trigger '" + name + "'.");
                     }
                  }

                  return this.editTrigger(this.triggerId, null);
               } else {
                  for(String key : this.getAnswer().stringPropertyNames()) {
                     boolean edtE = key.startsWith("edtE");
                     boolean delE = key.startsWith("delE");
                     boolean unlE = key.startsWith("unlE");
                     boolean clrE = key.startsWith("clrE");
                     if (edtE || delE || unlE || clrE) {
                        String sid = key.substring(4);
                        int eid = Integer.parseInt(sid);
                        TriggerEffect te = TriggerEffects.getTriggerEffect(eid);
                        if (te == null) {
                           this.errorText = "Cannot find effect!";
                           this.getResponder().getCommunicator().sendNormalServerMessage(this.errorText);
                           return this.reshow();
                        }

                        if (edtE) {
                           this.sbacks = this.sbacks + "|" + this.level + "," + this.missionId + "," + this.triggerId + ",0";
                           return this.editEffect(eid, null);
                        }

                        if (delE) {
                           return this.reshow();
                        }

                        if (unlE) {
                           Triggers2Effects.deleteLink(this.triggerId, eid);
                           this.getResponder()
                              .getCommunicator()
                              .sendNormalServerMessage("You unlink the effect '" + te.getName() + "' from the trigger '" + name + "'.");
                           return this.editTrigger(this.triggerId, null);
                        }

                        if (clrE) {
                           te.setTextDisplayed("");
                           te.setTopText("");
                           this.getResponder().getCommunicator().sendNormalServerMessage("You clear the text for trigger effect " + te.getName() + ".");
                           logger.log(Level.INFO, this.getResponder().getName() + " cleared text of effect " + te.getName());
                           return this.reshow();
                        }
                     }
                  }

                  return false;
               }
            }
         } else {
            name = this.getStringProp("name", 3);
            description = this.getStringProp("desc");
            inActive = this.getBooleanProp("inactive");
            spawnpoint = this.getBooleanProp("spawnpoint");
            onItemCreatedId = this.indexItemTemplate("onItemCreatedId", "Item Created");
            onActionPerformed = this.indexActionId("actionId", "action");
            useCurrentTarget = this.getBooleanProp("useCurrentTarget");
            if (useCurrentTarget) {
               if (this.getResponder().getPower() != 0 || this.target > 0L) {
                  onActionTargetId = this.currentTargetId;
               } else if (this.errorText.isEmpty()) {
                  this.errorText = "The trigger needs a valid target.";
               }
            } else {
               onActionTargetId = this.getLongProp("targetid");
               if (this.getResponder().getPower() == 0 && onActionTargetId <= 0L && this.errorText.isEmpty()) {
                  this.errorText = "The trigger needs a valid target.";
               }
            }

            this.currentTargetId = onActionTargetId;
            missionRequired = this.indexMission("missionRequired", "available missions");
            if (onActionPerformed == 475 && WurmId.getType(onActionTargetId) == 3 && this.getResponder().getPower() <= 0) {
               int tilex = Tiles.decodeTileX(onActionTargetId);
               int tiley = Tiles.decodeTileY(onActionTargetId);
               Village v = Villages.getVillage(tilex, tiley, true);
               if (v == null || v != this.getResponder().getCitizenVillage()) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = "You are only allowed to set the step on trigger action in your own settlement.";
                  }

                  onActionPerformed = 0;
               }
            }

            stateFrom = this.getFloatProp("stateFrom", -1.0F, 100.0F, true);
            stateTo = this.getFloatProp("stateTo", -1.0F, 100.0F, true);
            if (stateTo < stateFrom) {
               stateTo = 0.0F;
            }

            seconds = this.getIntProp("seconds", 0, 59, false);
            if (this.errorText.length() > 0) {
               return this.editTrigger(this.triggerId, this.getAnswer());
            } else {
               boolean changed = false;
               if (!name.equals(trg.getName())) {
                  trg.setName(name);
                  changed = true;
               }

               if (!description.equals(trg.getDescription())) {
                  trg.setDescription(description);
                  changed = true;
               }

               if (inActive != trg.isInactive()) {
                  trg.setInactive(inActive);
                  changed = true;
               }

               if (spawnpoint != trg.isSpawnPoint()) {
                  trg.setIsSpawnpoint(spawnpoint);
                  changed = true;
               }

               if (onActionTargetId != trg.getTarget()) {
                  if (this.level == 3) {
                     MissionTargets.removeMissionTrigger(trg, false);
                  }

                  trg.setOnTargetId(onActionTargetId);
                  if (this.level == 3) {
                     MissionTargets.addMissionTrigger(trg);
                  }

                  changed = true;
               }

               if (missionRequired != trg.getMissionRequired()) {
                  trg.setMissionRequirement(missionRequired);
                  changed = true;
               }

               if (stateFrom != trg.getStateRequired()) {
                  trg.setStateRequirement(stateFrom);
                  changed = true;
               }

               if (stateTo != trg.getStateEnd()) {
                  trg.setStateEnd(stateTo);
                  changed = true;
               }

               if (seconds > 0 && Action.isQuick(onActionPerformed)) {
                  seconds = 0;
                  this.getResponder().getCommunicator().sendAlertServerMessage("Seconds were set to 0 for that action since it is quick.");
               }

               if (seconds != trg.getSeconds()) {
                  trg.setSeconds(seconds);
                  changed = true;
               }

               if (onItemCreatedId != trg.getItemUsedId()) {
                  trg.setOnItemUsedId(onItemCreatedId);
                  changed = true;
               }

               if (onActionPerformed != trg.getOnActionPerformed()) {
                  trg.setOnActionPerformed(onActionPerformed);
                  changed = true;
               }

               if (this.level == 3) {
                  if (changed) {
                     trg.setLastModifierName(this.getResponder().getName());
                     trg.update();
                     this.getResponder().getCommunicator().sendNormalServerMessage("You change the trigger.");
                  }

                  return this.parseBack();
               } else if (changed) {
                  if (this.getResponder().getPower() == 0) {
                     if (ruler.getAuxData() <= 0) {
                        this.getResponder()
                           .getCommunicator()
                           .sendAlertServerMessage("Your " + ruler.getName() + " contains no charges. You can not create a new mission trigger.");
                        return this.parseBack();
                     }

                     ruler.setAuxData((byte)(ruler.getAuxData() - 1));
                     this.getResponder()
                        .getCommunicator()
                        .sendAlertServerMessage("You spend a charge from your " + ruler.getName() + ". It now has " + ruler.getAuxData() + " charges left.");
                     trg.setCreatorType((byte)3);
                  } else {
                     trg.setCreatorType((byte)1);
                  }

                  trg.setOwnerId(this.getResponder().getWurmId());
                  trg.create();
                  this.triggerId = trg.getId();
                  MissionTriggers.addMissionTrigger(trg);
                  this.getResponder().getCommunicator().sendNormalServerMessage("You create the trigger " + trg.getName() + ".");
                  return this.editTrigger(this.triggerId, null);
               } else {
                  if (!changed) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You change nothing.");
                  }

                  return this.parseBack();
               }
            }
         }
      }
   }

   private void sendMissionList() {
      StringBuilder buf = new StringBuilder();
      buf.append("border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "\"};");
      if (this.getResponder().getPower() > 0) {
         buf.append(
            "label{text=\"Group: \";hover=\"TODO\"};input{id=\"groupName\";text=\""
               + this.groupName
               + "\";maxchars=\"20\"}label{text=\" Player: \"};input{id=\"specialName\";text=\""
               + this.userName
               + "\";maxchars=\"30\"}}"
         );
         buf.append(
            "harray{checkbox{id=\"incMInactive\";text=\"Include inactive      \""
               + this.addSel(this.incMInactive)
               + ";hover=\"TODO\"};label{text=\"Include:\"};radio{group=\"includeM\";id=\""
               + 0
               + "\";text=\"All  \""
               + this.addSel(this.includeM == 0)
               + ";hover=\"TODO\"};radio{group=\"includeM\";id=\""
               + 1
               + "\";text=\"With triggers  \""
               + this.addSel(this.includeM == 1)
               + ";hover=\"TODO\"};radio{group=\"includeM\";id=\""
               + 2
               + "\";text=\"Without triggers  \""
               + this.addSel(this.includeM == 2)
               + ";hover=\"TODO\"};}"
         );
      } else {
         buf.append("label{text=\"Group: \";hover=\"TODO\"};input{id=\"groupName\";text=\"" + this.groupName + "\";maxchars=\"20\"}};");
         buf.append(this.appendChargeInfo());
      }

      buf.append(
         "}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"6\";label{text=\"\"};"
            + this.colHeader("Id", 1, this.sortBy)
            + this.colHeader("Group", 2, this.sortBy)
            + this.colHeader("Name", 3, this.sortBy)
            + this.colHeader("Owner", 4, this.sortBy)
            + this.colHeader("Last Modified", 5, this.sortBy)
      );
      Mission[] missions = Missions.getFilteredMissions(
         this.getResponder(),
         this.includeM,
         this.incMInactive,
         this.dontListMine,
         this.listMineOnly,
         this.listForUser,
         this.groupName,
         this.onlyCurrent,
         this.currentTargetId
      );
      if (missions.length > 0) {
         switch(absSortBy) {
            case 1:
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     int value1 = param1.getId();
                     int value2 = param2.getId();
                     if (value1 == value2) {
                        return 0;
                     } else {
                        return value1 < value2 ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
               break;
            case 2:
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     return param1.getName().compareTo(param2.getName());
                  }
               });
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     return param1.getGroupName().compareTo(param2.getGroupName()) * upDown;
                  }
               });
               break;
            case 3:
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  }
               });
               break;
            case 4:
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  }
               });
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     return param1.getOwnerName().compareTo(param2.getOwnerName()) * upDown;
                  }
               });
               break;
            case 5:
               Arrays.sort(missions, new Comparator<Mission>() {
                  public int compare(Mission param1, Mission param2) {
                     return param1.getLastModifiedDate().compareTo(param2.getLastModifiedDate()) * upDown;
                  }
               });
         }

         for(Mission mission : missions) {
            String colour = "color=\"127,255,127\"";
            String hover = ";hover=\"Active\"";
            if (!mission.hasTriggers()) {
               colour = "color=\"255,177,40\"";
               hover = ";hover=\"No Triggers\"";
            } else if (mission.isInactive()) {
               colour = "color=\"255,127,127\"";
               hover = ";hover=\"Inactive\"";
            } else if (mission.hasTargetOf(this.currentTargetId, this.getResponder())) {
               colour = "color=\"140,140,255\";";
               hover = ";hover=\"Current Target\"";
            }

            buf.append(
               "radio{group=\"sel\";id=\""
                  + mission.getId()
                  + "\"};label{"
                  + colour
                  + "text=\""
                  + mission.getId()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + mission.getGroupName()
                  + "\""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + mission.getName()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + mission.getOwnerName()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + mission.getLastModifiedString()
                  + " \""
                  + hover
                  + "};"
            );
         }
      }

      buf.append("}");
      buf.append("radio{group=\"sel\";id=\"0\";selected=\"true\";text=\"None\"}");
      buf.append("}};null;");
      buf.append("varray{rescale=\"true\";");
      buf.append("text{text=\"Select mission and choose what to do\"}");
      buf.append(
         "harray{button{id=\"editMission\";text=\"Edit Mission\";hover=\"If 'None' is selected then this will create a new mission.\"};label{text=\"  \"};button{id=\"showStats\";text=\"Show Stats \"};label{text=\"  \"};button{id=\"listTriggers\";text=\"List Triggers\";hover=\"If 'None' is selected then this will list all triggers.\"}label{text=\"  \"};button{id=\"listEffects\";text=\"List Effects \";hover=\"If 'None' is selected then this will list all effects.\"}label{text=\"  Or \"};button{id=\"createMission\";text=\"Create New Mission \"}}"
      );
      buf.append("}");
      buf.append("}");
      this.getResponder().getCommunicator().sendBml(600, 500, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private void sendTriggerList() {
      StringBuilder buf = new StringBuilder();
      buf.append("border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "\"};}");
      buf.append(
         "harray{checkbox{id=\"incTInactive\";text=\"Include inactive      \""
            + this.addSel(this.incTInactive)
            + ";hover=\"TODO\"};label{text=\"Show:\"};radio{group=\"showT\";id=\""
            + 0
            + "\";text=\"All  \""
            + this.addSel(this.showT == 0)
            + "};radio{group=\"showT\";id=\""
            + 1
            + "\";text=\"Linked to missions\""
            + this.addSel(this.showT == 1)
            + "}radio{group=\"showT\";id=\""
            + 2
            + "\";text=\"Unlinked\""
            + this.addSel(this.showT == 2)
            + "}}"
      );
      buf.append(
         "}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      MissionTrigger[] trigs = MissionTriggers.getFilteredTriggers(this.getResponder(), this.showT, this.incTInactive, this.missionId, 0);
      if (trigs.length > 0) {
         switch(absSortBy) {
            case 1:
               Arrays.sort(trigs, new Comparator<MissionTrigger>() {
                  public int compare(MissionTrigger param1, MissionTrigger param2) {
                     int value1 = param1.getId();
                     int value2 = param2.getId();
                     if (value1 == value2) {
                        return 0;
                     } else {
                        return value1 < value2 ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
               break;
            case 2:
               Arrays.sort(trigs, new Comparator<MissionTrigger>() {
                  public int compare(MissionTrigger param1, MissionTrigger param2) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  }
               });
               break;
            case 3:
               Arrays.sort(trigs, new Comparator<MissionTrigger>() {
                  public int compare(MissionTrigger param1, MissionTrigger param2) {
                     return param1.getStateRange().compareTo(param2.getStateRange()) * upDown;
                  }
               });
               break;
            case 4:
               Arrays.sort(trigs, new Comparator<MissionTrigger>() {
                  public int compare(MissionTrigger param1, MissionTrigger param2) {
                     return param1.getActionString().compareTo(param2.getActionString()) * upDown;
                  }
               });
               break;
            case 5:
               Arrays.sort(
                  trigs,
                  new Comparator<MissionTrigger>() {
                     public int compare(MissionTrigger param1, MissionTrigger param2) {
                        return param1.getTargetAsString(MissionManager.this.getResponder())
                              .compareTo(param2.getTargetAsString(MissionManager.this.getResponder()))
                           * upDown;
                     }
                  }
               );
         }

         buf.append(
            "table{rows=\"1\";cols=\"7\";label{text=\"\"};"
               + this.colHeader("Id", 1, this.sortBy)
               + this.colHeader("Name", 2, this.sortBy)
               + this.colHeader("State", 3, this.sortBy)
               + this.colHeader("Action", 4, this.sortBy)
               + this.colHeader("Target", 5, this.sortBy)
               + "label{text=\"\"};"
         );

         for(MissionTrigger trigger : trigs) {
            String colour = "color=\"127,255,127\"";
            String hover = ";hover=\"Active\"";
            if (trigger.isInactive()) {
               colour = "color=\"255,127,127\"";
               hover = ";hover=\"Inactive\"";
            } else if (trigger.hasTargetOf(this.currentTargetId, this.getResponder())) {
               colour = "color=\"140,140,255\";";
               hover = ";hover=\"Current Target\"";
            }

            buf.append(
               "radio{group=\"sel\";id=\""
                  + trigger.getId()
                  + "\"};label{"
                  + colour
                  + "text=\""
                  + trigger.getId()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + trigger.getName()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + trigger.getStateRange()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + trigger.getActionString()
                  + "\""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + trigger.getTargetAsString(this.getResponder())
                  + "\""
                  + hover
                  + "};harray{label{text=\" \"}button{id=\"delT"
                  + trigger.getId()
                  + "\";text=\"Delete\"hover=\"This will delete "
                  + trigger.getName()
                  + "\";confirm=\"You are about to delete "
                  + trigger.getName()
                  + ".\";question=\"Do you really want to do that?\"};};"
            );
         }

         buf.append("}");
      }

      buf.append("radio{group=\"sel\";id=\"0\";selected=\"true\";text=\"None\"}");
      buf.append("}};null;");
      buf.append("varray{rescale=\"true\";");
      buf.append("text{text=\"Select trigger and choose what to do\"}");
      buf.append(
         "harray{button{id=\"editTrigger\";text=\"Edit Trigger\"};label{text=\"  \"};button{id=\"listEffects\";text=\"List Effects \";hover=\"If 'None' is selected then this will list all effects.\"}label{text=\"  Or \"};button{id=\"createTrigger\";text=\"Create New Trigger\"}}"
      );
      buf.append("}");
      buf.append("}");
      this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private void sendEffectList() {
      StringBuilder buf = new StringBuilder();
      buf.append("border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type='bold';text=\"" + this.question + "\"};}");
      buf.append(
         "harray{checkbox{id=\"incEInactive\";text=\"Include inactive  \""
            + this.addSel(this.incEInactive)
            + "};label{text=\"Show:\"};radio{group=\"showE\";id=\""
            + 0
            + "\";text=\"All  \""
            + this.addSel(this.showE == 0)
            + "};radio{group=\"showE\";id=\""
            + 1
            + "\";text=\"Linked to trigger\""
            + this.addSel(this.showE == 1)
            + "}radio{group=\"showE\";id=\""
            + 2
            + "\";text=\"Unlinked\""
            + this.addSel(this.showE == 2)
            + "}}"
      );
      buf.append(
         "}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      boolean showAll = this.missionId == 0 && this.triggerId == 0;
      MissionTrigger[] trigs = MissionTriggers.getFilteredTriggers(this.getResponder(), this.showT, this.incTInactive, this.missionId, this.triggerId);
      TriggerEffect[] effs = TriggerEffects.getFilteredEffects(
         trigs, this.getResponder(), this.showE, this.incEInactive, this.dontListMine, this.listMineOnly, this.listForUser, showAll
      );
      if (effs.length > 0) {
         switch(absSortBy) {
            case 1:
               Arrays.sort(effs, new Comparator<TriggerEffect>() {
                  public int compare(TriggerEffect param1, TriggerEffect param2) {
                     int value1 = param1.getId();
                     int value2 = param2.getId();
                     if (value1 == value2) {
                        return 0;
                     } else {
                        return value1 < value2 ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
               break;
            case 2:
               Arrays.sort(effs, new Comparator<TriggerEffect>() {
                  public int compare(TriggerEffect param1, TriggerEffect param2) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  }
               });
               break;
            case 3:
               Arrays.sort(effs, new Comparator<TriggerEffect>() {
                  public int compare(TriggerEffect param1, TriggerEffect param2) {
                     float value1 = param1.getMissionStateChange();
                     float value2 = param2.getMissionStateChange();
                     if (value1 == value2) {
                        return 0;
                     } else {
                        return value1 < value2 ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
               break;
            case 4:
               Arrays.sort(effs, new Comparator<TriggerEffect>() {
                  public int compare(TriggerEffect param1, TriggerEffect param2) {
                     return param1.getType().compareTo(param2.getType()) * upDown;
                  }
               });
         }

         buf.append("table{rows=\"1\";cols=\"7\";");
         buf.append(
            "label{text=\"\"};"
               + this.colHeader("Id", 1, this.sortBy)
               + this.colHeader("Name", 2, this.sortBy)
               + this.colHeader("+State", 3, this.sortBy)
               + this.colHeader("Type", 4, this.sortBy)
               + "label{text=\"\"};label{text=\"\"};"
         );

         for(TriggerEffect lEff : effs) {
            String colour = "color=\"127,255,127\"";
            String hover = ";hover=\"Active\"";
            if (lEff.isInactive()) {
               colour = "color=\"255,127,127\"";
               hover = ";hover=\"Inactive\"";
            } else if (lEff.hasTargetOf(this.currentTargetId, this.getResponder())) {
               colour = "color=\"140,140,255\";";
               hover = ";hover=\"Current Target\"";
            }

            String clrtxt = "label{text=\"  no text to clear\"}";
            if (!lEff.getTextDisplayed().isEmpty()) {
               clrtxt = "harray{label{text=\" \";}button{id=\"clrE" + lEff.getId() + "\";text=\"Clear Text\"};}";
            }

            buf.append(
               "radio{group=\"sel\";id=\""
                  + lEff.getId()
                  + "\"};label{"
                  + colour
                  + "text=\""
                  + lEff.getId()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + lEff.getName()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + lEff.getMissionStateChange()
                  + " \""
                  + hover
                  + "};label{"
                  + colour
                  + "text=\""
                  + lEff.getType()
                  + " \""
                  + hover
                  + "};harray{label{text=\" \";}button{id=\"delE"
                  + lEff.getId()
                  + "\";text=\"Delete\"hover=\"This will delete "
                  + lEff.getName()
                  + "\";confirm=\"You are about to delete "
                  + lEff.getName()
                  + ".\";question=\"Do you really want to do that?\"};};"
                  + clrtxt
            );
         }

         buf.append("}");
      }

      buf.append("radio{group=\"sel\";id=\"0\";selected=\"true\";text=\"None\"}");
      buf.append("}};null;");
      buf.append("varray{rescale=\"true\";");
      buf.append("text{text=\"Select effect and choose what to do\"}");
      buf.append(
         "harray{button{id=\"editEffect\";text=\"Edit Effect\"};label{text=\"  \"};button{id=\"showTriggers\";text=\"Show Triggers\";hover=\"Not implemented (yet)\"};label{text=\"  Or \"};button{id=\"createEffect\";text=\"Create New Effect\"}}"
      );
      buf.append("}");
      buf.append("}");
      this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String addSel(boolean value) {
      return ";selected=\"" + (value ? "true" : "false") + "\"";
   }

   private int getIntProp(String key) {
      int value = 0;
      String svalue = this.getStringProp(key);
      if (svalue != null && !svalue.isEmpty()) {
         try {
            value = Integer.parseInt(svalue);
         } catch (NumberFormatException var5) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + key + ".";
            }
         }
      }

      return value;
   }

   private int getIntProp(String key, int min, int max, boolean restrict) {
      int value = this.getIntProp(key);
      if (max > min && restrict) {
         if (restrict) {
            if (value > max) {
               value = max;
            }

            if (value < min) {
               value = min;
            }
         } else if (value < min || value > max) {
            if (this.errorText.isEmpty()) {
               this.errorText = key + " not in required range " + min + "-" + max + ".";
            }

            value = min;
         }
      }

      return value;
   }

   private byte getByteProp(String key) {
      byte value = 0;
      String svalue = this.getStringProp(key);
      if (svalue != null && !svalue.isEmpty()) {
         try {
            value = Byte.parseByte(svalue);
         } catch (NumberFormatException var5) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + key + ".";
            }
         }
      }

      return value;
   }

   private float getFloatProp(String key, float min, float max, boolean restrict) {
      float value = 0.0F;
      String svalue = this.getStringProp(key);
      if (svalue != null && !svalue.isEmpty()) {
         try {
            value = Float.parseFloat(svalue);
            if (max > min && restrict) {
               if (restrict) {
                  if (value > max) {
                     value = max;
                  }

                  if (value < min) {
                     value = min;
                  }
               } else if (value < min || value > max) {
                  if (this.errorText.isEmpty()) {
                     this.errorText = key + " not in required range " + min + "-" + max + ".";
                  }

                  value = min;
               }
            }
         } catch (NumberFormatException var8) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + key + ".";
            }
         }
      }

      return value;
   }

   private long getLongProp(String key) {
      long value = 0L;
      String svalue = this.getStringProp(key);
      if (svalue != null && !svalue.isEmpty()) {
         try {
            value = Long.parseLong(svalue);
         } catch (NumberFormatException var6) {
            if (this.errorText.isEmpty()) {
               this.errorText = "Failed to parse value for " + key + ".";
            }
         }
      }

      return value;
   }

   private String getStringProp(String key, int minLength) {
      String value = this.getStringProp(key);
      if ((value == null || value.length() < minLength) && this.errorText.isEmpty()) {
         this.errorText = "Please select a name with at least " + minLength + " characters.";
      }

      return value == null ? "" : value;
   }
}

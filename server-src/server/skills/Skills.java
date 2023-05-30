package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public abstract class Skills implements MiscConstants, CounterTypes, TimeConstants {
   private static final ConcurrentHashMap<Long, Set<Skill>> creatureSkillsMap = new ConcurrentHashMap<>();
   Map<Integer, Skill> skills;
   long id = -10L;
   String templateName = null;
   private static Logger logger = Logger.getLogger(Skills.class.getName());
   public boolean paying = true;
   public boolean priest = false;
   public boolean hasSkillGain = true;
   private static final String moveWeek = "UPDATE SKILLS SET WEEK2=DAY7";
   private static final String moveDays = "UPDATE LOW_PRIORITY WURMPLAYERS.SKILLS SET DAY7=DAY6, DAY6=DAY5, DAY5=DAY4, DAY4=DAY3, DAY3=DAY2, DAY2=DAY1, DAY1=VALUE WHERE DAY7!=DAY6 OR DAY6!=DAY5 OR DAY5!=DAY4 OR DAY4!=DAY3 OR DAY3!=DAY2 OR DAY2!=DAY1 OR DAY1!=VALUE";
   private static final String moveDay6 = "UPDATE SKILLS SET DAY7=DAY6";
   private static final String moveDay5 = "UPDATE SKILLS SET DAY6=DAY5";
   private static final String moveDay4 = "UPDATE SKILLS SET DAY5=DAY4";
   private static final String moveDay3 = "UPDATE SKILLS SET DAY4=DAY3";
   private static final String moveDay2 = "UPDATE SKILLS SET DAY3=DAY2";
   private static final String moveDay1 = "UPDATE SKILLS SET DAY2=DAY1";
   private static final String moveDay0 = "UPDATE SKILLS SET DAY1=VALUE";
   public static final AtomicBoolean daySwitcherBeingRun = new AtomicBoolean();
   public static final float minChallengeValue = 21.0F;

   Skills() {
      this.skills = new TreeMap<>();
   }

   public boolean isTemplate() {
      return this.templateName != null;
   }

   boolean isPersonal() {
      return this.id != -10L;
   }

   private static final void switchWeek() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE SKILLS SET WEEK2=DAY7");
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "moveWeek: UPDATE SKILLS SET WEEK2=DAY7 - " + var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static final String getSkillSwitchString(int day) {
      switch(day) {
         case 0:
            return "UPDATE SKILLS SET DAY1=VALUE";
         case 1:
            return "UPDATE SKILLS SET DAY2=DAY1";
         case 2:
            return "UPDATE SKILLS SET DAY3=DAY2";
         case 3:
            return "UPDATE SKILLS SET DAY4=DAY3";
         case 4:
            return "UPDATE SKILLS SET DAY5=DAY4";
         case 5:
            return "UPDATE SKILLS SET DAY6=DAY5";
         case 6:
            return "UPDATE SKILLS SET DAY7=DAY6";
         default:
            logger.log(Level.WARNING, "This shouldn't happen: " + day);
            return "UPDATE SKILLS SET DAY7=DAY6";
      }
   }

   private static final void switchDay(int day) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      String psString = getSkillSwitchString(day);

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(psString);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Day: " + day + " - " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static final void switchDays() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE LOW_PRIORITY WURMPLAYERS.SKILLS SET DAY7=DAY6, DAY6=DAY5, DAY5=DAY4, DAY4=DAY3, DAY3=DAY2, DAY2=DAY1, DAY1=VALUE WHERE DAY7!=DAY6 OR DAY6!=DAY5 OR DAY5!=DAY4 OR DAY4!=DAY3 OR DAY3!=DAY2 OR DAY2!=DAY1 OR DAY1!=VALUE"
         );
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Update days - " + var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void switchSkills(final long now) {
      if (!Servers.localServer.LOGINSERVER && !Server.getInstance().isPS()) {
         if (daySwitcherBeingRun.get()) {
            return;
         }

         final boolean switchWeek = now - Servers.localServer.getSkillWeekSwitch() > 604800000L;
         final boolean switchDay = now - Servers.localServer.getSkillDaySwitch() > 86400000L;
         if (!switchDay && !switchWeek) {
            return;
         }

         daySwitcherBeingRun.set(true);
         Thread statsPoller = new Thread("Skills Day/Week Updater") {
            @Override
            public void run() {
               long start = System.currentTimeMillis();
               if (switchWeek) {
                  Skills.logger.log(Level.INFO, "Switching skill week");
                  Skills.switchWeek();
                  Servers.localServer.setSkillWeekSwitch(now);
               }

               if (switchDay) {
                  Skills.logger.log(Level.INFO, "Switching skill day");
                  Skills.switchDays();
                  Servers.localServer.setSkillDaySwitch(now);
               }

               Skills.logger.log(Level.INFO, "Skills Day/Week Updater took " + (System.currentTimeMillis() - start) + "ms");
               Skills.daySwitcherBeingRun.set(false);
            }
         };
         statsPoller.start();
      } else {
         Servers.localServer.setSkillDaySwitch(now);
         Servers.localServer.setSkillWeekSwitch(now);
      }
   }

   public TempSkill learnTemp(int skillNumber, float startValue) {
      TempSkill skill = new TempSkill(skillNumber, (double)startValue, this);
      int[] needed = skill.getDependencies();

      for(int x = 0; x < needed.length; ++x) {
         if (!this.skills.containsKey(needed[x])) {
            this.learnTemp(needed[x], 1.0F);
         }
      }

      if (this.id != -10L && WurmId.getType(this.id) == 0) {
         int parentSkillId = 0;
         if (needed.length > 0) {
            parentSkillId = needed[0];
         }

         try {
            if (parentSkillId != 0) {
               int parentType = SkillSystem.getTypeFor(parentSkillId);
               if (parentType == 0) {
                  parentSkillId = Integer.MAX_VALUE;
               }
            } else if (skill.getType() == 1) {
               parentSkillId = 2147483646;
            } else {
               parentSkillId = Integer.MAX_VALUE;
            }

            Affinity[] affs = Affinities.getAffinities(this.id);
            if (affs.length > 0) {
               for(int x = 0; x < affs.length; ++x) {
                  if (affs[x].skillNumber == skillNumber) {
                     skill.affinity = affs[x].number;
                  }
               }
            }

            Players.getInstance()
               .getPlayer(this.id)
               .getCommunicator()
               .sendAddSkill(skillNumber, parentSkillId, skill.getName(), startValue, startValue, skill.affinity);
         } catch (NoSuchPlayerException var8) {
         }
      }

      skill.touch();
      this.skills.put(skillNumber, skill);
      return skill;
   }

   @Nonnull
   public Skill learn(int skillNumber, float startValue) {
      return this.learn(skillNumber, startValue, true);
   }

   @Nonnull
   public Skill learn(int skillNumber, float startValue, boolean sendAdd) {
      Skill skill = new DbSkill(skillNumber, (double)startValue, this);
      int[] needed = skill.getDependencies();

      for(int aNeeded : needed) {
         if (!this.skills.containsKey(aNeeded)) {
            this.learn(aNeeded, 1.0F);
         }
      }

      if (this.id != -10L && WurmId.getType(this.id) == 0) {
         int parentSkillId = 0;
         if (needed.length > 0) {
            parentSkillId = needed[0];
         }

         try {
            if (parentSkillId != 0) {
               int parentType = SkillSystem.getTypeFor(parentSkillId);
               if (parentType == 0) {
                  parentSkillId = Integer.MAX_VALUE;
               }
            } else if (skill.getType() == 1) {
               parentSkillId = 2147483646;
            } else {
               parentSkillId = Integer.MAX_VALUE;
            }

            for(Affinity aff : Affinities.getAffinities(this.id)) {
               if (aff.skillNumber == skillNumber) {
                  skill.affinity = aff.number;
               }
            }

            Communicator comm = Players.getInstance().getPlayer(this.id).getCommunicator();
            if (sendAdd) {
               comm.sendAddSkill(skillNumber, parentSkillId, skill.getName(), startValue, startValue, skill.affinity);
            } else {
               comm.sendUpdateSkill(skillNumber, startValue, skill.affinity);
            }
         } catch (NoSuchPlayerException var12) {
            logger.log(Level.WARNING, "skillNumber: " + skillNumber + ", startValue: " + startValue, (Throwable)var12);
         }
      }

      skill.touch();
      this.skills.put(skillNumber, skill);

      try {
         skill.save();
         this.save();
      } catch (Exception var11) {
         logger.log(Level.WARNING, "Failed to save skill " + skill.getName() + "(" + skillNumber + ")", (Throwable)var11);
      }

      return skill;
   }

   @Nonnull
   public Skill getSkill(String name) throws NoSuchSkillException {
      Skill toReturn = null;

      for(Skill checked : this.skills.values()) {
         if (checked.getName().equals(name)) {
            toReturn = checked;
            break;
         }
      }

      if (toReturn == null) {
         throw new NoSuchSkillException("Unknown skill - " + name + ", total number of skills known is: " + this.skills.size());
      } else {
         return toReturn;
      }
   }

   @Nonnull
   public Skill getSkill(int number) throws NoSuchSkillException {
      Skill toReturn = this.skills.get(number);
      if (toReturn == null) {
         throw new NoSuchSkillException("Unknown skill - " + SkillSystem.getNameFor(number) + ", total number of skills known is: " + this.skills.size());
      } else {
         return toReturn;
      }
   }

   public final void switchSkillNumbers(Skill skillOne, Skill skillTwo) {
      int numberOne = skillTwo.getNumber();

      try {
         skillTwo.setNumber(skillOne.getNumber());
         this.skills.put(skillTwo.number, skillTwo);
         skillTwo.setKnowledge(skillTwo.knowledge, false, false);
      } catch (IOException var6) {
         logger.log(Level.INFO, var6.getMessage());
      }

      try {
         skillOne.setNumber(numberOne);
         this.skills.put(skillOne.number, skillOne);
         skillOne.setKnowledge(skillOne.knowledge, false, false);
      } catch (IOException var5) {
         logger.log(Level.INFO, var5.getMessage());
      }
   }

   @Nonnull
   public Skill getSkillOrLearn(int number) {
      Skill toReturn = this.skills.get(number);
      return toReturn == null ? this.learn(number, 1.0F) : toReturn;
   }

   public void checkDecay() {
      Set<Skill> memorySkills = new HashSet<>();
      Set<Skill> otherSkills = new HashSet<>();
      Set<Entry<Integer, Skill>> toRemove = new HashSet<>();

      for(Entry<Integer, Skill> entry : this.skills.entrySet()) {
         Skill toCheck = entry.getValue();

         try {
            if (toCheck.getType() == 1) {
               memorySkills.add(toCheck);
            } else {
               otherSkills.add(toCheck);
            }
         } catch (NullPointerException var8) {
            toRemove.add(entry);
         }
      }

      for(Skill mem : memorySkills) {
         mem.checkDecay();
      }

      for(Skill other : otherSkills) {
         other.checkDecay();
      }

      for(Entry<Integer, Skill> entry : toRemove) {
         Integer toremove = entry.getKey();
         this.skills.remove(toremove);
      }
   }

   public Map<Integer, Skill> getSkillTree() {
      return this.skills;
   }

   public Skill[] getSkills() {
      Skill[] toReturn = new Skill[this.skills.size()];
      int i = 0;

      for(Iterator<Skill> it = this.skills.values().iterator(); it.hasNext(); ++i) {
         toReturn[i] = it.next();
      }

      return toReturn;
   }

   public Skill[] getSkillsNoTemp() {
      Set<Skill> noTemps = new HashSet<>();

      for(Skill isTemp : this.skills.values()) {
         if (!isTemp.isTemporary()) {
            noTemps.add(isTemp);
         }
      }

      Skill[] toReturn = noTemps.toArray(new Skill[noTemps.size()]);
      return toReturn;
   }

   public void clone(Skill[] skillarr) {
      this.skills = new TreeMap<>();

      for(int x = 0; x < skillarr.length; ++x) {
         if (!skillarr[x].isTemporary() && !(skillarr[x] instanceof TempSkill)) {
            DbSkill newSkill = new DbSkill(skillarr[x].getNumber(), skillarr[x].knowledge, this);
            this.skills.put(skillarr[x].getNumber(), newSkill);

            try {
               newSkill.touch();
               newSkill.save();
            } catch (Exception var5) {
               logger.log(Level.WARNING, "Failed to save skill " + newSkill.getName() + " for " + this.id, (Throwable)var5);
            }
         } else {
            TempSkill newSkill = new TempSkill(skillarr[x].getNumber(), skillarr[x].knowledge, this);
            this.skills.put(skillarr[x].getNumber(), newSkill);
            newSkill.touch();
         }
      }
   }

   public long getId() {
      return this.id;
   }

   public static final void clearCreatureLoadMap() {
      creatureSkillsMap.clear();
   }

   public static final void loadAllCreatureSkills() throws Exception {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM SKILLS");
         rs = ps.executeQuery();

         while(rs.next()) {
            Skill skill = new DbSkill(rs.getLong("ID"), rs.getInt("NUMBER"), rs.getDouble("VALUE"), rs.getDouble("MINVALUE"), rs.getLong("LASTUSED"));
            long owner = rs.getLong("OWNER");
            Set<Skill> skills = creatureSkillsMap.get(owner);
            if (skills == null) {
               skills = new HashSet<>();
            }

            skills.add(skill);
            creatureSkillsMap.put(owner, skills);
         }
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final void fillCreatureTempSkills(Creature creature) {
      Skills cSkills = creature.getSkills();
      Map<Integer, Skill> treeSkills = creature.getSkills().getSkillTree();
      CreatureTemplate template = creature.getTemplate();

      try {
         Skills tSkills = template.getSkills();

         for(Skill ts : tSkills.getSkills()) {
            if (!treeSkills.containsKey(ts.getNumber())) {
               cSkills.learnTemp(ts.getNumber(), (float)ts.knowledge);
            }
         }
      } catch (Exception var9) {
         logger.log(Level.WARNING, "Unknown error while checking temp skill for creature: " + creature.getWurmId() + ".", (Throwable)var9);
      }
   }

   public final void initializeSkills() {
      Set<Skill> skillSet = creatureSkillsMap.get(this.id);
      if (skillSet != null) {
         for(Skill skill : skillSet) {
            Skill dbSkill = new DbSkill(skill.id, this, skill.getNumber(), skill.knowledge, skill.minimum, skill.lastUsed);
            this.skills.put(dbSkill.getNumber(), dbSkill);
         }
      }
   }

   public String getTemplateName() {
      return this.templateName;
   }

   public void saveDirty() throws IOException {
      if (this.id != -10L && WurmId.getType(this.id) == 0) {
         for(Skill skill : this.skills.values()) {
            skill.saveValue(true);
         }
      }
   }

   public void save() throws IOException {
      if (this.id != -10L && WurmId.getType(this.id) == 0) {
         for(Skill skill : this.skills.values()) {
            if (skill.isDirty()) {
               skill.saveValue(true);
            }
         }
      }
   }

   public final void addTempSkills() {
      float initialTempValue = WurmId.getType(this.id) == 0 ? Servers.localServer.getSkilloverallval() : 1.0F;

      for(int i = 0; i < SkillList.skillArray.length; ++i) {
         Integer key = SkillList.skillArray[i];
         if (!this.skills.containsKey(key)) {
            if (key == 1023 && WurmId.getType(this.id) == 0) {
               this.learnTemp(key, Servers.localServer.getSkillfightval());
            } else if (key == 100 && WurmId.getType(this.id) == 0) {
               this.learnTemp(key, Servers.localServer.getSkillmindval());
            } else if (key == 104 && WurmId.getType(this.id) == 0) {
               this.learnTemp(key, Servers.localServer.getSkillbcval());
            } else {
               this.learnTemp(key, initialTempValue);
            }
         }
      }
   }

   public abstract void load() throws Exception;

   public abstract void delete() throws Exception;
}

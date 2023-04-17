/*
 * Decompiled with CFR 0.152.
 */
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
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.DbSkill;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.TempSkill;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public abstract class Skills
implements MiscConstants,
CounterTypes,
TimeConstants {
    private static final ConcurrentHashMap<Long, Set<Skill>> creatureSkillsMap = new ConcurrentHashMap();
    Map<Integer, Skill> skills = new TreeMap<Integer, Skill>();
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
    public static final float minChallengeValue = 21.0f;

    Skills() {
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
            ps = dbcon.prepareStatement(moveWeek);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "moveWeek: UPDATE SKILLS SET WEEK2=DAY7 - " + ex.getMessage(), ex);
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

    private static final String getSkillSwitchString(int day) {
        switch (day) {
            case 0: {
                return moveDay0;
            }
            case 1: {
                return moveDay1;
            }
            case 2: {
                return moveDay2;
            }
            case 3: {
                return moveDay3;
            }
            case 4: {
                return moveDay4;
            }
            case 5: {
                return moveDay5;
            }
            case 6: {
                return moveDay6;
            }
        }
        logger.log(Level.WARNING, "This shouldn't happen: " + day);
        return moveDay6;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void switchDay(int day) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        String psString = Skills.getSkillSwitchString(day);
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(psString);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Day: " + day + " - " + ex.getMessage(), ex);
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

    private static final void switchDays() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(moveDays);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Update days - " + ex.getMessage(), ex);
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

    public static void switchSkills(long now) {
        if (!Servers.localServer.LOGINSERVER && !Server.getInstance().isPS()) {
            boolean switchDay;
            if (daySwitcherBeingRun.get()) {
                return;
            }
            boolean switchWeek = now - Servers.localServer.getSkillWeekSwitch() > 604800000L;
            boolean bl = switchDay = now - Servers.localServer.getSkillDaySwitch() > 86400000L;
            if (!switchDay && !switchWeek) {
                return;
            }
            daySwitcherBeingRun.set(true);
            Thread statsPoller = new Thread("Skills Day/Week Updater"){

                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    if (switchWeek) {
                        logger.log(Level.INFO, "Switching skill week");
                        Skills.switchWeek();
                        Servers.localServer.setSkillWeekSwitch(now);
                    }
                    if (switchDay) {
                        logger.log(Level.INFO, "Switching skill day");
                        Skills.switchDays();
                        Servers.localServer.setSkillDaySwitch(now);
                    }
                    logger.log(Level.INFO, "Skills Day/Week Updater took " + (System.currentTimeMillis() - start) + "ms");
                    daySwitcherBeingRun.set(false);
                }
            };
            statsPoller.start();
        } else {
            Servers.localServer.setSkillDaySwitch(now);
            Servers.localServer.setSkillWeekSwitch(now);
        }
    }

    public TempSkill learnTemp(int skillNumber, float startValue) {
        TempSkill skill = new TempSkill(skillNumber, startValue, this);
        int[] needed = skill.getDependencies();
        for (int x = 0; x < needed.length; ++x) {
            if (this.skills.containsKey(needed[x])) continue;
            this.learnTemp(needed[x], 1.0f);
        }
        if (this.id != -10L && WurmId.getType(this.id) == 0) {
            int parentSkillId = 0;
            if (needed.length > 0) {
                parentSkillId = needed[0];
            }
            try {
                if (parentSkillId != 0) {
                    short parentType = SkillSystem.getTypeFor(parentSkillId);
                    if (parentType == 0) {
                        parentSkillId = Integer.MAX_VALUE;
                    }
                } else {
                    parentSkillId = skill.getType() == 1 ? 0x7FFFFFFE : Integer.MAX_VALUE;
                }
                Affinity[] affs = Affinities.getAffinities(this.id);
                if (affs.length > 0) {
                    for (int x = 0; x < affs.length; ++x) {
                        if (affs[x].skillNumber != skillNumber) continue;
                        skill.affinity = affs[x].number;
                    }
                }
                Players.getInstance().getPlayer(this.id).getCommunicator().sendAddSkill(skillNumber, parentSkillId, skill.getName(), startValue, startValue, skill.affinity);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
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
        int[] needed;
        DbSkill skill = new DbSkill(skillNumber, startValue, this);
        for (int aNeeded : needed = skill.getDependencies()) {
            if (this.skills.containsKey(aNeeded)) continue;
            this.learn(aNeeded, 1.0f);
        }
        if (this.id != -10L && WurmId.getType(this.id) == 0) {
            int parentSkillId = 0;
            if (needed.length > 0) {
                parentSkillId = needed[0];
            }
            try {
                if (parentSkillId != 0) {
                    short parentType = SkillSystem.getTypeFor(parentSkillId);
                    if (parentType == 0) {
                        parentSkillId = Integer.MAX_VALUE;
                    }
                } else {
                    parentSkillId = skill.getType() == 1 ? 0x7FFFFFFE : Integer.MAX_VALUE;
                }
                for (Affinity aff : Affinities.getAffinities(this.id)) {
                    if (aff.skillNumber != skillNumber) continue;
                    skill.affinity = aff.number;
                }
                Communicator comm = Players.getInstance().getPlayer(this.id).getCommunicator();
                if (sendAdd) {
                    comm.sendAddSkill(skillNumber, parentSkillId, skill.getName(), startValue, startValue, skill.affinity);
                } else {
                    comm.sendUpdateSkill(skillNumber, startValue, skill.affinity);
                }
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "skillNumber: " + skillNumber + ", startValue: " + startValue, nsp);
            }
        }
        skill.touch();
        this.skills.put(skillNumber, skill);
        try {
            ((Skill)skill).save();
            this.save();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to save skill " + skill.getName() + "(" + skillNumber + ")", ex);
        }
        return skill;
    }

    @Nonnull
    public Skill getSkill(String name) throws NoSuchSkillException {
        Skill toReturn = null;
        for (Skill checked : this.skills.values()) {
            if (!checked.getName().equals(name)) continue;
            toReturn = checked;
            break;
        }
        if (toReturn == null) {
            throw new NoSuchSkillException("Unknown skill - " + name + ", total number of skills known is: " + this.skills.size());
        }
        return toReturn;
    }

    @Nonnull
    public Skill getSkill(int number) throws NoSuchSkillException {
        Skill toReturn = this.skills.get(number);
        if (toReturn == null) {
            throw new NoSuchSkillException("Unknown skill - " + SkillSystem.getNameFor(number) + ", total number of skills known is: " + this.skills.size());
        }
        return toReturn;
    }

    public final void switchSkillNumbers(Skill skillOne, Skill skillTwo) {
        int numberOne = skillTwo.getNumber();
        try {
            skillTwo.setNumber(skillOne.getNumber());
            this.skills.put(skillTwo.number, skillTwo);
            skillTwo.setKnowledge(skillTwo.knowledge, false, false);
        }
        catch (IOException iox2) {
            logger.log(Level.INFO, iox2.getMessage());
        }
        try {
            skillOne.setNumber(numberOne);
            this.skills.put(skillOne.number, skillOne);
            skillOne.setKnowledge(skillOne.knowledge, false, false);
        }
        catch (IOException iox) {
            logger.log(Level.INFO, iox.getMessage());
        }
    }

    @Nonnull
    public Skill getSkillOrLearn(int number) {
        Skill toReturn = this.skills.get(number);
        if (toReturn == null) {
            return this.learn(number, 1.0f);
        }
        return toReturn;
    }

    public void checkDecay() {
        HashSet<Skill> memorySkills = new HashSet<Skill>();
        HashSet<Skill> otherSkills = new HashSet<Skill>();
        HashSet<Map.Entry<Integer, Skill>> toRemove = new HashSet<Map.Entry<Integer, Skill>>();
        for (Map.Entry<Integer, Skill> entry : this.skills.entrySet()) {
            Skill toCheck = entry.getValue();
            try {
                if (toCheck.getType() == 1) {
                    memorySkills.add(toCheck);
                    continue;
                }
                otherSkills.add(toCheck);
            }
            catch (NullPointerException np) {
                toRemove.add(entry);
            }
        }
        for (Skill mem : memorySkills) {
            mem.checkDecay();
        }
        for (Skill other : otherSkills) {
            other.checkDecay();
        }
        for (Map.Entry<Integer, Skill> entry : toRemove) {
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
        Iterator<Skill> it = this.skills.values().iterator();
        while (it.hasNext()) {
            toReturn[i] = it.next();
            ++i;
        }
        return toReturn;
    }

    public Skill[] getSkillsNoTemp() {
        HashSet<Skill> noTemps = new HashSet<Skill>();
        for (Skill isTemp : this.skills.values()) {
            if (isTemp.isTemporary()) continue;
            noTemps.add(isTemp);
        }
        Skill[] toReturn = noTemps.toArray(new Skill[noTemps.size()]);
        return toReturn;
    }

    public void clone(Skill[] skillarr) {
        this.skills = new TreeMap<Integer, Skill>();
        for (int x = 0; x < skillarr.length; ++x) {
            Skill newSkill;
            if (!skillarr[x].isTemporary() && !(skillarr[x] instanceof TempSkill)) {
                newSkill = new DbSkill(skillarr[x].getNumber(), skillarr[x].knowledge, this);
                this.skills.put(skillarr[x].getNumber(), newSkill);
                try {
                    newSkill.touch();
                    ((DbSkill)newSkill).save();
                }
                catch (Exception iox) {
                    logger.log(Level.WARNING, "Failed to save skill " + newSkill.getName() + " for " + this.id, iox);
                }
                continue;
            }
            newSkill = new TempSkill(skillarr[x].getNumber(), skillarr[x].knowledge, this);
            this.skills.put(skillarr[x].getNumber(), newSkill);
            newSkill.touch();
        }
    }

    public long getId() {
        return this.id;
    }

    public static final void clearCreatureLoadMap() {
        creatureSkillsMap.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllCreatureSkills() throws Exception {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM SKILLS");
            rs = ps.executeQuery();
            while (rs.next()) {
                DbSkill skill = new DbSkill(rs.getLong("ID"), rs.getInt("NUMBER"), rs.getDouble("VALUE"), rs.getDouble("MINVALUE"), rs.getLong("LASTUSED"));
                long owner = rs.getLong("OWNER");
                Set<Skill> skills = creatureSkillsMap.get(owner);
                if (skills == null) {
                    skills = new HashSet<Skill>();
                }
                skills.add(skill);
                creatureSkillsMap.put(owner, skills);
            }
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    public static final void fillCreatureTempSkills(Creature creature) {
        Skills cSkills = creature.getSkills();
        Map<Integer, Skill> treeSkills = creature.getSkills().getSkillTree();
        CreatureTemplate template = creature.getTemplate();
        try {
            Skills tSkills = template.getSkills();
            for (Skill ts : tSkills.getSkills()) {
                if (treeSkills.containsKey(ts.getNumber())) continue;
                cSkills.learnTemp(ts.getNumber(), (float)ts.knowledge);
            }
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Unknown error while checking temp skill for creature: " + creature.getWurmId() + ".", e);
        }
    }

    public final void initializeSkills() {
        Set<Skill> skillSet = creatureSkillsMap.get(this.id);
        if (skillSet == null) {
            return;
        }
        for (Skill skill : skillSet) {
            DbSkill dbSkill = new DbSkill(skill.id, this, skill.getNumber(), skill.knowledge, skill.minimum, skill.lastUsed);
            this.skills.put(dbSkill.getNumber(), dbSkill);
        }
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public void saveDirty() throws IOException {
        if (this.id != -10L && WurmId.getType(this.id) == 0) {
            for (Skill skill : this.skills.values()) {
                skill.saveValue(true);
            }
        }
    }

    public void save() throws IOException {
        if (this.id != -10L && WurmId.getType(this.id) == 0) {
            for (Skill skill : this.skills.values()) {
                if (!skill.isDirty()) continue;
                skill.saveValue(true);
            }
        }
    }

    public final void addTempSkills() {
        float initialTempValue = WurmId.getType(this.id) == 0 ? Servers.localServer.getSkilloverallval() : 1.0f;
        for (int i = 0; i < SkillList.skillArray.length; ++i) {
            Integer key = SkillList.skillArray[i];
            if (this.skills.containsKey(key)) continue;
            if (key == 1023 && WurmId.getType(this.id) == 0) {
                this.learnTemp(key, Servers.localServer.getSkillfightval());
                continue;
            }
            if (key == 100 && WurmId.getType(this.id) == 0) {
                this.learnTemp(key, Servers.localServer.getSkillmindval());
                continue;
            }
            if (key == 104 && WurmId.getType(this.id) == 0) {
                this.learnTemp(key, Servers.localServer.getSkillbcval());
                continue;
            }
            this.learnTemp(key, initialTempValue);
        }
    }

    public abstract void load() throws Exception;

    public abstract void delete() throws Exception;

    static /* synthetic */ Logger access$000() {
        return logger;
    }

    static /* synthetic */ void access$100() {
        Skills.switchWeek();
    }

    static /* synthetic */ void access$200() {
        Skills.switchDays();
    }
}


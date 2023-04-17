/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.DbSkill;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Skill
implements MiscConstants,
CounterTypes,
TimeConstants,
Comparable<Skill> {
    public long lastUsed;
    protected double knowledge = 1.0;
    private static final double regainMultiplicator = 3.0;
    public double minimum;
    boolean joat = false;
    int number;
    private static final double maxBonus = 70.0;
    public static final Logger affinityDebug = Logger.getLogger("affinities");
    private static int totalAffinityChecks = 0;
    private static int totalAffinitiesGiven = 0;
    Skills parent;
    private static Logger logger = Logger.getLogger(Skill.class.getName());
    public int affinity = 0;
    private static final float affinityMultiplier = 0.1f;
    public long id = -10L;
    private Set<DoubleValueModifier> modifiers = null;
    private byte saveCounter = 0;
    private static Random random = new Random();
    private static final byte[][] chances = Skill.calculateChances();
    private static final double skillMod = Servers.localServer.EPIC ? 3.0 : 1.5;
    private static final double maxSkillGain = 1.0;
    private boolean basicPersonal = false;
    private boolean noCurve = false;
    protected static final boolean isChallenge = Servers.localServer.isChallengeServer();

    Skill(int aNumber, double startValue, Skills aParent) {
        this.number = aNumber;
        this.knowledge = Math.max(1.0, startValue);
        this.minimum = startValue;
        this.parent = aParent;
        if (aParent.isPersonal()) {
            if (WurmId.getType(aParent.getId()) == 0) {
                long l = this.id = this.isTemporary() ? WurmId.getNextTemporarySkillId() : WurmId.getNextPlayerSkillId();
                if (SkillSystem.getTypeFor(aNumber) == 0 || SkillSystem.getTypeFor(this.number) == 1) {
                    this.minimum = this.knowledge = Math.max(1.0, startValue);
                    this.basicPersonal = true;
                    this.noCurve = true;
                }
            } else {
                long l = this.id = this.isTemporary() ? WurmId.getNextTemporarySkillId() : WurmId.getNextCreatureSkillId();
            }
            if (this.number == 10076) {
                this.noCurve = true;
            }
        }
    }

    Skill(long _id, int _number, double _knowledge, double _minimum, long _lastused) {
        this.id = _id;
        this.number = _number;
        this.knowledge = _knowledge;
        this.minimum = _minimum;
        this.lastUsed = _lastused;
    }

    public boolean isDirty() {
        return this.saveCounter > 0;
    }

    Skill(long _id, Skills _parent, int _number, double _knowledge, double _minimum, long _lastused) {
        this.id = _id;
        this.parent = _parent;
        this.number = _number;
        this.knowledge = _knowledge;
        this.minimum = _minimum;
        this.lastUsed = _lastused;
        if (WurmId.getType(this.parent.getId()) == 0) {
            if (SkillSystem.getTypeFor(this.number) == 0 || SkillSystem.getTypeFor(this.number) == 1) {
                this.basicPersonal = true;
                this.noCurve = true;
            }
            if (this.number == 10076) {
                this.noCurve = true;
            }
        }
    }

    @Override
    public int compareTo(Skill otherSkill) {
        return this.getName().compareTo(otherSkill.getName());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final byte[][] calculateChances() {
        logger.log(Level.INFO, "Calculating skill chances...");
        long start = System.nanoTime();
        byte[][] toReturn = null;
        try {
            toReturn = DbSkill.loadSkillChances();
            if (toReturn == null) {
                throw new WurmServerException("Load failed. Creating chances.");
            }
            logger.log(Level.INFO, "Loaded skill chances succeeded.");
        }
        catch (Exception ex) {
            toReturn = new byte[101][101];
            for (int x = 0; x < 101; ++x) {
                for (int y = 0; y < 101; ++y) {
                    if (x == 0) {
                        toReturn[x][y] = 0;
                        continue;
                    }
                    if (y == 0) {
                        toReturn[x][y] = 99;
                        continue;
                    }
                    float succeed = 0.0f;
                    for (int t = 0; t < 1000; ++t) {
                        succeed += 1.0f;
                    }
                    toReturn[x][y] = (byte)(succeed /= 10.0f);
                }
            }
            Thread t = new Thread(){

                @Override
                public void run() {
                    logger.log(Level.INFO, "Starting to slowly build up statistics.");
                    byte[][] toSave = new byte[101][101];
                    for (int x = 0; x < 101; ++x) {
                        for (int y = 0; y < 101; ++y) {
                            if (x == 0) {
                                toSave[x][y] = 0;
                                continue;
                            }
                            if (y == 0) {
                                toSave[x][y] = 99;
                                continue;
                            }
                            float succeed = 0.0f;
                            for (int t2 = 0; t2 < 30000; ++t2) {
                                if (!(Skill.rollGaussian(x, y, 0L, "test") > 0.0f)) continue;
                                succeed += 1.0f;
                            }
                            toSave[x][y] = (byte)(succeed /= 300.0f);
                        }
                    }
                    try {
                        logger.log(Level.INFO, "Saving skill chances.");
                        DbSkill.saveSkillChances(toSave);
                    }
                    catch (Exception ex2) {
                        logger.log(Level.WARNING, "Saving failed.", ex2);
                    }
                }
            };
            t.setPriority(3);
            t.start();
        }
        finally {
            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
            logger.info("Done. Loading/Calculating skill chances from the database took " + lElapsedTime + " millis.");
        }
        return toReturn;
    }

    Skill(long aId, Skills aParent) throws IOException {
        this.id = aId;
        this.parent = aParent;
        this.load();
    }

    public void addModifier(DoubleValueModifier modifier) {
        if (this.modifiers == null) {
            this.modifiers = new HashSet<DoubleValueModifier>();
        }
        this.modifiers.add(modifier);
    }

    public void removeModifier(DoubleValueModifier modifier) {
        if (this.modifiers != null) {
            this.modifiers.remove(modifier);
        }
    }

    private boolean ignoresEnemy() {
        return SkillSystem.ignoresEnemies(this.number);
    }

    public double getModifierValues() {
        double toReturn = 0.0;
        if (this.modifiers != null) {
            Iterator<DoubleValueModifier> it = this.modifiers.iterator();
            while (it.hasNext()) {
                toReturn += it.next().getModifier();
            }
        }
        return toReturn;
    }

    void setParent(Skills skills) {
        this.parent = skills;
    }

    public String getName() {
        return SkillSystem.getNameFor(this.number);
    }

    public int getNumber() {
        return this.number;
    }

    public long getId() {
        return this.id;
    }

    public double getKnowledge() {
        return this.knowledge;
    }

    public double getKnowledge(double bonus) {
        long parentId;
        if (bonus > 70.0) {
            bonus = 70.0;
        }
        double bonusKnowledge = this.knowledge;
        if ((this.number == 102 || this.number == 105) && (parentId = this.parent.getId()) != -10L) {
            try {
                double pow;
                Creature holder = Server.getInstance().getCreature(parentId);
                float hellStrength = holder.getBonusForSpellEffect((byte)40);
                float forestGiantStrength = holder.getBonusForSpellEffect((byte)25);
                if (hellStrength > 0.0f) {
                    pow = 0.8;
                    double target = Math.pow(this.knowledge / 100.0, 0.8) * 100.0;
                    double diff = target - this.knowledge;
                    bonusKnowledge += diff * (double)hellStrength / 100.0;
                } else if (forestGiantStrength > 0.0f && this.number == 102) {
                    pow = 0.6;
                    double target = Math.pow(this.knowledge / 100.0, 0.6) * 100.0;
                    double diff = target - this.knowledge;
                    bonusKnowledge += diff * (double)forestGiantStrength / 100.0;
                }
                float ws = holder.getBonusForSpellEffect((byte)41);
                if (ws > 0.0f) {
                    bonusKnowledge *= (double)0.8f;
                }
            }
            catch (NoSuchPlayerException holder) {
            }
            catch (NoSuchCreatureException holder) {
                // empty catch block
            }
        }
        if (bonus != 0.0) {
            double linearMax = (100.0 + bonusKnowledge) / 2.0;
            double diffToMaxChange = Math.min(bonusKnowledge, linearMax - bonusKnowledge);
            double newBon = diffToMaxChange * bonus / 100.0;
            bonusKnowledge += newBon;
        }
        bonusKnowledge = Math.max(1.0, bonusKnowledge * (1.0 + this.getModifierValues()));
        if (!this.parent.paying) {
            if (!this.basicPersonal || Servers.localServer.PVPSERVER) {
                return Math.min(bonusKnowledge, 20.0);
            }
            return Math.min(bonusKnowledge, 30.0);
        }
        if (this.noCurve) {
            return bonusKnowledge;
        }
        return Server.getModifiedPercentageEffect(bonusKnowledge);
    }

    public double getKnowledge(Item item, double bonus) {
        long parentId;
        if (item == null || item.isBodyPart()) {
            return this.getKnowledge(bonus);
        }
        if (this.number == 1023) {
            try {
                int primweaponskill = item.getPrimarySkill();
                Skill pw = null;
                try {
                    pw = this.parent.getSkill(primweaponskill);
                    bonus += pw.getKnowledge(item, 0.0);
                }
                catch (NoSuchSkillException nss) {
                    pw = this.parent.learn(primweaponskill, 1.0f);
                    bonus += pw.getKnowledge(item, 0.0);
                }
            }
            catch (NoSuchSkillException primweaponskill) {
                // empty catch block
            }
        }
        double bonusKnowledge = 0.0;
        double ql = item.getCurrentQualityLevel();
        if (bonus > 70.0) {
            bonus = 70.0;
        }
        if (ql <= this.knowledge) {
            bonusKnowledge = (this.knowledge + ql) / 2.0;
        } else {
            double diff = ql - this.knowledge;
            bonusKnowledge = this.knowledge + this.knowledge * diff / 100.0;
        }
        if (this.number == 102 && (parentId = this.parent.getId()) != -10L) {
            try {
                float ws;
                Creature holder = Server.getInstance().getCreature(parentId);
                float hs = holder.getBonusForSpellEffect((byte)40);
                if (hs > 0.0f) {
                    if (this.knowledge < 40.0) {
                        double diff = 40.0 - this.knowledge;
                        bonusKnowledge += diff * (double)hs / 100.0;
                    }
                } else {
                    float x = holder.getBonusForSpellEffect((byte)25);
                    if (x > 0.0f && this.knowledge < 40.0) {
                        double diff = 40.0 - this.knowledge;
                        bonusKnowledge += diff * (double)x / 100.0;
                    }
                }
                if ((ws = holder.getBonusForSpellEffect((byte)41)) > 0.0f) {
                    bonusKnowledge *= (double)0.8f;
                }
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, nsp.getMessage(), nsp);
            }
            catch (NoSuchCreatureException nsp) {
                // empty catch block
            }
        }
        if (bonus != 0.0) {
            double linearMax = (100.0 + bonusKnowledge) / 2.0;
            double diffToMaxChange = Math.min(bonusKnowledge, linearMax - bonusKnowledge);
            double newBon = diffToMaxChange * bonus / 100.0;
            bonusKnowledge += newBon;
        }
        bonusKnowledge = Math.max(1.0, bonusKnowledge * (1.0 + this.getModifierValues()));
        if (!this.parent.paying) {
            if (!this.basicPersonal || Servers.localServer.PVPSERVER) {
                return Math.min(bonusKnowledge, 20.0);
            }
            return Math.min(bonusKnowledge, 30.0);
        }
        if (this.basicPersonal) {
            return bonusKnowledge;
        }
        return Server.getModifiedPercentageEffect(bonusKnowledge);
    }

    public final double getRealKnowledge() {
        if (this.parent.paying) {
            return this.getKnowledge();
        }
        if (!this.basicPersonal || Servers.localServer.PVPSERVER) {
            return Math.min(this.getKnowledge(), 20.0);
        }
        return Math.min(this.getKnowledge(), 30.0);
    }

    public void setKnowledge(double aKnowledge, boolean load) {
        this.setKnowledge(aKnowledge, load, false);
    }

    public void setKnowledge(double aKnowledge, boolean load, boolean setMinimum) {
        if (aKnowledge < 100.0) {
            double oldknowledge = this.knowledge;
            this.knowledge = Math.max(Math.min(aKnowledge, 100.0), 1.0);
            this.checkTitleChange(oldknowledge, this.knowledge);
            if (!load) {
                if (setMinimum) {
                    this.minimum = this.knowledge;
                }
                try {
                    this.save();
                }
                catch (IOException iox) {
                    logger.log(Level.INFO, "Failed to save skill " + this.id, iox);
                }
                long parentId = this.parent.getId();
                if (parentId != -10L && WurmId.getType(parentId) == 0) {
                    try {
                        Player holder = Players.getInstance().getPlayer(parentId);
                        double bonusKnowledge = this.knowledge;
                        if (this.number == 102) {
                            float ws;
                            float hs = holder.getBonusForSpellEffect((byte)40);
                            if (hs > 0.0f) {
                                if (this.knowledge < 40.0) {
                                    double diff = 40.0 - this.knowledge;
                                    bonusKnowledge = this.knowledge + diff * (double)hs / 100.0;
                                }
                            } else {
                                float x = holder.getBonusForSpellEffect((byte)25);
                                if (x > 0.0f && this.knowledge < 40.0) {
                                    double diff = 40.0 - this.knowledge;
                                    bonusKnowledge = this.knowledge + diff * (double)x / 100.0;
                                }
                            }
                            if ((ws = holder.getBonusForSpellEffect((byte)41)) > 0.0f) {
                                bonusKnowledge *= (double)0.8f;
                            }
                        }
                        if (!this.parent.paying && !this.basicPersonal) {
                            bonusKnowledge = Math.min(20.0, bonusKnowledge);
                        } else if (!this.parent.paying && bonusKnowledge > 20.0) {
                            bonusKnowledge = Math.min(this.getKnowledge(0.0), bonusKnowledge);
                        }
                        holder.getCommunicator().sendUpdateSkill(this.number, (float)bonusKnowledge, this.isTemporary() ? 0 : this.affinity);
                    }
                    catch (NoSuchPlayerException nsp) {
                        logger.log(Level.WARNING, nsp.getMessage(), nsp);
                    }
                }
            }
        }
    }

    public double getMinimumValue() {
        return this.minimum;
    }

    @Nonnull
    public int[] getDependencies() {
        return SkillSystem.getDependenciesFor(this.number);
    }

    public int[] getUniqueDependencies() {
        int[] fDeps = this.getDependencies();
        HashSet<Integer> lst = new HashSet<Integer>();
        for (int i = 0; i < fDeps.length; ++i) {
            Integer val = fDeps[i];
            if (lst.contains(val)) continue;
            lst.add(val);
        }
        int[] deps = new int[lst.size()];
        int ind = 0;
        for (Integer i : lst) {
            deps[ind] = i;
            ++ind;
        }
        return deps;
    }

    public double getDifficulty(boolean checkPriest) {
        return SkillSystem.getDifficultyFor(this.number, checkPriest);
    }

    public short getType() {
        return SkillSystem.getTypeFor(this.number);
    }

    public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider) {
        return this.skillCheck(check, bonus, test, times, useNewSystem, skillDivider, null, null);
    }

    public double skillCheck(double check, double bonus, boolean test, float times) {
        return this.skillCheck(check, bonus, test, 10.0f, true, 2.0);
    }

    public double skillCheck(double check, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent) {
        return this.skillCheck(check, bonus, test, 10.0f, true, 2.0, skillowner, opponent);
    }

    public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent) {
        if (skillowner == null || opponent == null || this.number == 10055 || this.number == 10053 || this.number == 10054) {
            // empty if block
        }
        this.touch();
        double power = this.checkAdvance(check, null, bonus, test, times, useNewSystem, skillDivider);
        if (WurmId.getType(this.parent.getId()) == 0) {
            try {
                this.save();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return power;
    }

    public double skillCheck(double check, Item item, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent) {
        return this.skillCheck(check, item, bonus, test, 10.0f, true, 2.0, skillowner, opponent);
    }

    public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent) {
        if (skillowner == null || opponent != null) {
            // empty if block
        }
        this.touch();
        double power = this.checkAdvance(check, item, bonus, test, times, useNewSystem, skillDivider);
        if (WurmId.getType(this.parent.getId()) == 0) {
            try {
                this.save();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return power;
    }

    public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider) {
        return this.skillCheck(check, item, bonus, test, times, useNewSystem, skillDivider, null, null);
    }

    public double skillCheck(double check, Item item, double bonus, boolean test, float times) {
        return this.skillCheck(check, item, bonus, test, 10.0f, true, 2.0, null, null);
    }

    public long getDecayTime() {
        return SkillSystem.getDecayTimeFor(this.number);
    }

    public void touch() {
        if (SkillSystem.getTickTimeFor(this.getNumber()) <= 0L) {
            this.lastUsed = System.currentTimeMillis();
        }
    }

    long getLastUsed() {
        return this.lastUsed;
    }

    boolean mayUpdateTimedSkill() {
        return System.currentTimeMillis() - this.lastUsed < SkillSystem.getTickTimeFor(this.getNumber());
    }

    void checkDecay() {
    }

    private void decay(boolean saved) {
        float decrease = 0.0f;
        if (this.getType() == 1) {
            this.alterSkill(-(100.0 - this.knowledge) / (this.getDifficulty(false) * this.knowledge), true, 1.0f);
        } else if (this.getType() == 0) {
            decrease = -0.1f;
            if (this.affinity > 0) {
                decrease = -0.1f + 0.05f * (float)this.affinity;
            }
            if (saved) {
                this.alterSkill(decrease / 2.0f, true, 1.0f);
            } else {
                this.alterSkill(decrease, true, 1.0f);
            }
        } else {
            decrease = -0.25f;
            if (this.affinity > 0) {
                decrease = -0.25f + 0.025f * (float)this.affinity;
            }
            if (saved) {
                this.alterSkill(decrease / 2.0f, true, 1.0f);
            } else {
                this.alterSkill(decrease, true, 1.0f);
            }
        }
    }

    public double getParentBonus() {
        double bonus = 0.0;
        int[] dep = this.getDependencies();
        for (int x = 0; x < dep.length; ++x) {
            short sType = SkillSystem.getTypeFor(dep[x]);
            if (sType != 2) continue;
            try {
                Skill enhancer = this.parent.getSkill(dep[x]);
                double ebonus = enhancer.getKnowledge(0.0);
                bonus += ebonus;
                continue;
            }
            catch (NoSuchSkillException ex) {
                logger.log(Level.WARNING, "Skill.checkAdvance(): Skillsystem bad. Skill '" + this.getName() + "' has no enhance parent with number " + dep[x] + ". Learning!", ex);
                this.parent.learn(dep[x], 1.0f);
            }
        }
        return bonus;
    }

    public double getChance(double check, @Nullable Item item, double bonus) {
        double skill = this.knowledge;
        if ((bonus += this.getParentBonus()) != 0.0 || item != null) {
            skill = item == null ? this.getKnowledge(bonus) : this.getKnowledge(item, bonus);
        }
        if (skill < 1.0) {
            skill = 1.0;
        }
        if (check < 1.0) {
            check = 1.0;
        }
        if (item != null && item.getSpellEffects() != null) {
            float skillBonus = (float)((100.0 - skill) * (double)(item.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SKILLCHECKBONUS) - 1.0f));
            skill += (double)skillBonus;
        }
        return Skill.getGaussianChance(skill, check);
    }

    public static final double getGaussianChance(double skill, double difficulty) {
        if (skill > 99.0 || difficulty > 99.0) {
            return Math.max(0.0, Math.min(100.0, ((skill * skill * skill - difficulty * difficulty * difficulty) / 50000.0 + (skill - difficulty)) / 2.0 + 50.0 + 0.5 * (skill - difficulty)));
        }
        return chances[(int)skill][(int)difficulty];
    }

    public static final float rollGaussian(float skill, float difficulty, long parentId, String name) {
        float slide = (skill * skill * skill - difficulty * difficulty * difficulty) / 50000.0f + (skill - difficulty);
        float w = 30.0f - Math.abs(skill - difficulty) / 4.0f;
        int attempts = 0;
        float result = 0.0f;
        do {
            result = (float)random.nextGaussian() * (w + Math.abs(slide) / 6.0f) + slide;
            float rejectCutoff = (float)random.nextGaussian() * (w - Math.abs(slide) / 6.0f) + slide;
            if (slide > 0.0f) {
                if (result > rejectCutoff + Math.max(100.0f - slide, 0.0f)) {
                    result = -1000.0f;
                }
            } else if (result < rejectCutoff - Math.max(100.0f + slide, 0.0f)) {
                result = -1000.0f;
            }
            if (++attempts != 100) continue;
            if (result > 100.0f) {
                return 90.0f + Server.rand.nextFloat() * 5.0f;
            }
            if (!(result < -100.0f)) continue;
            return -90.0f - Server.rand.nextFloat() * 5.0f;
        } while (result < -100.0f || result > 100.0f);
        return result;
    }

    private double checkAdvance(double check, @Nullable Item item, double bonus, boolean dryRun, float times, boolean useNewSystem, double skillDivider) {
        Player p;
        if (!dryRun) {
            dryRun = this.mayUpdateTimedSkill();
        }
        check = Math.max(1.0, check);
        short skillType = SkillSystem.getTypeFor(this.number);
        int[] dep = this.getUniqueDependencies();
        for (int x = 0; x < dep.length; ++x) {
            String name2;
            short sType = SkillSystem.getTypeFor(dep[x]);
            if (sType == 2) {
                try {
                    Skill enhancer = this.parent.getSkill(dep[x]);
                    double ebonus = Math.max(0.0, enhancer.skillCheck(check, 0.0, dryRun, times, useNewSystem, skillDivider) / 10.0);
                    bonus += ebonus;
                }
                catch (NoSuchSkillException ex) {
                    Creature cret = null;
                    try {
                        cret = Server.getInstance().getCreature(this.parent.getId());
                    }
                    catch (NoSuchCreatureException noSuchCreatureException) {
                    }
                    catch (NoSuchPlayerException noSuchPlayerException) {
                        // empty catch block
                    }
                    name2 = "Unknown creature";
                    if (cret != null) {
                        name2 = cret.getName();
                    }
                    logger.log(Level.WARNING, name2 + " - Skill.checkAdvance(): Skillsystem bad. Skill '" + this.getName() + "' has no enhance parent with number " + dep[x], ex);
                    this.parent.learn(dep[x], 1.0f);
                }
                continue;
            }
            try {
                Skill par = this.parent.getSkill(dep[x]);
                if (par.getNumber() == 1023) continue;
                par.skillCheck(check, 0.0, dryRun, times, useNewSystem, skillDivider);
                continue;
            }
            catch (NoSuchSkillException ex) {
                Creature cret = null;
                try {
                    cret = Server.getInstance().getCreature(this.parent.getId());
                }
                catch (NoSuchCreatureException name2) {
                }
                catch (NoSuchPlayerException name2) {
                    // empty catch block
                }
                name2 = "Unknown creature";
                if (cret != null) {
                    name2 = cret.getName();
                }
                logger.log(Level.WARNING, name2 + ": Skill.checkAdvance(): Skillsystem bad. Skill '" + this.getName() + "' has no limiting parent with number " + dep[x], ex);
                this.parent.learn(dep[x], 1.0f);
            }
        }
        bonus = Math.min(70.0, bonus);
        double skill = this.knowledge;
        double learnMod = 1.0;
        if (item == null) {
            skill = this.getKnowledge(bonus);
        } else {
            skill = this.getKnowledge(item, bonus);
            if (item.getSpellSkillBonus() > 0.0f) {
                learnMod += (double)(item.getSpellSkillBonus() / 100.0f);
            }
        }
        if (item != null && item.getSpellEffects() != null) {
            float skillBonus = (float)((100.0 - skill) * (double)(item.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SKILLCHECKBONUS) - 1.0f));
            skill += (double)skillBonus;
        }
        double power = Skill.rollGaussian((float)skill, (float)check, this.parent.getId(), this.getName());
        if (!dryRun) {
            if (useNewSystem) {
                double divs = skillDivider;
                this.doSkillGainNew(check, power, learnMod, times, divs);
            } else {
                this.doSkillGainOld(power, learnMod, times);
            }
        }
        if (power > 0.0 && (p = Players.getInstance().getPlayerOrNull(this.parent.getId())) != null) {
            ++totalAffinityChecks;
            if (p.shouldGiveAffinity(this.affinity, skillType == 1 || skillType == 0)) {
                if (this.affinity == 0) {
                    p.getCommunicator().sendNormalServerMessage("You realize that you have developed an affinity for " + SkillSystem.getNameFor(this.number).toLowerCase() + ".", (byte)2);
                } else {
                    p.getCommunicator().sendNormalServerMessage("You realize that your affinity for " + SkillSystem.getNameFor(this.number).toLowerCase() + " has grown stronger.", (byte)2);
                }
                Affinities.setAffinity(p.getWurmId(), this.number, this.affinity + 1, false);
                affinityDebug.log(Level.INFO, p.getName() + " gained affinity for skill " + SkillSystem.getNameFor(this.number) + " from skill usage. New affinity: " + this.affinity + ". Total checks this restart: " + totalAffinityChecks + " Total affinities given this restart: " + ++totalAffinitiesGiven);
            }
        }
        return power;
    }

    private final void doSkillGainNew(double check, double power, double learnMod, float times, double skillDivider) {
        double bonus = 1.0;
        double diff = Math.abs(check - this.knowledge);
        short sType = SkillSystem.getTypeFor(this.number);
        boolean awardBonus = true;
        if (sType == 1 || sType == 0) {
            awardBonus = false;
        }
        if (diff <= 15.0 && awardBonus) {
            bonus = 1.0 + (double)0.1f * (diff / 15.0);
        }
        if (power < 0.0) {
            if (this.knowledge < 20.0) {
                this.alterSkill((100.0 - this.knowledge) / (this.getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod * bonus, false, times, true, skillDivider);
            }
        } else {
            this.alterSkill((100.0 - this.knowledge) / (this.getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod * bonus, false, times, true, skillDivider);
        }
    }

    private final void doSkillGainOld(double power, double learnMod, float times) {
        if (!(power < 0.0)) {
            if (this.knowledge < 20.0) {
                this.alterSkill((100.0 - this.knowledge) / (this.getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod, false, times);
            } else if (power > 0.0 && power < 40.0) {
                this.alterSkill((100.0 - this.knowledge) / (this.getDifficulty(this.parent.priest) * this.knowledge * this.knowledge) * learnMod, false, times);
            } else if (this.number == 10055 || this.number == 10053 || this.number == 10054) {
                Creature cret = null;
                try {
                    cret = Server.getInstance().getCreature(this.parent.getId());
                    if (cret.loggerCreature1 > 0L) {
                        logger.log(Level.INFO, cret.getName() + " POWER=" + power);
                    }
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }

    protected void alterSkill(double advanceMultiplicator, boolean decay, float times) {
        this.alterSkill(advanceMultiplicator, decay, times, false, 1.0);
    }

    protected void alterSkill(double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider) {
        if (this.parent.hasSkillGain) {
            times = Math.min(SkillSystem.getTickTimeFor(this.getNumber()) > 0L || this.getNumber() == 10033 ? 100.0f : 30.0f, times);
            advanceMultiplicator *= (double)(times * Servers.localServer.getSkillGainRate());
            this.lastUsed = System.currentTimeMillis();
            boolean isplayer = false;
            long pid = this.parent.getId();
            if (WurmId.getType(pid) == 0) {
                isplayer = true;
            }
            double oldknowledge = this.knowledge;
            if (decay) {
                if (isplayer) {
                    if (this.knowledge <= 70.0) {
                        return;
                    }
                    double villageMod = 1.0;
                    try {
                        Player player = Players.getInstance().getPlayer(pid);
                        villageMod = player.getVillageSkillModifier();
                    }
                    catch (NoSuchPlayerException nsp) {
                        logger.log(Level.WARNING, "Player with id " + this.id + " is decaying skills while not online?", nsp);
                    }
                    this.knowledge = Math.max(1.0, this.knowledge + advanceMultiplicator * villageMod);
                } else {
                    this.knowledge = Math.max(1.0, this.knowledge + advanceMultiplicator);
                }
            } else {
                advanceMultiplicator *= skillMod;
                if (this.number == 10086 && Servers.localServer.isChallengeOrEpicServer() && !Server.getInstance().isPS()) {
                    advanceMultiplicator *= 2.0;
                }
                if (isplayer) {
                    try {
                        Player player = Players.getInstance().getPlayer(pid);
                        advanceMultiplicator *= (double)(1.0f + ItemBonus.getSkillGainBonus(player, this.getNumber()));
                        int currstam = player.getStatus().getStamina();
                        float staminaMod = 1.0f;
                        if (currstam <= 400) {
                            staminaMod = 0.1f;
                        }
                        if (player.getCultist() != null && player.getCultist().levelElevenSkillgain()) {
                            staminaMod *= 1.25f;
                        }
                        if (player.getDeity() != null) {
                            if (player.mustChangeTerritory() && !player.isFighting()) {
                                staminaMod = 0.1f;
                                if (Server.rand.nextInt(100) == 0) {
                                    player.getCommunicator().sendAlertServerMessage("You sense a lack of energy. Rumours have it that " + player.getDeity().name + " wants " + player.getDeity().getHisHerItsString() + " champions to move between kingdoms and seek out the enemy.");
                                }
                            }
                            if (player.getDeity().isLearner()) {
                                if (player.getFaith() > 20.0f && player.getFavor() >= 10.0f) {
                                    staminaMod += 0.1f;
                                }
                            } else if (player.getDeity().isWarrior() && player.getFaith() > 20.0f && player.getFavor() >= 20.0f && this.isFightingSkill()) {
                                staminaMod += 0.25f;
                            }
                        }
                        staminaMod += Math.max(player.getStatus().getNutritionlevel() / 10.0f - 0.05f, 0.0f);
                        if (player.isFighting() && currstam <= 400) {
                            staminaMod = 0.0f;
                        }
                        advanceMultiplicator *= (double)staminaMod;
                        if (player.getEnemyPresense() > Player.minEnemyPresence && !this.ignoresEnemy()) {
                            advanceMultiplicator *= (double)0.8f;
                        }
                        if (this.knowledge < this.minimum || this.basicPersonal && this.knowledge < 20.0) {
                            advanceMultiplicator *= 3.0;
                        }
                        if (player.hasSleepBonus()) {
                            advanceMultiplicator *= 2.0;
                        }
                        int taffinity = this.affinity + (AffinitiesTimed.isTimedAffinity(pid, this.getNumber()) ? 1 : 0);
                        advanceMultiplicator *= (double)(1.0f + (float)taffinity * 0.1f);
                        if (player.getMovementScheme().samePosCounts > 20) {
                            advanceMultiplicator = 0.0;
                        }
                        if (!player.isPaying() && this.knowledge >= 20.0) {
                            advanceMultiplicator = 0.0;
                            if (!player.isPlayerAssistant() && Server.rand.nextInt(500) == 0) {
                                player.getCommunicator().sendNormalServerMessage("You may only gain skill beyond level 20 if you have a premium account.", (byte)2);
                            }
                        }
                        if ((this.number == 10055 || this.number == 10053 || this.number == 10054) && player.loggerCreature1 > 0L) {
                            logger.log(Level.INFO, player.getName() + " advancing " + Math.min(1.0, advanceMultiplicator * this.knowledge / skillDivider) + "!");
                        }
                    }
                    catch (NoSuchPlayerException nsp) {
                        advanceMultiplicator = 0.0;
                        logger.log(Level.WARNING, "Player with id " + this.id + " is learning skills while not online?", nsp);
                    }
                }
                if (useNewSystem) {
                    double maxSkillRate = 40.0;
                    double rateMod = 1.0;
                    short sType = SkillSystem.getTypeFor(this.number);
                    if (sType == 1 || sType == 0) {
                        maxSkillRate = 60.0;
                        rateMod = 0.8;
                    }
                    double skillRate = Math.min(maxSkillRate, skillDivider * (1.0 + this.knowledge / (100.0 - 90.0 * (this.knowledge / 110.0))) * rateMod);
                    this.knowledge = Math.max(1.0, this.knowledge + Math.min(1.0, advanceMultiplicator * this.knowledge / skillRate));
                } else {
                    this.knowledge = Math.max(1.0, this.knowledge + Math.min(1.0, advanceMultiplicator * this.knowledge));
                }
                if (this.minimum < this.knowledge) {
                    this.minimum = this.knowledge;
                }
                this.checkTitleChange(oldknowledge, this.knowledge);
            }
            try {
                if (oldknowledge != this.knowledge && (this.saveCounter == 0 || this.knowledge > 50.0) || decay) {
                    this.saveValue(isplayer);
                }
                this.saveCounter = (byte)(this.saveCounter + 1);
                if (this.saveCounter == 10) {
                    this.saveCounter = 0;
                }
            }
            catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to save skill " + this.getName() + "(" + this.getNumber() + ") for creature " + this.parent.getId(), ex);
            }
            if (pid != -10L && isplayer) {
                try {
                    int diff;
                    Player holder = Players.getInstance().getPlayer(pid);
                    float weakMod = 1.0f;
                    double bonusKnowledge = this.knowledge;
                    float ws = holder.getBonusForSpellEffect((byte)41);
                    if (ws > 0.0f) {
                        weakMod = 0.8f;
                    }
                    if (this.number == 102 && this.knowledge < 40.0) {
                        float x = holder.getBonusForSpellEffect((byte)25);
                        if (x > 0.0f) {
                            double diff2 = 40.0 - this.knowledge;
                            bonusKnowledge = this.knowledge + diff2 * (double)x / 100.0;
                        } else {
                            float hs = holder.getBonusForSpellEffect((byte)40);
                            if (hs > 0.0f) {
                                double diff3 = 40.0 - this.knowledge;
                                bonusKnowledge = this.knowledge + diff3 * (double)hs / 100.0;
                            }
                        }
                    }
                    bonusKnowledge *= (double)weakMod;
                    if (isplayer && (diff = (int)this.knowledge - (int)oldknowledge) > 0) {
                        holder.achievement(371, diff);
                    }
                    if (!this.parent.paying && !this.basicPersonal) {
                        bonusKnowledge = Math.min(20.0, bonusKnowledge);
                    } else if (!this.parent.paying && bonusKnowledge > 20.0) {
                        bonusKnowledge = Math.min(this.getKnowledge(0.0), bonusKnowledge);
                    }
                    holder.getCommunicator().sendUpdateSkill(this.number, (float)bonusKnowledge, this.isTemporary() ? 0 : this.affinity);
                    if (this.number != 0x7FFFFFFC && this.number != 0x7FFFFFFA) {
                        holder.resetInactivity(true);
                    }
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
                }
            }
        }
    }

    public boolean isTemporary() {
        return false;
    }

    public boolean isFightingSkill() {
        return SkillSystem.isFightingSkill(this.number);
    }

    public void checkInitialTitle() {
        Titles.Title title;
        Player p;
        long pid;
        if (this.getNumber() == 10067 && WurmId.getType(pid = this.parent.getId()) == 0) {
            if (this.knowledge >= 20.0 && (p = Players.getInstance().getPlayerOrNull(pid)) != null) {
                p.maybeTriggerAchievement(605, true);
            }
            if (this.knowledge >= 50.0 && (p = Players.getInstance().getPlayerOrNull(pid)) != null) {
                p.maybeTriggerAchievement(617, true);
            }
        }
        if (this.knowledge >= 50.0) {
            pid = this.parent.getId();
            if (WurmId.getType(pid) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.NORMAL)) != null) {
                try {
                    Players.getInstance().getPlayer(pid).addTitle(title);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
                }
            }
            if ((p = Players.getInstance().getPlayerOrNull(pid)) != null) {
                p.maybeTriggerAchievement(555, true);
            }
        }
        if (this.knowledge >= 70.0) {
            pid = this.parent.getId();
            if (WurmId.getType(pid) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.MINOR)) != null) {
                try {
                    Players.getInstance().getPlayer(pid).addTitle(title);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
                }
            }
            if ((p = Players.getInstance().getPlayerOrNull(pid)) != null) {
                p.maybeTriggerAchievement(564, true);
            }
            if (p != null && this.getNumber() == 10066) {
                p.maybeTriggerAchievement(633, true);
            }
        }
        if (this.knowledge >= 90.0) {
            pid = this.parent.getId();
            if (WurmId.getType(pid) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.MASTER)) != null) {
                try {
                    Players.getInstance().getPlayer(pid).addTitle(title);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
                }
            }
            if ((p = Players.getInstance().getPlayerOrNull(pid)) != null) {
                p.maybeTriggerAchievement(590, true);
            }
        }
        if (this.knowledge >= 99.99999615 && WurmId.getType(pid = this.parent.getId()) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.LEGENDARY)) != null) {
            try {
                Players.getInstance().getPlayer(pid).addTitle(title);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
            }
        }
    }

    void checkTitleChange(double oldknowledge, double newknowledge) {
        Titles.Title title;
        long pid;
        if (this.getNumber() == 10067 && oldknowledge < 20.0 && newknowledge >= 20.0 && WurmId.getType(pid = this.parent.getId()) == 0) {
            try {
                Player p = Players.getInstance().getPlayer(pid);
                p.maybeTriggerAchievement(605, true);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
            }
        }
        if (oldknowledge < 50.0 && newknowledge >= 50.0 && WurmId.getType(pid = this.parent.getId()) == 0) {
            title = Titles.Title.getTitle(this.number, Titles.TitleType.NORMAL);
            if (title != null) {
                try {
                    Player p = Players.getInstance().getPlayer(pid);
                    p.addTitle(title);
                    p.achievement(555);
                    if (this.getNumber() == 10067) {
                        p.maybeTriggerAchievement(617, true);
                    }
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
                }
            }
            int count = 0;
            for (Skill s : this.parent.getSkills()) {
                if (!(s.getKnowledge() >= 50.0)) continue;
                ++count;
            }
            if (count >= 10) {
                try {
                    Player p = Players.getInstance().getPlayer(pid);
                    p.maybeTriggerAchievement(598, true);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
        if (oldknowledge < 70.0 && newknowledge >= 70.0 && WurmId.getType(pid = this.parent.getId()) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.MINOR)) != null) {
            try {
                Player p = Players.getInstance().getPlayer(pid);
                p.addTitle(title);
                p.achievement(564);
                if (this.getNumber() == 10066) {
                    p.maybeTriggerAchievement(633, true);
                }
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
            }
        }
        if (oldknowledge < 90.0 && newknowledge >= 90.0 && WurmId.getType(pid = this.parent.getId()) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.MASTER)) != null) {
            try {
                Player p = Players.getInstance().getPlayer(pid);
                p.addTitle(title);
                p.achievement(590);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
            }
        }
        if (oldknowledge < 99.99999615 && newknowledge >= 99.99999615 && WurmId.getType(pid = this.parent.getId()) == 0 && (title = Titles.Title.getTitle(this.number, Titles.TitleType.LEGENDARY)) != null) {
            try {
                Players.getInstance().getPlayer(pid).addTitle(title);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, pid + ":" + nsp.getMessage(), nsp);
            }
        }
    }

    public void setAffinity(int aff) {
        this.affinity = aff;
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0 && !this.isTemporary()) {
            try {
                Player holder = Players.getInstance().getPlayer(pid);
                float weakMod = 1.0f;
                double bonusKnowledge = this.knowledge;
                float ws = holder.getBonusForSpellEffect((byte)41);
                if (ws > 0.0f) {
                    weakMod = 0.8f;
                }
                if (this.number == 102 && this.knowledge < 40.0) {
                    float x = holder.getBonusForSpellEffect((byte)25);
                    if (x > 0.0f) {
                        double diff = 40.0 - this.knowledge;
                        bonusKnowledge = this.knowledge + diff * (double)x / 100.0;
                    } else {
                        float hs = holder.getBonusForSpellEffect((byte)40);
                        if (hs > 0.0f) {
                            double diff = 40.0 - this.knowledge;
                            bonusKnowledge = this.knowledge + diff * (double)hs / 100.0;
                        }
                    }
                }
                bonusKnowledge *= (double)weakMod;
                if (!this.parent.paying && !this.basicPersonal) {
                    bonusKnowledge = Math.min(20.0, bonusKnowledge);
                } else if (!this.parent.paying && bonusKnowledge > 20.0) {
                    bonusKnowledge = Math.min(this.getKnowledge(0.0), bonusKnowledge);
                }
                holder.getCommunicator().sendUpdateSkill(this.number, (float)bonusKnowledge, this.affinity);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, nsp.getMessage(), nsp);
            }
        }
    }

    abstract void save() throws IOException;

    abstract void load() throws IOException;

    abstract void saveValue(boolean var1) throws IOException;

    public abstract void setJoat(boolean var1) throws IOException;

    public abstract void setNumber(int var1) throws IOException;

    public boolean hasLowCreationGain() {
        switch (this.getNumber()) {
            case 1010: 
            case 10034: 
            case 10036: 
            case 10037: 
            case 10041: 
            case 10042: 
            case 10083: 
            case 10091: {
                return false;
            }
        }
        return true;
    }

    public void maybeSetMinimum() {
        if (this.minimum < this.knowledge) {
            this.minimum = this.knowledge;
            try {
                this.save();
            }
            catch (IOException iox) {
                logger.log(Level.INFO, "Failed to save skill " + this.id, iox);
            }
        }
    }

    public static int getTotalAffinityChecks() {
        return totalAffinityChecks;
    }

    public static int getTotalAffinitiesGiven() {
        return totalAffinitiesGiven;
    }

    static /* synthetic */ Logger access$000() {
        return logger;
    }
}


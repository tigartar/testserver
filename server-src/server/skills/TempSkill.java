/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TempSkill
extends Skill {
    private static Logger logger = Logger.getLogger(TempSkill.class.getName());

    public TempSkill(int aNumber, double aStartValue, Skills aParent) {
        super(aNumber, aStartValue, aParent);
    }

    public TempSkill(long aId, Skills aParent, int aNumber, double aKnowledge, double aMinimum, long aLastused) {
        super(aId, aParent, aNumber, aKnowledge, aMinimum, aLastused);
    }

    public TempSkill(long aId, Skills aParent) throws IOException {
        super(aId, aParent);
    }

    @Override
    void save() throws IOException {
    }

    @Override
    void load() throws IOException {
    }

    @Override
    void saveValue(boolean aPlayer) throws IOException {
    }

    @Override
    public void setJoat(boolean aJoat) throws IOException {
    }

    @Override
    public void setNumber(int newNumber) throws IOException {
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0) {
            try {
                Player player = Players.getInstance().getPlayer(pid);
                Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.setNumber(newNumber);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        } else {
            try {
                Creature creature = Creatures.getInstance().getCreature(pid);
                Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.setNumber(newNumber);
            }
            catch (NoSuchCreatureException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        }
    }

    @Override
    protected void alterSkill(double advanceMultiplicator, boolean decay, float times) {
        this.alterSkill(advanceMultiplicator, decay, times, false, 1.0);
    }

    @Override
    protected void alterSkill(double advanceMultiplicator, boolean decay, float times, boolean useNewSystem, double skillDivider) {
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0) {
            try {
                Player player = Players.getInstance().getPlayer(pid);
                Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.alterSkill(advanceMultiplicator, decay, times, useNewSystem, skillDivider);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        } else {
            try {
                Creature creature = Creatures.getInstance().getCreature(pid);
                Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.alterSkill(advanceMultiplicator, decay, times, useNewSystem, skillDivider);
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsc);
            }
        }
    }

    @Override
    public void setKnowledge(double aKnowledge, boolean load) {
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0) {
            try {
                Player player = Players.getInstance().getPlayer(pid);
                Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.setKnowledge(aKnowledge, load);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        } else {
            try {
                Creature creature = Creatures.getInstance().getCreature(pid);
                Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.setKnowledge(aKnowledge, load);
            }
            catch (NoSuchCreatureException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        }
    }

    @Override
    public void setKnowledge(double aKnowledge, boolean load, boolean setMinimum) {
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0) {
            try {
                Player player = Players.getInstance().getPlayer(pid);
                Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.setKnowledge(aKnowledge, load, setMinimum);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        } else {
            try {
                Creature creature = Creatures.getInstance().getCreature(pid);
                Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
                realSkill.setKnowledge(aKnowledge, load, setMinimum);
            }
            catch (NoSuchCreatureException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            }
        }
    }

    @Override
    public double skillCheck(double check, double bonus, boolean test, float times) {
        return this.skillCheck(check, bonus, test, 10.0f, true, 1.1f, null, null);
    }

    @Override
    public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider) {
        return this.skillCheck(check, bonus, test, 10.0f, true, 1.1f, null, null);
    }

    @Override
    public double skillCheck(double check, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent) {
        return this.skillCheck(check, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
    }

    @Override
    public double skillCheck(double check, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent) {
        if (skillowner != null) {
            Skill realSkill = skillowner.getSkills().learn(this.number, (float)this.knowledge, false);
            return realSkill.skillCheck(check, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
        }
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0) {
            try {
                Player player = Players.getInstance().getPlayer(pid);
                Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
                return realSkill.skillCheck(check, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
                return 0.0;
            }
        }
        try {
            Creature creature = Creatures.getInstance().getCreature(pid);
            Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
            return realSkill.skillCheck(check, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
        }
        catch (NoSuchCreatureException nsp) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            return 0.0;
        }
    }

    @Override
    public double skillCheck(double check, Item item, double bonus, boolean test, float times, @Nullable Creature skillowner, @Nullable Creature opponent) {
        return this.skillCheck(check, item, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
    }

    @Override
    public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider, @Nullable Creature skillowner, @Nullable Creature opponent) {
        if (skillowner != null) {
            Skill realSkill = skillowner.getSkills().learn(this.number, (float)this.knowledge, false);
            return realSkill.skillCheck(check, item, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
        }
        long pid = this.parent.getId();
        if (WurmId.getType(pid) == 0) {
            try {
                Player player = Players.getInstance().getPlayer(pid);
                Skill realSkill = player.getSkills().learn(this.number, (float)this.knowledge, false);
                return realSkill.skillCheck(check, item, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
                return 0.0;
            }
        }
        try {
            Creature creature = Creatures.getInstance().getCreature(pid);
            Skill realSkill = creature.getSkills().learn(this.number, (float)this.knowledge, false);
            return realSkill.skillCheck(check, item, bonus, test, 10.0f, true, 1.1f, skillowner, opponent);
        }
        catch (NoSuchCreatureException nsp) {
            logger.log(Level.WARNING, "Unable to find owner for skill, parentid: " + pid, nsp);
            return 0.0;
        }
    }

    @Override
    public double skillCheck(double check, Item item, double bonus, boolean test, float times) {
        return this.skillCheck(check, item, bonus, test, 10.0f, true, 1.1f, null, null);
    }

    @Override
    public double skillCheck(double check, Item item, double bonus, boolean test, float times, boolean useNewSystem, double skillDivider) {
        return this.skillCheck(check, item, bonus, test, 10.0f, true, 1.1f, null, null);
    }

    @Override
    public final boolean isTemporary() {
        return true;
    }
}


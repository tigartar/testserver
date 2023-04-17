/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

import com.wurmonline.server.Point;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.epic.ValreiFightHistory;
import com.wurmonline.shared.constants.ValreiConstants;
import java.util.Random;

public class ValreiFight {
    private static final int MAP_SIZE = 7;
    private static final short MODIFIER_NORMAL = 0;
    private static final short MODIFIER_BLANK = -1;
    private static final Point test1 = new Point(0, 0);
    private static final Point test2 = new Point(0, 0);
    private final MapHex mapHex;
    private final FightEntity fighter1;
    private final FightEntity fighter2;
    private ValreiFightHistory fightHistory;
    private ValreiFightHex[][] fightMap;
    private Random fightRand;

    public ValreiFight(MapHex mapHex, EpicEntity fighter1, EpicEntity fighter2) {
        this.mapHex = mapHex;
        this.fighter1 = new FightEntity(fighter1);
        this.fighter2 = new FightEntity(fighter2);
    }

    public ValreiFightHistory completeFight(boolean test) {
        this.fightHistory = new ValreiFightHistory(this.mapHex.getId(), this.mapHex.getName());
        this.fightHistory.addFighter(this.fighter1.getEntityId(), this.fighter1.getEntityName());
        this.fightHistory.addFighter(this.fighter2.getEntityId(), this.fighter2.getEntityName());
        this.fightRand = test ? new Random(System.nanoTime()) : new Random(this.fightHistory.getFightTime());
        this.fightMap = this.createFightMap();
        this.moveEntity(this.fighter1, 1, 1);
        this.moveEntity(this.fighter2, 5, 5);
        this.fighter1.setMaxFavor(25.0f + 0.75f * this.fighter1.rollSkill(105, 106));
        this.fighter1.setMaxKarma(25.0f + 0.75f * this.fighter1.rollSkill(106, 100));
        this.fighter2.setMaxFavor(25.0f + 0.75f * this.fighter2.rollSkill(105, 106));
        this.fighter2.setMaxKarma(25.0f + 0.75f * this.fighter2.rollSkill(106, 100));
        FightEntity currentFighter = this.fighter2;
        if (this.fighter1.rollInitiative() > this.fighter2.rollInitiative()) {
            currentFighter = this.fighter1;
        }
        while (!this.fightHistory.isFightCompleted()) {
            if (this.takeTurn(currentFighter)) {
                if (this.fighter1.getHealth() <= 0.0f && this.fighter2.getHealth() > 0.0f) {
                    this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(this.fighter2.getEntityId()));
                } else if (this.fighter2.getHealth() <= 0.0f && this.fighter1.getHealth() > 0.0f) {
                    this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(this.fighter1.getEntityId()));
                } else {
                    this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(-1L));
                }
                this.fightHistory.setFightCompleted(true);
            }
            if (currentFighter == this.fighter2) {
                currentFighter = this.fighter1;
                continue;
            }
            currentFighter = this.fighter2;
        }
        if (!test) {
            this.fightHistory.saveActions();
        }
        return this.fightHistory;
    }

    private boolean takeTurn(FightEntity e) {
        float karmaGone;
        float favorGone;
        FightEntity opponent = e == this.fighter1 ? this.fighter2 : this.fighter1;
        int actionCount = 2;
        boolean smartRound = e.rollSkill(100) > 0.0f;
        float spellRegen = e.rollSkill(100, 101);
        if (spellRegen > 0.0f && (favorGone = e.getMaxKarma() - e.getFavor()) + (karmaGone = e.getMaxFavor() - e.getKarma()) > 0.0f) {
            float favorPercent = favorGone / (favorGone + karmaGone);
            float karmaPercent = karmaGone / (favorGone + karmaGone);
            e.setFavor(Math.min(e.getMaxFavor(), e.getFavor() + spellRegen * favorPercent));
            e.setKarma(Math.min(e.getMaxKarma(), e.getKarma() + spellRegen * karmaPercent));
        }
        while (actionCount > 0 && e.getHealth() > 0.0f && opponent.getHealth() > 0.0f) {
            boolean moveTowards = true;
            int currentAction = 4 + this.fightRand.nextInt(4);
            int distance = e.getDistanceTo(opponent);
            if (smartRound) {
                short preferredAction = e.getPreferredAction();
                switch (preferredAction) {
                    case 4: {
                        if (distance > 1) {
                            currentAction = 2;
                            break;
                        }
                        currentAction = preferredAction;
                        break;
                    }
                    case 5: {
                        if (distance <= 2) {
                            currentAction = 2;
                            moveTowards = false;
                            break;
                        }
                        currentAction = preferredAction;
                        break;
                    }
                }
            }
            if (currentAction == 6 && e.getFavor() < 20.0f || currentAction == 7 && e.getKarma() < 20.0f) {
                currentAction = distance > 2 ? 5 : 4;
            }
            if (currentAction == 4 && distance > 1) {
                currentAction = 2;
                moveTowards = true;
            }
            Point moveTarget = e.getTargetMove(moveTowards, opponent);
            if (currentAction == 2 && !this.isMoveValid(e, moveTarget.getX(), moveTarget.getY())) {
                currentAction = distance > 1 ? 5 : 4;
            }
            switch (currentAction) {
                case 2: {
                    this.moveEntity(e, moveTarget.getX(), moveTarget.getY());
                    --actionCount;
                    break;
                }
                case 4: 
                case 5: {
                    this.attackEntity(e, opponent, (short)currentAction);
                    --actionCount;
                    break;
                }
                case 6: 
                case 7: {
                    this.castSpell(e, opponent, (short)currentAction);
                    --actionCount;
                }
            }
        }
        return e.getHealth() <= 0.0f || opponent.getHealth() <= 0.0f;
    }

    private void moveEntity(FightEntity e, int xPos, int yPos) {
        e.xPos = xPos;
        e.yPos = yPos;
        byte[] moveData = ValreiConstants.getMoveData(e.getEntityId(), xPos, yPos);
        this.fightHistory.addAction((short)2, moveData);
    }

    private void attackEntity(FightEntity attacker, FightEntity defender, short attackType) {
        float attackRoll = attackType == 4 ? attacker.rollSkill(102, 104, attacker.getAttackBuffed()) : attacker.rollSkill(104, 103, attacker.getAttackBuffed());
        float defendRoll = defender.rollSkill(103, 102, defender.getPhysDefBuffed());
        float damage = Math.min(attackRoll, attackRoll - defendRoll);
        if (attackRoll < 0.0f) {
            damage = -1.0f;
        } else if (defendRoll > attackRoll) {
            damage = 0.0f;
        }
        if (damage > 0.0f) {
            defender.setHealth(defender.getHealth() - (damage /= 3.0f));
        }
        byte[] attackData = ValreiConstants.getAttackData(attacker.getEntityId(), defender.getEntityId(), damage);
        this.fightHistory.addAction(attackType, attackData);
    }

    private void castSpell(FightEntity caster, FightEntity defender, short spellType) {
        float casterRoll = spellType == 6 ? caster.rollSkill(105, 106) : caster.rollSkill(106, 100);
        float defendRoll = defender.rollSkill(101, 105, defender.getSpellDefBuffed());
        byte s = 1;
        if (spellType == 6) {
            s = caster.getDeitySpell(defender);
        } else if (spellType == 7) {
            s = caster.getSorcerySpell(defender);
        }
        float damage = -100.0f;
        switch (s) {
            case 1: {
                casterRoll = spellType == 6 ? caster.rollSkill(105, 106, caster.getAttackBuffed()) : caster.rollSkill(106, 100, caster.getAttackBuffed());
                damage = Math.min(casterRoll, casterRoll - defendRoll);
                if (casterRoll < 0.0f) {
                    damage = -1.0f;
                } else if (defendRoll > casterRoll) {
                    damage = 0.0f;
                }
                if (!(damage > 0.0f)) break;
                defender.setHealth(defender.getHealth() - (damage /= 2.0f));
                if (spellType == 6) {
                    caster.setFavor(caster.getFavor() - 20.0f);
                    break;
                }
                caster.setKarma(caster.getKarma() - 20.0f);
                break;
            }
            case 0: {
                damage = casterRoll;
                if (casterRoll < 0.0f) {
                    damage = -1.0f;
                }
                if (!(damage > 0.0f)) break;
                caster.setHealth(Math.min(100.0f, caster.getHealth() + (damage /= 2.0f)));
                caster.setFavor(caster.getFavor() - 30.0f);
                break;
            }
            case 4: {
                damage = casterRoll;
                if (casterRoll < 0.0f) {
                    damage = -1.0f;
                }
                if (!(damage > 0.0f)) break;
                caster.setAttackBuffed(damage / 50.0f);
                caster.setFavor(caster.getFavor() - 50.0f);
                break;
            }
            case 2: {
                damage = casterRoll;
                if (casterRoll < 0.0f) {
                    damage = -1.0f;
                }
                if (!(damage > 0.0f)) break;
                caster.setPhysDefBuffed(damage / 50.0f);
                caster.setKarma(caster.getKarma() - 60.0f);
                break;
            }
            case 3: {
                damage = casterRoll;
                if (casterRoll < 0.0f) {
                    damage = -1.0f;
                }
                if (!(damage > 0.0f)) break;
                caster.setSpellDefBuffed(damage / 50.0f);
                caster.setKarma(caster.getKarma() - 60.0f);
            }
        }
        byte[] spellData = ValreiConstants.getSpellData(caster.getEntityId(), defender.getEntityId(), s, damage);
        this.fightHistory.addAction(spellType, spellData);
    }

    private ValreiFightHex[][] createFightMap() {
        ValreiFightHex[][] toReturn = new ValreiFightHex[7][7];
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                toReturn[i][j] = new ValreiFightHex(i, j);
                if (j + 1 < 4) {
                    if (i < 4 + j) continue;
                    toReturn[i][j].setModifier((short)-1);
                    continue;
                }
                if (j + 1 <= 4 || i > j - 7) continue;
                toReturn[i][j].setModifier((short)-1);
            }
        }
        return toReturn;
    }

    private final boolean isMoveValid(FightEntity e, int mapX, int mapY) {
        FightEntity opponent;
        if (this.fightMap == null) {
            return false;
        }
        if (mapX < 0 || mapY < 0 || mapX >= 7 || mapY >= 7) {
            return false;
        }
        if (this.fightMap[mapX][mapY].getModifier() == -1) {
            return false;
        }
        FightEntity fightEntity = opponent = e == this.fighter1 ? this.fighter2 : this.fighter1;
        if (mapX == opponent.xPos && mapY == opponent.yPos) {
            return false;
        }
        return mapX != e.xPos || mapY != e.yPos;
    }

    class ValreiFightHex {
        private int xPos;
        private int yPos;
        private short modifierType;

        ValreiFightHex(int xPos, int yPos) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.modifierType = 0;
        }

        public void setModifier(short newModifier) {
            this.modifierType = newModifier;
        }

        public short getModifier() {
            return this.modifierType;
        }

        public int getX() {
            return this.xPos;
        }

        public int getY() {
            return this.yPos;
        }
    }

    class FightEntity {
        private int xPos;
        private int yPos;
        private float health;
        private float maxFavor;
        private float maxKarma;
        private float favor;
        private float karma;
        private float attackBuffed = 0.0f;
        private float physDefBuffed = 0.0f;
        private float spellDefBuffed = 0.0f;
        private EpicEntity entityBase;

        FightEntity(EpicEntity entity) {
            this.entityBase = entity;
            this.health = 100.0f;
            this.favor = 100.0f;
            this.karma = 100.0f;
        }

        public long getEntityId() {
            return this.entityBase.getId();
        }

        public String getEntityName() {
            return this.entityBase.getName();
        }

        public float rollInitiative() {
            float bodyCon = this.entityBase.getCurrentSkill(104);
            return ValreiFight.this.fightRand.nextFloat() * 10.0f + (bodyCon += this.entityBase.getCurrentSkill(101) / 3.0f) / 10.0f;
        }

        public float rollSkill(int skillId) {
            return this.rollSkill(skillId, -1);
        }

        public float rollSkill(int skillId, int bonusSkillId, float skillBuffed) {
            return this.rollSkill(skillId, bonusSkillId, 3.0f, skillBuffed);
        }

        public float rollSkill(int skillId, int bonusSkillId) {
            return this.rollSkill(skillId, bonusSkillId, 3.0f, 0.0f);
        }

        public float rollSkill(int skillId, int bonusSkillId, float bonusModifier, float skillBuffed) {
            EpicEntity.SkillVal skillValue = this.entityBase.getSkill(skillId);
            if (skillValue != null) {
                float actualVal = skillValue.getCurrentVal();
                EpicEntity.SkillVal bonusVal = this.entityBase.getSkill(bonusSkillId);
                if (bonusVal != null) {
                    actualVal += bonusVal.getCurrentVal() / bonusModifier;
                }
                actualVal -= ValreiFight.this.fightRand.nextFloat() * 100.0f;
                if (skillBuffed > 0.0f) {
                    actualVal += (100.0f - actualVal) * skillBuffed;
                }
                return actualVal;
            }
            return -100.0f;
        }

        public float getAttackBuffed() {
            return this.attackBuffed;
        }

        public void setAttackBuffed(float isBuffed) {
            this.attackBuffed = isBuffed;
        }

        public float getPhysDefBuffed() {
            return this.physDefBuffed;
        }

        public void setPhysDefBuffed(float isBuffed) {
            this.physDefBuffed = isBuffed;
        }

        public float getSpellDefBuffed() {
            return this.spellDefBuffed;
        }

        public void setSpellDefBuffed(float isBuffed) {
            this.spellDefBuffed = isBuffed;
        }

        public float getHealth() {
            return this.health;
        }

        public void setHealth(float newHealth) {
            this.health = newHealth;
        }

        public float getMaxFavor() {
            return this.maxFavor;
        }

        public void setMaxFavor(float newMax) {
            this.maxFavor = newMax;
            if (this.favor > this.maxFavor) {
                this.setFavor(this.maxFavor);
            }
        }

        public float getFavor() {
            return this.favor;
        }

        public void setFavor(float newFavor) {
            this.favor = newFavor;
        }

        public float getMaxKarma() {
            return this.maxKarma;
        }

        public void setMaxKarma(float newMax) {
            this.maxKarma = newMax;
            if (this.karma > this.maxKarma) {
                this.setKarma(this.maxKarma);
            }
        }

        public float getKarma() {
            return this.karma;
        }

        public void setKarma(float newKarma) {
            this.karma = newKarma;
        }

        public int getDistanceTo(FightEntity other) {
            int totalDist = 0;
            test1.setXY(this.xPos, this.yPos);
            test2.setXY(other.xPos, other.yPos);
            while (test1.getX() != test2.getX() || test1.getY() != test2.getY()) {
                if (test1.getY() != test2.getY()) {
                    int yDiff = 0;
                    int xDiff = 0;
                    if (test1.getY() < test2.getY()) {
                        yDiff = 1;
                        if (test1.getX() < test2.getX()) {
                            xDiff = 1;
                        }
                    } else {
                        yDiff = -1;
                        if (test1.getX() > test2.getX()) {
                            xDiff = -1;
                        }
                    }
                    test1.setX(test1.getX() + xDiff);
                    test1.setY(test1.getY() + yDiff);
                } else {
                    test1.setX(test1.getX() + (test1.getX() < test2.getX() ? 1 : -1));
                }
                ++totalDist;
            }
            return totalDist;
        }

        public Point getTargetMove(boolean towards, FightEntity other) {
            test1.setXY(this.xPos, this.yPos);
            test2.setXY(other.xPos, other.yPos);
            if (test1.getY() != test2.getY()) {
                int newY;
                int newX;
                int i;
                if (towards) {
                    int yDiff = 0;
                    int xDiff = 0;
                    if (test1.getY() < test2.getY()) {
                        yDiff = 1;
                        if (test1.getX() < test2.getX()) {
                            xDiff = 1;
                        }
                    } else {
                        yDiff = -1;
                        if (test1.getX() > test2.getX()) {
                            xDiff = -1;
                        }
                    }
                    return new Point(test1.getX() + xDiff, test1.getY() + yDiff);
                }
                int testDir = ValreiFight.this.fightRand.nextInt(3);
                for (i = 0; i < 3; ++i) {
                    newX = test1.getX();
                    newY = test1.getY();
                    switch (testDir) {
                        case 0: {
                            if ((newY += test2.getY() > test1.getY() ? -1 : (test2.getY() == test1.getY() ? 0 : 1)) == test1.getY() || !ValreiFight.this.isMoveValid(this, newX, newY)) break;
                            return new Point(newX, newY);
                        }
                        case 1: {
                            if ((newX += test2.getX() > test1.getX() ? -1 : (test2.getX() == test1.getX() ? 0 : 1)) == test1.getX() || !ValreiFight.this.isMoveValid(this, newX, newY)) break;
                            return new Point(newX, newY);
                        }
                        case 2: {
                            if (test2.getX() > test1.getX() || test2.getY() > test1.getY()) {
                                --newX;
                                --newY;
                            } else if (test2.getX() < test1.getX() || test2.getY() < test1.getY()) {
                                ++newX;
                                ++newY;
                            }
                            if (newX == test1.getX() && newY == test1.getY() || !ValreiFight.this.isMoveValid(this, newX, newY)) break;
                            return new Point(newX, newY);
                        }
                    }
                    if (++testDir != 3) continue;
                    testDir = 0;
                }
                testDir = ValreiFight.this.fightRand.nextInt(3);
                for (i = 0; i < 3; ++i) {
                    newX = test1.getX();
                    newY = test1.getY();
                    switch (testDir) {
                        case 0: {
                            if ((newY += test2.getY() > test1.getY() ? 1 : (test2.getY() == test1.getY() ? 0 : -1)) == test1.getY() || !ValreiFight.this.isMoveValid(this, newX, newY)) break;
                            return new Point(newX, newY);
                        }
                        case 1: {
                            if ((newX += test2.getX() > test1.getX() ? 1 : (test2.getX() == test1.getX() ? 0 : -1)) == test1.getX() || !ValreiFight.this.isMoveValid(this, newX, newY)) break;
                            return new Point(newX, newY);
                        }
                        case 2: {
                            if (test2.getX() > test1.getX() || test2.getY() > test1.getY()) {
                                ++newX;
                                ++newY;
                            } else if (test2.getX() < test1.getX() || test2.getY() < test1.getY()) {
                                --newX;
                                --newY;
                            }
                            if (newX == test1.getX() && newY == test1.getY() || !ValreiFight.this.isMoveValid(this, newX, newY)) break;
                            return new Point(newX, newY);
                        }
                    }
                    if (++testDir != 3) continue;
                    testDir = 0;
                }
            } else if (test1.getX() != test2.getX()) {
                return new Point(test1.getX() + (test1.getX() < test2.getX() ? 1 : -1), test1.getY());
            }
            return new Point(test1.getX(), test1.getY());
        }

        public short getPreferredAction() {
            float meleeAtk = this.entityBase.getCurrentSkill(102) + this.entityBase.getCurrentSkill(104) / 3.0f;
            float rangedAtk = this.entityBase.getCurrentSkill(104) + this.entityBase.getCurrentSkill(103) / 3.0f;
            float deitySpell = this.entityBase.getCurrentSkill(105) + this.entityBase.getCurrentSkill(106) / 3.0f;
            float sorcSpell = this.entityBase.getCurrentSkill(106) + this.entityBase.getCurrentSkill(100) / 3.0f;
            if (meleeAtk > deitySpell && meleeAtk > sorcSpell && meleeAtk > rangedAtk) {
                return 4;
            }
            if (rangedAtk > deitySpell && rangedAtk > sorcSpell) {
                return 5;
            }
            if (deitySpell > sorcSpell) {
                return 6;
            }
            return 7;
        }

        public byte getDeitySpell(FightEntity defender) {
            int preferredType = 1;
            if (this.getFavor() >= 30.0f && this.getHealth() < 75.0f && defender.getHealth() > this.getHealth()) {
                preferredType = 0;
            } else if (this.getFavor() >= 50.0f && this.getAttackBuffed() == 0.0f && defender.entityBase.getCurrentSkill(101) > defender.entityBase.getCurrentSkill(103)) {
                preferredType = 4;
            }
            return (byte)preferredType;
        }

        public byte getSorcerySpell(FightEntity defender) {
            int preferredType = 1;
            if (this.getKarma() >= 60.0f && this.getHealth() < 75.0f && defender.getHealth() > this.getHealth()) {
                float spellHigh;
                float attackHigh = Math.max(defender.entityBase.getCurrentSkill(102), defender.entityBase.getCurrentSkill(104));
                if (attackHigh > (spellHigh = Math.max(defender.entityBase.getCurrentSkill(105), defender.entityBase.getCurrentSkill(106))) && this.getPhysDefBuffed() == 0.0f) {
                    preferredType = 2;
                } else if (this.getSpellDefBuffed() == 0.0f) {
                    preferredType = 3;
                }
            }
            return (byte)preferredType;
        }
    }
}


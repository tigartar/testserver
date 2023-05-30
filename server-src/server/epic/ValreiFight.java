package com.wurmonline.server.epic;

import com.wurmonline.server.Point;
import com.wurmonline.shared.constants.ValreiConstants;
import java.util.Random;

public class ValreiFight {
   private static final int MAP_SIZE = 7;
   private static final short MODIFIER_NORMAL = 0;
   private static final short MODIFIER_BLANK = -1;
   private static final Point test1 = new Point(0, 0);
   private static final Point test2 = new Point(0, 0);
   private final MapHex mapHex;
   private final ValreiFight.FightEntity fighter1;
   private final ValreiFight.FightEntity fighter2;
   private ValreiFightHistory fightHistory;
   private ValreiFight.ValreiFightHex[][] fightMap;
   private Random fightRand;

   public ValreiFight(MapHex mapHex, EpicEntity fighter1, EpicEntity fighter2) {
      this.mapHex = mapHex;
      this.fighter1 = new ValreiFight.FightEntity(fighter1);
      this.fighter2 = new ValreiFight.FightEntity(fighter2);
   }

   public ValreiFightHistory completeFight(boolean test) {
      this.fightHistory = new ValreiFightHistory(this.mapHex.getId(), this.mapHex.getName());
      this.fightHistory.addFighter(this.fighter1.getEntityId(), this.fighter1.getEntityName());
      this.fightHistory.addFighter(this.fighter2.getEntityId(), this.fighter2.getEntityName());
      if (test) {
         this.fightRand = new Random(System.nanoTime());
      } else {
         this.fightRand = new Random(this.fightHistory.getFightTime());
      }

      this.fightMap = this.createFightMap();
      this.moveEntity(this.fighter1, 1, 1);
      this.moveEntity(this.fighter2, 5, 5);
      this.fighter1.setMaxFavor(25.0F + 0.75F * this.fighter1.rollSkill(105, 106));
      this.fighter1.setMaxKarma(25.0F + 0.75F * this.fighter1.rollSkill(106, 100));
      this.fighter2.setMaxFavor(25.0F + 0.75F * this.fighter2.rollSkill(105, 106));
      this.fighter2.setMaxKarma(25.0F + 0.75F * this.fighter2.rollSkill(106, 100));
      ValreiFight.FightEntity currentFighter = this.fighter2;
      if (this.fighter1.rollInitiative() > this.fighter2.rollInitiative()) {
         currentFighter = this.fighter1;
      }

      while(!this.fightHistory.isFightCompleted()) {
         if (this.takeTurn(currentFighter)) {
            if (this.fighter1.getHealth() <= 0.0F && this.fighter2.getHealth() > 0.0F) {
               this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(this.fighter2.getEntityId()));
            } else if (this.fighter2.getHealth() <= 0.0F && this.fighter1.getHealth() > 0.0F) {
               this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(this.fighter1.getEntityId()));
            } else {
               this.fightHistory.addAction((short)8, ValreiConstants.getEndFightData(-1L));
            }

            this.fightHistory.setFightCompleted(true);
         }

         if (currentFighter == this.fighter2) {
            currentFighter = this.fighter1;
         } else {
            currentFighter = this.fighter2;
         }
      }

      if (!test) {
         this.fightHistory.saveActions();
      }

      return this.fightHistory;
   }

   private boolean takeTurn(ValreiFight.FightEntity e) {
      ValreiFight.FightEntity opponent = e == this.fighter1 ? this.fighter2 : this.fighter1;
      int actionCount = 2;
      boolean smartRound = e.rollSkill(100) > 0.0F;
      float spellRegen = e.rollSkill(100, 101);
      if (spellRegen > 0.0F) {
         float favorGone = e.getMaxKarma() - e.getFavor();
         float karmaGone = e.getMaxFavor() - e.getKarma();
         if (favorGone + karmaGone > 0.0F) {
            float favorPercent = favorGone / (favorGone + karmaGone);
            float karmaPercent = karmaGone / (favorGone + karmaGone);
            e.setFavor(Math.min(e.getMaxFavor(), e.getFavor() + spellRegen * favorPercent));
            e.setKarma(Math.min(e.getMaxKarma(), e.getKarma() + spellRegen * karmaPercent));
         }
      }

      while(actionCount > 0 && e.getHealth() > 0.0F && opponent.getHealth() > 0.0F) {
         boolean moveTowards = true;
         short currentAction = (short)(4 + this.fightRand.nextInt(4));
         int distance = e.getDistanceTo(opponent);
         if (smartRound) {
            short preferredAction = e.getPreferredAction();
            switch(preferredAction) {
               case 4:
                  if (distance > 1) {
                     currentAction = 2;
                  } else {
                     currentAction = preferredAction;
                  }
                  break;
               case 5:
                  if (distance <= 2) {
                     currentAction = 2;
                     moveTowards = false;
                  } else {
                     currentAction = preferredAction;
                  }
               case 6:
               case 7:
            }
         }

         if (currentAction == 6 && e.getFavor() < 20.0F || currentAction == 7 && e.getKarma() < 20.0F) {
            if (distance > 2) {
               currentAction = 5;
            } else {
               currentAction = 4;
            }
         }

         if (currentAction == 4 && distance > 1) {
            currentAction = 2;
            moveTowards = true;
         }

         Point moveTarget = e.getTargetMove(moveTowards, opponent);
         if (currentAction == 2 && !this.isMoveValid(e, moveTarget.getX(), moveTarget.getY())) {
            if (distance > 1) {
               currentAction = 5;
            } else {
               currentAction = 4;
            }
         }

         switch(currentAction) {
            case 2:
               this.moveEntity(e, moveTarget.getX(), moveTarget.getY());
               --actionCount;
            case 3:
            default:
               break;
            case 4:
            case 5:
               this.attackEntity(e, opponent, currentAction);
               --actionCount;
               break;
            case 6:
            case 7:
               this.castSpell(e, opponent, currentAction);
               --actionCount;
         }
      }

      return e.getHealth() <= 0.0F || opponent.getHealth() <= 0.0F;
   }

   private void moveEntity(ValreiFight.FightEntity e, int xPos, int yPos) {
      e.xPos = xPos;
      e.yPos = yPos;
      byte[] moveData = ValreiConstants.getMoveData(e.getEntityId(), xPos, yPos);
      this.fightHistory.addAction((short)2, moveData);
   }

   private void attackEntity(ValreiFight.FightEntity attacker, ValreiFight.FightEntity defender, short attackType) {
      float attackRoll = attackType == 4 ? attacker.rollSkill(102, 104, attacker.getAttackBuffed()) : attacker.rollSkill(104, 103, attacker.getAttackBuffed());
      float defendRoll = defender.rollSkill(103, 102, defender.getPhysDefBuffed());
      float damage = Math.min(attackRoll, attackRoll - defendRoll);
      if (attackRoll < 0.0F) {
         damage = -1.0F;
      } else if (defendRoll > attackRoll) {
         damage = 0.0F;
      }

      if (damage > 0.0F) {
         damage /= 3.0F;
         defender.setHealth(defender.getHealth() - damage);
      }

      byte[] attackData = ValreiConstants.getAttackData(attacker.getEntityId(), defender.getEntityId(), damage);
      this.fightHistory.addAction(attackType, attackData);
   }

   private void castSpell(ValreiFight.FightEntity caster, ValreiFight.FightEntity defender, short spellType) {
      float casterRoll = spellType == 6 ? caster.rollSkill(105, 106) : caster.rollSkill(106, 100);
      float defendRoll = defender.rollSkill(101, 105, defender.getSpellDefBuffed());
      byte s = 1;
      if (spellType == 6) {
         s = caster.getDeitySpell(defender);
      } else if (spellType == 7) {
         s = caster.getSorcerySpell(defender);
      }

      float damage = -100.0F;
      switch(s) {
         case 0:
            damage = casterRoll;
            if (casterRoll < 0.0F) {
               damage = -1.0F;
            }

            if (damage > 0.0F) {
               damage /= 2.0F;
               caster.setHealth(Math.min(100.0F, caster.getHealth() + damage));
               caster.setFavor(caster.getFavor() - 30.0F);
            }
            break;
         case 1:
            casterRoll = spellType == 6 ? caster.rollSkill(105, 106, caster.getAttackBuffed()) : caster.rollSkill(106, 100, caster.getAttackBuffed());
            damage = Math.min(casterRoll, casterRoll - defendRoll);
            if (casterRoll < 0.0F) {
               damage = -1.0F;
            } else if (defendRoll > casterRoll) {
               damage = 0.0F;
            }

            if (damage > 0.0F) {
               damage /= 2.0F;
               defender.setHealth(defender.getHealth() - damage);
               if (spellType == 6) {
                  caster.setFavor(caster.getFavor() - 20.0F);
               } else {
                  caster.setKarma(caster.getKarma() - 20.0F);
               }
            }
            break;
         case 2:
            damage = casterRoll;
            if (casterRoll < 0.0F) {
               damage = -1.0F;
            }

            if (damage > 0.0F) {
               caster.setPhysDefBuffed(damage / 50.0F);
               caster.setKarma(caster.getKarma() - 60.0F);
            }
            break;
         case 3:
            damage = casterRoll;
            if (casterRoll < 0.0F) {
               damage = -1.0F;
            }

            if (damage > 0.0F) {
               caster.setSpellDefBuffed(damage / 50.0F);
               caster.setKarma(caster.getKarma() - 60.0F);
            }
            break;
         case 4:
            damage = casterRoll;
            if (casterRoll < 0.0F) {
               damage = -1.0F;
            }

            if (damage > 0.0F) {
               caster.setAttackBuffed(damage / 50.0F);
               caster.setFavor(caster.getFavor() - 50.0F);
            }
      }

      byte[] spellData = ValreiConstants.getSpellData(caster.getEntityId(), defender.getEntityId(), s, damage);
      this.fightHistory.addAction(spellType, spellData);
   }

   private ValreiFight.ValreiFightHex[][] createFightMap() {
      ValreiFight.ValreiFightHex[][] toReturn = new ValreiFight.ValreiFightHex[7][7];

      for(int i = 0; i < 7; ++i) {
         for(int j = 0; j < 7; ++j) {
            toReturn[i][j] = new ValreiFight.ValreiFightHex(i, j);
            if (j + 1 < 4) {
               if (i >= 4 + j) {
                  toReturn[i][j].setModifier((short)-1);
               }
            } else if (j + 1 > 4 && i <= j - 7) {
               toReturn[i][j].setModifier((short)-1);
            }
         }
      }

      return toReturn;
   }

   private final boolean isMoveValid(ValreiFight.FightEntity e, int mapX, int mapY) {
      if (this.fightMap == null) {
         return false;
      } else if (mapX >= 0 && mapY >= 0 && mapX < 7 && mapY < 7) {
         if (this.fightMap[mapX][mapY].getModifier() == -1) {
            return false;
         } else {
            ValreiFight.FightEntity opponent = e == this.fighter1 ? this.fighter2 : this.fighter1;
            if (mapX == opponent.xPos && mapY == opponent.yPos) {
               return false;
            } else {
               return mapX != e.xPos || mapY != e.yPos;
            }
         }
      } else {
         return false;
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
      private float attackBuffed = 0.0F;
      private float physDefBuffed = 0.0F;
      private float spellDefBuffed = 0.0F;
      private EpicEntity entityBase;

      FightEntity(EpicEntity entity) {
         this.entityBase = entity;
         this.health = 100.0F;
         this.favor = 100.0F;
         this.karma = 100.0F;
      }

      public long getEntityId() {
         return this.entityBase.getId();
      }

      public String getEntityName() {
         return this.entityBase.getName();
      }

      public float rollInitiative() {
         float bodyCon = this.entityBase.getCurrentSkill(104);
         bodyCon += this.entityBase.getCurrentSkill(101) / 3.0F;
         return ValreiFight.this.fightRand.nextFloat() * 10.0F + bodyCon / 10.0F;
      }

      public float rollSkill(int skillId) {
         return this.rollSkill(skillId, -1);
      }

      public float rollSkill(int skillId, int bonusSkillId, float skillBuffed) {
         return this.rollSkill(skillId, bonusSkillId, 3.0F, skillBuffed);
      }

      public float rollSkill(int skillId, int bonusSkillId) {
         return this.rollSkill(skillId, bonusSkillId, 3.0F, 0.0F);
      }

      public float rollSkill(int skillId, int bonusSkillId, float bonusModifier, float skillBuffed) {
         EpicEntity.SkillVal skillValue = this.entityBase.getSkill(skillId);
         if (skillValue != null) {
            float actualVal = skillValue.getCurrentVal();
            EpicEntity.SkillVal bonusVal = this.entityBase.getSkill(bonusSkillId);
            if (bonusVal != null) {
               actualVal += bonusVal.getCurrentVal() / bonusModifier;
            }

            actualVal -= ValreiFight.this.fightRand.nextFloat() * 100.0F;
            if (skillBuffed > 0.0F) {
               actualVal += (100.0F - actualVal) * skillBuffed;
            }

            return actualVal;
         } else {
            return -100.0F;
         }
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

      public int getDistanceTo(ValreiFight.FightEntity other) {
         int totalDist = 0;
         ValreiFight.test1.setXY(this.xPos, this.yPos);
         ValreiFight.test2.setXY(other.xPos, other.yPos);

         for(; ValreiFight.test1.getX() != ValreiFight.test2.getX() || ValreiFight.test1.getY() != ValreiFight.test2.getY(); ++totalDist) {
            if (ValreiFight.test1.getY() != ValreiFight.test2.getY()) {
               int yDiff = 0;
               int xDiff = 0;
               byte var5;
               if (ValreiFight.test1.getY() < ValreiFight.test2.getY()) {
                  var5 = 1;
                  if (ValreiFight.test1.getX() < ValreiFight.test2.getX()) {
                     xDiff = 1;
                  }
               } else {
                  var5 = -1;
                  if (ValreiFight.test1.getX() > ValreiFight.test2.getX()) {
                     xDiff = -1;
                  }
               }

               ValreiFight.test1.setX(ValreiFight.test1.getX() + xDiff);
               ValreiFight.test1.setY(ValreiFight.test1.getY() + var5);
            } else {
               ValreiFight.test1.setX(ValreiFight.test1.getX() + (ValreiFight.test1.getX() < ValreiFight.test2.getX() ? 1 : -1));
            }
         }

         return totalDist;
      }

      public Point getTargetMove(boolean towards, ValreiFight.FightEntity other) {
         ValreiFight.test1.setXY(this.xPos, this.yPos);
         ValreiFight.test2.setXY(other.xPos, other.yPos);
         if (ValreiFight.test1.getY() != ValreiFight.test2.getY()) {
            if (towards) {
               int yDiff = 0;
               int xDiff = 0;
               byte var9;
               if (ValreiFight.test1.getY() < ValreiFight.test2.getY()) {
                  var9 = 1;
                  if (ValreiFight.test1.getX() < ValreiFight.test2.getX()) {
                     xDiff = 1;
                  }
               } else {
                  var9 = -1;
                  if (ValreiFight.test1.getX() > ValreiFight.test2.getX()) {
                     xDiff = -1;
                  }
               }

               return new Point(ValreiFight.test1.getX() + xDiff, ValreiFight.test1.getY() + var9);
            }

            int testDir = ValreiFight.this.fightRand.nextInt(3);

            for(int i = 0; i < 3; ++i) {
               int newX = ValreiFight.test1.getX();
               int newY = ValreiFight.test1.getY();
               switch(testDir) {
                  case 0:
                     newY += ValreiFight.test2.getY() > ValreiFight.test1.getY() ? -1 : (ValreiFight.test2.getY() == ValreiFight.test1.getY() ? 0 : 1);
                     if (newY != ValreiFight.test1.getY() && ValreiFight.this.isMoveValid(this, newX, newY)) {
                        return new Point(newX, newY);
                     }
                     break;
                  case 1:
                     newX += ValreiFight.test2.getX() > ValreiFight.test1.getX() ? -1 : (ValreiFight.test2.getX() == ValreiFight.test1.getX() ? 0 : 1);
                     if (newX != ValreiFight.test1.getX() && ValreiFight.this.isMoveValid(this, newX, newY)) {
                        return new Point(newX, newY);
                     }
                     break;
                  case 2:
                     if (ValreiFight.test2.getX() > ValreiFight.test1.getX() || ValreiFight.test2.getY() > ValreiFight.test1.getY()) {
                        --newX;
                        --newY;
                     } else if (ValreiFight.test2.getX() < ValreiFight.test1.getX() || ValreiFight.test2.getY() < ValreiFight.test1.getY()) {
                        ++newX;
                        ++newY;
                     }

                     if ((newX != ValreiFight.test1.getX() || newY != ValreiFight.test1.getY()) && ValreiFight.this.isMoveValid(this, newX, newY)) {
                        return new Point(newX, newY);
                     }
               }

               if (++testDir == 3) {
                  testDir = 0;
               }
            }

            testDir = ValreiFight.this.fightRand.nextInt(3);

            for(int i = 0; i < 3; ++i) {
               int newX = ValreiFight.test1.getX();
               int newY = ValreiFight.test1.getY();
               switch(testDir) {
                  case 0:
                     newY += ValreiFight.test2.getY() > ValreiFight.test1.getY() ? 1 : (ValreiFight.test2.getY() == ValreiFight.test1.getY() ? 0 : -1);
                     if (newY != ValreiFight.test1.getY() && ValreiFight.this.isMoveValid(this, newX, newY)) {
                        return new Point(newX, newY);
                     }
                     break;
                  case 1:
                     newX += ValreiFight.test2.getX() > ValreiFight.test1.getX() ? 1 : (ValreiFight.test2.getX() == ValreiFight.test1.getX() ? 0 : -1);
                     if (newX != ValreiFight.test1.getX() && ValreiFight.this.isMoveValid(this, newX, newY)) {
                        return new Point(newX, newY);
                     }
                     break;
                  case 2:
                     if (ValreiFight.test2.getX() > ValreiFight.test1.getX() || ValreiFight.test2.getY() > ValreiFight.test1.getY()) {
                        ++newX;
                        ++newY;
                     } else if (ValreiFight.test2.getX() < ValreiFight.test1.getX() || ValreiFight.test2.getY() < ValreiFight.test1.getY()) {
                        --newX;
                        --newY;
                     }

                     if ((newX != ValreiFight.test1.getX() || newY != ValreiFight.test1.getY()) && ValreiFight.this.isMoveValid(this, newX, newY)) {
                        return new Point(newX, newY);
                     }
               }

               if (++testDir == 3) {
                  testDir = 0;
               }
            }
         } else if (ValreiFight.test1.getX() != ValreiFight.test2.getX()) {
            return new Point(ValreiFight.test1.getX() + (ValreiFight.test1.getX() < ValreiFight.test2.getX() ? 1 : -1), ValreiFight.test1.getY());
         }

         return new Point(ValreiFight.test1.getX(), ValreiFight.test1.getY());
      }

      public short getPreferredAction() {
         float meleeAtk = this.entityBase.getCurrentSkill(102) + this.entityBase.getCurrentSkill(104) / 3.0F;
         float rangedAtk = this.entityBase.getCurrentSkill(104) + this.entityBase.getCurrentSkill(103) / 3.0F;
         float deitySpell = this.entityBase.getCurrentSkill(105) + this.entityBase.getCurrentSkill(106) / 3.0F;
         float sorcSpell = this.entityBase.getCurrentSkill(106) + this.entityBase.getCurrentSkill(100) / 3.0F;
         if (meleeAtk > deitySpell && meleeAtk > sorcSpell && meleeAtk > rangedAtk) {
            return 4;
         } else if (rangedAtk > deitySpell && rangedAtk > sorcSpell) {
            return 5;
         } else {
            return (short)(deitySpell > sorcSpell ? 6 : 7);
         }
      }

      public byte getDeitySpell(ValreiFight.FightEntity defender) {
         byte preferredType = 1;
         if (this.getFavor() >= 30.0F && this.getHealth() < 75.0F && defender.getHealth() > this.getHealth()) {
            preferredType = 0;
         } else if (this.getFavor() >= 50.0F
            && this.getAttackBuffed() == 0.0F
            && defender.entityBase.getCurrentSkill(101) > defender.entityBase.getCurrentSkill(103)) {
            preferredType = 4;
         }

         return preferredType;
      }

      public byte getSorcerySpell(ValreiFight.FightEntity defender) {
         byte preferredType = 1;
         if (this.getKarma() >= 60.0F && this.getHealth() < 75.0F && defender.getHealth() > this.getHealth()) {
            float attackHigh = Math.max(defender.entityBase.getCurrentSkill(102), defender.entityBase.getCurrentSkill(104));
            float spellHigh = Math.max(defender.entityBase.getCurrentSkill(105), defender.entityBase.getCurrentSkill(106));
            if (attackHigh > spellHigh && this.getPhysDefBuffed() == 0.0F) {
               preferredType = 2;
            } else if (this.getSpellDefBuffed() == 0.0F) {
               preferredType = 3;
            }
         }

         return preferredType;
      }
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
}

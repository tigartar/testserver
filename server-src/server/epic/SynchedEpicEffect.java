package com.wurmonline.server.epic;

import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SynchedEpicEffect {
   public static final int TYPE_SUMMON_CREATURES = 1;
   public static final int TYPE_EFFECT = 2;
   public static final int TYPE_EFFECT_CONTROLLER = 3;
   public static final int TYPE_EFFECT_DEAL_ITEM = 4;
   public static final int TYPE_EFFECT_DEAL_ITEM_FRAGMENT = 5;
   private final int type;
   private long deityNumber;
   private int creatureTemplateId;
   private int effectNumber;
   private int bonusEffectNum;
   private String eventString;
   private boolean resetKarma;
   private static final Logger logger = Logger.getLogger(SynchedEpicEffect.class.getName());

   public SynchedEpicEffect(int _type) {
      this.type = _type;
   }

   protected void run() {
      if (this.type == 1) {
         Effectuator.spawnOwnCreatures(this.deityNumber, this.creatureTemplateId, false);
      } else if (this.type == 2) {
         Effectuator.doEvent(this.effectNumber, this.deityNumber, this.creatureTemplateId, this.bonusEffectNum, this.eventString);
      } else if (this.type == 3) {
         Effectuator.setEffectController(this.getEffectNumber(), this.getDeityNumber());
      }

      if (this.isResetKarma()) {
         logger.log(Level.INFO, "Resetting scenario karma");
         PlayerInfoFactory.resetScenarioKarma();
      }
   }

   public long getDeityNumber() {
      return this.deityNumber;
   }

   public void setDeityNumber(long aDeityNumber) {
      this.deityNumber = aDeityNumber;
   }

   public int getCreatureTemplateId() {
      return this.creatureTemplateId;
   }

   public void setCreatureTemplateId(int aCreatureTemplateId) {
      this.creatureTemplateId = aCreatureTemplateId;
   }

   public int getEffectNumber() {
      return this.effectNumber;
   }

   public void setEffectNumber(int aEffectNumber) {
      this.effectNumber = aEffectNumber;
   }

   public int getBonusEffectNum() {
      return this.bonusEffectNum;
   }

   public void setBonusEffectNum(int aBonusEffectNum) {
      this.bonusEffectNum = aBonusEffectNum;
   }

   public String getEventString() {
      return this.eventString;
   }

   public void setEventString(String aEventString) {
      this.eventString = aEventString;
   }

   public final boolean isResetKarma() {
      return this.resetKarma;
   }

   public final void setResetKarma(boolean aResetKarma) {
      this.resetKarma = aResetKarma;
   }
}

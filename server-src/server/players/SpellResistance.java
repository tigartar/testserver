package com.wurmonline.server.players;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.SpellEffectsEnum;

public class SpellResistance {
   private static final float initialResistance = 0.5F;
   private float currentResistance = 0.0F;
   private int currentSecond = 0;
   private float customTickMod = 0.0F;
   private static final int secondsBetweenTicks = 7;
   private static final float tickModifier = 0.0117F;
   protected static final byte UTYPESTART = 1;
   protected static final byte UTYPESTOP = 0;
   protected static final byte UTYPEUPDATE = 2;
   private final short spellType;

   public SpellResistance(short spell) {
      this.spellType = spell;
   }

   public final short getSpellType() {
      return this.spellType;
   }

   public final boolean tickSecond(Communicator comm) {
      if (this.currentSecond++ == 7) {
         this.currentResistance -= this.customTickMod > 0.0F ? this.customTickMod : 0.0117F;
         this.currentSecond = 0;
         if (this.currentResistance <= 0.0F) {
            this.currentResistance = 0.0F;
            this.sendUpdateToClient(comm, (byte)0);
            return true;
         }

         this.sendUpdateToClient(comm, (byte)2);
      }

      return false;
   }

   private final int getSecondsLeft() {
      return (int)(this.currentResistance * 100.0F / ((this.customTickMod > 0.0F ? this.customTickMod : 0.0117F) * 100.0F) * 7.0F);
   }

   public final void setResistance() {
      this.currentResistance = 0.5F;
      this.currentSecond = 0;
   }

   public final void setResistance(float newRes, float tickModifier) {
      this.currentResistance = newRes;
      this.currentSecond = 0;
      this.customTickMod = tickModifier;
   }

   public final float getResistance() {
      return this.currentResistance;
   }

   public final void sendUpdateToClient(Communicator communicator, byte updateType) {
      switch(updateType) {
         case 0:
            communicator.sendRemoveSpellEffect(SpellEffectsEnum.getResistanceForSpell(this.spellType));
            break;
         case 1:
            communicator.sendAddStatusEffect(SpellEffectsEnum.getResistanceForSpell(this.spellType), this.getSecondsLeft());
            break;
         case 2:
         default:
            communicator.sendAddStatusEffect(SpellEffectsEnum.getResistanceForSpell(this.spellType), this.getSecondsLeft());
      }
   }
}

package com.wurmonline.server;

import com.wurmonline.server.creatures.Creature;

public enum PlonkData {
   FIRST_DAMAGE((short)1, 12),
   LOW_STAMINA((short)2, 13),
   THIRSTY((short)3, 15),
   HUNGRY((short)4, 14),
   FALL_DAMAGE((short)9, 16),
   DEATH((short)11, 18),
   SWIMMING((short)12, 17),
   ENCUMBERED((short)13, 20),
   ON_A_BOAT((short)14, 19),
   TREE_ACTIONS((short)16, 22),
   BOAT_SECURITY((short)48, 23);

   private final short plonkId;
   private final int flagBit;
   private final int flagColumn;

   private PlonkData(short _plonkId, int _flagBit, int _flagColumn) {
      this.plonkId = _plonkId;
      this.flagBit = _flagBit;
      this.flagColumn = _flagColumn;
   }

   private PlonkData(short _plonkId, int _flagBit) {
      this(_plonkId, _flagBit, 0);
   }

   public final short getPlonkId() {
      return this.plonkId;
   }

   public final int getFlagBit() {
      return this.flagBit;
   }

   public final int getFlagColumn() {
      return this.flagColumn;
   }

   public void trigger(Creature player) {
      if (player.isPlayer() && !player.hasFlag(this.getFlagBit())) {
         player.getCommunicator().sendPlonk(this.getPlonkId());
         player.setFlag(this.getFlagBit(), true);
      }
   }

   public final boolean hasSeenThis(Creature player) {
      return player.isPlayer() ? player.hasFlag(this.getFlagBit()) : true;
   }
}

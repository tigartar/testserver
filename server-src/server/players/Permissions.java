package com.wurmonline.server.players;

import java.util.BitSet;

public class Permissions {
   private int permissions = 0;
   protected BitSet permissionBits = new BitSet(32);

   public void setPermissionBits(int newPermissions) {
      this.permissions = newPermissions;
      this.permissionBits.clear();

      for(int x = 0; x < 32; ++x) {
         if ((newPermissions >>> x & 1) == 1) {
            this.permissionBits.set(x);
         }
      }
   }

   public final boolean hasPermission(int permissionBit) {
      return this.permissions != 0 ? this.permissionBits.get(permissionBit) : false;
   }

   private final int getPermissionsInt() {
      int ret = 0;

      for(int x = 0; x < 32; ++x) {
         if (this.permissionBits.get(x)) {
            ret = (int)((long)ret + (1L << x));
         }
      }

      return ret;
   }

   public final void setPermissionBit(int bit, boolean value) {
      this.permissionBits.set(bit, value);
      this.permissions = this.getPermissionsInt();
   }

   public int getPermissions() {
      return this.permissions;
   }

   public static enum Allow implements Permissions.IPermission {
      SETTLEMENT_MAY_MANAGE(0, "Allow Settlememnt to Manage", "Allow", "Manage", ""),
      NOT_RUNEABLE(7, "Item Attributes", "Cannot be", "Runed", ""),
      SEALED_BY_PLAYER(8, "Item Attributes", "Cannot", "Take / Put / Eat or Drink", ""),
      NO_EAT_OR_DRINK(9, "Item Attributes", "Cannot", "Eat or Drink", ""),
      OWNER_TURNABLE(10, "Item Attributes", "Turnable", "by Owner", ""),
      OWNER_MOVEABLE(11, "Item Attributes", "Moveable", "by Owner", ""),
      NO_DRAG(12, "Item Attributes", "Cannot be", "Dragged", ""),
      NO_IMPROVE(13, "Item Attributes", "Cannot be", "Improved", ""),
      NO_DROP(14, "Item Attributes", "Cannot be", "Dropped", ""),
      NO_REPAIR(15, "Item Attributes", "Cannot be", "Repaired", ""),
      PLANTED(16, "Item Attributes", "Is", "Planted", ""),
      AUTO_FILL(17, "Item Attributes", "Auto", "Fills", ""),
      AUTO_LIGHT(18, "Item Attributes", "Auto", "Lights", ""),
      ALWAYS_LIT(19, "Item Attributes", "Always", "Lit", ""),
      HAS_COURIER(20, "Item Attributes", "Has", "Courier", ""),
      HAS_DARK_MESSENGER(21, "Item Attributes", "Has", "Dark Messanger", ""),
      DECAY_DISABLED(22, "Item Attributes", "Decay", "Disabled", ""),
      NO_TAKE(23, "Item Attributes", "Cannot be", "Taken", ""),
      NO_SPELLS(24, "Item Restrictions", "Cannot be", "Cast Upon", ""),
      NO_BASH(25, "Item Restrictions", "Cannot be", "Bashed / Destroyed", ""),
      NOT_LOCKABLE(26, "Item Restrictions", "Cannot be", "Locked", ""),
      NOT_LOCKPICKABLE(27, "Item Restrictions", "Cannot be", "Lockpicked", ""),
      NOT_MOVEABLE(28, "Item Restrictions", "Cannot be", "Moved", ""),
      NOT_TURNABLE(29, "Item Restrictions", "Cannot be", "Turned", ""),
      NOT_PAINTABLE(30, "Item Restrictions", "Cannot be", "Painted", ""),
      NO_PUT(31, "Item Attributes", "Cannot", "Put items inside", "");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.Allow[] types = values();

      private Allow(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
         this.bit = (byte)aBit;
         this.description = aDescription;
         this.header1 = aHeader1;
         this.header2 = aHeader2;
         this.hover = aHover;
      }

      @Override
      public byte getBit() {
         return this.bit;
      }

      @Override
      public int getValue() {
         return 1 << this.bit;
      }

      @Override
      public String getDescription() {
         return this.description;
      }

      @Override
      public String getHeader1() {
         return this.header1;
      }

      @Override
      public String getHeader2() {
         return this.header2;
      }

      @Override
      public String getHover() {
         return this.hover;
      }

      public static Permissions.IPermission[] getPermissions() {
         return types;
      }
   }

   public interface IAllow {
      boolean canBeAlwaysLit();

      boolean canBeAutoFilled();

      boolean canBeAutoLit();

      boolean canBePeggedByPlayer();

      boolean canBePlanted();

      boolean canBeSealedByPlayer();

      boolean canChangeCreator();

      boolean canDisableDecay();

      boolean canDisableDestroy();

      boolean canDisableDrag();

      boolean canDisableDrop();

      boolean canDisableEatAndDrink();

      boolean canDisableImprove();

      boolean canDisableLocking();

      boolean canDisableLockpicking();

      boolean canDisableMoveable();

      boolean canDisableOwnerMoveing();

      boolean canDisableOwnerTurning();

      boolean canDisablePainting();

      boolean canDisablePut();

      boolean canDisableRepair();

      boolean canDisableRuneing();

      boolean canDisableSpellTarget();

      boolean canDisableTake();

      boolean canDisableTurning();

      boolean canHaveCourier();

      boolean canHaveDakrMessenger();

      String getCreatorName();

      float getDamage();

      String getName();

      float getQualityLevel();

      boolean hasCourier();

      boolean hasDarkMessenger();

      boolean hasNoDecay();

      boolean isAlwaysLit();

      boolean isAutoFilled();

      boolean isAutoLit();

      boolean isIndestructible();

      boolean isNoDrag();

      boolean isNoDrop();

      boolean isNoEatOrDrink();

      boolean isNoImprove();

      boolean isNoMove();

      boolean isNoPut();

      boolean isNoRepair();

      boolean isNoTake();

      boolean isNotLockable();

      boolean isNotLockpickable();

      boolean isNotPaintable();

      boolean isNotRuneable();

      boolean isNotSpellTarget();

      boolean isNotTurnable();

      boolean isOwnerMoveable();

      boolean isOwnerTurnable();

      boolean isPlanted();

      boolean isSealedByPlayer();

      void setCreator(String var1);

      boolean setDamage(float var1);

      void setHasCourier(boolean var1);

      void setHasDarkMessenger(boolean var1);

      void setHasNoDecay(boolean var1);

      void setIsAlwaysLit(boolean var1);

      void setIsAutoFilled(boolean var1);

      void setIsAutoLit(boolean var1);

      void setIsIndestructible(boolean var1);

      void setIsNoDrag(boolean var1);

      void setIsNoDrop(boolean var1);

      void setIsNoEatOrDrink(boolean var1);

      void setIsNoImprove(boolean var1);

      void setIsNoMove(boolean var1);

      void setIsNoPut(boolean var1);

      void setIsNoRepair(boolean var1);

      void setIsNoTake(boolean var1);

      void setIsNotLockable(boolean var1);

      void setIsNotLockpickable(boolean var1);

      void setIsNotPaintable(boolean var1);

      void setIsNotRuneable(boolean var1);

      void setIsNotSpellTarget(boolean var1);

      void setIsNotTurnable(boolean var1);

      void setIsOwnerMoveable(boolean var1);

      void setIsOwnerTurnable(boolean var1);

      void setIsPlanted(boolean var1);

      void setIsSealedByPlayer(boolean var1);

      boolean setQualityLevel(float var1);

      void setOriginalQualityLevel(float var1);

      void savePermissions();
   }

   public interface IPermission {
      byte getBit();

      int getValue();

      String getDescription();

      String getHeader1();

      String getHeader2();

      String getHover();
   }
}

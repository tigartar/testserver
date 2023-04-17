/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import java.util.BitSet;

public class Permissions {
    private int permissions = 0;
    protected BitSet permissionBits = new BitSet(32);

    public void setPermissionBits(int newPermissions) {
        this.permissions = newPermissions;
        this.permissionBits.clear();
        for (int x = 0; x < 32; ++x) {
            if ((newPermissions >>> x & 1) != 1) continue;
            this.permissionBits.set(x);
        }
    }

    public final boolean hasPermission(int permissionBit) {
        if (this.permissions != 0) {
            return this.permissionBits.get(permissionBit);
        }
        return false;
    }

    private final int getPermissionsInt() {
        int ret = 0;
        for (int x = 0; x < 32; ++x) {
            if (!this.permissionBits.get(x)) continue;
            ret = (int)((long)ret + (1L << x));
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

    public static enum Allow implements IPermission
    {
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
        private static final Allow[] types;

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

        public static IPermission[] getPermissions() {
            return types;
        }

        static {
            types = Allow.values();
        }
    }

    public static interface IAllow {
        public boolean canBeAlwaysLit();

        public boolean canBeAutoFilled();

        public boolean canBeAutoLit();

        public boolean canBePeggedByPlayer();

        public boolean canBePlanted();

        public boolean canBeSealedByPlayer();

        public boolean canChangeCreator();

        public boolean canDisableDecay();

        public boolean canDisableDestroy();

        public boolean canDisableDrag();

        public boolean canDisableDrop();

        public boolean canDisableEatAndDrink();

        public boolean canDisableImprove();

        public boolean canDisableLocking();

        public boolean canDisableLockpicking();

        public boolean canDisableMoveable();

        public boolean canDisableOwnerMoveing();

        public boolean canDisableOwnerTurning();

        public boolean canDisablePainting();

        public boolean canDisablePut();

        public boolean canDisableRepair();

        public boolean canDisableRuneing();

        public boolean canDisableSpellTarget();

        public boolean canDisableTake();

        public boolean canDisableTurning();

        public boolean canHaveCourier();

        public boolean canHaveDakrMessenger();

        public String getCreatorName();

        public float getDamage();

        public String getName();

        public float getQualityLevel();

        public boolean hasCourier();

        public boolean hasDarkMessenger();

        public boolean hasNoDecay();

        public boolean isAlwaysLit();

        public boolean isAutoFilled();

        public boolean isAutoLit();

        public boolean isIndestructible();

        public boolean isNoDrag();

        public boolean isNoDrop();

        public boolean isNoEatOrDrink();

        public boolean isNoImprove();

        public boolean isNoMove();

        public boolean isNoPut();

        public boolean isNoRepair();

        public boolean isNoTake();

        public boolean isNotLockable();

        public boolean isNotLockpickable();

        public boolean isNotPaintable();

        public boolean isNotRuneable();

        public boolean isNotSpellTarget();

        public boolean isNotTurnable();

        public boolean isOwnerMoveable();

        public boolean isOwnerTurnable();

        public boolean isPlanted();

        public boolean isSealedByPlayer();

        public void setCreator(String var1);

        public boolean setDamage(float var1);

        public void setHasCourier(boolean var1);

        public void setHasDarkMessenger(boolean var1);

        public void setHasNoDecay(boolean var1);

        public void setIsAlwaysLit(boolean var1);

        public void setIsAutoFilled(boolean var1);

        public void setIsAutoLit(boolean var1);

        public void setIsIndestructible(boolean var1);

        public void setIsNoDrag(boolean var1);

        public void setIsNoDrop(boolean var1);

        public void setIsNoEatOrDrink(boolean var1);

        public void setIsNoImprove(boolean var1);

        public void setIsNoMove(boolean var1);

        public void setIsNoPut(boolean var1);

        public void setIsNoRepair(boolean var1);

        public void setIsNoTake(boolean var1);

        public void setIsNotLockable(boolean var1);

        public void setIsNotLockpickable(boolean var1);

        public void setIsNotPaintable(boolean var1);

        public void setIsNotRuneable(boolean var1);

        public void setIsNotSpellTarget(boolean var1);

        public void setIsNotTurnable(boolean var1);

        public void setIsOwnerMoveable(boolean var1);

        public void setIsOwnerTurnable(boolean var1);

        public void setIsPlanted(boolean var1);

        public void setIsSealedByPlayer(boolean var1);

        public boolean setQualityLevel(float var1);

        public void setOriginalQualityLevel(float var1);

        public void savePermissions();
    }

    public static interface IPermission {
        public byte getBit();

        public int getValue();

        public String getDescription();

        public String getHeader1();

        public String getHeader2();

        public String getHover();
    }
}


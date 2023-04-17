/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.ActionTypes;
import com.wurmonline.server.creatures.Creature;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class ActionEntry
implements ActionTypes,
MiscConstants,
Comparable<ActionEntry> {
    private static final Logger logger = Logger.getLogger(ActionEntry.class.getName());
    private String actionString;
    private final String verbString;
    private final short number;
    private boolean quickSkillLess = false;
    private final int maxRange;
    private final int prio;
    private String animationString;
    private boolean needsFood = false;
    private boolean isSpell = false;
    private boolean isOffensive = false;
    private boolean isFatigue = false;
    private boolean isPoliced = false;
    private boolean isNoMove = false;
    private boolean isNonLibila = false;
    private boolean isNonWhiteReligion = false;
    private boolean isNonReligion = false;
    private boolean isAttackHigh = false;
    private boolean isAttackLow = false;
    private boolean isAttackLeft = false;
    private boolean isAttackRight = false;
    private boolean isDefend = false;
    private boolean isStanceChange = false;
    private boolean isAllowFo = false;
    private boolean isAllowFoOnSurface = false;
    private boolean isAllowMagranon = false;
    private boolean isAllowMagranonInCave = false;
    private boolean isAllowVynora = false;
    private boolean isAllowVynoraOnSurface = false;
    private boolean isAllowLibila = false;
    private boolean isAllowLibilaInCave = false;
    private boolean isOpportunity = true;
    private boolean ignoresRange = false;
    private boolean vulnerable = false;
    private boolean isMission = false;
    private boolean notVulnerable = false;
    private boolean isBlockedByUseOnGroundOnly = true;
    private boolean usesNewSkillSystem = false;
    private boolean isVerifiedNewSystem = false;
    private boolean showOnSelectBar = false;
    private int blockType = 4;
    private boolean sameBridgeOnly = false;
    private boolean isPerimeterAction = false;
    private boolean isCornerAction = false;
    private boolean isEnemyNever = false;
    private boolean isEnemyAlways = false;
    private boolean isEnemyNoGuards = false;
    private boolean useItemOnGround = false;
    private boolean stackable = true;
    private boolean stackableFight = true;
    private byte requiresActiveItem = 0;

    ActionEntry(short aNumber, int aPriority, String aActionString, String aVerbString, String aAnimationString, @Nullable int[] aTypes, int aRange, boolean blockedByUseOnGroundOnly) {
        this.prio = aPriority;
        this.number = aNumber;
        this.actionString = aActionString;
        this.verbString = aVerbString;
        this.animationString = aAnimationString.toLowerCase().replace(" ", "");
        this.maxRange = aRange;
        this.isBlockedByUseOnGroundOnly = blockedByUseOnGroundOnly;
        this.assignTypes(aTypes);
    }

    ActionEntry(short aNumber, int aPriority, String aActionString, String aVerbString, @Nullable int[] aTypes, int aRange, boolean blockedByUseOnGroundOnly) {
        this(aNumber, aPriority, aActionString, aVerbString, aActionString, aTypes, aRange, blockedByUseOnGroundOnly);
    }

    ActionEntry(short aNumber, String aActionString, String aVerbString, int[] aTypes, int aRange) {
        this(aNumber, aActionString, aVerbString, aActionString, aTypes, aRange);
    }

    ActionEntry(short aNumber, String aActionString, String aVerbString, String aAnimationString, int[] aTypes, int aRange) {
        this(aNumber, 5, aActionString, aVerbString, aAnimationString, aTypes, aRange, true);
    }

    ActionEntry(short aNumber, String aActionString, String aVerbString, int[] aTypes, int aRange, boolean blockedByUseOnGround) {
        this(aNumber, 5, aActionString, aVerbString, aActionString, aTypes, aRange, blockedByUseOnGround);
    }

    ActionEntry(short aNumber, int aPriority, String aActionString, String aVerbString, int[] aTypes) {
        this(aNumber, aPriority, aActionString, aVerbString, aActionString, aTypes, 4, true);
    }

    ActionEntry(short aNumber, int aPriority, String aActionString, String aVerbString, int[] aTypes, boolean blockedByUseOnGround) {
        this(aNumber, aPriority, aActionString, aVerbString, aActionString, aTypes, 4, blockedByUseOnGround);
    }

    ActionEntry(short aNumber, String aActionString, String aVerbString, @Nullable int[] aTypes) {
        this(aNumber, aActionString, aVerbString, aActionString, aTypes);
    }

    ActionEntry(short aNumber, String aActionString, String aVerbString, String aAnimationString, @Nullable int[] aTypes) {
        this(aNumber, 5, aActionString, aVerbString, aAnimationString, aTypes, 4, true);
    }

    ActionEntry(short aNumber, String aActionString, String aVerbString, @Nullable int[] aTypes, boolean blockedByUseOnGround) {
        this(aNumber, 5, aActionString, aVerbString, aActionString, aTypes, 4, blockedByUseOnGround);
    }

    public ActionEntry(short aNumber, String aActionString, String aVerbString) {
        this(aNumber, aActionString, aVerbString, null);
    }

    public static ActionEntry createEntry(short number, String actionString, String verb, int[] types) {
        return new ActionEntry(number, actionString, verb, types, 2);
    }

    private final void assignTypes(@Nullable int[] types) {
        if (types == null) {
            return;
        }
        block53: for (int x = 0; x < types.length; ++x) {
            switch (types[x]) {
                case 0: {
                    this.quickSkillLess = true;
                    this.isOpportunity = false;
                    continue block53;
                }
                case 1: {
                    this.needsFood = true;
                    continue block53;
                }
                case 2: {
                    this.isSpell = true;
                    continue block53;
                }
                case 3: {
                    this.isOffensive = true;
                    continue block53;
                }
                case 4: {
                    this.isFatigue = true;
                    continue block53;
                }
                case 5: {
                    this.isPoliced = true;
                    continue block53;
                }
                case 6: {
                    this.isNoMove = true;
                    continue block53;
                }
                case 7: {
                    this.setIsNonLibila(true);
                    continue block53;
                }
                case 8: {
                    this.setIsNonWhiteReligion(true);
                    continue block53;
                }
                case 9: {
                    this.setIsNonReligion(true);
                    continue block53;
                }
                case 12: {
                    this.isAttackHigh = true;
                    continue block53;
                }
                case 13: {
                    this.isAttackLow = true;
                    continue block53;
                }
                case 14: {
                    this.isAttackLeft = true;
                    continue block53;
                }
                case 15: {
                    this.isAttackRight = true;
                    continue block53;
                }
                case 16: {
                    this.isOpportunity = false;
                    this.isDefend = true;
                    this.ignoresRange = true;
                    continue block53;
                }
                case 17: {
                    this.isOpportunity = false;
                    this.isStanceChange = true;
                    this.ignoresRange = true;
                    continue block53;
                }
                case 18: {
                    this.setAllowMagranon(true);
                    continue block53;
                }
                case 38: {
                    this.setAllowMagranonInCave(true);
                    continue block53;
                }
                case 19: {
                    this.setAllowFo(true);
                    continue block53;
                }
                case 39: {
                    this.setAllowFoOnSurface(true);
                    continue block53;
                }
                case 20: {
                    this.setAllowVynora(true);
                    continue block53;
                }
                case 52: {
                    this.setAllowVynoraOnSurface(true);
                    continue block53;
                }
                case 21: {
                    this.setAllowLibila(true);
                    continue block53;
                }
                case 40: {
                    this.setAllowLibilaInCave(true);
                    continue block53;
                }
                case 22: {
                    this.isOpportunity = false;
                    continue block53;
                }
                case 23: {
                    this.blockType = 0;
                    this.ignoresRange = true;
                    continue block53;
                }
                case 43: {
                    this.showOnSelectBar = true;
                    continue block53;
                }
                case 24: {
                    this.vulnerable = true;
                    continue block53;
                }
                case 25: {
                    this.isMission = true;
                    continue block53;
                }
                case 26: {
                    this.notVulnerable = true;
                    continue block53;
                }
                case 27: {
                    this.stackable = false;
                    continue block53;
                }
                case 28: {
                    this.stackableFight = false;
                    continue block53;
                }
                case 29: {
                    this.blockType = 0;
                    continue block53;
                }
                case 30: {
                    this.blockType = 1;
                    continue block53;
                }
                case 31: {
                    this.blockType = 2;
                    continue block53;
                }
                case 32: {
                    this.blockType = 3;
                    continue block53;
                }
                case 33: {
                    this.blockType = 5;
                    continue block53;
                }
                case 34: {
                    this.blockType = 7;
                    continue block53;
                }
                case 35: {
                    this.requiresActiveItem = 0;
                    continue block53;
                }
                case 36: {
                    this.requiresActiveItem = 1;
                    continue block53;
                }
                case 37: {
                    this.requiresActiveItem = (byte)2;
                    continue block53;
                }
                case 50: {
                    this.blockType = 8;
                    continue block53;
                }
                case 44: {
                    this.setSameBridgeOnly(true);
                    continue block53;
                }
                case 45: {
                    this.isPerimeterAction = true;
                    continue block53;
                }
                case 46: {
                    this.isCornerAction = true;
                    continue block53;
                }
                case 47: {
                    this.isEnemyNever = true;
                    continue block53;
                }
                case 48: {
                    this.isEnemyAlways = true;
                    continue block53;
                }
                case 49: {
                    this.isEnemyNoGuards = true;
                    continue block53;
                }
                case 51: {
                    this.useItemOnGround = true;
                    continue block53;
                }
                case 41: {
                    this.usesNewSkillSystem = true;
                    continue block53;
                }
                case 42: {
                    this.isVerifiedNewSystem = true;
                    continue block53;
                }
                default: {
                    logger.warning("Unexepected ActionType: " + types[x] + " in " + this);
                }
            }
        }
    }

    public final String getActionString() {
        return this.actionString;
    }

    public final String getAnimationString() {
        return this.animationString;
    }

    public final int getRange() {
        return this.maxRange;
    }

    public final short getNumber() {
        return this.number;
    }

    public final String getVerbString() {
        return this.verbString;
    }

    public final String getVerbStartString() {
        if (this.verbString.toLowerCase().startsWith("stop ") || this.verbString.toLowerCase().startsWith("start ")) {
            return this.verbString;
        }
        return "start " + this.verbString;
    }

    public final String getVerbFinishString() {
        if (this.verbString.toLowerCase().startsWith("stop ") || this.verbString.toLowerCase().startsWith("start ")) {
            return this.verbString;
        }
        return "finish " + this.verbString;
    }

    public final boolean isQuickSkillLess() {
        return this.quickSkillLess;
    }

    public final boolean isStackable() {
        return this.stackable;
    }

    public final boolean isStackableFight() {
        return this.stackableFight;
    }

    final boolean isSpell() {
        return this.isSpell;
    }

    final boolean needsFood() {
        return this.needsFood;
    }

    final boolean isFatigue() {
        return this.isFatigue;
    }

    final boolean isBlockedByUseOnGroundOnly() {
        return this.isBlockedByUseOnGroundOnly;
    }

    final boolean isPoliced() {
        return this.isPoliced;
    }

    final boolean isNoMove() {
        return this.isNoMove;
    }

    public final boolean isNonLibila() {
        return this.isNonLibila;
    }

    public final boolean isNonWhiteReligion() {
        return this.isNonWhiteReligion;
    }

    public final boolean isNonReligion() {
        return this.isNonReligion;
    }

    public final boolean isDefend() {
        return this.isDefend;
    }

    public final boolean isStanceChange() {
        return this.isStanceChange;
    }

    final boolean isOpportunity() {
        return this.isOpportunity;
    }

    public final boolean isAttackHigh() {
        return this.isAttackHigh;
    }

    public final boolean isAttackLow() {
        return this.isAttackLow;
    }

    public final boolean isAttackLeft() {
        return this.isAttackLeft;
    }

    public final boolean isAttackRight() {
        return this.isAttackRight;
    }

    final boolean isIgnoresRange() {
        return this.ignoresRange;
    }

    public final boolean isOffensive() {
        return this.isOffensive;
    }

    public final boolean isMission() {
        return this.isMission;
    }

    public final boolean isShowOnSelectBar() {
        return this.showOnSelectBar;
    }

    public final boolean isVulnerable() {
        if (this.notVulnerable) {
            return false;
        }
        if (this.isSpell) {
            return true;
        }
        if (this.isOffensive) {
            return this.vulnerable;
        }
        return true;
    }

    public final boolean canUseNewSkillSystem() {
        return this.usesNewSkillSystem && (this.isVerifiedNewSystem || Features.Feature.NEW_SKILL_SYSTEM.isEnabled());
    }

    public final int getPriority() {
        return this.prio;
    }

    public final int getBlockType() {
        return this.blockType;
    }

    public final byte getUseActiveItem() {
        return this.requiresActiveItem;
    }

    public boolean isAllowed(Creature creature) {
        if (creature.getDeity() != null) {
            if (creature.getDeity().isWaterGod()) {
                if (creature.isOnSurface() && this.isAllowVynoraOnSurface()) {
                    return true;
                }
                if (this.isAllowVynora()) {
                    return true;
                }
            }
            if (creature.getDeity().isMountainGod()) {
                if (!creature.isOnSurface() && this.isAllowMagranonInCave()) {
                    return true;
                }
                if (this.isAllowMagranon()) {
                    return true;
                }
            }
            if (creature.getDeity().isForestGod()) {
                if (creature.isOnSurface() && this.isAllowFoOnSurface()) {
                    return true;
                }
                if (this.isAllowFo()) {
                    return true;
                }
            }
            if (creature.getDeity().isHateGod()) {
                if (!creature.isOnSurface() && this.isAllowLibilaInCave()) {
                    return true;
                }
                if (this.isAllowLibila()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int compareTo(ActionEntry aActionEntry) {
        return this.getActionString().compareTo(aActionEntry.getActionString());
    }

    public String toString() {
        StringBuilder lBuilder = new StringBuilder();
        lBuilder.append("ActionEntry [");
        lBuilder.append("Number: ").append(this.number);
        lBuilder.append(", Action String: ").append(this.actionString);
        lBuilder.append(", Verb String: ").append(this.verbString);
        lBuilder.append(", Range: ").append(this.maxRange);
        lBuilder.append(", Priority: ").append(this.prio);
        lBuilder.append(", UseActiveItem: ").append(this.requiresActiveItem);
        lBuilder.append(']');
        return lBuilder.toString();
    }

    public boolean isSameBridgeOnly() {
        return this.sameBridgeOnly;
    }

    public void setSameBridgeOnly(boolean aSameBridgeOnly) {
        this.sameBridgeOnly = aSameBridgeOnly;
    }

    public boolean isPerimeterAction() {
        return this.isPerimeterAction;
    }

    public boolean isUseItemOnGroundAction() {
        return this.useItemOnGround;
    }

    public boolean isCornerAction() {
        return this.isCornerAction;
    }

    public boolean isEnemyNeverAllowed() {
        return this.isEnemyNever;
    }

    public boolean isEnemyAlwaysAllowed() {
        return this.isEnemyAlways;
    }

    public boolean isEnemyAllowedWhenNoGuards() {
        return this.isEnemyNoGuards;
    }

    public void setIsNonReligion(boolean nonReligion) {
        this.isNonReligion = nonReligion;
    }

    public void setIsNonWhiteReligion(boolean nonWhiteReligion) {
        this.isNonWhiteReligion = nonWhiteReligion;
    }

    public void setIsNonLibila(boolean nonLibila) {
        this.isNonLibila = nonLibila;
    }

    public boolean isAllowFo() {
        return this.isAllowFo;
    }

    public void setAllowFo(boolean allowFo) {
        this.isAllowFo = allowFo;
    }

    public boolean isAllowFoOnSurface() {
        return this.isAllowFoOnSurface;
    }

    public void setAllowFoOnSurface(boolean allowFoOnSurface) {
        this.isAllowFoOnSurface = allowFoOnSurface;
    }

    public boolean isAllowMagranon() {
        return this.isAllowMagranon;
    }

    public void setAllowMagranon(boolean allowMagranon) {
        this.isAllowMagranon = allowMagranon;
    }

    public boolean isAllowMagranonInCave() {
        return this.isAllowMagranonInCave;
    }

    public void setAllowMagranonInCave(boolean allowMagranonInCave) {
        this.isAllowMagranonInCave = allowMagranonInCave;
    }

    public boolean isAllowVynora() {
        return this.isAllowVynora;
    }

    public void setAllowVynora(boolean allowVynora) {
        this.isAllowVynora = allowVynora;
    }

    public boolean isAllowVynoraOnSurface() {
        return this.isAllowVynoraOnSurface;
    }

    public void setAllowVynoraOnSurface(boolean allowVynoraOnSurface) {
        this.isAllowVynoraOnSurface = allowVynoraOnSurface;
    }

    public boolean isAllowLibila() {
        return this.isAllowLibila;
    }

    public void setAllowLibila(boolean allowLibila) {
        this.isAllowLibila = allowLibila;
    }

    public boolean isAllowLibilaInCave() {
        return this.isAllowLibilaInCave;
    }

    public void setAllowLibilaInCave(boolean allowLibilaInCave) {
        this.isAllowLibilaInCave = allowLibilaInCave;
    }
}


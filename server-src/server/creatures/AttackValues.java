/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

public final class AttackValues {
    private final float baseDamage;
    private final int attackReach;
    private final int weightGroup;
    private final float criticalChance;
    private final float baseSpeed;
    private final byte damageType;
    private final boolean usesWeapon;
    private final int rounds;
    private final float waitUntilNextAttack;

    public AttackValues(float baseDamage, float criticalChance, float baseSpeed, int attackReach, int weightGroup, byte damageType, boolean usesWeapon, int rounds, float waitUntilNextAttack) {
        this.baseDamage = baseDamage;
        this.criticalChance = criticalChance;
        this.baseSpeed = baseSpeed;
        this.attackReach = attackReach;
        this.weightGroup = weightGroup;
        this.damageType = damageType;
        this.usesWeapon = usesWeapon;
        this.rounds = rounds;
        this.waitUntilNextAttack = waitUntilNextAttack;
    }

    public final float getBaseDamage() {
        return this.baseDamage;
    }

    public final float getCriticalChance() {
        return this.criticalChance;
    }

    public final int getRounds() {
        return this.rounds;
    }

    public final float getBaseSpeed() {
        return this.baseSpeed;
    }

    public final int getAttackReach() {
        return this.attackReach;
    }

    public final int getWeightGroup() {
        return this.weightGroup;
    }

    public final byte getDamageType() {
        return this.damageType;
    }

    public final boolean isUsingWeapon() {
        return this.usesWeapon;
    }

    public final float getWaitTime() {
        return this.waitUntilNextAttack;
    }
}


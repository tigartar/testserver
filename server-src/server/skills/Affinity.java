/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

public class Affinity {
    public int skillNumber;
    public int number;

    public Affinity(int skillnum, int _number) {
        this.number = _number;
        this.skillNumber = skillnum;
    }

    public int getSkillNumber() {
        return this.skillNumber;
    }

    public void setSkillNumber(int aSkillNumber) {
        this.skillNumber = aSkillNumber;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int aNumber) {
        this.number = aNumber;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.skills.DbSkills;
import com.wurmonline.server.skills.Skills;

public final class SkillsFactory {
    private SkillsFactory() {
    }

    public static Skills createSkills(long id) {
        return new DbSkills(id);
    }

    public static Skills createSkills(String templateName) {
        return new DbSkills(templateName);
    }
}


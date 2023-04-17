/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.items.NoSpaceException;
import javax.annotation.Nullable;

public class TestDummyAI
extends CreatureAI {
    @Override
    protected boolean pollMovement(Creature c, long delta) {
        return false;
    }

    @Override
    protected boolean pollAttack(Creature c, long delta) {
        return false;
    }

    @Override
    protected boolean pollBreeding(Creature c, long delta) {
        return false;
    }

    @Override
    public CreatureAIData createCreatureAIData() {
        return new TestDummyAIData();
    }

    @Override
    public void creatureCreated(Creature c) {
    }

    @Override
    public double receivedWound(Creature c, @Nullable Creature performer, byte dmgType, int dmgPosition, float armourMod, double damage) {
        if (performer != null) {
            try {
                String message = "You dealt " + String.format("%.2f", damage / 65535.0 * 100.0) + " to " + c.getBody().getBodyPart(dmgPosition).getName() + " of type " + Wound.getName(dmgType) + ".";
                performer.getCommunicator().sendNormalServerMessage(message);
            }
            catch (NoSpaceException noSpaceException) {
                // empty catch block
            }
        }
        return 0.0;
    }

    public class TestDummyAIData
    extends CreatureAIData {
    }
}


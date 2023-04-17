/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import java.util.Properties;
import java.util.logging.Logger;

public class GMForceSpawnRiftLootQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(GMForceSpawnRiftLootQuestion.class.getName());

    public GMForceSpawnRiftLootQuestion(Creature aResponder) {
        super(aResponder, "Spawn Rift Loot", "Which item would you like to spawn?", 144, aResponder.getWurmId());
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
    }

    @Override
    public void sendQuestion() {
    }
}


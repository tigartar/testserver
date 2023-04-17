/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.math.Vector2f;
import com.wurmonline.server.creatures.ai.CreatureAIData;

public class GenericCreatureAIData
extends CreatureAIData {
    private boolean freezeMovement = false;
    private float randomMovementChance = 0.01f;
    private boolean prefersPlayers = false;
    private float prefersPlayersModifier = 2.0f;
    private boolean hasTether = false;
    private int tetherX = -1;
    private int tetherY = -1;
    private int tetherDistance = -1;

    public GenericCreatureAIData() {
    }

    public GenericCreatureAIData(boolean prefersPlayers, float movementChance) {
        this.prefersPlayers = prefersPlayers;
        this.randomMovementChance = movementChance;
    }

    public GenericCreatureAIData(boolean prefersPlayers, float movementChance, int tetherDistance) {
        this(prefersPlayers, movementChance);
        this.tetherDistance = tetherDistance;
        if (tetherDistance > 0) {
            this.hasTether = true;
        }
    }

    public boolean isMovementFrozen() {
        return this.freezeMovement;
    }

    public void setMovementFrozen(boolean frozen) {
        this.freezeMovement = frozen;
    }

    public float getRandomMovementChance() {
        return this.randomMovementChance;
    }

    public void setRandomMovementChance(float newChance) {
        if (newChance > 1.0f || newChance < 0.0f) {
            newChance = Math.max(0.0f, Math.min(1.0f, newChance));
        }
        this.randomMovementChance = newChance;
    }

    public boolean doesPreferPlayers() {
        return this.prefersPlayers;
    }

    public void setPrefersPlayers(boolean doesPreferPlayers) {
        this.prefersPlayers = doesPreferPlayers;
    }

    public float getPrefersPlayersModifier() {
        return this.prefersPlayersModifier;
    }

    public void setPrefersPlayersModifier(float prefersPlayersModifier) {
        this.prefersPlayersModifier = prefersPlayersModifier;
    }

    public boolean hasTether() {
        return this.hasTether;
    }

    public void setTether(boolean shouldHaveTether) {
        this.hasTether = shouldHaveTether;
    }

    public void setTether(int tileX, int tileY) {
        if (tileX > 0 && tileY > 0) {
            this.hasTether = true;
        }
        this.tetherX = tileX;
        this.tetherY = tileY;
    }

    public int getTetherX() {
        return this.tetherX;
    }

    public int getTetherY() {
        return this.tetherY;
    }

    public Vector2f getTetherPos() {
        return new Vector2f(this.tetherX, this.tetherY);
    }

    public void setTetherDistance(int newDistance) {
        this.tetherDistance = newDistance;
    }

    public int getTetherDistance() {
        return this.tetherDistance;
    }
}


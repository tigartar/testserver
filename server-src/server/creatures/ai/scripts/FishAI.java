/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.behaviours.FishEnums;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;

public class FishAI
extends CreatureAI {
    @Override
    protected boolean pollMovement(Creature c, long delta) {
        FishAIData aiData = (FishAIData)c.getCreatureAIData();
        float targetX = aiData.getTargetPosX();
        float targetY = aiData.getTargetPosY();
        if (targetX < 0.0f || targetY < 0.0f) {
            return false;
        }
        if (c.getPosX() != targetX || c.getPosY() != targetY) {
            float movementSpeed;
            float diffY;
            float diffX = c.getPosX() - targetX;
            float totalDiff = (float)Math.sqrt(diffX * diffX + (diffY = c.getPosY() - targetY) * diffY);
            if (totalDiff < (movementSpeed = aiData.getSpeed() * aiData.getMovementSpeedModifier())) {
                movementSpeed = totalDiff;
            }
            double lRotation = Math.atan2(targetY - c.getPosY(), targetX - c.getPosX()) * 57.29577951308232 + 90.0;
            float lXPosMod = (float)Math.sin(lRotation * 0.01745329238474369) * movementSpeed;
            float lYPosMod = -((float)Math.cos(lRotation * 0.01745329238474369)) * movementSpeed;
            int lNewTileX = (int)(c.getPosX() + lXPosMod) >> 2;
            int lNewTileY = (int)(c.getPosY() + lYPosMod) >> 2;
            int lDiffTileX = lNewTileX - c.getTileX();
            int lDiffTileY = lNewTileY - c.getTileY();
            c.setPositionX(c.getPosX() + lXPosMod);
            c.setPositionY(c.getPosY() + lYPosMod);
            c.setRotation((float)lRotation);
            try {
                float minZ = Math.min(-0.1f, Zones.calculateHeight(c.getPosX(), c.getPosY(), c.isOnSurface()));
                if (c.getPositionZ() < minZ) {
                    c.setPositionZ(minZ + Math.abs(minZ * 0.2f));
                } else if (c.getPositionZ() < minZ * 0.15f) {
                    c.setPositionZ(minZ * 0.15f);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            c.moved(lXPosMod, lYPosMod, 0.0f, lDiffTileX, lDiffTileY);
        }
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
        return new FishAIData();
    }

    @Override
    public void creatureCreated(Creature c) {
    }

    public class FishAIData
    extends CreatureAIData {
        private byte fishTypeId = 0;
        private double ql = 10.0;
        private float qlperc = 1.0f;
        private int weight = 0;
        private float targetPosX = -1.0f;
        private float targetPosY = -1.0f;
        private float timeToTarget = 0.0f;
        private float bodyStrength = 1.0f;
        private float bodyStamina = 1.0f;
        private float bodyControl = 1.0f;
        private float mindSpeed = 1.0f;
        private float difficulty = -10.0f;
        private boolean racingAway = false;
        private static final int PERC_OFFSET = 25;
        private static final int SPEED_OFFSET = 75;

        public byte getFishTypeId() {
            return this.fishTypeId;
        }

        public void setFishTypeId(byte fishTypeId) {
            this.fishTypeId = fishTypeId;
        }

        @Override
        public float getSpeed() {
            float mod = this.racingAway ? 2.5f : (75.0f + (float)this.ql) / 175.0f;
            return this.getFishData().getBaseSpeed() * mod;
        }

        public FishEnums.FishData getFishData() {
            return FishEnums.FishData.fromInt(this.fishTypeId);
        }

        public void setQL(double ql) {
            this.ql = ql;
            this.qlperc = (25.0f + (float)this.ql) / 125.0f;
            this.bodyStrength = Math.max(this.getFishData().getBodyStrength() * this.qlperc, 1.0f);
            this.bodyStamina = Math.max(this.getFishData().getBodyStamina() * this.qlperc, 1.0f);
            this.bodyControl = Math.max(this.getFishData().getBodyControl() * this.qlperc, 1.0f);
            this.mindSpeed = Math.max(this.getFishData().getMindSpeed() * this.qlperc, 1.0f);
            this.setSizeModifier(this.qlperc * this.getFishData().getScaleMod());
            ItemTemplate it = this.getFishData().getTemplate();
            if (it != null) {
                this.weight = (int)((double)it.getWeightGrams() * (ql / 100.0));
            }
        }

        public void setTargetPos(float targetPosX, float targetPosY) {
            this.targetPosX = targetPosX;
            this.targetPosY = targetPosY;
            this.calcTimeToTarget();
        }

        public void setRaceAway(boolean raceAway) {
            this.racingAway = raceAway;
            this.calcTimeToTarget();
        }

        private void calcTimeToTarget() {
            float diffX = this.targetPosX - this.getCreature().getPosX();
            float diffY = this.targetPosY - this.getCreature().getPosY();
            float dist = (float)Math.sqrt(diffX * diffX + diffY * diffY);
            float movementSpeed = this.getSpeed() * this.getMovementSpeedModifier();
            this.timeToTarget = dist / movementSpeed * 10.0f + 2.0f;
        }

        public float getTargetPosX() {
            return this.targetPosX;
        }

        public float getTargetPosY() {
            return this.targetPosY;
        }

        public float getTimeToTarget() {
            return this.timeToTarget;
        }

        public double getQL() {
            return this.ql;
        }

        public String getNameWithGenusAndSize() {
            return StringUtilities.addGenus(this.getNameWithSize(), false);
        }

        public String getNameWithSize() {
            StringBuilder buf = new StringBuilder();
            if (this.ql >= 99.0) {
                buf.append("stupendous ");
            } else if (this.ql >= 95.0) {
                buf.append("massive ");
            } else if (this.ql >= 85.0) {
                buf.append("huge ");
            } else if (this.ql >= 75.0) {
                buf.append("impressive ");
            } else if (this.ql >= 65.0) {
                buf.append("large ");
            }
            if (this.ql < 15.0) {
                buf.append("small ");
            }
            buf.append(this.getFishData().getName());
            return buf.toString();
        }

        public int getWeight() {
            return this.weight;
        }

        public float getBodyStrength() {
            return this.bodyStrength;
        }

        public float getBodyStamina() {
            return this.bodyStamina;
        }

        public void decBodyStamina(float bodyStamina) {
            this.bodyStamina = Math.max(this.bodyStamina - bodyStamina, 0.0f);
        }

        public float getBodyControl() {
            return this.bodyControl;
        }

        public float getMindSpeed() {
            return this.mindSpeed;
        }

        public void setDifficulty(float difficulty) {
            this.difficulty = difficulty;
        }

        public float getDifficulty() {
            return this.difficulty;
        }
    }
}


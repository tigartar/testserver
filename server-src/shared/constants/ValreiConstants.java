/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import java.nio.ByteBuffer;

public class ValreiConstants {
    private static final int LONG = 8;
    private static final int INT = 4;
    private static final int FLOAT = 4;
    private static final int SHORT = 2;
    private static final int BYTE = 1;
    public static final short ACTION_NONE = 0;
    public static final short ACTION_ADDFIGHTER = 1;
    public static final short ACTION_MOVE = 2;
    public static final short ACTION_STARTTURN = 3;
    public static final short ACTION_MELEEATTACK = 4;
    public static final short ACTION_RANGEDATTACK = 5;
    public static final short ACTION_DEITYSPELL = 6;
    public static final short ACTION_SORCERYSPELL = 7;
    public static final short ACTION_ENDFIGHT = 8;
    public static final byte ATTACK_MISS = 0;
    public static final byte ATTACK_BLOCK = 1;
    public static final byte ATTACK_HIT = 2;
    public static final byte SPELLTYPE_HEAL = 0;
    public static final byte SPELLTYPE_OFFENSIVE = 1;
    public static final byte SPELLTYPE_PHYSICALDEFENSE = 2;
    public static final byte SPELLTYPE_SPELLDEFENSE = 3;
    public static final byte SPELLTYPE_ATTACKBUFF = 4;
    public static final float COST_HEAL = 30.0f;
    public static final float COST_OFFENSIVE = 20.0f;
    public static final float COST_PHYSICALDEFENSE = 60.0f;
    public static final float COST_SPELLDEFENSE = 60.0f;
    public static final float COST_ATTACKBUFF = 50.0f;

    public static String getFightActionName(ValreiFightAction fightAction) {
        switch (fightAction.getActionId()) {
            case 2: {
                return "Move";
            }
            case 3: {
                return "Turn Begins";
            }
            case 4: {
                return "Melee Attack";
            }
            case 5: {
                return "Ranged Attack";
            }
            case 6: {
                return "Deity Spell";
            }
            case 7: {
                return "Sorcery Spell";
            }
            case 8: {
                return "Fight End";
            }
        }
        return "Unknown (" + fightAction.getActionId() + ")";
    }

    public static String getFightActionSummary(ValreiFightAction fightAction) {
        ByteBuffer bb = ByteBuffer.wrap(fightAction.getActionData());
        switch (fightAction.getActionId()) {
            case 2: {
                return "Fighter(" + bb.getLong() + ") to " + bb.getInt() + "," + bb.getInt();
            }
            case 3: {
                return "Fighter(" + bb.getLong() + ")";
            }
            case 4: 
            case 5: {
                return "Fighter(" + bb.getLong() + ") vs Defender(" + bb.getLong() + ") " + ValreiConstants.getAttackStatus(bb.get()) + " for " + bb.getFloat() + " damage";
            }
            case 6: 
            case 7: {
                return "Fighter(" + bb.getLong() + ") vs Defender(" + bb.getLong() + ") " + ValreiConstants.getSpellType(bb.get()) + "(" + ValreiConstants.getAttackStatus(bb.get()) + ") for " + bb.getFloat() + " damage";
            }
            case 8: {
                return "Winner(" + bb.getLong() + ")";
            }
        }
        return "Unknown (" + fightAction.getActionId() + ")";
    }

    public static long getFightActionActor(ValreiFightAction fightAction) {
        ByteBuffer bb = ByteBuffer.wrap(fightAction.getActionData());
        return bb.getLong();
    }

    private static String getAttackStatus(byte status) {
        switch (status) {
            case 0: {
                return "MISSES";
            }
            case 1: {
                return "BLOCKS";
            }
            case 2: {
                return "HITS";
            }
        }
        return "UNKNOWNS";
    }

    private static String getSpellType(byte spell) {
        switch (spell) {
            case 0: {
                return "HEAL";
            }
            case 1: {
                return "OFFENSIVE";
            }
            case 2: {
                return "PHYSBUFF";
            }
            case 3: {
                return "SPELLBUFF";
            }
            case 4: {
                return "ATKBUFF";
            }
        }
        return "UNKNOWN";
    }

    public static byte[] getMoveData(long fighterId, int xPos, int yPos) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(fighterId);
        bb.putInt(xPos);
        bb.putInt(yPos);
        return bb.array();
    }

    public static byte[] getStartTurnData(long fighterId) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(fighterId);
        return bb.array();
    }

    public static byte[] getAttackData(long fighterId, long defenderId, float damageDealt) {
        ByteBuffer bb = ByteBuffer.allocate(21);
        bb.putLong(fighterId);
        bb.putLong(defenderId);
        bb.put((byte)(damageDealt < 0.0f ? 0 : (damageDealt > 0.0f ? 2 : 1)));
        bb.putFloat(damageDealt);
        return bb.array();
    }

    public static byte[] getSpellData(long fighterId, long defenderId, byte spellType, float damageDealt) {
        ByteBuffer bb = ByteBuffer.allocate(22);
        bb.putLong(fighterId);
        bb.putLong(defenderId);
        bb.put(spellType);
        bb.put((byte)(damageDealt == -1.0f ? 0 : (damageDealt > 0.0f ? 2 : 1)));
        bb.putFloat(damageDealt);
        return bb.array();
    }

    public static byte[] getEndFightData(long winnerId) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putLong(winnerId);
        return bb.array();
    }

    public static long getEndFightWinner(byte[] actionData) {
        ByteBuffer bb = ByteBuffer.wrap(actionData);
        return bb.getLong();
    }

    public static class ValreiFightAction {
        private int actionNum;
        private short actionId;
        private byte[] actionData;

        public ValreiFightAction(int actionNum, short actionId, byte[] actionData) {
            this.actionNum = actionNum;
            this.actionId = actionId;
            this.actionData = actionData;
        }

        public int getActionNum() {
            return this.actionNum;
        }

        public void setActionNum(int actionNum) {
            this.actionNum = actionNum;
        }

        public short getActionId() {
            return this.actionId;
        }

        public void setActionId(short actionId) {
            this.actionId = actionId;
        }

        public byte[] getActionData() {
            return this.actionData;
        }

        public void setActionData(byte[] actionData) {
            this.actionData = actionData;
        }
    }
}


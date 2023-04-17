/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;

public final class SpecialEffects
implements CounterTypes,
MiscConstants {
    private final String name;
    private final int id;
    private byte requiredPower = 0;
    private static final int numEffects = 6;
    private static final SpecialEffects[] effects = new SpecialEffects[6];
    public static final int NO_EFFECT = 0;
    public static final int OPEN_DOOR = 1;
    public static final int HEAL = 2;
    public static final int WOUND = 3;
    public static final int DELETE_TILE_ITEMS = 4;
    public static final int SEND_PLONK = 5;

    private SpecialEffects(int _id, String _name) {
        this.id = _id;
        this.name = _name;
    }

    public void setPowerRequired(byte power) {
        this.requiredPower = power;
    }

    public byte getPowerRequired() {
        return this.requiredPower;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public static final SpecialEffects[] getEffects() {
        return effects;
    }

    public static final SpecialEffects getEffect(int number) {
        try {
            return effects[number];
        }
        catch (Exception ex) {
            return null;
        }
    }

    public boolean run(Creature performer, int tilex, int tiley, int layer) {
        boolean toReturn = false;
        switch (this.id) {
            case 0: {
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                Creature[] creatures;
                VolaTile t = Zones.getTileOrNull(tilex, tiley, layer >= 0);
                if (t == null) break;
                for (Creature c : creatures = t.getCreatures()) {
                    c.getBody().healFully();
                }
                break;
            }
            case 3: {
                Creature[] creatures;
                VolaTile t12 = Zones.getTileOrNull(tilex, tiley, layer >= 0);
                if (t12 == null) break;
                for (Creature c : creatures = t12.getCreatures()) {
                    CombatEngine.addWound(c, c, (byte)3, 13, 1000.0, 1.0f, "bite", null, 0.0f, 0.0f, false, false, false, false);
                }
                break;
            }
            case 4: {
                Item[] items;
                VolaTile t22 = Zones.getTileOrNull(tilex, tiley, layer >= 0);
                if (t22 == null) break;
                for (Item i : items = t22.getItems()) {
                    if (i.isIndestructible()) continue;
                    Items.destroyItem(i.getWurmId());
                }
                break;
            }
        }
        return false;
    }

    public boolean run(Creature performer, long target) {
        boolean toReturn = false;
        switch (this.id) {
            case 0: {
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                performer.getBody().healFully();
                break;
            }
            case 3: {
                CombatEngine.addWound(performer, performer, (byte)3, 13, 1000.0, 1.0f, "bite", null, 0.0f, 0.0f, false, false, false, false);
                break;
            }
        }
        return false;
    }

    public boolean run(Creature performer, long target, int numbers) {
        boolean toReturn = false;
        switch (this.id) {
            case 0: {
                break;
            }
            case 5: {
                performer.getCommunicator().sendPlonk((short)numbers);
                break;
            }
            default: {
                return this.run(performer, target);
            }
        }
        return false;
    }

    static {
        SpecialEffects.effects[0] = new SpecialEffects(0, "Do nothing");
        SpecialEffects.effects[0].requiredPower = 0;
        SpecialEffects.effects[1] = new SpecialEffects(1, "Open door or gate");
        SpecialEffects.effects[1].requiredPower = (byte)2;
        SpecialEffects.effects[2] = new SpecialEffects(2, "Heal all wounds");
        SpecialEffects.effects[2].requiredPower = (byte)2;
        SpecialEffects.effects[3] = new SpecialEffects(3, "Create a wound");
        SpecialEffects.effects[3].requiredPower = (byte)2;
        SpecialEffects.effects[4] = new SpecialEffects(4, "Delete items on tile");
        SpecialEffects.effects[4].requiredPower = (byte)2;
        SpecialEffects.effects[5] = new SpecialEffects(5, "Send a notification");
        SpecialEffects.effects[5].requiredPower = (byte)2;
    }
}


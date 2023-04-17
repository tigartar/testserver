/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.combat;

import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import java.util.HashMap;
import java.util.Map;

public final class Weapon
implements MiscConstants {
    private final int itemid;
    private final float damage;
    private final float speed;
    private final float critchance;
    private final int reach;
    private final int weightGroup;
    private final float parryPercent;
    private final double skillPenalty;
    private static float randomizer = 0.0f;
    private static final Map<Integer, Weapon> weapons = new HashMap<Integer, Weapon>();
    private static Weapon toCheck = null;
    private boolean damagedByMetal = false;
    private static final float critChanceMod = 5.0f;
    private static final float strengthModifier = Servers.localServer.isChallengeOrEpicServer() ? 1000.0f : 300.0f;

    public Weapon(int _itemid, float _damage, float _speed, float _critchance, int _reach, int _weightGroup, float _parryPercent, double _skillPenalty) {
        this.itemid = _itemid;
        this.damage = _damage;
        this.speed = _speed;
        this.critchance = _critchance / 5.0f;
        this.reach = _reach;
        this.weightGroup = _weightGroup;
        this.parryPercent = _parryPercent;
        this.skillPenalty = _skillPenalty;
        weapons.put(this.itemid, this);
    }

    public static final float getBaseDamageForWeapon(Item weapon) {
        if (weapon == null) {
            return 0.0f;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.damage;
        }
        return 0.0f;
    }

    public static final double getModifiedDamageForWeapon(Item weapon, Skill strength) {
        return Weapon.getModifiedDamageForWeapon(weapon, strength, false);
    }

    public static final double getModifiedDamageForWeapon(Item weapon, Skill strength, boolean fullDam) {
        randomizer = fullDam ? 1.0f : (50.0f + Server.rand.nextFloat() * 50.0f) / 100.0f;
        double damreturn = 1.0;
        damreturn = weapon.isBodyPartAttached() ? (double)Weapon.getBaseDamageForWeapon(weapon) : (double)(Weapon.getBaseDamageForWeapon(weapon) * weapon.getCurrentQualityLevel() / 100.0f);
        damreturn *= 1.0 + strength.getKnowledge(0.0) / (double)strengthModifier;
        return damreturn *= (double)randomizer;
    }

    public static final float getBaseSpeedForWeapon(Item weapon) {
        if (weapon == null || weapon.isBodyPartAttached()) {
            return 1.0f;
        }
        float materialMod = 1.0f;
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (weapon.getMaterial()) {
                case 57: {
                    materialMod = 0.9f;
                    break;
                }
                case 7: {
                    materialMod = 1.05f;
                    break;
                }
                case 67: {
                    materialMod = 0.95f;
                    break;
                }
                case 34: {
                    materialMod = 0.96f;
                    break;
                }
                case 13: {
                    materialMod = 0.95f;
                    break;
                }
                case 96: {
                    materialMod = 1.025f;
                }
            }
        }
        if ((toCheck = weapons.get(weapon.getTemplateId())) != null) {
            return Weapon.toCheck.speed * materialMod;
        }
        return 20.0f * materialMod;
    }

    public static final float getRarityCritMod(byte rarity) {
        switch (rarity) {
            case 0: {
                return 1.0f;
            }
            case 1: {
                return 1.1f;
            }
            case 2: {
                return 1.3f;
            }
            case 3: {
                return 1.5f;
            }
        }
        return 1.0f;
    }

    public static final float getCritChanceForWeapon(Item weapon) {
        if (weapon == null || weapon.isBodyPartAttached()) {
            return 0.01f;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.critchance * Weapon.getRarityCritMod(weapon.getRarity());
        }
        return 0.0f;
    }

    public static final int getReachForWeapon(Item weapon) {
        if (weapon == null || weapon.isBodyPartAttached()) {
            return 1;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.reach;
        }
        return 1;
    }

    public static final int getWeightGroupForWeapon(Item weapon) {
        if (weapon == null || weapon.isBodyPartAttached()) {
            return 1;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.weightGroup;
        }
        return 10;
    }

    public static final double getSkillPenaltyForWeapon(Item weapon) {
        if (weapon == null || weapon.isBodyPartAttached()) {
            return 0.0;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.skillPenalty;
        }
        return 7.0;
    }

    public static final float getWeaponParryPercent(Item weapon) {
        if (weapon == null) {
            return 0.0f;
        }
        if (weapon.isBodyPart()) {
            return 0.0f;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.parryPercent;
        }
        return 0.0f;
    }

    void setDamagedByMetal(boolean aDamagedByMetal) {
        this.damagedByMetal = aDamagedByMetal;
    }

    public static final boolean isWeaponDamByMetal(Item weapon) {
        if (weapon == null) {
            return false;
        }
        if (weapon.isBodyPart() && weapon.isBodyPartRemoved()) {
            return true;
        }
        toCheck = weapons.get(weapon.getTemplateId());
        if (toCheck != null) {
            return Weapon.toCheck.damagedByMetal;
        }
        return false;
    }

    public static double getMaterialDamageBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 56: {
                    return 1.1f;
                }
                case 30: {
                    return 0.99f;
                }
                case 31: {
                    return 0.985f;
                }
                case 10: {
                    return 0.65f;
                }
                case 7: {
                    return 0.975f;
                }
                case 12: {
                    return 0.5;
                }
                case 67: {
                    return 1.05f;
                }
                case 34: {
                    return 0.925f;
                }
                case 13: {
                    return 0.9f;
                }
            }
        } else if (material == 56) {
            return 1.1f;
        }
        return 1.0;
    }

    public static double getMaterialHunterDamageBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 8: {
                    return 1.1f;
                }
                case 96: {
                    return 1.05f;
                }
            }
        }
        return 1.0;
    }

    public static double getMaterialArmourDamageBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 30: {
                    return 1.05f;
                }
                case 31: {
                    return 1.075f;
                }
                case 7: {
                    return 1.05f;
                }
                case 9: {
                    return 1.025f;
                }
            }
        }
        return 1.0;
    }

    public static float getMaterialParryBonus(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 8: {
                    return 1.025f;
                }
                case 34: {
                    return 1.05f;
                }
            }
        }
        return 1.0f;
    }

    public static float getMaterialExtraWoundMod(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 10: {
                    return 0.3f;
                }
                case 12: {
                    return 0.75f;
                }
            }
        }
        return 0.0f;
    }

    public static byte getMaterialExtraWoundType(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 10: {
                    return 5;
                }
                case 12: {
                    return 5;
                }
            }
        }
        return 5;
    }

    public static double getMaterialBashModifier(byte material) {
        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
            switch (material) {
                case 56: {
                    return 1.075f;
                }
                case 30: {
                    return 1.05f;
                }
                case 31: {
                    return 1.025f;
                }
                case 10: {
                    return 0.9f;
                }
                case 57: {
                    return 1.1f;
                }
                case 7: {
                    return 1.1f;
                }
                case 12: {
                    return 1.2f;
                }
                case 67: {
                    return 1.075f;
                }
                case 8: {
                    return 1.1f;
                }
                case 9: {
                    return 1.05f;
                }
                case 34: {
                    return 0.9f;
                }
                case 13: {
                    return 0.85f;
                }
                case 96: {
                    return 1.1f;
                }
            }
        }
        return 1.0;
    }
}


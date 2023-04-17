/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.bodys;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.modifiers.ValueModifier;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.spells.EnchantUtil;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ProtoConstants;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class Wound
implements CounterTypes,
TimeConstants,
MiscConstants,
ModifierTypes {
    private final byte location;
    private final long id;
    private byte type;
    float severity;
    private final long owner;
    float poisonSeverity = 0.0f;
    float infectionSeverity = 0.0f;
    private Set<ValueModifier> modifiers = null;
    public static final String severe = "severe";
    public static final String verylight = "very light";
    public static final String light = "light";
    public static final String medium = "medium";
    public static final String bad = "bad";
    public static final String bandaged = ", bandaged";
    public static final String applied = ", applied";
    public static final String treated = ", treated";
    boolean isBandaged = false;
    long lastPolled = 0L;
    byte healEff = 0;
    public static final byte TYPE_CRUSH = 0;
    public static final byte TYPE_SLASH = 1;
    public static final byte TYPE_PIERCE = 2;
    public static final byte TYPE_BITE = 3;
    public static final byte TYPE_BURN = 4;
    public static final byte TYPE_POISON = 5;
    public static final byte TYPE_INFECTION = 6;
    public static final byte TYPE_WATER = 7;
    public static final byte TYPE_COLD = 8;
    public static final byte TYPE_INTERNAL = 9;
    public static final byte TYPE_ACID = 10;
    public static final String wound = "Wound";
    public static final String burn = "Burn";
    public static final String coldburn = "Coldburn";
    public static final String acidburn = "Acidburn";
    public static final String bruise = "Bruise";
    public static final String inter = "Internal";
    public static final String drown = "Water";
    public static final String cut = "Cut";
    public static final String bite = "Bite";
    public static final String poison = "Poison";
    public static final String hole = "Hole";
    public static final String infection = "Infection";
    public static final String acid = "Acid";
    private static final float WOUNDMULTIPLIER = 50000.0f;
    private static final Logger logger = Logger.getLogger(Wound.class.getName());
    public static final float slowWoundMod = 4.0f;
    public static final float fastWoundMod = 5.0f;
    public static final float severityVeryLight = 3275.0f;
    public static final float severityLight = 9825.0f;
    public static final float severityMedium = 19650.0f;
    public static final float severityBad = 29475.0f;
    public static final float severitySevere = 60000.0f;
    public static final float effSeverityVeryLight = 20.0f;
    public static final float effSeverityLight = 40.0f;
    public static final float effSeverityMedium = 60.0f;
    public static final float effSeverityBad = 80.0f;
    public static final float effSeveritySevere = 100.0f;
    public static final String crushVeryLight = "a small bruise";
    public static final String crushLight = "a bruise";
    public static final String crushMedium = "an aching bruise";
    public static final String crushBad = "a severe fracture";
    public static final String crushSevere = "splinters of crushed bone";
    public static final String slashVeryLight = "a small bleeding scar";
    public static final String slashLight = "a trickle of blood";
    public static final String slashMedium = "a cut";
    public static final String slashBad = "a severe cut";
    public static final String slashSevere = "a wide gap of cut tissue";
    public static final String pierceVeryLight = "a small bleeding pinch";
    public static final String pierceLight = "a trickle of blood";
    public static final String pierceMedium = "a small hole";
    public static final String pierceBad = "a deep hole";
    public static final String pierceSevere = "a straight-through gaping hole";
    public static final String biteVeryLight = "a bruise from a bite";
    public static final String biteLight = "a light bite";
    public static final String biteMedium = "holes from a bite";
    public static final String biteBad = "a large bitewound";
    public static final String biteSevere = "a huge bitewound";
    public static final String coldLight = "a reddish tone";
    public static final String coldMedium = "white flecks";
    public static final String coldSevere = "black skin with possible gangrene";
    public static final String internalLight = "small tingle";
    public static final String internalMedium = "throbbing ache";
    public static final String internalSevere = "excruciating pain";
    public static final String burnVeryLight = "a few blisters";
    public static final String burnLight = "a lot of blisters on red skin";
    public static final String burnMedium = "some black and red burnt skin";
    public static final String burnBad = "some melted skin";
    public static final String burnSevere = "black and red melted and loose skin";
    public static final String acidVeryLight = "a few moist blisters";
    public static final String acidLight = "a lot of moist blisters on red skin";
    public static final String acidMedium = "some watery red burnt skin";
    public static final String acidBad = "some oozing melted skin";
    public static final String acidSevere = "black and bubbling melted and loose skin";
    public static final String waterVeryLight = "some coughing and choking";
    public static final String waterMedium = "water in the lungs";
    public static final String waterSevere = "waterfilled lungs, causing severe breathing difficulties";
    public static final String poisonVeryLight = "a faint dark aura";
    public static final String poisonLight = "a worrying dark aura";
    public static final String poisonMedium = "an ominous dark aura";
    public static final String poisonBad = "blue-black aura miscolouring the veins";
    public static final String poisonSevere = "black veins running from";
    public static final String infectionVeryLight = "with faintly miscolored edges";
    public static final String infectionLight = "with worryingly deep red edges";
    public static final String infectionMedium = "with ominously red edges and some yellow pus";
    public static final String infectionBad = "covered in yellow pus";
    public static final String infectionSevere = "rotting from infection";
    Creature creature = null;
    public static final float champDamageModifier = 0.4f;

    Wound(byte _type, byte _location, float _severity, long _owner, float _poisonSeverity, float _infectionSeverity, boolean isTemporary, boolean pvp, boolean spell) {
        this.type = _type;
        this.location = _location;
        this.severity = _severity;
        this.severity = (float)((double)this.severity * this.getWoundMod());
        this.severity = (int)this.severity;
        this.owner = _owner;
        this.poisonSeverity = _poisonSeverity;
        this.infectionSeverity = _infectionSeverity;
        this.lastPolled = System.currentTimeMillis();
        this.id = isTemporary ? WurmId.getNextTemporaryWoundId() : WurmId.getNextWoundId();
        this.setCreature();
        if (this.creature == null) {
            return;
        }
        if ((this.type == 4 || this.type == 7 || this.type == 8) && this.creature.getCultist() != null && this.creature.getCultist().hasNoElementalDamage()) {
            this.severity = 0.0f;
        }
        this.severity *= this.creature.getDamageModifier(pvp, spell);
        if (this.creature.isChampion()) {
            this.severity = (int)Math.max(1.0f, this.severity * 0.4f);
        }
        if (this.creature.isPlayer() && (this.location == 18 || this.location == 19) && this.severity > 29475.0f) {
            this.creature.achievement(35);
        }
        this.create();
        this.addModifier(2);
        this.addModifier(3);
        this.addModifier(1);
        this.addModifier(4);
        this.addModifier(6);
        this.addModifier(5);
        this.creature.maybeInterruptAction((int)this.severity);
    }

    Wound(long _id, byte _type, byte _location, float _severity, long _owner, float _poisonSeverity, float _infectionSeverity, long _lastPolled, boolean aBandaged, byte healeff) {
        this.id = _id;
        this.type = _type;
        this.location = _location;
        this.severity = _severity;
        this.owner = _owner;
        this.poisonSeverity = _poisonSeverity;
        this.infectionSeverity = _infectionSeverity;
        this.healEff = healeff;
        this.isBandaged = aBandaged;
        this.lastPolled = System.currentTimeMillis();
        this.addModifier(2);
        this.addModifier(3);
        this.addModifier(1);
        this.addModifier(4);
        this.addModifier(6);
        this.addModifier(5);
        this.setCreature();
    }

    @Deprecated
    private static float getVulnerabilityModifier(Creature c, byte woundType) {
        if (c.hasAnyAbility()) {
            switch (woundType) {
                case 4: {
                    if (!(c.getFireVulnerability() > 0.0f)) break;
                    return c.getFireVulnerability();
                }
                case 1: {
                    if (!(c.getSlashVulnerability() > 0.0f)) break;
                    return c.getSlashVulnerability();
                }
                case 3: {
                    if (!(c.getBiteVulnerability() > 0.0f)) break;
                    return c.getBiteVulnerability();
                }
                case 2: {
                    if (!(c.getPierceVulnerability() > 0.0f)) break;
                    return c.getPierceVulnerability();
                }
                case 0: {
                    if (!(c.getCrushVulnerability() > 0.0f)) break;
                    return c.getCrushVulnerability();
                }
                case 9: {
                    if (!(c.getInternalVulnerability() > 0.0f)) break;
                    return c.getInternalVulnerability();
                }
                case 7: {
                    if (!(c.getWaterVulnerability() > 0.0f)) break;
                    return c.getWaterVulnerability();
                }
                case 6: {
                    if (!(c.getDiseaseVulnerability() > 0.0f)) break;
                    return c.getDiseaseVulnerability();
                }
                case 8: {
                    if (!(c.getColdVulnerability() > 0.0f)) break;
                    return c.getColdVulnerability();
                }
                case 5: {
                    if (!(c.getPoisonVulnerability() > 0.0f)) break;
                    return c.getPoisonVulnerability();
                }
                default: {
                    return 1.0f;
                }
            }
        }
        return 1.0f;
    }

    public static float getResistModifier(@Nullable Creature attacker, Creature c, byte woundType) {
        float mod = 1.0f;
        if (attacker != null) {
            float resMult = EnchantUtil.getJewelryDamageIncrease(attacker, woundType);
            if (Servers.isThisATestServer() && resMult != 1.0f) {
                c.getCommunicator().sendCombatAlertMessage(String.format("Damage reduced to %.1f%% from jewelry enchants.", Float.valueOf(mod * 100.0f)));
            }
            mod *= resMult;
        }
        float damMult = EnchantUtil.getJewelryResistModifier(c, woundType);
        mod *= damMult;
        if (Servers.isThisATestServer() && damMult != 1.0f) {
            c.getCommunicator().sendCombatAlertMessage(String.format("Damage increased to %.1f%% from jewelry enchants.", Float.valueOf(mod * 100.0f)));
        }
        if (c.hasAnyAbility()) {
            float physMod = 1.0f;
            if (c.getPhysicalResistance() > 0.0f) {
                physMod = 1.0f - c.getPhysicalResistance();
            }
            switch (woundType) {
                case 4: {
                    if (c.getFireResistance() > 0.0f) {
                        mod *= c.getFireResistance();
                    }
                    if (!(c.getFireVulnerability() > 0.0f)) break;
                    mod *= c.getFireVulnerability();
                    break;
                }
                case 1: {
                    if (c.getSlashResistance() > 0.0f) {
                        mod *= c.getSlashResistance();
                    }
                    if (c.getSlashVulnerability() > 0.0f) {
                        mod *= c.getSlashVulnerability();
                    }
                    mod *= physMod;
                    break;
                }
                case 3: {
                    if (c.getBiteResistance() > 0.0f) {
                        mod *= c.getBiteResistance();
                    }
                    if (c.getBiteVulnerability() > 0.0f) {
                        mod *= c.getBiteVulnerability();
                    }
                    mod *= physMod;
                    break;
                }
                case 2: {
                    if (c.getPierceResistance() > 0.0f) {
                        mod *= c.getPierceResistance();
                    }
                    if (c.getPierceVulnerability() > 0.0f) {
                        mod *= c.getPierceVulnerability();
                    }
                    mod *= physMod;
                    break;
                }
                case 0: {
                    if (c.getCrushResistance() > 0.0f) {
                        mod *= c.getCrushResistance() * physMod;
                    }
                    if (c.getCrushVulnerability() > 0.0f) {
                        mod *= c.getCrushVulnerability();
                    }
                    mod *= physMod;
                    break;
                }
                case 9: {
                    if (c.getInternalResistance() > 0.0f) {
                        mod *= c.getInternalResistance();
                    }
                    if (!(c.getInternalVulnerability() > 0.0f)) break;
                    mod *= c.getInternalVulnerability();
                    break;
                }
                case 7: {
                    if (c.getWaterResistance() > 0.0f) {
                        mod *= c.getWaterResistance();
                    }
                    if (!(c.getWaterVulnerability() > 0.0f)) break;
                    mod *= c.getWaterVulnerability();
                    break;
                }
                case 6: {
                    if (c.getDiseaseResistance() > 0.0f) {
                        mod *= c.getDiseaseResistance();
                    }
                    if (!(c.getDiseaseVulnerability() > 0.0f)) break;
                    mod *= c.getDiseaseVulnerability();
                    break;
                }
                case 8: {
                    if (c.getColdResistance() > 0.0f) {
                        mod *= c.getColdResistance();
                    }
                    if (!(c.getColdVulnerability() > 0.0f)) break;
                    mod *= c.getColdVulnerability();
                    break;
                }
                case 5: {
                    if (c.getPoisonResistance() > 0.0f) {
                        mod *= c.getPoisonResistance();
                    }
                    if (!(c.getPoisonVulnerability() > 0.0f)) break;
                    mod *= c.getPoisonVulnerability();
                }
            }
        }
        return mod;
    }

    public boolean isPoison() {
        return this.poisonSeverity > 0.0f;
    }

    public boolean isInternal() {
        return this.type == 9 || this.type == 5;
    }

    public boolean isBruise() {
        return this.severity < 19650.0f && this.type == 0;
    }

    public boolean isDrownWound() {
        return this.type == 7;
    }

    public boolean isAcidWound() {
        return this.type == 10;
    }

    public void setType(byte newType) {
        this.type = newType;
    }

    private void addModifier(int _type) {
        if (this.modifiers == null) {
            this.modifiers = new HashSet<ValueModifier>();
        }
        DoubleValueModifier modifier = null;
        if (_type == 2) {
            int w = Wounds.getModifiedSkill(this.location, this.type);
            if (w != -1) {
                try {
                    Creature _creature = Server.getInstance().getCreature(this.owner);
                    float champMod = 1.0f;
                    if (_creature.isChampion()) {
                        champMod = 2.5f;
                    }
                    modifier = new DoubleValueModifier(2, champMod * -this.severity / 50000.0f);
                    _creature.getSkills().getSkill(w).addModifier(modifier);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                    modifier = null;
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                    modifier = null;
                }
                catch (NoSuchSkillException nss) {
                    modifier = null;
                }
            }
        } else if (_type == 1) {
            if (this.infectionSeverity > 0.0f) {
                try {
                    Creature _creature = Server.getInstance().getCreature(this.owner);
                    float champMod = 1.0f;
                    if (_creature.isChampion()) {
                        champMod = 2.5f;
                    }
                    modifier = new DoubleValueModifier(1, champMod * -this.infectionSeverity / 500.0f);
                    _creature.getStatus().addModifier(modifier);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                    modifier = null;
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                    modifier = null;
                }
            }
        } else if (_type == 3) {
            if (this.impairsMovement()) {
                try {
                    double mod = 0.3f;
                    if (this.location == 15 || this.location == 16) {
                        mod = 0.45f;
                    }
                    Creature _creature = Server.getInstance().getCreature(this.owner);
                    float champMod = 1.0f;
                    if (_creature.isChampion()) {
                        champMod = 1.1f;
                    }
                    modifier = new DoubleValueModifier(3, (double)(champMod * -this.severity / 50000.0f) * mod);
                    MovementScheme scheme = _creature.getMovementScheme();
                    scheme.addModifier(modifier);
                    modifier.addListener(scheme);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                    modifier = null;
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                    modifier = null;
                }
            }
        } else if (_type == 4) {
            if (this.location == 18 || this.location == 19) {
                try {
                    Creature _creature = Server.getInstance().getCreature(this.owner);
                    float champMod = 1.0f;
                    if (_creature.isChampion()) {
                        champMod = 2.5f;
                    }
                    modifier = new DoubleValueModifier(4, champMod * this.severity / 50000.0f);
                    _creature.addVisionModifier(modifier);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                    modifier = null;
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                    modifier = null;
                }
            }
        } else if (_type == 5) {
            if (this.location == 4) {
                try {
                    Creature _creature = Server.getInstance().getCreature(this.owner);
                    float champMod = 1.0f;
                    if (_creature.isChampion()) {
                        champMod = 2.5f;
                    }
                    modifier = new DoubleValueModifier(5, champMod * this.severity / 50000.0f);
                    _creature.getCombatHandler().addParryModifier(modifier);
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                    modifier = null;
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                    modifier = null;
                }
            }
        } else if (_type == 6 && this.location == 23) {
            try {
                Creature _creature = Server.getInstance().getCreature(this.owner);
                float champMod = 1.0f;
                if (_creature.isChampion()) {
                    champMod = 2.5f;
                }
                modifier = new DoubleValueModifier(6, champMod * this.severity / 50000.0f);
                _creature.getCombatHandler().addDodgeModifier(modifier);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, nsp.getMessage(), nsp);
                modifier = null;
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, nsc.getMessage(), nsc);
                modifier = null;
            }
        }
        if (modifier != null) {
            this.modifiers.add(modifier);
        }
    }

    public final boolean impairsMovement() {
        return this.location == 30 || this.location == 31 || this.location == 15 || this.location == 16 || this.location == 11 || this.location == 12;
    }

    private void removeModifier(ValueModifier modifier) {
        this.modifiers.remove(modifier);
        try {
            Creature _creature = Server.getInstance().getCreature(this.owner);
            if (modifier.getType() == 1) {
                _creature.getStatus().removeModifier((DoubleValueModifier)modifier);
            } else if (modifier.getType() == 2) {
                int w = Wounds.getModifiedSkill(this.location, this.type);
                if (w != -1) {
                    _creature.getSkills().getSkill(w).removeModifier((DoubleValueModifier)modifier);
                } else {
                    logger.log(Level.WARNING, "This should not happen.");
                }
            } else if (modifier.getType() == 3) {
                MovementScheme scheme = _creature.getMovementScheme();
                scheme.removeModifier((DoubleValueModifier)modifier);
                modifier.removeListener(scheme);
            } else if (modifier.getType() == 5) {
                _creature.getCombatHandler().removeParryModifier((DoubleValueModifier)modifier);
            } else if (modifier.getType() == 6) {
                _creature.getCombatHandler().removeDodgeModifier((DoubleValueModifier)modifier);
            } else if (modifier.getType() == 4) {
                _creature.removeVisionModifier((DoubleValueModifier)modifier);
            }
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, nsc.getMessage(), nsc);
        }
        catch (NoSuchSkillException noSuchSkillException) {
            // empty catch block
        }
    }

    private void removeModifier(int _type) {
        if (this.modifiers != null) {
            ValueModifier[] mods = this.getModifiers();
            for (int x = 0; x < mods.length; ++x) {
                if (mods[x].getType() != _type) continue;
                this.removeModifier(mods[x]);
            }
        }
    }

    private ValueModifier getModifier(int _type) {
        if (this.modifiers != null) {
            ValueModifier[] mods = this.getModifiers();
            for (int x = 0; x < mods.length; ++x) {
                if (mods[x].getType() != _type) continue;
                return mods[x];
            }
        }
        return null;
    }

    private ValueModifier[] getModifiers() {
        if (this.modifiers != null) {
            return this.modifiers.toArray(new ValueModifier[this.modifiers.size()]);
        }
        return null;
    }

    final void removeAllModifiers() {
        ValueModifier[] mods = this.getModifiers();
        if (mods != null) {
            for (int x = 0; x < mods.length; ++x) {
                this.removeModifier(mods[x]);
            }
        }
    }

    public final byte getLocation() {
        return this.location;
    }

    public final float getPoisonSeverity() {
        return this.poisonSeverity;
    }

    public final float getSeverity() {
        return this.severity;
    }

    public final ProtoConstants.WoundSeverity getSeverityEnum() {
        ProtoConstants.WoundSeverity toReturn = ProtoConstants.WoundSeverity.severe;
        if (this.severity < 3275.0f) {
            toReturn = ProtoConstants.WoundSeverity.verylight;
        } else if (this.severity < 9825.0f) {
            toReturn = ProtoConstants.WoundSeverity.light;
        } else if (this.severity < 19650.0f) {
            toReturn = ProtoConstants.WoundSeverity.medium;
        } else if (this.severity < 29475.0f) {
            toReturn = ProtoConstants.WoundSeverity.bad;
        }
        return toReturn;
    }

    final long getId() {
        return this.id;
    }

    public final byte getType() {
        return this.type;
    }

    public final ProtoConstants.WoundType getTypeEnum() {
        return ProtoConstants.WoundType.bite;
    }

    final long getOwner() {
        return this.owner;
    }

    public final float getInfectionSeverity() {
        return this.infectionSeverity;
    }

    public final ProtoConstants.InfectionSeverity getInfectionSeverityEnum() {
        return ProtoConstants.InfectionSeverity.bad;
    }

    public final long getLastPolled() {
        return this.lastPolled;
    }

    public final byte getHealEff() {
        return this.healEff;
    }

    public final Creature getCreature() {
        return this.creature;
    }

    final void removeCreature() {
        this.creature = null;
    }

    public final boolean bandage() {
        this.setBandaged(true);
        return this.isBandaged;
    }

    public final boolean curePoison() {
        if (this.type == 5) {
            this.heal();
        } else {
            this.setPoisonSeverity(0.0f);
        }
        return true;
    }

    public final boolean cureInfection() {
        if (this.type == 6) {
            this.heal();
        } else {
            this.setInfectionSeverity(0.0f);
            this.removeModifier(1);
        }
        return true;
    }

    public final void heal() {
        if (this.creature != null) {
            this.creature.getStatus().modifyWounds((int)(-this.severity));
            Body body = this.creature.getBody();
            body.removeWound(this);
            if (this.creature != null && this.isPoison()) {
                this.creature.poisonChanged(true, this);
            }
        }
    }

    public final boolean modifySeverity(int num) {
        return this.modifySeverity(num, false, false);
    }

    public final boolean modifySeverity(int num, boolean pvp, boolean spell) {
        boolean dead;
        block21: {
            dead = false;
            if (this.creature != null) {
                if ((num = (int)((double)num * this.getWoundMod())) > 0) {
                    num = (int)((float)num * this.creature.getDamageModifier(pvp, spell));
                    if (this.creature.isChampion()) {
                        num = (int)Math.max(1.0f, (float)num * 0.4f);
                    }
                }
                Body body = this.creature.getBody();
                float sev = this.severity + (float)num;
                if (sev <= 0.0f) {
                    this.creature.getStatus().modifyWounds((int)(-this.severity));
                    body.removeWound(this);
                } else {
                    this.setSeverity(sev);
                    if (num > 0 && this.severity > 1000.0f) {
                        this.creature.maybeInterruptAction((int)this.severity);
                    }
                    float champMod = 1.0f;
                    if (this.creature.isChampion()) {
                        champMod = 2.5f;
                    }
                    dead = this.creature.getStatus().modifyWounds(num);
                    DoubleValueModifier val = (DoubleValueModifier)this.getModifier(2);
                    if (val != null) {
                        val.setModifier(champMod * -this.severity / 50000.0f);
                    }
                    if ((val = (DoubleValueModifier)this.getModifier(6)) != null) {
                        val.setModifier(champMod * this.severity / 50000.0f);
                    }
                    if ((val = (DoubleValueModifier)this.getModifier(5)) != null) {
                        val.setModifier(champMod * this.severity / 50000.0f);
                    }
                    if ((val = (DoubleValueModifier)this.getModifier(4)) != null) {
                        val.setModifier(champMod * this.severity / 50000.0f);
                    }
                    if ((val = (DoubleValueModifier)this.getModifier(3)) != null) {
                        double mod = 0.3f;
                        if (this.location == 15 || this.location == 16) {
                            mod = 0.45f;
                        }
                        if (this.creature.isChampion()) {
                            champMod = 1.1f;
                        }
                        val.setModifier((double)(champMod * -this.severity / 50000.0f) * mod);
                    }
                    if (!dead) {
                        try {
                            if (this.creature == null) break block21;
                            if (this.creature.getBody() != null) {
                                Item bodypart = this.creature.getBody().getBodyPartForWound(this);
                                try {
                                    Creature[] watchers = bodypart.getWatchers();
                                    for (int x = 0; x < watchers.length; ++x) {
                                        watchers[x].getCommunicator().sendUpdateWound(this, bodypart);
                                    }
                                    break block21;
                                }
                                catch (NoSuchCreatureException noSuchCreatureException) {
                                    break block21;
                                }
                            }
                            logger.log(Level.WARNING, this.creature.getName() + " body is null.", new Exception());
                        }
                        catch (NoSpaceException nsp) {
                            logger.log(Level.INFO, nsp.getMessage(), nsp);
                        }
                    }
                }
            }
        }
        return dead;
    }

    private double getWoundMod() {
        double toReturn = 1.0;
        if (this.location == 18 || this.location == 19 || this.location == 20) {
            toReturn = 1.35;
        } else if (this.location == 29 || this.location == 17 || this.location == 33 || this.location == 1) {
            toReturn = 1.3;
        } else if (this.location == 5 || this.location == 6) {
            toReturn = 1.25;
        } else if (this.location == 13 || this.location == 14 || this.location == 15 || this.location == 16 || this.location == 25) {
            toReturn = 1.2;
        }
        return toReturn;
    }

    final void poll(boolean hasWoundIncreasePrevention) throws Exception {
        if (System.currentTimeMillis() - this.lastPolled > 600000L || (this.creature == null || !this.creature.isUnique()) && this.type == 6 && System.currentTimeMillis() - this.lastPolled > 60000L) {
            float mod = 5.0f;
            if (this.severity > 3275.0f) {
                mod = -5.0f;
                if (this.severity < 9825.0f) {
                    mod = 4.0f;
                } else if (this.severity < 19650.0f) {
                    mod = 0.0f;
                } else if (this.severity < 29475.0f) {
                    mod = -4.0f;
                }
            }
            if (this.healEff > 0) {
                mod += (float)this.healEff / 2.0f;
            }
            if (this.isBandaged) {
                mod += 1.0f;
            }
            if (this.type == 7) {
                mod += 3.0f;
            } else if (this.type == 9) {
                mod += 10.0f;
            }
            if (this.type == 10) {
                mod -= 3.0f;
            }
            if (this.type == 6 && !this.isBandaged() && !this.isTreated()) {
                mod -= 5.0f;
            }
            if (this.creature != null) {
                if (!this.creature.isUnique()) {
                    int tn;
                    int rand;
                    if (this.creature.getStatus().getNutritionlevel() > 0.6f) {
                        mod += 1.0f;
                    } else if (this.creature.getStatus().getNutritionlevel() < 0.4f) {
                        mod -= 1.0f;
                    }
                    if (this.creature.getStatus().fat > 70) {
                        mod += 1.0f;
                    } else if (this.creature.getStatus().fat < 30) {
                        mod -= 1.0f;
                    }
                    if (this.infectionSeverity > 0.0f && (float)(rand = Server.rand.nextInt(100 + this.healEff)) < this.infectionSeverity) {
                        mod -= Math.max(1.0f, this.infectionSeverity / 10.0f);
                    }
                    if (this.creature != null && this.creature.getDeity() != null && this.creature.getDeity().isHealer() && this.creature.getFaith() >= 20.0f && this.creature.getFavor() > 10.0f) {
                        mod += 3.0f;
                    }
                    if (this.creature.getCultist() != null && this.creature.getCultist().healsFaster()) {
                        mod += 3.0f;
                    }
                    if (Tiles.getTile(Tiles.decodeType(tn = this.creature.getCurrentTileNum())).isEnchanted()) {
                        mod += 2.0f;
                    }
                } else {
                    this.setInfectionSeverity(0.0f);
                    mod += 3.0f;
                }
                if (this.type == 7 && this.creature.getPositionZ() + this.creature.getAltOffZ() > 0.0f) {
                    mod = 100.0f;
                }
                if (this.creature.getSpellEffects() != null && this.creature.getSpellEffects().getSpellEffect((byte)75) != null) {
                    mod += 5.0f;
                }
            }
            if (this.creature != null) {
                if (this.creature.getCitizenVillage() != null && this.creature.getCitizenVillage().getFaithHealBonus() > 0.0f && mod > 0.0f) {
                    mod *= 1.0f + this.creature.getCitizenVillage().getFaithHealBonus() / 100.0f;
                }
                if (mod > 0.0f) {
                    mod *= 1.0f + ItemBonus.getHealingBonus(this.creature);
                    if (this.creature.isPlayer() && (float)this.creature.getStatus().damage > 63568.953f) {
                        this.creature.achievement(36);
                    }
                }
                if (ItemBonus.getHealingBonus(this.creature) > 0.0f) {
                    mod += 1.0f;
                }
            }
            if (!hasWoundIncreasePrevention || mod > 0.0f) {
                this.modifySeverity((int)(-655.0f * mod));
            }
            if (this.creature != null && !this.creature.isUnique()) {
                this.checkInfection();
                this.checkPoison();
            }
            this.setLastPolled(System.currentTimeMillis());
        }
    }

    private void checkPoison() {
        if (this.poisonSeverity > 0.0f) {
            this.setPoisonSeverity(this.poisonSeverity + (float)Server.rand.nextInt(18) - 10.0f);
            if (this.poisonSeverity >= 100.0f) {
                if (this.creature != null) {
                    this.creature.getCommunicator().sendAlertServerMessage("The poison reaches your heart!", (byte)2);
                    Server.getInstance().broadCastAction(this.creature.getName() + " falls down dead, poisoned.", this.creature, 5);
                    this.creature.die(false, poison);
                } else {
                    logger.log(Level.WARNING, "Wound with id " + this.id + ", owner " + this.owner + " has no owner!", new Exception());
                }
            } else if (this.poisonSeverity > 50.0f) {
                this.creature.getCommunicator().sendAlertServerMessage("The poison burning in your veins makes you sweat!", (byte)2);
            } else {
                this.creature.getCommunicator().sendAlertServerMessage("Your wound aches and you feel feverish.", (byte)2);
            }
        }
    }

    private void setCreature() {
        try {
            this.creature = Server.getInstance().getCreature(this.owner);
            if (this.creature.isPlayer() && this.poisonSeverity > 0.0f) {
                this.creature.poisonChanged(false, this);
            }
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, "Creature not found for this wound " + nsc.getMessage(), nsc);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, "Player not found for this wound " + nsp.getMessage(), nsp);
        }
        catch (Exception nsw) {
            logger.log(Level.WARNING, "Wound not found " + nsw.getMessage(), nsw);
        }
    }

    private void checkInfection() {
        int r = 0;
        if (this.type == 1 || this.type == 2) {
            r = 100;
        } else if (this.type == 3) {
            r = 100;
        }
        if (this.type == 6) {
            r = 100;
        }
        if (r > 0) {
            int rand = Server.rand.nextInt(r);
            if (rand == 0) {
                if (this.infectionSeverity == 0.0f) {
                    this.setInfectionSeverity(10.0f);
                    this.addModifier(1);
                }
            } else if (this.infectionSeverity != 0.0f && (double)rand > (double)r * 0.7) {
                this.setInfectionSeverity(this.infectionSeverity - (float)rand / 10.0f);
                DoubleValueModifier val = (DoubleValueModifier)this.getModifier(1);
                if (val != null) {
                    if (this.infectionSeverity > 0.0f) {
                        val.setModifier(this.infectionSeverity);
                    } else {
                        this.removeModifier(1);
                    }
                }
            } else if (this.infectionSeverity != 0.0f && this.infectionSeverity > 0.0f) {
                if (rand % 2 == 0) {
                    this.setInfectionSeverity(this.infectionSeverity + (float)rand / 10.0f);
                } else {
                    this.setInfectionSeverity(this.infectionSeverity - (float)rand / 20.0f);
                }
                DoubleValueModifier val = (DoubleValueModifier)this.getModifier(1);
                if (val != null) {
                    if (this.infectionSeverity > 0.0f) {
                        val.setModifier(this.infectionSeverity);
                    } else {
                        this.removeModifier(1);
                    }
                }
            }
        }
    }

    public final int getWoundIconId() {
        if (this.poisonSeverity > 0.0f) {
            return 86;
        }
        switch (this.type) {
            case 4: {
                return 82;
            }
            case 1: {
                return 83;
            }
            case 3: {
                return 80;
            }
            case 2: {
                return 84;
            }
            case 0: {
                return 81;
            }
            case 9: {
                return 89;
            }
            case 7: {
                return 88;
            }
            case 6: {
                return 85;
            }
            case 8: {
                return 87;
            }
            case 5: {
                return 86;
            }
            case 10: {
                return 90;
            }
        }
        return 81;
    }

    public final String getName() {
        if (this.poisonSeverity > 0.0f) {
            return poison;
        }
        return Wound.getName(this.type);
    }

    public static final String getName(byte type) {
        switch (type) {
            case 4: {
                return burn;
            }
            case 1: {
                return cut;
            }
            case 3: {
                return bite;
            }
            case 2: {
                return hole;
            }
            case 0: {
                return bruise;
            }
            case 9: {
                return inter;
            }
            case 7: {
                return drown;
            }
            case 6: {
                return infection;
            }
            case 8: {
                return coldburn;
            }
            case 5: {
                return poison;
            }
            case 10: {
                return acid;
            }
        }
        return wound;
    }

    public final int getNumBandagesNeeded() {
        if (this.severity < 9825.0f) {
            return 1;
        }
        if (this.severity < 19650.0f) {
            return 2;
        }
        if (this.severity < 29475.0f) {
            return 4;
        }
        return 8;
    }

    public final String getDescription() {
        String toReturn = severe;
        if (this.severity < 3275.0f) {
            toReturn = verylight;
        } else if (this.severity < 9825.0f) {
            toReturn = light;
        } else if (this.severity < 19650.0f) {
            toReturn = medium;
        } else if (this.severity < 29475.0f) {
            toReturn = bad;
        }
        if (!this.isInternal() && this.isBandaged) {
            toReturn = toReturn + bandaged;
        } else if (this.isBandaged) {
            toReturn = toReturn + applied;
        }
        if (this.healEff > 0) {
            toReturn = toReturn + treated;
        }
        return toReturn;
    }

    public final String getWoundString() {
        String toReturn = "";
        if (this.poisonSeverity > 0.0f) {
            toReturn = this.poisonSeverity < 20.0f ? toReturn + poisonVeryLight : (this.poisonSeverity < 40.0f ? toReturn + poisonLight : (this.poisonSeverity < 60.0f ? toReturn + poisonMedium : (this.poisonSeverity < 80.0f ? toReturn + poisonBad : toReturn + poisonSevere)));
            toReturn = toReturn + " around ";
        }
        toReturn = this.type == 1 ? (this.severity < 3275.0f ? toReturn + slashVeryLight : (this.severity < 9825.0f ? toReturn + "a trickle of blood" : (this.severity < 19650.0f ? toReturn + slashMedium : (this.severity < 29475.0f ? toReturn + slashBad : toReturn + slashSevere)))) : (this.type == 2 ? (this.severity < 3275.0f ? toReturn + pierceVeryLight : (this.severity < 9825.0f ? toReturn + "a trickle of blood" : (this.severity < 19650.0f ? toReturn + pierceMedium : (this.severity < 29475.0f ? toReturn + pierceBad : toReturn + pierceSevere)))) : (this.type == 0 ? (this.severity < 3275.0f ? toReturn + crushVeryLight : (this.severity < 9825.0f ? toReturn + crushLight : (this.severity < 19650.0f ? toReturn + crushMedium : (this.severity < 29475.0f ? toReturn + crushBad : toReturn + crushSevere)))) : (this.type == 3 ? (this.severity < 3275.0f ? toReturn + biteVeryLight : (this.severity < 9825.0f ? toReturn + biteLight : (this.severity < 19650.0f ? toReturn + biteMedium : (this.severity < 29475.0f ? toReturn + biteBad : toReturn + biteSevere)))) : (this.type == 4 ? (this.severity < 3275.0f ? toReturn + burnVeryLight : (this.severity < 9825.0f ? toReturn + burnLight : (this.severity < 19650.0f ? toReturn + burnMedium : (this.severity < 29475.0f ? toReturn + burnBad : toReturn + burnSevere)))) : (this.type == 10 ? (this.severity < 3275.0f ? toReturn + acidVeryLight : (this.severity < 9825.0f ? toReturn + acidLight : (this.severity < 19650.0f ? toReturn + acidMedium : (this.severity < 29475.0f ? toReturn + acidBad : toReturn + acidSevere)))) : (this.type == 8 ? (this.severity < 9825.0f ? toReturn + coldLight : (this.severity < 29475.0f ? toReturn + coldMedium : toReturn + coldSevere)) : (this.type == 7 ? (this.severity < 9825.0f ? toReturn + waterVeryLight : (this.severity < 29475.0f ? toReturn + waterMedium : toReturn + waterSevere)) : (this.type == 9 ? (this.severity < 9825.0f ? toReturn + internalLight : (this.severity < 29475.0f ? toReturn + internalMedium : toReturn + internalSevere)) : toReturn + "a wound"))))))));
        if (this.infectionSeverity > 0.0f) {
            toReturn = toReturn + " ";
            toReturn = this.infectionSeverity < 20.0f ? toReturn + infectionVeryLight : (this.infectionSeverity < 40.0f ? toReturn + infectionLight : (this.infectionSeverity < 60.0f ? toReturn + infectionMedium : (this.infectionSeverity < 80.0f ? toReturn + infectionBad : toReturn + infectionSevere)));
        }
        if (this.creature != null && this.creature.getPower() >= 3) {
            toReturn = toReturn + " (" + this.severity + ")";
        }
        return toReturn;
    }

    public final long getWurmId() {
        return this.id;
    }

    public final boolean isBandaged() {
        return this.isBandaged;
    }

    public final boolean isTreated() {
        return this.healEff > 0;
    }

    abstract void create();

    abstract void setSeverity(float var1);

    public abstract void setPoisonSeverity(float var1);

    public abstract void setInfectionSeverity(float var1);

    public abstract void setBandaged(boolean var1);

    abstract void setLastPolled(long var1);

    public abstract void setHealeff(byte var1);

    abstract void delete();

    public static byte getFlagByte(boolean isBandaged, boolean isTreated) {
        byte flags = 0;
        flags = (byte)((isBandaged ? 1 : 0) << 1);
        flags = (byte)(flags & (isTreated ? 1 : 0) << 0);
        return (byte)(flags & 7);
    }
}


package com.wurmonline.server.bodys;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
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

public abstract class Wound implements CounterTypes, TimeConstants, MiscConstants, ModifierTypes {
   private final byte location;
   private final long id;
   private byte type;
   float severity;
   private final long owner;
   float poisonSeverity = 0.0F;
   float infectionSeverity = 0.0F;
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
   private static final float WOUNDMULTIPLIER = 50000.0F;
   private static final Logger logger = Logger.getLogger(Wound.class.getName());
   public static final float slowWoundMod = 4.0F;
   public static final float fastWoundMod = 5.0F;
   public static final float severityVeryLight = 3275.0F;
   public static final float severityLight = 9825.0F;
   public static final float severityMedium = 19650.0F;
   public static final float severityBad = 29475.0F;
   public static final float severitySevere = 60000.0F;
   public static final float effSeverityVeryLight = 20.0F;
   public static final float effSeverityLight = 40.0F;
   public static final float effSeverityMedium = 60.0F;
   public static final float effSeverityBad = 80.0F;
   public static final float effSeveritySevere = 100.0F;
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
   public static final float champDamageModifier = 0.4F;

   Wound(
      byte _type,
      byte _location,
      float _severity,
      long _owner,
      float _poisonSeverity,
      float _infectionSeverity,
      boolean isTemporary,
      boolean pvp,
      boolean spell
   ) {
      this.type = _type;
      this.location = _location;
      this.severity = _severity;
      this.severity = (float)((double)this.severity * this.getWoundMod());
      this.severity = (float)((int)this.severity);
      this.owner = _owner;
      this.poisonSeverity = _poisonSeverity;
      this.infectionSeverity = _infectionSeverity;
      this.lastPolled = System.currentTimeMillis();
      this.id = isTemporary ? WurmId.getNextTemporaryWoundId() : WurmId.getNextWoundId();
      this.setCreature();
      if (this.creature != null) {
         if ((this.type == 4 || this.type == 7 || this.type == 8) && this.creature.getCultist() != null && this.creature.getCultist().hasNoElementalDamage()) {
            this.severity = 0.0F;
         }

         this.severity *= this.creature.getDamageModifier(pvp, spell);
         if (this.creature.isChampion()) {
            this.severity = (float)((int)Math.max(1.0F, this.severity * 0.4F));
         }

         if (this.creature.isPlayer() && (this.location == 18 || this.location == 19) && this.severity > 29475.0F) {
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
   }

   Wound(
      long _id,
      byte _type,
      byte _location,
      float _severity,
      long _owner,
      float _poisonSeverity,
      float _infectionSeverity,
      long _lastPolled,
      boolean aBandaged,
      byte healeff
   ) {
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
         switch(woundType) {
            case 0:
               if (c.getCrushVulnerability() > 0.0F) {
                  return c.getCrushVulnerability();
               }
               break;
            case 1:
               if (c.getSlashVulnerability() > 0.0F) {
                  return c.getSlashVulnerability();
               }
               break;
            case 2:
               if (c.getPierceVulnerability() > 0.0F) {
                  return c.getPierceVulnerability();
               }
               break;
            case 3:
               if (c.getBiteVulnerability() > 0.0F) {
                  return c.getBiteVulnerability();
               }
               break;
            case 4:
               if (c.getFireVulnerability() > 0.0F) {
                  return c.getFireVulnerability();
               }
               break;
            case 5:
               if (c.getPoisonVulnerability() > 0.0F) {
                  return c.getPoisonVulnerability();
               }
               break;
            case 6:
               if (c.getDiseaseVulnerability() > 0.0F) {
                  return c.getDiseaseVulnerability();
               }
               break;
            case 7:
               if (c.getWaterVulnerability() > 0.0F) {
                  return c.getWaterVulnerability();
               }
               break;
            case 8:
               if (c.getColdVulnerability() > 0.0F) {
                  return c.getColdVulnerability();
               }
               break;
            case 9:
               if (c.getInternalVulnerability() > 0.0F) {
                  return c.getInternalVulnerability();
               }
               break;
            default:
               return 1.0F;
         }
      }

      return 1.0F;
   }

   public static float getResistModifier(@Nullable Creature attacker, Creature c, byte woundType) {
      float mod = 1.0F;
      if (attacker != null) {
         float resMult = EnchantUtil.getJewelryDamageIncrease(attacker, woundType);
         if (Servers.isThisATestServer() && resMult != 1.0F) {
            c.getCommunicator().sendCombatAlertMessage(String.format("Damage reduced to %.1f%% from jewelry enchants.", mod * 100.0F));
         }

         mod *= resMult;
      }

      float damMult = EnchantUtil.getJewelryResistModifier(c, woundType);
      mod *= damMult;
      if (Servers.isThisATestServer() && damMult != 1.0F) {
         c.getCommunicator().sendCombatAlertMessage(String.format("Damage increased to %.1f%% from jewelry enchants.", mod * 100.0F));
      }

      if (c.hasAnyAbility()) {
         float physMod = 1.0F;
         if (c.getPhysicalResistance() > 0.0F) {
            physMod = 1.0F - c.getPhysicalResistance();
         }

         switch(woundType) {
            case 0:
               if (c.getCrushResistance() > 0.0F) {
                  mod *= c.getCrushResistance() * physMod;
               }

               if (c.getCrushVulnerability() > 0.0F) {
                  mod *= c.getCrushVulnerability();
               }

               mod *= physMod;
               break;
            case 1:
               if (c.getSlashResistance() > 0.0F) {
                  mod *= c.getSlashResistance();
               }

               if (c.getSlashVulnerability() > 0.0F) {
                  mod *= c.getSlashVulnerability();
               }

               mod *= physMod;
               break;
            case 2:
               if (c.getPierceResistance() > 0.0F) {
                  mod *= c.getPierceResistance();
               }

               if (c.getPierceVulnerability() > 0.0F) {
                  mod *= c.getPierceVulnerability();
               }

               mod *= physMod;
               break;
            case 3:
               if (c.getBiteResistance() > 0.0F) {
                  mod *= c.getBiteResistance();
               }

               if (c.getBiteVulnerability() > 0.0F) {
                  mod *= c.getBiteVulnerability();
               }

               mod *= physMod;
               break;
            case 4:
               if (c.getFireResistance() > 0.0F) {
                  mod *= c.getFireResistance();
               }

               if (c.getFireVulnerability() > 0.0F) {
                  mod *= c.getFireVulnerability();
               }
               break;
            case 5:
               if (c.getPoisonResistance() > 0.0F) {
                  mod *= c.getPoisonResistance();
               }

               if (c.getPoisonVulnerability() > 0.0F) {
                  mod *= c.getPoisonVulnerability();
               }
               break;
            case 6:
               if (c.getDiseaseResistance() > 0.0F) {
                  mod *= c.getDiseaseResistance();
               }

               if (c.getDiseaseVulnerability() > 0.0F) {
                  mod *= c.getDiseaseVulnerability();
               }
               break;
            case 7:
               if (c.getWaterResistance() > 0.0F) {
                  mod *= c.getWaterResistance();
               }

               if (c.getWaterVulnerability() > 0.0F) {
                  mod *= c.getWaterVulnerability();
               }
               break;
            case 8:
               if (c.getColdResistance() > 0.0F) {
                  mod *= c.getColdResistance();
               }

               if (c.getColdVulnerability() > 0.0F) {
                  mod *= c.getColdVulnerability();
               }
               break;
            case 9:
               if (c.getInternalResistance() > 0.0F) {
                  mod *= c.getInternalResistance();
               }

               if (c.getInternalVulnerability() > 0.0F) {
                  mod *= c.getInternalVulnerability();
               }
         }
      }

      return mod;
   }

   public boolean isPoison() {
      return this.poisonSeverity > 0.0F;
   }

   public boolean isInternal() {
      return this.type == 9 || this.type == 5;
   }

   public boolean isBruise() {
      return this.severity < 19650.0F && this.type == 0;
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
         this.modifiers = new HashSet<>();
      }

      ValueModifier modifier = null;
      if (_type == 2) {
         int w = Wounds.getModifiedSkill(this.location, this.type);
         if (w != -1) {
            try {
               Creature _creature = Server.getInstance().getCreature(this.owner);
               float champMod = 1.0F;
               if (_creature.isChampion()) {
                  champMod = 2.5F;
               }

               modifier = new DoubleValueModifier(2, (double)(champMod * -this.severity / 50000.0F));
               _creature.getSkills().getSkill(w).addModifier((DoubleValueModifier)modifier);
            } catch (NoSuchPlayerException var16) {
               logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
               modifier = null;
            } catch (NoSuchCreatureException var17) {
               logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
               modifier = null;
            } catch (NoSuchSkillException var18) {
               modifier = null;
            }
         }
      } else if (_type == 1) {
         if (this.infectionSeverity > 0.0F) {
            try {
               Creature _creature = Server.getInstance().getCreature(this.owner);
               float champMod = 1.0F;
               if (_creature.isChampion()) {
                  champMod = 2.5F;
               }

               modifier = new DoubleValueModifier(1, (double)(champMod * -this.infectionSeverity / 500.0F));
               _creature.getStatus().addModifier((DoubleValueModifier)modifier);
            } catch (NoSuchPlayerException var14) {
               logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
               modifier = null;
            } catch (NoSuchCreatureException var15) {
               logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
               modifier = null;
            }
         }
      } else if (_type == 3) {
         if (this.impairsMovement()) {
            try {
               double mod = 0.3F;
               if (this.location == 15 || this.location == 16) {
                  mod = 0.45F;
               }

               Creature _creature = Server.getInstance().getCreature(this.owner);
               float champMod = 1.0F;
               if (_creature.isChampion()) {
                  champMod = 1.1F;
               }

               modifier = new DoubleValueModifier(3, (double)(champMod * -this.severity / 50000.0F) * mod);
               MovementScheme scheme = _creature.getMovementScheme();
               scheme.addModifier((DoubleValueModifier)modifier);
               modifier.addListener(scheme);
            } catch (NoSuchPlayerException var19) {
               logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
               modifier = null;
            } catch (NoSuchCreatureException var20) {
               logger.log(Level.WARNING, var20.getMessage(), (Throwable)var20);
               modifier = null;
            }
         }
      } else if (_type == 4) {
         if (this.location == 18 || this.location == 19) {
            try {
               Creature _creature = Server.getInstance().getCreature(this.owner);
               float champMod = 1.0F;
               if (_creature.isChampion()) {
                  champMod = 2.5F;
               }

               modifier = new DoubleValueModifier(4, (double)(champMod * this.severity / 50000.0F));
               _creature.addVisionModifier((DoubleValueModifier)modifier);
            } catch (NoSuchPlayerException var12) {
               logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
               modifier = null;
            } catch (NoSuchCreatureException var13) {
               logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
               modifier = null;
            }
         }
      } else if (_type == 5) {
         if (this.location == 4) {
            try {
               Creature _creature = Server.getInstance().getCreature(this.owner);
               float champMod = 1.0F;
               if (_creature.isChampion()) {
                  champMod = 2.5F;
               }

               modifier = new DoubleValueModifier(5, (double)(champMod * this.severity / 50000.0F));
               _creature.getCombatHandler().addParryModifier((DoubleValueModifier)modifier);
            } catch (NoSuchPlayerException var10) {
               logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
               modifier = null;
            } catch (NoSuchCreatureException var11) {
               logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
               modifier = null;
            }
         }
      } else if (_type == 6 && this.location == 23) {
         try {
            Creature _creature = Server.getInstance().getCreature(this.owner);
            float champMod = 1.0F;
            if (_creature.isChampion()) {
               champMod = 2.5F;
            }

            modifier = new DoubleValueModifier(6, (double)(champMod * this.severity / 50000.0F));
            _creature.getCombatHandler().addDodgeModifier((DoubleValueModifier)modifier);
         } catch (NoSuchPlayerException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            modifier = null;
         } catch (NoSuchCreatureException var9) {
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
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
      } catch (NoSuchPlayerException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
      } catch (NoSuchCreatureException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      } catch (NoSuchSkillException var6) {
      }
   }

   private void removeModifier(int _type) {
      if (this.modifiers != null) {
         ValueModifier[] mods = this.getModifiers();

         for(int x = 0; x < mods.length; ++x) {
            if (mods[x].getType() == _type) {
               this.removeModifier(mods[x]);
            }
         }
      }
   }

   private ValueModifier getModifier(int _type) {
      if (this.modifiers != null) {
         ValueModifier[] mods = this.getModifiers();

         for(int x = 0; x < mods.length; ++x) {
            if (mods[x].getType() == _type) {
               return mods[x];
            }
         }
      }

      return null;
   }

   private ValueModifier[] getModifiers() {
      return this.modifiers != null ? this.modifiers.toArray(new ValueModifier[this.modifiers.size()]) : null;
   }

   final void removeAllModifiers() {
      ValueModifier[] mods = this.getModifiers();
      if (mods != null) {
         for(int x = 0; x < mods.length; ++x) {
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
      if (this.severity < 3275.0F) {
         toReturn = ProtoConstants.WoundSeverity.verylight;
      } else if (this.severity < 9825.0F) {
         toReturn = ProtoConstants.WoundSeverity.light;
      } else if (this.severity < 19650.0F) {
         toReturn = ProtoConstants.WoundSeverity.medium;
      } else if (this.severity < 29475.0F) {
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
         this.setPoisonSeverity(0.0F);
      }

      return true;
   }

   public final boolean cureInfection() {
      if (this.type == 6) {
         this.heal();
      } else {
         this.setInfectionSeverity(0.0F);
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
      boolean dead = false;
      if (this.creature != null) {
         num = (int)((double)num * this.getWoundMod());
         if (num > 0) {
            num = (int)((float)num * this.creature.getDamageModifier(pvp, spell));
            if (this.creature.isChampion()) {
               num = (int)Math.max(1.0F, (float)num * 0.4F);
            }
         }

         Body body = this.creature.getBody();
         float sev = this.severity + (float)num;
         if (sev <= 0.0F) {
            this.creature.getStatus().modifyWounds((int)(-this.severity));
            body.removeWound(this);
         } else {
            this.setSeverity(sev);
            if (num > 0 && this.severity > 1000.0F) {
               this.creature.maybeInterruptAction((int)this.severity);
            }

            float champMod = 1.0F;
            if (this.creature.isChampion()) {
               champMod = 2.5F;
            }

            dead = this.creature.getStatus().modifyWounds(num);
            DoubleValueModifier val = (DoubleValueModifier)this.getModifier(2);
            if (val != null) {
               val.setModifier((double)(champMod * -this.severity / 50000.0F));
            }

            val = (DoubleValueModifier)this.getModifier(6);
            if (val != null) {
               val.setModifier((double)(champMod * this.severity / 50000.0F));
            }

            val = (DoubleValueModifier)this.getModifier(5);
            if (val != null) {
               val.setModifier((double)(champMod * this.severity / 50000.0F));
            }

            val = (DoubleValueModifier)this.getModifier(4);
            if (val != null) {
               val.setModifier((double)(champMod * this.severity / 50000.0F));
            }

            val = (DoubleValueModifier)this.getModifier(3);
            if (val != null) {
               double mod = 0.3F;
               if (this.location == 15 || this.location == 16) {
                  mod = 0.45F;
               }

               if (this.creature.isChampion()) {
                  champMod = 1.1F;
               }

               val.setModifier((double)(champMod * -this.severity / 50000.0F) * mod);
            }

            if (!dead) {
               try {
                  if (this.creature != null) {
                     if (this.creature.getBody() != null) {
                        Item bodypart = this.creature.getBody().getBodyPartForWound(this);

                        try {
                           Creature[] watchers = bodypart.getWatchers();

                           for(int x = 0; x < watchers.length; ++x) {
                              watchers[x].getCommunicator().sendUpdateWound(this, bodypart);
                           }
                        } catch (NoSuchCreatureException var12) {
                        }
                     } else {
                        logger.log(Level.WARNING, this.creature.getName() + " body is null.", (Throwable)(new Exception()));
                     }
                  }
               } catch (NoSpaceException var13) {
                  logger.log(Level.INFO, var13.getMessage(), (Throwable)var13);
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
      if (System.currentTimeMillis() - this.lastPolled > 600000L
         || (this.creature == null || !this.creature.isUnique()) && this.type == 6 && System.currentTimeMillis() - this.lastPolled > 60000L) {
         float mod = 5.0F;
         if (this.severity > 3275.0F) {
            mod = -5.0F;
            if (this.severity < 9825.0F) {
               mod = 4.0F;
            } else if (this.severity < 19650.0F) {
               mod = 0.0F;
            } else if (this.severity < 29475.0F) {
               mod = -4.0F;
            }
         }

         if (this.healEff > 0) {
            mod += (float)this.healEff / 2.0F;
         }

         if (this.isBandaged) {
            ++mod;
         }

         if (this.type == 7) {
            mod += 3.0F;
         } else if (this.type == 9) {
            mod += 10.0F;
         }

         if (this.type == 10) {
            mod -= 3.0F;
         }

         if (this.type == 6 && !this.isBandaged() && !this.isTreated()) {
            mod -= 5.0F;
         }

         if (this.creature != null) {
            if (!this.creature.isUnique()) {
               if (this.creature.getStatus().getNutritionlevel() > 0.6F) {
                  ++mod;
               } else if (this.creature.getStatus().getNutritionlevel() < 0.4F) {
                  --mod;
               }

               if (this.creature.getStatus().fat > 70) {
                  ++mod;
               } else if (this.creature.getStatus().fat < 30) {
                  --mod;
               }

               if (this.infectionSeverity > 0.0F) {
                  int rand = Server.rand.nextInt(100 + this.healEff);
                  if ((float)rand < this.infectionSeverity) {
                     mod -= Math.max(1.0F, this.infectionSeverity / 10.0F);
                  }
               }

               if (this.creature != null
                  && this.creature.getDeity() != null
                  && this.creature.getDeity().isHealer()
                  && this.creature.getFaith() >= 20.0F
                  && this.creature.getFavor() > 10.0F) {
                  mod += 3.0F;
               }

               if (this.creature.getCultist() != null && this.creature.getCultist().healsFaster()) {
                  mod += 3.0F;
               }

               int tn = this.creature.getCurrentTileNum();
               if (Tiles.getTile(Tiles.decodeType(tn)).isEnchanted()) {
                  mod += 2.0F;
               }
            } else {
               this.setInfectionSeverity(0.0F);
               mod += 3.0F;
            }

            if (this.type == 7 && this.creature.getPositionZ() + this.creature.getAltOffZ() > 0.0F) {
               mod = 100.0F;
            }

            if (this.creature.getSpellEffects() != null && this.creature.getSpellEffects().getSpellEffect((byte)75) != null) {
               mod += 5.0F;
            }
         }

         if (this.creature != null) {
            if (this.creature.getCitizenVillage() != null && this.creature.getCitizenVillage().getFaithHealBonus() > 0.0F && mod > 0.0F) {
               mod *= 1.0F + this.creature.getCitizenVillage().getFaithHealBonus() / 100.0F;
            }

            if (mod > 0.0F) {
               mod *= 1.0F + ItemBonus.getHealingBonus(this.creature);
               if (this.creature.isPlayer() && (float)this.creature.getStatus().damage > 63568.953F) {
                  this.creature.achievement(36);
               }
            }

            if (ItemBonus.getHealingBonus(this.creature) > 0.0F) {
               ++mod;
            }
         }

         if (!hasWoundIncreasePrevention || mod > 0.0F) {
            this.modifySeverity((int)(-655.0F * mod));
         }

         if (this.creature != null && !this.creature.isUnique()) {
            this.checkInfection();
            this.checkPoison();
         }

         this.setLastPolled(System.currentTimeMillis());
      }
   }

   private void checkPoison() {
      if (this.poisonSeverity > 0.0F) {
         this.setPoisonSeverity(this.poisonSeverity + (float)Server.rand.nextInt(18) - 10.0F);
         if (this.poisonSeverity >= 100.0F) {
            if (this.creature != null) {
               this.creature.getCommunicator().sendAlertServerMessage("The poison reaches your heart!", (byte)2);
               Server.getInstance().broadCastAction(this.creature.getName() + " falls down dead, poisoned.", this.creature, 5);
               this.creature.die(false, "Poison");
            } else {
               logger.log(Level.WARNING, "Wound with id " + this.id + ", owner " + this.owner + " has no owner!", (Throwable)(new Exception()));
            }
         } else if (this.poisonSeverity > 50.0F) {
            this.creature.getCommunicator().sendAlertServerMessage("The poison burning in your veins makes you sweat!", (byte)2);
         } else {
            this.creature.getCommunicator().sendAlertServerMessage("Your wound aches and you feel feverish.", (byte)2);
         }
      }
   }

   private void setCreature() {
      try {
         this.creature = Server.getInstance().getCreature(this.owner);
         if (this.creature.isPlayer() && this.poisonSeverity > 0.0F) {
            this.creature.poisonChanged(false, this);
         }
      } catch (NoSuchCreatureException var2) {
         logger.log(Level.WARNING, "Creature not found for this wound " + var2.getMessage(), (Throwable)var2);
      } catch (NoSuchPlayerException var3) {
         logger.log(Level.WARNING, "Player not found for this wound " + var3.getMessage(), (Throwable)var3);
      } catch (Exception var4) {
         logger.log(Level.WARNING, "Wound not found " + var4.getMessage(), (Throwable)var4);
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
            if (this.infectionSeverity == 0.0F) {
               this.setInfectionSeverity(10.0F);
               this.addModifier(1);
            }
         } else if (this.infectionSeverity != 0.0F && (double)rand > (double)r * 0.7) {
            this.setInfectionSeverity(this.infectionSeverity - (float)rand / 10.0F);
            DoubleValueModifier val = (DoubleValueModifier)this.getModifier(1);
            if (val != null) {
               if (this.infectionSeverity > 0.0F) {
                  val.setModifier((double)this.infectionSeverity);
               } else {
                  this.removeModifier(1);
               }
            }
         } else if (this.infectionSeverity != 0.0F && this.infectionSeverity > 0.0F) {
            if (rand % 2 == 0) {
               this.setInfectionSeverity(this.infectionSeverity + (float)rand / 10.0F);
            } else {
               this.setInfectionSeverity(this.infectionSeverity - (float)rand / 20.0F);
            }

            DoubleValueModifier val = (DoubleValueModifier)this.getModifier(1);
            if (val != null) {
               if (this.infectionSeverity > 0.0F) {
                  val.setModifier((double)this.infectionSeverity);
               } else {
                  this.removeModifier(1);
               }
            }
         }
      }
   }

   public final int getWoundIconId() {
      if (this.poisonSeverity > 0.0F) {
         return 86;
      } else {
         switch(this.type) {
            case 0:
               return 81;
            case 1:
               return 83;
            case 2:
               return 84;
            case 3:
               return 80;
            case 4:
               return 82;
            case 5:
               return 86;
            case 6:
               return 85;
            case 7:
               return 88;
            case 8:
               return 87;
            case 9:
               return 89;
            case 10:
               return 90;
            default:
               return 81;
         }
      }
   }

   public final String getName() {
      return this.poisonSeverity > 0.0F ? "Poison" : getName(this.type);
   }

   public static final String getName(byte type) {
      switch(type) {
         case 0:
            return "Bruise";
         case 1:
            return "Cut";
         case 2:
            return "Hole";
         case 3:
            return "Bite";
         case 4:
            return "Burn";
         case 5:
            return "Poison";
         case 6:
            return "Infection";
         case 7:
            return "Water";
         case 8:
            return "Coldburn";
         case 9:
            return "Internal";
         case 10:
            return "Acid";
         default:
            return "Wound";
      }
   }

   public final int getNumBandagesNeeded() {
      if (this.severity < 9825.0F) {
         return 1;
      } else if (this.severity < 19650.0F) {
         return 2;
      } else {
         return this.severity < 29475.0F ? 4 : 8;
      }
   }

   public final String getDescription() {
      String toReturn = "severe";
      if (this.severity < 3275.0F) {
         toReturn = "very light";
      } else if (this.severity < 9825.0F) {
         toReturn = "light";
      } else if (this.severity < 19650.0F) {
         toReturn = "medium";
      } else if (this.severity < 29475.0F) {
         toReturn = "bad";
      }

      if (!this.isInternal() && this.isBandaged) {
         toReturn = toReturn + ", bandaged";
      } else if (this.isBandaged) {
         toReturn = toReturn + ", applied";
      }

      if (this.healEff > 0) {
         toReturn = toReturn + ", treated";
      }

      return toReturn;
   }

   public final String getWoundString() {
      String toReturn = "";
      if (this.poisonSeverity > 0.0F) {
         if (this.poisonSeverity < 20.0F) {
            toReturn = toReturn + "a faint dark aura";
         } else if (this.poisonSeverity < 40.0F) {
            toReturn = toReturn + "a worrying dark aura";
         } else if (this.poisonSeverity < 60.0F) {
            toReturn = toReturn + "an ominous dark aura";
         } else if (this.poisonSeverity < 80.0F) {
            toReturn = toReturn + "blue-black aura miscolouring the veins";
         } else {
            toReturn = toReturn + "black veins running from";
         }

         toReturn = toReturn + " around ";
      }

      if (this.type == 1) {
         if (this.severity < 3275.0F) {
            toReturn = toReturn + "a small bleeding scar";
         } else if (this.severity < 9825.0F) {
            toReturn = toReturn + "a trickle of blood";
         } else if (this.severity < 19650.0F) {
            toReturn = toReturn + "a cut";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "a severe cut";
         } else {
            toReturn = toReturn + "a wide gap of cut tissue";
         }
      } else if (this.type == 2) {
         if (this.severity < 3275.0F) {
            toReturn = toReturn + "a small bleeding pinch";
         } else if (this.severity < 9825.0F) {
            toReturn = toReturn + "a trickle of blood";
         } else if (this.severity < 19650.0F) {
            toReturn = toReturn + "a small hole";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "a deep hole";
         } else {
            toReturn = toReturn + "a straight-through gaping hole";
         }
      } else if (this.type == 0) {
         if (this.severity < 3275.0F) {
            toReturn = toReturn + "a small bruise";
         } else if (this.severity < 9825.0F) {
            toReturn = toReturn + "a bruise";
         } else if (this.severity < 19650.0F) {
            toReturn = toReturn + "an aching bruise";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "a severe fracture";
         } else {
            toReturn = toReturn + "splinters of crushed bone";
         }
      } else if (this.type == 3) {
         if (this.severity < 3275.0F) {
            toReturn = toReturn + "a bruise from a bite";
         } else if (this.severity < 9825.0F) {
            toReturn = toReturn + "a light bite";
         } else if (this.severity < 19650.0F) {
            toReturn = toReturn + "holes from a bite";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "a large bitewound";
         } else {
            toReturn = toReturn + "a huge bitewound";
         }
      } else if (this.type == 4) {
         if (this.severity < 3275.0F) {
            toReturn = toReturn + "a few blisters";
         } else if (this.severity < 9825.0F) {
            toReturn = toReturn + "a lot of blisters on red skin";
         } else if (this.severity < 19650.0F) {
            toReturn = toReturn + "some black and red burnt skin";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "some melted skin";
         } else {
            toReturn = toReturn + "black and red melted and loose skin";
         }
      } else if (this.type == 10) {
         if (this.severity < 3275.0F) {
            toReturn = toReturn + "a few moist blisters";
         } else if (this.severity < 9825.0F) {
            toReturn = toReturn + "a lot of moist blisters on red skin";
         } else if (this.severity < 19650.0F) {
            toReturn = toReturn + "some watery red burnt skin";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "some oozing melted skin";
         } else {
            toReturn = toReturn + "black and bubbling melted and loose skin";
         }
      } else if (this.type == 8) {
         if (this.severity < 9825.0F) {
            toReturn = toReturn + "a reddish tone";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "white flecks";
         } else {
            toReturn = toReturn + "black skin with possible gangrene";
         }
      } else if (this.type == 7) {
         if (this.severity < 9825.0F) {
            toReturn = toReturn + "some coughing and choking";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "water in the lungs";
         } else {
            toReturn = toReturn + "waterfilled lungs, causing severe breathing difficulties";
         }
      } else if (this.type == 9) {
         if (this.severity < 9825.0F) {
            toReturn = toReturn + "small tingle";
         } else if (this.severity < 29475.0F) {
            toReturn = toReturn + "throbbing ache";
         } else {
            toReturn = toReturn + "excruciating pain";
         }
      } else {
         toReturn = toReturn + "a wound";
      }

      if (this.infectionSeverity > 0.0F) {
         toReturn = toReturn + " ";
         if (this.infectionSeverity < 20.0F) {
            toReturn = toReturn + "with faintly miscolored edges";
         } else if (this.infectionSeverity < 40.0F) {
            toReturn = toReturn + "with worryingly deep red edges";
         } else if (this.infectionSeverity < 60.0F) {
            toReturn = toReturn + "with ominously red edges and some yellow pus";
         } else if (this.infectionSeverity < 80.0F) {
            toReturn = toReturn + "covered in yellow pus";
         } else {
            toReturn = toReturn + "rotting from infection";
         }
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

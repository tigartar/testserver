package com.wurmonline.server.creatures;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.loot.LootPool;
import com.wurmonline.server.loot.LootTable;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class CreatureTemplate implements CreatureTemplateIds, CreatureTypes, MiscConstants, SoundNames, ItemMaterials, Comparable<CreatureTemplate> {
   private final String name;
   private final String plural;
   private static final Logger logger = Logger.getLogger(CreatureTemplate.class.getName());
   private final byte bodyType;
   private final byte sex;
   private final Skills skills;
   private short vision;
   private final short centimetersHigh;
   private final short centimetersLong;
   private final short centimetersWide;
   private final int size;
   private final String longDesc;
   private final String modelName;
   public final int id;
   private final int reputation = 100;
   private final String deathSoundMale;
   private final String hitSoundMale;
   private final String deathSoundFemale;
   private final String hitSoundFemale;
   private final byte meatMaterial;
   static final String BITE_DAMAGE_STRING = "bite";
   static final String BREATHE_DAMAGE_STRING = "breathe";
   static final String BURN_DAMAGE_STRING = "burn";
   static final String POISON_DAMAGE_STRING = "poison";
   static final String CLAW_DAMAGE_STRING = "claw";
   static final String HEAD_BUTT_DAMAGE_STRING = "headbutt";
   static final String HIT_DAMAGE_STRING = "hit";
   static final String KICK_DAMAGE_STRING = "kick";
   static final String MAUL_DAMAGE_STRING = "maul";
   static final String SQUEEZE_DAMAGE_STRING = "squeeze";
   static final String STING_DAMAGE_STRING = "sting";
   static final String TAIL_WHIP_DAMAGE_STRING = "tailwhip";
   static final String WING_BUFF_DAMAGE_STRING = "wingbuff";
   int abilityTitle = -1;
   long abilities = -10L;
   private String denName = null;
   private byte denMaterial = 14;
   private String handDamString = "hit";
   private String biteDamString = "bite";
   private String kickDamString = "kick";
   private String headbuttDamString = "headbutt";
   private String breathDamString = "breathe";
   private final int aggressivity;
   private float alignment = 0.0F;
   private final Deity deity = null;
   private float faith = 0.0F;
   private String corpsename;
   private final float naturalArmour;
   private ArmourTemplate.ArmourType armourType = ArmourTemplate.ARMOUR_TYPE_CLOTH;
   private boolean sentinel = false;
   private boolean trader = false;
   private boolean moveRandom = false;
   private boolean animal = false;
   private boolean human = false;
   private boolean monster = false;
   private boolean invulnerable = false;
   private boolean npcTrader = false;
   private boolean onlyAttacksPlayers = false;
   private boolean aggHuman = false;
   private boolean moveLocal = false;
   private boolean moveGlobal = false;
   private boolean grazer = false;
   private boolean herd = false;
   private boolean villageGuard = false;
   private boolean swimming = false;
   private boolean hunter = false;
   private boolean leadable = false;
   private boolean milkable = false;
   private boolean regenerating = false;
   private boolean dragon = false;
   private boolean kingdomGuard = false;
   private boolean spiritGuard = false;
   private boolean ghost = false;
   private boolean bartender = false;
   private boolean defendKingdom = false;
   private boolean isWarGuard = false;
   private boolean aggWhitie = false;
   private boolean herbivore = false;
   private boolean carnivore = false;
   private boolean omnivore = false;
   public boolean climber = false;
   private boolean dominatable = false;
   private boolean undead = false;
   private boolean caveDweller = false;
   private boolean eggLayer = false;
   private int eggTemplateId = -1;
   private int childTemplateId = -1;
   private static final int[] emptyMoves = EMPTY_INT_ARRAY;
   private int[] combatMoves = emptyMoves;
   private boolean subterranean = false;
   private boolean isNoSkillgain = false;
   private boolean isSubmerged = false;
   private final boolean royalAspiration;
   private boolean isFloating = false;
   public float offZ = 0.0F;
   private boolean isBreakFence = false;
   private final float handDamage;
   private int mateTemplateId = -1;
   private final float biteDamage;
   private final float kickDamage;
   private final float headButtDamage;
   private final float breathDamage;
   private final float speed;
   private int moveRate;
   private final int[] butcheredItems;
   private final int maxHuntDistance;
   private boolean unique = false;
   private int leaderTemplateId = -1;
   private int adultFemaleTemplateId = -1;
   private int adultMaleTemplateId = -1;
   public boolean keepSex = false;
   private boolean fleeing = false;
   private int maxGroupAttackSize = 1;
   private boolean tutorial = false;
   private int maxAge = Integer.MAX_VALUE;
   public float baseCombatRating = 1.0F;
   private float bonusCombatRating = 1.0F;
   public byte combatDamageType = 0;
   public int colorRed = 255;
   private int colorGreen = 255;
   private int colorBlue = 255;
   private String[] colourNameOverrides = new String[]{
      "grey",
      "brown",
      "gold",
      "black",
      "white",
      "piebaldPinto",
      "bloodBay",
      "ebonyBlack",
      "skewbaldpinto",
      "goldbuckskin",
      "blacksilver",
      "appaloosa",
      "chestnut"
   };
   public int maxColourCount = 1;
   private boolean glowing = false;
   private int paintMode = 1;
   private int sizeModX = 64;
   private int sizeModY = 64;
   private int sizeModZ = 64;
   private boolean isOnFire = false;
   private byte fireRadius = 0;
   private boolean isDetectInvis = false;
   public boolean nonNewbie = false;
   boolean isVehicle = false;
   boolean isHorse = false;
   boolean hasHands = false;
   byte daysOfPregnancy = 5;
   public boolean domestic = false;
   public boolean isBlackOrWhite = false;
   public boolean isColoured = false;
   private boolean careful = false;
   private boolean canOpenDoors = false;
   private boolean noCorpse = false;
   public float fireResistance;
   public float coldResistance;
   public float diseaseResistance;
   public float physicalResistance;
   public float pierceResistance;
   public float slashResistance;
   public float crushResistance;
   public float biteResistance;
   public float poisonResistance;
   public float waterResistance;
   public float acidResistance;
   public float internalResistance;
   public float fireVulnerability;
   public float coldVulnerability;
   public float diseaseVulnerability;
   public float physicalVulnerability;
   public float pierceVulnerability;
   public float slashVulnerability;
   public float crushVulnerability;
   public float biteVulnerability;
   public float poisonVulnerability;
   public float waterVulnerability;
   public float acidVulnerability;
   public float internalVulnerability;
   private boolean towerBasher = false;
   public boolean attacksVehicles = true;
   public boolean isPrey = false;
   public boolean isFromValrei = false;
   public boolean isBeachDweller = false;
   private float maxPercentOfCreatures = 0.01F;
   private int maxPopulationOfCreatures = 0;
   private boolean usesMaxPopulation = false;
   private boolean woolProducer = false;
   private boolean burning = false;
   private boolean riftCreature = false;
   private boolean isStealth = false;
   private boolean isCaster = false;
   private boolean isSummoner = false;
   private boolean useNewAttacks = false;
   private final List<AttackAction> primaryAttacks = new ArrayList<>();
   private final List<AttackAction> secondaryAttacks = new ArrayList<>();
   private boolean isEpicSlayable = false;
   private boolean isEpicTraitor = false;
   private boolean isMissionDisabled = false;
   private boolean isNotRebirthable = false;
   private boolean isBabyCreature = false;
   private float boundMinXMeter;
   private float boundMaxXMeter;
   private float boundMinYMeter;
   private float boundMaxYMeter;
   private boolean hasBoundingBox = false;
   private boolean noServerSounds = false;
   private CreatureAI creatureAI;
   private LootTable lootTable;

   CreatureTemplate(
      int aId,
      String aName,
      String aPlural,
      String aLongDesc,
      String aModelname,
      int[] aTypes,
      byte aBodyType,
      Skills aSkills,
      short aVision,
      byte aSex,
      short aCentimetersHigh,
      short aCentimetersLong,
      short aCentimetersWide,
      String aDeathSndMale,
      String aDeathSndFemale,
      String aHitSndMale,
      String aHitSndFemale,
      float aNaturalArmour,
      float aHandDam,
      float aKickDam,
      float aBiteDam,
      float aHeadDam,
      float aBreathDam,
      float aSpeed,
      int aMoveActivity,
      int[] aItemsButchered,
      int aMaxHuntdist,
      int aAggress,
      byte aMeatMaterial
   ) {
      this.name = aName;
      this.plural = aPlural;
      this.corpsename = aName.trim().toLowerCase().replaceAll(" ", "") + ".";
      this.mateTemplateId = aId;
      this.modelName = aModelname;
      this.sex = aSex;
      this.bodyType = aBodyType;
      this.skills = aSkills;
      this.vision = aVision;
      this.centimetersHigh = aCentimetersHigh;
      this.centimetersLong = aCentimetersLong;
      this.centimetersWide = aCentimetersWide;
      if (aCentimetersHigh > 400 || aCentimetersLong > 400 || aCentimetersWide > 400) {
         this.size = 5;
      } else if (aCentimetersHigh > 200 || aCentimetersLong > 200 || aCentimetersWide > 200) {
         this.size = 4;
      } else if (aCentimetersHigh > 100 || aCentimetersLong > 100 || aCentimetersWide > 100) {
         this.size = 3;
      } else if (aCentimetersHigh <= 50 && aCentimetersLong <= 50 && aCentimetersWide <= 50) {
         this.size = 1;
      } else {
         this.size = 2;
      }

      this.longDesc = aLongDesc;
      this.id = aId;
      if (this.id != 62 && this.id != 63) {
         this.royalAspiration = false;
      } else {
         this.royalAspiration = true;
      }

      this.naturalArmour = aNaturalArmour;
      this.speed = aSpeed;
      if (aMoveActivity > 1900) {
         this.moveRate = 1900;
      } else {
         this.moveRate = aMoveActivity;
      }

      this.handDamage = aHandDam;
      this.kickDamage = aKickDam;
      this.biteDamage = aBiteDam;
      this.headButtDamage = aHeadDam;
      this.breathDamage = aBreathDam;
      this.butcheredItems = aItemsButchered;
      this.maxHuntDistance = aMaxHuntdist;
      this.aggressivity = aAggress;
      this.hitSoundFemale = aHitSndFemale;
      this.hitSoundMale = aHitSndMale;
      this.deathSoundMale = aDeathSndMale;
      this.deathSoundFemale = aDeathSndFemale;
      this.meatMaterial = aMeatMaterial;
      this.assignTypes(aTypes);
      this.checkNoCorpse();
   }

   private final void checkNoCorpse() {
      if (this.id == 78 || this.id == 81 || this.id == 79 || this.id == 80) {
         this.noCorpse = true;
      }
   }

   public final int[] getItemsButchered() {
      return this.butcheredItems != null ? this.butcheredItems : EMPTY_INT_ARRAY;
   }

   public final void setHeadbuttDamString(String damString) {
      this.headbuttDamString = damString;
   }

   public final void setKickDamString(String damString) {
      this.kickDamString = damString;
   }

   public void setMaxPercentOfCreatures(float percent) {
      this.maxPercentOfCreatures = percent;
   }

   public float getMaxPercentOfCreatures() {
      return this.maxPercentOfCreatures;
   }

   public void setMaxPopulationOfCreatures(int maxPopulation) {
      this.maxPopulationOfCreatures = maxPopulation;
      this.usesMaxPopulation = true;
   }

   public void setBoundsValues(float minX, float minY, float maxX, float maxY) {
      this.boundMinXMeter = minX;
      this.boundMinYMeter = minY;
      this.boundMaxXMeter = maxX;
      this.boundMaxYMeter = maxY;
      this.hasBoundingBox = true;
   }

   public final boolean hasBoundingBox() {
      return this.hasBoundingBox;
   }

   public final float getBoundMinX() {
      return this.boundMinXMeter;
   }

   public final float getBoundMaxX() {
      return this.boundMaxXMeter;
   }

   public final float getBoundMinY() {
      return this.boundMinYMeter;
   }

   public final float getBoundMaxY() {
      return this.boundMaxYMeter;
   }

   public final int getMaxPopulationOfCreatures() {
      return this.maxPopulationOfCreatures;
   }

   public final boolean usesMaxPopulation() {
      return this.usesMaxPopulation;
   }

   public final void setBreathDamString(String damString) {
      this.breathDamString = damString;
   }

   public final void setHandDamString(String damString) {
      this.handDamString = damString;
   }

   public final void setBiteDamString(String damString) {
      this.biteDamString = damString;
   }

   public final float getSpeed() {
      return this.speed;
   }

   public final String getDeathSound(byte aSex) {
      return aSex == 1 ? this.deathSoundFemale : this.deathSoundMale;
   }

   public final String getHitSound(byte aSex) {
      return aSex == 1 ? this.hitSoundFemale : this.hitSoundMale;
   }

   final int getMoveRate() {
      return this.moveRate;
   }

   public final ArmourTemplate.ArmourType getArmourType() {
      return this.armourType;
   }

   public void addPrimaryAttack(AttackAction attack) {
      this.primaryAttacks.add(attack);
   }

   public void addSecondaryAttack(AttackAction attack) {
      this.secondaryAttacks.add(attack);
   }

   public final List<AttackAction> getPrimaryAttacks() {
      return this.primaryAttacks;
   }

   public final List<AttackAction> getSecondaryAttacks() {
      return this.secondaryAttacks;
   }

   public void setUsesNewAttacks(boolean newAttacks) {
      this.useNewAttacks = newAttacks;
   }

   public final boolean isUsingNewAttacks() {
      return this.useNewAttacks;
   }

   public final float getNaturalArmour() {
      return this.naturalArmour;
   }

   final boolean isVowel(String letter) {
      return "aeiouAEIOU".indexOf(letter) != -1;
   }

   public final String getName() {
      return this.name;
   }

   public final String getPlural() {
      return this.plural;
   }

   public final int getTemplateId() {
      return this.id;
   }

   public final String examine() {
      return this.longDesc;
   }

   public final String getModelName() {
      return this.modelName;
   }

   public final boolean isNoCorpse() {
      return this.noCorpse;
   }

   public final byte getBodyType() {
      return this.bodyType;
   }

   public final int getVision() {
      return this.vision;
   }

   public void setVision(short vision) {
      this.vision = vision;
   }

   public final byte getSex() {
      return this.sex;
   }

   public final Skills getSkills() throws Exception {
      return this.skills;
   }

   public final short getCentimetersLong() {
      return this.centimetersLong;
   }

   public final short getCentimetersHigh() {
      return this.centimetersHigh;
   }

   public final short getCentimetersWide() {
      return this.centimetersWide;
   }

   public final String getCorpsename() {
      return this.corpsename;
   }

   public void setCorpseName(String corpseName) {
      this.corpsename = corpseName;
   }

   public final int getSize() {
      return this.size;
   }

   private void assignTypes(int[] aTypes) {
      for(int x = 0; x < aTypes.length; ++x) {
         switch(aTypes[x]) {
            case 0:
               this.sentinel = true;
               break;
            case 1:
               this.trader = true;
               break;
            case 2:
               this.moveRandom = true;
               break;
            case 3:
               this.animal = true;
               break;
            case 4:
               this.invulnerable = true;
               break;
            case 5:
               this.npcTrader = true;
               this.trader = true;
               break;
            case 6:
               this.aggHuman = true;
               break;
            case 7:
               this.moveLocal = true;
               break;
            case 8:
               this.moveGlobal = true;
               break;
            case 9:
               this.grazer = true;
               break;
            case 10:
               this.herd = true;
               break;
            case 11:
               this.villageGuard = true;
               break;
            case 12:
               this.swimming = true;
               break;
            case 13:
               this.hunter = true;
               break;
            case 14:
               this.leadable = true;
               break;
            case 15:
               this.milkable = true;
               break;
            case 16:
               this.monster = true;
               break;
            case 17:
               this.human = true;
               break;
            case 18:
               this.regenerating = true;
               break;
            case 19:
               this.dragon = true;
               break;
            case 20:
               this.unique = true;
               break;
            case 21:
               this.kingdomGuard = true;
               break;
            case 22:
               this.ghost = true;
               break;
            case 23:
               this.spiritGuard = true;
               this.isFloating = true;
               break;
            case 24:
               this.defendKingdom = true;
               break;
            case 25:
               this.aggWhitie = true;
               break;
            case 26:
               this.bartender = true;
               break;
            case 27:
               this.omnivore = true;
               break;
            case 28:
               this.herbivore = true;
               this.fleeing = true;
               break;
            case 29:
               this.carnivore = true;
               break;
            case 30:
               this.climber = true;
               break;
            case 31:
            default:
               logger.warning("Ignoring unexpected CreatureTemplate type: " + aTypes[x]);
               break;
            case 32:
               this.dominatable = true;
               break;
            case 33:
               this.undead = true;
               break;
            case 34:
               this.caveDweller = true;
               break;
            case 35:
               this.fleeing = true;
               break;
            case 36:
               this.isDetectInvis = true;
               break;
            case 37:
               this.isSubmerged = true;
               break;
            case 38:
               this.isFloating = true;
               break;
            case 39:
               this.nonNewbie = true;
               break;
            case 40:
               this.isBreakFence = true;
               break;
            case 41:
               this.isVehicle = true;
               break;
            case 42:
               this.isHorse = true;
               break;
            case 43:
               this.domestic = true;
               break;
            case 44:
               this.moveRate = 100;
               this.careful = true;
               break;
            case 45:
               this.canOpenDoors = true;
               break;
            case 46:
               this.setTowerBasher(true);
               break;
            case 47:
               this.setOnlyAttacksPlayers(true);
               break;
            case 48:
               this.attacksVehicles = false;
               break;
            case 49:
               this.isPrey = true;
               break;
            case 50:
               this.isFromValrei = true;
               break;
            case 51:
               this.isBeachDweller = true;
               break;
            case 52:
               this.woolProducer = true;
               break;
            case 53:
               this.setWarGuard(true);
               break;
            case 54:
               this.isBlackOrWhite = true;
               break;
            case 55:
               this.burning = true;
               break;
            case 56:
               this.setRiftCreature(true);
               break;
            case 57:
               this.setStealth(true);
               break;
            case 58:
               this.setCaster(true);
               break;
            case 59:
               this.setSummoner(true);
               break;
            case 60:
               this.isEpicSlayable = true;
               break;
            case 61:
               this.isEpicTraitor = true;
               break;
            case 62:
               this.isNotRebirthable = true;
               break;
            case 63:
               this.isBabyCreature = true;
               break;
            case 64:
               this.isColoured = true;
               break;
            case 65:
               this.isMissionDisabled = true;
         }
      }
   }

   public final boolean isBeachDweller() {
      return this.isBeachDweller;
   }

   public final boolean isSentinel() {
      return this.sentinel;
   }

   public final boolean isPrey() {
      return this.isPrey;
   }

   public final boolean isCareful() {
      return this.careful;
   }

   public final void setOnlyAttacksPlayers(boolean attacks) {
      this.onlyAttacksPlayers = attacks;
   }

   public final boolean onlyAttacksPlayers() {
      return this.onlyAttacksPlayers;
   }

   public final boolean canOpenDoors() {
      return this.canOpenDoors;
   }

   public final boolean isSubterranean() {
      return this.subterranean;
   }

   public final boolean isHellHorse() {
      return this.id == 83;
   }

   public final boolean isUnicorn() {
      return this.id == 21;
   }

   public final boolean cantRideUntamed() {
      return this.id == 59 || this.id == 12 || this.id == 58 || this.id == 21 || this.isDragon();
   }

   public final void setSubterranean(boolean aSubterranean) {
      this.subterranean = aSubterranean;
   }

   public final boolean isNeedFood() {
      return this.carnivore || this.herbivore || this.omnivore || this.grazer;
   }

   public final boolean isTrader() {
      return this.trader;
   }

   public final boolean isMoveRandom() {
      return this.moveRandom;
   }

   public final boolean isAnimal() {
      return this.animal;
   }

   public final boolean isMonster() {
      return this.monster;
   }

   final boolean isDragon() {
      return this.dragon;
   }

   public static final boolean isDragon(int typeId) {
      return isFullyGrownDragon(typeId) || isDragonHatchling(typeId);
   }

   public static final boolean isFullyGrownDragon(int typeId) {
      return typeId == 89 || typeId == 91 || typeId == 90 || typeId == 92 || typeId == 16;
   }

   public static final boolean isDragonHatchling(int typeId) {
      return typeId == 18 || typeId == 104 || typeId == 17 || typeId == 103 || typeId == 19;
   }

   public final boolean isFleeing() {
      return this.fleeing;
   }

   public final boolean isHuman() {
      return this.human;
   }

   final boolean isRegenerating() {
      return this.regenerating;
   }

   public final boolean isInvulnerable() {
      return this.invulnerable;
   }

   final boolean isNpcTrader() {
      return this.npcTrader;
   }

   public final boolean isAggHuman() {
      return this.aggHuman;
   }

   public final boolean isMoveLocal() {
      return this.moveLocal;
   }

   final boolean isMoveGlobal() {
      return this.moveGlobal;
   }

   final boolean isGrazer() {
      return this.grazer;
   }

   public final boolean isWoolProducer() {
      return this.woolProducer;
   }

   public final boolean isBurning() {
      return this.burning;
   }

   final boolean isHerd() {
      return this.herd;
   }

   public final boolean isSwimming() {
      return this.swimming;
   }

   final boolean isLeadable() {
      return this.leadable;
   }

   public final byte getCombatDamageType() {
      return this.combatDamageType;
   }

   public final int getAggressivity() {
      return this.aggressivity;
   }

   public final float getHandDamage() {
      return this.handDamage;
   }

   public final float getBiteDamage() {
      return this.biteDamage;
   }

   public final float getKickDamage() {
      return this.kickDamage;
   }

   public final float getHeadButtDamage() {
      return this.headButtDamage;
   }

   public final float getBreathDamage() {
      return this.breathDamage;
   }

   public final String getKickDamString() {
      return this.kickDamString;
   }

   public final String getBiteDamString() {
      return this.biteDamString;
   }

   public final String getHeadButtDamString() {
      return this.headbuttDamString;
   }

   public final String getBreathDamString() {
      return this.breathDamString;
   }

   public final String getHandDamString() {
      return this.handDamString;
   }

   public final boolean isHunter() {
      return this.hunter;
   }

   public final boolean isUnique() {
      return this.unique;
   }

   public final boolean isMilkable() {
      return this.milkable;
   }

   public final float getFaith() {
      return !this.isCaster() && !this.isSummoner() ? this.faith : 100.0F;
   }

   public final Deity getDeity() {
      return !this.isCaster() && !this.isSummoner() ? this.deity : Deities.getDeity(4);
   }

   public final void setLeaderTemplateId(int aLeaderTemplateId) {
      this.leaderTemplateId = aLeaderTemplateId;
   }

   public final int getLeaderTemplateId() {
      return this.leaderTemplateId;
   }

   public final int compareTo(CreatureTemplate o1) {
      return this.getName().compareTo(o1.getName());
   }

   public final boolean hasDen() {
      return this.denName != null;
   }

   public final String getDenName() {
      return this.denName;
   }

   public final void setDenName(String aDenName) {
      this.denName = aDenName;
   }

   public final byte getDenMaterial() {
      return this.denMaterial;
   }

   public final void setDenMaterial(byte aMaterial) {
      this.denMaterial = aMaterial;
   }

   public final boolean isRoyalAspiration() {
      return this.royalAspiration;
   }

   public float getAlignment() {
      return this.alignment;
   }

   public void setAlignment(float aAlignment) {
      this.alignment = aAlignment;
   }

   public boolean isGhost() {
      return this.ghost;
   }

   public boolean isBartender() {
      return this.bartender;
   }

   public boolean isDefendKingdom() {
      return this.defendKingdom;
   }

   public boolean isAggWhitie() {
      return this.aggWhitie;
   }

   public boolean isHerbivore() {
      return this.herbivore;
   }

   public boolean isCarnivore() {
      return this.carnivore;
   }

   public boolean isClimber() {
      return this.climber;
   }

   public void setClimber(boolean aClimber) {
      this.climber = aClimber;
   }

   public boolean isDominatable() {
      return this.dominatable;
   }

   public boolean isCaveDweller() {
      return this.caveDweller;
   }

   public boolean isEggLayer() {
      return this.eggLayer;
   }

   public void setEggLayer(boolean aEggLayer) {
      this.eggLayer = aEggLayer;
   }

   public int getEggTemplateId() {
      return this.eggTemplateId;
   }

   public void setEggTemplateId(int aEggTemplateId) {
      this.eggTemplateId = aEggTemplateId;
   }

   public int getChildTemplateId() {
      return this.childTemplateId;
   }

   public void setChildTemplateId(int aChildTemplateId) {
      this.childTemplateId = aChildTemplateId;
   }

   public int[] getCombatMoves() {
      return this.combatMoves;
   }

   public void setCombatMoves(int[] aCombatMoves) {
      this.combatMoves = aCombatMoves;
   }

   public boolean isFloating() {
      return this.isFloating;
   }

   public boolean isBreakFence() {
      return this.isBreakFence;
   }

   public int getAdultFemaleTemplateId() {
      return this.adultFemaleTemplateId;
   }

   public void setAdultFemaleTemplateId(int aAdultFemaleTemplateId) {
      this.adultFemaleTemplateId = aAdultFemaleTemplateId;
   }

   public int getAdultMaleTemplateId() {
      return this.adultMaleTemplateId;
   }

   public void setAdultMaleTemplateId(int aAdultMaleTemplateId) {
      this.adultMaleTemplateId = aAdultMaleTemplateId;
   }

   public float getBaseCombatRating() {
      return this.baseCombatRating;
   }

   public void setBaseCombatRating(float aBaseCombatRating) {
      this.baseCombatRating = aBaseCombatRating;
   }

   public int getColorRed() {
      return this.colorRed;
   }

   public void setColorRed(int aColorRed) {
      this.colorRed = aColorRed;
   }

   public int getColorGreen() {
      return this.colorGreen;
   }

   public void setColorGreen(int aColorGreen) {
      this.colorGreen = aColorGreen;
   }

   public int getColorBlue() {
      return this.colorBlue;
   }

   public void setColorBlue(int aColorBlue) {
      this.colorBlue = aColorBlue;
   }

   public String getModelColourName(CreatureStatus status) {
      String colourString = "grey";
      if (this.isHorse || this.isColoured) {
         int id = this.getColourCode(status);
         if (id >= this.maxColourCount) {
            id = 0;
         }

         colourString = this.colourNameOverrides[id].replaceAll(" ", "");
      } else if (this.isBlackOrWhite) {
         if (status.isTraitBitSet(15)
            || status.isTraitBitSet(16)
            || status.isTraitBitSet(18)
            || status.isTraitBitSet(24)
            || status.isTraitBitSet(25)
            || status.isTraitBitSet(23)) {
            colourString = "white";
         } else if (status.isTraitBitSet(17)) {
            colourString = "black";
         }
      }

      return colourString;
   }

   public String getColourName(CreatureStatus status) {
      String colourString = this.colourNameOverrides[0];
      if (this.isHorse || this.isColoured) {
         int id = this.getColourCode(status);
         if (id >= this.maxColourCount) {
            id = 0;
         }

         colourString = this.colourNameOverrides[id];
      } else if (this.isBlackOrWhite) {
         if (status.isTraitBitSet(15)
            || status.isTraitBitSet(16)
            || status.isTraitBitSet(18)
            || status.isTraitBitSet(24)
            || status.isTraitBitSet(25)
            || status.isTraitBitSet(23)
            || status.isTraitBitSet(30)
            || status.isTraitBitSet(31)
            || status.isTraitBitSet(32)
            || status.isTraitBitSet(33)
            || status.isTraitBitSet(34)) {
            colourString = this.colourNameOverrides[4];
         } else if (status.isTraitBitSet(17)) {
            colourString = this.colourNameOverrides[3];
         }
      }

      return colourString;
   }

   public byte getColourCode(CreatureStatus status) {
      if (!this.isHorse && !this.isColoured) {
         if (this.isBlackOrWhite) {
            if (status.isTraitBitSet(15)
               || status.isTraitBitSet(16)
               || status.isTraitBitSet(18)
               || status.isTraitBitSet(24)
               || status.isTraitBitSet(25)
               || status.isTraitBitSet(23)
               || status.isTraitBitSet(30)
               || status.isTraitBitSet(31)
               || status.isTraitBitSet(32)
               || status.isTraitBitSet(33)
               || status.isTraitBitSet(34)) {
               return 4;
            }

            if (status.isTraitBitSet(17)) {
               return 3;
            }
         }
      } else {
         if (status.isTraitBitSet(15)) {
            return 1;
         }

         if (status.isTraitBitSet(16)) {
            return 2;
         }

         if (status.isTraitBitSet(17)) {
            return 3;
         }

         if (status.isTraitBitSet(18)) {
            return 4;
         }

         if (status.isTraitBitSet(24)) {
            return 5;
         }

         if (status.isTraitBitSet(25)) {
            return 6;
         }

         if (status.isTraitBitSet(23)) {
            return 7;
         }

         if (status.isTraitBitSet(30)) {
            return 8;
         }

         if (status.isTraitBitSet(31)) {
            return 9;
         }

         if (status.isTraitBitSet(32)) {
            return 10;
         }

         if (status.isTraitBitSet(33)) {
            return 11;
         }

         if (status.isTraitBitSet(34)) {
            return 12;
         }
      }

      return 0;
   }

   public String getTemplateColourName(int trait) {
      int index = 0;
      if (trait == 15) {
         index = 1;
      } else if (trait == 16) {
         index = 2;
      } else if (trait == 17) {
         index = 3;
      } else if (trait == 18) {
         index = 4;
      } else if (trait == 24) {
         index = 5;
      } else if (trait == 25) {
         index = 6;
      } else if (trait == 23) {
         index = 7;
      } else if (trait == 30) {
         index = 8;
      } else if (trait == 31) {
         index = 9;
      } else if (trait == 32) {
         index = 10;
      } else if (trait == 33) {
         index = 11;
      } else if (trait == 34) {
         index = 12;
      }

      return this.colourNameOverrides[index];
   }

   public void setColourNames(String[] colours) {
      for(int x = 0; x < colours.length && x < 13; ++x) {
         this.colourNameOverrides[x] = colours[x];
      }

      for(int x = colours.length; x < this.colourNameOverrides.length; ++x) {
         this.colourNameOverrides[x] = "unused";
      }

      this.maxColourCount = colours.length;
   }

   public boolean isGlowing() {
      return this.glowing;
   }

   public void setGlowing(boolean aGlowing) {
      this.glowing = aGlowing;
   }

   public byte getFireRadius() {
      return this.fireRadius;
   }

   public void setFireRadius(byte aFireRadius) {
      this.fireRadius = aFireRadius;
   }

   public boolean isDetectInvis() {
      return this.isDetectInvis;
   }

   public void setDetectInvis(boolean aIsDetectInvis) {
      this.isDetectInvis = aIsDetectInvis;
   }

   public byte getDaysOfPregnancy() {
      return this.daysOfPregnancy;
   }

   public void setDaysOfPregnancy(byte aDaysOfPregnancy) {
      this.daysOfPregnancy = aDaysOfPregnancy;
   }

   public void setArmourType(ArmourTemplate.ArmourType aArmourType) {
      this.armourType = aArmourType;
   }

   public void setCombatDamageType(byte aCombatDamageType) {
      this.combatDamageType = aCombatDamageType;
   }

   public boolean isKingdomGuard() {
      return this.kingdomGuard;
   }

   public boolean isSpiritGuard() {
      return this.spiritGuard;
   }

   public boolean isOmnivore() {
      return this.omnivore;
   }

   public boolean isUndead() {
      return this.undead;
   }

   public boolean isNoSkillgain() {
      return this.isNoSkillgain;
   }

   public void setNoSkillgain(boolean aIsNoSkillgain) {
      this.isNoSkillgain = aIsNoSkillgain;
   }

   public boolean isSubmerged() {
      return this.isSubmerged;
   }

   public int getMateTemplateId() {
      return this.mateTemplateId;
   }

   public void setMateTemplateId(int aMateTemplateId) {
      this.mateTemplateId = aMateTemplateId;
   }

   public boolean isKeepSex() {
      return this.keepSex;
   }

   public void setKeepSex(boolean aKeepSex) {
      this.keepSex = aKeepSex;
   }

   public int getMaxGroupAttackSize() {
      return this.maxGroupAttackSize;
   }

   public void setMaxGroupAttackSize(int aMaxGroupAttackSize) {
      this.maxGroupAttackSize = aMaxGroupAttackSize;
   }

   public boolean isTutorial() {
      return this.tutorial;
   }

   public void setTutorial(boolean aTutorial) {
      this.tutorial = aTutorial;
   }

   public int getMaxAge() {
      return this.maxAge;
   }

   public void setMaxAge(int aMaxAge) {
      this.maxAge = aMaxAge;
   }

   public int getPaintMode() {
      return this.paintMode;
   }

   public void setPaintMode(int aPaintMode) {
      this.paintMode = aPaintMode;
   }

   public int getSizeModX() {
      return this.sizeModX;
   }

   public void setSizeModX(int aSizeModX) {
      this.sizeModX = aSizeModX;
   }

   public int getSizeModY() {
      return this.sizeModY;
   }

   public void setSizeModY(int aSizeModY) {
      this.sizeModY = aSizeModY;
   }

   public int getSizeModZ() {
      return this.sizeModZ;
   }

   public void setSizeModZ(int aSizeModZ) {
      this.sizeModZ = aSizeModZ;
   }

   public boolean isOnFire() {
      return this.isOnFire;
   }

   public void setOnFire(boolean aIsOnFire) {
      this.isOnFire = aIsOnFire;
   }

   public boolean isNonNewbie() {
      return this.nonNewbie;
   }

   public void setNonNewbie(boolean aNonNewbie) {
      this.nonNewbie = aNonNewbie;
   }

   public boolean isVehicle() {
      return this.isVehicle;
   }

   public void setVehicle(boolean aIsVehicle) {
      this.isVehicle = aIsVehicle;
   }

   public int getReputation() {
      return 100;
   }

   public int getMaxHuntDistance() {
      return this.maxHuntDistance;
   }

   public float getFireResistance() {
      return this.fireResistance;
   }

   public float getColdResistance() {
      return this.coldResistance;
   }

   public float getDiseaseResistance() {
      return this.diseaseResistance;
   }

   public float getPhysicalResistance() {
      return this.physicalResistance;
   }

   public float getPierceResistance() {
      return this.pierceResistance;
   }

   public float getSlashResistance() {
      return this.slashResistance;
   }

   public float getCrushResistance() {
      return this.crushResistance;
   }

   public float getBiteResistance() {
      return this.biteResistance;
   }

   public float getPoisonResistance() {
      return this.poisonResistance;
   }

   public float getWaterResistance() {
      return this.waterResistance;
   }

   public float getAcidResistance() {
      return this.acidResistance;
   }

   public float getInternalResistance() {
      return this.internalResistance;
   }

   public float getFireVulnerability() {
      return this.fireVulnerability;
   }

   public float getColdVulnerability() {
      return this.coldVulnerability;
   }

   public float getDiseaseVulnerability() {
      return this.diseaseVulnerability;
   }

   public float getPhysicalVulnerability() {
      return this.physicalVulnerability;
   }

   public float getPierceVulnerability() {
      return this.pierceVulnerability;
   }

   public float getSlashVulnerability() {
      return this.slashVulnerability;
   }

   public float getCrushVulnerability() {
      return this.crushVulnerability;
   }

   public float getBiteVulnerability() {
      return this.biteVulnerability;
   }

   public float getPoisonVulnerability() {
      return this.poisonVulnerability;
   }

   public float getWaterVulnerability() {
      return this.waterVulnerability;
   }

   public float getAcidVulnerability() {
      return this.acidVulnerability;
   }

   public float getInternalVulnerability() {
      return this.internalVulnerability;
   }

   @Override
   public final String toString() {
      return "CreatureTemplate [id: " + this.id + ", name: " + this.name + ", modelName: " + this.modelName + ']';
   }

   public boolean isTowerBasher() {
      return this.towerBasher;
   }

   public void setTowerBasher(boolean aTowerBasher) {
      this.towerBasher = aTowerBasher;
   }

   public float getBonusCombatRating() {
      return this.bonusCombatRating;
   }

   public void setBonusCombatRating(float aBonusCombatRating) {
      this.bonusCombatRating = aBonusCombatRating;
   }

   public boolean noServerSounds() {
      return this.noServerSounds;
   }

   public void setNoServerSounds(boolean onServerSounds) {
      this.noServerSounds = onServerSounds;
   }

   public float getWeight() {
      return (float)(this.centimetersHigh * this.centimetersLong * this.centimetersWide) / 1.4F;
   }

   public boolean isWarGuard() {
      return this.isWarGuard;
   }

   public void setWarGuard(boolean isWarGuard) {
      this.isWarGuard = isWarGuard;
   }

   public CreatureAI getCreatureAI() {
      return this.creatureAI;
   }

   public void setCreatureAI(CreatureAI creatureAI) {
      this.creatureAI = creatureAI;
   }

   public boolean isRiftCreature() {
      return this.riftCreature;
   }

   public void setRiftCreature(boolean riftCreature) {
      this.riftCreature = riftCreature;
   }

   public boolean isStealth() {
      return this.isStealth;
   }

   public void setStealth(boolean isStealth) {
      this.isStealth = isStealth;
   }

   public boolean isCaster() {
      return this.isCaster;
   }

   public void setCaster(boolean isCaster) {
      this.isCaster = isCaster;
   }

   public boolean isSummoner() {
      return this.isSummoner;
   }

   public void setSummoner(boolean isSummoner) {
      this.isSummoner = isSummoner;
   }

   public boolean isEpicMissionSlayable() {
      return this.isEpicSlayable;
   }

   public boolean isEpicMissionTraitor() {
      return this.isEpicTraitor;
   }

   public boolean isMissionDisabled() {
      return this.isMissionDisabled;
   }

   public boolean isNotRebirthable() {
      return this.isNotRebirthable;
   }

   public boolean isBabyCreature() {
      return this.isBabyCreature;
   }

   public byte getMeatMaterial() {
      return this.meatMaterial;
   }

   public void addLootPool(LootPool... pool) {
      if (this.lootTable == null) {
         this.lootTable = new LootTable();
      }

      this.lootTable.addLootPools(pool);
   }

   public Optional<LootTable> getLootTable() {
      return Optional.ofNullable(this.lootTable);
   }
}

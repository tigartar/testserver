package com.wurmonline.server.spells;

import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class Spells implements AttitudeConstants {
   private static final Logger logger = Logger.getLogger(Spells.class.getName());
   private static final Map<Integer, Spell> spells = new HashMap<>();
   private static final Map<Integer, Spell> creatureSpells = new HashMap<>();
   private static final Map<Integer, Spell> itemSpells = new HashMap<>();
   private static final Map<Integer, Spell> woundSpells = new HashMap<>();
   private static final Map<Byte, Spell> enchantments = new HashMap<>();
   private static final Map<Integer, Spell> tileSpells = new HashMap<>();
   public static Spell SPELL_AURA_SHARED_PAIN = new SharedPain();
   public static Spell SPELL_BEARPAWS = new Bearpaw();
   public static Spell SPELL_BLAZE = new Blaze();
   public static Spell SPELL_BLESS = new Bless();
   public static Spell SPELL_BLESSINGS_OF_THE_DARK = new BlessingDark();
   public static Spell SPELL_BLOODTHIRST = new Bloodthirst();
   public static Spell SPELL_BREAK_ALTAR = new BreakAltar();
   public static Spell SPELL_CHARM_ANIMAL = new CharmAnimal();
   public static Spell SPELL_CIRCLE_OF_CUNNING = new CircleOfCunning();
   public static Spell SPELL_CLEANSE = new Cleanse();
   public static Spell SPELL_CORROSION = new Corrosion();
   public static Spell SPELL_CORRUPT = new Corrupt();
   public static Spell SPELL_COURIER = new Courier();
   public static Spell SPELL_CURE_LIGHT = new CureLight();
   public static Spell SPELL_CURE_MEDIUM = new CureMedium();
   public static Spell SPELL_CURE_SERIOUS = new CureSerious();
   public static Spell SPELL_DARK_MESSENGER = new DarkMessenger();
   public static Spell SPELL_DEMISE_ANIMAL = new DemiseAnimal();
   public static Spell SPELL_DEMISE_HUMAN = new DemiseHuman();
   public static Spell SPELL_DEMISE_LEGENDARY = new DemiseLegendary();
   public static Spell SPELL_DEMISE_MONSTER = new DemiseMonster();
   public static Spell SPELL_DIRT = new Dirt();
   public static Spell SPELL_DISINTEGRATE = new Disintegrate();
   public static Spell SPELL_DISPEL = new Dispel();
   public static Spell SPELL_DOMINATE = new Dominate();
   public static Spell SPELL_DRAIN_HEALTH = new DrainHealth();
   public static Spell SPELL_DRAIN_STAMINA = new DrainStamina();
   public static Spell SPELL_ESSENCE_DRAIN = new EssenceDrain();
   public static Spell SPELL_EXCEL = new Excel();
   public static Spell SPELL_FIREHEART = new FireHeart();
   public static Spell SPELL_FIRE_PILLAR = new FirePillar();
   public static Spell SPELL_FLAMING_AURA = new FlamingAura();
   public static Spell SPELL_FOCUSED_WILL = new FocusedWill();
   public static Spell SPELL_FOREST_GIANT_STRENGTH = new ForestGiant();
   public static Spell SPELL_FRANTIC_CHARGE = new FranticCharge();
   public static Spell SPELL_FROSTBRAND = new Frostbrand();
   public static Spell SPELL_FUNGUS_TRAP = new FungusTrap();
   public static Spell SPELL_GENESIS = new Genesis();
   public static Spell SPELL_GLACIAL = new Glacial();
   public static Spell SPELL_GOAT_SHAPE = new GoatShape();
   public static Spell SPELL_HEAL = new Heal();
   public static Spell SPELL_HELL_STRENGTH = new Hellstrength();
   public static Spell SPELL_HOLY_CROP = new HolyCrop();
   public static Spell SPELL_HUMID_DRIZZLE = new HumidDrizzle();
   public static Spell SPELL_HYPOTHERMIA = new Hypothermia();
   public static Spell SPELL_ICE_PILLAR = new IcePillar();
   public static Spell SPELL_INFERNO = new Inferno();
   public static Spell SPELL_LAND_OF_THE_DEAD = new LandOfTheDead();
   public static Spell SPELL_LIFE_TRANSFER = new LifeTransfer();
   public static Spell SPELL_LIGHT_OF_FO = new LightOfFo();
   public static Spell SPELL_LIGHT_TOKEN = new LightToken();
   public static Spell SPELL_LOCATE_ARTIFACT = new LocateArtifact();
   public static Spell SPELL_LOCATE_SOUL = new LocatePlayer();
   public static Spell SPELL_LURKER_IN_THE_DARK = new LurkerDark();
   public static Spell SPELL_LURKER_IN_THE_DEEP = new LurkerDeep();
   public static Spell SPELL_LURKER_IN_THE_WOODS = new LurkerWoods();
   public static Spell SPELL_MASS_STAMINA = new MassStamina();
   public static Spell SPELL_MEND = new Mend();
   public static Spell SPELL_MIND_STEALER = new MindStealer();
   public static Spell SPELL_MOLE_SENSES = new MoleSenses();
   public static Spell SPELL_MORNING_FOG = new MorningFog();
   public static Spell SPELL_NIMBLENESS = new Nimbleness();
   public static Spell SPELL_NOLOCATE = new Nolocate();
   public static Spell SPELL_OAKSHELL = new OakShell();
   public static Spell SPELL_OPULENCE = new Opulence();
   public static Spell SPELL_PAIN_RAIN = new PainRain();
   public static Spell SPELL_PHANTASMS = new Phantasms();
   public static Spell SPELL_PROTECT_ACID = new ProtectionAcid();
   public static Spell SPELL_PROTECT_FIRE = new ProtectionFire();
   public static Spell SPELL_PROTECT_FROST = new ProtectionFrost();
   public static Spell SPELL_PROTECT_POISON = new ProtectionPoison();
   public static Spell SPELL_PURGE = new Purge();
   public static Spell SPELL_REBIRTH = new Rebirth();
   public static Spell SPELL_REFRESH = new Refresh();
   public static Spell SPELL_REVEAL_CREATURES = new RevealCreatures();
   public static Spell SPELL_REVEAL_SETTLEMENTS = new RevealSettlements();
   public static Spell SPELL_RITE_OF_DEATH = new RiteDeath();
   public static Spell SPELL_RITE_OF_SPRING = new RiteSpring();
   public static Spell SPELL_RITUAL_OF_THE_SUN = new RitualSun();
   public static Spell SPELL_ROTTING_GUT = new RottingGut();
   public static Spell SPELL_ROTTING_TOUCH = new RottingTouch();
   public static Spell SPELL_SCORN_OF_LIBILA = new ScornOfLibila();
   public static Spell SPELL_SHARD_OF_ICE = new ShardOfIce();
   public static Spell SPELL_SIXTH_SENSE = new SixthSense();
   public static Spell SPELL_SMITE = new Smite();
   public static Spell SPELL_STRONGWALL = new StrongWall();
   public static Spell SPELL_SUMMON_SOUL = new SummonSoul();
   public static Spell SPELL_SUNDER = new Sunder();
   public static Spell SPELL_TANGLEWEAVE = new TangleWeave();
   public static Spell SPELL_TENTACLES = new DeepTentacles();
   public static Spell SPELL_TORNADO = new Tornado();
   public static Spell SPELL_TOXIN = new Toxin();
   public static Spell SPELL_TRUEHIT = new TrueHit();
   public static Spell SPELL_VENOM = new Venom();
   public static Spell SPELL_VESSEL = new Vessle();
   public static Spell SPELL_WARD = new Ward();
   public static Spell SPELL_WEAKNESS = new Weakness();
   public static Spell SPELL_WEB_ARMOUR = new WebArmour();
   public static Spell SPELL_WILD_GROWTH = new WildGrowth();
   public static Spell SPELL_WILLOWSPINE = new WillowSpine();
   public static Spell SPELL_WIND_OF_AGES = new WindOfAges();
   public static Spell SPELL_WISDOM_OF_VYNORA = new WisdomVynora();
   public static Spell SPELL_WORM_BRAINS = new WormBrains();
   public static Spell SPELL_WRATH_OF_MAGRANON = new WrathMagranon();
   public static Spell SPELL_ZOMBIE_INFESTATION = new ZombieInfestation();
   public static Spell SPELL_CONTINUUM = new Continuum();
   public static Spell SPELL_DISEASE = new Disease();
   public static Spell SPELL_FIREBALL = new Fireball();
   public static Spell SPELL_FORECAST = new Forecast();
   public static Spell SPELL_INCINERATE = new Incinerate();
   public static Spell SPELL_KARMA_BOLT = new KarmaBolt();
   public static Spell SPELL_KARMA_MISSILE = new KarmaMissile();
   public static Spell SPELL_KARMA_SLOW = new KarmaSlow();
   public static Spell SPELL_LIGHTNING = new Lightning();
   public static Spell SPELL_MIRRORED_SELF = new MirroredSelf();
   public static Spell SPELL_RUST_MONSTER = new RustMonster();
   public static Spell SPELL_SPROUT_TREES = new SproutTrees();
   public static Spell SPELL_STONESKIN = new StoneSkin();
   public static Spell SPELL_SUMMON = new Summon();
   public static Spell SPELL_SUMMON_SKELETON = new SummonSkeleton();
   public static Spell SPELL_SUMMON_WORG = new SummonWorg();
   public static Spell SPELL_SUMMON_WRAITH = new SummonWraith();
   public static Spell SPELL_TRUESTRIKE = new Truestrike();
   public static Spell SPELL_WALL_OF_FIRE = new WallOfFire();
   public static Spell SPELL_WALL_OF_ICE = new WallOfIce();
   public static Spell SPELL_WALL_OF_STONE = new WallOfStone();

   public static void addSpell(Spell spell) {
      spells.put(spell.getNumber(), spell);
      if (spell.isTargetCreature()) {
         creatureSpells.put(spell.getNumber(), spell);
      }

      if (spell.isTargetAnyItem()) {
         itemSpells.put(spell.getNumber(), spell);
      }

      if (spell.isTargetWound()) {
         woundSpells.put(spell.getNumber(), spell);
      }

      if (spell.isTargetTile()) {
         tileSpells.put(spell.getNumber(), spell);
      }

      if (spell.getEnchantment() != 0) {
         enchantments.put(spell.getEnchantment(), spell);
      }
   }

   public static final Spell getSpell(int number) {
      return spells.get(number);
   }

   public static final Spell[] getAllSpells() {
      return spells.values().toArray(new Spell[spells.size()]);
   }

   public static final Spell getEnchantment(byte num) {
      return enchantments.get(num);
   }

   public static final Spell[] getSpellsTargettingItems() {
      Set<Spell> toReturn = new HashSet<>();
      Iterator<Integer> it = itemSpells.keySet().iterator();

      while(it.hasNext()) {
         toReturn.add(itemSpells.get(it.next()));
      }

      return toReturn.toArray(new Spell[toReturn.size()]);
   }

   public static final Spell[] getSpellsEnchantingItems() {
      Set<Spell> toReturn = new HashSet<>();
      Iterator<Integer> it = itemSpells.keySet().iterator();

      while(it.hasNext()) {
         Spell spell = itemSpells.get(it.next());
         if (spell.getEnchantment() > 0) {
            toReturn.add(spell);
         }
      }

      return toReturn.toArray(new Spell[toReturn.size()]);
   }

   static {
      addSpell(SPELL_AURA_SHARED_PAIN);
      addSpell(SPELL_BEARPAWS);
      addSpell(SPELL_BLAZE);
      addSpell(SPELL_BLESS);
      addSpell(SPELL_BLESSINGS_OF_THE_DARK);
      addSpell(SPELL_BLOODTHIRST);
      addSpell(SPELL_BREAK_ALTAR);
      addSpell(SPELL_CHARM_ANIMAL);
      addSpell(SPELL_CIRCLE_OF_CUNNING);
      addSpell(SPELL_CLEANSE);
      addSpell(SPELL_CORROSION);
      addSpell(SPELL_CORRUPT);
      addSpell(SPELL_COURIER);
      addSpell(SPELL_CURE_LIGHT);
      addSpell(SPELL_CURE_MEDIUM);
      addSpell(SPELL_CURE_SERIOUS);
      addSpell(SPELL_DARK_MESSENGER);
      addSpell(SPELL_DEMISE_ANIMAL);
      addSpell(SPELL_DEMISE_HUMAN);
      addSpell(SPELL_DEMISE_LEGENDARY);
      addSpell(SPELL_DEMISE_MONSTER);
      addSpell(SPELL_DIRT);
      addSpell(SPELL_DISINTEGRATE);
      addSpell(SPELL_DISPEL);
      addSpell(SPELL_DOMINATE);
      addSpell(SPELL_DRAIN_HEALTH);
      addSpell(SPELL_DRAIN_STAMINA);
      addSpell(SPELL_ESSENCE_DRAIN);
      addSpell(SPELL_EXCEL);
      addSpell(SPELL_FIREHEART);
      addSpell(SPELL_FIRE_PILLAR);
      addSpell(SPELL_FLAMING_AURA);
      addSpell(SPELL_FOCUSED_WILL);
      addSpell(SPELL_FOREST_GIANT_STRENGTH);
      addSpell(SPELL_FRANTIC_CHARGE);
      addSpell(SPELL_FROSTBRAND);
      addSpell(SPELL_FUNGUS_TRAP);
      addSpell(SPELL_GENESIS);
      addSpell(SPELL_GLACIAL);
      addSpell(SPELL_GOAT_SHAPE);
      addSpell(SPELL_HEAL);
      addSpell(SPELL_HELL_STRENGTH);
      addSpell(SPELL_HOLY_CROP);
      addSpell(SPELL_HUMID_DRIZZLE);
      addSpell(SPELL_HYPOTHERMIA);
      addSpell(SPELL_ICE_PILLAR);
      addSpell(SPELL_INFERNO);
      addSpell(SPELL_LAND_OF_THE_DEAD);
      addSpell(SPELL_LIFE_TRANSFER);
      addSpell(SPELL_LIGHT_OF_FO);
      addSpell(SPELL_LIGHT_TOKEN);
      addSpell(SPELL_LOCATE_ARTIFACT);
      addSpell(SPELL_LOCATE_SOUL);
      addSpell(SPELL_LURKER_IN_THE_DARK);
      addSpell(SPELL_LURKER_IN_THE_DEEP);
      addSpell(SPELL_LURKER_IN_THE_WOODS);
      addSpell(SPELL_MASS_STAMINA);
      addSpell(SPELL_MEND);
      addSpell(SPELL_MIND_STEALER);
      addSpell(SPELL_MOLE_SENSES);
      addSpell(SPELL_MORNING_FOG);
      addSpell(SPELL_NIMBLENESS);
      addSpell(SPELL_NOLOCATE);
      addSpell(SPELL_OAKSHELL);
      addSpell(SPELL_OPULENCE);
      addSpell(SPELL_PAIN_RAIN);
      addSpell(SPELL_PHANTASMS);
      addSpell(SPELL_PROTECT_ACID);
      addSpell(SPELL_PROTECT_FIRE);
      addSpell(SPELL_PROTECT_FROST);
      addSpell(SPELL_PROTECT_POISON);
      addSpell(SPELL_PURGE);
      addSpell(SPELL_REBIRTH);
      addSpell(SPELL_REFRESH);
      addSpell(SPELL_REVEAL_CREATURES);
      addSpell(SPELL_REVEAL_SETTLEMENTS);
      addSpell(SPELL_RITE_OF_DEATH);
      addSpell(SPELL_RITE_OF_SPRING);
      addSpell(SPELL_RITUAL_OF_THE_SUN);
      addSpell(SPELL_ROTTING_GUT);
      addSpell(SPELL_ROTTING_TOUCH);
      addSpell(SPELL_SCORN_OF_LIBILA);
      addSpell(SPELL_SHARD_OF_ICE);
      addSpell(SPELL_SIXTH_SENSE);
      addSpell(SPELL_SMITE);
      addSpell(SPELL_STRONGWALL);
      addSpell(SPELL_SUMMON_SOUL);
      addSpell(SPELL_SUNDER);
      addSpell(SPELL_TANGLEWEAVE);
      addSpell(SPELL_TENTACLES);
      addSpell(SPELL_TORNADO);
      addSpell(SPELL_TOXIN);
      addSpell(SPELL_TRUEHIT);
      addSpell(SPELL_VENOM);
      addSpell(SPELL_VESSEL);
      addSpell(SPELL_WARD);
      addSpell(SPELL_WEAKNESS);
      addSpell(SPELL_WEB_ARMOUR);
      addSpell(SPELL_WILD_GROWTH);
      addSpell(SPELL_WILLOWSPINE);
      addSpell(SPELL_WIND_OF_AGES);
      addSpell(SPELL_WISDOM_OF_VYNORA);
      addSpell(SPELL_WORM_BRAINS);
      addSpell(SPELL_WRATH_OF_MAGRANON);
      addSpell(SPELL_ZOMBIE_INFESTATION);
      addSpell(SPELL_CONTINUUM);
      addSpell(SPELL_DISEASE);
      addSpell(SPELL_FIREBALL);
      addSpell(SPELL_FORECAST);
      addSpell(SPELL_INCINERATE);
      addSpell(SPELL_KARMA_BOLT);
      addSpell(SPELL_KARMA_MISSILE);
      addSpell(SPELL_KARMA_SLOW);
      addSpell(SPELL_LIGHTNING);
      addSpell(SPELL_MIRRORED_SELF);
      addSpell(SPELL_RUST_MONSTER);
      addSpell(SPELL_SPROUT_TREES);
      addSpell(SPELL_STONESKIN);
      addSpell(SPELL_SUMMON);
      addSpell(SPELL_SUMMON_SKELETON);
      addSpell(SPELL_SUMMON_WORG);
      addSpell(SPELL_SUMMON_WRAITH);
      addSpell(SPELL_TRUESTRIKE);
      addSpell(SPELL_WALL_OF_FIRE);
      addSpell(SPELL_WALL_OF_ICE);
      addSpell(SPELL_WALL_OF_STONE);
   }
}

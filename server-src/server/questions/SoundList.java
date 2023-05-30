package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.sounds.SoundPlayer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Properties;

public class SoundList extends Question {
   private MissionManager root = null;
   private String selected = "";
   private int sortBy = 1;
   private int showCat = 0;
   private SoundList.soundString[] soundStrings;
   private String[] catStrings;
   private int nextId = 1;

   public SoundList(Creature _responder, String _title, String _question) {
      super(_responder, _title, _question, 150, -10L);
      this.loadSoundStrings();
   }

   public void loadSoundStrings() {
      LinkedList<SoundList.soundString> soundsList = new LinkedList<>();
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CRICKETSDAY_SND", "sound.ambient.day.crickets"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CRICKETSNIGHT_SND", "sound.ambient.night.crickets"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LEAFRUSTLE_SND", "sound.forest.leafrustle"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_WINDWEAK_SND", "sound.ambient.wind.weak"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_WINDSTRONG_SND", "sound.ambient.wind.strong"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_RAINLIGHT_SND", "sound.ambient.rain.light"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_RAINHEAVY_SND", "sound.ambient.rain.heavy"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FORESTCREAKSOFT_SND", "sound.forest.creak.soft"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FORESTCREAKLOUD_SND", "sound.forest.creak.loud"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIRE", "sound.ambient.fire"));
      soundsList.add(new SoundList.soundString("Ambient", "FISHJUMP_SND", "sound.fish.splash"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_BUZZ_LEFT1", "sound.1.3.001.0001.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_BUZZ_RIGHT1", "sound.1.4.001.0001.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_DAY_1", "sound.2.3.013.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_DAY_2", "sound.2.4.013.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_DAY_3", "sound.2.3.013.0002.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_DAY_4", "sound.2.4.013.0002.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_NIGHT_1", "sound.2.3.013.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_NIGHT_2", "sound.2.3.013.0003.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_NIGHT_3", "sound.2.4.013.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FOREST_NIGHT_4", "sound.2.4.013.0003.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIELD_DAY_1", "sound.2.3.018.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIELD_DAY_3", "sound.2.4.018.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIELD_DAY_2", "sound.2.3.018.0002.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIELD_DAY_4", "sound.2.4.018.0002.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIELD_NIGHT_1", "sound.2.3.018.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_FIELD_NIGHT_2", "sound.2.4.018.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MYCELIUM_BUZZ_LEFT1", "sound.1.3.020.0001.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MYCELIUM_BUZZ_LEFT2", "sound.1.3.020.0001.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MYCELIUM_BUZZ_LEFT3", "sound.1.3.020.0001.003"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MYCELIUM_BUZZ_RIGHT1", "sound.1.4.020.0001.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MYCELIUM_BUZZ_RIGHT2", "sound.1.4.020.0001.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MYCELIUM_BUZZ_RIGHT3", "sound.1.4.020.0001.003"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_GRASS_DAY_1", "sound.2.3.012.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_GRASS_DAY_2", "sound.2.4.012.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_HIGH_DAY_3", "sound.2.3.015.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_HIGH_DAY_4", "sound.2.4.015.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_LOW_DAY_3", "sound.2.3.022.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_LOW_DAY_4", "sound.2.4.022.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_HIGH_NIGHT1", "sound.2.3.015.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_HIGH_NIGHT2", "sound.2.4.015.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_LOW_NIGHT1", "sound.2.3.022.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_MOUNTAIN_LOW_NIGHT2", "sound.2.4.022.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CAVE_1", "sound.2.3.021.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CAVE_2", "sound.2.4.021.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CAVE_3", "sound.2.3.021.0004.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CAVE_4", "sound.2.4.021.0004.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CAVE_WATER_1", "sound.2.3.021.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_CAVE_WATER_2", "sound.2.4.021.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_STEPPE_DAY_1", "sound.2.3.017.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_STEPPE_DAY_2", "sound.2.4.017.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_STEPPE_NIGHT_1", "sound.2.3.017.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_STEPPE_NIGHT_2", "sound.2.4.017.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_DESERT_DAY_1", "sound.2.3.019.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_DESERT_DAY_2", "sound.2.4.019.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_1", "sound.2.3.014.0003.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_2", "sound.2.4.014.0002.003"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_3", "sound.2.3.014.0003.003"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_4", "sound.2.4.014.0003.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_5", "sound.2.3.014.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_6", "sound.2.4.014.0002.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_7", "sound.2.3.014.0002.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_DAY_8", "sound.2.4.014.0002.002"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_NIGHT_1", "sound.2.3.014.0003.001"));
      soundsList.add(new SoundList.soundString("Ambient", "AMBIENT_LAKE_NIGHT_2", "sound.2.4.014.0003.001"));
      soundsList.add(new SoundList.soundString("Arrow", "HIT_ARROW_WOOD_SND", "sound.arrow.hit.wood"));
      soundsList.add(new SoundList.soundString("Arrow", "HIT_ARROW_METAL_SND", "sound.arrow.hit.metal"));
      soundsList.add(new SoundList.soundString("Arrow", "ARROW_FLY_SND", "sound.arrow.shot"));
      soundsList.add(new SoundList.soundString("Arrow", "ARROW_MISS_SND", "sound.arrow.miss"));
      soundsList.add(new SoundList.soundString("Arrow", "ARROW_AIM_SND", "sound.arrow.aim"));
      soundsList.add(new SoundList.soundString("Arrow", "HIT_ARROW_TREE_SND", "sound.arrow.stuck.wood"));
      soundsList.add(new SoundList.soundString("Arrow", "ARROW_HITGROUND_SND", "sound.arrow.stuck.ground"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_TING_SND", "sound.bell.handbell"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_CRAZYTING_SND", "sound.bell.handbell.long"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_DONG1_SND", "sound.bell.dong.1"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_DONG2_SND", "sound.bell.dong.2"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_DONG3_SND", "sound.bell.dong.3"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_DONG4_SND", "sound.bell.dong.4"));
      soundsList.add(new SoundList.soundString("Bell", "BELL_DONG5_SND", "sound.bell.dong.5"));
      soundsList.add(new SoundList.soundString("Bird", "LARCHSONG_SND", "sound.birdsong.bird4"));
      soundsList.add(new SoundList.soundString("Bird", "FINCHSONG_SND", "sound.birdsong.bird3"));
      soundsList.add(new SoundList.soundString("Bird", "THRUSHSONG_SND", "sound.birdsong.bird2"));
      soundsList.add(new SoundList.soundString("Bird", "OWLSONG_SND", "sound.birdsong.owl.short"));
      soundsList.add(new SoundList.soundString("Bird", "NIGHTJARSONG_SND", "sound.birdsong.bird1"));
      soundsList.add(new SoundList.soundString("Bird", "HAWKSONG_SND", "sound.birdsong.hawk"));
      soundsList.add(new SoundList.soundString("Bird", "CROWSONG_SND", "sound.birdsong.crows"));
      soundsList.add(new SoundList.soundString("Bird", "BIRD5SONG_SND", "sound.birdsong.bird5"));
      soundsList.add(new SoundList.soundString("Bird", "BIRD6SONG_SND", "sound.birdsong.bird6"));
      soundsList.add(new SoundList.soundString("Bird", "BIRD7SONG_SND", "sound.birdsong.bird7"));
      soundsList.add(new SoundList.soundString("Bird", "BIRD8SONG_SND", "sound.birdsong.bird8"));
      soundsList.add(new SoundList.soundString("Bird", "BIRD9SONG_SND", "sound.birdsong.bird9"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_MANDIBLES_1_SND", "sound.6.1.003.0001.001"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_MANDIBLES_2_SND", "sound.6.1.003.0001.002"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_CALL_1_SND", "sound.6.1.003.0002.001"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_CALL_2_SND", "sound.6.1.003.0002.002"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_HIT_1_SND", "sound.6.1.003.0003.001"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_HIT_2_SND", "sound.6.1.003.0003.002"));
      soundsList.add(new SoundList.soundString("Combat", "SCORP_SCREECH_SND", "sound.6.1.003.0003.003"));
      soundsList.add(new SoundList.soundString("Combat", "SHIELD_BASH_SND", "sound.combat.shield.bash"));
      soundsList.add(new SoundList.soundString("Combat", "SHIELD_WOOD_SND", "sound.combat.shield.wood"));
      soundsList.add(new SoundList.soundString("Combat", "SHIELD_METAL_SND", "sound.combat.shield.metal"));
      soundsList.add(new SoundList.soundString("Combat", "PARRY1_SND", "sound.combat.parry1"));
      soundsList.add(new SoundList.soundString("Combat", "PARRY2_SND", "sound.combat.parry2"));
      soundsList.add(new SoundList.soundString("Combat", "PARRY3_SND", "sound.combat.parry3"));
      soundsList.add(new SoundList.soundString("Combat", "FLESH1_SND", "sound.combat.fleshhit1"));
      soundsList.add(new SoundList.soundString("Combat", "FLESH2_SND", "sound.combat.fleshhit2"));
      soundsList.add(new SoundList.soundString("Combat", "FLESH3_SND", "sound.combat.fleshhit3"));
      soundsList.add(new SoundList.soundString("Combat", "FLESHMETAL1_SND", "sound.combat.fleshmetal1"));
      soundsList.add(new SoundList.soundString("Combat", "FLESHMETAL2_SND", "sound.combat.fleshmetal2"));
      soundsList.add(new SoundList.soundString("Combat", "FLESHMETAL3_SND", "sound.combat.fleshmetal3"));
      soundsList.add(new SoundList.soundString("Combat", "FLESHBONE1_SND", "sound.combat.fleshbone1"));
      soundsList.add(new SoundList.soundString("Combat", "FLESHBONE2_SND", "sound.combat.fleshbone2"));
      soundsList.add(new SoundList.soundString("Combat", "FLESHBONE3_SND", "sound.combat.fleshbone3"));
      soundsList.add(new SoundList.soundString("Combat", "MISS_LIGHT_SND", "sound.combat.miss.light"));
      soundsList.add(new SoundList.soundString("Combat", "MISS_MED_SND", "sound.combat.miss.med"));
      soundsList.add(new SoundList.soundString("Combat", "MISS_HEAVY_SND", "sound.combat.miss.heavy"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_MALE_SND", "sound.combat.hit.male"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_MALE_KID_SND", "sound.combat.hit.male.child"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_FEMALE_SND", "sound.combat.hit.female"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_FEMALE_KID_SND", "sound.combat.hit.female.child"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_ZOMBIE_SND", "sound.combat.hit.zombie"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SKELETON_SND", "sound.combat.hit.skeleton"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SPIRIT_MALE_SND", "sound.combat.hit.spirit.male"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SPIRIT_FEMALE_SND", "sound.combat.hit.spirit.female"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_COWBROWN_SND", "sound.combat.hit.cow.brown"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_DEER_SND", "sound.combat.hit.deer"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_HEN_SND", "sound.combat.hit.hen"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_WOLF_SND", "sound.combat.hit.wolf"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_LIZARD_SND", "sound.combat.hit.lizard"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_DEMON_SND", "sound.combat.hit.demon"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_DEATHCRAWLER_SND", "sound.combat.hit.deathcrawler"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SPAWN_UTTACHA_SND", "sound.combat.hit.uttacha.spawn"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SON_NOGUMP_SND", "sound.combat.hit.nogump.son"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_DRAKESPIRIT_SND", "sound.combat.hit.drakespirit"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_EAGLESPIRIT_SND", "sound.combat.hit.eaglespirit"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_EPIPHANY_VYNORA_SND", "sound.combat.hit.vynora.epiphany"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_JUGGERNAUT_MAGRANON_SND", "sound.combat.hit.magranon.juggernaut"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_MANIFESTATION_FO_SND", "sound.combat.hit.fo.manifestation"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_INCARNATION_LIBILA_SND", "sound.combat.hit.libila.incarnation"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_CROC_SND", "sound.combat.hit.croc"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_TROLL_SND", "sound.combat.hit.troll"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_PHEASANT_SND", "sound.combat.hit.pheasant"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_BEAR_SND", "sound.combat.hit.bear"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_INSECT_SND", "sound.combat.hit.insect"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_LION_SND", "sound.combat.hit.lion"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_RAT_SND", "sound.combat.hit.rat"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_CAT_SND", "sound.combat.hit.cat"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_DRAGON_SND", "sound.combat.hit.dragon"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_GIANT_SND", "sound.combat.hit.giant"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SPIDER_SND", "sound.combat.hit.spider"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_GOBLIN_SND", "sound.combat.hit.goblin"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_HORSE_SND", "sound.combat.hit.horse"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_OOZE_SND", "sound.combat.hit.ooze"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_GORILLA_SND", "sound.combat.hit.gorilla"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_PIG_SND", "sound.combat.hit.pig"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_SNAKE_SND", "sound.combat.hit.snake"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_DOG_SND", "sound.combat.hit.dog"));
      soundsList.add(new SoundList.soundString("Combat", "HIT_BISON_SND", "sound.combat.hit.bison"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_MALE_SND", "sound.death.male"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_MALE_KID_SND", "sound.death.male.child"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_FEMALE_SND", "sound.death.female"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_FEMALE_KID_SND", "sound.death.female.child"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_ZOMBIE_SND", "sound.combat.death.zombie"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SKELETON_SND", "sound.combat.death.skeleton"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SPIRIT_MALE_SND", "sound.death.spirit.male"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SPIRIT_FEMALE_SND", "sound.death.spirit.female"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_COWBROWN_SND", "sound.death.cow.brown"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_DEER_SND", "sound.death.deer"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_HEN_SND", "sound.death.hen"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_WOLF_SND", "sound.death.wolf"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_LIZARD_SND", "sound.death.lizard"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_DEMON_SND", "sound.death.demon"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_DEATHCRAWLER_SND", "sound.death.deathcrawler"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SPAWN_UTTACHA_SND", "sound.death.uttacha.spawn"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SON_NOGUMP_SND", "sound.death.nogump.son"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_DRAKESPIRIT_SND", "sound.death.drakespirit"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_EAGLESPIRIT_SND", "sound.death.eaglespirit"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_EPIPHANY_VYNORA_SND", "sound.death.vynora.epiphany"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_JUGGERNAUT_MAGRANON_SND", "sound.death.magranon.juggernaut"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_MANIFESTATION_FO_SND", "sound.death.fo.manifestation"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_INCARNATION_LIBILA_SND", "sound.death.libila.incarnation"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_CROC_SND", "sound.death.croc"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_PHEASANT_SND", "sound.death.pheasant"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_TROLL_SND", "sound.death.troll"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_BEAR_SND", "sound.death.bear"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_INSECT_SND", "sound.death.insect"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_LION_SND", "sound.death.lion"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_RAT_SND", "sound.death.rat"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_CAT_SND", "sound.death.cat"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_DRAGON_SND", "sound.death.dragon"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_GIANT_SND", "sound.death.giant"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SPIDER_SND", "sound.death.spider"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_GOBLIN_SND", "sound.death.goblin"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_HORSE_SND", "sound.death.horse"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_OOZE_SND", "sound.death.ooze"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_GORILLA_SND", "sound.death.gorilla"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_PIG_SND", "sound.death.pig"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_SNAKE_SND", "sound.death.snake"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_DOG_SND", "sound.death.dog"));
      soundsList.add(new SoundList.soundString("Death", "DEATH_BISON_SND", "sound.death.bison"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYWALLWOOD_AXE_SND", "sound.destroywall.wood.axe"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYWALLWOOD_MAUL_SND", "sound.destroywall.wood.maul"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYWALLSTONE_MAUL_SND", "sound.destroywall.stone.maul"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYWALLSTONE_AXE_SND", "sound.destroywall.stone.axe"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYITEMSTONE_AXE_SND", "sound.destroyobject.stone.axe"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYITEMSTONE_MAUL_SND", "sound.destroyobject.stone.maul"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYITEMWOOD_AXE_SND", "sound.destroyobject.wood.axe"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYITEMWOOD_MAUL_SND", "sound.destroyobject.wood.maul"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYITEMMETAL_AXE_SND", "sound.destroyobject.metal.axe"));
      soundsList.add(new SoundList.soundString("Destroy", "DESTROYITEMMETAL_MAUL_SND", "sound.destroyobject.metal.maul"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_CHUCKLE_SND", "sound.emote.chuckle"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_APPLAUD_SND", "sound.emote.applaud"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_KISS_SND", "sound.emote.kiss"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_COMFORT_SND", "sound.emote.comfort"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_WAVE_SND", "sound.emote.wave"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_CALL_SND", "sound.emote.call"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_DISAGREE_SND", "sound.emote.disagree"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_WORRY_SND", "sound.emote.worry"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_TEASE_SND", "sound.emote.tease"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_LAUGH_SND", "sound.emote.laugh"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_CRY_SND", "sound.emote.cry"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_SPIT_SND", "sound.emote.spit"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_FART_SND", "sound.emote.fart"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_INSULT_SND", "sound.emote.insult"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_CURSE_SND", "sound.emote.curse"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_SLAP_SND", "sound.emote.slap"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_WRONG_WAY_SND", "sound.emote.wrong.way"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_THAT_WAY_SND", "sound.emote.that.way"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_LEAD_SND", "sound.emote.lead"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_GOODBYE_SND", "sound.emote.goodbye"));
      soundsList.add(new SoundList.soundString("Emote", "EMOTE_FOLLOW_SND", "sound.emote.follow"));
      soundsList.add(new SoundList.soundString("Fx", "CONCH", "sound.fx.conch"));
      soundsList.add(new SoundList.soundString("Fx", "DRUMROLL", "sound.fx.drumroll"));
      soundsList.add(new SoundList.soundString("Fx", "ACHIEVEMENT", "sound.achievement"));
      soundsList.add(new SoundList.soundString("Fx", "ACHIEVEMENT_UPDATE", "sound.achievement.update"));
      soundsList.add(new SoundList.soundString("Fx", "FALLING_TREE", "sound.tree.falling"));
      soundsList.add(new SoundList.soundString("Fx", "FALLING_TREE2", "sound.tree.fall"));
      soundsList.add(new SoundList.soundString("Fx", "CHEST_OPENING", "sound.chest.open"));
      soundsList.add(new SoundList.soundString("Fx", "NOTIFICATION", "sound.notification"));
      soundsList.add(new SoundList.soundString("Fx", "ITEM_SPAWN_CENTRAL", "sound.spawn.item.central"));
      soundsList.add(new SoundList.soundString("Fx", "ITEM_SPAWN_PERIMETER", "sound.spawn.item.perimeter"));
      soundsList.add(new SoundList.soundString("Fx", "RIFTSPAWNCREATURES", "sound.rift.spawn"));
      soundsList.add(new SoundList.soundString("Fx", "CREATURELAND", "sound.creature.land.1"));
      soundsList.add(new SoundList.soundString("Fx", "RIFTCRYSTAL1", "sound.rift.crystal.1"));
      soundsList.add(new SoundList.soundString("Fx", "RIFTCRYSTAL2", "sound.rift.crystal.2"));
      soundsList.add(new SoundList.soundString("Fx", "RIFTCRYSTAL3", "sound.rift.crystal.3"));
      soundsList.add(new SoundList.soundString("Fx", "RIFTSHUT", "sound.rift.shut"));
      soundsList.add(new SoundList.soundString("Fx", "AMBIENT_BEES", "sound.bees"));
      soundsList.add(new SoundList.soundString("Fx", "HUMM_SND", "sound.fx.humm"));
      soundsList.add(new SoundList.soundString("Fx", "OOH_MALE_SND", "sound.fx.ooh.male"));
      soundsList.add(new SoundList.soundString("Fx", "OOH_FEMALE_SND", "sound.fx.ooh.female"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_NICE_1", "sound.5.2.009.0084.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_NICE_2", "sound.5.2.009.0085.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_NICE_3", "sound.5.2.009.0086.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_NICE_4", "sound.5.2.009.0087.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_ANGRY_1", "sound.5.2.009.0088.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_ANGRY_2", "sound.5.2.009.0089.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_ANGRY_3", "sound.5.2.009.0090.001"));
      soundsList.add(new SoundList.soundString("Gnome", "GNOME_ANGRY_4", "sound.2.4.021.0091.001"));
      soundsList.add(new SoundList.soundString("Liquid", "WATER_FIZZLE_SND", "sound.liquid.fzz"));
      soundsList.add(new SoundList.soundString("Liquid", "FILLCONTAINER_BARREL_SND", "sound.liquid.fillcontainer.barrel"));
      soundsList.add(new SoundList.soundString("Liquid", "FILLCONTAINER_BUCKET_SND", "sound.liquid.fillcontainer.bucket"));
      soundsList.add(new SoundList.soundString("Liquid", "FILLCONTAINER_JAR_SND", "sound.liquid.fillcontainer.jar"));
      soundsList.add(new SoundList.soundString("Liquid", "FILLCONTAINER_SND", "sound.liquid.fillcontainer"));
      soundsList.add(new SoundList.soundString("Liquid", "DRINKWATER_SND", "sound.liquid.drink"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_MOUNTAIN_1", "sound.7.1.006.0001.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_MOUNTAIN_2", "sound.7.1.006.0001.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_MOUNTAIN_3", "sound.7.1.006.0001.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_MOUNTAIN_4", "sound.7.1.006.0001.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_MOUNTAIN_5", "sound.7.1.006.0001.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_MOUNTAIN_6", "sound.7.1.006.0001.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRASS_1", "sound.7.1.006.0002.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRASS_2", "sound.7.1.006.0002.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRASS_3", "sound.7.1.006.0002.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRASS_4", "sound.7.1.006.0002.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRASS_5", "sound.7.1.006.0002.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRASS_6", "sound.7.1.006.0002.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_COBBLE_1", "sound.7.1.006.0003.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_COBBLE_2", "sound.7.1.006.0003.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_COBBLE_3", "sound.7.1.006.0003.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_COBBLE_4", "sound.7.1.006.0003.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_COBBLE_5", "sound.7.1.006.0003.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_COBBLE_6", "sound.7.1.006.0003.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_DIRT_1", "sound.7.1.006.0004.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_DIRT_2", "sound.7.1.006.0004.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_DIRT_3", "sound.7.1.006.0004.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_DIRT_4", "sound.7.1.006.0004.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_DIRT_5", "sound.7.1.006.0004.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_DIRT_6", "sound.7.1.006.0004.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRAVEL_1", "sound.7.1.006.0005.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRAVEL_2", "sound.7.1.006.0005.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRAVEL_3", "sound.7.1.006.0005.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRAVEL_4", "sound.7.1.006.0005.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRAVEL_5", "sound.7.1.006.0005.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_GRAVEL_6", "sound.7.1.006.0005.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_WOOD_1", "sound.7.1.006.0006.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_WOOD_2", "sound.7.1.006.0006.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_WOOD_3", "sound.7.1.006.0006.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_WOOD_4", "sound.7.1.006.0006.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_WOOD_5", "sound.7.1.006.0006.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_WOOD_6", "sound.7.1.006.0006.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SLAB_1", "sound.7.1.006.0007.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SLAB_2", "sound.7.1.006.0007.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SLAB_3", "sound.7.1.006.0007.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SLAB_4", "sound.7.1.006.0007.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SLAB_5", "sound.7.1.006.0007.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SLAB_6", "sound.7.1.006.0007.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SAND_1", "sound.7.1.006.0008.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SAND_2", "sound.7.1.006.0008.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SAND_3", "sound.7.1.006.0008.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SAND_4", "sound.7.1.006.0008.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SAND_5", "sound.7.1.006.0008.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SAND_6", "sound.7.1.006.0008.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_STEPPE_1", "sound.7.1.006.0009.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_STEPPE_2", "sound.7.1.006.0009.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_STEPPE_3", "sound.7.1.006.0009.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_STEPPE_4", "sound.7.1.006.0009.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_STEPPE_5", "sound.7.1.006.0009.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_STEPPE_6", "sound.7.1.006.0009.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CAVE_1", "sound.7.1.006.0010.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CAVE_2", "sound.7.1.006.0010.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CAVE_3", "sound.7.1.006.0010.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CAVE_4", "sound.7.1.006.0010.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CAVE_5", "sound.7.1.006.0010.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CAVE_6", "sound.7.1.006.0010.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SHORE_1", "sound.7.1.006.0011.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SHORE_2", "sound.7.1.006.0011.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SHORE_3", "sound.7.1.006.0011.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SHORE_4", "sound.7.1.006.0011.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SHORE_5", "sound.7.1.006.0011.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SHORE_6", "sound.7.1.006.0011.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_CONSTANTLOOP", "sound.7.1.006.0018.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_QUICK_RUSTLE", "sound.7.1.006.0019.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SWIMFAST_LOOP", "sound.7.1.006.0020.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SWIM_SLOW_LOOP", "sound.7.1.006.0021.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_SWIM_BOBBING", "sound.7.1.006.0022.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_ROLL_1", "sound.7.1.006.0023.001"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_ROLL_2", "sound.7.1.006.0023.002"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_ROLL_3", "sound.7.1.006.0023.003"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_ROLL_4", "sound.7.1.006.0023.004"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_ROLL_5", "sound.7.1.006.0023.005"));
      soundsList.add(new SoundList.soundString("Movement", "MOVE_ROLL_6", "sound.7.1.006.0023.006"));
      soundsList.add(new SoundList.soundString("Movement", "MOVEITEM_SND", "sound.object.move.pushpull"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_BLACKLIGHT_SND", "sound.music.song.blacklight"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_CAVEHALL1_SND", "sound.music.song.cavehall1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_CAVEHALL2_SND", "sound.music.song.cavehall2"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_COLOSSUS_SND", "sound.music.song.colossus"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_DISBAND_SND", "sound.music.song.disbandvillage"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_DYING1_SND", "sound.music.song.dying1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_DYING2_SND", "sound.music.song.dying2"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ECHOES1_SND", "sound.music.song.echoes1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ECHOES2_SND", "sound.music.song.echoes2"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ECHOES3_SND", "sound.music.song.echoes3"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ECHOES4_SND", "sound.music.song.echoes4"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ECHOES5_SND", "sound.music.song.echoes5"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ECHOES6_SND", "sound.music.song.echoes6"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_FOUNDSETTLEMENT_SND", "sound.music.song.foundsettlement"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_MOUNTAINTOP_SND", "sound.music.song.mountaintop"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_PRAYINGLIBILA_SND", "sound.music.song.prayinglibila"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_PRAYINGMAGRANON_SND", "sound.music.song.prayingmagranon"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_PRAYINGVYNORA_SND", "sound.music.song.prayingvynora"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_PRAYINGFO_SND", "sound.music.song.prayingfo"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_SHANTY1_SND", "sound.music.song.shanty1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_SUNRISEPASS_SND", "sound.music.song.sunrisepass"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_SUNRISE1_SND", "sound.music.song.sunrise1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TERRITORYHOTS_SND", "sound.music.song.territoryhots"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TERRITORYWL_SND", "sound.music.song.territorywl"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELLING1_SND", "sound.music.song.travelling1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELLING2_SND", "sound.music.song.travelling2"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELLING3_SND", "sound.music.song.travelling3"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WHITELIGHT_SND", "sound.music.song.whitelight"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_VILLAGERAIN_SND", "sound.music.song.villagerain"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_VILLAGESUN_SND", "sound.music.song.villagesun"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_VILLAGEWORK_SND", "sound.music.song.villagework"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WURMISWAITING_SND", "sound.music.song.wurmiswaiting"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ANTHEMHOTS_SND", "sound.music.song.anthemhots"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ANTHEMMOLREHAN_SND", "sound.music.song.anthemmolrehan"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_ANTHEMJENN_SND", "sound.music.song.anthemjenn"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_LOADING1_SND", "sound.music.song.loading1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_LOADING1A_SND", "sound.music.song.loading1a"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_LOADING2_SND", "sound.music.song.loading2"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_LOADING2A_SND", "sound.music.song.loading2a"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_QUIT_F12_SND", "sound.music.song.quit-f12"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_QUIT_NO_SND", "sound.music.song.quit-no"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_QUIT_YES_SND", "sound.music.song.quit-yes"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_SPAWN_SND", "sound.music.song.spawn1"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_OPENCHRISTMAS_SND", "sound.music.song.christmas"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_WAKING_SND", "sound.music.song.wakingup"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_FINGER_SND", "sound.music.song.fingerfo"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_INEYES_SND", "sound.music.song.inyoureyes"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_BEATING_SND", "sound.music.song.beatinganvil"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_PROMISING_SND", "sound.music.song.promisingfoal"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_SUMMER_SND", "sound.music.song.longsummer"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_WHYDIVE_SND", "sound.music.song.whyyoudive"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_NORTH_SND", "sound.music.song.north"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_THROUGH_SND", "sound.music.song.through"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_SHORES_SND", "sound.music.song.shores"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_STRIDE_SND", "sound.music.song.stride"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_RIDGE_SND", "sound.music.song.ridge"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_SKYFIRE_SND", "sound.music.song.skyfire"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_TRAVELEXP_FAMILIAR_SND", "sound.music.song.familiar"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_FINGER_TONING_SND", "sound.music.song.fingertone"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_WORKMED_PROMISTONE_SND", "sound.music.song.promisetone"));
      soundsList.add(new SoundList.soundString("Music", "MUSIC_GNO_SND", "sound.5.2.009.0083.001"));
      soundsList.add(new SoundList.soundString("Music", "SONG_ABANDON", "sound.music.song.abandon"));
      soundsList.add(new SoundList.soundString("Music", "SONG_BACKHOME", "sound.music.song.backhome"));
      soundsList.add(new SoundList.soundString("Music", "SONG_DEADWATER", "sound.music.song.deadwater"));
      soundsList.add(new SoundList.soundString("Music", "SONG_CONTACT", "sound.music.song.contact"));
      soundsList.add(new SoundList.soundString("Music", "SONG_SUNGLOW", "sound.music.song.sunglow"));
      soundsList.add(new SoundList.soundString("Music", "SONG_TURMOILED", "sound.music.song.turmoiled"));
      soundsList.add(new SoundList.soundString("Music", "SONG_DANCEHORDE", "sound.music.song.dancehorde"));
      soundsList.add(new SoundList.soundString("Music", "SONG_UNLIMITED", "sound.music.song.unlimited"));
      soundsList.add(new SoundList.soundString("Other", "LOCKUNLOCK_SND", "sound.object.lockunlock"));
      soundsList.add(new SoundList.soundString("Other", "EATFOOD_SND", "sound.food.eat"));
      soundsList.add(new SoundList.soundString("Other", "BRANCHSNAP_SND", "sound.forest.branchsnap"));
      soundsList.add(new SoundList.soundString("Other", "FIREWORKS_SND", "sound.object.fzz"));
      soundsList.add(new SoundList.soundString("Other", "PICK_BREAK_SND", "sound.object.lockpick.break.ogg"));
      soundsList.add(new SoundList.soundString("Other", "DOOR_OPEN_SND", "sound.door.open"));
      soundsList.add(new SoundList.soundString("Other", "DOOR_CLOSE_SND", "sound.door.close"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_SBOAT_MOVING_LEFT1", "sound.7.3.007.0001.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_SBOAT_STILL_LEFT1", "sound.7.3.007.0002.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_LBOAT_STILL_LEFT1", "sound.7.3.007.0003.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_LBOAT_MOVING_LEFT1", "sound.7.3.007.0004.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_ROWBOAT_STILL_LEFT1", "sound.7.3.007.0006.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_ROWBOAT_MOVING_LEFT1", "sound.7.3.007.0005.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_SBOAT_MOVING_RIGHT1", "sound.7.4.007.0001.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_SBOAT_STILL_RIGHT1", "sound.7.4.007.0002.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_LBOAT_STILL_RIGHT1", "sound.7.4.007.0003.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_LBOAT_MOVING_RIGHT1", "sound.7.4.007.0004.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_ROWBOAT_STILL_RIGHT1", "sound.7.4.007.0006.001"));
      soundsList.add(new SoundList.soundString("Other", "SPOT_ROWBOAT_MOVING_RIGHT1", "sound.7.4.007.0005.001"));
      soundsList.add(new SoundList.soundString("Religion", "RELIGION_CHANNEL_SND", "sound.religion.channel"));
      soundsList.add(new SoundList.soundString("Religion", "RELIGION_PRAYER_SND", "sound.religion.prayer"));
      soundsList.add(new SoundList.soundString("Religion", "RELIGION_DESECRATE_SND", "sound.religion.desecrate"));
      soundsList.add(new SoundList.soundString("Religion", "RELIGION_PREACH_SND", "sound.religion.preach"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_SET_SND", "sound.trap.set"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_DISARM_SND", "sound.trap.disarm"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_THUK_SND", "sound.trap.thuk"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_SWISH_SND", "sound.trap.swish"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_WHAM_SND", "sound.trap.wham"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_SCITH_SND", "sound.trap.scith"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_CHAK_SND", "sound.trap.chak"));
      soundsList.add(new SoundList.soundString("Trap", "TRAP_SPLASH_SND", "sound.trap.splash"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_FOREST_RAIN_1_LEFT", "sound.3.3.013.0004.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_FOREST_WIND_1_LEFT", "sound.3.3.013.0007.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_FOREST_THUNDER_1_LEFT", "sound.3.3.013.0006.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_FOREST_RAIN_1_RIGHT", "sound.3.4.013.0004.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_FOREST_WIND_1_RIGHT", "sound.3.4.013.0007.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_FOREST_THUNDER_1_RIGHT", "sound.3.4.013.0006.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_LAKE_WIND_1_LEFT", "sound.3.3.014.0004.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_LAKE_WIND_1_RIGHT", "sound.3.4.014.0004.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_LAKE_RAIN_1_LEFT", "sound.3.3.014.0005.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_LAKE_RAIN_1_RIGHT", "sound.3.4.014.0005.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_MID_DIST_THUNDER_LEFT", "sound.3.3.002.0005.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_LIGHT_BREEZE_LEFT", "sound.3.3.002.0002.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_DISTANT_THUNDER_1_LEFT", "sound.3.3.002.0001.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_DISTANT_THUNDER_1_RIGHT", "sound.3.4.002.0001.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_DISTANT_THUNDER_2_LEFT", "sound.3.3.002.0001.002"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_DISTANT_THUNDER_2_RIGHT", "sound.3.4.002.0001.002"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_MID_DIST_THUNDER_RIGHT", "sound.3.4.002.0005.001"));
      soundsList.add(new SoundList.soundString("Weather", "WEATHER_LIGHT_BREEZE_RIGHT", "sound.3.4.002.0002.001"));
      soundsList.add(new SoundList.soundString("Work", "HAMMERONWOOD1_SND", "sound.work.carpentry.mallet1"));
      soundsList.add(new SoundList.soundString("Work", "HAMMERONWOOD2_SND", "sound.work.carpentry.mallet2"));
      soundsList.add(new SoundList.soundString("Work", "CARPENTRY_SAW_SND", "sound.work.carpentry.saw"));
      soundsList.add(new SoundList.soundString("Work", "CARPENTRY_RASP_SND", "sound.work.carpentry.rasp"));
      soundsList.add(new SoundList.soundString("Work", "CARPENTRY_KNIFE_SND", "sound.work.carpentry.carvingknife"));
      soundsList.add(new SoundList.soundString("Work", "CARPENTRY_POLISH_SND", "sound.work.carpentry.polish"));
      soundsList.add(new SoundList.soundString("Work", "SMITHING_HAMMER_SND", "sound.work.smithing.hammer"));
      soundsList.add(new SoundList.soundString("Work", "SMITHING_WHET_SND", "sound.work.smithing.whetstone"));
      soundsList.add(new SoundList.soundString("Work", "SMITHING_TEMPER_SND", "sound.work.smithing.temper"));
      soundsList.add(new SoundList.soundString("Work", "SMITHING_POLISH_SND", "sound.work.smithing.polish"));
      soundsList.add(new SoundList.soundString("Work", "TAILORING_LOOM_SND", "sound.work.tailoring.loom"));
      soundsList.add(new SoundList.soundString("Work", "TAILORING_SPINDLE_SND", "sound.work.tailoring.spindle"));
      soundsList.add(new SoundList.soundString("Work", "FARMING_HARVEST_SND", "sound.work.farming.harvest"));
      soundsList.add(new SoundList.soundString("Work", "FARMING_SCYTHE_SND", "sound.work.farming.scythe"));
      soundsList.add(new SoundList.soundString("Work", "FARMING_RAKE_SND", "sound.work.farming.rake"));
      soundsList.add(new SoundList.soundString("Work", "FIRSTAID_BANDAGE_SND", "sound.work.firstaid.bandage"));
      soundsList.add(new SoundList.soundString("Work", "PAVING_SND", "sound.work.paving"));
      soundsList.add(new SoundList.soundString("Work", "PACKING_SND", "sound.work.digging.pack"));
      soundsList.add(new SoundList.soundString("Work", "WOODCUTTING_KINDLING_SND", "sound.work.woodcutting.kindling"));
      soundsList.add(new SoundList.soundString("Work", "HAMMERONMETAL_SND", "sound.work.smithing.metal"));
      soundsList.add(new SoundList.soundString("Work", "HAMMERONSTONE_SND", "sound.work.masonry"));
      soundsList.add(new SoundList.soundString("Work", "DIGGING1_SND", "sound.work.digging1"));
      soundsList.add(new SoundList.soundString("Work", "DIGGING2_SND", "sound.work.digging2"));
      soundsList.add(new SoundList.soundString("Work", "DIGGING3_SND", "sound.work.digging3"));
      soundsList.add(new SoundList.soundString("Work", "MINING1_SND", "sound.work.mining1"));
      soundsList.add(new SoundList.soundString("Work", "MINING2_SND", "sound.work.mining2"));
      soundsList.add(new SoundList.soundString("Work", "MINING3_SND", "sound.work.mining3"));
      soundsList.add(new SoundList.soundString("Work", "FORAGEBOT_SND", "sound.work.foragebotanize"));
      soundsList.add(new SoundList.soundString("Work", "PROSPECTING1_SND", "sound.work.prospecting1"));
      soundsList.add(new SoundList.soundString("Work", "PROSPECTING2_SND", "sound.work.prospecting2"));
      soundsList.add(new SoundList.soundString("Work", "PROSPECTING3_SND", "sound.work.prospecting3"));
      soundsList.add(new SoundList.soundString("Work", "STONECUTTING_SND", "sound.work.stonecutting"));
      soundsList.add(new SoundList.soundString("Work", "GROOMING_SND", "sound.work.horse.groom"));
      soundsList.add(new SoundList.soundString("Work", "MILKING_SND", "sound.work.milking"));
      soundsList.add(new SoundList.soundString("Work", "FARMING_SND", "sound.work.farming"));
      soundsList.add(new SoundList.soundString("Work", "WOODCUTTING1_SND", "sound.work.woodcutting1"));
      soundsList.add(new SoundList.soundString("Work", "WOODCUTTING2_SND", "sound.work.woodcutting2"));
      soundsList.add(new SoundList.soundString("Work", "WOODCUTTING3_SND", "sound.work.woodcutting3"));
      soundsList.add(new SoundList.soundString("Work", "FLINTSTEEL_SND", "sound.fire.lighting.flintsteel"));
      soundsList.add(new SoundList.soundString("Work", "TOOL_BUTCHERS_KNIFE", "sound.butcherKnife"));
      soundsList.add(new SoundList.soundString("Work", "TOOL_FORK", "sound.forkMix"));
      soundsList.add(new SoundList.soundString("Work", "TOOL_MORTAR_AND_PESTLE", "sound.grindSpice"));
      soundsList.add(new SoundList.soundString("Work", "TOOL_GRINDSTONE", "sound.grindstone"));
      soundsList.add(new SoundList.soundString("Work", "TOOL_KNIFE", "sound.knifeChop"));
      soundsList.add(new SoundList.soundString("Work", "TOOL_PRESS", "sound.press"));
      this.soundStrings = soundsList.toArray(new SoundList.soundString[soundsList.size()]);
      this.catStrings = new String[]{
         "None",
         "Ambient",
         "Arrow",
         "Bell",
         "Bird",
         "Combat",
         "Death",
         "Destroy",
         "Emote",
         "Fx",
         "Gnome",
         "Liquid",
         "Movement",
         "Music",
         "Other",
         "Religion",
         "Trap",
         "Weather",
         "Work"
      };
   }

   @Override
   public void answer(Properties aAnswers) {
      this.setAnswer(aAnswers);
      boolean back = this.getBooleanProp("back");
      boolean close = this.getBooleanProp("close");
      if (!back && !close) {
         boolean filter = this.getBooleanProp("filter");
         if (filter) {
            this.showCat = this.getIntProp("cat");
            this.reshow();
         } else {
            boolean playSound = this.getBooleanProp("playSound");
            if (playSound) {
               int sel = this.getIntProp("sel");

               for(SoundList.soundString ss : this.soundStrings) {
                  if (ss.getId() == sel) {
                     this.selected = ss.getSoundName();
                     SoundPlayer.playSound(this.selected, this.getResponder(), 1.5F);
                     this.reshow();
                     return;
                  }
               }
            }

            boolean select = this.getBooleanProp("select");
            if (select && this.root != null) {
               int sel = this.getIntProp("sel");
               if (sel == 0) {
                  this.root.cloneAndSendManageEffect(null);
                  return;
               }

               for(SoundList.soundString ss : this.soundStrings) {
                  if (ss.getId() == sel) {
                     this.root.cloneAndSendManageEffect(ss.getSoundName());
                     return;
                  }
               }
            }

            for(String key : this.getAnswer().stringPropertyNames()) {
               if (key.startsWith("sort")) {
                  String sid = key.substring(4);
                  this.sortBy = Integer.parseInt(sid);
                  break;
               }
            }

            this.reshow();
         }
      } else {
         if (this.root != null) {
            this.root.cloneAndSendManageEffect(null);
         }
      }
   }

   void reshow() {
      SoundList sl = new SoundList(this.getResponder(), this.getTitle(), this.getQuestion());
      sl.root = this.root;
      sl.selected = this.selected;
      sl.showCat = this.showCat;
      sl.sortBy = this.sortBy;
      sl.sendQuestion();
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "border{border{size=\"20,40\";null;null;varray{rescale=\"true\";harray{label{type=\"bold\";text=\""
            + this.question
            + "\"};}harray{label{text=\"show Category \"};dropdown{id=\"cat\";options=\"None,Ambient,Arrow,Bell,Bird,Combat,Death,Destroy,Emote,Fx,Gnome,Liquid,Movement,Music,Other,Religion,Trap,Weather,Work\"default=\""
            + this.showCat
            + "\"}}}varray{harray{label{text=\"           \"};button{text=\"Back\";id=\"back\"};label{text=\" \"}}harray{label{text=\" \"};button{text=\"Apply Filter\";id=\"filter\"};label{text=\" \"}}}null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      buf.append("closebutton{id=\"close\"};");
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      switch(absSortBy) {
         case 1:
            Arrays.sort(this.soundStrings, new Comparator<SoundList.soundString>() {
               public int compare(SoundList.soundString param1, SoundList.soundString param2) {
                  return param1.getCategory().compareTo(param2.getCategory()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(this.soundStrings, new Comparator<SoundList.soundString>() {
               public int compare(SoundList.soundString param1, SoundList.soundString param2) {
                  return param1.getName().compareTo(param2.getName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(this.soundStrings, new Comparator<SoundList.soundString>() {
               public int compare(SoundList.soundString param1, SoundList.soundString param2) {
                  return param1.getSoundName().compareTo(param2.getSoundName()) * upDown;
               }
            });
      }

      buf.append("table{rows=\"1\";cols=\"4\";");
      buf.append(
         "label{text=\"\"};"
            + this.colHeader("Category", 1, this.sortBy)
            + this.colHeader("Name", 2, this.sortBy)
            + this.colHeader("Sound Name", 3, this.sortBy)
      );
      boolean noneSel = true;

      for(SoundList.soundString ss : this.soundStrings) {
         boolean show = this.showCat == 0;
         boolean sel = this.selected.equals(ss.getSoundName());
         if (sel) {
            noneSel = false;
            show = true;
         }

         if (!show && ss.getCategory().equals(this.catStrings[this.showCat])) {
            show = true;
         }

         if (show) {
            buf.append(
               "radio{group=\"sel\";id=\""
                  + ss.getId()
                  + "\";selected=\""
                  + sel
                  + "\";text=\"\"}label{text=\""
                  + ss.getCategory()
                  + "\"};label{text=\""
                  + ss.getName()
                  + "\"};label{text=\""
                  + ss.getSoundName()
                  + "\"};"
            );
         }
      }

      buf.append("}");
      buf.append("radio{group=\"sel\";id=\"0\";selected=\"" + noneSel + "\";text=\"None\"}");
      buf.append("}};null;");
      buf.append("varray{rescale=\"true\";");
      buf.append("text{text=\"Select sound and choose what to do\"}");
      buf.append("harray{button{id=\"select\";text=\"Select\"};label{text=\"  \"};button{id=\"playSound\";text=\"Play sound\"};}");
      buf.append("}");
      buf.append("}");
      this.getResponder().getCommunicator().sendBml(500, 500, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   void setRoot(MissionManager aRoot) {
      this.root = aRoot;
   }

   void setSelected(String soundName) {
      this.selected = soundName;
   }

   private int getIntProp(String key) {
      String svalue = this.getStringProp(key);
      int value = 0;

      try {
         return Integer.parseInt(svalue);
      } catch (NumberFormatException var5) {
         return 0;
      }
   }

   class soundString {
      private final int id = SoundList.this.nextId++;
      private final String category;
      private final String name;
      private final String soundName;

      soundString(String category, String name, String soundName) {
         this.category = category;
         this.name = name;
         this.soundName = soundName;
      }

      int getId() {
         return this.id;
      }

      String getCategory() {
         return this.category;
      }

      String getName() {
         return this.name;
      }

      String getSoundName() {
         return this.soundName;
      }
   }
}

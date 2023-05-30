package com.wurmonline.server.zones;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SpawnTable implements CreatureTemplateIds {
   private static final Logger logger = Logger.getLogger(SpawnTable.class.getName());
   private static final Set<EncounterType> land = new HashSet<>();
   private static final Set<EncounterType> water = new HashSet<>();
   private static final Set<EncounterType> beach = new HashSet<>();
   private static final Set<EncounterType> deepwater = new HashSet<>();
   private static final Set<EncounterType> flying = new HashSet<>();
   private static final Set<EncounterType> flyinghigh = new HashSet<>();
   private static final Set<EncounterType> caves = new HashSet<>();

   private SpawnTable() {
   }

   private static void addTileType(EncounterType type) {
      if (type.getElev() == 0) {
         land.add(type);
      } else if (type.getElev() == 2) {
         deepwater.add(type);
      } else if (type.getElev() == 1) {
         water.add(type);
      } else if (type.getElev() == 3) {
         flying.add(type);
      } else if (type.getElev() == 4) {
         flyinghigh.add(type);
      } else if (type.getElev() == -1) {
         caves.add(type);
      } else if (type.getElev() == 5) {
         beach.add(type);
      } else {
         logger.warning("Cannot add unknown EncounterType: " + type);
      }
   }

   public static EncounterType getType(byte tiletype, byte elevation) {
      Set<EncounterType> types = land;
      if (elevation == 2) {
         types = deepwater;
      } else if (elevation == 1) {
         types = water;
      } else if (elevation == 3) {
         types = flying;
      } else if (elevation == 4) {
         types = flyinghigh;
      } else if (elevation == -1) {
         types = caves;
      } else if (elevation == 5) {
         types = beach;
      }

      for(EncounterType type : types) {
         if (type.getTiletype() == tiletype) {
            return type;
         }
      }

      return null;
   }

   public static Encounter getRandomEncounter(byte tiletype, byte elevation) {
      EncounterType enc = getType(tiletype, elevation);
      if (enc != null) {
         Encounter t = enc.getRandomEncounter();
         return t == EncounterType.NULL_ENCOUNTER ? null : t;
      } else {
         return null;
      }
   }

   static void createEncounters() {
      logger.info("Creating Encounters");
      long now = System.nanoTime();
      Encounter cow = new Encounter();
      cow.addType(3, 1);
      Encounter sheep = new Encounter();
      sheep.addType(96, 1);
      Encounter ram = new Encounter();
      sheep.addType(102, 1);
      Encounter anaconda = new Encounter();
      anaconda.addType(38, 1);
      Encounter horse = new Encounter();
      horse.addType(64, 2);
      Encounter wolf = new Encounter();
      wolf.addType(10, 4);
      Encounter bearbrown = new Encounter();
      bearbrown.addType(12, 2);
      Encounter bearblack = new Encounter();
      bearblack.addType(42, 2);
      Encounter rat = new Encounter();
      rat.addType(13, 3);
      Encounter mountainlion = new Encounter();
      mountainlion.addType(14, 2);
      Encounter wildcat = new Encounter();
      wildcat.addType(15, 2);
      Encounter cavebugs = new Encounter();
      cavebugs.addType(43, 5);
      Encounter pig = new Encounter();
      pig.addType(44, 3);
      Encounter deer = new Encounter();
      deer.addType(54, 2);
      Encounter bison = new Encounter();
      bison.addType(82, 10);
      Encounter bull = new Encounter();
      bull.addType(49, 3);
      Encounter calf = new Encounter();
      calf.addType(50, 1);
      Encounter hen = new Encounter();
      hen.addType(45, 3);
      Encounter rooster = new Encounter();
      rooster.addType(52, 1);
      Encounter pheasant = new Encounter();
      pheasant.addType(55, 2);
      Encounter dog = new Encounter();
      dog.addType(51, 2);
      Encounter spider = new Encounter();
      spider.addType(25, 6);
      Encounter lavaSpiderMulti = new Encounter();
      lavaSpiderMulti.addType(56, 10);
      Encounter lavaSpiderSingle = new Encounter();
      lavaSpiderSingle.addType(56, 1);
      Encounter scorpionSingle = new Encounter();
      scorpionSingle.addType(59, 3);
      Encounter crocodileSingle = new Encounter();
      crocodileSingle.addType(58, 1);
      Encounter lavaCreatureSingle = new Encounter();
      lavaCreatureSingle.addType(57, 1);
      Encounter trollSingle = new Encounter();
      trollSingle.addType(11, 1);
      Encounter hellHorseSingle = new Encounter();
      hellHorseSingle.addType(83, 1);
      Encounter hellHoundSingle = new Encounter();
      hellHoundSingle.addType(84, 1);
      Encounter hellScorpSingle = new Encounter();
      hellScorpSingle.addType(85, 1);
      Encounter sealPair = new Encounter();
      sealPair.addType(93, 2);
      Encounter crabSingle = new Encounter();
      crabSingle.addType(95, 1);
      Encounter tortoiseSingle = new Encounter();
      tortoiseSingle.addType(94, 1);
      Encounter uttacha = new Encounter();
      uttacha.addType(74, 1);
      Encounter eagleSpirit = new Encounter();
      eagleSpirit.addType(77, 1);
      Encounter crawler = new Encounter();
      crawler.addType(73, 1);
      Encounter nogump = new Encounter();
      nogump.addType(75, 1);
      Encounter drakeSpirit = new Encounter();
      drakeSpirit.addType(76, 1);
      Encounter demonsol = new Encounter();
      demonsol.addType(72, 1);
      Encounter unicorn = new Encounter();
      unicorn.addType(21, 1);
      EncounterType grassGround = new EncounterType(Tiles.Tile.TILE_GRASS.id, (byte)0);
      grassGround.addEncounter(cow, 1);
      grassGround.addEncounter(wildcat, 2);
      grassGround.addEncounter(dog, 3);
      grassGround.addEncounter(hen, 1);
      grassGround.addEncounter(rooster, 1);
      grassGround.addEncounter(calf, 1);
      grassGround.addEncounter(bull, 1);
      grassGround.addEncounter(pheasant, 1);
      grassGround.addEncounter(horse, 2);
      grassGround.addEncounter(sheep, 3);
      grassGround.addEncounter(ram, 1);
      grassGround.addEncounter(unicorn, 1);
      addTileType(grassGround);
      EncounterType beachSide = new EncounterType(Tiles.Tile.TILE_SAND.id, (byte)5);
      beachSide.addEncounter(crabSingle, 8);
      beachSide.addEncounter(tortoiseSingle, 1);
      beachSide.addEncounter(sealPair, 3);
      if (Servers.localServer.isChallengeServer()) {
         beachSide.addEncounter(crawler, 1);
         beachSide.addEncounter(uttacha, 1);
      }

      addTileType(beachSide);
      EncounterType rockSide = new EncounterType(Tiles.Tile.TILE_ROCK.id, (byte)1);
      rockSide.addEncounter(rat, 2);
      rockSide.addEncounter(sealPair, 2);
      rockSide.addEncounter(lavaCreatureSingle, 1);
      rockSide.addEncounter(EncounterType.NULL_ENCOUNTER, 5);
      if (Servers.localServer.isChallengeServer()) {
         rockSide.addEncounter(uttacha, 1);
      }

      addTileType(rockSide);
      EncounterType mycGround = new EncounterType(Tiles.Tile.TILE_MYCELIUM.id, (byte)0);
      mycGround.addEncounter(spider, 4);
      mycGround.addEncounter(rat, 2);
      mycGround.addEncounter(dog, 1);
      mycGround.addEncounter(wolf, 1);
      mycGround.addEncounter(unicorn, 1);
      mycGround.addEncounter(hellHorseSingle, 1);
      mycGround.addEncounter(hellHoundSingle, 1);
      if (Servers.localServer.isChallengeServer()) {
         mycGround.addEncounter(demonsol, 1);
         mycGround.addEncounter(crawler, 1);
      }

      addTileType(mycGround);
      EncounterType marsh = new EncounterType(Tiles.Tile.TILE_MARSH.id, (byte)0);
      marsh.addEncounter(rat, 2);
      marsh.addEncounter(anaconda, 1);
      if (Servers.localServer.isChallengeServer()) {
         marsh.addEncounter(nogump, 1);
         marsh.addEncounter(demonsol, 1);
      }

      addTileType(marsh);
      EncounterType steppe = new EncounterType(Tiles.Tile.TILE_STEPPE.id, (byte)0);
      steppe.addEncounter(pheasant, 1);
      steppe.addEncounter(horse, 4);
      steppe.addEncounter(wildcat, 1);
      steppe.addEncounter(hellHorseSingle, 1);
      steppe.addEncounter(bison, 1);
      steppe.addEncounter(sheep, 1);
      steppe.addEncounter(ram, 1);
      if (Servers.localServer.isChallengeServer()) {
         steppe.addEncounter(drakeSpirit, 1);
         steppe.addEncounter(eagleSpirit, 1);
      }

      addTileType(steppe);
      EncounterType treeGround = new EncounterType(Tiles.Tile.TILE_TREE.id, (byte)0);
      treeGround.addEncounter(pig, 1);
      treeGround.addEncounter(wolf, 1);
      treeGround.addEncounter(bearbrown, 1);
      treeGround.addEncounter(hellHoundSingle, 1);
      treeGround.addEncounter(pheasant, 1);
      treeGround.addEncounter(deer, 1);
      treeGround.addEncounter(spider, 2);
      treeGround.addEncounter(trollSingle, 1);
      treeGround.addEncounter(mountainlion, 1);
      if (Servers.localServer.isChallengeServer()) {
         treeGround.addEncounter(demonsol, 1);
         treeGround.addEncounter(crawler, 1);
      }

      addTileType(treeGround);
      EncounterType sandGround = new EncounterType(Tiles.Tile.TILE_SAND.id, (byte)0);
      sandGround.addEncounter(crocodileSingle, 10);
      sandGround.addEncounter(scorpionSingle, 10);
      sandGround.addEncounter(hellScorpSingle, 1);
      sandGround.addEncounter(anaconda, 1);
      addTileType(sandGround);
      EncounterType clayGround = new EncounterType(Tiles.Tile.TILE_CLAY.id, (byte)0);
      clayGround.addEncounter(crocodileSingle, 10);
      clayGround.addEncounter(anaconda, 1);
      addTileType(clayGround);
      EncounterType underGround = new EncounterType(Tiles.Tile.TILE_CAVE.id, (byte)-1);
      underGround.addEncounter(bearblack, 4);
      underGround.addEncounter(rat, 2);
      underGround.addEncounter(cavebugs, 5);
      underGround.addEncounter(spider, 2);
      underGround.addEncounter(lavaSpiderSingle, 2);
      underGround.addEncounter(lavaCreatureSingle, 4);
      underGround.addEncounter(mountainlion, 1);
      addTileType(underGround);
      EncounterType lavaGround = new EncounterType(Tiles.Tile.TILE_LAVA.id, (byte)0);
      lavaGround.addEncounter(lavaSpiderMulti, 10);
      lavaGround.addEncounter(lavaCreatureSingle, 10);
      addTileType(lavaGround);
      EncounterType lavaRock = new EncounterType(Tiles.Tile.TILE_LAVA.id, (byte)-1);
      lavaRock.addEncounter(lavaCreatureSingle, 10);
      addTileType(lavaRock);
      logger.log(Level.INFO, "Created Encounters. It took " + (float)(System.nanoTime() - now) / 1000000.0F + " ms.");
   }
}

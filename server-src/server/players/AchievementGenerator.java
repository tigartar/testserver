package com.wurmonline.server.players;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AchievementGenerator implements MiscConstants, AchievementList {
   private static final String INSERT = DbConnector.isUseSqlite()
      ? "INSERT OR IGNORE INTO ACHIEVEMENTTEMPLATES (NUMBER,NAME,TRIGGERON,DESCRIPTION,CREATORNAME,ATYPE,PLAYUPDATE) VALUES (?,?,?,?,?,?,?)"
      : "INSERT IGNORE INTO ACHIEVEMENTTEMPLATES (NUMBER,NAME,TRIGGERON,DESCRIPTION,CREATORNAME,ATYPE,PLAYUPDATE) VALUES (?,?,?,?,?,?,?)";
   private static final String LOADALL = "SELECT * FROM ACHIEVEMENTTEMPLATES";
   private static final String DELETE = "DELETE FROM ACHIEVEMENTTEMPLATES WHERE NUMBER=?";
   private static final Logger logger = Logger.getLogger(AchievementGenerator.class.getName());
   private static final ConcurrentHashMap<Integer, AchievementTemplate> loadedTemplates = new ConcurrentHashMap<>();

   private AchievementGenerator() {
   }

   public static boolean isRerollablePersonalGoal(int achievementId) {
      switch(achievementId) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 45:
         case 46:
         case 266:
         case 267:
         case 275:
         case 279:
         case 280:
         case 281:
         case 282:
         case 283:
         case 284:
         case 285:
         case 286:
         case 287:
         case 288:
         case 294:
         case 320:
         case 323:
         case 324:
         case 331:
         case 332:
         case 333:
            return true;
         default:
            return false;
      }
   }

   public static void generateAchievements() throws IOException {
      ConcurrentHashMap<Integer, AchievementTemplate> generatedTemplates = new ConcurrentHashMap<>();
      loadAchievementTemplates();
      AchievementTemplate template = createAchievement(
         1,
         "Pillar Hugger",
         true,
         1,
         (byte)3,
         "You conquered a pillar in the hunt of the ancients.",
         false,
         generatedTemplates,
         true,
         false,
         "Conquer a pillar in the Hunt Of The Ancients"
      );
      template.setAchievementsTriggered(new int[]{2});
      template = createAchievement(
         2, "Conqueror", false, 50, (byte)4, "Those pillars aren't conquering themselves, you got that right.", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{3});
      template = createAchievement(
         3, "Hero Of The Hunt", false, 10, (byte)5, "Is there a time of day when you're not conquering pillars?", false, generatedTemplates
      );
      template = createAchievement(
         4, "Gladiator", false, 1, (byte)3, "You were brave enough to venture into a PVP zone.", false, generatedTemplates, false, true
      );
      template = createAchievement(5, "Invisible:Farwalker", true, 1, (byte)3, "", false, generatedTemplates, true, false);
      template.setAchievementsTriggered(new int[]{6});
      template = createAchievement(
         6, "Runner", false, 50, (byte)3, "In times of danger, you prefer to be unstoppable!", false, generatedTemplates, true, false
      );
      template.setAchievementsTriggered(new int[]{7});
      template = createAchievement(
         7, "Coward", false, 10, (byte)4, "It appears as if you prefer to be unstoppable at all times.", false, generatedTemplates, true, false
      );
      template = createAchievement(
         8, "Just Killed A Man", false, 1, (byte)3, "Now he's dead.", false, generatedTemplates, true, false, "Defeat another player in combat"
      );
      template.setAchievementsTriggered(new int[]{9});
      template = createAchievement(9, "Warrior", false, 50, (byte)4, "You're getting a reputation for being a successful fighter.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{10});
      template = createAchievement(
         10, "The Butcher Of Jackal", false, 10, (byte)5, "People aren't sure anymore if you're some sort of warrior god or not.", false, generatedTemplates
      );
      template = createAchievement(11, "Bow Kill", false, 1, (byte)3, "", false, generatedTemplates, "Defeat another player using a bow");
      template.setAchievementsTriggered(new int[]{12, 23});
      template = createAchievement(
         12, "True Aim", false, 20, (byte)3, "You've learned to put the pointy end of an arrow to good use.", false, generatedTemplates, true, false
      );
      template.setAchievementsTriggered(new int[]{13});
      template = createAchievement(13, "Robin Hood", false, 3, (byte)4, "Who's the real legend now?", false, generatedTemplates);
      template = createAchievement(14, "Sword Kill", false, 1, (byte)3, "", false, generatedTemplates, "Defeat another player using a sword");
      template.setAchievementsTriggered(new int[]{15, 23});
      template = createAchievement(15, "Fencer", false, 20, (byte)3, "Pointy end of the sword goes into man.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{16, 24});
      template = createAchievement(16, "Knight", false, 3, (byte)4, "Knights do use swords.", false, generatedTemplates);
      template = createAchievement(17, "Maul Kill", false, 1, (byte)3, "", false, generatedTemplates, "Defeat another player using a crush weapon");
      template.setAchievementsTriggered(new int[]{18, 23});
      template = createAchievement(18, "Crusher", false, 20, (byte)3, "Blunt weapons hurt more than you'd think.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{19, 24});
      template = createAchievement(
         19, "Bonkers", false, 3, (byte)4, "People think you're slightly mad, hitting everything you see with that weapon.", false, generatedTemplates
      );
      template = createAchievement(
         20, "Axe Kill", false, 1, (byte)3, "Count of players killed with any type of axe", false, generatedTemplates, "Defeat another player using an axe"
      );
      template.setAchievementsTriggered(new int[]{21, 23});
      template = createAchievement(21, "Headsman", false, 20, (byte)3, "You like to axe people.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{22, 24});
      template = createAchievement(22, "Enforcer", false, 3, (byte)4, "Do you have a fetish for heads?", false, generatedTemplates);
      template = createAchievement(23, "Versatile Warrior", false, 1, (byte)4, "You're someone to watch out for.", false, generatedTemplates, false, true);
      template.setRequiredAchievements(new int[]{11, 14, 17, 20});
      template = createAchievement(24, "Master Of Arms", false, 1, (byte)5, "People fear you for a reason", false, generatedTemplates, false, true);
      template.setRequiredAchievements(new int[]{16, 19, 22});
      template = createAchievement(
         25,
         "Backstabber",
         false,
         1,
         (byte)3,
         "What's with everyone turning towards you all the time?",
         false,
         generatedTemplates,
         "Defeat another player using a knife"
      );
      template.setAchievementsTriggered(new int[]{26});
      template = createAchievement(26, "Mad Butcher", false, 5, (byte)3, "Feeling a bit lonely lately?", false, generatedTemplates);
      template = createAchievement(
         27,
         "Barbarian",
         false,
         1,
         (byte)3,
         "You don't mind some crushed bones and gore",
         false,
         generatedTemplates,
         "Defeat another player using a huge club"
      );
      template.setAchievementsTriggered(new int[]{28});
      template = createAchievement(
         28, "Turning Green", false, 5, (byte)3, "You're still trying to get used abnormal amounts of crushed blood and gore", false, generatedTemplates
      );
      template = createAchievement(
         29,
         "Angry Sailor",
         false,
         1,
         (byte)3,
         "Beware the Angry Sailor and his Belaying Pin.",
         false,
         generatedTemplates,
         "Defeat another player using a belaying pin"
      );
      template = createAchievement(
         30,
         "Miner On Strike",
         false,
         1,
         (byte)3,
         "Better not to underestimate a miner and his pickaxe.",
         false,
         generatedTemplates,
         "Defeat another player using a pickaxe"
      );
      template = createAchievement(
         31, "In Cloth We Trust", false, 1, (byte)3, "People should be more careful with what they're wearing", false, generatedTemplates
      );
      template = createAchievement(32, "Red Dragon Madness", false, 1, (byte)3, "The most powerful of armours will keep you safe", false, generatedTemplates);
      template = createAchievement(
         33, "Caught With Your Pants Down", false, 1, (byte)3, "You forgot to wear your pants. What a funny sight.", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{34});
      template = createAchievement(
         34, "Pantless Hero", false, 5, (byte)3, "This is becoming quite the habit. And a scary one at that.", false, generatedTemplates, true, false
      );
      template = createAchievement(35, "Cyclops", false, 1, (byte)3, "Ouch! The eye!", false, generatedTemplates);
      template = createAchievement(36, "The Lucky One", false, 1, (byte)3, "Wow, that was really, really close.", false, generatedTemplates);
      template = createAchievement(37, "Finish Him!", false, 1, (byte)3, "No mercy was shown that day.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{38});
      template = createAchievement(38, "Merciless", false, 10, (byte)3, "You're good at this!", false, generatedTemplates);
      template = createAchievement(39, "Be Gentle Please", false, 1, (byte)3, "You won a spar, hooray!", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{40, 600});
      template = createAchievement(40, "Brother In Spars", false, 10, (byte)3, "You're quite popular to spar against.", false, generatedTemplates);
      template = createAchievement(41, "Invisible:ThrowAndHit", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{42});
      template = createAchievement(42, "Gunslinger-ish", false, 5, (byte)3, "You sure like throwing things at things.", false, generatedTemplates);
      template = createAchievement(
         43, "Defensive Offensive", false, 1, (byte)3, "You put up a defensive wall and let your opponent crush himself against it", false, generatedTemplates
      );
      template = createAchievement(44, "Traitor", false, 1, (byte)3, "And that poor guard was just doing his duty.", false, generatedTemplates);
      template = createAchievement(
         45, "Debt Collector", false, 1, (byte)3, "That's your money!", false, generatedTemplates, true, false, "Drain another settlement of its upkeep"
      );
      template.setAchievementsTriggered(new int[]{46});
      template = createAchievement(46, "Up The Drain", false, 10, (byte)3, "You've got expenses like everyone else!", false, generatedTemplates);
      template = createAchievement(47, "Death From Above", false, 1, (byte)3, "Oops, that must have hurt!", false, generatedTemplates, true, false);
      template = createAchievement(48, "Crushed", false, 1, (byte)3, "Ouch! What hit you?", false, generatedTemplates);
      template = createAchievement(49, "BONK!", false, 1, (byte)3, "You're lucky to be wearing that helm", false, generatedTemplates);
      template = createAchievement(
         50,
         "Tossed Dwarf",
         false,
         1,
         (byte)3,
         "Nobody dares to throw you around and you're hoping nobody watched you flying helplessly through the air this time.",
         false,
         generatedTemplates
      );
      template = createAchievement(
         51,
         "Demolition",
         false,
         1,
         (byte)3,
         "Why walk over to knock something down when you can do it from here by shooting?",
         false,
         generatedTemplates,
         "Destroy a structure by catapulting it"
      );
      template = createAchievement(
         52,
         "Epic Helper",
         false,
         1,
         (byte)3,
         "You did your part in helping the gods advance on their journey on Valrei.",
         false,
         generatedTemplates,
         true,
         false,
         "Help in an Epic mission"
      );
      template = createAchievement(
         53,
         "Epic Finalizer",
         false,
         1,
         (byte)3,
         "You managed to finish that mission. Good job!",
         false,
         generatedTemplates,
         "Be the person to complete an Epic mission"
      );
      template = createAchievement(54, "Cog Sailor", false, 1, (byte)3, "Look, it floats!", false, generatedTemplates, false, false, "Pilot a Cog");
      template.setAchievementsTriggered(new int[]{61});
      template = createAchievement(55, "Knarr Sailor", false, 1, (byte)3, "Look, it floats!", false, generatedTemplates, false, false, "Pilot a Knarr");
      template.setAchievementsTriggered(new int[]{61, 558});
      template = createAchievement(56, "Corbita Sailor", false, 1, (byte)3, "Look, it floats!", false, generatedTemplates, false, false, "Pilot a Corbita");
      template.setAchievementsTriggered(new int[]{61});
      template = createAchievement(57, "Rowboat Sailor", false, 1, (byte)3, "Look, it floats!", false, generatedTemplates, false, false, "Pilot a Rowboat");
      template.setAchievementsTriggered(new int[]{60, 61});
      template = createAchievement(58, "Sailboat Sailor", false, 1, (byte)3, "Look, it floats!", false, generatedTemplates, false, false, "Pilot a Sailboat");
      template.setAchievementsTriggered(new int[]{60, 61});
      template = createAchievement(59, "Caravel Sailor", false, 1, (byte)3, "Look, it floats!", false, generatedTemplates, false, false, "Pilot a Caravel");
      template.setAchievementsTriggered(new int[]{61});
      template = createAchievement(
         60, "Chief Mate", false, 1, (byte)3, "Yarrr matey.", false, generatedTemplates, false, true, "Pilot a Rowboat and a Sailboat"
      );
      template.setRequiredAchievements(new int[]{57, 58});
      template = createAchievement(61, "Cap'n", false, 1, (byte)3, "Aye aye, Cap'n", false, generatedTemplates, false, true, "Pilot all known ship types");
      template.setRequiredAchievements(new int[]{54, 55, 56, 57, 58, 59});
      template = createAchievement(62, "Invisible:Distancemoved", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{63, 64});
      template = createAchievement(
         63, "Wanderer", false, 2, (byte)3, "You know how to move around", false, generatedTemplates, true, true, "Travel 500 tiles in one session"
      );
      template = createAchievement(64, "Nomad", false, 64, (byte)4, "Are you restless?", false, generatedTemplates);
      template = createAchievement(65, "Invisible:DistanceDraggedCart", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{66});
      template = createAchievement(
         66, "Hauler", false, 10, (byte)3, "You've worked hard today", false, generatedTemplates, true, false, "Drag carts 250 tiles"
      );
      template.setAchievementsTriggered(new int[]{67});
      template = createAchievement(
         67, "Moved A Mountain", false, 5, (byte)3, "You know how to drag that cart", false, generatedTemplates, "Drag carts 1250 tiles"
      );
      template = createAchievement(68, "Invisible:Servercross", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{69});
      template = createAchievement(69, "Tradesman", false, 20, (byte)3, "You're seeing the world!", false, generatedTemplates, false, true);
      template = createAchievement(
         70, "Wet Feet", false, 1, (byte)3, "It's cold. It's wet. I don't like it.", false, generatedTemplates, false, true, "Try swimming a bit"
      );
      template = createAchievement(71, "Dead End", false, 1, (byte)3, "Sometimes it's hard to survive while driving", false, generatedTemplates, false, true);
      template = createAchievement(
         72, "Could Not Let Go", false, 1, (byte)3, "You can't run and drag at the same time!", false, generatedTemplates, false, true
      );
      template = createAchievement(
         73, "Trucker", false, 1, (byte)3, "You drove that cart really far", false, generatedTemplates, false, true, "Drive a cart 4000 tiles in one session"
      );
      template = createAchievement(
         74,
         "Joyrider",
         false,
         1,
         (byte)3,
         "You had the chance to view the surroundings quite a while there",
         false,
         generatedTemplates,
         false,
         true,
         "Ride someone elses cart 4000 tiles in one session"
      );
      template = createAchievement(
         75,
         "Rider Of The Apocalypse",
         false,
         1,
         (byte)3,
         "You rode, and rode, and rode.",
         false,
         generatedTemplates,
         false,
         true,
         "Ride 4000 tiles in one session"
      );
      template = createAchievement(
         76, "Sore Bottom", false, 1, (byte)3, "Ok that horse got me pretty far, now where's my farmer's salve.", false, generatedTemplates, false, true
      );
      template = createAchievement(
         77, "Out At Sea", false, 1, (byte)3, "Hope you're not out of your depth", false, generatedTemplates, false, true, "Get out into the deep sea"
      );
      template = createAchievement(
         78, "Went Up A Hill", false, 1, (byte)3, "The view is pretty up here", false, generatedTemplates, false, true, "Climb up a hill"
      );
      template = createAchievement(
         79, "Mountain Goat", false, 1, (byte)3, "Wow, you can really climb heights", false, generatedTemplates, false, true, "Climb up a mountain"
      );
      template = createAchievement(
         80, "Thin Air", false, 1, (byte)3, "How high can you reach?", false, generatedTemplates, false, true, "Climb up a high mountain"
      );
      template = createAchievement(
         81,
         "On The Way To The Moon",
         false,
         1,
         (byte)3,
         "There's no limit! No, no, no limit!",
         false,
         generatedTemplates,
         false,
         true,
         "Climb up a really really high mountain"
      );
      template = createAchievement(82, "Invisible:Invulnkick", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{83});
      template = createAchievement(83, "Slacker", false, 10, (byte)3, "You're just standing there!", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{84});
      template = createAchievement(84, "Sloth", false, 5, (byte)3, "Don't just log on. Play!", false, generatedTemplates, false, true);
      template = createAchievement(85, "Invisible:DrinkPotion", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{86});
      template = createAchievement(86, "Under The Influence", false, 5, (byte)3, "You like them potions", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{87});
      template = createAchievement(87, "Hexer", false, 4, (byte)3, "Are you addicted to potions?", false, generatedTemplates);
      template = createAchievement(88, "Ouch That Hurt", false, 1, (byte)3, "", false, generatedTemplates, "Take some falling damage");
      template.setAchievementsTriggered(new int[]{89});
      template = createAchievement(89, "Clumsy", false, 100, (byte)3, "Try to be more gentle when climbing", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{90});
      template = createAchievement(90, "Disoriented", false, 10, (byte)3, "Try not to hurt yourself that much", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{91});
      template = createAchievement(91, "Tumbleweed", false, 5, (byte)4, "Do you ever NOT take damage when climbing?", false, generatedTemplates);
      template = createAchievement(92, "FreeFall", false, 1, (byte)3, "Wheeee!", false, generatedTemplates);
      template = createAchievement(
         93, "Gollum", false, 1, (byte)3, "It came to me, my own, my love... my... preciousss.", false, generatedTemplates, false, true
      );
      template = createAchievement(94, "Invisible:EquipRing", true, 1, (byte)3, "", false, generatedTemplates, false, true);
      template.setAchievementsTriggered(new int[]{97});
      template = createAchievement(95, "Invisible:EquipNecklace", true, 1, (byte)3, "", false, generatedTemplates, false, true);
      template.setAchievementsTriggered(new int[]{97});
      template = createAchievement(96, "Invisible:EquipBracelet", true, 1, (byte)3, "", false, generatedTemplates, false, true);
      template.setAchievementsTriggered(new int[]{97});
      template = createAchievement(
         97,
         "You Beauty",
         false,
         1,
         (byte)3,
         "You like to dress up",
         false,
         generatedTemplates,
         false,
         true,
         "Wear a ring, a necklace and a bracelet at the same time"
      );
      template.setRequiredAchievements(new int[]{94, 95, 96});
      template = createAchievement(98, "Concrete Shoes", false, 1, (byte)3, "You've discovered that swimming requires training", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{99});
      template = createAchievement(99, "Fish Food", false, 5, (byte)3, "You don't like to train swimming", false, generatedTemplates, true, false);
      template = createAchievement(100, "Nice Try", false, 1, (byte)3, "It's occupied!", false, generatedTemplates, false, true);
      template = createAchievement(101, "Gravedigger", false, 1, (byte)3, "You put the dead to rest", false, generatedTemplates, "Bury a corpse");
      template.setAchievementsTriggered(new int[]{102});
      template = createAchievement(
         102, "Undertaker", false, 100, (byte)3, "You keep souls to rest and the world free from rotting corpses", false, generatedTemplates, true, false
      );
      template = createAchievement(103, "Invisible:MeditatingAction", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{104});
      template = createAchievement(
         104, "Peace Of Mind", false, 200, (byte)3, "You're getting in to not getting in to things", false, generatedTemplates, "Meditate 200 times"
      );
      template.setAchievementsTriggered(new int[]{105});
      template = createAchievement(
         105, "Bodhisattva", false, 10, (byte)4, "Meditating cleans the mind and elevates your soul", false, generatedTemplates, true, false
      );
      template = createAchievement(106, "Actions Performed", false, 1, (byte)3, "You did a thing! Now do it again.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{107});
      template = createAchievement(
         107,
         "Ambitious",
         false,
         100000,
         (byte)4,
         "You're not fooling around. You're doing things!",
         false,
         generatedTemplates,
         true,
         false,
         "Perform 100000 actions"
      );
      template = createAchievement(108, "Invisible:LockpickBoat", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{109});
      template = createAchievement(109, "Pirate", false, 5, (byte)3, "Those boats are made for picking", false, generatedTemplates, "Lockpick five boats");
      template.setAchievementsTriggered(new int[]{110});
      template = createAchievement(110, "Captain Morgan", false, 5, (byte)3, "You need more boats!", false, generatedTemplates);
      template = createAchievement(111, "Invisible:PickLock", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{112});
      template = createAchievement(112, "Burglar", false, 5, (byte)3, "They can't keep you out!", false, generatedTemplates, "Pick five locks");
      template.setAchievementsTriggered(new int[]{113});
      template = createAchievement(113, "Cleptomaniac", false, 10, (byte)3, "Just.. one more..", false, generatedTemplates);
      template = createAchievement(114, "Invisible:Stealth", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{115});
      template = createAchievement(
         115,
         "Sneaky",
         false,
         10,
         (byte)3,
         "Sometimes it is best not to be seen by others.",
         false,
         generatedTemplates,
         "Successfully stealth yourself ten times"
      );
      template.setAchievementsTriggered(new int[]{116});
      template = createAchievement(
         116,
         "Shadow",
         false,
         5,
         (byte)3,
         "Hiding in the shadows is becoming one of your favorite pastimes.",
         false,
         generatedTemplates,
         "Successfully stealth yourself fifty times"
      );
      template.setAchievementsTriggered(new int[]{117});
      template = createAchievement(
         117, "Ghost", false, 4, (byte)3, "Others only notice a slight movement of the air but choose to look the other way.", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{118});
      template = createAchievement(
         118,
         "Invisible",
         false,
         5,
         (byte)4,
         "You mastered the art of hiding. The light cannot manage to find you anymore.",
         false,
         generatedTemplates,
         true,
         false
      );
      template = createAchievement(119, "Invisible:Planting", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{537, 120});
      template = createAchievement(120, "Tree Hugger", false, 200, (byte)3, "You plant trees", false, generatedTemplates, "Plant 200 trees");
      template.setAchievementsTriggered(new int[]{121});
      template = createAchievement(
         121, "Fo's Favourite", false, 5, (byte)3, "You start to become popular with the gods of nature", false, generatedTemplates, "Plant 1000 trees"
      );
      template.setAchievementsTriggered(new int[]{122});
      template = createAchievement(
         122,
         "Johnny Appleseed",
         false,
         5,
         (byte)4,
         "You have to check your skin for bark complexion",
         false,
         generatedTemplates,
         true,
         false,
         "Plant 5000 trees"
      );
      template = createAchievement(123, "Invisible: PlantFlower", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{124});
      template = createAchievement(124, "Hippie", false, 100, (byte)3, "Flower Power!", false, generatedTemplates, "Plant 100 flowers");
      template.setAchievementsTriggered(new int[]{125});
      template = createAchievement(125, "Bumble Bee", false, 5, (byte)4, "Are you in it for the honey, honey?", false, generatedTemplates, "Plant 500 flowers");
      template = createAchievement(126, "Invisible:CatchFish", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{127});
      template = createAchievement(
         127, "Vynora Commands You", false, 200, (byte)3, "The fish bring secrets from the deep", false, generatedTemplates, "Catch 200 fish"
      );
      template.setAchievementsTriggered(new int[]{128});
      template = createAchievement(
         128, "The Path of Vynora", false, 5, (byte)4, "You and fishing are one", false, generatedTemplates, true, false, "Catch 1000 fish"
      );
      template = createAchievement(129, "Invisible:CutTree", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{513, 130});
      template = createAchievement(
         130, "Deforestation", false, 1000, (byte)3, "There's something with you and trees", false, generatedTemplates, true, false, "Cut down 1000 trees"
      );
      template.setAchievementsTriggered(new int[]{131});
      template = createAchievement(
         131, "Paul Bunyan", false, 5, (byte)3, "You can measure yourself with the greatest of lumberjacks", false, generatedTemplates
      );
      template = createAchievement(132, "Midas", false, 1, (byte)4, "You can shop", false, generatedTemplates);
      template = createAchievement(133, "Drunken Sailor", false, 1, (byte)3, "What shall we do with you?", false, generatedTemplates, false, true);
      template = createAchievement(
         134, "Irresponsible Driving", false, 1, (byte)3, "Haven't you heard? Don't drink and drive!", false, generatedTemplates, false, true
      );
      template = createAchievement(
         135,
         "Almost As Good As An Axe",
         false,
         1,
         (byte)3,
         "There's always a way to bring down a tree",
         false,
         generatedTemplates,
         "Cut down a tree using a shovel"
      );
      template = createAchievement(
         136, "Last Rope", false, 1, (byte)3, "The more the merrier", false, generatedTemplates, false, true, "Lead three creatures at the same time"
      );
      template = createAchievement(
         137, "Cowboy", false, 1, (byte)3, "Bovine is your thing", false, generatedTemplates, false, true, "Lead a few bovine while riding"
      );
      template = createAchievement(138, "Activist", false, 1, (byte)3, "Excellent way of proving your point!", false, generatedTemplates, false, true);
      template = createAchievement(139, "Invisible:PickMushroom", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{140});
      template = createAchievement(140, "Truffle Pig", false, 20, (byte)3, "You like them mushies", false, generatedTemplates, "Pick 20 mushrooms");
      template = createAchievement(
         141, "Fast Learner", false, 1, (byte)3, "Great! You've finished the tutorial!", false, generatedTemplates, false, true, "Finish the tutorial"
      );
      template = createAchievement(
         142, "Ashes to Ashes...", false, 1, (byte)3, "Most people avoid lava for good reasons.", false, generatedTemplates, true, false
      );
      template = createAchievement(
         143, "Overdosed On Acupuncture", false, 1, (byte)3, "Those thorns give deadly prickles", false, generatedTemplates, true, false
      );
      template = createAchievement(144, "Survivor", false, 1, (byte)3, "Hah! I don't mind a crash or two", false, generatedTemplates, false, false);
      template = createAchievement(
         145, "Sleeping Beauty", false, 1, (byte)3, "You should consider using that precious sleep bonus", false, generatedTemplates, false, true
      );
      template = createAchievement(
         146, "Singing While Eating", false, 1, (byte)3, "Who was that cook?", false, generatedTemplates, false, true, "Eat some really high quality food"
      );
      template = createAchievement(
         147, "Abstinence", false, 1, (byte)3, "You wouldn't mind something to eat. Really really really.", false, generatedTemplates, false, true
      );
      template = createAchievement(148, "Ascetic", false, 1, (byte)3, "You are dying from famine", false, generatedTemplates, false, true);
      template = createAchievement(
         149, "Obsessive Eater", false, 1, (byte)3, "Welp, don't have any space left now. *BURP*", false, generatedTemplates, false, true
      );
      template = createAchievement(150, "Popular Joe", false, 1, (byte)3, "You have many friends!", false, generatedTemplates, false, true);
      template = createAchievement(151, "Invisible:ShakerOrbing", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{152});
      template = createAchievement(152, "Shaking The Foundations Of The Earth", false, 5, (byte)3, "Shake them orbs!", false, generatedTemplates);
      template = createAchievement(153, "Invisible:UseResStone", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{154});
      template = createAchievement(
         154,
         "Better Safe Than Sorry",
         false,
         5,
         (byte)3,
         "Who doesn't want to bring his puppy to heaven? And lots of other things for that matter?",
         false,
         generatedTemplates
      );
      template = createAchievement(155, "Maintenance", true, 1, (byte)3, "Good job repairing those things", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{156});
      template = createAchievement(
         156, "Janitor", false, 50, (byte)3, "You keep the place in good order", false, generatedTemplates, true, false, "Repair 50 fences, floors or walls"
      );
      template.setAchievementsTriggered(new int[]{157});
      template = createAchievement(
         157, "Environment Improval Engineer", false, 5, (byte)3, "You take pride in repairing structures", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{158});
      template = createAchievement(
         158,
         "Decay Removal Services",
         false,
         5,
         (byte)4,
         "You could do this for a living. Unless you already are.",
         false,
         generatedTemplates,
         "Repair 1250 fences, floors or walls"
      );
      template.setAchievementsTriggered(new int[]{159});
      template = createAchievement(
         159, "Sisyphos Says Hello", false, 5, (byte)5, "Is this boring yet?", false, generatedTemplates, "Repair 6250 fences, floors or walls"
      );
      template = createAchievement(160, "Invisible:ItemInTrash", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{161});
      template = createAchievement(
         161, "Cleanup Operation", false, 100, (byte)3, "You are recycling things. Good.", false, generatedTemplates, "Put 100 items in the trash bin"
      );
      template.setAchievementsTriggered(new int[]{162});
      template = createAchievement(162, "Waste Press", false, 100, (byte)4, "Keep destroying items!", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{163});
      template = createAchievement(
         163,
         "Environmental Hero",
         false,
         1,
         (byte)5,
         "You're a hero! You keep the world alive!",
         false,
         generatedTemplates,
         "Put 10000 items in the trash bin"
      );
      template = createAchievement(164, "Where Is The Key?", false, 1, (byte)3, "Oops. Can you get it unlocked now?", false, generatedTemplates, false, true);
      template = createAchievement(165, "Deep Pockets", false, 1, (byte)3, "You can't possibly carry more", false, generatedTemplates, false, true);
      template = createAchievement(166, "Animal Care", false, 1, (byte)3, "How nice of you to feed the pet", false, generatedTemplates, false, true);
      template = createAchievement(167, "Invisible:BulkBinDeposit", true, 1, (byte)3, "", false, generatedTemplates);
      template = createAchievement(168, "Hoarder", false, 100000, (byte)3, "You like to save things. Obsessively.", false, generatedTemplates);
      template = createAchievement(169, "Leecher", false, 1, (byte)3, "You like to take things other people store.", false, generatedTemplates);
      template = createAchievement(170, "Founder", false, 1, (byte)3, "There's nothing like home.", false, generatedTemplates, "Found a settlement");
      template = createAchievement(
         171, "Citizen", false, 1, (byte)3, "Congratulations! You've joined your first settlement!", false, generatedTemplates, "Join a settlement"
      );
      template = createAchievement(172, "The First", false, 1, (byte)3, "You're all alone here!", false, generatedTemplates);
      template = createAchievement(173, "Just A Cold", false, 1, (byte)3, "Sniffle. You've got a cold.", false, generatedTemplates, "Catch a cold");
      template = createAchievement(174, "Horse Whisperer", false, 1, (byte)3, "You communicate with them.", false, generatedTemplates, false, true);
      template = createAchievement(175, "Invisible:HugEmote", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{176});
      template = createAchievement(176, "Free Hugs", false, 10, (byte)3, "You're the cuddly type", false, generatedTemplates, false, true);
      template = createAchievement(177, "Invisible:SmileEmote", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{178});
      template = createAchievement(178, "Friendly Person", false, 10, (byte)3, "You're the friendly type", false, generatedTemplates, false, true);
      template = createAchievement(179, "Invisible:FlirtEmote", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{180});
      template = createAchievement(
         180, "Flirtatious", false, 10, (byte)3, "Your smiles can melt ice", false, generatedTemplates, false, true, "Be overly forthcoming"
      );
      template = createAchievement(181, "Invisible:DanceEmote", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{182});
      template = createAchievement(182, "Nancy The Tavern Wench", false, 10, (byte)3, "Let's danec!", false, generatedTemplates, false, true);
      template = createAchievement(183, "wine", true, 1, (byte)3, "", true, generatedTemplates);
      template.setIsInLiters(true);
      template.setAchievementsTriggered(new int[]{184, 560});
      template = createAchievement(184, "Adulterator", false, 100, (byte)3, "You like wine", false, generatedTemplates, "Create 100 liters of wine");
      template.setAchievementsTriggered(new int[]{185});
      template = createAchievement(185, "Peasant Winemaker", false, 5, (byte)3, "You really like wine!", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{186});
      template = createAchievement(186, "Vigneron", false, 2, (byte)4, "Creating wine is your thing", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{187});
      template = createAchievement(
         187,
         "Master Winemaker",
         false,
         5,
         (byte)5,
         "Winemaking is an art, and you're an expert at it!",
         false,
         generatedTemplates,
         "Create 5000 liters of wine"
      );
      template = createAchievement(188, "tea", true, 1, (byte)3, "", true, generatedTemplates);
      template.setIsInLiters(true);
      template.setAchievementsTriggered(new int[]{189});
      template = createAchievement(189, "Enjoying A Nice Cuppa", false, 500, (byte)3, "Tea is cultivated", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{190});
      template = createAchievement(190, "Tea Taster", false, 5, (byte)3, "Which tea do you prefer, dear?", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{191});
      template = createAchievement(191, "Renowned Tea Taster", false, 4, (byte)4, "People want to come to your tea parties", false, generatedTemplates);
      template = createAchievement(192, "Bread Maker", false, 1, (byte)3, "You can bake!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{193});
      template = createAchievement(
         193,
         "The Smell Of Freshly Baked Bread",
         false,
         20,
         (byte)3,
         "If you have two loaves of bread, sell one and buy a lily.",
         false,
         generatedTemplates,
         "Bake 20 loaves of bread"
      );
      template.setAchievementsTriggered(new int[]{194});
      template = createAchievement(194, "Baker", false, 5, (byte)3, "You should wear a white cap.", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{195});
      template = createAchievement(
         195, "Dough Hands", false, 5, (byte)4, "Ignorance and error are necessary to life, like bread and water.", false, generatedTemplates
      );
      template = createAchievement(
         196,
         "Sandwich Maker",
         false,
         1,
         (byte)3,
         "As the Sandwich-Islander believes that the strength and valor of the enemy he kills passes into himself, so we gain the strength of the temptations we resist",
         true,
         generatedTemplates
      );
      template = createAchievement(
         197,
         "Pie-Maker",
         false,
         1,
         (byte)3,
         "When you die, if you get a choice between going to regular heaven or pie heaven, choose pie heaven. It might be a trick, but if it's not, mmmmmmmm, boy.",
         true,
         generatedTemplates,
         true,
         true,
         "Bake one of each pie!"
      );
      template.setRequiredAchievements(new int[]{394, 395, 396, 397, 398, 399, 461, 469, 470});
      template = createAchievement(198, "Invisible:Swords", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{202, 203});
      template = createAchievement(199, "Invisible:Mauls", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{202});
      template = createAchievement(200, "Invisible:Axes", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{202});
      template = createAchievement(201, "Invisible:Shields", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{202});
      template = createAchievement(
         202,
         "Lord Of War",
         false,
         100,
         (byte)3,
         "You made an art out of providing instruments of war for a large number of people.",
         false,
         generatedTemplates,
         false,
         true,
         "Smith 100 swords, 100 mauls, 100 axes and 100 shields"
      );
      template.setRequiredAchievements(new int[]{198, 199, 200, 201});
      template = createAchievement(
         203, "In The Name Of Magranon", false, 200, (byte)3, "Making those swords pleases certain deities", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{204});
      template = createAchievement(204, "The Path Of Magranon", false, 5, (byte)3, "Are you a follower of Magranon yet?", false, generatedTemplates);
      template = createAchievement(205, "Improve", false, 1, (byte)3, "There! That makes it better.", false, generatedTemplates);
      template = createAchievement(206, "Oops, That Went Wrong", false, 1, (byte)3, "Hope it didn't break.", false, generatedTemplates);
      template = createAchievement(
         207,
         "Well Maker",
         false,
         1,
         (byte)3,
         "You found water and brought it to the surface of the world.",
         false,
         generatedTemplates,
         "Build a well at a suitable spot"
      );
      template = createAchievement(
         208, "Tower Builder", false, 1, (byte)3, "You finished the guard tower. Will you finally be safe now?", false, generatedTemplates
      );
      template = createAchievement(209, "Cog Maker", false, 1, (byte)3, "You finished the cog", false, generatedTemplates, "Build a Cog");
      template.setAchievementsTriggered(new int[]{215});
      template = createAchievement(210, "Corbita Maker", false, 1, (byte)3, "You finished the corbita", false, generatedTemplates, "Build a Corbita");
      template.setAchievementsTriggered(new int[]{215});
      template = createAchievement(211, "Knarr Maker", false, 1, (byte)3, "You finished the knarr", false, generatedTemplates, "Build a Knarr");
      template.setAchievementsTriggered(new int[]{215});
      template = createAchievement(212, "Caravel Maker", false, 1, (byte)3, "You finished the caravel", false, generatedTemplates, "Build a Caravel");
      template.setAchievementsTriggered(new int[]{215});
      template = createAchievement(213, "Rowboat Maker", false, 1, (byte)3, "You finished the rowboat", false, generatedTemplates, "Build a Rowboat");
      template.setAchievementsTriggered(new int[]{215});
      template = createAchievement(214, "Sailboat Maker", false, 1, (byte)3, "You finished the sailboat", false, generatedTemplates, "Build a Sailboat");
      template.setAchievementsTriggered(new int[]{215});
      template = createAchievement(
         215,
         "Master Shipbuilder",
         false,
         1,
         (byte)4,
         "People should come to you for boats.",
         false,
         generatedTemplates,
         false,
         true,
         "Build one of each boat type"
      );
      template.setRequiredAchievements(new int[]{209, 210, 211, 212, 213, 214});
      template = createAchievement(216, "Settler", false, 1, (byte)3, "A house is like a heart just bigger.", false, generatedTemplates, "Build a house");
      template.setAchievementsTriggered(new int[]{519});
      template = createAchievement(
         217,
         "Good Work",
         false,
         1,
         (byte)3,
         "You excel in improving things",
         false,
         generatedTemplates,
         "Improve an item beyond your knowledge at quality level 30+"
      );
      template = createAchievement(
         218, "Bugged Rarity", false, 1, (byte)3, "You were the victim of a weird achievement bug but survived.", false, generatedTemplates
      );
      template = createAchievement(
         219,
         "Exceptional Craftsman",
         false,
         1,
         (byte)3,
         "You can do really fine things",
         false,
         generatedTemplates,
         "Improve an item beyond quality level 50"
      );
      template = createAchievement(
         220,
         "Pursuit Of Excellence",
         false,
         1,
         (byte)3,
         "You manage to improve yourself",
         false,
         generatedTemplates,
         "Improve an item beyond quality level 70"
      );
      template = createAchievement(
         221,
         "On The Path To Perfection",
         false,
         1,
         (byte)4,
         "You are soon at the highest levels of creation",
         false,
         generatedTemplates,
         "Improve an item beyond quality level 90"
      );
      template = createAchievement(
         222,
         "Perfection",
         false,
         1,
         (byte)5,
         "Few have mastered item creation like you. There's little room for improving an item more than this.",
         false,
         generatedTemplates,
         "Improve an item beyond quality level 99"
      );
      template = createAchievement(223, "Hens Killed", false, 1, (byte)3, "You've killed hens", false, generatedTemplates, "Scare a wild hen to death");
      template.setAchievementsTriggered(new int[]{224, 252});
      template = createAchievement(
         224,
         "Out, Out, Brief Candle!",
         false,
         10,
         (byte)3,
         "It appears scaring hens to death is your favourite pasttime.",
         false,
         generatedTemplates,
         "Scare ten wild hens to death"
      );
      template = createAchievement(225, "Crocodiles Killed", false, 1, (byte)3, "You need them shoes, belts and bags", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{226, 252, 376});
      template = createAchievement(
         226, "Tastes Like Chicken", false, 20, (byte)3, "You know how to get crocodile meat", false, generatedTemplates, "Kill 20 Crocodile"
      );
      template.setAchievementsTriggered(new int[]{227});
      template = createAchievement(227, "Crocodile Dundee", false, 10, (byte)4, "It was the croc and not you this time", false, generatedTemplates);
      template = createAchievement(228, "Unicorns Killed", false, 1, (byte)3, "You've slain the Unicorn", false, generatedTemplates, "Kill a wild Unicorn");
      template.setAchievementsTriggered(new int[]{229, 252, 376});
      template = createAchievement(
         229, "Tears Of The Unicorn", false, 100, (byte)3, "You're a slayer of beauty", false, generatedTemplates, "Kill 100 wild Unicorns"
      );
      template.setAchievementsTriggered(new int[]{230});
      template = createAchievement(230, "Destroyer Of Innocence", false, 10, (byte)4, "The Unicorns stand no chance", false, generatedTemplates);
      template = createAchievement(231, "Spiders Killed", false, 1, (byte)3, "You've killed a spider", false, generatedTemplates, "Kill a Huge Spider");
      template.setAchievementsTriggered(new int[]{232, 252, 376});
      template = createAchievement(232, "Arachnophile", false, 100, (byte)3, "You hate spiders", false, generatedTemplates, "Kill 100 spiders");
      template.setAchievementsTriggered(new int[]{233});
      template = createAchievement(233, "Honey, Guess What's For Dinner!", false, 10, (byte)4, "Too many spiders!", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{234});
      template = createAchievement(
         234, "My Furry Eight-legged Friends", false, 2, (byte)5, "What's up with these spiders everywhere?", false, generatedTemplates
      );
      template = createAchievement(235, "Trolls Killed", false, 1, (byte)3, "You've killed the fearsome Troll", false, generatedTemplates, "Kill a Troll");
      template.setAchievementsTriggered(new int[]{236, 252, 376, 551});
      template = createAchievement(236, "No Son Of A Troll", false, 1000, (byte)4, "You don't fancy trolls", false, generatedTemplates);
      template = createAchievement(237, "Wolves Killed", false, 1, (byte)3, "You're a wolf slayer", false, generatedTemplates, "Kill a wild wolf");
      template.setAchievementsTriggered(new int[]{238, 252, 376});
      template = createAchievement(238, "Dances With Wolves", false, 100, (byte)3, "You know how to kill wolves now", false, generatedTemplates);
      template = createAchievement(239, "Deer Killed", false, 1, (byte)3, "You've killed the dear deer", false, generatedTemplates, "Kill a wild deer");
      template.setAchievementsTriggered(new int[]{240, 252, 376});
      template = createAchievement(240, "Hunter Apprentice", false, 20, (byte)3, "Deer are easy to hunt", false, generatedTemplates, "Kill 20 wild deer");
      template.setAchievementsTriggered(new int[]{241});
      template = createAchievement(241, "Hunter", false, 50, (byte)4, "There are too many deer around", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{242});
      template = createAchievement(242, "Deer Nemesis", false, 1, (byte)5, "There used to be too many deer around", false, generatedTemplates);
      template = createAchievement(
         243, "Lava Spiders Killed", false, 1, (byte)3, "You've killed the Lava Spider", false, generatedTemplates, "Kill a lava spider"
      );
      template.setAchievementsTriggered(new int[]{247, 252, 376});
      template = createAchievement(
         244, "Lava Fiends Killed", false, 1, (byte)3, "You've killed the Lava Fiend", false, generatedTemplates, "Kill a lava fiend"
      );
      template.setAchievementsTriggered(new int[]{247, 245, 252, 376});
      template = createAchievement(
         245, "You Cannot Pass", false, 20, (byte)3, "You've defeated many Lava Fiends", false, generatedTemplates, "Kill 20 lava fiends"
      );
      template.setAchievementsTriggered(new int[]{246});
      template = createAchievement(
         246, "No Fuel For The Flame Of Udun", false, 5, (byte)4, "You've defeated a lot of Lava Fiends", false, generatedTemplates, "Kill 100 lava fiends"
      );
      template = createAchievement(247, "Fire Extinguisher", false, 5, (byte)4, "You're the nemesis of Lava creatures", false, generatedTemplates);
      template = createAchievement(248, "Zombies Killed", false, 1, (byte)3, "Zombie! Zombie!", false, generatedTemplates, "Kill a zombie");
      template.setAchievementsTriggered(new int[]{249, 252});
      template = createAchievement(249, "Braaains", false, 10, (byte)3, "Zombies stand no chance", false, generatedTemplates, "Kill 10 zombies");
      template.setAchievementsTriggered(new int[]{250});
      template = createAchievement(250, "Zombie Hunter", false, 5, (byte)4, "Apparently it's you or the zombies", false, generatedTemplates, "Kill 50 zombies");
      template = createAchievement(
         251, "Over the Rainbow", false, 1, (byte)5, "You've killed one of each type of creature", false, generatedTemplates, false, true
      );
      template.setRequiredAchievements(new int[]{252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264});
      template = createAchievement(
         252, "Jagermeister", false, 1, (byte)5, "You've killed many different sorts of creatures", false, generatedTemplates, false, true
      );
      template.setRequiredAchievements(new int[]{223, 225, 228, 231, 235, 237, 239, 243, 244, 248, 265, 266, 268, 269, 273, 289});
      template = createAchievement(
         253, "Slayer of Fiercity", false, 1, (byte)3, "You've killed a fierce creature", false, generatedTemplates, "Kill a wild fierce creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         254, "Becalmer", false, 1, (byte)3, "You've killed an angry creature", false, generatedTemplates, "Kill a wild angry creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         255, "Silencer", false, 1, (byte)3, "You've killed a raging creature", false, generatedTemplates, "Kill a wild raging creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         256, "Slayer Of Procastination", false, 1, (byte)3, "You've killed a slow creature", false, generatedTemplates, "Kill a wild slow creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         257, "Speedfighter", false, 1, (byte)3, "You've killed an alert creature", false, generatedTemplates, "Kill a wild alert creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         258, "Soilent Green", false, 1, (byte)3, "You've killed a greenish creature", false, generatedTemplates, "Kill a wild greenish creature"
      );
      template.setAchievementsTriggered(new int[]{251, 566});
      template = createAchievement(
         259, "Slayer Of The Lurker", false, 1, (byte)3, "You've killed a lurking creature", false, generatedTemplates, "Kill a wild lurking creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         260, "Slayer Of The Sly", false, 1, (byte)3, "You've killed a sly creature", false, generatedTemplates, "Kill a wild sly creature"
      );
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(261, "Willbreaker", false, 1, (byte)3, "You've killed a hardened creature", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(262, "Slayer Of The Meek", false, 1, (byte)3, "You've killed a scared creature", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(263, "Mercykiller", false, 1, (byte)3, "You've killed a diseased creature", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{251});
      template = createAchievement(
         264, "Slayer Of The Champ", false, 1, (byte)3, "You've killed a champion creature", false, generatedTemplates, "Kill a wild champion creature"
      );
      template.setAchievementsTriggered(new int[]{251, 577});
      template = createAchievement(265, "Pheasant Hunt", false, 1, (byte)3, "You've killed the pheasant", false, generatedTemplates, "Kill a wild pheasant");
      template.setAchievementsTriggered(new int[]{252, 376});
      template = createAchievement(
         266, "Kingdom Assault", false, 1, (byte)3, "You've killed an enemy kingdom guard", false, generatedTemplates, "Kill an enemy kingdom guard"
      );
      template.setAchievementsTriggered(new int[]{252, 376});
      template = createAchievement(
         267, "Settlement Assault", false, 1, (byte)3, "You've killed an enemy spirit guard", false, generatedTemplates, "Kill an enemy spirit guard"
      );
      template.setAchievementsTriggered(new int[]{294, 376});
      template = createAchievement(268, "Debugger", false, 1, (byte)3, "You've killed the bug. Thank you.", false, generatedTemplates, "Kill a wild cave bug");
      template.setAchievementsTriggered(new int[]{252, 376});
      template = createAchievement(269, "Bear Hunt", false, 1, (byte)3, "You've killed the bear", false, generatedTemplates, "Kill a wild bear");
      template.setAchievementsTriggered(new int[]{252, 376});
      template = createAchievement(270, "Dragonslayer", false, 1, (byte)4, "You've killed the Red Dragon", false, generatedTemplates, "Slay the Red Dragon");
      template = createAchievement(271, "Drakeslayer", false, 1, (byte)4, "You've killed a Drake", false, generatedTemplates, "Slay a wild drake");
      template = createAchievement(
         272, "Forest Giant Slayer", false, 1, (byte)4, "You've killed the Forest Giant", false, generatedTemplates, "Slay the Forest Giant"
      );
      template = createAchievement(273, "Goblin Slayer", false, 1, (byte)3, "You've killed the goblin", false, generatedTemplates, "Slay a goblin");
      template.setAchievementsTriggered(new int[]{252, 376});
      template = createAchievement(
         274, "Troll King Assassination", false, 1, (byte)4, "You've killed the Troll King", false, generatedTemplates, "Slay the Troll King"
      );
      template = createAchievement(275, "Kingdom Infiltration", false, 1, (byte)3, "You've killed an enemy kingdom creature", false, generatedTemplates);
      template = createAchievement(276, "Avenger Of Avenger", false, 1, (byte)3, "You've killed the Avenger of Light", false, generatedTemplates);
      template = createAchievement(
         277, "Sea Mastership", false, 1, (byte)4, "You've killed the Sea Serpent", false, generatedTemplates, "Slay the Sea Serpent"
      );
      template = createAchievement(278, "Moby Dick", false, 1, (byte)3, "You've killed the Huge Shark", false, generatedTemplates, "Slay a huge shark");
      template = createAchievement(279, "Demons", false, 1, (byte)3, "You've killed the demon", false, generatedTemplates, "Slay a wild demon");
      template = createAchievement(
         280, "Death Comes Crawling", false, 1, (byte)3, "You've killed the Deathcrawler", false, generatedTemplates, "Slay a wild deathcrawler"
      );
      template = createAchievement(
         281, "Uttacha Spawn Slayer", false, 1, (byte)3, "You've killed the Uttacha Spawn", false, generatedTemplates, "Slay a wild spawn of uttacha"
      );
      template = createAchievement(
         282, "How Many Sons?", false, 1, (byte)3, "You've killed the Son of Nogump", false, generatedTemplates, "Slay a wild son of nogump"
      );
      template = createAchievement(
         283, "Drake Spirits", false, 1, (byte)3, "You've killed the Drake Spirit", false, generatedTemplates, "Slay a wild drake spirit"
      );
      template.setAchievementsTriggered(new int[]{294, 376});
      template = createAchievement(
         284, "Eagle Spirits", false, 1, (byte)3, "You've killed the Eagle Spirit", false, generatedTemplates, "Slay a wild eagle spirit"
      );
      template.setAchievementsTriggered(new int[]{294, 376});
      template = createAchievement(285, "Epiphany Gone Dark", false, 1, (byte)4, "You've vanquished the Epiphany of Vynora", false, generatedTemplates);
      template = createAchievement(
         286, "Juggernaut's Demise", false, 1, (byte)4, "You've vanquished the Juggernaut", false, generatedTemplates, "Slay a Juggernaut of Magranon"
      );
      template = createAchievement(
         287, "Manifested No More", false, 1, (byte)4, "You've vanquished the Manifestation of Fo", false, generatedTemplates, "Slay a Manifestation of Fo"
      );
      template = createAchievement(
         288,
         "Incarnated To Hell",
         false,
         1,
         (byte)4,
         "You've vanquished the Incarnation of Libila",
         false,
         generatedTemplates,
         "Slay an Incarnation of Libila"
      );
      template = createAchievement(289, "Bisons", false, 1, (byte)3, "You've killed the bison", false, generatedTemplates, "Slay a wild bison");
      template.setAchievementsTriggered(new int[]{252, 376});
      template = createAchievement(
         290, "To Hell With It", false, 1, (byte)3, "You've killed the Hell Hound", false, generatedTemplates, "Slay a wild hell hound"
      );
      template.setAchievementsTriggered(new int[]{293, 376});
      template = createAchievement(
         291, "Hellova Fight", false, 1, (byte)3, "You've killed the Hell Horse", false, generatedTemplates, "Slay a wild hell horse"
      );
      template.setAchievementsTriggered(new int[]{293, 376});
      template = createAchievement(
         292, "Hello Goodbye", false, 1, (byte)3, "You've killed the Hell Scorpius", false, generatedTemplates, "Slay a wild hell scorpious"
      );
      template.setAchievementsTriggered(new int[]{293, 376});
      template = createAchievement(
         293,
         "All Hell",
         false,
         1,
         (byte)4,
         "You've held your ground against the Hell creatures",
         false,
         generatedTemplates,
         false,
         true,
         "Slay one of each wild hell creature"
      );
      template.setRequiredAchievements(new int[]{290, 291, 292});
      template = createAchievement(
         294, "High Spirits", false, 1, (byte)4, "You've put certain Spirits in place", false, generatedTemplates, false, true, "Slay spirit creatures"
      );
      template.setRequiredAchievements(new int[]{267, 283, 284});
      template = createAchievement(295, "Alcoholist", false, 1, (byte)3, "You've had problems with drinking", false, generatedTemplates);
      template = createAchievement(296, "Party King", false, 1, (byte)3, "You've reached the highest levels", false, generatedTemplates);
      template = createAchievement(297, "Giant Fish", false, 1, (byte)4, "You caught the rare one", false, generatedTemplates, "Catch a 175 kg fish");
      template = createAchievement(
         298, "Exquisite Gem", false, 1, (byte)4, "You've found a very special gem", false, generatedTemplates, "Mine a Star Gem of any type."
      );
      template = createAchievement(299, "Brilliant!", false, 1, (byte)4, "You've mined a brilliant gem", false, generatedTemplates, "Mine a 90+ ql gemstone");
      template = createAchievement(
         300, "Almost Impossible", false, 1, (byte)5, "You've manufactured something really special", false, generatedTemplates, "Create a fantastic item"
      );
      template = createAchievement(
         301, "Really Rare", false, 1, (byte)3, "You've manufactured a really rare item", false, generatedTemplates, "Create a rare item"
      );
      template = createAchievement(
         302, "Supreme Being", false, 1, (byte)4, "You've created a supreme item", false, generatedTemplates, "Create a supreme item"
      );
      template = createAchievement(303, "Conquerer", false, 1, (byte)3, "You've conquered land!", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{304});
      template = createAchievement(304, "Genghis Khan", false, 1000000, (byte)4, "You have conquered enough for a lifetime", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{305});
      template = createAchievement(
         305, "Master Of The world", false, 100, (byte)5, "You are one of the greatest conquerers ever. Your people owes you.", false, generatedTemplates
      );
      template = createAchievement(
         306,
         "Forged Rarity",
         false,
         1,
         (byte)3,
         "You may have crafted something which is better than everything there ever was before but whether this is true is shrouded by history.",
         false,
         generatedTemplates
      );
      template = createAchievement(307, "One Eyed Snake", false, 1, (byte)4, "You've killed the Kyklops", false, generatedTemplates, "Kill the Kyklops");
      template.setAchievementsTriggered(new int[]{376});
      template = createAchievement(
         308, "Goblin Leadership", false, 1, (byte)4, "You slayed the Goblin Leader", false, generatedTemplates, "Slay the Goblin Leader"
      );
      template.setAchievementsTriggered(new int[]{376});
      template = createAchievement(309, "Off Horse", false, 1, (byte)3, "You slay horses", false, generatedTemplates);
      template = createAchievement(310, "Bovine Master", false, 1, (byte)3, "You slay bovine like none other", false, generatedTemplates);
      template = createAchievement(311, "Kill the Pig", false, 1, (byte)3, "You require pork", false, generatedTemplates, "Kill a wild pig");
      template = createAchievement(312, "Dog Life", false, 1, (byte)3, "You put dogs out of their misery", false, generatedTemplates);
      template = createAchievement(313, "Scrapion", false, 1, (byte)3, "Scorpions are at your mercy", false, generatedTemplates, "Kill a wild scorpion");
      template.setAchievementsTriggered(new int[]{376});
      template = createAchievement(314, "Meoww!", false, 1, (byte)3, "Wild Cats try to put up a fight", false, generatedTemplates);
      template = createAchievement(
         315, "Rrreoww!", false, 1, (byte)3, "Mountain Lions try to put up a fight", false, generatedTemplates, "Kill a wild mountain lion"
      );
      template.setAchievementsTriggered(new int[]{376});
      template = createAchievement(316, "Rat Race", false, 1, (byte)3, "The rat race has begun!", false, generatedTemplates, "Kill a wild rat");
      template.setAchievementsTriggered(new int[]{376});
      template = createAchievement(
         317, "Rarity", false, 1, (byte)3, "You crafted something which is better than everything there ever was before.", false, generatedTemplates
      );
      template = createAchievement(318, "Invisible:Hedges", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{319});
      template = createAchievement(
         319, "Hedgehog", false, 200, (byte)3, "You're a one for hedges", false, generatedTemplates, "Plant 200 hedges or flower beds"
      );
      template = createAchievement(
         320, "Waller", false, 1, (byte)3, "You weaved the source and created a wall", false, generatedTemplates, "Summon a magical wall"
      );
      template = createAchievement(321, "Ruler", false, 1, (byte)5, "You made it to the top", false, generatedTemplates, "Become the ruler of a kingdom");
      template = createAchievement(322, "Ascended", false, 1, (byte)5, "You are now immortal", false, generatedTemplates, "Ascend to demigod");
      template = createAchievement(
         323, "King's Court", false, 1, (byte)4, "You have been allowed to join the inner ranks", false, generatedTemplates, "Join a kings court"
      );
      template = createAchievement(
         324, "Fine Titles", false, 1, (byte)4, "The rulers have bestowed titles upon you", false, generatedTemplates, "Receive a title from a King"
      );
      template = createAchievement(325, "Order Of The", false, 1, (byte)4, "You have been let into the secret orders", false, generatedTemplates);
      template = createAchievement(
         326, "Won The Game", false, 1, (byte)5, "You achieved all your personal goals in these lands and may feel closure", false, generatedTemplates
      );
      template = createAchievement(
         327, "Arch Mage", false, 1, (byte)5, "You have become a very powerful mystic", false, generatedTemplates, "Become an Arch Mage"
      );
      template = createAchievement(
         328, "Planeswalker", false, 1, (byte)5, "You have become almost supernatural", false, generatedTemplates, "Become a Planeswalker"
      );
      template = createAchievement(329, "Shadowmage", false, 1, (byte)5, "You have become a powerful mystic", false, generatedTemplates, "Become a Shadowmage");
      template = createAchievement(330, "Magician", false, 1, (byte)5, "You have become a powerful mystic", false, generatedTemplates, "Become a Magician");
      template = createAchievement(331, "Diabolist", false, 1, (byte)4, "You have become a dark mystic", false, generatedTemplates, "Become a Diabolist");
      template = createAchievement(332, "Magus", false, 1, (byte)4, "You have become a red mystic", false, generatedTemplates, "Become a Magus");
      template = createAchievement(333, "Mage", false, 1, (byte)4, "You have become a renowned mystic", false, generatedTemplates, "Become a Mage");
      template = createAchievement(334, "Fantastic!", false, 1, (byte)5, "You've mined a fantastic gem", false, generatedTemplates);
      template = createAchievement(335, "Invisible:UndeadKills", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{336});
      template = createAchievement(336, "Fleshh!", false, 1, (byte)3, "Blood.. fleshh..", false, generatedTemplates, false, false);
      template.setAchievementsTriggered(new int[]{337});
      template = createAchievement(337, "Hungryy", false, 20, (byte)3, "Uuuurrhhh", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{338});
      template = createAchievement(338, "Mussst Kill", false, 50, (byte)4, "Not ssstop noow", false, generatedTemplates, "Slay creatures");
      template = createAchievement(339, "Invisible:UndeadPKills", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{340});
      template = createAchievement(340, "Humanss!", false, 1, (byte)3, "Bbessst flessh", false, generatedTemplates, false, false, "Slay Humans");
      template.setAchievementsTriggered(new int[]{341});
      template = createAchievement(341, "Rrrrr", false, 10, (byte)4, "Rrrrhhh", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{342});
      template = createAchievement(342, "ARRRRR", false, 20, (byte)5, "Ghhhrrraaaaahhhhh", false, generatedTemplates);
      template = createAchievement(343, "Landed", false, 1, (byte)3, "You have been premium for 1 month", false, generatedTemplates);
      template = createAchievement(
         344, "Survived", false, 1, (byte)3, "You have been premium for 3 months", false, generatedTemplates, "Play as a premium character for 3 months"
      );
      template = createAchievement(345, "Scouted", false, 1, (byte)3, "You have been premium for 6 months", false, generatedTemplates);
      template = createAchievement(346, "Experienced", false, 1, (byte)3, "You have been premium for 9 months", false, generatedTemplates);
      template = createAchievement(347, "Owning", false, 1, (byte)4, "You have been premium for 12 months", false, generatedTemplates);
      template = createAchievement(348, "Shined", false, 1, (byte)4, "You have been premium for 16 months", false, generatedTemplates);
      template = createAchievement(349, "Glittered", false, 1, (byte)4, "You have been premium for 20 months", false, generatedTemplates);
      template = createAchievement(350, "Highly Illuminated", false, 1, (byte)4, "You have been premium for 26 months", false, generatedTemplates);
      template = createAchievement(351, "Foundation Pillar", false, 1, (byte)5, "You have been premium for 36 months", false, generatedTemplates);
      template = createAchievement(352, "Revered One", false, 1, (byte)5, "You have been premium for 48 months", false, generatedTemplates);
      template = createAchievement(353, "Patron Of The Net", false, 1, (byte)5, "You have been premium for 60 months", false, generatedTemplates);
      template = createAchievement(354, "Myth Or Legend?", false, 1, (byte)5, "You have been premium for 80 months", false, generatedTemplates);
      template = createAchievement(355, "Atlas Reincarnated", false, 1, (byte)5, "You have been premium for 120 months", false, generatedTemplates);
      template = createAchievement(356, "Defiler", false, 1, (byte)5, "You have destroyed the holy altar", false, generatedTemplates);
      template = createAchievement(357, "Righteous", false, 1, (byte)5, "You have destroyed the unholy altar", false, generatedTemplates);
      template = createAchievement(358, "Rope Bridge Maker", false, 1, (byte)3, "You finished a Rope Bridge", false, generatedTemplates, "Build a Rope Bridge");
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(359, "Wood Bridge Maker", false, 1, (byte)3, "You finished a Wood Bridge", false, generatedTemplates, "Build a Wood Bridge");
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         360, "Stone Bridge Maker", false, 1, (byte)3, "You finished a Stone Bridge", false, generatedTemplates, "Build a Stone Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         361, "Marble Bridge Maker", false, 1, (byte)3, "You finished a Marble Bridge", false, generatedTemplates, "Build a Marble Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         362, "Master Bridgebuilder", false, 1, (byte)4, "People should come to you for bridges.", false, generatedTemplates, "Build one of each bridge type"
      );
      template.setRequiredAchievements(new int[]{358, 359, 360, 361});
      template = createAchievement(
         363, "Exquisite Gem", false, 1, (byte)4, "You've found a very special gem", false, generatedTemplates, "Dug up an exquisite gemstone"
      );
      template = createAchievement(
         364, "Brilliant!", false, 1, (byte)4, "You've dug up a brilliant gem", false, generatedTemplates, "Dug up a 90+ ql gemstone"
      );
      template = createAchievement(365, "Fantastic!", false, 1, (byte)5, "You've dug up a fantastic gem", false, generatedTemplates);
      template = createAchievement(366, "Archaeologist", false, 1, (byte)4, "You've dug up some bones", false, generatedTemplates);
      template = createAchievement(367, "Treasure Hunter", false, 1, (byte)3, "You've found a treasure chest", false, generatedTemplates);
      template = createAchievement(368, "Spearhead", false, 1, (byte)4, "You've conquered strategic points", false, generatedTemplates);
      template = createAchievement(369, "Cavalier", false, 1, (byte)3, "You're taking things seriously", false, generatedTemplates);
      template.setRequiredAchievements(new int[]{374, 375, 376});
      template = createAchievement(370, "The Tortoise", false, 1, (byte)3, "You turned that sweet poor thing into a shield!", false, generatedTemplates);
      template = createAchievement(371, "Skilled", false, 1, (byte)3, "Skill points. Can't have too many of them", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{375});
      template = createAchievement(372, "Gold Digger", false, 1, (byte)3, "Found the Gold. Now - what to do with it?", false, generatedTemplates);
      template = createAchievement(
         373, "Fallen Cavalier", false, 1, (byte)3, "You've lost your Cavalier status. Luckily there are ways back.", false, generatedTemplates
      );
      template = createAchievement(
         374, "Enemy Lands", false, 1, (byte)3, "You entered enemy lands. You are on the path to Cavalier!", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{369});
      template = createAchievement(
         375,
         "Cavalier Skills",
         false,
         Servers.localServer.testServer ? 3 : 80,
         (byte)3,
         "After 80 new skill points you may become a Cavalier!",
         false,
         generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{369});
      template = createAchievement(376, "Invisible:Cavalier Kills", true, 1, (byte)3, "", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{377});
      template = createAchievement(
         377, "Cavalier Kills", false, 10, (byte)3, "After 10 serious creature kills you may become a Cavalier!", false, generatedTemplates
      );
      template.setAchievementsTriggered(new int[]{369});
      template = createAchievement(378, "Rift Jackals", false, 1, (byte)3, "You've vanquished a Rift Jackal", false, generatedTemplates, "Slay a Rift Jackal");
      template.setAchievementsTriggered(new int[]{382, 383, 588});
      template = createAchievement(379, "Rift Beasts", false, 1, (byte)3, "You've vanquished a Rift Beast", false, generatedTemplates, "Slay a Rift Beast");
      template.setAchievementsTriggered(new int[]{382, 384, 588});
      template = createAchievement(380, "Rift Ogre", false, 1, (byte)3, "You've vanquished a Rift Ogre", false, generatedTemplates, "Slay a Rift Ogre");
      template.setAchievementsTriggered(new int[]{382, 385, 588});
      template = createAchievement(
         381, "Rift Warmaster", false, 1, (byte)3, "You've vanquished a Rift Warmaster", false, generatedTemplates, "Slay a Rift Warmaster"
      );
      template.setAchievementsTriggered(new int[]{382, 386, 588});
      template = createAchievement(
         382,
         "Rift Specialist",
         false,
         1,
         (byte)4,
         "You've defeated all sorts of Rift Creatures",
         false,
         generatedTemplates,
         "Slay one of each type of Rift Creature"
      );
      template.setRequiredAchievements(new int[]{378, 379, 380, 381});
      template = createAchievement(
         383, "Jackal Hunter", false, 100, (byte)4, "You've vanquished a lot of Rift Jackals", false, generatedTemplates, "Slay many Rift Jackals"
      );
      template.setAchievementsTriggered(new int[]{387});
      template = createAchievement(
         384, "Rift Beast Nemesis", false, 100, (byte)4, "You've vanquished a lot of Rift Beasts", false, generatedTemplates, "Slay many Rift Beasts"
      );
      template.setAchievementsTriggered(new int[]{387});
      template = createAchievement(
         385, "Rift Ogre Hero", false, 100, (byte)4, "You've vanquished a lot of Rift Ogres", false, generatedTemplates, "Slay many Rift Ogres"
      );
      template.setAchievementsTriggered(new int[]{387});
      template = createAchievement(
         386, "Ghost Of The Rift Warmasters", false, 40, (byte)4, "You're scaring the Rift Warmasters", false, generatedTemplates, "Slay many Rift Warmasters"
      );
      template.setAchievementsTriggered(new int[]{387});
      template = createAchievement(
         387,
         "Own The Rift",
         false,
         1,
         (byte)5,
         "You've defeated an insane amount of Rift Creatures",
         false,
         generatedTemplates,
         "Lose count of Rift Creature defeated"
      );
      template.setRequiredAchievements(new int[]{383, 384, 385, 386});
      template = createAchievement(
         388, "Rift Opener", false, 1, (byte)3, "You have sacrificed at the Rift Altar", false, generatedTemplates, "Sacrifice at the Rift Altar"
      );
      template.setAchievementsTriggered(new int[]{389});
      template = createAchievement(
         389,
         "Rift Surfer",
         false,
         1000,
         (byte)5,
         "Fearlessly, you stand in the way of Rift Waves",
         false,
         generatedTemplates,
         "Sacrifice many many times at the Rift Altar"
      );
      template = createAchievement(390, "Investigating The Rift", false, 1, (byte)3, "What's up there?", false, generatedTemplates, "Enter the Rift Area");
      template = createAchievement(391, "Die By The Rift", false, 1, (byte)3, "You died in the Rift Area", false, generatedTemplates, "Die in the Rift Area");
      template.setAchievementsTriggered(new int[]{392});
      template = createAchievement(
         392,
         "Die By The Rift A Gazillion Times",
         false,
         100,
         (byte)4,
         "You're not here for the hunt, are you?",
         false,
         generatedTemplates,
         "Die a lot in the Rift Area"
      );
      template = createAchievement(
         393, "Shutting Down", false, 1, (byte)3, "You were there when the Rift was shut", false, generatedTemplates, "Shut down the Rift"
      );
      template = createAchievement(394, "Cottage Pie", false, 1, (byte)3, "Just like my grandmother used to make!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(395, "Fish Pie", false, 1, (byte)3, "You made that, sounds a bit fishy to me!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(396, "Raspberry Pi", false, 1, (byte)3, "The code for this was intense!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(397, "Fruit Pie", false, 1, (byte)3, "Fruity", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(398, "Berry Pie", false, 1, (byte)3, "Berry nice!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(399, "Custard Pie", false, 1, (byte)3, "Phanton flam flinger", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(400, "Chocolate Icing", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404});
      template = createAchievement(401, "Chocolate Cake", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404});
      template = createAchievement(402, "Chocolate Biscuit", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404});
      template = createAchievement(403, "Chocolate", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404});
      template = createAchievement(
         404, "Chocoholic", false, 1, (byte)3, "um, chocolate!", true, generatedTemplates, true, true, "Make a load of chocolate foods!"
      );
      template.setRequiredAchievements(new int[]{400, 401, 402, 464, 403, 409, 406, 405, 462});
      template = createAchievement(405, "Chocolate Chip Cookie", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404, 408});
      template = createAchievement(406, "Chocolate Cookie", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404, 408});
      template = createAchievement(
         407, "Cookies", false, 1, (byte)3, "You say cookie, I say biscuit, lets om-nom-nom", true, generatedTemplates, "Create a cookie"
      );
      template.setAchievementsTriggered(new int[]{408});
      template = createAchievement(
         408, "Cookie Monster", false, 1000, (byte)3, "C is for cookie, and that's good enough for me!", true, generatedTemplates, "Create 1000 cookies"
      );
      template = createAchievement(
         409, "Black Forest Gateau", false, 1, (byte)3, "I want to get lost in this forest!", true, generatedTemplates, "Make a black forest gateau"
      );
      template.setAchievementsTriggered(new int[]{404, 411});
      template = createAchievement(
         410, "Custard Creams", false, 1, (byte)3, "When only the cream de la cream will do!", true, generatedTemplates, "Make a custard cream"
      );
      template = createAchievement(
         411,
         "Around The World",
         false,
         1,
         (byte)3,
         "...around the world, around the world, around the woooorld...",
         false,
         generatedTemplates,
         true,
         true,
         "Make meals from different places"
      );
      template.setRequiredAchievements(new int[]{409, 412, 413, 414, 415, 416, 417, 418, 419, 420, 421, 422, 423, 424, 425, 426, 427, 463});
      template = createAchievement(412, "Lutefisk", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(413, "Fiskbolle", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(414, "Bangers And Mash", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(415, "Fish And Chips", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(416, "Paella", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(417, "Kielbasa", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(418, "Haggis", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(419, "Ratatouille", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(420, "Special Fried Rice", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(421, "Frikadeller", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(422, "Focaccia", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{192, 411});
      template = createAchievement(423, "Rice Wine", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(424, "Any Pizza", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{444, 411});
      template = createAchievement(425, "Any Pasta", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{435, 411});
      template = createAchievement(426, "Any Curry", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(427, "Any Goulash", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{411});
      template = createAchievement(428, "Pasta Napolitana", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{425, 434});
      template = createAchievement(429, "Pasta Carbonara", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{425, 434});
      template = createAchievement(430, "Simple Pasta", false, 1, (byte)3, "I make a pasta!", true, generatedTemplates, "Make a simple pasta");
      template.setAchievementsTriggered(new int[]{425});
      template = createAchievement(431, "Pasta Al Funghi", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{425, 434});
      template = createAchievement(432, "Macaroni Cheese", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{425, 434});
      template = createAchievement(433, "Spaghetti Bolognese", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{425, 434});
      template = createAchievement(
         434, "Pasta Maker", false, 1, (byte)3, "Life is a combination of magic and pasta.", false, generatedTemplates, true, true, "Make each pasta meal"
      );
      template.setRequiredAchievements(new int[]{428, 429, 431, 432, 433});
      template = createAchievement(435, "Pasta Master", false, 1000, (byte)3, "Master of carby goodness", false, generatedTemplates, "Make 1000 pasta meals");
      template = createAchievement(436, "Three Cheese Pizza", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(437, "Four Seasons Pizza", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(438, "Pizza Marinara", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(439, "Pizza Margherita", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(440, "One True Pizza", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(441, "Full House Pizza", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(442, "Hawaiian Pizza", true, 1, (byte)3, "Invisible", true, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{424, 443});
      template = createAchievement(
         443,
         "Pizza Maker",
         false,
         1,
         (byte)3,
         "Making the world a better place, one tomato-laden delight at a time.",
         false,
         generatedTemplates,
         true,
         true,
         "Make each pizza meal"
      );
      template.setRequiredAchievements(new int[]{436, 437, 438, 439, 440, 441, 442});
      template = createAchievement(
         444,
         "Pizza Master",
         false,
         1000,
         (byte)3,
         "Pizza is amazing; even when you're in the middle of eating a pizza, you wish you were eating pizza.",
         false,
         generatedTemplates,
         "Make 1000 pizza meals"
      );
      template = createAchievement(445, "Gruel", false, 1, (byte)3, "Please sir, can I have some more?", true, generatedTemplates, "made gruel");
      template = createAchievement(
         446, "Vinegar", false, 1, (byte)3, "Oops, that was not expected, maybe i can use it for cooking!", true, generatedTemplates, "made vinegar"
      );
      template = createAchievement(
         447,
         "Whisky",
         false,
         1,
         (byte)3,
         "Always carry a small flask of whiskey in case of snake bite.  Also, always carry a small snake.",
         true,
         generatedTemplates,
         "made whisky"
      );
      template.setAchievementsTriggered(new int[]{452});
      template = createAchievement(448, "Vodka", false, 1, (byte)3, "At least 1 letter away from water", true, generatedTemplates, "made vodka");
      template.setAchievementsTriggered(new int[]{452});
      template = createAchievement(449, "Brandy", false, 1, (byte)3, "Drinking like an aspiring hero", true, generatedTemplates, "made brandy");
      template.setAchievementsTriggered(new int[]{452});
      template = createAchievement(450, "Gin", false, 1, (byte)3, "As drunk on fine floating palaces everywhere.", true, generatedTemplates, "made gin");
      template.setAchievementsTriggered(new int[]{452});
      template = createAchievement(451, "Moonshine", false, 1, (byte)3, "Yeee-haw!", true, generatedTemplates, "made moonshine");
      template.setAchievementsTriggered(new int[]{452});
      template = createAchievement(
         452, "Distiller", false, 1, (byte)3, "Distilled one of each!", true, generatedTemplates, true, true, "distilled one of each"
      );
      template.setRequiredAchievements(new int[]{447, 448, 449, 450, 451});
      template = createAchievement(453, "distilled liters", true, 1, (byte)3, "Invisible", false, generatedTemplates, "distilled liters");
      template.setIsInLiters(true);
      template.setAchievementsTriggered(new int[]{454});
      template = createAchievement(
         454, "Distilled 1000 Liters", false, 1, (byte)3, "I'm not as think as you drunk I am.", false, generatedTemplates, "distilled 1000 liters"
      );
      template = createAchievement(455, "Mead", false, 1, (byte)3, "Honey i'm home!", true, generatedTemplates, "made mead");
      template.setAchievementsTriggered(new int[]{458});
      template = createAchievement(456, "Cider", false, 1, (byte)3, "I'm a cider maker", true, generatedTemplates, "made mead");
      template.setAchievementsTriggered(new int[]{458});
      template = createAchievement(457, "Beer", false, 1, (byte)3, "Homebrew!", true, generatedTemplates, "made beer");
      template.setAchievementsTriggered(new int[]{458});
      template = createAchievement(458, "Brewer", false, 1, (byte)3, "Brewed one of each.", true, generatedTemplates, true, true, "brewed one of each");
      template.setRequiredAchievements(new int[]{455, 456, 457});
      template = createAchievement(459, "brewed liters", true, 1, (byte)3, "Invisible", false, generatedTemplates, "brewed liters");
      template.setIsInLiters(true);
      template.setAchievementsTriggered(new int[]{460});
      template = createAchievement(460, "Brewed 1000 Liters", false, 1, (byte)3, "Tipping the amber", false, generatedTemplates, "brewed 1000 liters");
      template = createAchievement(461, "Steak And Ale Pie", false, 1, (byte)3, "um, beer!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(462, "Chocolate Fudge Trifle", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404});
      template = createAchievement(463, "Olive Bread", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{192, 411});
      template = createAchievement(464, "Chocolate Chip Biscuit", true, 1, (byte)3, "Invisible", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{404});
      template = createAchievement(
         465, "Sushi Maker", false, 1, (byte)3, "Teach a man to fish, but not to cook, and he'll make sushi.", true, generatedTemplates
      );
      template = createAchievement(466, "Insect Stew", false, 1, (byte)3, "You begin to feel the speed of 6 legs come upon you.", true, generatedTemplates);
      template = createAchievement(467, "Scotsman", false, 1, (byte)3, "Just like a true scotsman.", true, generatedTemplates);
      template = createAchievement(468, "Pirates", false, 1, (byte)3, "Ahoy, me salty matey!  Come and join the scurvy trolls!", true, generatedTemplates);
      template = createAchievement(469, "Pork Pie", false, 1, (byte)3, "Come here piggy!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(470, "Meat Pie", false, 1, (byte)3, "Meat and gravy all in a pie!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{197});
      template = createAchievement(471, "Any Muffin", false, 1, (byte)3, "Small cake anyone!", true, generatedTemplates);
      template.setAchievementsTriggered(new int[]{472});
      template = createAchievement(
         472,
         "The Smell of Freshly Made Muffins",
         false,
         100,
         (byte)3,
         "If you have two muffins, just eat them!",
         false,
         generatedTemplates,
         "Make 100 muffins"
      );
      template.setAchievementsTriggered(new int[]{473});
      template = createAchievement(473, "Muffin Maker", false, 100, (byte)4, "Do you know the Muffin Man?", false, generatedTemplates, "Make a 1000 muffins");
      template = createAchievement(
         474, "Slate Bridge Maker", false, 1, (byte)3, "You finished a Slate Bridge", false, generatedTemplates, "Build a Slate Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         475,
         "Rounded Stone Bridge Maker",
         false,
         1,
         (byte)3,
         "You finished a Rounded Stone Bridge",
         false,
         generatedTemplates,
         "Build a Rounded Stone Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         476, "Pottery Bridge Maker", false, 1, (byte)3, "You finished a Pottery Bridge", false, generatedTemplates, "Build a Pottery Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         477, "Sandstone Bridge Maker", false, 1, (byte)3, "You finished a Sandstone Bridge", false, generatedTemplates, "Build a Sandstone Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(
         478, "Rendered Bridge Maker", false, 1, (byte)3, "You finished a Rendered Bridge", false, generatedTemplates, "Build a Rendered Bridge"
      );
      template.setAchievementsTriggered(new int[]{362});
      template = createAchievement(479, "investigator", true, 1, (byte)3, "Invisible", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{480});
      template = createAchievement(
         480,
         "Talented Investigator",
         false,
         50,
         (byte)3,
         "The ancient world cannot hide any secrets from you!",
         false,
         generatedTemplates,
         "Unearth 50 fragments"
      );
      template = createAchievement(
         481,
         "Rarity Discoverer",
         false,
         1,
         (byte)3,
         "You have no trouble finding the best secrets of the past.",
         false,
         generatedTemplates,
         "Unearth a rare fragment"
      );
      template = createAchievement(482, "indentifier", true, 1, (byte)3, "Invisible", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{483});
      template = createAchievement(
         483,
         "Careful Identifier",
         false,
         50,
         (byte)3,
         "Care and attention to detail let you identify ancient items.",
         false,
         generatedTemplates,
         "Completely identify 50 fragments"
      );
      template = createAchievement(484, "combiner", true, 1, (byte)3, "Invisible", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{485, 550});
      template = createAchievement(
         485, "Master of Recreation", false, 50, (byte)4, "Just like pieces of a puzzle.", false, generatedTemplates, "Recreate 50 items from fragments"
      );
      template = createAchievement(
         486,
         "Statue Bro?",
         false,
         1,
         (byte)4,
         "A really big puzzle with a lot of pieces - easy.",
         false,
         generatedTemplates,
         "Recreate a statue from fragments"
      );
      template.setAchievementsTriggered(new int[]{592});
      template = createAchievement(
         487, "Maskter of Recreation", false, 1, (byte)4, "People really wore these things?", false, generatedTemplates, "Recreate a mask from fragments"
      );
      template = createAchievement(
         488,
         "Fragmental",
         false,
         1,
         (byte)3,
         "The ancients apparently used these tools too, who knew?",
         false,
         generatedTemplates,
         "Recreate a tool from fragments"
      );
      template = createAchievement(489, "rune creator", true, 1, (byte)3, "Invisible", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{490, 593});
      template = createAchievement(490, "Master of Runes", false, 500, (byte)4, "Just a few more bonuses.", false, generatedTemplates, "Create 500 runes");
      template = createAchievement(491, "rune user", true, 1, (byte)3, "Invisible", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{492, 586});
      template = createAchievement(
         492, "Longterm Attachments", false, 500, (byte)4, "May they never detach.", false, generatedTemplates, "Successfully use 500 runes"
      );
      template = createAchievement(
         493, "If Looks Could Kill", false, 1, (byte)3, "Supposedly if you dye them red, you'll move faster.", false, generatedTemplates, "Dye some armour"
      );
      template = createAchievement(
         494, "Colourful Sailing", false, 1, (byte)3, "Surely the extra colour will help it catch the wind better.", false, generatedTemplates, "Dye a sail"
      );
      template = createAchievement(495, "Iron Item", false, 1, (byte)3, "You created an iron item.", false, generatedTemplates, "Create an iron item");
      template = createAchievement(
         496, "Adamantine Item", false, 1, (byte)3, "You created an adamantine item.", false, generatedTemplates, "Create an adamantine item"
      );
      template = createAchievement(497, "Brass Item", false, 1, (byte)3, "You created a brass item.", false, generatedTemplates, "Create a brass item");
      template = createAchievement(498, "Bronze Item", false, 1, (byte)3, "You created a bronze item.", false, generatedTemplates, "Create a bronze item");
      template = createAchievement(499, "Copper Item", false, 1, (byte)3, "You created a copper item.", false, generatedTemplates, "Create a copper item");
      template = createAchievement(
         500, "Glimmersteel Item", false, 1, (byte)3, "You created a glimmersteel item.", false, generatedTemplates, "Create a glimemrsteel item"
      );
      template = createAchievement(501, "Gold Item", false, 1, (byte)3, "You created a gold item.", false, generatedTemplates, "Create a gold item");
      template = createAchievement(502, "Lead Item", false, 1, (byte)3, "You created a lead item.", false, generatedTemplates, "Create a lead item");
      template = createAchievement(503, "Seryll Item", false, 1, (byte)3, "You created a seryll item.", false, generatedTemplates, "Create a seryll item");
      template = createAchievement(504, "Silver Item", false, 1, (byte)3, "You created a silver item.", false, generatedTemplates, "Create a silver item");
      template = createAchievement(505, "Steel Item", false, 1, (byte)3, "You created a steel item.", false, generatedTemplates, "Create a steel item");
      template = createAchievement(506, "Tin Item", false, 1, (byte)3, "You created a tin item.", false, generatedTemplates, "Create a tin item");
      template = createAchievement(507, "Zinc Item", false, 1, (byte)3, "You created a zinc item.", false, generatedTemplates, "Create a zinc item");
      template = createAchievement(
         508, "Rack 'Em Up", false, 1, (byte)3, "Keepin' things tidy with some storage racks.", false, generatedTemplates, "Create a storage rack"
      );
      template = createAchievement(509, "place on decorations", true, 1, (byte)3, "Invisible", false, generatedTemplates);
      template.setAchievementsTriggered(new int[]{510});
      template = createAchievement(
         510, "Neat and Tidy", false, 100, (byte)3, "Now that looks a lot better!", false, generatedTemplates, "Place 100 items on top of other items"
      );
      template.setAchievementsTriggered(new int[]{511});
      template = createAchievement(
         511,
         "A Place for Everything",
         false,
         25,
         (byte)4,
         "A place for everything and everything in its place.",
         false,
         generatedTemplates,
         "Place 2500 items on top of other items"
      );
      template = createAchievement(
         512, "Electrum Item", false, 1, (byte)3, "You created an electrum item.", false, generatedTemplates, "Create an electrum item"
      );
      createAchievement(
         513,
         "Chop Down Some Trees",
         true,
         5,
         (byte)3,
         "You managed to cut down 5 trees.",
         false,
         generatedTemplates,
         "Activate your hatchet and fell 5 lumber trees that are old, very old or overaged."
      );
      createAchievement(
         514,
         "Flatten a Tile",
         true,
         1,
         (byte)3,
         "You completely flattened a tile.",
         false,
         generatedTemplates,
         "Activate your shovel and use the flatten action on a tile to completely flatten it"
      );
      createAchievement(
         515,
         "Create a Mallet",
         true,
         1,
         (byte)3,
         "You completed a mallet.",
         false,
         generatedTemplates,
         "Create a wooden mallet from a mallet head and a shaft"
      );
      createAchievement(
         516,
         "Mine Iron",
         true,
         1,
         (byte)3,
         "You mined some iron ore.",
         false,
         generatedTemplates,
         "Activate your pickaxe and mine an iron vein to get some iron ore"
      );
      createAchievement(
         517,
         "Create Some Nails",
         true,
         1,
         (byte)3,
         "You created some iron nails.",
         false,
         generatedTemplates,
         "Create some iron small or large nails using a hot iron lump and small anvil"
      );
      createAchievement(
         518,
         "Plan a House",
         true,
         1,
         (byte)3,
         "You planned your first house.",
         false,
         generatedTemplates,
         "Use a hammer or mallet on a flat tile to plan a house, then right click the building plan and finalise it"
      );
      createAchievement(
         519,
         "Complete a House",
         true,
         1,
         (byte)3,
         "You completed your first house.",
         false,
         generatedTemplates,
         "Complete building all of the outer walls of a newly planned building"
      );
      createAchievement(
         520,
         "Improve a Metal Tool to 15QL",
         true,
         1,
         (byte)3,
         "You improved a metal tool to above 15QL.",
         false,
         generatedTemplates,
         "Activate the improvement item shown in the examine message of the tool and improve the item until at least 15QL"
      );
      createAchievement(
         521, "Ride a Cow", true, 1, (byte)3, "You're a true cowboy now!", false, generatedTemplates, "Activate a rope to lead a cow before riding it"
      );
      createAchievement(
         522, "Kill a Creature", true, 1, (byte)3, "You killed a creature.", false, generatedTemplates, "Target and attack a creature until it's dead"
      );
      createAchievement(
         523, "Plant a Crop", true, 1, (byte)3, "You planted a crop.", false, generatedTemplates, "Forage or botanize for seeds to plant on a dirt tile"
      );
      createAchievement(
         524,
         "Find a Route",
         true,
         1,
         (byte)3,
         "You found a route on a highway.",
         false,
         generatedTemplates,
         "Find a waystone and find a route to another place"
      );
      createAchievement(
         525,
         "Build a Stone House Wall",
         true,
         1,
         (byte)3,
         "You built a stone house wall.",
         false,
         generatedTemplates,
         "Activate a trowel, then add bricks and mortar to a house wall plan"
      );
      createAchievement(
         526,
         "Tame a Creature",
         true,
         1,
         (byte)3,
         "You tamed a creature.",
         false,
         generatedTemplates,
         "Activate food the creature would eat, then attempt to tame it"
      );
      template = createAchievement(
         527,
         "Mine Copper",
         true,
         1,
         (byte)3,
         "You mined copper.",
         false,
         generatedTemplates,
         "Activate your pickaxe and mine a copper vein to get some copper ore"
      );
      template.setAchievementsTriggered(new int[]{529});
      template = createAchievement(
         528, "Mine Tin", true, 1, (byte)3, "You mined tin.", false, generatedTemplates, "Activate your pickaxe and mine a tin vein to get some tin ore"
      );
      template.setAchievementsTriggered(new int[]{529});
      template = createAchievement(
         529,
         "Mine Copper and Tin",
         true,
         1,
         (byte)3,
         "You mined copper and tin.",
         false,
         generatedTemplates,
         "Activate your pickaxe and mine both copper and tin"
      );
      template.setRequiredAchievements(new int[]{527, 528});
      createAchievement(
         530,
         "Create a Bronze Lump",
         true,
         1,
         (byte)3,
         "You created a bronze lump.",
         false,
         generatedTemplates,
         "Activate a glowing hot copper lump and combine it with a glowing hot tin lump to create bronze"
      );
      createAchievement(
         531,
         "Breed a Creature",
         true,
         1,
         (byte)3,
         "You bred a new creature.",
         false,
         generatedTemplates,
         "Lead a creature, then mate it with another creature of the opposite gender"
      );
      createAchievement(
         532,
         "Create a Small Cart",
         true,
         1,
         (byte)3,
         "You created a small cart.",
         false,
         generatedTemplates,
         "Create a wheel axle using planks and shafts, then use a plank to begin constructing a small cart"
      );
      createAchievement(
         533,
         "Create a Forge or Oven",
         true,
         1,
         (byte)3,
         "You created a forge or oven.",
         false,
         generatedTemplates,
         "Use stone bricks and clay to create a forge or oven"
      );
      createAchievement(
         534,
         "Cook a Breakfast",
         true,
         1,
         (byte)3,
         "You cooked a breakfast.",
         true,
         generatedTemplates,
         "Heat a piece of meat and a vegetable in a pottery bowl inside a lit oven to cook a breakfast"
      );
      createAchievement(
         535, "Ride a Horse", true, 1, (byte)3, "You rode a horse.", false, generatedTemplates, "Activate a rope and lead a horse before riding it"
      );
      createAchievement(
         536,
         "Bandage a Wound",
         true,
         1,
         (byte)3,
         "You bandaged a wound.",
         false,
         generatedTemplates,
         "Activate cotton and then first aid a wound in the character panel"
      );
      createAchievement(
         537,
         "Plant a Tree",
         true,
         1,
         (byte)3,
         "You planted a tree.",
         false,
         generatedTemplates,
         "Use a sickle to harvest a sprout from a tree, then activate the sprout and plant it on a dirt or grass tile"
      );
      createAchievement(
         538,
         "Create a Large Cart",
         true,
         1,
         (byte)3,
         "You created a large cart.",
         false,
         generatedTemplates,
         "Create a wheel axle using planks and shafts, then use a plank to begin constructing a large cart"
      );
      createAchievement(
         539,
         "Build a Two Story Building",
         true,
         1,
         (byte)3,
         "You built a two story building.",
         false,
         generatedTemplates,
         "Create a ladder or staircase, then construct the next level of the house"
      );
      createAchievement(
         540,
         "Create a Boat",
         true,
         1,
         (byte)3,
         "You created a boat.",
         false,
         generatedTemplates,
         "Complete the creation of a boat of your choice, search the crafting recipes window for how to begin building one"
      );
      createAchievement(
         541,
         "Cook a Meal",
         true,
         1,
         (byte)3,
         "You cooked a meal.",
         true,
         generatedTemplates,
         "Heat a piece of meat and a vegetable inside a frying pan inside a lit oven to cook a meal"
      );
      createAchievement(
         542,
         "Catch a Large Fish",
         true,
         1,
         (byte)3,
         "You caught a large fish.",
         false,
         generatedTemplates,
         "Activate a fishing rod and fish at water level until you catch a large fish"
      );
      createAchievement(
         543,
         "Improve a Tool to 50QL",
         true,
         1,
         (byte)3,
         "You improved a tool to 50QL.",
         false,
         generatedTemplates,
         "Activate the improvement item shown in the examine message of the tool and improve the item until at least 50QL"
      );
      createAchievement(
         544,
         "Harvest 5 Cotton",
         true,
         1,
         (byte)3,
         "You harvested cotton.",
         false,
         generatedTemplates,
         "Tend the cotton field as it grows, granting higher yield when it is ripe for harvest"
      );
      createAchievement(
         545, "Ride a Bear or Bull", true, 1, (byte)3, "You rode a bear or bull.", false, generatedTemplates, "Tame the creature before leading and riding it"
      );
      createAchievement(
         546,
         "Create a Healing Cover",
         true,
         1,
         (byte)3,
         "You created a healing cover.",
         false,
         generatedTemplates,
         "Use two alchemical reagents, such as vegetables and butchered creature parts, and mix them to create a healing cover"
      );
      createAchievement(
         547,
         "Sleep in a Bed",
         true,
         1,
         (byte)3,
         "You slept in a bed.",
         false,
         generatedTemplates,
         "Sleeping in a bed grants sleep bonus, which can be used to increase skill gain"
      );
      createAchievement(
         548,
         "Join a Meditation Path",
         true,
         1,
         (byte)3,
         "You joined a meditation path.",
         false,
         generatedTemplates,
         "Meditate using a meditation rug until you have 20 meditation skill, then find a proper tile to join a path"
      );
      createAchievement(
         549,
         "Get to Focus Level 3",
         true,
         1,
         (byte)3,
         "You focused to level 3 during combat.",
         false,
         generatedTemplates,
         "Use the focus button during combat until you feel lightning inside, quickening your reflexes"
      );
      createAchievement(
         550,
         "Restore an Item from Fragments",
         true,
         1,
         (byte)3,
         "You restored an archaeology item.",
         false,
         generatedTemplates,
         "Activate a trowel and search tiles for archaeology fragments, then restore them with a metal brush"
      );
      createAchievement(551, "Slay a Troll", true, 1, (byte)3, "You slayed a troll.", false, generatedTemplates, "Find a troll and defeat it in combat");
      createAchievement(
         552,
         "Open a Wild Hive",
         true,
         1,
         (byte)3,
         "You are the master of bees.",
         false,
         generatedTemplates,
         "Find a wild bee hive and open it without being stung by using a bee smoker"
      );
      createAchievement(
         553,
         "Study a Tree or Bush",
         true,
         1,
         (byte)3,
         "You studied a tree or bush.",
         false,
         generatedTemplates,
         "Study a tree or bush to find out more about it"
      );
      createAchievement(
         554,
         "Improve a Weapon to 50QL",
         true,
         1,
         (byte)3,
         "You improved a weapon to 50QL.",
         false,
         generatedTemplates,
         "Activate the improvement item shown in the examine message of the weapon and improve the weapon until at least 50QL"
      );
      createAchievement(555, "Get a Skill to 50", true, 1, (byte)3, "You have a skill over 50.", false, generatedTemplates, "Get any skill above 50");
      createAchievement(
         556,
         "Become a Follower of a Deity",
         true,
         1,
         (byte)3,
         "You followed a deity.",
         false,
         generatedTemplates,
         "Be converted to a deity by another player, or make the pilgrimage to the white or black light"
      );
      String pbString;
      if (Features.Feature.SINGLE_PLAYER_BRIDGES.isEnabled()) {
         pbString = " a planted range pole, or ";
      } else {
         pbString = " a ";
      }

      createAchievement(
         557,
         "Plan a Bridge",
         true,
         1,
         (byte)3,
         "You planned a bridge.",
         false,
         generatedTemplates,
         "Activate a dioptra and target" + pbString + "player holding a range pole, to plan a bridge"
      );
      createAchievement(558, "Pilot a Knarr", true, 1, (byte)3, "You piloted a knarr.", false, generatedTemplates, "Take the commander seat of a knarr");
      createAchievement(559, "Spear a Fish", true, 1, (byte)3, "You speared a fish.", false, generatedTemplates, "Use a spear and go spear fishing");
      createAchievement(
         560,
         "Make some Wine",
         true,
         1,
         (byte)3,
         "You fermented some wine.",
         false,
         generatedTemplates,
         "Seal a wine barrel with grape juice and maple syrup, then let it ferment into wine"
      );
      createAchievement(
         561,
         "Create a Halter Rope",
         true,
         1,
         (byte)3,
         "You created a halter rope.",
         false,
         generatedTemplates,
         "Combine four ropes together to create a halter rope, allowing you to lead up to four creatures at once"
      );
      createAchievement(
         562,
         "Get to Focus Level 5",
         true,
         1,
         (byte)3,
         "You focused to level 5 during combat.",
         false,
         generatedTemplates,
         "Focus in combat until you feel supernatural"
      );
      createAchievement(
         563,
         "Taunt a Creature",
         true,
         1,
         (byte)3,
         "You taunted a creature.",
         false,
         generatedTemplates,
         "Enter combat with a creature, then use the taunt action while it's attacking something other than you"
      );
      createAchievement(564, "Get a Skill to 70", true, 1, (byte)3, "You have a skill over 70.", false, generatedTemplates, "Get any skill above 70");
      createAchievement(
         565,
         "Improve a Tool to 70QL",
         true,
         1,
         (byte)3,
         "You improved a tool to 70QL.",
         false,
         generatedTemplates,
         "Activate the improvement item shown in the examine message of the tool and improve the tool until at least 70QL"
      );
      createAchievement(
         566,
         "Slay a Greenish Creature",
         true,
         1,
         (byte)3,
         "You defeated a greenish creature.",
         false,
         generatedTemplates,
         "Greenish creatures are conditioned to be stronger than normal creatures"
      );
      createAchievement(567, "Get Drunk", true, 1, (byte)3, "You got drunk.", false, generatedTemplates, "Drink alcohol until you are drunk");
      createAchievement(
         568,
         "Become Diseased",
         true,
         1,
         (byte)3,
         "You were diseased.",
         false,
         generatedTemplates,
         "Spending a lot of time near livestock can give you disease"
      );
      createAchievement(
         569, "Reach 30 Faith", true, 1, (byte)3, "You reached 30 faith.", false, generatedTemplates, "Pray to your deity until you reach 30 faith"
      );
      createAchievement(
         570,
         "Reach Meditation Path Level 4",
         true,
         1,
         (byte)3,
         "You reached level 4 in a meditation path.",
         false,
         generatedTemplates,
         "Meditate and answer questions on a proper tile to advance in a path"
      );
      createAchievement(
         571,
         "Build an Underground House",
         true,
         1,
         (byte)3,
         "You built an underground house.",
         false,
         generatedTemplates,
         "Flatten and reinforce a cave floor with a support beam, then plan a house using a hammer or mallet"
      );
      createAchievement(
         572,
         "Obtain 15 Titles",
         true,
         1,
         (byte)3,
         "You obtained 15 titles.",
         false,
         generatedTemplates,
         "Titles can be obtained by increasing skills or completing special tasks"
      );
      createAchievement(
         573,
         "Kill a Creature with a Catapult",
         true,
         1,
         (byte)3,
         "You killed a creature with a catapult.",
         false,
         generatedTemplates,
         "Load a catapult with heavy objects, then launch them at a creature to slay it"
      );
      createAchievement(
         574,
         "Build a Guard Tower",
         true,
         1,
         (byte)3,
         "You built a guard tower.",
         false,
         generatedTemplates,
         "Construct a guard tower using stone bricks, clay, and planks"
      );
      createAchievement(
         575,
         "Complete an Almanac",
         true,
         1,
         (byte)3,
         "You completed an almanac.",
         false,
         generatedTemplates,
         "Study the world and record your findings in an almanac then read from it when it is completed"
      );
      createAchievement(
         576, "Join a Village", true, 1, (byte)3, "You joined a village.", false, generatedTemplates, "Join an existing village or found your own"
      );
      createAchievement(
         577,
         "Slay a Champion Creature",
         true,
         1,
         (byte)3,
         "You killed a champion.",
         false,
         generatedTemplates,
         "Find and kill a creature with the champion modifier"
      );
      createAchievement(
         578,
         "Reach Meditation Path Level 7",
         true,
         1,
         (byte)3,
         "You reached level 7 in a meditation path.",
         false,
         generatedTemplates,
         "Meditate and answer questions on a proper tile to advance in a path"
      );
      createAchievement(
         579,
         "Obtain 30 Titles",
         true,
         1,
         (byte)3,
         "You obtained 30 titles.",
         false,
         generatedTemplates,
         "Titles can be obtained by increasing skills or completing special tasks"
      );
      createAchievement(
         580,
         "Discover an Archaeology Cache",
         true,
         1,
         (byte)3,
         "You found an archaeology cache.",
         false,
         generatedTemplates,
         "Complete an Archaeology report enough to uncover a cache at the center of an old village"
      );
      createAchievement(
         581,
         "Equip an Enchanted Weapon",
         true,
         1,
         (byte)3,
         "You equipped an enchanted weapon.",
         false,
         generatedTemplates,
         "Equip a weapon with at least one enchantment on it"
      );
      createAchievement(
         582,
         "Help Complete 10 Missions",
         true,
         10,
         (byte)3,
         "You helped complete some missions.",
         false,
         generatedTemplates,
         "Participate in 10 different god missions. Viewing missions can be enabled from your HUD Settings."
      );
      createAchievement(
         583,
         "Create a Wagon",
         true,
         1,
         (byte)3,
         "You created a wagon.",
         false,
         generatedTemplates,
         "Complete creation of a wagon, starting with a plank on a small axle"
      );
      createAchievement(
         584,
         "Ride a Horse Over 30km/h",
         true,
         1,
         (byte)3,
         "You rode quite a fast horse.",
         false,
         generatedTemplates,
         "Breed a horse for speed or equip one with high quality or enchanted horse shoes and saddle"
      );
      createAchievement(
         585,
         "Catch a Huge Fish",
         true,
         1,
         (byte)3,
         "You caught a huge fish.",
         false,
         generatedTemplates,
         "Catch a fish of at least 10kg using a fishing rod."
      );
      createAchievement(
         586,
         "Use a Rune",
         true,
         1,
         (byte)3,
         "You enchanted an item with a rune.",
         false,
         generatedTemplates,
         "Enchant an item with a rune by activating the rune and using it on an item"
      );
      createAchievement(
         587, "Harvest 50 Herbs", true, 50, (byte)3, "You harvested 50 herbs.", false, generatedTemplates, "Harvest 50 different herbs from pottery planters"
      );
      template = createAchievement(
         588,
         "Slay Each Rift Creature",
         true,
         1,
         (byte)3,
         "You killed at least one of each rift creature.",
         false,
         generatedTemplates,
         "Kill at least one of each of these rift creatures: Rift Jackal, Rift Beast, Rift Ogre and Rift Warmaster."
      );
      template.setRequiredAchievements(new int[]{378, 379, 380, 381});
      createAchievement(
         589,
         "Slay a Legendary Creature",
         true,
         1,
         (byte)3,
         "You killed a legendary creature.",
         false,
         generatedTemplates,
         "Kill a legendary creature, such as a dragon, troll king, kyklops or forest giant"
      );
      createAchievement(590, "Get a Skill to 90", true, 1, (byte)3, "You have a skill over 90.", false, generatedTemplates, "Get any skill above 90");
      createAchievement(
         591,
         "Obtain 60 Titles",
         true,
         1,
         (byte)3,
         "You obtained 60 titles.",
         false,
         generatedTemplates,
         "Titles can be obtained by increasing skills or completing special tasks"
      );
      createAchievement(
         592,
         "Restore a Statue from Fragments",
         true,
         1,
         (byte)3,
         "You restored a statue.",
         false,
         generatedTemplates,
         "Completely restore a statue from fragments found via Archaeology"
      );
      createAchievement(
         593,
         "Create 10 Runes",
         true,
         10,
         (byte)3,
         "You created 10 runes.",
         false,
         generatedTemplates,
         "Using rift materials and different metal lumps, successfully create at least 10 runes"
      );
      createAchievement(
         594, "Ride a Unicorn", true, 1, (byte)3, "You rode a unicorn.", false, generatedTemplates, "You can tame or charm a Unicorn to be able to ride it"
      );
      createAchievement(
         595,
         "Mine a Tonne of High Quality Ore",
         true,
         50,
         (byte)3,
         "You mined a tonne of high quality ore",
         false,
         generatedTemplates,
         "Mine at least 1000kg of 80+QL Metal Ore"
      );
      createAchievement(
         596,
         "Cook Billy Sheep Gruff Stew",
         true,
         1,
         (byte)3,
         "You cooked some Billy Sheep Gruff Stew",
         true,
         generatedTemplates,
         "Discover the recipe for Billy Sheep Gruff Stew from defeating Trolls then cook it"
      );
      createAchievement(
         597,
         "Improve an Armour Piece to 80QL",
         true,
         1,
         (byte)3,
         "You improved some armour to high quality.",
         false,
         generatedTemplates,
         "Improve a single piece of armour to at least 80ql"
      );
      createAchievement(
         598, "Get 10 Skills to 50", true, 1, (byte)3, "You have at least 10 skills over 50", false, generatedTemplates, "Get any 10 skills above 50"
      );
      createAchievement(
         599,
         "Reach Meditation Path Level 9",
         true,
         1,
         (byte)3,
         "You reached level 9 in a meditation path.",
         false,
         generatedTemplates,
         "Meditate and answer questions on a proper tile to advance in a path"
      );
      createAchievement(
         600,
         "Win 10 Spars",
         true,
         10,
         (byte)3,
         "You are the sparring master.",
         false,
         generatedTemplates,
         "Spar with other players and win at least 10 of them"
      );
      template = createAchievement(601, "Help a Mission", true, 1, (byte)3, "Invisible", false, generatedTemplates, "Invisible");
      template.setAchievementsTriggered(new int[]{582});
      template = createAchievement(602, "Pick a Herb", true, 1, (byte)3, "Invisible", false, generatedTemplates, "Invisible");
      template.setAchievementsTriggered(new int[]{587});
      template = createAchievement(603, "Mine an 80QL Ore", true, 1, (byte)3, "Invisible", false, generatedTemplates, "Invisible");
      template.setAchievementsTriggered(new int[]{595});
      createAchievement(
         604,
         "Become a Priest",
         true,
         1,
         (byte)3,
         "You became a priest.",
         false,
         generatedTemplates,
         "Reach 30 faith with your deity then convert to a priest."
      );
      createAchievement(
         605,
         "Reach 20 Channeling",
         true,
         1,
         (byte)3,
         "You reached 20 channeling.",
         false,
         generatedTemplates,
         "Cast spells to increase your channeling skill to at least 20"
      );
      createAchievement(606, "Enchant an Item", true, 1, (byte)3, "You enchanted an item.", false, generatedTemplates, "Cast an enchantment spell on an item");
      template = createAchievement(
         607,
         "Sacrifice an Item",
         true,
         1,
         (byte)3,
         "You sacrificed an item.",
         false,
         generatedTemplates,
         "Sacrifice an item to your deity by placing it inside an altar of your deity and selecting the Sacrifice action"
      );
      template.setAchievementsTriggered(new int[]{624, 631});
      createAchievement(
         608, "Reach 40 Faith", true, 1, (byte)3, "You got to 40 faith.", false, generatedTemplates, "Pray to your deity until you reach at least 40 faith"
      );
      createAchievement(
         609,
         "Receive an Item via Prayer",
         true,
         1,
         (byte)3,
         "Your deity blessed you with an item.",
         false,
         generatedTemplates,
         "Pray to your deity and be blessed with an item"
      );
      createAchievement(
         610, "Listen to a Sermon", true, 1, (byte)3, "You listened to a sermon.", false, generatedTemplates, "Listen as another nearby priest holds a sermon"
      );
      createAchievement(
         611,
         "Sacrifice a Creature",
         true,
         1,
         (byte)3,
         "You sacrificed a creature.",
         false,
         generatedTemplates,
         "While in your deity's domain, use a sacrificial knife on a weak creature to sacrifice them for some favor"
      );
      createAchievement(
         612,
         "Listen to a Confession",
         true,
         1,
         (byte)3,
         "You heard another player's confession.",
         false,
         generatedTemplates,
         "Listen to the confession of another player that also follows your deity"
      );
      createAchievement(
         613,
         "Spend 1k Favor",
         true,
         1000,
         (byte)3,
         "You spent 1000 favor with your deity.",
         false,
         generatedTemplates,
         "Cast enough spells to use up at least 1000 favor with your deity"
      );
      createAchievement(
         614,
         "Store Favor in a Gem",
         true,
         1,
         (byte)3,
         "You vesseled some of your favor inside a gem.",
         false,
         generatedTemplates,
         "Cast the Vessel spell on a gem higher than 10QL to vessel some of your favor inside for later use."
      );
      createAchievement(
         615,
         "Link with Another Priest",
         true,
         1,
         (byte)3,
         "You linked with another priest.",
         false,
         generatedTemplates,
         "Link your favor with another priest aligned with your deity in order to cast more powerful spells"
      );
      createAchievement(
         616,
         "Create an Electrum Statuette",
         true,
         1,
         (byte)3,
         "You created an electrum statuette",
         false,
         generatedTemplates,
         "Using an electrum lump on a small anvil, create a statuette aligned to your deity"
      );
      createAchievement(
         617,
         "Reach 50 Channeling",
         true,
         1,
         (byte)3,
         "You reached 50 channeling.",
         false,
         generatedTemplates,
         "Cast spells to increase your channeling skill to at least 50"
      );
      createAchievement(
         618, "Reach 70 Faith", true, 1, (byte)3, "You got to 70 faith.", false, generatedTemplates, "Pray to your deity until you reach at least 70 faith"
      );
      createAchievement(
         619,
         "Cast a Spell Worth 50 Favor",
         true,
         1,
         (byte)3,
         "You cast a spell worth at least 50 favor.",
         false,
         generatedTemplates,
         "In a single casting of a spell spend at least 50 favor"
      );
      createAchievement(
         620,
         "Sacrifice an Item Worth 30 Favor",
         true,
         1,
         (byte)3,
         "You sacrificed a high value item",
         false,
         generatedTemplates,
         "Sacrifice a high value item to your deity that gains you at least 30 favor"
      );
      createAchievement(
         621,
         "Convert a Player",
         true,
         1,
         (byte)3,
         "You converted another player to your deity.",
         false,
         generatedTemplates,
         "Convert a player of another deity or no deity to follow your deity"
      );
      template = createAchievement(
         622,
         "Receive a Gem via Prayer",
         true,
         1,
         (byte)3,
         "Your deity blessed you with a gem from prayer.",
         false,
         generatedTemplates,
         "Reach high enough faith with your deity to receive a gem randomly from prayer"
      );
      template.setAchievementsTriggered(new int[]{637});
      createAchievement(
         623,
         "Hold 10 Sermons",
         true,
         10,
         (byte)3,
         "You spread the word of your deity.",
         false,
         generatedTemplates,
         "Spread the word of your deity by holding at least 10 sermons to nearby listening players"
      );
      createAchievement(
         624,
         "Sacrifice 1k Items",
         true,
         1000,
         (byte)3,
         "You sacrificed a lot of items to your deity.",
         false,
         generatedTemplates,
         "Sacrifice at least 1000 items to your deity"
      );
      createAchievement(
         625,
         "Recover 50 Favor from a Vesseled Gem",
         true,
         1,
         (byte)3,
         "You recovered a lot of favor from a gem.",
         false,
         generatedTemplates,
         "Use a vesseled gem while low on favor to regain at least 50 favor at once"
      );
      createAchievement(
         626,
         "Fully Align With Your Deity",
         true,
         1,
         (byte)3,
         "You are aligned with your deity.",
         false,
         generatedTemplates,
         "Reach 100 (or -100 if you follow a black-light god) alignment to become fully aligned with your deity"
      );
      createAchievement(
         627,
         "Shatter an Item",
         true,
         1,
         (byte)3,
         "Your spell casting caused an item to shatter.",
         false,
         generatedTemplates,
         "Fail enough when casting a spell on an item to cause it to shatter"
      );
      createAchievement(
         628,
         "Create a Gold Altar",
         true,
         1,
         (byte)3,
         "You created a shiny gold altar for your deity.",
         false,
         generatedTemplates,
         "Using a 5kg gold lump on a large anvil, create a gold altar"
      );
      createAchievement(
         629,
         "Cast a 95 Power Spell",
         true,
         1,
         (byte)3,
         "You cast a spell with a very high power.",
         false,
         generatedTemplates,
         "Cast a spell that ends up being at least 95 power"
      );
      createAchievement(
         630, "Reach 90 Faith", true, 1, (byte)3, "You got to 90 faith.", false, generatedTemplates, "Pray to your deity until you reach at least 90 faith"
      );
      createAchievement(
         631,
         "Sacrifice 10k Items",
         true,
         10000,
         (byte)3,
         "You sacrificed a ton of items.",
         false,
         generatedTemplates,
         "Sacrifice at least 10000 items to your deity"
      );
      createAchievement(
         632,
         "Hold 100 Sermons",
         true,
         100,
         (byte)3,
         "You have held a lot of sermons.",
         false,
         generatedTemplates,
         "Spread the word of your deity by holding at least 100 sermons to nearby listening players"
      );
      createAchievement(
         633, "Reach 70 Prayer", true, 1, (byte)3, "You got to 70 prayer.", false, generatedTemplates, "Pray to your deity enough to reach 70 prayer"
      );
      createAchievement(
         634,
         "Sacrifice a Champion Creature",
         true,
         1,
         (byte)3,
         "You sacrificed a champion creature.",
         false,
         generatedTemplates,
         "While in your deity's domain, use a sacrificial knife on a weakened champion creature to sacrifice them for some favor"
      );
      createAchievement(
         635,
         "Cast a Global Spell",
         true,
         1,
         (byte)3,
         "You cast a global spell.",
         false,
         generatedTemplates,
         "Cast yourself or link to a priest that casts a global spell for those aligned to your deity, giving everybody some bonuses"
      );
      createAchievement(
         636,
         "Spend 100k Favor",
         true,
         100000,
         (byte)3,
         "You spent a lot of favor with your deity",
         false,
         generatedTemplates,
         "Cast enough spells to use up at least 100000 favor with your deity"
      );
      createAchievement(
         637,
         "Receive 100 Gems via Prayer",
         true,
         100,
         (byte)3,
         "Your deity has blessed you with a lot of gems.",
         false,
         generatedTemplates,
         "Pray to your deity enough to receive at least 100 gems"
      );
      template = createAchievement(638, "Spend 1 Favor", true, 1, (byte)3, "", false, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{613, 636});
      template = createAchievement(639, "Hold a Sermon", true, 1, (byte)3, "", false, generatedTemplates, "");
      template.setAchievementsTriggered(new int[]{623, 632});

      for(AchievementTemplate t : generatedTemplates.values()) {
         t.setCreator("System");
         Achievement.addTemplate(t);
      }

      for(AchievementTemplate t : loadedTemplates.values()) {
         if (!generatedTemplates.containsKey(t.getNumber())) {
            Achievement.addTemplate(t);
         }
      }

      for(AchievementTemplate t : Achievement.getAllTemplates().values()) {
         for(int i : t.getAchievementsTriggered()) {
            Achievement.getTemplate(i).addTriggeredByAchievement(t.getNumber());
         }
      }
   }

   private static AchievementTemplate createAchievement(
      int number,
      String name,
      boolean isInvis,
      int triggerOn,
      byte type,
      String description,
      boolean isForCooking,
      ConcurrentHashMap<Integer, AchievementTemplate> templates,
      boolean playSoundOnUpdate,
      boolean isOneTimer
   ) {
      AchievementTemplate template = new AchievementTemplate(number, name, isInvis, triggerOn, type, playSoundOnUpdate, isOneTimer);
      template.setDescription(description);
      template.setIsForCooking(isForCooking);
      templates.put(number, template);
      return template;
   }

   private static AchievementTemplate createAchievement(
      int number,
      String name,
      boolean isInvis,
      int triggerOn,
      byte type,
      String description,
      boolean isForCooking,
      ConcurrentHashMap<Integer, AchievementTemplate> templates,
      boolean playSoundOnUpdate,
      boolean isOneTimer,
      String requirement
   ) {
      AchievementTemplate template = new AchievementTemplate(number, name, isInvis, triggerOn, type, playSoundOnUpdate, isOneTimer, requirement);
      template.setDescription(description);
      template.setIsForCooking(isForCooking);
      templates.put(number, template);
      return template;
   }

   private static AchievementTemplate createAchievement(
      int number,
      String name,
      boolean isInvis,
      int triggerOn,
      byte type,
      String description,
      boolean isForCooking,
      ConcurrentHashMap<Integer, AchievementTemplate> templates
   ) {
      AchievementTemplate template = new AchievementTemplate(number, name, isInvis, triggerOn, type, false, false);
      template.setDescription(description);
      template.setIsForCooking(isForCooking);
      templates.put(number, template);
      return template;
   }

   private static AchievementTemplate createAchievement(
      int number,
      String name,
      boolean isInvis,
      int triggerOn,
      byte type,
      String description,
      boolean isForCooking,
      ConcurrentHashMap<Integer, AchievementTemplate> templates,
      String requirement
   ) {
      AchievementTemplate template = new AchievementTemplate(number, name, isInvis, triggerOn, type, false, false, requirement);
      template.setDescription(description);
      template.setIsForCooking(isForCooking);
      templates.put(number, template);
      return template;
   }

   static void insertAchievementTemplate(AchievementTemplate achievement) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(INSERT);
         ps.setInt(1, achievement.getNumber());
         ps.setString(2, achievement.getName());
         ps.setInt(3, achievement.getTriggerOnCounter());
         ps.setString(4, achievement.getDescription());
         ps.setString(5, achievement.getCreator());
         ps.setByte(6, achievement.getType());
         ps.setBoolean(7, achievement.isPlaySoundOnUpdate());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save achievement " + achievement.getName() + " for " + achievement.getCreator(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      Achievement.addTemplate(achievement);
   }

   static void deleteAchievementTemplate(AchievementTemplate achievement) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM ACHIEVEMENTTEMPLATES WHERE NUMBER=?");
         ps.setInt(1, achievement.getNumber());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete achievement " + achievement.getName(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      Achievement.removeTemplate(achievement);
   }

   private static void loadAchievementTemplates() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         if (Constants.pruneDb) {
            logger.log(Level.INFO, "Loading achievement templates.");
         }

         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM ACHIEVEMENTTEMPLATES");
         rs = ps.executeQuery();

         while(rs.next()) {
            AchievementTemplate template = new AchievementTemplate(
               rs.getInt("NUMBER"),
               rs.getString("NAME"),
               false,
               rs.getInt("TRIGGERON"),
               rs.getString("DESCRIPTION"),
               rs.getString("CREATORNAME"),
               rs.getBoolean("PLAYUPDATE"),
               true
            );
            loadedTemplates.put(template.getNumber(), template);
         }
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         throw new IOException(var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }
}

package com.wurmonline.server.deities;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Deities implements MiscConstants, TimeConstants {
   private static final Map<Integer, Deity> deities = new HashMap<>();
   private static final String LOAD_DEITIES = "SELECT * FROM DEITIES";
   private static final String CALCULATE_FAITHS = "SELECT DEITY,FAITH, KINGDOM, PAYMENTEXPIRE FROM PLAYERS WHERE POWER=0 AND LASTLOGOUT>?";
   private static final Map<String, Integer> valreiStatuses = new HashMap<>();
   private static final Map<Integer, Integer> valreiPositions = new HashMap<>();
   private static final Map<Integer, String> valreiNames = new HashMap<>();
   private static Logger logger = Logger.getLogger(Deities.class.getName());
   public static final int DEITY_NONE = 0;
   public static final int DEITY_FO = 1;
   public static final int DEITY_MAGRANON = 2;
   public static final int DEITY_VYNORA = 3;
   public static final int DEITY_LIBILA = 4;
   public static final int DEITY_WURM = 5;
   public static final int DEITY_NOGUMP = 6;
   public static final int DEITY_WALNUT = 7;
   public static final int DEITY_PHARMAKOS = 8;
   public static final int DEITY_JACKAL = 9;
   public static final int DEITY_DEATHCRAWLER = 10;
   public static final int DEITY_SCAVENGER = 11;
   public static final int DEITY_GIANT = 12;
   public static final int DEITY_RESERVED = 100;
   public static final int DEITY_TOSIEK = 31;
   public static final int DEITY_NAHJO = 32;
   public static final int DEITY_NATHAN = 33;
   public static final int DEITY_PAAWEELR = 34;
   public static final int DEITY_SMEAGAIN = 35;
   public static final int DEITY_GARY = 36;
   public static int maxDeityNum = 100;
   public static final float THEFTMOD = -0.25F;
   public static float faithPlayers = 0.0F;
   public static final int FAVORNEEDEDFORRITUALS = 100000;

   private Deities() {
   }

   public static final boolean isOkOnFreedom(int deityNum, byte kingdomId) {
      if (kingdomId != 4) {
         return false;
      } else {
         return deityNum == 1 || deityNum == 3 || deityNum == 2;
      }
   }

   public static final byte getFavoredKingdom(int deityNum) {
      if (deityNum == 1 || deityNum == 3) {
         return 1;
      } else if (deityNum == 2) {
         return 2;
      } else if (deityNum == 4) {
         return 3;
      } else {
         Deity d = getDeity(deityNum);
         return d != null ? d.getFavoredKingdom() : 0;
      }
   }

   private static void rollBefriendPassive(Deity deity, Random rand) {
      if (deity.isHateGod()) {
         deity.setBefriendMonster(rand.nextInt(2) == 0);
      } else if (deity.isForestGod()) {
         deity.setBefriendCreature(rand.nextInt(2) == 0);
      } else {
         deity.setBefriendCreature(rand.nextInt(5) == 0);
         if (!deity.isBefriendCreature()) {
            deity.setBefriendMonster(rand.nextInt(10) == 0);
         }
      }
   }

   private static void rollWarriorPassive(Deity deity, Random rand) {
      if (deity.isMountainGod()) {
         deity.setWarrior(rand.nextInt(2) == 0);
      } else if (deity.isHateGod()) {
         deity.setDeathItemProtector(rand.nextInt(2) == 0);
      }

      if (!deity.isWarrior() && !deity.isDeathItemProtector()) {
         deity.setWarrior(rand.nextInt(4) == 0);
         if (!deity.isWarrior()) {
            deity.setDeathItemProtector(rand.nextInt(4) == 0);
            if (!deity.isDeathItemProtector()) {
               deity.setDeathProtector(rand.nextInt(3) == 0);
            }
         }

         if (!deity.isWarrior() && !deity.isDeathItemProtector() && !deity.isDeathProtector()) {
            deity.setDeathProtector(true);
         }
      }
   }

   private static void rollHealingLearningPassive(Deity deity, Random rand) {
      if (!deity.isWarrior() && !deity.isDeathItemProtector()) {
         if (!deity.isHateGod()) {
            if (deity.isForestGod()) {
               deity.setHealer(rand.nextInt(3) > 0);
            } else {
               deity.setHealer(rand.nextInt(3) == 0);
            }

            if (deity.isHealer()) {
               return;
            }
         }

         deity.setLearner(true);
      } else {
         deity.setLearner(rand.nextInt(3) == 0);
      }
   }

   private static void initializeDemigodPassives(Deity deity) {
      Random rand = deity.initializeAndGetRand();
      int template = 1 + rand.nextInt(4);
      if (deity.getNumber() == 31 || deity.getNumber() == 33 || deity.getNumber() == 36) {
         template = 4;
      }

      if (deity.getNumber() == 32) {
         template = 1;
      }

      if (deity.getNumber() == 34) {
         template = 3;
      }

      if (deity.getNumber() == 35) {
         template = 2;
      }

      deity.setTemplateDeity(template);
      if (template == 1) {
         deity.setForestGod(true);
         deity.setClothAffinity(true);
      } else if (template == 2) {
         deity.setMountainGod(true);
         deity.setMetalAffinity(true);
      } else if (template == 3) {
         deity.setWaterGod(true);
         deity.setClayAffinity(true);
      } else if (template == 4) {
         deity.setHateGod(true);
         deity.setMeatAffinity(true);
      }

      rollBefriendPassive(deity, rand);
      rollWarriorPassive(deity, rand);
      rollHealingLearningPassive(deity, rand);
      if (deity.isHateGod()) {
         deity.setAllowsButchering(true);
         deity.setFavorRegenerator(rand.nextInt(2) == 0);
      } else {
         deity.setAllowsButchering(rand.nextInt(5) == 0);
         deity.setFavorRegenerator(rand.nextInt(4) == 0);
      }

      if (deity.isForestGod()) {
         deity.setFoodAffinity(rand.nextInt(2) == 0);
         deity.setStaminaBonus(rand.nextInt(2) == 0);
         deity.setFoodBonus(rand.nextInt(2) == 0);
      } else {
         deity.setFoodAffinity(rand.nextInt(4) == 0);
         deity.setStaminaBonus(rand.nextInt(4) == 0);
         deity.setFoodBonus(rand.nextInt(4) == 0);
      }

      if (deity.isWaterGod()) {
         deity.setRoadProtector(rand.nextInt(2) == 0);
         deity.setItemProtector(rand.nextInt(2) == 0);
         deity.setRepairer(rand.nextInt(2) == 0);
         deity.setBuildWallBonus((float)rand.nextInt(10));
         deity.setWoodAffinity(rand.nextInt(2) == 0);
      } else {
         deity.setRoadProtector(rand.nextInt(4) == 0);
         deity.setItemProtector(rand.nextInt(4) == 0);
         deity.setRepairer(rand.nextInt(4) == 0);
         deity.setBuildWallBonus((float)(5 - rand.nextInt(10)));
         deity.setWoodAffinity(rand.nextInt(4) == 0);
      }

      if (deity.isHealer()) {
         deity.alignment = 100;
      } else if (deity.isHateGod()) {
         deity.alignment = -100;
      }

      if (deity.getNumber() == 35) {
         deity.setDeathItemProtector(true);
      }

      if (deity.getNumber() == 32) {
         deity.setMeatAffinity(true);
      }

      createHumanConvertStrings(deity);
   }

   private static void addDeity(Deity deity) {
      if (deity.number == 1) {
         deity.setTemplateDeity(1);
         deity.setForestGod(true);
         deity.setClothAffinity(true);
         deity.setFoodAffinity(true);
         deity.setBefriendCreature(true);
         deity.setStaminaBonus(true);
         deity.setFoodBonus(true);
         deity.setHealer(true);
         createFoConvertStrings(deity);
      } else if (deity.number == 2) {
         deity.setTemplateDeity(2);
         deity.setMountainGod(true);
         deity.setMetalAffinity(true);
         deity.setWarrior(true);
         deity.setDeathProtector(true);
         deity.setDeathItemProtector(true);
         createMagranonConvertStrings(deity);
      } else if (deity.number == 3) {
         deity.setTemplateDeity(3);
         deity.setWaterGod(true);
         deity.setClayAffinity(true);
         deity.setWoodAffinity(true);
         deity.setRoadProtector(true);
         deity.setItemProtector(true);
         deity.setRepairer(true);
         deity.setLearner(true);
         deity.setBuildWallBonus(20.0F);
         createVynoraConvertStrings(deity);
      } else if (deity.number == 4) {
         deity.setTemplateDeity(4);
         deity.setHateGod(true);
         deity.setMeatAffinity(true);
         deity.setFavorRegenerator(true);
         deity.setBefriendMonster(true);
         deity.setDeathProtector(true);
         deity.setAllowsButchering(true);
         createLibilaConvertStrings(deity);
      } else if (deity.isCustomDeity()) {
         initializeDemigodPassives(deity);
         createHumanConvertStrings(deity);
      } else {
         Random rand = deity.initializeAndGetRand();
         deity.setMeatAffinity(rand.nextInt(3) == 0);
         deity.setHateGod(rand.nextInt(3) == 0 || deity.alignment < 0);
         deity.setAllowsButchering(rand.nextInt(3) == 0 || deity.isHateGod());
         deity.setRoadProtector(rand.nextInt(3) == 0);
         deity.setItemProtector(rand.nextInt(3) == 0);
         deity.setWarrior(rand.nextInt(3) == 0);
         deity.setDeathProtector(rand.nextInt(3) == 0);
         deity.setDeathItemProtector(rand.nextInt(3) == 0);
         deity.setMetalAffinity(rand.nextInt(3) == 0);
         deity.setMountainGod(rand.nextInt(3) == 0 || deity.number == 32);
         deity.setRepairer(rand.nextInt(3) == 0);
         deity.setLearner(rand.nextInt(3) == 0);
         deity.setBuildWallBonus((float)rand.nextInt(10));
         deity.setWoodAffinity(rand.nextInt(3) == 0);
         deity.setWaterGod(rand.nextInt(3) == 0 || deity.number == 32);
         deity.setBefriendCreature(rand.nextInt(3) == 0);
         deity.setStaminaBonus(rand.nextInt(3) == 0);
         deity.setFoodBonus(rand.nextInt(3) == 0);
         if (!deity.isHateGod()) {
            deity.setHealer(rand.nextInt(3) == 0);
         }

         deity.setClayAffinity(rand.nextInt(3) == 0);
         deity.setClothAffinity(rand.nextInt(3) == 0);
         deity.setFoodAffinity(rand.nextInt(3) == 0);
         if (!deity.isHateGod() && !deity.isWaterGod() && !deity.isMountainGod()) {
            deity.setForestGod(rand.nextInt(8) < 2);
         }

         if (deity.isHealer()) {
            deity.alignment = 100;
         } else if (deity.isHateGod()) {
            deity.alignment = -100;
         }

         createHumanConvertStrings(deity);
      }

      deities.put(deity.number, deity);
   }

   static void removeDeity(int number) {
      deities.remove(number);
   }

   public static Deity getDeity(int number) {
      return deities.get(number);
   }

   private static void resetDeityFollowers() {
      for(Deity d : deities.values()) {
         d.setActiveFollowers(0);
      }
   }

   public static Deity[] getDeities() {
      return deities.values().toArray(new Deity[deities.size()]);
   }

   public static Map<Integer, String> getEntities() {
      return valreiNames;
   }

   public static final int getEntityNumber(String name) {
      for(Entry<Integer, String> entry : valreiNames.entrySet()) {
         if (entry.getValue().equalsIgnoreCase(name)) {
            return entry.getKey();
         }
      }

      return -1;
   }

   public static final Deity translateDeityForEntity(int entityNumber) {
      String entityName = valreiNames.get(entityNumber);
      if (entityName != null) {
         for(Deity d : deities.values()) {
            if (d.getName().equalsIgnoreCase(entityName)) {
               return d;
            }
         }
      }

      return null;
   }

   public static final int translateEntityForDeity(int deityNumber) {
      Deity d = getDeity(deityNumber);
      if (d != null) {
         for(Entry<Integer, String> entry : valreiNames.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(d.getName())) {
               return entry.getKey();
            }
         }
      }

      return -1;
   }

   public static final void addEntity(int number, String name) {
      valreiNames.put(number, name);
   }

   public static final boolean isNameOkay(String aName) {
      String lName = aName.toLowerCase();

      for(Deity d : deities.values()) {
         if (d.getNumber() < 100 && lName.equals(d.name.toLowerCase())) {
            return false;
         }
      }

      return !lName.equals("jackal")
         && !lName.equals("valrej")
         && !lName.equals("valrei")
         && !lName.equals("seris")
         && !lName.equals("sol")
         && !lName.equals("upkeep")
         && !lName.equals("system")
         && !lName.equals("village")
         && !lName.equals("team")
         && !lName.equals("local")
         && !lName.equals("combat")
         && !lName.equals("friends")
         && !lName.equals("nogump")
         && !lName.equals("uttacha")
         && !lName.equals("pharmakos")
         && !lName.equals("walnut");
   }

   public static final void calculateFaiths() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      resetDeityFollowers();

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT DEITY,FAITH, KINGDOM, PAYMENTEXPIRE FROM PLAYERS WHERE POWER=0 AND LASTLOGOUT>?");
         ps.setLong(1, System.currentTimeMillis() - 604800000L);
         rs = ps.executeQuery();
         faithPlayers = 0.0F;
         Map<Integer, Float> faithMap = new HashMap<>();
         Kingdoms.activePremiumHots = 0;
         Kingdoms.activePremiumJenn = 0;

         byte kdom;
         for(Kingdoms.activePremiumMolr = 0; rs.next(); Kingdoms.getKingdom(kdom).countedAtleastOnce = true) {
            byte d = rs.getByte("DEITY");
            if (d > 0) {
               float faith = rs.getFloat("FAITH");
               Float f = faithMap.get(Integer.valueOf(d));
               if (f == null) {
                  f = new Float(faith);
                  faithMap.put(Integer.valueOf(d), f);
               } else {
                  new Float(f + faith);
               }

               Deity deity = deities.get(Integer.valueOf(d));
               if (deity != null) {
                  deity.setActiveFollowers(deity.getActiveFollowers() + 1);
               }

               ++faithPlayers;
            }

            kdom = rs.getByte("KINGDOM");
            if (kdom == 1) {
               ++Kingdoms.activePremiumJenn;
            } else if (kdom == 3) {
               ++Kingdoms.activePremiumHots;
            } else if (kdom == 2) {
               ++Kingdoms.activePremiumMolr;
            }

            ++Kingdoms.getKingdom(kdom).activePremiums;
         }

         try {
            for(Entry<Integer, Float> me : faithMap.entrySet()) {
               Deity deity = getDeity(me.getKey());
               if (deity != null && me.getValue() > 0.0F) {
                  deity.setFaith((double)(me.getValue() / faithPlayers));
                  if (deity.getNumber() < 0 || deity.getNumber() > 4) {
                     deity.setMaxKingdom();
                  }
               } else if (deity != null) {
                  deity.setFaith(0.0);
               }
            }
         } catch (IOException var12) {
            logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      } finally {
         DbConnector.returnConnection(dbcon);
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      Kingdoms.checkIfDisbandKingdom();
   }

   public static boolean mayDestroyAltars() {
      return WurmCalendar.mayDestroyHugeAltars();
   }

   private static final void loadDeities() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM DEITIES");
         rs = ps.executeQuery();

         int found;
         for(found = 0; rs.next(); ++found) {
            int number = rs.getByte("ID");
            if (number > maxDeityNum) {
               maxDeityNum = number;
            }

            String name = rs.getString("NAME");
            byte sex = rs.getByte("SEX");
            double faith = rs.getDouble("FAITH");
            int favor = rs.getInt("FAVOR");
            byte alignment = rs.getByte("ALIGNMENT");
            byte power = rs.getByte("POWER");
            int holyitem = rs.getInt("HOLYITEM");
            float attack = rs.getFloat("ATTACK");
            float vitality = rs.getFloat("VITALITY");
            DbDeity deity = new DbDeity(number, name, alignment, sex, power, faith, holyitem, favor, attack, vitality, false);
            addDeity(deity);
            if (number == 1 || number == 3) {
               deity.setFavoredKingdom((byte)1);
            } else if (number == 2) {
               deity.setFavoredKingdom((byte)2);
            } else if (number == 4) {
               deity.setFavoredKingdom((byte)3);
            }
         }

         if (found == 0) {
            createBasicDeities();
         }
      } catch (SQLException var19) {
         logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
         throw new IOException(var19);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final int getNextDeityNum() {
      return ++maxDeityNum;
   }

   public static final Deity ascend(int newid, String name, long wurmid, byte sex, byte power, float attack, float vitality) {
      if (newid > maxDeityNum) {
         maxDeityNum = newid;
      }

      Random rand = new Random(wurmid);
      DbDeity deity = new DbDeity(newid, name, (byte)0, sex, power, 0.0, 505 + rand.nextInt(4), 0, attack, vitality, true);
      addDeity(deity);

      try {
         deity.save();
         return deity;
      } catch (IOException var12) {
         logger.log(Level.WARNING, "Failed to save " + deity.name, (Throwable)var12);
         return null;
      }
   }

   public static final Deity getRandomHateDeity() {
      boolean exists = false;

      for(Deity d : deities.values()) {
         if (d.isHateGod() && d.number != 4) {
            exists = true;
         }
      }

      if (exists) {
         LinkedList<Deity> toRet = new LinkedList<>();

         for(Deity d : deities.values()) {
            if (d.isHateGod() && d.number != 4) {
               toRet.add(d);
            }
         }

         return toRet.get(Server.rand.nextInt(toRet.size()));
      } else {
         return null;
      }
   }

   public static final Deity getRandomNonHateDeity() {
      boolean exists = false;

      for(Deity d : deities.values()) {
         if (!d.isHateGod() && d.number > 100) {
            exists = true;
         }
      }

      if (exists) {
         LinkedList<Deity> toRet = new LinkedList<>();

         for(Deity d : deities.values()) {
            if (!d.isHateGod() && d.number > 100) {
               toRet.add(d);
            }
         }

         return toRet.get(Server.rand.nextInt(toRet.size()));
      } else {
         return null;
      }
   }

   private static final void createBasicDeities() {
      DbDeity deity = new DbDeity(1, "Fo", (byte)100, (byte)0, (byte)5, 0.0, 505, 0, 7.0F, 5.0F, true);
      addDeity(deity);
      deity.setFavoredKingdom((byte)1);

      try {
         deity.save();
      } catch (IOException var5) {
         logger.log(Level.WARNING, "Failed to save " + deity.name, (Throwable)var5);
      }

      deity = new DbDeity(2, "Magranon", (byte)70, (byte)0, (byte)4, 0.0, 507, 0, 6.0F, 6.0F, true);
      addDeity(deity);
      deity.setFavoredKingdom((byte)2);

      try {
         deity.save();
      } catch (IOException var4) {
         logger.log(Level.WARNING, "Failed to save " + deity.name, (Throwable)var4);
      }

      deity = new DbDeity(3, "Vynora", (byte)70, (byte)1, (byte)4, 0.0, 508, 0, 5.0F, 7.0F, true);
      addDeity(deity);
      deity.setFavoredKingdom((byte)1);

      try {
         deity.save();
      } catch (IOException var3) {
         logger.log(Level.WARNING, "Failed to save " + deity.name, (Throwable)var3);
      }

      deity = new DbDeity(4, "Libila", (byte)-100, (byte)1, (byte)4, 0.0, 506, 0, 6.0F, 6.0F, true);
      addDeity(deity);
      deity.setFavoredKingdom((byte)3);

      try {
         deity.save();
      } catch (IOException var2) {
         logger.log(Level.WARNING, "Failed to save " + deity.name, (Throwable)var2);
      }
   }

   private static void createHumanConvertStrings(Deity deity) {
      String[] conv1 = new String[]{
         deity.name + " is here to grant strength and guidance.",
         "We are all seekers here in these foreign lands,",
         "We are all threatened by the ancient powers,",
         "And it is easy to stumble in this darkness never to rise again.",
         deity.getCapHeSheItString()
            + " will lead you through the marshes and caverns, and "
            + deity.getHeSheItString()
            + " will hold your hand when you falter.",
         deity.isHealer()
            ? "Follow " + deity.getHimHerItString() + " and " + deity.getHeSheItString() + " will keep you safe and healthy."
            : "Trust " + deity.getHimHerItString() + " in the darkness and sorrow.",
         deity.isHateGod() ? "Together we will crush our enemies and rule here in eternity." : "There is a blessed land we want to show you.",
         deity.isHateGod()
            ? "Listen carefully and you will hear the thunder of " + deity.getHisHerItsString() + " armies!"
            : "Join us in freedom and follow us in peaceful bliss!",
         "The path leads on to victory and a new dawn for humankind.",
         deity.isHateGod() ? "Will you join us or be crushed like a flea?" : "Will you join us?"
      };
      String[] alt1 = new String[]{
         deity.isHateGod() ? "Mortal," : "Dear friend,",
         "I have transcended to a higher state of being.",
         "I am now among the Immortals.",
         "I have become the epitome of " + (deity.isHateGod() ? "darkness." : (deity.isHealer() ? "love." : "light.")),
         "Trust me when I promise you a path to strength and glory.",
         deity.isHateGod() ? "I will lead the way and you will follow." : "I will walk beside you in eternal friendship.",
         deity.isHateGod()
            ? "Together we will slay our enemies in the sleep and tear their children apart."
            : "I will support you when you stagger, and keep your children safe.",
         "Nothing will stop us and one day we will meet on the Western Spurs and drink " + (deity.isHateGod() ? "our enemies' blood." : "honey and wine!"),
         "Let us grow together and conquer the forbidden lands where our souls rule in eternity!",
         "Are you ready to join us?"
      };
      deity.convertText1 = conv1;
      deity.altarConvertText1 = alt1;
   }

   private static void createFoConvertStrings(Deity fo) {
      String[] conv1 = new String[]{
         "Fo!",
         "His creations surround you. His love is everywhere around you.",
         "He is the father of all things. He created the world out of love and passion.",
         "To embrace Fo is to embrace all living things around you.",
         "All and everyone is equal, but different.",
         "We are all dirt. We are all gems. We just come in different shapes and colors.",
         "To create more and love all that is already created is to love Fo.",
         "To passionately strike down at those who aim to destroy these creations is to love Fo.",
         "To strive after beauty and harmony with nature is to love Fo.",
         "If you love Fo, Fo loves you!"
      };
      String[] alt1 = new String[]{
         "I am Fo.",
         "I am the Silence and the Trees. The sprout is my symbol.",
         "Silent and lonely I lingered in darkness.",
         "Look around you. I created this of love and loneliness.",
         "You are all like, but different.",
         "You are all dirt. You are all gems. You just come in different shapes and colors.",
         "Do my bidding. Let all things grow into the splendor they may possess.",
         "Strive after beauty and harmony with nature. Let your soul become a lustrous gem.",
         "With the same passion by which I once created all this, strike down at those who aim to destroy these creations.",
         "Love me. Let me love you."
      };
      fo.convertText1 = conv1;
      fo.altarConvertText1 = alt1;
   }

   private static void createMagranonConvertStrings(Deity magranon) {
      String[] conv1 = new String[]{
         "Is your goal in life to achieve riches? To achieve freedom?",
         "Who is stopping you? You are. Who will help you? Magranon will!",
         "We, the followers of Magranon will stand at the top of the world one day and sing!",
         "Together we will strive to rule the world. We will conquer all evil, build fantastic houses and live rich and glorious lives in them.",
         "What is knowledge for if you do not use it? What use is compassion if you are hungry?",
         "What are the alternatives?",
         "Say yes to yourself! Say 'I will!' Your world will change, and you will change the world!",
         "There are obstacles. People and forces will oppose us. Who will want to deny us all we strive for.",
         "That force must be utterly defeated! No victory will be possible unless we cleanse the world of that evil.",
         "Join our ranks. Help yourself reach the top!"
      };
      String[] alt1 = new String[]{
         "Listen to the words of Magranon:",
         "What are you? Could you not be more?",
         "I am the Fire and the Mountain. A sword is my symbol.",
         "I will help you rule the world. You will conquer all evil, and live a rich and glorious life.",
         "One day you will stand at the top of the world and sing!",
         "What is knowledge for if you do not use it? What use is compassion if you are hungry?",
         "Paths leading endlessly into the mist! What matters is power!",
         "First, power over self. Then, power over others.",
         "Say yes to yourself! Say 'I will!' Your world will change, and you will change the world!",
         "Let me help. Together, nothing can stop us!"
      };
      magranon.convertText1 = conv1;
      magranon.altarConvertText1 = alt1;
   }

   private static void createVynoraConvertStrings(Deity vynora) {
      String[] conv1 = new String[]{
         "What is this?",
         "Have you ever asked yourself that question?",
         "Vynora, our godess and guide, will help you seek the answer to that ancient riddle, just as we help her in her quest to know everything.",
         "Many secrets has she gathered, and we who call ourselves Seekers will be the first ones to learn.",
         "What is a Man? What is a Woman? What lies in darkness of the Void? Questions need answers!",
         "Seekers strive after excellence. We seek the truth in all things. We will go anywhere in our attempts to find it.",
         "True knowledge also brings us power. Power over self, but also power over others.",
         "Our gathered experience tells us that the best for all is to use that knowledge with care.",
         "Therefore most of us are peaceful and strive after a calm and orderly way to gather knowledge.",
         "Welcome to join the followers of Vynora!"
      };
      String[] alt1 = new String[]{
         "Seeker!",
         "I am the Water and the Wind. A bowl is my symbol.",
         "Have you ever asked yourself the Questions?",
         "I will help you find the answer to the Ancient Riddles, if you help me.",
         "My knowledge is vast, but I need to know the last parts! We all must know!",
         "What are you? What lingers in the darkness of the Void?",
         "Seek excellence. Seek the truth in all things. Go anywhere in your attempts to find it.",
         "True knowledge also brings you power.",
         "Exercise that power with care, or it will hurt you like the snake who bites its tail.",
         "Too many secrets are hidden by the other gods, and none will they reveal. This cannot be.",
         "Flow with me!"
      };
      vynora.convertText1 = conv1;
      vynora.altarConvertText1 = alt1;
   }

   private static void createLibilaConvertStrings(Deity libila) {
      String[] conv1 = new String[]{
         "Look at you. Pitiful creature.",
         "You seek the powers of the Whisperer? They may be available.",
         "Know that I personally will not help you much. But I am bound to tell you that she will.",
         "To tell the truth - She rewards me for recruiting you.",
         "People say much about us. They think we lie. And yes we do. We lie about a lot of things, but not about the truth.",
         "Truth is everything will end. Truth is some of us will be rewarded greatly by Her some day.",
         "Libila will not accept her having been betrayed by the other gods. Your contract with her is to help her, and she will help you.",
         "Her aim is to gain control here, and your goal is to stop the others from gaining it instead.",
         "You are expected and required to use all effective means available: Terror, deception, torture, death, sacrifices.",
         "Are you ready to join the Horde of the Summoned? Know that if you choose not to, you are against us!"
      };
      String[] alt1 = new String[]{
         "Look at you. Pitiful creature.",
         "You seek the powers of the Whisperer? They may be available.",
         "I am the Hate and the Deceit, but know that I will help you much.",
         "Know that I want revenge for the betrayal by the others. This is why the scythe is my symbol.",
         "Become my tool and my weapon. Let me sharpen you, and let me run you through the heart of my enemies.",
         "For this I will reward you greatly. You will be given powers beyond normal mortal possibilities.",
         "Exact my revenge anywhere, anytime and anyhow. Make it painful and frightening.",
         "Together, let us enter the Forbidden Lands. We are all in our right to do so!",
         "Let us grow together, and throw our enemies into the void!",
         "Are you ready to join the Horde of the Summoned? Know that if you choose not to, you are against me!"
      };
      libila.convertText1 = conv1;
      libila.altarConvertText1 = alt1;
   }

   public static boolean acceptsNewChampions(int deityNumber) {
      int nums = PlayerInfoFactory.getNumberOfChamps(deityNumber);
      if (deityNumber == 1) {
         nums += PlayerInfoFactory.getNumberOfChamps(3);
      }

      if (deityNumber == 3) {
         nums += PlayerInfoFactory.getNumberOfChamps(1);
      }

      if (deityNumber == 4) {
         return nums < 200;
      } else {
         return nums < 200;
      }
   }

   public static final void clearValreiPositions() {
      valreiPositions.clear();
   }

   public static final boolean hasValreiPositions() {
      return valreiPositions.size() > 0;
   }

   public static final void addPosition(int deityId, int hexPosition) {
      valreiPositions.put(deityId, hexPosition);
   }

   public static final Integer getPosition(int deityId) {
      return valreiPositions.get(deityId);
   }

   public static final void clearValreiStatuses() {
      valreiStatuses.clear();
   }

   public static final void addStatus(String status, int deityId) {
      valreiStatuses.put(status, deityId);
   }

   public static final String getEntityName(int deityId) {
      switch(deityId) {
         case 6:
            return "Nogump";
         case 7:
            return "Walnut";
         case 8:
            return "Pharmakos";
         case 9:
            return "Jackal";
         case 10:
            return "The Deathcrawler";
         case 11:
            return "The Scavenger";
         case 12:
            return "The Dirtmaw Giant";
         default:
            String n = valreiNames.get(deityId);
            if (n == null) {
               n = "";
            }

            return n;
      }
   }

   public static final String getDeityName(int deityId) {
      Deity d = getDeity(deityId);
      if (d != null) {
         return d.getName();
      } else {
         switch(deityId) {
            case 6:
               return "Nogump";
            case 7:
               return "Walnut";
            case 8:
               return "Pharmakos";
            case 9:
               return "Jackal";
            case 10:
               return "The Deathcrawler";
            case 11:
               return "The Scavenger";
            case 12:
               return "The Dirtmaw Giant";
            default:
               String n = valreiNames.get(deityId);
               if (n == null) {
                  n = "";
               }

               return n;
         }
      }
   }

   public static final String getRandomStatusFor(int deityId) {
      List<String> availStatuses = new ArrayList<>();

      for(Entry<String, Integer> status : valreiStatuses.entrySet()) {
         if (status.getValue() == deityId) {
            availStatuses.add(status.getKey());
         }
      }

      if (availStatuses.size() > 0) {
         int num = Server.rand.nextInt(availStatuses.size());
         return availStatuses.get(num);
      } else {
         return "";
      }
   }

   public static final boolean hasValreiStatuses() {
      return valreiStatuses.size() > 0;
   }

   public static final String getRandomStatus() {
      if (valreiStatuses.size() > 0) {
         int num = Server.rand.nextInt(valreiStatuses.size());
         int x = 0;

         for(Entry<String, Integer> status : valreiStatuses.entrySet()) {
            if (x >= num) {
               return status.getKey();
            }

            ++x;
         }
      }

      return "";
   }

   static {
      try {
         logger.log(Level.INFO, "Loading deities ");
         loadDeities();
      } catch (IOException var1) {
         logger.log(Level.WARNING, "Failed to load deities!", (Throwable)var1);
      }
   }
}

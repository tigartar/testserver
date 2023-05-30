package com.wurmonline.server.skills;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nonnull;

public final class SkillSystem implements TimeConstants {
   public static final Map<Integer, SkillTemplate> templates = new HashMap<>();
   public static final Map<Integer, String> skillNames = new HashMap<>();
   public static final Map<String, Integer> namesToSkill = new HashMap<>();
   private static final List<SkillTemplate> templateList = new ArrayList<>();
   private static final Random randomSource = new Random();
   public static final long SKILLGAIN_BASIC = 300000L;
   public static final long SKILLGAIN_CHARACTERISTIC = 200000L;
   public static final long SKILLGAIN_CHARACTERISTIC_BC = 150000L;
   public static final long SKILLGAIN_GROUP = 20000L;
   public static final long SKILLGAIN_FIGHTING = 4000L;
   public static final long SKILLGAIN_TOOL = 7000L;
   public static final long SKILLGAIN_NORMAL = 4000L;
   public static final long SKILLGAIN_FAST = 3000L;
   public static final long SKILLGAIN_RARE = 2000L;
   public static final long SKILLGAIN_FIGHTING_GROUP = 10000L;
   private static Integer[] skillnums = new Integer[0];
   private static final float priestSlowMod = 1.25F;
   private static final long STANDARD_DECAY = 1209600000L;

   private SkillSystem() {
   }

   public static String getNameFor(int skillNum) {
      String name = skillNames.get(skillNum);
      return name != null ? name : "unknown";
   }

   public static float getNextRandomFloat() {
      return randomSource.nextFloat();
   }

   @Nonnull
   public static int[] getDependenciesFor(int skillNum) {
      SkillTemplate template = templates.get(skillNum);

      assert template != null;

      return template.getDependencies();
   }

   public static float getDifficultyFor(int skillNum, boolean priest) {
      SkillTemplate template = templates.get(skillNum);
      return template.getType() == 0 && priest && template.isPriestSlowskillgain ? template.getDifficulty() * 1.25F : template.getDifficulty();
   }

   static long getDecayTimeFor(int skillNum) {
      SkillTemplate template = templates.get(skillNum);
      return template.getDecayTime();
   }

   public static short getTypeFor(int skillNum) {
      SkillTemplate template = templates.get(skillNum);
      return template.getType();
   }

   public static long getTickTimeFor(int skillNum) {
      SkillTemplate template = templates.get(skillNum);
      return template.getTickTime();
   }

   static boolean isFightingSkill(int skillNum) {
      SkillTemplate template = templates.get(skillNum);
      return template.fightSkill;
   }

   static boolean isThieverySkill(int skillNum) {
      SkillTemplate template = templates.get(skillNum);
      return template.thieverySkill;
   }

   private static void addSkillTemplate(SkillTemplate template) {
      templates.put(template.getNumber(), template);
      SkillStat.addSkill(template.getNumber(), template.name);
      skillNames.put(template.getNumber(), template.getName());
      namesToSkill.put(template.getName().toLowerCase(), template.getNumber());
      skillnums = templates.keySet().toArray(new Integer[templates.size()]);
      templateList.add(template);
   }

   static boolean ignoresEnemies(int skillNum) {
      SkillTemplate template = templates.get(skillNum);
      return template.ignoresEnemies;
   }

   public static int getRandomSkillNum() {
      return skillnums[Server.rand.nextInt(templates.size())];
   }

   public static SkillTemplate[] getAllSkillTemplates() {
      return templates.values().toArray(new SkillTemplate[templates.size()]);
   }

   public static int getSkillByName(String name) {
      Integer i = namesToSkill.get(name.toLowerCase());
      return i == null ? -1 : i;
   }

   public static SkillTemplate getSkillTemplateByIndex(int index) {
      return index > templateList.size() ? null : templateList.get(index);
   }

   public static int getNumberOfSkillTemplates() {
      return templateList.size();
   }

   static {
      addSkillTemplate(new SkillTemplate(2, "Mind", 300000.0F, MiscConstants.EMPTY_INT_ARRAY, 1209600000L, (short)1, false, true));
      addSkillTemplate(new SkillTemplate(1, "Body", 300000.0F, MiscConstants.EMPTY_INT_ARRAY, 1209600000L, (short)1, false, true));
      addSkillTemplate(new SkillTemplate(3, "Soul", 300000.0F, MiscConstants.EMPTY_INT_ARRAY, 1209600000L, (short)1, false, true));
      SkillTemplate bct = new SkillTemplate(104, "Body control", 150000.0F, new int[]{1}, 1209600000L, (short)0, false, true);
      addSkillTemplate(bct);
      SkillTemplate bst = new SkillTemplate(103, "Body stamina", 200000.0F, new int[]{1}, 1209600000L, (short)0, false, true);
      addSkillTemplate(bst);
      SkillTemplate bsr = new SkillTemplate(102, "Body strength", 200000.0F, new int[]{1}, 1209600000L, (short)0, false, true);
      addSkillTemplate(bsr);
      SkillTemplate mlg = new SkillTemplate(100, "Mind logic", 200000.0F, new int[]{2}, 1209600000L, (short)0, false, true);
      addSkillTemplate(mlg);
      SkillTemplate msp = new SkillTemplate(101, "Mind speed", 200000.0F, new int[]{2}, 1209600000L, (short)0, false, true);
      addSkillTemplate(msp);
      addSkillTemplate(new SkillTemplate(106, "Soul depth", 200000.0F, new int[]{3}, 1209600000L, (short)0, false, true));
      addSkillTemplate(new SkillTemplate(105, "Soul strength", 200000.0F, new int[]{3}, 1209600000L, (short)0, false, true));
      addSkillTemplate(new SkillTemplate(1000, "Swords", 10000.0F, new int[]{104}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1003, "Axes", 10000.0F, new int[]{102}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1001, "Knives", 10000.0F, new int[]{104}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1004, "Mauls", 10000.0F, new int[]{102}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1025, "Clubs", 10000.0F, new int[]{102}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1027, "Hammers", 10000.0F, new int[]{102}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1030, "Archery", 3000.0F, new int[]{104}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1033, "Polearms", 10000.0F, new int[]{102}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1012, "Tailoring", 20000.0F, new int[]{100, 104}, 1209600000L, (short)2));
      addSkillTemplate(new SkillTemplate(1018, "Cooking", 20000.0F, new int[]{100, 106}, 1209600000L, (short)2));
      addSkillTemplate(new SkillTemplate(1015, "Smithing", 20000.0F, new int[]{102, 104}, 1209600000L, (short)2));
      addSkillTemplate(
         new SkillTemplate(1016, "Weapon smithing", Servers.localServer.isChallengeServer() ? 4000.0F : 20000.0F, new int[]{1015}, 1209600000L, (short)2)
      );
      addSkillTemplate(new SkillTemplate(1017, "Armour smithing", 20000.0F, new int[]{1015}, 1209600000L, (short)2));
      addSkillTemplate(new SkillTemplate(1020, "Miscellaneous items", 20000.0F, new int[]{100}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(1002, "Shields", 4000.0F, new int[]{101, 104}, 1209600000L, (short)2, true, true));
      addSkillTemplate(new SkillTemplate(1021, "Alchemy", 20000.0F, new int[]{100}, 1209600000L, (short)2));
      addSkillTemplate(new SkillTemplate(1019, "Nature", 20000.0F, new int[]{106}, 1209600000L, (short)2));
      addSkillTemplate(new SkillTemplate(1022, "Toys", 20000.0F, MiscConstants.EMPTY_INT_ARRAY, 1209600000L, (short)2));
      addSkillTemplate(new SkillTemplate(1023, "Fighting", 20000.0F, new int[]{101, 104, 102}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(1024, "Healing", 20000.0F, new int[]{106, 100}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1026, "Religion", 20000.0F, new int[]{106, 105}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(1028, "Thievery", 4000.0F, new int[0], 1209600000L, (short)2, true, 0L));
      addSkillTemplate(new SkillTemplate(1029, "War machines", 10000.0F, new int[]{100}, 1209600000L, (short)2, false, true));
      addSkillTemplate(new SkillTemplate(10049, "Farming", 4000.0F, new int[]{1019, 102}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10091, "Papyrusmaking", 4000.0F, new int[]{1019, 102}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10092, "Thatching", 4000.0F, new int[]{104, 100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10045, "Gardening", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10086, "Meditating", 2000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10048, "Forestry", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10004, "Rake", 7000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10047, "Scythe", 7000.0F, new int[]{1020}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10046, "Sickle", 7000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10001, "Small Axe", 7000.0F, new int[]{1003}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(1008, "Mining", 8000.0F, new int[]{103, 102, 105}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1009, "Digging", 3000.0F, new int[]{103, 102}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10009, "Pickaxe", 7000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10002, "Shovel", 7000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1011, "Pottery", 4000.0F, new int[]{106, 100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1014, "Ropemaking", 4000.0F, new int[]{100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1007, "Woodcutting", 4000.0F, new int[]{103, 102}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10003, "Hatchet", 7000.0F, new int[]{1003}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10017, "Leatherworking", 4000.0F, new int[]{1012}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10016, "Cloth tailoring", 4000.0F, new int[]{1012}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1013, "Masonry", 4000.0F, new int[]{102, 100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10010, "Blades smithing", 4000.0F, new int[]{1016}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10011, "Weapon heads smithing", 4000.0F, new int[]{1016}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10012, "Chain armour smithing", 4000.0F, new int[]{1017}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10013, "Plate armour smithing", 4000.0F, new int[]{1017}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10014, "Shield smithing", 4000.0F, new int[]{1017}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10015, "Blacksmithing", 4000.0F, new int[]{1015}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10037, "Dairy food making", 4000.0F, new int[]{1018}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10038, "Hot food cooking", 4000.0F, new int[]{1018}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10039, "Baking", 2000.0F, new int[]{1018}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10083, "Beverages", 4000.0F, new int[]{1018}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10005, "Longsword", 4000.0F, new int[]{1000}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10061, "Large maul", 4000.0F, new int[]{1004}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10062, "Medium maul", 4000.0F, new int[]{1004}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10063, "Small maul", 4000.0F, new int[]{1004}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10070, "Warhammer", 4000.0F, new int[]{1027}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10088, "Long spear", 4000.0F, new int[]{1033}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10089, "Halberd", 4000.0F, new int[]{1033}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10090, "Staff", 4000.0F, new int[]{1033}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10007, "Carving knife", 4000.0F, new int[]{1001}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10029, "Butchering knife", 4000.0F, new int[]{1001}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10030, "Stone chisel", 4000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10064, "Huge club", 4000.0F, new int[]{1025}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10008, "Saw", 3000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10059, "Butchering", 4000.0F, new int[]{1018, 102}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1005, "Carpentry", 4000.0F, new int[]{104, 100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1010, "Firemaking", 4000.0F, new int[]{100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10018, "Tracking", 2000.0F, new int[]{100, 106}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10019, "Small wooden shield", 3000.0F, new int[]{1002}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10020, "Medium wooden shield", 3000.0F, new int[]{1002}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10021, "Large wooden shield", 3000.0F, new int[]{1002}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10022, "Small metal shield", 3000.0F, new int[]{1002}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10023, "Large metal shield", 3000.0F, new int[]{1002}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10006, "Medium metal shield", 3000.0F, new int[]{1002}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10024, "Large axe", 4000.0F, new int[]{1003}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10025, "Huge axe", 4000.0F, new int[]{1003}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10027, "Shortsword", 4000.0F, new int[]{1000}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10028, "Two handed sword", 4000.0F, new int[]{1000}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10026, "Hammer", 4000.0F, new int[]{1020}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10031, "Paving", 4000.0F, new int[]{102, 105}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10032, "Prospecting", 2000.0F, MiscConstants.EMPTY_INT_ARRAY, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10033, "Fishing", 3000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10034, "Locksmithing", 4000.0F, new int[]{1015}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10035, "Repairing", 4000.0F, new int[]{1020}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10036, "Coal-making", 2000.0F, new int[]{105, 100}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10040, "Milling", 2000.0F, new int[]{105, 103}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10041, "Metallurgy", 4000.0F, new int[]{1015}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10042, "Natural substances", 4000.0F, new int[]{1021}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10043, "Jewelry smithing", 4000.0F, new int[]{1015}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10044, "Fine carpentry", 4000.0F, new int[]{1005}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1031, "Bowyery", 4000.0F, new int[]{1005}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(1032, "Fletching", 4000.0F, new int[]{1005}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10050, "Yoyo", 7000.0F, new int[]{1022}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10087, "Puppeteering", 2000.0F, new int[]{1022}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10051, "Toy making", 4000.0F, new int[]{1005}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10052, "Weaponless fighting", 4000.0F, new int[]{1023}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10053, "Aggressive fighting", 4000.0F, new int[]{1023, 101}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10054, "Defensive fighting", 4000.0F, new int[]{1023, 101}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10055, "Normal fighting", 4000.0F, new int[]{1023, 101}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10056, "First aid", 4000.0F, new int[]{1024}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10057, "Taunting", 3000.0F, new int[]{1023}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10058, "Shield bashing", 3000.0F, new int[]{1023}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10060, "Milking", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10065, "Preaching", 2000.0F, new int[]{1026}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10066, "Prayer", 4000.0F, new int[]{1026}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10067, "Channeling", 4000.0F, new int[]{1026}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10068, "Exorcism", 2000.0F, new int[]{1026}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10069, "Archaeology", 4000.0F, new int[]{100, 104}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10071, "Foraging", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10072, "Botanizing", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10073, "Climbing", 4000.0F, new int[]{104, 102}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10074, "Stone cutting", 4000.0F, new int[]{1013}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10076, "Lock picking", 2000.0F, new int[]{1028, 104, 100}, 1209600000L, (short)4, true, 600000L));
      addSkillTemplate(new SkillTemplate(10075, "Stealing", 2000.0F, new int[]{1028}, 1209600000L, (short)4, true, 600000L));
      addSkillTemplate(new SkillTemplate(10084, "Traps", 4000.0F, new int[]{1028, 104, 100}, 1209600000L, (short)4, true, 0L));
      addSkillTemplate(new SkillTemplate(10077, "Catapults", 4000.0F, new int[]{1029}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10078, "Animal taming", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10085, "Animal husbandry", 4000.0F, new int[]{1019}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10079, "Short bow", 4000.0F, new int[]{1030}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10081, "Long bow", 4000.0F, new int[]{1030}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10080, "Medium bow", 4000.0F, new int[]{1030}, 1209600000L, (short)4, true, true));
      addSkillTemplate(new SkillTemplate(10082, "Ship building", 7000.0F, new int[]{1005}, 1209600000L, (short)4));
      addSkillTemplate(new SkillTemplate(10093, "Ballistae", 2000.0F, new int[]{1029}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10094, "Trebuchets", 2000.0F, new int[]{1029}, 1209600000L, (short)4, false, true));
      addSkillTemplate(new SkillTemplate(10095, "Restoration", 4000.0F, new int[]{10069}, 1209600000L, (short)4, false, true));
   }
}

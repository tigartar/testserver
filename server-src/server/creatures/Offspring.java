package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Offspring {
   private static final Logger logger = Logger.getLogger(Offspring.class.getName());
   private static final Map<Long, Offspring> offsprings = new ConcurrentHashMap<>();
   private byte deliveryDays = 0;
   private final long traits;
   private final long mother;
   private final long father;
   private boolean checked = false;
   private static final String LOAD_ALL_OFFSPRING = "SELECT * FROM OFFSPRING";
   private static final String DELETE_OFFSPRING = "DELETE FROM OFFSPRING WHERE MOTHERID=?";
   private static final String UPDATE_OFFSPRING_DAYS = "UPDATE OFFSPRING SET DELIVERYDAYS=? WHERE MOTHERID=?";
   private static final String CREATE_OFFSPRING = "INSERT INTO OFFSPRING (MOTHERID,FATHERID,TRAITS,DELIVERYDAYS) VALUES (?,?,?,?)";
   private static final String[] MALE_NAMES = createMaleNames();
   private static final String[] FEMALE_NAMES = createFemaleNames();
   private static final String[] GENERIC_NAMES = createGenericNames();
   private static final String[] MALE_UNI_NAMES = createMaleUnicornNames();
   private static final String[] FEMALE_UNI_NAMES = createFemaleUnicornNames();

   Offspring(long _motherId, long _fatherId, long _traits, byte _deliveryDays, boolean load) {
      this.father = _fatherId;
      this.mother = _motherId;
      this.traits = _traits;
      this.deliveryDays = _deliveryDays;
      if (!load) {
         this.create();
      }

      offsprings.put(this.mother, this);
   }

   private void create() {
      if (this.deliveryDays > 100) {
         this.deliveryDays = 100;
      } else if (this.deliveryDays < 1) {
         this.deliveryDays = 1;
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("INSERT INTO OFFSPRING (MOTHERID,FATHERID,TRAITS,DELIVERYDAYS) VALUES (?,?,?,?)");
         ps.setLong(1, this.mother);
         ps.setLong(2, this.father);
         ps.setLong(3, this.traits);
         ps.setByte(4, this.deliveryDays);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to create offspring for " + this.mother, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   boolean decreaseDaysLeft() {
      this.checked = true;
      if (this.deliveryDays-- > 0) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement("UPDATE OFFSPRING SET DELIVERYDAYS=? WHERE MOTHERID=?");
            ps.setByte(1, this.deliveryDays);
            ps.setLong(2, this.mother);
            ps.executeUpdate();
         } catch (SQLException var7) {
            logger.log(Level.WARNING, "Failed to update offspring for " + this.mother + ", days=" + this.deliveryDays, (Throwable)var7);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }

         return false;
      } else {
         offsprings.remove(this.mother);
         deleteSettings(this.mother);
         return true;
      }
   }

   static void deleteSettings(long motherid) {
      offsprings.remove(motherid);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("DELETE FROM OFFSPRING WHERE MOTHERID=?");
         ps.setLong(1, motherid);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to delete offspring for " + motherid, (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   static void loadAllOffspring() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM OFFSPRING");
         rs = ps.executeQuery();

         while(rs.next()) {
            new Offspring(rs.getLong("MOTHERID"), rs.getLong("FATHERID"), rs.getLong("TRAITS"), rs.getByte("DELIVERYDAYS"), true);
         }
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed loading all offspring " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   static Offspring getOffspring(long wurmid) {
      return offsprings.get(wurmid);
   }

   long getTraits() {
      return this.traits;
   }

   long getMother() {
      return this.mother;
   }

   long getFather() {
      return this.father;
   }

   public int getDaysLeft() {
      return this.deliveryDays;
   }

   public static void resetOffspringCounters() {
      ArrayList<Offspring> invalid = new ArrayList<>();

      for(Offspring lOffspring : offsprings.values()) {
         if (Creatures.getInstance().getCreatureOrNull(lOffspring.mother) == null) {
            invalid.add(lOffspring);
         }

         lOffspring.checked = false;
      }

      for(Offspring offspring : invalid) {
         logger.warning(
            "Deleting offspring data, mother not found. mother="
               + offspring.mother
               + ", father="
               + offspring.father
               + ", traits="
               + offspring.traits
               + ", deliveryDays="
               + offspring.deliveryDays
         );
         deleteSettings(offspring.mother);
      }
   }

   boolean isChecked() {
      return this.checked;
   }

   static String generateMaleName() {
      if (MALE_NAMES.length > 0) {
         int num = Server.rand.nextInt(100);
         if (num == 99) {
            return MALE_NAMES[Server.rand.nextInt(MALE_NAMES.length)];
         } else {
            return num < 50
               ? generateGenericPrefix() + MALE_NAMES[Server.rand.nextInt(MALE_NAMES.length)]
               : MALE_NAMES[Server.rand.nextInt(MALE_NAMES.length)] + generateGenericPrefix();
         }
      } else {
         return "";
      }
   }

   static String generateGenericPrefix() {
      return GENERIC_NAMES.length > 0 ? GENERIC_NAMES[Server.rand.nextInt(GENERIC_NAMES.length)] : "";
   }

   static String generateGenericName() {
      return GENERIC_NAMES.length > 0
         ? GENERIC_NAMES[Server.rand.nextInt(GENERIC_NAMES.length)] + GENERIC_NAMES[Server.rand.nextInt(GENERIC_NAMES.length)]
         : "";
   }

   static String generateFemaleName() {
      if (FEMALE_NAMES.length > 0) {
         int num = Server.rand.nextInt(100);
         if (num == 99) {
            return FEMALE_NAMES[Server.rand.nextInt(FEMALE_NAMES.length)];
         } else {
            return num < 50
               ? generateGenericPrefix() + FEMALE_NAMES[Server.rand.nextInt(FEMALE_NAMES.length)]
               : FEMALE_NAMES[Server.rand.nextInt(FEMALE_NAMES.length)] + generateGenericPrefix();
         }
      } else {
         return "";
      }
   }

   static String generateFemaleUnicornName() {
      if (FEMALE_UNI_NAMES.length > 0) {
         int num = Server.rand.nextInt(100);
         if (num == 99) {
            return FEMALE_UNI_NAMES[Server.rand.nextInt(FEMALE_UNI_NAMES.length)];
         } else {
            return num < 50
               ? generateGenericPrefix() + FEMALE_UNI_NAMES[Server.rand.nextInt(FEMALE_UNI_NAMES.length)]
               : FEMALE_UNI_NAMES[Server.rand.nextInt(FEMALE_UNI_NAMES.length)] + generateGenericPrefix();
         }
      } else {
         return "";
      }
   }

   static String generateMaleUnicornName() {
      if (MALE_UNI_NAMES.length > 0) {
         int num = Server.rand.nextInt(100);
         if (num == 99) {
            return MALE_UNI_NAMES[Server.rand.nextInt(MALE_UNI_NAMES.length)];
         } else {
            return num < 50
               ? generateGenericPrefix() + MALE_UNI_NAMES[Server.rand.nextInt(MALE_UNI_NAMES.length)]
               : MALE_UNI_NAMES[Server.rand.nextInt(MALE_UNI_NAMES.length)] + generateGenericPrefix();
         }
      } else {
         return "";
      }
   }

   public static String getRandomMaleName() {
      return MALE_NAMES[Server.rand.nextInt(MALE_NAMES.length)];
   }

   public static String getRandomFemaleName() {
      return FEMALE_NAMES[Server.rand.nextInt(FEMALE_NAMES.length)];
   }

   public static String getRandomGenericName() {
      return GENERIC_NAMES[Server.rand.nextInt(GENERIC_NAMES.length)];
   }

   static String[] createMaleNames() {
      List<String> malelist = new ArrayList<>(80);
      malelist.add("lars");
      malelist.add("bom");
      malelist.add("ally");
      malelist.add("dom");
      malelist.add("mack");
      malelist.add("hard");
      malelist.add("billy");
      malelist.add("flint");
      malelist.add("wart");
      malelist.add("stark");
      malelist.add("tom");
      malelist.add("master");
      malelist.add("prancer");
      malelist.add("bouncer");
      malelist.add("mark");
      malelist.add("rolf");
      malelist.add("notch");
      malelist.add("bear");
      malelist.add("minsc");
      malelist.add("abbas");
      malelist.add("ace");
      malelist.add("baron");
      malelist.add("duke");
      malelist.add("baxter");
      malelist.add("ben");
      malelist.add("benny");
      malelist.add("cesar");
      malelist.add("cactus");
      malelist.add("dale");
      malelist.add("damien");
      malelist.add("eagle");
      malelist.add("ears");
      malelist.add("echo");
      malelist.add("eben");
      malelist.add("eclipse");
      malelist.add("ed");
      malelist.add("faith");
      malelist.add("falcon");
      malelist.add("fancy");
      malelist.add("fantasy");
      malelist.add("gage");
      malelist.add("gallant");
      malelist.add("hal");
      malelist.add("ibn");
      malelist.add("ice");
      malelist.add("jack");
      malelist.add("kaden");
      malelist.add("kalil");
      malelist.add("lad");
      malelist.add("man");
      malelist.add("maestro");
      malelist.add("nada");
      malelist.add("nafa");
      malelist.add("ocho");
      malelist.add("oblivion");
      malelist.add("paddy");
      malelist.add("quail");
      malelist.add("raffle");
      malelist.add("rags");
      malelist.add("sage");
      malelist.add("tails");
      malelist.add("vigor");
      malelist.add("venture");
      malelist.add("waldo");
      malelist.add("walt");
      malelist.add("thunder");
      malelist.add("xo");
      malelist.add("yasin");
      malelist.add("cliff");
      malelist.add("hill");
      malelist.add("max");
      malelist.add("alex");
      malelist.add("erik");
      malelist.add("roman");
      malelist.add("johan");
      malelist.add("emil");
      malelist.add("uze");
      return malelist.toArray(new String[malelist.size()]);
   }

   static String[] createFemaleNames() {
      List<String> femalelist = new ArrayList<>(80);
      femalelist.add("molly");
      femalelist.add("tess");
      femalelist.add("lily");
      femalelist.add("anna");
      femalelist.add("bella");
      femalelist.add("ann");
      femalelist.add("pinkie");
      femalelist.add("belle");
      femalelist.add("adriana");
      femalelist.add("abia");
      femalelist.add("adara");
      femalelist.add("agnes");
      femalelist.add("aisha");
      femalelist.add("ballet");
      femalelist.add("babe");
      femalelist.add("bashira");
      femalelist.add("benita");
      femalelist.add("caine");
      femalelist.add("daisy");
      femalelist.add("dalia");
      femalelist.add("echo");
      femalelist.add("eben");
      femalelist.add("fabiola");
      femalelist.add("fancy");
      femalelist.add("fantasy");
      femalelist.add("gala");
      femalelist.add("halim");
      femalelist.add("hall");
      femalelist.add("ida");
      femalelist.add("jade");
      femalelist.add("kia");
      femalelist.add("kim");
      femalelist.add("kalil");
      femalelist.add("kalypso");
      femalelist.add("lace");
      femalelist.add("lady");
      femalelist.add("mac");
      femalelist.add("madia");
      femalelist.add("nafar");
      femalelist.add("nana");
      femalelist.add("nanook");
      femalelist.add("napa");
      femalelist.add("oak");
      femalelist.add("ocean");
      femalelist.add("paint");
      femalelist.add("queen");
      femalelist.add("sara");
      femalelist.add("sadie");
      femalelist.add("sage");
      femalelist.add("sahar");
      femalelist.add("taffy");
      femalelist.add("tahu");
      femalelist.add("tammy");
      femalelist.add("ula");
      femalelist.add("uma");
      femalelist.add("umbra");
      femalelist.add("unity");
      femalelist.add("vanessa");
      femalelist.add("vanilla");
      femalelist.add("rose");
      femalelist.add("xena");
      femalelist.add("li");
      femalelist.add("lei");
      femalelist.add("zoe");
      femalelist.add("zafir");
      femalelist.add("zara");
      femalelist.add("yazmeen");
      femalelist.add("yahya");
      femalelist.add("yoana");
      femalelist.add("cliff");
      femalelist.add("pifa");
      femalelist.add("tich");
      femalelist.add("panda");
      return femalelist.toArray(new String[femalelist.size()]);
   }

   static String[] createGenericNames() {
      List<String> genericlist = new ArrayList<>(59);
      genericlist.add("lightning");
      genericlist.add("flash");
      genericlist.add("flea");
      genericlist.add("osio");
      genericlist.add("stark");
      genericlist.add("strong");
      genericlist.add("fast");
      genericlist.add("hard");
      genericlist.add("brisk");
      genericlist.add("sweet");
      genericlist.add("jolly");
      genericlist.add("ecker");
      genericlist.add("golden");
      genericlist.add("silver");
      genericlist.add("pearl");
      genericlist.add("gold");
      genericlist.add("heart");
      genericlist.add("honey");
      genericlist.add("kiss");
      genericlist.add("dance");
      genericlist.add("swift");
      genericlist.add("hop");
      genericlist.add("pie");
      genericlist.add("iron");
      genericlist.add("copper");
      genericlist.add("grey");
      genericlist.add("blood");
      genericlist.add("north");
      genericlist.add("west");
      genericlist.add("wind");
      genericlist.add("east");
      genericlist.add("rain");
      genericlist.add("south");
      genericlist.add("cloud");
      genericlist.add("rock");
      genericlist.add("mountain");
      genericlist.add("happy");
      genericlist.add("halt");
      genericlist.add("sad");
      genericlist.add("tear");
      genericlist.add("clip");
      genericlist.add("dream");
      genericlist.add("chaser");
      genericlist.add("hunting");
      genericlist.add("pick");
      genericlist.add("dog");
      genericlist.add("call");
      genericlist.add("ebony");
      genericlist.add("rage");
      genericlist.add("raid");
      genericlist.add("wing");
      genericlist.add("warrior");
      genericlist.add("war");
      genericlist.add("walking");
      genericlist.add("run");
      genericlist.add("wild");
      genericlist.add("coffee");
      return genericlist.toArray(new String[genericlist.size()]);
   }

   private static String[] createMaleUnicornNames() {
      List<String> mul = new ArrayList<>();
      mul.add("Amor");
      mul.add("Amulius");
      mul.add("Aries");
      mul.add("Belial");
      mul.add("Cai");
      mul.add("Consus");
      mul.add("Cupid");
      mul.add("Faunus");
      mul.add("Gemini");
      mul.add("Caesar");
      mul.add("Iovis");
      mul.add("Italus");
      mul.add("Janus");
      mul.add("Ausimus");
      mul.add("Jupiter");
      mul.add("Kay");
      mul.add("Liber");
      mul.add("Mars");
      mul.add("Mercury");
      mul.add("Neptune");
      mul.add("Numitor");
      mul.add("Quirinus");
      mul.add("Remus");
      mul.add("Romulus");
      mul.add("Saturn");
      mul.add("Silvius");
      mul.add("Summanus");
      mul.add("Tatius");
      mul.add("Terminus");
      mul.add("Vulcan");
      mul.add("Saro");
      return mul.toArray(new String[mul.size()]);
   }

   private static String[] createFemaleUnicornNames() {
      List<String> ful = new ArrayList<>();
      ful.add("Amor");
      ful.add("Angerona");
      ful.add("Aurora");
      ful.add("Bellona");
      ful.add("Camilla");
      ful.add("Cardea");
      ful.add("Ceres");
      ful.add("Concordia");
      ful.add("Cora");
      ful.add("Diana");
      ful.add("Dido");
      ful.add("Elissa");
      ful.add("Fauna");
      ful.add("Corona");
      ful.add("Flora");
      ful.add("Hersilia");
      ful.add("Juno");
      ful.add("Juturna");
      ful.add("Juventas");
      ful.add("Laverna");
      ful.add("Lavinia");
      ful.add("Libitina");
      ful.add("Lucina");
      ful.add("Ausimus");
      ful.add("Luna");
      ful.add("Maia");
      ful.add("Minerva");
      ful.add("Naenia");
      ful.add("Nona");
      ful.add("Pax");
      ful.add("Pomona");
      ful.add("Leonia");
      ful.add("Salacia");
      ful.add("Silvia");
      ful.add("Venus");
      ful.add("Victoria");
      return ful.toArray(new String[ful.size()]);
   }
}

package com.wurmonline.server.kingdom;

import com.wurmonline.server.MiscConstants;
import java.util.HashMap;
import java.util.Map;

public final class Appointment implements MiscConstants {
   public static final byte TYPE_TITLE = 0;
   public static final byte TYPE_ORDER = 1;
   public static final byte TYPE_OFFICE = 2;
   private final String malename;
   private final String femalename;
   private final int id;
   private final byte type;
   private final byte kingdom;
   private final int level;
   private static final Map<Integer, Appointment> jennapps = new HashMap<>();
   private static final Map<Integer, Appointment> hotsapps = new HashMap<>();
   private static final Map<Integer, Appointment> molrapps = new HashMap<>();
   private static final Map<Integer, Appointment> nonerapps = new HashMap<>();

   private static void addAppointment(Appointment app) {
      Kingdom k = Kingdoms.getKingdom(app.kingdom);
      if (app.kingdom == 1 || k.getTemplate() == 1) {
         jennapps.put(app.id, app);
      } else if (app.kingdom == 3 || k.getTemplate() == 3) {
         hotsapps.put(app.id, app);
      } else if (app.kingdom != 2 && k.getTemplate() != 2) {
         nonerapps.put(app.id, app);
      } else {
         molrapps.put(app.id, app);
      }
   }

   public static final Appointment getAppointment(int id, byte kingdom) {
      Kingdom k = Kingdoms.getKingdom(kingdom);
      if (kingdom == 1 || k.getTemplate() == 1) {
         return jennapps.get(id);
      } else if (kingdom == 3 || k.getTemplate() == 3) {
         return hotsapps.get(id);
      } else {
         return kingdom != 2 && k.getTemplate() != 2 ? nonerapps.get(id) : molrapps.get(id);
      }
   }

   static void setAppointments(Appointments app) {
      app.addAppointment(getAppointment(0, app.kingdom));
      app.addAppointment(getAppointment(1, app.kingdom));
      app.addAppointment(getAppointment(2, app.kingdom));
      app.addAppointment(getAppointment(3, app.kingdom));
      app.addAppointment(getAppointment(4, app.kingdom));
      app.addAppointment(getAppointment(5, app.kingdom));
      app.addAppointment(getAppointment(6, app.kingdom));
      app.addAppointment(getAppointment(7, app.kingdom));
      app.addAppointment(getAppointment(8, app.kingdom));
      app.addAppointment(getAppointment(30, app.kingdom));
      app.addAppointment(getAppointment(31, app.kingdom));
      app.addAppointment(getAppointment(32, app.kingdom));
      app.addAppointment(getAppointment(33, app.kingdom));
      app.addAppointment(getAppointment(34, app.kingdom));
      app.addAppointment(getAppointment(35, app.kingdom));
      app.addAppointment(getAppointment(1500, app.kingdom));
      app.addAppointment(getAppointment(1501, app.kingdom));
      app.addAppointment(getAppointment(1502, app.kingdom));
      app.addAppointment(getAppointment(1503, app.kingdom));
      app.addAppointment(getAppointment(1504, app.kingdom));
      app.addAppointment(getAppointment(1505, app.kingdom));
      app.addAppointment(getAppointment(1506, app.kingdom));
      app.addAppointment(getAppointment(1507, app.kingdom));
      app.addAppointment(getAppointment(1508, app.kingdom));
      app.addAppointment(getAppointment(1509, app.kingdom));
      app.addAppointment(getAppointment(1510, app.kingdom));
   }

   private Appointment(String _malename, String _femalename, int _id, byte _kingdom, byte _type, int _level) {
      this.malename = _malename;
      this.femalename = _femalename;
      this.id = _id;
      this.kingdom = _kingdom;
      this.type = _type;
      this.level = _level;
      addAppointment(this);
   }

   public String getNameForGender(byte gender) {
      return gender == 1 ? this.femalename : this.malename;
   }

   public String getMaleName() {
      return this.malename;
   }

   public String getFemaleName() {
      return this.femalename;
   }

   public int getId() {
      return this.id;
   }

   int getLevel() {
      return this.level;
   }

   public byte getType() {
      return this.type;
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + this.id;
      return 31 * result + this.kingdom;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof Appointment)) {
         return false;
      } else {
         Appointment other = (Appointment)obj;
         if (this.id != other.id) {
            return false;
         } else {
            return this.kingdom == other.kingdom;
         }
      }
   }

   @Override
   public String toString() {
      StringBuilder lBuilder = new StringBuilder(150);
      lBuilder.append("Appointment [" + this.id);
      lBuilder.append(", Male name: ").append(this.malename);
      lBuilder.append(", Female name: ").append(this.femalename);
      lBuilder.append(", Kingdom: ").append(Kingdoms.getNameFor(this.kingdom));
      lBuilder.append(", Type: ").append(this.type);
      lBuilder.append(", Level: ").append(this.level);
      return lBuilder.toString();
   }

   static {
      new Appointment("Knight of the Land", "Knightess of the Land", 0, (byte)1, (byte)0, 1);
      new Appointment("Knight of the Sword", "Knightess of the Sword", 1, (byte)1, (byte)0, 2);
      new Appointment("Defender of the Crown", "Defendress of the Crown", 2, (byte)1, (byte)0, 3);
      new Appointment("Defender of the Heart", "Defendress of the Heart", 3, (byte)1, (byte)0, 4);
      new Appointment("Baron", "Baroness", 4, (byte)1, (byte)0, 5);
      new Appointment("Viscount", "Viscountess", 5, (byte)1, (byte)0, 6);
      new Appointment("Earl", "Countess", 6, (byte)1, (byte)0, 7);
      new Appointment("Marquess", "Marchioness", 7, (byte)1, (byte)0, 8);
      new Appointment("Duke", "Duchess", 8, (byte)1, (byte)0, 9);
      new Appointment("Blue Pearl", "Blue Pearl", 30, (byte)1, (byte)1, 1);
      new Appointment("White Pearl", "White Pearl", 31, (byte)1, (byte)1, 2);
      new Appointment("Golden Unicorn", "Golden Unicorn", 32, (byte)1, (byte)1, 3);
      new Appointment("Sad Goose", "Sad Goose", 33, (byte)1, (byte)1, 4);
      new Appointment("Shining Knight", "Shining Knight", 34, (byte)1, (byte)1, 5);
      new Appointment("Northern Star", "Northern Star", 35, (byte)1, (byte)1, 6);
      new Appointment("Information minister", "Information minister", 1500, (byte)1, (byte)2, 10);
      new Appointment("Court magus", "Court magus", 1501, (byte)1, (byte)2, 10);
      new Appointment("Earl Marshal", "Earl Marshal", 1502, (byte)1, (byte)2, 10);
      new Appointment("Court smith", "Court smith", 1503, (byte)1, (byte)2, 10);
      new Appointment("Court jester", "Court jester", 1504, (byte)1, (byte)2, 10);
      new Appointment("Chief of economy", "Chief of economy", 1505, (byte)1, (byte)2, 10);
      new Appointment("Royal priest", "Royal priest", 1506, (byte)1, (byte)2, 10);
      new Appointment("Royal lover", "Royal mistress", 1507, (byte)1, (byte)2, 10);
      new Appointment("Royal avenger", "Royal avenger", 1508, (byte)1, (byte)2, 10);
      new Appointment("Royal cook", "Royal cook", 1509, (byte)1, (byte)2, 10);
      new Appointment("Royal herald", "Royal herald", 1510, (byte)1, (byte)2, 10);
      new Appointment("Hoardmaster", "Hoardmaster", 0, (byte)3, (byte)0, 1);
      new Appointment("Fire of the cabal", "Fire of the cabal", 1, (byte)3, (byte)0, 2);
      new Appointment("Forcifunghi", "Forcafungha", 2, (byte)3, (byte)0, 3);
      new Appointment("Plagia", "Plagia", 3, (byte)3, (byte)0, 4);
      new Appointment("Nonsentie", "Nonsentiess", 4, (byte)3, (byte)0, 5);
      new Appointment("Ignora", "Ignoress", 5, (byte)3, (byte)0, 6);
      new Appointment("Immateria", "Immateriess", 6, (byte)3, (byte)0, 7);
      new Appointment("Submissant", "Submissa", 7, (byte)3, (byte)0, 8);
      new Appointment("Nonexist", "Nonexista", 8, (byte)3, (byte)0, 9);
      new Appointment("Steel Crucifix", "Steel Crucifix", 30, (byte)3, (byte)1, 1);
      new Appointment("Yarn Fungus", "Yarn Fungus", 31, (byte)3, (byte)1, 2);
      new Appointment("Pointless Nudge", "Pointless Nudge", 32, (byte)3, (byte)1, 3);
      new Appointment("Unholy Matramonic Insignia", "Unholy Matramonic Insignia", 33, (byte)3, (byte)1, 4);
      new Appointment("Silver Eyeball", "Silver Eyeball", 34, (byte)3, (byte)1, 5);
      new Appointment("Weird yellow stick", "Weird yellow stick", 35, (byte)3, (byte)1, 6);
      new Appointment("Bloodwhisperer", "Bloodwhisperer", 1500, (byte)3, (byte)2, 10);
      new Appointment("Shaman", "Shamaness", 1501, (byte)3, (byte)2, 10);
      new Appointment("Chief of the Cabal", "Chieftain of the Cabal", 1502, (byte)3, (byte)2, 10);
      new Appointment("Painwringer", "Painwringer", 1503, (byte)3, (byte)2, 10);
      new Appointment("Dancer", "Dancer", 1504, (byte)3, (byte)2, 10);
      new Appointment("Main thug", "Main thug", 1505, (byte)3, (byte)2, 10);
      new Appointment("Seer", "Seer", 1506, (byte)3, (byte)2, 10);
      new Appointment("Lover", "Mistress", 1507, (byte)3, (byte)2, 10);
      new Appointment("Assassin", "Nightingale", 1508, (byte)3, (byte)2, 10);
      new Appointment("Party fixer", "Party hostess", 1509, (byte)3, (byte)2, 10);
      new Appointment("Harbinger", "Harbinger", 1510, (byte)3, (byte)2, 10);
      new Appointment("Punisher", "Punisher", 0, (byte)2, (byte)0, 1);
      new Appointment("Dreadnaught", "Dreadnaught", 1, (byte)2, (byte)0, 2);
      new Appointment("Firestarter", "Firestarter", 2, (byte)2, (byte)0, 3);
      new Appointment("Firetyrant", "Firetyrant", 3, (byte)2, (byte)0, 4);
      new Appointment("Vassal", "Vassal", 4, (byte)2, (byte)0, 5);
      new Appointment("Blood Roy", "Blood Regin", 5, (byte)2, (byte)0, 6);
      new Appointment("Graf", "Grevin", 6, (byte)2, (byte)0, 7);
      new Appointment("Merchant king", "Merchant Queen", 7, (byte)2, (byte)0, 8);
      new Appointment("Grand Duke", "Grand Duchess", 8, (byte)2, (byte)0, 9);
      new Appointment("Concord of Blood", "Concord of Blood", 30, (byte)2, (byte)1, 1);
      new Appointment("Western Flame", "Eastern Flame", 31, (byte)2, (byte)1, 2);
      new Appointment("Papalegba", "Mamalegba", 32, (byte)2, (byte)1, 3);
      new Appointment("Holy Diver", "Holy Diver", 33, (byte)2, (byte)1, 4);
      new Appointment("Blood Crest", "Blood Crest", 34, (byte)2, (byte)1, 5);
      new Appointment("Silver Ribbon in Yellow and Red", "Silver Ribbon in Yellow and Red", 35, (byte)2, (byte)1, 6);
      new Appointment("Head of the secret police", "Head of the secret police", 1500, (byte)2, (byte)2, 10);
      new Appointment("Court magus", "Court magus", 1501, (byte)2, (byte)2, 10);
      new Appointment("Defense advisor", "Defense advisor", 1502, (byte)2, (byte)2, 10);
      new Appointment("Court smith", "Court smith", 1503, (byte)2, (byte)2, 10);
      new Appointment("Court Harlequin", "Court Harlequin", 1504, (byte)2, (byte)2, 10);
      new Appointment("Economic advisor", "Economic advisor", 1505, (byte)2, (byte)2, 10);
      new Appointment("Religious advisor", "Religious advisor", 1506, (byte)2, (byte)2, 10);
      new Appointment("Betrothed", "Betrothed", 1507, (byte)2, (byte)2, 10);
      new Appointment("Executioner", "Executioner", 1508, (byte)2, (byte)2, 10);
      new Appointment("Court chef", "Court chef", 1509, (byte)2, (byte)2, 10);
      new Appointment("Court Announcer", "Court Announcer", 1510, (byte)2, (byte)2, 10);
      new Appointment("Knight of some Land", "Knightess of some Land", 0, (byte)0, (byte)0, 1);
      new Appointment("Knight of a Sword", "Knightess of a Sword", 1, (byte)0, (byte)0, 2);
      new Appointment("Defender of a Crown", "Defendress of a Crown", 2, (byte)0, (byte)0, 3);
      new Appointment("Defender of the Hat", "Defendress of the Hat", 3, (byte)0, (byte)0, 4);
      new Appointment("Cent", "Cent", 4, (byte)0, (byte)0, 5);
      new Appointment("Shilling", "Shilling", 5, (byte)0, (byte)0, 6);
      new Appointment("Tenpiece", "Tenpiece", 6, (byte)0, (byte)0, 7);
      new Appointment("Quarter", "Quarter", 7, (byte)0, (byte)0, 8);
      new Appointment("Carrot", "Carrot", 8, (byte)0, (byte)0, 9);
      new Appointment("Faded Pearl", "Faded Pearl", 30, (byte)0, (byte)1, 1);
      new Appointment("Yellow Pearl", "Yellow Pearl", 31, (byte)0, (byte)1, 2);
      new Appointment("Silver Unicorn", "Silver Unicorn", 32, (byte)0, (byte)1, 3);
      new Appointment("Jolly Goose", "Jolly Goose", 33, (byte)0, (byte)1, 4);
      new Appointment("Lost Knight", "Lost Knight", 34, (byte)0, (byte)1, 5);
      new Appointment("Fading Star", "Fading Star", 35, (byte)0, (byte)1, 6);
      new Appointment("Disinformation Minister", "Disinformation Minister", 1500, (byte)0, (byte)2, 10);
      new Appointment("Court Trixter", "Court Trixter", 1501, (byte)0, (byte)2, 10);
      new Appointment("Earl Warmonger", "Earl Warmonger", 1502, (byte)0, (byte)2, 10);
      new Appointment("Court Rustfriend", "Court Rustfriend", 1503, (byte)0, (byte)2, 10);
      new Appointment("Royal Jest", "Royal Jest", 1504, (byte)0, (byte)2, 10);
      new Appointment("Economic catastrophe", "Economic catastrophe", 1505, (byte)0, (byte)2, 10);
      new Appointment("Abbot", "Abbot", 1506, (byte)0, (byte)2, 10);
      new Appointment("Royal Page", "Royal Page", 1507, (byte)0, (byte)2, 10);
      new Appointment("Royal Bully", "Royal Bully", 1508, (byte)0, (byte)2, 10);
      new Appointment("Royal Poisoner", "Royal Poisoner", 1509, (byte)0, (byte)2, 10);
      new Appointment("Royal Bragger", "Royal Bragger", 1510, (byte)0, (byte)2, 10);
   }
}

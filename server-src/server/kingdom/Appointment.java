/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.kingdom;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import java.util.HashMap;
import java.util.Map;

public final class Appointment
implements MiscConstants {
    public static final byte TYPE_TITLE = 0;
    public static final byte TYPE_ORDER = 1;
    public static final byte TYPE_OFFICE = 2;
    private final String malename;
    private final String femalename;
    private final int id;
    private final byte type;
    private final byte kingdom;
    private final int level;
    private static final Map<Integer, Appointment> jennapps = new HashMap<Integer, Appointment>();
    private static final Map<Integer, Appointment> hotsapps = new HashMap<Integer, Appointment>();
    private static final Map<Integer, Appointment> molrapps = new HashMap<Integer, Appointment>();
    private static final Map<Integer, Appointment> nonerapps = new HashMap<Integer, Appointment>();

    private static void addAppointment(Appointment app) {
        Kingdom k = Kingdoms.getKingdom(app.kingdom);
        if (app.kingdom == 1 || k.getTemplate() == 1) {
            jennapps.put(app.id, app);
        } else if (app.kingdom == 3 || k.getTemplate() == 3) {
            hotsapps.put(app.id, app);
        } else if (app.kingdom == 2 || k.getTemplate() == 2) {
            molrapps.put(app.id, app);
        } else {
            nonerapps.put(app.id, app);
        }
    }

    public static final Appointment getAppointment(int id, byte kingdom) {
        Kingdom k = Kingdoms.getKingdom(kingdom);
        if (kingdom == 1 || k.getTemplate() == 1) {
            return jennapps.get(id);
        }
        if (kingdom == 3 || k.getTemplate() == 3) {
            return hotsapps.get(id);
        }
        if (kingdom == 2 || k.getTemplate() == 2) {
            return molrapps.get(id);
        }
        return nonerapps.get(id);
    }

    static void setAppointments(Appointments app) {
        app.addAppointment(Appointment.getAppointment(0, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1, app.kingdom));
        app.addAppointment(Appointment.getAppointment(2, app.kingdom));
        app.addAppointment(Appointment.getAppointment(3, app.kingdom));
        app.addAppointment(Appointment.getAppointment(4, app.kingdom));
        app.addAppointment(Appointment.getAppointment(5, app.kingdom));
        app.addAppointment(Appointment.getAppointment(6, app.kingdom));
        app.addAppointment(Appointment.getAppointment(7, app.kingdom));
        app.addAppointment(Appointment.getAppointment(8, app.kingdom));
        app.addAppointment(Appointment.getAppointment(30, app.kingdom));
        app.addAppointment(Appointment.getAppointment(31, app.kingdom));
        app.addAppointment(Appointment.getAppointment(32, app.kingdom));
        app.addAppointment(Appointment.getAppointment(33, app.kingdom));
        app.addAppointment(Appointment.getAppointment(34, app.kingdom));
        app.addAppointment(Appointment.getAppointment(35, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1500, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1501, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1502, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1503, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1504, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1505, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1506, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1507, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1508, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1509, app.kingdom));
        app.addAppointment(Appointment.getAppointment(1510, app.kingdom));
    }

    private Appointment(String _malename, String _femalename, int _id, byte _kingdom, byte _type, int _level) {
        this.malename = _malename;
        this.femalename = _femalename;
        this.id = _id;
        this.kingdom = _kingdom;
        this.type = _type;
        this.level = _level;
        Appointment.addAppointment(this);
    }

    public String getNameForGender(byte gender) {
        if (gender == 1) {
            return this.femalename;
        }
        return this.malename;
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

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.id;
        result = 31 * result + this.kingdom;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Appointment)) {
            return false;
        }
        Appointment other = (Appointment)obj;
        if (this.id != other.id) {
            return false;
        }
        return this.kingdom == other.kingdom;
    }

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
        new Appointment("Knight of the Land", "Knightess of the Land", 0, 1, 0, 1);
        new Appointment("Knight of the Sword", "Knightess of the Sword", 1, 1, 0, 2);
        new Appointment("Defender of the Crown", "Defendress of the Crown", 2, 1, 0, 3);
        new Appointment("Defender of the Heart", "Defendress of the Heart", 3, 1, 0, 4);
        new Appointment("Baron", "Baroness", 4, 1, 0, 5);
        new Appointment("Viscount", "Viscountess", 5, 1, 0, 6);
        new Appointment("Earl", "Countess", 6, 1, 0, 7);
        new Appointment("Marquess", "Marchioness", 7, 1, 0, 8);
        new Appointment("Duke", "Duchess", 8, 1, 0, 9);
        new Appointment("Blue Pearl", "Blue Pearl", 30, 1, 1, 1);
        new Appointment("White Pearl", "White Pearl", 31, 1, 1, 2);
        new Appointment("Golden Unicorn", "Golden Unicorn", 32, 1, 1, 3);
        new Appointment("Sad Goose", "Sad Goose", 33, 1, 1, 4);
        new Appointment("Shining Knight", "Shining Knight", 34, 1, 1, 5);
        new Appointment("Northern Star", "Northern Star", 35, 1, 1, 6);
        new Appointment("Information minister", "Information minister", 1500, 1, 2, 10);
        new Appointment("Court magus", "Court magus", 1501, 1, 2, 10);
        new Appointment("Earl Marshal", "Earl Marshal", 1502, 1, 2, 10);
        new Appointment("Court smith", "Court smith", 1503, 1, 2, 10);
        new Appointment("Court jester", "Court jester", 1504, 1, 2, 10);
        new Appointment("Chief of economy", "Chief of economy", 1505, 1, 2, 10);
        new Appointment("Royal priest", "Royal priest", 1506, 1, 2, 10);
        new Appointment("Royal lover", "Royal mistress", 1507, 1, 2, 10);
        new Appointment("Royal avenger", "Royal avenger", 1508, 1, 2, 10);
        new Appointment("Royal cook", "Royal cook", 1509, 1, 2, 10);
        new Appointment("Royal herald", "Royal herald", 1510, 1, 2, 10);
        new Appointment("Hoardmaster", "Hoardmaster", 0, 3, 0, 1);
        new Appointment("Fire of the cabal", "Fire of the cabal", 1, 3, 0, 2);
        new Appointment("Forcifunghi", "Forcafungha", 2, 3, 0, 3);
        new Appointment("Plagia", "Plagia", 3, 3, 0, 4);
        new Appointment("Nonsentie", "Nonsentiess", 4, 3, 0, 5);
        new Appointment("Ignora", "Ignoress", 5, 3, 0, 6);
        new Appointment("Immateria", "Immateriess", 6, 3, 0, 7);
        new Appointment("Submissant", "Submissa", 7, 3, 0, 8);
        new Appointment("Nonexist", "Nonexista", 8, 3, 0, 9);
        new Appointment("Steel Crucifix", "Steel Crucifix", 30, 3, 1, 1);
        new Appointment("Yarn Fungus", "Yarn Fungus", 31, 3, 1, 2);
        new Appointment("Pointless Nudge", "Pointless Nudge", 32, 3, 1, 3);
        new Appointment("Unholy Matramonic Insignia", "Unholy Matramonic Insignia", 33, 3, 1, 4);
        new Appointment("Silver Eyeball", "Silver Eyeball", 34, 3, 1, 5);
        new Appointment("Weird yellow stick", "Weird yellow stick", 35, 3, 1, 6);
        new Appointment("Bloodwhisperer", "Bloodwhisperer", 1500, 3, 2, 10);
        new Appointment("Shaman", "Shamaness", 1501, 3, 2, 10);
        new Appointment("Chief of the Cabal", "Chieftain of the Cabal", 1502, 3, 2, 10);
        new Appointment("Painwringer", "Painwringer", 1503, 3, 2, 10);
        new Appointment("Dancer", "Dancer", 1504, 3, 2, 10);
        new Appointment("Main thug", "Main thug", 1505, 3, 2, 10);
        new Appointment("Seer", "Seer", 1506, 3, 2, 10);
        new Appointment("Lover", "Mistress", 1507, 3, 2, 10);
        new Appointment("Assassin", "Nightingale", 1508, 3, 2, 10);
        new Appointment("Party fixer", "Party hostess", 1509, 3, 2, 10);
        new Appointment("Harbinger", "Harbinger", 1510, 3, 2, 10);
        new Appointment("Punisher", "Punisher", 0, 2, 0, 1);
        new Appointment("Dreadnaught", "Dreadnaught", 1, 2, 0, 2);
        new Appointment("Firestarter", "Firestarter", 2, 2, 0, 3);
        new Appointment("Firetyrant", "Firetyrant", 3, 2, 0, 4);
        new Appointment("Vassal", "Vassal", 4, 2, 0, 5);
        new Appointment("Blood Roy", "Blood Regin", 5, 2, 0, 6);
        new Appointment("Graf", "Grevin", 6, 2, 0, 7);
        new Appointment("Merchant king", "Merchant Queen", 7, 2, 0, 8);
        new Appointment("Grand Duke", "Grand Duchess", 8, 2, 0, 9);
        new Appointment("Concord of Blood", "Concord of Blood", 30, 2, 1, 1);
        new Appointment("Western Flame", "Eastern Flame", 31, 2, 1, 2);
        new Appointment("Papalegba", "Mamalegba", 32, 2, 1, 3);
        new Appointment("Holy Diver", "Holy Diver", 33, 2, 1, 4);
        new Appointment("Blood Crest", "Blood Crest", 34, 2, 1, 5);
        new Appointment("Silver Ribbon in Yellow and Red", "Silver Ribbon in Yellow and Red", 35, 2, 1, 6);
        new Appointment("Head of the secret police", "Head of the secret police", 1500, 2, 2, 10);
        new Appointment("Court magus", "Court magus", 1501, 2, 2, 10);
        new Appointment("Defense advisor", "Defense advisor", 1502, 2, 2, 10);
        new Appointment("Court smith", "Court smith", 1503, 2, 2, 10);
        new Appointment("Court Harlequin", "Court Harlequin", 1504, 2, 2, 10);
        new Appointment("Economic advisor", "Economic advisor", 1505, 2, 2, 10);
        new Appointment("Religious advisor", "Religious advisor", 1506, 2, 2, 10);
        new Appointment("Betrothed", "Betrothed", 1507, 2, 2, 10);
        new Appointment("Executioner", "Executioner", 1508, 2, 2, 10);
        new Appointment("Court chef", "Court chef", 1509, 2, 2, 10);
        new Appointment("Court Announcer", "Court Announcer", 1510, 2, 2, 10);
        new Appointment("Knight of some Land", "Knightess of some Land", 0, 0, 0, 1);
        new Appointment("Knight of a Sword", "Knightess of a Sword", 1, 0, 0, 2);
        new Appointment("Defender of a Crown", "Defendress of a Crown", 2, 0, 0, 3);
        new Appointment("Defender of the Hat", "Defendress of the Hat", 3, 0, 0, 4);
        new Appointment("Cent", "Cent", 4, 0, 0, 5);
        new Appointment("Shilling", "Shilling", 5, 0, 0, 6);
        new Appointment("Tenpiece", "Tenpiece", 6, 0, 0, 7);
        new Appointment("Quarter", "Quarter", 7, 0, 0, 8);
        new Appointment("Carrot", "Carrot", 8, 0, 0, 9);
        new Appointment("Faded Pearl", "Faded Pearl", 30, 0, 1, 1);
        new Appointment("Yellow Pearl", "Yellow Pearl", 31, 0, 1, 2);
        new Appointment("Silver Unicorn", "Silver Unicorn", 32, 0, 1, 3);
        new Appointment("Jolly Goose", "Jolly Goose", 33, 0, 1, 4);
        new Appointment("Lost Knight", "Lost Knight", 34, 0, 1, 5);
        new Appointment("Fading Star", "Fading Star", 35, 0, 1, 6);
        new Appointment("Disinformation Minister", "Disinformation Minister", 1500, 0, 2, 10);
        new Appointment("Court Trixter", "Court Trixter", 1501, 0, 2, 10);
        new Appointment("Earl Warmonger", "Earl Warmonger", 1502, 0, 2, 10);
        new Appointment("Court Rustfriend", "Court Rustfriend", 1503, 0, 2, 10);
        new Appointment("Royal Jest", "Royal Jest", 1504, 0, 2, 10);
        new Appointment("Economic catastrophe", "Economic catastrophe", 1505, 0, 2, 10);
        new Appointment("Abbot", "Abbot", 1506, 0, 2, 10);
        new Appointment("Royal Page", "Royal Page", 1507, 0, 2, 10);
        new Appointment("Royal Bully", "Royal Bully", 1508, 0, 2, 10);
        new Appointment("Royal Poisoner", "Royal Poisoner", 1509, 0, 2, 10);
        new Appointment("Royal Bragger", "Royal Bragger", 1510, 0, 2, 10);
    }
}


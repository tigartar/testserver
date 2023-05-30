package com.wurmonline.server.kingdom;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Appointments implements MiscConstants, TimeConstants {
   private static final Logger logger = Logger.getLogger(Appointments.class.getName());
   byte kingdom = 0;
   private final int era;
   static Appointments jenn = null;
   static Appointments hots = null;
   static Appointments molr = null;
   static Appointments none = null;
   private static final String CREATE_APPOINTMENTS = "insert into APPOINTMENTS ( ERA,KINGDOM, LASTCHECKED ) VALUES(?,?,?)";
   private static final String CREATE_OFFICES = "insert into OFFICES ( ERA ) VALUES(?)";
   private static final String RESET_APPOINTMENTS = "DELETE FROM APPOINTMENTS WHERE ERA=?";
   private static final String RESET_OFFICES = "DELETE FROM OFFICES WHERE ERA=?";
   private static final String GET_APPOINTMENTS = "select * FROM APPOINTMENTS WHERE ERA=?";
   private static final String GET_OFFICES = "select * FROM OFFICES WHERE ERA=?";
   private final Map<Integer, Appointment> appointments = new HashMap<>();
   private static final Map<Integer, Appointments> allAppointments = new HashMap<>();
   long lastChecked = 0L;
   static final int title1 = 0;
   static final int title2 = 1;
   static final int title3 = 2;
   static final int title4 = 3;
   static final int title5 = 4;
   static final int title6 = 5;
   static final int title7 = 6;
   static final int title8 = 7;
   static final int title9 = 8;
   public final int[] availableTitles = new int[9];
   private static final String[] titleDBStrings = new String[9];
   public static final int order1 = 30;
   public static final int order2 = 31;
   public static final int order3 = 32;
   public static final int order4 = 33;
   public static final int order5 = 34;
   public static final int order6 = 35;
   private static final int numappointments = 36;
   public final int[] availableOrders = new int[6];
   private static final String[] orderDBStrings;
   public static final int official1 = 1500;
   public static final int official2 = 1501;
   public static final int official3 = 1502;
   public static final int official4 = 1503;
   public static final int official5 = 1504;
   public static final int official6 = 1505;
   public static final int official7 = 1506;
   public static final int official8 = 1507;
   public static final int official9 = 1508;
   public static final int official10 = 1509;
   public static final int official11 = 1510;
   private static final int numOfficials = 11;
   public long[] officials = new long[11];
   private boolean[] officesSet = new boolean[11];
   private static final String[] officialDBStrings;
   private static final String[] officeDBStrings;

   Appointments(int _era, byte kdom, boolean current) {
      this.era = _era;
      this.kingdom = kdom;
      if (current) {
         if (this.kingdom == 1 && jenn == null) {
            jenn = this;
         }

         if (this.kingdom == 3 && hots == null) {
            hots = this;
         }

         if (this.kingdom == 2 && molr == null) {
            molr = this;
         }
      }

      logger.log(Level.INFO, "Loading era " + this.era + " for kingdom " + this.kingdom + " current=" + current);
      this.loadAppointments();
      addAppointments(this.era, this);
      Appointment.setAppointments(this);
   }

   private static void addAppointments(int era, Appointments app) {
      allAppointments.put(era, app);
   }

   static Appointments getCurrentAppointments(byte _kingdom) {
      if (_kingdom == 1) {
         return jenn;
      } else if (_kingdom == 3) {
         return hots;
      } else {
         return _kingdom == 2 ? molr : none;
      }
   }

   public static Appointments getAppointments(int era) {
      return allAppointments.get(era);
   }

   void addAppointment(Appointment app) {
      this.appointments.put(app.getId(), app);
   }

   public Appointment getAppointment(int _id) {
      return this.appointments.get(_id);
   }

   public Appointment getFinestAppointment(long data, long wurmid) {
      int highestlevel = 0;
      Appointment highest = null;
      Appointment tempapp = null;

      for(int x = 0; x < 36; ++x) {
         if ((data >> x & 1L) == 1L) {
            tempapp = this.getAppointment(x);
            if (tempapp.getLevel() > highestlevel) {
               highestlevel = tempapp.getLevel();
               highest = tempapp;
            }
         }
      }

      for(int x = 0; x < this.officials.length; ++x) {
         if (this.officials[x] == wurmid) {
            tempapp = this.getAppointment(x + 1500);
            if (tempapp.getLevel() >= highestlevel) {
               highestlevel = tempapp.getLevel();
               highest = tempapp;
            }
         }
      }

      return highest;
   }

   public int getAppointmentLevels(long data, long wurmid) {
      int levels = 0;

      for(int x = 0; x < 36; ++x) {
         if ((data >> x & 1L) == 1L) {
            Appointment tempapp = this.getAppointment(x);
            levels += tempapp.getLevel();
         }
      }

      for(int x = 0; x < this.officials.length; ++x) {
         if (this.officials[x] == wurmid) {
            Appointment tempapp = this.getAppointment(x + 1500);
            levels += tempapp.getLevel();
         }
      }

      return levels;
   }

   public String getTitles(long data, boolean male) {
      Set<Appointment> tempset = new HashSet<>();

      for(int x = 0; x < 36; ++x) {
         if ((data >> x & 1L) == 1L) {
            Appointment tempapp = this.getAppointment(x);
            if (tempapp != null && tempapp.getType() == 0) {
               tempset.add(tempapp);
            }
         }
      }

      if (tempset.isEmpty()) {
         return "";
      } else {
         int tempcounter = 0;
         StringBuilder sb = new StringBuilder();
         Iterator<Appointment> it = tempset.iterator();

         while(it.hasNext()) {
            if (male) {
               sb.append(it.next().getMaleName());
            } else {
               sb.append(it.next().getFemaleName());
            }

            if (++tempcounter <= tempset.size() - 2) {
               sb.append(", ");
            } else if (tempcounter == tempset.size() - 1) {
               sb.append(" and ");
            }
         }

         return sb.toString();
      }
   }

   public String getOrders(long data, boolean male) {
      Set<Appointment> tempset = new HashSet<>();

      for(int x = 30; x < 36; ++x) {
         if ((data >> x & 1L) == 1L) {
            Appointment tempapp = this.getAppointment(x);
            if (tempapp.getType() == 1) {
               tempset.add(tempapp);
            }
         }
      }

      if (tempset.isEmpty()) {
         return "";
      } else {
         int tempcounter = 0;
         StringBuilder sb = new StringBuilder();
         Iterator<Appointment> it = tempset.iterator();

         while(it.hasNext()) {
            if (male) {
               sb.append(it.next().getMaleName());
            } else {
               sb.append(it.next().getFemaleName());
            }

            if (++tempcounter <= tempset.size() - 2) {
               sb.append(", ");
            } else if (tempcounter == tempset.size() - 1) {
               sb.append(" and the ");
            }
         }

         return sb.toString();
      }
   }

   public boolean isAppointed(long wurmid) {
      for(int x = 0; x < this.officials.length; ++x) {
         if (this.officials[x] == wurmid) {
            return true;
         }
      }

      return false;
   }

   public String getOffices(long wurmid, boolean male) {
      Set<Appointment> tempset = new HashSet<>();

      for(int x = 0; x < this.officials.length; ++x) {
         if (this.officials[x] == wurmid) {
            Appointment tempapp = this.getAppointment(x + 1500);
            tempset.add(tempapp);
         }
      }

      if (tempset.isEmpty()) {
         return "";
      } else {
         int tempcounter = 0;
         StringBuilder sb = new StringBuilder();
         Iterator<Appointment> it = tempset.iterator();

         while(it.hasNext()) {
            if (male) {
               sb.append(it.next().getMaleName());
            } else {
               sb.append(it.next().getFemaleName());
            }

            if (++tempcounter <= tempset.size() - 2) {
               sb.append(", ");
            } else if (tempcounter == tempset.size() - 1) {
               sb.append(" and ");
            }
         }

         return sb.toString();
      }
   }

   public int getAvailTitlesForId(int _id) {
      return this.availableTitles[_id];
   }

   public int getAvailOrdersForId(int _id) {
      return this.availableOrders[_id - 30];
   }

   public long getOfficialForId(int _id) {
      return this.officials[_id - 1500];
   }

   private void loadAppointments() {
      boolean existed = false;
      if (this.era > 0) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("select * FROM APPOINTMENTS WHERE ERA=?");
            ps.setInt(1, this.era);
            rs = ps.executeQuery();
            if (rs.next()) {
               existed = true;

               for(int x = 0; x < this.availableOrders.length; ++x) {
                  this.availableOrders[x] = rs.getInt("ORDER" + (x + 1));
               }

               for(int x = 0; x < this.availableTitles.length; ++x) {
                  this.availableTitles[x] = rs.getInt("TITLE" + (x + 1));
               }

               this.officials = new long[11];

               for(int x = 0; x < this.officials.length; ++x) {
                  this.officials[x] = rs.getLong("OFFICIAL" + (x + 1));
               }

               this.lastChecked = rs.getLong("LASTCHECKED");
               logger.log(
                  Level.INFO,
                  "Loaded lastChecked for Era "
                     + this.era
                     + ". Last checked was "
                     + (System.currentTimeMillis() - this.lastChecked) / 3600000L
                     + " hours ago."
               );
            }

            DbUtilities.closeDatabaseObjects(ps, rs);
            ps = dbcon.prepareStatement("select * FROM OFFICES WHERE ERA=?");
            ps.setInt(1, this.era);
            rs = ps.executeQuery();
            if (rs.next()) {
               this.officesSet = new boolean[11];

               for(int x = 0; x < this.officesSet.length; ++x) {
                  this.officesSet[x] = rs.getBoolean("OFFICIAL" + (x + 1));
               }
            }

            DbUtilities.closeDatabaseObjects(ps, rs);
            if (!existed) {
               ps = dbcon.prepareStatement("insert into APPOINTMENTS ( ERA,KINGDOM, LASTCHECKED ) VALUES(?,?,?)");
               ps.setInt(1, this.era);
               ps.setByte(2, this.kingdom);
               ps.setLong(3, this.lastChecked);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
               ps = dbcon.prepareStatement("insert into OFFICES ( ERA ) VALUES(?)");
               ps.setInt(1, this.era);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to load kingdom officials for era " + this.era + var9.getMessage(), (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public int useTitle(int id) {
      int newAvail = --this.availableTitles[id];
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement(titleDBStrings[id]);
         ps.setInt(1, newAvail);
         ps.setInt(2, this.era);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(
            Level.WARNING,
            "Failed to set avail titles for era " + this.era + " " + id + ", " + Kingdoms.getNameFor(this.kingdom) + var9.getMessage(),
            (Throwable)var9
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return newAvail;
   }

   public int useOrder(int id) {
      int newAvail = --this.availableOrders[id - 30];
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement(orderDBStrings[id - 30]);
         ps.setInt(1, newAvail);
         ps.setInt(2, this.era);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(
            Level.WARNING,
            "Failed to set avail orders for era " + this.era + " " + id + ", " + Kingdoms.getNameFor(this.kingdom) + var9.getMessage(),
            (Throwable)var9
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return newAvail;
   }

   public boolean isOfficeSet(int id) {
      return this.officesSet[id - 1500];
   }

   public long setOfficial(int id, long wurmid) {
      long oldOfficial = this.officials[id - 1500];
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.officials[id - 1500] = wurmid;
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement(officialDBStrings[id - 1500]);
         ps.setLong(1, wurmid);
         ps.setInt(2, this.era);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         if (wurmid > 0L) {
            this.officesSet[id - 1500] = true;
            ps = dbcon.prepareStatement(officeDBStrings[id - 1500]);
            ps.setBoolean(1, true);
            ps.setInt(2, this.era);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
         }
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to set official era " + this.era + " " + wurmid + ", " + var12.getMessage(), (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return oldOfficial;
   }

   public void resetAppointments(byte kingdomId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         Zones.calculateZones(false);
         logger.log(
            Level.INFO,
            "Resetting lastChecked for Era " + this.era + ". Last checked was " + (System.currentTimeMillis() - this.lastChecked) / 3600000L + " hours ago."
         );
         this.lastChecked = System.currentTimeMillis();
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM APPOINTMENTS WHERE ERA=?");
         ps.setInt(1, this.era);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("insert into APPOINTMENTS ( ERA,KINGDOM, LASTCHECKED ) VALUES(?,?,?)");
         ps.setInt(1, this.era);
         ps.setByte(2, this.kingdom);
         ps.setLong(3, this.lastChecked);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM OFFICES WHERE ERA=?");
         ps.setInt(1, this.era);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("insert into OFFICES ( ERA ) VALUES(?)");
         ps.setInt(1, this.era);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         this.officesSet = new boolean[11];
         this.resetOfficials();
         float perc = Zones.getPercentLandForKingdom(kingdomId);
         this.resetOrders(perc);
         this.resetTitles(perc);
      } catch (SQLException var8) {
         logger.log(
            Level.WARNING,
            "Failed to reset appointments for era " + this.era + " kingdom " + Kingdoms.getNameFor(kingdomId) + ", " + var8.getMessage(),
            (Throwable)var8
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void resetOfficials() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();

         for(int x = 0; x < this.officials.length; ++x) {
            if (this.officials[x] > 0L) {
               ps = dbcon.prepareStatement(officialDBStrings[x]);
               ps.setLong(1, this.officials[x]);
               ps.setInt(2, this.era);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         }
      } catch (SQLException var7) {
         logger.log(
            Level.WARNING,
            "Failed to reset officials for era " + this.era + " kingdom " + Kingdoms.getNameFor(this.kingdom) + ", " + var7.getMessage(),
            (Throwable)var7
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void resetOrders(float percentOwned) {
      percentOwned = Math.min(50.0F, percentOwned);
      if (Servers.localServer.HOMESERVER) {
         percentOwned = 10.0F;
      }

      for(int a = 0; a < this.availableOrders.length; ++a) {
         int avail = (int)((percentOwned + 10.0F) / ((float)Math.max(a, 1) * 5.0F));
         if (avail == 0) {
            break;
         }

         this.availableOrders[a] = avail;
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();

         for(int x = 0; x < this.availableOrders.length; ++x) {
            if (this.availableOrders[x] > 0) {
               ps = dbcon.prepareStatement(orderDBStrings[x]);
               ps.setInt(1, this.availableOrders[x]);
               ps.setInt(2, this.era);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         }
      } catch (SQLException var8) {
         logger.log(
            Level.WARNING,
            "Failed to reset orders for era " + this.era + " kingdom " + Kingdoms.getNameFor(this.kingdom) + ", " + var8.getMessage(),
            (Throwable)var8
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void resetTitles(float percentOwned) {
      percentOwned = Math.min(50.0F, percentOwned);
      if (Servers.localServer.HOMESERVER) {
         percentOwned = 10.0F;
      }

      for(int a = 0; a < this.availableTitles.length; ++a) {
         int avail = (int)((percentOwned + 10.0F) / ((float)Math.max(a, 1) * 5.0F));
         if (avail == 0) {
            break;
         }

         this.availableTitles[a] = avail;
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();

         for(int x = 0; x < this.availableTitles.length; ++x) {
            if (this.availableTitles[x] > 0) {
               ps = dbcon.prepareStatement(titleDBStrings[x]);
               ps.setInt(1, this.availableTitles[x]);
               ps.setInt(2, this.era);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         }
      } catch (SQLException var8) {
         logger.log(
            Level.WARNING,
            "Failed to reset titles for era " + this.era + " kingdom " + Kingdoms.getNameFor(this.kingdom) + ", " + var8.getMessage(),
            (Throwable)var8
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public long getResetTimeRemaining() {
      return this.lastChecked + 604800000L - System.currentTimeMillis();
   }

   public static int getMaxAppointment(byte kId, int aId) {
      float p = Zones.getPercentLandForKingdom(kId);
      p = Math.min(50.0F, p);
      if (Servers.localServer.HOMESERVER) {
         p = 10.0F;
      }

      return (int)((p + 10.0F) / ((float)Math.max(aId, 1) * 5.0F));
   }

   static {
      for(int x = 0; x < titleDBStrings.length; ++x) {
         titleDBStrings[x] = "UPDATE APPOINTMENTS SET TITLE" + (x + 1) + "=? WHERE ERA=?";
      }

      orderDBStrings = new String[6];

      for(int x = 0; x < orderDBStrings.length; ++x) {
         orderDBStrings[x] = "UPDATE APPOINTMENTS SET ORDER" + (x + 1) + "=? WHERE ERA=?";
      }

      officialDBStrings = new String[11];
      officeDBStrings = new String[11];

      for(int x = 0; x < officialDBStrings.length; ++x) {
         officialDBStrings[x] = "UPDATE APPOINTMENTS SET OFFICIAL" + (x + 1) + "=? WHERE ERA=?";
         officeDBStrings[x] = "UPDATE OFFICES SET OFFICIAL" + (x + 1) + "=? WHERE ERA=?";
      }
   }
}

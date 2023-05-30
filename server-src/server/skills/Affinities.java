package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public final class Affinities {
   private static Logger logger = Logger.getLogger(Affinities.class.getName());
   private static final String updatePlayerAffinity = "update AFFINITIES set NUMBER=? where WURMID=? AND SKILL=?";
   private static final String createPlayerAffinity = "INSERT INTO AFFINITIES (WURMID,SKILL,NUMBER) VALUES(?,?,?)";
   private static final String deletePlayerAffinity = "DELETE FROM AFFINITIES WHERE WURMID=? AND SKILL=?";
   private static final String loadAllAffinities = "SELECT * FROM AFFINITIES WHERE NUMBER>0";
   private static final String deleteAllPlayerAffinity = "DELETE FROM AFFINITIES WHERE WURMID=?";
   private static Map<Long, Set<Affinity>> affinities = new HashMap<>();
   private static Affinity toRemove = null;
   private static boolean found = false;
   private static final Affinity[] emptyAffs = new Affinity[0];

   private Affinities() {
   }

   public static void loadAffinities() {
      long start = System.nanoTime();
      int loadedAffinities = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         affinities = new HashMap<>();
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM AFFINITIES WHERE NUMBER>0");

         for(rs = ps.executeQuery(); rs.next(); ++loadedAffinities) {
            setAffinity(rs.getLong("WURMID"), rs.getInt("SKILL"), rs.getByte("NUMBER"), true);
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, "Failed to load affinities!", (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.info("Loaded " + loadedAffinities + " affinities from the database took " + (float)(end - start) / 1000000.0F + " ms");
      }
   }

   public static void setAffinity(long playerid, int skillnumber, int value, boolean loading) {
      found = false;
      Long idl = playerid;
      Set<Affinity> affs = affinities.get(idl);
      if (affs == null) {
         affs = new HashSet<>();
         affinities.put(idl, affs);
      }

      for(Affinity a : affs) {
         if (a.skillNumber == skillnumber) {
            found = true;
            a.number = Math.min(5, value);
            if (!loading) {
               setAffinityForSkill(playerid, a.skillNumber, a.number);
               updateAffinity(playerid, a.skillNumber, a.number);
            }

            return;
         }
      }

      value = Math.min(5, value);
      if (!found && !loading) {
         createAffinity(playerid, skillnumber, value);
         setAffinityForSkill(playerid, skillnumber, value);
      }

      affs.add(new Affinity(skillnumber, value));
   }

   public static void decreaseAffinity(long playerid, int skillnum, int value) {
      Long idl = playerid;
      Set<Affinity> affs = affinities.get(idl);
      toRemove = null;
      if (affs == null) {
         logger.log(Level.WARNING, "Affinities not found when removing from " + playerid);
      } else {
         for(Affinity a : affs) {
            if (a.skillNumber == skillnum) {
               a.number -= value;
               setAffinityForSkill(playerid, skillnum, Math.max(0, a.number));
               if (a.number <= 0) {
                  toRemove = a;
               }
               break;
            }
         }

         if (toRemove != null) {
            affs.remove(toRemove);
            deleteAffinity(playerid, skillnum);
         }
      }
   }

   private static void setAffinityForSkill(long playerid, int skillid, int number) {
      try {
         Player p = Players.getInstance().getPlayer(playerid);
         if (p != null) {
            try {
               Skill s = p.getSkills().getSkill(skillid);
               s.setAffinity(number);
            } catch (NoSuchSkillException var6) {
            }
         }
      } catch (NoSuchPlayerException var7) {
      }
   }

   private static void deleteAffinity(long playerid, int skillnum) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM AFFINITIES WHERE WURMID=? AND SKILL=?");
         ps.setLong(1, playerid);
         ps.setInt(2, skillnum);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete affinity " + skillnum + " for " + playerid, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void deleteAllPlayerAffinity(long playerid) {
      Set<Affinity> set = affinities.get(playerid);
      if (set != null) {
         set.clear();
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM AFFINITIES WHERE WURMID=?");
         ps.setLong(1, playerid);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete affinities for " + playerid, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void updateAffinity(long playerid, int skillnum, int number) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("update AFFINITIES set NUMBER=? where WURMID=? AND SKILL=?");
         ps.setByte(1, (byte)number);
         ps.setLong(2, playerid);
         ps.setInt(3, skillnum);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to update affinity " + skillnum + " for " + playerid, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void createAffinity(long playerid, int skillnum, int number) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("INSERT INTO AFFINITIES (WURMID,SKILL,NUMBER) VALUES(?,?,?)");
         ps.setLong(1, playerid);
         ps.setInt(2, skillnum);
         ps.setByte(3, (byte)number);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to create affinity " + skillnum + " for " + playerid + " nums=" + number, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Nonnull
   public static Affinity[] getAffinities(long playerid) {
      Set<Affinity> affs = affinities.get(playerid);
      return affs != null && !affs.isEmpty() ? affs.toArray(new Affinity[affs.size()]) : emptyAffs;
   }
}

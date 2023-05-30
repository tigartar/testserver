package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public final class SkillStat implements TimeConstants {
   private static Logger logger = Logger.getLogger(SkillStat.class.getName());
   public final Map<Long, Double> stats = new HashMap<>();
   @GuardedBy("RW_LOCK")
   private static final Map<Integer, SkillStat> allStats = new HashMap<>();
   private static final ReentrantReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
   private final String skillName;
   private final int skillnum;
   private static final String loadAllPlayerSkills = "select NUMBER,OWNER,VALUE from SKILLS sk INNER JOIN PLAYERS p ON p.WURMID=sk.OWNER AND p.CURRENTSERVER=? WHERE sk.VALUE>25 ";

   private SkillStat(int num, String name) {
      this.skillName = name;
      this.skillnum = num;
   }

   private static int loadAllStats() {
      Connection dbcon = null;
      int numberSkillsLoaded = 0;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(
            "select NUMBER,OWNER,VALUE from SKILLS sk INNER JOIN PLAYERS p ON p.WURMID=sk.OWNER AND p.CURRENTSERVER=? WHERE sk.VALUE>25 "
         );
         ps.setInt(1, Servers.localServer.id);

         for(rs = ps.executeQuery(); rs.next(); ++numberSkillsLoaded) {
            SkillStat sk = getSkillStatForSkill(rs.getInt("NUMBER"));
            if (sk != null) {
               sk.stats.put(new Long(rs.getLong("OWNER")), new Double(rs.getDouble("VALUE")));
            }
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem loading the Skill stats due to " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return numberSkillsLoaded;
   }

   static final void addSkill(int skillNum, String name) {
      RW_LOCK.writeLock().lock();

      try {
         allStats.put(skillNum, new SkillStat(skillNum, name));
      } finally {
         RW_LOCK.writeLock().unlock();
      }
   }

   public static final void pollSkills() {
      Thread statsPoller = new Thread("StatsPoller") {
         @Override
         public void run() {
            try {
               long now = System.currentTimeMillis();
               int numberSkillsLoaded = SkillStat.loadAllStats();
               SkillStat.logger
                  .log(Level.WARNING, "Polling " + numberSkillsLoaded + " skills for stats v2 took " + (System.currentTimeMillis() - now) + " ms.");
            } catch (RuntimeException var4) {
               SkillStat.logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
            }
         }
      };
      statsPoller.start();
   }

   public static final SkillStat getSkillStatForSkill(int num) {
      RW_LOCK.readLock().lock();

      SkillStat var1;
      try {
         var1 = allStats.get(num);
      } finally {
         RW_LOCK.readLock().unlock();
      }

      return var1;
   }
}

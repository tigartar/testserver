package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Triggers2Effects {
   private static Logger logger = Logger.getLogger(Triggers2Effects.class.getName());
   private static final String LOAD_ALL_LINKS = "SELECT * FROM TRIGGERS2EFFECTS";
   private static final String CREATE_LINK = "INSERT INTO TRIGGERS2EFFECTS (TRIGGERID, EFFECTID) VALUES(?,?)";
   private static final String DELETE_LINK = "DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=? AND EFFECTID=?";
   private static final String DELETE_TRIGGER = "DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=?";
   private static final String DELETE_EFFECT = "DELETE FROM TRIGGERS2EFFECTS WHERE EFFECTID=?";
   private static final Map<Integer, HashSet<Integer>> triggers2Effects = new ConcurrentHashMap<>();
   private static final Map<Integer, HashSet<Integer>> effects2Triggers = new ConcurrentHashMap<>();

   private Triggers2Effects() {
   }

   public static TriggerEffect[] getEffectsForTrigger(int triggerId, boolean incInactive) {
      Set<TriggerEffect> effs = new HashSet<>();
      HashSet<Integer> effects = triggers2Effects.get(triggerId);
      if (effects != null) {
         for(Integer effectId : effects) {
            TriggerEffect eff = TriggerEffects.getTriggerEffect(effectId);
            if (eff != null && (incInactive || !incInactive && !eff.isInactive())) {
               effs.add(eff);
            }
         }
      }

      return effs.toArray(new TriggerEffect[effs.size()]);
   }

   public static MissionTrigger[] getTriggersForEffect(int effectId, boolean incInactive) {
      Set<MissionTrigger> trgs = new HashSet<>();
      HashSet<Integer> triggers = effects2Triggers.get(effectId);
      if (triggers != null) {
         for(Integer triggerId : triggers) {
            MissionTrigger trg = MissionTriggers.getTriggerWithId(triggerId);
            if (trg != null && (incInactive || !incInactive && !trg.isInactive())) {
               trgs.add(trg);
            }
         }
      }

      return trgs.toArray(new MissionTrigger[trgs.size()]);
   }

   public static boolean hasLink(int triggerId, int effectId) {
      HashSet<Integer> effects = triggers2Effects.get(triggerId);
      return effects != null ? effects.contains(effectId) : false;
   }

   public static boolean hasEffect(int triggerId) {
      HashSet<Integer> effects = triggers2Effects.get(triggerId);
      if (effects != null) {
         return !effects.isEmpty();
      } else {
         return false;
      }
   }

   public static boolean hasTrigger(int effectId) {
      HashSet<Integer> triggers = effects2Triggers.get(effectId);
      if (triggers != null) {
         return !triggers.isEmpty();
      } else {
         return false;
      }
   }

   public static void addLink(int triggerId, int effectId, boolean loading) {
      if (triggerId > 0 && effectId > 0) {
         HashSet<Integer> effects = triggers2Effects.get(triggerId);
         if (effects == null) {
            effects = new HashSet<>();
         }

         boolean effAdded = effects.add(effectId);
         if (!effects.isEmpty()) {
            triggers2Effects.put(triggerId, effects);
         }

         HashSet<Integer> triggers = effects2Triggers.get(effectId);
         if (triggers == null) {
            triggers = new HashSet<>();
         }

         boolean trgAdded = triggers.add(triggerId);
         if (!triggers.isEmpty()) {
            effects2Triggers.put(effectId, triggers);
         }

         if (!loading && (effAdded || trgAdded)) {
            dbCreateLink(triggerId, effectId);
         }
      }
   }

   public static void deleteLink(int triggerId, int effectId) {
      HashSet<Integer> effects = triggers2Effects.remove(triggerId);
      if (effects != null) {
         effects.remove(effectId);
         if (!effects.isEmpty()) {
            triggers2Effects.put(triggerId, effects);
         }
      }

      HashSet<Integer> triggers = effects2Triggers.remove(effectId);
      if (triggers != null) {
         triggers.remove(triggerId);
         if (!triggers.isEmpty()) {
            effects2Triggers.put(effectId, triggers);
         }
      }

      dbDeleteLink(triggerId, effectId);
   }

   public static void deleteTrigger(int triggerId) {
      HashSet<Integer> effects = triggers2Effects.remove(triggerId);
      if (effects != null) {
         for(Integer effectId : effects) {
            HashSet<Integer> triggers = effects2Triggers.remove(effectId);
            if (triggers != null) {
               triggers.remove(triggerId);
               if (!triggers.isEmpty()) {
                  effects2Triggers.put(effectId, triggers);
               }
            }
         }
      }

      dbDeleteTrigger(triggerId);
   }

   public static void deleteEffect(int effectId) {
      HashSet<Integer> triggers = effects2Triggers.remove(effectId);
      if (triggers != null) {
         for(Integer triggerId : triggers) {
            HashSet<Integer> effects = effects2Triggers.remove(triggerId);
            if (effects != null) {
               effects.remove(effectId);
               if (!effects.isEmpty()) {
                  effects2Triggers.put(effectId, triggers);
               }
            }
         }
      }

      dbDeleteEffect(effectId);
   }

   private static void dbCreateLink(int triggerId, int effectId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("INSERT INTO TRIGGERS2EFFECTS (TRIGGERID, EFFECTID) VALUES(?,?)");
         ps.setInt(1, triggerId);
         ps.setInt(2, effectId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, var9.getMessage());
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbDeleteLink(int triggerId, int effectId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=? AND EFFECTID=?");
         ps.setInt(1, triggerId);
         ps.setInt(2, effectId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, var9.getMessage());
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbDeleteTrigger(int triggerId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM TRIGGERS2EFFECTS WHERE TRIGGERID=?");
         ps.setInt(1, triggerId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage());
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbDeleteEffect(int effectId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM TRIGGERS2EFFECTS WHERE EFFECTID=?");
         ps.setInt(1, effectId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage());
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbLoadAllTriggers2Effects() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM TRIGGERS2EFFECTS");
         rs = ps.executeQuery();

         while(rs.next()) {
            int triggerId = rs.getInt("TRIGGERID");
            int effectId = rs.getInt("EFFECTID");
            addLink(triggerId, effectId, true);
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage());
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   static {
      try {
         dbLoadAllTriggers2Effects();
      } catch (Exception var1) {
         logger.log(Level.WARNING, "Problems loading all Triggers 2 Effects", (Throwable)var1);
      }
   }
}

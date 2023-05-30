package com.wurmonline.server.zones;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Dens implements CreatureTemplateIds {
   private static final String GET_DENS = "select * from DENS";
   private static final String DELETE_DEN = "DELETE FROM DENS  where TEMPLATEID=?";
   private static final String CREATE_DEN = "insert into DENS(TEMPLATEID,TILEX, TILEY, SURFACED) values(?,?,?,?)";
   private static final Logger logger = Logger.getLogger(Dens.class.getName());
   private static final Map<Integer, Den> dens = new HashMap<>();

   private Dens() {
   }

   private static void addDen(Den den) {
      dens.put(den.getTemplateId(), den);
   }

   private static void removeDen(int templateId) {
      dens.remove(templateId);
   }

   public static Den getDen(int templateId) {
      return dens.get(templateId);
   }

   public static Map<Integer, Den> getDens() {
      return Collections.unmodifiableMap(dens);
   }

   public static Den getDen(int tilex, int tiley) {
      for(Den d : dens.values()) {
         if (d.getTilex() == tilex && d.getTiley() == tiley) {
            return d;
         }
      }

      return null;
   }

   public static void loadDens() {
      logger.info("Loading dens");
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("select * from DENS");
         rs = ps.executeQuery();
         int tid = -1;
         int tilex = 0;
         int tiley = 0;
         boolean surfaced = false;

         while(rs.next()) {
            tid = rs.getInt("TEMPLATEID");
            tilex = rs.getInt("TILEX");
            tiley = rs.getInt("TILEY");
            surfaced = rs.getBoolean("SURFACED");
            if (tid > 0) {
               Den den = new Den(tid, tilex, tiley, surfaced);
               addDen(den);
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("Loaded Den: " + den);
               }
            }
         }

         checkDens(false);
      } catch (SQLException var15) {
         logger.log(Level.WARNING, "Problem loading Dens - " + var15.getMessage(), (Throwable)var15);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.info("Loaded " + dens.size() + " dens from the database took " + (float)(end - start) / 1000000.0F + " ms");
      }
   }

   public static void checkDens(boolean whileRunning) {
      checkTemplate(16, whileRunning);
      checkTemplate(89, whileRunning);
      checkTemplate(91, whileRunning);
      checkTemplate(90, whileRunning);
      checkTemplate(92, whileRunning);
      checkTemplate(17, whileRunning);
      checkTemplate(18, whileRunning);
      checkTemplate(19, whileRunning);
      checkTemplate(104, whileRunning);
      checkTemplate(103, whileRunning);
      checkTemplate(20, whileRunning);
      checkTemplate(22, whileRunning);
      checkTemplate(27, whileRunning);
      checkTemplate(11, whileRunning);
      checkTemplate(26, whileRunning);
      checkTemplate(23, whileRunning);
      Constants.respawnUniques = false;
   }

   private static final Den getDragonSpawnTop(int templateId) {
      switch(templateId) {
         case 16:
            return Zones.getNorthTop(templateId);
         case 17:
            return Zones.getSouthTop(templateId);
         case 18:
            return Zones.getEastTop(templateId);
         case 19:
            return Zones.getWestTop(templateId);
         case 89:
            return Zones.getWestTop(templateId);
         case 90:
            return Zones.getNorthTop(templateId);
         case 91:
            return Zones.getSouthTop(templateId);
         case 92:
            return Zones.getEastTop(templateId);
         case 103:
            return Zones.getSouthTop(templateId);
         case 104:
            return Zones.getNorthTop(templateId);
         default:
            return Zones.getRandomTop();
      }
   }

   private static void checkTemplate(int templateId, boolean whileRunning) {
      try {
         CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
         boolean creatureExists = Creatures.getInstance().creatureWithTemplateExists(templateId);
         if (!Constants.respawnUniques && !whileRunning) {
            return;
         }

         if (whileRunning && Server.rand.nextInt(300) > 0) {
            return;
         }

         if (!creatureExists) {
            Den d = dens.get(templateId);
            if (d != null) {
               deleteDen(templateId);
            }
         }

         if (!dens.containsKey(templateId)) {
            Den den = null;
            if (CreatureTemplate.isDragon(templateId)) {
               if (!Servers.localServer.isChallengeServer()) {
                  if (Constants.respawnUniques) {
                     den = getDragonSpawnTop(templateId);
                  } else if (Server.rand.nextBoolean()) {
                     den = Zones.getRandomTop();
                  } else {
                     den = Zones.getRandomForest(templateId);
                  }

                  if (den != null) {
                     den = createDen(den.getTemplateId(), den.getTilex(), den.getTiley(), den.isSurfaced());
                  }
               }
            } else {
               if (template.getLeaderTemplateId() > 0) {
                  den = getDen(template.getLeaderTemplateId());
                  if (den != null) {
                     den.setTemplateId(templateId);
                  }
               } else {
                  den = Zones.getRandomForest(templateId);
               }

               if (den != null) {
                  den = createDen(den.getTemplateId(), den.getTilex(), den.getTiley(), den.isSurfaced());
               }
            }

            if (den != null) {
               if (template.isUnique()) {
                  VolaTile villtile = Zones.getOrCreateTile(den.getTilex(), den.getTiley(), den.isSurfaced());
                  Village vill = villtile.getVillage();
                  if (vill != null) {
                     logger.log(Level.INFO, "Unique spawn " + template.getName() + ", on deed " + vill.getName() + ".");
                     removeDen(templateId);
                     return;
                  }
               }

               if (!template.isUnique()) {
                  try {
                     Zone zone = Zones.getZone(den.getTilex(), den.getTiley(), den.isSurfaced());
                     zone.den = den;
                     logger.log(
                        Level.INFO,
                        "Zone at " + den.getTilex() + ", " + den.getTiley() + " now spawning " + template.getName() + " (" + den.getTemplateId() + ")"
                     );
                  } catch (NoSuchZoneException var9) {
                     logger.log(Level.WARNING, "Den at " + den.getTilex() + ", " + den.getTiley() + " surf=" + den.isSurfaced() + " - zone does not exist.");
                  }
               } else if (!creatureExists) {
                  byte ctype = (byte)Math.max(0, Server.rand.nextInt(22) - 10);
                  if (Server.rand.nextInt(3) < 2) {
                     ctype = 0;
                  }

                  if (Server.rand.nextInt(40) == 0) {
                     ctype = 99;
                  }

                  try {
                     Creature.doNew(
                        templateId,
                        ctype,
                        (float)((den.getTilex() << 2) + 2),
                        (float)((den.getTiley() << 2) + 2),
                        180.0F,
                        den.isSurfaced() ? 0 : -1,
                        template.getName(),
                        template.getSex()
                     );
                     logger.log(Level.INFO, "Created " + template.getName() + " at " + den.getTilex() + "," + den.getTiley() + "!");
                  } catch (Exception var8) {
                     logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
                  }
               }

               addDen(den);
            }
         } else if (!template.isUnique()) {
            Den den = getDen(templateId);

            try {
               Zone zone = Zones.getZone(den.getTilex(), den.getTiley(), den.isSurfaced());
               zone.den = den;
               logger.log(
                  Level.INFO, "Zone at " + den.getTilex() + ", " + den.getTiley() + " now spawning " + template.getName() + " (" + den.getTemplateId() + ")"
               );
            } catch (NoSuchZoneException var7) {
               logger.log(Level.WARNING, "Den at " + den.getTilex() + ", " + den.getTiley() + " surf=" + den.isSurfaced() + " - zone does not exist.");
            }
         }
      } catch (NoSuchCreatureTemplateException var10) {
         logger.log(Level.WARNING, templateId + ":" + var10.getMessage(), (Throwable)var10);
      }
   }

   public static void deleteDen(int templateId) {
      logger.log(Level.INFO, "Deleting den for " + templateId);
      Den d = getDen(templateId);
      if (d != null) {
         logger.log(Level.INFO, "Den for " + templateId + " was at " + d.getTilex() + "," + d.getTiley());
      }

      removeDen(templateId);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM DENS  where TEMPLATEID=?");
         ps.setInt(1, templateId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, templateId + ":" + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static Den createDen(int templateId, int tilex, int tiley, boolean surfaced) {
      Den den = new Den(templateId, tilex, tiley, surfaced);
      addDen(den);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("insert into DENS(TEMPLATEID,TILEX, TILEY, SURFACED) values(?,?,?,?)");
         ps.setInt(1, templateId);
         ps.setInt(2, tilex);
         ps.setInt(3, tiley);
         ps.setBoolean(4, surfaced);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, templateId + ":" + var11.getMessage(), (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return den;
   }
}

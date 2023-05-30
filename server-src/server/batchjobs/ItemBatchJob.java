package com.wurmonline.server.batchjobs;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.BodyDbStrings;
import com.wurmonline.server.items.CoinDbStrings;
import com.wurmonline.server.items.DbStrings;
import com.wurmonline.server.items.ItemDbStrings;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemBatchJob implements ItemTypes, MiscConstants, ItemMaterials {
   private static final String deleteLegs = "DELETE FROM BODYITEMS WHERE TEMPLATEID=10";
   private static final String deleteFeet = "DELETE FROM BODYITEMS WHERE TEMPLATEID=15";
   private static final String deleteLeg = "DELETE FROM BODYITEMS WHERE TEMPLATEID=19";
   private static Logger logger = Logger.getLogger(ItemBatchJob.class.getName());

   private ItemBatchJob() {
   }

   public static void fixStructureGuests() {
      logger.log(Level.INFO, "Fixing structure guests.");
      Connection dbcon = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         String getAll = "select WURMID,GUESTS from STRUCTURES";
         ps = dbcon.prepareStatement("select WURMID,GUESTS from STRUCTURES");
         rs = ps.executeQuery();

         while(rs.next()) {
            long wurmid = rs.getLong("WURMID");
            long[] guestArr = (long[])rs.getObject("GUESTS");

            for(long lGuest : guestArr) {
               ps2 = dbcon.prepareStatement("INSERT INTO STRUCTUREGUESTS (STRUCTUREID,GUESTID)VALUES(?,?)");
               ps2.setLong(1, wurmid);
               ps2.setLong(2, lGuest);
               ps2.executeUpdate();
               ps2.close();
            }
         }
      } catch (SQLException var16) {
         logger.log(Level.WARNING, "Failed to move structure guests.", (Throwable)var16);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbUtilities.closeDatabaseObjects(ps2, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void fixZonesStructure() {
      logger.log(Level.INFO, "Fixing zone structures.");
      Connection dbcon = null;
      PreparedStatement ps = null;
      PreparedStatement ps2 = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         String getAll = "select ZONEID, STRUCTURES FROM ZONES";
         ps = dbcon.prepareStatement("select ZONEID, STRUCTURES FROM ZONES");
         rs = ps.executeQuery();

         while(rs.next()) {
            int zoneid = rs.getInt("ZONEID");
            long[] structArr = (long[])rs.getObject("STRUCTURES");

            for(long lStructure : structArr) {
               ps2 = dbcon.prepareStatement("INSERT INTO ZONESTRUCTURES (ZONEID,STRUCTUREID)VALUES(?,?)");
               ps2.setInt(1, zoneid);
               ps2.setLong(2, lStructure);
               ps2.executeUpdate();
               ps2.close();
            }
         }
      } catch (SQLException var15) {
         logger.log(Level.WARNING, "Failed to move zone structure.", (Throwable)var15);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbUtilities.closeDatabaseObjects(ps2, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void setNames() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         String getAll = "select * from ITEMS";
         ps = dbcon.prepareStatement("select * from ITEMS");
         rs = ps.executeQuery();

         while(rs.next()) {
            long wurmid = rs.getLong("WURMID");
            String description = rs.getString("NAME");
            setDescription(dbcon, wurmid, description);
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to check if item exists.", (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void setDescription(Connection dbcon, long wurmId, String desc) {
      PreparedStatement ps = null;

      try {
         String setAll = "update ITEMS set DESCRIPTION=?, NAME=\"\" where WURMID=?";
         ps = dbcon.prepareStatement("update ITEMS set DESCRIPTION=?, NAME=\"\" where WURMID=?");
         ps.setString(1, desc);
         ps.setLong(2, wurmId);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to save item " + wurmId, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }
   }

   public static final void deleteFeet() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM BODYITEMS WHERE TEMPLATEID=10");
         ps.executeUpdate();
         ps.close();
         ps = dbcon.prepareStatement("DELETE FROM BODYITEMS WHERE TEMPLATEID=19");
         ps.executeUpdate();
         ps.close();
         ps = dbcon.prepareStatement("DELETE FROM BODYITEMS WHERE TEMPLATEID=15");
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static boolean isFish(int templateId) {
      return templateId == 158
         || templateId == 164
         || templateId == 160
         || templateId == 159
         || templateId == 163
         || templateId == 157
         || templateId == 162
         || templateId == 161
         || templateId == 165;
   }

   public static final void trimSizes() {
      trimSizes(ItemDbStrings.getInstance());
      trimSizes(BodyDbStrings.getInstance());
      trimSizes(CoinDbStrings.getInstance());
   }

   public static final void trimSizes(DbStrings instance) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(instance.getItemWeights());
         rs = ps.executeQuery();
         int maxSizeMod = 5;

         while(rs.next()) {
            try {
               long id = rs.getLong("WURMID");
               ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(rs.getInt("TEMPLATEID"));
               if (template == null) {
                  throw new WurmServerException("No template.");
               }

               int weight = rs.getInt("WEIGHT");
               if ((!template.isCombine() || template.isLiquid()) && !isFish(template.getTemplateId())) {
                  if (!template.isLiquid()) {
                     setSizeX(id, template.getSizeX(), dbcon, instance);
                     setSizeY(id, template.getSizeY(), dbcon, instance);
                     setSizeZ(id, template.getSizeZ(), dbcon, instance);
                  }
               } else {
                  float mod = (float)weight / (float)template.getWeightGrams();
                  if (mod > 125.0F) {
                     setSizeZ(id, template.getSizeZ() * 5, dbcon, instance);
                     setSizeY(id, template.getSizeY() * 5, dbcon, instance);
                     setSizeX(id, template.getSizeX() * 5, dbcon, instance);
                  } else if (mod > 25.0F) {
                     setSizeZ(id, template.getSizeZ() * 5, dbcon, instance);
                     setSizeY(id, template.getSizeY() * 5, dbcon, instance);
                     mod /= 25.0F;
                     setSizeX(id, (int)((float)template.getSizeX() * mod), dbcon, instance);
                  } else if (mod > 5.0F) {
                     setSizeZ(id, template.getSizeZ() * 5, dbcon, instance);
                     mod /= 5.0F;
                     setSizeY(id, (int)((float)template.getSizeY() * mod), dbcon, instance);
                     setSizeX(id, template.getSizeX(), dbcon, instance);
                  } else {
                     setSizeZ(id, Math.max(1, (int)((float)template.getSizeZ() * mod)), dbcon, instance);
                     setSizeY(id, Math.max(1, (int)((float)template.getSizeY() * mod)), dbcon, instance);
                     setSizeX(id, Math.max(1, (int)((float)template.getSizeX() * mod)), dbcon, instance);
                  }
               }
            } catch (Exception var14) {
               if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, "Problem: " + var14.getMessage(), (Throwable)var14);
               }
            }
         }
      } catch (SQLException var15) {
         logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void setSizeX(long id, int sizex, Connection dbcon, DbStrings instance) {
      PreparedStatement ps = null;

      try {
         ps = dbcon.prepareStatement(instance.setSizeX());
         ps.setInt(1, sizex);
         ps.setLong(2, id);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }
   }

   public static void setSizeY(long id, int sizey, Connection dbcon, DbStrings instance) {
      PreparedStatement ps = null;

      try {
         ps = dbcon.prepareStatement(instance.setSizeY());
         ps.setInt(1, sizey);
         ps.setLong(2, id);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }
   }

   public static void setSizeZ(long id, int sizez, Connection dbcon, DbStrings instance) {
      PreparedStatement ps = null;

      try {
         ps = dbcon.prepareStatement(instance.setSizeZ());
         ps.setInt(1, sizez);
         ps.setLong(2, id);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }
   }

   public static void setMat(long id, byte material, DbStrings instance) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(instance.setMaterial());
         ps.setByte(1, material);
         ps.setLong(2, id);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void setPar(long id, long pid, DbStrings instance) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(instance.setParentId());
         ps.setLong(1, pid);
         ps.setLong(2, id);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void setDesc(long id, String name, DbStrings instance) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(instance.setName());
         ps.setString(1, name);
         ps.setLong(2, id);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void setW(long id, int w, DbStrings instance) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(instance.setWeight());
         ps.setInt(1, w);
         ps.setLong(2, id);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save item " + id, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}

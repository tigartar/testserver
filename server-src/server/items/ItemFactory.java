package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ItemFactory implements MiscConstants, ItemTypes, ItemMaterials {
   private static final Logger logger = Logger.getLogger(ItemFactory.class.getName());
   private static final String deleteItemData = "delete from ITEMDATA where WURMID=?";
   private static DbStrings dbstrings;
   public static int[] metalLumpList = new int[]{46, 221, 223, 205, 47, 220, 49, 44, 45, 48, 837, 698, 694, 1411};

   private ItemFactory() {
   }

   @Nonnull
   public static Item createItem(int templateId, float qualityLevel, byte material, byte aRarity, @Nullable String creator) throws FailedException, NoSuchTemplateException {
      return createItem(templateId, qualityLevel, material, aRarity, -10L, creator);
   }

   public static Optional<Item> createItemOptional(int templateId, float qualityLevel, byte material, byte aRarity, @Nullable String creator) {
      try {
         return Optional.of(createItem(templateId, qualityLevel, material, aRarity, creator));
      } catch (Exception var6) {
         var6.printStackTrace();
         return Optional.empty();
      }
   }

   public static void createContainerRestrictions(Item item) {
      ItemTemplate template = item.getTemplate();
      if (template.getContainerRestrictions() != null && !template.isNoPut()) {
         for(ContainerRestriction cRest : template.getContainerRestrictions()) {
            boolean skipAdd = false;

            for(Item i : item.getItems()) {
               if (i.getTemplateId() == 1392 && cRest.contains(i.getRealTemplateId())) {
                  skipAdd = true;
               } else if (cRest.contains(i.getTemplateId())) {
                  skipAdd = true;
               }
            }

            if (!skipAdd) {
               try {
                  Item tempSlotItem = createItem(1392, 100.0F, item.getCreatorName());
                  tempSlotItem.setRealTemplate(cRest.getEmptySlotTemplateId());
                  tempSlotItem.setName(cRest.getEmptySlotName());
                  item.insertItem(tempSlotItem, true);
               } catch (NoSuchTemplateException | FailedException var7) {
               }
            }
         }
      }
   }

   @Nonnull
   public static Item createItem(int templateId, float qualityLevel, byte material, byte aRarity, long bridgeId, @Nullable String creator) throws FailedException, NoSuchTemplateException {
      ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
      if (material == 0) {
         material = template.getMaterial();
      }

      String name = generateName(template, material);
      Item toReturn;
      if (template.isTemporary()) {
         try {
            toReturn = new TempItem(name, template, qualityLevel, creator);
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest("Creating tempitem: " + toReturn);
            }
         } catch (IOException var17) {
            throw new FailedException(var17);
         }
      } else {
         try {
            if (template.isRecycled) {
               toReturn = Itempool.getRecycledItem(templateId, qualityLevel);
               if (toReturn != null) {
                  if (toReturn.isTemporary()) {
                     toReturn.clear(WurmId.getNextTempItemId(), creator, 0.0F, 0.0F, 0.0F, 1.0F, "", name, qualityLevel, material, aRarity, bridgeId);
                  } else {
                     toReturn.clear(toReturn.id, creator, 0.0F, 0.0F, 0.0F, 1.0F, "", name, qualityLevel, material, aRarity, bridgeId);
                  }

                  return toReturn;
               }
            }

            toReturn = new DbItem(-10L, name, template, qualityLevel, material, aRarity, bridgeId, creator);
            if (template.isCoin()) {
               Server.getInstance().transaction(toReturn.getWurmId(), -10L, bridgeId, "new " + toReturn.getName(), (long)template.getValue());
            }
         } catch (IOException var16) {
            throw new FailedException(var16);
         }
      }

      if (template.getInitialContainers() != null) {
         for(InitialContainer ic : template.getInitialContainers()) {
            byte icMaterial = ic.getMaterial() == 0 ? material : ic.getMaterial();
            Item subItem = createItem(ic.getTemplateId(), Math.max(1.0F, qualityLevel), icMaterial, aRarity, creator);
            subItem.setName(ic.getName());
            toReturn.insertItem(subItem, true);
         }
      }

      if (toReturn != null) {
         createContainerRestrictions(toReturn);
      }

      return toReturn;
   }

   public static Item createItem(int templateId, float qualityLevel, byte aRarity, @Nullable String creator) throws FailedException, NoSuchTemplateException {
      return createItem(templateId, qualityLevel, (byte)0, aRarity, creator);
   }

   public static Optional<Item> createItemOptional(int templateId, float qualityLevel, byte aRarity, @Nullable String creator) {
      return createItemOptional(templateId, qualityLevel, (byte)0, aRarity, creator);
   }

   @Nonnull
   public static Item createItem(int templateId, float qualityLevel, @Nullable String creator) throws FailedException, NoSuchTemplateException {
      return createItem(templateId, qualityLevel, (byte)0, (byte)0, creator);
   }

   public static String generateName(ItemTemplate template, byte material) {
      String name = template.sizeString + template.getName();
      if (template.getTemplateId() == 683) {
         name = HexMap.generateFirstName() + " " + HexMap.generateSecondName();
      }

      if (template.unique) {
         name = template.getName();
      }

      return name;
   }

   public static Item createBodyPart(Body body, short place, int templateId, String name, float qualityLevel) throws FailedException, NoSuchTemplateException {
      ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
      Item toReturn = null;

      try {
         long wurmId = WurmId.getNextBodyPartId(body.getOwnerId(), (byte)place, WurmId.getType(body.getOwnerId()) == 0);
         if (template.isRecycled) {
            toReturn = Itempool.getRecycledItem(templateId, qualityLevel);
            if (toReturn != null) {
               toReturn.clear(-10L, "", 0.0F, 0.0F, 0.0F, 0.0F, "", name, qualityLevel, template.getMaterial(), (byte)0, -10L);
               toReturn.setPlace(place);
            }
         }

         if (toReturn == null) {
            toReturn = new TempItem(wurmId, place, name, template, qualityLevel, "");
         }

         return toReturn;
      } catch (IOException var9) {
         throw new FailedException(var9);
      }
   }

   @Nullable
   public static Item createInventory(long ownerId, short place, float qualityLevel) throws FailedException, NoSuchTemplateException {
      ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(0);
      Item toReturn = null;

      try {
         long wurmId = WurmId.getNextBodyPartId(ownerId, (byte)place, WurmId.getType(ownerId) == 0);
         if (template.isRecycled) {
            toReturn = Itempool.getRecycledItem(0, qualityLevel);
            if (toReturn != null) {
               toReturn.clear(wurmId, "", 0.0F, 0.0F, 0.0F, 0.0F, "", "inventory", qualityLevel, template.getMaterial(), (byte)0, -10L);
            }
         }

         if (toReturn == null) {
            toReturn = new TempItem(wurmId, place, "inventory", template, qualityLevel, "");
         }

         return toReturn;
      } catch (IOException var8) {
         throw new FailedException(var8);
      }
   }

   public static Item loadItem(long id) throws NoSuchItemException, Exception {
      Item item = null;
      if (WurmId.getType(id) != 2 && WurmId.getType(id) != 19 && WurmId.getType(id) != 20) {
         throw new NoSuchItemException("Temporary item.");
      } else {
         return new DbItem(id);
      }
   }

   public static void decay(long id, @Nullable DbStrings dbStrings) {
      dbstrings = dbStrings;
      if (dbstrings == null) {
         dbstrings = Item.getDbStringsByWurmId(id);
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(dbstrings.deleteItem());
         ps.setLong(1, id);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("delete from ITEMDATA where WURMID=?");
         ps.setLong(1, id);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
         ps.setLong(1, id);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to decay item with id " + id, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void clearData(long id) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("delete from ITEMDATA where WURMID=?");
         ps.setLong(1, id);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
         ps.setLong(1, id);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to decay item with id " + id, (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static Item createItem(
      int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte rarity, long bridgeId, @Nullable String creator
   ) throws NoSuchTemplateException, FailedException {
      return createItem(templateId, qualityLevel, posX, posY, rot, onSurface, (byte)0, rarity, bridgeId, creator);
   }

   public static Item createItem(
      int templateId,
      float qualityLevel,
      float posX,
      float posY,
      float rot,
      boolean onSurface,
      byte material,
      byte aRarity,
      long bridgeId,
      @Nullable String creator
   ) throws NoSuchTemplateException, FailedException {
      return createItem(templateId, qualityLevel, posX, posY, rot, onSurface, material, aRarity, bridgeId, creator, (byte)0);
   }

   public static Item createItem(
      int templateId,
      float qualityLevel,
      float posX,
      float posY,
      float rot,
      boolean onSurface,
      byte material,
      byte aRarity,
      long bridgeId,
      @Nullable String creator,
      byte initialAuxData
   ) throws NoSuchTemplateException, FailedException {
      float height = 0.0F;

      try {
         height = Zones.calculateHeight(posX, posY, onSurface);
      } catch (NoSuchZoneException var17) {
         logger.log(
            Level.WARNING,
            "Could not calculate height for position: " + posX + ", " + posY + ", surfaced: " + onSurface + " due to " + var17.getMessage(),
            (Throwable)var17
         );
      }

      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Factory trying to create item with id " + templateId + " at " + posX + ", " + posY + ", " + height + ".");
      }

      ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
      if (material == 0) {
         material = template.getMaterial();
      }

      String name = generateName(template, material);
      Item toReturn = null;

      try {
         if (template.isRecycled) {
            toReturn = Itempool.getRecycledItem(templateId, qualityLevel);
            if (toReturn != null) {
               if (toReturn.isTemporary()) {
                  toReturn.clear(WurmId.getNextTempItemId(), creator, posX, posY, height, rot, "", name, qualityLevel, material, aRarity, bridgeId);
               } else {
                  toReturn.clear(toReturn.id, creator, posX, posY, height, rot, "", name, qualityLevel, material, aRarity, bridgeId);
               }
            }
         }

         if (toReturn == null) {
            if (template.isTemporary()) {
               toReturn = new TempItem(name, template, qualityLevel, posX, posY, height, rot, bridgeId, creator);
            } else {
               toReturn = new DbItem(name, template, qualityLevel, posX, posY, height, rot, material, aRarity, bridgeId, creator);
            }
         }

         try {
            if (toReturn.getTemplateId() == 385 || toReturn.getTemplateId() == 731) {
               toReturn.setAuxData((byte)(100 + initialAuxData));
            }

            Zone zone = Zones.getZone((int)posX >> 2, (int)posY >> 2, onSurface);
            zone.addItem(toReturn);
            if (toReturn.getTemplateId() == 385 || toReturn.getTemplateId() == 731) {
               toReturn.setAuxData(initialAuxData);
            }
         } catch (NoSuchZoneException var18) {
            logger.log(
               Level.WARNING,
               "Could not get Zone for position: " + posX + ", " + posY + ", surfaced: " + onSurface + " due to " + var18.getMessage(),
               (Throwable)var18
            );
         }
      } catch (IOException var19) {
         throw new FailedException(var19);
      }

      toReturn.setOwner(-10L, true);
      if (toReturn.isFire()) {
         toReturn.setTemperature((short)20000);
         Effect effect = EffectFactory.getInstance()
            .createFire(toReturn.getWurmId(), toReturn.getPosX(), toReturn.getPosY(), toReturn.getPosZ(), toReturn.isOnSurface());
         toReturn.addEffect(effect);
      }

      return toReturn;
   }

   public static boolean isMetalLump(int itemTemplateId) {
      for(int lumpId : metalLumpList) {
         if (lumpId == itemTemplateId) {
            return true;
         }
      }

      return false;
   }

   public static Optional<Item> createItemOptional(int itemTemplateId, float qualityLevel, String creator) {
      try {
         return Optional.of(createItem(itemTemplateId, qualityLevel, creator));
      } catch (Exception var4) {
         var4.printStackTrace();
         return Optional.empty();
      }
   }
}

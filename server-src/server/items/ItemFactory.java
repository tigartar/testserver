/*
 * Decompiled with CFR 0.152.
 */
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
import com.wurmonline.server.items.ContainerRestriction;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.DbStrings;
import com.wurmonline.server.items.InitialContainer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Itempool;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.TempItem;
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

public final class ItemFactory
implements MiscConstants,
ItemTypes,
ItemMaterials {
    private static final Logger logger = Logger.getLogger(ItemFactory.class.getName());
    private static final String deleteItemData = "delete from ITEMDATA where WURMID=?";
    private static DbStrings dbstrings;
    public static int[] metalLumpList;

    private ItemFactory() {
    }

    @Nonnull
    public static Item createItem(int templateId, float qualityLevel, byte material, byte aRarity, @Nullable String creator) throws FailedException, NoSuchTemplateException {
        return ItemFactory.createItem(templateId, qualityLevel, material, aRarity, -10L, creator);
    }

    public static Optional<Item> createItemOptional(int templateId, float qualityLevel, byte material, byte aRarity, @Nullable String creator) {
        try {
            return Optional.of(ItemFactory.createItem(templateId, qualityLevel, material, aRarity, creator));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static void createContainerRestrictions(Item item) {
        ItemTemplate template = item.getTemplate();
        if (template.getContainerRestrictions() != null && !template.isNoPut()) {
            for (ContainerRestriction cRest : template.getContainerRestrictions()) {
                boolean skipAdd = false;
                for (Item i : item.getItems()) {
                    if (i.getTemplateId() == 1392 && cRest.contains(i.getRealTemplateId())) {
                        skipAdd = true;
                        continue;
                    }
                    if (!cRest.contains(i.getTemplateId())) continue;
                    skipAdd = true;
                }
                if (skipAdd) continue;
                try {
                    Item tempSlotItem = ItemFactory.createItem(1392, 100.0f, item.getCreatorName());
                    tempSlotItem.setRealTemplate(cRest.getEmptySlotTemplateId());
                    tempSlotItem.setName(cRest.getEmptySlotName());
                    item.insertItem(tempSlotItem, true);
                }
                catch (FailedException | NoSuchTemplateException wurmServerException) {}
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    @Nonnull
    public static Item createItem(int templateId, float qualityLevel, byte material, byte aRarity, long bridgeId, @Nullable String creator) throws FailedException, NoSuchTemplateException {
        template = ItemTemplateFactory.getInstance().getTemplate(templateId);
        if (material == 0) {
            material = template.getMaterial();
        }
        name = ItemFactory.generateName(template, material);
        if (template.isTemporary()) {
            try {
                toReturn = new TempItem(name, template, qualityLevel, creator);
                if (!ItemFactory.logger.isLoggable(Level.FINEST)) ** GOTO lbl26
                ItemFactory.logger.finest("Creating tempitem: " + toReturn);
            }
            catch (IOException ex) {
                throw new FailedException(ex);
            }
        } else {
            try {
                if (template.isRecycled && (toReturn = Itempool.getRecycledItem(templateId, qualityLevel)) != null) {
                    if (toReturn.isTemporary()) {
                        toReturn.clear(WurmId.getNextTempItemId(), creator, 0.0f, 0.0f, 0.0f, 1.0f, "", name, qualityLevel, material, aRarity, bridgeId);
                    } else {
                        toReturn.clear(toReturn.id, creator, 0.0f, 0.0f, 0.0f, 1.0f, "", name, qualityLevel, material, aRarity, bridgeId);
                    }
                    return toReturn;
                }
                toReturn = new DbItem(-10L, name, template, qualityLevel, material, aRarity, bridgeId, creator);
                if (template.isCoin()) {
                    Server.getInstance().transaction(toReturn.getWurmId(), -10L, bridgeId, "new " + toReturn.getName(), template.getValue());
                }
            }
            catch (IOException iox) {
                throw new FailedException(iox);
            }
        }
lbl26:
        // 3 sources

        if (template.getInitialContainers() != null) {
            for (InitialContainer ic : template.getInitialContainers()) {
                icMaterial = ic.getMaterial() == 0 ? material : ic.getMaterial();
                subItem = ItemFactory.createItem(ic.getTemplateId(), Math.max(1.0f, qualityLevel), icMaterial, aRarity, creator);
                subItem.setName(ic.getName());
                toReturn.insertItem(subItem, true);
            }
        }
        if (toReturn != null) {
            ItemFactory.createContainerRestrictions(toReturn);
        }
        return toReturn;
    }

    public static Item createItem(int templateId, float qualityLevel, byte aRarity, @Nullable String creator) throws FailedException, NoSuchTemplateException {
        return ItemFactory.createItem(templateId, qualityLevel, (byte)0, aRarity, creator);
    }

    public static Optional<Item> createItemOptional(int templateId, float qualityLevel, byte aRarity, @Nullable String creator) {
        return ItemFactory.createItemOptional(templateId, qualityLevel, (byte)0, aRarity, creator);
    }

    @Nonnull
    public static Item createItem(int templateId, float qualityLevel, @Nullable String creator) throws FailedException, NoSuchTemplateException {
        return ItemFactory.createItem(templateId, qualityLevel, (byte)0, (byte)0, creator);
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
            if (template.isRecycled && (toReturn = Itempool.getRecycledItem(templateId, qualityLevel)) != null) {
                toReturn.clear(-10L, "", 0.0f, 0.0f, 0.0f, 0.0f, "", name, qualityLevel, template.getMaterial(), (byte)0, -10L);
                toReturn.setPlace(place);
            }
            if (toReturn == null) {
                toReturn = new TempItem(wurmId, place, name, template, qualityLevel, "");
            }
        }
        catch (IOException ex) {
            throw new FailedException(ex);
        }
        return toReturn;
    }

    @Nullable
    public static Item createInventory(long ownerId, short place, float qualityLevel) throws FailedException, NoSuchTemplateException {
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(0);
        Item toReturn = null;
        try {
            long wurmId = WurmId.getNextBodyPartId(ownerId, (byte)place, WurmId.getType(ownerId) == 0);
            if (template.isRecycled && (toReturn = Itempool.getRecycledItem(0, qualityLevel)) != null) {
                toReturn.clear(wurmId, "", 0.0f, 0.0f, 0.0f, 0.0f, "", "inventory", qualityLevel, template.getMaterial(), (byte)0, -10L);
            }
            if (toReturn == null) {
                toReturn = new TempItem(wurmId, place, "inventory", template, qualityLevel, "");
            }
        }
        catch (IOException ex) {
            throw new FailedException(ex);
        }
        return toReturn;
    }

    public static Item loadItem(long id) throws NoSuchItemException, Exception {
        DbItem item = null;
        if (WurmId.getType(id) != 2 && WurmId.getType(id) != 19 && WurmId.getType(id) != 20) {
            throw new NoSuchItemException("Temporary item.");
        }
        item = new DbItem(id);
        return item;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
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
            ps = dbcon.prepareStatement(deleteItemData);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
            ps.setLong(1, id);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to decay item with id " + id, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearData(long id) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(deleteItemData);
            ps.setLong(1, id);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
            ps.setLong(1, id);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to decay item with id " + id, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public static Item createItem(int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte rarity, long bridgeId, @Nullable String creator) throws NoSuchTemplateException, FailedException {
        return ItemFactory.createItem(templateId, qualityLevel, posX, posY, rot, onSurface, (byte)0, rarity, bridgeId, creator);
    }

    public static Item createItem(int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte material, byte aRarity, long bridgeId, @Nullable String creator) throws NoSuchTemplateException, FailedException {
        return ItemFactory.createItem(templateId, qualityLevel, posX, posY, rot, onSurface, material, aRarity, bridgeId, creator, (byte)0);
    }

    public static Item createItem(int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte material, byte aRarity, long bridgeId, @Nullable String creator, byte initialAuxData) throws NoSuchTemplateException, FailedException {
        float height = 0.0f;
        try {
            height = Zones.calculateHeight(posX, posY, onSurface);
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "Could not calculate height for position: " + posX + ", " + posY + ", surfaced: " + onSurface + " due to " + nsz.getMessage(), nsz);
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Factory trying to create item with id " + templateId + " at " + posX + ", " + posY + ", " + height + ".");
        }
        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
        if (material == 0) {
            material = template.getMaterial();
        }
        String name = ItemFactory.generateName(template, material);
        Item toReturn = null;
        try {
            if (template.isRecycled && (toReturn = Itempool.getRecycledItem(templateId, qualityLevel)) != null) {
                if (toReturn.isTemporary()) {
                    toReturn.clear(WurmId.getNextTempItemId(), creator, posX, posY, height, rot, "", name, qualityLevel, material, aRarity, bridgeId);
                } else {
                    toReturn.clear(toReturn.id, creator, posX, posY, height, rot, "", name, qualityLevel, material, aRarity, bridgeId);
                }
            }
            if (toReturn == null) {
                toReturn = template.isTemporary() ? new TempItem(name, template, qualityLevel, posX, posY, height, rot, bridgeId, creator) : new DbItem(name, template, qualityLevel, posX, posY, height, rot, material, aRarity, bridgeId, creator);
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
            }
            catch (NoSuchZoneException sex) {
                logger.log(Level.WARNING, "Could not get Zone for position: " + posX + ", " + posY + ", surfaced: " + onSurface + " due to " + sex.getMessage(), sex);
            }
        }
        catch (IOException ex) {
            throw new FailedException(ex);
        }
        toReturn.setOwner(-10L, true);
        if (toReturn.isFire()) {
            toReturn.setTemperature((short)20000);
            Effect effect = EffectFactory.getInstance().createFire(toReturn.getWurmId(), toReturn.getPosX(), toReturn.getPosY(), toReturn.getPosZ(), toReturn.isOnSurface());
            toReturn.addEffect(effect);
        }
        return toReturn;
    }

    public static boolean isMetalLump(int itemTemplateId) {
        for (int lumpId : metalLumpList) {
            if (lumpId != itemTemplateId) continue;
            return true;
        }
        return false;
    }

    public static Optional<Item> createItemOptional(int itemTemplateId, float qualityLevel, String creator) {
        try {
            return Optional.of(ItemFactory.createItem(itemTemplateId, qualityLevel, creator));
        }
        catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    static {
        metalLumpList = new int[]{46, 221, 223, 205, 47, 220, 49, 44, 45, 48, 837, 698, 694, 1411};
    }
}


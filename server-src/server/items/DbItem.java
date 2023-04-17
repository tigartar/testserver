/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.items.ContainerRestriction;
import com.wurmonline.server.items.DbStrings;
import com.wurmonline.server.items.FrozenItemDbStrings;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemData;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Itempool;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.ItemDamageDatabaseUpdatable;
import com.wurmonline.server.utils.ItemDamageDatabaseUpdater;
import com.wurmonline.server.utils.ItemLastOwnerDatabaseUpdatable;
import com.wurmonline.server.utils.ItemLastOwnerDatabaseUpdater;
import com.wurmonline.server.utils.ItemOwnerDatabaseUpdatable;
import com.wurmonline.server.utils.ItemOwnerDatabaseUpdater;
import com.wurmonline.server.utils.ItemParentDatabaseUpdatable;
import com.wurmonline.server.utils.ItemParentDatabaseUpdater;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbItem
extends Item
implements ItemTypes,
MiscConstants,
ItemMaterials {
    private static final Logger logger = Logger.getLogger(DbItem.class.getName());
    private static PreparedStatement lastmPS = null;
    private static int lastmPSCount = 0;
    public static int overallLastmPSCount = 0;
    private static PreparedStatement lastDmgPS = null;
    private static int lastDmgPSCount = 0;
    public static int overallDmgPSCount = 0;
    private DbStrings dbstrings;
    private static final ItemDamageDatabaseUpdater itemDamageDatabaseUpdater = new ItemDamageDatabaseUpdater("Item Database Damage Updater", Constants.numberOfDbItemDamagesToUpdateEachTime);
    private static final ItemOwnerDatabaseUpdater itemOwnerDatabaseUpdater = new ItemOwnerDatabaseUpdater("Item Database Owner Updater", Constants.numberOfDbItemOwnersToUpdateEachTime);
    private static final ItemLastOwnerDatabaseUpdater itemLastOwnerDatabaseUpdater = new ItemLastOwnerDatabaseUpdater("Item Database Last Owner Updater", Constants.numberOfDbItemOwnersToUpdateEachTime);
    private static final ItemParentDatabaseUpdater itemParentDatabaseUpdater = new ItemParentDatabaseUpdater("Item Database Parent Updater", Constants.numberOfDbItemOwnersToUpdateEachTime);

    DbItem(long wurmId, String _name, ItemTemplate _template, float _qualityLevel, byte _material, byte aRarity, long bridgeId, String _creator) throws IOException {
        super(wurmId, _name, _template, _qualityLevel, _material, aRarity, bridgeId, _creator);
    }

    DbItem(String _name, ItemTemplate _template, float _qualityLevel, float x, float y, float z, float rot, byte _material, byte aRarity, long bridgeId, String _creator) throws IOException {
        super(_name, _template, _qualityLevel, x, y, z, rot, _material, aRarity, bridgeId, _creator);
    }

    public DbItem(long aId) throws Exception {
        this.id = aId;
        this.load(false);
    }

    public DbItem(long aId, boolean frozen) throws Exception {
        this.id = aId;
        this.load(frozen);
    }

    public DbItem(long wurmId, String aName, short aPlace, ItemTemplate aTemplate, float aQualityLevel, String aCreator) throws IOException {
        this(wurmId, aName, aTemplate, aQualityLevel, 1, 0, -10L, aCreator);
        this.setPlace(aPlace);
    }

    public DbItem(long wid, ItemTemplate templ, String nam, long last, float ql, float origQl, int sizex, int sizey, int sizez, float posx, float posy, float posz, float rot, long parentid, long ownerid, int zoneid, float dam, int w, byte mat, long lid, short plac, int pric, short temper, String desc, byte blesser, byte enchant, boolean bank, long lastOwnerId, byte auxdata, long created, byte createState, int rTemplate, boolean wornArmour, int _color, int _color2, boolean _female, boolean _mailed, boolean _transferred, String _creator, boolean _hidden, byte mailedTimes, byte rarebyte, long bridgeId, int aSettings, boolean _placedOnParent, DbStrings dbStrings) {
        try {
            this.id = wid;
            this.template = templ;
            this.dbstrings = dbStrings;
            this.name = nam;
            this.lastMaintained = Math.min(WurmCalendar.currentTime, last);
            this.qualityLevel = ql;
            this.originalQualityLevel = origQl;
            int[] sizes = new int[]{sizex, sizey, sizez};
            Arrays.sort(sizes);
            this.sizeX = sizes[0];
            this.sizeY = sizes[1];
            this.sizeZ = sizes[2];
            this.posX = posx;
            this.posY = posy;
            this.posZ = posz;
            this.rotation = rot;
            this.parentId = parentid;
            this.ownerId = ownerid;
            this.zoneId = zoneid;
            this.damage = dam;
            this.price = pric;
            this.weight = w;
            this.material = mat;
            this.lockid = lid;
            this.place = plac;
            this.temperature = temper;
            this.description = desc;
            this.bless = blesser;
            this.enchantment = enchant;
            this.banked = bank;
            this.lastOwner = lastOwnerId;
            this.auxbyte = auxdata;
            this.creationDate = created;
            this.creationState = createState;
            this.realTemplate = rTemplate;
            this.wornAsArmour = wornArmour;
            this.color = _color;
            this.color2 = _color2;
            this.female = _female;
            this.mailed = _mailed;
            this.mailTimes = mailedTimes;
            this.transferred = _transferred;
            this.creator = _creator;
            this.hidden = _hidden;
            this.rarity = rarebyte;
            this.onBridge = bridgeId;
            this.setSettings(aSettings);
            this.placedOnParent = _placedOnParent;
            if (templ.hasData()) {
                this.data = Items.getItemData(this.id);
            }
            if (templ.canHaveInscription()) {
                this.inscription = Items.getItemInscriptionData(this.id);
            }
            this.setOwnerStuff(templ);
            if (templ.isLock()) {
                this.loadKeys();
            }
            if (templ.getTemplateId() == 74 && this.temperature > 200) {
                this.temperature = (short)10000;
            }
            if (templ.getTemplateId() == 1172) {
                this.setInternalVolumeFromAuxByte();
            }
            if (this.getSpellEffects() != null) {
                if (this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_GLOW) > 1.0f) {
                    this.setLightOverride(true);
                    this.setIsAlwaysLit(true);
                } else {
                    this.setLightOverride(false);
                    this.setIsAlwaysLit(false);
                }
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        if (this.template.isRecycled && this.banked && this.ownerId == -10L) {
            Itempool.addRecycledItem(this);
        } else {
            Items.putItem(this);
        }
        ItemFactory.createContainerRestrictions(this);
    }

    @Override
    public void setOwnerStuff(ItemTemplate templ) {
        if (this.ownerId != -10L) {
            try {
                ArmourTemplate armour;
                Creature owner = Server.getInstance().getCreature(this.ownerId);
                if (templ.isBodyPart()) {
                    if (this.getAuxData() == 100) {
                        owner.addCarriedWeight(this.getWeightGrams());
                    }
                } else {
                    owner.addCarriedWeight(this.getWeightGrams());
                }
                if (this.isKey()) {
                    owner.addKey(this, true);
                }
                if (this.wornAsArmour && (armour = ArmourTemplate.getArmourTemplate(this.template.templateId)) != null) {
                    float moveModChange = armour.getMoveModifier();
                    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
                        moveModChange *= ArmourTemplate.getMaterialMovementModifier(this.getMaterial());
                    } else if (Servers.localServer.isChallengeOrEpicServer()) {
                        if (this.getMaterial() == 57 || this.getMaterial() == 67) {
                            moveModChange *= 0.9f;
                        } else if (this.getMaterial() == 56) {
                            moveModChange *= 0.95f;
                        }
                    }
                    owner.getMovementScheme().armourMod.setModifier(owner.getMovementScheme().armourMod.getModifier() - (double)moveModChange);
                }
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public static void clearBatches() {
        try {
            int[] x;
            if (lastmPS != null) {
                x = lastmPS.executeBatch();
                logger.log(Level.INFO, "Saved last maintained batch size " + x.length);
                DbUtilities.closeDatabaseObjects(lastmPS, null);
                lastmPS = null;
                lastmPSCount = 0;
            }
            if (lastDmgPS != null) {
                x = lastDmgPS.executeBatch();
                logger.log(Level.INFO, "Saved last damage batch size " + x.length);
                DbUtilities.closeDatabaseObjects(lastDmgPS, null);
                lastDmgPS = null;
                lastDmgPSCount = 0;
            }
        }
        catch (Exception iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean exists(Connection dbcon) {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(this.dbstrings.loadItem());
            ps.setLong(1, this.id);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to check if item exists.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return false;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void create(float aQualityLevel, long aCreationDate) throws IOException {
        PreparedStatement ps;
        Connection dbcon;
        block4: {
            dbcon = null;
            ps = null;
            try {
                this.dbstrings = DbItem.getDbStrings(this.template.getTemplateId());
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.createItem());
                ps.setLong(1, this.id);
                ps.setInt(2, this.template.getTemplateId());
                ps.setString(3, this.name);
                ps.setFloat(4, aQualityLevel);
                ps.setFloat(5, this.originalQualityLevel);
                this.lastMaintained = aCreationDate;
                this.creationDate = aCreationDate;
                ps.setLong(6, aCreationDate);
                ps.setLong(7, -10L);
                this.sizeX = this.template.getSizeX();
                this.sizeY = this.template.getSizeY();
                this.sizeZ = this.template.getSizeZ();
                ps.setInt(8, this.sizeX);
                ps.setInt(9, this.sizeY);
                ps.setInt(10, this.sizeZ);
                ps.setInt(11, -10);
                ps.setFloat(12, 0.0f);
                ps.setFloat(13, 1.0f);
                ps.setLong(14, this.parentId);
                ps.setInt(15, this.template.getWeightGrams());
                ps.setByte(16, this.material);
                ps.setLong(17, this.lockid);
                ps.setString(18, this.description);
                ps.setLong(19, aCreationDate);
                ps.setByte(20, this.rarity);
                ps.setString(21, this.creator);
                ps.setLong(22, this.onBridge);
                ps.setInt(23, this.getSettings().getPermissions());
                ps.executeUpdate();
                if (!this.isLock()) break block4;
                this.createLock();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to create/update item with id " + this.id, sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void load() throws Exception {
        this.load(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load(boolean frozen) throws Exception {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block17: {
            dbcon = null;
            ps = null;
            rs = null;
            try {
                this.dbstrings = frozen ? FrozenItemDbStrings.getInstance() : DbItem.getDbStringsByWurmId(this.id);
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.loadItem());
                ps.setLong(1, this.id);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    throw new NoSuchItemException("No item with id " + this.id);
                }
                this.template = ItemTemplateFactory.getInstance().getTemplate(rs.getInt("TEMPLATEID"));
                this.lastMaintained = rs.getLong("LASTMAINTAINED");
                this.qualityLevel = rs.getFloat("QUALITYLEVEL");
                this.originalQualityLevel = rs.getFloat("ORIGINALQUALITYLEVEL");
                this.sizeX = rs.getInt("SIZEX");
                this.sizeY = rs.getInt("SIZEY");
                this.sizeZ = rs.getInt("SIZEZ");
                this.posX = rs.getFloat("POSX");
                this.posY = rs.getFloat("POSY");
                this.posZ = rs.getFloat("POSZ");
                this.rotation = rs.getFloat("ROTATION");
                this.parentId = rs.getLong("PARENTID");
                this.ownerId = rs.getLong("OWNERID");
                this.lastOwner = rs.getLong("LASTOWNERID");
                this.zoneId = rs.getInt("ZONEID");
                this.name = rs.getString("NAME");
                this.damage = rs.getFloat("DAMAGE");
                this.weight = rs.getInt("WEIGHT");
                this.material = rs.getByte("MATERIAL");
                this.lockid = rs.getLong("LOCKID");
                this.place = rs.getShort("PLACE");
                this.price = rs.getInt("PRICE");
                this.temperature = rs.getShort("TEMPERATURE");
                this.description = rs.getString("DESCRIPTION");
                this.bless = rs.getByte("BLESS");
                this.enchantment = rs.getByte("ENCHANT");
                this.banked = rs.getBoolean("BANKED");
                this.auxbyte = rs.getByte("AUXDATA");
                this.color = rs.getInt("COLOR");
                this.color2 = rs.getInt("COLOR2");
                this.female = rs.getBoolean("FEMALE");
                this.mailed = rs.getBoolean("MAILED");
                this.hidden = rs.getBoolean("HIDDEN");
                this.realTemplate = rs.getInt("REALTEMPLATE");
                this.creationState = rs.getByte("CREATIONSTATE");
                this.creationDate = rs.getLong("CREATIONDATE");
                this.wornAsArmour = rs.getBoolean("WORNARMOUR");
                this.transferred = rs.getBoolean("TRANSFERRED");
                this.creator = rs.getString("CREATOR");
                this.mailTimes = rs.getByte("MAILTIMES");
                this.onBridge = rs.getLong("ONBRIDGE");
                this.rarity = rs.getByte("RARITY");
                this.setSettings(rs.getInt("SETTINGS"));
                DbUtilities.closeDatabaseObjects(ps, rs);
                logger.log(Level.WARNING, this.name + " this load should not happen anymore. " + this.id + ".", new Exception());
                if (this.hasData()) {
                    this.data = Items.getItemData(this.id);
                }
                if (this.canHaveInscription()) {
                    this.inscription = Items.getItemInscriptionData(this.id);
                }
                if (this.ownerId != -10L) {
                    try {
                        Creature owner = Server.getInstance().getCreature(this.ownerId);
                        if (this.isBodyPart()) {
                            if (this.getAuxData() == 100) {
                                owner.addCarriedWeight(this.getWeightGrams());
                            }
                        } else {
                            owner.addCarriedWeight(this.getWeightGrams());
                        }
                        if (this.isKey()) {
                            owner.addKey(this, true);
                        }
                    }
                    catch (NoSuchCreatureException owner) {
                    }
                    catch (NoSuchPlayerException owner) {
                        // empty catch block
                    }
                }
                if (this.isLock()) {
                    this.loadKeys();
                }
                if (this.template.getTemplateId() == 74 && this.temperature > 200) {
                    this.temperature = (short)10000;
                }
                if (!this.isHollow()) break block17;
                this.items = this.getItems();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to load item with id " + this.id, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void loadEffects() {
        EffectFactory.getInstance().getEffectsFor(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadKeys() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.getLock());
            ps.setLong(1, this.id);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.locked = rs.getBoolean("LOCKED");
            } else {
                this.createLock();
            }
            this.getKeys();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to load keys for lock with id " + this.id, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void createLock() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.createLock());
            ps.setLong(1, this.id);
            ps.setBoolean(2, this.locked);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save keys for lock with id " + this.id, ex);
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
    @Override
    public void addNewKey(long keyId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.addKey());
            ps.setLong(1, this.id);
            ps.setLong(2, keyId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to add key for lock with id " + this.id, ex);
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
    public void getKeys() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            this.keys = new HashSet();
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.getKeys());
            ps.setLong(1, this.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.keys.add(new Long(rs.getLong("KEYID")));
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to load keys for lock with id " + this.id, ex);
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
    @Override
    public void removeNewKey(long keyId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.removeKey());
            ps.setLong(1, keyId);
            ps.setLong(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to remove key for lock with id " + this.id, ex);
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
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean lockExists(Connection dbcon) {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(this.dbstrings.getLock());
            ps.setLong(1, this.id);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to check if lock exists:", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            return false;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLockId(long lid) {
        if (lid != this.lockid) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.lockid = lid;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setLockId());
                ps.setLong(1, this.lockid);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    @Override
    public long getLockId() {
        return this.lockid;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setZoneId(int zid, boolean isOnSurface) {
        this.surfaced = isOnSurface;
        if (this.isHollow() && this.items != null) {
            for (Item item : this.items) {
                item.setSurfaced(this.surfaced);
            }
        }
        if (zid != this.zoneId) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.zoneId = zid;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setZoneId());
                ps.setInt(1, this.zoneId);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set Zone ID to " + this.zoneId + " for item " + this.id, sqx);
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
    }

    @Override
    public int getZoneId() {
        if (this.parentId != -10L && Items.isItemLoaded(this.parentId)) {
            try {
                Item parent = Items.getItem(this.parentId);
                return parent.getZoneId();
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "This REALLY shouldn't happen! parentId: " + this.parentId, nsi);
            }
        }
        return this.zoneId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setParentId(long pid, boolean isOnSurface) {
        this.surfaced = isOnSurface;
        if (this.parentId != pid) {
            PreparedStatement ps;
            Connection dbcon;
            block20: {
                if (this.isCoin() && this.getValue() >= 1000000) {
                    logger.log(Level.INFO, "COINLOG PID " + pid + ", " + this.getWurmId() + " owner " + this.ownerId + " banked " + this.banked + " mailed=" + this.mailed, new Exception());
                }
                dbcon = null;
                ps = null;
                try {
                    if (pid == -10L) {
                        if (this.watchers != null) {
                            for (Creature watcher : this.watchers) {
                                watcher.getCommunicator().sendRemoveFromInventory(this);
                                watcher.getCommunicator().sendCloseInventoryWindow(this.getWurmId());
                            }
                        }
                        this.watchers = null;
                    } else {
                        try {
                            Item parent = Items.getItem(pid);
                            if (this.ownerId != parent.getOwnerId()) {
                                if (parent.getPosX() != this.getPosX() || parent.getPosY() != this.getPosY()) {
                                    this.setPosXYZ(parent.getPosX(), parent.getPosY(), parent.getPosZ());
                                }
                                for (Item i : this.getItems()) {
                                    if (!i.isPlacedOnParent() || !i.isHollow() || i.getWatcherSet() == null) continue;
                                    for (Creature watcher : i.getWatcherSet()) {
                                        watcher.getCommunicator().sendCloseInventoryWindow(i.getWurmId());
                                    }
                                }
                            } else if (this.recursiveParentCheck() != null && this.getTopParentOrNull().getTemplateId() != 0 && this.watchers != null) {
                                for (Creature watcher : this.watchers) {
                                    watcher.getCommunicator().sendRemoveFromInventory(this);
                                    watcher.getCommunicator().sendCloseInventoryWindow(this.getWurmId());
                                }
                            }
                        }
                        catch (NoSuchItemException parent) {
                            // empty catch block
                        }
                    }
                    this.parentId = pid;
                    if (WurmId.getType(this.parentId) == 6) break block20;
                    if (Constants.useScheduledExecutorToUpdateItemParentInDatabase) {
                        ItemParentDatabaseUpdatable lUpdatable = new ItemParentDatabaseUpdatable(this.id, this.parentId, this.dbstrings.setParentId());
                        itemParentDatabaseUpdater.addToQueue(lUpdatable);
                        break block20;
                    }
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setParentId());
                    ps.setLong(1, pid);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to set parentId to " + pid + " for item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public long getParentId() {
        return this.parentId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTemplateId(int tid) {
        if (this.template != null && this.template.getTemplateId() != tid) {
            try {
                boolean skipNameUpdate = this.isUnenchantedTurret();
                this.template = ItemTemplateFactory.getInstance().getTemplate(tid);
                if (this.template.getMaterial() != 0 && (this.template.isTransmutable || this.material == 0)) {
                    this.setMaterial(this.template.getMaterial());
                }
                if (this.template.isDragonArmour) {
                    if (this.name.startsWith("unfinished")) {
                        StringTokenizer st = new StringTokenizer(this.name);
                        st.nextToken();
                        String n = st.nextToken();
                        while (st.hasMoreTokens()) {
                            n = n + " " + st.nextToken();
                        }
                        this.setName(n, !skipNameUpdate);
                    } else {
                        this.setName(ItemFactory.generateName(this.template, this.getMaterial()), !skipNameUpdate);
                    }
                } else {
                    this.setName(ItemFactory.generateName(this.template, this.getMaterial()), !skipNameUpdate);
                }
                this.setSizes(this.template.getSizeX(), this.template.getSizeY(), this.template.getSizeZ());
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "Tried to set item " + this.id + " to templateid " + tid + " which doesn't exist.", nst);
            }
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.setTemplateId());
            ps.setInt(1, tid);
            ps.setLong(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set templateId to " + tid + " for item " + this.id, sqx);
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
        this.updatePos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int getTemplateId() {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        int toReturn;
        block5: {
            if (this.template != null) {
                return this.template.getTemplateId();
            }
            toReturn = -10;
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.getTemplateId());
                ps.setLong(1, this.id);
                rs = ps.executeQuery();
                if (!rs.next()) break block5;
                toReturn = rs.getInt("TEMPLATEID");
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to get template ID for item " + this.id, sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return toReturn;
    }

    @Override
    public boolean setInscription(String aInscription, String inscriber) {
        return this.setInscription(aInscription, inscriber, 0);
    }

    @Override
    public boolean setInscription(String aInscription, String inscriber, int penColour) {
        if (this.inscription == null) {
            this.inscription = new InscriptionData(this.id, "", inscriber, penColour);
        }
        if (this.inscription.getInscription().compareTo(aInscription) != 0) {
            VolaTile t;
            this.inscription.setInscription(aInscription);
            this.saveInscription();
            if (this.watchers != null) {
                for (Creature watcher : this.watchers) {
                    watcher.getCommunicator().sendUpdateInventoryItem(this);
                }
            } else if (this.zoneId > 0 && this.parentId == -10L && (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) != null) {
                t.renameItem(this);
            }
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    boolean saveInscription() {
        block6: {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                if (this.inscription == null) break block6;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    if (this.inscriptionExists(dbcon)) {
                        ps = dbcon.prepareStatement(this.dbstrings.setInscription());
                        ps.setString(1, this.inscription.getInscription());
                        ps.setLong(2, this.id);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                        break block5;
                    }
                    this.createInscriptionDataEntry(dbcon);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return true;
        }
        return false;
    }

    @Override
    public InscriptionData getInscription() {
        return this.inscription;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setDescription(String newdesc) {
        block7: {
            PreparedStatement ps;
            Connection dbcon;
            block6: {
                if (this.description.equals(newdesc = newdesc.substring(0, Math.min(255, newdesc.length())))) break block7;
                dbcon = null;
                ps = null;
                try {
                    VolaTile t;
                    this.description = newdesc;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setDescription());
                    ps.setString(1, this.description);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            watcher.getCommunicator().sendUpdateInventoryItem(this);
                        }
                        break block6;
                    }
                    if (this.zoneId <= 0 || this.parentId != -10L || (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) == null) break block6;
                    t.renameItem(this);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to set Description to '" + this.description + "' for item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return true;
        }
        return false;
    }

    @Override
    public void setName(String newName) {
        this.setName(newName, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setName(String newname, boolean sendUpdate) {
        if (!this.name.equals(newname = newname.substring(0, Math.min(39, newname.length())))) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    VolaTile t;
                    this.name = newname;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setName());
                    ps.setString(1, this.name);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (!sendUpdate) break block7;
                    if (this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            watcher.getCommunicator().sendUpdateInventoryItem(this);
                        }
                        break block7;
                    }
                    if (this.zoneId <= 0 || this.parentId != -10L || (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) == null) break block7;
                    t.renameItem(this);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to set name to '" + this.name + "' for item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public String getDescription() {
        if (this.template.descIsExam) {
            return "";
        }
        if (this.getTemplateId() == 1309 && this.isSealedByPlayer()) {
            return Delivery.getContainerDescription(this.getWurmId());
        }
        return this.description;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPlace(short pl) {
        if (pl != this.place) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.place = pl;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setPlace());
                ps.setShort(1, this.place);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id + " and place: " + pl, sqx);
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
    }

    @Override
    public short getPlace() {
        return this.place;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOriginalQualityLevel(float qlevel) {
        if (qlevel != this.originalQualityLevel) {
            this.originalQualityLevel = qlevel;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setOriginalQualityLevel());
                ps.setFloat(1, this.originalQualityLevel);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set original QL to " + this.originalQualityLevel + " for item " + this.id, sqx);
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
    }

    @Override
    public float getOriginalQualityLevel() {
        return this.originalQualityLevel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setQualityLevel(float qlevel) {
        boolean decayed;
        block18: {
            block19: {
                decayed = false;
                if (qlevel == this.qualityLevel && !(qlevel <= 0.0f)) break block18;
                boolean belowQL10 = false;
                if (this.isBoat() && this.getCurrentQualityLevel() < 10.0f) {
                    belowQL10 = true;
                }
                this.qualityLevel = Math.min(100.0f, qlevel);
                if (this.checkDecay()) break block19;
                if (this.parentId != -10L) {
                    if (this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            watcher.getCommunicator().sendUpdateInventoryItem(this);
                        }
                    }
                } else if (!(!this.isUseOnGroundOnly() || this.isDomainItem() || this.isKingdomMarker() || this.hideAddToCreationWindow() || this.isNoDrop())) {
                    if (this.getTopParent() == this.getWurmId() && this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            watcher.getCommunicator().sendUpdateGroundItem(this);
                        }
                    }
                } else if (this.isUnfinished() && this.watchers != null) {
                    for (Creature watcher : this.watchers) {
                        watcher.getCommunicator().sendUpdateGroundItem(this);
                    }
                }
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setQualityLevel());
                    ps.setFloat(1, this.qualityLevel);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to set QL to " + this.qualityLevel + " for item " + this.id, sqx);
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
                if (belowQL10 && this.getCurrentQualityLevel() > 10.0f && this.isBoat()) {
                    this.updateIfGroundItem();
                }
                if (this.getTemplate().getInitialContainers() != null) {
                    for (Item item : this.getItemsAsArray()) {
                        item.setQualityLevel(qlevel);
                    }
                }
                break block18;
            }
            decayed = true;
        }
        return decayed;
    }

    @Override
    public float getQualityLevel() {
        return this.qualityLevel;
    }

    @Override
    public void setLastMaintained(long last) {
        if (last != this.lastMaintained) {
            try {
                this.lastMaintained = last;
                if (lastmPS == null) {
                    Connection dbcon = DbConnector.getItemDbCon();
                    lastmPS = Server.getInstance().isPS() ? dbcon.prepareStatement(this.dbstrings.setLastMaintainedOld()) : dbcon.prepareStatement(this.dbstrings.setLastMaintained());
                }
                lastmPS.setLong(1, this.lastMaintained);
                lastmPS.setLong(2, this.id);
                lastmPS.addBatch();
                ++overallLastmPSCount;
                ++this.template.maintUpdates;
                if (++lastmPSCount > 700) {
                    long checkms = System.currentTimeMillis();
                    lastmPS.executeBatch();
                    DbUtilities.closeDatabaseObjects(lastmPS, null);
                    lastmPS = null;
                    if (System.currentTimeMillis() - checkms > 300L || logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.WARNING, "SaveItemLastMaintained batch took " + (System.currentTimeMillis() - checkms) + " ms for " + lastmPSCount + " updates.");
                    }
                    lastmPSCount = 0;
                }
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, "Failed to set lastMaintained to " + this.lastMaintained + " for item " + this.id, sqx);
            }
        }
    }

    @Override
    public long getLastMaintained() {
        return this.lastMaintained;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public boolean setOwnerId(long newOwnerId) {
        if (this.ownerId == newOwnerId) return true;
        if (Constants.useScheduledExecutorToUpdateItemOwnerInDatabase) {
            this.ownerId = newOwnerId;
            ItemOwnerDatabaseUpdatable lUpdatable = new ItemOwnerDatabaseUpdatable(this.id, this.ownerId, this.dbstrings.setOwnerId());
            itemOwnerDatabaseUpdater.addToQueue(lUpdatable);
            return true;
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.ownerId = newOwnerId;
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.setOwnerId());
            ps.setLong(1, this.ownerId);
            ps.setLong(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            boolean bl;
            try {
                logger.log(Level.WARNING, "Failed to set ownerId to " + this.ownerId + " for item " + this.id, sqx);
                bl = false;
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return bl;
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void setLastOwnerId(long oid) {
        PreparedStatement ps;
        Connection dbcon;
        block7: {
            if (this.lastOwner == oid) return;
            if (Constants.useScheduledExecutorToUpdateItemLastOwnerInDatabase) {
                this.lastOwner = oid;
                ItemLastOwnerDatabaseUpdatable lUpdatable = new ItemLastOwnerDatabaseUpdatable(this.id, this.lastOwner, this.dbstrings.setLastOwnerId());
                itemLastOwnerDatabaseUpdater.addToQueue(lUpdatable);
                if (this.template.getInitialContainers() == null) return;
                for (Item ic : this.getItems()) {
                    if (ic.getLastOwnerId() == this.lastOwner) continue;
                    ic.setLastOwnerId(this.lastOwner);
                }
                return;
            }
            dbcon = null;
            ps = null;
            try {
                this.lastOwner = oid;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setLastOwnerId());
                ps.setLong(1, this.lastOwner);
                ps.setLong(2, this.id);
                ps.executeUpdate();
                if (this.template.getInitialContainers() == null) break block7;
                for (Item ic : this.getItems()) {
                    if (ic.getLastOwnerId() == this.lastOwner) continue;
                    ic.setLastOwnerId(this.lastOwner);
                }
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set last ownerId to " + this.lastOwner + " for item " + this.id, sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                return;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        return;
    }

    @Override
    public long getOwnerId() {
        return this.ownerId;
    }

    public void maybeUpdateKeepnetPos() {
        Optional<Item> keepNet;
        if (this.getExtra() != -1L && (this.getTemplateId() == 491 || this.getTemplateId() == 490) && (keepNet = Items.getItemOptional(this.getExtra())).isPresent()) {
            boolean switchLayers = keepNet.get().isOnSurface() != this.isOnSurface();
            keepNet.get().setPos(this.posX, this.posY, this.posZ, this.rotation, this.onBridge);
            for (Item subItem : keepNet.get().getItems()) {
                subItem.setPos(this.posX, this.posY, this.posZ, this.rotation, this.onBridge);
            }
            if (switchLayers) {
                keepNet.get().setSurfaced(this.isOnSurface());
            }
        }
    }

    @Override
    public void setPos(float _posX, float _posY, float _posZ, float _rot, long bridgeId) {
        if (this.posX != _posX || this.posY != _posY || this.posZ != _posZ || this.rotation != _rot || this.onBridge != bridgeId) {
            this.posX = _posX;
            this.posY = _posY;
            this.posZ = _posZ;
            this.rotation = _rot;
            this.onBridge = bridgeId;
            this.savePosition();
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPosXYZRotation(float _posX, float _posY, float _posZ, float _rot) {
        if (this.posX != _posX || this.posY != _posY || this.posZ != _posZ || this.rotation != _rot) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.posX = _posX;
                    this.posY = _posY;
                    this.posZ = _posZ;
                    this.rotation = _rot;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setPosXYZRotation());
                    ps.setFloat(1, this.posX);
                    ps.setFloat(2, this.posY);
                    ps.setFloat(3, this.posZ);
                    ps.setFloat(4, this.rotation);
                    ps.setLong(5, this.id);
                    ps.executeUpdate();
                    if (this.effects == null) break block7;
                    for (Effect effect : this.effects) {
                        effect.setPosX(this.posX);
                        effect.setPosY(this.posY);
                        effect.setPosZ(this.posZ);
                    }
                }
                catch (SQLException sqx) {
                    try {
                        if (Server.getMillisToShutDown() == Long.MIN_VALUE) {
                            Server.getInstance().startShutdown(5, "The server lost connection to the database. Shutting down ");
                        }
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPosXYZ(float _posX, float _posY, float _posZ) {
        if (this.posX != _posX || this.posY != _posY || this.posZ != _posZ) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.posX = _posX;
                    this.posY = _posY;
                    this.posZ = _posZ;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setPosXYZ());
                    ps.setFloat(1, this.posX);
                    ps.setFloat(2, this.posY);
                    ps.setFloat(3, this.posZ);
                    ps.setLong(4, this.id);
                    ps.executeUpdate();
                    if (this.effects == null) break block7;
                    for (Effect effect : this.effects) {
                        effect.setPosX(this.posX);
                        effect.setPosY(this.posY);
                        effect.setPosZ(this.posZ);
                    }
                }
                catch (SQLException sqx) {
                    try {
                        if (Server.getMillisToShutDown() == Long.MIN_VALUE) {
                            Server.getInstance().startShutdown(5, "The server lost connection to the database. Shutting down ");
                        }
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPosXY(float _posX, float _posY) {
        if (this.posX != _posX || this.posY != _posY) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.posX = _posX;
                    this.posY = _posY;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setPosXY());
                    ps.setFloat(1, this.posX);
                    ps.setFloat(2, this.posY);
                    ps.setLong(3, this.id);
                    ps.executeUpdate();
                    if (this.effects == null) break block7;
                    for (Effect effect : this.effects) {
                        effect.setPosX(this.posX);
                        effect.setPosY(this.posY);
                    }
                }
                catch (SQLException sqx) {
                    try {
                        if (Server.getMillisToShutDown() == Long.MIN_VALUE) {
                            Server.getInstance().startShutdown(5, "The server lost connection to the database. Shutting down ");
                        }
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPosX(float _posX) {
        if (this.posX != _posX) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.posX = _posX;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setPosX());
                    ps.setFloat(1, this.posX);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    if (this.effects == null) break block7;
                    for (Effect effect : this.effects) {
                        effect.setPosX(this.posX);
                    }
                }
                catch (SQLException sqx) {
                    try {
                        if (Server.getMillisToShutDown() == Long.MIN_VALUE) {
                            Server.getInstance().startShutdown(5, "The server lost connection to the database. Shutting down ");
                        }
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPosY(float posy) {
        if (this.posY != posy) {
            PreparedStatement ps;
            Connection dbcon;
            block6: {
                dbcon = null;
                ps = null;
                try {
                    this.posY = posy;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setPosY());
                    ps.setFloat(1, this.posY);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    if (this.effects == null) break block6;
                    for (Effect effect : this.effects) {
                        effect.setPosY(this.posY);
                    }
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPosZ(float posz) {
        if (this.isFloating()) {
            posz = Math.max(0.0f, posz);
        }
        if (this.posZ != posz) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.posZ = posz;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setPosZ());
                    ps.setFloat(1, this.posZ);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    if (this.effects == null) break block7;
                    for (Effect effect : this.effects) {
                        effect.setPosZ(this.posZ);
                    }
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        this.maybeUpdateKeepnetPos();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setRotation(float rot) {
        if (this.rotation != rot) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                dbcon = null;
                ps = null;
                try {
                    VolaTile t;
                    this.rotation = this.ladderRotate(rot);
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setRotation());
                    ps.setFloat(1, this.rotation);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    if (!this.isWind() || this.getParentId() != -10L || !this.isOnSurface() || (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) == null) break block5;
                    t.sendRotate(this, this.rotation);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTransferred(boolean trans) {
        if (this.transferred != trans) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.transferred = trans;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setTransferred());
                ps.setBoolean(1, this.transferred);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void savePosition() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.savePos());
            ps.setFloat(1, this.posX);
            ps.setFloat(2, this.posY);
            ps.setFloat(3, this.posZ);
            ps.setFloat(4, this.rotation);
            ps.setLong(5, this.onBridge);
            ps.setLong(6, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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

    @Override
    public float getRotation() {
        return this.rotation;
    }

    @Override
    public void checkSaveDamage() {
    }

    @Override
    public boolean setDamage(float dam) {
        float modifier = 1.0f;
        float difference = dam - this.damage;
        if (difference > 0.0f && this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_DAMAGETAKEN);
            difference *= modifier;
        }
        return this.setDamage(this.damage + difference, false);
    }

    @Override
    public boolean setDamage(float dam, boolean overrideIndestructible) {
        boolean destroyed = false;
        if (!overrideIndestructible && this.isIndestructible() && !this.isHugeAltar()) {
            return false;
        }
        this.lastMaintained = WurmCalendar.currentTime;
        if (!this.isBodyPartAttached() && (dam != this.damage || dam >= 100.0f)) {
            try {
                boolean belowQL10 = false;
                if (this.isBoat() && this.getCurrentQualityLevel() < 10.0f) {
                    belowQL10 = true;
                }
                boolean updateModel = false;
                if (this.isVisibleDecay()) {
                    if (dam >= 50.0f && this.damage < 50.0f) {
                        updateModel = true;
                    } else if (dam < 50.0f && this.damage >= 50.0f) {
                        updateModel = true;
                    } else if (dam < 25.0f && this.damage >= 25.0f) {
                        updateModel = true;
                    } else if (dam >= 25.0f && this.damage < 25.0f) {
                        updateModel = true;
                    }
                }
                this.damage = Math.max(0.0f, dam);
                if (!this.checkDecay()) {
                    if (this.parentId == -10L) {
                        if (!(!this.isUseOnGroundOnly() || this.isDomainItem() || this.isKingdomMarker() || this.hideAddToCreationWindow() || this.isNoDrop())) {
                            if (this.getTopParent() == this.getWurmId() && this.watchers != null) {
                                for (Creature watcher : this.watchers) {
                                    watcher.getCommunicator().sendUpdateGroundItem(this);
                                }
                            }
                        } else if (this.isUnfinished()) {
                            if (this.watchers != null) {
                                for (Creature watcher : this.watchers) {
                                    watcher.getCommunicator().sendUpdateGroundItem(this);
                                }
                            }
                        } else if (updateModel) {
                            this.updateModelNameOnGroundItem();
                        }
                    } else if (this.parentId != -10L) {
                        if (this.watchers != null) {
                            for (Creature watcher : this.watchers) {
                                watcher.getCommunicator().sendUpdateInventoryItem(this);
                            }
                        }
                    } else if (updateModel) {
                        this.updateModelNameOnGroundItem();
                    }
                } else {
                    destroyed = true;
                }
                if (!destroyed) {
                    VolaTile vt;
                    if (Constants.useScheduledExecutorToUpdateItemDamageInDatabase) {
                        ItemDamageDatabaseUpdatable lUpdatable = new ItemDamageDatabaseUpdatable(this.id, this.damage, this.lastMaintained, this.dbstrings.setDamageOld());
                        itemDamageDatabaseUpdater.addToQueue(lUpdatable);
                        ++overallDmgPSCount;
                    } else {
                        if (lastDmgPS == null) {
                            Connection dbcon = DbConnector.getItemDbCon();
                            lastDmgPS = Server.getInstance().isPS() ? dbcon.prepareStatement(this.dbstrings.setDamageOld()) : dbcon.prepareStatement(this.dbstrings.setDamage());
                        }
                        lastDmgPS.setFloat(1, this.damage);
                        lastDmgPS.setLong(2, this.lastMaintained);
                        lastDmgPS.setLong(3, this.id);
                        lastDmgPS.addBatch();
                        ++overallDmgPSCount;
                        ++this.template.damUpdates;
                        if (++lastDmgPSCount > 700) {
                            long checkms = System.currentTimeMillis();
                            lastDmgPS.executeBatch();
                            DbUtilities.closeDatabaseObjects(lastDmgPS, null);
                            lastDmgPS = null;
                            if (System.currentTimeMillis() - checkms > 300L || logger.isLoggable(Level.FINEST)) {
                                logger.log(Level.WARNING, "SaveItemDamage batch took " + (System.currentTimeMillis() - checkms) + " ms for " + lastDmgPSCount + " updates.");
                            }
                            lastDmgPSCount = 0;
                        }
                    }
                    if (belowQL10 && this.getCurrentQualityLevel() > 10.0f && this.isBoat()) {
                        this.updateModelNameOnGroundItem();
                    }
                    if (this.isPlanted() && !this.isRoadMarker() && (this.getDamage() > 70.0f || this.getCurrentQualityLevel() < 10.0f) && ((vt = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.surfaced)) == null || vt.getVillage() == null)) {
                        this.setIsPlanted(false);
                        logger.info("Item " + this.id + " just unplanted itself.");
                    }
                    if (this.damage > 0.0f) {
                        this.setIsFresh(false);
                    }
                    if (this.isBoat() && Vehicles.getVehicle(this).getPilotId() != -10L) {
                        Vehicle boat = Vehicles.getVehicle(this);
                        try {
                            Players.getInstance().getPlayer(boat.getPilotId()).getMovementScheme().addMountSpeed(boat.calculateNewBoatSpeed(false));
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {}
                    }
                }
            }
            catch (SQLException sqx) {
                logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
            }
        }
        return destroyed;
    }

    @Override
    public float getDamage() {
        return this.damage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLocked(boolean lock) {
        if (lock != this.locked) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.locked = lock;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setLocked());
                ps.setBoolean(1, this.locked);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTemperature(short temp) {
        block25: {
            if (this.isFood()) {
                temp = (short)Math.min(3500, temp);
            }
            if (this.temperature != temp) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    boolean flag = this.isOnFire();
                    int diff = this.temperature - temp;
                    this.tempChange = diff > 400 || diff < -400 ? 400 : (diff > 0 ? (int)((byte)Math.min(400, this.tempChange + diff)) : (int)((byte)Math.max(-400, this.tempChange + diff)));
                    this.temperature = temp;
                    boolean flag2 = this.isOnFire();
                    if (this.isFood() && (this.tempChange >= 100 || this.tempChange <= -100) || this.tempChange == 400 || this.tempChange == -400) {
                        this.tempChange = 0;
                        dbcon = DbConnector.getItemDbCon();
                        ps = dbcon.prepareStatement(this.dbstrings.setTemperature());
                        ps.setShort(1, temp);
                        ps.setLong(2, this.id);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                    }
                    if ((this.isLight() || this.isFire() || this.getTemplateId() == 178 || this.getTemplateId() == 889 || this.getTemplateId() == 180 || this.getTemplateId() == 1178 || this.getTemplateId() == 1301 || this.getTemplateId() == 1243) && flag != flag2) {
                        if (flag) {
                            if (this.parentId == -10L) {
                                VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                                if (t != null) {
                                    t.renameItem(this);
                                    t.removeLightSource(this);
                                }
                            } else {
                                this.notifyWatchersTempChange();
                                try {
                                    VolaTile vt;
                                    if (this.getParent() != null && this.getParent().getTemplate().hasViewableSubItems() && (!this.getParent().getTemplate().isContainerWithSubItems() || this.isPlacedOnParent()) && (vt = Zones.getTileOrNull(this.getParent().getTileX(), this.getParent().getTileY(), this.getParent().isOnSurface())) != null) {
                                        vt.renameItem(this);
                                        vt.removeLightSource(this);
                                    }
                                }
                                catch (NoSuchItemException vt) {}
                            }
                        } else if (flag2) {
                            if (this.parentId == -10L) {
                                VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
                                if (t != null) {
                                    t.renameItem(this);
                                    t.addLightSource(this);
                                }
                            } else {
                                this.notifyWatchersTempChange();
                                try {
                                    VolaTile vt;
                                    if (this.getParent() != null && this.getParent().getTemplate().hasViewableSubItems() && (!this.getParent().getTemplate().isContainerWithSubItems() || this.isPlacedOnParent()) && (vt = Zones.getTileOrNull(this.getParent().getTileX(), this.getParent().getTileY(), this.getParent().isOnSurface())) != null) {
                                        vt.renameItem(this);
                                        vt.addLightSource(this);
                                    }
                                }
                                catch (NoSuchItemException noSuchItemException) {
                                    // empty catch block
                                }
                            }
                        }
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
                catch (SQLException sqx) {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    break block25;
                }
                finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public boolean getLocked() {
        return this.locked;
    }

    @Override
    public void addItem(Item item, boolean loading) {
        if (item.getTemplateId() == 1392) {
            for (Item i : this.getItemsAsArray()) {
                if (i.getTemplateId() != 1392 || i.getRealTemplateId() != item.getRealTemplateId()) continue;
                Items.destroyItem(i.getWurmId());
            }
        }
        if (item != null) {
            if (this.items == null) {
                this.items = new HashSet();
            }
            this.items.add(item);
            item.setSurfaced(this.surfaced);
            if (!loading) {
                this.updateParents();
            }
            if (this.getTemplate().getContainerRestrictions() != null) {
                Item[] existingItems = this.getItemsAsArray();
                for (ContainerRestriction cRest : this.getTemplate().getContainerRestrictions()) {
                    if (!cRest.doesItemOverrideSlot(item)) continue;
                    for (Item i : existingItems) {
                        if (i.getTemplateId() != 1392 || i.getRealTemplateId() != cRest.getEmptySlotTemplateId()) continue;
                        Items.destroyItem(i.getWurmId());
                    }
                }
            }
        } else {
            logger.warning("Ignored attempt to add a null item to " + this);
        }
    }

    @Override
    public void removeItem(Item item) {
        if (this.items != null) {
            if (item != null) {
                this.items.remove(item);
            } else {
                logger.warning("Ignored attempt to remove a null item from " + this);
            }
        }
        this.updateParents();
    }

    @Override
    public Set<Item> getItems() {
        if (this.items == null) {
            this.items = new HashSet();
        }
        return this.items;
    }

    @Override
    public Item[] getItemsAsArray() {
        if (this.items == null) {
            return emptyItems;
        }
        return this.items.toArray(new Item[this.items.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void setSizeX(int sizex) {
        if (sizex != this.sizeX) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                if (!this.isLiquid()) {
                    sizex = Math.min(this.template.getSizeX() * 4, sizex);
                }
                this.sizeX = sizex;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setSizeX());
                ps.setInt(1, this.sizeX);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    @Override
    public int getSizeX() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        return (int)((float)this.sizeX * modifier);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void setSizeY(int sizey) {
        if (sizey != this.sizeY) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                if (!this.isLiquid()) {
                    sizey = Math.min(this.template.getSizeY() * 4, sizey);
                }
                this.sizeY = sizey;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setSizeY());
                ps.setInt(1, this.sizeY);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    @Override
    public int getSizeY() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        return (int)((float)this.sizeY * modifier);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void setSizeZ(int sizez) {
        if (this.sizeZ != sizez) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                if (!this.isLiquid()) {
                    sizez = Math.min(this.template.getSizeZ() * 4, sizez);
                }
                this.sizeZ = sizez;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setSizeZ());
                ps.setInt(1, this.sizeZ);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    @Override
    public int getSizeZ() {
        float modifier = 1.0f;
        if (this.getSpellEffects() != null) {
            modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_SIZE);
        }
        return (int)((float)this.sizeZ * modifier);
    }

    @Override
    public boolean setWeight(int w, boolean destroyOnWeightZero) {
        return this.setWeight(w, destroyOnWeightZero, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setWeight(int w, boolean destroyOnWeightZero, boolean updateOwner) {
        block27: {
            if (this.weight != w) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    if (destroyOnWeightZero && w <= 0) {
                        Items.destroyItem(this.id);
                        boolean bl = true;
                        return bl;
                    }
                    if (this.ownerId != -10L && updateOwner) {
                        try {
                            Creature owner = Server.getInstance().getCreature(this.ownerId);
                            if (this.isBodyPart()) {
                                if (this.getAuxData() == 100) {
                                    if (!owner.removeCarriedWeight(this.weight)) {
                                        logger.log(Level.WARNING, this.getName() + " removed " + this.weight + " and added " + w, new Exception());
                                    }
                                    owner.addCarriedWeight(w);
                                }
                            } else {
                                if (!owner.removeCarriedWeight(this.weight)) {
                                    logger.log(Level.WARNING, this.getName() + " removed " + this.weight + " and added " + w, new Exception());
                                }
                                owner.addCarriedWeight(w);
                            }
                        }
                        catch (NoSuchCreatureException owner) {
                        }
                        catch (NoSuchPlayerException nsp) {
                            logger.log(Level.WARNING, "Creature doesn't exist although it says so." + nsp.getMessage(), nsp);
                        }
                    }
                    if (this.isCombine() && !this.isLiquid()) {
                        double modi = Math.min(4.0, Math.pow(w, 0.3333333333333333) / Math.pow(this.template.getWeightGrams(), 0.3333333333333333));
                        this.setSizeZ(Math.max(1, (int)((double)this.template.getSizeZ() * modi)));
                        this.setSizeY(Math.max(1, (int)((double)this.template.getSizeY() * modi)));
                        this.setSizeX(Math.max(1, (int)((double)this.template.getSizeX() * modi)));
                    }
                    this.weight = w;
                    if (this.isBulkItem()) {
                        this.setDescription("" + this.getBulkNums() + "x");
                    }
                    if (this.parentId != -10L) {
                        this.updateParents();
                    } else if (!(!this.isUseOnGroundOnly() || this.isDomainItem() || this.isKingdomMarker() || this.hideAddToCreationWindow() || this.isNoDrop())) {
                        if (this.getTopParent() == this.getWurmId() && this.watchers != null) {
                            for (Creature watcher : this.watchers) {
                                watcher.getCommunicator().sendUpdateGroundItem(this);
                            }
                        }
                    } else if (this.isUnfinished() && this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            watcher.getCommunicator().sendUpdateGroundItem(this);
                        }
                    }
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setWeight());
                    ps.setInt(1, this.weight);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
                catch (SQLException sqx) {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    break block27;
                }
                finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
                DbConnector.returnConnection(dbcon);
            }
        }
        return false;
    }

    @Override
    public int getWeightGrams() {
        if (this.getSpellEffects() == null) {
            return this.weight;
        }
        return (int)((float)this.weight * this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_WEIGHT));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setData1(int d1) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.data1 != d1) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.data.data1 = d1;
                    dbcon = DbConnector.getItemDbCon();
                    if (this.dataExists(dbcon)) {
                        ps = dbcon.prepareStatement(this.dbstrings.updateData1());
                        ps.setInt(1, this.data.data1);
                        ps.setLong(2, this.id);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                        break block7;
                    }
                    this.createDataEntry(dbcon);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public int getData1() {
        if (this.data != null) {
            return this.data.data1;
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setData2(int d2) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.data2 != d2) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.data.data2 = d2;
                    dbcon = DbConnector.getItemDbCon();
                    if (this.dataExists(dbcon)) {
                        ps = dbcon.prepareStatement(this.dbstrings.updateData2());
                        ps.setInt(1, this.data.data2);
                        ps.setLong(2, this.id);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                        break block7;
                    }
                    this.createDataEntry(dbcon);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public int getData2() {
        if (this.data != null) {
            return this.data.data2;
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setData(int d1, int d2) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.data1 != d1 || this.data.data2 != d2) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.data.data1 = d1;
                this.data.data2 = d2;
                dbcon = DbConnector.getItemDbCon();
                ps = this.dataExists(dbcon) ? dbcon.prepareStatement(this.dbstrings.updateAllData()) : dbcon.prepareStatement(this.dbstrings.createData());
                ps.setInt(1, this.data.data1);
                ps.setInt(2, this.data.data2);
                ps.setInt(3, this.data.extra1);
                ps.setInt(4, this.data.extra2);
                ps.setLong(5, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setExtra1(int e1) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.extra1 != e1) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.data.extra1 = e1;
                    dbcon = DbConnector.getItemDbCon();
                    if (this.dataExists(dbcon)) {
                        ps = dbcon.prepareStatement(this.dbstrings.updateExtra1());
                        ps.setInt(1, this.data.extra1);
                        ps.setLong(2, this.id);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                        break block7;
                    }
                    this.createDataEntry(dbcon);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public int getExtra1() {
        if (this.data != null) {
            return this.data.extra1;
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setExtra2(int e2) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.extra2 != e2) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                try {
                    this.data.extra2 = e2;
                    dbcon = DbConnector.getItemDbCon();
                    if (this.dataExists(dbcon)) {
                        ps = dbcon.prepareStatement(this.dbstrings.updateExtra2());
                        ps.setInt(1, this.data.extra2);
                        ps.setLong(2, this.id);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                        break block7;
                    }
                    this.createDataEntry(dbcon);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public int getExtra2() {
        if (this.data != null) {
            return this.data.extra2;
        }
        return -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setExtra(int e1, int e2) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.extra1 != e1 || this.data.extra2 != e2) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.data.extra1 = e1;
                this.data.extra2 = e2;
                dbcon = DbConnector.getItemDbCon();
                ps = this.dataExists(dbcon) ? dbcon.prepareStatement(this.dbstrings.updateAllData()) : dbcon.prepareStatement(this.dbstrings.createData());
                ps.setInt(1, this.data.data1);
                ps.setInt(2, this.data.data2);
                ps.setInt(3, this.data.extra1);
                ps.setInt(4, this.data.extra2);
                ps.setLong(5, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setAllData(int d1, int d2, int e1, int e2) {
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        if (this.data.data1 != d1 || this.data.data2 != d2 || this.data.extra1 != e1 || this.data.extra2 != e2) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.data.data1 = d1;
                this.data.data2 = d2;
                this.data.extra1 = e1;
                this.data.extra2 = e2;
                dbcon = DbConnector.getItemDbCon();
                ps = this.dataExists(dbcon) ? dbcon.prepareStatement(this.dbstrings.updateAllData()) : dbcon.prepareStatement(this.dbstrings.createData());
                ps.setInt(1, this.data.data1);
                ps.setInt(2, this.data.data2);
                ps.setInt(3, this.data.extra1);
                ps.setInt(4, this.data.extra2);
                ps.setLong(5, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean inscriptionExists(Connection dbcon) {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(this.dbstrings.getInscription());
            ps.setLong(1, this.id);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            return false;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean dataExists(Connection dbcon) {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(this.dbstrings.getData());
            ps.setLong(1, this.id);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            return false;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createInscriptionDataEntry(Connection dbcon) {
        PreparedStatement ps = null;
        if (this.inscription == null) {
            this.inscription = new InscriptionData(this.id, "", "", 0);
        }
        try {
            ps = dbcon.prepareStatement(this.dbstrings.createInscription());
            ps.setLong(1, this.id);
            ps.setString(2, this.inscription.getInscription());
            ps.setString(3, this.inscription.getInscriber());
            ps.setInt(4, this.inscription.getPenColour());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save inscription data for item " + this.id, sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void createDataEntry(Connection dbcon) {
        PreparedStatement ps = null;
        if (this.data == null) {
            this.data = new ItemData(this.id, -1, -1, -1, -1);
        }
        try {
            ps = dbcon.prepareStatement(this.dbstrings.createData());
            ps.setInt(1, this.data.data1);
            ps.setInt(2, this.data.data2);
            ps.setInt(3, this.data.extra1);
            ps.setInt(4, this.data.extra2);
            ps.setLong(5, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
    }

    @Override
    public byte getMaterial() {
        if (this.getTemplateId() == 1307 && this.getData1() > 0 && this.getRealTemplate() != null) {
            if (this.material != 0) {
                return this.material;
            }
            return this.getRealTemplate().getMaterial();
        }
        if (this.getTemplateId() == 1307) {
            return 0;
        }
        if (this.material == 0) {
            return this.template.getMaterial();
        }
        return this.material;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMaterial(byte mat) {
        if (this.material != mat) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.material = mat;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setMaterial());
                ps.setByte(1, this.material);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBanked(boolean bank) {
        if (this.banked != bank) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.banked = bank;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setBanked());
                ps.setBoolean(1, this.banked);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void bless(int blesser) {
        if (this.bless == 0) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.bless = (byte)blesser;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setBless());
                ps.setByte(1, this.bless);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void enchant(byte ench) {
        if (this.enchantment != ench) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.enchantment = ench;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setEnchant());
                ps.setByte(1, this.enchantment);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPrice(int newPrice) {
        if (this.price != newPrice) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.price = newPrice;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setPrice());
                ps.setInt(1, this.price);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setAuxData(byte auxdata) {
        block9: {
            if (this.auxbyte == auxdata) break block9;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.auxbyte = auxdata;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setAuxData());
                ps.setByte(1, this.auxbyte);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
            if (this.isFood() || this.isAlcohol()) {
                VolaTile t;
                if (this.watchers != null) {
                    for (Creature watcher : this.watchers) {
                        watcher.getCommunicator().sendUpdateInventoryItem(this);
                    }
                } else if (this.zoneId > 0 && this.parentId == -10L && (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) != null) {
                    t.renameItem(this);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCreationState(byte newState) {
        if (this.creationState != newState) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.creationState = newState;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setCreationState());
                ps.setByte(1, newState);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setRealTemplate(int rTemplate) {
        if (this.realTemplate != rTemplate) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.realTemplate = rTemplate;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setRealTemplate());
                ps.setInt(1, this.realTemplate);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFemale(boolean _female) {
        if (this.female != _female) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.female = _female;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setFemale());
                ps.setBoolean(1, this.female);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCreator(String _creator) {
        if (this.isNamed() && _creator != null && _creator.length() > 0 && (this.creator == null || !this.creator.equals(_creator))) {
            this.creator = _creator.substring(0, Math.min(_creator.length(), this.creatorMaxLength));
            Connection dbcon = null;
            PreparedStatement ps = null;
            if (this.creator.equals("0")) {
                logger.log(Level.INFO, "Creator set to 0 at ", new Exception());
            }
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setCreator());
                ps.setString(1, this.creator);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    @Override
    public void setColor(int _color) {
        if (this.color != _color) {
            this.setColors(_color, this.color2);
        }
    }

    @Override
    public void setColor2(int _color2) {
        if (this.color2 != _color2) {
            this.setColors(this.color, _color2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void setColors(int _color, int _color2) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.color = _color;
            this.color2 = _color2;
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.setColor());
            ps.setInt(1, this.color);
            ps.setInt(2, this.color2);
            ps.setLong(3, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
        if (this.getParentId() != -10L) {
            if (this.watchers != null) {
                for (Creature watcher : this.watchers) {
                    watcher.getCommunicator().sendUpdateInventoryItem(this);
                }
            }
        } else {
            this.updateIfGroundItem();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void setWornAsArmour(boolean wornArmour, long newOwner) {
        block26: {
            if (this.wornAsArmour == wornArmour) break block26;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.wornAsArmour = wornArmour;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setWornAsArmour());
                ps.setBoolean(1, this.wornAsArmour);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
            if (this.wornAsArmour) {
                try {
                    Creature creature = Server.getInstance().getCreature(newOwner);
                    ArmourTemplate armour = ArmourTemplate.getArmourTemplate(this.template.templateId);
                    if (armour == null) break block26;
                    float moveModChange = armour.getMoveModifier();
                    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
                        moveModChange *= ArmourTemplate.getMaterialMovementModifier(this.getMaterial());
                    } else if (Servers.localServer.isChallengeOrEpicServer()) {
                        if (this.getMaterial() == 57 || this.getMaterial() == 67) {
                            moveModChange *= 0.9f;
                        } else if (this.getMaterial() == 56) {
                            moveModChange *= 0.95f;
                        }
                    }
                    creature.getMovementScheme().armourMod.setModifier(creature.getMovementScheme().armourMod.getModifier() - (double)moveModChange);
                    if (armour.getLimitFactor() != creature.getArmourLimitingFactor()) {
                        creature.recalcLimitingFactor(this);
                    }
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, "Worn armour on unknown player: ", nsp);
                }
                catch (NoSuchCreatureException cnf) {
                    logger.log(Level.WARNING, "Worn armour on unknown creature: ", cnf);
                }
            } else {
                try {
                    Creature creature = Server.getInstance().getCreature(this.getOwnerId());
                    ArmourTemplate armour = ArmourTemplate.getArmourTemplate(this.template.templateId);
                    if (armour != null) {
                        float moveModChange = armour.getMoveModifier();
                        if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
                            moveModChange *= ArmourTemplate.getMaterialMovementModifier(this.getMaterial());
                        } else if (Servers.localServer.isChallengeOrEpicServer()) {
                            if (this.getMaterial() == 57 || this.getMaterial() == 67) {
                                moveModChange *= 0.9f;
                            } else if (this.getMaterial() == 56) {
                                moveModChange *= 0.95f;
                            }
                        }
                        creature.getMovementScheme().armourMod.setModifier(creature.getMovementScheme().armourMod.getModifier() + (double)moveModChange);
                        creature.recalcLimitingFactor(null);
                    }
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, "Worn armour on unknown player: ", nsp);
                }
                catch (NoSuchCreatureException cnf) {
                    logger.log(Level.WARNING, "Worn armour on unknown creature: ", cnf);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void clear(long wurmId, String _creator, float posx, float posy, float posz, float _rot, String _desc, String _name, float _qualitylevel, byte _material, byte aRarity, long bridgeId) {
        this.id = wurmId;
        this.creator = _creator;
        this.posX = posx;
        this.posY = posy;
        this.posZ = posz;
        this.description = _desc;
        this.name = _name;
        this.originalQualityLevel = this.qualityLevel = _qualitylevel;
        this.rotation = _rot;
        this.zoneId = -10;
        this.parentId = -10L;
        this.auxbyte = 0;
        this.sizeX = this.template.getSizeX();
        this.sizeY = this.template.getSizeY();
        this.sizeZ = this.template.getSizeZ();
        this.weight = this.template.getWeightGrams();
        this.lastMaintained = WurmCalendar.currentTime;
        this.creationDate = WurmCalendar.currentTime;
        this.banked = false;
        this.damage = 0.0f;
        this.enchantment = 0;
        this.data = null;
        this.color = -1;
        this.color2 = -1;
        this.temperature = (short)200;
        this.creator = "";
        this.isBusy = false;
        this.material = _material;
        this.bless = 0;
        this.mailed = false;
        this.mailTimes = 0;
        this.rarity = aRarity;
        this.onBridge = bridgeId;
        this.creationState = 0;
        this.hatching = false;
        this.ownerId = -10L;
        this.lastOwner = -10L;
        this.realTemplate = -10;
        if (this.isNamed() && _creator != null && _creator.length() > 0) {
            this.creator = _creator.substring(0, Math.min(_creator.length(), this.creatorMaxLength));
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.clearItem());
            ps.setString(1, this.name);
            ps.setString(2, this.description);
            ps.setFloat(3, this.qualityLevel);
            ps.setFloat(4, this.originalQualityLevel);
            ps.setLong(5, this.lastMaintained);
            ps.setByte(6, this.enchantment);
            ps.setBoolean(7, this.banked);
            ps.setInt(8, this.sizeX);
            ps.setInt(9, this.sizeY);
            ps.setInt(10, this.sizeZ);
            ps.setInt(11, this.zoneId);
            ps.setFloat(12, this.damage);
            ps.setLong(13, this.parentId);
            ps.setFloat(14, this.rotation);
            ps.setInt(15, this.weight);
            ps.setFloat(16, this.posX);
            ps.setFloat(17, this.posY);
            ps.setFloat(18, this.posZ);
            ps.setString(19, this.creator);
            ps.setByte(20, this.auxbyte);
            ps.setInt(21, this.color);
            ps.setInt(22, this.color2);
            ps.setShort(23, this.temperature);
            ps.setLong(24, this.creationDate);
            ps.setByte(25, this.material);
            ps.setByte(26, this.bless);
            ps.setByte(27, this.rarity);
            ps.setByte(28, this.creationState);
            ps.setLong(29, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to create/update item with id " + this.id, sqex);
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
    @Override
    public void setMailed(boolean _mailed) {
        if (this.mailed != _mailed) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.mailed = _mailed;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setMailed());
                ps.setBoolean(1, this.mailed);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setHidden(boolean _hidden) {
        if (this.hidden != _hidden) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.hidden = _hidden;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setHidden());
                ps.setBoolean(1, this.hidden);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void savePermissions() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.setSettings());
            ps.setInt(1, this.getSettings().getPermissions());
            ps.setLong(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save permissions for item " + this.id, sqx);
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

    @Override
    public final void setDbStrings(DbStrings newDbStrings) {
        this.dbstrings = newDbStrings;
    }

    @Override
    public DbStrings getDbStrings() {
        return this.dbstrings;
    }

    public static ItemDamageDatabaseUpdater getItemDamageDatabaseUpdater() {
        return itemDamageDatabaseUpdater;
    }

    public static ItemOwnerDatabaseUpdater getItemOwnerDatabaseUpdater() {
        return itemOwnerDatabaseUpdater;
    }

    public static ItemLastOwnerDatabaseUpdater getItemLastOwnerDatabaseUpdater() {
        return itemLastOwnerDatabaseUpdater;
    }

    public static ItemParentDatabaseUpdater getItemParentDatabaseUpdater() {
        return itemParentDatabaseUpdater;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMailTimes(byte times) {
        if (this.mailTimes != times) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.mailTimes = times;
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement(this.dbstrings.setMailTimes());
                ps.setByte(1, this.mailTimes);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void moveToFreezer() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.freeze());
            ps.setLong(1, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to freeze item " + this.id, sqx);
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
    @Override
    public void returnFromFreezer() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.thaw());
            ps.setLong(1, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to unfreeze item " + this.id, sqx);
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
    @Override
    public boolean setRarity(byte newRarity) {
        block7: {
            PreparedStatement ps;
            Connection dbcon;
            block6: {
                if (newRarity == this.rarity) break block7;
                this.rarity = newRarity;
                dbcon = null;
                ps = null;
                try {
                    VolaTile t;
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(this.dbstrings.setRarity());
                    ps.setByte(1, this.rarity);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                    if (this.watchers != null) {
                        for (Creature watcher : this.watchers) {
                            watcher.getCommunicator().sendUpdateInventoryItem(this);
                        }
                        break block6;
                    }
                    if (this.zoneId <= 0 || this.parentId != -10L || (t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface())) == null) break block6;
                    t.renameItem(this);
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to set rarity " + this.rarity + " for item " + this.id, sqx);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPlacedOnParent(boolean onParent) {
        if (this.placedOnParent != onParent) {
            this.placedOnParent = onParent;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getItemDbCon();
                ps = dbcon.prepareStatement("UPDATE ITEMS SET PLACEDONPARENT=? WHERE WURMID=?");
                ps.setBoolean(1, this.placedOnParent);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set placedOnParent " + this.placedOnParent + " for item " + this.id, sqx);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void deleteInDatabase() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(this.dbstrings.deleteItem());
            ps.setLong(1, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete item " + this.id, sqx);
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

    @Override
    public boolean isItem() {
        return true;
    }
}


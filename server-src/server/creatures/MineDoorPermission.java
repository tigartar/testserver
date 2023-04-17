/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorSettings;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
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
import javax.annotation.Nullable;

public final class MineDoorPermission
implements MiscConstants,
PermissionsPlayerList.ISettings {
    private static final Logger logger = Logger.getLogger(MineDoorPermission.class.getName());
    private static final String CREATE_MINEDOOR = "INSERT INTO MINEDOOR (CREATOR,VILLAGE,ALLOWALL,ALLOWALLIES,NAME,SETTINGS,ID) VALUES (?,?,?,?,?,?,?)";
    private static final String UPDATE_MINEDOOR = "UPDATE MINEDOOR SET CREATOR=?,VILLAGE=?,ALLOWALL=?,ALLOWALLIES=?,NAME=?,SETTINGS=? WHERE ID=?";
    private static final String DELETE_MINEDOOR = "DELETE FROM MINEDOOR WHERE ID=?";
    private static final String GET_ALL_MINEDOORS = "SELECT * FROM MINEDOOR";
    private static final Map<Integer, MineDoorPermission> mineDoorPermissions = new ConcurrentHashMap<Integer, MineDoorPermission>();
    private final int id;
    private final long wurmId;
    private long creator = -10L;
    private int villageId = -1;
    private String name = "";
    private boolean allowAll = false;
    private boolean allowAllies = false;
    private Village village = null;
    private Permissions permissions = new Permissions();
    private long closingTime = 0L;
    Set<Creature> creaturesOpened = new HashSet<Creature>();
    Set<VirtualZone> watchers = new HashSet<VirtualZone>();
    boolean open = false;

    public MineDoorPermission(int tilex, int tiley, long _creator, Village currentvillage, boolean _allowAll, boolean _allowAllies, String _name, int settings) {
        this.id = MineDoorPermission.makeId(tilex, tiley);
        this.wurmId = Tiles.getTileId(tilex, tiley, 0);
        this.creator = _creator;
        this.allowAllies = _allowAllies;
        this.name = _name;
        this.permissions.setPermissionBits(settings);
        this.village = currentvillage;
        if (this.village != null) {
            this.setIsManaged(true, null);
        } else {
            this.setIsManaged(false, null);
        }
        try {
            this.save();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        MineDoorPermission.addPermission(this);
    }

    private MineDoorPermission(int _id, long _creator, int _villageid, boolean _allowall, boolean _allowAllies, String _name, int settings) {
        this.id = _id;
        this.wurmId = Tiles.getTileId(MineDoorPermission.decodeTileX(this.id), MineDoorPermission.decodeTileY(this.id), 0);
        this.creator = _creator;
        this.villageId = _villageid;
        this.allowAll = _allowall;
        this.allowAllies = _allowAllies;
        this.name = _name;
        this.permissions.setPermissionBits(settings);
        MineDoorPermission.addPermission(this);
    }

    @Override
    public int getMaxAllowed() {
        return MineDoorSettings.getMaxAllowed();
    }

    public final void setVillage(@Nullable Village vill) {
        this.village = vill;
        if (vill != null) {
            this.setIsManaged(true, null);
        } else if (this.villageId != -1) {
            this.setIsManaged(false, null);
        }
    }

    public boolean setVillageId(int newid) {
        if (this.villageId == newid) {
            return false;
        }
        this.villageId = newid;
        return true;
    }

    public int getVillageId() {
        return this.villageId;
    }

    public Village getVillage() {
        return this.village;
    }

    private Village getPermissionsVillage() {
        Village vill = this.getManagedByVillage();
        if (vill != null) {
            return vill;
        }
        return this.getOwnerVillage();
    }

    public final Village getOwnerVillage() {
        return Villages.getVillageForCreature(this.creator);
    }

    public final Village getManagedByVillage() {
        if (this.villageId >= 0) {
            try {
                return Villages.getVillage(this.villageId);
            }
            catch (NoSuchVillageException e) {
                this.setVillageId(-1);
            }
        }
        return null;
    }

    public boolean setAllowAll(boolean allow) {
        if (this.allowAll == allow) {
            return false;
        }
        this.allowAll = allow;
        return true;
    }

    public boolean isAllowAll() {
        return this.allowAll;
    }

    public boolean setAllowAllies(boolean allow) {
        if (this.allowAllies == allow) {
            return false;
        }
        this.allowAllies = allow;
        return true;
    }

    public boolean isAllowAllies() {
        return this.allowAllies;
    }

    public boolean setController(long newid) {
        if (this.creator == newid) {
            return false;
        }
        this.creator = newid;
        return true;
    }

    public long getController() {
        return this.creator;
    }

    public void removeMDPerm(long creatureId) {
        MineDoorSettings.removePlayer(this.id, creatureId);
    }

    public void addMDPerm(long creatureId, int settings) {
        MineDoorSettings.addPlayer(this.id, creatureId, settings);
    }

    public static final void removePermission(MineDoorPermission perm) {
        mineDoorPermissions.remove(perm.id);
        int x = perm.getTileX();
        int y = perm.getTileY();
        try {
            Zones.getZone(perm.getTileX(), perm.getTileY(), true).getOrCreateTile(x, y).removeMineDoor(perm);
        }
        catch (NoSuchZoneException e) {
            logger.log(Level.SEVERE, "Could not find zone for removing a mine door at " + x + ":" + y + "!");
        }
    }

    private static final void addPermission(MineDoorPermission perm) {
        mineDoorPermissions.put(perm.id, perm);
        int x = perm.getTileX();
        int y = perm.getTileY();
        try {
            if (MineDoorPermission.isVisibleDoorTile(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
                Point highestCorner = TileRockBehaviour.findHighestCorner(x, y);
                Point nextHighestCorner = TileRockBehaviour.findNextHighestCorner(x, y, highestCorner);
                if (highestCorner.getH() == nextHighestCorner.getH()) {
                    Zones.getZone(perm.getTileX(), perm.getTileY(), true).getOrCreateTile(x, y).addMineDoor(perm);
                }
            }
        }
        catch (NoSuchZoneException e) {
            logger.log(Level.SEVERE, "Could not find zone for adding a mine door at " + x + ":" + y + "!");
        }
    }

    private static final boolean isVisibleDoorTile(int tile) {
        return tile == 27 || tile == 25 || tile == 28 || tile == 29;
    }

    public static final MineDoorPermission getPermission(long wurmId) {
        short tilex = Tiles.decodeTileX(wurmId);
        int tiley = Tiles.decodeTileY(wurmId);
        return MineDoorPermission.getPermission(tilex, tiley);
    }

    public static final MineDoorPermission getPermission(int tilex, int tiley) {
        return MineDoorPermission.getPermission(MineDoorPermission.makeId(tilex, tiley));
    }

    public static final MineDoorPermission getPermission(int mineDoorId) {
        return mineDoorPermissions.get(mineDoorId);
    }

    @Override
    public final boolean canHavePermissions() {
        return true;
    }

    @Override
    public final boolean mayShowPermissions(Creature creature) {
        return this.mayManage(creature);
    }

    public final boolean canManage(Creature creature) {
        if (MineDoorSettings.isExcluded(this, this.id, creature)) {
            return false;
        }
        if (MineDoorSettings.canManage(this, this.id, creature)) {
            return true;
        }
        if (creature.getCitizenVillage() == null) {
            return false;
        }
        Village vill = this.getManagedByVillage();
        if (vill == null) {
            return false;
        }
        if (!vill.isCitizen(creature)) {
            return false;
        }
        return vill.isActionAllowed((short)364, creature);
    }

    public final boolean mayManage(Creature creature) {
        if (creature.getPower() > 1) {
            return true;
        }
        return this.canManage(creature);
    }

    public final boolean maySeeHistory(Creature creature) {
        if (creature.getPower() > 1) {
            return true;
        }
        return this.isOwner(creature);
    }

    public final boolean mayPass(Creature creature) {
        return !MineDoorSettings.isExcluded(this, this.id, creature) && MineDoorSettings.mayPass(this, this.id, creature);
    }

    @Override
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_MINEDOOR);
            ps.setLong(1, this.creator);
            ps.setInt(2, this.villageId);
            ps.setBoolean(3, this.allowAll);
            ps.setBoolean(4, this.allowAllies);
            ps.setString(5, this.name);
            ps.setInt(6, this.permissions.getPermissions());
            ps.setInt(7, this.id);
            if (ps.executeUpdate() == 0) {
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(CREATE_MINEDOOR);
                ps.setLong(1, this.creator);
                ps.setInt(2, this.villageId);
                ps.setBoolean(3, this.allowAll);
                ps.setBoolean(4, this.allowAllies);
                ps.setString(5, this.name);
                ps.setInt(6, this.permissions.getPermissions());
                ps.setInt(7, this.id);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to create mine door: " + this.id + ", " + this.creator + ", " + this.villageId + ":" + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void deleteMineDoor(int tilex, int tiley) {
        int mdId = MineDoorPermission.makeId(tilex, tiley);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_MINEDOOR);
            ps.setInt(1, mdId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete mine door: " + mdId + ":" + sqex.getMessage(), sqex);
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
        MineDoorSettings.remove(mdId);
        PermissionsHistories.remove(mdId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllMineDoors() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_ALL_MINEDOORS);
            rs = ps.executeQuery();
            while (rs.next()) {
                new MineDoorPermission(rs.getInt("ID"), rs.getLong("CREATOR"), rs.getInt("VILLAGE"), rs.getBoolean("ALLOWALL"), rs.getBoolean("ALLOWALLIES"), rs.getString("NAME"), rs.getInt("SETTINGS"));
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load all mine doors: " + sqex.getMessage(), sqex);
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

    public final void addWatcher(VirtualZone watcher) {
        if (this.watchers == null) {
            this.watchers = new HashSet<VirtualZone>();
        }
        if (!this.watchers.contains(watcher)) {
            this.watchers.add(watcher);
        }
    }

    public final void removeWatcher(VirtualZone watcher) {
        if (this.watchers != null && this.watchers.contains(watcher)) {
            this.watchers.remove(watcher);
        }
    }

    public void open(Creature creature) {
        this.open = true;
        if (this.creaturesOpened.isEmpty()) {
            for (VirtualZone z : this.watchers) {
                if (!creature.isVisibleTo(z.getWatcher())) continue;
                z.openMineDoor(this);
            }
        }
        this.creaturesOpened.add(creature);
    }

    public void close(Creature creature) {
        this.open = true;
        this.creaturesOpened.remove(creature);
        if (this.creaturesOpened.isEmpty()) {
            for (VirtualZone z : this.watchers) {
                z.closeMineDoor(this);
            }
        }
    }

    public long getClosingTime() {
        return this.closingTime;
    }

    public void setClosingTime(long aClosingTime) {
        this.closingTime = aClosingTime;
    }

    public final boolean isWideOpen() {
        return this.getClosingTime() > System.currentTimeMillis();
    }

    public static int makeId(int tilex, int tiley) {
        return (tilex << 16) + tiley;
    }

    public static int decodeTileX(int mdId) {
        return mdId >> 16 & 0xFFFF;
    }

    public static int decodeTileY(int mdId) {
        return mdId & 0xFFFF;
    }

    public static MineDoorPermission[] getAllMineDoors() {
        return mineDoorPermissions.values().toArray(new MineDoorPermission[mineDoorPermissions.size()]);
    }

    public static final MineDoorPermission[] getManagedMineDoorsFor(Player player, int villageId, boolean includeAll) {
        HashSet<MineDoorPermission> mineDoors = new HashSet<MineDoorPermission>();
        for (MineDoorPermission mineDoor : mineDoorPermissions.values()) {
            int encodedTile = Server.surfaceMesh.getTile(mineDoor.getTileX(), mineDoor.getTileY());
            byte type = Tiles.decodeType(encodedTile);
            if (!Tiles.isMineDoor(type)) {
                MineDoorPermission.removePermission(mineDoor);
                continue;
            }
            if (mineDoor.canManage(player)) {
                mineDoors.add(mineDoor);
            }
            if (!includeAll || (villageId < 0 || mineDoor.getVillageId() != villageId) && (mineDoor.getVillage() == null || !mineDoor.getVillage().isMayor(player.getWurmId()))) continue;
            mineDoors.add(mineDoor);
        }
        return mineDoors.toArray(new MineDoorPermission[mineDoors.size()]);
    }

    public static final MineDoorPermission[] getOwnedMinedoorsFor(Player player) {
        HashSet<MineDoorPermission> mineDoors = new HashSet<MineDoorPermission>();
        for (MineDoorPermission mineDoor : mineDoorPermissions.values()) {
            int encodedTile = Server.surfaceMesh.getTile(mineDoor.getTileX(), mineDoor.getTileY());
            byte type = Tiles.decodeType(encodedTile);
            if (!Tiles.isMineDoor(type)) {
                MineDoorPermission.removePermission(mineDoor);
                continue;
            }
            if (!mineDoor.isOwner(player) && !mineDoor.isActualOwner(player.getWurmId())) continue;
            mineDoors.add(mineDoor);
        }
        return mineDoors.toArray(new MineDoorPermission[mineDoors.size()]);
    }

    public static final void unManageMineDoorsFor(int villageId) {
        for (MineDoorPermission md : MineDoorPermission.getAllMineDoors()) {
            if (md.getVillageId() != villageId) continue;
            md.setVillage(null);
        }
    }

    @Override
    public long getWurmId() {
        return this.wurmId;
    }

    @Override
    public int getTemplateId() {
        return -10;
    }

    public int getTileX() {
        return MineDoorPermission.decodeTileX(this.id);
    }

    public int getTileY() {
        return MineDoorPermission.decodeTileY(this.id);
    }

    @Override
    public String getObjectName() {
        return this.name;
    }

    @Override
    public String getTypeName() {
        int encodedTile = Server.surfaceMesh.getTile(this.getTileX(), this.getTileY());
        byte type = Tiles.decodeType(encodedTile);
        Tiles.Tile tile = Tiles.getTile(type);
        return tile.getDesc();
    }

    @Override
    public boolean setObjectName(String newName, Creature creature) {
        if (this.name.equals(newName)) {
            return true;
        }
        this.name = newName;
        return true;
    }

    @Override
    public boolean isActualOwner(long playerId) {
        return this.creator == playerId;
    }

    @Override
    public boolean isOwner(Creature creature) {
        return this.isOwner(creature.getWurmId());
    }

    @Override
    public boolean isOwner(long playerId) {
        Village vill;
        if (this.isManaged() && (vill = this.getManagedByVillage()) != null) {
            return vill.isMayor(playerId);
        }
        return this.isActualOwner(playerId);
    }

    @Override
    public boolean canChangeName(Creature creature) {
        return creature.getPower() > 1 || this.isOwner(creature.getWurmId());
    }

    @Override
    public boolean canChangeOwner(Creature creature) {
        return creature.getPower() > 1 || this.isActualOwner(creature.getWurmId());
    }

    private boolean showWarning() {
        return false;
    }

    @Override
    public String getWarning() {
        if (this.showWarning()) {
            return "NEEDS A LOCK";
        }
        return "";
    }

    @Override
    public PermissionsPlayerList getPermissionsPlayerList() {
        return MineDoorSettings.getPermissionsPlayerList(this.id);
    }

    @Override
    public boolean isManaged() {
        return this.permissions.hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
    }

    @Override
    public boolean isManageEnabled(Player player) {
        Village vil;
        if (player.getPower() > 1) {
            return true;
        }
        if (this.isManaged() && (vil = Villages.getVillage(this.getTileX(), this.getTileY(), true)) != null) {
            return false;
        }
        return this.isOwner(player);
    }

    @Override
    public void setIsManaged(boolean newIsManaged, Player player) {
        int oldId = this.villageId;
        if (newIsManaged) {
            if (this.village != null) {
                this.setVillageId(this.village.getId());
            } else {
                Village cv = this.getOwnerVillage();
                if (cv != null) {
                    this.setVillageId(cv.getId());
                } else {
                    this.setVillageId(-1);
                }
            }
        } else {
            this.setVillageId(-1);
        }
        if (oldId != this.villageId && MineDoorSettings.exists(this.id)) {
            MineDoorSettings.remove(this.id);
            PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
        }
        this.permissions.setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), this.villageId != -1);
        try {
            this.save();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public boolean isCitizen(Creature creature) {
        Village vill = this.getManagedByVillage();
        if (vill == null) {
            vill = this.getOwnerVillage();
        }
        if (vill != null) {
            return vill.isCitizen(creature);
        }
        return false;
    }

    @Override
    public boolean isAllied(Creature creature) {
        Village vill = this.getManagedByVillage();
        if (vill == null) {
            vill = this.getOwnerVillage();
        }
        if (vill != null) {
            return vill.isAlly(creature);
        }
        return false;
    }

    @Override
    public boolean isSameKingdom(Creature creature) {
        Village vill = this.getPermissionsVillage();
        if (vill != null) {
            return vill.kingdom == creature.getKingdomId();
        }
        return Players.getInstance().getKingdomForPlayer(this.creator) == creature.getKingdomId();
    }

    @Override
    public void addGuest(long guestId, int settings) {
        MineDoorSettings.addPlayer(this.id, guestId, settings);
    }

    @Override
    public void removeGuest(long guestId) {
        MineDoorSettings.removePlayer(this.id, guestId);
    }

    @Override
    public boolean isGuest(Creature creature) {
        return this.isGuest(creature.getWurmId());
    }

    @Override
    public boolean isGuest(long playerId) {
        return MineDoorSettings.isGuest((PermissionsPlayerList.ISettings)this, playerId);
    }

    public final long getOwnerId() {
        return this.creator;
    }

    @Override
    public String mayManageText(Player aPlayer) {
        String vName = "";
        Village vill = this.getManagedByVillage();
        if (vill != null) {
            vName = "Settlement \"" + vill.getName() + "\" may manage";
        } else {
            vill = this.getVillage();
            if (vill != null) {
                vName = "Settlement \"" + vill.getName() + "\" may manage";
            } else {
                vill = Villages.getVillageForCreature(this.getOwnerId());
                if (vill != null) {
                    vName = "Settlement \"" + vill.getName() + "\" may manage";
                }
            }
        }
        return vName;
    }

    @Override
    public String mayManageHover(Player aPlayer) {
        return "";
    }

    @Override
    public String messageOnTick() {
        return "By selecting this you are giving full control to settlement.";
    }

    @Override
    public String questionOnTick() {
        return "Are you positive you want to give your control away?";
    }

    @Override
    public String messageUnTick() {
        return "By doing this you are reverting the control to owner";
    }

    @Override
    public String questionUnTick() {
        return "Are you sure you want them to have control?";
    }

    @Override
    public String getSettlementName() {
        String sName = "";
        Village vill = this.getPermissionsVillage();
        if (vill != null) {
            sName = vill.getName();
        }
        if (sName.length() == 0) {
            return sName;
        }
        return "Citizens of \"" + sName + "\"";
    }

    @Override
    public String getAllianceName() {
        String aName = "";
        Village vill = this.getPermissionsVillage();
        if (vill != null) {
            aName = vill.getAllianceName();
        }
        if (aName.length() == 0) {
            return aName;
        }
        return "Alliance of \"" + aName + "\"";
    }

    @Override
    public String getKingdomName() {
        byte kingdom = 0;
        Village vill = this.getPermissionsVillage();
        kingdom = vill != null ? vill.kingdom : Players.getInstance().getKingdomForPlayer(this.creator);
        return "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
    }

    @Override
    public String getRolePermissionName() {
        return "";
    }

    @Override
    public boolean canAllowEveryone() {
        return true;
    }

    @Override
    public boolean setNewOwner(long playerId) {
        if (!this.isManaged() && MineDoorSettings.exists(this.id)) {
            MineDoorSettings.remove(this.id);
            PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), playerId, "Auto", "Cleared Permissions");
        }
        return this.setController(playerId);
    }

    @Override
    public String getOwnerName() {
        return PlayerInfoFactory.getPlayerName(this.creator);
    }

    void addDefaultAllyPermissions() {
        if (!this.getPermissionsPlayerList().exists(-20L)) {
            int value = MineDoorSettings.MinedoorPermissions.PASS.getValue();
            this.addGuest(-20L, value);
        }
    }

    @Override
    public void addDefaultCitizenPermissions() {
        if (!this.getPermissionsPlayerList().exists(-30L)) {
            int value = MineDoorSettings.MinedoorPermissions.PASS.getValue();
            this.addGuest(-30L, value);
        }
    }

    void addDefaultKingdomPermission() {
        if (!this.getPermissionsPlayerList().exists(-40L)) {
            int value = MineDoorSettings.MinedoorPermissions.PASS.getValue();
            this.addGuest(-40L, value);
        }
    }

    public boolean convertToNewPermissions() {
        boolean didConvert = false;
        PermissionsPlayerList ppl = StructureSettings.getPermissionsPlayerList(this.getWurmId());
        if (this.allowAllies && !ppl.exists(-20L)) {
            this.addDefaultAllyPermissions();
            didConvert = true;
        }
        if (this.getVillageId() >= 0) {
            this.setIsManaged(true, null);
            didConvert = true;
        }
        if (this.getVillageId() >= 0 && !ppl.exists(-30L)) {
            this.addDefaultCitizenPermissions();
            didConvert = true;
        }
        if (this.allowAll && !ppl.exists(-40L)) {
            this.addDefaultKingdomPermission();
            didConvert = true;
        }
        if (didConvert) {
            try {
                this.save();
            }
            catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return didConvert;
    }

    @Override
    public boolean isItem() {
        return false;
    }
}


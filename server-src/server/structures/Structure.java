/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.BuildTile;
import com.wurmonline.server.structures.DbDoor;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.StructureBounds;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.structures.StructureSupport;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.TilePoint;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Structure
implements MiscConstants,
CounterTypes,
TimeConstants,
StructureConstants,
PermissionsPlayerList.ISettings {
    private static Logger logger = Logger.getLogger(Structure.class.getName());
    private long wurmId;
    Set<VolaTile> structureTiles;
    Set<BuildTile> buildTiles = new HashSet<BuildTile>();
    int minX = 1 << Constants.meshSize;
    int maxX = 0;
    int minY = 1 << Constants.meshSize;
    int maxY = 0;
    protected boolean surfaced;
    private long creationDate;
    private byte roof;
    private String name;
    private boolean isLoading = false;
    private boolean hasLoaded = false;
    boolean finished = false;
    Set<Door> doors;
    long writid = -10L;
    boolean finalfinished = false;
    boolean allowsVillagers = false;
    boolean allowsAllies = false;
    boolean allowsKingdom = false;
    private String planner = "";
    long ownerId = -10L;
    private Permissions permissions = new Permissions();
    int villageId = -1;
    private byte structureType = 0;
    private long lastPolled = System.currentTimeMillis();
    public static final float DAMAGE_STATE_DIVIDER = 60.0f;
    public static final int[] noEntrance = new int[]{-1, -1};

    Structure(byte theStructureType, String aName, long aId, int aStartX, int aStartY, boolean aSurfaced) {
        this.structureType = theStructureType;
        this.wurmId = aId;
        this.name = aName;
        this.structureTiles = new HashSet<VolaTile>();
        if (aStartX > this.maxX) {
            this.maxX = aStartX;
        }
        if (aStartX < this.minX) {
            this.minX = aStartX;
        }
        if (aStartY > this.maxY) {
            this.maxY = aStartY;
        }
        if (aStartY < this.minY) {
            this.minY = aStartY;
        }
        this.surfaced = aSurfaced;
        this.creationDate = System.currentTimeMillis();
        try {
            Zone zone = Zones.getZone(aStartX, aStartY, aSurfaced);
            VolaTile tile = zone.getOrCreateTile(aStartX, aStartY);
            this.structureTiles.add(tile);
            if (theStructureType == 0) {
                tile.addBuildMarker(this);
                this.clearAllWallsAndMakeWallsForStructureBorder(tile);
            } else {
                tile.addStructure(this);
            }
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "No such zone: " + aStartX + ", " + aStartY + ", StructureId: " + this.wurmId, nsz);
        }
    }

    Structure(byte theStructureType, String aName, long aId, boolean aIsSurfaced, byte _roof, boolean _finished, boolean finFinished, long _writid, String aPlanner, long aOwnerId, int aSettings, int aVillageId, boolean allowsCitizens, boolean allowAllies, boolean allowKingdom) {
        this.structureType = theStructureType;
        this.wurmId = aId;
        this.writid = _writid;
        this.name = aName;
        this.structureTiles = new HashSet<VolaTile>();
        this.surfaced = aIsSurfaced;
        this.roof = _roof;
        this.finished = _finished;
        this.finalfinished = finFinished;
        this.allowsVillagers = allowsCitizens;
        this.allowsAllies = allowAllies;
        this.allowsKingdom = allowKingdom;
        this.planner = aPlanner;
        this.ownerId = aOwnerId;
        this.setSettings(aSettings);
        this.villageId = aVillageId;
        this.setMaxAndMin();
    }

    Structure(long id) throws IOException, NoSuchStructureException {
        this.wurmId = id;
        this.structureTiles = new HashSet<VolaTile>();
        this.load();
        this.setMaxAndMin();
    }

    public final void addBuildTile(BuildTile toadd) {
        this.buildTiles.add(toadd);
    }

    public final void clearBuildTiles() {
        this.buildTiles.clear();
    }

    public final String getName() {
        return this.name;
    }

    public final void setPlanner(String newPlanner) {
        this.planner = newPlanner;
    }

    public final String getPlanner() {
        return this.planner;
    }

    final void setSettings(int newSettings) {
        this.permissions.setPermissionBits(newSettings);
    }

    public final Permissions getSettings() {
        return this.permissions;
    }

    public final void setName(String aName, boolean saveIt) {
        this.name = aName.substring(0, Math.min(255, aName.length()));
        VolaTile[] vtiles = this.getStructureTiles();
        for (int x = 0; x < vtiles.length; ++x) {
            vtiles[x].changeStructureName(aName);
        }
        if (saveIt) {
            try {
                this.saveName();
            }
            catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    @Override
    public final String getTypeName() {
        return "Building";
    }

    @Override
    public final String getObjectName() {
        return this.name;
    }

    @Override
    public final boolean setObjectName(String newName, Creature creature) {
        if (this.writid != -10L) {
            try {
                Item writ = Items.getItem(this.getWritId());
                if (writ.getOwnerId() != creature.getWurmId()) {
                    return false;
                }
                writ.setDescription(newName);
            }
            catch (NoSuchItemException nsi) {
                this.writid = -10L;
                try {
                    this.saveWritId();
                }
                catch (IOException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        this.setName(newName, false);
        return true;
    }

    final void setRoof(byte aRoof) {
        this.roof = aRoof;
    }

    public final byte getRoof() {
        return this.roof;
    }

    public final long getOwnerId() {
        PlayerInfo pInfo;
        if (this.writid != -10L) {
            try {
                Item writ = Items.getItem(this.writid);
                if (this.ownerId != writ.getOwnerId()) {
                    this.ownerId = writ.getOwnerId();
                    this.saveOwnerId();
                }
                return writ.getOwnerId();
            }
            catch (NoSuchItemException nsi) {
                this.setWritid(-10L, true);
            }
            catch (IOException ioe) {
                logger.log(Level.WARNING, ioe.getMessage(), ioe);
            }
        }
        if (this.ownerId == -10L && this.planner.length() > 0 && (pInfo = PlayerInfoFactory.getPlayerInfoWithName(this.planner)) != null) {
            this.ownerId = pInfo.wurmId;
            try {
                this.saveOwnerId();
            }
            catch (IOException ioe) {
                logger.log(Level.WARNING, ioe.getMessage(), ioe);
            }
        }
        return this.ownerId;
    }

    final void setOwnerId(long newOwnerId) {
        this.ownerId = newOwnerId;
    }

    public final int getVillageId() {
        return this.villageId;
    }

    public final void setVillageId(int newVillageId) {
        this.villageId = newVillageId;
    }

    public final Village getManagedByVillage() {
        if (this.villageId >= 0) {
            try {
                return Villages.getVillage(this.villageId);
            }
            catch (NoSuchVillageException noSuchVillageException) {
                // empty catch block
            }
        }
        return null;
    }

    public final long getWritId() {
        return this.writid;
    }

    public boolean isEnemy(Creature creature) {
        if (creature.getPower() > 1) {
            return false;
        }
        if (this.isGuest(creature)) {
            return false;
        }
        Village vil = this.getPermissionsVillage();
        if (vil != null) {
            return vil.isEnemy(creature);
        }
        return !this.isSameKingdom(creature);
    }

    public boolean isEnemyAllowed(Creature creature, short action) {
        Village v = this.getVillage();
        if (v != null && Actions.actionEntrys[action] != null) {
            if (Actions.actionEntrys[action].isEnemyAllowedWhenNoGuards() && v.getGuards().length != 0) {
                return false;
            }
            if (Actions.actionEntrys[action].isEnemyNeverAllowed()) {
                return false;
            }
            if (Actions.actionEntrys[action].isEnemyAlwaysAllowed()) {
                return true;
            }
        }
        return true;
    }

    public boolean mayLockPick(Creature creature) {
        Village v;
        if (Servers.isThisAPvpServer() && this.isEnemyAllowed(creature, (short)101)) {
            return true;
        }
        Village village = v = this.getManagedByVillage() == null ? this.getVillage() : this.getManagedByVillage();
        if (v != null) {
            return v.getRoleFor(creature).mayPickLocks();
        }
        return this.mayManage(creature) || Servers.isThisAPvpServer();
    }

    @Override
    public boolean isCitizen(Creature creature) {
        Village vil = this.getPermissionsVillage();
        if (vil != null) {
            return vil.isCitizen(creature);
        }
        return false;
    }

    public boolean isActionAllowed(Creature performer, short action) {
        if (performer.getPower() > 1) {
            return true;
        }
        if (this.isEnemy(performer) && !this.isEnemyAllowed(performer, action)) {
            return false;
        }
        if (Actions.isActionAttachLock(action)) {
            return this.isEnemy(performer) || this.mayManage(performer);
        }
        if (Actions.isActionChangeBuilding(action)) {
            return this.mayManage(performer);
        }
        if (Actions.isActionLockPick(action)) {
            return this.mayLockPick(performer);
        }
        if (Actions.isActionTake(action) || Actions.isActionPullPushTurn(action) || 671 == action || 672 == action || Actions.isActionPick(action)) {
            return this.isEnemy(performer) || this.mayPickup(performer);
        }
        if (Actions.isActionPickupPlanted(action)) {
            return this.mayPickupPlanted(performer);
        }
        if (Actions.isActionPlaceMerchants(action)) {
            return this.mayPlaceMerchants(performer);
        }
        if (Actions.isActionDestroy(action) || Actions.isActionBuild(action) || Actions.isActionDestroyFence(action) || Actions.isActionPlanBuilding(action) || Actions.isActionPack(action) || Actions.isActionPave(action) || Actions.isActionDestroyItem(action)) {
            return this.isEnemy(performer) || this.mayModify(performer);
        }
        if (Actions.isActionLoad(action) || Actions.isActionUnload(action)) {
            return this.mayLoad(performer);
        }
        if (Actions.isActionDrop(action) && this.isEnemy(performer)) {
            return true;
        }
        if (Actions.isActionImproveOrRepair(action) || Actions.isActionDrop(action)) {
            return this.isEnemy(performer) || this.mayPass(performer);
        }
        return true;
    }

    @Override
    public boolean isAllied(Creature creature) {
        Village vil = this.getPermissionsVillage();
        if (vil != null) {
            return vil.isAlly(creature);
        }
        return false;
    }

    @Override
    public boolean isSameKingdom(Creature creature) {
        Village vill = this.getPermissionsVillage();
        if (vill != null) {
            return vill.kingdom == creature.getKingdomId();
        }
        return Players.getInstance().getKingdomForPlayer(this.getOwnerId()) == creature.getKingdomId();
    }

    public Village getVillage() {
        Village village = Villages.getVillage(this.getMinX(), this.getMinY(), this.isSurfaced());
        if (village == null) {
            return null;
        }
        Village v = Villages.getVillage(this.getMinX(), this.getMaxY(), this.isSurfaced());
        if (v == null || v.getId() != village.getId()) {
            return null;
        }
        v = Villages.getVillage(this.getMaxX(), this.getMaxY(), this.isSurfaced());
        if (v == null || v.getId() != village.getId()) {
            return null;
        }
        v = Villages.getVillage(this.getMinX(), this.getMinY(), this.isSurfaced());
        if (v == null || v.getId() != village.getId()) {
            return null;
        }
        return village;
    }

    private Village getPermissionsVillage() {
        Village vill = this.getManagedByVillage();
        if (vill != null) {
            return vill;
        }
        long wid = this.getOwnerId();
        if (wid != -10L) {
            return Villages.getVillageForCreature(wid);
        }
        return null;
    }

    private String getVillageName(Player player) {
        String sName = "";
        Village vill = this.getVillage();
        sName = vill != null ? vill.getName() : player.getVillageName();
        return sName;
    }

    @Override
    public boolean canChangeName(Creature creature) {
        return creature.getPower() > 1 || this.isOwner(creature.getWurmId());
    }

    @Override
    public boolean canChangeOwner(Creature creature) {
        return (this.isActualOwner(creature.getWurmId()) || creature.getPower() > 1) && this.writid == -10L;
    }

    @Override
    public String getWarning() {
        if (!this.isFinished()) {
            return "NEEDS TO BE COMPLETE FOR INTERIOR PERMISSIONS TO WORK";
        }
        return "";
    }

    @Override
    public PermissionsPlayerList getPermissionsPlayerList() {
        return StructureSettings.getPermissionsPlayerList(this.getWurmId());
    }

    public Floor[] getFloors() {
        HashSet<Floor> floors = new HashSet<Floor>();
        for (VolaTile tile : this.structureTiles) {
            Floor[] fArr = tile.getFloors();
            for (int x = 0; x < fArr.length; ++x) {
                floors.add(fArr[x]);
            }
        }
        Floor[] toReturn = new Floor[floors.size()];
        return floors.toArray(toReturn);
    }

    public Floor[] getFloorsAtTile(int tilex, int tiley, int offsetHeightStart, int offsetHeightEnd) {
        HashSet<Floor> floors = new HashSet<Floor>();
        for (VolaTile tile : this.structureTiles) {
            if (tile.getTileX() != tilex || tile.getTileY() != tiley) continue;
            Floor[] fArr = tile.getFloors(offsetHeightStart, offsetHeightEnd);
            for (int x = 0; x < fArr.length; ++x) {
                floors.add(fArr[x]);
            }
        }
        Floor[] toReturn = new Floor[floors.size()];
        return floors.toArray(toReturn);
    }

    public final Wall[] getWalls() {
        HashSet<Wall> walls = new HashSet<Wall>();
        for (VolaTile tile : this.structureTiles) {
            Wall[] wArr = tile.getWalls();
            for (int x = 0; x < wArr.length; ++x) {
                walls.add(wArr[x]);
            }
        }
        Wall[] toReturn = new Wall[walls.size()];
        return walls.toArray(toReturn);
    }

    public final Wall[] getExteriorWalls() {
        HashSet<Wall> walls = new HashSet<Wall>();
        for (VolaTile tile : this.structureTiles) {
            Wall[] wArr = tile.getExteriorWalls();
            for (int x = 0; x < wArr.length; ++x) {
                walls.add(wArr[x]);
            }
        }
        Wall[] toReturn = new Wall[walls.size()];
        return walls.toArray(toReturn);
    }

    public BridgePart[] getBridgeParts() {
        HashSet<BridgePart> bridgeParts = new HashSet<BridgePart>();
        for (VolaTile tile : this.structureTiles) {
            BridgePart[] fArr = tile.getBridgeParts();
            for (int x = 0; x < fArr.length; ++x) {
                bridgeParts.add(fArr[x]);
            }
        }
        BridgePart[] toReturn = new BridgePart[bridgeParts.size()];
        return bridgeParts.toArray(toReturn);
    }

    public final VolaTile getTileFor(Wall wall) {
        for (int xx = 1; xx >= -1; --xx) {
            for (int yy = 1; yy >= -1; --yy) {
                try {
                    Zone zone = Zones.getZone(wall.tilex + xx, wall.tiley + yy, this.surfaced);
                    VolaTile tile = zone.getTileOrNull(wall.tilex + xx, wall.tiley + yy);
                    if (tile == null) continue;
                    Wall[] walls = tile.getWalls();
                    for (int s = 0; s < walls.length; ++s) {
                        if (walls[s] != wall) continue;
                        return tile;
                    }
                    continue;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    public final void poll(long time) {
        if (time - this.lastPolled > 3600000L) {
            this.lastPolled = System.currentTimeMillis();
            if (!this.isFinalized()) {
                if (time - this.creationDate > 10800000L) {
                    logger.log(Level.INFO, "Deleting unfinished structure " + this.getName());
                    this.totallyDestroy();
                }
            } else {
                boolean destroy = false;
                if (time - this.creationDate > 172800000L) {
                    destroy = true;
                    if (this.structureType == 0) {
                        if (this.hasWalls()) {
                            destroy = false;
                        }
                    } else if (this.getBridgeParts().length != 0) {
                        destroy = false;
                    }
                }
                if (destroy) {
                    this.totallyDestroy();
                }
            }
        }
    }

    public final boolean hasWalls() {
        for (VolaTile tile : this.structureTiles) {
            Wall[] wallArr = tile.getWalls();
            for (int x = 0; x < wallArr.length; ++x) {
                if (wallArr[x].getType() == StructureTypeEnum.PLAN) continue;
                return true;
            }
        }
        return false;
    }

    public final void totallyDestroy() {
        Players.getInstance().setStructureFinished(this.wurmId);
        if (this.isFinalized()) {
            if (this.getWritId() != -10L || this.structureType != 1) {
                try {
                    Item writ = Items.getItem(this.getWritId());
                    try {
                        Server.getInstance().getCreature(writ.getOwnerId());
                        Items.destroyItem(this.getWritId());
                    }
                    catch (NoSuchCreatureException nsc) {
                        Items.decay(this.getWritId(), null);
                    }
                    catch (NoSuchPlayerException nsp) {
                        Items.decay(this.getWritId(), null);
                    }
                }
                catch (NoSuchItemException writ) {
                    // empty catch block
                }
            }
            if (this.structureType == 0) {
                for (VolaTile vt : this.structureTiles) {
                    BridgePart[] bps;
                    Structure structWest;
                    VolaTile vtWest;
                    BridgePart[] bps2;
                    Structure structSouth;
                    VolaTile vtSouth;
                    BridgePart[] bps3;
                    Structure structEast;
                    VolaTile vtEast;
                    BridgePart[] bps4;
                    Structure structNorth;
                    VolaTile vtNorth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() - 1, vt.isOnSurface());
                    if (vtNorth != null && (structNorth = vtNorth.getStructure()) != null && structNorth.isTypeBridge() && (bps4 = vtNorth.getBridgeParts()).length == 1 && bps4[0].hasHouseSouthExit()) {
                        structNorth.totallyDestroy();
                    }
                    if ((vtEast = Zones.getTileOrNull(vt.getTileX() + 1, vt.getTileY(), vt.isOnSurface())) != null && (structEast = vtEast.getStructure()) != null && structEast.isTypeBridge() && (bps3 = vtEast.getBridgeParts()).length == 1 && bps3[0].hasHouseWestExit()) {
                        structEast.totallyDestroy();
                    }
                    if ((vtSouth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() + 1, vt.isOnSurface())) != null && (structSouth = vtSouth.getStructure()) != null && structSouth.isTypeBridge() && (bps2 = vtSouth.getBridgeParts()).length == 1 && bps2[0].hasHouseNorthExit()) {
                        structSouth.totallyDestroy();
                    }
                    if ((vtWest = Zones.getTileOrNull(vt.getTileX() - 1, vt.getTileY(), vt.isOnSurface())) == null || (structWest = vtWest.getStructure()) == null || !structWest.isTypeBridge() || (bps = vtWest.getBridgeParts()).length != 1 || !bps[0].hasHouseEastExit()) continue;
                    structWest.totallyDestroy();
                }
            }
            MissionTargets.destroyStructureTargets(this.getWurmId(), null);
        }
        for (VolaTile tile : this.structureTiles) {
            tile.deleteStructure(this.getWurmId());
        }
        this.remove();
        this.delete();
    }

    public final boolean hasBridgeEntrance() {
        for (VolaTile vt : this.structureTiles) {
            BridgePart[] bps;
            Structure structWest;
            BridgePart[] bps2;
            Structure structSouth;
            BridgePart[] bps3;
            Structure structEast;
            BridgePart[] bps4;
            Structure structNorth;
            if (!vt.isOnSurface()) continue;
            VolaTile vtNorth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() - 1, vt.isOnSurface());
            if (vtNorth != null && (structNorth = vtNorth.getStructure()) != null && structNorth.isTypeBridge() && (bps4 = vtNorth.getBridgeParts()).length == 1 && bps4[0].hasHouseSouthExit()) {
                return true;
            }
            VolaTile vtEast = Zones.getTileOrNull(vt.getTileX() + 1, vt.getTileY(), vt.isOnSurface());
            if (vtEast != null && (structEast = vtEast.getStructure()) != null && structEast.isTypeBridge() && (bps3 = vtEast.getBridgeParts()).length == 1 && bps3[0].hasHouseWestExit()) {
                return true;
            }
            VolaTile vtSouth = Zones.getTileOrNull(vt.getTileX(), vt.getTileY() + 1, vt.isOnSurface());
            if (vtSouth != null && (structSouth = vtSouth.getStructure()) != null && structSouth.isTypeBridge() && (bps2 = vtSouth.getBridgeParts()).length == 1 && bps2[0].hasHouseNorthExit()) {
                return true;
            }
            VolaTile vtWest = Zones.getTileOrNull(vt.getTileX() - 1, vt.getTileY(), vt.isOnSurface());
            if (vtWest == null || (structWest = vtWest.getStructure()) == null || !structWest.isTypeBridge() || (bps = vtWest.getBridgeParts()).length != 1 || !bps[0].hasHouseEastExit()) continue;
            return true;
        }
        return false;
    }

    public final void remove() {
        if (this.structureTiles.size() > 0) {
            Zone[] zones = Zones.getZonesCoveredBy(this.minX, this.minY, this.maxX, this.maxY, this.surfaced);
            for (int x = 0; x < zones.length; ++x) {
                zones[x].removeStructure(this);
            }
        }
        Structures.removeStructure(this.wurmId);
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
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        if (StructureSettings.canManage(this, creature)) {
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
        return vill.isActionAllowed((short)664, creature);
    }

    public boolean mayManage(Creature creature) {
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

    public final boolean mayModify(Creature creature) {
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        return StructureSettings.mayModify(this, creature);
    }

    final boolean isExcluded(Creature creature) {
        return StructureSettings.isExcluded(this, creature);
    }

    public final boolean mayPass(Creature creature) {
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        return StructureSettings.mayPass(this, creature);
    }

    public final boolean mayPickup(Creature creature) {
        if (this.isEnemy(creature)) {
            return true;
        }
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        return StructureSettings.mayPickup(this, creature);
    }

    @Override
    public boolean isGuest(Creature creature) {
        return this.isGuest(creature.getWurmId());
    }

    @Override
    public boolean isGuest(long playerId) {
        return StructureSettings.isGuest((PermissionsPlayerList.ISettings)this, playerId);
    }

    @Override
    public final void addGuest(long guestId, int aSettings) {
        StructureSettings.addPlayer(this.getWurmId(), guestId, aSettings);
    }

    @Override
    public final void removeGuest(long guestId) {
        StructureSettings.removePlayer(this.getWurmId(), guestId);
    }

    public final long getCreationDate() {
        return this.creationDate;
    }

    public final int getSize() {
        return this.structureTiles.size();
    }

    public final int getLimit() {
        return this.structureTiles.size() + this.getExteriorWalls().length;
    }

    public final int getLimitFor(int tilex, int tiley, boolean onSurface, boolean adding) {
        VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, onSurface);
        int points = this.getLimit();
        if (this.contains(tilex, tiley) && adding) {
            return points;
        }
        int newTilePoints = 5;
        if (adding) {
            Set<VolaTile> neighbors = Structure.createNeighbourStructureTiles(this, newTile);
            return (points -= neighbors.size()) + (newTilePoints -= neighbors.size());
        }
        if (this.contains(tilex, tiley)) {
            Set<VolaTile> neighbors = Structure.createNeighbourStructureTiles(this, newTile);
            return (points += neighbors.size()) - (newTilePoints -= neighbors.size());
        }
        return points;
    }

    private void setMaxAndMin() {
        this.maxX = 0;
        this.minX = 1 << Constants.meshSize;
        this.maxY = 0;
        this.minY = 1 << Constants.meshSize;
        if (this.structureTiles != null) {
            for (VolaTile tile : this.structureTiles) {
                int xx = tile.getTileX();
                int yy = tile.getTileY();
                if (xx > this.maxX) {
                    this.maxX = xx;
                }
                if (xx < this.minX) {
                    this.minX = xx;
                }
                if (yy > this.maxY) {
                    this.maxY = yy;
                }
                if (yy >= this.minY) continue;
                this.minY = yy;
            }
        }
    }

    static final StructureBounds getStructureBounds(List<Wall> structureWalls) {
        return null;
    }

    final StructureBounds secureOuterWalls(List<Wall> structureWalls) {
        TilePoint max = new TilePoint(0, 0);
        TilePoint min = new TilePoint(Zones.worldTileSizeX, Zones.worldTileSizeY);
        StructureBounds structBounds = new StructureBounds(max, min);
        for (Wall wall : structureWalls) {
            if (wall.getStartX() > structBounds.max.getTileX()) {
                structBounds.getMax().setTileX(wall.getStartX());
            }
            if (wall.getStartY() > structBounds.max.getTileY()) {
                structBounds.getMax().setTileY(wall.getStartY());
            }
            if (wall.getStartX() < structBounds.min.getTileX()) {
                structBounds.getMin().setTileX(wall.getStartX());
            }
            if (wall.getStartY() >= structBounds.min.getTileY()) continue;
            structBounds.getMin().setTileY(wall.getStartY());
        }
        return structBounds;
    }

    private void fixWalls(VolaTile tile) {
        for (BuildTile bt : this.buildTiles) {
            if (bt.getTileX() != tile.getTileX() || bt.getTileY() != tile.getTileY() || tile.isOnSurface() != (bt.getLayer() == 0)) continue;
            return;
        }
        for (Wall wall : tile.getWalls()) {
            int x = tile.getTileX();
            int y = tile.getTileY();
            int newTileX = 0;
            int newTileY = 0;
            boolean found = false;
            Structure s = null;
            if (wall.isHorizontal()) {
                s = Structures.getStructureForTile(x, y - 1, tile.isOnSurface());
                if (s != null && s.isTypeHouse()) {
                    newTileX = x;
                    newTileY = y - 1;
                    found = true;
                }
                if ((s = Structures.getStructureForTile(x, y + 1, tile.isOnSurface())) != null && s.isTypeHouse()) {
                    newTileX = x;
                    newTileY = y + 1;
                    found = true;
                }
            } else {
                s = Structures.getStructureForTile(x - 1, y, tile.isOnSurface());
                if (s != null && s.isTypeHouse()) {
                    newTileX = x - 1;
                    newTileY = y;
                    found = true;
                }
                if ((s = Structures.getStructureForTile(x + 1, y, tile.isOnSurface())) != null && s.isTypeHouse()) {
                    newTileX = x + 1;
                    newTileY = y;
                    found = true;
                }
            }
            if (!found) {
                logger.log(Level.WARNING, StringUtil.format("Wall with WALL.ID = %d is orphan, but belongs to structure %d. Does the structure exist?", wall.getNumber(), wall.getStructureId()));
                return;
            }
            VolaTile t = Zones.getTileOrNull(newTileX, newTileY, tile.isOnSurface());
            tile.removeWall(wall, true);
            wall.setTile(newTileX, newTileY);
            t.addWall(wall);
            logger.log(Level.WARNING, StringUtil.format("fixWalls found a wall %d at %d,%d and moved it to %d,%d for structure %d", wall.getNumber(), x, y, newTileX, newTileY, wall.getStructureId()));
        }
    }

    final boolean loadStructureTiles(List<Wall> structureWalls) {
        boolean toReturn = true;
        if (!this.buildTiles.isEmpty()) {
            toReturn = false;
            for (BuildTile buildTile : this.buildTiles) {
                try {
                    Zone zone = Zones.getZone(buildTile.getTileX(), buildTile.getTileY(), buildTile.getLayer() == 0);
                    VolaTile tile = zone.getOrCreateTile(buildTile.getTileX(), buildTile.getTileY());
                    this.addBuildTile(tile, true);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, "Structure with id " + this.wurmId + " is built on the edge of the world at " + buildTile.getTileX() + ", " + buildTile.getTileY(), nsz);
                }
            }
        }
        int tilex = 0;
        int tiley = 0;
        for (Wall wall : structureWalls) {
            try {
                tilex = wall.getTileX();
                tiley = wall.getTileY();
                Zone zone = Zones.getZone(tilex, tiley, this.isSurfaced());
                VolaTile tile = zone.getOrCreateTile(tilex, tiley);
                tile.addWall(wall);
                if (!this.structureTiles.contains(tile)) {
                    logger.log(Level.WARNING, "Wall with  WURMZONES.WALLS.ID =" + wall.getId() + " exists outside a structure! ");
                    this.fixWalls(tile);
                }
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, "Failed to locate zone at " + tilex + ", " + tiley);
            }
            if (wall.getType() != StructureTypeEnum.DOOR && wall.getType() != StructureTypeEnum.DOUBLE_DOOR && wall.getType() != StructureTypeEnum.PORTCULLIS && wall.getType() != StructureTypeEnum.CANOPY_DOOR) continue;
            if (this.doors == null) {
                this.doors = new HashSet<Door>();
            }
            DbDoor door = new DbDoor(wall);
            this.addDoor(door);
            door.addToTiles();
            try {
                ((Door)door).load();
            }
            catch (IOException e) {
                logger.log(Level.WARNING, "Failed to load a door: " + e.getMessage(), e);
            }
        }
        this.buildTiles.clear();
        return toReturn;
    }

    final boolean fillHoles() {
        int numTiles = this.structureTiles.size() + 3;
        HashSet<VolaTile> tilesToAdd = new HashSet<VolaTile>();
        HashSet<VolaTile> tilesChecked = new HashSet<VolaTile>();
        HashSet<VolaTile> tilesRemaining = new HashSet<VolaTile>();
        tilesRemaining.addAll(this.structureTiles);
        int iterations = 0;
        while (iterations++ < numTiles) {
            for (VolaTile tile : tilesRemaining) {
                tilesChecked.add(tile);
                Wall[] walls = tile.getWalls();
                boolean checkNorth = true;
                boolean checkEast = true;
                boolean checkSouth = true;
                boolean checkWest = true;
                for (int x = 0; x < walls.length; ++x) {
                    if (walls[x].isIndoor()) continue;
                    if (walls[x].getHeight() > 0) {
                        logger.log(Level.INFO, "Wall at " + tile.getTileX() + "," + tile.getTileY() + " not indoor at height " + walls[x].getHeight());
                    }
                    if (walls[x].isHorizontal()) {
                        if (walls[x].getStartY() == tile.getTileY()) {
                            checkNorth = false;
                            continue;
                        }
                        checkSouth = false;
                        continue;
                    }
                    if (walls[x].getStartX() == tile.getTileX()) {
                        checkWest = false;
                        continue;
                    }
                    checkEast = false;
                }
                if (checkNorth) {
                    try {
                        VolaTile t = Zones.getZone(tile.tilex, tile.tiley - 1, this.surfaced).getOrCreateTile(tile.tilex, tile.tiley - 1);
                        if (!this.structureTiles.contains(t) && !tilesToAdd.contains(t)) {
                            tilesToAdd.add(t);
                        }
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, "CN Structure with id " + this.wurmId + " is built on the edge of the world at " + tile.getTileX() + ", " + tile.getTileY());
                    }
                }
                if (checkEast) {
                    try {
                        VolaTile t = Zones.getZone(tile.tilex + 1, tile.tiley, this.surfaced).getOrCreateTile(tile.tilex + 1, tile.tiley);
                        if (!this.structureTiles.contains(t) && !tilesToAdd.contains(t)) {
                            tilesToAdd.add(t);
                        }
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, "CE Structure with id " + this.wurmId + " is built on the edge of the world at " + tile.getTileX() + ", " + tile.getTileY());
                    }
                }
                if (checkWest) {
                    try {
                        VolaTile t = Zones.getZone(tile.tilex - 1, tile.tiley, this.surfaced).getOrCreateTile(tile.tilex - 1, tile.tiley);
                        if (!this.structureTiles.contains(t) && !tilesToAdd.contains(t)) {
                            tilesToAdd.add(t);
                        }
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, "CW Structure with id " + this.wurmId + " is built on the edge of the world at " + tile.getTileX() + ", " + tile.getTileY());
                    }
                }
                if (!checkSouth) continue;
                try {
                    VolaTile t = Zones.getZone(tile.tilex, tile.tiley + 1, this.surfaced).getOrCreateTile(tile.tilex, tile.tiley + 1);
                    if (this.structureTiles.contains(t) || tilesToAdd.contains(t)) continue;
                    tilesToAdd.add(t);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, "CS Structure with id " + this.wurmId + " is built on the edge of the world at " + tile.getTileX() + ", " + tile.getTileY());
                }
            }
            tilesRemaining.removeAll(tilesChecked);
            if (tilesToAdd.size() > 0) {
                for (VolaTile tile : tilesToAdd) {
                    try {
                        if (tile.getTileX() > this.maxX) {
                            this.maxX = tile.getTileX();
                        }
                        if (tile.getTileX() < this.minX) {
                            this.minX = tile.getTileX();
                        }
                        if (tile.getTileY() > this.maxY) {
                            this.maxY = tile.getTileY();
                        }
                        if (tile.getTileY() < this.minY) {
                            this.minY = tile.getTileY();
                        }
                        Zone zone = Zones.getZone(tile.getTileX(), tile.getTileY(), this.isSurfaced());
                        zone.addStructure(this);
                        this.structureTiles.add(tile);
                        this.addNewBuildTile(tile.getTileX(), tile.getTileY(), tile.getLayer());
                        tile.setStructureAtLoad(this);
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, "Structure with id " + this.wurmId + " is built on the edge of the world at " + tile.getTileX() + ", " + tile.getTileY(), nsz);
                    }
                }
                tilesRemaining.addAll(tilesToAdd);
                tilesToAdd.clear();
                continue;
            }
            return false;
        }
        logger.log(Level.WARNING, "Iterations went over " + numTiles + " for " + this.getName() + " at " + this.getCenterX() + ", " + this.getCenterY());
        return false;
    }

    static final boolean isEqual(Structure struct1, Structure struct2) {
        if (struct1 == null) {
            return false;
        }
        if (struct2 == null) {
            return false;
        }
        return struct1.getWurmId() == struct2.getWurmId();
    }

    static final Set<VolaTile> createNeighbourStructureTiles(Structure struct, VolaTile modifiedTile) {
        HashSet<VolaTile> toReturn = new HashSet<VolaTile>();
        VolaTile t = Zones.getTileOrNull(modifiedTile.getTileX() + 1, modifiedTile.getTileY(), modifiedTile.isOnSurface());
        if (t != null && Structure.isEqual(t.getStructure(), struct)) {
            toReturn.add(t);
        }
        if ((t = Zones.getTileOrNull(modifiedTile.getTileX(), modifiedTile.getTileY() + 1, modifiedTile.isOnSurface())) != null && Structure.isEqual(t.getStructure(), struct)) {
            toReturn.add(t);
        }
        if ((t = Zones.getTileOrNull(modifiedTile.getTileX(), modifiedTile.getTileY() - 1, modifiedTile.isOnSurface())) != null && Structure.isEqual(t.getStructure(), struct)) {
            toReturn.add(t);
        }
        if ((t = Zones.getTileOrNull(modifiedTile.getTileX() - 1, modifiedTile.getTileY(), modifiedTile.isOnSurface())) != null && Structure.isEqual(t.getStructure(), struct)) {
            toReturn.add(t);
        }
        return toReturn;
    }

    public static void adjustWallsAroundAddedStructureTile(Structure structure, int tilex, int tiley) {
        VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, structure.isOnSurface());
        Set<VolaTile> neighbourTiles = Structure.createNeighbourStructureTiles(structure, newTile);
        structure.adjustSurroundingWallsAddedStructureTile(tilex, tiley, neighbourTiles);
    }

    public static void adjustWallsAroundRemovedStructureTile(Structure structure, int tilex, int tiley) {
        VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, structure.isOnSurface());
        Set<VolaTile> neighbourTiles = Structure.createNeighbourStructureTiles(structure, newTile);
        structure.adjustSurroundingWallsRemovedStructureTile(tilex, tiley, neighbourTiles);
    }

    public void updateWallIsInner(Structure localStructure, VolaTile volaTile, Wall wall, boolean isInner) {
        if (localStructure.getWurmId() != this.getWurmId()) {
            logger.log(Level.WARNING, "Warning structures too close to eachother: " + localStructure.getWurmId() + " and " + this.getWurmId() + " at " + volaTile.getTileX() + "," + volaTile.getTileY());
            return;
        }
        if (wall.getHeight() > 0) {
            wall.setIndoor(true);
        } else {
            wall.setIndoor(isInner);
        }
        volaTile.updateWall(wall);
    }

    public void adjustSurroundingWallsAddedStructureTile(int tilex, int tiley, Set<VolaTile> neighbourTiles) {
        VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, this.isOnSurface());
        for (VolaTile neighbourTile : neighbourTiles) {
            Wall[] walls;
            Structure localStructure = neighbourTile.getStructure();
            for (Wall wall : walls = neighbourTile.getWalls()) {
                if (wall.isHorizontal() && wall.getStartY() == tiley && wall.getEndY() == tiley && wall.getStartX() == tilex && wall.getEndX() == tilex + 1) {
                    if (this.isFree(tilex, tiley - 1)) {
                        this.updateWallIsInner(localStructure, newTile, wall, false);
                    } else {
                        this.updateWallIsInner(localStructure, newTile, wall, true);
                    }
                }
                if (wall.isHorizontal() && wall.getStartY() == tiley + 1 && wall.getEndY() == tiley + 1 && wall.getStartX() == tilex && wall.getEndX() == tilex + 1) {
                    if (this.isFree(tilex, tiley + 1)) {
                        this.updateWallIsInner(localStructure, newTile, wall, false);
                    } else {
                        this.updateWallIsInner(localStructure, newTile, wall, true);
                    }
                }
                if (!wall.isHorizontal() && wall.getStartX() == tilex + 1 && wall.getEndX() == tilex + 1 && wall.getStartY() == tiley && wall.getEndY() == tiley + 1) {
                    if (this.isFree(tilex + 1, tiley)) {
                        this.updateWallIsInner(localStructure, newTile, wall, false);
                    } else {
                        this.updateWallIsInner(localStructure, newTile, wall, true);
                    }
                }
                if (wall.isHorizontal() || wall.getStartX() != tilex || wall.getEndX() != tilex || wall.getStartY() != tiley || wall.getEndY() != tiley + 1) continue;
                if (this.isFree(tilex - 1, tiley)) {
                    this.updateWallIsInner(localStructure, newTile, wall, false);
                    continue;
                }
                this.updateWallIsInner(localStructure, newTile, wall, true);
            }
        }
    }

    public void adjustSurroundingWallsRemovedStructureTile(int tilex, int tiley, Set<VolaTile> neighbourTiles) {
        VolaTile removedTile = Zones.getOrCreateTile(tilex, tiley, this.isOnSurface());
        for (VolaTile neighbourTile : neighbourTiles) {
            Wall[] walls;
            Structure localStructure = neighbourTile.getStructure();
            for (Wall wall : walls = neighbourTile.getWalls()) {
                if (wall.isHorizontal() && wall.getStartX() == tilex && wall.getEndX() == tilex + 1 && wall.getStartY() == tiley && wall.getEndY() == tiley) {
                    if (this.isFree(tilex, tiley - 1)) {
                        logger.log(Level.WARNING, "Wall exist.");
                    } else {
                        this.updateWallIsInner(localStructure, removedTile, wall, false);
                    }
                }
                if (wall.isHorizontal() && wall.getStartX() == tilex && wall.getEndX() == tilex + 1 && wall.getStartY() == tiley + 1 && wall.getEndY() == tiley + 1) {
                    if (this.isFree(tilex, tiley + 1)) {
                        logger.log(Level.WARNING, "Wall exist.");
                    } else {
                        this.updateWallIsInner(localStructure, removedTile, wall, false);
                    }
                }
                if (!wall.isHorizontal() && wall.getStartX() == tilex + 1 && wall.getEndX() == tilex + 1 && wall.getStartY() == tiley && wall.getEndY() == tiley + 1) {
                    if (this.isFree(tilex + 1, tiley)) {
                        logger.log(Level.WARNING, "Walls exist.");
                    } else {
                        this.updateWallIsInner(localStructure, removedTile, wall, false);
                    }
                }
                if (wall.isHorizontal() || wall.getStartX() != tilex || wall.getEndX() != tilex || wall.getStartY() != tiley || wall.getEndY() != tiley + 1) continue;
                if (this.isFree(tilex - 1, tiley)) {
                    logger.log(Level.WARNING, "Walls exist.");
                    continue;
                }
                this.updateWallIsInner(localStructure, removedTile, wall, false);
            }
        }
    }

    public void addMissingWallPlans(VolaTile tile) {
        boolean lacksNorth = true;
        boolean lacksSouth = true;
        boolean lacksWest = true;
        boolean lacksEast = true;
        for (Wall w : tile.getWallsForLevel(0)) {
            if (w.isHorizontal()) {
                if (w.getStartY() == tile.tiley) {
                    lacksNorth = false;
                }
                if (w.getStartY() != tile.tiley + 1) continue;
                lacksSouth = false;
                continue;
            }
            if (w.getStartX() == tile.tilex) {
                lacksWest = false;
            }
            if (w.getStartX() != tile.tilex + 1) continue;
            lacksEast = false;
        }
        if (lacksWest && this.isFree(tile.tilex - 1, tile.tiley)) {
            tile.addWall(StructureTypeEnum.PLAN, tile.tilex, tile.tiley, tile.tilex, tile.tiley + 1, 10.0f, this.wurmId, false);
        }
        if (lacksEast && this.isFree(tile.tilex + 1, tile.tiley)) {
            tile.addWall(StructureTypeEnum.PLAN, tile.tilex + 1, tile.tiley, tile.tilex + 1, tile.tiley + 1, 10.0f, this.wurmId, false);
        }
        if (lacksNorth && this.isFree(tile.tilex, tile.tiley - 1)) {
            tile.addWall(StructureTypeEnum.PLAN, tile.tilex, tile.tiley, tile.tilex + 1, tile.tiley, 10.0f, this.wurmId, false);
        }
        if (lacksSouth && this.isFree(tile.tilex, tile.tiley + 1)) {
            tile.addWall(StructureTypeEnum.PLAN, tile.tilex, tile.tiley + 1, tile.tilex + 1, tile.tiley + 1, 10.0f, this.wurmId, false);
        }
    }

    public static final VolaTile expandStructureToTile(Structure structure, VolaTile toAdd) throws NoSuchZoneException {
        structure.structureTiles.add(toAdd);
        toAdd.getZone().addStructure(structure);
        return toAdd;
    }

    public final void addBuildTile(VolaTile toAdd, boolean loading) throws NoSuchZoneException {
        if (toAdd.tilex > this.maxX) {
            this.maxX = toAdd.tilex;
        }
        if (toAdd.tilex < this.minX) {
            this.minX = toAdd.tilex;
        }
        if (toAdd.tiley > this.maxY) {
            this.maxY = toAdd.tiley;
        }
        if (toAdd.tiley < this.minY) {
            this.minY = toAdd.tiley;
        }
        if (this.buildTiles.isEmpty() && this.isFinalized()) {
            this.addNewBuildTile(toAdd.tilex, toAdd.tiley, toAdd.getLayer());
        }
        Structure.expandStructureToTile(this, toAdd);
        if (this.structureType == 0) {
            toAdd.addBuildMarker(this);
        } else if (loading) {
            toAdd.setStructureAtLoad(this);
        }
    }

    private static final VolaTile getFirstNeighbourTileOrNull(VolaTile structureTile) {
        VolaTile t = Zones.getTileOrNull(structureTile.getTileX() + 1, structureTile.getTileY(), structureTile.isOnSurface());
        if (t != null && t.getStructure() == structureTile.getStructure()) {
            return t;
        }
        t = Zones.getTileOrNull(structureTile.getTileX(), structureTile.getTileY() + 1, structureTile.isOnSurface());
        if (t != null && t.getStructure() == structureTile.getStructure()) {
            return t;
        }
        t = Zones.getTileOrNull(structureTile.getTileX(), structureTile.getTileY() - 1, structureTile.isOnSurface());
        if (t != null && t.getStructure() == structureTile.getStructure()) {
            return t;
        }
        t = Zones.getTileOrNull(structureTile.getTileX() - 1, structureTile.getTileY(), structureTile.isOnSurface());
        if (t != null && t.getStructure() == structureTile.getStructure()) {
            return t;
        }
        return null;
    }

    public final boolean testRemove(VolaTile tileToCheck) {
        if (this.structureTiles.size() <= 2) {
            return true;
        }
        HashSet<VolaTile> remainingTiles = new HashSet<VolaTile>();
        HashSet<VolaTile> removedTiles = new HashSet<VolaTile>();
        remainingTiles.addAll(this.structureTiles);
        remainingTiles.remove(tileToCheck);
        VolaTile firstNeighbour = Structure.getFirstNeighbourTileOrNull(tileToCheck);
        if (firstNeighbour == null) {
            return true;
        }
        removedTiles.add(firstNeighbour);
        HashSet<VolaTile> tilesToRemove = new HashSet<VolaTile>();
        while (true) {
            for (VolaTile removed : removedTiles) {
                for (VolaTile remaining : remainingTiles) {
                    if (!removed.isNextTo(remaining)) continue;
                    tilesToRemove.add(remaining);
                }
            }
            if (tilesToRemove.isEmpty()) {
                return remainingTiles.isEmpty();
            }
            removedTiles.addAll(tilesToRemove);
            remainingTiles.removeAll(tilesToRemove);
            tilesToRemove.clear();
        }
    }

    public final boolean removeTileFromFinishedStructure(Creature performer, int tilex, int tiley, int layer) {
        VolaTile southTile;
        VolaTile northTile;
        VolaTile eastTile;
        Floor[] floorArray;
        Wall[] walls;
        if (this.structureTiles == null) {
            return false;
        }
        VolaTile toRemove = null;
        for (VolaTile volaTile : this.structureTiles) {
            int xx = volaTile.getTileX();
            int yy = volaTile.getTileY();
            if (xx != tilex || yy != tiley) continue;
            toRemove = volaTile;
            break;
        }
        if (!this.testRemove(toRemove)) {
            return false;
        }
        for (Wall wall : walls = toRemove.getWalls()) {
            toRemove.removeWall(wall, false);
            wall.delete();
        }
        for (Floor floor : floorArray = toRemove.getFloors()) {
            toRemove.removeFloor(floor);
            floor.delete();
        }
        this.structureTiles.remove(toRemove);
        this.removeBuildTile(tilex, tiley, layer);
        MethodsStructure.removeBuildMarker(this, tilex, tiley);
        this.setMaxAndMin();
        VolaTile westTile = Zones.getTileOrNull(toRemove.getTileX() - 1, toRemove.getTileY(), toRemove.isOnSurface());
        if (westTile != null && westTile.getStructure() == this) {
            this.addMissingWallPlans(westTile);
        }
        if ((eastTile = Zones.getTileOrNull(toRemove.getTileX() + 1, toRemove.getTileY(), toRemove.isOnSurface())) != null && eastTile.getStructure() == this) {
            this.addMissingWallPlans(eastTile);
        }
        if ((northTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() - 1, toRemove.isOnSurface())) != null && northTile.getStructure() == this) {
            this.addMissingWallPlans(northTile);
        }
        if ((southTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() + 1, toRemove.isOnSurface())) != null && southTile.getStructure() == this) {
            this.addMissingWallPlans(southTile);
        }
        Structure.adjustWallsAroundRemovedStructureTile(this, tilex, tiley);
        return true;
    }

    public final boolean removeTileFromPlannedStructure(Creature aPlanner, int tilex, int tiley) {
        boolean allowed = false;
        if (this.structureTiles == null) {
            return false;
        }
        VolaTile toRemove = null;
        for (VolaTile volaTile : this.structureTiles) {
            int xx = volaTile.getTileX();
            int yy = volaTile.getTileY();
            if (xx != tilex || yy != tiley) continue;
            toRemove = volaTile;
            break;
        }
        if (toRemove == null) {
            logger.warning("Tile " + tilex + "," + tiley + " was not part of structure '" + this.getWurmId() + "'");
            return false;
        }
        if (this.testRemove(toRemove)) {
            VolaTile southTile;
            VolaTile northTile;
            VolaTile eastTile;
            Wall[] walls;
            allowed = true;
            for (Wall wall : walls = toRemove.getWalls()) {
                toRemove.removeWall(wall, false);
                wall.delete();
            }
            this.structureTiles.remove(toRemove);
            MethodsStructure.removeBuildMarker(this, tilex, tiley);
            this.setMaxAndMin();
            VolaTile volaTile = Zones.getTileOrNull(toRemove.getTileX() - 1, toRemove.getTileY(), toRemove.isOnSurface());
            if (volaTile != null && volaTile.getStructure() == this) {
                this.addMissingWallPlans(volaTile);
            }
            if ((eastTile = Zones.getTileOrNull(toRemove.getTileX() + 1, toRemove.getTileY(), toRemove.isOnSurface())) != null && eastTile.getStructure() == this) {
                this.addMissingWallPlans(eastTile);
            }
            if ((northTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() - 1, toRemove.isOnSurface())) != null && northTile.getStructure() == this) {
                this.addMissingWallPlans(northTile);
            }
            if ((southTile = Zones.getTileOrNull(toRemove.getTileX(), toRemove.getTileY() + 1, toRemove.isOnSurface())) != null && southTile.getStructure() == this) {
                this.addMissingWallPlans(southTile);
            }
        }
        if (this.structureTiles.isEmpty()) {
            aPlanner.setStructure(null);
            try {
                aPlanner.save();
            }
            catch (Exception iox) {
                logger.log(Level.WARNING, "Failed to save player " + aPlanner.getName() + ", StructureId: " + this.wurmId, iox);
            }
            Structures.removeStructure(this.wurmId);
        }
        return allowed;
    }

    public final void addDoor(Door door) {
        if (this.doors == null) {
            this.doors = new HashSet<Door>();
        }
        if (!this.doors.contains(door)) {
            this.doors.add(door);
            door.setStructureId(this.wurmId);
        }
    }

    public final Door[] getAllDoors() {
        Door[] toReturn = new Door[]{};
        if (this.doors != null && this.doors.size() != 0) {
            toReturn = this.doors.toArray(new Door[this.doors.size()]);
        }
        return toReturn;
    }

    public final Door[] getAllDoors(boolean includeAll) {
        HashSet<Door> ldoors = new HashSet<Door>();
        if (this.doors != null && this.doors.size() != 0) {
            for (Door door : this.doors) {
                if (!includeAll && !door.hasLock()) continue;
                ldoors.add(door);
            }
        }
        return ldoors.toArray(new Door[ldoors.size()]);
    }

    public final void removeDoor(Door door) {
        if (this.doors != null) {
            this.doors.remove(door);
            door.delete();
        }
    }

    public final void unlockAllDoors() {
        Door[] lDoors = this.getAllDoors();
        for (int x = 0; x < lDoors.length; ++x) {
            lDoors[x].unlock(x == 0);
        }
    }

    public final void lockAllDoors() {
        Door[] lDoors = this.getAllDoors();
        for (int x = 0; x < lDoors.length; ++x) {
            lDoors[x].lock(x == 0);
        }
    }

    public final boolean isLocked() {
        Door[] lDoors = this.getAllDoors();
        for (int x = 0; x < lDoors.length; ++x) {
            if (lDoors[x].isLocked()) continue;
            return false;
        }
        return true;
    }

    public final boolean isLockable() {
        Door[] lDoors = this.getAllDoors();
        for (int x = 0; x < lDoors.length; ++x) {
            try {
                lDoors[x].getLock();
                continue;
            }
            catch (NoSuchLockException nsl) {
                return false;
            }
        }
        return true;
    }

    public final boolean isTypeBridge() {
        return this.structureType == 1;
    }

    public final boolean isTypeHouse() {
        return this.structureType == 0;
    }

    private void finalizeBuildPlanForTiles(long oldStructureId) throws IOException {
        for (VolaTile tile : this.structureTiles) {
            tile.finalizeBuildPlan(oldStructureId, this.wurmId);
            this.addNewBuildTile(tile.tilex, tile.tiley, tile.getLayer());
            Wall[] walls = tile.getWalls();
            for (int x = 0; x < walls.length; ++x) {
                walls[x].setStructureId(this.wurmId);
                walls[x].save();
            }
            Floor[] floors = tile.getFloors();
            for (int x = 0; x < floors.length; ++x) {
                floors[x].setStructureId(this.wurmId);
                floors[x].save();
            }
            BridgePart[] bridgeParts = tile.getBridgeParts();
            for (int x = 0; x < bridgeParts.length; ++x) {
                bridgeParts[x].setStructureId(this.wurmId);
                bridgeParts[x].save();
            }
        }
    }

    public final boolean makeFinal(Creature aOwner, String aName) throws IOException, NoSuchZoneException {
        int size = this.structureTiles.size();
        if (size > 0) {
            String sName;
            if (this.structureType == 1) {
                sName = aName;
                Achievements.triggerAchievement(aOwner.getWurmId(), 557);
            } else {
                sName = size <= 2 ? aName + "shed" : (size <= 3 ? aName + "shack" : (size <= 5 ? aName + "cottage" : (size <= 6 ? aName + "house" : (size <= 10 ? aName + "villa" : (size <= 20 ? aName + "mansion" : (size <= 30 ? aName + "estate" : aName + "stronghold"))))));
            }
            long oldStructureId = this.wurmId;
            this.wurmId = WurmId.getNextStructureId();
            Structures.removeStructure(oldStructureId);
            this.name = sName;
            Structures.addStructure(this);
            this.finalizeBuildPlanForTiles(oldStructureId);
            Zone northW = null;
            Zone northE = null;
            Zone southW = null;
            Zone southE = null;
            try {
                northW = Zones.getZone(this.minX, this.minY, this.surfaced);
                northW.addStructure(this);
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            try {
                northE = Zones.getZone(this.maxX, this.minY, this.surfaced);
                if (northE != northW) {
                    northE.addStructure(this);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            try {
                southE = Zones.getZone(this.maxX, this.maxY, this.surfaced);
                if (southE != northE && southE != northW) {
                    southE.addStructure(this);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            try {
                southW = Zones.getZone(this.minX, this.maxY, this.surfaced);
                if (southW != northE && southW != northW && southW != southE) {
                    southW.addStructure(this);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            this.writid = -10L;
            this.setPlanner(aOwner.getName());
            this.setOwnerId(aOwner.getWurmId());
            this.save();
            return true;
        }
        return false;
    }

    public void clearAllWallsAndMakeWallsForStructureBorder(VolaTile toAdd) {
        for (VolaTile tile : Structure.createNeighbourStructureTiles(this, toAdd)) {
            this.destroyWallsBorderingToTile(tile, toAdd);
        }
        this.addMissingWallPlans(toAdd);
    }

    private void destroyWallsBorderingToTile(VolaTile start, VolaTile target) {
        boolean destroy = false;
        for (Wall wall : start.getWalls()) {
            destroy = false;
            if (wall.isHorizontal() && wall.getMinX() == target.getTileX()) {
                if (wall.getMinY() == target.getTileY()) {
                    destroy = true;
                } else if (wall.getMinY() == target.getTileY() + 1) {
                    destroy = true;
                }
            }
            if (!wall.isHorizontal() && wall.getMinY() == target.getTileY()) {
                if (wall.getMinX() == target.getTileX()) {
                    destroy = true;
                } else if (wall.getMinX() == target.getTileX() + 1) {
                    destroy = true;
                }
            }
            if (!destroy) continue;
            start.removeWall(wall, false);
            wall.delete();
        }
    }

    private boolean isFree(int x, int y) {
        return !this.contains(x, y);
    }

    public final boolean isFinished() {
        return this.finished;
    }

    public final boolean isFinalFinished() {
        return this.finalfinished;
    }

    public final boolean needsDoor() {
        int free = 0;
        for (VolaTile tile : this.structureTiles) {
            Wall[] wallArr = tile.getWallsForLevel(0);
            for (int x = 0; x < wallArr.length; ++x) {
                StructureTypeEnum type = wallArr[x].getType();
                if (type == StructureTypeEnum.DOOR) {
                    return false;
                }
                if (type == StructureTypeEnum.DOUBLE_DOOR) {
                    return false;
                }
                if (Wall.isArched(type)) {
                    return false;
                }
                if (type == StructureTypeEnum.PORTCULLIS) {
                    return false;
                }
                if (type == StructureTypeEnum.CANOPY_DOOR) {
                    return false;
                }
                if (type != StructureTypeEnum.PLAN) continue;
                ++free;
            }
        }
        return free < 2;
    }

    public final int getDoors() {
        int numdoors = 0;
        for (VolaTile tile : this.structureTiles) {
            Wall[] wallArr = tile.getWalls();
            for (int x = 0; x < wallArr.length; ++x) {
                StructureTypeEnum type = wallArr[x].getType();
                if (type == StructureTypeEnum.DOOR) {
                    ++numdoors;
                }
                if (type == StructureTypeEnum.DOUBLE_DOOR) {
                    ++numdoors;
                }
                if (Wall.isArched(type)) {
                    ++numdoors;
                }
                if (type == StructureTypeEnum.PORTCULLIS) {
                    ++numdoors;
                }
                if (type != StructureTypeEnum.CANOPY_DOOR) continue;
                ++numdoors;
            }
        }
        return numdoors;
    }

    public boolean updateStructureFinishFlag() {
        for (VolaTile tile : this.structureTiles) {
            int x;
            if (this.structureType == 0) {
                Wall[] wallArr = tile.getWalls();
                for (x = 0; x < wallArr.length; ++x) {
                    if (wallArr[x].isIndoor() || wallArr[x].isFinished()) continue;
                    this.setFinished(false);
                    this.setFinalFinished(false);
                    return false;
                }
                continue;
            }
            BridgePart[] bridgeParts = tile.getBridgeParts();
            for (x = 0; x < bridgeParts.length; ++x) {
                if (bridgeParts[x].isFinished()) continue;
                this.setFinished(false);
                this.setFinalFinished(false);
                return false;
            }
        }
        this.setFinished(true);
        this.setFinalFinished(true);
        Players.getInstance().setStructureFinished(this.wurmId);
        return true;
    }

    public final boolean isFinalized() {
        return WurmId.getType(this.wurmId) == 4;
    }

    public final boolean contains(int tilex, int tiley) {
        if (this.structureTiles == null) {
            logger.log(Level.WARNING, "StructureTiles is null in building with id " + this.wurmId);
            return true;
        }
        for (VolaTile tile : this.structureTiles) {
            if (tilex != tile.tilex || tiley != tile.tiley) continue;
            return true;
        }
        return false;
    }

    public final boolean isOnSurface() {
        return this.surfaced;
    }

    @Override
    public final long getWurmId() {
        return this.wurmId;
    }

    @Override
    public int getTemplateId() {
        return -10;
    }

    @Override
    public int getMaxAllowed() {
        return AnimalSettings.getMaxAllowed();
    }

    @Override
    public boolean isActualOwner(long playerId) {
        return this.getOwnerId() == playerId;
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

    public boolean mayPlaceMerchants(Creature creature) {
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        return StructureSettings.mayPlaceMerchants(this, creature);
    }

    public boolean mayUseBed(Creature creature) {
        if (this.isOwner(creature)) {
            return true;
        }
        if (this.isGuest(creature)) {
            return true;
        }
        if (this.allowsCitizens() && this.isInOwnerSettlement(creature)) {
            return true;
        }
        return this.allowsAllies() && this.isInOwnerAlliance(creature);
    }

    public boolean mayPickupPlanted(Creature creature) {
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        return StructureSettings.mayPickupPlanted(this, creature);
    }

    public boolean mayLoad(Creature creature) {
        if (this.isEnemy(creature)) {
            return true;
        }
        if (StructureSettings.isExcluded(this, creature)) {
            return false;
        }
        return StructureSettings.mayLoad(this, creature);
    }

    public boolean isInOwnerSettlement(Creature creature) {
        long wid;
        if (creature.getCitizenVillage() != null && (wid = this.getOwnerId()) != -10L) {
            Village creatorVillage = Villages.getVillageForCreature(wid);
            return creatorVillage != null && creature.getCitizenVillage().getId() == creatorVillage.getId();
        }
        return false;
    }

    public boolean isInOwnerAlliance(Creature creature) {
        long wid;
        if (creature.getCitizenVillage() != null && (wid = this.getOwnerId()) != -10L) {
            Village creatorVillage = Villages.getVillageForCreature(wid);
            return creatorVillage != null && creature.getCitizenVillage().isAlly(creatorVillage);
        }
        return false;
    }

    public final int getCenterX() {
        return this.minX + Math.max(1, this.maxX - this.minX) / 2;
    }

    public final int getCenterY() {
        return this.minY + Math.max(1, this.maxY - this.minY) / 2;
    }

    public final int getMaxX() {
        return this.maxX;
    }

    public final int getMaxY() {
        return this.maxY;
    }

    public final int getMinX() {
        return this.minX;
    }

    public final int getMinY() {
        return this.minY;
    }

    public final VolaTile[] getStructureTiles() {
        VolaTile[] tiles = new VolaTile[this.structureTiles.size()];
        return this.structureTiles.toArray(tiles);
    }

    public boolean allowsAllies() {
        return this.allowsAllies;
    }

    public boolean allowsKingdom() {
        return this.allowsKingdom;
    }

    public boolean allowsCitizens() {
        return this.allowsVillagers;
    }

    @Override
    public boolean isManaged() {
        return this.permissions.hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
    }

    @Override
    public boolean isManageEnabled(Player player) {
        if (player.getPower() > 1) {
            return true;
        }
        if (this.isManaged()) {
            Village vil = this.getPermissionsVillage();
            if (vil != null) {
                return vil.isMayor(player);
            }
            return false;
        }
        return this.isOwner(player);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void setIsManaged(boolean newIsManaged, Player player) {
        int oldId = this.villageId;
        if (newIsManaged) {
            Village v = this.getVillage();
            if (v != null) {
                this.villageId = v.getId();
            } else {
                Village cv = player.getCitizenVillage();
                if (cv == null) return;
                this.villageId = cv.getId();
            }
        } else {
            this.villageId = -1;
        }
        if (oldId != this.villageId && StructureSettings.exists(this.getWurmId())) {
            StructureSettings.remove(this.getWurmId());
            PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
        }
        this.permissions.setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), newIsManaged);
    }

    @Override
    public String mayManageText(Player player) {
        String sName = this.getVillageName(player);
        if (sName.length() > 0) {
            return "Settlement \"" + sName + "\" may manage";
        }
        return sName;
    }

    @Override
    public String mayManageHover(Player aPlayer) {
        return "";
    }

    @Override
    public String messageOnTick() {
        return "This gives full control to the settlement";
    }

    @Override
    public String questionOnTick() {
        return "Did you realy mean to do that?";
    }

    @Override
    public String messageUnTick() {
        return "Doing this reverts the control back to the owner.";
    }

    @Override
    public String questionUnTick() {
        return "Are you really positive you want to do that?";
    }

    @Override
    public String getSettlementName() {
        String sName = "";
        Village vill = this.getPermissionsVillage();
        if (vill != null) {
            sName = vill.getName();
        }
        if (sName.length() > 0) {
            return "Citizens of \"" + sName + "\"";
        }
        return sName;
    }

    @Override
    public String getAllianceName() {
        String aName = "";
        Village vill = this.getPermissionsVillage();
        if (vill != null) {
            aName = vill.getAllianceName();
        }
        if (aName.length() > 0) {
            return "Alliance of \"" + aName + "\"";
        }
        return "";
    }

    @Override
    public String getKingdomName() {
        byte kingdom = 0;
        Village vill = this.getPermissionsVillage();
        kingdom = vill != null ? vill.kingdom : Players.getInstance().getKingdomForPlayer(this.getOwnerId());
        return "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
    }

    @Override
    public boolean canAllowEveryone() {
        return false;
    }

    @Override
    public String getRolePermissionName() {
        return "";
    }

    final boolean hasLoaded() {
        return this.hasLoaded;
    }

    final void setHasLoaded(boolean aHasLoaded) {
        this.hasLoaded = aHasLoaded;
    }

    final boolean isLoading() {
        return this.isLoading;
    }

    final void setLoading(boolean aIsLoading) {
        this.isLoading = aIsLoading;
    }

    final boolean isSurfaced() {
        return this.surfaced;
    }

    public final byte getLayer() {
        if (this.surfaced) {
            return 0;
        }
        return -1;
    }

    final void setSurfaced(boolean aSurfaced) {
        this.surfaced = aSurfaced;
    }

    final void setStructureType(byte theStructureType) {
        this.structureType = theStructureType;
    }

    public final byte getStructureType() {
        return this.structureType;
    }

    final long getWritid() {
        return this.writid;
    }

    public final void setWritid(long aWritid, boolean save) {
        this.writid = aWritid;
        if (save) {
            try {
                this.saveWritId();
            }
            catch (IOException iox) {
                logger.log(Level.INFO, "Problems saving WritId " + aWritid + ", StructureId: " + this.wurmId + iox.getMessage(), iox);
            }
        }
    }

    public final Set<StructureSupport> getAllSupports() {
        HashSet<StructureSupport> toReturn = new HashSet<StructureSupport>();
        if (this.structureTiles == null) {
            return toReturn;
        }
        for (VolaTile tile : this.structureTiles) {
            toReturn.addAll(tile.getAllSupport());
        }
        return toReturn;
    }

    private static final void addAllGroundStructureSupportToSet(Set<StructureSupport> supportingSupports, Set<StructureSupport> remainingSupports) {
        HashSet<StructureSupport> toMove = new HashSet<StructureSupport>();
        for (StructureSupport remaining : remainingSupports) {
            if (!remaining.isSupportedByGround()) continue;
            toMove.add(remaining);
        }
        supportingSupports.addAll(toMove);
        remainingSupports.removeAll(toMove);
    }

    public final boolean wouldCreateFlyingStructureIfRemoved(StructureSupport supportToCheck) {
        Set<StructureSupport> allSupports = this.getAllSupports();
        HashSet<StructureSupport> supportingSupports = new HashSet<StructureSupport>();
        allSupports.remove(supportToCheck);
        StructureSupport match = null;
        for (StructureSupport csupport : allSupports) {
            if (csupport.getId() != supportToCheck.getId()) continue;
            match = csupport;
        }
        if (match != null) {
            allSupports.remove(match);
        }
        Structure.addAllGroundStructureSupportToSet(supportingSupports, allSupports);
        HashSet<StructureSupport> toRemove = new HashSet<StructureSupport>();
        while (!allSupports.isEmpty()) {
            for (StructureSupport checked : supportingSupports) {
                for (StructureSupport remaining : allSupports) {
                    if (!checked.supports(remaining)) continue;
                    toRemove.add(remaining);
                }
            }
            if (toRemove.isEmpty()) break;
            supportingSupports.addAll(toRemove);
            allSupports.removeAll(toRemove);
            toRemove.clear();
        }
        return !allSupports.isEmpty();
    }

    public final int[] getNortEntrance() {
        return new int[]{this.minX, this.minY - 1};
    }

    public final int[] getSouthEntrance() {
        return new int[]{this.maxX, this.maxY + 1};
    }

    public final int[] getWestEntrance() {
        return new int[]{this.minX - 1, this.minY};
    }

    public final int[] getEastEntrance() {
        return new int[]{this.maxX + 1, this.maxY};
    }

    public final boolean isHorizontal() {
        return this.minX < this.maxX && this.minY == this.maxY;
    }

    public final boolean containsSettlement(int[] tileCoords, int layer) {
        if (tileCoords[0] == -1) {
            return false;
        }
        VolaTile t = Zones.getTileOrNull(tileCoords[0], tileCoords[1], layer == 0);
        if (t != null) {
            return t.getVillage() != null;
        }
        return false;
    }

    public final int[] findBestBridgeEntrance(Creature creature, int tilex, int tiley, int layer, long bridgeId, int currentPathFindCounter) {
        int diffMin;
        boolean switchEntrance;
        int lMaxX = this.isHorizontal() ? this.getMaxX() + 1 : this.getMaxX();
        int lMinX = this.isHorizontal() ? this.getMinX() - 1 : this.getMinX();
        int lMinY = this.isHorizontal() ? this.getMinY() : this.getMinY() - 1;
        int lMaxY = this.isHorizontal() ? this.getMaxY() : this.getMaxY() + 1;
        int[] min = new int[]{lMinX, lMinY};
        int[] max = new int[]{lMaxX, lMaxY};
        if (!creature.isUnique() && this.containsSettlement(min, layer) && this.containsSettlement(max, layer)) {
            return noEntrance;
        }
        boolean bl = switchEntrance = currentPathFindCounter > 5 && Server.rand.nextBoolean();
        if ((this.isHorizontal() ? creature.getTileX() >= lMaxX : creature.getTileY() >= lMaxY) && (!this.containsSettlement(max, layer) || creature.isUnique()) && !switchEntrance) {
            return max;
        }
        if ((this.isHorizontal() ? creature.getTileX() <= lMinX : creature.getTileY() <= lMinY) && (!this.containsSettlement(min, layer) || creature.isUnique()) && !switchEntrance) {
            return min;
        }
        int diffMax = Math.abs(this.isHorizontal() ? creature.getTileX() - lMaxX : creature.getTileY() - lMaxY);
        int n = diffMin = this.isHorizontal() ? creature.getTileX() - lMinX : creature.getTileY() - lMinY;
        if (diffMax <= diffMin) {
            if (!(this.containsSettlement(max, layer) && !creature.isUnique() || switchEntrance)) {
                return max;
            }
            return min;
        }
        if (diffMin <= diffMax) {
            if (!(this.containsSettlement(min, layer) && !creature.isUnique() || switchEntrance)) {
                return min;
            }
            return max;
        }
        if (Server.rand.nextBoolean()) {
            if (!(this.containsSettlement(max, layer) && !creature.isUnique() || switchEntrance)) {
                return max;
            }
            return min;
        }
        if (!(this.containsSettlement(min, layer) && !creature.isUnique() || switchEntrance)) {
            return min;
        }
        return max;
    }

    public boolean isBridgeJustPlans() {
        if (this.structureType != 1) {
            return false;
        }
        for (BridgePart bp : this.getBridgeParts()) {
            if (bp.getBridgePartState() == BridgeConstants.BridgeState.PLANNED) continue;
            return false;
        }
        return true;
    }

    public boolean isBridgeGone() {
        if (this.isBridgeJustPlans()) {
            for (BridgePart bp : this.getBridgeParts()) {
                bp.destroy();
            }
            this.totallyDestroy();
            return true;
        }
        return false;
    }

    void addDefaultAllyPermissions() {
        if (!this.getPermissionsPlayerList().exists(-20L)) {
            int value = StructureSettings.StructurePermissions.PASS.getValue() + StructureSettings.StructurePermissions.PICKUP.getValue();
            this.addNewGuest(-20L, value);
        }
    }

    @Override
    public void addDefaultCitizenPermissions() {
        if (!this.getPermissionsPlayerList().exists(-30L)) {
            int value = StructureSettings.StructurePermissions.PASS.getValue() + StructureSettings.StructurePermissions.PICKUP.getValue();
            this.addNewGuest(-30L, value);
        }
    }

    void addDefaultKingdomPermissions() {
        if (!this.getPermissionsPlayerList().exists(-40L)) {
            int value = StructureSettings.StructurePermissions.PASS.getValue();
            this.addNewGuest(-40L, value);
        }
    }

    public final void setWalkedOnBridge(long now) {
        long lastUsed = 0L;
        for (BridgePart bp : this.getBridgeParts()) {
            if (!bp.isFinished() || lastUsed >= bp.getLastUsed()) continue;
            lastUsed = bp.getLastUsed();
        }
        if (lastUsed < now - 86400000L) {
            for (BridgePart bp : this.getBridgeParts()) {
                bp.setLastUsed(now);
            }
        }
    }

    @Override
    public boolean setNewOwner(long playerId) {
        if (this.writid != -10L) {
            return false;
        }
        if (!this.isManaged() && StructureSettings.exists(this.getWurmId())) {
            StructureSettings.remove(this.getWurmId());
            PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
        }
        this.ownerId = playerId;
        try {
            this.saveOwnerId();
            return true;
        }
        catch (IOException iOException) {
            return false;
        }
    }

    @Override
    public String getOwnerName() {
        this.getOwnerId();
        if (this.writid != -10L) {
            return "has writ";
        }
        return PlayerInfoFactory.getPlayerName(this.ownerId);
    }

    public boolean convertToNewPermissions() {
        boolean didConvert = false;
        PermissionsPlayerList ppl = StructureSettings.getPermissionsPlayerList(this.wurmId);
        if (this.allowsAllies && !ppl.exists(-20L)) {
            this.addDefaultAllyPermissions();
            didConvert = true;
        }
        if (this.allowsVillagers && !ppl.exists(-30L)) {
            this.addDefaultCitizenPermissions();
            didConvert = true;
        }
        if (this.allowsKingdom && !ppl.exists(-40L)) {
            this.addDefaultKingdomPermissions();
            didConvert = true;
        }
        if (didConvert) {
            try {
                this.saveSettings();
            }
            catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        for (Door d : this.getAllDoors()) {
            d.setIsManaged(true, null);
        }
        return didConvert;
    }

    public byte getKingdomId() {
        byte kingdom = 0;
        Village vill = this.getPermissionsVillage();
        kingdom = vill != null ? vill.kingdom : Players.getInstance().getKingdomForPlayer(this.getOwnerId());
        return kingdom;
    }

    public static boolean isGroundFloorAtPosition(float x, float y, boolean isOnSurface) {
        Floor[] floors;
        TilePos tilePos = CoordUtils.WorldToTile(x, y);
        VolaTile tile = Zones.getOrCreateTile(tilePos, isOnSurface);
        return tile != null && (floors = tile.getFloors(0, 0)) != null && floors.length > 0 && floors[0].getType() == StructureConstants.FloorType.FLOOR;
    }

    public boolean isDestroyed() {
        if (this.isTypeBridge()) {
            return this.isBridgeJustPlans() || this.isBridgeGone();
        }
        Wall[] walls = this.getWalls();
        Floor[] floors = this.getFloors();
        boolean destroyed = true;
        for (Wall wall : walls) {
            if (wall.isWallPlan()) continue;
            destroyed = false;
        }
        for (TimeConstants timeConstants : floors) {
            if (((Floor)timeConstants).isAPlan()) continue;
            destroyed = false;
        }
        return destroyed;
    }

    abstract void setFinalFinished(boolean var1);

    public abstract void setFinished(boolean var1);

    public abstract void endLoading() throws IOException;

    abstract void load() throws IOException, NoSuchStructureException;

    @Override
    public abstract void save() throws IOException;

    public abstract void saveWritId() throws IOException;

    public abstract void saveOwnerId() throws IOException;

    public abstract void saveSettings() throws IOException;

    public abstract void saveName() throws IOException;

    abstract void delete();

    abstract void removeStructureGuest(long var1);

    abstract void addNewGuest(long var1, int var3);

    public abstract void setAllowAllies(boolean var1);

    public abstract void setAllowVillagers(boolean var1);

    public abstract void setAllowKingdom(boolean var1);

    public String toString() {
        return "Structure [wurmId=" + this.wurmId + ", surfaced=" + this.surfaced + ", name=" + this.name + ", writid=" + this.writid + "]";
    }

    public abstract void removeBuildTile(int var1, int var2, int var3);

    public abstract void addNewBuildTile(int var1, int var2, int var3);

    public abstract void deleteAllBuildTiles();
}


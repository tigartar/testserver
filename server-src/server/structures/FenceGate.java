/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.DoorSettings;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class FenceGate
extends Door
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(FenceGate.class.getName());
    final Fence fence;
    private Village village = null;
    int villageId = -1;
    int openTime = 0;
    int closeTime = 0;
    static final Map<Long, FenceGate> gates = new ConcurrentHashMap<Long, FenceGate>();

    FenceGate(Fence aFence) {
        this.fence = aFence;
        gates.put(new Long(aFence.getId()), this);
        try {
            this.load();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to load/save " + this.name + "," + aFence.getId(), iox);
        }
    }

    @Override
    public final float getQualityLevel() {
        return this.fence.getCurrentQualityLevel();
    }

    public final void setVillage(@Nullable Village vill) {
        this.village = vill;
        if (vill != null) {
            this.setIsManaged(true, null);
        } else if (this.villageId != -1) {
            this.setIsManaged(false, null);
        }
    }

    public final Village getVillage() {
        return this.village;
    }

    private Village getPermissionsVillage() {
        Village vill = this.getVillage();
        if (vill != null) {
            return vill;
        }
        long wid = this.getOwnerId();
        if (wid != -10L) {
            return Villages.getVillageForCreature(wid);
        }
        return null;
    }

    public final int getVillageId() {
        return this.villageId;
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

    public final Fence getFence() {
        return this.fence;
    }

    public final int getOpenTime() {
        return this.openTime;
    }

    public final int getCloseTime() {
        return this.closeTime;
    }

    @Override
    public final void addToTiles() {
        this.innerTile = this.fence.getTile();
        int tilex = this.innerTile.getTileX();
        int tiley = this.innerTile.getTileY();
        this.innerTile.addDoor(this);
        if (this.fence.isHorizontal()) {
            this.outerTile = Zones.getOrCreateTile(tilex, tiley - 1, this.fence.isOnSurface());
            this.outerTile.addDoor(this);
        } else {
            this.outerTile = Zones.getOrCreateTile(tilex - 1, tiley, this.fence.isOnSurface());
            this.outerTile.addDoor(this);
        }
        this.calculateArea();
    }

    @Override
    public final boolean canBeOpenedBy(Creature creature, boolean passedThroughDoor) {
        if (creature == null) {
            return false;
        }
        if (MissionTriggers.isDoorOpen(creature, this.getWurmId(), 1)) {
            return true;
        }
        if (creature.getPower() > 1) {
            return true;
        }
        if (creature.isKingdomGuard() || creature.isGhost()) {
            return true;
        }
        if (creature.getLeader() != null && this.canBeOpenedBy(creature.getLeader(), false)) {
            return true;
        }
        if (!creature.canOpenDoors()) {
            return false;
        }
        if (this.village != null && this.village.isEnemy(creature)) {
            return this.canBeUnlockedBy(creature);
        }
        if (creature.isPlayer() && this.mayPass(creature)) {
            return true;
        }
        return this.canBeUnlockedBy(creature);
    }

    @Override
    public final boolean canBeUnlockedBy(Creature creature) {
        if (creature.getPower() > 1) {
            return true;
        }
        if (this.lockCounter > 0) {
            creature.sendToLoggers("Lock counter=" + this.lockCounter);
            return true;
        }
        if (this.lock == -10L) {
            creature.sendToLoggers("No lock ");
            return true;
        }
        Item doorlock = null;
        try {
            doorlock = Items.getItem(this.lock);
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.INFO, "Lock has decayed? Id was " + this.lock);
            creature.sendToLoggers("Lock id " + this.lock + " has decayed?");
            return true;
        }
        if (doorlock.isLocked()) {
            Item[] items = creature.getKeys();
            for (int x = 0; x < items.length; ++x) {
                if (!doorlock.isUnlockedBy(items[x].getWurmId())) continue;
                creature.sendToLoggers("I have key");
                return true;
            }
            if (this.mayLock(creature)) {
                creature.sendToLoggers("I have Lock Permission");
                return true;
            }
        } else {
            creature.sendToLoggers("It's not locked");
            return true;
        }
        return false;
    }

    @Override
    public final void creatureMoved(Creature creature, int diffTileX, int diffTileY) {
        if (this.covers(creature.getStatus().getPositionX(), creature.getStatus().getPositionY(), creature.getPositionZ(), creature.getFloorLevel(), creature.followsGround())) {
            this.addCreature(creature);
        } else {
            this.removeCreature(creature);
        }
    }

    @Override
    public final void removeCreature(Creature creature) {
        if (this.creatures != null) {
            if (this.creatures.contains(creature)) {
                this.creatures.remove(creature);
                if (this.isOpen() && !creature.isGhost()) {
                    creature.getCommunicator().sendCloseFence(this.fence, false, true);
                    boolean close = true;
                    for (Creature checked : this.creatures) {
                        if (!this.canBeOpenedBy(checked, false)) continue;
                        close = false;
                    }
                    if (close) {
                        this.close();
                        if (this.watchers != null && creature.isVisible()) {
                            for (VirtualZone z : this.watchers) {
                                if (z.getWatcher() == creature) continue;
                                z.closeFence(this.fence, false, false);
                            }
                        }
                    }
                }
            }
            if (this.creatures.size() == 0) {
                this.creatures = null;
            }
        }
    }

    public final boolean containsCreature(Creature creature) {
        if (this.creatures == null) {
            return false;
        }
        return this.creatures.contains(creature);
    }

    @Override
    public void updateDoor(Creature creature, Item key, boolean removedKey) {
        boolean isOpenToCreature = this.canBeOpenedBy(creature, false);
        if (removedKey) {
            if (this.creatures != null) {
                if (this.creatures.contains(creature) && !isOpenToCreature && this.canBeUnlockedByKey(key)) {
                    creature.getCommunicator().sendCloseFence(this.fence, false, true);
                    if (this.isOpen()) {
                        boolean close = true;
                        for (Creature checked : this.creatures) {
                            if (!this.canBeOpenedBy(checked, false)) continue;
                            close = false;
                        }
                        if (close && creature.isVisible() && !creature.isGhost()) {
                            this.close();
                            if (this.watchers != null && creature.isVisible()) {
                                for (VirtualZone z : this.watchers) {
                                    if (z.getWatcher() == creature) continue;
                                    z.closeFence(this.fence, false, false);
                                }
                            }
                        }
                    }
                }
                if (this.creatures.size() == 0) {
                    this.creatures = null;
                }
            }
        } else if (this.creatures != null && this.creatures.contains(creature) && isOpenToCreature && this.canBeUnlockedByKey(key) && !this.isOpen() && creature.isVisible() && !creature.isGhost()) {
            if (this.watchers != null && creature.isVisible()) {
                for (VirtualZone z : this.watchers) {
                    if (z.getWatcher() == creature) continue;
                    z.openFence(this.fence, false, false);
                }
            }
            this.open();
            creature.getCommunicator().sendOpenFence(this.fence, true, true);
        }
    }

    @Override
    public final boolean addCreature(Creature creature) {
        if (this.creatures == null) {
            this.creatures = new HashSet();
        }
        if (!this.creatures.contains(creature)) {
            this.creatures.add(creature);
            if (this.canBeOpenedBy(creature, false) && !creature.isGhost()) {
                if (!this.isOpen()) {
                    if (this.watchers != null && creature.isVisible()) {
                        for (VirtualZone z : this.watchers) {
                            if (z.getWatcher() == creature) continue;
                            z.openFence(this.fence, false, false);
                        }
                    }
                    this.open();
                    if (creature.getEnemyPresense() > 0 && this.getVillage() == null) {
                        this.setLockCounter((short)120);
                    }
                    creature.getCommunicator().sendOpenFence(this.fence, true, true);
                } else {
                    creature.getCommunicator().sendOpenFence(this.fence, true, true);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean keyFits(long keyId) throws NoSuchLockException {
        if (this.lock == -10L) {
            throw new NoSuchLockException("No ID");
        }
        try {
            Item doorlock = Items.getItem(this.lock);
            return doorlock.isUnlockedBy(keyId);
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.INFO, "Lock has decayed? Id was " + this.lock);
            return false;
        }
    }

    public final boolean isOpenTime() {
        return false;
    }

    @Override
    final void close() {
        this.open = false;
    }

    @Override
    final void open() {
        this.open = true;
    }

    public final void removeFromVillage() {
        if (this.village != null) {
            this.village.removeGate(this);
        }
    }

    public static final FenceGate getFenceGate(long id) {
        Long lid = new Long(id);
        FenceGate toReturn = gates.get(lid);
        return toReturn;
    }

    public static final FenceGate[] getAllGates() {
        return gates.values().toArray(new FenceGate[gates.size()]);
    }

    public static final FenceGate[] getManagedGatesFor(Player player, int villageId, boolean includeAll) {
        HashSet<FenceGate> fenceGates = new HashSet<FenceGate>();
        for (FenceGate gate : gates.values()) {
            if (!gate.canManage(player) && (villageId < 0 || gate.getVillageId() != villageId) || !includeAll && !gate.hasLock()) continue;
            fenceGates.add(gate);
        }
        return fenceGates.toArray(new FenceGate[fenceGates.size()]);
    }

    public static final FenceGate[] getOwnedGatesFor(Player player) {
        HashSet<FenceGate> fenceGates = new HashSet<FenceGate>();
        for (FenceGate gate : gates.values()) {
            if (!gate.isOwner(player) && !gate.isActualOwner(player.getWurmId())) continue;
            fenceGates.add(gate);
        }
        return fenceGates.toArray(new FenceGate[fenceGates.size()]);
    }

    public static final void unManageGatesFor(int villageId) {
        for (FenceGate gate : FenceGate.getAllGates()) {
            if (gate.getVillageId() != villageId) continue;
            gate.setIsManaged(false, null);
        }
    }

    public final Village getOwnerVillage() {
        return Villages.getVillageForCreature(this.getOwnerId());
    }

    @Override
    public final boolean covers(float x, float y, float posz, int floorLevel, boolean followGround) {
        return (this.fence != null && this.fence.isWithinZ(posz + 1.0f, posz, followGround) || this.isTransition() && floorLevel <= 0) && x >= (float)this.startx && x <= (float)this.endx && y >= (float)this.starty && y <= (float)this.endy;
    }

    @Override
    public final int getFloorLevel() {
        return this.fence.getFloorLevel();
    }

    public abstract void setOpenTime(int var1);

    public abstract void setCloseTime(int var1);

    @Override
    public abstract void setLock(long var1);

    @Override
    public abstract void save() throws IOException;

    @Override
    abstract void load() throws IOException;

    @Override
    public abstract void delete();

    public final long getOwnerId() {
        if (this.lock != -10L) {
            try {
                Item doorlock = Items.getItem(this.lock);
                return doorlock.getLastOwnerId();
            }
            catch (NoSuchItemException nsi) {
                return -10L;
            }
        }
        return -10L;
    }

    @Override
    public long getWurmId() {
        return this.fence.getId();
    }

    @Override
    public boolean setObjectName(String aNewName, Creature aCreature) {
        this.setName(aNewName);
        this.outerTile.updateFence(this.getFence());
        return true;
    }

    @Override
    public boolean isActualOwner(long playerId) {
        long wid = this.getOwnerId();
        if (this.lock != -10L) {
            return wid == playerId;
        }
        return false;
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
    public boolean canChangeOwner(Creature creature) {
        return this.hasLock() && (creature.getPower() > 1 || this.isActualOwner(creature.getWurmId()));
    }

    @Override
    public String getWarning() {
        if (this.lock == -10L) {
            return "NEEDS TO HAVE A LOCK FOR PERMISSIONS TO WORK";
        }
        if (!this.isLocked()) {
            return "NEEDS TO BE LOCKED OTHERWISE EVERYONE CAN PASS";
        }
        return "";
    }

    @Override
    public PermissionsPlayerList getPermissionsPlayerList() {
        return DoorSettings.getPermissionsPlayerList(this.getWurmId());
    }

    @Override
    public final boolean canHavePermissions() {
        return this.isLocked();
    }

    @Override
    public final boolean mayShowPermissions(Creature creature) {
        return this.hasLock() && this.mayManage(creature);
    }

    @Override
    public boolean canManage(Creature creature) {
        if (DoorSettings.isExcluded(this, creature)) {
            return false;
        }
        if (DoorSettings.canManage(this, creature)) {
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
        return vill.isActionAllowed((short)667, creature);
    }

    @Override
    public boolean mayManage(Creature creature) {
        if (creature.getPower() > 1) {
            return true;
        }
        return this.canManage(creature);
    }

    @Override
    public boolean isManaged() {
        if (this.fence == null) {
            return false;
        }
        return this.fence.getSettings().hasPermission(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit());
    }

    @Override
    public boolean isManageEnabled(Player player) {
        Village vil;
        if (player.getPower() > 1) {
            return true;
        }
        if (this.isManaged() && (vil = this.getVillage()) != null) {
            return false;
        }
        return this.isOwner(player);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void setIsManaged(boolean newIsManaged, @Nullable Player player) {
        if (this.fence == null) return;
        int oldId = this.villageId;
        if (newIsManaged) {
            Village v = this.getVillage();
            if (v != null) {
                this.setVillageId(v.getId());
            } else {
                Village cv = this.getOwnerVillage();
                if (cv == null) return;
                this.setVillageId(cv.getId());
            }
        } else {
            this.setVillageId(-1);
        }
        if (oldId != this.villageId && DoorSettings.exists(this.getWurmId())) {
            DoorSettings.remove(this.getWurmId());
            PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
        }
        this.fence.getSettings().setPermissionBit(Permissions.Allow.SETTLEMENT_MAY_MANAGE.getBit(), newIsManaged);
        this.fence.savePermissions();
        try {
            this.save();
            return;
        }
        catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public String mayManageText(Player aPlayer) {
        String vName = "";
        Village vill = this.getManagedByVillage();
        if (vill != null) {
            vName = vill.getName();
        } else {
            vill = this.getVillage();
            if (vill != null) {
                vName = vill.getName();
            } else {
                vill = Villages.getVillageForCreature(this.getOwnerId());
                if (vill != null) {
                    vName = vill.getName();
                }
            }
        }
        if (vName.length() > 0) {
            return "Settlement \"" + vName + "\" may manage";
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
        kingdom = vill != null ? vill.kingdom : Players.getInstance().getKingdomForPlayer(this.getOwnerId());
        return "Kingdom of \"" + Kingdoms.getNameFor(kingdom) + "\"";
    }

    @Override
    public void addDefaultCitizenPermissions() {
        if (!this.getPermissionsPlayerList().exists(-30L)) {
            int value = DoorSettings.DoorPermissions.PASS.getValue();
            this.addGuest(-30L, value);
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
        return Players.getInstance().getKingdomForPlayer(this.getOwnerId()) == creature.getKingdomId();
    }

    @Override
    public boolean isGuest(Creature creature) {
        return this.isGuest(creature.getWurmId());
    }

    @Override
    public boolean isGuest(long playerId) {
        return DoorSettings.isGuest((PermissionsPlayerList.ISettings)this, playerId);
    }

    @Override
    public void addGuest(long guestId, int aSettings) {
        DoorSettings.addPlayer(this.getWurmId(), guestId, aSettings);
    }

    @Override
    public void removeGuest(long guestId) {
        DoorSettings.removePlayer(this.getWurmId(), guestId);
    }

    @Override
    public final boolean mayPass(Creature creature) {
        if (!this.isLocked()) {
            return true;
        }
        if (DoorSettings.exists(this.getWurmId())) {
            if (DoorSettings.isExcluded(this, creature)) {
                return false;
            }
            if (DoorSettings.mayPass(this, creature)) {
                return true;
            }
        }
        if (this.isManaged()) {
            Village vill = this.getManagedByVillage();
            VillageRole vr = vill == null ? null : vill.getRoleFor(creature);
            return vr != null && vr.mayPassGates();
        }
        return this.isOwner(creature);
    }

    public final boolean mayAttachLock(Creature creature) {
        if (this.hasLock()) {
            if (this.village != null) {
                VillageRole vr = this.village.getRoleFor(creature);
                return vr != null && vr.mayAttachLock();
            }
            return this.isOwner(creature);
        }
        return true;
    }

    @Override
    public final boolean mayLock(Creature creature) {
        if (DoorSettings.exists(this.getWurmId())) {
            if (DoorSettings.isExcluded(this, creature)) {
                return false;
            }
            if (DoorSettings.mayLock(this, creature)) {
                return true;
            }
        }
        if (this.isManaged()) {
            Village vill = this.getManagedByVillage();
            VillageRole vr = vill == null ? null : vill.getRoleFor(creature);
            return vr != null && vr.mayAttachLock();
        }
        return this.isOwner(creature);
    }

    @Override
    public String getTypeName() {
        if (this.fence == null) {
            return "No Fence!";
        }
        return this.fence.getTypeName();
    }

    @Override
    public boolean isNotLockpickable() {
        return this.fence.isNotLockpickable();
    }

    @Override
    public boolean setNewOwner(long playerId) {
        try {
            if (!this.isManaged() && DoorSettings.exists(this.getWurmId())) {
                DoorSettings.remove(this.getWurmId());
                PermissionsHistories.addHistoryEntry(this.getWurmId(), System.currentTimeMillis(), -10L, "Auto", "Cleared Permissions");
            }
            Item theLock = this.getLock();
            logger.info("Overwritting owner (" + theLock.getLastOwnerId() + ") of lock " + theLock.getWurmId() + " to " + playerId);
            theLock.setLastOwnerId(playerId);
            return true;
        }
        catch (NoSuchLockException noSuchLockException) {
            return false;
        }
    }

    @Override
    public String getOwnerName() {
        try {
            Item theLock = this.getLock();
            return PlayerInfoFactory.getPlayerName(theLock.getLastOwnerId());
        }
        catch (NoSuchLockException noSuchLockException) {
            return "";
        }
    }

    public final boolean maySeeHistory(Creature creature) {
        if (creature.getPower() > 1) {
            return true;
        }
        return this.isOwner(creature);
    }

    public boolean convertToNewPermissions() {
        boolean didConvert = false;
        if (this.village != null) {
            this.setIsManaged(true, null);
            didConvert = true;
        }
        if (didConvert) {
            this.fence.savePermissions();
        }
        return didConvert;
    }

    public boolean fixForNewPermissions() {
        boolean didConvert = false;
        if (this.village != null) {
            this.addDefaultCitizenPermissions();
            didConvert = true;
        }
        return didConvert;
    }

    public abstract void setVillageId(int var1);
}


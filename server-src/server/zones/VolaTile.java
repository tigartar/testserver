/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.MovementListener;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.epic.EpicTargetItems;
import com.wurmonline.server.highways.HighwayFinder;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbWall;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSupport;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.TempFence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Stairs;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTileItems;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import com.wurmonline.shared.util.MaterialUtilities;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VolaTile
implements MovementListener,
ItemTypes,
MiscConstants,
CounterTypes,
ItemMaterials {
    private static final Logger logger = Logger.getLogger(VolaTile.class.getName());
    private VolaTileItems vitems = null;
    private Structure structure;
    private Set<Effect> effects;
    private Set<Creature> creatures;
    private Set<Wall> walls;
    private Set<Floor> floors;
    private Set<BridgePart> bridgeParts;
    private Set<MineDoorPermission> mineDoors;
    public final int tilex;
    public final int tiley;
    private final boolean surfaced;
    private Set<VirtualZone> watchers;
    private final Zone zone;
    private Set<Door> doors;
    private Set<Door> unlockedDoors;
    private boolean inactive = false;
    private boolean isLava = false;
    private static final Set<StructureSupport> emptySupports = new HashSet<StructureSupport>();
    private Map<Long, Fence> fences;
    private Map<Long, Fence> magicFences;
    private Village village;
    public boolean isTransition;
    private static final Creature[] emptyCreatures = new Creature[0];
    private static final Item[] emptyItems = new Item[0];
    private static final Wall[] emptyWalls = new Wall[0];
    private static final Fence[] emptyFences = new Fence[0];
    private static final Floor[] emptyFloors = new Floor[0];
    private static final BridgePart[] emptyBridgeParts = new BridgePart[0];
    private static final VirtualZone[] emptyWatchers = new VirtualZone[0];
    private static final Effect[] emptyEffects = new Effect[0];
    private static final Door[] emptyDoors = new Door[0];
    static final Set<Wall> toRemove = new HashSet<Wall>();

    VolaTile(int x, int y, boolean isSurfaced, Set<VirtualZone> aWatchers, Zone zon) {
        this.tilex = x;
        this.tiley = y;
        this.surfaced = isSurfaced;
        this.zone = zon;
        this.watchers = aWatchers;
        this.checkTransition();
        this.checkIsLava();
    }

    private final void checkTransition() {
        this.isTransition = Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id;
    }

    public boolean isOnSurface() {
        return this.surfaced;
    }

    private boolean isLava() {
        return this.isLava;
    }

    private void checkIsLava() {
        this.isLava = this.isOnSurface() && Tiles.decodeType(Server.surfaceMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_LAVA.id || !this.isOnSurface() && Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)) == Tiles.Tile.TILE_CAVE_WALL_LAVA.id;
    }

    public int getNumberOfItems(int floorLevel) {
        if (this.vitems == null) {
            return 0;
        }
        return this.vitems.getNumberOfItems(floorLevel);
    }

    public final int getNumberOfDecorations(int floorLevel) {
        if (this.vitems == null) {
            return 0;
        }
        return this.vitems.getNumberOfDecorations(floorLevel);
    }

    public void addFence(Fence fence) {
        Fence f;
        if (this.fences == null) {
            this.fences = new ConcurrentHashMap<Long, Fence>();
        }
        if (fence.isMagic()) {
            if (this.magicFences == null) {
                this.magicFences = new ConcurrentHashMap<Long, Fence>();
            }
            this.magicFences.put(fence.getId(), fence);
        }
        if (fence.isTemporary() && (f = this.fences.get(fence.getId())) != null && !f.isTemporary()) {
            return;
        }
        this.fences.put(fence.getId(), fence);
        if (fence.getZoneId() != this.zone.getId()) {
            fence.setZoneId(this.zone.getId());
        }
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.addFence(fence);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    void setVillage(Village aVillage) {
        MineDoorPermission md = MineDoorPermission.getPermission(this.tilex, this.tiley);
        if (this.village == null || !this.village.equals(aVillage)) {
            if (this.doors != null) {
                for (Door door : this.doors) {
                    if (!(door instanceof FenceGate)) continue;
                    if (aVillage != null) {
                        aVillage.addGate((FenceGate)door);
                        continue;
                    }
                    if (this.village == null) continue;
                    this.village.removeGate((FenceGate)door);
                }
            }
            if (md != null) {
                if (aVillage != null) {
                    aVillage.addMineDoor(md);
                } else if (this.village != null) {
                    this.village.removeMineDoor(md);
                }
            }
            if (this.creatures != null) {
                for (Creature c : this.creatures) {
                    Shop s;
                    Wagoner wagoner;
                    c.setCurrentVillage(aVillage);
                    if (c.isWagoner() && aVillage == null && (wagoner = c.getWagoner()) != null) {
                        wagoner.clrVillage();
                    }
                    if (!c.isNpcTrader() || c.getCitizenVillage() != null || (s = Economy.getEconomy().getShop(c)).getOwnerId() != -10L) continue;
                    if (aVillage != null) {
                        try {
                            logger.log(Level.INFO, "Adding " + c.getName() + " as citizen to " + aVillage.getName());
                            aVillage.addCitizen(c, aVillage.getRoleForStatus((byte)3));
                        }
                        catch (IOException iox) {
                            logger.log(Level.INFO, iox.getMessage());
                        }
                        catch (NoSuchRoleException nsx) {
                            logger.log(Level.INFO, nsx.getMessage());
                        }
                        continue;
                    }
                    c.setCitizenVillage(null);
                }
            }
            if (this.vitems != null) {
                for (Item i : this.vitems.getAllItemsAsSet()) {
                    Node node;
                    if (i.getTemplateId() == 757) {
                        if (aVillage != null) {
                            aVillage.addBarrel(i);
                            continue;
                        }
                        if (this.village == null) continue;
                        this.village.removeBarrel(i);
                        continue;
                    }
                    if (i.getTemplateId() != 1112) continue;
                    if (aVillage != null) {
                        node = Routes.getNode(i.getWurmId());
                        if (node == null) continue;
                        node.setVillage(aVillage);
                        continue;
                    }
                    if (this.village == null || (node = Routes.getNode(i.getWurmId())) == null) continue;
                    node.setVillage(null);
                }
            }
            this.village = aVillage;
        } else {
            if (this.doors != null) {
                for (Door door : this.doors) {
                    if (!(door instanceof FenceGate)) continue;
                    aVillage.addGate((FenceGate)door);
                }
            }
            if (md != null) {
                aVillage.addMineDoor(md);
            }
        }
    }

    public Village getVillage() {
        return this.village;
    }

    public void removeFence(Fence fence) {
        Fence f;
        if (this.fences != null && (f = this.fences.remove(fence.getId())) != null) {
            if (f.isMagic() && this.magicFences != null) {
                this.magicFences.remove(fence.getId());
                if (this.magicFences.isEmpty()) {
                    this.magicFences = null;
                }
            }
            if (fence.isTemporary() && !f.isTemporary()) {
                this.fences.put(f.getId(), f);
            } else {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.removeFence(f);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
                if (this.fences.isEmpty()) {
                    this.fences = null;
                }
            }
        }
    }

    public void addSound(Sound sound) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.playSound(sound);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateFence(Fence fence) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.removeFence(fence);
                vz.addFence(fence);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateMagicalFence(Fence fence) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.addFence(fence);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public Fence[] getFences() {
        if (this.fences != null) {
            return this.fences.values().toArray(new Fence[this.fences.size()]);
        }
        return emptyFences;
    }

    public Collection<Fence> getFencesList() {
        if (this.fences != null) {
            return this.fences.values();
        }
        return null;
    }

    public Fence[] getAllFences() {
        VolaTile southTile;
        VolaTile eastTile;
        HashSet<Fence> fenceSet = new HashSet<Fence>();
        if (this.fences != null) {
            for (Fence f : this.fences.values()) {
                fenceSet.add(f);
            }
        }
        if ((eastTile = this.zone.getTileOrNull(this.tilex + 1, this.tiley)) != null) {
            Fence[] eastFences = eastTile.getFencesForDir(Tiles.TileBorderDirection.DIR_DOWN);
            for (int x = 0; x < eastFences.length; ++x) {
                fenceSet.add(eastFences[x]);
            }
        }
        if ((southTile = this.zone.getTileOrNull(this.tilex, this.tiley + 1)) != null) {
            Fence[] southFences = southTile.getFencesForDir(Tiles.TileBorderDirection.DIR_HORIZ);
            for (int x = 0; x < southFences.length; ++x) {
                fenceSet.add(southFences[x]);
            }
        }
        if (fenceSet.size() == 0) {
            return emptyFences;
        }
        return fenceSet.toArray(new Fence[fenceSet.size()]);
    }

    public boolean hasFenceOnCorner(int floorLevel) {
        Fence[] northFences;
        Fence[] westFences;
        if (this.fences != null && this.getFencesForLevel(floorLevel).length > 0) {
            return true;
        }
        VolaTile westTile = this.zone.getTileOrNull(this.tilex - 1, this.tiley);
        if (westTile != null && (westFences = westTile.getFencesForDirAndLevel(Tiles.TileBorderDirection.DIR_HORIZ, floorLevel)).length > 0) {
            return true;
        }
        VolaTile northTile = this.zone.getTileOrNull(this.tilex, this.tiley - 1);
        return northTile != null && (northFences = northTile.getFencesForDirAndLevel(Tiles.TileBorderDirection.DIR_DOWN, floorLevel)).length > 0;
    }

    public Fence[] getFencesForDirAndLevel(Tiles.TileBorderDirection dir, int floorLevel) {
        if (this.fences != null) {
            HashSet<Fence> fenceSet = new HashSet<Fence>();
            for (Fence f : this.fences.values()) {
                if (f.getDir() != dir || f.getFloorLevel() != floorLevel) continue;
                fenceSet.add(f);
            }
            return fenceSet.toArray(new Fence[fenceSet.size()]);
        }
        return emptyFences;
    }

    public Fence[] getFencesForDir(Tiles.TileBorderDirection dir) {
        if (this.fences != null) {
            HashSet<Fence> fenceSet = new HashSet<Fence>();
            for (Fence f : this.fences.values()) {
                if (f.getDir() != dir) continue;
                fenceSet.add(f);
            }
            return fenceSet.toArray(new Fence[fenceSet.size()]);
        }
        return emptyFences;
    }

    public Fence[] getFencesForLevel(int floorLevel) {
        if (this.fences != null) {
            HashSet<Fence> fenceSet = new HashSet<Fence>();
            for (Fence f : this.fences.values()) {
                if (f.getFloorLevel() != floorLevel) continue;
                fenceSet.add(f);
            }
            return fenceSet.toArray(new Fence[fenceSet.size()]);
        }
        return emptyFences;
    }

    public Fence getFence(long id) {
        if (this.fences != null) {
            return this.fences.get(id);
        }
        return null;
    }

    public void addDoor(Door door) {
        if (this.doors == null) {
            this.doors = new HashSet<Door>();
        }
        if (!this.doors.contains(door)) {
            this.doors.add(door);
            if (this.watchers != null) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        door.addWatcher(vz);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void removeDoor(Door door) {
        if (this.doors != null && this.doors.contains(door)) {
            if (this.watchers != null) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        door.removeWatcher(vz);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            this.doors.remove(door);
            if (this.unlockedDoors != null) {
                this.unlockedDoors.remove(door);
            }
            if (this.doors.isEmpty()) {
                this.doors = null;
            }
        }
    }

    public void addUnlockedDoor(Door door) {
        if (this.unlockedDoors == null) {
            this.unlockedDoors = new HashSet<Door>();
        }
        if (!this.unlockedDoors.contains(door)) {
            this.unlockedDoors.add(door);
        }
    }

    public void removeUnlockedDoor(Door door) {
        if (this.unlockedDoors == null) {
            return;
        }
        this.unlockedDoors.remove(door);
        if (this.unlockedDoors.isEmpty()) {
            this.unlockedDoors = null;
        }
    }

    public void addMineDoor(MineDoorPermission door) {
        if (this.mineDoors == null) {
            this.mineDoors = new HashSet<MineDoorPermission>();
        }
        if (this.mineDoors != null && !this.mineDoors.contains(door)) {
            this.mineDoors.add(door);
            if (this.watchers != null) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        door.addWatcher(vz);
                        vz.addMineDoor(door);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public void removeMineDoor(MineDoorPermission door) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                door.removeWatcher(vz);
                vz.removeMineDoor(door);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        if (this.mineDoors == null) {
            return;
        }
        this.mineDoors.remove(door);
        if (this.mineDoors.isEmpty()) {
            this.mineDoors = null;
        }
    }

    public void checkChangedAttitude(Creature creature) {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    vz.sendAttitude(creature);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public void sendUpdateTarget(Creature creature) {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    vz.sendUpdateHasTarget(creature);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public Door[] getDoors() {
        if (this.doors != null && this.doors.size() > 0) {
            return this.doors.toArray(new Door[this.doors.size()]);
        }
        return emptyDoors;
    }

    public final int getTileX() {
        return this.tilex;
    }

    public final int getTileY() {
        return this.tiley;
    }

    public final float getPosX() {
        return (this.tilex << 2) + 2;
    }

    public final float getPosY() {
        return (this.tiley << 2) + 2;
    }

    public final void pollMagicFences(long time) {
        if (this.magicFences != null) {
            for (Fence f : this.magicFences.values()) {
                f.pollMagicFences(time);
            }
        }
    }

    public void pollStructures(long time) {
        if (this.floors != null) {
            for (TimeConstants timeConstants : this.getFloors()) {
                if (!((Floor)timeConstants).poll(time, this, this.structure)) continue;
                this.removeFloor((Floor)timeConstants);
            }
        }
        if (this.walls != null) {
            Wall[] lTempWalls = this.getWalls();
            for (int x = 0; x < lTempWalls.length; ++x) {
                lTempWalls[x].poll(time, this, this.structure);
            }
        }
        if (this.fences != null) {
            for (TimeConstants timeConstants : this.getFences()) {
                ((Fence)timeConstants).poll(time);
            }
        }
        if (this.bridgeParts != null) {
            for (TimeConstants timeConstants : this.getBridgeParts()) {
                if (!((BridgePart)timeConstants).poll(time, this.structure)) continue;
                this.removeBridgePart((BridgePart)timeConstants);
            }
        }
        if (this.structure != null) {
            this.structure.poll(time);
        }
    }

    public void poll(boolean pollItems, int seed, boolean setAreaEffectFlag) {
        boolean lava = this.isLava();
        long now = System.nanoTime();
        if (this.vitems != null) {
            this.vitems.poll(pollItems, seed, lava, this.structure, this.isOnSurface(), this.village, now);
        }
        this.pollMagicFences(now);
        if (lava) {
            for (Creature c : this.getCreatures()) {
                c.setDoLavaDamage(true);
            }
        }
        if (setAreaEffectFlag && this.getAreaEffect() != null) {
            for (Creature c : this.getCreatures()) {
                c.setDoAreaEffect(true);
            }
        }
        this.pollAllUnlockedDoorsOnThisTile();
        this.applyLavaDamageToWallsAndFences();
        this.checkDeletion();
        if (Servers.isThisAPvpServer()) {
            this.pollOnDeedEnemys();
        }
    }

    private void pollOnDeedEnemys() {
        if (this.getVillage() != null) {
            for (Creature c : this.getCreatures()) {
                if (c.getPower() >= 1 || !c.isPlayer() || this.getVillage().kingdom == c.getKingdomId()) continue;
                try {
                    c.currentVillage.getToken().setLastOwnerId(System.currentTimeMillis());
                }
                catch (NoSuchItemException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public final boolean doAreaDamage(Creature aCreature) {
        AreaSpellEffect aes;
        boolean dead = false;
        if (!(aCreature.isInvulnerable() || aCreature.isGhost() || aCreature.isUnique() || (aes = this.getAreaEffect()) == null)) {
            int pz;
            int heightOffset;
            System.out.println("AREA DAMAGE " + aCreature.getName());
            if (aes.getFloorLevel() != aCreature.getFloorLevel() && (heightOffset = aes.getHeightOffset()) != 0 && Math.abs((pz = aCreature.getPosZDirts()) - heightOffset) > 10) {
                System.out.println("AREA DAMAGE FAILED");
                return false;
            }
            byte type = this.getAreaEffect().getType();
            Creature caster = null;
            try {
                caster = Server.getInstance().getCreature(aes.getCreator());
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
            if (caster != null) {
                try {
                    if (aCreature.getAttitude(caster) == 2 || caster.getCitizenVillage() != null && caster.getCitizenVillage().isEnemy(aCreature)) {
                        Village v;
                        boolean ok = true;
                        if (!(caster.isOnPvPServer() && aCreature.isOnPvPServer() || (v = aCreature.getCurrentVillage()) == null || v.mayAttack(caster, aCreature))) {
                            ok = false;
                        }
                        if (ok) {
                            aCreature.addAttacker(caster);
                            if (type == 36 || type == 53) {
                                byte pos = aCreature.getBody().getRandomWoundPos();
                                this.sendAttachCreatureEffect(aCreature, (byte)6, (byte)0, (byte)0, (byte)0, (byte)0);
                                double damage = (double)this.getAreaEffect().getPower() * 4.0;
                                damage += 150.0;
                                double resistance = SpellResist.getSpellResistance(aCreature, 414);
                                SpellResist.addSpellResistance(aCreature, 414, damage *= resistance);
                                damage = Spell.modifyDamage(aCreature, damage);
                                dead = CombatEngine.addWound(caster, aCreature, (byte)8, pos, damage, 1.0f, "", null, 0.0f, 0.0f, false, false, true, true);
                            } else if (type == 35 || type == 51) {
                                byte pos = aCreature.getBody().getRandomWoundPos();
                                double damage = (double)this.getAreaEffect().getPower() * 2.75;
                                damage += 300.0;
                                double resistance = SpellResist.getSpellResistance(aCreature, 420);
                                SpellResist.addSpellResistance(aCreature, 420, damage *= resistance);
                                damage = Spell.modifyDamage(aCreature, damage);
                                dead = CombatEngine.addWound(caster, aCreature, (byte)4, pos, damage, 1.0f, "", null, 0.0f, 0.0f, false, false, true, true);
                            } else if (type == 34) {
                                byte pos = aCreature.getBody().getRandomWoundPos();
                                double damage = (double)this.getAreaEffect().getPower() * 1.0;
                                damage += 400.0;
                                double resistance = SpellResist.getSpellResistance(aCreature, 418);
                                SpellResist.addSpellResistance(aCreature, 418, damage *= resistance);
                                damage = Spell.modifyDamage(aCreature, damage);
                                dead = CombatEngine.addWound(caster, aCreature, (byte)0, pos, damage, 1.0f, "", null, 1.0f, 0.0f, false, false, true, true);
                            } else if (type == 37) {
                                this.sendAttachCreatureEffect(aCreature, (byte)7, (byte)0, (byte)0, (byte)0, (byte)0);
                                byte pos = aCreature.getBody().getRandomWoundPos();
                                double damage = (double)this.getAreaEffect().getPower() * 2.0;
                                damage += 350.0;
                                double resistance = SpellResist.getSpellResistance(aCreature, 433);
                                SpellResist.addSpellResistance(aCreature, 433, damage *= resistance);
                                damage = Spell.modifyDamage(aCreature, damage);
                                dead = CombatEngine.addWound(caster, aCreature, (byte)5, pos, damage, 1.0f, "", null, 0.0f, 3.0f, false, false, true, true);
                            }
                        }
                    }
                }
                catch (Exception exe) {
                    logger.log(Level.WARNING, exe.getMessage(), exe);
                }
            }
        }
        return dead;
    }

    private void pollAllUnlockedDoorsOnThisTile() {
        if (this.unlockedDoors != null && this.unlockedDoors.size() > 0) {
            Iterator<Door> it = this.unlockedDoors.iterator();
            while (it.hasNext()) {
                if (!it.next().pollUnlocked()) continue;
                it.remove();
            }
        }
        if (this.unlockedDoors != null && this.unlockedDoors.isEmpty()) {
            this.unlockedDoors = null;
        }
    }

    private void applyLavaDamageToWallsAndFences() {
        if (this.isLava()) {
            if (this.walls != null) {
                Wall[] lTempWalls = this.getWalls();
                for (int x = 0; x < lTempWalls.length; ++x) {
                    lTempWalls[x].setDamage(lTempWalls[x].getDamage() + 1.0f);
                }
            }
            if (this.fences != null) {
                for (Fence f : this.getFences()) {
                    f.setDamage(f.getDamage() + 1.0f);
                }
            }
        }
    }

    private void pollAllWatchersOfThisTile() {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (!(vz.getWatcher() instanceof Player) || vz.getWatcher().hasLink()) continue;
                this.removeWatcher(vz);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private void pollAllCreaturesOnThisTile(boolean lava, boolean areaEffect) {
        long lStart = System.nanoTime();
        Creature[] lTempCreatures = this.getCreatures();
        for (int x = 0; x < lTempCreatures.length; ++x) {
            this.pollOneCreatureOnThisTile(lava, lTempCreatures[x], areaEffect);
        }
        if ((float)(System.nanoTime() - lStart) / 1000000.0f > 300.0f && !Servers.localServer.testServer) {
            int destroyed = 0;
            for (int y = 0; y < lTempCreatures.length; ++y) {
                if (!lTempCreatures[y].isDead()) continue;
                ++destroyed;
            }
            logger.log(Level.INFO, "Tile at " + this.tilex + ", " + this.tiley + " polled " + lTempCreatures.length + " creatures. Of those were " + destroyed + " destroyed. It took " + (float)(System.nanoTime() - lStart) / 1000000.0f + " ms");
        }
    }

    public final boolean isVisibleToPlayers() {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher() == null || !vz.getWatcher().isPlayer()) continue;
                return true;
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return false;
    }

    private void pollOneCreatureOnThisTile(boolean lava, Creature aCreature, boolean areaEffect) {
        block19: {
            try {
                AreaSpellEffect aes;
                boolean dead = false;
                if (aCreature.poll()) {
                    this.deleteCreature(aCreature);
                    break block19;
                }
                if (lava) {
                    if (aCreature.isInvulnerable() || aCreature.isGhost() || aCreature.isUnique() || aCreature.getDeity() != null && aCreature.getDeity().isMountainGod() && !(aCreature.getFaith() < 35.0f) || aCreature.getFarwalkerSeconds() > 0) break block19;
                    Wound wound = null;
                    try {
                        byte pos = aCreature.getBody().getRandomWoundPos((byte)10);
                        if (Server.rand.nextInt(10) <= 6 && aCreature.getBody().getWounds() != null && (wound = aCreature.getBody().getWounds().getWoundAtLocation(pos)) != null) {
                            dead = wound.modifySeverity((int)(5000.0f + (float)Server.rand.nextInt(5000) * (100.0f - aCreature.getSpellDamageProtectBonus()) / 100.0f));
                            wound.setBandaged(false);
                            aCreature.setWounded();
                        }
                        if (!(wound != null || aCreature.isGhost() || aCreature.isUnique() || aCreature.isKingdomGuard())) {
                            dead = aCreature.addWoundOfType(null, (byte)4, pos, false, 1.0f, true, 5000.0f + (float)Server.rand.nextInt(5000) * (100.0f - aCreature.getSpellDamageProtectBonus()) / 100.0f, 0.0f, 0.0f, false, false);
                        }
                        aCreature.getCommunicator().sendAlertServerMessage("You are burnt by lava!");
                        if (dead) {
                            aCreature.achievement(142);
                            this.deleteCreature(aCreature);
                        }
                        break block19;
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, aCreature.getName() + " " + ex.getMessage(), ex);
                    }
                    break block19;
                }
                if (dead || !areaEffect || aCreature.isInvulnerable() || aCreature.isGhost() || aCreature.isUnique() || (aes = this.getAreaEffect()) == null || aes.getFloorLevel() != aCreature.getFloorLevel()) break block19;
                byte type = aes.getType();
                Creature caster = null;
                try {
                    caster = Server.getInstance().getCreature(aes.getCreator());
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
                if (caster == null) break block19;
                try {
                    if (aCreature.getAttitude(caster) == 2 || caster.getCitizenVillage() != null && caster.getCitizenVillage().isEnemy(aCreature)) {
                        Village v;
                        boolean ok = true;
                        if (!(caster.isOnPvPServer() && aCreature.isOnPvPServer() || (v = aCreature.getCurrentVillage()) == null || v.mayAttack(caster, aCreature))) {
                            ok = false;
                        }
                        if (ok) {
                            aCreature.addAttacker(caster);
                        }
                    }
                }
                catch (Exception exe) {
                    logger.log(Level.WARNING, exe.getMessage(), exe);
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to poll creature " + aCreature.getWurmId() + " " + ex.getMessage(), ex);
                try {
                    Server.getInstance().getCreature(aCreature.getWurmId());
                }
                catch (Exception nsc) {
                    logger.log(Level.INFO, "Failed to locate creature. Removing from tile. Creature: " + aCreature);
                    if (this.creatures == null) break block19;
                    this.creatures.remove(aCreature);
                }
            }
        }
    }

    public void deleteCreature(Creature creature) {
        creature.setNewTile(null, 0.0f, false);
        this.removeCreature(creature);
        try {
            this.zone.deleteCreature(creature, false);
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, nsc.getMessage(), nsc);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        Door[] _doors = this.getDoors();
        for (int d = 0; d < _doors.length; ++d) {
            _doors[d].removeCreature(creature);
        }
    }

    boolean containsCreature(Creature creature) {
        return this.creatures != null && this.creatures.contains(creature);
    }

    public void deleteCreatureQuick(Creature creature) {
        creature.setNewTile(null, 0.0f, false);
        this.zone.removeCreature(creature, true, false);
        Door[] lDoors = this.getDoors();
        for (int d = 0; d < lDoors.length; ++d) {
            lDoors[d].removeCreature(creature);
        }
    }

    public void broadCastMulticolored(List<MulticolorLineSegment> segments, Creature performer, @Nullable Creature receiver, boolean combat, byte onScreenMessage) {
        if (this.creatures != null) {
            for (Creature creature : this.creatures) {
                if (creature.equals(performer) || receiver != null && creature.equals(receiver) || !performer.isVisibleTo(creature) || creature.getCommunicator().isInvulnerable()) continue;
                if (combat) {
                    creature.getCommunicator().sendColoredMessageCombat(segments, onScreenMessage);
                    continue;
                }
                creature.getCommunicator().sendColoredMessageEvent(segments, onScreenMessage);
            }
        }
    }

    public void broadCastAction(String message, Creature performer, boolean combat) {
        this.broadCastAction(message, performer, null, combat);
    }

    public void broadCastAction(String message, Creature performer, @Nullable Creature receiver, boolean combat) {
        if (this.creatures != null) {
            for (Creature creature : this.creatures) {
                if (creature.equals(performer) || receiver != null && creature.equals(receiver) || !performer.isVisibleTo(creature) || creature.getCommunicator().isInvulnerable()) continue;
                if (combat) {
                    creature.getCommunicator().sendCombatNormalMessage(message);
                    continue;
                }
                creature.getCommunicator().sendNormalServerMessage(message);
            }
        }
    }

    public void broadCast(String message) {
        if (this.creatures != null) {
            for (Creature creature : this.creatures) {
                if (creature.getCommunicator().isInvulnerable()) continue;
                creature.getCommunicator().sendNormalServerMessage(message);
            }
        }
    }

    public void broadCastMessage(Message message) {
        if (this.watchers != null) {
            for (VirtualZone z : this.watchers) {
                z.broadCastMessage(message);
            }
        }
    }

    void broadCastMessageLocal(Message message) {
        if (this.creatures != null) {
            for (Creature creature : this.creatures) {
                if (creature.getCommunicator().isInvulnerable()) continue;
                creature.getCommunicator().sendMessage(message);
            }
        }
    }

    void addWatcher(VirtualZone watcher) {
        if (this.watchers == null) {
            this.watchers = new HashSet<VirtualZone>();
        }
        if (!this.watchers.contains(watcher)) {
            this.watchers.add(watcher);
            this.linkTo(watcher, false);
            if (this.doors != null) {
                for (Door door : this.doors) {
                    door.addWatcher(watcher);
                }
            }
            if (this.mineDoors != null) {
                for (MineDoorPermission mineDoorPermission : this.mineDoors) {
                    mineDoorPermission.addWatcher(watcher);
                }
            }
        }
    }

    void removeWatcher(VirtualZone watcher) {
        if (this.watchers != null) {
            if (this.watchers.contains(watcher)) {
                this.watchers.remove(watcher);
                this.linkTo(watcher, true);
                if (this.doors != null) {
                    for (Door door : this.doors) {
                        door.removeWatcher(watcher);
                    }
                }
                if (this.mineDoors != null) {
                    for (MineDoorPermission mineDoorPermission : this.mineDoors) {
                        mineDoorPermission.removeWatcher(watcher);
                    }
                }
                if (!this.isVisibleToPlayers()) {
                    for (Creature c : this.getCreatures()) {
                        c.setVisibleToPlayers(false);
                    }
                }
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Tile: " + this.tilex + ", " + this.tiley + "removing watcher " + watcher.getId());
            }
        } else if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Tile: " + this.tilex + ", " + this.tiley + " tried to remove but watchers is null though.");
        }
    }

    void addEffect(Effect effect, boolean temp) {
        if (this.isTransition && this.surfaced) {
            this.getCaveTile().addEffect(effect, temp);
            return;
        }
        if (!temp) {
            if (this.effects == null) {
                this.effects = new HashSet<Effect>();
            }
            this.effects.add(effect);
        }
        if (this.watchers != null) {
            Iterator<VirtualZone> it = this.watchers.iterator();
            while (it.hasNext()) {
                it.next().addEffect(effect, temp);
            }
        }
        effect.setSurfaced(this.surfaced);
        try {
            effect.save();
        }
        catch (IOException iox) {
            logger.log(Level.INFO, iox.getMessage(), iox);
        }
    }

    int addCreature(Creature creature, float diffZ) throws NoSuchCreatureException, NoSuchPlayerException {
        if (this.inactive) {
            logger.log(Level.WARNING, "AT 1 adding " + creature.getName() + " who is at " + creature.getTileX() + ", " + creature.getTileY() + " to inactive tile " + this.tilex + "," + this.tiley, new Exception());
            logger.log(Level.WARNING, "The zone " + this.zone.id + " covers " + this.zone.startX + ", " + this.zone.startY + " to " + this.zone.endX + "," + this.zone.endY);
        }
        if (!creature.setNewTile(this, diffZ, false)) {
            return 0;
        }
        if (this.creatures == null) {
            this.creatures = new HashSet<Creature>();
        }
        for (Creature c : this.creatures) {
            if (c.isFriendlyKingdom(creature.getKingdomId())) continue;
            c.setStealth(false);
        }
        this.creatures.add(creature);
        creature.setCurrentVillage(this.village);
        creature.calculateZoneBonus(this.tilex, this.tiley, this.surfaced);
        if (creature.isPlayer()) {
            try {
                FaithZone z = Zones.getFaithZone(this.tilex, this.tiley, this.surfaced);
                if (z != null) {
                    creature.setCurrentDeity(z.getCurrentRuler());
                } else {
                    creature.setCurrentDeity(null);
                }
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, "No faith zone here? " + this.tilex + ", " + this.tiley + ", surf=" + this.surfaced);
            }
        }
        if (creature.getHighwayPathDestination().length() > 0 || creature.isWagoner()) {
            Floor floor;
            BridgePart bridgePart;
            HighwayPos currentHighwayPos = null;
            if (creature.getBridgeId() != -10L && (bridgePart = Zones.getBridgePartFor(this.tilex, this.tiley, this.surfaced)) != null) {
                currentHighwayPos = MethodsHighways.getHighwayPos(bridgePart);
            }
            if (currentHighwayPos == null && creature.getFloorLevel() > 0 && (floor = Zones.getFloor(this.tilex, this.tiley, this.surfaced, creature.getFloorLevel())) != null) {
                currentHighwayPos = MethodsHighways.getHighwayPos(floor);
            }
            if (currentHighwayPos == null) {
                currentHighwayPos = MethodsHighways.getHighwayPos(this.tilex, this.tiley, this.surfaced);
            }
            if (currentHighwayPos != null) {
                Item waystone = this.getWaystone(currentHighwayPos);
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)1));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)-128));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)64));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)32));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)2));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)4));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)16));
                }
                if (waystone == null) {
                    waystone = this.getWaystone(MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)8));
                }
                if (waystone != null && creature.getLastWaystoneChecked() != waystone.getWurmId()) {
                    if (creature.isWagoner()) {
                        creature.setLastWaystoneChecked(waystone.getWurmId());
                    } else {
                        Node startNode = Routes.getNode(waystone.getWurmId());
                        String goingto = creature.getHighwayPathDestination();
                        if (startNode.getVillage() == null || creature.currentVillage != null || !startNode.getVillage().getName().equalsIgnoreCase(goingto)) {
                            creature.setLastWaystoneChecked(waystone.getWurmId());
                            try {
                                Village destinationVillage = Villages.getVillage(goingto);
                                HighwayFinder.queueHighwayFinding(creature, startNode, destinationVillage, (byte)0);
                            }
                            catch (NoSuchVillageException e) {
                                creature.getCommunicator().sendNormalServerMessage("Destination village (" + goingto + ") cannot be found.");
                            }
                        }
                    }
                }
            }
        }
        return this.creatures.size();
    }

    @Nullable
    private Item getWaystone(@Nullable HighwayPos highwayPos) {
        if (highwayPos == null) {
            return null;
        }
        Item marker = MethodsHighways.getMarker(highwayPos);
        if (marker != null && marker.getTemplateId() == 1112) {
            return marker;
        }
        return null;
    }

    public boolean removeCreature(Creature creature) {
        if (this.creatures != null) {
            boolean removed = this.creatures.remove(creature);
            if (this.creatures.isEmpty()) {
                this.creatures = null;
            }
            if (!removed) {
                return false;
            }
            Door[] doorArr = this.getDoors();
            for (int d = 0; d < doorArr.length; ++d) {
                if (doorArr[d].covers(creature.getPosX(), creature.getPosY(), creature.getPositionZ(), creature.getFloorLevel(), creature.followsGround())) continue;
                doorArr[d].removeCreature(creature);
            }
            if (this.watchers != null) {
                for (VirtualZone watchingZone : this.watchers) {
                    watchingZone.removeCreature(creature);
                }
            }
            return true;
        }
        return false;
    }

    public boolean checkOpportunityAttacks(Creature creature) {
        Creature[] lTempCreatures = this.getCreatures();
        for (int x = 0; x < lTempCreatures.length; ++x) {
            if (lTempCreatures[x] == creature || lTempCreatures[x].isMoving() || lTempCreatures[x].getAttitude(creature) != 2 || !VirtualZone.isCreatureTurnedTowardsTarget(creature, lTempCreatures[x])) continue;
            return lTempCreatures[x].opportunityAttack(creature);
        }
        return false;
    }

    public void makeInvisible(Creature creature) {
        if (this.watchers != null) {
            for (VirtualZone watchingZone : this.watchers) {
                watchingZone.makeInvisible(creature);
            }
        }
    }

    public void makeVisible(Creature creature) throws NoSuchCreatureException, NoSuchPlayerException {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    vz.addCreature(creature.getWurmId(), false);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public void makeInvisible(Item item) {
        if (this.watchers != null) {
            for (VirtualZone watchingZone : this.watchers) {
                watchingZone.removeItem(item);
            }
        }
    }

    public void makeVisible(Item item) {
        if (this.watchers != null) {
            for (VirtualZone watchingZone : this.watchers) {
                watchingZone.addItem(item, this, -10L, true);
            }
        }
    }

    private VolaTile getCaveTile() {
        try {
            Zone z = Zones.getZone(this.tilex, this.tiley, false);
            return z.getOrCreateTile(this.tilex, this.tiley);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            logger.log(Level.WARNING, "No cave tile for " + this.tilex + ", " + this.tiley);
            return this;
        }
    }

    private VolaTile getSurfaceTile() {
        try {
            Zone z = Zones.getZone(this.tilex, this.tiley, true);
            return z.getOrCreateTile(this.tilex, this.tiley);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            logger.log(Level.WARNING, "No surface tile for " + this.tilex + ", " + this.tiley);
            return this;
        }
    }

    public void addItem(Item item, boolean moving, boolean starting) {
        this.addItem(item, moving, -10L, starting);
    }

    public void addItem(Item item, boolean moving, long creatureId, boolean starting) {
        if (this.inactive) {
            logger.log(Level.WARNING, "adding " + item.getName() + " to inactive tile " + this.tilex + "," + this.tiley + " surf=" + this.surfaced + " itemsurf=" + item.isOnSurface(), new Exception());
            logger.log(Level.WARNING, "The zone " + this.zone.id + " covers " + this.zone.startX + ", " + this.zone.startY + " to " + this.zone.endX + "," + this.zone.endY);
        }
        if (item.hidden) {
            return;
        }
        if (this.isTransition && this.surfaced && !item.isVehicle()) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Adding " + item.getName() + " to cave level instead.");
            }
            boolean stayOnSurface = false;
            if (Zones.getTextureForTile(this.tilex, this.tiley, 0) != Tiles.Tile.TILE_HOLE.id) {
                stayOnSurface = true;
            }
            if (!stayOnSurface) {
                this.getCaveTile().addItem(item, moving, starting);
                return;
            }
        }
        if (item.isTileAligned()) {
            item.setPosXY((this.tilex << 2) + 2, (this.tiley << 2) + 2);
            item.setOwnerId(-10L);
            if (item.isFence() && this.isOnSurface()) {
                VolaTile next;
                int offz = 0;
                try {
                    offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), this.surfaced)) / 10.0f);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, "Dropping fence item outside zones.");
                }
                float rot = Creature.normalizeAngle(item.getRotation());
                if (rot >= 45.0f && rot < 135.0f) {
                    next = Zones.getOrCreateTile(this.tilex + 1, this.tiley, this.surfaced);
                    next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex + 1, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, next.getZone().getId(), this.getLayer()));
                } else if (rot >= 135.0f && rot < 225.0f) {
                    next = Zones.getOrCreateTile(this.tilex, this.tiley + 1, this.surfaced);
                    next.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley + 1, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next.getZone().getId(), this.getLayer()));
                } else if (rot >= 225.0f && rot < 315.0f) {
                    this.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, this.getZone().getId(), this.getLayer()));
                } else {
                    this.addFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, this.getZone().getId(), this.getLayer()));
                }
            }
        } else if (item.getTileX() != this.tilex || item.getTileY() != this.tiley) {
            this.putRandomOnTile(item);
            item.setOwnerId(-10L);
        }
        if (!this.surfaced && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(this.tilex, this.tiley)))) {
            if (!this.getSurfaceTile().isTransition) {
                this.getSurfaceTile().addItem(item, moving, creatureId, starting);
                logger.log(Level.INFO, "adding " + item.getName() + " in rock at " + this.tilex + ", " + this.tiley + " ");
            }
            return;
        }
        item.setZoneId(this.zone.getId(), this.surfaced);
        if (!starting && !item.getTemplate().hovers()) {
            item.updatePosZ(this);
        }
        if (this.vitems == null) {
            this.vitems = new VolaTileItems();
        }
        if (this.vitems.addItem(item, starting)) {
            if (item.getTemplateId() == 726) {
                Zones.addDuelRing(item);
            }
            if (!item.isDecoration()) {
                Item pile = this.vitems.getPileItem(item.getFloorLevel());
                if (this.vitems.checkIfCreatePileItem(item.getFloorLevel()) || pile != null) {
                    if (pile == null) {
                        pile = this.createPileItem(item, starting);
                        this.vitems.addPileItem(pile);
                    }
                    pile.insertItem(item, true);
                    int data = pile.getData1();
                    if (data != -1 && item.getTemplateId() != data) {
                        pile.setData1(-1);
                        pile.setName(pile.getTemplate().getName());
                        String modelname = pile.getTemplate().getModelName().replaceAll(" ", "") + "unknown.";
                        if (this.watchers != null) {
                            Iterator<VirtualZone> it = this.watchers.iterator();
                            while (it.hasNext()) {
                                it.next().renameItem(pile, pile.getName(), modelname);
                            }
                        }
                    }
                } else if (this.watchers != null) {
                    boolean onGroundLevel = true;
                    if (item.getFloorLevel() > 0) {
                        onGroundLevel = false;
                    } else if (this.getFloors(0, 0).length > 0) {
                        onGroundLevel = false;
                    }
                    for (VirtualZone vz : this.getWatchers()) {
                        try {
                            if (!vz.isVisible(item, this)) continue;
                            vz.addItem(item, this, creatureId, onGroundLevel);
                        }
                        catch (Exception e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                }
            } else if (this.watchers != null) {
                boolean onGroundLevel = true;
                if (item.getFloorLevel() > 0) {
                    onGroundLevel = false;
                } else if (this.getFloors(0, 0).length > 0) {
                    onGroundLevel = false;
                }
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        if (!vz.isVisible(item, this)) continue;
                        vz.addItem(item, this, creatureId, onGroundLevel);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            if (item.isDomainItem()) {
                Zones.addAltar(item, moving);
            }
            if ((item.getTemplateId() == 1175 || item.getTemplateId() == 1239) && item.getAuxData() > 0) {
                Zones.addHive(item, moving);
            }
            if (item.getTemplateId() == 939 || item.isEnchantedTurret()) {
                Zones.addTurret(item, moving);
            }
            if (item.isEpicTargetItem()) {
                EpicTargetItems.addRitualTargetItem(item);
            }
            if (this.village != null && item.getTemplateId() == 757) {
                this.village.addBarrel(item);
            }
        } else {
            item.setZoneId(this.zone.getId(), this.surfaced);
            if (!item.deleted) {
                logger.log(Level.WARNING, "tile already contained item " + item.getName() + " (ID: " + item.getWurmId() + ") at " + this.tilex + ", " + this.tiley, new Exception());
            }
        }
    }

    public void updatePile(Item pile) {
        this.checkIfRenamePileItem(pile);
    }

    protected void removeItem(Item item, boolean moving) {
        if (this.vitems != null) {
            Item pileItem;
            if (!this.vitems.isEmpty()) {
                if (item.getTemplateId() == 726) {
                    Zones.removeDuelRing(item);
                }
                if ((pileItem = this.vitems.getPileItem(item.getFloorLevel())) != null && item.getWurmId() == pileItem.getWurmId()) {
                    this.vitems.removePileItem(item.getFloorLevel());
                    if (this.vitems.isEmpty()) {
                        this.vitems.destroy(this);
                        this.vitems = null;
                    }
                } else if (this.vitems.removeItem(item)) {
                    this.vitems.destroy(this);
                    this.vitems = null;
                    if (pileItem != null) {
                        this.destroyPileItem(pileItem.getFloorLevel());
                    }
                } else if (pileItem != null && this.vitems.checkIfRemovePileItem(pileItem.getFloorLevel())) {
                    if (!moving) {
                        this.destroyPileItem(pileItem.getFloorLevel());
                    }
                } else if (!item.isDecoration() && pileItem != null) {
                    this.checkIfRenamePileItem(pileItem);
                }
                if (item.isDomainItem()) {
                    Zones.removeAltar(item, moving);
                }
                if (item.getTemplateId() == 1175 || item.getTemplateId() == 1239) {
                    Zones.removeHive(item, moving);
                }
                if (item.getTemplateId() == 939 || item.isEnchantedTurret()) {
                    Zones.removeTurret(item, moving);
                }
                if (item.isEpicTargetItem()) {
                    EpicTargetItems.removeRitualTargetItem(item);
                }
                if (item.isKingdomMarker() && item.getTemplateId() != 328) {
                    Kingdoms.destroyTower(item);
                }
                if (item.getTemplateId() == 521) {
                    this.zone.creatureSpawn = null;
                    --Zone.spawnPoints;
                }
                if (this.village != null && item.getTemplateId() == 757) {
                    this.village.removeBarrel(item);
                }
            } else {
                pileItem = this.vitems.getPileItem(item.getFloorLevel());
                if (pileItem != null && item.getWurmId() == pileItem.getWurmId()) {
                    this.vitems.removePileItem(item.getFloorLevel());
                } else if (pileItem != null && this.vitems.checkIfRemovePileItem(item.getFloorLevel())) {
                    this.destroyPileItem(item.getFloorLevel());
                }
            }
        }
        if (item.isFence()) {
            int offz = 0;
            try {
                offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), item.isOnSurface())) / 10.0f);
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, "Dropping fence item outside zones.");
            }
            float rot = Creature.normalizeAngle(item.getRotation());
            if (rot >= 45.0f && rot < 135.0f) {
                VolaTile next = Zones.getOrCreateTile(this.tilex + 1, this.tiley, item.isOnSurface() || this.isTransition);
                next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex + 1, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, next.getZone().getId(), Math.max(0, next.getLayer())));
            } else if (rot >= 135.0f && rot < 225.0f) {
                VolaTile next = Zones.getOrCreateTile(this.tilex, this.tiley + 1, item.isOnSurface() || this.isTransition);
                next.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley + 1, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, next.getZone().getId(), Math.max(0, next.getLayer())));
            } else if (rot >= 225.0f && rot < 315.0f) {
                this.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_DOWN, this.getZone().getId(), Math.max(0, this.getLayer())));
            } else {
                this.removeFence(new TempFence(StructureConstantsEnum.FENCE_SIEGEWALL, this.tilex, this.tiley, offz, item, Tiles.TileBorderDirection.DIR_HORIZ, this.getZone().getId(), Math.max(0, this.getLayer())));
            }
        }
        this.sendRemoveItem(item, moving);
        if (!moving) {
            item.setZoneId(-10, this.surfaced);
        }
    }

    private void checkIfRenamePileItem(Item pileItem) {
        if (pileItem.getData1() == -1) {
            int itid = -1;
            byte material = 0;
            boolean multipleMaterials = false;
            for (Item item : pileItem.getItems()) {
                if (!multipleMaterials && item.getMaterial() != material) {
                    if (material == 0) {
                        material = item.getMaterial();
                    } else {
                        material = 0;
                        multipleMaterials = true;
                    }
                }
                if (itid == -1) {
                    itid = item.getTemplateId();
                    continue;
                }
                if (itid == item.getTemplateId()) continue;
                itid = -1;
                break;
            }
            if (itid != -1) {
                try {
                    String name = pileItem.getTemplate().getName();
                    pileItem.setData1(itid);
                    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(itid);
                    String tname = template.getName();
                    name = "Pile of " + template.sizeString + tname;
                    pileItem.setMaterial(material);
                    StringBuilder build = new StringBuilder();
                    build.append(pileItem.getTemplate().getModelName());
                    build.append(tname);
                    build.append(".");
                    build.append(MaterialUtilities.getMaterialString(pileItem.getMaterial()));
                    String modelname = build.toString().replaceAll(" ", "").trim();
                    pileItem.setName(name);
                    if (this.watchers != null) {
                        Iterator<VirtualZone> it = this.watchers.iterator();
                        while (it.hasNext()) {
                            it.next().renameItem(pileItem, name, modelname);
                        }
                    }
                }
                catch (NoSuchTemplateException noSuchTemplateException) {
                    // empty catch block
                }
            }
        }
    }

    private void sendRemoveItem(Item item, boolean moving) {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    if (moving && vz.isVisible(item, this)) continue;
                    vz.removeItem(item);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public final void sendSetBridgeId(Creature creature, long bridgeId, boolean sendToSelf) {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    if (sendToSelf) {
                        if (creature.getWurmId() == vz.getWatcher().getWurmId()) {
                            vz.sendBridgeId(-1L, bridgeId);
                            continue;
                        }
                        if (!creature.isVisibleTo(vz.getWatcher())) continue;
                        vz.sendBridgeId(creature.getWurmId(), bridgeId);
                        continue;
                    }
                    if (creature.getWurmId() == vz.getWatcher().getWurmId() || !creature.isVisibleTo(vz.getWatcher())) continue;
                    vz.sendBridgeId(creature.getWurmId(), bridgeId);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public final void sendSetBridgeId(Item item, long bridgeId) {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    if (!vz.isVisible(item, this)) continue;
                    vz.sendBridgeId(item.getWurmId(), bridgeId);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public void removeWall(Wall wall, boolean silent) {
        if (wall != null) {
            if (this.walls != null) {
                this.walls.remove(wall);
                if (this.walls.size() == 0) {
                    this.walls = null;
                }
            }
            if (this.watchers != null && !silent) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.removeWall(this.structure.getWurmId(), wall);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
    }

    boolean removeEffect(Effect effect) {
        boolean removed = false;
        if (this.effects != null && this.effects.contains(effect)) {
            this.effects.remove(effect);
            if (this.watchers != null) {
                Iterator<VirtualZone> it = this.watchers.iterator();
                while (it.hasNext()) {
                    it.next().removeEffect(effect);
                }
            }
            if (this.effects.size() == 0) {
                this.effects = null;
            }
            removed = true;
        }
        return removed;
    }

    @Override
    public void creatureMoved(long creatureId, float diffX, float diffY, float diffZ, int diffTileX, int diffTileY) throws NoSuchCreatureException, NoSuchPlayerException {
        this.creatureMoved(creatureId, diffX, diffY, diffZ, diffTileX, diffTileY, false);
    }

    public void creatureMoved(long creatureId, float diffX, float diffY, float diffZ, int diffTileX, int diffTileY, boolean passenger) throws NoSuchCreatureException, NoSuchPlayerException {
        Creature creature = Server.getInstance().getCreature(creatureId);
        int tileX = this.tilex + diffTileX;
        int tileY = this.tiley + diffTileY;
        boolean changedLevel = false;
        if (diffTileX != 0 || diffTileY != 0) {
            if (!creature.isPlayer()) {
                boolean godown;
                block46: {
                    boolean following = creature.getLeader() != null || creature.isRidden() || creature.getHitched() != null;
                    godown = false;
                    if (this.surfaced && following) {
                        if (creature.isRidden() || creature.getHitched() != null) {
                            Creature rider;
                            if (creature.getHitched() != null) {
                                rider = Server.getInstance().getCreature(creature.getHitched().pilotId);
                                if (!rider.isOnSurface()) {
                                    godown = true;
                                }
                            } else {
                                try {
                                    if (creature.getMountVehicle() != null) {
                                        rider = Server.getInstance().getCreature(creature.getMountVehicle().pilotId);
                                        if (!rider.isOnSurface()) {
                                            godown = true;
                                        }
                                        break block46;
                                    }
                                    logger.log(Level.WARNING, "Mount Vehicle is null for ridden " + creature.getWurmId());
                                }
                                catch (NoSuchCreatureException rider2) {
                                }
                                catch (NoSuchPlayerException rider2) {}
                            }
                        } else if (creature.getLeader() != null && !creature.getLeader().isOnSurface()) {
                            godown = true;
                        }
                    }
                }
                if (!this.surfaced) {
                    if (this.isTransition && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tileX, tileY)))) {
                        changedLevel = true;
                        creature.getStatus().setLayer(0);
                    } else if (creature.getLeader() != null && creature.getLeader().isOnSurface() && !this.isTransition) {
                        changedLevel = true;
                        creature.getStatus().setLayer(0);
                    }
                } else if (Tiles.decodeType(Server.surfaceMesh.getTile(tileX, tileY)) == Tiles.Tile.TILE_HOLE.id || godown && Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tileX, tileY)))) {
                    changedLevel = true;
                    creature.getStatus().setLayer(-1);
                }
                VolaTile nextTile = Zones.getTileOrNull(tileX, tileY, this.isOnSurface());
                if (nextTile != null) {
                    if (nextTile.getStructure() != null && creature.getBridgeId() != nextTile.getStructure().getWurmId()) {
                        if (nextTile.getBridgeParts().length > 0) {
                            for (BridgePart bp : nextTile.getBridgeParts()) {
                                int ht = Math.max(0, creature.getPosZDirts());
                                if (Math.abs(ht - bp.getHeightOffset()) >= 25 || !bp.hasAnExit()) continue;
                                creature.setBridgeId(nextTile.structure.getWurmId());
                            }
                        }
                    } else if (creature.getBridgeId() > 0L && (nextTile.getStructure() == null || nextTile.getStructure().getWurmId() != creature.getBridgeId())) {
                        boolean leave = true;
                        BridgePart[] parts = this.getBridgeParts();
                        if (parts != null) {
                            for (BridgePart bp : parts) {
                                if (!bp.isFinished()) continue;
                                if (bp.getDir() == 0 || bp.getDir() == 4) {
                                    if (this.getTileY() != nextTile.getTileY()) continue;
                                    leave = false;
                                    continue;
                                }
                                if (this.getTileX() != nextTile.getTileX()) continue;
                                leave = false;
                            }
                        }
                        if (leave) {
                            creature.setBridgeId(-10L);
                        }
                    }
                }
            }
            if (!changedLevel && this.zone.covers(tileX, tileY)) {
                VolaTile newTile = this.zone.getOrCreateTile(tileX, tileY);
                newTile.addCreature(creature, diffZ);
            } else {
                try {
                    this.zone.removeCreature(creature, changedLevel, false);
                    Zone newZone = Zones.getZone(tileX, tileY, creature.isOnSurface());
                    newZone.addCreature(creature.getWurmId());
                }
                catch (NoSuchZoneException sex) {
                    logger.log(Level.INFO, sex.getMessage() + " this tile at " + this.tilex + "," + this.tiley + ", diff=" + diffTileX + ", " + diffTileY, sex);
                }
            }
            if (!passenger) {
                this.zone.createTrack(creature, this.tilex, this.tiley, diffTileX, diffTileY);
            }
        }
        if (this.isTransition) {
            if (!passenger) {
                VolaTile.updateNeighbourTileDoors(creature, this.tilex, this.tiley);
            }
        } else if (!passenger) {
            this.doorCreatureMoved(creature, diffTileX, diffTileY);
        }
        if (!changedLevel && !passenger) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    if (diffZ * 10.0f <= 127.0f && diffZ * 10.0f >= -128.0f) {
                        if (!vz.creatureMoved(creatureId, diffX, diffY, diffZ, diffTileX, diffTileY)) continue;
                        logger.log(Level.INFO, "Forcibly removing watcher " + vz);
                        this.removeWatcher(vz);
                        continue;
                    }
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest(creature.getName() + " moved more than byte max (" + 127 + ") or min (" + -128 + ") in z: " + diffZ + " at " + this.tilex + ", " + this.tiley + " surfaced=" + this.isOnSurface());
                    }
                    this.makeInvisible(creature);
                    this.makeVisible(creature);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Exception when " + creature.getName() + " moved at " + this.tilex + ", " + this.tiley + " tile surf=" + this.isOnSurface() + " cret onsurf=" + creature.isOnSurface() + ": ", ex);
                }
            }
        }
        if (creature instanceof Player && !passenger && creature.getBridgeId() != -10L && this.getStructure() != null && this.getStructure().isTypeBridge()) {
            this.getStructure().setWalkedOnBridge(System.currentTimeMillis());
        }
    }

    private static void updateNeighbourTileDoors(Creature creature, int tilex, int tiley) {
        VolaTile newTile;
        if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley - 1)))) {
            newTile = Zones.getOrCreateTile(tilex, tiley - 1, true);
            newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
        } else {
            newTile = Zones.getOrCreateTile(tilex, tiley - 1, false);
            newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
        }
        if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex + 1, tiley)))) {
            newTile = Zones.getOrCreateTile(tilex + 1, tiley, true);
            newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
        } else {
            newTile = Zones.getOrCreateTile(tilex + 1, tiley, false);
            newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
        }
        if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley + 1)))) {
            newTile = Zones.getOrCreateTile(tilex, tiley + 1, true);
            newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
        } else {
            newTile = Zones.getOrCreateTile(tilex, tiley + 1, false);
            newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
        }
        if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex - 1, tiley)))) {
            newTile = Zones.getOrCreateTile(tilex - 1, tiley, true);
            newTile.getSurfaceTile().doorCreatureMoved(creature, 0, 0);
        } else {
            newTile = Zones.getOrCreateTile(tilex - 1, tiley, false);
            newTile.getCaveTile().doorCreatureMoved(creature, 0, 0);
        }
    }

    private void doorCreatureMoved(Creature creature, int diffTileX, int diffTileY) {
        if (this.doors != null) {
            for (Door door : this.doors) {
                door.creatureMoved(creature, diffTileX, diffTileY);
            }
        }
    }

    @Nonnull
    public Creature[] getCreatures() {
        if (this.creatures != null) {
            return this.creatures.toArray(new Creature[this.creatures.size()]);
        }
        return emptyCreatures;
    }

    public Item[] getItems() {
        if (this.vitems != null) {
            return this.vitems.getAllItemsAsArray();
        }
        return emptyItems;
    }

    final Effect[] getEffects() {
        if (this.effects != null) {
            return this.effects.toArray(new Effect[this.effects.size()]);
        }
        return emptyEffects;
    }

    public VirtualZone[] getWatchers() {
        if (this.watchers != null) {
            return this.watchers.toArray(new VirtualZone[this.watchers.size()]);
        }
        return emptyWatchers;
    }

    public final int getMaxFloorLevel() {
        if (!this.surfaced || this.isTransition) {
            return 3;
        }
        int toRet = 0;
        if (this.floors != null) {
            toRet = 1;
            for (Floor f : this.floors) {
                if (f.getFloorLevel() <= toRet) continue;
                toRet = f.getFloorLevel();
            }
        }
        if (this.bridgeParts != null) {
            toRet = 1;
            for (BridgePart b : this.bridgeParts) {
                if (b.getFloorLevel() <= toRet) continue;
                toRet = b.getFloorLevel();
            }
        }
        return toRet;
    }

    public final int getDropFloorLevel(int maxFloorLevel) {
        int toRet = 0;
        if (this.floors != null) {
            for (Floor f : this.floors) {
                if (!f.isSolid()) continue;
                if (f.getFloorLevel() == maxFloorLevel) {
                    return maxFloorLevel;
                }
                if (f.getFloorLevel() >= maxFloorLevel || f.getFloorLevel() <= toRet) continue;
                toRet = f.getFloorLevel();
            }
        }
        return toRet;
    }

    void sendRemoveItem(Item item) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.removeItem(item);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void change() {
        this.checkTransition();
        Item stumpToDestroy = null;
        if (this.vitems != null) {
            for (Item item : this.vitems.getAllItemsAsArray()) {
                if (item.getTileX() != this.tilex || item.getTileY() != this.tiley || item.getBridgeId() != -10L) continue;
                boolean ok = true;
                if (item.isVehicle()) {
                    Vehicle vehic = Vehicles.getVehicle(item);
                    if (vehic.getHitched().length > 0) {
                        ok = false;
                    } else {
                        for (Seat seat : vehic.getSeats()) {
                            if (seat.occupant <= 0L) continue;
                            ok = false;
                        }
                    }
                }
                if (item.getTemplateId() == 731) {
                    stumpToDestroy = item;
                    continue;
                }
                int oldFloorLevel = item.getFloorLevel();
                item.updatePosZ(this);
                Item pileItem = this.vitems.getPileItem(item.getFloorLevel());
                if (oldFloorLevel != item.getFloorLevel()) {
                    this.vitems.moveToNewFloorLevel(item, oldFloorLevel);
                    logger.log(Level.INFO, item.getName() + " moving from " + oldFloorLevel + " fl=" + item.getFloorLevel());
                }
                if (!ok || pileItem != null && !item.isDecoration()) continue;
                boolean onGroundLevel = true;
                if (item.getFloorLevel() > 0) {
                    onGroundLevel = false;
                } else if (this.getFloors(0, 0).length > 0) {
                    onGroundLevel = false;
                }
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.removeItem(item);
                        if (!vz.isVisible(item, this)) continue;
                        vz.addItem(item, this, onGroundLevel);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
        if (this.vitems != null) {
            for (Item pileItem : this.vitems.getPileItems()) {
                if (pileItem.getBridgeId() != -10L) continue;
                int oldFloorLevel = pileItem.getFloorLevel();
                pileItem.updatePosZ(this);
                boolean destroy = false;
                if (pileItem.getFloorLevel() != oldFloorLevel) {
                    destroy = this.vitems.movePileItemToNewFloorLevel(pileItem, oldFloorLevel);
                }
                if (!destroy) {
                    boolean onGroundLevel = true;
                    if (pileItem.getFloorLevel() > 0) {
                        onGroundLevel = false;
                    } else if (this.getFloors(0, 0).length > 0) {
                        onGroundLevel = false;
                    }
                    for (VirtualZone vz : this.getWatchers()) {
                        try {
                            vz.removeItem(pileItem);
                            if (!vz.isVisible(pileItem, this)) continue;
                            vz.addItem(pileItem, this, onGroundLevel);
                        }
                        catch (Exception e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                    continue;
                }
                this.destroyPileItem(pileItem);
            }
        }
        if (this.effects != null) {
            for (Effect effect : this.effects) {
                block55: {
                    if (effect.getTileX() != this.tilex || effect.getTileY() != this.tiley) continue;
                    try {
                        if (this.structure != null) break block55;
                        if (this.isTransition) {
                            effect.setPosZ(Zones.calculateHeight(effect.getPosX(), effect.getPosY(), false));
                            break block55;
                        }
                        long owner = effect.getOwner();
                        long bridgeId = -10L;
                        if (WurmId.getType(owner) == 2) {
                            try {
                                Item i = Items.getItem(owner);
                                bridgeId = i.onBridge();
                            }
                            catch (NoSuchItemException i) {}
                        } else if (WurmId.getType(owner) == 1) {
                            try {
                                Creature c = Creatures.getInstance().getCreature(owner);
                                bridgeId = c.getBridgeId();
                            }
                            catch (NoSuchCreatureException c) {}
                        } else if (WurmId.getType(owner) == 0) {
                            try {
                                Player p = Players.getInstance().getPlayer(owner);
                                bridgeId = p.getBridgeId();
                            }
                            catch (NoSuchPlayerException p) {
                                // empty catch block
                            }
                        }
                        float height = Zones.calculatePosZ(effect.getPosX(), effect.getPosY(), this, this.surfaced, false, effect.getPosZ(), null, bridgeId);
                        effect.setPosZ(height);
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, effect.getId() + " moved out of zone.");
                    }
                }
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.removeEffect(effect);
                        vz.addEffect(effect, false);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
        if (this.creatures != null) {
            for (Creature creature : this.creatures) {
                if (creature.getBridgeId() != -10L || creature instanceof Player) continue;
                if (creature.isSubmerged()) {
                    creature.submerge();
                    continue;
                }
                if (creature.getVehicle() >= 0L) continue;
                float oldPosZ = creature.getStatus().getPositionZ();
                float newPosZ = 1.0f;
                boolean surf = this.surfaced;
                if (this.isTransition) {
                    surf = false;
                }
                newPosZ = Zones.calculatePosZ(creature.getPosX(), creature.getPosY(), this, surf, false, creature.getPositionZ(), null, creature.getBridgeId());
                creature.setPositionZ(Math.max(-1.25f, newPosZ));
                try {
                    creature.savePosition(this.zone.id);
                }
                catch (Exception iox) {
                    logger.log(Level.WARNING, creature.getName() + ": " + iox.getMessage(), iox);
                }
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.creatureMoved(creature.getWurmId(), 0.0f, 0.0f, newPosZ - oldPosZ, 0, 0);
                    }
                    catch (NoSuchCreatureException nsc) {
                        logger.log(Level.INFO, "Creature not found when changing height of tile.", nsc);
                    }
                    catch (NoSuchPlayerException nsp) {
                        logger.log(Level.INFO, "Player not found when changing height of tile.", nsp);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
        if (stumpToDestroy != null) {
            Items.destroyItem(stumpToDestroy.getWurmId());
        }
        this.checkIsLava();
    }

    public Wall[] getWalls() {
        if (this.walls != null) {
            return this.walls.toArray(new Wall[this.walls.size()]);
        }
        return emptyWalls;
    }

    public BridgePart[] getBridgeParts() {
        if (this.bridgeParts != null) {
            return this.bridgeParts.toArray(new BridgePart[this.bridgeParts.size()]);
        }
        return emptyBridgeParts;
    }

    public final Set<StructureSupport> getAllSupport() {
        if (this.walls == null && this.fences == null && this.floors == null) {
            return emptySupports;
        }
        HashSet<StructureSupport> toReturn = new HashSet<StructureSupport>();
        if (this.walls != null) {
            for (Wall w : this.walls) {
                toReturn.add(w);
            }
        }
        if (this.fences != null) {
            toReturn.addAll(this.fences.values());
        }
        if (this.floors != null) {
            toReturn.addAll(this.floors);
        }
        return toReturn;
    }

    public Wall[] getWallsForLevel(int floorLevel) {
        if (this.walls != null) {
            HashSet<Wall> wallsSet = new HashSet<Wall>();
            for (Wall w : this.walls) {
                if (w.getFloorLevel() != floorLevel) continue;
                wallsSet.add(w);
            }
            return wallsSet.toArray(new Wall[wallsSet.size()]);
        }
        return emptyWalls;
    }

    public Wall[] getExteriorWalls() {
        if (this.walls != null) {
            HashSet<Wall> wallsSet = new HashSet<Wall>();
            for (Wall w : this.walls) {
                if (w.isIndoor()) continue;
                wallsSet.add(w);
            }
            return wallsSet.toArray(new Wall[wallsSet.size()]);
        }
        return emptyWalls;
    }

    Wall getWall(long wallId) throws NoSuchWallException {
        if (this.walls != null) {
            for (Wall wall : this.walls) {
                if (wall.getId() != wallId) continue;
                return wall;
            }
        }
        throw new NoSuchWallException("There are no walls on this tile so cannot find wallid: " + wallId);
    }

    Wall getWall(int startX, int startY, int endX, int endY, boolean horizontal) {
        if (this.walls != null) {
            for (Wall wall : this.walls) {
                if (wall.getStartX() != startX || wall.getStartY() != startY || wall.getEndX() != endX || wall.getEndY() != endY || wall.isHorizontal() != horizontal) continue;
                return wall;
            }
        }
        return null;
    }

    public Structure getStructure() {
        return this.structure;
    }

    /*
     * WARNING - void declaration
     */
    public void deleteStructure(long wurmStructureId) {
        VolaTile ts;
        VolaTile tw;
        Iterator<TimeConstants> it;
        if (this.structure == null) {
            return;
        }
        if (this.structure.getWurmId() != wurmStructureId) {
            logger.log(Level.WARNING, "Tried to delete structure " + wurmStructureId + " from VolaTile [" + this.tilex + "," + this.tiley + "] but it was structure " + this.structure.getWurmId() + " so nothing was deleted.");
            return;
        }
        if (this.walls != null) {
            it = this.walls.iterator();
            while (it.hasNext()) {
                Wall wall = (Wall)it.next();
                if (wall.getStructureId() != wurmStructureId) continue;
                if (wall.getType() == StructureTypeEnum.DOOR || wall.getType() == StructureTypeEnum.DOUBLE_DOOR || wall.getType() == StructureTypeEnum.PORTCULLIS || wall.getType() == StructureTypeEnum.CANOPY_DOOR || wall.isArched()) {
                    Door[] alld = this.getDoors();
                    for (int i = 0; i < alld.length; ++i) {
                        try {
                            if (alld[i].getWall() != wall) continue;
                            alld[i].removeFromTiles();
                            alld[i].delete();
                            continue;
                        }
                        catch (NoSuchWallException nsw) {
                            logger.log(Level.WARNING, nsw.getMessage(), nsw);
                        }
                    }
                }
                wall.delete();
                it.remove();
            }
        }
        if (this.floors != null) {
            it = this.floors.iterator();
            while (it.hasNext()) {
                Floor floor = (Floor)it.next();
                if (floor.getStructureId() != wurmStructureId) continue;
                floor.delete();
                it.remove();
                if (!floor.isStair()) continue;
                Stairs.removeStair(this.hashCode(), floor.getFloorLevel());
            }
        }
        if (this.bridgeParts != null) {
            it = this.bridgeParts.iterator();
            while (it.hasNext()) {
                BridgePart bridgepart = (BridgePart)it.next();
                if (bridgepart.getStructureId() != wurmStructureId) continue;
                bridgepart.delete();
                it.remove();
            }
        }
        if (this.fences != null) {
            for (Iterator<TimeConstants> iterator : this.getFences()) {
                if (((Fence)((Object)iterator)).getFloorLevel() <= 0) continue;
                ((Fence)((Object)iterator)).destroy();
            }
        }
        if ((tw = Zones.getTileOrNull(this.getTileX() + 1, this.getTileY(), this.isOnSurface())) != null && tw.getStructure() == null) {
            void var6_19;
            Fence[] bridgepart = tw.getFences();
            int alld = bridgepart.length;
            boolean bl = false;
            while (var6_19 < alld) {
                Fence fence = bridgepart[var6_19];
                if (fence.getFloorLevel() > 0) {
                    fence.destroy();
                }
                ++var6_19;
            }
        }
        if ((ts = Zones.getTileOrNull(this.getTileX(), this.getTileY() + 1, this.isOnSurface())) != null && ts.getStructure() == null) {
            for (Fence fence : ts.getFences()) {
                if (fence.getFloorLevel() <= 0) continue;
                fence.destroy();
            }
        }
        if (this.watchers != null) {
            logger.log(Level.INFO, "deleteStructure " + wurmStructureId + " (Watchers  " + this.watchers.size() + ")");
            for (VirtualZone virtualZone : this.watchers) {
                virtualZone.deleteStructure(this.structure);
            }
        }
        if (this.vitems != null) {
            for (Item pileItem : this.vitems.getPileItems()) {
                if (pileItem.getFloorLevel() > 0) {
                    float pileHeight = pileItem.getPosZ();
                    float tileHeight = 0.0f;
                    try {
                        tileHeight = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface());
                    }
                    catch (NoSuchZoneException noSuchZoneException) {
                        // empty catch block
                    }
                    if (tileHeight == pileHeight) continue;
                    this.destroyPileItem(pileItem.getFloorLevel());
                    continue;
                }
                pileItem.updatePosZ(this);
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        if (!vz.isVisible(pileItem, this)) continue;
                        vz.removeItem(pileItem);
                        vz.addItem(pileItem, this, true);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            for (Item item : this.vitems.getAllItemsAsArray()) {
                if (item.getParentId() != -10L) continue;
                item.updatePosZ(this);
                item.updateIfGroundItem();
                item.setOnBridge(-10L);
            }
        }
        if (this.creatures != null) {
            for (Creature creature : this.creatures) {
                if (!creature.isPlayer()) {
                    float oldposz = creature.getPositionZ();
                    float newPosz = creature.calculatePosZ();
                    float diffz = newPosz - oldposz;
                    creature.setPositionZ(newPosz);
                    creature.moved(0.0f, 0.0f, diffz, 0, 0);
                } else {
                    creature.getCommunicator().setGroundOffset(0, true);
                }
                creature.setBridgeId(-10L);
            }
        }
        this.structure = null;
        if (this.walls != null) {
            for (Wall wall : this.walls) {
                long sid = wall.getStructureId();
                try {
                    Structure struct;
                    this.structure = struct = Structures.getStructure(sid);
                    struct.addBuildTile(this, false);
                    break;
                }
                catch (NoSuchStructureException sns) {
                    logger.log(Level.WARNING, sns.getMessage(), " for wall " + wall);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.INFO, "Out of bounds?: " + nsz.getMessage(), nsz);
                }
            }
        }
    }

    private void updateStructureForZone(VirtualZone vzone, Structure _structure, int aTilex, int aTiley) {
        vzone.sendStructureWalls(_structure);
    }

    public void addStructure(Structure _structure) {
        if (this.structure == null && this.watchers != null) {
            for (VirtualZone vzone : this.watchers) {
                this.updateStructureForZone(vzone, _structure, this.tilex, this.tiley);
            }
        }
        this.structure = _structure;
    }

    public void addBridge(Structure bridge) {
        if (this.structure == null && this.watchers != null) {
            for (VirtualZone vzone : this.watchers) {
                vzone.addStructure(bridge);
            }
        }
        this.structure = bridge;
    }

    public void addBuildMarker(Structure _structure) {
        if (this.structure == null && this.watchers != null) {
            for (VirtualZone vzone : this.watchers) {
                vzone.addBuildMarker(_structure, this.tilex, this.tiley);
            }
        }
        this.structure = _structure;
    }

    public void setStructureAtLoad(Structure _structure) {
        this.structure = _structure;
    }

    public void removeBuildMarker(Structure _structure, int _tilex, int _tiley) {
        if (this.structure != null) {
            if (this.watchers != null) {
                for (VirtualZone vzone : this.watchers) {
                    vzone.removeBuildMarker(_structure, _tilex, _tiley);
                }
            }
            this.structure = null;
        } else {
            logger.log(Level.INFO, "Hmm tried to remove buildmarker from a tile that didn't contain it.");
        }
    }

    public void finalizeBuildPlan(long oldStructureId, long newStructureId) {
        if (this.structure != null && this.watchers != null) {
            for (VirtualZone lZone : this.watchers) {
                lZone.finalizeBuildPlan(oldStructureId, newStructureId);
            }
        }
    }

    public void addWall(StructureTypeEnum type, int x1, int y1, int x2, int y2, float qualityLevel, long structureId, boolean isIndoor) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("StructureID: " + structureId + " adding wall at " + x1 + "-" + y1 + "," + x2 + "-" + y2 + ", QL: " + qualityLevel);
        }
        DbWall inside = new DbWall(type, this.tilex, this.tiley, x1, y1, x2, y2, qualityLevel, structureId, StructureMaterialEnum.WOOD, isIndoor, 0, this.getLayer());
        this.addWall(inside);
        this.updateWall(inside);
    }

    public void addWall(Wall wall) {
        if (this.walls == null) {
            this.walls = new HashSet<Wall>();
        }
        boolean removedOneWall = false;
        for (Wall w : this.walls) {
            removedOneWall = false;
            if (wall.heightOffset != 0 || w.heightOffset != 0 || wall.x1 != w.x1 || wall.x2 != w.x2 || wall.y1 != w.y1 || wall.y2 != w.y2) continue;
            if (wall.getType().value <= w.getType().value) {
                removedOneWall = true;
                break;
            }
            if (w.getType().value >= wall.getType().value) continue;
            toRemove.add(w);
        }
        if (!removedOneWall) {
            this.walls.add(wall);
        }
        if (removedOneWall) {
            logger.log(Level.INFO, "Not adding wall at " + wall.getTileX() + ", " + wall.getTileY() + ", structure: " + this.structure);
        }
        for (Wall torem : toRemove) {
            logger.log(Level.INFO, "Deleting wall at " + torem.getTileX() + ", " + torem.getTileY() + ", structure: " + this.structure);
            torem.delete();
        }
        toRemove.clear();
    }

    public void updateFloor(Floor floor) {
        if (this.structure != null) {
            if (this.watchers != null) {
                for (VirtualZone virtualZone : this.getWatchers()) {
                    try {
                        virtualZone.updateFloor(this.structure.getWurmId(), floor);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            try {
                floor.save();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to save structure floor: " + floor.getId() + '.', iox);
            }
            if (floor.isFinished() && this.vitems != null) {
                for (ProtoConstants protoConstants : this.vitems.getAllItemsAsArray()) {
                    ((Item)protoConstants).updatePosZ(this);
                    ((Item)protoConstants).updateIfGroundItem();
                }
            }
            if (this.vitems != null) {
                for (ProtoConstants protoConstants : this.vitems.getPileItems()) {
                    ((Item)protoConstants).updatePosZ(this);
                    for (VirtualZone vz : this.getWatchers()) {
                        try {
                            if (!vz.isVisible((Item)protoConstants, this)) continue;
                            vz.removeItem((Item)protoConstants);
                            vz.addItem((Item)protoConstants, this, true);
                        }
                        catch (Exception e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    public void updateBridgePart(BridgePart bridgePart) {
        if (!this.isOnSurface() && !Features.Feature.CAVE_BRIDGES.isEnabled()) {
            this.getSurfaceTile().updateBridgePart(bridgePart);
        } else if (this.structure != null) {
            if (this.watchers != null) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.updateBridgePart(this.structure.getWurmId(), bridgePart);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            try {
                bridgePart.save();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to save structure bridge part: " + bridgePart.getId() + '.', iox);
            }
            if (bridgePart.getState() != BridgeConstants.BridgeState.COMPLETED.getCode()) {
                if (this.vitems != null) {
                    for (Item item : this.vitems.getAllItemsAsArray()) {
                        if (item.onBridge() != this.structure.getWurmId()) continue;
                        item.setOnBridge(-10L);
                        for (VirtualZone vz : this.getWatchers()) {
                            try {
                                if (item.getParentId() != -10L || !vz.isVisible(item, this)) continue;
                                vz.removeItem(item);
                                item.setPosZ(-3000.0f);
                                vz.addItem(item, this, true);
                            }
                            catch (Exception e) {
                                logger.log(Level.WARNING, e.getMessage(), e);
                            }
                        }
                    }
                    for (Item pile : this.vitems.getPileItems()) {
                        if (pile.onBridge() != this.structure.getWurmId()) continue;
                        pile.setOnBridge(-10L);
                        for (VirtualZone vz : this.getWatchers()) {
                            try {
                                if (!vz.isVisible(pile, this)) continue;
                                pile.setPosZ(-3000.0f);
                                vz.removeItem(pile);
                                vz.addItem(pile, this, true);
                            }
                            catch (Exception e) {
                                logger.log(Level.WARNING, e.getMessage(), e);
                            }
                        }
                    }
                }
                if (this.creatures != null) {
                    for (Creature c : this.creatures) {
                        if (c.getBridgeId() != this.structure.getWurmId()) continue;
                        c.setBridgeId(-10L);
                        if (c.isPlayer()) continue;
                        float oldposz = c.getPositionZ();
                        float newPosz = c.calculatePosZ();
                        float diffz = newPosz - oldposz;
                        c.setPositionZ(newPosz);
                        c.moved(0.0f, 0.0f, diffz, 0, 0);
                    }
                }
            }
        }
    }

    public void updateWall(Wall wall) {
        if (this.structure != null) {
            if (this.watchers != null) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        vz.updateWall(this.structure.getWurmId(), wall);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            if (this.structure.isFinalized()) {
                try {
                    wall.save();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save structure wall: " + wall.getId() + '.', iox);
                }
            }
        }
    }

    void linkTo(VirtualZone aZone, boolean aRemove) {
        this.linkStructureToZone(aZone, aRemove);
        this.linkFencesToZone(aZone, aRemove);
        this.linkDoorsToZone(aZone, aRemove);
        this.linkMineDoorsToZone(aZone, aRemove);
        this.linkCreaturesToZone(aZone, aRemove);
        this.linkItemsToZone(aZone, aRemove);
        this.linkPileToZone(aZone, aRemove);
        this.linkEffectsToZone(aZone, aRemove);
        this.linkAreaEffectsToZone(aZone, aRemove);
    }

    private void linkAreaEffectsToZone(VirtualZone aZone, boolean aRemove) {
        if (aZone.getWatcher().isPlayer()) {
            AreaSpellEffect ae = this.getAreaEffect();
            if (ae != null && !aRemove && aZone.covers(this.tilex, this.tiley)) {
                aZone.addAreaSpellEffect(ae, true);
            } else if (ae != null) {
                aZone.removeAreaSpellEffect(ae);
            }
        }
    }

    private void linkDoorsToZone(VirtualZone aZone, boolean aRemove) {
        block4: {
            if (this.doors == null) break block4;
            if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                for (Door door : this.doors) {
                    aZone.addDoor(door);
                }
            } else {
                for (Door door : this.doors) {
                    aZone.removeDoor(door);
                }
            }
        }
    }

    private void linkMineDoorsToZone(VirtualZone aZone, boolean aRemove) {
        block4: {
            if (this.mineDoors == null) break block4;
            if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                for (MineDoorPermission door : this.mineDoors) {
                    aZone.addMineDoor(door);
                }
            } else {
                for (MineDoorPermission door : this.mineDoors) {
                    aZone.removeMineDoor(door);
                }
            }
        }
    }

    private void linkFencesToZone(VirtualZone aZone, boolean aRemove) {
        block4: {
            if (this.fences == null) break block4;
            if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                for (Fence f : this.getFences()) {
                    aZone.addFence(f);
                }
            } else {
                for (Fence f : this.getFences()) {
                    aZone.removeFence(f);
                }
            }
        }
    }

    protected void linkCreaturesToZone(VirtualZone aZone, boolean aRemove) {
        block10: {
            if (this.creatures == null) break block10;
            Creature[] crets = this.getCreatures();
            if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                for (int x = 0; x < crets.length; ++x) {
                    if (crets[x].isDead()) continue;
                    try {
                        aZone.addCreature(crets[x].getWurmId(), false);
                        continue;
                    }
                    catch (NoSuchCreatureException cnf) {
                        this.creatures.remove(crets[x]);
                        logger.log(Level.INFO, crets[x].getName() + "," + cnf.getMessage(), cnf);
                        continue;
                    }
                    catch (NoSuchPlayerException nsp) {
                        this.creatures.remove(crets[x]);
                        logger.log(Level.INFO, crets[x].getName() + "," + nsp.getMessage(), nsp);
                    }
                }
            } else {
                for (int x = 0; x < crets.length; ++x) {
                    try {
                        aZone.deleteCreature(crets[x], true);
                        continue;
                    }
                    catch (NoSuchPlayerException nsp) {
                        logger.log(Level.INFO, crets[x].getName() + "," + nsp.getMessage(), nsp);
                        continue;
                    }
                    catch (NoSuchCreatureException nsc) {
                        logger.log(Level.INFO, crets[x].getName() + "," + nsc.getMessage(), nsc);
                    }
                }
            }
        }
    }

    private void linkPileToZone(VirtualZone aZone, boolean aRemove) {
        if (this.vitems != null) {
            for (Item pileItem : this.vitems.getPileItems()) {
                if (pileItem == null) continue;
                if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                    if (aZone.isVisible(pileItem, this)) {
                        boolean onGroundLevel = true;
                        if (pileItem.getFloorLevel() > 0) {
                            onGroundLevel = false;
                        } else if (this.getFloors(0, 0).length > 0) {
                            onGroundLevel = false;
                        }
                        aZone.addItem(pileItem, this, onGroundLevel);
                        continue;
                    }
                    aZone.removeItem(pileItem);
                    continue;
                }
                aZone.removeItem(pileItem);
            }
        }
    }

    public byte getKingdom() {
        return Zones.getKingdom(this.tilex, this.tiley);
    }

    private void linkItemsToZone(VirtualZone aZone, boolean aRemove) {
        block11: {
            if (this.vitems == null) break block11;
            if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                Item[] lTempItemsLink = this.vitems.getAllItemsAsArray();
                for (int x = 0; x < lTempItemsLink.length; ++x) {
                    Item pileItem = this.vitems.getPileItem(lTempItemsLink[x].getFloorLevel());
                    if (pileItem != null && !lTempItemsLink[x].isDecoration()) continue;
                    if (aZone.isVisible(lTempItemsLink[x], this)) {
                        boolean onGroundLevel = true;
                        if (lTempItemsLink[x].getFloorLevel() > 0) {
                            onGroundLevel = false;
                        } else if (this.getFloors(0, 0).length > 0) {
                            onGroundLevel = false;
                        }
                        if (aZone.addItem(lTempItemsLink[x], this, onGroundLevel)) continue;
                        try {
                            Items.getItem(lTempItemsLink[x].getWurmId());
                            this.removeItem(lTempItemsLink[x], false);
                            Zone z = Zones.getZone(lTempItemsLink[x].getTileX(), lTempItemsLink[x].getTileY(), this.isOnSurface());
                            z.addItem(lTempItemsLink[x]);
                            logger.log(Level.INFO, this.tilex + ", " + this.tiley + " removing " + lTempItemsLink[x].getName() + " with id " + lTempItemsLink[x].getWurmId() + " and added it to " + lTempItemsLink[x].getTileX() + "," + lTempItemsLink[x].getTileY() + " where it belongs.");
                        }
                        catch (NoSuchItemException nsi) {
                            logger.log(Level.INFO, this.tilex + ", " + this.tiley + " removing " + lTempItemsLink[x].getName() + " with id " + lTempItemsLink[x].getWurmId() + " since it doesn't belong here.");
                            this.removeItem(lTempItemsLink[x], false);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, this.tilex + ", " + this.tiley + " removed " + lTempItemsLink[x].getName() + " with id " + lTempItemsLink[x].getWurmId() + ". It is in no valid zone.");
                        }
                        continue;
                    }
                    aZone.removeItem(lTempItemsLink[x]);
                }
            } else {
                for (Item item : this.vitems.getAllItemsAsSet()) {
                    if (item.getSizeZ() >= 500) continue;
                    aZone.removeItem(item);
                }
            }
        }
    }

    private void linkEffectsToZone(VirtualZone aZone, boolean aRemove) {
        block4: {
            if (this.effects == null) break block4;
            if (!aRemove && aZone.covers(this.tilex, this.tiley)) {
                for (Effect effect : this.effects) {
                    aZone.addEffect(effect, false);
                }
            } else {
                for (Effect effect : this.effects) {
                    aZone.removeEffect(effect);
                }
            }
        }
    }

    private void linkStructureToZone(VirtualZone aZone, boolean aRemove) {
        if (this.structure != null) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.INFO, "linkStructureToZone: " + this.structure.getWurmId() + " " + aZone.getId());
            }
            if (!aRemove) {
                aZone.addStructure(this.structure);
            } else {
                aZone.removeStructure(this.structure);
            }
        }
    }

    private boolean checkDeletion() {
        if (!(this.creatures != null && this.creatures.size() != 0 || this.vitems != null && !this.vitems.isEmpty() || this.walls != null && this.walls.size() != 0 || this.structure != null || this.fences != null || this.doors != null && this.doors.size() != 0 || this.effects != null && this.effects.size() != 0 || this.floors != null && this.floors.size() != 0 || this.mineDoors != null && this.mineDoors.size() != 0)) {
            this.zone.removeTile(this);
            return true;
        }
        return false;
    }

    public void changeStructureName(String newName) {
        if (this.watchers != null && this.structure != null) {
            for (VirtualZone vzone : this.watchers) {
                vzone.changeStructureName(this.structure.getWurmId(), newName);
            }
        }
    }

    private final Item createPileItem(Item posItem, boolean starting) {
        try {
            Item pileItem = ItemFactory.createItem(177, 60.0f, null);
            float newXPos = (float)((this.tilex << 2) + 1) + Server.rand.nextFloat() * 2.0f;
            float newYPos = (float)((this.tiley << 2) + 1) + Server.rand.nextFloat() * 2.0f;
            float height = posItem.getPosZ();
            if (Server.getSecondsUptime() > 0) {
                height = Zones.calculatePosZ(newXPos, newYPos, this, this.isOnSurface(), false, posItem.getPosZ(), null, posItem.onBridge());
            }
            pileItem.setPos(newXPos, newYPos, height, posItem.getRotation(), posItem.getBridgeId());
            pileItem.setZoneId(this.zone.getId(), this.surfaced);
            int data = posItem.getTemplateId();
            pileItem.setData1(data);
            byte material = 0;
            boolean multipleMaterials = false;
            if (this.vitems != null) {
                for (Item item : this.vitems.getAllItemsAsArray()) {
                    if (item.isDecoration() || item.getFloorLevel() != pileItem.getFloorLevel()) continue;
                    if (!starting) {
                        this.sendRemoveItem(item, false);
                    }
                    if (!multipleMaterials && item.getMaterial() != material) {
                        if (material == 0) {
                            material = item.getMaterial();
                        } else {
                            material = 0;
                            multipleMaterials = true;
                        }
                    }
                    if (!item.equals(posItem)) {
                        pileItem.insertItem(item, true);
                    }
                    if (data == -1 || item.getTemplateId() == data) continue;
                    pileItem.setData1(-1);
                    data = -1;
                }
            }
            String name = pileItem.getName();
            String modelname = pileItem.getModelName();
            if (data != -1) {
                ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(data);
                String tname = template.getName();
                name = "Pile of " + template.sizeString + tname;
                if (material == 0) {
                    pileItem.setMaterial(template.getMaterial());
                } else {
                    pileItem.setMaterial(material);
                }
                StringBuilder build = new StringBuilder();
                build.append(pileItem.getTemplate().getModelName());
                build.append(tname);
                build.append(".");
                build.append(MaterialUtilities.getMaterialString(material));
                modelname = build.toString().replaceAll(" ", "").trim();
                pileItem.setName(name);
            }
            if (!starting && this.watchers != null) {
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        if (!vz.isVisible(pileItem, this)) continue;
                        boolean onGroundLevel = true;
                        if (pileItem.getFloorLevel() > 0) {
                            onGroundLevel = false;
                        } else if (this.getFloors(0, 0).length > 0) {
                            onGroundLevel = false;
                        }
                        vz.addItem(pileItem, this, onGroundLevel);
                        if (data == -1) continue;
                        vz.renameItem(pileItem, name, modelname);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            return pileItem;
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        return null;
    }

    public void renameItem(Item item) {
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    if (!vz.isVisible(item, this)) continue;
                    vz.renameItem(item, item.getName(), item.getModelName());
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public void putRandomOnTile(Item item) {
        float newPosX = (float)(this.tilex << 2) + 0.5f + Server.rand.nextFloat() * 3.0f;
        float newPosY = (float)(this.tiley << 2) + 0.5f + Server.rand.nextFloat() * 3.0f;
        item.setPosXY(newPosX, newPosY);
    }

    private void destroyPileItem(int floorLevel) {
        if (this.vitems != null) {
            Item pileItem = this.vitems.getPileItem(floorLevel);
            this.destroyPileItem(pileItem);
            if (floorLevel == 0) {
                this.vitems.removePileItem(floorLevel);
            }
        }
    }

    private final void destroyPileItem(Item pileItem) {
        if (pileItem != null) {
            int x;
            try {
                Creature[] iwatchers = pileItem.getWatchers();
                for (x = 0; x < iwatchers.length; ++x) {
                    iwatchers[x].getCommunicator().sendCloseInventoryWindow(pileItem.getWurmId());
                }
            }
            catch (NoSuchCreatureException iwatchers) {
                // empty catch block
            }
            if (this.vitems != null) {
                Item[] itemarra = this.vitems.getAllItemsAsArray();
                for (x = 0; x < itemarra.length; ++x) {
                    if (itemarra[x].isDecoration() || itemarra[x].getFloorLevel() != pileItem.getFloorLevel()) continue;
                    this.vitems.removeItem(itemarra[x]);
                }
                Item p = this.vitems.getPileItem(pileItem.getFloorLevel());
                if (p != null && p != pileItem) {
                    Items.destroyItem(p.getWurmId());
                }
            }
            Items.destroyItem(pileItem.getWurmId());
        }
    }

    public int hashCode() {
        int result = this.tilex + 1;
        return (result += Zones.worldTileSizeY * (this.tiley + 1)) * (this.surfaced ? 1 : 2);
    }

    public static int generateHashCode(int _tilex, int _tiley, boolean _surfaced) {
        int result = _tilex + 1;
        return (result += (_tiley + 1) * Zones.worldTileSizeY) * (_surfaced ? 1 : 2);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && object.getClass() == this.getClass()) {
            VolaTile tile = (VolaTile)object;
            return tile.getTileX() == this.tilex && tile.getTileY() == this.tiley && tile.surfaced == this.surfaced;
        }
        return false;
    }

    public boolean isGuarded() {
        if (this.village != null) {
            return this.village.guards.size() > 0;
        }
        return false;
    }

    public boolean hasFire() {
        if (this.vitems == null) {
            return false;
        }
        return this.vitems.hasFire();
    }

    public Zone getZone() {
        return this.zone;
    }

    boolean isInactive() {
        return this.inactive;
    }

    void setInactive(boolean aInactive) {
        this.inactive = aInactive;
    }

    public boolean isTransition() {
        return this.isTransition;
    }

    public final boolean hasOnePerTileItem(int floorLevel) {
        return this.vitems != null && this.vitems.hasOnePerTileItem(floorLevel);
    }

    public final int getFourPerTileCount(int floorLevel) {
        if (this.vitems == null) {
            return 0;
        }
        return this.vitems.getFourPerTileCount(floorLevel);
    }

    public final Item getOnePerTileItem(int floorLevel) {
        if (this.vitems == null) {
            return null;
        }
        return this.vitems.getOnePerTileItem(floorLevel);
    }

    void lightningStrikeSpell(float baseDamage, Creature caster) {
        if (this.structure == null || !this.structure.isFinished()) {
            if (this.creatures != null) {
                Creature[] crets = this.getCreatures();
                for (int c = 0; c < crets.length; ++c) {
                    TempWound wound = null;
                    if (!crets[c].isPlayer()) {
                        wound = new TempWound(4, 1, baseDamage, crets[c].getWurmId(), 0.0f, 0.0f, true);
                        continue;
                    }
                    if (Servers.localServer.PVPSERVER) {
                        float mod = 1.0f;
                        try {
                            Item armour = crets[c].getArmour((byte)1);
                            if (armour != null) {
                                if (armour.isMetal()) {
                                    mod = 2.0f;
                                } else if (armour.isLeather() || armour.isCloth()) {
                                    mod = 0.5f;
                                }
                                armour.setDamage(armour.getDamage() + armour.getDamageModifier());
                            }
                        }
                        catch (NoArmourException armour) {
                        }
                        catch (NoSpaceException nsp) {
                            logger.log(Level.WARNING, crets[c].getName() + " no armour space on loc " + 1);
                        }
                        crets[c].getCommunicator().sendAlertServerMessage("YOU ARE HIT BY LIGHTNING! OUCH!");
                        if (Servers.isThisATestServer()) {
                            crets[c].getCommunicator().sendNormalServerMessage("Lightning damage mod: " + mod);
                        }
                        crets[c].addWoundOfType(null, (byte)4, 1, false, 1.0f, false, baseDamage * mod, 0.0f, 0.0f, false, true);
                    }
                    crets[c].addAttacker(caster);
                }
            }
            if (Servers.localServer.PVPSERVER && this.vitems != null) {
                Item[] ttempItems = this.vitems.getAllItemsAsArray();
                for (int x = 0; x < ttempItems.length; ++x) {
                    if (ttempItems[x].isIndestructible() || ttempItems[x].isHugeAltar()) continue;
                    ttempItems[x].setDamage(ttempItems[x].getDamage() + ttempItems[x].getDamageModifier() * (ttempItems[x].isLocked() || ttempItems[x].isDecoration() || ttempItems[x].isMetal() || ttempItems[x].isStone() ? 0.1f : 10.0f));
                }
            }
        }
    }

    void flashStrike() {
        if (this.structure == null || !this.structure.isFinished()) {
            if (this.creatures != null) {
                Creature[] crets = this.getCreatures();
                for (int c = 0; c < crets.length; ++c) {
                    TempWound wound = null;
                    if (!crets[c].isPlayer()) {
                        wound = new TempWound(4, 1, 10000.0f, crets[c].getWurmId(), 0.0f, 0.0f, false);
                        continue;
                    }
                    float mod = 1.0f;
                    try {
                        Item armour = crets[c].getArmour((byte)1);
                        if (armour != null) {
                            if (armour.isMetal()) {
                                mod = 2.0f;
                            } else if (armour.isLeather() || armour.isCloth()) {
                                mod = 0.5f;
                            }
                            armour.setDamage(armour.getDamage() + armour.getDamageModifier() * 10.0f);
                        }
                        Item[] lItems = crets[c].getBody().getContainersAndWornItems();
                        for (int x = 0; x < lItems.length; ++x) {
                            if (!lItems[x].isArmour() && !lItems[x].isWeapon() || !lItems[x].isMetal()) continue;
                            mod += 0.1f;
                            lItems[x].setDamage(lItems[x].getDamage() + lItems[x].getDamageModifier() * 10.0f);
                        }
                    }
                    catch (NoArmourException armour) {
                    }
                    catch (NoSpaceException nsp) {
                        logger.log(Level.WARNING, crets[c].getName() + " no armour space on loc " + 1);
                    }
                    crets[c].getCommunicator().sendAlertServerMessage("YOU ARE HIT BY LIGHTNING! OUCH!");
                    crets[c].addWoundOfType(null, (byte)4, 1, false, 1.0f, false, 3000.0f * mod, 0.0f, 0.0f, false, false);
                    HistoryManager.addHistory(crets[c].getName(), "was hit by lightning!");
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer(crets[c].getName() + " was hit by lightning!");
                    }
                    Skills skills = crets[c].getSkills();
                    Skill mindspeed = null;
                    try {
                        mindspeed = skills.getSkill(101);
                        double knowl = mindspeed.getKnowledge();
                        mindspeed.setKnowledge(knowl + (double)(1.0f * mod), false);
                    }
                    catch (NoSuchSkillException nss) {
                        mindspeed = skills.learn(101, 21.0f);
                    }
                    crets[c].getCommunicator().sendNormalServerMessage("A strange dizziness runs through your head, eventually sharpening your senses.");
                }
            }
            if (this.vitems != null) {
                Item[] ttempItems = this.vitems.getAllItemsAsArray();
                for (int x = 0; x < ttempItems.length; ++x) {
                    ttempItems[x].setDamage(ttempItems[x].getDamage() + ttempItems[x].getDamageModifier() * 10.0f);
                }
            }
        }
    }

    public void moveItem(Item item, float newPosX, float newPosY, float newPosZ, float newRot, boolean surf, float oldPosZ) {
        float diffX = newPosX - item.getPosX();
        float diffY = newPosY - item.getPosY();
        if (diffX != 0.0f || diffY != 0.0f) {
            int newTileX = (int)newPosX >> 2;
            int newTileY = (int)newPosY >> 2;
            long newBridgeId = item.getBridgeId();
            long oldBridgeId = item.getBridgeId();
            if (newTileX != this.tilex || newTileY != this.tiley || surf != this.isOnSurface()) {
                VolaTile dt = Zones.getTileOrNull(Zones.safeTileX(newTileX), Zones.safeTileY(newTileY), surf);
                if (item.onBridge() == -10L && dt != null && dt.getStructure() != null && dt.getStructure().isTypeBridge()) {
                    BridgePart bp;
                    if (item.getBridgeId() == -10L) {
                        bp = Zones.getBridgePartFor(newTileX, newTileY, surf);
                        if (bp != null && bp.isFinished() && bp.hasAnExit()) {
                            if (Servers.isThisATestServer() && item.isWagonerWagon()) {
                                Players.getInstance().sendGmMessage(null, "System", "Debug: Wagon " + item.getName() + " bid:" + oldBridgeId + " z:" + item.getPosZ() + " fl:" + item.getFloorLevel() + " bp:" + bp.getStructureId() + " N:" + bp.getNorthExit() + " E:" + bp.getEastExit() + " S:" + bp.getSouthExit() + " W:" + bp.getWestExit() + " @" + item.getTileX() + "," + item.getTileY() + " to " + newTileX + "," + newTileY + "," + surf, false);
                            }
                            if (newTileY < item.getTileY() && bp.getSouthExitFloorLevel() == item.getFloorLevel()) {
                                newBridgeId = bp.getStructureId();
                            } else if (newTileX > item.getTileX() && bp.getWestExitFloorLevel() == item.getFloorLevel()) {
                                newBridgeId = bp.getStructureId();
                            } else if (newTileY > item.getTileY() && bp.getNorthExitFloorLevel() == item.getFloorLevel()) {
                                newBridgeId = bp.getStructureId();
                            } else if (newTileX < item.getTileX() && bp.getEastExitFloorLevel() == item.getFloorLevel()) {
                                newBridgeId = bp.getStructureId();
                            }
                            if (Servers.isThisATestServer() && newBridgeId != oldBridgeId) {
                                Players.getInstance().sendGmMessage(null, "System", "Debug: Wagon " + item.getName() + " obid:" + oldBridgeId + " z:" + item.getPosZ() + " fl:" + item.getFloorLevel() + " nbid:" + newBridgeId + " N:" + bp.getNorthExit() + " E:" + bp.getEastExit() + " S:" + bp.getSouthExit() + " W:" + bp.getWestExit() + " @" + item.getTileX() + "," + item.getTileY() + " to " + newTileX + "," + newTileY + "," + surf, false);
                            }
                        } else {
                            newBridgeId = -10L;
                            item.setOnBridge(-10L);
                            this.sendSetBridgeId(item, -10L);
                            item.calculatePosZ(dt, null);
                        }
                    } else {
                        bp = Zones.getBridgePartFor(newTileX, newTileY, surf);
                        if (bp == null) {
                            newBridgeId = -10L;
                            item.setOnBridge(-10L);
                            this.sendSetBridgeId(item, -10L);
                            item.calculatePosZ(dt, null);
                        }
                    }
                    if (item.onBridge() != newBridgeId) {
                        float nz = Zones.calculatePosZ(newPosX, newPosY, dt, this.isOnSurface(), false, oldPosZ, null, newBridgeId);
                        if (Servers.isThisATestServer() && item.isWagonerWagon()) {
                            Players.getInstance().sendGmMessage(null, "System", "Debug: Wagon " + item.getName() + " moving onto, or off, a bridge from bid:" + oldBridgeId + " z:" + item.getPosZ() + " fl:" + item.getFloorLevel() + " to bp:" + newBridgeId + " newZ:" + nz + " @" + item.getTileX() + "," + item.getTileY() + " to " + newTileX + "," + newTileY + "," + surf, false);
                        }
                        if (Math.abs(oldPosZ - nz) < 10.0f && !item.isBoat()) {
                            item.setOnBridge(newBridgeId);
                            newPosZ = nz;
                            this.sendSetBridgeId(item, newBridgeId);
                        }
                    }
                } else if (item.onBridge() > 0L && (dt == null || dt.getStructure() == null || dt.getStructure().getWurmId() != item.onBridge())) {
                    boolean leave = true;
                    BridgePart bp = Zones.getBridgePartFor(newTileX, newTileY, surf);
                    if (bp != null && bp.isFinished()) {
                        if (bp.getDir() == 0 || bp.getDir() == 4) {
                            if (this.getTileX() != newTileX) {
                                leave = false;
                            }
                        } else if (this.getTileY() != newTileY) {
                            leave = false;
                        }
                    }
                    if (leave) {
                        newBridgeId = -10L;
                        item.setOnBridge(-10L);
                        this.sendSetBridgeId(item, -10L);
                    }
                }
                if (surf != this.isOnSurface()) {
                    item.newLayer = (byte)(this.isOnSurface() ? -1 : 0);
                }
                this.removeItem(item, true);
                if (diffX != 0.0f && diffY != 0.0f) {
                    item.setPosXYZRotation(newPosX, newPosY, newPosZ, newRot);
                } else {
                    item.setRotation(newRot);
                    if (diffX != 0.0f) {
                        item.setPosX(newPosX);
                    }
                    if (diffY != 0.0f) {
                        item.setPosY(newPosY);
                    }
                    item.setPosZ(newPosZ);
                }
                try {
                    Zone _zone = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, surf);
                    _zone.addItem(item, true, surf != this.isOnSurface(), false);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, item.getName() + ", " + nsz.getMessage(), nsz);
                }
                if (surf != this.isOnSurface()) {
                    item.newLayer = (byte)-128;
                }
            } else {
                if (diffX != 0.0f) {
                    item.setTempXPosition(newPosX);
                }
                if (diffY != 0.0f) {
                    item.setTempYPosition(newPosY);
                }
                item.setTempZandRot(newPosZ, newRot);
            }
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    if (vz.isVisible(item, this)) {
                        if (item.getFloorLevel() <= 0 && item.onBridge() <= 0L) {
                            if (Structure.isGroundFloorAtPosition(newPosX, newPosY, item.isOnSurface())) {
                                vz.sendMoveMovingItemAndSetZ(item.getWurmId(), item.getPosX(), item.getPosY(), item.getPosZ(), (int)(newRot * 256.0f / 360.0f));
                                continue;
                            }
                            vz.sendMoveMovingItem(item.getWurmId(), item.getPosX(), item.getPosY(), (int)(newRot * 256.0f / 360.0f));
                            continue;
                        }
                        vz.sendMoveMovingItemAndSetZ(item.getWurmId(), item.getPosX(), item.getPosY(), item.getPosZ(), (int)(newRot * 256.0f / 360.0f));
                        continue;
                    }
                    vz.removeItem(item);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public void destroyEverything() {
        Creature[] crets = this.getCreatures();
        for (int x = 0; x < crets.length; ++x) {
            crets[x].getCommunicator().sendNormalServerMessage("The rock suddenly caves in! You are crushed!");
            crets[x].die(true, "Cave collapse");
        }
        Fence[] fenceArr = this.getFences();
        for (int x = 0; x < fenceArr.length; ++x) {
            if (fenceArr[x] == null) continue;
            fenceArr[x].destroy();
        }
        Wall[] wallArr = this.getWalls();
        for (int x = 0; x < wallArr.length; ++x) {
            if (wallArr[x] == null) continue;
            wallArr[x].destroy();
        }
        Floor[] floorArr = this.getFloors();
        for (int x = 0; x < floorArr.length; ++x) {
            if (floorArr[x] == null) continue;
            floorArr[x].destroy();
        }
        Item[] ttempItems = this.getItems();
        for (int x = 0; x < ttempItems.length; ++x) {
            Items.destroyItem(ttempItems[x].getWurmId());
        }
    }

    protected void sendNewLayerToWatchers(Item item) {
        logger.log(Level.INFO, "Tile at " + this.tilex + ", " + this.tiley + " sending secondary");
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.justSendNewLayer(item);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    protected void newLayer(Item item) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.newLayer(item);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        if (!this.isOnSurface()) {
            for (VirtualZone vz : this.getSurfaceTile().getWatchers()) {
                try {
                    vz.addItem(item, this.getSurfaceTile(), true);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public void newLayer(Creature creature) {
        if (creature.isOnSurface() != this.isOnSurface()) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    vz.newLayer(creature, this.isOnSurface());
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        try {
            Zone newzone = Zones.getZone(this.tilex, this.tiley, creature.getLayer() >= 0);
            VolaTile currentTile = newzone.getOrCreateTile(this.tilex, this.tiley);
            this.removeCreature(creature);
            currentTile.addCreature(creature, 0.0f);
        }
        catch (NoSuchZoneException noSuchZoneException) {
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
            // empty catch block
        }
    }

    public void addLightSource(Item lightSource) {
        if (lightSource.getTemplateId() == 1243) {
            return;
        }
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (lightSource.getColor() != -1) {
                    int lightStrength = Math.max(WurmColor.getColorRed(lightSource.getColor()), WurmColor.getColorGreen(lightSource.getColor()));
                    if ((lightStrength = Math.max(lightStrength, WurmColor.getColorBlue(lightSource.getColor()))) == 0) {
                        lightStrength = 1;
                    }
                    byte r = (byte)(WurmColor.getColorRed(lightSource.getColor()) * 128 / lightStrength);
                    byte g = (byte)(WurmColor.getColorGreen(lightSource.getColor()) * 128 / lightStrength);
                    byte b = (byte)(WurmColor.getColorBlue(lightSource.getColor()) * 128 / lightStrength);
                    vz.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, r, g, b, lightSource.getRadius());
                    continue;
                }
                if (lightSource.isLightBright()) {
                    int lightStrength = (int)(80.0f + lightSource.getCurrentQualityLevel() / 100.0f * 40.0f);
                    vz.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource.getRadius());
                    continue;
                }
                vz.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, (byte)80, (byte)80, (byte)80, lightSource.getRadius());
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void removeLightSource(Item lightSource) {
        if (lightSource.getTemplateId() == 1243) {
            return;
        }
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendRemoveEffect(lightSource.getWurmId(), (byte)0);
                vz.sendRemoveEffect(lightSource.getWurmId(), (byte)4);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void setHasLightSource(Creature creature, @Nullable Item lightSource) {
        if (lightSource != null && lightSource.getTemplateId() == 1243) {
            return;
        }
        for (VirtualZone vz : this.getWatchers()) {
            try {
                byte b;
                byte g;
                byte r;
                if (lightSource == null) {
                    if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                        vz.sendRemoveEffect(-1L, (byte)0);
                        continue;
                    }
                    vz.sendRemoveEffect(creature.getWurmId(), (byte)0);
                    continue;
                }
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    if (lightSource.getColor() != -1) {
                        int lightStrength = Math.max(WurmColor.getColorRed(lightSource.color), WurmColor.getColorGreen(lightSource.color));
                        lightStrength = Math.max(lightStrength, WurmColor.getColorBlue(lightSource.color));
                        r = (byte)(WurmColor.getColorRed(lightSource.color) * 128 / lightStrength);
                        g = (byte)(WurmColor.getColorGreen(lightSource.color) * 128 / lightStrength);
                        b = (byte)(WurmColor.getColorBlue(lightSource.color) * 128 / lightStrength);
                        vz.sendAttachCreatureEffect(null, (byte)0, r, g, b, lightSource.getRadius());
                        continue;
                    }
                    if (lightSource.isLightBright()) {
                        int lightStrength = (int)(80.0f + lightSource.getCurrentQualityLevel() / 100.0f * 40.0f);
                        vz.sendAttachCreatureEffect(null, (byte)0, Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource.getRadius());
                        continue;
                    }
                    vz.sendAttachCreatureEffect(null, (byte)0, Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
                    continue;
                }
                if (lightSource.getColor() != -1) {
                    int lightStrength = Math.max(WurmColor.getColorRed(lightSource.color), WurmColor.getColorGreen(lightSource.color));
                    lightStrength = Math.max(lightStrength, WurmColor.getColorBlue(lightSource.color));
                    r = (byte)(WurmColor.getColorRed(lightSource.color) * 128 / lightStrength);
                    g = (byte)(WurmColor.getColorGreen(lightSource.color) * 128 / lightStrength);
                    b = (byte)(WurmColor.getColorBlue(lightSource.color) * 128 / lightStrength);
                    vz.sendAttachCreatureEffect(creature, (byte)0, r, g, b, lightSource.getRadius());
                    continue;
                }
                if (lightSource.isLightBright()) {
                    int lightStrength = (int)(80.0f + lightSource.getCurrentQualityLevel() / 100.0f * 40.0f);
                    vz.sendAttachCreatureEffect(creature, (byte)0, Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource.getRadius());
                    continue;
                }
                vz.sendAttachCreatureEffect(creature, (byte)0, Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void setHasLightSource(Creature creature, byte colorRed, byte colorGreen, byte colorBlue, byte radius) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendAttachCreatureEffect(null, (byte)0, colorRed, colorGreen, colorBlue, radius);
                    continue;
                }
                vz.sendAttachCreatureEffect(creature, (byte)0, colorRed, colorGreen, colorBlue, radius);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendAttachCreatureEffect(Creature creature, byte effectType, byte data0, byte data1, byte data2, byte radius) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendAttachCreatureEffect(null, effectType, data0, data1, data2, radius);
                    continue;
                }
                vz.sendAttachCreatureEffect(creature, effectType, data0, data1, data2, radius);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendRemoveCreatureEffect(Creature creature, byte effectType) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendRemoveEffect(-1L, effectType);
                    continue;
                }
                vz.sendRemoveEffect(creature.getWurmId(), effectType);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendProjectile(long itemid, byte type, String modelName, String name, byte material, float startX, float startY, float startH, float rot, byte layer, float endX, float endY, float endH, long sourceId, long targetId, float projectedSecondsInAir, float actualSecondsInAir) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == targetId) {
                    if (vz.getWatcher().getWurmId() == sourceId) {
                        vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, -1L, -1L, projectedSecondsInAir, actualSecondsInAir);
                        continue;
                    }
                    vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, sourceId, -1L, projectedSecondsInAir, actualSecondsInAir);
                    continue;
                }
                if (vz.getWatcher().getWurmId() == sourceId) {
                    if (vz.getWatcher().getWurmId() == targetId) {
                        vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, -1L, -1L, projectedSecondsInAir, actualSecondsInAir);
                        continue;
                    }
                    vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, -1L, targetId, projectedSecondsInAir, actualSecondsInAir);
                    continue;
                }
                vz.sendProjectile(itemid, type, modelName, name, material, startX, startY, startH, rot, layer, endX, endY, endH, sourceId, targetId, projectedSecondsInAir, actualSecondsInAir);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendNewProjectile(long itemid, byte type, String modelName, String name, byte material, Vector3f startingPosition, Vector3f startingVelocity, Vector3f endingPosition, float rotation, boolean surface) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendNewProjectile(itemid, type, modelName, name, material, startingPosition, startingVelocity, endingPosition, rotation, surface);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendHorseWear(long creatureId, int itemId, byte material, byte slot, byte aux_data) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendHorseWear(creatureId, itemId, material, slot, aux_data);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendRemoveHorseWear(long creatureId, int itemId, byte slot) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendRemoveHorseWear(creatureId, itemId, slot);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendBoatAttachment(long itemId, int templateId, byte material, byte slot, byte aux) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendBoatAttachment(itemId, templateId, material, slot, aux);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendBoatDetachment(long itemId, int templateId, byte slot) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendBoatDetachment(itemId, templateId, slot);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendWearItem(long creatureId, int itemId, byte bodyPart, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue, byte material, byte rarity) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creatureId) {
                    vz.sendWearItem(-1L, itemId, bodyPart, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue, material, rarity);
                    continue;
                }
                vz.sendWearItem(creatureId, itemId, bodyPart, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue, material, rarity);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendRemoveWearItem(long creatureId, byte bodyPart) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creatureId) {
                    vz.sendRemoveWearItem(-1L, bodyPart);
                    continue;
                }
                vz.sendRemoveWearItem(creatureId, bodyPart);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendWieldItem(long creatureId, byte slot, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creatureId) {
                    vz.sendWieldItem(-1L, slot, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
                    continue;
                }
                vz.sendWieldItem(creatureId, slot, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendUseItem(Creature creature, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendUseItem(null, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
                    continue;
                }
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendUseItem(creature, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendStopUseItem(Creature creature) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendStopUseItem(null);
                    continue;
                }
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendStopUseItem(creature);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendAnimation(Creature creature, String animationName, boolean looping, long target) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendAnimation(null, animationName, looping, target);
                    continue;
                }
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendAnimation(creature, animationName, looping, target);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendStance(Creature creature, byte stance) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creature.getWurmId()) {
                    vz.sendStance(null, stance);
                    continue;
                }
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendStance(creature, stance);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendAnimation(Creature initiator, Item item, String animationName, boolean looping, boolean freeze) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                Creature watcher = vz.getWatcher();
                if (watcher == null || !vz.isVisible(item, this) || initiator != null && !initiator.isVisibleTo(watcher)) continue;
                watcher.getCommunicator().sendAnimation(item.getWurmId(), animationName, looping, freeze);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendCreatureDamage(Creature creature, float damPercent) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendCreatureDamage(creature, damPercent);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendFishingLine(Creature creature, float posX, float posY, byte floatType) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendFishingLine(creature, posX, posY, floatType);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendFishHooked(Creature creature, byte fishType, long fishId) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendFishHooked(creature, fishType, fishId);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendFishingStopped(Creature creature) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendFishingStopped(creature);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendSpearStrike(Creature creature, float posX, float posY) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (!creature.isVisibleTo(vz.getWatcher())) continue;
                vz.sendSpearStrike(creature, posX, posY);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendRepaint(Item item) {
        boolean noPaint = item.color == -1;
        boolean noPaint2 = item.color2 == -1;
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor()), (byte)WurmColor.getColorGreen(item.getColor()), (byte)WurmColor.getColorBlue(item.getColor()), noPaint ? (byte)0 : -1, (byte)0);
                if (!item.supportsSecondryColor()) continue;
                vz.sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor2()), (byte)WurmColor.getColorGreen(item.getColor2()), (byte)WurmColor.getColorBlue(item.getColor2()), noPaint2 ? (byte)0 : -1, (byte)1);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendAttachCreature(long creatureId, long targetId, float offx, float offy, float offz, int seatId) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creatureId) {
                    vz.sendAttachCreature(-1L, targetId, offx, offy, offz, seatId);
                    continue;
                }
                if (vz.getWatcher().getWurmId() == targetId) {
                    vz.sendAttachCreature(creatureId, -1L, offx, offy, offz, seatId);
                    continue;
                }
                vz.sendAttachCreature(creatureId, targetId, offx, offy, offz, seatId);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendAttachCreature(long creatureId, long targetId, float offx, float offy, float offz, int seatId, boolean ignoreOrigin) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == creatureId) {
                    if (ignoreOrigin) continue;
                    vz.sendAttachCreature(-1L, targetId, offx, offy, offz, seatId);
                    continue;
                }
                if (vz.getWatcher().getWurmId() == targetId) {
                    vz.sendAttachCreature(creatureId, -1L, offx, offy, offz, seatId);
                    continue;
                }
                vz.sendAttachCreature(creatureId, targetId, offx, offy, offz, seatId);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public Set<VolaTile> getThisAndSurroundingTiles(int dist) {
        HashSet<VolaTile> surr = new HashSet<VolaTile>();
        VolaTile t = null;
        for (int x = -dist; x <= dist; ++x) {
            for (int y = -dist; y <= dist; ++y) {
                t = Zones.getTileOrNull(Zones.safeTileX(this.tilex + x), Zones.safeTileY(this.tiley + y), this.surfaced);
                if (t == null) continue;
                surr.add(t);
            }
        }
        return surr;
    }

    public void checkDiseaseSpread() {
        int dist = 1;
        if (this.village != null && this.village.getCreatureRatio() < Village.OPTIMUMCRETRATIO) {
            dist = 2;
        }
        Set<VolaTile> set = this.getThisAndSurroundingTiles(dist);
        for (VolaTile t : set) {
            Creature[] crets;
            for (Creature c : crets = t.getCreatures()) {
                if (c.isPlayer() || c.isKingdomGuard() || c.isSpiritGuard() || c.isUnique() || Server.rand.nextInt(100) != 0 || c.getDisease() != 0) continue;
                logger.log(Level.INFO, "Disease spreads to " + c.getName() + " at " + t);
                c.setDisease((byte)1);
            }
        }
    }

    public void checkVisibility(Creature watched, boolean makeInvis) {
        float lStealthMod = makeInvis ? MethodsCreatures.getStealthTerrainModifier(watched, this.tilex, this.tiley, this.surfaced) : 0.0f;
        for (VirtualZone vz : this.getWatchers()) {
            try {
                if (vz.getWatcher().getWurmId() == watched.getWurmId()) continue;
                if (makeInvis && !watched.visibilityCheck(vz.getWatcher(), lStealthMod)) {
                    vz.makeInvisible(watched);
                    continue;
                }
                try {
                    vz.addCreature(watched.getWurmId(), false);
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                }
                catch (NoSuchPlayerException noSuchPlayerException) {}
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void checkCaveOpening() {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.getWatcher().getVisionArea().checkCaves(false);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void setNewFace(Creature c) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.setNewFace(c);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void setNewRarityShader(Creature c) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.setNewRarityShader(c);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendActionControl(Creature c, String actionString, boolean start, int timeLeft) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendActionControl(c.getWurmId(), actionString, start, timeLeft);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendActionControl(Item item, String actionString, boolean start, int timeLeft) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendActionControl(item.getWurmId(), actionString, start, timeLeft);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendRotate(Item item, float rotation) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendRotate(item, rotation);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public int getLayer() {
        if (this.surfaced) {
            return 0;
        }
        return -1;
    }

    public void sendAddTileEffect(AreaSpellEffect effect, boolean loop) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.addAreaSpellEffect(effect, loop);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendAddQuickTileEffect(byte effect, int floorOffset) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.sendAddTileEffect(this.tilex, this.tiley, this.getLayer(), effect, floorOffset, false);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void sendRemoveTileEffect(AreaSpellEffect effect) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.removeAreaSpellEffect(effect);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateFenceState(Fence fence) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.updateFenceDamageState(fence);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateTargetStatus(long targetId, byte type, float status) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.updateTargetStatus(targetId, type, status);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateWallDamageState(Wall wall) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.updateWallDamageState(wall);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateFloorDamageState(Floor floor) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.updateFloorDamageState(floor);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void updateBridgePartDamageState(BridgePart bridgePart) {
        for (VirtualZone vz : this.getWatchers()) {
            try {
                vz.updateBridgePartDamageState(bridgePart);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private AreaSpellEffect getAreaEffect() {
        return AreaSpellEffect.getEffect(this.tilex, this.tiley, this.getLayer());
    }

    public final boolean isInPvPZone() {
        return Zones.isOnPvPServer(this.tilex, this.tiley);
    }

    public final void lightLamps() {
        if (this.vitems != null) {
            for (Item i : this.vitems.getAllItemsAsSet()) {
                if (!i.isStreetLamp() || !i.isPlanted()) continue;
                i.setAuxData((byte)120);
                i.setTemperature((short)10000);
            }
        }
    }

    public String toString() {
        return "VolaTile [X: " + this.tilex + ", Y: " + this.tiley + ", surf=" + this.surfaced + "]";
    }

    public Floor[] getFloors(int startHeightOffset, int endHeightOffset) {
        if (this.floors == null) {
            return emptyFloors;
        }
        ArrayList<Floor> toReturn = new ArrayList<Floor>();
        for (Floor floor : this.floors) {
            if (floor.getHeightOffset() < startHeightOffset || floor.getHeightOffset() > endHeightOffset) continue;
            toReturn.add(floor);
        }
        return toReturn.toArray(new Floor[toReturn.size()]);
    }

    @Nullable
    public Floor getFloor(int floorLevel) {
        if (this.floors != null) {
            for (Floor floor : this.floors) {
                if (floor.getFloorLevel() != floorLevel) continue;
                return floor;
            }
        }
        return null;
    }

    public Floor[] getFloors() {
        if (this.floors == null) {
            return emptyFloors;
        }
        return this.floors.toArray(new Floor[this.floors.size()]);
    }

    public final void addFloor(Floor floor) {
        if (this.floors == null) {
            this.floors = new HashSet<Floor>();
        }
        this.floors.add(floor);
        if (floor.isStair()) {
            Stairs.addStair(this.hashCode(), floor.getFloorLevel());
        }
        if (this.vitems != null) {
            for (ProtoConstants protoConstants : this.vitems.getPileItems()) {
                ((Item)protoConstants).updatePosZ(this);
            }
        }
        if (this.watchers != null) {
            for (ProtoConstants protoConstants : this.getWatchers()) {
                try {
                    ((VirtualZone)protoConstants).updateFloor(this.structure.getWurmId(), floor);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public final void removeFloor(Blocker floor) {
        if (this.floors != null) {
            Floor toRem = null;
            for (Floor fl : this.floors) {
                if (fl.getId() != floor.getId()) continue;
                toRem = fl;
                break;
            }
            if (toRem != null) {
                this.removeFloor(toRem);
            }
        }
    }

    public final void removeFloor(Floor floor) {
        Item pileItem;
        int floorLevel = floor.getFloorLevel();
        if (this.floors != null) {
            this.floors.remove(floor);
            if (floor.isStair()) {
                Stairs.removeStair(this.hashCode(), floorLevel);
            }
            if (this.floors.size() == 0) {
                this.floors = null;
            }
        }
        if (this.structure == null) {
            return;
        }
        if (this.watchers != null) {
            for (VirtualZone vz : this.getWatchers()) {
                try {
                    vz.removeFloor(this.structure.getWurmId(), floor);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        if (floorLevel > 0) {
            this.destroyPileItem(floorLevel);
        } else if (this.vitems != null && (pileItem = this.vitems.getPileItem(floorLevel)) != null) {
            pileItem.updatePosZ(this);
            VirtualZone[] virtualZoneArray = this.getWatchers();
            int n = virtualZoneArray.length;
            for (int vz = 0; vz < n; ++vz) {
                VirtualZone vz2 = virtualZoneArray[vz];
                try {
                    if (!vz2.isVisible(pileItem, this)) continue;
                    vz2.removeItem(pileItem);
                    vz2.addItem(pileItem, this, true);
                    continue;
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        if (this.vitems != null) {
            for (Item item : this.vitems.getAllItemsAsArray()) {
                if (!item.isDecoration() || item.getFloorLevel() != floorLevel) continue;
                item.updatePosZ(this);
                item.updateIfGroundItem();
            }
        }
        if (this.creatures != null) {
            for (Creature c : this.creatures) {
                if (c.getFloorLevel() != floorLevel || c.isPlayer()) continue;
                float oldposz = c.getPositionZ();
                float newPosz = c.calculatePosZ();
                float diffz = newPosz - oldposz;
                c.setPositionZ(newPosz);
                c.moved(0.0f, 0.0f, diffz, 0, 0);
            }
        }
        this.checkDeletion();
    }

    public final void addBridgePart(BridgePart bridgePart) {
        if (this.bridgeParts == null) {
            this.bridgeParts = new HashSet<BridgePart>();
        }
        this.bridgeParts.add(bridgePart);
        if (this.vitems != null) {
            for (ProtoConstants protoConstants : this.vitems.getPileItems()) {
                ((Item)protoConstants).updatePosZ(this);
            }
        }
        if (this.watchers != null) {
            for (ProtoConstants protoConstants : this.getWatchers()) {
                try {
                    ((VirtualZone)protoConstants).updateBridgePart(this.structure.getWurmId(), bridgePart);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    public final void removeBridgePart(BridgePart bridgePart) {
        if (this.bridgeParts != null) {
            this.bridgeParts.remove(bridgePart);
            if (this.bridgeParts.size() == 0) {
                this.bridgeParts = null;
            }
        }
        if (this.structure == null) {
            return;
        }
        if (this.watchers != null) {
            for (ProtoConstants protoConstants : this.getWatchers()) {
                try {
                    ((VirtualZone)protoConstants).removeBridgePart(this.structure.getWurmId(), bridgePart);
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
        if (this.vitems != null) {
            for (Item item : this.vitems.getPileItems()) {
                item.setOnBridge(-10L);
                item.updatePosZ(this);
                for (VirtualZone vz : this.getWatchers()) {
                    try {
                        if (!vz.isVisible(item, this)) continue;
                        vz.removeItem(item);
                        vz.addItem(item, this, true);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
        if (this.vitems != null) {
            for (Item item : this.vitems.getAllItemsAsArray()) {
                if (item.getBridgeId() != this.structure.getWurmId()) continue;
                item.setOnBridge(-10L);
                item.updatePosZ(this);
                item.updateIfGroundItem();
            }
        }
        if (this.creatures != null) {
            for (Creature c : this.creatures) {
                if (c.getBridgeId() != this.structure.getWurmId()) continue;
                c.setBridgeId(-10L);
                if (c.isPlayer()) continue;
                float oldposz = c.getPositionZ();
                float f = c.calculatePosZ();
                float diffz = f - oldposz;
                c.setPositionZ(f);
                c.moved(0.0f, 0.0f, diffz, 0, 0);
            }
        }
        this.checkDeletion();
    }

    public final Floor getTopFloor() {
        if (this.floors != null) {
            Floor toret = null;
            for (Floor floor : this.floors) {
                if (toret != null && floor.getFloorLevel() <= toret.getFloorLevel()) continue;
                toret = floor;
            }
            return toret;
        }
        return null;
    }

    public final Fence getTopFence() {
        if (this.fences != null) {
            Fence toret = null;
            for (Fence f : this.fences.values()) {
                if (toret != null && f.getFloorLevel() <= toret.getFloorLevel()) continue;
                toret = f;
            }
            return toret;
        }
        return null;
    }

    public final Wall getTopWall() {
        if (this.walls != null) {
            Wall toret = null;
            for (Wall f : this.walls) {
                if (toret != null && f.getFloorLevel() <= toret.getFloorLevel() || !f.isFinished()) continue;
                toret = f;
            }
            return toret;
        }
        return null;
    }

    public final boolean isNextTo(VolaTile t) {
        if (t == null || t.getLayer() != this.getLayer()) {
            return false;
        }
        if ((t.getTileX() == this.getTileX() - 1 || t.getTileX() == this.getTileX() + 1) && t.getTileY() == this.getTileY()) {
            return true;
        }
        return t.getTileX() == this.getTileX() && (t.getTileY() == this.getTileY() - 1 || t.getTileY() == this.getTileY() + 1);
    }

    public final void damageFloors(int minFloorLevel, int maxFloorLevel, float addedDamage) {
        Floor[] floorArr;
        for (Floor floor : floorArr = this.getFloors(minFloorLevel * 30, maxFloorLevel * 30)) {
            floor.setDamage(floor.getDamage() + addedDamage);
            if (!(floor.getDamage() >= 100.0f)) continue;
            this.removeFloor(floor);
        }
    }

    public final boolean hasStair(int floorLevel) {
        return Stairs.hasStair(this.hashCode(), floorLevel);
    }

    public Item findHive(int hiveType) {
        if (this.vitems != null) {
            for (Item item : this.vitems.getAllItemsAsArray()) {
                if (item.getTemplateId() != hiveType) continue;
                return item;
            }
        }
        return null;
    }

    public Item findHive(int hiveType, boolean withQueen) {
        if (this.vitems != null) {
            for (Item item : this.vitems.getAllItemsAsArray()) {
                if (item.getTemplateId() != hiveType) continue;
                if (withQueen && item.getAuxData() > 0) {
                    return item;
                }
                if (withQueen || item.getAuxData() != 0) continue;
                return item;
            }
        }
        return null;
    }
}


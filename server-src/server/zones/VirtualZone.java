/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
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
import com.wurmonline.server.bodys.BodyTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureMove;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Npc;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.MovementEntity;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.StructureTypeEnum;
import com.wurmonline.shared.util.StringUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VirtualZone
implements MiscConstants,
CounterTypes,
AttitudeConstants,
ProtoConstants,
TimeConstants {
    private final Creature watcher;
    private Zone[] watchedZones;
    private Set<Item> items;
    private Set<Effect> effects;
    private Set<AreaSpellEffect> areaEffects;
    private Set<Long> finalizedBuildings;
    private Set<Door> doors;
    private Set<Fence> fences;
    private Set<MineDoorPermission> mineDoors;
    private int centerx;
    private int centery;
    private int startX = 0;
    private int endX = 0;
    private int startY = 0;
    private int endY = 0;
    private Set<Structure> structures;
    private final Map<Long, CreatureMove> creatures = new HashMap<Long, CreatureMove>();
    private static final Logger logger = Logger.getLogger(VirtualZone.class.getName());
    private int size;
    private final int id;
    private static int ids = 0;
    private static final int worldTileSizeX = 1 << Constants.meshSize;
    private static final int worldTileSizeY = 1 << Constants.meshSize;
    private final boolean isOnSurface;
    private static final Long[] emptyLongArray = new Long[0];
    private float MOVELIMIT = 0.05f;
    private ArrayList<Structure> nearbyStructureList = new ArrayList();
    private static final Set<VirtualZone> allZones = new HashSet<VirtualZone>();
    private static final int surfaceToSurfaceLocalDistance = 80;
    private static final int caveToSurfaceLocalDistance = Servers.localServer.EPIC ? 20 : 80;
    private static final int surfaceToCaveLocalDistance = 20;
    private static final int caveToCaveLocalDistance = 20;
    public static final int ITEM_INSIDE_RENDERDIST = 15;
    public static final int HOUSEITEMS_RENDERDIST = 5;
    boolean hasReceivedLocalMessageOnChaos = false;

    public VirtualZone(Creature aWatcher, int aStartX, int aStartY, int centerX, int centerY, int aSz, boolean aIsOnSurface) {
        this.isOnSurface = aIsOnSurface;
        this.startX = Math.max(0, aStartX);
        this.startY = Math.max(0, aStartY);
        this.centerx = Math.max(0, centerX);
        this.centery = Math.max(0, centerY);
        this.centerx = Math.min(worldTileSizeX - 1, centerX);
        this.centery = Math.min(worldTileSizeY - 1, centerY);
        this.endX = Math.min(worldTileSizeX - 1, this.centerx + aSz);
        this.endY = Math.min(worldTileSizeY - 1, this.centery + aSz);
        this.id = ids++;
        this.size = aSz;
        this.watcher = aWatcher;
        allZones.add(this);
    }

    public boolean covers(int x, int y) {
        return x >= this.startX && x <= this.endX && y >= this.startY && y <= this.endY;
    }

    public Creature getWatcher() {
        return this.watcher;
    }

    public int getId() {
        return this.id;
    }

    public boolean isOnSurface() {
        return this.isOnSurface;
    }

    public int getStartX() {
        return this.startX;
    }

    public int getStartY() {
        return this.startY;
    }

    public int getEndX() {
        return this.endX;
    }

    public int getEndY() {
        return this.endY;
    }

    public void initialize() {
        if (!this.watcher.isDead()) {
            this.watchedZones = Zones.getZonesCoveredBy(this);
            for (int i = 0; i < this.watchedZones.length; ++i) {
                try {
                    this.watchedZones[i].addWatcher(this.id);
                    continue;
                }
                catch (NoSuchZoneException nze) {
                    logger.log(Level.INFO, nze.getMessage(), nze);
                }
            }
        }
    }

    void addVillage(Village newVillage) {
    }

    void broadCastMessage(Message message) {
        if (!this.watcher.isIgnored(message.getSender().getWurmId())) {
            if (this.watcher.isNpc() && message.getSender().getWurmId() != this.watcher.getWurmId()) {
                ((Npc)this.watcher).getChatManager().addLocalChat(message);
            }
            if (!this.watcher.getCommunicator().isInvulnerable()) {
                this.watcher.getCommunicator().sendMessage(message);
            }
        }
    }

    public void callGuards() {
        boolean found = false;
        if (this.items != null) {
            for (Item i : this.items) {
                GuardTower tower;
                if (!i.isKingdomMarker() || i.getKingdom() != this.watcher.getKingdomId() || (tower = Kingdoms.getTower(i)) == null || !tower.alertGuards(this.watcher)) continue;
                this.watcher.getCommunicator().sendSafeServerMessage("Guards from " + i.getName() + " runs to the rescue!");
                found = true;
            }
        }
        if (!found) {
            this.watcher.getCommunicator().sendSafeServerMessage("No guards seem to respond to your call.");
        }
    }

    public int getCenterX() {
        return this.centerx;
    }

    public int getCenterY() {
        return this.centery;
    }

    int getSize() {
        return this.size;
    }

    public boolean shouldSeeCaves() {
        if (this.watcher.isPlayer()) {
            if (this.watcher.isOnSurface()) {
                for (int x = this.startX + 10; x <= this.endX - 10; ++x) {
                    for (int y = this.startY + 10; y <= this.endY - 10; ++y) {
                        if (Tiles.decodeType(Server.caveMesh.data[x | y << Constants.meshSize]) != Tiles.Tile.TILE_CAVE_EXIT.id) continue;
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public void move(int xChange, int yChange) {
        this.centerx = Math.max(0, this.centerx + xChange);
        this.centery = Math.max(0, this.centery + yChange);
        this.centerx = Math.min(worldTileSizeX - 1, this.centerx);
        this.centery = Math.min(worldTileSizeY - 1, this.centery);
        this.startX = Math.max(0, this.centerx - this.size);
        this.startY = Math.max(0, this.centery - this.size);
        this.endX = Math.min(worldTileSizeX - 1, this.centerx + this.size);
        this.endY = Math.min(worldTileSizeY - 1, this.centery + this.size);
    }

    private final int getSizeX() {
        return this.endX - this.startX;
    }

    private final int getSizeY() {
        return this.endY - this.startY;
    }

    public void stopWatching() {
        Zone[] checkedZones = Zones.getZonesCoveredBy(Math.max(0, this.startX - 100), Math.max(0, this.startY - 100), Math.min(Zones.worldTileSizeX - 1, this.endX + 100), Math.min(Zones.worldTileSizeY - 1, this.endY + 100), this.isOnSurface);
        for (int x = 0; x < checkedZones.length; ++x) {
            try {
                checkedZones[x].removeWatcher(this);
                continue;
            }
            catch (NoSuchZoneException sex) {
                logger.log(Level.WARNING, sex.getMessage(), sex);
            }
        }
        this.watchedZones = null;
        this.pruneDestroy();
        this.size = 0;
        allZones.remove(this);
        if (Server.rand.nextInt(1000) == 0) {
            int cs = Creatures.getInstance().getNumberOfCreatures();
            int ps = Players.getInstance().getNumberOfPlayers();
            if (allZones.size() > ps * 2 + cs * 2 + 100) {
                logger.log(Level.INFO, "Number of virtual zones now: " + allZones.size() + ". Creatures*2=" + cs * 2 + ", players*2=" + ps * 2);
            }
        }
    }

    public Long[] getCreatures() {
        if (this.creatures != null) {
            return this.creatures.keySet().toArray(new Long[this.creatures.size()]);
        }
        return emptyLongArray;
    }

    public boolean containsCreature(Creature creature) {
        if (this.creatures != null) {
            return this.creatures.keySet().contains(creature.getWurmId());
        }
        return false;
    }

    public void refreshAttitudes() {
        for (Long l : this.creatures.keySet()) {
            try {
                Creature cret = Creatures.getInstance().getCreature(l);
                this.sendAttitude(cret);
            }
            catch (NoSuchCreatureException noSuchCreatureException) {}
        }
    }

    private void pruneDestroy() {
        this.removeAllStructures();
        this.finalizedBuildings = null;
        if (this.doors != null) {
            for (Door door : this.doors) {
                door.removeWatcher(this);
            }
            this.doors = null;
        }
        if (this.creatures != null) {
            for (Long l : this.creatures.keySet()) {
                this.watcher.getCommunicator().sendDeleteCreature(l);
            }
            this.creatures.clear();
        }
        if (this.fences != null) {
            for (Fence fence : this.fences) {
                this.watcher.getCommunicator().sendRemoveFence(fence);
            }
            this.fences = null;
        }
        if (this.items != null) {
            for (Item item : this.items) {
                if (item.isMovingItem()) {
                    this.watcher.getCommunicator().sendDeleteMovingItem(item.getWurmId());
                    continue;
                }
                this.watcher.getCommunicator().sendRemoveItem(item);
            }
            this.items = null;
        }
        if (this.effects != null) {
            for (Effect effect : this.effects) {
                this.watcher.getCommunicator().sendRemoveEffect(effect.getOwner());
            }
            this.effects = null;
        }
    }

    void addFence(Fence fence) {
        if (this.fences == null) {
            this.fences = new HashSet<Fence>();
        }
        if (!this.fences.contains(fence) && this.covers(fence.getTileX(), fence.getTileY())) {
            this.fences.add(fence);
            this.watcher.getCommunicator().sendAddFence(fence);
            if (fence.getDamage() >= 60.0f) {
                this.watcher.getCommunicator().sendDamageState(fence.getId(), (byte)fence.getDamage());
            }
        }
    }

    void removeFence(Fence fence) {
        if (this.fences != null) {
            if (this.fences.contains(fence) && this.watcher != null) {
                this.watcher.getCommunicator().sendRemoveFence(fence);
            }
            this.fences.remove(fence);
        }
    }

    public void addMineDoor(MineDoorPermission door) {
        if (this.mineDoors == null) {
            this.mineDoors = new HashSet<MineDoorPermission>();
        }
        if (!this.mineDoors.contains(door)) {
            this.mineDoors.add(door);
            this.watcher.getCommunicator().sendAddMineDoor(door);
        }
    }

    public void removeMineDoor(MineDoorPermission door) {
        if (this.mineDoors != null && this.mineDoors.contains(door)) {
            if (this.watcher != null) {
                this.watcher.getCommunicator().sendRemoveMineDoor(door);
            }
            this.mineDoors.remove(door);
        }
    }

    void renameItem(Item item, String newName, String newModelName) {
        if (this.items != null && this.items.contains(item) && this.watcher != null) {
            this.watcher.getCommunicator().sendRename(item, newName, newModelName);
        }
    }

    void sendAttitude(Creature creature) {
        if (this.creatures != null && this.creatures.keySet().contains(new Long(creature.getWurmId())) && this.watcher instanceof Player) {
            this.watcher.getCommunicator().changeAttitude(creature.getWurmId(), creature.getAttitude(this.watcher));
        }
    }

    void sendUpdateHasTarget(Creature creature) {
        if (this.creatures != null && this.creatures.keySet().contains(new Long(creature.getWurmId())) && this.watcher instanceof Player) {
            if (creature.getTarget() != null) {
                this.watcher.getCommunicator().sendHasTarget(creature.getWurmId(), true);
            } else {
                this.watcher.getCommunicator().sendHasTarget(creature.getWurmId(), false);
            }
        }
    }

    private byte getLayer() {
        if (this.isOnSurface()) {
            return 0;
        }
        return -1;
    }

    boolean addCreature(long creatureId, boolean overRideRange) throws NoSuchCreatureException, NoSuchPlayerException {
        return this.addCreature(creatureId, overRideRange, -10L, 0.0f, 0.0f, 0.0f);
    }

    public final boolean addCreature(long creatureId, boolean overRideRange, long copyId, float offx, float offy, float offz) throws NoSuchCreatureException, NoSuchPlayerException {
        Creature creature = Server.getInstance().getCreature(creatureId);
        if (this.coversCreature(creature) || this.watcher != null && this.watcher.isPlayer() && overRideRange) {
            if (this.watcher != null && this.watcher.getWurmId() != creatureId && creature.isVisibleTo(this.watcher) && (!this.watcher.isPlayer() || this.watcher.hasLink())) {
                Seat s;
                Seat s2;
                Vehicle vehic;
                if (this.creatures.keySet().contains(creatureId) && copyId == -10L) {
                    return false;
                }
                if (!this.creatures.keySet().contains(creatureId)) {
                    if (this.watcher.isPlayer()) {
                        this.creatures.put(creatureId, new CreatureMove());
                        creature.setVisibleToPlayers(true);
                    } else {
                        this.creatures.put(creatureId, null);
                    }
                }
                if (this.watcher.hasLink()) {
                    String suff = "";
                    String pre = "";
                    if (!this.watcher.hasFlag(56)) {
                        if (!creature.hasFlag(24)) {
                            pre = creature.getAbilityTitle();
                        }
                        if (creature.getCultist() != null && !creature.hasFlag(25)) {
                            suff = suff + " " + creature.getCultist().getCultistTitleShort();
                        }
                    }
                    boolean enemy = false;
                    if (creature.getPower() > 0 && !Servers.localServer.testServer) {
                        if (creature.getPower() == 1) {
                            suff = " (HERO)";
                        } else if (creature.getPower() == 2) {
                            suff = " (GM)";
                        } else if (creature.getPower() == 3) {
                            suff = " (GOD)";
                        } else if (creature.getPower() == 4) {
                            suff = " (ARCH)";
                        } else if (creature.getPower() == 5) {
                            suff = " (ADMIN)";
                        }
                    } else {
                        if (creature.isKing()) {
                            suff = suff + " [" + King.getRulerTitle(creature.getSex() == 0, creature.getKingdomId()) + "]";
                        }
                        if (this.watcher.getKingdomId() != 0 && creature.getKingdomId() != 0 && !creature.isFriendlyKingdom(this.watcher.getKingdomId())) {
                            if (creature.getPower() < 2 && this.watcher.getPower() < 2 && creature.isPlayer()) {
                                suff = this.watcher.getCultist() != null && this.watcher.getCultist().getLevel() > 8 && this.watcher.getCultist().getPath() == 3 ? suff + " (ENEMY)" : " (ENEMY)";
                                enemy = true;
                            }
                        } else if (creature.getKingdomTemplateId() != 3 && creature.getReputation() < 0) {
                            suff = suff + " (OUTLAW)";
                            enemy = true;
                        } else if (this.watcher.getCitizenVillage() != null && creature.isPlayer() && this.watcher.getCitizenVillage().isEnemy(creature)) {
                            suff = " (ENEMY)";
                            enemy = true;
                        } else if (creature.hasAttackedUnmotivated()) {
                            suff = " (HUNTED)";
                        } else if (!(this.watcher.isPlayer() && this.watcher.hasFlag(56) || creature.getTitle() == null && (!Features.Feature.COMPOUND_TITLES.isEnabled() || creature.getSecondTitle() == null) || creature.getTitleString().isEmpty())) {
                            suff = suff + " [";
                            suff = suff + creature.getTitleString();
                            suff = suff + "]";
                        }
                        if (creature.isChampion() && creature.getDeity() != null) {
                            suff = suff + " [Champion of " + creature.getDeity().name + "]";
                        }
                    }
                    if (enemy && creature.getPower() < 2 && this.watcher.getPower() < 2 && creature.isPlayer() && (creature.getFightingSkill().getRealKnowledge() > 20.0 || creature.getFaith() > 25.0f) && Servers.isThisAPvpServer()) {
                        this.watcher.addEnemyPresense();
                    }
                    byte layer = (byte)creature.getLayer();
                    if (overRideRange) {
                        layer = this.getLayer();
                    }
                    String hoverText = creature.getHoverText(this.watcher);
                    this.watcher.getCommunicator().sendNewCreature(copyId != -10L ? copyId : creatureId, pre + StringUtilities.raiseFirstLetterOnly(creature.getName()) + suff, hoverText, creature.isUndead() ? creature.getUndeadModelName() : creature.getModelName(), creature.getStatus().getPositionX() + offx, creature.getStatus().getPositionY() + offy, creature.getStatus().getPositionZ() + offz, creature.getStatus().getBridgeId(), creature.getStatus().getRotation(), layer, creature.getBridgeId() <= 0L && !creature.isSubmerged() && (creature.getPower() == 0 || creature.getMovementScheme().onGround) && creature.getFloorLevel() <= 0 && creature.getMovementScheme().getGroundOffset() <= 0.0f, false, creature.getTemplate().getTemplateId() != 119, creature.getKingdomId(), creature.getFace(), creature.getBlood(), creature.isUndead(), copyId != -10L || creature.isNpc(), creature.getStatus().getModType());
                    if (creature.getRarityShader() != 0) {
                        this.setNewRarityShader(creature);
                    }
                    if (copyId != -10L) {
                        this.watcher.getCommunicator().setCreatureDamage(copyId, creature.getStatus().calcDamPercent());
                        if (creature.getRarityShader() != 0) {
                            this.watcher.getCommunicator().updateCreatureRarity(copyId, creature.getRarityShader());
                        }
                    }
                    for (Item item : creature.getBody().getContainersAndWornItems()) {
                        if (item == null) continue;
                        try {
                            byte armorSlot;
                            byte by = armorSlot = item.isArmour() ? BodyTemplate.convertToArmorEquipementSlot((byte)item.getParent().getPlace()) : BodyTemplate.convertToItemEquipementSlot((byte)item.getParent().getPlace());
                            if (creature.isAnimal() && creature.isVehicle()) {
                                this.watcher.getCommunicator().sendHorseWear(creature.getWurmId(), item.getTemplateId(), item.getMaterial(), armorSlot, item.getAuxData());
                                continue;
                            }
                            this.watcher.getCommunicator().sendWearItem(copyId != -10L ? copyId : creature.getWurmId(), item.getTemplateId(), armorSlot, WurmColor.getColorRed(item.getColor()), WurmColor.getColorGreen(item.getColor()), WurmColor.getColorBlue(item.getColor()), WurmColor.getColorRed(item.getColor2()), WurmColor.getColorGreen(item.getColor2()), WurmColor.getColorBlue(item.getColor2()), item.getMaterial(), item.getRarity());
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (creature.hasCustomColor()) {
                        this.sendRepaint(copyId != -10L ? copyId : creatureId, creature.getColorRed(), creature.getColorGreen(), creature.getColorBlue(), (byte)-1, (byte)creature.getPaintMode());
                    }
                    if (creature.hasCustomSize() || creature.isFish()) {
                        this.sendResizeCreature(copyId != -10L ? copyId : creatureId, creature.getSizeModX(), creature.getSizeModY(), creature.getSizeModZ());
                    }
                    if (creature.getBestLightsource() != null) {
                        this.addLightSource(creature, creature.getBestLightsource());
                    } else if (creature.isPlayer()) {
                        ((Player)creature).sendLantern(this);
                    }
                    if (this.watcher.isPlayer()) {
                        this.sendCreatureDamage(creature, creature.getStatus().calcDamPercent());
                    }
                    if (creature.isOnFire()) {
                        this.sendAttachCreatureEffect(creature, (byte)1, creature.getFireRadius(), (byte)-1, (byte)-1, (byte)1);
                    }
                    if (creature.isGhost()) {
                        this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)2, creature.isSpiritGuard() ? (byte)-56 : 100, creature.isSpiritGuard() ? (byte)1 : 1, (byte)0, (byte)1);
                        this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)3, (byte)50, creature.isSpiritGuard() ? (byte)50 : 50, (byte)50, (byte)1);
                    } else if (creature.hasGlow()) {
                        if (creature.hasCustomColor()) {
                            this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)3, (byte)1, (byte)1, (byte)1, (byte)1);
                        } else {
                            this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), (byte)3, creature.getColorRed(), creature.getColorGreen(), creature.getColorGreen(), (byte)1);
                        }
                    }
                    this.sendCreatureItems(creature);
                    if ((creature.isPlayer() || creature.isNpc()) && (!Servers.localServer.PVPSERVER || this.watcher.isPaying())) {
                        this.watcher.getCommunicator().sendAddLocal(creature.getName(), creatureId);
                    }
                }
                if (this.watcher.isTypeFleeing() && (creature.isPlayer() || creature.isAggHuman() || creature.isHuman() || creature.isCarnivore() || creature.isMonster())) {
                    float newDistance = creature.getPos2f().distance(this.watcher.getPos2f());
                    if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                        int baseCounter = (int)(Math.max(1.0f, creature.getBaseCombatRating() - this.watcher.getBaseCombatRating()) * 5.0f);
                        if ((float)baseCounter - newDistance > 0.0f) {
                            this.watcher.setFleeCounter((int)Math.min(60.0f, Math.max(3.0f, (float)baseCounter - newDistance)));
                        }
                    } else {
                        this.watcher.setFleeCounter(60);
                    }
                }
                this.checkIfAttack(creature, creatureId);
                byte att = creature.getAttitude(this.watcher);
                if (att != 0) {
                    this.watcher.getCommunicator().changeAttitude(copyId != -10L ? copyId : creatureId, att);
                }
                if (creature.getVehicle() != -10L && (vehic = Vehicles.getVehicleForId(creature.getVehicle())) != null && (s2 = vehic.getSeatFor(creature.getWurmId())) != null) {
                    this.sendAttachCreature(creatureId, creature.getVehicle(), s2.offx, s2.offy, s2.offz, vehic.getSeatNumberFor(s2));
                }
                if (creature.getHitched() != null && (s = creature.getHitched().getHitchSeatFor(creature.getWurmId())) != null) {
                    this.sendAttachCreature(creatureId, creature.getHitched().wurmid, s.offx, s.offy, s.offz, 0);
                }
                if (creature.getTarget() != null) {
                    this.watcher.getCommunicator().sendHasTarget(creature.getWurmId(), true);
                }
                if (creature.isRidden() && (vehic = Vehicles.getVehicleForId(creatureId)) != null) {
                    Seat[] seats = vehic.getSeats();
                    for (int x = 0; x < seats.length; ++x) {
                        if (!seats[x].isOccupied()) continue;
                        if (!this.creatures.containsKey(seats[x].occupant)) {
                            try {
                                this.addCreature(seats[x].occupant, true);
                            }
                            catch (NoSuchCreatureException nsc) {
                                logger.log(Level.INFO, nsc.getMessage(), nsc);
                            }
                            catch (NoSuchPlayerException nsp) {
                                logger.log(Level.INFO, nsp.getMessage(), nsp);
                            }
                        }
                        this.sendAttachCreature(seats[x].occupant, creatureId, seats[x].offx, seats[x].offy, seats[x].offz, x);
                    }
                }
                return true;
            }
        } else {
            this.removeCreature(creature);
        }
        return false;
    }

    public void sendCreatureItems(Creature creature) {
        if (creature.isPlayer()) {
            Item lTempItem;
            try {
                lTempItem = creature.getEquippedWeapon((byte)37);
                if (lTempItem != null && !lTempItem.isBodyPartAttached()) {
                    this.sendWieldItem(creature.getWurmId() == this.watcher.getWurmId() ? -1L : creature.getWurmId(), (byte)0, lTempItem.getModelName(), lTempItem.getRarity(), WurmColor.getColorRed(lTempItem.getColor()), WurmColor.getColorGreen(lTempItem.getColor()), WurmColor.getColorBlue(lTempItem.getColor()), WurmColor.getColorRed(lTempItem.getColor2()), WurmColor.getColorGreen(lTempItem.getColor2()), WurmColor.getColorBlue(lTempItem.getColor2()));
                }
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, creature.getName() + " could not get equipped weapon for left hand due to " + nsp.getMessage(), nsp);
            }
            try {
                lTempItem = creature.getEquippedWeapon((byte)38);
                if (lTempItem != null && !lTempItem.isBodyPartAttached()) {
                    this.sendWieldItem(creature.getWurmId() == this.watcher.getWurmId() ? -1L : creature.getWurmId(), (byte)1, lTempItem.getModelName(), lTempItem.getRarity(), WurmColor.getColorRed(lTempItem.getColor()), WurmColor.getColorGreen(lTempItem.getColor()), WurmColor.getColorBlue(lTempItem.getColor()), WurmColor.getColorRed(lTempItem.getColor2()), WurmColor.getColorGreen(lTempItem.getColor2()), WurmColor.getColorBlue(lTempItem.getColor2()));
                }
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, creature.getName() + " could not get equipped weapon for right hand due to " + nsp.getMessage(), nsp);
            }
        }
    }

    void newLayer(Creature creature, boolean tileIsSurfaced) {
        if (creature != null && this.watcher.getWurmId() != creature.getWurmId()) {
            if (this.creatures.containsKey(creature.getWurmId())) {
                if (this.watcher.hasLink()) {
                    this.watcher.getCommunicator().sendCreatureChangedLayer(creature.getWurmId(), (byte)creature.getLayer());
                }
                if (this.isOnSurface()) {
                    if (this.watcher.getVisionArea().getUnderGround() != null && this.watcher.getVisionArea().getUnderGround().coversCreature(creature)) {
                        CreatureMove cm = this.creatures.remove(creature.getWurmId());
                        this.addToVisionArea(creature, cm, this.watcher.getVisionArea().getUnderGround());
                    } else {
                        try {
                            this.deleteCreature(creature, true);
                        }
                        catch (NoSuchCreatureException cm) {
                        }
                        catch (NoSuchPlayerException cm) {}
                    }
                } else {
                    CreatureMove cm = this.creatures.remove(creature.getWurmId());
                    this.addToVisionArea(creature, cm, this.watcher.getVisionArea().getSurface());
                }
            }
        } else if (creature != null && this.watcher.getWurmId() == creature.getWurmId() && this.watcher.getVehicle() != -10L && !this.watcher.isVehicleCommander()) {
            this.watcher.getCommunicator().sendCreatureChangedLayer(-1L, (byte)creature.getLayer());
        }
    }

    public void justSendNewLayer(Item item) {
        if (this.watcher.getVehicle() != item.getWurmId()) {
            this.watcher.getCommunicator().sendCreatureChangedLayer(item.getWurmId(), item.newLayer);
        }
    }

    public void addToVisionArea(Creature creature, CreatureMove cm, VirtualZone newzone) {
        newzone.addCreatureToMap(creature, cm);
        if (creature.isRidden()) {
            Set<Long> riders = creature.getRiders();
            for (Long rider : riders) {
                cm = this.creatures.remove((long)rider);
                try {
                    newzone.addCreature(rider, true);
                }
                catch (Exception nex) {
                    logger.log(Level.WARNING, nex.getMessage(), nex);
                }
            }
        }
    }

    void newLayer(Item vehicle) {
        if (vehicle != null && this.items != null && this.items.contains(vehicle)) {
            byte newlayer;
            byte by = newlayer = vehicle.isOnSurface() ? (byte)0 : -1;
            if (vehicle.newLayer != -128) {
                newlayer = vehicle.newLayer;
            }
            if (this.watcher.hasLink()) {
                this.watcher.getCommunicator().sendCreatureChangedLayer(vehicle.getWurmId(), newlayer);
            }
            if (newlayer < 0) {
                if (this.watcher.getVisionArea().getUnderGround() != null && this.watcher.getVisionArea().getUnderGround().covers(vehicle.getTileX(), vehicle.getTileY())) {
                    this.watcher.getVisionArea().getUnderGround().addItem(vehicle, null, true);
                } else {
                    this.removeItem(vehicle);
                }
            } else {
                this.watcher.getVisionArea().getSurface().addItem(vehicle, null, true);
            }
            this.items.remove(vehicle);
        }
    }

    public void addCreatureToMap(Creature creature, CreatureMove cm) {
        if (cm != null) {
            this.creatures.put(creature.getWurmId(), cm);
        }
    }

    public void checkForEnemies() {
        for (Long cid : this.creatures.keySet()) {
            if (this.watcher.target == -10L) {
                try {
                    Creature creature = Server.getInstance().getCreature(cid);
                    this.checkIfAttack(creature, cid);
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                }
                catch (NoSuchPlayerException noSuchPlayerException) {}
                continue;
            }
            return;
        }
    }

    private void checkIfAttack(Creature creature, long creatureId) {
        if (this.watcher.getTemplate().getCreatureAI() != null && this.watcher.getTemplate().getCreatureAI().maybeAttackCreature(this.watcher, this, creature)) {
            return;
        }
        if (creature.isTransferring()) {
            return;
        }
        if (this.watcher.isPlayer()) {
            if (creature.addingAfterTeleport && this.watcher.lastOpponent == creature && creature.isWithinDistanceTo(this.watcher.getPosX(), this.watcher.getPosY(), this.watcher.getPositionZ() + this.watcher.getAltOffZ(), 12.0f)) {
                this.watcher.setTarget(creatureId, false);
            }
            return;
        }
        if (creature.isNpc() && this.watcher.isNpc() && creature.getAttitude(this.watcher) != 2) {
            return;
        }
        if (creature.getLayer() == this.watcher.getLayer() || this.watcher.isKingdomGuard() || this.watcher.isUnique() || this.watcher.isWarGuard()) {
            if (creature.fleeCounter > 0) {
                return;
            }
            if (creature.getVehicle() > -10L && this.watcher.isNoAttackVehicles()) {
                return;
            }
            if (creature.getCultist() != null && (creature.getCultist().hasFearEffect() || creature.getCultist().hasLoveEffect())) {
                return;
            }
            if (!(creature.isWithinDistanceTo(this.watcher.getPosX(), this.watcher.getPosY(), this.watcher.getPositionZ() + this.watcher.getAltOffZ(), this.watcher.isSpiritGuard() || this.watcher.isKingdomGuard() || this.watcher.isWarGuard() || this.watcher.isUnique() ? 30.0f : 12.0f) || VirtualZone.isCreatureTurnedTowardsTarget(creature, this.watcher) || this.watcher.isKingdomGuard() || this.watcher.isWarGuard())) {
                return;
            }
            if (creature.isBridgeBlockingAttack(this.watcher, true)) {
                return;
            }
            if (this.watcher.getAttitude(creature) == 2) {
                if (this.watcher.target == -10L) {
                    if (this.watcher.isKingdomGuard()) {
                        int tpy;
                        int tpx;
                        GuardTower gt;
                        if ((creature.getCurrentTile().getKingdom() == this.watcher.getKingdomId() || this.watcher.getKingdomId() == 0) && (gt = Kingdoms.getTower(this.watcher)) != null && creature.isWithinTileDistanceTo(tpx = gt.getTower().getTileX(), tpy = gt.getTower().getTileY(), (int)gt.getTower().getPosZ(), 50)) {
                            if (creature.isRidden()) {
                                if (Server.rand.nextInt(50) == 0) {
                                    this.watcher.setTarget(creatureId, false);
                                }
                            } else if (creature.isPlayer() || creature.isDominated()) {
                                this.watcher.setTarget(creatureId, false);
                            } else if (this.watcher.getAlertSeconds() > 0 && creature.isAggHuman()) {
                                this.watcher.setTarget(creatureId, false);
                                if (this.watcher.target == creatureId) {
                                    GuardTower.yellHunt(this.watcher, creature, false);
                                }
                            }
                        }
                        return;
                    }
                    if (this.watcher.isWarGuard()) {
                        Item target = Kingdoms.getClosestWarTarget(this.watcher.getTileX(), this.watcher.getTileY(), this.watcher);
                        if (target != null && this.watcher.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 15) && creature.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 5)) {
                            this.watcher.setTarget(creatureId, false);
                            return;
                        }
                    } else if (this.watcher.isDominated()) {
                        this.watcher.setTarget(creatureId, false);
                    }
                    if (!this.watcher.isSpiritGuard()) {
                        if (creature.isRidden() && Server.rand.nextInt(10) == 0) {
                            this.watcher.setTarget(creatureId, false);
                        } else if (creature.isDominated() && Server.rand.nextInt(10) == 0) {
                            this.watcher.setTarget(creatureId, false);
                        }
                    }
                    if (creature instanceof Player && this.watcher.isAggHuman()) {
                        if (!creature.hasLink()) {
                            return;
                        }
                        if (creature.getSpellEffects() != null && creature.getSpellEffects().getSpellEffect((byte)73) != null && !creature.isWithinDistanceTo(this.watcher, 7.0f)) {
                            return;
                        }
                        if (creature.addingAfterTeleport || (float)Server.rand.nextInt(100) <= (float)this.watcher.getAggressivity() * this.watcher.getStatus().getAggTypeModifier()) {
                            this.watcher.setTarget(creatureId, false);
                        }
                    } else if (this.watcher.isAggHuman() && creature.isKingdomGuard()) {
                        if (creature.addingAfterTeleport || creature.getAlertSeconds() > 0 && Server.rand.nextInt((int)Math.max(1.0f, (float)this.watcher.getAggressivity() * this.watcher.getStatus().getAggTypeModifier())) == 0) {
                            this.watcher.setTarget(creatureId, false);
                        }
                    } else if (this.watcher.isAggHuman() && creature.isSpiritGuard()) {
                        if (creature.getCitizenVillage() != null) {
                            if (!creature.getCitizenVillage().isEnemy(this.watcher)) {
                                return;
                            }
                            if (creature.addingAfterTeleport || Server.rand.nextInt((int)Math.max(1.0f, (float)this.watcher.getAggressivity() * this.watcher.getStatus().getAggTypeModifier())) == 0) {
                                this.watcher.setTarget(creatureId, false);
                            }
                        }
                    } else if (this.watcher.isSpiritGuard()) {
                        if (this.watcher.getCitizenVillage() == null) {
                            if (creature.addingAfterTeleport || Server.rand.nextInt(100) <= 80) {
                                this.watcher.setTarget(creatureId, false);
                            }
                        } else if (creature.isRidden()) {
                            if (!this.watcher.getCitizenVillage().isEnemy(creature)) {
                                return;
                            }
                            if (Server.rand.nextInt(100) == 0) {
                                this.watcher.setTarget(creatureId, false);
                            }
                        } else if ((creature.isPlayer() || creature.isBreakFence() || creature.isDominated()) && this.watcher.getCitizenVillage().isWithinAttackPerimeter(creature.getTileX(), creature.getTileY())) {
                            if (!this.watcher.getCitizenVillage().isEnemy(creature)) {
                                return;
                            }
                            this.watcher.setTarget(creatureId, false);
                        }
                    } else {
                        this.watcher.setTarget(creatureId, false);
                    }
                }
            } else if (this.watcher.getTemplate().getLeaderTemplateId() > 0 && this.watcher.leader == null && !this.watcher.isDominated() && creature.getTemplate().getTemplateId() == this.watcher.getTemplate().getLeaderTemplateId() && (!this.watcher.isHerbivore() || !this.watcher.isHungry()) && ((double)creature.getPositionZ() >= -0.71 || creature.isSwimming() && this.watcher.isSwimming()) && creature.mayLeadMoreCreatures()) {
                creature.addFollower(this.watcher, null);
                this.watcher.setLeader(creature);
            }
        }
    }

    void addAreaSpellEffect(AreaSpellEffect effect, boolean loop) {
        if (effect != null) {
            if (this.areaEffects == null) {
                this.areaEffects = new HashSet<AreaSpellEffect>();
            }
            if (!this.areaEffects.contains(effect)) {
                this.areaEffects.add(effect);
                if (this.watcher.hasLink()) {
                    this.watcher.getCommunicator().sendAddAreaSpellEffect(effect.getTilex(), effect.getTiley(), effect.getLayer(), effect.getType(), effect.getFloorLevel(), effect.getHeightOffset(), loop);
                }
            }
        }
    }

    void removeAreaSpellEffect(AreaSpellEffect effect) {
        if (this.areaEffects != null && this.areaEffects.contains(effect)) {
            this.areaEffects.remove(effect);
            if (effect != null && this.watcher.hasLink()) {
                this.watcher.getCommunicator().sendRemoveAreaSpellEffect(effect.getTilex(), effect.getTiley(), effect.getLayer());
            }
        }
    }

    public void addEffect(Effect effect, boolean temp) {
        if (this.effects == null) {
            this.effects = new HashSet<Effect>();
        }
        if (!this.effects.contains(effect) || temp) {
            if (!(temp || WurmId.getType(effect.getOwner()) != 2 && WurmId.getType(effect.getOwner()) != 6)) {
                try {
                    Item effectHolder = Items.getItem(effect.getOwner());
                    if (this.items == null || !this.items.contains(effectHolder)) {
                        return;
                    }
                }
                catch (NoSuchItemException nsi) {
                    return;
                }
            }
            if (!temp) {
                this.effects.add(effect);
            }
            if (this.watcher.hasLink()) {
                this.watcher.getCommunicator().sendAddEffect(effect.getOwner(), effect.getType(), effect.getPosX(), effect.getPosY(), effect.getPosZ(), effect.getLayer(), effect.getEffectString(), effect.getTimeout(), effect.getRotationOffset());
            }
        }
    }

    public void removeEffect(Effect effect) {
        if (this.effects != null && this.effects.contains(effect)) {
            this.effects.remove(effect);
            if (this.watcher.hasLink()) {
                this.watcher.getCommunicator().sendRemoveEffect(effect.getOwner());
            }
        }
    }

    void removeCreature(Creature creature) {
        if (!this.coversCreature(creature) || creature.isLoggedOut()) {
            if (!this.watcher.isSpiritGuard() && creature.getWurmId() == this.watcher.target) {
                this.watcher.setTarget(-10L, true);
            }
            if (this.creatures != null && this.creatures.keySet().contains(creature.getWurmId())) {
                this.creatures.remove(creature.getWurmId());
                this.checkIfEnemyIsPresent(false);
                if (creature.getCurrentTile() == null || !creature.getCurrentTile().isVisibleToPlayers()) {
                    creature.setVisibleToPlayers(false);
                }
                if (this.watcher.hasLink()) {
                    Vehicle vehic;
                    this.watcher.getCommunicator().sendDeleteCreature(creature.getWurmId());
                    if (creature instanceof Player || creature.isNpc()) {
                        this.watcher.getCommunicator().sendRemoveLocal(creature.getName());
                    }
                    if (this.watcher.isPlayer() && (vehic = Vehicles.getVehicleForId(creature.getWurmId())) != null) {
                        Seat[] seats = vehic.getSeats();
                        for (int x = 0; x < seats.length; ++x) {
                            if (!seats[x].isOccupied()) continue;
                            try {
                                Creature occ = Server.getInstance().getCreature(seats[x].occupant);
                                this.watcher.getCommunicator().sendRemoveLocal(occ.getName());
                                continue;
                            }
                            catch (NoSuchCreatureException nsc) {
                                logger.log(Level.WARNING, nsc.getMessage(), nsc);
                                continue;
                            }
                            catch (NoSuchPlayerException nsp) {
                                logger.log(Level.WARNING, nsp.getMessage(), nsp);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean checkIfEnemyIsPresent(boolean checkedFromOtherVirtualZone) {
        if (this.watcher.isPlayer() && !this.watcher.isTeleporting() && this.watcher.hasLink() && this.watcher.getVisionArea() != null && this.watcher.getVisionArea().isInitialized()) {
            boolean foundEnemy = false;
            if (this.creatures != null) {
                Long[] crets = this.getCreatures();
                for (int x = 0; x < crets.length; ++x) {
                    if (WurmId.getType(crets[x]) != 0) continue;
                    try {
                        Creature creature = Server.getInstance().getCreature(crets[x]);
                        if (this.watcher.getKingdomId() != 0 && creature.getKingdomId() != 0 && !this.watcher.isFriendlyKingdom(creature.getKingdomId())) {
                            if (creature.getPower() >= 2 || this.watcher.getPower() >= 2 || !(creature.getFightingSkill().getRealKnowledge() > 20.0)) continue;
                            foundEnemy = true;
                            continue;
                        }
                        if (this.watcher.getCitizenVillage() == null || !this.watcher.getCitizenVillage().isEnemy(creature) || creature.getPower() >= 2 || this.watcher.getPower() >= 2 || !(creature.getFightingSkill().getRealKnowledge() > 20.0)) continue;
                        foundEnemy = true;
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
            if (!foundEnemy) {
                if (!checkedFromOtherVirtualZone) {
                    boolean found = false;
                    if (this.watcher.getVisionArea() != null) {
                        found = this.isOnSurface ? this.watcher.getVisionArea().getUnderGround().checkIfEnemyIsPresent(true) : this.watcher.getVisionArea().getSurface().checkIfEnemyIsPresent(true);
                    }
                    if (!found) {
                        this.watcher.removeEnemyPresense();
                    }
                    return found;
                }
                return false;
            }
        }
        return true;
    }

    void makeInvisible(Creature creature) {
        if (this.creatures != null && this.creatures.keySet().contains(new Long(creature.getWurmId()))) {
            this.creatures.remove(new Long(creature.getWurmId()));
            this.checkIfEnemyIsPresent(false);
            if (this.watcher.hasLink()) {
                this.watcher.getCommunicator().sendDeleteCreature(creature.getWurmId());
            }
            if (creature instanceof Player || creature.isNpc()) {
                this.watcher.getCommunicator().sendRemoveLocal(creature.getName());
            }
        }
    }

    private boolean coversCreature(Creature creature) {
        if (creature.isDead()) {
            return false;
        }
        if (creature == this.watcher) {
            return true;
        }
        if (creature.isPlayer() && Servers.localServer.PVPSERVER) {
            if (this.watcher.isOnSurface() && creature.isOnSurface() && this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), 80)) {
                return true;
            }
            if (!this.watcher.isOnSurface() && creature.isOnSurface() && this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), caveToSurfaceLocalDistance)) {
                return true;
            }
            if (this.watcher.isOnSurface() && !creature.isOnSurface() && this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), 20)) {
                return true;
            }
            return !this.watcher.isOnSurface() && !creature.isOnSurface() && this.watcher.isWithinDistanceTo(creature.getTileX(), creature.getTileY(), 20);
        }
        return this.covers(creature.getTileX(), creature.getTileY());
    }

    public void moveAllCreatures() {
        Map.Entry[] arr = this.creatures.entrySet().toArray(new Map.Entry[this.creatures.size()]);
        for (int x = 0; x < arr.length; ++x) {
            if (((CreatureMove)arr[x].getValue()).timestamp == 0L) continue;
            try {
                Creature creature = Server.getInstance().getCreature((Long)arr[x].getKey());
                if (!VirtualZone.isMovingSameLevel(creature) || Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                    this.watcher.getCommunicator().sendMoveCreatureAndSetZ((Long)arr[x].getKey(), creature.getPosX(), creature.getPosY(), creature.getPositionZ(), ((CreatureMove)arr[x].getValue()).rotation);
                } else {
                    this.watcher.getCommunicator().sendMoveCreature((Long)arr[x].getKey(), creature.getPosX(), creature.getPosY(), ((CreatureMove)arr[x].getValue()).rotation, creature.isMoving());
                }
                this.clearCreatureMove(creature, (CreatureMove)arr[x].getValue());
                continue;
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
                continue;
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    private static final boolean isMovingSameLevel(Creature creature) {
        return creature.getBridgeId() <= 0L && (creature.isPlayer() && creature.getMovementScheme().onGround || !creature.isSubmerged()) && creature.getFloorLevel() <= 0 && creature.getMovementScheme().getGroundOffset() == 0.0f;
    }

    boolean creatureMoved(long creatureId, float diffX, float diffY, float diffZ, int diffTileX, int diffTileY) throws NoSuchCreatureException, NoSuchPlayerException {
        if (this.watcher == null || this.watcher.isPlayer() && !this.watcher.hasLink()) {
            return true;
        }
        Creature creature = Server.getInstance().getCreature(creatureId);
        if (this.watcher.equals(creature)) {
            if (this.watcher.getPower() > 2 && this.watcher.loggerCreature1 != -10L) {
                this.watcher.getCommunicator().sendAck(this.watcher.getPosX(), this.watcher.getPosY());
            }
            if (this.watcher.isPlayer() && (diffTileX != 0 || diffTileY != 0)) {
                this.getStructuresWithinDistance(5);
            }
            return false;
        }
        if (!this.coversCreature(creature)) {
            this.removeCreature(creature);
            return false;
        }
        if (!creature.isVisibleTo(this.watcher)) {
            return false;
        }
        if (!this.addCreature(creatureId, false)) {
            if (!this.watcher.isPlayer()) {
                this.watcher.creatureMoved(creature, diffX, diffY, diffZ);
            } else if (this.watcher.hasLink()) {
                Set<MovementEntity> illusions;
                if (creature.isPlayer() && (illusions = Creature.getIllusionsFor(creatureId)) != null) {
                    for (MovementEntity e : illusions) {
                        this.watcher.getCommunicator().sendMoveCreature(e.getWurmid(), creature.getPosX() + e.getMovePosition().diffX, creature.getPosY() + e.getMovePosition().diffY, (int)(creature.getStatus().getRotation() * 256.0f / 360.0f), true);
                        if (!e.shouldExpire()) continue;
                        this.watcher.getCommunicator().sendDeleteCreature(e.getWurmid());
                    }
                }
                CreatureMove cmove = this.creatures.get(new Long(creatureId));
                boolean moveSameLevel = VirtualZone.isMovingSameLevel(creature);
                if (diffX != 0.0f || diffY != 0.0f || diffZ != 0.0f) {
                    this.MOVELIMIT = Math.max(0.05f, Math.min(0.7f, (float)Creature.rangeTo(creature, this.watcher) / 100.0f));
                    if (Math.abs(cmove.diffX + diffX) > this.MOVELIMIT || Math.abs(cmove.diffY + diffY) > this.MOVELIMIT || Math.abs(cmove.diffZ + diffZ) > this.MOVELIMIT) {
                        if (!moveSameLevel || Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                            this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, creature.getPosX(), creature.getPosY(), creature.getPositionZ(), cmove.rotation);
                        } else {
                            this.watcher.getCommunicator().sendMoveCreature(creatureId, creature.getPosX(), creature.getPosY(), cmove.rotation, creature.isMoving());
                        }
                        cmove.resetXYZ();
                        cmove.timestamp = System.currentTimeMillis();
                        cmove.rotation = (int)(creature.getStatus().getRotation() * 256.0f / 360.0f);
                    } else if (creature.getAttitude(this.watcher) == 2 || Math.abs(diffZ) > 0.3f) {
                        if (creature.isSubmerged() && creature.getPositionZ() < 0.0f) {
                            this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, creature.getPosX(), creature.getPosY(), creature.getPositionZ(), (int)(creature.getStatus().getRotation() * 256.0f / 360.0f));
                            this.clearCreatureMove(creature, cmove);
                        } else {
                            if (!moveSameLevel || Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                                this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, creature.getPosX(), creature.getPosY(), creature.getPositionZ(), (int)(creature.getStatus().getRotation() * 256.0f / 360.0f));
                            } else {
                                this.watcher.getCommunicator().sendMoveCreature(creatureId, creature.getPosX(), creature.getPosY(), (int)(creature.getStatus().getRotation() * 256.0f / 360.0f), creature.isMoving());
                            }
                            this.clearCreatureMove(creature, cmove);
                        }
                    } else {
                        cmove.timestamp = System.currentTimeMillis();
                        cmove.diffX += diffX;
                        cmove.diffY += diffY;
                        cmove.diffZ += diffZ;
                        cmove.rotation = (int)(creature.getStatus().getRotation() * 256.0f / 360.0f);
                    }
                } else if (creature.getAttitude(this.watcher) == 2) {
                    if (!moveSameLevel || Structure.isGroundFloorAtPosition(creature.getPosX(), creature.getPosY(), creature.isOnSurface())) {
                        this.watcher.getCommunicator().sendMoveCreatureAndSetZ(creatureId, creature.getPosX(), creature.getPosY(), creature.getPositionZ(), (int)(creature.getStatus().getRotation() * 256.0f / 360.0f));
                    } else {
                        this.watcher.getCommunicator().sendMoveCreature(creatureId, creature.getPosX(), creature.getPosY(), (int)(creature.getStatus().getRotation() * 256.0f / 360.0f), creature.isMoving());
                    }
                    this.clearCreatureMove(creature, cmove);
                } else {
                    cmove.timestamp = System.currentTimeMillis();
                    cmove.rotation = (int)(creature.getStatus().getRotation() * 256.0f / 360.0f);
                }
            }
        }
        if (diffTileX != 0 || diffTileY != 0) {
            this.checkIfAttack(creature, creatureId);
            if (creature.getVehicle() != -10L) {
                try {
                    Item itemVehicle = Items.getItem(creature.getVehicle());
                    Vehicle vehicle = Vehicles.getVehicle(itemVehicle);
                    for (Seat seat : vehicle.getSeats()) {
                        PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(seat.getOccupant());
                        if (oInfo == null) continue;
                        try {
                            Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                            if (!oPlayer.hasLink()) continue;
                            this.checkIfAttack(oPlayer, oPlayer.getWurmId());
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {
                            // empty catch block
                        }
                    }
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
            }
        }
        return false;
    }

    public void clearCreatureMove(Creature creature, CreatureMove cmove) {
        cmove.timestamp = 0L;
        cmove.diffX = 0.0f;
        cmove.diffY = 0.0f;
        cmove.diffZ = 0.0f;
    }

    public void clearMovementForCreature(long creatureId) {
        CreatureMove cmove = this.creatures.get(creatureId);
        if (cmove != null) {
            cmove.timestamp = 0L;
            cmove.diffX = 0.0f;
            cmove.diffY = 0.0f;
            cmove.diffZ = 0.0f;
        }
    }

    public void linkVisionArea() {
        this.checkNewZone();
        if (this.size > 0) {
            for (int x = 0; x < this.watchedZones.length; ++x) {
                this.watchedZones[x].linkTo(this, this.startX, this.startY, this.endX, this.endY);
            }
        } else {
            logger.log(Level.WARNING, "Size is 0 for creature " + this.watcher.getName());
        }
    }

    private void checkNewZone() {
        if (this.size <= 0) {
            return;
        }
        Zone[] checkedZones = Zones.getZonesCoveredBy(this);
        LinkedList<Zone> newZones = new LinkedList<Zone>(Arrays.asList(checkedZones));
        if (this.watchedZones == null) {
            this.watchedZones = new Zone[0];
        }
        LinkedList<Zone> oldZones = new LinkedList<Zone>(Arrays.asList(this.watchedZones));
        Iterator it = newZones.listIterator();
        while (it.hasNext()) {
            Zone newZ = (Zone)it.next();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("new zone is " + newZ.getStartX() + "," + newZ.getEndX() + "," + newZ.getStartY() + "," + newZ.getEndY());
            }
            ListIterator it2 = oldZones.listIterator();
            while (it2.hasNext()) {
                Zone oldZ = (Zone)it2.next();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("old zone is " + oldZ.getStartX() + "," + oldZ.getEndX() + "," + oldZ.getStartY() + "," + oldZ.getEndY());
                }
                if (!newZ.equals(oldZ)) continue;
                it.remove();
                it2.remove();
            }
        }
        for (Zone toAdd : newZones) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Adding zone " + this.getId() + " as watcher to " + toAdd.getId());
            }
            try {
                toAdd.addWatcher(this.id);
            }
            catch (NoSuchZoneException nze) {
                logger.log(Level.INFO, nze.getMessage(), nze);
            }
        }
        for (Zone toRemove : oldZones) {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Removing zone " + this.getId() + " as watcher to " + toRemove.getId());
                }
                toRemove.removeWatcher(this);
            }
            catch (NoSuchZoneException sex) {
                logger.log(Level.WARNING, "Zone with id does not exist!", sex);
            }
        }
        this.watchedZones = checkedZones;
    }

    void deleteCreature(Creature creature, boolean removeAsTarget) throws NoSuchCreatureException, NoSuchPlayerException {
        if (this.watcher == null) {
            logger.log(Level.WARNING, "Watcher is null when linking: " + creature.getName(), new Exception());
            return;
        }
        if (removeAsTarget) {
            boolean removeTarget = true;
            if (creature.isTeleporting()) {
                removeTarget = Math.abs(this.watcher.getPosX() - creature.getTeleportX()) > 20.0f || Math.abs(this.watcher.getPosY() - creature.getTeleportY()) > 20.0f;
            } else if (this.watcher.isTeleporting()) {
                boolean bl = removeTarget = Math.abs(creature.getPosX() - this.watcher.getTeleportX()) > 20.0f || Math.abs(creature.getPosY() - this.watcher.getTeleportY()) > 20.0f;
            }
            if (creature.isDead()) {
                removeTarget = true;
            }
            if (this.watcher.isDead()) {
                removeTarget = true;
            }
            if (removeTarget) {
                if (!(creature.getWurmId() != this.watcher.target || this.watcher.getVisionArea() != null && this.watcher.getVisionArea().getSurface() != null && this.watcher.getVisionArea().getSurface().containsCreature(creature))) {
                    this.watcher.setTarget(-10L, true);
                }
                if (!(creature.target != this.watcher.getWurmId() || creature.getVisionArea() != null && creature.getVisionArea().getSurface() != null && creature.getVisionArea().getSurface().containsCreature(this.watcher))) {
                    creature.setTarget(-10L, true);
                }
            }
        }
        if (this.creatures != null && this.creatures.keySet().contains(new Long(creature.getWurmId()))) {
            Vehicle vehic;
            this.creatures.remove(new Long(creature.getWurmId()));
            if (removeAsTarget) {
                this.checkIfEnemyIsPresent(false);
            }
            if (this.watcher.hasLink()) {
                if (this.watcher != null && !this.watcher.equals(creature)) {
                    this.watcher.getCommunicator().sendDeleteCreature(creature.getWurmId());
                }
                if (creature instanceof Player || creature.isNpc()) {
                    this.watcher.getCommunicator().sendRemoveLocal(creature.getName());
                }
            }
            if (creature.getVehicle() != -10L && WurmId.getType(creature.getVehicle()) == 2 && (vehic = Vehicles.getVehicleForId(creature.getVehicle())) != null) {
                boolean shouldRemove = true;
                Seat[] seats = vehic.getSeats();
                for (int x = 0; x < seats.length; ++x) {
                    if (!seats[x].isOccupied() || !this.creatures.containsKey(seats[x].occupant)) continue;
                    shouldRemove = false;
                    break;
                }
                if (shouldRemove) {
                    try {
                        Item vc = Items.getItem(creature.getVehicle());
                        VolaTile tile = Zones.getOrCreateTile(vc.getTileX(), vc.getTileY(), vc.isOnSurface());
                        if (!this.isVisible(vc, tile)) {
                            if (vc.isMovingItem()) {
                                this.watcher.getCommunicator().sendDeleteMovingItem(vc.getWurmId());
                            } else {
                                if (vc.isWarTarget()) {
                                    this.watcher.getCommunicator().sendRemoveEffect(vc.getWurmId());
                                }
                                this.watcher.getCommunicator().sendRemoveItem(vc);
                            }
                        }
                    }
                    catch (NoSuchItemException noSuchItemException) {
                        // empty catch block
                    }
                }
            }
        }
    }

    public final void pollVisibleVehicles() {
        if (this.items != null) {
            Iterator<Item> it = this.items.iterator();
            while (it.hasNext()) {
                Item i = it.next();
                if (!i.isVehicle()) continue;
                if (i.deleted) {
                    it.remove();
                    this.sendRemoveItem(i);
                    continue;
                }
                VolaTile t = Zones.getTileOrNull(i.getTileX(), i.getTileY(), i.isOnSurface());
                if (this.isVisible(i, t)) continue;
                it.remove();
                this.sendRemoveItem(i);
            }
        }
    }

    public boolean isVisible(Item item, VolaTile tile) {
        Structure itemStructure;
        if (item.getTemplateId() == 344) {
            return this.watcher.getPower() > 0;
        }
        if (tile == null) {
            return false;
        }
        int distancex = Math.abs(tile.getTileX() - this.centerx);
        int distancey = Math.abs(tile.getTileY() - this.centery);
        int distance = Math.max(distancex, distancey);
        if (item.isVehicle()) {
            Vehicle vehic = Vehicles.getVehicleForId(item.getWurmId());
            if (vehic != null) {
                Seat[] seats = vehic.getSeats();
                for (int x = 0; x < seats.length; ++x) {
                    if (!seats[x].isOccupied() || !this.creatures.containsKey(seats[x].occupant)) continue;
                    return true;
                }
                if (this.watcher.isPlayer()) {
                    return Math.max(Math.abs(this.centerx - item.getTileX()), Math.abs(this.centery - item.getTileY())) <= this.size;
                }
            }
        } else if (this.watcher.isPlayer() && item.getSizeZ() >= 500) {
            return true;
        }
        if (item.isLight()) {
            return true;
        }
        if (distance > this.size) {
            return false;
        }
        int isize = item.getSizeZ();
        int mod = 3;
        if (isize >= 300) {
            mod = 128;
        } else if (isize >= 200) {
            mod = 64;
        } else if (isize >= 100) {
            mod = 32;
        } else if (isize >= 50) {
            mod = 16;
        } else if (isize >= 10) {
            mod = 8;
        }
        if (item.isBrazier()) {
            return distance <= Math.max(mod, 16);
        }
        if (item.isCarpet()) {
            int n = mod = distance <= Math.max(mod, 10) ? Math.max(mod, 10) : mod;
        }
        if ((itemStructure = tile.getStructure()) != null && itemStructure.isTypeHouse()) {
            if (this.watcher.isPlayer() && this.nearbyStructureList != null && this.nearbyStructureList.contains(itemStructure)) {
                return distance <= mod;
            }
            if (distance > 15) {
                return false;
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(item.getName() + " distance=" + distance + ", size=" + item.getSizeZ() / 10);
        }
        return distance <= mod;
    }

    private final ArrayList<Structure> getStructuresWithinDistance(int tileDistance) {
        if (this.nearbyStructureList == null) {
            this.nearbyStructureList = new ArrayList();
        }
        this.nearbyStructureList.clear();
        for (int i = this.watcher.getTileX() - tileDistance; i < this.watcher.getTileX() + tileDistance; ++i) {
            for (int j = this.watcher.getTileY() - tileDistance; j < this.watcher.getTileY() + tileDistance; ++j) {
                VolaTile tile = Zones.getTileOrNull(Zones.safeTileX(i), Zones.safeTileY(j), this.watcher.isOnSurface());
                if (tile == null || tile.getStructure() == null || !tile.getStructure().isTypeHouse() || this.nearbyStructureList.contains(tile.getStructure())) continue;
                this.nearbyStructureList.add(tile.getStructure());
            }
        }
        return this.nearbyStructureList;
    }

    private final Structure getStructureAtWatcherPosition() {
        try {
            Zone zone = Zones.getZone(this.watcher.getTileX(), this.watcher.getTileY(), this.watcher.isOnSurface());
            VolaTile tile = zone.getOrCreateTile(this.watcher.getTileX(), this.watcher.getTileY());
            return tile.getStructure();
        }
        catch (NoSuchZoneException e) {
            logger.log(Level.WARNING, "Unable to find the zone at the watchers tile position.", e);
            return null;
        }
    }

    void sendMoveMovingItem(long aId, float x, float y, int rot) {
        if (this.watcher.hasLink()) {
            this.watcher.getCommunicator().sendMoveMovingItem(aId, x, y, rot);
        }
    }

    void sendMoveMovingItemAndSetZ(long aId, float x, float y, float z, int rot) {
        if (this.watcher.hasLink()) {
            this.watcher.getCommunicator().sendMoveMovingItemAndSetZ(aId, x, y, z, rot);
        }
    }

    boolean addItem(Item item, VolaTile tile, boolean onGroundLevel) {
        return this.addItem(item, tile, -10L, onGroundLevel);
    }

    boolean addItem(Item item, VolaTile tile, long creatureId, boolean onGroundLevel) {
        if (this.items == null) {
            this.items = new HashSet<Item>();
        }
        if (item.isMovingItem() || this.covers(item.getTileX(), item.getTileY())) {
            if (!this.items.contains(item)) {
                this.items.add(item);
                if (this.watcher.hasLink()) {
                    Optional<Item> extraItem;
                    if (item.isMovingItem()) {
                        byte newlayer;
                        byte by = newlayer = item.isOnSurface() ? (byte)0 : -1;
                        if (item.newLayer != -128) {
                            newlayer = item.newLayer;
                        }
                        this.watcher.getCommunicator().sendNewMovingItem(item.getWurmId(), item.getName(), item.getModelName(), item.getPosX(), item.getPosY(), item.getPosZ(), item.onBridge(), item.getRotation(), newlayer, item.getFloorLevel() <= 0, item.isFloating() && item.getCurrentQualityLevel() >= 10.0f, true, item.getMaterial(), item.getRarity());
                        Vehicle vehic = Vehicles.getVehicleForId(item.getWurmId());
                        if (vehic != null) {
                            Seat[] seats = vehic.getSeats();
                            for (int x = 0; x < seats.length; ++x) {
                                Creature occ;
                                if (!seats[x].isOccupied() || this.watcher.getWurmId() == seats[x].occupant || (occ = Server.getInstance().getCreatureOrNull(seats[x].occupant)) == null || occ.equals(this.watcher) || !occ.isVisibleTo(this.watcher)) continue;
                                if ((!Servers.localServer.PVPSERVER || this.watcher.isPaying()) && occ.isPlayer()) {
                                    this.watcher.getCommunicator().sendAddLocal(occ.getName(), seats[x].occupant);
                                }
                                if (!this.creatures.containsKey(seats[x].occupant)) {
                                    if (this.watcher.isPlayer()) {
                                        this.creatures.put(creatureId, new CreatureMove());
                                    } else {
                                        this.creatures.put(creatureId, null);
                                    }
                                }
                                this.sendAttachCreature(seats[x].occupant, item.getWurmId(), seats[x].offx, seats[x].offy, seats[x].offz, x);
                            }
                            Seat[] hitched = vehic.hitched;
                            for (int x = 0; x < hitched.length; ++x) {
                                if (hitched[x].isOccupied() && this.creatures.containsKey(hitched[x].occupant) && this.watcher.getWurmId() != hitched[x].occupant) {
                                    this.sendAttachCreature(hitched[x].occupant, item.getWurmId(), hitched[x].offx, hitched[x].offy, hitched[x].offz, x);
                                    continue;
                                }
                                if (this.watcher.getWurmId() != hitched[x].occupant) continue;
                                logger.log(Level.WARNING, "This should be unused code.");
                                this.sendAttachCreature(-1L, item.getWurmId(), hitched[x].offx, hitched[x].offy, hitched[x].offz, x);
                            }
                        }
                    } else {
                        this.watcher.getCommunicator().sendItem(item, creatureId, onGroundLevel);
                        if (item.isWarTarget()) {
                            this.watcher.getCommunicator().sendAddEffect(item.getWurmId(), (short)24, item.getPosX(), item.getPosY(), item.getData1(), (byte)(item.isOnSurface() ? 0 : -1));
                            this.watcher.getCommunicator().sendTargetStatus(item.getWurmId(), (byte)item.getData2(), item.getData1());
                        }
                        if (item.getTemplate().hasViewableSubItems() && item.getItemCount() > 0) {
                            boolean normalContainer = item.getTemplate().isContainerWithSubItems();
                            for (Item i : item.getItems()) {
                                if (normalContainer && !i.isPlacedOnParent()) continue;
                                this.watcher.getCommunicator().sendItem(i, -10L, false);
                                if (i.isLight() && i.isOnFire()) {
                                    this.addLightSource(i);
                                }
                                if (i.getEffects().length > 0) {
                                    for (Effect e : i.getEffects()) {
                                        this.addEffect(e, false);
                                    }
                                }
                                if (i.getColor() != -1) {
                                    this.sendRepaint(i.getWurmId(), (byte)WurmColor.getColorRed(i.getColor()), (byte)WurmColor.getColorGreen(i.getColor()), (byte)WurmColor.getColorBlue(i.getColor()), (byte)-1, (byte)0);
                                }
                                if (i.getColor2() == -1) continue;
                                this.sendRepaint(i.getWurmId(), (byte)WurmColor.getColorRed(i.getColor2()), (byte)WurmColor.getColorGreen(i.getColor2()), (byte)WurmColor.getColorBlue(i.getColor2()), (byte)-1, (byte)1);
                            }
                        }
                    }
                    if (item.isLight() && item.isOnFire()) {
                        this.addLightSource(item);
                        if (item.getEffects().length > 0) {
                            Effect[] effs = item.getEffects();
                            for (int x = 0; x < effs.length; ++x) {
                                this.addEffect(effs[x], false);
                            }
                        }
                    }
                    if (!item.isLight() || item.getTemplateId() == 1396) {
                        if (item.getColor() != -1) {
                            this.sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor()), (byte)WurmColor.getColorGreen(item.getColor()), (byte)WurmColor.getColorBlue(item.getColor()), (byte)-1, (byte)0);
                        }
                        if (item.supportsSecondryColor() && item.getColor2() != -1) {
                            this.sendRepaint(item.getWurmId(), (byte)WurmColor.getColorRed(item.getColor2()), (byte)WurmColor.getColorGreen(item.getColor2()), (byte)WurmColor.getColorBlue(item.getColor2()), (byte)-1, (byte)1);
                        }
                    }
                    if (item.getExtra() != -1L && (item.getTemplateId() == 491 || item.getTemplateId() == 490) && (extraItem = Items.getItemOptional(item.getExtra())).isPresent()) {
                        this.sendBoatAttachment(item.getWurmId(), extraItem.get().getTemplateId(), extraItem.get().getMaterial(), (byte)1, extraItem.get().getAuxData());
                    }
                }
                if (item.isHugeAltar()) {
                    if (this.watcher.getMusicPlayer() != null && this.watcher.getMusicPlayer().isItOkToPlaySong(true)) {
                        if (item.getTemplateId() == 327) {
                            this.watcher.getMusicPlayer().checkMUSIC_WHITELIGHT_SND();
                        } else {
                            this.watcher.getMusicPlayer().checkMUSIC_BLACKLIGHT_SND();
                        }
                    }
                } else if (item.getTemplateId() == 518 && this.watcher.getMusicPlayer() != null && this.watcher.getMusicPlayer().isItOkToPlaySong(true)) {
                    this.watcher.getMusicPlayer().checkMUSIC_COLOSSUS_SND();
                }
            }
        } else {
            int tilex = item.getTileX();
            int tiley = item.getTileY();
            try {
                Item i = item.getParent();
            }
            catch (NoSuchItemException i) {
                // empty catch block
            }
            if (item.getContainerSizeZ() < 500 && !item.isVehicle()) {
                VolaTile vtile = Zones.getTileOrNull(tilex, tiley, this.isOnSurface);
                if (vtile != null) {
                    if (!vtile.equals(tile)) {
                        return false;
                    }
                    if (!this.covers(tile.getTileX(), tile.getTileY())) {
                        vtile.removeWatcher(this);
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendRemoveItem(Item item) {
        if (this.watcher.hasLink()) {
            if (item.isMovingItem()) {
                Vehicle vehic;
                this.watcher.getCommunicator().sendDeleteMovingItem(item.getWurmId());
                if (this.watcher.isPlayer() && (vehic = Vehicles.getVehicleForId(item.getWurmId())) != null) {
                    Seat[] seats = vehic.getSeats();
                    for (int x = 0; x < seats.length; ++x) {
                        if (!seats[x].isOccupied()) continue;
                        try {
                            Creature occ = Server.getInstance().getCreature(seats[x].occupant);
                            if (occ == null || occ.equals(this.watcher)) continue;
                            if (this.creatures != null) {
                                this.creatures.remove(seats[x].occupant);
                            }
                            this.watcher.getCommunicator().sendRemoveLocal(occ.getName());
                            continue;
                        }
                        catch (NoSuchCreatureException nsc) {
                            logger.log(Level.WARNING, nsc.getMessage(), nsc);
                            continue;
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {
                            // empty catch block
                        }
                    }
                }
            } else {
                if (item.isWarTarget()) {
                    this.watcher.getCommunicator().sendRemoveEffect(item.getWurmId());
                }
                if (item.getTemplate().hasViewableSubItems() && item.getItemCount() > 0) {
                    boolean normalContainer = item.getTemplate().isContainerWithSubItems();
                    for (Item i : item.getAllItems(false)) {
                        if (normalContainer && !i.isPlacedOnParent()) continue;
                        this.watcher.getCommunicator().sendRemoveItem(i);
                    }
                }
                this.watcher.getCommunicator().sendRemoveItem(item);
            }
        }
    }

    void removeItem(Item item) {
        if (this.items != null && this.items.contains(item)) {
            this.items.remove(item);
            this.sendRemoveItem(item);
        }
    }

    void removeStructure(Structure structure) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(this.watcher.getName() + " removing structure " + structure);
        }
        if (this.structures != null && this.structures.contains(structure)) {
            boolean stillHere = false;
            VolaTile[] tiles = structure.getStructureTiles();
            for (int x = 0; x < tiles.length; ++x) {
                if (!this.covers(tiles[x].getTileX(), tiles[x].getTileY())) continue;
                stillHere = true;
                break;
            }
            if (!stillHere) {
                this.structures.remove(structure);
                this.watcher.getCommunicator().sendRemoveStructure(structure.getWurmId());
            }
        }
    }

    void deleteStructure(Structure structure) {
        if (this.structures != null && this.structures.contains(structure)) {
            this.structures.remove(structure);
            this.watcher.getCommunicator().sendRemoveStructure(structure.getWurmId());
        }
    }

    private void removeAllStructures() {
        if (this.structures != null) {
            for (Structure structure : this.structures) {
                this.watcher.getCommunicator().sendRemoveStructure(structure.getWurmId());
            }
        }
        this.structures = null;
    }

    void sendStructureWalls(Structure structure) {
        Wall[] wallArr = structure.getWalls();
        for (int x = 0; x < wallArr.length; ++x) {
            this.updateWall(structure.getWurmId(), wallArr[x]);
        }
    }

    void addStructure(Structure structure) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(this.watcher.getName() + " adding structure " + structure);
        }
        if (this.structures == null) {
            this.structures = new HashSet<Structure>();
        }
        if (!this.structures.contains(structure)) {
            BridgePart[] bridgePartArr;
            Floor[] floorArr;
            this.structures.add(structure);
            this.watcher.getCommunicator().sendAddStructure(structure.getName(), (short)structure.getCenterX(), (short)structure.getCenterY(), structure.getWurmId(), structure.getStructureType(), structure.getLayer());
            if (structure.isTypeHouse()) {
                this.watcher.getCommunicator().sendMultipleBuildMarkers(structure.getWurmId(), structure.getStructureTiles(), structure.getLayer());
                this.sendStructureWalls(structure);
            }
            if ((floorArr = structure.getFloors()) != null) {
                for (int x = 0; x < floorArr.length; ++x) {
                    this.updateFloor(structure.getWurmId(), floorArr[x]);
                }
            }
            if ((bridgePartArr = structure.getBridgeParts()) != null) {
                for (int x = 0; x < bridgePartArr.length; ++x) {
                    this.updateBridgePart(structure.getWurmId(), bridgePartArr[x]);
                }
            }
        }
    }

    void addBuildMarker(Structure structure, int tilex, int tiley) {
        if (this.structures == null) {
            this.structures = new HashSet<Structure>();
        }
        if (!this.structures.contains(structure)) {
            this.addStructure(structure);
        } else {
            this.watcher.getCommunicator().sendSingleBuildMarker(structure.getWurmId(), tilex, tiley, this.getLayer());
        }
    }

    void removeBuildMarker(Structure structure, int tilex, int tiley) {
        if (this.structures != null && this.structures.contains(structure)) {
            boolean stillHere = false;
            VolaTile[] tiles = structure.getStructureTiles();
            for (int x = 0; x < tiles.length; ++x) {
                if (!this.covers(tiles[x].getTileX(), tiles[x].getTileY())) continue;
                stillHere = true;
                break;
            }
            if (stillHere) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(this.watcher.getName() + " removing build marker for structure " + structure.getWurmId());
                }
                this.watcher.getCommunicator().sendSingleBuildMarker(structure.getWurmId(), tilex, tiley, this.getLayer());
            } else {
                this.removeStructure(structure);
            }
        } else {
            logger.log(Level.INFO, "Hmm tried to remove buildmarker from a zone that didn't contain it.");
        }
    }

    void finalizeBuildPlan(long oldStructureId, long newStructureId) {
        if (this.finalizedBuildings == null) {
            this.finalizedBuildings = new HashSet<Long>();
        }
        if (!this.finalizedBuildings.contains(new Long(newStructureId))) {
            try {
                Structure structure = Structures.getStructure(newStructureId);
                if (structure.isTypeHouse()) {
                    this.watcher.getCommunicator().sendRemoveStructure(oldStructureId);
                }
                this.watcher.getCommunicator().sendAddStructure(structure.getName(), (short)structure.getCenterX(), (short)structure.getCenterY(), structure.getWurmId(), structure.getStructureType(), structure.getLayer());
                if (structure.isTypeHouse()) {
                    this.watcher.getCommunicator().sendMultipleBuildMarkers(structure.getWurmId(), structure.getStructureTiles(), structure.getLayer());
                }
                Wall[] wallArr = structure.getWalls();
                for (int x = 0; x < wallArr.length; ++x) {
                    if (wallArr[x].getType() == StructureTypeEnum.PLAN) continue;
                    this.watcher.getCommunicator().sendAddWall(structure.getWurmId(), wallArr[x]);
                    if (!(wallArr[x].getDamage() >= 60.0f)) continue;
                    this.watcher.getCommunicator().sendWallDamageState(structure.getWurmId(), wallArr[x].getId(), (byte)wallArr[x].getDamage());
                }
            }
            catch (NoSuchStructureException nss) {
                logger.log(Level.WARNING, "The new building doesn't exist.", nss);
            }
            this.finalizedBuildings.add(new Long(newStructureId));
        }
    }

    void addDoor(Door door) {
        if (this.doors == null) {
            this.doors = new HashSet<Door>();
        }
        if (!this.doors.contains(door)) {
            this.doors.add(door);
            if (door.isOpen()) {
                if (door instanceof FenceGate) {
                    this.openFence(((FenceGate)door).getFence(), false, true);
                } else {
                    this.openDoor(door);
                }
            }
        }
    }

    void removeDoor(Door door) {
        if (this.doors != null) {
            this.doors.remove(door);
        }
    }

    public void openDoor(Door door) {
        this.watcher.getCommunicator().sendOpenDoor(door);
    }

    public void closeDoor(Door door) {
        this.watcher.getCommunicator().sendCloseDoor(door);
    }

    public void openFence(Fence fence, boolean passable, boolean changedPassable) {
        this.watcher.getCommunicator().sendOpenFence(fence, passable, changedPassable);
    }

    public void closeFence(Fence fence, boolean passable, boolean changedPassable) {
        this.watcher.getCommunicator().sendCloseFence(fence, passable, changedPassable);
    }

    public void openMineDoor(MineDoorPermission door) {
        this.watcher.getCommunicator().sendOpenMineDoor(door);
    }

    public void closeMineDoor(MineDoorPermission door) {
        this.watcher.getCommunicator().sendCloseMineDoor(door);
    }

    void updateFloor(long structureId, Floor floor) {
        this.watcher.getCommunicator().sendAddFloor(structureId, floor);
        if (floor.getDamage() >= 60.0f) {
            this.watcher.getCommunicator().sendWallDamageState(floor.getStructureId(), floor.getId(), (byte)floor.getDamage());
        }
    }

    void updateBridgePart(long structureId, BridgePart bridgePart) {
        this.watcher.getCommunicator().sendAddBridgePart(structureId, bridgePart);
        if (bridgePart.getDamage() >= 60.0f) {
            this.watcher.getCommunicator().sendWallDamageState(bridgePart.getStructureId(), bridgePart.getId(), (byte)bridgePart.getDamage());
        }
    }

    void updateWall(long structureId, Wall wall) {
        this.watcher.getCommunicator().sendAddWall(structureId, wall);
        if (wall.getDamage() >= 60.0f) {
            this.watcher.getCommunicator().sendWallDamageState(wall.getStructureId(), wall.getId(), (byte)wall.getDamage());
        }
    }

    void removeWall(long structureId, Wall wall) {
        this.watcher.getCommunicator().sendRemoveWall(structureId, wall);
    }

    void removeFloor(long structureId, Floor floor) {
        this.watcher.getCommunicator().sendRemoveFloor(structureId, floor);
    }

    void removeBridgePart(long structureId, BridgePart bridgePart) {
        this.watcher.getCommunicator().sendRemoveBridgePart(structureId, bridgePart);
    }

    void changeStructureName(long structureId, String newName) {
        this.watcher.getCommunicator().sendChangeStructureName(structureId, newName);
    }

    void playSound(Sound sound) {
        this.watcher.getCommunicator().sendSound(sound);
    }

    public static boolean isCreatureTurnedTowardsTarget(Creature target, Creature performer) {
        return VirtualZone.isCreatureTurnedTowardsTarget(target, performer, 180.0f, false);
    }

    public static boolean isCreatureShieldedVersusTarget(Creature target, Creature performer) {
        if (performer.isWithinDistanceTo(target, 1.5f)) {
            if (Servers.localServer.testServer && target.isPlayer() && performer.isPlayer()) {
                target.getCommunicator().sendNormalServerMessage(performer.getName() + " is so close he auto blocks you.");
            }
            return true;
        }
        return VirtualZone.isCreatureTurnedTowardsTarget(target, performer, 135.0f, true);
    }

    public static boolean isCreatureTurnedTowardsItem(Item target, Creature performer, float angle) {
        double newrot = Math.atan2(target.getPosY() - (float)((int)performer.getStatus().getPositionY()), target.getPosX() - (float)((int)performer.getStatus().getPositionX()));
        float attAngle = (float)(newrot * 57.29577951308232) + 90.0f;
        attAngle = Creature.normalizeAngle(attAngle);
        float prot = Creature.normalizeAngle(performer.getStatus().getRotation() - attAngle);
        return !(prot > angle / 2.0f) || !(prot < 360.0f - angle / 2.0f);
    }

    public static boolean isItemTurnedTowardsCreature(Creature target, Item performer, float angle) {
        double newrot = Math.atan2(target.getPosY() - (float)((int)performer.getPosY()), target.getPosX() - (float)((int)performer.getPosX()));
        float attAngle = (float)(newrot * 57.29577951308232) - 90.0f;
        attAngle = Creature.normalizeAngle(attAngle);
        float prot = Creature.normalizeAngle(performer.getRotation() - attAngle);
        return !(prot > angle / 2.0f) || !(prot < 360.0f - angle / 2.0f);
    }

    public static boolean isCreatureTurnedTowardsTarget(Creature target, Creature performer, float angle, boolean leftWinged) {
        boolean log = leftWinged && Servers.localServer.testServer && target.isPlayer() && performer.isPlayer();
        double newrot = Math.atan2(target.getPosY() - (float)((int)performer.getStatus().getPositionY()), target.getPosX() - (float)((int)performer.getStatus().getPositionX()));
        float attAngle = (float)(newrot * 57.29577951308232) + 90.0f;
        attAngle = Creature.normalizeAngle(attAngle);
        float crot = Creature.normalizeAngle(performer.getStatus().getRotation());
        float prot = Creature.normalizeAngle(attAngle - crot);
        float rightAngle = angle / 2.0f;
        float leftAngle = 360.0f - angle / 2.0f;
        if (leftWinged) {
            leftAngle -= 45.0f;
            rightAngle -= 45.0f;
        }
        leftAngle = Creature.normalizeAngle(leftAngle);
        rightAngle = Creature.normalizeAngle(rightAngle);
        if (log) {
            target.getCommunicator().sendNormalServerMessage(attAngle + ", " + crot + ", prot=" + prot);
        }
        if (prot > rightAngle && prot < leftAngle) {
            if (log) {
                target.getCommunicator().sendNormalServerMessage("1.5 " + performer.getName() + " will not block you. Angle to me= " + attAngle + ", creature angle=" + crot + ", difference=" + prot + ". Max left=" + leftAngle + ", right=" + rightAngle);
            }
            return false;
        }
        if (log) {
            target.getCommunicator().sendNormalServerMessage("1.5 " + performer.getName() + " will block you. Angle to me= " + attAngle + ", creature angle=" + crot + ", difference=" + prot + ". Max left=" + leftAngle + ", right=" + rightAngle);
        }
        return true;
    }

    private void addLightSource(Item lightSource) {
        int colorToUse = lightSource.getColor();
        if (lightSource.getTemplateId() == 1396) {
            colorToUse = lightSource.getColor2();
        }
        if (colorToUse != -1) {
            int lightStrength = Math.max(WurmColor.getColorRed(colorToUse), WurmColor.getColorGreen(colorToUse));
            lightStrength = Math.max(1, Math.max(lightStrength, WurmColor.getColorBlue(colorToUse)));
            byte r = (byte)(WurmColor.getColorRed(colorToUse) * 128 / lightStrength);
            byte g = (byte)(WurmColor.getColorGreen(colorToUse) * 128 / lightStrength);
            byte b = (byte)(WurmColor.getColorBlue(colorToUse) * 128 / lightStrength);
            this.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, r, g, b, lightSource.getRadius());
        } else if (lightSource.isLightBright()) {
            int lightStrength = (int)(80.0f + lightSource.getCurrentQualityLevel() / 100.0f * 40.0f);
            this.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource.getRadius());
        } else {
            this.sendAttachItemEffect(lightSource.getWurmId(), (byte)4, Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
        }
    }

    private void addLightSource(Creature creature, Item lightSource) {
        if (lightSource.getColor() != -1) {
            int lightStrength = Math.max(WurmColor.getColorRed(lightSource.getColor()), WurmColor.getColorGreen(lightSource.getColor()));
            lightStrength = Math.max(1, Math.max(lightStrength, WurmColor.getColorBlue(lightSource.getColor())));
            byte r = (byte)(WurmColor.getColorRed(lightSource.getColor()) * 128 / lightStrength);
            byte g = (byte)(WurmColor.getColorGreen(lightSource.getColor()) * 128 / lightStrength);
            byte b = (byte)(WurmColor.getColorBlue(lightSource.getColor()) * 128 / lightStrength);
            this.sendAttachCreatureEffect(creature, (byte)0, r, g, b, lightSource.getRadius());
        } else if (lightSource.isLightBright()) {
            int lightStrength = (int)(80.0f + lightSource.getCurrentQualityLevel() / 100.0f * 40.0f);
            this.sendAttachCreatureEffect(creature, (byte)0, Item.getRLight(lightStrength), Item.getGLight(lightStrength), Item.getBLight(lightStrength), lightSource.getRadius());
        } else {
            this.sendAttachCreatureEffect(creature, (byte)0, Item.getRLight(80), Item.getGLight(80), Item.getBLight(80), lightSource.getRadius());
        }
    }

    public void sendAttachCreatureEffect(Creature creature, byte effectType, byte data0, byte data1, byte data2, byte radius) {
        if (creature == null) {
            this.watcher.getCommunicator().sendAttachEffect(-1L, effectType, data0, data1, data2, radius);
        } else if (this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendAttachEffect(creature.getWurmId(), effectType, data0, data1, data2, radius);
        }
    }

    void sendAttachItemEffect(long targetId, byte effectType, byte data0, byte data1, byte data2, byte radius) {
        this.watcher.getCommunicator().sendAttachEffect(targetId, effectType, data0, data1, data2, radius);
    }

    void sendRemoveEffect(long targetId, byte effectType) {
        if (WurmId.getType(targetId) == 2) {
            this.watcher.getCommunicator().sendRemoveEffect(targetId, effectType);
        } else if (targetId == -1L || this.creatures.containsKey(targetId)) {
            this.watcher.getCommunicator().sendRemoveEffect(targetId, effectType);
        }
    }

    void sendHorseWear(long creatureId, int itemId, byte material, byte slot, byte aux_data) {
        if (this.creatures.containsKey(creatureId)) {
            this.watcher.getCommunicator().sendHorseWear(creatureId, itemId, material, slot, aux_data);
        }
    }

    void sendRemoveHorseWear(long creatureId, int itemId, byte slot) {
        if (this.creatures.containsKey(creatureId)) {
            this.watcher.getCommunicator().sendRemoveHorseWear(creatureId, itemId, slot);
        }
    }

    void sendBoatAttachment(long itemId, int templateId, byte material, byte slot, byte aux) {
        this.watcher.getCommunicator().sendHorseWear(itemId, templateId, material, slot, aux);
    }

    void sendBoatDetachment(long itemId, int templateId, byte slot) {
        this.watcher.getCommunicator().sendRemoveHorseWear(itemId, templateId, slot);
    }

    void sendWearItem(long creatureId, int itemId, byte bodyPart, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue, byte material, byte rarity) {
        if (creatureId == -1L || this.creatures.containsKey(creatureId)) {
            this.watcher.getCommunicator().sendWearItem(creatureId, itemId, bodyPart, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue, material, rarity);
        }
    }

    void sendRemoveWearItem(long creatureId, byte bodyPart) {
        if (creatureId == -1L || this.creatures.containsKey(creatureId)) {
            this.watcher.getCommunicator().sendRemoveWearItem(creatureId, bodyPart);
        }
    }

    void sendWieldItem(long creatureId, byte slot, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue) {
        if (creatureId == -1L || this.creatures.containsKey(creatureId)) {
            this.watcher.getCommunicator().sendWieldItem(creatureId, slot, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        }
    }

    void sendUseItem(Creature creature, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue) {
        if (creature == null) {
            this.watcher.getCommunicator().sendUseItem(-1L, modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        } else if (!creature.isTeleporting() && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendUseItem(creature.getWurmId(), modelname, rarity, colorRed, colorGreen, colorBlue, secondaryColorRed, secondaryColorGreen, secondaryColorBlue);
        }
    }

    void sendStopUseItem(Creature creature) {
        if (creature == null) {
            this.watcher.getCommunicator().sendStopUseItem(-1L);
        } else if (!creature.isTeleporting() && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendStopUseItem(creature.getWurmId());
        }
    }

    public void sendRepaint(long wurmid, byte red, byte green, byte blue, byte alpha, byte paintType) {
        this.watcher.getCommunicator().sendRepaint(wurmid, red, green, blue, alpha, paintType);
    }

    private void sendResizeCreature(long wurmid, byte xscaleMod, byte yscaleMod, byte zscaleMod) {
        this.watcher.getCommunicator().sendResize(wurmid, xscaleMod, yscaleMod, zscaleMod);
    }

    void sendAnimation(Creature creature, String animationName, boolean looping, long target) {
        if (creature == null) {
            if (target <= 0L) {
                this.watcher.getCommunicator().sendAnimation(-1L, animationName, looping, animationName.equals("die"));
            } else {
                this.watcher.getCommunicator().sendAnimation(-1L, animationName, looping, false, target);
            }
        } else if (this.creatures.containsKey(creature.getWurmId())) {
            if (target <= 0L) {
                this.watcher.getCommunicator().sendAnimation(creature.getWurmId(), animationName, looping, animationName.equals("die"));
            } else {
                this.watcher.getCommunicator().sendAnimation(creature.getWurmId(), animationName, looping, false, target);
            }
        }
    }

    void sendStance(Creature creature, byte stance) {
        if (creature == null) {
            this.watcher.getCommunicator().sendStance(-1L, stance);
        } else if (this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendStance(creature.getWurmId(), stance);
        }
    }

    void sendCreatureDamage(Creature creature, float currentDamage) {
        if (creature != null && this.watcher != null && !creature.equals(this.watcher) && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().setCreatureDamage(creature.getWurmId(), currentDamage);
        }
    }

    void sendFishingLine(Creature creature, float posX, float posY, byte floatType) {
        if (creature != null && this.watcher != null && !creature.equals(this.watcher) && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendFishCasted(creature.getWurmId(), posX, posY, floatType);
        }
    }

    void sendFishHooked(Creature creature, byte fishType, long fishId) {
        if (creature != null && this.watcher != null && !creature.equals(this.watcher) && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendFishBite(fishType, fishId, creature.getWurmId());
        }
    }

    void sendFishingStopped(Creature creature) {
        if (creature != null && this.watcher != null && !creature.equals(this.watcher) && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendFishSubCommand((byte)15, creature.getWurmId());
        }
    }

    void sendSpearStrike(Creature creature, float posX, float posY) {
        if (creature != null && this.watcher != null && !creature.equals(this.watcher) && this.creatures.containsKey(creature.getWurmId())) {
            this.watcher.getCommunicator().sendSpearStrike(creature.getWurmId(), posX, posY);
        }
    }

    void sendAttachCreature(long creatureId, long targetId, float offx, float offy, float offz, int seatId) {
        Item item2;
        boolean send = true;
        if (targetId != -1L) {
            if (WurmId.getType(targetId) == 1 || WurmId.getType(targetId) == 0) {
                if (!this.creatures.containsKey(targetId)) {
                    try {
                        this.addCreature(targetId, true);
                        send = false;
                    }
                    catch (NoSuchCreatureException noSuchCreatureException) {
                    }
                    catch (NoSuchPlayerException noSuchPlayerException) {}
                }
            } else if (WurmId.getType(targetId) == 2 || WurmId.getType(targetId) == 19 || WurmId.getType(targetId) == 20) {
                try {
                    item2 = Items.getItem(targetId);
                    if (this.items == null || !this.items.contains(item2)) {
                        if (this.watcher.getVisionArea() != null) {
                            if (this.isOnSurface()) {
                                if (!(item2.isOnSurface() || this.watcher.getVisionArea().getUnderGround() != null && this.watcher.getVisionArea().getUnderGround().items != null && this.watcher.getVisionArea().getUnderGround().items.contains(item2))) {
                                    if (this.watcher.getVisionArea().getUnderGround() != null) {
                                        if (this.watcher.getVisionArea().getUnderGround().covers(item2.getTileX(), item2.getTileY())) {
                                            this.watcher.getVisionArea().getUnderGround().addItem(item2, null, true);
                                        }
                                    } else {
                                        this.addItem(item2, null, true);
                                    }
                                    send = false;
                                }
                            } else if (item2.isOnSurface() && (this.watcher.getVisionArea().getSurface() == null || this.watcher.getVisionArea().getSurface().items == null || !this.watcher.getVisionArea().getSurface().items.contains(item2))) {
                                if (this.watcher.getVisionArea().getSurface() != null) {
                                    this.watcher.getVisionArea().getSurface().addItem(item2, null, true);
                                } else {
                                    this.addItem(item2, null, true);
                                }
                                send = false;
                            }
                        } else {
                            this.addItem(item2, null, true);
                            send = false;
                        }
                    }
                }
                catch (NoSuchItemException item2) {
                    // empty catch block
                }
            }
        }
        if (creatureId != -1L) {
            if (WurmId.getType(creatureId) == 1 || WurmId.getType(creatureId) == 0) {
                if (this.watcher.getWurmId() != creatureId && !this.creatures.containsKey(creatureId) && targetId != -1L) {
                    try {
                        this.addCreature(creatureId, true);
                        send = false;
                    }
                    catch (NoSuchCreatureException item2) {
                    }
                    catch (NoSuchPlayerException item2) {}
                }
            } else if (WurmId.getType(creatureId) == 2 || WurmId.getType(creatureId) == 19 || WurmId.getType(creatureId) == 20) {
                try {
                    item2 = Items.getItem(creatureId);
                    if (!this.items.contains(item2)) {
                        this.addItem(item2, null, true);
                    }
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
            }
        }
        if (send) {
            if (creatureId == this.watcher.getWurmId()) {
                this.watcher.getCommunicator().attachCreature(-1L, targetId, offx, offy, offz, seatId);
            } else {
                this.watcher.getCommunicator().attachCreature(creatureId, targetId, offx, offy, offz, seatId);
            }
        }
    }

    public void sendRotate(Item item, float rotation) {
        if (this.items != null && this.items.contains(item)) {
            this.watcher.getCommunicator().sendRotate(item.getWurmId(), rotation);
        }
    }

    public String toString() {
        return "VirtualZone [ID: " + this.id + ", Watcher: " + this.watcher.getWurmId() + ']';
    }

    public void sendHostileCreatures() {
        int nums = 0;
        String layer = "Above ground";
        if (!this.isOnSurface) {
            layer = "Below ground";
        }
        for (Long c : this.creatures.keySet()) {
            try {
                Creature creat = Server.getInstance().getCreature(c);
                if (creat.getAttitude(this.watcher) != 2) continue;
                int tilex = creat.getTileX();
                int tiley = creat.getTileY();
                if (this.watcher.getCurrentTile() == null) continue;
                ++nums;
                int ctx = this.watcher.getCurrentTile().tilex;
                int cty = this.watcher.getCurrentTile().tiley;
                int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                int dir = MethodsCreatures.getDir(this.watcher, tilex, tiley);
                String direction = MethodsCreatures.getLocationStringFor(this.watcher.getStatus().getRotation(), dir, "you");
                this.watcher.getCommunicator().sendNormalServerMessage(EndGameItems.getDistanceString(mindist, creat.getName(), direction, false) + layer);
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, nsc.getMessage(), nsc);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, nsp.getMessage(), nsp);
            }
        }
        if (nums == 0) {
            this.watcher.getCommunicator().sendNormalServerMessage("No hostile creatures found " + layer.toLowerCase() + ".");
        }
    }

    public void sendAddTileEffect(int tilex, int tiley, int layer, byte effect, int floorLevel, boolean loop) {
        this.watcher.getCommunicator().sendAddAreaSpellEffect(tilex, tiley, layer, effect, floorLevel, 0, loop);
    }

    public void sendRemoveTileEffect(int tilex, int tiley, int layer) {
        this.watcher.getCommunicator().sendRemoveAreaSpellEffect(tilex, tiley, layer);
    }

    public void updateWallDamageState(Wall wall) {
        this.watcher.getCommunicator().sendWallDamageState(wall.getStructureId(), wall.getId(), (byte)wall.getDamage());
    }

    public void updateFloorDamageState(Floor floor) {
        this.watcher.getCommunicator().sendWallDamageState(floor.getStructureId(), floor.getId(), (byte)floor.getDamage());
    }

    public void updateBridgePartDamageState(BridgePart bridgePart) {
        this.watcher.getCommunicator().sendWallDamageState(bridgePart.getStructureId(), bridgePart.getId(), (byte)bridgePart.getDamage());
    }

    public void updateFenceDamageState(Fence fence) {
        this.watcher.getCommunicator().sendDamageState(fence.getId(), (byte)fence.getDamage());
    }

    public void updateTargetStatus(long targetId, byte statusType, float status) {
        this.watcher.getCommunicator().sendTargetStatus(targetId, statusType, status);
    }

    public void setNewFace(Creature c) {
        if (c.getWurmId() == this.watcher.getWurmId()) {
            this.watcher.getCommunicator().sendNewFace(-10L, c.getFace());
        } else if (this.containsCreature(c)) {
            this.watcher.getCommunicator().sendNewFace(c.getWurmId(), c.getFace());
        }
    }

    public void setNewRarityShader(Creature c) {
        if (c.getWurmId() == this.watcher.getWurmId()) {
            this.watcher.getCommunicator().updateCreatureRarity(-10L, c.getRarityShader());
        } else if (this.containsCreature(c)) {
            this.watcher.getCommunicator().updateCreatureRarity(c.getWurmId(), c.getRarityShader());
        }
    }

    public void sendActionControl(long creatureId, String actionString, boolean start, int timeLeft) {
        if (creatureId == this.watcher.getWurmId()) {
            this.watcher.getCommunicator().sendActionControl(-1L, actionString, start, timeLeft);
        } else {
            this.watcher.getCommunicator().sendActionControl(creatureId, actionString, start, timeLeft);
        }
    }

    public void sendProjectile(long itemid, byte type, String modelName, String name, byte material, float _startX, float _startY, float startH, float rot, byte layer, float _endX, float _endY, float endH, long sourceId, long targetId, float projectedSecondsInAir, float actualSecondsInAir) {
        this.watcher.getCommunicator().sendProjectile(itemid, type, modelName, name, material, _startX, _startY, startH, rot, layer, _endX, _endY, endH, sourceId, targetId, projectedSecondsInAir, actualSecondsInAir);
    }

    public void sendNewProjectile(long itemid, byte type, String modelName, String name, byte material, Vector3f startingPosition, Vector3f startingVelocity, Vector3f endingPosition, float rotation, boolean surface) {
        this.watcher.getCommunicator().sendNewProjectile(itemid, type, modelName, name, material, startingPosition, startingVelocity, endingPosition, rotation, surface);
    }

    public final void sendBridgeId(long creatureId, long bridgeId) {
        this.watcher.getCommunicator().sendBridgeId(creatureId, bridgeId);
    }
}


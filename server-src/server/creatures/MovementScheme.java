/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.modifiers.FixedDoubleValueModifier;
import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.modifiers.ValueModifiedListener;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.Enchants;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.util.MovementChecker;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MovementScheme
extends MovementChecker
implements ModifierTypes,
ValueModifiedListener,
ProtoConstants,
CounterTypes,
TimeConstants,
Enchants {
    private final Creature creature;
    boolean halted = false;
    private boolean encumbered = false;
    public Item draggedItem;
    private static final Logger logger = Logger.getLogger(MovementScheme.class.getName());
    private Set<DoubleValueModifier> modifiers;
    private float baseModifier = 1.0f;
    private static final DoubleValueModifier dragMod = new FixedDoubleValueModifier(-0.5);
    private static final DoubleValueModifier ramDragMod = new FixedDoubleValueModifier(-0.75);
    private static final DoubleValueModifier combatMod = new FixedDoubleValueModifier(-0.7);
    private static final DoubleValueModifier drunkMod = new FixedDoubleValueModifier(-0.6);
    private static final DoubleValueModifier mooreMod = new FixedDoubleValueModifier(-5.0);
    private static final DoubleValueModifier farwalkerMod = new FixedDoubleValueModifier(0.5);
    private boolean webArmoured = false;
    private boolean hasSpiritSpeed = false;
    private final DoubleValueModifier webArmourMod = new DoubleValueModifier(7, 0.0);
    private boolean justWebSlowArmour = false;
    private static final DoubleValueModifier chargeMod = new FixedDoubleValueModifier(0.1f);
    DoubleValueModifier stealthMod;
    private static final DoubleValueModifier freezeMod = new FixedDoubleValueModifier(-5.0);
    private static final long NOID = -10L;
    public final DoubleValueModifier armourMod = new DoubleValueModifier(0.0);
    private final List<Float> movementSpeeds;
    private final List<Byte> windImpacts;
    private final List<Short> mountSpeeds;
    private final Set<Integer> intraports = new HashSet<Integer>();
    public int samePosCounts = 0;
    private static Vehicle vehic;
    private static Creature cretVehicle;
    public static Item itemVehicle;
    private static Player passenger;
    private int climbSkill = 10;
    Map<Long, Float> oldmoves = new HashMap<Long, Float>();
    private int changedTileCounter = 0;
    private int errors = 0;
    private boolean hasWetFeet = false;
    private boolean outAtSea = false;
    private boolean m300m = false;
    private boolean m700m = false;
    private boolean m1400m = false;
    private boolean m2180m = false;
    static Creature toRemove;

    MovementScheme(Creature _creature) {
        this.creature = _creature;
        this.movementSpeeds = new ArrayList<Float>();
        this.windImpacts = new ArrayList<Byte>();
        this.mountSpeeds = new ArrayList<Short>();
        if (!this.creature.isPlayer()) {
            this.onGround = true;
        } else {
            this.halted = true;
        }
        this.setLog(true);
    }

    public void initalizeModifiersWithTemplate() {
        this.addModifier(this.armourMod);
        this.armourMod.addListener(this);
    }

    @Override
    public float getTileSteepness(int tilex, int tiley, int clayer) {
        if (this.creature != null && this.creature.getBridgeId() > 0L) {
            return 0.0f;
        }
        float highest = -100.0f;
        float lowest = 32000.0f;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                float height;
                if (tilex + x >= Zones.worldTileSizeX || tiley + y >= Zones.worldTileSizeY) continue;
                if (clayer >= 0) {
                    height = Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(tilex + x, tiley + y));
                    if (height > highest) {
                        highest = height;
                    }
                    if (!(height < lowest)) continue;
                    lowest = height;
                    continue;
                }
                height = Tiles.decodeHeightAsFloat(Server.caveMesh.getTile(tilex + x, tiley + y));
                if (height > highest) {
                    highest = height;
                }
                if (!(height < lowest)) continue;
                lowest = height;
            }
        }
        return highest - lowest;
    }

    @Override
    protected byte getTextureForTile(int xTile, int yTile, int layer, long bridgeId) {
        VolaTile vt;
        if (bridgeId > 0L && (vt = Zones.getTileOrNull(xTile, yTile, layer == 0)) != null) {
            for (BridgePart bp : vt.getBridgeParts()) {
                if (bp.getStructureId() != bridgeId) continue;
                if (bp.getMaterial() == BridgeConstants.BridgeMaterial.WOOD || bp.getMaterial() == BridgeConstants.BridgeMaterial.ROPE) {
                    return Tiles.Tile.TILE_PLANKS.id;
                }
                if (bp.getMaterial() == BridgeConstants.BridgeMaterial.BRICK) {
                    return Tiles.Tile.TILE_COBBLESTONE.id;
                }
                return Tiles.Tile.TILE_STONE_SLABS.id;
            }
        }
        MeshIO mesh = Server.surfaceMesh;
        if (layer < 0) {
            if (xTile < 0 || xTile > Zones.worldTileSizeX || yTile < 0 || yTile > Zones.worldTileSizeY) {
                return Tiles.Tile.TILE_ROCK.id;
            }
            mesh = Server.caveMesh;
        } else {
            if (this.creature.hasOpenedMineDoor(xTile, yTile)) {
                return Tiles.Tile.TILE_HOLE.id;
            }
            if (xTile < 0 || xTile > Zones.worldTileSizeX || yTile < 0 || yTile > Zones.worldTileSizeY) {
                return Tiles.Tile.TILE_DIRT.id;
            }
        }
        return Tiles.decodeType(mesh.getTile(xTile, yTile));
    }

    @Override
    public final float getHeightOfBridge(int xTile, int yTile, int layer) {
        BridgePart[] bridgePartArray;
        int n;
        int n2;
        VolaTile vt = Zones.getTileOrNull(xTile, yTile, layer == 0);
        if (vt != null && (n2 = 0) < (n = (bridgePartArray = vt.getBridgeParts()).length)) {
            BridgePart bp = bridgePartArray[n2];
            return (float)bp.getHeight() / 10.0f;
        }
        return -1000.0f;
    }

    public boolean isIntraTeleporting() {
        return !this.intraports.isEmpty();
    }

    public void addIntraTeleport(int teleportNumber) {
        this.intraports.add(teleportNumber);
    }

    public boolean removeIntraTeleport(int teleportNumber) {
        this.intraports.remove(teleportNumber);
        return this.intraports.isEmpty();
    }

    public void clearIntraports() {
        this.intraports.clear();
    }

    @Override
    protected float getCeilingForNode(int xTile, int yTile) {
        return (float)Tiles.decodeData(Server.caveMesh.getTile(xTile, yTile) & 0xFF) / 10.0f;
    }

    public boolean removeWindMod(byte impact) {
        ListIterator<Byte> it = this.windImpacts.listIterator();
        while (it.hasNext()) {
            Byte b = it.next();
            it.remove();
            if (b != impact) continue;
            if (this.creature.isPlayer()) {
                ((Player)this.creature).sentWind = 0L;
            }
            return true;
        }
        return false;
    }

    public void setWindMod(byte impact) {
        this.setWindImpact((float)impact / 200.0f);
    }

    public boolean addWindImpact(byte impact) {
        if (this.windImpacts.isEmpty() || this.windImpacts.get(this.windImpacts.size() - 1) != impact) {
            this.windImpacts.add(impact);
            this.creature.getCommunicator().sendWindImpact(impact);
            return true;
        }
        return false;
    }

    public void resendMountSpeed() {
        if (this.mountSpeeds.size() > 0) {
            this.creature.getCommunicator().sendMountSpeed(this.mountSpeeds.get(0));
        }
    }

    public boolean removeMountSpeed(short speed) {
        ListIterator<Short> it = this.mountSpeeds.listIterator();
        while (it.hasNext()) {
            Short b = it.next();
            it.remove();
            if (b != speed) continue;
            if (this.creature.isPlayer()) {
                ((Player)this.creature).sentMountSpeed = 0L;
            }
            return true;
        }
        return false;
    }

    public void setMountSpeed(short newMountSpeed) {
        if (this.commandingBoat) {
            this.setMountSpeed((float)newMountSpeed / 1000.0f);
        } else {
            this.setMountSpeed((float)newMountSpeed / 200.0f);
        }
    }

    public boolean addMountSpeed(short speed) {
        if (this.mountSpeeds.isEmpty() || this.mountSpeeds.get(this.mountSpeeds.size() - 1) != speed) {
            this.mountSpeeds.add(speed);
            this.creature.getCommunicator().sendMountSpeed(speed);
            return true;
        }
        return false;
    }

    boolean removeSpeedMod(float speedmod) {
        ListIterator<Float> it = this.movementSpeeds.listIterator();
        while (it.hasNext()) {
            Float f = it.next();
            it.remove();
            if (f.floatValue() != speedmod) continue;
            return true;
        }
        return false;
    }

    public void sendSpeedModifier() {
        if (this.addSpeedMod(Math.max(0.0f, this.getSpeedModifier()))) {
            this.creature.getCommunicator().sendSpeedModifier(Math.max(0.0f, this.getSpeedModifier()));
        }
    }

    private boolean addSpeedMod(float speedmod) {
        this.oldmoves.put(new Long(System.currentTimeMillis()), new Float(speedmod));
        if (this.oldmoves.size() > 20) {
            Long[] longs = this.oldmoves.keySet().toArray(new Long[this.oldmoves.size()]);
            for (int x = 0; x < 10; ++x) {
                this.oldmoves.remove(longs[x]);
            }
        }
        if (this.movementSpeeds.isEmpty() || this.movementSpeeds.get(this.movementSpeeds.size() - 1).floatValue() != speedmod) {
            this.movementSpeeds.add(Float.valueOf(speedmod));
            return true;
        }
        return false;
    }

    @Override
    public void hitGround(float speed) {
        if (this.creature instanceof Player && !this.creature.isDead() && this.creature.isOnSurface() && this.creature.getVisionArea() != null && this.creature.getVisionArea().isInitialized() && !this.creature.getCommunicator().isInvulnerable() && !this.creature.getCommunicator().stillLoggingIn() && this.creature.getFarwalkerSeconds() <= 0 && (this.creature.getPower() < 1 || this.creature.loggerCreature1 > 0L) && speed > 0.5f) {
            if (this.creature.getLayer() >= 0) {
                TimeConstants f2;
                int w2;
                int n;
                TimeConstants[] timeConstantsArray;
                TimeConstants[] fences;
                Wall[] walls;
                VolaTile current;
                float distFromBorder = 0.09f;
                float tilex = (int)this.getX() / 4;
                float tiley = (int)this.getY() / 4;
                if (this.getX() / 4.0f - tilex < distFromBorder && (current = Zones.getTileOrNull((int)tilex, (int)tiley, true)) != null) {
                    for (Wall w2 : walls = current.getWalls()) {
                        if (w2.getFloorLevel() != this.creature.getFloorLevel() || (float)w2.getTileX() != tilex || w2.isHorizontal()) continue;
                        return;
                    }
                    timeConstantsArray = fences = current.getFences();
                    n = timeConstantsArray.length;
                    for (w2 = 0; w2 < n; ++w2) {
                        f2 = timeConstantsArray[w2];
                        if ((float)((Fence)f2).getTileX() != tilex || ((Fence)f2).isHorizontal() || ((Fence)f2).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                }
                if (this.getY() / 4.0f - tiley < distFromBorder && (current = Zones.getTileOrNull((int)tilex, (int)tiley, true)) != null) {
                    walls = current.getWalls();
                    fences = walls;
                    int n2 = fences.length;
                    for (n = 0; n < n2; ++n) {
                        TimeConstants w3 = fences[n];
                        if ((float)((Wall)w3).getTileY() != tiley || !((Wall)w3).isHorizontal() || ((Wall)w3).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                    timeConstantsArray = fences = current.getFences();
                    n = timeConstantsArray.length;
                    for (w2 = 0; w2 < n; ++w2) {
                        f2 = timeConstantsArray[w2];
                        if ((float)((Fence)f2).getTileY() != tiley || !((Fence)f2).isHorizontal() || ((Fence)f2).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                }
                if (tilex + 1.0f - this.getX() / 4.0f < distFromBorder && (current = Zones.getTileOrNull((int)tilex + 1, (int)tiley, true)) != null) {
                    walls = current.getWalls();
                    fences = walls;
                    int n3 = fences.length;
                    for (n = 0; n < n3; ++n) {
                        TimeConstants w4 = fences[n];
                        if ((float)((Wall)w4).getTileX() != tilex + 1.0f || ((Wall)w4).isHorizontal() || ((Wall)w4).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                    timeConstantsArray = fences = current.getFences();
                    n = timeConstantsArray.length;
                    for (w2 = 0; w2 < n; ++w2) {
                        f2 = timeConstantsArray[w2];
                        if ((float)((Fence)f2).getTileX() != tilex + 1.0f || ((Fence)f2).isHorizontal() || ((Fence)f2).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                }
                if (tiley + 1.0f - this.getY() / 4.0f < distFromBorder && (current = Zones.getTileOrNull((int)tilex, (int)tiley + 1, true)) != null) {
                    walls = current.getWalls();
                    fences = walls;
                    int n4 = fences.length;
                    for (n = 0; n < n4; ++n) {
                        TimeConstants w5 = fences[n];
                        if ((float)((Wall)w5).getTileY() != tiley + 1.0f || !((Wall)w5).isHorizontal() || ((Wall)w5).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                    for (TimeConstants f2 : fences = current.getFences()) {
                        if ((float)((Fence)f2).getTileY() != tiley + 1.0f || !((Fence)f2).isHorizontal() || ((Fence)f2).getFloorLevel() != this.creature.getFloorLevel()) continue;
                        return;
                    }
                }
            }
            float baseDam = 1.0f + speed;
            try {
                float damMod = 20.0f;
                float dam = baseDam * baseDam * baseDam * 24.0f * 60.0f * 20.0f / 15.0f;
                dam = Math.max(dam, 300.0f);
                this.creature.getCommunicator().sendNormalServerMessage("Ouch! That hurt!");
                this.creature.sendToLoggers("Speed=" + speed + ", baseDam=" + baseDam + " damMod=" + 20.0f + " weightCarried=" + this.creature.getCarriedWeight() + " dam=" + dam);
                this.creature.achievement(88);
                if (!PlonkData.FALL_DAMAGE.hasSeenThis(this.creature)) {
                    PlonkData.FALL_DAMAGE.trigger(this.creature);
                }
                this.creature.addWoundOfType(null, (byte)0, 1, true, 1.0f, false, dam, 0.0f, 0.0f, false, false);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    protected float getHeightForNode(int xNode, int yNode, int layer) {
        return Zones.getHeightForNode(xNode, yNode, layer);
    }

    @Override
    protected float[] getNodeHeights(int xNode, int yNode, int layer, long bridgeId) {
        return Zones.getNodeHeights(xNode, yNode, layer, bridgeId);
    }

    @Override
    protected boolean handleWrongLayer(int clientInputLayer, int expectedLayer) {
        if (this.creature.getVehicle() != -10L) {
            return false;
        }
        if (this.creature.getPower() >= 2 && Tiles.decodeType(Server.caveMesh.getTile(this.creature.getTileX(), this.creature.getTileY())) != Tiles.Tile.TILE_CAVE_EXIT.id) {
            this.creature.getCommunicator().sendAlertServerMessage("You were detected to be on a different layer from what is shown in your client, setting layer to the one in your client.");
        }
        return true;
    }

    @Override
    protected void handlePlayerInRock() {
        block20: {
            if (!this.creature.isDead()) {
                int tiley;
                int tilex = this.creature.getTileX();
                if (Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley = this.creature.getTileY())) != Tiles.Tile.TILE_CAVE.id && Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)) != Tiles.Tile.TILE_CAVE_EXIT.id) {
                    if (this.creature.getVehicle() == -10L) {
                        for (int x = -1; x <= 1; ++x) {
                            for (int y = -1; y <= 1; ++y) {
                                byte type = Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX(x + tilex), Zones.safeTileY(y + tiley)));
                                Tiles.Tile tempTile = Tiles.getTile(type);
                                if (type == Tiles.Tile.TILE_CAVE_EXIT.id) {
                                    this.creature.setTeleportPoints((short)Zones.safeTileX(x + tilex), (short)Zones.safeTileY(y + tiley), 0, 0);
                                    this.creature.startTeleporting();
                                    this.creature.getCommunicator().sendTeleport(false, false, (byte)0);
                                    return;
                                }
                                if (tempTile.isSolidCave() || tempTile == null) continue;
                                this.creature.setTeleportPoints((short)Zones.safeTileX(x + tilex), (short)Zones.safeTileY(y + tiley), -1, 0);
                                this.creature.startTeleporting();
                                this.creature.getCommunicator().sendTeleport(false, false, (byte)0);
                                return;
                            }
                        }
                        this.creature.getCommunicator().sendAlertServerMessage("You manage to become stuck in the rock, and quickly suffocate.");
                        this.creature.die(false, "Suffocated in Rock (2)");
                    }
                } else {
                    Vehicle creatureVehicle;
                    ((Player)this.creature).intraTeleport(this.creature.getPosX(), this.creature.getPosY(), this.creature.getPositionZ(), this.creature.getStatus().getRotation(), this.creature.getLayer(), "in rock commanding=" + this.creature.isVehicleCommander() + " height=" + this.creature.getPositionZ());
                    if (this.creature.isVehicleCommander() && (creatureVehicle = Vehicles.getVehicleForId(this.creature.getVehicle())) != null) {
                        if (!creatureVehicle.isCreature()) {
                            try {
                                Item ivehicle = Items.getItem(this.creature.getVehicle());
                                if (ivehicle.isOnSurface()) break block20;
                                MovementScheme.itemVehicle.newLayer = (byte)this.creature.getLayer();
                                try {
                                    Zone z1 = Zones.getZone(ivehicle.getTileX(), ivehicle.getTileY(), false);
                                    z1.removeItem(ivehicle);
                                    ivehicle.setPosXY(this.creature.getPosX(), this.creature.getPosY());
                                    Zone z2 = Zones.getZone(ivehicle.getTileX(), ivehicle.getTileY(), false);
                                    z2.addItem(ivehicle);
                                }
                                catch (NoSuchZoneException z1) {
                                    // empty catch block
                                }
                                MovementScheme.itemVehicle.newLayer = (byte)-128;
                            }
                            catch (NoSuchItemException ivehicle) {}
                        } else {
                            try {
                                Creature cvehicle = Creatures.getInstance().getCreature(this.creature.getVehicle());
                                if (!cvehicle.isOnSurface()) {
                                    try {
                                        Zone z1 = Zones.getZone(cvehicle.getTileX(), cvehicle.getTileY(), false);
                                        z1.removeCreature(cvehicle, true, false);
                                        cvehicle.setPositionX(this.creature.getPosX());
                                        cvehicle.setPositionY(this.creature.getPosY());
                                        Zone z2 = Zones.getZone(cvehicle.getTileX(), cvehicle.getTileY(), false);
                                        z2.addCreature(cvehicle.getWurmId());
                                    }
                                    catch (NoSuchZoneException noSuchZoneException) {
                                    }
                                    catch (NoSuchPlayerException noSuchPlayerException) {}
                                }
                            }
                            catch (NoSuchCreatureException noSuchCreatureException) {
                                // empty catch block
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void setLayer(int layer) {
        this.creature.setLayer(layer, false);
    }

    @Override
    protected boolean handleMoveTooFar(float clientInput, float expectedDistance) {
        if (!this.creature.isDead() && (this.creature.getPower() < 1 || this.creature.loggerCreature1 != -10L) && clientInput - expectedDistance * 1.1f > 0.0f && this.changedTileCounter == 0) {
            this.setErrors(this.getErrors() + 1);
            if (this.getErrors() > 4) {
                this.creature.getStatus().setNormalRegen(false);
                logger.log(Level.WARNING, this.creature.getName() + " TOO FAR, input=" + clientInput + ", expected=" + expectedDistance + " :  " + this.getCurrx() + "(" + this.getXold() + "); " + this.getCurry() + "(" + this.getYold() + ") bridge=" + this.creature.getBridgeId());
                ((Player)this.creature).intraTeleport(this.creature.getPosX(), this.creature.getPosY(), this.creature.getPositionZ(), this.creature.getStatus().getRotation(), this.creature.getLayer(), "Moved too far");
                this.setAbort(true);
                if (this.getErrors() > 10) {
                    Players.getInstance().sendGlobalGMMessage(this.creature, " movement too far (" + (clientInput - expectedDistance) + ") at " + this.getCurrx() / 4.0f + "," + this.getCurry() / 4.0f);
                }
                return true;
            }
        }
        return false;
    }

    public void setServerClimbing(boolean climb) {
        this.setClimbing(climb);
    }

    @Override
    protected boolean handleMoveTooShort(float clientInput, float expectedDistance) {
        if (this.creature.isTeleporting() || this.isIntraTeleporting() || this.creature.isDead() || !this.creature.getCommunicator().hasReceivedTicks() && this.isClimbing() || this.creature.getPower() >= 2 || this.changedTileCounter != 0 || this.creature.getCurrentTile() != null) {
            // empty if block
        }
        return false;
    }

    @Override
    public final boolean movedOnStair() {
        VolaTile t = this.creature.getCurrentTile();
        if (t != null && (t.hasStair(this.creature.getFloorLevel() + 1) || t.hasStair(this.creature.getFloorLevel()))) {
            this.setBridgeCounter(10);
            this.wasOnStair = true;
        }
        return this.wasOnStair;
    }

    @Override
    protected boolean handleZError(float clientInput, float expectedPosition) {
        if (!(this.changedTileCounter != 0 || this.creature.getPower() >= 2 || this.creature.isTeleporting() || this.creature.isDead() || !this.creature.getCommunicator().hasReceivedTicks() && this.isClimbing())) {
            if (this.getTargetGroundOffset() != this.getGroundOffset()) {
                return false;
            }
            if (this.creature.getVisionArea() != null && this.creature.getVisionArea().isInitialized()) {
                this.setErrors(this.getErrors() + 1);
                if (this.getErrors() > 4) {
                    ((Player)this.creature).intraTeleport(this.creature.getPosX(), this.creature.getPosY(), this.creature.getPositionZ(), this.creature.getStatus().getRotation(), this.creature.getLayer(), "Error in z=" + clientInput + ", expected=" + expectedPosition);
                    this.setAbort(true);
                    this.setErrors(this.getErrors() + 1);
                    if (this.getErrors() > 10) {
                        Players.getInstance().sendGlobalGMMessage(this.creature, " movement too high (" + (clientInput - expectedPosition) + ") at " + this.getCurrx() / 4.0f + "," + this.getCurry() / 4.0f);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static void movePassengers(Vehicle aVehicle, @Nullable Creature driver, boolean isCreature) {
        if (isCreature) {
            for (int x = 0; x < aVehicle.seats.length; ++x) {
                if (aVehicle.seats[x].type != 1 || !aVehicle.seats[x].isOccupied()) continue;
                try {
                    passenger = Players.getInstance().getPlayer(aVehicle.seats[x].occupant);
                    float r = (-cretVehicle.getStatus().getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                    float s = (float)Math.sin(r);
                    float c = (float)Math.cos(r);
                    float xo = s * -aVehicle.seats[x].offx - c * -aVehicle.seats[x].offy;
                    float yo = c * -aVehicle.seats[x].offx + s * -aVehicle.seats[x].offy;
                    float newposx = cretVehicle.getPosX() + xo;
                    float newposy = cretVehicle.getPosY() + yo;
                    newposx = Math.max(3.0f, newposx);
                    newposx = Math.min(Zones.worldMeterSizeX - 3.0f, newposx);
                    newposy = Math.max(3.0f, newposy);
                    newposy = Math.min(Zones.worldMeterSizeY - 3.0f, newposy);
                    int diffx = ((int)newposx >> 2) - passenger.getTileX();
                    int diffy = ((int)newposy >> 2) - passenger.getTileY();
                    boolean move = true;
                    if (diffy != 0 || diffx != 0) {
                        Blocker first;
                        BlockingResult result = Blocking.getBlockerBetween(passenger, passenger.getStatus().getPositionX(), passenger.getStatus().getPositionY(), newposx, newposy, passenger.getPositionZ(), passenger.getPositionZ(), passenger.isOnSurface(), passenger.isOnSurface(), false, 6, -1L, passenger.getBridgeId(), passenger.getBridgeId(), cretVehicle.followsGround());
                        if (!(result == null || (first = result.getFirstBlocker()).isDoor() && (first.canBeOpenedBy(passenger, false) || driver == null || first.canBeOpenedBy(driver, false)) || first instanceof BridgePart)) {
                            if (driver != null) {
                                newposx = driver.getPosX();
                                newposy = driver.getPosY();
                                diffx = ((int)newposx >> 2) - passenger.getTileX();
                                diffy = ((int)newposy >> 2) - passenger.getTileY();
                            } else {
                                move = false;
                                passenger.disembark(false);
                            }
                        }
                        if (move && passenger.getLayer() < 0 && (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newposx >> 2, (int)newposy >> 2))) || Blocking.isDiagonalRockBetween(passenger, passenger.getTileX(), passenger.getTileY(), (int)newposx >> 2, (int)newposy >> 2) != null) && driver != null) {
                            newposx = driver.getPosX();
                            newposy = driver.getPosY();
                            diffx = ((int)newposx >> 2) - passenger.getTileX();
                            diffy = ((int)newposy >> 2) - passenger.getTileY();
                        }
                        if (passenger.getStatus().isTrading()) {
                            Trade trade = passenger.getStatus().getTrade();
                            Creature lOpponent = null;
                            lOpponent = trade.creatureOne == passenger ? trade.creatureTwo : trade.creatureOne;
                            if (Creature.rangeTo((Creature)passenger, lOpponent) > 6) {
                                trade.end(passenger, false);
                            }
                        }
                    }
                    if (!move) continue;
                    passenger.getStatus().setPositionXYZ(newposx, newposy, cretVehicle.getPositionZ() + aVehicle.seats[x].offz);
                    passenger.getMovementScheme().setPosition(passenger.getStatus().getPositionX(), passenger.getStatus().getPositionY(), passenger.getStatus().getPositionZ(), passenger.getStatus().getRotation(), passenger.getLayer());
                    if (diffy != 0 || diffx != 0) {
                        try {
                            if (passenger.hasLink() && passenger.getVisionArea() != null) {
                                passenger.getVisionArea().move(diffx, diffy);
                                passenger.getVisionArea().linkZones(diffy, diffx);
                            }
                            Zone z = Zones.getZone(passenger.getTileX(), passenger.getTileY(), passenger.isOnSurface());
                            passenger.getStatus().savePosition(passenger.getWurmId(), true, z.getId(), false);
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, iox.getMessage(), iox);
                            passenger.setLink(false);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.WARNING, nsz.getMessage(), nsz);
                            passenger.setLink(false);
                        }
                        catch (Exception nsz) {
                            // empty catch block
                        }
                    }
                    diffx = ((int)newposx >> 2) - MovementScheme.passenger.getCurrentTile().tilex;
                    diffy = ((int)newposy >> 2) - MovementScheme.passenger.getCurrentTile().tiley;
                    if (diffy == 0 && diffx == 0) continue;
                    try {
                        passenger.getCurrentTile().creatureMoved(passenger.getWurmId(), 0.0f, 0.0f, 0.0f, diffx, diffy, true);
                    }
                    catch (NoSuchPlayerException nsz) {
                    }
                    catch (NoSuchCreatureException nsz) {}
                    continue;
                }
                catch (NoSuchPlayerException r) {
                    // empty catch block
                }
            }
        } else {
            for (int x = 0; x < aVehicle.seats.length; ++x) {
                if (aVehicle.seats[x].type != 1 || !aVehicle.seats[x].isOccupied()) continue;
                try {
                    passenger = Players.getInstance().getPlayer(aVehicle.seats[x].occupant);
                    float r = (-itemVehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                    float s = (float)Math.sin(r);
                    float c = (float)Math.cos(r);
                    float xo = s * -aVehicle.seats[x].offx - c * -aVehicle.seats[x].offy;
                    float yo = c * -aVehicle.seats[x].offx + s * -aVehicle.seats[x].offy;
                    float newposx = itemVehicle.getPosX() + xo;
                    float newposy = itemVehicle.getPosY() + yo;
                    newposx = Math.max(3.0f, newposx);
                    newposx = Math.min(Zones.worldMeterSizeX - 3.0f, newposx);
                    newposy = Math.max(3.0f, newposy);
                    newposy = Math.min(Zones.worldMeterSizeY - 3.0f, newposy);
                    int diffx = ((int)newposx >> 2) - passenger.getTileX();
                    int diffy = ((int)newposy >> 2) - passenger.getTileY();
                    boolean move = true;
                    if (diffy != 0 || diffx != 0) {
                        Blocker first;
                        BlockingResult result;
                        if (!(!passenger.isOnSurface() || (result = Blocking.getBlockerBetween(passenger, passenger.getStatus().getPositionX(), passenger.getStatus().getPositionY(), newposx, newposy, passenger.getPositionZ(), passenger.getPositionZ(), passenger.isOnSurface(), passenger.isOnSurface(), false, 6, -1L, passenger.getBridgeId(), passenger.getBridgeId(), itemVehicle.getFloorLevel() == 0 && itemVehicle.getBridgeId() <= 0L)) == null || (first = result.getFirstBlocker()).isDoor() && (first.canBeOpenedBy(passenger, false) || driver == null || first.canBeOpenedBy(driver, false)) || first instanceof BridgePart)) {
                            if (driver != null) {
                                newposx = driver.getPosX();
                                newposy = driver.getPosY();
                                diffx = ((int)newposx >> 2) - passenger.getTileX();
                                diffy = ((int)newposy >> 2) - passenger.getTileY();
                            } else {
                                move = false;
                                passenger.disembark(false);
                            }
                        }
                        if (move && passenger.getLayer() < 0 && (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newposx >> 2, (int)newposy >> 2))) || Blocking.isDiagonalRockBetween(passenger, passenger.getTileX(), passenger.getTileY(), (int)newposx >> 2, (int)newposy >> 2) != null) && driver != null) {
                            newposx = driver.getPosX();
                            newposy = driver.getPosY();
                            diffx = ((int)newposx >> 2) - passenger.getTileX();
                            diffy = ((int)newposy >> 2) - passenger.getTileY();
                        }
                        if (passenger.getStatus().isTrading()) {
                            Trade trade = passenger.getStatus().getTrade();
                            Creature lOpponent = null;
                            lOpponent = trade.creatureOne == passenger ? trade.creatureTwo : trade.creatureOne;
                            if (Creature.rangeTo((Creature)passenger, lOpponent) > 6) {
                                trade.end(passenger, false);
                            }
                        }
                    }
                    if (!move) continue;
                    passenger.getStatus().setPositionXYZ(newposx, newposy, itemVehicle.getPosZ() + aVehicle.seats[x].offz);
                    passenger.getMovementScheme().setPosition(passenger.getStatus().getPositionX(), passenger.getStatus().getPositionY(), passenger.getStatus().getPositionZ(), passenger.getStatus().getRotation(), passenger.getLayer());
                    if (diffy != 0 || diffx != 0) {
                        try {
                            if (passenger.hasLink() && passenger.getVisionArea() != null) {
                                passenger.getVisionArea().move(diffx, diffy);
                                passenger.getVisionArea().linkZones(diffy, diffx);
                            }
                            Zone z = Zones.getZone(passenger.getTileX(), passenger.getTileY(), passenger.isOnSurface());
                            passenger.getStatus().savePosition(passenger.getWurmId(), true, z.getId(), false);
                        }
                        catch (IOException iox) {
                            passenger.setLink(false);
                        }
                        catch (NoSuchZoneException nsz) {
                            passenger.setLink(false);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    diffx = ((int)newposx >> 2) - MovementScheme.passenger.getCurrentTile().tilex;
                    diffy = ((int)newposy >> 2) - MovementScheme.passenger.getCurrentTile().tiley;
                    if (diffy == 0 && diffx == 0) continue;
                    try {
                        passenger.getCurrentTile().creatureMoved(passenger.getWurmId(), 0.0f, 0.0f, 0.0f, diffx, diffy, true);
                    }
                    catch (NoSuchPlayerException noSuchPlayerException) {
                    }
                    catch (NoSuchCreatureException noSuchCreatureException) {}
                    continue;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }

    public void moveVehicle(float diffX, float diffY, float diffZ) {
        if (vehic.isChair()) {
            return;
        }
        if (MovementScheme.vehic.creature) {
            try {
                cretVehicle = Creatures.getInstance().getCreature(MovementScheme.vehic.wurmid);
                if (this.creature.isOnSurface() == cretVehicle.isOnSurface()) {
                    VolaTile t = Zones.getTileOrNull(cretVehicle.getTileX(), cretVehicle.getTileY(), cretVehicle.isOnSurface());
                    if (t == null) {
                        try {
                            Zone z = Zones.getZone(cretVehicle.getTileX(), cretVehicle.getTileY(), cretVehicle.isOnSurface());
                            z.removeCreature(cretVehicle, false, false);
                            z.addCreature(MovementScheme.vehic.wurmid);
                        }
                        catch (NoSuchZoneException z) {
                            // empty catch block
                        }
                        this.creature.disembark(true);
                        return;
                    }
                    Seat driverseat = vehic.getPilotSeat();
                    float diffrot = Creature.normalizeAngle(this.getVehicleRotation()) - cretVehicle.getStatus().getRotation();
                    cretVehicle.setRotation(Creature.normalizeAngle(this.getVehicleRotation()));
                    float _r = (-cretVehicle.getStatus().getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                    float _s = (float)Math.sin(_r);
                    float _c = (float)Math.cos(_r);
                    float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                    float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                    float nPosX = this.creature.getPosX() - xo;
                    float nPosY = this.creature.getPosY() - yo;
                    float nPosZ = this.creature.getPositionZ() - driverseat.offz;
                    nPosX = Math.max(3.0f, nPosX);
                    nPosX = Math.min(Zones.worldMeterSizeX - 3.0f, nPosX);
                    nPosY = Math.max(3.0f, nPosY);
                    nPosY = Math.min(Zones.worldMeterSizeY - 3.0f, nPosY);
                    if (!cretVehicle.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2))) && Tiles.decodeType(Server.caveMesh.getTile(cretVehicle.getTileX(), cretVehicle.getTileY())) != 201) {
                        return;
                    }
                    diffX = nPosX - cretVehicle.getPosX();
                    diffY = nPosY - cretVehicle.getPosY();
                    if (diffX != 0.0f || diffY != 0.0f) {
                        int dtx = ((int)nPosX >> 2) - cretVehicle.getTileX();
                        int dty = ((int)nPosY >> 2) - cretVehicle.getTileY();
                        cretVehicle.setPositionX(nPosX);
                        cretVehicle.setPositionY(nPosY);
                        cretVehicle.setPositionZ(nPosZ);
                        try {
                            cretVehicle.getVisionArea().move(dtx, dty);
                            cretVehicle.getVisionArea().linkZones(dtx, dty);
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                        t.creatureMoved(MovementScheme.vehic.wurmid, diffX, diffY, diffZ, dtx, dty);
                    } else if (diffrot != 0.0f) {
                        t.creatureMoved(MovementScheme.vehic.wurmid, 0.0f, 0.0f, diffZ, 0, 0);
                    }
                } else {
                    Zone zone = null;
                    try {
                        zone = Zones.getZone(cretVehicle.getTileX(), cretVehicle.getTileY(), cretVehicle.isOnSurface());
                        zone.removeCreature(cretVehicle, false, false);
                    }
                    catch (NoSuchZoneException driverseat) {
                        // empty catch block
                    }
                    Seat driverseat = vehic.getPilotSeat();
                    cretVehicle.setRotation(Creature.normalizeAngle(this.getVehicleRotation()));
                    float _r = (-cretVehicle.getStatus().getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                    float _s = (float)Math.sin(_r);
                    float _c = (float)Math.cos(_r);
                    float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                    float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                    float nPosX = cretVehicle.getPosX() - xo;
                    float nPosY = cretVehicle.getPosY() - yo;
                    nPosX = Math.max(3.0f, nPosX);
                    nPosX = Math.min(Zones.worldMeterSizeX - 3.0f, nPosX);
                    nPosY = Math.max(3.0f, nPosY);
                    nPosY = Math.min(Zones.worldMeterSizeY - 3.0f, nPosY);
                    cretVehicle.setPositionX(nPosX);
                    cretVehicle.setPositionY(nPosY);
                    cretVehicle.setLayer(this.creature.getLayer(), false);
                    try {
                        zone = Zones.getZone(cretVehicle.getTileX(), cretVehicle.getTileY(), this.creature.isOnSurface());
                        zone.addCreature(cretVehicle.getWurmId());
                    }
                    catch (NoSuchZoneException noSuchZoneException) {
                        // empty catch block
                    }
                }
                MovementScheme.movePassengers(vehic, this.creature, true);
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
            }
            catch (NoSuchPlayerException noSuchPlayerException) {}
        } else {
            try {
                itemVehicle = Items.getItem(MovementScheme.vehic.wurmid);
                if (this.creature.isOnSurface() == itemVehicle.isOnSurface()) {
                    this.moveItemVehicleSameLevel();
                } else {
                    this.moveItemVehicleOtherLevel();
                }
                MovementScheme.movePassengers(vehic, this.creature, false);
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
    }

    public void moveItemVehicleOtherLevel() {
        Seat[] pseats;
        int caveTile;
        Seat driverseat = vehic.getPilotSeat();
        float _r = (-itemVehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
        float _s = (float)Math.sin(_r);
        float _c = (float)Math.cos(_r);
        float xo = _s * -driverseat.offx - _c * -driverseat.offy;
        float yo = _c * -driverseat.offx + _s * -driverseat.offy;
        float nPosX = itemVehicle.getPosX() - xo;
        float nPosY = itemVehicle.getPosY() - yo;
        nPosX = Math.max(3.0f, nPosX);
        nPosX = Math.min(Zones.worldMeterSizeX - 3.0f, nPosX);
        nPosY = Math.max(3.0f, nPosY);
        nPosY = Math.min(Zones.worldMeterSizeY - 3.0f, nPosY);
        if (!this.creature.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(caveTile = Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2)))) {
            this.moveItemVehicleSameLevel();
            return;
        }
        Zone zone = null;
        MovementScheme.itemVehicle.newLayer = (byte)this.creature.getLayer();
        try {
            zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, itemVehicle.isOnSurface());
            zone.removeItem(itemVehicle, true, true);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
        itemVehicle.setPosXY(nPosX, nPosY);
        try {
            zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, MovementScheme.itemVehicle.newLayer >= 0);
            zone.addItem(itemVehicle, false, false, false);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
        MovementScheme.itemVehicle.newLayer = (byte)-128;
        Seat[] seats = MovementScheme.vehic.hitched;
        if (seats != null) {
            for (int x = 0; x < seats.length; ++x) {
                if (seats[x] == null || seats[x].occupant == -10L) continue;
                try {
                    Creature c = Server.getInstance().getCreature(seats[x].occupant);
                    c.getStatus().setLayer(itemVehicle.isOnSurface() ? 0 : -1);
                    c.getCurrentTile().newLayer(c);
                    continue;
                }
                catch (NoSuchPlayerException c) {
                    continue;
                }
                catch (NoSuchCreatureException c) {
                    // empty catch block
                }
            }
        }
        if ((pseats = MovementScheme.vehic.seats) != null) {
            for (int x = 0; x < pseats.length; ++x) {
                if (x <= 0 || pseats[x] == null || pseats[x].occupant == -10L) continue;
                try {
                    Creature c = Server.getInstance().getCreature(pseats[x].occupant);
                    logger.log(Level.INFO, c.getName() + " Setting to new layer " + (itemVehicle.isOnSurface() ? 0 : -1));
                    c.getStatus().setLayer(itemVehicle.isOnSurface() ? 0 : -1);
                    c.getCurrentTile().newLayer(c);
                    if (!c.isPlayer()) continue;
                    if (itemVehicle.isOnSurface()) {
                        c.getCommunicator().sendNormalServerMessage("You leave the cave.");
                        continue;
                    }
                    c.getCommunicator().sendNormalServerMessage("You enter the cave.");
                    if (c.getVisionArea() == null) continue;
                    c.getVisionArea().initializeCaves();
                    continue;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    continue;
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                    // empty catch block
                }
            }
        }
    }

    public void moveItemVehicleSameLevel() {
        VolaTile t = Zones.getTileOrNull(itemVehicle.getTileX(), itemVehicle.getTileY(), itemVehicle.isOnSurface());
        if (t == null) {
            try {
                Zone z = Zones.getZone(itemVehicle.getTileX(), itemVehicle.getTileY(), itemVehicle.isOnSurface());
                z.removeItem(itemVehicle);
                z.addItem(itemVehicle);
            }
            catch (NoSuchZoneException z) {
                // empty catch block
            }
            this.creature.disembark(true);
            return;
        }
        Seat driverseat = vehic.getPilotSeat();
        if (driverseat == null) {
            logger.warning("Driverseat null for " + this.creature.getName());
            this.creature.disembark(true);
            return;
        }
        float _r = (-itemVehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
        float _s = (float)Math.sin(_r);
        float _c = (float)Math.cos(_r);
        float xo = _s * -driverseat.offx - _c * -driverseat.offy;
        float yo = _c * -driverseat.offx + _s * -driverseat.offy;
        float nPosX = this.creature.getPosX() - xo;
        float nPosY = this.creature.getPosY() - yo;
        float nPosZ = this.creature.getPositionZ() - driverseat.offz;
        nPosX = Math.max(3.0f, nPosX);
        nPosX = Math.min(Zones.worldMeterSizeX - 3.0f, nPosX);
        nPosY = Math.max(3.0f, nPosY);
        nPosY = Math.min(Zones.worldMeterSizeY - 3.0f, nPosY);
        int diffdecx = (int)(nPosX * 100.0f - itemVehicle.getPosX() * 100.0f);
        int diffdecy = (int)(nPosY * 100.0f - itemVehicle.getPosY() * 100.0f);
        if (diffdecx != 0 || diffdecy != 0) {
            nPosX = itemVehicle.getPosX() + (float)diffdecx * 0.01f;
            nPosY = itemVehicle.getPosY() + (float)diffdecy * 0.01f;
        }
        if (!itemVehicle.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2))) && Tiles.decodeType(Server.caveMesh.getTile((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2)) != 201) {
            return;
        }
        t.moveItem(itemVehicle, nPosX, nPosY, nPosZ, Creature.normalizeAngle(this.getVehicleRotation()), itemVehicle.isOnSurface(), itemVehicle.getPosZ());
        if (MovementScheme.vehic.draggers != null) {
            for (Creature c : MovementScheme.vehic.draggers) {
                if (c.isDead()) {
                    toRemove = c;
                    continue;
                }
                this.moveDragger(c);
            }
            if (toRemove != null) {
                vehic.removeDragger(toRemove);
                toRemove = null;
            }
        }
    }

    public void moveDragger(Creature c) {
        Vehicle v = c.getHitched();
        Seat seat = v.getHitchSeatFor(c.getWurmId());
        float _r = (-itemVehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
        float _s = (float)Math.sin(_r);
        float _c = (float)Math.cos(_r);
        float xo = _s * -seat.offx - _c * -seat.offy;
        float yo = _c * -seat.offx + _s * -seat.offy;
        float nPosX = this.creature.getPosX() + xo;
        float nPosY = this.creature.getPosY() + yo;
        nPosX = Math.max(3.0f, nPosX);
        nPosX = Math.min(Zones.worldMeterSizeX - 3.0f, nPosX);
        nPosY = Math.max(3.0f, nPosY);
        nPosY = Math.min(Zones.worldMeterSizeY - 3.0f, nPosY);
        int diffdecx = (int)(nPosX * 100.0f - itemVehicle.getPosX() * 100.0f);
        int diffdecy = (int)(nPosY * 100.0f - itemVehicle.getPosY() * 100.0f);
        if (diffdecx != 0 || diffdecy != 0) {
            nPosX = itemVehicle.getPosX() + (float)diffdecx * 0.01f;
            nPosY = itemVehicle.getPosY() + (float)diffdecy * 0.01f;
        }
        this.moveDragger(c, nPosX, nPosY, itemVehicle.getPosZ(), Creature.normalizeAngle(this.getVehicleRotation()), false);
    }

    public void moveDragger(Creature c, float nPosX, float nPosY, float nPosZ, float newRot, boolean addRemove) {
        int diffx = ((int)nPosX >> 2) - c.getCurrentTile().tilex;
        int diffy = ((int)nPosY >> 2) - c.getCurrentTile().tiley;
        if (c.getLayer() < 0 && itemVehicle.isOnSurface()) {
            if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2)))) {
                c.setLayer(0, false);
            } else {
                byte typeSurf = Tiles.decodeType(Server.surfaceMesh.getTile((int)nPosX >> 2, (int)nPosY >> 2));
                if (typeSurf != 0) {
                    c.setLayer(0, false);
                }
            }
        }
        c.getStatus().setPositionXYZ(nPosX, nPosY, nPosZ);
        c.getMovementScheme().setPosition(c.getStatus().getPositionX(), c.getStatus().getPositionY(), c.getStatus().getPositionZ(), newRot, c.getLayer());
        if (Math.abs(c.getStatus().getRotation() - newRot) > 10.0f) {
            c.setRotation(newRot);
            c.moved(0.0f, 0.0f, 0.0f, 0, 0);
        }
        if (diffy != 0 || diffx != 0) {
            try {
                if (c.getVisionArea() != null) {
                    c.getVisionArea().move(diffx, diffy);
                    c.getVisionArea().linkZones(diffy, diffx);
                }
                Zone z = Zones.getZone(c.getTileX(), c.getTileY(), c.isOnSurface());
                c.getStatus().savePosition(c.getWurmId(), c.isPlayer(), z.getId(), true);
                try {
                    c.getCurrentTile().creatureMoved(c.getWurmId(), 0.0f, 0.0f, 0.0f, diffx, diffy, true);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                }
                catch (NoSuchCreatureException noSuchCreatureException) {}
            }
            catch (IOException iOException) {
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
        }
        if (addRemove) {
            diffx = ((int)nPosX >> 2) - MovementScheme.passenger.getCurrentTile().tilex;
            diffy = ((int)nPosY >> 2) - MovementScheme.passenger.getCurrentTile().tiley;
            if (diffy != 0 || diffx != 0) {
                try {
                    passenger.getCurrentTile().creatureMoved(passenger.getWurmId(), 0.0f, 0.0f, 0.0f, diffx, diffy, true);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                    // empty catch block
                }
            }
        }
    }

    public void move(float diffX, float diffY, float diffZ) {
        vehic = null;
        if (!this.isIntraTeleporting()) {
            double moveDiff;
            int weight = this.creature.getCarriedWeight();
            if (this.draggedItem != null) {
                weight += this.draggedItem.getWeightGrams() / 100;
            }
            double beforeZMod = moveDiff = Math.sqrt(diffX * diffX + diffY * diffY) * 15.0;
            if (this.isClimbing() && this.creature.getVehicle() == -10L && !this.creature.isUsingLastGasp()) {
                if (weight <= 10000) {
                    weight = 10000;
                }
                if (this.creature.getLayer() <= 0) {
                    short[] steepness = Creature.getTileSteepness(this.creature.getTileX(), this.creature.getTileY(), this.creature.isOnSurface());
                    if (diffZ != 0.0f && (steepness[1] > 23 || steepness[1] < -23)) {
                        float gsbon = this.creature.getBonusForSpellEffect((byte)38) / 20.0f;
                        moveDiff = gsbon > 0.0f ? (moveDiff += (double)Math.max(1.0f, Math.abs(diffZ) * 500.0f / Math.max(1.0f, gsbon))) : (moveDiff += (double)Math.max(1.0f, Math.abs(diffZ) * 500.0f) / Math.max(1.0, Math.pow(this.climbSkill / 10, 0.3)));
                        if (Server.rand.nextInt(this.climbSkill * 2) == 0) {
                            this.climbSkill = Math.max(10, (int)this.creature.getClimbingSkill().getKnowledge(0.0));
                            if (this.creature.getStatus().getStamina() < 10000) {
                                int stam = (int)(101.0f - Math.max(10.0f, (float)this.creature.getStatus().getStamina() / 100.0f));
                                if (gsbon <= 0.0f || this.creature.getStatus().getStamina() < 25 && Server.rand.nextInt(this.climbSkill) == 0) {
                                    Skill skill = this.creature.getClimbingSkill();
                                    double d = stam;
                                    boolean bl = this.creature.getStatus().getStamina() < 25;
                                    if (skill.skillCheck(d, 0.0, bl, Math.max(1, this.climbSkill / 20)) < 0.0) {
                                        try {
                                            this.creature.getCommunicator().sendNormalServerMessage("You need to catch your breath, and stop climbing.");
                                            this.creature.setClimbing(false);
                                            this.creature.getCommunicator().sendToggle(0, this.creature.isClimbing());
                                        }
                                        catch (IOException iox) {
                                            logger.log(Level.WARNING, this.creature.getName() + ' ' + iox.getMessage(), iox);
                                        }
                                    }
                                }
                            } else {
                                this.creature.getClimbingSkill().skillCheck(Math.max((float)(-this.climbSkill / 2), Math.min((float)this.climbSkill * 1.25f, (float)(steepness[1] - this.climbSkill * 2))), 0.0, gsbon > 0.0f, Math.max(1, this.climbSkill / 20));
                            }
                        }
                    }
                }
            } else if (diffZ < 0.0f) {
                moveDiff += Math.max(-moveDiff, (double)Math.min(-1.0f, diffZ * 100.0f));
            } else if (diffZ != 0.0f && moveDiff > 0.0) {
                moveDiff += Math.max(0.5, (double)(diffZ * 50.0f));
            }
            if (this.creature.isStealth()) {
                if (weight <= 10000) {
                    weight = 10000;
                }
                weight += 5000;
            }
            if (diffX != 0.0f || diffY != 0.0f || diffZ != 0.0f) {
                block101: {
                    Item barrier;
                    VolaTile vtile;
                    this.creature.getStatus().setMoving(true);
                    MeshIO mesh = Server.surfaceMesh;
                    if (this.creature.getLayer() < 0) {
                        mesh = Server.caveMesh;
                    }
                    int tile = mesh.getTile(this.creature.getCurrentTile().tilex, this.creature.getCurrentTile().tiley);
                    if (this.creature.isPlayer()) {
                        short height = Tiles.decodeHeight(tile);
                        if (height > 21800) {
                            if (!this.m2180m) {
                                this.m2180m = true;
                                this.creature.achievement(81);
                            }
                        } else if (height > 14000) {
                            if (!this.m1400m) {
                                this.m1400m = true;
                                this.creature.achievement(80);
                            }
                        } else if (height > 7000) {
                            if (!this.m700m) {
                                this.m700m = true;
                                this.creature.achievement(79);
                            }
                        } else if (height > 3000) {
                            if (!this.m300m) {
                                this.m300m = true;
                                this.creature.achievement(78);
                            }
                        } else if (height < -300 && !this.outAtSea) {
                            this.outAtSea = true;
                            this.creature.achievement(77);
                        }
                    }
                    if (this.creature.isStealth() && Terraforming.isRoad(Tiles.decodeType(tile)) && Server.rand.nextInt(20) == 0) {
                        this.creature.setStealth(false);
                    }
                    if ((vtile = Zones.getTileOrNull(this.creature.getCurrentTile().tilex, this.creature.getCurrentTile().tiley, this.creature.isOnSurface())) != null && (this.creature.getPower() < 2 || this.creature.loggerCreature1 > 0L) && vtile.hasOnePerTileItem(this.creature.getFloorLevel()) && Servers.localServer.PVPSERVER && (barrier = vtile.getOnePerTileItem(this.creature.getFloorLevel())) != null && barrier.getTemplateId() == 938 && this.creature.getFarwalkerSeconds() <= 0) {
                        Rectangle rect = new Rectangle(this.creature.getCurrentTile().tilex * 4, this.creature.getCurrentTile().tiley * 4 + 1, 4, 1);
                        AffineTransform transform = new AffineTransform();
                        transform.rotate(barrier.getRotation() - 90.0f, rect.getX() + (double)(rect.width / 2), rect.getY() + (double)(rect.height / 2));
                        Shape transformed = transform.createTransformedShape(rect);
                        if (transformed.contains(this.creature.getPosX(), this.creature.getPosY())) {
                            Wound wound = null;
                            boolean dead = false;
                            try {
                                byte pos = this.creature.getBody().getRandomWoundPos();
                                if (Server.rand.nextInt(10) <= 6 && this.creature.getBody().getWounds() != null) {
                                    wound = this.creature.getBody().getWounds().getWoundAtLocation(pos);
                                    if (wound != null) {
                                        dead = wound.modifySeverity((int)(1000.0f + (float)Server.rand.nextInt(4000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f));
                                        wound.setBandaged(false);
                                        this.creature.setWounded();
                                    }
                                    barrier.setDamage(barrier.getDamage() + Server.rand.nextFloat() * 5.0f);
                                }
                                if (wound == null) {
                                    dead = this.creature.addWoundOfType(null, (byte)2, pos, false, 1.0f, true, 500.0f + (float)Server.rand.nextInt(6000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f, 0.0f, 0.0f, false, false);
                                }
                                Server.getInstance().broadCastAction(this.creature.getNameWithGenus() + " is pierced by the barrier.", this.creature, 2);
                                this.creature.getCommunicator().sendAlertServerMessage("You are pierced by the barrier!");
                                if (dead) {
                                    this.creature.achievement(143);
                                    return;
                                }
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    }
                    if (Tiles.decodeType(tile) == Tiles.Tile.TILE_LAVA.id) {
                        if (this.creature.getPower() < 1 && (this.creature.getDeity() == null || !this.creature.getDeity().isMountainGod() || this.creature.getFaith() < 35.0f) && this.creature.getFarwalkerSeconds() <= 0 && Server.rand.nextInt(10) == 0) {
                            Wound wound = null;
                            boolean dead = false;
                            try {
                                byte pos = 15;
                                if (Server.rand.nextBoolean()) {
                                    pos = 16;
                                }
                                if (Server.rand.nextInt(10) <= 6 && this.creature.getBody().getWounds() != null && (wound = this.creature.getBody().getWounds().getWoundAtLocation(pos)) != null) {
                                    dead = wound.modifySeverity((int)(1000.0f + (float)Server.rand.nextInt(4000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f));
                                    wound.setBandaged(false);
                                    this.creature.setWounded();
                                }
                                if (wound == null && this.creature.isPlayer()) {
                                    dead = this.creature.addWoundOfType(null, (byte)4, pos, false, 1.0f, true, 1000.0f + (float)Server.rand.nextInt(4000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f, 0.0f, 0.0f, false, false);
                                }
                                this.creature.getCommunicator().sendAlertServerMessage("You are burnt by lava!");
                                if (dead) {
                                    this.creature.achievement(142);
                                    return;
                                }
                            }
                            catch (Exception pos) {}
                        }
                    } else if (this.creature.getDeity() == null || !this.creature.getDeity().isForestGod() || this.creature.getFaith() < 35.0f) {
                        Tiles.Tile theTile;
                        if (this.creature.getPower() < 1 && ((theTile = Tiles.getTile(Tiles.decodeType(tile))).isNormalBush() || theTile.isMyceliumBush())) {
                            byte data = Tiles.decodeData(tile);
                            byte age = FoliageAge.getAgeAsByte(data);
                            if (theTile.isThorn(data) && age > FoliageAge.OLD_TWO.getAgeId() && this.creature.getFarwalkerSeconds() <= 0 && Server.rand.nextInt(10) == 0) {
                                Wound wound = null;
                                boolean dead = false;
                                try {
                                    byte pos = this.creature.getBody().getRandomWoundPos();
                                    if (Server.rand.nextInt(10) <= 6 && this.creature.getBody().getWounds() != null && (wound = this.creature.getBody().getWounds().getWoundAtLocation(pos)) != null) {
                                        dead = Tiles.getTile(Tiles.decodeType(tile)).isMyceliumBush() && this.creature.getKingdomTemplateId() == 3 ? wound.modifySeverity((int)(500.0f + (float)Server.rand.nextInt(2000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f)) : wound.modifySeverity((int)(1000.0f + (float)Server.rand.nextInt(4000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f));
                                        wound.setBandaged(false);
                                        this.creature.setWounded();
                                    }
                                    if (wound == null && WurmId.getType(this.creature.getWurmId()) == 0) {
                                        dead = Tiles.getTile(Tiles.decodeType(tile)).isMyceliumBush() && this.creature.getKingdomId() == 3 ? this.creature.addWoundOfType(null, (byte)2, pos, false, 1.0f, true, 500.0f + (float)Server.rand.nextInt(4000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f, 0.0f, 0.0f, false, false) : this.creature.addWoundOfType(null, (byte)2, pos, false, 1.0f, true, 500.0f + (float)Server.rand.nextInt(6000) * (100.0f - this.creature.getSpellDamageProtectBonus()) / 100.0f, 0.0f, 0.0f, false, false);
                                    }
                                    this.creature.getCommunicator().sendAlertServerMessage("You are pierced by the sharp thorns!");
                                    if (dead) {
                                        this.creature.achievement(143);
                                        return;
                                    }
                                }
                                catch (Exception exception) {}
                            }
                        }
                    } else if (this.creature.getDeity() != null) {
                        Tiles.Tile theTile;
                        if (this.creature.getDeity().isForestGod() && this.creature.getFaith() >= 70.0f) {
                            byte type = Tiles.decodeType(tile);
                            theTile = Tiles.getTile(type);
                            if (theTile.isNormalTree() || theTile.isMyceliumTree() || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id || type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_TUNDRA.id) {
                                weight = (int)((double)weight * 0.5);
                            }
                        } else if (this.creature.getDeity().isRoadProtector() && this.creature.getFaith() >= 60.0f && this.creature.getFavor() > 30.0f) {
                            byte type = Tiles.decodeType(tile);
                            if (Terraforming.isRoad(type)) {
                                weight = (int)((double)weight * 0.5);
                            }
                        } else if (this.creature.getDeity().isMountainGod() && this.creature.getFaith() >= 60.0f && this.creature.getFavor() > 30.0f) {
                            byte type = Tiles.decodeType(tile);
                            if (type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_SAND.id && this.creature.getKingdomId() == 2) {
                                weight = (int)((double)weight * 0.5);
                            }
                        } else if (this.creature.getDeity().isHateGod() && this.creature.getFaith() >= 60.0f && this.creature.getFavor() > 30.0f) {
                            byte type = Tiles.decodeType(tile);
                            theTile = Tiles.getTile(type);
                            if (type == Tiles.Tile.TILE_MYCELIUM.id || theTile.isMyceliumTree()) {
                                weight = (int)((double)weight * 0.5);
                            }
                        }
                    }
                    if (this.draggedItem != null) {
                        try {
                            if (!this.moveDraggedItem() || this.draggedItem == null) break block101;
                            vehic = Vehicles.getVehicleForId(this.draggedItem.getWurmId());
                            if (vehic != null) {
                                if (MovementScheme.vehic.creature) {
                                    try {
                                        cretVehicle = Server.getInstance().getCreature(MovementScheme.vehic.wurmid);
                                    }
                                    catch (NoSuchCreatureException type) {
                                    }
                                    catch (NoSuchPlayerException type) {}
                                } else {
                                    try {
                                        itemVehicle = Items.getItem(MovementScheme.vehic.wurmid);
                                    }
                                    catch (NoSuchItemException type) {
                                        // empty catch block
                                    }
                                }
                                MovementScheme.movePassengers(vehic, this.creature, MovementScheme.vehic.creature);
                            }
                            weight += this.draggedItem.getFullWeight() / 10;
                            if (moveDiff > 0.0) {
                                weight *= 2;
                            }
                        }
                        catch (NoSuchZoneException nsz) {
                            Items.stopDragging(this.draggedItem);
                        }
                    }
                }
                if (this.creature.isVehicleCommander()) {
                    vehic = Vehicles.getVehicleForId(this.creature.getVehicle());
                    if (vehic != null && !vehic.isChair()) {
                        this.moveVehicle(diffX, diffY, diffZ);
                    }
                    try {
                        weight += Items.getItem(this.creature.getVehicle()).getWeightGrams() / 100;
                    }
                    catch (NoSuchItemException noSuchItemException) {
                        // empty catch block
                    }
                }
                if (this.creature.getPower() < 2 && (double)this.creature.getPositionZ() < -1.3 && this.creature.getVehicle() == -10L) {
                    if (!this.hasWetFeet) {
                        this.hasWetFeet = true;
                        this.creature.achievement(70);
                    }
                    if (!PlonkData.SWIMMING.hasSeenThis(this.creature)) {
                        PlonkData.SWIMMING.trigger(this.creature);
                    }
                    moveDiff += 1.0;
                    weight += 20000;
                    if (this.creature.getStatus().getStamina() < 50 && !this.creature.isSubmerged() && !this.creature.isUndead() && Server.rand.nextInt(100) == 0) {
                        this.creature.addWoundOfType(null, (byte)7, 2, false, 1.0f, false, (4000.0f + Server.rand.nextFloat() * 3000.0f) * ItemBonus.getDrownDamReduction(this.creature), 0.0f, 0.0f, false, false);
                        this.creature.getCommunicator().sendAlertServerMessage("You are drowning!");
                    }
                }
            } else {
                if (this.getBitMask() == 0) {
                    this.creature.getStatus().setMoving(false);
                }
                if (this.creature.isVehicleCommander() && (vehic = Vehicles.getVehicleForId(this.creature.getVehicle())) != null) {
                    this.moveVehicle(diffX, diffY, diffZ);
                }
            }
            if ((beforeZMod > 0.0 || moveDiff > 0.0) && (this.creature.getVehicle() == -10L || this.isMovingVehicle() || this.draggedItem != null)) {
                this.creature.getStatus().setNormalRegen(false);
                if (moveDiff > 0.0) {
                    this.creature.getStatus().modifyStamina((int)(-moveDiff * (double)weight / 5000.0));
                }
            }
        }
    }

    public final void setHasSpiritSpeed(boolean hasSpeed) {
        this.hasSpiritSpeed = hasSpeed;
    }

    public float getSpeedModifier() {
        if (this.halted) {
            return 0.0f;
        }
        double bonus = 0.0;
        if (this.modifiers != null) {
            double webHurtModifier = 0.0;
            for (DoubleValueModifier lDoubleValueModifier : this.modifiers) {
                if (lDoubleValueModifier.getType() == 7) {
                    if (!(lDoubleValueModifier.getModifier() < webHurtModifier)) continue;
                    webHurtModifier = lDoubleValueModifier.getModifier();
                    continue;
                }
                bonus += lDoubleValueModifier.getModifier();
            }
            bonus += webHurtModifier;
        }
        if (bonus < -4.0) {
            return 0.0f;
        }
        if (this.encumbered) {
            return 0.05f;
        }
        if (this.hasSpiritSpeed) {
            bonus *= (double)1.05f;
        }
        return (float)Math.max((double)0.05f, (double)this.baseModifier + bonus);
    }

    public void setEncumbered(boolean enc) {
        this.encumbered = this.creature.getVehicle() != -10L ? false : enc;
    }

    public void setBaseModifier(float base) {
        this.baseModifier = this.creature.getVehicle() != -10L ? 1.0f : base;
        this.update();
    }

    public void update() {
        if (!this.creature.isPlayer()) {
            if (this.creature.isRidden()) {
                this.creature.forceMountSpeedChange();
            }
            return;
        }
        if (!this.halted) {
            this.sendSpeedModifier();
        }
    }

    public void haltSpeedModifier() {
        this.addSpeedMod(0.0f);
        this.creature.getCommunicator().sendSpeedModifier(0.0f);
        this.halted = true;
    }

    public void resumeSpeedModifier() {
        this.halted = false;
        this.sendSpeedModifier();
    }

    public void stopSendingSpeedModifier() {
        this.halted = true;
    }

    public Item getDraggedItem() {
        return this.draggedItem;
    }

    public void setDraggedItem(@Nullable Item dragged) {
        if (this.draggedItem != null && !this.draggedItem.equals(dragged)) {
            if (dragged != null) {
                Items.stopDragging(this.draggedItem);
            } else {
                this.creature.getCommunicator().sendNormalServerMessage("You stop dragging " + this.draggedItem.getNameWithGenus() + '.');
                if (this.draggedItem.getTemplateId() == 1125) {
                    this.removeModifier(ramDragMod);
                } else {
                    this.removeModifier(dragMod);
                }
            }
        }
        this.draggedItem = dragged;
        if (this.draggedItem != null) {
            this.creature.getCommunicator().sendNormalServerMessage("You start dragging " + this.draggedItem.getNameWithGenus() + '.');
            if (this.draggedItem.getTemplateId() == 1125) {
                this.addModifier(ramDragMod);
            } else {
                this.addModifier(dragMod);
            }
        }
    }

    public void setFightMoveMod(boolean fighting) {
        if (fighting) {
            this.addModifier(combatMod);
        } else {
            this.removeModifier(combatMod);
        }
    }

    public void setFarwalkerMoveMod(boolean add) {
        if (add) {
            this.addModifier(farwalkerMod);
        } else {
            this.removeModifier(farwalkerMod);
        }
    }

    public boolean setWebArmourMod(boolean add, float power) {
        if (add) {
            if (!this.webArmoured) {
                this.webArmoured = true;
                this.justWebSlowArmour = true;
                this.webArmourMod.setModifier(-power / 200.0f);
                this.addModifier(this.webArmourMod);
            }
        } else if (this.webArmoured) {
            if (this.justWebSlowArmour) {
                this.justWebSlowArmour = false;
            } else {
                this.webArmoured = false;
                this.removeModifier(this.webArmourMod);
                this.webArmourMod.setModifier(0.0);
            }
        }
        return this.webArmoured;
    }

    public double getWebArmourMod() {
        if (this.webArmoured) {
            return this.webArmourMod.getModifier();
        }
        return 0.0;
    }

    public void setChargeMoveMod(boolean add) {
        if (add) {
            this.addModifier(chargeMod);
        } else {
            this.removeModifier(chargeMod);
        }
    }

    public void setDrunkMod(boolean drunk) {
        if (drunk) {
            this.addModifier(drunkMod);
        } else {
            this.removeModifier(drunkMod);
        }
    }

    public void setMooredMod(boolean moored) {
        if (moored) {
            this.addModifier(mooreMod);
        } else {
            this.removeModifier(mooreMod);
        }
    }

    public void setStealthMod(boolean stealth) {
        if (stealth) {
            this.addModifier(this.stealthMod);
        } else {
            this.removeModifier(this.stealthMod);
        }
    }

    public void setFreezeMod(boolean frozen) {
        if (frozen) {
            this.addModifier(freezeMod);
        } else {
            this.removeModifier(freezeMod);
        }
    }

    private final float getDragDistanceMod(int templateId) {
        switch (templateId) {
            case 539: {
                return -2.0f;
            }
            case 853: 
            case 1410: {
                return -3.0f;
            }
        }
        return -1.5f;
    }

    protected boolean moveDraggedItem() throws NoSuchZoneException {
        int weight = this.draggedItem.getFullWeight(true);
        int left = this.creature.getCarryingCapacityLeft();
        if (this.draggedItem.getTemplateId() == 539 && weight < left || weight < left * 10) {
            float iposx = this.creature.getStatus().getPositionX();
            float iposy = this.creature.getStatus().getPositionY();
            float rot = this.creature.getStatus().getRotation();
            float oldPosZ = this.creature.getPositionZ();
            float distMod = this.getDragDistanceMod(this.draggedItem.getTemplateId());
            float xPosMod = (float)Math.sin(rot * ((float)Math.PI / 180)) * distMod;
            float yPosMod = -((float)Math.cos(rot * ((float)Math.PI / 180))) * distMod;
            float newPosX = iposx + xPosMod;
            float newPosY = iposy + yPosMod;
            if (!this.creature.isOnSurface() && (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2))) || Blocking.isDiagonalRockBetween(this.creature, (int)iposx >> 2, (int)iposy >> 2, (int)newPosX >> 2, (int)newPosY >> 2) != null)) {
                if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)iposx >> 2, (int)iposy >> 2)))) {
                    newPosX = iposx;
                    newPosY = iposy;
                } else {
                    Items.stopDragging(this.draggedItem);
                    return false;
                }
            }
            if (this.draggedItem.onBridge() > 0L) {
                VolaTile newTile;
                int ntx = (int)newPosX >> 2;
                int nty = (int)newPosY >> 2;
                if (!(this.draggedItem.getTileX() == ntx && this.draggedItem.getTileY() == nty || (newTile = Zones.getOrCreateTile(ntx, nty, this.draggedItem.isOnSurface())) != null && newTile.getStructure() != null && newTile.getStructure().getWurmId() == this.draggedItem.onBridge())) {
                    BridgePart[] bridgePartArray;
                    int n;
                    int n2;
                    BridgePart[] bridgeParts;
                    VolaTile oldTile = Zones.getOrCreateTile(this.draggedItem.getTileX(), this.draggedItem.getTileY(), this.draggedItem.isOnSurface());
                    boolean leavingOnSide = false;
                    if (oldTile != null && (bridgeParts = oldTile.getBridgeParts()) != null && (n2 = 0) < (n = (bridgePartArray = bridgeParts).length)) {
                        BridgePart bp = bridgePartArray[n2];
                        if ((bp.getDir() == 0 || bp.getDir() == 4) && this.draggedItem.getTileX() != ntx) {
                            leavingOnSide = true;
                        } else if ((bp.getDir() == 2 || bp.getDir() == 6) && this.draggedItem.getTileY() != nty) {
                            leavingOnSide = true;
                        }
                    }
                    if (leavingOnSide) {
                        newPosX = iposx;
                        newPosY = iposy;
                    }
                }
            }
            float newPosZ = Zones.calculatePosZ(newPosX, newPosY, null, this.draggedItem.isOnSurface(), false, oldPosZ, null, this.draggedItem.onBridge());
            float maxDepth = -6.0f;
            if (this.draggedItem.isVehicle() && !this.draggedItem.isBoat()) {
                Vehicle lVehicle = Vehicles.getVehicle(this.draggedItem);
                maxDepth = lVehicle.getMaxDepth();
            }
            if (this.draggedItem.isFloating() && (double)newPosZ > 0.3) {
                this.creature.getCommunicator().sendAlertServerMessage("The " + this.draggedItem.getName() + " gets stuck in the ground.", (byte)3);
                Items.stopDragging(this.draggedItem);
                return false;
            }
            if (!this.draggedItem.isFloating() && newPosZ < maxDepth && this.draggedItem.onBridge() <= 0L) {
                this.creature.getCommunicator().sendAlertServerMessage("The " + this.draggedItem.getName() + " gets stuck on the bottom.", (byte)3);
                Items.stopDragging(this.draggedItem);
                return false;
            }
            if (this.creature.isOnSurface() == this.draggedItem.isOnSurface()) {
                VolaTile t = Zones.getTileOrNull(this.draggedItem.getTileX(), this.draggedItem.getTileY(), this.draggedItem.isOnSurface());
                if (t == null) {
                    Items.stopDragging(this.draggedItem);
                    return false;
                }
                if (!this.draggedItem.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2))) && Tiles.decodeType(Server.caveMesh.getTile(this.draggedItem.getTileX(), this.draggedItem.getTileY())) != 201) {
                    return false;
                }
                t.moveItem(this.draggedItem, newPosX, newPosY, newPosZ, Creature.normalizeAngle(rot), this.creature.isOnSurface(), oldPosZ);
            } else {
                Seat[] seats;
                Vehicle vehicle;
                Zone zone = null;
                if (this.creature.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2)))) {
                    newPosX = iposx;
                    newPosY = iposy;
                }
                try {
                    zone = Zones.getZone(this.draggedItem.getTileX(), this.draggedItem.getTileY(), this.draggedItem.isOnSurface());
                    zone.removeItem(this.draggedItem);
                }
                catch (NoSuchZoneException oldTile) {
                    // empty catch block
                }
                this.draggedItem.setPosXYZ(newPosX, newPosY, newPosZ);
                this.draggedItem.newLayer = (byte)(this.creature.isOnSurface() ? 0 : -1);
                zone = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, this.creature.isOnSurface());
                zone.addItem(this.draggedItem, true, this.creature.isOnSurface(), false);
                this.draggedItem.newLayer = (byte)-128;
                if (this.draggedItem.isVehicle() && (vehicle = Vehicles.getVehicleForId(this.draggedItem.getWurmId())) != null && (seats = vehicle.getSeats()) != null) {
                    for (int x = 0; x < seats.length; ++x) {
                        if (seats[x] == null || seats[x].occupant == -10L) continue;
                        try {
                            Creature c = Server.getInstance().getCreature(seats[x].occupant);
                            c.setLayer(this.creature.getLayer(), false);
                            c.refreshVisible();
                            c.getCommunicator().attachCreature(-1L, this.draggedItem.getWurmId(), seats[x].offx, seats[x].offy, seats[x].offz, x);
                            continue;
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {
                            continue;
                        }
                        catch (NoSuchCreatureException noSuchCreatureException) {
                            // empty catch block
                        }
                    }
                }
            }
            return true;
        }
        this.creature.getCommunicator().sendNormalServerMessage("The " + this.draggedItem.getName() + " is too heavy.");
        Items.stopDragging(this.draggedItem);
        return false;
    }

    public void addModifier(DoubleValueModifier modifier) {
        if (!this.creature.isPlayer() && !this.creature.isVehicle()) {
            return;
        }
        if (this.modifiers == null) {
            this.modifiers = new HashSet<DoubleValueModifier>();
        }
        if (!this.modifiers.contains(modifier)) {
            this.modifiers.add(modifier);
        }
        this.update();
    }

    public void removeModifier(DoubleValueModifier modifier) {
        if (this.modifiers != null) {
            this.modifiers.remove(modifier);
        }
        this.update();
    }

    @Override
    public void valueChanged(double oldValue, double newValue) {
        this.update();
    }

    public final void touchFreeMoveCounter() {
        this.changedTileCounter = 5;
    }

    public final void decreaseFreeMoveCounter() {
        if (this.changedTileCounter > 0) {
            --this.changedTileCounter;
        }
    }

    @Override
    public int getMaxTargetGroundOffset(int suggestedOffset) {
        VolaTile t;
        if (this.creature.getPower() > 0) {
            return suggestedOffset;
        }
        float xPos = this.getCurrx();
        float yPos = this.getCurry();
        if (xPos == 0.0f && yPos == 0.0f) {
            xPos = this.getX();
            yPos = this.getY();
        }
        if ((t = Zones.getOrCreateTile((int)xPos / 4, (int)yPos / 4, this.getLayer() >= 0)) == null) {
            return 0;
        }
        int max = t.getMaxFloorLevel() * 30 + 30;
        return max;
    }

    public int getErrors() {
        return this.errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    static {
        toRemove = null;
    }
}


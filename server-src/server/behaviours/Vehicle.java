/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.RiteEvent;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ProtoConstants;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Vehicle
implements MiscConstants,
ProtoConstants,
TimeConstants {
    private static final Logger logger = Logger.getLogger(Vehicle.class.getName());
    static final Seat[] EMPTYSEATS = new Seat[0];
    public Seat[] seats = EMPTYSEATS;
    public Seat[] hitched = EMPTYSEATS;
    private float maxSpeed = 1.0f;
    private byte windImpact = 0;
    public boolean creature = false;
    String pilotName = "driver";
    public long pilotId = -10L;
    String embarkString = "embark";
    String embarksString = "embarks";
    public String name = "vehicle";
    public final long wurmid;
    public float maxDepth = -2500.0f;
    public float maxHeight = 2500.0f;
    public float maxHeightDiff = 2000.0f;
    public float skillNeeded = 20.1f;
    private int maxAllowedLoadDistance = 4;
    private boolean unmountable = false;
    private byte maxPassengers = 0;
    public Set<Creature> draggers = null;
    private boolean chair = false;
    private boolean bed = false;
    public byte commandType = 0;
    boolean canHaveEquipment = false;
    private ServerEntry destinationServer;
    public static final long plotCoursePvPCooldown = 1800000L;

    Vehicle(long aWurmId) {
        this.wurmid = aWurmId;
    }

    public boolean addDragger(Creature aCreature) {
        if (this.hitched.length > 0) {
            if (this.draggers == null) {
                this.draggers = new HashSet<Creature>();
            }
            if (this.draggers.size() < this.hitched.length) {
                if (this.draggers.add(aCreature)) {
                    for (int x = 0; x < this.hitched.length; ++x) {
                        if (this.hitched[x].occupant != -10L) continue;
                        this.hitched[x].setOccupant(aCreature.getWurmId());
                        if (this.getPilotId() > -10L) {
                            try {
                                Creature c = Server.getInstance().getCreature(this.getPilotId());
                                c.getMovementScheme().addMountSpeed(this.calculateNewVehicleSpeed(true));
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        return true;
                    }
                    logger.log(Level.WARNING, "error when adding to hitched seat - no free space.");
                    this.draggers.remove(aCreature);
                }
            } else {
                logger.log(Level.WARNING, "draggers.size=" + this.draggers.size() + ", hitched.length=" + this.hitched.length + " - no space");
            }
        }
        return false;
    }

    public void purgeDraggers() {
        if (this.draggers != null) {
            for (Creature dragger : this.draggers) {
                for (int x = 0; x < this.hitched.length; ++x) {
                    if (this.hitched[x].occupant != dragger.getWurmId()) continue;
                    this.hitched[x].setOccupant(-10L);
                    break;
                }
                dragger.setHitched(null, false);
                Server.getInstance().broadCastMessage(dragger.getName() + " stops dragging a " + this.getName() + ".", dragger.getTileX(), dragger.getTileY(), dragger.isOnSurface(), 5);
            }
            if (this.getPilotId() > -10L) {
                try {
                    Creature c = Server.getInstance().getCreature(this.getPilotId());
                    c.getMovementScheme().addMountSpeed(this.calculateNewVehicleSpeed(true));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public boolean removeDragger(Creature aCreature) {
        if (this.hitched.length > 0 && this.draggers != null && this.draggers.remove(aCreature)) {
            for (int x = 0; x < this.hitched.length; ++x) {
                if (this.hitched[x].occupant != aCreature.getWurmId()) continue;
                this.hitched[x].setOccupant(-10L);
                break;
            }
            aCreature.setHitched(null, false);
            String hitchedType = "stop dragging";
            if (!this.creature) {
                try {
                    Item dragged = Items.getItem(this.getWurmid());
                    if (dragged.isTent()) {
                        hitchedType = "is no longer hitched to";
                    }
                }
                catch (NoSuchItemException dragged) {
                    // empty catch block
                }
            }
            Server.getInstance().broadCastMessage(aCreature.getName() + " " + hitchedType + " a " + this.getName() + ".", aCreature.getTileX(), aCreature.getTileY(), aCreature.isOnSurface(), 5);
            if (this.getPilotId() > -10L) {
                try {
                    Creature c = Server.getInstance().getCreature(this.getPilotId());
                    c.getMovementScheme().addMountSpeed(this.calculateNewVehicleSpeed(true));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return true;
        }
        return false;
    }

    public void updateDraggedSpeed(boolean hitching) {
        if (this.hitched.length > 0 && this.draggers != null && this.getPilotId() > -10L) {
            try {
                Creature c = Server.getInstance().getCreature(this.getPilotId());
                c.getMovementScheme().addMountSpeed(this.calculateNewVehicleSpeed(hitching));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public Seat[] getHitched() {
        return this.hitched;
    }

    public void setHitched(Seat[] aHitched) {
        this.hitched = aHitched;
    }

    public float getMaxDepth() {
        return this.maxDepth;
    }

    public void setMaxDepth(float aMaxDepth) {
        this.maxDepth = aMaxDepth;
    }

    public float getMaxHeight() {
        return this.maxHeight;
    }

    public void setMaxHeight(float aMaxHeight) {
        this.maxHeight = aMaxHeight;
    }

    public float getMaxHeightDiff() {
        return this.maxHeightDiff;
    }

    public void setMaxHeightDiff(float aMaxHeightDiff) {
        this.maxHeightDiff = aMaxHeightDiff;
    }

    public float getSkillNeeded() {
        return this.skillNeeded;
    }

    public boolean getCanHaveEquipment() {
        return this.canHaveEquipment;
    }

    public void setSkillNeeded(float aSkillNeeded) {
        this.skillNeeded = aSkillNeeded;
    }

    public Set<Creature> getDraggers() {
        return this.draggers;
    }

    public boolean isDragger(Creature aCreature) {
        if (this.hitched.length > 0 && this.draggers != null) {
            return this.draggers.contains(aCreature);
        }
        return false;
    }

    public boolean hasHumanDragger() {
        if (this.draggers != null) {
            for (Creature dragger : this.draggers) {
                if (!dragger.isPlayer()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean mayAddDragger() {
        return this.hitched.length > 0 && (this.draggers == null || this.draggers.size() < this.hitched.length);
    }

    public void addHitchSeats(Seat[] hitchSeats) {
        this.hitched = hitchSeats == null ? EMPTYSEATS : hitchSeats;
    }

    void createPassengerSeats(int aNumber) {
        this.maxPassengers = (byte)aNumber;
        if (aNumber >= 0) {
            this.seats = new Seat[aNumber + 1];
            this.seats[0] = new Seat(0);
            for (int x = 1; x <= aNumber; ++x) {
                this.seats[x] = new Seat(1);
            }
        } else {
            logger.warning("Can only create a positive number of seats not " + aNumber);
        }
    }

    void createOnlyPassengerSeats(int aNumber) {
        if (aNumber >= 0) {
            this.seats = new Seat[aNumber];
            for (int x = 0; x < aNumber; ++x) {
                this.seats[x] = new Seat(1);
            }
        } else {
            logger.warning("Can only create a positive number of seats not " + aNumber);
        }
    }

    public byte getMaxPassengers() {
        return this.maxPassengers;
    }

    public boolean setSeatOffset(int aNumber, float aOffx, float aOffy, float aOffz) {
        if (aNumber > this.seats.length - 1 || aNumber < 0) {
            return false;
        }
        this.seats[aNumber].offx = aOffx;
        this.seats[aNumber].offy = aOffy;
        this.seats[aNumber].offz = aOffz;
        return true;
    }

    public boolean setSeatOffset(int aNumber, float aOffx, float aOffy, float aOffz, float aAltOffz) {
        if (aNumber > this.seats.length - 1 || aNumber < 0) {
            return false;
        }
        this.seats[aNumber].offx = aOffx;
        this.seats[aNumber].offy = aOffy;
        this.seats[aNumber].offz = aOffz;
        this.seats[aNumber].setAltOffz(aAltOffz);
        return true;
    }

    public boolean setSeatFightMod(int aNumber, float aCover, float aManouvre) {
        if (aNumber > this.seats.length - 1 || aNumber < 0) {
            return false;
        }
        this.seats[aNumber].cover = aCover;
        this.seats[aNumber].manouvre = aManouvre;
        return true;
    }

    public Seat getPilotSeat() {
        if (this.seats.length != 0 && this.seats[0].type == 0) {
            return this.seats[0];
        }
        return null;
    }

    public Seat getSeatFor(long aCreatureId) {
        for (int x = 0; x < this.seats.length; ++x) {
            if (this.seats[x].occupant != aCreatureId) continue;
            return this.seats[x];
        }
        return null;
    }

    public final int getSeatNumberFor(Seat seat) {
        for (int i = 0; i < this.seats.length; ++i) {
            if (this.seats[i].getId() != seat.getId()) continue;
            return i;
        }
        return -1;
    }

    public Seat getHitchSeatFor(long aCreatureId) {
        for (int x = 0; x < this.hitched.length; ++x) {
            if (this.hitched[x].occupant != aCreatureId) continue;
            return this.hitched[x];
        }
        return null;
    }

    public void kickAll() {
        for (int x = 0; x < this.seats.length; ++x) {
            this.seats[x].leave(this);
        }
        this.pilotId = -10L;
        this.pilotName = "";
    }

    public Seat[] getSeats() {
        return this.seats;
    }

    void setSeats(Seat[] aSeats) {
        this.seats = aSeats;
    }

    public final boolean isAnySeatOccupied() {
        return this.isAnySeatOccupied(true);
    }

    public final boolean isAnySeatOccupied(boolean countOffline) {
        if (this.seats != null) {
            for (int i = 0; i < this.seats.length; ++i) {
                if (!this.seats[i].isOccupied()) continue;
                if (!countOffline) {
                    try {
                        long occupantId = this.seats[i].getOccupant();
                        Player p = Players.getInstance().getPlayer(occupantId);
                        if (!p.isOffline()) continue;
                    }
                    catch (NoSuchPlayerException e) {}
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    public final boolean isAnythingHitched() {
        if (this.hitched != null) {
            for (int i = 0; i < this.hitched.length; ++i) {
                if (!this.hitched[i].isOccupied()) continue;
                return true;
            }
        }
        return false;
    }

    public byte calculateNewBoatSpeed(boolean disembarking) {
        int numsOccupied = 0;
        float qlMod = 0.0f;
        for (int x = 0; x < this.seats.length; ++x) {
            if (!this.seats[x].isOccupied()) continue;
            ++numsOccupied;
            try {
                long occupantId = this.seats[x].getOccupant();
                Player p = Players.getInstance().getPlayer(occupantId);
                if (!p.isOffline()) continue;
                --numsOccupied;
                continue;
            }
            catch (NoSuchPlayerException e) {
                --numsOccupied;
            }
        }
        try {
            Item itemVehicle = Items.getItem(this.wurmid);
            numsOccupied = Math.min(this.seats.length, numsOccupied + itemVehicle.getRarity());
            qlMod = Math.max(0.0f, itemVehicle.getCurrentQualityLevel() - 10.0f) / 90.0f;
            if (qlMod > 0.0f) {
                qlMod += 1.0f;
            }
        }
        catch (NoSuchItemException nsi) {
            return 0;
        }
        if (disembarking) {
            --numsOccupied;
        }
        float percentOccupied = 1.0f;
        percentOccupied = 1.0f + (float)numsOccupied / (float)this.seats.length;
        float maxSpeed = this.getMaxSpeed();
        if (RiteEvent.isActive(403)) {
            maxSpeed *= 2.0f;
        }
        if (Servers.localServer.PVPSERVER) {
            return (byte)Math.min(127.0f, percentOccupied * 9.0f * maxSpeed + qlMod * 3.0f * maxSpeed);
        }
        return (byte)Math.min(127.0f, percentOccupied * 3.0f * maxSpeed + qlMod * 9.0f * maxSpeed);
    }

    private final int getMinimumDraggers(Item vehicleItem) {
        if (vehicleItem == null) {
            return 0;
        }
        if (vehicleItem.getTemplateId() == 850) {
            return 2;
        }
        if (!vehicleItem.isBoat()) {
            return 1;
        }
        return 0;
    }

    public byte calculateNewVehicleSpeed(boolean hitching) {
        if (this.isChair()) {
            return 0;
        }
        if (this.hitched.length > 0) {
            boolean isWagon = false;
            int bisonCount = 0;
            if (this.draggers == null) {
                return 0;
            }
            double strength = 0.0;
            try {
                Item itemVehicle = Items.getItem(this.wurmid);
                strength = (float)itemVehicle.getRarity() * 0.1f;
                if (this.getDraggers().size() < this.getMinimumDraggers(itemVehicle)) {
                    return 0;
                }
                if (itemVehicle.getTemplateId() == 850) {
                    isWagon = true;
                }
            }
            catch (NoSuchItemException nsi) {
                return 0;
            }
            for (Creature next : this.draggers) {
                if (isWagon && next.getTemplate().getTemplateId() == 82) {
                    ++bisonCount;
                }
                strength += next.getStrengthSkill() / (double)(this.hitched.length * 10) * (double)next.getMountSpeedPercent(hitching);
            }
            return (byte)Math.min(127.0, 10.0 * strength * (double)this.getMaxSpeed() + (double)((float)(1 * bisonCount) * this.getMaxSpeed()));
        }
        return (byte)Math.min(127.0f, 10.0f * this.getMaxSpeed());
    }

    public byte calculateNewMountSpeed(Creature mount, boolean mounting) {
        double strength = mount.getMountSpeedPercent(mounting);
        if (mount.getTemplateId() == 64 && strength * (double)this.getMaxSpeed() >= 42.0 && this.getPilotId() != -10L) {
            Achievements.triggerAchievement(this.getPilotId(), 584);
        }
        return (byte)Math.max(0.0, Math.min(127.0, strength * (double)this.getMaxSpeed()));
    }

    float getMaxSpeed() {
        try {
            Item itemVehicle = Items.getItem(this.wurmid);
            if (itemVehicle != null && itemVehicle.getSpellEffects() != null) {
                float modifier = itemVehicle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_VEHCSPEED);
                return this.maxSpeed * modifier;
            }
        }
        catch (NoSuchItemException nsi) {
            return this.maxSpeed;
        }
        return this.maxSpeed;
    }

    void setMaxSpeed(float aMaxSpeed) {
        this.maxSpeed = aMaxSpeed;
    }

    public byte getWindImpact() {
        float modifier = 1.0f;
        try {
            Item itemVehicle = Items.getItem(this.wurmid);
            if (itemVehicle != null && itemVehicle.getSpellEffects() != null) {
                modifier = itemVehicle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_WIND);
            }
        }
        catch (NoSuchItemException noSuchItemException) {
            // empty catch block
        }
        return (byte)Math.min(127.0f, (float)this.windImpact * modifier);
    }

    void setWindImpact(byte impact) {
        this.windImpact = (byte)Math.min(127, impact);
    }

    public boolean isCreature() {
        return this.creature;
    }

    void setCreature(boolean aCreature) {
        this.creature = aCreature;
    }

    String getPilotName() {
        return this.pilotName;
    }

    void setPilotName(String aPilotName) {
        this.pilotName = aPilotName;
    }

    public long getPilotId() {
        return this.pilotId;
    }

    void setPilotId(long aPilotId) {
        this.pilotId = aPilotId;
    }

    String getEmbarkString() {
        return this.embarkString;
    }

    void setEmbarkString(String aEmbarkString) {
        this.embarkString = aEmbarkString;
    }

    String getName() {
        return this.name;
    }

    public static final String getVehicleName(Vehicle vehicle) {
        if (vehicle.isCreature()) {
            try {
                Creature mount = Creatures.getInstance().getCreature(vehicle.getWurmid());
                return mount.getName();
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, StringUtil.format("Unable to find creature with id: %d.", vehicle.getWurmid()), nsc);
            }
        }
        if (vehicle.isChair()) {
            try {
                Item chair = Items.getItem(vehicle.getWurmid());
                return chair.getName();
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, StringUtil.format("Unable to find item with id: %d.", vehicle.getWurmid()), nsi);
            }
        }
        return vehicle.getName();
    }

    void setName(String aName) {
        this.name = aName;
    }

    long getWurmid() {
        return this.wurmid;
    }

    public String toString() {
        StringBuilder lBuilder = new StringBuilder(200);
        lBuilder.append("Vehicle [id: ").append(this.wurmid);
        lBuilder.append(", Name: ").append(this.name);
        lBuilder.append(", PilotId: ").append(this.pilotId);
        lBuilder.append(", PilotName: ").append(this.pilotName);
        lBuilder.append(", MaxSpeed: ").append(this.getMaxSpeed());
        lBuilder.append(", EmbarkString: ").append(this.embarkString);
        lBuilder.append(", Creature: ").append(this.creature);
        lBuilder.append(']');
        return lBuilder.toString();
    }

    public int getSeatPosForPassenger(long _wurmid) {
        for (int x = 0; x < this.seats.length; ++x) {
            if (this.seats[x].occupant != _wurmid) continue;
            return x;
        }
        return -1;
    }

    public final int getFloorLevel() {
        if (this.creature) {
            try {
                return Server.getInstance().getCreature(this.wurmid).getFloorLevel();
            }
            catch (Exception ex) {
                return 0;
            }
        }
        try {
            return Items.getItem(this.wurmid).getFloorLevel();
        }
        catch (NoSuchItemException nsi) {
            return 0;
        }
    }

    public final float getPosZ() {
        if (this.creature) {
            try {
                return Server.getInstance().getCreature(this.wurmid).getPositionZ();
            }
            catch (Exception ex) {
                return 0.0f;
            }
        }
        try {
            return Items.getItem(this.wurmid).getPosZ();
        }
        catch (NoSuchItemException nsi) {
            return 0.0f;
        }
    }

    public boolean positionDragger(Creature dragger, Creature performer) {
        Item itemVehicle = null;
        try {
            itemVehicle = Items.getItem(this.wurmid);
        }
        catch (NoSuchItemException nsi) {
            return false;
        }
        for (int x = 0; x < this.hitched.length; ++x) {
            if (this.hitched[x].type != 2 || this.hitched[x].getOccupant() != dragger.getWurmId()) continue;
            float r = (-itemVehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
            float s = (float)Math.sin(r);
            float c = (float)Math.cos(r);
            Seat pilotSeat = this.getPilotSeat();
            float xo2 = pilotSeat == null ? 0.0f : s * -pilotSeat.offx - c * -pilotSeat.offy;
            float yo2 = pilotSeat == null ? 0.0f : c * -pilotSeat.offx + s * -pilotSeat.offy;
            float origposx = itemVehicle.getPosX() + xo2;
            float origposy = itemVehicle.getPosY() + yo2;
            origposx = Math.max(3.0f, origposx);
            origposx = Math.min(Zones.worldMeterSizeX - 3.0f, origposx);
            origposy = Math.max(3.0f, origposy);
            origposy = Math.min(Zones.worldMeterSizeY - 3.0f, origposy);
            float xo = s * -this.hitched[x].offx - c * -this.hitched[x].offy;
            float yo = c * -this.hitched[x].offx + s * -this.hitched[x].offy;
            float newposx = itemVehicle.getPosX() + xo;
            float newposy = itemVehicle.getPosY() + yo;
            if (itemVehicle.isTent()) {
                newposx = performer.getPosX();
                newposy = performer.getPosY();
            }
            newposx = Math.max(3.0f, newposx);
            newposx = Math.min(Zones.worldMeterSizeX - 3.0f, newposx);
            newposy = Math.max(3.0f, newposy);
            newposy = Math.min(Zones.worldMeterSizeY - 3.0f, newposy);
            int diffx = ((int)newposx >> 2) - ((int)origposx >> 2);
            int diffy = ((int)newposy >> 2) - ((int)origposy >> 2);
            boolean move = true;
            if (!(itemVehicle.isTent() || diffy == 0 && diffx == 0)) {
                Blocker first;
                BlockingResult result = Blocking.getBlockerBetween(dragger, origposx, origposy, newposx, newposy, dragger.getPositionZ(), dragger.getPositionZ(), dragger.getLayer() >= 0, dragger.getLayer() >= 0, false, 6, -1L, itemVehicle.getBridgeId(), itemVehicle.getBridgeId(), false);
                if (!(result == null || (first = result.getFirstBlocker()).isDoor() && first.canBeOpenedBy(dragger, false))) {
                    move = false;
                }
                if (move && dragger.getLayer() < 0 && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newposx >> 2, (int)newposy >> 2)))) {
                    move = false;
                }
            }
            if (!move) {
                newposx = origposx;
                newposy = origposy;
            }
            try {
                Zones.getZone(dragger.getCurrentTile().tilex, dragger.getCurrentTile().tiley, dragger.isOnSurface()).removeCreature(dragger, true, false);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, dragger.getWurmId() + "," + ex.getMessage(), ex);
            }
            dragger.getStatus().setPositionX(newposx);
            dragger.getStatus().setPositionY(newposy);
            dragger.setBridgeId(itemVehicle.getBridgeId());
            float z = Zones.calculatePosZ(newposx, newposy, Zones.getTileOrNull(dragger.getTilePos(), dragger.isOnSurface()), dragger.isOnSurface(), false, dragger.getStatus().getPositionZ(), dragger, dragger.getBridgeId());
            dragger.getMovementScheme().setPosition(dragger.getStatus().getPositionX(), dragger.getStatus().getPositionY(), z, dragger.getStatus().getRotation(), dragger.getLayer());
            dragger.destroyVisionArea();
            try {
                Zones.getZone(dragger.getTileX(), dragger.getTileY(), dragger.isOnSurface()).addCreature(dragger.getWurmId());
                dragger.createVisionArea();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, dragger.getWurmId() + "," + ex.getMessage(), ex);
            }
            return true;
        }
        return false;
    }

    public boolean isUnmountable() {
        return this.unmountable;
    }

    public void setUnmountable(boolean aUnmountable) {
        this.unmountable = aUnmountable;
    }

    public boolean isChair() {
        return this.chair;
    }

    public void setChair(boolean isChair) {
        this.chair = isChair;
    }

    public boolean isBed() {
        return this.bed;
    }

    public void setBed(boolean isBed) {
        this.bed = isBed;
    }

    public int getMaxAllowedLoadDistance() {
        return this.maxAllowedLoadDistance;
    }

    public void setMaxAllowedLoadDistance(int newMaxDist) {
        this.maxAllowedLoadDistance = newMaxDist;
    }

    public ServerEntry getDestinationServer() {
        return this.destinationServer;
    }

    public boolean hasDestinationSet() {
        return this.destinationServer != null;
    }

    public void setDestination(ServerEntry entry) {
        this.destinationServer = entry;
    }

    public void clearDestination() {
        this.destinationServer = null;
    }

    public void alertPassengerOfEnemies(Creature performer, ServerEntry entry, boolean embarking) {
        if (entry.PVPSERVER && (!entry.EPIC || Server.getInstance().isPS()) || entry.isChaosServer()) {
            int pKingdom = ((Player)performer).getSaveFile().getChaosKingdom() == 0 ? 4 : (int)((Player)performer).getSaveFile().getChaosKingdom();
            for (Seat lSeat : this.seats) {
                int oKingdom;
                PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant());
                if (oInfo == null) continue;
                int n = oKingdom = oInfo.getChaosKingdom() == 0 ? 4 : (int)oInfo.getChaosKingdom();
                if (oKingdom == pKingdom) continue;
                performer.getCommunicator().sendAlertServerMessage("Warning: " + oInfo.getName() + " will be an enemy when you cross into " + entry.getName() + "!");
                if (!embarking) continue;
                try {
                    Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                    oPlayer.getCommunicator().sendAlertServerMessage("Warning: " + performer.getName() + " will be an enemy when you cross into " + entry.getName() + "!");
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }

    public void alertAllPassengersOfEnemies(ServerEntry entry) {
        for (Seat lSeat : this.seats) {
            PlayerInfo oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant());
            if (oInfo == null) continue;
            try {
                Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                this.alertPassengerOfEnemies(oPlayer, entry, false);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public void notifyAllPassengers(String message, boolean includeDriver, boolean alert) {
        for (Seat lSeat : this.seats) {
            PlayerInfo oInfo;
            if (!includeDriver && lSeat == this.getPilotSeat() || (oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant())) == null) continue;
            try {
                Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                if (alert) {
                    oPlayer.getCommunicator().sendAlertServerMessage(message);
                    continue;
                }
                oPlayer.getCommunicator().sendNormalServerMessage(message);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public void alertPassengersOfKingdom(ServerEntry entry, boolean includeDriver) {
        for (Seat lSeat : this.seats) {
            PlayerInfo oInfo;
            if (!includeDriver && lSeat == this.getPilotSeat() || (oInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lSeat.getOccupant())) == null) continue;
            byte oKingdom = oInfo.getChaosKingdom() == 0 ? (byte)4 : (byte)oInfo.getChaosKingdom();
            try {
                String kingdomMsg;
                Player oPlayer = Players.getInstance().getPlayer(oInfo.wurmId);
                if (!Server.getInstance().isPS() && entry.isChaosServer() || entry.PVPSERVER && !Servers.localServer.PVPSERVER) {
                    kingdomMsg = "This course will take you into hostile territory";
                    if (oKingdom != oPlayer.getKingdomId()) {
                        kingdomMsg = kingdomMsg + ", and you will join the " + Kingdoms.getNameFor(oKingdom) + " kingdom until you return";
                    }
                    oPlayer.getCommunicator().sendAlertServerMessage(kingdomMsg + ".");
                } else if (!Server.getInstance().isPS() && Servers.localServer.isChaosServer() || Servers.localServer.PVPSERVER && entry.HOMESERVER && !entry.PVPSERVER) {
                    kingdomMsg = "This course will take you into friendly territory";
                    if (oKingdom != entry.getKingdom()) {
                        kingdomMsg = kingdomMsg + ", and you will join the " + Kingdoms.getNameFor(entry.getKingdom()) + " kingdom until you return";
                    }
                    oPlayer.getCommunicator().sendNormalServerMessage(kingdomMsg + ".");
                }
                if (!entry.PVPSERVER || Servers.localServer.PVPSERVER || oPlayer.getDeity() == null || QuestionParser.doesKingdomTemplateAcceptDeity(Kingdoms.getKingdomTemplateFor(oKingdom), oPlayer.getDeity())) continue;
                oPlayer.getCommunicator().sendAlertServerMessage("Warning: " + oPlayer.getDeity().getName() + " does not align with your kingdom of " + Kingdoms.getNameFor(oKingdom) + ". If you continue travel to " + entry.getName() + " you will lose all faith and abilities granted by your deity.");
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public boolean checkPassengerPermissions(Creature performer) {
        boolean toReturn;
        block7: {
            toReturn = false;
            if (!Servers.localServer.PVPSERVER) {
                try {
                    Item ivehic = Items.getItem(this.wurmid);
                    if (!ivehic.isGuest(performer) || !ivehic.mayCommand(performer)) {
                        performer.getCommunicator().sendNormalServerMessage("You may not leave the server with this boat. You need to be explicitly specified in the boat's permissions.");
                        toReturn = true;
                        break block7;
                    }
                    for (Seat seat : this.getSeats()) {
                        if (!seat.isOccupied() || seat.type != 1 || ivehic.isGuest(seat.getOccupant())) continue;
                        try {
                            Creature c = Server.getInstance().getCreature(seat.occupant);
                            if (ivehic.mayPassenger(c)) continue;
                            performer.getCommunicator().sendNormalServerMessage("You may not leave the server with this boat as one of your passengers will not have passenger permission on new server.");
                            toReturn = true;
                            break;
                        }
                        catch (NoSuchPlayerException | NoSuchCreatureException wurmServerException) {
                            // empty catch block
                        }
                    }
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
            }
        }
        return !toReturn;
    }

    public void touchPlotCourseCooldowns() {
        this.touchPlotCourseCooldowns(1800000L);
    }

    public void touchPlotCourseCooldowns(long cooldown) {
        for (Seat seat : this.getSeats()) {
            Cooldowns cd = Cooldowns.getCooldownsFor(seat.getOccupant(), true);
            cd.addCooldown(717, System.currentTimeMillis() + cooldown, false);
        }
    }

    public long getPlotCourseCooldowns() {
        long currentTimer = 0L;
        for (Seat seat : this.getSeats()) {
            long remain;
            Cooldowns cd = Cooldowns.getCooldownsFor(seat.getOccupant(), false);
            if (cd == null || (remain = cd.isAvaibleAt(717)) <= currentTimer) continue;
            currentTimer = remain;
        }
        return currentTimer;
    }

    public String checkCourseRestrictions() {
        long currentTimer = 0L;
        for (Seat seat : this.getSeats()) {
            long remain;
            try {
                Player p = Players.getInstance().getPlayer(seat.getOccupant());
                if ((p.isFighting() || p.getEnemyPresense() > 0) && p.getSecondsPlayed() > 300.0f) {
                    return "There are enemies in the vicinity. You fail to focus on a course.";
                }
            }
            catch (NoSuchPlayerException p) {
                // empty catch block
            }
            Cooldowns cd = Cooldowns.getCooldownsFor(seat.getOccupant(), false);
            if (cd == null || (remain = cd.isAvaibleAt(717)) <= currentTimer) continue;
            currentTimer = remain;
        }
        if (currentTimer > 0L) {
            return "You must wait another " + Server.getTimeFor(currentTimer) + " to plot a course.";
        }
        return "";
    }

    public boolean isPvPBlocking() {
        for (Seat lSeat : this.seats) {
            try {
                Player oPlayer = Players.getInstance().getPlayer(lSeat.getOccupant());
                if (!oPlayer.isBlockingPvP()) continue;
                return true;
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
        return false;
    }
}


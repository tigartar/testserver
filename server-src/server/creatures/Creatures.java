/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.BodyFactory;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Npc;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Creatures
implements MiscConstants,
CreatureTemplateIds,
TimeConstants {
    private final Map<Long, Creature> creatures;
    private final Map<Long, Creature> offlineCreatures;
    private final ConcurrentHashMap<Long, Creature> avatars;
    private final Map<Long, Long> protectedCreatures = new ConcurrentHashMap<Long, Long>();
    private final Map<String, Creature> npcs = new ConcurrentHashMap<String, Creature>();
    private final Map<Integer, Integer> creaturesByType;
    private static Creatures instance = null;
    private static Logger logger = Logger.getLogger(Creatures.class.getName());
    private static final String getAllCreatures = "SELECT * FROM CREATURES";
    private static final String COUNT_CREATURES = "SELECT COUNT(*) FROM CREATURES";
    private static final String DELETE_CREATURE = "DELETE FROM CREATURES WHERE WURMID=?";
    private static final String DELETE_CREATUREBODY = "DELETE FROM BODYPARTS WHERE OWNERID=?";
    private static final String DELETE_CREATURESKILLS = "DELETE FROM SKILLS WHERE OWNER=?";
    private static final String DELETE_CREATUREITEMS = "DELETE FROM ITEMS WHERE OWNERID=?";
    private static final String DELETE_CREATURE_SPLIT = "DELETE FROM CREATURES_BASE WHERE WURMID=?";
    private static final String DELETE_CREATURE_POS_SPLIT = "DELETE FROM CREATURES_POS WHERE WURMID=?";
    private static final String DELETE_PROT_CREATURE = "DELETE FROM PROTECTED WHERE WURMID=?";
    private static final String INSERT_PROT_CREATURE = "INSERT INTO PROTECTED (WURMID,PLAYERID) VALUES(?,?)";
    private static final String LOAD_PROT_CREATURES = "SELECT * FROM PROTECTED";
    private static final boolean fixColourTraits = false;
    private final Map<Long, Brand> brandedCreatures = new ConcurrentHashMap<Long, Brand>();
    private final Map<Long, Long> ledCreatures = new ConcurrentHashMap<Long, Long>();
    private static Map<Long, Creature> rideCreatures;
    private final Timer creaturePollThread;
    private final PollTimerTask pollTask;
    private int numberOfNice = 0;
    private int numberOfAgg = 0;
    private int numberOfTyped = 0;
    private int kingdomCreatures = 0;
    private static int destroyedCaveCrets;
    private static boolean loading;
    private static int nums;
    private static int seaMonsters;
    private static int seaHunters;
    private int currentCreature = 0;
    private Creature[] crets;
    public int numberOfZonesX = 64;
    private long totalTime = 0L;
    private long startTime = 0L;
    private boolean logCreaturePolls = false;

    public static Creatures getInstance() {
        if (instance == null) {
            instance = new Creatures();
        }
        return instance;
    }

    public int getNumberOfCreatures() {
        return this.creatures.size();
    }

    public int getNumberOfCreaturesWithTemplate(int templateChecked) {
        int toReturn = 0;
        for (Creature cret : this.creatures.values()) {
            if (cret.getTemplate().getTemplateId() != templateChecked) continue;
            ++toReturn;
        }
        return toReturn;
    }

    public final void setLastLed(long creatureLed, long leader) {
        this.ledCreatures.put(creatureLed, leader);
    }

    public final boolean wasLastLed(long potentialLeader, long creatureLed) {
        Long lastLeader = this.ledCreatures.get(creatureLed);
        if (lastLeader != null) {
            return lastLeader == potentialLeader;
        }
        return false;
    }

    public final void addBrand(Brand brand) {
        this.brandedCreatures.put(brand.getCreatureId(), brand);
    }

    public final void setBrand(long creatureId, long brandid) {
        if (brandid <= 0L) {
            this.brandedCreatures.remove(creatureId);
        } else {
            Brand brand = this.brandedCreatures.get(creatureId);
            if (brand == null) {
                brand = new Brand(creatureId, System.currentTimeMillis(), brandid, false);
            } else {
                brand.setBrandId(brandid);
            }
            this.brandedCreatures.put(creatureId, brand);
        }
    }

    public final Brand getBrand(long creatureId) {
        Brand brand = this.brandedCreatures.get(creatureId);
        return brand;
    }

    public final boolean isBrandedBy(long creatureId, long brandId) {
        Brand brand = this.brandedCreatures.get(creatureId);
        if (brand != null) {
            return brand.getBrandId() == brandId;
        }
        return false;
    }

    public final Creature[] getBranded(long villageId) {
        ConcurrentHashMap<Long, Brand> removeMap = new ConcurrentHashMap<Long, Brand>();
        HashSet<Creature> brandedSet = new HashSet<Creature>();
        for (Brand b : this.brandedCreatures.values()) {
            if (b.getBrandId() != villageId) continue;
            try {
                brandedSet.add(this.getCreature(b.getCreatureId()));
            }
            catch (NoSuchCreatureException e) {
                Long cid = new Long(b.getCreatureId());
                if (this.isCreatureOffline(cid)) {
                    Creature creature = this.offlineCreatures.get(cid);
                    brandedSet.add(creature);
                    continue;
                }
                removeMap.put(b.getCreatureId(), b);
            }
        }
        return brandedSet.toArray(new Creature[brandedSet.size()]);
    }

    public void removeBrandingFor(int villageId) {
        for (Brand b : this.brandedCreatures.values()) {
            if (b.getBrandId() != (long)villageId) continue;
            b.deleteBrand();
        }
    }

    public int getNumberOfNice() {
        return this.numberOfNice;
    }

    public int getNumberOfAgg() {
        return this.numberOfAgg;
    }

    public int getNumberOfTyped() {
        return this.numberOfTyped;
    }

    public int getNumberOfKingdomCreatures() {
        return this.kingdomCreatures;
    }

    public int getNumberOfSeaMonsters() {
        return seaMonsters;
    }

    public int getNumberOfSeaHunters() {
        return seaHunters;
    }

    private Creatures() {
        int numberOfCreaturesInDatabase = Math.max(this.getNumberOfCreaturesInDatabase(), 100);
        this.creatures = new ConcurrentHashMap<Long, Creature>(numberOfCreaturesInDatabase);
        this.avatars = new ConcurrentHashMap();
        this.creaturesByType = new ConcurrentHashMap<Integer, Integer>(numberOfCreaturesInDatabase);
        this.offlineCreatures = new ConcurrentHashMap<Long, Creature>();
        this.creaturePollThread = new Timer();
        this.pollTask = new PollTimerTask();
    }

    public final void startPollTask() {
    }

    public final void shutDownPolltask() {
        this.pollTask.shutDown();
    }

    public void sendOfflineCreatures(Communicator c, boolean showOwner) {
        for (Creature cret : this.offlineCreatures.values()) {
            String dominatorName = " dominator=" + cret.dominator;
            if (showOwner) {
                try {
                    PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(cret.dominator);
                    if (p != null) {
                        dominatorName = " dominator=" + p.getName();
                    }
                }
                catch (Exception exception) {}
            } else {
                dominatorName = "";
            }
            c.sendNormalServerMessage(cret.getName() + " at " + cret.getPosX() / 4.0f + ", " + cret.getPosY() / 4.0f + " loyalty " + cret.getLoyalty() + dominatorName);
        }
    }

    public void setCreatureDead(Creature dead) {
        long deadid = dead.getWurmId();
        for (Creature creature : this.creatures.values()) {
            if (creature.opponent == dead) {
                creature.setOpponent(null);
            }
            if (creature.target == deadid) {
                creature.setTarget(-10L, true);
            }
            creature.removeTarget(deadid);
        }
        Vehicles.removeDragger(dead);
    }

    public void combatRound() {
        for (Creature lCreature : this.creatures.values()) {
            lCreature.getCombatHandler().clearRound();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getNumberOfCreaturesInDatabase() {
        int numberOfCreatures;
        ResultSet rs;
        Statement stmt;
        block4: {
            stmt = null;
            rs = null;
            numberOfCreatures = 0;
            try {
                Connection dbcon = DbConnector.getCreatureDbCon();
                stmt = dbcon.createStatement();
                rs = stmt.executeQuery(COUNT_CREATURES);
                if (!rs.next()) break block4;
                numberOfCreatures = rs.getInt(1);
            }
            catch (SQLException e) {
                try {
                    logger.log(Level.WARNING, "Failed to count creatures:" + e.getMessage(), e);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(stmt, rs);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(stmt, rs);
            }
        }
        DbUtilities.closeDatabaseObjects(stmt, rs);
        return numberOfCreatures;
    }

    private final void loadMoreStuff(Creature toReturn) {
        try {
            toReturn.getBody().createBodyParts();
            Items.loadAllItemsForNonPlayer(toReturn, toReturn.getStatus().getInventoryId());
            Village v = Villages.getVillageForCreature(toReturn);
            if (v == null && toReturn.isNpcTrader() && toReturn.getName().startsWith("Trader") && (v = Villages.getVillage(toReturn.getTileX(), toReturn.getTileY(), true)) != null) {
                try {
                    logger.log(Level.INFO, "Adding " + toReturn.getName() + " as citizen to " + v.getName());
                    v.addCitizen(toReturn, v.getRoleForStatus((byte)3));
                }
                catch (IOException iox) {
                    logger.log(Level.INFO, iox.getMessage());
                }
                catch (NoSuchRoleException nsx) {
                    logger.log(Level.INFO, nsx.getMessage());
                }
            }
            toReturn.setCitizenVillage(v);
            toReturn.postLoad();
            if (toReturn.getTemplate().getTemplateId() == 46 || toReturn.getTemplate().getTemplateId() == 47) {
                Zones.setHasLoadedChristmas(true);
                if (!WurmCalendar.isChristmas()) {
                    this.permanentlyDelete(toReturn);
                } else if (toReturn.getTemplate().getTemplateId() == 46) {
                    if (!Servers.localServer.HOMESERVER && toReturn.getKingdomId() == 2) {
                        Zones.santaMolRehan = toReturn;
                    } else if (Servers.localServer.HOMESERVER && toReturn.getKingdomId() == 4) {
                        Zones.santas.put(toReturn.getWurmId(), toReturn);
                    } else {
                        Zones.santa = toReturn;
                    }
                } else {
                    Zones.evilsanta = toReturn;
                }
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private static final void initializeCreature(String templateName, ResultSet rs, Creature statusHolder) {
        long id = statusHolder.getWurmId();
        statusHolder.getStatus().setPosition(CreaturePos.getPosition(id));
        try {
            statusHolder.getStatus().setStatusExists(true);
            statusHolder.template = statusHolder.getStatus().template = CreatureTemplateFactory.getInstance().getTemplate(templateName);
            statusHolder.getStatus().bodyId = rs.getLong("BODYID");
            statusHolder.getStatus().body = BodyFactory.getBody(statusHolder, statusHolder.getStatus().template.getBodyType(), statusHolder.getStatus().template.getCentimetersHigh(), statusHolder.getStatus().template.getCentimetersLong(), statusHolder.getStatus().template.getCentimetersWide());
            statusHolder.getStatus().body.setCentimetersLong(rs.getShort("CENTIMETERSLONG"));
            statusHolder.getStatus().body.setCentimetersHigh(rs.getShort("CENTIMETERSHIGH"));
            statusHolder.getStatus().body.setCentimetersWide(rs.getShort("CENTIMETERSWIDE"));
            statusHolder.getStatus().sex = rs.getByte("SEX");
            statusHolder.getStatus().modtype = rs.getByte("TYPE");
            String name = rs.getString("NAME");
            statusHolder.setName(name);
            statusHolder.getStatus().inventoryId = rs.getLong("INVENTORYID");
            statusHolder.getStatus().stamina = rs.getShort("STAMINA") & 0xFFFF;
            statusHolder.getStatus().hunger = rs.getShort("HUNGER") & 0xFFFF;
            statusHolder.getStatus().thirst = rs.getShort("THIRST") & 0xFFFF;
            statusHolder.getStatus().buildingId = rs.getLong("BUILDINGID");
            statusHolder.getStatus().kingdom = rs.getByte("KINGDOM");
            statusHolder.getStatus().dead = rs.getBoolean("DEAD");
            statusHolder.getStatus().stealth = rs.getBoolean("STEALTH");
            statusHolder.getStatus().age = rs.getInt("AGE");
            statusHolder.getStatus().fat = rs.getByte("FAT");
            statusHolder.getStatus().lastPolledAge = rs.getLong("LASTPOLLEDAGE");
            statusHolder.dominator = rs.getLong("DOMINATOR");
            statusHolder.getStatus().reborn = rs.getBoolean("REBORN");
            statusHolder.getStatus().loyalty = rs.getFloat("LOYALTY");
            statusHolder.getStatus().lastPolledLoyalty = rs.getLong("LASTPOLLEDLOYALTY");
            statusHolder.getStatus().detectInvisCounter = rs.getShort("DETECTIONSECS");
            statusHolder.getStatus().traits = rs.getLong("TRAITS");
            if (statusHolder.getStatus().traits != 0L) {
                statusHolder.getStatus().setTraitBits(statusHolder.getStatus().traits);
            }
            statusHolder.getStatus().mother = rs.getLong("MOTHER");
            statusHolder.getStatus().father = rs.getLong("FATHER");
            statusHolder.getStatus().nutrition = rs.getFloat("NUTRITION");
            statusHolder.getStatus().disease = rs.getByte("DISEASE");
            if (statusHolder.getStatus().buildingId != -10L) {
                try {
                    Structure struct = Structures.getStructure(statusHolder.getStatus().buildingId);
                    if (!struct.isFinalFinished()) {
                        statusHolder.setStructure(struct);
                    } else {
                        statusHolder.getStatus().buildingId = -10L;
                    }
                }
                catch (NoSuchStructureException nss) {
                    statusHolder.getStatus().buildingId = -10L;
                    logger.log(Level.INFO, "Could not find structure for " + statusHolder.getName());
                    statusHolder.setStructure(null);
                }
            }
            statusHolder.getStatus().lastGroomed = rs.getLong("LASTGROOMED");
            statusHolder.getStatus().offline = rs.getBoolean("OFFLINE");
            statusHolder.getStatus().stayOnline = rs.getBoolean("STAYONLINE");
            String petName = rs.getString("PETNAME");
            statusHolder.setPetName(petName);
            statusHolder.calculateSize();
            statusHolder.vehicle = rs.getLong("VEHICLE");
            statusHolder.seatType = rs.getByte("SEAT_TYPE");
            if (statusHolder.vehicle > 0L) {
                rideCreatures.put(id, statusHolder);
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to load creature " + id + " " + ex.getMessage(), ex);
        }
    }

    public int loadAllCreatures() throws NoSuchCreatureException {
        Brand.loadAllBrands();
        loading = true;
        Offspring.loadAllOffspring();
        this.loadAllProtectedCreatures();
        long lNow2 = System.nanoTime();
        logger.info("Loading all skills for creatures");
        try {
            Skills.loadAllCreatureSkills();
        }
        catch (Exception ex) {
            logger.log(Level.INFO, "Failed Loading creature skills.", ex);
            System.exit(0);
        }
        logger.log(Level.INFO, "Loaded creature skills. That took " + (float)(System.nanoTime() - lNow2) / 1000000.0f);
        logger.info("Loading Creatures");
        long lNow = System.nanoTime();
        long cpS = 0L;
        long cpOne = 0L;
        long cpTwo = 0L;
        long cpThree = 0L;
        long cpFour = 0L;
        Creature toReturn = null;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        HashSet<Creature> toRemove = new HashSet<Creature>();
        rideCreatures = new ConcurrentHashMap<Long, Creature>();
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(getAllCreatures);
            rs = ps.executeQuery();
            while (rs.next()) {
                cpS = System.nanoTime();
                try {
                    String templateName = rs.getString("TEMPLATENAME");
                    toReturn = templateName.equalsIgnoreCase("human") || templateName.equalsIgnoreCase("npc human") ? new Npc(rs.getLong("WURMID")) : new Creature(rs.getLong("WURMID"));
                    Creatures.initializeCreature(templateName, rs, toReturn);
                    toReturn.loadTemplate();
                    if (toReturn.isFish()) {
                        logger.info("Fish removed " + toReturn.getName());
                        this.permanentlyDelete(toReturn);
                    } else if (!toReturn.isUnique() && (toReturn.isOffline() || toReturn.isDominated()) && !toReturn.isStayonline() || !Constants.loadNpcs && toReturn.isNpc()) {
                        this.addOfflineCreature(toReturn);
                        this.addCreature(toReturn, true);
                        toRemove.add(toReturn);
                    } else if (!this.addCreature(toReturn, false)) {
                        this.permanentlyDelete(toReturn);
                    }
                    cpOne += System.nanoTime() - cpS;
                    cpS = System.nanoTime();
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to load creature: " + toReturn + "due to " + ex.getMessage(), ex);
                }
            }
            block11: for (Creature rider : rideCreatures.values()) {
                long vehicleId = rider.vehicle;
                byte seatType = rider.seatType;
                rider.vehicle = -10L;
                rider.seatType = (byte)-1;
                try {
                    Vehicle vehic = null;
                    Item vehicle = null;
                    Creature creature = null;
                    if (WurmId.getType(vehicleId) == 1) {
                        creature = Server.getInstance().getCreature(vehicleId);
                        vehic = Vehicles.getVehicle(creature);
                    } else {
                        vehicle = Items.getItem(vehicleId);
                        vehic = Vehicles.getVehicle(vehicle);
                    }
                    if (vehic == null) continue;
                    if (seatType == -1 || seatType == 2) {
                        if (!vehic.addDragger(rider)) continue;
                        rider.setHitched(vehic, true);
                        Seat driverseat = vehic.getPilotSeat();
                        if (driverseat == null) continue;
                        float _r = (-vehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                        float _s = (float)Math.sin(_r);
                        float _c = (float)Math.cos(_r);
                        float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                        float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                        float nPosX = rider.getStatus().getPositionX() - xo;
                        float nPosY = rider.getStatus().getPositionY() - yo;
                        float nPosZ = rider.getStatus().getPositionZ() - driverseat.offz;
                        rider.getStatus().setPositionX(nPosX);
                        rider.getStatus().setPositionY(nPosY);
                        rider.getStatus().setRotation(-vehicle.getRotation() + 180.0f);
                        rider.getMovementScheme().setPosition(rider.getStatus().getPositionX(), rider.getStatus().getPositionY(), nPosZ, rider.getStatus().getRotation(), rider.getLayer());
                        continue;
                    }
                    if (seatType != 0 && seatType != 1) continue;
                    for (int x = 0; x < vehic.seats.length; ++x) {
                        if (vehic.seats[x].getType() != seatType || vehic.seats[x].isOccupied() && vehic.seats[x].occupant != rider.getWurmId()) continue;
                        vehic.seats[x].occupy(vehic, rider);
                        if (seatType == 0) {
                            vehic.pilotId = rider.getWurmId();
                            rider.setVehicleCommander(true);
                        }
                        MountAction m = new MountAction(creature, vehicle, vehic, x, seatType == 0, vehic.seats[x].offz);
                        rider.setMountAction(m);
                        rider.setVehicle(vehicleId, true, seatType);
                        continue block11;
                    }
                }
                catch (NoSuchItemException | NoSuchPlayerException | NoSuchCreatureException nsi) {
                    logger.log(Level.INFO, "Item " + vehicleId + " missing for hitched " + rider.getWurmId() + " " + rider.getName());
                }
            }
            rideCreatures = null;
            long lNow1 = System.nanoTime();
            logger.info("Loading all items for creatures");
            Items.loadAllCreatureItems();
            logger.log(Level.INFO, "Loaded creature items. That took " + (float)(System.nanoTime() - lNow1) / 1000000.0f + " ms for " + Items.getNumItems() + " items and " + Items.getNumCoins() + " coins.");
            for (Creature creature : this.creatures.values()) {
                Skills.fillCreatureTempSkills(creature);
                this.loadMoreStuff(creature);
            }
            for (Creature creature : toRemove) {
                this.loadMoreStuff(creature);
                this.removeCreature(creature);
                creature.getStatus().offline = true;
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load creatures:" + sqx.getMessage(), sqx);
                throw new NoSuchCreatureException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                logger.log(Level.INFO, "Loaded " + this.getNumberOfCreatures() + " creatures. Destroyed " + destroyedCaveCrets + ". That took " + (float)(System.nanoTime() - lNow) / 1000000.0f + " ms. CheckPoints cp1=" + (float)cpOne / 1000000.0f + ", cp2=" + 0.0f + ", cp3=" + 0.0f + ", cp4=" + 0.0f);
                logger.log(Level.INFO, "Loaded items for creature. CheckPoints cp1=" + (float)Items.getCpOne() / 1000000.0f + ", cp2=" + (float)Items.getCpTwo() / 1000000.0f + ", cp3=" + (float)Items.getCpThree() / 1000000.0f + ", cp4=" + (float)Items.getCpFour() / 1000000.0f);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        logger.log(Level.INFO, "Loaded " + this.getNumberOfCreatures() + " creatures. Destroyed " + destroyedCaveCrets + ". That took " + (float)(System.nanoTime() - lNow) / 1000000.0f + " ms. CheckPoints cp1=" + (float)cpOne / 1000000.0f + ", cp2=" + 0.0f + ", cp3=" + 0.0f + ", cp4=" + 0.0f);
        logger.log(Level.INFO, "Loaded items for creature. CheckPoints cp1=" + (float)Items.getCpOne() / 1000000.0f + ", cp2=" + (float)Items.getCpTwo() / 1000000.0f + ", cp3=" + (float)Items.getCpThree() / 1000000.0f + ", cp4=" + (float)Items.getCpFour() / 1000000.0f);
        loading = false;
        Items.clearCreatureLoadMap();
        Skills.clearCreatureLoadMap();
        Offspring.resetOffspringCounters();
        return this.getNumberOfCreatures();
    }

    public boolean creatureWithTemplateExists(int templateId) {
        for (Creature lCreature : this.creatures.values()) {
            if (lCreature.template.getTemplateId() != templateId) continue;
            return true;
        }
        return false;
    }

    public Creature getUniqueCreatureWithTemplate(int templateId) {
        ArrayList<Creature> foundCreatures = new ArrayList<Creature>();
        for (Creature lCreature : this.creatures.values()) {
            if (lCreature.template.getTemplateId() != templateId) continue;
            foundCreatures.add(lCreature);
        }
        if (foundCreatures.size() == 0) {
            return null;
        }
        if (foundCreatures.size() == 1) {
            return (Creature)foundCreatures.get(0);
        }
        throw new UnsupportedOperationException("Multiple creatures found");
    }

    public Creature getCreature(long id) throws NoSuchCreatureException {
        Creature toReturn = null;
        Long cid = new Long(id);
        if (!this.creatures.containsKey(cid)) {
            throw new NoSuchCreatureException("No such creature for id: " + id);
        }
        toReturn = this.creatures.get(cid);
        if (toReturn == null) {
            throw new NoSuchCreatureException("No creature with id " + id);
        }
        return toReturn;
    }

    public Creature getCreatureOrNull(long id) {
        try {
            return this.getCreature(id);
        }
        catch (NoSuchCreatureException n) {
            return null;
        }
    }

    private void removeTarget(long id) {
        Player[] players;
        for (Creature cret : this.creatures.values()) {
            if (cret.target != id) continue;
            cret.setTarget(-10L, true);
        }
        for (Creature cret : this.offlineCreatures.values()) {
            if (cret.target != id) continue;
            cret.setTarget(-10L, true);
        }
        for (Player lPlayer : players = Players.getInstance().getPlayers()) {
            if (lPlayer.target != id) continue;
            lPlayer.setTarget(-10L, true);
        }
    }

    public void setCreatureOffline(Creature creature) {
        try {
            Creature[] watchers;
            for (Creature lWatcher : watchers = creature.getInventory().getWatchers()) {
                creature.getInventory().removeWatcher(lWatcher, true);
            }
        }
        catch (NoSuchCreatureException watchers) {
        }
        catch (Exception nsc) {
            logger.log(Level.WARNING, creature.getName() + " " + nsc.getMessage(), nsc);
        }
        try {
            for (Creature lWatcher : watchers = creature.getBody().getBodyItem().getWatchers()) {
                creature.getBody().getBodyItem().removeWatcher(lWatcher, true);
            }
        }
        catch (NoSuchCreatureException watchers) {
        }
        catch (Exception nsc) {
            logger.log(Level.WARNING, creature.getName() + " " + nsc.getMessage(), nsc);
        }
        creature.clearOrders();
        creature.setLeader(null);
        creature.destroyVisionArea();
        this.removeTarget(creature.getWurmId());
        this.removeCreature(creature);
        this.addOfflineCreature(creature);
        creature.setPathing(false, true);
        creature.setOffline(true);
        try {
            creature.getStatus().savePosition(creature.getWurmId(), false, -10, true);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final void saveCreatureProtected(long creatureId, long protector) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(INSERT_PROT_CREATURE);
            ps.setLong(1, creatureId);
            ps.setLong(2, protector);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to insert creature protected " + creatureId, sqex);
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
    private final void deleteCreatureProtected(long creatureId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(DELETE_PROT_CREATURE);
            ps.setLong(1, creatureId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete creature protected " + creatureId, sqex);
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
    private final void loadAllProtectedCreatures() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(LOAD_PROT_CREATURES);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.protectedCreatures.put(rs.getLong("WURMID"), rs.getLong("PLAYERID"));
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load creatures protected.", sqex);
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

    public final int getNumberOfCreaturesProtectedBy(long protector) {
        int numsToReturn = 0;
        for (Long l : this.protectedCreatures.values()) {
            if (l != protector) continue;
            ++numsToReturn;
        }
        return numsToReturn;
    }

    public final int setNoCreaturesProtectedBy(long protector) {
        int numsToReturn = 0;
        LinkedList toRemove = new LinkedList();
        for (Map.Entry<Long, Long> l : this.protectedCreatures.entrySet()) {
            if (l.getValue() != protector) continue;
            toRemove.add(l.getKey());
        }
        for (Map.Entry<Long, Long> l : toRemove) {
            ++numsToReturn;
            this.deleteCreatureProtected((Long)((Object)l));
            this.protectedCreatures.remove(l);
        }
        return numsToReturn;
    }

    public final void setCreatureProtected(Creature creature, long protector, boolean setProtected) {
        if (setProtected) {
            if (!this.protectedCreatures.containsKey(creature.getWurmId())) {
                this.saveCreatureProtected(creature.getWurmId(), protector);
            }
            this.protectedCreatures.put(creature.getWurmId(), protector);
        } else if (this.protectedCreatures.containsKey(creature.getWurmId())) {
            this.deleteCreatureProtected(creature.getWurmId());
            this.protectedCreatures.remove(creature.getWurmId());
        }
    }

    public final long getCreatureProtectorFor(long wurmId) {
        if (this.protectedCreatures.containsKey(wurmId)) {
            return this.protectedCreatures.get(wurmId);
        }
        return -10L;
    }

    public final Creature[] getProtectedCreaturesFor(long playerId) {
        HashSet<Creature> protectedSet = new HashSet<Creature>();
        for (Map.Entry<Long, Long> entry : this.protectedCreatures.entrySet()) {
            if (entry.getValue() != playerId) continue;
            try {
                protectedSet.add(this.getCreature(entry.getKey()));
            }
            catch (NoSuchCreatureException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return protectedSet.toArray(new Creature[protectedSet.size()]);
    }

    public final boolean isCreatureProtected(long wurmId) {
        return this.protectedCreatures.containsKey(wurmId);
    }

    public final long getCreatureProctector(Creature creature) {
        Long whom = this.protectedCreatures.get(creature.getWurmId());
        if (whom != null) {
            return whom;
        }
        return -10L;
    }

    public void pollOfflineCreatures() {
        HashSet<Creature> toReturn = new HashSet<Creature>();
        Iterator<Creature> creatureIterator = this.offlineCreatures.values().iterator();
        while (creatureIterator.hasNext()) {
            Creature offline = creatureIterator.next();
            if (offline.pollAge()) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer(offline.getWurmId() + ", " + offline.getName() + " is dead.");
                }
                creatureIterator.remove();
                continue;
            }
            offline.pollLoyalty();
            if (offline.dominator != -10L || offline.isNpc() && !Constants.loadNpcs) continue;
            toReturn.add(offline);
        }
        for (Creature c : toReturn) {
            try {
                logger.log(Level.INFO, "Returning " + c.getName() + " from being offline due to no loyalty.");
                this.loadOfflineCreature(c.getWurmId());
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, nsc.getMessage());
            }
        }
    }

    public Creature loadOfflineCreature(long creatureId) throws NoSuchCreatureException {
        Long cid = new Long(creatureId);
        if (this.isCreatureOffline(cid)) {
            Creature creature = this.offlineCreatures.remove(cid);
            creature.setOffline(false);
            creature.setLeader(null);
            creature.setCitizenVillage(Villages.getVillageForCreature(creature));
            creature.getStatus().visible = true;
            try {
                creature.createVisionArea();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Problem creating VisionArea for creature with id " + creatureId + "due to " + ex.getMessage(), ex);
            }
            this.addCreature(creature, false);
            return creature;
        }
        throw new NoSuchCreatureException("No such creature with id " + creatureId);
    }

    public boolean isCreatureOffline(Long aCreatureId) {
        return this.offlineCreatures.containsKey(aCreatureId);
    }

    public final long getPetId(long dominatorId) {
        for (Creature c : this.offlineCreatures.values()) {
            if (c.dominator != dominatorId) continue;
            return c.getWurmId();
        }
        for (Creature c : this.creatures.values()) {
            if (c.dominator != dominatorId) continue;
            return c.getWurmId();
        }
        return -10L;
    }

    public void returnCreaturesForPlayer(long playerId) {
        HashSet<Creature> toLoad = new HashSet<Creature>();
        for (Creature c : this.offlineCreatures.values()) {
            if (c.dominator != playerId) continue;
            toLoad.add(c);
            c.setLoyalty(0.0f);
            c.setDominator(-10L);
        }
        for (Creature c : toLoad) {
            try {
                logger.log(Level.INFO, "Returning " + c.getName() + " from being offline due to no loyalty.");
                this.loadOfflineCreature(c.getWurmId());
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, nsc.getMessage());
            }
        }
    }

    public final Creature getNpc(String name) {
        return this.npcs.get(LoginHandler.raiseFirstLetter(name));
    }

    public final Npc[] getNpcs() {
        return this.npcs.values().toArray(new Npc[this.npcs.size()]);
    }

    public void removeCreature(Creature creature) {
        if (creature.isNpc()) {
            this.npcs.remove(creature.getName());
        }
        this.creatures.remove(new Long(creature.getWurmId()));
        this.avatars.remove(new Long(creature.getWurmId()));
        this.removeCreatureByType(creature.getTemplate().getTemplateId());
    }

    public boolean addCreature(Creature creature, boolean offline) {
        return this.addCreature(creature, offline, true);
    }

    void sendToWorld(Creature creature) {
        try {
            Zones.getZone(creature.getTileX(), creature.getTileY(), creature.isOnSurface()).addCreature(creature.getWurmId());
        }
        catch (NoSuchCreatureException nex) {
            logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nex.getMessage(), nex);
        }
        catch (NoSuchZoneException sex) {
            logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + sex.getMessage(), sex);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nsp.getMessage(), nsp);
        }
    }

    final void addCreatureByType(int creatureType) {
        Integer val = this.creaturesByType.get(creatureType);
        if (val == null) {
            this.creaturesByType.put(creatureType, 1);
        } else {
            this.creaturesByType.put(creatureType, val + 1);
        }
    }

    final void removeCreatureByType(int creatureType) {
        Integer val = this.creaturesByType.get(creatureType);
        if (val == null || val == 0) {
            this.creaturesByType.put(creatureType, 0);
        } else {
            this.creaturesByType.put(creatureType, val - 1);
        }
    }

    public final int getCreatureByType(int creatureType) {
        Integer val = this.creaturesByType.get(creatureType);
        if (val == null || val == 0) {
            return 0;
        }
        return val;
    }

    public final Map<Integer, Integer> getCreatureTypeList() {
        return this.creaturesByType;
    }

    public final int getOpenSpawnSlotsForCreatureType(int creatureType) {
        int currentCount = this.getCreatureByType(creatureType);
        try {
            CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureType);
            int maxByPercent = (int)((float)Servers.localServer.maxCreatures * ctemplate.getMaxPercentOfCreatures());
            int slotsOpenForPercent = Math.max(maxByPercent - currentCount, 0);
            if (ctemplate.usesMaxPopulation()) {
                int maxPop = ctemplate.getMaxPopulationOfCreatures();
                int slotsByPopulation = Math.max(maxPop - currentCount, 0);
                if (maxPop <= maxByPercent) {
                    return slotsByPopulation;
                }
                return slotsOpenForPercent;
            }
            return slotsOpenForPercent;
        }
        catch (NoSuchCreatureTemplateException e) {
            logger.log(Level.WARNING, "Unable to find creature template with id: " + creatureType + ".", e);
            return 0;
        }
    }

    boolean addCreature(Creature creature, boolean offline, boolean sendToWorld) {
        this.creatures.put(new Long(creature.getWurmId()), creature);
        if (creature.isNpc()) {
            this.npcs.put(LoginHandler.raiseFirstLetter(creature.getName()), creature);
        }
        if (creature.isAvatar()) {
            this.avatars.put(new Long(creature.getWurmId()), creature);
        }
        this.addCreatureByType(creature.getTemplate().getTemplateId());
        if (!creature.isDead()) {
            try {
                if (!creature.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(creature.getTileX(), creature.getTileY())))) {
                    creature.setLayer(0, false);
                    logger.log(Level.INFO, "Changed layer to surface for ID: " + creature.getWurmId() + " - " + creature.getName() + '.');
                }
                if (!offline) {
                    if (!creature.isFloating()) {
                        if (creature.isMonster() || creature.isAggHuman()) {
                            ++this.numberOfAgg;
                        } else {
                            ++this.numberOfNice;
                        }
                    }
                    if (creature.getStatus().modtype > 0) {
                        ++this.numberOfTyped;
                    }
                    if (creature.isAggWhitie() || creature.isDefendKingdom()) {
                        ++this.kingdomCreatures;
                    }
                    if (creature.isFloating() && !creature.isSpiritGuard()) {
                        if (creature.getTemplate().getTemplateId() == 70) {
                            ++seaMonsters;
                        } else {
                            ++seaHunters;
                        }
                    }
                    if (sendToWorld) {
                        int numsOnTile = Zones.getZone(creature.getTileX(), creature.getTileY(), creature.isOnSurface()).addCreature(creature.getWurmId());
                        if (!(!loading || numsOnTile <= 2 || creature.isHorse() || creature.isOnSurface() || creature.isDominated() || creature.isUnique() || creature.isSalesman() || creature.isWagoner() || creature.hasTrait(63) || creature.isHitched() || creature.getBody().getAllItems().length != 0 || creature.isBranded() || creature.isCaredFor())) {
                            Zones.getZone(creature.getTileX(), creature.getTileY(), creature.isOnSurface()).deleteCreature(creature, true);
                            logger.log(Level.INFO, "Destroying " + creature.getName() + ", " + creature.getWurmId() + " at cave " + creature.getTileX() + ", " + creature.getTileY() + " - overcrowded.");
                            ++destroyedCaveCrets;
                            return false;
                        }
                    }
                }
            }
            catch (NoSuchCreatureException nex) {
                logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nex.getMessage(), nex);
                this.creatures.remove(new Long(creature.getWurmId()));
                this.avatars.remove(new Long(creature.getWurmId()));
                this.removeCreatureByType(creature.getTemplate().getTemplateId());
                return false;
            }
            catch (NoSuchZoneException sex) {
                logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + sex.getMessage(), sex);
                this.creatures.remove(new Long(creature.getWurmId()));
                this.avatars.remove(new Long(creature.getWurmId()));
                this.removeCreatureByType(creature.getTemplate().getTemplateId());
                return false;
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Failed to add creature ID: " + creature.getWurmId() + " due to " + nsp.getMessage(), nsp);
                this.creatures.remove(new Long(creature.getWurmId()));
                this.avatars.remove(new Long(creature.getWurmId()));
                this.removeCreatureByType(creature.getTemplate().getTemplateId());
                return false;
            }
        }
        return true;
    }

    public static final boolean isLoading() {
        return loading;
    }

    private void addOfflineCreature(Creature creature) {
        this.offlineCreatures.put(new Long(creature.getWurmId()), creature);
        if (!creature.isDead() && !creature.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(creature.getTileX(), creature.getTileY())))) {
            creature.setLayer(0, false);
            logger.log(Level.INFO, "Changed layer to surface for ID: " + creature.getWurmId() + " - " + creature.getName() + '.');
        }
    }

    public Creature[] getCreatures() {
        Creature[] toReturn = new Creature[this.creatures.size()];
        return this.creatures.values().toArray(toReturn);
    }

    public Creature[] getAvatars() {
        Creature[] toReturn = new Creature[this.avatars.size()];
        return this.avatars.values().toArray(toReturn);
    }

    public void saveCreatures() {
        Creature[] creatarr = this.getCreatures();
        IOException error = null;
        int numsSaved = 0;
        for (Creature creature : creatarr) {
            try {
                if (!creature.getStatus().save()) continue;
                ++numsSaved;
            }
            catch (IOException iox) {
                error = iox;
            }
        }
        logger.log(Level.INFO, "Saved " + numsSaved + " creature statuses.");
        if (error != null) {
            logger.log(Level.INFO, "An error occurred while saving creatures:" + error.getMessage(), error);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void permanentlyDelete(Creature creature) {
        Den d;
        Brand brand;
        this.removeCreature(creature);
        if (!creature.isFloating()) {
            if (creature.isMonster() || creature.isAggHuman()) {
                --this.numberOfAgg;
            } else {
                --this.numberOfNice;
            }
        }
        if (creature.getStatus().modtype > 0) {
            --this.numberOfTyped;
        }
        if (creature.isAggWhitie() || creature.isDefendKingdom()) {
            --this.kingdomCreatures;
        }
        if (creature.isFloating()) {
            if (creature.getTemplate().getTemplateId() == 70) {
                --seaMonsters;
            } else {
                --seaHunters;
            }
        }
        if ((brand = this.getBrand(creature.getWurmId())) != null) {
            brand.deleteBrand();
            this.setBrand(creature.getWurmId(), 0L);
        }
        this.setCreatureProtected(creature, -10L, false);
        CreaturePos.delete(creature.getWurmId());
        MissionTargets.destroyMissionTarget(creature.getWurmId(), true);
        Connection dbcon = null;
        PreparedStatement ps = null;
        Connection dbcon2 = null;
        PreparedStatement ps2 = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            if (Constants.useSplitCreaturesTable) {
                ps = dbcon.prepareStatement(DELETE_CREATURE_SPLIT);
                ps.setLong(1, creature.getWurmId());
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_CREATURE_POS_SPLIT);
                ps.setLong(1, creature.getWurmId());
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_CREATURESKILLS);
                ps.setLong(1, creature.getWurmId());
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
            } else {
                ps = dbcon.prepareStatement(DELETE_CREATURE);
                ps.setLong(1, creature.getWurmId());
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(DELETE_CREATURESKILLS);
                ps.setLong(1, creature.getWurmId());
                ps.executeUpdate();
                DbUtilities.closeDatabaseObjects(ps, null);
            }
            dbcon2 = DbConnector.getItemDbCon();
            ps2 = dbcon2.prepareStatement(DELETE_CREATUREBODY);
            ps2.setLong(1, creature.getWurmId());
            ps2.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps2, null);
            ps2 = dbcon2.prepareStatement(DELETE_CREATUREITEMS);
            ps2.setLong(1, creature.getWurmId());
            ps2.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps2, null);
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete creature " + creature, sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbUtilities.closeDatabaseObjects(ps2, null);
                DbConnector.returnConnection(dbcon);
                DbConnector.returnConnection(dbcon2);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbUtilities.closeDatabaseObjects(ps2, null);
            DbConnector.returnConnection(dbcon);
            DbConnector.returnConnection(dbcon2);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbUtilities.closeDatabaseObjects(ps2, null);
        DbConnector.returnConnection(dbcon);
        DbConnector.returnConnection(dbcon2);
        if (creature.isUnique() && creature.getTemplate() != null && (d = Dens.getDen(creature.getTemplate().getTemplateId())) != null && !Creatures.getInstance().creatureWithTemplateExists(creature.getTemplate().getTemplateId())) {
            Dens.deleteDen(creature.getTemplate().getTemplateId());
        }
    }

    int resetGuardSkills() {
        int count = 0;
        for (Creature cret : this.creatures.values()) {
            if (!cret.isSpiritGuard()) continue;
            try {
                cret.skills.delete();
                cret.skills.clone(cret.getSkills().getSkills());
                cret.skills.save();
                ++count;
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, cret.getWurmId() + ":" + ex.getMessage(), ex);
            }
        }
        logger.log(Level.INFO, "Reset " + count + " guards skills.");
        return count;
    }

    final Creature[] getCreaturesWithName(String name) {
        name = name.toLowerCase();
        HashSet<Creature> toReturn = new HashSet<Creature>();
        for (Creature cret : this.creatures.values()) {
            if (cret.getName().toLowerCase().indexOf(name) < 0) continue;
            toReturn.add(cret);
        }
        return toReturn.toArray(new Creature[toReturn.size()]);
    }

    public Creature[] getHorsesWithName(String aName) {
        String name = aName.toLowerCase();
        HashSet<Creature> toReturn = new HashSet<Creature>();
        for (Creature cret : this.creatures.values()) {
            if (!cret.getTemplate().isHorse || cret.getName().toLowerCase().indexOf(name) < 0) continue;
            toReturn.add(cret);
        }
        return toReturn.toArray(new Creature[toReturn.size()]);
    }

    public static boolean shouldDestroy(Creature c) {
        int tid = c.getTemplate().getTemplateId();
        if (nums < 7000 && (tid == 15 || tid == 54 || tid == 25 || tid == 44 || tid == 52 || tid == 55 || tid == 10 || tid == 42 || tid == 12 || tid == 45 || tid == 48 || tid == 59 || tid == 13 || tid == 21)) {
            ++nums;
            return true;
        }
        return false;
    }

    public static void destroySwimmers() {
        Creature[] crets;
        for (Creature lCret : crets = Creatures.getInstance().getCreatures()) {
            if (!Creatures.shouldDestroy(lCret)) continue;
            lCret.destroy();
        }
    }

    public static void createLightAvengers() {
        int numsa = 0;
        while (numsa < 20) {
            int y;
            int x = Zones.safeTileX(Server.rand.nextInt(Zones.worldTileSizeX));
            int t = Server.surfaceMesh.getTile(x, y = Zones.safeTileY(Server.rand.nextInt(Zones.worldTileSizeY)));
            if (Tiles.decodeHeight(t) <= 0) continue;
            int deity = 1;
            if (Tiles.decodeHeightAsFloat(t) > 100.0f) {
                deity = 2;
            }
            try {
                CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(68);
                Creature cret = Creature.doNew(68, (float)(x << 2) + 2.0f, (float)(y << 2) + 2.0f, (float)Server.rand.nextInt(360), 0, "", ctemplate.getSex(), (byte)0);
                cret.setDeity(Deities.getDeity(deity));
                ++numsa;
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    public final void togglePollTaskLog() {
        this.setLog(!this.isLog());
    }

    public void pollAllCreatures(int num) {
        VolaTile t;
        if (num == 1 || this.crets == null) {
            if (this.crets != null) {
                if (this.isLog() && this.totalTime > 0L) {
                    logger.log(Level.INFO, "Creatures polled " + this.crets.length + " Took " + this.totalTime);
                }
                this.totalTime = 0L;
            }
            this.currentCreature = 0;
            this.crets = this.getCreatures();
            if (this.crets != null) {
                for (Player creature : Players.getInstance().getPlayers()) {
                    try {
                        t = creature.getCurrentTile();
                        if (creature.poll()) {
                            if (t != null) {
                                t.deleteCreature(creature);
                            }
                        } else if (creature.isDoLavaDamage() && creature.doLavaDamage() && t != null) {
                            t.deleteCreature(creature);
                        }
                        if (!creature.isDoAreaDamage() || t == null || !t.doAreaDamage(creature) || t == null) continue;
                        t.deleteCreature(creature);
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, ex.getMessage(), ex);
                        ex.printStackTrace();
                    }
                }
            }
        }
        this.startTime = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        int rest = 0;
        if (num == this.numberOfZonesX) {
            rest = this.crets.length % this.numberOfZonesX;
        }
        for (int x = this.currentCreature; x < rest + this.crets.length / this.numberOfZonesX * num; ++x) {
            ++this.currentCreature;
            try {
                t = this.crets[x].getCurrentTile();
                if (this.crets[x].poll()) {
                    if (t != null) {
                        t.deleteCreature(this.crets[x]);
                    }
                } else if (this.crets[x].isDoLavaDamage() && this.crets[x].doLavaDamage() && t != null) {
                    t.deleteCreature(this.crets[x]);
                }
                if (!this.crets[x].isDoAreaDamage() || t == null || !t.doAreaDamage(this.crets[x]) || t == null) continue;
                t.deleteCreature(this.crets[x]);
                continue;
            }
            catch (Exception ex) {
                logger.log(Level.INFO, ex.getMessage(), ex);
                ex.printStackTrace();
            }
        }
        this.totalTime += System.currentTimeMillis() - start;
    }

    public boolean isLog() {
        return this.logCreaturePolls;
    }

    public void setLog(boolean log) {
        this.logCreaturePolls = log;
    }

    public static final Creature[] getManagedAnimalsFor(Player player, int villageId, boolean includeAll) {
        HashSet<Creature> animals = new HashSet<Creature>();
        if (villageId >= 0 && includeAll) {
            for (Creature animal : Creatures.getInstance().getBranded(villageId)) {
                animals.add(animal);
            }
        }
        for (Creature animal : Creatures.getInstance().creatures.values()) {
            long whom = Creatures.getInstance().getCreatureProctector(animal);
            if (whom == player.getWurmId()) {
                animals.add(animal);
                continue;
            }
            if (!animal.canManage(player) || animal.isWagoner()) continue;
            animals.add(animal);
        }
        if (player.getPet() != null) {
            animals.add(player.getPet());
        }
        return animals.toArray(new Creature[animals.size()]);
    }

    public static final Creature[] getManagedWagonersFor(Player player, int villageId) {
        HashSet<Creature> animals = new HashSet<Creature>();
        if (!Servers.isThisAPvpServer()) {
            for (Map.Entry<Long, Wagoner> entry : Wagoner.getWagoners().entrySet()) {
                Creature wagoner;
                if (entry.getValue().getVillageId() == villageId) {
                    wagoner = entry.getValue().getCreature();
                    if (wagoner == null) continue;
                    animals.add(wagoner);
                    continue;
                }
                wagoner = entry.getValue().getCreature();
                if (wagoner == null || !wagoner.canManage(player)) continue;
                animals.add(wagoner);
            }
        }
        return animals.toArray(new Creature[animals.size()]);
    }

    public static final Set<Creature> getMayUseWagonersFor(Creature performer) {
        HashSet<Creature> wagoners = new HashSet<Creature>();
        if (!Servers.isThisAPvpServer()) {
            for (Map.Entry<Long, Wagoner> entry : Wagoner.getWagoners().entrySet()) {
                Wagoner wagoner = entry.getValue();
                Creature creature = wagoner.getCreature();
                if (wagoner.getVillageId() == -1 || creature == null || !creature.mayUse(performer)) continue;
                wagoners.add(creature);
            }
        }
        return wagoners;
    }

    static /* synthetic */ Map access$100(Creatures x0) {
        return x0.creatures;
    }

    static /* synthetic */ Logger access$200() {
        return logger;
    }

    static {
        destroyedCaveCrets = 0;
        loading = false;
        nums = 0;
        seaMonsters = 0;
        seaHunters = 0;
    }

    private class PollTimerTask
    extends TimerTask {
        private boolean keeprunning = true;
        private boolean log = false;

        private PollTimerTask() {
        }

        public final void shutDown() {
            this.keeprunning = false;
        }

        @Override
        public void run() {
            if (this.keeprunning) {
                int polled = 0;
                int destroyed = 0;
                int failedRemove = 0;
                long start = System.currentTimeMillis();
                for (Creature creature : Creatures.getInstance().creatures.values()) {
                    try {
                        VolaTile t = creature.getCurrentTile();
                        if (creature.poll()) {
                            ++destroyed;
                            if (t != null) {
                                t.deleteCreature(creature);
                            } else {
                                ++failedRemove;
                            }
                        } else if (creature.isDoLavaDamage() && creature.doLavaDamage()) {
                            ++destroyed;
                            if (t != null) {
                                t.deleteCreature(creature);
                            } else {
                                ++failedRemove;
                            }
                        }
                        if (creature.isDoAreaDamage()) {
                            // empty if block
                        }
                        ++polled;
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, ex.getMessage(), ex);
                    }
                }
                if (this.isLog()) {
                    logger.log(Level.INFO, "PTT polled " + polled + " Took " + (System.currentTimeMillis() - start) + " destroyed=" + destroyed + " failed remove=" + failedRemove);
                }
                for (Player creature : Players.getInstance().getPlayers()) {
                    try {
                        VolaTile t = creature.getCurrentTile();
                        if (creature.poll()) {
                            ++destroyed;
                            if (t != null) {
                                t.deleteCreature(creature);
                            } else {
                                ++failedRemove;
                            }
                        } else if (creature.isDoLavaDamage() && creature.doLavaDamage()) {
                            ++destroyed;
                            if (t != null) {
                                t.deleteCreature(creature);
                            } else {
                                ++failedRemove;
                            }
                        }
                        if (creature.isDoAreaDamage()) {
                            // empty if block
                        }
                        ++polled;
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, ex.getMessage(), ex);
                    }
                }
            } else {
                logger.log(Level.INFO, "PollTimerTask shut down.");
                this.cancel();
            }
        }

        public boolean isLog() {
            return this.log;
        }
    }
}


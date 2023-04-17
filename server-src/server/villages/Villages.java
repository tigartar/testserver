/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.InfluenceChain;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Alliance;
import com.wurmonline.server.villages.DbGuardPlan;
import com.wurmonline.server.villages.DbVillage;
import com.wurmonline.server.villages.DbVillageWar;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.VillageWar;
import com.wurmonline.server.villages.WarDeclaration;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;
import java.awt.Rectangle;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

public final class Villages
implements VillageStatus,
MiscConstants,
MonetaryConstants,
TimeConstants {
    private static final ConcurrentHashMap<Integer, Village> villages = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Long, DeadVillage> deadVillages = new ConcurrentHashMap();
    private static Logger logger = Logger.getLogger(Villages.class.getName());
    private static final String LOAD_VILLAGES = "SELECT * FROM VILLAGES WHERE DISBANDED=0";
    private static final String LOAD_DEAD_VILLAGES = "SELECT * FROM VILLAGES WHERE DISBANDED=1";
    private static final String CREATE_DEAD_VILLAGE = "INSERT INTO VILLAGES (NAME,FOUNDER,MAYOR,CREATIONDATE,STARTX,ENDX,STARTY,ENDY,DEEDID,LASTLOGIN,KINGDOM,DISBAND,DISBANDED,DEVISE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String LOAD_WARS = "SELECT * FROM VILLAGEWARS";
    private static final String LOAD_WAR_DECLARATIONS = "SELECT * FROM VILLAGEWARDECLARATIONS";
    @GuardedBy(value="ALLIANCES_RW_LOCK")
    private static final Set<Alliance> alliances = new HashSet<Alliance>();
    private static final ReentrantReadWriteLock ALLIANCES_RW_LOCK = new ReentrantReadWriteLock();
    @GuardedBy(value="WARS_RW_LOCK")
    private static final Set<Object> wars = new HashSet<Object>();
    private static final ReentrantReadWriteLock WARS_RW_LOCK = new ReentrantReadWriteLock();
    public static long TILE_UPKEEP = 20L;
    public static String TILE_UPKEEP_STRING = new Change(TILE_UPKEEP).getChangeString();
    public static long TILE_COST = 100L;
    public static String TILE_COST_STRING = new Change(TILE_COST).getChangeString();
    public static long GUARD_COST = (Servers.localServer.isChallengeOrEpicServer() ? 3 : 2) * 10000;
    public static String GUARD_COST_STRING = new Change(GUARD_COST).getChangeString();
    public static long GUARD_UPKEEP = (Servers.localServer.isChallengeOrEpicServer() ? 3 : 1) * 10000;
    public static String GUARD_UPKEEP_STRING = new Change(GUARD_UPKEEP).getChangeString();
    public static long PERIMETER_COST = 50L;
    public static String PERIMETER_COST_STRING = new Change(PERIMETER_COST).getChangeString();
    public static long PERIMETER_UPKEEP = 5L;
    public static String PERIMETER_UPKEEP_STRING = new Change(PERIMETER_UPKEEP).getChangeString();
    public static long MINIMUM_UPKEEP = 10000L;
    public static String MINIMUM_UPKEEP_STRING = new Change(MINIMUM_UPKEEP).getChangeString();
    private static long lastPolledVillageFaith = System.currentTimeMillis();

    private Villages() {
    }

    public static Village getVillage(int id) throws NoSuchVillageException {
        Village toReturn = villages.get(id);
        if (toReturn == null) {
            throw new NoSuchVillageException("No village with id " + id);
        }
        return toReturn;
    }

    public static Village getVillage(String name) throws NoSuchVillageException {
        for (Village v : villages.values()) {
            if (!v.getName().equalsIgnoreCase(name)) continue;
            return v;
        }
        throw new NoSuchVillageException("No village with name " + name);
    }

    public static Village getVillage(@Nonnull TilePos tilePos, boolean surfaced) {
        return Villages.getVillage(tilePos.x, tilePos.y, surfaced);
    }

    public static Village getVillage(int tilex, int tiley, boolean surfaced) {
        for (Village village : villages.values()) {
            if (!village.covers(tilex, tiley)) continue;
            return village;
        }
        return null;
    }

    public static Village getVillagePlus(int tilex, int tiley, boolean surfaced, int extra) {
        for (Village village : villages.values()) {
            if (!village.coversPlus(tilex, tiley, extra)) continue;
            return village;
        }
        return null;
    }

    public static final boolean isNameOk(String villageName, int ignoreVillageId) {
        for (Village village : villages.values()) {
            if (village.id == ignoreVillageId || !village.getName().equals(villageName)) continue;
            return false;
        }
        return true;
    }

    public static final boolean isNameOk(String villageName) {
        return Villages.isNameOk(villageName, -1);
    }

    public static Village createVillage(int startx, int endx, int starty, int endy, int tokenx, int tokeny, String villageName, Creature founder, long deedid, boolean surfaced, boolean democracy, String devise, boolean permanent, byte spawnKingdom, int initialPerimeter) throws NoSuchItemException, IOException, NoSuchCreatureException, NoSuchPlayerException, NoSuchRoleException, FailedException {
        if (!Villages.isNameOk(villageName)) {
            throw new FailedException("The name " + villageName + " already exists. Please select another.");
        }
        DbVillage toReturn = null;
        Item deed = Items.getItem(deedid);
        if (deed.getTemplateId() == 862) {
            deed.setDamage(0.0f);
            deed.setTemplateId(663);
            deed.setData1(100);
        }
        toReturn = new DbVillage(startx, endx, starty, endy, villageName, founder, deedid, surfaced, democracy, devise, permanent, spawnKingdom, initialPerimeter);
        toReturn.addCitizen(founder, toReturn.getRoleForStatus((byte)2));
        toReturn.initialize();
        try {
            Item token = Villages.createVillageToken(toReturn, tokenx, tokeny);
            ((Village)toReturn).setTokenId(token.getWurmId());
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
        deed.setData2(toReturn.getId());
        villages.put(toReturn.getId(), toReturn);
        toReturn.createInitialUpkeepPlan();
        ((Village)toReturn).addHistory(founder.getName(), "founded");
        HistoryManager.addHistory(founder.getName(), "founded " + villageName, false);
        founder.achievement(170);
        if (Features.Feature.TOWER_CHAINING.isEnabled()) {
            InfluenceChain chain = InfluenceChain.getInfluenceChain(toReturn.kingdom);
            InfluenceChain.addTokenToChain(toReturn.kingdom, toReturn.getToken());
        }
        return toReturn;
    }

    static void removeVillage(int id) {
        Village v = villages.remove(id);
        if (v != null) {
            DeadVillage dv = new DeadVillage(v.getDeedId(), v.getStartX(), v.getStartY(), v.getEndX(), v.getEndY(), v.getName(), v.getFounderName(), v.getMayor() != null ? v.getMayor().getName() : "Unknown", v.getCreationDate(), System.currentTimeMillis(), System.currentTimeMillis(), v.kingdom);
            deadVillages.put(v.getDeedId(), dv);
        }
    }

    public static boolean mayCreateTokenOnTile(boolean surfaced, int tilex, int tiley) {
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, surfaced);
        if (tile == null) {
            return true;
        }
        return tile.getStructure() == null;
    }

    static Item createTokenOnTile(Village village, int tilex, int tiley) throws NoSuchTemplateException, FailedException {
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, village.isOnSurface());
        if (tile == null) {
            Item token = ItemFactory.createItem(236, 99.0f, (tilex << 2) + 2, (tiley << 2) + 2, 180.0f, village.isOnSurface(), (byte)0, -10L, null);
            token.setData2(village.getId());
            return token;
        }
        if (tile.getStructure() == null) {
            Item token = ItemFactory.createItem(236, 99.0f, (tilex << 2) + 2, (tiley << 2) + 2, 180.0f, village.isOnSurface(), (byte)0, -10L, null);
            token.setData2(village.getId());
            return token;
        }
        return null;
    }

    static Item createVillageToken(Village village, int tokenx, int tokeny) throws NoSuchTemplateException, FailedException {
        int y;
        int x;
        int size = village.endx - village.startx;
        Item token = Villages.createTokenOnTile(village, tokenx, tokeny);
        if (token != null) {
            return token;
        }
        for (x = -1; x <= 1; ++x) {
            for (y = -1; y <= 1; ++y) {
                token = Villages.createTokenOnTile(village, tokenx + x, tokeny + y);
                if (token == null) continue;
                return token;
            }
        }
        for (x = -size / 2; x <= size / 2; ++x) {
            for (y = -size / 2; y <= size / 2; ++y) {
                token = Villages.createTokenOnTile(village, tokenx + x, tokeny + y);
                if (token == null) continue;
                return token;
            }
        }
        throw new FailedException("Failed to locate a good spot for the token item.");
    }

    public static final String isFocusZoneBlocking(int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, int desiredPerimeter, boolean surfaced) {
        FocusZone[] fzs;
        int startpx = Zones.safeTileX(tokenx - sizeW - 5 - desiredPerimeter);
        int startpy = Zones.safeTileY(tokeny - sizeN - 5 - desiredPerimeter);
        int endpy = Zones.safeTileX(tokeny + sizeS + 1 + 5 + desiredPerimeter);
        int endpx = Zones.safeTileY(tokenx + sizeE + 1 + 5 + desiredPerimeter);
        Rectangle bounds = new Rectangle(startpx, startpy, endpx - startpx, endpy - startpy);
        StringBuilder toReturn = new StringBuilder();
        for (FocusZone focusz : fzs = FocusZone.getAllZones()) {
            Rectangle focusRect;
            if (!focusz.isNonPvP() && !focusz.isPvP() || !(focusRect = new Rectangle(focusz.getStartX(), focusz.getStartY(), focusz.getEndX() - focusz.getStartX(), focusz.getEndY() - focusz.getStartY())).intersects(bounds)) continue;
            toReturn.append(focusz.getName() + " is within the planned area. ");
        }
        if (toReturn.toString().length() > 0) {
            toReturn.append("Settling there is no longer allowed.");
        }
        return toReturn.toString();
    }

    public static final Set<Village> getVillagesWithin(int startX, int startY, int endX, int endY) {
        Rectangle bounds;
        Rectangle perimRect = bounds = new Rectangle(startX, startY, endX - startX, endY - startY);
        HashSet<Village> toReturn = new HashSet<Village>();
        for (Village village : villages.values()) {
            perimRect = new Rectangle(village.startx, village.starty, village.getDiameterX(), village.getDiameterY());
            if (!perimRect.intersects(bounds)) continue;
            toReturn.add(village);
        }
        return toReturn;
    }

    public static Map<Village, String> canFoundVillage(int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, int desiredPerimeter, boolean surfaced, @Nullable Village original, Creature founder) {
        Rectangle bounds;
        int startpx = Zones.safeTileX(tokenx - sizeW - 5 - desiredPerimeter);
        int startpy = Zones.safeTileY(tokeny - sizeN - 5 - desiredPerimeter);
        int endpy = Zones.safeTileX(tokeny + sizeS + 1 + 5 + desiredPerimeter);
        int endpx = Zones.safeTileY(tokenx + sizeE + 1 + 5 + desiredPerimeter);
        Rectangle perimRect = bounds = new Rectangle(startpx, startpy, endpx - startpx, endpy - startpy);
        Hashtable<Village, String> decliners = new Hashtable<Village, String>();
        boolean allianceOnly = Servers.localServer.PVPSERVER && !Servers.localServer.isChallengeOrEpicServer();
        Rectangle allianceBounds = allianceOnly ? new Rectangle(Zones.safeTileX(startpx - 100), Zones.safeTileY(startpy - 100), endpx - startpx + 200, endpy - startpy + 200) : bounds;
        boolean accept = false;
        boolean prohibited = false;
        for (Village village : villages.values()) {
            if (village == original) continue;
            int mindist = 5 + village.getPerimeterSize();
            perimRect = new Rectangle(village.startx - mindist, village.starty - mindist, village.getDiameterX() + mindist * 2, village.getDiameterY() + mindist * 2);
            if (perimRect.intersects(bounds)) {
                prohibited = true;
                decliners.put(village, "has perimeter within the planned settlement or its perimeter.");
                continue;
            }
            if (!allianceOnly || original != null || !perimRect.intersects(allianceBounds) || founder == null) continue;
            if (founder.getCitizenVillage() != null && (founder.getCitizenVillage() == village || village.isAlly(founder))) {
                accept = true;
                continue;
            }
            if (founder.getCitizenVillage() != null && founder.getCitizenVillage() == village && village.isAlly(founder)) continue;
            decliners.put(village, "requires " + founder.getName() + " to be a citizen or ally.");
        }
        if (prohibited || !accept) {
            return decliners;
        }
        return new Hashtable<Village, String>();
    }

    public static Village getVillageWithPerimeterAt(int tilex, int tiley, boolean surfaced) {
        for (Village village : villages.values()) {
            int mindist = 5 + village.getPerimeterSize();
            Rectangle perimRect = new Rectangle(village.startx - mindist, village.starty - mindist, village.endx - village.startx + (1 + mindist * 2), village.endy - village.starty + (1 + mindist * 2));
            if (!perimRect.contains(tilex, tiley)) continue;
            return village;
        }
        return null;
    }

    public static Village doesNotAllowAction(Creature performer, int action, int tilex, int tiley, boolean surfaced) {
        if (!Servers.localServer.HOMESERVER) {
            return null;
        }
        if (performer.getKingdomId() != Servers.localServer.KINGDOM) {
            return null;
        }
        if (performer.getPower() > 1) {
            return null;
        }
        if (performer.getKingdomTemplateId() == 3) {
            return null;
        }
        VolaTile t = Zones.getTileOrNull(tilex, tiley, surfaced);
        if (t != null && t.getVillage() != null) {
            return null;
        }
        Village v = Villages.getVillageWithPerimeterAt(tilex, tiley, surfaced);
        if (v != null && !v.isCitizen(performer) && !v.isAlly(performer)) {
            return v;
        }
        return null;
    }

    public static final Village doesNotAllowBuildAction(Creature performer, int action, int tilex, int tiley, boolean surfaced) {
        VillageRole role;
        Village village;
        if (performer.getPower() > 1) {
            return null;
        }
        VolaTile t = Zones.getTileOrNull(tilex, tiley, surfaced);
        if (t != null && (village = t.getVillage()) != null && (role = village.getRoleFor(performer)) != null) {
            if (role.mayBuild()) {
                return null;
            }
            return village;
        }
        Village v = Villages.getVillageWithPerimeterAt(tilex, tiley, surfaced);
        if (v != null && !v.isCitizen(performer) && !v.isAlly(performer)) {
            return v;
        }
        return null;
    }

    public static Item isAltarOnDeed(int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, boolean surfaced) {
        int startx = Math.max(0, tokenx - sizeW);
        int starty = Math.max(0, tokeny - sizeN);
        int endy = Math.min((1 << Constants.meshSize) - 1, tokeny + sizeS);
        int endx = Math.min((1 << Constants.meshSize) - 1, tokenx + sizeE);
        for (int x = startx; x <= endx; ++x) {
            for (int y = starty; y <= endy; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, surfaced);
                if (t == null) continue;
                Item[] items = t.getItems();
                for (int i = 0; i < items.length; ++i) {
                    if (items[i].isUnfinished() || !items[i].isNonDeedable() && (!items[i].isRoyal() || !items[i].isNoTake()) && (!items[i].isEpicTargetItem() || !Servers.localServer.PVPSERVER)) continue;
                    return items[i];
                }
            }
        }
        return null;
    }

    public static Object isAggOnDeed(@Nullable Village currVill, Creature responder, int sizeW, int sizeE, int sizeN, int sizeS, int tokenx, int tokeny, boolean surfaced) {
        int startx = Math.max(0, tokenx - sizeW);
        int starty = Math.max(0, tokeny - sizeN);
        int endy = Zones.safeTileY(tokeny + sizeS);
        int endx = Zones.safeTileX(tokenx + sizeE);
        for (int x = startx; x <= endx; ++x) {
            for (int y = starty; y <= endy; ++y) {
                VolaTile t;
                Den den = Dens.getDen(x, y);
                if (den != null) {
                    try {
                        CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(den.getTemplateId());
                        if (responder.getPower() >= 2) {
                            responder.getCommunicator().sendSafeServerMessage(template.getName() + " Den found at " + x + "," + y + ".");
                        }
                        if (!template.isUnique() || Creatures.getInstance().creatureWithTemplateExists(den.getTemplateId())) {
                            return den;
                        }
                    }
                    catch (NoSuchCreatureTemplateException nst) {
                        logger.log(Level.WARNING, den.getTemplateId() + ":" + nst.getMessage(), nst);
                        if (responder.getPower() >= 2) {
                            responder.getCommunicator().sendSafeServerMessage("Den with unknown template ID: " + den.getTemplateId() + " found at " + x + ", " + y + ".");
                        } else {
                            responder.getCommunicator().sendSafeServerMessage("An invalid creature den was found. Please use /support to ask a GM for help to deal with this issue.");
                        }
                        return den;
                    }
                }
                if ((t = Zones.getTileOrNull(x, y, surfaced)) == null || currVill != null && t.getVillage() == currVill) continue;
                Creature[] crets = t.getCreatures();
                for (int i = 0; i < crets.length; ++i) {
                    if ((crets[i].getAttitude(responder) != 2 || !(crets[i].getBaseCombatRating() > 5.0f) && !crets[i].isPlayer()) && !crets[i].isUnique()) continue;
                    if (responder.getPower() >= 2) {
                        responder.getCommunicator().sendSafeServerMessage(crets[i].getName() + " agro Creature found at " + x + "," + y + ".");
                    }
                    return crets[i];
                }
            }
        }
        return null;
    }

    public static boolean canExpandVillage(int size, Item token) throws NoSuchVillageException {
        Village vill = Villages.getVillage(token.getData2());
        int tilex = vill.getStartX();
        int tiley = vill.getStartY();
        boolean surfaced = vill.isOnSurface();
        int startx = Math.max(0, tilex - size);
        int starty = Math.max(0, tiley - size);
        int endx = Math.min((1 << Constants.meshSize) - 1, tilex + size);
        int endy = Math.min((1 << Constants.meshSize) - 1, tiley + size);
        for (int x = startx; x <= endx; x += 5) {
            for (int y = starty; y <= endy; y += 5) {
                Village check = Zones.getVillage(x, y, surfaced);
                if (check == null || check.equals(vill)) continue;
                return false;
            }
        }
        return true;
    }

    public static void generateDeadVillage(Player performer, boolean sendFeedback) throws IOException {
        int centerX = -1;
        int centerY = -1;
        boolean gotLocation = false;
        while (!gotLocation) {
            int testY;
            int testX = Server.rand.nextInt((int)((float)Zones.worldTileSizeX * 0.8f)) + (int)((float)Zones.worldTileSizeX * 0.1f);
            if (Tiles.decodeHeight(Server.surfaceMesh.getTile(testX, testY = Server.rand.nextInt((int)((float)Zones.worldTileSizeY * 0.8f)) + (int)((float)Zones.worldTileSizeY * 0.1f))) <= 0) continue;
            centerX = testX;
            centerY = testY;
            gotLocation = true;
        }
        int sizeX = Server.rand.nextInt(30) * (Server.rand.nextInt(4) == 0 ? 3 : 1) + 5;
        int sizeY = Math.max(sizeX / 4, Math.min(sizeX * 4, Server.rand.nextInt(30) * (Server.rand.nextInt(4) == 0 ? 3 : 1) + 5));
        sizeY = Math.max(5, sizeY);
        int startx = centerX - sizeX;
        int starty = centerY - sizeY;
        int endx = centerX + sizeX;
        int endy = centerY + sizeY;
        String name = StringUtilities.raiseFirstLetterOnly(Villages.generateGenericVillageName());
        String founderName = StringUtilities.raiseFirstLetterOnly(Server.rand.nextBoolean() ? Offspring.getRandomFemaleName() : Offspring.getRandomMaleName());
        String mayorName = StringUtilities.raiseFirstLetterOnly(Server.rand.nextBoolean() ? founderName : (Server.rand.nextBoolean() ? Offspring.getRandomFemaleName() : Offspring.getRandomMaleName()));
        long creationDate = System.currentTimeMillis() - 2419200000L * (long)Server.rand.nextInt(60);
        long deedid = WurmId.getNextItemId();
        long disbandDate = (long)Math.min((float)(System.currentTimeMillis() - 2419200000L), Math.max((float)(creationDate + 2419200000L), (float)creationDate + (float)(System.currentTimeMillis() - creationDate) * Server.rand.nextFloat()));
        long lastLogin = Math.max(creationDate + 2419200000L, disbandDate - 2419200000L * (long)Server.rand.nextInt(6));
        byte kingdom = Servers.localServer.HOMESERVER ? Servers.localServer.KINGDOM : (byte)(Server.rand.nextInt(4) + 1);
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_DEAD_VILLAGE, 2);
            ps.setString(1, name);
            ps.setString(2, founderName);
            ps.setString(3, mayorName);
            ps.setLong(4, creationDate);
            ps.setInt(5, startx);
            ps.setInt(6, endx);
            ps.setInt(7, starty);
            ps.setInt(8, endy);
            ps.setLong(9, deedid);
            ps.setLong(10, lastLogin);
            ps.setByte(11, kingdom);
            ps.setLong(12, disbandDate);
            ps.setBoolean(13, true);
            ps.setString(14, "A settlement like no other.");
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        DeadVillage dv = new DeadVillage(deedid, startx, starty, endx, endy, name, founderName, mayorName, creationDate, disbandDate, lastLogin, kingdom);
        deadVillages.put(deedid, dv);
        performer.sendToLoggers("Generated a dead village at " + centerX + "," + centerY + ".");
        if (sendFeedback) {
            performer.getCommunicator().sendNormalServerMessage("Dead Village \"" + name + "\" created at " + centerX + "," + centerY + ".");
        }
    }

    private static String generateGenericVillageName() {
        ArrayList<String> genericEndings = new ArrayList<String>();
        Villages.addAllStrings(genericEndings, " Village", " Isle", " Island", " Mountain", " Plains", " Estate", " Beach", " Homestead", " Valley", " Forest", " Farm", " Castle");
        ArrayList<String> genericSuffix = new ArrayList<String>();
        Villages.addAllStrings(genericSuffix, "ford", "borough", "ington", "ton", "stead", "chester", "dale", "ham", "ing", "mouth", "port");
        String toReturn = "";
        switch (Server.rand.nextInt(3)) {
            case 0: {
                toReturn = toReturn + Offspring.getRandomMaleName();
                break;
            }
            case 1: {
                toReturn = toReturn + Offspring.getRandomFemaleName();
                break;
            }
            case 2: {
                toReturn = toReturn + Offspring.getRandomGenericName();
            }
        }
        if (Server.rand.nextInt(3) == 0) {
            toReturn = toReturn + genericSuffix.get(Server.rand.nextInt(genericSuffix.size()));
            if (Server.rand.nextBoolean()) {
                toReturn = toReturn + genericEndings.get(Server.rand.nextInt(genericEndings.size()));
            }
        } else {
            toReturn = toReturn + genericEndings.get(Server.rand.nextInt(genericEndings.size()));
        }
        return toReturn;
    }

    private static void addAllStrings(ArrayList<String> toAddTo, String ... names) {
        for (String s : names) {
            toAddTo.add(s);
        }
    }

    public static void loadDeadVillages() throws IOException {
        logger.info("Loading dead villages.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_DEAD_VILLAGES);
            rs = ps.executeQuery();
            while (rs.next()) {
                int startx = rs.getInt("STARTX");
                int starty = rs.getInt("STARTY");
                int endx = rs.getInt("ENDX");
                int endy = rs.getInt("ENDY");
                String name = rs.getString("NAME");
                String founderName = rs.getString("FOUNDER");
                String mayorName = rs.getString("MAYOR");
                long creationDate = rs.getLong("CREATIONDATE");
                long deedid = rs.getLong("DEEDID");
                long disband = rs.getLong("DISBAND");
                long lastLogin = rs.getLong("LASTLOGIN");
                byte kingdom = rs.getByte("KINGDOM");
                DeadVillage dv = new DeadVillage(deedid, startx, starty, endx, endy, name, founderName, mayorName, creationDate, disband, lastLogin, kingdom);
                deadVillages.put(deedid, dv);
            }
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + deadVillages.size() + " dead villages from the database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + deadVillages.size() + " dead villages from the database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    public static final void loadVillages() throws IOException {
        logger.info("Loading villages.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_VILLAGES);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID");
                int startx = rs.getInt("STARTX");
                int starty = rs.getInt("STARTY");
                int endx = rs.getInt("ENDX");
                int endy = rs.getInt("ENDY");
                String name = rs.getString("NAME");
                String founderName = rs.getString("FOUNDER");
                String mayorName = rs.getString("MAYOR");
                long creationDate = rs.getLong("CREATIONDATE");
                long deedid = rs.getLong("DEEDID");
                boolean surfaced = rs.getBoolean("SURFACED");
                String devise = rs.getString("DEVISE");
                boolean democracy = rs.getBoolean("DEMOCRACY");
                boolean homestead = rs.getBoolean("HOMESTEAD");
                long tokenid = rs.getLong("TOKEN");
                long disband = rs.getLong("DISBAND");
                long disbander = rs.getLong("DISBANDER");
                long lastLogin = rs.getLong("LASTLOGIN");
                byte kingdom = rs.getByte("KINGDOM");
                long upkeep = rs.getLong("UPKEEP");
                byte settings = rs.getByte("MAYPICKUP");
                boolean acceptsHomesteads = rs.getBoolean("ACCEPTSHOMESTEADS");
                int maxcitizens = rs.getInt("MAXCITIZENS");
                boolean perma = rs.getBoolean("PERMANENT");
                byte spawnKingdom = rs.getByte("SPAWNKINGDOM");
                boolean merchants = rs.getBoolean("MERCHANTS");
                int perimeterTiles = rs.getInt("PERIMETER");
                boolean aggros = rs.getBoolean("AGGROS");
                String consumerKeyToUse = rs.getString("TWITKEY");
                String consumerSecretToUse = rs.getString("TWITSECRET");
                String applicationToken = rs.getString("TWITAPP");
                String applicationSecret = rs.getString("TWITAPPSECRET");
                boolean twitChat = rs.getBoolean("TWITCHAT");
                boolean twitEnabled = rs.getBoolean("TWITENABLE");
                float faithWar = rs.getFloat("FAITHWAR");
                float faithHeal = rs.getFloat("FAITHHEAL");
                float faithCreate = rs.getFloat("FAITHCREATE");
                byte spawnSituation = rs.getByte("SPAWNSITUATION");
                int allianceNumber = rs.getInt("ALLIANCENUMBER");
                short wins = rs.getShort("HOTAWINS");
                long lastChangedName = rs.getLong("NAMECHANGED");
                int villageRep = rs.getInt("VILLAGEREP");
                String motd = rs.getString("MOTD");
                DbVillage toAdd = new DbVillage(id, startx, endx, starty, endy, name, founderName, mayorName, deedid, surfaced, democracy, devise, creationDate, homestead, tokenid, disband, disbander, lastLogin, kingdom, upkeep, settings, acceptsHomesteads, merchants, maxcitizens, perma, spawnKingdom, perimeterTiles, aggros, consumerKeyToUse, consumerSecretToUse, applicationToken, applicationSecret, twitChat, twitEnabled, faithWar, faithHeal, faithCreate, spawnSituation, allianceNumber, wins, lastChangedName, motd);
                toAdd.villageReputation = villageRep;
                villages.put(id, toAdd);
                Kingdoms.getKingdom(kingdom).setExistsHere(true);
                ((Village)toAdd).loadRoles();
                ((Village)toAdd).loadVillageMapAnnotations();
                ((Village)toAdd).loadVillageRecruitees();
                toAdd.plan = new DbGuardPlan(id);
                if (!logger.isLoggable(Level.FINE)) continue;
                logger.fine("Loaded Village ID: " + id + ": " + toAdd);
            }
            for (Village toAdd : villages.values()) {
                toAdd.initialize();
                toAdd.addGates();
                toAdd.addMineDoors();
                toAdd.loadReputations();
                toAdd.plan.fixGuards();
                toAdd.checkForEnemies();
                toAdd.loadHistory();
            }
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + villages.size() + " villages from the database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + villages.size() + " villages from the database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    public static final void loadCitizens() {
        logger.info("Loading villages citizens.");
        for (Village toAdd : villages.values()) {
            toAdd.loadCitizens();
        }
    }

    public static final void loadGuards() {
        logger.info("Loading villages guards.");
        for (Village toAdd : villages.values()) {
            toAdd.loadGuards();
        }
    }

    static final void createWar(Village villone, Village villtwo) {
        DbVillageWar newWar = new DbVillageWar(villone, villtwo);
        ((VillageWar)newWar).save();
        villone.startWar(newWar, true);
        villtwo.startWar(newWar, false);
        HistoryManager.addHistory("", villone.getName() + " and " + villtwo.getName() + " goes to war.");
    }

    public static final void declareWar(Village villone, Village villtwo) {
        WarDeclaration newWar = new WarDeclaration(villone, villtwo);
        villone.addWarDeclaration(newWar);
        villtwo.addWarDeclaration(newWar);
    }

    public static final void declarePeace(Creature performer, Creature accepter, Village villone, Village villtwo) {
        villone.declarePeace(performer, accepter, villtwo, true);
        villtwo.declarePeace(performer, accepter, villone, false);
        VillageWar[] wararr = Villages.getWars();
        for (int x = 0; x < wararr.length; ++x) {
            if ((wararr[x].getVillone() != villone || wararr[x].getVilltwo() != villtwo) && (wararr[x].getVilltwo() != villone || wararr[x].getVillone() != villtwo)) continue;
            Villages.removeAndDeleteVillageWar(wararr[x]);
        }
        HistoryManager.addHistory("", villone.getName() + " and " + villtwo.getName() + " make peace.");
    }

    private static boolean removeAndDeleteVillageWar(VillageWar aVillageWar) {
        boolean lVillageWarExisted = false;
        if (aVillageWar != null) {
            WARS_RW_LOCK.writeLock().lock();
            try {
                lVillageWarExisted = wars.remove(aVillageWar);
                aVillageWar.delete();
            }
            finally {
                WARS_RW_LOCK.writeLock().unlock();
            }
        }
        return lVillageWarExisted;
    }

    public static final VillageWar[] getWars() {
        WARS_RW_LOCK.readLock().lock();
        try {
            VillageWar[] villageWarArray = wars.toArray(new VillageWar[wars.size()]);
            return villageWarArray;
        }
        finally {
            WARS_RW_LOCK.readLock().unlock();
        }
    }

    public static final Alliance[] getAlliances() {
        ALLIANCES_RW_LOCK.readLock().lock();
        try {
            Alliance[] allianceArray = alliances.toArray(new Alliance[alliances.size()]);
            return allianceArray;
        }
        finally {
            ALLIANCES_RW_LOCK.readLock().unlock();
        }
    }

    public static final void loadWars() throws IOException {
        logger.log(Level.INFO, "Loading all wars.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        WARS_RW_LOCK.writeLock().lock();
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_WARS);
            rs = ps.executeQuery();
            int aid = -10;
            while (rs.next()) {
                try {
                    aid = rs.getInt("ID");
                    Village villone = Villages.getVillage(rs.getInt("VILLONE"));
                    Village villtwo = Villages.getVillage(rs.getInt("VILLTWO"));
                    DbVillageWar war = new DbVillageWar(villone, villtwo);
                    villone.addWar(war);
                    villtwo.addWar(war);
                    wars.add(war);
                    if (!logger.isLoggable(Level.FINE)) continue;
                    logger.fine("Loaded War ID: " + aid + ": " + war);
                }
                catch (NoSuchVillageException nsv) {
                    logger.log(Level.WARNING, "Failed to load war with id " + aid + "!");
                }
            }
            WARS_RW_LOCK.writeLock().unlock();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                WARS_RW_LOCK.writeLock().unlock();
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + wars.size() + " wars from the database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + wars.size() + " wars from the database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    public static final void loadWarDeclarations() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        WARS_RW_LOCK.writeLock().lock();
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_WAR_DECLARATIONS);
            rs = ps.executeQuery();
            int aid = -10;
            while (rs.next()) {
                try {
                    aid = rs.getInt("ID");
                    Village villone = Villages.getVillage(rs.getInt("VILLONE"));
                    Village villtwo = Villages.getVillage(rs.getInt("VILLTWO"));
                    long time = rs.getLong("DECLARETIME");
                    WarDeclaration war = new WarDeclaration(villone, villtwo, time);
                    villone.addWarDeclaration(war);
                    villtwo.addWarDeclaration(war);
                    wars.add(war);
                }
                catch (NoSuchVillageException nsv) {
                    logger.log(Level.WARNING, "Failed to load war with id " + aid + "!");
                }
            }
            WARS_RW_LOCK.writeLock().unlock();
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                WARS_RW_LOCK.writeLock().unlock();
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    public static Village getVillageForCreature(Creature creature) {
        if (creature == null) {
            return null;
        }
        for (Village village : villages.values()) {
            if (!village.isCitizen(creature)) continue;
            return village;
        }
        return null;
    }

    public static Village getVillageForCreature(long wid) {
        if (wid == -10L) {
            return null;
        }
        for (Village village : villages.values()) {
            if (!village.isCitizen(wid)) continue;
            return village;
        }
        return null;
    }

    public static long getVillageMoney() {
        long toReturn = 0L;
        for (Village village : villages.values()) {
            if (village.plan == null) continue;
            toReturn += village.plan.moneyLeft;
        }
        return toReturn;
    }

    public static final int getSizeForDeed(int templateId) {
        if (templateId == 237 || templateId == 234) {
            return 5;
        }
        if (templateId == 211 || templateId == 253) {
            return 10;
        }
        if (templateId == 238) {
            return 15;
        }
        if (templateId == 239 || templateId == 254) {
            return 20;
        }
        if (templateId == 242) {
            return 50;
        }
        if (templateId == 244) {
            return 100;
        }
        if (templateId == 245) {
            return 200;
        }
        return 5;
    }

    public static final Village[] getVillages() {
        Village[] toReturn = new Village[]{};
        if (villages != null) {
            toReturn = villages.values().toArray(new Village[villages.size()]);
        }
        return toReturn;
    }

    public static int getNumberOfVillages() {
        return villages.size();
    }

    public static final void poll() {
        long now = System.currentTimeMillis();
        Village[] aVillages = Villages.getVillages();
        boolean lowerFaith = System.currentTimeMillis() - lastPolledVillageFaith > 86400000L;
        for (int x = 0; x < aVillages.length; ++x) {
            aVillages[x].poll(now, lowerFaith);
        }
        if (lowerFaith) {
            lastPolledVillageFaith = System.currentTimeMillis();
        }
    }

    public static final Village getCapital(byte kingdom) {
        Village[] vills = Villages.getVillages();
        for (int x = 0; x < vills.length; ++x) {
            if (vills[x].kingdom != kingdom || !vills[x].isCapital()) continue;
            return vills[x];
        }
        return null;
    }

    public static final Village getFirstVillageForKingdom(byte kingdom) {
        Village[] vills = Villages.getVillages();
        for (int x = 0; x < vills.length; ++x) {
            if (vills[x].kingdom != kingdom) continue;
            return vills[x];
        }
        return null;
    }

    public static final Village getFirstPermanentVillageForKingdom(byte kingdom) {
        Village[] vills = Villages.getVillages();
        for (int x = 0; x < vills.length; ++x) {
            if (vills[x].kingdom != kingdom || !vills[x].isPermanent) continue;
            return vills[x];
        }
        return null;
    }

    public static final Village[] getPermanentVillagesForKingdom(byte kingdom) {
        ConcurrentHashMap<Integer, Village> permVills = new ConcurrentHashMap<Integer, Village>();
        for (Village village : villages.values()) {
            if (!village.isPermanent || village.kingdom != kingdom) continue;
            permVills.put(village.getId(), village);
        }
        return permVills.values().toArray(new Village[permVills.size()]);
    }

    public static final boolean wasLastVillage(Village village) {
        Village[] vills = Villages.getVillages();
        for (int x = 0; x < vills.length; ++x) {
            if (village.getId() == vills[x].getId() || vills[x].kingdom != village.kingdom) continue;
            return false;
        }
        return true;
    }

    public static final void convertTowers() {
        int x;
        Village[] vills = Villages.getVillages();
        for (x = 0; x < vills.length; ++x) {
            vills[x].convertTowersWithinDistance(150);
        }
        for (x = 0; x < vills.length; ++x) {
            vills[x].convertTowersWithinPerimeter();
        }
    }

    public static final Village[] getPermanentVillages(byte kingdomChecked) {
        HashSet<Village> toReturn = new HashSet<Village>();
        Kingdom kingd = Kingdoms.getKingdom(kingdomChecked);
        if (kingd != null) {
            for (Village v : villages.values()) {
                if (v.kingdom != kingdomChecked || !v.isPermanent && (!v.isCapital() || !kingd.isCustomKingdom())) continue;
                toReturn.add(v);
            }
        }
        return toReturn.toArray(new Village[toReturn.size()]);
    }

    public static final Village[] getKosVillagesFor(long playerId) {
        HashSet<Village> toReturn = new HashSet<Village>();
        for (Village v : villages.values()) {
            Reputation rep = v.getReputationObject(playerId);
            if (rep == null) continue;
            toReturn.add(v);
        }
        return toReturn.toArray(new Village[toReturn.size()]);
    }

    @Nullable
    public static final Village getVillageFor(Item waystone) {
        for (Village village : villages.values()) {
            if (!village.coversPlus(waystone.getTileX(), waystone.getTileY(), 2)) continue;
            return village;
        }
        return null;
    }

    public static final ArrayList<DeadVillage> getDeadVillagesFor(int tilex, int tiley) {
        return Villages.getDeadVillagesNear(tilex, tiley, 0);
    }

    public static final ArrayList<DeadVillage> getDeadVillagesNear(int tilex, int tiley, int range) {
        ArrayList<DeadVillage> toReturn = new ArrayList<DeadVillage>();
        for (DeadVillage dv : deadVillages.values()) {
            if (dv.getStartX() - range > tilex || dv.getEndX() + range < tilex || dv.getStartY() - range > tiley || dv.getEndY() + range < tiley) continue;
            toReturn.add(dv);
        }
        return toReturn;
    }

    public static final DeadVillage getDeadVillage(long deadVillageId) {
        return deadVillages.get(deadVillageId);
    }
}


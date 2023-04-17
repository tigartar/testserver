/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.endgames;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.Enchants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class EndGameItems
implements MiscConstants,
ItemTypes,
Enchants,
TimeConstants {
    public static final Map<Long, EndGameItem> altars = new HashMap<Long, EndGameItem>();
    private static final Map<Long, EndGameItem> artifacts = new HashMap<Long, EndGameItem>();
    private static final Logger logger = Logger.getLogger(EndGameItems.class.getName());
    private static final String LOAD_ENDGAMEITEMS = "SELECT * FROM ENDGAMEITEMS";
    private static float posx = 0.0f;
    private static float posy = 0.0f;
    private static int tileX = 0;
    private static int tileY = 0;
    private static final LinkedList<Kingdom> missingCrowns = new LinkedList();
    public static final byte chargeDecay = 10;
    private static long lastRechargedItem = 0L;

    private EndGameItems() {
    }

    public static final void createAltars() {
        EndGameItem eg;
        logger.log(Level.INFO, "Creating altars.");
        boolean found = false;
        int startX = (Zones.worldTileSizeX - 10) / 2;
        int startY = Math.min(Zones.worldTileSizeY / 20, 300);
        int tries = 0;
        while (!found && tries < 1000) {
            ++tries;
            float posz = EndGameItems.findPlacementTile(startX, startY);
            if (posz <= 0.0f) {
                if ((startX += Math.min(Zones.worldTileSizeX / 20, 300)) >= Zones.worldTileSizeX - Math.min(Zones.worldTileSizeX / 20, 100)) {
                    startX = (Zones.worldTileSizeX - 10) / 2;
                    startY += Math.min(Zones.worldTileSizeY / 20, 100);
                }
                if (startY < Zones.worldTileSizeY - Math.min(Zones.worldTileSizeY / 20, 100)) continue;
                break;
            }
            found = true;
        }
        if (!found) {
            logger.log(Level.WARNING, "Failed to locate a good spot to create holy altar. Exiting.");
            return;
        }
        posx = tileX << 2;
        posy = tileY << 2;
        try {
            Item holy = ItemFactory.createItem(327, 90.0f, posx, posy, 180.0f, true, (byte)0, -10L, null);
            holy.bless(1);
            holy.enchant((byte)5);
            eg = new EndGameItem(holy, true, 68, true);
            altars.put(new Long(eg.getWurmid()), eg);
            logger.log(Level.INFO, "Created holy altar at " + posx + ", " + posy + ".");
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
        tileX = 0;
        tileY = 0;
        found = false;
        startX = (Zones.worldTileSizeX - 10) / 2;
        startY = Math.max(Zones.worldTileSizeY - 300, Zones.worldTileSizeY - Zones.worldTileSizeY / 20);
        tries = 0;
        while (!found && tries < 1000) {
            ++tries;
            float posz = EndGameItems.findPlacementTile(startX, startY);
            if (posz <= 0.0f) {
                if ((startX += Math.min(Zones.worldTileSizeX / 20, 300)) >= Zones.worldTileSizeX - Math.min(Zones.worldTileSizeX / 20, 100)) {
                    startX = (Zones.worldTileSizeX - 10) / 2;
                    startY -= Math.min(Zones.worldTileSizeY / 20, 100);
                }
                if (startY > 0) continue;
                break;
            }
            found = true;
        }
        if (!found) {
            logger.log(Level.WARNING, "Failed to locate a good spot to create unholy altar. Exiting.");
            return;
        }
        posx = tileX << 2;
        posy = tileY << 2;
        try {
            Item unholy = ItemFactory.createItem(328, 90.0f, posx, posy, 180.0f, true, (byte)0, -10L, null);
            unholy.bless(4);
            unholy.enchant((byte)8);
            eg = new EndGameItem(unholy, false, 68, true);
            altars.put(new Long(eg.getWurmid()), eg);
            logger.log(Level.INFO, "Created unholy altar at " + posx + ", " + posy + ".");
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
    }

    @Nullable
    public static final EndGameItem getEvilAltar() {
        if (altars != null) {
            for (EndGameItem eg : altars.values()) {
                if (eg.getItem().getTemplateId() != 328) continue;
                return eg;
            }
        }
        return null;
    }

    @Nullable
    public static final EndGameItem getGoodAltar() {
        if (altars != null) {
            for (EndGameItem eg : altars.values()) {
                if (eg.getItem().getTemplateId() != 327) continue;
                return eg;
            }
        }
        return null;
    }

    public static final float findPlacementTile(int tx, int ty) {
        float maxZ = 0.0f;
        if (Zones.isWithinDuelRing(tx, ty, tx + 20, ty + 20)) {
            return maxZ;
        }
        for (int x = 0; x < 20; ++x) {
            for (int y = 0; y < 20; ++y) {
                int tile = Server.surfaceMesh.getTile(tx + x, ty + y);
                float z = Tiles.decodeHeight(tile);
                byte ttype = Tiles.decodeType(tile);
                if (ttype == Tiles.Tile.TILE_ROCK.id || ttype == Tiles.Tile.TILE_CLIFF.id || ttype == Tiles.Tile.TILE_HOLE.id || !(z > 0.0f) || !(z > maxZ) || !(z < 700.0f)) continue;
                tileX = tx + x;
                tileY = ty + y;
                maxZ = z;
            }
        }
        return maxZ;
    }

    public static final void createArtifacts() {
        try {
            Item rod = ItemFactory.createItem(329, 90.0f, (byte)3, null);
            rod.bless(1);
            rod.enchant((byte)5);
            EndGameItems.placeArtifact(rod);
            EndGameItem eg = new EndGameItem(rod, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item crownmight = ItemFactory.createItem(330, 90.0f, (byte)3, null);
            crownmight.bless(2);
            crownmight.enchant((byte)6);
            EndGameItems.placeArtifact(crownmight);
            eg = new EndGameItem(crownmight, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item charmOfFo = ItemFactory.createItem(331, 90.0f, (byte)3, null);
            charmOfFo.bless(1);
            charmOfFo.enchant((byte)5);
            EndGameItems.placeArtifact(charmOfFo);
            eg = new EndGameItem(charmOfFo, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item vynorasEye = ItemFactory.createItem(332, 90.0f, (byte)3, null);
            vynorasEye.bless(3);
            vynorasEye.enchant((byte)7);
            EndGameItems.placeArtifact(vynorasEye);
            eg = new EndGameItem(vynorasEye, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item vynorasEar = ItemFactory.createItem(333, 90.0f, (byte)3, null);
            vynorasEar.bless(3);
            vynorasEar.enchant((byte)7);
            EndGameItems.placeArtifact(vynorasEar);
            eg = new EndGameItem(vynorasEar, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item vynorasMouth = ItemFactory.createItem(334, 90.0f, (byte)3, null);
            vynorasMouth.bless(3);
            vynorasEar.enchant((byte)7);
            EndGameItems.placeArtifact(vynorasMouth);
            eg = new EndGameItem(vynorasMouth, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item fingerOfFo = ItemFactory.createItem(335, 90.0f, (byte)3, null);
            fingerOfFo.bless(1);
            fingerOfFo.enchant((byte)5);
            EndGameItems.placeArtifact(fingerOfFo);
            eg = new EndGameItem(fingerOfFo, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item swordOfMagranon = ItemFactory.createItem(336, 90.0f, (byte)3, null);
            swordOfMagranon.bless(2);
            swordOfMagranon.enchant((byte)4);
            EndGameItems.placeArtifact(swordOfMagranon);
            eg = new EndGameItem(swordOfMagranon, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item hammerOfMagranon = ItemFactory.createItem(337, 90.0f, (byte)3, null);
            hammerOfMagranon.bless(2);
            hammerOfMagranon.enchant((byte)4);
            EndGameItems.placeArtifact(hammerOfMagranon);
            eg = new EndGameItem(hammerOfMagranon, true, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item libilasScale = ItemFactory.createItem(338, 90.0f, (byte)3, null);
            libilasScale.bless(4);
            libilasScale.enchant((byte)8);
            EndGameItems.placeArtifact(libilasScale);
            eg = new EndGameItem(libilasScale, false, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item orbOfDoom = ItemFactory.createItem(339, 90.0f, (byte)3, null);
            orbOfDoom.bless(4);
            orbOfDoom.enchant((byte)8);
            EndGameItems.placeArtifact(orbOfDoom);
            eg = new EndGameItem(orbOfDoom, false, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
            Item sceptreOfAscension = ItemFactory.createItem(340, 90.0f, (byte)3, null);
            sceptreOfAscension.bless(4);
            sceptreOfAscension.enchant((byte)1);
            EndGameItems.placeArtifact(sceptreOfAscension);
            eg = new EndGameItem(sceptreOfAscension, false, 69, true);
            artifacts.put(new Long(eg.getWurmid()), eg);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, "Failed to create item: " + nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, "Failed to create item: " + fe.getMessage(), fe);
        }
    }

    public static final void placeArtifact(Item artifact) {
        boolean found = false;
        while (!found) {
            VolaTile t;
            int x = Server.rand.nextInt(Zones.worldTileSizeX);
            int y = Server.rand.nextInt(Zones.worldTileSizeX);
            int tile = Server.surfaceMesh.getTile(x, y);
            int rocktile = Server.rockMesh.getTile(x, y);
            float th = Tiles.decodeHeightAsFloat(tile);
            float rh = Tiles.decodeHeightAsFloat(rocktile);
            FocusZone hoderZone = FocusZone.getHotaZone();
            assert (hoderZone != null);
            float seth = 0.0f;
            if (!(th > 4.0f) || !(rh > 4.0f)) continue;
            if (th - rh >= 1.0f) {
                seth = Math.max(1, Server.rand.nextInt((int)(th * 10.0f - 5.0f - rh * 10.0f)));
            }
            if (!(seth > 0.0f) || (t = Zones.getTileOrNull(x, y, true)) != null && (t.getStructure() != null || t.getVillage() != null || t.getZone() == hoderZone)) continue;
            found = true;
            artifact.setPosXYZ((x << 2) + 2, (y << 2) + 2, rh + (seth /= 10.0f));
            artifact.setAuxData((byte)30);
            logger.log(Level.INFO, "Placed " + artifact.getName() + " at " + x + "," + y + " at height " + (rh + seth) + " rockheight=" + rh + " tileheight=" + th);
        }
    }

    public static final Item[] getArtifactDugUp(int x, int y, float height, boolean allCornersRock) {
        HashSet<Item> found = new HashSet<Item>();
        for (EndGameItem artifact : artifacts.values()) {
            if ((long)artifact.getItem().getZoneId() != -10L || artifact.getItem().getOwnerId() != -10L || (int)artifact.getItem().getPosX() >> 2 != x || (int)artifact.getItem().getPosY() >> 2 != y || !(height <= artifact.getItem().getPosZ()) && !allCornersRock) continue;
            found.add(artifact.getItem());
            artifact.setLastMoved(System.currentTimeMillis());
        }
        return found.toArray(new Item[found.size()]);
    }

    public static final EndGameItem getArtifactAtTile(int x, int y) {
        for (EndGameItem artifact : artifacts.values()) {
            if ((long)artifact.getItem().getZoneId() != -10L || artifact.getItem().getOwnerId() != -10L || (int)artifact.getItem().getPosX() >> 2 != x || (int)artifact.getItem().getPosY() >> 2 != y) continue;
            return artifact;
        }
        return null;
    }

    public static final void deleteEndGameItem(EndGameItem eg) {
        if (eg != null) {
            if (eg.getItem().isHugeAltar()) {
                altars.remove(new Long(eg.getWurmid()));
            } else if (eg.getItem().isArtifact()) {
                artifacts.remove(new Long(eg.getWurmid()));
            }
            eg.delete();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadEndGameItems() {
        logger.info("Loading End Game Items.");
        long now = System.nanoTime();
        if (Servers.localServer.id == 3 || Servers.localServer.id == 12 || Servers.localServer.isChallengeServer() || Server.getInstance().isPS() && Constants.loadEndGameItems) {
            ResultSet rs;
            PreparedStatement ps;
            Connection dbcon;
            block13: {
                dbcon = null;
                ps = null;
                rs = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(LOAD_ENDGAMEITEMS);
                    rs = ps.executeQuery();
                    long iid = -10L;
                    boolean holy = true;
                    short type = 0;
                    boolean found = false;
                    boolean foundAltar = false;
                    long lastMoved = 0L;
                    while (rs.next()) {
                        iid = rs.getLong("WURMID");
                        holy = rs.getBoolean("HOLY");
                        type = rs.getShort("TYPE");
                        lastMoved = rs.getLong("LASTMOVED");
                        try {
                            Item item = Items.getItem(iid);
                            EndGameItem eg = new EndGameItem(item, holy, type, false);
                            eg.lastMoved = lastMoved;
                            if (type == 68) {
                                eg.setLastMoved(System.currentTimeMillis());
                                foundAltar = true;
                                altars.put(new Long(iid), eg);
                                continue;
                            }
                            if (type == 69) {
                                found = true;
                                artifacts.put(new Long(iid), eg);
                                if (!logger.isLoggable(Level.FINE)) continue;
                                logger.fine("Loaded Artifact, ID: " + iid + ", " + eg);
                                continue;
                            }
                            logger.warning("End Game Items should only be Huge Altars or Artifiacts not type " + type + ", ID: " + iid + ", " + eg);
                        }
                        catch (NoSuchItemException nsi) {
                            if (Server.getInstance().isPS()) {
                                logger.log(Level.INFO, "Endgame item missing: " + iid + ". Deleting entry.");
                                EndGameItem.delete(iid);
                                if (type != 68) continue;
                                logger.log(Level.INFO, (holy ? "White Light" : "Black Light") + " altar is missing. Destroy the " + (!holy ? "White Light" : "Black Light") + " altar to respawn both.");
                                continue;
                            }
                            logger.log(Level.WARNING, "Endgame item missing: " + iid, nsi);
                        }
                    }
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    if (!found) {
                        EndGameItems.createArtifacts();
                    } else {
                        EndGameItems.setArtifactsInWorld();
                    }
                    if (foundAltar) break block13;
                    EndGameItems.createAltars();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, "Failed to load item datas: " + sqx.getMessage(), sqx);
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
        int numberOfAltars = altars != null ? altars.size() : 0;
        int numberOfArtifacts = artifacts != null ? artifacts.size() : 0;
        logger.log(Level.INFO, "Loaded " + numberOfAltars + " altars and " + numberOfArtifacts + " artifacts. That took " + (float)(System.nanoTime() - now) / 1000000.0f + " ms.");
    }

    public static EndGameItem getEndGameItem(Item item) {
        if (item.isHugeAltar()) {
            return altars.get(new Long(item.getWurmId()));
        }
        if (item.isArtifact()) {
            return artifacts.get(new Long(item.getWurmId()));
        }
        return null;
    }

    public static final boolean mayRechargeItem() {
        return System.currentTimeMillis() - lastRechargedItem > 60000L;
    }

    public static final void touchRecharge() {
        lastRechargedItem = System.currentTimeMillis();
    }

    public static final void destroyHugeAltar(Item altar, Creature destroyer) {
        EndGameItem eg = altars.get(new Long(altar.getWurmId()));
        if (eg != null) {
            Player[] players;
            Server.getInstance().broadCastAlert("The " + altar.getName() + " has fallen to the hands of " + destroyer.getName() + "!", true, (byte)4);
            HistoryManager.addHistory(destroyer.getName(), "Destroyed the " + altar.getName() + ".");
            if (destroyer.isPlayer()) {
                float sx = altar.getPosX() - 100.0f;
                float ex = altar.getPosX() + 100.0f;
                float sy = altar.getPosY() - 100.0f;
                float ey = altar.getPosY() + 100.0f;
                for (Player p : Players.getInstance().getPlayers()) {
                    if (!(p.getPosX() > sx) || !(p.getPosX() < ex) || !(p.getPosY() > sy) || !(p.getPosY() < ey) || p.getKingdomId() != destroyer.getKingdomId()) continue;
                    p.addTitle(Titles.Title.Altar_Destroyer);
                    if (eg.isHoly()) {
                        p.achievement(356);
                        continue;
                    }
                    p.achievement(357);
                }
            }
            for (Player lPlayer : players = Players.getInstance().getPlayers()) {
                if (eg.isHoly()) {
                    if (lPlayer.getDeity() != null && lPlayer.getDeity().isHateGod()) {
                        lPlayer.setFarwalkerSeconds((byte)100);
                        lPlayer.healRandomWound(100);
                        continue;
                    }
                    if (lPlayer.getDeity() == null || lPlayer.getDeity().isHateGod()) continue;
                    lPlayer.getCommunicator().sendCombatAlertMessage("Your life force is drained, as it is used to heal the " + altar.getName() + "!");
                    lPlayer.addWoundOfType(null, (byte)9, 1, false, 1.0f, false, 5000.0, 0.0f, 0.0f, false, false);
                    continue;
                }
                if (lPlayer.getDeity() != null && !lPlayer.getDeity().isHateGod()) {
                    lPlayer.setFarwalkerSeconds((byte)100);
                    lPlayer.healRandomWound(100);
                    continue;
                }
                if (lPlayer.getDeity() == null || !lPlayer.getDeity().isHateGod()) continue;
                lPlayer.getCommunicator().sendCombatAlertMessage("Your life force is drained, as it is used to heal the " + altar.getName() + "!");
                lPlayer.addWoundOfType(null, (byte)9, 1, false, 1.0f, false, 5000.0, 0.0f, 0.0f, false, false);
            }
            EndGameItems.healAndTeleportAltar(eg);
            EndGameItems.hideRandomArtifact(eg.isHoly());
        }
    }

    private static final void healAndTeleportAltar(EndGameItem altar) {
        Player[] p;
        Item altarItem = altar.getItem();
        altarItem.putInVoid();
        altarItem.setDamage(0.0f);
        for (Player lPlayer : p = Players.getInstance().getPlayers()) {
            lPlayer.getCommunicator().sendRemoveEffect(altar.getWurmid());
        }
        boolean found = false;
        int randX = Zones.worldTileSizeX - 200;
        int randY = Zones.worldTileSizeY / 2;
        int startX = Zones.safeTileX(100 + Server.rand.nextInt(randX));
        int startY = Zones.safeTileY(100 + Server.rand.nextInt(randY));
        if (!altar.isHoly()) {
            startY = Zones.safeTileY(Zones.worldTileSizeY / 2 + startY);
        }
        int tries = 0;
        float posz = 0.0f;
        while (!found && tries < 1000) {
            ++tries;
            posz = EndGameItems.findPlacementTile(startX, startY);
            if (Villages.getVillageWithPerimeterAt(tileX, tileY, true) != null) {
                posz = -1.0f;
            }
            if (posz <= 0.0f) {
                startX = Zones.safeTileX(100 + Server.rand.nextInt(randX));
                startY = Zones.safeTileY(100 + Server.rand.nextInt(randY));
                if (altar.isHoly()) continue;
                startY = Zones.safeTileY(Zones.worldTileSizeY / 2 + startY);
                continue;
            }
            found = true;
        }
        if (!found) {
            logger.log(Level.WARNING, "Failed to locate a good spot to create holy altar. Exiting.");
            return;
        }
        posx = (tileX << 2) + 2;
        posy = (tileY << 2) + 2;
        altarItem.setPosXYZ(posx, posy, posz);
        try {
            Zone z = Zones.getZone(tileX, tileY, true);
            z.addItem(altarItem);
            if (altar.isHoly()) {
                for (Player lPlayer : p) {
                    lPlayer.getCommunicator().sendAddEffect(altar.getWurmid(), (short)2, altar.getItem().getPosX(), altar.getItem().getPosY(), altar.getItem().getPosZ(), (byte)0);
                }
            } else {
                for (Player lPlayer : p) {
                    lPlayer.getCommunicator().sendAddEffect(altar.getWurmid(), (short)3, altar.getItem().getPosX(), altar.getItem().getPosY(), altar.getItem().getPosZ(), (byte)0);
                }
            }
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, nsz.getMessage(), nsz);
        }
    }

    private static final void hideRandomArtifact(boolean holy) {
        EndGameItem[] arts = artifacts.values().toArray(new EndGameItem[artifacts.size()]);
        Item artifactToPlace = null;
        ArrayList<Item> candidates = new ArrayList<Item>();
        for (EndGameItem lArt : arts) {
            Item artifact = lArt.getItem();
            if (!lArt.isInWorld() || lArt.isHoly() != holy) continue;
            candidates.add(artifact);
        }
        if (candidates.size() > 0) {
            artifactToPlace = (Item)candidates.get(Server.rand.nextInt(candidates.size()));
            try {
                Item parent = artifactToPlace.getParent();
                parent.dropItem(artifactToPlace.getWurmId(), false);
                EndGameItems.placeArtifact(artifactToPlace);
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
    }

    public static final void setArtifactsInWorld() {
        EndGameItem[] arts;
        for (EndGameItem lArt : arts = artifacts.values().toArray(new EndGameItem[artifacts.size()])) {
            CreaturePos stat;
            Item artifact = lArt.getItem();
            if (artifact.getOwnerId() == -10L || (stat = CreaturePos.getPosition(artifact.getOwnerId())) == null || !(stat.getPosX() > 0.0f)) continue;
            try {
                Item parent = artifact.getParent();
                parent.dropItem(artifact.getWurmId(), false);
                Zone z = Zones.getZone((int)stat.getPosX() >> 2, (int)stat.getPosY() >> 2, stat.getLayer() >= 0);
                artifact.setPosXY(stat.getPosX(), stat.getPosY());
                z.addItem(artifact);
                logger.log(Level.INFO, "Zone " + z.getId() + " added " + artifact.getName() + " at " + ((int)stat.getPosX() >> 2) + "," + ((int)stat.getPosY() >> 2));
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, artifact.getName() + ": " + nsi.getMessage(), nsi);
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, artifact.getName() + ": " + nsz.getMessage(), nsz);
            }
        }
    }

    public static final void pollAll() {
        EndGameItem[] arts;
        for (EndGameItem lArt : arts = artifacts.values().toArray(new EndGameItem[artifacts.size()])) {
            if (!lArt.isInWorld() || System.currentTimeMillis() - lArt.getLastMoved() <= (Servers.isThisATestServer() ? 60000L : 604800000L)) continue;
            lArt.setLastMoved(System.currentTimeMillis());
            Item artifact = lArt.getItem();
            if (artifact.getAuxData() <= 0) {
                EndGameItems.moveArtifact(artifact);
                continue;
            }
            artifact.setAuxData((byte)Math.max(0, artifact.getAuxData() - 10));
            try {
                if (artifact.getOwner() == -10L) continue;
                Creature owner = Server.getInstance().getCreature(artifact.getOwner());
                owner.getCommunicator().sendNormalServerMessage(artifact.getName() + " vibrates faintly.");
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
            }
            catch (NotOwnedException notOwnedException) {
                // empty catch block
            }
        }
    }

    private static final void moveArtifact(Item artifact) {
        String act;
        try {
            if (artifact.getOwner() != -10L) {
                Creature owner = Server.getInstance().getCreature(artifact.getOwner());
                owner.getCommunicator().sendNormalServerMessage(artifact.getName() + " disappears. It has fulfilled its mission.");
            }
        }
        catch (NoSuchCreatureException owner) {
        }
        catch (NoSuchPlayerException owner) {
        }
        catch (NotOwnedException owner) {
            // empty catch block
        }
        switch (Server.rand.nextInt(6)) {
            case 0: {
                act = "is reported to have disappeared.";
                break;
            }
            case 1: {
                act = "is gone missing.";
                break;
            }
            case 2: {
                act = "returned to the depths.";
                break;
            }
            case 3: {
                act = "seems to have decided to leave.";
                break;
            }
            case 4: {
                act = "has found a new location.";
                break;
            }
            default: {
                act = "has vanished.";
            }
        }
        HistoryManager.addHistory("The " + artifact.getName(), act);
        artifact.putInVoid();
        EndGameItems.placeArtifact(artifact);
    }

    public static final void destroyArtifacts() {
        EndGameItem[] arts;
        for (EndGameItem lArt : arts = artifacts.values().toArray(new EndGameItem[artifacts.size()])) {
            Item artifact = lArt.getItem();
            try {
                if (artifact.getOwner() != -10L) {
                    Creature owner = Server.getInstance().getCreature(artifact.getOwner());
                    owner.getCommunicator().sendNormalServerMessage(artifact.getName() + " disappears. It has fulfilled its mission.");
                }
            }
            catch (NoSuchCreatureException noSuchCreatureException) {
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
            }
            catch (NotOwnedException notOwnedException) {
                // empty catch block
            }
            Items.destroyItem(artifact.getWurmId());
            lArt.destroy();
        }
    }

    public static final String locateEndGameItem(int templateId, Creature performer) {
        String toReturn;
        block44: {
            toReturn = "The artifact was hidden from view by the gods.";
            if (Servers.localServer.HOMESERVER) {
                if (Servers.localServer.serverEast != null && !Servers.localServer.serverEast.HOMESERVER) {
                    return "You feel a faint indication far to the east.";
                }
                if (Servers.localServer.serverSouth != null && !Servers.localServer.serverSouth.HOMESERVER) {
                    return "You feel a faint indication far to the south.";
                }
                if (Servers.localServer.serverWest != null && !Servers.localServer.serverWest.HOMESERVER) {
                    return "You feel a faint indication far to the west.";
                }
                if (Servers.localServer.serverNorth != null && !Servers.localServer.serverNorth.HOMESERVER) {
                    return "You feel a faint indication far to the north.";
                }
                return toReturn;
            }
            EndGameItem itemsearched2 = null;
            if (templateId == -1) {
                int s;
                if (Server.rand.nextBoolean()) {
                    missingCrowns.clear();
                    Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
                    for (int x = 0; x < kingdoms.length; ++x) {
                        King k;
                        if (!kingdoms[x].isCustomKingdom() || !kingdoms[x].existsHere() || (k = King.getKing(kingdoms[x].getId())) != null) continue;
                        missingCrowns.add(kingdoms[x]);
                    }
                    if (missingCrowns.size() > 0) {
                        Item[] _items;
                        int crownToLookFor = Server.rand.nextInt(missingCrowns.size());
                        Kingdom toLookFor = missingCrowns.get(crownToLookFor);
                        for (Item lItem : _items = Items.getAllItems()) {
                            if (!lItem.isRoyal() || lItem.getKingdom() != toLookFor.getId()) continue;
                            itemsearched2 = new EndGameItem(lItem, false, 122, false);
                        }
                    }
                }
                if (itemsearched2 == null && (s = artifacts.size()) > 0) {
                    int num = Server.rand.nextInt(s);
                    int x = 0;
                    for (EndGameItem itemsearched2 : artifacts.values()) {
                        if (x != num) {
                            ++x;
                            continue;
                        }
                        break;
                    }
                }
            } else {
                for (EndGameItem itemsearched2 : artifacts.values()) {
                    if (itemsearched2.getItem().getTemplateId() == templateId) break;
                }
            }
            String name = "artifact";
            if (itemsearched2 != null && itemsearched2.getItem() != null) {
                Kingdom k;
                toReturn = "";
                name = itemsearched2.getItem().getName();
                if (itemsearched2.getType() == 122 && (k = Kingdoms.getKingdom(itemsearched2.getItem().getKingdom())) != null) {
                    name = itemsearched2.getItem().getName() + " of " + k.getName();
                }
                int tilex = (int)itemsearched2.getItem().getPosX() >> 2;
                int tiley = (int)itemsearched2.getItem().getPosY() >> 2;
                if (itemsearched2.getItem().getOwnerId() != -10L) {
                    try {
                        Creature c = Server.getInstance().getCreature(itemsearched2.getItem().getOwnerId());
                        toReturn = toReturn + "The " + name + " is carried by " + c.getName() + ". ";
                        VolaTile t = c.getCurrentTile();
                        if (t == null) break block44;
                        if (t.getVillage() != null) {
                            toReturn = toReturn + c.getName() + " is in the settlement of " + t.getVillage().getName() + ". ";
                        }
                        if (t.getStructure() != null) {
                            toReturn = toReturn + c.getName() + " is in the house of " + t.getStructure().getName() + ". ";
                        }
                    }
                    catch (NoSuchCreatureException nsc) {
                        toReturn = toReturn + "In your vision, you can only discern a shadow that carries the " + name + ". ";
                    }
                    catch (NoSuchPlayerException nsp) {
                        toReturn = toReturn + "In your vision, you can only discern a shadow that carries the " + name + ". ";
                    }
                } else if (itemsearched2.isInWorld()) {
                    VolaTile t = Zones.getTileOrNull(tilex, tiley, itemsearched2.getItem().isOnSurface());
                    if (t != null) {
                        if (t.getVillage() != null) {
                            toReturn = toReturn + "The " + name + " is in the settlement of " + t.getVillage().getName() + ". ";
                        }
                        if (t.getStructure() != null) {
                            toReturn = toReturn + "The " + name + " is in the house of " + t.getStructure().getName() + ". ";
                        }
                        try {
                            long parentId;
                            Item parent;
                            if (itemsearched2.getItem() != null && (parent = Items.getItem(parentId = itemsearched2.getItem().getTopParent())) != itemsearched2.getItem()) {
                                toReturn = toReturn + "It is within a " + parent.getName() + ".";
                            }
                        }
                        catch (NoSuchItemException parentId) {
                            // empty catch block
                        }
                        toReturn = toReturn + "The " + name + " is in the wild. ";
                        VolaTile ct = performer.getCurrentTile();
                        if (ct != null) {
                            int ctx = ct.tilex;
                            int cty = ct.tiley;
                            int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                            int dir = MethodsCreatures.getDir(performer, tilex, tiley);
                            String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                            toReturn = toReturn + EndGameItems.getDistanceString(mindist, name, direction, true);
                        }
                    } else {
                        try {
                            Zone z = Zones.getZone(tilex, tiley, true);
                            Village[] villages = z.getVillages();
                            if (villages.length > 0) {
                                for (Village lVillage : villages) {
                                    toReturn = toReturn + "The " + name + " is near the settlement of " + lVillage.getName() + ". ";
                                }
                            } else {
                                Structure[] structs = z.getStructures();
                                if (structs.length > 0) {
                                    for (Structure lStruct : structs) {
                                        toReturn = toReturn + "The " + name + " is near " + lStruct.getName() + ". ";
                                    }
                                } else {
                                    VolaTile ct = performer.getCurrentTile();
                                    if (ct != null) {
                                        int ctx = ct.tilex;
                                        int cty = ct.tiley;
                                        int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                                        int dir = MethodsCreatures.getDir(performer, tilex, tiley);
                                        String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                                        toReturn = toReturn + EndGameItems.getDistanceString(mindist, name, direction, true);
                                    }
                                }
                            }
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.WARNING, "No Zone At " + tilex + ", " + tiley + " surf=true for item " + itemsearched2.getItem().getName() + ".", nsz);
                        }
                    }
                } else {
                    toReturn = toReturn + "The " + name + " has not yet been revealed. ";
                    VolaTile ct = performer.getCurrentTile();
                    if (ct != null) {
                        int ctx = ct.tilex;
                        int cty = ct.tiley;
                        int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                        int dir = MethodsCreatures.getDir(performer, tilex, tiley);
                        String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
                        toReturn = toReturn + EndGameItems.getDistanceString(mindist, name, direction, true);
                    }
                }
            }
        }
        return toReturn;
    }

    public static final EndGameItem[] getArtifacts() {
        return artifacts.values().toArray(new EndGameItem[artifacts.values().size()]);
    }

    public static String getEpicPlayerLocateString(int mindist, String name, String direction) {
        String toReturn = "";
        toReturn = mindist == 0 ? toReturn + "You are practically standing on the " + name + "! " : (mindist < 1 ? toReturn + "The " + name + " is " + direction + " a few steps away! " : (mindist < 4 ? toReturn + "The " + name + " is " + direction + " a stone's throw away! " : (mindist < 6 ? toReturn + "The " + name + " is " + direction + " very close. " : (mindist < 10 ? toReturn + "The " + name + " is " + direction + " pretty close by. " : (mindist < 20 ? toReturn + "The " + name + " is " + direction + " fairly close by. " : (mindist < 50 ? toReturn + "The " + name + " is some distance away " + direction + ". " : (mindist < 200 ? toReturn + "The " + name + " is quite some distance away " + direction + ". " : toReturn + "No such soul found.")))))));
        return toReturn;
    }

    public static final String getDistanceString(int mindist, String name, String direction, boolean includeThe) {
        String toReturn = "";
        toReturn = mindist == 0 ? toReturn + "You are practically standing on the " + name + "! " : (mindist < 1 ? toReturn + "The " + name + " is " + direction + " a few steps away! " : (mindist < 4 ? toReturn + "The " + name + " is " + direction + " a stone's throw away! " : (mindist < 6 ? toReturn + "The " + name + " is " + direction + " very close. " : (mindist < 10 ? toReturn + "The " + name + " is " + direction + " pretty close by. " : (mindist < 20 ? toReturn + "The " + name + " is " + direction + " fairly close by. " : (mindist < 50 ? toReturn + "The " + name + " is some distance away " + direction + ". " : (mindist < 200 ? toReturn + "The " + name + " is quite some distance away " + direction + ". " : (mindist < 500 ? toReturn + "The " + name + " is rather a long distance away " + direction + ". " : (mindist < 1000 ? toReturn + "The " + name + " is pretty far away " + direction + ". " : (mindist < 2000 ? toReturn + "The " + name + " is far away " + direction + ". " : toReturn + "The " + name + " is very far away " + direction + ". "))))))))));
        return toReturn;
    }

    public static final String locateRandomEndGameItem(Creature performer) {
        return EndGameItems.locateEndGameItem(-1, performer);
    }

    public static final void relocateAllEndGameItems() {
        for (EndGameItem eg : artifacts.values()) {
            eg.setLastMoved(System.currentTimeMillis());
            EndGameItems.moveArtifact(eg.getItem());
        }
        for (EndGameItem altar : altars.values()) {
            Items.destroyItem(altar.getItem().getWurmId());
            altar.delete();
        }
        altars.clear();
    }
}


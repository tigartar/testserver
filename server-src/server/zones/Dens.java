/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Dens
implements CreatureTemplateIds {
    private static final String GET_DENS = "select * from DENS";
    private static final String DELETE_DEN = "DELETE FROM DENS  where TEMPLATEID=?";
    private static final String CREATE_DEN = "insert into DENS(TEMPLATEID,TILEX, TILEY, SURFACED) values(?,?,?,?)";
    private static final Logger logger = Logger.getLogger(Dens.class.getName());
    private static final Map<Integer, Den> dens = new HashMap<Integer, Den>();

    private Dens() {
    }

    private static void addDen(Den den) {
        dens.put(den.getTemplateId(), den);
    }

    private static void removeDen(int templateId) {
        dens.remove(templateId);
    }

    public static Den getDen(int templateId) {
        return dens.get(templateId);
    }

    public static Map<Integer, Den> getDens() {
        return Collections.unmodifiableMap(dens);
    }

    public static Den getDen(int tilex, int tiley) {
        for (Den d : dens.values()) {
            if (d.getTilex() != tilex || d.getTiley() != tiley) continue;
            return d;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadDens() {
        logger.info("Loading dens");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_DENS);
            rs = ps.executeQuery();
            int tid = -1;
            int tilex = 0;
            int tiley = 0;
            boolean surfaced = false;
            while (rs.next()) {
                tid = rs.getInt("TEMPLATEID");
                tilex = rs.getInt("TILEX");
                tiley = rs.getInt("TILEY");
                surfaced = rs.getBoolean("SURFACED");
                if (tid <= 0) continue;
                Den den = new Den(tid, tilex, tiley, surfaced);
                Dens.addDen(den);
                if (!logger.isLoggable(Level.FINE)) continue;
                logger.fine("Loaded Den: " + den);
            }
            Dens.checkDens(false);
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading Dens - " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + dens.size() + " dens from the database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded " + dens.size() + " dens from the database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + dens.size() + " dens from the database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    public static void checkDens(boolean whileRunning) {
        Dens.checkTemplate(16, whileRunning);
        Dens.checkTemplate(89, whileRunning);
        Dens.checkTemplate(91, whileRunning);
        Dens.checkTemplate(90, whileRunning);
        Dens.checkTemplate(92, whileRunning);
        Dens.checkTemplate(17, whileRunning);
        Dens.checkTemplate(18, whileRunning);
        Dens.checkTemplate(19, whileRunning);
        Dens.checkTemplate(104, whileRunning);
        Dens.checkTemplate(103, whileRunning);
        Dens.checkTemplate(20, whileRunning);
        Dens.checkTemplate(22, whileRunning);
        Dens.checkTemplate(27, whileRunning);
        Dens.checkTemplate(11, whileRunning);
        Dens.checkTemplate(26, whileRunning);
        Dens.checkTemplate(23, whileRunning);
        Constants.respawnUniques = false;
    }

    private static final Den getDragonSpawnTop(int templateId) {
        switch (templateId) {
            case 16: {
                return Zones.getNorthTop(templateId);
            }
            case 89: {
                return Zones.getWestTop(templateId);
            }
            case 91: {
                return Zones.getSouthTop(templateId);
            }
            case 90: {
                return Zones.getNorthTop(templateId);
            }
            case 92: {
                return Zones.getEastTop(templateId);
            }
            case 17: {
                return Zones.getSouthTop(templateId);
            }
            case 18: {
                return Zones.getEastTop(templateId);
            }
            case 19: {
                return Zones.getWestTop(templateId);
            }
            case 103: {
                return Zones.getSouthTop(templateId);
            }
            case 104: {
                return Zones.getNorthTop(templateId);
            }
        }
        return Zones.getRandomTop();
    }

    private static void checkTemplate(int templateId, boolean whileRunning) {
        block27: {
            try {
                Den den;
                CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(templateId);
                boolean creatureExists = Creatures.getInstance().creatureWithTemplateExists(templateId);
                if (Constants.respawnUniques || whileRunning) {
                    Den d;
                    if (whileRunning && Server.rand.nextInt(300) > 0) {
                        return;
                    }
                    if (!creatureExists && (d = dens.get(templateId)) != null) {
                        Dens.deleteDen(templateId);
                    }
                } else {
                    return;
                }
                if (!dens.containsKey(templateId)) {
                    VolaTile villtile;
                    Village vill;
                    den = null;
                    if (CreatureTemplate.isDragon(templateId)) {
                        if (!Servers.localServer.isChallengeServer() && (den = Constants.respawnUniques ? Dens.getDragonSpawnTop(templateId) : (Server.rand.nextBoolean() ? Zones.getRandomTop() : Zones.getRandomForest(templateId))) != null) {
                            den = Dens.createDen(den.getTemplateId(), den.getTilex(), den.getTiley(), den.isSurfaced());
                        }
                    } else {
                        if (template.getLeaderTemplateId() > 0) {
                            den = Dens.getDen(template.getLeaderTemplateId());
                            if (den != null) {
                                den.setTemplateId(templateId);
                            }
                        } else {
                            den = Zones.getRandomForest(templateId);
                        }
                        if (den != null) {
                            den = Dens.createDen(den.getTemplateId(), den.getTilex(), den.getTiley(), den.isSurfaced());
                        }
                    }
                    if (den == null) break block27;
                    if (template.isUnique() && (vill = (villtile = Zones.getOrCreateTile(den.getTilex(), den.getTiley(), den.isSurfaced())).getVillage()) != null) {
                        logger.log(Level.INFO, "Unique spawn " + template.getName() + ", on deed " + vill.getName() + ".");
                        Dens.removeDen(templateId);
                        return;
                    }
                    if (!template.isUnique()) {
                        try {
                            Zone zone = Zones.getZone(den.getTilex(), den.getTiley(), den.isSurfaced());
                            zone.den = den;
                            logger.log(Level.INFO, "Zone at " + den.getTilex() + ", " + den.getTiley() + " now spawning " + template.getName() + " (" + den.getTemplateId() + ")");
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.WARNING, "Den at " + den.getTilex() + ", " + den.getTiley() + " surf=" + den.isSurfaced() + " - zone does not exist.");
                        }
                    } else if (!creatureExists) {
                        byte ctype = (byte)Math.max(0, Server.rand.nextInt(22) - 10);
                        if (Server.rand.nextInt(3) < 2) {
                            ctype = 0;
                        }
                        if (Server.rand.nextInt(40) == 0) {
                            ctype = 99;
                        }
                        try {
                            Creature.doNew(templateId, ctype, (float)((den.getTilex() << 2) + 2), (float)((den.getTiley() << 2) + 2), 180.0f, den.isSurfaced() ? 0 : -1, template.getName(), template.getSex());
                            logger.log(Level.INFO, "Created " + template.getName() + " at " + den.getTilex() + "," + den.getTiley() + "!");
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, ex.getMessage(), ex);
                        }
                    }
                    Dens.addDen(den);
                    break block27;
                }
                if (!template.isUnique()) {
                    den = Dens.getDen(templateId);
                    try {
                        Zone zone = Zones.getZone(den.getTilex(), den.getTiley(), den.isSurfaced());
                        zone.den = den;
                        logger.log(Level.INFO, "Zone at " + den.getTilex() + ", " + den.getTiley() + " now spawning " + template.getName() + " (" + den.getTemplateId() + ")");
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, "Den at " + den.getTilex() + ", " + den.getTiley() + " surf=" + den.isSurfaced() + " - zone does not exist.");
                    }
                }
            }
            catch (NoSuchCreatureTemplateException nst) {
                logger.log(Level.WARNING, templateId + ":" + nst.getMessage(), nst);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void deleteDen(int templateId) {
        logger.log(Level.INFO, "Deleting den for " + templateId);
        Den d = Dens.getDen(templateId);
        if (d != null) {
            logger.log(Level.INFO, "Den for " + templateId + " was at " + d.getTilex() + "," + d.getTiley());
        }
        Dens.removeDen(templateId);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_DEN);
            ps.setInt(1, templateId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, templateId + ":" + sqx.getMessage(), sqx);
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
    private static Den createDen(int templateId, int tilex, int tiley, boolean surfaced) {
        Den den = new Den(templateId, tilex, tiley, surfaced);
        Dens.addDen(den);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_DEN);
            ps.setInt(1, templateId);
            ps.setInt(2, tilex);
            ps.setInt(3, tiley);
            ps.setBoolean(4, surfaced);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, templateId + ":" + sqx.getMessage(), sqx);
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
        return den;
    }
}


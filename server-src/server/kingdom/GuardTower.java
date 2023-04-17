/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.kingdom;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GuardTower
implements CreatureTemplateIds,
MiscConstants,
TimeConstants,
AttitudeConstants {
    private static final String ADD_GUARD = "INSERT INTO TOWERGUARDS(TOWERID,CREATUREID) VALUES(?,?)";
    private static final String LOAD_GUARDS = "SELECT CREATUREID, RETURNED FROM TOWERGUARDS WHERE TOWERID=?";
    private static final String RETURN_GUARD = "UPDATE TOWERGUARDS SET RETURNED=? WHERE CREATUREID=?";
    private static final String DELETE_TOWER = "DELETE FROM TOWERGUARDS WHERE TOWERID=?";
    private static final String DELETE_CREATURE = "DELETE FROM TOWERGUARDS WHERE CREATUREID=?";
    private static Logger logger = Logger.getLogger(GuardTower.class.getName());
    private Item tower = null;
    private final Set<Creature> guards = new HashSet<Creature>();
    private final LinkedList<Creature> freeGuards = new LinkedList();
    private static final Random rand = new Random();
    private long lastSentAttackMessage = 0L;

    GuardTower(Item item) {
        this.tower = item;
        this.load();
    }

    public Item getTower() {
        return this.tower;
    }

    public byte getKingdom() {
        return this.tower.getAuxData();
    }

    public final String getName() {
        String name = "unknown";
        try {
            name = Players.getInstance().getNameFor(this.tower.lastOwner);
        }
        catch (NoSuchPlayerException | IOException exception) {
            // empty catch block
        }
        return name + " " + this.tower.getWurmId() % 1000L;
    }

    public final long getLastSentWarning() {
        return this.lastSentAttackMessage;
    }

    public void sendAttackWarning() {
        if (System.currentTimeMillis() - this.lastSentAttackMessage > 180000L) {
            this.lastSentAttackMessage = System.currentTimeMillis();
            Creature c = King.getOfficial(this.getKingdom(), 1502);
            if (c != null) {
                StringBuilder buf = new StringBuilder();
                String name = this.getName();
                c.getCommunicator().sendAlertServerMessage("Guard tower of " + name + " is under attack!", (byte)4);
                int tilex = (int)this.tower.getPosX() >> 2;
                int tiley = (int)this.tower.getPosY() >> 2;
                VolaTile t = Zones.getTileOrNull(tilex, tiley, this.tower.isOnSurface());
                if (t != null) {
                    VolaTile ct;
                    if (t.getVillage() != null) {
                        buf.append("The ");
                        buf.append(this.tower.getName());
                        buf.append(" is in the settlement of ");
                        buf.append(t.getVillage().getName());
                        buf.append(". ");
                    }
                    if ((ct = c.getCurrentTile()) != null) {
                        int ctx = ct.tilex;
                        int cty = ct.tiley;
                        int mindist = Math.max(Math.abs(tilex - ctx), Math.abs(tiley - cty));
                        int dir = MethodsCreatures.getDir(c, tilex, tiley);
                        String direction = MethodsCreatures.getLocationStringFor(c.getStatus().getRotation(), dir, "you");
                        buf.append(EndGameItems.getDistanceString(mindist, this.tower.getName(), direction, true));
                    }
                    c.getCommunicator().sendAlertServerMessage(buf.toString());
                }
            }
        }
    }

    boolean isMyTower(Creature guard) {
        if (guard.getKingdomId() == this.tower.getAuxData()) {
            Iterator<Creature> it = this.guards.iterator();
            while (it.hasNext()) {
                if (it.next() != guard) continue;
                return true;
            }
            it = this.freeGuards.iterator();
            while (it.hasNext()) {
                if (it.next() != guard) continue;
                return true;
            }
        }
        return false;
    }

    void destroy() {
        this.destroyGuards();
        try {
            this.deleteTower();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        this.tower = null;
    }

    void destroyGuards() {
        this.guards.clear();
        for (Creature g : this.freeGuards) {
            try {
                this.destroyGuard(g);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "Problem destroying guard: " + g + " for tower: " + this + " due to " + e.getMessage(), e);
            }
        }
        this.freeGuards.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void destroyGuard(Creature guard) throws IOException {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Destroying guard " + guard);
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_CREATURE);
            ps.setLong(1, guard.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to delete tower creature " + guard.getWurmId(), ex);
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

    public final boolean hasLiveGuards() {
        return this.guards.size() > 0;
    }

    public void returnGuard(Creature guard) throws IOException {
        block5: {
            block6: {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("Returning guard " + guard);
                }
                this.guards.remove(guard);
                if (this.freeGuards.contains(guard)) break block5;
                if (this.guards.size() >= this.getMaxGuards()) break block6;
                this.freeGuards.add(guard);
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement(RETURN_GUARD);
                    ps.setBoolean(1, true);
                    ps.setLong(2, guard.getWurmId());
                    ps.executeUpdate();
                }
                catch (SQLException ex) {
                    try {
                        logger.log(Level.INFO, "Failed to return guard for " + this.tower.getWurmId());
                        throw new IOException("Failed to return guard for " + this.tower.getWurmId(), ex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block5;
            }
            guard.destroy();
            this.destroyGuard(guard);
        }
    }

    private void setGuardInWorld(Creature guard) {
        Fence[] fences;
        VolaTile t = Zones.getTileOrNull((int)this.tower.getPosX() >> 2, (int)this.tower.getPosY() >> 2, this.tower.isOnSurface());
        if (t != null && (fences = t.getFencesForLevel(0)) != null) {
            for (Fence f : fences) {
                f.destroy();
            }
        }
        if ((t = Zones.getTileOrNull(((int)this.tower.getPosX() >> 2) + 1, (int)this.tower.getPosY() >> 2, this.tower.isOnSurface())) != null) {
            for (Fence f : fences = t.getFencesForLevel(0)) {
                if (f.isHorizontal()) continue;
                f.destroy();
            }
        }
        if ((t = Zones.getTileOrNull((int)this.tower.getPosX() >> 2, ((int)this.tower.getPosY() >> 2) + 1, this.tower.isOnSurface())) != null) {
            for (Fence f : fences = t.getFencesForLevel(0)) {
                if (!f.isHorizontal()) continue;
                f.destroy();
            }
        }
        guard.setPositionX(this.tower.getPosX());
        guard.setPositionY(this.tower.getPosY());
        guard.setPositionZ(this.tower.getPosZ());
        guard.setRotation(1.0f + Server.rand.nextFloat() * 359.0f);
        guard.setLayer(this.tower.isOnSurface() ? 0 : -1, false);
        try {
            guard.respawn();
            Zone zone = Zones.getZone((int)this.tower.getPosX() >> 2, (int)this.tower.getPosY() >> 2, this.tower.isOnSurface());
            zone.addCreature(guard.getWurmId());
            guard.savePosition(zone.getId());
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + nsz.getMessage(), nsz);
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + nsc.getMessage(), nsc);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, "Guard: " + guard.getWurmId() + ": " + nsp.getMessage(), nsp);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to return village guard: " + ex.getMessage(), ex);
        }
    }

    private void activateGuard(Creature guard) throws IOException {
        this.freeGuards.remove(guard);
        if (!this.guards.contains(guard)) {
            this.guards.add(guard);
        }
        guard.setGuardTower(this);
        this.setGuardInWorld(guard);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(RETURN_GUARD);
            ps.setBoolean(1, false);
            ps.setLong(2, guard.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to activate guard for " + this.tower.getWurmId());
                throw new IOException("Failed to activate guard for " + this.tower.getWurmId(), ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public boolean alertGuards(Creature caller) {
        boolean helps = false;
        long target1 = -10L;
        long target2 = -10L;
        if ((caller.getReputation() >= 0 || caller.getKingdomTemplateId() == 3) && this.guards.size() > 0) {
            int tpx = this.getTower().getTileX();
            int tpy = this.getTower().getTileY();
            int posz = (int)this.getTower().getPosZ();
            Creature callerTarg = caller.getTarget();
            for (Creature g : this.guards) {
                boolean hasTarget = false;
                if (g.target != -10L) continue;
                if (callerTarg != null && callerTarg.isWithinTileDistanceTo(tpx, tpy, posz, 20) && target2 == -10L && callerTarg.getAttitude(g) == 2 && callerTarg.currentKingdom == this.getKingdom()) {
                    g.setTarget(callerTarg.getWurmId(), false);
                    if (g.target == callerTarg.getWurmId()) {
                        if (target1 == -10L) {
                            target1 = callerTarg.getWurmId();
                            GuardTower.yellHunt(g, callerTarg, false);
                        } else if (target2 == -10L) {
                            target2 = callerTarg.getWurmId();
                            GuardTower.yellHunt(g, callerTarg, true);
                        } else {
                            logger.log(Level.INFO, "This shouldn't happen? Three targets when yelling.");
                        }
                        GuardTower.yellHunt(g, callerTarg, true);
                        hasTarget = true;
                        helps = true;
                    }
                }
                if (hasTarget || caller.opponent == null || !caller.opponent.isWithinTileDistanceTo(tpx, tpy, posz, 20) || target2 != -10L || caller.opponent.getAttitude(g) != 2 || caller.opponent.currentKingdom != this.getKingdom()) continue;
                g.setTarget(caller.opponent.getWurmId(), false);
                if (g.target != caller.opponent.getWurmId()) continue;
                if (target1 == -10L) {
                    target1 = caller.opponent.getWurmId();
                    GuardTower.yellHunt(g, caller.opponent, false);
                } else if (target2 == -10L) {
                    target2 = caller.opponent.getWurmId();
                    GuardTower.yellHunt(g, caller.opponent, true);
                } else {
                    logger.log(Level.INFO, "This shouldn't happen? Three targets when yelling.");
                    GuardTower.yellHunt(g, caller.opponent, true);
                }
                hasTarget = true;
                helps = true;
            }
        }
        return helps;
    }

    public static void spawnSoldier(Item target, byte kingdom) {
        try {
            Creature c = Creature.doNew(7, target.getPosX() - 8.0f + Server.rand.nextFloat() * 16.0f, target.getPosY() - 8.0f + Server.rand.nextFloat() * 16.0f, Server.rand.nextFloat() * 360.0f, 0, LoginHandler.raiseFirstLetter(target.getName() + " guard"), Server.rand.nextBoolean() ? (byte)1 : 0, kingdom);
            c.checkForEnemies(true);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public static void spawnCommander(Item target, byte kingdom) {
        try {
            String name = target.getName() + " lieutenant";
            boolean captain = Server.rand.nextBoolean();
            if (captain) {
                name = target.getName() + " captain";
            }
            Creature c = Creature.doNew(8, target.getPosX() - 8.0f + Server.rand.nextFloat() * 16.0f, target.getPosY() - 8.0f + Server.rand.nextFloat() * 16.0f, Server.rand.nextFloat() * 360.0f, 0, LoginHandler.raiseFirstLetter(name), Server.rand.nextBoolean() ? (byte)1 : 0, kingdom);
            if (captain) {
                Skills s = c.getSkills();
                try {
                    Skill bc = s.getSkill(104);
                    bc.setKnowledge(bc.getKnowledge() + 10.0, false);
                    Skill bs = s.getSkill(103);
                    bs.setKnowledge(bs.getKnowledge() + 10.0, false);
                    Skill bst = s.getSkill(102);
                    bst.setKnowledge(bst.getKnowledge() + 10.0, false);
                    Skill mst = s.getSkill(101);
                    mst.setKnowledge(mst.getKnowledge() + 10.0, false);
                }
                catch (NoSuchSkillException nss) {
                    logger.log(Level.WARNING, c.getWurmId() + ": " + nss.getMessage());
                }
            }
            c.checkForEnemies(true);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public static final void yellHunt(Creature guard, Creature target, boolean aiding) {
        String toYell = "";
        if (!aiding) {
            switch (guard.getKingdomId()) {
                case 3: {
                    toYell = GuardTower.yellHotsHunter(guard, target);
                    break;
                }
                case 1: {
                    toYell = GuardTower.yellJennHunter(guard, target);
                    break;
                }
                case 2: {
                    toYell = GuardTower.yellMolrHunter(guard, target);
                    break;
                }
                case 4: {
                    toYell = GuardTower.yellFreedomHunter(guard, target);
                    break;
                }
                default: {
                    toYell = GuardTower.yellGenericHunter(guard, target);
                    break;
                }
            }
        } else {
            toYell = GuardTower.yellAidHunter(guard, target);
        }
        guard.say(toYell);
    }

    public static final String yellAidHunter(Creature guard, Creature target) {
        StringBuilder sb = new StringBuilder();
        int random = rand.nextInt(10);
        if (random < 1) {
            sb.append("Coming for ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 2) {
            sb.append("I'll help with ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 3) {
            sb.append("Joining in on ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 4) {
            sb.append("Beware of me as well, ");
        } else if (random < 5) {
            sb.append("Whoa! Here I come, ");
        } else if (random < 6) {
            sb.append("Now we are two, ");
        } else if (random < 7) {
            sb.append("You better believe it, ");
        } else if (random < 8) {
            sb.append("I come as well, ");
        } else if (random < 9) {
            sb.append("I also found ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 10) {
            sb.append("I see ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        }
        if (target.isPlayer()) {
            sb.append(target.getName());
        } else {
            sb.append(target.getName().toLowerCase());
        }
        sb.append(".");
        return sb.toString();
    }

    public static final String yellHotsHunter(Creature guard, Creature target) {
        StringBuilder sb = new StringBuilder();
        int random = rand.nextInt(10);
        if (random < 1) {
            sb.append("You're one ugly bastard, ");
        } else if (random < 2) {
            sb.append("This will be fun, ");
        } else if (random < 3) {
            sb.append("I will enjoy killing you, ");
        } else if (random < 4) {
            sb.append("Eat my wrath, ");
        } else if (random < 5) {
            sb.append("I will shred you, ");
        } else if (random < 6) {
            sb.append("You will look bad torn to pieces, ");
        } else if (random < 7) {
            sb.append("Your corpse will rot away in silence, ");
        } else if (random < 8) {
            sb.append("I will drink your blood, ");
        } else if (random < 9) {
            sb.append("Die, die and die again, ");
        } else if (random < 10) {
            sb.append("Prepare to be exterminated, ");
        }
        if (target.isPlayer()) {
            sb.append(target.getName());
        } else {
            sb.append(target.getName().toLowerCase());
        }
        sb.append("!");
        return sb.toString();
    }

    public static final String yellJennHunter(Creature guard, Creature target) {
        StringBuilder sb = new StringBuilder();
        int random = rand.nextInt(10);
        if (random < 1) {
            sb.append("I'll hunt down ");
            if (!target.isPlayer()) {
                sb.append("this ");
            }
        } else if (random < 2) {
            sb.append("I'll take care of ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 3) {
            sb.append("You will soon be history, ");
        } else if (random < 4) {
            sb.append("Goodbye, ");
        } else if (random < 5) {
            sb.append("Quick, help me dispatch ");
            if (!target.isPlayer()) {
                sb.append("this ");
            }
        } else if (random < 6) {
            sb.append("I found ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 7) {
            sb.append("No soup for you, ");
        } else if (random < 8) {
            sb.append("Let me handle ");
            if (!target.isPlayer()) {
                sb.append("this ");
            }
        } else if (random < 9) {
            sb.append("Here is ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 10) {
            sb.append("This will hurt some, ");
        }
        if (target.isPlayer()) {
            sb.append(target.getName());
        } else {
            sb.append(target.getName().toLowerCase());
        }
        sb.append("!");
        return sb.toString();
    }

    public static final String yellMolrHunter(Creature guard, Creature target) {
        StringBuilder sb = new StringBuilder();
        int random = rand.nextInt(10);
        if (random < 1) {
            sb.append("By the fires of Magranon! Die, ");
        } else if (random < 2) {
            sb.append("I will rip you apart, ");
        } else if (random < 3) {
            sb.append("I will crush you, ");
        } else if (random < 4) {
            sb.append("Prepare to die, ");
        } else if (random < 5) {
            sb.append("This will hurt badly, ");
        } else if (random < 6) {
            sb.append("You receive no mercy, ");
        } else if (random < 7) {
            sb.append("Bleed, ");
        } else if (random < 8) {
            sb.append("This will be your last breath, ");
        } else if (random < 9) {
            sb.append("Pain will be served, ");
        } else if (random < 10) {
            sb.append("This is the end, ");
        }
        if (target.isPlayer()) {
            sb.append(target.getName());
        } else {
            sb.append(target.getName().toLowerCase());
        }
        sb.append("!");
        return sb.toString();
    }

    public static final String yellFreedomHunter(Creature guard, Creature target) {
        StringBuilder sb = new StringBuilder();
        int random = rand.nextInt(10);
        if (random < 1) {
            sb.append("Goodbye, ");
        } else if (random < 2) {
            sb.append("I have to slay you now, ");
        } else if (random < 3) {
            sb.append("Forgive me, ");
        } else if (random < 4) {
            sb.append("Stop that immediately, ");
        } else if (random < 5) {
            sb.append("I have to hurt you now, ");
        } else if (random < 6) {
            sb.append("Flee, ");
        } else if (random < 7) {
            sb.append("Run quickly now, ");
        } else if (random < 8) {
            sb.append("It is my duty to inform you that this is the end, ");
        } else if (random < 9) {
            sb.append("I will terminate ");
        } else if (random < 10) {
            sb.append("My pleasure, ");
        }
        if (target.isPlayer()) {
            sb.append(target.getName());
        } else {
            sb.append(target.getName().toLowerCase());
        }
        sb.append("!");
        return sb.toString();
    }

    public static final String yellGenericHunter(Creature guard, Creature target) {
        StringBuilder sb = new StringBuilder();
        int random = rand.nextInt(10);
        if (random < 1) {
            sb.append("I'll hunt down ");
            if (!target.isPlayer()) {
                sb.append("this ");
            }
        } else if (random < 2) {
            sb.append("I'll take care of ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 3) {
            sb.append("Prepare to meet your maker, ");
        } else if (random < 4) {
            sb.append("Goodbye, ");
        } else if (random < 5) {
            sb.append("This is the end, ");
        } else if (random < 6) {
            sb.append("I found ");
            if (!target.isPlayer()) {
                sb.append("this ");
            }
        } else if (random < 7) {
            sb.append("I will terminate you, ");
        } else if (random < 8) {
            sb.append("I attack ");
            if (!target.isPlayer()) {
                sb.append("this ");
            }
        } else if (random < 9) {
            sb.append("I will hunt ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        } else if (random < 10) {
            sb.append("I get ");
            if (!target.isPlayer()) {
                sb.append("the ");
            }
        }
        if (target.isPlayer()) {
            sb.append(target.getName());
        } else {
            sb.append(target.getName().toLowerCase());
        }
        sb.append("!");
        return sb.toString();
    }

    private void newGuard(Creature guard) throws IOException {
        guard.setGuardTower(this);
        this.guards.add(guard);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(ADD_GUARD);
            ps.setLong(1, this.tower.getWurmId());
            ps.setLong(2, guard.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to insert guard for " + this.tower.getWurmId());
                throw new IOException("Failed to insert guard for " + this.tower.getWurmId(), ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    private void deleteTower() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_TOWER);
            ps.setLong(1, this.tower.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to delete tower " + this.tower.getWurmId());
                throw new IOException("Failed to delete tower " + this.tower.getWurmId(), ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    void poll() {
        this.pollGuards();
        this.tower.attackEnemies(true);
    }

    public final int getGuardCount() {
        return this.guards.size();
    }

    public final int getMaxGuards() {
        if (Features.Feature.TOWER_CHAINING.isEnabled() && !this.tower.isChained()) {
            return 0;
        }
        return Math.min(5 + this.tower.getRarity(), (int)this.tower.getQualityLevel() / 10);
    }

    public final int getMaxPossibleGuards() {
        return Math.min(5 + this.tower.getRarity(), (int)this.tower.getQualityLevel() / 10);
    }

    private void pollGuards() {
        if (this.guards.size() < this.getMaxGuards()) {
            Village v = Villages.getVillageWithPerimeterAt(this.getTower().getTileX(), this.getTower().getTileY(), true);
            if (v != null && this.getKingdom() != v.kingdom) {
                return;
            }
            boolean sex = false;
            if (Server.rand.nextInt(2) == 0) {
                sex = true;
            }
            int templateId = 34;
            byte templateKingdom = this.tower.getAuxData();
            Kingdom kingdom = Kingdoms.getKingdom(this.tower.getAuxData());
            if (kingdom != null) {
                templateKingdom = kingdom.getTemplate();
            }
            if (templateKingdom == 3) {
                templateId = 35;
            } else if (templateKingdom == 2) {
                templateId = 36;
            } else if (templateKingdom == 4) {
                templateId = 67;
            }
            try {
                if (this.freeGuards.isEmpty()) {
                    Kingdom k = Kingdoms.getKingdom(this.tower.getAuxData());
                    if (k.getId() != 0) {
                        this.spawnGuard(templateId, "tower guard", false);
                    } else if (this.tower.getTemplateId() == 996) {
                        this.spawnGuard(67, "Peacekeeper", false);
                    }
                } else {
                    Creature toReturn = this.freeGuards.removeFirst();
                    this.activateGuard(toReturn);
                    if (logger.isLoggable(Level.FINER)) {
                        logger.finer("Activating " + toReturn.getWurmId());
                    }
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Problem while polling guards for tower: " + this.tower + ", " + ex.getMessage(), ex);
            }
        }
    }

    public int getBashAlertDamage() {
        return 90;
    }

    public void checkBashDamage(float oldDam, float newDam) {
        for (byte i = 1; i <= 3; i = (byte)((byte)(i + 1))) {
            if (!(oldDam < (float)(30 * i)) || !(newDam >= (float)(30 * i))) continue;
            this.spawnBashWave(i);
            break;
        }
        if (oldDam < (float)this.getBashAlertDamage() && newDam >= (float)this.getBashAlertDamage()) {
            Players.getInstance().broadCastBashInfo(this.getTower(), this.getName() + " is under attack.");
        }
    }

    public int getTowerSpawnMod() {
        return Math.round(this.getTower().getQualityLevel() / 30.0f);
    }

    public int getTowerBashSpawnCount(int spawnMod, byte waveStrength) {
        return Math.max(1, spawnMod) * waveStrength;
    }

    public void spawnGuard(int templateId, String name, boolean offSetSpawn) {
        try {
            byte sex = Server.rand.nextInt(2) == 0 ? (byte)1 : 0;
            Kingdom k = Kingdoms.getKingdom(this.tower.getAuxData());
            if (k.getId() != 0) {
                int xOffSet = 0;
                int yOffSet = 0;
                if (offSetSpawn) {
                    if (Server.rand.nextBoolean()) {
                        int n = xOffSet = Server.rand.nextBoolean() ? 1 : -1;
                    }
                    if (Server.rand.nextBoolean()) {
                        yOffSet = Server.rand.nextBoolean() ? 1 : -1;
                    }
                }
                String creatureName = k.getName() + " " + name;
                Creature newc = Creature.doNew(templateId, this.tower.getPosX() + (float)xOffSet, this.tower.getPosY() + (float)yOffSet, (float)Server.rand.nextInt(360), this.tower.isOnSurface() ? 0 : -1, creatureName, sex, this.tower.getAuxData());
                this.newGuard(newc);
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("WT Created guard " + newc.getName() + " now=" + this.guards.size() + " max=" + this.getMaxGuards());
                }
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problem while spawning guard for tower: " + this.tower + ", " + ex.getMessage(), ex);
        }
    }

    public void spawnBashWave(byte waveStrength) {
        boolean sex = false;
        int templateId = 34;
        byte templateKingdom = this.tower.getAuxData();
        Kingdom kingdom = Kingdoms.getKingdom(this.tower.getAuxData());
        if (kingdom != null) {
            templateKingdom = kingdom.getTemplate();
        }
        if (templateKingdom == 3) {
            templateId = 35;
        } else if (templateKingdom == 2) {
            templateId = 36;
        } else if (templateKingdom == 4) {
            templateId = 67;
        }
        if (this.guards.size() < this.getMaxPossibleGuards()) {
            for (int i = this.guards.size(); i < this.getMaxPossibleGuards(); ++i) {
                this.spawnGuard(templateId, "tower guard", true);
            }
        }
        int spawnCount = this.getTowerBashSpawnCount(this.getTowerSpawnMod(), waveStrength);
        for (int i = 0; i < spawnCount; ++i) {
            this.spawnGuard(8, "Captain", true);
        }
    }

    public static boolean hasNearbyAlliedTower(int tilex, int tiley, byte founderKingdom) {
        GuardTower closest = null;
        int minDist = Integer.MAX_VALUE;
        for (GuardTower tower : Kingdoms.getTowers().values()) {
            int disty;
            int distx = Math.abs(tower.getTower().getTileX() - tilex);
            int tileDistance = Math.max(distx, disty = Math.abs(tower.getTower().getTileY() - tiley));
            if (tileDistance > minDist) continue;
            minDist = tileDistance;
            closest = tower;
        }
        if (closest != null) {
            return minDist <= 100 && closest.getTower().getKingdom() == founderKingdom;
        }
        return false;
    }

    public static void canConstructTower(Creature performer, Item realTarget) throws NoSuchItemException {
        boolean onSurface;
        int tiley;
        if (!performer.isOnSurface() || !realTarget.isOnSurface()) {
            performer.getCommunicator().sendAlertServerMessage("You can't construct the tower now; you can't build this below surface.");
            throw new NoSuchItemException("Below surface.");
        }
        VolaTile targTile = Zones.getTileOrNull(realTarget.getTileX(), realTarget.getTileY(), true);
        if (targTile != null && targTile.isTransition()) {
            performer.getCommunicator().sendAlertServerMessage("You can't construct the tower here - the foundation is not stable enough.");
            throw new NoSuchItemException("On cave opening.");
        }
        int tilex = realTarget.getTileX();
        if (Terraforming.isTileUnderWater(1, tilex, tiley = realTarget.getTileY(), onSurface = realTarget.isOnSurface())) {
            performer.getCommunicator().sendAlertServerMessage("You can't construct the tower now; the ground is not solid here.");
            throw new NoSuchItemException("Too wet.");
        }
        int mindist = Kingdoms.minKingdomDist;
        if (Kingdoms.isTowerTooNear(tilex, tiley, onSurface, false)) {
            performer.getCommunicator().sendAlertServerMessage("You can't construct the tower now; another tower is too near.");
            throw new NoSuchItemException("Too close to another tower.");
        }
        if (Features.Feature.TOWER_CHAINING.isEnabled() && !GuardTower.hasNearbyAlliedTower(tilex, tiley, performer.getKingdomId()) && performer.getKingdomId() != 4) {
            performer.getCommunicator().sendAlertServerMessage("You can't construct the tower now; it must be within range of another allied tower.");
            throw new NoSuchItemException("Not within range of an allied tower.");
        }
        if (!Zones.isKingdomBlocking(tilex - mindist, tiley - mindist, tilex + mindist, tiley + mindist, performer.getKingdomId())) {
            performer.getCommunicator().sendAlertServerMessage("You can't construct the tower now; another kingdom is too near.");
            throw new NoSuchItemException("Too close to another kingdom.");
        }
        if (!Servers.isThisAHomeServer()) {
            int maxwest;
            int maxsouth;
            int maxnorth;
            if (Terraforming.isTileModBlocked(performer, tilex, tiley, performer.isOnSurface())) {
                throw new NoSuchItemException("Tile protected by the deities in this area.");
            }
            for (Item targ : Items.getWarTargets()) {
                int maxnorth2 = Math.max(0, tiley - 60);
                int maxsouth2 = Math.min(Zones.worldTileSizeY, tiley + 60);
                int maxwest2 = Math.max(0, tilex - 60);
                int maxeast = Math.min(Zones.worldTileSizeX, tilex + 60);
                if ((int)targ.getPosX() >> 2 <= maxwest2 || (int)targ.getPosX() >> 2 >= maxeast || (int)targ.getPosY() >> 2 >= maxsouth2 || (int)targ.getPosY() >> 2 <= maxnorth2) continue;
                performer.getCommunicator().sendSafeServerMessage("You cannot construct the tower here, since this is an active battle ground.");
                throw new NoSuchItemException("Too close to a war target.");
            }
            EndGameItem alt = EndGameItems.getEvilAltar();
            if (alt != null) {
                maxnorth = Math.max(0, tiley - 100);
                maxsouth = Math.min(Zones.worldTileSizeY, tiley + 100);
                int maxeast = Math.max(0, tilex - 100);
                maxwest = Math.min(Zones.worldTileSizeX, tilex + 100);
                if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                    throw new NoSuchItemException("You cannot construct a tower here, since this is unholy ground.");
                }
            }
            if ((alt = EndGameItems.getGoodAltar()) != null) {
                maxnorth = Math.max(0, tiley - 100);
                maxsouth = Math.min(Zones.worldTileSizeY, tiley + 100);
                int maxeast = Math.max(0, tilex - 100);
                maxwest = Math.min(Zones.worldTileSizeX, tilex + 100);
                if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                    throw new NoSuchItemException("You cannot construct a tower here, since this is holy ground.");
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void load() {
        dbcon = null;
        ps = null;
        rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("SELECT CREATUREID, RETURNED FROM TOWERGUARDS WHERE TOWERID=?");
            ps.setLong(1, this.tower.getWurmId());
            rs = ps.executeQuery();
            while (rs.next()) {
                creatureid = rs.getLong("CREATUREID");
                returned = rs.getBoolean("RETURNED");
                try {
                    guard = Creatures.getInstance().getCreature(creatureid);
                    if (GuardTower.logger.isLoggable(Level.FINER)) {
                        GuardTower.logger.finer("GT Loaded " + guard.getName());
                    }
                    if (!returned) {
                        if (!this.guards.contains(guard)) {
                            this.guards.add(guard);
                        }
                        guard.setGuardTower(this);
                        continue;
                    }
                    if (this.freeGuards.contains(guard)) continue;
                    this.freeGuards.add(guard);
                }
                catch (NoSuchCreatureException nsc) {
                    dbcon2 = null;
                    ps2 = null;
                    try {
                        GuardTower.logger.log(Level.WARNING, "Deleting from towerguards where creatureid=" + creatureid);
                        dbcon2 = DbConnector.getZonesDbCon();
                        ps2 = dbcon2.prepareStatement("DELETE FROM TOWERGUARDS WHERE CREATUREID=?");
                        ps2.setLong(1, creatureid);
                        ps2.executeUpdate();
                        ** GOTO lbl45
                    }
                    catch (SQLException ex) {
                        try {
                            GuardTower.logger.log(Level.INFO, "Failed to delete tower creature " + creatureid, ex);
                        }
                        catch (Throwable var11_12) {
                            DbUtilities.closeDatabaseObjects(ps2, null);
                            DbConnector.returnConnection(dbcon2);
                            throw var11_12;
                        }
lbl45:
                        // 1 sources

                        DbUtilities.closeDatabaseObjects(ps2, null);
                        DbConnector.returnConnection(dbcon2);
                        continue;
                        DbUtilities.closeDatabaseObjects(ps2, null);
                        DbConnector.returnConnection(dbcon2);
                    }
                }
            }
            ** GOTO lbl60
        }
        catch (SQLException sqx) {
            try {
                GuardTower.logger.log(Level.WARNING, "Failed to load guards for tower with id " + this.tower.getWurmId(), sqx);
            }
            catch (Throwable var12_13) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw var12_13;
            }
lbl60:
            // 1 sources

            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return;
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return;
        }
    }

    public String toString() {
        long lWurmID = this.tower != null ? this.tower.getWurmId() : -1L;
        return "GuardTower [WurmID: " + lWurmID + ", Kingdom: " + Kingdoms.getNameFor(this.getKingdom()) + ", #guards: " + this.guards.size() + ", #freeGuards: " + this.freeGuards.size() + ", Item: " + this.tower + ']';
    }
}


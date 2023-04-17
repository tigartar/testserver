/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SpellEffect
implements MiscConstants {
    private static final String DELETE_EFFECT = "DELETE FROM SPELLEFFECTS WHERE WURMID=?";
    private static final String DELETE_EFFECTS_FOR_PLAYER = "DELETE FROM SPELLEFFECTS WHERE OWNER=?";
    private static final String DELETE_EFFECTS_FOR_ITEM = "DELETE FROM SPELLEFFECTS WHERE ITEMID=?";
    private static final String UPDATE_POWER = "UPDATE SPELLEFFECTS SET POWER=? WHERE WURMID=?";
    private static final String UPDATE_TIMELEFT = "UPDATE SPELLEFFECTS SET TIMELEFT=? WHERE WURMID=?";
    private static final String GET_EFFECTS_FOR_PLAYER = "SELECT * FROM SPELLEFFECTS WHERE OWNER=?";
    private static final String CREATE_EFFECT = "INSERT INTO SPELLEFFECTS (WURMID, OWNER,TYPE,POWER,TIMELEFT,EFFTYPE,INFLUENCE) VALUES(?,?,?,?,?,?,?)";
    private static final String CREATE_ITEM_EFFECT = "INSERT INTO SPELLEFFECTS (WURMID, ITEMID,TYPE,POWER,TIMELEFT) VALUES(?,?,?,?,?)";
    private static final Logger logger = Logger.getLogger(SpellEffect.class.getName());
    public final long id;
    public float power = 0.0f;
    public int timeleft = 0;
    public final long owner;
    public final byte type;
    private final boolean isplayer;
    private final boolean isItem;
    private final byte effectType;
    private final byte influence;
    private boolean persistant = true;

    public SpellEffect(long aOwner, byte aType, float aPower, int aTimeleft) {
        this(aOwner, aType, aPower, aTimeleft, 9, 0, true);
    }

    public SpellEffect(long aOwner, byte aType, float aPower, int aTimeleft, byte effType, byte influenceType, boolean persist) {
        this.owner = aOwner;
        this.type = aType;
        this.power = aPower;
        this.timeleft = aTimeleft;
        this.effectType = effType;
        this.influence = influenceType;
        this.persistant = persist;
        this.id = WurmId.getNextSpellId();
        if (WurmId.getType(aOwner) == 0) {
            this.isplayer = true;
            this.isItem = false;
        } else if (WurmId.getType(aOwner) == 2 || WurmId.getType(aOwner) == 19 || WurmId.getType(aOwner) == 20) {
            this.isplayer = false;
            this.isItem = true;
        } else {
            this.isplayer = false;
            this.isItem = false;
        }
        if ((this.isplayer || this.isItem) && this.persistant) {
            this.save();
        }
    }

    public SpellEffect(long aId, long aOwner, byte aType, float aPower, int aTimeleft, byte efftype, byte influ) {
        this.id = aId;
        this.owner = aOwner;
        this.type = aType;
        this.power = aPower;
        this.timeleft = aTimeleft;
        this.effectType = efftype;
        this.influence = influ;
        this.persistant = true;
        if (WurmId.getType(aOwner) == 0) {
            this.isplayer = true;
            this.isItem = false;
        } else if (WurmId.getType(aOwner) == 2) {
            this.isplayer = false;
            this.isItem = true;
        } else {
            this.isplayer = false;
            this.isItem = false;
        }
    }

    public byte getSpellEffectType() {
        return this.effectType;
    }

    public byte getSpellInfluenceType() {
        return this.influence;
    }

    public final boolean isSmeared() {
        return this.type >= 77 && this.type <= 92;
    }

    public String getName() {
        if (this.type == 22 && this.getPower() > 70.0f) {
            return "Thornshell";
        }
        if (this.type == 73) {
            return "Newbie agg range buff";
        }
        if (this.type == 74) {
            return "Newbie food and drink buff";
        }
        if (this.type == 75) {
            return "Newbie healing buff";
        }
        if (this.type == 64) {
            return "Hunted";
        }
        if (this.type == 72) {
            return "Illusion";
        }
        if (this.type == 78) {
            return "potion of the ropemaker";
        }
        if (this.type == 79) {
            return "potion of mining";
        }
        if (this.type == 77) {
            return "oil of the weapon smith";
        }
        if (this.type == 80) {
            return "ointment of tailoring";
        }
        if (this.type == 81) {
            return "oil of the armour smith";
        }
        if (this.type == 82) {
            return "fletching potion";
        }
        if (this.type == 83) {
            return "oil of the blacksmith";
        }
        if (this.type == 84) {
            return "potion of leatherworking";
        }
        if (this.type == 85) {
            return "potion of shipbuilding";
        }
        if (this.type == 86) {
            return "ointment of stonecutting";
        }
        if (this.type == 87) {
            return "ointment of masonry";
        }
        if (this.type == 88) {
            return "potion of woodcutting";
        }
        if (this.type == 89) {
            return "potion of carpentry";
        }
        if (this.type == 99) {
            return "potion of butchery";
        }
        if (this.type == 94) {
            return "Incineration";
        }
        if (this.type == 98) {
            return "Shatter Protection";
        }
        if ((long)this.type < -10L) {
            return RuneUtilities.getRuneName(this.type);
        }
        return Spells.getEnchantment((byte)this.type).name;
    }

    public String getLongDesc() {
        if (this.type == 78) {
            return "improves rope making max ql";
        }
        if (this.type == 79) {
            return "improves mining max ql";
        }
        if (this.type == 77) {
            return "improves weapon smithing max ql";
        }
        if (this.type == 80) {
            return "improves tailoring max ql";
        }
        if (this.type == 81) {
            return "improves armour smithing max ql";
        }
        if (this.type == 82) {
            return "improves fletching max ql";
        }
        if (this.type == 83) {
            return "improves blacksmithing max ql";
        }
        if (this.type == 84) {
            return "improves leather working max ql";
        }
        if (this.type == 85) {
            return "improves ship building max ql";
        }
        if (this.type == 86) {
            return "improves stone cutting max ql";
        }
        if (this.type == 87) {
            return "improves masonry max ql";
        }
        if (this.type == 88) {
            return "improves wood cutting max ql";
        }
        if (this.type == 89) {
            return "improves carpentry max ql";
        }
        if (this.type == 99) {
            return "improves butchery product max ql";
        }
        if (this.type == 98) {
            return "protects against damage when spells are cast upon it";
        }
        if ((long)this.type < -10L) {
            return "will " + RuneUtilities.getRuneLongDesc(this.type);
        }
        return Spells.getEnchantment((byte)this.type).effectdesc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void save() {
        block9: {
            block10: {
                if (!this.isplayer || !this.persistant) break block10;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(CREATE_EFFECT);
                    ps.setLong(1, this.id);
                    ps.setLong(2, this.owner);
                    ps.setByte(3, this.type);
                    ps.setFloat(4, this.power);
                    ps.setInt(5, this.timeleft);
                    ps.setByte(6, this.effectType);
                    ps.setByte(7, this.influence);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block9;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block9;
            }
            if (this.isItem && this.persistant) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(CREATE_ITEM_EFFECT);
                    ps.setLong(1, this.id);
                    ps.setLong(2, this.owner);
                    ps.setByte(3, this.type);
                    ps.setFloat(4, this.power);
                    ps.setInt(5, this.timeleft);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
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
        }
    }

    public float getPower() {
        return this.power;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPower(float newpower) {
        block9: {
            block10: {
                if (this.power == newpower) break block9;
                this.power = newpower;
                if (!this.persistant) break block9;
                if (!this.isplayer) break block10;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(UPDATE_POWER);
                    ps.setFloat(1, this.power);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block9;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block9;
            }
            if (this.isItem) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(UPDATE_POWER);
                    ps.setFloat(1, this.power);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
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
        }
    }

    public void improvePower(Creature performer, float newpower) {
        float mod = 5.0f * (1.0f - this.power / (performer.hasFlag(82) ? 105.0f : 100.0f));
        this.setPower(mod + newpower);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setTimeleft(int newTimeleft) {
        if (this.timeleft != newTimeleft) {
            this.timeleft = newTimeleft;
            if (this.isplayer && this.persistant) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(UPDATE_TIMELEFT);
                    ps.setInt(1, this.timeleft);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
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
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void saveTimeleft() {
        if (this.isplayer && this.persistant) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(UPDATE_TIMELEFT);
                ps.setInt(1, this.timeleft);
                ps.setLong(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, sqex.getMessage(), sqex);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void delete() {
        block9: {
            block10: {
                if (!this.persistant) break block9;
                if (!this.isplayer) break block10;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(DELETE_EFFECT);
                    ps.setLong(1, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block9;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block9;
            }
            if (this.isItem) {
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getItemDbCon();
                    ps = dbcon.prepareStatement(DELETE_EFFECT);
                    ps.setLong(1, this.id);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, sqex.getMessage(), sqex);
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
        }
    }

    public boolean poll(SpellEffects effects) {
        --this.timeleft;
        if (this.timeleft <= 0) {
            effects.removeSpellEffect(this);
            return true;
        }
        if (this.timeleft % 60 == 0) {
            this.saveTimeleft();
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final SpellEffect[] loadEffectsForPlayer(long wurmid) {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        SpellEffect[] spells;
        block5: {
            spells = new SpellEffect[]{};
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(GET_EFFECTS_FOR_PLAYER);
                ps.setLong(1, wurmid);
                rs = ps.executeQuery();
                HashSet<SpellEffect> spset = new HashSet<SpellEffect>();
                while (rs.next()) {
                    SpellEffect sp = new SpellEffect(rs.getLong("WURMID"), rs.getLong("OWNER"), rs.getByte("TYPE"), rs.getFloat("POWER"), rs.getInt("TIMELEFT"), rs.getByte("EFFTYPE"), rs.getByte("INFLUENCE"));
                    spset.add(sp);
                }
                if (spset.size() <= 0) break block5;
                spells = spset.toArray(new SpellEffect[spset.size()]);
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, wurmid + ": " + sqx.getMessage(), sqx);
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
        return spells;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void deleteEffectsForPlayer(long playerid) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Deleting Effects for Player ID: " + playerid);
            }
            ps = dbcon.prepareStatement(DELETE_EFFECTS_FOR_PLAYER);
            ps.setLong(1, playerid);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Problem deleting effects for playerid: " + playerid + " due to " + sqex.getMessage(), sqex);
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
    public static final void deleteEffectsForItem(long itemid) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(DELETE_EFFECTS_FOR_ITEM);
            ps.setLong(1, itemid);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Problem deleting effects for itemid: " + itemid + " due to " + sqex.getMessage(), sqex);
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
}


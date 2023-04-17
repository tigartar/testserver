/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SpellEffectMetaData
implements MiscConstants,
CounterTypes {
    private static final String CREATE_EFFECT = "INSERT INTO SPELLEFFECTS (WURMID, OWNER,TYPE,POWER,TIMELEFT) VALUES(?,?,?,?,?)";
    private static final String CREATE_ITEM_EFFECT = "INSERT INTO SPELLEFFECTS (WURMID, ITEMID,TYPE,POWER,TIMELEFT) VALUES(?,?,?,?,?)";
    private static final Logger logger = Logger.getLogger(SpellEffectMetaData.class.getName());
    private final long id;
    private final float power;
    private final int timeleft;
    private final long owner;
    private final byte type;

    public SpellEffectMetaData(long aWurmid, long aOwner, byte aType, float aPower, int aTimeleft, boolean aAddToTables) {
        this.owner = aOwner;
        this.type = aType;
        this.power = aPower;
        this.timeleft = aTimeleft;
        this.id = aWurmid;
        if (aAddToTables && (WurmId.getType(aOwner) == 2 || WurmId.getType(aOwner) == 19 || WurmId.getType(aOwner) == 20)) {
            SpellEffect sp = new SpellEffect(aWurmid, aOwner, aType, aPower, aTimeleft, 9, 0);
            ItemSpellEffects eff = ItemSpellEffects.getSpellEffects(sp.owner);
            if (eff == null) {
                eff = new ItemSpellEffects(sp.owner);
            }
            eff.addSpellEffect(sp);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() {
        block9: {
            block10: {
                if (WurmId.getType(this.owner) != 0) break block10;
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
            if (WurmId.getType(this.owner) == 2) {
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
}


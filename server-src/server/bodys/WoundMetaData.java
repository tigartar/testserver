/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.bodys;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class WoundMetaData
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WoundMetaData.class.getName());
    private final byte location;
    private final long id;
    private final byte type;
    private final float severity;
    private final long owner;
    private final float poisonSeverity;
    private final float infectionSeverity;
    private final boolean isBandaged;
    private final long lastPolled;
    private final byte healEff;
    private static final String CREATE_WOUND = "INSERT INTO WOUNDS( ID, OWNER,TYPE,LOCATION,SEVERITY, POISONSEVERITY,INFECTIONSEVERITY,BANDAGED,LASTPOLLED, HEALEFF) VALUES(?,?,?,?,?,?,?,?,?,?)";

    public WoundMetaData(long aId, byte aType, byte aLocation, float aSeverity, long aOwner, float aPoisonSeverity, float aInfectionSeverity, boolean aBandaged, long aLastPolled, byte aHealEff) {
        this.id = aId;
        this.type = aType;
        this.location = aLocation;
        this.severity = aSeverity;
        this.owner = aOwner;
        this.poisonSeverity = aPoisonSeverity;
        this.infectionSeverity = aInfectionSeverity;
        this.lastPolled = aLastPolled;
        this.healEff = aHealEff;
        this.isBandaged = aBandaged;
    }

    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(CREATE_WOUND);
            ps.setLong(1, this.id);
            ps.setLong(2, this.owner);
            ps.setByte(3, this.type);
            ps.setByte(4, this.location);
            ps.setFloat(5, this.severity);
            ps.setFloat(6, this.poisonSeverity);
            ps.setFloat(7, this.infectionSeverity);
            ps.setBoolean(8, this.isBandaged);
            ps.setLong(9, this.lastPolled);
            ps.setByte(10, this.healEff);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                throw new IOException(this.id + " " + sqex.getMessage(), sqex);
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
}


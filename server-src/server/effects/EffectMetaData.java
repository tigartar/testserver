/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.effects;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class EffectMetaData {
    private final long owner;
    private final short type;
    private final float posX;
    private final float posY;
    private final float posZ;
    private final long startTime;
    private static final String CREATE_EFFECT = "INSERT INTO EFFECTS(  OWNER,TYPE,POSX,POSY,POSZ,STARTTIME) VALUES(?,?,?,?,?,?)";

    public EffectMetaData(long aOwner, short aType, float aPosx, float aPosy, float aPosz, long aStartTime) {
        this.owner = aOwner;
        this.type = aType;
        this.posX = aPosx;
        this.posY = aPosy;
        this.posZ = aPosz;
        this.startTime = aStartTime;
    }

    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(CREATE_EFFECT);
            ps.setLong(1, this.owner);
            ps.setShort(2, this.type);
            ps.setFloat(3, this.posX);
            ps.setFloat(4, this.posY);
            ps.setFloat(5, this.posZ);
            ps.setLong(6, this.startTime);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                throw new IOException(this.owner + " " + sqex.getMessage(), sqex);
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


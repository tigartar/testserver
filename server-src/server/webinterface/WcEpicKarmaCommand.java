/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcEpicKarmaCommand
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcEpicKarmaCommand.class.getName());
    private long[] pids;
    private int[] karmas;
    private int deity;
    private static final String CLEAR_KARMA = "DELETE FROM HELPERS";

    public WcEpicKarmaCommand(long _id, long[] playerids, int[] karmaValues, int _deity) {
        super(_id, (short)16);
        this.pids = playerids;
        this.karmas = karmaValues;
        this.deity = _deity;
    }

    public WcEpicKarmaCommand(long _id, byte[] _data) {
        super(_id, (short)16, _data);
    }

    @Override
    public boolean autoForward() {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    byte[] encode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        byte[] barr = null;
        try {
            dos = new DataOutputStream(bos);
            dos.writeInt(this.pids.length);
            dos.writeInt(this.deity);
            for (int x = 0; x < this.pids.length; ++x) {
                dos.writeLong(this.pids[x]);
                dos.writeInt(this.karmas[x]);
            }
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, "Problem encoding for Deity " + this.deity + " - " + ex.getMessage(), ex);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
                barr = bos.toByteArray();
                StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
                this.setData(barr);
                throw throwable;
            }
            StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
            barr = bos.toByteArray();
            StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
            this.setData(barr);
        }
        StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
        barr = bos.toByteArray();
        StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
        this.setData(barr);
        return barr;
    }

    @Override
    public void execute() {
        new Thread(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new ByteArrayInputStream(WcEpicKarmaCommand.this.getData()));
                    int nums = dis.readInt();
                    int lDeity = dis.readInt();
                    Deity d = Deities.getDeity(lDeity == 3 ? 1 : lDeity);
                    for (int x = 0; x < nums; ++x) {
                        long pid = dis.readLong();
                        int val = dis.readInt();
                        if (d == null) continue;
                        d.setPlayerKarma(pid, val);
                    }
                }
                catch (IOException ex) {
                    try {
                        logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
                    }
                    catch (Throwable throwable) {
                        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                        throw throwable;
                    }
                    StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
        }.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearKarma() {
        for (Deity deity : Deities.getDeities()) {
            deity.clearKarma();
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getDeityDbCon();
            ps = dbcon.prepareStatement(CLEAR_KARMA);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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

    public static void loadAllKarmaHelpers() {
        for (Deity deity : Deities.getDeities()) {
            deity.loadAllKarmaHelpers();
        }
    }
}

